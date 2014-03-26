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

package com.nortel.appcore.app.drac.database.helper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPolicyProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbGlobalPolicy;
import com.nortel.appcore.app.drac.database.dracdb.DbResourceGroupProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbUser;
import com.nortel.appcore.app.drac.database.dracdb.DbUserGroupProfile;

/**
 * @author pitman
 */
public enum UserPolicyUtility {
	INSTANCE;
	private static final Logger log = LoggerFactory.getLogger(UserPolicyUtility.class);

	@SuppressWarnings("unchecked")
  public Set<String>[] diffMembershipLists(Set<String> currentList,
	    Set<String> newList) {
		TreeSet<String>[] result = (TreeSet<String>[]) Array.newInstance(
		    TreeSet.class, 2);
		result[0] = new TreeSet<String>();
		result[1] = new TreeSet<String>();
		TreeSet<String> appendList = result[0];
		TreeSet<String> removedList = result[1];

		if (newList != null) {
			for (String reference : newList) {
				if (currentList == null || !currentList.contains(reference)) {
					// it must be new
					appendList.add(reference);
				}
			}
		}

		if (currentList != null) {
			for (String reference : currentList) {
				if (newList == null || !newList.contains(reference)) {
					// it must be old
					removedList.add(reference);
				}
			}
		}

		log.debug("Computed difference between " + currentList + " and " + newList
		    + " as new elements " + appendList + " and old elements " + removedList);
		return result;
	}

	public List<Set<UserGroupName>> diffUserGroupLists(
	    Set<UserGroupName> currentList, Set<UserGroupName> newList) {

		TreeSet<UserGroupName> appendList = new TreeSet<UserGroupName>();
		TreeSet<UserGroupName> removedList = new TreeSet<UserGroupName>();

		if (newList != null) {
			for (UserGroupName reference : newList) {
				if (currentList == null || !currentList.contains(reference)) {
					// it must be new
					appendList.add(reference);
				}
			}
		}

		if (currentList != null) {
			for (UserGroupName reference : currentList) {
				if (newList == null || !newList.contains(reference)) {
					// it must be old
					removedList.add(reference);
				}
			}
		}

		log.debug("Computed difference between " + currentList + " and " + newList
		    + " as new elements " + appendList + " and old elements " + removedList);

		List<Set<UserGroupName>> results = new ArrayList<Set<UserGroupName>>();
		results.add(appendList);
		results.add(removedList);
		return results;
	}

    /*
     * Called in a non-static fashion so we insure the DbUser is created and up
     * before we go and that all access is via the getInstance method to make
     * searching easier
     */
    private static final Object LOCK = DbUser.INSTANCE.getLock();

	/**
	 * This method loads a user, its user groups and global policies to search for
	 * the relevant profile data and does not belong in any one place
	 */
	public UserPolicyProfile getUserPolicyProfile(String userID) throws Exception {

        try {
            UserProfile userProfile;
            synchronized (LOCK) {
                userProfile = DbUser.INSTANCE.getUserProfile(userID);
            }

            if (userProfile == null) {
                return null;
            }

            GlobalPolicy globalPolicy = DbGlobalPolicy.INSTANCE
                .getGlobalPolicy();

            MembershipData membershipData = userProfile.getMembershipData();
            if (membershipData == null) {
                return new UserPolicyProfile(userProfile, null, null, globalPolicy);
            }

            List<UserGroupProfile> userGroupList = new ArrayList<UserGroupProfile>();
            List<ResourceGroupProfile> resourceGroupList = new ArrayList<ResourceGroupProfile>();

            if (membershipData.getMemberUserGroupName() != null) {
                for (UserGroupName userGroupID : membershipData
                    .getMemberUserGroupName()) {
                    UserGroupProfile userGroupProfile = DbUserGroupProfile.INSTANCE
                        .getUserGroupProfile(userGroupID);

                    if (userGroupProfile == null) {

                        continue;
                    }

                    userGroupList.add(userGroupProfile);
                    if (userGroupProfile.getMembership() != null) {
                        for (String resGroupName : userGroupProfile.getMembership()
                            .getMemberResourceGroupName()) {
                            ResourceGroupProfile profile = DbResourceGroupProfile
                                .INSTANCE.getResourceGroupProfile(resGroupName);
                            if (profile == null) {
                                log.error("cannot retrieve resource group: " + resGroupName);
                            }
                            else {
                                resourceGroupList.add(profile);
                            }
                        }
                    }
                }
            }

            return new UserPolicyProfile(userProfile, userGroupList,
                resourceGroupList, globalPolicy);

        }
        catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
                new Object[] { ex.getMessage() }, ex);
        }
	}

}
