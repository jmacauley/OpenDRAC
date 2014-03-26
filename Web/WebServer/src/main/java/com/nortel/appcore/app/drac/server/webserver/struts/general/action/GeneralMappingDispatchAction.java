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

package com.nortel.appcore.app.drac.server.webserver.struts.general.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
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

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.info.ServerInfo;
import com.nortel.appcore.app.drac.common.info.ServerInfo.ServerState;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionWarningCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationAuditData;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;
import com.nortel.appcore.app.drac.common.types.SysMetricType;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RemoteConnectionProxyException;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.general.form.LoginForm;
import com.nortel.appcore.app.drac.server.webserver.struts.general.form.ServerInfoForm;
import com.nortel.appcore.app.drac.server.webserver.struts.general.form.ServerSettingsForm;

/**
 * Created on 21-Aug-06
 */
public final class GeneralMappingDispatchAction extends
    DracBaseMappingDispatchAction {
  
  private static final Logger log = LoggerFactory.getLogger(GeneralMappingDispatchAction.class);
	// no instance variables allowed, struts not thread-safe

	public static Cookie getCookieByName(HttpServletRequest request, String name) {
		if (request != null && name != null) {
			Cookie[] allCookies = request.getCookies();
			if (allCookies != null) {
				for (Cookie candidate : allCookies) {
					if (name.equals(candidate.getName())) {
						return candidate;
					}
				}
			}
		}

		return null;
	}

	public static String getRandomToken() throws Exception {
		SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
		String randomNum = new Integer(prng.nextInt()).toString();

		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		byte[] result = sha.digest(randomNum.getBytes());

		StringBuilder sb = new StringBuilder();
		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
		    'b', 'c', 'd', 'e', 'f' };
		for (int idx = 0; idx < result.length; ++idx) {
			byte b = result[idx];
			sb.append(digits[(b & 0xf0) >> 4]);
			sb.append(digits[b & 0x0f]);
		}
		return sb.toString();
	}

	public static void login(HttpServletRequest request, String userID,
	    String password) throws Exception {
		HttpSession session = request.getSession();
		String sessionID = session.getId();

		RequestHandler rh = RequestHandler.INSTANCE;

		LoginToken token = rh.login(
		    ClientLoginType.WEB_CLIENT_LOGIN,
		    userID,
		    password.toCharArray(),
		    new IPAddress(request.getRemoteAddr(), String.valueOf(request
		        .getRemotePort())).toString(), null, request.getSession().getId());
		UserDetails userDetails = rh.getUserDetails(token);

		// Security (Crystal Box):
		// ================================================================
		// Medium Risk 3
		// Assign a new value to the session cookie after a successful login
		//
		// Note - this will result in the loss of any/all attributes housed in the
		// current session, and for DRAC, this includes the properties (message
		// strings)
		// read from DRAC.properties, based on the language/locale ... also set in
		// the
		// session. All of this will be loaded into this new session however via
		// a language.do redirection in DracBaseMappingDispatchAction::executeBefore
		//
		// Also note: Because a bounce of the session now results in a fresh visit
		// to language.jsp after login, it was necessary to relax the CSRF check in
		// language.jsp.
		// The file had included 'verifyCSRFToken.jsp', as a means of preventing a
		// a forged request to change the language. The session has a token, but the
		// the request does not. A forged request to change language would be
		// annoying,
		// but not serious.

		session.invalidate();
		session = request.getSession(true);

		// Set the new session id in our authentication object
		sessionID = session.getId();
		userDetails.setSessionID(sessionID);

		// Security (Crystal Box):
		// ================================================================
		// Low Risk #5 - IP Hopping
		//
		// Risk:
		// ----
		// Because sessions are not bound to a single IP address, a means of
		// protection against the
		// exploitation of certain vulnerabilities, are not used.
		//
		// Recommendation:
		// --------------
		// Invalidate a session cookie when it is used from a different IP address
		// than the one to which
		// it is issued.
		//
		// Note: see
		// http://www.owasp.org/index.php?title=Session_Management&setlang=es#Associating_Session_Information_With_IP_Address
		//
		// See DracBaseMappingDispatchAction::executeBefore
		//
		String reqIpAddress = request.getRemoteAddr();
		session.setAttribute(DracConstants.SessionIpAddress, reqIpAddress);

		session.setAttribute(DracConstants.AUTH_OBJ, userDetails);
		session.setAttribute(DracConstants.TOKEN_OBJ, token);

		// Security (Crystal Box):
		// ================================================================
		// High Risk 4
		// Cross-site request forgery (CSRF):
		// Strategy is to use 'synchronizer token' pattern for operations affecting
		// sensitive data to
		// prevent cross-site request forgery (XSRF/CSRF).
		// Note: DRAC supports both GET and POST methods for requests.
		// See
		// http://www.owasp.org/index.php/Cross-Site_Request_Forgery_%28CSRF%29_Prevention_Cheat_Sheet

		setCSRFToken(session);

		// Security (Crystal Box):
		// ================================================================
		// Low Risk 2
		// Set the cookie to httpOnly.
		// The httpOnly browser flag is now enabled in Tomcat conf/context.xml:
		//
		// <Context useHttpOnly="true">
		// ...
		// </Context>

		session.setAttribute(DracConstants.USER_ID, userID);
	}

	public static void setCSRFToken(HttpSession session) throws Exception {
		session.setAttribute(DracConstants.CSRFToken, getRandomToken());
	}

	public static void verifyCSRFToken(HttpSession session,
	    HttpServletRequest request) throws Exception {
		// Security (Crystal Box):
		// ================================================================
		// See note in GeneralMappingDispatchAction. There, a synch token was
		// placed into the session parameters. That token should also be supplied
		// 'back' on posted operations from any jsp page accessing said operations
		// (this is done by supplying the token via a hidden jsp field). The token
		// supplied via posted data is compared to the session value to guard
		// against
		// CSRF. A forgery would be characterized by a request missing the token.

		String csrfTokenFromSession = (String) session
		    .getAttribute(DracConstants.CSRFToken);
		String csrfTokenFromData = request.getParameter(DracConstants.CSRFToken);
		log.debug("csrfTokenFromSession = " + csrfTokenFromSession
		    + " csrfTokenFromData = " + csrfTokenFromData);
		if (csrfTokenFromData == null
		    || !csrfTokenFromData.equals(csrfTokenFromSession)) {
			log.debug("Cross-site request forgery detected." + " userId = "
			    + (String) session.getAttribute(DracConstants.USER_ID));
			Exception csrfException = new Exception(
			    "Cross-site request forgery detected.");
			throw new DracException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
			    new Object[] { csrfException.getMessage() }, csrfException);
		}
	}

	public ActionForward checkBrowser(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		String testCookieValue = request.getParameter(DracConstants.CHECK_BROWSER);

		Cookie[] cookies = request.getCookies();
		if (cookies != null && testCookieValue != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(DracConstants.CHECK_BROWSER)
				    && c.getValue().equals(testCookieValue)) {
					// The client browser was able to save its most recently issued test
					// value as a cookie.
					return mapping.findForward(DracConstants.LANGUAGE);
				}
			}
		}

		// throw new Exception("Browser must accept cookies");
		return mapping.findForward(DracConstants.BROWSER_CONFIGURATION);
	}

	public ActionForward doLogin(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();

		/* Get authentication parameters passed from login.jsp. */
		String userID = request.getParameter(DracConstants.USER_ID);
		if (userID == null || userID.equals("")) {
			userID = ((LoginForm) form).getUserID();
		}

		String password = request.getParameter("password");
		String forwardURL = request.getParameter("redirect");

		if (forwardURL == null || forwardURL.equals("")
		    || "null".equals(forwardURL)) {
			forwardURL = DracConstants.PAGE_REF_WELCOME;
		}

		// Security (Crystal Box):
		// ================================================================
		// High Risk 1
		// page redirection is employed upon login, and on any request to drac
		// which must first run through login authentication. The desired URI
		// is specified via the 'callingURL' request parameter, which is then
		// passed to the HttpServletResponse.sendRedirect.
		// To avoid security exposures in this case, it is enough to simply check
		// that the redirect URI will only attempt to access local resources ...
		// this
		// being done by simply ensuring that the redirect URI begins with a slash.
		// Attempts to redirect to external URLs will not work as a consequence.
		// Also, attempts to redirect to a URI beginning with 'javascript' will also
		// not function (if proceeded with a slash)...and without a preceeding
		// slash,
		// modern browser version do not permit http redirects to URLs beginning
		// with javascript.

		if (!forwardURL.startsWith("/")) {
			log.debug("URI redirect tampering detected." + " userId = "
			    + (String) session.getAttribute(DracConstants.USER_ID));
			Exception redirectException = new Exception(
			    "URI redirect tampering detected.");
			throw new DracException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
			    new Object[] { redirectException.getMessage() }, redirectException);
		}

		log.debug("doLogin: userID=" + userID + ":sessionID=" + session.getId()
		    + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":" + "operation=authenticate user;");

		if (userID == null || userID.equals("")) {
			// no user ID? will never be able to login
			log.error("User ID is null or blank!");
			session.invalidate();
			ActionRedirect redirect = new ActionRedirect(
			    mapping.findForward(DracConstants.WELCOME_JSP_PAGE));
			return redirect;
		}

		if (password == null) {
			// user hasn't provided the password yet, forward to the password page
			request.setAttribute(DracConstants.USER_ID, userID);
			request.setAttribute(DracConstants.CALLING_URL, forwardURL);
			return mapping.findForward(DracConstants.LOGIN_JSP_PAGE);
		}

		try {
			login(request, userID, password);
		}

		catch (RemoteException re) {
			if (re.getCause() instanceof NrbException) {

				NrbException n = (NrbException) re.getCause();

				if (n.getArgs() != null && n.getArgs().length > 0) {
					if (n.getArgs()[0] instanceof SessionErrorCode) {
						SessionErrorCode ec = (SessionErrorCode) n.getArgs()[0];

						session.setAttribute(DracConstants.SESSION_ERROR_CODE, ec);
						ActionMessages messages = new ActionMessages();
						ActionMessage message = null;
						ActionRedirect redirect = null;

						// redirect to password or login page depending on the type of
						// failure

						/**
						 * We have failed to login to the server, so we cannot go back to
						 * the server and ask it details such as loginAttempts remaining,
						 * and other things !!!!
						 */
						switch (ec) {
						case ERROR_SESSION_EXPIRED:
							message = new ActionMessage(
							    DracConstants.ERROR_MSG_LOGIN_LOCKED_CLIENT_SESSION);
							redirect = new ActionRedirect(
							    mapping.findForward(DracConstants.PASSWORD_JSP_PAGE));
							break;
						case ERROR_INVALID_USERID_OR_PASSWORD_AND_RETRY:
						case ERROR_INVALID_USERID_OR_PASSWORD_AND_TO_BE_LOCKEDOUT:
							message = new ActionMessage(
							    DracConstants.ERROR_MSG_LOGIN_INVALID_PASSWORD);
							redirect = new ActionRedirect(
							    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
							break;
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
							    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
							break;
						case ERROR_LOCKOUT:						
							message = new ActionMessage(
							    DracConstants.ERROR_MSG_LOGIN_LOCKED_OUT_SEC, "unknown");
							redirect = new ActionRedirect(
							    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
							break;
						case ERROR_LOCKED_CLIENT_IP_ADDRESSS:
							message = new ActionMessage(
							    DracConstants.ERROR_MSG_LOGIN_LOCKED_CLIENT_IPADDRESS);
							redirect = new ActionRedirect(
							    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
							break;
						case ERROR_LOCKED_CLIENT_SESSION:
							message = new ActionMessage(
							    DracConstants.ERROR_MSG_LOGIN_LOCKED_CLIENT_SESSION);
							redirect = new ActionRedirect(
							    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
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
							    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
							break;
						case ERROR_INVALID_AUTHENTICATION_TYPE:
							// ???
							redirect = new ActionRedirect(
							    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
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
							    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
						}

						if (message != null) {
							messages.add(ActionMessages.GLOBAL_MESSAGE, message);
							saveMessages(session, messages);
						}

						redirect.addParameter(DracConstants.USER_ID, userID);
						redirect.addParameter(DracConstants.CALLING_URL, forwardURL);
						return redirect;
					}
				}
			}
		}
		catch (RemoteConnectionProxyException rpce) {
			// This exception is thrown by RemoteConnectionProxy when trying to get
			// the NRB_PORT reference

			ActionMessages messages = new ActionMessages();
			ActionMessage message = null;
			ActionRedirect redirect = new ActionRedirect(
			    mapping.findForward(DracConstants.LOGIN_JSP_PAGE));
			;
			if (rpce.getErrorCode() == DracErrorConstants.RH_ERROR_6002_REDUNDANCY_SWITCHOVER_IN_PROGRESS) {
				// a switchover is in progress, display the generic maintenance message

				message = new ActionMessage(
				    DracConstants.ERROR_MSG_LOGIN_GENERAL_MAINTENANCE);
			}
			else {
				// something more severe happened and requires analysis
				log.error("doLogin, caught RemoteProxyConnectionException", rpce);
				message = new ActionMessage(DracConstants.ERROR_MSG_LOGIN_PROCESSING);
			}

			if (message != null) {
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(session, messages);
			}

			redirect.addParameter(DracConstants.USER_ID, userID);
			redirect.addParameter(DracConstants.CALLING_URL, forwardURL);
			return redirect;
		}

		catch (Exception e) {
			log.error("doLogin, exception caught", e);

			// If it's not a remote exception, throw it back to the caller
			// (RequestHandler)
			// for the error to be shown.
			throw e;
		}

		response.sendRedirect(forwardURL);
		return null;

	}

	public ActionForward doLogout(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		/* Get who is logging out. */
		String activeUserID = request.getParameter("userID");
		String sessionID = session.getId();
		String errorDetails = null;

		/* Create base logging string for this page. */
		String logEntry = "performLogout: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=user session logout;");

		/* Where are we sending the user after the logout? */
		String forwardURL = request.getParameter("redirect");

		if (forwardURL == null) {
			forwardURL = DracConstants.LOGIN_PATH;
		}

		/* What action was requested? */
		String yes = request.getParameter("Yes");
		String no = request.getParameter("No");

		/* Determine if the user canceled or accepted logout. */
		if (yes != null && no == null) {
			/* Check to see if the user login is still valid. */
			RequestHandler rh = RequestHandler.INSTANCE;

			/* Try to logout the user. */
			try {

				LoginToken token = (LoginToken) session
				    .getAttribute(DracConstants.TOKEN_OBJ);
				if (token != null) {
					rh.logout(token);
				}
				else {
					log.error("token was null trying to logout!");
				}
			}
			catch (Exception e) {
				/* Something caused us to fail so we are in a bit of a pickle. */
				errorDetails = "performLogout Error - logout() failed: "
				    + e.getMessage();
				log.error(logEntry + "error=" + errorDetails + ";");
				throw e;
			}
			finally {
				session.setAttribute(DracConstants.AUTH_OBJ, null);
				session.setAttribute(DracConstants.TOKEN_OBJ, null);
			}

			/* Clear all the session parameters. */
			session.invalidate();

			/* Fall through to the forward page. */
			ActionRedirect langRedirect = new ActionRedirect(
			    mapping.findForward(DracConstants.LOGIN));
			return langRedirect;
		}

		/* Assume everything else is a cancel logout. */
		try {
			response.sendRedirect(forwardURL);
		}
		catch (IOException e) {
			errorDetails = "performLogout Error - cancel logout failed: "
			    + e.getMessage();
			log.error(logEntry + "error=" + errorDetails + ";");
		}
		return null;

	}

	public ActionForward editSettings(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "edit server settings: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=edit server settings;");
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		ServerSettingsForm sForm = (ServerSettingsForm) form;
		RequestHandler rh = RequestHandler.INSTANCE;

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in serverSettings.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (sForm.getConfirmationTimeout() != sForm.getOldConfirmationTimeout()) {
			rh.setConfirmationTimeout(token, sForm.getConfirmationTimeout());
		}

		if (sForm.getScheduleOffset() != sForm.getOldScheduleOffset()) {
			rh.setScheduleOffset(token, sForm.getScheduleOffset());
		}

		// TODO: Set the confirmation timeout value from DB
		request.setAttribute("success", "true");
		return mapping.findForward(DracConstants.SETTINGS_JSP_PAGE);
	}

	public ActionForward forwardAboutPage(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response) {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "about: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		// The servlet reads from the installation directory
		File f = new File("release.ext");
		if (f.canRead()) {
			Map<String, String> map = new HashMap<String, String>();
			BufferedReader br = null;
			String input;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				while ((input = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(input, "=");
					if (st.countTokens() == 2) {
						map.put(st.nextToken(), st.nextToken());
					}
				}

				request.setAttribute("releaseInfo", map);
			}
			catch (IOException io) {
				log.error("Error: ", io);
			}
			finally {
				if (br != null) {
					try {
						br.close();
					}
					catch (IOException io) {
						log.error("Error closing file " + f, io);
					}
				}
			}
		}
		else {
			log.warn("Unable to read file release.ext");
		}

		/* Entry into page. */
		log.debug(logEntry + "operation=about;");
		return mapping.findForward(DracConstants.ABOUT_JSP_PAGE);
	}

	public ActionForward forwardBrowserPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "browser: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=browser support;");

		return mapping.findForward(DracConstants.BROWSER_JSP_PAGE);
	}

	public ActionForward forwardDownloadPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "download: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=display download;");

		return mapping.findForward(DracConstants.DOWNLOAD_JSP_PAGE);
	}

	// Forwards for help menu
	public ActionForward forwardHelpPage(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response) {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "help: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=help contents;");

		return mapping.findForward(DracConstants.HELP_JSP_PAGE);
	}

	public ActionForward forwardLoginPage(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response) {
		// clear the user details from the session
		request.getSession().setAttribute(DracConstants.AUTH_OBJ, null);
		request.getSession().setAttribute(DracConstants.TOKEN_OBJ, null);
		return mapping.findForward(DracConstants.LOGIN_JSP_PAGE);
	}

	public ActionForward forwardLogoutPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		return mapping.findForward(DracConstants.LOGOUT_JSP_PAGE);

	}

	public ActionForward forwardNotImplementedPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String logEntry = "notImplemented: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=not implemented");
		return mapping.findForward(DracConstants.NOT_IMPLEMENTED_ACTION);
	}

	public ActionForward forwardPasswordPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		return mapping.findForward(DracConstants.PASSWORD_JSP_PAGE);
	}

	public ActionForward forwardReleasePage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "release: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=help contents;");

		return mapping.findForward(DracConstants.RELEASE_JSP_PAGE);
	}

	public ActionForward forwardSettingsPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "server settings: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=server settings;");
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		ServerSettingsForm sForm = (ServerSettingsForm) form;
		sForm.setConfirmationTimeout(RequestHandler.INSTANCE
		    .getConfirmationTimeout(token));
		sForm.setOldConfirmationTimeout(sForm.getConfirmationTimeout());
		SysMetricType metric = RequestHandler.INSTANCE.getSystemMetric(token);

		if (metric != null) {
			sForm.setScheduleOffset(metric.getConfigureOffset());
			sForm.setOldScheduleOffset(sForm.getScheduleOffset());
		}
		return mapping.findForward(DracConstants.SETTINGS_JSP_PAGE);
	}

	public ActionForward forwardWelcomePage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "login: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=display welcome;");

		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		@SuppressWarnings("unused")
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(userDetails);
		if (userDetails != null) {
			AuthenticationData authData = userDetails.getUserPolicyProfile()
			    .getUserProfile().getAuthenticationData();
			if (authData != null) {
				AuthenticationAuditData auditData = authData.getAuditData();
				if (auditData != null) {
					LoginForm loginForm = (LoginForm) form;

					// print out the last IP address
					List<IPAddress> listOfIp = auditData.getLastLoginAddressList();
					if (listOfIp != null && !listOfIp.isEmpty()) {
						IPAddress lastAddress = listOfIp.get(listOfIp.size() - 1);
						if (lastAddress != null) {
							loginForm.setLastLoginAddress(lastAddress.toString());
						}
					}
					int numInvalid = auditData.getNumberOfInvalidAttempts();
					List<IPAddress> ipAddrs = auditData.getLocationOfInvalidAttempts();
					loginForm.setNumInvalidLogin(numInvalid);

					int listSize = numInvalid;
					if (listSize > ipAddrs.size()) {
						listSize = ipAddrs.size();
					}

					// the list of IP addresses is LIFO order, and is never cleared,
					// so always print from the tail-end, up to the number of invalid
					// attempts indicated
					if (ipAddrs != null && !ipAddrs.isEmpty() && numInvalid > 0) {
						List<String> ipAddressList = new ArrayList<String>(listSize);
						for (int i = ipAddrs.size() - 1; i >= ipAddrs.size() - listSize; i--) {
							ipAddressList.add(ipAddrs.get(i).toString());
						}
						loginForm.setLocationOfInvalidAttempts(ipAddressList);
					}
					else {
						loginForm.setLocationOfInvalidAttempts(new ArrayList<String>(0));
					}

					loginForm.setTimeZone(DateFormatter.getTimeZoneDisplayName(tz));

					SimpleDateFormat dateFormatter = new SimpleDateFormat(
					    DracConstants.WEB_GUI_TIME, locale);
					dateFormatter.setTimeZone(tz);
					loginForm.setLastLoginDate(dateFormatter.format(authData
					    .getLastAuthenticationStateChange().getTime()));
				}
			}

			List<SessionWarningCode> warnings = userDetails.getWarningCode();
			if (warnings != null && !warnings.isEmpty()) {
				for (SessionWarningCode code : warnings) {
					switch (code) {
					case PASSWORD_EXPIRING_NOTIFICATION:
						if (userDetails.getCredential() != null
						    && userDetails.getCredential().getLoginAuditData() != null) {
							int days = userDetails.getCredential().getLoginAuditData()
							    .getExpiringNotificationDays();
							request.setAttribute("passwordExpiry", days);
							break;
						}
					}
				}
			}
		}

		return mapping.findForward(DracConstants.WELCOME_JSP_PAGE);
	}

	public ActionForward getServerStatus(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/* Create base logging string for this page. */
		String logEntry = "serverStatus: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";
		/* Entry into page. */
		log.debug(logEntry + "operation=get server status;");

		/* Intantiate the RequestHandler object. */
		RequestHandler rh = RequestHandler.INSTANCE;
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		ServerInfoType serverInfo = null;

		/* A technical error message is assigned if there is a failure. */
		String errorDetails = null;
		try {
			serverInfo = rh.getServerInfo(token);
		}
		catch (Exception e) {
			errorDetails = "serverStatus Error - getServerInfo() failed: "
			    + e.getMessage();
			log.error(logEntry + "error=" + errorDetails + ";", e);

		}

		/* TODO: Need to provide better DRAC state management. */
		String version = "unknown";
		String status = "unknown";

		if (serverInfo != null) {
			// version = serverInfo.getControllerInfo().getVersion();
			version = rh.getVersion();
			status = serverInfo.getControllerInfo().getStatus();
		}

		request.setAttribute("version", version);
		request.setAttribute("status", status);

		ActionMessages messages = new ActionMessages();
		ActionMessage message = null;

		// redundancy information
		ServerInfoForm serverForm = (ServerInfoForm) form;
		log.debug(serverForm.getForceSwitchTo());
		try {
		  rh.forceRedundancySwitch();
		}
		catch (RequestHandlerException e) {
			message = new ActionMessage(
			    DracConstants.ERROR_REUDNDANCY_SWITCH_VERSIONS_MISMATCH,
			    e.getArgs()[1]);
			messages.add(ActionMessages.GLOBAL_MESSAGE, message);
		}
		// reset the force switch field
		serverForm.setForceSwitchTo("");

		ServerInfo[] info = rh.getRedundancyServerInfo();
		ServerInfo server1Info = info[0];
		ServerInfo server2Info = info[1];

		if (server1Info != null) { // server1 info
			serverForm.setServer1Ip(server1Info.getIpAddress());
			serverForm.setServer1Version(server1Info.getSoftwareVersion());
			serverForm.setServer1Config(server1Info.getServerConfig().name());
			serverForm.setServer1Mode(server1Info.getMode().name());
			serverForm.setServer1State(server1Info.getState().name());
		}
		else {
			message = new ActionMessage(DracConstants.ERROR_REDUNDANCY_SERVER1);
			messages.add(ActionMessages.GLOBAL_MESSAGE, message);
		}

		if (server2Info != null) { // server 2 info
			serverForm.setServer2Ip(server2Info.getIpAddress());
			serverForm.setServer2Version(server2Info.getSoftwareVersion());
			serverForm.setServer2Config(server2Info.getServerConfig().name());
			serverForm.setServer2Mode(server2Info.getMode().name());
			serverForm.setServer2State(server2Info.getState().name());
		}
		else if (server1Info != null
		    && server1Info.getServerConfig() == ServerInfo.ServerConfigType.REDUNDANT) {
			// error only if server1 is redundant config, must have a server2
			message = new ActionMessage(DracConstants.ERROR_REDUNDANCY_SERVER2);
			messages.add(ActionMessages.GLOBAL_MESSAGE, message);
		}

		if (server1Info != null && server2Info != null) { // each server's info
			if (server1Info.getMode() == ServerInfo.ServerRelationship.PRIMARY
			    && server2Info.getMode() == ServerInfo.ServerRelationship.PRIMARY) {
				message = new ActionMessage(DracConstants.ERROR_REDUNDANCY_BOTHPRIMARY);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
			}
			if (server1Info.getServerConfig() == ServerInfo.ServerConfigType.REDUNDANT
			    && server2Info.getServerConfig() != ServerInfo.ServerConfigType.REDUNDANT) {
				message = new ActionMessage(
				    DracConstants.ERROR_REDUNDANCY_SERVER2_NOT_REDUNDANT);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
			}

			if (!server1Info.getSoftwareVersion().equals(
			    server2Info.getSoftwareVersion())) {
				message = new ActionMessage(
				    DracConstants.ERROR_REDUNDANCY_SERVER_VERSIONS_MISMATCH);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
			}
		}

		if (!messages.isEmpty()) {
			saveMessages(request, messages);
		}
		
		if(server1Info.getIpAddress().equalsIgnoreCase(server2Info.getIpAddress())){
		  serverForm.setRedundancyMode("STANDALONE");
		}
		else if(server1Info.getState() == ServerState.ACTIVE){
		  serverForm.setRedundancyMode("SERVER1");
		}
		else if(server2Info.getState() == ServerState.ACTIVE){
      serverForm.setRedundancyMode("SERVER2");
    }

		return mapping.findForward(DracConstants.SERVER_STATUS_JSP_PAGE);
	}

}
