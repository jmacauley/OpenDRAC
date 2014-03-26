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
import java.util.List;
import java.util.TreeSet;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;

/**
 * UserPolicyProfile holds all policy information for a particular user. It is
 * basically comprised of user group info; resource group info, and globalpolicy
 * ibnfo as well
 * 
 * @author vnguyen
 */

public final class UserPolicyProfile implements Serializable {
	private static final long serialVersionUID = 1L;
	private final UserProfile userProfile;
	private final List<UserGroupProfile> userGroupList;
	private final List<ResourceGroupProfile> resourceGroupList;
	private final GlobalPolicy globalPolicy;
	private List<String> listOfResourceName;
	// private List<String> listOfGroupName;
	private UserGroupType userGroupType;
	private Boolean accessAllResources;

	public UserPolicyProfile(UserProfile uppUserProfile,
	    List<UserGroupProfile> uppUserGroupList,
	    List<ResourceGroupProfile> uppResourceGroupList,
	    GlobalPolicy uppGlobalPolicy) {
		userProfile = uppUserProfile;
		resourceGroupList = uppResourceGroupList;
		userGroupList = uppUserGroupList;
		globalPolicy = uppGlobalPolicy;
	}

	public boolean containResourceGroup(ResourceGroupProfile group) {
		if (this.getResourceGroupList() == null
		    || this.getResourceGroupList().isEmpty()) {
			return false;
		}

		for (ResourceGroupProfile resourceGroupProfile : this
		    .getResourceGroupList()) {
			if (group.getName().equals(resourceGroupProfile.getName())) {
				return true;
			}
		}

		return false;
	}

	public GlobalPolicy getGlobalPolicy() {
		return globalPolicy;
	}

	public List<String> getListOfResourcesByName() {
		if (listOfResourceName != null) {
			return listOfResourceName;
		}

		listOfResourceName = new ArrayList<String>();

		if (this.getResourceGroupList().isEmpty()) {
			return listOfResourceName;
		}

		TreeSet<String> set = new TreeSet<String>();
		for (ResourceGroupProfile resourceGroupProfile : this
		    .getResourceGroupList()) {
			if (resourceGroupProfile.getResourceList() != null) {
				for (Resource resource : resourceGroupProfile.getResourceList()) {
					set.add(resource.getResourceID());
				}
			}
		}

		listOfResourceName = new ArrayList<String>(set);
		return listOfResourceName;
	}

	public List<ResourceGroupProfile> getResourceGroupList() {
		return resourceGroupList;
	}

	public List<UserGroupProfile> getUserGroupList() {
		return userGroupList;
	}

	public UserGroupType getUserGroupType() {
		if (this.userGroupType != null) {
			return this.userGroupType;
		}

		// lowest access
		this.userGroupType = UserGroupType.USER;

		if (this.userGroupList == null) {
			return this.userGroupType;
		}

		// we collect all resource groups that contain given resourceID
		for (UserGroupProfile userGroupProfile : this.userGroupList) {
			if (userGroupProfile.getUserGroupType()
			    .equals(UserGroupType.SYSTEM_ADMIN)) {
				this.userGroupType = UserGroupType.SYSTEM_ADMIN;
				return this.userGroupType;
			}
			else if (userGroupProfile.getUserGroupType().equals(
			    UserGroupType.GROUP_ADMIN)) {
				this.userGroupType = UserGroupType.GROUP_ADMIN;
			}
		}
		return this.userGroupType;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public boolean hasAccessToAllResources() {

		if (accessAllResources != null) {
			return accessAllResources.booleanValue();
		}

		this.accessAllResources = Boolean.FALSE;

		if (this.getResourceGroupList() == null) {
			return this.accessAllResources.booleanValue();
		}

		for (ResourceGroupProfile resourceGroupProfile : this
		    .getResourceGroupList()) {
			if (resourceGroupProfile.getDefaultResourceGroup().equals(Boolean.TRUE)) {
				this.accessAllResources = Boolean.TRUE;
				break;
			}
		}

		return this.accessAllResources.booleanValue();
	}

	public boolean hasBelongedToUserGroup(UserGroupProfile group) {
		return hasBelongedToUserGroup(group.getName());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		if (this.userProfile != null) {
			buf.append(this.userProfile.toXMLString());
		}

		if (this.userGroupList != null) {
			for (UserGroupProfile userGroupProfile : this.userGroupList) {
				buf.append('\n');
				buf.append(userGroupProfile.toXMLString());
			}
		}

		if (this.resourceGroupList != null) {
			for (ResourceGroupProfile resourceGroupProfile : this.resourceGroupList) {
				buf.append('\n');
				buf.append(resourceGroupProfile.toXMLString());
			}
		}

		if (this.globalPolicy != null) {
			buf.append(this.globalPolicy.toXMLString());
		}

		return buf.toString();
	}

	private boolean hasBelongedToUserGroup(UserGroupName groupName) {
		if (this.getUserGroupList() == null || this.getUserGroupList().isEmpty()) {
			return false;
		}

		for (UserGroupProfile userGroupProfile : this.getUserGroupList()) {
			if (groupName.equals(userGroupProfile.getName())) {
				return true;
			}
		}
		return false;
	}

}
