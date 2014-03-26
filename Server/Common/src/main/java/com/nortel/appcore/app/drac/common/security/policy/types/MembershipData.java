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

package com.nortel.appcore.app.drac.common.security.policy.types;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class MembershipData implements Serializable {
	private static final long serialVersionUID = 1;
	public static final String MB_RESOURCEGROUPNAME_ELEMENT = "memberResourceGroupName";
	public static final String MB_USERGROUPNAME_ELEMENT = "memberUserGroupName";
	public static final String MB_USERID_ELEMENT = "memberUserID";
	public static final String MB_CREATEDBY_GROUPNAME_ELEMENT = "createdByMemberName";
	public static final String MEMBERSHIPDATA_ELEMENT = "membership";

	private final Set<String> memberUserID = new TreeSet<String>();
	private final Set<UserGroupName> memberUserGroupName = new TreeSet<UserGroupName>();
	private final Set<String> memberResourceGroupName = new TreeSet<String>();
	private String createdByGroup;

	// /**
	// * @deprecated Use MembershipData(List<String>, List<String>, List<String>)
	// * @param description
	// */
	// @Deprecated
	// public MembershipData(List<String> memberUserID, List<String>
	// memberUserGroupName, List<String>
	// memberResourceGroupName)
	// {
	// this(null, memberUserID, memberUserGroupName, memberResourceGroupName);
	// }

	public MembershipData(String creatorGroup) {
		super();
		this.createdByGroup = creatorGroup;
	}

	public MembershipData(String creatorGroup, Set<String> newMemberUserID,
	    Set<UserGroupName> newMemberUserGroupName,
	    Set<String> newMemberResourceGroupName) {
		super();
		this.createdByGroup = creatorGroup;
		setMemberUserID(newMemberUserID);
		setMemberUserGroupName(newMemberUserGroupName);
		setMemberResourceGroupName(newMemberResourceGroupName);

	}

	private static TreeSet<UserGroupName> getElementsToGroupSet(
	    List<Element> elements, String tag) {
		TreeSet<UserGroupName> results = new TreeSet<UserGroupName>();

		if (elements == null || elements.isEmpty()) {
			return results;
		}

		for (Element element : elements) {
			Element el = element;
			if (el.getName().equals(tag)) {
				results.add(new UserGroupName(XmlUtility.convertXMLLiteralsToString(el
				    .getText())));
			}
		}
		return results;
	}

	private static TreeSet<String> getElementsToSet(List<Element> elements,
	    String tag) {
		TreeSet<String> results = new TreeSet<String>();

		if (elements == null || elements.isEmpty()) {
			return results;
		}

		for (Element element : elements) {
			Element el = element;
			if (el.getName().equals(tag)) {
				results.add(XmlUtility.convertXMLLiteralsToString(el.getText()));
			}
		}
		return results;
	}

	public void fromXML(Element root) throws Exception {
		/*********************************************************************/
		/* Constructs Membership attribute. */
		/*********************************************************************/
		Element membershipDataElement = root.getChild(MEMBERSHIPDATA_ELEMENT);
		if (membershipDataElement != null) {

			this.createdByGroup = membershipDataElement
			    .getChildText(MB_CREATEDBY_GROUPNAME_ELEMENT);
			/*****************************************************************************/
			/*
			 * UserGroupName is multipled-instances, we need to collect them one by
			 * one
			 */
			/*****************************************************************************/
			memberUserID.clear();
			memberUserID.addAll(getElementsToSet(
			    membershipDataElement.getChildren(MB_USERID_ELEMENT),
			    MB_USERID_ELEMENT));

			memberUserGroupName.clear();
			memberUserGroupName.addAll(getElementsToGroupSet(
			    membershipDataElement.getChildren(MB_USERGROUPNAME_ELEMENT),
			    MB_USERGROUPNAME_ELEMENT));

			memberResourceGroupName.clear();
			memberResourceGroupName.addAll(getElementsToSet(
			    membershipDataElement.getChildren(MB_RESOURCEGROUPNAME_ELEMENT),
			    MB_RESOURCEGROUPNAME_ELEMENT));
		}

	}

	public String getCreatedByGroupName() {
		return createdByGroup;
	}

	public Set<String> getMemberResourceGroupName() {
		return memberResourceGroupName;
	}

	public Set<UserGroupName> getMemberUserGroupName() {
		return memberUserGroupName;
	}

	public Set<String> getMemberUserID() {
		return memberUserID;
	}

	public void setCreatedByGroupName(String createdByGroup) {
		this.createdByGroup = createdByGroup;
	}

	public void setMemberResourceGroupName(Set<String> newMemberResourceGroupName) {
		memberResourceGroupName.clear();
		if (newMemberResourceGroupName != null) {
			memberResourceGroupName.addAll(newMemberResourceGroupName);
		}
	}

	public void setMemberUserGroupName(Set<UserGroupName> newMemberUserGroupName) {
		// newMemberUserGroupName, new
		// Exception("StackTrace"));
		memberUserGroupName.clear();
		if (newMemberUserGroupName != null) {
			memberUserGroupName.addAll(newMemberUserGroupName);
		}
	}

	public void setMemberUserID(Set<String> list) {
		memberUserID.clear();
		if (list != null) {
			memberUserID.addAll(list);
		}
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {

		StringBuilder buf = new StringBuilder();
		buf.append("<");
		buf.append(MembershipData.MEMBERSHIPDATA_ELEMENT);
		buf.append(">");

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    MembershipData.MB_CREATEDBY_GROUPNAME_ELEMENT, this.createdByGroup));

		for (String string : this.memberUserID) {
			buf.append("\n");
			buf.append(XmlUtility.toXMLString(MB_USERID_ELEMENT,
			    XmlUtility.convertStringToXMLLiterals(string)));
		}

		for (UserGroupName group : this.memberUserGroupName) {
			buf.append("\n");
			buf.append(XmlUtility.toXMLString(MB_USERGROUPNAME_ELEMENT,
			    XmlUtility.convertStringToXMLLiterals(group.toString())));
		}

		for (String string : this.memberResourceGroupName) {
			buf.append("\n");
			buf.append(XmlUtility.toXMLString(MB_RESOURCEGROUPNAME_ELEMENT,
			    XmlUtility.convertStringToXMLLiterals(string)));
		}

		buf.append("\n");
		buf.append("</");
		buf.append(MEMBERSHIPDATA_ELEMENT);
		buf.append(">");

		return buf.toString();
	}

}
