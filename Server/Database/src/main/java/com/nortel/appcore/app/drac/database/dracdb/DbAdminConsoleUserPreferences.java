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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

public enum DbAdminConsoleUserPreferences {
	INSTANCE;
	private static final String XML = "xml";
	private static final String TABLENAME = "UserPreferences";

	public boolean add(final String userId, final Element userPreferences)
	    throws Exception {
		String sql = "insert into " + TABLENAME + " ("
		    + DbKeys.AdminConsoleUserPreferencesCols.USERID + ", " + XML + ") "
		    + "values (?, ?);";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			int updateCount = -1;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, userId);
				stmt.setString(2, DbOpsHelper.elementToString(userPreferences));
				return stmt;
			}

			@Override
			public Object getResult() {
				return Boolean.valueOf(updateCount > 0);
			}

			@Override
			public void setUpdateCount(int count) {
				updateCount = count;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
		return ((Boolean) dbOp.getResult()).booleanValue();
	}

	public void addUpdate(final String userId, final Element newUserPreferences)
	    throws Exception {
		Element existingUserPreferences = retrieve(userId);

		if (existingUserPreferences == null) {
			add(userId, newUserPreferences);
		}
		else {

			String sql = "update " + TABLENAME + " SET " + XML + " = ? where "
			    + DbKeys.AdminConsoleUserPreferencesCols.USERID + " = ?;";

			DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
				@Override
				public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
				    throws Exception {
					stmt.setString(1, DbOpsHelper.elementToString(newUserPreferences));
					stmt.setString(2, userId);
					return stmt;
				}
			};
			DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
		}
	}

	/**
	 * For testing only drops and recreates database.
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "delete from " + TABLENAME + ";", new DbOpWithResultsAdapter());
	}

	public Element retrieve(final String userId) throws Exception {
		String sql = "select " + XML + " from " + TABLENAME + " where "
		    + DbKeys.AdminConsoleUserPreferencesCols.USERID + "=?;";
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			Element result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, userId);
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				if (rs.next()) {
					result = DbOpsHelper.xmlToElement(rs.getString(1));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
		return (Element) dbOp.getResult();
	}

}
