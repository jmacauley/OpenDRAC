/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import java.util.GregorianCalendar;
import javax.xml.datatype.XMLGregorianCalendar;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._10.connection.types.DirectionalityType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
import org.ogf.schemas.nsi._2011._10.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;

/**
 *
 * @author hacksaw
 */
public class Reservation extends NsiOperation {

    private String description = null;
    private String sourceStpId = null;
    private String destStpId = null;
    private int    serviceBandwidth = 0;
    private String serviceProtection = null;
    private GregorianCalendar startTime = null;
    private GregorianCalendar endTime = null;

    public ReserveRequestType getReservationRequestType() {

        ReserveRequestType reservation = new ReserveRequestType();
        reservation.setCorrelationId(getCorrelationId());
        reservation.setReplyTo(getReplyTo());

        ReserveType reservationRequest = new ReserveType();
        reservationRequest.setProviderNSA(getProviderNSA());
        reservationRequest.setRequesterNSA(getRequesterNSA());

        reservationRequest.setSessionSecurityAttr(SessionSecurityAttr.getAttributeStatementType(getGlobalUserName(), getUserRole()));

        ReservationInfoType info = new ReservationInfoType();
        info.setConnectionId(getCorrelationId());
        info.setDescription(getDescription());
        info.setGlobalReservationId(getGlobalReservationId());

        PathType path = new PathType();
        ServiceTerminationPointType source = new ServiceTerminationPointType();
        source.setStpId(getSourceStpId());
        path.setSourceSTP(source);
        ServiceTerminationPointType dest = new ServiceTerminationPointType();
        dest.setStpId(getDestStpId());
        path.setDestSTP(dest);
        path.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        info.setPath(path);

        ServiceParametersType params = new ServiceParametersType();
        BandwidthType bandwidth = new BandwidthType();
        bandwidth.setDesired(getServiceBandwidth());
        params.setBandwidth(bandwidth);

        String protection = getServiceProtection();
        if (protection != null && !protection.isEmpty()) {
            params.setServiceAttributes(TechnologySpecificAttr.getTechnologySpecificAttr("sNCP", protection));
        }
        
        ScheduleType sch = new ScheduleType();
        XMLGregorianCalendar start = new XMLGregorianCalendarImpl(getStartTime());
        sch.setStartTime(start);

        XMLGregorianCalendar end = new XMLGregorianCalendarImpl(getEndTime());
        sch.setEndTime(end);
        params.setSchedule(sch);

        info.setServiceParameters(params);
        reservationRequest.setReservation(info);
        reservation.setReserve(reservationRequest);

        return reservation;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the sourceStpId
     */
    public String getSourceStpId() {
        return sourceStpId;
    }

    /**
     * @param sourceStpId the sourceStpId to set
     */
    public void setSourceStpId(String sourceStpId) {
        this.sourceStpId = sourceStpId;
    }

    /**
     * @return the destStpId
     */
    public String getDestStpId() {
        return destStpId;
    }

    /**
     * @param destStpId the destStpId to set
     */
    public void setDestStpId(String destStpId) {
        this.destStpId = destStpId;
    }

    /**
     * @return the serviceBandwidth
     */
    public int getServiceBandwidth() {
        return serviceBandwidth;
    }

    /**
     * @param serviceBandwidth the serviceBandwidth to set
     */
    public void setServiceBandwidth(int serviceBandwidth) {
        this.serviceBandwidth = serviceBandwidth;
    }

        /**
     * @return the serviceProtection
     */
    public String getServiceProtection() {
        return serviceProtection;
    }

    /**
     * @param serviceProtection the serviceProtection to set
     */
    public void setServiceProtection(String serviceProtection) {
        this.serviceProtection = serviceProtection;
    }

    /**
     * @return the startTime
     */
    public GregorianCalendar getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(GregorianCalendar startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public GregorianCalendar getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(GregorianCalendar endTime) {
        this.endTime = endTime;
    }

}
