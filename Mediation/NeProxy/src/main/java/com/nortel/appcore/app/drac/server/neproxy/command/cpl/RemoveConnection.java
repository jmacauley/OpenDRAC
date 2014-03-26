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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.InventoryXml;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;

public final class RemoveConnection extends AbstractCommandlet {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	public RemoveConnection(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		CrossConnection xcon = (CrossConnection) getParameters().get(
		    ClientMessageXml.CROSSCONNECTION_KEY);

		

		boolean returnCode = true;

		NetworkElement ne = (NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(xcon.getSourceNeId());

		log.debug("CPL RemoveConnection: Invoked for NE " + ne);
		if (ne.getState().ordinal() < NeStatus.NE_ALIGNED.ordinal()) {
			log.debug("CPL RemoveConnection: NE not in a valid state, skipping");
			getCandidate().setErrorCode("ERR_INRY");
			return false;
		}
		InventoryXml toInventory = null;
		InventoryXml fromInventory = null;

		String fromXcAid = xcon.getSourceXcAid();
		String toXcAid = xcon.getTargetXcAid();
		// String cktId = xcon.getId();

		String fromShelf = fromXcAid.split("-")[1];
		String fromSlot = fromXcAid.split("-")[2];
		String fromPort = fromXcAid.split("-")[3];
		String fromWaveLength = fromXcAid.split("-")[4];
		fromInventory = ne.getInventory(fromShelf, fromSlot);

		String toShelf = toXcAid.split("-")[1];
		String toSlot = toXcAid.split("-")[2];
		String toPort = toXcAid.split("-")[3];
		// String toWaveLength = toAid.split("-")[4];
		toInventory = ne.getInventory(toShelf, toSlot);

		// see "GG 2009-02-03"
		String callId = xcon.getCallid();

		Tl1CommandCode commandCode = null;
		Map<String, String> data = new HashMap<String, String>();

		if (ne.isBranchSite()) {

			commandCode = Tl1CommandCode.DLT_CRS_OCH;
			/*
			 * Since the AIDs are presented to the upper layers in the form of TX
			 * port, we have to replace either fromAid or toAid to the corresponding
			 * RX port. And since the LIM port are always (!!!) in paired of TX=5 and
			 * RX=8, just pick 8 for RX
			 */
			String temp = "OCH-" + fromShelf + "-" + fromSlot + "-8-"
			    + fromWaveLength;
			data.put("FROMAID", temp);
			data.put("TOAID", toXcAid.replaceFirst("LIM", "OCH"));
			log.debug("CPL RemoveConnection: Ne is a branch site using " + commandCode
			    + " " + data);
		}
		else {
			commandCode = Tl1CommandCode.ED_ADJ_TX;
			String tempAid;
			if (fromInventory.getComponentAid().startsWith("LIM")) {
				tempAid = "ADJ-" + toShelf + "-" + toSlot + "-" + toPort;
			}
			else {
				tempAid = "ADJ-" + fromShelf + "-" + fromSlot + "-" + fromPort;
			}
			data.put("AID", tempAid);
			data.put("ACTIVE", "FALSE");
			data.put("CTKID", "");
			log.debug("CPL RemoveConnection: Not a branch site, using " + commandCode
			    + " " + data);
		}

		/**
		 * fromAid = LIM and toAid = LIM, then it's a pass-through. This can only
		 * happen in BRANCH/ROADM node. Need to do ENT-CRS-OCH fromAid = CMD and
		 * toAid = LIM, then it's an add. This can happen in the TOADM or GOADM
		 * node. Need to do ED-ADJ on the TX fromAid = LIM and toAid = CMD, then
		 * it's a drop. This can happen in the TOADM or GOADM node. Don't need to do
		 * anything.
		 */

		boolean shouldSend = false;

		log.debug("CPL RemoveConnection: fromInventory " + fromInventory
		    + " toInventory " + toInventory);

		if (fromInventory.getComponentAid().startsWith("LIM")) {
			log.debug("CPL RemoveConnection: aid starts with LIM "
			    + fromInventory.getComponentAid());

			if (toInventory.getComponentAid().startsWith("LIM")) {
				// Make sure the path involves more than 1 domains
				String fromOsid = ne.getShelves().get(fromInventory.getShelf());
				String toOsid = ne.getShelves().get(toInventory.getShelf());
				if (fromOsid == null || toOsid == null) {
					log.error("CPL RemoveConnection: Unexpected OSID: "
					    + fromInventory.toString() + " and " + toInventory.toString());
					shouldSend = true;
				}
				else if (fromOsid.equalsIgnoreCase(toOsid)) {
					log.debug("CPL RemoveConnection: Same domain: " + fromOsid);
					shouldSend = false;
				}
				else if (new File("NO_CPL_OCH_DELETE").exists()) {
					log.debug("Interdomain OCH connection deletion DISABLED...");
					log.debug("NE: " + ne + " COMMAND: " + commandCode + " DATA: " + data);

					shouldSend = false;
				}
				else {
					/*
					 * At this point, we know that it the cross connect between 2 domains
					 * so, we have to delay the request
					 */
					log.debug("CPL RemoveConnection: cross domain, defering to DeleteConnectionTask ");
					DeleteConnectionTask aTask = new DeleteConnectionTask(ne,
					    new TL1RequestMessage(commandCode, data), buildEvent(ne, xcon),
					    fromXcAid, toXcAid, callId);

					log.debug("CPL RemoveConnection: enQueueCommand DeleteConnectionTask "
					    + fromWaveLength);
					ne.enQueueCommand(fromWaveLength, aTask);
					return true;
				}

			}
			else if (toInventory.getComponentAid().startsWith("CMD")) {
				shouldSend = true;
			}
		}
		else if (fromInventory.getComponentAid().startsWith("CMD")) {
			if (toInventory.getComponentAid().startsWith("LIM")) {
				shouldSend = true;
			}
		}

		if (shouldSend) {
			TL1RequestMessage message = new TL1RequestMessage(commandCode, data);
			TL1ResponseMessage response = null;
			try {
				log.debug("CPL RemoveConnection: Sending the Delete Connection to the NE");
				response = ne.getTl1Session().sendSyncMessage(message);
				if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
					log.debug("CPL RemoveConnection: Connection deleted");
				}
				else {
					log.error("CPL RemoveConnection: Failure deleting connection "
					    + response.toString());
					getCandidate().setErrorCode("ERR_SIOE");
					return false;
				}
			}
			catch (Exception e) {
				log.error("CPL RemoveConnection: Failed in DLT_CRS", e);
				String errorCode = e.getMessage().split(":")[0];
				getCandidate().setErrorCode(errorCode);
				return false;
			}
		}
		// if (returnCode) {
		/**
		 * At this time, there is no mechanism to map a certain NE reports to the
		 * connection events, so we generate our own
		 */
		// Timer eventTimer = new Timer();
		// eventTimer.schedule(new ConnectionEventTask(ne, buildEvent(ne,
		// parameters), fromAid, toAid), 2000);
		// }
		return returnCode;
	}

	private Tl1XmlDbChangeEvent buildEvent(NetworkElement ne, CrossConnection xcon) {
		Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
		dbchgEvent.updateNeInfo(ne.getNeStatus().getStateString());
		String timeDate = new java.sql.Timestamp(System.currentTimeMillis())
		    .toString();
		dbchgEvent.setOccurrentDate(timeDate.substring(2, 10));
		dbchgEvent.setOccurrentTime(timeDate.substring(11, 19));
		dbchgEvent.setEventId(ne.getNeId() + "_" + "1111");
		dbchgEvent.setReportType("connection");
		dbchgEvent.setOwnerId(ne.getTerminalId());
		Map<String, String> data = new HashMap<String, String>();

		String cktid = xcon.getId();
		// String neId = ne.getNeId();
		// String neIp = ne.getIpAddress();

		String fromXcAid = xcon.getSourceXcAid();
		String toXcAid = xcon.getTargetXcAid();

		String[] aidMap = fromXcAid.split("-");

		data.put(AbstractFacilityXml.RATE_ATTR, "STS192C");
		data.put("sShelf", aidMap[1]);
		data.put("sSlot", aidMap[2]);
		data.put("sPort", aidMap[3]);
		data.put("sChannel", aidMap[4]);

		aidMap = toXcAid.split("-");
		data.put("tShelf", aidMap[1]);
		data.put("tSlot", aidMap[2]);
		data.put("tPort", aidMap[3]);
		data.put("tChannel", aidMap[4]);

		data.put("cktid", cktid);
		data.put("source", fromXcAid);

		data.put("target", toXcAid);
		data.put("operation", "DELETE");
		dbchgEvent.addDataElement(data);
		return dbchgEvent;
	}
}
