/**
 * Copyright (c) 2011, SURFnet bv, The Netherlands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
 * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package org.opendrac.nsi.actors.terminate;

import akka.actor.UntypedActor;
import java.text.DateFormat;
import java.util.Date;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.client.RequesterClientProxy;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.config.xml.AuthenticationInfoType;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperation;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.topology.TopologyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class TerminateRequesterRemoteActor extends UntypedActor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("TerminateRequesterRemoteActor.onReceive: received message.");
        if (message == null) {
            logger.error("TerminateRequesterRemoteActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("TerminateRequesterRemoteActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("TerminateRequesterRemoteActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    private void process(NsaMessage terminateMessage) {
        String correlationId = terminateMessage.getCorrelationId();
        String replyTo = terminateMessage.getReplyTo();
        String requesterNSA = terminateMessage.getRequesterNSA();
        String providerNSA = terminateMessage.getProviderNSA();
        String connectionId = terminateMessage.getConnectionId();
        String globalReservationId = terminateMessage.getGlobalReservationId();

        // Get handles to the data managers.
        DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        TopologyFactory topologyManager = dataManager.getTopologyFactory();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();

        // Update the time we sent out the operation.
        PendingOperation pendingOperation = pendingOperations.get(correlationId);
        if (pendingOperation == null) {
            logger.error("TerminateRequesterRemoteActor.process: could not find pendingOperation for " + correlationId);
            return;
        }
        pendingOperation.setTimeSentNow();
        PathSegment pathSegment = pendingOperation.getSegment();
        String parentConnectionId = pathSegment.getParentConnectionId();

        // Get the state machine using the parent connectionId.
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);

		// Log the incoming message information.
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        logger.info(
                "TerminateRequesterRemoteActor.process: Sending provsionRequest to local NRM [" +
                ", globalReservationId=" + globalReservationId +
                ", parentConnectionId=" + pathSegment.getParentConnectionId() +
                ", childConnectionId=" + pathSegment.getChildConnectionId() +
                ", connectionId=" + connectionId +
                ", correlationId=" + correlationId +
                ", sourcePort=" + pathSegment.getSourceStpURN() +
                ", destPort=" + pathSegment.getDestStpURN() +
                ", desiredBandwidth=" + stateMachine.getDesiredBandwidth() +
                ", startTime=" + df.format(new Date(stateMachine.getStartTime().getTimeInMillis())) +
                ", endTime=" + df.format(new Date(stateMachine.getEndTime().getTimeInMillis())) +
                ", replyTo=" + replyTo +
                "]");

        // Get the authentication information for this remote providerNSA.
        AuthenticationInfoType auth = nsaConfiguration.getNsaAuthenticationInfo(providerNSA);

        // Lookup provider endpoint information in the global topology.
        String providerEndpoint = topologyManager.getCsProviderEnpointByNsaURN(providerNSA);

        if (providerEndpoint == null || providerEndpoint.isEmpty()) {
            // TODO: Add error handling here.
            logger.error("TerminateRequesterLocalActor: could not find endpoint information for NsNetwork=" + providerNSA);
            return;
        }

        // Allocate and set up an NSI proxy.
        RequesterClientProxy proxy = new RequesterClientProxy();

        // Configure the request with the remote endpoint information.
        proxy.setProviderEndpoint(providerEndpoint, auth.getUserId(), auth.getPassword());

        // Build the provision request.
        GenericRequestType requestType = new GenericRequestType();
        requestType.setConnectionId(connectionId);
        requestType.setRequesterNSA(requesterNSA);
        requestType.setProviderNSA(providerNSA);
        requestType.setSessionSecurityAttr(stateMachine.getSessionSecurity().getStatement());

        TerminateRequestType request = new TerminateRequestType();
        request.setReplyTo(replyTo);
        request.setCorrelationId(correlationId);
        request.setTerminate(requestType);

        // Update this path segments connection state.
        if (pathSegment.getCurrentState() == ConnectionStateType.TERMINATED) {
            logger.info("TerminateRequesterRemoteActor.process: pathSegment already in TERMINATED state " + pathSegment.toString());
        }
        else {
            pathSegment.setCurrentState(ConnectionStateType.TERMINATING);
            logger.info("TerminateRequesterRemoteActor.process: transitioning pathSegment to TERMINATING state " + pathSegment.toString());
        }

        // Send the terminate message to the remote NSA.
        GenericAcknowledgmentType result = null;
        try {
            logger.info("TerminateRequesterRemoteActor.process: Issuing terminate request to NSA" + terminateMessage.getProviderNSA());
            result = proxy.getProxy().terminate(request);
            logger.info("TerminateRequesterRemoteActor.process: completed successfully returning correlationId=" + result.getCorrelationId());
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException ex) {
            // TODO: Need error handling and retry for communication errors.
            logger.info("TerminateRequesterRemoteActor.process: received exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            return;
        }

        // Check to make sure we got back the correct correlationId.
        if (result.getCorrelationId().equalsIgnoreCase(terminateMessage.getCorrelationId())) {
            // No clue what happened - must have been a provider NSA error.
            logger.error("TerminateRequesterRemoteActor.process: failed ack for " +
                    "providerNSA=" + terminateMessage.getProviderNSA() +
                    ", correlationId=" + terminateMessage.getCorrelationId() +
                    ", received correlationId=" + result.getCorrelationId());

            // Update this path segments connection state.
            pathSegment.setCurrentState(ConnectionStateType.UNKNOWN);

            /*
             * TODO: This should be an error that causes us to back out the
             * entire reservation and return a reservation failed message.
             */
            return;
        }

        logger.info("TerminateRequesterRemoteActor.process: successfully sent providerNSA=" +
                terminateMessage.getProviderNSA() +
                ", correlationId=" + terminateMessage.getCorrelationId());

        // We are done.
        logger.info("TerminateRequesterRemoteActor.process: Completed.");
    }
}
