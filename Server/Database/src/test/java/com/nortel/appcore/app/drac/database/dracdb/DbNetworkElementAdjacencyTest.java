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
import java.util.List;

import org.junit.Test;
import org.opendrac.test.TestHelper;

import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;

public class DbNetworkElementAdjacencyTest {

	@Test
	public void testExercise() throws Exception {
		TestHelper.INSTANCE.initialize();

		try {
			DbNetworkElementAdjacency.INSTANCE.deleteAll();

			List<NetworkElementAdjacency> list = new ArrayList<NetworkElementAdjacency>();

			NetworkElementAdjacency adj = new NetworkElementAdjacency(
			    "00-00-00-00-00-00", "OC48-1-11-2", "00-00-00-00-00-00_OC48-1-11-2",
			    "00-00-00-00-00-11_OC48-1-11-2", "SECT", false);
			list.add(adj);

			adj = new NetworkElementAdjacency("00-00-00-00-00-00", "OC192-1-1",
			    "00-00-00-00-00-00_OC192-1-1", "00-00-00-00-00-11_OC192-2-2", "SECT",
			    false);
			list.add(adj);

			DbNetworkElementAdjacency.INSTANCE.add(list);
			DbNetworkElementAdjacency.INSTANCE.retrieve();

//			adj.setRxTag("00-00-00-00-00-11_OC192-2-3");
			DbNetworkElementAdjacency.INSTANCE.update(adj);

			DbNetworkElementAdjacency.INSTANCE.delete("00-00-00-00-00-00",
			    "OC48-1-11-2", "SECT");
			DbNetworkElementAdjacency.INSTANCE.delete("00-00-00-00-00-00");
		}
		finally {
			DbNetworkElementAdjacency.INSTANCE.deleteAllNonManual();
			DbNetworkElementAdjacency.INSTANCE.deleteAll();
		}

	}

}
