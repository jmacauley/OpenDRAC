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
import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.OrganizationData;
import com.nortel.appcore.app.drac.common.security.policy.types.PersonalData;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPreferences;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOpWithResultsI;
import com.nortel.appcore.app.drac.database.DbOperationsManager;
import com.nortel.appcore.app.drac.database.helper.UserPolicyUtility;

/**
 * @author pitman
 */
public enum DbUser {
	INSTANCE;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private static class Deleter implements DbOpWithResultsI {
		private final String userID;

		public Deleter(String user) {
			userID = user;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			statement.setString(1, userID);
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
		private final String userID;
		private final List<UserProfile> results = new ArrayList<UserProfile>();

		public Reader(String user) {
			userID = user;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			if (userID != null) {
				statement.setString(1, userID);
			}
			return statement;
		}

		public List<UserProfile> getResults() {
			return results;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			while (rs.next()) {
				UserProfile u = new UserProfile();
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
		private final UserProfile p;

		Updater(UserProfile userProfile) {
			p = userProfile;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			statement.setString(1, p.toXMLString());
			statement.setString(2, p.getUserID());
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
		private final UserProfile p;

		Writer(UserProfile userProfile) {
			p = userProfile;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			statement.setString(1, p.getUserID());
			statement.setString(2, p.toXMLString());
			return statement;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			// not used
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
			//
		}

	}

	/**
	 * We synchronize user, user group and resource group operations on a single
	 * lock. Why? Some operations end up touching multiple databases to maintain
	 * the linkage between users in a group, users, etc. Having a common lock
	 * avoids runtime deadlocks and prevents against any mischief. It hurts
	 * performance, but user profile data isn't a performance goal.
	 */
	private static final Object LOCK = new Object();

	public void createUserProfile(UserProfile user) throws Exception {
		synchronized (LOCK) {
			if (user == null) {
				log.error("Existing element: " + UserProfile.USERID_ATTR);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { UserProfile.USER_ELEMENT });
			}

			// check if existed
			if (userExists(user.getUserID())) {
				log.error("Cannot create user, user already exists " + user.getUserID());
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_EXISTED,
				    new Object[] { UserProfile.USER_ELEMENT + ": " + user.getUserID() });
			}

			DbOperationsManager.INSTANCE.executeDbOpWithResults(
			    "insert into Users (userId, xml) values(?,?);", new Writer(user));

			/* Update the user groups to reference this user */
			Set<UserGroupName> userGroupList = user.getMembershipData()
			    .getMemberUserGroupName();
			for (UserGroupName userGroup : userGroupList) {
				UserGroupProfile profile = DbUserGroupProfile.INSTANCE
				    .getUserGroupProfile(userGroup);
				Set<String> ids = profile.getMembership().getMemberUserID();
				if (ids.add(user.getUserID())) {
					DbUserGroupProfile.INSTANCE.update(profile);
				}
			}
		}
	}

	/**
	 * For testing only, will destroy the cross linkage between
	 * users/groups/resources!
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "delete from Users where userId <> 'admin';",
		    new DbOpWithResultsAdapter());
	}

	public void deleteUserProfile(String userID) throws Exception {
		synchronized (LOCK) {
			UserProfile user = getUserProfile(userID);
			if (user == null) {
				return;
			}

			//

			/* Update the user groups which reference this user */
			Set<UserGroupName> userGroupList = user.getMembershipData()
			    .getMemberUserGroupName();
			for (UserGroupName userGroup : userGroupList) {
				UserGroupProfile profile = DbUserGroupProfile.INSTANCE
				    .getUserGroupProfile(userGroup);
				if (profile == null) {
					log.error("Referenced user group " + userGroup
					    + " does not exist, while deleting user " + user.toString());
				}
				else {
					Set<String> ids = profile.getMembership().getMemberUserID();
					if (ids.remove(user.getUserID())) {
						DbUserGroupProfile.INSTANCE.update(profile);
					}
				}
			}
			DbOperationsManager.INSTANCE.executeDbOpWithResults(
			    "delete from Users where userId=?;", new Deleter(userID));
		}
	}

	/**
	 * We synchronize user, user group and resource group operations on a single
	 * lock. Why? Some operations end up touching multiple databases to maintain
	 * the linkage between users in a group, users, etc. Having a common lock
	 * avoids runtime deadlocks and prevents against any mischief. It hurts
	 * performance, but user profile data isn't a performance goal.
	 */
	public Object getLock() {
		return LOCK;
	}

	public UserProfile getUserProfile(String userID) throws Exception {
		if (userID == null) {
			throw new Exception("Cannot get userProfile, userID null");
		}
		Reader r = new Reader(userID);
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "select xml from Users where userId=?;", r);

		if (r.getResults().isEmpty()) {
			return null;
		}
		return r.getResults().get(0);
	}

	public List<UserProfile> getUserProfileList() throws Exception {
		Reader r = new Reader(null);
		DbOperationsManager.INSTANCE.executeDbOpWithResults("select xml from Users;", r);
		return r.getResults();
	}

	public void setupUserAuthenticationAccount(String userID,
	    AbstractCredential credential) throws Exception {
		synchronized (LOCK) {
			UserProfile user = getUserProfile(userID);
			if (user == null) {
				throw new Exception("Cannot update user " + userID + ", does not exist");
			}

			user.setupUserAuthenticationAccount(
			    DbGlobalPolicy.INSTANCE.getGlobalPolicy(), credential);
			//
			setUserAuthenticationData(userID, user.getAuthenticationData());
		}
	}

	public void setUserAccountStatus(String userID, AccountStatus status)
	    throws Exception {
		synchronized (LOCK) {
			UserProfile user = getUserProfile(userID);
			if (user == null) {
				throw new Exception("Cannot update user " + userID + ", does not exist");
			}

			if (status == null) {
				log.error("Invalid parm: " + AccountStatus.ACCOUNT_STATUS_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { AccountStatus.ACCOUNT_STATUS_ELEMENT });
			}

			user.setAccountStatus(status);
			//
			update(user);
		}
	}

	public void setUserAuthenticationData(String userID,
	    AuthenticationData authenticationData) throws Exception {
		synchronized (LOCK) {
			UserProfile user = getUserProfile(userID);
			if (user == null) {
				throw new Exception("Cannot update user " + userID + ", does not exist");
			}

			if (authenticationData == null) {
				log.error("Invalid parm: " + AuthenticationData.AUTHENTICATION_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { AuthenticationData.AUTHENTICATION_ELEMENT });
			}

			user.setAuthenticationData(authenticationData);
			//
			update(user);
		}
	}

	public void setUserAuthenticationState(String userID,
	    UserProfile.AuthenticationState authenticationState) throws Exception {
		synchronized (LOCK) {
			UserProfile user = getUserProfile(userID);
			if (user == null) {
				throw new Exception("Cannot update user " + userID + ", does not exist");
			}

			if (authenticationState == null) {
				log.error("Invalid parm: " + UserProfile.AUTHENTICATIONSTATE_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { UserProfile.AUTHENTICATIONSTATE_ELEMENT });
			}

			if (user.getAuthenticationData() == null) {
				user.setAuthenticationData(new AuthenticationData());
			}
			user.getAuthenticationData().setAuthenticationState(authenticationState);
			//
			update(user);
		}
	}

	public void setUserMembership(String userID, MembershipData membership)
	    throws Exception {
		synchronized (LOCK) {
			if (userID == null) {
				log.error("Invalid parm: " + UserProfile.USERID_ATTR);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { UserProfile.USERID_ATTR });
			}

			if (membership == null) {
				log.error("Invalid parm: " + MembershipData.MEMBERSHIPDATA_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { MembershipData.MEMBERSHIPDATA_ELEMENT });
			}

			UserProfile profile = getUserProfile(userID);
			if (profile == null) {
				log.error("cannot locate usergroup: " + userID);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { UserProfile.USER_ELEMENT + ": " + userID });
			}

			MembershipData old = profile.getMembershipData();
			profile.setMembershipData(membership);
			update(profile);

			// we should update the user membership referencing in user-group...
			List<Set<UserGroupName>> result = UserPolicyUtility.INSTANCE
			    .diffUserGroupLists(old.getMemberUserGroupName(),
			        membership.getMemberUserGroupName());
			Set<UserGroupName> appendList = result.get(0);
			Set<UserGroupName> removedList = result.get(1);
			log.debug("Processing setUserMembership for " + userID + " to "
			    + membership + " from " + old + " delta append:" + appendList
			    + " delta remove: " + removedList);
			for (UserGroupName userGroup : removedList) {
				UserGroupProfile groupProfile = DbUserGroupProfile.INSTANCE
				    .getUserGroupProfile(userGroup);
				if (groupProfile == null) {
					throw new Exception("Unable to update user member ship for '"
					    + userID + "' to '" + membership.toString()
					    + "' the usergroup profile '" + userGroup + "' does not exist");
				}
				Set<String> ids = groupProfile.getMembership().getMemberUserID();
				if (ids.remove(userID)) {
					DbUserGroupProfile.INSTANCE.update(groupProfile);
				}
			}

			for (UserGroupName userGroup : appendList) {
				UserGroupProfile groupProfile = DbUserGroupProfile.INSTANCE
				    .getUserGroupProfile(userGroup);
				if (groupProfile == null) {
					throw new Exception("Unable to update user member ship for '"
					    + userID + "' to '" + membership.toString()
					    + "' the usergroup profile '" + userGroup + "' does not exist");
				}
				Set<String> ids = groupProfile.getMembership().getMemberUserID();
				if (ids.add(userID)) {
					DbUserGroupProfile.INSTANCE.update(groupProfile);
				}
			}
		}
	}

	public void setUserOrganization(String userID, OrganizationData orgData)
	    throws Exception {
		synchronized (LOCK) {
			UserProfile user = getUserProfile(userID);
			if (user == null) {
				throw new Exception("Cannot update user " + userID + ", does not exist");
			}

			if (orgData == null) {
				log.error("Invalid parm: " + OrganizationData.ORGANIZATIONDATA_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { OrganizationData.ORGANIZATIONDATA_ELEMENT });
			}

			user.setOrganizationData(orgData);
			update(user);
		}
	}

	public void setUserPersonalData(String userID, PersonalData personalData)
	    throws Exception {
		synchronized (LOCK) {
			UserProfile user = getUserProfile(userID);
			if (user == null) {
				throw new Exception("Cannot update user " + userID + ", does not exist");
			}

			if (personalData == null) {
				log.error("Invalid parm: " + PersonalData.USERDATA_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { PersonalData.USERDATA_ELEMENT });
			}

			user.setPersonalData(personalData);
			//
			update(user);
		}
	}

	public void setUserTimeZoneIDPreference(String userID, String localeValue)
	    throws Exception {
		synchronized (LOCK) {
			UserProfile user = getUserProfile(userID);
			if (user == null) {
				throw new Exception("Cannot update user " + userID + ", does not exist");
			}

			if (localeValue == null) {
				log.error("Invalid parm: " + OrganizationData.ORGANIZATIONDATA_ELEMENT);
				throw new NrbException(
				    DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
				    new Object[] { OrganizationData.ORGANIZATIONDATA_ELEMENT });
			}

			if (user.getPreferences() == null) {
				user.setPreferences(new UserPreferences());
			}

			user.getPreferences().setTimeZoneId(localeValue);
			//
			update(user);
		}
	}

	/**
     *
     */
	protected void update(UserProfile user) throws Exception {
		synchronized (LOCK) {
			Updater u = new Updater(user);
			DbOperationsManager.INSTANCE.executeDbOpWithResults(
			    "update Users set xml = ? where userId=?;", u);
		}
	}

	private boolean userExists(String userID) throws Exception {
        boolean result = false;
        synchronized (LOCK) {
            if (getUserProfile(userID) != null) {
                result = true;
            }
        }
		return result;
	}

}
