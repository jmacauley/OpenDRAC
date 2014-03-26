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

package com.nortel.appcore.app.drac.server.webserver.struts.network;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.network.form.EndpointForm;
import com.nortel.appcore.app.drac.server.webserver.struts.network.form.UtilListForm;

/**
 * Created on 26-Jul-06
 */
public final class EndpointHelper {
  
  private static final Logger log = LoggerFactory.getLogger(EndpointHelper.class);

	public static synchronized void copyProperties(EndPointType endPoint,
	    EndpointForm form, Map<String, Set<UserGroupName>> endpointsToGroupMap) {
		if (endPoint != null) {
			// Process common stuff
			form.setState(endPoint.getState());
			form.setSignalingType(endPoint.getSignalingType());
			form.setId(endPoint.getId());

			// Convert (String,AttributeType) map to a (String,String) map
			/* TODO: Temporary fix to missing attributes. */
			Map<String, String> formAttributes = endPoint.getAttributes();

			formAttributes.put("endpointID", endPoint.getId());
			form.setAttributes(formAttributes);
			// form.setName((String) attributes.get(FacilityConstants.TNA_ATTR));
			form.setName(formAttributes.get(FacilityConstants.TNA_ATTR));
			form.setLabel(formAttributes.get(FacilityConstants.FACLABEL_ATTR));

			if (endpointsToGroupMap != null) {
				Set<UserGroupName> groups = endpointsToGroupMap.get(endPoint.getId());
				if (groups != null) {
					form.getGroupList().addAll(groups);
				}
			}

			/* Parse out the port type. */
			int start;
			int end = 0;
			String portType = "";
			start = endPoint.getId().indexOf('_') + 1;
			end = endPoint.getId().indexOf('-', start);
			try {
				portType = endPoint.getId().substring(start, end);
			}
			catch (IndexOutOfBoundsException idxEx) {
				log.error(
				    "EndpointHelper--Possible malformed endpoint ID: "
				        + endPoint.getId(), idxEx);
			}

			String layer = endPoint.getLayer();
			form.setLayer(layer);
			if (Layer.LAYER1.toString().equals(layer)) {
				if (portType.equalsIgnoreCase("OC192")) {
					portType = "STM64";
				}
				else if (portType.equalsIgnoreCase("OC48")) {
					portType = "STM16";
				}
				else if (portType.equalsIgnoreCase("OC24")) {
					portType = "STM8";
				}
				else if (portType.equalsIgnoreCase("OC12")) {
					portType = "STM4";
				}
				else if (portType.equalsIgnoreCase("OC6")) {
					portType = "STM2";
				}
				else if (portType.equalsIgnoreCase("OC3")) {
					portType = "STM1";
				}

				form.setCost(formAttributes.get(FacilityConstants.COST_ATTR));
				form.setMetric(formAttributes.get(FacilityConstants.METRIC_ATTR));
				form.setSrlg(formAttributes.get(FacilityConstants.SRLG_ATTR));
			}
			else if (Layer.LAYER2.toString().equals(layer)) {
				form.setDataRate(formAttributes.get(FacilityConstants.SPEED_ATTR));
				form.setMtu(formAttributes.get(FacilityConstants.MTU_ATTR));
				String physAddrAttr = formAttributes
				    .get(FacilityConstants.PHYSICALADDRESS_ATTR);
				if (physAddrAttr != null) {
					form.setPhysAddr(physAddrAttr);
				}
				else {
					form.setPhysAddr(DracConstants.PHYS_ADDR_UNKWN);
				}
				form.setVcat(formAttributes.get(FacilityConstants.VCAT_ATTR));
			}
			else if (Layer.LAYER0.toString().equals(layer)) {
				form.setWavelength(endPoint.getWavelength());
			}
			form.setPortType(portType);
		}
	}

	public static void copyProperties(Locale locale, TimeZone tz,
	    DracService src, UtilListForm dest) {
		if (src != null && dest != null) {
			dest.setServiceID(src.getId());
			dest.setStatus(src.getStatus().name());

			dest.setStartTimeMillis(src.getStartTime());
			dest.setEndTimeMillis(src.getEndTime());
			Date startDate = new Date(src.getStartTime());
			Date endDate = new Date(src.getEndTime());

			SimpleDateFormat dateFormatter = new SimpleDateFormat(
			    DracConstants.WEB_GUI_TIME2, locale);
			dateFormatter.setTimeZone(tz);
			dest.setStartDateForList(dateFormatter.format(startDate));
			dest.setEndDateForList(dateFormatter.format(endDate));

			if (!"".equals(src.getScheduleName())) {
				dest.setScheduleName(src.getScheduleName());
			}
			if (!"".equals(src.getScheduleId())) {
				dest.setScheduleId(src.getScheduleId());
			}
			dest.setRate(src.getRate());

		}
	}

	public static Map<String, Set<UserGroupName>> mapEndpointsToUserGroups(
	    LoginToken token, List<EndPointType> endpointList,
	    List<UserGroupProfile> userGroupList) throws Exception {
		HashMap<String, Set<UserGroupName>> endpointIdToGroupMap = new HashMap<String, Set<UserGroupName>>();
		HashMap<String, Set<UserGroupName>> endpointToGroupMap = new HashMap<String, Set<UserGroupName>>();
		RequestHandler rh = RequestHandler.INSTANCE;

		if (endpointList != null && !endpointList.isEmpty()) {
			// create a lookup table using the endpoint ID
			for (EndPointType endpoint : endpointList) {
				endpointIdToGroupMap
				    .put(endpoint.getId(), new HashSet<UserGroupName>());
			}

			for (UserGroupProfile aUserGroup : userGroupList) {
				// get a list of the user group's direct members
				Set<String> resGroups = aUserGroup.getMembership()
				    .getMemberResourceGroupName();
				for (String resGroupName : resGroups) {
					// get the resource group profile
					ResourceGroupProfile rgp = rh.getResourceGroupProfile(token,
					    resGroupName);
					List<Resource> resources = rgp.getResourceList();
					if (rgp.getDefaultResourceGroup()) {
						// add this group to all endpoints in the map
						for (EndPointType endpoint : endpointList) {
							endpointIdToGroupMap.get(endpoint.getId()).add(
							    aUserGroup.getName());
						}
						break;
					}

					/*
					 * for each endpoint in the resource group that matches our entry
					 * list, add the group name to the set
					 */
					for (Resource resource : resources) {
						Set<UserGroupName> groupSet = endpointIdToGroupMap.get(resource
						    .getResourceID());
						if (groupSet != null) {
							groupSet.add(aUserGroup.getName());
						}
					}
				}
			}

			// convert the lookup table from ID to TNA
			for (EndPointType endpoint : endpointList) {
				endpointToGroupMap.put(endpoint.getName(),
				    endpointIdToGroupMap.get(endpoint.getId()));
			}
		}

		return endpointIdToGroupMap;
	}

	public void editEndpoint(LoginToken token, EndPointType endpoint, String mtu)
	    throws Exception {
		endpoint.getAttributes().put(FacilityConstants.MTU_ATTR, mtu);
		RequestHandler.INSTANCE.editEndPoint(token, endpoint);
	}

}
