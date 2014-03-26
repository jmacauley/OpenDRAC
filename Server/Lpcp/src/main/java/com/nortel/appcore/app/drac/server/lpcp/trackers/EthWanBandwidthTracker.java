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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.server.lpcp.common.Utility;

/**
 * EthWanBandwidthTracker Bandwidth tracker for an ETH/WAN port combination
 * based on the JAVA BitSet class. Note: 1 STS-1 rate is equivalent to 51.84
 * Mb/s 1 STS-3C rate is equivalent of 155.52 Mb/s Cases: (1) EPL card: the WAN
 * facility is implicitly tied to the ETH. (2) L2SS service provisioning from
 * the ETH: the WAN is created on the fly i.e. the WAN doesn't exist at the time
 * of service provisioning (3) L2SS nodal WAN switching: the L2SS WAN may be
 * setup manually ahead of time, and be selected as src/dst of the service
 * request
 */

public final class EthWanBandwidthTracker extends AbstractTracker implements
    EthTrackerI, SonetTrackerI {
  private final Logger log = LoggerFactory.getLogger(getClass());
	// See note in init(): "CONNECTION CONSTRAINTS"
	private static final int MAX_STS_SIZE_CCAT_FE = 3;
	private static final int MAX_STS_SIZE_VCAT_FE = 21;
	private static final int MAX_STS_SIZE_VCAT_1GE = 21;
	private static final int MAX_STS_SIZE_CCAT_1GE = 24;
	private static final int MAX_STS_SIZE_VCAT_10GE = 192;
	private static final int MAX_STS_SIZE_CCAT_10GE = 192;
	private static final int MBS_BASE_RATE = 50; // Mb/s

	private final int channelSizes[] = new int[7];
	private int ethRate = -1;
	private final LpcpFacility facility;
	private int MAX_SIZE_STS = -1;

	private final Set<Integer> vlanIds = new TreeSet<Integer>();
	private BitSet channels;
	private final List<String> ethServiceUsage = new ArrayList<String>();

	public EthWanBandwidthTracker(LpcpFacility lpcpFacility) throws Exception {
		super(lpcpFacility.getNeId(), lpcpFacility.getAid());

		facility = lpcpFacility;
		init();
	}

	@Override
	public SonetTrackerI getBlankCopy() throws Exception {
		/*
		 * The constructor throws an exception, but if instantiated, surely the
		 * request for it to copy itself should not have a problem.
		 */
		return new EthWanBandwidthTracker(facility);
	}

	@Override
	public BitSet getInternalTracker() {
		return channels;
	}

	/*
	 * Given a rate index, determines the next available channel where the
	 * requested BW can be obtained.
	 *
	 * @param rateIdx - rate index for the requested rate for the channelSizes[]
	 * array
	 *
	 * @return int of the next available channel number or -1 is no bandwidth is
	 * available.
	 */
	@Override
	public int getNextChannel(int rateIdx, Map<SPF_KEYS, Object> parameters)
	    throws Exception {
		boolean firstVCATMember = parameters.get(SPF_KEYS.SPF_FIRST_VCAT_DONE) == null;
		int nextChannel = -1;
		BitSet test = channels.get(1, MAX_SIZE_STS + 1); // +1 because get() on a
		                                                 // bitset excludes the last
		                                                 // bit
		boolean vcat_user_request = "true".equalsIgnoreCase((String) parameters
		    .get(SPF_KEYS.SPF_VCATROUTING_OPTION));

		/**
		 * Make sure that the requested vlan is not already in use, in which case
		 * return -1 as the port is already in use... this code only functions if
		 * this port is at the start or end of the path, not in the middle and will
		 * have to be revisited if we support true layer 2 paths where we can route
		 * across multiple layer 2 boxes, in which case we'll need to know what vlan
		 * we are going to use on this port and can make sure its available. In this
		 * limited case we check to see if we are the end or tail end of the path
		 * and use the head or tail end vlan id to check if we are in use or not.
		 */

		if (Facility.isEth(getAid())) {
			int rate = Integer.parseInt((String) parameters.get(SPF_KEYS.SPF_RATE));
			if (!isETHBandwidthAvailable(rate)) {
				nextChannel = -1;
				log.debug("EthWanBandwidthTracker: getNextChannel("
				    + rateIdx
				    + "), returning: "
				    + nextChannel
				    + ". Not enough bandwidth remains on the port for the requested rate "
				    + rate + "");
				return nextChannel;
			}

			/*
			 * For vcat we will invoke getNextChannel multiple times, only check the
			 * vid the first time...
			 */
			if (vcat_user_request && firstVCATMember || !vcat_user_request) {
				/* If we support multiple service flows we need to consider the VLAN id */
				if (supportsMultipleServiceFlows()) {
					// Pull out the VLAN id to use
					String vid = Utility.scanForVlanIdFromTna(parameters,
					    facility.getTNA());

					if (vid != null && vid.trim().length() > 0) {
						boolean rc = isVlanAvailable(vid);
						if (!rc) {
							nextChannel = -1;
							log.debug("getNextChannel(" + rateIdx + "), returning: "
							    + nextChannel + " vlan " + vid + " is not available!");
							return nextChannel;
						}
					}
					else {
						/**
						 * throw a fit here, if no vlan id was provided its time to
						 * complain, this tracker is a vlan based tracker!
						 */
						nextChannel = -1;
						log.error("getNextChannel("
						    + rateIdx
						    + "), returning: "
						    + nextChannel
						    + " vlan "
						    + vid
						    + " is not available, no vlanid was specified yet one is required!");
						return nextChannel;
					}
				}
			}
		}

		if (!vcat_user_request) {
			// Allow validation on the overall channel availability. 10G EPL needs to
			// allow STS192C
			// if (rateIdx <= STS24C)
			{
				if (Facility.isEth(getAid())) {
					// layer1 encapsulated ETH port
					// Empty EPL tracker or L2SS can return nextChannel as '1'
					if (test.cardinality() == 0 || supportsMultipleServiceFlows()) {
						nextChannel = 1;
					}
				}
				else {
					// non-encapsulated ETH port
					// These ports should allow bandwidth to be added to them up to the
					// port capacity
					// without restriction (ie PP8600)

					if (test.cardinality() <= MAX_SIZE_STS) {
						if (test.cardinality() != 0) {
							int lastUsedChannel = test.cardinality();

							if (lastUsedChannel + channelSizes[rateIdx] > MAX_SIZE_STS) {
								nextChannel = -1;
							}
							else {
								nextChannel = lastUsedChannel + 1;
							}
						}
						else {
							nextChannel = 1;
						}
					}
				}
			}

			// else
			// {
			// throw new
			// InvalidArgumentException("Requested rate not available in CCAT mode.");
			// }
		}
		else {
			// VCAT bandwidth request can be STS-1 or STS-3C rates.
			if (rateIdx <= STS3C) {
				nextChannel = channels.nextClearBit(1);

				if (nextChannel > MAX_SIZE_STS) {
					// IN VCAT mode, cannot exceed 21 STS 1s
					nextChannel = -1;
				}

				// The Nortel/Ciena Ethernet Private Line (EPL)
				// cards can only provision a single Ethternet
				// service mapping against a port. Therefore we
				// need to see if there is already a service
				// provisioned on this Ethernet port.
				if (firstVCATMember // If this is the first bandwith request for the new
														// service
				    && !supportsMultipleServiceFlows() // and the Ethernet facility does
																							 // not support multiple service
																							 // flows (EPL)
				    && test.cardinality() != 0) { // and there is already bandwidth
																					// provisioned against the port.
					nextChannel = -1;
				}
			}
			else {
				log.error("Requested rate not available in VCAT mode: " + rateIdx);
				throw new Exception(
				    "InvalidArgument: Requested rate not available in VCAT mode");
			}
		}

		// SDH SUPPORT
		// xxx

		return nextChannel;
	}

	@Override
	public int getNextChannel(String startingChannel, int rateIdx,
	    Map<SPF_KEYS, Object> parms) throws Exception {
		// @TODO Revisit this code!!!
		/*
		 * This seams odd, we've asked for a channel starting with x and we throw
		 * back any channel possibly smaller than x...
		 */
		int nextChannel = getNextChannel(rateIdx, parms);
		log.debug("getNextChannel(" + startingChannel + "," + rateIdx
		    + "), returning: " + nextChannel);
		return nextChannel;
	}

	@Override
	public double getUtilisation() {
		int channelsUsed = channels.cardinality();

		int totalChannels = MAX_SIZE_STS;

		double result = channelsUsed * 1.0 / totalChannels * 1.0 * 100.0;
		log.debug("getUtilisation - aid: " + getAid() + " channelsUsed: "
		    + channelsUsed + " totalChannels: " + totalChannels
		    + " percent utilisation: " + result);
		return result;
	}

	/**
	 * Release or mark as free the given bandwidth (lambda, cross connect, vlan)
	 * associated with the following connection information. Returns true if
	 * sucessfull, false or exception if not. This method is generic to layer 0, 1
	 * and 2 the "cross connect" information provided needs to support all 3
	 * layers.
	 */
	@Override
	public boolean giveBandwidth(CrossConnection c) throws Exception {
		/*
		 * Our cross connection has a A and Z end, first figure out which applies to
		 * this tracker, A or Z... Then extract the relevant data and attempt to
		 * update the tracker.
		 */

		// See additional comments in takeBandwidth
		int sizeIdx = Utility.convertStringRateToRateIdxInt(c.getRate());
		if (getAid().equals(c.getSourcePortAid())) // ||
		// getAid().equals(Utility.convertWAN2ETH(c.getSourcePortAid())))
		{
			return giveBandwidth(c.getId(), Integer.parseInt(c.getSourceChannel()),
			    sizeIdx, c.getVlanId());
		}
		else if (getAid().equals(c.getTargetPortAid())) // ||
		// getAid().equals(Utility.convertWAN2ETH(c.getTargetPortAid())))
		{
			return giveBandwidth(c.getId(), Integer.parseInt(c.getTargetChannel()),
			    sizeIdx, c.getVlanId());
		}
		else if (getAid().equals(c.getSwMatePortAid())) // ||
		// getAid().equals(Utility.convertWAN2ETH(c.getSwMatePortAid())))
		{
			return giveBandwidth(c.getId(), Integer.parseInt(c.getSwMateChannel()),
			    sizeIdx, c.getVlanId());
		}
		else {
			log.error("Failed to update tracker, cannot determine which end of the connection applies to us aid:"
			    + getAid()
			    + " "
			    + c.getSourcePortAid()
			    + " "
			    + c.getTargetPortAid()
			    + " from " + c + " in tracker " + toString());
			return false;
		}
	}

	/*
	 * @param startChannel - starting channel of request, indexed at 1
	 *
	 * @param sizeIdx - size index for the requested rate
	 */

	// @Override
	// public boolean isBandWidthAvailable(int startChannel, int sizeIdx, boolean
	// ignoreConstraints)
	// {
	// boolean result = false;
	// int realSize = channelSizes[sizeIdx];
	// int adjustedSize = realSize - 1; // Since size is added to startChannel for
	// the range,
	// // need to offset by 1 since the range is inclusive.
	// int convertedStartChannel = startChannel;
	// BitSet testAll = null;
	// BitSet tester = null;
	//
	// // SDH support
	// // xxx
	//
	// testAll = channels.get(1, convertedStartChannel + adjustedSize);
	// tester = channels.get(convertedStartChannel, convertedStartChannel +
	// adjustedSize);
	//
	// LpcpFacility.VCAT_ROUTING_TYPE routingType = facility.getVCATRoutingType();
	//
	// if (routingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_CCAT)
	// {
	// if (facility.getParent() != null)
	// {
	// result = testAll.cardinality() == 0;
	// }
	//
	// else
	// {
	// result = true;
	// }
	// }
	//
	// else
	// {
	//
	// result = tester.cardinality() == 0;
	// }
	//
	// return result;
	// }

	/*
	 * Placeholder method to satisfy interface requirements
	 */
	@Override
	public boolean isBoundaryChannel(int startChannel, int rateIdx) {
		return true;
	}

	@Override
	public boolean isContiguousUsage() {
		// Not implemented
		return false;
	}

	@Override
	public boolean isVlanAvailable(String vlanId) throws Exception {
		if (vlanId != null && vlanId.trim().length() > 0
		    && !vlanId.equalsIgnoreCase("Untagged")) {
			return !vlanIds.contains(Integer.valueOf(vlanId));
		}
		return true;
	}

	@Override
	public void setConstraints(TrackerConstraints constraints) throws Exception {
		if (constraints == null) {
			// don't complain if the constraints are null
			return;
		}
		if (BigInteger.ZERO.equals(constraints.getConstraintsInteger())) {
			// don't complain if the constraints are zero
			return;
		}

		throw new Exception(
		    "EthWanBandwidthTracker: This tracker does not support setConstraints :"
		        + constraints);
	}

	@Override
	public boolean supportsMultipleServiceFlows() {
		/*
		 * L2SS allows multiple VCSs to connect to the ETH port: multiple VCEs can
		 * be associated with a single LAN ... and each VCE can apply its own set of
		 * allow/filter VCEMAPs ...
		 */
		return facility.isL2SS();
	}

	/**
	 * Attempt to take or mark as in use the given bandwidth (lambda, cross
	 * connect, vlan) associated with the following connection information.
	 * Returns true if sucessfull, false or exception if not. This method is
	 * generic to layer 0, 1 and 2 the "cross connect" information provided needs
	 * to support all 3 layers.
	 */
	@Override
	public boolean takeBandwidth(CrossConnection c) throws Exception {
		/*
		 * Our cross connection has a A and Z end, first figure out which applies to
		 * this tracker, A or Z... Then extract the relevant data and attempt to
		 * update the tracker.
		 */
		int sizeIdx = Utility.convertStringRateToRateIdxInt(c.getRate());

		// EPL style connections have been converted to WAN by the lpcp model. L2SS
		// has not. An overall
		// redo of the eth/wan conversions should be done throughout drac, but until
		// then...

		if (getAid().equals(c.getSourcePortAid())) // ||
		// getAid().equals(Utility.convertWAN2ETH(c.getSourcePortAid())))
		{
			return takeBandwidth(c.getId(), Integer.parseInt(c.getSourceChannel()),
			    sizeIdx, c.getVlanId());
		}
		else if (getAid().equals(c.getTargetPortAid())) // ||
		// getAid().equals(Utility.convertWAN2ETH(c.getTargetPortAid())))
		{
			return takeBandwidth(c.getId(), Integer.parseInt(c.getTargetChannel()),
			    sizeIdx, c.getVlanId());
		}
		else if (getAid().equals(c.getSwMatePortAid())) // ||
		// getAid().equals(Utility.convertWAN2ETH(c.getSwMatePortAid())))
		{
			return takeBandwidth(c.getId(), Integer.parseInt(c.getSwMateChannel()),
			    sizeIdx, c.getVlanId());
		}
		else {
			log.error("Failed to update tracker, cannot determine which end of the connection applies to us aid:"
			    + getAid()
			    + " "
			    + c.getSourcePortAid()
			    + " "
			    + c.getTargetPortAid()
			    + " from " + c + " in tracker " + toString());
			return false;
		}
	}

	// @Override
	// public String toString()
	// {
	// return Utility.printBitSet(channels, aid, neid, MAX_SIZE_STS,
	// (getConstraints() != null ?
	// getConstraints()
	// .getConstraintsInteger() : null));
	// }

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EthWanBandwidthTracker [channelSizes=");
		builder.append(Arrays.toString(channelSizes));
		builder.append(", channels=");
		builder.append(channels);
		builder.append(", ethRate=");
		builder.append(ethRate);
		builder.append(", ethServiceUsage=");
		builder.append(ethServiceUsage);
		builder.append(", vlanIds=");
		builder.append(vlanIds);
		builder.append(", getNeid()=");
		builder.append(getNeid());
		builder.append(", getAid()=");
		builder.append(getAid());
		builder.append(", getConstraints()=");
		builder.append(getConstraints());
		builder.append("]");
		return builder.toString();
	}

	// private int convertChannelToSDH(int sonetChannel)
	// {
	// int sdhChannel = -1;
	// if (sonetChannel == 1)
	// {
	// sdhChannel = 1;
	// }
	// else
	// {
	// sdhChannel = (int) ((sonetChannel - 1.0) / 3.0) + 1;
	// }
	// return sdhChannel;
	// }
	//
	// private int convertChannelToSONET(int sdhChannel)
	// {
	// int sonetChannel = -1;
	// if (sdhChannel == 1)
	// {
	// sonetChannel = 1;
	// }
	// else
	// {
	// sonetChannel = (sdhChannel - 1) * 3 + 1;
	// }
	// return sonetChannel;
	// }

	private boolean addVlanId(Integer vlanId) {
		return vlanIds.add(vlanId);
	}

	private synchronized boolean giveBandwidth(String conId, int startChannel,
	    int sizeIdx, String vlanId) {

		boolean done = true;
		int realSize = channelSizes[sizeIdx];
		int convertedStartChannel = startChannel;

		// Convert SONET/SDH: convertChannelToSONET
		// xxx;

		if (Facility.isEth(getAid()) && vlanId != null
		    && vlanId.trim().length() > 0) {
			removeVlanId(Integer.valueOf(vlanId.trim()));
		}

		if (Facility.isEth(getAid()) && supportsMultipleServiceFlows()) {
			String ethServiceUsageKey = conId + "-" + startChannel + "-" + sizeIdx;

			if (ethServiceUsage.contains(ethServiceUsageKey)) {
				// See NOTE in takeBandwidth.
				int startIdx = channels.nextSetBit(1);
				int endIdx = startIdx + realSize;

				if (endIdx < channels.size()) {
					channels.flip(startIdx, endIdx);
					ethServiceUsage.remove(ethServiceUsageKey);
				}
				else {
					done = false;
					log.error("EthWanBandwidthTracker::giveBandwidth - error ");
				}
			}
			else {
				log.error("EthWanBandwidthTracker: giveBandwidth called for unmarked usage: "
				    + ethServiceUsageKey);
				done = false;
			}
		}
		else {

			BitSet test = null;

			test = channels.get(convertedStartChannel, convertedStartChannel
			    + realSize);

			if (test.cardinality() == realSize) {
				channels.flip(convertedStartChannel, convertedStartChannel + realSize);
			}
			else {
				log.error("Error, free size is not equal to size of channels used: "
                        + this.getNeid() + " " + getAid()
                        + " requested real size: "
                        + realSize
                        + " actual size: "
                        + test.cardinality()
                        * MBS_BASE_RATE
                        + "Mb/s");
			}
		}

		return done;
	}

	private void init() throws Exception {
		// Initialize the channelSizes array. All commands to the OME NE are in STS
		// rates
		channelSizes[STS1] = 1;
		channelSizes[STS3C] = 3;
		channelSizes[STS6C] = 6;
		channelSizes[STS12C] = 12;
		channelSizes[STS24C] = 24;
		channelSizes[STS48C] = 48;
		channelSizes[STS192C] = 192;

		// CONNECTION CONSTRAINTS (from the NTP):
		//
		// 1GE EPL (Eth/Wan pair) maximums:
		// contiguous concatenation (STS3c, STS12c/VC4-4c, and STS24c/VC4-8c)
		// virtual concatenation (STS1-nv/VC3-nv, where n = 1 to 21,
		// STS3c-nv/VC4-nv, where n = 1 to 7)
		//
		// 10GE EPL (Eth/Wan pair):
		// contiguous concatenation (STS192c/VC4-64c)
		// virtual concatenation (STS3-nv/VC4-nv, where n = 1 to 64)
		//
		// 20G L2SS - GE & 10GE ETH (with on-the-fly WAN) facilities, standalone WAN
		// facilities (created via
		// Site Mgr)
		// - contiguous concatenation (STS3c, STS12c/VC4-4c, STS24c/VC4-8c, and
		// STS48c/VC4-16c)
		// ... BUT it allows provisioning of STS-192C CCAT between a WAN and Line
		// - virtual concatenation STS1-nv/HO_VC3-nv/LO_VC3-nv, where n = 1 to 192,
		// STS3c-nv/VC4-nv, where n =
		// 1 to 64)
		//
		// SuperMux
		// GE ETH (PGEFC)(Eth/Wan pair)
		// - A GFP-T mapped GbE port uses STS-3c-7v/VC-4-7v
		// - A GFP-F mapped GbE can have any STS-3c/VC-4 VCAT combination up to 7v
		// FE ETH100 (PFE) (Eth/Wan pair)
		// - A GFP-T mapped ETH100 port uses STS-3c-7v/VC-4-7v ... VCAT (cannot be
		// set to CCAT)
		// - A GFP-F mapped ETH100 port uses STS-3c/VC-4 (single STS-3C) ... CCAT
		// (cannot be set to VCAT)

		LpcpFacility.VCAT_ROUTING_TYPE routingType = facility.getVCATRoutingType();
		boolean supported = false;
		String aidType = facility.getType();

		// MUST_BE_VCAT
		if (routingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_VCAT) {
			// SUPPORTED ETH TYPES
			if (Facility.isEth(aidType)) {
				if (FacilityConstants.tenGE.equals(facility.getRate())) {
					MAX_SIZE_STS = MAX_STS_SIZE_VCAT_10GE;
					this.ethRate = 9600;
					supported = true;
				}
				else if (FacilityConstants.GE.equals(facility.getRate())) {
					MAX_SIZE_STS = MAX_STS_SIZE_VCAT_1GE;
					// Allow for 7 x STS3C = 21 STSs = 1050mbs
					this.ethRate = 1050;
					supported = true;
				}

				else if (FacilityConstants.FE.equals(facility.getRate())) {
					MAX_SIZE_STS = MAX_STS_SIZE_VCAT_FE;
					// Allow for 7 x STS3C = 21 STSs = 1050mbs
					this.ethRate = 1050;
					supported = true;
				}
			}
			else if (Facility.isWan(aidType)) {
				// A stand alone WAN associated with an L2SS slot. Upto 10G VCAT
				MAX_SIZE_STS = MAX_STS_SIZE_VCAT_10GE;
				this.ethRate = 9600;
				supported = true;
			}
		}

		// MUST_BE_CCAT, CAN_BE_EITHER
		else {
			if (Facility.isEth(aidType)) {
				if (FacilityConstants.tenGE.equals(facility.getRate())) {
					MAX_SIZE_STS = MAX_STS_SIZE_CCAT_10GE;
					this.ethRate = 9600;
					supported = true;
				}

				else if (FacilityConstants.GE.equals(facility.getRate())) {
					MAX_SIZE_STS = MAX_STS_SIZE_CCAT_1GE;

					// Allow for STS24C = 24 STSs x 50 mb/s = 1200 mbs
					this.ethRate = 1200;
					supported = true;
				}

				else if (FacilityConstants.FE.equals(facility.getRate())) {
					MAX_SIZE_STS = MAX_STS_SIZE_CCAT_FE;

					// Allow for single STS3C = 3 STSs x 50 mb/s = 150 mbs
					this.ethRate = 150;
					supported = true;
				}
			}
			else if (Facility.isWan(aidType)) {
				// A stand alone WAN associated with an L2SS slot. Upto 10G CCAT
				MAX_SIZE_STS = MAX_STS_SIZE_CCAT_10GE;
				this.ethRate = 9600;
				supported = true;
			}
		}

		if (!supported) {
			throw new Exception(
			    "EthWanBandwidthTracker: This facility type is not supported :"
			        + getAid());
		}

		// The BitSet is created with MAX_SIZE + 1 because BitSet of size n creates
		// bits 0...n-1
		// Since we don't want to deal with offsets, channel 0 is never used and
		// instead we increase
		// the MAX_SIZE by 1 bit so that we can use bits 1..n to represent channel
		// numbers
		channels = new BitSet(MAX_SIZE_STS + 1); // Defaults to all bits = 0, i.e.
		                                         // all channels free
	}

	// private void printTracker()
	// {
	// }

	private boolean isETHBandwidthAvailable(int rateMbs) {
		boolean result = false;

		if (rateMbs <= ethRate) {
			// Check that total bandwidth on port does not exceed port capacity
			int channelsUsed = channels.get(1, MAX_SIZE_STS).cardinality();

			log.debug("channelsUsed: " + channelsUsed
			    + " calculated used bandwidth: " + channelsUsed * 50);



			/*
			 * An STS-1 is 51.84 Mb/s. A GigE port connected to a WAN port will allow
			 * a maximum of 21 vcat STS-1s or ~1088 Mb/s Since we are using the
			 * mapping MBS_BASE_RATE = 1 STS-1 = 50 Mb/s, we should allow a max of
			 * 1050 Mb/s Similarly for 100 Mb/s
			 */

			if (channelsUsed * MBS_BASE_RATE + rateMbs <= ethRate + MBS_BASE_RATE) {
				result = true;
			}

		}

		return result;
	}

	private synchronized boolean removeVlanId(Integer vlanId) {
		return vlanIds.remove(vlanId);
	}

	private synchronized boolean takeBandwidth(String conId, int startChannel,
	    int sizeIdx, String vlanId) throws Exception {
		log.debug("ETHBandwidthTracker::takeBandwidth - neid: " + getNeid()
		    + " AID: " + getAid() + " startChannel: " + startChannel + " sizeIdx: "
		    + sizeIdx + " vlanId: " + vlanId);

		if (Facility.isEth(getAid()) && vlanId != null
		    && vlanId.trim().length() > 0) {
			addVlanId(Integer.valueOf(vlanId.trim()));
		}

		boolean done = true;
		BitSet tester;
		int realSize = channelSizes[sizeIdx];
		int convertedStartChannel = startChannel;

		// Convert SONET/SDH convertChannelToSONET
		// xxx;

		// realSize is added to startChannel but the BitSet.get() and
		// BitSet.set() methods exclude the last bit
		// therefore, no adjustment is needed here.
		int adjustedSize = realSize;

		if (realSize <= MAX_SIZE_STS) {
			if (Facility.isEth(getAid()) && supportsMultipleServiceFlows()) {
				String ethServiceUsageKey = conId + "-" + startChannel + "-" + sizeIdx;

				if (ethServiceUsage.contains(ethServiceUsageKey)) {
					log.error("EthWanBandwidthTracker: takeBandwidth called for duplicate usage: "
					    + ethServiceUsageKey);
					done = false;
				}

				else {
					int startIdx = channels.nextClearBit(1);
					int endIdx = startIdx + adjustedSize;

					if (endIdx <= channels.size()) {
						channels.set(startIdx, endIdx);
						ethServiceUsage.add(ethServiceUsageKey);
					}
					else {
						log.error("ETthWanBandwidthTracker: Requested bandwidth exceeds facility: "
						    + getAid());
						done = false;
					}
				}
			}
			else {
				tester = channels.get(convertedStartChannel, convertedStartChannel
				    + adjustedSize);

				if (tester.cardinality() == 0) {
					channels.set(convertedStartChannel, convertedStartChannel
					    + adjustedSize);
				}
				else {
					// Overlapping bit
					int overlapIdx = convertedStartChannel + tester.nextSetBit(0);

					log.error("Request channels: "
					    + convertedStartChannel
					    + " to "
					    + (convertedStartChannel + adjustedSize)
					    + "\n Overlapping request detected!!! neid: "
					    + getNeid()
					    + " aid: "
					    + getAid()
					    + " 1st overlap channel: "
					    + overlapIdx
					    + "\n"
					    + Utility.printBitSet(channels, getAid(), "channels ("
					        + getNeid() + ")", MAX_SIZE_STS, null));

					done = false;
				}
			}
		}
		else {
			log.error("ETHBandwidthTracker: Requested bandwidth exceeds facility: "
			    + getAid());
			done = false;
		}

		return done;
	}

}
