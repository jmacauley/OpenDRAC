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

package com.nortel.appcore.app.drac.server.webserver.struts.security.form;

import org.apache.struts.validator.ValidatorActionForm;

import com.nortel.appcore.app.drac.common.security.policy.types.AccessStateRule;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfileXML.ResourceAccessStateRule;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on Oct 23, 2006
 */
public final class GlobalPolicyForm extends ValidatorActionForm {
	private static final long serialVersionUID = -4696768571628065348L;

	public static AccessStateRule[] states = {
	    new AccessStateRule(ResourceAccessStateRule.OPEN),
	    new AccessStateRule(ResourceAccessStateRule.CLOSED),
	    new AccessStateRule(ResourceAccessStateRule.DISABLED) };

	// Field just checks if form has data, will be set by webpage
	private String redirected = DracConstants.EMPTY_STRING;

	// Account Policy
	private String dormancy = DracConstants.EMPTY_STRING;
	private String inactivityPeriod = DracConstants.EMPTY_STRING;
	private int inactivityMetric = 0;
	private String invalidLogins = DracConstants.EMPTY_STRING;
	private String lockoutPeriod = DracConstants.EMPTY_STRING;
	private int lockoutMetric = 0;
	private String clientIPToLock = DracConstants.EMPTY_STRING;
	private String[] lockedClientIPs = {};
	private String[] tempLockedClientIPs = {};
	
	// Password Policy
	private String passwordAging = DracConstants.EMPTY_STRING;
	private String passwordExpirationNotification = DracConstants.EMPTY_STRING;
	private String passwordHistorySize = DracConstants.EMPTY_STRING;
	private String invalidPassword = DracConstants.EMPTY_STRING;
	private String[] invalidPasswords = {};

	// Password Rules
	private String specialCharacters = DracConstants.EMPTY_STRING;
	private String minPasswordLength = DracConstants.EMPTY_STRING;
	private String minAlphaCharacters = DracConstants.EMPTY_STRING;
	private String minNumericCharacters = DracConstants.EMPTY_STRING;
	private String minSpecialCharacters = DracConstants.EMPTY_STRING;
	private String minDifferentCharacters = DracConstants.EMPTY_STRING;
	private String mixedCaseCharacters = DracConstants.EMPTY_STRING;

	// indicates what authentication types are supported by DRAC
	private boolean supportAselect = false;
	private boolean supportRadius = false;
	private boolean supportInternal = false;

	private boolean aselect = false;
	private boolean radius = false;
	private boolean internal = false;

	// UserGroupPolicy fields
	private String[] groupAccessControlRules = {};
	private String[] groupSystemAccessRules = {};
	private RuleForm[] groupRules = {};
	private RuleForm[] accessControlRules = {};
	private String groupMaxServiceSize = DracConstants.EMPTY_STRING;
	private String groupMaxServiceDuration = DracConstants.EMPTY_STRING;
	private String groupMaxServiceBandwidth = DracConstants.EMPTY_STRING;
	private String groupMaxAggregateServiceSize = DracConstants.EMPTY_STRING;

	// Resource Group Policy fields
	private int resourceAccessState = 0;
	private String[] resourceSystemAccessRules = {};
	private RuleForm[] resRules = {};
	private String resourceMaxServiceSize = DracConstants.EMPTY_STRING;
	private String resourceMaxServiceDuration = DracConstants.EMPTY_STRING;
	private String resourceMaxServiceBandwidth = DracConstants.EMPTY_STRING;
	private String resourceMaxAggregateServiceSize = DracConstants.EMPTY_STRING;

	/**
	 * @return the accessControlRules
	 */
	public RuleForm[] getAccessControlRules() {
		return accessControlRules;
	}

	/**
	 * @return Returns the clientIPToLock.
	 */
	public String getClientIPToLock() {
		return clientIPToLock;
	}

	/**
	 * @return the dormancy
	 */
	public String getDormancy() {
		return dormancy;
	}

	/**
	 * @return the groupAccessControlRules
	 */
	public String[] getGroupAccessControlRules() {
		return groupAccessControlRules;
	}

	/**
	 * @return the groupMaxAggregateServiceSize
	 */
	public String getGroupMaxAggregateServiceSize() {
		return groupMaxAggregateServiceSize;
	}

	/**
	 * @return the groupMaxServiceBandwidth
	 */
	public String getGroupMaxServiceBandwidth() {
		return groupMaxServiceBandwidth;
	}

	/**
	 * @return the groupMaxServiceDuration
	 */
	public String getGroupMaxServiceDuration() {
		return groupMaxServiceDuration;
	}

	/**
	 * @return the groupMaxServiceSize
	 */
	public String getGroupMaxServiceSize() {
		return groupMaxServiceSize;
	}

	/**
	 * @return the groupRules
	 */
	public RuleForm[] getGroupRules() {
		return groupRules;
	}

	/**
	 * @return the groupSystemAccessRules
	 */
	public String[] getGroupSystemAccessRules() {
		return groupSystemAccessRules;
	}

	/**
	 * @return the inactivityMetric
	 */
	public int getInactivityMetric() {
		return inactivityMetric;
	}

	/**
	 * @return the inactivityPeriod
	 */
	public String getInactivityPeriod() {
		return inactivityPeriod;
	}

	/**
	 * @return the invalidLogins
	 */
	public String getInvalidLogins() {
		return invalidLogins;
	}

	/**
	 * @return Returns the invalidPassword.
	 */
	public String getInvalidPassword() {
		return invalidPassword;
	}

	/**
	 * @return Returns the invalidPasswords.
	 */
	public String[] getInvalidPasswords() {
		return invalidPasswords;
	}

	/**
	 * @return the lockedClientIPs
	 */
	public String[] getLockedClientIPs() {
		return lockedClientIPs;
	}

	/**
	 * @return the lockoutMetric
	 */
	public int getLockoutMetric() {
		return lockoutMetric;
	}

	/**
	 * @return the lockoutPeriod
	 */
	public String getLockoutPeriod() {
		return lockoutPeriod;
	}

	/**
	 * @return Returns the minAlphaCharacters.
	 */
	public String getMinAlphaCharacters() {
		return minAlphaCharacters;
	}

	/**
	 * @return Returns the minDifferentCharacters.
	 */
	public String getMinDifferentCharacters() {
		return minDifferentCharacters;
	}

	/**
	 * @return Returns the minNumericCharacters.
	 */
	public String getMinNumericCharacters() {
		return minNumericCharacters;
	}

	/**
	 * @return Returns the minPasswordLength.
	 */
	public String getMinPasswordLength() {
		return minPasswordLength;
	}

	/**
	 * @return Returns the minSpecialCharacters.
	 */
	public String getMinSpecialCharacters() {
		return minSpecialCharacters;
	}

	/**
	 * @return Returns the mixedCaseCharacters.
	 */
	public String getMixedCaseCharacters() {
		return mixedCaseCharacters;
	}

	/**
	 * @return the passwordAging
	 */
	public String getPasswordAging() {
		return passwordAging;
	}

	/**
	 * @return Returns the passwordExpirationNotification.
	 */
	public String getPasswordExpirationNotification() {
		return passwordExpirationNotification;
	}

	/**
	 * @return Returns the passwordHistorySize.
	 */
	public String getPasswordHistorySize() {
		return passwordHistorySize;
	}

	/**
	 * @return the redirected
	 */
	public String getRedirected() {
		return redirected;
	}

	/**
	 * @return the resourceAccessState
	 */
	public int getResourceAccessState() {
		return resourceAccessState;
	}

	/**
	 * @return the resourceMaxAggregateServiceSize
	 */
	public String getResourceMaxAggregateServiceSize() {
		return resourceMaxAggregateServiceSize;
	}

	/**
	 * @return the resourceMaxServiceBandwidth
	 */
	public String getResourceMaxServiceBandwidth() {
		return resourceMaxServiceBandwidth;
	}

	/**
	 * @return the resourceMaxServiceDuration
	 */
	public String getResourceMaxServiceDuration() {
		return resourceMaxServiceDuration;
	}

	/**
	 * @return the resourceMaxServiceSize
	 */
	public String getResourceMaxServiceSize() {
		return resourceMaxServiceSize;
	}

	/**
	 * @return the resourceSystemAccessRules
	 */
	public String[] getResourceSystemAccessRules() {
		return resourceSystemAccessRules;
	}

	/**
	 * @return the resRules
	 */
	public RuleForm[] getResRules() {
		return resRules;
	}

	/**
	 * @return Returns the specialCharacters.
	 */
	public String getSpecialCharacters() {
		return specialCharacters;
	}

	/**
	 * @return the aselect
	 */
	public boolean isAselect() {
		return aselect;
	}

	/**
	 * @return the internal
	 */
	public boolean isInternal() {
		return internal;
	}

	/**
	 * @return the radius
	 */
	public boolean isRadius() {
		return radius;
	}

	/**
	 * @return the supportAselect
	 */
	public boolean isSupportAselect() {
		return supportAselect;
	}

	/**
	 * @return the supportInternal
	 */
	public boolean isSupportInternal() {
		return supportInternal;
	}

	/**
	 * @return the supportRadius
	 */
	public boolean isSupportRadius() {
		return supportRadius;
	}

	/**
	 * @param accessControlRules
	 *          the accessControlRules to set
	 */
	public void setAccessControlRules(RuleForm[] accessControlRules) {
		this.accessControlRules = accessControlRules;
	}

	/**
	 * @param aselect
	 *          the aselect to set
	 */
	public void setAselect(boolean aselect) {
		this.aselect = aselect;
	}

	/**
	 * @param clientIPToLock
	 *          The clientIPToLock to set.
	 */
	public void setClientIPToLock(String clientIPToLock) {
		this.clientIPToLock = clientIPToLock;
	}

	/**
	 * @param dormancy
	 *          the dormancy to set
	 */
	public void setDormancy(String dormancy) {
		this.dormancy = dormancy;
	}

	/**
	 * @param groupAccessControlRules
	 *          the groupAccessControlRules to set
	 */
	public void setGroupAccessControlRules(String[] groupAccessControlRules) {
		this.groupAccessControlRules = groupAccessControlRules;
	}

	/**
	 * @param groupMaxAggregateServiceSize
	 *          the groupMaxAggregateServiceSize to set
	 */
	public void setGroupMaxAggregateServiceSize(
	    String groupMaxAggregateServiceSize) {
		this.groupMaxAggregateServiceSize = groupMaxAggregateServiceSize;
	}

	/**
	 * @param groupMaxServiceBandwidth
	 *          the groupMaxServiceBandwidth to set
	 */
	public void setGroupMaxServiceBandwidth(String groupMaxServiceBandwidth) {
		this.groupMaxServiceBandwidth = groupMaxServiceBandwidth;
	}

	/**
	 * @param groupMaxServiceDuration
	 *          the groupMaxServiceDuration to set
	 */
	public void setGroupMaxServiceDuration(String groupMaxServiceDuration) {
		this.groupMaxServiceDuration = groupMaxServiceDuration;
	}

	/**
	 * @param groupMaxServiceSize
	 *          the groupMaxServiceSize to set
	 */
	public void setGroupMaxServiceSize(String groupMaxServiceSize) {
		this.groupMaxServiceSize = groupMaxServiceSize;
	}

	/**
	 * @param groupRules
	 *          the groupRules to set
	 */
	public void setGroupRules(RuleForm[] groupRules) {
		this.groupRules = groupRules;
	}

	/**
	 * @param groupSystemAccessRules
	 *          the groupSystemAccessRules to set
	 */
	public void setGroupSystemAccessRules(String[] groupSystemAccessRules) {
		this.groupSystemAccessRules = groupSystemAccessRules;
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
	 *          the inactivityPeriod to set
	 */
	public void setInactivityPeriod(String inactivityPeriod) {
		this.inactivityPeriod = inactivityPeriod;
	}

	/**
	 * @param internal
	 *          the internal to set
	 */
	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	/**
	 * @param invalidLogins
	 *          the invalidLogins to set
	 */
	public void setInvalidLogins(String invalidLogins) {
		this.invalidLogins = invalidLogins;
	}

	/**
	 * @param invalidPassword
	 *          The invalidPassword to set.
	 */
	public void setInvalidPassword(String invalidPassword) {
		this.invalidPassword = invalidPassword;
	}

	/**
	 * @param invalidPasswords
	 *          The invalidPasswords to set.
	 */
	public void setInvalidPasswords(String[] invalidPasswords) {
		this.invalidPasswords = invalidPasswords;
	}

	/**
	 * @param lockedClientIPs
	 *          the lockedClientIPs to set
	 */
	public void setLockedClientIPs(String[] lockedClientIPs) {
		this.lockedClientIPs = lockedClientIPs;
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
	 *          the lockoutPeriod to set
	 */
	public void setLockoutPeriod(String lockoutPeriod) {
		this.lockoutPeriod = lockoutPeriod;
	}

	/**
	 * @param minAlphaCharacters
	 *          The minAlphaCharacters to set.
	 */
	public void setMinAlphaCharacters(String minAlphaCharacters) {
		this.minAlphaCharacters = minAlphaCharacters;
	}

	/**
	 * @param minDifferentCharacters
	 *          The minDifferentCharacters to set.
	 */
	public void setMinDifferentCharacters(String minDifferentCharacters) {
		this.minDifferentCharacters = minDifferentCharacters;
	}

	/**
	 * @param minNumericCharacters
	 *          The minNumericCharacters to set.
	 */
	public void setMinNumericCharacters(String minNumericCharacters) {
		this.minNumericCharacters = minNumericCharacters;
	}

	/**
	 * @param minPasswordLength
	 *          The minPasswordLength to set.
	 */
	public void setMinPasswordLength(String minPasswordLength) {
		this.minPasswordLength = minPasswordLength;
	}

	/**
	 * @param minSpecialCharacters
	 *          The minSpecialCharacters to set.
	 */
	public void setMinSpecialCharacters(String minSpecialCharacters) {
		this.minSpecialCharacters = minSpecialCharacters;
	}

	/**
	 * @param mixedCaseCharacters
	 *          The mixedCaseCharacters to set.
	 */
	public void setMixedCaseCharacters(String mixedCaseCharacters) {
		this.mixedCaseCharacters = mixedCaseCharacters;
	}

	/**
	 * @param passwordAging
	 *          the passwordAging to set
	 */
	public void setPasswordAging(String passwordAging) {
		this.passwordAging = passwordAging;
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
	 * @param passwordHistorySize
	 *          The passwordHistorySize to set.
	 */
	public void setPasswordHistorySize(String passwordHistorySize) {
		this.passwordHistorySize = passwordHistorySize;
	}

	/**
	 * @param radius
	 *          the radius to set
	 */
	public void setRadius(boolean radius) {
		this.radius = radius;
	}

	/**
	 * @param redirected
	 *          the redirected to set
	 */
	public void setRedirected(String redirected) {
		this.redirected = redirected;
	}

	/**
	 * @param resourceAccessState
	 *          the resourceAccessState to set
	 */
	public void setResourceAccessState(int resourceAccessState) {
		this.resourceAccessState = resourceAccessState;
	}

	/**
	 * @param resourceMaxAggregateServiceSize
	 *          the resourceMaxAggregateServiceSize to set
	 */
	public void setResourceMaxAggregateServiceSize(
	    String resourceMaxAggregateServiceSize) {
		this.resourceMaxAggregateServiceSize = resourceMaxAggregateServiceSize;
	}

	/**
	 * @param resourceMaxServiceBandwidth
	 *          the resourceMaxServiceBandwidth to set
	 */
	public void setResourceMaxServiceBandwidth(String resourceMaxServiceBandwidth) {
		this.resourceMaxServiceBandwidth = resourceMaxServiceBandwidth;
	}

	/**
	 * @param resourceMaxServiceDuration
	 *          the resourceMaxServiceDuration to set
	 */
	public void setResourceMaxServiceDuration(String resourceMaxServiceDuration) {
		this.resourceMaxServiceDuration = resourceMaxServiceDuration;
	}

	/**
	 * @param resourceMaxServiceSize
	 *          the resourceMaxServiceSize to set
	 */
	public void setResourceMaxServiceSize(String resourceMaxServiceSize) {
		this.resourceMaxServiceSize = resourceMaxServiceSize;
	}

	/**
	 * @param resourceSystemAccessRules
	 *          the resourceSystemAccessRules to set
	 */
	public void setResourceSystemAccessRules(String[] resourceSystemAccessRules) {
		this.resourceSystemAccessRules = resourceSystemAccessRules;
	}

	/**
	 * @param resRules
	 *          the resRules to set
	 */
	public void setResRules(RuleForm[] resRules) {
		this.resRules = resRules;
	}

	/**
	 * @param specialCharacters
	 *          The specialCharacters to set.
	 */
	public void setSpecialCharacters(String specialCharacters) {
		this.specialCharacters = specialCharacters;
	}

	/**
	 * @param supportAselect
	 *          the supportAselect to set
	 */
	public void setSupportAselect(boolean supportAselect) {
		this.supportAselect = supportAselect;
	}

	/**
	 * @param supportInternal
	 *          the supportInternal to set
	 */
	public void setSupportInternal(boolean supportInternal) {
		this.supportInternal = supportInternal;
	}

	/**
	 * @param supportRadius
	 *          the supportRadius to set
	 */
	public void setSupportRadius(boolean supportRadius) {
		this.supportRadius = supportRadius;
	}

	public String[] getTempLockedClientIPs() {		
    	return tempLockedClientIPs;
    }

	public void setTempLockedClientIPs(String[] tempLockedClientIPs) {
    	this.tempLockedClientIPs = tempLockedClientIPs;
    }

}
