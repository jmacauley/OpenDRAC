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

import java.util.GregorianCalendar;
import java.util.List;

import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperation;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.pathfinding.PathFinder;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.security.NsaSecurityContext;
import org.opendrac.nsi.security.SessionSecurity;
import org.opendrac.nsi.util.MessageDump;
import org.opendrac.nsi.util.UUIDUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

/**
 * This {@link ReserveProviderActor} class models the provider side of the
 * reservation request message.  The JAX-WS provider endpoint parses the NSI
 * protocol reservation message and pass the contained reservation information
 * to this class for processing.  This class will validate applicable
 * parameters, perform path computation, then route any resulting path segments
 * to appropriate actors for processing.
 *
 * @author hacksaw
 */
public class ReserveProviderActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@link onReceive} receives messages sent to this UntypedActor.  We
     * validate that an NsaMessage was received then delegate for processing.
     *
     * @param message Message to process.
     */
    @Override
	public void onReceive(Object message) {
        logger.info("ReserveProviderActor.onReceive: received message.");
        if (message == null) {
            logger.error("ReserveProviderActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("ReserveProviderActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ReserveProviderActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    /**
     * Process the NSI reservation message and dispatch to individual NSA/NRM
     * involved in the path segments.
     *
     * @param reserveMessage Our generic message holding the NSI reservation message.
     */
    private void process(NsaMessage reserveMessage) {

        // We have already verified this field is present and not null.
        NsaSecurityContext nsaSecurityContext = reserveMessage.getNsaSecurityContext();
        ReserveType reservation = (ReserveType) reserveMessage.getPayload();
        String correlationId = reserveMessage.getCorrelationId();
        String replyTo = reserveMessage.getReplyTo();
        String requesterNSA = reserveMessage.getRequesterNSA();
        String providerNSA = reserveMessage.getProviderNSA();
        String connectionId = reserveMessage.getConnectionId();
        List<PathSegment> pathList = null;

		// Log the incoming message information.
        logger.info("ReserveProviderActor.process: Incoming message [" +
                nsaSecurityContext.getUserName() + " @ " +
                nsaSecurityContext.getRemoteAddr() +
                ", requesterNSA=" + requesterNSA +
                ", correlationId=" + correlationId + "]");
        logger.info(MessageDump.dump(ReserveType.class, reservation));

        /*
         * TODO: In the next iteration we can apply any needed access control
         * based on the NSA request the resource.  In general, we will only
         * want to give them access to the endpoints within their associated
         * groups.  Whether we trust an NSA 100% and only enforce user level
         * access control will need to be determined.
         */

        // Get a reference to the realted data managers.
        DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        CastingDirector castingDirector = dataManager.getCastingDirector();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();

        // Populate the new state machine.
        StateMachine machine = new StateMachine();

        /*
         * If we find any error messages from this point forward we need to
         * send a reservationFailed message back to the requesterNSA.
         */
        try {
            /*
             * Verify we do not have an existing reservation with the same
             * connectionId from the same RequesterNSA.
             */
            stateMachineManager.failOnStateMachine(connectionId);

            logger.info("ReserveProviderActor.process: Creating new state machine for connecionId=" + connectionId);

            machine.setRequesterNSA(requesterNSA);
            machine.setProviderNSA(providerNSA);
            machine.setReplyTo(replyTo);
            machine.setNsaSecurityContext(nsaSecurityContext);
            machine.setConnectionId(connectionId);

            // Parse the session security and assign to state machine.
            SessionSecurity sessionSecurity = new SessionSecurity();

            // Assign the reservation information to the state machine if available.
            AttributeStatementType sessionSecurityAttr = reservation.getSessionSecurityAttr();

            if (sessionSecurityAttr == null) {
                sessionSecurity.setDefaultSessionSecurity();
            }
            else {
                sessionSecurity.parseSessionSecurityAttr(sessionSecurityAttr);
            }

            machine.setSessionSecurity(sessionSecurity);

            // Save the original resevations specifics.
            ReservationInfoType resType = reservation.getReservation();
            machine.setGlobalReservationId(resType.getGlobalReservationId());
            machine.setDescription(resType.getDescription());
            machine.setServiceParameters(resType.getServiceParameters());
            machine.setPath(resType.getPath());

            // Get the reservation details.
            ServiceParametersType attributes = ReservationHelper.getServiceParameters(resType);

            /*
             * We are not modeling available bandwidth in topology so this will
             * need to be determined by the local NRM after path finding.
             */
            machine.setDesiredBandwidth(ReservationHelper.getDesiredBandwidth(attributes.getBandwidth()));
            machine.setMinimumBandwidth(ReservationHelper.getMinimumBandwidth(attributes.getBandwidth()));
            machine.setMaximumBandwidth(ReservationHelper.getMaximumBandwidth(attributes.getBandwidth()));

            /*
             * Now we process the startTime, endTime and duration parameters.  Once
             * again, bandwidth availability for a specific period of time is not
             * yet being modeled in topology so let us leave it up to the local
             * NRM to determine.
             * TODO: Add duration support.
             */
            GregorianCalendar startTime = ReservationHelper.getStartTime(attributes.getSchedule());
            machine.setStartTime(startTime);
            machine.setEndTime(ReservationHelper.getEndTime(startTime, attributes.getSchedule()));

            /*
             * Looks like it is a new reservation so we need to create a
             * state machine and begin the reservation process.
             */
            stateMachineManager.putStateMachine(connectionId, machine);

            /*
             * Path computation comes next so we can determine how many request
             * we will send down the tree.
             *
             * Path finding will filter reservation parameters into internally
             * supported values to make sure we can support the request.  We are
             * still not at local NRM specifics, just from a NSA perspective.
             */
            PathFinder router = new PathFinder();
            pathList = router.computePath(resType.getPath(), resType.getServiceParameters());
            machine.setRoutePathList(pathList);

            /*
             * If we are this far then we must have a feasible path and a set
             * of NSAs to which we can propagate the request.
             */
        }
        catch (ServiceException nsi) {
            /*
             * Send this to a web services actor for delivery of a reserveFailed
             * message.
             */
            logger.info("ReserveProviderActor.process: Sending reserveFailed message " + nsi.toString());

            /*
             * Update the state to terminated since we did not yet send any
             * messages to children.
             */
            machine.setCurrentState(ConnectionStateType.TERMINATED);

            NsaMessage failedMessage = new NsaMessage();
            failedMessage.setCorrelationId(correlationId);
            failedMessage.setReplyTo(replyTo);
            failedMessage.setRequesterNSA(requesterNSA);
            failedMessage.setProviderNSA(providerNSA);
            failedMessage.setConnectionId(connectionId);
            failedMessage.setConnectionState(machine.getCurrentState());
            failedMessage.setGlobalReservationId(machine.getGlobalReservationId());
            failedMessage.setMessageType(NsaMessage.MessageType.reserveFailedProvider);
            failedMessage.setPayload(nsi.getFaultInfo());

            // Route this message to the appropriate ReservationFailedProviderActor for processing.
            castingDirector.send(failedMessage);
            logger.info("ReserveProviderActor.process: reserveFailed message issued to actor so returning");
            return;
        }

        /*
         * Transition state machine to RESERVING state for this reservation then
         * send all the child messages.  Setting it before we send the
         * reservations means any child failures can change the state to failed
         * and we won't overwrite it later.
         */
        machine.setCurrentState(ConnectionStateType.RESERVING);

        /*
         * For all path segments in the list that are managed by the local
         * NSA we will send these to local NSA actors, while path segments
         * on remote NSA will be sent to remote NSA actors.
         */
        logger.info("ReserveProviderActor.process: Path computation completed.  Routing reservation requests to the following NSA...");
        for (PathSegment segment : pathList) {
            logger.info("NSA = " + segment.getManagingNsaURN() +
                    ", NsaType = " + segment.getNsaType().name() +
                    ", sourceSTP = " + segment.getSourceStpURN() +
                    ", destSTP = " + segment.getDestStpURN());

            // Create request message to child NSA or NRM.
            NsaMessage childMessage = new NsaMessage();
            String childConnectionId = UUIDUtilities.getUrnUuid();
            String childCorrelationId = UUIDUtilities.getUrnUuid();

            /*
             * Prepare the path segment to track reservation mapping
             * information.
             */
            segment.setParentConnectionId(connectionId);
            segment.setChildConnectionId(childConnectionId);

            // Allocate a new correlationId.
            childMessage.setCorrelationId(childCorrelationId);

            // Set replyTo to the ConnectionServicesRequester endpoint.
            childMessage.setReplyTo(nsaConfiguration.getNsaRequesterEndpoint(providerNSA));

            // The reqesterNSA is this NSA so use the request context.
            childMessage.setRequesterNSA(providerNSA);

            // ProviderNSA is the one targeted in the PathSeqment.
            childMessage.setProviderNSA(segment.getManagingNsaURN());
            childMessage.setConnectionId(childConnectionId);
            childMessage.setGlobalReservationId(machine.getGlobalReservationId());
            childMessage.setNsaSecurityContext(nsaSecurityContext);

            // Now store this new operation in the PendingOperationManager.
            PendingOperation pendingOperation = new PendingOperation();
            pendingOperation.setOperation(PendingOperation.OperationType.Reserve);
            pendingOperation.setCorrelationId(childCorrelationId);
            pendingOperation.setParentCorrelationId(correlationId);
            pendingOperation.setParentReplyTo(replyTo);
            pendingOperation.setSegment(segment);
            pendingOperations.add(pendingOperation);

            /*
             * Remote requests go to the ReservationRequesterRemoteActor for
             * sending to remote NSA via the NSI protocol.
             */
            if (segment.getNsaType() == PathSegment.NsaType.REMOTE) {
                logger.info("ReserveProviderActor.process: routing to " +
                      NsaMessage.MessageType.reserveRequesterRemote.name());
                childMessage.setMessageType(NsaMessage.MessageType.reserveRequesterRemote);
            }
            /*
             * Local requests go to the ReservationRequesterLocalActor for
             * sending to co-located NRM via internal communications.
             */
            else {
                logger.info("ReserveProviderActor.process: routing to " +
                      NsaMessage.MessageType.reserveRequesterLocal.name());
                childMessage.setMessageType(NsaMessage.MessageType.reserveRequesterLocal);
            }

            // The casting director will get the message to where it needs to go.
            castingDirector.send(childMessage);
            logger.info("ReserveProviderActor.process: reserve message issued to actor for processing");
        }

        // We are done - all other processing is handled by other actors.
        logger.info("ReserveProviderActor.process: reserveRequest issued to children NSA so returning");
    }

}