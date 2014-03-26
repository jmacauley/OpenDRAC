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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.datastructure.tree.Node;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;

/**
 * @author VNGUYEN A singleton to encapsulate the policy trees: user group and
 *         resource group. This class should allows ways to re-init the trees.
 *         This class should provide additional behavior on top on the existing
 *         tree structures for policing purpose. It should make use of the
 *         structure to alleviate the complexity for policing.
 */

public enum HierarchicalContainmentPolicy {

	INSTANCE;

	private static final long serialVersionUID = 1L;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/******************************************************/
	/* Singleton structure */
	/******************************************************/
	private UserGroupProfileTree ugTree;
	private ResourceGroupProfileTree rgTree;

	private HierarchicalContainmentPolicy() {
		ugTree = new UserGroupProfileTree();
		rgTree = new ResourceGroupProfileTree();
	}

	public UserGroupType computeUserGroupLevel(String userId,
	    UserGroupName userGroupId) throws Exception {
		UserGroupType ugLevel = UserGroupType.USER;

		if (userGroupId == null || userGroupId.toString().equals("")) {
			
			return UserGroupType.USER;
		}

		Node<UserGroupProfile> userGroupProfileNode = getUserGroupProfileTree()
		    .findNodeByIdFromRoot(userGroupId.toString());

		if (userGroupProfileNode == null) {
			
			return ugLevel;
		}

		ugLevel = userGroupProfileNode.getData().getUserGroupType();

		ugLevel = computeUserGroupLevel_0(userGroupProfileNode.getParent(), userId,
		    ugLevel);

		
		return ugLevel;

	}

	public Node<ResourceGroupProfile> getResourceGroupProfileNode(
	    String resourcegroupId) throws Exception {
		return getResourceGroupProfileTree().findNodeByIdFromRoot(resourcegroupId);
	}

	public ResourceGroupProfileTree getResourceGroupProfileTree()
	    throws Exception {
		if (rgTree == null) {
			rgTree = new ResourceGroupProfileTree();
		}

		return rgTree;
	}

	public Node<UserGroupProfile> getUserGroupProfileNode(
	    UserGroupName userGroupId) throws Exception {
		return getUserGroupProfileTree().findNodeByIdFromRoot(
		    userGroupId.toString());
	}

	public UserGroupProfileTree getUserGroupProfileTree() throws Exception {
		if (ugTree == null) {
			ugTree = new UserGroupProfileTree();
		}
		return ugTree;
	}

	/*****************************************************************************/
	/* Return the list of all nodes created by a user and his members */
	/* Tenically, find all group in the sub tree where a user is amember of */
	/*****************************************************************************/
	public boolean hasThisResourceGroupParentable(String userId,
	    String resourceGroupName) throws Exception {
		log.debug("HierachicalContainmentPolicy hasThisResourceGroupParentable ...");
		List<Node<UserGroupProfile>> ugpList = new ArrayList<Node<UserGroupProfile>>();
		findAllNodesAUserIsMemberOf(userId, ugpList, getUserGroupProfileTree()
		    .getRoot());

		List<UserGroupProfile> subList;

		for (Node<UserGroupProfile> node : ugpList) {
			subList = new ArrayList<UserGroupProfile>();
			findAllDescendantOfNode(subList, node);

			for (UserGroupProfile ugp : subList) {
				// we scan all resource groups to check if contain given resource group
				
				for (String thisResourceGroup : ugp.getMembership()
				    .getMemberResourceGroupName()) {
					log.debug("Check if " + thisResourceGroup + " matches: "
					    + resourceGroupName);
					if (thisResourceGroup.equals(resourceGroupName)) {
						UserGroupType ugType = computeUserGroupLevel(userId, ugp.getName());
						if (!ugType.equals(UserGroupType.USER)) {
							return true;
						}
					}

					// just added this code
					// to make sure it can read all resource groups created by the
					// resource group directly
					// assigned to a user group
					List<ResourceGroupProfile> resourceGroupList = null;
					Node<ResourceGroupProfile> resourceGroupNode = null;
					resourceGroupList = new ArrayList<ResourceGroupProfile>();
					resourceGroupNode = getResourceGroupProfileNode(thisResourceGroup);
					if (node != null) {
						findAllDescendantOfResourceGroupNode(resourceGroupList,
						    resourceGroupNode);
						for (ResourceGroupProfile rgp : resourceGroupList) {
							
							if (rgp.getName().equals(resourceGroupName)) {
								UserGroupType ugType = computeUserGroupLevel(userId,
								    ugp.getName());
								if (!ugType.equals(UserGroupType.USER)) {
									return true;
								}
							}
						}
					}

				}
			}
		}

		return false;
	}

	public boolean hasThisUserAccessChildrenResourceGroup(String userId,
	    String resourceGroupName) throws Exception {
		log.debug("HierachicalContainmentPolicy hasThisUserAccessThisChildrenResourceGroup ...");
		List<Node<UserGroupProfile>> ugpList = new ArrayList<Node<UserGroupProfile>>();
		findAllNodesAUserIsMemberOf(userId, ugpList, getUserGroupProfileTree()
		    .getRoot());

		List<UserGroupProfile> subList;

		for (Node<UserGroupProfile> node : ugpList) {
			subList = new ArrayList<UserGroupProfile>();

			findAllDescendantOfNode(subList, node);

			for (UserGroupProfile ugp : subList) {
				// we scan all resource groups to check if contain given resource group
				
				for (String thisResourceGroup : ugp.getMembership()
				    .getMemberResourceGroupName()) {

					List<ResourceGroupProfile> resourceGroupList = new ArrayList<ResourceGroupProfile>();
					Node<ResourceGroupProfile> resourceGroupNode = getResourceGroupProfileNode(thisResourceGroup);

					if (node != null) {
						log.debug("findAllResourceDescendantOfNode: "
						    + resourceGroupNode.getData().getName());
						findAllAbsoluteDescendantOfResourceGroupNode(resourceGroupList,
						    resourceGroupNode);

						for (ResourceGroupProfile rgp : resourceGroupList) {
							log.debug("Check if " + rgp.getName() + " matches: "
							    + resourceGroupName);
							if (rgp.getName().equals(resourceGroupName)) {
								return true;
							}
						}
					}

				}
			}
		}
		return false;
	}

	public boolean hasThisUserAccessResource(String userId, String resourceId)
	    throws Exception {
		log.debug("HierachicalContainmentPolicy hasThisUserAccessResource...");
		List<Node<UserGroupProfile>> ugpList = new ArrayList<Node<UserGroupProfile>>();
		findAllNodesAUserIsMemberOf(userId, ugpList, getUserGroupProfileTree()
		    .getRoot());

		List<UserGroupProfile> subList;
		for (Node<UserGroupProfile> node : ugpList) {
			subList = new ArrayList<UserGroupProfile>();
			
			
			findAllDescendantOfNode(subList, node);

			for (UserGroupProfile ugp : subList) {
				// we scan all resource groups to check if contain given resource group
				
				for (String thisResourceGroup : ugp.getMembership()
				    .getMemberResourceGroupName()) {
					log.debug("Check if " + thisResourceGroup + " contains resourceid: "
					    + resourceId);
					Node<ResourceGroupProfile> rgNode = getResourceGroupProfileNode(thisResourceGroup);
					if (rgNode == null) {
						log.debug("Resource group node should not be null: "
						    + thisResourceGroup);
					}
					else {

						// if the direct resource group contains resource ?
						ResourceGroupProfile rgp = rgNode.getData();
						if (rgp.containResource(resourceId)) {
							return true;
						}

						// Ok, if sub resource group contains resource ?
						List<ResourceGroupProfile> childrenResourceGroupList = new ArrayList<ResourceGroupProfile>();
						findAllDescendantOfResourceGroupNode(childrenResourceGroupList,
						    rgNode);
						for (ResourceGroupProfile childRGP : childrenResourceGroupList) {
							log.debug("Check if " + childRGP.getName()
							    + " contains resourceid: " + resourceId);
							if (childRGP.containResource(resourceId)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public boolean hasThisUserAccessResourceGroupIncludingChildren(String userId,
	    String resourceGroupName) throws Exception {
		return hasThisUserAccessResourceGroupIncludingChildren_0(userId,
		    resourceGroupName, true);
	}

	/*****************************************************************************/
	/* Return the list of all nodes created by a user and his members */
	/* Tenically, find all group in the sub tree where a user is amember of */
	/*****************************************************************************/
	public boolean hasThisUserDirectMemberOfResourceGroup(String userId,
	    String resourceGroupName) throws Exception {
		List<Node<UserGroupProfile>> ugpList = new ArrayList<Node<UserGroupProfile>>();
		findAllNodesAUserIsMemberOf(userId, ugpList, getUserGroupProfileTree()
		    .getRoot());

		for (Node<UserGroupProfile> node : ugpList) {
			UserGroupProfile ugp = node.getData();
			for (String thisResourceGroup : ugp.getMembership()
			    .getMemberResourceGroupName()) {
				if (thisResourceGroup.equals(resourceGroupName)) {
					return true;
				}
			}
		}
		return false;
	}

	public void invalidate() {
		log.debug("Containment invalidated ...");
		this.rgTree = null;
		this.ugTree = null;
	}

	public boolean isAdminOfUser(String adminId, String userId) throws Exception {
		// Find all node adminId is member of.
		List<Node<UserGroupProfile>> list = new ArrayList<Node<UserGroupProfile>>();
		findAllNodesAUserIsMemberOf(adminId, list, this.getUserGroupProfileTree()
		    .getRoot());

		// for each node, recursively scan each descendants to check if userId is
		// member of.
		// if any is true then return true.
		// else false

		for (Node<UserGroupProfile> ugp : list) {
			if (isAdminOfUser_0(ugp, userId)) {
				return true;
			}
		}

		return false;

	}

	/********************************************************************/
	/* Check if that userId is admin of creator of schedule/service */
	/********************************************************************/
	public boolean isAdminOfUserGroup(String userId, UserGroupName userGroupId)
	    throws Exception {
		if (userGroupId == null || userGroupId.toString().equals("")) {
			
			return false;
		}

		Node<UserGroupProfile> userGroupProfileNode = getUserGroupProfileTree()
		    .findNodeByIdFromRoot(userGroupId.toString());

		if (userGroupProfileNode == null) {
			
			return false;
		}
		return isAmdinOfUserGroup_0(userGroupProfileNode, userId);

	}

	public boolean isDirectMemberOfUser(String userID, String memberId)
	    throws Exception {

		// find all resourcegroup that both memberId, and userId is memberof if any
		// then true
		// Find all node userID is member of.
		List<Node<UserGroupProfile>> list = new ArrayList<Node<UserGroupProfile>>();
		findAllNodesAUserIsMemberOf(userID, list, this.getUserGroupProfileTree()
		    .getRoot());
		for (Node<UserGroupProfile> ugn : list) {
			UserGroupProfile ugp = ugn.getData();
			if (ugp.getMembership().getMemberUserID().contains(memberId)) {
				return true;
			}
		}

		return false;
	}

	/********************************************************************/
	/* Check if that userId is admin of creator of schedule/service */
	/********************************************************************/
	public boolean isMemberOfUserGroup(String userId, UserGroupName userGroupId)
	    throws Exception {
		log.debug("HierachicalContainment isMemberOfUserGroup");
		Node<UserGroupProfile> node = getUserGroupProfileTree()
		    .findNodeByIdFromRoot(userGroupId.toString());

		if (node == null) {
			
			return false;
		}

		UserGroupProfile ugProfile = node.getData();
		Set<String> userMembers = ugProfile.getMembership().getMemberUserID();

		if (userMembers.contains(userId)) {
			log.debug(userId + " is direct member of " + userGroupId);
			return true;
		}

		log.debug(userId + " is not a direct member of " + userGroupId);
		return false;

	}

	public boolean isResourceGroupALeaf(ResourceGroupProfile rgp)
	    throws Exception {

		if (rgp == null) {
			return false;
		}

		return getResourceGroupProfileTree().isLeafNode(rgp.getName());
	}

	public boolean isResourceGroupARoot(ResourceGroupProfile rgp)
	    throws Exception {

		if (rgp == null) {
			return false;
		}

		// we using isLeadNode(String) to ensure the node is in synch with db
		return getResourceGroupProfileTree().isRootNode(rgp.getName());
	}

	public boolean isUserGroupALeaf(UserGroupProfile ugp) throws Exception {
		if (ugp == null) {
			return false;
		}

		return getUserGroupProfileTree().isLeafNode(ugp.getName().toString());
	}

	public boolean isUserGroupARoot(UserGroupProfile ugp) throws Exception {
		if (ugp == null) {
			return false;
		}

		// we using isLeadNode(String) to ensure the node is in synch with db
		return getUserGroupProfileTree().isRootNode(ugp.getName().toString());
	}

	public boolean isUserMemberOfDefaultAdminGroup(String userId)
	    throws Exception {

		if (userId == null) {
			return false;
		}

		// we using isLeadNode(String) to ensure the node is in synch with db
		Node<UserGroupProfile> root = getUserGroupProfileTree().getRoot();

		if (root == null) {
			return false;
		}

		if (root.getData().getMembership().getMemberUserID().contains(userId)) {
			return true;
		}

		return false;
	}

	private UserGroupType computeUserGroupLevel_0(
	    Node<UserGroupProfile> userGroupProfileNode, String userId,
	    UserGroupType ugLevel) throws Exception {

		if (userGroupProfileNode == null) {
			return ugLevel;
		}

		UserGroupProfile ug = userGroupProfileNode.getData();

		if (ug.getMembership().getMemberUserID().contains(userId)) {
			ugLevel = ug.getUserGroupType();
			// 
		}

		return computeUserGroupLevel_0(userGroupProfileNode.getParent(), userId,
		    ugLevel);

	}

	private void findAllAbsoluteDescendantOfResourceGroupNode(
	    List<ResourceGroupProfile> list, Node<ResourceGroupProfile> node) {

		/*************************************************
		 * Visit the root first; and then traverse the left subtree; and then
		 * traverse the right subtree (or vice versa).
		 *************************************************/

		for (Node<ResourceGroupProfile> child : node.getChildren()) {
			
			list.add(child.getData());
			findAllDescendantOfResourceGroupNode(list, child);
		}
	}

	private void findAllDescendantOfNode(List<UserGroupProfile> list,
	    Node<UserGroupProfile> node) {
		list.add(node.getData());
		/*************************************************
		 * Visit the root first; and then traverse the left subtree; and then
		 * traverse the right subtree (or vice versa).
		 *************************************************/

		for (Node<UserGroupProfile> child : node.getChildren()) {
			
			findAllDescendantOfNode(list, child);
		}
	}

	private void findAllDescendantOfResourceGroupNode(
	    List<ResourceGroupProfile> list, Node<ResourceGroupProfile> node) {
		list.add(node.getData());
		/*************************************************
		 * Visit the root first; and then traverse the left subtree; and then
		 * traverse the right subtree (or vice versa).
		 *************************************************/

		for (Node<ResourceGroupProfile> child : node.getChildren()) {
			
			findAllDescendantOfResourceGroupNode(list, child);
		}
	}

	private void findAllNodesAUserIsMemberOf(String userId,
	    List<Node<UserGroupProfile>> ugpList, Node<UserGroupProfile> node) {

		UserGroupProfile ugp = node.getData();

		if (ugp.getMembership().getMemberUserID().contains(userId)) {
			
			ugpList.add(node);
		}

		/*************************************************
		 * Visit the root first; and then traverse the left subtree; and then
		 * traverse the right subtree (or vice versa).
		 *************************************************/

		for (Node<UserGroupProfile> child : node.getChildren()) {
			findAllNodesAUserIsMemberOf(userId, ugpList, child);
		}

	}

	/*****************************************************************************/
	/* Return the list of all nodes created by a user and his members */
	/* Tenically, find all group in the sub tree where a user is amember of */
	/*****************************************************************************/
	private boolean hasThisUserAccessResourceGroupIncludingChildren_0(
	    String userId, String resourceGroupName, boolean withChildren)
	    throws Exception {

		log.debug("HierachicalContainmentPolicy hasThisUserAccessResourceGroup ...");
		List<Node<UserGroupProfile>> ugpList = new ArrayList<Node<UserGroupProfile>>();
		findAllNodesAUserIsMemberOf(userId, ugpList, getUserGroupProfileTree()
		    .getRoot());

		List<UserGroupProfile> subList;

		for (Node<UserGroupProfile> node : ugpList) {
			subList = new ArrayList<UserGroupProfile>();
			
			findAllDescendantOfNode(subList, node);

			for (UserGroupProfile ugp : subList) {
				// we scan all resource groups to check if contain given resource group
				
				for (String thisResourceGroup : ugp.getMembership()
				    .getMemberResourceGroupName()) {
					log.debug("Check if " + thisResourceGroup + " matches: "
					    + resourceGroupName);
					if (thisResourceGroup.equals(resourceGroupName)) {
						return true;
					}

					// just added this code
					// to make sure it can read all resource groups created by the
					// resource group directly
					// assigned to a user group
					List<ResourceGroupProfile> resourceGroupList = null;
					Node<ResourceGroupProfile> resourceGroupNode = null;
					if (withChildren == true) {
						resourceGroupList = new ArrayList<ResourceGroupProfile>();
						resourceGroupNode = getResourceGroupProfileNode(thisResourceGroup);
						if (node != null) {

							findAllDescendantOfResourceGroupNode(resourceGroupList,
							    resourceGroupNode);
							for (ResourceGroupProfile rgp : resourceGroupList) {
								
								if (rgp.getName().equals(resourceGroupName)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/********************************************************************/
	/* Check if that userId is admin of creator of schedule/service */
	/********************************************************************/

	private boolean isAdminOfUser_0(Node<UserGroupProfile> ugp, String userId) {

		for (Node<UserGroupProfile> child : ugp.getChildren()) {
			if (child.getData().getMembership().getMemberUserID().contains(userId)) {
				return true;
			}
			if (isAdminOfUser_0(child, userId)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAmdinOfUserGroup_0(Node<UserGroupProfile> userGroupNode,
	    String userId) throws Exception {

		log.debug("HierarchicalContainment isAdminOfUserGroup ....");

		if (userGroupNode == null) { // probably hit the root
			return false;
		}

		UserGroupProfile ugp = userGroupNode.getData();
		log.debug("Processing : " + ugp.getName());
		Set<String> userMembers = ugp.getMembership().getMemberUserID();

		if (userMembers.contains(userId)) {
			log.debug("Processing : " + ugp.getName() + "is admin of " + userId);
			return true;
		}

		if (isAmdinOfUserGroup_0(userGroupNode.getParent(), userId)) {
			return true;
		}

		// else we hit the root probably, or else we hit stack flow exception.
		return false;
	}

}
