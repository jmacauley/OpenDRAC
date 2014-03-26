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

import com.nortel.appcore.app.drac.common.datastructure.tree.Node;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.security.policy.types.AbstractRule;
import com.nortel.appcore.app.drac.common.security.policy.types.BandwidthControlRule;
import com.nortel.appcore.app.drac.common.security.policy.types.GroupPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourcePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPolicyProfile;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Schedule;

public final class CreateScheduleServiceRuleEvaluator {
  
  private static final Logger log = LoggerFactory.getLogger(CreateScheduleServiceRuleEvaluator.class);
	private CreateScheduleServiceRuleEvaluator() {
	}

	/****************************************************************/
	/* Recursively evaluating bandwidth policy on 3 nodes: */
	/* 1) source resource group */
	/* 2) target resource group */
	/* 3) billing group */
	/* For each node, each should walk up the tree to exercise all */
	/* policies to the root of the tree */
	/****************************************************************/
	public static boolean evaluateScheduleOnBandwidthPolicy(
	    UserPolicyProfile policyProfile, Schedule schedule) throws Exception {
		int rate = schedule.getPath().getRate();
		UserGroupName billingGroup = schedule.getUserInfo().getBillingGroup();
		String sourceResourceGroup = schedule.getUserInfo()
		    .getSourceEndpointResourceGroup();

		log.debug("Source resource group bandwidth: " + sourceResourceGroup
		    + " checking ...");

		// searching for resource group node in the containment
		Node<ResourceGroupProfile> sourceResGroupNode = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .findNodeByIdFromRoot(sourceResourceGroup);

		if (sourceResGroupNode == null) {
			log.debug("Resource group: " + sourceResourceGroup + " does not exist");
			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
			    new Object[] { "Resource group: " + sourceResourceGroup
			        + " does not exist" });
		}

		// find all parent + ancestors of the group ...
		List<Node<ResourceGroupProfile>> familyTreeOfSourceResourceGroup = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .traverseUpRootToList(sourceResGroupNode);

		// recursively evaluating the policy
		boolean hasResourceGroupPolicy = false;
		for (Node<ResourceGroupProfile> node : familyTreeOfSourceResourceGroup) {
			ResourcePolicy groupPolicy = node.getData().getResourcePolicy();

			if (!groupPolicy.isEmpty()) {
				AbstractRule rule = groupPolicy.getBandwidthControlRule();
				if (rule != null && !((BandwidthControlRule) rule).isEmpty()) {
					hasResourceGroupPolicy = true;
					BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
					    BandwidthControlRuleEvaluator.INVALID_DURATION, rate, 0, 0, null,
					    null, null, null, null);
				}
			}
		}

		if (!hasResourceGroupPolicy) {
			AbstractRule rule = policyProfile.getGlobalPolicy()
			    .getResourceGroupPolicy().getBandwidthControlRule();
			if (rule != null) {
				BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
				    BandwidthControlRuleEvaluator.INVALID_DURATION, rate, 0, 0, null,
				    null, null, null, null);
			}
		}

		// Done the same thing on target resource group then
		String targetResourceGroup = schedule.getUserInfo()
		    .getTargetEndpointResourceGroup();
		log.debug("Target resource group bandwidth: " + targetResourceGroup
		    + " checking ...");

		Node<ResourceGroupProfile> targetResGroupNode = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .findNodeByIdFromRoot(targetResourceGroup);

		if (targetResourceGroup == null) {
			log.debug("Resource group: is null");
			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
			    new Object[] { "Resource group: is null" });
		}

		List<Node<ResourceGroupProfile>> familyTreeOfTargetResourceGroup = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .traverseUpRootToList(targetResGroupNode);

		hasResourceGroupPolicy = false;

		for (Node<ResourceGroupProfile> node : familyTreeOfTargetResourceGroup) {
			ResourcePolicy groupPolicy = node.getData().getResourcePolicy();

			if (!groupPolicy.isEmpty()) {
				AbstractRule rule = groupPolicy.getBandwidthControlRule();
				if (rule != null && !((BandwidthControlRule) rule).isEmpty()) {
					hasResourceGroupPolicy = true;
					BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
					    BandwidthControlRuleEvaluator.INVALID_DURATION, rate, 0, 0, null,
					    null, null, null, null);
				}
			}
		}

		if (!hasResourceGroupPolicy) { //
			AbstractRule rule = policyProfile.getGlobalPolicy()
			    .getResourceGroupPolicy().getBandwidthControlRule();
			if (rule != null) {
				BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
				    BandwidthControlRuleEvaluator.INVALID_DURATION, rate, 0, 0, null,
				    null, null, null, null);
			}
		}

		log.debug("Billing group bandwidth: " + billingGroup + " checking ...");
		// Billing group bandwidth policy check

		// Get billing user group node first and start walking up the tree.
		Node<UserGroupProfile> billingGroupNode = HierarchicalContainmentPolicy
		    .INSTANCE.getUserGroupProfileTree()
		    .findNodeByIdFromRoot(billingGroup.toString());

		List<Node<UserGroupProfile>> familyTreeOfBillingGroup = HierarchicalContainmentPolicy
		    .INSTANCE.getUserGroupProfileTree()
		    .traverseUpRootToList(billingGroupNode);

		boolean hasGroupPolicy = false;
		for (Node<UserGroupProfile> node : familyTreeOfBillingGroup) {
			GroupPolicy groupPolicy = node.getData().getGroupPolicy();

			if (!groupPolicy.isEmpty()) {
				AbstractRule rule = groupPolicy.getBandwidthControlRule();
				if (rule != null && !((BandwidthControlRule) rule).isEmpty()) {
					hasGroupPolicy = true;
					BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
					    BandwidthControlRuleEvaluator.INVALID_DURATION, rate, 0, 0, null,
					    null, null, null, null);
				}
			}
		}

		if (!hasGroupPolicy) { //
			AbstractRule rule = policyProfile.getGlobalPolicy().getUserGroupPolicy()
			    .getBandwidthControlRule();
			if (rule != null) {
				BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
				    BandwidthControlRuleEvaluator.INVALID_DURATION, rate, 0, 0, null,
				    null, null, null, null);
			}
		}

		return true;
	}

	public static boolean evaluateServiceOnBandwidthPolicy(
	    UserPolicyProfile policyProfile, DracService service) throws Exception {

		long startTime = service.getStartTime();
		long endTime = service.getEndTime();
		long duration = endTime - startTime;
		String sourceEndPoint = service.getPath().getSourceEndPoint().getId();
		String targetEndPoint = service.getPath().getTargetEndPoint().getId();

		boolean hasResourceGroupPolicy = false;

		// ResourceGroup BandwidthControl
		String sourceResourceGroup = service.getUserInfo()
		    .getSourceEndpointResourceGroup();

		log.debug("Source resource group bandwidth: " + sourceResourceGroup
		    + " checking ...");
		Node<ResourceGroupProfile> sourceResGroupNode = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .findNodeByIdFromRoot(sourceResourceGroup);

		List<Node<ResourceGroupProfile>> familyTreeOfSourceResourceGroup = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .traverseUpRootToList(sourceResGroupNode);

		for (Node<ResourceGroupProfile> node : familyTreeOfSourceResourceGroup) {
			ResourceGroupProfile rgp = node.getData();
			ResourcePolicy groupPolicy = rgp.getResourcePolicy();

			if (!groupPolicy.isEmpty()) {
				AbstractRule rule = groupPolicy.getBandwidthControlRule();
				if (rule != null && !((BandwidthControlRule) rule).isEmpty()) {
					hasResourceGroupPolicy = true;
					BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
					    duration, service.getPath().getRate(), startTime, endTime, null,
					    sourceEndPoint, sourceResourceGroup, null, null);
				}
			}
		}

		if (!hasResourceGroupPolicy) {
			AbstractRule rule = policyProfile.getGlobalPolicy()
			    .getResourceGroupPolicy().getBandwidthControlRule();
			if (rule != null) {
				BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
				    duration, service.getPath().getRate(), startTime, endTime, null,
				    sourceEndPoint, sourceResourceGroup, null, null);
			}
		}

		String targetResourceGroup = service.getUserInfo()
		    .getTargetEndpointResourceGroup();
		log.debug("Target resource group bandwidth: " + targetResourceGroup
		    + " checking ...");
		Node<ResourceGroupProfile> targetResGroupNode = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .findNodeByIdFromRoot(targetResourceGroup);

		List<Node<ResourceGroupProfile>> familyTreeOfTargetResourceGroup = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .traverseUpRootToList(targetResGroupNode);

		hasResourceGroupPolicy = false;

		for (Node<ResourceGroupProfile> node : familyTreeOfTargetResourceGroup) {
			ResourcePolicy groupPolicy = node.getData().getResourcePolicy();

			if (!groupPolicy.isEmpty()) {
				AbstractRule rule = groupPolicy.getBandwidthControlRule();
				if (rule != null && !((BandwidthControlRule) rule).isEmpty()) {
					hasResourceGroupPolicy = true;
					BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
					    duration, service.getPath().getRate(), startTime, endTime, null,
					    null, null, targetEndPoint, targetResourceGroup);
				}
			}
		}

		if (!hasResourceGroupPolicy) { //
			AbstractRule rule = policyProfile.getGlobalPolicy()
			    .getResourceGroupPolicy().getBandwidthControlRule();
			if (rule != null) {
				BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
				    duration, service.getPath().getRate(), startTime, endTime, null,
				    null, null, targetEndPoint, targetResourceGroup);
			}
		}

		// Get billing user group node and start walking up the tree.
		UserGroupName billingGroup = service.getUserInfo().getBillingGroup();
		log.debug("Billing group bandwidth: " + billingGroup + " checking ...");

		Node<UserGroupProfile> billingGroupNode = HierarchicalContainmentPolicy
		    .INSTANCE.getUserGroupProfileTree()
		    .findNodeByIdFromRoot(billingGroup.toString());
		List<Node<UserGroupProfile>> familyTreeOfBillingGroup = HierarchicalContainmentPolicy
		    .INSTANCE.getUserGroupProfileTree()
		    .traverseUpRootToList(billingGroupNode);
		boolean hasGroupPolicy = false;

		for (Node<UserGroupProfile> node : familyTreeOfBillingGroup) {
			GroupPolicy groupPolicy = node.getData().getGroupPolicy();

			if (!groupPolicy.isEmpty()) {
				AbstractRule rule = groupPolicy.getBandwidthControlRule();
				if (rule != null && !((BandwidthControlRule) rule).isEmpty()) {
					hasGroupPolicy = true;
					BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
					    duration, service.getPath().getRate(), startTime, endTime,
					    billingGroup, null, null, null, null);
				}
			}
		}

		if (!hasGroupPolicy) { //
			AbstractRule rule = policyProfile.getGlobalPolicy().getUserGroupPolicy()
			    .getBandwidthControlRule();
			if (rule != null) {
				BandwidthControlRuleEvaluator.evaluate((BandwidthControlRule) rule,
				    duration, service.getPath().getRate(), startTime, endTime,
				    billingGroup, null, null, null, null);
			}
		}

		return true;
	}

	public static boolean evaluateServiceOnTimeBasedPolicy(
	    UserPolicyProfile policyProfile, DracService service) throws Exception {

		long startTime = service.getStartTime();
		long endTime = service.getEndTime();
		boolean hasResourceGroupPolicy = false;

		// ResourceGroup Timebased
		String sourceResourceGroup = service.getUserInfo()
		    .getSourceEndpointResourceGroup();

		log.debug("Source resource group time based policy: " + sourceResourceGroup
		    + " checking...");

		Node<ResourceGroupProfile> sourceResGroupNode = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .findNodeByIdFromRoot(sourceResourceGroup);

		List<Node<ResourceGroupProfile>> familyTreeOfSourceResourceGroup = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .traverseUpRootToList(sourceResGroupNode);

		for (Node<ResourceGroupProfile> node : familyTreeOfSourceResourceGroup) {
			ResourceGroupProfile rgp = node.getData();
			ResourcePolicy groupPolicy = rgp.getResourcePolicy();

			if (!groupPolicy.isEmpty()) {
				List<AbstractRule> ruleList = groupPolicy.getAccessRule();
				if (ruleList != null && !ruleList.isEmpty()) {
					hasResourceGroupPolicy = true;
					TimeBasedRuleEvaluator.evaluate(ruleList, startTime, endTime);
				}
			}
		}

		if (!hasResourceGroupPolicy) {
			List<AbstractRule> ruleList = policyProfile.getGlobalPolicy()
			    .getResourceGroupPolicy().getAccessRule();
			if (ruleList != null && !ruleList.isEmpty()) {
				TimeBasedRuleEvaluator.evaluate(ruleList, startTime, endTime);
			}
		}

		String targetResourceGroup = service.getUserInfo()
		    .getTargetEndpointResourceGroup();
		log.debug("Target resource group time based policy: " + targetResourceGroup
		    + " checking ...");
		Node<ResourceGroupProfile> targetResGroupNode = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .findNodeByIdFromRoot(targetResourceGroup);

		List<Node<ResourceGroupProfile>> familyTreeOfTargetResourceGroup = HierarchicalContainmentPolicy
		    .INSTANCE.getResourceGroupProfileTree()
		    .traverseUpRootToList(targetResGroupNode);

		hasResourceGroupPolicy = false;

		for (Node<ResourceGroupProfile> node : familyTreeOfTargetResourceGroup) {
			ResourcePolicy groupPolicy = node.getData().getResourcePolicy();

			if (!groupPolicy.isEmpty()) {
				List<AbstractRule> ruleList = groupPolicy.getAccessRule();
				if (ruleList != null && !ruleList.isEmpty()) {
					hasResourceGroupPolicy = true;
					TimeBasedRuleEvaluator.evaluate(ruleList, startTime, endTime);
				}
			}
		}

		if (!hasResourceGroupPolicy) { //
			List<AbstractRule> ruleList = policyProfile.getGlobalPolicy()
			    .getResourceGroupPolicy().getAccessRule();
			if (ruleList != null) {
				TimeBasedRuleEvaluator.evaluate(ruleList, startTime, endTime);
			}
		}

		// Get billing user group node and start walking up the tree.
		UserGroupName billingGroup = service.getUserInfo().getBillingGroup();
		log.debug("Billing group time based policy: " + billingGroup
		    + " checking ...");

		Node<UserGroupProfile> billingGroupNode = HierarchicalContainmentPolicy
		    .INSTANCE.getUserGroupProfileTree()
		    .findNodeByIdFromRoot(billingGroup.toString());
		List<Node<UserGroupProfile>> familyTreeOfBillingGroup = HierarchicalContainmentPolicy
		    .INSTANCE.getUserGroupProfileTree()
		    .traverseUpRootToList(billingGroupNode);
		boolean hasGroupPolicy = false;

		for (Node<UserGroupProfile> node : familyTreeOfBillingGroup) {
			GroupPolicy groupPolicy = node.getData().getGroupPolicy();

			if (!groupPolicy.isEmpty()) {
				List<AbstractRule> ruleList = groupPolicy.getAccessRule();
				if (ruleList != null && !ruleList.isEmpty()) {
					hasGroupPolicy = true;
					TimeBasedRuleEvaluator.evaluate(ruleList, startTime, endTime);
				}
			}
		}

		if (!hasGroupPolicy) { //
			List<AbstractRule> ruleList = policyProfile.getGlobalPolicy()
			    .getUserGroupPolicy().getAccessRule();
			if (ruleList != null) {
				TimeBasedRuleEvaluator.evaluate(ruleList, startTime, endTime);
			}
		}

		return true;
	}

}
