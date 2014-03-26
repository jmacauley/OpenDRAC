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

import java.io.Serializable;
import java.security.SecureRandom;

/**
 * A login token is generated when a user successfully logs into the server. The
 * token is then provided on later method calls to the server so that the server
 * can associate the call with the end user.
 * 
 * @author pitman
 */
public final class LoginToken implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String loginToken;
	/**
	 * This field is intended for clients to extract and print the user name, as
	 * the raw login token is useless for generating logs. Set by the server
	 * during the login process.
	 */
	private final String userName;

	/*
	 * A hard coded token that should never be considered valid from a security
	 * perspective, but is intended instead to be passed in method calls where a
	 * valid token is not required, instead of passing null or any other token. By
	 * using this token, we can search for places we are invoking methods without
	 * a credential check.
	 */
	private static final LoginToken STATIC_TOKEN = new LoginToken("666",
	    "STATIC_TOKEN");

	/**
	 * Generate a login token for a user. The token is generated using a secure
	 * random generator and is generated such that an attacker cannot determine
	 * what number will be generated in the future nor what numbers have
	 * previously been generated.
	 */
	public LoginToken(String userID) {
		userName = userID;
		SecureRandom sr = new SecureRandom();
		/*
		 * Randomly determine the how long the id will be. Since nextInt can return
		 * zero, add 8 to insure a minimum length
		 */
		int length = 8 + sr.nextInt(18);
		byte[] bytes = new byte[length];
		sr.nextBytes(bytes);
		// We have generated length bytes of random data. Convert it into a hex
		// string
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%X", b));
		}

		loginToken = sb.toString();
	}

	public LoginToken(String id, String userID) {
		userName = userID;
		loginToken = id;
		if (loginToken == null || userName == null) {
			throw new RuntimeException("Cannot be null");
		}
	}

	/**
	 * A hard coded token that should never be considered valid from a security
	 * perspective, but is intended instead to be passed in method calls where a
	 * valid token is not required, instead of passing null or any other token. By
	 * using this token, we can search for places we are invoking methods without
	 * a credential check.
	 */
	public static LoginToken getStaticToken() {
		return STATIC_TOKEN;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof LoginToken)) {
			return false;
		}
		LoginToken other = (LoginToken) obj;
		if (loginToken == null) {
			if (other.loginToken != null) {
				return false;
			}
		}
		else if (!loginToken.equals(other.loginToken)) {
			return false;
		}
		if (userName == null) {
			if (other.userName != null) {
				return false;
			}
		}
		else if (!userName.equals(other.userName)) {
			return false;
		}
		return true;
	}

	public String getId() {
		return loginToken;
	}

	public String getUser() {
		return userName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (loginToken == null ? 0 : loginToken.hashCode());
		result = prime * result + (userName == null ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LoginToken [userName=");
		builder.append(userName);
		builder.append(", loginToken=");
		builder.append(loginToken);
		builder.append("]");
		return builder.toString();
	}

}
