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

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * @author pitman
 */
public enum DbLightPathAlarmSummaries {
	INSTANCE;
	/*
	 * TABLENAME is not public so that anyone outside of this class must use
	 * getInstance().getTableName() to access it and thus insure the table has
	 * been created before its accessed.
	 */
	private static final String TABLENAME = "LightPathAlarmSummaries";

	/**
	 * neid is to match on LightPathEdge 'Source' ... see old implementation of
	 * DbUtilityLpcpScheduler::insertAlarm
	 */
	public void add(final String neid, final Element eventElement)
	    throws Exception {
		/*
		 * Sample input: <alarm id="00-1B-25-2D-5B-E6_0100033553-1005-0115"
		 * severity="MJ" occurredTime="1227579230936"
		 * serviceId="SERVICE-1227579225012"/>
		 */

		String sql = "insert into " + TABLENAME + " ( "
		    + DbKeys.LightPathAlarmSummariesCols.ALARMID + ", "
		    + DbKeys.LightPathAlarmSummariesCols.SEVERITY + ", "
		    + DbKeys.LightPathAlarmSummariesCols.OCCURREDTIME + ", "
		    + DbKeys.LightPathAlarmSummariesCols.SERVICEID + ", "
		    + DbKeys.LightPathAlarmSummariesCols.SOURCE
		    + ") values (?, ?, ?, ?, ?) ";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, eventElement
				    .getAttributeValue(DbKeys.LightPathAlarmSummariesCols.ALARMID));
				stmt.setString(2, eventElement
				    .getAttributeValue(DbKeys.LightPathAlarmSummariesCols.SEVERITY));
				stmt.setLong(
				    3,
				    Long.parseLong(eventElement
				        .getAttributeValue(DbKeys.LightPathAlarmSummariesCols.OCCURREDTIME)));
				stmt.setString(4, eventElement
				    .getAttributeValue(DbKeys.LightPathAlarmSummariesCols.SERVICEID));
				stmt.setString(5, neid);
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

	public List<String> deleteByServiceId(final String serviceId)
	    throws Exception {
		// [1] Retrieve the affected alarmIds
		// select DISTINCT id from LightPathAlarm where serviceId =
		// 'SERVICE-1227797492678';
		String sqlQueryIds = "select DISTINCT "
		    + DbKeys.LightPathAlarmSummariesCols.ALARMID + " from " + TABLENAME
		    + " where " + DbKeys.LightPathAlarmSummariesCols.SERVICEID + " = ?;";

		DbOpWithResultsAdapter dbOpQuery = new DbOpWithResultsAdapter() {
			List<String> alarmIds;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, serviceId);
				return stmt;
			}

			@Override
			public Object getResult() {
				return alarmIds;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				alarmIds = new ArrayList<String>();
				while (rs.next()) {
					alarmIds
					    .add(rs.getString(DbKeys.LightPathAlarmSummariesCols.ALARMID));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sqlQueryIds, dbOpQuery);
		List<String> alarmIds = (List<String>) dbOpQuery.getResult();

		// [2] Delete the records
		String sqlDelete = "delete from " + TABLENAME + " where "
		    + DbKeys.LightPathAlarmSummariesCols.SERVICEID + " = ? ;";
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, serviceId);
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sqlDelete, dbOp);

		return alarmIds;
	}

	public String getTableName() {
		return TABLENAME;
	}
}
