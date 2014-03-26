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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * @author pitman
 */
public enum DbNetworkElement {
	INSTANCE;
	private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * Table NetworkElement had smaller fields, NetworkElementV2 has varchar(255)
	 * fields and the new field NE_RELEASE added.
	 * <p>
	 * NetworkElement changed the COMMPROTOCOL and MODE fields from sql enums to
	 * varchar(255) strings so we don't have to mess with the schema when new
	 * types are added. silly
	 */
	private static final String TABLENAME = "NetworkElement";

	public boolean add(final NetworkElementHolder holder) throws Exception {
		if (exists(encodePK(holder.getIp(), holder.getPort()))) {
			return false;
		}

		String sql = "insert into " + TABLENAME + " ("
		    + DbKeys.NetworkElementCols.PK + ", "
		    + DbKeys.NetworkElementCols.AUTOREDISC + ", "
		    + DbKeys.NetworkElementCols.COMMPROTOCOL + ", "
		    + DbKeys.NetworkElementCols.NEID + ", "
		    + DbKeys.NetworkElementCols.MANAGEDBY + ", "
		    + DbKeys.NetworkElementCols.MODE + ", "
		    + DbKeys.NetworkElementCols.NEINDEX + ", "
		    + DbKeys.NetworkElementCols.PASSWORD + ", "
		    + DbKeys.NetworkElementCols.STATUS + ", "
		    + DbKeys.NetworkElementCols.TID + ", "
                    + DbKeys.NetworkElementCols.TYPE + ", "
                    + DbKeys.NetworkElementCols.USERID + ", "
                    + DbKeys.NetworkElementCols.SUBTYPE + ", "
		    + DbKeys.NetworkElementCols.NE_RELEASE + ", "
		    + DbKeys.NetworkElementCols.POSITION_X + ", "
		    + DbKeys.NetworkElementCols.POSITION_Y + ") "
		    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			int updateCount = -1;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				/*
				 * See neproxy.database.DbOperationsManager.INSTANCE.addNewNe. When the NE is
				 * first being added to the db (i.e. when calling this method), the 'id'
				 * value is zeroed out! 00-00-00-00-00-00 Only on the updateNe does the
				 * actual discovered 'id' value get written. So, because there is a
				 * period of time in which the 'id' may NOT be unique (i.e. if two NEs
				 * are added in sequence to the db before either is updated with actual
				 * values), we cannot use 'id' as the table's primary key.
				 */

				stmt.setString(1, encodePK(holder.getIp(), holder.getPort()));
				stmt.setBoolean(2, holder.isAutoReDiscover()); // autoReDiscover
				stmt.setString(3, holder.getCommProtocol().asString()); // commProtocol
				stmt.setString(4, holder.getId()); // neid
				stmt.setString(5, holder.getManagedBy()); // managedBy
				stmt.setString(6, holder.getMode().toString()); // mode
				stmt.setInt(7, holder.getNeIndex()); // neIndex
				stmt.setString(8, holder.getPassword().toString()); // encrypted
				// passwd
				stmt.setString(9, holder.getNeStatusWithDate()); // status and
				// date in
				// one field
				stmt.setString(10, holder.getTid()); // tid
				stmt.setString(11, holder.getType().toString()); // type
				stmt.setString(12, holder.getUserId()); // userId
				stmt.setString(13, holder.getSubType()); // subType
				stmt.setString(14, holder.getNeRelease()); // neRelease
				stmt.setDouble(15,
				    holder.getPositionX() == null ? 0 : holder.getPositionX());
				stmt.setDouble(16,
				    holder.getPositionY() == null ? 0 : holder.getPositionY());
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

	public void delete(final AbstractNetworkElement ne) throws Exception {
		final String neKey = encodePK(ne.getIpAddress(),
		    Integer.toString(ne.getPortNumber()));

		DbNetworkElementConnection.INSTANCE.deleteAll(ne.getNeId());
		DbNetworkElementFacility.INSTANCE.deleteAll(ne.getNeId());
		DbNetworkElementAdjacency.INSTANCE.deleteAllByNeId(ne.getNeId());

		String sql = "delete from " + TABLENAME + " where pk LIKE ? ;";
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, neKey);
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
	}

	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "delete from " + TABLENAME + ";", new DbOpWithResultsAdapter());
	}

	public boolean exists(final String neKey) throws Exception {
		String sql = "select count(*) from " + TABLENAME + " where pk LIKE ? ;";
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			boolean neExists = true;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, neKey);
				return stmt;
			}

			@Override
			public Object getResult() {
				return Boolean.valueOf(neExists);
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				rs.next();
				int count = rs.getInt(1);
				neExists = count > 0;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
		return ((Boolean) dbOp.getResult()).booleanValue();
	}

	public List<NetworkElementHolder> retrieve(final Map<String, String> filter)
	    throws Exception {
		boolean flag = false;
		final List<String> attrList = new ArrayList<String>();

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME);

		String ip = "%";
		String port = "%";

		if (filter != null) {
			for (String key : filter.keySet()) {
				if (!(DbKeys.NetworkElementCols.NEIP.equals(key)
				    || DbKeys.NetworkElementCols.NEPORT.endsWith(key) || DbKeys.NetworkElementCols.MANAGEDBY
				    .equals(key))) {
					throw new Exception("Invalid filter data " + filter);
				}
			}
		}
		if (filter != null) {
			ip = filter.get(DbKeys.NetworkElementCols.NEIP) != null ? filter
			    .get(DbKeys.NetworkElementCols.NEIP) : "%";
			port = filter.get(DbKeys.NetworkElementCols.NEPORT) != null ? filter
			    .get(DbKeys.NetworkElementCols.NEPORT) : "%";
		}

		String pk = encodePK(ip, port);
		sql.append(" where ");
		sql.append(DbKeys.NetworkElementCols.PK + " LIKE ? ");
		attrList.add(pk);
		flag = true;

		// Add additional search criteria checks here
		/*
		 * if (filter != null && filter.containsKey(DbNetworkElement.BLA)) { if
		 * (flag) { sb.append("AND "); } else { sb.append(" where "); }
		 * b.append(DbNetworkElementConnection.BLA + " = ? ");
		 * attrList.add(data.get(DbNetworkElement.BLA)); flag = true; }
		 */

		// Attr: MANAGEDBY ... exact match only
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementCols.MANAGEDBY)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}
			sql.append(DbKeys.NetworkElementCols.MANAGEDBY + " = ? ");
			attrList.add(filter.get(DbKeys.NetworkElementCols.MANAGEDBY));
			flag = true;
		}

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<NetworkElementHolder> result;

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
		return (List<NetworkElementHolder>) dbOp.getResult();
	}

	public List<NetworkElementHolder> retrieveAll() throws Exception {
		return retrieve(null);
	}

	public void update(final AbstractNetworkElement ne,
	    final Map<String, String> data) throws Exception {

		boolean flag = false;
		final List<String> attrList = new ArrayList<String>();

		StringBuilder sql = new StringBuilder();
		sql.append("update " + TABLENAME + " SET ");

		if (data.containsKey(DbKeys.NetworkElementCols.TYPE)) {
			sql.append(DbKeys.NetworkElementCols.TYPE + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.TYPE));
			flag = true;
		}

		if (data.containsKey(DbKeys.NetworkElementCols.NEID)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.NEID + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.NEID));
			flag = true;
		}
		if (data.containsKey(DbKeys.NetworkElementCols.STATUS)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.STATUS + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.STATUS));
			flag = true;
		}
		if (data.containsKey(DbKeys.NetworkElementCols.MODE)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.MODE + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.MODE));
			flag = true;
		}
		if (data.containsKey(DbKeys.NetworkElementCols.TID)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.TID + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.TID));
			flag = true;
		}
		if (data.containsKey(DbKeys.NetworkElementCols.USERID)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.USERID + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.USERID));
			flag = true;
		}
		if (data.containsKey(DbKeys.NetworkElementCols.PASSWORD)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.PASSWORD + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.PASSWORD));
			flag = true;
		}
                if (data.containsKey(DbKeys.NetworkElementCols.SUBTYPE)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.SUBTYPE + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.SUBTYPE));
			flag = true;
		}
		if (data.containsKey(DbKeys.NetworkElementCols.NE_RELEASE)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.NE_RELEASE + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.NE_RELEASE));
			flag = true;
		}
		if (data.containsKey(DbKeys.NetworkElementCols.POSITION_X)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.POSITION_X + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.POSITION_X));
			flag = true;
		}
		if (data.containsKey(DbKeys.NetworkElementCols.POSITION_Y)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(DbKeys.NetworkElementCols.POSITION_Y + " = ? ");
			attrList.add(data.get(DbKeys.NetworkElementCols.POSITION_Y));
			flag = true;
		}

		if (flag) {
			sql.append("where pk LIKE ? ");
		}
		else {
			log.error(TABLENAME + " update called for unsupported data. " + data);
			return;
		}

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				int i;
				for (i = 0; i < attrList.size(); i++) {
					stmt.setString(i + 1, attrList.get(i));
				}

				// where
				stmt.setString(i + 1,
				    encodePK(ne.getIpAddress(), Integer.toString(ne.getPortNumber())));

				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
	}

	public String encodePK(String ip, String port) {
		if (ip != null && port != null) {
			return ip + "_" + port;
		}

		return null;
	}

	private NetworkElementHolder processRetrieveResult(ResultSet rs)
	    throws Exception {
		String[] fields = rs.getString(DbKeys.NetworkElementCols.PK).split("_");

		String ip = fields[0];
		String port = fields[1];

		String userId = rs.getString(DbKeys.NetworkElementCols.USERID);
		String type = rs.getString(DbKeys.NetworkElementCols.TYPE);
		String tid = rs.getString(DbKeys.NetworkElementCols.TID);
		String status = rs.getString(DbKeys.NetworkElementCols.STATUS);
		String password = rs.getString(DbKeys.NetworkElementCols.PASSWORD);
		int neIndex = rs.getInt(DbKeys.NetworkElementCols.NEINDEX);
		String mode = rs.getString(DbKeys.NetworkElementCols.MODE);
		String managedBy = rs.getString(DbKeys.NetworkElementCols.MANAGEDBY);
		String id = rs.getString(DbKeys.NetworkElementCols.NEID);
		String commProtocol = rs.getString(DbKeys.NetworkElementCols.COMMPROTOCOL);
		PROTOCOL_TYPE protocol = PROTOCOL_TYPE.fromString(commProtocol);
                String subType = rs.getString(DbKeys.NetworkElementCols.SUBTYPE);
		String release = rs.getString(DbKeys.NetworkElementCols.NE_RELEASE);
		boolean autoReDiscover = rs
		    .getBoolean(DbKeys.NetworkElementCols.AUTOREDISC);
		Double positionX = rs.getDouble(DbKeys.NetworkElementCols.POSITION_X);
		Double positionY = rs.getDouble(DbKeys.NetworkElementCols.POSITION_Y);

		// The status string should have a NeStatus field followed by a date
		// stamp. The date stamp can be
		// missing.
		NeStatus neStatus;
		String statusString;
		if (status.indexOf(' ') > 0) {
			String st = status.substring(0, status.indexOf(' '));
			neStatus = NeStatus.fromString(st);
			statusString = status.substring(status.indexOf(' ') + 1);
		}
		else {
			neStatus = NeStatus.fromString(status);
			statusString = null;
		}

		NetworkElementHolder h = new NetworkElementHolder(ip, port, userId,
		    NeType.fromString(type), tid, neStatus, statusString,
		    new CryptedString(password), neIndex,
		    NETWORK_ELEMENT_MODE.valueOf(mode), managedBy, id, protocol,
		    autoReDiscover, subType, release, positionX, positionY);
		return h;
	}

	private List<NetworkElementHolder> processRetrieveResultList(ResultSet rs)
	    throws Exception {
		List<NetworkElementHolder> result = new ArrayList<NetworkElementHolder>();
		while (rs.next()) {
			result.add(processRetrieveResult(rs));
		}

		return result;
	}
}
