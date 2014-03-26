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

package com.nortel.appcore.app.drac.database.helper.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.PathType.PROTECTION_TYPE;
import com.nortel.appcore.app.drac.common.types.RecurrenceType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.ServiceXml.XC_TYPE;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.dracdb.DbAdminConsoleUserPreferences;
import com.nortel.appcore.app.drac.database.dracdb.DbDracSchema;
import com.nortel.appcore.app.drac.database.dracdb.DbGlobalPolicy;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPathAlarmDetails;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPathAlarmSummaries;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPathEdge;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementConnection;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
import com.nortel.appcore.app.drac.database.dracdb.DbResourceGroupProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;
import com.nortel.appcore.app.drac.database.dracdb.DbSites;
import com.nortel.appcore.app.drac.database.dracdb.DbUser;
import com.nortel.appcore.app.drac.database.dracdb.DbUserGroupProfile;
import com.nortel.appcore.app.drac.database.helper.DbUtilityLpcpScheduler;

/**
 * To build a reproducible Junit/Integration test environment we need our DRAC
 * database running and populated with test data.
 * 
 * @author pitman
 */
public final class DbTestPopulateDb {
	/*
	 * We populate the database with certain entries, use these to reference them
	 * in tests if required.
	 */
	public static final String TEST_USER_GROUP1 = "TestUserGroup1";
	public static final String TEST_USER_NAME1 = "TestUser1";
	public static final String TEST_RESOURCE_GROUP1 = "TestResourceGroup1";
	// expired password user
	public static final String TEST_USER_NAME2 = "expired";

	public static final String TEST_SCHEDULE_NAME = "SCHEDULE-1266529013426";
	public static final String TEST_SERVICE_NAME = "SERVICE-1266529013429";

	private DbTestPopulateDb() {
		super();
	}

	/**
	 * Deletes all records in all databases. Note that this will remove then
	 * re-add certain default records, such as the default admin user in the user
	 * profile record, so technically the databases are not 100% empty but rather
	 * mostly empty and in a default state. In nearly all cases, we first drop the
	 * table then recreate it and add any mandatory records back in.
	 */
	public static void clearAllDRACDatabases() throws Exception {
		DbDracSchema.INSTANCE.deleteAll();
		DbResourceGroupProfile.INSTANCE.deleteAll();
		DbUser.INSTANCE.deleteAll();
		DbUserGroupProfile.INSTANCE.deleteAll();
		DbSites.INSTANCE.deleteAll();
		DbSchedule.INSTANCE.deleteAll();
		DbNetworkElementFacility.INSTANCE.deleteAll();
		DbNetworkElementConnection.INSTANCE.deleteAll();
		DbNetworkElementAdjacency.INSTANCE.deleteAll();
		DbNetworkElement.INSTANCE.deleteAll();
		DbLog.INSTANCE.deleteAll();
		DbLightPathEdge.INSTANCE.deleteAll();
		DbLightPathAlarmSummaries.INSTANCE.deleteAll();
		DbLightPathAlarmDetails.INSTANCE.deleteAll();
		DbLightPath.INSTANCE.deleteAll();
		DbAdminConsoleUserPreferences.INSTANCE.deleteAll();
		DbGlobalPolicy.INSTANCE.deleteAll();
	}

	/**
	 * Populates the various DRAC database tables with data suitable for running
	 * tests with. Modeled aginst a real 3 NE system at Nortel's test lab.
	 */
	public static void populateTestSystem(boolean populateNeData)
	    throws Exception {
		// Start from scratch
		clearAllDRACDatabases();

		/**
		 * The following tables are ok as is.
		 * <p>
		 * DracSchema
		 * <p>
		 * DbSites
		 * <p>
		 * DbMail
		 * <p>
		 * GlobalPolicy
		 * <p>
		 * DbAdminConsoleUserPreferences
		 * <p>
		 */

		/**
		 * Populate
		 * <p>
		 * DbNetworkElement
		 * <p>
		 * DbNetworkElementFacility
		 * <p>
		 * DbNetworkElementAdjacency
		 * <p>
		 * DbNetworkElementConnection
		 * <p>
		 * DbSchedule
		 * <p>
		 * DbLightPath DbLightPathEdge DbLightPathAlarmDetails
		 * DbLightPathAlarmSummaries
		 * <p>
		 * <p>
		 * DbLog.INSTANCE.deleteAll();
		 * <p>
		 * DbResourceGroupProfile.INSTANCE.deleteAll();
		 * DbUser.INSTANCE.deleteAll();
		 * DbUserGroupProfile.INSTANCE.deleteAll();
		 */

		/*
		 * IF we are going to run the NE simulator and simulate NEs this data should
		 * not be added.
		 */
		if (populateNeData) {
			// Add 3 NEs
			populateNes();
			// Add facilities
			populateFacilities();
			// Add Topology between them
			populateAdjacency();
		}
		else {
			populateFacilities();
		}

		populateManualAdjacency();
		populateAlarms();

		// add Connections?
		// Create a schedule & service with alarms?

		populateASchedule(TEST_SCHEDULE_NAME);

		populateAService(TEST_SERVICE_NAME, TEST_SCHEDULE_NAME,
		    "b928862f-1266529013793");

		populateSchedulesAndServices();
		// Add some audit logs

		// For security testing add non-default users/groups/resources
		populateUsersUserGroupsResourceGroups();

		populateConsoleUserPrefs();

		populateTestLogs();
	}

	/**
     * 
     */
	private static void populateAdjacency() throws Exception {
		/**
		 * INSERT INTO `NetworkElementAdjacency` VALUES (
		 * '00-21-E1-D6-D8-2C_OC12-1-11-1','00-21-e1-d6-d8-2c','OC12-1-11-1','00-21-e1-d6-d6-70','OC12-1-12
		 * - 1 ' , 0 ) ,
		 * <p>
		 * (
		 * '00-21-E1-D6-D8-2C_OC12-1-12-1','00-21-e1-d6-d8-2c','OC12-1-12-1','00-21-e1-d6-d5-dc','OC12-1-11
		 * - 1 ' , 0 ) ,
		 * <p>
		 * (
		 * '00-21-E1-D6-D5-DC_OC12-1-11-1','00-21-e1-d6-d5-dc','OC12-1-11-1','00-21-e1-d6-d8-2c','OC12-1-12
		 * - 1 ' , 0 ) ,
		 * <p>
		 * (
		 * '00-21-E1-D6-D5-DC_OC12-1-12-1','00-21-e1-d6-d5-dc','OC12-1-12-1','00-21-e1-d6-d6-70','OC12-1-11
		 * - 1 ' , 0 ) ,
		 * <p>
		 * (
		 * '00-21-E1-D6-D6-70_OC12-1-11-1','00-21-e1-d6-d6-70','OC12-1-11-1','00-21-e1-d6-d5-dc','OC12-1-12
		 * - 1 ' , 0 ) ,
		 * <p>
		 * (
		 * '00-21-E1-D6-D6-70_OC12-1-12-1','00-21-e1-d6-d6-70','OC12-1-12-1','00-21-e1-d6-d8-2c','OC12-1-11
		 * - 1 ' , 0 ) ;
		 */
		List<NetworkElementAdjacency> list = new ArrayList<NetworkElementAdjacency>();

		NetworkElementAdjacency adj;

		// Providing mix of topo link types
		// adj = new NetworkElementAdjacency("00-21-e1-d6-d8-2c", "OC12-1-11-1",
		// "00-21-e1-d6-d8-2c_OC12-1-11-1", "00-21-e1-d6-d6-70_OC12-1-12-1",
		// "SECT", false);
		// list.add(adj);
		// adj = new NetworkElementAdjacency("00-21-e1-d6-d6-70", "OC12-1-12-1",
		// "00-21-e1-d6-d6-70_OC12-1-12-1",
		// "00-21-e1-d6-d8-2c_OC12-1-11-1", "SECT", false);
		// list.add(adj);

		adj = new NetworkElementAdjacency("00-21-e1-d6-d8-2c", "OC12-1-12-1",
		    "00-21-e1-d6-d8-2c_OC12-1-12-1", "00-21-e1-d6-d5-dc_OC12-1-11-1",
		    "SECT", false);
		list.add(adj);

		adj = new NetworkElementAdjacency("00-21-e1-d6-d5-dc", "OC12-1-11-1",
		    "00-21-e1-d6-d5-dc_OC12-1-11-1", "00-21-e1-d6-d8-2c_OC12-1-12-1",
		    "SECT", false);
		list.add(adj);

		adj = new NetworkElementAdjacency("00-21-e1-d6-d5-dc", "OC12-1-12-1",
		    "00-21-e1-d6-d5-dc_OC12-1-12-1", "00-21-e1-d6-d6-70_OC12-1-11-1",
		    "SECT", false);
		list.add(adj);

		adj = new NetworkElementAdjacency("00-21-e1-d6-d6-70", "OC12-1-11-1",
		    "00-21-e1-d6-d6-70_OC12-1-11-1", "00-21-e1-d6-d5-dc_OC12-1-12-1",
		    "SECT", false);
		list.add(adj);

		DbNetworkElementAdjacency.INSTANCE.add(list);
	}

	/**
     * 
     */
	private static void populateAlarms() throws Exception {
		String[] summaryAlarm1 = new String[] {
		    "<alarm id=\"00-21-E1-D6-D5-DC_0100002916-0008-0001\" severity=\"CR\" occurredTime=\"1266529202321\" serviceId=\"SERVICE-1266529013429\"/>",
		    "<alarm id=\"00-21-E1-D6-D5-DC_0100002917-0004-0149\" severity=\"CR\" occurredTime=\"1266529202474\" serviceId=\"SERVICE-1266529013429\"/>",
		    "<alarm id=\"00-21-E1-D6-D5-DC_0100002918-1005-0049\" severity=\"MJ\" occurredTime=\"1266529205074\" serviceId=\"SERVICE-1266529013429\"/>",
		    "<alarm id=\"00-21-E1-D6-D5-DC_0100002919-0001-0048\" severity=\"MN\" occurredTime=\"1266529206089\" serviceId=\"SERVICE-1266529013429\"/>"

		};
		for (String s : summaryAlarm1) {
			DbLightPathAlarmSummaries.INSTANCE.add("00-21-E1-D6-D5-DC",
			    XmlUtility.createDocumentRoot(s));
		}

		String[] summaryAlarm2 = new String[] {
		    "<alarm id=\"00-21-E1-D6-D8-2C_0100000608-0001-0148\" severity=\"CR\" occurredTime=\"1266529202587\" serviceId=\"SERVICE-1266529013429\"/>",
		    "<alarm id=\"00-21-E1-D6-D8-2C_0100000609-1005-0049\" severity=\"MJ\" occurredTime=\"1266529205283\" serviceId=\"SERVICE-1266529013429\"/>",
		    "<alarm id=\"00-21-E1-D6-D8-2C_0100000610-0001-0048\" severity=\"MN\" occurredTime=\"1266529206038\" serviceId=\"SERVICE-1266529013429\"/>"

		};
		for (String s : summaryAlarm2) {
			DbLightPathAlarmSummaries.INSTANCE.add("00-21-E1-D6-D8-2C",
			    XmlUtility.createDocumentRoot(s));
		}

		String[] summaryAlarm3 = new String[] {

		    "<alarm id=\"00-21-E1-D6-D6-70_0100002426-0001-0285\" severity=\"CR\" occurredTime=\"1266529203360\" serviceId=\"SERVICE-1266529013429\"/>",
		    "<alarm id=\"00-21-E1-D6-D6-70_0100002427-0001-0048\" severity=\"MN\" occurredTime=\"1266529205997\" serviceId=\"SERVICE-1266529013429\"/>"

		};

		for (String s : summaryAlarm3) {
			DbLightPathAlarmSummaries.INSTANCE.add("00-21-E1-D6-D6-70",
			    XmlUtility.createDocumentRoot(s));
		}

		String[] detailedAlarms = new String[] {
		    "<event name=\"alarm\" id=\"00-21-E1-D6-D5-DC_0100002916-0008-0001\" owner=\"TDEFAULT_PROXY\" time=\"1266529202321\" duration=\"0\"><eventInfo notificationType=\"CR\" occurredDate=\"2010-02-18\" occurredTime=\"21-37-41\" /><data><element name=\"description\" value=\"Loss Of Signal\" /><element name=\"aid\" value=\"OC48-1-5-1\" /><element name=\"facility\" value=\"OC48-1-5-1\" /><element name=\"serviceId\" value=\"SERVICE-1266529013429\" /></data><node type=\"OME6\" id=\"00-21-E1-D6-D5-DC\" ip=\"47.134.3.229\" port=\"10001\" tid=\"OME0237\" mode=\"SONET\" status=\"aligned\" /></event>",
		    "<event name=\"alarm\" id=\"00-21-E1-D6-D5-DC_0100002917-0004-0149\" owner=\"TDEFAULT_PROXY\" time=\"1266529202474\" duration=\"0\"><eventInfo notificationType=\"CR\" occurredDate=\"2010-02-18\" occurredTime=\"21-37-41\" /><data><element name=\"description\" value=\"RFI\" /><element name=\"aid\" value=\"OC12-1-11-1\" /><element name=\"facility\" value=\"OC12-1-11-1\" /><element name=\"serviceId\" value=\"SERVICE-1266529013429\" /></data><node type=\"OME6\" id=\"00-21-E1-D6-D5-DC\" ip=\"47.134.3.229\" port=\"10001\" tid=\"OME0237\" mode=\"SONET\" status=\"aligned\" /></event>",
		    "<event name=\"alarm\" id=\"00-21-E1-D6-D8-2C_0100000608-0001-0148\" owner=\"TDEFAULT_PROXY\" time=\"1266529202587\" duration=\"0\"><eventInfo notificationType=\"CR\" occurredDate=\"2010-02-18\" occurredTime=\"21-37-41\" /><data><element name=\"description\" value=\"AIS\" /><element name=\"aid\" value=\"OC12-1-12-1\" /><element name=\"facility\" value=\"OC12-1-12-1\" /><element name=\"serviceId\" value=\"SERVICE-1266529013429\" /></data><node type=\"OME6\" id=\"00-21-E1-D6-D8-2C\" ip=\"47.134.3.228\" port=\"10001\" tid=\"OME0307\" mode=\"SONET\" status=\"aligned\" /></event>",
		    "<event name=\"alarm\" id=\"00-21-E1-D6-D6-70_0100002426-0001-0285\" owner=\"TDEFAULT_PROXY\" time=\"1266529203360\" duration=\"0\"><eventInfo notificationType=\"CR\" occurredDate=\"2010-02-18\" occurredTime=\"21-37-42\" /><data><element name=\"description\" value=\"AIS\" /><element name=\"aid\" value=\"OC192-1-9-1\" /><element name=\"facility\" value=\"OC192-1-9-1\" /><element name=\"serviceId\" value=\"SERVICE-1266529013429\" /></data><node type=\"OME6\" id=\"00-21-E1-D6-D6-70\" ip=\"47.134.3.230\" port=\"10001\" tid=\"OME0039\" mode=\"SONET\" status=\"aligned\" /></event>",
		    "<event name=\"alarm\" id=\"00-21-E1-D6-D5-DC_0100002918-1005-0049\" owner=\"TDEFAULT_PROXY\" time=\"1266529205074\" duration=\"0\"><eventInfo notificationType=\"MJ\" occurredDate=\"2010-02-18\" occurredTime=\"21-37-44\" /><data><element name=\"description\" value=\"Unequipped\" /><element name=\"aid\" value=\"STS1-1-11-1-1\" /><element name=\"facility\" value=\"OC12-1-11-1\" /><element name=\"channel\" value=\"1\" /><element name=\"serviceId\" value=\"SERVICE-1266529013429\" /></data><node type=\"OME6\" id=\"00-21-E1-D6-D5-DC\" ip=\"47.134.3.229\" port=\"10001\" tid=\"OME0237\" mode=\"SONET\" status=\"aligned\" /></event>",
		    "<event name=\"alarm\" id=\"00-21-E1-D6-D8-2C_0100000609-1005-0049\" owner=\"TDEFAULT_PROXY\" time=\"1266529205283\" duration=\"0\"><eventInfo notificationType=\"MJ\" occurredDate=\"2010-02-18\" occurredTime=\"21-37-43\" /><data><element name=\"description\" value=\"Unequipped\" /><element name=\"aid\" value=\"STS1-1-11-1-1\" /><element name=\"facility\" value=\"OC12-1-11-1\" /><element name=\"channel\" value=\"1\" /><element name=\"serviceId\" value=\"SERVICE-1266529013429\" /></data><node type=\"OME6\" id=\"00-21-E1-D6-D8-2C\" ip=\"47.134.3.228\" port=\"10001\" tid=\"OME0307\" mode=\"SONET\" status=\"aligned\" /></event>",
		    "<event name=\"alarm\" id=\"00-21-E1-D6-D6-70_0100002427-0001-0048\" owner=\"TDEFAULT_PROXY\" time=\"1266529205997\" duration=\"0\"><eventInfo notificationType=\"MN\" occurredDate=\"2010-02-18\" occurredTime=\"21-37-45\" /><data><element name=\"description\" value=\"AIS\" /><element name=\"aid\" value=\"STS1-1-12-1-1\" /><element name=\"facility\" value=\"OC12-1-12-1\" /><element name=\"channel\" value=\"1\" /><element name=\"serviceId\" value=\"SERVICE-1266529013429\" /></data><node type=\"OME6\" id=\"00-21-E1-D6-D6-70\" ip=\"47.134.3.230\" port=\"10001\" tid=\"OME0039\" mode=\"SONET\" status=\"aligned\" /></event>",
		    "<event name=\"alarm\" id=\"00-21-E1-D6-D8-2C_0100000610-0001-0048\" owner=\"TDEFAULT_PROXY\" time=\"1266529206038\" duration=\"0\"><eventInfo notificationType=\"MN\" occurredDate=\"2010-02-18\" occurredTime=\"21-37-44\" /><data><element name=\"description\" value=\"AIS\" /><element name=\"aid\" value=\"STS1-1-11-1-1\" /><element name=\"facility\" value=\"OC12-1-11-1\" /><element name=\"channel\" value=\"1\" /><element name=\"serviceId\" value=\"SERVICE-1266529013429\" /></data><node type=\"OME6\" id=\"00-21-E1-D6-D8-2C\" ip=\"47.134.3.228\" port=\"10001\" tid=\"OME0307\" mode=\"SONET\" status=\"aligned\" /></event>",
		    "<event name=\"alarm\" id=\"00-21-E1-D6-D5-DC_0100002919-0001-0048\" owner=\"TDEFAULT_PROXY\" time=\"1266529206089\" duration=\"0\"><eventInfo notificationType=\"MN\" occurredDate=\"2010-02-18\" occurredTime=\"21-37-45\" /><data><element name=\"description\" value=\"AIS\" /><element name=\"aid\" value=\"STS1-1-11-1-1\" /><element name=\"facility\" value=\"OC12-1-11-1\" /><element name=\"channel\" value=\"1\" /><element name=\"serviceId\" value=\"SERVICE-1266529013429\" /></data><node type=\"OME6\" id=\"00-21-E1-D6-D5-DC\" ip=\"47.134.3.229\" port=\"10001\" tid=\"OME0237\" mode=\"SONET\" status=\"aligned\" /></event>", };

		for (String s : detailedAlarms) {
			DbLightPathAlarmDetails.INSTANCE.add(
			    XmlUtility.createDocumentRoot(s));
		}

	}

	private static void populateASchedule(String scheduleName) throws Exception {
		/*
		 * (ACTIVATION_TYPE activation, String identifier, String userName, SCHEDULE
		 * scheduleStatus, long scheduleStartTime, Long scheduleEndTime, long
		 * scheduleDuration, UserInfoType userInfoType, PathType schedulePath,
		 * boolean isRecurring, RecurrenceType recurrenceType, List<ServiceIdType>
		 * serviceIdTypeList)
		 */
		UserType ui = new UserType("admin", new UserGroupName("SystemAdminGroup"),
		    "SystemAdminGroup", "SystemAdminGroup", "SystemAdminResourceGroup",
		    "SystemAdminResourceGroup", "");

		PathType pt = new PathType();
		pt.setCost(-1);
		pt.setHop(-1);
		pt.setMetric(-1);
		pt.setProtectionType(PROTECTION_TYPE.UNPROTECTED);
		pt.setRate(50);
		pt.setSource("00-21-E1-D6-D5-DC");
		EndPointType src = new EndPointType();
		src.setChannelNumber(-1);
		src.setId("00-21-E1-D6-D5-DC_OC48-1-5-1");
		src.setName("OME0237_OC48-1-5-1");
		pt.setSourceEndPoint(src);
		pt.setTarget("00-21-E1-D6-D6-70");
		EndPointType dst = new EndPointType();
		dst.setChannelNumber(-1);
		dst.setId("00-21-E1-D6-D6-70_OC192-1-9-1");
		dst.setName("OME0039_OC192-1-9-1");
		pt.setTargetEndPoint(dst);

		List<DracService> li = new ArrayList<DracService>();
		Schedule a = new Schedule(ACTIVATION_TYPE.RESERVATION_AUTOMATIC,
		    scheduleName, "admin@pcard0ym_1266529013216",
		    SCHEDULE.EXECUTION_SUCCEEDED, 1266529200000L,
		    Long.valueOf(1266529500000L), 300000L, ui, pt, false,
		    new RecurrenceType(), li);
		DbSchedule.INSTANCE.add(a);
	}

	private static void populateAService(String serviceId, String scheduleId,
	    String callId) throws Exception {
		ServiceXml s = new ServiceXml(State.SERVICE.EXECUTION_SUCCEEDED, callId,
		    serviceId, "RESERVATION_AUTOMATIC", "47.134.40.185:8001", scheduleId,
		    "admin@pcard0ym_1266529013216", "false", 1266529200000L,
		    1266529500000L, "admin", null, null, "0", 50, "STS1",
		    "00-21-E1-D6-D5-DC", "00-21-E1-D6-D6-70");

		List<CrossConnection> p = new ArrayList<CrossConnection>();
		Map<String, String> xc = new HashMap<String, String>();

		xc.clear();
		xc.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D5-DC");
		xc.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D5-DC");
		xc.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-5-1");
		xc.put(CrossConnection.TARGET_PORT_AID, "OC12-1-11-1");
		xc.put(CrossConnection.SOURCE_CHANNEL, "1");
		xc.put(CrossConnection.TARGET_CHANNEL, "1");
		xc.put(CrossConnection.RATE, "STS1");
		p.add(new CrossConnection(xc));

		xc.clear();
		xc.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D8-2C");
		xc.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D8-2C");
		xc.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-12-1");
		xc.put(CrossConnection.TARGET_PORT_AID, "OC12-1-11-1");
		xc.put(CrossConnection.SOURCE_CHANNEL, "1");
		xc.put(CrossConnection.TARGET_CHANNEL, "1");
		xc.put(CrossConnection.RATE, "STS1");
		p.add(new CrossConnection(xc));

		xc.clear();
		xc.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D6-70");
		xc.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D6-70");
		xc.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-12-1");
		xc.put(CrossConnection.TARGET_PORT_AID, "OC192-1-9-1");
		xc.put(CrossConnection.SOURCE_CHANNEL, "1");
		xc.put(CrossConnection.TARGET_CHANNEL, "1");
		xc.put(CrossConnection.RATE, "STS1");
		p.add(new CrossConnection(xc));

		s.addPath(XC_TYPE.MAIN, p);

		DbUtilityLpcpScheduler.INSTANCE.addNewService(s);

	}

	private static void populateConsoleUserPrefs() throws Exception {
		Element preferences = XmlUtility
		    .createDocumentRoot("<user id=\"admin\"><preferencesList><pref neLabel=\"INSIDE\" edgeLineStyle=\"LINE_QUAD_CURVE\" edgeColorSelected=\"-256\" edgeColorNetworkDiscovered=\"-16738048\" edgeColorManual=\"-65485\" edgeColorNetworkDiscoveredAndManual=\"-39424\" /></preferencesList></user>");

		DbAdminConsoleUserPreferences.INSTANCE.addUpdate("admin", preferences);
	}

	private static void populateFacilities() throws Exception {
		/**
		 * INSERT INTO `NetworkElementFacility` VALUES
		 * ('00-21-E1-D6-D8-2C_OC12-1-11-1_laye
		 * r1',NULL,'1','11','1','IS','INNI','OME0307_OC12-1-11-1','<WP port=\"1\"
		 * valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-1\"
		 * type=\"OC12\" userLabel=\"label-OME0307_OC12-1-11-1\" id=\"1\"
		 * manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\"
		 * shelf=\"1\" signalingType=\"INNI\" slot=\"11\"
		 * neipForFac=\"47.134.3.228\" layer=\"layer1\" tna=\"OME0307_OC12-1-11-1\"
		 * cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D8-2C\"
		 * metric=\"1\" group=\"none\" primaryState=\"IS\" ps=\"N/A\"
		 * pk=\"00-21-E1-D6-D8-2C_OC12-1-11-1\" /> ')
		 * <p>
		 * ,(
		 * '00-21-E1-D6-D8-2C_OC12-1-12-1',NULL,'1','12','1','OOS-AU','INNI','OME0307_OC12-1-12-1','
		 * < W P port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\"
		 * aid=\"OC12-1-12-1\" type=\"OC12\" userLabel=\"label-OME0307_OC12-1-12-1\"
		 * id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\"
		 * shelf=\"1\" signalingType=\"INNI\" slot=\"12\"
		 * neipForFac=\"47.134.3.228\" layer=\"layer1\" tna=\"OME0307_OC12-1-12-1\"
		 * cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D8-2C\"
		 * metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\"
		 * pk=\"00-21-E1-D6-D8-2C_OC12-1-12-1\" />
		 * '),('00-21-E1-D6-D5-DC_OC12-1-11-1',NULL,'1','11','1','IS-ANR','INNI','OME0237_OC12-1-11-1
		 * ' , '
		 */

		{
			String[] ne1Facilities = new String[] {
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-1\" type=\"OC12\" userLabel=\"label-OME0307_OC12-1-11-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"11\" neipForFac=\"47.134.3.228\" layer=\"layer1\" tna=\"OME0307_OC12-1-11-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D8-2C\" metric=\"1\" group=\"none\" primaryState=\"IS\" ps=\"N/A\" pk=\"00-21-E1-D6-D8-2C_OC12-1-11-1\" />",
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-12-1\" type=\"OC12\" userLabel=\"label-OME0307_OC12-1-12-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"12\" neipForFac=\"47.134.3.228\" layer=\"layer1\" tna=\"OME0307_OC12-1-12-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D8-2C\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D8-2C_OC12-1-12-1\" />" };

			List<Facility> facList = new ArrayList<Facility>();
			for (String f : ne1Facilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-21-E1-D6-D8-2C",
			    "47.134.3.228", 10001, facList);
		}

		{
			String[] ne2Facilities = new String[] {
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-1\" type=\"OC12\" userLabel=\"label-OME0237_OC12-1-11-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"11\" neipForFac=\"47.134.3.229\" layer=\"layer1\" tna=\"OME0237_OC12-1-11-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"IS-ANR\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_OC12-1-11-1\" />",
			    "<WP port=\"2\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-2\" type=\"OC12\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"unassigned\" slot=\"11\" neipForFac=\"47.134.3.229\" layer=\"layer1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" pk=\"00-21-E1-D6-D5-DC_OC12-1-11-2\" ps=\"N/A\" />",
			    "<WP port=\"3\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-3\" type=\"OC12\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"unassigned\" slot=\"11\" neipForFac=\"47.134.3.229\" layer=\"layer1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" pk=\"00-21-E1-D6-D5-DC_OC12-1-11-3\" ps=\"N/A\" />",
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-12-1\" type=\"OC12\" userLabel=\"label-OME0237_OC12-1-12-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"12\" neipForFac=\"47.134.3.229\" layer=\"layer1\" tna=\"OME0237_OC12-1-12-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"IS-ANR\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_OC12-1-12-1\" />",
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC48-1-5-1\" type=\"OC48\" userLabel=\"label-OME0237_OC48-1-5-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"5\" neipForFac=\"47.134.3.229\" layer=\"layer1\" tna=\"OME0237_OC48-1-5-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_OC48-1-5-1\" />",
			    "<WP port=\"2\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC48-1-5-2\" type=\"OC48\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"unassigned\" slot=\"5\" neipForFac=\"47.134.3.229\" layer=\"layer1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" pk=\"00-21-E1-D6-D5-DC_OC48-1-5-2\" ps=\"N/A\" />",
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC192-1-9-1\" type=\"OC192\" userLabel=\"label-OME0237_OC192-1-9-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"9\" neipForFac=\"47.134.3.229\" layer=\"layer1\" tna=\"OME0237_OC192-1-9-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"IS-ANR\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_OC192-1-9-1\" />",
			    "<WP aid=\"OC192-1-4-1\" apsId=\"N/A\" constrain=\"0\" cost=\"1\" domain=\"N/A\" group=\"none\" layer=\"layer1\" manualProvision=\"false\" metric=\"1\" neidForFac=\"00-21-E1-D6-D6-70\" neipForFac=\"127.0.0.1\" neportForFac=\"2610\" pk=\"00-21-E1-D6-D6-70_OC192-1-4-1\" port=\"1\" primaryState=\"OOS-AUMA\" ps=\"N/A\" shelf=\"1\" signalingType=\"UNI\" siteId=\"N/A\" slot=\"4\" srlg=\"N/A\" tna=\"OME0237_OC192-1-4-1\" type=\"OC192\" userLabel=\"N/A\" valid=\"true\" />",
			    // WANs that belong to Eth/Wan EPL pair no longer write
			    // explicitly to the database.
			    "<WP port=\"1\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-1\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0237_ETH-1-1-1\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"0016CA40C336\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.229\" layer=\"layer2\" tna=\"OME0237_ETH-1-1-1\" cost=\"1\" autoNegotiationStatus=\"INPROGRESS\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D5-DC_ETH-1-1-1\" ps=\"N/A\" />",
			    "<WP port=\"2\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-2\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0237_ETH-1-1-2\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.229\" layer=\"layer2\" tna=\"OME0237_ETH-1-1-2\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D5-DC_ETH-1-1-2\" ps=\"N/A\" />",
			    "<WP port=\"3\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-3\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0237_ETH-1-1-3\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.229\" layer=\"layer2\" tna=\"OME0237_ETH-1-1-3\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D5-DC_ETH-1-1-3\" ps=\"N/A\" />",
			    "<WP port=\"4\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-4\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0237_ETH-1-1-4\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.229\" layer=\"layer2\" tna=\"OME0237_ETH-1-1-4\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D5-DC_ETH-1-1-4\" ps=\"N/A\" />",
			    "<WP port=\"4\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-3-1\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0237_ETH-1-3-1\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.229\" layer=\"layer2\" tna=\"OME0237_ETH-1-3-1\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D5-DC_ETH-1-3-1\" ps=\"N/A\" />",
			    "<WP mtu=\"9216\" port=\"4\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH-1-13-4\" type=\"ETH\" userLabel=\"N/A\" L2SS_FACILITY=\"true\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"unassigned\" slot=\"13\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.229\" layer=\"layer2\" tna=\"N/A\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" autoNegotiation=\"DISABLE\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AU\" controlPauseTx=\"DISABLE\" ps=\"N/A\" />",
			    "<WP port=\"1\" mtu=\"9216\" valid=\"true\" interfaceType=\"UNI\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH10G-1-13-1\" type=\"ETH10G\" L2SS_FACILITY=\"true\" userLabel=\"label-OME0237_ETH10G-1-13-1\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"UNI\" slot=\"13\" speed=\"10000\" flowControl=\"NONE\" neipForFac=\"47.134.3.229\" layer=\"layer2\" tna=\"OME0237_ETH10G-1-13-1\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" autoNegotiation=\"DISABLE\" metric=\"1\" controlPauseRx=\"DISABLE\" primaryState=\"OOS-AU\" group=\"none\" controlPauseTx=\"DISABLE\" pk=\"00-21-E1-D6-D5-DC_ETH10G-1-13-1\" ps=\"N/A\" />",
			    "<WP port=\"2\" mtu=\"9216\" valid=\"true\" interfaceType=\"UNI\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH10G-1-13-2\" type=\"ETH10G\" L2SS_FACILITY=\"true\" userLabel=\"label-OME0237_ETH10G-1-13-2\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"UNI\" slot=\"13\" speed=\"10000\" flowControl=\"NONE\" neipForFac=\"47.134.3.229\" layer=\"layer2\" tna=\"OME0237_ETH10G-1-13-2\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" autoNegotiation=\"DISABLE\" metric=\"1\" controlPauseRx=\"DISABLE\" primaryState=\"OOS-AU\" group=\"none\" controlPauseTx=\"DISABLE\" pk=\"00-21-E1-D6-D5-DC_ETH10G-1-13-2\" ps=\"N/A\" />", };

			List<Facility> facList = new ArrayList<Facility>();
			for (String f : ne2Facilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-21-E1-D6-D5-DC",
			    "47.134.3.229", 10001, facList);
		}

		{
			String[] ne3Facilities = new String[] {
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-1\" type=\"OC12\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"11\" neipForFac=\"47.134.3.230\" layer=\"layer1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" pk=\"00-21-E1-D6-D6-70_OC12-1-11-1\" ps=\"N/A\" />",
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-12-1\" type=\"OC12\" userLabel=\"label-OME0039_OC12-1-12-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"12\" neipForFac=\"47.134.3.230\" layer=\"layer1\" tna=\"OME0039_OC12-1-12-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"IS\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_OC12-1-12-1\" />",
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC48-1-5-1\" type=\"OC48\" userLabel=\"label-OME0039_OC48-1-5-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"5\" neipForFac=\"47.134.3.230\" layer=\"layer1\" tna=\"OME0039_OC48-1-5-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_OC48-1-5-1\" />",
			    "<WP port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC192-1-9-1\" type=\"OC192\" userLabel=\"label-OME0039_OC192-1-9-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"9\" neipForFac=\"47.134.3.230\" layer=\"layer1\" tna=\"OME0039_OC192-1-9-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_OC192-1-9-1\" />",
			    "<WP aid=\"OC192-1-4-1\" apsId=\"N/A\" constrain=\"0\" cost=\"1\" domain=\"N/A\" group=\"none\" layer=\"layer1\" manualProvision=\"false\" metric=\"1\" neidForFac=\"00-21-E1-D6-D6-70\" neipForFac=\"127.0.0.1\" neportForFac=\"2610\" pk=\"00-21-E1-D6-D6-70_OC192-1-4-1\" port=\"1\" primaryState=\"OOS-AUMA\" ps=\"N/A\" shelf=\"1\" signalingType=\"UNI\" siteId=\"N/A\" slot=\"4\" srlg=\"N/A\" tna=\"OME0039_OC192-1-4-1\" type=\"OC192\" userLabel=\"N/A\" valid=\"true\" />",
			    "<WP AID=\"ETH10G-1-2-1\" TYPE=\"ETH10G\" actualUnit=\"UNKNOWN\" advertisedDuplex=\"UNKNOWN\" aid=\"ETH10G-1-2-1\" anspeed=\"UNKNOWN\" apsId=\"N/A\" autoNegotiation=\"DISABLE\" autoNegotiationStatus=\"UNKNOWN\" constrain=\"0\" controlPauseRx=\"DISABLE\" controlPauseTx=\"ENABLE\" cost=\"1\" domain=\"N/A\" etherDuplex=\"FULL\" flowControl=\"ASYM\" group=\"none\" isEPL=\"true\" layer=\"layer2\" lcas=\"DISABLE\" manualProvision=\"false\" mapping=\"GFP-F\" metric=\"1\" mtu=\"1600\" neidForFac=\"00-21-E1-D6-D6-70\" neipForFac=\"127.0.0.1\" neportForFac=\"2610\" passControlFrame=\"DISABLE\" physicalAddress=\"UNKNOWN\" pk=\"00-21-E1-D6-D6-70_ETH10G-1-2-1\" port=\"1\" primaryState=\"OOS-AUMA\" provUnit=\"1\" ps=\"N/A\" rate=\"STS3C\" shelf=\"1\" signalingType=\"UNI\" siteId=\"N/A\" slot=\"2\" speed=\"10000\" srlg=\"N/A\" tna=\"OME0039_ETH10G-1-2-1\" txConditioning=\"ENABLE\" type=\"ETH10G\" userLabel=\"N/A\" valid=\"true\" vcat=\"ENABLE\" />",
			    "<WP port=\"1\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-1\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0039_ETH-1-1-1\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"0016CA40C34D\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.230\" layer=\"layer2\" tna=\"OME0039_ETH-1-1-1\" cost=\"1\" autoNegotiationStatus=\"INPROGRESS\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D6-70_ETH-1-1-1\" ps=\"N/A\" />",
			    "<WP port=\"2\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-2\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0039_ETH-1-1-2\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.230\" layer=\"layer2\" tna=\"OME0039_ETH-1-1-2\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D6-70_ETH-1-1-2\" ps=\"N/A\" />",
			    "<WP port=\"3\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-3\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0039_ETH-1-1-3\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.230\" layer=\"layer2\" tna=\"OME0039_ETH-1-1-3\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D6-70_ETH-1-1-3\" ps=\"N/A\" />",
			    "<WP port=\"4\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-4\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0039_ETH-1-1-4\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.230\" layer=\"layer2\" tna=\"OME0039_ETH-1-1-4\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D6-70_ETH-1-1-4\" ps=\"N/A\" />",
			    "<WP port=\"4\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-3-1\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0039_ETH-1-3-1\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.230\" layer=\"layer2\" tna=\"OME0039_ETH-1-3-1\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D6-70_ETH-1-3-1\" ps=\"N/A\" />",
			    "<WP mtu=\"9216\" port=\"4\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH-1-13-4\" type=\"ETH\" userLabel=\"N/A\" L2SS_FACILITY=\"true\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"unassigned\" slot=\"13\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.230\" layer=\"layer2\" tna=\"N/A\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" autoNegotiation=\"DISABLE\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AU\" controlPauseTx=\"DISABLE\" ps=\"N/A\" />",
			    "<WP port=\"1\" mtu=\"9216\" valid=\"true\" interfaceType=\"UNI\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH10G-1-13-1\" type=\"ETH10G\" L2SS_FACILITY=\"true\" userLabel=\"label-OME0039_ETH10G-1-13-1\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"UNI\" slot=\"13\" speed=\"10000\" flowControl=\"NONE\" neipForFac=\"47.134.3.230\" layer=\"layer2\" tna=\"OME0039_ETH10G-1-13-1\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" autoNegotiation=\"DISABLE\" metric=\"1\" controlPauseRx=\"DISABLE\" primaryState=\"OOS-AU\" group=\"none\" controlPauseTx=\"DISABLE\" pk=\"00-21-E1-D6-D6-70_ETH10G-1-13-1\" ps=\"N/A\" />",
			    "<WP port=\"2\" mtu=\"9216\" valid=\"true\" interfaceType=\"UNI\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH10G-1-13-2\" type=\"ETH10G\" L2SS_FACILITY=\"true\" userLabel=\"label-OME0039_ETH10G-1-13-2\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"UNI\" slot=\"13\" speed=\"10000\" flowControl=\"NONE\" neipForFac=\"47.134.3.230\" layer=\"layer2\" tna=\"OME0039_ETH10G-1-13-2\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" autoNegotiation=\"DISABLE\" metric=\"1\" controlPauseRx=\"DISABLE\" primaryState=\"OOS-AU\" group=\"none\" controlPauseTx=\"DISABLE\" pk=\"00-21-E1-D6-D6-70_ETH10G-1-13-2\" ps=\"N/A\" />", };
			List<Facility> facList = new ArrayList<Facility>();
			for (String f : ne3Facilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-21-E1-D6-D6-70",
			    "47.134.3.230", 10001, facList);
		}

		// OME SONMP
		{
			String[] neFacilities = new String[] {
			    "<WP port=\"101\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-2-101\" type=\"WAN\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"2\" signalingType=\"unassigned\" vcat=\"ENABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.78\" layer=\"layer1\" actualUnit=\"UNKNOWN\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" metric=\"1\" primaryState=\"IS\" group=\"none\" ps=\"N/A\" pk=\"00-1B-25-2C-E9-6E_WAN-1-2-101\" />",
			    "<WP port=\"101\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-3-101\" type=\"WAN\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"3\" signalingType=\"unassigned\" vcat=\"ENABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.78\" layer=\"layer1\" actualUnit=\"UNKNOWN\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" metric=\"1\" primaryState=\"IS\" group=\"none\" ps=\"N/A\" pk=\"00-1B-25-2C-E9-6E_WAN-1-3-101\" />",
			    "<WP port=\"101\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-4-101\" type=\"WAN\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"4\" signalingType=\"unassigned\" vcat=\"ENABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.78\" layer=\"layer1\" actualUnit=\"0\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" metric=\"1\" primaryState=\"OOS-AU\" group=\"none\" ps=\"N/A\" pk=\"00-1B-25-2C-E9-6E_WAN-1-4-101\" />",
			    "<WP port=\"101\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-6-101\" type=\"WAN\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"6\" signalingType=\"unassigned\" vcat=\"ENABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.78\" layer=\"layer1\" actualUnit=\"UNKNOWN\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" metric=\"1\" primaryState=\"IS\" group=\"none\" ps=\"N/A\" pk=\"00-1B-25-2C-E9-6E_WAN-1-6-101\" />",
			    "<WP mtu=\"9216\" port=\"1\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"UNKNOWN\" remainedBandwidth=\"1000000\" srlg=\"N/A\" aid=\"ETH-1-2-1\" type=\"ETH\" userLabel=\"N/A\" L2SS_FACILITY=\"true\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" bandwidthThreshold=\"80\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" physicalAddress=\"UNKNOWN\" shelf=\"1\" signalingType=\"unassigned\" slot=\"2\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.78\" layer=\"layer2\" tna=\"N/A\" cost=\"1\" bandwidthUtilization=\"0\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"DISABLE\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"IS\" controlPauseTx=\"DISABLE\" ps=\"N/A\" pk=\"00-1B-25-2C-E9-6E_ETH-1-2-1\" />",
			    "<WP port=\"1\" mtu=\"9216\" valid=\"true\" interfaceType=\"UNI\" constrain=\"0\" advertisedDuplex=\"FULL\" remainedBandwidth=\"1000000\" srlg=\"N/A\" aid=\"ETH-1-4-1\" type=\"ETH\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" bandwidthThreshold=\"80\" neportForFac=\"10001\" anspeed=\"1000\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" physicalAddress=\"0025C3BE4120\" shelf=\"1\" signalingType=\"unassigned\" slot=\"4\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.78\" layer=\"layer2\" tna=\"N/A\" cost=\"1\" bandwidthUtilization=\"0\" autoNegotiationStatus=\"COMPLETED\" apsId=\"N/A\" policing=\"DISABLE\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"DISABLE\" primaryState=\"IS\" group=\"none\" controlPauseTx=\"DISABLE\" pk=\"00-1B-25-2C-E9-6E_ETH-1-4-1\" ps=\"N/A\" />",
			    "<WP mtu=\"9216\" port=\"1\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"FULL\" remainedBandwidth=\"1000000\" srlg=\"N/A\" aid=\"ETH-1-6-1\" type=\"ETH\" userLabel=\"N/A\" L2SS_FACILITY=\"true\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" bandwidthThreshold=\"80\" neportForFac=\"10001\" anspeed=\"1000\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" physicalAddress=\"0016CA40B895\" shelf=\"1\" signalingType=\"unassigned\" slot=\"6\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.78\" layer=\"layer2\" tna=\"N/A\" cost=\"1\" bandwidthUtilization=\"0\" autoNegotiationStatus=\"COMPLETED\" apsId=\"N/A\" policing=\"DISABLE\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"DISABLE\" group=\"none\" primaryState=\"IS\" controlPauseTx=\"DISABLE\" ps=\"N/A\" pk=\"00-1B-25-2C-E9-6E_ETH-1-6-1\" />",
			    "<WP mtu=\"9600\" port=\"3\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH-1-9-3\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"N/A\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"unassigned\" slot=\"9\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.78\" layer=\"layer2\" tna=\"N/A\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"UNKNOWN\" primaryState=\"IS\" group=\"none\" controlPauseTx=\"DISABLE\" ps=\"N/A\" pk=\"00-1B-25-2C-E9-6E_ETH-1-9-3\" />",
			    "<WP port=\"3\" mtu=\"9216\" valid=\"true\" interfaceType=\"UNI\" constrain=\"0\" advertisedDuplex=\"FULL\" srlg=\"N/A\" aid=\"ETH-1-12-3\" type=\"ETH\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"1000\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"001B25314BCC\" signalingType=\"unassigned\" slot=\"12\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.78\" layer=\"layer2\" tna=\"N/A\" cost=\"1\" autoNegotiationStatus=\"COMPLETED\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"DISABLE\" primaryState=\"IS\" group=\"none\" controlPauseTx=\"DISABLE\" pk=\"00-1B-25-2C-E9-6E_ETH-1-12-3\" ps=\"N/A\" />",
			    "<WP mtu=\"9216\" port=\"4\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH-1-12-4\" type=\"ETH\" userLabel=\"N/A\" L2SS_FACILITY=\"true\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"unassigned\" slot=\"12\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.78\" layer=\"layer2\" tna=\"N/A\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-1B-25-2C-E9-6E\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"IS\" controlPauseTx=\"DISABLE\" ps=\"N/A\" pk=\"00-1B-25-2C-E9-6E_ETH-1-12-4\" />",
			    "<WP IS_L2SS_FACILITY=\"true\" L2SS_FACILITY=\"true\" advertisedDuplex=\"FULL\" aid=\"ETH-1-3-1\" anspeed=\"1000\" apsId=\"N/A\" autoNegotiation=\"ENABLE\" autoNegotiationStatus=\"COMPLETED\" bandwidthThreshold=\"80\" bandwidthUtilization=\"0\" constrain=\"0\" controlPauseRx=\"DISABLE\" controlPauseTx=\"DISABLE\" cost=\"1\" domain=\"N/A\" encapsulationType=\"8100\" etherDuplex=\"FULL\" flowControl=\"NONE\" group=\"none\" id=\"2\" interfaceType=\"UNI\" layer=\"layer2\" manualProvision=\"false\" metric=\"1\" mtu=\"9216\" neidForFac=\"00-1B-25-2C-E9-6E\" neipForFac=\"127.0.0.1\" neportForFac=\"2883\" physicalAddress=\"0016CA403D68\" pk=\"00-1B-25-2C-E9-6E_ETH-1-3-1\" policing=\"DISABLE\" port=\"1\" primaryState=\"IS\" priorityMode=\"PBITS\" ps=\"N/A\" remainedBandwidth=\"1000000\" shelf=\"1\" signalingType=\"ENNI\" siteId=\"N/A\" slot=\"3\" speed=\"1000\" srlg=\"N/A\" tna=\"OME0171-SONMP_ETH-1-3-1\" type=\"ETH\" userLabel=\"N/A\" valid=\"true\" />",
			    "<WP advertisedDuplex=\"FULL\" aid=\"ETH-1-9-4\" anspeed=\"1000\" apsId=\"N/A\" autoNegotiation=\"ENABLE\" autoNegotiationStatus=\"COMPLETED\" constrain=\"0\" controlPauseRx=\"DISABLE\" controlPauseTx=\"DISABLE\" cost=\"1\" domain=\"N/A\" encapsulationType=\"8100\" etherDuplex=\"FULL\" flowControl=\"NONE\" group=\"none\" id=\"2\" interfaceType=\"UNI\" layer=\"layer2\" manualProvision=\"false\" metric=\"1\" mtu=\"9600\" neidForFac=\"00-1B-25-2C-E9-6E\" neipForFac=\"127.0.0.1\" neportForFac=\"2883\" passControlFrame=\"DISABLE\" physicalAddress=\"98D88CFF5176\" pk=\"00-1B-25-2C-E9-6E_ETH-1-9-4\" policing=\"ENABLE\" port=\"4\" primaryState=\"IS\" ps=\"N/A\" shelf=\"1\" signalingType=\"UNI\" siteId=\"N/A\" slot=\"9\" speed=\"1000\" srlg=\"N/A\" tna=\"OME0171-SONMP_ETH-1-9-4\" type=\"ETH\" userLabel=\"N/A\" valid=\"true\" />" };

			List<Facility> facList = new ArrayList<Facility>();
			for (String f : neFacilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-1B-25-2C-E9-6E",
			    "47.134.3.78", 10001, facList);
		}

		// CPL
		{
			String[] neFacilities = new String[] {
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-1-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"2\" neipForFac=\"145.145.69.14\" layer=\"layer0\" tna=\"ASD001A-CPL01_LIM-1-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-70-9B\" metric=\"1\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-70-9B_LIM-1-2-5\" />",
			    "<WP port=\"3\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-1-12-3\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.14\" layer=\"layer0\" tna=\"ASD001A-CPL01_CMD8-1-12-3\" cost=\"1\" wavelength=\"155817\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-70-9B\" metric=\"1\" active=\"false\" primaryState=\"IS\" group=\"none\" pk=\"00-0F-CD-32-70-9B_CMD8-1-12-3\" />",
			    "<WP port=\"11\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-1-12-11\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.14\" layer=\"layer0\" tna=\"ASD001A-CPL01_CMD8-1-12-11\" cost=\"1\" wavelength=\"155979\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-70-9B\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-70-9B_CMD8-1-12-11\" />",
			    "<WP port=\"15\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-1-12-15\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.14\" layer=\"layer0\" tna=\"ASD001A-CPL01_CMD8-1-12-15\" cost=\"1\" wavelength=\"156061\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-70-9B\" metric=\"1\" active=\"false\" primaryState=\"IS\" group=\"none\" pk=\"00-0F-CD-32-70-9B_CMD8-1-12-15\" />",
			    "<WP port=\"17\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-1-12-17\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.14\" layer=\"layer0\" tna=\"ASD001A-CPL01_CMD8-1-12-17\" cost=\"1\" wavelength=\"156101\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-70-9B\" metric=\"1\" active=\"true\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-70-9B_CMD8-1-12-17\" />", };

			List<Facility> facList = new ArrayList<Facility>();
			for (String f : neFacilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-0F-CD-32-70-9B",
			    "145.145.69.14", 10001, facList);

		}

		{
			String[] neFacilities = new String[] {
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-10-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"10\" signalingType=\"INNI\" slot=\"2\" neipForFac=\"145.145.69.16\" layer=\"layer0\" tna=\"ASD001A-CPL2P_LIM-10-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-AB-1C\" metric=\"1\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-AB-1C_LIM-10-2-5\" />",
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-9-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"9\" slot=\"2\" signalingType=\"INNI\" neipForFac=\"145.145.69.16\" layer=\"layer0\" tna=\"ASD001A-CPL2P_LIM-9-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-AB-1C\" metric=\"1\" primaryState=\"IS\" group=\"none\" pk=\"00-0F-CD-32-AB-1C_LIM-9-2-5\" />",
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-13-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"13\" slot=\"2\" signalingType=\"INNI\" neipForFac=\"145.145.69.16\" layer=\"layer0\" tna=\"ASD001A-CPL2P_LIM-13-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-AB-1C\" metric=\"1\" primaryState=\"IS\" group=\"none\" pk=\"00-0F-CD-32-AB-1C_LIM-13-2-5\" />", };

			List<Facility> facList = new ArrayList<Facility>();
			for (String f : neFacilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-0F-CD-32-AB-1C",
			    "145.145.69.16", 10001, facList);
		}

		{
			String[] neFacilities = new String[] {
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-14-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"14\" signalingType=\"INNI\" slot=\"2\" neipForFac=\"145.145.69.23\" layer=\"layer0\" tna=\"ASD001A-CPL14_LIM-14-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-16-CA-3F-D1-6F\" metric=\"1\" group=\"none\" primaryState=\"IS\" pk=\"00-16-CA-3F-D1-6F_LIM-14-2-5\" />",
			    "<WP port=\"3\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-14-12-3\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"14\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.23\" layer=\"layer0\" tna=\"ASD001A-CPL14_CMD8-14-12-3\" cost=\"1\" wavelength=\"155817\" siteId=\"N/A\" neidForFac=\"00-16-CA-3F-D1-6F\" metric=\"1\" active=\"false\" primaryState=\"IS\" group=\"none\" pk=\"00-16-CA-3F-D1-6F_CMD8-14-12-3\" />",
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-14-12-5\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"14\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.23\" layer=\"layer0\" tna=\"ASD001A-CPL14_CMD8-14-12-5\" cost=\"1\" wavelength=\"155858\" siteId=\"N/A\" neidForFac=\"00-16-CA-3F-D1-6F\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-16-CA-3F-D1-6F_CMD8-14-12-5\" />",
			    "<WP port=\"7\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-14-12-7\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"14\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.23\" layer=\"layer0\" tna=\"ASD001A-CPL14_CMD8-14-12-7\" cost=\"1\" wavelength=\"155898\" siteId=\"N/A\" neidForFac=\"00-16-CA-3F-D1-6F\" metric=\"1\" active=\"true\" group=\"none\" primaryState=\"IS\" pk=\"00-16-CA-3F-D1-6F_CMD8-14-12-7\" />",
			    "<WP port=\"9\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-14-12-9\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"14\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.23\" layer=\"layer0\" tna=\"ASD001A-CPL14_CMD8-14-12-9\" cost=\"1\" wavelength=\"155939\" siteId=\"N/A\" neidForFac=\"00-16-CA-3F-D1-6F\" metric=\"1\" active=\"true\" group=\"none\" primaryState=\"IS\" pk=\"00-16-CA-3F-D1-6F_CMD8-14-12-9\" />",
			    "<WP port=\"13\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-14-12-13\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"14\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.23\" layer=\"layer0\" tna=\"ASD001A-CPL14_CMD8-14-12-13\" cost=\"1\" wavelength=\"156020\" siteId=\"N/A\" neidForFac=\"00-16-CA-3F-D1-6F\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-16-CA-3F-D1-6F_CMD8-14-12-13\" />",
			    "<WP port=\"15\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-14-12-15\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"14\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.23\" layer=\"layer0\" tna=\"ASD001A-CPL14_CMD8-14-12-15\" cost=\"1\" wavelength=\"156061\" siteId=\"N/A\" neidForFac=\"00-16-CA-3F-D1-6F\" metric=\"1\" active=\"true\" group=\"none\" primaryState=\"IS\" pk=\"00-16-CA-3F-D1-6F_CMD8-14-12-15\" />",
			    "<WP port=\"17\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-14-12-17\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"14\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.23\" layer=\"layer0\" tna=\"ASD001A-CPL14_CMD8-14-12-17\" cost=\"1\" wavelength=\"156101\" siteId=\"N/A\" neidForFac=\"00-16-CA-3F-D1-6F\" metric=\"1\" active=\"true\" group=\"none\" primaryState=\"IS\" pk=\"00-16-CA-3F-D1-6F_CMD8-14-12-17\" />", };

			List<Facility> facList = new ArrayList<Facility>();
			for (String f : neFacilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-16-CA-3F-D1-6F",
			    "145.145.69.23", 10001, facList);
		}

		{
			String[] neFacilities = new String[] {
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-2-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"2\" signalingType=\"INNI\" slot=\"2\" neipForFac=\"145.145.65.231\" layer=\"layer0\" tna=\"ASD002A-CPL02_LIM-2-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-15-08\" metric=\"1\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-15-08_LIM-2-2-5\" />",
			    "<WP port=\"3\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-2-12-3\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"2\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.65.231\" layer=\"layer0\" tna=\"ASD002A-CPL02_CMD8-2-12-3\" cost=\"1\" wavelength=\"155817\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-15-08\" metric=\"1\" active=\"false\" primaryState=\"IS\" group=\"none\" pk=\"00-0F-CD-32-15-08_CMD8-2-12-3\" />",
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-2-12-5\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"2\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.65.231\" layer=\"layer0\" tna=\"ASD002A-CPL02_CMD8-2-12-5\" cost=\"1\" wavelength=\"155858\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-15-08\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-15-08_CMD8-2-12-5\" />",
			    "<WP port=\"9\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-2-12-9\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"2\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.65.231\" layer=\"layer0\" tna=\"ASD002A-CPL02_CMD8-2-12-9\" cost=\"1\" wavelength=\"155939\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-15-08\" metric=\"1\" active=\"true\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-15-08_CMD8-2-12-9\" />",
			    "<WP port=\"11\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-2-12-11\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"2\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.65.231\" layer=\"layer0\" tna=\"ASD002A-CPL02_CMD8-2-12-11\" cost=\"1\" wavelength=\"155979\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-15-08\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-15-08_CMD8-2-12-11\" />",
			    "<WP port=\"13\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-2-12-13\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"2\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.65.231\" layer=\"layer0\" tna=\"ASD002A-CPL02_CMD8-2-12-13\" cost=\"1\" wavelength=\"156020\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-15-08\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-15-08_CMD8-2-12-13\" />",
			    "<WP port=\"15\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-2-12-15\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"2\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.65.231\" layer=\"layer0\" tna=\"ASD002A-CPL02_CMD8-2-12-15\" cost=\"1\" wavelength=\"156061\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-15-08\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-15-08_CMD8-2-12-15\" />", };

			List<Facility> facList = new ArrayList<Facility>();
			for (String f : neFacilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-0F-CD-32-15-08",
			    "145.145.65.231", 10001, facList);
		}
		{
			String[] neFacilities = new String[] {
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-11-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"11\" signalingType=\"INNI\" slot=\"2\" neipForFac=\"145.145.65.230\" layer=\"layer0\" tna=\"ASD002A-CPL2P_LIM-11-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-75-F5\" metric=\"1\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-75-F5_LIM-11-2-5\" />",
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-9-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"9\" signalingType=\"INNI\" slot=\"2\" neipForFac=\"145.145.65.230\" layer=\"layer0\" tna=\"ASD002A-CPL2P_LIM-9-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-75-F5\" metric=\"1\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-75-F5_LIM-9-2-5\" />",
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-10-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"10\" signalingType=\"INNI\" slot=\"2\" neipForFac=\"145.145.65.230\" layer=\"layer0\" tna=\"ASD002A-CPL2P_LIM-10-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-0F-CD-32-75-F5\" metric=\"1\" group=\"none\" primaryState=\"IS\" pk=\"00-0F-CD-32-75-F5_LIM-10-2-5\" />", };
			List<Facility> facList = new ArrayList<Facility>();
			for (String f : neFacilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-0F-CD-32-75-F5",
			    "145.145.65.230", 10001, facList);
		}
		{
			String[] neFacilities = new String[] {
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"LIM-1-2-5\" type=\"LIM\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"2\" neipForFac=\"145.145.69.249\" layer=\"layer0\" tna=\"ASD003A-CPL01_LIM-1-2-5\" cost=\"1\" siteId=\"N/A\" neidForFac=\"00-17-D1-9E-2A-ED\" metric=\"1\" group=\"none\" primaryState=\"IS\" pk=\"00-17-D1-9E-2A-ED_LIM-1-2-5\" />",
			    "<WP port=\"3\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-1-12-3\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.249\" layer=\"layer0\" tna=\"ASD003A-CPL01_CMD8-1-12-3\" cost=\"1\" wavelength=\"155817\" siteId=\"N/A\" neidForFac=\"00-17-D1-9E-2A-ED\" metric=\"1\" active=\"false\" primaryState=\"IS\" group=\"none\" pk=\"00-17-D1-9E-2A-ED_CMD8-1-12-3\" />",
			    "<WP port=\"5\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-1-12-5\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.249\" layer=\"layer0\" tna=\"ASD003A-CPL01_CMD8-1-12-5\" cost=\"1\" wavelength=\"155858\" siteId=\"N/A\" neidForFac=\"00-17-D1-9E-2A-ED\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-17-D1-9E-2A-ED_CMD8-1-12-5\" />",
			    "<WP port=\"7\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-1-12-7\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.249\" layer=\"layer0\" tna=\"ASD003A-CPL01_CMD8-1-12-7\" cost=\"1\" wavelength=\"155898\" siteId=\"N/A\" neidForFac=\"00-17-D1-9E-2A-ED\" metric=\"1\" active=\"true\" group=\"none\" primaryState=\"IS\" pk=\"00-17-D1-9E-2A-ED_CMD8-1-12-7\" />",
			    "<WP port=\"11\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-1-12-11\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.249\" layer=\"layer0\" tna=\"ASD003A-CPL01_CMD8-1-12-11\" cost=\"1\" wavelength=\"155979\" siteId=\"N/A\" neidForFac=\"00-17-D1-9E-2A-ED\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-17-D1-9E-2A-ED_CMD8-1-12-11\" />",
			    "<WP port=\"15\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"CMD8-1-12-15\" type=\"CMD8\" userLabel=\"N/A\" id=\"0\" manualProvision=\"false\" rate=\"STS192C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"12\" neipForFac=\"145.145.69.249\" layer=\"layer0\" tna=\"ASD003A-CPL01_CMD8-1-12-15\" cost=\"1\" wavelength=\"156061\" siteId=\"N/A\" neidForFac=\"00-17-D1-9E-2A-ED\" metric=\"1\" active=\"false\" group=\"none\" primaryState=\"IS\" pk=\"00-17-D1-9E-2A-ED_CMD8-1-12-15\" />", };
			List<Facility> facList = new ArrayList<Facility>();
			for (String f : neFacilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-17-D1-9E-2A-ED",
			    "145.145.69.249", 10001, facList);
		}

		// Force 10
		{
			String[] neFacilities = new String[] {
			    "<WP InUseVlans=\"1,100,200,201\" aid=\"GigabitEthernet-1-0-1\" constrain=\"0\" cost=\"1\" domain=\"N/A\" group=\"none\" id=\"2\" ifAdminStatus=\"down\" ifDescr=\"\" ifHardwareType=\"Force10Eth\" ifIndex=\"34128387\" ifLineSpeed=\"auto\" ifOperStatus=\"down\" layer=\"layer2\" metric=\"1\" neidForFac=\"00-01-E8-D6-37-A8\" neipForFac=\"127.0.0.1\" neportForFac=\"3176\" pk=\"00-01-E8-D6-37-A8_GigabitEthernet-1-0-1\" port=\"1\" primaryState=\"down/down\" shelf=\"1\" signalingType=\"UNI\" siteId=\"N/A\" slot=\"0\" slrg=\"N/A\" speed=\"1000\" srlg=\"N/A\" tna=\"Asd001A_F25S1T_GigabitEthernet-1-0-1\" type=\"GigabitEthernet\" userLabel=\"N/A\" valid=\"true\" vcat=\"DISABLE\" />",
			    "<WP InUseVlans=\"1,100,200,201\" aid=\"TenGigabitEthernet-1-0-28\" constrain=\"0\" cost=\"1\" domain=\"N/A\" group=\"none\" id=\"2\" ifAdminStatus=\"down\" ifDescr=\"\" ifHardwareType=\"Force10Eth\" ifIndex=\"41222659\" ifLineSpeed=\"10000 Mbit\" ifOperStatus=\"down\" layer=\"layer2\" metric=\"1\" neidForFac=\"00-01-E8-D6-37-A8\" neipForFac=\"127.0.0.1\" neportForFac=\"3176\" pk=\"00-01-E8-D6-37-A8_TenGigabitEthernet-1-0-28\" port=\"28\" primaryState=\"down/down\" shelf=\"1\" signalingType=\"INNI\" siteId=\"N/A\" slot=\"0\" slrg=\"N/A\" speed=\"10000\" srlg=\"N/A\" tna=\"Asd001A_F25S1T_TenGigabitEthernet-1-0-28\" type=\"TenGigabitEthernet\" userLabel=\"N/A\" valid=\"true\" vcat=\"DISABLE\" />",
			    "<WP InUseVlans=\"1,100,200,201\" aid=\"GigabitEthernet-1-0-10\" constrain=\"0\" cost=\"1\" domain=\"N/A\" group=\"none\" ifAdminStatus=\"down\" ifDescr=\"\" ifHardwareType=\"Force10Eth\" ifIndex=\"36487683\" ifLineSpeed=\"auto\" ifOperStatus=\"down\" layer=\"layer2\" metric=\"1\" neidForFac=\"00-01-E8-D6-37-A8\" neipForFac=\"127.0.0.1\" neportForFac=\"3176\" pk=\"00-01-E8-D6-37-A8_GigabitEthernet-1-0-10\" port=\"10\" primaryState=\"down/down\" shelf=\"1\" signalingType=\"UNI\" siteId=\"N/A\" slot=\"0\" speed=\"1000\" srlg=\"N/A\" tna=\"Asd001A_F25S1T_GigabitEthernet-1-0-10\" type=\"GigabitEthernet\" userLabel=\"N/A\" valid=\"true\" vcat=\"DISABLE\" />",
			//
			};
			List<Facility> facList = new ArrayList<Facility>();
			for (String f : neFacilities) {
				facList.add(new Facility(DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(f))));
			}
			DbNetworkElementFacility.INSTANCE.addFacility("00-01-E8-D6-37-A8",
			    "127.0.0.1", 22, facList);
		}
	}

	/**
     * 
     */
	private static void populateManualAdjacency() throws Exception {
		List<NetworkElementAdjacency> list = new ArrayList<NetworkElementAdjacency>();

		// Force 10 to OME link
		NetworkElementAdjacency adj = new NetworkElementAdjacency(
		    "00-01-E8-D6-37-A8", "TenGigabitEthernet-1-0-28",
		    "00-01-E8-D6-37-A8_TenGigabitEthernet-1-0-28",
		    "00-1B-25-2C-E9-6E_ETH-1-3-1",
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		list.add(adj);

		adj = new NetworkElementAdjacency("00-1B-25-2C-E9-6E", "ETH-1-3-1",
		    "00-1B-25-2C-E9-6E_ETH-1-3-1",
		    "00-01-E8-D6-37-A8_TenGigabitEthernet-1-0-28",
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		list.add(adj);

		adj = new NetworkElementAdjacency("00-21-E1-D6-D8-2C", "OC12-1-11-1",
		    "00-21-E1-D6-D8-2C_OC12-1-11-1", "00-21-E1-D6-D6-70_OC12-1-12-1",
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		list.add(adj);
		adj = new NetworkElementAdjacency("00-21-E1-D6-D6-70", "OC12-1-12-1",
		    "00-21-E1-D6-D6-70_OC12-1-12-1", "00-21-E1-D6-D8-2C_OC12-1-11-1",
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		list.add(adj);

		// Providing a mix of topo link types
		// adj = new NetworkElementAdjacency("00-21-E1-D6-D5-DC", "OC12-1-12-1",
		// "00-21-E1-D6-D5-DC_OC12-1-12-1",
		// "00-21-E1-D6-D6-70_OC12-1-11-1",
		// DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		// list.add(adj);
		// adj = new NetworkElementAdjacency("00-21-E1-D6-D6-70", "OC12-1-11-1",
		// "00-21-E1-D6-D6-70_OC12-1-11-1",
		// "00-21-E1-D6-D5-DC_OC12-1-12-1",
		// DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		// list.add(adj);

		adj = new NetworkElementAdjacency("00-21-E1-D6-D5-DC", "OC12-1-11-1",
		    "00-21-E1-D6-D5-DC_OC12-1-11-1", "00-21-E1-D6-D8-2C_OC12-1-12-1",
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		list.add(adj);

		adj = new NetworkElementAdjacency("00-21-E1-D6-D8-2C", "OC12-1-12-1",
		    "00-21-E1-D6-D8-2C_OC12-1-12-1", "00-21-E1-D6-D5-DC_OC12-1-11-1",
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		list.add(adj);

		adj = new NetworkElementAdjacency("00-21-E1-D6-D8-2C", "OC192-1-9-1",
		    "00-21-E1-D6-D8-2C_OC192-1-9-1", "00-21-E1-D6-D6-70_OC192-1-9-1",
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		list.add(adj);

		adj = new NetworkElementAdjacency("00-21-E1-D6-D6-70", "OC192-1-9-1",
		    "00-21-E1-D6-D6-70_OC192-1-9-1", "00-21-E1-D6-D8-2C_OC192-1-9-1",
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		list.add(adj);

		DbNetworkElementAdjacency.INSTANCE.add(list);
	}

	/**
	 * @throws Exception
	 */
	private static void populateNes() throws Exception {
		/**
		 * INSERT INTO `NetworkElementV2` VALUES
		 * <p>
		 * (
		 * '47.134.3.228_10001',1,'tl1','00-21-E1-D6-D8-2C','TDEFAULT_PROXY','SONET',1,'ad5699d17bf7fce6','
		 * a l i g n e d ' , ' O M E 0 3 0 7 ' , ' O M E 6 ' , ' A D M I N ' , ' R E
		 * L 0 6 0 0 Z . P K ' ) ,
		 * <p>
		 * (
		 * '47.134.3.229_10001',1,'tl1','00-21-E1-D6-D5-DC','TDEFAULT_PROXY','SONET',1,'ad5699d17bf7fce6','
		 * a l i g n e d ' , ' O M E 0 2 3 7 ' , ' O M E 6 ' , ' A D M I N ' , ' R E
		 * L 0 6 0 0 Z . P K ' ) ,
		 * <p>
		 * (
		 * '47.134.3.230_10001',1,'tl1','00-21-E1-D6-D6-70','TDEFAULT_PROXY','SONET',1,'ad5699d17bf7fce6','
		 * a l i g n e d ' , ' O M E 0 0 3 9 ' , ' O M E 6 ' , ' A D M I N ' , ' R E
		 * L 0 6 0 0 Z . P K ' )
		 */

		NetworkElementHolder n1 = new NetworkElementHolder("47.134.3.228", "10001",
		    "ADMIN", NeType.OME6, "OME0307", NeStatus.NE_ALIGNED, null,
		    CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
		    NETWORK_ELEMENT_MODE.SONET, "TDEFAULT_PROXY", "00-21-E1-D6-D8-2C",
		    PROTOCOL_TYPE.NETL1_PROTOCOL, true, "Unknown", "REL0600Z.PK", null, null);

		NetworkElementHolder n2 = new NetworkElementHolder("47.134.3.229", "10001",
		    "ADMIN", NeType.OME6, "OME0237", NeStatus.NE_ALIGNED, null,
		    CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
		    NETWORK_ELEMENT_MODE.SONET, "TDEFAULT_PROXY", "00-21-E1-D6-D5-DC",
		    PROTOCOL_TYPE.NETL1_PROTOCOL, true, "Unknown", "REL0600Z.PK", null, null);

		NetworkElementHolder n3 = new NetworkElementHolder("47.134.3.230", "10001",
		    "ADMIN", NeType.OME6, "OME0039", NeStatus.NE_ALIGNED, null,
		    CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
		    NETWORK_ELEMENT_MODE.SONET, "TDEFAULT_PROXY", "00-21-E1-D6-D6-70",
		    PROTOCOL_TYPE.NETL1_PROTOCOL, true, "Unknown", "REL0600Z.PK", null, null);

		// Force 10
		NetworkElementHolder n4 = new NetworkElementHolder("127.0.0.1", "22",
		    "ADMIN", NeType.FORCE10, "", NeStatus.NE_ALIGNED, null, CryptoWrapper
		        .INSTANCE.encrypt("ADMIN"), 1, NETWORK_ELEMENT_MODE.Unknown,
		    "TDEFAULT_PROXY", "00-01-E8-D6-37-A8", PROTOCOL_TYPE.FORCE10_PROTOCOL,
		    true, "E1200i", "8.3.1.1", null, null);

		DbNetworkElement.INSTANCE.add(n1);
		DbNetworkElement.INSTANCE.add(n2);
		DbNetworkElement.INSTANCE.add(n3);
		DbNetworkElement.INSTANCE.add(n4);
	}

	private static void populateSchedulesAndServices() throws Exception {
		String scheduleName = "SCHEDULE-11111111";
		populateASchedule(scheduleName);
		for (int i = 1; i < 50; i++) {
			populateAService("SERVICE-" + Integer.toString(i), scheduleName,
			    "b928862f-" + Integer.toString(i));
		}

		scheduleName = "SCHEDULE-22222222";
		populateASchedule(scheduleName);
		for (int i = 50; i < 100; i++) {
			populateAService("SERVICE-" + Integer.toString(i), scheduleName,
			    "b928862f-" + Integer.toString(i));
		}

		scheduleName = "SCHEDULE-33333333";
		populateASchedule(scheduleName);
		for (int i = 100; i < 150; i++) {
			populateAService("SERVICE-" + Integer.toString(i), scheduleName,
			    "b928862f-" + Integer.toString(i));
		}
	}

	private static void populateTestLogs() {
		for (int i = 0; i < 200; i++) {
			DbLog.INSTANCE.generateLog(
			    new LogRecord(null, null, null, null,
			        LogKeyEnum.KEY_A_LOG_FOR_TESTING_ONLY));
		}
	}

	private static void populateUsersUserGroupsResourceGroups() throws Exception {
		// Non-default users etc.

		/*-********************************
		 *  Test Set #1
		 ********************************-*/

		// TestUserGroup1
		String testUserGroup1String = "<userGroup creationDate=\"2000-08-02T12:12:12-04:00\" "
		    + "lastModificationUserID=\"admin\" lastModifiedDate=\"2000-08-02T12:12:13-04:00\" name=\"TestUserGroup1\"> "
		    + "<userGroupType>User</userGroupType> "
		    + "<defaultUserGroup>false</defaultUserGroup> "
		    + "<groupPolicy> "
		    + "<bandwidthControlRule ruleID=\"1159803989702\"> <maximumServiceSize/> <maximumServiceDuration/> <maximumServiceBandwidth/> <maximumAggregateServiceSize/> "
		    + "</bandwidthControlRule> </groupPolicy> "
		    + "<membership> <createdByMemberName>SystemAdminGroup</createdByMemberName> <memberUserID>TestUser1</memberUserID> <memberResourceGroupName>TestResourceGroup1</memberResourceGroupName> </membership> "
		    + "</userGroup>";
		UserGroupProfile testUserGroup1 = new UserGroupProfile();
		testUserGroup1.fromXML(XmlUtility.createDocumentRoot(testUserGroup1String));
		DbUserGroupProfile.INSTANCE.createUserGroupProfile(testUserGroup1);

		// TestUser1
		String testUser1String = "<user creationDate=\"2010-05-30T09:20:10-04:00\" lastModifiedDate=\"2010-06-29T09:20:10-04:00\" userID=\"TestUser1\"> "
		    + "<authentication> <authenticationType>Internal</authenticationType>  <internalAccountData>  <userPassword>292c2cdcb5f669a8</userPassword> "
		    + " <lastPasswordChanged>2002-05-30T09:20:10-04:00</lastPasswordChanged> <expirationDate>2099-05-30T09:20:10-04:00</expirationDate> "
		    + " </internalAccountData> <authenticationState>valid</authenticationState> <lastAuthenticationStateChange>2002-05-30T09:30:10-04:00</lastAuthenticationStateChange> "
		    + " <userAccountPolicy> <localPasswordPolicy>  <passwordAging>100</passwordAging>  <passwordExpirationNotification></passwordExpirationNotification> "
		    + " <passwordHistorySize></passwordHistorySize> <invalidPasswords></invalidPasswords> <passwordRules></passwordRules> "
		    + " </localPasswordPolicy> <dormantPeriod>0</dormantPeriod> <inactivityPeriod>900</inactivityPeriod>  <maxInvalidLoginAttempts>3</maxInvalidLoginAttempts> "
		    + " <lockoutPeriod>300</lockoutPeriod> <lockedClientIPs></lockedClientIPs> </userAccountPolicy> <auditData> "
		    + " <lastLoginAddress>localhost</lastLoginAddress> <numOfInvalidAttempts>0</numOfInvalidAttempts> <locationOfInvalidAttempts></locationOfInvalidAttempts> "
		    + " </auditData> <wsdlCredential>292c2cdcb5f669a8</wsdlCredential> "
		    + "</authentication> "
		    + "<accountStatus> <accountState>enabled</accountState> <disabledReason></disabledReason> </accountStatus>"
		    + "<userData> <commonName>TestUser1</commonName> <givenName>TestUser1</givenName> <surname></surname> <telephoneNumber></telephoneNumber> <mail></mail> "
		    + " <postalAddress></postalAddress> <description></description> </userData> "
		    + "<organization> <description></description> <organizationName></organizationName> <organizationalUnitName></organizationalUnitName> "
		    + " <owner></owner> <seeAlso></seeAlso> <businessCategory></businessCategory> </organization> "
		    + "<membership> <memberUserGroupName>TestUserGroup1</memberUserGroupName> </membership> <preferences></preferences> </user>";
		UserProfile testUser1 = new UserProfile();
		testUser1.fromXML(XmlUtility.createDocumentRoot(testUser1String));
		DbUser.INSTANCE.createUserProfile(testUser1);

		// TestResourceGroup1
		String testResourceGroup1String = "<resourceGroup name=\"TestResourceGroup1\" "
		    + "creationDate=\"2000-08-02T12:12:12-04:00\" lastModifiedDate=\"2000-08-02T12:12:12-04:00\" lastModificationUserID=\"admin\"> "
		    + "  <defaultResourceGroup>false</defaultResourceGroup> "
		    + " <resourceList>"
		    + " <endpoint resourceID=\"00-21-E1-D6-D5-DC_OC48-1-5-1\" resourceType=\"endpoint\"></endpoint>"
		    + " <endpoint resourceID=\"00-21-E1-D6-D6-70_OC48-1-5-1\" resourceType=\"endpoint\"></endpoint>"
		    + "</resourceList> "
		    + "  <resourcePolicy> <resourceStateRule>closed</resourceStateRule> </resourcePolicy> <membership> "
		    + " <createdByMemberName>SystemAdminResourceGroup</createdByMemberName> <memberUserGroupName>TestUserGroup1</memberUserGroupName> </membership> "
		    + " <referencingUserGroupNameList></referencingUserGroupNameList> </resourceGroup>";
		ResourceGroupProfile testResourceGroup1 = new ResourceGroupProfile(null,
		    null, null, null, null);
		testResourceGroup1.fromXML(XmlUtility
		    .createDocumentRoot(testResourceGroup1String));
		DbResourceGroupProfile.INSTANCE.createResourceGroupProfile(
		    testResourceGroup1);

		/*-********************************
		 *  Test Set #2
		 ********************************-*/

		// ExpiredUser
		String expUserString = "<user creationDate=\"2002-05-30T09:20:10-04:00\" lastModifiedDate=\"2002-06-29T09:20:10-04:00\" userID=\"expired\"> "
		    + "<authentication> <authenticationType>Internal</authenticationType>  <internalAccountData>  <userPassword>292c2cdcb5f669a8</userPassword> "
		    + " <lastPasswordChanged>2002-05-30T09:20:10-04:00</lastPasswordChanged> <expirationDate>2010-01-30T09:20:10-04:00</expirationDate> "
		    + " <passwordHistory oldPassword=\"1c9df49b71e05b56c080fe58f480b657\" dateChanged=\"2002-05-30T09:30:10-04:00\"></passwordHistory> "
		    + " </internalAccountData> <authenticationState>valid</authenticationState> <lastAuthenticationStateChange>2002-05-30T09:30:10-04:00</lastAuthenticationStateChange> "
		    + " <userAccountPolicy> <localPasswordPolicy>  <passwordAging>100</passwordAging>  <passwordExpirationNotification></passwordExpirationNotification> "
		    + " <passwordHistorySize></passwordHistorySize> <invalidPasswords></invalidPasswords> <passwordRules></passwordRules> "
		    + " </localPasswordPolicy> <dormantPeriod>0</dormantPeriod> <inactivityPeriod>900</inactivityPeriod>  <maxInvalidLoginAttempts>3</maxInvalidLoginAttempts> "
		    + " <lockoutPeriod>300</lockoutPeriod> <lockedClientIPs></lockedClientIPs> </userAccountPolicy> <auditData> "
		    + " <lastLoginAddress>localhost</lastLoginAddress> <numOfInvalidAttempts>0</numOfInvalidAttempts> <locationOfInvalidAttempts></locationOfInvalidAttempts> "
		    + " </auditData> <wsdlCredential>292c2cdcb5f669a8</wsdlCredential> "
		    + "</authentication> "
		    + "<accountStatus> <accountState>enabled</accountState> <disabledReason></disabledReason> </accountStatus>"
		    + "<userData> <commonName>expired</commonName> <givenName>expired</givenName> <surname></surname> <telephoneNumber></telephoneNumber> <mail></mail> "
		    + " <postalAddress></postalAddress> <description></description> </userData> "
		    + "<organization> <description></description> <organizationName></organizationName> <organizationalUnitName></organizationalUnitName> "
		    + " <owner></owner> <seeAlso></seeAlso> <businessCategory></businessCategory> </organization> "
		    + "<membership> <memberUserGroupName>SystemAdminGroup</memberUserGroupName> </membership> <preferences></preferences> </user>";
		UserProfile expUser = new UserProfile();
		expUser.fromXML(XmlUtility.createDocumentRoot(expUserString));
		DbUser.INSTANCE.createUserProfile(expUser);
	}

}
