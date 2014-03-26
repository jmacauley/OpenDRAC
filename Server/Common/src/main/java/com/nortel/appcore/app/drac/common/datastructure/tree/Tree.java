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

package com.nortel.appcore.app.drac.common.datastructure.tree;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Tree<T> {
  protected final Logger log = LoggerFactory.getLogger(getClass());
  private Node<T> root;

  public Tree() {
    super();
  }

  public Node<T> findNodeById(String id, Node<T> fromNode) throws Exception {

    // 

    if (fromNode.getId().equals(id)) {
      // 
      return fromNode;
    }

    /*************************************************
     * Visit the root first; and then traverse the left subtree; and then
     * traverse the right subtree (or vice versa).
     *************************************************/
    Node<T> result = null;
    for (Node<T> node : fromNode.getChildren()) {
      result = findNodeById(id, node);
      if (result != null) {
        return result;
      }
    }

    return result;
  }

  public Node<T> findNodeByIdFromRoot(String id) throws Exception {

    return findNodeById(id, this.getRoot());

    /*
     * log.debug("Matching node: " + (result == null ? "null" :
     * result.getId())); return result;
     */

  }

  public Node<T> getRoot() {
    return this.root;
  }

  public abstract String getRootName();

  public boolean isRootNode(String profileID) {
    if (profileID != null) {
      return profileID.equals(getRootName());
    }
    return false;
  }

  public void setRoot(Node<T> node) {
    this.root = node;
  }

  public List<Node<T>> toList() {
    List<Node<T>> list = new ArrayList<Node<T>>();
    dfsTraverse(root, list);
    return list;
  }

  /**
   * Returns a String representation of the Tree. The elements are generated
   * from a dfs fashion.
   * 
   * @return the String representation of the Tree.
   */
  @Override
  public String toString() {
    return toList().toString();
  }

  public List<Node<T>> traverseUpRootToList(Node<T> node) {

    List<Node<T>> list = new ArrayList<Node<T>>();

    traverseUpToRoot(node, list);

    return list;
  }

  /**
   * Traverse up the tree to visit parent, and all ancestors. This is a
   * recursive method
   * 
   * @param element
   *          the starting element.
   * @param list
   *          the output of the walk.
   */
  public void traverseUpToRoot(Node<T> element, List<Node<T>> list) {

    // add your self first to the list
    list.add(element);

    // recursively against the parent attribute.
    Node<T> parent = element.getParent();

    if (parent != null) {
      traverseUpToRoot(parent, list);
    } // else we hit the root probably, or else we hit stack flow exception.

  }

  /**
   * Traverse the tree in dfs fashion. This is a recursive method, and is called
   * from the toList() method with the root element as the first argument.
   * 
   * @param element
   *          the starting element.
   * @param list
   *          the output of the walk.
   */
  private void dfsTraverse(Node<T> element, List<Node<T>> list) {

    list.add(element);

    /*************************************************
     * Visit the root first; and then traverse the left subtree; and then
     * traverse the right subtree (or vice versa).
     *************************************************/

    for (Node<T> data : element.getChildren()) {
      dfsTraverse(data, list);
    }

  }
}
