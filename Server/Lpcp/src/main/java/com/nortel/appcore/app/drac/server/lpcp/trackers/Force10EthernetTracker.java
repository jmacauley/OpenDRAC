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
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.server.lpcp.Lpcp;
import com.nortel.appcore.app.drac.server.lpcp.common.Utility;

public final class Force10EthernetTracker extends AbstractTracker implements
    EthTrackerI {
	
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final LpcpFacility facility;
	private final Set<Integer> vlanIds = new TreeSet<Integer>();
	
	private int currentUsage;
	private int portSize = 0;

	public Force10EthernetTracker(final LpcpFacility facility) {
		super(facility.getNeId(), facility.getAid());
		this.facility = facility;

		if (facility.getAid().startsWith("GigabitEthernet")) {
			portSize = 1000;
		}
		else if (facility.getAid().startsWith("TenGigabitEthernet")) {
			portSize = 10000;
		}
		else if (facility.getAid().startsWith("FortyGigE")) {
			portSize = 40000;
		}
		
		if (portSize == 0) {
			log.error("Unable to determine the port size based on the port type "
			    + facility);
		}

		vlanIds.add(0);

		// Block off vlans that are currently in use on the switch.
		String inuseVids = facility.getExtendedAttributes().get("InUseVlans");
		if (inuseVids != null) {
			log.debug("Updating tracker with OSS in use VLANS "
			    + inuseVids + " " + toString());
			StringTokenizer st = new StringTokenizer(inuseVids, ",");
			while (st.hasMoreTokens()) {
				String vid = st.nextToken().trim();
				addVlanId(Integer.valueOf(vid));
			}
		}

		/*
		 * Force10 Rule: 1024 VLANs can be in operation at one time, any of which
		 * can have a VLAN ID up to 3965. The top 129 VLANs are reserved.
		 */

		for (int i = 3966; i <= 4095; i++) {
			addVlanId(i);
		}

		/*
		 * Our web interface also permits untagged and all tagged vlan numbers for
		 * the use of the L2SS card. We restrict them here buy marking them in use.
		 */
		// 4096
		addVlanId(Integer.valueOf(FacilityConstants.UNTAGGED_LOCLBL_VALUE));
		// 4098
		addVlanId(Integer.valueOf(FacilityConstants.ALLTAGGED_LOCLBL_VALUE));
	}

	@Override
	public int getNextChannel(int rateIdx, Map<SPF_KEYS, Object> parameters)
	    throws Exception {
		log.debug("Force10EthernetTracker: getNextChannel(" + rateIdx + ","
		    + parameters + ") invoked " + toString());
		int nextChannel = 1;
		int rate = Integer.parseInt((String) parameters.get(SPF_KEYS.SPF_RATE));

		/**
		 * Do some product specific path level validation here. we don't support
		 * vcat or 1+1 protection, if set, refuse to provide bandwidth. This will
		 * cause Dijkstra to choose an alternative path if possible or fail and give
		 * a no bandwidth error message, which is misleading, but better than saying
		 * it works when it does not.
		 * <p>
		 * 
		 * @TODO How do we perform validation on a facility basis of the overall
		 *       request and provide a useful error message back to the user?
		 */

		boolean vcat_user_request = "true".equalsIgnoreCase((String) parameters
		    .get(SPF_KEYS.SPF_VCATROUTING_OPTION));
		if (vcat_user_request) {
			nextChannel = -1;
			log.error("Force10EthernetTracker: getNextChannel(" + rateIdx
			    + "), returning: " + nextChannel
			    + ". This tracker does not support vcat requests! " + toString()
			    + " parms " + parameters);
			return nextChannel;
		}

		if (Lpcp.PRT_PATH1PLUS1.equalsIgnoreCase((String) parameters
		    .get(SPF_KEYS.SPF_PROTECTION))) {
			nextChannel = -1;
			log.error("Force10EthernetTracker: getNextChannel(" + rateIdx
			    + "), returning: " + nextChannel
			    + ". This tracker does not support 1+1 path protection! "
			    + toString() + " parms " + parameters);
			return nextChannel;
		}

		if (currentUsage + rate > portSize) {
			nextChannel = -1;
			log.warn("Channel:" + rateIdx
			    + ", returning: " + nextChannel + ".  Facility's current usage:"
			    + currentUsage + " requested rate " + rate
			    + " execeeds the size of the port " + portSize
			    + " No bandwidth remains:\n" + toString());
			return nextChannel;
		}

		/**
		 * Make sure that the requested vlan is not already in use, in which case
		 * return -1 as the port is already in use...
		 */
		String vid = Utility.scanForVlanIdFromTna(parameters, facility.getTNA());

		if (vid != null && vid.trim().length() > 0) {
			boolean rc = isVlanAvailable(vid);
			if (!rc) {
				nextChannel = -1;
				log.debug("Force10EthernetTracker: getNextChannel(" + rateIdx
				    + "), returning: " + nextChannel + " vlan " + vid
				    + " is not available!");
				return nextChannel;
			}
		}
		else {
			/**
			 * throw a fit here, if no vlan id was provided its time to complain, this
			 * tracker is a vlan based tracker!
			 */
			nextChannel = -1;
			log.error("Force10EthernetTracker: getNextChannel(" + rateIdx
			    + "), returning: " + nextChannel + " vlan " + vid
			    + " is not available, no vlanid was specified yet one is required!");
			return nextChannel;
		}

		nextChannel = 1;
		log.debug("Force10EthernetTracker: getNextChannel(" + rateIdx
		    + "), VLAN ID (" + vid + ") returning: " + nextChannel);
		return nextChannel;
	}

	@Override
	public int getNextChannel(String startingChannel, int rate,
	    Map<SPF_KEYS, Object> parms) throws Exception {
		return getNextChannel(rate, parms);
	}

	@Override
	public double getUtilisation() throws Exception {
		double result = currentUsage * 1.0 / portSize * 1.0 * 100.0;
		log.debug("getUtilisation - aid: " + getAid() + " currentUsage: "
		    + currentUsage + " portSize: " + portSize + " percent utilisation: "
		    + result);
		return result;
	}

	/**
	 * Release or mark as free the given bandwidth (lambda, cross connect, vlan)
	 * associated with the following connection information. Returns true if
	 * Successful, false or exception if not. This method is generic to layer 0, 1
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

		int size;
		try {
			size = Integer.parseInt(c.getRateinMbs());
		}
		catch (Exception e) {
			throw new Exception(
			    "Force10EthernetTracker: Unable to extact rate in mbs from cross connect "
			        + c, e);
		}

		Integer vlan = null;
		try {
			vlan = Integer.valueOf(c.getVlanId());
		}
		catch (Exception e) {
			throw new Exception(
			    "Force10EthernetTracker: Unable to extact vlan from cross connect "
			        + c, e);
		}

		if (getAid().equals(c.getSourcePortAid())) {
			return giveBandwidth(size, vlan);
		}
		else if (getAid().equals(c.getTargetPortAid())) {
			return giveBandwidth(size, vlan);
		}
		else {
			log.error("Force10EthernetTracker: Failed to update tracker, cannot determine which end of the connection applies to us aid:"
			    + getAid()
			    + " "
			    + c.getSourcePortAid()
			    + " "
			    + c.getTargetPortAid()
			    + " from " + c + " in tracker " + toString());
			return false;
		}
	}

	@Override
	public boolean isVlanAvailable(String vlanId) throws Exception {
		if (vlanId != null && vlanId.trim().length() > 0) {
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
		    "Force10EthernetTracker: This tracker does not support setConstraints :"
		        + constraints);
	}

	@Override
	public boolean supportsMultipleServiceFlows() throws Exception {
		return true; // today we only support Ethernet facilities on force10 that
		             // are vlan switched.
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

		int size;
		try {
			size = Integer.parseInt(c.getRateinMbs());
		}
		catch (Exception e) {
			throw new Exception(
			    "Force10EthernetTracker: Unable to extact rate in mbs from cross connect "
			        + c, e);
		}

		Integer vlan = null;
		try {
			vlan = Integer.valueOf(c.getVlanId());
		}
		catch (Exception e) {
			throw new Exception(
			    "Force10EthernetTracker: Unable to extact vlan from cross connect "
			        + c, e);
		}

		if (getAid().equals(c.getSourcePortAid())) {
			return takeBandwidth(size, vlan);
		}
		else if (getAid().equals(c.getTargetPortAid())) {
			return takeBandwidth(size, vlan);
		}
		else {
			log.error("Force10EthernetTracker: Failed to update tracker, cannot determine which end of the connection applies to us aid:"
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
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Force10EthernetTracker [portSize=");
		builder.append(portSize);
		builder.append(", currentUsage=");
		builder.append(currentUsage);
		builder.append(", vlanIds=");
		builder.append(vlanIds);
		builder.append(", getNeid()=");
		builder.append(getNeid());
		builder.append(", getAid()=");
		builder.append(getAid());
		builder.append("]");
		return builder.toString();
	}

	private boolean addVlanId(Integer vlanId) {
		boolean rc = vlanIds.add(vlanId);
		if (!rc) {
			log.error("Force10EthernetTracker: Unable to add vlanId " + vlanId
			    + " to the inuse vlans, vlan was already marked as in use! "
			    + toString());
		}
		return rc;
	}

	private boolean giveBandwidth(int size, Integer vlanId) throws Exception {
		boolean rc = true;
		log.debug("Force10EthernetTracker: giveBandwidth(" + size + ", " + vlanId
		    + ") against " + toString());

		rc = removeVlanId(vlanId);

		if (size > portSize) {
			rc = false;
			log.error("Force10EthernetTracker: giveBandwidth attempting to return more bandwidth that the port supports! size: "
			    + size + " exceeds the size of the port " + portSize);
		}
		currentUsage -= size;
		if (currentUsage < 0) {
			rc = false;
			log.error("Force10EthernetTracker: giveBandwidth bandwidth returned "
			    + size
			    + " exceeds the current utilization of the port usage is now negative "
			    + currentUsage);
		}

		return rc;
	}

	private boolean removeVlanId(Integer vlanId) {
		boolean rc = vlanIds.remove(vlanId);
		if (!rc) {
			log.error("Force10EthernetTracker: Unable to remove vlanId " + vlanId
			    + " from the inuse vlans, vlan was not marked as in use! " + vlanIds);
		}
		return rc;
	}

	private boolean takeBandwidth(int size, Integer vlanId) throws Exception {
		boolean rc = true;
		log.debug("Force10EthernetTracker: takeBandwidth(" + size + "," + vlanId
		    + ") against " + toString());
		currentUsage += size;
		rc = addVlanId(vlanId);
		if (currentUsage > portSize) {
			rc = false;
			log.error("Force10EthernetTracker: takeBandwidth bandwidth taken "
			    + currentUsage + " exceeds the size of the port " + portSize);

		}
		return rc;
	}
}
