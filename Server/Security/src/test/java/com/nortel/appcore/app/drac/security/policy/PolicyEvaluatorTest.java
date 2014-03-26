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

package com.nortel.appcore.app.drac.security.policy;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.authentication.types.LocalAccountCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest.CommandType;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationAuditData;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
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
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AccountState;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationState;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationType;
import com.nortel.appcore.app.drac.common.types.ControllerType;
import com.nortel.appcore.app.drac.database.dracdb.DbGlobalPolicy;
import com.nortel.appcore.app.drac.database.dracdb.DbResourceGroupProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbUser;
import com.nortel.appcore.app.drac.database.dracdb.DbUserGroupProfile;
import com.nortel.appcore.app.drac.database.helper.ProfileValidator;
import com.nortel.appcore.app.drac.database.helper.UserPolicyUtility;

/**
 * @author pitman
 */
public final class PolicyEvaluatorTest {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final String DEFAULT_USER = "admin";

	/*
	 * Add to the database a several test users, user groups and resource groups
	 * to test against
	 */

	public void createTestUsers() throws Exception {
		TestHelper.INSTANCE.initialize();
		removeTestUsers();
		log.debug("AuditUserProfileLinkages returns :"
		    + ProfileValidator.auditUserProfileLinkages());

		/* Create a group admin JunitUG */
		UserGroupProfile ugp = new UserGroupProfile(new UserGroupName("JunitUG"),
		    Calendar.getInstance(), Calendar.getInstance(), "admin",
		    new UserGroupName(DbUserGroupProfile.DEFAULT_USER_GROUP));
		ugp.getMembership().setCreatedByGroupName(
		    DbUserGroupProfile.DEFAULT_USER_GROUP);
		ugp.setUserGroupType(UserGroupType.GROUP_ADMIN);
		ugp.getMembership().setMemberResourceGroupName(
		    new HashSet<String>(Arrays
		        .asList(new String[] { "JunitResourceGroup" })));
		ugp.setReferencingUserGroupName(new UserGroupName(
		    DbUserGroupProfile.DEFAULT_USER_GROUP));
		DbUserGroupProfile.INSTANCE.createUserGroupProfile(ugp);

		/* Create a group JunitUG1 */
		UserGroupProfile ugp1 = new UserGroupProfile(new UserGroupName("JunitUG1"),
		    Calendar.getInstance(), Calendar.getInstance(), "admin",
		    new UserGroupName(DbUserGroupProfile.DEFAULT_USER_GROUP));
		ugp1.getMembership().setCreatedByGroupName(ugp.getName().toString());
		ugp1.getMembership().setMemberResourceGroupName(
		    new HashSet<String>(Arrays
		        .asList(new String[] { "JunitResourceGroup" })));
		ugp1.setUserGroupType(UserGroupType.USER);
		ugp1.setReferencingUserGroupName(ugp.getName());
		DbUserGroupProfile.INSTANCE.createUserGroupProfile(ugp1);

		/* Create a new resource group */
		ResourceGroupProfile rgp = new ResourceGroupProfile("JunitResourceGroup",
		    Calendar.getInstance(), Calendar.getInstance(), DEFAULT_USER,
		    DbResourceGroupProfile.DEFAULT_RESOURCE_GROUP);

		DbResourceGroupProfile.INSTANCE.createResourceGroupProfile(rgp);

		/* Create a user JunitUP belonging to no group */
		{
			UserProfile up = new UserProfile("JunitUP", Calendar.getInstance(),
			    Calendar.getInstance());
			up.setupUserAuthenticationAccount(DbGlobalPolicy.INSTANCE
			    .getGlobalPolicy(), new LocalAccountCredential("admin", "myDrac",
			    new IPAddress(null, null)));
			up.setAccountStatus(new AccountStatus());
			MembershipData md = new MembershipData(null);
			md.setMemberUserGroupName(new HashSet<UserGroupName>(Arrays
			    .asList(new UserGroupName[] {
			        new UserGroupName(DbUserGroupProfile.DEFAULT_USER_GROUP),
			        ugp1.getName() })));

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
			DbUser.INSTANCE.setUserAccountStatus("JunitUP",
			    new AccountStatus(AccountState.ENABLED, "becase"));

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
			ad.getUserAccountPolicy()
			    .setMaxInvalidLoginAttempts(Integer.valueOf(100));
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
			DbUser.INSTANCE.setupUserAuthenticationAccount(
			    "JunitUP",
			    new LocalAccountCredential("admin", "mydrac", new IPAddress(null,
			        null)));
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
		}

		/*
         * 
         */

		/* Create a user JunitUP belonging to group head */
		{
			UserProfile up = new UserProfile("JunitUP1", Calendar.getInstance(),
			    Calendar.getInstance());
			up.setupUserAuthenticationAccount(DbGlobalPolicy.INSTANCE
			    .getGlobalPolicy(), new LocalAccountCredential("admin", "myDrac",
			    new IPAddress(null, null)));
			up.setAccountStatus(new AccountStatus());
			MembershipData md = new MembershipData(null);
			md.setMemberUserGroupName(new HashSet<UserGroupName>(Arrays
			    .asList(new UserGroupName[] { ugp.getName() })));

			up.setMembershipData(md);
			DbUser.INSTANCE.createUserProfile(up);
		}

		{
			UserProfile up = new UserProfile("JunitUP2", Calendar.getInstance(),
			    Calendar.getInstance());
			up.setupUserAuthenticationAccount(DbGlobalPolicy.INSTANCE
			    .getGlobalPolicy(), new LocalAccountCredential("admin", "myDrac",
			    new IPAddress(null, null)));
			up.setAccountStatus(new AccountStatus());
			MembershipData md = new MembershipData(null);
			md.setMemberUserGroupName(new HashSet<UserGroupName>(Arrays
			    .asList(new UserGroupName[] { ugp1.getName() })));

			up.setMembershipData(md);
			DbUser.INSTANCE.createUserProfile(up);
		}

		log.debug("AuditUserProfileLinkages returns :"
		    + ProfileValidator.auditUserProfileLinkages());

	}

	public void removeTestUsers() throws Exception {
		DbResourceGroupProfile.INSTANCE.deleteResourceGroupProfile(
		    "JunitResourceGroup");
		DbUser.INSTANCE.deleteUserProfile("JunitUP");
		DbUser.INSTANCE.deleteUserProfile("JunitUP1");
		DbUser.INSTANCE.deleteUserProfile("JunitUP2");
		DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(
		    new UserGroupName("JunitUG"));
		DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(
		    new UserGroupName("JunitUG1"));

	}

	@Test
	public void testEvaluate() throws Exception {
		TestHelper.INSTANCE.initialize();

		createTestUsers();
		try {
			PolicyEvaluator.evaluate(null, null);
			fail("null!");
		}
		catch (Exception e) {
			
		}

		UserDetails ud = new UserDetails("id", "uid", null);

		@SuppressWarnings("unused")
		LocalAccountCredential credential = new LocalAccountCredential("admin",
		    "myDrac", new IPAddress("127.0.0.1", null));

		try {
			PolicyEvaluator.evaluate(ud, null);
			fail("null!");
		}
		catch (Exception e) {
			
		}

		ud.setUserPolicyProfile(UserPolicyUtility.INSTANCE
		    .getUserPolicyProfile("admin"));

		try {
			PolicyEvaluator.evaluate(ud, null);
			fail("null!");
		}
		catch (Exception e) {
			
		}

		PolicyRequest pr = new PolicyRequest(null, CommandType.READ);
		try {
			PolicyEvaluator.evaluate(ud, pr);
			fail("null!");
		}
		catch (Exception e) {
			
		}

		try {
			PolicyEvaluator.evaluate(ud, new PolicyRequest(new ControllerType(),
			    CommandType.EDIT));
			fail("null!");
		}
		catch (Exception e) {
			
		}

		/*
		 * For all users in the database ask the question, can that user
		 * create,delete,modify the given user?
		 */
		for (UserProfile u : DbUser.INSTANCE.getUserProfileList()) {
			UserDetails me = new UserDetails("id", "uid", null);
			me.setUserPolicyProfile(UserPolicyUtility.INSTANCE
			    .getUserPolicyProfile(u.getUserID()));

			for (UserProfile user : DbUser.INSTANCE.getUserProfileList()) {
				for (CommandType c : CommandType.values()) {
					try {
						PolicyEvaluator.evaluate(me, new PolicyRequest(user, c));
					}
					catch (Exception e) {
						
					}
				}
			}
		}

		/*
		 * For all users in the database ask the question, can that user
		 * create,delete,modify the given user group profile?
		 */
		for (UserProfile u : DbUser.INSTANCE.getUserProfileList()) {
			UserDetails me = new UserDetails("id", "uid", null);
			me.setUserPolicyProfile(UserPolicyUtility.INSTANCE
			    .getUserPolicyProfile(u.getUserID()));

			for (UserGroupProfile group : DbUserGroupProfile.INSTANCE
			    .getUserGroupProfileList()) {
				for (CommandType c : CommandType.values()) {
					try {
						PolicyEvaluator.evaluate(me, new PolicyRequest(group, c));
					}
					catch (Exception e) {
						
					}
				}
			}
		}

		/*
		 * For all users in the database ask the question, can that user
		 * create,delete,modify the given user group profile?
		 */
		for (UserProfile u : DbUser.INSTANCE.getUserProfileList()) {
			UserDetails me = new UserDetails("id", "uid", null);
			me.setUserPolicyProfile(UserPolicyUtility.INSTANCE
			    .getUserPolicyProfile(u.getUserID()));

			for (ResourceGroupProfile group : DbResourceGroupProfile.INSTANCE
			    .getResourceGroupProfileList()) {
				for (CommandType c : CommandType.values()) {
					try {
						PolicyEvaluator.evaluate(me, new PolicyRequest(group, c));
					}
					catch (Exception e) {
						
					}
				}
			}
		}

		removeTestUsers();
	}
}
