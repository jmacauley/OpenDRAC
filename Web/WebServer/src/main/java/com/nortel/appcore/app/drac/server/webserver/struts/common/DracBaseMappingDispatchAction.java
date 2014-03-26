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

package com.nortel.appcore.app.drac.server.webserver.struts.common;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.MappingDispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.errorhandling.DracRemoteException;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;

public class DracBaseMappingDispatchAction extends MappingDispatchAction {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	/*
	 * See http://www.owasp.org/index.php?title=Session_Management&setlang=es#
	 * Associating_Session_Information_With_IP_Address If these points prove to be
	 * an issue, provide an option to override (via system parameter specified in
	 * launcher.xml for the Tomcat target.
	 */
	private static final Boolean allowIpHopping = Boolean
	    .getBoolean("drac.security.allowIpHopping");

	// Control the display of the exception stack trace.
	private static final boolean displayException = Boolean.getBoolean("org.opendrac.web.show.exceptions");

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();

		if (session != null && request != null) {
			String debugInfoString = " userId: "
			    + (String) session.getAttribute(DracConstants.USER_ID) + " locale: "
			    + session.getAttribute(DracConstants.MYLOCALE) + " requestURI: "
			    + request.getRequestURI() + " sessionId: " + session.getId()
			    + " token: " + session.getAttribute(DracConstants.TOKEN_OBJ)
			    + " sessionIpAddress: "
			    + (String) session.getAttribute(DracConstants.SessionIpAddress)
			    + " ipAddress: " + request.getRemoteAddr() + " pageRef: "
			    + (String) request.getAttribute(DracConstants.PAGE_REF)
			    + " queryString: " + request.getQueryString() + " forwardURL: "
			    + request.getParameter("redirect");

			
		}

		try {
			ActionForward execBefForward = executeBefore(mapping, form, request,
			    response);
			ActionForward actionForward = null;
			if (execBefForward == null) {
				actionForward = super.execute(mapping, form, request, response);
				if (actionForward != null) {
					return actionForward;
				}
				return null;
				// executeAfter
			}
			return execBefForward;
		}
		catch (RequestHandlerException e) {
			log.error("Caught requesthandler exception", e);
			Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
			request.setAttribute(DracConstants.ERROR_CODE, e.getKey()
			    .getKeyAsErrorCode());

			if (e.getErrorCode() == DracErrorConstants.RH_ERROR_6002_REDUNDANCY_SWITCHOVER_IN_PROGRESS) {
				return mapping.findForward(DracConstants.REDUNDANCY_SWITCH_JSP_PAGE);
			}
			else if (e.getErrorCode() >= DracErrorConstants.SECURITY_ERROR_MARKER_START
			    && e.getErrorCode() < DracErrorConstants.MLBW_ERROR_MARKER_START) {
				String msg = DracErrorConstants.formatErrorCode(locale, e);
				request.setAttribute(DracConstants.ERROR_MESSAGE, msg);
				return mapping.findForward(DracConstants.NO_PERMISSION_JSP_PAGE);
			}

			if (displayException) {
				request.setAttribute(DracConstants.DRAC_ERROR, e);
				request.setAttribute(DracConstants.CAUSE_EXCEPTION, e.getCause());
			}

			if (e.getCause() instanceof DracException) {
				DracException ex = (DracException) e.getCause();
				String msg = DracErrorConstants.formatErrorCode(locale, ex);
				request.setAttribute(DracConstants.ERROR_MESSAGE, msg);
			}
			else if (e.getCause() instanceof DracRemoteException) {
				DracRemoteException ex = (DracRemoteException) e.getCause();
				String msg = DracErrorConstants.formatErrorCode(locale, ex);
				request.setAttribute(DracConstants.ERROR_MESSAGE, msg);
			}
			else {
				String msg = DracErrorConstants.formatErrorCode(locale, e);
				request.setAttribute(DracConstants.ERROR_MESSAGE, msg);
			}
			return mapping.findForward(DracConstants.RH_ERROR_JSP_PAGE);
		}
		catch (Exception e) {
			Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
			log.error("Unexpected exception caught", e);

			if (displayException) {
				request.setAttribute(DracConstants.DRAC_ERROR, e);
				request.setAttribute(DracConstants.CAUSE_EXCEPTION, e.getCause());
			}

			if (e.getCause() instanceof DracException) {
				DracException ex = (DracException) e.getCause();
				String msg = DracErrorConstants.formatErrorCode(locale, ex);
				request.setAttribute(DracConstants.ERROR_MESSAGE, msg);
				request.setAttribute(DracConstants.ERROR_CODE, ex.getKey()
				    .getKeyAsErrorCode());
			}
			else if (e.getCause() instanceof DracRemoteException) {
				DracRemoteException ex = (DracRemoteException) e.getCause();
				String msg = DracErrorConstants.formatErrorCode(locale, ex);
				request.setAttribute(DracConstants.ERROR_MESSAGE, msg);
				request.setAttribute(DracConstants.ERROR_CODE, ex.getKey()
				    .getKeyAsErrorCode());
			}
			else if (e.getCause() != null) {
				request.setAttribute(DracConstants.ERROR_MESSAGE, e.getCause()
				    .getMessage());
			}
			else {
				String code = String.valueOf(DracErrorConstants.GENERAL_ERROR_INTERNAL);
				String msg = DracErrorConstants.formatErrorCode(locale, code, null);
				request.setAttribute(DracConstants.ERROR_MESSAGE, msg);
			}
			return mapping.findForward(DracConstants.RH_ERROR_JSP_PAGE);
		}
	}

	protected ActionForward executeAction(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		return null;
	}

	protected String getTZString(TimeZone tz, Locale locale) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
		    DracConstants.WEB_GUI_TIMEZONE, locale);
		dateFormatter.setTimeZone(tz);
		Date d = new Date(System.currentTimeMillis());
		return dateFormatter.format(d);
	}

	protected String[] stringToStringArray(String data) {
		if (data != null) {
			String array[] = data.split(",");
			if (array != null) {
				for (int i = 0; i < array.length; i++) {
					array[i] = array[i].trim();
				}
			}
			if (array.length == 1) {
				if (array[0].equals("N/A")) {
					return null;
				}
			}
			return array;
		}
		return null;
	}

	private ActionForward executeBefore(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		// Please see GeneralMappingDispatchAction::doLogin for possible session
		// attribute settings
		// made on successful login (which may then be checked here on incoming
		// requests).

		String pageRef = (String) request.getAttribute(DracConstants.PAGE_REF);
		HttpSession session = request.getSession(false);
		if (session == null) {
			// HttpSession has its own timeout period, default is 30 minutes
			ActionMessages messages = new ActionMessages();
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
			    DracConstants.ERROR_MSG_LOGIN_BROWSER_EXPIRED));
			saveMessages(session, messages);
			return new ActionRedirect(mapping.findForward(DracConstants.LOGIN));
		}
		session.setMaxInactiveInterval(-1);

		if (pageRef == null) {
			/* We do not know the page so this will force a login. */
			pageRef = DracConstants.EMPTY_STRING;
		}

		/* Get the calling URL of this page for later referals. */
		String myURL = request.getRequestURI();

		if (myURL == null) {
			/* Assign a default just in case we cannot resolve our URI. */
			myURL = DracConstants.PAGE_REF_WELCOME;
		}
		request.setAttribute(DracConstants.MYURL, myURL);

		String queryString = request.getQueryString();

		if (mapping.findForward(DracConstants.CHECK_BROWSER).getPath()
		    .equals(myURL)) {
			// Nothing more here; want to map through to the action
			return null;
		}

		// following booleans are kludgey way of determining when session validation
		// is not needed. server-side doesn't know which page we are on, so it is
		// up to client code up here to figure out
		boolean callingLogout = false;
		if (DracConstants.LOGOUT_PATH.equals(myURL)
		    || DracConstants.PERFORM_LOGOUT_PATH.equals(myURL)
		    || pageRef.equals(DracConstants.PAGE_REF_LOGOUT)) {
			callingLogout = true;
		}

		boolean callingLogin = false;
		if (pageRef.equals(DracConstants.PAGE_REF_LOGIN)
		    || myURL.equals(DracConstants.LOGIN_PATH)
		    || myURL.equals(DracConstants.PASSWORD_PATH)
		    || myURL.equals(DracConstants.PERFORM_LOGIN_PATH)
		    || myURL.equals(DracConstants.CHANGE_PASSWORD_PATH)) {
			callingLogin = true;
		}

		boolean callingChangePassword = false;
		if (myURL.equals(DracConstants.CHANGE_PASSWORD_PATH)
		    || myURL.equals(DracConstants.CHANGE_PASSWORD_ACTION)) {
			callingChangePassword = true;
		}

		boolean callingLang = false;
		if (pageRef.equals(DracConstants.PAGE_REF_LANG)) {
			callingLang = true;
		}

		/*****************************************************************************************************
		 * Check to make sure the locale is set otherwise we will not have language
		 * specific bundle resources loaded.
		 ****************************************************************************************************/
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);

		session.setAttribute(Globals.LOCALE_KEY, locale);

		/* If the locale is not set then we need to set it. */
		if (locale == null && !pageRef.equals(DracConstants.PAGE_REF_LANG)) {
			log.debug("DracBaseMappingDispatchAction::executeBefore - Locale not set; creating default.");
			session.setAttribute(DracConstants.MYLOCALE, Locale.US);

			ActionRedirect checkForCookiesRedirect = new ActionRedirect(
			    mapping.findForward(DracConstants.CHECK_BROWSER));
			checkForCookiesRedirect.addParameter(DracConstants.CALLING_URL, myURL);

			String testCookieValue = Long.toString(System.currentTimeMillis());

			// Set test value in cookie
			Cookie cookie = new Cookie(DracConstants.CHECK_BROWSER, testCookieValue);
			// A cookie is id'ed by name...and path! Use "/" to make the cookie valid
			// for the entire site:
			cookie.setPath("/");
			response.addCookie(cookie);

			// Set test value in parameter
			checkForCookiesRedirect.addParameter(DracConstants.CHECK_BROWSER,
			    testCookieValue);

			return checkForCookiesRedirect;
		}

		/*****************************************************************************************************
		 * Now we need to verify the user has logged in and posesses a valid ticket.
		 * We permit access to page contents without validation if this is the login
		 * page, logout page, or the language page.
		 ****************************************************************************************************/
		String userID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		{
			if (userID == null && token != null) {
				userID = token.getUser();
			}
		}
		if (!callingLang && !callingLogin && !callingLogout
		    && !callingChangePassword) {
			// Security (Crystal Box):
			// ================================================================
			// Low Risk #5 - IP Hopping
			String reqIpAddress = request.getRemoteAddr();
			String sessionIpAddress = (String) session
			    .getAttribute(DracConstants.SessionIpAddress);
			boolean sessionIpRequestIpMatchFlag = false;

			if (userID != null && token != null) {
				// We're in a valid authenticated session, so the IPs should match
				sessionIpRequestIpMatchFlag = reqIpAddress.equals(sessionIpAddress);

				if (!sessionIpRequestIpMatchFlag) {
					if (allowIpHopping) {
						log.debug("Detected IP session hop, but system is configured to allow.");
						sessionIpRequestIpMatchFlag = true;
					}
					else {
						log.debug("Detected IP session hop; session will be invalidated and the request will be forward to login.");

						// The session (on which the IP change has been detected) will be
						// junked.
						session.invalidate();
					}
				}
			}

			if (userID == null || token == null || !sessionIpRequestIpMatchFlag) {
				log.debug("forwarding to login: url=" + myURL + ";");
				ActionRedirect loginRedirect = new ActionRedirect(
				    mapping.findForward(DracConstants.LOGIN));
				if (queryString != null && !queryString.equals("")) {
					myURL += "?" + queryString;
				}
				loginRedirect.addParameter(DracConstants.CALLING_URL, myURL);
				return loginRedirect;
			}

			RequestHandler rh = RequestHandler.INSTANCE;
			try {
				rh.sessionValidate(token);
				UserDetails authObj = rh.getUserDetails(token);
				// All is good.
				session.setAttribute(DracConstants.AUTH_OBJ, authObj);
				session.setAttribute(DracConstants.TOKEN_OBJ, token);
				// session is okay, continue on...
				// check for unread messages
				return null;
			}
			catch (RemoteException re) {
				log.error("session not valid", re);
				ActionRedirect redirect = null;

				if (re.getCause() instanceof NrbException) {

					NrbException n = (NrbException) re.getCause();

					if (n.getArgs() != null && n.getArgs().length > 0) {
						if (n.getArgs()[0] instanceof SessionErrorCode) {
							SessionErrorCode ec = (SessionErrorCode) n.getArgs()[0];

							ActionMessage message = null;
							ActionMessages messages = new ActionMessages();

							switch (ec) {
							case ERROR_SESSION_EXPIRED:

								message = new ActionMessage(
								    DracConstants.ERROR_MSG_LOGIN_SESSION_EXPIRED);

								redirect = new ActionRedirect(
								    mapping.findForward(DracConstants.PASSWORD));
								redirect.addParameter(DracConstants.USER_ID, userID);
								// }
								break;
							/*
							 * case ERROR_INVALID_USERID_OR_PASSWORD_AND_RETRY: message = new
							 * ActionMessage
							 * (DracConstants.ERROR_MSG_LOGIN_INVALID_PASSWORD_RETRY,
							 * getMaxInvalidLoginAttempts(profile)); redirect = new
							 * ActionRedirect(mapping.findForward(DracConstants.PASSWORD));
							 * break; case
							 * ERROR_INVALID_USERID_OR_PASSWORD_AND_TO_BE_LOCKEDOUT: message =
							 * new ActionMessage(DracConstants.
							 * ERROR_MSG_LOGIN_INVALID_PASSWORD_LOCKED_OUT,
							 * getLockoutPeriod(profile)); redirect = new
							 * ActionRedirect(mapping.findForward(DracConstants.LOGIN));
							 * break;
							 */
							case ERROR_PASSWORD_EXPIRED:
								// redirect to the change password page
								session.setAttribute(DracConstants.USER_ID, userID);
								message = new ActionMessage(DracConstants.ERROR_PW_EXPIRED);
								redirect = new ActionRedirect(
								    mapping.findForward(DracConstants.FORWARD_CHANGE_PASSWORD));
								break;
							case ERROR_DORMANT:
								message = new ActionMessage(
								    DracConstants.ERROR_MSG_LOGIN_DORMANT, "unknown");
								redirect = new ActionRedirect(
								    mapping.findForward(DracConstants.LOGIN));
								break;
							case ERROR_LOCKOUT:
								message = new ActionMessage(
								    DracConstants.ERROR_MSG_LOGIN_LOCKED_OUT_SEC, "unknown");
								redirect = new ActionRedirect(
								    mapping.findForward(DracConstants.LOGIN));
								break;
							case ERROR_ACCOUNT_DISABLED:
								message = new ActionMessage(
								    DracConstants.ERROR_MSG_LOGIN_DISABLED);
								redirect = new ActionRedirect(
								    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
								break;
							case ERROR_NO_POLICY:
								message = new ActionMessage(
								    DracConstants.ERROR_MSG_LOGIN_NO_POLICY);
								redirect = new ActionRedirect(
								    mapping.findForward(DracConstants.LOGIN));
								break;
							case ERROR_INVALID_AUTHENTICATION_TYPE:
								// ???
								break;
							case ERROR_ASELECT_COMMUNICATION_ERROR:
								session.setAttribute(DracConstants.AUTH_OBJ, null);
								session.setAttribute(DracConstants.TOKEN_OBJ, null);
								message = new ActionMessage(
								    DracConstants.ERROR_ASELECT_COMMS_LOGIN);
								redirect = new ActionRedirect(
								    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
								break;
							default:
								redirect = new ActionRedirect(
								    mapping.findForward(DracConstants.LOGIN));
							}
							session.setAttribute(DracConstants.USER_ID, null);
							if (message != null) {
								messages.add(ActionMessages.GLOBAL_MESSAGE, message);
								saveMessages(session, messages);
							}
							log.debug("invalid session:sessionID=" + sessionID + ";");

							if (queryString != null && !queryString.equals("")) {
								myURL += "?" + queryString;
							}
							redirect.addParameter(DracConstants.CALLING_URL, myURL);
							redirect.addParameter(DracConstants.USER_ID, userID);
							return redirect;
						}
					}
				}

				// Any other exception type
				redirect = new ActionRedirect(mapping.findForward(DracConstants.LOGIN));
				return redirect;
			}
			catch (Exception e) {
				log.error("Error: ", e);
				return new ActionRedirect(mapping.findForward(DracConstants.LOGIN));
			}

		}
		return null;
	}

}
