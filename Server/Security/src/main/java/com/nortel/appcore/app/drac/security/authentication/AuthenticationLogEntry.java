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

package com.nortel.appcore.app.drac.security.authentication;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;

public final class AuthenticationLogEntry implements Serializable {
  private static final Logger log = LoggerFactory.getLogger(AuthenticationLogEntry.class);
	private static final long serialVersionUID = 1L;

	private AuthenticationLogEntry() {
	}

	public static void createLogEntry(UserDetails userDetails, LogKeyEnum key) {
		createLogEntry(userDetails, key, null);
	}

	public static void createLogEntry(UserDetails userDetails, LogKeyEnum key,
	    String[] args) {
		if (userDetails == null) {
			log.error("Userdetails should not be null");
			return;
		}
		if (key == null) {
			log.error("key should not be null");
			return;
		}

		DbLog.INSTANCE.generateLog(
		    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
		        null, userDetails.getUserID(), key, args));
	}

	public static void createLogEntryForAuthenticate(UserDetails userDetails) {
		if (userDetails == null) {
			log.error("User details should not be null");
			return;
		}
		SessionErrorCode code = userDetails.getErrorCode();
		// handle authentication cases first
		if (code == SessionErrorCode.NO_ERROR) {
			createLogEntry(userDetails, LogKeyEnum.KEY_AUTH_SUCCESS, null);
		}
		else if (code == SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_TO_BE_LOCKEDOUT) {
			createLogEntry(userDetails, LogKeyEnum.KEY_AUTH_FAILED,
			    new String[] { "Invalid user ID or password" });
		}
		else if (code == SessionErrorCode.ERROR_INVALID_USERID_OR_PASSWORD_AND_RETRY) {
			createLogEntry(userDetails, LogKeyEnum.KEY_AUTH_FAILED,
			    new String[] { "Invalid user ID or password" });
		}
		else if (code == SessionErrorCode.ERROR_ASELECT_COMMUNICATION_ERROR) {
			createLogEntry(userDetails, LogKeyEnum.KEY_AUTH_FAILED,
			    new String[] { "A-Select communication error" });

			// unauthorized to login logs
		}
		else if (code == SessionErrorCode.ERROR_LOCKOUT) {
			createLogEntry(userDetails, LogKeyEnum.KEY_LOGIN_FAILED,
			    new String[] { "User in lockout" });
		}
		else if (code == SessionErrorCode.ERROR_ACCOUNT_DISABLED) {
			createLogEntry(userDetails, LogKeyEnum.KEY_LOGIN_FAILED,
			    new String[] { "Account is disabled" });
		}
		else if (code == SessionErrorCode.ERROR_INVALID_AUTHENTICATION_TYPE) {
			createLogEntry(userDetails, LogKeyEnum.KEY_AUTH_FAILED,
			    new String[] { "Invalid authentication type" });
		}
		else if (code == SessionErrorCode.ERROR_DORMANT) {
			createLogEntry(userDetails, LogKeyEnum.KEY_LOGIN_FAILED,
			    new String[] { "Account is dormant" });
		}
		else if (code == SessionErrorCode.ERROR_NO_POLICY) {
			createLogEntry(userDetails, LogKeyEnum.KEY_LOGIN_FAILED,
			    new String[] { "No policy" });
		}
		else if (code == SessionErrorCode.ERROR_LOCKED_CLIENT_IP_ADDRESSS) {
			createLogEntry(userDetails, LogKeyEnum.KEY_LOGIN_FAILED,
			    new String[] { "Log in attempt from locked IP address" });
		}
		else if (code == SessionErrorCode.ERROR_LOCKED_CLIENT_SESSION) {		
			createLogEntry(userDetails, LogKeyEnum.KEY_LOGIN_FAILED,
			    new String[] { "Log in attempt from locked session" });
		}		
		else if (code == SessionErrorCode.ERROR_GENERAL) {
			createLogEntry(userDetails, LogKeyEnum.KEY_AUTH_FAILED,
			    new String[] { "Unknown" });
		}
	}
}
