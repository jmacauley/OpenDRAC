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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1;

/**
 * ResponseListener defines a protocol that allows receipt of Response objects.
 * </p></p> Objects sending a command through the TL1 engine may specify a
 * ResponseListener. This listener is stored in the engine's registry. When an
 * output response message is received that corresponds to the sent command, the
 * engine builds a Response object and notifies the listener with
 * #received(Response). </p></p> A timeout period may also be specified when
 * sending a command. If a complete response is not received, and no
 * acknowledgments or response messages are received for the specified duration,
 * the listener is notified with #timedOut(Response). </p></p>
 */
public interface ResponseListener {

	/** in the case that anything comes back this method is called */
	void received(Response response);

	/**
	 * Handle the specified Response object, which represents a timeout. A timeout
	 * period was specified along with this listener when the listener's command
	 * was sent. That period expired without any messages from the server, before
	 * all response messages were received.
	 * 
	 * @see Response
	 * @see TL1Engine
	 */
	void timedOut(Response response);
}
