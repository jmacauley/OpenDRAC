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
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperation;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.nrm.NrmTerminate;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class TerminateRequesterLocalActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("TerminateRequesterLocalActor.onReceive: received message.");
        if (message == null) {
            logger.error("TerminateRequesterLocalActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("TerminateRequesterLocalActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("TerminateRequesterLocalActor.onReceive: correlationId=" +
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
        CastingDirector castingDirector = dataManager.getCastingDirector();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();

        // Update the time we sent out the operation.
        PendingOperation pendingOperation = pendingOperations.get(correlationId);
        if (pendingOperation == null) {
            logger.error("TerminateRequesterLocalActor.process: could not find pendingOperation for " + correlationId);
            return;
        }
        pendingOperation.setTimeSentNow();
        PathSegment pathSegment = pendingOperation.getSegment();
        String parentConnectionId = pathSegment.getParentConnectionId();

        // Get the state machine using the parent connectionId.
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);

        // We can indicate a transition to the terminating state.
        ConnectionStateType segmentState = pathSegment.getCurrentState();
        if (segmentState == ConnectionStateType.TERMINATED) {
            logger.error("TerminateRequesterLocalActor.process: segment state invalid for connectionId=" + parentConnectionId + ", state=" + segmentState.name());
        }
        else {
            pathSegment.setCurrentState(ConnectionStateType.TERMINATING);
        }

		// Log the incoming message information.
        String scheduleId = pathSegment.getData();
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        logger.info(
                "TerminateRequesterLocalActor.process: Sending terminateRequest to local NRM [" +
                ", globalReservationId=" + globalReservationId +
                ", parentConnectionId=" + pathSegment.getParentConnectionId() +
                ", childConnectionId=" + pathSegment.getChildConnectionId() +
                ", connectionId=" + connectionId +
                ", correlationId=" + correlationId +
                ", OpenDRAC scheduleId=" + scheduleId +
                ", sourcePort=" + pathSegment.getSourceStpURN() +
                ", destPort=" + pathSegment.getDestStpURN() +
                ", desiredBandwidth=" + stateMachine.getDesiredBandwidth() +
                ", startTime=" + df.format(new Date(stateMachine.getStartTime().getTimeInMillis())) +
                ", endTime=" + df.format(new Date(stateMachine.getEndTime().getTimeInMillis())) +
                "]");

        NrmTerminate terminate = new NrmTerminate();

        try {
            logger.info("TerminateRequesterLocalActor.process: before NrmTerminate " + scheduleId);
            terminate.terminate(providerNSA, scheduleId);
            logger.info("TerminateRequesterLocalActor.process: after NrmTerminate " + scheduleId);
        }
        catch (ServiceException nsi) {
            logger.error("TerminateRequesterLocalActor.process: Processing failure occured", nsi);

            /*
             * We need to feed this back to the terminateFailedRequesterActor
             * to process with other responses.
             */
            NsaMessage failedMessage = new NsaMessage();
            failedMessage.setMessageType(NsaMessage.MessageType.terminateFailedRequester);
            failedMessage.setCorrelationId(correlationId);
            failedMessage.setRequesterNSA(requesterNSA);
            failedMessage.setProviderNSA(providerNSA);
            failedMessage.setConnectionId(connectionId);
            failedMessage.setGlobalReservationId(globalReservationId);
            failedMessage.setPayload(nsi.getFaultInfo());

            // Route this message to the appropriate ReservationFailedRequesterActor for processing.
            castingDirector.send(failedMessage);
            logger.info("TerminateRequesterLocalActor.process: provisionFailed message issued to actor so returning");
            return;
        }

        // Now we need to feed a ReservationConfirmation back into the system.
        NsaMessage confirmedMessage = new NsaMessage();
        confirmedMessage.setCorrelationId(correlationId);
        confirmedMessage.setConnectionId(connectionId);
        confirmedMessage.setConnectionState(pathSegment.getCurrentState());
        confirmedMessage.setGlobalReservationId(globalReservationId);
        confirmedMessage.setMessageType(NsaMessage.MessageType.terminateConfirmedRequester);
        confirmedMessage.setProviderNSA(providerNSA);
        confirmedMessage.setRequesterNSA(requesterNSA);

        // Now we need to route this message to the appropriate actor.
        castingDirector.send(confirmedMessage);
        logger.info("TerminateRequesterLocalActor.process: completed");
    }

}