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

import java.util.Map;

import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;

// Methods common to all tracker interfaces

public interface BasicTracker {
	String getAid();

	/**
	 * Return the constraints that the administrator set for this port, if
	 * supported.
	 */
	TrackerConstraints getConstraints();

	String getNeid();

	/**
	 * Return the next available channel to use at the given rate. This is a
	 * generic method and applies to layer 0,1 and 2. A channel can be a
	 * wavelength, sts time slot or vlan id. Return -1 if no channel is available.
	 * <p>
	 * Note that the getNextChannel methods do not update the tracker to reflect
	 * the recently requested channel, they simply return then next free channel.
	 * The tracker is only updated by calling the give/take bandwidth calls. The
	 * external caller should lock the entire model to prevent multiple threads
	 * from obtaining the same (free) channel !
	 */
	int getNextChannel(int rate, Map<SPF_KEYS, Object> parms) throws Exception;

	/**
	 * Return the next available channel starting at the given channel.
	 */
	int getNextChannel(String startingChannel, int rate,
	    Map<SPF_KEYS, Object> parms) throws Exception;

	/**
	 * Return the percentage utilisation (0-100) expressed as a double.
	 */
	double getUtilisation() throws Exception;

	/**
	 * Release or mark as free the given bandwidth (lambda, cross connect, vlan)
	 * associated with the following connection information. Returns true if
	 * sucessfull, false or exception if not. This method is generic to layer 0, 1
	 * and 2 the "cross connect" information provided needs to support all 3
	 * layers.
	 */
	boolean giveBandwidth(CrossConnection c) throws Exception;

	/**
	 * Set the constraints that admin user has provisioned on this
	 * facility/tracker
	 * 
	 * @param constraints
	 * @throws Exception
	 */
	void setConstraints(TrackerConstraints constraints) throws Exception;

	/**
	 * Attempt to take or mark as in use the given bandwidth (lambda, cross
	 * connect, vlan) associated with the following connection information.
	 * Returns true if sucessfull, false or exception if not. This method is
	 * generic to layer 0, 1 and 2 the "cross connect" information provided needs
	 * to support all 3 layers.
	 */
	boolean takeBandwidth(CrossConnection c) throws Exception;

	String toString();
}
