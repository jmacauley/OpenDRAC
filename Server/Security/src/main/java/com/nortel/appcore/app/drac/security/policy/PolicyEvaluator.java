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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckablePolicy;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPolicyProfile;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.UserType;

public final class PolicyEvaluator {
  
  private static final Logger log = LoggerFactory.getLogger(PolicyEvaluator.class);

	public static boolean evaluate(UserDetails userDetails, PolicyRequest request)
	    throws DracPolicyException {
		long start = System.currentTimeMillis();
		try {

			if (userDetails == null) {
				log.debug("user details should not be null.");
				throw new DracPolicyException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { "UserDetails " });
			}

			UserPolicyProfile policyProfile = userDetails.getUserPolicyProfile();

			if (policyProfile == null) {
				log.debug("user policy should not be null.");
				throw new DracPolicyException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { "User Policy Profile" });
			}

			if (request == null) {
				log.debug("Request should not be null");
				throw new DracPolicyException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { "Policy request" });
			}

			if (request.getRequestor() == null) {
				log.debug("PolicyCheckable request is null");
				throw new DracPolicyException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { "Policy request should not be null" });
			}

			if (request.getRequestor() instanceof Schedule) {
				log.debug("evaluating policy: "
				    + request.getRequestor().getClass().getName());
				return PolicyEvaluator.evaluatePolicyOnSchedule(userDetails, request);
			}

			if (request.getRequestor() instanceof DracService) {
				log.debug("evaluating policy: "
				    + request.getRequestor().getClass().getName());
				return PolicyEvaluator.evaluatePolicyOnService(userDetails, request);
			}

			if (request.getRequestor() instanceof EndPointType) {
				log.debug("evaluating policy: "
				    + request.getRequestor().getClass().getName());
				return PolicyEvaluator.evaluatePolicyOnEndpoint(policyProfile, request);
			}

			if (request.getRequestor() instanceof PolicyCheckableNull) {
				return true;
			}

			if (request.getRequestor() instanceof PolicyCheckablePolicy) {
				log.debug("evaluating policy: "
				    + request.getRequestor().getClass().getName());
				return PolicyEvaluator.evaluatePolicyOnPolicy(policyProfile, request);
			}

			// we don't care about other type for now
			log.debug("Not yet supported PolicyCheckable type of: "
			    + request.getClass().getName());
			throw new DracPolicyException(
			    DracErrorConstants.SECURITY_ERROR_TYPE_NOT_SUPPORTED,
			    new Object[] { request.getClass().getName() });

		}
		catch (DracPolicyException e) {
			throw e;
		}
		catch (Exception e) {
			log.error("Policy failed: " + e.getMessage(), e);
			throw new DracPolicyException(
			    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
			    new Object[] { e.getMessage() }, e);
		}
		finally {
			long howLong = System.currentTimeMillis() - start;
			log.debug("WP: PolicyEvaluator: isAllowed() took " + howLong + " "
			    + userDetails + " " + request);
		}
	}

	private static boolean evaluatePolicyOnEndpoint(
	    UserPolicyProfile policyProfile, PolicyRequest request) throws Exception {

		// we stream the userprofile to contain all usergroups/resourcegroup relates
		// to interested endpoints.
		// String endpointID = ((EndPointType)request.getRequestor()).getId();
		// UserPolicyProfile profile =
		// UserPolicyProfile.getUserPolicyOnlyContainingResource(policyProfile,
		// endpointID);
		return EndpointAccessRuleEvaluator.evaluate(policyProfile, request);

	}

	private static boolean evaluatePolicyOnPolicy(
	    UserPolicyProfile policyProfile, PolicyRequest request) throws Exception {
		return PolicyAccessRuleEvaluator.evaluate(policyProfile, request);
	}

	private static boolean evaluatePolicyOnSchedule(UserDetails userDetails,
	    PolicyRequest request) throws Exception {
		try {

			UserPolicyProfile policyProfile = userDetails.getUserPolicyProfile();

			Schedule schedule = (Schedule) request.getRequestor();
			
			UserGroupName billingGroup = schedule.getUserInfo().getBillingGroup();
			String sourceEndPoint = schedule.getPath().getSourceEndPoint().getId();
			String targetEndPoint = schedule.getPath().getTargetEndPoint().getId();
			log.debug("Policy check for resources:" + sourceEndPoint + " & "
			    + targetEndPoint);

			// POLICY for CANCEL
			if (request.getType().equals(PolicyRequest.CommandType.CANCEL)
			    || request.getType().equals(PolicyRequest.CommandType.EDIT)) {
				log.debug("Policy check for cancel/edit...");
				return EditScheduleServiceRuleEvaluator.evaluate(policyProfile,
				    billingGroup, schedule.getUserInfo().getUserId(),
				    schedule.getName());
			}

			// POLICY for VIEW
			else if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				
				return ViewScheduleServiceRuleEvaluator.evaluate(policyProfile,
				    billingGroup, schedule.getName());

			}
			else if (request.getType().equals(PolicyRequest.CommandType.CREATE)
			    || request.getType().equals(PolicyRequest.CommandType.WRITE)) {

				// POLICY for WRITE
				log.debug("Policy check for write...");
				validateScheduleServiceEndpoints(policyProfile, schedule.getName(),
				    schedule.getPath(), schedule.getUserInfo());
				CreateScheduleServiceRuleEvaluator.evaluateScheduleOnBandwidthPolicy(
				    policyProfile, schedule);

			}
			else {

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] {
				        "user profile: " + policyProfile.getUserProfile().getUserID(),
				        request.getType().toString() + " is not supported." });
			}

		}
		catch (Exception e) {

			// we only create audit if it is not a read command
			if (!request.getType().equals(PolicyRequest.CommandType.READ)) {
				userDetails.setErrorCode(SessionErrorCode.ERROR_GENERAL);
				if (request.getRequestor() instanceof Schedule) {
					PolicyLogEntry.createLogEntry(userDetails,
					    ((Schedule) request.getRequestor()).getId(),
					    LogKeyEnum.KEY_POLICY_SCHEDULE_ACCESS_FAILED);
				}
			}
			throw e;

		}

		return true;
	}

	private static boolean evaluatePolicyOnService(UserDetails userDetails,
	    PolicyRequest request) throws Exception {
		try {
			UserPolicyProfile policyProfile = userDetails.getUserPolicyProfile();
			DracService service = (DracService) request.getRequestor();
			log.debug("Policy checking service:" + service.getId());

			UserGroupName billingGroup = service.getUserInfo().getBillingGroup();

			if (request.getType().equals(PolicyRequest.CommandType.CANCEL)
			    || request.getType().equals(PolicyRequest.CommandType.EDIT)) {
				log.debug("Policy check for cancel/edit...");
				EditScheduleServiceRuleEvaluator.evaluate(policyProfile, billingGroup,
				    service.getUserInfo().getUserId(), service.getId());

			}
			else if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				log.debug("Policy check for read...");
				ViewScheduleServiceRuleEvaluator.evaluate(policyProfile, billingGroup,
				    service.getId());

			}
			else if (request.getType().equals(PolicyRequest.CommandType.CREATE)
			    || request.getType().equals(PolicyRequest.CommandType.WRITE)) {

				// Policy that apply to ServiceIdType write
				log.debug("Policy check for write...");

				validateScheduleServiceEndpoints(policyProfile, service.getId(),
				    service.getPath(), service.getUserInfo());

				CreateScheduleServiceRuleEvaluator.evaluateServiceOnBandwidthPolicy(
				    policyProfile, service);
				CreateScheduleServiceRuleEvaluator.evaluateServiceOnTimeBasedPolicy(
				    policyProfile, service);

			}
			else {

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] {
				        "user profile: " + policyProfile.getUserProfile().getUserID(),
				        request.getType().toString() + " is not supported." });
			}

		}
		catch (Exception e) {
			// we only create audit if it is not a read command
			if (!request.getType().equals(PolicyRequest.CommandType.READ)) {
				userDetails.setErrorCode(SessionErrorCode.ERROR_GENERAL);
				if (request.getRequestor() instanceof DracService) {
					PolicyLogEntry.createLogEntry(userDetails,
					    ((DracService) request.getRequestor()).getId(),
					    LogKeyEnum.KEY_POLICY_SERVICE_ACCESS_FAILED);
				}
			}

			throw e;

		}

		return true;
	}

	private static ResourceGroupProfile getProfileForResourceGroupEndPoint(String endPoint, List<ResourceGroupProfile> profiles){
		ResourceGroupProfile theProfileToReturn = null;
		for(ResourceGroupProfile profile: profiles){
			if(getResourceForEndpoint(endPoint, profile.getResourceList())!=null){
				theProfileToReturn = profile;				
				break;
			}
		}
		return theProfileToReturn;
	}
	
	private static Resource getResourceForEndpoint(String endPoint, List<Resource> resources){
		Resource resourceToReturn = null;
		for(Resource resource: resources){
			if(resource.getResourceID().equals(endPoint)){
				resourceToReturn = resource;
				break;
			}
		}
		return resourceToReturn;
	}

	private static boolean validateScheduleServiceEndpoints(UserPolicyProfile policyProfile, String scheduleServiceId,
	        PathType path, UserType userType) throws Exception {
		
		log.debug("Start validateScheduleServiceEndpoints ...");
		if(!policyProfile.hasAccessToAllResources()){		
			List<ResourceGroupProfile> resourceGroupProfiles = policyProfile.getResourceGroupList();		
			String sourceEndPoint = path.getSourceEndPoint().getId();
			String targetEndPoint = path.getTargetEndPoint().getId();
			ResourceGroupProfile sourceProfile = getProfileForResourceGroupEndPoint(sourceEndPoint, resourceGroupProfiles);
			ResourceGroupProfile targetProfile = getProfileForResourceGroupEndPoint(targetEndPoint, resourceGroupProfiles);
	
			if ((sourceProfile == null || targetProfile == null)) {
				throw new DracPolicyException(DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL, new Object[] {
				        "schedule/service: " + scheduleServiceId,
				        "No profile foud with endpoint(s):" + sourceEndPoint + " and " + targetEndPoint});
			}
		}
		return true;
	}
}
