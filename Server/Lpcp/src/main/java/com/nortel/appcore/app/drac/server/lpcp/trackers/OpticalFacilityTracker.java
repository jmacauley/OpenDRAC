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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.server.lpcp.common.Utility;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility.MODE;

/**
 * OpticalFacilityTracker Bandwidth tracker for an optical port based on the
 * JAVA BitSet class.
 *
 * @author adlee
 */
public final class OpticalFacilityTracker extends AbstractTracker implements
    SonetTrackerI {

  private final Logger log = LoggerFactory.getLogger(getClass());

	private int tracker[];
	private int channelSizes[];
	private int maxRateIdx = -1; // range(0,7)
	private int maxChannels = -1;
	private final LpcpFacility.MODE mode; // SONET/SDH
	private final LpcpFacility.MODE nemode; // SONET/SDH
	private final int rateIdx;
	private BitSet channels; // Used to track channel usage for a facility

	/**
	 * Constructor mode = SONET or SDH
	 * <p>
	 * rateIdx =
	 * BitBandwidthTrackerI.STS1,STS3C,STS6C,STS12C,STS24C,STS48C,STS192C
	 * <p>
	 * aid = AID of the facility
	 */
	public OpticalFacilityTracker(LpcpFacility.MODE facMode,
	    LpcpFacility.MODE neMode, int rateIndex, String networkId, String neAid,
	    TrackerConstraints trackerConstraints) throws Exception {
		super(networkId, neAid);

		if (!(facMode == LpcpFacility.MODE.SONET || facMode == LpcpFacility.MODE.SDH)) {
			throw new Exception("InvalidArgument: Invalid mode " + facMode);
		}
		if (!checkRate(facMode, rateIndex)) {
			throw new Exception("InvalidArgument: Invalid rate " + rateIndex);
		}
		if (neAid == null) {
			throw new Exception("InvalidArgument: AID cannot be null");
		}
		mode = facMode;
		nemode = neMode;
		rateIdx = rateIndex;
		init();
		setConstraints(trackerConstraints);
	}

	@Override
	public SonetTrackerI getBlankCopy() throws Exception {
		/*
		 * The constructor throws an exception, but if instantiated, surely the
		 * request for it to copy itself should not have a problem.
		 */
		return new OpticalFacilityTracker(this.mode, this.nemode, this.rateIdx,
		    getNeid(), getAid(), getConstraints());
	}

	@Override
	public BitSet getInternalTracker() {
		return channels;
	}

	// @Override
	// public BitSet getInternalTrackerClone()
	// {
	// return (BitSet) channels.clone();
	// }
	//
	// @Override
	// public int getMaxSize()
	// {
	// return maxChannels;
	// }

	/*
	 * @param rateIdx = rate index of rate to get next available channel number
	 * for Note: The returned channel number starts from channel 1.
	 */
	// public int getNextChannel(int startChannel, int neRateIdx, BitSet
	// srcChannels, BitSet dstChannels,
	// Map<String, Object> parameters)
	// throws InvalidArgumentException
	// {
	//
	// Dlog.enter("getNextFreeBoundaryChannel");
	// int nextFreeStartChannel = -1;
	// BitSet combinedChannels = null;
	//
	// try
	// {
	//
	// int nextBoundaryChannel = 1;
	// BitSet test = null;
	//
	// srcChannels.or(dstChannels);
	// combinedChannels = srcChannels;
	//
	//
	// if (nextBoundaryChannel != -1)
	// {
	// // Check that the size required is available
	// boolean free = false;
	// int i = 0;
	//
	// for (i = nextBoundaryChannel; i < maxChannels; i +=
	// channelSizes[neRateIdx])
	// {
	// test = combinedChannels.get(i, i + channelSizes[neRateIdx]);
	// if (test.cardinality() == 0)
	// {
	// free = true;
	// break;
	// }
	// }
	// if (free)
	// {
	// nextFreeStartChannel = i;
	// }
	// }
	//
	// }
	// catch (Exception e)
	// {
	// throw new InvalidArgumentException(e.getMessage(), e);
	// }
	//
	// // SDH SUPPORT
	// if (nemode == MODE.SDH && nextFreeStartChannel != -1 &&
	// aid.startsWith("STM"))
	// {
	//
	// nextFreeStartChannel = convertChannelToSDH(nextFreeStartChannel);
	//
	// }
	//
	// Dlog.exit("getNextChannel");
	// return nextFreeStartChannel;
	//
	// }

	/*
	 * @param rateIdx = rate index of rate to get next available channel number
	 * for Note: The returned channel number starts from channel 1.
	 */
	@Override
	public int getNextChannel(int neRateIdx, Map<SPF_KEYS, Object> parameters)
	    throws Exception {
		int channel = tracker[neRateIdx];
		log.debug("OpticalFacilityTracker::getNextChannel::rateIdx: " + neRateIdx
		    + " tracker[rateIdx]: " + tracker[neRateIdx] + " channel: " + channel);

		// SDH SUPPORT
		if (nemode == LpcpFacility.MODE.SDH && channel != -1
		    && getAid().startsWith("STM")) {

			channel = convertChannelToSDH(channel);

		}


		return channel;
	}

	/*
	 * @param rateIdx = rate index of rate to get next available channel number
	 * for Note: The returned channel number starts from channel 1.
	 */
	//

	@Override
	public int getNextChannel(String startChannel, int neRateIdx,
	    Map<SPF_KEYS, Object> parms) throws Exception {
		int channel = -1;

		channel = getNextFreeBoundaryChannel(Integer.parseInt(startChannel),
		    neRateIdx);
		log.debug("OpticalFacilityTracker::getNextChannel::rateIdx: " + neRateIdx
		    + " channelSizes[rateIdx]: " + channelSizes[neRateIdx] + " channel: "
		    + channel);

		// SDH SUPPORT
		if (nemode == MODE.SDH && channel != -1 && getAid().startsWith("STM")) {

			channel = convertChannelToSDH(channel);

		}

		log.debug("getNextChannel(" + startChannel + "," + neRateIdx
		    + "), returning: " + channel);
		return channel;
	}

	@Override
	public double getUtilisation() throws Exception {
		BitSet channelsClone = new BitSet(0);
		channelsClone.or(channels);

		if (getConstraints().getConstraints() != null) {
			channelsClone.andNot(getConstraints().getConstraints());
		}

		int channelsUsed = channelsClone.cardinality();
		int totalChannels = channelSizes[rateIdx];
		double result = channelsUsed * 1.0 / totalChannels * 1.0 * 100.0;

		log.debug("getUtilisation - neid: " + getNeid() + " aid: " + getAid()
		    + " channelsUsed: " + channelsUsed + " totalChannels: " + totalChannels
		    + " percent utilisation: " + result);
		return result;
	}

	@Override
	public boolean giveBandwidth(CrossConnection c) throws Exception {

		/*
		 * Our cross connection has a A and Z end, first figure out which applies to
		 * this tracker, A or Z... Then extract the relevant data and attempt to
		 * update the tracker.
		 */
		int sizeIdx = Utility.convertStringRateToRateIdxInt(c.getRate());
		if (getAid().equals(c.getSourcePortAid())) {
			return giveBandwidth(c.getId(), Integer.parseInt(c.getSourceChannel()),
			    sizeIdx);
		}
		else if (getAid().equals(c.getTargetPortAid())) {
			return giveBandwidth(c.getId(), Integer.parseInt(c.getTargetChannel()),
			    sizeIdx);
		}
		else if (getAid().equals(c.getSwMatePortAid())) {
			return giveBandwidth(c.getId(), Integer.parseInt(c.getSwMateChannel()),
			    sizeIdx);
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
	 * @param startChannel = starting channel indexed at 1
	 *
	 * @param sizeIdx = size index of the bandwidth size
	 */
	// @Override
	// public boolean isBWavailable(int startChannel, int sizeIdx, boolean
	// ignoreConstraints)
	// {
	// BitSet tester = new BitSet(192); // Defaults to all bits zero
	// int realSize = channelSizes[sizeIdx];
	// BitSet trackerBits = channels;
	//
	//
	// if (ignoreConstraints)
	// {
	// trackerBits = (BitSet) channels.clone();
	// trackerBits.andNot(getConstraints().getConstraints());
	// }
	//
	// if (startChannel > 0 && sizeIdx >= 0)
	// {
	// int convertedStartChannel = startChannel;
	// // SDH SUPPORT
	// if (nemode == MODE.SDH && aid.startsWith("STM"))
	// {
	// // Convert the channel number back to SONET for checking since we store
	// everything in SONET
	// // mode
	// convertedStartChannel = convertChannelToSONET(startChannel);
	// }
	// // startChannel comes in as a standard payload number from 1 to size of
	// payload
	// // BitSet is indexed from bit 0 but channels is created with 1 extra bit to
	// avoid offsets.
	// // The realSize is either 1,3,6,12,24,48,192 corresponding to equivalent
	// STS rates
	// // Note that all rates in OME are in SONET payload sizes
	// // The realSize does not need to be offset by 1 since BitSet.get() excludes
	// the last bit.
	// // Example:
	// // Get STS3C worth of bandwidth starting at channel 1: startChannel = 1,
	// size = 1, realSize =3
	// // Bits to check are bits 1,2,3 so realSize should to be offset by 1 to 2
	// 1+2 = 3 but BitSet.get()
	// // already excludes the last bit so no need to offset.
	// tester = trackerBits.get(convertedStartChannel, convertedStartChannel +
	// realSize);
	// // .
	// // get
	// // (
	// // )
	// // excludes
	// // last
	// // bit
	//
	//
	// }
	// else
	// {
	// }
	// return tester.cardinality() == 0;
	// }

	@Override
	public boolean isBoundaryChannel(int channel, int sizeIdx) throws Exception {

		boolean isBoundary = false;
		int channelLessOne = channel - 1;
		int nextBoundaryChannel = -1;

		if (channel == 1) {
			isBoundary = true;
		}
		else if (channel > 1) {
			try {
				nextBoundaryChannel = getNextBoundaryChannel(channelLessOne,
				    channelSizes[sizeIdx]);
				log.debug("channel to check: " + channel + " nextBoundaryChannel from "
				    + channelLessOne + " is: " + nextBoundaryChannel
				    + " channelSize to use: " + channelSizes[sizeIdx]);

				isBoundary = nextBoundaryChannel == channel;

			}
			catch (Exception e) {
				log.error("isBoundaryChannel check failed: ", e);
			}
		}
		return isBoundary;

	}

	@Override
	public boolean isContiguousUsage() throws Exception {
		boolean result = true;

		if (!channels.isEmpty()) {
			BitSet shiftLeft = channels
			    .get(channels.nextSetBit(0), channels.length());
			int nextClear = shiftLeft.nextClearBit(0);
			result = !(nextClear < shiftLeft.length());
		}

		return result;
	}

	@Override
	public void setConstraints(TrackerConstraints newConstraints)
	    throws Exception {
		BitSet clone = (BitSet) getInternalTracker().clone();
		TrackerConstraints currentConstraints = getConstraints();


		if (currentConstraints != null) {
			log.debug(Utility.printBitSet(clone, getAid(), "Current tracker ("
			    + getNeid() + ")", channelSizes[rateIdx],
			    currentConstraints.getConstraintsInteger()));
			log.debug(Utility.printBitSet(currentConstraints.getConstraints(),
			    getAid(), "Current constraints", channelSizes[rateIdx], null));
			clone.andNot(currentConstraints.getConstraints());
		}
		else {
			log.debug(Utility.printBitSet(clone, getAid(), "Current tracker ("
			    + getNeid() + ")", channelSizes[rateIdx], null));
		}

		log.debug(Utility.printBitSet(clone, getAid(),
		    "Current tracker without constraints (" + getNeid() + ")",
		    channelSizes[rateIdx], null));
		// if ( clone.cardinality() == 0 ) {
		super.setConstraints(newConstraints);
		log.debug(Utility.printBitSet(getInternalTracker(), getAid(),
		    "Tracker before applying constraints (" + getNeid() + ")",
		    channelSizes[rateIdx], null));
		// First, remove the existing constraints
		if (currentConstraints != null) {
			getInternalTracker().andNot(currentConstraints.getConstraints());
		}
		log.debug(Utility.printBitSet(getInternalTracker(), getAid(),
		    "Tracker after removing current constraints (" + getNeid() + ")",
		    channelSizes[rateIdx], null));
		// Apply the new constraints
		getInternalTracker().or(newConstraints.getConstraints());
		// Update the trackers
		// We have to update each tracker manually in this case using the for-loop
		// This is the only way to force the update for each tracker to start at
		// channel 1
		// using updateTracker
		for (int i = 0; i <= maxRateIdx; i++) {
			updateTracker(1, i);
		}
		log.debug(Utility.printBitSet(getInternalTracker(), getAid(),
		    "Tracker after applying constraints (" + getNeid() + ")",
		    channelSizes[rateIdx], getConstraints().getConstraintsInteger()));
		printTracker();
		// } else {
		// Utility.printBits( getInternalTracker(), aid, "Tracker (" + neid + ")",
		// channelSizes[rateIdx] );
		// InvalidArgumentException iae = new InvalidArgumentException(
		// "Constraints violation: Port is not empty." );
		// throw( iae );
		// }
	}

	@Override
	public boolean takeBandwidth(CrossConnection c) throws Exception {

		/*
		 * Our cross connection has a A and Z end, first figure out which applies to
		 * this tracker, A or Z... Then extract the relevant data and attempt to
		 * update the tracker.
		 */
		int sizeIdx = Utility.convertStringRateToRateIdxInt(c.getRate());
		if (getAid().equals(c.getSourcePortAid())) {
			return takeBandwidth(c.getId(), Integer.parseInt(c.getSourceChannel()),
			    sizeIdx);
		}
		else if (getAid().equals(c.getTargetPortAid())) {
			return takeBandwidth(c.getId(), Integer.parseInt(c.getTargetChannel()),
			    sizeIdx);
		}
		else if (getAid().equals(c.getSwMatePortAid())) {
			return takeBandwidth(c.getId(), Integer.parseInt(c.getSwMateChannel()),
			    sizeIdx);
		}
		else {
			log.error("takeBandwidth: Failed to update tracker, cannot determine which end of the connection applies to us aid:"
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

	// @Override
	// public String toString()
	// {
	// if (getConstraints() != null)
	// {
	// return Utility.printBitSet(channels, getAid(), getNeid(), maxChannels,
	// getConstraints().getConstraintsInteger());
	// }
	// return Utility.printBitSet(channels, getAid(), getNeid(), maxChannels,
	// null);
	// }

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OpticalFacilityTracker [channelSizes=");
		builder.append(Arrays.toString(channelSizes));
		builder.append(", channels=");
		builder.append(channels);
		builder.append(", maxChannels=");
		builder.append(maxChannels);
		builder.append(", maxRateIdx=");
		builder.append(maxRateIdx);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", nemode=");
		builder.append(nemode);
		builder.append(", rateIdx=");
		builder.append(rateIdx);
		builder.append(", tracker=");
		builder.append(Arrays.toString(tracker));
		builder.append(", getNeid()=");
		builder.append(getNeid());
		builder.append(", getAid()=");
		builder.append(getAid());
		builder.append(", getConstraints()=");
		builder.append(getConstraints());
		builder.append("]");
		return builder.toString();
	}

	// protected for junit test cases
	protected synchronized boolean takeBandwidth(String connId, int startChannel,
	    int sizeIdx) throws Exception {
		boolean done = true;
		int realSize = channelSizes[sizeIdx];
		int printableRealSize = realSize - 1; // For printing, must decrement
		int convertedStartChannel = startChannel;
		BitSet tester; // = new BitSet(maxChannels); // Defaults to all bits zero

		log.debug("startChannel: " + startChannel + " sizeIdx: " + sizeIdx
		    + "real size: " + realSize);

		if (startChannel >= 1 && sizeIdx >= 0) {
			// SDH SUPPORT
			if (nemode == LpcpFacility.MODE.SDH && getAid().startsWith("STM")) {

				convertedStartChannel = convertChannelToSONET(startChannel);

			}

			// startChannel comes in as a standard payload number from 1 to size of
			// payload
			// BitSet is indexed from bit 0 but the channels BitSet is created with 1
			// extra bit
			// so no offset is required.
			// The realSize is either 1,3,6,12,24,48,192 corresponding to equivalent
			// STS rates
			// Note that all rates in OME are in SONET payload sizes
			// The realSize would normally need to be offset by 1 because the starting
			// channel needs
			// to be counted. However, the BitSet.set() and get() methods exclude the
			// last bit so no
			// offsetting
			// is required.
			// Example:
			// Get STS3C worth of bandwidth starting at channel 1: startChannel = 1,
			// size = 1, realSize =3
			// Bits to check are bits 1,2,3 so realSize should be offset by 1 to 2
			// since we go from
			// startChannel
			// to startChannel+realSize
			// However, since the BitSet.set() and get() methods effectively does this
			// for us, the realSize
			// should not be
			// to be adjusted.
			tester = channels.get(convertedStartChannel, convertedStartChannel
			    + realSize);

			log.debug("Checking channels: " + convertedStartChannel + " to: "
			    + (convertedStartChannel + printableRealSize));

			if (tester.cardinality() == 0) {

				log.debug("tester.cardinality() returned: " + tester.cardinality()
				    + "\nBandwidth is available, taking bandwidth.\n"
				    + "Raw channels.toString() before: " + channels.toString());
				log.debug(Utility.printBitSet(channels, getAid(),
				    "channels (takeBandwidth::before taking bandwidth) - " + getNeid(),
				    maxChannels, null));
				channels.set(convertedStartChannel, convertedStartChannel + realSize);

				log.debug(Utility.printBitSet(channels, getAid(),
				    "channels (takeBandwidth::after taking bandwidth) - " + getNeid(),
				    maxChannels, null));
			}
			else {
				// Overlapping bit
				int overlapIdx = convertedStartChannel + tester.nextSetBit(0);
				log.debug(Utility.printBitSet(channels, getAid(), "channels ("
				    + getNeid() + ")", maxChannels, null));
				log.error("Overlapping request detected, internal channel: "
				    + overlapIdx + " (external=" + overlapIdx + ") not available!");
				done = false;
			}

			if (done) {

				// Update all trackers
				// Always update trackers from channel 1
				// Example: channels 1,2,3 are available.
				// Take channel 1
				// Take channel 2
				// Free channel 1
				// Restart server
				// Take channel 2 - but channel 1 is free therefore, must update from
				// channel 1
				try {
					log.debug("Starting search from channel 1, but CAN start from channel: "
					    + channels.nextClearBit(1));
					updateTrackers(1, sizeIdx);
				}
				catch (Exception iae) {
					log.error("Invalid parameter detected", iae);
				}
				// printTracker();
			}
		}
		else {
			log.error("Invalid startChannel or size index.  startChannel: "
			    + startChannel + " sizeIdx: " + sizeIdx);
		}
		return done;
	}

	private boolean checkRate(LpcpFacility.MODE neMode, int neRateIdx) {
		boolean ok = false;
		if (neMode == LpcpFacility.MODE.SONET) {
			ok = neRateIdx >= STS1 && neRateIdx <= STS192C;
		}
		else if (neMode == LpcpFacility.MODE.SDH) {
			ok = neRateIdx >= STM1 && neRateIdx <= VC4_64C;
		}
		return ok;
	}

	private int convertChannelToSDH(int sonetChannel) {
		int sdhChannel = -1;
		if (sonetChannel == 1) {
			sdhChannel = 1;
		}
		else {
			sdhChannel = (int) ((sonetChannel - 1.0) / 3.0) + 1;
		}
		return sdhChannel;
	}

	private int convertChannelToSONET(int sdhChannel) {
		int sonetChannel = -1;
		if (sdhChannel == 1) {
			sonetChannel = 1;
		}
		else {
			sonetChannel = (sdhChannel - 1) * 3 + 1;
		}
		return sonetChannel;
	}

	/*
	 * getNextBoundaryChannel Given an input startChannel and a size (actual size
	 * of bandwidth required), returns the next boundary channel for the specified
	 * bandwidth size.
	 *
	 * @param startChannel = starting channel indexed at 1
	 *
	 * @param size = number of channels ex. 1,3,6,12,24,48,192
	 */
	private int getNextBoundaryChannel(int startChannel, int size)
	    throws Exception {
		int nextBoundary = -1;

		if (startChannel > 0) {
			// Must multiply the size by 1.0 to make it a double otherwise precision
			// is lost
			nextBoundary = (int) Math.ceil(startChannel / (size * 1.0)) * size + 1;
			if (nextBoundary > maxChannels) {
				nextBoundary = -1;
			}
		}
		else {
			Exception iae = new Exception(
			    "InvalidArgument: Invalid startChannel detected: " + startChannel);
			throw iae;
		}
		return nextBoundary;
	}

	/**
	 * getNextFreeBoundaryChannel Given an input startChannel and a size index
	 * (STS1,STS3C,STS6C,...,STS192C), returns the next boundary channel where the
	 * requested channel size is available.
	 *
	 * @param startChannel
	 *          - starting channel indexed at 1
	 * @param sizeIdx
	 *          - size index ex. (0,1,2,3,4,5,6,7)
	 */
	private int getNextFreeBoundaryChannel(int startChannel, int sizeIdx)
	    throws Exception {
		int nextFreeStartChannel = -1;
		BitSet test = null;

		log.debug("OpticalFacilityTracker::getNextFreeBoundaryChannel - startChannel: "
		    + startChannel
		    + " sizeIdx: "
		    + sizeIdx
		    + " realsize: "
		    + channelSizes[sizeIdx]);

		// NOTE: Important behaviour considerations here ... see
		// OpticalFacilityTrackerTest

		// Most efficient: allow back-fill:
		// int i = 1;

		// Respect the startChannel requested thus not allowing back-fill.
		// Required for managing ENNI/UNI hand-off
		// Will monitor for contiguous vcat blocks at the caller level.
		int i = startChannel != -1 ? startChannel : 1;

		for (; i < maxChannels; i += channelSizes[sizeIdx]) {
			test = channels.get(i, i + channelSizes[sizeIdx]);

            /* John reduced the bit range by one to model the proper timeslot range. */
			log.debug("For bits: " + i + " to: " + (i + channelSizes[sizeIdx] - 1)
			    + " test cardinality is: " + test.cardinality());

			if (test.cardinality() == 0) {
				// The requested block of b/w has been found:
				nextFreeStartChannel = i;
				break;
			}
		}
		return nextFreeStartChannel;
	}

	private synchronized boolean giveBandwidth(String connId, int startChannel,
	    int sizeIdx) throws Exception {
		boolean done = true;
		int convertedStartChannel = startChannel;
		BitSet test = null;

		// SDH SUPPORT
		if (nemode == LpcpFacility.MODE.SDH && getAid().startsWith("STM")) {

			convertedStartChannel = convertChannelToSONET(startChannel);

		}

		test = channels.get(convertedStartChannel, convertedStartChannel
		    + channelSizes[sizeIdx]);

		if (test.cardinality() == channelSizes[sizeIdx]) {
			// Note BitSet.set() includes the specified starting bit but excludes the
			// ending bit so that no offsetting is required of the ending channel
			channels.flip(convertedStartChannel, convertedStartChannel
			    + channelSizes[sizeIdx]);
			// Update all trackers with size <= the block size that was just returned.
			for (int i = STS1; i <= sizeIdx; i++) {

				log.debug("OpticalFacilityTracker::giveBandwidth::tracker[" + i + "]: "
				    + tracker[i] + " convertedStartChannel: " + convertedStartChannel);

				if (tracker[i] >= 0) {
					if (convertedStartChannel < tracker[i]) {
						tracker[i] = convertedStartChannel;
					}
				}
				else {
					tracker[i] = convertedStartChannel;
				}
			}

			// Update all trackers with size > the block size that was just returned.
			try {
				for (int i = sizeIdx + 1; i <= maxRateIdx; i++) {
					tracker[i] = getNextFreeBoundaryChannel(1, i);
				}
			}
			catch (Exception iae) {
				log.error("Invalid parameter detected", iae);
			}
		}
		else {
			log.debug("test.cardinality is: " + test.cardinality() + " channelSizes["
			    + sizeIdx + "] is: " + channelSizes[sizeIdx]);
			for (int i = 0; i <= channelSizes[sizeIdx]; i++) {
				if (!test.get(i)) {
					log.debug("Error, channel: " + (convertedStartChannel + i)
					    + " is already freed!");
				}
			}
		}
		return done;
	}

	private void init() {

		// Initialise all channel sizes
		if (mode == LpcpFacility.MODE.SONET) {
			channelSizes = new int[7];
			tracker = new int[7];
			channelSizes[STS1] = 1;
			channelSizes[STS3C] = 3;
			channelSizes[STS6C] = 6;
			channelSizes[STS12C] = 12;
			channelSizes[STS24C] = 24;
			channelSizes[STS48C] = 48;
			channelSizes[STS192C] = 192;
		}
		else if (mode == LpcpFacility.MODE.SDH) {
			channelSizes = new int[13];
			tracker = new int[13];
			channelSizes[STM1] = 1;
			channelSizes[STM2] = 2;
			channelSizes[STM4] = 4;
			channelSizes[STM8] = 8;
			channelSizes[STM16] = 16;
			channelSizes[STM64] = 64;
			channelSizes[VC3] = 0;
			channelSizes[VC4] = 1;
			channelSizes[VC4_2C] = 2;
			channelSizes[VC4_4C] = 4;
			channelSizes[VC4_8C] = 8;
			channelSizes[VC4_16C] = 16;
			channelSizes[VC4_64C] = 64;
		}

		maxChannels = channelSizes[rateIdx];
		maxRateIdx = rateIdx;
		/*
		 * Create a new BitSet to track channel usage BitSets start at bit 0 but
		 * payload channels start at 1 (ex. 1 to 192) Create a BitSet of
		 * MAX_CHANNELS + 1 so that we don't have to deal with any offsets Note that
		 * this means bit 0 in the channels BitSet is not to be used. The newly
		 * created channels BitSet defaults to all bits = 0, i.e. all channels free
		 */
		channels = new BitSet(maxChannels + 1);
		log.debug(Utility.printBitSet(channels, getAid(), "channels (" + getNeid()
		    + ")", maxChannels, null));

		// Initialise all trackers to channel 1 or -1 if tracker is greater than
		// rate
		for (int i = 0; i < 7; i++) {
			if (i <= maxRateIdx) {
				tracker[i] = 1;
			}
			else {
				tracker[i] = -1;
			}
		}
	}

	private void printTracker() {
		StringBuilder sb = new StringBuilder();
		sb.append(getNeid() + " tracker: " + getAid());
		for (int j = 0; j <= maxRateIdx; j++) {
			sb.append("\n tracker[" + channelSizes[j] + "]: " + tracker[j]);
		}

	}

	/*
	 * Update a single tracker
	 *
	 * @param startChannel = the channel to start at (indexed from 1)
	 *
	 * @param sizeIdx = the index into the channelSizes array
	 */
	private synchronized void updateTracker(int startChannel, int sizeIdx)
	    throws Exception {
		tracker[sizeIdx] = getNextFreeBoundaryChannel(startChannel, sizeIdx);
	}

	/*
	 * Update all trackers
	 *
	 * @param startChannel = the channel to start at (indexed from 1)
	 *
	 * @param sizeIdx = the index into the channelSizes array
	 */
	private synchronized void updateTrackers(int startChannel, int sizeIdx)
	    throws Exception {
		tracker[sizeIdx] = getNextFreeBoundaryChannel(startChannel, sizeIdx);
		// Update the rest of the trackers

		for (int i = 0; i <= maxRateIdx; i++) {
			if (i != sizeIdx) { // Don't update for the channel size that has already
				                  // been updated

				if (tracker[i] != -1) {
					tracker[i] = getNextFreeBoundaryChannel(tracker[i], i);
				}
				else {
					// If the tracker for the specified rate is currently -1, (no
					// bandwidth available for
					// this rate), then we need to check starting from channel 1 whether
					// there is now
					// enough bandwidth available.
					tracker[i] = getNextFreeBoundaryChannel(1, i);
				}
			}
		}
	}
}
