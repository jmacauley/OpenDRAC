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

import java.io.Serializable;
import java.util.List;

import com.nortel.appcore.app.drac.common.datastructure.tree.Node;
import com.nortel.appcore.app.drac.common.datastructure.tree.Tree;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbResourceGroupProfile;

public final class ResourceGroupProfileTree extends Tree<ResourceGroupProfile>
    implements Serializable {
  private static final long serialVersionUID = 1L;

  public ResourceGroupProfileTree() {
    super();
    try {
      initialize();
    }
    catch (Exception e) {
      log.error("Error: ", e);
      throw new RuntimeException("", e);
    }
  }

  @Override
  public String getRootName() {
    return DbResourceGroupProfile.ROOT_ID;
  }

  protected boolean isLeafNode(String profileID) {
    try {
      Node<ResourceGroupProfile> node = findNodeByIdFromRoot(profileID);

      if (node != null) {
        return isLeafNode(node.getData());
      }
    }
    catch (Exception e) {
      log.error("Cannot locate node: " + profileID, e);
    }
    return false;
  }

  private void auditTree(List<ResourceGroupProfile> list) {
    if (list.isEmpty()) {
      log.debug("UserGroupProfileTree does not need audit");
    }
    else {
      log.debug("Containment invalidate required audit");
      for (ResourceGroupProfile rgp : list) {
        log.error("ResourceGroupProfile: " + rgp.getName()
            + " needs audit with parent: "
            + rgp.getMembership().getCreatedByGroupName());
      }
    }
  }

  private ResourceGroupProfile getDataFromList(String id,
      List<ResourceGroupProfile> list) {
    for (ResourceGroupProfile group : list) {
      if (group.getName().equals(id)) {

        /*
         * just added this in for auditing; we expected at the end the list
         * should be empty Remove the root from list we don't need there anymore
         */
        list.remove(group);
        return group;
      }
    }
    return null;
  }

  private void initialize() throws Exception {
    initTree();
  }

  /******************************************************************/
  /* We need to load the tree from our usergroup collection */
  /* The tree should start with the "SystemAdmin user group rooted */
  /* node. The tree should be built by recursively dfs traverse */
  /* thru the membership/usergroupmember attribute */
  /******************************************************************/
  private void initTree() throws Exception {

    List<ResourceGroupProfile> resourceGroupList = DbResourceGroupProfile
        .INSTANCE.getResourceGroupProfileList();
    log.debug("There will be: " + resourceGroupList.size() + " members");

    // first we need to find the root.
    // The root node is the default SystemAdminGroup Node.
    ResourceGroupProfile rootProfile = getDataFromList(
        DbResourceGroupProfile.ROOT_ID, resourceGroupList);

    if (rootProfile == null) {
      throw new Exception("Cannot build tree because of no root");
    }

    // Remove the root from list we don't need there anymore
    resourceGroupList.remove(rootProfile);

    // now we are ready to rumble on the construction of the tree now.

    // 1 add root to tree.
    this.setRoot(new ResourceGroupProfileNode(rootProfile));
    log.debug("Traverse: " + rootProfile.getName());

    // build tree from the added root.
    initTreeZero(this.getRoot(), resourceGroupList);

    // just to verify if audit needed be
    auditTree(resourceGroupList);
  }

  private void initTreeZero(Node<ResourceGroupProfile> node,
      List<ResourceGroupProfile> list) throws Exception {

    log.debug("Start traverse recursively on: "
        + (node == null ? "null" : node.getData().getName()));

    if (node == null) {
      return; // just in case some unexpectedly happened here
    }

    ResourceGroupProfile profile = node.data;

    /*************************************************
     * Visit the root first; and then traverse the left subtree; and then
     * traverse the right subtree (or vice versa).
     *************************************************/

    for (String childId : profile.getMembership().getMemberResourceGroupName()) {
      ResourceGroupProfile child = getDataFromList(childId, list);

      if (child == null) { // orphaned group
        log.debug("Could not find user group: " + childId
            + " .Probably is duplicate");
        continue;
      }

      Node<ResourceGroupProfile> childNode = new ResourceGroupProfileNode(child);
      node.addChild(childNode);
      initTreeZero(childNode, list);
    }

  }

  private boolean isLeafNode(ResourceGroupProfile profile) {
    if (profile != null) {

      if (profile.getMembership().getMemberResourceGroupName() == null
          || profile.getMembership().getMemberResourceGroupName().isEmpty()) {
        return true;
      }
    }
    return false;
  }
}
