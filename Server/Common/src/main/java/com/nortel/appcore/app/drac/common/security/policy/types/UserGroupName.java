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

/**
 * @author pitman
 */
public final class UserGroupName implements Serializable,
    Comparable<UserGroupName>

{
	private static final long serialVersionUID = 1L;
	private final String userGroup;

	/*
	 * Use this when the user group isn't relevant. Use
	 * RootUserGroupName.SYSTEM_ADMIN_GROUP for the system user
	 */
	public static final UserGroupName USER_GROUP_NOTAPPL = new UserGroupName(
	    "N/A");

	public UserGroupName(String name) {
		if (name == null) {
			// might want to check for empty strings as well?
			throw new NullPointerException(
			    "Cannot construct a UserGroupName with null");
		}
		userGroup = name;
	}

	@Override
	public int compareTo(UserGroupName o) {
		return userGroup.toLowerCase().compareTo(o.userGroup.toLowerCase());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof UserGroupName) {
			return userGroup.equals(((UserGroupName) o).userGroup);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return userGroup.hashCode();
	}

	@Override
	public String toString() {
		return userGroup;
	}
}
