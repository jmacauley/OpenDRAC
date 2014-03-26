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

package com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.common.types.Layer;
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
import com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.ResourceGroupHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form.CreateResourceGroupForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form.ListResourceGroupsForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form.ResourceForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form.ResourceGroupForm;

/**
 * Created on 19-Oct-2006
 */
public final class ResourceGroupMappingDispatchAction extends
    DracBaseMappingDispatchAction {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	// no instance variables allowed, struts not thread-safe

	/*
     * 
     */
	public ActionForward createResourceGroupResult(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "createResourceGroupResult: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=create ResourceGroup result");

		String rgid = DracHelper.decodeFromUTF8(request.getParameter("rgid"));

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);
		if (rgid != null && !rgid.equals("")) {
			RequestHandler rh = RequestHandler.INSTANCE;
			ResourceGroupProfile rgp = rh.getResourceGroupProfile(token, rgid);

			if (rgp != null) {
				ResourceGroupForm aForm = (ResourceGroupForm) form;
				Map<String, String> filter = new HashMap<String, String>();
				filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
				    Layer.LAYER_ALL.toString());
				List<EndpointResourceUiType> masterEndpointList = RequestHandler
				    .INSTANCE.getUserEndpointsUIInfo(token, filter);
				ResourceGroupHelper.updateResourceGroupForm(
				    (Locale) session.getAttribute(DracConstants.MYLOCALE), tz, rgp,
				    aForm, masterEndpointList);
			}
			else {
				log.debug(logEntry
				    + "operation=unable to read new RG back from DB for: " + rgid);
				String error = "Unable to create Resource Group for: " + rgid;
				((ResourceGroupForm) form).setError(error);
			}
		}
		else {
			String error = (String) request.getAttribute("message");
			log.debug(logEntry + "operation=lost the rgid from the HTTP request for: "
			    + rgid);
			((ResourceGroupForm) form).setError(error);
		}
		return mapping.findForward(DracConstants.CREATE_RESOURCE_GROUP_RESULT_PAGE);
	}

	/*
     * 
     */
	public ActionForward deleteResourceGroup(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		// Create base logging string for this page.
		String logEntry = "deleteResourceGroup: userID=" + activeUserID
		    + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		// Entry into page.
		log.debug(logEntry + "operation=invoke");

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		String rgid = request.getParameter("rgid");
		Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in resourceGroupDetails.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		if (rgid != null && !rgid.equals("")) {
			ActionForward forward = mapping
			    .findForward(DracConstants.DELETE_RESGROUP_RESULT_PAGE);
			ActionMessages messages = new ActionMessages();

			try {
				RequestHandler.INSTANCE.deleteResourceGroup(token, rgid);
				ActionMessage message = new ActionMessage(
				    DracConstants.DELETE_RESGROUP_PASS, rgid);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
			}
			catch (RequestHandlerException e) {
				ActionMessage message = new ActionMessage(
				    DracConstants.DELETE_RESGROUP_FAIL, rgid);
				ActionMessage message2 = new DracActionMessage(e)
				    .getActionMessage(locale);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message2);
				saveMessages(request, messages);
				request.setAttribute("error", "true");
			}
			return forward;
		}
		else {
			return new ActionRedirect(
			    mapping.findForward(DracConstants.LIST_RESOURCE_GROUPS_PAGE));
		}

	}

	/*
     * 
     */
	public ActionForward editResourceGroup(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "editResourceGroup: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=edit ResourceGroup");

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in resourceGroupDetails.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		ResourceGroupForm aForm = (ResourceGroupForm) form;
		ActionRedirect redirect = new ActionRedirect(
		    mapping.findForward(DracConstants.EDIT_RESOURCE_GROUP_RESULT_PAGE));
		String resGroupName = request.getParameter("resGroupName");
		Locale myLocale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		try {
			LoginToken token = (LoginToken) session
			    .getAttribute(DracConstants.TOKEN_OBJ);
			ResourceGroupProfile aRGProfile = RequestHandler.INSTANCE
			    .getResourceGroupProfile(token, resGroupName);
			Map<String, String> filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER_ALL.toString());
			List<EndpointResourceUiType> masterEndpointList = RequestHandler
			    .INSTANCE.getUserEndpointsUIInfo(token, filter);
			List<String> memberTNAs = stringArrayToList(aForm.getMemberTNAs());
			ResourceGroupHelper.copyProperties(myLocale, token, aForm, resGroupName,
			    aRGProfile, memberTNAs, masterEndpointList);

			redirect.addParameter("rgName", resGroupName);
			redirect.addParameter("successParam",
			    "Resource Group edited successfully.");
		}
		catch (RequestHandlerException e) {
			ActionMessages messages = new ActionMessages();
			ActionMessage message = new DracActionMessage(e)
			    .getActionMessage(myLocale);
			messages.add(ActionMessages.GLOBAL_MESSAGE, message);
			saveMessages(request, messages);
			request.setAttribute("rgName", resGroupName);
			return mapping.findForward(DracConstants.EDIT_RESOURCE_GROUP_RESULT_PAGE);
		}
		return redirect;
	}

	/*
     * 
     */
	public ActionForward forwardListResourceGroupsPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		/* Create base logging string for this action. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		StringBuilder logEntryBuffer = new StringBuilder(
		    "forwardListResourceGroupsPage: userID=");
		logEntryBuffer.append(activeUserID);
		logEntryBuffer.append(":sessionID=");
		logEntryBuffer.append(sessionID);
		logEntryBuffer.append(":ipAddress=");
		logEntryBuffer.append(request.getRemoteAddr());
		logEntryBuffer.append(":protocol=");
		logEntryBuffer.append(request.getProtocol());
		logEntryBuffer.append("operation=setting up get list of resource groups;");
		String logEntry = logEntryBuffer.toString();
		log.debug(logEntry);

		return mapping.findForward(DracConstants.LIST_RESOURCE_GROUPS_PAGE);
	}

	public ActionForward handleCreateResourceGroup(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "handleCreateResourceGroup: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";
		Locale myLocale = (Locale) session.getAttribute(DracConstants.MYLOCALE);
		/* Entry into page. */
		log.debug(logEntry + "operation=handle create ResourceGroup;");

		// Security (Crystal Box):
		// ===========================================================
		// Requires token set in createResourceGroup.jsp
		GeneralMappingDispatchAction.verifyCSRFToken(session, request);

		CreateResourceGroupForm aForm = (CreateResourceGroupForm) form;
		ActionRedirect redirect = new ActionRedirect(
		    mapping
		        .findForward(DracConstants.HANDLE_CREATE_RESOURCE_GROUP_SUCCESS_PAGE));
		if (!DracConstants.EMPTY_STRING.equals(aForm.getName())) {
			try {
				LoginToken token = (LoginToken) session
				    .getAttribute(DracConstants.TOKEN_OBJ);
				// Initialize the list of available resources/endpoints as TNAs:
				Map<String, String> filter = new HashMap<String, String>();
				filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
				    Layer.LAYER_ALL.toString());
				List<EndpointResourceUiType> masterEndpointList = RequestHandler
				    .INSTANCE.getUserEndpointsUIInfo(token, filter);
				List<String> memberTNAs = stringArrayToList(aForm.getMemberTNAs());
				String resourceGroupID = ResourceGroupHelper.create(myLocale, token,
				    aForm, memberTNAs, masterEndpointList);

				redirect.addParameter("rgid", DracHelper.encodeToUTF8(resourceGroupID));
			}
			catch (RequestHandlerException e) {
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new DracActionMessage(e)
				    .getActionMessage(myLocale);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);
				return mapping
				    .findForward(DracConstants.HANDLE_CREATE_RESOURCE_GROUP_FAILED_PAGE);
			}
			return redirect;
		}
		// lost request parameters through authenticate redirect
		return mapping.findForward(DracConstants.CREATE_RESOURCE_GROUP_ACTION);
	}

	public ActionForward listResourceGroups(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		ListResourceGroupsForm aListResourceGroupsForm = (ListResourceGroupsForm) form;

		// ***************************************************************************
		// User must be valid so we are onto the main page logic.
		// ***************************************************************************

		String searchBy = aListResourceGroupsForm.getSearchBy();
		String searchFor = aListResourceGroupsForm.getSearchFor();

		RequestHandler rh = RequestHandler.INSTANCE;
		List<ResourceGroupProfile> resourceGroupProfilesList = rh
		    .getResourceGroups(token);

		List<ResourceGroupForm> treeResult = new ArrayList<ResourceGroupForm>();
		boolean matchesCriteria = false;

		Map<String, String> filter = new HashMap<String, String>();
		filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
		    Layer.LAYER_ALL.toString());
		List<EndpointResourceUiType> masterEndpointList = rh
		    .getUserEndpointsUIInfo(token, filter);

		for (ResourceGroupProfile rgProfile : resourceGroupProfilesList) {
			matchesCriteria = false;

			if (searchBy.equals("allResourceGroups")) {
				matchesCriteria = true;
			}

			else if (searchBy.equals("name")) {
				if (!DracConstants.EMPTY_STRING.equals(searchFor)) {
					if (DracHelper.doWildCardWork(searchFor, rgProfile.getName())) {
						matchesCriteria = true;
					}
				}
			}

			if (matchesCriteria) {
				List<String> groupNameLineage = rh.getResourceGroupNameLineage(token,
				    rgProfile);
				mergeLineageListToResults(rh, token, treeResult, groupNameLineage,
				    resourceGroupProfilesList, masterEndpointList);
			}
		}

		String searchSubString = searchBy.equals("name") ? searchFor : null;

		List<IndexedTreeNode> indexedTreeNodesOfResourceGroupsAndResources = new ArrayList<IndexedTreeNode>();
		createIndexedTreeNodeListOfResourceGroupsAndResources(searchSubString,
		    treeResult, indexedTreeNodesOfResourceGroupsAndResources, 0, 0);
		request.setAttribute("indexedTreeNodesOfResourceGroupsAndResources",
		    indexedTreeNodesOfResourceGroupsAndResources);

		List<IndexedTreeNode> indexedTreeNodesOfResourceGroupsAndUserGroups = new ArrayList<IndexedTreeNode>();
		createIndexedTreeNodeListOfResourceGroupsAndUserGroups(searchSubString,
		    treeResult, indexedTreeNodesOfResourceGroupsAndUserGroups, 0, 0);
		request.setAttribute("indexedTreeNodesOfResourceGroupsAndUserGroups",
		    indexedTreeNodesOfResourceGroupsAndUserGroups);

		return mapping.findForward(DracConstants.LIST_RESOURCE_GROUPS_RESULT_PAGE);
	}

	/*
     * 
     */
	public ActionForward queryResource(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		// Create base logging string for this page.
		String logEntry = "userInfo: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		// Entry into page.
		log.debug(logEntry + "operation=queryResource");

		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		String tna = request.getParameter("res");
		ActionForward forward = null;
		Resource resource = null;
		if (tna != null) {
			forward = mapping.findForward(DracConstants.RESOURCE_DETAILS_PAGE);

			EndPointType ep = RequestHandler.INSTANCE.findEndpointByTna(token,
			    tna);

			if (ep != null) {
				String uniqueId = ep.getId();
				if (uniqueId != null) {
					resource = RequestHandler.INSTANCE.getEndpointResource(token,
					    uniqueId);
				}
			}
			else {
				log.warn("Could not find endpoint for " + tna);
			}

			if (resource != null) {
				ResourceGroupHelper.copyProperties(resource, (ResourceForm) form);
				((ResourceForm) form).setName(tna);
				((ResourceForm) form).setLabel(ep.getLabel());
			}
			else {
				// Not found!
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.RESOURCE_NOT_FOUND, tna));
				saveMessages(request, messages);
			}

		}
		else {
			// may have timed out and lost the uid, redirect to the list page.
			forward = mapping
			    .findForward(DracConstants.LIST_RESOURCE_GROUPS_RESULT_PAGE);
		}
		return forward;
	}

	/*
     * 
     */
	public ActionForward queryResourceGroup(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);

		Locale myLocale = (Locale) session.getAttribute(DracConstants.MYLOCALE);

		/* Create base logging string for this page. */
		String logEntry = "resourceGroupInfo: userID=" + activeUserID
		    + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=invoke");
		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);
		String rgName = (String) request.getAttribute("rgName");
		if (rgName == null || rgName.equals("")) {
			rgName = request.getParameter("rgName");
		}
		ActionForward forward = null;

		if (rgName != null && !DracConstants.EMPTY_STRING.equals(rgName)) {
			forward = mapping.findForward(DracConstants.RESOURCE_GROUP_DETAILS_PAGE);

			RequestHandler rh = RequestHandler.INSTANCE;
			ResourceGroupProfile aRGProfile = rh.getResourceGroupProfile(token,
			    rgName);
			boolean resourceGroupIsEditable = false;

			if (aRGProfile != null) {
				if (userDetails.getUserPolicyProfile().getUserGroupType()
				    .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)
				    || userDetails.getUserPolicyProfile().getUserGroupType()
				        .equals(UserGroupProfileXML.UserGroupType.GROUP_ADMIN)) {
					if (rh.isResourceGroupEditable(token, aRGProfile)) {
						resourceGroupIsEditable = true;
						request.setAttribute("editable", "true");
					}
				}

				ResourceGroupForm aResourceGroupForm = (ResourceGroupForm) form;

				// Obtain the master endpoint list (no more cache)...
				// Get the profile for the parent resource group.
				String parentResourceGroupName = aRGProfile.getMembership()
				    .getCreatedByGroupName();
				boolean groupHasParent = parentResourceGroupName != null
				    && parentResourceGroupName.length() > 0;

				// TO DO: what if a parent is deleted???

				List<EndpointResourceUiType> masterEndpointResourceList = null;
				Map<String, String> facilityFilter = new HashMap<String, String>();
				facilityFilter.put(DbKeys.NetworkElementFacilityCols.LAYER,
				    Layer.LAYER_ALL.toString());

				if (resourceGroupIsEditable && groupHasParent) {
					// The master list derives from the parent...forming sets of available
					// and current
					// resources
					masterEndpointResourceList = rh.getUserEndpointsUIInfo(token,
					    parentResourceGroupName, facilityFilter);
				}
				else {
					// The master list derives only from those resource we can access ...
					// forming the current
					// resources list
					masterEndpointResourceList = rh.getUserEndpointsUIInfo(token,
					    aRGProfile.getName(), facilityFilter);
				}

				// Initialize BOTH the form's available AND membership resource data
				ResourceGroupHelper.updateResourceGroupForm(myLocale, tz, aRGProfile,
				    aResourceGroupForm, masterEndpointResourceList);

				// User Groups.
				// Check if already have a list of member user groups, usually if came
				// back from error
				try {
					List<UserGroupProfile> ugProfileList = rh.getUserGroups(token);
					List<String> memberUserGroups = stringArrayToList(aResourceGroupForm
					    .getUserGroupMembership());
					List<String> availableUserGroups = new ArrayList<String>();

					if (ugProfileList != null) {
						for (UserGroupProfile ugp : ugProfileList) {
							if (rh.isUserGroupEditable(token, ugp)) {
								if (!memberUserGroups.contains(ugp.getName().toString())) {
									if (!availableUserGroups.contains(ugp.getName().toString())) {
										availableUserGroups.add(ugp.getName().toString());
									}
									else {
										log.debug(logEntry + "duplicate: " + ugp.getName());
									}
								}
							}
						}
						Collections.sort(availableUserGroups);
						aResourceGroupForm.setAvailableUserGroups(availableUserGroups
						    .toArray(new String[availableUserGroups.size()]));
					}
				}
				catch (RequestHandlerException rhe) {
					log.debug(logEntry + "cannot get User Groups from RH for: "
					    + userDetails.getUserID());
				}

			}
			else {
				// Not found!
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				    DracConstants.RESOURCE_GROUP_NOT_FOUND, rgName));
				saveMessages(request, messages);
			}
		}
		else {
			// may have timed out and lost the rgName, redirect to the list page
			forward = mapping.findForward(DracConstants.LIST_RESOURCE_GROUPS_PAGE);
		}
		return forward;
	}

	public ActionForward setupCreateResourceGroup(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		/*****************************************************************************************************
		 * User must be valid so we are onto the main page logic.
		 ****************************************************************************************************/

		/* Create base logging string for this page. */
		String logEntry = "setupCreateResourceGroup: userID=" + activeUserID
		    + ":sessionID=" + sessionID + ":ipAddress=" + request.getRemoteAddr()
		    + ":protocol=" + request.getProtocol() + ":";

		UserDetails userDetails = (UserDetails) session
		    .getAttribute(DracConstants.AUTH_OBJ);
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		if (userDetails.getUserPolicyProfile().getUserGroupType()
		    .equals(UserGroupProfileXML.UserGroupType.USER)) {
			// Entry into page.
			log.debug(logEntry
			    + "user is not allowed to perform create resource group;");
		}
		else {
			// Entry into page.
			log.debug(logEntry
			    + "operation=admin/group admin trying to create resource group;");

			request.setAttribute("editable", "true");

			CreateResourceGroupForm createResGroupForm = (CreateResourceGroupForm) form;

			RequestHandler rh = RequestHandler.INSTANCE;

			List<String> memberUserGroups = stringArrayToList(createResGroupForm
			    .getReferencedUserGroups());
			List<String> availableUserGroups = new ArrayList<String>();
			List<String> resourceGroupList = new ArrayList<String>();
			List<UserGroupProfile> ugList = rh.getUserGroups(token);

			for (UserGroupProfile ugp : ugList) {
				if (rh.isUserGroupEditable(token, ugp)) {
					if (!memberUserGroups.contains(ugp.getName().toString())) {
						if (!availableUserGroups.contains(ugp.getName().toString())) {
							availableUserGroups.add(ugp.getName().toString());
						}
						else {
							log.debug(logEntry + "duplicate: " + ugp.getName());
						}
					}
				}

				if (ugp.getMembership() != null) {
					for (String resGroupName : ugp.getMembership()
					    .getMemberResourceGroupName()) {
						if (!resourceGroupList.contains(resGroupName)) {
							if (rh.isResourceGroupParentable(token, resGroupName)) {
								resourceGroupList.add(resGroupName);
							}
						}
						else {
							log.debug(logEntry + "duplicate: " + resGroupName);
						}
					}
				}
			}

			Collections.sort(resourceGroupList);
			Collections.sort(availableUserGroups);

			createResGroupForm.setParentResourceGroups(resourceGroupList
			    .toArray(new String[resourceGroupList.size()]));
			createResGroupForm.setAvailableUserGroups(availableUserGroups
			    .toArray(new String[availableUserGroups.size()]));

			// Initialize the list of available resources/endpoints as TNAs:
			/*
			 * Map<String, String> filter = new HashMap<String, String>();
			 * filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			 * Layer.LAYER_ALL.toString()); List<EndpointResourceUIInfo>
			 * masterEndpointList = rh.getUserEndpointsUIInfo(token, filter); // If
			 * we've returned here from an error, there could be selections remaining
			 * in the member list. // Remove these from the available list (no
			 * duplicates across available & members) List<String> memberTNAs =
			 * stringArrayToList(createResGroupForm.getMemberTNAs());
			 * createResGroupForm.setAvailableTNAs
			 * (EndpointResourceUIInfo.toTnaStringArray
			 * (EndpointResourceUIInfo.excludeFrom( memberTNAs, masterEndpointList)));
			 */
			// Setting the available endpoints from here is not required (even as far
			// back as DRAC 3.0)!
			// When the jsp loads, it calls via Ajax ('getTnaForResGrp' in
			// DracServlet) to retrieve the
			// available
			// endpoints for the RGP, thus overriding
			// anything that is set here. The Ajax call does not, however, handle
			// existing entries in the
			// members list...thus causing duplicates. Simply clear both lists here.
			createResGroupForm.setAvailableTNAs(new String[0]);
			createResGroupForm.setMemberTNAs(new String[0]);

		}
		log.debug("Before go to create resource group page");
		return mapping.findForward(DracConstants.CREATE_RESOURCE_GROUP_JSP_PAGE);
	}

	private int createIndexedTreeNodeListOfResourceGroupsAndResources(
	    String subStringHighlight, List<ResourceGroupForm> treeResult,
	    List<IndexedTreeNode> result, int pIdx, int runningIndex) {
		
		for (ResourceGroupForm ugForm : treeResult) {
			int parentGroupIdx = pIdx;
			int thisGroupIdx = ++runningIndex;

			IndexedTreeNode treeNode = new IndexedTreeNode(ugForm.getName(),
			    parentGroupIdx, thisGroupIdx, true);
			result.add(treeNode);
			treeNode.setSubStringHighlight(subStringHighlight);

			String tna = null;
			for (String compoundTnaLabel : ugForm.getMemberTNAs()) {
				// Decode compound string: tna::label
				String[] arr = StringParser.COLONS_PATTERN.split(compoundTnaLabel);
				tna = StringParser.decodeForDRACSpecialChars(arr[0]);

				String label = null;
				// facLabel
				if (arr.length > 1) {
					label = StringParser.decodeForDRACSpecialChars(arr[1]);
				}
				result.add(new IndexedTreeNode(tna, thisGroupIdx, ++runningIndex,
				    false, label));
			}

			runningIndex = createIndexedTreeNodeListOfResourceGroupsAndResources(
			    subStringHighlight, ugForm.getChildGroups(), result, thisGroupIdx,
			    runningIndex);
		}

		return runningIndex;
	}

	private int createIndexedTreeNodeListOfResourceGroupsAndUserGroups(
	    String subStringHighlight, List<ResourceGroupForm> treeResult,
	    List<IndexedTreeNode> result, int pIdx, int runningIndex) {
		for (ResourceGroupForm ugForm : treeResult) {
			int parentGroupIdx = pIdx;
			int thisGroupIdx = ++runningIndex;

			IndexedTreeNode treeNode = new IndexedTreeNode(ugForm.getName(),
			    parentGroupIdx, thisGroupIdx, true);
			result.add(treeNode);
			treeNode.setSubStringHighlight(subStringHighlight);

			for (String userGroup : ugForm.getUserGroupMembership()) {
				result.add(new IndexedTreeNode(userGroup, thisGroupIdx, ++runningIndex,
				    false));
			}

			runningIndex = createIndexedTreeNodeListOfResourceGroupsAndUserGroups(
			    subStringHighlight, ugForm.getChildGroups(), result, thisGroupIdx,
			    runningIndex);
		}

		return runningIndex;
	}

	private ResourceGroupForm findRGForm(RequestHandler rh, LoginToken token,
	    List<ResourceGroupForm> treeResult, String groupName,
	    List<ResourceGroupProfile> resourceGroupProfilesList,
	    List<EndpointResourceUiType> masterEndpointList) throws Exception {
		ResourceGroupForm rgForm = null;

		for (ResourceGroupForm form : treeResult) {
			if (form.getName().equals(groupName)) {
				rgForm = form;
				break;
			}
		}

		if (rgForm == null) {
			rgForm = new ResourceGroupForm();
			rgForm.setName(groupName);
			rgForm.setWebSafeName(groupName);

			// Retrieve the ugProfile for this group
			for (ResourceGroupProfile rgProfile : resourceGroupProfilesList) {
				String rgName = rgProfile.getName();
				if (rgName.equals(groupName)) {
					ResourceGroupHelper.updateMembership(rgProfile, rgForm,
					    masterEndpointList, true);
				}
			}

			treeResult.add(rgForm);
		}

		return rgForm;
	}

	private void mergeLineageListToResults(RequestHandler rh, LoginToken token,
	    List<ResourceGroupForm> treeResult, List<String> groupNameLineage,
	    List<ResourceGroupProfile> resourceGroupProfilesList,
	    List<EndpointResourceUiType> masterEndpointList) throws Exception {
		if (groupNameLineage.size() > 0) {
			String groupName = groupNameLineage.remove(0);
			ResourceGroupForm form = findRGForm(rh, token, treeResult, groupName,
			    resourceGroupProfilesList, masterEndpointList);
			List<ResourceGroupForm> childGroups = form.getChildGroups();
			mergeLineageListToResults(rh, token, childGroups, groupNameLineage,
			    resourceGroupProfilesList, masterEndpointList);
		}

		// Sort all peers
		Collections.sort(treeResult);
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
