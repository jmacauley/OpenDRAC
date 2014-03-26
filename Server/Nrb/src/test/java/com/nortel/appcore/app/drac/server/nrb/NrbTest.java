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

import static org.junit.Assert.*;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.rmi.Naming;
import java.rmi.Remote;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;
import org.opendrac.launcher.RmiLauncher;
import org.opendrac.server.nrb.reporting.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

// See: -> 1191  TODO: FIXME: Remove this if....
//@Ignore("Doesn't work anymore after LoginAttemptsPolicy changes......")
public class NrbTest implements Remote {
  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * This is a do nothing delegate for testing the NRB_PORT infrastructure.
   * 
   * @author pitman
   */
  static class DelegateForJunitTesting implements NrbServerInterface, Remote,
      Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public void activateService(UserDetails userDetails, String serviceId)
        throws Exception {
      h(userDetails);
    }

    @Override
    public Map<String, Exception> activateServices(UserDetails userDetails,
        String[] serviceIds) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public void addManualAdjacency(UserDetails userDetails, String sourceIEEE,
        String sourcePort, String destIEEE, String destPort) throws Exception {
      h(userDetails);
    }

    @Override
    public void addSite(UserDetails userDetails, Site site) throws Exception {
      h(userDetails);
    }

    @Override
    public String asyncCreateSchedule(UserDetails userDetails,
        Schedule aSchedule) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<AuditResult> auditModel(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public String auditUserProfileLinkages(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public void cancelSchedule(UserDetails userDetails, String scheduleId)
        throws Exception {
      h(userDetails);
    }

    @Override
    public Map<String, Exception> cancelSchedules(UserDetails userDetails,
        String[] scheduleIds) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public void cancelService(UserDetails userDetails, String serviceId)
        throws Exception {
      h(userDetails);
    }

    @Override
    public Map<String, Exception> cancelServices(UserDetails userDetails,
        String[] serviceIds) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public boolean cancelTask(UserDetails userDetails, String taskId)
        throws Exception {
      h(userDetails);
      return false;
    }

    @Override
    public void changeNetworkElementPassword(UserDetails userDetails,
        NetworkElementHolder updatedNE) throws Exception {
      h(userDetails);
    }

    @Override
    public void changePassword(UserDetails userDetails, String oldpw,
        String newpw) throws Exception {
      h(userDetails);
    }

    @Override
    public boolean clearTaskInfo(UserDetails userDetails, String taskId)
        throws Exception {
      h(userDetails);
      return false;
    }

    @Override
    public void confirmSchedule(UserDetails userDetails, String scheduleId)
        throws Exception {
      h(userDetails);
    }

    @Override
    public Map<String, Exception> confirmSchedules(UserDetails userDetails,
        String[] scheduleIds) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public void correctModel(UserDetails userDetails) throws Exception {
      h(userDetails);
    }

    @Override
    public void createResourceGroupProfile(UserDetails userDetails,
        ResourceGroupProfile profile) throws Exception {
      h(userDetails);
    }

    @Override
    public ScheduleResult createSchedule(UserDetails userDetails,
        Map<SPF_KEYS, String> parms, boolean queryOnly) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public String createService(UserDetails userDetails, String scheduleId,
        DracService aService) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public void createUserGroupProfile(UserDetails userDetails,
        UserGroupProfile profile) throws Exception {
      h(userDetails);
    }

    @Override
    public void createUserProfile(UserDetails userDetails, String userID,
        AbstractCredential credential, AccountStatus status) throws Exception {
      h(userDetails);
    }

    @Override
    public void deleteCrossConnection(UserDetails userDetails,
        CrossConnection xcon) throws Exception {
      h(userDetails);
    }

    @Override
    public void deleteFacility(UserDetails userDetails, String neId, String aid)
        throws Exception {
      h(userDetails);
    }

    @Override
    public void deleteManualAdjacency(UserDetails userDetails, String neIEEE,
        String port) throws Exception {
      h(userDetails);
    }

    @Override
    public void deleteManualAdjacency(UserDetails userDetails,
        String sourceIEEE, String sourcePort, String destIEEE, String destPort)
        throws Exception {
      h(userDetails);
    }

    @Override
    public void deleteNetworkElement(UserDetails userDetails,
        NetworkElementHolder oldNe) throws Exception {
      h(userDetails);
    }

    @Override
    public void deleteResourceGroupProfile(UserDetails userDetails,
        String resourceGroupName) throws Exception {
      h(userDetails);
    }

    @Override
    public void deleteUserGroupProfile(UserDetails userDetails,
        UserGroupName name) throws Exception {
      h(userDetails);
    }

    @Override
    public void deleteUserProfile(UserDetails userDetails, String userID)
        throws Exception {
      h(userDetails);
    }

    @Override
    public void editEndPoint(UserDetails userDetails, EndPointType endPoint)
        throws Exception {
      h(userDetails);
    }

    @Override
    public void editFacility(UserDetails userDetails, String neid, String aid,
        String tna, String faclabel, String mtu, String srlg, String grp,
        String cost, String metric2, String sigType, String constraints,
        String domainId, String siteId) throws Exception {
      h(userDetails);
    }

    @Override
    public void enrollNetworkElement(UserDetails userDetails,
        NetworkElementHolder newNe) throws Exception {
      h(userDetails);
    }

    @Override
    public List<Schedule> getActiveSchedules(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<String> getAlarms(UserDetails userDetails,
        Map<String, Object> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<NetworkElementHolder> getAllNetworkElements(
        UserDetails userDetails) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public int getConfirmationTimeout(UserDetails userDetails) throws Exception {
      h(userDetails);
      return 0;
    }

    @Override
    public Resource getEndpointResource(UserDetails userDetails,
        String resourceID) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<Facility> getFacilities(UserDetails userDetails, String neId)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public Map<String, BigInteger> getFacilityConstraints(
        UserDetails userDetails, Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public GlobalPolicy getGlobalPolicy(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public GraphData getGraphData(UserDetails userDetails) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public ServerInfoType getInfo(UserDetails userDetails) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<String> getInprogressCalls(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<BandwidthRecord> getInternalBandwithUsage(
        UserDetails userDetails, long startTime, long endTime) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<LogRecord> getLogs(UserDetails userDetails, long startTime,
        long endTime, Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public String getLpcpDiscoveryStatus(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public LpcpStatus getLpcpStatus(UserDetails userDetails) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<NetworkElementAdjacency> getNetworkElementAdjacencies(
        UserDetails userDetails, Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<CrossConnection> getNetworkElementConnections(
        UserDetails userDetails, Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<Facility> getNetworkElementFacilities(UserDetails userDetails,
        Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<NetworkElementHolder> getNetworkElements(
        UserDetails userDetails, Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public String getPeerIPAddress(UserDetails userDetails) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<String> getResourceGroupNameLineage(UserDetails userDetails,
        ResourceGroupProfile rgProfile) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public ResourceGroupProfile getResourceGroupProfile(
        UserDetails userDetails, String resourceGroupName) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<ResourceGroupProfile> getResourceGroupProfileList(
        UserDetails userDetails) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public Schedule getSchedule(UserDetails userDetails, String scheduleId)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<Schedule> getSchedules(UserDetails userDetails,
        Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<ServiceXml> getServices(UserDetails userDetails,
        Map<String, Object> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<String> getServicesEligibleForPurge(UserDetails userDetails,
        Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<ServiceXml> getServicesFromAID(UserDetails userDetails,
        Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<DracService> getServiceUsageForTNA(UserDetails userDetails,
        String tna) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public String getSRLGListForService(UserDetails userDetails,
        String serviceId) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<StatusType> getStatusInfo(UserDetails userDetails, String taskId)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public SysMetricType getSystemMetric(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<TaskType> getTaskInfo(UserDetails userDetails) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public TaskType getTaskInfo(UserDetails userDetails, String taskId)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public UserDetails getUserDetails(LoginToken token) throws Exception {
      /**
       * SPECIAL CASE
       */
      if (token.equals(loginToken1)) {
        return userDetails1;
      }
      if (token.equals(loginToken2)) {
        return userDetails2;
      }
      h(token);
      return null;
    }

    @Override
    public List<EndPointType> getUserEndpoints(UserDetails userDetails,
        List<UserGroupName> userGroupFilter, Map<String, String> filter)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<EndPointType> getUserEndpoints(UserDetails userDetails,
        Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<EndpointResourceUiType> getUserEndpointsUIInfo(
        UserDetails userDetails, Map<String, String> filter) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<EndpointResourceUiType> getUserEndpointsUIInfo(
        UserDetails userDetails, String resGroup, Map<String, String> filter)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<String> getUserGroupNameLineage(UserDetails userDetails,
        UserGroupProfile ugProfile) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public UserGroupProfile getUserGroupProfile(UserDetails userDetails,
        UserGroupName name) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<UserGroupProfile> getUserGroupProfileList(
        UserDetails userDetails) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<List<String>> getUserNameLineage(UserDetails userDetails,
        UserProfile userProfile) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public UserProfile getUserProfile(UserDetails userDetails, String userID)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<UserProfile> getUserProfileList(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public double getUtilization(UserDetails userDetails, String tnaName)
        throws Exception {
      h(userDetails);
      return 0;
    }

    @Override
    public boolean isAllowed(UserDetails userDetails, PolicyRequest request)
        throws Exception {
      h(userDetails);
      return false;
    }

    @Override
    public boolean isAlive(UserDetails userDetails) throws Exception {
      h(userDetails);
      return false;
    }

    @Override
    public boolean isResourceGroupEditable(UserDetails userDetails,
        ResourceGroupProfile rgProfile) throws Exception {
      h(userDetails);
      return false;
    }

    @Override
    public boolean isUserEditable(UserDetails userDetails,
        UserProfile userProfile) throws Exception {
      h(userDetails);
      return false;
    }

    @Override
    public boolean isUserGroupEditable(UserDetails userDetails,
        UserGroupProfile ugProfile) throws Exception {
      h(userDetails);
      return false;
    }

    @Override
    public String loadUserPreferences(UserDetails userDetails, String userId)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public LoginToken login(ClientLoginType loginType, String user,
        char[] password, String clientIp, String aSelectTicket, String sessionId)
        throws Exception {
      if (ClientLoginType.ADMIN_CONSOLE_LOGIN == loginType) {
        h(new LoginToken("0"));
      }
      else {
        h(new LoginToken("1"));
      }

      return null;
    }

    @Override
    public void logout(LoginToken token) throws Exception {
      h(token);
    }

    @Override
    public UserDetails prepareLogin(ClientLoginType loginType, String user,
        char[] password, String clientIp, String aSelectTicket, String sessionId)
        throws Exception {
      if (ClientLoginType.ADMIN_CONSOLE_LOGIN == loginType) {
        h(new LoginToken("0"));
      }
      else {
        h(new LoginToken("1"));
      }

      return null;
    }

    @Override
    public void purgeLogs(UserDetails userDetails, Map<String, String> data)
        throws Exception {
      h(userDetails);
    }

    @Override
    public void purgeServices(UserDetails userDetails, List<String> serviceIds)
        throws Exception {
      h(userDetails);
    }

    @Override
    public List<AlarmType> queryAllServiceAlarms(UserDetails userDetails,
        long startTime, long endTime) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<EndPointType> queryAllUserEndpoints(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public boolean queryPath(UserDetails userDetails, DracService aService)
        throws Exception {
      h(userDetails);
      return false;
    }

    @Override
    public Schedule queryScheduleByService(UserDetails userDetails,
        String serviceId) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<Schedule> querySchedules(UserDetails userDetails,
        long startTime, long endTime, List<UserGroupName> groups, String name)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public AlarmType queryServiceAlarm(UserDetails userDetails, String alarmId)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public List<DracService> queryServices(UserDetails userDetails,
        long startTime, long endTime, List<UserGroupName> groups)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public UtilizationStructure queryUtilization(UserDetails userDetails,
        String tna, double speed, long startTime, long endTime, int days,
        TimeZone tz) throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public void registerForLpcpEventNotifications(UserDetails userDetails,
        LpcpEventCallback cb) throws Exception {
      h(userDetails);
    }

    @Override
    public List<Site> retrieveSiteList(UserDetails userDetails)
        throws Exception {
      h(userDetails);
      return null;
    }

    @Override
    public void saveUserPreferences(UserDetails userDetails, String userId,
        String xmlPreferences) throws Exception {
      h(userDetails);
    }

    @Override
    public void sessionValidate(LoginToken token) throws Exception {
      h(token);
    }

    @Override
    public void setConfirmationTimeout(UserDetails userDetails, int timeout)
        throws Exception {
      h(userDetails);
    }

    @Override
    public void setDefaultGlobalPolicy(UserDetails userDetails,
        GlobalPolicy policy) throws Exception {
      h(userDetails);
    }

    @Override
    public void setResourceGroupMembership(UserDetails userDetails,
        String resourceGroupName, MembershipData membershipData)
        throws Exception {
      h(userDetails);
    }

    @Override
    public void setResourceGroupPolicy(UserDetails userDetails,
        String resourceGroupName, ResourcePolicy resourcePolicy)
        throws Exception {
      h(userDetails);
    }

    @Override
    public void setResourceGroupResourceList(UserDetails userDetails,
        String resourceGroupName, List<Resource> resource) throws Exception {
      h(userDetails);
    }

    @Override
    public void setScheduleOffset(UserDetails userDetails, int offset)
        throws Exception {
      h(userDetails);
    }

    @Override
    public void setupUserAuthenticationAccount(UserDetails userDetails,
        String userID, AbstractCredential cred) throws Exception {
      h(userDetails);
    }

    @Override
    public void setUserAccountStatus(UserDetails userDetails, String userID,
        AccountStatus status) throws Exception {
      h(userDetails);
    }

    @Override
    public void setUserAuthenticationData(UserDetails userDetails,
        String userID, AuthenticationData authenticationData) throws Exception {
      h(userDetails);
    }

    @Override
    public void setUserGroupMembership(UserDetails userDetails,
        UserGroupName userGroupName, MembershipData data) throws Exception {
      h(userDetails);
    }

    @Override
    public void setUserGroupUserGroupPolicy(UserDetails userDetails,
        UserGroupName userGroupName, GroupPolicy groupPolicy) throws Exception {
      h(userDetails);
    }

    @Override
    public void setUserMembership(UserDetails userDetails, String userID,
        MembershipData membership) throws Exception {
      h(userDetails);
    }

    @Override
    public void setUserOrganization(UserDetails userDetails, String userID,
        OrganizationData orgData) throws Exception {
      h(userDetails);
    }

    @Override
    public void setUserPersonalData(UserDetails userDetails, String userID,
        PersonalData personalData) throws Exception {
      h(userDetails);
    }

    @Override
    public void setUserTimeZoneIDPreference(UserDetails userDetails,
        String userID, String localeValue) throws Exception {
      h(userDetails);
    }

    @Override
    public void toggleNetworkElementAssociation(UserDetails userDetails,
        NetworkElementHolder existingNe) throws Exception {
      h(userDetails);
    }

    @Override
    public void updateSite(UserDetails userDetails, Site site) throws Exception {
      h(userDetails);
    }

    @Override
    public void updateNetworkElementPosition(UserDetails userDetails,
        String ip, String port, Double positionX, Double positionY)
        throws Exception {
      h(userDetails);
    }

    private void h(LoginToken tok) throws Exception {
      if (tok != null) {
        if ("1".equals(tok.getUser())) {
          throw new Exception(
              "This is expected, we are testing the failure path");
        }
      }
    }

    /**
     * Helper that either does nothing or throws an exception, used to get code
     * coverage of the success and failure paths of the NrbRemote class.
     */
    private void h(UserDetails u) throws Exception {
      if (u != null) {
        if ("exception".equals(u.getUserID())) {
          throw new Exception(
              "This is expected, we are testing the failure path");
        }
      }
    }

    @Override
    public void authenticate(ClientLoginType loginType, String user,
        char[] password, String clientIp, String aSelectTicket)
        throws Exception {
      if (ClientLoginType.ADMIN_CONSOLE_LOGIN == loginType) {
        h(new LoginToken("0"));
      }
      else {
        h(new LoginToken("1"));
      }
    }

    @Override
    public LoginToken authorize(ClientLoginType loginType, String user,
        char[] password, String clientIp, String aSelectTicket)
        throws Exception {
      LoginToken token;
      if (ClientLoginType.ADMIN_CONSOLE_LOGIN == loginType) {
        token = new LoginToken("0");
      }
      else {
        token = new LoginToken("1");
      }
      h(token);
      return token;
    }

    @Override
    public int extendServiceTime(UserDetails userDetails,
        DracService serviceToExtend, int minutesToExtendService)
        throws Exception {
      return 0;
    }

    @Override
    public void updateAddressAndPort(UserDetails userDetails,
        final String oldAddress, final int oldPort, final String newAddress,
        final int newPort) throws Exception {
      h(userDetails);
    }

	@Override
    public void setLockedIPs(LoginToken token, List<String> stillLockedIPs) throws Exception {
		h(token);
	    
    }
	@Override
    public void setLockedIPs(List<String> stillLockedIPs) throws Exception {
	    
    }

  }

  private static UserDetails userDetails1 = new UserDetails("admin", "admin",
      null);
  private static UserDetails userDetails2 = new UserDetails("exception",
      "exception", null);
  private static LoginToken loginToken1 = new LoginToken("0");
  private static LoginToken loginToken2 = new LoginToken("1");

  @Test
  public void testNRB() throws Exception {
    final int rmiRandomPort = 0;
    RmiLauncher rmiLauncher = new RmiLauncher();
    rmiLauncher.start();

    final NrbRemote nrb = new NrbRemote(rmiRandomPort,
        new DelegateForJunitTesting());
    Naming.rebind("testNRB", nrb);
    NrbInterface n = (NrbInterface) Naming.lookup("testNRB");
    assertNotNull(n);
    // bogus but good enough

    // Use reflection to call all of the methods in the NrbInterface remote
    // class.
    int ok = 0;
    for (Method m : n.getClass().getDeclaredMethods()) {
      if (m.getReturnType().isAssignableFrom(Report.class)
          || m.getReturnType().isAssignableFrom(File.class)) {
        continue;
      }
      if (m.getReturnType().isAssignableFrom(List.class)) {
        if (m.getName().indexOf("Report") >= rmiRandomPort
            || m.getName().indexOf("getFilterGroupsForCurrentUser") >= rmiRandomPort) {
          continue;
        }
      }
      if (!Modifier.isPublic(m.getModifiers())
          || Modifier.isStatic(m.getModifiers())) {
        log.debug("Skipping method " + m.getName() + " " + ok);
        continue;
      }

      if ("getUserDetails".equals(m.getName())) {
        log.debug("skipping " + m.getName()
            + " we use it for testing internally");
        continue;
      }
      if ("extendServiceTime".equals(m.getName())) {
        log.debug("skipping " + m.getName()
            + " we use it for testing internally");
        continue;
      }

      // Type[] argTypes = m.getGenericParameterTypes();
      Class<?>[] argTypes = m.getParameterTypes();
      Object[] args = new Object[argTypes.length];

      for (int i = 1; i < args.length; i++) {
        if (argTypes[i].isPrimitive()) {
          if (argTypes[i] == Integer.TYPE) {
            args[i] = Integer.valueOf(rmiRandomPort);
          }
          else if (argTypes[i] == Long.TYPE) {
            args[i] = Long.valueOf(rmiRandomPort);
          }
          else if (argTypes[i] == Boolean.TYPE) {
            args[i] = Boolean.FALSE;
          }
          else if (argTypes[i] == Double.TYPE) {
            args[i] = Double.valueOf(rmiRandomPort);
          }
          else {
            fail("Unsupported type, enhance test case for primitive ");
          }
        }
      }

      log.debug("Invoking method " + m.getName() + " with types "
          + Arrays.asList(argTypes));
      /*
       * Pass in a system level user details, expect the method to return
       * without throwing an exception.
       */
      if (m.getName().equals("equals") || argTypes.length == 0) {
        continue;
      }
      else if (argTypes[rmiRandomPort].equals(UserDetails.class)) {
        args[rmiRandomPort] = userDetails1;
      }
      else if (argTypes[rmiRandomPort].equals(LoginToken.class)) {
        args[rmiRandomPort] = loginToken1;
      }
      else if (argTypes[rmiRandomPort].equals(ClientLoginType.class)) {
        // special case for login parm 1 is not a token
        args[rmiRandomPort] = ClientLoginType.ADMIN_CONSOLE_LOGIN;
      }
      else {

        fail("Unsupported paramater number 0! " + argTypes[rmiRandomPort]
            + "class  m: " + m.getClass().getName() + " method " + m.getName());
      }
      m.invoke(n, args);

      /*
       * Pass in a user details object with a user name set to exception, expect
       * the method to throw an exception, fail if it does not.
       */

      if (argTypes[rmiRandomPort].equals(UserDetails.class)) {
        args[rmiRandomPort] = userDetails2;
      }
      else if (argTypes[rmiRandomPort].equals(LoginToken.class)) {
        args[rmiRandomPort] = loginToken2;
      }
      else if (argTypes[rmiRandomPort].equals(ClientLoginType.class)) {
        // special case for login parm 1 is not a token
        args[rmiRandomPort] = null;
      }
      else {
        fail("Unsupported paramater number 0! " + argTypes[rmiRandomPort]);
      }

      try {
      	// TODO: FIXME: Remove this if....
      	//if(!m.getName().equals("setLockedIPs")){
      		m.invoke(n, args);
        fail("exepected method " + m.getName()
            + " to throw an exception in this test mode!");
      	//}
      }
      catch (Exception e) {
        // good we wanted an exception, increment ok just to shut up
        // the
        // compiler warning.
        ok++;

      }
    }

    assertFalse(n.isAlive(LoginToken.getStaticToken()));
    rmiLauncher.stop();
  }
}
