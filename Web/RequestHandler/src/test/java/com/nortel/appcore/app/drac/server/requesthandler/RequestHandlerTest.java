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

package com.nortel.appcore.app.drac.server.requesthandler;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracPasswordEvaluationException;
import com.nortel.appcore.app.drac.common.security.authentication.types.LocalAccountCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.PasswordErrorCode;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AccountState;
import com.nortel.appcore.app.drac.common.types.AbstractMessageType.MessageBox;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.SentMessageType;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.database.dracdb.DbResourceGroupProfile;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.IntegrationTestHelper;

//@Ignore("Currently broken")
public class RequestHandlerTest {
  private static final Logger log = LoggerFactory
      .getLogger(RequestHandlerTest.class);
  private static RequestHandler rh;
  // private static UserDetails userDetails;
  private static LoginToken token;

  @BeforeClass
  public static void setupBefore() throws Exception {
    TestHelper.INSTANCE.initialize();
    IntegrationTestHelper.setUpServerForIntergrationTests(false, null);
    rh = RequestHandler.INSTANCE;
    token = rh.login(ClientLoginType.INTERNAL_LOGIN, "admin",
        "myDrac".toCharArray(), InetAddress.getLocalHost().getHostAddress(),
        null, "123");
    rh.getUserDetails(token);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    rh.logout(token);

    log.debug("SERVER TEAR DOWN BEGINS ");
  }

  @Test
  public void tesCancelProgress() throws Exception {
    rh.cancelProgress(token, "123");
  }

  @Test
  public void testActivateService() throws Exception {
    try {
      rh.activateService(token, "123");
      fail("expected exception");
    }
    catch (Exception rhe) {
    }
  }

  @Test
  public void testActivateServices() throws Exception {
    rh.activateServices(token, new String[] { "123" });
  }

  @Test
  public void testCancelProgress() throws Exception {
    rh.cancelProgress(token, "123");
  }

  @Test
  public void testCancelSchedule() throws Exception {
    try {
      rh.cancelSchedule(token, "123");
      fail("expected exception");
    }
    catch (Exception rhe) {
    }
  }

  @Test
  public void testCancelSchedules() throws Exception {

    rh.cancelSchedules(token, new String[] { "123" });
  }

  @Test
  public void testCancelService() throws Exception {

    try {
      rh.cancelService(token, "123");
      fail("expected exception");
    }
    catch (Exception rhe) {
    }
  }

  @Test
  public void testCancelServices() throws Exception {

    rh.cancelServices(token, new String[] { "123" });
  }

  @Test
  public void testChangePassword() throws Exception {

    try {
      rh.changePassword(token, "admin", "myDrac", "myDrac", "myDrac",
          ClientLoginType.WEB_CLIENT_LOGIN,
          new IPAddress("127.0.0.1", "80").toString(), "123");
    }
    catch (DracPasswordEvaluationException e) {
      // Expected: PasswordErrorCode.ERROR_PW_NOT_OLD
      PasswordErrorCode[] ruleViolations = e.getRuleViolations();
      if (ruleViolations != null && ruleViolations.length > 0) {
        PasswordErrorCode firstErrorCode = ruleViolations[0];
        Assert.assertFalse("Cannot change to old password",
            !PasswordErrorCode.ERROR_PW_NOT_OLD.equals(firstErrorCode));
      }
    }
  }

  @Test
  public void testClearProgress() throws Exception {

    rh.clearProgress(token, "123");
  }

  @Test
  public void testConfirmSchedule() throws Exception {

    try {
      rh.confirmSchedule(token, "123");
      fail("expected exception");
    }
    catch (Exception rhe) {
    }
  }

  @Test
  public void testConfirmSchedules() throws Exception {

    rh.confirmSchedules(token, new String[] { "123" });
  }

  @Test
  public void testCreateResourceGroupProfile() throws Exception {

    ResourceGroupProfile profile = new ResourceGroupProfile("bob",
        Calendar.getInstance(), Calendar.getInstance(), "admin",
        DbResourceGroupProfile.DEFAULT_RESOURCE_GROUP);
    rh.createResourceGroupProfile(token, profile);
    rh.deleteResourceGroup(token, profile.getName());
  }

  @Test
  public void testCreateScheduleAsync() throws Exception {

    try {
      Schedule s = new Schedule(ACTIVATION_TYPE.RESERVATION_AUTOMATIC, "id",
          "admin", SCHEDULE.CONFIRMATION_PENDING,
          System.currentTimeMillis() - 1000 * 60 * 5, null,
          System.currentTimeMillis(), new UserType(null, null, null, null,
              null, null, null), new PathType(), false, new RecurrenceType(),
          new ArrayList<DracService>());
      rh.createScheduleAsync(token, s);
      fail("expected exception");
    }
    catch (RequestHandlerException rhe) {
    }
  }

  @Test
  public void testCreateService() throws Exception {

    try {
      rh.createService(token, null, null);
      fail("expected exception");
    }
    catch (Exception rhe) {
    }
  }

  @Test
  public void testCreateUserGroupProfile() throws Exception {

    UserGroupProfile profile = new UserGroupProfile(new UserGroupName("bob"),
        Calendar.getInstance(), Calendar.getInstance(), "admin",
        new UserGroupName("SystemAdminGroup"));
    rh.createUserGroupProfile(token, profile);
    rh.deleteUserGroup(token, profile.getName());
  }

  @Test
  public void testCreateUserProfile() throws Exception {

    rh.createUserProfile(token, "bob", new LocalAccountCredential("admin",
        "admin", new IPAddress("127.0.0.1", "80")), new AccountStatus(
        AccountState.ENABLED, ""));
    rh.deleteUser(token, "bob");
  }

  @Test
  public void testEditEndPoint() throws Exception {

    // @TODO
  }

  @Test
  public void testEditGlobalPolicy() throws Exception {

    rh.editGlobalPolicy(token, rh.getGlobalPolicy(token));
  }

  @Test
  public void testFindEndpointById() throws Exception {

    EndPointType ep = rh.findEndpointById(token, "123");
    Assert.assertNull("shouldn't find anything", ep);

    ep = rh.findEndpointById(token, "00-21-E1-D6-D5-DC_ETH-1-1-1");
    Assert.assertNotNull("Expected to find an ep", ep);
  }

  @Test
  public void testFindEndpointByTna() throws Exception {

    EndPointType ep = rh.findEndpointByTna(token, "123");
    Assert.assertNull("shouldn't find anything", ep);

    ep = rh.findEndpointByTna(token, "OME0237_OC48-1-5-1");
    Assert.assertNotNull("Expected to find an ep", ep);
  }

  @Test
  public void testForceRedundancySwitch() throws Exception {

    rh.forceRedundancySwitch();
  }

  @Test
  public void testGetActiveSchedules() throws Exception {

    log.debug("getActiveSchedules :" + rh.getActiveSchedules(token));
  }

  @Test
  public void testGetAllServices() throws Exception {

    List<UserGroupName> l = new ArrayList<UserGroupName>();
    rh.getAllServices(token, 0, Long.MAX_VALUE, l);
  }

  @Test
  public void testGetAvailableTimes() throws Exception {

    rh.getAvailableTimes(token, "123", "456", 55, 60);
  }

  @Test
  public void testGetEndpointNames() throws Exception {

    rh.getEndpointNames(token);
  }

  @Test
  public void testGetEndpointResource() throws Exception {

    rh.getEndpointResource(token, "123");
  }

  @Test
  public void testGetEndpoints() throws Exception {

    List<UserGroupName> l = new ArrayList<UserGroupName>();
    rh.getEndpoints(token, "layer0", l);
  }

  @Test
  public void testGetEndpointsForSiteId() throws Exception {

    rh.getEndpointsForSiteId(token, "bob");
  }

  @Test
  public void testGetEndpointsForWavelength() throws Exception {

    rh.getEndpointsForWavelength(token, "");
  }

  @Test
  public void testGetInstance() throws Exception {

    log.debug("version=" + rh.getVersion());
  }

  @Test
  public void testGetLogs() throws Exception {

    rh.getLogs(token, 0, Long.MAX_VALUE, null);
  }

  @Test
  public void testGetProgressUserDetails() throws Exception {

    rh.getProgress(token);
  }

  @Test
  public void testGetProgressUserDetailsString() throws Exception {

    rh.getProgress(token, "123");
  }

  @Test
  public void testGetRedundancyServerInfo() throws Exception {

    rh.getRedundancyServerInfo();
  }

  @Test
  public void testGetResourceGroupProfile() throws Exception {

    rh.getResourceGroupProfile(token, "SystemAdminResourceGroup");
  }

  @Test
  public void testGetResourceGroupProfileNames() throws Exception {

    rh.getResourceGroupProfileNames(token);
  }

  @Test
  public void testGetResourceGroups() throws Exception {

    rh.getResourceGroups(token);
  }

  @Test
  public void testGetScheduleForService() throws Exception {

    try {
      rh.getScheduleForService(token, "123");
      fail("expected exception");
    }
    catch (Exception rhe) {
    }
  }

  @Test
  public void testGetServerInfo() throws Exception {

    rh.getServerInfo(token);
  }

  @Test
  public void testGetService() throws Exception {

    try {
      rh.getService(token, "123");
      fail("expected exception");
    }
    catch (Exception rhe) {
    }
  }

  @Test
  public void testGetSRLGListForService() throws Exception {

    rh.getSRLGListForService(token, "123");
  }

  @Test
  public void testGetStatusInfoList() throws Exception {

    rh.getStatusInfoList(token, "123");
  }

  @Test
  public void testGetSystemMetric() throws Exception {

    rh.getSystemMetric(token);
  }

  @Test
  public void testGetUserDetails() throws Exception {

    rh.getUserDetails(token);
  }

  @Test
  public void testGetUserGroupProfile() throws Exception {

    rh.getUserGroupProfile(token, new UserGroupName("SystemAdminGroup"));
  }

  @Test
  public void testGetUserGroupProfileNames() throws Exception {

    rh.getUserGroupProfileNames(token);
  }

  @Test
  public void testGetUserGroups() throws Exception {

    rh.getUserGroups(token);
  }

  @Test
  public void testGetUserIDs() throws Exception {

    rh.getUserIDs(token);
  }

  @Test
  public void testGetUserProfile() throws Exception {

    rh.getUserProfile(token, "admin");
  }

  @Test
  public void testGetUtilizationUserDetailsString() throws Exception {

    try {
      rh.getUtilization(token, "123");
    }
    catch (Exception rhe) {
    }
  }

  @Test
  public void testGetUtilizationUserDetailsStringDoubleLongLongIntTimeZone()
      throws Exception {

    rh.getUtilization(token, "123", 12.0, 0, Long.MAX_VALUE, 1,
        TimeZone.getDefault());
  }

  @Test
  public void testGetVersion() throws Exception {

    rh.getVersion();
  }

  @Test
  public void testIsResourceGroupEditable() throws Exception {

    rh.isResourceGroupEditable(token,
        rh.getResourceGroupProfile(token, "SystemAdminResourceGroup"));
  }

  @Test
  public void testIsResourceGroupParentable() throws Exception {

    rh.isResourceGroupParentable(token, "SystemAdminResourceGroup");
  }

  @Test
  public void testIsUserEditable() throws Exception {

    rh.isUserEditable(token, rh.getUserProfile(token, "admin"));
  }

  @Test
  public void testIsUserGroupEditable() throws Exception {

    rh.isUserGroupEditable(token,
        rh.getUserGroupProfile(token, new UserGroupName("SystemAdminGroup")));
  }

  @Test
  public void testLogout() throws Exception {

    LoginToken tok = rh.login(ClientLoginType.INTERNAL_LOGIN, "admin",
        "myDrac".toCharArray(), "127.0.0.1", null, "123");
    rh.logout(tok);
  }

  @Test
  public void testQueryAllServiceAlarms() throws Exception {

    List<UserGroupName> l = new ArrayList<UserGroupName>();
    rh.queryAllServiceAlarms(token, 0, Long.MAX_VALUE, l);
  }

  @Test
  public void testQueryPath() throws Exception {

    try {
      rh.queryPath(token, new DracService());
    }
    catch (RequestHandlerException rhe) {
    }
  }

  @Test
  public void testQuerySchedule() throws Exception {

    try {
      rh.querySchedule(token, "123");
    }
    catch (Exception rhe) {
    }
  }

  @Test
  public void testQuerySchedules() throws Exception {

    List<UserGroupName> l = new ArrayList<UserGroupName>();
    rh.querySchedules(token, 0, Long.MAX_VALUE, l);
  }

  @Test
  public void testQueryServiceAlarm() throws Exception {

    rh.queryServiceAlarm(token, "123");
  }

  @Test
  public void testSessionValidate() throws Exception {

    rh.sessionValidate(token);
  }

  @Test
  public void testSetConfirmationTimeout() throws Exception {

    rh.setConfirmationTimeout(token, rh.getConfirmationTimeout(token));
  }

  @Test
  public void testSetResourceGroupMembership() throws Exception {

    ResourceGroupProfile p = rh.getResourceGroupProfile(token,
        "SystemAdminResourceGroup");
    rh.setResourceGroupMembership(token, p.getName(), p.getMembership());
  }

  @Test
  public void testSetResourceGroupPolicy() throws Exception {

    ResourceGroupProfile p = rh.getResourceGroupProfile(token,
        "SystemAdminResourceGroup");
    rh.setResourceGroupPolicy(token, p.getName(), p.getResourcePolicy());
  }

  @Test
  public void testSetResourceGroupResourceList() throws Exception {

    ResourceGroupProfile p = rh.getResourceGroupProfile(token,
        "SystemAdminResourceGroup");
    rh.setResourceGroupResourceList(token, p.getName(), p.getResourceList());
  }

  @Test
  public void testSetScheduleOffset() throws Exception {

    rh.setScheduleOffset(token, rh.getGlobalPolicy(token)
        .getScheduleProvisioningOffset());
  }

  @Test
  public void testSetupUserAuthenticationAccount() throws Exception {

    rh.setupUserAuthenticationAccount(token, "admin",
        new LocalAccountCredential("admin", "myDrac", new IPAddress(
            "127.0.0.1", "80")));
  }

  @Test
  public void testSetUserAccountStatus() throws Exception {

    rh.setUserAccountStatus(token, "admin", new AccountStatus());
  }

  @Test
  public void testSetUserAuthenticationData() throws Exception {

    UserProfile p = rh.getUserProfile(token, "admin");
    rh.setUserAuthenticationData(token, p.getUserID(),
        p.getAuthenticationData());
  }

  @Test
  public void testSetUserGroupMembership() throws Exception {

    UserGroupProfile p = rh.getUserGroupProfile(token, new UserGroupName(
        "SystemAdminGroup"));
    rh.setUserGroupMembership(token, p.getName(), p.getMembership());
  }

  @Test
  public void testSetUserGroupUserGroupPolicy() throws Exception {

    UserGroupProfile p = rh.getUserGroupProfile(token, new UserGroupName(
        "SystemAdminGroup"));
    rh.setUserGroupUserGroupPolicy(token, p.getName(), p.getGroupPolicy());
  }

  @Test
  public void testSetUserMembership() throws Exception {

    UserProfile p = rh.getUserProfile(token, "admin");
    rh.setUserMembership(token, p.getUserID(), p.getMembershipData());
  }

  @Test
  public void testSetUserOrganization() throws Exception {

    UserProfile p = rh.getUserProfile(token, "admin");
    rh.setUserOrganization(token, p.getUserID(), p.getOrganizationData());
  }

  @Test
  public void testSetUserPersonalData() throws Exception {

    UserProfile p = rh.getUserProfile(token, "admin");
    rh.setUserPersonalData(token, p.getUserID(), p.getPersonalData());
  }

  @Test
  public void testSetUserTimeZoneIDPreference() throws Exception {

    UserProfile p = rh.getUserProfile(token, "admin");
    rh.setUserTimeZoneIDPreference(token, p.getUserID(), p.getPreferences()
        .getTimeZoneId());
  }
}
