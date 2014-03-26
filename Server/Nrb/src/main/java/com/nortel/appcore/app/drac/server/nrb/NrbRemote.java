/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.server.nrb;

import java.io.File;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.opendrac.server.nrb.reporting.Report;
import org.opendrac.server.nrb.reporting.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPasswordEvaluationException;
import com.nortel.appcore.app.drac.common.errorhandling.InvalidLoginException;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.PasswordErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.GroupPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.OrganizationData;
import com.nortel.appcore.app.drac.common.security.policy.types.PersonalData;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourcePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.types.AbstractMessageType.MessageBox;
import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.common.types.AuditResult;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.GraphData;
import com.nortel.appcore.app.drac.common.types.LpcpStatus;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.ReceivedMessageType;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.SentMessageType;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.Site;
import com.nortel.appcore.app.drac.common.types.StatusType;
import com.nortel.appcore.app.drac.common.types.SysMetricType;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.UtilizationStructure;
import com.nortel.appcore.app.drac.common.utility.RmiOperationHelper;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;

/**
 * This is the NRB_PORT instance we bind into the RMI registry and permit remote
 * calls to be made to. It delegates all the real work to the NRBImpl class
 * (which also constructs it). We do this so that the NRB_PORT.jar file will contain
 * RMI stubs for all the interfaces but none of the implementation which we keep
 * in NRBImpl.
 */

public final class NrbRemote extends UnicastRemoteObject implements
    NrbInterface {

	private static final long serialVersionUID = 1L;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * Note to self: This class was generated by creating a new empty class and
	 * adding the NRBDelegate line below. Then using eclipse, select the line and
	 * under source generate Delegate Methods. If the RMI interface changes
	 * greatly, just regenerate this class.
	 */
	private static NrbServerInterface nrbServerInterface;
	private static RmiOperationHelper roh = new RmiOperationHelper("NrbInterface");

	public NrbRemote(int port, NrbServerInterface delegate)
	    throws RemoteException {
		super(port);
		nrbServerInterface = delegate;
	}

	private static Exception error(Exception t) {
		return roh.error(t);
	}

	private static void finish() {
		roh.finish();
	}

	private static UserDetails lookupUserDetails(LoginToken token) {
		try {
			return nrbServerInterface.getUserDetails(token);
		}
		catch (Exception e) {
			return null;
		}
	}

	private static UserDetails start(LoginToken token, String name,
			boolean permitMissingToken, Object... args) throws Exception {
		String host;
		try {
			host = getClientHost();
		} catch (ServerNotActiveException e) {
			host = System.getProperty("org.opendrac.controller.primary",
					"localhost");
		}
		UserDetails userDetails = lookupUserDetails(token);
		roh.start(name, host, userDetails, args);

		if (permitMissingToken) {
			/*
			 * User details will be null if a null or invalid token was
			 * supplied, on the other hand they may have provided a valid
			 * token... we just permit them to pass an invalid one.
			 */
			return userDetails;
		}
		if (token == null) {
			throw new Exception(
					"LoginToken cannot be null in call to operation " + name);
		}
		if (userDetails == null) {
			throw new InvalidLoginException(
					DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					new Object[] { "Invalid LoginToken provided " + token
							+ " for operation " + name });
		}
		return userDetails;
	}

	private static UserDetails start(LoginToken token, String name,
	    Object... args) throws Exception {
		return start(token, name, false, args);
	}

	@Override
	public void activateService(LoginToken token, String serviceId)
	    throws Exception {
		try {
			nrbServerInterface.activateService(start(token, "activateService", serviceId), serviceId);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public Map<String, Exception> activateServices(LoginToken token,
	    String[] serviceIds) throws Exception {
		try {
			Map<String, Exception> rc = nrbServerInterface.activateServices(
			    start(token, "activateServices", Arrays.toString(serviceIds)),
			    serviceIds);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void addManualAdjacency(LoginToken token, String sourceIEEE,
	    String sourcePort, String destIEEE, String destPort) throws Exception {
		try {
			nrbServerInterface.addManualAdjacency(
			    start(token, "addManualAdjacency", sourceIEEE, sourcePort, destIEEE,
			        destPort), sourceIEEE, sourcePort, destIEEE, destPort);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void updateNetworkElementPosition(LoginToken token, String ip,
	    String port, Double positionX, Double positionY) throws Exception {
		try {
			nrbServerInterface.updateNetworkElementPosition(
			    start(token, "updateNetworkElementPosition", ip, port, positionX,
			        positionY), ip, port, positionX, positionY);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void addSite(LoginToken token, Site site) throws Exception {
		try {
			nrbServerInterface.addSite(start(token, "addSite", site), site);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public String asyncCreateSchedule(LoginToken token, Schedule aSchedule)
	    throws Exception {
		try {
			String rc = nrbServerInterface.asyncCreateSchedule(
			    start(token, "asyncCreateSchedule", aSchedule), aSchedule);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<AuditResult> auditModel(LoginToken token) throws Exception {
		try {
			List<AuditResult> rc = nrbServerInterface.auditModel(start(token, "auditModel"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public String auditUserProfileLinkages(LoginToken token) throws Exception {
		try {
			String rc = nrbServerInterface.auditUserProfileLinkages(start(token,
			    "auditUserProfileLinkages"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void authenticate(ClientLoginType loginType, String user,
	    char[] password, String clientIp, String aSelectTicket) throws Exception {
		try {
			start(null, "login", true, new Object[]{});
			nrbServerInterface.authenticate(loginType, user, password, clientIp, aSelectTicket);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public LoginToken authorize(ClientLoginType loginType, String user,
	    char[] password, String clientIp, String aSelectTicket) throws Exception {
		try {
			start(null, "login", true, new Object[]{});
			LoginToken loginToken = nrbServerInterface.authorize(loginType, user, password, clientIp,
			    aSelectTicket);
			finish();
			return loginToken;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void cancelSchedule(LoginToken token, String scheduleId)
	    throws Exception {
		try {
			nrbServerInterface.cancelSchedule(start(token, "cancelSchedule", scheduleId), scheduleId);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public Map<String, Exception> cancelSchedules(LoginToken token,
	    String[] scheduleIds) throws Exception {
		try {
			Map<String, Exception> rc = nrbServerInterface.cancelSchedules(
			    start(token, "cancelSchedules", Arrays.toString(scheduleIds)),
			    scheduleIds);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void cancelService(LoginToken token, String serviceId)
	    throws Exception {
		try {
			nrbServerInterface.cancelService(start(token, "cancelService", serviceId), serviceId);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public Map<String, Exception> cancelServices(LoginToken token,
	    String[] serviceIds) throws Exception {
		try {
			Map<String, Exception> rc = nrbServerInterface.cancelServices(
			    start(token, "cancelServices", Arrays.toString(serviceIds)),
			    serviceIds);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public boolean cancelTask(LoginToken token, String taskId) throws Exception {
		try {
			boolean rc = nrbServerInterface.cancelTask(start(token, "cancelTask", taskId), taskId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void changeNetworkElementPassword(LoginToken token,
	    NetworkElementHolder updatedNE) throws Exception {
		try {
			nrbServerInterface.changeNetworkElementPassword(
			    start(token, "changeNetworkElementPassword", updatedNE), updatedNE);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}

	}

	@Override
	public void changePassword(LoginToken token, String userId, String oldpw,
	    String newpw, ClientLoginType loginType, String clientIp, String sessionId)
	    throws Exception {
		try {
			// Two cases to deal with:
			//
			// (1) a user has successfully logged in (has valid token, userDetails in
			// cache, etc).
			// and now wants to change their password (menu item on the web ui)
			//
			// (2) a user has attempted to login, but it was detected that their
			// password has
			// has expired. The webserver has redirected the user down the
			// changePassword code
			// path WITHOUT having issued a valid login token. We have to essentially
			// retest for
			// a login here using the old password in case some evil-doer is calling
			// here directly
			// with ill intent.

			// (1)
			UserDetails userDetails = start(token, "changePassword", true, new Object[]{});

			if (userDetails == null) {
				// (2)
				userDetails = nrbServerInterface.prepareLogin(loginType, userId, oldpw.toCharArray(),
				    clientIp, null, sessionId);

				SessionErrorCode ec = userDetails.getErrorCode();

				if (ec == SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_RETRY
				    || ec == SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_TO_BE_LOCKEDOUT) {
					throw new DracPasswordEvaluationException(
					    new PasswordErrorCode[] { PasswordErrorCode.ERROR_OLDPW_INVALID });
				}

				if (ec != SessionErrorCode.ERROR_PASSWORD_EXPIRED
				    && ec != SessionCodes.SessionErrorCode.NO_ERROR) {
					log.error("changePassword failed on prepareLogin"
					    + userDetails.getErrorCode());
					throw new NrbException(
					    DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
					    new Object[] { userDetails.getErrorCode() });
				}
			}

			nrbServerInterface.changePassword(userDetails, oldpw, newpw);

			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public boolean clearTaskInfo(LoginToken token, String taskId)
	    throws Exception {
		try {
			boolean rc = nrbServerInterface.clearTaskInfo(start(token, "clearTaskInfo", taskId),
			    taskId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void confirmSchedule(LoginToken token, String scheduleId)
	    throws Exception {
		try {
			nrbServerInterface.confirmSchedule(start(token, "confirmSchedule", scheduleId), scheduleId);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public Map<String, Exception> confirmSchedules(LoginToken token,
	    String[] scheduleIds) throws Exception {
		try {
			Map<String, Exception> rc = nrbServerInterface.confirmSchedules(
			    start(token, "confirmSchedules", Arrays.toString(scheduleIds)),
			    scheduleIds);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void correctModel(LoginToken token) throws Exception {
		try {
			nrbServerInterface.correctModel(start(token, "correctModel"));
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void createResourceGroupProfile(LoginToken token,
	    ResourceGroupProfile profile) throws Exception {
		try {
			nrbServerInterface.createResourceGroupProfile(
			    start(token, "createResourceGroupProfile", profile), profile);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public ScheduleResult createSchedule(LoginToken token,
	    Map<SPF_KEYS, String> parms, boolean queryOnly) throws Exception {
		try {
			ScheduleResult rc = nrbServerInterface.createSchedule(
			    start(token, "createSchedule", parms, queryOnly), parms, queryOnly);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public String createService(LoginToken token, String scheduleId,
	    DracService aService) throws Exception {
		try {
			String rc = nrbServerInterface.createService(
			    start(token, "createService", scheduleId, aService), scheduleId,
			    aService);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void createUserGroupProfile(LoginToken token, UserGroupProfile profile)
	    throws Exception {
		try {
			nrbServerInterface.createUserGroupProfile(start(token, "createUserGroupProfile", profile),
			    profile);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void createUserProfile(LoginToken token, String userID,
	    AbstractCredential credential, AccountStatus status) throws Exception {
		try {
			nrbServerInterface.createUserProfile(
			    start(token, "createUserProfile", userID, credential, status),
			    userID, credential, status);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void deleteCrossConnection(LoginToken token, CrossConnection xcon)
	    throws Exception {
		try {
			nrbServerInterface.deleteCrossConnection(start(token, "deleteCrossConnection", xcon), xcon);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void deleteFacility(LoginToken token, String neId, String aid)
	    throws Exception {
		try {
			nrbServerInterface.deleteFacility(start(token, "deleteFacility", neId, aid), neId, aid);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void deleteManualAdjacency(LoginToken token, String neIEEE, String port)
	    throws Exception {
		try {
			nrbServerInterface.deleteManualAdjacency(
			    start(token, "deleteManualAdjacency", neIEEE, port), neIEEE, port);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void deleteManualAdjacency(LoginToken token, String sourceIEEE,
	    String sourcePort, String destIEEE, String destPort) throws Exception {
		try {
			nrbServerInterface.deleteManualAdjacency(
			    start(token, "deleteManualAdjacency", sourceIEEE, sourcePort,
			        destIEEE, destPort), sourceIEEE, sourcePort, destIEEE, destPort);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void deleteNetworkElement(LoginToken token, NetworkElementHolder oldNe)
	    throws Exception {
		try {
			nrbServerInterface.deleteNetworkElement(start(token, "deleteNetworkElement", oldNe), oldNe);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void deleteResourceGroupProfile(LoginToken token,
	    String resourceGroupName) throws Exception {

		try {
			nrbServerInterface.deleteResourceGroupProfile(
			    start(token, "deleteResourceGroupProfile", resourceGroupName),
			    resourceGroupName);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void deleteUserGroupProfile(LoginToken token, UserGroupName name)
	    throws Exception {
		try {
			nrbServerInterface.deleteUserGroupProfile(start(token, "deleteUserGroupProfile", name),
			    name);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void deleteUserProfile(LoginToken token, String userID)
	    throws Exception {
		try {
			nrbServerInterface.deleteUserProfile(start(token, "deleteUserProfile", userID), userID);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void editEndPoint(LoginToken token, EndPointType endPoint)
	    throws Exception {
		try {
			nrbServerInterface.editEndPoint(start(token, "editEndPoint", endPoint), endPoint);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void editFacility(LoginToken token, String neid, String aid,
	    String tna, String faclabel, String mtu, String srlg, String grp,
	    String cost, String metric2, String sigType, String constraints,
	    String domainId, String siteId) throws Exception {
		try {
			nrbServerInterface.editFacility(
			    start(token, "editFacility", neid, aid, tna, faclabel, mtu, srlg,
			        grp, cost, metric2, sigType, constraints, domainId, siteId),
			    neid, aid, tna, faclabel, mtu, srlg, grp, cost, metric2, sigType,
			    constraints, domainId, siteId);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void enrollNetworkElement(LoginToken token, NetworkElementHolder newNe)
	    throws Exception {
		try {
			nrbServerInterface.enrollNetworkElement(start(token, "enrollNetworkElement", newNe), newNe);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<Schedule> getActiveSchedules(LoginToken token) throws Exception {
		try {
			List<Schedule> rc = nrbServerInterface.getActiveSchedules(start(token,
			    "getActiveSchedules"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<String> getAlarms(LoginToken token, Map<String, Object> filter)
	    throws Exception {
		try {
			List<String> rc = nrbServerInterface.getAlarms(start(token, "getAlarms", filter), filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<NetworkElementHolder> getAllNetworkElements(LoginToken token)
	    throws Exception {
		try {
			List<NetworkElementHolder> rc = nrbServerInterface.getAllNetworkElements(start(token,
			    "getAllNetworkElements"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public int getConfirmationTimeout(LoginToken token) throws Exception {
		try {
			int rc = nrbServerInterface.getConfirmationTimeout(start(token, "getConfirmationTimeout"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public Resource getEndpointResource(LoginToken token, String resourceID)
	    throws Exception {
		try {
			Resource rc = nrbServerInterface.getEndpointResource(
			    start(token, "getEndpointResource", resourceID), resourceID);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<Facility> getFacilities(LoginToken token, String neId)
	    throws Exception {
		try {
			List<Facility> rc = nrbServerInterface.getFacilities(start(token, "getFacilities", neId),
			    neId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public Map<String, BigInteger> getFacilityConstraints(LoginToken token,
	    Map<String, String> filter) throws Exception {
		try {
			Map<String, BigInteger> rc = nrbServerInterface.getFacilityConstraints(
			    start(token, "getFacilityConstraints", filter), filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public GlobalPolicy getGlobalPolicy(LoginToken token) throws Exception {
		try {
			GlobalPolicy rc = nrbServerInterface.getGlobalPolicy(start(token, "getGlobalPolicy"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public GraphData getGraphData(LoginToken token) throws Exception {
		try {
			GraphData rc = nrbServerInterface.getGraphData(start(token, "getGraphData"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public ServerInfoType getInfo(LoginToken token) throws Exception {
		try {
			ServerInfoType rc = nrbServerInterface.getInfo(start(token, "getInfo", true, new Object[]{}));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<String> getInprogressCalls(LoginToken token) throws Exception {
		try {
			List<String> rc = nrbServerInterface
			    .getInprogressCalls(start(token, "getInprogressCalls"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<BandwidthRecord> getInternalBandwithUsage(LoginToken token,
	    long startTime, long endTime) throws Exception {
		try {
			List<BandwidthRecord> rc = nrbServerInterface.getInternalBandwithUsage(
			    start(token, "getInternalBandwithUsage", startTime, endTime),
			    startTime, endTime);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<LogRecord> getLogs(LoginToken token, long startTime,
	    long endTime, Map<String, String> filter) throws Exception {
		try {
			List<LogRecord> rc = nrbServerInterface.getLogs(
			    start(token, "getLogs", startTime, endTime, filter), startTime,
			    endTime, filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public String getLpcpDiscoveryStatus(LoginToken token) throws Exception {
		try {
			String rc = nrbServerInterface.getLpcpDiscoveryStatus(start(token,
			    "getLpcpDiscoveryStatus"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public LpcpStatus getLpcpStatus(LoginToken token) throws Exception {
		try {
			LpcpStatus rc = nrbServerInterface.getLpcpStatus(start(token, "getLpcpStatus"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<NetworkElementAdjacency> getNetworkElementAdjacencies(
	    LoginToken token, Map<String, String> filter) throws Exception {
		try {
			List<NetworkElementAdjacency> rc = nrbServerInterface.getNetworkElementAdjacencies(
			    start(token, "getNetworkElementAdjacencies", filter), filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<CrossConnection> getNetworkElementConnections(LoginToken token,
	    Map<String, String> filter) throws Exception {
		try {
			List<CrossConnection> rc = nrbServerInterface.getNetworkElementConnections(
			    start(token, "getNetworkElementConnections", filter), filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<Facility> getNetworkElementFacilities(LoginToken token,
	    Map<String, String> filter) throws Exception {
		try {
			List<Facility> rc = nrbServerInterface.getNetworkElementFacilities(
			    start(token, "getNetworkElementFacilities", filter), filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<NetworkElementHolder> getNetworkElements(LoginToken token,
	    Map<String, String> filter) throws Exception {
		try {
			List<NetworkElementHolder> rc = nrbServerInterface.getNetworkElements(
			    start(token, "getNetworkElements", filter), filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public String getPeerIPAddress(LoginToken token) throws Exception {
		try {
			String rc = nrbServerInterface.getPeerIPAddress(start(token, "getPeerIPAddress"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<String> getResourceGroupNameLineage(LoginToken token,
	    ResourceGroupProfile rgProfile) throws Exception {
		try {
			List<String> rc = nrbServerInterface
			    .getResourceGroupNameLineage(
			        start(token, "getResourceGroupNameLineage with rgProfile",
			            rgProfile), rgProfile);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public ResourceGroupProfile getResourceGroupProfile(LoginToken token,
	    String resourceGroupName) throws Exception {
		try {
			ResourceGroupProfile rc = nrbServerInterface.getResourceGroupProfile(
			    start(token, "getResourceGroupProfile", resourceGroupName),
			    resourceGroupName);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<ResourceGroupProfile> getResourceGroupProfileList(LoginToken token)
	    throws Exception {
		try {
			List<ResourceGroupProfile> rc = nrbServerInterface.getResourceGroupProfileList(start(
			    token, "getResourceGroupProfileList"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public Schedule getSchedule(LoginToken token, String scheduleId)
	    throws Exception {
		try {
			Schedule rc = nrbServerInterface.getSchedule(start(token, "getSchedule", scheduleId),
			    scheduleId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public DracService getCurrentlyActiveServiceByScheduleId(LoginToken token, String scheduleId) throws Exception {
		Date now = new Date();
		DracService serviceToReturn = null ;
		Schedule schedule = getSchedule(token, scheduleId);
		if (schedule != null) {			
			for (DracService service : schedule.getServiceIdList()) {
				if (service.getStartTime() < now.getTime() && service.getEndTime() > now.getTime()) {
					serviceToReturn = service;
					break;
				}
			}
		}
		return serviceToReturn;
	}
	
	@Override
	public int extendServiceTime(LoginToken token, DracService serviceToExtend, int minutesToExtendService)throws Exception{
		try {
			int availableMinutes = nrbServerInterface.extendServiceTime(start(token, "extendServiceTime", serviceToExtend.getId()),
					serviceToExtend, minutesToExtendService);
			finish();
			return availableMinutes;
		}
		catch (Exception t) {
			throw error(t);
		}	
	}
	
	@Override
	public List<Schedule> getSchedules(LoginToken token,
	    Map<String, String> filter) throws Exception {
		try {
			List<Schedule> rc = nrbServerInterface.getSchedules(start(token, "getSchedules", filter),
			    filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<ServiceXml> getServices(LoginToken token,
	    Map<String, Object> filter) throws Exception {
		try {
			List<ServiceXml> rc = nrbServerInterface.getServices(start(token, "getServices", filter),
			    filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<String> getServicesEligibleForPurge(LoginToken token,
	    Map<String, String> filter) throws Exception {
		try {
			List<String> rc = nrbServerInterface.getServicesEligibleForPurge(
			    start(token, "getServicesEligibleForPurge", filter), filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<ServiceXml> getServicesFromAID(LoginToken token,
	    Map<String, String> filter) throws Exception {
		try {
			List<ServiceXml> rc = nrbServerInterface.getServicesFromAID(
			    start(token, "getServicesFromAID", filter), filter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<DracService> getServiceUsageForTNA(LoginToken token, String tna)
	    throws Exception {
		try {
			List<DracService> rc = nrbServerInterface.getServiceUsageForTNA(
			    start(token, "getServiceUsageForTNA", tna), tna);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public String getSRLGListForService(LoginToken token, String serviceId)
	    throws Exception {
		try {
			String rc = nrbServerInterface.getSRLGListForService(
			    start(token, "getSRLGListForService", serviceId), serviceId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<StatusType> getStatusInfo(LoginToken token, String taskId)
	    throws Exception {
		try {
			List<StatusType> rc = nrbServerInterface.getStatusInfo(
			    start(token, "getStatusInfo", taskId), taskId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public SysMetricType getSystemMetric(LoginToken token) throws Exception {
		try {
			SysMetricType rc = nrbServerInterface.getSystemMetric(start(token, "getSystemMetric"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<TaskType> getTaskInfo(LoginToken token) throws Exception {
		try {
			List<TaskType> rc = nrbServerInterface.getTaskInfo(start(token, "getTaskInfo"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public TaskType getTaskInfo(LoginToken token, String taskId) throws Exception {
		try {
			TaskType rc = nrbServerInterface.getTaskInfo(start(token, "getTaskInfo", taskId), taskId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public UserDetails getUserDetails(LoginToken token) throws Exception {
		try {
			/**
			 * *********************************** This will end up calling
			 * getUserDetails twice as the start method will invoke getUserDetails to
			 * verify the token represents a valid logged in user, however start will
			 * return null if its not valid where as getUserDetails will toss an
			 * Exception, which is what we want.
			 */
			start(token, "getUserDetails");
			UserDetails rc = nrbServerInterface.getUserDetails(token);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<EndPointType> getUserEndpoints(LoginToken token,
	    List<UserGroupName> userGroupFilter, Map<String, String> facilityFilter)
	    throws Exception {
		try {
			List<EndPointType> rc = nrbServerInterface
			    .getUserEndpoints(
			        start(token,
			            "getUserEndpoints with userGroupFilter and facilityFilter",
			            userGroupFilter, facilityFilter), userGroupFilter,
			        facilityFilter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<EndPointType> getUserEndpoints(LoginToken token,
	    Map<String, String> facilityFilter) throws Exception {
		try {
			List<EndPointType> rc = nrbServerInterface.getUserEndpoints(
			    start(token, "getUserEndpoints with facilityFilter", facilityFilter),
			    facilityFilter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<EndpointResourceUiType> getUserEndpointsUIInfo(LoginToken token,
	    Map<String, String> facilityFilter) throws Exception {
		try {
			List<EndpointResourceUiType> rc = nrbServerInterface.getUserEndpointsUIInfo(
			    start(token, "getUserEndpointsUIInfo with facilityFilter",
			        facilityFilter), facilityFilter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<EndpointResourceUiType> getUserEndpointsUIInfo(LoginToken token,
	    String resGroup, Map<String, String> facilityFilter) throws Exception {
		try {
			List<EndpointResourceUiType> rc = nrbServerInterface.getUserEndpointsUIInfo(
			    start(token,
			        "getUserEndpointsUIInfo with resourceGroup and facilityFilter",
			        resGroup, facilityFilter), resGroup, facilityFilter);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<String> getUserGroupNameLineage(LoginToken token,
	    UserGroupProfile ugProfile) throws Exception {
		try {
			List<String> rc = nrbServerInterface.getUserGroupNameLineage(
			    start(token, "getUserGroupNameLineage with ugProfile", ugProfile),
			    ugProfile);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public UserGroupProfile getUserGroupProfile(LoginToken token,
	    UserGroupName name) throws Exception {
		try {
			UserGroupProfile rc = nrbServerInterface.getUserGroupProfile(
			    start(token, "getUserGroupProfile", name), name);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<UserGroupProfile> getUserGroupProfileList(LoginToken token)
	    throws Exception {
		try {
			List<UserGroupProfile> rc = nrbServerInterface.getUserGroupProfileList(start(token,
			    "getUserGroupProfileList"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<List<String>> getUserNameLineage(LoginToken token,
	    UserProfile userProfile) throws Exception {
		try {
			List<List<String>> rc = nrbServerInterface.getUserNameLineage(
			    start(token, "getUserNameLineage with userProfile", userProfile),
			    userProfile);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public UserProfile getUserProfile(LoginToken token, String userID)
	    throws Exception {
		try {
			UserProfile rc = nrbServerInterface.getUserProfile(start(token, "getUserProfile", userID),
			    userID);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<UserProfile> getUserProfileList(LoginToken token)
	    throws Exception {
		try {
			List<UserProfile> rc = nrbServerInterface.getUserProfileList(start(token,
			    "getUserProfileList"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public double getUtilization(LoginToken token, String tnaName)
	    throws Exception {
		try {
			double rc = nrbServerInterface.getUtilization(start(token, "getUtilization", tnaName),
			    tnaName);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public boolean isAllowed(LoginToken token, PolicyRequest request)
	    throws Exception {
		try {
			boolean rc = nrbServerInterface.isAllowed(start(token, "isAllowed", request), request);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public boolean isAlive(LoginToken token) throws Exception {
		try {
			boolean rc = nrbServerInterface.isAlive(start(token, "isAlive", true, new Object[]{}));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public boolean isResourceGroupEditable(LoginToken token,
	    ResourceGroupProfile rgProfile) throws Exception {
		try {
			boolean rc = nrbServerInterface.isResourceGroupEditable(
			    start(token, "isResourceGroupEditable with rgProfile", rgProfile),
			    rgProfile);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public boolean isUserEditable(LoginToken token, UserProfile userProfile)
	    throws Exception {
		try {
			boolean rc = nrbServerInterface.isUserEditable(
			    start(token, "isUserEditable with userProfile", userProfile),
			    userProfile);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public boolean isUserGroupEditable(LoginToken token,
	    UserGroupProfile ugProfile) throws Exception {
		try {
			boolean rc = nrbServerInterface.isUserGroupEditable(
			    start(token, "isUserGroupEditable with ugProfile", ugProfile),
			    ugProfile);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public String loadUserPreferences(LoginToken token, String userId)
	    throws Exception {
		try {
			String rc = nrbServerInterface.loadUserPreferences(
			    start(token, "loadUserPreferences", userId), userId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public LoginToken login(ClientLoginType loginType, String user,
	    char[] password, String clientIp, String aSelectTicket, String sessionId) throws Exception {
		try {
			/**
			 * We dont have a LoginToken to pass to start here!
			 */
			start(null, "login", true, new Object[]{user, "***", clientIp, aSelectTicket});
			LoginToken rc = nrbServerInterface.login(loginType, user, password, clientIp,
			    aSelectTicket, sessionId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void logout(LoginToken token) throws Exception {
		try {
			/**
			 * We want to pass the token to the server this time, not user details!
			 */
			if(token != null){
				start(token, "logout");
				nrbServerInterface.logout(token);
			}
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}
	

	@Override
	public void purgeServices(LoginToken token, List<String> serviceIds)
	    throws Exception {
		try {
			nrbServerInterface.purgeServices(start(token, "explicitPurge", serviceIds), serviceIds);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<AlarmType> queryAllServiceAlarms(LoginToken token,
	    long startTime, long endTime) throws Exception {
		try {
			List<AlarmType> rc = nrbServerInterface.queryAllServiceAlarms(
			    start(token, "queryAllServiceAlarms", startTime, endTime), startTime,
			    endTime);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<EndPointType> queryAllUserEndpoints(LoginToken token)
	    throws Exception {
		try {
			// @TODO Remove the endpoint cache then limit this to logged in callers!
			List<EndPointType> rc = nrbServerInterface.queryAllUserEndpoints(start(token,
			    "queryAllUserEndpoints"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public boolean queryPath(LoginToken token, DracService aService)
	    throws Exception {
		try {
			boolean rc = nrbServerInterface.queryPath(start(token, "queryPath", aService), aService);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public Schedule queryScheduleByService(LoginToken token, String serviceId)
	    throws Exception {
		try {
			Schedule rc = nrbServerInterface.queryScheduleByService(
			    start(token, "queryScheduleByService", serviceId), serviceId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<Schedule> querySchedules(LoginToken token, long startTime, long endTime) throws Exception {
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		return querySchedules(token, startTime, endTime, groups);
	}
	@Override
	public List<Schedule> querySchedules(LoginToken token, long startTime,
		    long endTime, List<UserGroupName> groups) throws Exception {
			return querySchedules(token, startTime, endTime, groups, null);
	}
	@Override
	public List<Schedule> querySchedules(LoginToken token, long startTime, long endTime, String name) throws Exception{
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		return querySchedules(token, startTime, endTime, groups, name);		
	}	
	@Override
	public List<Schedule> querySchedules(LoginToken token, long startTime,
	    long endTime, List<UserGroupName> groups, String name) throws Exception {
		try {
			List<Schedule> rc = nrbServerInterface.querySchedules(
			    start(token, "querySchedules", startTime, endTime, groups),
			    startTime, endTime, groups, name);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}
	@Override
	public AlarmType queryServiceAlarm(LoginToken token, String alarmId)
	    throws Exception {
		try {
			AlarmType rc = nrbServerInterface.queryServiceAlarm(
			    start(token, "queryServiceAlarm", alarmId), alarmId);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<DracService> queryServices(LoginToken token, long startTime,
	    long endTime, List<UserGroupName> groups) throws Exception {
		try {
			List<DracService> rc = nrbServerInterface.queryServices(
			    start(token, "queryServices", startTime, endTime, groups), startTime,
			    endTime, groups);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public UtilizationStructure queryUtilization(LoginToken token, String tna,
	    double speed, long startTime, long endTime, int days, TimeZone tz)
	    throws Exception {
		try {
			UtilizationStructure rc = nrbServerInterface.queryUtilization(
			    start(token, "queryUtilization", tna, speed, startTime, endTime,
			        days, tz), tna, speed, startTime, endTime, days, tz);
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void registerForLpcpEventNotifications(LoginToken token,
	    LpcpEventCallback cb) throws Exception {
		try {
			nrbServerInterface.registerForLpcpEventNotifications(
			    start(token, "registerForLpcpEventNotifications", cb), cb);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public List<Site> retrieveSiteList(LoginToken token) throws Exception {
		try {
			List<Site> rc = nrbServerInterface.retrieveSiteList(start(token, "retrieveSiteList"));
			finish();
			return rc;
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void saveUserPreferences(LoginToken token, String userId,
	    String xmlPreferences) throws Exception {
		try {
			nrbServerInterface.saveUserPreferences(
			    start(token, "saveUserPreferences", userId, xmlPreferences), userId,
			    xmlPreferences);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void sessionValidate(LoginToken token) throws Exception {
		try {
			/**
			 * Need to pass the token to the server here!
			 */
			start(token, "sessionValidate");
			nrbServerInterface.sessionValidate(token);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setConfirmationTimeout(LoginToken token, int timeout)
	    throws Exception {
		try {
			nrbServerInterface.setConfirmationTimeout(start(token, "setConfirmationTimeout", timeout),
			    timeout);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setDefaultGlobalPolicy(LoginToken token, GlobalPolicy policy)
	    throws Exception {
		try {
			nrbServerInterface.setDefaultGlobalPolicy(start(token, "setDefaultGlobalPolicy", policy),
			    policy);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setResourceGroupMembership(LoginToken token,
	    String resourceGroupName, MembershipData membershipData) throws Exception {
		try {
			nrbServerInterface.setResourceGroupMembership(
			    start(token, "setResourceGroupMembership", resourceGroupName,
			        membershipData), resourceGroupName, membershipData);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setResourceGroupPolicy(LoginToken token,
	    String resourceGroupName, ResourcePolicy resourcePolicy) throws Exception {
		try {
			nrbServerInterface.setResourceGroupPolicy(
			    start(token, "setResourceGroupPolicy", resourceGroupName,
			        resourcePolicy), resourceGroupName, resourcePolicy);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setResourceGroupResourceList(LoginToken token,
	    String resourceGroupName, List<Resource> resource) throws Exception {
		try {
			nrbServerInterface.setResourceGroupResourceList(
			    start(token, "setResourceGroupResourceList", resourceGroupName,
			        resource), resourceGroupName, resource);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setScheduleOffset(LoginToken token, int offset) throws Exception {
		try {
			nrbServerInterface.setScheduleOffset(start(token, "setScheduleOffset", offset), offset);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setupUserAuthenticationAccount(LoginToken token, String userID,
	    AbstractCredential cred) throws Exception {
		try {
			nrbServerInterface.setupUserAuthenticationAccount(
			    start(token, "setupUserAuthenticationAccount", userID, cred), userID,
			    cred);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setUserAccountStatus(LoginToken token, String userID,
	    AccountStatus status) throws Exception {
		try {
			nrbServerInterface.setUserAccountStatus(
			    start(token, "setUserAccountStatus", userID, status), userID, status);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setUserAuthenticationData(LoginToken token, String userID,
	    AuthenticationData authenticationData) throws Exception {
		try {
			nrbServerInterface.setUserAuthenticationData(
			    start(token, "setUserAuthenticationData", userID, authenticationData),
			    userID, authenticationData);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setUserGroupMembership(LoginToken token,
	    UserGroupName userGroupName, MembershipData data) throws Exception {
		try {
			nrbServerInterface.setUserGroupMembership(
			    start(token, "setUserGroupMembership", userGroupName, data),
			    userGroupName, data);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setUserGroupUserGroupPolicy(LoginToken token,
	    UserGroupName userGroupName, GroupPolicy groupPolicy) throws Exception {
		try {
			nrbServerInterface.setUserGroupUserGroupPolicy(
			    start(token, "setUserGroupUserGroupPolicy", userGroupName,
			        groupPolicy), userGroupName, groupPolicy);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setUserMembership(LoginToken token, String userID,
	    MembershipData membership) throws Exception {
		try {
			nrbServerInterface.setUserMembership(
			    start(token, "setUserMembership", userID, membership), userID,
			    membership);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setUserOrganization(LoginToken token, String userID,
	    OrganizationData orgData) throws Exception {
		try {
			nrbServerInterface.setUserOrganization(
			    start(token, "setUserOrganization", userID, orgData), userID, orgData);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setUserPersonalData(LoginToken token, String userID,
	    PersonalData personalData) throws Exception {
		try {
			nrbServerInterface.setUserPersonalData(
			    start(token, "setUserPersonalData", userID, personalData), userID,
			    personalData);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void setUserTimeZoneIDPreference(LoginToken token, String userID,
	    String localeValue) throws Exception {
		try {
			nrbServerInterface.setUserTimeZoneIDPreference(
			    start(token, "setUserTimeZoneIDPreference", userID, localeValue),
			    userID, localeValue);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void toggleNetworkElementAssociation(LoginToken token,
	    NetworkElementHolder existingNe) throws Exception {
		try {
			nrbServerInterface.toggleNetworkElementAssociation(
			    start(token, "toggleNetworkElementAssociation", existingNe),
			    existingNe);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	@Override
	public void updateSite(LoginToken token, Site site) throws Exception {
		try {
			nrbServerInterface.updateSite(start(token, "updateSite", site), site);
			finish();
		}
		catch (Exception t) {
			throw error(t);
		}
	}

	private Report getAggregatedReport(int reportId, String titleLeader,
			Date startDate, Date endDate, Map<String, String> filter,
			List<UserGroupName> groups) throws Exception {
		ReportGenerator generator = new ReportGenerator();
		Report report = generator.getAggregatedReport(titleLeader, startDate,
				endDate, filter, groups);
		return report;
	}

	private Report getAggregatedReportFull(int reportId, String titleLeader,
			Date startDate, Date endDate, Map<String, String> filter,
			List<UserGroupName> groups) throws Exception {
		ReportGenerator generator = new ReportGenerator();
		Report report = generator.getAggregatedReportFull(titleLeader,
				startDate, endDate, filter, groups);
		return report;
	}

	private List<Report> getAggregatedReportForGui(int reportId,
			String titleLeader, Date startDate, Date endDate,
			Map<String, String> filter, List<UserGroupName> groups)
			throws Exception {
		ReportGenerator generator = new ReportGenerator();
		return generator.getReportsForGui(titleLeader, startDate, endDate,
				filter, groups);
	}

	public List<Report> getAggregatedReportForGui(int reportId,
			String titleLeader, Date startDate, Date endDate,
			Map<String, String> filter, LoginToken token) throws Exception {
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		return getAggregatedReportForGui(reportId, titleLeader, startDate,
				endDate, filter, groups);
	}
	private Report getQualitativeReportByPort(Date startDate, Date endDate,
			List<UserGroupName> groups) throws Exception {
		ReportGenerator generator = new ReportGenerator();
		return generator.getQualitativeReportByPort(startDate, endDate, groups);
	}
	
	private Report getQualitativeReport(Date startDate, Date endDate,
			List<UserGroupName> groups) throws Exception {
		ReportGenerator generator = new ReportGenerator();
		return generator.getQualitativeReport(startDate, endDate, groups);
	}

	public Report getQualitativeReport(Date startDate, Date endDate,
			LoginToken token) throws Exception {
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		return getQualitativeReport(startDate, endDate, groups);
	}
	public Report getQualitativeReportByPort(Date startDate, Date endDate,
			LoginToken token) throws Exception {
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		return getQualitativeReportByPort(startDate, endDate, groups);
	}
	
	private Report getScheduledServicesReport(Date startDate, Date endDate,
			List<UserGroupName> groups) throws Exception {
		ReportGenerator generator = new ReportGenerator();
		return generator.generateScheduledServicesReport(startDate, endDate,
				groups);
	}

	public Report getScheduledServicesReport(Date startDate, Date endDate,
			LoginToken token) throws Exception {
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		return getScheduledServicesReport(startDate, endDate, groups);
	}

	private Report getActualschedulesReport(Date startDate, Date endDate,
			List<UserGroupName> groups) throws Exception {
		ReportGenerator generator = new ReportGenerator();
		return generator.getServicesReport(startDate, endDate, groups);
	}

	public Report getActualschedulesReport(Date startDate, Date endDate,
			LoginToken token) throws Exception {
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		return getActualschedulesReport(startDate, endDate, groups);
	}

	public File getAggregatedReportAsFile(int reportId, String titleLeader,
			Date startDate, Date endDate, Map<String, String> filter,
			LoginToken token) throws Exception {
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		Report report = getAggregatedReport(reportId, titleLeader, startDate,
				endDate, filter, groups);
		return report.getAsFile();
	}

	public Report getAggregatedReport(int reportId, String titleLeader,
			Date startDate, Date endDate, Map<String, String> filter,
			LoginToken token) throws Exception {
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		Report report = getAggregatedReport(reportId, titleLeader, startDate,
				endDate, filter, groups);
		return report;
	}

	public Report getAggregatedReportFull(int reportId, String titleLeader,
			Date startDate, Date endDate, Map<String, String> filter,
			LoginToken token) throws Exception {
		List<UserGroupName> groups = getFilterGroupsForCurrentUser(token);
		Report report = getAggregatedReportFull(reportId, titleLeader,
				startDate, endDate, filter, groups);
		return report;
	}
	
	/**
	 * Retrieve a list of usergroups to filter results by i.e. show the user only
	 * those objects (s)he has access to. If the user is a member of admin, the
	 * list will be empty, so that no results are filtered
	 * 
	 * @param token
	 *          LoginToken token from request, set at login
	 * @return List<UserGroupName> list of usergropups
	 * @throws Exception
	 */
	public List<UserGroupName> getFilterGroupsForCurrentUser(LoginToken token)
	    throws Exception {
		List<UserGroupProfile> userGroups = getUserGroupProfileList(token);
		if(userGroups == null){
			userGroups = new ArrayList<UserGroupProfile>();
		}
		boolean isSuperuser = false;
		List<UserGroupName> filterGroups = new ArrayList<UserGroupName>();
		for (UserGroupProfile profile : userGroups) {
			filterGroups.add(profile.getName());
			if (profile.getUserGroupType().equals(UserGroupType.SYSTEM_ADMIN)) {
				isSuperuser = true;
			}
		}
		if (isSuperuser) {
			filterGroups = new ArrayList<UserGroupName>();
		}
		return filterGroups;
	}

  @Override
  public void updateAddressAndPort(LoginToken token, final String oldAddress, final int oldPort, final String newAddress, final int newPort) throws Exception {
    try {
      nrbServerInterface.updateAddressAndPort(
          start(token, "updateAddressAndPort", oldAddress, oldPort, newAddress, newPort), oldAddress, oldPort, newAddress, newPort);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
    
  }
  @Override
  public void setLockedIPs(LoginToken token, List<String> stillLockedIPs )throws Exception{
	  nrbServerInterface.setLockedIPs(token, stillLockedIPs );
  }
  
}
