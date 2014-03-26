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

import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

/**
 * @author vnguyen AuthenticationData encapsulates information related to a
 *         specific user's authentication.
 */

/*
 * <!-- ===== authentication_T ===== --> <xsd:complexType
 * name="authentication_T"> <xsd:annotation> <xsd:documentation> This structure
 * holds all user authentication realted information. authenticationType - The
 * type of authentication supported for this user. internalAccountData -
 * Authentication specific data associated with "Internal" authentication of a
 * user. Must be present when authenticationType is set to "Internal".
 * authenticationState - The current authentication state of this user's
 * session. - A value of "expired" indicates that the user does not have an
 * authenticated session. - A value of "locked" indicates that the user's
 * account is currently locked due to repetative authentication failures. The
 * lastAuthenticationStateChange element will indicate the time this state was
 * entered, and therefore, combined with the lockoutPeriod value will indicate
 * when the next authentication can be accepted. - A value of "authenticating"
 * indicates that the user is currently going through an authentication attempt.
 * - A value of "valid" indicates the user has a current authenticated session.
 * lastAuthenticationStateChange - Date and time of last authenticationState
 * change. Used primarily to compute a transition from a "locked" state when
 * combined with the lockoutPeriod value. userAccountPolicy - User specific
 * settings that will override any global account policy settings for this user.
 * This element will only exist if there are user specific overrides to the
 * global policies. auditData - Audit information to track a user's valid and
 * invalid login attempts. wsdlCredential - A user provisioned credential that
 * will be used by this specific user to authenticate via the DRAC WSDL
 * interface. </xsd:documentation> </xsd:annotation> <xsd:sequence> <xsd:element
 * name="authenticationType" type="authenticationType_T"/> <xsd:element
 * name="internalAccountData" type="internalAuthentication_T" minOccurs="0"/>
 * <xsd:element name="authenticationState" type="authenticationState_T"
 * default="expired"/> <xsd:element name="lastAuthenticationStateChange"
 * type="dateTime_T"/> <xsd:element name="userAccountPolicy"
 * type="accountPolicy_T" minOccurs="0"/> <xsd:element name="auditData"
 * type="authenticationAuditData_T"/> <xsd:element name="wsdlCredential"
 * type="xsd:string" minOccurs="0"/> </xsd:sequence> </xsd:complexType>
 */

/*
 * XML Sample <authentication> <authenticationType>Internal</authenticationType>
 * <internalAccountData>
 * <userPassword>1c9df49b71e05b56c080fe58f480b657</userPassword>
 * <lastPasswordChanged>2002-05-30T09:30:10-0400</lastPasswordChanged>
 * <expirationDate>2009-05-30T09:30:10-0400</expirationDate> <passwordHistory
 * oldPassword="admin" dateChanged="2002-05-30T09:30:10-0400"></passwordHistory>
 * <passwordHistory oldPassword="admin2"
 * dateChanged="2000-05-30T09:30:10-0400"></passwordHistory>
 * </internalAccountData> <authenticationState>valid</authenticationState>
 * <lastAuthenticationStateChange
 * >2002-05-30T09:30:10-0400</lastAuthenticationStateChange> <userAccountPolicy>
 * <localPasswordPolicy> <passwordAging>100</passwordAging>
 * <passwordExpirationNotification>10</passwordExpirationNotification>
 * <passwordHistorySize>2</passwordHistorySize>
 * <invalidPasswords></invalidPasswords> <passwordRules></passwordRules>
 * </localPasswordPolicy> <dormantPeriod>0</dormantPeriod>
 * <inactivityPeriod>0</inactivityPeriod>
 * <maxInvalidLoginAttempts>3</maxInvalidLoginAttempts>
 * <lockoutPeriod>0</lockoutPeriod>
 * <lockedClientIPs>10.2.2.2,10.1.1.1</lockedClientIPs> </userAccountPolicy>
 * <auditData> <lastLoginAddress>localhost</lastLoginAddress>
 * <numOfInvalidAttempts>2</numOfInvalidAttempts>
 * <locationOfInvalidAttempts>localhost,100.192.1.1</locationOfInvalidAttempts>
 * </auditData>
 * <wsdlCredential>1c9df49b71e05b56c080fe58f480b657</wsdlCredential>
 * </authentication>
 */

public final class AuthenticationData implements Serializable {
  
  private static final Logger log = LoggerFactory.getLogger(AuthenticationData.class);
  
	private static final long serialVersionUID = 1;

	public static final String AUTHENTICATION_ELEMENT = "authentication";
	public static final String WSDL_CREDENTIAL_ELEMENT = "wsdlCredential";
	public static final String LAST_AUTHEN_STATE_CHANGE_ELEMENT = "lastAuthenticationStateChange";

	private UserProfile.AuthenticationType authenticationType;
	private InternalAccountData internalAccountData;
	private UserProfile.AuthenticationState authenticationState;
	private Calendar lastAuthenticationStateChange;
	private UserAccountPolicy userAccountPolicy;
	private AuthenticationAuditData auditData;
	private String wSDLCredential;

	private static int DAYS_IN_MILSEC = 1000 * 24 * 60 * 60;
	public static int NO_EXPIRED_NOTIF = -1;

	public AuthenticationData() {
		this.authenticationType = UserProfile.AuthenticationType.INTERNAL;
		this.authenticationState = UserProfile.AuthenticationState.EXPIRED;
		this.lastAuthenticationStateChange = Calendar.getInstance();
		this.userAccountPolicy = new UserAccountPolicy();
		this.auditData = new AuthenticationAuditData();
		this.internalAccountData = new InternalAccountData();
	}

	/**
	 * Create a UserAccountPolicy object with internal values set. On a
	 * field-by-field basis if the user specified value is empty, use the global
	 * security one instead
	 */
	public static UserAccountPolicy compareUserAndGlobalPolicies(
	    UserAccountPolicy localPolicy, UserAccountPolicy globalPolicy) {

		UserAccountPolicy result = new UserAccountPolicy();

		try {
			if (localPolicy.getDormantPeriod() != null) {
				result.setDormantPeriod(localPolicy.getDormantPeriod());
			}
			else {
				result.setDormantPeriod(globalPolicy.getDormantPeriod());
			}

			if (localPolicy.getInactivityPeriod() != null) {
				result.setInactivityPeriod(localPolicy.getInactivityPeriod());
			}
			else {
				result.setInactivityPeriod(globalPolicy.getInactivityPeriod());
			}

			if (localPolicy.getLockoutPeriod() != null) {
				result.setLockoutPeriod(localPolicy.getLockoutPeriod());
			}
			else {
				result.setLockoutPeriod(globalPolicy.getLockoutPeriod());
			}

			if (localPolicy.getMaxInvalidLoginAttempts() != null) {
				result.setMaxInvalidLoginAttempts(localPolicy
				    .getMaxInvalidLoginAttempts());
			}
			else {
				result.setMaxInvalidLoginAttempts(globalPolicy
				    .getMaxInvalidLoginAttempts());
			}

			// For locked IP's, lists should be joined
			result.getLockedClientIPs().addAll(localPolicy.getLockedClientIPs());
			result.getLockedClientIPs().addAll(globalPolicy.getLockedClientIPs());

			// password policy has its own fields, need to check one-by-one
			PasswordPolicy userPasswordPolicy = localPolicy.getLocalPasswordPolicy();
			PasswordPolicy passwordPolicy = globalPolicy.getLocalPasswordPolicy();

			if (userPasswordPolicy.getPwAging() != null) {
				result.getLocalPasswordPolicy().setPwAging(
				    userPasswordPolicy.getPwAging());
			}
			else {
				result.getLocalPasswordPolicy().setPwAging(passwordPolicy.getPwAging());
			}

			if (userPasswordPolicy.getPwExpiredNotif() != null) {
				result.getLocalPasswordPolicy().setPwExpiredNotif(
				    userPasswordPolicy.getPwExpiredNotif());
			}
			else {
				result.getLocalPasswordPolicy().setPwExpiredNotif(
				    passwordPolicy.getPwExpiredNotif());
			}

			if (userPasswordPolicy.getPwHistorySize() != null) {
				result.getLocalPasswordPolicy().setPwHistorySize(
				    userPasswordPolicy.getPwHistorySize());
			}
			else {
				result.getLocalPasswordPolicy().setPwHistorySize(
				    passwordPolicy.getPwHistorySize());
			}

			// lists should be joined
			// result.getLocalPasswordPolicy().getPwRules().addAll(userPasswordPolicy.getPwRules());
			// result.getLocalPasswordPolicy().getPwRules().addAll(passwordPolicy.getPwRules());

			if (userPasswordPolicy.getPwRules() != null) {
				result.getLocalPasswordPolicy().setPwRules(
				    userPasswordPolicy.getPwRules());
			}
			else {
				result.getLocalPasswordPolicy().setPwRules(passwordPolicy.getPwRules());
			}

			result.getLocalPasswordPolicy().getPwInvalids()
			    .addAll(userPasswordPolicy.getPwInvalids());
			result.getLocalPasswordPolicy().getPwInvalids()
			    .addAll(passwordPolicy.getPwInvalids());

		}
		catch (Exception e) {
			log.error("Error in compareUserAndGlobalPolicies", e);
			return new UserAccountPolicy();
		}

		return result;
	}

	public void fromXML(Element root) throws Exception {

		if (root == null) {
			return;
		}

		Element authenticationElement = root
		    .getChild(AuthenticationData.AUTHENTICATION_ELEMENT);

		if (authenticationElement == null) {
			return;
		}
		/*****************************************************************/
		/* Construct AuthenticationData. */
		/*****************************************************************/

		this.authenticationType = UserProfile.AuthenticationType
		    .fromXMLToObject(authenticationElement);

		InternalAccountData internalAccountData = new InternalAccountData();
		internalAccountData.fromXML(authenticationElement);
		this.internalAccountData = internalAccountData;

		this.authenticationState = UserProfile
		    .fromXMLToObject(authenticationElement);

		String lastAuthenticationStateChangeStr = authenticationElement
		    .getChildText(AuthenticationData.LAST_AUTHEN_STATE_CHANGE_ELEMENT);
		if (lastAuthenticationStateChangeStr != null
		    && !lastAuthenticationStateChangeStr.equals("")) {
			this.lastAuthenticationStateChange = DateFormatter
			    .getDateFromString(lastAuthenticationStateChangeStr);
		}

		UserAccountPolicy policy = new UserAccountPolicy();
		policy.fromXML(authenticationElement);
		this.userAccountPolicy = policy;

		AuthenticationAuditData auditDat = new AuthenticationAuditData();
		auditDat.fromXML(authenticationElement);
		this.auditData = auditDat;

		String wSDLCredentialStr = authenticationElement
		    .getChildText(AuthenticationData.WSDL_CREDENTIAL_ELEMENT);
		if (!(wSDLCredentialStr == null || wSDLCredentialStr.equals(""))) {
			CryptedString wSDLCredential = new CryptedString(wSDLCredentialStr);
			this.wSDLCredential = CryptoWrapper.INSTANCE.decrypt(wSDLCredential);
		}

		// 
	}

	public AuthenticationAuditData getAuditData() {
		return auditData;
	}

	public UserProfile.AuthenticationState getAuthenticationState() {
		return authenticationState;
	}

	public UserProfile.AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	public InternalAccountData getInternalAccountData() {
		return internalAccountData;
	}

	public Calendar getLastAuthenticationStateChange() {
		return lastAuthenticationStateChange;
	}

	public UserAccountPolicy getUserAccountPolicy() {
		return userAccountPolicy;
	}

	public String getWSDLCredential() {
		return wSDLCredential;
	}

	public boolean isDormant(UserAccountPolicy userAccountPolicy) {

		Calendar dormantTime = (Calendar) this.getLastAuthenticationStateChange()
		    .clone();

		// UserAccountPolicy userAccountPolicy =
		// this.getUserAccountPolicyIfEmptyThenGlobal();

		if (userAccountPolicy == null) {
			return false;
		}

		Integer dormantPeriod = userAccountPolicy.getDormantPeriod();

		if (dormantPeriod == null || dormantPeriod.intValue() == 0) {
			return false;
		}

		dormantTime.add(Calendar.DAY_OF_MONTH, dormantPeriod.intValue());

		if (dormantTime.compareTo(Calendar.getInstance()) < 0) {
			return true;
		}

		return false;
	}

	public boolean isExceedingMaxAttempts(UserAccountPolicy userAccountPolicy,
	    int currentAttempts) {

		// UserAccountPolicy userAccountPolicy =
		// this.getUserAccountPolicyIfEmptyThenGlobal();
		if (userAccountPolicy.getMaxInvalidLoginAttempts() == null) {
			return false;
		}
		if (userAccountPolicy.getMaxInvalidLoginAttempts().intValue() == 0) {
			return false;
		}

		log.debug("MaxLoggingAttemps/CurrentAttempt: "
		    + userAccountPolicy.getMaxInvalidLoginAttempts().intValue() + " vs "
		    + currentAttempts);
		return userAccountPolicy.getMaxInvalidLoginAttempts().intValue() <= currentAttempts;

	}

	public boolean isInLockoutPeriod(UserAccountPolicy userAccountPolicy) {

		if (!this.getAuthenticationState().equals(
		    UserProfile.AuthenticationState.LOCKED)) {
			return false;
		}

		// else
		Calendar lockoutTime = (Calendar) this.getLastAuthenticationStateChange()
		    .clone();

		// UserAccountPolicy userAccountPolicy =
		// this.getUserAccountPolicyIfEmptyThenGlobal();

		if (userAccountPolicy == null) {
			return false;
		}

		Integer lockedout = userAccountPolicy.getLockoutPeriod();

		if (lockedout == null || lockedout.intValue() == 0) {
			return false;
		}

		lockoutTime.add(Calendar.SECOND, lockedout.intValue());

		log.debug("Last login failed + lockedout(" + lockedout.intValue()
		    + " in sec): " + DateFormatter.dateToString(lockoutTime));
		if (lockoutTime.compareTo(Calendar.getInstance()) > 0) {
			return true;
		}

		return false;
	}

	public boolean isPasswordExpired(UserAccountPolicy userAccountPolicy) {

		if (this.getInternalAccountData() == null) {
			
			return false;
		}

		// UserAccountPolicy userAccountPolicy =
		// this.getUserAccountPolicyIfEmptyThenGlobal();
		if (userAccountPolicy == null) {
			return false;
		}

		PasswordPolicy passwordPolicy = userAccountPolicy.getLocalPasswordPolicy();
		if (passwordPolicy == null) {
			
			return false;
		}

		Integer pwAging = userAccountPolicy.getLocalPasswordPolicy().getPwAging();
		// 

		if (pwAging == null || pwAging.intValue() == 0) {
			return false;
		}

		return this.getInternalAccountData().isPasswordExpired();
	}

	public void setAuditData(AuthenticationAuditData auditData) {
		this.auditData = auditData;
	}

	public void setAuthenticationState(
	    UserProfile.AuthenticationState authenticationState) {
		this.authenticationState = authenticationState;
	}

	public void setAuthenticationType(
	    UserProfile.AuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
	}

	public void setInternalAccountData(InternalAccountData internalAccountData) {
		this.internalAccountData = internalAccountData;
	}

	public void setLastAuthenticationStateChange(
	    Calendar lastAuthenticationStateChange) {
		this.lastAuthenticationStateChange = lastAuthenticationStateChange;
	}

	public void setUserAccountPolicy(UserAccountPolicy userAccountPolicy) {
		this.userAccountPolicy = userAccountPolicy;
	}

	public void setWSDLCredential(String credential) {
		wSDLCredential = credential;
	}

	public int shouldPasswordExpireddNotify(UserAccountPolicy userAccountPolicy) {

		int dayLeft = NO_EXPIRED_NOTIF;

		// UserAccountPolicy userAccountPolicy =
		// this.getUserAccountPolicyIfEmptyThenGlobal();
		if (userAccountPolicy == null) {
			return dayLeft;
		}

		PasswordPolicy passwordPolicy = userAccountPolicy.getLocalPasswordPolicy();
		if (passwordPolicy == null) {
			
			return dayLeft;
		}

		Integer notifyDay = passwordPolicy.getPwExpiredNotif();
		// 

		if (notifyDay == null || notifyDay.intValue() == 0) {
			return dayLeft;
		}

		// we need to check if passwordExpired is disabled or not
		// if it is then return false
		Integer pwAging = userAccountPolicy.getLocalPasswordPolicy().getPwAging();
		if (pwAging == null || pwAging.intValue() == 0) {
			return dayLeft;
		}

		// Up to this point, every necessary fields have been checked. The only
		// thing
		// left is the expiration date. Since the pwaging is not disabled, then this
		// field should not be null.
		if (this.getInternalAccountData().getExpirationDate() == null) {
			
			return dayLeft;
		}

		// Now, the last step is to check with the current password expired of the
		// user profile
		Calendar todayPlusNotif = Calendar.getInstance();
		todayPlusNotif.add(Calendar.DAY_OF_YEAR, notifyDay.intValue());

		long diff = -1;

		if (todayPlusNotif.getTimeInMillis()
		    - this.getInternalAccountData().getExpirationDate().getTimeInMillis() > 0) {
			diff = this.getInternalAccountData().getExpirationDate()
			    .getTimeInMillis()
			    - Calendar.getInstance().getTimeInMillis();
		}

		if (diff < 0) {
			return dayLeft;
		}

		// we compute the actual dayLeft
		dayLeft = (int) (diff / DAYS_IN_MILSEC);

		

		return dayLeft;
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder();

		// Authentication attributes
		buf.append("<");
		buf.append(AuthenticationData.AUTHENTICATION_ELEMENT);
		buf.append(">");

		// UserProfile Elements
		if (this.authenticationType != null) {
			buf.append(" ");
			buf.append(this.authenticationType.toXMLString());
		}

		if (this.internalAccountData != null) {
			buf.append(" ");
			buf.append(this.internalAccountData.toXMLString());
		}

		buf.append(" ");
		buf.append(this.authenticationState.toXMLString());

		if (this.lastAuthenticationStateChange != null) {
			buf.append(" ");
			buf.append(XmlUtility.toXMLString(LAST_AUTHEN_STATE_CHANGE_ELEMENT,
			    DateFormatter.dateToString(this.lastAuthenticationStateChange)));
		}

		if (this.userAccountPolicy != null) {
			buf.append(" ");
			buf.append(this.userAccountPolicy.toXMLString());
		}

		if (this.auditData != null) {
			buf.append(" ");
			buf.append(this.auditData.toXMLString());
		}

		buf.append(" ");

		try {
			buf.append(XmlUtility.toXMLString(WSDL_CREDENTIAL_ELEMENT,
			    this.wSDLCredential == null ? null : CryptoWrapper.INSTANCE
			        .encrypt(this.wSDLCredential)));
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}

		// end AuthenticationData
		buf.append("</" + AuthenticationData.AUTHENTICATION_ELEMENT + ">");
		return buf.toString();
	}

	// This method can only be called on the server/NRB_PORT
	/*
	 * public UserAccountPolicy getUserAccountPolicyIfEmptyThenGlobal() {
	 * UserAccountPolicy globalPolicy = null; // Get the global policy account
	 * policy object, because it does new object creation there // then check each
	 * field and overwrite with the user account policy if needed try {
	 * globalPolicy =
	 * UserPolicyManager.INSTANCE.getGlobalPolicy(null).getLocalAccountPolicy
	 * (); } catch(Exception e){ log.error("cannot retrieve global policy", e); }
	 * return
	 * AuthenticationData.compareUserAndGlobalPolicies(this.userAccountPolicy,
	 * globalPolicy); }
	 */

}
