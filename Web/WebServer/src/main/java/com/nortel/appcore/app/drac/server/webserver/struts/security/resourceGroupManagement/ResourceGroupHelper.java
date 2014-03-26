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

package com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.types.AbstractRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRuleList;
import com.nortel.appcore.app.drac.common.security.policy.types.BandwidthControlRule;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourcePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.security.SecurityHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.security.form.RuleForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form.CreateResourceGroupForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form.ResourceForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form.ResourceGroupForm;

/**
 * @author Colin Hart
 */
public final class ResourceGroupHelper {
  
  private static final Logger log = LoggerFactory.getLogger(ResourceGroupHelper.class);

	public static void copyProperties(Locale locale, LoginToken token,
	    ResourceGroupForm src, String resGroupName,
	    ResourceGroupProfile aRGProfile, List<String> memberTNAs,
	    List<EndpointResourceUiType> masterEndpointList) throws Exception {
		log.debug("ResourceGroupHelper.copyProperties() called...");

		RequestHandler rh = RequestHandler.INSTANCE;

		List<Resource> myResources = new ArrayList<Resource>();
		List<EndpointResourceUiType> memberEndpointResources = EndpointResourceUiType
		    .selectFrom(memberTNAs, masterEndpointList);
		for (EndpointResourceUiType memberEndpointResource : memberEndpointResources) {
			myResources.add(new Resource(memberEndpointResource.getResourceId()));
		}

		try {
			rh.setResourceGroupResourceList(token, resGroupName, myResources);
		}
		catch (Exception e) {
			log.error(
			    "Could not update resource group resource list " + resGroupName, e);
			throw e;
		}

		try {
			aRGProfile = rh.getResourceGroupProfile(token, resGroupName);
			MembershipData oldMembership = aRGProfile.getMembership();

			MembershipData newMembership = new MembershipData(
			    oldMembership.getCreatedByGroupName(),
			    oldMembership.getMemberUserID(),
			    stringArrayToSet(src.getUserGroupMembership()),
			    oldMembership.getMemberResourceGroupName());

			log.debug("Update membership data for: " + resGroupName);
			log.debug(newMembership.toXMLString());

			rh.setResourceGroupMembership(token, resGroupName, newMembership);
		}
		catch (Exception e) {
			log.error("Could not update resource group user group list "
			    + resGroupName, e);
			throw e;
		}

		ResourcePolicy resourcePolicy = aRGProfile.getResourcePolicy();

		// update the time-based (system access) rules
		String[] systemAccessRules = src.getResourceSystemAccessRules();
		List<AbstractRule> accessRules = new ArrayList<AbstractRule>();
		AbstractRule r = null;

		if (systemAccessRules != null) {
			log.debug("systemAccessRules.length is: " + systemAccessRules.length);
			for (int i = 0; i < systemAccessRules.length; i++) {
				log.debug("systemAccessRule at " + i + " is: " + systemAccessRules[i]);
				r = SecurityHelper.parseSystemAccessRule(locale, systemAccessRules[i]);

				if (r != null) {
					log.debug("adding rule: " + r.toXMLString());
					accessRules.add(r);
				}
			}
		}
		resourcePolicy.setAccessRule(accessRules);

		// update the bandwidth control rules
		BandwidthControlRule bwr = (BandwidthControlRule) resourcePolicy
		    .getBandwidthControlRule();

		if (bwr != null) {
			if (bwr.getMaximumServiceSize() != null) {
				if (!bwr.getMaximumServiceSize().toString()
				    .equals(src.getMaximumServiceSize())) {
					if (src.getMaximumServiceSize().equals("")) {
						bwr.setMaximumServiceSize(null);
					}
					else {
						bwr.setMaximumServiceSize(Integer.valueOf(src
						    .getMaximumServiceSize()));
					}
				}
			}
			else {
				if (src.getMaximumServiceSize() != null
				    && src.getMaximumServiceSize() != "") {
					bwr.setMaximumServiceSize(Integer.valueOf(src.getMaximumServiceSize()));
				}
			}

			if (bwr.getMaximumServiceDuration() != null) {
				if (!bwr.getMaximumServiceDuration().toString()
				    .equals(src.getMaximumServiceDuration())) {
					if (src.getMaximumServiceDuration().equals("")) {
						bwr.setMaximumServiceDuration(null);
					}
					else {
						bwr.setMaximumServiceDuration(Integer.valueOf(src
						    .getMaximumServiceDuration()));
					}
				}
			}
			else {
				if (src.getMaximumServiceDuration() != null
				    && src.getMaximumServiceDuration() != "") {
					bwr.setMaximumServiceDuration(Integer.valueOf(src
					    .getMaximumServiceDuration()));
				}
			}

			if (bwr.getMaximumServiceBandwidth() != null) {
				if (!bwr.getMaximumServiceBandwidth().toString()
				    .equals(src.getMaximumServiceBandwidth())) {
					if (src.getMaximumServiceBandwidth().equals("")) {
						bwr.setMaximumServiceBandwidth(null);
					}
					else {
						bwr.setMaximumServiceBandwidth(Integer.valueOf(src
						    .getMaximumServiceBandwidth()));
					}
				}
			}
			else {
				if (src.getMaximumServiceBandwidth() != null
				    && src.getMaximumServiceBandwidth() != "") {
					bwr.setMaximumServiceBandwidth(Integer.valueOf(src
					    .getMaximumServiceBandwidth()));
				}
			}

			if (bwr.getMaximumAggregateServiceSize() != null) {
				if (!bwr.getMaximumAggregateServiceSize().toString()
				    .equals(src.getMaximumAggregateServiceSize())) {
					if (src.getMaximumAggregateServiceSize().equals("")) {
						bwr.setMaximumAggregateServiceSize(null);
					}
					else {
						bwr.setMaximumAggregateServiceSize(Integer.valueOf(src
						    .getMaximumAggregateServiceSize()));
					}
				}
			}
			else {
				if (src.getMaximumAggregateServiceSize() != null
				    && src.getMaximumAggregateServiceSize() != "") {
					bwr.setMaximumAggregateServiceSize(Integer.valueOf(src
					    .getMaximumAggregateServiceSize()));
				}
			}

			try {
				resourcePolicy.setBandwidthControlRule(bwr);
				// later maybe rh.setResourceGroupRule(userDetails, resGroupName, bwr);
				rh.setResourceGroupPolicy(token, resGroupName, resourcePolicy);
			}
			catch (Exception e) {
				log.error("Could not update resource group bandwidth rule "
				    + resGroupName, e);
				throw e;
			}
		}
	}


	public static synchronized void copyProperties(Resource src, ResourceForm dest) {
		ArrayList<String> referencingResourceGroupNames = new ArrayList<String>();
		List<String> referencingRGNList = src.getReferencingResourceGroupName();
		for (String referencingResourceGroupName : referencingRGNList) {
			referencingResourceGroupNames.add(referencingResourceGroupName);
		}

		if (src != null && dest != null) {
			dest.setResourceID(src.getResourceID());
			dest.setResourceType(src.getResourceType());
			dest.setReferencingResourceGroupNames(referencingResourceGroupNames);
		}

	}

	public static String create(Locale myLocale, LoginToken token,
	    CreateResourceGroupForm form, List<String> memberTNAs,
	    List<EndpointResourceUiType> masterEndpointList) throws Exception {
		RequestHandler myRequestHandler = RequestHandler.INSTANCE;

		String name = "";
		String lastModificationUserID = token.getUser();
		boolean defaultRG = true;
		String parentResourceGroup = "";
		ResourcePolicy policy = new ResourcePolicy();
		String[] refUserGroupNames;

		// Retrieving values from CreateResourceGroupForm Java bean.
		name = form.getName().trim();
		defaultRG = form.isDefaultResourceGroup();
		parentResourceGroup = form.getParentResourceGroup();

		// Resources:
		List<Resource> myResources = new ArrayList<Resource>();
		List<EndpointResourceUiType> memberEndpointResources = EndpointResourceUiType
		    .selectFrom(memberTNAs, masterEndpointList);
		for (EndpointResourceUiType memberEndpointResource : memberEndpointResources) {
			myResources.add(new Resource(memberEndpointResource.getResourceId()));
		}

		refUserGroupNames = form.getReferencedUserGroups();
		TreeSet<String> refUserGroupNameList = new TreeSet<String>(
		    Arrays.asList(refUserGroupNames));

		// ResourcePolicy fields.
		// parse access state
		int state = form.getResourceAccessState();
		policy.setStateRule(CreateResourceGroupForm.states[state]);

		// parse time-based (system access) rules
		String[] systemAccessRules = form.getResourceSystemAccessRules();
		List<AbstractRule> accessRules = new AccessRuleList();
		AbstractRule r = null;
		if (systemAccessRules != null) {
			for (String systemAccessRule : systemAccessRules) {
				r = SecurityHelper.parseSystemAccessRule(myLocale, systemAccessRule);
				if (r != null) {
					accessRules.add(r);
				}
			}
		}
		policy.setAccessRule(accessRules);

		// parse bandwidth control rules
		BandwidthControlRule bwRule = new BandwidthControlRule();
		try {
			bwRule
			    .setMaximumServiceSize(new Integer(form.getResourceMaxServiceSize()));
		}
		catch (NumberFormatException nfe) {
			bwRule.setMaximumServiceSize(null);
		}
		try {
			bwRule.setMaximumServiceBandwidth(new Integer(form
			    .getResourceMaxServiceBandwidth()));
		}
		catch (NumberFormatException nfe) {
			bwRule.setMaximumServiceBandwidth(null);
		}
		try {
			bwRule.setMaximumServiceDuration(new Integer(form
			    .getResourceMaxServiceDuration()));
		}
		catch (NumberFormatException nfe) {
			bwRule.setMaximumServiceDuration(null);
		}
		try {
			bwRule.setMaximumAggregateServiceSize(new Integer(form
			    .getResourceMaxAggregateServiceSize()));
		}
		catch (NumberFormatException nfe) {
			bwRule.setMaximumAggregateServiceSize(null);
		}
		policy.setBandwidthControlRule(bwRule);

		// Instantiating ResourceGroupProfile object.
		ResourceGroupProfile aResourceGroupProfile = new ResourceGroupProfile(name,
		    Calendar.getInstance(), Calendar.getInstance(), lastModificationUserID,
		    parentResourceGroup);

		// Assigning values to ResourceGroupProfile object.
		aResourceGroupProfile.setDefaultResourceGroup(Boolean.valueOf(defaultRG));
		aResourceGroupProfile.setResourceList(myResources);
		aResourceGroupProfile.setResourcePolicy(policy);
		Set<UserGroupName> ugSet = new TreeSet<UserGroupName>();
		for (String s : refUserGroupNameList) {
			ugSet.add(new UserGroupName(s));
		}
		aResourceGroupProfile.getMembership().setMemberUserGroupName(ugSet);

		// Calling CreateResourceGroup function.
		log.debug("Calling CreateResourceGroup");

		myRequestHandler.createResourceGroupProfile(token, aResourceGroupProfile);

		log.debug("Generated resource group:" + name);

		return name;
	}

	public static synchronized void updateMembership(ResourceGroupProfile src,
	    ResourceGroupForm dest,
	    List<EndpointResourceUiType> masterEndpointResourceList,
	    boolean appendLabel) throws Exception {
		if (src != null && dest != null) {
			// Membership Data
			MembershipData myMembershipData = src.getMembership();

			Set<String> referencingUGNList = new TreeSet<String>();
			for (UserGroupName n : myMembershipData.getMemberUserGroupName()) {
				referencingUGNList.add(n.toString());
			}
			dest.setUserGroupMembership(referencingUGNList
			    .toArray(new String[referencingUGNList.size()]));
			dest.setParentResourceGroup(src.getMembership().getCreatedByGroupName());

			// Update BOTH the form's available AND member TNA lists...
			List<Resource> resourceList = src.getResourceList();
			List<String> resourceIdList = new ArrayList<String>();
			for (Resource resource : resourceList) {
				resourceIdList.add(resource.getResourceID());
			}

			Map<String, List<EndpointResourceUiType>> lists = EndpointResourceUiType
			    .splitOn(resourceIdList, masterEndpointResourceList);
			dest.setAvailableTNAs(EndpointResourceUiType.toTnaStringArray(lists
			    .get(EndpointResourceUiType.REMAINING)));
			dest.setMemberTNAs(EndpointResourceUiType.toTnaStringArray(
			    lists.get(EndpointResourceUiType.SUBSET), appendLabel));

		}
	}

	public static synchronized void updateResourceGroupForm(Locale myLocale,
	    TimeZone tz, ResourceGroupProfile src, ResourceGroupForm dest,
	    List<EndpointResourceUiType> masterEndpointResourceList) throws Exception {

		if (src != null && dest != null) {
			ResourcePolicy resourcePolicy = src.getResourcePolicy();

			if (resourcePolicy != null) {
				AbstractRule stateRule = resourcePolicy.getStateRule();
				List<AbstractRule> rules = resourcePolicy.getAccessRule();
				if (rules != null) {
					RuleForm[] ruleBeans = new RuleForm[rules.size()];
					String[] ruleStr = new String[rules.size()];
					for (int i = 0; i < rules.size(); i++) {
						ruleStr[i] = SecurityHelper.convertAccessRule2(myLocale,
						    (AccessRule) rules.get(i));
						ruleBeans[i] = SecurityHelper.convertAccessRule(myLocale,
						    (AccessRule) rules.get(i));
					}
					dest.setResourceSystemAccessRules(ruleStr);
					dest.setResRules(ruleBeans);
				}

				BandwidthControlRule bwr = (BandwidthControlRule) resourcePolicy
				    .getBandwidthControlRule();
				if (bwr != null) {
					if (bwr.getMaximumServiceSize() != null) {
						dest.setMaximumServiceSize(bwr.getMaximumServiceSize().toString());
					}
					if (bwr.getMaximumServiceDuration() != null) {
						dest.setMaximumServiceDuration(bwr.getMaximumServiceDuration()
						    .toString());
					}
					if (bwr.getMaximumServiceBandwidth() != null) {
						dest.setMaximumServiceBandwidth(bwr.getMaximumServiceBandwidth()
						    .toString());
					}
					if (bwr.getMaximumAggregateServiceSize() != null) {
						dest.setMaximumAggregateServiceSize(bwr
						    .getMaximumAggregateServiceSize().toString());
					}
				}

				String dateString = "";
				String timeString = "";

				dest.setName(src.getName());
				dest.setWebSafeName(src.getName());
				dateString = DateFormatter.guiDateToString(src.getCreationDate(),
				    myLocale, tz);
				dest.setCreationDate(dateString);
				timeString = DateFormatter.guiTimeToString(src.getCreationDate(),
				    myLocale, tz);
				dest.setCreationTime(timeString);
				dateString = DateFormatter.guiDateToString(src.getLastModifiedDate(),
				    myLocale, tz);
				dest.setLastModifiedDate(dateString);
				timeString = DateFormatter.guiTimeToString(src.getLastModifiedDate(),
				    myLocale, tz);
				dest.setLastModifiedTime(timeString);
				dest.setLastModificationUserID(src.getLastModificationUserID());
				dest.setDefaultResourceGroup(src.getDefaultResourceGroup());
				dest.setStateRule(stateRule);

				updateMembership(src, dest, masterEndpointResourceList, false);
			}
		}
		log.debug("...ResourceGroupHelper.updateResourceGroupForm() ends.");
	}

	private static TreeSet<UserGroupName> stringArrayToSet(String[] array) {
		TreeSet<UserGroupName> list = new TreeSet<UserGroupName>();
		if (array != null) {
			for (String element : array) {
				list.add(new UserGroupName(element));
			}
		}
		return list;
	}

	public String toString(ResourceGroupForm form) {
		Method[] methods = ResourceGroupForm.class.getDeclaredMethods();
		StringBuilder buf = new StringBuilder();
		Method method;
		for (Method method2 : methods) {
			method = method2;
			if (method.getName().startsWith("get")) {
				try {
					buf.append(method.getName() + ": "
					    + method.invoke(form, new Object[0]) + "\n");
				}
				catch (InvocationTargetException ite) {
					
				}
				catch (IllegalAccessException iae) {
					
				}
			}
		}
		return buf.toString();
	}
}
