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

package com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.types.AbstractRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessControlRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRuleList;
import com.nortel.appcore.app.drac.common.security.policy.types.BandwidthControlRule;
import com.nortel.appcore.app.drac.common.security.policy.types.GroupPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.security.SecurityHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.security.form.RuleForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement.form.CreateUserGroupForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement.form.UserGroupForm;

/**
 * @author Colin Hart
 */
public final class UserGroupHelper {
  
  private static final Logger log = LoggerFactory.getLogger(UserGroupHelper.class);

	public static void copyProperties(Locale locale, UserGroupForm src,
	    GroupPolicy dest) {

		log.debug("UserGroupHelper.copyProperties called...");

		// update the time-based (system access) rules
		String[] systemAccessRules = src.getGroupSystemAccessRules();
		List<AbstractRule> accessRules = new ArrayList<AbstractRule>();
		AbstractRule r = null;

		if (systemAccessRules != null) {
			for (String systemAccessRule : systemAccessRules) {
				r = SecurityHelper.parseSystemAccessRule(locale, systemAccessRule);

				if (r != null) {
					accessRules.add(r);
				}
			}
		}
		dest.setSystemAccessRule(accessRules);

		// update the bandwidth control rules
		BandwidthControlRule bwr = (BandwidthControlRule) dest
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

			dest.setBandwidthControlRule(bwr);

		}
	}

	public static UserGroupName create(LoginToken token, Locale locale,
	    CreateUserGroupForm form) throws Exception {

		RequestHandler myRequestHandler = RequestHandler.INSTANCE;

		// Variables taking values from the Create page.

		String lastModificationUserID = token.getUser();
		int userGroupType = 0;
		boolean defaultUserGroup = true;

		// later String aReferencingUserGroupName = "";

		// Retrieving values from CreateUserGroupForm Java bean.
		UserGroupName name = new UserGroupName(form.getName().trim());
		defaultUserGroup = form.isDefaultUserGroup();
		String parentUserGroup = form.getParentUserGroup();
		userGroupType = form.getUserGroupType();

		// later aReferencingUserGroupName = form.getReferencingUserGroupName();

		// Instantiating UserGroupProfile object.
		UserGroupProfile aUserGroupProfile = new UserGroupProfile(name,
		    Calendar.getInstance(), Calendar.getInstance(), lastModificationUserID,
		    new UserGroupName(parentUserGroup));
		aUserGroupProfile.setReferencingUserGroupName(name);

		UserGroupType ugt = UserGroupProfileXML.UserGroupType.USER;
		try {
			ugt = UserGroupProfileXML.UserGroupType.values()[userGroupType];
		}
		catch (ArrayIndexOutOfBoundsException aoobe) {
			log.warn("UserGroupHelper.create() - invalid user group type", aoobe);
		}
		aUserGroupProfile.setUserGroupType(ugt);

		aUserGroupProfile.setDefaultUserGroup(Boolean.valueOf(defaultUserGroup));

		String[] users = form.getUserMembership();
		TreeSet<String> userList = new TreeSet<String>();
		for (String user : users) {
			userList.add(user);
		}
		/*
		 * junk String[] userGroups = form.getUserGroupMembership(); List<String>
		 * userGroupList = new ArrayList<String>(); for (int i=0;
		 * i<userGroups.length; i++) { userGroupList.add(userGroups[i]); }
		 */
		String[] resourceGroups = form.getResourceGroupMembership();
		TreeSet<String> resourceGroupList = new TreeSet<String>();
		for (String resourceGroup : resourceGroups) {
			resourceGroupList.add(resourceGroup);
		}

		MembershipData membershipData = new MembershipData(parentUserGroup,
		    userList, null, resourceGroupList);
		aUserGroupProfile.setMembership(membershipData);

		// policy
		GroupPolicy aGroupPolicy = UserGroupHelper.createGroupPolicy(locale, form);
		aUserGroupProfile.setGroupPolicy(aGroupPolicy);

		// later
		// aUserGroupProfile.setReferencingUserGroupName(aReferencingUserGroupName);

		// Calling CreateResourceGroup function.
		

		myRequestHandler.createUserGroupProfile(token, aUserGroupProfile);

		log.debug("Generated user group:" + name);

		return name;
	}

	public static GroupPolicy createGroupPolicy(Locale locale,
	    CreateUserGroupForm gpForm) {
		GroupPolicy policy = new GroupPolicy();

		if (gpForm != null) {
			// parse access control rules
			String[] accessControlRules = gpForm.getGroupAccessControlRules();
			List<AbstractRule> controlRules = new ArrayList<AbstractRule>();
			if (accessControlRules != null) {
				for (String accessControlRule : accessControlRules) {
					log.debug(accessControlRule);
					StringTokenizer st = new StringTokenizer(accessControlRule, "=");
					if (st.countTokens() == 2) {
						String key = st.nextToken();
						String value = st.nextToken();
						controlRules.add(new AccessControlRule(key, value));
					}
				}
			}
			policy.setAccessControlRule(controlRules);

			// parse time-based (system access) rules
			String[] systemAccessRules = gpForm.getGroupSystemAccessRules();
			List<AbstractRule> accessRules = new AccessRuleList();
			AbstractRule r = null;
			if (systemAccessRules != null) {
				for (String systemAccessRule : systemAccessRules) {
					r = SecurityHelper.parseSystemAccessRule(locale, systemAccessRule);
					if (r != null) {
						accessRules.add(r);
					}
				}
			}
			policy.setSystemAccessRule(accessRules);

			// parse bandwidth control rules
			BandwidthControlRule bwRule = new BandwidthControlRule();
			try {
				bwRule.setMaximumServiceSize(new Integer(gpForm
				    .getGroupMaxServiceSize()));
			}
			catch (NumberFormatException nfe) {
				bwRule.setMaximumServiceSize(null);
			}
			try {
				bwRule.setMaximumServiceBandwidth(new Integer(gpForm
				    .getGroupMaxServiceBandwidth()));
			}
			catch (NumberFormatException nfe) {
				bwRule.setMaximumServiceBandwidth(null);
			}
			try {
				bwRule.setMaximumServiceDuration(new Integer(gpForm
				    .getGroupMaxServiceDuration()));
			}
			catch (NumberFormatException nfe) {
				bwRule.setMaximumServiceDuration(null);
			}
			try {
				bwRule.setMaximumAggregateServiceSize(new Integer(gpForm
				    .getGroupMaxAggregateServiceSize()));
			}
			catch (NumberFormatException nfe) {
				bwRule.setMaximumAggregateServiceSize(null);
			}
			policy.setBandwidthControlRule(bwRule);
		}
		return policy;
	}


	public static synchronized void updateMembership(UserGroupProfile src,
	    UserGroupForm dest) {
		// Membership data
		MembershipData membershipData = src.getMembership();
		if (membershipData != null) {
			dest.setParentUserGroup(membershipData.getCreatedByGroupName());

			dest.setUserMembership(membershipData.getMemberUserID().toArray(
			    new String[membershipData.getMemberUserID().size()]));

			dest.setResourceGroupMembership(membershipData
			    .getMemberResourceGroupName().toArray(
			        new String[membershipData.getMemberResourceGroupName().size()]));
		}
	}

	public static synchronized void updateUserGroupForm(Locale myLocale,
	    TimeZone tz, UserGroupProfile src, UserGroupForm dest) throws Exception {
		/* Create base logging string for this page. */
		String logEntry = "updateUserGroupForm:";

		String dateString = "";
		String timeString = "";

		/* Entry into page. */
		log.debug(logEntry
		    + "operation=use UserGroupProfile to update the UserGroupForm...");

		if (src != null && dest != null) {
			log.debug(logEntry + "updating the UserGroupForm...");

			dest.setName(src.getName().toString());
			dest.setWebSafeName(src.getName().toString());

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
			dest.setUserGroupType(src.getUserGroupType());
			dest.setDefaultUserGroup(src.getDefaultUserGroup());
			dest.setReferencingUserGroupName(src.getReferencingUserGroupName()
			    .toString());

			// Membership data
			updateMembership(src, dest);

			GroupPolicy policy = src.getGroupPolicy();
			if (policy != null) {
				log.debug(logEntry + "updating the group policy info...");
				List<AbstractRule> rules = policy.getAccessControlRule();
				if (rules != null) {
					String[] ruleStr = new String[rules.size()];
					RuleForm[] controlRules = new RuleForm[rules.size()];
					for (int i = 0; i < rules.size(); i++) {
						ruleStr[i] = SecurityHelper
						    .convertAccessControlRule((AccessControlRule) rules.get(i));
						controlRules[i] = SecurityHelper
						    .convertAccessControlRule2((AccessControlRule) rules.get(i));
					}
					dest.setAccessControlRules(controlRules);
					dest.setGroupAccessControlRules(ruleStr);
				}

				rules = policy.getAccessRule();
				if (rules != null) {
					String[] ruleStr = new String[rules.size()];
					RuleForm[] ruleBeans = new RuleForm[rules.size()];
					for (int i = 0; i < rules.size(); i++) {
						ruleStr[i] = SecurityHelper.convertAccessRule2(myLocale,
						    (AccessRule) rules.get(i));
						ruleBeans[i] = SecurityHelper.convertAccessRule(myLocale,
						    (AccessRule) rules.get(i));
					}
					dest.setGroupSystemAccessRules(ruleStr);
					dest.setGroupRules(ruleBeans);
				}

				BandwidthControlRule bwr = (BandwidthControlRule) policy
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
			}
		}
		log.debug(logEntry
		    + "operation=use UserGroupProfile to update the UserGroupForm... DONE.");
	}

	public String toString(UserGroupForm form) {
		Method[] methods = UserGroupForm.class.getDeclaredMethods();
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
					log.error("Error: ", ite);
				}
				catch (IllegalAccessException iae) {
					log.error("Error: ", iae);
				}
			}
		}
		return buf.toString();
	}

}
