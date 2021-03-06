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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.AccessPermission;

public class AccessRuleList
// Don't try converting to generics, too much pain
    extends ArrayList {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	private static final long serialVersionUID = 1L;

	/**
	 * YECK : Override ArrayList just to keep contents ordered. THis does not
	 * override all ArrayList add methods, so it isn't fool proof!!!
	 */
	@Override
	public boolean add(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof AccessRule)) {
			return false;
		}
		AccessRule rule = (AccessRule) obj;

		try {
			if (rule.getPermission().equals(AccessPermission.DENY)) {
				add(0, rule);
			}
			else if (rule.getPermission().equals(AccessPermission.GRANT)) {
				add(this.size(), rule);
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
			return false;
		}

		return true;
	}
}
