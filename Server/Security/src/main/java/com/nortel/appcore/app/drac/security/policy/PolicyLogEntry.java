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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;

public final class PolicyLogEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(PolicyLogEntry.class);

	private PolicyLogEntry() {
	}

	public static void createLogEntry(UserDetails userDetails, String resourceId,
	    LogKeyEnum key) throws Exception {
		createLogEntry(userDetails, resourceId, key, null);
	}

	public static void createLogEntry(UserDetails userDetails, String resourceId,
	    LogKeyEnum key, String[] args) throws Exception {
		if (userDetails == null) {
			log.error("Userdetails should not be null");
			return;
		}

		DbLog.INSTANCE.addLog(
		    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
		        null, resourceId, key, args));
	}

}
