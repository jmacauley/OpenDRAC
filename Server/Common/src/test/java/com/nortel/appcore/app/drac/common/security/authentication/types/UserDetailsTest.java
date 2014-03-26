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

package com.nortel.appcore.app.drac.common.security.authentication.types;

import org.junit.Test;

import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionWarningCode;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;

public class UserDetailsTest {

	@Test
	public void testUserDetails() {
		UserDetails ud = new UserDetails("uid", "sessionId",
		    new LocalAccountCredential("admin", "myDrac", new IPAddress(
		        "127.0.0.1", "8443")));
		ud.addWarningCode(SessionWarningCode.PASSWORD_EXPIRING_NOTIFICATION);
		ud.getCredential();
		ud.getErrorCode();
		ud.getLoginAddress();
		ud.getLoginIPAddress();
		ud.getUserID();
		ud.getUserPolicyProfile();
		ud.getWarningCode();
		ud.setErrorCode(SessionErrorCode.ERROR_ACCOUNT_DISABLED);
		ud.setUserPolicyProfile(null);
		ud.toString();
	}

}
