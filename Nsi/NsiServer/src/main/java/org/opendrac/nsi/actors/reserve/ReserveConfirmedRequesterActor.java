/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.actors.reserve;

import akka.actor.UntypedActor;
import java.util.List;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
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
import org.opendrac.nsi.util.SpringApplicationContext;
import org.opendrac.nsi.util.StartTimeScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class ReserveConfirmedRequesterActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("ReserveConfirmedRequesterActor.onReceive: received message.");
        if (message == null) {
            logger.error("ReserveConfirmedRequesterActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("ReserveConfirmedRequesterActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ReserveConfirmedRequesterActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    private void process(NsaMessage reserveConfirmedMessage) {
        /*
         * We want to correlate this response to the original request.  We will
         * set the appropriate path segment to Reserved, and if all path segments
         * are in the reserved state, we can send a reservedConfirm to the
         * requesterNSA.
         */
        NsaSecurityContext nsaSecurityContext = reserveConfirmedMessage.getNsaSecurityContext();
        ReservationInfoType resInfo = (ReservationInfoType) reserveConfirmedMessage.getPayload();
        String correlationId = reserveConfirmedMessage.getCorrelationId();
        String requesterNSA = reserveConfirmedMessage.getRequesterNSA();
        String providerNSA = reserveConfirmedMessage.getProviderNSA();
        String connectionId = reserveConfirmedMessage.getConnectionId();

        logger.info("reserveConfirmedRequesterActor.process: Incoming message [" +
                ", providerNSA=" + providerNSA +
                ", requesterNSA=" + requesterNSA +
                ", correlationId=" + correlationId +
                ", connectionId=" + connectionId + "]");

        // Get references to our data managers.
		DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        CastingDirector castingDirector = dataManager.getCastingDirector();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();

        // Remove the associated pending operation.
        PendingOperation pendingOperation = pendingOperations.get(correlationId);
        if (pendingOperation == null) {
            logger.error("ReserveConfirmedRequesterActor.process: could not find correlationId " + correlationId + " in PendingOperationManager.");
            return;
        }
        else if (pendingOperation.getOperation() != PendingOperation.OperationType.Reserve) {
            logger.error("ReserveConfirmedRequesterActor.process: correlationId " + correlationId + " not a reserve operation " + pendingOperation.getOperation().name());
        }

        // This is a valid operation for our context so remove from queue.
        pendingOperations.remove(correlationId);

        // Now get our original path segment from the pendingOperation.
        PathSegment pathSegment = pendingOperation.getSegment();

        /*
         * There are a large number of parameters we could validate, but we
         * will pick only connectionId to make sure nothing is screwed up.
         *
         * TODO: Consider validating the reservation information returned
         * matches that requested.
         */
        if (!connectionId.equals(pathSegment.getChildConnectionId())) {
            logger.error("reserveConfirmedRequesterActor.process: connectionId=" + connectionId + " does no match stored childConnectionId=" + pathSegment.getChildConnectionId());
            return;
        }

        /*
         * Do a sanity check to make sure we are in the correct state for this
         * message.
         */
        String parentConnectionId = pathSegment.getParentConnectionId();
        ConnectionStateType currentState = pathSegment.getCurrentState();
        logger.info("reserveConfirmedRequesterActor.process: parentConnectionId=" + parentConnectionId + " is in " + currentState.name() + "  state");

        if (currentState != ConnectionStateType.RESERVING &&
                currentState != ConnectionStateType.RESERVED) {
            logger.error("reserveConfirmedRequesterActor.process: parentConnectionId=" + parentConnectionId + " not in RESERVING/RESERVED state");
            return;
        }

        // Set this path segment to reserved.
        pathSegment.setCurrentState(ConnectionStateType.RESERVED);

        /*
         * Now we need to check the remaining pathSegments to see if we can
         * send a reserveConfirmed up the tree.  We will lookup the state
         * machine using the parentConnectionId as an index.
         */
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);
        if (stateMachine == null) {
            logger.error("reserveConfirmedRequesterActor.process: could not find state machine for parentConnectionId=" + parentConnectionId);
            return;
        }

        // TODO: Thread safe?

        // Check each path in the stateMachine.
        List<PathSegment> list = stateMachine.getRoutePathList();
        synchronized (list) {
            for (PathSegment path : list) {
                if (path.getCurrentState() != ConnectionStateType.RESERVED) {
                    // We still have pending confirmations so we do nothing now.
                    return;
                }
            }

            /*
             * All path segments are in the reserved state so set the state machine
             * to RESERVED and fire a reserveConfirmed up the tree.
             */
            logger.info("reserveConfirmedRequesterActor.process: transitioning stateMachine from " +
                    stateMachine.getCurrentState().name() + " to " +
                    ConnectionStateType.RESERVED.name());
            stateMachine.setCurrentState(ConnectionStateType.RESERVED);
        }

        // Build the reserveConfirmed message.
        NsaMessage confirmedMessage = new NsaMessage();
        confirmedMessage.setCorrelationId(pendingOperation.getParentCorrelationId());
        confirmedMessage.setConnectionId(parentConnectionId);
        confirmedMessage.setReplyTo(pendingOperation.getParentReplyTo());
        confirmedMessage.setGlobalReservationId(stateMachine.getGlobalReservationId());
        confirmedMessage.setConnectionState(stateMachine.getCurrentState());
        confirmedMessage.setNsaSecurityContext(stateMachine.getNsaSecurityContext());
        confirmedMessage.setProviderNSA(stateMachine.getProviderNSA());
        confirmedMessage.setRequesterNSA(stateMachine.getRequesterNSA());

        // Should we do this here or in the reserveConfirmedProviderActor?
        ReservationInfoType newResInfo = new ReservationInfoType();
        newResInfo.setConnectionId(parentConnectionId);
        newResInfo.setDescription(stateMachine.getDescription());
        newResInfo.setGlobalReservationId(stateMachine.getGlobalReservationId());
        newResInfo.setPath(stateMachine.getPath());
        newResInfo.setServiceParameters(stateMachine.getServiceParameters());

        confirmedMessage.setPayload(newResInfo);
        confirmedMessage.setMessageType(NsaMessage.MessageType.reserveConfirmedProvider);

        // Route this reserveConfirmed to the associated actor.
        castingDirector.send(confirmedMessage);

        // Start a timer for this reservation.
        StartTimeScheduler.getInstance().timerAudit(stateMachine.getStartTime());

        logger.info("reserveConfirmedRequesterActor.process: reserveConfirmed message issued to actor for processing");
    }

}
