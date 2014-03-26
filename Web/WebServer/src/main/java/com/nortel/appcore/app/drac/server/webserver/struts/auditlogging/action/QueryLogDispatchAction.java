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

package com.nortel.appcore.app.drac.server.webserver.struts.auditlogging.action;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.webserver.struts.auditlogging.AuditLogHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.auditlogging.form.AuditLoggingForm;
import com.nortel.appcore.app.drac.server.webserver.struts.auditlogging.form.QueryAuditLogForm;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;

/**
 * Created on 16-Aug-06
 */
public final class QueryLogDispatchAction extends DracBaseMappingDispatchAction {
	private static final String LOG_LIST = "auditLogList";
	private static final String NO_LOGS = "no_logs";

	// These page forwards forces validation check by doing executeBefore()
	public ActionForward forwardQueryLogPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		return mapping.findForward(DracConstants.QUERY_LOG_PAGE);
	}

	public ActionForward getLogList(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {

		HttpSession session = request.getSession();
		QueryAuditLogForm queryLogForm = (QueryAuditLogForm) form;
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);

		List<AuditLoggingForm> list = AuditLogHelper.getLogs(token, locale,
		    queryLogForm, tz);

		if (list.isEmpty()) {
			request.setAttribute(NO_LOGS, "no logs found");
		}
		else {

			request.setAttribute(LOG_LIST, list);
			String tzString = getTZString(tz, locale);
			request.setAttribute(DracConstants.TZSTRING, tzString);
		}
		return mapping.findForward(DracConstants.QUERY_LOG_PAGE);
	}
}
