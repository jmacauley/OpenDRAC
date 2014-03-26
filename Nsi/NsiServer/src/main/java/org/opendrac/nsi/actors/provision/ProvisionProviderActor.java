/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.actors.provision;

import akka.actor.UntypedActor;
import java.util.List;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperation;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.security.NsaSecurityContext;
import org.opendrac.nsi.security.SessionSecurity;
import org.opendrac.nsi.topology.TopologyFactory;
import org.opendrac.nsi.util.ExceptionCodes;
import org.opendrac.nsi.util.MessageDump;
import org.opendrac.nsi.util.UUIDUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class ProvisionProviderActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("ProvisionProviderActor received message");
        if (NsaMessage.class.isAssignableFrom(message.getClass())) {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ProvisionProviderActor correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    private void process(NsaMessage provisionMessage) {

        // We have already verified this field is present and not null.
        NsaSecurityContext nsaSecurityContext = provisionMessage.getNsaSecurityContext();
        GenericRequestType provision = (GenericRequestType) provisionMessage.getPayload();
        String correlationId = provisionMessage.getCorrelationId();
        String replyTo = provisionMessage.getReplyTo();
        String requesterNSA = provisionMessage.getRequesterNSA();
        String providerNSA = provisionMessage.getProviderNSA();
        String connectionId = provisionMessage.getConnectionId();

		// Log the incoming message information.
		if (logger.isDebugEnabled()) {
			logger.debug("ProvisionProviderActor.process: Incoming message [" +
					nsaSecurityContext.getUserName() + " @ " +
					nsaSecurityContext.getRemoteAddr() +
                    ", requesterNSA=" + requesterNSA +
                    ", correlationId=" + correlationId + "]");
			logger.debug(MessageDump.dump(GenericRequestType.class, provision));
		}

        /*
         * TODO: In the next iteration we can apply any needed access control
         * based on the NSA request the resource.  In general, we will only
         * want to give them access to the endpoints within their associated
         * groups.  Whether we trust an NSA 100% and only enforce user level
         * access control will need to be determined.
         */

        // Get a reference to the related data managers.
		DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        CastingDirector castingDirector = dataManager.getCastingDirector();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();
        TopologyFactory topology = dataManager.getTopologyFactory();

        /*
         * If we find any error messages from this point forward we need to
         * send a provisionFailed message back to the requesterNSA.
         */
        StateMachine machine = null;
        try {
            /*
             * Verify we have an existing reservation with the same
             * connectionId.
             */
            machine = stateMachineManager.failOnNoStateMachine(connectionId);

            /*
             * Now we must determine if we are in a correct state to receive
             * a provision request.
             */
            failOnInvalidProvisionState(machine);

            // Parse the session security so we can do an access control check.
            SessionSecurity sessionSecurity = new SessionSecurity();
            sessionSecurity.parseSessionSecurityAttr(provision.getSessionSecurityAttr());

            /*
             * TODO: compare security to determine if sending NSA/User can
             * manipulate this state machine using credentials stored in
             * state machine.
             */
        }
        catch (ServiceException nsi) {
            /*
             * Send this to a web services actor for delivery of a
             * ReservationFailed message.
             */
            logger.info("ProvisionProviderActor.process: Sending provisionFailed message " + nsi.toString());

            // Leave the state where it is for now.

            NsaMessage failedMessage = new NsaMessage();
            failedMessage.setCorrelationId(correlationId);
            failedMessage.setReplyTo(replyTo);
            failedMessage.setRequesterNSA(requesterNSA);
            failedMessage.setProviderNSA(providerNSA);
            failedMessage.setConnectionId(connectionId);
            if (machine != null) {
                failedMessage.setConnectionState(machine.getCurrentState());
                failedMessage.setGlobalReservationId(machine.getGlobalReservationId());
            }

            failedMessage.setMessageType(NsaMessage.MessageType.provisionFailedProvider);
            failedMessage.setPayload(nsi.getFaultInfo());

            // Route this message to the appropriate actor for processing.
            castingDirector.send(failedMessage);
            logger.info("ProvisionProviderActor.process: provisionFailed message issued to actor so returning");
            return;
        }

        /*
         * If we were already in the provisioned state then we can send back a
         * provisionConfirmed message immediately and leave the state machine
         * in the PROVISIONED state.
         */
        if (machine.getCurrentState() == ConnectionStateType.PROVISIONED ||
                machine.getCurrentState() == ConnectionStateType.TERMINATED) {
            NsaMessage childMessage = new NsaMessage();
            childMessage.setCorrelationId(correlationId);
            childMessage.setConnectionId(connectionId);
            childMessage.setConnectionState(machine.getCurrentState());
            childMessage.setGlobalReservationId(machine.getGlobalReservationId());
            childMessage.setMessageType(NsaMessage.MessageType.provisionConfirmedProvider);
            childMessage.setProviderNSA(providerNSA);
            childMessage.setRequesterNSA(requesterNSA);
            childMessage.setReplyTo(replyTo);

            castingDirector.send(childMessage);
            logger.info("ProvisionProviderActor.process: provisionConfirmed message issued to actor for processing");
        }

        // Transition state machine to appropriate state.
        if (machine.getCurrentState() == ConnectionStateType.RESERVED) {
            machine.setCurrentState(ConnectionStateType.AUTO_PROVISION);
        }
        else if (machine.getCurrentState() == ConnectionStateType.SCHEDULED) {
            machine.setCurrentState(ConnectionStateType.PROVISIONING);
        }

        /*
         * For all path segments in the list that are managed by the local
         * NSA we will send a provisionRequest to local NSA actors, while path
         * segments on remote NSA will be sent to remote NSA actors via the
         * NSI protocol.
         */
        List<PathSegment> pathList = machine.getRoutePathList();

        for (PathSegment segment : pathList) {
            logger.info("ProvisionProviderActor.process: Routing provison requests to NSA = " +
                    segment.getManagingNsaURN() +
                    ", NsaType = " + segment.getNsaType().name() +
                    ", childConnectionId = " + segment.getChildConnectionId() +
                    ", sourceSTP = " + segment.getSourceStpURN() +
                    ", destSTP = " + segment.getDestStpURN());

            // Create request message to child NSA or NRM.
            NsaMessage childMessage = new NsaMessage();

            /*
             * We need to allocate a new correlationId for this request
             * targetting the child.
             */
            String childCorrelationId = UUIDUtilities.getUrnUuid();
            childMessage.setCorrelationId(childCorrelationId);

            // Set replyTo to our ConnectionServicesRequester endpoint.
            childMessage.setReplyTo(nsaConfiguration.getNsaRequesterEndpoint(providerNSA));

            /*
             * The reqesterNSA is this NSA so use the original providerNSA id
             * instead of looking up ours just in case we are trying to
             * support multiple NSA.
             */
            childMessage.setRequesterNSA(providerNSA);

            // ProviderNSA is the one targeted in the PathSeqment.
            childMessage.setProviderNSA(segment.getManagingNsaURN());
            childMessage.setConnectionId(segment.getChildConnectionId());
            childMessage.setGlobalReservationId(machine.getGlobalReservationId());
            childMessage.setNsaSecurityContext(nsaSecurityContext);

            // Now store this new operation in the PendingOperationManager.
            PendingOperation pendingOperation = new PendingOperation();
            pendingOperation.setOperation(PendingOperation.OperationType.Provision);
            pendingOperation.setCorrelationId(childCorrelationId);
            pendingOperation.setParentCorrelationId(correlationId);
            pendingOperation.setParentReplyTo(replyTo);
            pendingOperation.setSegment(segment);
            pendingOperations.add(pendingOperation);

            // Route to remote actor if we need to send via NSI protocol.
            if (segment.getNsaType() == PathSegment.NsaType.REMOTE) {
                logger.info("ProvisionProviderActor.process: routing to " +
                      NsaMessage.MessageType.provisionRequesterRemote.name());
                childMessage.setMessageType(NsaMessage.MessageType.provisionRequesterRemote);
            }
            // Route locally if this targets the local NRM.
            else {
                logger.info("ProvisionProviderActor.process: routing to " +
                      NsaMessage.MessageType.provisionRequesterLocal.name());
                childMessage.setMessageType(NsaMessage.MessageType.provisionRequesterLocal);
            }

            // Give this to the casting director to find us an actor.
            castingDirector.send(childMessage);
            logger.info("ProvisionProviderActor.process: provisionRequest message issued to actor for processing");
        }

        // We are done - all other processing is handled by other actors.
        logger.info("ProvisionProviderActor.process: provisionRequests issued to children NSA so returning with state=" + machine.getCurrentState());
    }

    public void failOnInvalidProvisionState(StateMachine machine) throws ServiceException {

        // Check for valid states to accept provision message.
        ConnectionStateType state = machine.getCurrentState();

        if (state == ConnectionStateType.RESERVED ||
                state == ConnectionStateType.SCHEDULED ||
                state == ConnectionStateType.PROVISIONED ||
                state == ConnectionStateType.TERMINATED) {
            return;
        }

        logger.info("ProvisionProviderActor.failOnInvalidProvisionState: State machine in invalid state for provision message state=" + state.name() + ", connectionId=" + machine.getConnectionId());
        throw ExceptionCodes.buildProviderException(ExceptionCodes.INVALID_STATE, "connectionId", machine.getConnectionId());
    }

}