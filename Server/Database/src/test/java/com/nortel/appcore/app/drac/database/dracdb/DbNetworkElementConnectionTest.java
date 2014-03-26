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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opendrac.test.TestHelper;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;

public class DbNetworkElementConnectionTest {

	private static class Ab extends AbstractNetworkElement {
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

	@Test
	public void testGetInstance() throws Exception {
		TestHelper.INSTANCE.initialize();

		try {
			DbNetworkElementConnection.INSTANCE.deleteAll();

			Ab ab = new Ab();
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();

			Map<String, String> one = new HashMap<String, String>();
			one.put(DbKeys.NetworkElementConnectionCols.SOURCEAID, "OC12-1-1");
			one.put(DbKeys.NetworkElementConnectionCols.TARGETAID, "OC12-2-2");
			one.put(DbKeys.NetworkElementConnectionCols.ID, "MyId");
			one.put("junkKey", "junkValue");

			data.add(one);

			DbNetworkElementConnection.INSTANCE.add(ab.getNeId(), data);

			DbNetworkElementConnection.INSTANCE.retrieve(null);

			Map<String, String> filter = new HashMap<String, String>();
			DbNetworkElementConnection.INSTANCE.retrieve(filter);

			filter.put(DbKeys.NetworkElementConnectionCols.NEID_FOR_CONN,
			    ab.getNeId());
			filter.put(DbKeys.NetworkElementConnectionCols.SOURCEAID, "%");
			filter.put(DbKeys.NetworkElementConnectionCols.TARGETAID, "%");
			filter.put(DbKeys.NetworkElementConnectionCols.ID, "99");
			filter.put(DbKeys.NetworkElementConnectionCols.ID_NOT, "45");

			DbNetworkElementConnection.INSTANCE.retrieve(filter);

			DbNetworkElementConnection.INSTANCE.delete(ab, "OC12-1-1",
			    "OC12-2-2");
			DbNetworkElementConnection.INSTANCE.delete(ab.getNeId(), "OC12-1-1",
			    "OC12-2-2");
			DbNetworkElementConnection.INSTANCE.deleteAll(ab);
			DbNetworkElementConnection.INSTANCE.deleteAll(ab.getNeId());
		}
		finally {
			DbNetworkElementConnection.INSTANCE.deleteAll();
		}
	}
}
