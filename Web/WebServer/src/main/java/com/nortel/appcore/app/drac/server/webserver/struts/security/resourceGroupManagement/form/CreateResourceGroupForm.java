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

import org.apache.struts.validator.ValidatorActionForm;

import com.nortel.appcore.app.drac.common.security.policy.types.AccessStateRule;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfileXML.ResourceAccessStateRule;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.security.form.RuleForm;

/**
 * Created on 27-Oct-2006
 */
public final class CreateResourceGroupForm extends ValidatorActionForm {
	public static AccessStateRule[] states = {
	    new AccessStateRule(ResourceAccessStateRule.OPEN),
	    new AccessStateRule(ResourceAccessStateRule.CLOSED),
	    new AccessStateRule(ResourceAccessStateRule.DISABLED) };
	private static final long serialVersionUID = 3246339126949878762L;
	// Fields pertaining to create resource group page
	private String name = DracConstants.EMPTY_STRING;
	private boolean defaultResourceGroup;
	private String[] parentResourceGroups = {};
	private String parentResourceGroup = DracConstants.EMPTY_STRING;
	private String[] availableTNAs = {};
	private String[] memberTNAs = {}; // returns the selected (member) resources
	private String[] availableUserGroups = {}; // holds the available User Groups

	private String[] referencedUserGroups = {}; // returns the User Groups being
	                                            // referenced by this RG

	// Field just checks if form has data, will be set by webpage
	private String redirected = "";

	private int inactivityPeriod;

	// Resource Group Policy fields
	private int resourceAccessState;
	private String[] resourceSystemAccessRules = {};
	private RuleForm[] resRules = {};
	private String resourceMaxServiceSize = "";
	private String resourceMaxServiceDuration = "";
	private String resourceMaxServiceBandwidth = "";
	private String resourceMaxAggregateServiceSize = "";

	/**
	 * @return Returns the availableTNAs.
	 */
	public String[] getAvailableTNAs() {
		return availableTNAs;
	}

	/**
	 * @return Returns the availableUserGroups.
	 */
	public String[] getAvailableUserGroups() {
		return availableUserGroups;
	}

	/**
	 * @return the inactivityPeriod
	 */
	public int getInactivityPeriod() {
		return inactivityPeriod;
	}

	/**
	 * @return Returns the memberTNAs.
	 */
	public String[] getMemberTNAs() {
		return memberTNAs;
	}

	/**
	 * @return Returns the name.
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
	 * @return the parentResourceGroups
	 */
	public String[] getParentResourceGroups() {
		return parentResourceGroups;
	}

	/**
	 * @return the redirected
	 */
	public String getRedirected() {
		return redirected;
	}

	/**
	 * @return Returns the referencedUserGroups.
	 */
	public String[] getReferencedUserGroups() {
		return referencedUserGroups;
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
	 * @return Returns the defaultResourceGroup
	 */
	public boolean isDefaultResourceGroup() {
		return defaultResourceGroup;
	}

	/**
     *
     */
	public void setAvailableTNAs(String[] availableTNAs) {
		this.availableTNAs = availableTNAs;
	}

	/**
	 * @param availableUserGroups
	 *          The availableUserGroups to set.
	 */
	public void setAvailableUserGroups(String[] availableUserGroups) {
		this.availableUserGroups = availableUserGroups;
	}

	/**
	 * @param defaultResourceGroup
	 *          The defaultResourceGroup to set
	 */
	public void setDefaultResourceGroup(boolean defaultResourceGroup) {
		this.defaultResourceGroup = defaultResourceGroup;
	}

	/**
	 * @param inactivityPeriod
	 *          the inactivityPeriod to set
	 */
	public void setInactivityPeriod(int inactivityPeriod) {
		this.inactivityPeriod = inactivityPeriod;
	}

	/**
	 * @param memberTNAs
	 */
	public void setMemberTNAs(String[] memberTNAs) {
		this.memberTNAs = memberTNAs;
	}

	/**
	 * @param name
	 *          The name to set.
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
	 * @param parentResourceGroups
	 *          the parentResourceGroups to set
	 */
	public void setParentResourceGroups(String[] parentResourceGroups) {
		this.parentResourceGroups = parentResourceGroups;
	}

	/**
	 * @param redirected
	 *          the redirected to set
	 */
	public void setRedirected(String redirected) {
		this.redirected = redirected;
	}

	/**
	 * @param referencedUserGroups
	 *          The referencedUserGroups to set.
	 */
	public void setReferencedUserGroups(String[] referencedUserGroups) {
		this.referencedUserGroups = referencedUserGroups;
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

}
