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
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TerminateFailedRequesterActor} class models the requester
 * side of the provisionFailed message, and therefore, we are receiving a
 * provisionedFailed response from a child NSA.  We will correlate this
 * response to the original request and issue a provisionFailed to the original
 * requesterNSA and set both our state and the overall reservation state to
 * SCHEDULED.
 *
 * TODO: Determine where to trigger
 * @author hacksaw
 */
public class TerminateFailedRequesterActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@link onReceive} is required as part of the {@link UntypedActor}
     * interface so that this actor can receive work.
     */
    @Override
	public void onReceive(Object message) {
        logger.info("TerminateFailedRequesterActor.onReceive: received message.");
        if (message == null) {
            logger.error("TerminateFailedRequesterActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("TerminateFailedRequesterActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("TerminateFailedRequesterActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    /**
     * {@link process} will correlate this failed response to the original
     * request.  If we follow the defined state machine then a failed terminate
     * operation will result in us transitioning into the TERMINATED state
     * anyways.  The only difference is that we send the temrinateFailed up
     * the tree to the requesterNSA.
     */
    private void process(NsaMessage terminateFailedMessage) {
        NsaSecurityContext nsaSecurityContext = terminateFailedMessage.getNsaSecurityContext();
        String correlationId = terminateFailedMessage.getCorrelationId();
        String requesterNSA = terminateFailedMessage.getRequesterNSA();
        String providerNSA = terminateFailedMessage.getProviderNSA();
        String connectionId = terminateFailedMessage.getConnectionId();

        logger.info("TerminateFailedRequesterActor.process: Incoming message [" +
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
            logger.error("TerminateFailedRequesterActor.process: could not find correlationId " + correlationId + " in PendingOperationManager.");
            return;
        }
        else if (pendingOperation.getOperation() != PendingOperation.OperationType.Terminate) {
            logger.error("TerminateFailedRequesterActor.process: pending operation correlationId " + correlationId + " not a terminate operation " + pendingOperation.getOperation().name());
            return;
        }

        // This is a valid operation for our context so remove from queue.
        pendingOperations.remove(correlationId);

        // Now get our original path segment from the pendingOperation.
        PathSegment pathSegment = pendingOperation.getSegment();

        // Validate the connectionId.  We could validate more but...
        if (!connectionId.equals(pathSegment.getChildConnectionId())) {
            logger.error("TerminateFailedRequesterActor.process: connectionId=" + connectionId + " does no match stored childConnectionId=" + pathSegment.getChildConnectionId());
            return;
        }

        /*
         * Do a sanity check to make sure we are in the correct state for this
         * message.  We will accept a terminateFailed message only when we
         * are in the TERMINATING state.  If this was a resend of a previous
         * terminateFailed then we should have already bailed out since
         * there is no matching pendingOperation response.
         */
        String parentConnectionId = pathSegment.getParentConnectionId();
        ConnectionStateType currentState = pathSegment.getCurrentState();
        logger.info("TerminateFailedRequesterActor.process: parentConnectionId=" + parentConnectionId + " the childConnectionId=" + connectionId + " is in " + currentState.name() + "  state");

        if (currentState != ConnectionStateType.TERMINATING) {
            /*
             * We do not have the ability to send back an error to the sending
             * NSA for this message so we just have to silentlly fail.
             */
            logger.error("TerminateFailedRequesterActor.process: childConnectionId=" + connectionId + " not in TERMINATING state");
            return;
        }

        /*
         * TODO: I am here... We need to model a TERMINATE_FAILED state so we
         * can identify when we need to send a terminateFailed up the tree.  I
         * woudl like to wait until all messages come back to keep it message
         * handling consistent.  For this one we do not need to send a message
         * back down the tree to the other children.
         */


        /*
         * Set this path segment back to the TERMINATED state even though the
         * original request has failed.
         */
        pathSegment.setCurrentState(ConnectionStateType.TERMINATED);

        /*
         * We will lookup the state machine using the parentConnectionId as an
         * index.
         */
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);
        if (stateMachine == null) {
            logger.error("TerminateFailedRequesterActor.process: could not find state machine for parentConnectionId=" + parentConnectionId);
            return;
        }

        /*
         * We transition the parent state machine of this segment to a
         * TERMINATED state due to this segment's terminateFailed.  We leave all
         * children in whatever state they have achieved, but make sure not
         * to return any more messages to the parent for this request.
         */
        logger.info("TerminateFailedRequesterActor.process: transitioning stateMachine from " +
                stateMachine.getCurrentState().name() + " to " +
                ConnectionStateType.TERMINATED.name());
        stateMachine.setCurrentState(ConnectionStateType.TERMINATED);

        // Build the terminateConfirmed message.
        NsaMessage confirmedMessage = new NsaMessage();
        confirmedMessage.setCorrelationId(pendingOperation.getParentCorrelationId());
        confirmedMessage.setConnectionId(parentConnectionId);
        confirmedMessage.setReplyTo(pendingOperation.getParentReplyTo());
        confirmedMessage.setGlobalReservationId(stateMachine.getGlobalReservationId());
        confirmedMessage.setConnectionState(stateMachine.getCurrentState());
        confirmedMessage.setNsaSecurityContext(stateMachine.getNsaSecurityContext());
        confirmedMessage.setProviderNSA(stateMachine.getProviderNSA());
        confirmedMessage.setRequesterNSA(stateMachine.getRequesterNSA());
        confirmedMessage.setMessageType(NsaMessage.MessageType.terminateFailedProvider);

        // Route this provisionedConfirmed to the associated actor.
        castingDirector.send(confirmedMessage);
        logger.info("TerminateFailedRequesterActor.process: provisionFailed message issued to actor for processing");
    }

}
