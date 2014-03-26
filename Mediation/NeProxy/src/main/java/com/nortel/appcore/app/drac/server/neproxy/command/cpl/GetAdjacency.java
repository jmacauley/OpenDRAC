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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.InventoryXml;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class GetAdjacency extends AbstractCommandlet {
	// private static final String CPL_TX_LIM_PORT = "5";
	private static final String CPL_RX_LIM_PORT = "8";

	public GetAdjacency(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		/*
		 * RTRV-ADJ:
		 * "ADJ-2-12-9::ADJTYPE=TX,PROVFEADDR=\"\",PADDRFORM=TID-SH-SL-PRT,DISCFEADDR=\""
		 * ,DADDRFORM=NULL ,ADJSTAT=UNVERIFIED,CLFI=\"\":IS"
		 * "ADJ-2-12-10::ADJTYPE=RX,PROVFEADDR=\"\",PADDRFORM=TID-SH-SL-PRT,DISCFEADDR=\"\",DADDRFORM=NULL,ADJSTAT=UNVERIFIED,CLFI=\"\":IS"
		 * "ADJ-2-2-5::ADJTYPE=LINE,PROVFEADDR=\"\",PADDRFORM=NULL,DISCFEADDR=\"TOADMA-1-2-8\",DADDRFORM=TID-SH-SL-PRT,ADJSTAT=RELIABLE,CLFI=\"\":IS"
		 * RTRV-ADJ-LINE: "ADJ-1-2-5::FIBERTYPE=NDSF"
		 */
		try {
			// TODO: Adjust NeDescription to simply retrieve the full RTRV-ADJ
			// String aCommand = (String)
			// getParameters().get(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			String aCommand = Tl1CommandCode.RTRV_ADJ.toString();

			getParameters().remove(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			NetworkElement ne = (NetworkElement) getParameters().get(
			    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			getParameters().remove(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);

			log.debug("CPL GetAdjacency: Starting for ne " + ne);

			// CPL reports/models links unidirectionally: transmit from LIM port 5,
			// receive on LIM port 8.
			// Here this is reconciled with a bidirectional model by translating port
			// 8 to port 5 (to permit
			// link consolidation, facility lookups, etc.

			Map<String, String> paramList = new HashMap<String, String>();
			paramList.put(ObjectElement.OPERATION_KEY, aCommand);
			paramList.put("AID", "ALL");

			Iterator<String> anotherIr = getParameters().keySet().iterator();
			while (anotherIr.hasNext()) {
				String aParam = anotherIr.next();
				String aValue = (String) getParameters().get(aParam);
				paramList.put(aParam, aValue);
			}

			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);

			if (result != null) {
				List<NetworkElementAdjacency> list = new ArrayList<NetworkElementAdjacency>();

				// For each record coming back from the NE, extract only the needed
				// attributes.
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> m = resultIr.next();

					// Only interested in LINE adjacencies
					if ("LINE".equals(m.get("ADJTYPE"))) {

						String srcAid = m.get("ADJAID");
						// Some inspection required to obtain the port AID type. The port
						// AID is used in LPCP_PORT
						// to cross reference between adjacencies, edges, and facilities.
						String[] tempSrcAid = srcAid.split("-");
						String portLocation = srcAid.substring(srcAid.indexOf("-"));
						InventoryXml anInventory = ne.getInventory(tempSrcAid[1],
						    tempSrcAid[2]);
						srcAid = anInventory.getComponentAid() + portLocation;

						// Transmit tag is not given explicitly. Some construction required.
						// And while we do this, we have to apply further changes to
						// accomodate that CPL
						// topology reports unidirectionally: tx on port 5, rx on port 8. In
						// order
						// to allow links to consolidate, we'll adjust the tx side port
						// number.
						StringBuilder modifiedPortLocation = new StringBuilder();
						modifiedPortLocation.append("-");
						for (int i = 1; i < tempSrcAid.length - 1; i++) {
							modifiedPortLocation.append(tempSrcAid[i] + "-");
						}
						modifiedPortLocation.append(CPL_RX_LIM_PORT);
						final String txTag = ne.getNeName() + modifiedPortLocation.toString();
						
						// DISCFEADDR=ASD002A-CPL2P-11-2-8						
						list.add(new NetworkElementAdjacency(ne.getNeId(), srcAid, txTag, m.get("DISCFEADDR"), m.get("ADJTYPE"), false));
					}
				}

				// Best effort add of list:
				DbNetworkElementAdjacency.INSTANCE.add(list);
			}

			return true;
		}
		catch (Exception e) {
			log.error("CPL GetAdjacency: Failed to populate facility:", e);
		}
		return false;
	}

}
