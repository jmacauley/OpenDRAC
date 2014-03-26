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
 * <!-- ===== passwordPolicy_T ===== --> <xsd:complexType name="passwordPolicy_T"> <xsd:annotation>
 * <xsd:documentation> passwordAging - the maximum age in days of a user password. The time duration between
 * forced password changes. A value of "0" indicates this check is disabled. invalidPasswords - a list of
 * clear text passwords a user cannot use; each item seperated by a comma. passwordExpirationNotification -
 * number of days before a user's password expires that the system will notify start to notify the user to
 * change their password. passwordHistorySize - number of old passwords to maintain in a user's password
 * history. passwordRules - password checking rules used to validate a newly entered password; each rule
 * seperated by a comma. </xsd:documentation> </xsd:annotation> <xsd:sequence> <xsd:element
 * name="passwordAging" type="xsd:unsignedInt" minOccurs="0"/> <xsd:element
 * name="passwordExpirationNotification" type="xsd:unsignedInt" minOccurs="0"/> <xsd:element
 * name="passwordHistorySize" type="xsd:unsignedInt" minOccurs="0"/> <xsd:element name="invalidPasswords"
 * type="xsd:string" minOccurs="0"/> <xsd:element name="passwordRules" type="xsd:string" minOccurs="0"/>
 * </xsd:sequence> </xsd:complexType>
 */

/*
 * XML Sample <localPasswordPolicy> <passwordAging>100</passwordAging>
 * <passwordExpirationNotification>10</passwordExpirationNotification>
 * <passwordHistorySize>2</passwordHistorySize> <invalidPasswords></invalidPasswords>
 * <passwordRules></passwordRules> </localPasswordPolicy>
 */
public final class PasswordPolicy implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String LOCAL_PWPOLICY_ELEMENT = "localPasswordPolicy";
	public static final String PW_AGING_ELEMENT = "passwordAging";
	public static final String PW_EXPIRATION_NOTIF_ELEMENT = "passwordExpirationNotification";
	public static final String PW_HISTORY_SIZE_ELEMENT = "passwordHistorySize";
	public static final String PW_INVALIDS_ELEMENT = "invalidPasswords";
	public static final String PW_RULES_ELEMENT = "passwordRules";

	private Integer pwAging;
	private Integer pwExpiredNotif;
	private Integer pwHistorySize;
	private List<String> pwInvalids;
	private String pwRules;

	public PasswordPolicy() {
		this.pwInvalids = new ArrayList<String>();
	}

	public void fromXML(Element node) throws Exception {
		/*****************************************************************/
		/* Construct InternalAccountData. */
		/*****************************************************************/
		String pwAging = node.getChildText(PasswordPolicy.PW_AGING_ELEMENT);
		if (pwAging != null && !pwAging.equals("")) {
			this.setPwAging(Integer.valueOf(pwAging));
		}

		String pwExNotif = node
		    .getChildText(PasswordPolicy.PW_EXPIRATION_NOTIF_ELEMENT);
		if (pwExNotif != null && !pwExNotif.equals("")) {
			this.setPwExpiredNotif(Integer.valueOf(pwExNotif));
		}

		String pwHistSize = node
		    .getChildText(PasswordPolicy.PW_HISTORY_SIZE_ELEMENT);
		if (pwHistSize != null && !pwHistSize.equals("")) {
			this.setPwHistorySize(Integer.valueOf(pwHistSize));
		}

		String pwInvalids = node.getChildText(PasswordPolicy.PW_INVALIDS_ELEMENT);
		if (pwInvalids != null) {
			this.setPwInvalids(XmlUtility.stringToArray(pwInvalids, ","));
		}

		String pwRules = node.getChildText(PasswordPolicy.PW_RULES_ELEMENT);

		if (pwRules != null && !pwRules.equals("")) {
			this.setPwRules(pwRules);
		}
	}

	public Integer getPwAging() {
		return pwAging;
	}

	public Integer getPwExpiredNotif() {
		return pwExpiredNotif;
	}

	public Integer getPwHistorySize() {
		return pwHistorySize;
	}

	public List<String> getPwInvalids() {
		return pwInvalids;
	}

	public String getPwRules() {
		return pwRules;
	}

	//
	// public boolean isEmpty()
	// {
	// if (pwAging != null)
	// {
	// return false;
	// }
	// if (pwExpiredNotif != null)
	// {
	// return false;
	// }
	// if (pwInvalids != null && !pwInvalids.isEmpty())
	// {
	// return false;
	// }
	// if (pwRules != null)
	// {
	// return false;
	// }
	//
	// return true;
	// }

	public void setPwAging(Integer pwAging) {
		this.pwAging = pwAging;
	}

	public void setPwExpiredNotif(Integer pwExpiredNotif) {
		this.pwExpiredNotif = pwExpiredNotif;
	}

	public void setPwHistorySize(Integer pwHistorySize) {
		this.pwHistorySize = pwHistorySize;
	}

	public void setPwInvalids(List<String> pwInvalids) {
		this.pwInvalids = pwInvalids;
	}

	public void setPwRules(String pwRules) {
		this.pwRules = pwRules;
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder(40);

		buf.append("<");
		buf.append(PasswordPolicy.LOCAL_PWPOLICY_ELEMENT);
		buf.append(">");

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(PasswordPolicy.PW_AGING_ELEMENT,
		    this.pwAging));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    PasswordPolicy.PW_EXPIRATION_NOTIF_ELEMENT, this.pwExpiredNotif));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(PasswordPolicy.PW_HISTORY_SIZE_ELEMENT,
		    this.pwHistorySize));

		buf.append("\n");

		buf.append(XmlUtility.toXMLString(PasswordPolicy.PW_INVALIDS_ELEMENT,
		    XmlUtility.arrayToString(this.pwInvalids, ",")));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(PasswordPolicy.PW_RULES_ELEMENT,
		    this.pwRules));

		buf.append("\n");
		buf.append("</");
		buf.append(PasswordPolicy.LOCAL_PWPOLICY_ELEMENT);
		buf.append(">");

		return buf.toString();
	}
}
