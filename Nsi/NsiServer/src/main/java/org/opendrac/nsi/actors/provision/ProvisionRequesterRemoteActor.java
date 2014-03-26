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
package org.opendrac.nsi.actors.provision;

import akka.actor.UntypedActor;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
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
import org.opendrac.nsi.util.StartTimeScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class ProvisionRequesterRemoteActor extends UntypedActor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("ProvisionRequesterRemoteActor.onReceive: received message.");
        if (message == null) {
            logger.error("ProvisionRequesterRemoteActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("ProvisionRequesterRemoteActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ProvisionRequesterRemoteActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    private void process(NsaMessage provisionMessage) {
        String correlationId = provisionMessage.getCorrelationId();
        String replyTo = provisionMessage.getReplyTo();
        String requesterNSA = provisionMessage.getRequesterNSA();
        String providerNSA = provisionMessage.getProviderNSA();
        String connectionId = provisionMessage.getConnectionId();
        String globalReservationId = provisionMessage.getGlobalReservationId();

        // Get handles to the data managers.
        DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        TopologyFactory topologyManager = dataManager.getTopologyFactory();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();

        // Update the time we sent out the operation.
        PendingOperation pendingOperation = pendingOperations.get(correlationId);
        if (pendingOperation == null) {
            logger.error("ProvisionRequesterRemoteActor.process: could not find pendingOperation for " + correlationId);
            return;
        }
        else if (pendingOperation.getOperation() != PendingOperation.OperationType.Provision) {
            logger.error("ProvisionRequesterRemoteActor.process: pending operation correlationId " + correlationId + " not a provision operation " + pendingOperation.getOperation().name());
            return;
        }

        pendingOperation.setTimeSentNow();
        PathSegment pathSegment = pendingOperation.getSegment();
        String parentConnectionId = pathSegment.getParentConnectionId();

        // Get the state machine using the parent connectionId.
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);
        if (stateMachine == null) {
            logger.error("ProvisionRequesterRemoteActor.process: could not find stateMachine for " + parentConnectionId);
            return;
        }

        // Update this path segments connection state.
        GregorianCalendar startTime = stateMachine.getStartTime();
        GregorianCalendar now = new GregorianCalendar();

        if (startTime.before(now)) {
            logger.info("ProvisionRequesterRemoteActor.process: startTime is passed for connectionId=" + parentConnectionId + ", so setting state=" + ConnectionStateType.PROVISIONING.name());
            pathSegment.setCurrentState(ConnectionStateType.PROVISIONING);
        }
        else {
            logger.info("ProvisionRequesterRemoteActor.process: startTime is in future for connectionId=" + parentConnectionId + ", so setting state=" + ConnectionStateType.AUTO_PROVISION.name());
            pathSegment.setCurrentState(ConnectionStateType.AUTO_PROVISION);
        }

		// Log the incoming message information.
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        logger.info(
                "ProvisionRequesterRemoteActor.process: Sending provsionRequest to local NRM [" +
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
            logger.error("ProvisionRequesterRemoteActor: could not find endpoint information for NSA=" + providerNSA);
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

        ProvisionRequestType request = new ProvisionRequestType();
        request.setReplyTo(replyTo);
        request.setCorrelationId(correlationId);
        request.setProvision(requestType);

        // Send the provisioning message to the remote NSA.
        GenericAcknowledgmentType result = null;
        try {
            logger.info("ProvisionRequesterRemoteActor.process: Issuing provision request to NSA" + provisionMessage.getProviderNSA());
            result = proxy.getProxy().provision(request);
            logger.info("ProvisionRequesterRemoteActor.process: completed successfully returning correlationId=" + result.getCorrelationId());
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException ex) {
            // TODO: Need error handling and retry for communication errors.
            logger.info("ProvisionRequesterRemoteActor.process: received exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            return;
        }

        // Check to make sure we got back the correct correlationId.
        if (result.getCorrelationId().equalsIgnoreCase(provisionMessage.getCorrelationId())) {
            // No clue what happened - must have been a provider NSA error.
            logger.error("ProvisionRequesterRemoteActor.process: failed ack for " +
                    "providerNSA=" + provisionMessage.getProviderNSA() +
                    ", correlationId=" + provisionMessage.getCorrelationId() +
                    ", received correlationId=" + result.getCorrelationId());

            // Update this path segments connection state.
            pathSegment.setCurrentState(ConnectionStateType.UNKNOWN);

            /*
             * TODO: This should be an error that causes us to back out the
             * entire reservation and return a reservation failed message.
             */
            return;
        }

        logger.info("ProvisionRequesterRemoteActor.process: successfully sent providerNSA=" +
                provisionMessage.getProviderNSA() +
                ", correlationId=" + provisionMessage.getCorrelationId());

        // We are done.
        logger.info("ProvisionRequesterRemoteActor.process: Completed.");
    }
}
