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

package com.nortel.appcore.app.drac.server.neproxy.command.cpl;

import java.util.HashMap;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.InventoryXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class ReptCrs extends AbstractCommandlet {
	// private static final int MODIFY = 0;
	private static final int ADD = 1;
	private static final int DELETE = 2;
	private static final String[] OPERATION_STR = { "modify", "add", "delete" };
	private Tl1XmlDbChangeEvent dbchgEvent;

	public ReptCrs(Map<String, Object> param) {
		super(param);
	}

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

		log.debug("CPL ReptCRS: Process REPT-CRS-OCH event: from NE " + ne
		    + " values " + values);

		// For some reason there is no DATE and TIME field in the REPT from the NE
		// String[] temp = ((String)values.get("DATETIME")).split("-");
		// dbchgEvent.setOccurrentDate(temp[0] + "-" + temp[1] + "-" + temp[2]);
		// dbchgEvent.setOccurrentTime(temp[3] + "-" + temp[4] + "-" + temp[5]);
		dbchgEvent.setOccurrentDate("N/A");
		dbchgEvent.setOccurrentTime("N/A");

		String neId = ne.getNeId();
		dbchgEvent.setEventId(neId + "_" + anEvent.getCtag());
		dbchgEvent.setOwnerId(ne.getTerminalId());
		dbchgEvent.updateDescription(values.get("operation"));

		String docCare = values.get("DOCCARE");
		int operation;
		if (docCare.equalsIgnoreCase("true")) {
			operation = ADD;
		}
		else {
			operation = DELETE;
		}
		data.put("operation", OPERATION_STR[operation]);
		String cktid = values.get("CKTID");
		if (cktid == null) {
			cktid = "N/A";
		}
		String fromAid = values.get("FROMAID");
		String toAid = values.get("TOAID");

		String[] aidMap = fromAid.split("-");
		String waveLength = aidMap[4];
		InventoryXml fromInventory = ne.getInventory(aidMap[1], aidMap[2]);
		String modifyFromAid = fromInventory.getComponentAid() + "-" + aidMap[1]
		    + "-" + aidMap[2] + "-" + aidMap[3] + "-" + waveLength;

		aidMap = toAid.split("-");
		InventoryXml toInventory = ne.getInventory(aidMap[1], aidMap[2]);
		String modifyToAid = toInventory.getComponentAid() + "-" + aidMap[1] + "-"
		    + aidMap[2] + "-" + aidMap[3] + "-" + aidMap[4];

		String existConnection = null;
		try {
			existConnection = DbUtility.INSTANCE.retrieveAXConnect(ne,
			    modifyFromAid, modifyToAid);
			if (existConnection == null) {
				existConnection = DbUtility.INSTANCE.retrieveAXConnect(ne,
				    modifyToAid, modifyFromAid);
				if (existConnection != null) {
					fromAid = modifyToAid;
					toAid = modifyFromAid;
				}
			}
		}
		catch (Exception e) {
			log.error("CPL ReptCRS: Failed to handle event REPT-CRS-OCH", e);
			return false;
		}

		aidMap = fromAid.split("-");
		fromInventory = ne.getInventory(aidMap[1], aidMap[2]);
		data.put("sShelf", aidMap[1]);
		data.put("sSlot", aidMap[2]);
		data.put("sPort", aidMap[3]);
		data.put("sChannel", waveLength);
		data.put("source", modifyFromAid);
		modifyFromAid = fromInventory.getComponentAid() + "-" + aidMap[1] + "-"
		    + aidMap[2] + "-" + aidMap[3] + "-" + waveLength;

		aidMap = toAid.split("-");
		toInventory = ne.getInventory(aidMap[1], aidMap[2]);
		modifyToAid = toInventory.getComponentAid() + "-" + aidMap[1] + "-"
		    + aidMap[2] + "-" + aidMap[3] + "-" + aidMap[4];
		data.put("tShelf", aidMap[1]);
		data.put("tSlot", aidMap[2]);
		data.put("tPort", aidMap[3]);
		data.put("tChannel", waveLength);
		data.put("target", modifyToAid);
		data.put("cktid", cktid);

		getParameters().put("replaceRate", "STS192C");
		getParameters().put("replaceSourceAddress", neId);
		getParameters().put("replaceTargetAddress", neId);
		getParameters().put("replaceId", cktid);
		getParameters().put("replaceType", "2WAY");
		getParameters().put("replaceCommitted", "true");
		getParameters().put("replaceSwmate", "");
		getParameters().put("replaceSourceAid", modifyFromAid);
		getParameters().put("replaceTargetAid", modifyToAid);

		log.debug("CPL ReptCRS: SHOULD SEND THIS: " + data);
		/*
		 * try { if (operation == ADD) { if (existConnection != null) {
		 * log.error(ne.getNeName() + ": connection " + fromAid + ":" + toAid +
		 * " already exists"); return false; } else {
		 * DbUtility.INSTANCE.addOneXConnect(ne, parameters); } } else { if
		 * (existConnection == null) { log.error(ne.getNeName() + ": connection " +
		 * fromAid + ":" + toAid + " doesn't exist"); return false; } else {
		 * DbUtility.INSTANCE.deleteXConnect(ne, modifyFromAid, modifyToAid); }
		 * } dbchgEvent.addDataElement(data); parameters.put(Commandlet.RESULT_KEY,
		 * dbchgEvent); return true; } catch (Exception e) {
		 * log.error("Failed to handle event REPT-CRS-OCH", e); }
		 */
		return false;
	}
}