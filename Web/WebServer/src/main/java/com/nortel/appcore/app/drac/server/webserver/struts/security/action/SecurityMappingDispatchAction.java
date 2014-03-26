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

package com.nortel.appcore.app.drac.server.webserver.struts.security.action;

import java.util.Arrays;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.nortel.appcore.app.drac.common.errorhandling.DracPasswordEvaluationException;
import com.nortel.appcore.app.drac.common.security.authentication.types.PasswordErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.general.action.GeneralMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.security.SecurityHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.security.form.ChangePasswordForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.form.GlobalPolicyForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.UserHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.EditUserForm;

/**
 * Created on Oct 23, 2006
 */
public final class SecurityMappingDispatchAction extends
    DracBaseMappingDispatchAction {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	public ActionForward changePassword(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		ActionMessages messages = new ActionMessages();
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();

		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder("changePassword: userID=");
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

		RequestHandler rh = RequestHandler.INSTANCE;

		ChangePasswordForm pForm = (ChangePasswordForm) form;
		String oldpw = pForm.getOldPassword();

		String newpw = pForm.getNewPassword1();
		String newpw2 = pForm.getNewPassword2();

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		try {
			rh.changePassword(
			    token,
			    activeUserID,
			    oldpw,
			    newpw,
			    newpw2,
			    ClientLoginType.WEB_CLIENT_LOGIN,
			    new IPAddress(request.getRemoteAddr(), String.valueOf(request
			        .getRemotePort())).toString(),request.getSession().getId());
			request.setAttribute("success", "true");

			if (token == null) {
				GeneralMappingDispatchAction.login(request, activeUserID, newpw);

				String forwardURL = request.getParameter("redirect");
				if (forwardURL == null || forwardURL.equals("")
				    || "null".equals(forwardURL)) {
					forwardURL = DracConstants.PAGE_REF_WELCOME;
				}

				response.sendRedirect(forwardURL);
				return null;
			}
		}
		catch (DracPasswordEvaluationException e) {
			log.error("SecurityMappingDispatchAction::changePassword exception.", e);

			// TODO: Provide complete dynamic rule violation listing.
			// For now, implement previous behaviour whereby the first rule violation
			// is listed, and the complete rule set is provided...
			Map<String, String> ruleSet = e.getRuleSet();
			if (ruleSet != null && ruleSet.size() > 0) {
				request.setAttribute("pwRulesFailed", "true");

				for (Map.Entry<String, String> rule : ruleSet.entrySet()) {
					request.setAttribute(rule.getKey(), rule.getValue());
				}
			}

			PasswordErrorCode[] ruleViolations = e.getRuleViolations();
			if (ruleViolations != null && ruleViolations.length > 0) {
				PasswordErrorCode firstErrorCode = ruleViolations[0];
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    firstErrorCode.toString()));
			}
			else {
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.PW_ERROR_GENERAL));
			}
		}

		if (!messages.isEmpty()) {
			saveMessages(request, messages);
		}

		// clear the form of all values, no need to redisplay them
		pForm.setOldPassword("");
		pForm.setNewPassword1("");
		pForm.setNewPassword2("");

		return mapping.findForward(DracConstants.CHANGE_PASSWORD_JSP_PAGE);
	}
	
	@SuppressWarnings("unchecked")
	public ActionForward editGlobalPolicy(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		GlobalPolicyForm gpForm = (GlobalPolicyForm) form;

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in globalPolic.jsp for listing
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (!DracConstants.EMPTY_STRING.equals(gpForm.getRedirected())) {
			UserDetails usrDetails = (UserDetails) session
			    .getAttribute(DracConstants.AUTH_OBJ);
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);
			RequestHandler rh = RequestHandler.INSTANCE;
			GlobalPolicy gp = rh.getGlobalPolicy(token);
			if (gp != null) {
				GlobalPolicy gp2 = SecurityHelper.convertToGlobalPolicy(
				    (Locale) session.getAttribute(DracConstants.MYLOCALE), gpForm,
				    gp.getSupportedAuthenticationData());
				rh.editGlobalPolicy(token, gp2);
			
				rh.setLockedIPs(token, gp2, Arrays.asList(gpForm.getTempLockedClientIPs()));
				request.setAttribute("success", "true");
				if (usrDetails.getUserPolicyProfile().getUserGroupType()
				    .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)) {
					request.setAttribute("editable", "true");
				}
				SecurityHelper.copyProperties(
				    (Locale) session.getAttribute(DracConstants.MYLOCALE), gp2,
				    (GlobalPolicyForm) form);
			}
		}

		// return new
		// ActionRedirect(mapping.findForward(DracConstants.GLOBAL_POLICY_ACTION));
		return mapping.findForward(DracConstants.GLOBAL_POLICY_JSP_PAGE);
	}

	public ActionForward forwardChangePasswordPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this action. */
		StringBuilder logEntryBuffer = new StringBuilder(
		    "forwardChangePasswordPage: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=forward;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		return mapping.findForward(DracConstants.CHANGE_PASSWORD_JSP_PAGE);
	}

	// no instance variables allowed, struts not thread-safe
	public ActionForward getGlobalPolicy(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		UserDetails usrDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		GlobalPolicy gp = RequestHandler.INSTANCE.getGlobalPolicy(token);
		if (usrDetails.getUserPolicyProfile().getUserGroupType()
		    .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)) {
			request.setAttribute("editable", "true");
		}
		SecurityHelper.copyProperties(
		    (Locale) session.getAttribute(DracConstants.MYLOCALE), gp,
		    (GlobalPolicyForm) form);
		return mapping.findForward(DracConstants.GLOBAL_POLICY_JSP_PAGE);
	}

	public ActionForward viewUserProfile(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		// Create base logging string for this page.
		String logEntry = "queryUser: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		// Entry into page.
		log.debug(logEntry + "operation=invoke");

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		TimeZone tz = DracHelper.getTimeZone(token);
		ActionForward forward = null;
		RequestHandler rh = RequestHandler.INSTANCE;

		if (activeUserID != null
		    && !DracConstants.EMPTY_STRING.equals(activeUserID)) {
			forward = mapping.findForward(DracConstants.EDIT_USER_JSP_PAGE);

			UserProfile aUP = rh.getUserProfile(token, activeUserID);

			if (aUP != null) {

				// Vu just added this piece of code temporarily to get "edit access" to
				// work properly
				if (rh.isUserEditable(token, aUP)) {
					/*
					 * ******** WARNING *********** The jsp files appear all to modify for
					 * 'display' versus 'edit' using the struts logic tag "<logic:present"
					 * on the request attribute editable. So, the security model will
					 * break very badly if you set the editable attribute in the request
					 * to 'false' ... it will still be interpreted as true ... thus
					 * granting edit privileges when not otherwise appropriate.
					 */
					request.setAttribute("editable", "editable");
				}

				GlobalPolicy aGP = rh.getGlobalPolicy(token);
				if (aGP != null) {
					UserHelper.populateEditUserForm(locale, tz, aGP, aUP,
					    (EditUserForm) form);
				}
				else {
					// Not found!
					ActionMessages messages = new ActionMessages();
					// To Do: Change this to more correct error message - i.e. GP not
					// found.
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					    DracConstants.USER_NOT_FOUND, activeUserID));
					saveMessages(request, messages);
				}
			}
			else {
				// Not found!
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.USER_NOT_FOUND, activeUserID));
				saveMessages(request, messages);
			}
		}
		return forward;
	}
}
