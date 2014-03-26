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

import com.nortel.appcore.app.drac.common.datastructure.tree.Node;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.errorhandling.ExceptionFormatter;
import com.nortel.appcore.app.drac.common.errorhandling.ResourceKey;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.GroupPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourcePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPolicyProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;

public final class PolicyAccessRuleEvaluator {
  
  private static final Logger log = LoggerFactory.getLogger(PolicyAccessRuleEvaluator.class);
  
	private static final ResourceKey ERROR_READ_ACCESS = new ResourceKey(
	    "policy.readAccess.failed");
	private static final ResourceKey ERROR_EDIT_ACCESS = new ResourceKey(
	    "policy.editAccess.failed");
	private static final ResourceKey ERROR_DELETE_ACCESS = new ResourceKey(
	    "policy.deleteAccess.failed");
	private static final ResourceKey ERROR_READGROUP_ACCESS = new ResourceKey(
	    "policy.readGroupAccess.failed");
	private static final ResourceKey ERROR_CREATEGROUP_ACCESS = new ResourceKey(
	    "policy.createGroupAccess.failed");
	private static final ResourceKey ERROR_EDITGROUP_ACCESS = new ResourceKey(
	    "policy.editGroupAccess.failed");
	private static final ResourceKey ERROR_DELETEGROUP_CHILDREN = new ResourceKey(
	    "policy.deleteGroup.failed.child");
	private static final ResourceKey ERROR_DELETEGROUP_ROOT = new ResourceKey(
	    "policy.deleteGroup.failed.root");
	private static final ResourceKey ERROR_DELETEGROUP_ACCESS = new ResourceKey(
	    "policy.deleteGroupAccess.failed");
	private static final ResourceKey ERROR_READRESGROUP_ACCESS = new ResourceKey(
	    "policy.readResGroupAccess.failed");

	private static final ResourceKey ERROR_CREATERESGROUP_PARENT = new ResourceKey(
	    "policy.createResGroup.failed.parent");
	private static final ResourceKey ERROR_CREATERESGROUP_NOTASSIGNED = new ResourceKey(
	    "policy.createResGroup.failed.notAssigned");
	private static final ResourceKey ERROR_CREATERESGROUP_NORESOURCE = new ResourceKey(
	    "policy.createResGroup.failed.resource");
	private static final ResourceKey ERROR_EDITRESGROUP = new ResourceKey(
	    "policy.editResGroupAccess.failed");
	private static final ResourceKey ERROR_DELETERESGROUP_CHILDREN = new ResourceKey(
	    "policy.deleteResGroup.failed.child");
	private static final ResourceKey ERROR_DELETERESGROUP_ROOT = new ResourceKey(
	    "policy.deleteResGroup.failed.root");
	private static final ResourceKey ERROR_DELETERESGROUP_ACCESS = new ResourceKey(
	    "policy.deleteResGroupAccess.failed");

	private PolicyAccessRuleEvaluator() {
	}

	public static boolean evaluate(UserPolicyProfile policyProfile,
	    PolicyRequest request) throws Exception {
		UserGroupType userGroupType = policyProfile.getUserGroupType();

		if (userGroupType.equals(UserGroupType.USER)) {
			return evalUser(policyProfile, request);
		}
		else if (userGroupType.equals(UserGroupType.GROUP_ADMIN)) {
			return evalGroup(policyProfile, request);
		}
		else if (userGroupType.equals(UserGroupType.SYSTEM_ADMIN)) {
			return evalSysAdmin(policyProfile, request);
		}

		throw new DracPolicyException(
		    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
		    new Object[] { "invalid user-group type: " + userGroupType.toString() });
	}

	/**
     * 
     */
	private static boolean evalGroup(UserPolicyProfile policyProfile,
	    PolicyRequest request) throws Exception {
		String error;
		UserGroupType userGroupType = policyProfile.getUserGroupType();

		/** *********************************************************** */
		/* Being able see and view everything belonged to his groups */
		/** *********************************************************** */

		if (request.getRequestor() instanceof UserProfile) {

			UserProfile userProfile = (UserProfile) request.getRequestor();

			if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				// Is this correct?
				// return true;

				// u can read yours
				if (userProfile.getUserID().equals(
				    policyProfile.getUserProfile().getUserID())) {
					return true;
				}

				// if the user is not assigned to any user group then accept it.
				if (userProfile.getMembershipData().getMemberUserGroupName().isEmpty()) {
					return true;
				}

				if (HierarchicalContainmentPolicy.INSTANCE
				    .isAdminOfUser(policyProfile.getUserProfile().getUserID(),
				        userProfile.getUserID())) {
					return true;
				}

				if (HierarchicalContainmentPolicy.INSTANCE
				    .isDirectMemberOfUser(policyProfile.getUserProfile().getUserID(),
				        userProfile.getUserID())) {
					return true;
				}

				log.debug("read access failed on userID: "
				    + policyProfile.getUserProfile().getUserID()
				    + " with access type of " + userGroupType.toString()
				    + " on user profile: " + userProfile.getUserID()
				    + ". Reason: you can only read profiles of (direct) members.");
				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_READ_ACCESS, new Object[] { userProfile.getUserID() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

			}

			// a group-admin user can create user profile.
			if (request.getType().equals(PolicyRequest.CommandType.WRITE)
			    || request.getType().equals(PolicyRequest.CommandType.CREATE)) {
				// @TODO: Is this correct?
				log.debug("Check create access");
				return true;
			}

			if (request.getType().equals(PolicyRequest.CommandType.EDIT)) {
				log.debug("Check edit access");
				// u can always edit yours
				if (userProfile.getUserID().equals(
				    policyProfile.getUserProfile().getUserID())) {
					return true;
				}

				// if the user is not assigned to any user group then accept it.
				if (userProfile.getMembershipData().getMemberUserGroupName().isEmpty()) {
					return true;
				}

				// u can edit all user profile that created by u or your member.
				if (HierarchicalContainmentPolicy.INSTANCE
				    .isAdminOfUser(policyProfile.getUserProfile().getUserID(),
				        userProfile.getUserID())
				    || HierarchicalContainmentPolicy.INSTANCE
				        .isDirectMemberOfUser(
				            policyProfile.getUserProfile().getUserID(),
				            userProfile.getUserID())) {
					return true;
				}

				error = "Edit access failed on userID: "
				    + policyProfile.getUserProfile().getUserID()
				    + " with access type of " + userGroupType.toString()
				    + " on user profile: " + userProfile.getUserID()
				    + ". Reason: you can only edit profiles of (direct) members.";

				log.debug(error);

				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_EDIT_ACCESS, new Object[] { userProfile.getUserID() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

			}
			if (request.getType().equals(PolicyRequest.CommandType.DELETE)) {
				log.debug("Check delete access");

				// u can delete a user if he does not belong to any group
				if (userProfile.getMembershipData().getMemberUserGroupName() == null
				    || userProfile.getMembershipData().getMemberUserGroupName()
				        .isEmpty()) {
					return true;
				}

				error = "Delete access failed on userID: "
				    + policyProfile.getUserProfile().getUserID()
				    + " with access type of "
				    + userGroupType.toString()
				    + " on user profile: "
				    + userProfile.getUserID()
				    + ". Reason: User still belongs to the following groups: "
				    + userProfile.getMembershipData().getMemberUserGroupName()
				        .toString();
				log.error(error);

				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_DELETE_ACCESS,
				    new Object[] {
				        userProfile.getUserID(),
				        userProfile.getMembershipData().getMemberUserGroupName()
				            .toString() });

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
			}

		}

		if (request.getRequestor() instanceof UserGroupProfile) {

			UserGroupProfile ugp = (UserGroupProfile) request.getRequestor();
			String userID = policyProfile.getUserProfile().getUserID();
			String policyCreator = ugp.getLastModificationUserID();

			if (policyCreator == null) {
				log.debug("Policy check failed on userID: " + userID + " vs "
				    + policyCreator);
				throw new DracPolicyException(
				    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
				    new Object[] { "the policy last modifier should not be null" });
			}

			/*
			 * u can read what u created or what created by your direct members, and
			 * by your indirect members.
			 */

			log.debug("Policy check on usergroup: " + ugp.getName());
			if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				log.debug("Check read access");
				if (userID.equals(policyCreator)
				    || HierarchicalContainmentPolicy.INSTANCE.isMemberOfUserGroup(
				        userID, ugp.getName())
				    || HierarchicalContainmentPolicy.INSTANCE.isAdminOfUserGroup(
				        policyProfile.getUserProfile().getUserID(),
				        new UserGroupName(ugp.getMembership().getCreatedByGroupName()))) {
					return true;
				}

				error = "Read access failed on userID: "
				    + userID
				    + " with access type of "
				    + userGroupType.toString()
				    + " on user group profile: "
				    + ugp.getName()
				    + ". Reason: you can only read the group profiles of (direct) members.";

				log.debug(error);
				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_READGROUP_ACCESS, new Object[] { ugp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

			}

			if (request.getType().equals(PolicyRequest.CommandType.WRITE)
			    || request.getType().equals(PolicyRequest.CommandType.CREATE)) {

				log.debug("Check create access");
				if (HierarchicalContainmentPolicy.INSTANCE.isMemberOfUserGroup(
				    userID, ugp.getCreatedByGroupName())
				    || HierarchicalContainmentPolicy.INSTANCE.isAdminOfUserGroup(
				        policyProfile.getUserProfile().getUserID(),
				        new UserGroupName(ugp.getMembership().getCreatedByGroupName()))) {
					return true;
				}

				error = " Creation access failed on userID: "
				    + userID
				    + " with access type of "
				    + userGroupType.toString()
				    + " on user group profile: "
				    + ugp.getName()
				    + ". Reason: you are not a direct member of the parent user group: "
				    + ugp.getMembership().getCreatedByGroupName();
				log.debug(error);
				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_CREATEGROUP_ACCESS,
				    new Object[] { ugp.getMembership().getCreatedByGroupName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
			}

			if (request.getType().equals(PolicyRequest.CommandType.EDIT)) {

				// u can edit a user group if it is belonged to your direct domain
				log.debug("Check edit access");
				/*
				 * if(HierachicalContainmentPolicy.INSTANCE .isMemberOfUserGroup(
				 * userID, ugp.getName()) || HierachicalContainmentPolicy.INSTANCE
				 * .isAdminOfUserGroup( policyProfile.getUserProfile().getUserID(),
				 * ugp.getMembership().getCreatedByGroupName())){ return true; }
				 */

				if (HierarchicalContainmentPolicy.INSTANCE.isAdminOfUserGroup(
				    policyProfile.getUserProfile().getUserID(),
				    new UserGroupName(ugp.getMembership().getCreatedByGroupName()))) {
					return true;
				}

				error = " Edit access failed on userID: "
				    + userID
				    + " with access type of "
				    + userGroupType.toString()
				    + " on user group profile: "
				    + ugp.getName()
				    + ". Reason: you can only edit the group profiles of (direct) members.";
				log.error(error);

				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_EDITGROUP_ACCESS, new Object[] { ugp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

			}
			if (request.getType().equals(PolicyRequest.CommandType.DELETE)) {

				log.debug("Check delete access on " + ugp.toString());

				// if u are not at the leaf then return exception
				if (!HierarchicalContainmentPolicy.INSTANCE.isUserGroupALeaf(ugp)) {
					error = " Deletion access failed for user: " + userID
					    + " with access type of: " + userGroupType.toString()
					    + " on user group: " + ugp.getName()
					    + ". Reason: The usergroup probably has child groups";

					log.error(error);

					String msg = ExceptionFormatter.INSTANCE.formatMessage(
					    ERROR_DELETEGROUP_CHILDREN,
					    new Object[] { ugp.getName(),
					        ugp.getMembership().getMemberUserGroupName().toString() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

				}

				if (HierarchicalContainmentPolicy.INSTANCE.isUserGroupARoot(ugp)) {

					error = " Deletion access failed for userID: " + userID
					    + " with access type of " + userGroupType.toString()
					    + " on resource group profile: " + ugp.getName()
					    + ". Reason: The group probably is the root";
					log.error(error);

					String msg = ExceptionFormatter.INSTANCE.formatMessage(
					    ERROR_DELETEGROUP_ROOT, new Object[] { ugp.getName() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

				}

				// u can delete a user group if it is belonged to your direct domain
				if (HierarchicalContainmentPolicy.INSTANCE.isAdminOfUserGroup(
				    policyProfile.getUserProfile().getUserID(),
				    new UserGroupName(ugp.getMembership().getCreatedByGroupName()))) {
					return true;
				}

				error = " Deletion access failed for userID: "
				    + userID
				    + " with access type of "
				    + userGroupType.toString()
				    + " on user group profile: "
				    + ugp.getName()
				    + ". Reason: This user does not have delete priviledge to the resource group";

				log.debug(error);

				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_DELETEGROUP_ACCESS, new Object[] { ugp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
			}

			throw new DracPolicyException(
			    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
			    new Object[] { "invalid command type: "
			        + request.getType().toString() });

		}

		if (request.getRequestor() instanceof ResourceGroupProfile) {

			ResourceGroupProfile rgp = (ResourceGroupProfile) request.getRequestor();
			String userID = policyProfile.getUserProfile().getUserID();
			String policyCreator = rgp.getLastModificationUserID();

			if (policyCreator == null) {
				log.error("Policy check failed on userID: " + userID + " vs "
				    + policyCreator);
				throw new DracPolicyException(
				    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
				    new Object[] { "the policy last modifier should not be null" });
			}

			log.debug("Policy check with resource group: " + rgp.getName());

			if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				log.debug("Check read access");
				// u can read what u and your member are assigned to.

				if (userID.equals(policyCreator)) {
					return true;
				}

				if (HierarchicalContainmentPolicy.INSTANCE
				    .hasThisUserAccessResourceGroupIncludingChildren(userID,
				        rgp.getName())) {
					return true;
				}

				error = " Read access failed on userID: "
				    + userID
				    + " with access type of "
				    + userGroupType.toString()
				    + " on resource group profile: "
				    + rgp.getName()
				    + ". Reason: you can only read resource profiles of (direct) members";
				log.debug(error);

				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_READRESGROUP_ACCESS, new Object[] { rgp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
			}

			if (request.getType().equals(PolicyRequest.CommandType.SET_AS_PARENT)) {
				log.debug("Check parental access");
				if (userID.equals(policyCreator)) {
					log.debug("Creator of the resource group should be able to use them as parent for others");
					return true;
				}
				return HierarchicalContainmentPolicy.INSTANCE
				    .hasThisResourceGroupParentable(userID, rgp.getName());
			}

			if (request.getType().equals(PolicyRequest.CommandType.WRITE)
			    || request.getType().equals(PolicyRequest.CommandType.CREATE)) {
				// u can write if the resource parent is directly belonged to u, and
				// resource list of the resource must be a subset of the parent's
				// resource list.
				log.debug("Check create access");
				Node<ResourceGroupProfile> rgpOfParent = HierarchicalContainmentPolicy
				    .INSTANCE.getResourceGroupProfileNode(
				        rgp.getMembership().getCreatedByGroupName());

				if (rgpOfParent == null) {
					error = " Creation access failed on userID: " + userID
					    + " with access type of " + userGroupType.toString()
					    + " on resource group profile: " + rgp.getName()
					    + ". Reason: cannot find the parent resource group: "
					    + rgp.getMembership().getCreatedByGroupName();
					String msg = ExceptionFormatter.INSTANCE.formatMessage(
					    ERROR_CREATERESGROUP_PARENT,
					    new Object[] { rgp.getMembership().getCreatedByGroupName() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
				}

				if (!HierarchicalContainmentPolicy.INSTANCE
				    .hasThisUserAccessResourceGroupIncludingChildren(userID,
				        rgp.getMembership().getCreatedByGroupName())) {

					error = " Creation access failed on userID: " + userID
					    + " with access type of " + userGroupType.toString()
					    + " on resource group profile: " + rgp.getName()
					    + ". Reason: the parent resource group: "
					    + rgp.getMembership().getCreatedByGroupName()
					    + " is not assigned to his user group.";

					log.debug(error);
					String msg = ExceptionFormatter.INSTANCE.formatMessage(
					    ERROR_CREATERESGROUP_NOTASSIGNED,
					    new Object[] { rgp.getMembership().getCreatedByGroupName() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
				}

				if (rgpOfParent.getData().getDefaultResourceGroup()
				    .equals(Boolean.TRUE)) {
					return true;

				}

				if (rgp.getResourceList().isEmpty()) {
					return true;
				}

				if (hasResourcesContainAllInHisParent(rgpOfParent.getData(), rgp)) {
					return true;
				}

				error = " Creation access failed on userID: " + userID
				    + " with access type of " + userGroupType.toString()
				    + " on resource group profile: " + rgp.getName()
				    + ". Reason: the parent "
				    + rgp.getMembership().getCreatedByGroupName()
				    + " resource list does not cover all resources from the child.";

				log.debug(error);
				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_CREATERESGROUP_NORESOURCE,
				    new Object[] { rgp.getMembership().getCreatedByGroupName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

			}

			if (request.getType().equals(PolicyRequest.CommandType.EDIT)) {

				log.debug("Check edit access");
				if (userID.equals(policyCreator)) {
					log.error("Creator should be able to edit this");
					return true;
				}

				if (HierarchicalContainmentPolicy.INSTANCE
				    .hasThisUserAccessChildrenResourceGroup(userID, rgp.getName())) {
					return true;
				}

				error = " Edit access failed on userID: " + userID
				    + " with access type of " + userGroupType.toString()
				    + " on resource group profile: " + rgp.getName()
				    + ". Reason: This user do not have access to the resource group";

				log.debug(error);
				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_EDITRESGROUP, new Object[] { rgp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
			}

			if (request.getType().equals(PolicyRequest.CommandType.DELETE)) {

				log.debug("Check delete access");
				if (userID.equals(policyCreator)) {
					log.error("Creator should be able to delete this");
					return true;
				}
				// group admin should only delete what sub resource-groups only

				// if u are not at the leaf then return exception
				if (!HierarchicalContainmentPolicy.INSTANCE.isResourceGroupALeaf(
				    rgp)) {

					error = " Deletion access failed for user: " + userID
					    + " with access type of: " + userGroupType.toString()
					    + " on resource group: " + rgp.getName()
					    + ". Reason: the group has child groups";

					log.error(error);
					String msg = ExceptionFormatter.INSTANCE
					    .formatMessage(
					        ERROR_DELETERESGROUP_CHILDREN,
					        new Object[] {
					            rgp.getName(),
					            rgp.getMembership().getMemberResourceGroupName()
					                .toString() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

				}

				if (HierarchicalContainmentPolicy.INSTANCE.isResourceGroupARoot(
				    rgp)) {

					error = " Deletion access failed for userID: " + userID
					    + " with access type of " + userGroupType.toString()
					    + " on resource group profile: " + rgp.getName()
					    + ". Reason: The group probably is the root";

					log.debug(error);
					String msg = ExceptionFormatter.INSTANCE.formatMessage(
					    ERROR_DELETERESGROUP_ROOT, new Object[] { rgp.getName() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
				}

				if (!HierarchicalContainmentPolicy.INSTANCE
				    .hasThisUserDirectMemberOfResourceGroup(userID, rgp.getName())
				    && HierarchicalContainmentPolicy.INSTANCE
				        .hasThisUserAccessResourceGroupIncludingChildren(userID,
				            rgp.getName())) {
					return true;
				}

				error = " Deletion failed for userID: "
				    + userID
				    + " with access type of "
				    + userGroupType.toString()
				    + " on resource group profile: "
				    + rgp.getName()
				    + ". Reason: This user do not have delete priviledge to the resource group";

				log.debug(error);
				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_DELETERESGROUP_ACCESS, new Object[] { rgp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
			}

			throw new DracPolicyException(
			    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
			    new Object[] { "invalid command type: "
			        + request.getType().toString() });

		}

		if (request.getRequestor() instanceof GlobalPolicy
		    || request.getRequestor() instanceof GroupPolicy
		    || request.getRequestor() instanceof ResourcePolicy) {
			// return true in all cases, or are we counting on an exception to be
			// thrown
			return true;
		}
		throw new DracPolicyException(
		    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
		    new Object[] { "invalid command type: " + request.getType().toString() });

	}

	/**
     * 
     */
	private static boolean evalSysAdmin(UserPolicyProfile policyProfile,
	    PolicyRequest request) throws Exception {
		UserGroupType userGroupType = policyProfile.getUserGroupType();
		String error;
		/** ******************************************************************** */
		/* SYS ADMIN CHECKING */
		/** ******************************************************************** */

		String userId = policyProfile.getUserProfile().getUserID();

		if (request.getRequestor() instanceof UserProfile) {

			UserProfile userProfile = (UserProfile) request.getRequestor();

			if (request.getType().equals(PolicyRequest.CommandType.DELETE)) {

				log.debug("Check delete access");

				// u can delete a user if he does not belong to any group
				if (userProfile.getMembershipData().getMemberUserGroupName() == null
				    || userProfile.getMembershipData().getMemberUserGroupName()
				        .isEmpty()) {
					return true;
				}

				error = "Delete access failed on userID: "
				    + policyProfile.getUserProfile().getUserID()
				    + " with access type of "
				    + userGroupType.toString()
				    + " on user profile: "
				    + userProfile.getUserID()
				    + ". Reason: user still belongs to groups: "
				    + userProfile.getMembershipData().getMemberUserGroupName()
				        .toString();
				log.error(error);

				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_DELETE_ACCESS,
				    new Object[] {
				        userProfile.getUserID(),
				        userProfile.getMembershipData().getMemberUserGroupName()
				            .toString() });

				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
			}

			return true;

		}
		else if (request.getRequestor() instanceof UserGroupProfile) {

			UserGroupProfile ugp = (UserGroupProfile) request.getRequestor();
			String policyCreator = ugp.getLastModificationUserID();

			if (request.getType().equals(PolicyRequest.CommandType.READ)) {

				if (policyCreator == null) {
					log.debug("Policy check failed on userID: " + userId + " vs null");
					throw new DracPolicyException(
					    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
					    new Object[] { "the policy last modifier should not be null" });
				}

				// u can read what u created or
				// what created by your direct members,
				// and by your indirect members.

				log.debug("Check read access on: " + ugp.getName());
				if (userId.equals(policyCreator)
				    || HierarchicalContainmentPolicy.INSTANCE.isMemberOfUserGroup(
				        userId, ugp.getName())
				    || HierarchicalContainmentPolicy.INSTANCE.isAdminOfUserGroup(
				        policyProfile.getUserProfile().getUserID(),
				        new UserGroupName(ugp.getMembership().getCreatedByGroupName()))) {
					return true;
				}

				error = "Read access failed on userID: "
				    + userId
				    + " with access type of "
				    + userGroupType.toString()
				    + " on user group profile: "
				    + ugp.getName()
				    + ". Reason: you can only read the group profiles of (direct) members";

				log.debug(error);
				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_READGROUP_ACCESS, new Object[] { ugp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

			}
			else if (request.getType().equals(PolicyRequest.CommandType.EDIT)) {

				log.debug("Check if default admin");
				if (HierarchicalContainmentPolicy.INSTANCE
				    .isUserMemberOfDefaultAdminGroup(userId)) {
					return true;
				}

				// u can edit a user group if it is belonged to your direct domain
				if (HierarchicalContainmentPolicy.INSTANCE.isAdminOfUserGroup(
				    policyProfile.getUserProfile().getUserID(),
				    new UserGroupName(ugp.getMembership().getCreatedByGroupName()))) {
					return true;
				}

				error = " Edit access failed on userID: "
				    + userId
				    + " with access type of "
				    + userGroupType.toString()
				    + " on user group profile: "
				    + ugp.getName()
				    + ". Reason: you can only edit the group profiles of (direct) members";
				log.error(error);

				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_EDITGROUP_ACCESS, new Object[] { ugp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

			}
			else if (request.getType().equals(PolicyRequest.CommandType.DELETE)) {

				log.debug("Check delete access");
				if (!HierarchicalContainmentPolicy.INSTANCE.isUserGroupALeaf(ugp)) {

					error = " Deletion access failed for userID: " + userId
					    + " with access type of " + userGroupType.toString()
					    + " on resource group profile: " + ugp.getName()
					    + ". Reason: The group probably has child groups";

					log.debug(error);
					String msg = ExceptionFormatter.INSTANCE.formatMessage(
					    ERROR_DELETEGROUP_CHILDREN,
					    new Object[] { ugp.getName(),
					        ugp.getMembership().getMemberUserGroupName().toString() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

				}

				if (HierarchicalContainmentPolicy.INSTANCE.isUserGroupARoot(ugp)) {

					error = " Deletion access failed for userID: " + userId
					    + " with access type of " + userGroupType.toString()
					    + " on resource group profile: " + ugp.getName()
					    + ". Reason: The group probably is the root";

					log.debug(error);
					String msg = ExceptionFormatter.INSTANCE.formatMessage(
					    ERROR_DELETEGROUP_ROOT, new Object[] { ugp.getName() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
				}

				return true;
			}

			// everything else then true, in this case it is "create or write"
			return true;

		}
		else if (request.getRequestor() instanceof ResourceGroupProfile) {

			ResourceGroupProfile rgp = (ResourceGroupProfile) request.getRequestor();
			String policyCreator = rgp.getLastModificationUserID();

			if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				log.debug("Check read access");
				// u can read what u and your member are assigned to.

				if (userId.equals(policyCreator)) {
					return true;
				}

				if (HierarchicalContainmentPolicy.INSTANCE
				    .hasThisUserAccessResourceGroupIncludingChildren(userId,
				        rgp.getName())) {
					return true;
				}

				error = " Read access failed on userID: "
				    + userId
				    + " with access type of "
				    + userGroupType.toString()
				    + " on resource group profile: "
				    + rgp.getName()
				    + ". Reason: you can only read the resource profiles of (direct) members";
				log.debug(error);

				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_READRESGROUP_ACCESS, new Object[] { rgp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

			}
			else if (request.getType()
			    .equals(PolicyRequest.CommandType.SET_AS_PARENT)) {
				log.debug("Check parental access");
				if (userId.equals(policyCreator)) {
					log.debug("Creator of the resource group should be able to use them as parent for others");
					return true;
				}

				return HierarchicalContainmentPolicy.INSTANCE
				    .hasThisResourceGroupParentable(userId, rgp.getName());

			}
			else if (request.getType().equals(PolicyRequest.CommandType.EDIT)) {

				log.debug("Check edit access");
				log.debug("Check if default admin");
				if (HierarchicalContainmentPolicy.INSTANCE
				    .isUserMemberOfDefaultAdminGroup(userId)) {
					return true;
				}

				if (userId.equals(policyCreator)) {
					log.debug("Creator should be able to edit this");
					return true;
				}

				// Preferably, using this one but need to fix a bug
				if (HierarchicalContainmentPolicy.INSTANCE
				    .hasThisUserAccessChildrenResourceGroup(userId, rgp.getName())) {
					return true;
				}

				error = " Edit access failed on userID: " + userId
				    + " with access type of " + userGroupType.toString()
				    + " on resource group profile: " + rgp.getName()
				    + ". Reason: This user do not have access to the resource group";

				log.debug(error);
				String msg = ExceptionFormatter.INSTANCE.formatMessage(
				    ERROR_EDITRESGROUP, new Object[] { rgp.getName() });
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

			}
			else if (request.getType().equals(PolicyRequest.CommandType.DELETE)) {

				log.debug("Check delete access");
				log.debug("Check if leaf ");
				if (!HierarchicalContainmentPolicy.INSTANCE.isResourceGroupALeaf(
				    rgp)) {

					error = " Deletion access failed for userID: " + userId
					    + " with access type of " + userGroupType.toString()
					    + " on resource group profile: " + rgp.getName()
					    + ". Reason: The group probably has sub-groups";

					log.debug(error);
					String msg = ExceptionFormatter.INSTANCE
					    .formatMessage(
					        ERROR_DELETERESGROUP_CHILDREN,
					        new Object[] {
					            rgp.getName(),
					            rgp.getMembership().getMemberResourceGroupName()
					                .toString() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });
				}

				log.debug("Check if root ");
				if (HierarchicalContainmentPolicy.INSTANCE.isResourceGroupARoot(
				    rgp)) {

					error = " Deletion access failed for userID: " + userId
					    + " with access type of " + userGroupType.toString()
					    + " on resource group profile: " + rgp.getName()
					    + ". Reason: The group probably is the root";

					log.debug(error);
					String msg = ExceptionFormatter.INSTANCE.formatMessage(
					    ERROR_DELETERESGROUP_ROOT, new Object[] { rgp.getName() });
					throw new DracPolicyException(
					    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
					    new Object[] { policyProfile.getUserProfile().getUserID(), msg });

				}

				return true;
			}

			// everything else return true; i.e. WRITE/CREATE
			return true;

		}
		else {
			return true;
		}
	}

	/**
     * 
     */
	private static boolean evalUser(UserPolicyProfile policyProfile,
	    PolicyRequest request) throws Exception {
		UserGroupType userGroupType = policyProfile.getUserGroupType();

		/** *********************************************************** */
		/* Beeing able see and view everything belonged to himself */
		/** *********************************************************** */

		if (request.getRequestor() instanceof UserProfile) {
			UserProfile userProfile = (UserProfile) request.getRequestor();

			if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				log.debug("Check read access");
				if (userProfile.getUserID().equals(
				    policyProfile.getUserProfile().getUserID())) {
					return true;
				}
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_READ_ACCESS_CONTROL_USER_TYPE,
				    new Object[] {
				        "user profile: " + policyProfile.getUserProfile().getUserID(),
				        userGroupType.toString() });

			}
			else if (request.getType().equals(PolicyRequest.CommandType.EDIT)) {
				if (userProfile.getUserID().equals(
				    policyProfile.getUserProfile().getUserID())) {
					return true;
				}
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] {
				        "user profile: " + policyProfile.getUserProfile().getUserID(),
				        userGroupType.toString() });
			}
			else {
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL,
				    new Object[] {
				        "user profile: " + policyProfile.getUserProfile().getUserID(),
				        userGroupType.toString() });
			}
		}

		if (request.getRequestor() instanceof UserGroupProfile) {

			if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				log.debug("Check read access");
				if (policyProfile.hasBelongedToUserGroup((UserGroupProfile) request
				    .getRequestor())) {
					return true;
				}
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_READ_ACCESS_CONTROL_USER_TYPE,
				    new Object[] {
				        "user profile: " + policyProfile.getUserProfile().getUserID(),
				        userGroupType.toString() });
			}

			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL, new Object[] {
			        "user profile: " + policyProfile.getUserProfile().getUserID(),
			        userGroupType.toString() });

		}

		if (request.getRequestor() instanceof ResourceGroupProfile) {
			if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				log.debug("Check read access");
				if (policyProfile.containResourceGroup((ResourceGroupProfile) request
				    .getRequestor())) {
					return true;
				}
				throw new DracPolicyException(
				    DracErrorConstants.POLICY_ERROR_READ_ACCESS_CONTROL_USER_TYPE,
				    new Object[] {
				        "user profile: " + policyProfile.getUserProfile().getUserID(),
				        userGroupType.toString() });

			}

			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL, new Object[] {
			        "user profile: " + policyProfile.getUserProfile().getUserID(),
			        userGroupType.toString() });

		}

		if (request.getRequestor() instanceof GlobalPolicy
		    || request.getRequestor() instanceof GroupPolicy
		    || request.getRequestor() instanceof ResourcePolicy) {

			if (request.getType().equals(PolicyRequest.CommandType.READ)) {
				return true;
			}

			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_READ_ACCESS_CONTROL_USER_TYPE,
			    new Object[] {
			        "user profile: " + policyProfile.getUserProfile().getUserID(),
			        userGroupType.toString() });
		}
		throw new DracPolicyException(
		    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
		    new Object[] { "invalid command type: " + request.getType().toString() });
	}

	private static boolean hasResourcesContainAllInHisParent(
	    ResourceGroupProfile parentRG, ResourceGroupProfile rgp) {
		boolean hit = false;
		for (Resource resource : rgp.getResourceList()) {
			hit = false;
			String resourceID = resource.getResourceID();
			log.debug("Check : " + resourceID);
			for (Resource pResource : parentRG.getResourceList()) {
				String pResourceID = pResource.getResourceID();
				if (resourceID.equals(pResourceID)) {
					log.debug("Check hit true");
					hit = true;
					break;
				}
			}
			if (!hit) {
				return false;
			}
		}
		return hit;
	}
}
