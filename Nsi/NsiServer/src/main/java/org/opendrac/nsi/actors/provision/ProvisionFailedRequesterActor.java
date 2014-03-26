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
import java.util.GregorianCalendar;
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
 * The {@link ProvisionFailedRequesterActor} class models the requester
 * side of the provisionFailed message, and therefore, we are receiving a
 * provisionedFailed response from a child NSA.  We will correlate this
 * response to the original request and issue a provisionFailed to the original
 * requesterNSA and set both our state and the overall reservation state to
 * SCHEDULED.
 *
 * TODO: Determine where to trigger
 * @author hacksaw
 */
public class ProvisionFailedRequesterActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@link onReceive} is required as part of the {@link UntypedActor}
     * interface so that this actor can receive work.
     */
    @Override
	public void onReceive(Object message) {

        if (NsaMessage.class.isAssignableFrom(message.getClass())) {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ProvisionFailedRequesterActor Received message: " + nsaMessage.getCorrelationId());
            logger.info("ProvisionFailedRequesterActor Processing: " + nsaMessage.getMessageType().toString());

            process(nsaMessage);
        }
	}

    /**
     * {@link process} will correlate this response to the original request.
     * We will set the appropriate path segment to SCHEDULED, and the parent
     * state to SCHEDULED, we can send a provisionFailed to the requesterNSA.
     */
    private void process(NsaMessage provisionFailedMessage) {
        NsaSecurityContext nsaSecurityContext = provisionFailedMessage.getNsaSecurityContext();
        String correlationId = provisionFailedMessage.getCorrelationId();
        String requesterNSA = provisionFailedMessage.getRequesterNSA();
        String providerNSA = provisionFailedMessage.getProviderNSA();
        String connectionId = provisionFailedMessage.getConnectionId();

        logger.info("ProvisionFailedRequesterActor.process: Incoming message [" +
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
            logger.error("ProvisionFailedRequesterActor.process: could not find correlationId " + correlationId + " in PendingOperationManager.");
            return;
        }
        else if (pendingOperation.getOperation() != PendingOperation.OperationType.Provision) {
            logger.error("ProvisionFailedRequesterActor.process: pending operation correlationId " + correlationId + " not a provision operation " + pendingOperation.getOperation().name());
            return;
        }

        // This is a valid operation for our context so remove from queue.
        pendingOperations.remove(correlationId);

        // Now get our original path segment from the pendingOperation.
        PathSegment pathSegment = pendingOperation.getSegment();

        // Validate the connectionId.  We could validate more but...
        if (!connectionId.equals(pathSegment.getChildConnectionId())) {
            logger.error("ProvisionFailedRequesterActor.process: connectionId=" + connectionId + " does no match stored childConnectionId=" + pathSegment.getChildConnectionId());
            return;
        }

        /*
         * Do a sanity check to make sure we are in the correct state for this
         * message.  We will accept a provisionFailed message only when we
         * are in the PROVISIONING state.  If this was a resend of a previous
         * provisionFailed then we should have already bailed out since
         * there is no matching pendingOperation response.
         */
        String parentConnectionId = pathSegment.getParentConnectionId();
        ConnectionStateType currentState = pathSegment.getCurrentState();
        logger.info("ProvisionFailedRequesterActor.process: parentConnectionId=" + parentConnectionId + " the childConnectionId=" + connectionId + " is in " + currentState.name() + "  state");

        // Get the state machine using the parent connectionId.
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);
        if (stateMachine == null) {
            logger.error("ProvisionFailedRequesterActor.process: could not find stateMachine for " + parentConnectionId);
            return;
        }

        /*
         * Set this path segment back to the proper state based on the
         * failure.
         */
        GregorianCalendar startTime = stateMachine.getStartTime();
        GregorianCalendar now = new GregorianCalendar();

        if (startTime.before(now)) {
            logger.info("ProvisionFailedRequesterActor.process: startTime is passed for connectionId=" + parentConnectionId + ", so setting segment state=" + ConnectionStateType.SCHEDULED.name());
            pathSegment.setCurrentState(ConnectionStateType.SCHEDULED);
        }
        else {
            logger.info("ProvisionFailedRequesterActor.process: startTime is in future for connectionId=" + parentConnectionId + ", so setting segment state=" + ConnectionStateType.AUTO_PROVISION.name());
            pathSegment.setCurrentState(ConnectionStateType.RESERVED);
        }

        /*
         * We transition the parent state machine of this segment to a
         * SCHEDULED state due to this segment's provisionFailed.  We leave all
         * children in whatever state they have achieved.
         */
        logger.info("ProvisionFailedRequesterActor.process: transitioning stateMachine from " +
                stateMachine.getCurrentState().name() + " to " +
                ConnectionStateType.SCHEDULED.name());
        stateMachine.setCurrentState(ConnectionStateType.SCHEDULED);

        // Build the provisionFailed message.
        NsaMessage failedMessage = new NsaMessage();
        failedMessage.setCorrelationId(pendingOperation.getParentCorrelationId());
        failedMessage.setConnectionId(parentConnectionId);
        failedMessage.setReplyTo(pendingOperation.getParentReplyTo());
        failedMessage.setGlobalReservationId(stateMachine.getGlobalReservationId());
        failedMessage.setConnectionState(stateMachine.getCurrentState());
        failedMessage.setNsaSecurityContext(stateMachine.getNsaSecurityContext());
        failedMessage.setProviderNSA(stateMachine.getProviderNSA());
        failedMessage.setRequesterNSA(stateMachine.getRequesterNSA());
        failedMessage.setMessageType(NsaMessage.MessageType.provisionFailedProvider);
        failedMessage.setPayload(provisionFailedMessage.getPayload());

        // Route this provisionedConfirmed to the associated actor.
        castingDirector.send(failedMessage);
        logger.info("ProvisionFailedRequesterActor.process: provisionFailed message issued to actor for processing");
    }

}
