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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.network.EndpointHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.network.form.EndpointForm;

/**
 * Created on 26-Jul-06
 */
public final class EndpointMappingDispatchAction extends
    DracBaseMappingDispatchAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	public ActionForward editEndpoint(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		EndpointForm epForm = (EndpointForm) form;
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);

		/* Create base logging string for this page. */
		String logEntry = "editEndpoint: userID=" + activeUserID + ":ipAddress="
		    + request.getRemoteAddr() + ":protocol=" + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=edit");

		String mtu = epForm.getNewMtu();
		String endPointId = epForm.getId();
		String name = epForm.getName();

		LoginToken token = (LoginToken) request.getSession().getAttribute(
		    DracConstants.TOKEN_OBJ);
		if (endPointId != null && !DracConstants.EMPTY_STRING.equals(endPointId)) {
			request.setAttribute("id", endPointId);
			if (DracConstants.EMPTY_STRING.equals(mtu)
			    || DracConstants.EMPTY_STRING.equals(endPointId)) {
				request.setAttribute("failed", name);
				log.debug("Attempt to edit endpoint with blank values: id=" + endPointId
				    + " mtu=" + mtu);
				ActionMessages messages = new ActionMessages();
				ActionMessage message = new ActionMessage(
				    "drac.network.edit.endpointnotfound", endPointId);
				messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				saveMessages(request, messages);

			}
			else {
				EndpointHelper helper = new EndpointHelper();
				EndPointType end = RequestHandler.INSTANCE.findEndpointByTna(
				    token, name);
				if (end != null) {

					/** * Calling function to edit the selected endpoint ** */
					try {
						helper.editEndpoint(token, end, mtu);
						log.debug("End Point has been edited successfully");
						request.setAttribute("status", name);
					}
					catch (NrbException ne) {
						request.setAttribute("failed", name);
						String msg = ne.getMessage().toString();
						ActionMessages messages = new ActionMessages();
						ActionMessage message = new ActionMessage(msg, false);
						messages.add(ActionMessages.GLOBAL_MESSAGE, message);
						saveMessages(request, messages);
					}
				}
				else {
					request.setAttribute("failed", name);
					ActionMessages messages = new ActionMessages();
					ActionMessage message = new ActionMessage(
					    "drac.network.edit.endpointnotfound", endPointId);
					messages.add(ActionMessages.GLOBAL_MESSAGE, message);
					saveMessages(request, messages);
				}
			}
			return mapping.findForward(DracConstants.EDIT_ENDPOINT_RESULT_PAGE);
		}
		// lost request parameters through authenticate redirect
		return mapping.findForward(DracConstants.LIST_ENDPOINTS_PAGE);
	}

	public ActionForward forwardEditEndpointPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "editEndpoint: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=invoke;");
		return mapping.findForward(DracConstants.EDIT_ENDPOINT_PAGE);
	}

	public ActionForward forwardListEndpointsPage(ActionMapping mapping,
	    ActionForm form, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		/* Create base logging string for this page. */
		HttpSession session = request.getSession();
		String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);
		String sessionID = session.getId();
		String logEntry = "listEndpoints: userID=" + activeUserID + ":sessionID="
		    + sessionID + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
		    + request.getProtocol() + ":";

		/* Entry into page. */
		log.debug(logEntry + "operation=invoke;");
		LoginToken token = (LoginToken) request.getSession().getAttribute(
		    DracConstants.TOKEN_OBJ);
		List<UserGroupName> groups = RequestHandler.INSTANCE
		    .getUserGroupProfileNames(token);
		if (groups.isEmpty()) {
			log.warn("No user groups for " + token.getUser());
		}

		((EndpointForm) form).setGroupList(groups);
		return mapping.findForward(DracConstants.LIST_ENDPOINTS_PAGE);
	}

	// no instance variables allowed, struts not thread-safe
	public ActionForward listEndpoints(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		try {
			/* Create base logging string for this page. */
			String logEntry = "listEndpoints: userID="
			    + request.getSession().getAttribute(DracConstants.USER_ID)
			    + ":ipAddress=" + request.getRemoteAddr() + ":protocol="
			    + request.getProtocol() + ":";

			/* Entry into page. */
			log.debug(logEntry + "operation=list");

			EndpointForm epForm = (EndpointForm) form;

			/* Local objects used in building endpoint query. */

			String group = epForm.getGroup();
			String layer = epForm.getLayerFilter();

			if (layer != null && !layer.equals(DracConstants.EMPTY_STRING)) {
				log.debug(logEntry + "dbg: getting session ");
				UserDetails userDetails = (UserDetails) request.getSession()
				    .getAttribute(DracConstants.AUTH_OBJ);
				LoginToken token = (LoginToken) request.getSession().getAttribute(
				    DracConstants.TOKEN_OBJ);
				// get a list of groups this user can access
				log.debug(logEntry + "dbg: getting user Groups ");
				List<UserGroupProfile> userGroupList = RequestHandler.INSTANCE
				    .getUserGroups(token);

				// convert the list into just names
				Set<UserGroupName> treeSet = new TreeSet<UserGroupName>();
				for (UserGroupProfile userGroup : userGroupList) {
					treeSet.add(userGroup.getName());
				}
				List<UserGroupName> result = new ArrayList<UserGroupName>(treeSet);
				treeSet = null;

				/* Submit the endpoint query . */

				List<UserGroupName> filterGroups = new ArrayList<UserGroupName>();
				if (DracConstants.ALL_GROUPS.equals(group)) {
					if (userDetails.getUserPolicyProfile() != null) {
						// leave filterGroups empty if system-admin user, can see everything
						if (userDetails.getUserPolicyProfile().getUserGroupType() != UserGroupType.SYSTEM_ADMIN) {
							filterGroups.addAll(result);
						}
					}
					else {
						log.error(logEntry + " User policy profile is null for "
						    + userDetails.getUserID());
					}
				}
				else {
					filterGroups.add(new UserGroupName(group));
				}
				log.debug(logEntry + " fetching end points ");
				List<EndPointType> endpointList = RequestHandler.INSTANCE
				    .getEndpoints(token, layer, filterGroups);
				log.debug(logEntry + " got " + endpointList.size() + " endpoints ");

				// get a map of the endpoints to which user group they belong to
				Map<String, Set<UserGroupName>> endpointsToGroupMap = EndpointHelper
				    .mapEndpointsToUserGroups(token, endpointList, userGroupList);
				TreeSet<EndpointForm> set = new TreeSet<EndpointForm>();
				EndPointType endpoint = null;
				EndpointForm endptForm = null;
				for (int j = 0; j < endpointList.size(); j++) {
					endpoint = endpointList.get(j);
					if (endpoint != null) {
						endptForm = new EndpointForm();
						EndpointHelper.copyProperties(endpoint, endptForm,
						    endpointsToGroupMap);
						endptForm.setGroup(group);
						endptForm.setLayerFilter(layer);
						set.add(endptForm);
					}
				}
				request.setAttribute("layerFilter", layer);
				request.setAttribute("endPointList", new ArrayList<EndpointForm>(set));
				request.setAttribute("groupFilter", group);

				log.debug(logEntry + " forwarding to results page with " + set.size()
				    + " results ");
				return mapping.findForward(DracConstants.LIST_ENDPOINTS_RESULT_PAGE);
			}

			log.debug(logEntry + " lost parameters, redirecting to authenticate!");
			// lost request parameters through authenticate redirect
			return mapping.findForward(DracConstants.LIST_ENDPOINTS_PAGE);
		}
		catch (Exception t) {
			log.error("listEndpoints", t);
			throw new Exception(t);
		}
	}
}
