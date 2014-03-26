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

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.XmlUtility;

/**
 * @author vnguyen
 */

public final class UserGroupProfileXML {
  private static final Logger log = LoggerFactory.getLogger(UserGroupProfileXML.class);

	public static enum AccessPermission {
		GRANT(0, "grant"), DENY(1, "deny");

		private static final long serialVersionUID = 1;
		public static final String UGP_SAR_RULE_ELEMENT = "rule";
		private final int code;
		private final String description;

		AccessPermission(int accessPermissionCode,
		    String accessPermissionDescription) {
			code = accessPermissionCode;
			description = accessPermissionDescription;
		}

		public static AccessPermission fromString(String permissionStr)
		    throws Exception {
			if (permissionStr != null && !permissionStr.equals("")) {
				if (permissionStr.equals(GRANT.description)) {
					return GRANT;
				}

				if (permissionStr.equals(DENY.description)) {
					return DENY;
				}

				throw new Exception("Cannot convert Access Permission '"
				    + permissionStr + "' into enum");
			}
			return null;
		}

		public static AccessPermission fromXMLToObject(Element root)
		    throws Exception {
			return fromString(root.getChildText(UGP_SAR_RULE_ELEMENT));
		}

		public boolean isGranted() {
			return code == GRANT.code;
		}

		@Override
		public String toString() {
			return description;
		}

		public String toXMLString() {
			return XmlUtility.toXMLString(UGP_SAR_RULE_ELEMENT, description);
		}
	}

	// Global declaration for UserGroup entity
	// Sample of a usergroup
	/*
	 * <userGroupList> <userGroup creationDate="2000-08-02T12:12:12"
	 * lastModificationUserID="bob" lastModifiedDate="2000-08-02T12:12:13"
	 * name="bobGroup"> <userGroupType>User</userGroupType>
	 * <defaultUserGroup>false</defaultUserGroup> <information>1</information>
	 * <groupPolicy> <accessControlRule> <key>tada</key> <value>1,2,3</value>
	 * </accessControlRule> <bandwidthControlRule>
	 * <maximumServiceSize>1</maximumServiceSize>
	 * <maximumServiceDuration>1000</maximumServiceDuration>
	 * <maximumServiceBandwidth>1000</maximumServiceBandwidth>
	 * <maximumAggregateServiceSize>100</maximumAggregateServiceSize>
	 * </bandwidthControlRule> </groupPolicy> <membership>1</membership>
	 * </userGroup>
	 */

	public static enum UserGroupType

	{
		USER("User"), SYSTEM_ADMIN("SystemAdministrator"), GROUP_ADMIN(
		    "GroupAdministrator");

		public static final String UG_TYPE = "userGroupType";
		private static final long serialVersionUID = 1;
		private final String description;

		UserGroupType(String userGroupTypeDescription) {
			description = userGroupTypeDescription;
		}

		public static UserGroupType fromString(String userGroupTypeStr)
		    throws Exception {
			UserGroupType userGroupType = null;
			if (userGroupTypeStr != null && !userGroupTypeStr.equals("")) {
				if (userGroupTypeStr.equals(USER.description)) {
					userGroupType = USER;
				}
				else if (userGroupTypeStr.equals(SYSTEM_ADMIN.description)) {
					userGroupType = SYSTEM_ADMIN;
				}
				else if (userGroupTypeStr.equals(GROUP_ADMIN.description)) {
					userGroupType = GROUP_ADMIN;
				}
				else {
					log.error("Invalid userGroupType: " + userGroupTypeStr);
				}
			}
			return userGroupType;
		}

		public static UserGroupType fromXMLToObject(Element root) throws Exception {
			return fromString(root.getChildText(UG_TYPE));
		}

		@Override
		public String toString() {
			return description;
		}

		public String toXMLString() {
			return XmlUtility.toXMLString(UG_TYPE, description);
		}
	}

	private UserGroupProfileXML() {
		super();
	}
}
