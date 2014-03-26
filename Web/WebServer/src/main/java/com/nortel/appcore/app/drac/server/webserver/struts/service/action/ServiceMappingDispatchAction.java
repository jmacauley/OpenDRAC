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

package com.nortel.appcore.app.drac.server.webserver.struts.service.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracActionMessage;
import com.nortel.appcore.app.drac.server.webserver.struts.general.action.GeneralMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.ScheduleHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.ScheduleForm;
import com.nortel.appcore.app.drac.server.webserver.struts.service.ServiceHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.service.form.AddServiceForm;
import com.nortel.appcore.app.drac.server.webserver.struts.service.form.ListServicesForm;
import com.nortel.appcore.app.drac.server.webserver.struts.service.form.ServiceAlarmForm;
import com.nortel.appcore.app.drac.server.webserver.struts.service.form.ServiceForm;

public final class ServiceMappingDispatchAction extends
    DracBaseMappingDispatchAction {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	// no instance variables allowed, struts not thread-safe

	// to be removed
	public ActionForward activateService(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String deleteID = request.getParameter("sid");
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		try {
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);
			RequestHandler.INSTANCE.activateService(token, deleteID);
			request.setAttribute("activateID", deleteID);
		}
		catch (RequestHandlerException re) {
			ActionMessages messages = new ActionMessages();
			ActionMessage message = new DracActionMessage(re)
			    .getActionMessage(locale);
			messages.add(ActionMessages.GLOBAL_MESSAGE, message);
			saveMessages(request, messages);
		}
		return mapping.findForward(DracConstants.ACTIVATE_SERVICE_JSP_PAGE);
	}

	public ActionForward addServiceList(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(90);
		logEntryBuffer.append("addServiceList: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=setting up add services;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);
		List<Schedule> scheduleList = RequestHandler.INSTANCE
		    .getActiveSchedules(token);
		List<ScheduleForm> result = ScheduleHelper.copyProperties(locale, tz,
		    scheduleList);

		request.setAttribute(DracConstants.TZSTRING, getTZString(tz, locale));
		request.setAttribute("scheduleList", result);
		return mapping.findForward(DracConstants.ADD_SERVICE_LIST_JSP_PAGE);
	}

	public ActionForward addServiceResult(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(90);
		logEntryBuffer.append("addServiceResult: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=result of add service;");
		String logEntry = logEntryBuffer.toString();

		/* Entry into page. */
		log.debug(logEntry);

		String selectedID = request.getParameter("newServiceId");

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		TimeZone tz = DracHelper.getTimeZone(token);
		Schedule schedule = RequestHandler.INSTANCE.getScheduleForService(
		    token, selectedID);
		if (schedule != null) {
			ServiceHelper.copyProperties(locale, tz, selectedID, schedule,
			    (ServiceForm) form);
			try {
				((ServiceForm) form).setSrlgInclusions(RequestHandler.INSTANCE
				    .getSRLGListForService(token, selectedID));
			}
			catch (RequestHandlerException e) {
				log.warn("Error looking up SRLG for " + selectedID, e);
			}
		}
		else {
			ActionMessages messages = new ActionMessages();
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
			    DracConstants.SERVICE_NOT_FOUND, selectedID));
			saveMessages(request, messages);
		}

		return mapping.findForward(DracConstants.ADD_SERVICE_RESULT_JSP_PAGE);
	}

	public ActionForward deleteService(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String deleteID = request.getParameter("sid");
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		try {
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);
			RequestHandler.INSTANCE.cancelService(token, deleteID);
			request.setAttribute("deletedID", deleteID);
		}
		catch (RequestHandlerException re) {
			ActionMessages messages = new ActionMessages();
			ActionMessage message = new DracActionMessage(re)
			    .getActionMessage(locale);
			messages.add(ActionMessages.GLOBAL_MESSAGE, message);
			saveMessages(request, messages);
		}
		return mapping.findForward(DracConstants.DELETE_SERVICE_JSP_PAGE);
	}

	public ActionForward doAddService(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(80);
		logEntryBuffer.append("doAddService: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=invoke");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		AddServiceForm addSvcForm = (AddServiceForm) form;
		String id = addSvcForm.getSchId();

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in scheduleDetails.jsp for listing
		// (which also sources a number of other schedule and service operations,
		// secured by ScheduleMappingDispatchAction::processSchedule)
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (id != null && !id.equals("")) {
			Schedule schedule =  RequestHandler.INSTANCE.querySchedule(token, id);
			String srcTna = addSvcForm.getSrctna();
			String destTna = addSvcForm.getDesttna();
			Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
			TimeZone tz = DracHelper.getTimeZone(token);

			long startTime = 0;
			if (addSvcForm.isStartNow()) {
				startTime = new Date().getTime();
			}
			else {
				startTime = DracHelper.parseWebDateToMillis(locale, tz,
				    addSvcForm.getStartdate(), addSvcForm.getStartTime());
			}

			long endTime = 0;
			if (addSvcForm.isEndNever()) {
				Calendar cal = Calendar.getInstance();
				cal.set(2999, 11, 31, 0, 0);
				endTime = cal.getTimeInMillis();
			}
			else {
				endTime = DracHelper.parseWebDateToMillis(locale, tz,
				    addSvcForm.getEnddate(), addSvcForm.getEndTime());
			}

			String srcGroup = addSvcForm.getSrcGroup();
			String destGroup = addSvcForm.getDestGroup();
			String scheduleName = addSvcForm.getSchName();
			if (DracConstants.EMPTY_STRING.equals(srcGroup)
			    || DracConstants.EMPTY_STRING.equals(destGroup)) {
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new ActionMessage(
				    DracConstants.ADD_SERVICE_SCHED_BAD, scheduleName);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
				return mapping.findForward(DracConstants.ADD_SERVICE_RESULT_JSP_PAGE);
			}

			DracService service = new DracService();
			service.setStartTime(startTime);
			service.setEndTime(endTime);
			service.setScheduleId(id);
			service.setScheduleName(scheduleName);

			// Locate src tna and add it to the service
			EndPointType sourceEndpoint = RequestHandler.INSTANCE
			    .findEndpointByTna(token, srcTna);
			if (sourceEndpoint != null) {
				service.getPath().setSourceEndPoint(sourceEndpoint);
				service.getPath().setSource(sourceEndpoint.getNode());
			}
			else {
				log.warn("Source endpoint " + srcTna
				    + " not found, using string values");
				sourceEndpoint = new EndPointType();
				sourceEndpoint.setName(srcTna);
				service.getPath().setSourceEndPoint(sourceEndpoint);
			}

			// Locate target tna and add it to the service
			EndPointType targetEndpoint = RequestHandler.INSTANCE
			    .findEndpointByTna(token, destTna);
			if (targetEndpoint != null) {
				service.getPath().setTargetEndPoint(targetEndpoint);
				service.getPath().setTarget(targetEndpoint.getNode());
			}
			else {
				log.warn("Target endpoint " + destTna
				    + " not found, using string values");
				targetEndpoint = new EndPointType();
				targetEndpoint.setName(destTna);
				service.getPath().setTargetEndPoint(targetEndpoint);
			}

			service.getPath().getTargetEndPoint().setName(destTna);

			service.getPath().setSrlg(addSvcForm.getSrlg().trim());
			service.getPath().setSharedRiskServiceGroup(addSvcForm.getSrsg().trim());
			service.getPath().setVcatRoutingOption(schedule.getPath().getVcatRoutingOption());
			service.setUserInfo(new UserType(activeUserID, null, srcGroup, destGroup,
			    null, null, null));

			try {
				
				

				String serviceId = RequestHandler.INSTANCE.createService(token,
				    id, service);
				ActionRedirect redirect = new ActionRedirect(
				    mapping.findForward(DracConstants.ADD_SERVICE_RESULT_ACTION));
				redirect.addParameter("newServiceId", serviceId);
				return redirect;
			}
			catch (RequestHandlerException re) {
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new DracActionMessage(re)
				    .getActionMessage(locale);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
				return mapping.findForward("AddServiceAction");

			}
		}
		// Lost the parameters due to login redirect, go back to list of schedules
		return addServiceList(mapping, form, request, response);
	}

	public ActionForward forwardListAlarmsPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(120);
		logEntryBuffer.append("forwardListAlarmsPage: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=setting up get list of service alarms;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		ServiceAlarmForm serviceAlarmForm = (ServiceAlarmForm) form;
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		List<UserGroupName> groups = RequestHandler.INSTANCE
		    .getUserGroupProfileNames(token);
		if (groups.isEmpty()) {
			log.error("No user groups for " + token.getUser());
		}
		serviceAlarmForm.setMemberGroupList(groups);
		return mapping.findForward(DracConstants.LIST_SERVICE_ALARMS_JSP_PAGE);
	}

	public ActionForward forwardListServicesPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "forwardListServicesPage: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=setting up get list of services;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		ListServicesForm serviceForm = (ListServicesForm) form;
		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		List groups = RequestHandler.INSTANCE.getUserGroupProfileNames(token);

		if (groups.isEmpty()) {
			log.warn("No user groups for " + userDetails.getUserID());
		}
		serviceForm.setMemberGroupList(groups);
		return mapping.findForward(DracConstants.LIST_SERVICE_JSP_PAGE);
	}

	public ActionForward getServiceAlarm(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "getServiceAlarms: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=getting list of service alarms;");
		String logEntry = logEntryBuffer.toString();

		/* Entry into page. */
		log.debug(logEntry);

		String alarmId = request.getParameter("alarmId");

		if (!alarmId.equals(DracConstants.EMPTY_STRING)) {
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);

			Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
			TimeZone tz = DracHelper.getTimeZone(token);

			List<ServiceAlarmForm> result = new ArrayList<ServiceAlarmForm>();
			RequestHandler rh = RequestHandler.INSTANCE;
			List<AlarmType> alarms = new ArrayList<AlarmType>();
			AlarmType alarm = rh.queryServiceAlarm(token, alarmId);
			if (alarm != null) {
				alarms.add(alarm);
			}

			result = ServiceHelper.convertListToForm(alarms, locale, tz);

			request.setAttribute(DracConstants.TZSTRING, getTZString(tz, locale));
			request.setAttribute("alarmList", result);

			return mapping.findForward(DracConstants.LIST_SERVICE_ALARMS_RESULT);
		}
		// lost request parameters through authentication redirect
		return new ActionRedirect(
		    mapping.findForward(DracConstants.WELCOME_JSP_PAGE));
	}

	public ActionForward getServiceAlarms(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "getServiceAlarms: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=getting list of service alarms;");
		String logEntry = logEntryBuffer.toString();

		/* Entry into page. */
		log.debug(logEntry);

		ServiceAlarmForm serviceAlarmForm = (ServiceAlarmForm) form;

		if (!serviceAlarmForm.getGroupFilter().equals(DracConstants.EMPTY_STRING)) {

			UserDetails userDetails = (UserDetails) session
			    .getAttribute(DracConstants.AUTH_OBJ);
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);

			Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
			TimeZone tz = DracHelper.getTimeZone(token);

			List<ServiceAlarmForm> result = new ArrayList<ServiceAlarmForm>();
			String startTime = serviceAlarmForm.getStartTime();
			String endTime = serviceAlarmForm.getEndTime();
			String startDate = serviceAlarmForm.getStartDate();
			String endDate = serviceAlarmForm.getEndDate();

			String group = serviceAlarmForm.getGroupFilter();
			List<UserGroupName> filterGroups = new ArrayList<UserGroupName>();
			if (DracConstants.ALL_GROUPS.equals(group)) {
				if (userDetails.getUserPolicyProfile() != null) {
					// leave filterGroups empty if system-admin user, can see everything
					if (userDetails.getUserPolicyProfile().getUserGroupType() != UserGroupType.SYSTEM_ADMIN) {
						filterGroups.addAll(RequestHandler.INSTANCE
						    .getUserGroupProfileNames(token));
					}
				}
				else {
					log.error("User policy profile is null for " + token.getUser());
				}
			}
			else {
				filterGroups.add(new UserGroupName(group));
			}
			if (startTime != null && endTime != null
			    && !startTime.equals(DracConstants.EMPTY_STRING)
			    && !endTime.equals(DracConstants.EMPTY_STRING)) {
				RequestHandler rh = RequestHandler.INSTANCE;
				List<AlarmType> alarms = rh.queryAllServiceAlarms(token,
				    DracHelper.parseWebDateToMillis(locale, tz, startDate, startTime),
				    DracHelper.parseWebDateToMillis(locale, tz, endDate, endTime),
				    filterGroups);

				result = ServiceHelper.convertListToForm(alarms, locale, tz);
			}

			request.setAttribute(DracConstants.TZSTRING, getTZString(tz, locale));
			request.setAttribute("filterFrom", startDate + " " + startTime);
			request.setAttribute("filterTo", endDate + " " + endTime);
			request.setAttribute("alarmList", result);

			return mapping.findForward(DracConstants.LIST_SERVICE_ALARMS_RESULT);
		}
		// lost request parameters through authentication redirect
		return new ActionRedirect(
		    mapping.findForward(DracConstants.LIST_SERVICE_ALARMS_JSP_PAGE));
	}

	public ActionForward getServiceList(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder("getServiceList: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=getting list of services;");
		String logEntry = logEntryBuffer.toString();

		/* Entry into page. */
		log.debug(logEntry);
		ListServicesForm serviceForm = (ListServicesForm) form;

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in listServices.jsp for listing
		// AND
		// in listServicesResult.jsp for activate and cancel (passed to
		// listServicesAction)
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		// No. URL parameters 9 or more indicates a sort operation was called
		// (kludgey yes)
		if (request.getParameterMap().size() < 9) {
			if (serviceForm.getCommand() != null
			    && !serviceForm.getCommand().equals("")) {
				listServicesAction(mapping, form, request, response);
			}
		}


		if (!serviceForm.getStartdate().equals(DracConstants.EMPTY_STRING)
		    && !serviceForm.getEnddate().equals(DracConstants.EMPTY_STRING)) {
			UserDetails userDetails = (UserDetails) session
			    .getAttribute(DracConstants.AUTH_OBJ);
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);

			Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
			TimeZone tz = DracHelper.getTimeZone(token);
			String startDate = serviceForm.getStartdate();
			String endDate = serviceForm.getEnddate();
			String group = serviceForm.getMemberGroup();
			List<UserGroupName> filterGroups = new ArrayList<UserGroupName>();
			if (DracConstants.ALL_GROUPS.equals(group)) {
				if (userDetails.getUserPolicyProfile() != null) {
					// leave filterGroups empty if system-admin user, can see everything
					if (userDetails.getUserPolicyProfile().getUserGroupType() != UserGroupType.SYSTEM_ADMIN) {
						filterGroups.addAll(RequestHandler.INSTANCE
						    .getUserGroupProfileNames(token));
					}
				}
				else {
					log.error("User policy profile is null for " + token.getUser());
				}
			}
			else {
				filterGroups.add(new UserGroupName(group));
			}

			List<DracService> serviceList = RequestHandler.INSTANCE
			    .getAllServices(token,
			        DracHelper.parseWebDateToMillis(locale, tz, startDate),
			        DracHelper.parseWebDateToMillis(locale, tz, endDate, true),
			        filterGroups);
			List<ServiceForm> result = new ArrayList<ServiceForm>(serviceList.size());
			ServiceForm resultForm = null;
			DracService serviceBean = null;
			for (int i = 0; i < serviceList.size(); i++) {
				serviceBean = serviceList.get(i);
				if (serviceBean != null) {
					resultForm = new ServiceForm();
					ServiceHelper.copyProperties(locale, tz, serviceBean, resultForm);
					result.add(resultForm);
				}
			}

			request.setAttribute(DracConstants.TZSTRING, getTZString(tz, locale));
			request.setAttribute("filterFrom", startDate);
			request.setAttribute("filterTo", endDate);
			request.setAttribute("filterGroup", group);
			request.setAttribute("serviceList", result);

			return mapping.findForward(DracConstants.LIST_SERVICE_JSP_PAGE);
		}
		// lost request parameters through authentication redirect
		return mapping.findForward(DracConstants.QUERY_SERVICE_JSP_PAGE);
	}

	public ActionForward serviceDetails(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder("serviceDetails: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=getting details of a service;");
		String logEntry = logEntryBuffer.toString();

		/* Entry into page. */
		log.debug(logEntry);

		String selectedID = request.getParameter("sid");

		if (selectedID != null && !selectedID.equals("")) {
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);

			TimeZone tz = DracHelper.getTimeZone(token);
			Schedule schedule = RequestHandler.INSTANCE.getScheduleForService(
			    token, selectedID);
			Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
			if (schedule != null) {
				ServiceHelper.copyProperties(locale, tz, selectedID, schedule,
				    (ServiceForm) form);
				try {
					((ServiceForm) form).setSrlgInclusions(RequestHandler.INSTANCE
					    .getSRLGListForService(token, selectedID));
				}
				catch (RequestHandlerException e) {
					log.warn("Error looking up SRLG for " + selectedID, e);
				}
			}
			else {
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.SERVICE_NOT_FOUND, selectedID));
				saveMessages(request, messages);
			}

			return mapping.findForward(DracConstants.SERVICE_DETAILS_JSP_PAGE);
		}
		// lost the service ID
		return mapping.findForward(DracConstants.QUERY_SERVICE_JSP_PAGE);
	}

	public ActionForward setupAddService(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder("setupAddService: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=setting up add services2;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		String scheduleId = request.getParameter("sid");
		// if not in URL parameter, check the form
		if (scheduleId == null || scheduleId.equals("")) {
			if (form != null && form instanceof AddServiceForm) {
				scheduleId = ((AddServiceForm) form).getSchId();
			}
		}
		if (scheduleId != null && !scheduleId.equals("")) {
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);

			RequestHandler rh = RequestHandler.INSTANCE;
			Schedule schedule = rh.querySchedule(token, scheduleId);
			if (schedule != null) {
				AddServiceForm addSvcForm = (AddServiceForm) form;
				addSvcForm.setSchId(scheduleId);
				addSvcForm.setSchName(schedule.getName());
				addSvcForm.setRate("" + schedule.getRate());
				addSvcForm.setSrctna(schedule.getSrcTNA());
				addSvcForm.setDesttna(schedule.getDestTNA());

				EndPointType sourceEndpoint = rh.findEndpointByTna(token,
				    schedule.getSrcTNA());
				if (sourceEndpoint != null) {
					addSvcForm.setSrcLayer(sourceEndpoint.getLayer());
				}
				else {
					log.warn("Source endpoint " + schedule.getSrcTNA()
					    + " not found, layer will be blank");
				}
				EndPointType destEndpoint = rh.findEndpointByTna(token,
				    schedule.getDestTNA());
				if (destEndpoint != null) {
					addSvcForm.setDestLayer(destEndpoint.getLayer());
				}
				else {
					log.warn("Target endpoint " + schedule.getDestTNA()
					    + " not found, layer will be blank");
				}

				// For add service, all we need are the user groups, don't care about
				// user ID for PolicyCheck
				UserType userType = schedule.getUserInfo();
				boolean noUserData = false;
				if (userType != null) {
					String sourceUG = userType.getSourceEndpointUserGroup();
					String targetUG = userType.getTargetEndpointUserGroup();
					if (sourceUG != null && targetUG != null) {
						addSvcForm.setSrcGroup(sourceUG);
						addSvcForm.setDestGroup(targetUG);
					}
					else {
						if (sourceUG != null) {
							// if there is no target user group, use the source one
							addSvcForm.setSrcGroup(sourceUG);
							addSvcForm.setDestGroup(sourceUG);
						}
						else {
							noUserData = true;
						}
					}
				}
				else {
					noUserData = true;
				}

				if (noUserData) {
					ActionMessages messages = new ActionMessages();
					ActionMessage message = new ActionMessage(
					    DracConstants.ADD_SERVICE_SCHED_BAD, schedule.getName());
					messages.add(ActionMessages.GLOBAL_MESSAGE, message);
					saveMessages(request, messages);
				}

			}
			else {
				// add error message
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new ActionMessage(
				    DracConstants.SCHED_NOT_FOUND, scheduleId);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
			}
			return mapping.findForward(DracConstants.ADD_SERVICE_JSP_PAGE);
		}
		// no schedule ID provided, forward to the list of schedules page
		return addServiceList(mapping, form, request, response);
	}

	private void listServicesAction(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "listServicesAction: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=performing multiple actions on service;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		ListServicesForm listForm = (ListServicesForm) form;
		String command = listForm.getCommand();
		String[] services = listForm.getSelectedItems();
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in listServicesResult.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (ListServicesForm.CANCEL_ACTION.equals(command)
		    || ListServicesForm.ACTIVATE_ACTION.equals(command)) {
			String sid = listForm.getSelectedItem();
			if (sid != null) {
				services = new String[] { sid };
			}
			else {
				return;
			}
		}

		if (services.length > 0) {
			Map<String, Exception> result = null;
			if (command.startsWith(ListServicesForm.CANCEL_ACTION)) {
				result = RequestHandler.INSTANCE.cancelServices(token, services);
			}
			else if (command.startsWith(ListServicesForm.ACTIVATE_ACTION)) {
				result = RequestHandler.INSTANCE.activateServices(token, services);
			}

			if (result != null) {
				ActionMessages messages = new ActionMessages();
				for (String service : services) {
					Exception ex = result.get(service);
					if (ex != null) {
						// error
						ActionMessage message = new ActionMessage(ex.getMessage(), false);
						messages.add("errors", message);

					}
					else {
						String property = "";
						if (command.startsWith(ListServicesForm.CANCEL_ACTION)) {
							property = DracConstants.CANCEL_SERVICE_SUCCESS_MSG;
						}
						else if (command.startsWith(ListServicesForm.ACTIVATE_ACTION)) {
							property = DracConstants.ACTIVATE_SERVICE_SUCCESS_MSG;
						}
						ActionMessage message = new ActionMessage(property, service);
						messages.add("success", message);
					}
				}
				saveMessages(request, messages);
			}
		}
	}
}
