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

package com.nortel.appcore.app.drac.server.webserver.struts.network.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.UtilizationStructure;
import com.nortel.appcore.app.drac.common.utility.OpticalUtility;
import com.nortel.appcore.app.drac.common.utility.OpticalUtility.OpticalPortType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.network.EndpointHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.network.form.QueryUtilForm;
import com.nortel.appcore.app.drac.server.webserver.struts.network.form.UtilListForm;
import com.nortel.appcore.app.drac.server.webserver.struts.network.graphicalview.UtilizationCountData;

public final class QueryUtilDispatchAction extends
    DracBaseMappingDispatchAction {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	public ActionForward getUtilClick(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		StringBuilder logEntryBuffer = new StringBuilder(
		    "RedirectSchedule: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());

		String strGV = request.getParameter("Graphical View");
		if (strGV != null && !strGV.trim().equals(DracConstants.EMPTY_STRING)) {
			log.debug(logEntryBuffer.toString() + "forwarding to notImplemented.jsp;");
			ActionForward QueryUtilListGVForward = mapping
			    .findForward(DracConstants.GRAPHICAL_VIEW_ACTION);
			return QueryUtilListGVForward;

		}
		ActionForward QueryUtilListBackForward = mapping
		    .findForward(DracConstants.QUERY_UTIL_LIST_JSP_PAGE);
		return QueryUtilListBackForward;
	}

	// no instance variables allowed, struts not thread-safe
	public ActionForward getUtilEndPointList(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder("getUtilization: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=querying for utilization;");

		log.debug(logEntryBuffer.toString());

		// this will have endpoint list values for the JSP

		LoginToken token = (LoginToken) request.getSession().getAttribute(
		    DracConstants.TOKEN_OBJ);
		List<String> endPointList = RequestHandler.INSTANCE.getEndpointNames(
		    token);

		QueryUtilForm queryUtilForm = (QueryUtilForm) form;
		queryUtilForm.setEndPointList(endPointList);

		return mapping.findForward(DracConstants.QUERY_UTIL_JSP_PAGE);
	}

	public ActionForward getUtilList(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "getUtilizationList: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=querying for utilization list;");

		/* Entry into page. */
		log.debug(logEntryBuffer.toString());

		QueryUtilForm queryUtilForm = (QueryUtilForm) form;
		String tna = queryUtilForm.getEndpoint();

		if (DracConstants.EMPTY_STRING.equals(tna)) {
			// lost request parameters through authentication redirect
			return mapping.findForward(DracConstants.QUERY_UTIL_PAGE);
		}

		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		LoginToken token = (LoginToken) request.getSession().getAttribute(
		    DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);
		RequestHandler rh = RequestHandler.INSTANCE;
		EndPointType ep = rh.findEndpointByTna(token, tna);
		if (ep == null) {
			log.error("getUtilizationList unable to map tna " + tna
			    + " into endPoingType");
			return mapping.findForward(DracConstants.QUERY_UTIL_LIST_JSP_PAGE);
		}

		boolean isLayer1 = ep.getLayer().equals("layer1");
		double speed = 0.0;
		if (isLayer1) {
			OpticalPortType port = OpticalUtility.lookupOptical(ep.getType());
			if (port != null) {
				speed = port.getRate();
			}
		}
		else {
			speed = ep.getDataRate();
		}

		long startTime = DracHelper.parseWebDateToMillis(locale, tz,
		    queryUtilForm.getStartdate(), false);

		// calculate end time based on the selected range
		long endTime = 0;
		int daysBetween = 1;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(startTime);

		if (queryUtilForm.getRange().equals(QueryUtilForm.RANGE_ONE_DAY)) {
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		else if (queryUtilForm.getRange().equals(QueryUtilForm.RANGE_ONE_WEEK)) {
			cal.add(Calendar.DAY_OF_YEAR, 7);
		}
		else if (queryUtilForm.getRange().equals(QueryUtilForm.RANGE_ONE_MONTH)) {
			cal.add(Calendar.MONTH, 1);
		}
		else if (queryUtilForm.getRange().equals(QueryUtilForm.RANGE_THREE_MONTH)) {
			cal.add(Calendar.MONTH, 3);
		}
		endTime = cal.getTimeInMillis();
		// daysBetween used to determine how many 1-minute buckets we need to
		// calculate utilization
		daysBetween = (int) Math.ceil((endTime - startTime)
		    / (DracConstants.SECS_PER_DAY * 1000));

		UtilizationStructure u = new UtilizationStructure(
		    new ArrayList<DracService>(), new double[] {});
		if (speed > 0.0 && startTime > -1 && endTime > -1) {
			u = rh.getUtilization(token, tna, speed, startTime, endTime, daysBetween,
			    tz);
		}

		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		UtilizationCountData utilData = null;
		if (isLayer1) {
			utilData = new UtilizationCountData(startTime, u.getBandwidth(), false,
			    bundle.getString("drac.network.utilization.legend1"), null);
		}
		else {
			boolean fill = ep.isEthWanEPL();

			utilData = new UtilizationCountData(startTime, u.getBandwidth(), fill,
			    bundle.getString("drac.network.utilization.legend1"),
			    bundle.getString("drac.network.utilization.legend2"));
		}

		ArrayList<UtilListForm> result = new ArrayList<UtilListForm>(u
		    .getServiceList().size());
		UtilListForm uForm = null;

		for (DracService serviceBean : u.getServiceList()) {
			if (serviceBean != null) {
				uForm = new UtilListForm();
				EndpointHelper.copyProperties(locale, tz, serviceBean, uForm);
				result.add(uForm);
			}
		}

		request.setAttribute(DracConstants.TZSTRING, getTZString(tz, locale));
		request.setAttribute("range", queryUtilForm.getRange());
		request.setAttribute("utilList", result);
		request.setAttribute("utilCount", utilData);
		request.setAttribute("date", queryUtilForm.getStartdate());
		request.setAttribute("tna", tna);

		return mapping.findForward(DracConstants.QUERY_UTIL_LIST_JSP_PAGE);

	}

}
