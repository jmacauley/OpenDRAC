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

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.SPF_KEYS;

public class OpticalFacilityTrackerTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * -------------------------------------------------------------
	 * <p>
	 * Defect case #1:
	 * <p>
	 * Low-order connections cause valid CCAT request to fail
	 * <p>
	 * -------------- Scenario: Three node OME ring 1-2-3. OC12 links. OC48 UNI
	 * TNAs (tribs) Short hop 1-2 full filled by STS12C VCAT. Request a second
	 * STS12 service between 1-2. Should route via long route 1-3-2 and autoselect
	 * channel 13 on the OC48 TNAs. Routing request fails because the OC48 TNA
	 * trib is determined to have no available b/w. Defect: The problem resides in
	 * OpticalFacilityTracker::getNextFreeBoundaryChannel for the OC48 TNA tribs.
	 * When the second request is made, the first service is loaded as routing
	 * exclusions and loads the tracker. This loading of the tracker incorrectly
	 * incremented the nextAvailable 12C timeslot for each exclusion loaded. i.e.
	 * as channels 1,4,6,10 are loaded, the 12C incremented 13,25,37,-1. The cause
	 * for this was that the loop in getNextFreeBoundaryChannel was incorrectly
	 * starting on nextBoundaryChannel on each invocation. => for (i =
	 * nextBoundaryChannel; i < maxChannels; i += channelSizes[sizeIdx]) The
	 * tracker should not increment if the 'current' b/w block remains free. The
	 * initial fix, therefore, is to begin on the requested startChannel. => for
	 * (int i = (startChannel != -1 ? startChannel : 1); i < maxChannels; i +=
	 * channelSizes[sizeIdx])
	 * <p>
	 * Defect case #2: Unassembled vcat member handoff should provision to
	 * contiguous 3C blocks
	 * <p>
	 * -------------- In the same method as discussed in case #1... Why start the
	 * search for an open b/w block at anything other than 1? By starting on
	 * startChannel, the case #1 defect is corrected, BUT, A request could still
	 * be rejected if contiguous times-lots are not available in the forward-fill
	 * direction (from the specified start channel) ... while in fact lower order
	 * time-slots are available for back-fill. Technically, it is valid to do
	 * this...why not backfill? In discussion with Adrian: This was a request by
	 * Surfnet. In order to manage inter-domain hand-off, you need the VCAT
	 * members to start on the user-requested start channel, and to
	 * deterministically pack (contiguously) on the inter-domain ENNI/UNI
	 * hand-off. The current implementation was correctly forward-filling only,
	 * but it was not enforcing contiguous blocks.
	 * -------------------------------------------------------------
	 */

	@Before
	public void setUp() {
	}

	@Test
	public void test1() throws Exception {
		int result = -1;

		OpticalFacilityTracker oc48OpticalTracker = new OpticalFacilityTracker(
		    LpcpFacility.MODE.SONET, LpcpFacility.MODE.SONET, LpcpFacility.STS48C,
		    "00-21-E1-D6-D5-DC", "OC48-1-5-1", new TrackerConstraints());

		// Existing service present: four vcat members take low-order channels
		oc48OpticalTracker.takeBandwidth("id", 1, LpcpFacility.STS3C);
		oc48OpticalTracker.takeBandwidth("id", 4, LpcpFacility.STS3C);
		oc48OpticalTracker.takeBandwidth("id", 7, LpcpFacility.STS3C);
		oc48OpticalTracker.takeBandwidth("id", 10, LpcpFacility.STS3C);

		// New service request for an OC12 CCAT. Should correctly take timeslot 13
		// on the OC48
		/*
		 * For auto-channel selection, dstChannel = -1 ... This was the call
		 * sequence: => validateTNAs(rSrcNode, rDstNode, srcTNA, dstTNA, rate,
		 * srcChannel, dstChannel, model, precheckedData, parameters
		 * .get(LpcpConstants.firstVCATMemberDone) != null, parameters); =>
		 * dstTNAchannel = getChannel(dstChannel, dstTNAtracker, trackerRate,
		 * firstVCATMemberDone, parameters); => tracker.getNextChannel(trackerRate,
		 * parameters);
		 */

		Map<SPF_KEYS, Object> parameters = new HashMap<SPF_KEYS, Object>();
		// getNextChannel trackerRate of 3 is for STS12C
		result = oc48OpticalTracker.getNextChannel(3, parameters);
		log.debug("testOpticalFacilityTracker_case1: " + result);

		assertTrue(result == 13);
	}

	@Test
	public void test2() throws Exception {
		OpticalFacilityTracker tracker = new OpticalFacilityTracker(
		    LpcpFacility.MODE.SONET, LpcpFacility.MODE.SONET, LpcpFacility.STS12C,
		    "00-21-E1-D6-D5-DC", "OC48-1-5-1", new TrackerConstraints());

		tracker.takeBandwidth("id", 4, LpcpFacility.STS3C);
		tracker.takeBandwidth("id", 10, LpcpFacility.STS3C);

		assertTrue(!tracker.isContiguousUsage());
	}

	@Test
	// valid channel boundaries
	public void test3() throws Exception {
		OpticalFacilityTracker tracker = new OpticalFacilityTracker(
		    LpcpFacility.MODE.SONET, LpcpFacility.MODE.SONET, LpcpFacility.STS48C,
		    "00-21-E1-D6-D5-DC", "OC48-1-5-1", new TrackerConstraints());

		assertTrue(tracker.isBoundaryChannel(1, 0));
		assertTrue(tracker.isBoundaryChannel(3, 0));
		assertTrue(tracker.isBoundaryChannel(4, 0));

		assertTrue(tracker.isBoundaryChannel(1, 1));
		assertFalse(tracker.isBoundaryChannel(2, 1));
		assertFalse(tracker.isBoundaryChannel(3, 1));
		assertTrue(tracker.isBoundaryChannel(4, 1));

		assertTrue(tracker.isBoundaryChannel(2, 0));
	}
}
