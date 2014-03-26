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

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckablePolicy;

public final class GlobalPolicy implements Serializable, PolicyCheckablePolicy {
	private static final long serialVersionUID = 1L;
	public static final String GLOBALPOLICY_ELEMENT = "globalPolicy";
	// public static final String DEFAULT_USERGROUP_POLICY_ELEMENT =
	// "defaultUserGroupPolicy";
	// public static final String DEFAULT_RESOURCE_GROUPPOLICY_ELEMENT =
	// "defaultResourceGroupPolicy";

	private GroupPolicy userGroupPolicy;
	private ResourcePolicy resourceGroupPolicy;
	private UserAccountPolicy localAccountPolicy;
	private GlobalAuthentication supportedAuthentication;

	// Server settings: as managed from the WUI - General - Server Settings
	public static final String PRERESERVATIONCONFIRMATIONTIMEOUT_ELEMENT = "preReservationConfirmationTimeout";
	public static final int PRERESERVATIONCONFIRMATIONTIMEOUT_DEFAULT = 10;
	private int preReservationConfirmationTimeout = PRERESERVATIONCONFIRMATIONTIMEOUT_DEFAULT;

	public static final String SCHEDULEPROVISIONINGOFFSET_ELEMENT = "scheduleProvisioningOffset";
	public static final int SCHEDULEPROVISIONINGOFFSET_DEFAULT = 10;
	private int scheduleProvisioningOffset = SCHEDULEPROVISIONINGOFFSET_DEFAULT;

	public GlobalPolicy() {
		this(new GroupPolicy(), new ResourcePolicy(), new UserAccountPolicy(),
		    new GlobalAuthentication());
	}

	public GlobalPolicy(GroupPolicy userGroupPolicy,
	    ResourcePolicy resourceGroupPolicy, UserAccountPolicy userAccountPolicy,
	    GlobalAuthentication authentication) {
		super();

		this.userGroupPolicy = userGroupPolicy;
		if (this.userGroupPolicy == null) {
			this.userGroupPolicy = new GroupPolicy();
		}

		this.resourceGroupPolicy = resourceGroupPolicy;
		if (this.resourceGroupPolicy == null) {
			this.resourceGroupPolicy = new ResourcePolicy();
		}

		this.localAccountPolicy = userAccountPolicy;
		if (this.localAccountPolicy == null) {
			this.localAccountPolicy = new UserAccountPolicy();
		}

		this.supportedAuthentication = authentication;
		if (this.supportedAuthentication == null) {
			this.supportedAuthentication = new GlobalAuthentication();
		}
	}

	public void fromXML(Element root) throws Exception {

		/** ****************************************************************** */
		/* Constructs GlobalPolicy object. */
		/** ****************************************************************** */

		UserAccountPolicy userAccountPolicy = new UserAccountPolicy();
		userAccountPolicy.fromXML(root);
		this.localAccountPolicy = userAccountPolicy;

		GroupPolicy userGroupPolicy = new GroupPolicy();
		userGroupPolicy.fromXML(root);
		this.userGroupPolicy = userGroupPolicy;

		ResourcePolicy resourceGroupPolicy = new ResourcePolicy();
		resourceGroupPolicy.fromXML(root);
		this.resourceGroupPolicy = resourceGroupPolicy;

		GlobalAuthentication authen = new GlobalAuthentication();
		authen.fromXML(root);
		this.supportedAuthentication = authen;


		// Server Settings
		{
			Element preReservationConfirmationTimeoutElement = root
			    .getChild(GlobalPolicy.PRERESERVATIONCONFIRMATIONTIMEOUT_ELEMENT);
			if (preReservationConfirmationTimeoutElement != null) {
				this.preReservationConfirmationTimeout = Integer
				    .parseInt(preReservationConfirmationTimeoutElement.getText());
			}

			Element scheduleProvisioningOffsetElement = root
			    .getChild(GlobalPolicy.SCHEDULEPROVISIONINGOFFSET_ELEMENT);
			if (scheduleProvisioningOffsetElement != null) {
				this.scheduleProvisioningOffset = Integer
				    .parseInt(scheduleProvisioningOffsetElement.getText());
			}
		}
	}

	public UserAccountPolicy getLocalAccountPolicy() {
		return localAccountPolicy;
	}

	public int getPreReservationConfirmationTimeout() {
		return preReservationConfirmationTimeout;
	}

	public ResourcePolicy getResourceGroupPolicy() {
		return resourceGroupPolicy;
	}

	public int getScheduleProvisioningOffset() {
		return scheduleProvisioningOffset;
	}

	public GlobalAuthentication getSupportedAuthenticationData() {
		return supportedAuthentication;
	}

	public GroupPolicy getUserGroupPolicy() {
		return userGroupPolicy;
	}

	// public void setLocalAccountPolicy(UserAccountPolicy localAccountPolicy)
	// {
	// this.localAccountPolicy = localAccountPolicy;
	// }
	//
	// public void setResourceGroupPolicy(ResourcePolicy resourceGroupPolicy)
	// {
	// this.resourceGroupPolicy = resourceGroupPolicy;
	// }

	// public void setSupportedAuthenticationData(GlobalAuthentication
	// supportedAuthentication)
	// {
	// this.supportedAuthentication = supportedAuthentication;
	// }

	// public void setUserGroupPolicy(GroupPolicy userGroupPolicy)
	// {
	// this.userGroupPolicy = userGroupPolicy;
	// }

	public void setPreReservationConfirmationTimeout(int timeout) {
		this.preReservationConfirmationTimeout = timeout;
	}

	public void setScheduleProvisioningOffset(int offset) {
		this.scheduleProvisioningOffset = offset;
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder();
		buf.append("<");
		buf.append(GLOBALPOLICY_ELEMENT);
		buf.append(">");

		if (this.localAccountPolicy != null) {
			buf.append("\n");
			buf.append(this.localAccountPolicy.toXMLString());
		}

		if (this.userGroupPolicy != null) {
			buf.append("\n");
			buf.append(this.userGroupPolicy.toXMLString());
		}

		if (this.resourceGroupPolicy != null) {
			buf.append("\n");
			buf.append(this.resourceGroupPolicy.toXMLString());
		}

		if (this.supportedAuthentication != null) {
			buf.append("\n");
			buf.append(this.supportedAuthentication.toXMLString());
		}

		// Server settings
		{
			buf.append("\n");
			buf.append("<");
			buf.append(GlobalPolicy.PRERESERVATIONCONFIRMATIONTIMEOUT_ELEMENT);
			buf.append(">");
			buf.append(this.preReservationConfirmationTimeout);
			buf.append("</");
			buf.append(GlobalPolicy.PRERESERVATIONCONFIRMATIONTIMEOUT_ELEMENT);
			buf.append(">");

			buf.append("\n");
			buf.append("<");
			buf.append(GlobalPolicy.SCHEDULEPROVISIONINGOFFSET_ELEMENT);
			buf.append(">");
			buf.append(this.scheduleProvisioningOffset);
			buf.append("</");
			buf.append(GlobalPolicy.SCHEDULEPROVISIONINGOFFSET_ELEMENT);
			buf.append(">");
		}

		buf.append("\n");
		buf.append("</");
		buf.append(GLOBALPOLICY_ELEMENT);
		buf.append(">");
		return buf.toString();
	}
}
