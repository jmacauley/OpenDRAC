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

import org.junit.Test;

import com.nortel.appcore.app.drac.common.security.authentication.types.LocalAccountCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;

public class LoginTokenCacheTest {

	@Test
	public void testLoginTokenCache() throws Exception {
		UserDetails ud = new UserDetails("", "admin", new LocalAccountCredential(
		    "admin", "myDrac", null));
		LoginToken token = new LoginToken("admin");
		LoginTokenCache.INSTANCE.addLogin(token, ud);
		LoginTokenCache.INSTANCE.getUser(token);
		LoginTokenCache.INSTANCE.updateUserDetails(token, ud);
		LoginTokenCache.INSTANCE.logoutUser(token);
		// audit an empty cache
		LoginTokenCache.INSTANCE.auditTokenCache();
		/*
		 * if we want to audit a non-empty cache we'll need to bring the database up
		 * and // really login to test this
		 */
	}
}
