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
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.InventoryXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class ReptDbchg extends AbstractCommandlet {
	// private static final int MODIFY = 0;
	private static final int ADD = 1;
	private static final int DELETE = 2;
	private static final String[] OPERATION_STR = { "modify", "add", "delete" };
	private Tl1XmlDbChangeEvent dbchgEvent;

	public ReptDbchg(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		/**
		 * "DBCHGSEQ=100006919,DATE=07-09-13,TIME=15-13-39,USERID=ADMIN,SOURCE=2,
		 * PRIORITY=GEN_TL1_CMD:ED-ADJ-TX:ADJ-1-10-9::,,,,,,,ACTIVE=TRUE:"
		 */
		boolean result = false;
		NetworkElement ne = (NetworkElement) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
		ne.upDateLocalInfo();
		dbchgEvent = new Tl1XmlDbChangeEvent(ne);
		TL1AlarmEvent anEvent = (TL1AlarmEvent) getParameters().get(
		    AbstractNetworkElement.EVENTRECV_KEY);
		Map<String, String> values = anEvent.getPayloads().get(0);

		log.debug("CPL ReptDbChg: Process DBCHG event: from " + ne + " " + values);
		dbchgEvent.setOccurrentDate(values.get("DATE"));
		dbchgEvent.setOccurrentTime(values.get("TIME"));

		String neId = ne.getNeId();
		dbchgEvent.setEventId(neId + "_" + values.get("DBCHGSEQ"));
		dbchgEvent.setOwnerId(ne.getTerminalId());
		String command = values.get("COMMAND");
		dbchgEvent.updateDescription(values.get("PRIOR"));

		/*
		 * Only support "ED-ADJ-TX" for now
		 */
		if (command.indexOf("ED-ADJ-TX") >= 0) {
			dbchgEvent.setReportType("connection");
			log.debug("CPL ReptDbChg: handling " + command);
			result = handleEditAdj(ne, values);
		}
		else {
			
		}
		return result;
	}

	private String[] getSwitchTopo(NetworkElement ne, String aid) {
		List<Map<String, String>> result;
		Map<String, String> paramList = new HashMap<String, String>();
		paramList.put(ObjectElement.OPERATION_KEY,
		    Tl1CommandCode.RTRV_ADJ_TX.toString());
		paramList.put("AID", aid);
		try {
			result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				Map<String, String> aResult = result.get(0);
				String waveLength = aResult.get("WAVELENGTH");
				if (waveLength != null) {
					waveLength = waveLength.replaceAll("\\.", "");
				}

				// Now use the waveLength info to get the PORT trail in the given shelf
				String[] temp = aid.split("-");
				String toShelf = temp[1];
				String lookForSlotPort = temp[2] + "-" + temp[3];
				paramList.clear();
				paramList.put(ObjectElement.OPERATION_KEY,
				    Tl1CommandCode.RTRV_TOPO_SWT.toString());
				paramList.put("AID", "SWT-" + toShelf + "-" + waveLength);
				result = ne.getTl1Session().sendToNE(paramList);
				if (result != null) {
					for (Map<String, String> map : result) {
						String[] portTrail = map.get("PORTTRAIL").split(",");
						log.debug("CPL ReptDbChg: Try to match " + lookForSlotPort
						    + " with " + map.get("PORTTRAIL"));
						if (portTrail[0].equals(lookForSlotPort)) {
							String toSlot = portTrail[portTrail.length - 1].split("-")[0];
							String toPort = portTrail[portTrail.length - 1].split("-")[1];
							InventoryXml toInventory = ne.getInventory(temp[1], toSlot);
							String toAid = toInventory.getComponentAid() + "-" + toShelf
							    + "-" + toSlot + "-" + toPort + "-" + waveLength;
							String[] returnValue = new String[3];
							returnValue[0] = toAid;
							returnValue[1] = map.get("CKTID");
							returnValue[2] = waveLength;
							return returnValue;
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.error("CPL ReptDbChg: Failed to query the Adjacency", e);
		}
		return null;
	}

	/**
	 * A connection needs to have the fromAid and toAid attributes but the event
	 * only provides the fromAid. So, we have to walk through the PORT Trail to
	 * find out the end which is used as the toAid.
	 */
	private boolean handleEditAdj(NetworkElement ne, Map<String, String> values) {
		String fromAid = values.get("AID1");

		HashMap<String, String> data = new HashMap<String, String>();

		String activeFlag = values.get("ACTIVE");

		if (activeFlag != null && activeFlag.length() > 0) {
			if (activeFlag.equalsIgnoreCase("TRUE")) {
				data.put("operation", OPERATION_STR[ADD]);
			}
			else {
				data.put("operation", OPERATION_STR[DELETE]);
			}
			// Build the fromAid
			String[] aidMap = fromAid.split("-");
			String fromShelf = aidMap[1];
			String fromSlot = aidMap[2];
			String fromPort = aidMap[3];
			InventoryXml fromInventory = ne.getInventory(fromShelf, fromSlot);
			String modifyFromAid = fromInventory.getComponentAid() + "-" + fromShelf
			    + "-" + fromSlot + "-" + fromPort;

			String toAid;
			String waveLength;
			String ckTid = "";

			// Get the toAid
			String[] buildData = this.getSwitchTopo(ne, fromAid);
			if (buildData != null) {
				toAid = buildData[0];
				ckTid = buildData[1];
				waveLength = buildData[2];

				fromAid = modifyFromAid + "-" + waveLength;

				aidMap = toAid.split("-");
				String toShelf = aidMap[1];
				String toSlot = aidMap[2];
				String toPort = aidMap[3];

				getParameters().put("replaceRate", "STS192C");
				data.put(AbstractFacilityXml.RATE_ATTR, "STS192C");

				data.put("sShelf", fromShelf);
				data.put("sSlot", fromSlot);
				data.put("sPort", fromPort);
				data.put("sChannel", waveLength);
				data.put("tShelf", toShelf);
				data.put("tSlot", toSlot);
				data.put("tPort", toPort);
				data.put("tChannel", waveLength);
				data.put("cktid", ckTid);
				data.put("source", fromAid);
				data.put("target", toAid);

				getParameters().put("replaceSourceAddress", ne.getNeId());
				getParameters().put("replaceTargetAddress", ne.getNeId());
				getParameters().put("replaceId", ckTid);
				getParameters().put("replaceType", "2WAY");
				getParameters().put("replaceCommitted", "true");
				getParameters().put("replaceSwmate", "");
				getParameters().put("replaceSourceAid", fromAid);
				getParameters().put("replaceTargetAid", toAid);

				dbchgEvent.addDataElement(data);
				getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);
				try {
					String existConnection = DbUtility.INSTANCE.retrieveAXConnect(
					    ne, fromAid, toAid);
					if (existConnection != null) {
						if (activeFlag.equalsIgnoreCase("TRUE")) {
							log.error("CPL ReptDbChg: " + ne.getNeName() + ": connection "
							    + fromAid + ":" + toAid + " already exists");
							return false;
						}
					}
					else if (!activeFlag.equalsIgnoreCase("TRUE")) {
						log.error("CPL ReptDbChg: " + ne.getNeName() + ": connection "
						    + fromAid + ":" + toAid + " doesn't exist");
						log.debug("CPL ReptDbChg: Trying the switched values: " + toAid
						    + " to" + fromAid);
						existConnection = DbUtility.INSTANCE.retrieveAXConnect(ne,
						    toAid, fromAid);
						if (existConnection == null) {
							log.error("CPL ReptDbChg: " + ne.getNeName() + ": connection "
							    + toAid + ":" + fromAid + " doesn't exist");
							return false;
						}
					}
					if (activeFlag.equalsIgnoreCase("TRUE")) {
						DbUtility.INSTANCE.addOneXConnect(ne, getParameters());
					}
					else {
						DbUtility.INSTANCE.deleteXConnect(ne, fromAid, toAid);
					}
					return true;
				}
				catch (Exception e) {
					log.error("CPL ReptDbChg: Failed to handle the ED-ADj-TX event", e);
				}
			}
			else {
				log.error("CPL ReptDbChg: Failed to build the toAid");
			}
		}

		return false;
	}
}