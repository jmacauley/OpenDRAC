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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckablePolicy;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class ResourceGroupProfile implements Serializable,
    PolicyCheckablePolicy {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	// ResourceGroup XML element and attribute related constants
	public static final String RESOURCEGROUP_ELEMENT = "resourceGroup";
	public static final String CREATIONDATE_ATTR = "creationDate";
	public static final String LASTMODIFICATIONUSERID_ATTR = "lastModificationUserID";
	public static final String LASTMODIFICATIONDATE_ATTR = "lastModifiedDate";
	public static final String NAME_ATTR = "name";
	public static final String RESOURCEGROUPDEFAULT_ELEMENT = "defaultResourceGroup";
	public static final String INFORMATION_ELEMENT = "organization";

	// public static final String REFERENCINGLIST_ELEMENT =
	// "referencingUserGroupNameList";
	// public static final String MB_RESOURCEGROUPNAME_ELEMENT =
	// "memberResourceGroupName";
	// public static final String USERGROUPNAME_ELEMENT = "userGroupName";

	public static final String RESOURCELIST_ELEMENT = "resourceList";

	// UserGroup fields
	private String name;
	private Calendar creationDate;
	private Calendar lastModifiedDate;
	private String lastModificationUserID;
	private Boolean defaultResourceGroup;
	private Integer informationID;
	private final List<Resource> resourceList = new ArrayList<Resource>();
	private ResourcePolicy resourcePolicy;
	private MembershipData membership;

	// private final List<String> referencingUserGroupName;

	public ResourceGroupProfile(String profileName, Calendar profileCreationDate,
	    Calendar profileLastModifiedDate, String profileLastModificationUserID,
	    String creator) {
		super();
		name = profileName;
		creationDate = profileCreationDate;
		lastModifiedDate = profileLastModifiedDate;
		lastModificationUserID = profileLastModificationUserID;
		defaultResourceGroup = Boolean.FALSE;
		// this.referencingUserGroupName = new ArrayList<String>();

		resourcePolicy = new ResourcePolicy();
		membership = new MembershipData(creator);

	}

	public boolean containResource(String resourceID) {
		if (resourceID == null) {
			return false;
		}

		if (this.defaultResourceGroup.equals(Boolean.TRUE)) {
			return true;
		}

		for (Resource resource : this.resourceList) {
			if (resource.getResourceID().equals(resourceID)) {
				return true;
			}
		}

		return false;
	}

	public void fromXML(Element root) throws Exception {

		if (root == null) {
			return;
		}

		String xmlName = root.getAttributeValue(NAME_ATTR);

		if (xmlName == null || xmlName.equals("")) {
			DracException ex = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { NAME_ATTR });
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		String userID = root.getAttributeValue(LASTMODIFICATIONUSERID_ATTR);

		if (userID == null || userID.equals("")) {
			DracException ex = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { LASTMODIFICATIONUSERID_ATTR });
			log.error(ex.getMessage(), ex);
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
		this.name = XmlUtility.convertXMLLiteralsToString(xmlName);
		this.lastModificationUserID = userID;
		this.creationDate = creationTime;
		this.lastModifiedDate = lastModifiedTime;

		this.defaultResourceGroup = Boolean.valueOf(root
		    .getChildText(RESOURCEGROUPDEFAULT_ELEMENT));

		Element xmlResourceList = root.getChild(RESOURCELIST_ELEMENT);
		if (xmlResourceList != null) {
			List<Element> list = xmlResourceList.getChildren();
			if (list != null) {
				for (Element element : list) {
					Resource resource = new Resource();
					resource.fromXML(element);
					this.resourceList.add(resource);
				}
			}
		}

		String infoIDStr = root.getChildText(INFORMATION_ELEMENT);
		if (infoIDStr != null && !infoIDStr.equals("")) {
			informationID = Integer.valueOf(infoIDStr);
		}

		ResourcePolicy resourcePolicyXml = new ResourcePolicy();
		resourcePolicyXml.fromXML(root);
		this.resourcePolicy = resourcePolicyXml;

		/*
		 * Element referenceList = root.getChild(REFERENCINGLIST_ELEMENT);
		 * if(referenceList != null){ List userGroupName =
		 * referenceList.getChildren(); if(userGroupName != null){ Element element =
		 * null; for(Iterator it = userGroupName.iterator(); it.hasNext();){ element
		 * = (Element) (it.next()); String convertString =
		 * XmlUtility.convertXMLLiteralsToString(element.getValue());
		 * if(element.getName().equals(MB_RESOURCEGROUPNAME_ELEMENT)){
		 * this.createdByGroup = convertString; } else {
		 * this.referencingUserGroupName.add(convertString); } } } }
		 */

		membership.fromXML(root);

	}

	public Calendar getCreationDate() {
		return creationDate;
	}

	public Boolean getDefaultResourceGroup() {
		return defaultResourceGroup;
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

	public String getName() {
		return name;
	}

	public List<Resource> getResourceList() {
		return resourceList;
	}

	public ResourcePolicy getResourcePolicy() {
		return resourcePolicy;
	}

	// public void setCreatedByGroupName(String createdByGroup)
	// {
	// this.membership.setCreatedByGroupName(createdByGroup);
	// }

	// public void setCreationDate(Calendar creationDate)
	// {
	// this.creationDate = creationDate;
	// }

	public void setDefaultResourceGroup(Boolean defaultGroup) {
		defaultResourceGroup = defaultGroup;
	}

	// public void setInformationID(Integer informationID)
	// {
	// this.informationID = informationID;
	// }

	// public void setLastModificationUserID(String lastModificationUserID)
	// {
	// this.lastModificationUserID = lastModificationUserID;
	// }

	// public void setLastModifiedDate(Calendar lastModifiedDate)
	// {
	// this.lastModifiedDate = lastModifiedDate;
	// }

	public void setMembership(MembershipData newMembership) {
		membership = newMembership;
	}

	public void setResourceList(List<Resource> list) {
		resourceList.clear();
		if (list == null) {
			return;
		}

		resourceList.addAll(list);
	}

	public void setResourcePolicy(ResourcePolicy policy) {
		resourcePolicy = policy;
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder(60);

		// UserProfile attributes
		buf.append("<");
		buf.append(RESOURCEGROUP_ELEMENT);
		buf.append(" " + NAME_ATTR + "=\""
		    + XmlUtility.convertStringToXMLLiterals(name) + "\"");
		buf.append(" " + LASTMODIFICATIONUSERID_ATTR + "=\""
		    + this.lastModificationUserID + "\"");
		buf.append(" " + CREATIONDATE_ATTR + "=\""
		    + DateFormatter.dateToString(this.creationDate) + "\"");
		buf.append(" " + LASTMODIFICATIONDATE_ATTR + "=\""
		    + DateFormatter.dateToString(this.lastModifiedDate) + "\"");
		buf.append(">");

		// UserGroupProfile Elements
		buf.append("\n");
		buf.append(XmlUtility.toXMLString(RESOURCEGROUPDEFAULT_ELEMENT,
		    this.defaultResourceGroup));

		buf.append("\n");
		buf.append("<");
		buf.append(RESOURCELIST_ELEMENT);
		buf.append(">");

		for (Resource resource : this.resourceList) {
			buf.append(resource.toXMLString());
		}

		buf.append("</" + RESOURCELIST_ELEMENT + ">");

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(INFORMATION_ELEMENT, this.informationID));

		buf.append("\n");
		buf.append(resourcePolicy.toXMLString());
		/*
		 * buf.append("\n"); buf.append("<" + REFERENCINGLIST_ELEMENT + ">");
		 * buf.append("\n");
		 * buf.append(XmlUtility.toXMLString(MB_RESOURCEGROUPNAME_ELEMENT,
		 * this.createdByGroup)); if(this.referencingUserGroupName != null){
		 * for(Iterator <String> it = this.referencingUserGroupName.iterator();
		 * it.hasNext();){ buf.append("\n");
		 * buf.append(XmlUtility.toXMLString(USERGROUPNAME_ELEMENT,
		 * it.next().toString())); } } buf.append("</" + REFERENCINGLIST_ELEMENT +
		 * ">");
		 */

		buf.append(this.membership.toXMLString());
		buf.append("\n");
		buf.append("</" + RESOURCEGROUP_ELEMENT + ">");
		return buf.toString();
	}

}
