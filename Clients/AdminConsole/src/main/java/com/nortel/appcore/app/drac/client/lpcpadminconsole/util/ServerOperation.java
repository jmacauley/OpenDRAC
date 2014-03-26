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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.util;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.types.AuditResult;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.GraphData;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.LpcpStatus;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.LpcpEventCallback;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;

/**
 * ServerOperation This class contains various methods for communicating with
 * the DRAC server.
 * 
 * @author adlee Revision history:
 * @since 006-01-12
 * @author alee
 */
public final class ServerOperation implements Runnable {
  private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * Simple holder class to hold the parms for editing a facility.
	 */
	public static class EditFacilityHolder {
		public String neid;
		public String aid;
		public String tna;
		public String faclabel;
		public String constraints;
		public String cost;
		public String domainId;
		public String grp;
		public String metric2;
		public String mtu;
		public String sigType;
		public String siteId;
		public String srlg;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("EditFacilityHolder [neid=");
			builder.append(neid);
			builder.append(", aid=");
			builder.append(aid);
			builder.append(", tna=");
			builder.append(tna);
			builder.append(", faclabel=");
			builder.append(faclabel);
			builder.append(", constraints=");
			builder.append(constraints);
			builder.append(", cost=");
			builder.append(cost);
			builder.append(", domainId=");
			builder.append(domainId);
			builder.append(", grp=");
			builder.append(grp);
			builder.append(", metric2=");
			builder.append(metric2);
			builder.append(", mtu=");
			builder.append(mtu);
			builder.append(", sigType=");
			builder.append(sigType);
			builder.append(", siteId=");
			builder.append(siteId);
			builder.append(", srlg=");
			builder.append(srlg);
			builder.append("]");
			return builder.toString();
		}
	}

	public enum Operation {
		OP_GET_ALL_FACILITIES, OP_GET_DRAC_INFO, OP_CREATE_SCHEDULE, OP_EDT_FACILITY, OP_GET_ALL_ALARMS, OP_GET_SERVICES, OP_GET_SERVICES_FROM_AID, OP_SAVE_PREFERENCES, OP_LOAD_PREFERENCES, OP_GET_ALL_NES, OP_GET_PROVISONED_SCHEDULES, OP_GET_ACTIVE_SCHEDULES, OP_GET_PROVISONED_SERVICES, OP_GET_ACTIVE_SERVICES, OP_GET_PROVISONED_USERS;
	}

	public static final String STATUS_ATTR = "status";
	public static final String EXCEPTION_ATTR = "exception";
	public static final String ID_ATTR = "id";
	public static final String STATUS_SUCCESS = "success";
	public static final String STATUS_FAIL = "fail";
	public static final String MAP_RESULT_KEY = "RESULT";

	private Operation operation;
	private final Map<String, Object> result = new HashMap<String, Object>();
	private ServerOperationCallback cb;

	private Object parameterObject;
	private LoginToken token;

	public ServerOperation() {
		super();
	}

	public ServerOperation(Operation op, ServerOperationCallback callback) {
		operation = op;
		cb = callback;
	}

	public ServerOperation(Operation op, ServerOperationCallback callback,
	    Object parameterO) {
		operation = op;
		cb = callback;
		parameterObject = parameterO;
	}

	public void activateService(String serviceId) throws Exception {
		try {
			getNRBHandle().activateService(token, serviceId);
		}
		catch (Exception e) {
			log.error("Exception activating service: " + serviceId, e);
			throw e;
		}
	}

	public void updateNetworkElementPosition(String ip, String port,
	    Double positionX, Double positionY) throws Exception {
		getNRBHandle().updateNetworkElementPosition(token, ip, port, positionX,
		    positionY);
	}

	public void addAjacency(String sourceIEEE, String sourcePort,
	    String destIEEE, String destPort) throws Exception {
		getNRBHandle().addManualAdjacency(token, sourceIEEE, sourcePort, destIEEE,
		    destPort);
	}

	public List<AuditResult> auditModel() throws Exception {
		return getNRBHandle().auditModel(token);
	}

	public void cancelService(String serviceId) throws Exception {
		getNRBHandle().cancelService(token, serviceId);
	}

	public void changeNePassword(NetworkElementHolder updatedNE) throws Exception {
		getNRBHandle().changeNetworkElementPassword(token, updatedNE);
	}

	public void correctModel() throws Exception {
		getNRBHandle().correctModel(token);
	}

	public void deleteAdjacency(String neid, String port) throws Exception {
		getNRBHandle().deleteManualAdjacency(token, neid, port);
	}

	public void deleteAdjanceny(String srcNeID, String srcPort, String dstNeID,
	    String dstPort) throws Exception {
		getNRBHandle().deleteManualAdjacency(token, srcNeID, srcPort, dstNeID,
		    dstPort);
	}

	public void deleteConnection(CrossConnection xcon) throws Exception {
		getNRBHandle().deleteCrossConnection(token, xcon);
	}

	public void deleteFacility(String neId, String aid) throws Exception {
		getNRBHandle().deleteFacility(token, neId, aid);
	}

	public void deleteNetworkElement(NetworkElementHolder oldNe) throws Exception {
		getNRBHandle().deleteNetworkElement(token, oldNe);
	}

	public void enrollNetworkElement(NetworkElementHolder newNe) throws Exception {
		
		getNRBHandle().enrollNetworkElement(token, newNe);
	}

	/*
	 * getAllConstraints @param dbServer - Database server IP @param dbPort -
	 * Database server port
	 * 
	 * @return A map of all constraints
	 */
	public Map<String, BigInteger> getAllConstraints() {
		// THIS IS FOR CHANNEL CONSTRAINTS ON INNI FACILITIES
		// Element constraints = null;
		// Element facility = null;
		Map<String, BigInteger> constraintsMap = new HashMap<String, BigInteger>();

		try {
			// Original implementation made two calls: one for layer1, another for
			// layer2

			/*
			 * Original db result format: <layer1> <facility neid="00-1B-25-2D-5B-E6"
			 * aid="OC12-1-11-1" constrain="0"/> <facility neid="00-1B-25-2D-5B-E6"
			 * aid="OC12-1-12-1" constrain="0"/> <facility neid="00-1B-25-2D-5C-55"
			 * aid="OC12-1-11-1" constrain="0"/> <facility neid="00-1B-25-2D-5C-55"
			 * aid="OC12-1-12-1" constrain="0"/> </layer1>
			 */
			Map<String, String> filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER1_LAYER2.toString());
			constraintsMap.putAll(getNRBHandle()
			    .getFacilityConstraints(token, filter));

			
		}
		catch (Exception ex) {
			log.error("Error: ", ex);
		}

		return constraintsMap;
	}

	/*
	 * getConstraints
	 * 
	 * @param neid - String representation of the network element identifier
	 * 
	 * @param aid - String represenation of the access identifier for the port
	 * 
	 * @param dbServer - Database server IP
	 * 
	 * @param dbPort - Database server port
	 * 
	 * @return The value of the constraints attribute for the speciried port
	 */
	public BigInteger getConstraints(String neid, String aid) {
		// THIS IS FOR CHANNEL CONSTRAINTS ON INNI FACILITIES
		BigInteger constraintBI = BigInteger.ZERO;
		try {
			Map<String, String> filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, neid);
			filter.put(DbKeys.NetworkElementFacilityCols.AID, aid);

			Map<String, BigInteger> constraintsMap = getNRBHandle()
			    .getFacilityConstraints(token, filter);

			if (constraintsMap != null && constraintsMap.size() == 1) {
				// Should be only one facility
				constraintBI = constraintsMap.entrySet().iterator().next().getValue();
			}
		}
		catch (Exception ex) {
			log.error("ServerOperation::getContraints failed.", ex);
		}

		return constraintBI;
	}

	public String getDiscoveryStatus() throws Exception {
		return getNRBHandle().getLpcpDiscoveryStatus(token);
	}

	public GraphData getGraphData() throws Exception {
		return getNRBHandle().getGraphData(token);
	}

	public List<BandwidthRecord> getInternalBandwithUsage(long startTime,
	    long endTime) throws Exception {
		return getNRBHandle().getInternalBandwithUsage(token, startTime, endTime);
	}

	public LpcpStatus getLpcpStatus() throws Exception {
		return getNRBHandle().getLpcpStatus(token);
	}

	public Operation getOperation() {
		return operation;
	}

	public String getRemoteServerIP() throws Exception {
		return getNRBHandle().getPeerIPAddress(token);
	}

	public Set<String> getResourceGroups(String userID, UserGroupName userGroup) {
		Set<String> resGroupProfiles = null;
		try {
			
			if (getNRBHandle().getUserProfile(token, userID) != null) {
				resGroupProfiles = getNRBHandle().getUserGroupProfile(token, userGroup)
				    .getMembership().getMemberResourceGroupName();
				
			}
			else {
				log.debug("User group profile could not be found for user: " + userID
				    + " group: " + userGroup);
				resGroupProfiles = new TreeSet<String>();
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}

		return resGroupProfiles;
	}

	public Map<String, Object> getResult() {
		return result;
	}

	public List<UserGroupProfile> getUserGroupProfiles() {
		List<UserGroupProfile> userGroupProfiles = null;

		try {
			
			userGroupProfiles = getNRBHandle().getUserGroupProfileList(token);

			for (int i = 0; i < userGroupProfiles.size(); i++) {
				UserGroupProfile ugp = userGroupProfiles.get(i);
				
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}

		return userGroupProfiles;
	}

	public List<Facility> listFacalities(String neid) throws Exception {
		return getNRBHandle().getFacilities(token, neid);
	}

	public void purgeServices(List<String> serviceIds) throws Exception {
		try {
			getNRBHandle().purgeServices(token, serviceIds);
		}

		catch (Exception e) {
			log.error("Exception purging services/schedules", e);

			// Re-throw the exception
			throw e;
		}
	}

	public ScheduleResult querySchedule(Map<SPF_KEYS, String> parms,
	    boolean queryOnly) throws Exception {
		return getNRBHandle().createSchedule(token, parms, queryOnly);
	}

	public void registerForLpcpEventNotifications(LpcpEventCallback callback)
	    throws Exception {
		getNRBHandle().registerForLpcpEventNotifications(token, callback);
	}

	@Override
	public void run() {
		try {
			String resultMsg = null;
			switch (operation) {
			case OP_GET_ALL_FACILITIES:
				result.put(
				    MAP_RESULT_KEY,
				    getNRBHandle().getNetworkElementFacilities(token,
				        (Map<String, String>) parameterObject));
				break;

			case OP_GET_DRAC_INFO:
				result.put(MAP_RESULT_KEY, getNRBHandle().getInfo(token));
				break;

			case OP_CREATE_SCHEDULE:
				resultMsg = createScheduleOperation();
				result.put(MAP_RESULT_KEY, resultMsg);
				break;

			case OP_EDT_FACILITY:
				try {
					EditFacilityHolder holder = (EditFacilityHolder) parameterObject;
					getNRBHandle().editFacility(token, holder.neid, holder.aid, holder.tna, holder.faclabel,
					    holder.mtu, holder.srlg, holder.grp, holder.cost, holder.metric2, holder.sigType,
					    holder.constraints, holder.domainId, holder.siteId);
					result.put(MAP_RESULT_KEY, null);
				}
				catch (Exception e) {
					result.put(MAP_RESULT_KEY, e);
				}
				break;

			case OP_GET_ALL_ALARMS:
				Element allAlarms = getAllAlarmsThreaded();
				result.put(MAP_RESULT_KEY, allAlarms);
				break;
			case OP_GET_SERVICES: {
				List<ServiceXml> services = getServicesThreaded();
				result.put(MAP_RESULT_KEY, services);
				break;
			}
			case OP_GET_SERVICES_FROM_AID: {
				List<ServiceXml> services = getServicesFromAIDThreaded();
				result.put(MAP_RESULT_KEY, services);
				break;
			}
			case OP_SAVE_PREFERENCES: {
				resultMsg = saveUserPreferences();
				result.put(MAP_RESULT_KEY, resultMsg);
				break;
			}
			case OP_LOAD_PREFERENCES: {
				Element userPreferences = loadUserPreferencesThreaded();
				result.put(MAP_RESULT_KEY, userPreferences);
				break;
			}
			case OP_GET_ALL_NES: {
				result.put(MAP_RESULT_KEY, getAllNesThreaded());
				break;
			}
			case OP_GET_PROVISONED_SCHEDULES: {
				result.put(MAP_RESULT_KEY, getProvisionedSchedulesThreaded());
				break;
			}
			case OP_GET_ACTIVE_SCHEDULES: {
				result.put(MAP_RESULT_KEY, getActiveSchedulesThreaded());
				break;
			}
			case OP_GET_PROVISONED_SERVICES: {
				result.put(MAP_RESULT_KEY, getProvisionedServicesThreaded());
				break;
			}
			case OP_GET_ACTIVE_SERVICES: {
				result.put(MAP_RESULT_KEY, getActiveServicesThreaded());
				break;
			}			
			case OP_GET_PROVISONED_USERS: {
				result.put(MAP_RESULT_KEY, getProvisionedUsersThreaded());
				break;
			}
			default:
				log.error("unknown operation code " + operation);
				break;
			}

			if (cb != null) {
				cb.handleServerOperationResult(this);
			}
			else {
				
			}
		}
		catch (Exception t) {
			log.error("Sever operation run method encountered an error ", t);
		}
	}

	public void sessionValidate() throws Exception {
		getNRBHandle().sessionValidate(token);
	}

	/*
	 * @param tna - String representation of the TNA to check
	 */
	public boolean tnaExists(String tna) {
		boolean rc = false;

		try {
			Map<String, String> filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.TNA, tna);

			List<Facility> facList = getNRBHandle().getNetworkElementFacilities(
			    token, filter);

			if (facList.size() > 0) {
				rc = true;
			}
		}
		catch (Exception ex) {
			log.error("ServerOperation::tnaExists failed.", ex);
		}

		return rc;
	}

	public void toggleNeAssociation(NetworkElementHolder existingNe)
	    throws Exception {
		getNRBHandle().toggleNetworkElementAssociation(token, existingNe);
	}

	private String createScheduleOperation() {
		String rc = null;
		String id = null;

		try {
			if (!(parameterObject instanceof Schedule)) {
				throw new Exception(
				    "ServerOperation: CreateSchedule: parameterObject is null/not a scheduleType object");
			}

			
			id = getNRBHandle()
			    .asyncCreateSchedule(token, (Schedule) parameterObject);

			// Need to query the actual status now that we are using the asynchronous
			// create
			if (getNRBHandle().getTaskInfo(token, id) != null) {
				TaskType ti = getNRBHandle().getTaskInfo(token, id);
				while (ti.getPercentage() < 100 && ti.getExceptionMessage() == null) {
				  Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
					ti = getNRBHandle().getTaskInfo(token, id);
				}

				if (ti.getExceptionMessage() != null) {
					rc = "<result status=\"" + STATUS_FAIL + "\"><" + EXCEPTION_ATTR
					    + " message=\"" + ti.getExceptionMessage() + "\"/></result>";
				}
				else {
					rc = "<result status=\"" + STATUS_SUCCESS + "\" " + ID_ATTR + "=\""
					    + id + "\"/>";
				}
				
			}

		}
		catch (RemoteException e) {
			log.error("A remote exception occurred communicating with remote server",
			    e);
			
		}
		catch (Exception e) {
			log.error(
			    "ServerOperation::createScheduleOperation::Exception occurred: ", e);
		}
		return rc;
	}

	private Element getAllAlarmsThreaded() {
		Element allAlarms = null;

		try {
			// NOTE: Multiple serviceId entries per Alarm detail record
			// Possible to have entry shown below. See also
			// DbLightPathAlarmDetails::deleteServiceReference
			// The xml in this case parses it as shown below (listed with space
			// delimiter)
			/*
			 * <event name="alarm" id="00-1B-25-2D-5B-E6_0100033489-1005-0115"
			 * owner="TDEFAULT_PROXY" time="1227556734905" duration="0"> <eventInfo
			 * notificationType="MJ" occurredDate="2001-07-19" occurredTime="07-58-46"
			 * /> <data> <element name="description" value="Unequipped" /> <element
			 * name="aid" value="STS3C-1-11-1-1" /> <element name="facility"
			 * value="OC12-1-11-1" /> <element name="channel" value="1" /> -> <element
			 * name="serviceId" value="SERVICE-1227556729295" /> -> <element
			 * name="serviceId" value="SERVICE-1227794449669"/> </data> <node
			 * type="OME" id="00-1B-25-2D-5B-E6" ip="47.134.3.229" port="10001"
			 * tid="OME0237" mode="SONET" status="aligned" /> </event>
			 */

			/*
			 * Output example: <alarmList> <svcalarm
			 * id="00-1B-25-2D-5B-E6_0100034857-0062-0339"
			 * serviceId="SERVICE-1228750359065" severity="MN" time="1228750485939"
			 * description="Circuit Pack Missing - Pluggable"/> <svcalarm
			 * id="00-1B-25-2D-5B-E6_0100034907-1005-0115" -> serviceId=
			 * "SERVICE-1228755993410 SERVICE-1227794449669 SERVICE-9999994449669"
			 * severity="MJ" time="1228755998650" description="Unequipped"/> <svcalarm
			 * id="00-1B-25-2D-5B-E6_0100034908-6000-0334"
			 * serviceId="SERVICE-1228755993410" severity="CR" time="1228755999276"
			 * description="Link Down"/> </alarmList>
			 */

			Map<String, Object> filter = new HashMap<String, Object>();

			List<Element> alarms = XmlUtility.createDocumentRoot(getNRBHandle()
			    .getAlarms(token, filter));

			allAlarms = new Element("alarmList");

			for (Element alarmDetailRecord : alarms) {
				Element alarm = new Element("svcalarm");
				alarm.setAttribute("id", alarmDetailRecord.getAttributeValue("id"));
				alarm.setAttribute("time", alarmDetailRecord.getAttributeValue("time"));

				Element eventInfoElement = alarmDetailRecord.getChild("eventInfo");
				alarm.setAttribute("severity",
				    eventInfoElement.getAttributeValue("notificationType"));

				Element dataElement = alarmDetailRecord.getChild("data");
				List<Element> dataChildren = dataElement.getChildren();
				StringBuilder serviceIdSB = new StringBuilder();
				String description = null;
				boolean flag = false;
				for (Object element : dataChildren) {
					Element el = (Element) element;

					if (DbKeys.LightPathAlarmSummariesCols.SERVICEID.equals(el
					    .getAttributeValue("name"))) {
						if (flag) {
							serviceIdSB.append(" ");
						}
						serviceIdSB.append(el.getAttributeValue("value"));
						flag = true;
					}
					else if ("description".equals(el.getAttributeValue("name"))) {
						description = el.getAttributeValue("value");
					}
				}

				alarm.setAttribute("serviceId", serviceIdSB.toString());
				alarm.setAttribute("description", description);

				allAlarms.addContent(alarm);
			}

		}
		catch (Exception ex) {
			log.error("ServerOperation::getAllAlarmsThreaded failed.", ex);
		}

		return allAlarms;
	}

	private List<NetworkElementHolder> getAllNesThreaded() {
		List<NetworkElementHolder> nes = null;
		try {

			nes = getNRBHandle().getAllNetworkElements(token);
		}
		catch (Exception e) {
			log.error("Unable to fetch NE list", e);
		}
		return nes;

	}


	private List<Schedule> getProvisionedSchedulesThreaded(){
		List<Schedule> schedules = null;
		Map<String, String> filter = new HashMap<String, String>();
		try {
			schedules = getNRBHandle().getSchedules(token, filter);
		}
		catch (Exception e) {
			log.error("Unable to fetch schedule list", e);
		}
		return schedules;		
	}
	
	private List<Schedule> getActiveSchedulesThreaded(){
		List<Schedule> schedules = null;
		Map<String, String> filter = new HashMap<String, String>();		
		Date now = new Date();
		filter.put(DbKeys.STARTTIME_LESSTHAN_EQUALTO, ""+now.getTime());
		filter.put(DbKeys.ENDTIME_GREATERTHAN_EQUALTO, ""+now.getTime());
		try {
			schedules = getNRBHandle().getSchedules(token, filter);
		}
		catch (Exception e) {
			log.error("Unable to fetch active schedule list", e);
		}
		return schedules;		
	}
	
	private List<DracService> getProvisionedServicesThreaded(){
		List<DracService> services = null;
		List<Schedule> schedules = getProvisionedSchedulesThreaded();
		if(schedules!=null){
			services = new ArrayList<DracService>();
			for(Schedule schedule: schedules){
				services.addAll(schedule.getServiceIdArrayList());
			}
		}
		return services;
	}
	
	private List<DracService> getActiveServicesThreaded(){
		List<DracService> services = null;
		List<Schedule> schedules = getActiveSchedulesThreaded();
		if(schedules!=null){
			services = new ArrayList<DracService>();
			for(Schedule schedule: schedules){
				services.addAll(schedule.getServiceIdArrayList());
			}
		}
		return services;	
	}
	
	private List<UserProfile> getProvisionedUsersThreaded(){
		List<UserProfile> profiles = null;
		try {
			profiles = getNRBHandle().getUserProfileList(token);
		}
		catch (Exception e) {
			log.error("Unable to fetch NE list", e);
		}
		return profiles;		
	}
	
	private NrbInterface getNRBHandle() {
		/*
		 * If the server goes down and comes back both the token and instance may
		 * have changed
		 */
		token = OpenDracDesktop.getAuth().getLoginToken();
		return OpenDracDesktop.getAuth().getNrb();
	}

	private List<ServiceXml> getServicesFromAIDThreaded() {
		try {
			// filter parameters:
			Map<String, String> serviceFilter = new HashMap<String, String>();
			if (parameterObject instanceof Map) {
				serviceFilter.putAll((Map) parameterObject);
			}

			List<ServiceXml> serviceXMLList = getNRBHandle().getServicesFromAID(
			    token, serviceFilter);
			return serviceXMLList;
		}
		catch (Exception ex) {
			log.error("ServerOperation::getServicesThreaded failed.", ex);
		}
		return null;
	}

	private List<ServiceXml> getServicesThreaded() {
		try {
			// filter parameters:
			Map<String, Object> serviceFilter = new HashMap<String, Object>();
			if (parameterObject instanceof Map<?, ?>) {
				serviceFilter.putAll((Map<String, Object>) parameterObject);
			}
			List<ServiceXml> serviceXMLList = getNRBHandle().getServices(token,
			    serviceFilter);
			return serviceXMLList;
		}
		catch (Exception ex) {
			log.error("ServerOperation::getServicesThreaded failed.", ex);
		}

		return null;
	}

	private Element loadUserPreferencesThreaded() {
		try {
			if (parameterObject instanceof String) {
				String userId = (String) parameterObject;
				String x = getNRBHandle().loadUserPreferences(token, userId);
				if (x == null) {
					// No user prefs.
					return null;
				}
				return XmlUtility.createDocumentRoot(x);
			}
		}
		catch (Exception ex) {
			log.error("ServerOperation::loadUserPreferences failed.", ex);
		}

		return null;
	}

	private String saveUserPreferences() {
		try {
			if (parameterObject instanceof Map) {
				
				Map<String, Object> m = (Map<String, Object>) parameterObject;
				String userid = (String) m
				    .get(DbKeys.AdminConsoleUserPreferencesCols.USERID);
				Element e = (Element) m
				    .get(DbKeys.AdminConsoleUserPreferencesCols.ELEMENT);
				getNRBHandle().saveUserPreferences(token, userid,
				    XmlUtility.rootNodeToString(e));
				return STATUS_SUCCESS;
			}
		}
		catch (Exception e) {
			log.error("ServerOperation::saveUserPreferences failed.", e);
		}
		return STATUS_FAIL;
	}

  public void updateAddressAndPort(final String oldAddress, final int oldPort, final String newAddress, final int newPort) throws Exception {
    getNRBHandle().updateAddressAndPort(token, oldAddress, oldPort, newAddress, newPort);    
  }
}
