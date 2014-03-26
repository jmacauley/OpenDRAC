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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.Site;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * @author pitman
 */
public enum DbSites {
	INSTANCE;
	private static final String TABLENAME = "Sites";

	/**
	 * This is a set of database column names that we permit filtering on. This
	 * can include all valid columns even if we don't expect or want anyone to
	 * actually query on a given column. Most importantly this list is used to
	 * prevent sql injection attacks, our filters are supplied as
	 * Map<String,String> and while we escape the values we don't escape the
	 * column names, instead we verify that they are in this set before permitting
	 * them to be used. As long as this set does not contain invalid SQL
	 * characters our queries are safe.
	 */
	private static final TreeSet<String> VALID_FILTER_KEYS = new TreeSet<String>(
	    Arrays.asList(new String[] { DbKeys.SitesCols.LOCATION,
	        DbKeys.SitesCols.DESCRIPTION, DbKeys.SitesCols.ID }));

	public void add(final Site site) throws Exception {
		String sql = "insert into " + TABLENAME + " ( " + DbKeys.SitesCols.ID
		    + ", " + DbKeys.SitesCols.LOCATION + ", "
		    + DbKeys.SitesCols.DESCRIPTION + ") values (?, ?, ?) ";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, site.getId());
				stmt.setString(2, site.getLocation());
				stmt.setString(3, site.getDescription());
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
	}

	/**
	 * For testing only drops and recreates database.
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "delete from " + TABLENAME + ";", new DbOpWithResultsAdapter());
	}

	public List<Site> retrieve(final Map<String, String> filter) throws Exception {
		boolean flag = false;
		final List<String> attrList = new ArrayList<String>();

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME);

		if (filter != null) {
			for (Map.Entry<String, String> oEntry : filter.entrySet()) {
				String name = oEntry.getKey();
				String value = oEntry.getValue();
				if (!VALID_FILTER_KEYS.contains(name)) {
					throw new Exception("DbSites invalid filter entry " + name + "="
					    + value + "! Name is not a valid filter key from "
					    + VALID_FILTER_KEYS);
				}

				if (flag) {
					sql.append("AND ");
				}
				else {
					sql.append(" where ");
				}

				/*
				 * This is not subject to a SQL injection attack, as name has been
				 * validated to be a valid column name above.
				 */
				sql.append(name + " = ? ");
				attrList.add(value);
				flag = true;
			}
		}

		sql.append(";");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<Site> result = new ArrayList<Site>();

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				for (int i = 0; i < attrList.size(); i++) {
					stmt.setString(i + 1, attrList.get(i));
				}

				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				while (rs.next()) {
					result.add(new Site(rs.getString(DbKeys.SitesCols.ID), rs
					    .getString(DbKeys.SitesCols.LOCATION), rs
					    .getString(DbKeys.SitesCols.DESCRIPTION)));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (List<Site>) dbOp.getResult();
	}

	public List<Site> retrieveAll() throws Exception {
		return retrieve(null);
	}

	public void update(final Site site) throws Exception {
		String sql = "update " + TABLENAME + " SET " + DbKeys.SitesCols.LOCATION
		    + " =?, " + DbKeys.SitesCols.DESCRIPTION + " =? where "
		    + DbKeys.SitesCols.ID + " = ?;";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, site.getLocation());
				stmt.setString(2, site.getDescription());
				stmt.setString(3, site.getId());
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
	}
}
