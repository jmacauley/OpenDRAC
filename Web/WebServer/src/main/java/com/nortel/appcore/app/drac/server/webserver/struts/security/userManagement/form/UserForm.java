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

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;

/**
 * Created on 24-Jul-06
 */
public final class UserForm extends ValidatorForm implements
    Comparable<UserForm> {
	private static final long serialVersionUID = 2283748229749779528L;

	private String userID = DracConstants.EMPTY_STRING;

	// Account Status
	private String accountState = DracConstants.EMPTY_STRING;
	private String disabledReason = DracConstants.EMPTY_STRING;

	// Authentication Data
	private String authenticationType = DracConstants.EMPTY_STRING;

	// Personal Data
	private String surname = DracConstants.EMPTY_STRING;
	private String givenName = DracConstants.EMPTY_STRING;
	private String commonName = DracConstants.EMPTY_STRING;
	private String email = DracConstants.EMPTY_STRING;
	private String postalAddress = DracConstants.EMPTY_STRING;
	private String phone = DracConstants.EMPTY_STRING;
	private String description = DracConstants.EMPTY_STRING;
	private String title = DracConstants.EMPTY_STRING;

	// Organization Data
	private String orgName = DracConstants.EMPTY_STRING;
	private String orgUnitName = DracConstants.EMPTY_STRING;
	private String owner = DracConstants.EMPTY_STRING;
	private String orgDescription = DracConstants.EMPTY_STRING;
	private String seeAlso = DracConstants.EMPTY_STRING;
	private String category = DracConstants.EMPTY_STRING;

	// Preferences
	private String timeZone = DracConstants.EMPTY_STRING;

	// Membership Data
	private ArrayList<String> memberUserID = new ArrayList<String>();
	private ArrayList<String> memberUserGroupName = new ArrayList<String>();
	private List<List<String>> memberUserGroupNameTree = new ArrayList<List<String>>();
	private ArrayList<String> memberResourceGroupName = new ArrayList<String>();

	// Account policy
	private String dormantPeriodGP = DracConstants.EMPTY_STRING;
	private String dormantPeriod = DracConstants.EMPTY_STRING;
	private String inactivityPeriodGP = DracConstants.EMPTY_STRING;
	private String inactivityPeriod = DracConstants.EMPTY_STRING;
	private int inactivityMetric = 0;
	private String maxInvalidLoginAttemptsGP = DracConstants.EMPTY_STRING;
	private String maxInvalidLoginAttempts = DracConstants.EMPTY_STRING;
	private String lockoutPeriodGP = DracConstants.EMPTY_STRING;
	private String lockoutPeriod = DracConstants.EMPTY_STRING;
	private int lockoutMetric = 0;
	private String clientIPToLock = DracConstants.EMPTY_STRING;
	private ArrayList<String> lockedClientIPsGP = new ArrayList<String>();
	private ArrayList<String> lockedClientIPs = new ArrayList<String>();
	private String passwordAgingGP = DracConstants.EMPTY_STRING;
	private String passwordAging = DracConstants.EMPTY_STRING;
	private String passwordExpirationNotificationGP = DracConstants.EMPTY_STRING;
	private String passwordExpirationNotification = DracConstants.EMPTY_STRING;
	private String passwordHistorySizeGP = DracConstants.EMPTY_STRING;
	private String passwordHistorySize = DracConstants.EMPTY_STRING;

	private String WSDLCredential = DracConstants.EMPTY_STRING;
	private String invalidPasswords = DracConstants.EMPTY_STRING;
	private String passwordRules = DracConstants.EMPTY_STRING;

	// Other fields - system defined
	private String expirationDate = DracConstants.EMPTY_STRING; // password
	                                                            // related
	private String creationDate = DracConstants.EMPTY_STRING;
	private String lastModifiedDate = DracConstants.EMPTY_STRING;
	private String creationTime = DracConstants.EMPTY_STRING;
	private String lastModifiedTime = DracConstants.EMPTY_STRING;

	private String webSafeName = DracConstants.EMPTY_STRING;
	private String error = "";

	@Override
	public int compareTo(UserForm uForm) {
		if (uForm != null) {
			return this.getUserID().compareTo(uForm.getUserID());
		}
		return 0;
	}

	/**
	 * @return the accountState
	 */
	public String getAccountState() {
		return accountState;
	}

	/**
	 * @return the authenticationType
	 */
	public String getAuthenticationType() {
		return authenticationType;
	}

	/**
	 * @return the category
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
	 * @return the commonName
	 */
	public String getCommonName() {
		return commonName;
	}

	/**
	 * @return the creationDate
	 */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * @return the creationTime
	 */
	public String getCreationTime() {
		return creationTime;
	}

	/**
	 * @return the description
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
	 * @return the eMail
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @return the expirationDate
	 */
	public String getExpirationDate() {
	  if("-1".equals(passwordAging)){
	    return "NEVER";
	  }
		return expirationDate;
	}

	/**
	 * @return the givenName
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
	 * @return Returns the invalidPasswords.
	 */
	public String getInvalidPasswords() {
		return invalidPasswords;
	}

	/**
	 * @return the lastModifiedDate
	 */
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	/**
	 * @return the lastModifiedTime
	 */
	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	/**
	 * @return Returns the lockedClientIPs.
	 */
	public ArrayList getLockedClientIPs() {
		return lockedClientIPs;
	}

	/**
	 * @return Returns the lockedClientIPsGP.
	 */
	public ArrayList getLockedClientIPsGP() {
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
	 * @return the memberResourceGroupName
	 */
	public ArrayList getMemberResourceGroupName() {
		return memberResourceGroupName;
	}

	/**
	 * @return the memberUserGroupName
	 */
	public ArrayList getMemberUserGroupName() {
		return memberUserGroupName;
	}

	public List<List<String>> getMemberUserGroupNameTree() {
		return memberUserGroupNameTree;
	}

	/**
	 * @return the memberUserID
	 */
	public ArrayList getMemberUserID() {
		return memberUserID;
	}

	/**
	 * @return the orgDescription
	 */
	public String getOrgDescription() {
		return orgDescription;
	}

	/**
	 * @return the orgName
	 */
	public String getOrgName() {
		return orgName;
	}

	/**
	 * @return the orgUnitName
	 */
	public String getOrgUnitName() {
		return orgUnitName;
	}

	/**
	 * @return the owner
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
	 * @return Returns the passwordRules.
	 */
	public String getPasswordRules() {
		return passwordRules;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @return the postalAddress
	 */
	public String getPostalAddress() {
		return postalAddress;
	}

	/**
	 * @return the seeAlso
	 */
	public String getSeeAlso() {
		return seeAlso;
	}

	/**
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}

	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

	public String getWebSafeName() {
		return this.webSafeName;
	}

	/**
	 * @return Returns the WSDLCredential.
	 */
	public String getWSDLCredential() {
		return WSDLCredential;
	}

	/**
	 * @param accountState
	 *          the accountState to set
	 */
	public void setAccountState(String accountState) {
		this.accountState = accountState;
	}

	/**
	 * @param authenticationType
	 *          the authenticationType to set
	 */
	public void setAuthenticationType(String authenticationType) {
		this.authenticationType = authenticationType;
	}

	/**
	 * @param category
	 *          the category to set
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
	 *          the commonName to set
	 */
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/**
	 * @param creationDate
	 *          the creationDate to set
	 */
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @param creationTime
	 *          the creationTime to set
	 */
	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * @param description
	 *          the description to set
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
	 * @param eMail
	 *          the eMail to set
	 */
	public void setEmail(String eMail) {
		this.email = eMail;
	}

	/**
	 * @param error
	 *          the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * @param expirationDate
	 *          the expirationDate to set
	 */
	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @param givenName
	 *          the givenName to set
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
	 * @param invalidPasswords
	 *          The invalidPasswords to set.
	 */
	public void setInvalidPasswords(String invalidPasswords) {
		this.invalidPasswords = invalidPasswords;
	}

	/**
	 * @param lastModifiedDate
	 *          the lastModifiedDate to set
	 */
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	/**
	 * @param lastModifiedTime
	 *          the lastModifiedTime to set
	 */
	public void setLastModifiedTime(String lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	/**
	 * @param lockedClientIPs
	 *          The lockedClientIPs to set.
	 */
	public void setLockedClientIPs(ArrayList lockedClientIPs) {
		this.lockedClientIPs = lockedClientIPs;
	}

	/**
	 * @param lockedClientIPsGP
	 *          The lockedClientIPsGP to set.
	 */
	public void setLockedClientIPsGP(ArrayList lockedClientIPsGP) {
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
	 * @param memberResourceGroupName
	 *          the memberResourceGroupName to set
	 */
	public void setMemberResourceGroupName(ArrayList memberResourceGroupName) {
		this.memberResourceGroupName = memberResourceGroupName;
	}

	/**
	 * @param memberUserGroupName
	 *          the memberUserGroupName to set
	 */
	public void setMemberUserGroupName(ArrayList memberUserGroupName) {
		this.memberUserGroupName = memberUserGroupName;
	}

	public void setMemberUserGroupNameTree(
	    List<List<String>> memberUserGroupNameTree) {
		this.memberUserGroupNameTree = memberUserGroupNameTree;
	}

	/**
	 * @param memberUserID
	 *          the memberUserID to set
	 */
	public void setMemberUserID(ArrayList memberUserID) {
		this.memberUserID = memberUserID;
	}

	/**
	 * @param orgDescription
	 *          the orgDescription to set
	 */
	public void setOrgDescription(String orgDescription) {
		this.orgDescription = orgDescription;
	}

	/**
	 * @param orgName
	 *          the orgName to set
	 */
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	/**
	 * @param orgUnitName
	 *          the orgUnitName to set
	 */
	public void setOrgUnitName(String orgUnitName) {
		this.orgUnitName = orgUnitName;
	}

	/**
	 * @param owner
	 *          the owner to set
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
	 * @param passwordRules
	 *          The passwordRules to set.
	 */
	public void setPasswordRules(String passwordRules) {
		this.passwordRules = passwordRules;
	}

	/**
	 * @param phone
	 *          the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @param postalAddress
	 *          the postalAddress to set
	 */
	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}

	/**
	 * @param seeAlso
	 *          the seeAlso to set
	 */
	public void setSeeAlso(String seeAlso) {
		this.seeAlso = seeAlso;
	}

	/**
	 * @param surname
	 *          the surname to set
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * @param title
	 *          the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param userID
	 *          the userID to set
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

	public void setWebSafeName(String webSafeName) throws Exception {
		this.webSafeName = DracHelper.encodeToUTF8(webSafeName);
	}

	/**
	 * @param WSDLCredential
	 *          The WSDLCredential to set.
	 */
	public void setWSDLCredential(String WSDLCredential) {
		this.WSDLCredential = WSDLCredential;
	}

}
