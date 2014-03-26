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

package com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.struts.validator.ValidatorActionForm;

import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on 12-Jul-2006
 */
public final class CreateUserForm extends ValidatorActionForm {
	private static final long serialVersionUID = -4978802884614623765L;

	// Fields pertaining to create user page.
	private String userID = DracConstants.EMPTY_STRING;

	// Account Status
	private int accountState;
	private String disabledReason = DracConstants.EMPTY_STRING;

	// Authentication data
	private List<String> availAuthenticationTypes = new ArrayList<String>();
	private String authType = DracConstants.EMPTY_STRING;
	private String wsdlCertificate = DracConstants.EMPTY_STRING;

	// Internal Account data
	private String userPassword = DracConstants.EMPTY_STRING;
	private String confirmUserPassword = DracConstants.EMPTY_STRING;

	// Personal data
	private String surname = DracConstants.EMPTY_STRING;
	private String givenName = DracConstants.EMPTY_STRING;
	private String commonName = DracConstants.EMPTY_STRING;
	private String email = DracConstants.EMPTY_STRING;
	private String postalAddress = DracConstants.EMPTY_STRING;
	private String phone = DracConstants.EMPTY_STRING;
	private String description = DracConstants.EMPTY_STRING;
	private String title = DracConstants.EMPTY_STRING;

	// Organization data
	private String orgName = DracConstants.EMPTY_STRING;
	private String orgUnitName = DracConstants.EMPTY_STRING;
	private String owner = DracConstants.EMPTY_STRING;
	private String orgDescription = DracConstants.EMPTY_STRING;
	private String seeAlso = DracConstants.EMPTY_STRING;
	private String category = DracConstants.EMPTY_STRING;

	// Membership data
	private String[] availableUserGroups = {};
	private String[] memberUserGroups = {}; // returns the selected UserGroup
	                                        // Names

	// Account Policy
	private String dormantPeriodGP = DracConstants.EMPTY_STRING;
	private String dormantPeriod = DracConstants.EMPTY_STRING;
	private String inactivityPeriodGP = DracConstants.EMPTY_STRING;
	private String inactivityPeriod = DracConstants.EMPTY_STRING;
	private int inactivityMetric;
	private String maxInvalidLoginAttemptsGP = DracConstants.EMPTY_STRING;
	private String maxInvalidLoginAttempts = DracConstants.EMPTY_STRING;
	private String lockoutPeriodGP = DracConstants.EMPTY_STRING;
	private String lockoutPeriod = DracConstants.EMPTY_STRING;
	private int lockoutMetric;
	private String clientIPToLock = DracConstants.EMPTY_STRING;
	private List<String> lockedClientIPsGP = new ArrayList<String>();
	private String[] lockedClientIPs = {};
	private String passwordAgingGP = DracConstants.EMPTY_STRING;
	private String passwordAging = DracConstants.EMPTY_STRING;
	private String passwordExpirationNotificationGP = DracConstants.EMPTY_STRING;
	private String passwordExpirationNotification = DracConstants.EMPTY_STRING;
	private String passwordHistorySizeGP = DracConstants.EMPTY_STRING;
	private String passwordHistorySize = DracConstants.EMPTY_STRING;

	// Preferences
	private String timeZone = TimeZone.getDefault().getID();
	private String[] timeZoneNames = DateFormatter.getGuiTimeZoneNames();
	private String[] timeZoneIds = TimeZone.getAvailableIDs();

	// Fields returned when user is created for display.
	private String status = DracConstants.EMPTY_STRING;

	/**
	 * @return Returns the accountState.
	 */
	public int getAccountState() {
		return accountState;
	}

	/**
	 * @return the authType
	 */
	public String getAuthType() {
		return authType;
	}

	/**
	 * @return Returns the availableUserGroups.
	 */
	// see referencingUserGroupNamesList in createResourceGroup.jsp
	public String[] getAvailableUserGroups() {
		return availableUserGroups;
	}

	/**
	 * @return the availAuthenticationTypes
	 */
	public List<String> getAvailAuthenticationTypes() {
		return availAuthenticationTypes;
	}

	/**
	 * @return Returns the category.
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return Returns the clientIPToLock.
	 */
	public String getClientIPToLock() {
		return clientIPToLock;
	}

	/**
	 * @return Returns the commonName.
	 */
	public String getCommonName() {
		return commonName;
	}

	/**
	 * @return Returns the confirmUserPassword.
	 */
	public String getConfirmUserPassword() {
		return confirmUserPassword;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Returns the disabledReason.
	 */
	public String getDisabledReason() {
		return disabledReason;
	}

	/**
	 * @return Returns the dormantPeriod.
	 */
	public String getDormantPeriod() {
		return dormantPeriod;
	}

	/**
	 * @return Returns the dormantPeriodGP.
	 */
	public String getDormantPeriodGP() {
		return dormantPeriodGP;
	}

	/**
	 * @return Returns the email.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return Returns the givenName.
	 */
	public String getGivenName() {
		return givenName;
	}

	/**
	 * @return the inactivityMetric
	 */
	public int getInactivityMetric() {
		return inactivityMetric;
	}

	/**
	 * @return Returns the inactivityPeriod.
	 */
	public String getInactivityPeriod() {
		return inactivityPeriod;
	}

	/**
	 * @return Returns the inactivityPeriodGP.
	 */
	public String getInactivityPeriodGP() {
		return inactivityPeriodGP;
	}

	/**
	 * @return the lockedClientIPs
	 */
	public String[] getLockedClientIPs() {
		return lockedClientIPs;
	}

	/**
	 * @return Returns the lockedClientIPsGP.
	 */
	public List<String> getLockedClientIPsGP() {
		return lockedClientIPsGP;
	}

	/**
	 * @return the lockoutMetric
	 */
	public int getLockoutMetric() {
		return lockoutMetric;
	}

	/**
	 * @return Returns the lockoutPeriod.
	 */
	public String getLockoutPeriod() {
		return lockoutPeriod;
	}

	/**
	 * @return Returns the lockoutPeriodGP.
	 */
	public String getLockoutPeriodGP() {
		return lockoutPeriodGP;
	}

	/**
	 * @return Returns the maxInvalidLoginAttempts.
	 */
	public String getMaxInvalidLoginAttempts() {
		return maxInvalidLoginAttempts;
	}

	/**
	 * @return Returns the maxInvalidLoginAttemptsGP.
	 */
	public String getMaxInvalidLoginAttemptsGP() {
		return maxInvalidLoginAttemptsGP;
	}

	/**
	 * @return Returns the memberUserGroups.
	 */
	// see referencingUserGroupNames in createResourceGroup.jsp
	public String[] getMemberUserGroups() {
		return memberUserGroups;
	}

	/**
	 * @return Returns the orgDescription.
	 */
	public String getOrgDescription() {
		return orgDescription;
	}

	/**
	 * @return Returns the orgName.
	 */
	public String getOrgName() {
		return orgName;
	}

	/**
	 * @return Returns the orgUnitName.
	 */
	public String getOrgUnitName() {
		return orgUnitName;
	}

	/**
	 * @return Returns the owner.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @return Returns the passwordAging.
	 */
	public String getPasswordAging() {
		return passwordAging;
	}

	/**
	 * @return Returns the passwordAgingGP.
	 */
	public String getPasswordAgingGP() {
		return passwordAgingGP;
	}

	/**
	 * @return Returns the passwordExpirationNotification.
	 */
	public String getPasswordExpirationNotification() {
		return passwordExpirationNotification;
	}

	/**
	 * @return Returns the passwordExpirationNotificationGP.
	 */
	public String getPasswordExpirationNotificationGP() {
		return passwordExpirationNotificationGP;
	}

	/**
	 * @return Returns the passwordHistorySize.
	 */
	public String getPasswordHistorySize() {
		return passwordHistorySize;
	}

	/**
	 * @return Returns the passwordHistorySizeGP.
	 */
	public String getPasswordHistorySizeGP() {
		return passwordHistorySizeGP;
	}

	/**
	 * @return Returns the phone.
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @return Returns the postalAddress.
	 */
	public String getPostalAddress() {
		return postalAddress;
	}

	/**
	 * @return Returns the seeAlso.
	 */
	public String getSeeAlso() {
		return seeAlso;
	}

	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return Returns the surname.
	 */
	public String getSurname() {
		return surname;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public String[] getTimeZoneIds() {
		return timeZoneIds;
	}

	public String[] getTimeZoneNames() {
		return timeZoneNames;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Returns the userID.
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * @return Returns the userPassword.
	 */
	public String getUserPassword() {
		return userPassword;
	}

	/**
	 * @return the wsdlCertificate
	 */
	public String getwsdlCertificate() {
		return wsdlCertificate;
	}

	/**
	 * @param accountState
	 *          The accountState to set.
	 */
	public void setAccountState(int accountState) {
		this.accountState = accountState;
	}

	/**
	 * @param authType
	 *          the authType to set
	 */
	public void setAuthType(String authType) {
		this.authType = authType;
	}

	/**
	 * @param availableUserGroups
	 *          The availableUserGroups to set.
	 */
	public void setAvailableUserGroups(String[] availableUserGroups) {
		this.availableUserGroups = availableUserGroups;
	}

	/**
	 * @param availAuthenticationTypes
	 *          the availAuthenticationTypes to set
	 */
	public void setAvailAuthenticationTypes(List<String> availAuthenticationTypes) {
		this.availAuthenticationTypes = availAuthenticationTypes;
	}

	/**
	 * @param category
	 *          The category to set.
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @param clientIPToLock
	 *          The clientIPToLock to set.
	 */
	public void setClientIPToLock(String clientIPToLock) {
		this.clientIPToLock = clientIPToLock;
	}

	/**
	 * @param commonName
	 *          The commonName to set.
	 */
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/**
	 * @param confirmUserPassword
	 *          The confirmUserPassword to set.
	 */
	public void setConfirmUserPassword(String confirmUserPassword) {
		this.confirmUserPassword = confirmUserPassword;
	}

	/**
	 * @param description
	 *          The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param disabledReason
	 *          The disabledReason to set.
	 */
	public void setDisabledReason(String disabledReason) {
		this.disabledReason = disabledReason;
	}

	/**
	 * @param dormantPeriod
	 *          The dormantPeriod to set.
	 */
	public void setDormantPeriod(String dormantPeriod) {
		this.dormantPeriod = dormantPeriod;
	}

	/**
	 * @param dormantPeriodGP
	 *          The dormantPeriodGP to set.
	 */
	public void setDormantPeriodGP(String dormantPeriodGP) {
		this.dormantPeriodGP = dormantPeriodGP;
	}

	/**
	 * @param email
	 *          The email to set.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @param givenName
	 *          The givenName to set.
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * @param inactivityMetric
	 *          the inactivityMetric to set
	 */
	public void setInactivityMetric(int inactivityMetric) {
		this.inactivityMetric = inactivityMetric;
	}

	/**
	 * @param inactivityPeriod
	 *          The inactivityPeriod to set.
	 */
	public void setInactivityPeriod(String inactivityPeriod) {
		this.inactivityPeriod = inactivityPeriod;
	}

	/**
	 * @param inactivityPeriodGP
	 *          The inactivityPeriodGP to set.
	 */
	public void setInactivityPeriodGP(String inactivityPeriodGP) {
		this.inactivityPeriodGP = inactivityPeriodGP;
	}

	/**
	 * @param lockedClientIPs
	 *          the lockedClientIPs to set
	 */
	public void setLockedClientIPs(String[] lockedClientIPs) {
		this.lockedClientIPs = lockedClientIPs;
	}

	/**
	 * @param lockedClientIPsGP
	 *          The lockedClientIPsGP to set.
	 */
	public void setLockedClientIPsGP(List<String> lockedClientIPsGP) {
		this.lockedClientIPsGP = lockedClientIPsGP;
	}

	/**
	 * @param lockoutMetric
	 *          the lockoutMetric to set
	 */
	public void setLockoutMetric(int lockoutMetric) {
		this.lockoutMetric = lockoutMetric;
	}

	/**
	 * @param lockoutPeriod
	 *          The lockoutPeriod to set.
	 */
	public void setLockoutPeriod(String lockoutPeriod) {
		this.lockoutPeriod = lockoutPeriod;
	}

	/**
	 * @param lockoutPeriodGP
	 *          The lockoutPeriodGP to set.
	 */
	public void setLockoutPeriodGP(String lockoutPeriodGP) {
		this.lockoutPeriodGP = lockoutPeriodGP;
	}

	/**
	 * @param maxInvalidLoginAttempts
	 *          The maxInvalidLoginAttempts to set.
	 */
	public void setMaxInvalidLoginAttempts(String maxInvalidLoginAttempts) {
		this.maxInvalidLoginAttempts = maxInvalidLoginAttempts;
	}

	/**
	 * @param maxInvalidLoginAttemptsGP
	 *          The maxInvalidLoginAttemptsGP to set.
	 */
	public void setMaxInvalidLoginAttemptsGP(String maxInvalidLoginAttemptsGP) {
		this.maxInvalidLoginAttemptsGP = maxInvalidLoginAttemptsGP;
	}

	/**
	 * @param memberUserGroups
	 *          The memberUserGroups to set.
	 */
	public void setMemberUserGroups(String[] memberUserGroups) {
		this.memberUserGroups = memberUserGroups;
	}

	/**
	 * @param orgDescription
	 *          The orgDescription to set.
	 */
	public void setOrgDescription(String orgDescription) {
		this.orgDescription = orgDescription;
	}

	/**
	 * @param orgName
	 *          The orgName to set.
	 */
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	/**
	 * @param orgUnitName
	 *          The orgUnitName to set.
	 */
	public void setOrgUnitName(String orgUnitName) {
		this.orgUnitName = orgUnitName;
	}

	/**
	 * @param owner
	 *          The owner to set.
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @param passwordAging
	 *          The passwordAging to set.
	 */
	public void setPasswordAging(String passwordAging) {
		this.passwordAging = passwordAging;
	}

	/**
	 * @param passwordAgingGP
	 *          The passwordAgingGP to set.
	 */
	public void setPasswordAgingGP(String passwordAgingGP) {
		this.passwordAgingGP = passwordAgingGP;
	}

	/**
	 * @param passwordExpirationNotification
	 *          The passwordExpirationNotification to set.
	 */
	public void setPasswordExpirationNotification(
	    String passwordExpirationNotification) {
		this.passwordExpirationNotification = passwordExpirationNotification;
	}

	/**
	 * @param passwordExpirationNotificationGP
	 *          The passwordExpirationNotificationGP to set.
	 */
	public void setPasswordExpirationNotificationGP(
	    String passwordExpirationNotificationGP) {
		this.passwordExpirationNotificationGP = passwordExpirationNotificationGP;
	}

	/**
	 * @param passwordHistorySize
	 *          The passwordHistorySize to set.
	 */
	public void setPasswordHistorySize(String passwordHistorySize) {
		this.passwordHistorySize = passwordHistorySize;
	}

	/**
	 * @param passwordHistorySizeGP
	 *          The passwordHistorySizeGP to set.
	 */
	public void setPasswordHistorySizeGP(String passwordHistorySizeGP) {
		this.passwordHistorySizeGP = passwordHistorySizeGP;
	}

	/**
	 * @param phone
	 *          The phone to set.
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @param postalAddress
	 *          The postalAddress to set.
	 */
	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}

	/**
	 * @param seeAlso
	 *          The seeAlso to set.
	 */
	public void setSeeAlso(String seeAlso) {
		this.seeAlso = seeAlso;
	}

	/**
	 * @param status
	 *          The status to set.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @param surname
	 *          The surname to set.
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public void setTimeZoneIds(String[] timeZoneIds) {
		this.timeZoneIds = timeZoneIds;
	}

	public void setTimeZoneNames(String[] timeZoneNames) {
		this.timeZoneNames = timeZoneNames;
	}

	/**
	 * @param title
	 *          The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param userID
	 *          The userID to set.
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

	/**
	 * @param userPassword
	 *          The userPassword to set.
	 */
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	/**
	 * @param wsdlCertificate
	 *          the wsdlCertificate to set
	 */
	public void setwsdlCertificate(String wsdlCertificate) {
		this.wsdlCertificate = wsdlCertificate;
	}

}
