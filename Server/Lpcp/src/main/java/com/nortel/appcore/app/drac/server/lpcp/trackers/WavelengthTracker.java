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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;

public class WavelengthTracker extends AbstractTracker {
  private final Logger log = LoggerFactory.getLogger(getClass());

	private static final int MAX_WAVELENGTHS = 72;

	private final List<String> wavelengths = new ArrayList<String>();
	private final List<String> usedWavelengths = new ArrayList<String>();

	// Constructor
	// aid = AID of the facility
	public WavelengthTracker(String neid, String aid, String wavelength,
	    TrackerConstraints trackerConstraints) throws Exception {
		super(neid, aid);
		setConstraints(trackerConstraints);
		wavelengths.add(wavelength);
		init();
	}

	// public int getNextChannel(int startChannel, int rate, BitSet srcChannels,
	// BitSet dstChannels,
	// Map<String, Object> parameters)
	// throws InvalidArgumentException
	// {
	// int nextChannel = -1;
	// try
	// {
	// if (aid.startsWith("LIM"))
	// {
	// nextChannel = startChannel;
	// }
	// else
	// {
	// if (!wavelengths.isEmpty())
	// {
	// nextChannel = Integer.parseInt(wavelengths.get(0));
	// }
	// else
	// {
	// nextChannel = startChannel;
	// }
	// }
	// }
	// catch (Exception e)
	// {
	// }
	// return nextChannel;
	// }

	/*
	 * @param rateIdx = rate index of rate to get next available channel number
	 * for Note: The returned channel number starts from channel 1.
	 */
	@Override
	public int getNextChannel(int rateIdx, Map<SPF_KEYS, Object> parameters)
	    throws Exception {
		int nextChannel = -1;
		String lambda = null;
		try {
			
			if (!wavelengths.isEmpty()) {
				lambda = wavelengths.get(0);
				if (lambda != null) {
					if (lambda.indexOf('.') > 0) { // Wavelength contains a "."
						
						lambda = lambda.replaceAll("\\.", "");
						
					}
				}
				nextChannel = Integer.parseInt(lambda);
			}
			else {
				log.error("No wavelengths available");
			}
		}
		catch (Exception e) {
			log.error("Exception getting nextChannel, lambda: " + lambda, e);
		}
		
		return nextChannel;
	}

	/*
	 * @param rateIdx = rate index of rate to get next available channel number
	 * for Note: The returned channel number starts from channel 1.
	 */
	@Override
	public int getNextChannel(String startChannel, int rateIdx,
	    Map<SPF_KEYS, Object> parameters) throws Exception {
		int nextChannel = -1;
		try {
			
			if (getAid().startsWith("LIM")) {
				nextChannel = Integer.parseInt(startChannel);
			}
			else {
				if (!wavelengths.isEmpty()) {
					nextChannel = Integer.parseInt(wavelengths.get(0));
				}
				else {
					nextChannel = Integer.parseInt(startChannel);
				}
			}
		}
		catch (Exception e) {
			log.error("Exception getting nextChannel: ", e);
		}
		log.debug("getNextChannel(" + startChannel + "," + rateIdx
		    + "), returning: " + nextChannel);
		return nextChannel;
	}

	// public int getNextChannelFor(int rateIdx, WMap<SPF_KEYS, Object>
	// parameters)
	// {
	// int nextChannel = -1;
	// try
	// {
	// 
	// if (!wavelengths.isEmpty())
	// {
	// nextChannel = Integer.parseInt(wavelengths.get(0));
	// }
	// else
	// {
	// }
	// }
	// catch (Exception e)
	// {
	// }
	// 
	// return nextChannel;
	// }

	// public String getPrintableRate(int mode, int rateIdx)
	// {
	// String printableRate = "?unknown" + mode + "_" + rateIdx;
	// try
	// {
	// if (mode == MODE.SONET)
	// {
	// printableRate = printableSTSRates[rateIdx];
	// }
	// else if (mode == MODE>SDH)
	// {
	// printableRate = printableSTMRates[rateIdx];
	// }
	// }
	// catch (Exception e)
	// {
	// printableRate = "?unknown, invalid rate for mode: " + mode;
	// }
	//
	// return printableRate;
	// }

	@Override
	public double getUtilisation() {
		double utilisation = usedWavelengths.size() / (MAX_WAVELENGTHS * 1.0) * 100;
		log.debug("neid: " + getNeid() + " aid: " + getAid() + " usedWavelengths: "
		    + usedWavelengths + " MAX_WAVELENGTHS: " + MAX_WAVELENGTHS
		    + " utilisation: " + utilisation);
		return utilisation;
	}

	@Override
	public boolean giveBandwidth(CrossConnection c) throws Exception {
		/*
		 * Our cross connection has a A and Z end, first figure out which applies to
		 * this tracker, A or Z... Then extract the relevant data and attempt to
		 * update the tracker.
		 */
		if (getAid().equals(c.getSourcePortAid())) {
			return giveBandwidth(c.getSourceChannel());
		}
		else if (getAid().equals(c.getTargetPortAid())) {
			return giveBandwidth(c.getTargetChannel());
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
	 * giveBandwidth
	 * 
	 * @param startChannel = start channel indexed at 1
	 * 
	 * @param sizeIdx = size index (e.g.
	 * STS1,STS3C,STS6C,STS12C,STS24C,STS48C,STS192C)
	 */

	// /*
	// * @param startChannel = starting channel indexed at 1
	// * @param sizeIdx = size index of the bandwidth size
	// */
	// @Override
	// public boolean isBandWidthAvailable(int startChannel, int sizeIdx, boolean
	// ignoreConstraints)
	// {
	// boolean isAvailable = false;
	//
	// try
	// {
	// if (getAid().indexOf(FacilityConstants.LIM) >= 0)
	// {
	// isAvailable = true;
	// }
	// else
	// {
	// isAvailable = usedWavelengths.size() == 0;
	// }
	// }
	// catch (Exception e)
	// {
	// }
	//
	// return isAvailable;
	// }

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
	public boolean takeBandwidth(CrossConnection c) throws Exception {

		/*
		 * Our cross connection has a A and Z end, first figure out which applies to
		 * this tracker, A or Z... Then extract the relevant data and attempt to
		 * update the tracker.
		 */

		if (getAid().equals(c.getSourcePortAid())) {
			return takeBandwidth(c.getSourceChannel());
		}
		else if (getAid().equals(c.getTargetPortAid())) {
			return takeBandwidth(c.getTargetChannel());
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
	 * takeBandwidth
	 * 
	 * @param startChannel = start channel indexed at 1
	 * 
	 * @param sizeIdx = size index (e.g.
	 * STS1,STS3C,STS6C,STS12C,STS24C,STS48C,STS192C)
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WavelengthTracker [usedWavelengths=");
		builder.append(usedWavelengths);
		builder.append(", wavelengths=");
		builder.append(wavelengths);
		builder.append(", getNeid()=");
		builder.append(getNeid());
		builder.append(", getAid()=");
		builder.append(getAid());
		builder.append(", getConstraints()=");
		builder.append(getConstraints());
		builder.append("]");
		return builder.toString();
	}

	private synchronized boolean giveBandwidth(String waveLength) {
		boolean bandwidthGiven = false;
		int idx = -1;
		for (int i = 0; i < usedWavelengths.size(); i++) {
			log.debug("aid: " + getAid() + " usedWavelengths.get(i): |"
			    + usedWavelengths.get(i) + "| startChannel: |" + waveLength + "|");
			if (usedWavelengths.get(i).equals(waveLength)) {
				idx = i;
				break;
			}
		}

		if (idx != -1) {
			wavelengths.add(usedWavelengths.remove(idx));
			bandwidthGiven = true;
		}
		return bandwidthGiven;
	}

	// @Override
	// public String toString()
	// {
	// StringBuilder sb = new StringBuilder();
	// sb.append("wavelengths: ");
	// sb.append(wavelengths);
	// sb.append("usedWavelengths: ");
	// sb.append(usedWavelengths);
	// return sb.toString();
	// }

	private void init() {

		// int tracker[];

		// int maxRateIndex = -1;

		// // Initialise all channel sizes
		// int channelSizes[];
		// if (mode == MODE.SONET)
		// {
		// channelSizes = new int[7];
		// tracker = new int[7];
		// channelSizes[AbstractBitBandwidthTrackerI.STS1] = 1;
		// channelSizes[STS3C] = 3;
		// channelSizes[STS6C] = 6;
		// channelSizes[STS12C] = 12;
		// channelSizes[STS24C] = 24;
		// channelSizes[STS48C] = 48;
		// channelSizes[STS192C] = 192;
		// }
		// else if (mode == MODE.SDH)
		// {
		// channelSizes = new int[13];
		// tracker = new int[13];
		// channelSizes[STM1] = 1;
		// channelSizes[STM2] = 2;
		// channelSizes[STM4] = 4;
		// channelSizes[STM8] = 8;
		// channelSizes[STM16] = 16;
		// channelSizes[STM64] = 64;
		// channelSizes[VC3] = 0;
		// channelSizes[VC4] = 1;
		// channelSizes[VC4_2C] = 2;
		// channelSizes[VC4_4C] = 4;
		// channelSizes[VC4_8C] = 8;
		// channelSizes[VC4_16C] = 16;
		// channelSizes[VC4_64C] = 64;
		// }

		// int maxChannels = channelSizes[rateIdx];
		// maxRateIndex = rateIdx;
		// Create a new BitSet to track channel usage
		// BitSets start at bit 0 but payload channels start at 1 (ex. 1 to 192)
		// Create a BitSet of MAX_CHANNELS + 1 so that we don't have to deal with
		// any offsets
		// Note that this means bit 0 in the channels BitSet is not to be used.
		// The newly created channels BitSet defaults to all bits = 0, i.e. all
		// channels free
		// BitSet channels = new BitSet(maxChannels + 1);

		// Initialise all trackers to channel -1 except for the tracker used for our
		// rate

		// for (int i = 0; i < 7; i++)
		// {
		// if (i != rateIdx)
		// {
		// tracker[i] = -1;
		// }
		// }
		//
		// tracker[rateIdx] = 1;

	}

	// private void printTracker()
	// {
	// // StringBuilder sb = new StringBuilder();
	// // sb.append(getNeid() + " tracker: " + getAid());
	// // for (int j = 0; j <= maxRateIndex; j++)
	// // {
	// // sb.append("\n" + getNeid() + " tracker[" + channelSizes[j] + "]: " +
	// tracker[j]);
	// // }
	// }

	private synchronized boolean takeBandwidth(String waveLength) {
		boolean bandwidthTaken = false;
		try {
			if (getAid() != null && !getAid().startsWith(FacilityConstants.LIM)) {
				if (usedWavelengths.isEmpty()) {
					if (!wavelengths.isEmpty()) {
						usedWavelengths.add(wavelengths.remove(0));
						bandwidthTaken = true;
					}
					else {
						log.error("No wavelengths available - wavelengths: " + wavelengths
						    + " usedWavelengths: " + usedWavelengths);
					}
				}
				else {
					log.error("Tracker: " + getAid() + " already used by wavelength: "
					    + usedWavelengths);
				}
			}
			else if (getAid() != null && getAid().startsWith(FacilityConstants.LIM)) {
				usedWavelengths.add(waveLength);
				bandwidthTaken = true;
			}
			else {
				log.error("Error taking bandwidth, aid=" + getAid() + " waveLength="
				    + waveLength);
			}
		}
		catch (Exception e) {
			log.error("Exception taking bandwidth: ", e);
		}
		return bandwidthTaken;
	}
}
