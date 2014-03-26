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
import java.util.Calendar;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckablePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

/**
 * @author vnguyen This class serves as a container for UserGroup information.
 */

public final class UserGroupProfile implements Serializable,
    PolicyCheckablePolicy {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(getClass());

	// UserGroup XML element and attribute related constants
	public static final String USERGROUP_ELEMENT = "userGroup";
	public static final String CREATIONDATE_ATTR = "creationDate";
	public static final String LASTMODIFICATIONUSERID_ATTR = "lastModificationUserID";
	public static final String LASTMODIFICATIONDATE_ATTR = "lastModifiedDate";
	public static final String NAME_ATTR = "name";
	// public static final String USERGROUPTYPE_ELEMENT = "userGroupType";
	public static final String USERGROUPDEFAULT_ELEMENT = "defaultUserGroup";
	private static final String INFORMATION_ELEMENT = "organization";

	// public static final String MEMBERSHIP_ELEMENT = "membership";

	public static final String REFERENCING_ELEMENT = "referencingUserGroupName";

	// UserGroup fields
	private UserGroupName name;
	private Calendar creationDate;
	private Calendar lastModifiedDate;
	private String lastModificationUserID;
	private UserGroupType userGroupType;
	private Boolean defaultUserGroup;
	private Integer informationID;
	private GroupPolicy groupPolicy;
	private MembershipData membership;
	private UserGroupName referencingUserGroupName;

	/**
	 * Use only when calling fromXML() on the resulting object
	 */
	public UserGroupProfile() {
		// we need membership and groupPolicy initialized
		this(new UserGroupName("admin"), null, null, "admin", new UserGroupName(
		    "SystemAdminGroup"));
	}

	public UserGroupProfile(UserGroupName groupName, Calendar dateOfCreation,
	    Calendar dateLastModified, String lastModifiedUser, UserGroupName creator) {
		super();
		this.name = groupName;
		this.creationDate = dateOfCreation;
		this.lastModifiedDate = dateLastModified;
		this.lastModificationUserID = lastModifiedUser;
		this.userGroupType = UserGroupType.USER;
		this.defaultUserGroup = Boolean.FALSE;
		this.groupPolicy = new GroupPolicy();
		this.membership = new MembershipData(creator.toString());
	}

	public void fromXML(Element root) throws Exception {
		if (root == null) {
			throw new Exception("Null element");
		}

		String nameXml = root.getAttributeValue(NAME_ATTR);

		if (nameXml == null || nameXml.equals("")) {
			DracException ex = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { NAME_ATTR });
			log.error("Error: ", ex);
			throw ex;
		}

		String userID = root.getAttributeValue(LASTMODIFICATIONUSERID_ATTR);

		if (userID == null || userID.equals("")) {
			DracException ex = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { LASTMODIFICATIONUSERID_ATTR });
			log.error("Error: ", ex);
			throw ex;
		}

		Calendar creationTime = null;
		Calendar lastModifiedTime = null;
		try {
			String creationTimeString = root.getAttributeValue(CREATIONDATE_ATTR);
			creationTime = DateFormatter.getDateFromString(creationTimeString);

			String lastModifiedTimeString = root
			    .getAttributeValue(LASTMODIFICATIONDATE_ATTR);

			lastModifiedTime = DateFormatter
			    .getDateFromString(lastModifiedTimeString);

			defaultUserGroup = Boolean.valueOf(root
			    .getChildText(USERGROUPDEFAULT_ELEMENT));

		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			DracException e = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { CREATIONDATE_ATTR + " or/and "
			        + LASTMODIFICATIONDATE_ATTR }, ex);
			throw e;
		}

		// this.name = name;
		name = new UserGroupName(XmlUtility.convertXMLLiteralsToString(nameXml));
		this.lastModificationUserID = XmlUtility.convertXMLLiteralsToString(userID);
		this.creationDate = creationTime;
		this.lastModifiedDate = lastModifiedTime;
		this.userGroupType = UserGroupType.fromXMLToObject(root);
		this.groupPolicy.fromXML(root);

		String infoIDStr = root.getChildText(INFORMATION_ELEMENT);
		if (infoIDStr != null && !infoIDStr.equals("")) {
			this.informationID = Integer.decode(infoIDStr);
		}

		membership.fromXML(root);

		if (root.getChildText(REFERENCING_ELEMENT) != null) {
			this.referencingUserGroupName = new UserGroupName(
			    root.getChildText(REFERENCING_ELEMENT));
		}


	}

	@Deprecated
	public UserGroupName getCreatedByGroupName() {
		return new UserGroupName(membership.getCreatedByGroupName());
	}

	public Calendar getCreationDate() {
		return creationDate;
	}

	public Boolean getDefaultUserGroup() {
		return defaultUserGroup;
	}

	public GroupPolicy getGroupPolicy() {
		return groupPolicy;
	}

	// public Integer getInformationID()
	// {
	// return informationID;
	// }

	public String getLastModificationUserID() {
		return lastModificationUserID;
	}

	public Calendar getLastModifiedDate() {
		return lastModifiedDate;
	}

	public MembershipData getMembership() {
		return membership;
	}

	public UserGroupName getName() {
		return name;
	}

	public UserGroupName getReferencingUserGroupName() {
		return referencingUserGroupName;
	}

	public UserGroupType getUserGroupType() {
		return userGroupType;
	}

	// public void setCreatedByGroupName(String createdByGroup)
	// {
	// this.membership.setCreatedByGroupName(createdByGroup);
	// }

	// public void setCreationDate(Calendar creationDate)
	// {
	// this.creationDate = creationDate;
	// }

	public void setDefaultUserGroup(Boolean defaultUserGroup) {
		this.defaultUserGroup = defaultUserGroup;
	}

	public void setGroupPolicy(GroupPolicy groupPolicy) {
		this.groupPolicy = groupPolicy;
	}

	// public void setInformationID(Integer informationID)
	// {
	// this.informationID = informationID;
	// }

	public void setLastModificationUserID(String lastModificationUserID) {
		this.lastModificationUserID = lastModificationUserID;
	}

	public void setLastModifiedDate(Calendar lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public void setMembership(MembershipData membership) {
		this.membership = membership;
	}

	//
	// public void setName(String name)
	// {
	// this.name = name;
	// }

	public void setReferencingUserGroupName(UserGroupName referencingUserGroupName) {
		this.referencingUserGroupName = referencingUserGroupName;
	}

	public void setUserGroupType(UserGroupType userGroupType) {
		this.userGroupType = userGroupType;
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder(40);

		// UserProfile attributes
		buf.append("<");
		buf.append(USERGROUP_ELEMENT);
		buf.append(" " + NAME_ATTR + "=\""
		    + XmlUtility.convertStringToXMLLiterals(name.toString()) + "\"");
		buf.append(" " + LASTMODIFICATIONUSERID_ATTR + "=\""
		    + XmlUtility.convertStringToXMLLiterals(this.lastModificationUserID)
		    + "\"");
		buf.append(" " + CREATIONDATE_ATTR + "=\""
		    + DateFormatter.dateToString(this.creationDate) + "\"");
		buf.append(" " + LASTMODIFICATIONDATE_ATTR + "=\""
		    + DateFormatter.dateToString(this.lastModifiedDate) + "\"");
		buf.append(">");

		// UserGroupProfile Elements
		if (this.userGroupType != null) {
			buf.append("\n");
			buf.append(this.userGroupType.toXMLString());
		}

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(USERGROUPDEFAULT_ELEMENT,
		    this.defaultUserGroup));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(INFORMATION_ELEMENT, this.informationID));

		if (this.groupPolicy != null) {
			buf.append("\n");
			buf.append(this.groupPolicy.toXMLString());
		}

		buf.append(membership.toXMLString());

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(REFERENCING_ELEMENT,
		    this.referencingUserGroupName));

		// End UserGroupProfile
		buf.append("\n");
		buf.append("</" + USERGROUP_ELEMENT + ">");
		return buf.toString();
	}

}
