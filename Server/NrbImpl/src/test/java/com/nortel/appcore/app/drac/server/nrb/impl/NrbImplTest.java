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

package com.nortel.appcore.app.drac.server.nrb.impl;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.security.authentication.types.LocalAccountCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.security.policy.types.OrganizationData;
import com.nortel.appcore.app.drac.common.security.policy.types.PersonalData;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfileXML.ResourceType;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AccountState;
import com.nortel.appcore.app.drac.common.types.AbstractMessageType.MessageBox;
import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.ReceivedMessageType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.SentMessageType;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.Site;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.common.types.UtilizationStructure;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
import com.nortel.appcore.app.drac.database.dracdb.DbResourceGroupProfile;
import com.nortel.appcore.app.drac.database.helper.test.DbTestPopulateDb;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbServerInterface;


@SuppressWarnings("unused")
public class NrbImplTest {
  private static final Logger log = LoggerFactory.getLogger(NrbImplTest.class);
	private static NrbLauncher main;
	private static NrbServerInterface nrb;

	/*
	 * A number of user and authentication states for testing Improved NRB_PORT
	 * Interface
	 */
	private static UserDetails adminUserDetails;
	private static LoginToken adminToken;

	private static UserDetails user1Details;
	private static LoginToken user1Token;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestHelper.INSTANCE.initialize();

		// Populate our database with records we can use to exercise with
		DbTestPopulateDb.populateTestSystem(true);

		String bindName = "//localhost:" + RmiServerInfo.RMI_REGISTRY_PORT + "/"
		    + RmiServerInfo.NRB_RMI_NAME;
		main = new NrbLauncher();
		main.startup();
		Thread.sleep(3 * 1000);

		nrb = main.getNrb();
		// nrb = (NrbInterface) Naming.lookup(bindName);
		nrb.isAlive(null);

		/*
		 * A number of user and authentication states for testing Improved NRB_PORT
		 * Interface
		 */

		// adminUser_validPass_loggedIn
		adminToken = nrb.login(ClientLoginType.INTERNAL_LOGIN, "admin",
		    "myDrac".toCharArray(), "127.0.0.1", null, "123");
		adminUserDetails = nrb.getUserDetails(adminToken);

		// TestUser1 from DbTestPopulateDb
		user1Token = nrb.login(ClientLoginType.INTERNAL_LOGIN,
		    DbTestPopulateDb.TEST_USER_NAME1, "myDrac".toCharArray(), "127.0.0.1",
		    null, "123");
		user1Details = nrb.getUserDetails(user1Token);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		log.debug("Calling terminate");
		main.shutdown();
		Thread.sleep(2*1000L);
		TestHelper.INSTANCE.destroy();
	}

	//
	// @Test
	// public void FtestQueryPath()
	// throws Exception
	// {
	// if (TestHelper.disabled())
	// {
	// return;
	// }
	// boolean result = nrb.queryPath(userDetails, new
	// ServiceIdType(ACTIVATION_TYPE.RESERVATION_AUTOMATIC));
	// }

	@Test
	public void testActivateService() throws Exception {
		try {
			nrb.activateService(adminUserDetails, "123");
			fail("Expected this to fail!");
		}
		catch (Exception e) {
			// Expected this to fail.
		}
		try {
			nrb.activateService(adminUserDetails, DbTestPopulateDb.TEST_SERVICE_NAME);
			fail("Expected this to fail!");
		}
		catch (Exception e) {
			// Expected this to fail.
		}
	}

	@Test
	public void testActivateServices() throws Exception {
		nrb.activateServices(adminUserDetails, null);
		nrb.activateServices(adminUserDetails,
		    new String[] { DbTestPopulateDb.TEST_SERVICE_NAME });
	}

	@Test
	public void testAsyncCreateSchedule() throws Exception {
		try {
			Schedule s = new Schedule(ACTIVATION_TYPE.RESERVATION_AUTOMATIC, "id",
			    "admin", SCHEDULE.CONFIRMATION_PENDING,
			    System.currentTimeMillis() - 1000 * 60 * 5, null,
			    System.currentTimeMillis(), new UserType(null, null, null, null,
			        null, null, null), new PathType(), false, new RecurrenceType(),
			    new ArrayList<DracService>());
			nrb.asyncCreateSchedule(adminUserDetails, s);
		}
		catch (Exception e) {
			checkforLpcpNotRunning(e);
		}
	}

	@Test
	public void testAuditUserProfileLinkages() throws Exception {
		nrb.auditUserProfileLinkages(adminUserDetails);
	}

	@Test
	public void testCancelSchedules() throws Exception {

		nrb.cancelSchedules(adminUserDetails, null);
		nrb.cancelSchedules(adminUserDetails, new String[] { "123" });
	}

	@Test
	public void testCancelServices() throws Exception {

		nrb.cancelServices(adminUserDetails, null);
		nrb.cancelServices(adminUserDetails, new String[] { "123" });
	}

	@Test
	public void testCancelTask() throws Exception {

		nrb.cancelTask(adminUserDetails, "123");
	}

	@Test
	public void testClearTaskInfo() throws Exception {

		nrb.clearTaskInfo(adminUserDetails, "123");
	}

	@Test
	public void testConfirmSchedules() throws Exception {

		nrb.confirmSchedules(adminUserDetails, null);
		nrb.confirmSchedules(adminUserDetails, new String[] { "123" });
	}

	@Test
	public void testCreateResourceGroupProfile() throws Exception {

		ResourceGroupProfile profile = new ResourceGroupProfile("bob",
		    Calendar.getInstance(), Calendar.getInstance(), "admin",
		    DbResourceGroupProfile.DEFAULT_RESOURCE_GROUP);
		nrb.createResourceGroupProfile(adminUserDetails, profile);
		nrb.deleteResourceGroupProfile(adminUserDetails, profile.getName());
	}

	@Test
	public void testCreateUserGroupProfile() throws Exception {

		UserGroupProfile profile = new UserGroupProfile(new UserGroupName("bob"),
		    Calendar.getInstance(), Calendar.getInstance(), "admin",
		    new UserGroupName("SystemAdminGroup"));
		nrb.createUserGroupProfile(adminUserDetails, profile);
		nrb.deleteUserGroupProfile(adminUserDetails, profile.getName());
	}

	@Test
	public void testCreateUserProfile() throws Exception {

		nrb.createUserProfile(adminUserDetails, "bob", new LocalAccountCredential(
		    "admin", "admin", new IPAddress("127.0.0.1", "80")), new AccountStatus(
		    AccountState.ENABLED, ""));
		nrb.deleteUserProfile(adminUserDetails, "bob");
	}

	@Test
	public void testDeleteFacility() throws Exception {

		nrb.deleteFacility(adminUserDetails, "00-00-00-00", "OC192-1-1-1");
	}

	@Test
	public void testEditEndPoint() throws Exception {

		List<EndPointType> l = nrb.queryAllUserEndpoints(adminUserDetails);
		if (l == null || l.isEmpty()) {
			fail("Could not fetch any endpoints to edit...");
		}
		try {
			nrb.editEndPoint(adminUserDetails, l.get(0));
		}
		catch (Exception e) {
			checkforLpcpNotRunning(e);
		}
	}

	@Test
	public void testExplicitPurge() throws Exception {

		nrb.purgeServices(adminUserDetails, new ArrayList<String>());
	}

	@Test
	public void testGetActiveSchedules() throws Exception {

		List<Schedule> results = nrb.getActiveSchedules(adminUserDetails);
	}

	@Test
	public void testGetAlarms() throws Exception {

		List<String> results = nrb.getAlarms(adminUserDetails, null);
	}

	@Test
	public void testGetConfirmationTimeout() throws Exception {

		nrb.getConfirmationTimeout(adminUserDetails);
	}

	@Test
	public void testGetEndpointResource() throws Exception {

		Resource result = nrb.getEndpointResource(adminUserDetails, "123");
	}

	@Test
	public void testGetEndpointResourceUIInfo() throws Exception {

		Map<String, String> filter = new HashMap<String, String>();

		// Regular User success path
		filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
		    Layer.LAYER_ALL.toString());
		List<EndpointResourceUiType> list = nrb.getUserEndpointsUIInfo(
		    user1Details, "TestResourceGroup1", filter);
		Assert
		    .assertNotNull(
		        "TestUser1 expects endpoint info from resource group TestResourceGroup1",
		        list);
		Assert.assertEquals(2, list.size());

		// Admin User success path
		filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
		    Layer.LAYER_ALL.toString());
		list = nrb.getUserEndpointsUIInfo(adminUserDetails,
		    "SystemAdminResourceGroup", filter);
		Assert.assertNotNull("Admin expects endpoint info for all resources", list);
		Assert.assertEquals(45, list.size());
		filter
		    .put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER1.toString());
		list = nrb.getUserEndpointsUIInfo(adminUserDetails,
		    "SystemAdminResourceGroup", filter);
		Assert.assertNotNull("Admin expects endpoint info for all resources", list);
		Assert.assertEquals(4, list.size());
		filter
		    .put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER2.toString());
		list = nrb.getUserEndpointsUIInfo(adminUserDetails,
		    "SystemAdminResourceGroup", filter);
		Assert.assertNotNull("Admin expects endpoint info for all resources", list);
		Assert.assertEquals(19, list.size());
		filter
		    .put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER0.toString());
		list = nrb.getUserEndpointsUIInfo(adminUserDetails,
		    "SystemAdminResourceGroup", filter);
		Assert.assertNotNull("Admin expects endpoint info for all resources", list);
		Assert.assertEquals(22, list.size());
		filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
		    Layer.LAYER1_LAYER2.toString());
		list = nrb.getUserEndpointsUIInfo(adminUserDetails,
		    "SystemAdminResourceGroup", filter);
		Assert.assertNotNull("Admin expects endpoint info for all resources", list);
		Assert.assertEquals(23, list.size());

		// failure path - no access privilege
		try {
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER_ALL.toString());
			list = nrb.getUserEndpointsUIInfo(user1Details,
			    "SystemAdminResourceGroup", filter);
			fail("Expected this to fail");
		}
		catch (Exception e) {
			// expected
		}

		// Test a facility retrieval that cross-references with ResourceGroupProfile
		ResourceGroupProfile rgp = nrb.getResourceGroupProfile(user1Details,
		    "TestResourceGroup1");
		List<Resource> resources = rgp.getResourceList();
		Assert.assertEquals("Expected the assigned number of resources", 2,
		    resources.size());
		List<String> resourceEndpointIds = new ArrayList<String>();
		for (Resource resource : resources) {
			if (resource.getResourceType() == ResourceType.ENDPOINT) {
				resourceEndpointIds.add(resource.getResourceID());
			}
		}
		filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
		    Layer.LAYER_ALL.toString());
		List<Map<String, String>> eps = DbNetworkElementFacility.INSTANCE
		    .retrieveUserEndpoints(resourceEndpointIds, filter);
		Assert.assertEquals("Expected the assigned number of resources", 2,
		    resources.size());

	}

	@Test
	public void testGetFacilityConstraints() throws Exception {

		Map<String, String> filter = new HashMap<String, String>();
		filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
		    Layer.LAYER1_LAYER2.toString());
		Map<String, BigInteger> result = nrb.getFacilityConstraints(
		    adminUserDetails, filter);
		Assert.assertFalse(
		    "expected non-zero mapping of facilities to their constraints",
		    result.isEmpty());
	}

	@Test
	public void testGetGlobalPolicy() throws Exception {

		GlobalPolicy result = nrb.getGlobalPolicy(adminUserDetails);
	}

	@Test
	public void testGetInfo() throws Exception {

		try {
			nrb.getInfo(adminUserDetails);
		}
		catch (Exception e) {
			// lpcp is not running, will fail
			// checkforLpcpNotRunning(e);
		}
	}

	@Test
	public void testGetInprogressCalls() throws Exception {

		nrb.getInprogressCalls(adminUserDetails);
	}

	@Test
	public void testGetLogs() throws Exception {

		List<LogRecord> result = nrb.getLogs(adminUserDetails, 0, Long.MAX_VALUE,
		    null);
	}

	@Test
	public void testGetNetworkElementAdjacencies() throws Exception {

		List<NetworkElementAdjacency> result = nrb.getNetworkElementAdjacencies(
		    adminUserDetails, null);
		Assert.assertFalse("expected non-zero adjacencies", result.isEmpty());
	}

	@Test
	public void testGetNetworkElementConnections() throws Exception {

		List<CrossConnection> results = nrb.getNetworkElementConnections(
		    adminUserDetails, null);
	}

	@Test
	public void testGetNetworkElementFacilities() throws Exception {

		List<Facility> result = nrb.getNetworkElementFacilities(adminUserDetails,
		    null);
		Assert.assertFalse("expected non-zero Facilities", result.isEmpty());
	}

	@Test
	public void testGetNetworkElements() throws Exception {

		List<NetworkElementHolder> result = nrb.getNetworkElements(
		    adminUserDetails, null);
		Assert.assertFalse("expected non-zero NetworkElements", result.isEmpty());
	}

	@Test
	public void testGetResourceGroupProfile() throws Exception {

		ResourceGroupProfile result = nrb.getResourceGroupProfile(adminUserDetails,
		    "SystemAdminResourceGroup");
		Assert.assertNotNull("expected non-null resourceGroupProfile", result);

		// Correct access for this test user
		result = nrb.getResourceGroupProfile(user1Details, "TestResourceGroup1");
		Assert.assertNotNull("TestUser1 expects access to TestResourceGroup1",
		    result);

		// Incorrect access for this test user
		try {
			// this user should not have access to the SystemAdminResourceGroup
			result = nrb.getResourceGroupProfile(user1Details,
			    "SystemAdminResourceGroup");
			fail("Expected this to fail");
		}
		catch (Exception e) {
			// expected
		}

	}

	@Test
	public void testGetResourceGroupProfileList() throws Exception {

		List<ResourceGroupProfile> result = nrb
		    .getResourceGroupProfileList(adminUserDetails);
		Assert.assertFalse("expected non-zero ResourceGroupProfileList",
		    result.isEmpty());
	}

	// @Test
	// public void FtestBackupDatabase()
	// throws Exception
	// {
	// if (TestHelper.disabled())
	// {
	// return;
	// }
	// nrb.backupDatabase(userDetails, "./logs");
	// }
	//
	// @Test
	// public void FtestBackupDatabaseRemotely()
	// throws Exception
	// {
	// if (TestHelper.disabled())
	// {
	// return;
	// }
	// FileIteratorI it = nrb.backupDatabaseRemotely(userDetails);
	// while (it.getChunk() != null)
	// {
	// }
	// }
	//
	// @Test
	// public void FtestCancelService()
	// throws Exception
	// {
	// if (TestHelper.disabled())
	// {
	// return;
	// }
	// nrb.cancelService(userDetails, "123");
	// }
	//
	// @Test
	// public void FtestConfirmSchedule()
	// throws Exception
	// {
	// if (TestHelper.disabled())
	// {
	// return;
	// }
	// nrb.confirmSchedule(userDetails, "123");
	// }
	//
	// @Test
	// public void FtestCreateService()
	// throws Exception
	// {
	// if (TestHelper.disabled())
	// {
	// return;
	// }
	// try
	// {
	// ServiceIdType s = new ServiceIdType(ACTIVATION_TYPE.RESERVATION_AUTOMATIC);
	// nrb.createService(userDetails, "123", s);
	// }
	// catch (Exception e)
	// {
	// checkforLpcpNotRunning(e);
	// }
	// }
	//
	@Test
	public void testGetSchedule() throws Exception {

		nrb.getSchedule(adminUserDetails, DbTestPopulateDb.TEST_SCHEDULE_NAME);
	}

	@Test
	public void testGetSchedules() throws Exception {

		nrb.getSchedules(adminUserDetails, null);
	}

	@Test
	public void testGetServices() throws Exception {

		List<ServiceXml> result = nrb.getServices(adminUserDetails, null);
	}

	@Test
	public void testGetServicesEligibleForPurge() throws Exception {

		nrb.getServicesEligibleForPurge(adminUserDetails, null);
	}

	@Test
	public void testGetServicesFromAID() throws Exception {

		Map<String, String> m = new HashMap<String, String>();
		m.put(DbKeys.LightPathCols.LP_AID, "OC12-1-1");
		m.put(DbKeys.LightPathCols.LP_NEID, "111");
		List<ServiceXml> results = nrb.getServicesFromAID(adminUserDetails, m);
	}

	@Test
	public void testGetServiceUsageForTNA() throws Exception {

		List<DracService> result = nrb.getServiceUsageForTNA(adminUserDetails,
		    "tna");
	}

	// @Test
	/*
	 * public void testGetSRLGListForService() throws Exception { if
	 * (TestHelper.disabled()) { return; } String result =
	 * nrb.getSRLGListForService(adminUserDetails, "123"); }
	 */

	@Test
	public void testGetStatusInfo() throws Exception {

		nrb.getStatusInfo(adminUserDetails, "123");
	}

	@Test
	public void testGetSystemMetric() throws Exception {

		nrb.getSystemMetric(adminUserDetails);
	}

	@Test
	public void testGetTaskInfoUserDetails() throws Exception {

		List<TaskType> result = nrb.getTaskInfo(adminUserDetails);
	}

	@Test
	public void testGetTaskInfoUserDetailsString() throws Exception {

		TaskType result = nrb.getTaskInfo(adminUserDetails, "123");
	}

	
	@Test
	public void testGetUserGroupProfile() throws Exception {

		UserGroupProfile result = nrb.getUserGroupProfile(adminUserDetails,
		    new UserGroupName("SystemAdminGroup"));
		Assert.assertNotNull("expected non-null usergroup profile", result);
	}

	@Test
	public void testGetUserGroupProfileList() throws Exception {

		List<UserGroupProfile> result = nrb
		    .getUserGroupProfileList(adminUserDetails);
		Assert.assertFalse("expected non-zero UserGroupProfileList",
		    result.isEmpty());
	}

	@Test
	public void testGetUserProfile() throws Exception {

		UserProfile result = nrb.getUserProfile(adminUserDetails, "admin");
		Assert.assertNotNull("expected non-null user profile", result);
	}

	@Test
	public void testGetUserProfileList() throws Exception {

		List<UserProfile> result = nrb.getUserProfileList(adminUserDetails);
		Assert.assertFalse("expected non-zero UserProfileList", result.isEmpty());
	}

	/*
	 * @Test public void testGetUtilization() throws Exception { if
	 * (TestHelper.disabled()) { return; } try {
	 * nrb.getUtilization(adminUserDetails, "tna"); } catch (Exception e) {
	 * checkforLpcpNotRunning(e); } }
	 */

	@Test
	public void testIsAllowed() throws Exception {

		UserProfile p = nrb.getUserProfile(adminUserDetails, "admin");
		nrb.isAllowed(adminUserDetails, new PolicyRequest(p,
		    PolicyRequest.CommandType.READ));
	}

	@Test
	public void testIsNRBAlive() throws Exception {

		nrb.isAlive(adminUserDetails);
	}

	@Test
	public void testLoadUserPreferences() throws Exception {

		nrb.saveUserPreferences(adminUserDetails, "admin", "<pref a=\"bob\" />");
		String result = nrb.loadUserPreferences(adminUserDetails, "admin");
		Assert.assertNotNull("preferences should not be null!", result);
	}

	@Test
	public void testLogin() throws Exception {

		LoginToken token = nrb.login(ClientLoginType.ADMIN_CONSOLE_LOGIN, "admin",
		    "myDrac".toCharArray(), "localhost", null, "123");
		nrb.sessionValidate(token);
		nrb.getUserDetails(token);
		nrb.logout(token);
	}

	@Test
	public void testPurgeLogs() throws Exception {

		Map<String, String> map = new HashMap<String, String>();
		map.put(DbKeys.LogKeys.TIME, "17");
		nrb.purgeLogs(adminUserDetails, map);
	}

	@Test
	public void testQueryAllServiceAlarms() throws Exception {

		List<AlarmType> alarms = nrb.queryAllServiceAlarms(adminUserDetails, 0,
		    Long.MAX_VALUE);
	}

	@Test
	public void testQueryAllUserEndpoints() throws Exception {

		List<EndPointType> result = nrb.queryAllUserEndpoints(adminUserDetails);
		Assert.assertFalse("expected non-zero UserEndpoints", result.isEmpty());
	}

	@Test
	public void testQueryScheduleByService() throws Exception {

		Schedule result = nrb.queryScheduleByService(adminUserDetails,
		    DbTestPopulateDb.TEST_SERVICE_NAME);
	}

	@Test
	public void testQueryServiceAlarm() throws Exception {

		AlarmType result = nrb.queryServiceAlarm(adminUserDetails, "123");
	}

	@Test
	public void testQueryServices() throws Exception {

		List<UserGroupName> g = Arrays
		    .asList(new UserGroupName[] { new UserGroupName("bob") });
		List<DracService> result = nrb.queryServices(adminUserDetails, 0,
		    Long.MAX_VALUE, g);
	}

	@Test
	public void testQueryUtilization() throws Exception {

		UtilizationStructure result = nrb.queryUtilization(adminUserDetails, "TNA",
		    100, 0, Long.MAX_VALUE, 1, TimeZone.getDefault());
	}

	@Test
	public void testSessionValidate() throws Exception {

		nrb.sessionValidate(adminToken);
	}

	@Test
	public void testSetConfirmationTimeout() throws Exception {

		nrb.setConfirmationTimeout(adminUserDetails, 55);
	}

	@Test
	public void testSetDefaultGlobalPolicy() throws Exception {

		nrb.setDefaultGlobalPolicy(adminUserDetails,
		    nrb.getGlobalPolicy(adminUserDetails));
	}

	@Test
	public void testSetResourceGroupMembership() throws Exception {

		ResourceGroupProfile p = nrb.getResourceGroupProfile(adminUserDetails,
		    DbResourceGroupProfile.DEFAULT_RESOURCE_GROUP);
		nrb.setResourceGroupMembership(adminUserDetails, p.getName(),
		    p.getMembership());
	}

	@Test
	public void testSetResourceGroupPolicy() throws Exception {

		ResourceGroupProfile p = nrb.getResourceGroupProfile(adminUserDetails,
		    DbResourceGroupProfile.DEFAULT_RESOURCE_GROUP);
		nrb.setResourceGroupPolicy(adminUserDetails, p.getName(),
		    p.getResourcePolicy());
	}

	@Test
	public void testSetResourceGroupResourceList() throws Exception {

		ResourceGroupProfile p = nrb.getResourceGroupProfile(adminUserDetails,
		    DbResourceGroupProfile.DEFAULT_RESOURCE_GROUP);
		nrb.setResourceGroupResourceList(adminUserDetails, p.getName(),
		    p.getResourceList());
	}

	@Test
	public void testSetScheduleOffset() throws Exception {

		nrb.setScheduleOffset(adminUserDetails, 55);
	}

	@Test
	public void testSetupUserAuthenticationAccount() throws Exception {

		nrb.setupUserAuthenticationAccount(adminUserDetails, "admin",
		    new LocalAccountCredential("admin", "admin", new IPAddress("127.0.0.1",
		        "80")));
	}

	@Test
	public void testSetUserAccountStatus() throws Exception {

		nrb.setUserAccountStatus(adminUserDetails, "admin", new AccountStatus());
	}

	@Test
	public void testSetUserAuthenticationData() throws Exception {

		nrb.setUserAuthenticationData(adminUserDetails, "admin", nrb
		    .getUserProfile(adminUserDetails, "admin").getAuthenticationData());
	}

	@Test
	public void testSetUserGroupMembership() throws Exception {

		UserGroupProfile p = nrb.getUserGroupProfile(adminUserDetails,
		    new UserGroupName("SystemAdminGroup"));
		nrb.setUserGroupMembership(adminUserDetails, p.getName(), p.getMembership());
	}

	@Test
	public void testSetUserGroupUserGroupPolicy() throws Exception {

		UserGroupProfile p = nrb.getUserGroupProfile(adminUserDetails,
		    new UserGroupName("SystemAdminGroup"));
		nrb.setUserGroupUserGroupPolicy(adminUserDetails, p.getName(),
		    p.getGroupPolicy());
	}

	@Test
	public void testSetUserMembership() throws Exception {

		nrb.setUserMembership(adminUserDetails, "admin",
		    nrb.getUserProfile(adminUserDetails, "admin").getMembershipData());
	}

	@Test
	public void testSetUserOrganization() throws Exception {

		OrganizationData od = new OrganizationData();
		od.setDescription("bob");
		nrb.setUserOrganization(adminUserDetails, "admin", od);
	}

	@Test
	public void testSetUserPersonalData() throws Exception {

		PersonalData pd = new PersonalData();
		pd.setDescription("testing");
		nrb.setUserPersonalData(adminUserDetails, "admin", pd);
	}

	@Test
	public void testSetUserTimeZoneIDPreference() throws Exception {
		nrb.setUserTimeZoneIDPreference(adminUserDetails, "admin", "GMT");
	}

	@Test
	public void testSite() throws Exception {
		nrb.addSite(adminUserDetails, new Site("id", "farAway", "who uses these?"));
		List<Site> sites = nrb.retrieveSiteList(adminUserDetails);
		Assert.assertFalse("eh? Expected non-zero sites", sites.isEmpty());
		nrb.updateSite(adminUserDetails, new Site("id",
		    "not that far away after all", "who uses these?"));
	}

	/**
	 * Certain operations will fail because we don't have LPCP_PORT up and running.
	 * Those operations should return a NRB_PORT exception with the error code
	 * DracErrorConstants.MLBW_ERROR_2003_CONTROLLER_UNAVAILBLE. If we see that
	 * error code, swallow the exception and pass the test case, else throw it
	 * again and fail the test case.
	 */
	private void checkforLpcpNotRunning(Exception e) throws Exception {
		Throwable ne = e;
		do {
			if (ne instanceof NrbException) {
				if (((NrbException) ne).getErrorCode() == DracErrorConstants.MLBW_ERROR_2003_CONTROLLER_UNAVAILBLE) {
					// Pass we failed because LPCP_PORT is not running.
					return;
				}
				throw (NrbException) ne;
			}
			ne = ne.getCause();
		}
		while (ne != null);

	}
}
