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
import java.util.List;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.utility.XmlUtility;

/*
 * <!-- ===== authenticationAuditData_T ===== --> <xsd:complexType name="authenticationAuditData_T">
 * <xsd:annotation> <xsd:documentation> We should probably add some more stuff in there. lastLoginAddress - A
 * list of source IP address/TCP port pairs tracking the set of machines from which the user last
 * authenticated. Entries in this list should be treated FIFO. The IP addressed used in logging should be
 * generated from the interface over which a request is generated, and not using this list of IP addresses.
 * numOfInvalidAttempts - The running count of the number of successive invalid login attempts. The value is
 * reset to zero when a valid login occurs. locationOfInvalidAttempt - A list of source IP address/TCP port
 * pairs tracking the set of machines from which the user failed to authenticate. Entries in this list should
 * be treated FIFO, seperated by a comma. </xsd:documentation> </xsd:annotation> <xsd:sequence> <xsd:element
 * name="lastLoginAddress" type="xsd:string" minOccurs="0" maxOccurs="10"/> <xsd:element
 * name="numOfInvalidAttempts" type="xsd:unsignedInt" default="0"/> <xsd:element
 * name="locationOfInvalidAttempts" type="xsd:string" minOccurs="0" maxOccurs="1"/> </xsd:sequence>
 * </xsd:complexType>
 */
/*
 * <!-- ===== AuditData Sample ===== --> <auditData> <lastLoginAddress>localhost</lastLoginAddress>
 * <numOfInvalidAttempts>2</numOfInvalidAttempts>
 * <locationOfInvalidAttempts>localhost,100.192.1.1</locationOfInvalidAttempts> </auditData>
 */

public final class AuthenticationAuditData implements Serializable {
	private static final long serialVersionUID = 1;

	public static final String AUDIT_DATA_ELEMENT = "auditData";
	public static final String LAST_LOGIN_ADDRESS_ELEMENT = "lastLoginAddress";
	public static final String INVALID_ADDRESS_NUM_ELEMENT = "numOfInvalidAttempts";
	public static final String INVALID_ADDRESSES_ELEMENT = "locationOfInvalidAttempts";

	private final List<IPAddress> lastLoginAddressList;
	private List<IPAddress> locationOfInvalidAttempts;
	private int numOfInvalidAttempts;
	private static final int MAX_SIZE = 10;

	public AuthenticationAuditData() {
		this.lastLoginAddressList = new ArrayList<IPAddress>(MAX_SIZE);
		this.locationOfInvalidAttempts = new ArrayList<IPAddress>();
		numOfInvalidAttempts = 0;
	}
	
	public void resetLocationOfInvalidAttemps(){
	  this.locationOfInvalidAttempts.clear();
	  this.numOfInvalidAttempts = -1;
	}

	public void addLocationOfInvalidAttempt(IPAddress ip) {
		if (ip == null) {
			return;
		}

		if (this.locationOfInvalidAttempts == null) {
			this.locationOfInvalidAttempts = new ArrayList<IPAddress>();
		}

		if (this.locationOfInvalidAttempts.size() == MAX_SIZE) {
			this.locationOfInvalidAttempts.remove(0);
		}

		this.locationOfInvalidAttempts.add(ip);
	}

	public void addLoginAddress(IPAddress lastLoginAddress) {

		if (lastLoginAddress == null) {
			return;
		}

		if (this.locationOfInvalidAttempts == null) {
			this.locationOfInvalidAttempts = new ArrayList<IPAddress>();
		}

		if (this.lastLoginAddressList.size() == MAX_SIZE) {
			this.lastLoginAddressList.remove(0);
		}

		this.lastLoginAddressList.add(lastLoginAddress);
	}

	// public void clearAllInvalidAttempts()
	// {
	// if (this.locationOfInvalidAttempts != null)
	// {
	// this.locationOfInvalidAttempts.clear();
	// }
	// }

	public void fromXML(Element root) throws Exception {

		if (root == null) {
			return;
		}

		Element auditDataElement = root
		    .getChild(AuthenticationAuditData.AUDIT_DATA_ELEMENT);

		if (auditDataElement == null) {
			return;
		}

		/*****************************************************************/
		/* Construct AuthenticationAuditData. */
		/*****************************************************************/
		List<String> logins = XmlUtility.stringToArray(auditDataElement
		    .getChildText(AuthenticationAuditData.LAST_LOGIN_ADDRESS_ELEMENT), ",");

		if (logins != null) {
			for (String string : logins) {
				this.lastLoginAddressList.add(IPAddress.fromString(string));
			}

		}

		String numOfInvalidAttemptStr = auditDataElement
		    .getChildText(AuthenticationAuditData.INVALID_ADDRESS_NUM_ELEMENT);

		// 
		if (numOfInvalidAttemptStr != null && !numOfInvalidAttemptStr.equals("")) {
			this.numOfInvalidAttempts = Integer.parseInt(numOfInvalidAttemptStr);
			// 
		}

		List<String> invalidAttempts = XmlUtility.stringToArray(auditDataElement
		    .getChildText(AuthenticationAuditData.INVALID_ADDRESSES_ELEMENT), ",");
		if (invalidAttempts != null) {
			for (String string : invalidAttempts) {
				this.locationOfInvalidAttempts.add(IPAddress.fromString(string));
			}
		}

		// 

	}

	public List<IPAddress> getLastLoginAddressList() {
		return lastLoginAddressList;
	}

	public List<IPAddress> getLocationOfInvalidAttempts() {
		return locationOfInvalidAttempts;
	}

	public int getNumberOfInvalidAttempts() {
		return numOfInvalidAttempts;
	}

	public void incrementNumOfInvalidAttempts() {
		this.numOfInvalidAttempts += 1;
	}

	public void setNumOfInvalidAttempts(int numOfInvalidAttempts) {
		this.numOfInvalidAttempts = numOfInvalidAttempts;
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {

		StringBuilder buf = new StringBuilder();

		// AuthenticationAuditData attributes
		buf.append("<");
		buf.append(AuthenticationAuditData.AUDIT_DATA_ELEMENT);
		buf.append(">");

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    AuthenticationAuditData.LAST_LOGIN_ADDRESS_ELEMENT,
		    XmlUtility.arrayToString(this.lastLoginAddressList, ",")));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    AuthenticationAuditData.INVALID_ADDRESS_NUM_ELEMENT,
		    this.numOfInvalidAttempts));

		if (this.locationOfInvalidAttempts != null) {
			buf.append("\n");
			buf.append(XmlUtility.toXMLString(
			    AuthenticationAuditData.INVALID_ADDRESSES_ELEMENT,
			    XmlUtility.arrayToString(this.locationOfInvalidAttempts, ",")));
		}

		// end AuthenticationData
		buf.append("\n");
		buf.append("</" + AuthenticationAuditData.AUDIT_DATA_ELEMENT + ">");
		return buf.toString();
	}

}
