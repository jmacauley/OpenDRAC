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

package com.nortel.appcore.app.drac.server.neproxy.command.ome7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * @author nguyentd
 */
public final class ReptAdjacency extends AbstractCommandlet {
	public ReptAdjacency(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		AbstractNe ne = (AbstractNe) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
		ne.upDateLocalInfo();
		TL1AlarmEvent anEvent = (TL1AlarmEvent) getParameters().get(
		    AbstractNetworkElement.EVENTRECV_KEY);

		Map<String, String> values = anEvent.getPayloads().get(0);
		String port = values.get("AID");
		String txTag = values.get("TX_TAG");
		String rxTag = values.get("RXACTUAL");
		String layer = values.get("LAYR");

		Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
		dbchgEvent.setReportType("adjacency");

		dbchgEvent.setEventId(Long.toString(System.currentTimeMillis()));

		dbchgEvent.setOwnerId(ne.getTerminalId());
		dbchgEvent.updateDescription(values.get("ACTION"));

		final NetworkElementAdjacency adj = new NetworkElementAdjacency(ne.getNeId(),
		    port, txTag, rxTag, layer, false);
		// adj.setNeid(ne.getNeId());
		// adj.setPort(port);
		// adj.setTxTag(txTag);
		// adj.setRxTag(rxTag);
		// adj.setType(layer);
		// adj.setManual(false);

		try {
			// DATABASE updates to reflect actual network state
			if ("ADD".equals(values.get("ACTION"))) {
				List<NetworkElementAdjacency> adjList = new ArrayList<NetworkElementAdjacency>();
				adjList.add(adj);
				DbNetworkElementAdjacency.INSTANCE.add(adjList);

			}
			else if ("DELETE".equals(values.get("ACTION"))) {
				DbNetworkElementAdjacency.INSTANCE.delete(adj.getNeid(),
				    adj.getPort(), adj.getType());
			}

			else if ("UPDATE".equals(values.get("ACTION"))) {
				DbNetworkElementAdjacency.INSTANCE.update(adj);
			}

			// EVENTS to send northbound to the network model
			// If comms are coming up one end at a time, we'll see a partially empty
			// add, followed later (when the other end is done) by a full
			// update. For the network model, we want to fashion things such that
			// only complete adjacencies are seen as ADD.

			Map<String, String> eventData = new HashMap<String, String>();
			boolean sendEvent = false;

			// The AO event stream from the NE in various scenarios are somewhat
			// unexpected (in terms of
			// what is filled in). e.g. delete doesn't fill in the far end

			String rxReliability = values.get("RX_RELIABILITY");

			if ("ADD".equals(values.get("ACTION"))) {
				if (txTag != null && txTag.length() > 0 && rxTag != null
				    && rxTag.length() > 0 && rxReliability != null
				    && rxReliability.equals("RELIABLE")) {
					sendEvent = true;
				}
			}

			else if ("DELETE".equals(values.get("ACTION"))) {
				sendEvent = true;
			}

			else if ("UPDATE".equals(values.get("ACTION"))) {
				if (txTag != null && txTag.length() > 0 && rxTag != null
				    && rxTag.length() > 0 && rxReliability != null
				    && rxReliability.equals("RELIABLE")) {
					sendEvent = true;
				}
				else {
					sendEvent = false;
				}
			}

			dbchgEvent.addDataElement(eventData);

			getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);

			return sendEvent;
		}

		catch (Exception e) {
			log.error("Fail to update Adjacency DB: " + adj, e);
			return false;
		}
	}
}
