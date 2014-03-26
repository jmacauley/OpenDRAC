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

package com.nortel.appcore.app.drac.common.types;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.ServiceXml.XC_TYPE;

public class ServiceXMLTest {
  private final Logger log = LoggerFactory.getLogger(getClass());
	@Test
	public void testServiceXML1() {
		ServiceXml s = getTestService1();
		
		s.getActivationType();
		s.getAend();
		s.getBandwidth();
		s.getBeginDateTime();
		s.getBillingGroup();
		s.getCallId();
		s.getCrossConnections();
		s.getCrossConnectionInfo(0);
		s.getControllerId();

		s.getEmail();
		s.getEndConnection(false);
		s.getEndConnection(true);
		s.getEndDateTime();
		s.getEndTime();
		s.getMbs();

		s.getPriority();
		s.getScheduleId();
		s.getScheduleName();
		s.getServiceId();
		s.getStartTime();
		s.getStatus();
		s.getUser();
		s.getVcatRoutingOption();
		s.getXMLUserData();
		s.getZend();
		s.numberOfConnectionsInPath();

	}

	@Test
	public void testServiceXML2() {
		ServiceXml s = getTestService2();
		
		
		

		

		log.debug(" numberOfConnectionsInPath new: "
		    + s.numberOfConnectionsInPath());

		

		

	}

	private ServiceXml getTestService1() {
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

	private ServiceXml getTestService2() {
		/**
		 * This is a service that LPCP_PORT just created, note the edge IDs are DRAC-N/A.
		 * 
		 * <pre>
		 * <lightPath 
		 *     status="6" 
		 *     id="cd28862f-1289919563054" 
		 *     serviceId="SERVICE-1289919562928"
		 *     activationType="RESERVATION_AUTOMATIC"
		 *     controllerId="localhost"
		 *     scheduleId="SCHEDULE-1289919562927"
		 *     scheduleName="admin@pcard0yn_1289919562889"
		 *     VCAT_ROUTING_OPTION="false">
		 *     <startTime>1289919562940</startTime>
		 *     <endTime>1289919840000</endTime>
		 *     <requestInfo>
		 *         <user>admin</user>
		 *         <billingGroup>N/A</billingGroup>
		 *         <email />
		 *         <priority>0</priority>
		 *         <mbs>50</mbs>
		 *         <rate>STS1</rate>
		 *         <begin>Tue Nov 16 09:59:22 EST 2010</begin>
		 *         <end>Tue Nov 16 10:04:00 EST 2010</end>
		 *     </requestInfo>
		 *     <path aEnd="00-21-E1-D6-D5-DC" zEnd="00-21-E1-D6-D6-70">
		 *         <edge sourceAid="OC12-1-12-1" targetAid="OC48-1-5-1" source="00-21-E1-D6-D5-DC"
		 *             target="00-21-E1-D6-D5-DC" rate="STS1" sourceChannel="1"
		 *             targetChannel="1" ctkid="DRAC-N/A" mep="" CCT="2WAYPR" swmateAid="OC12-1-11-1-1"
		 *             vlanId="" />
		 *         <edge sourceAid="OC12-1-11-1" targetAid="OC48-1-5-1" source="00-21-E1-D6-D6-70"
		 *             target="00-21-E1-D6-D6-70" ra te="STS1" sourceChannel="1"
		 *             targetChannel="1" ctkid="DRAC-N/A" mep="" CCT="2WAYPR" swmateAid="OC12-1-12-1-1"
		 *             vlanId="" />
		 *         <edge sourceAid="OC12-1-12-1" targetAid="OC12-1-11-1" source="00-21-E1-D6-D8-2C"
		 *             target="00-21-E1-D6-D8-2C" rate="STS1" sourceChannel="1"
		 *             targetChannel="1" ctkid="DRAC-N/A" mep="" />
		 *     </path>
		 *     <path type="working" aEnd="00-21-E1-D6-D5-DC" zEnd="00-21-E1-D6-D6-70">
		 *         <edge sourceAid="OC48-1-5-1" targetAid="OC12-1-12-1" source="00-21-E1-D6-D5-DC"
		 *             target="00-21-E1-D6-D5-DC" rate="STS1" sourceChannel="1"
		 *             targetChannel="1" ctkid="DRAC-N/A" mep="" CCT="2WAYPR" vlanId="" />
		 *         <edge sourceAid="OC12-1-11-1" targetAid="OC48-1-5-1" source="00-21-E1-D6-D6-70"
		 *             target="00-21-E1-D6-D6-70" rate="STS1" sourceChannel="1"
		 *             targetChannel="1" ctkid="DRAC-N/A" mep="" CCT="2WAYPR" vlanId="" />
		 *     </path>
		 *     <path type="protection" aEnd="00-21-E1-D6-D5-DC" zEnd="00-21-E1-D6-D6-70">
		 *         <edge sourceAid="OC48-1-5-1" targetAid="OC12-1-11-1" source="00-21-E1-D6-D5-DC"
		 *             target="00-21-E1-D6-D5-DC" rate="STS1" sourceChannel="1"
		 *             targetChannel="1" ctkid="DRAC-N/A" mep="" CCT="2WAYPR" vlanId="" />
		 *         <edge sourceAid="OC12-1-12-1" targetAid="OC12-1-11-1" source="00-21-E1-D6-D8-2C"
		 *             target="00-21-E1-D6-D8-2C" rate="STS1" sourceChannel="1"
		 *             targetChannel="1" ctkid="DRAC-N/A" mep="" />
		 *         <edge sourceAid="OC12-1-12-1" targetAid="OC48-1-5-1" source="00-21-E1-D6-D6-70"
		 *             target="00-21-E1-D6-D6-70" rate="STS1" sourceChannel="1"
		 *             targetChannel="1" ctkid="DRAC-N/A" mep="" CCT="2WAYPR" vlanId="" />
		 *     </path>
		 * </lightPath>
		 * </pre>
		 */

		/* We need a service in the light path db */
		ServiceXml serviceXml = new ServiceXml(State.SERVICE.EXECUTION_PENDING,
		    "cd28862f-1289919563054", "SERVICE-1289919562928",
		    "RESERVATION_AUTOMATIC", "localhost", "SCHEDULE-1289919562927",
		    "admin@pcard0yn_1289919562889", "false", 1289919562940L,
		    1289919840000L, "admin", null, null, "0", 50, "STS1",
		    "00-21-E1-D6-D5-DC", "00-21-E1-D6-D6-70");

		List<CrossConnection> p = new ArrayList<CrossConnection>();
		Map<String, String> m = new HashMap<String, String>();

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-12-1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC48-1-5-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CCT_TYPE, "2WAYPR");
		m.put(CrossConnection.SWMATE_PORT_AID, "OC12-1-11-1-1");
		m.put(CrossConnection.CKTID, "DRAC-N/A");
		m.put(CrossConnection.RATE, "STS1");
		p.add(new CrossConnection(m));

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D6-70");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D6-70");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-11-1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC48-1-5-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CCT_TYPE, "2WAYPR");
		m.put(CrossConnection.SWMATE_PORT_AID, "OC12-1-12-1-1");
		m.put(CrossConnection.CKTID, "DRAC-N/A");
		m.put(CrossConnection.RATE, "STS1");
		p.add(new CrossConnection(m));

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D8-2C");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D8-2C");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-12-1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC12-1-11-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CKTID, "DRAC-N/A");
		m.put(CrossConnection.RATE, "STS1");
		p.add(new CrossConnection(m));

		serviceXml.addPath(ServiceXml.XC_TYPE.MAIN, p);

		List<CrossConnection> wlist = new ArrayList<CrossConnection>();

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-5-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC12-1-12-1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CKTID, "DRAC-N/A");
		m.put(CrossConnection.RATE, "STS1");
		wlist.add(new CrossConnection(m));

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D6-70");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D6-70");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-11-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC48-1-5-1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CKTID, "DRAC-N/A");
		m.put(CrossConnection.RATE, "STS1");
		wlist.add(new CrossConnection(m));

		List<CrossConnection> plist = new ArrayList<CrossConnection>();

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-5-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC12-1-11-1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CKTID, "DRAC-N/A");
		m.put(CrossConnection.RATE, "STS1");
		plist.add(new CrossConnection(m));

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D8-2C");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D8-2C");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-12-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC12-1-11-1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CKTID, "DRAC-N/A");
		m.put(CrossConnection.RATE, "STS1");
		plist.add(new CrossConnection(m));

		m.clear();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D6-70");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D6-70");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC12-1-12-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC48-1-5-1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.CKTID, "DRAC-N/A");
		m.put(CrossConnection.RATE, "STS1");
		plist.add(new CrossConnection(m));

		serviceXml.addPath(ServiceXml.XC_TYPE.WORKING, wlist);
		serviceXml.addPath(ServiceXml.XC_TYPE.PROTECTION, plist);
		return serviceXml;
	}
}
