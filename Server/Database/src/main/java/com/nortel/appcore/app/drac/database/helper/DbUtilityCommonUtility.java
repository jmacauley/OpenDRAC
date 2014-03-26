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

package com.nortel.appcore.app.drac.database.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.common.utility.AlarmXml;
import com.nortel.appcore.app.drac.database.dracdb.DbGlobalPolicy;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPathAlarmDetails;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;

/**
 * Created on Aug 22, 2005
 * 
 * @author nguyentd
 */

public enum DbUtilityCommonUtility {
	INSTANCE;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public List<Schedule> findExpandableSchedules() throws Exception {
		List<Schedule> resultList = new ArrayList<Schedule>();

		Map<String, String> schedFilter = new HashMap<String, String>();
		schedFilter.put(DbKeys.ENDTIME_GREATERTHAN_EQUALTO,
		    Long.toString(System.currentTimeMillis()));
		List<Schedule> listSchedules = DbSchedule.INSTANCE.retrieve(
		    schedFilter);
		for (Schedule scheduleType : listSchedules) {
			if (State.isExpandable(scheduleType.getStatus())) {
				resultList.add(scheduleType);
			}
		}

		return resultList;
	}

	public long getNextSchedule() throws Exception {
		return DbSchedule.INSTANCE.getNextSchedule();
	}

	public void purgeServices(List<String> serviceIds, UserDetails userDetails)
	    throws Exception {
		for (String serviceId : serviceIds) {
			DbLightPath.INSTANCE.deleteByServiceId(serviceId);
		}

		DbLog.INSTANCE.generateLog(
		    new LogRecord(null, null, null, null,
		        LogKeyEnum.KEY_SYSTEM_DB_PURGED_FULL, new String[] { "" }));
	}

	public List<AlarmType> queryAllServiceAlarms(long startTime, long endTime)
	    throws Exception {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(DbLightPathAlarmDetails.TIME_GREATERTHAN_EQUALTO,
		    Long.valueOf(startTime));
		filter.put(DbLightPathAlarmDetails.TIME_LESSTHAN_EQUALTO,
		    Long.valueOf(endTime));
		List<Element> list = DbLightPathAlarmDetails.INSTANCE.retrieve(filter);

		return elementToAlarmTypeListConversion(list);
	}

	public String queryCallStatus(String callId) {
		String result = "unknown";

		Map<String, Object> serviceFilter = new HashMap<String, Object>();
		serviceFilter.put(DbKeys.LightPathCols.LP_CALLID, callId);
		try {
			List<ServiceXml> results = DbLightPath.INSTANCE.retrieve(
			    serviceFilter);

			if (results != null && results.size() == 1) {
				ServiceXml aService = results.get(0);
				result = Integer.toString(aService.getStatus().ordinal());
			}
		}
		catch (Exception e) {
			log.error("queryCallStatus:: failed to retrieve service record for id: "
			    + callId);
		}

		return result;
	}

	public int queryConfirmationTimeout() {
		int timeout = GlobalPolicy.PRERESERVATIONCONFIRMATIONTIMEOUT_DEFAULT;

		try {
			GlobalPolicy gp = DbGlobalPolicy.INSTANCE.getGlobalPolicy();
			timeout = gp.getPreReservationConfirmationTimeout();
		}
		catch (Exception e) {
			log.error("queryConfirmationTimeout: Failed to obtain global policy value.");
		}

		return timeout;
	}

	public Schedule querySchedule(String scheduleId) throws Exception {
		Map<String, String> schedFilter = new HashMap<String, String>();
		schedFilter.put(DbSchedule.SCHD_ID, scheduleId);
		List<Schedule> listSchedules = DbSchedule.INSTANCE.retrieve(
		    schedFilter);
		if (listSchedules == null || listSchedules.size() == 0) {
			throw new Exception("querySchedule:: Record not found: " + scheduleId);
		}

		return listSchedules.get(0);
	}

	public Schedule queryScheduleFromServiceId(String serviceId) throws Exception {
		return DbSchedule.INSTANCE.queryScheduleFromServiceId(serviceId);
	}

	public int queryScheduleOffset() {
		int offset = GlobalPolicy.SCHEDULEPROVISIONINGOFFSET_DEFAULT;

		try {
			GlobalPolicy gp = DbGlobalPolicy.INSTANCE.getGlobalPolicy();
			offset = gp.getScheduleProvisioningOffset();
		}
		catch (Exception e) {
			log.error("queryScheduleOffset: Failed to obtain global policy value.");
		}

		return offset;
	}

	public List<Schedule> querySchedules(long startTime, long endTime, List<UserGroupName> groups,
	        String name) throws Exception {
		return DbSchedule.INSTANCE.querySchedules(startTime, endTime, groups, name);
	}
	
	public AlarmType queryServiceAlarm(String alarmId) throws Exception {
		AlarmType result = null;
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(DbLightPathAlarmDetails.ALARMID, alarmId);
		List<Element> elementList = DbLightPathAlarmDetails.INSTANCE.retrieve(
		    filter);
		List<AlarmType> alarmList = elementToAlarmTypeListConversion(elementList);

		// Allow for a null response...to accomodate the case in which an old
		// service alarm
		// audit log is being viewed and the underlying service record has actually
		// been purged.
		if (alarmList != null && alarmList.size() == 1) {
			result = alarmList.get(0);
		}

		return result;
	}

	public List<DracService> queryServices(long startTime, long endTime,
	    List<UserGroupName> groups) throws Exception {
		// NOTE: The input start/end times are on the LIGHTPATH record,
		// not the aggregrate start/end on the SCHEDULE.
		return DbSchedule.INSTANCE.queryServices(startTime, endTime, groups);
	}

	public DracService queryServiceSummaryFromServiceId(String serviceId)
	    throws Exception {
		return DbSchedule.INSTANCE.queryServiceSummaryFromServiceId(serviceId);
	}

	public List<DracService> queryUtilization(String tna, long rangeStart,
	    long rangeEnd) throws Exception {
		// NOTE: The start/end times are on the LIGHTPATH record,
		// not the aggregrate start/end on the SCHEDULE!!!
		return DbSchedule.INSTANCE.queryUtilization(tna, rangeStart, rangeEnd);
	}

	public void updateConfirmationTimeout(int timeout) throws Exception {
		GlobalPolicy gp = DbGlobalPolicy.INSTANCE.getGlobalPolicy();
		gp.setPreReservationConfirmationTimeout(timeout);
		DbGlobalPolicy.INSTANCE.setDefaultGlobalPolicy(gp);
	}

	public void updateScheduleOffset(int offset) throws Exception {
		GlobalPolicy gp = DbGlobalPolicy.INSTANCE.getGlobalPolicy();
		gp.setScheduleProvisioningOffset(offset);
		DbGlobalPolicy.INSTANCE.setDefaultGlobalPolicy(gp);
	}

	/**
	 * @param scheduleId
	 */
	public void updateScheduleStatus(String scheduleId) throws Exception {
		Map<String, String> schedFilter = new HashMap<String, String>();
		schedFilter.put(DbSchedule.SCHD_ID, scheduleId);
		List<Schedule> listSchedules = DbSchedule.INSTANCE.retrieve(
		    schedFilter);
		if (listSchedules == null || listSchedules.size() == 0) {
			throw new Exception("Record not found: " + scheduleId);
		}
		Schedule schedule = listSchedules.get(0);
		SCHEDULE currentState = schedule.getStatus();

		List<DracService> listServicesFromScheduleRecord = schedule
		    .getServiceIdArrayList();

		Set<SERVICE> serviceStatusList = new HashSet<SERVICE>();

		for (DracService service : listServicesFromScheduleRecord) {
			serviceStatusList.add(service.getStatus());
		}

		if (serviceStatusList.size() > 0) {
			SCHEDULE newState = State.evaluateState(currentState, serviceStatusList);
			if (!currentState.equals(newState)) {
				updateScheduleStatus(scheduleId, newState.name());
			}
		}
	}
	
	
	public void updateScheduleStatus(String id, String status) throws Exception {
		Map<String, String> data = new HashMap<String, String>();
		data.put(DbSchedule.SCHD_STATUS, status);
		DbSchedule.INSTANCE.update(id, data);
	}

	public void updateScheduleTime(String scheduleId, long startTime, long endTime) {
		try {
			Map<String, String> data = new HashMap<String, String>();
			data.put(DbSchedule.SCHD_STARTTIME, Long.toString(startTime));
			data.put(DbSchedule.SCHD_ENDTIME, Long.toString(endTime));
			DbSchedule.INSTANCE.update(scheduleId, data);
		}
		catch (Exception e) {
			log.error("DbUtilityCommonUtility::updateScheduleTime failed.", e);
		}
	}

	private List<AlarmType> elementToAlarmTypeListConversion(List<Element> list)
	    throws Exception {
		Element alarmList = new Element("alarmList");

		if (list != null && list.size() > 0) {
			for (Element alarmDetailRecord : list) {
				Element alarm = new Element("alarm");
				alarm.setAttribute("id", alarmDetailRecord.getAttributeValue("id"));
				alarm.setAttribute("duration",
				    alarmDetailRecord.getAttributeValue("duration"));
				alarm.setAttribute("time", alarmDetailRecord.getAttributeValue("time"));

				Element eventInfoElement = alarmDetailRecord.getChild("eventInfo");
				alarm.setAttribute("severity",
				    eventInfoElement.getAttributeValue("notificationType"));

				/*
				 * Need to accommodate a one alarm to many services condition. The alarm
				 * details record would look like the following: <event name="alarm"
				 * id="00-1B-25-2D-5B-E6_0100033489-1005-0115" owner="TDEFAULT_PROXY"
				 * time="1227556734905" duration="0"> <eventInfo notificationType="MJ"
				 * occurredDate="2001-07-19" occurredTime="07-58-46" /> <data> <element
				 * name="description" value="Unequipped" /> <element name="aid"
				 * value="STS3C-1-11-1-1" /> <element name="facility"
				 * value="OC12-1-11-1" /> <element name="channel" value="1" /> ->
				 * <element name="serviceId" value="SERVICE-1227556729295" /> ->
				 * <element name="serviceId" value="SERVICE-1227794449669"/> </data>
				 * <node type="OME" id="00-1B-25-2D-5B-E6" ip="47.134.3.229"
				 * port="10001" tid="OME0237" mode="SONET" status="aligned" /> </event>
				 */
				Element dataElement = alarmDetailRecord.getChild("data");
				List<Element> dataChildren = dataElement.getChildren();
				List<String> serviceIdList = new ArrayList<String>();
				String description = null;
				for (Element el : dataChildren) {
					if (DbKeys.LightPathAlarmSummariesCols.SERVICEID.equals(el
					    .getAttributeValue("name"))) {
						serviceIdList.add(el.getAttributeValue("value"));
					}
					else if ("description".equals(el.getAttributeValue("name"))) {
						description = el.getAttributeValue("value");
					}
				}

				alarm.setAttribute("description", description);

				if (serviceIdList.size() > 0) {
					for (String serviceId : serviceIdList) {
						Element alarmWithService = (Element) alarm.clone();

						// Now, go pull the service record info:
						Map<String, Object> serviceFilter = new HashMap<String, Object>();
						serviceFilter.put(DbKeys.LightPathCols.LP_SERVICEID, serviceId);
						List<ServiceXml> results = DbLightPath.INSTANCE.retrieve(
						    serviceFilter);
						if (results.size() == 1) {
							ServiceXml aService = results.get(0);

							Element service = new Element("service");
							service.setAttribute("id", serviceId);
							service.setAttribute("scheduleId", aService.getScheduleId());
							service.setAttribute("scheduleName", aService.getScheduleName());

							alarmWithService.addContent(service);
						}

						alarmList.addContent(alarmWithService);
					}
				}
			}
		}

		String result = DbOpsHelper.elementToString(alarmList);
		AlarmXml alarmXML = new AlarmXml(result);
		return alarmXML.rootNodeToAlarmList();
	}

}
