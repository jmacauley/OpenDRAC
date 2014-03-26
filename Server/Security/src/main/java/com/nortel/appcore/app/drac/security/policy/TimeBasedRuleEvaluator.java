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

package com.nortel.appcore.app.drac.security.policy;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.security.policy.types.AbstractRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRule.TimeOfDayRange;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.AccessPermission;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;

public final class TimeBasedRuleEvaluator {
  private static final Logger log = LoggerFactory.getLogger(TimeBasedRuleEvaluator.class);

	private TimeBasedRuleEvaluator() {
	}

	public static boolean evaluate(AccessRule rule, long reqStartTime,
	    long reqEndTime) throws Exception {

		if (rule == null) {
			return true;
		}

		AccessPermission permission = rule.getPermission();

		if (rule.isEmpty()) {
			if (rule.getPermission().equals(AccessPermission.GRANT)) {
				return true;
			}

			if (rule.getPermission().equals(AccessPermission.DENY)) {

				log.debug("Policy check failed on access rule: no-detailed rule"
				    + " with permission: " + permission);

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_TIME_BASED, new Object[] {
				        "permission: " + permission, " rule has no details" });

			}
		}

		Calendar requestStartTime = Calendar.getInstance();
		requestStartTime.setTimeInMillis(reqStartTime);

		Calendar requestEndTime = Calendar.getInstance();
		requestEndTime.setTimeInMillis(reqEndTime);

		if (permission == null) {
			log.debug("Permission is null");
			return true;
		}

		boolean matched = false;
		boolean hasRuled = false;

		
		/**********************************************************/
		/* evaluate timeOfDayRange */
		/**********************************************************/
		/*
		 * evaluate timeOfDayRange first if fall into the range and deny then set
		 * hit to true; if not return true or false depends on permission. Among
		 * time range, "OR" operation is performed. In other words, if one match is
		 * enough to breake out and continue to other time range attributes
		 */
		for (TimeOfDayRange it : rule.getTimeOfDayRange()) {
			hasRuled = true;
			TimeOfDayRange thisTime = setTimeOfDayRangeToTheSameDate(it,
			    requestStartTime);
			long ruleStartTime = thisTime.getStart().getTimeInMillis();
			long ruleEndTime = thisTime.getEnd().getTimeInMillis();

			log.debug("Request start hour: " + reqStartTime + " end:  " + reqEndTime
			    + " Rule start hour: " + ruleStartTime + " end: " + ruleEndTime
			    + " with permission: " + permission);

			if (permission.equals(AccessPermission.DENY)) {
				if (reqStartTime <= ruleEndTime && reqEndTime >= ruleStartTime) {
					// no overlapping wrt service duration; including the equality
					// matched here meant yes the service request time satisfies "DENY"
					// condition.
					matched = true; // just for clarity
					break;
				}
			}
			else { // AccessPermission.GRANT
				if (reqStartTime >= ruleStartTime && reqEndTime <= ruleEndTime) {
					matched = true; // matched here meant yes the service request time
					                // satisfies "GRANT"
					// condition.
					break;
				}
			}
		}

		if (hasRuled) {
			if (permission.equals(AccessPermission.GRANT) && !matched) { // No point
				                                                           // to go
				                                                           // further we
				                                                           // return
				                                                           // exception
				                                                           // here
				String serviceTimeRange = "start="
				    + DateFormatter.timeOfDayRangeToString(requestStartTime) + "/end="
				    + DateFormatter.timeOfDayRangeToString(requestEndTime);
				log.debug("Policy check failed on access rule time range of day: "
				    + serviceTimeRange + " with permission: " + permission);

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_TIME_BASED, new Object[] {
				        "time range of day with permission: " + permission,
				        serviceTimeRange + " is not accepted" });
			}

			if (permission.equals(AccessPermission.DENY) && !matched) {
				// we don't need to go further because all time range does not fall into
				log.debug("No DENY condition match for time range");
				return true;
			}
		}

		/************************************************************/
		/* evaluate day of week */
		/************************************************************/
		hasRuled = false;
		matched = false;
		
		for (Integer it : rule.getDayOfWeek()) {

			hasRuled = true;

			int day = it.intValue();
			if (requestStartTime.get(Calendar.DAY_OF_WEEK) == day
			    || requestEndTime.get(Calendar.DAY_OF_WEEK) == day) { // actually we
				                                                        // only need
				                                                        // either one

				matched = true;
				break;
			}
		}

		if (hasRuled) {
			if (!matched && permission.equals(AccessPermission.GRANT)) {

				String serviceDayOfWeek = String.valueOf(requestStartTime
				    .get(Calendar.DAY_OF_WEEK));
				log.debug("Policy check failed on access rule day of weeks: "
				    + rule.getDayOfWeek() + " vs service day of week:"
				    + serviceDayOfWeek);

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_TIME_BASED, new Object[] {
				        "Day of Week with permission: " + rule.getPermission(),
				        "Rule day of weeks: " + rule.getDayOfWeek()
				            + " against service day of week: " + serviceDayOfWeek });
			}

			if (!matched && permission.equals(AccessPermission.DENY)) {
				return true;
			}
		}

		/**********************************************************/
		/* evaluate day of month */
		/**********************************************************/
		matched = false;
		hasRuled = false;
		
		for (Integer it : rule.getDayOfMonth()) {

			hasRuled = true;

			int day = it.intValue();
			if (requestStartTime.get(Calendar.DAY_OF_MONTH) == day
			    || requestEndTime.get(Calendar.DAY_OF_MONTH) == day) {

				matched = true;
				break;
			}
		}

		if (hasRuled) {
			if (!matched && permission.equals(AccessPermission.GRANT)) {

				String serviceDayOfMonth = "start="
				    + requestStartTime.get(Calendar.DAY_OF_MONTH) + "/end="
				    + requestEndTime.get(Calendar.DAY_OF_MONTH);
				log.debug("Policy check failed on access rule day of week : "
				    + rule.getDayOfMonth() + " vs service time range: "
				    + serviceDayOfMonth);

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_TIME_BASED, new Object[] {
				        "Day of month with permission: " + rule.getPermission(),
				        "Rule day of month: " + rule.getDayOfMonth()
				            + " against service day of week: " + serviceDayOfMonth });
			}

			if (!matched && permission.equals(AccessPermission.DENY)) {
				return true;
			}

		}

		/**********************************************************/
		/* evaluate month */
		/**********************************************************/
		matched = false;
		hasRuled = false;
		
		for (Integer it : rule.getMonth()) {

			hasRuled = true;

			int month = it.intValue();
			if (requestStartTime.get(Calendar.MONTH) == month
			    || requestEndTime.get(Calendar.MONTH) == month) {

				matched = true;
				break;
			}
		}

		if (hasRuled) {
			if (!matched && permission.equals(AccessPermission.GRANT)) {

				log.debug("Policy check failed on access rule mont of the year : "
				    + rule.getMonth() + " vs service month: "
				    + requestStartTime.get(Calendar.MONTH));

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_TIME_BASED, new Object[] {
				        "month with permission: " + permission,
				        "Rule day of weeks: " + rule.getMonth()
				            + " against service month: "
				            + requestStartTime.get(Calendar.MONTH) });
			}

			if (!matched && permission.equals(AccessPermission.DENY)) {
				return true;
			}
		}

		// finally if all granted then granted; if all denied then denied
		if (permission.equals(AccessPermission.GRANT)) {
			return true;

		}

		String serviceTimeRange = "start="
		    + requestStartTime.get(Calendar.HOUR_OF_DAY) + "/end="
		    + requestEndTime.get(Calendar.HOUR_OF_DAY);
		log.debug("Policy check failed on access rule: " + serviceTimeRange
		    + " with permission: " + rule.getPermission());

		throw new DracPolicyException(DracErrorConstants.POLICY_ERROR_TIME_BASED,
		    new Object[] { "permission: " + permission,
		        " requested time is not accepted" });

	}

	public static boolean evaluate(List<AbstractRule> ruleList,
	    long reqStartTime, long reqEndTime) throws Exception {

		// / setup logic business for rule as the whole list.
		// / If permission = GRANT, one rule is sastisfied then return true
		// / If permission = DENY, one rule is sastisfied then return exception.
		// / Note: There is no need to go further for DENY as opposed to GRANT.
		// / That logic should work when the rule list contained both permission
		// types.

		boolean result = true;
		Exception exp = null;

		for (AbstractRule rule : ruleList) {

			if (!(rule instanceof AccessRule)) {
				log.debug("Policy check failed on access rule. Reason: invalid type");
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_TIME_BASED,
				    new Object[] { " invalid type of access rule" });

			}

			AccessRule accessRule = null;
			try {

				accessRule = (AccessRule) rule;
				TimeBasedRuleEvaluator.evaluate(accessRule, reqStartTime, reqEndTime);

				if (accessRule.getPermission().equals(AccessPermission.GRANT)) {
					return true;
				}

			}
			catch (Exception e) {

				if (accessRule.getPermission().equals(AccessPermission.DENY)) {
					throw e;
				}

				result = false;
				exp = e;

			}
		}

		if (!result) {

			if (exp != null) {
				throw exp;
			}

			throw new DracPolicyException(DracErrorConstants.POLICY_ERROR_TIME_BASED,
			    new Object[] { " not accepted" });
		}

		return result;
	}

	private static TimeOfDayRange setTimeOfDayRangeToTheSameDate(
	    TimeOfDayRange time, Calendar thatDate) {

		time.getStart().set(Calendar.YEAR, thatDate.get(Calendar.YEAR));
		time.getStart().set(Calendar.MONTH, thatDate.get(Calendar.MONTH));
		time.getStart().set(Calendar.DATE, thatDate.get(Calendar.DATE));

		time.getEnd().set(Calendar.YEAR, thatDate.get(Calendar.YEAR));
		time.getEnd().set(Calendar.MONTH, thatDate.get(Calendar.MONTH));
		time.getEnd().set(Calendar.DATE, thatDate.get(Calendar.DATE));

		return time;
	}
}
