/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.actors.reserve;

import java.util.GregorianCalendar;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.opendrac.nsi.util.ExceptionCodes;

/**
 *
 * @author hacksaw
 */
public class ReservationHelper {

    public static ServiceParametersType getServiceParameters(ReservationInfoType resInfo) throws ServiceException {
        if (resInfo.getServiceParameters() == null) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "serviceParameters", "<null>");
        }

        return resInfo.getServiceParameters();
    }

    public static int getDesiredBandwidth(BandwidthType bandwidth) throws ServiceException {
        // Desired has to be present in the message, but could be zero.
        if (bandwidth == null) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "desiredBandwidth", "<null>");
        }
        else if (bandwidth.getDesired() < 0) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "desiredBandwidth", Integer.toString(bandwidth.getDesired()));
        }

        return bandwidth.getDesired();
    }

    public static Integer getMinimumBandwidth(BandwidthType bandwidth) throws ServiceException {
        if (bandwidth == null || bandwidth.getMinimum() == null) {
            return null;
        }
        else if (bandwidth.getMinimum().intValue() < 0) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "minimumBandwidth", bandwidth.getMinimum().toString());
        }

        return bandwidth.getMinimum();
    }

    public static Integer getMaximumBandwidth(BandwidthType bandwidth) throws ServiceException {
        if (bandwidth == null || bandwidth.getMaximum() == null) {
            return null;
        }
        else if (bandwidth.getMaximum().intValue() < 0) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "maximumBandwidth", bandwidth.getMaximum().toString());
        }

        return bandwidth.getMaximum();
    }

    public static GregorianCalendar getStartTime(ScheduleType schedule) throws ServiceException {
        if (schedule == null || schedule.getStartTime() == null) {
            // If startTime is null then we use the current time.
            return new GregorianCalendar();
        }

        return schedule.getStartTime().toGregorianCalendar();
    }

    public static GregorianCalendar getEndTime(GregorianCalendar startTime, ScheduleType schedule) throws ServiceException {
        /*
         * If both endTime and duration are not present then we have an
         * infinite schedule.
         */
        if (schedule == null) {
            // Return null to represent infinite schedule.
            return null;
        }
        XMLGregorianCalendar endTime = schedule.getEndTime();
        Duration duration = schedule.getDuration();

        if (endTime == null) {
            if (duration != null) {
                // We have a duration so use startTime and add as an offset.
                GregorianCalendar result = (GregorianCalendar) startTime.clone();
                duration.addTo(result);
                return result;
            }

            return null;
        }

        return endTime.toGregorianCalendar();
    }

}
