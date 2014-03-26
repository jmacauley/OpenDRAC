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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAlarmEvent;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPathAlarmDetails;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPathAlarmSummaries;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPathEdge;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;

/**
 * Created on Aug 22, 2005
 * 
 * @author nguyentd
 */

public enum DbUtilityLpcpScheduler {
	INSTANCE;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public void addNewService(ServiceXml serviceXml) throws Exception {
//		
		DbLightPath.INSTANCE.add(serviceXml);
		DbLightPathEdge.INSTANCE.add(serviceXml);
	}

	public void appendAlarmData(String alarmId, Element dataElement)
	    throws Exception {
		// e.g. data = <element name=reason value=cleared by audit />";
		/*
		 * <event duration="194854" id="00-1B-25-2D-5C-7A_0100000425-0001-0148"
		 * name="alarm" owner="TDEFAULT_PROXY" time="1227722945349"> <eventInfo
		 * notificationType="CR" occurredDate="2001-07-21" occurredTime="06-06-53"/>
		 * <data> <element name="description" value="AIS"/> <element name="aid"
		 * value="OC12-1-11-1"/> <element name="facility" value="OC12-1-11-1"/>
		 * <element name="serviceId" value="SERVICE-1227722874473"/> <element
		 * name="reason" value="cleared by audit"/> </data> <node
		 * id="00-1B-25-2D-5C-7A" ip="47.134.3.230" mode="SONET" port="10001"
		 * status="aligned" tid="OME0039" type="OME"/> </event>
		 */

		DbLightPathAlarmDetails.INSTANCE.appendAlarmData(alarmId, dataElement);
	}

	/*
	 * NOTE: Both old/new implementation here breaks the LPCP_PORT / NRB_PORT segregation:
	 * LPCP_PORT is accessing NRB_PORT schedule data.
	 */
	public String getActivationTypeForService(String serviceId) throws Exception {
		ServiceXml aService = getServiceFromServiceId(serviceId);

		String scheduleId = aService.getScheduleId();

		Map<String, String> schedFilter = new HashMap<String, String>();
		schedFilter.put(DbSchedule.SCHD_ID, scheduleId);
		List<Schedule> listSchedules = DbSchedule.INSTANCE.retrieve(
		    schedFilter);
		if (listSchedules == null || listSchedules.isEmpty()) {
			throw new Exception("Record not found: " + scheduleId);
		}
		Schedule schedule = listSchedules.get(0);

		return schedule.getActivationType().name();
	}

	public Map<String, String> getActiveAlarmFromNe(String neId) throws Exception {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(DbLightPathAlarmDetails.NEID, neId);
		filter.put(DbLightPathAlarmDetails.DURATION, Long.valueOf(0));
		List<Element> list = DbLightPathAlarmDetails.INSTANCE.retrieve(filter);

		Map<String, String> resultMap = new HashMap<String, String>();
		if (list != null) {
			for (Element element : list) {
				resultMap.put(
				    element.getAttributeValue(DbLightPathAlarmDetails.ALARMID),
				    element.getAttributeValue(DbLightPathAlarmDetails.TIME));
			}
		}

		return resultMap;
	}

	public Tl1XmlAlarmEvent getAlarm(String alarmId) throws Exception {
		Tl1XmlAlarmEvent aEvent = null;
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(DbLightPathAlarmDetails.ALARMID, alarmId);
		List<Element> list = DbLightPathAlarmDetails.INSTANCE.retrieve(filter);
		if (list != null && !list.isEmpty()) {
			Element alarmEvent = list.get(0);
			aEvent = new Tl1XmlAlarmEvent(alarmEvent);
		}
		return aEvent;
	}

	public List<ServiceXml> getLiveServicesWithinTimeInterval(long fromTime,
	    long toTime) throws Exception {
		// NOTE: The OLD implementation for this method (was named: getServices) did
		// a look up of the
		// services within the Schedule collection?! LPCP_PORT should be referencing LPCP_PORT
		// data only
		// within the Services.xml collection.

		return DbLightPath.INSTANCE.getLiveServicesWithinTimeInterval(
		    fromTime, toTime);
	}

	public List<String> getNeWithActiveAlarm() throws Exception {
		return DbLightPathAlarmDetails.INSTANCE.getNeWithActiveAlarm();
	}

	/*
	 * Get the next schedule with minimum endTime that is in the INPROGRESS or
	 * PENDING state
	 * 
	 * @param @return ScheduleXML
	 */
	public ServiceXml getNextServiceToDelete() throws Exception {
		return DbLightPath.INSTANCE.getNextServiceToDelete();
	}

	/*
	 * Get the next schedule with minimum startTime and endTime greater than
	 * fromTime that is in the PENDING state
	 * 
	 * @param long fromTime, time in milliseconds to start the search from
	 * 
	 * @return ScheduleXML
	 */
	public ServiceXml getNextServiceToStart(long fromTime) throws Exception {
		return DbLightPath.INSTANCE.getNextServiceToStart(fromTime);
	}

	public ServiceXml getServiceFromCallId(String callId) throws Exception {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(DbKeys.LightPathCols.LP_CALLID, callId);
		List<ServiceXml> results = DbLightPath.INSTANCE.retrieve(filter);
		if (results == null || results.isEmpty()) {
			throw new Exception("Record not found: " + callId);
		}
		return results.get(0);
	}

	public ServiceXml getServiceFromServiceId(String serviceId) throws Exception {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(DbKeys.LightPathCols.LP_SERVICEID, serviceId);
		List<ServiceXml> results = DbLightPath.INSTANCE.retrieve(filter);
		if (results == null || results.isEmpty()) {
			throw new Exception("Record not found: " + serviceId);
		}
		return results.get(0);
	}

	public List<ServiceXml> getServicesFromAid(String aid, String channel,
	    long fromTime, String sourceNeId) throws Exception {
		return DbLightPath.INSTANCE.getServicesFromAid(aid, channel, fromTime,
		    sourceNeId);
	}

	public List<ServiceXml> getServicesFromAlarm(String alarmId) throws Exception {
		return DbLightPath.INSTANCE.getServicesFromAlarm(alarmId);
	}

	public SERVICE getServiceStatus(String id) throws Exception {
		ServiceXml aService = getServiceFromServiceId(id);
		SERVICE status = aService.getStatus();
		return status;
	}

	public void insertAlarmDetail(String anEvent) throws Exception {
		DbLightPathAlarmDetails.INSTANCE
		    .add(DbOpsHelper.xmlToElement(anEvent));
	}

	public void insertAlarmSummary(String neId, String anAlarm) throws Exception {
		DbLightPathAlarmSummaries.INSTANCE.add(neId,
		    DbOpsHelper.xmlToElement(anAlarm));
	}

	public void updateAlarmDuration(String alarmId, long duration)
	    throws Exception {
		DbLightPathAlarmDetails.INSTANCE
		    .updateAlarmDuration(alarmId, duration);
	}

	public void updateServiceByServiceId(String serviceId, SERVICE status)
	    throws Exception {
		Map<String, String> data = new HashMap<String, String>();
		data.put(DbKeys.LightPathCols.LP_STATUS, Integer.toString(status.ordinal()));
		Map<String, String> idMap = new HashMap<String, String>();
		idMap.put(DbKeys.LightPathCols.LP_SERVICEID, serviceId);
		DbLightPath.INSTANCE.update(idMap, data);
	}

	public void updateServiceStatusByCallId(String callId, SERVICE status)
	    throws Exception {
		Map<String, String> data = new HashMap<String, String>();
		data.put(DbKeys.LightPathCols.LP_STATUS, Integer.toString(status.ordinal()));
		Map<String, String> idMap = new HashMap<String, String>();
		idMap.put(DbKeys.LightPathCols.LP_CALLID, callId);
		DbLightPath.INSTANCE.update(idMap, data);

		ServiceXml service = getServiceFromCallId(callId);

		Map<String, String> details = new HashMap<String, String>();
		details.put("ServiceState", status.toString());

		try {
			List<NetworkElementHolder> neList = DbNetworkElement.INSTANCE
			    .retrieveAll();
			String aTid = null;
			String aIp = null;
			String zTid = null;
			String zIp = null;

			for (NetworkElementHolder h : neList) {
				if (h.getId().equals(service.getAend())) {
					aTid = h.getTid();
					aIp = h.getIp() + ":" + h.getPort();
				}
				else if (h.getId().equals(service.getZend())) {
					zTid = h.getTid();
					zIp = h.getIp() + ":" + h.getPort();
				}
			}

			if (aIp != null) {
				details.put("A-End-Ne-IP", aIp);
			}

			if (aTid != null) {
				details.put("A-End-Ne-TID", aTid);
			}

			if (zIp != null) {
				details.put("Z-End-Ne-IP", zIp);
			}

			if (zTid != null) {
				details.put("Z-End-Ne-TID", zTid);
			}
		}
		catch (Exception e) {
			log.error("Unable to provide a/z end NE record details for service "
			    + service.toString(), e);
		}

		details.put("A-End-Ne-ID", service.getAend());
		details.put("A-End-AID", service.getEndConnection(true).getSourceXcAid());

		details.put("Z-End-Ne-ID", service.getZend());
		details.put("Z-End-AID", service.getEndConnection(false).getTargetXcAid());

		details.put("StartTimeString", service.getBeginDateTime());
		details.put("EndTimeString", service.getEndDateTime());
		details.put("StartTime", Long.toString(service.getStartTime()));
		details.put("EndTime", Long.toString(service.getEndTime()));
		details.put("User", service.getUser());
		details.put("Bandwidth", service.getBandwidth());
		details.put("Mps", Integer.toString(service.getMbs()));
		// 
		details.put("FullServiceRecord", service.toString());
		LogRecord lr = new LogRecord(null, null, service.getBillingGroup(),
		    service.getServiceId(), LogKeyEnum.KEY_SERVICE_STATE_CHANGED,
		    new String[] { status.toString() }, details);
		DbLog.INSTANCE.addLog(lr);
	}
}
