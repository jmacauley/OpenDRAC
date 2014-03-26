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
package org.opendrac.nsi.actors.reserve;

import akka.actor.UntypedActor;
import java.util.List;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.actors.messages.TerminateMessage;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperation;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.security.NsaSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class ReserveFailedRequesterActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("ReserveFailedRequesterActor.onReceive: received message.");
        if (message == null) {
            logger.error("ReserveFailedRequesterActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("ReserveFailedRequesterActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ReserveFailedRequesterActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    private void process(NsaMessage reservefailedMessage) {
        /*
         * We want to correlate this response to the original request.  We will
         * set the appropriate path segment to TERMINATED, and if all path
         * segments are returned, we will initate error recovery by cancelling
         * all child reservations and sending a reserveFailed up the tree to the
         * requesterNSA.
         */
        NsaSecurityContext nsaSecurityContext = reservefailedMessage.getNsaSecurityContext();
        String correlationId = reservefailedMessage.getCorrelationId();
        String requesterNSA = reservefailedMessage.getRequesterNSA();
        String providerNSA = reservefailedMessage.getProviderNSA();
        String connectionId = reservefailedMessage.getConnectionId();
        ServiceExceptionType exception = (ServiceExceptionType) reservefailedMessage.getPayload();

        logger.info("ReserveFailedRequesterActor.process: Incoming message [" +
                ", providerNSA=" + providerNSA +
                ", requesterNSA=" + requesterNSA +
                ", correlationId=" + correlationId +
                ", connectionId=" + connectionId +
                ", errorId=" + exception.getErrorId() +
                ", text=" + exception.getText() +
                "]");

        // Get references to our data managers.
		DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        CastingDirector castingDirector = dataManager.getCastingDirector();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();

        // Remove the associated pending operation.
        PendingOperation pendingOperation = pendingOperations.get(correlationId);
        if (pendingOperation == null) {
            logger.error("ReserveFailedRequesterActor.process: could not find correlationId " + correlationId + " in PendingOperationManager.");
            return;
        }
        else if (pendingOperation.getOperation() != PendingOperation.OperationType.Reserve) {
            logger.error("ReserveFailedRequesterActor.process: correlationId " + correlationId + " not a reserve operation " + pendingOperation.getOperation().name());
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
            logger.error("ReserveFailedRequesterActor.process: connectionId=" + connectionId + " does no match stored childConnectionId=" + pathSegment.getChildConnectionId());
            return;
        }

        /*
         * Do a sanity check to make sure we are in the correct state for this
         * message.
         */
        String parentConnectionId = pathSegment.getParentConnectionId();
        ConnectionStateType currentState = pathSegment.getCurrentState();
        logger.info("ReserveFailedRequesterActor.process: parentConnectionId=" + parentConnectionId + " is in " + currentState.name() + "  state");

        if (currentState != ConnectionStateType.RESERVING &&
                currentState != ConnectionStateType.RESERVED) {
            logger.error("ReserveFailedRequesterActor.process: parentConnectionId=" + parentConnectionId + " not in RESERVING/RESERVED state");
            return;
        }

        // Set this path segment to TERMINATED.
        pathSegment.setCurrentState(ConnectionStateType.TERMINATED);

        /*
         * Now we need to check the remaining pathSegments to see if we can
         * send a reserveFailed up the tree.  We will lookup the state
         * machine using the parentConnectionId as an index.
         */
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);
        if (stateMachine == null) {
            logger.error("ReserveFailedRequesterActor.process: could not find state machine for parentConnectionId=" + parentConnectionId);
            return;
        }

        // Set the state machine to a cleaning state while sending terminates.
        logger.info("ReserveFailedRequesterActor.process: transitioning stateMachine from " +
                    stateMachine.getCurrentState().name() + " to " +
                    ConnectionStateType.CLEANING.name());
        stateMachine.setCurrentState(ConnectionStateType.CLEANING);

        /*
         * TODO: Now we want to issue terminate requests to all children in the
         * RESERVED or RESERVING state.
         */
        List<PathSegment> list = stateMachine.getRoutePathList();
        synchronized (list) {
            boolean sent = false;
            for (PathSegment path : list) {
                if (path.getCurrentState() == ConnectionStateType.RESERVED ||
                        path.getCurrentState() == ConnectionStateType.RESERVING) {
                    logger.info("ReserveFailedRequesterActor.process: Routing terminate requests to NSA = " +
                    ", NsaType = " + path.getNsaType().name() +
                    ", childConnectionId = " + path.getChildConnectionId() +
                    ", sourceSTP = " + path.getSourceStpURN() +
                    ", destSTP = " + path.getDestStpURN());

                    sent = true;
                    TerminateMessage termMessage = new TerminateMessage();

                    NsaMessage childMessage = termMessage.getRequestMessage(
                        null, stateMachine.getGlobalReservationId(), null,
                        providerNSA, path);

                    // Give this to the casting director to find us an actor.
                    castingDirector.send(childMessage);
                }
            }

            // We might not need to wait for any responses and can transition now.
            if (sent == false) {
                stateMachine.setCurrentState(ConnectionStateType.TERMINATED);
            }
        }

        // Build the reserveFailed message.
        NsaMessage failedMessage = new NsaMessage();
        failedMessage.setCorrelationId(pendingOperation.getParentCorrelationId());
        failedMessage.setConnectionId(parentConnectionId);
        failedMessage.setReplyTo(pendingOperation.getParentReplyTo());
        failedMessage.setGlobalReservationId(stateMachine.getGlobalReservationId());
        failedMessage.setConnectionState(stateMachine.getCurrentState());
        failedMessage.setNsaSecurityContext(stateMachine.getNsaSecurityContext());
        failedMessage.setProviderNSA(stateMachine.getProviderNSA());
        failedMessage.setRequesterNSA(stateMachine.getRequesterNSA());
        failedMessage.setPayload(exception);
        failedMessage.setMessageType(NsaMessage.MessageType.reserveFailedProvider);

        // Route this reserveFailed to the associated actor.
        castingDirector.send(failedMessage);
        logger.info("ReserveFailedRequesterActor.process: reserveFailed message issued to actor for processing");
    }

}
