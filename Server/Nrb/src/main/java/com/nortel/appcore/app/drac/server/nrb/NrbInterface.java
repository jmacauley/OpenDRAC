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
import java.rmi.Remote;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.opendrac.server.nrb.reporting.Report;

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

public interface NrbInterface extends Remote {
  
  
  
  void activateService(LoginToken token, String serviceId) throws Exception;

  Map<String, Exception> activateServices(LoginToken token, String[] serviceIds)
      throws Exception;

  /**
   * Creates a manual topological link between two NEs identified by their IEEE
   * address and port identifiers.
   */
  void addManualAdjacency(LoginToken token, String sourceIEEE,
      String sourcePort, String destIEEE, String destPort) throws Exception;

  void addSite(LoginToken token, Site site) throws Exception;

  String asyncCreateSchedule(LoginToken token, Schedule aSchedule)
      throws Exception;

  String auditUserProfileLinkages(LoginToken token) throws Exception;

  /**
   * The authenticate method, similar to log in, checks the user's credentials
   * but does not actually log them in or apply restrictions and other
   * authorization policies.
   * 
   * @param loginType
   * @param user
   * @param password
   * @param clientIp
   * @param aSelectTicket
   * @throws Exception
   */
  void authenticate(ClientLoginType loginType, String user, char[] password,
      String clientIp, String aSelectTicket) throws Exception;

  LoginToken authorize(ClientLoginType loginType, String user, char[] password,
      String clientIp, String aSelectTicket) throws Exception;

  void cancelSchedule(LoginToken token, String scheduleId) throws Exception;

  Map<String, Exception> cancelSchedules(LoginToken token, String[] scheduleIds)
      throws Exception;

  void cancelService(LoginToken token, String serviceId) throws Exception;

  Map<String, Exception> cancelServices(LoginToken token, String[] serviceIds)
      throws Exception;

  boolean cancelTask(LoginToken token, String taskId) throws Exception;

  /**
   * changeNetworkElementPassword, update the credentials used to communicate
   * with the device.
   */
  void changeNetworkElementPassword(LoginToken token,
      NetworkElementHolder updatedNE) throws Exception;

  void changePassword(LoginToken token, String userId, String oldpw,
      String newpw, ClientLoginType loginType, String clientIp, String sessionId)
      throws Exception;

  boolean clearTaskInfo(LoginToken token, String taskId) throws Exception;

  void confirmSchedule(LoginToken token, String scheduleId) throws Exception;

  Map<String, Exception> confirmSchedules(LoginToken token, String[] scheduleIds)
      throws Exception;

  void createResourceGroupProfile(LoginToken token, ResourceGroupProfile profile)
      throws Exception;

  /**
   * Used by the admin console to create/query a schedule, this method returns
   * an xml string that contains the proposed route which is then displayed to
   * the user when the query operation is selected @TODO merge and combine with
   * the asyncCreateSchedule method
   */
  ScheduleResult createSchedule(LoginToken token, Map<SPF_KEYS, String> parms,
      boolean queryOnly) throws Exception;

  String createService(LoginToken token, String scheduleId, DracService aService)
      throws Exception;

  void createUserGroupProfile(LoginToken token, UserGroupProfile profile)
      throws Exception;

  void createUserProfile(LoginToken token, String userID,
      AbstractCredential credential, AccountStatus status) throws Exception;

  /**
   * Delete an existing cross connection in the network, discovered and reported
   * via the audit, not for general use.
   */
  void deleteCrossConnection(LoginToken token, CrossConnection xcon)
      throws Exception;

  void deleteFacility(LoginToken token, String neId, String aid)
      throws Exception;

  void deleteManualAdjacency(LoginToken token, String neIEEE, String port)
      throws Exception;

  /**
   * Creates a manual topological link between two NEs identified by their IEEE
   * address and port identifiers.
   */
  void deleteManualAdjacency(LoginToken token, String sourceIEEE,
      String sourcePort, String destIEEE, String destPort) throws Exception;

  /**
   * Delete or remove a network element from DRACs control.
   */
  void deleteNetworkElement(LoginToken token, NetworkElementHolder oldNe)
      throws Exception;

  void deleteResourceGroupProfile(LoginToken token, String resourceGroupName)
      throws Exception;

  void deleteUserGroupProfile(LoginToken token, UserGroupName name)
      throws Exception;

  void deleteUserProfile(LoginToken token, String userID) throws Exception;

  void editEndPoint(LoginToken token, EndPointType endPoint) throws Exception;

  /**
   * Create or enroll a new network element. Not all of the fields in the
   * NetworkElement holder need to be filled out with the correct values.
   */
  void enrollNetworkElement(LoginToken token, NetworkElementHolder newNe)
      throws Exception;

  List<Schedule> getActiveSchedules(LoginToken token) throws Exception;

  List<String> getAlarms(LoginToken token, Map<String, Object> filter)
      throws Exception;

  List<NetworkElementHolder> getAllNetworkElements(LoginToken token)
      throws Exception;

  int getConfirmationTimeout(LoginToken token) throws Exception;

  Resource getEndpointResource(LoginToken token, String resourceID)
      throws Exception;

  Map<String, BigInteger> getFacilityConstraints(LoginToken token,
      Map<String, String> filter) throws Exception;

  GlobalPolicy getGlobalPolicy(LoginToken token) throws Exception;

  public List<Report> getAggregatedReportForGui(int reportId,
      String titleLeader, Date startDate, Date endDate,
      Map<String, String> filter, LoginToken token) throws Exception;

  public Report getQualitativeReport(Date startDate, Date endDate,
      LoginToken token) throws Exception;
  
  public Report getQualitativeReportByPort(Date startDate, Date endDate,
	      LoginToken token) throws Exception;
  
  public Report getScheduledServicesReport(Date startDate, Date endDate,
      LoginToken token) throws Exception;

  public Report getActualschedulesReport(Date startDate, Date endDate,
      LoginToken token) throws Exception;

  public File getAggregatedReportAsFile(int reportId, String titleLeader,
      Date startDate, Date endDate, Map<String, String> filter, LoginToken token)
      throws Exception;

  public Report getAggregatedReport(int reportId, String titleLeader,
      Date startDate, Date endDate, Map<String, String> filter, LoginToken token)
      throws Exception;

  public Report getAggregatedReportFull(int reportId, String titleLeader,
      Date startDate, Date endDate, Map<String, String> filter, LoginToken token)
      throws Exception;

  /**
   * Return LPCP_PORT status along with VM parms and other info, displayed in admin
   * console main screen.
   */

  ServerInfoType getInfo(LoginToken token) throws Exception;

  List<String> getInprogressCalls(LoginToken token) throws Exception;

  List<LogRecord> getLogs(LoginToken token, long startTime, long endTime,
      Map<String, String> filter) throws Exception;

  LpcpStatus getLpcpStatus(LoginToken token) throws Exception;

  List<NetworkElementAdjacency> getNetworkElementAdjacencies(LoginToken token,
      Map<String, String> filter) throws Exception;

  List<CrossConnection> getNetworkElementConnections(LoginToken token,
      Map<String, String> filter) throws Exception;

  List<Facility> getNetworkElementFacilities(LoginToken token,
      Map<String, String> filter) throws Exception;

  List<NetworkElementHolder> getNetworkElements(LoginToken token,
      Map<String, String> filter) throws Exception;

  List<String> getResourceGroupNameLineage(LoginToken token,
      ResourceGroupProfile rgProfile) throws Exception;

  ResourceGroupProfile getResourceGroupProfile(LoginToken token,
      String resourceGroupName) throws Exception;

  List<ResourceGroupProfile> getResourceGroupProfileList(LoginToken token)
      throws Exception;

  Schedule getSchedule(LoginToken token, String scheduleId) throws Exception;

  int extendServiceTime(LoginToken token, DracService serviceToExtend, int minutesToExtendService)throws Exception;
  
  List<Schedule> getSchedules(LoginToken token, Map<String, String> filter)
      throws Exception;

  List<ServiceXml> getServices(LoginToken token, Map<String, Object> filter)
      throws Exception;

  List<String> getServicesEligibleForPurge(LoginToken token,
      Map<String, String> filter) throws Exception;

  List<ServiceXml> getServicesFromAID(LoginToken token,
      Map<String, String> filter) throws Exception;

  List<DracService> getServiceUsageForTNA(LoginToken token, String tna)
      throws Exception;

  String getSRLGListForService(LoginToken token, String serviceId)
      throws Exception;

  List<StatusType> getStatusInfo(LoginToken token, String taskId)
      throws Exception;

  SysMetricType getSystemMetric(LoginToken token) throws Exception;

  List<TaskType> getTaskInfo(LoginToken token) throws Exception;

  TaskType getTaskInfo(LoginToken token, String taskId) throws Exception;

  /**
   * Given a loginToken return the userDetails object corresponding to this, or
   * toss an exception if the token is invalid or the user no longer logged in.
   * This method is called internally on each method call in the NRB_PORT interface
   * to map the LoginToken to a userDetails object, and must return quickly.
   */
  UserDetails getUserDetails(LoginToken token) throws Exception;

  List<EndPointType> getUserEndpoints(LoginToken token,
      List<UserGroupName> userGroupFilter, Map<String, String> facilityFilter)
      throws Exception;

  List<EndPointType> getUserEndpoints(LoginToken token,
      Map<String, String> facilityFilter) throws Exception;

  List<EndpointResourceUiType> getUserEndpointsUIInfo(LoginToken token,
      Map<String, String> facilityFilter) throws Exception;

  List<EndpointResourceUiType> getUserEndpointsUIInfo(LoginToken token,
      String resGroup, Map<String, String> facilityFilter) throws Exception;

  List<String> getUserGroupNameLineage(LoginToken token,
      UserGroupProfile ugProfile) throws Exception;

  UserGroupProfile getUserGroupProfile(LoginToken token, UserGroupName name)
      throws Exception;

  List<UserGroupProfile> getUserGroupProfileList(LoginToken token)
      throws Exception;

  List<List<String>> getUserNameLineage(LoginToken token,
      UserProfile userProfile) throws Exception;

  UserProfile getUserProfile(LoginToken token, String userID) throws Exception;

  List<UserProfile> getUserProfileList(LoginToken token) throws Exception;

  double getUtilization(LoginToken token, String tnaName) throws Exception;

  /**
   * @deprecated This should be done only on the server side
   */
  @Deprecated
  boolean isAllowed(LoginToken token, PolicyRequest request) throws Exception;

  boolean isAlive(LoginToken token) throws Exception;

  boolean isResourceGroupEditable(LoginToken token,
      ResourceGroupProfile rgProfile) throws Exception;

  boolean isUserEditable(LoginToken token, UserProfile userProfile)
      throws Exception;

  boolean isUserGroupEditable(LoginToken token, UserGroupProfile ugProfile)
      throws Exception;

  String loadUserPreferences(LoginToken token, String userId) throws Exception;
  
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

  void purgeServices(LoginToken token, List<String> serviceIds)
      throws Exception;

  List<AlarmType> queryAllServiceAlarms(LoginToken token, long startTime,
      long endTime) throws Exception;

  List<EndPointType> queryAllUserEndpoints(LoginToken token) throws Exception;

  boolean queryPath(LoginToken token, DracService aService) throws Exception;

  Schedule queryScheduleByService(LoginToken token, String serviceId)
      throws Exception;

  List<Schedule> querySchedules(LoginToken token, long startTime, long endTime)
      throws Exception;

  List<Schedule> querySchedules(LoginToken token, long startTime, long endTime,
      List<UserGroupName> groups, String name) throws Exception;

  List<Schedule> querySchedules(LoginToken token, long startTime, long endTime,
      String name) throws Exception;

  List<Schedule> querySchedules(LoginToken token, long startTime, long endTime,
      List<UserGroupName> groups) throws Exception;

  AlarmType queryServiceAlarm(LoginToken token, String alarmId)
      throws Exception;

  List<DracService> queryServices(LoginToken token, long startTime,
      long endTime, List<UserGroupName> groups) throws Exception;

  UtilizationStructure queryUtilization(LoginToken token, String tna,
      double speed, long startTime, long endTime, int days, TimeZone tz)
      throws Exception;

  /**
   * Clients can register for event notifications or callbacks.
   */
  void registerForLpcpEventNotifications(LoginToken token, LpcpEventCallback cb)
      throws Exception;

  List<Site> retrieveSiteList(LoginToken token) throws Exception;

  void saveUserPreferences(LoginToken token, String userId,
      String xmlPreferences) throws Exception;

  /**
   * Determines if a token is (still) valid. Not normally required as other
   * calls to the server will validate the session and throw an exception if the
   * session is not valid.
   * <p>
   * Throws an exception if the session is not valid.
   */
  void sessionValidate(LoginToken token) throws Exception;

  void setConfirmationTimeout(LoginToken token, int timeout) throws Exception;

  void setDefaultGlobalPolicy(LoginToken token, GlobalPolicy policy)
      throws Exception;

  void setResourceGroupMembership(LoginToken token, String resourceGroupName,
      MembershipData membershipData) throws Exception;

  void setResourceGroupPolicy(LoginToken token, String resourceGroupName,
      ResourcePolicy resourcePolicy) throws Exception;

  void setResourceGroupResourceList(LoginToken token, String resourceGroupName,
      List<Resource> resource) throws Exception;

  void setScheduleOffset(LoginToken token, int offset) throws Exception;

  void setupUserAuthenticationAccount(LoginToken token, String userID,
      AbstractCredential cred) throws Exception;

  void setUserAccountStatus(LoginToken token, String userID,
      AccountStatus status) throws Exception;

  void setUserAuthenticationData(LoginToken token, String userID,
      AuthenticationData authenticationData) throws Exception;

  void setUserGroupMembership(LoginToken token, UserGroupName userGroupName,
      MembershipData data) throws Exception;

  void setUserGroupUserGroupPolicy(LoginToken token,
      UserGroupName userGroupName, GroupPolicy groupPolicy) throws Exception;

  void setUserMembership(LoginToken token, String userID,
      MembershipData membership) throws Exception;

  void setUserOrganization(LoginToken token, String userID,
      OrganizationData orgData) throws Exception;

  void setUserPersonalData(LoginToken token, String userID,
      PersonalData personalData) throws Exception;

  void setUserTimeZoneIDPreference(LoginToken token, String userID,
      String localeValue) throws Exception;

  /**
   * Toggle the association (connection) between drac and the network element.
   * In the case of a TL1 based Ne, we just close the socket to the NE and let
   * the mediation layer reconnect. For SNMP and other devices this method
   * should trigger the mediation layer to reload or re-align its view of the
   * device.
   * <p>
   * This method will return after triggering the toggle, but not necessarily
   * after the refresh is complete.
   * <p>
   */
  void toggleNetworkElementAssociation(LoginToken token,
      NetworkElementHolder existingNe) throws Exception;

  void updateSite(LoginToken token, Site site) throws Exception;

  void updateNetworkElementPosition(LoginToken token, String ip, String port,
      Double positionX, Double positionY) throws Exception;


  // From Improved NRB_PORT interface
  /**
   * Methods from ServerOperation to add:
   * <p>
   * cancelService
   * <p>
   * queryutilisation
   * <p>
   * querysched
   * <p>
   */

  /**
   * Audits the DRAC LPCP_PORT network model, mostly just compares the bandwidth
   * trackers in the model with trackers populated with known connections in the
   * network.
   */
  List<AuditResult> auditModel(LoginToken token) throws Exception;

  /**
   * Typically called after auditModel, forces the trackers to the "correct"
   * state. Use with caution, consider removing this method! The network model
   * will be recreated after a DRAC restart so this "correction" is temporary at
   * best.
   */
  void correctModel(LoginToken token) throws Exception;

  /**
   * Edit selected attributes of a facility or endpoint. Should really take a
   * EndpointType or facility object to be more generic and such that it evolves
   * as those classes evolve. In particular a Facility object can carry around
   * arbitrary name/value pairs which we might want to edit. This message is
   * fist sent to LPCP_PORT so that it can update is internal model then to NeProxy
   * which will update the device and or database. A better design would have
   * been to send it to NeProxy and fire an event to Lpcp so that it updates its
   * model.
   */
  void editFacility(LoginToken token, String neid, String aid, String tna,
      String faclabel, String mtu, String srlg, String grp, String cost,
      String metric2, String sigType, String constraints, String domainId,
      String siteId) throws Exception;

  /**
   * A less efficient form of get Facilities, go all the way down to NeProxy to
   * pull the facility records out of the database. Returns a different XML
   * format than the other method.
   */
  List<Facility> getFacilities(LoginToken token, String neId) throws Exception;

  /**
   * Retrieve the graph built and maintained by the LPCP_PORT routing engine. This
   * will be used for the topology display in the admin console.
   */
  GraphData getGraphData(LoginToken token) throws Exception;

  /**
   * Return the bandwidth usage of the internal links in the network for the
   * adminConsole and administrator monitoring of the networks internal links
   */
  List<BandwidthRecord> getInternalBandwithUsage(LoginToken token,
      long startTime, long endTime) throws Exception;

  /**
   * Returns a XML string giving debug/status info about the Lpcp Discovery
   * manager. Remove?
   */
  String getLpcpDiscoveryStatus(LoginToken token) throws Exception;

  /**
   * Return the peer (redundant DRAC's) IP address. Currently used for display
   * only.
   */
  String getPeerIPAddress(LoginToken token) throws Exception;

  /**
   * Retrieve a service for a schedule that is currently active
   * @param token
   * @param scheduleId
   * @return
   * @throws Exception
   */
  DracService getCurrentlyActiveServiceByScheduleId(LoginToken token, String scheduleId) throws Exception ;


  void updateAddressAndPort(LoginToken token, String oldAddress, int oldPort,
      String newAddress, int newPort) throws Exception;
  
  public void setLockedIPs(LoginToken token, List<String> stillLockedIPs )throws Exception ;

}
