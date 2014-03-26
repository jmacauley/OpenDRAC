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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOpWithResultsI;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * @author pitman
 */
public enum DbGlobalPolicy {
	INSTANCE;
	private static final Logger log = LoggerFactory.getLogger(DbGlobalPolicy.class);

	static class Reader implements DbOpWithResultsI {
		private GlobalPolicy policy;

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			// we use a fixed sql statement
			return statement;
		}

		public GlobalPolicy getPolicy() {
			return policy;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			int count = 0;
			while (rs.next()) {
				// Only ever expect a single result as we only ever store a
				// single object.
				policy = new GlobalPolicy();
				policy.fromXML(XmlUtility.createDocumentRoot(rs.getString(1)));
				count++;
			}

			if (count > 1) {
				log.error("Opps, we encountered "
				    + count
				    + " global policy records in the database, only expected to find zero or one.");
			}
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
			// Not used
		}

	}

	static class Writer implements DbOpWithResultsI {
		private final GlobalPolicy p;

		Writer(GlobalPolicy policy) {
			p = policy;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			statement.setString(1, p.toXMLString());
			return statement;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			// Not used
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
			
		}
	}

	private final Object lock = new Object();

	/**
	 * For testing only drops and recreates database.
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "delete from GlobalPolicy where id <> 'key';",
		    new DbOpWithResultsAdapter());
	}

	public GlobalPolicy getGlobalPolicy() throws Exception {
		synchronized (lock) {
			Reader r = new Reader();
			DbOperationsManager.INSTANCE.executeDbOpWithResults(
			    "select xml from GlobalPolicy;", r);
			return r.getPolicy();
		}
	}

	public void setDefaultGlobalPolicy(GlobalPolicy policy) throws Exception {
		synchronized (lock) {
			DbOperationsManager.INSTANCE
			    .executeDbOpWithResults(
			        "update GlobalPolicy set xml=? where id = 'key';", new Writer(
			            policy));
		}
	}
}
