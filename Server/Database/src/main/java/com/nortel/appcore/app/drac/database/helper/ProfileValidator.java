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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbResourceGroupProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbUser;
import com.nortel.appcore.app.drac.database.dracdb.DbUserGroupProfile;

/**
 * @author pitman
 */
public final class ProfileValidator {
  private static final Logger log = LoggerFactory.getLogger(ProfileValidator.class);
	
	private ProfileValidator() {
		super();
	}

	/**
	 * Slurp user profiles, user groups and users into memory and walk between
	 * them to verify that the linkages between the 3 groups are correct.
	 */
	public static String auditUserProfileLinkages() throws Exception {
		List<ResourceGroupProfile> resources = DbResourceGroupProfile.INSTANCE
		    .getResourceGroupProfileList();
		List<UserGroupProfile> groups = DbUserGroupProfile.INSTANCE
		    .getUserGroupProfileList();
		List<UserProfile> users = DbUser.INSTANCE.getUserProfileList();
		return auditUserProfileLinkages(resources, groups, users);
	}

	/**
     * 
     */
	public static String auditUserProfileLinkages(
	    List<ResourceGroupProfile> resources, List<UserGroupProfile> groups,
	    List<UserProfile> users) throws Exception {
		StringBuilder feedback = new StringBuilder();

		feedback.append("1. Scanning: " + users.size() + " users, " + groups.size()
		    + " groups, and " + resources.size() + " resource groups\n");

		Map<String, UserProfile> umap = new TreeMap<String, UserProfile>();
		for (UserProfile u : users) {
			umap.put(u.getUserID(), u);
		}

		Map<UserGroupName, UserGroupProfile> gmap = new TreeMap<UserGroupName, UserGroupProfile>();
		for (UserGroupProfile g : groups) {
			gmap.put(g.getName(), g);
		}

		Map<String, ResourceGroupProfile> rmap = new TreeMap<String, ResourceGroupProfile>();
		for (ResourceGroupProfile r : resources) {
			rmap.put(r.getName(), r);
		}

		/**
		 * Users are members of groups. Make sure all users reference an existing
		 * group
		 */

		for (UserProfile u : umap.values()) {
			if (u.getAuthenticationData().getInternalAccountData().getUserPassword() == null
			    || "".equals(u.getAuthenticationData().getInternalAccountData()
			        .getUserPassword())) {
				feedback.append("2. User: <" + u.getUserID()
				    + "> has a empty or missing password entry\n");
			}

			if (!u.getMembershipData().getMemberResourceGroupName().isEmpty()) {
				feedback.append("3. User: <" + u.getUserID()
				    + "> has a non-empty resource group set, expected empty\n");
			}

			if (!u.getMembershipData().getMemberUserID().isEmpty()) {
				feedback.append("4. User: <" + u.getUserID()
				    + "> has a non-empty memberUserID set, expected empty\n");
			}

			if (u.getMembershipData().getCreatedByGroupName() != null) {
				if (u.getMembershipData().getCreatedByGroupName().trim().length() > 0) {
					feedback.append("5. User: <" + u.getUserID()
					    + "> has a non-empty createdByGroupName set, expected empty\n");
				}
			}
			Set<UserGroupName> userG = u.getMembershipData().getMemberUserGroupName();
			if (userG.isEmpty()) {
				feedback.append("6. User: <" + u.getUserID()
				    + "> does not belong to any group, expected otherwise\n");
			}

			for (UserGroupName s : userG) {
				UserGroupProfile t = gmap.get(s);
				if (t == null) {
					feedback.append("7. User: <" + u.getUserID() + "> belongs to group <"
					    + s + "> however no such group was found!\n");
				}
				else {
					/*
					 * This user says they belong to group 't'. Does group 't' contain
					 * user? If not the linkage is broken
					 */
					if (!t.getMembership().getMemberUserID().contains(u.getUserID())) {
						feedback.append("8. User: <" + u.getUserID()
						    + "> belongs to group <" + s + "> however group <" + s
						    + "> does not list that user as a member, linkage error!\n");
					}
				}
			}
		}

		/**
		 * user Groups have a last modified user, created by member name, member
		 * user ids, member group names, member resource group names and referencing
		 * user group name.
		 */

		for (UserGroupProfile g : gmap.values()) {
			if (umap.get(g.getLastModificationUserID()) == null) {
				feedback.append("9. UserGroup: <" + g.getName()
				    + "> last modificationUserID  user <"
				    + g.getLastModificationUserID() + "> does not exist!\n");
			}

			/*
			 * SystemAdminGroup returns null for getCreatedByGroupName, but a null key
			 * is not tolerated by the get method of TreeMap
			 */
			if (!"SystemAdminGroup".equals(g.getName().toString())) {
				if (g.getMembership().getCreatedByGroupName() == null
				    || gmap.get(new UserGroupName(g.getMembership()
				        .getCreatedByGroupName())) == null) {
					feedback.append("10. UserGroup: <" + g.getName()
					    + "> createdByGroup Name <"
					    + g.getMembership().getCreatedByGroupName()
					    + "> does not exist!\n");
				}
				else {
					// The parent usergroup must list the child in its
					// membership/membergroup entry
					UserGroupProfile parent = gmap.get(new UserGroupName(g
					    .getMembership().getCreatedByGroupName()));

					if (!parent.getMembership().getMemberUserGroupName()
					    .contains(g.getName())) {
						feedback.append("11. UserGroup: <" + g.getName()
						    + "> getCreatedByGroupName Name <" + parent.getName()
						    + "> does not list group <" + g.getName()
						    + "> as a child group!\n");
					}
				}
			}

			for (String s : g.getMembership().getMemberResourceGroupName()) {
				ResourceGroupProfile t = rmap.get(s);
				if (t == null) {
					feedback.append("12. UserGroup: <" + g.getName()
					    + "> getMemberResourceGroupName contains resource <" + s
					    + "> but does not exist!\n");
				}
				else {
					/*
					 * This user group says they have a resource group member 't'. Does
					 * 't' contain this user group? If not the linkage is broken
					 */
					if (!t.getMembership().getMemberUserGroupName().contains(g.getName())) {
						feedback
						    .append("13. UserGroup: <"
						        + g.getName()
						        + "> getMemberResourceGroupName contains resource group <"
						        + s
						        + "> but resource group <"
						        + s
						        + "> does not list that user group as a member, linkage error!\n");
					}
				}
			}

			for (UserGroupName s : g.getMembership().getMemberUserGroupName()) {
				UserGroupProfile t = gmap.get(s);
				if (t == null) {
					feedback.append("14. UserGroup: <" + g.getName()
					    + "> getMemberUserGroupName contains group <" + s
					    + "> but does not exist!\n");
				}
			}

			for (String s : g.getMembership().getMemberUserID()) {
				UserProfile t = umap.get(s);
				if (t == null) {
					feedback.append("15. UserGroup: <" + g.getName()
					    + "> getMemberUserID contains user <" + s
					    + "> but does not exist!\n");
				}
				else {
					/*
					 * This user group says they have a user 't'. Does 't' contain this
					 * user group? If not the linkage is broken
					 */
					if (!t.getMembershipData().getMemberUserGroupName()
					    .contains(g.getName())) {
						feedback
						    .append("16. UserGroup: <"
						        + g.getName()
						        + "> getMemberUserID contains user <"
						        + s
						        + "> but user <"
						        + s
						        + "> does not list that user group as a member, linkage error!\n");
					}
				}
			}

			if (g.getReferencingUserGroupName() != null) {
				if (gmap.get(g.getReferencingUserGroupName()) == null) {
					if (!"SystemAdminGroup".equals(g.getName().toString())) {
						feedback.append("17. UserGroup: <" + g.getName()
						    + "> ReferencingUserGroupName contains group <"
						    + g.getReferencingUserGroupName() + "> but does not exist!\n");
					}
				}
			}
		}

		/**
		 * resource groups have : last modification user, member user group names,
		 * member resource group names, created by group name
		 */

		for (ResourceGroupProfile r : rmap.values()) {

			if (r.getLastModificationUserID() != null
			    && r.getLastModificationUserID().trim().length() > 0) {
				if (umap.get(r.getLastModificationUserID().trim()) == null) {
					feedback.append("18. ResourceGroupProfile: <" + r.getName()
					    + "> lastmodification user Id <" + r.getLastModificationUserID()
					    + "> does not exist!\n");
				}
			}

			String createdBy = r.getMembership().getCreatedByGroupName();
			if (createdBy == null || rmap.get(createdBy) == null) {
				if (!"SystemAdminResourceGroup".equals(r.getName())) {
					feedback
					    .append("19. ResourceGroupProfile: <"
					        + r.getName()
					        + "> was created by <"
					        + createdBy
					        + "> however that user group does not exist, expected otherwise!\n");
				}
			}

			for (String s : r.getMembership().getMemberResourceGroupName()) {
				ResourceGroupProfile t = rmap.get(s);
				if (t == null) {
					feedback.append("20. ResourceGroupProfile: <" + r.getName()
					    + "> has a member resourceGroupName <" + s
					    + ">, but no such resource group exists!\n");
				}

			}

			for (UserGroupName s : r.getMembership().getMemberUserGroupName()) {
				UserGroupProfile t = gmap.get(s);
				if (t == null) {
					feedback.append("21. ResourceGroupProfile: <" + r.getName()
					    + "> has a member userGroupName <" + s
					    + ">, but no such user group exists!\n");
				}
				else {

					/*
					 * This resource group says they have a group member 't'. Does group
					 * 't' contain this resource group? If not the linkage is broken
					 */
					if (!t.getMembership().getMemberResourceGroupName()
					    .contains(r.getName())) {
						feedback
						    .append("22. ResourceGroupProfile: <"
						        + r.getName()
						        + "> has a member userGroupName <"
						        + s
						        + "> however group <"
						        + s
						        + "> does not list that resource group as a member, linkage error!\n");
					}
				}
			}

			// expect empty
			if (!r.getMembership().getMemberUserID().isEmpty()) {
				feedback.append("23. ResourceGroupProfile: <" + r.getName()
				    + "> has a non-empty memberUserId set, expected empty set, not <"
				    + r.getMembership().getMemberUserID() + "> !\n");
			}
		}

		log.debug("auditUserProfileLinkages returning  with feedback \n"
		    + feedback.toString());
		return feedback.toString();
	}
}
