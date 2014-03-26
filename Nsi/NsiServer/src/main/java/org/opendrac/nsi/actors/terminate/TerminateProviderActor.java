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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;


import java.util.List;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.actors.messages.TerminateMessage;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.util.MessageDump;
import org.opendrac.nsi.security.NsaSecurityContext;
import org.opendrac.nsi.security.SessionSecurity;
import org.opendrac.nsi.topology.TopologyFactory;

/**
 *
 * @author hacksaw
 */
public class TerminateProviderActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("TerminateProviderActor.onReceive: received message.");
        if (message == null) {
            logger.error("TerminateProviderActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("TerminateProviderActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("TerminateProviderActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    /**
     * Process the NSI terminate message and dispatch to individual NSA/NRM
     * involved in the path segments.
     *
     * @param terminateMessage Our generic message holding the NSI reservation message.
     */
    private void process(NsaMessage terminateMessage) {

        // We have already verified this field is present and not null.
        NsaSecurityContext nsaSecurityContext = terminateMessage.getNsaSecurityContext();
        GenericRequestType terminate = (GenericRequestType) terminateMessage.getPayload();
        String correlationId = terminateMessage.getCorrelationId();
        String replyTo = terminateMessage.getReplyTo();
        String requesterNSA = terminateMessage.getRequesterNSA();
        String providerNSA = terminateMessage.getProviderNSA();
        String connectionId = terminateMessage.getConnectionId();

		// Log the incoming message information.
        logger.info("TerminateProviderActor.process: Incoming message [" +
                nsaSecurityContext.getUserName() + " @ " +
                nsaSecurityContext.getRemoteAddr() +
                ", requesterNSA=" + requesterNSA +
                ", correlationId=" + correlationId + "]");
        logger.info(MessageDump.dump(GenericRequestType.class, terminate));

        /*
         * TODO: In the next iteration we can apply any needed access control
         * based on the NSA request the resource.  In general, we will only
         * want to give them access to the endpoints within their associated
         * groups.  Whether we trust an NSA 100% and only enforce user level
         * access control will need to be determined.
         */

        // Get a reference to the related data managers.
		DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        CastingDirector castingDirector = dataManager.getCastingDirector();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();
        TopologyFactory topology = dataManager.getTopologyFactory();

        /*
         * If we find any error messages from this point forward we need to
         * send a terminateFailed message back to the requesterNSA.
         */
        StateMachine machine = null;
        try {
            /*
             * Verify we have an existing reservation with the same
             * connectionId.
             */
            machine = stateMachineManager.failOnNoStateMachine(connectionId);

            /*
             * We are always in the correct state to get a terminate request.
             */

            // Parse the session security and assign to state machine.
            SessionSecurity sessionSecurity = new SessionSecurity();
            sessionSecurity.parseSessionSecurityAttr(terminate.getSessionSecurityAttr());

            /*
             * TODO: compare security to determine if sending NSA/User can
             * manipulate this state machine.  We will use the user's NSI role
             * to do this in the future.
             */
        }
        catch (ServiceException nsi) {
            /*
             * Send this to a web services actor for delivery of a
             * ReservationFailed message.
             */
            logger.info("TerminateProviderActor.process: Sending terminateFailed message " + nsi.toString());

            // Leave the state where it is for now.

            NsaMessage failedMessage = new NsaMessage();
            failedMessage.setCorrelationId(correlationId);
            failedMessage.setReplyTo(replyTo);
            failedMessage.setRequesterNSA(requesterNSA);
            failedMessage.setProviderNSA(providerNSA);
            failedMessage.setConnectionId(connectionId);
            if (machine != null) {
                failedMessage.setConnectionState(machine.getCurrentState());
                failedMessage.setGlobalReservationId(machine.getGlobalReservationId());
            }

            failedMessage.setMessageType(NsaMessage.MessageType.terminateFailedProvider);
            failedMessage.setPayload(nsi.getFaultInfo());

            // Route this message to the appropriate actor for processing.
            castingDirector.send(failedMessage);
            logger.info("TerminateProviderActor.process: terminateFailed message issued to actor so returning");
            return;
        }

        /*
         * If we were already in the TERMINATING or TERMINATED state then we
         * can send back a terminateConfirmed message immediately and leave the
         * state machine in the current state.
         *
         * TODO: should we consider sending this terminate request down the
         * tree if in the TERMINATING state already?  Will this help with error
         * recovery?
         */
        if (machine.getCurrentState() == ConnectionStateType.TERMINATING ||
                machine.getCurrentState() == ConnectionStateType.TERMINATED ||
                machine.getCurrentState() == ConnectionStateType.CLEANING) {
            NsaMessage childMessage = new NsaMessage();
            childMessage.setCorrelationId(correlationId);
            childMessage.setConnectionId(connectionId);
            childMessage.setConnectionState(machine.getCurrentState());
            childMessage.setGlobalReservationId(machine.getGlobalReservationId());
            childMessage.setMessageType(NsaMessage.MessageType.terminateConfirmedProvider);
            childMessage.setProviderNSA(providerNSA);
            childMessage.setRequesterNSA(requesterNSA);
            childMessage.setReplyTo(replyTo);

            castingDirector.send(childMessage);
            logger.info("TerminateProviderActor.process: terminateConfirmed message issued to actor for processing");
        }

        /*
         * Transition state machine to Terminating state before issuing
         * individual termnate messages to NSA.
         */
        machine.setCurrentState(ConnectionStateType.TERMINATING);

        /*
         * For all path segments in the list that are managed by the local
         * NSA we will send a terminateRequest to local NSA actors, while path
         * segments on remote NSA will be sent to remote NSA actors via the
         * NSI protocol.
         */
        List<PathSegment> pathList = machine.getRoutePathList();

        for (PathSegment segment : pathList) {
            logger.info("TerminateProviderActor.process: Routing terminate requests to NSA = " +
                    segment.getManagingNsaURN() +
                    ", NsaType = " + segment.getNsaType().name() +
                    ", childConnectionId = " + segment.getChildConnectionId() +
                    ", sourceSTP = " + segment.getSourceStpURN() +
                    ", destSTP = " + segment.getDestStpURN());

            TerminateMessage termMessage = new TerminateMessage();

            NsaMessage childMessage = termMessage.getRequestMessage(
                    correlationId, machine.getGlobalReservationId(), replyTo,
                    providerNSA, segment);

            // Give this to the casting director to find us an actor.
            castingDirector.send(childMessage);
            logger.info("TerminateProviderActor.process: terminateRequest message issued to actor for processing");
        }

        // We are done - all other processing is handled by other actors.
        logger.info("TerminateProviderActor.process: terminateRequests issued to children NSA so returning with state=" + machine.getCurrentState());
    }
}