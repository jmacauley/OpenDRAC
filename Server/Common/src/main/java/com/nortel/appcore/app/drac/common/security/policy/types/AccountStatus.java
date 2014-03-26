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

/*
 * <!-- ===== accountStatus_T ===== --> <xsd:complexType name="accountStatus_T"> <xsd:annotation>
 * <xsd:documentation xml:lang="en"> This structure models the status of a user account. "enabled" - the user
 * account is enabled and can be used for authenticing a user. "disabled" - the user account is disabled and
 * cannot be used for authenticing a user. disabledReason - Holds a text string describing the reason the
 * account was disabled. </xsd:documentation> </xsd:annotation> <xsd:sequence> <xsd:element
 * name="accountState" type="accountState_T" default="enabled"/> <xsd:element name="disabledReason"
 * type="xsd:string" minOccurs="0"/> </xsd:sequence> </xsd:complexType>
 */

/*
 * XML Sample <accountStatus> <accountState>enabled</accountState> <disabledReason></disabledReason>
 * </accountStatus>
 */
public final class AccountStatus implements Serializable {
	private static final long serialVersionUID = 1;

	public static final String DISABLED_REASON_ELEMENT = "disabledReason";
	public static final String ACCOUNT_STATUS_ELEMENT = "accountStatus";

	private String disabledReason;
	private UserProfile.AccountState accountState;

	public AccountStatus() {
		this.accountState = UserProfile.AccountState.ENABLED;
	}

	public AccountStatus(UserProfile.AccountState state, String reason) {
		this.accountState = state;
		this.disabledReason = reason;
	}

	public void fromXML(Element root) throws Exception {

		if (root == null) {
			return;
		}

		Element accountStatusElement = root
		    .getChild(AccountStatus.ACCOUNT_STATUS_ELEMENT);

		if (accountStatusElement == null) {
			return;
		}
		/*****************************************************************/
		/* Construct AuthenticationData. */
		/*****************************************************************/

		this.accountState = UserProfile.AccountState
		    .fromXMLToObject(accountStatusElement);
		this.disabledReason = accountStatusElement
		    .getChildText(AccountStatus.DISABLED_REASON_ELEMENT);

		// 
	}

	public UserProfile.AccountState getAccountState() {
		return accountState;
	}

	public String getDisabledReason() {
		return disabledReason;
	}

	public void setAccountState(UserProfile.AccountState accountState) {
		this.accountState = accountState;
	}

	public void setDisabledReason(String disabledReason) {
		this.disabledReason = disabledReason;
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {

		StringBuilder buf = new StringBuilder();

		// Authentication attributes
		buf.append("<");
		buf.append(AccountStatus.ACCOUNT_STATUS_ELEMENT);
		buf.append(">");

		// UserProfile Elements
		if (this.accountState != null) {
			buf.append(" ");
			buf.append(this.accountState.toXMLString());
		}

		buf.append(" ");
		buf.append(XmlUtility.toXMLString(DISABLED_REASON_ELEMENT,
		    this.disabledReason));

		// end AuthenticationData
		buf.append(" ");
		buf.append("</" + AccountStatus.ACCOUNT_STATUS_ELEMENT + ">");
		return buf.toString();
	}
}
