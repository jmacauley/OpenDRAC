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
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class GetXConnect extends AbstractCommandlet {
	private final Map<String, String[]> cacheTopology = new HashMap<String, String[]>();

	public GetXConnect(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		try {
			String aCommand = (String) getParameters().get(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			getParameters().remove(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			NetworkElement ne = (NetworkElement) getParameters().get(
			    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			getParameters().remove(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);

			log.debug("CPL getXConnect: Getting xConnections for NE " + ne);

			Map<String, String> paramList = new HashMap<String, String>();
			paramList.put(ObjectElement.OPERATION_KEY, aCommand);

			Iterator<String> anotherIr = getParameters().keySet().iterator();
			while (anotherIr.hasNext()) {
				String aParam = anotherIr.next();
				String aValue = (String) getParameters().get(aParam);
				paramList.put(aParam, aValue);
			}

			DbUtility.INSTANCE.deleteAllXConnection(ne);
			List<String> data = new ArrayList<String>();
			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
			String neId = ne.getNeId();
			// String neIp = ne.getIpAddress();

			if (result != null) {
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					String activeFlag = aResult.get("ACTIVE");
					if (activeFlag == null || activeFlag.equalsIgnoreCase("FALSE")) {
						continue;
					}
					String routing = aResult.get("ROUTING");
					if (routing != null && !routing.equalsIgnoreCase("UNKNOWN")) {
						boolean entryValid = false;
						String fromAid;
						String fromShelf;
						String fromSlot;
						String fromPort;
						String toAid;
						String toShelf;
						String toSlot;
						String toPort;
						InventoryXml toInventory = null;
						InventoryXml fromInventory;
						String aid = aResult.get("SWTAID");
						String waveLength = aid.split("-")[2];
						fromShelf = aid.split("-")[1];
						String portTrial[] = aResult.get("PORTTRAIL").split(",");
						fromSlot = portTrial[0].split("-")[0];
						fromPort = portTrial[0].split("-")[1];
						fromInventory = ne.getInventory(fromShelf, fromSlot);
						fromAid = fromInventory.getComponentAid() + "-" + fromShelf + "-"
						    + fromSlot + "-" + fromPort + "-" + waveLength;

						toShelf = fromShelf;
						toSlot = portTrial[portTrial.length - 1].split("-")[0];
						toPort = portTrial[portTrial.length - 1].split("-")[1];
						toInventory = ne.getInventory(fromShelf, toSlot);
						toAid = toInventory.getComponentAid() + "-" + toShelf + "-"
						    + toSlot + "-" + toPort + "-" + waveLength;

						if (routing.equalsIgnoreCase("ADD")) {
							/*
							 * this should be from UNI to NNI. It's possible that the
							 * connection can expand more than one shelves, so use the cache
							 * Adjacencies to figure out the intra connection.
							 */
							if (!toInventory.getComponentAid().startsWith("LIM")) {
								String pathId = aResult.get("PATHID");
								Map<String, String> aFacility = ne.getCacheFacility().get(
								    "ADJ-" + fromShelf + "-" + toSlot + "-" + toPort);
								// PROVFEADDR=\"BRANCH-3-3-11\"
								String farEnd = aFacility.get("PROVFEADDR");
								toShelf = farEnd.split("-")[1];
								String[] tempTrail = this.getSwitchTopo(toShelf, waveLength,
								    pathId, result);
								if (tempTrail == null) {
									log.error("CPL getXConnect: Skip this entry: " + aid);
									continue;
								}

								toSlot = tempTrail[tempTrail.length - 1].split("-")[0];
								toPort = tempTrail[tempTrail.length - 1].split("-")[1];
								toInventory = ne.getInventory(toShelf, toSlot);
								toAid = toInventory.getComponentAid() + "-" + toShelf + "-"
								    + toSlot + "-" + toPort + "-" + waveLength;
							}
							entryValid = true;

						}
						else if (routing.equalsIgnoreCase("PASSTHROUGH")) {
							/*
							 * There are 2 entries of the same wavelength (i.e 2WAY) for each
							 * domain, so maximum of 4 entries in the Y- or T-Branch site.
							 * Also, we only support LIM-LIM connection only.
							 */
							if (fromInventory.getComponentAid().startsWith("LIM")) {
								/*
								 * need to adjust the port because the ADJ on CPL is
								 * unidirectional and on the "out" port.
								 */
								String pathId = aResult.get("PATHID");
								if ("8".equals(fromPort)) {
									fromPort = "5";
									fromAid = fromInventory.getComponentAid() + "-" + fromShelf
									    + "-" + fromSlot + "-" + fromPort + "-" + waveLength;
								}
								Map<String, String> aFacility = ne.getCacheFacility().get(
								    "ADJ-" + fromShelf + "-" + toSlot + "-" + toPort);
								// PROVFEADDR=\"BRANCH-3-3-11\"
								String farEnd = aFacility.get("PROVFEADDR");
								toShelf = farEnd.split("-")[1];
								String[] tempTrail = this.getSwitchTopo(toShelf, waveLength,
								    pathId, result);
								if (tempTrail == null) {
									log.error("CPL getXConnect: Skip this entry: " + aid);
									continue;
								}

								toSlot = tempTrail[tempTrail.length - 1].split("-")[0];
								toPort = tempTrail[tempTrail.length - 1].split("-")[1];

								toInventory = ne.getInventory(toShelf, toSlot);
								toAid = toInventory.getComponentAid() + "-" + toShelf + "-"
								    + toSlot + "-" + toPort + "-" + waveLength;
								entryValid = true;
							}
						}
						else if (routing.equalsIgnoreCase("DROP")) {
							/*
							 * this should be from NNI to UNI but we're assuming all
							 * connections are 2WAY, so there must be a corresponding DROP for
							 * every ADD; thus, ignore the DROP
							 */
						}
						if (entryValid) {
							cacheTopology.put(aid, portTrial);
							String newConnection = "<edge" + " id=\""
							    + aResult.get("CKTID").replaceAll("&", " and ")
							    + "\" type=\"2WAY" + "\" rate=\"STS192C" + "\" source=\""
							    + neId +

							    "\" sShelf=\"" + fromShelf + "\" sSlot=\"" + fromSlot
							    + "\" sSubslot=\"" + 0 + "\" sPort=\"" + fromPort
							    + "\" sChannel=\"" + waveLength + "\" target=\"" + neId
							    + "\" sourceAid=\"" + fromAid +

							    "\" tShelf=\"" + toShelf + "\" tSlot=\"" + toSlot
							    + "\" tSubslot=\"" + 0 + "\" tPort=\"" + toPort
							    + "\" tChannel=\"" + waveLength + "\" targetAid=\"" + toAid
							    + "\" swmate=\"" + "\" committed=\"true\" />";
							
							data.add(newConnection);
						}
					}
				}
				log.debug("CPL getXConnect: Storing XConnects in database for " + ne
				    + " " + data);

				DbUtility.INSTANCE.addNewXConnect(ne, data);
				return true;
			}
		}
		catch (Exception e) {
			log.error("CPL getXConnect: Failed to get xConnections", e);
		}
		return false;
	}

	private String[] getSwitchTopo(String shelf, String waveLength,
	    String pathId, List<Map<String, String>> result) {
		String temp = "SWT" + "-" + shelf + "-" + waveLength;
		Iterator<Map<String, String>> resultIr = result.iterator();
		while (resultIr.hasNext()) {
			Map<String, String> aResult = resultIr.next();
			String aid = aResult.get("SWTAID");
			String tempPathId = aResult.get("PATHID");
			if (aid.equals(temp)) {
				// remove the entry from the list so that the caller doesn't to process
				// it
				// result.remove(aResult);
				if (tempPathId.equals(pathId)) {
					return aResult.get("PORTTRAIL").split(",");
				}
			}
		}
		log.error("CPL getXConnect: Couldn't find this SWITCH TOPO: " + temp);
		return null;
	}
}
