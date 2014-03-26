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

package com.nortel.appcore.app.drac.database.dracdb;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opendrac.test.TestHelper;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.ServiceXml.XC_TYPE;
import com.nortel.appcore.app.drac.common.types.State;

public class DbLightPathEdgeTest {

	@Test
	public void testExercise() throws Exception {
		TestHelper.INSTANCE.initialize();

		ServiceXml serviceXml = getTestService();

		DbLightPathEdge.INSTANCE.getTableName();
		DbLightPathEdge.INSTANCE.deleteAll();
		DbLightPathEdge.INSTANCE.add(serviceXml);
		DbLightPathEdge.INSTANCE.retrieve(null);

		Map<String, String> filter = new HashMap<String, String>();
		filter.put(DbKeys.LightPathCols.LP_CALLID, "callId");
		DbLightPathEdge.INSTANCE.retrieve(filter);

		try {
			Map<String, String> badFilter = new HashMap<String, String>();
			badFilter.put("';drop table bob;", "callId");
			DbLightPathEdge.INSTANCE.retrieve(badFilter);
			fail("Bad filter worked!");
		}
		catch (Exception e) {
		}

		DbLightPathEdge.INSTANCE.deleteByCallId("callId");
		DbLightPathEdge.INSTANCE.deleteAll();
	}

	private ServiceXml getTestService() {
		/**
		 * Here is a sample record from a live system.
		 * <p>
		 * 
		 * <pre>
		 *  ServiceXml [root=<lightPath 
		 *  status="7" 
		 *  id="b928862f-1265900826791"
		 *  serviceId="SERVICE-1265900826567" 
		 *  activationType="RESERVATION_AUTOMATIC"
		 *  controllerId="47.134.40.185:8001" 
		 *  scheduleId="SCHEDULE-1265900826564"
		 *  scheduleName="admin@PITMAN-1_1265900826491"
		 *  VCAT_ROUTING_OPTION="false">
		 *  <startTime>1265901000000</startTime>
		 *  <endTime>1265901300000</endTime>
		 *  
		 *  <requestInfo>
		 *   <user>admin</user>
		 *   <billingGroup>N/A</billingGroup>
		 *   <email/>
		 *   <priority>0</priority>
		 *   <mbs>50</mbs>
		 *   <rate>STS1</rate>
		 *   <begin>Thu Feb 11 10:10:00 EST 2010</begin>
		 *   <end>Thu Feb 11 10:15:00 EST 2010</end>
		 *  </requestInfo>
		 *  
		 *  <path 
		 *   aEnd="00-21-E1-D6-D5-DC"
		 *   zEnd="00-21-E1-D6-D6-70">
		 *  
		 *   <edge 
		 *       sourceAid="OC192-1-9-1" 
		 *       targetAid="OC12-1-11-1"
		 *       source="00-21-E1-D6-D5-DC" 
		 *       target="00-21-E1-D6-D5-DC" 
		 *       rate="STS1" 
		 *       sourceChannel="1"
		 *       targetChannel="1" 
		 *       ctkid="DRAC-b928862f-1265900826791" 
		 *       mep="" 
		 *       vlanId="" />
		 *   <edge
		 *       sourceAid="OC12-1-12-1" 
		 *       targetAid="OC12-1-11-1" 
		 *       source="00-21-E1-D6-D8-2C"
		 *       target="00-21-E1-D6-D8-2C" 
		 *       rate="STS1" 
		 *       sourceChannel="1" 
		 *       targetChannel="1"
		 *       ctkid="DRAC-b928862f-1265900826791" 
		 *       mep="" 
		 *   />
		 *   <edge 
		 *       sourceAid="OC12-1-12-1" 
		 *       targetAid="OC192-1-9-1"
		 *       source="00-21-E1-D6-D6-70" 
		 *       target="00-21-E1-D6-D6-70" 
		 *       rate="STS1" 
		 *       sourceChannel="1"
		 *       targetChannel="1" 
		 *       ctkid="DRAC-b928862f-1265900826791" 
		 *       mep="" 
		 *       vlanId="" 
		 *   />
		 * </path>
		 * </lightPath>,
		 * aidTransformMap=null, xmlUserData=<UserData />]
		 * </pre>
		 */

		/* We need a service in the light path db */
		ServiceXml serviceXml = new ServiceXml(State.SERVICE.EXECUTION_PENDING,
		    "b928862f-1265900826791", "SERVICE-1265900826567",
		    "RESERVATION_AUTOMATIC", "47.134.40.185:8001",
		    "SCHEDULE-1265900826564", "admin@PITMAN-1_1265900826491", "false",
		    new GregorianCalendar(2001, 5, 8).getTimeInMillis(),
		    new GregorianCalendar(2005, 5, 8).getTimeInMillis(), "admin", null,
		    null, "0", 50, "STS1", "00-21-E1-D6-D5-DC", "00-21-E1-D6-D6-70");

		List<CrossConnection> p = new ArrayList<CrossConnection>();
		Map<String, String> m = new HashMap<String, String>();

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC192-1-9-1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC12-1-11-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CKTID, "DRAC-b928862f-1265900826791");
		m.put(CrossConnection.RATE, "STS1");
		p.add(new CrossConnection(m));

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D8-2C");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D8-2C");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-12-1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC12-1-11-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CKTID, "DRAC-b928862f-1265900826791");
		m.put(CrossConnection.RATE, "STS1");
		p.add(new CrossConnection(m));

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D6-70");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D6-70");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-12-1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC192-1-9-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CKTID, "DRAC-b928862f-1265900826791");
		m.put(CrossConnection.RATE, "STS1");
		p.add(new CrossConnection(m));

		serviceXml.addPath(XC_TYPE.MAIN, p);

		return serviceXml;

	}

}