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

public class Node<T> {

  public T data;
  private List<Node<T>> children;
  private Node<T> parent;

  private Node<T> ROOT = null;

  // For serialization
  public Node() {
    super();
  }

  public Node(T data) {
    super();
    setData(data);
  }

  /**
   * Adds a child to the list of children for this Node<T>. The addition of the
   * first child will create a new List<Node<T>>.
   * 
   * @param child
   *          a Node<T> object to set.
   */
  public void addChild(Node<T> child) {
    if (children == null) {
      children = new ArrayList<Node<T>>();
    }
    children.add(child);
    child.parent = this;
  }

  /**
   * Return the children of Node<T>. The Tree<T> is represented by a single root
   * Node<T> whose children are represented by a List<Node<T>>. Each of these
   * Node<T> elements in the List can have children. The getChildren() method
   * will return the children of a Node<T>.
   * 
   * @return the children of Node<T>
   */
  public List<Node<T>> getChildren() {
    if (this.children == null) {
      return new ArrayList<Node<T>>();
    }
    return this.children;
  }

  public T getData() {
    return this.data;
  }

  public String getId() {
    return data.toString();
  }

  /**
   * Returns the number of immediate children of this Node<T>.
   * 
   * @return the number of immediate children.
   */
  public int getNumberOfChildren() {
    if (children == null) {
      return 0;
    }
    return children.size();
  }

  public Node<T> getParent() {
    return this.parent;
  }

  public boolean isRoot() {
    return parent == ROOT;
  }

  /**
   * Sets the children of a Node<T> object. See docs for getChildren() for more
   * information.
   * 
   * @param children
   *          the List<Node<T>> to set.
   */
  public void setChildren(List<Node<T>> children) {
    this.children = children;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{").append(getId()).append(",[");
    int i = 0;
    for (Node<T> e : getChildren()) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(e.getId());
      i++;
    }
    sb.append("]").append("}");
    return sb.toString();
  }

}
