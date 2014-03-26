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

public final class SessionCodes {
	public static enum SessionErrorCode {
		NO_ERROR("Successfully executed"), ERROR_INVALID_USERID_OR_PASSWORD_AND_RETRY(
		    "Invalid userid or password"), ERROR_INVALID_USERID_OR_PASSWORD_AND_TO_BE_LOCKEDOUT(
		    "Invalid userid or password"), ERROR_SESSION_EXPIRED("Session expired"), ERROR_PASSWORD_EXPIRED(
		    "Password expired"), ERROR_DORMANT("Dormant is under effect"), ERROR_LOCKOUT(
		    "Lockout is under effect"), ERROR_ACCOUNT_DISABLED(
		    "User account is disabled"), ERROR_GENERAL(
		    "General error. See logs for details"), ERROR_NO_POLICY(
		    "This user does not have policy"), ERROR_INVALID_AUTHENTICATION_TYPE(
		    "Authentication type is invalid"), ERROR_ASELECT_COMMUNICATION_ERROR(
		    "A-Select comunication failed"), ERROR_LOCKED_CLIENT_IP_ADDRESSS(
		    "Locked client ip address"),ERROR_LOCKED_CLIENT_SESSION(
		    "Locked client http session");

		private final String description;

		SessionErrorCode(String desc) {
			this.description = desc;
		}

		@Override
		public String toString() {
			return this.description;
		}
	}

	public static enum SessionWarningCode {
		PASSWORD_EXPIRING_NOTIFICATION
	}
}
