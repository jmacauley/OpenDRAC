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

import java.util.Date;

import javax.xml.ws.Holder;

import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
import org.ogf.schemas.nsi._2011._10.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.opendrac.nsi.topology.TopologyFactory;

/**
 * This {@link ReserveRequesterRemoteActor} class models the requester side
 * of the reservation request message. This class will sends a reservation
 * request to a child NSA via the NSI protocol as specified in the incoming
 * reservation message.  JAX-WS bindings are used to send the SOAP message.
 *
 * @author hacksaw
 */
public class ReserveRequesterRemoteActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@link onReceive} receives messages sent to this UntypedActor.  We
     * validate that an NsaMessage was received then delegate for processing.
     *
     * @param message Message to process.
     */
    @Override
	public void onReceive(Object message) {
        logger.info("ReserveRequesterRemoteActor.onReceive: received message.");
        if (message == null) {
            logger.error("ReserveRequesterRemoteActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("ReserveRequesterRemoteActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ReserveRequesterRemoteActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    /**
     * Process the outgoing NSI reservation request message and dispatch to
     * identified NSA.  We will have been called from the
     * ReservationProviderActor as it sequences through the remote NSAs
     * requiring communication via the NSI protocol.
     *
     * The reservationRequest message contains the newly generated unique
     * reservation parameters for this path segment while we will look up the
     * common reservation parameters in the parent state machine.
     *
     * @param reservationMessage Our generic message holding the NSI
     * reservation message.
     */
    private void process(NsaMessage reservationRequest) {
        String correlationId = reservationRequest.getCorrelationId();
        String requesterNSA = reservationRequest.getRequesterNSA();
        String providerNSA = reservationRequest.getProviderNSA();
        String connectionId = reservationRequest.getConnectionId();
        String globalReservationId = reservationRequest.getGlobalReservationId();

		// Log the incoming message information.
        logger.info("ReserveRequesterRemoteActor.process: Outgoing message [" +
                "requesterNSA=" + requesterNSA +
                "providerNSA=" + providerNSA +
                ", correlationId=" + correlationId +
                ", replyTo=" + reservationRequest.getReplyTo() + "]");

        // Get a reference to the realted data managers.
        DataManager dataManager = DataManager.getInstance();
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        NsaConfigurationManager nsaConfiguration = dataManager.getNsaConfigurationManager();
        TopologyFactory topologyManager = dataManager.getTopologyFactory();
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();

        /*
         * Get the pending operation manager so we can update the retry
         * information for this reservation request.
         */
        PendingOperation pendingOperation = pendingOperations.get(correlationId);
        if (pendingOperation == null) {
            logger.error("ReserveRequesterRemoteActor.process: could not find pendingOperation for " + correlationId);
            logger.error("ReserveRequesterRemoteActor.process: failing operation.");
            return;
        }
        else if (pendingOperation.getOperation() != PendingOperation.OperationType.Reserve) {
            logger.error("ReserveRequesterRemoteActor.process: pending operation correlationId " + correlationId + " not a reserve operation " + pendingOperation.getOperation().name());
            return;
        }

        pendingOperation.setTimeSentNow();
        PathSegment pathSegment = pendingOperation.getSegment();

        // Get the parent state machine for schedule information.
        StateMachine machine =  stateMachineManager.getStateMachine(pathSegment.getParentConnectionId());

        // Get the authentication information for this remote providerNSA.
        AuthenticationInfoType auth = nsaConfiguration.getNsaAuthenticationInfo(providerNSA);

        if (auth == null) {
            // TODO: Add error handling here.
            logger.error("ReserveRequesterRemoteActor: could not find authentication information for NsNetwork=" + providerNSA);
            return;
        }

        // Lookup provider endpoint information in the global topology.
        String providerEndpoint = topologyManager.getCsProviderEnpointByNsaURN(providerNSA);

        if (providerEndpoint == null || providerEndpoint.isEmpty()) {
            // TODO: Add error handling here.
            logger.error("ReserveRequesterRemoteActor: could not find endpoint information for NsNetwork=" + providerNSA);
            return;
        }

        // Allocate and set up an NSI proxy.
        RequesterClientProxy proxy = new RequesterClientProxy();

        // Configure the request with the remote endpoint information.
        proxy.setProviderEndpoint(providerEndpoint, auth.getUserId(), auth.getPassword());

        // Build the NSI reservation message.
        ReservationInfoType resInfo = new ReservationInfoType();

        // Unique connectionId for this child reservation.
        resInfo.setConnectionId(connectionId);

        // Reservation description from the parent state machine.
        resInfo.setDescription(machine.getDescription());

        // Global reservationId from the parent state machine.
        resInfo.setGlobalReservationId(globalReservationId);

        // Build the path segment.
        ServiceTerminationPointType sourceSTP = new ServiceTerminationPointType();
        sourceSTP.setStpId(pathSegment.getSourceStpURN());

        ServiceTerminationPointType destSTP = new ServiceTerminationPointType();
        destSTP.setStpId(pathSegment.getDestStpURN());

        PathType path = new PathType();
        path.setSourceSTP(sourceSTP);
        path.setDestSTP(destSTP);
        path.setDirectionality(pathSegment.getDirectionality());

        resInfo.setPath(path);

        /*
         * Now for the service parameters.  We send the resolved parameters
         * from our parent state machine and not the original one this NSA
         * received on the reservation request.
         */
        ServiceParametersType service = new ServiceParametersType();

        /*
         * We only support desired bandwidth for now so this is what we
         * propagate on to the child.
         */
        BandwidthType bandwidth = new BandwidthType();
        bandwidth.setDesired(machine.getDesiredBandwidth());
        service.setBandwidth(bandwidth);

        ScheduleType schedule = new ScheduleType();
        schedule.setStartTime(new XMLGregorianCalendarImpl(machine.getStartTime()));
        if (machine.getEndTime() != null) {
            schedule.setEndTime(new XMLGregorianCalendarImpl(machine.getEndTime()));
        }

        service.setSchedule(schedule);

        /*
         * TODO: we need to process the service attributes but just prop them
         * on for now.
         */
        service.setServiceAttributes(machine.getServiceParameters().getServiceAttributes());
        resInfo.setServiceParameters(service);

        ReserveType res = new ReserveType();
        res.setProviderNSA(reservationRequest.getProviderNSA());
        res.setRequesterNSA(reservationRequest.getRequesterNSA());
        res.setSessionSecurityAttr(machine.getSessionSecurity().getStatement());
        res.setReservation(resInfo);

        /*
         * Now we build the Reservation "transport" message and send to
         * providerNSA.
         */
        ReserveRequestType reservation = new ReserveRequestType();
        reservation.setCorrelationId(reservationRequest.getCorrelationId());
        reservation.setReplyTo(reservationRequest.getReplyTo());
        reservation.setReserve(res);

        Holder<String> holder = new Holder<String>(reservationRequest.getCorrelationId());

        try {
            logger.info("ReserveRequesterRemoteActor.process: Issuing reservation to NSA" + reservationRequest.getProviderNSA());
            proxy.getProxy().reserve(reservation);
            logger.info("ReserveRequesterRemoteActor.process: completed successfully returning correlationId=" + holder.value);
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException ex) {
            // TODO: Need error handling and retry for communication errors.
            logger.info("ReserveRequesterRemoteActor.process: received exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            return;
        }

        // Check to make sure we got back the correct correlationId.
        if (holder.value.equalsIgnoreCase(reservationRequest.getCorrelationId())) {
            logger.info("ReserveRequesterRemoteActor.process: successfully sent providerNSA=" +
                    reservationRequest.getProviderNSA() +
                    ", correlationId=" + reservationRequest.getCorrelationId());

            // Update this path segments connection state.
            pathSegment.setCurrentState(ConnectionStateType.RESERVING);
            pendingOperation.setTimeSent(new Date());
        }
        else {
            // No clue what happened - must have been a provider NSA error.
            logger.error("ReserveRequesterRemoteActor.process: failed ack for " +
                    "providerNSA=" + reservationRequest.getProviderNSA() +
                    ", correlationId=" + reservationRequest.getCorrelationId() +
                    ", received correlationId=" + holder.value);

            // Update this path segments connection state.
            pathSegment.setCurrentState(ConnectionStateType.UNKNOWN);

            /*
             * TODO: This should be an error that causes us to back out the
             * entire reservation and return a reservation failed message.
             */
            return;
        }

        // We are done.
        logger.info("ReserveRequesterRemoteActor.process: Completed.");
    }
}