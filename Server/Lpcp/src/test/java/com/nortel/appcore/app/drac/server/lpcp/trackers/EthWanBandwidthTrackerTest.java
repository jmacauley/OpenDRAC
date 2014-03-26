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

import static org.junit.Assert.*;

import java.math.BigInteger;
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

public class EthWanBandwidthTrackerTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	// Fixed-WAN EPL style card. CCAT. VLAN is not required (multiple service
	// flows not supported)
	public void test1() throws Exception {
		Map<String, String> fm = new HashMap<String, String>();
		fm.put(FacilityConstants.SRLG_ATTR, "");
		fm.put(FacilityConstants.COST_ATTR, "1");
		fm.put(FacilityConstants.METRIC_ATTR, "1");

		NetworkElementHolder h = new NetworkElementHolder("127.0.0.1", "10000",
		    "ADMIN", NeType.OME6, "tid", NeStatus.NE_ALIGNED, null, CryptoWrapper
		        .INSTANCE.encrypt("ADMIN"), 1, NETWORK_ELEMENT_MODE.SONET, "",
		    "00-21-E1-D6-D5-DC", PROTOCOL_TYPE.NETL1_PROTOCOL, true, "rel6", null,
		    null);

		LpcpFacility ethFac = new LpcpFacility(h, "ETH-1-1-1", "1", "1", "1",
		    "ETH", "is", "bob", "label", "ETH", Layer.LAYER2, "1000", null, null,
		    fm, new TrackerConstraints());
		EthWanBandwidthTracker eth = new EthWanBandwidthTracker(ethFac);

		LpcpFacility wanFac = new LpcpFacility(h, "WAN-1-1-1", "1", "1", "1",
		    "WAN", "is", "bob", "label", "WAN", Layer.LAYER2, "1000", null, null,
		    fm, new TrackerConstraints());
		EthWanBandwidthTracker wan = new EthWanBandwidthTracker(wanFac);

		eth.getNeid();
		wan.getNeid();
		eth.getAid();
		wan.getAid();
		eth.getConstraints();
		wan.getConstraints();
		Assert.assertEquals("getUtilisation should return zero", 0,
		    eth.getUtilisation(), .001);
		Assert.assertEquals("getUtilisation should return zero", 0,
		    wan.getUtilisation(), .001);

		Map<SPF_KEYS, Object> parms = new HashMap<SPF_KEYS, Object>();
		parms.put(SPF_KEYS.SPF_SRCTNA, "bob");
		// parms.put(SPF_KEYS.SPF_SRCVLAN, "666");
		parms.put(SPF_KEYS.SPF_RATE, "100");

		Assert.assertEquals(1, eth.getNextChannel(1, parms));
		Assert.assertEquals(1, wan.getNextChannel(1, parms));
		/*
		 * This version of getNextChanel asks for the next free channel starting at
		 * the given start channel, as is it does not work!
		 */
		Assert.assertEquals(1, eth.getNextChannel("5", 1, parms));
		Assert.assertEquals(1, wan.getNextChannel("5", 1, parms));

		try {
			eth.setConstraints(new TrackerConstraints(BigInteger.valueOf(4095)));
			fail("Expected exception!");
		}
		catch (Exception e) {
			// Sucess
		}
		try {
			wan.setConstraints(new TrackerConstraints(BigInteger.valueOf(4095)));
			fail("Expected exception!");
		}
		catch (Exception e) {
			// Sucess
		}

		Map<String, String> m = new HashMap<String, String>();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-1-1");
		m.put(CrossConnection.TARGET_PORT_AID, "ETH-1-1-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.RATE, "STS1");
		// m.put(CrossConnection.VLANID, "666");

		CrossConnection c = new CrossConnection(m);
		Assert.assertTrue("take bandwidth failed " + c, eth.takeBandwidth(c));
		Assert.assertTrue("give bandwidth failed " + c, eth.giveBandwidth(c));
		Assert.assertTrue("take bandwidth failed " + c, eth.takeBandwidth(c));

		m.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-1-1");
		m.put(CrossConnection.TARGET_PORT_AID, "WAN-1-1-1");
		c = new CrossConnection(m);
		Assert.assertTrue("take bandwidth failed " + c, wan.takeBandwidth(c));
		Assert.assertTrue("give bandwidth failed " + c, wan.giveBandwidth(c));
		Assert.assertTrue("take bandwidth failed " + c, wan.takeBandwidth(c));

		m.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-1-1");
		m.put(CrossConnection.TARGET_PORT_AID, "ETH-1-1-1");
		// m.put(CrossConnection.VLANID, "667");
		m.put(CrossConnection.SOURCE_CHANNEL, "3");
		m.put(CrossConnection.TARGET_CHANNEL, "3");
		c = new CrossConnection(m);
		Assert.assertTrue("take bandwidth failed " + c, eth.takeBandwidth(c));
		Assert.assertTrue("give bandwidth failed " + c, eth.giveBandwidth(c));
		Assert.assertTrue("take bandwidth failed " + c, eth.takeBandwidth(c));

		m.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-1-1");
		m.put(CrossConnection.TARGET_PORT_AID, "WAN-1-1-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "3");
		m.put(CrossConnection.TARGET_CHANNEL, "3");
		c = new CrossConnection(m);
		Assert.assertTrue("take bandwidth failed " + c, wan.takeBandwidth(c));
		Assert.assertTrue("give bandwidth failed " + c, wan.giveBandwidth(c));
		Assert.assertTrue("take bandwidth failed " + c, wan.takeBandwidth(c));

		
		

		// Only single service flow from the ETH; next channel should return -1 on
		// the eth side
		Assert.assertEquals(-1, eth.getNextChannel(1, parms));
		// Next free channel should be 3
		Assert.assertEquals(3, wan.getNextChannel(1, parms));

		// The single service flow tracker does not require a vlanid
		Assert.assertEquals(true, eth.isVlanAvailable(null));

	}
}
