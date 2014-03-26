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
import org.junit.BeforeClass;
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

public final class TrackerTester {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static EthWanBandwidthTracker eth;
	private static EthWanBandwidthTracker wan;
	private static Force10EthernetTracker f10;
	private static OpticalFacilityTracker opt;
	private static WavelengthTracker w;

	@BeforeClass
	public static void setUp() throws Exception {
		/**
		 * Create empty trackers, these trackers have no bandwidth in use, and will
		 * function as basic test.
		 */
		Map<String, String> m = new HashMap<String, String>();

		m.put(FacilityConstants.SRLG_ATTR, "");
		m.put(FacilityConstants.COST_ATTR, "1");
		m.put(FacilityConstants.METRIC_ATTR, "1");

		NetworkElementHolder h = new NetworkElementHolder("127.0.0.1", "10000",
		    "ADMIN", NeType.OME6, "tid", NeStatus.NE_ALIGNED, null, CryptoWrapper
		        .INSTANCE.encrypt("ADMIN"), 1, NETWORK_ELEMENT_MODE.SONET, "",
		    "00-21-E1-D6-D5-DC", PROTOCOL_TYPE.NETL1_PROTOCOL, true, "rel6", null,
		    null);

		LpcpFacility ethFac = new LpcpFacility(h, "ETH-1-1-1", "1", "1", "1",
		    "ETH", "is", "bob", "label", "ETH", Layer.LAYER2, "1000", null, null,
		    m, new TrackerConstraints());
		eth = new EthWanBandwidthTracker(ethFac);
		LpcpFacility wanFac = new LpcpFacility(h, "WAN-1-1-1", "1", "1", "1",
		    "WAN", "is", "bob", "label", "WAN", Layer.LAYER2, "1000", null, null,
		    m, new TrackerConstraints());
		wan = new EthWanBandwidthTracker(wanFac);

		NetworkElementHolder f10Holder = new NetworkElementHolder("127.0.0.1",
		    "10000", "ADMIN", NeType.FORCE10, "tid", NeStatus.NE_ALIGNED, null,
		    CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
		    NETWORK_ELEMENT_MODE.SONET, "", "00-21-E1-D6-D5-DC",
		    PROTOCOL_TYPE.FORCE10_PROTOCOL, true, "some release", null, null);

		LpcpFacility f10Fac = new LpcpFacility(f10Holder,
		    "TenGigabitEthernet-1-0-27", "1", "1", "1", "TenGigabitEthernet", "is",
		    "bob", "label", "TenGigabitEthernet", Layer.LAYER2, "1000", null, null,
		    m, new TrackerConstraints());
		f10 = new Force10EthernetTracker(f10Fac);

		opt = new OpticalFacilityTracker(LpcpFacility.MODE.SONET,
		    LpcpFacility.MODE.SONET, LpcpFacility.STS48C, "00-21-E1-D6-D5-DC",
		    "OC48-1-1-1", new TrackerConstraints());
		w = new WavelengthTracker("00-21-E1-D6-D5-DC", "1000-1-1-1", "1000",
		    new TrackerConstraints());
	}

	@Test
	public void testBasic() throws Exception {
		eth.getNeid();
		wan.getNeid();
		f10.getNeid();
		opt.getNeid();
		w.getNeid();

		eth.getAid();
		wan.getAid();
		f10.getAid();
		opt.getNeid();
		w.getNeid();

		eth.getConstraints();
		wan.getConstraints();
		f10.getConstraints();
		opt.getConstraints();
		w.getConstraints();

		Assert.assertEquals("getUtilisation should return zero", 0,
		    eth.getUtilisation(), .001);
		Assert.assertEquals("getUtilisation should return zero", 0,
		    wan.getUtilisation(), .001);
		Assert.assertEquals("getUtilisation should return zero", 0,
		    f10.getUtilisation(), .001);
		Assert.assertEquals("getUtilisation should return zero", 0,
		    opt.getUtilisation(), .001);
		Assert.assertEquals("getUtilisation should return zero", 0,
		    w.getUtilisation(), .001);

		Map<SPF_KEYS, Object> parms = new HashMap<SPF_KEYS, Object>();
		parms.put(SPF_KEYS.SPF_RATE, "100");
		Assert.assertEquals(1, eth.getNextChannel(1, parms));
		Assert.assertEquals(1, wan.getNextChannel(1, parms));
		Assert.assertEquals(1, f10.getNextChannel(1, parms));
		Assert.assertEquals(1, opt.getNextChannel(1, parms));
		Assert.assertEquals(1000, w.getNextChannel(1, parms));

		/*
		 * This version of getNextChanel asks for the next free channel starting at
		 * the given start channel, as is it does not work!
		 */
		Assert.assertEquals(1, eth.getNextChannel("5", 1, parms));
		Assert.assertEquals(1, wan.getNextChannel("5", 1, parms));
		Assert.assertEquals(1, f10.getNextChannel("5", 1, parms)); // will always
		                                                           // return 1
		Assert.assertEquals(5, opt.getNextChannel("5", 1, parms));
		Assert.assertEquals(1000, w.getNextChannel("5", 1, parms));

		try {
			eth.setConstraints(new TrackerConstraints(BigInteger.valueOf(4095)));
			fail("expected an exception!");
		}
		catch (Exception e) {
			// Success;
		}
		try {
			wan.setConstraints(new TrackerConstraints(BigInteger.valueOf(4095)));
			fail("expected an exception!");
		}
		catch (Exception e) {
			// Success;
		}

		try {
			f10.setConstraints(new TrackerConstraints(BigInteger.valueOf(4095)));
			fail("expected an exception!");
		}
		catch (Exception e) {
			// Success;
		}

		opt.setConstraints(new TrackerConstraints(BigInteger.valueOf(4095)));

		try {
			w.setConstraints(new TrackerConstraints(BigInteger.valueOf(4095)));
			fail("expected an exception!");
		}
		catch (Exception e) {
			// Success;
		}

		eth.getUtilisation();
		wan.getUtilisation();
		f10.getUtilisation();
		opt.getUtilisation();
		w.getUtilisation();

		eth.setConstraints(new TrackerConstraints());
		wan.setConstraints(new TrackerConstraints());
		f10.setConstraints(new TrackerConstraints());
		opt.setConstraints(new TrackerConstraints());
		w.setConstraints(new TrackerConstraints());

		Map<String, String> m = new HashMap<String, String>();
		m.put(CrossConnection.SOURCE_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.TARGET_NEID, "00-21-E1-D6-D5-DC");
		m.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-1-1");
		m.put(CrossConnection.TARGET_PORT_AID, "ETH-1-1-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1");
		m.put(CrossConnection.TARGET_CHANNEL, "1");
		m.put(CrossConnection.RATE, "STS1");
		m.put(CrossConnection.RATE_IN_MBS, "50");
		m.put(CrossConnection.VLANID, "666");

		eth.takeBandwidth(new CrossConnection(m));

		m.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-1-1");
		m.put(CrossConnection.TARGET_PORT_AID, "WAN-1-1-1");
		wan.takeBandwidth(new CrossConnection(m));

		m.put(CrossConnection.SOURCE_PORT_AID, "GigabitEthernet-1-0-10");
		m.put(CrossConnection.TARGET_PORT_AID, "TenGigabitEthernet-1-0-27");
		f10.takeBandwidth(new CrossConnection(m));

		m.put(CrossConnection.SOURCE_PORT_AID, "OC48-1-1-1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC12-1-1-1");
		opt.takeBandwidth(new CrossConnection(m));

		m.put(CrossConnection.SOURCE_PORT_AID, "1000-1-1-1");
		m.put(CrossConnection.TARGET_PORT_AID, "OC12-1-1-1");
		m.put(CrossConnection.SOURCE_CHANNEL, "1000");
		w.takeBandwidth(new CrossConnection(m));

		
		
		
		
		

		/*
		 * int getNextChannel(String startingChannel, int rate, WMap<SPF_KEYS,
		 * Object> parms) throws Exception; boolean giveBandwidth(CrossConnection c)
		 * throws Exception; boolean takeBandwidth(CrossConnection c) throws
		 * Exception; void setConstraints(TrackerConstraints constraints) throws
		 * Exception; String toString();
		 */

	}
}
