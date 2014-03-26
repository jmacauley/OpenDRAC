/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.nrm;

import akka.actor.UntypedActor;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import java.util.GregorianCalendar;
import java.util.Map.Entry;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.actors.messages.NrmMessage;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperation;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.nrm.NrmProvision;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.topology.TopologyFactory;
import org.opendrac.nsi.util.ExceptionCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class NrmProvisionActor extends UntypedActor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("NrmProvisionActor.onReceive: received message.");
        if (message == null) {
            logger.error("NrmProvisionActor.onReceive: received null message");
        }
        else if (!NrmMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("NrmProvisionActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NrmMessage nrmMessage = (NrmMessage) message;
            logger.info("NrmProvisionActor.onReceive: parentConnectiontId=" +
                    nrmMessage.getParentConnectionId() +
                    ", childConnectionId=" + nrmMessage.getChildConnectionId() +
                    ", scheduleId=" + nrmMessage.getNrmScheduleId() +
                    ", messageType=" + nrmMessage.getNrmMessageType().name());
            process(nrmMessage);
        }
	}

    private void process(NrmMessage provisionMessage) {
        // Get a reference to the realted data managers.
        DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        TopologyFactory topologyManager = dataManager.getTopologyFactory();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();
        CastingDirector castingDirector = dataManager.getCastingDirector();

        // Get the state machine using the parent connectionId.
        StateMachine stateMachine = stateMachineManager.getStateMachine(provisionMessage.getParentConnectionId());
        if (stateMachine == null) {
            logger.error("NrmProvisionActor.process: could not find stateMachine for " + provisionMessage.getParentConnectionId());
            return;
        }

        // Find the pending operation corresponding to this provision request.
        PendingOperation operation = null;
        String correlationId = null;
        for (Entry<String, PendingOperation> entry : pendingOperations.getPendingOperations().entrySet()) {
            operation = entry.getValue();
            if (operation.getSegment().getChildConnectionId().equals(provisionMessage.getChildConnectionId()) &&
                    operation.getSegment().getParentConnectionId().equals(provisionMessage.getParentConnectionId()) &&
                    operation.getOperation() == PendingOperation.OperationType.Provision) {
                // We found the matching operation.
                correlationId = operation.getCorrelationId();
                break;
            }
        }

        // Did we find our pending operation?
        if (correlationId == null) {
            logger.error("NrmProvisionActor.process: could not find pending operation for local NRM path segment "
                    + provisionMessage.getParentConnectionId());
            return;
        }

        // Now we provision/active the reservation on the local NRM.
        PathSegment pathSegment = operation.getSegment();
        String parentConnectionId = pathSegment.getParentConnectionId();
        String providerNSA = pathSegment.getManagingNsaURN();
        String connectionId = pathSegment.getChildConnectionId();
        String globalReservationId = stateMachine.getGlobalReservationId();

        NrmProvision provision = new NrmProvision();
        try {
            logger.info("NrmProvisionActor.process: currentState=" + pathSegment.getCurrentState());

            if (pathSegment.getCurrentState() == ConnectionStateType.PROVISIONING ||
                    pathSegment.getCurrentState() == ConnectionStateType.AUTO_PROVISION) {
                provision.provision(providerNSA, provisionMessage.getNrmScheduleId());
                logger.info("NrmProvisionActor.process: Local NRM provision successful.");

                monitorProvision(providerNSA, provisionMessage.getNrmScheduleId());
                logger.info("NrmProvisionActor.process: Local NRM activation successful.");
            }

            // pathSegment.setCurrentState(ConnectionStateType.PROVISIONED);
        }
        catch (ServiceException nsi) {
            logger.error("NrmProvisionActor.process: Processing failure occured", nsi);

            /*
             * We need to feed this back to the reserveFailedRequesterActor
             * to process with other responses.  reserveFailedRequesterActor will
             * set the segment state machine back into an appropriate state.
             */
            NsaMessage failedMessage = new NsaMessage();
            failedMessage.setMessageType(NsaMessage.MessageType.provisionFailedRequester);
            failedMessage.setCorrelationId(correlationId);
            failedMessage.setRequesterNSA(providerNSA); // We are both the requester and provider for local requests.
            failedMessage.setProviderNSA(providerNSA);
            failedMessage.setConnectionId(connectionId);
            failedMessage.setGlobalReservationId(globalReservationId);
            failedMessage.setConnectionState(pathSegment.getCurrentState());
            failedMessage.setPayload(nsi.getFaultInfo());

            // Route this message to the appropriate ReservationFailedRequesterActor for processing.
            castingDirector.send(failedMessage);
            logger.info("NrmProvisionActor.process: provisionFailed message issued to actor so returning");
            return;
        }

        /*
         * NRM was successful provisioning the connection so return a
         * provisionConfirm back into the system for processing.
         */
        NsaMessage childMessage = new NsaMessage();
        childMessage.setCorrelationId(correlationId);
        childMessage.setConnectionId(provisionMessage.getChildConnectionId());
        childMessage.setConnectionState(ConnectionStateType.PROVISIONED);
        childMessage.setGlobalReservationId(stateMachine.getGlobalReservationId());
        childMessage.setMessageType(NsaMessage.MessageType.provisionConfirmedRequester);
        childMessage.setProviderNSA(stateMachine.getProviderNSA());
        childMessage.setRequesterNSA(stateMachine.getRequesterNSA());

        // Now we need to route this message to the appropriate actor.
        castingDirector.send(childMessage);
    }

    private static final int WAIT_ITERATIONS = 60;

    public void monitorProvision(String nsaIdentifier, String scheduleId) throws ServiceException {

        /*
         * Get an active login token.  This will throw a ServiceException if
         * there is an issue.
         */
        NrmManager nrmManager = NrmManager.getInstance();
        NrmLoginManager nrmLoginManager = nrmManager.getNrmLoginManager(nsaIdentifier);
        LoginToken token = nrmLoginManager.getToken();

        RequestHandler rh = RequestHandler.INSTANCE;
		try {
            DracService service = rh.getCurrentlyActiveServiceByScheduleId(token, scheduleId);
            int count = 0;
            while ((service == null || service.getStatus() != SERVICE.EXECUTION_INPROGRESS) && count < WAIT_ITERATIONS) {
                logger.info("NrmProvisionActor.monitorProvision: wait count = " + count++);
                Thread.sleep(1000);
                service = rh.getCurrentlyActiveServiceByScheduleId(token, scheduleId);
            }

            if (count < WAIT_ITERATIONS) {
                logger.info("NrmProvisionActor.monitorProvision: active scheduleId="
                        + scheduleId + ", serviceId=" + service.getId() +
                        ", serviceStatus=" + service.getStatus());
            }
            else {
                logger.error("OpenDRAC failed to activate scheduleId="
                        + scheduleId + ", serviceId=" + service.getId());
                throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMException", "OpenDRAC provision request failed: ");

            }
		}
        catch (Exception ex) {
			logger.error("OpenDRAC provision operation failed", ex);
			throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMException", "OpenDRAC provision request failed: " + ex.getMessage());
        }
    }
}
