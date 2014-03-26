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

package com.nortel.appcore.app.drac.security;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.database.dracdb.DbResourceGroupProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbUser;
import com.nortel.appcore.app.drac.database.dracdb.DbUserGroupProfile;

public class SecurityServerTest {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @BeforeClass
  public static void setupTests() throws Exception {
    TestHelper.INSTANCE.initialize();
    DbUserGroupProfile.INSTANCE.deleteAll();
    DbUser.INSTANCE.deleteAll();
    DbResourceGroupProfile.INSTANCE.deleteAll();
  }

  @Test
  public void testAuditUserProfileLinkages() throws Exception {
    TestHelper.INSTANCE.initialize();
    log.debug("auditUserProfileLinkages returns: "
        + SecurityServer.INSTANCE.auditUserProfileLinkages(null));
  }

  // @Test
  // public void testDeleteCreateResourceGroupAndUser()
  // throws Exception
  // {
  // if (TestHelper.disabled())
  // {
  // return;
  // }
  // SecurityServer p = SecurityServer.INSTANCE;
  //
  // p.auditUserProfileLinkages();
  // p.deleteResourceGroupProfile("JunitResource");
  // p.deleteUserGroupProfile(new UserGroupName("JunitGroup"));
  // p.deleteUserProfile("JunitUser");
  //
  // ResourceGroupProfile rgp = new ResourceGroupProfile("JunitResource",
  // Calendar.getInstance(),
  // Calendar.getInstance(),
  // "SystemAdminResourceGroup", "SystemAdminResourceGroup");
  // p.createResourceGroupProfile(rgp);
  //
  // UserGroupProfile ugp = new UserGroupProfile(new
  // UserGroupName("JunitGroup"), Calendar.getInstance(),
  // Calendar
  // .INSTANCE, "admin", new UserGroupName("SystemAdminGroup"));
  //
  // GroupPolicy gp = new GroupPolicy();
  // BandwidthControlRule b = new BandwidthControlRule();
  // b.setMaximumAggregateServiceSize(Integer.valueOf(10000));
  // b.setMaximumServiceBandwidth(Integer.valueOf(10000));
  // b.setMaximumServiceDuration(Integer.valueOf(10000));
  // b.setMaximumServiceSize(Integer.valueOf(10000));
  // b.setRuleID("id");
  // gp.setBandwidthControlRule(b);
  // AccessRule a = new AccessRule(AccessPermission.GRANT);
  // gp.setAccessControlRule(Arrays.asList(new AbstractRule[]
  // { a }));
  // // gp.setSystemAccessRule()
  // ugp.setGroupPolicy(gp);
  // ugp.setUserGroupType(UserGroupType.GROUP_ADMIN);
  // ugp.setMembership(new MembershipData("admin"));
  // ugp.setReferencingUserGroupName(new UserGroupName("SystemAdminGroup"));
  //
  // p.createUserGroupProfile(ugp);
  //
  // p.createUserProfile("JunitUser", new LocalAccountCredential("admin",
  // "myDrac", new
  // IPAddress("127.0.0.1", "10001")),
  // new AccountStatus());
  //
  // // p.createUserProfile(up);
  //
  // p.auditUserProfileLinkages();
  // p.deleteResourceGroupProfile("JunitResource");
  // p.deleteUserGroupProfile(new UserGroupName("JunitGroup"));
  // p.deleteUserProfile("JunitUser");
  // p.auditUserProfileLinkages();
  // }

  @Test
  public void testGetInstance() {
    TestHelper.INSTANCE.initialize();
  }

  @Test
  public void testLoginValidateLogout() throws Exception {
    TestHelper.INSTANCE.initialize();
    log.debug("Testing new login methods");
    String fakeUseslessSessionId = ""+new Date().getTime();
    
    
    
    log.debug("Props: "+System.getProperties());
    
    LoginToken tok = SecurityServer.INSTANCE.login(
        ClientLoginType.ADMIN_CONSOLE_LOGIN, "admin", "myDrac".toCharArray(),
        null, null, fakeUseslessSessionId);
    log.debug("got tok=" + tok.toString());

    SecurityServer.INSTANCE.sessionValidate(tok);
    SecurityServer.INSTANCE.logout(tok);

    tok = SecurityServer.INSTANCE.login(ClientLoginType.ADMIN_CONSOLE_LOGIN,
        "admin", "myDrac".toCharArray(), "127.0.0.1:8000", "bogusTicketString", fakeUseslessSessionId);
    log.debug("got tok=" + tok.toString());
    SecurityServer.INSTANCE.sessionValidate(tok);
    SecurityServer.INSTANCE.logout(tok);

    try {
      tok = SecurityServer.INSTANCE.login(ClientLoginType.ADMIN_CONSOLE_LOGIN,
          "admin", "joke".toCharArray(), null, null, fakeUseslessSessionId);
      fail("unexpected pass");
    }
    catch (Exception e) {
    }

    try {
      tok = SecurityServer.INSTANCE.login(ClientLoginType.ADMIN_CONSOLE_LOGIN,
          "bob", "joke".toCharArray(), null, null, fakeUseslessSessionId);
      fail("unexpected pass");
    }
    catch (Exception e) {
    }
  }

}
