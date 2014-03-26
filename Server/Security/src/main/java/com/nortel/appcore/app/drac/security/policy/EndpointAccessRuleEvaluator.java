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
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPolicyProfile;
import com.nortel.appcore.app.drac.common.types.EndPointType;

public final class EndpointAccessRuleEvaluator {
  
  private static final Logger log = LoggerFactory.getLogger(EndpointAccessRuleEvaluator.class);
	private EndpointAccessRuleEvaluator() {
	}

	public static boolean evaluate(UserPolicyProfile policyProfile,
	    PolicyRequest request) throws Exception {
		EndPointType ep = (EndPointType) request.getRequestor();
		String endpointID = ep.getId();
		String endpointTNA = ep.getName();

		if (policyProfile == null) {
			log.error("There is no policy assoicated with this endpoint: "
			    + endpointTNA);
			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL, new Object[] {
			        "endpoint: " + endpointTNA,
			        "the resource is not assigned to this user" });
		}

		// check on usergroup level, the user is lowest and system admin is highest
		UserGroupType userGroupType = policyProfile.getUserGroupType();
		log.debug("UserGroup Type: " + userGroupType.toString());

		if (userGroupType.equals(UserGroupType.USER)) {
			/******************************************************************/
			/* Able see and view everything belonged to his groups, but write */
			/******************************************************************/

			if (request.getType().equals(PolicyRequest.CommandType.READ)) {

				if (HierarchicalContainmentPolicy.INSTANCE
				    .hasThisUserAccessResource(
				        policyProfile.getUserProfile().getUserID(), endpointID)) {
					return true;
				}

				log.debug("cannot find resource: " + endpointTNA);
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_READ_ACCESS_CONTROL_USER_TYPE,
				    new Object[] { "endpoint: " + endpointTNA, userGroupType.toString() });

			}

			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_WRITE_ACCESS_CONTROL_USER_TYPE,
			    new Object[] { "endpoint: " + endpointTNA, userGroupType.toString() });

		}
		else if (userGroupType.equals(UserGroupType.GROUP_ADMIN)) {

			/**************************************************************/
			/* Beeing able see and view everything belonged to his groups */
			/**************************************************************/

			if (request.getType().equals(PolicyRequest.CommandType.READ)) {

				if (HierarchicalContainmentPolicy.INSTANCE
				    .hasThisUserAccessResource(
				        policyProfile.getUserProfile().getUserID(), endpointID)) {
					return true;
				}

				log.debug("User: " + policyProfile.getUserProfile().getUserID()
				    + " has no access to: " + endpointTNA);

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_READ_ACCESS_CONTROL_USER_TYPE,
				    new Object[] { "endpoint: " + endpointTNA, userGroupType.toString() });

			}

			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_WRITE_ACCESS_CONTROL_USER_TYPE,
			    new Object[] { "endpoint: " + endpointTNA, userGroupType.toString() });

		}
		else if (userGroupType.equals(UserGroupType.SYSTEM_ADMIN)) {
			/******************************************************************/
			/* can do anything: read and write */
			/******************************************************************/

			return true;
		}
		else {

			log.error("UserGroupType not supported..." + userGroupType);
			throw new DracPolicyException(
			    DracErrorConstants.SECURITY_ERROR_TYPE_NOT_SUPPORTED,
			    new Object[] { userGroupType.toString() });
		}

	}
}
