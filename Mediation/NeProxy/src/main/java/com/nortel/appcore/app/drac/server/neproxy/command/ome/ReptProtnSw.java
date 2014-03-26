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

package com.nortel.appcore.app.drac.server.neproxy.command.ome;

import java.util.Map;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlProtnSwEvent;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * This is a simple class to handle REPT^PROTNSW TL1 autonomous events.  At the
 * moment it does nothing except log the fact an event was received.  This
 * class can be expanded in the future if OpenDRAC needs to use protection
 * switch data for operation.
 *
 * @author hacksaw
 */
public final class ReptProtnSw extends AbstractCommandlet {

	private Tl1XmlProtnSwEvent protnswEvent;

	public ReptProtnSw(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() throws Exception {
		AbstractNe ne = (AbstractNe) getParameters().get(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
		ne.upDateLocalInfo();

		TL1AlarmEvent anEvent = (TL1AlarmEvent) getParameters().get(
		    AbstractNetworkElement.EVENTRECV_KEY);

		Map<String, String> values = anEvent.getPayloads().get(0);

        String neId = ne.getNeId();

        log.info("ReptProtnSw: Protection switch event for NE " + ne.getNeInfo().getNetworkElementName() + " " + values);

        /* Process event - not completed at the moment. */
        protnswEvent = new Tl1XmlProtnSwEvent(ne);
		protnswEvent.setOccurrentDate(values.get("DATE"));
		protnswEvent.setOccurrentTime(values.get("TIME"));

        /* Return false so no additional processing will be performed on the
         * event.
         */
		return false;
	}
}
