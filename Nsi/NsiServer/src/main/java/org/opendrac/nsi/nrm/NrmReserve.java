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

package org.opendrac.nsi.nrm;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.ogf.schemas.nsi._2011._10.connection.types.DirectionalityType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.TechnologySpecificAttributesType;
import org.opendrac.nsi.config.xml.NrmConfigurationType;
import org.opendrac.nsi.config.xml.ProtectionType;
import org.opendrac.nsi.util.AttributeStatementUtilities;
import org.opendrac.nsi.util.ExceptionCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class NrmReserve {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String reserve(
                String nsaIdentifier,
                String connectionId,
                String globalReservationId,
                GregorianCalendar startTime,
                GregorianCalendar endTime,
                int desiredBandwidth,
                String sourcePort,
                String destPort,
                DirectionalityType directionality,
                TechnologySpecificAttributesType serviceAttributes,
                String globalUserId
            ) throws ServiceException {

        /*
         * Get an active login token.  This will throw a ServiceException if
         * there is an issue.
         */
        NrmManager nrmManager = NrmManager.getInstance();
        NrmConfigurationType nrmConfiguration = nrmManager.getNrmConfiguration(nsaIdentifier);
        NrmLoginManager nrmLoginManager = nrmManager.getNrmLoginManager(nsaIdentifier);
        LoginToken token = nrmLoginManager.getToken();

        // All NSI schedules are manual reservations.
        Schedule schedule = new Schedule();
		schedule.setActivationType(Schedule.ACTIVATION_TYPE.RESERVATION_MANUAL);

		// Map the NRM credentials through to a OpenDRAC UserInfo.
        schedule.setUserInfo(mapCredentials(nrmLoginManager));

        /*
         * Populate OpenDRAC schedule name.  We will use the provided
         * globalUserId and the reservation connectionId to name the schedule
         * in OpenDRAC.
         */
        schedule.setName(getScheduleName(globalUserId, connectionId));

		// Set start and end times.
        logger.info("NrmReserve.reserve: creating reservation for " +
                "startTime=" + startTime.getTime().toString() +
                ", endTime=" + endTime.getTime().toString() +
                ", bandwidth=" + desiredBandwidth);

		schedule.setStartTime(startTime.getTimeInMillis());

        if (endTime == null) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(2999, 11, 31, 0, 0);
			schedule.setEndTime(cal.getTimeInMillis());
        }
        else {
            schedule.setEndTime(endTime.getTimeInMillis());
        }

        schedule.setDuration(schedule.getEndTime() - schedule.getStartTime());

        schedule.setRecurring(false);
        schedule.setRate(desiredBandwidth);

        schedule.setPath(
                mapPath(
                    sourcePort,
                    destPort,
                    directionality,
                    desiredBandwidth,
                    nrmConfiguration.getPortPrefix(),
                    nrmConfiguration.getServiceType(),
                    nrmConfiguration.getDefaultProtection(),
                    serviceAttributes
                )
            );

        // Send the schedule request to the local NRM instance. IN_PROGRESS
		String scheduleId = null;
        RequestHandler rh = RequestHandler.INSTANCE;
		try {
            // We woudl use the blocking interface if there was one.
            logger.info("NrmReserve.reserve: submitting schedule request");
			scheduleId = rh.createScheduleAsync(token, schedule);
            logger.info("NrmReserve.reserve: submitted schedule request " + scheduleId);

            // Now we look until state=DONE and we hope result=SUCCESS.
            waitForResult(rh, token, scheduleId);
            logger.info("NrmReserve.reserve: schedule created successfully " + scheduleId);
		}
        catch (ServiceException nsi) {
            throw nsi;
        }
        catch (Exception e) {
			logger.error("OpenDRAC schedule operation failed", e);
			throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMException", "OpenDRAC schedule request failed: " + e.getMessage());
		}

        return scheduleId;
    }

	private UserType mapCredentials(NrmLoginManager nrmLoginManager) {

        UserType userType = new UserType();
		userType.setUserId(nrmLoginManager.getUserName());
		userType.setBillingGroup(new UserGroupName(nrmLoginManager.getBillingGroup()));
		userType.setSourceEndpointUserGroup(nrmLoginManager.getEndpointUserGroup());
		userType.setTargetEndpointUserGroup(nrmLoginManager.getEndpointUserGroup());
		userType.setSourceEndpointResourceGroup(nrmLoginManager.getEndpointResourceGroup());
		userType.setTargetEndpointResourceGroup(nrmLoginManager.getEndpointResourceGroup());

		return userType;
	}

    private static final String SERVICETYPE_VCAT = "VCAT";
    private static final String DEFAULT_VLANID = "0";

	private PathType mapPath(
                String source,
                String destination,
                DirectionalityType directionality,
                int desiredBandwidth,
                String portPrefix,
                String serviceType,
                ProtectionType defaultProtection,
                TechnologySpecificAttributesType serviceAttributes
            ) throws ServiceException {

		// Fail this operation anything but a BIDIRECTIONAL request.
		if (directionality != DirectionalityType.BIDIRECTIONAL) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.UNSUPPORTED_OPTION, "directionality", directionality.value());
		}

		// Build an openDRAC path.
		PathType path = new PathType();
        path.setRate(desiredBandwidth);
		path.setProtectionType(getProtection(defaultProtection, serviceAttributes));

        if (SERVICETYPE_VCAT.equalsIgnoreCase(serviceType)) {
            path.setVcatRoutingOption(true);
        }
        else {
            path.setVcatRoutingOption(false);
        }

        /*
         * TODO: Parse out the port and vlan information from the provided
         * mapsTo attributes.  In the future we will need to determine if the
         * source and destination port is a base port and vlanIDs provided in
         *
         */

        // Convert the NML port names to an OpenDRAC port name.
        String sourceName = getPortName(source, portPrefix);
        String sourceVLanId = getVLanId(source);

        if (sourceVLanId == null) {
            sourceVLanId = DEFAULT_VLANID;
        }

        String destPort = getPortName(destination, portPrefix);
        String destVLanId = getVLanId(destination);
        if (destVLanId == null) {
            destVLanId = DEFAULT_VLANID;
        }

        /*
         * TODO: We have added this to make sure the vlanId are identical.
         * OpenDRAC does not handle vlanId interchange at the moment.
         */
        if (!sourceVLanId.equalsIgnoreCase(destVLanId)) {
            ServiceException se = ExceptionCodes.buildProviderException(ExceptionCodes.VLANID_INTERCANGE_NOT_SUPPORTED, "sourcePort", source);
            ExceptionCodes.addVariable(se, "destinationPort", destination);
            logger.error("NrmReserve.mapPath: source and destination vlanId differ.");
            throw se;
        }

		// Populate the schedule endpoint information.
		EndPointType sourceEndpoint = new EndPointType();
		EndPointType destEndpoint = new EndPointType();

		sourceEndpoint.setName(sourceName);
		sourceEndpoint.setChannelNumber(-1);
		path.setSrcVlanId(sourceVLanId);

        destEndpoint.setName(destPort);
		destEndpoint.setChannelNumber(-1);
		path.setDstVlanId(destVLanId);

		path.setSourceEndPoint(sourceEndpoint);
		path.setTargetEndPoint(destEndpoint);

        logger.info("NrmReserve.mapPath: mapped path information " + path.toString());

		return path;
	}

    private static final String VLANID = ":vlan=";

    private String stripPrefix(String mapsTo, String portPrefix) {

        // Did we get a port?
        if (mapsTo == null || mapsTo.isEmpty()) {
            return null;
        }

        // Do we have anything to strip?
        if (portPrefix == null || portPrefix.isEmpty()) {
            return mapsTo;
        }

        // Strip away sailor!
        String result = null;
        int index = mapsTo.toLowerCase().lastIndexOf(portPrefix.toLowerCase());
        if (index >= 0) {
            result = mapsTo.substring(index + portPrefix.length());
        }

        return result;
    }

    private String getPortName(String mapsTo, String portPrefix) {

        // Remove the port prefi URN if there.
        String tmp = stripPrefix(mapsTo, portPrefix);

        // Remove the VLANID parameter.
        String result = null;
        if (tmp != null && !tmp.isEmpty()) {
            int index = tmp.toLowerCase().indexOf(VLANID);
            if (index < 0) {
                result = tmp;
            }
            else {
                result = tmp.substring(0, index);
            }
        }

        logger.debug("NrmReserve.getPortName: converted " + mapsTo + " to " + result);
        return result;
    }

    private String getVLanId(String mapsTo) {
        String result = null;

        if (mapsTo != null) {
            int index = mapsTo.toLowerCase().lastIndexOf(VLANID);
            if (index >= 0) {
                result = mapsTo.substring(index + VLANID.length());
            }
        }

        return result;
    }

    private static final String ATTRIBUTENAME_SNCP = "sNCP";

    private PathType.PROTECTION_TYPE getProtection(
                ProtectionType defaultProtection,
                TechnologySpecificAttributesType serviceAttributes
            ) throws ServiceException {
        /*
         * OpenDRAC supports the following two subnetwork protection types:
         *      PathType.PROTECTION_TYPE.PATH1PLUS1
         *      PathType.PROTECTION_TYPE.UNPROTECTED
         *
         * We need to set the default protection scheme for this network before
         * we see if the requesting NSA provided one.
         */
        PathType.PROTECTION_TYPE protection = PathType.PROTECTION_TYPE.UNPROTECTED;
        if (ProtectionType.UNPROTECTED != defaultProtection) {
            protection = PathType.PROTECTION_TYPE.PATH1PLUS1;
        }

        /*
         * At the moment we support a single guaranteed service attribute which
         * is the service subnetwork connection protection scheme (sncp).
         *
         */
        if (serviceAttributes != null) {
            String basicAttributeValue;
            try {
                basicAttributeValue = AttributeStatementUtilities.getBasicAttributeValue(ATTRIBUTENAME_SNCP, serviceAttributes.getGuaranteed());
            }
            catch (IllegalArgumentException ex) {
                throw ExceptionCodes.buildProviderException(ExceptionCodes.UNSUPPORTED_OPTION, "TechnologySpecificAttributesType.guaranteed", ex.getMessage());
            }

            if (basicAttributeValue != null && !basicAttributeValue.isEmpty()) {
                basicAttributeValue = basicAttributeValue.trim();

                if (basicAttributeValue.equalsIgnoreCase(ProtectionType.UNPROTECTED.value())) {
                    protection = PathType.PROTECTION_TYPE.UNPROTECTED;
                }
                else if (basicAttributeValue.equalsIgnoreCase(ProtectionType.PROTECTED.value())) {
                    protection = PathType.PROTECTION_TYPE.PATH1PLUS1;
                }
                else if (basicAttributeValue.equalsIgnoreCase(ProtectionType.REDUNDANT.value())) {
                    protection = PathType.PROTECTION_TYPE.PATH1PLUS1;
                }
                else {
                    throw ExceptionCodes.buildProviderException(ExceptionCodes.UNSUPPORTED_OPTION, ATTRIBUTENAME_SNCP, basicAttributeValue);
                }
            }
        }

        return protection;
    }

    private static final int WAIT_ITERATIONS = 60;

    private void waitForResult(RequestHandler rh, LoginToken token, String scheduleId) throws ServiceException, InterruptedException, Exception {
        // Now we look until state=DONE and we hope result=SUCCESS.
        TaskType task = rh.getProgress(token, scheduleId);
        TaskType.State state = task.getState();

        int count = 0;
        while ((state == TaskType.State.IN_PROGRESS ||
                state == TaskType.State.SUBMITTED) &&
                count < WAIT_ITERATIONS) {
            logger.debug("NrmReserve.waitForResult: " + state.name() + ", count=" + count++);

            // Pause this thread for a second while we wait for the request scheduling.
            Thread.sleep(1000);

            // Check progress.
            task = rh.getProgress(token, scheduleId);
            state = task.getState();
        }

        TaskType.Result result = task.getResult();
        String errorKey = task.getExceptionResourceKey();
        String errorMessage = task.getExceptionMessage();

        // Did it complete?
        if (state != TaskType.State.DONE) {
            logger.error("OpenDRAC schedule request failed with state =" + state.name());
            throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMScheduleStatus", "OpenDRAC schedule request failed with state =" + state.name());
        }

        if (result == null) {
            logger.error("OpenDRAC schedule request failed with state =" + state.name() + " but result == null");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMScheduleStatus", "OpenDRAC schedule request failed with state =" + state.name() + " but result == null");
        }
        else if (result != TaskType.Result.SUCCESS) {
            StringBuilder error = new StringBuilder("OpenDRAC schedule request failed with result =");
            error.append(result.name());
            error.append(", errorKey=");
            error.append(errorKey);
            error.append(", errorMessage=");
            error.append(errorMessage);
            logger.error(errorMessage);
            throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMScheduleError", error.toString());
        }
    }

    final static String CONNECTIONID_PREFIX = "urn:uuid:";
    private String getScheduleName(String globalUserId, String connectionId) {
        StringBuilder name = new StringBuilder();

        if (globalUserId != null) {
            name.append(globalUserId);
        }
        else {
            name.append("unspecifiedUserName");
        }

        name.append(" ");

        if (connectionId.trim().startsWith(CONNECTIONID_PREFIX)) {
            name.append(connectionId.substring(CONNECTIONID_PREFIX.length()));
        }
        else {
            name.append(connectionId);
        }

        return name.toString();
    }
}
