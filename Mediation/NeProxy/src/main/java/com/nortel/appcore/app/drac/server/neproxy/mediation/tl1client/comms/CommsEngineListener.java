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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms;

/**
 * CommsEngineListener is the interface objects should implement to register
 * with comms engines and receive notification of suspends and resumes.
 * CommsEngines are suspended when another engine speaking a different protocol
 * needs exclusive use of a shared comm adapter.
 */
interface CommsEngineListener {
	/**
	 * The engine has resumed communication with the network. Objects sending
	 * polling messages can begin polling again. Objects relying on autonomous
	 * messages should refresh their data, which may be out of date since messages
	 * may have been ignored while the engine was suspended.
	 */
	void resumed();

	/**
	 * The engine has been suspended. Objects sending polling messages can stop,
	 * since these messages can't be sent to the network.
	 */
	void suspended();
}
