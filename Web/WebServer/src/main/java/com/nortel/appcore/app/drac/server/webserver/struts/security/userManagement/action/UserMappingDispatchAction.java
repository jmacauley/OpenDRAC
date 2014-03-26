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

package com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.authentication.types.LocalAccountCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.PasswordErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.PasswordEvaluator;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalAuthentication;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalAuthentication.SupportedAuthenticationType;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationType;
import com.nortel.appcore.app.drac.common.utility.StringParser;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracActionMessage;
import com.nortel.appcore.app.drac.server.webserver.struts.common.IndexedTreeNode;
import com.nortel.appcore.app.drac.server.webserver.struts.general.action.GeneralMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.UserHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.CreateUserForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.EditUserForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.ListUsersForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.UserForm;

/**
 * Created on 12-Jul-2006
 */
public final class UserMappingDispatchAction extends
    DracBaseMappingDispatchAction {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	// no instance variables allowed, struts not thread-safe

	public ActionForward createUserResult(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		// ****************************************************************************
		// * User must be valid so we are onto the main page logic.
		// ****************************************************************************

		// Create base logging string for this page.
		String logEntry = "createUserResult: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		// Entry into page.
		log.debug(logEntry + "operation=create user result");

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		TimeZone tz = DracHelper.getTimeZone(token);
		String uid = (String)request.getSession().getAttribute("uid");
		if (uid != null && !uid.equals("")) {
			RequestHandler rh = RequestHandler.INSTANCE;
			log.debug(logEntry + "query user profile: " + uid);
			UserProfile aUP = rh.getUserProfile(token, uid);
			if (aUP == null) {
				String error = (String) request.getAttribute("message");
				
				((UserForm) form).setError(error);
			}
			else {
				GlobalPolicy aGP = rh.getGlobalPolicy(token);
				UserHelper.populateUserForm(token, locale, tz, aUP, aGP,
				    (UserForm) form);
				// orig UserHelper.copyProperties(locale, tz, aUP, aGP, (UserForm)
				// form);
			}
		}
		else {
			String error = (String) request.getAttribute("message");
			log.debug("CreateUserResult " + error);
			((UserForm) form).setError(error);
		}

		return mapping.findForward(DracConstants.CREATE_USER_RESULT_PAGE);
	}

	public ActionForward deleteUser(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		// Create base logging string for this page.
		String logEntry = "deleteUser: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		// Entry into page.
		log.debug(logEntry + "operation=invoke");
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		String uid = request.getParameter("uid");

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in editUser.jsp
		// SPECIAL CASE HANDLING!
		// This is the first command encountered that isn't sent via a jsp form
		// posting.
		// The token had to be sent via request parameters in the url.
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (uid != null && !uid.equals("")) {
			ActionForward forward = mapping
			    .findForward(DracConstants.DELETE_USER_RESULT_PAGE);
			ActionMessages messages = new ActionMessages();

			try {
				RequestHandler.INSTANCE.deleteUser(token, uid);
				ActionMessage message = new ActionMessage(
				    DracConstants.DELETE_USER_PASS, uid);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
			}
			catch (RequestHandlerException e) {
				ActionMessage message = new ActionMessage(
				    DracConstants.DELETE_USER_FAIL, uid);
				ActionMessage message2 = new DracActionMessage(e)
				    .getActionMessage(locale);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message2);
				saveMessages(request, messages);
				request.setAttribute("error", "true");
			}
			return forward;
		}
		return new ActionRedirect(
		    mapping.findForward(DracConstants.LIST_USERS_PAGE));
	}

	/*
     * 
     */
	public ActionForward forwardListUsersPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "forwardListUsersPage: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol()
		    + ":operation=setting up get list of users;";

		/* Entry into page. */
		log.debug(logEntry);

		return mapping.findForward(DracConstants.LIST_USERS_PAGE);
	}

	public ActionForward handleCreateUser(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		String sessionID = session.getId();
		// ***************************************************************************
		// User must be valid so we are onto the main page logic.
		// ***************************************************************************

		// Create base logging string for this page.
		String logEntry = "handleCreateUser: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		// Entry into page.
		log.debug(logEntry + "operation=handle create user;");
		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		CreateUserForm userForm = (CreateUserForm) form;
		ActionMessages messages = new ActionMessages();
		RequestHandler rh = RequestHandler.INSTANCE;
		log.debug(logEntry + "userForm.getAuthType() is: " + userForm.getAuthType());

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in createUser.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (userForm.getAuthType().equals(AuthenticationType.INTERNAL.toString())) {
			String userPassword = userForm.getUserPassword();
			if (userPassword.equals(userForm.getConfirmUserPassword())) {
				// Password entered twice correctly.
				// Does it follow the password rules?
				String encodedPasswordRules = rh.getGlobalPolicy(token)
				    .getLocalAccountPolicy().getLocalPasswordPolicy().getPwRules();

				if (encodedPasswordRules != null) {
					Map<String, String> encodedPasswordRulesMap = new HashMap<String, String>();
					encodedPasswordRulesMap.put(PasswordEvaluator.PASSWD_PASSWORDRULES,
					    encodedPasswordRules);

					Map<String, String> decodedRulesMap = new HashMap<String, String>();
					PasswordEvaluator.decodePasswordRules(encodedPasswordRulesMap,
					    decodedRulesMap);

					PasswordErrorCode myPasswordErrorCode = PasswordEvaluator
					    .validatePassword(userDetails, userPassword, decodedRulesMap,
					        null);

					/*
					 * There was a problem with the password. Check to see what error was
					 * returned to set up appropriate error message in the response.
					 */
					switch (myPasswordErrorCode) {
					case NO_ERROR:
						// It worked, do nothing;
						break;
					case ERROR_GENERAL:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_GENERAL));
						break;
					case ERROR_PASSWD_ILLEGALCHARS:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_ILLEGALCHARS));
						break;
					case ERROR_PASSWD_MINLENGTH:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_MINLENGTH));
						break;
					case ERROR_PASSWD_MINALPHAVALUE:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_MINALPHAVALUE));
						break;
					case ERROR_PASSWD_MINDIGITVALUE:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_MINDIGITVALUE));
						break;
					case ERROR_PASSWD_MINSPECIALVALUE:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_MINSPECIALVALUE));
						break;
					case ERROR_PASSWD_MINDIFFERENT:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_MINDIFFERENT));
						break;
					case ERROR_PASSWD_MIXEDALPHA:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_MIXEDALPHA));
						break;
					case ERROR_PASSWD_INVALIDLIST:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_INVALIDLIST));
						break;
					case ERROR_PASSWD_MAXLENGTH:
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						    DracConstants.PW_ERROR_MAXLENGTH));
						break;

					// don't expect these to be returned, fall through to the default
					// clause!
					case ERROR_OLDPW_INVALID:
					case ERROR_PW_NOT_MATCH:
					case ERROR_PW_NOT_OLD:
					case ERROR_RULES_NOT_FOUND:
					default:
						log.error("Unexpected case in enum reached " + myPasswordErrorCode);
						break;
					}
				}
				else {
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					    DracConstants.ERROR_NO_ENCODED_PW_RULES));
				}
			}
			else {
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.ERROR_PW_NOT_MATCH));
			}
		}
		if (!messages.isEmpty()) {
			saveMessages(request, messages);
			return mapping.findForward(DracConstants.HANDLE_CREATE_USER_FAILED_PAGE);
		}
		/*
		 * Looks like we passed what checks were needed. Let's create a user.
		 */
		ActionRedirect redirect = new ActionRedirect(
		    mapping.findForward(DracConstants.HANDLE_CREATE_USER_SUCCESS_PAGE));
		if (!DracConstants.EMPTY_STRING.equals(userForm.getUserID())) {
			try {
				String userId = UserHelper.create(token, userForm);
				request.getSession().setAttribute("uid", userId);
			}
			catch (RequestHandlerException e) {
				ActionMessage message = new DracActionMessage(e)
				    .getActionMessage(locale);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
				return mapping
				    .findForward(DracConstants.HANDLE_CREATE_USER_FAILED_PAGE);
			}
			return redirect;
		}
		// lost request parameters through authenticate redirect
		return mapping.findForward(DracConstants.CREATE_USER_ACTION);
	}

	/*
     * 
     */
	public ActionForward handleEditUser(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		// Create base logging string for this page.
		String logEntry = "handleEditUser: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		// Entry into page.
		log.debug(logEntry + "operation=invoke");

		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		RequestHandler rh = RequestHandler.INSTANCE;

		EditUserForm editForm = (EditUserForm) form;
		ActionMessages messages = new ActionMessages();
		log.debug(logEntry + "editing user: " + editForm.getUserID());
		UserProfile aUP = rh.getUserProfile(token, editForm.getUserID());

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in editUser.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (aUP != null) {
			UserProfile.AuthenticationType authType = aUP.getAuthenticationData()
			    .getAuthenticationType();
			log.debug(logEntry + "authType is: " + authType);
			if (authType != null) {
				if (authType.equals(AuthenticationType.INTERNAL)) {
					String oldUserPassword = aUP.getAuthenticationData()
					    .getInternalAccountData().getUserPassword();
					String newUserPassword = editForm.getPassword();

					if (!newUserPassword.trim().equals("")) { // check not blank
						if (newUserPassword.equals(editForm.getPassword2())) {
							// Password entered twice correctly.
							// Has the password changed?
							if (!newUserPassword.equals(oldUserPassword)) {
								// Seems so - apply the password rules.
								String encodedPasswordRules = rh.getGlobalPolicy(token)
								    .getLocalAccountPolicy().getLocalPasswordPolicy()
								    .getPwRules();

								if (encodedPasswordRules != null) {
									Map encodedPasswordRulesMap = new HashMap();
									encodedPasswordRulesMap.put(
									    PasswordEvaluator.PASSWD_PASSWORDRULES,
									    encodedPasswordRules);

									Map<String, String> decodedRulesMap = new HashMap<String, String>();
									PasswordEvaluator.decodePasswordRules(
									    encodedPasswordRulesMap, decodedRulesMap);

									PasswordErrorCode myPasswordErrorCode = PasswordEvaluator
									    .validatePassword(userDetails, newUserPassword,
									        decodedRulesMap, oldUserPassword);

									switch (myPasswordErrorCode) {
									case NO_ERROR:
										/*
										 * The password has been changed for this internal account
										 * user and it passes all the rules. Update the password on
										 * the DB.
										 */
										try {
											log.debug(logEntry + "change password in DB.");
											rh.setupUserAuthenticationAccount(token, editForm
											    .getUserID(),
											    new LocalAccountCredential(editForm.getUserID(),
											        newUserPassword, userDetails.getLoginIPAddress()));
										}
										catch (RequestHandlerException rhe) {
											log.error("Error: ", rhe);
										}
										break;
									case ERROR_GENERAL:
										messages.add(ActionMessages.GLOBAL_MESSAGE,
										    new ActionMessage(DracConstants.PW_ERROR_GENERAL));
										break;
									case ERROR_PASSWD_ILLEGALCHARS:
										messages.add(ActionMessages.GLOBAL_MESSAGE,
										    new ActionMessage(DracConstants.PW_ERROR_ILLEGALCHARS));
										break;
									case ERROR_PASSWD_MINLENGTH:
										messages.add(ActionMessages.GLOBAL_MESSAGE,
										    new ActionMessage(DracConstants.PW_ERROR_MINLENGTH));
										break;
									case ERROR_PASSWD_MINALPHAVALUE:
										messages
										    .add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
										        DracConstants.PW_ERROR_MINALPHAVALUE));
										break;
									case ERROR_PASSWD_MINDIGITVALUE:
										messages
										    .add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
										        DracConstants.PW_ERROR_MINDIGITVALUE));
										break;
									case ERROR_PASSWD_MINSPECIALVALUE:
										messages.add(ActionMessages.GLOBAL_MESSAGE,
										    new ActionMessage(
										        DracConstants.PW_ERROR_MINSPECIALVALUE));
										break;
									case ERROR_PASSWD_MINDIFFERENT:
										messages.add(ActionMessages.GLOBAL_MESSAGE,
										    new ActionMessage(DracConstants.PW_ERROR_MINDIFFERENT));
										break;
									case ERROR_PASSWD_MIXEDALPHA:
										messages.add(ActionMessages.GLOBAL_MESSAGE,
										    new ActionMessage(DracConstants.PW_ERROR_MIXEDALPHA));
										break;
									case ERROR_PASSWD_INVALIDLIST:
										messages.add(ActionMessages.GLOBAL_MESSAGE,
										    new ActionMessage(DracConstants.PW_ERROR_INVALIDLIST));
										break;
									case ERROR_PASSWD_MAXLENGTH:
										messages.add(ActionMessages.GLOBAL_MESSAGE,
										    new ActionMessage(DracConstants.PW_ERROR_MAXLENGTH));
										break;

									// don't expect these to be returned, fall through to the
									// default
									// clause!
									case ERROR_OLDPW_INVALID:
									case ERROR_PW_NOT_MATCH:
									case ERROR_PW_NOT_OLD:
									case ERROR_RULES_NOT_FOUND:
									default:
										log.error("Unexpected case in enum reached "
										    + myPasswordErrorCode);
										break;
									}
								}
								else {
									messages
									    .add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
									        DracConstants.ERROR_NO_ENCODED_PW_RULES));
								}
							}
						}
						else {
							messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
							    DracConstants.ERROR_PW_NOT_MATCH));
						}
					}
				}
			}

			if (!messages.isEmpty()) {
				saveMessages(request, messages);
			}
			else {
				// No problems.
				log.debug(logEntry
				    + "apply edit form values (except passwords) to UserProfile.");

				UserHelper.updateUserProfile(token, userDetails, editForm, aUP);
				request.setAttribute("uid", editForm.getUserID());
				request.setAttribute("editSuccess", "true");
			}

			// reset the password fields to not show up any values
			editForm.setPassword("");
			editForm.setPassword2("");
			editForm.setwsdlCertificate("");

			return mapping.findForward(DracConstants.SUCCESS_PATH);
		}
		// may have timed out and lost the uid, redirect to the list page.
		return mapping.findForward(DracConstants.LIST_USERS_PAGE);
	}

	public ActionForward listUsers(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		ListUsersForm aListUsersForm = (ListUsersForm) form;

		// ***************************************************************************
		// User must be valid so we are onto the main page logic.
		// ***************************************************************************

		String searchBy = aListUsersForm.getSearchBy();
		String searchFor = aListUsersForm.getSearchFor();

		RequestHandler rh = RequestHandler.INSTANCE;
		List<String> userIDList = rh.getUserIDs(token);

		List<UserForm> treeResult = new ArrayList<UserForm>();
		UserForm aUserForm = null;
		UserProfile userProfile = null;
		String userProfileField = "";
		boolean matchesCriteria = false;

		for (String aUserID : userIDList) {
			matchesCriteria = false;

			aUserForm = new UserForm();

			try {
				userProfile = rh.getUserProfile(token, aUserID);
			}
			catch (Exception e) {
				continue;
			}

			if (userProfile != null) {
				if (userDetails.getUserPolicyProfile().getUserGroupType()
				    .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)
				    || userDetails.getUserPolicyProfile().getUserGroupType()
				        .equals(UserGroupProfileXML.UserGroupType.GROUP_ADMIN)) {
					request.setAttribute("editable", "true");
				}

				if (searchBy.equals("allUsers")) {
					matchesCriteria = true;
				}

				else if (searchBy.equals("unassignedUsers")) {
					MembershipData membershipData = userProfile.getMembershipData();
					if (membershipData.getMemberUserGroupName().size() == 0) {
						matchesCriteria = true;
					}
				}

				else if (searchBy.equals("userID")) {
					if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
						userProfileField = userProfile.getUserID();
						if (DracHelper.doWildCardWork(searchFor, userProfileField)) {
							matchesCriteria = true;
						}
					}
				}
				else if (searchBy.equals("surname")) {
					if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
						userProfileField = userProfile.getPersonalData().getName()
						    .getSurName();
						if (DracHelper.doWildCardWork(searchFor, userProfileField)) {
							matchesCriteria = true;
						}
					}
				}
				else if (searchBy.equals("givenName")) {
					if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
						userProfileField = userProfile.getPersonalData().getName()
						    .getGivenName();
						if (DracHelper.doWildCardWork(searchFor, userProfileField)) {
							matchesCriteria = true;
						}
					}
				}
				else if (searchBy.equals("commonName")) {
					if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
						userProfileField = userProfile.getPersonalData().getName()
						    .getCommonName();
						if (DracHelper.doWildCardWork(searchFor, userProfileField)) {
							matchesCriteria = true;
						}
					}
				}
				else if (searchBy.equals("phone")) {
					if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
						userProfileField = userProfile.getPersonalData().getAddress()
						    .getPhone();
						if (DracHelper.doWildCardWork(searchFor, userProfileField)) {
							matchesCriteria = true;
						}
					}
				}
				else if (searchBy.equals("eMail")) {
					if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
						userProfileField = userProfile.getPersonalData().getAddress()
						    .getMail();
						if (DracHelper.doWildCardWork(searchFor, userProfileField)) {
							matchesCriteria = true;
						}
					}
				}
				else if (searchBy.equals("postalAddress")) {
					if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
						userProfileField = userProfile.getPersonalData().getAddress()
						    .getPostalAddress();
						if (DracHelper.doWildCardWork(searchFor, userProfileField)) {
							matchesCriteria = true;
						}
					}
				}
				else if (searchBy.equals("description")) {
					if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
						userProfileField = userProfile.getPersonalData().getDescription();
						if (DracHelper.doWildCardWork(searchFor, userProfileField)) {
							matchesCriteria = true;
						}
					}
				}

				if (matchesCriteria) {
					UserHelper.updateMembership(token, userProfile, aUserForm, true);
					UserHelper.copyPersonalDataProperties(userProfile, aUserForm);

					treeResult.add(aUserForm);
				}
			}

		}

		String searchSubString = searchBy.equals("allUsers") ? null : searchFor;

		// Sort them
		Collections.sort(treeResult);

		List<IndexedTreeNode> indexedTreeNodesOfUsersAndUserGroups = new ArrayList<IndexedTreeNode>();
		createIndexedTreeNodeListOfUsersAndUserGroups(searchSubString, treeResult,
		    indexedTreeNodesOfUsersAndUserGroups);
		request.setAttribute("indexedTreeNodesOfUsersAndUserGroups",
		    indexedTreeNodesOfUsersAndUserGroups);

		return mapping.findForward(DracConstants.LIST_USERS_RESULT_PAGE);
	}

	/*
     * 
     */
	public ActionForward queryUser(ActionMapping mapping, ActionForm form,
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
		String uid = request.getParameter("uid");
		ActionForward forward = null;
		RequestHandler rh = RequestHandler.INSTANCE;

		if (uid != null && !DracConstants.EMPTY_STRING.equals(uid)) {
			forward = mapping.findForward(DracConstants.USER_DETAILS_PAGE);

			UserProfile aUP = null;
			try {
				log.debug(logEntry + "operation=Get the user profile for: " + uid);
				aUP = rh.getUserProfile(token, uid);
			}
			catch (RequestHandlerException e) {
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.USER_NOT_FOUND, uid));
				saveMessages(request, messages);
			}
			if (aUP != null) {
				log.debug(logEntry + "operation=Get the global policy.");
				GlobalPolicy aGP = rh.getGlobalPolicy(token);
				if (aGP != null) {
					UserHelper.populateUserForm(token, locale, tz, aUP, aGP,
					    (UserForm) form);
				}
				else {
					// Not found!
					ActionMessages messages = new ActionMessages();
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					    DracConstants.GLOBAL_POLICY_NOT_FOUND, uid));
					saveMessages(request, messages);
				}
			}
			else {
				// Not found (or policy check failed)!
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.USER_NOT_FOUND, uid));
				saveMessages(request, messages);
			}
		}
		else {
			// may have timed out and lost the uid, redirect to the list page.
			forward = mapping.findForward(DracConstants.LIST_USERS_PAGE);
		}
		return forward;
	}

	public ActionForward setupCreateUser(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		// ***************************************************************************
		// User must be valid so we are onto the main page logic.
		// ***************************************************************************

		// Create base logging string for this page.
		String logEntry = "setupCreateUser: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);

		if (userDetails.getUserPolicyProfile().getUserGroupType()
		    .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)
		    || userDetails.getUserPolicyProfile().getUserGroupType()
		        .equals(UserGroupProfileXML.UserGroupType.GROUP_ADMIN)) {
			// Entry into page.
			log.debug(logEntry + "operation=setup create user;");

			request.setAttribute("editable", "true");
		}
		else {
			// Entry into page.
			log.debug(logEntry + "user is not allowed to perform create user;");
		}

		CreateUserForm createUserForm = (CreateUserForm) form;
		RequestHandler aRequestHandler = RequestHandler.INSTANCE;

		// Set up user groups, don't want to duplicate if already have membership
		// set
		List<UserGroupName> userGroupsList = aRequestHandler
		    .getUserGroupProfileNames(token);
		List<String> memberUserGroups = stringArrayToList(createUserForm
		    .getMemberUserGroups());
		List<String> availableUserGroups = new ArrayList<String>();
		UserGroupName userGroup = null;
		for (int i = 0; i < userGroupsList.size(); i++) {
			userGroup = userGroupsList.get(i);
			if (!memberUserGroups.contains(userGroup.toString())) {
				availableUserGroups.add(userGroup.toString());
			}
		}
		createUserForm.setAvailableUserGroups(availableUserGroups
		    .toArray(new String[availableUserGroups.size()]));

		GlobalPolicy gp = aRequestHandler.getGlobalPolicy(token);
		if (gp != null) {
			GlobalAuthentication globalAuth = gp.getSupportedAuthenticationData();
			if (globalAuth != null) {
				List<SupportedAuthenticationType> authTypes = globalAuth
				    .getOnlySupportedAuthenticationTypes();
				if (authTypes != null && !authTypes.isEmpty()) {
					List<String> list = new ArrayList<String>(authTypes.size());
					for (SupportedAuthenticationType authType : authTypes) {
						list.add(authType.getType().toString());
					}
					createUserForm.setAvailAuthenticationTypes(list);
				}
			}
		}

		return mapping.findForward(DracConstants.CREATE_USER_JSP_PAGE);
	}

	public ActionForward setupEditUser(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		// Create base logging string for this page.
		String logEntry = "setupEditUser: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		// Entry into page.
		log.debug(logEntry + "operation=invoke");

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		TimeZone tz = DracHelper.getTimeZone(token);

		EditUserForm editForm = (EditUserForm) form;

		String uid = request.getParameter("uid");
		if (uid == null) {
			uid = (String) request.getAttribute("uid");
		}
		if (uid == null) {
			uid = editForm.getUserID();
		}

		ActionForward forward = null;

		if (uid != null && !DracConstants.EMPTY_STRING.equals(uid)) {

			forward = mapping.findForward(DracConstants.EDIT_USER_JSP_PAGE);

			RequestHandler rh = RequestHandler.INSTANCE;
			UserProfile aUP = rh.getUserProfile(token, uid);

			if (rh.isUserEditable(token, aUP)) {
				/*
				 * ******** WARNING *********** The jsp files appear all to modify for
				 * 'display' versus 'edit' using the struts logic tag "<logic:present"
				 * on the request attribute editable. So, the security model will break
				 * very badly if you set the editable attribute in the request to
				 * 'false' ... it will still be interpreted as true ... thus granting
				 * edit privileges when not otherwise appropriate.
				 */
				request.setAttribute("editable", "editable");
			}

			if (aUP != null) {
				GlobalPolicy gp = rh.getGlobalPolicy(token);
				if (gp != null) {
					UserHelper.populateEditUserForm(locale, tz, gp, aUP, editForm);
				}

				List<UserGroupName> userGroupList = rh.getUserGroupProfileNames(token);

				/*
				 * see if any of the membership fields already have values, only if we
				 * are coming back from an error we don't want to duplicate them in the
				 * available lists and membership lists
				 */
				MembershipData membershipData = aUP.getMembershipData();
				Set<UserGroupName> memberUserGroups = null;
				if (membershipData != null) {
					memberUserGroups = membershipData.getMemberUserGroupName();
				}
				else {
					memberUserGroups = new TreeSet<UserGroupName>();
					log.warn("User membership data is null for " + aUP.getUserID());
				}

				List<String> availableUserGroups = new ArrayList<String>();
				String userGroup = null;
				for (int i = 0; i < userGroupList.size(); i++) {
					userGroup = userGroupList.get(i).toString();
					if (!memberUserGroups.contains(new UserGroupName(userGroup))) {
						availableUserGroups.add(userGroup);
					}
				}
				editForm.setAvailableUserGroups(availableUserGroups
				    .toArray(new String[availableUserGroups.size()]));
				List<String> userGroupsAsString = new ArrayList<String>();
				for (UserGroupName g : memberUserGroups) {
					userGroupsAsString.add(g.toString());
				}
				editForm.setMemberUserGroups(userGroupsAsString
				    .toArray(new String[userGroupsAsString.size()]));
			}
			else {
				// Not found!
				ActionMessages actionMessages = new ActionMessages();
				actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.USER_NOT_FOUND, uid));
				saveMessages(request, actionMessages);
			}
		}
		else {
			// may have timed out and lost the uid, redirect to the list page.
			forward = mapping.findForward(DracConstants.LIST_USERS_PAGE);
		}
		return forward;
	}

	private void createIndexedTreeNodeListOfUsersAndUserGroups(
	    String subStringHighlight, List<UserForm> treeResult,
	    List<IndexedTreeNode> result) {
		int runningIndex = 0;

		for (UserForm userForm : treeResult) {
			int thisIdx = ++runningIndex;

			IndexedTreeNode treeNode = new IndexedTreeNode(userForm.getUserID(), 0,
			    thisIdx, false);
			result.add(treeNode);
			treeNode.setSubStringHighlight(subStringHighlight);

			Map<String, String> tooltipMap = new LinkedHashMap<String, String>();
			tooltipMap.put("<b>User id: </b>", StringParser
			    .encodeForXMLSpecialChars(StringParser.escapeForClient(userForm
			        .getUserID())));
			tooltipMap.put("<b>Common name: </b>", StringParser
			    .encodeForXMLSpecialChars(StringParser.escapeForClient(userForm
			        .getCommonName())));
			tooltipMap.put("<b>Given name: </b>", StringParser
			    .encodeForXMLSpecialChars(StringParser.escapeForClient(userForm
			        .getGivenName())));
			tooltipMap.put("<b>Surname: </b>", StringParser
			    .encodeForXMLSpecialChars(StringParser.escapeForClient(userForm
			        .getSurname())));
			tooltipMap.put("<b>Phone: </b>", StringParser
			    .encodeForXMLSpecialChars(StringParser.escapeForClient(userForm
			        .getPhone())));
			tooltipMap.put("<b>Email: </b>", StringParser
			    .encodeForXMLSpecialChars(StringParser.escapeForClient(userForm
			        .getEmail())));
			treeNode.setClientDataMap(tooltipMap);

			List<List<String>> lineages = userForm.getMemberUserGroupNameTree();

			for (List<String> lineage : lineages) {
				int parentIdx = thisIdx;
				for (String group : lineage) {
					treeNode = new IndexedTreeNode(group, parentIdx, ++runningIndex, true);
					result.add(treeNode);
					treeNode.setSubStringHighlight(subStringHighlight);

					parentIdx = runningIndex;
				}
			}
		}
	}

	private List<String> stringArrayToList(String[] array) {
		List<String> list = new ArrayList<String>();
		if (array != null) {
			for (String element : array) {
				list.add(element);
			}
		}
		return list;
	}

}
