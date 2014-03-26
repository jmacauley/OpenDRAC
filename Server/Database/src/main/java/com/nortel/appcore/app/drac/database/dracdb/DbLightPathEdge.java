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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbKeys.LightPathEdgeKeys;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsI;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * @author pitman
 */
public enum DbLightPathEdge {
	INSTANCE;
	/*
	 * TABLENAME is not public so that anyone outside of this class must use
	 * getInstance().getTableName() to access it and thus insure the table has
	 * been created before its accessed.
	 */
	private static final String TABLENAME = "LightPathEdge";

	public void add(final ServiceXml serviceXml) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into " + TABLENAME + "(");
		sql.append(DbKeys.LightPathCols.LP_CALLID + ", ");
		sql.append(LightPathEdgeKeys.EDGE_SOURCE + ", ");
		sql.append(LightPathEdgeKeys.EDGE_SOURCEAID + ", ");
		sql.append(LightPathEdgeKeys.EDGE_SOURCECHANNEL + ", ");
		sql.append(LightPathEdgeKeys.EDGE_TARGET + ", ");
		sql.append(LightPathEdgeKeys.EDGE_TARGETAID + ", ");
		sql.append(LightPathEdgeKeys.EDGE_TARGETCHANNEL + ", ");
		sql.append(LightPathEdgeKeys.EDGE_RATE + ", ");
		sql.append(LightPathEdgeKeys.EDGE_CCT + ", ");
		sql.append(LightPathEdgeKeys.EDGE_SWMATEAID + ", ");
		sql.append(LightPathEdgeKeys.EDGE_MEP + ", ");
		sql.append(LightPathEdgeKeys.EDGE_VLANID);
		sql.append(" ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? );");

		DbBatchWithResultsI dbBatch = new DbBatchWithResultsAdapter(serviceXml
		    .getCrossConnections().size()) {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				/**
				 * Note: the call to getEdges returns only the single copy of connection
				 * records from the 'main' overall service record. See notes in that
				 * method.
				 */

				for (CrossConnection xc : serviceXml.getCrossConnections()) {
					stmt.setString(1, serviceXml.getCallId());
					stmt.setString(2, xc.getSourceNeId());
					stmt.setString(3, xc.getSourcePortAid());
					stmt.setInt(4, Integer.parseInt(xc.getSourceChannel()));
					stmt.setString(5, xc.getTargetNeId());
					stmt.setString(6, xc.getTargetPortAid());
					stmt.setInt(7, Integer.parseInt(xc.getTargetChannel()));
					stmt.setString(8, xc.getRate());
					stmt.setString(9, xc.getType());
					stmt.setString(10, xc.getSwMateXcAid());
					stmt.setString(11, "");
					stmt.setString(12, xc.getVlanId());
					stmt.addBatch();
				}

				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbBatchWithResults(sql.toString(), dbBatch);
	}

	/**
	 * For testing only drops and recreates database.
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "delete from " + TABLENAME + ";", new DbOpWithResultsAdapter());
	}

	// There are N DbLightPathEdge records whose callId refers to one
	// DbLightPath
	public void deleteByCallId(final String callId) throws Exception {
		String sql = "delete from " + TABLENAME + " where "
		    + DbKeys.LightPathCols.LP_CALLID + " LIKE ?;";
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, callId);
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
	}

	public String getTableName() {
		return TABLENAME;
	}

	/**
	 * Retrieve the edges. An edge is like a cross connection but is missing
	 * important data that needs to be added back in from the service.
	 */
	public List<Map<String, String>> retrieve(final Map<String, String> filter)
	    throws Exception {
		final List<String> attrList = new ArrayList<String>();

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME);

		if (filter != null) {
			boolean flag = false;
			for (Map.Entry<String, String> oEntry : filter.entrySet()) {
				String name = oEntry.getKey();
				String value = oEntry.getValue();

				/*
				 * We only allow this single filter right now. Need to validate to
				 * prevent an SQL injection attack.
				 */
				if (!DbKeys.LightPathCols.LP_CALLID.equals(name)) {
					throw new Exception("Invalid filter '" + name + "' not supported");
				}

				if (flag) {
					sql.append(" AND ");
				}
				else {
					sql.append(" where ");
				}

				// Safe from sql Injection attack, we've verified that 'name' is
				// valid
				sql.append(name + " = ? ");
				attrList.add(value);
				flag = true;
			}

		}
		sql.append(";");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<Map<String, String>> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				int i;
				for (i = 0; i < attrList.size(); i++) {
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
				result = processRetrieveResultList(rs);
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (List<Map<String, String>>) dbOp.getResult();
	}

	private Map<String, String> processRetrieveResult(ResultSet rs)
	    throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put(DbKeys.LightPathCols.LP_CALLID,
		    rs.getString(DbKeys.LightPathCols.LP_CALLID));
		map.put(LightPathEdgeKeys.EDGE_SOURCE,
		    rs.getString(LightPathEdgeKeys.EDGE_SOURCE));
		map.put(LightPathEdgeKeys.EDGE_SOURCEAID,
		    rs.getString(LightPathEdgeKeys.EDGE_SOURCEAID));
		map.put(LightPathEdgeKeys.EDGE_SOURCECHANNEL,
		    Integer.toString(rs.getInt(LightPathEdgeKeys.EDGE_SOURCECHANNEL)));
		map.put(LightPathEdgeKeys.EDGE_TARGET,
		    rs.getString(LightPathEdgeKeys.EDGE_TARGET));
		map.put(LightPathEdgeKeys.EDGE_TARGETAID,
		    rs.getString(LightPathEdgeKeys.EDGE_TARGETAID));
		map.put(LightPathEdgeKeys.EDGE_TARGETCHANNEL,
		    Integer.toString(rs.getInt(LightPathEdgeKeys.EDGE_TARGETCHANNEL)));
		map.put(LightPathEdgeKeys.EDGE_RATE,
		    rs.getString(LightPathEdgeKeys.EDGE_RATE));
		map.put(LightPathEdgeKeys.EDGE_CCT,
		    rs.getString(LightPathEdgeKeys.EDGE_CCT));
		map.put(LightPathEdgeKeys.EDGE_SWMATEAID,
		    rs.getString(LightPathEdgeKeys.EDGE_SWMATEAID));
		map.put(LightPathEdgeKeys.EDGE_MEP,
		    rs.getString(LightPathEdgeKeys.EDGE_MEP));
		map.put(LightPathEdgeKeys.EDGE_VLANID,
		    rs.getString(LightPathEdgeKeys.EDGE_VLANID));
		return map;
	}

	private List<Map<String, String>> processRetrieveResultList(ResultSet rs)
	    throws Exception {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (rs.next()) {
			result.add(processRetrieveResult(rs));
		}

		return result;
	}

}
