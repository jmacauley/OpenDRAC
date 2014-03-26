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

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
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
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;

/**
 * This interface is very similar to the NrbInterface except that it takes a
 * UserDetails object in place of a LoginToken in most method calls.
 * <p>
 * Originally the NrbInterface used UserDetails but that was not secure, we
 * switched to a LoginToken on the interface, the code in NrbRemote validates
 * the LoginToken and calls the NRBserver with a UserDetails object in its place
 * or fails before a call is made.
 * <p>
 * As a result, only a valid logged in user can talk to the server by providing
 * a valid LoginToken.
 * 
 * @author pitman
 */
public interface NrbServerInterface {
	void activateService(UserDetails userDetails, String serviceId)
	    throws Exception;

	Map<String, Exception> activateServices(UserDetails userDetails,
	    String[] serviceIds) throws Exception;

	void addManualAdjacency(UserDetails userDetails, String sourceIEEE,
	    String sourcePort, String destIEEE, String destPort) throws Exception;

	void addSite(UserDetails userDetails, Site site) throws Exception;

	String asyncCreateSchedule(UserDetails userDetails, Schedule aSchedule)
	    throws Exception;

	List<AuditResult> auditModel(UserDetails userDetails) throws Exception;

	String auditUserProfileLinkages(UserDetails userDetails) throws Exception;

	void authenticate(ClientLoginType loginType, String user, char[] password,
	    String clientIp, String aSelectTicket) throws Exception;

	LoginToken authorize(ClientLoginType loginType, String user, char[] password,
	    String clientIp, String aSelectTicket) throws Exception;

	void cancelSchedule(UserDetails userDetails, String scheduleId)
	    throws Exception;

	Map<String, Exception> cancelSchedules(UserDetails userDetails,
	    String[] scheduleIds) throws Exception;

	void cancelService(UserDetails userDetails, String serviceId)
	    throws Exception;

	Map<String, Exception> cancelServices(UserDetails userDetails,
	    String[] serviceIds) throws Exception;

	boolean cancelTask(UserDetails userDetails, String taskId) throws Exception;

	void changeNetworkElementPassword(UserDetails userDetails,
	    NetworkElementHolder updatedNE) throws Exception;

	void changePassword(UserDetails userDetails, String oldpw, String newpw)
	    throws Exception;

	boolean clearTaskInfo(UserDetails userDetails, String taskId)
	    throws Exception;

	void confirmSchedule(UserDetails userDetails, String scheduleId)
	    throws Exception;

	Map<String, Exception> confirmSchedules(UserDetails userDetails,
	    String[] scheduleIds) throws Exception;

	void correctModel(UserDetails userDetails) throws Exception;

	void createResourceGroupProfile(UserDetails userDetails,
	    ResourceGroupProfile profile) throws Exception;

	ScheduleResult createSchedule(UserDetails userDetails,
	    Map<SPF_KEYS, String> parms, boolean queryOnly) throws Exception;

	String createService(UserDetails userDetails, String scheduleId,
	    DracService aService) throws Exception;

	void createUserGroupProfile(UserDetails userDetails, UserGroupProfile profile)
	    throws Exception;

	void createUserProfile(UserDetails userDetails, String userID,
	    AbstractCredential credential, AccountStatus status) throws Exception;

	void deleteCrossConnection(UserDetails userDetails, CrossConnection xcon)
	    throws Exception;

	void deleteFacility(UserDetails userDetails, String neId, String aid)
	    throws Exception;


	void deleteManualAdjacency(UserDetails userDetails, String neIEEE, String port)
	    throws Exception;

	void deleteManualAdjacency(UserDetails userDetails, String sourceIEEE,
	    String sourcePort, String destIEEE, String destPort) throws Exception;

	void deleteNetworkElement(UserDetails userDetails, NetworkElementHolder oldNe)
	    throws Exception;

	void deleteResourceGroupProfile(UserDetails userDetails,
	    String resourceGroupName) throws Exception;

	void deleteUserGroupProfile(UserDetails userDetails, UserGroupName name)
	    throws Exception;

	void deleteUserProfile(UserDetails userDetails, String userID)
	    throws Exception;

	void editEndPoint(UserDetails userDetails, EndPointType endPoint)
	    throws Exception;

	void editFacility(UserDetails userDetails, String neid, String aid,
	    String tna, String faclabel, String mtu, String srlg, String grp,
	    String cost, String metric2, String sigType, String constraints,
	    String domainId, String siteId) throws Exception;

	void enrollNetworkElement(UserDetails userDetails, NetworkElementHolder newNe)
	    throws Exception;

	List<Schedule> getActiveSchedules(UserDetails userDetails) throws Exception;

	List<String> getAlarms(UserDetails userDetails, Map<String, Object> filter)
	    throws Exception;

	List<NetworkElementHolder> getAllNetworkElements(UserDetails userDetails)
	    throws Exception;

	int getConfirmationTimeout(UserDetails userDetails) throws Exception;

	Resource getEndpointResource(UserDetails userDetails, String resourceID)
	    throws Exception;

	List<Facility> getFacilities(UserDetails userDetails, String neId)
	    throws Exception;

	Map<String, BigInteger> getFacilityConstraints(UserDetails userDetails,
	    Map<String, String> filter) throws Exception;

	GlobalPolicy getGlobalPolicy(UserDetails userDetails) throws Exception;

	GraphData getGraphData(UserDetails userDetails) throws Exception;

	ServerInfoType getInfo(UserDetails userDetails) throws Exception;

	List<String> getInprogressCalls(UserDetails userDetails) throws Exception;

	List<BandwidthRecord> getInternalBandwithUsage(UserDetails userDetails,
	    long startTime, long endTime) throws Exception;

	List<LogRecord> getLogs(UserDetails userDetails, long startTime,
	    long endTime, Map<String, String> filter) throws Exception;

	String getLpcpDiscoveryStatus(UserDetails userDetails) throws Exception;

	LpcpStatus getLpcpStatus(UserDetails userDetails) throws Exception;

	List<NetworkElementAdjacency> getNetworkElementAdjacencies(
	    UserDetails userDetails, Map<String, String> filter) throws Exception;

	List<CrossConnection> getNetworkElementConnections(UserDetails userDetails,
	    Map<String, String> filter) throws Exception;

	List<Facility> getNetworkElementFacilities(UserDetails userDetails,
	    Map<String, String> filter) throws Exception;

	List<NetworkElementHolder> getNetworkElements(UserDetails userDetails,
	    Map<String, String> filter) throws Exception;

	String getPeerIPAddress(UserDetails userDetails) throws Exception;

	List<String> getResourceGroupNameLineage(UserDetails userDetails,
	    ResourceGroupProfile rgProfile) throws Exception;

	ResourceGroupProfile getResourceGroupProfile(UserDetails userDetails,
	    String resourceGroupName) throws Exception;

	List<ResourceGroupProfile> getResourceGroupProfileList(UserDetails userDetails)
	    throws Exception;

	Schedule getSchedule(UserDetails userDetails, String scheduleId)
	    throws Exception;
	
	int extendServiceTime(UserDetails userDetails, DracService serviceToExtend, int minutesToExtendService)
		throws Exception;
	
	List<Schedule> getSchedules(UserDetails userDetails,
	    Map<String, String> filter) throws Exception;

	List<ServiceXml> getServices(UserDetails userDetails,
	    Map<String, Object> filter) throws Exception;

	List<String> getServicesEligibleForPurge(UserDetails userDetails,
	    Map<String, String> filter) throws Exception;

	List<ServiceXml> getServicesFromAID(UserDetails userDetails,
	    Map<String, String> filter) throws Exception;

	List<DracService> getServiceUsageForTNA(UserDetails userDetails, String tna)
	    throws Exception;

	String getSRLGListForService(UserDetails userDetails, String serviceId)
	    throws Exception;

	List<StatusType> getStatusInfo(UserDetails userDetails, String taskId)
	    throws Exception;

	SysMetricType getSystemMetric(UserDetails userDetails) throws Exception;

	List<TaskType> getTaskInfo(UserDetails userDetails) throws Exception;

	TaskType getTaskInfo(UserDetails userDetails, String taskId) throws Exception;

	UserDetails getUserDetails(LoginToken token) throws Exception;

	List<EndPointType> getUserEndpoints(UserDetails userDetails,
	    List<UserGroupName> userGroupFilter, Map<String, String> facilityFilter)
	    throws Exception;

	List<EndPointType> getUserEndpoints(UserDetails userDetails,
	    Map<String, String> facilityFilter) throws Exception;

	List<EndpointResourceUiType> getUserEndpointsUIInfo(UserDetails userDetails,
	    Map<String, String> facilityFilter) throws Exception;

	List<EndpointResourceUiType> getUserEndpointsUIInfo(UserDetails userDetails,
	    String resGroup, Map<String, String> facilityFilter) throws Exception;

	List<String> getUserGroupNameLineage(UserDetails userDetails,
	    UserGroupProfile ugProfile) throws Exception;

	UserGroupProfile getUserGroupProfile(UserDetails userDetails,
	    UserGroupName name) throws Exception;

	List<UserGroupProfile> getUserGroupProfileList(UserDetails userDetails)
	    throws Exception;

	List<List<String>> getUserNameLineage(UserDetails userDetails,
	    UserProfile userProfile) throws Exception;

	UserProfile getUserProfile(UserDetails userDetails, String userID)
	    throws Exception;

	List<UserProfile> getUserProfileList(UserDetails userDetails)
	    throws Exception;

	double getUtilization(UserDetails userDetails, String tnaName)
	    throws Exception;

	boolean isAllowed(UserDetails userDetails, PolicyRequest request)
	    throws Exception;

	boolean isAlive(UserDetails userDetails) throws Exception;

	boolean isResourceGroupEditable(UserDetails userDetails,
	    ResourceGroupProfile rgProfile) throws Exception;

	boolean isUserEditable(UserDetails userDetails, UserProfile userProfile)
	    throws Exception;

	boolean isUserGroupEditable(UserDetails userDetails,
	    UserGroupProfile ugProfile) throws Exception;

	String loadUserPreferences(UserDetails userDetails, String userId)
	    throws Exception;
	
	/**
	 * <p>
	 * Authenticate with the server. The ClientLoginType indicates how/why the
	 * client is logging in. The clientIP is the IP address of the end client (if
	 * possible to determine) and not necessarily the IP address of the code
	 * performing the login action. Use null if the clients IP address cannot be
	 * determined and we will log the IP address of the caller.
	 * <p>
	 * Unsuccessful logins will throw an exception.
	 * <p>
	 * Successful logins will return a login reference object that can be used to
	 * make other calls to the server.
	 * <p>
	 * 
	 * @throws Exception
	 */
	LoginToken login(ClientLoginType loginType, String user, char[] password,
	    String clientIp, String aSelectTicket, String sessionId) throws Exception;

	/**
	 * Log a user out of the server.
	 */
	void logout(LoginToken token) throws Exception;

	/**
	 * <p>
	 * Essentially the front end of 'login'. This authenticates with the server
	 * and returns the appropriate UserDetails, prior to issuing and caching a
	 * LoginToken.
	 * <p>
	 * Unsuccessful logins will throw an exception.
	 * <p>
	 * Successful logins will return an instance of UserDetails.
	 * <p>
	 * 
	 * @throws Exception
	 */
	UserDetails prepareLogin(ClientLoginType loginType, String user,
	    char[] password, String clientIp, String aSelectTicket, String sessionId) throws Exception;

	void purgeLogs(UserDetails userDetails, Map<String, String> data)
	    throws Exception;

	void purgeServices(UserDetails userDetails, List<String> serviceIds)
	    throws Exception;

	List<AlarmType> queryAllServiceAlarms(UserDetails userDetails,
	    long startTime, long endTime) throws Exception;

	List<EndPointType> queryAllUserEndpoints(UserDetails userDetails)
	    throws Exception;

	boolean queryPath(UserDetails userDetails, DracService aService)
	    throws Exception;

	Schedule queryScheduleByService(UserDetails userDetails, String serviceId)
	    throws Exception;
	
	List<Schedule> querySchedules(UserDetails userDetails, long startTime, long endTime, List<UserGroupName> groups,
            String name) throws Exception;
	
	AlarmType queryServiceAlarm(UserDetails userDetails, String alarmId)
	    throws Exception;

	List<DracService> queryServices(UserDetails userDetails, long startTime,
	    long endTime, List<UserGroupName> groups) throws Exception;

	UtilizationStructure queryUtilization(UserDetails userDetails, String tna,
	    double speed, long startTime, long endTime, int days, TimeZone tz)
	    throws Exception;

	void registerForLpcpEventNotifications(UserDetails userDetails,
	    LpcpEventCallback cb) throws Exception;

	List<Site> retrieveSiteList(UserDetails userDetails) throws Exception;

	void saveUserPreferences(UserDetails userDetails, String userId,
	    String xmlPreferences) throws Exception;

	/**
	 * Determines if a token is (still) valid. Not normally required as other
	 * calls to the server will validate the session and throw an exception if the
	 * session is not valid.
	 * <p>
	 * Throws an exception if the session is not valid.
	 */
	void sessionValidate(LoginToken token) throws Exception;

	void setConfirmationTimeout(UserDetails userDetails, int timeout)
	    throws Exception;

	void setDefaultGlobalPolicy(UserDetails userDetails, GlobalPolicy policy)
	    throws Exception;

	void setResourceGroupMembership(UserDetails userDetails,
	    String resourceGroupName, MembershipData membershipData) throws Exception;

	void setResourceGroupPolicy(UserDetails userDetails,
	    String resourceGroupName, ResourcePolicy resourcePolicy) throws Exception;

	void setResourceGroupResourceList(UserDetails userDetails,
	    String resourceGroupName, List<Resource> resource) throws Exception;

	void setScheduleOffset(UserDetails userDetails, int offset) throws Exception;

	void setupUserAuthenticationAccount(UserDetails userDetails, String userID,
	    AbstractCredential cred) throws Exception;

	void setUserAccountStatus(UserDetails userDetails, String userID,
	    AccountStatus status) throws Exception;

	void setUserAuthenticationData(UserDetails userDetails, String userID,
	    AuthenticationData authenticationData) throws Exception;

	void setUserGroupMembership(UserDetails userDetails,
	    UserGroupName userGroupName, MembershipData data) throws Exception;

	void setUserGroupUserGroupPolicy(UserDetails userDetails,
	    UserGroupName userGroupName, GroupPolicy groupPolicy) throws Exception;

	void setUserMembership(UserDetails userDetails, String userID,
	    MembershipData membership) throws Exception;

	void setUserOrganization(UserDetails userDetails, String userID,
	    OrganizationData orgData) throws Exception;

	void setUserPersonalData(UserDetails userDetails, String userID,
	    PersonalData personalData) throws Exception;

	void setUserTimeZoneIDPreference(UserDetails userDetails, String userID,
	    String localeValue) throws Exception;

	void toggleNetworkElementAssociation(UserDetails userDetails,
	    NetworkElementHolder existingNe) throws Exception;

	void updateSite(UserDetails userDetails, Site site) throws Exception;

	void updateNetworkElementPosition(UserDetails userDetails, String ip,
	    String port, Double positionX, Double positionY) throws Exception;

  void updateAddressAndPort(UserDetails userDetails, String oldAddress,
      int oldPort, String newAddress, int newPort) throws Exception;
  
  public void setLockedIPs(LoginToken token, List<String> stillLockedIPs )throws Exception;
  public void setLockedIPs(List<String> stillLockedIPs) throws Exception ;

}
