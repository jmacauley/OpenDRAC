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

package com.nortel.appcore.app.drac.security.authentication;

import java.util.Calendar;
import java.util.List;

import org.opendrac.security.policy.LoginAttemptsPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionWarningCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationAuditData;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.security.policy.types.UserAccountPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbGlobalPolicy;
import com.nortel.appcore.app.drac.database.dracdb.DbUser;

public final class LocalAccountAuthenticatorHelper {
    
	private static InvalidAttemptCache invalidAttemptCache = new InvalidAttemptCache();
	
	private static final Logger log = LoggerFactory.getLogger(LocalAccountAuthenticatorHelper.class);
	
	private LocalAccountAuthenticatorHelper() {
	}

	public static InvalidAttemptCache getInvalidAttemptCache() {
		return invalidAttemptCache;
	}

	// This method can only be called on the server/NRB_PORT
	public static UserAccountPolicy getUserAccountPolicyDerivedFromLocalAndGlobal(
	    UserAccountPolicy localAccount) {
		UserAccountPolicy globalAccount = null;

		// Get the global policy account policy object, because it does new object
		// creation there
		// then check each field and overwrite with the user account policy if
		// needed
		try {
			globalAccount = DbGlobalPolicy.INSTANCE.getGlobalPolicy()
			    .getLocalAccountPolicy();
		}
		catch (Exception e) {
			log.error("cannot retrieve global policy", e);
			return localAccount;
		}

		globalAccount = AuthenticationData.compareUserAndGlobalPolicies(
		    localAccount, globalAccount);

		return globalAccount;
	}

	public static void updatePasswordExpireddNotification(UserDetails userDetails) {

		/***********************************************************/
		/* Now we check password expired notification */
		/***********************************************************/
		UserProfile userProfile = userDetails.getUserPolicyProfile()
		    .getUserProfile();
		int dayLeft = userProfile.getAuthenticationData()
		    .shouldPasswordExpireddNotify(
		        LocalAccountAuthenticatorHelper
		            .getUserAccountPolicyDerivedFromLocalAndGlobal(userProfile
		                .getAuthenticationData().getUserAccountPolicy()));
		if (dayLeft > AuthenticationData.NO_EXPIRED_NOTIF) {
			userDetails
			    .addWarningCode(SessionWarningCode.PASSWORD_EXPIRING_NOTIFICATION);
			
			userDetails.getCredential().getLoginAuditData()
			    .setExpiringNotificationDays(dayLeft);
		}
	}

	public static UserDetails updateUserProfileForLogin(UserDetails userDetails,
	    SessionErrorCode errorCode) {

		try {
			UserProfile userProfile = userDetails.getUserPolicyProfile()
			    .getUserProfile();
			AuthenticationData authenData = userProfile.getAuthenticationData();
			// init the state no matter what
			authenData
			    .setAuthenticationState(UserProfile.AuthenticationState.EXPIRED);
			// audit data
			AuthenticationAuditData auditData = authenData.getAuditData();
			if (auditData == null) {
				auditData = new AuthenticationAuditData();
			}

			int numOfLoginFailure = -1;
			// authentication state
			IPAddress ipAddress = userDetails.getCredential().getIpAddress();

			if (errorCode
			    .equals(SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_TO_BE_LOCKEDOUT)) {
				authenData
				    .setAuthenticationState(UserProfile.AuthenticationState.LOCKED);
				auditData.addLocationOfInvalidAttempt(ipAddress);
				auditData.incrementNumOfInvalidAttempts();
			}
			else if (errorCode
			    .equals(SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_RETRY)) {
				authenData
				    .setAuthenticationState(UserProfile.AuthenticationState.EXPIRED);
				authenData.setLastAuthenticationStateChange(Calendar.getInstance());
				auditData.addLocationOfInvalidAttempt(ipAddress);
				auditData.incrementNumOfInvalidAttempts();
			}
			else if (errorCode.equals(SessionErrorCode.ERROR_DORMANT)) {
				authenData.setLastAuthenticationStateChange(Calendar.getInstance());
				userProfile.setAccountStatus(new AccountStatus(
				    UserProfile.AccountState.DISABLED, SessionErrorCode.ERROR_DORMANT
				        .toString()));
				DbUser.INSTANCE.setUserAccountStatus(userProfile.getUserID(),
				    userProfile.getAccountStatus());
			}
			else if (errorCode.equals(SessionErrorCode.NO_ERROR)) {
				authenData.setLastAuthenticationStateChange(Calendar.getInstance());
				authenData
				    .setAuthenticationState(UserProfile.AuthenticationState.VALID);
				auditData.addLoginAddress(ipAddress);
				numOfLoginFailure = auditData.getNumberOfInvalidAttempts();
				auditData.setNumOfInvalidAttempts(0);
			}
			else if (errorCode.equals(SessionErrorCode.ERROR_LOCKOUT)) {
				return userDetails;
			}			
			else if (errorCode.equals(SessionErrorCode.ERROR_LOCKED_CLIENT_IP_ADDRESSS)) {
				return userDetails;
			}
			else if (errorCode.equals(SessionErrorCode.ERROR_LOCKED_CLIENT_SESSION)) {
				return userDetails;
			}				
			else {
				auditData.addLocationOfInvalidAttempt(ipAddress);
				auditData.incrementNumOfInvalidAttempts();
			}

			// Now we will update authenticationData to db
			DbUser.INSTANCE.setUserAuthenticationData(userProfile.getUserID(),
			    authenData);

			if (numOfLoginFailure != -1) {
				auditData.setNumOfInvalidAttempts(numOfLoginFailure);
			}

		}
		catch (Exception e) {
			log.error("Cannot update the user authentication data", e);
			userDetails.setErrorCode(SessionErrorCode.ERROR_GENERAL);
		}

		return userDetails;
	}

	public static void updateUserProfileForLogout(String userId) {
		try {
			// Now we will update authenticationData to db
			DbUser.INSTANCE.setUserAuthenticationState(userId,
			    UserProfile.AuthenticationState.EXPIRED);
		}
		catch (Exception e) {
			log.error("Cannot update the user authentication data", e);
		}
	}

	public static UserDetails updateUserProfileForSessionValidate(
	    UserDetails userDetails, SessionErrorCode errorCode) {
		try {
			UserProfile userProfile = userDetails.getUserPolicyProfile()
			    .getUserProfile();
			AuthenticationData authenData = userProfile.getAuthenticationData();

			// last activity
			authenData.setLastAuthenticationStateChange(Calendar.getInstance());

			if (errorCode.equals(SessionErrorCode.ERROR_SESSION_EXPIRED)) {
				authenData
				    .setAuthenticationState(UserProfile.AuthenticationState.EXPIRED);
			}
			else {
				authenData
				    .setAuthenticationState(UserProfile.AuthenticationState.VALID);
			}

			// Now we will update authenticationData to db
			DbUser.INSTANCE.setUserAuthenticationState(userProfile.getUserID(),
			    authenData.getAuthenticationState());

		}
		catch (Exception e) {
			log.error("Cannot update the user authentication data", e);
			userDetails.setErrorCode(SessionErrorCode.ERROR_GENERAL);
		}

		return userDetails;
	}

	public static SessionErrorCode validateClientIP(UserDetails userDetails) {

		SessionErrorCode result = SessionErrorCode.NO_ERROR;
		IPAddress clientAddress = userDetails.getLoginIPAddress();

		if (clientAddress == null) {
			return SessionErrorCode.ERROR_GENERAL;
		}

		UserProfile userProfile = userDetails.getUserPolicyProfile()
		    .getUserProfile();
		List<String> lockedIPList = LocalAccountAuthenticatorHelper
		    .getUserAccountPolicyDerivedFromLocalAndGlobal(
		        userProfile.getAuthenticationData().getUserAccountPolicy())
		    .getLockedClientIPs();

		if (lockedIPList == null) {
			return result;
		}

		if (lockedIPList.contains(clientAddress.getAddress())) {
			return SessionErrorCode.ERROR_LOCKED_CLIENT_IP_ADDRESSS;
		}

		return result;
	}

	public static SessionErrorCode validateInternalAccountAttribute(
	    UserProfile userProfile, UserDetails userDetails) {

		AbstractCredential cred = userDetails.getCredential();
		String userId = userProfile.getUserID();
		String sessionId = userDetails.getSessionID();
		String IP = cred.getIpAddress().getAddress();
		
		/***********************************************************/
		/* Now we validate user policy: account status */
		/***********************************************************/
		if (!userProfile.getAccountStatus().getAccountState()
		    .equals(UserProfile.AccountState.ENABLED)) {
			return SessionErrorCode.ERROR_ACCOUNT_DISABLED;
		}

		/***********************************************************/
		/* Now we validate user policy: dormant attribute */
		/***********************************************************/
		if (userProfile.getAuthenticationData().isDormant(
		    LocalAccountAuthenticatorHelper
		        .getUserAccountPolicyDerivedFromLocalAndGlobal(userProfile
		            .getAuthenticationData().getUserAccountPolicy()))) {
			return SessionErrorCode.ERROR_DORMANT;
		}

		/***********************************************************/
		/* Now we validate user policy: expired password attribute */
		/* TODO: Make sure fill-in internal account password info */
		/***********************************************************/
		if (userProfile.getAuthenticationData().isPasswordExpired(
		    LocalAccountAuthenticatorHelper
		        .getUserAccountPolicyDerivedFromLocalAndGlobal(userProfile
		            .getAuthenticationData().getUserAccountPolicy()))) {
			return SessionErrorCode.ERROR_PASSWORD_EXPIRED;

		}

		/***********************************************************/
		/* Now we validate user policy: lockout attribute */
		/***********************************************************/
		if (userProfile.getAuthenticationData().isInLockoutPeriod(
		    LocalAccountAuthenticatorHelper
		        .getUserAccountPolicyDerivedFromLocalAndGlobal(userProfile
		            .getAuthenticationData().getUserAccountPolicy()))) {
			return SessionErrorCode.ERROR_LOCKOUT;
		}
		LoginAttemptsPolicy attemptsPolicy = LoginAttemptsPolicy.INSTANCE;
		if(attemptsPolicy.isInLockoutPeriodSession(sessionId)){
			return SessionErrorCode.ERROR_LOCKED_CLIENT_SESSION;
		}		
		if(attemptsPolicy.isInLockoutPeriodIP(IP)){
			return SessionErrorCode.ERROR_LOCKED_CLIENT_IP_ADDRESSS;
		}
		return SessionErrorCode.NO_ERROR;
	}

}
