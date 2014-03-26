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

package com.nortel.appcore.app.drac.server.webserver.struts.schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.PathType.PROTECTION_TYPE;
import com.nortel.appcore.app.drac.common.types.RecurrenceType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType.RecurrenceFreq;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.common.types.StatusType;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.CreateScheduleForm;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.ScheduleForm;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.TaskForm;
import com.nortel.appcore.app.drac.server.webserver.struts.service.ServiceHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.service.form.ServiceForm;

/**
 * @author Darryl Cheung
 */
public final class ScheduleHelper {
  
  private static final Logger log = LoggerFactory.getLogger(ScheduleHelper.class);
	private ScheduleHelper() {
	}

	public static List<ScheduleForm> copyAvailableTimes(Locale locale,
	    TimeZone tz, List<DracService> services) {
		List<ScheduleForm> result = new ArrayList<ScheduleForm>();
		if (services != null && !services.isEmpty()) {
			for (DracService src : services) {
				ScheduleForm dest = new ScheduleForm();
				dest.setStartTimeMillis(src.getStartTime());
				dest.setEndTimeMillis(src.getEndTime());
				Date startDate = new Date(src.getStartTime());
				Date endDate = new Date(src.getEndTime());

				SimpleDateFormat dateFormatter = new SimpleDateFormat(
				    DracConstants.WEB_GUI_TIME2, locale);
				dateFormatter.setTimeZone(tz);
				dest.setStartDate(dateFormatter.format(startDate));
				dest.setEndDate(dateFormatter.format(endDate));
				result.add(dest);
			}
		}
		return result;
	}

	public static synchronized List<ScheduleForm> copyProperties(Locale locale,
	    TimeZone tz, List<Schedule> scheduleList) throws Exception {
		List<ScheduleForm> result = new ArrayList<ScheduleForm>();
		if (scheduleList != null && !scheduleList.isEmpty()) {
			for (Schedule src : scheduleList) {
				ScheduleForm dest = new ScheduleForm();
				dest.setName(src.getName());
				dest.setWebSafeName(src.getName());
				dest.setId(src.getId());
				dest.setStatus(src.getStatus().name());
				dest.setRate(String.valueOf(src.getRate()));
				dest.setRecurrence(src.isRecurring());
				dest.setSrcTNA(src.getSrcTNA());
				dest.setSrcFacLabel(src.getSrcFacLabel());
				dest.setDestTNA(src.getDestTNA());
				dest.setDestFacLabel(src.getDestFacLabel());

				dest.setCancellable(State.isCancelable(src.getStatus()));

				// remove
				dest.setConfirmable(State.isConfirmable(src.getStatus()));

				dest.setStartTimeMillis(src.getStartTime());
				dest.setEndTimeMillis(src.getEndTime());
				Date startDate = new Date(src.getStartTime());
				Date endDate = new Date(src.getEndTime());

				SimpleDateFormat dateFormatter = new SimpleDateFormat(
				    DracConstants.WEB_GUI_TIME2, locale);
				dateFormatter.setTimeZone(tz);
				dest.setStartDate(dateFormatter.format(startDate));
				dest.setEndDate(dateFormatter.format(endDate));
				UserType userType = src.getUserInfo();
				if (userType != null) {
					dest.setUserId(userType.getUserId());
					String sourceUserGroup = userType.getSourceEndpointUserGroup();
					String targetUserGroup = userType.getTargetEndpointUserGroup();
					String groupNames = "";
					if (sourceUserGroup != null && targetUserGroup != null) {
						groupNames = sourceUserGroup + ", " + targetUserGroup;
					}
					else {
						if (sourceUserGroup != null) {
							groupNames = sourceUserGroup;
						}
						if (targetUserGroup != null) {
							groupNames = targetUserGroup;
						}
					}
					dest.setUserGroup(groupNames);
				}

				result.add(dest);
			}
		}
		return result;
	}

	public static synchronized void copyProperties(LoginToken token,
	    Locale locale, TimeZone tz, Schedule src, ScheduleForm dest)
	    throws Exception {
		if (src != null && dest != null) {
			copyScheduleDetails(token, locale, tz, src, dest);

			DracService[] serviceIdList = src.getServiceIdList();
			if (serviceIdList != null) {
				ServiceForm[] services = new ServiceForm[serviceIdList.length];
				ServiceHelper.copyProperties(locale, tz, serviceIdList, services);
				dest.setServices(services);

				for (ServiceForm service : services) {
					if (service.isActivateable()) {
						dest.setActivateableService(true);
						break;
					}
				}
				for (ServiceForm service : services) {
					if (service.isCancellable()) {
						dest.setCancellableService(true);
						break;
					}
				}
			}
		}
	}

	public static synchronized void copyProperties(TaskType src, TaskForm dest)
	    throws Exception {
		if (src != null && dest != null) {
			dest.setTaskName(src.getTaskName());
			dest.setWebSafeName(src.getTaskName());
			dest.setResult(src.getResult().toString());
			dest.setNumberOfCompletedActivity(src.getNumberOfCompletedActivity());
			dest.setPercent(src.getPercentage());
			dest.setState(DracConstants.TaskStates[src.getState().ordinal()]);
			dest.setSubmittedTime(src.getSubmittedTime());
			dest.setTotalNumberOfActivity(src.getTotalNumberOfActivity());
			dest.setTaskId(src.getTaskId());
			dest.setTaskOwner(src.getTaskOwner());
		}
	}

	public static void copyScheduleDetails(LoginToken token, Locale locale,
	    TimeZone tz, Schedule src, ScheduleForm dest) throws Exception {
		if (src != null && dest != null) {
			dest.setName(src.getName());
			dest.setWebSafeName(src.getName());
			dest.setId(src.getId());
			dest.setStatus(src.getStatus().name());
			dest.setDuration(src.getDurationLong());
			dest.setRate(String.valueOf(src.getRate()));
			dest.setActivationType(src.getActivationType().name());
			dest.setExpandable(State.isExpandable(src.getStatus()));
			dest.setCancellable(State.isCancelable(src.getStatus()));
			dest.setConfirmable(State.isConfirmable(src.getStatus()));

			dest.setRecurrence(src.isRecurring());
			RecurrenceType recurrence = src.getRecurrence();
			if (recurrence != null) {
				dest.setRecurrenceType(recurrence.getType().toString());
				dest.setWeekDay(recurrence.getWeekDay());
				dest.setRecDay(Integer.toString(recurrence.getDay()));
				dest.setRecMonth(Integer.toString(recurrence.getMonth()));
			}
			dest.setSrcTNA(src.getSrcTNA());
			dest.setSrcFacLabel(src.getSrcFacLabel());
			dest.setSrcVlanId(src.getPath().getSrcVlanId());
			dest.setDestVlanId(src.getPath().getDstVlanId());
			dest.setVcat(src.getPath().getVcatRoutingOption());
			dest.setDestTNA(src.getDestTNA());
			dest.setDestFacLabel(src.getDestFacLabel());

			int channel = src.getPath().getSourceEndPoint().getChannelNumber();
			dest.setSrcCh(String.valueOf(channel));
			// If endpoint is layer2, don't set a channel
			if (channel == -1) {
				EndPointType ep = RequestHandler.INSTANCE.findEndpointById(token,
				    src.getPath().getSourceEndPoint().getId());

				if (ep != null) {
					String layer = ep.getLayer();
					if (!layer.equals(Layer.LAYER1.toString())) {
						dest.setSrcCh(String.valueOf(DracConstants.EMPTY_STRING));
					}
					else {
						dest.setSrcCh(String.valueOf(DracConstants.AUTO_CH));
					}
				}
				else {
					log.warn("No endpoint found for "
					    + src.getPath().getSourceEndPoint().getId());
				}
			}

			channel = src.getPath().getTargetEndPoint().getChannelNumber();
			dest.setDestCh(String.valueOf(channel));
			// If endpoint is layer2, don't set a channel
			if (channel == -1) {
				EndPointType ep = RequestHandler.INSTANCE.findEndpointById(token,
				    src.getPath().getTargetEndPoint().getId());

				if (ep != null) {
					String layer = ep.getLayer();
					if (!layer.equals(Layer.LAYER1.toString())) {
						dest.setDestCh(String.valueOf(DracConstants.EMPTY_STRING));
					}
					else {
						dest.setDestCh(String.valueOf(DracConstants.AUTO_CH));
					}
				}
				else {
					log.warn("No endpoint found for "
					    + src.getPath().getTargetEndPoint().getId());
				}
			}
			dest.setProtectionType(src.getPath().getProtectionType().toString());
			dest.setSrlg(src.getPath().getSrlg());
			String srsg = src.getPath().getSharedRiskServiceGroup();
			StringTokenizer st = new StringTokenizer(srsg, ",");
			while (st.hasMoreTokens()) {
				dest.getSrsg().add(st.nextToken().trim());
			}
			dest.setStartTimeMillis(src.getStartTime());
			dest.setEndTimeMillis(src.getEndTime());
			Date startDate = new Date(src.getStartTime());
			Date endDate = new Date(src.getEndTime());

			SimpleDateFormat dateFormatter = new SimpleDateFormat(
			    DracConstants.WEB_GUI_TIME, locale);
			dateFormatter.setTimeZone(tz);
			dest.setStartDate(dateFormatter.format(startDate));
			dest.setEndDate(dateFormatter.format(endDate));

			dateFormatter = new SimpleDateFormat(DracConstants.WEB_GUI_DATE, locale);
			dest.setFirstOccurrence(dateFormatter.format(startDate));
			dest.setLastOccurrence(dateFormatter.format(endDate));
			UserType userType = src.getUserInfo();
			//
			if (userType != null) {
				dest.setUserId(userType.getUserId());
				dest.setUserGroup(userType.getBillingGroup().toString());
			}
		}
	}

	public static Schedule create(LoginToken token, Locale locale,
	    CreateScheduleForm form) throws Exception {

		RequestHandler rh = RequestHandler.INSTANCE;

		String userID = token.getUser();
		String srcChannel = "-1";
		String destChannel = "-1";

		/* Instantiating ScheduleType Object */
		// ScheduleType sch = new ScheduleType();

		/* Retrieving values from CreateScheduleForm Java bean */
		String srctna = form.getSrcTna();
		String desttna = form.getDestTna();
		String rate = form.getRate();
		String startDate = form.getStartdate();
		log.debug("Start date: " + startDate);
		String endDate = form.getEnddate();
		log.debug("End date: " + endDate);
		String startTime = form.getStartTime();
		log.debug("Start time: " + startTime);
		String endTime = form.getEndTime();
		log.debug("End time: " + endTime);
		String schName = form.getSchName();
		String email = form.getEmail();
		boolean recurrence = form.isRecurrence();
		boolean startNow = form.isStartNow();
		boolean endNever = form.isEndNever();
		srcChannel = form.getSourceChannel();
		destChannel = form.getDestChannel();
		String billingGroup = form.getBillingGroup();
		String recEndByDate = form.getRecEndDate();
		log.debug("Rec End by date: " + recEndByDate);
		String srcGroup = form.getSrcGroup();
		String destGroup = form.getDestGroup();
		String srcResGroup = form.getSrcResGroup();
		String destResGroup = form.getDestResGroup();
		long duration = form.getDuration() * 60 * 1000;
		String protectionType = form.getProtectionType();
		String srlg = form.getSrlg().trim();
		String srsg = form.getSrsg().trim();
		String algorithm = form.getAlgorithm();
		String metric = form.getRoutingMetric();
		String activationType = form.getScheduleType();
		int metricValue = form.getMetricValue();
		String srcVlan = form.getSrcVlan();
		String dstVlan = form.getDstVlan();
		boolean vcatRoutingOption = form.isVcatRoutingOption();

		int rateVal = 0;
		TimeZone tz = DracHelper.getTimeZone(token);

		try {
			rateVal = Integer.parseInt(rate);
		}
		catch (NumberFormatException nfe) {
			log.error("ScheduleHelper--Rate value is not a number: " + rate, nfe);
		}

		log.debug("ScheduleHelper--Retrieved the values from Create form");

		// remove
		Schedule.ACTIVATION_TYPE type = null;
		try {
			type = Schedule.ACTIVATION_TYPE.valueOf(activationType);
		}
		catch (Exception e) {
			log.error("Error: ", e);
			type = ACTIVATION_TYPE.RESERVATION_AUTOMATIC;
		}

		PathType path = new PathType();
		try {
			path.setProtectionType(PROTECTION_TYPE.valueOf(protectionType));
		}
		catch (IllegalArgumentException e) {
			log.error("No such protection type: " + protectionType
			    + ". Setting to unprotected!!", e);
		}

		EndPointType sourceEndpoint = rh.findEndpointByTna(token, srctna);
		if (sourceEndpoint != null) {
			path.setSourceEndPoint(sourceEndpoint);
			path.setSource(sourceEndpoint.getNode());
		}
		else {
			log.warn("Source endpoint " + srctna + " not found, using string values");
			sourceEndpoint = new EndPointType();
			sourceEndpoint.setName(srctna);
			path.setSourceEndPoint(sourceEndpoint);
		}
		int channel = -1;		
		if(!"".equals(srcChannel)){
			try {
				channel = Integer.parseInt(srcChannel);
			}
			catch (NumberFormatException nfe) {
				log.warn("source channel not a number", nfe);
			}
		}
		
		path.getSourceEndPoint().setChannelNumber(channel);
		path.setSrcVlanId(srcVlan);

		EndPointType targetEndpoint = rh.findEndpointByTna(token, desttna);
		if (targetEndpoint != null) {
			path.setTargetEndPoint(targetEndpoint);
			path.setTarget(targetEndpoint.getNode());
		}
		else {
			log.warn("Target endpoint " + desttna + " not found, using string values");
			targetEndpoint = new EndPointType();
			targetEndpoint.setName(desttna);
			path.setTargetEndPoint(targetEndpoint);
		}

		channel = -1;
		if(!"".equals(destChannel)){
			try {
				channel = Integer.parseInt(destChannel);
			}
			catch (NumberFormatException nfe) {
				log.warn("destChannel channel not a number", nfe);
			}
		}
		path.getTargetEndPoint().setChannelNumber(channel);
		path.setDstVlanId(dstVlan);

		path.setRate(rateVal);
		path.setSrlg(srlg);

		if (CreateScheduleForm.CSPF_ALG.equals(algorithm)) {
			if (CreateScheduleForm.COST_METRIC.equals(metric)) {
				path.setCost(metricValue);
			}
			else if (CreateScheduleForm.HOP_METRIC.equals(metric)) {
				path.setHop(metricValue);
			}
			else if (CreateScheduleForm.METRIC2_METRIC.equals(metric)) {
				path.setMetric(metricValue);
			}
		}
		path.setSharedRiskServiceGroup(srsg);

		path.setVcatRoutingOption(vcatRoutingOption);

		UserType userType = new UserType(userID, new UserGroupName(billingGroup),
		    srcGroup, destGroup, srcResGroup, destResGroup, email);

		/* Assigning values to Schedule Object */

		long scheduleStartTime = 0;
		if (!startNow) {
			if (startTime != null && startDate != null) {
				scheduleStartTime = DracHelper.parseWebDateToMillis(locale, tz,
				    startDate, startTime);
			}
		}
		else {
			scheduleStartTime = new Date().getTime();
		}

		Long scheduleEndTime = null;
		if (!endNever) {
			if (endTime != null && endDate != null) {
				scheduleEndTime = Long.valueOf(DracHelper.parseWebDateToMillis(locale,
				    tz, endDate, endTime));
			}
		}
		else {
			Calendar cal = Calendar.getInstance();
			cal.set(2999, 11, 31, 0, 0);
			scheduleEndTime = Long.valueOf(cal.getTimeInMillis());
			// somehow the end never button trashes the duration value by setting it to 0 in most cases.
			duration = scheduleEndTime.longValue() - scheduleStartTime;
		}

		// sanity check
		if (duration != scheduleEndTime.longValue() - scheduleStartTime) {
			log.warn("Duration of service does not match start/end time");
		}

		RecurrenceType rec;
		if (recurrence) {
			// the end time of the schedule is really the end time of the last service
			long startOfLastService = DracHelper.parseWebDateToMillis(locale, tz,
			    recEndByDate, startTime);
			

			long finalEndTime = startOfLastService + duration;
			
			scheduleEndTime = Long.valueOf(finalEndTime);

			RecurrenceFreq freq = RecurrenceFreq.parseString(form.getFrequency());
			if (RecurrenceFreq.FREQ_WEEKLY.equals(freq)) {
				List<Integer> weekdayList = new ArrayList<Integer>();
				if (form.isWeeklySun()) {
					weekdayList.add(Integer.valueOf(Calendar.SUNDAY));
				}
				if (form.isWeeklyMon()) {
					weekdayList.add(Integer.valueOf(Calendar.MONDAY));
				}
				if (form.isWeeklyTue()) {
					weekdayList.add(Integer.valueOf(Calendar.TUESDAY));
				}
				if (form.isWeeklyWed()) {
					weekdayList.add(Integer.valueOf(Calendar.WEDNESDAY));
				}
				if (form.isWeeklyThu()) {
					weekdayList.add(Integer.valueOf(Calendar.THURSDAY));
				}
				if (form.isWeeklyFri()) {
					weekdayList.add(Integer.valueOf(Calendar.FRIDAY));
				}
				if (form.isWeeklySat()) {
					weekdayList.add(Integer.valueOf(Calendar.SATURDAY));
				}
				int[] weekdays = new int[weekdayList.size()];
				for (int i = 0; i < weekdays.length; i++) {
					weekdays[i] = weekdayList.get(i).intValue();
				}
				rec = new RecurrenceType(freq, 0, 0, weekdays);

			}
			else if (RecurrenceFreq.FREQ_MONTHLY.equals(freq)) {
				String monthlyDay = form.getMonthlyDay();
				int monthDay = 0;
				try {
					monthDay = Integer.parseInt(monthlyDay);
				}
				catch (NumberFormatException nfe) {
					log.error("ScheduleHelper--MonthlyDay value is not a number: "
					    + monthlyDay, nfe);
				}
				rec = new RecurrenceType(freq, 0, monthDay, null);
			}
			else if (RecurrenceFreq.FREQ_YEARLY.equals(freq)) {
				String yearlyDay = form.getYearlyDay();
				String yearlyMonth = form.getYearlyMonth();
				int yearDay = 0;
				int yearMonth = 0;
				try {
					yearDay = Integer.parseInt(yearlyDay);
				}
				catch (NumberFormatException nfe) {
					log.error("ScheduleHelper--yearDay value is not a number: "
					    + yearlyDay, nfe);
				}
				try {
					yearMonth = Integer.parseInt(yearlyMonth);
				}
				catch (NumberFormatException nfe) {
					log.error("ScheduleHelper--yearMonth value is not a number: "
					    + yearlyMonth, nfe);
				}

				rec = new RecurrenceType(freq, yearDay, yearMonth, null);
			}
			else if (RecurrenceFreq.FREQ_DAILY.equals(freq)) {

				rec = new RecurrenceType(RecurrenceFreq.FREQ_DAILY, 0, 0, null);
			}
			else {
				// What kind of recurrence is this??
				log.error("ScheduleHelper--Recurrence Type is not valid: " + freq);
				throw new Exception("ScheduleHelper--Recurrence Type is not valid: "
				    + freq);
			}
		}
		else {
			rec = new RecurrenceType(RecurrenceFreq.FREQ_ONCE, 0, 0, null);
		}

		/* Calling CreateSchedule Function */
		String schID = null;
		log.debug("Calling CreateSchedule");

		Schedule sch = new Schedule(type, "unknown", schName,
		    SCHEDULE.EXECUTION_PENDING, scheduleStartTime, scheduleEndTime,
		    duration, userType, path, recurrence, rec, null);

		ScheduleHelper.debugSchedule(sch);

		log.debug("Creating schedule " + sch.toDebugString());
		schID = rh.createScheduleAsync(token, sch);
		log.debug("Generated Schedule Id:" + schID);

		/*
		 * @TODO: This is wrong, we are setting the ID as the task number, not the
		 * actual schedule id!
		 */
		sch.setId(schID);

		return sch;
	}

	public static void setServiceList(Locale locale, TimeZone tz,
	    List<StatusType> services, ScheduleForm form) {
		if (services != null && form != null) {
			ServiceForm serviceForm = null;
			ArrayList<ServiceForm> list = new ArrayList<ServiceForm>();

			SimpleDateFormat dateFormatter = new SimpleDateFormat(
			    DracConstants.WEB_GUI_TIME, locale);
			dateFormatter.setTimeZone(tz);
			int cnt = 0;
			for (StatusType status : services) {
				serviceForm = new ServiceForm();

				String startTime = status.getProperties(StatusType.START_TIME);
				String endTime = status.getProperties(StatusType.END_TIME);
				if (startTime == null || startTime.equals("") || endTime == null
				    || endTime.equals("")) {
					continue;
				}
				Date startDate = new Date(Long.parseLong(startTime));
				serviceForm.setStartdate(dateFormatter.format(startDate));
				Date endDate = new Date(Long.parseLong(endTime));
				serviceForm.setEnddate(dateFormatter.format(endDate));
				serviceForm.setStatus(status.getType().toString());
				serviceForm.setMessage(status.getProperties(StatusType.MESSAGE));

				serviceForm.setResultNum(++cnt);
				list.add(serviceForm);
			}
			ServiceForm[] serviceForms = list.toArray(new ServiceForm[list.size()]);
			form.setServices(serviceForms);
		}
	}

	private static void debugSchedule(Schedule sch) {

		if (sch != null && log.isDebugEnabled()) {
			
			
			
			log.debug("Src tna: "
			    + sch.getPath().getSourceEndPoint().getAttributes()
			        .get(FacilityConstants.TNA_ATTR));
			log.debug("Target tna: "
			    + sch.getPath().getTargetEndPoint().getAttributes()
			        .get(FacilityConstants.TNA_ATTR));
		
			if (sch.getRecurrence().getWeekDay() != null) {
				for (int i : sch.getRecurrence().getWeekDay()) {
					
				}
			}
		}
	}

}
