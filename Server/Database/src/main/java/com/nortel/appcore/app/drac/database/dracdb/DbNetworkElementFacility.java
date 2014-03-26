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

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbBatchWithResultsI;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOpWithResultsI;
import com.nortel.appcore.app.drac.database.DbOperationsManager;

/**
 * @author pitman
 */
public enum DbNetworkElementFacility {
	INSTANCE;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private static class Reader implements DbOpWithResultsI {
		private final List<String> attrList;
		private final List<Facility> fac = new ArrayList<Facility>();

		public Reader(List<String> attributes) {
			attrList = attributes;
		}

		/*
		 * An opportunity to tweak outgoing responses. First use: avoid db upgrade
		 * procedure and minimize changes to upper level getters by writing in a
		 * default userLabel if not present.
		 */
		public void adjustMapParameters(Map<String, String> map) {
			if (!map.containsKey(FacilityConstants.FACLABEL_ATTR)) {
				map.put(FacilityConstants.FACLABEL_ATTR,
				    FacilityConstants.DEFAULT_FACLABEL);
			}
		}

		@Override
		public PreparedStatement buildPreparedStatement(PreparedStatement statement)
		    throws Exception {
			if (attrList != null) {
				for (int i = 0; i < attrList.size(); i++) {
					statement.setString(i + 1, attrList.get(i));
				}
			}
			return statement;
		}

		public List<Facility> getResult() {
			return fac;
		}

		@Override
		public void processResults(ResultSet rs) throws Exception {
			while (rs.next()) {
				Map<String, String> facMap = DbOpsHelper.elementToMap(XmlUtility
				    .createDocumentRoot(rs.getString(XML)));
				// Dunk in the pk for use in the update method
				facMap.put(DbKeys.NetworkElementFacilityCols.PK,
				    rs.getString(DbKeys.NetworkElementFacilityCols.PK));
				adjustMapParameters(facMap);
				fac.add(new Facility(facMap));
			}
		}

		@Override
		public void setUpdateCount(int count) throws Exception {
			return;
		}

	}

	private static final String TABLENAME = "NetworkElementFacility";
	private static final String XML = "xml";

	/**
	 * @deprecated use Facility.asElement
	 */
	@Deprecated
	public static Element facMapToElement(Map<String, String> facMap) {
		Map<String, String> tmpMap = new TreeMap<String, String>(facMap);
		String rootLayer = tmpMap.get(DbKeys.NetworkElementFacilityCols.LAYER);
		tmpMap.remove(DbKeys.NetworkElementFacilityCols.LAYER);
		return DbOpsHelper.mapToElement(rootLayer, tmpMap);
	}

	public void addFacility(final String neid, final String neip,
	    final int neport, final List<Facility> data) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into " + TABLENAME + "(");
		sql.append(DbKeys.NetworkElementFacilityCols.PK + ", ");
		sql.append(DbKeys.NetworkElementFacilityCols.LAYER + ", ");
		sql.append(DbKeys.NetworkElementFacilityCols.SHELF + ", ");
		sql.append(DbKeys.NetworkElementFacilityCols.SLOT + ", ");
		sql.append(DbKeys.NetworkElementFacilityCols.PORT + ", ");
		sql.append(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE + ", ");
		sql.append(DbKeys.NetworkElementFacilityCols.SIGTYPE + ", ");
		sql.append(DbKeys.NetworkElementFacilityCols.TNA + ", ");
		sql.append(DbKeys.NetworkElementFacilityCols.SITE + ", ");
		sql.append(XML);
		sql.append(" ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ? );");

		DbBatchWithResultsI dbBatch = new DbBatchWithResultsAdapter(data.size()) {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				for (Facility fac : data) {
					Map<String, String> facMap = new TreeMap<String, String>(
					    fac.asUnmodifiableMap());
					// Dunk in the ne identifiers to facilitate caller
					// formatting:
					facMap.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, neid);
					facMap.put(DbKeys.NetworkElementFacilityCols.NEIP_FOR_FAC, neip);
					facMap.put(DbKeys.NetworkElementFacilityCols.NEPORT_FOR_FAC,
					    Integer.toString(neport));

					stmt.setString(1, encodePK(neid, fac.getAid()));
					stmt.setString(2, fac.getLayer().toString());
					stmt.setString(3, fac.getShelf());
					stmt.setString(4, fac.getSlot());
					stmt.setString(5, fac.getPort());
					stmt.setString(6, fac.getPrimaryState());
					stmt.setString(7, fac.getSigType());
					stmt.setString(8, fac.getTna());
					stmt.setString(9, fac.getSite());
					stmt.setString(10, XmlUtility.rootNodeToString(DbOpsHelper
					    .mapToElement("WP", facMap)));
					stmt.addBatch();
				}

				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbBatchWithResults(sql.toString(),
		    dbBatch);
	}

	public void delete(final AbstractNetworkElement ne, final String aid)
	    throws Exception {
		delete(ne.getNeId(), aid);
	}

	public void delete(final String neId, final String aid) throws Exception {
		String sql = "delete from " + TABLENAME + " where "
		    + DbKeys.NetworkElementFacilityCols.PK + " LIKE ?;";
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, encodePK(neId, aid));
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
	}

	/**
	 * For testing only drops and recreates database.
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults("delete from "
		    + TABLENAME + ";", new DbOpWithResultsAdapter());
	}

	public void deleteAll(final String neId) throws Exception {
		delete(neId, "%");
	}

	public Map<String, BigInteger> getFacilityConstraints(
	    Map<String, String> filter) throws Exception {
		Map<String, BigInteger> constraintsMap = new HashMap<String, BigInteger>();

		List<Facility> facList = DbNetworkElementFacility.INSTANCE
		    .retrieveFacilities(filter);
		for (Facility fac : facList) {
			constraintsMap.put(
			    fac.get(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC) + "_"
			        + fac.get(DbKeys.NetworkElementFacilityCols.AID),
			    new java.math.BigInteger(fac
			        .get(DbKeys.NetworkElementFacilityCols.CONSTRAIN)));
		}

		return constraintsMap;
	}

	public void invalidateAllFacilities() throws Exception {
		// The implementation must consider a server coming up in the presence
		// of NEs no longer connected, failing authentication, etc. Discovery
		// will have cleared those NEIds
		// (00-00-00-00-00), so discovery NEs cannot be used to reference
		// facility records in all cases.
		// The validity flag is stored in the xml blob, so each record has to be
		// pulled, edited, and saved.

		// We'll scale to a network element at least ...
		// SELECT DISTINCT (SUBSTRING_INDEX(pk, '_', 1)) FROM
		// drac.NetworkElementFacility;

		String query = "SELECT DISTINCT SUBSTRING("
		    + DbKeys.NetworkElementFacilityCols.PK + ", 1, LOCATE('"
		    + EndPointType.FACILITY_ENDPOINT_RESOURCE_ID_DELIMITER + "', "
		    + DbKeys.NetworkElementFacilityCols.PK + ") - 1) FROM " + TABLENAME
		    + ";";

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<String> neIEEEs;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				return stmt;
			}

			@Override
			public Object getResult() {
				return neIEEEs;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				neIEEEs = new ArrayList<String>();
				while (rs.next()) {
					neIEEEs.add(rs.getString(1));
				}

			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(query, dbOp);

		Map<String, String> data = new HashMap<String, String>();
		data.put(DbKeys.NetworkElementFacilityCols.VALID, "false");
		boolean addIfAbsent = false; // update only

		for (String neIEEE : (List<String>) dbOp.getResult()) {
			update(neIEEE, data, addIfAbsent);
		}
	}

	public List<Map<String, String>> retrieve(final Map<String, String> filter)
	    throws Exception {
		// Allow for passing a single PK through the filter
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.PK)) {
			List<String> resourceEndpointIds = new ArrayList<String>();
			resourceEndpointIds.add(filter.get(DbKeys.NetworkElementFacilityCols.PK));
			return retrieve(resourceEndpointIds, filter);
		}
		return retrieve(null, filter);
	}

	public List<Facility> retrieveFacilities(final Map<String, String> filter)
	    throws Exception {
		// Allow for passing a single PK through the filter
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.PK)) {
			List<String> resourceEndpointIds = new ArrayList<String>();
			resourceEndpointIds.add(filter.get(DbKeys.NetworkElementFacilityCols.PK));
			return retrieveFacilities(resourceEndpointIds, filter);
		}
		return retrieveFacilities(null, filter);
	}

	/**
	 * @deprecated use retrieveUserFacilities
	 */
	@Deprecated
	public List<Map<String, String>> retrieveUserEndpoints(
	    List<String> resourceEndpointIds, final Map<String, String> facilityFilter)
	    throws Exception {
		// We'll add to the facilityFilter that was passed in, in order to scope
		// to user endpoints only:
		facilityFilter.put(DbKeys.NetworkElementFacilityCols.TNA_SET, "true");
		facilityFilter.put(DbKeys.NetworkElementFacilityCols.SIGTYPE,
		    FacilityConstants.SIGTYPE_DRACUSERENDPOINT);
		return retrieve(resourceEndpointIds, facilityFilter);
	}

	/**
	 * @deprecated use retrieveUserFacilities
	 */
	@Deprecated
	public List<Map<String, String>> retrieveUserEndpoints(
	    final Map<String, String> facilityFilter) throws Exception {
		// Allow for passing a single PK through the filter
		if (facilityFilter != null
		    && facilityFilter.containsKey(DbKeys.NetworkElementFacilityCols.PK)) {
			List<String> resourceEndpointIds = new ArrayList<String>();
			resourceEndpointIds.add(facilityFilter
			    .get(DbKeys.NetworkElementFacilityCols.PK));

			return retrieveUserEndpoints(resourceEndpointIds, facilityFilter);
		}
		return retrieveUserEndpoints(null, facilityFilter);
	}

	/**
	 * Update all facility records for a NE with the given data map. Typically
	 * used to reset or mark all as invalid.
	 */
	public void update(final AbstractNetworkElement ne,
	    final Map<String, String> data, boolean addIfAbsent) throws Exception {
		update(ne, "%", data, addIfAbsent);
	}

	public void update(final AbstractNetworkElement ne, final String aid,
	    final Map<String, String> data, boolean addIfAbsent) throws Exception {

		log.debug("Update facility for NE " + ne.getIpAddress() + ":"
		    + ne.getPortNumber() + " neId:" + ne.getNeId() + " name:"
		    + ne.getNeName() + " aid:" + aid + " data:" + data + " addIfAbsent:"
		    + addIfAbsent);

		update(ne.getNeId(), aid, data, addIfAbsent);

	}

	public void update(final String neIEEE, final Map<String, String> data,
	    boolean addIfAbsent) throws Exception {
		update(neIEEE, "%", data, addIfAbsent);
	}

	/**
	 * Update Takes a map of new or updated attributes and applies them on top of
	 * the existing record(s). If addIfAbsent is true then any new attributes are
	 * added, otherwise only existing attributes are updated.
	 * <p>
	 * Note that AID can be a database wildcard in which case the update will
	 * apply to multiple records (we add the attribute isvalid=false to all rows )
	 * <p>
	 * See 'NOTES ON THE SCHEMA' - all attribute adds/updates are done in the blob
	 * addOrUpdate: false = update only existing attributes; true = add as new
	 * attribute if not already present
	 */
	public void update(final String neIEEE, final String aid,
	    final Map<String, String> data, boolean addIfAbsent) throws Exception {

		// [1] First pass - retrieve all affected rows:
		Map<String, String> filter = new HashMap<String, String>();
		filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, neIEEE);
		filter.put(DbKeys.NetworkElementFacilityCols.AID, aid);

		final List<Facility> facsToUpdate = retrieveFacilities(filter);
		final List<Facility> updatedFacs = new ArrayList<Facility>();

		// [2.1] Second pass - update the affected records

		for (Facility fac : facsToUpdate) {
			Map<String, String> m = new TreeMap<String, String>(
			    fac.asUnmodifiableMap());
			Map<String, String> updates = new TreeMap<String, String>();
			List<String> notUpdated = new ArrayList<String>();
			boolean changed = false;
			for (Map.Entry<String, String> e : data.entrySet()) {
				boolean addOverride = FacilityConstants.FACLABEL_ATTR
				    .equals(e.getKey());

				if (fac.containsKey(e.getKey()) || addIfAbsent || addOverride) {
					// Check if the value is actually changed or not, if no
					// change, dont update it
					if (m.containsKey(e.getKey())
					    && e.getValue().equals(m.get(e.getKey()))) {
						notUpdated.add(e.getKey());
					}
					else {
						updates.put(e.getKey(), e.getValue());
						changed = true;
					}
				}
			}

			if (changed) {
				log.debug("Updating these facility attributes "
				    + updates
				    + (notUpdated.size() > 0 ? " the following attributes values did not change and were not updated "
				        + notUpdated
				        : ""));
				m.putAll(updates);
				updatedFacs.add(new Facility(m));
			}
			else {

			}
		}

		if (updatedFacs.isEmpty()) {

			return;
		}

		// [2.2] ... and write them back :
		// ... using a batch update

		StringBuilder sql = new StringBuilder();
		sql.append("update " + TABLENAME + " SET ");
		// catch all: the blob will update with the entire record
		sql.append(XML + " = ?  ");
		// searchable column is updated
		sql.append(", " + DbKeys.NetworkElementFacilityCols.PRIMARYSTATE + " = ? ");
		// searchable column is updated
		sql.append(", " + DbKeys.NetworkElementFacilityCols.SIGTYPE + " = ? ");
		// searchable column is updated
		sql.append(", " + DbKeys.NetworkElementFacilityCols.TNA + " = ? ");
		// searchable column is updated
		sql.append(", " + DbKeys.NetworkElementFacilityCols.SITE + " = ? ");
		sql.append("where " + DbKeys.NetworkElementFacilityCols.PK + " LIKE ?;");

		DbBatchWithResultsI dbBatch = new DbBatchWithResultsI() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				for (Facility fac : updatedFacs) {
					stmt.setString(
					    1,
					    XmlUtility.rootNodeToString(DbOpsHelper.mapToElement("WP",
					        fac.asUnmodifiableMap())));
					stmt.setString(2, fac.getPrimaryState());
					stmt.setString(3, fac.getSigType());
					stmt.setString(4, fac.getTna());
					stmt.setString(5, fac.getSite());
					stmt.setString(6, fac.get(DbKeys.NetworkElementFacilityCols.PK));
					stmt.addBatch();
				}
				return stmt;
			}

			@Override
			public void setUpdateCount(int[] count) throws Exception {
				return;
			}
		};
		DbOperationsManager.INSTANCE.executeDbBatchWithResults(sql.toString(),
		    dbBatch);

	}

	protected List<Facility> retrieveFacilities(List<String> resourceEndpointIds,
	    final Map<String, String> filter) throws Exception {
		boolean flag = false;
		final List<String> attrList = new ArrayList<String>();

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME + " T1");

		// First, prepare the primary key search:
		if (resourceEndpointIds != null && resourceEndpointIds.size() > 0) {
			StringBuilder commaListResourceEndpointIds = new StringBuilder();
			for (String id : resourceEndpointIds) {
				if (commaListResourceEndpointIds.length() != 0) {
					commaListResourceEndpointIds.append(",");
				}

				commaListResourceEndpointIds.append("'" + id + "'");
			}

			sql.append(" where " + DbKeys.NetworkElementFacilityCols.PK + " IN ( "
			    + commaListResourceEndpointIds.toString() + " ) ");

			flag = true;
		}

		// And now prepare for additional filtering:

		String s = null;
		String neid = "%";
		String aid = "%";

		// NOTE that the primary key list above will take precedence over any
		// primary key 'filtering' here.
		// e.g. the primary key list above evaluates a result set first, then
		// here, an neid primary key filter
		// will further refine that result set.
		if (filter != null) {
			neid = (s = filter.get(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC)) != null ? s
			    : "%";
			aid = (s = filter.get(DbKeys.NetworkElementFacilityCols.AID)) != null ? s
			    : "%";
		}

		String pk = encodePK(neid, aid);

		// Check for a primary key - accommodates for substring matching
		if (pk != null) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}

			sql.append("T1." + DbKeys.NetworkElementFacilityCols.PK + " LIKE ? ");
			attrList.add(pk);

			flag = true;
		}

		// Add additional search criteria checks here
		/*
		 * if (filter != null && filter.containsKey(BLA)) { if (flag) {
		 * sb.append("AND "); } else { sb.append(" where "); } b.append(BLA +
		 * " = ? "); attrList.add(data.get(BLA)); flag = true; }
		 */

		// Attr: LAYER ... exact match only
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.LAYER)) {
			String layerString = filter.get(DbKeys.NetworkElementFacilityCols.LAYER);
			Layer layerEnum = Layer.toEnum(layerString);

			// Layer.LAYER_ALL means all of course, so no filter on layer
			if (layerEnum != Layer.LAYER_ALL) {
				if (flag) {
					sql.append("AND ");
				}
				else {
					sql.append(" where ");
				}

				// Compound layer search
				if (layerEnum == Layer.LAYER1_LAYER2) {
					sql.append(" ( T1." + DbKeys.NetworkElementFacilityCols.LAYER
					    + " = '" + Layer.LAYER1.toString() + "'");
					sql.append(" OR T1." + DbKeys.NetworkElementFacilityCols.LAYER
					    + " = '" + Layer.LAYER2.toString() + "' ) ");
				}

				else {
					sql.append("T1." + DbKeys.NetworkElementFacilityCols.LAYER + " = ? ");
					attrList.add(layerString);
				}

				flag = true;
			}
		}

		// Attr: SHELF ... exact match only
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.SHELF)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}
			sql.append("T1." + DbKeys.NetworkElementFacilityCols.SHELF + " = ? ");
			attrList.add(filter.get(DbKeys.NetworkElementFacilityCols.SHELF));
			flag = true;
		}

		// Attr: SLOT ... exact match only
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.SLOT)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}
			sql.append("T1." + DbKeys.NetworkElementFacilityCols.SLOT + " = ? ");
			attrList.add(filter.get(DbKeys.NetworkElementFacilityCols.SLOT));
			flag = true;
		}

		// Attr: PORT ... exact match only
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.PORT)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}
			sql.append("T1." + DbKeys.NetworkElementFacilityCols.PORT + " = ? ");
			attrList.add(filter.get(DbKeys.NetworkElementFacilityCols.PORT));
			flag = true;
		}

		// Attr: PRIMARYSTATE ... allow for substring matching
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}
			sql.append("T1." + DbKeys.NetworkElementFacilityCols.PRIMARYSTATE
			    + " LIKE ? ");
			attrList.add(filter.get(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE));
			flag = true;
		}

		// Attr: signalingType ... exact match only
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.SIGTYPE)) {
			String sigTypeString = filter
			    .get(DbKeys.NetworkElementFacilityCols.SIGTYPE);

			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}

			// Compound search
			if (FacilityConstants.SIGTYPE_DRACUSERENDPOINT.equals(sigTypeString)) {
				sql.append(" ( T1." + DbKeys.NetworkElementFacilityCols.SIGTYPE
				    + " = '" + FacilityConstants.SIGNAL_TYPE.UNI + "'");
				sql.append(" OR T1." + DbKeys.NetworkElementFacilityCols.SIGTYPE
				    + " = '" + FacilityConstants.SIGNAL_TYPE.ENNI + "' ) ");
			}
			else {
				sql.append("T1." + DbKeys.NetworkElementFacilityCols.SIGTYPE + " = ? ");
				attrList.add(filter.get(DbKeys.NetworkElementFacilityCols.SIGTYPE));
			}

			flag = true;
		}

		// Attr: TNA ... exact match only
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.TNA)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}
			sql.append("T1." + DbKeys.NetworkElementFacilityCols.TNA + " = ? ");
			attrList.add(filter.get(DbKeys.NetworkElementFacilityCols.TNA));
			flag = true;
		}

		// Attr: TNA_SET
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.TNA_SET)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}

			boolean b = Boolean.valueOf(
			    filter.get(DbKeys.NetworkElementFacilityCols.TNA_SET)).booleanValue();
			if (b) {
				sql.append("T1." + DbKeys.NetworkElementFacilityCols.TNA
				    + " NOT LIKE ? ");
			}
			else {
				sql.append("T1." + DbKeys.NetworkElementFacilityCols.TNA + " LIKE ? ");
			}

			attrList.add(FacilityConstants.DEFAULT_TNA);
			flag = true;
		}

		// Attr: SITE ... exact match only
		if (filter != null
		    && filter.containsKey(DbKeys.NetworkElementFacilityCols.SITE)) {
			if (flag) {
				sql.append("AND ");
			}
			else {
				sql.append(" where ");
			}
			sql.append("T1." + DbKeys.NetworkElementFacilityCols.SITE + " = ? ");
			attrList.add(filter.get(DbKeys.NetworkElementFacilityCols.SITE));
			flag = true;
		}

		if (filter != null && !flag) {
			throw new Exception(
			    "NetworkElementFacility - Unsupported search criteria");
		}

		Reader dbOp = new Reader(attrList);
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return dbOp.getResult();
	}

	private String encodePK(String neid, String aid) {
		if (neid != null && aid != null) {
			return EndPointType.encodeFacilityEndpointResourceId(neid, aid);
		}

		return null;
	}

	/**
	 * @deprecated use retrieveFacilities and get a List of Facility objects
	 */
	@Deprecated
	private List<Map<String, String>> retrieve(List<String> resourceEndpointIds,
	    final Map<String, String> filter) throws Exception {
		final List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		for (final Facility facility : retrieveFacilities(resourceEndpointIds,
		    filter)) {
			result.add(facility.asUnmodifiableMap());
		}
		return result;
	}

	public void updateAddressAndPort(final String oldAddress, final int oldPort,
	    final String newAddress, final int newPort) throws Exception {
		final String oldPk = DbNetworkElement.INSTANCE.encodePK(oldAddress,
		    Integer.toString(oldPort));
		final String newPk = DbNetworkElement.INSTANCE.encodePK(newAddress,
		    Integer.toString(newPort));

		final Connection connection = DbOperationsManager.INSTANCE.getDatasource()
		    .getConnection();
		final PreparedStatement preparedStatement = connection
		    .prepareStatement("UPDATE NetworkElement SET pk=? WHERE pk=?");
		preparedStatement.setString(1, newPk);
		preparedStatement.setString(2, oldPk);
		final int updateCount = preparedStatement.executeUpdate();
		log.debug(String.format("Updated %s to %s, update count is %s", oldPk,
		    newPk, updateCount));
		DbOperationsManager.INSTANCE.close(connection, preparedStatement);
	}
}
