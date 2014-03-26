/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.actors.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperation;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.topology.TopologyFactory;
import org.opendrac.nsi.util.UUIDUtilities;

/**
 *
 * @author hacksaw
 */
public class TerminateMessage {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public NsaMessage getRequestMessage(
            String parentCorrelationId, // Null if not going back to a parent.
            String globalReservationId,
            String replyTo,             // Null is not going back to a parent.
            String myNSA,
            PathSegment segment) {

        // Get a reference to the related data managers.
		DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();
        TopologyFactory topology = dataManager.getTopologyFactory();

            // Create request message to child NSA or NRM.
        NsaMessage childMessage = new NsaMessage();

        /*
         * We need to allocate a new correlationId for this request
         * targetting the child.
         */
        String childCorrelationId = UUIDUtilities.getUrnUuid();
        childMessage.setCorrelationId(childCorrelationId);

        // Set replyTo to our ConnectionServicesRequester endpoint.
        childMessage.setReplyTo(nsaConfiguration.getNsaRequesterEndpoint(myNSA));

        /*
         * The reqesterNSA is the original NSnetwork ID sent in the request.
         */
        childMessage.setRequesterNSA(myNSA);

        // ProviderNSA is the one targeted in the PathSeqment.
        childMessage.setProviderNSA(topology.getNsaByURN(segment.getManagingNsaURN()).getNsaURN());
        childMessage.setConnectionId(segment.getChildConnectionId());
        childMessage.setGlobalReservationId(globalReservationId);

        // Now store this new operation in the PendingOperationManager.
        PendingOperation pendingOperation = new PendingOperation();
        pendingOperation.setOperation(PendingOperation.OperationType.Terminate);
        pendingOperation.setCorrelationId(childCorrelationId);
        pendingOperation.setParentCorrelationId(parentCorrelationId);
        pendingOperation.setParentReplyTo(replyTo);
        pendingOperation.setSegment(segment);
        pendingOperations.add(pendingOperation);

        // Route to remote actor if we need to send via NSI protocol.
        if (segment.getNsaType() == PathSegment.NsaType.REMOTE) {
            logger.info("TerminateMessage.getRequestMessage: routing to " +
                  NsaMessage.MessageType.terminateRequesterRemote.name());
            childMessage.setMessageType(NsaMessage.MessageType.terminateRequesterRemote);
        }
        // Route locally if this targets the local NRM.
        else {
            logger.info("TerminateMessage.getRequestMessage: routing to " +
                  NsaMessage.MessageType.terminateRequesterLocal.name());
            childMessage.setMessageType(NsaMessage.MessageType.terminateRequesterLocal);
        }

        return childMessage;
    }

}
