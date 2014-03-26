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
 * ReportListener defines a protocol that allows receipt of Report objects.
 * </p></p> Objects may want notification when the TL1 engine receives certain
 * TL1 autonomous messages. These objects should implement this interface and
 * register with the TL1 engine, specifying the tid (target identifier) and
 * output code (autonomous message type) in which they are interested. When the
 * engine receives an autonomous message with that tid and code, it will create
 * a Report object and send it to the listener via #received(Report). </p></p>
 */
public interface ReportListener {
	/**
	 * Handle the specified Report object, which represents the receipt of an
	 * autonmous message. The report object was built by the TL1 engine after
	 * parsing an autonomous event that had the same target identifier (tid) and
	 * output code that was specified when the listener was registered.
	 */
	void received(Report report);
}
