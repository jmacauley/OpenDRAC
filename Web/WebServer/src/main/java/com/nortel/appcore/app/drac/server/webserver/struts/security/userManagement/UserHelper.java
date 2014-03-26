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

package com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.LocalAccountCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalAuthentication;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalAuthentication.SupportedAuthenticationType;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.security.policy.types.InternalAccountData;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.OrganizationData;
import com.nortel.appcore.app.drac.common.security.policy.types.PasswordPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.PersonalData;
import com.nortel.appcore.app.drac.common.security.policy.types.UserAccountPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPreferences;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationType;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.CreateUserForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.EditUserForm;
import com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.UserForm;

/**
 * @author Colin Hart
 */
public final class UserHelper {
  
  private static final Logger log = LoggerFactory.getLogger(UserHelper.class);

	public static synchronized void copyPersonalDataProperties(UserProfile src,
	    UserForm dest) throws Exception {
		if (src != null && dest != null) {
			PersonalData personalData = src.getPersonalData();

			dest.setUserID(src.getUserID());
			dest.setWebSafeName(src.getUserID());

			if (personalData != null) {
				dest.setCommonName(personalData.getName().getCommonName());
				dest.setGivenName(personalData.getName().getGivenName());
				dest.setSurname(personalData.getName().getSurName());
				dest.setPhone(personalData.getAddress().getPhone());
				dest.setEmail(personalData.getAddress().getMail());
				// The following are not needed right now when listing users,
				// but copy them in case others call this method.
				dest.setPostalAddress(personalData.getAddress().getPostalAddress());
				dest.setDescription(personalData.getDescription());
				dest.setTitle(personalData.getTitle());
			}
		}
	}

	public static String create(LoginToken token, CreateUserForm form)
	    throws Exception {

		RequestHandler rh = RequestHandler.INSTANCE;

		// Variables taking values from the Create page.
		String userID = "";
		String userPassword = "";
		String authType = "";
		int accountState = 0;
		String disabledReason = "";
		String wsdlCertificate = "";

		userID = form.getUserID().trim();
		userPassword = form.getUserPassword();
		authType = form.getAuthType();
		accountState = form.getAccountState();
		disabledReason = form.getDisabledReason();
		wsdlCertificate = form.getwsdlCertificate();

		log.debug("UserHelper--Retrieved the values from Create form");

		UserProfile.AccountState accState = UserProfile.AccountState.ENABLED;
		try {
			accState = UserProfile.AccountState.values()[accountState];
		}
		catch (ArrayIndexOutOfBoundsException aoobe) {
			log.warn("invalid account state", aoobe);
		}

		AccountStatus myAccountStatus = new AccountStatus(accState, disabledReason);

		AbstractCredential myCredential = null;

		if (authType != null) {
			if (authType.equals(AuthenticationType.INTERNAL.toString())) {
				myCredential = new LocalAccountCredential(userID, userPassword,
				    new IPAddress(null, null));
			}
			else if (authType.equals(AuthenticationType.RADIUS.toString())) {
				// Do nothing.
			}
		}

		// Calling CreateUser function.
		log.debug("Calling CreateUserProfile");
		rh.createUserProfile(token, userID, myCredential, myAccountStatus);
		log.debug("Generated User Id:" + userID);

		// Populate other user profile attributes if available
		// Personal Data
		PersonalData myPersonalData = new PersonalData();
		myPersonalData.setName(new PersonalData.UserName(form.getCommonName(), form
		    .getGivenName(), form.getSurname()));
		myPersonalData.setAddress(new PersonalData.Address(form.getPhone(), form
		    .getEmail(), form.getPostalAddress()));
		myPersonalData.setDescription(form.getDescription());
		myPersonalData.setTitle(form.getTitle());
		rh.setUserPersonalData(token, userID, myPersonalData);

		// Organization Data
		OrganizationData myOrganizationData = new OrganizationData();
		myOrganizationData.setOrgName(form.getOrgName());
		myOrganizationData.setOrgUnitName(form.getOrgUnitName());
		myOrganizationData.setCategory(form.getCategory());
		myOrganizationData.setOwner(form.getOwner());
		myOrganizationData.setDescription(form.getOrgDescription());
		myOrganizationData.setSeeAlso(form.getSeeAlso());
		rh.setUserOrganization(token, userID, myOrganizationData);

		// Membership Data
		UserProfile userProfile = rh.getUserProfile(token, userID);
		MembershipData membershipData = userProfile.getMembershipData();
		String[] memberGroups = form.getMemberUserGroups();
		TreeSet<UserGroupName> memberGroupList = new TreeSet<UserGroupName>();
		for (String memberGroup : memberGroups) {
			memberGroupList.add(new UserGroupName(memberGroup));
		}
		membershipData.setMemberUserGroupName(memberGroupList);
		rh.setUserMembership(token, userID, membershipData);

		// Account Policy Data
		AuthenticationData authData = userProfile.getAuthenticationData();
		authData.setWSDLCredential(wsdlCertificate);
		copyAuthenticationDataToProfile(form, authData);
		rh.setUserAuthenticationData(token, userID, authData);
		// instead of calling setUserAuthenticationData directly which is not
		// allowed,
		// Colin can do this instead
		// for setting wsdl cer
		// rh.setUserWSDLCertificate();
		// rh.setUserAccountPolicy(); // get the UserAccountPolicy from the
		// AuthenticationData

		if (form.getTimeZone() != null) {
			rh.setUserTimeZoneIDPreference(token, userID, form.getTimeZone());
		}

		return userID;
	}

	/**
	 * Copy properties from user profile to edit form
	 * 
	 * @param src
	 * @param dest
	 * @param globalAuth
	 */
	public static void populateEditUserForm(Locale locale, TimeZone tz,
	    GlobalPolicy aGP, UserProfile src, EditUserForm dest) {
		if (src != null && dest != null) {
			dest.setUserID(src.getUserID());

			String dateString = "";
			String timeString = "";

			dateString = DateFormatter.guiDateToString(src.getCreationDate(), locale,
			    tz);
			dest.setCreationDate(dateString);
			timeString = DateFormatter.guiTimeToString(src.getCreationDate(), locale,
			    tz);
			dest.setCreationTime(timeString);
			dateString = DateFormatter.guiDateToString(src.getLastModifiedDate(),
			    locale, tz);
			dest.setLastModifiedDate(dateString);
			timeString = DateFormatter.guiTimeToString(src.getLastModifiedDate(),
			    locale, tz);
			dest.setLastModifiedTime(timeString);

			AccountStatus myAccountStatus = src.getAccountStatus();
			if (myAccountStatus != null) {
				dest.setAccountState(myAccountStatus.getAccountState().toString());
				dest.setDisabledReason(myAccountStatus.getDisabledReason());
			}

			AuthenticationData authData = src.getAuthenticationData();
			if (authData != null) {
				dest.setAuthType(authData.getAuthenticationType().toString());
				dest.setwsdlCertificate("");

				if (authData.getAuthenticationType().toString()
				    .equals(AuthenticationType.INTERNAL.toString())) {
					InternalAccountData internalAccountData = authData
					    .getInternalAccountData();
					if (internalAccountData != null) {
						dest.setPassword("");
						dest.setPassword2("");
						// Expiration date may not be set if user was created as A-Select.
						if (internalAccountData.getExpirationDate() != null) {
							dest.setExpirationDate(DateFormatter.guiDateToString(
							    internalAccountData.getExpirationDate(), locale, tz));
						}
					}
				}

				UserAccountPolicy userAccountPolicy = authData.getUserAccountPolicy();
				if (userAccountPolicy != null) {
					PasswordPolicy myPasswordPolicy = userAccountPolicy
					    .getLocalPasswordPolicy();
					if (myPasswordPolicy != null) {
						if (myPasswordPolicy.getPwHistorySize() != null) {
							dest.setPasswordHistorySize(myPasswordPolicy.getPwHistorySize()
							    .toString());
						}
						else {
							if (aGP.getLocalAccountPolicy().getLocalPasswordPolicy()
							    .getPwHistorySize() != null) {
								dest.setPasswordHistorySizeGP(aGP.getLocalAccountPolicy()
								    .getLocalPasswordPolicy().getPwHistorySize().toString());
							}
							else {
								dest.setPasswordHistorySizeGP(null);
							}
						}
						if (myPasswordPolicy.getPwExpiredNotif() != null) {
							dest.setPasswordExpirationNotification(myPasswordPolicy
							    .getPwExpiredNotif().toString());
						}
						else {
							if (aGP.getLocalAccountPolicy().getLocalPasswordPolicy()
							    .getPwExpiredNotif() != null) {
								dest.setPasswordExpirationNotificationGP(aGP
								    .getLocalAccountPolicy().getLocalPasswordPolicy()
								    .getPwExpiredNotif().toString());
							}
							else {
								dest.setPasswordExpirationNotificationGP(null);
							}
						}
						if (myPasswordPolicy.getPwAging() != null) {
							dest.setPasswordAging(myPasswordPolicy.getPwAging().toString());
						}
						else {
							dest.setPasswordAgingGP(aGP.getLocalAccountPolicy()
							    .getLocalPasswordPolicy().getPwAging().toString());
						}
					}
					if (userAccountPolicy.getInactivityPeriod() != null) {
						dest.setInactivityPeriod(userAccountPolicy.getInactivityPeriod()
						    .toString());
					}
					else {
						if (aGP.getLocalAccountPolicy().getInactivityPeriod() != null) {
							dest.setInactivityPeriodGP(aGP.getLocalAccountPolicy()
							    .getInactivityPeriod().toString());
						}
						else {
							dest.setInactivityPeriodGP(null);
						}
					}
					dest.setInactivityMetric(DracConstants.TimeMetric.SEC.ordinal());

					if (userAccountPolicy.getMaxInvalidLoginAttempts() != null) {
						dest.setMaxInvalidLoginAttempts(userAccountPolicy
						    .getMaxInvalidLoginAttempts().toString());
					}
					else {
						if (aGP.getLocalAccountPolicy().getMaxInvalidLoginAttempts() != null) {
							dest.setMaxInvalidLoginAttemptsGP(aGP.getLocalAccountPolicy()
							    .getMaxInvalidLoginAttempts().toString());
						}
						else {
							dest.setMaxInvalidLoginAttemptsGP(null);
						}
					}

					if (userAccountPolicy.getDormantPeriod() != null) {
						dest.setDormantPeriod(userAccountPolicy.getDormantPeriod()
						    .toString());
					}
					else {
						if (aGP.getLocalAccountPolicy().getDormantPeriod() != null) {
							dest.setDormantPeriodGP(aGP.getLocalAccountPolicy()
							    .getDormantPeriod().toString());
						}
						else {
							dest.setDormantPeriodGP(null);
						}
					}
					if (userAccountPolicy.getLockoutPeriod() != null) {
						dest.setLockoutPeriod(userAccountPolicy.getLockoutPeriod()
						    .toString());
					}
					else {
						if (aGP.getLocalAccountPolicy().getLockoutPeriod() != null) {
							dest.setLockoutPeriodGP(aGP.getLocalAccountPolicy()
							    .getLockoutPeriod().toString());
						}
						else {
							dest.setLockoutPeriodGP(null);
						}
					}
					dest.setLockoutMetric(DracConstants.TimeMetric.SEC.ordinal());

					if (userAccountPolicy.getLockedClientIPs() != null
					    && !userAccountPolicy.getLockedClientIPs().isEmpty()) {
						dest.setLockedClientIPs(userAccountPolicy.getLockedClientIPs()
						    .toArray(
						        new String[userAccountPolicy.getLockedClientIPs().size()]));
					}
					if (aGP.getLocalAccountPolicy().getLockedClientIPs() != null
					    && !aGP.getLocalAccountPolicy().getLockedClientIPs().isEmpty()) {
						dest.setLockedClientIPsGP(aGP
						    .getLocalAccountPolicy()
						    .getLockedClientIPs()
						    .toArray(
						        new String[aGP.getLocalAccountPolicy().getLockedClientIPs()
						            .size()]));
					}
				}
			}

			GlobalAuthentication globalAuth = aGP.getSupportedAuthenticationData();
			if (globalAuth != null) {
				List<SupportedAuthenticationType> authTypes = globalAuth
				    .getOnlySupportedAuthenticationTypes();
				if (authTypes != null && !authTypes.isEmpty()) {
					ArrayList list = new ArrayList(authTypes.size());
					for (SupportedAuthenticationType authType : authTypes) {
						list.add(authType.getType().name());
					}
					dest.setAvailAuthenticationTypes(list);
				}
			}

			PersonalData personalData = src.getPersonalData();
			if (personalData != null) {
				dest.setCommonName(personalData.getName().getCommonName());
				dest.setGivenName(personalData.getName().getGivenName());
				dest.setSurname(personalData.getName().getSurName());
				dest.setPhone(personalData.getAddress().getPhone());
				dest.setEmail(personalData.getAddress().getMail());
				dest.setPostalAddress(personalData.getAddress().getPostalAddress());
				dest.setDescription(personalData.getDescription());
				dest.setTitle(personalData.getTitle());
			}

			OrganizationData orgData = src.getOrganizationData();
			if (orgData != null) {
				dest.setOrgName(orgData.getOrgName());
				dest.setOrgUnitName(orgData.getOrgUnitName());
				dest.setOwner(orgData.getOwner());
				dest.setOrgDescription(orgData.getDescription());
				dest.setSeeAlso(orgData.getSeeAlso());
				dest.setCategory(orgData.getCategory());
			}

			// Preferences Data
			UserPreferences prefs = src.getPreferences();
			if (prefs != null) {
				dest.setTimeZone(prefs.getTimeZoneId());
			}

			// Membership Data
			MembershipData myMembershipData = src.getMembershipData();
			if (myMembershipData != null) {

				ArrayList<String> memberUserGroupNames = new ArrayList<String>();
				Set<UserGroupName> memberUGNList = myMembershipData
				    .getMemberUserGroupName();
				for (UserGroupName memberUserGroupName : memberUGNList) {
					memberUserGroupNames.add(memberUserGroupName.toString());
				}

				dest.setMemberUserGroups(memberUserGroupNames
				    .toArray(new String[memberUserGroupNames.size()]));
			}
		}
	}

	public static synchronized void populateUserForm(LoginToken token,
	    Locale locale, TimeZone tz, UserProfile src, GlobalPolicy aGP,
	    UserForm dest) throws Exception {
		if (src != null && dest != null) {

			String dateString = "";
			String timeString = "";

			dateString = DateFormatter.guiDateToString(src.getCreationDate(), locale,
			    tz);
			dest.setCreationDate(dateString);
			timeString = DateFormatter.guiTimeToString(src.getCreationDate(), locale,
			    tz);
			dest.setCreationTime(timeString);
			dateString = DateFormatter.guiDateToString(src.getLastModifiedDate(),
			    locale, tz);
			dest.setLastModifiedDate(dateString);
			timeString = DateFormatter.guiTimeToString(src.getLastModifiedDate(),
			    locale, tz);
			dest.setLastModifiedTime(timeString);

			copyPersonalDataProperties(src, dest);

			OrganizationData myOrganizationData = src.getOrganizationData();
			if (myOrganizationData != null) {
				dest.setOrgName(myOrganizationData.getOrgName());
				dest.setOrgUnitName(myOrganizationData.getOrgUnitName());
				dest.setOwner(myOrganizationData.getOwner());
				dest.setOrgDescription(myOrganizationData.getDescription());
				dest.setSeeAlso(myOrganizationData.getSeeAlso());
				dest.setCategory(myOrganizationData.getCategory());
			}

			AccountStatus myAccountStatus = src.getAccountStatus();
			if (myAccountStatus != null) {
				dest.setAccountState(myAccountStatus.getAccountState().toString());
				dest.setDisabledReason(myAccountStatus.getDisabledReason());
			}

			AuthenticationData authData = src.getAuthenticationData();
			if (authData != null) {
				dest.setAuthenticationType(authData.getAuthenticationType().toString());

				if (authData.getAuthenticationType().toString()
				    .equals(AuthenticationType.INTERNAL.toString())) {
					InternalAccountData internalAccountData = authData
					    .getInternalAccountData();
					if (internalAccountData != null) {
						if (internalAccountData.getExpirationDate() != null) {
							dest.setExpirationDate(DateFormatter.guiDateToString(
							    internalAccountData.getExpirationDate(), locale, tz));
						}
					}
				}

				UserAccountPolicy userAccountPolicy = authData.getUserAccountPolicy();
				if (userAccountPolicy != null) {
					PasswordPolicy myPasswordPolicy = userAccountPolicy
					    .getLocalPasswordPolicy();
					if (myPasswordPolicy != null) {
						if (myPasswordPolicy.getPwHistorySize() != null) {
							dest.setPasswordHistorySize(myPasswordPolicy.getPwHistorySize()
							    .toString());
						}
						else {
							if (aGP.getLocalAccountPolicy().getLocalPasswordPolicy()
							    .getPwHistorySize() != null) {
								dest.setPasswordHistorySizeGP(aGP.getLocalAccountPolicy()
								    .getLocalPasswordPolicy().getPwHistorySize().toString());
							}
							else {
								dest.setPasswordHistorySizeGP(null);
							}
						}
						if (myPasswordPolicy.getPwExpiredNotif() != null) {
							dest.setPasswordExpirationNotification(myPasswordPolicy
							    .getPwExpiredNotif().toString());
						}
						else {
							if (aGP.getLocalAccountPolicy().getLocalPasswordPolicy()
							    .getPwExpiredNotif() != null) {
								dest.setPasswordExpirationNotificationGP(aGP
								    .getLocalAccountPolicy().getLocalPasswordPolicy()
								    .getPwExpiredNotif().toString());
							}
							else {
								dest.setPasswordExpirationNotificationGP(null);
							}
						}
						if (myPasswordPolicy.getPwAging() != null) {
							dest.setPasswordAging(myPasswordPolicy.getPwAging().toString());
						}
						else {
							dest.setPasswordAgingGP(aGP.getLocalAccountPolicy()
							    .getLocalPasswordPolicy().getPwAging().toString());
						}
					}

					if (userAccountPolicy.getInactivityPeriod() != null) {
						dest.setInactivityPeriod(userAccountPolicy.getInactivityPeriod()
						    .toString());
					}
					else {
						if (aGP.getLocalAccountPolicy().getInactivityPeriod() != null) {
							dest.setInactivityPeriodGP(aGP.getLocalAccountPolicy()
							    .getInactivityPeriod().toString());
						}
						else {
							dest.setInactivityPeriodGP(null);
						}
					}
					dest.setInactivityMetric(DracConstants.TimeMetric.SEC.ordinal());

					if (userAccountPolicy.getMaxInvalidLoginAttempts() != null) {
						dest.setMaxInvalidLoginAttempts(userAccountPolicy
						    .getMaxInvalidLoginAttempts().toString());
					}
					else {
						if (aGP.getLocalAccountPolicy().getMaxInvalidLoginAttempts() != null) {
							dest.setMaxInvalidLoginAttemptsGP(aGP.getLocalAccountPolicy()
							    .getMaxInvalidLoginAttempts().toString());
						}
						else {
							dest.setMaxInvalidLoginAttemptsGP(null);
						}
					}

					if (userAccountPolicy.getDormantPeriod() != null) {
						dest.setDormantPeriod(userAccountPolicy.getDormantPeriod()
						    .toString());
					}
					else {
						if (aGP.getLocalAccountPolicy().getDormantPeriod() != null) {
							dest.setDormantPeriodGP(aGP.getLocalAccountPolicy()
							    .getDormantPeriod().toString());
						}
						else {
							dest.setDormantPeriodGP(null);
						}
					}

					if (userAccountPolicy.getLockoutPeriod() != null) {
						dest.setLockoutPeriod(userAccountPolicy.getLockoutPeriod()
						    .toString());
					}
					else {
						if (aGP.getLocalAccountPolicy().getLockoutPeriod() != null) {
							dest.setLockoutPeriodGP(aGP.getLocalAccountPolicy()
							    .getLockoutPeriod().toString());
						}
						else {
							dest.setLockoutPeriodGP(null);
						}
					}
					dest.setLockoutMetric(DracConstants.TimeMetric.SEC.ordinal());

					if (userAccountPolicy.getLockedClientIPs() != null
					    && !userAccountPolicy.getLockedClientIPs().isEmpty()) {
						ArrayList<String> lockedClientIPs = new ArrayList<String>();
						List<String> lockedClientIPsList = userAccountPolicy
						    .getLockedClientIPs();
						for (String lockedClientIP : lockedClientIPsList) {
							lockedClientIPs.add(lockedClientIP);
						}
						dest.setLockedClientIPs(lockedClientIPs);
					}
					if (aGP.getLocalAccountPolicy().getLockedClientIPs() != null
					    && !aGP.getLocalAccountPolicy().getLockedClientIPs().isEmpty()) {
						ArrayList<String> lockedClientIPsGP = new ArrayList<String>();
						List<String> lockedClientIPsGPList = aGP.getLocalAccountPolicy()
						    .getLockedClientIPs();
						for (String lockedClientIPGP : lockedClientIPsGPList) {
							lockedClientIPsGP.add(lockedClientIPGP);
						}
						dest.setLockedClientIPsGP(lockedClientIPsGP);
					}
				}
			}

			// Preferences Data
			UserPreferences myUserPreferences = src.getPreferences();
			if (myUserPreferences != null) {
				dest.setTimeZone(myUserPreferences.getTimeZoneId());
			}

			// Membership Data
			updateMembership(token, src, dest, false);
		}
	}

	public static synchronized void updateMembership(LoginToken token,
	    UserProfile src, UserForm dest, boolean updateMembershipTree)
	    throws Exception {
		if (src != null && dest != null) {
			// Membership Data
			MembershipData myMembershipData = src.getMembershipData();

			if (myMembershipData != null) {
				ArrayList<String> memberUserIDs = new ArrayList<String>();
				Set<String> memberUserIDList = myMembershipData.getMemberUserID();
				for (String memberUserID : memberUserIDList) {
					memberUserIDs.add(memberUserID);
				}

				ArrayList<String> memberUserGroupNames = new ArrayList<String>();
				Set<UserGroupName> memberUGNList = myMembershipData
				    .getMemberUserGroupName();
				for (UserGroupName memberUserGroupName : memberUGNList) {
					memberUserGroupNames.add(memberUserGroupName.toString());
				}

				ArrayList<String> memberResourceGroupNames = new ArrayList<String>();
				Set<String> memberRGNList = myMembershipData
				    .getMemberResourceGroupName();
				for (String memberResourceGroupName : memberRGNList) {
					memberResourceGroupNames.add(memberResourceGroupName);
				}

				dest.setMemberUserID(memberUserIDs);
				dest.setMemberUserGroupName(memberUserGroupNames);
				dest.setMemberResourceGroupName(memberResourceGroupNames);
				if (updateMembershipTree) {
					dest.setMemberUserGroupNameTree(RequestHandler.INSTANCE
					    .getUserNameLineage(token, src));
				}

			}
		}
	}

	/**
	 * Copy properties from edit form to user profile
	 * 
	 * @param userDetails
	 * @param EditUserForm
	 *          src
	 * @param UserProfile
	 *          dest
	 * @throws Exception
	 */
	public static void updateUserProfile(LoginToken token,
	    UserDetails userDetails, EditUserForm src, UserProfile dest)
	    throws Exception {
		if (src != null && dest != null) {
			RequestHandler rh = RequestHandler.INSTANCE;

			AccountStatus myAccountStatus = dest.getAccountStatus();
			if (myAccountStatus != null) {
				copyAccountStatusToProfile(src, myAccountStatus);
				rh.setUserAccountStatus(token, dest.getUserID(), myAccountStatus);
			}

			PersonalData myPersonalData = dest.getPersonalData();
			if (myPersonalData != null) {
				myPersonalData.setName(new PersonalData.UserName(src.getCommonName(),
				    src.getGivenName(), src.getSurname()));
				myPersonalData.setAddress(new PersonalData.Address(src.getPhone(), src
				    .getEmail(), src.getPostalAddress()));
				myPersonalData.setDescription(src.getDescription());
				myPersonalData.setTitle(src.getTitle());
				rh.setUserPersonalData(token, dest.getUserID(), myPersonalData);
			}

			OrganizationData myOrganizationData = dest.getOrganizationData();
			if (myOrganizationData != null) {
				myOrganizationData.setOrgName(src.getOrgName());
				myOrganizationData.setOrgUnitName(src.getOrgUnitName());
				myOrganizationData.setCategory(src.getCategory());
				myOrganizationData.setOwner(src.getOwner());
				myOrganizationData.setDescription(src.getOrgDescription());
				myOrganizationData.setSeeAlso(src.getSeeAlso());
				rh.setUserOrganization(token, dest.getUserID(), myOrganizationData);
			}

			if (dest.getPreferences() != null) {
				String dbTimeZone = dest.getPreferences().getTimeZoneId();
				if (src.getTimeZone() != null && dbTimeZone != null
				    && !dbTimeZone.equals(src.getTimeZone())) {
					rh.setUserTimeZoneIDPreference(token, dest.getUserID(),
					    src.getTimeZone());
				}
			}

			AuthenticationData authData = dest.getAuthenticationData();
			if (authData != null) {
				if (!src.getwsdlCertificate().equals("")) {
					authData.setWSDLCredential(src.getwsdlCertificate());
				}
				// If we are a regular user we are not allowed to edit Auth Data.
				if (!userDetails.getUserPolicyProfile().getUserGroupType()
				    .equals(UserGroupProfileXML.UserGroupType.USER)) {
					copyEditFormAuthDataToProfile(src, authData);
				}
				// TODO: Please use the direct wdsl setting API. This is errornuous.
				rh.setUserAuthenticationData(token, dest.getUserID(), authData);

				/*
				 * check if user groups changed If we are a regular user we are not
				 * allowed to edit membership.
				 */
				if (!userDetails.getUserPolicyProfile().getUserGroupType()
				    .equals(UserGroupProfileXML.UserGroupType.USER)) {
					String[] memberGroups = src.getMemberUserGroups();
					MembershipData membershipData = dest.getMembershipData();
					TreeSet<UserGroupName> memberGroupList = new TreeSet<UserGroupName>();
					for (String memberGroup : memberGroups) {
						memberGroupList.add(new UserGroupName(memberGroup));
					}

					if (membershipData != null) {
						Set<UserGroupName> oldGroupList = membershipData
						    .getMemberUserGroupName();

						/*
						 * old group is the same if it contains the same groups as the
						 * form's list, as well, the form's list contains all the same
						 * groups as the old group list
						 */
						if (!memberGroupList.containsAll(oldGroupList)
						    || !oldGroupList.containsAll(memberGroupList)) {
							membershipData.setMemberUserGroupName(memberGroupList);
							rh.setUserMembership(token, dest.getUserID(), membershipData);
						}
					}
				}
			}
		}
	}

	/**
	 * copies the account status data to the user profile for editing
	 * 
	 * @param src
	 * @param dest
	 */
	private static void copyAccountStatusToProfile(EditUserForm src,
	    AccountStatus dest) {
		String userAuthType = src.getAuthType();
		if (userAuthType.equals(AuthenticationType.INTERNAL.toString())) {
			String accountState = src.getAccountState();
			String disabledReason = src.getDisabledReason();

			if (accountState != null
			    && !accountState.equals(dest.getAccountState().toString())) {
				if (accountState.equals(UserProfile.AccountState.ENABLED.toString())) {
					dest.setAccountState(UserProfile.AccountState.ENABLED);
				}
				else if (accountState.equals(UserProfile.AccountState.DISABLED
				    .toString())) {
					dest.setAccountState(UserProfile.AccountState.DISABLED);
				}
			}
			if (disabledReason != null
			    && !disabledReason.equals(dest.getDisabledReason())) {
				dest.setDisabledReason(disabledReason);
			}
		}
	}

	private static void copyAuthenticationDataToProfile(CreateUserForm src,
	    AuthenticationData authData) {
		UserAccountPolicy userAccountPolicy = authData.getUserAccountPolicy();

		if (userAccountPolicy != null) {
			int metric = src.getInactivityMetric();
			int multiplier = DracConstants.SECS_MULTIPLIER[metric];
			try {
				userAccountPolicy.setInactivityPeriod(new Integer(src
				    .getInactivityPeriod()) * multiplier);
			}
			catch (NumberFormatException nfe) {
				// value is blank or not a number, use null
				userAccountPolicy.setInactivityPeriod(null);
			}

			try {
				userAccountPolicy.setMaxInvalidLoginAttempts(new Integer(src
				    .getMaxInvalidLoginAttempts()));
			}
			catch (NumberFormatException nfe) {
				userAccountPolicy.setMaxInvalidLoginAttempts(null);
			}

			metric = src.getLockoutMetric();
			multiplier = DracConstants.SECS_MULTIPLIER[metric];
			try {
				userAccountPolicy.setLockoutPeriod(new Integer(src.getLockoutPeriod())
				    * multiplier);
			}
			catch (NumberFormatException nfe) {
				userAccountPolicy.setLockoutPeriod(null);
			}

			try {
				userAccountPolicy.setDormantPeriod(new Integer(src.getDormantPeriod()));
			}
			catch (NumberFormatException nfe) {
				userAccountPolicy.setDormantPeriod(null);
			}
			String[] lockedClientIPs = src.getLockedClientIPs();
			List<String> lockedClientIPsList = new ArrayList<String>();
			for (String lockedClientIP : lockedClientIPs) {
				lockedClientIPsList.add(lockedClientIP);
			}
			userAccountPolicy.setLockedClientIPs(lockedClientIPsList);

			PasswordPolicy myPasswordPolicy = userAccountPolicy
			    .getLocalPasswordPolicy();
			if (myPasswordPolicy != null) {
				try {
					myPasswordPolicy.setPwAging(new Integer(src.getPasswordAging()));
				}
				catch (NumberFormatException nfe) {
					myPasswordPolicy.setPwAging(null);
				}
				try {
					myPasswordPolicy.setPwHistorySize(new Integer(src
					    .getPasswordHistorySize()));
				}
				catch (NumberFormatException nfe) {
					myPasswordPolicy.setPwHistorySize(null);
				}
				try {
					myPasswordPolicy.setPwExpiredNotif(new Integer(src
					    .getPasswordExpirationNotification()));
				}
				catch (NumberFormatException nfe) {
					myPasswordPolicy.setPwExpiredNotif(null);
				}
				// need to add invalid passwords and password rules here.
			}
		}
	}

	/**
	 * copies the user edit form data to the user profile auth data.
	 * 
	 * @param EditUserForm
	 *          src
	 * @param AuthenticationData
	 *          authData
	 * @return
	 */
	private static void copyEditFormAuthDataToProfile(EditUserForm src,
	    AuthenticationData authData) {
		UserAccountPolicy userAccountPolicy = authData.getUserAccountPolicy();

		if (authData.getAuthenticationType().equals(AuthenticationType.INTERNAL)) {
			// We are editing a user that has an internal account.
			InternalAccountData intAccountData = authData.getInternalAccountData();
			if (intAccountData != null) {
				if (src.getPassword() != null && !src.getPassword().trim().equals("")
				    && !src.getPassword().equals(intAccountData.getUserPassword())) {
					// password changed. leave it up to validator to check passwords match
					intAccountData.setUserPassword(src.getPassword());
				}
			}

			if (userAccountPolicy != null) {
				int metric = src.getInactivityMetric();
				int multiplier = DracConstants.SECS_MULTIPLIER[metric];
				try {
					userAccountPolicy.setInactivityPeriod(new Integer(src
					    .getInactivityPeriod()) * multiplier);
				}
				catch (NumberFormatException nfe) {
					// value is blank or not a number, use null
					userAccountPolicy.setInactivityPeriod(null);
				}

				try {
					userAccountPolicy.setMaxInvalidLoginAttempts(new Integer(src
					    .getMaxInvalidLoginAttempts()));
				}
				catch (NumberFormatException nfe) {
					userAccountPolicy.setMaxInvalidLoginAttempts(null);
				}

				metric = src.getLockoutMetric();
				multiplier = DracConstants.SECS_MULTIPLIER[metric];
				try {
					userAccountPolicy
					    .setLockoutPeriod(new Integer(src.getLockoutPeriod())
					        * multiplier);
				}
				catch (NumberFormatException nfe) {
					userAccountPolicy.setLockoutPeriod(null);
				}

				try {
					userAccountPolicy
					    .setDormantPeriod(new Integer(src.getDormantPeriod()));
				}
				catch (NumberFormatException nfe) {
					userAccountPolicy.setDormantPeriod(null);
				}

				String[] lockedClientIPs = src.getLockedClientIPs();
				List<String> lockedClientIPsList = new ArrayList<String>();
				for (String lockedClientIP : lockedClientIPs) {
					lockedClientIPsList.add(lockedClientIP);
				}
				userAccountPolicy.setLockedClientIPs(lockedClientIPsList);

				PasswordPolicy myPasswordPolicy = userAccountPolicy
				    .getLocalPasswordPolicy();
				if (myPasswordPolicy != null) {
					try {
						myPasswordPolicy.setPwAging(new Integer(src.getPasswordAging()));
					}
					catch (NumberFormatException nfe) {
						myPasswordPolicy.setPwAging(null);
					}
					try {
						myPasswordPolicy.setPwHistorySize(new Integer(src
						    .getPasswordHistorySize()));
					}
					catch (NumberFormatException nfe) {
						myPasswordPolicy.setPwHistorySize(null);
					}
					try {
						myPasswordPolicy.setPwExpiredNotif(new Integer(src
						    .getPasswordExpirationNotification()));
					}
					catch (NumberFormatException nfe) {
						myPasswordPolicy.setPwExpiredNotif(null);
					}
				}
			}
		}
		else if (authData.getAuthenticationType().equals(
		    AuthenticationType.A_SELECT)) {
			// We are editing a user that has an A-Select account.
			if (userAccountPolicy != null) {
				try {
					userAccountPolicy
					    .setDormantPeriod(new Integer(src.getDormantPeriod()));
				}
				catch (NumberFormatException nfe) {
					userAccountPolicy.setDormantPeriod(null);
				}
			}
		}
	}

	public String toString(UserForm form) {
		Method[] methods = UserForm.class.getDeclaredMethods();
		StringBuilder buf = new StringBuilder();
		Method method;
		for (Method method2 : methods) {
			method = method2;
			if (method.getName().startsWith("get")) {
				try {
					buf.append(method.getName() + ": "
					    + method.invoke(form, new Object[0]) + "\n");
				}
				catch (InvocationTargetException ite) {
					log.error("Error: ", ite);
				}
				catch (IllegalAccessException iae) {
					log.error("Error: ", iae);
				}
			}
		}
		return buf.toString();
	}
}
