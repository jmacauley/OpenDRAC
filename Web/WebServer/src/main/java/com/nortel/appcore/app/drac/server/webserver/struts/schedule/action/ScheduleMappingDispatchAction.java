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

package com.nortel.appcore.app.drac.server.webserver.struts.schedule.action;

import java.rmi.ServerException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.util.LabelValueBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.errorhandling.DracRemotePolicyException;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.errorhandling.DracRemoteException;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.PathType.PROTECTION_TYPE;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.StatusType;
import com.nortel.appcore.app.drac.common.types.SysMetricType;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracActionMessage;
import com.nortel.appcore.app.drac.server.webserver.struts.general.action.GeneralMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.ScheduleHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.CreateScheduleForm;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.FindTimeForm;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.ListSchedulesForm;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.ScheduleForm;
import com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.TaskForm;

/**
 * Created on 12-Jul-2006
 */
public final class ScheduleMappingDispatchAction extends
    DracBaseMappingDispatchAction {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	static final ArrayList<LabelValueBean> vlanIdSelections = new ArrayList<LabelValueBean>();
	static {
		// Create a list of valid vlanId selections...that includes string flags for
		// untagged and alltagged.
		vlanIdSelections.add(new LabelValueBean(
		    FacilityConstants.UNTAGGED_LOCLBL_FLAG,
		    FacilityConstants.UNTAGGED_LOCLBL_VALUE));
		vlanIdSelections.add(new LabelValueBean(
		    FacilityConstants.ALLTAGGED_LOCLBL_FLAG,
		    FacilityConstants.ALLTAGGED_LOCLBL_VALUE));
		for (int i = 0; i <= FacilityConstants.MAX_VLANTAGGED_LOCLBL; i++) {
			vlanIdSelections.add(new LabelValueBean(Integer.toString(i), Integer
			    .toString(i)));
		}
	}

	public ActionForward cancelCreate(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder("cancelCreate: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=canceling schedule;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in createProgress.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		String taskId = request.getParameter("tid");
		String schName = request.getParameter("name");
		if (taskId != null && !taskId.equals("")) {
			try {
				LoginToken token = (LoginToken) request.getSession().getAttribute(
				    DracConstants.TOKEN_OBJ);
				boolean result = RequestHandler.INSTANCE.cancelProgress(token,
				    taskId);
				log.debug("Result of schedule task " + taskId + " cancellation: "
				    + result);
				request.setAttribute("tid", taskId);
				request.setAttribute("result", result);
			}
			catch (RequestHandlerException re) {
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new DracActionMessage(re)
				    .getActionMessage(locale);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
			}
			return mapping.findForward(DracConstants.CANCEL_CREATE_SCHEDULE_PAGE);
		}
		// no parameters sent along with the request, forward to the schedule query
		// page
		return mapping.findForward(DracConstants.LIST_SCHEDULES_PAGE);
	}

	public ActionForward clearCreateProgress(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "clearCreateProgress: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=invoke;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in listProgressResult.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		TaskForm taskForm = (TaskForm) form;
		String[] tids = taskForm.getSelectedItems();
		if (tids != null && tids.length > 0) {
			RequestHandler rh = RequestHandler.INSTANCE;
			for (String tid : tids) {
				session.setAttribute(tid, null);
				rh.clearProgress(token, tid);

			}
		}
		return listCreateProgress(mapping, form, request, response);
	}

	// not to be shipped
	public ActionForward confirmSchedule(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);

		/* Create base logging string for this page. */
		String logEntry = "confirmSchedule: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=invoke");
		String sid = request.getParameter("sid");
		String schName = request.getParameter("schName");
		ActionForward forward = mapping
		    .findForward(DracConstants.CONFIRM_SCHEDULE_RESULT_PAGE);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		if (sid != null && !DracConstants.EMPTY_STRING.equals(sid)) {
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);
			try {
				RequestHandler.INSTANCE.confirmSchedule(token, sid);
				log.debug("Success in confirming Schedule with ID  " + sid);
				request.setAttribute("confirmID", DracHelper.decodeFromUTF8(schName));
			}
			catch (RequestHandlerException re) {
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new DracActionMessage(re)
				    .getActionMessage(locale);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
			}
		}
		else {
			// may have timed out and lost the sid, redirect to the list page
			forward = mapping.findForward(DracConstants.LIST_SCHEDULES_PAGE);
		}
		return forward;
	}

	public ActionForward createScheduleResult(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "createScheduleResult: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=create schedule result");

		String schid = request.getParameter("sid");
		if (schid != null && !schid.equals("")) {
			RequestHandler rh = RequestHandler.INSTANCE;
			ScheduleForm schedForm = (ScheduleForm) form;

			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);
			TimeZone tz = DracHelper.getTimeZone(token);

			Schedule schedule = null;
			try {
				schedule = rh.querySchedule(token, schid);
			}
			catch (RequestHandlerException e) {
				if (e.getErrorCode() < DracErrorConstants.SECURITY_ERROR_MARKER_START
				    || e.getErrorCode() >= DracErrorConstants.MLBW_ERROR_MARKER_START) {
					throw e;
				}
			}

			if (schedule == null) {
				// get the schedule from the session instead
				schedule = (Schedule) session.getAttribute(schid);
			}

			TaskType taskType = rh.getProgress(token, schid);
			if (taskType != null) {
				List<StatusType> statusInfoList = rh.getStatusInfoList(token, schid);
				if (statusInfoList != null) {
					schedForm.setCreateResult(taskType.getResult().toString());
					ScheduleHelper.setServiceList(locale, tz, statusInfoList, schedForm);
				}
			}
			if (schedule != null) {
				ScheduleHelper.copyScheduleDetails(token, locale, tz, schedule,
				    schedForm);
			}
			else {
				String schName = request.getParameter("name");
				if (schName != null) {
					schName = DracHelper.decodeFromUTF8(schName);
				}
				else {
					// name was lost? use the Schedule ID
					schName = schid;
				}
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new ActionMessage(
				    DracConstants.SCHED_CREATE_REMOVED, schName);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
			}
		}
		else {
			String error = (String) request.getAttribute("message");
			log.error("CreateScheduleResult " + error);
			((ScheduleForm) form).setError(error);
		}
		return mapping.findForward(DracConstants.CREATE_SCHEDULE_RESULT_PAGE);
	}

	public ActionForward deleteSchedule(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);

		/* Create base logging string for this page. */
		String logEntry = "deleteSchedule: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=invoke");
		String sid = request.getParameter("sid");
		String schName = request.getParameter("schName");
		ActionForward forward = mapping
		    .findForward(DracConstants.DELETE_SCHEDULE_RESULT_PAGE);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		if (sid != null && !DracConstants.EMPTY_STRING.equals(sid)) {
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);

			try {
				RequestHandler.INSTANCE.cancelSchedule(token, sid);
				log.debug("Success in deleting Schedule with ID  " + sid);
				request.setAttribute("deletedID", DracHelper.decodeFromUTF8(schName));
			}
			catch (RequestHandlerException re) {
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new DracActionMessage(re)
				    .getActionMessage(locale);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
			}
		}
		else {
			// may have timed out and lost the sid, redirect to the list page
			forward = mapping.findForward(DracConstants.LIST_SCHEDULES_PAGE);
		}
		return forward;
	}

	public ActionForward findAvailableTime(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);
		FindTimeForm querySchForm = (FindTimeForm) form;
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "listSchedulesResult: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=list schedule results;");

		String srcTna = querySchForm.getSrcTna();
		String destTna = querySchForm.getDestTna();
		int minDur = querySchForm.getDuration();
		int rate = querySchForm.getRate();
		querySchForm.setEndPoints(getEndpoints(token));
		request.setAttribute("endPoints", querySchForm.getEndPoints());

		if (!DracConstants.EMPTY_STRING.equals(srcTna)
		    && !DracConstants.EMPTY_STRING.equals(destTna)) {
			List<DracService> serviceTimes = RequestHandler.INSTANCE
			    .getAvailableTimes(token, srcTna, destTna, minDur, rate);
			List<ScheduleForm> result = ScheduleHelper.copyAvailableTimes(locale, tz,
			    serviceTimes);

			request.setAttribute(DracConstants.TZSTRING, getTZString(tz, locale));
			request.setAttribute("querySrc", srcTna);
			request.setAttribute("queryDest", destTna);
			request.setAttribute("scheduleList", result);
			return mapping.findForward(DracConstants.SCH_ADVANCED_SEARCH_RESULTS);
		}
		// lost request parameters through authentication redirect
		return new ActionRedirect(
		    mapping.findForward(DracConstants.SCH_ADVANCED_SEARCH));
	}

	public ActionForward forwardAdvancedSearch(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "forwardAdvancedSearch: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=setting up advanced search;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		FindTimeForm tForm = (FindTimeForm) form;
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		 
		tForm.setEndPoints(getEndpoints(token));
		request.setAttribute("endPoints", tForm.getEndPoints());
		return mapping.findForward(DracConstants.SCH_ADVANCED_SEARCH);
	}

	private Set<Map<String, String>> getEndpoints(LoginToken token) throws Exception {
		List<UserGroupName> userGroupFilter = new ArrayList<UserGroupName>();
		return convertEndpoints(RequestHandler.INSTANCE.getEndpoints(token, "layer2", userGroupFilter));
	}
	private Set<Map<String, String>> convertEndpoints(List<EndPointType> endpoints) {
		SortedSet<Map<String, String>> endPointsList = new TreeSet<Map<String, String>>(
		        new Comparator<Map<String, String>>() {
			        public int compare(Map<String, String> arg0, Map<String, String> arg1) {
				        String name1 = arg0.get("name");
				        String name2 = arg1.get("name");
				        return name1.compareTo(name2);
			        }
		        });
		for (EndPointType type : endpoints) {
			String name = type.getName();
			String label = type.getLabel();
			if (label == null || "".equals(label) || "N/A".equals(label)) {
				label = name;
			}
			Map<String, String> point = new HashMap<String, String>();
			point.put("label", label);
			point.put("name", name);
			endPointsList.add(point);
		}
		return endPointsList;
	}

	public ActionForward forwardListSchedulesPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "forwardListSchedulesPage: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=setting up get list of schedules;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		ListSchedulesForm schForm = (ListSchedulesForm) form;
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		List<UserGroupName> groups = RequestHandler.INSTANCE
		    .getUserGroupProfileNames(token);
		if (groups.isEmpty()) {
			log.warn("No user groups for " + token.getUser());
		}
		schForm.setGroupList(groups);
		return mapping.findForward(DracConstants.LIST_SCHEDULES_PAGE);
	}

	public ActionForward handleCreateSchedule(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this page. */
		String logEntry = "handleCreateSchedule: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		

		// Security (Crystal Box):
		// ===========================================================
		// This will verify schedule creation.
		// Requires token set in createSchedule.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		/* Entry into page. */
		log.debug(logEntry + "operation=handle create schedule;");
		CreateScheduleForm schedForm = (CreateScheduleForm) form;
		try {
			LoginToken token = (LoginToken) session
			.getAttribute(DracConstants.TOKEN_OBJ);
			TimeZone tz = DracHelper.getTimeZone(token);
			
			SimpleDateFormat dateFormatter = new SimpleDateFormat(
				    DracConstants.WEB_GUI_AUDITTIME, locale);
				dateFormatter.setTimeZone(tz);
			
			RequestHandler rh = RequestHandler.INSTANCE;
			final Map<String, String> filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
				    Layer.LAYER2.toString());
			List<EndpointResourceUiType> list = rh.getUserEndpointsUIInfo(token,filter);
			EndpointResourceUiType srcResourceUIInfo = null;
			EndpointResourceUiType destResourceUIInfo = null;
			for (EndpointResourceUiType endpointResourceUiType : list) {
				if (srcResourceUIInfo == null) {
					if (endpointResourceUiType.getTna().equals(schedForm.getSrcTna())) {
						srcResourceUIInfo = endpointResourceUiType;
					}
				}
				if (destResourceUIInfo == null) {
					if (endpointResourceUiType.getTna().equals(schedForm.getDestTna())) {
						destResourceUIInfo = endpointResourceUiType;
					}
				}
				if (srcResourceUIInfo != null && destResourceUIInfo != null) {
					break;
				}
			}
			
			List<ResourceGroupProfile> resGroups = rh.getResourceGroups(token);
			Iterator<ResourceGroupProfile> it = resGroups.iterator();
			// fill in the source and destination resource group
			ResourceGroupProfile resGroup;
			String srcResGroup = null;
			String destResGroup = null;
			while (it.hasNext()) {
				resGroup = it.next();
				list = rh.getUserEndpointsUIInfo(token,resGroup.getName(), filter);
				if (srcResGroup == null) {
					if (list.contains(srcResourceUIInfo)) {
						srcResGroup = resGroup.getName();
					}
				}
				if (destResGroup == null) {
					if (list.contains(destResourceUIInfo)) {
						destResGroup = resGroup.getName();
					}
				}
				if (srcResGroup != null && destResGroup != null) break;
			}
			schedForm.setSrcResGroup(srcResGroup);
			schedForm.setDestResGroup(destResGroup);
			schedForm.setSchName(activeUserID + " " 
				+ dateFormatter.format(new Date(System.currentTimeMillis())) );
			schedForm.setProtectionType(PROTECTION_TYPE.PATH1PLUS1.toString());
			schedForm.setVcatRoutingOption(true);
			
			// default src & dest group to billing group value
			schedForm.setSrcGroup(schedForm.getBillingGroup());
			schedForm.setDestGroup(schedForm.getBillingGroup());
			
			Schedule sch = ScheduleHelper.create(token, locale, schedForm);
			ActionRedirect redirect = new ActionRedirect(
				    mapping.findForward(DracConstants.CREATE_SCHEDULE_PROGRESS_PAGE));
			redirect.addParameter("sid", sch.getId());

			session.setAttribute(sch.getId(), sch);
			return redirect;
		}
		catch (RequestHandlerException e) {
			if (!(e.getCause() instanceof DracRemoteException)) {
				throw e;
			}
			ActionMessages messages = new ActionMessages();
			ActionMessage message = new DracActionMessage(e)
			.getActionMessage(locale);
			messages.add(ActionMessages.GLOBAL_MESSAGE, message);
			saveMessages(request, messages);
			return mapping.findForward(DracConstants.CREATE_SCHEDULE_ACTION);
		}
		catch (ServerException ex) {
			if (ex.getCause() instanceof NrbException) {
				NrbException nrbEx = (NrbException) ex.getCause();
				if (nrbEx.getCause() instanceof DracRemotePolicyException) {
					DracRemotePolicyException dpe = (DracRemotePolicyException) nrbEx.getCause();
					ActionMessages messages = new ActionMessages();

					ActionMessage message = new DracActionMessage(new DracException(dpe.getErrorCode(), dpe.getArgs()))
					.getActionMessage(locale);
					messages.add(ActionMessages.GLOBAL_MESSAGE, message);
					saveMessages(request, messages);
					return mapping.findForward(DracConstants.CREATE_SCHEDULE_ACTION);
				} //else log.error("Not remote policy" + nrbEx.getCause().getClass().getCanonicalName());
			} //else log.error("Nnot NrbException" + ex.getCause().getClass().getCanonicalName());
			throw ex;
		}

	}
	
	public ActionForward handleCreateAdvancedSchedule(ActionMapping mapping,
		    ActionForm form, HttpServletRequest request, HttpServletResponse response)
		    throws Exception {
			HttpSession session = request.getSession();
			/*****************************************************************************************************
			 * User must be valid so we are onto the main page logic.
			 ****************************************************************************************************/
			String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
			String sessionID = session.getId();
			/* Create base logging string for this page. */
			String logEntry = "handleCreateAdvancedSchedule: userID=" + activeUserID
			    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
			    + ":protocol=" + request.getProtocol() + ":";
			Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);

			// Security (Crystal Box):
			// ===========================================================
			// This will verify schedule creation.
			// Requires token set in createSchedule.jsp
			GeneralMappingDispatchAction.verifyCSRFToken(session, request);

			/* Entry into page. */
			log.debug(logEntry + "operation=handle create advanced schedule;");
			CreateScheduleForm schedForm = (CreateScheduleForm) form;
			if (!DracConstants.EMPTY_STRING.equals(schedForm.getSchName())) {
				try {
					LoginToken token = (LoginToken) session
					    .getAttribute(DracConstants.TOKEN_OBJ);
					Schedule sch = ScheduleHelper.create(token, locale, schedForm);
					request.setAttribute("name", schedForm.getSchName());

					ActionRedirect redirect = new ActionRedirect(
					    mapping.findForward(DracConstants.CREATE_SCHEDULE_PROGRESS_PAGE));
					redirect.addParameter("sid", sch.getId());
					session.setAttribute(sch.getId(), sch);
					return redirect;
				}
				catch (RequestHandlerException e) {
					if (!(e.getCause() instanceof DracRemoteException)) {
						throw e;
					}
					ActionMessages messages = new ActionMessages();
					ActionMessage message = new DracActionMessage(e)
					    .getActionMessage(locale);
					messages.add(ActionMessages.GLOBAL_MESSAGE, message);
					saveMessages(request, messages);
					return mapping.findForward(DracConstants.CREATE_SCHEDULE_ACTION);
				}
				catch (ServerException ex) {
				    if (ex.getCause() instanceof NrbException) {
				        NrbException nrbEx = (NrbException) ex.getCause();
				        if (nrbEx.getCause() instanceof DracRemotePolicyException) {
				            DracRemotePolicyException dpe = (DracRemotePolicyException) nrbEx.getCause();
			                ActionMessages messages = new ActionMessages();
			                
			                ActionMessage message = new DracActionMessage(new DracException(dpe.getErrorCode(), dpe.getArgs()))
			                    .getActionMessage(locale);
			                messages.add(ActionMessages.GLOBAL_MESSAGE, message);
			                saveMessages(request, messages);
			                return mapping.findForward(DracConstants.CREATE_SCHEDULE_ACTION);
				        } //else log.error("Not remote policy" + nrbEx.getCause().getClass().getCanonicalName());
				    } //else log.error("Nnot NrbException" + ex.getCause().getClass().getCanonicalName());
				    throw ex;
				}
			}
			// lost request parameters through authenticate redirect
			return new ActionRedirect(
			    mapping.findForward(DracConstants.CREATE_SCHEDULE_ACTION));
		}

	public ActionForward listCreateProgress(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		/* Create base logging string for this action. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		StringBuilder logEntryBuffer = new StringBuilder(
		    "listCreateProgress: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=invoke;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		List<TaskType> tasks = RequestHandler.INSTANCE.getProgress(token);
		List<TaskForm> taskList = new ArrayList<TaskForm>();
		TaskForm taskForm = null;

		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		if (userDetails != null && userDetails.getUserPolicyProfile() != null) {
			// This is the one command that is accessed controlled in the menu.jsp at
			// the client.
			// For users belonging to usergroup type of 'USER', this command is not
			// permitted.
			if (userDetails.getUserPolicyProfile().getUserGroupType() == UserGroupType.USER) {
				log.debug("The user " + activeUserID
				    + " does not have access privilege to ListCreateProgress.");

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { "user : " + activeUserID,
				        " ListCreateProgress is not supported for this user." });
			}

			for (TaskType t : tasks) {
				if (t.getActivityName().equals("createSchedule")) {
					taskForm = new TaskForm();
					ScheduleHelper.copyProperties(t, taskForm);
					taskList.add(taskForm);
				}
			}

		}

		request.setAttribute("taskList", taskList);
		return mapping.findForward(DracConstants.LIST_CREATE_PROGRESS_PAGE);
	}

	public ActionForward listSchedules(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		try {
			HttpSession session = request.getSession();
			Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
			UserDetails userDetails = (UserDetails) session
			    .getAttribute(DracConstants.AUTH_OBJ);
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);
			TimeZone tz = DracHelper.getTimeZone(token);
			ListSchedulesForm querySchForm = (ListSchedulesForm) form;
			/*************************************************************************************************
			 * User must be valid so we are onto the main page logic.
			 ************************************************************************************************/

			/* Create base logging string for this page. */
			String logEntry = "listSchedulesResult: userID="
			    + session.getAttribute(DracConstants.USER_ID) + ":sessionID="
			    + session.getId() + ":ipAddress=" + request.getRemoteAddr()
			    + ":protocol=" + request.getProtocol() + ":";

			/* Entry into page. */
			log.debug(logEntry + "operation=list schedule results;");

			// Security (Crystal Box):
			// ===========================================================
			// This will verify schedule list requests, confirmation, and cancellation
			// (calls into
			// listSchedulesAction).
			// Requires token set in listSchedules.jsp, listSchedulesResult.jsp
			GeneralMappingDispatchAction.verifyCSRFToken(session, request);

			// No. URL parameters 8 or more indicates a sort operation was called
			// (kludgey yes)
			if (request.getParameterMap().size() < 8) {
				if (querySchForm.getCommand() != null
				    && !querySchForm.getCommand().equals("")) {
					listSchedulesAction(mapping, form, request, response);
				}
			}

			String date = querySchForm.getStartdate();
			String date1 = querySchForm.getEnddate();
			String group = querySchForm.getGroup();

			if (!DracConstants.EMPTY_STRING.equals(date)
			    && !DracConstants.EMPTY_STRING.equals(date1)) {
				long startTime = 0;
				long endTime = 0;

				if (date != null && !DracConstants.EMPTY_STRING.equals(date)) {
					startTime = DracHelper.parseWebDateToMillis(
					    (Locale) session.getAttribute(DracConstants.MYLOCALE), tz, date);
				}
				else {
					log.error(logEntry + "error=Start date is null;");
				}

				if (date1 != null && !DracConstants.EMPTY_STRING.equals(date1)) {
					endTime = DracHelper.parseWebDateToMillis(
					    (Locale) session.getAttribute(DracConstants.MYLOCALE), tz, date1,
					    true);
				}
				else {
					log.error(logEntry + "error=End date is null;");
				}

				List<UserGroupName> filterGroups = new ArrayList<UserGroupName>();
				if (DracConstants.ALL_GROUPS.equals(group)) {
					if (userDetails.getUserPolicyProfile() != null) {
						if (userDetails.getUserPolicyProfile().getUserGroupType() != UserGroupType.SYSTEM_ADMIN) {
							filterGroups.addAll(RequestHandler.INSTANCE
							    .getUserGroupProfileNames(token));
						}
						// else if system admin, leave the filter group empty, he can see
						// everything
					}
					else {
						log.error(logEntry + "User policy profile is null for "
						    + userDetails.getUserID());
					}
				}
				else {
					filterGroups.add(new UserGroupName(group));
				}
				List<Schedule> scheduleList = RequestHandler.INSTANCE
				    .querySchedules(token, startTime, endTime, filterGroups);
				List<ScheduleForm> result = ScheduleHelper.copyProperties(locale, tz,
				    scheduleList);

				request.setAttribute(DracConstants.TZSTRING, getTZString(tz, locale));
				request.setAttribute("filterFrom", date);
				request.setAttribute("filterTo", date1);
				request.setAttribute("filterGroup", group);
				request.setAttribute("scheduleList", result);

				log.debug(logEntry + " forwarding to results page");
				return mapping.findForward(DracConstants.LIST_SCHEDULES_RESULT_PAGE);
			}
			// lost request parameters through authentication redirect
			log.debug(logEntry + " lost parameters, redirecting to authenticate!");
			return new ActionRedirect(
			    mapping.findForward(DracConstants.LIST_SCHEDULES_PAGE));
		}
		catch (Exception t) {
			log.error("listSchedules", t);
			throw new Exception(t);
		}
	}

	public ActionForward processSchedule(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		/* Create base logging string for this page. */
		String logEntry = "scheduleInfo: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=invoke");
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);

		ListSchedulesForm scheduleForm = (ListSchedulesForm) form;

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in scheduleDetails.jsp
		// Handles many operations e.g. schedule cancel,confirm,addservice; service
		// activate,cancel
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (scheduleForm.getId() != null && !scheduleForm.getId().equals("")) {
			ActionMessages messages = new ActionMessages();
			String command = scheduleForm.getCommand();
			String[] items = null;

			if (ListSchedulesForm.CANCEL_SERVICES_ACTION.equals(command)
			    || ListSchedulesForm.ACTIVATE_ACTION.equals(command)) {
				items = scheduleForm.getSelectedItems();
			}
			else if (ListSchedulesForm.CANCEL_ACTION.equals(command)
			    || ListSchedulesForm.CONFIRM_ACTION.equals(command)) {
				items = new String[] { scheduleForm.getId() };
			}

			Map<String, Exception> result = null;
			if (ListSchedulesForm.CANCEL_SERVICES_ACTION.equals(command)) {
				result = RequestHandler.INSTANCE.cancelServices(token, items);
			}
			else if (ListSchedulesForm.ACTIVATE_ACTION.equals(command)) {
				result = RequestHandler.INSTANCE.activateServices(token, items);
			}
			else if (ListSchedulesForm.CANCEL_ACTION.equals(command)) {
				result = RequestHandler.INSTANCE.cancelSchedules(token, items);
			}
			else if (ListSchedulesForm.CONFIRM_ACTION.equals(command)) {
				result = RequestHandler.INSTANCE.confirmSchedules(token, items);
			}

			if (result != null) {

				for (String item : items) {
					Exception ex = result.get(item);
					if (ex != null) {
						// error
						ActionMessage message = new ActionMessage(ex.getMessage(), false);
						messages.add("errors", message);
					}
				}
				if (!messages.isEmpty()) {
					Schedule scd = RequestHandler.INSTANCE.querySchedule(token,
					    scheduleForm.getId());
					ScheduleForm schForm = new ScheduleForm();
					ScheduleHelper.copyProperties(token,
					    (Locale) session.getAttribute(DracConstants.MYLOCALE), tz, scd,
					    schForm);
					request.setAttribute("ScheduleData", schForm);
					scheduleForm.reset();
					saveMessages(request, messages);
					return mapping.findForward(DracConstants.SCHEDULE_DETAILS_PAGE);
				}
				ActionRedirect redirect = new ActionRedirect(
				    mapping.findForward(DracConstants.QUERY_SCHEDULE_ACTION));
				redirect.addParameter("sid", scheduleForm.getId());
				return redirect;
			}
			return new ActionRedirect(
			    mapping.findForward(DracConstants.LIST_SCHEDULES_PAGE));
		}
		return new ActionRedirect(
		    mapping.findForward(DracConstants.LIST_SCHEDULES_PAGE));
	}

	public ActionForward querySchedule(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		/* Create base logging string for this page. */
		String logEntry = "scheduleInfo: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=invoke");

		String sid = request.getParameter("sid");
		ActionForward forward = null;
		if (sid != null && !DracConstants.EMPTY_STRING.equals(sid)) {
			RequestHandler rh = RequestHandler.INSTANCE;
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);
			TimeZone tz = DracHelper.getTimeZone(token);

			forward = mapping.findForward(DracConstants.SCHEDULE_DETAILS_PAGE);
			Schedule scd = rh.querySchedule(token, sid);
			if (scd != null) {
				ScheduleForm sForm = new ScheduleForm();
				ScheduleHelper.copyProperties(token,
				    (Locale) session.getAttribute(DracConstants.MYLOCALE), tz, scd,
				    sForm);
				request.setAttribute("ScheduleData", sForm);

			}
			else {
				// not found!
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.SCHED_NOT_FOUND, sid));
				saveMessages(request, messages);
			}
		}
		else {
			// may have timed out and lost the sid, redirect to the list page
			forward = new ActionRedirect(
			    mapping.findForward(DracConstants.LIST_SCHEDULES_PAGE));
		}
		return forward;
	}

	// no instance variables allowed, struts not thread-safe
	public ActionForward setupCreateAdvancedSchedule(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this page. */
		String logEntry = "setupCreateSchedule: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=setup create schedule;");
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		CreateScheduleForm schedForm = (CreateScheduleForm) form;
		RequestHandler rh = RequestHandler.INSTANCE;
		List<UserGroupProfile> userGroups = rh.getUserGroups(token);
		Set<UserGroupName> groupSet = new TreeSet<UserGroupName>();
		for (UserGroupProfile group : userGroups) {
			groupSet.add(group.getName());
		}

		SysMetricType sysMetric = rh.getSystemMetric(token);
		if (sysMetric != null) {
			int offsetMillis = sysMetric.getConfigureOffset();
			if (offsetMillis < 999) {
				schedForm.setSystemOffsetTime(offsetMillis + "ms");
			}
			else if (offsetMillis >= 1000 && offsetMillis < 59999) {
				schedForm.setSystemOffsetTime((double) offsetMillis / 1000 + "s");
			}
			else {
				int minutes = offsetMillis / 60000;
				int remain = offsetMillis - 60000 * minutes;
				double secs = 0;
				if (remain > 0) {
					secs = (double) remain / 1000;
				}
				schedForm.setSystemOffsetTime(minutes + "m "
				    + (secs > 0 ? secs + "s" : ""));
			}
		}

		schedForm.getGroups().addAll(groupSet);

		request.setAttribute("VlanIdSelections", vlanIdSelections);

		return mapping.findForward(DracConstants.CREATE_SCHEDULE_JSP_PAGE);
	}
	
	public ActionForward setupCreateSchedule(ActionMapping mapping,
		    ActionForm form, HttpServletRequest request, HttpServletResponse response)
		    throws Exception {
			/*****************************************************************************************************
			 * User must be valid so we are onto the main page logic.
			 ****************************************************************************************************/
			HttpSession session = request.getSession();
			String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
			String sessionID = session.getId();
			UserDetails userDetails = (UserDetails) request.getSession()
		    .getAttribute(DracConstants.AUTH_OBJ);
			/* Create base logging string for this page. */
			String logEntry = "setupCreateScheduleSimple: userID=" + activeUserID
			    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
			    + ":protocol=" + request.getProtocol() + ":";

			/* Entry into page. */
			log.debug(logEntry + "operation=setup create schedule simple;");
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);
			CreateScheduleForm schedForm = (CreateScheduleForm) form;
			RequestHandler rh = RequestHandler.INSTANCE;
			List<UserGroupProfile> userGroups = rh.getUserGroups(token);
			Set<UserGroupName> groupSet = new TreeSet<UserGroupName>();
			for (UserGroupProfile group : userGroups) {
				groupSet.add(group.getName());
			}
			
			// default billing group is the first one returned
			schedForm.setBillingGroup(userGroups.get(0).getName().toString());
			
			// fetch all endpoints
			List<UserGroupProfile> userGroupList = RequestHandler.INSTANCE
		    .getUserGroups(token);

			// convert the list into just names
			Set<UserGroupName> treeSet = new TreeSet<UserGroupName>();
			for (UserGroupProfile userGroup : userGroupList) {
				treeSet.add(userGroup.getName());
			}
			List<UserGroupName> result = new ArrayList<UserGroupName>(treeSet);
			treeSet = null;
		
			List<UserGroupName> filterGroups = new ArrayList<UserGroupName>();
			if (userDetails.getUserPolicyProfile() != null) {
				// leave filterGroups empty if system-admin user, can see everything
				if (userDetails.getUserPolicyProfile().getUserGroupType() != UserGroupType.SYSTEM_ADMIN) {
					filterGroups.addAll(result);
				}
				else {
					log.debug(logEntry + " User policy profile is null for "
							+ userDetails.getUserID());
				}
			}
			List<String> endpointList = RequestHandler.INSTANCE.getEndpointNamesByLayer(token, Layer.LAYER2);
			if (endpointList != null) {
				log.debug(logEntry + " got " + endpointList.size() + " endpoints ");
				schedForm.setTnas(endpointList);
			}
			SysMetricType sysMetric = rh.getSystemMetric(token);
			if (sysMetric != null) {
				int offsetMillis = sysMetric.getConfigureOffset();
				if (offsetMillis < 999) {
					schedForm.setSystemOffsetTime(offsetMillis + "ms");
				}
				else if (offsetMillis >= 1000 && offsetMillis < 59999) {
					schedForm.setSystemOffsetTime((double) offsetMillis / 1000 + "s");
				}
				else {
					int minutes = offsetMillis / 60000;
					int remain = offsetMillis - 60000 * minutes;
					double secs = 0;
					if (remain > 0) {
						secs = (double) remain / 1000;
					}
					schedForm.setSystemOffsetTime(minutes + "m "
					    + (secs > 0 ? secs + "s" : ""));
				}
			}

			schedForm.getGroups().addAll(groupSet);
			final Map<String, String> filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
				    Layer.LAYER2.toString());
			List<EndpointResourceUiType> list = rh.getUserEndpointsUIInfo(token,filter);
			EndpointResourceUiType srcResourceUIInfo = null;
			EndpointResourceUiType destResourceUIInfo = null;
			for (EndpointResourceUiType endpointResourceUiType : list) {
				if (srcResourceUIInfo == null) {
					if (endpointResourceUiType.getTna().equals(schedForm.getSrcTna())) {
						srcResourceUIInfo = endpointResourceUiType;
					}
				}
				if (destResourceUIInfo == null) {
					if (endpointResourceUiType.getTna().equals(schedForm.getDestTna())) {
						destResourceUIInfo = endpointResourceUiType;
					}
				}
				if (srcResourceUIInfo != null && destResourceUIInfo != null) {
					break;
				}
			}
			
			List<ResourceGroupProfile> resGroups = rh.getResourceGroups(token);
			Iterator<ResourceGroupProfile> it = resGroups.iterator();
			
			// fill in the source and destination resource group
			ResourceGroupProfile resGroup;
			String srcResGroup = null;
			String destResGroup = null;

			if(resGroups!=null && resGroups.size()>0){
				resGroup = resGroups.get(0);
				srcResGroup = resGroup.getName();
				destResGroup = resGroup.getName();
			}
			schedForm.setSrcResGroup(srcResGroup);
			schedForm.setDestResGroup(destResGroup);			
			
			
			request.setAttribute("VlanIdSelections", vlanIdSelections);

			return mapping.findForward(DracConstants.CREATE_SCHEDULE_SIMPLE_JSP_PAGE);
		}

	private void listSchedulesAction(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "listSchedulesAction: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=performing multiple actions on schedule;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		ListSchedulesForm listForm = (ListSchedulesForm) form;

		String command = listForm.getCommand();
		String property = "";
		String[] schedules = listForm.getSelectedItems();
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		if (ListSchedulesForm.CANCEL_ACTION.equals(command)
		    || ListSchedulesForm.CONFIRM_ACTION.equals(command)) {
			String sid = listForm.getSelectedItem();
			if (sid != null) {
				schedules = new String[] { sid };
			}
			else {
				return;
			}
		}

		if (schedules.length > 0) {
			Map<String, Exception> result = null;
			if (command.startsWith(ListSchedulesForm.CANCEL_ACTION)) {
				result = RequestHandler.INSTANCE.cancelSchedules(token, schedules);
				property = DracConstants.CANCEL_SCHED_SUCCESS_MSG;
			}
			else if (command.startsWith(ListSchedulesForm.CONFIRM_ACTION)) {
				result = RequestHandler.INSTANCE
				    .confirmSchedules(token, schedules);
				property = DracConstants.CONFIRM_SCHED_SUCCESS_MSG;
			}

			if (result != null) {
				ActionMessages messages = new ActionMessages();
				for (String schedule : schedules) {
					Exception ex = result.get(schedule);
					if (ex != null) {
						// error
						ActionMessage message = new ActionMessage(ex.getMessage(), false);
						messages.add("errors", message);

					}
					else {
						String name = RequestHandler.INSTANCE
						    .querySchedule(token, schedule).getName();
						ActionMessage message = new ActionMessage(property, name);
						messages.add("success", message);
					}
				}
				saveMessages(request, messages);
			}
		}
	}
}
