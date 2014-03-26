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

package com.nortel.appcore.app.drac.common.auditlogs;

import java.sql.ResultSet;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;

/**
 * This helper class is a bridge between LogRecord and DbLog. We set the full
 * argument constructor to LogRecord as protected to stop people from creating
 * logs without using the LogKeyEnum class. This helper permits us to construct
 * a "raw" log record based on the database result set. It cannot be part of the
 * LogRecord class as we don't want sql classes being referenced.
 * 
 * @author pitman
 */
public final class LogDbHelper {

	private LogDbHelper() {
		super();
	}

	public static LogRecord helper(ResultSet rs) throws Exception {
		LogRecord log = new LogRecord(rs.getLong(DbKeys.LogKeys.TIME), //
		    rs.getString(DbKeys.LogKeys.ORIGINATOR), //
		    rs.getString(DbKeys.LogKeys.IP_ADDR), //
		    new UserGroupName(rs.getString(DbKeys.LogKeys.BILLING_GROUP)), //
		    LogRecord.LOG_SEVERITY.valueOf(rs.getString(DbKeys.LogKeys.SEVERITY)), //
		    LogRecord.LOG_CATEGORY.valueOf(rs.getString(DbKeys.LogKeys.CATEGORY)),//
		    LogRecord.LOG_TYPE.valueOf(rs.getString(DbKeys.LogKeys.LOG_TYPE)),//
		    rs.getString(DbKeys.LogKeys.RESOURCE),//
		    LogRecord.LOG_RESULT.valueOf(rs.getString(DbKeys.LogKeys.RESULT)),//
		    rs.getString(DbKeys.LogKeys.DESC),
		    DbOpsHelper.nameValuePairElementToMap(DbOpsHelper.xmlToElement(rs
		        .getString(DbKeys.LogKeys.XML))));
		return log;
	}
}
