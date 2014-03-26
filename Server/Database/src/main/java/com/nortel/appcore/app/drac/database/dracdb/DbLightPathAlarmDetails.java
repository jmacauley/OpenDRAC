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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsI;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * @author pitman
 */
public enum DbLightPathAlarmDetails {
	INSTANCE;
	public static final String ALARMID = "id";
	public static final String DURATION = "duration";
	public static final String NEID = "neid";

	public static final String TIME = "time";
	/*
	 * for search filter ONLY. The neid is actually key'ed as 'id' in the xml
	 * element...so it's overloaded and must be dealt with throughout.
	 */

	private static final String NODE = "node";
	private static final String XML = "xml";
	private static final String TABLENAME = "LightPathAlarmDetails";

	// filter options
	public static final String TIME_GREATERTHAN_EQUALTO = "endtime_greaterthan_equalto";
	public static final String TIME_LESSTHAN_EQUALTO = "endtime_lessthan_equalto";

	public void add(final Element eventElement) throws Exception {
		/*
		 * Sample input: <event name="alarm"
		 * id="00-1B-25-2D-5B-E6_0100033489-1005-0115" owner="TDEFAULT_PROXY"
		 * time="1227556734905" duration="0"> <eventInfo notificationType="MJ"
		 * occurredDate="2001-07-19" occurredTime="07-58-46" /> <data> <element
		 * name="description" value="Unequipped" /> <element name="aid"
		 * value="STS3C-1-11-1-1" /> <element name="facility" value="OC12-1-11-1" />
		 * <element name="channel" value="1" /> => NOTE: The data element can list
		 * affected services: <element name="serviceId"
		 * value="SERVICE-1227556729295" /> <element name="serviceId"
		 * value="SERVICE-1227794449669"/> </data> <node type="OME"
		 * id="00-1B-25-2D-5B-E6" ip="47.134.3.229" port="10001" tid="OME0237"
		 * mode="SONET" status="aligned" /> </event>
		 */

		String sql = "insert into " + TABLENAME + " ( "
		    + DbLightPathAlarmDetails.ALARMID + ", " + DbLightPathAlarmDetails.NEID
		    + ", " + DbLightPathAlarmDetails.TIME + ", "
		    + DbLightPathAlarmDetails.DURATION + ", " + DbLightPathAlarmDetails.XML
		    + ") values (?, ?, ?, ?, ?);";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1,
				    eventElement.getAttributeValue(DbLightPathAlarmDetails.ALARMID));
				Element node = eventElement.getChild(DbLightPathAlarmDetails.NODE);
				// overloaded key: 'id'
				stmt.setString(2, node.getAttributeValue("id"));
				stmt.setLong(3, Long.parseLong(eventElement
				    .getAttributeValue(DbLightPathAlarmDetails.TIME)));
				stmt.setLong(4, Long.parseLong(eventElement
				    .getAttributeValue(DbLightPathAlarmDetails.DURATION)));
				stmt.setObject(5, DbOpsHelper.elementToString(eventElement));
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
	}

	public void appendAlarmData(final String alarmId,
	    final Element dataElementToAdd) throws Exception {
		// e.g. data = <element name=reason value=cleared by audit />";
		/*
		 * <event duration="194854" id="00-1B-25-2D-5C-7A_0100000425-0001-0148"
		 * name="alarm" owner="TDEFAULT_PROXY" time="1227722945349"> <eventInfo
		 * notificationType="CR" occurredDate="2001-07-21" occurredTime="06-06-53"/>
		 * <data> <element name="description" value="AIS"/> <element name="aid"
		 * value="OC12-1-11-1"/> <element name="facility" value="OC12-1-11-1"/>
		 * <element name="serviceId" value="SERVICE-1227722874473"/> => <element
		 * name="reason" value="cleared by audit"/> </data> <node
		 * id="00-1B-25-2D-5C-7A" ip="47.134.3.230" mode="SONET" port="10001"
		 * status="aligned" tid="OME0039" type="OME"/> </event>
		 */

		// [1] Retrieve:
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(DbLightPathAlarmDetails.ALARMID, alarmId);
		List<Element> list = retrieve(filter);
		if (list != null && list.size() > 0) {
			final Element alarmEvent = list.get(0);

			// [2] Update
			final Element dataElement = alarmEvent.getChild("data");
			if (dataElement == null || dataElementToAdd == null) {
				return;
			}
			dataElement.addContent(dataElementToAdd);

			// [3] Write it back
			String sql = "update " + TABLENAME + " SET " + XML + " = ? WHERE "
			    + ALARMID + " = ? ;";

			DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
				@Override
				public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
				    throws Exception {
					stmt.setObject(1, DbOpsHelper.elementToString(alarmEvent));
					stmt.setString(2, alarmId);

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

	public void deleteServiceReference(final List<String> alarmIds,
	    final String serviceId) throws Exception {
		// For each alarmId: retrieve the record and remove the serviceId reference.
		// If this serviceId was the last one removed, remove the alarm record.

		/*
		 * <event name="alarm" id="00-1B-25-2D-5B-E6_0100033489-1005-0115"
		 * owner="TDEFAULT_PROXY" time="1227556734905" duration="0"> <eventInfo
		 * notificationType="MJ" occurredDate="2001-07-19" occurredTime="07-58-46"
		 * /> <data> <element name="description" value="Unequipped" /> <element
		 * name="aid" value="STS3C-1-11-1-1" /> <element name="facility"
		 * value="OC12-1-11-1" /> <element name="channel" value="1" /> -> <element
		 * name="serviceId" value="SERVICE-1227556729295" /> -> <element
		 * name="serviceId" value="SERVICE-1227794449669"/> </data> <node type="OME"
		 * id="00-1B-25-2D-5B-E6" ip="47.134.3.229" port="10001" tid="OME0237"
		 * mode="SONET" status="aligned" /> </event>
		 */

		final Map<String, Element> recordsToModify = new HashMap<String, Element>();
		final List<String> recordsToDelete = new ArrayList<String>();

		for (String alarmId : alarmIds) {
			boolean wasModified = false;

			// [1] Retrieve:
			Map<String, Object> filter = new HashMap<String, Object>();
			filter.put(DbLightPathAlarmDetails.ALARMID, alarmId);
			List<Element> list = retrieve(filter);

			if (list != null && list.size() > 0) {
				final Element alarmEvent = list.get(0);

				// Remove the serviceId
				List<Element> dataChildren = alarmEvent.getChild("data").getChildren();
				for (Iterator<Element> iter = dataChildren.iterator(); iter.hasNext();) {
					Element el = iter.next();

					if (DbKeys.LightPathAlarmSummariesCols.SERVICEID.equals(el
					    .getAttributeValue("name"))
					    && serviceId.equals(el.getAttributeValue("value"))) {
						iter.remove();
						wasModified = true;
						break;
					}
				}

				boolean hasServiceIds = false;
				for (Element e : list) {
					if (e.getAttribute("serviceId") != null) {
						hasServiceIds = true;
						break;
					}
				}

				/*
				 * Don't expect any record to NOT modify, since we passed in a list of
				 * alarmIds that were known to map to a deleting serviceId. If this
				 * isn't the case, there has to be an inconsistency between the summary
				 * and detail lightPath alarm tables!!!
				 */
				if (wasModified) {
					// Should not arrive here without modifications!
					if (hasServiceIds) {
						recordsToModify.put(alarmId, alarmEvent);
					}
					else {
						recordsToDelete.add(alarmId);
					}
				}

				// Updates
				if (recordsToModify.size() > 0) {
					String sql = "update " + TABLENAME + " SET " + XML + " = ?  "
					    + "where " + ALARMID + " = ?;";
					DbBatchWithResultsI dbBatch = new DbBatchWithResultsAdapter(
					    recordsToModify.size()) {
						@Override
						public PreparedStatement buildPreparedStatement(
						    PreparedStatement stmt) throws Exception {
							for (Map.Entry<String, Element> entry : recordsToModify
							    .entrySet()) {
								String alarmId = entry.getKey();
								Element alarmEvent = entry.getValue();

								stmt.setObject(1, DbOpsHelper.elementToString(alarmEvent));
								stmt.setString(2, alarmId);
								stmt.addBatch();
							}

							return stmt;
						}
					};
					DbOperationsManager.INSTANCE.executeDbBatchWithResults(sql, dbBatch);
				}

				// Deletes
				if (recordsToDelete.size() > 0) {
					String sql = "delete from " + TABLENAME + " where " + ALARMID
					    + " = ?;";
					DbBatchWithResultsI dbBatch = new DbBatchWithResultsAdapter(
					    recordsToDelete.size()) {
						@Override
						public PreparedStatement buildPreparedStatement(
						    PreparedStatement stmt) throws Exception {
							for (String alarmId : recordsToDelete) {
								stmt.setString(1, alarmId);
								stmt.addBatch();
							}

							return stmt;
						}
					};
					DbOperationsManager.INSTANCE.executeDbBatchWithResults(sql, dbBatch);
				}
			}
		}
	}

	public List<String> getNeWithActiveAlarm() throws Exception {
		/*
		 * select DISTINCT neid from NetworkAlarm where duration = '0'
		 */

		String sql = "select DISTINCT " + DbLightPathAlarmDetails.NEID + " from "
		    + DbLightPathAlarmDetails.TABLENAME + " where "
		    + DbLightPathAlarmDetails.DURATION + " = 0 ;";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<String> result;

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
				result = new ArrayList<String>();

				while (rs.next()) {
					result.add(rs.getString(DbLightPathAlarmDetails.NEID));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
		return (List<String>) dbOp.getResult();
	}

	public List<Element> retrieve(final Map<String, Object> filter)
	    throws Exception {

		boolean flag = false;
		final List<Object> attrList = new ArrayList<Object>();

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME);

		if (filter != null) {
			Set<Map.Entry<String, Object>> entrySet = filter.entrySet();
			Iterator<Map.Entry<String, Object>> itr = entrySet.iterator();
			while (itr.hasNext()) {
				Map.Entry<String, Object> oEntry = itr.next();
				String name = oEntry.getKey();
				Object value = oEntry.getValue();

				if (flag) {
					sql.append("AND ");
				}
				else {
					sql.append(" where ");
				}

				if (DbLightPathAlarmDetails.TIME_GREATERTHAN_EQUALTO.equals(name)) {
					sql.append(DbLightPathAlarmDetails.TIME + " >= ? ");
					Long f = (Long) filter
					    .get(DbLightPathAlarmDetails.TIME_GREATERTHAN_EQUALTO);
					attrList.add(f);
				}
				else if (DbLightPathAlarmDetails.TIME_LESSTHAN_EQUALTO.equals(name)) {
					sql.append(DbLightPathAlarmDetails.TIME + " <= ? ");
					Long f = (Long) filter
					    .get(DbLightPathAlarmDetails.TIME_LESSTHAN_EQUALTO);
					attrList.add(f);
				}
				else {
					if (ALARMID.equals(name) || DURATION.equals(name)
					    || NEID.equals(name)) {
						// We avoid SQL injection attacks by validating the name before we
						// add it.
						sql.append(name + " = ? ");
						attrList.add(value);
					}
					else {
						throw new Exception("Invalid filter " + name);
					}
				}

				flag = true;
			}
		}

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<Element> result;

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
				result = new ArrayList<Element>();

				while (rs.next()) {
					Element event = DbOpsHelper.xmlToElement(rs
					    .getString(DbLightPathAlarmDetails.XML));
					event.setAttribute(DURATION,
					    rs.getString(DbLightPathAlarmDetails.DURATION));
					result.add(event);
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (List<Element>) dbOp.getResult();
	}

	/**
	 * The only field that updates in this table is 'duration', so I'm not going
	 * to provide a generalized 'update' method with a filter parameter map
	 */
	public void updateAlarmDuration(final String alarmId, final long duration)
	    throws Exception {
		String sql = "update " + TABLENAME + " SET " + DURATION + " = ? WHERE "
		    + ALARMID + " = ? ;";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setLong(1, duration);
				stmt.setString(2, alarmId);
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
	}

}
