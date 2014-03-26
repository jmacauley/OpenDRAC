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

package com.nortel.appcore.app.drac.database.dracdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogDbHelper;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOpWithResultsI;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * Read & logs to the database. This class replaces the two separate audit logs
 * and event logs into a single log database format that is used for both. Since
 * the database name "log" is in lower case it will not be synced between
 * primary & backup servers! We don't want to toss large databases back and
 * forth, and if we did, each log would need to indicate what server was
 * generated on.
 * 
 * @author pitman
 */

public enum DbLog {
	
	INSTANCE;
	
	private static final Logger log = LoggerFactory.getLogger(DbLog.class);
	
	
	static class Reader implements DbOpWithResultsI {
		private final List<LogRecord> logRecords = new ArrayList<LogRecord>();
		private final Map<String, String> keys;

		public Reader(Map<String, String> params) {
			keys = params;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			// we use a fixed sql statement
			if (keys != null && !keys.isEmpty()) {
				int i = 1;
				for (String key : keys.keySet()) {
					statement.setString(i, keys.get(key));
					i++;
				}
			}

			return statement;
		}

		public List<LogRecord> getLogs() {
			return logRecords;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			while (rs.next()) {
				logRecords.add(LogDbHelper.helper(rs));
			}
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
			// not used
		}
	}

	static class Writer extends DbOpWithResultsAdapter {
		// Used to support batches of logs, now just write them one at a time.
		// private final List<LogRecord> l;
		private final LogRecord l;

		Writer(LogRecord log) {
			l = log;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
		    throws Exception {
			stmt.setLong(1, l.getTime());
			stmt.setString(2, trim(l.getOriginator()));
			stmt.setString(3, trim(l.getIp()));
			stmt.setString(4, trim(l.getBillingGroup().toString()));
			stmt.setString(5, l.getSeverity().name());
			stmt.setString(6, l.getCategory().name());
			stmt.setString(7, l.getType().name());
			stmt.setString(8, trim(l.getResource()));
			stmt.setString(9, l.getResult().name());
			stmt.setString(10, trim(l.getDescription()));
			stmt.setString(11, DbOpsHelper.elementToString(DbOpsHelper
			    .mapToNameValuePairElement("details", l.getDetails())));

			return stmt;
		}

		private String trim(String s) {
			if (s.length() >= 255) {
				log.error("Log field is too long, trucating '" + s
				    + "' to 255 characters in length");
				return s.substring(0, 255);
			}
			return s;
		}
	}

	private static final String TABLENAME = "logs";

	/**
	 * Add a log, toss back exceptions if something goes wrong.
	 */
	public void addLog(LogRecord log) throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults("insert into " + TABLENAME
		    + " (" + DbKeys.LogKeys.TIME + ", " + DbKeys.LogKeys.ORIGINATOR + ", "
		    + DbKeys.LogKeys.IP_ADDR + ", " + DbKeys.LogKeys.BILLING_GROUP + ", "
		    + DbKeys.LogKeys.SEVERITY + ", " + DbKeys.LogKeys.CATEGORY + ", "
		    + DbKeys.LogKeys.LOG_TYPE + ", " + DbKeys.LogKeys.RESOURCE + ", "
		    + DbKeys.LogKeys.RESULT + ", " + DbKeys.LogKeys.DESC + ", "
		    + DbKeys.LogKeys.XML + ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
		    new Writer(log));		
	}

	/**
	 * For testing only drops and recreates database.
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "delete from " + TABLENAME + ";", new DbOpWithResultsAdapter());
	}

	/**
	 * Remove logs older than the given time stamp. For purging old logs.
	 */
	public void deleteTimeLessThan(long time) throws Exception {
		if (time == 0) {
			deleteAll();
		}
		else {
			DbOperationsManager.INSTANCE.executeDbOpWithResults("delete from " + TABLENAME
			    + " where " + DbKeys.LogKeys.TIME + "<= " + time + ";",
			    new DbOpWithResultsAdapter());
		}
	}

	/**
	 * Best effort generate a log and swallow exceptions that might occur.
	 */
	public void generateLog(LogRecord logRecord) {
		try {
			addLog(logRecord);
		}
		catch (Exception e) {
			log.error("Unable to generate log " + logRecord.toString(), e);
		}
	}

	public List<LogRecord> getLogs(long startTime, long endTime,
	    Map<String, String> filter) throws Exception {
		StringBuilder sql = new StringBuilder("select * from " + TABLENAME
		    + " where ");
		/*
		 * This is safe from a SQL injection attack, the startTime and endTime
		 * fields are longs and cannot break the statement by embedding them
		 * directly.
		 */
		sql.append(DbKeys.LogKeys.TIME + " >= " + startTime + " AND "
		    + DbKeys.LogKeys.TIME + " <= " + endTime);

		if (filter != null && !filter.isEmpty()) {
			for (String key : filter.keySet()) {
				if (DbKeys.LogKeys.ORIGINATOR.equals(key)
				    || DbKeys.LogKeys.BILLING_GROUP.equals(key)
				    || DbKeys.LogKeys.SEVERITY.equals(key)
				    || DbKeys.LogKeys.CATEGORY.equals(key)
				    || DbKeys.LogKeys.LOG_TYPE.equals(key)
				    || DbKeys.LogKeys.RESOURCE.equals(key)
				    || DbKeys.LogKeys.RESULT.equals(key)
				    || DbKeys.LogKeys.IP_ADDR.equals(key)
				    || DbKeys.LogKeys.DESC.equals(key)) {
					/*
					 * This is safe from a SQL injection attack as we check the key is
					 * valid before embedding it.
					 */
					sql.append(" AND " + key + " LIKE " + "?");
				}
				else {
					throw new Exception("Invalid filter key '" + key + "'");
				}
			}
		}
//		sql.append(" order by time desc limit "+DbOperationsManager.INSTANCE.MAX_RESULT_SET);

		Reader r = new Reader(filter);
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), r);
		return r.getLogs();
	}
	
	
	public boolean findServiceActivatedByResource(String resource) {
	  return findByResourceAndDescription(resource, "Service activated");
	}

  public boolean findByResourceAndDescription(String resourceId, String description) {
    final String sql = "select * from " + TABLENAME + " where resource = ? and descr = ?";
    try {
      final Connection connection = DbOperationsManager.INSTANCE.getDatasource().getConnection();
      final PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, DbLightPath.INSTANCE.findServiceIdByScheduleId(resourceId));
      preparedStatement.setString(2, description);
      boolean isFound = false;
      preparedStatement.execute();
      final ResultSet resultSet = preparedStatement.getResultSet();
      if (resultSet!=null && resultSet.next()) {
        isFound =  true;
      }
      DbOperationsManager.INSTANCE.close(connection, preparedStatement);
      return isFound;
    }
    catch (SQLException e) {
      log.error("Error: ", e);
      return false;
    }
  }
}
