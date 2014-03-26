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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.AbstractCommAdapter;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.AbstractCommsEngine;

/**
 * LanguageEngine is the abstract base class for Site Manager's OS-NE language
 * layers. Site Manager applications request data and actions from NEs via text
 * message exchanges of a specific format. Site Manager currently supports only
 * one format, TL1, but may support others in the future (CLUI, SNMP). </p></p>
 * Since Site Manager currently includes only one concrete language engine
 * (TL1Engine), LanguageEngine does not include very much generalized behaviour.
 * When and if further language engines are implemented, more common behaviour
 * may become apparent, and will be refactored into LanguageEngine at that time.
 * </p></p> LanguageEngine briefly had some behaviour relating to protocol
 * switching and server message flow control. However, the protocol switching
 * behaviour was better placed at a higher level in CommsEngine. </p></p>
 * Message flow control behaviour was also removed because it turned out that it
 * was never actually used. In practice, Site Manager never queues up enough
 * client-to-server messages to make that kind of queue necessary. Further,
 * server-to-client messages can't be practically detected as flow controlled -
 * if a message is overwritten in the comms buffer, the drop will manifest as a
 * timeout. There's currently no clear way of inferring server-to- client
 * message drop is due to this kind of flow control.</p></p> You're probably
 * wondering to yourself: "Self, why the heck is this class here?". "Well", I'd
 * reply, "First you should probably stop talking to yourself, and secondly the
 * comms package makes use of this abstraction. Taking it out would be more
 * trouble than it's worth."
 */
public abstract class AbstractLanguageEngine extends AbstractCommsEngine {
	public AbstractLanguageEngine(AbstractCommAdapter adapter) {
		super(adapter);
	}
}
