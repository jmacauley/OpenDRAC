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

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opendrac.test.TestHelper;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;

public class DbNetworkElementTest {
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
	public void testExercise() throws Exception {
		TestHelper.INSTANCE.initialize();

		try {

			NetworkElementHolder holder = new NetworkElementHolder(
                    "127.0.0.1",
                    "10001",
                    "ADMIN",
                    NeType.UNKNOWN,
                    "TID",
                    NeStatus.NE_UNKNOWN,
                    null,
                    CryptoWrapper.INSTANCE.encrypt("ADMIN"),
                    1,
                    NETWORK_ELEMENT_MODE.Unknown,
                    "",
                    "1",
                    PROTOCOL_TYPE.NETL1_PROTOCOL,
                    true,
                    "Unknown",
                    "",
                    null,
                    null);
			AbstractNetworkElement ab = new Ab();

			DbNetworkElement.INSTANCE.deleteAll();
			DbNetworkElement.INSTANCE.add(holder);
			DbNetworkElement.INSTANCE.add(holder);

			DbNetworkElement.INSTANCE.delete(ab);
			DbNetworkElement.INSTANCE.add(holder);

			{
				DbNetworkElement.INSTANCE.retrieve(null);
				Map<String, String> filter = new HashMap<String, String>();
				DbNetworkElement.INSTANCE.retrieve(filter);
				filter.put(DbKeys.NetworkElementCols.NEPORT, "10001");
				filter.put(DbKeys.NetworkElementCols.NEIP, "127.0.0.1");
				DbNetworkElement.INSTANCE.retrieve(filter);
				filter.put(DbKeys.NetworkElementCols.MANAGEDBY, "");
				DbNetworkElement.INSTANCE.retrieve(filter);
				try {
					filter.put(DbKeys.NetworkElementCols.PASSWORD, "bogus");
					DbNetworkElement.INSTANCE.retrieve(filter);
					fail("bad");
				}
				catch (Exception e) {
				}
			}

			DbNetworkElement.INSTANCE.retrieveAll();
			Map<String, String> data = new HashMap<String, String>();
			DbNetworkElement.INSTANCE.update(ab, data);
			data.put(DbKeys.NetworkElementCols.NE_RELEASE, "release4");
			DbNetworkElement.INSTANCE.update(ab, data);
                        data.put(DbKeys.NetworkElementCols.SUBTYPE, "Unknown");
			DbNetworkElement.INSTANCE.update(ab, data);

			data.put(DbKeys.NetworkElementCols.TYPE, "tl1");
			data.put(DbKeys.NetworkElementCols.NEID, "19");
			data.put(DbKeys.NetworkElementCols.STATUS, "");
			data.put(DbKeys.NetworkElementCols.USERID, "ADMIND");
			data.put(DbKeys.NetworkElementCols.MODE, "Unknown");
			data.put(DbKeys.NetworkElementCols.TID, "ADMIND");
			data.put(DbKeys.NetworkElementCols.PASSWORD, "encryptedPass");

			DbNetworkElement.INSTANCE.update(ab, data);
		}
		finally {
			DbNetworkElement.INSTANCE.deleteAll();
		}
	}
}
