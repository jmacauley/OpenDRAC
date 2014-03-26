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

package com.nortel.appcore.app.drac.database.dracdb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourcePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOpWithResultsI;
import com.nortel.appcore.app.drac.database.DbOperationsManager;
import com.nortel.appcore.app.drac.database.helper.UserPolicyUtility;

/**
 * @author pitman
 */
public enum DbResourceGroupProfile {
	INSTANCE;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private static class Deleter implements DbOpWithResultsI {
		private final String groupID;

		public Deleter(String group) {
			groupID = group;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			statement.setString(1, groupID);
			return statement;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			// not used
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
			// not used
		}
	}

	private static class Reader implements DbOpWithResultsI {
		private final String groupID;
		private final List<ResourceGroupProfile> results = new ArrayList<ResourceGroupProfile>();

		public Reader(String groupName) {
			groupID = groupName;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			if (groupID != null) {
				statement.setString(1, groupID);
			}
			return statement;
		}

		public List<ResourceGroupProfile> getResults() {
			return results;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			while (rs.next()) {
				ResourceGroupProfile u = new ResourceGroupProfile(null, null, null,
				    null, null);
				u.fromXML(XmlUtility.createDocumentRoot(rs.getString(1)));
				results.add(u);
			}
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
			// not used
		}
	}

	private static class Updater implements DbOpWithResultsI {
		private final ResourceGroupProfile p;

		Updater(ResourceGroupProfile userGroupProfile) {
			p = userGroupProfile;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			statement.setString(1, p.toXMLString());
			statement.setString(2, p.getName());
			return statement;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			// not used
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
			// not used
		}

	}

	private static class Writer implements DbOpWithResultsI {
		private final ResourceGroupProfile p;

		Writer(ResourceGroupProfile profile) {
			p = profile;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			statement.setString(1, p.getName());
			statement.setString(2, p.toXMLString());
			return statement;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			// not used
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
		}
	}

	/**
	 * We synchronize user, user group and resource group operations on a single
	 * lock. Why? Some operations end up touching multiple databases to maintain
	 * the linkage between users in a group, users, etc. Having a common lock
	 * avoids runtime deadlocks and prevents against any mischief. It hurts
	 * performance, but user profile data isn't a performance goal.
	 */
	private static final Object LOCK = DbUser.INSTANCE.getLock();

	public static final String DEFAULT_RESOURCE_GROUP = "SystemAdminResourceGroup";

	public static final String ROOT_ID = System.getProperty(
	    "drac.db.policy.resourcegroup.root.id", "SystemAdminResourceGroup");

	public void createResourceGroupProfile(ResourceGroupProfile profile)
	    throws Exception {
		synchronized (LOCK) {
			if (profile == null) {
				log.error("Invalid parm: " + ResourceGroupProfile.RESOURCEGROUP_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { ResourceGroupProfile.RESOURCEGROUP_ELEMENT });
			}

			// check if existed
			if (isResourceGroupProfileExisted(profile.getName())) {
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_EXISTED,
				    new Object[] { profile.getName() });
			}

			// check if createdBy existed
			if (!DEFAULT_RESOURCE_GROUP.equals(profile.getName())) {
				if (profile.getMembership().getCreatedByGroupName() == null) {
					log.error("Existing element: " + UserGroupProfile.NAME_ATTR + " "
					    + profile.getName());
					throw new NrbException(
					    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
					    new Object[] { profile.getName() + "::"
					        + MembershipData.MB_CREATEDBY_GROUPNAME_ELEMENT });
				}

				if (profile.getMembership().getCreatedByGroupName() == null) {
					log.error("createResourceGroupProfile createdBygroup name is null! "
					    + profile.toString());
					throw new Exception(
					    "createResourceGroupProfile createdBygroup name is null! "
					        + profile.toString());
				}

				if (getResourceGroupProfile(profile.getMembership()
				    .getCreatedByGroupName()) == null) {
					log.error("createResourceGroupProfile createdBygroup name <"
					    + profile.getMembership().getCreatedByGroupName()
					    + "> does not exist" + profile.toString());
					throw new Exception(
					    "createResourceGroupProfile createdBygroup name <"
					        + profile.getMembership().getCreatedByGroupName()
					        + "> does not exist " + profile.toString());
				}
			}

			DbOperationsManager.INSTANCE.executeDbOpWithResults(
			    "insert into ResourceGroups (groupId, xml) values(?,?);", new Writer(
			        profile));

			/*
			 * Update references to user groups
			 */
			Set<UserGroupName> userGroupList = profile.getMembership()
			    .getMemberUserGroupName();
			if (userGroupList != null) {
				for (UserGroupName userGroup : userGroupList) {
					UserGroupProfile p = DbUserGroupProfile.INSTANCE
					    .getUserGroupProfile(userGroup);
					if (p != null) {
						if (p.getMembership().getMemberResourceGroupName()
						    .add(profile.getName())) {
							DbUserGroupProfile.INSTANCE.update(p);
						}
					}
				}
			}

			// appending this group to the creator
			// membership/memberResourceGroup
			String groupCreator = profile.getMembership().getCreatedByGroupName();
			if (groupCreator != null) {
				ResourceGroupProfile parent = getResourceGroupProfile(groupCreator);
				if (parent != null) {
					if (parent.getMembership().getMemberResourceGroupName()
					    .add(profile.getName())) {
						update(parent);
					}
				}
			}
		}
	}

	/**
	 * For testing only, will destroy the cross linkage between
	 * users/groups/resources!
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE
		    .executeDbOpWithResults(
		        "delete from ResourceGroups where groupId <> 'SystemAdminResourceGroup';",
		        new DbOpWithResultsAdapter());
	}

	public void deleteResourceGroupProfile(String resourceGroupName)
	    throws Exception {
		synchronized (LOCK) {
			if (resourceGroupName == null) {
				log.error("Invalid parm: " + ResourceGroupProfile.NAME_ATTR);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { ResourceGroupProfile.NAME_ATTR });
			}

			ResourceGroupProfile profile = getResourceGroupProfile(resourceGroupName);
			if (profile == null) {
				// no such groupfro
				return;
			}

			// 

			/* Remove the resource group */
			DbOperationsManager.INSTANCE.executeDbOpWithResults(
			    "delete from ResourceGroups where groupId=?;", new Deleter(
			        resourceGroupName));

			for (UserGroupName groupId : profile.getMembership()
			    .getMemberUserGroupName()) {
				UserGroupProfile p = DbUserGroupProfile.INSTANCE
				    .getUserGroupProfile(groupId);
				if (p == null) {
					log.error("Referenced user group " + groupId
					    + " does not exist, while deleting resource group "
					    + profile.toString());
				}
				else {
					Set<String> ids = p.getMembership().getMemberResourceGroupName();
					if (ids.remove(profile.getName())) {
						DbUserGroupProfile.INSTANCE.update(p);
					}
				}
			}

			/*
			 * we update the resource group creator of this group by removing it from
			 * membership/memberusergroup
			 */
			String groupCreator = profile.getMembership().getCreatedByGroupName();
			if (groupCreator != null) {
				ResourceGroupProfile parent = getResourceGroupProfile(groupCreator);
				if (parent == null) {
					log.error("CreatedByGroup resource group " + groupCreator
					    + " does not exist, while deleting resource group "
					    + profile.toString());
				}
				else {
					if (parent.getMembership().getMemberResourceGroupName()
					    .remove(profile.getName())) {
						update(parent);
					}
				}
			}
		}
	}

	public Resource getEndpointResource(String resourceID) throws Exception {
		synchronized (LOCK) {
			// TODO: Inefficient getEndpointResource invoked, operation is slow
			log.error("inefficient getEndpointResource invoked, operation is slow");
			for (ResourceGroupProfile profile : getResourceGroupProfileList()) {
				for (Resource resource : profile.getResourceList()) {
					if (resource.getResourceID().equals(resourceID)) {
						return resource;
					}
				}
			}
			return null;
		}
	}

	public ResourceGroupProfile getResourceGroupProfile(String resourceGroupName)
	    throws Exception {
		synchronized (LOCK) {
			if (resourceGroupName == null) {
				log.error("Invalid parm: " + ResourceGroupProfile.NAME_ATTR);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { ResourceGroupProfile.NAME_ATTR });
			}

			Reader r = new Reader(resourceGroupName);
			DbOperationsManager.INSTANCE.executeDbOpWithResults(
			    "select xml from ResourceGroups where groupId=?;", r);

			if (r.getResults().isEmpty()) {
				return null;
			}
			return r.getResults().get(0);
		}
	}

	public List<ResourceGroupProfile> getResourceGroupProfileList()
	    throws Exception {
		synchronized (LOCK) {
			Reader r = new Reader(null);
			DbOperationsManager.INSTANCE.executeDbOpWithResults(
			    "select xml from ResourceGroups;", r);
			return r.getResults();
		}
	}

	public void setResourceGroupMembership(String resourceGroupName,
	    MembershipData membership) throws Exception {
		synchronized (LOCK) {

			if (resourceGroupName == null) {
				log.error("Invalid parm: " + UserGroupProfile.NAME_ATTR);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { ResourceGroupProfile.NAME_ATTR });
			}

			if (membership == null) {
				log.error("Invalid parm: " + MembershipData.MEMBERSHIPDATA_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { MembershipData.MEMBERSHIPDATA_ELEMENT });
			}

			if (membership.getCreatedByGroupName() == null
			    || membership.getCreatedByGroupName().equals("")) {
				if (!resourceGroupName.equals(DbResourceGroupProfile.ROOT_ID)) {
					log.error("Invalid parm: " + MembershipData.MEMBERSHIPDATA_ELEMENT
					    + "." + MembershipData.MB_CREATEDBY_GROUPNAME_ELEMENT);
					throw new NrbException(
					    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
					    new Object[] { MembershipData.MEMBERSHIPDATA_ELEMENT + "."
					        + MembershipData.MB_CREATEDBY_GROUPNAME_ELEMENT });
				}
			}

			ResourceGroupProfile resourceGroupProfile = getResourceGroupProfile(resourceGroupName);
			MembershipData old = resourceGroupProfile.getMembership();
			resourceGroupProfile.setMembership(membership);
			update(resourceGroupProfile);

			/*
			 * we should update the membership referencing in user and resource
			 * group... user first
			 */
			List<Set<UserGroupName>> result = UserPolicyUtility.INSTANCE
			    .diffUserGroupLists(old.getMemberUserGroupName(),
			        membership.getMemberUserGroupName());
			Set<UserGroupName> appendList = result.get(0);
			Set<UserGroupName> removedList = result.get(1);

			for (UserGroupName userGroup : removedList) {
				// userGroup has been removed from our resource group
				UserGroupProfile u = DbUserGroupProfile.INSTANCE
				    .getUserGroupProfile(userGroup);
				Set<String> resourceMemberShip = u.getMembership()
				    .getMemberResourceGroupName();
				if (resourceMemberShip.remove(resourceGroupName)) {
					DbUserGroupProfile.INSTANCE.update(u);
				}
			}

			for (UserGroupName userGroup : appendList) {
				// userGroup has been added to our resource group, update if
				// required.
				UserGroupProfile u = DbUserGroupProfile.INSTANCE
				    .getUserGroupProfile(userGroup);
				Set<String> resourceMemberShip = u.getMembership()
				    .getMemberResourceGroupName();

				if (resourceMemberShip.add(resourceGroupName)) {
					DbUserGroupProfile.INSTANCE.update(u);
				}
			}
		}
	}

	public void setResourceGroupPolicy(String resourceGroupName,
	    ResourcePolicy resourcePolicy) throws Exception {
		synchronized (LOCK) {
			if (resourceGroupName == null) {
				log.error("Invalid parm: " + ResourceGroupProfile.NAME_ATTR);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { ResourceGroupProfile.NAME_ATTR });
			}

			if (resourcePolicy == null) {
				log.error("Invalid parm: " + ResourcePolicy.RESOURCEPOLICY_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { ResourcePolicy.RESOURCEPOLICY_ELEMENT });
			}

			ResourceGroupProfile profile = getResourceGroupProfile(resourceGroupName);
			if (profile == null) {
				throw new Exception("Cannot update policy on non-existant profile "
				    + resourceGroupName);
			}

			profile.setResourcePolicy(resourcePolicy);
			update(profile);
		}
	}

	public ResourceGroupProfile setResourceGroupResourceList(String resourceGroupName,
	    List<Resource> resourceList) throws Exception {
		ResourceGroupProfile profile;
		synchronized (LOCK) {
			if (resourceGroupName == null) {
				log.error("Invalid parm: " + ResourceGroupProfile.NAME_ATTR);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { ResourceGroupProfile.NAME_ATTR });
			}

			if (resourceList == null) {
				log.error("Invalid parm: " + ResourceGroupProfile.RESOURCELIST_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { ResourceGroupProfile.RESOURCELIST_ELEMENT });
			}

			profile = getResourceGroupProfile(resourceGroupName);
			if (profile == null) {
				throw new Exception(
				    "Cannot update resource list for non-existant resource group "
				        + resourceGroupName);
			}

			profile.setResourceList(resourceList);
			update(profile);
		}
		return profile;
	}

	protected void update(ResourceGroupProfile profile) throws Exception {
		synchronized (LOCK) {
			Updater u = new Updater(profile);
			DbOperationsManager.INSTANCE.executeDbOpWithResults(
			    "update ResourceGroups set xml = ? where groupId=?;", u);

		}
	}

	private boolean isResourceGroupProfileExisted(String groupID)
	    throws Exception {
		return getResourceGroupProfile(groupID) != null;
	}
}
