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

import java.util.BitSet;

/**
 * Methods and constants common to all Sonet/Sdh trackers
 */

public interface SonetTrackerI extends BasicTracker {

	int STS1 = 0;
	int STS3C = 1;
	int STS6C = 2;
	int STS12C = 3;
	int STS24C = 4;
	int STS48C = 5;
	int STS192C = 6;

	int STM1 = 0;
	int STM2 = 1;
	int STM4 = 2;
	int STM8 = 3;
	int STM16 = 4;
	int STM64 = 5;

	int VC3 = 6;
	int VC4 = 7;
	int VC4_2C = 8;
	int VC4_4C = 9;
	int VC4_8C = 10;
	int VC4_16C = 11;
	int VC4_64C = 12;

	/**
	 * Return a new INSTANCE of this tracker with free bandwidth.
	 */
	SonetTrackerI getBlankCopy() throws Exception;

	/**
	 * Return a bitset that represents the state of this tracker, for each STS1 in
	 * use, the corresponding bit is set.
	 */
	BitSet getInternalTracker() throws Exception;

	boolean isBoundaryChannel(int channel, int sizeIdx) throws Exception;

	/**
	 * Return true if the bandwidth is contiguous.
	 */
	boolean isContiguousUsage() throws Exception;

}
