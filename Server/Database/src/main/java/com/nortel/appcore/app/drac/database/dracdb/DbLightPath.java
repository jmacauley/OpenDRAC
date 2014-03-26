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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbKeys.LightPathEdgeKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.ServiceXml.XC_TYPE;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * @author pitman
 */
public enum DbLightPath {
	INSTANCE;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	/*
	 * TABLENAME is not public so that anyone outside of this class must use
	 * getInstance().getTableName() to access it and thus insure the table has
	 * been created before its accessed.
	 */
	private static final String TABLENAME = "LightPath";

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
	    Arrays.asList(new String[] { DbKeys.LightPathCols.LP_SERVICEID,
	        DbKeys.LightPathCols.LP_CALLID, DbKeys.LightPathCols.LP_STATUS,
	        DbKeys.LightPathCols.LP_VCAT, DbKeys.LightPathCols.LP_STARTTIME,
	        DbKeys.LightPathCols.LP_ENDTIME, DbKeys.LightPathCols.LP_USER,
	        DbKeys.LightPathCols.LP_BILLINGGROUP,
	        DbKeys.LightPathCols.LP_PRIORITY, DbKeys.LightPathCols.LP_RATE,
	        DbKeys.LightPathCols.LP_AEND, DbKeys.LightPathCols.LP_ZEND,
	        DbKeys.LightPathCols.LP_FILTER_SCHEDULEID_LIST,
	        DbKeys.LightPathCols.LP_ACTIVATIONTYPE,
	        DbKeys.LightPathCols.LP_CONTROLLERID,
	        DbKeys.LightPathCols.LP_SCHEDULEID,
	        DbKeys.LightPathCols.LP_SCHEDULENAME, DbKeys.LightPathCols.LP_MBS,
	        DbKeys.LightPathCols.LP_STARTTIME_GREATERTHAN_EQUALTO,
	        DbKeys.LightPathCols.LP_STARTTIME_LESSTHAN_EQUALTO,
	        DbKeys.LightPathCols.LP_ENDTIME_GREATERTHAN_EQUALTO,
	        DbKeys.LightPathCols.LP_ENDTIME_LESSTHAN_EQUALTO,
	        DbKeys.LightPathCols.LP_NEID, DbKeys.LightPathCols.LP_AID,
	        DbKeys.LightPathCols.LP_XML }));


	public boolean add(final ServiceXml serviceXml) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into " + TABLENAME + " (");
		sql.append(DbKeys.LightPathCols.LP_SERVICEID + ", ");
		sql.append(DbKeys.LightPathCols.LP_CALLID + ", ");
		sql.append(DbKeys.LightPathCols.LP_SCHEDULEID + ", ");
		sql.append(DbKeys.LightPathCols.LP_ACTIVATIONTYPE + ", ");
		sql.append(DbKeys.LightPathCols.LP_CONTROLLERID + ", ");
		sql.append(DbKeys.LightPathCols.LP_SCHEDULENAME + ", ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + ", ");
		sql.append(DbKeys.LightPathCols.LP_VCAT + ", ");
		sql.append(DbKeys.LightPathCols.LP_STARTTIME + ", ");
		sql.append(DbKeys.LightPathCols.LP_ENDTIME + ", ");
		sql.append(DbKeys.LightPathCols.LP_USER + ", ");
		sql.append(DbKeys.LightPathCols.LP_BILLINGGROUP + ", ");
		sql.append(DbKeys.LightPathCols.LP_PRIORITY + ", ");
		sql.append(DbKeys.LightPathCols.LP_MBS + ", ");
		sql.append(DbKeys.LightPathCols.LP_RATE + ", ");
		sql.append(DbKeys.LightPathCols.LP_AEND + ", ");
		sql.append(DbKeys.LightPathCols.LP_ZEND + ", ");
		sql.append(DbKeys.LightPathCols.LP_1PLUS1_PATH_DATA + ", ");
		sql.append(DbKeys.LightPathCols.LP_XML + ") ");

		sql.append("values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			int updateCount = -1;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, serviceXml.getServiceId());
				stmt.setString(2, serviceXml.getCallId());
				stmt.setString(3, serviceXml.getScheduleId());
				stmt.setString(4, serviceXml.getActivationType());
				stmt.setString(5, serviceXml.getControllerId());
				stmt.setString(6, serviceXml.getScheduleName());
				stmt.setString(7, Integer.toString(serviceXml.getStatus().ordinal()));
				stmt.setString(8, serviceXml.getVcatRoutingOption());
				stmt.setLong(9, serviceXml.getStartTime());
				stmt.setLong(10, serviceXml.getEndTime());
				stmt.setString(11, serviceXml.getUser());
				stmt.setString(12, serviceXml.getBillingGroup().toString());
				stmt.setString(13, serviceXml.getPriority());
				stmt.setInt(14, serviceXml.getMbs());
				stmt.setString(15, serviceXml.getBandwidth());
				stmt.setString(16, serviceXml.getAend());
				stmt.setString(17, serviceXml.getZend());

				// For 1+1 support:
				/**
				 * A service record contains a list of cross connections that must be
				 * provisioned to bring the service alive... this list of cross
				 * connections includes the working and protection path if a protected
				 * service was created. We store the active list of cross connections in
				 * the lightpath edge table, on entry per cross connection and link them
				 * back to the service record by the ID.
				 * <p>
				 * However we also store the actual working and protected paths in the
				 * service record, just for the pleasure of the admin console which uses
				 * that data to color the graph with the working and protection paths.
				 * Interestingly the working and protection path records are incomplete
				 * cross connection records and will fail if we attempt to provision
				 * them in the network.
				 * <p>
				 * We used to store these as a java List<String> in the database as a
				 * blob. The string contained the XML version of the working and
				 * protection paths in the list. Now we store
				 * List<List<CrossConnection>> where the outer list has 2 elements, the
				 * working and connection paths and each contains an inner list of zero
				 * or more cross connection records.
				 * <p>
				 * The cross connection objects in the working and protection paths have
				 * been stripped of unnecessary information, its really only necessary
				 * to have the NE and A and Z end entries for the cross connection,
				 * thinks like rate and switch mate and vlan id are not required and
				 * were omitted by LPCP_PORT to save on space.
				 * <p>
				 * Since the format of has changed from List<String> to
				 * List<List<CrossConnection>> we might need a upgrade procedure, or we
				 * can simply tolerate old format records and drop them, these
				 * information is only used by the admin console so its not vital that
				 * we keep this data for old schedules..
				 */

				List<List<CrossConnection>> oneplusonepaths = new ArrayList<List<CrossConnection>>();
				oneplusonepaths.add(serviceXml.getWorkingPath());
				oneplusonepaths.add(serviceXml.getProtectionPath());
				byte[] data = DbOpsHelper.serialize((Serializable) oneplusonepaths);
				stmt.setObject(18, data);

				// @TODO
				String xmlUserData = serviceXml.getXMLUserData();
				if (xmlUserData != null) {
					stmt.setString(19, xmlUserData);
				}
				else {
					stmt.setString(19, "");
				}
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
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return ((Boolean) dbOp.getResult()).booleanValue();
	}

	/**
	 * For testing only drops and recreates database.
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "delete from " + TABLENAME + ";", new DbOpWithResultsAdapter());
	}

	/**
	 * This method is used by the Purge utility and can result in a fully
	 * cascading cleanup: removal of one lightpath can potentially remove the
	 * parent schedule and all associated alarms.
	 */
	public void deleteByServiceId(final String serviceId) throws Exception {
		// Find the service record
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(DbKeys.LightPathCols.LP_SERVICEID, serviceId);
		List<ServiceXml> results = retrieve(filter);
		if (results == null || results.size() == 0) {
			throw new Exception("Record not found: " + serviceId);
		}
		final ServiceXml aService = results.get(0);

		// Remove all of the connection 'edge' records associated with this
		// service (referred via
		// callid)
		DbLightPathEdge.INSTANCE.deleteByCallId(aService.getCallId());

		// Remove the lightPath alarm summary records
		List<String> alarmIds = DbLightPathAlarmSummaries.INSTANCE
		    .deleteByServiceId(serviceId);

		/*
		 * Remove the lightPath references from the Alarm details. (If this is the
		 * last service to go, that Alarm details record will also be deleted) (The
		 * alarmIds list is passed in to deal with the fact that is is a
		 * many-to-many).
		 */
		DbLightPathAlarmDetails.INSTANCE.deleteServiceReference(alarmIds,
		    serviceId);

		// Remove this service
		String sql = "delete from " + TABLENAME + " where "
		    + DbKeys.LightPathCols.LP_SERVICEID + " LIKE ?;";
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, serviceId);
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);

		/*
		 * Cross check with the schedules. If this is the last service to go, that
		 * schedule should also be deleted
		 */
		List<String> scheduleIdForQuery = new ArrayList<String>(
		    Arrays.asList(new String[] { aService.getScheduleId() }));
		Map<String, Object> serviceFilter = new HashMap<String, Object>();
		serviceFilter.put(DbKeys.LightPathCols.LP_FILTER_SCHEDULEID_LIST,
		    scheduleIdForQuery);
		List<ServiceXml> serviceResults = DbLightPath.INSTANCE.retrieve(
		    serviceFilter);
		if (serviceResults == null || serviceResults.size() == 0) {
			DbSchedule.INSTANCE.delete(aService.getScheduleId());
		}
	}

	public List<String> getInprogressCalls() throws Exception {
		/*
		 * select DISTINCT id from LightPath where status = '7' response: list of
		 * live callIds ["DRAC-cd28862f-1228830516803"]
		 */

		String sql = "select DISTINCT " + DbKeys.LightPathCols.LP_CALLID + " from "
		    + TABLENAME + " where " + DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_INPROGRESS.ordinal() + "';";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<String> result = new ArrayList<String>();

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				while (rs.next()) {
					// DON'T FORGET TO PREPEND "DRAC-" to form the cktid; see
					// notes at top
					result.add("DRAC-" + rs.getString(1));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
		return (List<String>) dbOp.getResult();
	}

	public List<ServiceXml> getLiveServicesWithinTimeInterval(
	    final long fromTime, final long toTime) throws Exception {
		// select * from LightPath where startTime < toTime AND endTime >=
		// fromTime AND
		// ( status = 3 OR status = 0 OR status = 6 OR status = 7);

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME + " where ");
		sql.append(DbKeys.LightPathCols.LP_STARTTIME + " < ? AND  ");
		sql.append(DbKeys.LightPathCols.LP_ENDTIME + " >= ? AND  ");
		sql.append("( ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = ? OR ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = ? OR ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = ? OR ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = ?");
		sql.append(");");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<ServiceXml> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setLong(1, toTime);
				stmt.setLong(2, fromTime);
				stmt.setInt(3, State.SERVICE.ACTIVATION_PENDING.ordinal());
				stmt.setInt(4, State.SERVICE.CONFIRMATION_PENDING.ordinal());
				stmt.setInt(5, State.SERVICE.EXECUTION_PENDING.ordinal());
				stmt.setInt(6, State.SERVICE.EXECUTION_INPROGRESS.ordinal());
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
		return (List<ServiceXml>) dbOp.getResult();
	}

	public ServiceXml getNextServiceToDelete() throws Exception {
		// select * from LightPath where endTime = ( select MIN(endTime) from
		// LightPath where status
		// = 6 or
		// status = 3 or status = 7 )
		// AND ( status = 6 or status = 3 or status = 7 )

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME + " where "
		    + DbKeys.LightPathCols.LP_ENDTIME + " = ");

		sql.append("( select MIN(" + DbKeys.LightPathCols.LP_ENDTIME + ") from "
		    + TABLENAME + " where ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_PENDING.ordinal() + "'");
		sql.append(" OR ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.ACTIVATION_PENDING.ordinal() + "'");
		sql.append(" OR ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_INPROGRESS.ordinal() + "' )");

		/*
		 * NEED TO CHECK STATUS AGAIN! For multiple services with same start time,
		 * the MIN() function could return multiple records
		 */
		sql.append(" AND ( ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_PENDING.ordinal() + "'");
		sql.append(" OR ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.ACTIVATION_PENDING.ordinal() + "'");
		sql.append(" OR ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_INPROGRESS.ordinal() + "' );");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			ServiceXml result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				if (rs.next()) {
					result = processRetrieveResult(rs);
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (ServiceXml) dbOp.getResult();
	}

	public ServiceXml getNextServiceToStart(final long fromTime) throws Exception {
		/*
		 * select from LightPath where startTime = ( select MIN(startTime) from
		 * LightPath where status = 6 AND endTime >= 1227198711755 ) AND status = 6
		 */

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME + " where "
		    + DbKeys.LightPathCols.LP_STARTTIME + " = ");
		sql.append("( select MIN(" + DbKeys.LightPathCols.LP_STARTTIME + ") from "
		    + TABLENAME + " where ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_PENDING.ordinal() + "'");
		sql.append(" AND ");
		sql.append(DbKeys.LightPathCols.LP_ENDTIME + " >= ? )");
		/*
		 * NEED TO CHECK STATUS AGAIN! For multiple services with same start time,
		 * the MIN() function could return multiple records
		 */
		sql.append(" AND " + DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_PENDING.ordinal() + "'");
		sql.append(";");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			ServiceXml result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setLong(1, fromTime);
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				if (rs.next()) {
					result = processRetrieveResult(rs);
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (ServiceXml) dbOp.getResult();
	}

	public List<String> getServicesEligibleForPurge(Map<String, String> filter)
	    throws Exception {
		/*
		 * select from LightPath where (status != 0 and status != 3 and status != 6
		 * and status != 7) and ( endTime < 9228971600000 return format: <list>
		 * <lightPath serviceId="SERVICE-1228853731084" status="12"/> </list>
		 */

		StringBuilder sql = new StringBuilder();
		sql.append("select " + DbKeys.LightPathCols.LP_SERVICEID + ","
		    + DbKeys.LightPathCols.LP_STATUS + " from " + TABLENAME + " where ( "
		    + DbKeys.LightPathCols.LP_STATUS + " != '"
		    + State.SERVICE.CONFIRMATION_PENDING.ordinal() + "' and "
		    + DbKeys.LightPathCols.LP_STATUS + " != '"
		    + State.SERVICE.ACTIVATION_PENDING.ordinal() + "' and "
		    + DbKeys.LightPathCols.LP_STATUS + " != '"
		    + State.SERVICE.EXECUTION_PENDING.ordinal() + "'" + " and "
		    + DbKeys.LightPathCols.LP_STATUS + " != '"
		    + State.SERVICE.EXECUTION_INPROGRESS.ordinal() + "' )");

		/**
		 * This should be safe from sql injection attacks as we are only embedding a
		 * long integer value into the query string
		 */
		if (filter != null && filter.containsKey(DbKeys.LightPathCols.LP_ENDTIME)) {
			Long endTime = Long.valueOf(filter.get(DbKeys.LightPathCols.LP_ENDTIME));
			sql.append(" AND ( " + DbKeys.LightPathCols.LP_ENDTIME + " < " + endTime
			    + " ) ");
		}
		sql.append(";");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<String> result = new ArrayList<String>();

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				while (rs.next()) {
					// Element service = new Element("lightPath");
					// service.setAttribute("serviceId", rs.getString(1));
					// service.setAttribute("status", rs.getString(2));
					result.add(rs.getString(1));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (List<String>) dbOp.getResult();
	}

	public List<ServiceXml> getServicesFromAid(String aid, String neId)
	    throws Exception {
		return getServicesFromAid(aid, (String) null, 0, neId);

	}

	// Channel can be null - omit if so
	// intervalTime can be zero - omit if so
	public List<ServiceXml> getServicesFromAid(final String aid,
	    final String channel, final long intervalTime, final String neId)
	    throws Exception {
		/*
		 * select FROM LightPath WHERE id IN ( select DISTINCT id from LightPathEdge
		 * where ( ( ( targetAid = 'OC12-1-11-1' AND targetChannel = '1' ) OR ( (
		 * sourceAid = 'OC12-1-11-1' AND sourceChannel = '1' ) ) ) AND ( source =
		 * '00-1B-25-2D-5C-7A' ) ) ) AND startTime <= 1227627624753 AND endTime >=
		 * 1227627624753 if channel == null: select FROM LightPath WHERE id IN (
		 * select DISTINCT id from LightPathEdge where ( ( ( targetAid =
		 * 'OC12-1-11-1' ) OR ( ( sourceAid = 'OC12-1-11-1' ) ) ) AND ( source =
		 * '00-1B-25-2D-5C-7A' ) ) ) AND startTime <= 1227627624753 AND endTime >=
		 * 1227627624753
		 */

		final List<Object> attrList = new ArrayList<Object>();

		/*
		 * Call DbLightPathEdge.getInstance to make sure the table has been created
		 * first. Really only DbLightPathEdge should access this table, but that
		 * isn't always possible
		 */

		StringBuilder sql = new StringBuilder();
		sql.append("select * FROM " + TABLENAME + " WHERE "
		    + DbKeys.LightPathCols.LP_CALLID + " IN ");
		sql.append(" ( ");
		sql.append(" select DISTINCT " + DbKeys.LightPathCols.LP_CALLID + " from "
		    + DbLightPathEdge.INSTANCE.getTableName() + " where ( ( ( ");
		sql.append(LightPathEdgeKeys.EDGE_TARGETAID + " = ? ");
		attrList.add(aid);

		if (channel != null) {
			sql.append("AND " + LightPathEdgeKeys.EDGE_TARGETCHANNEL + " = ? ");
			attrList.add(channel);
		}

		sql.append(" ) ");
		sql.append(" OR ( ( " + LightPathEdgeKeys.EDGE_SOURCEAID + " = ? ");
		attrList.add(aid);

		if (channel != null) {
			sql.append("AND " + LightPathEdgeKeys.EDGE_SOURCECHANNEL + " = ? ");
			attrList.add(channel);
		}

		sql.append(" ) ) ) ");
		sql.append("AND ( " + LightPathEdgeKeys.EDGE_SOURCE + " = ? ) )");
		attrList.add(neId);
		sql.append(" ) ");

		if (intervalTime > 0) {
			sql.append(" AND " + DbKeys.LightPathCols.LP_STARTTIME + " <= ? AND "
			    + DbKeys.LightPathCols.LP_ENDTIME + " >= ? ");
			Long l = Long.valueOf(intervalTime);
			attrList.add(l);
			attrList.add(l);
		}
		sql.append(";");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<ServiceXml> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				for (int i = 0; i < attrList.size(); i++) {
					Object attr = attrList.get(i);

					if (attr instanceof String) {
						stmt.setString(i + 1, (String) attr);
					}
					else if (attr instanceof Long) {
						stmt.setLong(i + 1, ((Long) attr).longValue());
					}
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
		return (List<ServiceXml>) dbOp.getResult();
	}

	public List<ServiceXml> getServicesFromAlarm(final String alarmId)
	    throws Exception {
		/*
		 * select FROM LightPath WHERE serviceId IN ( select DISTINCT serviceId from
		 * LightPathAlarm where id = '00-1B-25-2D-5C-7A_0100000400-0001-0148' and
		 * severity != 'CL' )
		 */

		StringBuilder sql = new StringBuilder();
		sql.append("select * FROM " + TABLENAME + " WHERE "
		    + DbKeys.LightPathCols.LP_SERVICEID + " IN ");
		sql.append(" ( ");
		sql.append(" select DISTINCT " + DbKeys.LightPathCols.LP_SERVICEID
		    + " from " + DbLightPathAlarmSummaries.INSTANCE.getTableName()
		    + " where ");
		sql.append(DbKeys.LightPathAlarmSummariesCols.ALARMID + " = ? AND "
		    + DbKeys.LightPathAlarmSummariesCols.SEVERITY + " != 'CL' ");
		sql.append(" );");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<ServiceXml> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, alarmId);
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
		return (List<ServiceXml>) dbOp.getResult();
	}

	public String getTableName() {
		return TABLENAME;
	}

	public double getTotalOverLappingServiceRatesByBillingGroup(
	    final long fromTime, final long toTime, final UserGroupName billingGroup)
	    throws Exception {
		/*
		 * select ( SUM(mbs((endTime-startTime)/1000)) ) from LightPath WHERE
		 * scheduleId IN ( select id from Schedule where billingGroup =
		 * 'SystemAdminGroup' ) AND endTime >= fromTime AND startTime < toTime AND (
		 * status = 0 or status = 3 or status = 6 or status = 7 )
		 */

		StringBuilder sql = new StringBuilder();
		sql.append("select ( SUM(" + DbKeys.LightPathCols.LP_MBS + "*(("
		    + DbKeys.LightPathCols.LP_ENDTIME + "-"
		    + DbKeys.LightPathCols.LP_STARTTIME + ")/1000)) )");
		sql.append(" from " + TABLENAME + " where ");
		sql.append(DbKeys.LightPathCols.LP_SCHEDULEID + " IN ( select "
		    + DbSchedule.SCHD_ID + " from "
		    + DbSchedule.INSTANCE.getTableName() + " where "
		    + DbSchedule.SCHD_BILLINGGROUP + " = ?)");
		sql.append(" AND " + DbKeys.LightPathCols.LP_ENDTIME + " >= ?");
		sql.append(" AND " + DbKeys.LightPathCols.LP_STARTTIME + " < ? ");
		sql.append(" AND ( ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.CONFIRMATION_PENDING.ordinal() + "' or ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.ACTIVATION_PENDING.ordinal() + "' or ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_PENDING.ordinal() + "' or ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_INPROGRESS.ordinal() + "'");
		sql.append(" );");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			Double result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, billingGroup.toString());
				stmt.setLong(2, fromTime);
				stmt.setLong(3, toTime);
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				if (rs.next()) {
					result = Double.valueOf(rs.getDouble(1));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return ((Double) dbOp.getResult()).doubleValue();
	}

	public double getTotalOverLappingServiceRatesByResourceGroup(long fromTime,
	    long toTime, final String endpoint, final String resourceGroup,
	    boolean isSourceInfo) throws Exception {
		/*
		 * select ( SUM(mbs((endTime-startTime)/1000)) ) from LightPath where
		 * scheduleId IN ( select id from Schedule where targetEndpointResourceGroup
		 * = 'SystemAdminResourceGroup' and path_targetendpoint_id =
		 * '00-1B-25-2D-5B-E6_ETH-1-1-1' ) AND endTime >= fromTime AND startTime <
		 * toTime AND ( status = 0 or status = 3 or status = 6 or status = 7 )
		 */

		StringBuilder sql = new StringBuilder();
		sql.append("select ( SUM(" + DbKeys.LightPathCols.LP_MBS + "*(("
		    + DbKeys.LightPathCols.LP_ENDTIME + "-"
		    + DbKeys.LightPathCols.LP_STARTTIME + ")/1000)) )");
		sql.append(" from " + TABLENAME + " where ");
		sql.append(DbKeys.LightPathCols.LP_SCHEDULEID + " IN ( select "
		    + DbSchedule.SCHD_ID + " from "
		    + DbSchedule.INSTANCE.getTableName() + " where ");

		if (isSourceInfo) {
			sql.append(DbSchedule.SCHD_SOURCEENDPOINTRESOURCEGROUP + " = ? AND ");
			sql.append(DbSchedule.SCHD_PATH_SOURCEENDPOINT_ID + " = ? ) ");
		}
		else {
			sql.append(DbSchedule.SCHD_TARGETENDPOINTRESOURCEGROUP + " = ? AND ");
			sql.append(DbSchedule.SCHD_PATH_TARGETENDPOINT_ID + " = ? ) ");
		}

		/*
		 * fromtime and toTime are ok from a sql injection perspective, as they are
		 * long values
		 */
		sql.append(" AND " + DbKeys.LightPathCols.LP_ENDTIME + " >= " + fromTime);
		sql.append(" AND " + DbKeys.LightPathCols.LP_STARTTIME + " < " + toTime);
		sql.append(" AND ( ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.CONFIRMATION_PENDING.ordinal() + "' or ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.ACTIVATION_PENDING.ordinal() + "' or ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_PENDING.ordinal() + "' or ");
		sql.append(DbKeys.LightPathCols.LP_STATUS + " = '"
		    + State.SERVICE.EXECUTION_INPROGRESS.ordinal() + "'");
		sql.append(" );");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			Double result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, resourceGroup);
				stmt.setString(2, endpoint);
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				if (rs.next()) {
					result = Double.valueOf(rs.getDouble(1));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return ((Double) dbOp.getResult()).doubleValue();
	}


	public List<ServiceXml> queryServices(long startTime, long endTime)
	    throws Exception {
		/*
		 * Time Conditions condition 1: start time occurs inside our search range
		 * condition 2: end time occurs inside our search range condition condition
		 * 3: start time before start range, and end time after end range (i.e.
		 * executes inside range)
		 */

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME + " where ");
		sql.append(" ( ");

		/*
		 * condition 1: schedule start time occurs inside our search range condition
		 * 2: schedule end time occurs inside our search range condition condition
		 * 3: schedule start time before start range, and end time after end range
		 * (i.e. executes inside range)
		 */
		sql.append(" ( " + DbKeys.LightPathCols.LP_STARTTIME + " >= " + startTime
		    + " AND " + DbKeys.LightPathCols.LP_STARTTIME + " <= " + endTime
		    + " ) ");
		sql.append("or ( " + DbKeys.LightPathCols.LP_ENDTIME + " >= " + startTime
		    + " AND " + DbKeys.LightPathCols.LP_ENDTIME + " <= " + endTime + " ) ");
		sql.append("or ( " + DbKeys.LightPathCols.LP_STARTTIME + " <= " + startTime
		    + " AND " + DbKeys.LightPathCols.LP_ENDTIME + " >= " + endTime + ") ");
		sql.append(" );");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<ServiceXml> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
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
		return (List<ServiceXml>) dbOp.getResult();
	}

	public List<ServiceXml> retrieve(final Map<String, Object> filter)
	    throws Exception {
		boolean flag = false;
		final List<Object> attrList = new ArrayList<Object>();

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME);

		if (filter != null) {

			for (Map.Entry<String, Object> oEntry : filter.entrySet()) {
				String name = oEntry.getKey();
				Object value = oEntry.getValue();

				/*
				 * This will stop SQL injection attacks where the filter contains a name
				 * value pair like "';drop table LightPath"="bob" We already escape the
				 * values, the column keys were not being escaped validated.
				 */

				if (!VALID_FILTER_KEYS.contains(name)) {
					throw new Exception("DbLightPath invalid filter entry " + name + "="
					    + value + "! Name is not a valid filter key from "
					    + VALID_FILTER_KEYS);
				}

				if (flag) {
					sql.append("AND ");
				}
				else {
					sql.append(" where ");
				}

				if (DbKeys.LightPathCols.LP_FILTER_SCHEDULEID_LIST.equals(name)) {
					/*
					 * LP_SCHEDULEID should be a List<String> that holds the schedules to
					 * query
					 */
					List<String> ids = (List<String>) value;
					if (ids != null && !ids.isEmpty()) {
						sql.append(DbKeys.LightPathCols.LP_SCHEDULEID + " IN (");
						boolean needComma = false;
						for (String id : ids) {
							if (needComma) {
								sql.append(",");
							}
							sql.append("? ");
							attrList.add(id);
							needComma = true;
						}
						sql.append(")");
					}
				}
				else if (DbKeys.LightPathCols.LP_ENDTIME_GREATERTHAN_EQUALTO
				    .equals(name)) {
					sql.append(DbKeys.LightPathCols.LP_ENDTIME + " >= ? ");
					attrList.add(Long.valueOf((String) value));
				}
				else if (DbKeys.LightPathCols.LP_ENDTIME_LESSTHAN_EQUALTO.equals(name)) {
					sql.append(DbKeys.LightPathCols.LP_ENDTIME + " <= ? ");
					attrList.add(Long.valueOf((String) value));
				}
				else if (DbKeys.LightPathCols.LP_STARTTIME_GREATERTHAN_EQUALTO
				    .equals(name)) {
					sql.append(DbKeys.LightPathCols.LP_STARTTIME + " >= ? ");
					attrList.add(Long.valueOf((String) value));
				}
				else if (DbKeys.LightPathCols.LP_STARTTIME_LESSTHAN_EQUALTO
				    .equals(name)) {
					sql.append(DbKeys.LightPathCols.LP_STARTTIME + " <= ? ");
					attrList.add(Long.valueOf((String) value));
				}
				else {
					/*
					 * This is not subject to a SQL injection attack, as name has been
					 * validated to be a valid column name above.
					 */
					sql.append(name + " LIKE ? ");
					attrList.add(value);
				}

				flag = true;
			}
		}
		sql.append(";");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<ServiceXml> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				for (int i = 0; i < attrList.size(); i++) {
					Object attr = attrList.get(i);

					if (attr instanceof String) {
						stmt.setString(i + 1, (String) attr);
					}
					else if (attr instanceof Long) {
						stmt.setLong(i + 1, ((Long) attr).longValue());
					}
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
		return (List<ServiceXml>) dbOp.getResult();
	}

	public void update(final Map<String, String> filterMap,
	    final Map<String, String> dataToUpdate) throws Exception {

		boolean flag = false;
		final List<Object> attrList = new ArrayList<Object>();
		final String whereClauseRecord;
		String tempWhereClauseRecord = null;

		StringBuilder sql = new StringBuilder();
		sql.append("update " + TABLENAME + " SET ");

		if (dataToUpdate != null) {
			for (Map.Entry<String, String> oEntry : dataToUpdate.entrySet()) {
				String name = oEntry.getKey();
				String value = oEntry.getValue();

				/*
				 * This will stop SQL injection attacks where the map contains a name
				 * value pair like "';drop table LightPath;" = "bob" We already escape
				 * the values, the column keys were not being escaped or validated.
				 */

				if (!VALID_FILTER_KEYS.contains(name)) {
					throw new Exception("DbLightPath invalid column entry " + name + "="
					    + value + "! Name is not a valid column key from "
					    + VALID_FILTER_KEYS);
				}

				if (flag) {
					sql.append(", ");
				}
				sql.append(name + " = ? ");
				attrList.add(value);
				flag = true;
			}
		}

		if (flag) {
			boolean idFlag = false;

			if (filterMap != null) {
				if (filterMap.containsKey(DbKeys.LightPathCols.LP_SERVICEID)) {
					idFlag = true;
					sql.append("where " + DbKeys.LightPathCols.LP_SERVICEID + " LIKE ? ");
					tempWhereClauseRecord = filterMap
					    .get(DbKeys.LightPathCols.LP_SERVICEID);
				}
				else if (filterMap.containsKey(DbKeys.LightPathCols.LP_CALLID)) {
					idFlag = true;
					sql.append("where " + DbKeys.LightPathCols.LP_CALLID + " LIKE ? ");
					tempWhereClauseRecord = filterMap.get(DbKeys.LightPathCols.LP_CALLID);
				}
			}

			if (!idFlag) {
				log.error(TABLENAME + " update called for unsupported data.");
				return;
			}
		}
		else {
			log.error(TABLENAME + " update called for unsupported data.");
			return;
		}

		whereClauseRecord = tempWhereClauseRecord;

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				int i;
				for (i = 0; i < attrList.size(); i++) {
					Object attr = attrList.get(i);

					if (attr instanceof String) {
						stmt.setString(i + 1, (String) attr);
					}
					else if (attr instanceof Long) {
						stmt.setLong(i + 1, ((Long) attr).longValue());
					}
				}

				/*
				 * We permit one where statement to be set, note we'll set this even if
				 * we didn't have a where clause.
				 */
				stmt.setString(i + 1, whereClauseRecord);
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
	}

	private ServiceXml processRetrieveResult(ResultSet rs) throws Exception {
		String callid = rs.getString(DbKeys.LightPathCols.LP_CALLID);
		ServiceXml serviceXml = new ServiceXml(
		    State.SERVICE.values()[Integer.parseInt(rs
		        .getString(DbKeys.LightPathCols.LP_STATUS))], callid,
		    rs.getString(DbKeys.LightPathCols.LP_SERVICEID),
		    rs.getString(DbKeys.LightPathCols.LP_ACTIVATIONTYPE),
		    rs.getString(DbKeys.LightPathCols.LP_CONTROLLERID),
		    rs.getString(DbKeys.LightPathCols.LP_SCHEDULEID),
		    rs.getString(DbKeys.LightPathCols.LP_SCHEDULENAME),
		    rs.getString(DbKeys.LightPathCols.LP_VCAT),
		    rs.getLong(DbKeys.LightPathCols.LP_STARTTIME),
		    rs.getLong(DbKeys.LightPathCols.LP_ENDTIME),
		    rs.getString(DbKeys.LightPathCols.LP_USER),
		    rs.getString(DbKeys.LightPathCols.LP_BILLINGGROUP), null,
		    rs.getString(DbKeys.LightPathCols.LP_PRIORITY),
		    rs.getInt(DbKeys.LightPathCols.LP_MBS),
		    rs.getString(DbKeys.LightPathCols.LP_RATE),
		    rs.getString(DbKeys.LightPathCols.LP_AEND),
		    rs.getString(DbKeys.LightPathCols.LP_ZEND));

		// retrieve the edge element to construct the path
		Map<String, String> filter = new HashMap<String, String>();
		filter.put(DbKeys.LightPathCols.LP_CALLID, callid);
		serviceXml.putEdges(DbLightPathEdge.INSTANCE.retrieve(filter));

		// For 1+1 support:
		byte[] data = (byte[]) rs
		    .getObject(DbKeys.LightPathCols.LP_1PLUS1_PATH_DATA);
		if (data != null) {
			try {
				List<List<CrossConnection>> paths = (List<List<CrossConnection>>) DbOpsHelper
				    .deserialize(data);
				serviceXml.addPath(XC_TYPE.WORKING, paths.get(0));
				serviceXml.addPath(XC_TYPE.PROTECTION, paths.get(1));
			}
			catch (Exception e) {
				/**
				 * Upgrade warning, at one point we stored List<String> in the database
				 * instead of a List<List<CrossConnection>> if we attempt to read an old
				 * (protected) service record we'll fail to cast/extract the working and
				 * protection paths... this is ok as we can live without this
				 * information, the admin console won't color the service in but thats
				 * it.
				 */
				log.error(
				    "Error unpacking 1+1 protection blob, is it in the old format? Ignored. ",
				    e);
			}
		}

		String xmlString = rs.getString(DbKeys.LightPathCols.LP_XML);
		if (xmlString != null && xmlString.length() > 0) {

			serviceXml.setXMLUserData(xmlString);
		}

		return serviceXml;
	}

	private List<ServiceXml> processRetrieveResultList(ResultSet rs)
	    throws Exception {
		List<ServiceXml> result = new ArrayList<ServiceXml>();
		while (rs.next()) {
			result.add(processRetrieveResult(rs));
		}

		return result;
	}
	
	 public String findServiceIdByScheduleId(final String scheduleId){
	    final String sql = "select serviceId from " + TABLENAME + " where scheduleId = ?";
	    try {
	      final Connection connection = DbOperationsManager.INSTANCE.getDatasource().getConnection();
	      final PreparedStatement preparedStatement = connection.prepareStatement(sql);
	      preparedStatement.setString(1, scheduleId);
	      preparedStatement.execute();
	      final ResultSet resultSet = preparedStatement.getResultSet();
        String result = null;
        if (resultSet != null && resultSet.next()) {
          result = resultSet.getString("serviceId");
        }
	      DbOperationsManager.INSTANCE.close(connection, preparedStatement);
	      return result;
	    }
	    catch (SQLException e) {
	      log.error("Error: ", e);
	      return null;
	    }
	  }

}
