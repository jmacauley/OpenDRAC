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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPolicyProfile;

public final class ViewScheduleServiceRuleEvaluator {
  private static final Logger log = LoggerFactory.getLogger(ViewScheduleServiceRuleEvaluator.class);

	private ViewScheduleServiceRuleEvaluator() {
	}

	public static boolean evaluate(UserPolicyProfile policyProfile,
	    UserGroupName billingGroup, String serviceScheduleName) throws Exception {

		UserGroupType userType = policyProfile.getUserGroupType();

		if (userType.equals(UserGroupType.SYSTEM_ADMIN)) {
			return true;
		}

		String userID = policyProfile.getUserProfile().getUserID();
		String error;

		if (userType.equals(UserGroupType.USER)) {
			if (HierarchicalContainmentPolicy.INSTANCE.isMemberOfUserGroup(
			    userID, billingGroup)) {
				return true;
			}

			// Low Risk #10 - Do not include internal information in error messages
			// presented to
			// non-administrator users
			// (info to which that the user would not otherwise have access)
			error = "Policy check failed on userID: " + userID
			    + "  with access type of " + userType.toString()
			    + " on schedule/service: " + serviceScheduleName; // +
			                                                      // " created by billing group: "
			                                                      // +
			                                                      // billingGroup;
			log.debug(error);

			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL, new Object[] {
			        "schedule/service: " + serviceScheduleName, error });
		}

		if (userType.equals(UserGroupType.GROUP_ADMIN)) {
			if (HierarchicalContainmentPolicy.INSTANCE.isMemberOfUserGroup(
			    userID, billingGroup)
			    || HierarchicalContainmentPolicy.INSTANCE.isAdminOfUserGroup(
			        userID, billingGroup)) {
				return true;
			}

			// Low Risk #10 - Do not include internal information in error messages
			// presented to
			// non-administrator users
			// (info to which that the user would not otherwise have access)
			error = "Policy check failed on userID: " + userID
			    + "  with access type of " + userType.toString()
			    + " on schedule/service: " + serviceScheduleName; // +
			                                                      // " created by billing group: "
			                                                      // +
			                                                      // billingGroup;
			log.debug(error);
			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL, new Object[] {
			        "schedule/service: " + serviceScheduleName, error });
		}

		throw new DracPolicyException(
		    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
		    new Object[] { "invalid user-group type: " + userType.toString() });
	}
}
