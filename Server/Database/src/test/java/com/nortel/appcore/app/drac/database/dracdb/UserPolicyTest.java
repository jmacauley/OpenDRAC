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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.authentication.types.LocalAccountCredential;
import com.nortel.appcore.app.drac.common.security.policy.types.AbstractRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccessRule;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationAuditData;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
import com.nortel.appcore.app.drac.common.security.policy.types.BandwidthControlRule;
import com.nortel.appcore.app.drac.common.security.policy.types.GroupPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.security.policy.types.InternalAccountData;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.OrganizationData;
import com.nortel.appcore.app.drac.common.security.policy.types.PasswordPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.PersonalData;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserAccountPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.AccessPermission;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AccountState;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationState;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationType;
import com.nortel.appcore.app.drac.database.helper.ProfileValidator;

/**
 * @author pitman
 */
public final class UserPolicyTest {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final String ITS_NULL = "null!";
  private static final String DEFAULT_USER = "admin";

  @Before
  public void setup() {
    TestHelper.INSTANCE.initialize();
  }

  @Test
  public void resourceGroupProfileNullandEmptyTests() throws Exception {
    DbResourceGroupProfile.INSTANCE.deleteAll();

    try {
      DbResourceGroupProfile.INSTANCE.deleteResourceGroupProfile(null);
      fail(ITS_NULL);
    }
    catch (Exception e) {
    }

    try {
      DbResourceGroupProfile.INSTANCE.createResourceGroupProfile(null);
      fail(ITS_NULL);
    }
    catch (Exception e) {
    }

    DbResourceGroupProfile.INSTANCE.getEndpointResource(null);
    DbResourceGroupProfile.INSTANCE.getEndpointResource("bob");

    try {
      DbResourceGroupProfile.INSTANCE.getResourceGroupProfile(null);
      fail(ITS_NULL);
    }
    catch (Exception e) {
    }

    try {
      DbResourceGroupProfile.INSTANCE.setResourceGroupMembership(null, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbResourceGroupProfile.INSTANCE.setResourceGroupMembership("bob", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbResourceGroupProfile.INSTANCE.setResourceGroupPolicy(null, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbResourceGroupProfile.INSTANCE.setResourceGroupPolicy("bob", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbResourceGroupProfile.INSTANCE.setResourceGroupResourceList(null, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbResourceGroupProfile.INSTANCE.setResourceGroupResourceList("bob", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }
  }

  @Test
  public void testCreateResourceGroupUserGroupUser() throws Exception {
    DbResourceGroupProfile.INSTANCE.deleteAll();

    UserGroupName testGroup = new UserGroupName("JunitUserGroup");

    DbResourceGroupProfile.INSTANCE
        .deleteResourceGroupProfile("JunitResourceGroup");
    DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(testGroup);
    DbUser.INSTANCE.deleteUserProfile("JunitUser");
    DbUser.INSTANCE.deleteUserProfile("JunitUser");

    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    log.debug("Existing resource groups "
        + DbResourceGroupProfile.INSTANCE.getResourceGroupProfileList());
    log.debug("Existing UserGroups "
        + DbUserGroupProfile.INSTANCE.getUserGroupProfileList());
    log.debug("Existing Users  " + DbUser.INSTANCE.getUserProfileList());

    ResourceGroupProfile rgp = new ResourceGroupProfile("JunitResourceGroup",
        Calendar.getInstance(), Calendar.getInstance(), DEFAULT_USER,
        DbResourceGroupProfile.DEFAULT_RESOURCE_GROUP);
    rgp.getMembership().setMemberUserGroupName(
        new HashSet<UserGroupName>(Arrays
            .asList(new UserGroupName[] { testGroup })));

    DbResourceGroupProfile.INSTANCE.createResourceGroupProfile(rgp);

    DbResourceGroupProfile.INSTANCE
        .deleteResourceGroupProfile("JunitResourceGroup");
    DbResourceGroupProfile.INSTANCE
        .deleteResourceGroupProfile("JunitResourceGroup");

    DbResourceGroupProfile.INSTANCE.createResourceGroupProfile(rgp);

    try {
      DbResourceGroupProfile.INSTANCE.createResourceGroupProfile(rgp);
      fail("created a duplicate resource group!");
    }
    catch (Exception e) {

    }

    UserGroupProfile ugp = new UserGroupProfile(testGroup,
        Calendar.getInstance(), Calendar.getInstance(), DEFAULT_USER,
        new UserGroupName(DbUserGroupProfile.DEFAULT_USER_GROUP));
    ugp.getMembership().setMemberResourceGroupName(
        new HashSet<String>(Arrays
            .asList(new String[] { "JunitResourceGroup" })));

    DbUserGroupProfile.INSTANCE.createUserGroupProfile(ugp);

    UserProfile up = new UserProfile("JunitUser", Calendar.getInstance(),
        Calendar.getInstance());
    OrganizationData od = new OrganizationData();
    od.setCategory("Category");
    od.setDescription("Description");
    od.setOrgName("setOrgName");
    od.setOrgUnitName("setOrgUnitName");
    od.setOwner("setOwner");
    od.setSeeAlso("setSeeAlso");
    PersonalData pd = new PersonalData();
    pd.setAddress(new PersonalData.Address("phone", "mail@mail",
        "15 someStreet, St, Ottawa,on"));
    pd.setDescription("setDescription");
    pd.setName(new PersonalData.UserName("commonName", "givenname", "surName"));
    pd.setTitle("title");
    up.setOrganizationData(od);
    up.setPersonalData(pd);

    AuthenticationData ad = new AuthenticationData();
    AuthenticationAuditData aad = new AuthenticationAuditData();
    aad.setNumOfInvalidAttempts(1);
    aad.addLoginAddress(new IPAddress("127.0.0.1", "80"));
    aad.addLocationOfInvalidAttempt(new IPAddress("127.0.0.1", "1000"));
    ad.setAuditData(aad);
    ad.setAuthenticationState(AuthenticationState.VALID);
    ad.setAuthenticationType(AuthenticationType.INTERNAL);
    ad.setInternalAccountData(new InternalAccountData());
    ad.setWSDLCredential("12345");
    ad.getUserAccountPolicy().setDormantPeriod(Integer.valueOf(1000));
    ad.getUserAccountPolicy().setInactivityPeriod(Integer.valueOf(10000000));
    ad.getUserAccountPolicy().setMaxInvalidLoginAttempts(Integer.valueOf(100));
    ad.getUserAccountPolicy().setLockoutPeriod(Integer.valueOf(10));
    PasswordPolicy ppp = new PasswordPolicy();
    ppp.setPwAging(Integer.valueOf(9999));
    ppp.setPwExpiredNotif(Integer.valueOf(10));
    ppp.setPwHistorySize(Integer.valueOf(10));
    ppp.setPwInvalids(Arrays.asList(new String[] { "big", "bad", "bob" }));
    ppp.setPwRules("badRule");
    ad.getUserAccountPolicy().setLocalPasswordPolicy(ppp);
    ad.getUserAccountPolicy().setLockedClientIPs(
        Arrays.asList(new String[] { "127.10.10.10", "192.192.192.1" }));
    up.setAuthenticationData(ad);

    up.setupUserAuthenticationAccount(new LocalAccountCredential("JunitUser",
        "myDrac", new IPAddress(null, null)), new UserAccountPolicy());

    MembershipData md = new MembershipData("");
    md.setMemberUserGroupName(new HashSet<UserGroupName>(Arrays
        .asList(new UserGroupName[] { testGroup })));
    up.setMembershipData(md);

    DbUser.INSTANCE.createUserProfile(up);

    try {
      DbUser.INSTANCE.createUserProfile(up);
      fail("added twice!");
    }
    catch (Exception e) {

    }

    log.debug("Test Resource group "
        + DbResourceGroupProfile.INSTANCE
            .getResourceGroupProfile("JunitResourceGroup"));
    log.debug("Test UserGroup "
        + DbUserGroupProfile.INSTANCE.getUserGroupProfile(testGroup));
    log.debug("Test Users  " + DbUser.INSTANCE.getUserProfile("JunitUser"));
    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());
    DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(testGroup);
    DbUser.INSTANCE.deleteUserProfile("JunitUser");
  }

  @Test
  /*
   * Create several resourcegroups, user groups and users and link and unlink
   * them to each other.
   */
  public void testLinkages() throws Exception {
    UserGroupName ugn1 = new UserGroupName("JunitUg1");
    UserGroupName ugn2 = new UserGroupName("JunitUg2");
    UserGroupName ugn3 = new UserGroupName("JunitUg3");
    DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(ugn1);
    DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(ugn2);
    DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(ugn3);
    DbResourceGroupProfile.INSTANCE.deleteResourceGroupProfile("JunitRG3");
    DbResourceGroupProfile.INSTANCE.deleteResourceGroupProfile("JunitRG2");
    DbResourceGroupProfile.INSTANCE.deleteResourceGroupProfile("JunitRG1");
    DbUser.INSTANCE.deleteUserProfile("JunitU1");
    DbUser.INSTANCE.deleteUserProfile("JunitU2");
    DbUser.INSTANCE.deleteUserProfile("JunitU3");

    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    /* Resource GROUPS 1, 2, 3 */
    ResourceGroupProfile rgp1 = new ResourceGroupProfile("JunitRG1",
        Calendar.getInstance(), Calendar.getInstance(), DEFAULT_USER,
        DbResourceGroupProfile.DEFAULT_RESOURCE_GROUP);
    DbResourceGroupProfile.INSTANCE.createResourceGroupProfile(rgp1);

    ResourceGroupProfile rgp2 = new ResourceGroupProfile("JunitRG2",
        Calendar.getInstance(), Calendar.getInstance(), DEFAULT_USER,
        "JunitRG1");
    DbResourceGroupProfile.INSTANCE.createResourceGroupProfile(rgp2);

    ResourceGroupProfile rgp3 = new ResourceGroupProfile("JunitRG3",
        Calendar.getInstance(), Calendar.getInstance(), DEFAULT_USER,
        "JunitRG2");
    DbResourceGroupProfile.INSTANCE.createResourceGroupProfile(rgp3);

    /* User Groups 1, 2, 3 */

    UserGroupProfile ugp1 = new UserGroupProfile(ugn1, Calendar.getInstance(),
        Calendar.getInstance(), DEFAULT_USER, new UserGroupName(
            DbUserGroupProfile.DEFAULT_USER_GROUP));
    ugp1.getMembership().setMemberResourceGroupName(
        new HashSet<String>(Arrays.asList(new String[] { "JunitRG1" })));
    ugp1.setReferencingUserGroupName(ugn1);
    DbUserGroupProfile.INSTANCE.createUserGroupProfile(ugp1);

    UserGroupProfile ugp2 = new UserGroupProfile(ugn2, Calendar.getInstance(),
        Calendar.getInstance(), DEFAULT_USER, new UserGroupName(
            DbUserGroupProfile.DEFAULT_USER_GROUP));
    ugp2.getMembership().setMemberResourceGroupName(
        new HashSet<String>(Arrays.asList(new String[] { "JunitRG2" })));
    ugp2.setReferencingUserGroupName(ugn2);
    DbUserGroupProfile.INSTANCE.createUserGroupProfile(ugp2);

    UserGroupProfile ugp3 = new UserGroupProfile(ugn3, Calendar.getInstance(),
        Calendar.getInstance(), DEFAULT_USER, new UserGroupName(
            DbUserGroupProfile.DEFAULT_USER_GROUP));
    ugp3.getMembership().setMemberResourceGroupName(
        new HashSet<String>(Arrays.asList(new String[] { "JunitRG3" })));

    ugp3.setReferencingUserGroupName(ugn3);
    DbUserGroupProfile.INSTANCE.createUserGroupProfile(ugp3);

    GroupPolicy gp = new GroupPolicy();
    BandwidthControlRule b = new BandwidthControlRule();
    b.setMaximumAggregateServiceSize(Integer.valueOf(10000));
    b.setMaximumServiceBandwidth(Integer.valueOf(10000));
    b.setMaximumServiceDuration(Integer.valueOf(10000));
    b.setMaximumServiceSize(Integer.valueOf(10000));
    b.setRuleID("id");
    gp.setBandwidthControlRule(b);
    AccessRule a = new AccessRule(AccessPermission.GRANT);
    gp.setAccessControlRule(Arrays.asList(new AbstractRule[] { a }));
    // gp.setSystemAccessRule()
    DbUserGroupProfile.INSTANCE.setUserGroupUserGroupPolicy(ugn3, gp);

    /* Users 1, 2, 3 */
    UserProfile u1 = new UserProfile("JunitU1", Calendar.getInstance(),
        Calendar.getInstance());
    UserProfile u2 = new UserProfile("JunitU2", Calendar.getInstance(),
        Calendar.getInstance());
    UserProfile u3 = new UserProfile("JunitU3", Calendar.getInstance(),
        Calendar.getInstance());

    DbUser.INSTANCE.createUserProfile(u1);
    DbUser.INSTANCE.createUserProfile(u2);
    DbUser.INSTANCE.createUserProfile(u3);

    DbUserGroupProfile.INSTANCE.getUserGroupProfile(ugn1);
    ugp2 = DbUserGroupProfile.INSTANCE.getUserGroupProfile(ugn2);
    DbUserGroupProfile.INSTANCE.getUserGroupProfile(ugn3);

    u1 = DbUser.INSTANCE.getUserProfile("JunitU1");
    DbUser.INSTANCE.getUserProfile("JunitU2");
    u3 = DbUser.INSTANCE.getUserProfile("JunitU3");

    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    // no delta.
    DbUser.INSTANCE.setUserMembership("JunitU1", u1.getMembershipData());

    MembershipData m1 = u1.getMembershipData();
    Set<UserGroupName> groups = m1.getMemberUserGroupName();
    groups.add(ugn1);
    groups.add(ugn2);

    // Add two groups
    DbUser.INSTANCE.setUserMembership("JunitU1", u1.getMembershipData());
    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    u1 = DbUser.INSTANCE.getUserProfile("JunitU1");
    m1 = u1.getMembershipData();
    groups = m1.getMemberUserGroupName();
    groups.remove(ugn1);
    groups.remove(ugn2);
    DbUser.INSTANCE.setUserMembership("JunitU1", u1.getMembershipData());
    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    groups = m1.getMemberUserGroupName();
    groups.add(ugn1);
    groups.add(ugn2);
    DbUser.INSTANCE.setUserMembership("JunitU1", u1.getMembershipData());
    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    groups = u3.getMembershipData().getMemberUserGroupName();
    groups.add(ugn1);
    groups.add(ugn2);
    DbUser.INSTANCE.setUserMembership("JunitU3", u1.getMembershipData());
    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    // no change
    DbUserGroupProfile.INSTANCE.setUserGroupMembership(ugn2,
        ugp2.getMembership());
    ugp2.getMembership().getMemberResourceGroupName().add("JunitRG1");
    ugp2.getMembership().getMemberResourceGroupName().add("JunitRG2");
    ugp2.getMembership().getMemberResourceGroupName().add("JunitRG3");
    ugp2.getMembership().getMemberUserID().add("JunitU1");
    ugp2.getMembership().getMemberUserID().add("JunitU2");
    ugp2.getMembership().getMemberUserID().add("JunitU3");
    DbUserGroupProfile.INSTANCE.setUserGroupMembership(ugn2,
        ugp2.getMembership());
    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    ugp2 = DbUserGroupProfile.INSTANCE.getUserGroupProfile(ugn2);
    ugp2.getMembership().getMemberResourceGroupName().remove("JunitRG1");
    ugp2.getMembership().getMemberUserID().remove("JunitU1");
    DbUserGroupProfile.INSTANCE.setUserGroupMembership(ugn2,
        ugp2.getMembership());
    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    // no change
    rgp2 = DbResourceGroupProfile.INSTANCE.getResourceGroupProfile("JunitRG2");
    DbResourceGroupProfile.INSTANCE.setResourceGroupMembership("JunitRG2",
        rgp2.getMembership());
    rgp2.getMembership().getMemberUserGroupName().add(ugn1);
    rgp2.getMembership().getMemberUserGroupName().add(ugn2);
    rgp2.getMembership().getMemberUserGroupName().add(ugn3);
    DbResourceGroupProfile.INSTANCE.setResourceGroupMembership("JunitRG2",
        rgp2.getMembership());
    rgp2.getMembership().getMemberUserGroupName().remove(ugn2);
    DbResourceGroupProfile.INSTANCE.setResourceGroupMembership("JunitRG2",
        rgp2.getMembership());

    DbUserGroupProfile.INSTANCE.getUserGroupProfileListByMember();

    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    DbUser.INSTANCE.deleteUserProfile("JunitU3");
    DbResourceGroupProfile.INSTANCE.deleteResourceGroupProfile("JunitRG3");
    DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(ugn1);
    DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(ugn2);
    DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(ugn3);
    DbUser.INSTANCE.deleteUserProfile("JunitU2");
    DbResourceGroupProfile.INSTANCE.deleteResourceGroupProfile("JunitRG2");
    DbResourceGroupProfile.INSTANCE.deleteResourceGroupProfile("JunitRG1");
    DbUser.INSTANCE.deleteUserProfile("JunitU1");
  }

  @Test
  public void userGroupProfileNullandEmptyTests() throws Exception {
    try {
      DbUserGroupProfile.INSTANCE.createUserGroupProfile(null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      UserGroupProfile ugp = DbUserGroupProfile.INSTANCE
          .getUserGroupProfile(new UserGroupName(
              DbUserGroupProfile.DEFAULT_USER_GROUP));
      DbUserGroupProfile.INSTANCE.createUserGroupProfile(ugp);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      UserGroupProfile ugp = new UserGroupProfile(new UserGroupName("bogus"),
          Calendar.getInstance(), Calendar.getInstance(), "",
          new UserGroupName(DbUserGroupProfile.DEFAULT_USER_GROUP));
      ugp.getMembership().setCreatedByGroupName(null);
      DbUserGroupProfile.INSTANCE.createUserGroupProfile(ugp);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(new UserGroupName(
        "nOSuchUserEverExistedIHope"));

    try {
      DbUserGroupProfile.INSTANCE.getUserGroupProfile(null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    DbUserGroupProfile.INSTANCE.getUserGroupProfileListByMember();

    try {
      DbUserGroupProfile.INSTANCE.setUserGroupMembership(null, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUserGroupProfile.INSTANCE.setUserGroupMembership(new UserGroupName(
          "bogusBetterNotExist"), null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUserGroupProfile.INSTANCE.setUserGroupMembership(new UserGroupName(
          "bogusBetterNotExist"), new MembershipData("bob"));
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUserGroupProfile.INSTANCE.setUserGroupUserGroupPolicy(null, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUserGroupProfile.INSTANCE.setUserGroupUserGroupPolicy(
          new UserGroupName("bogusBetterNotExist"), null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUserGroupProfile.INSTANCE.setUserGroupUserGroupPolicy(
          new UserGroupName("bogusBetterNotExist"), new GroupPolicy());
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }
  }

  @Test
  public void userNullandEmptyTests() throws Exception {
    try {
      DbUser.INSTANCE.createUserProfile(null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.deleteUserProfile(null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.getUserProfile(null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setupUserAuthenticationAccount(null, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setupUserAuthenticationAccount("Non-ExistantUser", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserAccountStatus("Non-ExistantUser", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserAccountStatus(DEFAULT_USER, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserAuthenticationData("Non-ExistantUser", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserAuthenticationData(DEFAULT_USER, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserAuthenticationState("Non-ExistantUser", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserAuthenticationState(DEFAULT_USER, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserMembership(null, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserMembership(DEFAULT_USER, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserMembership("Non-ExistantUser", new MembershipData(
          DbUserGroupProfile.DEFAULT_USER_GROUP));
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserOrganization("Non-ExistantUser", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserOrganization(DEFAULT_USER, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserPersonalData(DEFAULT_USER, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserPersonalData("Non-ExistantUser", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserTimeZoneIDPreference("Non-ExistantUser", null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

    try {
      DbUser.INSTANCE.setUserTimeZoneIDPreference(DEFAULT_USER, null);
      fail(ITS_NULL);
    }
    catch (Exception e) {

    }

  }

  @Test
  public void userTest() throws Exception {
    DbUser.INSTANCE.deleteUserProfile("JunitUP");
    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    UserProfile up = new UserProfile("JunitUP", Calendar.getInstance(),
        Calendar.getInstance());
    up.setupUserAuthenticationAccount(
        DbGlobalPolicy.INSTANCE.getGlobalPolicy(), new LocalAccountCredential(
            "admin", "myDrac", new IPAddress(null, null)));
    up.setAccountStatus(new AccountStatus());
    MembershipData md = new MembershipData(
        DbUserGroupProfile.DEFAULT_USER_GROUP);
    md.setMemberUserGroupName(new HashSet<UserGroupName>(Arrays
        .asList(new UserGroupName[] { new UserGroupName(
            DbUserGroupProfile.DEFAULT_USER_GROUP) })));

    up.setMembershipData(md);
    DbUser.INSTANCE.createUserProfile(up);

    OrganizationData od = new OrganizationData();
    od.setCategory("Category");
    od.setDescription("Description");
    od.setOrgName("setOrgName");
    od.setOrgUnitName("setOrgUnitName");
    od.setOwner("setOwner");
    od.setSeeAlso("setSeeAlso");
    PersonalData pd = new PersonalData();
    pd.setAddress(new PersonalData.Address("phone", "mail@mail",
        "15 someStreet, St, Ottawa,on"));
    pd.setDescription("setDescription");
    pd.setName(new PersonalData.UserName("commonName", "givenname", "surName"));
    pd.setTitle("title");

    
    DbUser.INSTANCE.setUserOrganization("JunitUP", od);

    
    DbUser.INSTANCE.setUserPersonalData("JunitUP", pd);

    
    DbUser.INSTANCE.setUserAccountStatus("JunitUP", new AccountStatus(
        AccountState.DISABLED, "becase"));
    
    DbUser.INSTANCE.setUserAccountStatus("JunitUP", new AccountStatus(
        AccountState.ENABLED, "becase"));

    AuthenticationData ad = new AuthenticationData();
    AuthenticationAuditData aad = new AuthenticationAuditData();
    aad.setNumOfInvalidAttempts(1);
    aad.addLoginAddress(new IPAddress("127.0.0.1", "80"));
    aad.addLocationOfInvalidAttempt(new IPAddress("127.0.0.1", "1000"));
    ad.setAuditData(aad);
    ad.setAuthenticationState(AuthenticationState.LOCKED);
    ad.setAuthenticationType(AuthenticationType.INTERNAL);
    InternalAccountData iad = new InternalAccountData();
    iad.setExpirationDate(Calendar.getInstance());
    iad.setLastPasswordChanged(Calendar.getInstance());
    iad.setUserPassword("bigDeal");
    ad.setInternalAccountData(iad);

    ad.setWSDLCredential("12345");
    ad.getUserAccountPolicy().setDormantPeriod(Integer.valueOf(1000));
    ad.getUserAccountPolicy().setInactivityPeriod(Integer.valueOf(10000000));
    ad.getUserAccountPolicy().setMaxInvalidLoginAttempts(Integer.valueOf(100));
    ad.getUserAccountPolicy().setLockoutPeriod(Integer.valueOf(10));
    PasswordPolicy ppp = new PasswordPolicy();
    ppp.setPwAging(Integer.valueOf(9999));
    ppp.setPwExpiredNotif(Integer.valueOf(10));
    ppp.setPwHistorySize(Integer.valueOf(10));
    ppp.setPwInvalids(Arrays.asList(new String[] { "big", "bad", "bob" }));
    ppp.setPwRules("badRule");
    ad.getUserAccountPolicy().setLocalPasswordPolicy(ppp);
    ad.getUserAccountPolicy().setLockedClientIPs(
        Arrays.asList(new String[] { "127.10.10.10", "192.192.192.1" }));

    
    DbUser.INSTANCE.setUserAuthenticationData("JunitUP", ad);
    DbUser.INSTANCE
        .setupUserAuthenticationAccount("JunitUP", new LocalAccountCredential(
            "admin", "mydrac", new IPAddress(null, null)));
    UserAccountPolicy acp = new UserAccountPolicy();
    acp.setDormantPeriod(Integer.valueOf(25));
    acp.setInactivityPeriod(Integer.valueOf(25));
    acp.setLockoutPeriod(Integer.valueOf(25));
    acp.setMaxInvalidLoginAttempts(Integer.valueOf(25));
    acp.setLockedClientIPs(Arrays.asList(new String[] { "127.0.0.99" }));
    acp.setLocalPasswordPolicy(new PasswordPolicy());

    DbUser.INSTANCE.setUserAuthenticationState("JunitUP",
        AuthenticationState.VALID);

    
    DbUser.INSTANCE.setUserTimeZoneIDPreference("JunitUP", "US/Eastern");
    log.debug("AuditUserProfileLinkages returns :"
        + ProfileValidator.auditUserProfileLinkages());

    DbUser.INSTANCE.deleteUserProfile("JunitUP");
  }
}
