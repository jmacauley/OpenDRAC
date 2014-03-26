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

package com.nortel.appcore.app.drac.server.neproxy.command.hdx;

import java.util.HashMap;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;
import com.nortel.appcore.app.drac.server.neproxy.util.CheckAllComplete;

public final class ReptCrs extends AbstractCommandlet {
	// private static final int MODIFY = 0;
	private static final int ADD = 1;
	private static final int DELETE = 2;
	private static final String[] OPERATION_STR = { "modify", "add", "delete" };
	private Tl1XmlDbChangeEvent dbchgEvent;

	public ReptCrs(Map<String, Object> param) {
		super(param);
	}

	/*
	 * processCompoundFields: command=REPT-CRS-PLD,data={fromSignalNumber=1,
	 * toSubSlotId=0, astnCallType=SPC, CCT=2WAY, direction=Bidir,
	 * toSignalNumber=1, fromSlotId=502, AST=LOCKED, toPortId=1, toShelfId=1,
	 * fromSubSlotId=0, RATE=STS1, toEntityType=OC192, fromEntityType=OC192,
	 * FROMAID=OC192-1-502-0-2-1-34, ASTNDATA=1101800001020304010000e4010000e3,
	 * astnHeadEndNEId=aa-40-00-00-00-01, toPayloadNumber=34, fromPortId=2,
	 * DATETIME=2006-06-03-18-00-18, operation=REPT-CRS-PLD,
	 * astnCallKey=CALL-aa400000000127796cfe00e4, PRIME=ASTN,
	 * fromPayloadNumber=34, toSlotId=501,
	 * ASTNCONNID=aa400000000127796cfe00e427796cff00e4,
	 * TOAID=OC192-1-501-0-1-1-34, fromShelfId=1,
	 * uniqueKey=OC192-1-502-0-2-1-34OC192-1-501-0-1-1-34, DISOWN=IDLE,
	 * CONNID=228, ACTION=DEL, neId=aa-40-00-00-00-02, SST=ACT, LABEL=}
	 */
	@Override
	public boolean start() {
		AbstractNe ne = (AbstractNe) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
		Map<String, String> data = new HashMap<String, String>();
		ne.upDateLocalInfo();
		dbchgEvent = new Tl1XmlDbChangeEvent(ne);
		dbchgEvent.setReportType("connection");
		TL1AlarmEvent anEvent = (TL1AlarmEvent) getParameters().get(
		    AbstractNetworkElement.EVENTRECV_KEY);
		Map<String, String> values = anEvent.getPayloads().get(0);

		String[] temp = values.get("DATETIME").split("-");
		dbchgEvent.setOccurrentDate(temp[0] + "-" + temp[1] + "-" + temp[2]);
		dbchgEvent.setOccurrentTime(temp[3] + "-" + temp[4] + "-" + temp[5]);

		String neId = ne.getNeId();
		dbchgEvent.setEventId(neId + "_" + anEvent.getCtag());
		dbchgEvent.setOwnerId(ne.getTerminalId());
		dbchgEvent.updateDescription(values.get("operation"));

		String rate = values.get("RATE");
		if (ne.getNeMode().toString().equalsIgnoreCase("sdh")) {
			String mapRate = AbstractNetworkElement.getSdhToSonetMap().get(rate);
			log.debug("Converting rate: " + rate + " to: " + mapRate);
			rate = mapRate;
		}
		else {
			// The HDX uses this format "STS-3C" is is not the same as OME, so
			// remove the "-"
			rate = rate.replaceAll("-", "");
		}

		getParameters().put("replaceRate", rate);
		data.put("rate", rate);

		String command = values.get("ACTION");
		String cktid = values.get("LABEL");
		if (cktid == null) {
			cktid = "N/A";
		}
		// Notify the internal utility that keeps track of all Xconnections
		// being created, not deleted
		if (command.startsWith("ADD")) {
			CheckAllComplete.INSTANCE.iAmDone(cktid);
		}

		String swMate = values.get("SWMATE") == null ? "" : (String) values
		    .get("SWMATE");
		log.debug("Process DBCHG event: " + values);
		getParameters().put("replaceSourceAddress", neId);
		getParameters().put("replaceTargetAddress", neId);
		getParameters().put("replaceId", cktid);
		getParameters().put("replaceType", values.get("CCT"));
		getParameters().put("replaceCommitted", "true");
		getParameters().put("replaceSwmate", swMate);

		data.put("swmate", swMate);

		String fromAid = values.get("FROMAID");
		String toAid = values.get("TOAID");
		data.put("cktid", cktid);
		data.put("source", fromAid);
		getParameters().put("replaceSourceAid", fromAid);

		// OC192-1-501-0-1-1-34
		String[] aidMap = fromAid.split("-");
		data.put("sShelf", aidMap[1]);
		data.put("sSlot", aidMap[2]);
		data.put("sPort", aidMap[4]);
		data.put("sChannel", aidMap[6]);

		data.put("target", toAid);
		getParameters().put("replaceTargetAid", toAid);
		aidMap = toAid.split("-");
		data.put("tShelf", aidMap[1]);
		data.put("tSlot", aidMap[2]);
		data.put("tPort", aidMap[4]);
		data.put("tChannel", aidMap[6]);

		// Update the database and re-use the "parameters"
		try {
			if (command.startsWith("ADD")) {
				data.put("operation", OPERATION_STR[ADD]);
				DbUtility.INSTANCE.addOneXConnect(ne, getParameters());
			}
			else {
				data.put("operation", OPERATION_STR[DELETE]);
				DbUtility.INSTANCE.deleteXConnect(ne, fromAid, toAid);
			}
			dbchgEvent.addDataElement(data);
			// DbUtility.INSTANCE.addNewEvent(dbchgEvent.eventNodeToString());
		}
		catch (Exception e) {
			log.error("Failed to handle event", e);
			return false;
		}
		getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);
		return true;
	}
}
