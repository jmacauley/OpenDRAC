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

import java.util.Calendar;

import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbUserGroupProfile;
import com.nortel.appcore.app.drac.database.helper.ProfileValidator;

/**
 * @author pitman
 */
public class HierarchicalContainmentPolicyTest {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private static final String DEFAULT_USER = "admin";

	@Test
	public void testGetInstance() {
		TestHelper.INSTANCE.initialize();
	}

	@Test
	public void testInvalidate() {
		TestHelper.INSTANCE.initialize();
		HierarchicalContainmentPolicy.INSTANCE.invalidate();
	}

	@Test
	public void testMethods() throws Exception {
		TestHelper.INSTANCE.initialize();
		UserGroupProfile u = DbUserGroupProfile.INSTANCE.getUserGroupProfile(
		    new UserGroupName(DbUserGroupProfile.DEFAULT_USER_GROUP));
		log.debug("isUserGroupALeaf for user " + u.getName() + " returns "
		    + HierarchicalContainmentPolicy.INSTANCE.isUserGroupALeaf(u));
		log.debug("isUserGroupARoot for user +" + u.getName() + " returns "
		    + HierarchicalContainmentPolicy.INSTANCE.isUserGroupARoot(u));
		log.debug("ResourceGroupProfileTree="
		    + HierarchicalContainmentPolicy.INSTANCE
		        .getResourceGroupProfileTree().toString());
		log.debug("UserGroupProfileTree="
		    + HierarchicalContainmentPolicy.INSTANCE.getUserGroupProfileTree()
		        .toString());

		UserGroupName ugn = new UserGroupName("JunitUser999");
		DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(ugn);

		UserGroupProfile ugp1 = new UserGroupProfile(ugn, Calendar.getInstance(),
		    Calendar.getInstance(), DEFAULT_USER, new UserGroupName(
		        DbUserGroupProfile.DEFAULT_USER_GROUP));
		ugp1.setReferencingUserGroupName(ugn);
		DbUserGroupProfile.INSTANCE.createUserGroupProfile(ugp1);

		ProfileValidator.auditUserProfileLinkages();
		HierarchicalContainmentPolicy.INSTANCE.invalidate();
		log.debug("ResourceGroupProfileTree="
		    + HierarchicalContainmentPolicy.INSTANCE
		        .getResourceGroupProfileTree().toString());
		log.debug("UserGroupProfileTree="
		    + HierarchicalContainmentPolicy.INSTANCE.getUserGroupProfileTree()
		        .toString());

		log.debug("getUserGroupProfileNode("
		    + ugn
		    + ") returns "
		    + HierarchicalContainmentPolicy.INSTANCE.getUserGroupProfileNode(
		        ugn));

		log.debug("isUserGroupALeaf for user " + ugp1.getName() + " returns "
		    + HierarchicalContainmentPolicy.INSTANCE.isUserGroupALeaf(ugp1));
		DbUserGroupProfile.INSTANCE.deleteUserGroupProfile(ugn);
	}
}
