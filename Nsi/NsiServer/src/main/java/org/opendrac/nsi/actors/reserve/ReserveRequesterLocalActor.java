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
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import java.text.DateFormat;
import java.util.Date;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.domain.PendingOperation;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.nrm.NrmReserve;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.topology.Stp;
import org.opendrac.nsi.topology.TopologyFactory;
import org.opendrac.nsi.util.StartTimeScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class ReserveRequesterLocalActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {

        logger.info("ReserveRequesterLocalActor.onReceive: received message.");
        if (message == null) {
            logger.error("ReserveRequesterLocalActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("ReserveRequesterLocalActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ReserveRequesterLocalActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    private void process(NsaMessage reservationMessage) {

        String correlationId = reservationMessage.getCorrelationId();
        String requesterNSA = reservationMessage.getRequesterNSA();
        String providerNSA = reservationMessage.getProviderNSA();
        String connectionId = reservationMessage.getConnectionId();
        String globalReservationId = reservationMessage.getGlobalReservationId();
        String replyTo = reservationMessage.getReplyTo();
        String ipAddress = reservationMessage.getNsaSecurityContext().getRemoteAddr();

        // Get handles to the data managers.
        DataManager dataManager = DataManager.getInstance();
        CastingDirector castingDirector = dataManager.getCastingDirector();

        // Retrieve the pending operation for this message.
        PendingOperationManager pendingOperations = dataManager.getPendingOperationManager();
        PendingOperation pendingOperation = pendingOperations.get(correlationId);
        if (pendingOperation == null) {
            logger.error("ReserveRequesterLocalActor.process: could not find pendingOperation for " + correlationId);
            logger.error("ReserveRequesterLocalActor.process: failing operation.");
            return;
        }
        pendingOperation.setTimeSentNow();
        PathSegment pathSegment = pendingOperation.getSegment();
        String parentConnectionId = pathSegment.getParentConnectionId();

        // Get the state machine using the parent connectionId.
        StateMachineManager stateMachineManager = dataManager.getStateMachineManager();
        StateMachine stateMachine = stateMachineManager.getStateMachine(parentConnectionId);
        if (stateMachine == null) {
            logger.error("ReserveRequesterLocalActor.process: could not find stateMachine for " + parentConnectionId);
            logger.error("ReserveRequesterLocalActor.process: failing operation.");
            return;

        }

        // Update state for this segment to RESERVING.
        if (pathSegment.getCurrentState() == ConnectionStateType.INITIAL) {
            pathSegment.setCurrentState(ConnectionStateType.RESERVING);
        }

		// Log the incoming message information.
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        logger.info(
                "ReserveRequesterLocalActor.process: Sending reservation request to local NRM [" +
                ", parentConnectionId=" + pathSegment.getParentConnectionId() +
                ", childConnectionId=" + pathSegment.getChildConnectionId() +
                ", correlationId=" + correlationId +
                ", sourcePort=" + pathSegment.getSourceStpURN() +
                ", destPort=" + pathSegment.getDestStpURN() +
                ", desiredBandwidth=" + stateMachine.getDesiredBandwidth() +
                ", startTime=" + df.format(new Date(stateMachine.getStartTime().getTimeInMillis())) +
                ", endTime=" + df.format(new Date(stateMachine.getEndTime().getTimeInMillis())) +
                "]");

        /*
         * Lookup STP objects corresponding to provided source and destination
         * STP URN.
         */
        TopologyFactory topology = dataManager.getTopologyFactory();
        Stp sourcePort = topology.getStpByURN(pathSegment.getSourceStpURN());
        Stp destPort = topology.getStpByURN(pathSegment.getDestStpURN());

        String scheduleId = null;
        NrmReserve reserve = new NrmReserve();
        try {
            scheduleId = reserve.reserve(
                    providerNSA,
                    connectionId,
                    globalReservationId,
                    stateMachine.getStartTime(),
                    stateMachine.getEndTime(),
                    stateMachine.getDesiredBandwidth(),
                    sourcePort.getMapsTo(),
                    destPort.getMapsTo(),
                    pathSegment.getDirectionality(),
                    pathSegment.getServiceAttributes(),
                    ipAddress);
            //  stateMachine.getSessionSecurity().getGlobalUserName());
            pathSegment.setData(scheduleId);
        }
        catch (ServiceException nsi) {
            logger.error("ReserveRequesterLocalActor.process: Processing failure occured", nsi);

            /*
             * We need to feed this back to the reserveFailedRequesterActor
             * to process with other responses.
             */
            NsaMessage failedMessage = new NsaMessage();
            failedMessage.setMessageType(NsaMessage.MessageType.reserveFailedRequester);
            failedMessage.setCorrelationId(correlationId);
            failedMessage.setRequesterNSA(requesterNSA);
            failedMessage.setProviderNSA(providerNSA);
            failedMessage.setConnectionId(connectionId);
            failedMessage.setGlobalReservationId(globalReservationId);
            failedMessage.setPayload(nsi.getFaultInfo());

            // Route this message to the appropriate ReservationFailedRequesterActor for processing.
            castingDirector.send(failedMessage);
            logger.info("ReserveRequesterLocalActor.process: reserveFailed message issued to actor so returning");
            return;
        }

        // Now we need to feed a reserveConfirm back into the system.
        logger.info("ReserveRequesterLocalActor.process: sending a reserveConfirm back for scheduleId=" + scheduleId);

        NsaMessage childMessage = new NsaMessage();
        childMessage.setCorrelationId(correlationId);
        childMessage.setConnectionId(connectionId);
        childMessage.setConnectionState(pathSegment.getCurrentState());
        childMessage.setGlobalReservationId(globalReservationId);
        childMessage.setMessageType(NsaMessage.MessageType.reserveConfirmedRequester);
        childMessage.setProviderNSA(providerNSA);
        childMessage.setRequesterNSA(requesterNSA);

        ReservationInfoType confirmed = new ReservationInfoType();
        confirmed.setConnectionId(connectionId);
        confirmed.setGlobalReservationId(globalReservationId);
        confirmed.setDescription(stateMachine.getDescription());

        PathType path = new PathType();
        path.setDirectionality(pathSegment.getDirectionality());

        ServiceTerminationPointType sourceSTP = new ServiceTerminationPointType();
        sourceSTP.setStpId(pathSegment.getSourceStpURN());
        path.setSourceSTP(sourceSTP);

        ServiceTerminationPointType destSTP = new ServiceTerminationPointType();
        destSTP.setStpId(pathSegment.getDestStpURN());
        path.setDestSTP(destSTP);

        confirmed.setPath(path);

        BandwidthType bandwidth = new BandwidthType();
        bandwidth.setDesired(stateMachine.getDesiredBandwidth());

        ScheduleType schedule = new ScheduleType();
        schedule.setStartTime(new XMLGregorianCalendarImpl(stateMachine.getStartTime()));
        schedule.setEndTime(new XMLGregorianCalendarImpl(stateMachine.getEndTime()));

        ServiceParametersType params = new ServiceParametersType();
        params.setSchedule(schedule);
        params.setBandwidth(bandwidth);
        confirmed.setServiceParameters(params);

        childMessage.setPayload(confirmed);

        // Now we need to route this message to the appropriate actor.
        castingDirector.send(childMessage);

        /*
         * Now we need to kick the startTime scheduler to look at this new
         * state machine state containing our local NRM segment.
         */
        StartTimeScheduler.getInstance().timerAudit(stateMachine.getStartTime());

        logger.info("ReserveRequesterLocalActor.process: ReservationConfirmed message issued to actor for processing");
    }

}