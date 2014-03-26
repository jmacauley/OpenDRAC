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

import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class InternalAccountData implements Serializable {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	/*
	 * <xsd:complexType name="internalAuthentication_T"> <xsd:annotation>
	 * <xsd:documentation> This element is associated with a user entry when their
	 * authenticationType is set to internal. userPassword - The encrypted
	 * password used to authenticate the user associated with this entry.
	 * lastPasswordChange - The date of the last password change. This is used to
	 * calculate the interval for the next valid password change, or when a forced
	 * password change must occur. passwordHistory - A list of old passwords for
	 * this user and the date they were changed. The size of the list of old
	 * passwords is dicated by the passwordHistorySize value. Entries in this list
	 * should be treated FIFO. expirationDate - The date the user's current
	 * password will expire. </xsd:documentation> </xsd:annotation> <xsd:sequence>
	 * <xsd:element name="userPassword" type="xsd:string"/> <xsd:element
	 * name="lastPasswordChange" type="dateTime_T" minOccurs="0"/> <xsd:element
	 * name="expirationDate" type="dateTime_T" minOccurs="0"/> <xsd:element
	 * name="passwordHistory" type="passwordHistory_T" minOccurs="0"
	 * maxOccurs="unbounded"/> </xsd:sequence> </xsd:complexType>
	 */

	/*
	 * XML sample of an internalAccountData <internalAccountData>
	 * <userPassword>1c9df49b71e05b56c080fe58f480b657</userPassword>
	 * <lastPasswordChanged>2002-05-30T09:30:10-0400</lastPasswordChanged>
	 * <expirationDate>2009-05-30T09:30:10-0400</expirationDate> <passwordHistory
	 * oldPassword="admin"
	 * dateChanged="2002-05-30T09:30:10-0400"></passwordHistory> <passwordHistory
	 * oldPassword="admin2"
	 * dateChanged="2000-05-30T09:30:10-0400"></passwordHistory>
	 * </internalAccountData>
	 */

	private static final long serialVersionUID = 1L;
	private String userPassword;
	private Calendar lastPasswordChanged;
	private Calendar expirationDate;
	private List<PasswordHistory> history;

	public static final String INTERNAL_ACCOUNT_ELEMENT = "internalAccountData";
	public static final String USER_PASSWORD_ELEMENT = "userPassword";
	public static final String LAST_PWCHANGED_ELEMENT = "lastPasswordChanged";
	public static final String EXPIRATION_DATE_ELEMENT = "expirationDate";

	public InternalAccountData() {
		this.history = new ArrayList<PasswordHistory>();
	}

	public void addPasswordHistory(PasswordHistory pwHist) {
		if (this.history == null) {
			this.history = new ArrayList<PasswordHistory>();
		}
		this.history.add(pwHist);
	}

	public void fromXML(Element node) throws Exception {
		/*****************************************************************/
		/* Construct InternalAccountData. */
		/*****************************************************************/
		Element internalAccountElement = node
		    .getChild(InternalAccountData.INTERNAL_ACCOUNT_ELEMENT);

		if (internalAccountElement != null && !internalAccountElement.equals("")) {

			String userPW = internalAccountElement
			    .getChildText(InternalAccountData.USER_PASSWORD_ELEMENT);
			String decryptedUserPW = null;
			if (userPW == null || userPW.equals("")) {
				log.error("userPassword element should not be: <" + userPW + "> for "
				    + XmlUtility.rootNodeToString(node));
				// return;
			}
			else {
				decryptedUserPW = CryptoWrapper.INSTANCE.decrypt(
				    new CryptedString(userPW));
			}

			this.setUserPassword(decryptedUserPW);

			// lastPWChange attribute
			String lastPWChangeStr = internalAccountElement
			    .getChildText(InternalAccountData.LAST_PWCHANGED_ELEMENT);
			if (lastPWChangeStr != null && !lastPWChangeStr.equals("")) {
				Calendar lastPWChange = DateFormatter
				    .getDateFromString(lastPWChangeStr);
				this.setLastPasswordChanged(lastPWChange);
			}

			// expirationDate attribute
			String expirationDateStr = internalAccountElement
			    .getChildText(InternalAccountData.EXPIRATION_DATE_ELEMENT);
			if (expirationDateStr != null && !expirationDateStr.equals("")) {
				Calendar expirationDate = DateFormatter
				    .getDateFromString(expirationDateStr);
				this.setExpirationDate(expirationDate);
			}

			// get all password history
			List<Element> historyList = internalAccountElement
			    .getChildren(PasswordHistory.PASSWORD_HISTORY_ELEMENT);
			if (historyList != null) {
				for (Element element : historyList) {
					PasswordHistory hist = new PasswordHistory(null, null);
					hist.fromXML(element);
					this.history.add(hist);
				}
			}

		}
	}

	public Calendar getExpirationDate() {
		return expirationDate;
	}

	// public List<PasswordHistory> getHistory()
	// {
	// return history;
	// }

	public Calendar getLastPasswordChanged() {
		return lastPasswordChanged;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public boolean isPasswordExpired() {
		return Calendar.getInstance().compareTo(this.getExpirationDate()) > 0;
	}

	public void setExpirationDate(Calendar expirationDate) {
		this.expirationDate = expirationDate;
	}

	public void setLastPasswordChanged(Calendar lastPasswordChanged) {
		this.lastPasswordChanged = lastPasswordChanged;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	@Override
	public String toString() {
		return this.toXMLString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder();

		buf.append("<");
		buf.append(InternalAccountData.INTERNAL_ACCOUNT_ELEMENT);
		buf.append(">");

		buf.append("\n");

		CryptedString encryptedPW = null;
		if (this.userPassword != null) {
			try {
				encryptedPW = CryptoWrapper.INSTANCE.encrypt(this.userPassword);
			}
			catch (Exception e) {
				log.error("Error: ", e);
			}
		}

		buf.append(XmlUtility.toXMLString(
		    InternalAccountData.USER_PASSWORD_ELEMENT, encryptedPW == null ? null
		        : encryptedPW.toString()));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    InternalAccountData.LAST_PWCHANGED_ELEMENT,
		    this.lastPasswordChanged == null ? null : DateFormatter
		        .dateToString(this.lastPasswordChanged)));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    InternalAccountData.EXPIRATION_DATE_ELEMENT,
		    this.expirationDate == null ? null : DateFormatter
		        .dateToString(this.expirationDate)));

		for (PasswordHistory passwordHistory : this.history) {
			buf.append("\n");
			buf.append(passwordHistory.toXMLString());

		}

		buf.append("\n");
		buf.append("</");
		buf.append(InternalAccountData.INTERNAL_ACCOUNT_ELEMENT);
		buf.append(">");

		return buf.toString();

	}

}
