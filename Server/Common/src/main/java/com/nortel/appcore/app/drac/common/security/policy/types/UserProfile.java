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
import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckablePolicy;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

/**
 * /* <xsd:complexType name="user_T"> <xsd:annotation> <xsd:documentation> This
 * type models information relating to an active user entry. userID - The user
 * identification string returned from the authentication system. This is used
 * to map into group names. authenticationState - The current state in the
 * authentication sequence for this user entry. accountState - The
 * enabled/disabled state this user entry. creationDate - Date when this user
 * entry was created. lastModifiedDate - Date of the last modification to this
 * user entry. membership - Associated user group memebership information.
 * </xsd:documentation> </xsd:annotation> <xsd:sequence> <xsd:element
 * name="authentication" type="authentication_T"/> <xsd:element
 * name="accountStatus" type="accountStatus_T"/> <xsd:element name="userData"
 * type="userInformation_T" minOccurs="0"/> <xsd:element name="organization"
 * type="orgInformation_T"/> <xsd:element name="membership"
 * type="membershipInformation_T" minOccurs="0"/> </xsd:sequence> <xsd:attribute
 * name="userID" type="xsd:string" use="required"/> <xsd:attribute
 * name="creationDate" type="dateTime_T" use="required"/> <xsd:attribute
 * name="lastModifiedDate" type="dateTime_T" use="required"/> </xsd:complexType>
 * 
 * @author vnguyen
 */

public final class UserProfile implements Serializable, PolicyCheckablePolicy {
  
  private static final Logger log = LoggerFactory.getLogger(UserProfile.class);

	/*******************************************************************************************
	 * AccountStatus: enabled, disabled A enumerated string type defining account
	 * states. enabled - the user account is enabled and can be used for
	 * authenticing a user. disabled - the user account is disabled and cannot be
	 * used for authenticing a user.
	 *******************************************************************************************/
	public static enum AccountState {
		ENABLED("enabled"), DISABLED("disabled");

		private static final String ACCOUNTSTATE_ELEMENT = "accountState";
		private static final long serialVersionUID = 1;
		private final String description;

		AccountState(String accountStateDescription) {
			description = accountStateDescription;
		}

		public static AccountState fromXMLToObject(Element root) throws Exception {
			/*********************************************************************/
			/* Constructs accountStatus attribute. */
			/*********************************************************************/
			String accountStatus = root.getChildText(ACCOUNTSTATE_ELEMENT);
			AccountState status = AccountState.DISABLED;

			if (accountStatus != null && !accountStatus.equals("")) {
				if (accountStatus.equals(AccountState.DISABLED.description)) {
					status = AccountState.DISABLED;

				}
				else if (accountStatus.equals(AccountState.ENABLED.description)) {
					status = AccountState.ENABLED;
				}
				else {
					log.error("Invalid accountStatus element: " + accountStatus);
				}
			}

			return status;
		}

		@Override
		public String toString() {
			return description;
		}

		public String toXMLString() {
			return XmlUtility.toXMLString(ACCOUNTSTATE_ELEMENT, description);
		}
	}

	/******************************************************************************************
	 * AuthenticationState: authenticating, valid, expired A enumerated string
	 * type defining user states. authenticating - the user associated with the
	 * entry is currently authenticating with the DRAC system. valid - the user
	 * associated with the entry is in an active state with a valid ticket.
	 * expired - the user associated with the entry does not have a valid ticket.
	 *******************************************************************************************/
	public static enum AuthenticationState {
		LOCKED("locked"), VALID("valid"), EXPIRED("expired");

		private static final long serialVersionUID = 1;
		private final String description;

		AuthenticationState(String authenticationStateDescription) {
			description = authenticationStateDescription;
		}

		@Override
		public String toString() {
			return description;
		}

		public String toXMLString() {
			return XmlUtility.toXMLString(UserProfile.AUTHENTICATIONSTATE_ELEMENT,
			    description);
		}
	}

	/**************************************************************************************
	 * AuthenticationType: internal, external Indicates if the user associated
	 * with the entry is using internal or external authentication mechanisms.
	 * internal - the internal user database is used to authenticate the user
	 * using simple userID and password capabilities. The internalAuthentication
	 * element will need to contain the uses stored password. external - an
	 * external authentication system is being used for this user.
	 **************************************************************************************/
	public static enum AuthenticationType {
		INTERNAL("Internal"), A_SELECT("A-Select"), RADIUS("RADIUS");

		private static final String AUTHENTICATIONTYPE_ELEMENT = "authenticationType";
		private static final long serialVersionUID = 1;
		private final String description;

		AuthenticationType(String authenticationTypeDescription) {
			description = authenticationTypeDescription;
		}

		public static AuthenticationType fromXMLToObject(Element root)
		    throws Exception {
			/****************************************************************/
			/* Construct authenticationType attribute. */
			/****************************************************************/
			String authenType = root.getChildText(AUTHENTICATIONTYPE_ELEMENT);
			AuthenticationType type = null;
			if (authenType != null && !authenType.equals("")) {
				if (authenType.equals(AuthenticationType.INTERNAL.toString())) {
					type = AuthenticationType.INTERNAL;

				}
				else if (authenType.equals(AuthenticationType.A_SELECT.toString())) {
					type = AuthenticationType.A_SELECT;
				}
				else if (authenType.equals(AuthenticationType.RADIUS.toString())) {
					type = AuthenticationType.RADIUS;
				}
				else {
					log.error("Invalid authentication type: '" + authenType + "'");
				}
			}

			return type;
		}

		@Override
		public String toString() {
			return description;
		}

		public String toXMLString() {
			return XmlUtility.toXMLString(AUTHENTICATIONTYPE_ELEMENT, description);
		}
	}

	public static final String AUTHENTICATIONSTATE_ELEMENT = "authenticationState";
	public static final String LASTMODIFIEDDATE_ATTR = "lastModifiedDate";
	public static final String USERID_ATTR = "userID";
	public static final String USER_ELEMENT = "user";

	private static final String CREATIONDATE_ATTR = "creationDate";
	private static final long serialVersionUID = 1;

	private String userID;
	private Calendar creationDate;
	private Calendar lastModifiedDate;
	private AuthenticationData authenticationData;
	private AccountStatus accountStatus;
	private PersonalData personalData;
	private OrganizationData organizationData;
	private MembershipData membershipData = new MembershipData(null);
	private UserPreferences preferences;

	public UserProfile() {
		this(null, null, null);
	}

	public UserProfile(String userProfileUserID,
	    Calendar userProfileCreationDate, Calendar userProfileLastModifiedDate) {

		userID = userProfileUserID;
		creationDate = userProfileCreationDate;
		lastModifiedDate = userProfileLastModifiedDate;
		accountStatus = new AccountStatus();
		personalData = new PersonalData();
		organizationData = new OrganizationData();
		authenticationData = new AuthenticationData();
		membershipData = new MembershipData(null);
		preferences = new UserPreferences();
	}

	public static UserProfile.AuthenticationState fromXMLToObject(Element root)
	    throws Exception {
		/*********************************************************************/
		/* Constructs authenticationState attribute. */
		/*********************************************************************/
		String authenState = root.getChildText(AUTHENTICATIONSTATE_ELEMENT);
		UserProfile.AuthenticationState state = null;

		if (authenState != null && !authenState.equals("")) {

			if (authenState
			    .equals(UserProfile.AuthenticationState.LOCKED.description)) {
				state = UserProfile.AuthenticationState.LOCKED;

			}
			else if (authenState
			    .equals(UserProfile.AuthenticationState.EXPIRED.description)) {
				state = UserProfile.AuthenticationState.EXPIRED;

			}
			else if (authenState
			    .equals(UserProfile.AuthenticationState.VALID.description)) {
				state = UserProfile.AuthenticationState.VALID;
			}
			else {
				log.error("Invalid authentication state: '" + authenState + "'");
			}
		}
		else {
			log.error("Error authentication state: " + authenState);
		}

		return state;
	}

	public void fromXML(Element root) throws Exception {

		if (root == null) {
			return;
		}
		String xmlUserID = root.getAttributeValue(UserProfile.USERID_ATTR);

		if (xmlUserID == null || xmlUserID.equals("")) {
			DracException ex = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { UserProfile.USERID_ATTR });
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		Calendar creationTime = null;
		Calendar lastModifiedTime = null;
		try {
			String creationTimeString = root
			    .getAttributeValue(UserProfile.CREATIONDATE_ATTR);
			creationTime = DateFormatter.getDateFromString(creationTimeString);

			String lastModifiedTimeString = root
			    .getAttributeValue(UserProfile.LASTMODIFIEDDATE_ATTR);
			lastModifiedTime = DateFormatter
			    .getDateFromString(lastModifiedTimeString);

		}
		catch (Exception ex) {
			DracException e = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { UserProfile.CREATIONDATE_ATTR + " or/and "
			        + UserProfile.LASTMODIFIEDDATE_ATTR }, ex);
			log.error(ex.getMessage(), e);
			throw e;
		}

		// this.userID= userID;
		this.userID = XmlUtility.convertXMLLiteralsToString(xmlUserID);
		this.creationDate = creationTime;
		this.lastModifiedDate = lastModifiedTime;

		// let get UserProfile attributes constructing themselve from XML Node.
		// some weird error with enum, we have to do a bit different here

		this.authenticationData.fromXML(root);
		this.accountStatus.fromXML(root);
		this.personalData.fromXML(root);
		this.organizationData.fromXML(root);
		this.membershipData.fromXML(root);
		this.preferences.fromXML(root);

	}

	public AccountStatus getAccountStatus() {
		return this.accountStatus;
	}

	public AuthenticationData getAuthenticationData() {
		return authenticationData;
	}

	public Calendar getCreationDate() {
		return creationDate;
	}

	public Calendar getLastModifiedDate() {
		return lastModifiedDate;
	}

	public MembershipData getMembershipData() {
		return membershipData;
	}

	public OrganizationData getOrganizationData() {
		return organizationData;
	}

	public PersonalData getPersonalData() {
		return personalData;
	}

	public UserPreferences getPreferences() {
		return preferences;
	}

	public String getUserID() {
		return userID;
	}

	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}

	public void setAuthenticationData(AuthenticationData authenticationData) {
		this.authenticationData = authenticationData;
	}

	public void setCreationDate(Calendar creationDate) {
		this.creationDate = creationDate;
	}

	public void setLastModifiedDate(Calendar lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public void setMembershipData(MembershipData membershipData) {
		this.membershipData = membershipData;
	}

	public void setOrganizationData(OrganizationData organizationData) {
		this.organizationData = organizationData;
	}

	public void setPersonalData(PersonalData personalData) {
		this.personalData = personalData;
	}

	public void setPreferences(UserPreferences preferences) {
		this.preferences = preferences;
	}

	public void setupUserAuthenticationAccount(AbstractCredential credential,
	    UserAccountPolicy globalPolicy) throws Exception {
		AuthenticationData authenData = this.getAuthenticationData();
		InternalAccountData internalAccountData = authenData
		    .getInternalAccountData();

		// if
		// (credential.getAuthenticateMethod().equals(AbstractCredential.AuthenticateMethod.A_SELECT))
		// {
		// authenData.setAuthenticationType(UserProfile.AuthenticationType.A_SELECT);
		// return;
		// }

		String oldPassword = null;
		Integer passwordAging = null;
		Calendar newExpiryDate = Calendar.getInstance();
		
		if (globalPolicy != null) {
			passwordAging = globalPolicy.getLocalPasswordPolicy().getPwAging();
		}
		if (passwordAging == null) {
			log.debug("Password Aging should not be null serring it to 0");
			// For now we will treat it as no password expired
			passwordAging = 0;
		}
		if ("-1".equals(passwordAging)) {
      log.debug("Password aging set to -1 for user {}", userID);
      // will never expire
      newExpiryDate.add(Calendar.YEAR, 10);
    }
		else {
		  newExpiryDate.add(Calendar.DAY_OF_YEAR, passwordAging.intValue());
		}

		if (credential.getAuthenticateMethod().equals(
		    AbstractCredential.AuthenticateMethod.LOCAL_ACCOUNT)) {
			authenData.setAuthenticationType(UserProfile.AuthenticationType.INTERNAL);
			oldPassword = this.getAuthenticationData().getInternalAccountData()
			    .getUserPassword();

			internalAccountData.setUserPassword(credential.getPassword());
			internalAccountData.setLastPasswordChanged(Calendar.getInstance());
			internalAccountData.setExpirationDate(newExpiryDate);
			if (oldPassword != null) {
				internalAccountData.addPasswordHistory(new PasswordHistory(
				    CryptoWrapper.INSTANCE.encrypt(oldPassword).toString(),
				    internalAccountData.getLastPasswordChanged()));
			}

		}
		else if (credential.getAuthenticateMethod().equals(
		    AbstractCredential.AuthenticateMethod.WEB_SERVICES)) {
			oldPassword = this.getAuthenticationData().getWSDLCredential();

			authenData.setWSDLCredential(credential.getPassword());
			internalAccountData.setLastPasswordChanged(Calendar.getInstance());
			internalAccountData.setExpirationDate(newExpiryDate);
			if (oldPassword != null) {
				internalAccountData.addPasswordHistory(new PasswordHistory(
				    CryptoWrapper.INSTANCE.encrypt(oldPassword).toString(),
				    internalAccountData.getLastPasswordChanged()));
			}
		}
	}

	public void setupUserAuthenticationAccount(GlobalPolicy globalPolicy,
	    AbstractCredential credential) throws Exception {
		AuthenticationData authenData = this.getAuthenticationData();
		UserAccountPolicy policy = AuthenticationData
		    .compareUserAndGlobalPolicies(authenData.getUserAccountPolicy(),
		        globalPolicy.getLocalAccountPolicy());
		setupUserAuthenticationAccount(credential, policy);
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	@Override
	public String toString() {
		return toXMLString();
		// StringBuilder buf = new StringBuilder(20);
		// buf.append("<");
		// buf.append(PersonalData.USERDATA_ELEMENT);
		// buf.append(" " + UserProfile.USERID_ATTR + "='" + this.userID + "'");
		// buf.append(" " + UserProfile.CREATIONDATE_ATTR + "='" +
		// DateFormatter.dateToString(this.creationDate) + "'");
		// buf.append(" " + UserProfile.LASTMODIFIEDDATE_ATTR + "='" +
		// DateFormatter.dateToString(this.lastModifiedDate) + "'");
		// buf.append(">");
		// buf.append("</" + PersonalData.USERDATA_ELEMENT + ">");
		// return buf.toString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder(30);

		// UserProfile attributes
		buf.append("<");
		buf.append(UserProfile.USER_ELEMENT);
		buf.append(" " + UserProfile.USERID_ATTR + "=\""
		    + XmlUtility.convertStringToXMLLiterals(this.userID) + "\"");
		buf.append(" " + UserProfile.LASTMODIFIEDDATE_ATTR + "=\""
		    + DateFormatter.dateToString(this.lastModifiedDate) + "\"");
		buf.append(" " + UserProfile.CREATIONDATE_ATTR + "=\""
		    + DateFormatter.dateToString(this.creationDate) + "\"");
		buf.append(">");

		if (this.authenticationData != null) {
			buf.append(" ");
			buf.append(this.authenticationData.toXMLString());
		}

		if (this.accountStatus != null) {
			buf.append(" ");
			buf.append(this.accountStatus.toXMLString());
		}

		if (this.personalData != null) {
			buf.append(" ");
			buf.append(this.personalData.toXMLString());
		}

		if (this.organizationData != null) {
			buf.append(" ");
			buf.append(this.organizationData.toXMLString());
		}

		buf.append(" ");
		buf.append(this.membershipData.toXMLString());

		if (this.preferences != null) {
			buf.append(" ");
			buf.append(this.preferences.toXMLString());
		}

		// End UserProfile
		buf.append("</" + UserProfile.USER_ELEMENT + ">");
		return buf.toString();
	}

}
