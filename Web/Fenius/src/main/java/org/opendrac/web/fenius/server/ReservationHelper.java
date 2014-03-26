/**
 * Copyright (c) 2010, SURFnet bv, The Netherlands
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

package org.opendrac.web.fenius.server;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.datatype.XMLGregorianCalendar;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

//Third-party imports.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// DRAC imports.
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.State;

// GLIF Fenius imports.
import is.glif.fenius.ws.connection.v1.service.InternalFault;
import is.glif.fenius.ws.connection.v1.service.ExternalFault;
import is.glif.fenius.ws.connection.v1.types.ActivationStatus;
import is.glif.fenius.ws.connection.v1.types.Advance;
import is.glif.fenius.ws.connection.v1.types.Avp;
import is.glif.fenius.ws.connection.v1.types.AvpSeq;
import is.glif.fenius.ws.connection.v1.types.Edge;
import is.glif.fenius.ws.connection.v1.types.Reservation;
import is.glif.fenius.ws.connection.v1.types.ReservationStatus;
import is.glif.fenius.ws.connection.v1.types.Topology;
import is.glif.fenius.ws.connection.v1.types.Vertex;

// Local imports.
import org.opendrac.web.fenius.utilities.DateUtils;

/**
 *
 * @author hacksaw
 */
public class ReservationHelper {
  private final Logger logger = LoggerFactory.getLogger(getClass());

    public Reservation mapScheduleToReservation(Schedule sch)
            throws InternalFault, ExternalFault {
        // Base for our error string.
        is.glif.fenius.ws.connection.v1.faults.InternalFault internalFault =
                new is.glif.fenius.ws.connection.v1.faults.InternalFault();
        StringBuilder error = new StringBuilder(
                "OpenDRAC: ReservationHelper.mapScheduleToReservation() ");
        error.append(DateUtils.now());
        error.append(" - ");

        if (sch == null) {
            error.append("null DRAC schedule parameter provided.");
            String errMsg = error.toString();
            internalFault.setMessage(errMsg);
            logger.error(errMsg);
            throw new InternalFault(errMsg, internalFault);
        }

        Reservation res = new Reservation();

        // Map scheduleId to <reservationId>.
        res.setReservationId(sch.getId());

        // We hid the <description> in the schedule name.
        res.getDescription().add(sch.getName());

        // Map the schedule status to <reservationStatus>.
        res.setReservationStatus(mapFromScheduleState(sch.getStatus()));

        // Map the aggregate service status to <activationStatus>.
        res.setActivationStatus(mapServiceStateToActivationStatus(sch.getServiceIdList()));

        // Map the schedule time parameters to a <schedule> element.
        res.setSchedule(mapScheduleToFeniusSchedule(sch));

        // Map the DRAC endpoints to the <topology> element.
        Topology topology = new Topology();
        topology.setDirected(false);

        /*
         * TODO: We really should be mapping the individual service instances
         * through to vertex and edges, unfortunately, the services are
         * broken for layer 2 vlans.
         */
/*        List<ServiceIdType> serviceIdList = sch.getServiceIdArrayList();
        ListIterator serviceList = serviceIdList.listIterator();
        while(serviceList.hasNext()) {
            ServiceIdType service = (ServiceIdType) serviceList.next();

            if (service != null) {
                Vertex a = new Vertex();
                a.setVertexId("vertexA");
                a.setResourceId(service.getSrcTNA());

                Avp aTechParams = new Avp();
                aTechParams.setAttrName("eth.vlanTag");
                aTechParams.setAttrVal(service.getPath().getSrcVlanId());
                logger.debug("SrcVlanId: " + service.getPath().getSrcVlanId());
                AvpSeq aSeq = new AvpSeq();
                aSeq.getAvp().add(aTechParams);
                a.setTechParams(aSeq);

                Vertex z = new Vertex();
                z.setVertexId("vertexZ");
                z.setResourceId(service.getDestTNA());

                Avp zTechParams = new Avp();
                zTechParams.setAttrName("eth.vlanTag");
                zTechParams.setAttrVal(service.getPath().getDstVlanId());
                logger.debug("DstVlanId: " + service.getPath().getDstVlanId());
                AvpSeq zSeq = new AvpSeq();
                zSeq.getAvp().add(zTechParams);
                z.setTechParams(zSeq);

                topology.getVertices().add(a);
                topology.getVertices().add(z);

                Edge e = new Edge();
                e.setA("vertexA");
                e.setZ("vertexZ");
                e.setBandwidth(service.getRate());
                topology.getEdges().add(e);
            }
        }
*/
        // Just use the main schedule entry for now.
        Vertex a = new Vertex();
        a.setVertexId("vertexA");
        a.setResourceId(sch.getSrcTNA());

        Avp aTechParams = new Avp();
        aTechParams.setAttrName("eth.vlanTag");
        aTechParams.setAttrVal(sch.getPath().getSrcVlanId());
        logger.debug("SrcVlanId: " + sch.getPath().getSrcVlanId());
        AvpSeq aSeq = new AvpSeq();
        aSeq.getAvp().add(aTechParams);
        a.setTechParams(aSeq);

        Vertex z = new Vertex();
        z.setVertexId("vertexZ");
        z.setResourceId(sch.getDestTNA());

        Avp zTechParams = new Avp();
        zTechParams.setAttrName("eth.vlanTag");
        zTechParams.setAttrVal(sch.getPath().getDstVlanId());
        logger.debug("DstVlanId: " + sch.getPath().getDstVlanId());
        AvpSeq zSeq = new AvpSeq();
        zSeq.getAvp().add(zTechParams);
        z.setTechParams(zSeq);

        topology.getVertices().add(a);
        topology.getVertices().add(z);

        Edge e = new Edge();
        e.setA("vertexA");
        e.setZ("vertexZ");
        e.setBandwidth(sch.getRate());
        topology.getEdges().add(e);

        res.setTopology(topology);

        return res;
    }

        // This can be replaced by a new method in DRAC's State class.
    private static ReservationStatus mapFromScheduleState(State.SCHEDULE status)
    {
        ReservationStatus result = ReservationStatus.RESERVED;

        switch (status) {
            case EXECUTION_PENDING:
                result = ReservationStatus.RESERVED;
                break;

            case EXECUTION_INPROGRESS:
                result = ReservationStatus.RESERVED;
                break;

            case EXECUTION_SUCCEEDED:
                result = ReservationStatus.FINISHED;
                break;

            case EXECUTION_PARTIALLY_SUCCEEDED:
            case EXECUTION_TIME_OUT:
            case EXECUTION_FAILED:
                result = ReservationStatus.FINISHED;
                break;

            case EXECUTION_PARTIALLY_CANCELLED:
            case EXECUTION_CANCELLED:
                result = ReservationStatus.RELEASED;
                break;

            case CONFIRMATION_PENDING:
                result = ReservationStatus.RESERVING;
                break;

            case CONFIRMATION_TIMED_OUT:
                result = ReservationStatus.RELEASED;
                break;

            case CONFIRMATION_CANCELLED:
                result = ReservationStatus.RELEASED;
                break;

            default:
                result = ReservationStatus.RESERVED;
                break;
        }

        return result;
    }

    // This can be replaced by a new method in DRAC's State class.
    private static ActivationStatus mapServiceStateToActivationStatus(DracService[] list)
    {
        Set<State.SERVICE> serviceSet = new HashSet<State.SERVICE>();
        for (int i = 0; i < list.length; i++) {
            serviceSet.add(list[i].getStatus());
        }

        // if one Call is pending, the Service is pending as well;
        if (serviceSet.contains(State.SERVICE.EXECUTION_PENDING))
        {
            return ActivationStatus.INACTIVE;
        }
        else if (serviceSet.contains(State.SERVICE.EXECUTION_INPROGRESS))
        {
            return ActivationStatus.ACTIVE;
        }
        else if (serviceSet.contains(State.SERVICE.EXECUTION_PARTIALLY_CANCELLED))
        {
            return ActivationStatus.INACTIVE;
        }
        else if (serviceSet.contains(State.SERVICE.CREATE_FAILED))
        {
            return ActivationStatus.FAILED;
        }
        else if (serviceSet.contains(State.SERVICE.DELETE_FAILED))
        {
            return ActivationStatus.INACTIVE;
        }
        else if (serviceSet.contains(State.SERVICE.EXECUTION_CANCELLED))
        {
            return ActivationStatus.INACTIVE;
        }
        else if (serviceSet.contains(State.SERVICE.EXECUTION_TIMED_OUT))
        {
            return ActivationStatus.FAILED;
        }
        else if (serviceSet.contains(State.SERVICE.EXECUTION_SUCCEEDED))
        {
            return ActivationStatus.INACTIVE;
        }
        else if (serviceSet.contains(State.SERVICE.CONFIRMATION_PENDING))
        {
            return ActivationStatus.INACTIVE;
        }
        else if (serviceSet.contains(State.SERVICE.CONFIRMATION_TIMED_OUT))
        {
            return ActivationStatus.INACTIVE;
        }
        else if (serviceSet.contains(State.SERVICE.CONFIRMATION_CANCELLED))
        {
            return ActivationStatus.INACTIVE;
        }
        else if (serviceSet.contains(State.SERVICE.ACTIVATION_PENDING))
        {
            return ActivationStatus.ACTIVATING;
        }
        else if (serviceSet.contains(State.SERVICE.ACTIVATION_TIMED_OUT))
        {
            return ActivationStatus.FAILED;
        }
        else if (serviceSet.contains(State.SERVICE.ACTIVATION_CANCELLED))
        {
            return ActivationStatus.INACTIVE;
        }

        return ActivationStatus.INACTIVE;
    }


    public is.glif.fenius.ws.connection.v1.types.Schedule mapScheduleToFeniusSchedule(Schedule schType) {

        // Map the <advance> element.
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        gc.setTimeInMillis(schType.getStartTime());
        XMLGregorianCalendar xmlStartTime = new XMLGregorianCalendarImpl(gc);

        Advance advance = new Advance();
        advance.setStart(xmlStartTime);

        // TODO: Fix this to map DRAC infinite schedules to a duration -1.
        if (schType.getEndTime() > 0) {
            gc.setTimeInMillis(schType.getEndTime());
            XMLGregorianCalendar xmlEndTime = new XMLGregorianCalendarImpl(gc);
            advance.setEnd(xmlEndTime);
        }
        else {
            Long duration = schType.getDurationLong();
            advance.setDuration(duration.intValue()/1000);
        }

        is.glif.fenius.ws.connection.v1.types.Schedule schedule =
				new is.glif.fenius.ws.connection.v1.types.Schedule();
        schedule.setAdvance(advance);
        schedule.setCreated(xmlStartTime);

        return schedule;
    }
}
