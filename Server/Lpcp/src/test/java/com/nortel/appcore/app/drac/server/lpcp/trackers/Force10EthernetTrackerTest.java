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

package com.nortel.appcore.app.drac.server.lpcp.trackers;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;

public class Force10EthernetTrackerTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testIt() throws Exception {
		Map<String, String> m1 = new HashMap<String, String>();

		m1.put(FacilityConstants.SRLG_ATTR, "");
		m1.put(FacilityConstants.COST_ATTR, "1");
		m1.put(FacilityConstants.METRIC_ATTR, "1");

		NetworkElementHolder h = new NetworkElementHolder("127.0.0.1", "10000",
		    "ADMIN", NeType.FORCE10, "tid", NeStatus.NE_ALIGNED, null,
		    CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
		    NETWORK_ELEMENT_MODE.SONET, "", "00-21-E1-D6-D5-DC",
		    PROTOCOL_TYPE.FORCE10_PROTOCOL, true, "some release", null, null);

		LpcpFacility f10Fac = new LpcpFacility(h, "TenGigabitEthernet-1-0-27", "1",
		    "1", "1", "TenGigabitEthernet", "is", "bob", "label",
		    "TenGigabitEthernet", Layer.LAYER2, "1000", null, null, m1,
		    new TrackerConstraints());
		Force10EthernetTracker f10 = new Force10EthernetTracker(f10Fac);

		Map<SPF_KEYS, Object> parms = new HashMap<SPF_KEYS, Object>();
		parms.put(SPF_KEYS.SPF_SRCTNA, "TenGigabitEthernet-1-0-27");
		parms.put(SPF_KEYS.SPF_SRCVLAN, "10");
		parms.put(SPF_KEYS.SPF_RATE, "100");

		Assert.assertEquals(1, f10.getNextChannel(1, parms));
		Assert.assertEquals(1, f10.getNextChannel("1", 1, parms));
		parms.put(SPF_KEYS.SPF_RATE, "1000000");
		Assert.assertEquals(-1, f10.getNextChannel(1, parms));
		parms.put(SPF_KEYS.SPF_RATE, "100");

		Map<String, String> m = new HashMap<String, String>();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.SOURCE_PORT_AID, "TenGigabitEthernet-1-0-27");
		m.put(CrossConnection.TARGET_PORT_AID, "TenGigabitEthernet-1-0-28");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.RATE, "STS1");
		m.put(CrossConnection.RATE_IN_MBS, "50");
		m.put(CrossConnection.VLANID, "666");

		CrossConnection c = new CrossConnection(m);
		Assert.assertTrue("take bandwidth failed " + c, f10.takeBandwidth(c));
		Assert.assertTrue("give bandwidth failed " + c, f10.giveBandwidth(c));
		Assert.assertTrue("take bandwidth failed " + c, f10.takeBandwidth(c));

		m.put(CrossConnection.RATE_IN_MBS, "50000");
		c = new CrossConnection(m);
		Assert.assertFalse("take bandwidth worked! " + c, f10.takeBandwidth(c));
		Assert.assertFalse("give bandwidth worked! " + c, f10.giveBandwidth(c));
		log.debug("tracker " + f10);
	}

}
