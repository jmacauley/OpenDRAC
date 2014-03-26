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

import org.opendrac.security.policy.LoginAttemptsPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.UserAccountPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPolicyProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationState;
import com.nortel.appcore.app.drac.database.helper.UserPolicyUtility;
import com.nortel.appcore.app.drac.security.ClientLoginType;

public final class LocalAccountAuthenticator {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * This method checks the user/password for authenticating, does not apply
	 * restrictions, etc.
	 * 
	 * @param loginType
	 * @param userDetails
	 * @throws Exception
	 */
	public UserDetails authenticate(ClientLoginType loginType,
	    UserDetails userDetails) throws Exception {
		
		AbstractCredential cred = userDetails.getCredential();
		InvalidAttemptCache attemptsCache = LocalAccountAuthenticatorHelper.getInvalidAttemptCache();
		String sessionId = userDetails.getSessionID();
		String IP = cred.getIpAddress().getAddress();	
		/*************************************/
		/* Validate the credential parameter */
		/*************************************/
		if (!cred.getUserID().equals(userDetails.getUserID())) {
			userDetails.setErrorCode(SessionErrorCode.ERROR_GENERAL);
			AuthenticationLogEntry.createLogEntry(userDetails,
			    LogKeyEnum.KEY_LOGIN_FAILED, new String[] { "Invalid userId" });
			log.error("---LocalAccountAuthenticator:authenticate Mismatch between userDetails and creds "
			    + cred.getUserID() + " vs " + userDetails.getUserID());
			return userDetails;
		}

		/*************************************/
		/* Query the userProfile INSTANCE */
		/*************************************/
		UserPolicyProfile userPolicyProfile = UserPolicyUtility
		    .INSTANCE.getUserPolicyProfile(cred.getUserID());
		if (userPolicyProfile == null || userPolicyProfile.getUserProfile() == null) {
			userDetails.setErrorCode(SessionErrorCode.ERROR_NO_POLICY);
			attemptsCache.incrementInvalidAttemptForIP(IP);
			attemptsCache.incrementInvalidAttemptForSessionId(sessionId);				
			AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);			
			AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
			return userDetails;
		}

		/************************************************/
		/* setting userPolicyProfile to security object */
		/************************************************/
		userDetails.setUserPolicyProfile(userPolicyProfile);
		UserProfile userProfile = userPolicyProfile.getUserProfile();

		/***********************************************************/
		/* Now we validate user policy: authentication type */
		/***********************************************************/
		if (!userProfile.getAuthenticationData().getAuthenticationType()
		    .equals(UserProfile.AuthenticationType.INTERNAL)) {
			userDetails
			    .setErrorCode(SessionErrorCode.ERROR_INVALID_AUTHENTICATION_TYPE);
			AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
			return userDetails;
		}

		/***********************************************************/
		/*
		 * Now we validate password. For a WEB service login validate against the
		 * WSDL password
		 */
		/***********************************************************/
		String psword = null;
		try {
			if (ClientLoginType.WEB_SERVICE_LOGIN.equals(loginType)) {
				log.debug("WEB_SERVICE_LOGIN using Wsdl Credentials");
				psword = userDetails.getUserPolicyProfile().getUserProfile()
				    .getAuthenticationData().getWSDLCredential();
			}
			else {
				psword = userProfile.getAuthenticationData().getInternalAccountData()
				    .getUserPassword();
			}
		}
		catch (Exception e) {
			log.error("Fatal error: cannot retrieve password", e);
			userDetails.setErrorCode(SessionErrorCode.ERROR_GENERAL);
		}

		log.debug("Validating user: " + userProfile.getUserID() + "/" + "******");

		if (psword == null || !cred.getPassword().equals(psword)) {
			if (userProfile.getAuthenticationData().isInLockoutPeriod(
			    LocalAccountAuthenticatorHelper
			        .getUserAccountPolicyDerivedFromLocalAndGlobal(userProfile
			            .getAuthenticationData().getUserAccountPolicy()))) {
				// If the user is already locked out, keep them lock out
				userDetails.setErrorCode(SessionErrorCode.ERROR_LOCKOUT);
			}
			else {
				String userId = userProfile.getUserID();

				/*************************************************************************/
				/* Get current number of invalid attempt from the cache, update it with */
				/* this login user, and update to the loginAuditData */
				/*************************************************************************/
			
				attemptsCache.incrementInvalidAttemptForUser(userId);
				attemptsCache.incrementInvalidAttemptForIP(IP);
				attemptsCache.incrementInvalidAttemptForSessionId(sessionId);
				
				cred.getLoginAuditData().setInvalidLoginAttempts(
				    LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
				        .getInvalidAttemptNumberForUser(userProfile.getUserID()));
				
				log.debug("Increment Attempt: "
				    + cred.getLoginAuditData().getLoginAttempts());

				if (userProfile
				    .getAuthenticationData()
				    .isExceedingMaxAttempts(
				        LocalAccountAuthenticatorHelper.getUserAccountPolicyDerivedFromLocalAndGlobal(userProfile
				            .getAuthenticationData().getUserAccountPolicy()),
				        cred.getLoginAuditData().getLoginAttempts())) {
					userDetails
					    .setErrorCode(SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_TO_BE_LOCKEDOUT);

					/*************************************************************************/
					/* reset number of invalid attempt from the cache, update it with */
					/* this login user, and update to the loginAuditData */
					/*************************************************************************/
					LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
					    .clearInvalidAttemptForUser(userProfile.getUserID());
					cred.getLoginAuditData().setInvalidLoginAttempts(
					    LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
					        .getInvalidAttemptNumberForUser(userProfile.getUserID()));
				}				
				else {				
					if(LoginAttemptsPolicy.INSTANCE.isInLockoutPeriodSession(sessionId)){
						userDetails.setErrorCode(SessionErrorCode.ERROR_LOCKED_CLIENT_SESSION);
					}
					else if(LoginAttemptsPolicy.INSTANCE.isInLockoutPeriodIP(IP)){
						userDetails.setErrorCode(SessionErrorCode.ERROR_LOCKED_CLIENT_IP_ADDRESSS);
					}
					else{
						userDetails.setErrorCode(SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_RETRY);
					}
				}				
			}
		}
		else {
			// password matches
			userDetails.setErrorCode(SessionErrorCode.NO_ERROR);
		}

		return userDetails;
	}

	/**
	 * Similar to login except does not apply authentication (i.e. password
	 * checking)
	 * 
	 * @param loginType
	 * @param userDetails
	 * @return
	 * @throws Exception
	 */
	public UserDetails authorize(ClientLoginType loginType,
	    UserDetails userDetails) throws Exception {
		
		AbstractCredential cred = userDetails.getCredential();
		InvalidAttemptCache attemptsCache = LocalAccountAuthenticatorHelper.getInvalidAttemptCache();
		String sessionId = userDetails.getSessionID();
		String IP = cred.getIpAddress().getAddress();	
		/*************************************/
		/* Validate the credential parameter */
		/*************************************/
		if (!cred.getUserID().equals(userDetails.getUserID())) {
			userDetails.setErrorCode(SessionErrorCode.ERROR_GENERAL);
			AuthenticationLogEntry.createLogEntry(userDetails,
			    LogKeyEnum.KEY_LOGIN_FAILED, new String[] { "Invalid userId" });
			log.debug("---LocalAccountAuthenticator:login Mismatch between userDetails and creds "
			    + cred.getUserID() + " vs " + userDetails.getUserID());
			return userDetails;
		}

		/*************************************/
		/* Query the userProfile INSTANCE */
		/*************************************/
		UserPolicyProfile userPolicyProfile = UserPolicyUtility
		    .INSTANCE.getUserPolicyProfile(cred.getUserID());
		if (userPolicyProfile == null || userPolicyProfile.getUserProfile() == null) {
			userDetails.setErrorCode(SessionErrorCode.ERROR_NO_POLICY);
			attemptsCache.incrementInvalidAttemptForIP(IP);
			attemptsCache.incrementInvalidAttemptForSessionId(sessionId);				
			AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);			
			AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
			return userDetails;
		}

		/************************************************/
		/* setting userPolicyProfile to security object */
		/************************************************/
		userDetails.setUserPolicyProfile(userPolicyProfile);
		UserProfile userProfile = userPolicyProfile.getUserProfile();

		/***********************************************************/
		/* Now we validate user policy: authentication type */
		/***********************************************************/
		if (!userProfile.getAuthenticationData().getAuthenticationType()
		    .equals(UserProfile.AuthenticationType.INTERNAL)) {
			userDetails
			    .setErrorCode(SessionErrorCode.ERROR_INVALID_AUTHENTICATION_TYPE);
			AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
			return userDetails;
		}

		/**************************************************/
		/* First check for locked ip */
		/**************************************************/
		userDetails.setErrorCode(LocalAccountAuthenticatorHelper
		    .validateClientIP(userDetails));
		if (!userDetails.getErrorCode().equals(SessionErrorCode.NO_ERROR)) {
			
			AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
			return userDetails;
		}

		log.debug("Validating user: " + userProfile.getUserID() + "/" + "******");

		/*********************************************************************/
		/* now setting the login time and error code */
		/*********************************************************************/
		cred.setLoginTime(System.currentTimeMillis());
		userDetails.setErrorCode(SessionErrorCode.NO_ERROR);

		/*************************************************************************/
		/* reset number of invalid attempt from the cache, update it with */
		/* this login user, and update to the loginAuditData */
		/*************************************************************************/
		LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
		    .clearInvalidAttemptForUser(userProfile.getUserID());
		cred.getLoginAuditData().setInvalidLoginAttempts(
		    LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
		        .getInvalidAttemptNumberForUser(userProfile.getUserID()));

		/**************************************************/
		/* Validate internal account attribute */
		/**************************************************/
		userDetails.setErrorCode(LocalAccountAuthenticatorHelper
		    .validateInternalAccountAttribute(userProfile, userDetails));

		/***************************************************/
		/* check if password expiration need to be notified */
		/***************************************************/
		LocalAccountAuthenticatorHelper
		    .updatePasswordExpireddNotification(userDetails);
		log.debug("Password to be expired in: "
		    + userDetails.getCredential().getLoginAuditData()
		        .getExpiringNotificationDays());

		// update security audit log based on SessionErrorCode
		AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
		return LocalAccountAuthenticatorHelper.updateUserProfileForLogin(
		    userDetails, userDetails.getErrorCode());
	}

	public UserDetails login(ClientLoginType loginType, UserDetails userDetails)
	    throws Exception {
		log.debug("---LocalAccountAuthenticator:login---");
		InvalidAttemptCache attemptsCache = LocalAccountAuthenticatorHelper.getInvalidAttemptCache();
		AbstractCredential cred = userDetails.getCredential();
		String sessionId = userDetails.getSessionID();
		String IP = cred.getIpAddress().getAddress();	
		/*************************************/
		/* Validate the credential parameter */
		/*************************************/
		boolean sessionLocked = LoginAttemptsPolicy.INSTANCE.isInLockoutPeriodSession(sessionId);
		boolean IPLocked = LoginAttemptsPolicy.INSTANCE.isInLockoutPeriodIP(IP);
		if(!( sessionLocked || IPLocked)){
			if (!cred.getUserID().equals(userDetails.getUserID())) {
				userDetails.setErrorCode(SessionErrorCode.ERROR_GENERAL);
				AuthenticationLogEntry.createLogEntry(userDetails,
				    LogKeyEnum.KEY_LOGIN_FAILED, new String[] { "Invalid userId" });
				log.error("---LocalAccountAuthenticator:login Mismatch between userDetails and creds "
				    + cred.getUserID() + " vs " + userDetails.getUserID());
				return userDetails;
			}
		}
		/*************************************/
		/* Query the userProfile INSTANCE */
		/*************************************/
		UserPolicyProfile userPolicyProfile = UserPolicyUtility
		    .INSTANCE.getUserPolicyProfile(cred.getUserID());
		if (userPolicyProfile == null || userPolicyProfile.getUserProfile() == null) {			
			if(sessionLocked){
				userDetails.setErrorCode(SessionErrorCode.ERROR_LOCKED_CLIENT_SESSION);
				AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);				
			}
			else if(IPLocked){
				userDetails.setErrorCode(SessionErrorCode.ERROR_LOCKED_CLIENT_IP_ADDRESSS);
				AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);				
			}
			else{
				userDetails.setErrorCode(SessionErrorCode.ERROR_NO_POLICY);
				attemptsCache.incrementInvalidAttemptForIP(IP);
				attemptsCache.incrementInvalidAttemptForSessionId(sessionId);				
				AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
			}
			return userDetails;
		}

		/************************************************/
		/* setting userPolicyProfile to security object */
		/************************************************/
		userDetails.setUserPolicyProfile(userPolicyProfile);
		UserProfile userProfile = userPolicyProfile.getUserProfile();

		/***********************************************************/
		/* Now we validate user policy: authentication type */
		/***********************************************************/
		if (!userProfile.getAuthenticationData().getAuthenticationType()
		    .equals(UserProfile.AuthenticationType.INTERNAL)) {
			userDetails
			    .setErrorCode(SessionErrorCode.ERROR_INVALID_AUTHENTICATION_TYPE);
			AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
			return userDetails;
		}

		/**************************************************/
		/* First check for locked ip */
		/**************************************************/
		userDetails.setErrorCode(LocalAccountAuthenticatorHelper
		    .validateClientIP(userDetails));
		if (!userDetails.getErrorCode().equals(SessionErrorCode.NO_ERROR)) {
			
			AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
			return userDetails;
		}

		/***********************************************************/
		/*
		 * Now we validate password. For a WEB service login validate against the
		 * WSDL password
		 */
		/***********************************************************/
		String psword = null;
		try {
			if (ClientLoginType.WEB_SERVICE_LOGIN.equals(loginType)) {
				log.debug("WEB_SERVICE_LOGIN using Wsdl Credential for login");
				psword = userDetails.getUserPolicyProfile().getUserProfile()
				    .getAuthenticationData().getWSDLCredential();
			}
			else {
				psword = userProfile.getAuthenticationData().getInternalAccountData()
				    .getUserPassword();
			}
		}
		catch (Exception e) {
			log.error("Fatal error: cannot retrieve password", e);
			userDetails.setErrorCode(SessionErrorCode.ERROR_GENERAL);
		}

		log.debug("Validating user: " + userProfile.getUserID() + "/" + "******");

		if (psword == null || !cred.getPassword().equals(psword)) {
			if (userProfile.getAuthenticationData().isInLockoutPeriod(
			    LocalAccountAuthenticatorHelper
			        .getUserAccountPolicyDerivedFromLocalAndGlobal(userProfile
			            .getAuthenticationData().getUserAccountPolicy()))) {
				// If the user is already locked out, keep them lock out
				userDetails.setErrorCode(SessionErrorCode.ERROR_LOCKOUT);
			}
			else {
				String userId = userProfile.getUserID();
			
				/*************************************************************************/
				/* Get current number of invalid attempt from the cache, update it with */
				/* this login user, and update to the loginAuditData */
				/*************************************************************************/
				
				attemptsCache.incrementInvalidAttemptForUser(userId);
				attemptsCache.incrementInvalidAttemptForIP(IP);
				attemptsCache.incrementInvalidAttemptForSessionId(sessionId);	
				
				cred.getLoginAuditData().setInvalidLoginAttempts(attemptsCache.
						getInvalidAttemptNumberForUser(userProfile.getUserID()));
				log.debug("Increment Attempt: "
				    + cred.getLoginAuditData().getLoginAttempts());

				if (userProfile
				    .getAuthenticationData()
				    .isExceedingMaxAttempts(
				        LocalAccountAuthenticatorHelper.getUserAccountPolicyDerivedFromLocalAndGlobal(userProfile
				            .getAuthenticationData().getUserAccountPolicy()),
				        cred.getLoginAuditData().getLoginAttempts())) {
					userDetails
					    .setErrorCode(SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_TO_BE_LOCKEDOUT);

					/*************************************************************************/
					/* reset number of invalid attempt from the cache, update it with */
					/* this login user, and update to the loginAuditData */
					/*************************************************************************/
					LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
					    .clearInvalidAttemptForUser(userProfile.getUserID());
					cred.getLoginAuditData().setInvalidLoginAttempts(
					    LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
					        .getInvalidAttemptNumberForUser(userProfile.getUserID()));
				}
				else {
					if(LoginAttemptsPolicy.INSTANCE.isInLockoutPeriodSession(sessionId)){
						userDetails.setErrorCode(SessionErrorCode.ERROR_LOCKED_CLIENT_SESSION);
					}
					else if(LoginAttemptsPolicy.INSTANCE.isInLockoutPeriodIP(IP)){
						userDetails.setErrorCode(SessionErrorCode.ERROR_LOCKED_CLIENT_IP_ADDRESSS);
					}
					else{
						userDetails.setErrorCode(SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_RETRY);
					}
				}	
			}
		}
		else {

			/*********************************************************************/
			/* Password is correct, now setting the loging time and error code */
			/*********************************************************************/
			cred.setLoginTime(System.currentTimeMillis());
			userDetails.setErrorCode(SessionErrorCode.NO_ERROR);

			/*************************************************************************/
			/* reset number of invalid attempt from the cache, update it with */
			/* this login user, and update to the loginAuditData */
			/*************************************************************************/
			LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
			    .clearInvalidAttemptForUser(userProfile.getUserID());
			cred.getLoginAuditData().setInvalidLoginAttempts(
			    LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
			        .getInvalidAttemptNumberForUser(userProfile.getUserID()));

			/**************************************************/
			/* Validate internal account attribute */
			/**************************************************/
			userDetails.setErrorCode(LocalAccountAuthenticatorHelper
			    .validateInternalAccountAttribute(userProfile, userDetails));

			/***************************************************/
			/* check if password expiration need to be notified */
			/***************************************************/
			LocalAccountAuthenticatorHelper
			    .updatePasswordExpireddNotification(userDetails);
			log.debug("Password to be expired in: "
			    + userDetails.getCredential().getLoginAuditData()
			        .getExpiringNotificationDays());
		}

		// update security audit log based on SessionErrorCode
		AuthenticationLogEntry.createLogEntryForAuthenticate(userDetails);
		return LocalAccountAuthenticatorHelper.updateUserProfileForLogin(
		    userDetails, userDetails.getErrorCode());
	}

	public boolean logout(UserDetails userDetails) throws Exception {
		/*
		 * we invalidate the userDetails, we logout by setting the authentication
		 * state accordingly.
		 */
		LocalAccountAuthenticatorHelper.updateUserProfileForLogout(userDetails
		    .getUserID());
		// update security audit log
		return true;
	}

	public UserDetails sessionValidate(UserDetails userDetails) throws Exception {

		if (!LocalAccountAuthenticatorHelper.getInvalidAttemptCache()
		    .hasUserExisted(userDetails.getUserID())) {
			log.debug("This user did not login yet");
			userDetails.setErrorCode(SessionErrorCode.ERROR_SESSION_EXPIRED);
			return userDetails;
		}
		UserProfile userProfile = userDetails.getUserPolicyProfile()
		    .getUserProfile();
		UserAccountPolicy accountPolicy = LocalAccountAuthenticatorHelper
		    .getUserAccountPolicyDerivedFromLocalAndGlobal(userProfile
		        .getAuthenticationData().getUserAccountPolicy());

		if (!userProfile.getAuthenticationData().getAuthenticationState()
		    .equals(AuthenticationState.VALID)) {
      if (userProfile.getAuthenticationData().getUserAccountPolicy().getLocalPasswordPolicy().getPwAging() == -1) {
        log.debug("Password aging for user {} is set to {}", userProfile.getUserID(), -1);
      }
      else {
        log.debug("This user did not login yet");
        return userDetails;
      }
		}

		long currentTime = System.currentTimeMillis();
		

		/************************************************************/
		/* If accountPolilcy is not exist nor InactivityPeriod, */
		/* or 0, then passed. */
		/************************************************************/
		if (accountPolicy == null || accountPolicy.getInactivityPeriod() == null
		    || accountPolicy.getInactivityPeriod().intValue() == 0) {
			
			userDetails.getCredential().setLoginTime(currentTime);
			userDetails.setErrorCode(SessionErrorCode.NO_ERROR);
		}
		else if (currentTime >= userDetails.getCredential().getLoginTime()
		    + accountPolicy.getInactivityPeriod().intValue() * 1000) {
			// has it expired yet? if so, kick them out
			log.debug("Session expired: Current time: " + currentTime
			    + " Last activity time: "
			    + userDetails.getCredential().getLoginTime()
			    + " Expired with inactivity in sec: "
			    + accountPolicy.getInactivityPeriod().intValue());
			userDetails.setErrorCode(SessionErrorCode.ERROR_SESSION_EXPIRED);

			// update security audit log
			AuthenticationLogEntry.createLogEntry(userDetails,
			    LogKeyEnum.KEY_LOGOUT_TIMEOUT);
		}
		else {
			userDetails.getCredential().setLoginTime(currentTime);
			userDetails.setErrorCode(SessionErrorCode.NO_ERROR);
		}

		// AuditLogger.INSTANCE.log(AuthenticationLogEntry.createLogEntry(userDetails,
		// AuditType.SESSION_VALIDATED, null));
		return LocalAccountAuthenticatorHelper.updateUserProfileForSessionValidate(
		    userDetails, userDetails.getErrorCode());
	}
}
