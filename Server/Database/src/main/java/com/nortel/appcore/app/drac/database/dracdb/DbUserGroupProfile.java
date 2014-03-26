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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.security.policy.types.GroupPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.RootUserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOpWithResultsI;
import com.nortel.appcore.app.drac.database.DbOperationsManager;
import com.nortel.appcore.app.drac.database.helper.UserPolicyUtility;

/**
 * @author pitman
 */
public enum DbUserGroupProfile {
  INSTANCE;
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static class Deleter implements DbOpWithResultsI {
    private final UserGroupName groupID;

    public Deleter(UserGroupName group) {
      groupID = group;
    }

    @Override
    public PreparedStatement buildPreparedStatement(PreparedStatement statement)
        throws Exception {
      statement.setString(1, groupID.toString());
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
    private final UserGroupName groupID;
    private final List<UserGroupProfile> results = new ArrayList<UserGroupProfile>();

    public Reader(UserGroupName groupName) {
      groupID = groupName;
    }

    @Override
    public PreparedStatement buildPreparedStatement(PreparedStatement statement)
        throws Exception {
      if (groupID != null) {
        statement.setString(1, groupID.toString());
      }
      return statement;
    }

    public List<UserGroupProfile> getResults() {
      return results;
    }

    @Override
    public void processResults(ResultSet rs) throws Exception {
      while (rs.next()) {
        UserGroupProfile u = new UserGroupProfile();
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
    private final UserGroupProfile p;

    Updater(UserGroupProfile userGroupProfile) {
      p = userGroupProfile;
    }

    @Override
    public PreparedStatement buildPreparedStatement(PreparedStatement statement)
        throws Exception {
      statement.setString(1, p.toXMLString());
      statement.setString(2, p.getName().toString());
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
    private final UserGroupProfile p;

    Writer(UserGroupProfile userGroupProfile) {
      p = userGroupProfile;
    }

    @Override
    public PreparedStatement buildPreparedStatement(PreparedStatement statement)
        throws Exception {
      statement.setString(1, p.getName().toString());
      statement.setString(2, p.toXMLString());
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

  /*
   * Called in a non-static fashion so we insure the DbUser is created and up
   * before we go and that all access is via the getInstance method to make
   * searching easier
   */
  private static final Object LOCK = DbUser.INSTANCE.getLock();

  public static final String DEFAULT_USER_GROUP = "SystemAdminGroup";

  public void createUserGroupProfile(UserGroupProfile profile) throws Exception {
    synchronized (LOCK) {
      if (profile == null) {
        log.error("Invalid parm: " + UserGroupProfile.USERGROUP_ELEMENT);
        throw new NrbException(
            DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
            new Object[] { UserGroupProfile.USERGROUP_ELEMENT });
      }

      // check if existed
      if (userProfileExists(profile.getName())) {
        log.error("Existing element: " + UserGroupProfile.NAME_ATTR + " "
            + profile.getName());
        throw new NrbException(
            DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
            new Object[] { profile.getName() });
      }

      /*
       * check if createdBy exists, required on all records except for the
       * DEFAULT_USER_GROUP entry which has not default
       */
      String parentStr = profile.getMembership().getCreatedByGroupName();
      if (parentStr != null && "".equals(parentStr.trim())) {
        // treat blank and null the same to save later comparisons.
        parentStr = null;
      }

      if (parentStr == null
          && !DEFAULT_USER_GROUP.equals(profile.getName().toString())) {
        log.error("Existing element: " + UserGroupProfile.NAME_ATTR + " "
            + profile.getName());
        throw new NrbException(
            DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
            new Object[] { profile.getName() + "::"
                + MembershipData.MB_CREATEDBY_GROUPNAME_ELEMENT });
      }

      UserGroupName parent = null;
      UserGroupProfile parentProfile = null;
      if (parentStr != null) {
        // Make sure the parent group exists
        parent = new UserGroupName(parentStr);
        parentProfile = getUserGroupProfile(parent);
        if (parentProfile == null
            && !DEFAULT_USER_GROUP.equals(profile.getName().toString())) {
          log.error("Cannot create new user group, group declares parent of <"
              + parentStr + "> but that parent group does not exist for "
              + profile.toString());
          throw new NrbException(
              DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
              new Object[] { profile.getName() + "::"
                  + MembershipData.MB_CREATEDBY_GROUPNAME_ELEMENT });
        }
      }


      DbOperationsManager.INSTANCE.executeDbOpWithResults(
          "insert into UserGroups (groupId, xml) values(?,?);", new Writer(
              profile));

      if (parentStr != null) {
        // Now update the parent groups record.
        Set<UserGroupName> children = parentProfile.getMembership()
            .getMemberUserGroupName();
        children.add(profile.getName());
        update(parentProfile);
      }

      /*
       * If we are creating the user group and say we belong to resource groups
       * then we need to update those resource groups to link back to us.
       */
      Set<String> resourcegroupList = profile.getMembership()
          .getMemberResourceGroupName();
      if (resourcegroupList != null) {
        for (String resourceGroup : resourcegroupList) {
          ResourceGroupProfile p = DbResourceGroupProfile.INSTANCE
              .getResourceGroupProfile(resourceGroup);
          if (p != null) {
            if (p.getMembership().getMemberUserGroupName()
                .add(profile.getName())) {
              DbResourceGroupProfile.INSTANCE.update(p);
            }
          }
        }
      }

      // Update our member users to keep the linkages straight.
      Set<String> userList = profile.getMembership().getMemberUserID();
      if (userList != null) {
        for (String user : userList) {
          UserProfile u = DbUser.INSTANCE.getUserProfile(user);
          if (u != null) {
            if (u.getMembershipData().getMemberUserGroupName()
                .add(profile.getName())) {
              DbUser.INSTANCE.update(u);
            }
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
    DbOperationsManager.INSTANCE.executeDbOpWithResults(
        "delete from UserGroups where groupId <> 'SystemAdminGroup';",
        new DbOpWithResultsAdapter());
  }

  public void deleteUserGroupProfile(UserGroupName groupID) throws Exception {
    synchronized (LOCK) {
      if (groupID == null) {
        log.error("Invalid parm: " + UserGroupProfile.NAME_ATTR);
        throw new NrbException(
            DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
            new Object[] { UserGroupProfile.NAME_ATTR });
      }

      UserGroupProfile profile = getUserGroupProfile(groupID);
      if (profile == null) {
        return;
      }

      // 

      /* Step 1: Remove linkages in resource Groups to this user group */
      for (String resourceGroup : profile.getMembership()
          .getMemberResourceGroupName()) {
        ResourceGroupProfile r = DbResourceGroupProfile.INSTANCE
            .getResourceGroupProfile(resourceGroup);
        if (r == null) {
          log.error("Referenced resource group " + resourceGroup
              + " does not exist, while deleting user group profile "
              + profile.toString());
        }
        else {
          if (r.getMembership().getMemberUserGroupName().remove(groupID)) {
            DbResourceGroupProfile.INSTANCE.update(r);
          }
        }
      }

      /*
       * Step: 2 Other user groups can reference us if we are nested, remove
       * linkages from other groups, here we scan all existing users.
       */

      for (UserGroupProfile ugp : getUserGroupProfileList()) {
        Set<UserGroupName> ug = ugp.getMembership().getMemberUserGroupName();
        if (ug.remove(groupID)) {
          /*
           * This Group listed the group we want to delete as a member, update
           * it.
           */
          update(ugp);
        }
      }

      /* Step 3: Remove linkages in user to this user group */

      for (UserProfile u : DbUser.INSTANCE.getUserProfileList()) {
        Set<UserGroupName> ug = u.getMembershipData().getMemberUserGroupName();
        if (ug.remove(groupID)) {
          /*
           * This user was a member of the group we want to delete, update the
           * user record.
           */
          DbUser.INSTANCE.update(u);
        }
      }

      /* Step 4: Remove the user group */
      DbOperationsManager.INSTANCE.executeDbOpWithResults(
          "delete from UserGroups where groupId=?;", new Deleter(groupID));

    }

  }

  public UserGroupProfile getUserGroupProfile(UserGroupName groupID)
      throws Exception {
    synchronized (LOCK) {
      if (groupID == null) {
        log.error("Invalid parm: " + UserGroupProfile.NAME_ATTR);
        throw new NrbException(
            DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
            new Object[] { UserGroupProfile.NAME_ATTR });
      }

      Reader r = new Reader(groupID);
      DbOperationsManager.INSTANCE.executeDbOpWithResults(
          "select xml from UserGroups where groupId=?;", r);

      if (r.getResults().isEmpty()) {
        return null;
      }
      return r.getResults().get(0);
    }
  }

  public List<UserGroupProfile> getUserGroupProfileList() throws Exception {
    synchronized (LOCK) {
      Reader r = new Reader(null);
      DbOperationsManager.INSTANCE.executeDbOpWithResults("select xml from UserGroups;",
          r);
      return r.getResults();
    }
  }

  public List<UserGroupProfile> getUserGroupProfileListByMember()
      throws Exception {
    List<UserGroupProfile> ugp = getUserGroupProfileList();
    UserGroupProfile[] up = ugp.toArray(new UserGroupProfile[ugp.size()]);

    /*
     * We want to return list sorted by createdByMemberName not that i'm not
     * 100% positive that the calling code even needs to have sorted results
     * given to it!
     */
    Arrays.sort(up, new Comparator<UserGroupProfile>() {
      @Override
      public int compare(UserGroupProfile o1, UserGroupProfile o2) {
        String s1 = o1.getMembership() == null ? "" : o1.getMembership()
            .getCreatedByGroupName();
        String s2 = o2.getMembership() == null ? "" : o2.getMembership()
            .getCreatedByGroupName();
        if (s1.compareTo(s2) != 0) {
          return s1.compareTo(s2);
        }
        // They were created by the same member, sort on name as a
        // secondary key
        return o1.getName().compareTo(o2.getName());
      }
    });

    return new ArrayList<UserGroupProfile>(Arrays.asList(up));
  }

  public void setUserGroupMembership(UserGroupName groupID,
      MembershipData membership) throws Exception {
    synchronized (LOCK) {
      if (groupID == null) {
        log.error("Invalid parm: " + UserGroupProfile.NAME_ATTR);
        throw new NrbException(
            DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
            new Object[] { UserGroupProfile.NAME_ATTR });
      }

      if (membership == null) {
        log.error("Invalid parm: " + MembershipData.MEMBERSHIPDATA_ELEMENT);
        throw new NrbException(
            DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
            new Object[] { MembershipData.MEMBERSHIPDATA_ELEMENT });
      }

      if (membership.getCreatedByGroupName() == null
          || membership.getCreatedByGroupName().equals("")) {
        if (!groupID.equals(RootUserGroupName.SYSTEM_ADMIN_GROUP)) {
          log.error("Invalid parm: " + MembershipData.MEMBERSHIPDATA_ELEMENT
              + "." + MembershipData.MB_CREATEDBY_GROUPNAME_ELEMENT);
          throw new NrbException(
              DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
              new Object[] { MembershipData.MEMBERSHIPDATA_ELEMENT + "."
                  + MembershipData.MB_CREATEDBY_GROUPNAME_ELEMENT });
        }
      }

      log.debug("Processing setUserGroupMembership for " + groupID + " to "
          + membership);
      UserGroupProfile profile = getUserGroupProfile(groupID);
      if (profile == null) {
        throw new Exception("Cannot update userGroupProfile " + groupID
            + ", does not exist");
      }

      /* Step 1: Update resource groups */

      Set<String>[] result = UserPolicyUtility.INSTANCE
          .diffMembershipLists(profile.getMembership()
              .getMemberResourceGroupName(), membership
              .getMemberResourceGroupName());
      Set<String> appendList = result[0];
      Set<String> removedList = result[1];

      for (String resGrpID : removedList) {
        ResourceGroupProfile p = DbResourceGroupProfile.INSTANCE
            .getResourceGroupProfile(resGrpID);
        Set<UserGroupName> groupMembership = p.getMembership()
            .getMemberUserGroupName();
        if (groupMembership.remove(groupID)) {
          DbResourceGroupProfile.INSTANCE.update(p);
        }
      }

      for (String resGrpID : appendList) {
        ResourceGroupProfile p = DbResourceGroupProfile.INSTANCE
            .getResourceGroupProfile(resGrpID);
        Set<UserGroupName> groupMembership = p.getMembership()
            .getMemberUserGroupName();
        if (groupMembership.add(groupID)) {
          DbResourceGroupProfile.INSTANCE.update(p);
        }
      }

      /*
       * Step 2: Update users in the group. Diff the old and new members and
       * update as required
       */

      result = UserPolicyUtility.INSTANCE.diffMembershipLists(profile
          .getMembership().getMemberUserID(), membership.getMemberUserID());
      appendList = result[0];
      removedList = result[1];

      for (String user : removedList) {
        // User is no longer a member of this group
        UserProfile u = DbUser.INSTANCE.getUserProfile(user);
        Set<UserGroupName> groupMemberShip = u.getMembershipData()
            .getMemberUserGroupName();
        if (groupMemberShip.remove(groupID)) {
          DbUser.INSTANCE.update(u);
        }
      }

      for (String user : appendList) {
        // User is now a member of this group
        UserProfile u = DbUser.INSTANCE.getUserProfile(user);
        Set<UserGroupName> groupMemberShip = u.getMembershipData()
            .getMemberUserGroupName();
        if (groupMemberShip.add(groupID)) {
          DbUser.INSTANCE.update(u);
        }
      }

      /* Step 3: Update the group */
      profile.setMembership(membership);

      update(profile);
    }
  }

  public void setUserGroupUserGroupPolicy(UserGroupName groupID,
      GroupPolicy groupPolicy) throws Exception {
    synchronized (LOCK) {
      if (groupID == null) {
        log.error("Invalid parm: " + UserGroupProfile.NAME_ATTR);
        throw new NrbException(
            DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
            new Object[] { UserGroupProfile.NAME_ATTR });
      }

      if (groupPolicy == null) {
        log.error("Invalid parm: " + GroupPolicy.USERGOURPPOLICY_ELEMENT);
        throw new NrbException(
            DracErrorConstants.SECURITY_ERROR_PARAMETER_INVALID,
            new Object[] { GroupPolicy.USERGOURPPOLICY_ELEMENT });
      }

      UserGroupProfile profile = getUserGroupProfile(groupID);
      if (profile == null) {
        throw new Exception("Cannot update userGroupProfile " + groupID
            + ", does not exist");
      }

      profile.setGroupPolicy(groupPolicy);
      // 
      update(profile);
    }
  }

  protected void update(UserGroupProfile profile) throws Exception {
    synchronized (LOCK) {
      Updater u = new Updater(profile);
      DbOperationsManager.INSTANCE.executeDbOpWithResults(
          "update UserGroups set xml = ? where groupId=?;", u);
      log.debug("Updated userProfile " + profile.getName() + " to "
          + profile.toString()); // ,
    }
  }

  private boolean userProfileExists(UserGroupName groupID) throws Exception {
    return getUserGroupProfile(groupID) != null;
  }

}
