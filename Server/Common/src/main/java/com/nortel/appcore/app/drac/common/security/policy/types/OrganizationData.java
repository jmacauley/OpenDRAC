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

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class OrganizationData implements Serializable {
	private static final long serialVersionUID = 1;
	private String orgName;
	private String orgUnitName;
	private String owner;
	private String description;
	private String seeAlso;
	private String category;
	// OrganizationList element
	public static final String ORGANIZATIONDATA_ELEMENT = "organization";
	public static final String OD_DESCRIPTION_ELEMENT = "description";
	public static final String OD_NAME_ELEMENT = "organizationName";
	public static final String OD_UNITNAME_ELEMENT = "organizationalUnitName";
	public static final String OD_OWNER_ELEMENT = "owner";
	public static final String OD_SEEALSO_ELEMENT = "seeAlso";
	public static final String OD_BUSINESSCATEGORY_ELEMENT = "businessCategory";

	public OrganizationData() {
		super();
	}

	public void fromXML(Element root) throws Exception {
		Element orgElement = root
		    .getChild(OrganizationData.ORGANIZATIONDATA_ELEMENT);

		if (orgElement == null) {
			return;
		}

		/*********************************************************************/
		/* Constructs Organization attribute. */
		/*********************************************************************/
		this.setOrgName(orgElement.getChildText(OrganizationData.OD_NAME_ELEMENT));
		this.setOrgUnitName(orgElement
		    .getChildText(OrganizationData.OD_UNITNAME_ELEMENT));
		this.setDescription(orgElement
		    .getChildText(OrganizationData.OD_DESCRIPTION_ELEMENT));
		this.setSeeAlso(orgElement
		    .getChildText(OrganizationData.OD_SEEALSO_ELEMENT));
		this.setCategory(orgElement
		    .getChildText(OrganizationData.OD_BUSINESSCATEGORY_ELEMENT));
		this.setOwner(orgElement.getChildText(OrganizationData.OD_OWNER_ELEMENT));

	}

	public String getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public String getOrgName() {
		return orgName;
	}

	public String getOrgUnitName() {
		return orgUnitName;
	}

	public String getOwner() {
		return owner;
	}

	public String getSeeAlso() {
		return seeAlso;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public void setOrgUnitName(String orgUnitName) {
		this.orgUnitName = orgUnitName;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setSeeAlso(String seeAlso) {
		this.seeAlso = seeAlso;
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder(40);

		buf.append("<");
		buf.append(OrganizationData.ORGANIZATIONDATA_ELEMENT);
		buf.append(">");
		buf.append("\n");

		buf.append(XmlUtility.toXMLString(OrganizationData.OD_DESCRIPTION_ELEMENT,
		    this.description));
		buf.append("\n");

		buf.append(XmlUtility.toXMLString(OrganizationData.OD_NAME_ELEMENT,
		    this.orgName));
		buf.append("\n");

		buf.append(XmlUtility.toXMLString(OrganizationData.OD_UNITNAME_ELEMENT,
		    this.orgUnitName));
		buf.append("\n");

		buf.append(XmlUtility.toXMLString(OrganizationData.OD_OWNER_ELEMENT,
		    this.owner));
		buf.append("\n");

		buf.append(XmlUtility.toXMLString(OrganizationData.OD_SEEALSO_ELEMENT,
		    this.seeAlso));
		buf.append("\n");

		buf.append(XmlUtility.toXMLString(
		    OrganizationData.OD_BUSINESSCATEGORY_ELEMENT, this.category));

		buf.append("\n");
		buf.append("</");
		buf.append(OrganizationData.ORGANIZATIONDATA_ELEMENT);
		buf.append(">");

		return buf.toString();
	}
}
