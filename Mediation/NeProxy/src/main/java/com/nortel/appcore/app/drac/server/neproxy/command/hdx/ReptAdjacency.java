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
 * (Contributors insert name & email here)
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
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.server.neproxy.command.hdx;

import java.util.Map;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;

/**
 * @author nguyentd
 */
public final class ReptAdjacency extends AbstractCommandlet {
	public ReptAdjacency(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		// AbstractNe ne = (AbstractNe)
		// getParameters().get(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
		// ne.upDateLocalInfo();
		// Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
		// TL1AlarmEvent anEvent = (TL1AlarmEvent)
		// getParameters().get(AbstractNetworkElement.EVENTRECV_KEY);
		// Map<String, String> values = anEvent.getPayloads().get(0);
		// dbchgEvent.setReportType("adjacency");
		//
		// // DATETIME=2006-03-02-21-00-15
		// String dateTime = values.get("DATETIME");
		// if (dateTime != null)
		// {
		// dbchgEvent.setOccurrentDate(dateTime.substring(0, 10));
		// dbchgEvent.setOccurrentTime(dateTime.substring(11, dateTime.length()));
		// }
		// else
		// {
		// dbchgEvent.setOccurrentDate(values.get("DATETIME"));
		// }
		//
		// dbchgEvent.setEventId(ne.getNeId() + "_" + dateTime);
		// dbchgEvent.setOwnerId(ne.getTerminalId());
		// dbchgEvent.updateDescription(values.get("ACTION"));
		// String aid = values.get("AID");
		// String rxTag = values.get("RX_ACTUAL");
		// String[] rxArray = rxTag.split("_");
		// String txTag = values.get("TX_TAG");
		// String[] txArray = txTag.split("_");
		// String source = "";
		// String target = "";
		//
		// String[] aidArray = aid.split("-");
		// String sourceAid = "";
		// String targetAid = "";
		// Map<String, String> data = new HashMap<String, String>();
		// // Expecting the following format:
		// // AD_2_TX:TX:TX:TX:TX:TX_Shelf_000_Slot_000_Port_ME_1
		// // AD_1_aa:40:00:00:00:07_001_000_501_000_003
		// if (txArray.length >= 8)
		// {
		// source = txArray[2].replaceAll(":", "-");
		// sourceAid = aidArray[0] + "-" + Integer.parseInt(txArray[3]) + "-" +
		// Integer.parseInt(txArray[5]) +
		// "-"
		// + Integer.parseInt(txArray[7]);
		// }
		// if (rxArray.length >= 8)
		// {
		// target = rxArray[2].replaceAll(":", "-");
		// targetAid = aidArray[0] + "-" + Integer.parseInt(rxArray[3]) + "-" +
		// Integer.parseInt(rxArray[5]) +
		// "-"
		// + Integer.parseInt(rxArray[7]);
		// }
		// data.put("source", source);
		// data.put("sourceAid", sourceAid);
		// data.put("target", target);
		// data.put("targetAid", targetAid);
		// data.put("aid", aid);
		// dbchgEvent.addDataElement(data);
		//
		// getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);
		//
		// /*
		// * add the event into the Database try {
		// DbUtility.INSTANCE.addNewEvent(dbchgEvent.toString());
		// }
		// * catch (Exception e) { log.error("Fail to insert event " +
		// anEvent.getCommandCode(), e); }
		// */
		// try
		// {
		// if (DbUtility.INSTANCE.retrieveAAdjacency(ne, aid) != null)
		// {
		// DbUtility.INSTANCE.updateAdjacency(ne, data, aid);
		// }
		// else
		// {
		// ArrayList<String> temp = new ArrayList<String>();
		// temp
		// .add("<adjacencyInstance aid=\"" + aid + "\" " + "source=\"" + source +
		// "\" " + "sourceAid =\""
		// + sourceAid + "\" " + "target=\"" + target + "\" " + "targetAid =\"" +
		// targetAid
		// + "\"  manualProvision=\"false\" />");
		// DbUtility.INSTANCE.addNewAdjacency(ne, new ArrayList<String>(temp));
		// }
		// }
		// catch (Exception e)
		// {
		// }
		return true;
	}
}