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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;

public class DbNetworkElementFacilityTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

	private static class Ab extends AbstractNetworkElement {
		@Override
		public void changeNePassword(String userId, CryptedString newPassword) {
			return;
		}

		/*
		 * Here we make a subclass of AbstractnetworkElement that has just enough
		 * knowledge to generate its key
		 */

		@Override
		public String getIpAddress() {
			return "127.0.0.1";
		}

		@Override
		public String getNeId() {
			return "00-21-E1-D6-D6-70";
		}

		@Override
		public int getPortNumber() {
			return 10001;
		}

		@Override
		public void nextState() {
			// ignore

		}

		@Override
		public void terminate() {
			// ignore

		}
	}

	private static class Ab2 extends AbstractNetworkElement {
		@Override
		public void changeNePassword(String userId, CryptedString newPassword) {
			// ignore

		}

		/*
		 * Here we make a subclass of AbstractnetworkElement that has just enough
		 * knowledge to generate its key
		 */
		@Override
		public String getIpAddress() {
			return "127.0.0.1";
		}

		@Override
		public String getNeId() {
			return "00-21-E1-D6-D5-DC";
		}

		@Override
		public int getPortNumber() {
			return 10001;
		}

		@Override
		public void nextState() {
			return;
		}

		@Override
		public void terminate() {
			return;
		}
	}

	@Test
	public void testExercise() throws Exception {
		TestHelper.INSTANCE.initialize();

		try {
			DbNetworkElementFacility.INSTANCE.deleteAll();

			Ab ab = new Ab();
			Ab2 ab2 = new Ab2();

			List<Facility> data = new ArrayList<Facility>();
			Map<String, String> row = new HashMap<String, String>();
			row.put(DbKeys.NetworkElementFacilityCols.AID, "aid");
			row.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER1.toString());
			row.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			row.put(DbKeys.NetworkElementFacilityCols.SLOT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PORT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "is");
			row.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "unassigned");
			row.put(DbKeys.NetworkElementFacilityCols.TNA, "bigbobsTNA");
			row.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			row.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC,
			    "00-21-E1-D6-D6-70");
			// row.put("Other_xml_random_stuff", "1");

			data.add(new Facility(row));
			DbNetworkElementFacility.INSTANCE.addFacility(ab.getNeId(),
			    ab.getIpAddress(), ab.getPortNumber(), data);

			List<Facility> ret = DbNetworkElementFacility.INSTANCE
			    .retrieveFacilities(null);
			assertEquals("Not expected size", 1, ret.size());

			DbNetworkElementFacility.INSTANCE.retrieveFacilities(null);
			Map<String, String> filter = new HashMap<String, String>();
			DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
			filter.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			filter.put(DbKeys.NetworkElementFacilityCols.SLOT, "1");
			filter.put(DbKeys.NetworkElementFacilityCols.PORT, "1");
			filter.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "1");
			filter.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "UNI");
			filter.put(DbKeys.NetworkElementFacilityCols.TNA, "1");
			filter.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);

			DbNetworkElementFacility.INSTANCE.update(ab, row, true);
			DbNetworkElementFacility.INSTANCE.update(ab, row, false);
			DbNetworkElementFacility.INSTANCE.update(ab, "2", row, true);
			DbNetworkElementFacility.INSTANCE.update(ab, "2", row, false);
			DbNetworkElementFacility.INSTANCE.update(ab, "192", row, true);
			DbNetworkElementFacility.INSTANCE.update(ab, "192", row, false);

			DbNetworkElementFacility.INSTANCE.delete(ab, "192");
			DbNetworkElementFacility.INSTANCE.delete(ab.getNeId(), "192");
			DbNetworkElementFacility.INSTANCE.deleteAll(ab.getNeId());

			// Add a 'user' endpoint and retrieve it back:
			DbNetworkElementFacility.INSTANCE.deleteAll();
			data = new ArrayList<Facility>();

			// this is a valid user ep
			row = new HashMap<String, String>();
			row.put(DbKeys.NetworkElementFacilityCols.AID, "OC48-1-1-1");
			row.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER1.toString());
			row.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			row.put(DbKeys.NetworkElementFacilityCols.SLOT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PORT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "IS");
			row.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "UNI");
			row.put(DbKeys.NetworkElementFacilityCols.TNA, "bigbobsTNA");
			row.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			row.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC,
			    "00-21-E1-D6-D6-70");
			row.put("group", "none");
			row.put("constrain", "0");
			// row.put("Other_xml_random_stuff", "1");
			data.add(new Facility(row));

			// this is NOT a user ep ... it's INNI
			row = new HashMap<String, String>();
			row.put(DbKeys.NetworkElementFacilityCols.AID, "OC48-1-1-2");
			row.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER1.toString());
			row.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			row.put(DbKeys.NetworkElementFacilityCols.SLOT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PORT, "2");
			row.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "IS");
			row.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "INNI");
			row.put(DbKeys.NetworkElementFacilityCols.TNA, "blablabla");
			row.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			row.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC,
			    "00-21-E1-D6-D6-70");
			row.put("group", "none");
			row.put("constrain", "0");
			// row.put("Other_xml_random_stuff", "1");
			data.add(new Facility(row));

			// this is NOT a user ep ... it's OSS
			row = new HashMap<String, String>();
			row.put(DbKeys.NetworkElementFacilityCols.AID, "OC48-1-1-3");
			row.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER1.toString());
			row.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			row.put(DbKeys.NetworkElementFacilityCols.SLOT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PORT, "2");
			row.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "IS");
			row.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "OSS");
			row.put(DbKeys.NetworkElementFacilityCols.TNA, "blablabla");
			row.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			row.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC,
			    "00-21-E1-D6-D6-70");
			row.put("group", "none");
			row.put("constrain", "0");
			// row.put("Other_xml_random_stuff", "1");
			data.add(new Facility(row));

			// this is NOT a user ep ... no tna assigned
			row = new HashMap<String, String>();
			row.put(DbKeys.NetworkElementFacilityCols.AID, "OC48-1-1-4");
			row.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER1.toString());
			row.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			row.put(DbKeys.NetworkElementFacilityCols.SLOT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PORT, "3");
			row.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "IS");
			row.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "ENNI");
			row.put(DbKeys.NetworkElementFacilityCols.TNA,
			    FacilityConstants.DEFAULT_TNA);
			row.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			row.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC,
			    "00-21-E1-D6-D6-70");

			row.put("group", "none");
			row.put("constrain", "0");
			// row.put("Other_xml_random_stuff", "1");
			data.add(new Facility(row));

			DbNetworkElementFacility.INSTANCE.addFacility(ab.getNeId(),
			    ab.getIpAddress(), ab.getPortNumber(), data);

			filter = new HashMap<String, String>();

			// all user endpoints
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER_ALL.toString());

			// this should be equivalent result:
			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER_ALL.toString());
			filter.put(DbKeys.NetworkElementFacilityCols.TNA_SET, "true");
			filter.put(DbKeys.NetworkElementFacilityCols.SIGTYPE,
			    FacilityConstants.SIGTYPE_DRACUSERENDPOINT);
			List<Facility> facList = DbNetworkElementFacility.INSTANCE
			    .retrieveFacilities(filter);
			assertEquals("Not expected size", 1, facList.size());

			// user endpoints at layer 1
			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER1.toString());

			DbNetworkElementFacility.INSTANCE.deleteAll();

			// layer retrievals
			data = new ArrayList<Facility>();
			row = new HashMap<String, String>();
			row.put(DbKeys.NetworkElementFacilityCols.AID, "OC48-1-5-1");
			row.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER1.toString());
			row.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			row.put(DbKeys.NetworkElementFacilityCols.SLOT, "5");
			row.put(DbKeys.NetworkElementFacilityCols.PORT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "IS");
			row.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "UNI");
			row.put(DbKeys.NetworkElementFacilityCols.TNA, "xxx1");
			row.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			row.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC,
			    "00-21-E1-D6-D6-70");
			row.put("group", "none");
			row.put("constrain", "0");
			data.add(new Facility(row));
			row = new HashMap<String, String>();
			row.put(DbKeys.NetworkElementFacilityCols.AID, "ETH-1-1-1");
			row.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER2.toString());
			row.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			row.put(DbKeys.NetworkElementFacilityCols.SLOT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PORT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "IS");
			row.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "UNI");
			row.put(DbKeys.NetworkElementFacilityCols.TNA, "xxx2");
			row.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			row.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC,
			    "00-21-E1-D6-D6-70");
			row.put("group", "none");
			row.put("constrain", "0");
			data.add(new Facility(row));
			row = new HashMap<String, String>();
			row.put(DbKeys.NetworkElementFacilityCols.AID, "CMD-1-99-1");
			row.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER0.toString());
			row.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			row.put(DbKeys.NetworkElementFacilityCols.SLOT, "99");
			row.put(DbKeys.NetworkElementFacilityCols.PORT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "IS");
			row.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "UNI");
			row.put(DbKeys.NetworkElementFacilityCols.TNA, "xxx3");
			row.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			row.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC,
			    "00-21-E1-D6-D6-70");
			row.put("group", "none");
			row.put("constrain", "0");
			data.add(new Facility(row));

			DbNetworkElementFacility.INSTANCE.addFacility(ab.getNeId(),
			    ab.getIpAddress(), ab.getPortNumber(), data);
			DbNetworkElementFacility.INSTANCE.addFacility(ab2.getNeId(),
			    ab2.getIpAddress(), ab2.getPortNumber(), data);

			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER0.toString());
			ret = DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
			assertEquals("Not expected size", 2, ret.size());

			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER1.toString());
			ret = DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
			assertEquals("Not expected size", 2, ret.size());

			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER2.toString());
			ret = DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
			assertEquals("Not expected size", 2, ret.size());

			// Compounds retrievals
			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER1_LAYER2.toString());
			ret = DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
			assertEquals("Not expected size", 4, ret.size());

			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER_ALL.toString());
			ret = DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
			assertEquals("Not expected size", 6, ret.size());

			// Test the new primary key retrieval, mixed with filter
			List<String> resourceEndpointIds = new ArrayList<String>();
			resourceEndpointIds.add(EndPointType.encodeFacilityEndpointResourceId(
			    ab.getNeId(), "OC48-1-5-1"));
			resourceEndpointIds.add(EndPointType.encodeFacilityEndpointResourceId(
			    ab2.getNeId(), "OC48-1-5-1"));
			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
			    Layer.LAYER_ALL.toString());
			ret = DbNetworkElementFacility.INSTANCE.retrieveFacilities(
			    resourceEndpointIds, filter);
			assertEquals("Not expected size", 2, ret.size());
			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, ab.getNeId());
			ret = DbNetworkElementFacility.INSTANCE.retrieveFacilities(
			    resourceEndpointIds, filter);
			assertEquals("Not expected size", 1, ret.size());

			// Test new indexed column 'Site'
			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			ret = DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
			assertEquals("Not expected size", 6, ret.size());

			// Test retrieval by id via facility filter
			// Test new indexed column 'Site'
			filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.PK, ab.getNeId()
			    + "_ETH-1-1-1");
			ret = DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
			assertEquals("Not expected size", 1, ret.size());

			log.debug("Fac dump: "
			    + DbNetworkElementFacility.INSTANCE.retrieveFacilities(null));
			DbNetworkElementFacility.INSTANCE.deleteAll();
		}
		finally {
			DbNetworkElementFacility.INSTANCE.deleteAll();
		}
	}

	@Test
	public void testUpdate() throws Exception {
		TestHelper.INSTANCE.initialize();

		try {
			DbNetworkElementFacility.INSTANCE.deleteAll();

			Ab ab = new Ab();
			List<Facility> data = new ArrayList<Facility>();
			Map<String, String> row = new HashMap<String, String>();
			row.put(DbKeys.NetworkElementFacilityCols.AID, "aid");
			row.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER1.toString());
			row.put(DbKeys.NetworkElementFacilityCols.SHELF, "1");
			row.put(DbKeys.NetworkElementFacilityCols.SLOT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PORT, "1");
			row.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, "is");
			row.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "UNI");
			row.put(DbKeys.NetworkElementFacilityCols.TNA, "bigbobsTNA");
			row.put(DbKeys.NetworkElementFacilityCols.SITE, "Ottawa");
			row.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC,
			    "00-21-E1-D6-D6-70");
			// row.put("Other_xml_random_stuff", "1");
			data.add(new Facility(ab, row));
			DbNetworkElementFacility.INSTANCE.addFacility(ab.getNeId(),
			    ab.getIpAddress(), ab.getPortNumber(), data);

			log.debug("Update :Prior="
			    + DbNetworkElementFacility.INSTANCE.retrieveFacilities(null));

			Map<String, String> updateData = new HashMap<String, String>();
			updateData.put(DbKeys.NetworkElementFacilityCols.SITE, "Not Ottawa");
			updateData.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, "ENNI");
			updateData.put("SomethingElse", "randome");
			DbNetworkElementFacility.INSTANCE.update(ab, "aid", updateData,
			    false);
			log.debug("Update :after 1="
			    + DbNetworkElementFacility.INSTANCE.retrieveFacilities(null));
			DbNetworkElementFacility.INSTANCE
			    .update(ab, "aid", updateData, true);
			log.debug("Update :after 2="
			    + DbNetworkElementFacility.INSTANCE.retrieveFacilities(null));

			// Try it again, nothing changed this time.
			DbNetworkElementFacility.INSTANCE
			    .update(ab, "aid", updateData, true);
		}
		finally {
			DbNetworkElementFacility.INSTANCE.deleteAll();
		}
	}
}
