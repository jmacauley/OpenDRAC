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

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;

/**
 * Created on Dec 5, 2005
 *
 * @author nguyentd
 */
public final class UserType implements Serializable {
	private static final long serialVersionUID = 1;

	private String userId = "unknown";
	private UserGroupName billingGroup;
	private String sourceEndpointUserGroup;
	private String targetEndpointUserGroup;
	private String sourceEndpointResourceGroup;
	private String targetEndpointResourceGroup;
	private String emailAddress;

	public UserType() {
		super();
	}

	public UserType(String id, UserGroupName billing, String srcEndptUserGroup,
	    String tgtEndPtUserGroup, String srcEndptResGroup,
	    String tgtEndPtResGroup, String email) {
		userId = id;
		billingGroup = billing;
		sourceEndpointUserGroup = srcEndptUserGroup;
		targetEndpointUserGroup = tgtEndPtUserGroup;
		sourceEndpointResourceGroup = srcEndptResGroup;
		targetEndpointResourceGroup = tgtEndPtResGroup;
		emailAddress = email;
	}

	/**
	 * @return the billingGroup
	 */
	public UserGroupName getBillingGroup() {
		return billingGroup;
	}

	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @return the sourceEndpointResourceGroup
	 */
	public String getSourceEndpointResourceGroup() {
		return sourceEndpointResourceGroup;
	}

	/**
	 * @return the sourceEndpointUserGroup
	 */
	public String getSourceEndpointUserGroup() {
		return sourceEndpointUserGroup;
	}

	/**
	 * @return the targetEndpointResourceGroup
	 */

	public String getTargetEndpointResourceGroup() {
		return targetEndpointResourceGroup;
	}

	/**
	 * @return the targetEndpointUserGroup
	 */
	public String getTargetEndpointUserGroup() {
		return targetEndpointUserGroup;
	}

	/**
	 * @return the userId
	 */

	public String getUserId() {
		return this.userId;
	}

	public boolean isEmpty() {
		return !(userId != null && userId.equals("unknown") && userId.equals("")
		    && billingGroup != null && billingGroup.toString().equals("")
		    && sourceEndpointUserGroup != null
		    && sourceEndpointUserGroup.equals("")
		    && sourceEndpointResourceGroup != null
		    && sourceEndpointResourceGroup.equals("")
		    && targetEndpointUserGroup != null
		    && targetEndpointUserGroup.equals("")
		    && targetEndpointResourceGroup != null && targetEndpointResourceGroup
		    .equals(""));
	}

	/**
	 * @param billingGroup
	 *          the billingGroup to set
	 */
	public void setBillingGroup(UserGroupName billingGroup) {
		this.billingGroup = billingGroup;
	}

	/**
	 * @param emailAddress
	 *          the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * @param sourceEndpointResourceGroup
	 *          the sourceEndpointResourceGroup to set
	 */
	public void setSourceEndpointResourceGroup(String sourceEndpointResourceGroup) {
		this.sourceEndpointResourceGroup = sourceEndpointResourceGroup;
	}

	/**
	 * @param sourceEndpointUserGroup
	 *          the sourceEndpointUserGroup to set
	 */
	public void setSourceEndpointUserGroup(String sourceEndpointUserGroup) {
		this.sourceEndpointUserGroup = sourceEndpointUserGroup;
	}

	/**
	 * @param targetEndpointResourceGroup
	 *          the targetEndpointResourceGroup to set
	 */
	public void setTargetEndpointResourceGroup(String targetEndpointResourceGroup) {
		this.targetEndpointResourceGroup = targetEndpointResourceGroup;
	}

	/**
	 * @param targetEndpointUserGroup
	 *          the targetEndpointUserGroup to set
	 */
	public void setTargetEndpointUserGroup(String targetEndpointUserGroup) {
		this.targetEndpointUserGroup = targetEndpointUserGroup;
	}

	public void setUserId(String id) {
		this.userId = id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserInfoType [userId=");
		builder.append(userId);
		builder.append(", billingGroup=");
		builder.append(billingGroup);
		builder.append(", sourceEndpointUserGroup=");
		builder.append(sourceEndpointUserGroup);
		builder.append(", targetEndpointUserGroup=");
		builder.append(targetEndpointUserGroup);
		builder.append(", sourceEndpointResourceGroup=");
		builder.append(sourceEndpointResourceGroup);
		builder.append(", targetEndpointResourceGroup=");
		builder.append(targetEndpointResourceGroup);
		builder.append(", emailAddress=");
		builder.append(emailAddress);
		builder.append("]");
		return builder.toString();
	}

}
