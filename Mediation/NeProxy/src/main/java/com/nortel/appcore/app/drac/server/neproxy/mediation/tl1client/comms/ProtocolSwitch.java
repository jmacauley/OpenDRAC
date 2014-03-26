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
 * This interface is implemented by components(FTP, ZModemClient, Telnet etc,.)
 * to allow their clients to switch between different protocols that they use.
 */

public interface ProtocolSwitch {
	/**
	 * Sets the protocol to be the active protocol from then on. Clients
	 * implementing this method will require this method call to be issued first
	 * so that they carry out their functionality.
	 */

	void startProtocol();

	/**
	 * Resets this protocol so that it is no longer the active protocol.
	 * Susbequent method calls to carry out any functionalties of the protocol
	 * could result in an exception
	 */

	void stopProtocol();
} // End interface ProtocolSwitch
