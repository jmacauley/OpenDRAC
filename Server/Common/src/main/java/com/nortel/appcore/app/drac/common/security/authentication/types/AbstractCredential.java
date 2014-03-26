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

import java.io.Serializable;

import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;

public abstract class AbstractCredential implements Serializable {

	private static final long serialVersionUID = -238156449510308768L;
	private final AuthenticateMethod authenticateMethod;
	private final String userID;
	private String password;
	private final LoginAuditData loginAuditData;
	private final IPAddress ipAddress;
	private long loginTime;

	public static enum AuthenticateMethod {
		LOCAL_ACCOUNT(1, "local account"), //
		WEB_SERVICES(2, "web services");//

		private static final long serialVersionUID = 1;
		private final int code;
		private final String description;

		AuthenticateMethod(int code, String description) {
			this.code = code;
			this.description = description;
		}

		public int getCode() {
			return code;
		}

		@Override
		public String toString() {
			return this.description;
		}
	}

	public static class LoginAuditData implements Serializable {
		private static final long serialVersionUID = 1L;
		protected int loginAttempts;
		protected int expiringNotificationDays;

		public LoginAuditData() {
			this.loginAttempts = 0;
		}


		public int getExpiringNotificationDays() {
			return expiringNotificationDays;
		}

		public int getLoginAttempts() {
			return loginAttempts;
		}

		public void setExpiringNotificationDays(int expiringNotificationDays) {
			this.expiringNotificationDays = expiringNotificationDays;
		}

		public void setInvalidLoginAttempts(int attempts) {
			this.loginAttempts = attempts;
		}
	}

	public AbstractCredential(String userName, String userPassword,
	    IPAddress clientIp, AuthenticateMethod authMethod) {
		userID = userName;
		password = userPassword;
		loginAuditData = new LoginAuditData();
		ipAddress = clientIp;
		authenticateMethod = authMethod;
	}

	public AuthenticateMethod getAuthenticateMethod() {
		return authenticateMethod;
	}

	public IPAddress getIpAddress() {
		return ipAddress;
	}

	public LoginAuditData getLoginAuditData() {
		return loginAuditData;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public String getPassword() {
		return password;
	}

	public String getUserID() {
		return userID;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "AbstractCredential " + this.getClass().getName() + " "
		    + authenticateMethod;
	}
}
