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

import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TerminateConfirmedRequesterActor} class models the requester
 * side of the provisionConfirmed message, and therefore, we are receiving a
 * provisionedConfirmed response from a child NSA.  We will correlate this
 * response to the original request and issue a provisionConfirm to the original
 * requesterNSA if all associated path segments are in the PROVISIONED state.
 *
 * @author hacksaw
 */
public class TerminateConfirmedRequesterActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@link onReceive} is required as part of the {@link UntypedActor}
     * interface so that this actor can receive work.
     */
    @Override
	public void onReceive(Object message) {
        logger.info("TerminateConfirmedRequesterActor.onReceive: received message.");
        if (message == null) {
            logger.error("TerminateConfirmedRequesterActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("TerminateConfirmedRequesterActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("TerminateConfirmedRequesterActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    /**
     * {@link process} will correlate this response to the original request.
     * We will set the appropriate path segment to TERMINATED, and if all path
     * segments are in the TERMINATED state, we can send a terminateConfirm
     * to the requesterNSA.
     */
    private void process(NsaMessage terminateConfirmedMessage) {
        NsaSecurityContext nsaSecurityContext = terminateConfirmedMessage.getNsaSecurityContext();
        String correlationId = terminateConfirmedMessage.getCorrelationId();
        String requesterNSA = terminateConfirmedMessage.getRequesterNSA();
        String providerNSA = terminateConfirmedMessage.getProviderNSA();
        String connectionId = terminateConfirmedMessage.getConnectionId();

        logger.info("TerminateConfirmedRequesterActor.process: Incoming message [" +
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
            logger.error("TerminateConfirmedRequesterActor.process: could not find correlationId " + correlationId + " in PendingOperationManager.");
            return;
        }
        else if (pendingOperation.getOperation() != PendingOperation.OperationType.Terminate) {
            logger.error("TerminateConfirmedRequesterActor.process: pending operation correlationId " + correlationId + " not a terminate operation " + pendingOperation.getOperation().name());
            return;
        }

        // This is a valid operation for our context so remove from queue.
        pendingOperations.remove(correlationId);

        // Now get our original path segment from the pendingOperation.
        PathSegment pathSegment = pendingOperation.getSegment();

        // Validate the connectionId.  We could validate more but...
        if (!connectionId.equals(pathSegment.getChildConnectionId())) {
            logger.error("TerminateConfirmedRequesterActor.process: connectionId=" + connectionId + " does no match stored childConnectionId=" + pathSegment.getChildConnectionId());
            return;
        }

        /*
         * Do a sanity check to make sure we are in the correct state for this
         * message.  We will accept a provisionConfirmed message only when we
         * are in the PROVISIONING state.  If this was a resend of a previous
         * provisionConfirmed then we should have already bailed out since
         * there is no matching pendingOperation response.
         */
        String parentConnectionId = pathSegment.getParentConnectionId();
        ConnectionStateType currentState = pathSegment.getCurrentState();
        logger.info("TerminateConfirmedRequesterActor.process: parentConnectionId=" + parentConnectionId + " the childConnectionId=" + connectionId + " is in " + currentState.name() + "  state");

        if (currentState != ConnectionStateType.TERMINATING &&
                currentState != ConnectionStateType.CLEANING &&
                currentState != ConnectionStateType.TERMINATED) {
            /*
             * We do not have the ability to send back an error to the sending
             * NSA for this message so we just have to silentlly fail.
             */
            logger.error("TerminateConfirmedRequesterActor.process: childConnectionId=" + connectionId + " not in TERMINATING or TERMINATED state");
            return;
        }

        // Set this path segment to appropriate state.
        pathSegment.setCurrentState(ConnectionStateType.TERMINATED);

        /*
         * Now we need to check the remaining pathSegments to see if we can
         * send a terminateConfirmed up the tree.  We will lookup the state
         * machine using the parentConnectionId as an index.
         */
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);
        if (stateMachine == null) {
            logger.error("TerminateConfirmedRequesterActor.process: could not find state machine for parentConnectionId=" + parentConnectionId);
            return;
        }

        /*
         * TODO: I need a lesson on how to make something Thread safe.  I have
         * no clue where I need it!
         *
         * Check each path in the stateMachine.
         */
        List<PathSegment> list = stateMachine.getRoutePathList();
        synchronized (list) {
            for (PathSegment path : list) {
                if (path.getCurrentState() != ConnectionStateType.TERMINATED) {
                    // We still have pending confirmations so we do nothing now.
                    return;
                }
            }
        }

        /*
         * All path segments are in the TERMINATED state so set the state
         * overall machine state to TERMINATED and fire a terminateConfirmed
         * up the tree.
         */
        ConnectionStateType state = stateMachine.getCurrentState();

        logger.info("TerminateConfirmedRequesterActor.process: transitioning stateMachine from " +
                state.name() + " to " + ConnectionStateType.TERMINATED.name());
        stateMachine.setCurrentState(ConnectionStateType.TERMINATED);

        if (state != ConnectionStateType.CLEANING) {
            // Build the provisionConfirmed message.
            NsaMessage confirmedMessage = new NsaMessage();
            confirmedMessage.setCorrelationId(pendingOperation.getParentCorrelationId());
            confirmedMessage.setConnectionId(parentConnectionId);
            confirmedMessage.setReplyTo(pendingOperation.getParentReplyTo());
            confirmedMessage.setGlobalReservationId(stateMachine.getGlobalReservationId());
            confirmedMessage.setConnectionState(stateMachine.getCurrentState());
            confirmedMessage.setNsaSecurityContext(stateMachine.getNsaSecurityContext());
            confirmedMessage.setProviderNSA(stateMachine.getProviderNSA());
            confirmedMessage.setRequesterNSA(stateMachine.getRequesterNSA());
            confirmedMessage.setMessageType(NsaMessage.MessageType.terminateConfirmedProvider);

            // Route this terminateConfirmed to the associated actor.
            castingDirector.send(confirmedMessage);
            logger.info("TerminateConfirmedRequesterActor.process: terminateConfirmed message issued to actor for processing");
        }

        logger.info("TerminateConfirmedRequesterActor.process: completed.");
    }

}
