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

package com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.utility.StringParser;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.security.form.RuleForm;

/**
 * Created on 19-Oct-06
 */
public final class UserGroupForm extends ValidatorForm implements
    Comparable<UserGroupForm> {
	private static final long serialVersionUID = 4823166849424068462L;
	private String name = DracConstants.EMPTY_STRING;
	// URL safe...for input
	private String webSafeName = DracConstants.EMPTY_STRING;
	private String creationDate = DracConstants.EMPTY_STRING;
	private String lastModifiedDate = DracConstants.EMPTY_STRING;
	private String creationTime = DracConstants.EMPTY_STRING;
	private String lastModifiedTime = DracConstants.EMPTY_STRING;
	private String lastModificationUserID = DracConstants.EMPTY_STRING;
	private UserGroupType userGroupType = UserGroupType.USER;
	private Boolean defaultUserGroup = Boolean.FALSE;
	private String parentUserGroup = DracConstants.EMPTY_STRING;

	// membership info
	private String[] userMembership = {};
	private List<UserGroupForm> childGroups = new ArrayList<UserGroupForm>();
	private String[] availableUsers = {};
	private String[] userGroupMembership = {};
	private String[] availableUserGroups = {};
	private String[] resourceGroupMembership = {};
	private String[] availableResourceGroups = {};

	private String[] groupAccessControlRules = {};
	private String[] groupSystemAccessRules = {};
	private RuleForm[] groupRules = {};
	private RuleForm[] accessControlRules = {};
	private String maximumServiceSize = "";
	private String maximumServiceDuration = "";
	private String maximumServiceBandwidth = "";
	private String maximumAggregateServiceSize = "";

	private String referencingUserGroupName = DracConstants.EMPTY_STRING;

	private String error = "";

	@Override
	public int compareTo(UserGroupForm ugForm) {
		if (ugForm != null) {
			return this.getName().compareTo(ugForm.getName());
		}

		return 0;
	}

	/**
	 * @return the accessControlRules
	 */
	public RuleForm[] getAccessControlRules() {
		return accessControlRules;
	}

	/**
	 * @return the availableResourceGroups
	 */
	public String[] getAvailableResourceGroups() {
		return availableResourceGroups;
	}

	/**
	 * @return the availableUserGroups
	 */
	public String[] getAvailableUserGroups() {
		return availableUserGroups;
	}

	/**
	 * @return the availableUsers
	 */
	public String[] getAvailableUsers() {
		return availableUsers;
	}

	public List<UserGroupForm> getChildGroups() {
		return childGroups;
	}

	public String getClientSafeName() {
		return StringParser.encodeForXMLSpecialChars(getName());
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
	 * @return the defaultUserGroup
	 */
	public Boolean getDefaultUserGroup() {
		return defaultUserGroup;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @return the groupAccessControlRules
	 */
	public String[] getGroupAccessControlRules() {
		return groupAccessControlRules;
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
	 * @return the lastModificationUserID
	 */
	public String getLastModificationUserID() {
		return lastModificationUserID;
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

	public String getMaximumAggregateServiceSize() {
		return maximumAggregateServiceSize;
	}

	public String getMaximumServiceBandwidth() {
		return maximumServiceBandwidth;
	}

	public String getMaximumServiceDuration() {
		return maximumServiceDuration;
	}

	public String getMaximumServiceSize() {
		return maximumServiceSize;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the parentUserGroup
	 */
	public String getParentUserGroup() {
		return parentUserGroup;
	}

	/**
	 * @return the referencingUserGroupName
	 */
	public String getReferencingUserGroupName() {
		return referencingUserGroupName;
	}

	/**
	 * @return the resourceGroupMembership
	 */
	public String[] getResourceGroupMembership() {
		return resourceGroupMembership;
	}

	/**
	 * @return the userGroupMembership
	 */
	public String[] getUserGroupMembership() {
		return userGroupMembership;
	}

	/**
	 * @return the userGroupType
	 */
	public UserGroupType getUserGroupType() {
		return userGroupType;
	}

	/**
	 * @return the userMembership
	 */
	public String[] getUserMembership() {
		return userMembership;
	}

	public String getWebSafeName() {
		return this.webSafeName;
	}

	/**
	 * @param accessControlRules
	 *          the accessControlRules to set
	 */
	public void setAccessControlRules(RuleForm[] accessControlRules) {
		this.accessControlRules = accessControlRules;
	}

	/**
	 * @param availableResourceGroups
	 *          the availableResourceGroups to set
	 */
	public void setAvailableResourceGroups(String[] availableResourceGroups) {
		this.availableResourceGroups = availableResourceGroups;
	}

	/**
	 * @param availableUserGroups
	 *          the availableUserGroups to set
	 */
	public void setAvailableUserGroups(String[] availableUserGroups) {
		this.availableUserGroups = availableUserGroups;
	}

	/**
	 * @param availableUsers
	 *          the availableUsers to set
	 */
	public void setAvailableUsers(String[] availableUsers) {
		this.availableUsers = availableUsers;
	}

	public void setChildGroups(List<UserGroupForm> childGroups) {
		this.childGroups = childGroups;
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
	 * @param defaultUserGroup
	 *          the defaultUserGroup to set
	 */
	public void setDefaultUserGroup(Boolean defaultUserGroup) {
		this.defaultUserGroup = defaultUserGroup;
	}

	/**
	 * @param error
	 *          the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * @param groupAccessControlRules
	 *          the groupAccessControlRules to set
	 */
	public void setGroupAccessControlRules(String[] groupAccessControlRules) {
		this.groupAccessControlRules = groupAccessControlRules;
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
	 * @param lastModificationUserID
	 *          the lastModificationUserID to set
	 */
	public void setLastModificationUserID(String lastModificationUserID) {
		this.lastModificationUserID = lastModificationUserID;
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

	public void setMaximumAggregateServiceSize(String maximumAggregateServiceSize) {
		this.maximumAggregateServiceSize = maximumAggregateServiceSize;
	}

	public void setMaximumServiceBandwidth(String maximumServiceBandwidth) {
		this.maximumServiceBandwidth = maximumServiceBandwidth;
	}

	public void setMaximumServiceDuration(String maximumServiceDuration) {
		this.maximumServiceDuration = maximumServiceDuration;
	}

	public void setMaximumServiceSize(String maximumServiceSize) {
		this.maximumServiceSize = maximumServiceSize;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param parentUserGroup
	 *          the parentUserGroup to set
	 */
	public void setParentUserGroup(String parentUserGroup) {
		this.parentUserGroup = parentUserGroup;
	}

	/**
	 * @param referencingUserGroupName
	 *          the referencingUserGroupName to set
	 */
	public void setReferencingUserGroupName(String referencingUserGroupName) {
		this.referencingUserGroupName = referencingUserGroupName;
	}

	/**
	 * @param resourceGroupMembership
	 *          the resourceGroupMembership to set
	 */
	public void setResourceGroupMembership(String[] resourceGroupMembership) {
		this.resourceGroupMembership = resourceGroupMembership;
	}

	/**
	 * @param userGroupMembership
	 *          the userGroupMembership to set
	 */
	public void setUserGroupMembership(String[] userGroupMembership) {
		this.userGroupMembership = userGroupMembership;
	}

	/**
	 * @param userGroupType
	 *          the userGroupType to set
	 */
	public void setUserGroupType(UserGroupType userGroupType) {
		this.userGroupType = userGroupType;
	}

	/**
	 * @param userMembership
	 *          the userMembership to set
	 */
	public void setUserMembership(String[] userMembership) {
		this.userMembership = userMembership;
	}

	public void setWebSafeName(String webSafeName) throws Exception {
		this.webSafeName = DracHelper.encodeToUTF8(webSafeName);
	}

}
