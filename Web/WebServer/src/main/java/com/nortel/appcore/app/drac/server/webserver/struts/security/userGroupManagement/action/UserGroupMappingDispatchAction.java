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

package com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.RootUserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracActionMessage;
import com.nortel.appcore.app.drac.server.webserver.struts.common.IndexedTreeNode;
import com.nortel.appcore.app.drac.server.webserver.struts.general.action.GeneralMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement.UserGroupHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement.form.CreateUserGroupForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement.form.ListUserGroupsForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement.form.UserGroupForm;

/**
 * Created on 19-Oct-2006
 */
public final class UserGroupMappingDispatchAction extends
    DracBaseMappingDispatchAction {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	// no instance variables allowed, struts not thread-safe

	/*
     * 
     */
	public ActionForward createUserGroupResult(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "createUserGroupResult:userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=create UserGroup result...");

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		String userGroupId = DracHelper.decodeFromUTF8(request.getParameter("gid"));

		if (userGroupId != null && !userGroupId.equals("")) {
			log.debug(logEntry + "createUserGroupResult:get UG (" + userGroupId
			    + ") from DB...");
			UserGroupProfile aUGProfile = RequestHandler.INSTANCE
			    .getUserGroupProfile(token, new UserGroupName(userGroupId));

			if (aUGProfile != null) {
				log.debug(logEntry
				    + "createUserGroupResult:found UGProfile. Update the UserGroup form.");
				UserGroupHelper.updateUserGroupForm(locale, tz, aUGProfile,
				    (UserGroupForm) form);
			}
			else {
				// Not found!
				log.debug(logEntry + "createUserGroupResult:UG (" + userGroupId
				    + ") not found!");
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.USER_GROUP_NOT_FOUND, userGroupId));
				saveMessages(request, messages);
			}
		}
		else {
			String error = (String) request.getAttribute("message");
			log.debug(logEntry
			    + "createUserGroupResult:userGroupID from request is null! Error is: "
			    + error);
			((UserGroupForm) form).setError(error);
		}
		return mapping.findForward(DracConstants.CREATE_USER_GROUP_RESULT_PAGE);
	}

	/*
     * 
     */
	public ActionForward deleteUserGroup(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		// Create base logging string for this page.
		String logEntry = "deleteUserGroup: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		// Entry into page.
		log.debug(logEntry + "operation=invoke;");
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		String gid = request.getParameter("gid");

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in userGroupDetails.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (gid != null && !gid.equals("")) {
			ActionForward forward = mapping
			    .findForward(DracConstants.DELETE_USERGROUP_RESULT_PAGE);
			ActionMessages messages = new ActionMessages();

			try {
				RequestHandler.INSTANCE.deleteUserGroup(token,
				    new UserGroupName(gid));
				ActionMessage message = new ActionMessage(
				    DracConstants.DELETE_USERGROUP_PASS, gid);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
			}
			catch (RequestHandlerException e) {
				ActionMessage message = new ActionMessage(
				    DracConstants.DELETE_USERGROUP_FAIL, gid);
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
		    mapping.findForward(DracConstants.LIST_USER_GROUPS_PAGE));
	}

	/*
     * 
     */
	public ActionForward editUserGroup(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "editUserGroup: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=edit UserGroup;");

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in userGroupDetails.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		UserGroupForm aForm = (UserGroupForm) form;
		ActionRedirect redirect = new ActionRedirect(
		    mapping.findForward(DracConstants.EDIT_USER_GROUP_RESULT_PAGE));
		String userGroupName = request.getParameter("userGroupName");
		if (userGroupName == null) {
			userGroupName = (String) request.getAttribute("ugName");
		}
		try {
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);

			if (!postValidateUserGroup(userGroupName, aForm.getUserMembership())) {
				throw new RequestHandlerException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { "edit", "User membership cannot be empty" });
			}

			RequestHandler rh = RequestHandler.INSTANCE;
			UserGroupProfile aUGProfile = rh.getUserGroupProfile(token,
			    new UserGroupName(userGroupName));
			MembershipData oldMembership = aUGProfile.getMembership();

			UserGroupHelper.copyProperties(
			    (Locale) session.getAttribute(DracConstants.MYLOCALE), aForm,
			    aUGProfile.getGroupPolicy());

			rh.setUserGroupUserGroupPolicy(token, new UserGroupName(userGroupName),
			    aUGProfile.getGroupPolicy());

			MembershipData newMembership = new MembershipData(
			    oldMembership.getCreatedByGroupName(),
			    stringArrayToSet(aForm.getUserMembership()),
			    oldMembership.getMemberUserGroupName(),
			    stringArrayToSet(aForm.getResourceGroupMembership()));

			log.debug("editUserGroup setting membership for userGroup "
			    + userGroupName + " from " + oldMembership.toXMLString() + " to "
			    + newMembership.toXMLString());
			rh.setUserGroupMembership(token, new UserGroupName(userGroupName),
			    newMembership);

			redirect.addParameter("ugName", userGroupName);
			redirect.addParameter("successParam", "User Group edited successfully.");
		}
		catch (RequestHandlerException e) {
			request.setAttribute("ugName", userGroupName);
			ActionMessages messages = new ActionMessages();
			ActionMessage message = new DracActionMessage(e).getActionMessage(locale);
			messages.add(ActionMessages.GLOBAL_MESSAGE, message);
			saveMessages(request, messages);
			return mapping.findForward(DracConstants.EDIT_USER_GROUP_RESULT_PAGE);
		}
		return redirect;
	}

	/*
     * 
     */
	public ActionForward forwardListUserGroupsPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		/* Create base logging string for this action. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		StringBuilder logEntryBuffer = new StringBuilder(
		    "forwardListUserGroupsPage:userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer
		    .append("operation=Display the List User Groups Search Criteria page...");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		return mapping.findForward(DracConstants.LIST_USER_GROUPS_PAGE);
	}

	public ActionForward handleCreateUserGroup(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "handleCreateUserGroup: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);

		log.debug(logEntry + "operation=handle create UserGroup;");

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in createUserGroup.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		CreateUserGroupForm aForm = (CreateUserGroupForm) form;

		if (!DracConstants.EMPTY_STRING.equals(aForm.getName())) {
			ActionRedirect redirect = new ActionRedirect(
			    mapping
			        .findForward(DracConstants.HANDLE_CREATE_USER_GROUP_SUCCESS_PAGE));
			try {
				UserGroupName userGroupID = UserGroupHelper
				    .create(token, locale, aForm);
				redirect.addParameter("gid",
				    DracHelper.encodeToUTF8(userGroupID.toString()));
			}
			catch (RequestHandlerException e) {
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new DracActionMessage(e)
				    .getActionMessage(locale);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
				return mapping
				    .findForward(DracConstants.HANDLE_CREATE_USER_GROUP_FAILED_PAGE);
			}
			return redirect;
		}
		// lost request parameters through authenticate redirect
		return mapping.findForward(DracConstants.CREATE_USER_GROUP_ACTION);
	}

	public ActionForward listUserGroups(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		ListUserGroupsForm aListUserGroupsForm = (ListUserGroupsForm) form;

		// ***************************************************************************
		// User must be valid so we are onto the main page logic.
		// ***************************************************************************

		String searchBy = aListUserGroupsForm.getSearchBy();
		String searchFor = aListUserGroupsForm.getSearchFor();

		RequestHandler rh = RequestHandler.INSTANCE;
		List<UserGroupProfile> userGroupProfilesList = rh.getUserGroups(token);

		List<UserGroupForm> treeResult = new ArrayList<UserGroupForm>();
		String aUserGroupName = "";
		boolean matchesCriteria = false;

		for (UserGroupProfile ugProfile : userGroupProfilesList) {
			matchesCriteria = false;
			aUserGroupName = ugProfile.getName().toString();

			if (searchBy.equals("allUserGroups")) {
				matchesCriteria = true;
			}

			else if (searchBy.equals("name")) {
				if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
					if (DracHelper.doWildCardWork(searchFor, aUserGroupName)) {
						matchesCriteria = true;
					}
				}
			}

			if (matchesCriteria) {
				// { SystemAdminGroup, TestGroup, SubTestGroup, SubSubTestGroup }
				List<String> groupNameLineage = rh.getUserGroupNameLineage(token,
				    ugProfile);

				mergeLineageListToResults(rh, treeResult, groupNameLineage,
				    userGroupProfilesList);
			}
		}

		String searchSubString = searchBy.equals("name") ? searchFor : null;

		List<IndexedTreeNode> indexedTreeNodesOfGroupsAndUsers = new ArrayList<IndexedTreeNode>();
		createIndexedTreeNodeListOfGroupsAndUsers(searchSubString, treeResult,
		    indexedTreeNodesOfGroupsAndUsers, 0, 0);
		request.setAttribute("indexedTreeNodesOfGroupsAndUsers",
		    indexedTreeNodesOfGroupsAndUsers);

		List<IndexedTreeNode> indexedTreeNodesOfGroupsAndResourceGroups = new ArrayList<IndexedTreeNode>();
		createIndexedTreeNodeListOfGroupsAndResourceGroups(searchSubString,
		    treeResult, indexedTreeNodesOfGroupsAndResourceGroups, 0, 0);
		request.setAttribute("indexedTreeNodesOfGroupsAndResourceGroups",
		    indexedTreeNodesOfGroupsAndResourceGroups);

		return mapping.findForward(DracConstants.LIST_USER_GROUPS_RESULT_PAGE);
	}

	public ActionForward queryUserGroup(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);

		Locale myLocale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);

		/* Create base logging string for this page. */
		String logEntry = "queryUserGroup: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=invoke;");

		String ugName = request.getParameter("ugName");
		if (ugName == null || ugName.equals("")) {
			ugName = (String) request.getAttribute("ugName");
		}
		ActionForward forward = null;

		if (ugName != null && !DracConstants.EMPTY_STRING.equals(ugName)) {
			forward = mapping.findForward(DracConstants.USER_GROUP_DETAILS_PAGE);
			RequestHandler rh = RequestHandler.INSTANCE;

			log.debug(logEntry + "get the UserGroupProfile for: " + ugName);
			UserGroupProfile aUGProfile = rh.getUserGroupProfile(token,
			    new UserGroupName(ugName));

			if (aUGProfile != null) {
				// Only SystemAdmins and GroupAdmins can edit usergroups.
				if (userDetails.getUserPolicyProfile().getUserGroupType()
				    .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)
				    || userDetails.getUserPolicyProfile().getUserGroupType()
				        .equals(UserGroupProfileXML.UserGroupType.GROUP_ADMIN)) {

					// request.setAttribute("editable", rh.isUserGroupEditable(token,
					// aUGProfile));
					/*
					 * ******** WARNING *********** The jsp files appear all to modify for
					 * 'display' versus 'edit' using the struts logic tag "<logic:present"
					 * on the request attribute editable. So, the security model will
					 * break very badly if you set the editable attribute in the request
					 * to 'false' ... it will still be interpreted as true ... thus
					 * granting edit privileges when not otherwise appropriate.
					 */
					if (rh.isUserGroupEditable(token, aUGProfile)) {
						// Here, set the attribute to any value. Let's avoid true/false to
						// highlight the note
						// above
						request.setAttribute("editable", "editable");
					}
				}

				List<String> userIDs = rh.getUserIDs(token);
				/*
				 * see if any of the membership fields already have values, only if we
				 * are coming back from an error we don't want to duplicate them in the
				 * available lists and membership lists
				 */
				Set<String> memberUserIds = aUGProfile.getMembership()
				    .getMemberUserID();
				Set<String> memberResourceGroups = aUGProfile.getMembership()
				    .getMemberResourceGroupName();

				List<String> availableUsers = new ArrayList<String>();
				String userID = null;
				for (int i = 0; i < userIDs.size(); i++) {
					userID = userIDs.get(i);
					if (!memberUserIds.contains(userID)) {
						// Add this userID to the available list.
						availableUsers.add(userID);
					}
				}

				List<String> availableResourceGroups = new ArrayList<String>();
				List<ResourceGroupProfile> resourceGroupList = rh
				    .getResourceGroups(token);

				for (ResourceGroupProfile resourceGroup : resourceGroupList) {
					if (rh.isResourceGroupEditable(token, resourceGroup)) {
						String resourceGroupName = resourceGroup.getName();

						if (!memberResourceGroups.contains(resourceGroupName)) {
							// Add this resourceGroupName to the available list.
							availableResourceGroups.add(resourceGroupName);
						}
					}
				}

				for (String resourceGroupName : availableResourceGroups) {
					log.debug("Available: " + resourceGroupName);
				}

				UserGroupForm userGrpForm = (UserGroupForm) form;

				// junk
				// userGrpForm.setAvailableUserGroups(availableUserGroups.toArray(new
				// String[availableUserGroups.size()]));
				userGrpForm.setAvailableResourceGroups(availableResourceGroups
				    .toArray(new String[availableResourceGroups.size()]));
				userGrpForm.setAvailableUsers(availableUsers
				    .toArray(new String[availableUsers.size()]));

				UserGroupHelper.updateUserGroupForm(myLocale, tz, aUGProfile,
				    userGrpForm);
			}
			else {
				// Not found!
				log.debug(logEntry + "could not get the UserGroupProfile for: " + ugName);
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.USER_GROUP_NOT_FOUND, ugName));
				saveMessages(request, messages);
			}
		}
		else {
			// may have timed out and lost the ugName, redirect to the list page
			log.debug(logEntry + "failed to get the UserGroupProfile for: " + ugName);
			forward = mapping.findForward(DracConstants.LIST_USER_GROUPS_PAGE);
		}
		return forward;
	}

	/*
     * 
     */
	public ActionForward setupCreateUserGroup(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "setupCreateUserGroup: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		if (userDetails.getUserPolicyProfile().getUserGroupType()
		    .equals(UserGroupProfileXML.UserGroupType.USER)) {
			log.debug(logEntry + "user is not allowed to perform create user group;");
		}
		else {
			log.debug(logEntry + "get this user's UserProfile from the DB. UserID: "
			    + token.getUser());

			request.setAttribute("editable", "true");

			CreateUserGroupForm createUserGroupForm = (CreateUserGroupForm) form;

			RequestHandler rh = RequestHandler.INSTANCE;

			// Build the drop down list of parent user groups.
			// Get my userProfile to determine what userGroups this user belongs to.
			log.debug(logEntry + "get the UserProfile from DB for: " + token.getUser());

			// Fixup the issue with populating the paren usergroup
			List<UserGroupProfile> parentUserGroupProfileList = new ArrayList<UserGroupProfile>();
			List<String> parentUserGroupsList = new ArrayList<String>();
			try {
				parentUserGroupProfileList = rh.getUserGroups(token);
				for (UserGroupProfile userGroupProfile : parentUserGroupProfileList) {
					if (!userGroupProfile.getUserGroupType().equals(
					    UserGroupProfileXML.UserGroupType.USER)) {
						parentUserGroupsList.add(userGroupProfile.getName().toString());
					}
				}
			}
			catch (Exception e) {
				// Colin should handle this exception better
				log.debug(logEntry + " parent user group empty", e);
			}

			Collections.sort(parentUserGroupsList);
			createUserGroupForm.setAvailableUserGroups(parentUserGroupsList
			    .toArray(new String[parentUserGroupsList.size()]));
	
			// First, do this for user ids.
			log.debug(logEntry + "get the UserIDs from the DB.");
			// Get the list of UserIDs this user can see.
			List<String> userIDs = rh.getUserIDs(token);

			TreeSet<String> memberUserIds = stringArrayToSet(createUserGroupForm
			    .getUserMembership());
			List<String> availableUsers = new ArrayList<String>();
			String userID = null;

			for (int i = 0; i < userIDs.size(); i++) {
				userID = userIDs.get(i);
				if (!memberUserIds.contains(userID)) {
					if (!availableUsers.contains(userID)) {
						// Add this userID if it is not already in our list.
						availableUsers.add(userID);
					}
					else {
						log.debug(logEntry + "duplicate: " + userID);
					}
				}
			}
			Collections.sort(availableUsers);
			createUserGroupForm.setAvailableUsers(availableUsers
			    .toArray(new String[availableUsers.size()]));

			// Now do this for available resource groups.
			log.debug(logEntry + "get the RGNames from the DB.");
			List<ResourceGroupProfile> resourceGroupList = rh
			    .getResourceGroups(token);

			TreeSet<String> memberResourceGroups = stringArrayToSet(createUserGroupForm
			    .getResourceGroupMembership());
			List<String> availableResourceGroups = new ArrayList<String>();

			for (ResourceGroupProfile resourceGroup : resourceGroupList) {
				if (rh.isResourceGroupEditable(token, resourceGroup)) {
					String resourceGroupName = resourceGroup.getName();
					if (!memberResourceGroups.contains(resourceGroupName)) {
						if (!availableResourceGroups.contains(resourceGroupName)) {
							// Add this rgName if it is not already in our list.
							availableResourceGroups.add(resourceGroupName);
						}
						else {
							log.debug(logEntry + "duplicate: " + resourceGroupName);
						}
					}
				}
			}

			Collections.sort(availableResourceGroups);
			createUserGroupForm.setAvailableResourceGroups(availableResourceGroups
			    .toArray(new String[availableResourceGroups.size()]));
		}

		return mapping.findForward(DracConstants.CREATE_USER_GROUP_JSP_PAGE);
	}

	private int createIndexedTreeNodeListOfGroupsAndResourceGroups(
	    String subStringHighlight, List<UserGroupForm> treeResult,
	    List<IndexedTreeNode> result, int pIdx, int runningIndex) {
		for (UserGroupForm ugForm : treeResult) {
			int parentGroupIdx = pIdx;
			int thisGroupIdx = ++runningIndex;

			IndexedTreeNode treeNode = new IndexedTreeNode(ugForm.getName(),
			    parentGroupIdx, thisGroupIdx, true);
			result.add(treeNode);
			treeNode.setSubStringHighlight(subStringHighlight);

			for (String user : ugForm.getResourceGroupMembership()) {
				result.add(new IndexedTreeNode(user, thisGroupIdx, ++runningIndex,
				    false));
			}

			runningIndex = createIndexedTreeNodeListOfGroupsAndResourceGroups(
			    subStringHighlight, ugForm.getChildGroups(), result, thisGroupIdx,
			    runningIndex);
		}

		return runningIndex;
	}

	private int createIndexedTreeNodeListOfGroupsAndUsers(
	    String subStringHighlight, List<UserGroupForm> treeResult,
	    List<IndexedTreeNode> result, int pIdx, int runningIndex) {
		for (UserGroupForm ugForm : treeResult) {
			int parentGroupIdx = pIdx;
			int thisGroupIdx = ++runningIndex;

			IndexedTreeNode treeNode = new IndexedTreeNode(ugForm.getName(),
			    parentGroupIdx, thisGroupIdx, true);
			result.add(treeNode);
			treeNode.setSubStringHighlight(subStringHighlight);

			for (String user : ugForm.getUserMembership()) {
				result.add(new IndexedTreeNode(user, thisGroupIdx, ++runningIndex,
				    false));
			}

			runningIndex = createIndexedTreeNodeListOfGroupsAndUsers(
			    subStringHighlight, ugForm.getChildGroups(), result, thisGroupIdx,
			    runningIndex);
		}

		return runningIndex;
	}

	private UserGroupForm findUGForm(RequestHandler rh,
	    List<UserGroupForm> treeResult, String groupName,
	    List<UserGroupProfile> userGroupProfilesList) throws Exception {
		UserGroupForm ugForm = null;

		for (UserGroupForm form : treeResult) {
			if (form.getName().equals(groupName)) {
				ugForm = form;
				break;
			}
		}

		if (ugForm == null) {
			ugForm = new UserGroupForm();
			ugForm.setName(groupName);
			ugForm.setWebSafeName(groupName);

			// Retrieve the ugProfile for this group
			for (UserGroupProfile ugProfile : userGroupProfilesList) {
				String userGroupName = ugProfile.getName().toString();
				if (userGroupName.equals(groupName)) {
					UserGroupHelper.updateMembership(ugProfile, ugForm);
				}
			}

			treeResult.add(ugForm);
		}

		return ugForm;
	}

	private void mergeLineageListToResults(RequestHandler rh,
	    List<UserGroupForm> treeResult, List<String> groupNameLineage,
	    List<UserGroupProfile> userGroupProfilesList) throws Exception {
		if (groupNameLineage.size() > 0) {
			String groupName = groupNameLineage.remove(0);
			UserGroupForm ugForm = findUGForm(rh, treeResult, groupName,
			    userGroupProfilesList);
			List<UserGroupForm> childGroups = ugForm.getChildGroups();
			mergeLineageListToResults(rh, childGroups, groupNameLineage,
			    userGroupProfilesList);
		}

		// Sort all peers
		Collections.sort(treeResult);
	}

	private boolean postValidateUserGroup(String ugpName,
	    String[] newUserMembership) {
		if (ugpName == null) {
			return false;
		}

		if (newUserMembership == null) {
			return false;
		}

		if (ugpName.equals(RootUserGroupName.SYSTEM_ADMIN_GROUP.toString())) {
			if (newUserMembership.length == 0) {
				return false;
			}
		}
		return true;
	}

	private TreeSet<String> stringArrayToSet(String[] array) {
		TreeSet<String> list = new TreeSet<String>();
		if (array != null) {
			for (String element : array) {
				list.add(element);
			}
		}
		return list;
	}
}
