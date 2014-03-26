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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts.validator.ValidatorActionForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on 31-Oct-2006
 */
public final class CreateUserGroupForm extends ValidatorActionForm {
	private static final long serialVersionUID = 4718811189355068801L;
	// Fields pertaining to create user group page.
	private String name = DracConstants.EMPTY_STRING;
	private int userGroupType = 0;
	private boolean defaultUserGroup = false;
	private String parentUserGroup = DracConstants.EMPTY_STRING;

	private String[] userMembership = {};
	private String[] availableUsers = {};
	private String[] availableUserGroups = {};
	private String[] resourceGroupMembership = {};
	private String[] availableResourceGroups = {};

	// UserGroupPolicy fields
	private String[] groupAccessControlRules = {};
	private String[] groupSystemAccessRules = {};
	private String groupMaxServiceSize = "";
	private String groupMaxServiceDuration = "";
	private String groupMaxServiceBandwidth = "";
	private String groupMaxAggregateServiceSize = "";

	/**
	 * @return String[] availableResourceGroups
	 */
	public String[] getAvailableResourceGroups() {
		return availableResourceGroups;
	}

	/**
	 * @return String[] availableUserGroups
	 */
	public String[] getAvailableUserGroups() {
		return availableUserGroups;
	}

	/**
	 * @return String[] availableUsers
	 */
	public String[] getAvailableUsers() {
		return availableUsers;
	}

	/**
	 * @return String[] groupAccessControlRules
	 */
	public String[] getGroupAccessControlRules() {
		return groupAccessControlRules;
	}

	/**
	 * @return String groupMaxAggregateServiceSize
	 */
	public String getGroupMaxAggregateServiceSize() {
		return groupMaxAggregateServiceSize;
	}

	/**
	 * @return String groupMaxServiceBandwidth
	 */
	public String getGroupMaxServiceBandwidth() {
		return groupMaxServiceBandwidth;
	}

	/**
	 * @return String groupMaxServiceDuration
	 */
	public String getGroupMaxServiceDuration() {
		return groupMaxServiceDuration;
	}

	/**
	 * @return String groupMaxServiceSize
	 */
	public String getGroupMaxServiceSize() {
		return groupMaxServiceSize;
	}

	/**
	 * @return String[] groupSystemAccessRules
	 */
	public String[] getGroupSystemAccessRules() {
		return groupSystemAccessRules;
	}

	/**
	 * @return String name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return String parentUserGroup
	 */
	public String getParentUserGroup() {
		return parentUserGroup;
	}

	/**
	 * @return String[] resourceGroupMembership
	 */
	public String[] getResourceGroupMembership() {
		return resourceGroupMembership;
	}

	/**
	 * @return int userGroupType
	 */
	public int getUserGroupType() {
		return userGroupType;
	}

	/**
	 * @return String[] userMembership
	 */
	public String[] getUserMembership() {
		return userMembership;
	}

	/**
	 * @return boolean defaultUserGroup
	 */
	public boolean isDefaultUserGroup() {
		return defaultUserGroup;
	}

	/**
	 * @param availableResourceGroups
	 *          the availableResourceGroups to set
	 */
	public void setAvailableResourceGroups(String[] availableResourceGroups) {
		Set<String> resourceGroups = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		for (String resourceGroup : availableResourceGroups) {
			if(resourceGroup!=null){
				resourceGroups.add(resourceGroup);
			}
		}
		this.availableResourceGroups = (String[]) resourceGroups.toArray(new String [0]);		
	}

	/**
	 * @param String
	 *          [] availableUserGroups the availableUserGroups to set
	 */
	public void setAvailableUserGroups(String[] availableUserGroups) {
		Set<String> userGroups = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		for (String userGroup : availableUserGroups) {
			if(userGroup!=null){
				userGroups.add(userGroup);
			}
		}
		this.availableUserGroups = (String[]) userGroups.toArray(new String [0]);
	}

	/**
	 * @param String
	 *          [] availableUsers - The availableUsers to set.
	 */
	public void setAvailableUsers(String[] availableUsers) {
		
		Set<String> users = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		for (String user : availableUsers) {
			if(user!=null){
				users.add(user);
			}
		}
		this.availableUsers = (String[]) users.toArray(new String [0]);	
	}

	/**
	 * @param boolean defaultUserGroup - The defaultUserGroup to set.
	 */
	public void setDefaultUserGroup(boolean defaultUserGroup) {
		this.defaultUserGroup = defaultUserGroup;
	}

	/**
	 * @param String
	 *          [] groupAccessControlRules - the groupAccessControlRules to set
	 */
	public void setGroupAccessControlRules(String[] groupAccessControlRules) {
		
		Set<String> controlRules = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		for (String controlRule : groupAccessControlRules) {
			if(controlRule!=null){
				controlRules.add(controlRule);
			}
		}
		this.groupAccessControlRules = (String[]) controlRules.toArray(new String [0]);
	}

	/**
	 * @param String
	 *          groupMaxAggregateServiceSize - the groupMaxAggregateServiceSize to
	 *          set
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
	 * @param groupSystemAccessRules
	 *          the groupSystemAccessRules to set
	 */
	public void setGroupSystemAccessRules(String[] groupSystemAccessRules) {
		this.groupSystemAccessRules = groupSystemAccessRules;
	}

	/**
	 * @param String
	 *          name - The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param String
	 *          parentUserGroup - The parentUserGroup to set.
	 */
	public void setParentUserGroup(String parentUserGroup) {
		this.parentUserGroup = parentUserGroup;
	}

	/**
	 * @param String
	 *          [] resourceGroupMembership the resourceGroupMembership to set
	 */
	public void setResourceGroupMembership(String[] resourceGroupMembership) {
		Arrays.sort(resourceGroupMembership, String.CASE_INSENSITIVE_ORDER);
		this.resourceGroupMembership = resourceGroupMembership;	
	}

	/**
	 * @param int userGroupType - The userGroupType to set.
	 */
	public void setUserGroupType(int userGroupType) {
		this.userGroupType = userGroupType;
	}

	/**
	 * @param String
	 *            [] userMembership - The userMembership to set.
	 */
	public void setUserMembership(String[] userMembership) {
		Set<String> memberShips = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		for (String membership : userMembership) {
			if(membership!=null){
				memberShips.add(membership);
			}
		}
		this.userMembership = memberShips.toArray(new String[]{});
	}
}
