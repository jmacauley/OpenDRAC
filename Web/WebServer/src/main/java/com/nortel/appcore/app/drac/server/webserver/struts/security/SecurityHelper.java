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

package com.nortel.appcore.app.drac.server.webserver.struts.security;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.opendrac.security.policy.LoginAttemptsPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.PasswordEvaluator;
import com.nortel.appcore.app.drac.common.security.policy.types.AbstractRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessControlRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRule.TimeOfDayRange;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRuleList;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessStateRule;
import com.nortel.appcore.app.drac.common.security.policy.types.BandwidthControlRule;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalAuthentication;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalAuthentication.SupportedAuthenticationType;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.GroupPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.PasswordPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourcePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserAccountPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.AccessPermission;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationType;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.security.form.GlobalPolicyForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.form.RuleForm;

/**
 * Created on Oct 23, 2006
 */
public final class SecurityHelper {
  private static final Logger log = LoggerFactory.getLogger(SecurityHelper.class);
	private SecurityHelper() {
	}

	public static String convertAccessControlRule(AccessControlRule rule) {
		String rc = "";
		if (rule != null) {
			StringBuilder ruleString = new StringBuilder(rule.getKey());
			ruleString.append("=");
			List<String> values = rule.getValue();
			for (String v : values) {
				ruleString.append(v);
				ruleString.append(",");
			}
			rc = ruleString.substring(0, ruleString.length() - 1);
		}
		return rc;
	}

	public static RuleForm convertAccessControlRule2(AccessControlRule rule) {
		RuleForm ruleBean = new RuleForm();
		if (rule != null) {
			ruleBean.setKey(rule.getKey());
			List<String> values = rule.getValue();
			StringBuilder ruleString = new StringBuilder();
			String rc = "";
			if (values.size() > 0) {
				for (String v : values) {
					ruleString.append(v);
					ruleString.append(",");
				}
				rc = ruleString.substring(0, ruleString.length() - 1);
				ruleBean.setValue(rc);
			}
		}
		return ruleBean;
	}

	public static RuleForm convertAccessRule(Locale locale, AccessRule rule) {
		RuleForm ruleBean = new RuleForm();
		if (rule != null) {
			if (rule.getPermission().isGranted()) {
				ruleBean.setPermission(DracConstants.PERMISSION_GRANT_STATE);
			}
			else {
				ruleBean.setPermission(DracConstants.PERMISSION_DENY_STATE);
			}

			List<Integer> months = rule.getMonth();
			int[] monthArray = new int[months.size()];
			if (months != null && months.size() > 0) {
				for (int i = 0; i < months.size(); i++) {
					monthArray[i] = months.get(i).intValue();
				}
			}
			ruleBean.setMonths(monthArray);

			List<Integer> days = rule.getDayOfMonth();
			int[] dayArray = new int[days.size()];
			if (days != null && days.size() > 0) {
				for (int i = 0; i < days.size(); i++) {
					dayArray[i] = days.get(i).intValue();
				}
			}
			ruleBean.setDays(dayArray);

			List<Integer> daysOfWeek = rule.getDayOfWeek();
			int[] weekdayArray = new int[daysOfWeek.size()];
			if (daysOfWeek != null && daysOfWeek.size() > 0) {
				for (int i = 0; i < daysOfWeek.size(); i++) {
					weekdayArray[i] = daysOfWeek.get(i).intValue();
				}
			}
			ruleBean.setDayOfWeek(weekdayArray);

			List<TimeOfDayRange> timeOfDayRangeList = rule.getTimeOfDayRange();
			if (timeOfDayRangeList != null && timeOfDayRangeList.size() > 0) {
				TimeOfDayRange tdr = timeOfDayRangeList.get(0);
				SimpleDateFormat dateFormatter = (SimpleDateFormat) DateFormat
				    .getTimeInstance(DateFormat.SHORT, locale);
				ruleBean.setStartTime(dateFormatter.format(tdr.getStart().getTime()));
				ruleBean.setEndTime(dateFormatter.format(tdr.getEnd().getTime()));

			}

		}
		return ruleBean;
	}

	public static String convertAccessRule2(Locale locale, AccessRule rule) {
		String ruleString = "";
		if (rule != null) {
			String permission = rule.getPermission().toString();

			List<Integer> months = rule.getMonth();
			StringBuilder monthBuf = new StringBuilder("[");
			if (months != null && months.size() > 0) {
				for (Integer month : months) {
					monthBuf.append(month.intValue() + ":");
				}
				monthBuf.deleteCharAt(monthBuf.lastIndexOf(":"));
			}

			monthBuf.append("]");

			List<Integer> days = rule.getDayOfMonth();
			StringBuilder daysBuf = new StringBuilder("[");
			if (days != null && days.size() > 0) {
				for (Integer day : days) {
					daysBuf.append(day.intValue() + ":");
				}
				daysBuf.deleteCharAt(daysBuf.lastIndexOf(":"));
			}

			daysBuf.append("]");

			List<Integer> daysOfWeek = rule.getDayOfWeek();
			StringBuilder weekdayBuf = new StringBuilder("[");
			if (daysOfWeek != null && daysOfWeek.size() > 0) {
				for (Integer day : daysOfWeek) {
					weekdayBuf.append(day.intValue() + ":");
				}
				weekdayBuf.deleteCharAt(weekdayBuf.lastIndexOf(":"));
			}

			weekdayBuf.append("]");

			String startTime = "";
			String endTime = "";
			List<TimeOfDayRange> timeOfDayRangeList = rule.getTimeOfDayRange();
			if (timeOfDayRangeList != null && timeOfDayRangeList.size() > 0) {
				TimeOfDayRange tdr = timeOfDayRangeList.get(0);
				SimpleDateFormat dateFormatter = (SimpleDateFormat) DateFormat
				    .getTimeInstance(DateFormat.SHORT, locale);
				startTime = dateFormatter.format(tdr.getStart().getTime());
				endTime = dateFormatter.format(tdr.getEnd().getTime());

			}
			ruleString = permission + "={" + monthBuf.toString() + ","
			    + daysBuf.toString() + "," + weekdayBuf.toString() + "," + startTime
			    + "," + endTime + "}";
		}
		return ruleString;
	}

	public static GlobalPolicy convertToGlobalPolicy(Locale locale,
	    GlobalPolicyForm gpForm, GlobalAuthentication globalAuthentication) {
		GlobalPolicy policy = null;
		if (gpForm != null) {
			GroupPolicy gp = createGroupPolicy(locale, gpForm);
			ResourcePolicy rp = createResourcePolicy(locale, gpForm);
			UserAccountPolicy up = createAccountPolicy(gpForm);
			setupGlobalAuthentication(globalAuthentication, gpForm);
			policy = new GlobalPolicy(gp, rp, up, globalAuthentication);

		}
		if (policy == null) {
			log.error("could not create global policy");
			policy = new GlobalPolicy();
		}
		return policy;
	}

	public static List<Integer> convertToList(String s) {
		List<Integer> list = new ArrayList<Integer>();
		if (s != null) {
			s = s.substring(1, s.lastIndexOf("]"));
			StringTokenizer st = new StringTokenizer(s, ":");
			while (st.hasMoreTokens()) {
				list.add(new Integer(st.nextToken()));
			}
		}
		return list;
	}

	public static void copyProperties(Locale locale, GlobalPolicy src,
	    GlobalPolicyForm dest) {
		if (src != null && dest != null) {
			GroupPolicy policy = src.getUserGroupPolicy();
			if (policy != null) {
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

				List<AbstractRule> accessRules = policy.getAccessRule();
				if (accessRules != null) {
					String[] ruleStr = new String[accessRules.size()];
					RuleForm[] ruleBeans = new RuleForm[accessRules.size()];
					for (int i = 0; i < accessRules.size(); i++) {
						ruleStr[i] = SecurityHelper.convertAccessRule2(locale,
						    (AccessRule) accessRules.get(i));
						ruleBeans[i] = SecurityHelper.convertAccessRule(locale,
						    (AccessRule) accessRules.get(i));
					}
					dest.setGroupSystemAccessRules(ruleStr);
					dest.setGroupRules(ruleBeans);
				}

				BandwidthControlRule bwr = (BandwidthControlRule) policy
				    .getBandwidthControlRule();
				if (bwr != null) {
					if (bwr.getMaximumServiceSize() != null) {
						dest.setGroupMaxServiceSize(bwr.getMaximumServiceSize().toString());
					}
					if (bwr.getMaximumServiceDuration() != null) {
						dest.setGroupMaxServiceDuration(bwr.getMaximumServiceDuration()
						    .toString());
					}
					if (bwr.getMaximumServiceBandwidth() != null) {
						dest.setGroupMaxServiceBandwidth(bwr.getMaximumServiceBandwidth()
						    .toString());
					}
					if (bwr.getMaximumAggregateServiceSize() != null) {
						dest.setGroupMaxAggregateServiceSize(bwr
						    .getMaximumAggregateServiceSize().toString());
					}
				}
			}

			ResourcePolicy resPolicy = src.getResourceGroupPolicy();
			if (resPolicy != null) {
				AccessStateRule state = (AccessStateRule) resPolicy.getStateRule();
				if (state != null) {
					for (int i = 0; i < GlobalPolicyForm.states.length; i++) {
						if (state.equals(GlobalPolicyForm.states[i])) {
							dest.setResourceAccessState(i);
							break;
						}
					}
				}

				List<AbstractRule> rules = resPolicy.getAccessRule();
				if (rules != null) {
					RuleForm[] ruleBeans = new RuleForm[rules.size()];
					String[] ruleStr = new String[rules.size()];
					for (int i = 0; i < rules.size(); i++) {
						ruleStr[i] = SecurityHelper.convertAccessRule2(locale,
						    (AccessRule) rules.get(i));
						ruleBeans[i] = SecurityHelper.convertAccessRule(locale,
						    (AccessRule) rules.get(i));
					}
					dest.setResourceSystemAccessRules(ruleStr);
					dest.setResRules(ruleBeans);
				}
				BandwidthControlRule bwr = (BandwidthControlRule) resPolicy
				    .getBandwidthControlRule();
				if (bwr != null) {
					if (bwr.getMaximumServiceSize() != null) {
						dest.setResourceMaxServiceSize(bwr.getMaximumServiceSize()
						    .toString());
					}
					if (bwr.getMaximumServiceDuration() != null) {
						dest.setResourceMaxServiceDuration(bwr.getMaximumServiceDuration()
						    .toString());
					}
					if (bwr.getMaximumServiceBandwidth() != null) {
						dest.setResourceMaxServiceBandwidth(bwr
						    .getMaximumServiceBandwidth().toString());
					}
					if (bwr.getMaximumAggregateServiceSize() != null) {
						dest.setResourceMaxAggregateServiceSize(bwr
						    .getMaximumAggregateServiceSize().toString());
					}
				}
			}

			UserAccountPolicy accountPolicy = src.getLocalAccountPolicy();
			if (accountPolicy != null) {
				if (accountPolicy.getDormantPeriod() != null) {
					dest.setDormancy(accountPolicy.getDormantPeriod().toString());
				}

				if (accountPolicy.getMaxInvalidLoginAttempts() != null) {
					dest.setInvalidLogins(accountPolicy.getMaxInvalidLoginAttempts()
					    .toString());
				}

				if (accountPolicy.getLockoutPeriod() != null) {
					int lockout = accountPolicy.getLockoutPeriod().intValue();

					dest.setLockoutMetric(DracConstants.TimeMetric.SEC.ordinal());
					dest.setLockoutPeriod(Integer.toString(lockout));
				}

				if (accountPolicy.getInactivityPeriod() != null) {
					int inactivity = accountPolicy.getInactivityPeriod().intValue();
					dest.setInactivityMetric(DracConstants.TimeMetric.SEC.ordinal());
					dest.setInactivityPeriod(Integer.toString(inactivity));
				}

				if (accountPolicy.getLockedClientIPs() != null) {
					dest.setLockedClientIPs(accountPolicy.getLockedClientIPs().toArray(
					    new String[accountPolicy.getLockedClientIPs().size()]));
				}
				Set<String> lockedIPs = LoginAttemptsPolicy.INSTANCE.getIPsInLockout();
				dest.setTempLockedClientIPs(lockedIPs.toArray(new String[lockedIPs.size()]));
				
				if (accountPolicy.getLocalPasswordPolicy() != null) {
					PasswordPolicy passwordPolicy = accountPolicy
					    .getLocalPasswordPolicy();
					Integer pwAging = passwordPolicy.getPwAging();
					if (pwAging != null) {
						dest.setPasswordAging(pwAging.toString());
					}
					if (passwordPolicy.getPwExpiredNotif() != null) {
						dest.setPasswordExpirationNotification(passwordPolicy
						    .getPwExpiredNotif().toString());
					}
					if (passwordPolicy.getPwHistorySize() != null) {
						dest.setPasswordHistorySize(passwordPolicy.getPwHistorySize()
						    .toString());
					}
					if (passwordPolicy.getPwInvalids() != null) {
						dest.setInvalidPasswords(passwordPolicy.getPwInvalids().toArray(
						    new String[passwordPolicy.getPwInvalids().size()]));
					}

					// Password Rules
					String encodedPasswordRules = passwordPolicy.getPwRules();

					if (encodedPasswordRules != null
					    && !encodedPasswordRules.equals(DracConstants.EMPTY_STRING)) {
						Map encodedPasswordRulesMap = new HashMap();
						encodedPasswordRulesMap.put(PasswordEvaluator.PASSWD_PASSWORDRULES,
						    encodedPasswordRules);
						Map decodedRulesMap = new HashMap();
						PasswordEvaluator.decodePasswordRules(encodedPasswordRulesMap,
						    decodedRulesMap);

						String allowedSpecialChars = null;
						String minPasswordLength;
						String minAlphaCharacters;
						String minNumericCharacters;
						String minSpecialCharacters;
						String minDifferentCharacters;
						String mixedCaseCharacters;

						allowedSpecialChars = (String) decodedRulesMap
						    .get(PasswordEvaluator.PASSWD_SPECIALCHARS);

						try {
							allowedSpecialChars = new String(URLDecoder.decode(
							    allowedSpecialChars, "UTF-8"));
						}
						catch (Exception e) {
							log.error("Unexpected exception", e);
						}

						minPasswordLength = (String) decodedRulesMap
						    .get(PasswordEvaluator.PASSWD_MINLENGTH);
						minAlphaCharacters = (String) decodedRulesMap
						    .get(PasswordEvaluator.PASSWD_MINALPHAVALUE);
						minNumericCharacters = (String) decodedRulesMap
						    .get(PasswordEvaluator.PASSWD_MINDIGITVALUE);
						minSpecialCharacters = (String) decodedRulesMap
						    .get(PasswordEvaluator.PASSWD_MINSPECIALVALUE);
						minDifferentCharacters = (String) decodedRulesMap
						    .get(PasswordEvaluator.PASSWD_MINDIFFERENT);
						mixedCaseCharacters = (String) decodedRulesMap
						    .get(PasswordEvaluator.PASSWD_MIXEDALPHA);

						dest.setSpecialCharacters(allowedSpecialChars);
						dest.setMinPasswordLength(minPasswordLength);
						dest.setMinAlphaCharacters(minAlphaCharacters);
						dest.setMinNumericCharacters(minNumericCharacters);
						dest.setMinSpecialCharacters(minSpecialCharacters);
						dest.setMinDifferentCharacters(minDifferentCharacters);
						dest.setMixedCaseCharacters(mixedCaseCharacters);
					}
				}
			}

			GlobalAuthentication globalAuth = src.getSupportedAuthenticationData();
			if (globalAuth != null) {
				List<SupportedAuthenticationType> authTypes = globalAuth
				    .getAllAuthenticationTypes();
				if (authTypes != null) {
					for (SupportedAuthenticationType authType : authTypes) {
						AuthenticationType theType = authType.getType();

						if (AuthenticationType.INTERNAL.equals(theType)) {
							dest.setSupportInternal(true);
							dest.setInternal(authType.isSupported());
						}
						else if (AuthenticationType.A_SELECT.equals(theType)) {
							dest.setSupportAselect(true);
							dest.setAselect(authType.isSupported());
						}
						else if (AuthenticationType.RADIUS.equals(theType)) {
							dest.setSupportRadius(true);
							dest.setRadius(authType.isSupported());
						}
					}
				}
			}
		}
	}

	
	public static UserAccountPolicy createAccountPolicy(GlobalPolicyForm gpForm) {
		UserAccountPolicy accountPolicy = new UserAccountPolicy();
		if (gpForm != null) {
			int metric = gpForm.getInactivityMetric();
			int multiplier = DracConstants.SECS_MULTIPLIER[metric];
			try {
				accountPolicy.setInactivityPeriod(new Integer(gpForm
				    .getInactivityPeriod()) * multiplier);
			}
			catch (NumberFormatException nfe) {
				accountPolicy.setInactivityPeriod(null);
			}

			try {
				accountPolicy.setMaxInvalidLoginAttempts(new Integer(gpForm
				    .getInvalidLogins()));
			}
			catch (NumberFormatException nfe) {
				accountPolicy.setMaxInvalidLoginAttempts(null);
			}

			metric = gpForm.getLockoutMetric();
			multiplier = DracConstants.SECS_MULTIPLIER[metric];
			try {
				accountPolicy.setLockoutPeriod(new Integer(gpForm.getLockoutPeriod())
				    * multiplier);
			}
			catch (NumberFormatException nfe) {
				accountPolicy.setLockoutPeriod(null);
			}

			try {
				accountPolicy.setDormantPeriod(new Integer(gpForm.getDormancy()));
			}
			catch (NumberFormatException nfe) {
				accountPolicy.setDormantPeriod(null);
			}

			String[] lockedClientIPsArray = gpForm.getLockedClientIPs();
			List<String> lockedClientIPsList = new ArrayList<String>();
			String ip = null;
			if (lockedClientIPsArray != null) {
				for (String element : lockedClientIPsArray) {
					ip = element;
					if (ip != null) {
						lockedClientIPsList.add(ip);
					}
				}
			}
			try {
				accountPolicy.setLockedClientIPs(lockedClientIPsList);
			}
			catch (Exception e) {
				accountPolicy.setLockedClientIPs(null);
			}

			PasswordPolicy passwordPolicy = new PasswordPolicy();

			String[] invalidPasswordsArray = gpForm.getInvalidPasswords();
			List<String> invalidPasswordsList = new ArrayList<String>();
			String r = null;
			if (invalidPasswordsArray != null) {
				for (String element : invalidPasswordsArray) {
					r = element;
					if (r != null) {
						invalidPasswordsList.add(r);
					}
				}
			}
			try {
				passwordPolicy.setPwInvalids(invalidPasswordsList);
			}
			catch (Exception e) {
				passwordPolicy.setPwInvalids(null);
			}

			// Password Rules
			String specialCharacters = gpForm.getSpecialCharacters();
			String minPasswordLength = gpForm.getMinPasswordLength();

			if (Integer.parseInt(minPasswordLength) < Integer
			    .parseInt(DracConstants.MINIMUM_PASSWORD_LENGTH)) {
				minPasswordLength = DracConstants.MINIMUM_PASSWORD_LENGTH;
			}

			String minAlphaCharacters = gpForm.getMinAlphaCharacters();
			String minNumericCharacters = gpForm.getMinNumericCharacters();
			String minSpecialCharacters = gpForm.getMinSpecialCharacters();
			String minDifferentCharacters = gpForm.getMinDifferentCharacters();
			String mixedCaseCharacters = gpForm.getMixedCaseCharacters();

			Map passwordRules = new HashMap();

			try {
				specialCharacters = new String(URLEncoder.encode(specialCharacters,
				    "UTF-8"));
			}
			catch (Exception e) {
				log.error("Unexpected exception", e);
			}

			passwordRules.put(PasswordEvaluator.PASSWD_SPECIALCHARS,
			    specialCharacters);
			passwordRules.put(PasswordEvaluator.PASSWD_MINLENGTH, minPasswordLength);
			passwordRules.put(PasswordEvaluator.PASSWD_MINALPHAVALUE,
			    minAlphaCharacters);
			passwordRules.put(PasswordEvaluator.PASSWD_MINDIGITVALUE,
			    minNumericCharacters);
			passwordRules.put(PasswordEvaluator.PASSWD_MINSPECIALVALUE,
			    minSpecialCharacters);
			passwordRules.put(PasswordEvaluator.PASSWD_MINDIFFERENT,
			    minDifferentCharacters);
			passwordRules.put(PasswordEvaluator.PASSWD_MIXEDALPHA,
			    mixedCaseCharacters);

			String myEncodedPasswordRules = PasswordEvaluator
			    .encodedPasswordRules(passwordRules);

			try {
				passwordPolicy.setPwRules(myEncodedPasswordRules);
			}
			catch (Exception e) {
				passwordPolicy.setPwRules(null);
			}

			try {
				passwordPolicy.setPwAging(new Integer(gpForm.getPasswordAging()));
			}
			catch (NumberFormatException nfe) {
				// set a default of 90 days
				passwordPolicy.setPwAging(new Integer(90));
			}

			try {
				passwordPolicy.setPwExpiredNotif(new Integer(gpForm
				    .getPasswordExpirationNotification()));
			}
			catch (NumberFormatException nfe) {
				passwordPolicy.setPwExpiredNotif(null);
			}

			try {
				passwordPolicy.setPwHistorySize(new Integer(gpForm
				    .getPasswordHistorySize()));
			}
			catch (NumberFormatException nfe) {
				passwordPolicy.setPwHistorySize(null);
			}

			accountPolicy.setLocalPasswordPolicy(passwordPolicy);
		}
		return accountPolicy;
	}

	public static GroupPolicy createGroupPolicy(Locale locale,
	    GlobalPolicyForm gpForm) {
		GroupPolicy policy = new GroupPolicy();

		if (gpForm != null) {
			// parse access control rules
			String[] accessControlRules = gpForm.getGroupAccessControlRules();
			List<AbstractRule> controlRules = new ArrayList<AbstractRule>();
			if (accessControlRules != null) {
				for (String accessControlRule : accessControlRules) {
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
					r = parseSystemAccessRule(locale, systemAccessRule);

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

	public static ResourcePolicy createResourcePolicy(Locale locale,
	    GlobalPolicyForm gpForm) {
		ResourcePolicy policy = new ResourcePolicy();
		if (gpForm != null) {
			// parse access state
			int state = gpForm.getResourceAccessState();
			policy.setStateRule(GlobalPolicyForm.states[state]);

			// parse time-based (system access) rules
			String[] systemAccessRules = gpForm.getResourceSystemAccessRules();
			List<AbstractRule> accessRules = new AccessRuleList();
			AbstractRule r = null;
			if (systemAccessRules != null) {
				for (String systemAccessRule : systemAccessRules) {
					r = parseSystemAccessRule(locale, systemAccessRule);
					if (r != null) {
						accessRules.add(r);
					}
				}
			}
			policy.setAccessRule(accessRules);

			// parse bandwidth control rules
			BandwidthControlRule bwRule = new BandwidthControlRule();
			try {
				bwRule.setMaximumServiceSize(new Integer(gpForm
				    .getResourceMaxServiceSize()));
			}
			catch (NumberFormatException nfe) {
				bwRule.setMaximumServiceSize(null);
			}
			try {
				bwRule.setMaximumServiceBandwidth(new Integer(gpForm
				    .getResourceMaxServiceBandwidth()));
			}
			catch (NumberFormatException nfe) {
				bwRule.setMaximumServiceBandwidth(null);
			}
			try {
				bwRule.setMaximumServiceDuration(new Integer(gpForm
				    .getResourceMaxServiceDuration()));
			}
			catch (NumberFormatException nfe) {
				bwRule.setMaximumServiceDuration(null);
			}
			try {
				bwRule.setMaximumAggregateServiceSize(new Integer(gpForm
				    .getResourceMaxAggregateServiceSize()));
			}
			catch (NumberFormatException nfe) {
				bwRule.setMaximumAggregateServiceSize(null);
			}
			policy.setBandwidthControlRule(bwRule);
		}
		return policy;
	}

	//
	// Note that a (grant) rule with no start or end time will look like this:
	// grant={[],[],[],,}
	// A (grant) rule with a start and end time will look like this:
	// grant={[],[],[],1:00 AM,3:30 AM}
	//
	public static AccessRule parseSystemAccessRule(Locale locale, String ruleText) {

		/* Create base logging string for this method. */
		String logEntry = "SecurityHelper.parseSystemAccessRule(): ";

		if (ruleText != null && !ruleText.equals("")) {
			log.debug(logEntry + "ruleText is: " + ruleText);
			StringTokenizer st = new StringTokenizer(ruleText, "=");
			AccessPermission permissionType = null;

			if (st.countTokens() == 2) {
				String permission = st.nextToken();

				if (permission.equals("grant")) {
					permissionType = AccessPermission.GRANT;
				}
				else {
					permissionType = AccessPermission.DENY;
				}

				AccessRule rule = new AccessRule(permissionType);
				String values = st.nextToken();
				values = values.substring(1, values.lastIndexOf("}"));
				StringTokenizer st2 = new StringTokenizer(values, ",");

				if (st2.countTokens() == 5) {
					List<Integer> months = convertToList(st2.nextToken());
					List<Integer> days = convertToList(st2.nextToken());
					List<Integer> weekdays = convertToList(st2.nextToken());
					SimpleDateFormat dateFormatter = (SimpleDateFormat) DateFormat
					    .getTimeInstance(DateFormat.SHORT, locale);
					Date fromTime = dateFormatter.parse(st2.nextToken(),
					    new ParsePosition(0));
					Date toTime = dateFormatter.parse(st2.nextToken(), new ParsePosition(
					    0));
					Calendar startTime = Calendar.getInstance();
					startTime.setTime(fromTime);
					Calendar endTime = Calendar.getInstance();
					endTime.setTime(toTime);
					TimeOfDayRange tdr = new AccessRule.TimeOfDayRange(startTime, endTime);
					List<TimeOfDayRange> tdrList = new ArrayList<TimeOfDayRange>();
					tdrList.add(tdr);

					rule.setMonth(months);
					rule.setDayOfMonth(days);
					rule.setDayOfWeek(weekdays);
					rule.setTimeOfDayRange(tdrList);
					return rule;
				}
				else if (st2.countTokens() == 3) {
					// no start or end time given.
					List<Integer> months = convertToList(st2.nextToken());
					List<Integer> days = convertToList(st2.nextToken());
					List<Integer> weekdays = convertToList(st2.nextToken());
					List<TimeOfDayRange> tdrList = new ArrayList<TimeOfDayRange>();

					rule.setMonth(months);
					rule.setDayOfMonth(days);
					rule.setDayOfWeek(weekdays);
					rule.setTimeOfDayRange(tdrList);
					return rule;
				}
				else {
					log.debug(logEntry + "do not have 5 or 3 tokens. Return null.");
					return null;
				}
			}
			else {
				log.debug(logEntry + "do not have 2 tokens. Return null.");
				return null;
			}
		}
		else {
			log.debug(logEntry + "ruleText is null or blank (invalid). Return null.");
			return null;
		}
	}

	public static void setupGlobalAuthentication(GlobalAuthentication src,
	    GlobalPolicyForm gpForm) {
		if (gpForm != null && src != null) {
			List<SupportedAuthenticationType> authTypes = src
			    .getAllAuthenticationTypes();
			if (authTypes != null) {
				for (SupportedAuthenticationType authType : authTypes) {
					AuthenticationType theType = authType.getType();
					if (AuthenticationType.INTERNAL.equals(theType)) {
						authType.setSupported(new Boolean(gpForm.isInternal()));
					}
					else if (AuthenticationType.A_SELECT.equals(theType)) {
						authType.setSupported(new Boolean(gpForm.isAselect()));
					}
					else if (AuthenticationType.RADIUS.equals(theType)) {
						authType.setSupported(new Boolean(gpForm.isRadius()));
					}
				}
			}
		}
	}

}
