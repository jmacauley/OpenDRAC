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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsI;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOpWithResultsI;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * @author pitman
 */
public enum DbNetworkElementConnection {
	INSTANCE;
	private static class Reader implements DbOpWithResultsI {
		private final List<String> attrList;
		private final List<CrossConnection> result = new ArrayList<CrossConnection>();

		public Reader(List<String> attr) {
			attrList = attr;
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
		    throws Exception {
			int i;
			for (i = 0; i < attrList.size(); i++) {
				stmt.setString(i + 1, attrList.get(i));
			}

			return stmt;
		}

		public List<CrossConnection> getResults() {
			return result;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {

			while (rs.next()) {
				Map<String, String> connMap = (Map<String, String>) DbOpsHelper
				    .deserialize((byte[]) rs.getObject("data"));
				result.add(new CrossConnection(connMap));
			}
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
			//
		}
	}

	private static final String TABLENAME = "NetworkElementConnection";
	private static final String PK = "pk";
	private static final String DATA = "data";

	public void add(final String neid, final List<Map<String, String>> data)
	    throws Exception {
		/*
		 * Sample input: [ { id="DRAC-cd28862f-1222266429798" type="2WAY"
		 * rate="STS3C" source="00-1B-25-2D-5C-7A" sShelf="1" sSlot="11"
		 * sSubslot="0" sPort="1" sChannel="1" target="00-1B-25-2D-5C-7A"
		 * sourceAid="OC12-1-11-1-1" tShelf="1" tSlot="1" tSubslot="0" tPort="1"
		 * tChannel="1" targetAid="OC12-1-1-1-1" swmate="" committed="true" } { ...
		 * } { ... } ]
		 */

		String sql = "insert into " + TABLENAME + " ( "
		    + DbNetworkElementConnection.PK + ", "
		    + DbKeys.NetworkElementConnectionCols.ID + ", "
		    + DbNetworkElementConnection.DATA + ") values (?, ?, ?) ";

		DbBatchWithResultsI dbBatch = new DbBatchWithResultsAdapter(data.size()) {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				for (int i = 0; i < data.size(); i++) {
					Map<String, String> connMap = data.get(i);

					stmt.setString(
					    1,
					    encodePK(neid,
					        connMap.get(DbKeys.NetworkElementConnectionCols.SOURCEAID),
					        connMap.get(DbKeys.NetworkElementConnectionCols.TARGETAID)));
					stmt.setString(2, connMap.get(DbKeys.NetworkElementConnectionCols.ID));
					stmt.setObject(3, DbOpsHelper.serialize((Serializable) connMap));

					stmt.addBatch();
				}

				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbBatchWithResults(sql, dbBatch);
	}

    public void edit(final String neid, final String srcAid, final String dstAid, final List<Map<String, String>> data) throws Exception {

    }

	public void delete(final AbstractNetworkElement ne, final String srcAid,
	    final String dstAid) throws Exception {
		delete(ne.getNeId(), srcAid, dstAid);
	}

	public void delete(final String neId, final String srcAid, final String dstAid)
	    throws Exception {
		String sql = "delete from " + TABLENAME + " where pk LIKE ?;";
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, encodePK(neId, srcAid, dstAid));
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

	public void deleteAll(AbstractNetworkElement ne) throws Exception {
		delete(ne, "%", "%");
	}

	public void deleteAll(String neId) throws Exception {
		delete(neId, "%", "%");
	}

	public List<CrossConnection> retrieve(final Map<String, String> filter)
	    throws Exception {
		boolean flag = false;
		final List<String> attrList = new ArrayList<String>();

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME);

		String pk = null;

		String neid = "%";
		String srcAid = "%";
		String targetAid = "%";

		if (filter != null) {
			neid = filter.get(DbKeys.NetworkElementConnectionCols.NEID_FOR_CONN) != null ? filter
			    .get(DbKeys.NetworkElementConnectionCols.NEID_FOR_CONN) : "%";
			srcAid = filter.get(DbKeys.NetworkElementConnectionCols.SOURCEAID) != null ? filter
			    .get(DbKeys.NetworkElementConnectionCols.SOURCEAID) : "%";
			targetAid = filter.get(DbKeys.NetworkElementConnectionCols.TARGETAID) != null ? filter
			    .get(DbKeys.NetworkElementConnectionCols.TARGETAID) : "%";
		}

		pk = encodePK(neid, srcAid, targetAid);

		// Check for a primary key - accommodates for substring matching
		if (pk != null) {
			sql.append(" where ");
			sql.append(DbNetworkElementConnection.PK + " LIKE ? ");
			attrList.add(pk);
			flag = true;
		}

		// Add additional search criteria checks here
		/*
		 * if (filter != null && filter.containsKey(DbNetworkElementConnection.BLA))
		 * { if (flag) { sb.append("AND "); } else { sb.append(" where "); }
		 * b.append(DbNetworkElementConnection.BLA + " = ? ");
		 * attrList.add(data.get(DbNetworkElementConnection.BLA)); flag = true; }
		 */

		// Attr: ID ... allows for substring matching
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementConnectionCols.ID)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}
			sql.append(DbKeys.NetworkElementConnectionCols.ID + " LIKE ? ");
			attrList.add(filter.get(DbKeys.NetworkElementConnectionCols.ID));
			flag = true;
		}

		// Attr: ID_NOT ... allows for substring matching
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementConnectionCols.ID_NOT)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}
			sql.append(DbKeys.NetworkElementConnectionCols.ID + " NOT LIKE ? ");
			attrList.add(filter.get(DbKeys.NetworkElementConnectionCols.ID_NOT));
			flag = true;
		}

		sql.append(";");
		if (filter != null && flag == false) {
			throw new Exception(
			    "NetworkElementConnection - Unsupported search criteria " + filter);
		}

		Reader dbOp = new Reader(attrList);
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return dbOp.getResults();
	}

	private String encodePK(String neid, String sourceAid, String targetAid) {
		if (neid != null && sourceAid != null && targetAid != null) {
			return neid + "_" + sourceAid + "_" + targetAid;
		}

		return null;
	}

}
