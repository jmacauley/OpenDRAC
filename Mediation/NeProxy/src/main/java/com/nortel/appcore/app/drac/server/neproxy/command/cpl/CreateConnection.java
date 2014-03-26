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

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.InventoryXml;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;

public final class CreateConnection extends AbstractCommandlet {
	public CreateConnection(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		try {
			CrossConnection xcon = (CrossConnection) getParameters().get(
			    ClientMessageXml.CROSSCONNECTION_KEY);

			

			boolean returnCode = false;

			NetworkElement ne = (NetworkElement) DiscoverNePool.INSTANCE
			    .getNeByTidOrIdOrIpandPort(xcon.getSourceNeId());

			if (ne.getState().ordinal() < NeStatus.NE_ALIGNED.ordinal()) {
				getCandidate().setErrorCode("ERR_INRY");
				return false;
			}

			InventoryXml toInventory = null;
			InventoryXml fromInventory = null;

			String fromAid = xcon.getSourceXcAid();
			String toAid = xcon.getTargetXcAid();
			String cktId = xcon.getType();

			String fromShelf = fromAid.split("-")[1];
			String fromSlot = fromAid.split("-")[2];
			String fromPort = fromAid.split("-")[3];
			String fromWaveLength = fromAid.split("-")[4];
			fromInventory = ne.getInventory(fromShelf, fromSlot);
			if (fromInventory == null) {
				// Debuging a problem
				log.error("CPL CreateConnection: Unable to find shelf/slot  "
				    + fromShelf + " " + fromSlot + " from ne " + ne);
			}

			String toShelf = toAid.split("-")[1];
			String toSlot = toAid.split("-")[2];
			String toPort = toAid.split("-")[3];
			// String toWaveLength = toAid.split("-")[4];
			toInventory = ne.getInventory(toShelf, toSlot);
			if (toInventory == null) {
				// Debuging a problem
				log.error("CPL CreateConnection: Unable to find shelf/slot  " + toShelf
				    + " " + toSlot + " from ne " + ne);
			}

			Tl1CommandCode commandCode = null;
			HashMap<String, String> data = new HashMap<String, String>();

			if (ne.isBranchSite()) {
				commandCode = Tl1CommandCode.ENT_CRS_OCH;
				/*
				 * Since the AIDs are presented to the upper layers in the form of TX
				 * port, we have to replace either fromAid or toAid to the corresponding
				 * RX port. And since the LIM port are always (!!!) in paired of TX=5
				 * and RX=8, just pick 8 for RX
				 */
				String temp = "OCH-" + fromShelf + "-" + fromSlot + "-8-"
				    + fromWaveLength;
				data.put("FROMAID", temp);
				data.put("TOAID", toAid.replaceFirst("LIM", "OCH"));
				data.put("CCT", "2WAY");
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
				data.put("ACTIVE", "TRUE");
				data.put("CKTID", cktId);
			}
			log.debug("CPL CreateConnection: Add Connection data: " + commandCode
			    + ":" + data);
			/**
			 * fromAid = LIM and toAid = LIM, then it's a passthrough. This can only
			 * happen in BRANCH/ROADM node. Need to do ENT-CRS-OCH only when the path
			 * involves more than 1 domains. fromAid = CMD and toAid = LIM, then it's
			 * an add. This can happen in the TOADM or GOADM node. Need to do ED-ADJ
			 * on the TX fromAid = LIM and toAid = CMD, then it's a drop. This can
			 * happen in the TOADM or GOADM node. Don't need to do anything.
			 */

			boolean shouldSend = false;
			log.debug("CPL CreateConnection: ...validating "
			    + fromInventory.getComponentAid() + ":"
			    + toInventory.getComponentAid());
			if (fromInventory.getComponentAid().startsWith("LIM")) {
				if (toInventory.getComponentAid().startsWith("LIM")) {
					// Make sure the path involves more than 1 domains
					String fromOsid = ne.getShelves().get(fromInventory.getShelf());
					String toOsid = ne.getShelves().get(toInventory.getShelf());
					if (fromOsid == null || toOsid == null) {
						log.error("CPL CreateConnection: Unexpected OSID: "
						    + fromInventory.toString() + " and " + toInventory.toString());
						shouldSend = true;
					}
					else if (fromOsid.equalsIgnoreCase(toOsid)) {
						log.debug("CPL CreateConnection: Same domain: " + fromOsid);
						shouldSend = false;
						returnCode = true;
					}
					else if (new File("NO_CPL_OCH_ADD").exists()) {
						log.debug("Interdomain OCH connection add DISABLED...");
						log.debug("NE: " + ne + " COMMAND: " + commandCode + " DATA: "
						    + data);

						shouldSend = false;
						returnCode = true;
					}
					else {
						shouldSend = true;
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
					log.debug("CPL CreateConnection: Sending the Add Connection to the NE");
					response = ne.getTl1Session().sendSyncMessage(message);
					if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
						log.debug("CPL CreateConnection:Connection created");
						returnCode = true;
					}
					else {
						getCandidate().setErrorCode("ERR_SIOE");
						return false;
					}
				}
				catch (Exception e) {
					log.error("CPL CreateConnection: Failed in ENT_CRS", e);
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
			// eventTimer.schedule(new ConnectionEventTask(ne, parameters), 2000);
			// }
			return returnCode;
		}
		catch (RuntimeException re) {
			log.error("CPL CreateConnection: Caught ", re);
			throw re;
		}
	}
}
