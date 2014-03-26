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

public final class PersonalData implements Serializable {
	public static class Address implements Serializable {
		private static final long serialVersionUID = 1;
		private final String phone;
		private final String mail;
		/*
		 * I know this is the wrong spelling for address, but because we serialize
		 * this class changing it would require a upgrade proc to the database
		 */
		private final String postalAdrress;

		public Address(String phoneNumber, String mailAddress, String address) {
			phone = phoneNumber;
			mail = mailAddress;
			postalAdrress = address;
		}

		public String getMail() {
			return mail;
		}

		public String getPhone() {
			return phone;
		}

		public String getPostalAddress() {
			return postalAdrress;
		}

	}

	public static class UserName implements Serializable {
		private static final long serialVersionUID = 1;
		private final String commonName;
		private final String givenName;
		private final String surName;

		public UserName(String commonName, String givenName, String surName) {
			super();
			this.commonName = commonName;
			this.givenName = givenName;
			this.surName = surName;
		}

		public String getCommonName() {
			return commonName;
		}

		public String getGivenName() {
			return givenName;
		}

		public String getSurName() {
			return surName;
		}

		// public void setCommonName(String commonName)
		// {
		// this.commonName = commonName;
		// }
		//
		// public void setGivenName(String givenName)
		// {
		// this.givenName = givenName;
		// }
		//
		// public void setSurName(String surName)
		// {
		// this.surName = surName;
		// }
	}

	private static final long serialVersionUID = 1;
	private UserName name;
	private Address address;
	private String description;
	private String title;
	public static final String UD_DESCRIPTION_ELEMENT = "description";
	public static final String UD_POSTALADDRESS_ELEMENT = "postalAddress";
	public static final String UD_MAIL_ELEMENT = "mail";
	public static final String UD_TELEPHONENUMBER_ELEMENT = "telephoneNumber";
	public static final String UD_TITLE_ELEMENT = "title";
	public static final String UD_SURNAME_ELEMENT = "surname";
	public static final String UD_GIVENNAME_ELEMENT = "givenName";
	public static final String UD_COMMONNAME_ELEMENT = "commonName";
	public static final String USERDATA_ELEMENT = "userData";

	/*
	 * public PersonalData(UserName name, String title, Address address, String
	 * description) { super(); this.name = name; this.title = title; this.address
	 * = address; this.description = description; }
	 */

	public void fromXML(Element root) throws Exception {
		Element userDataElement = root.getChild(PersonalData.USERDATA_ELEMENT);

		if (userDataElement != null) {

			this.setName(new UserName(userDataElement
			    .getChildText(PersonalData.UD_COMMONNAME_ELEMENT), userDataElement
			    .getChildText(PersonalData.UD_GIVENNAME_ELEMENT), userDataElement
			    .getChildText(PersonalData.UD_SURNAME_ELEMENT)));
			this.title = userDataElement.getChildText(PersonalData.UD_TITLE_ELEMENT);

			this.setAddress(new Address(userDataElement
			    .getChildText(PersonalData.UD_TELEPHONENUMBER_ELEMENT),
			    userDataElement.getChildText(PersonalData.UD_MAIL_ELEMENT),
			    userDataElement.getChildText(PersonalData.UD_POSTALADDRESS_ELEMENT)));

			this.description = userDataElement
			    .getChildText(PersonalData.UD_DESCRIPTION_ELEMENT);

		}
	}

	public Address getAddress() {
		return address;
	}

	public String getDescription() {
		return description;
	}

	public UserName getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(UserName name) {
		this.name = name;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {

		return toXMLString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder(40);

		buf.append("<");
		buf.append(PersonalData.USERDATA_ELEMENT);
		buf.append(">");

		buf.append(XmlUtility.toXMLString(
		    PersonalData.UD_COMMONNAME_ELEMENT,
		    this.name != null && this.name.getCommonName() != null ? this.name
		        .getCommonName() : null));

		buf.append("\n");

		buf.append(XmlUtility.toXMLString(
		    PersonalData.UD_GIVENNAME_ELEMENT,
		    this.name != null && this.name.getGivenName() != null ? this.name
		        .getGivenName() : null));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    PersonalData.UD_SURNAME_ELEMENT,
		    this.name != null && this.name.getSurName() != null ? this.name
		        .getSurName() : null));

		buf.append("\n");
		buf.append(XmlUtility
		    .toXMLString(PersonalData.UD_TITLE_ELEMENT, this.title));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    PersonalData.UD_TELEPHONENUMBER_ELEMENT,
		    this.address != null && this.address.getPhone() != null ? this.address
		        .getPhone() : null));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    PersonalData.UD_MAIL_ELEMENT,
		    this.address != null && this.address.getMail() != null ? this.address
		        .getMail() : null));

		buf.append("\n");
		buf.append(XmlUtility
		    .toXMLString(
		        PersonalData.UD_POSTALADDRESS_ELEMENT,
		        this.address != null && this.address.getPostalAddress() != null ? this.address
		            .getPostalAddress() : null));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(PersonalData.UD_DESCRIPTION_ELEMENT,
		    this.description));

		buf.append("\n");
		buf.append("</");
		buf.append(PersonalData.USERDATA_ELEMENT);
		buf.append(">");

		return buf.toString();
	}
}
