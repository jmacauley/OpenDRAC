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
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperation;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.util.ExceptionCodes;
import org.opendrac.nsi.util.StartTimeScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class ProvisionRequesterLocalActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("ProvisionRequesterLocalActor.onReceive: received message.");
        if (message == null) {
            logger.error("ProvisionRequesterLocalActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("ProvisionRequesterLocalActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ProvisionRequesterLocalActor.onReceive: correlationId=" +
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
        CastingDirector castingDirector = dataManager.getCastingDirector();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();

        // Update the time we sent out the operation.
        PendingOperation pendingOperation = pendingOperations.get(correlationId);
        if (pendingOperation == null) {
            logger.error("ProvisionRequesterLocalActor.process: could not find pendingOperation for " + correlationId);
            return;
        }
        pendingOperation.setTimeSentNow();
        PathSegment pathSegment = pendingOperation.getSegment();
        String parentConnectionId = pathSegment.getParentConnectionId();
        String scheduleId = pathSegment.getData();

        // Get the state machine using the parent connectionId.
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);
        if (stateMachine == null) {
            logger.error("ProvisionRequesterLocalActor.process: could not find stateMachine for " + parentConnectionId);
            return;
        }

        /*
         * These are the valid states to receive a provision message.  We would
         * have already rejected the message in the ProvisionProviderActor for
         * invalid states on the parent state machine, however, we need to
         * handle our segment individually since some segments may already be
         * provisioned (ESnet requested the protocol leave already provisioned
         * segements in the PROVISIONED state if other segments fail).
         *
         * If we are in the PROVISIONED state then we can send a
         * provisionConfirmed back immediately.
         */
        ConnectionStateType segmentState = pathSegment.getCurrentState();
        if (segmentState == ConnectionStateType.PROVISIONED) {
            // Send back a provisionConfirmed.
            logger.info("ProvisionRequesterLocalActor.process: already provisioned - sending provisionConfirm back for scheduleId=" + scheduleId);

            NsaMessage childMessage = new NsaMessage();
            childMessage.setCorrelationId(correlationId);
            childMessage.setConnectionId(connectionId);
            childMessage.setConnectionState(pathSegment.getCurrentState());
            childMessage.setGlobalReservationId(globalReservationId);
            childMessage.setMessageType(NsaMessage.MessageType.provisionConfirmedRequester);
            childMessage.setProviderNSA(providerNSA);
            childMessage.setRequesterNSA(requesterNSA);

            // Route this message to the appropriate ReservationFailedRequesterActor for processing.
            castingDirector.send(childMessage);
            logger.info("ProvisionRequesterLocalActor.process: provisionConfirmed message issued to actor so returning");
            return;
        }
        else if (segmentState != ConnectionStateType.RESERVED &&
                segmentState != ConnectionStateType.SCHEDULED) {
            /*
             * We need to feed this back to the reserveFailedRequesterActor
             * to process with other responses.
             */
            ServiceException exception = ExceptionCodes.buildProviderException(ExceptionCodes.INVALID_STATE, "nrmSegmentState", segmentState.value());

            NsaMessage failedMessage = new NsaMessage();
            failedMessage.setMessageType(NsaMessage.MessageType.provisionFailedRequester);
            failedMessage.setCorrelationId(correlationId);
            failedMessage.setRequesterNSA(requesterNSA);
            failedMessage.setProviderNSA(providerNSA);
            failedMessage.setConnectionId(connectionId);
            failedMessage.setGlobalReservationId(globalReservationId);
            failedMessage.setPayload(exception.getFaultInfo());

            // Route this message to the appropriate ReservationFailedRequesterActor for processing.
            castingDirector.send(failedMessage);
            logger.error("ProvisionRequesterLocalActor.process: segment state invalid for connectionId=" + parentConnectionId + ", state=" + segmentState.name());
            return;
        }

        /*
         * We transition into the AUTO_PROVISION state to kick off this schedule
         * at startTime. If after startTime we should go into PROVISIONING but
         * we simplify this for the local NRM and just set to AUTO_PROVISION.
         */
        logger.info("ProvisionRequesterLocalActor.process: connectionId=" + parentConnectionId + " transitioning to state=" + ConnectionStateType.AUTO_PROVISION.name());
        pathSegment.setCurrentState(ConnectionStateType.AUTO_PROVISION);

        /*
         * Now we need to kick the startTime scheduler to look at this new
         * state machine state.  When it is time for provisioning to start on
         * the local NRM this will manage the transition.
         */
        logger.info("ProvisionRequesterLocalActor.process: kicking StartTimeScheduler.");
        StartTimeScheduler.getInstance().timerAudit(stateMachine.getStartTime());

        logger.info("ProvisionRequesterLocalActor.process: completed");
    }

}