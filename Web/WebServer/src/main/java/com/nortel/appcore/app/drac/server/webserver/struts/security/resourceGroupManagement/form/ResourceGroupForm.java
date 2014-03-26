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

package com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.common.security.policy.types.AbstractRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessStateRule;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfileXML.ResourceAccessStateRule;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.AccessPermission;
import com.nortel.appcore.app.drac.common.utility.StringParser;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.security.form.RuleForm;

/**
 * Created on 19-Oct-06
 */
public final class ResourceGroupForm extends ValidatorForm implements
    Comparable<ResourceGroupForm> {
	public static AccessStateRule[] states = {
	    new AccessStateRule(ResourceAccessStateRule.OPEN),
	    new AccessStateRule(ResourceAccessStateRule.CLOSED),
	    new AccessStateRule(ResourceAccessStateRule.DISABLED) };
	private static final long serialVersionUID = 6244654578864926954L;
	private String name = DracConstants.EMPTY_STRING;
	// URL safe...for input
	private String webSafeName = DracConstants.EMPTY_STRING;
	// HTML safe...for output on browser
	// private String clientSafeName = DracConstants.EMPTY_STRING;
	private String creationDate = DracConstants.EMPTY_STRING;
	private String lastModifiedDate = DracConstants.EMPTY_STRING;
	private String creationTime = DracConstants.EMPTY_STRING;
	private String lastModifiedTime = DracConstants.EMPTY_STRING;
	private String lastModificationUserID = DracConstants.EMPTY_STRING;
	private Boolean defaultResourceGroup = Boolean.TRUE;
	private String parentResourceGroup = DracConstants.EMPTY_STRING;
	private ArrayList<Resource> resources = new ArrayList<Resource>();
	private String[] availableTNAs = {}; // returns the selected resources
	private String[] memberTNAs = {}; // returns the selected (member) resources
	private String[] availableUserGroups = {};
	private String[] userGroupMembership = {};
	private List<ResourceGroupForm> childGroups = new ArrayList<ResourceGroupForm>();

	private List<List<String>> memberUserGroupNameTree = new ArrayList<List<String>>();

	// Resource Group Policy fields
	private int resourceAccessState = 0;
	private String[] resourceSystemAccessRules = {};
	private RuleForm[] resRules = {};

	private ArrayList<AbstractRule> accessRule = new ArrayList<AbstractRule>();
	private AbstractRule stateRule = new AccessStateRule(
	    ResourceAccessStateRule.OPEN);
	private ArrayList<AccessPermission> accessRulePermissions = new ArrayList<AccessPermission>();
	private String maximumServiceSize = "";
	private String maximumServiceDuration = "";
	private String maximumServiceBandwidth = "";
	private String maximumAggregateServiceSize = "";

	private String error = "";

	@Override
	public int compareTo(ResourceGroupForm rgForm) {
		if (rgForm != null) {
			return this.getName().compareTo(rgForm.getName());
		}
		return 0;
	}

	public ArrayList<AbstractRule> getAccessRule() {
		return accessRule;
	}

	public ArrayList<AccessPermission> getAccessRulePermissions() {
		return accessRulePermissions;
	}

	/**
	 * @return Returns the availableTNAs.
	 */
	public String[] getAvailableTNAs() {
		return availableTNAs;
	}

	/**
	 * @return the availableUserGroups
	 */
	public String[] getAvailableUserGroups() {
		return availableUserGroups;
	}

	public List<ResourceGroupForm> getChildGroups() {
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
	 * @return the defaultResourceGroup
	 */
	public Boolean getDefaultResourceGroup() {
		return defaultResourceGroup;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
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
	 * @return Returns the memberTNAs.
	 */
	public String[] getMemberTNAs() {
	  final List<String> tnas = Arrays.asList(memberTNAs);
	  Collections.sort(tnas);
		return new HashSet<String>(tnas).toArray(new String[]{});
	}

	public List<List<String>> getMemberUserGroupNameTree() {
		return memberUserGroupNameTree;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the parentResourceGroup
	 */
	public String getParentResourceGroup() {
		return parentResourceGroup;
	}

	/**
	 * @return the resourceAccessState
	 */
	public int getResourceAccessState() {
		return resourceAccessState;
	}

	/**
	 * @return the resources
	 */
	public ArrayList<Resource> getResourceList() {
		return resources;
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

	public AbstractRule getStateRule() {
		return stateRule;
	}

	/**
	 * @return the userGroupMembership
	 */
	public String[] getUserGroupMembership() {
		return userGroupMembership;
	}

	public String getWebSafeName() {
		return this.webSafeName;
	}

	public void setAccessRule(ArrayList<AbstractRule> accessRule) {
		this.accessRule = accessRule;
	}

	public void setAccessRulePermissions(
	    ArrayList<AccessPermission> accessRulePermissions) {
		this.accessRulePermissions = accessRulePermissions;
	}

	/**
	 * @param availableTNAs
	 */
	public void setAvailableTNAs(String[] availableTNAs) {
		this.availableTNAs = availableTNAs;
	}

	/**
	 * @param availableUserGroups
	 *          the availableUserGroups to set
	 */
	public void setAvailableUserGroups(String[] availableUserGroups) {
		this.availableUserGroups = availableUserGroups;
	}

	public void setChildGroups(List<ResourceGroupForm> childGroups) {
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
	 * @param defaultResourceGroup
	 *          the defaultResourceGroup to set
	 */
	public void setDefaultResourceGroup(Boolean defaultResourceGroup) {
		this.defaultResourceGroup = defaultResourceGroup;
	}

	/**
	 * @param error
	 *          the error to set
	 */
	public void setError(String error) {
		this.error = error;
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
	 * @param memberTNAs
	 */
	public void setMemberTNAs(String[] memberTNAs) {
		this.memberTNAs = memberTNAs;
	}

	public void setMemberUserGroupNameTree(
	    List<List<String>> memberUserGroupNameTree) {
		this.memberUserGroupNameTree = memberUserGroupNameTree;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param parentResourceGroup
	 *          the parentResourceGroup to set
	 */
	public void setParentResourceGroup(String parentResourceGroup) {
		this.parentResourceGroup = parentResourceGroup;
	}

	/**
	 * @param resourceAccessState
	 *          the resourceAccessState to set
	 */
	public void setResourceAccessState(int resourceAccessState) {
		this.resourceAccessState = resourceAccessState;
	}

	/**
	 * @param resources
	 *          the resources to set
	 */
	public void setResourceList(ArrayList<Resource> resources) {
		this.resources = resources;
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

	public void setStateRule(AbstractRule stateRule) {
		this.stateRule = stateRule;
	}

	/**
	 * @param userGroupMembership
	 *          the userGroupMembership to set
	 */
	public void setUserGroupMembership(String[] userGroupMembership) {
		this.userGroupMembership = userGroupMembership;
	}

	public void setWebSafeName(String webSafeName) throws Exception {
		this.webSafeName = DracHelper.encodeToUTF8(webSafeName);
	}
}
