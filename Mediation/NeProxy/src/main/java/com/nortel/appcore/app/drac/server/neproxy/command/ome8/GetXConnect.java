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

package com.nortel.appcore.app.drac.server.neproxy.command.ome8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * @author nguyentd
 */
public final class GetXConnect extends AbstractCommandlet {
	public GetXConnect(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		// <edge id="Connection 1" type="2WAY" source="aa-00-00-00-00-10"
		// sourceAid="OC192-1-5-1"
		// target="aa-00-00-00-00-09" targetAid="OC192-1-6-1"/>
		try {
			String aCommand = (String) getParameters().get(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			getParameters().remove(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			NetworkElement ne = (NetworkElement) getParameters().get(
			    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			getParameters().remove(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);

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
			String neIp = ne.getIpAddress();
			Map<String, String> cacheFacility = new HashMap<String, String>();
			Set<String> cacheConnection = new HashSet<String>();
			if (result != null) {
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					
					String fromAid = aResult.get("FROMAID");
					String toAid = aResult.get("TOAID");

					String[] fromAidArray = fromAid.split("-");
					String rate = fromAidArray[0];

					String shelfSlotPort = fromAidArray[1] + "-" + fromAidArray[2] + "-"
					    + fromAidArray[3];
					String aFacility = null;
					aFacility = cacheFacility.get(shelfSlotPort);
					if (aFacility == null) {
						String facilityRecord = DbUtility.INSTANCE.retrieveAFacility(
						    ne, fromAidArray[1], fromAidArray[2], fromAidArray[3]);
						if (facilityRecord == null) {
							// can't find the Facility, so skip this connection
							log.debug("Skip this connection:" + neIp + " - fromAid=" + fromAid);
							continue;
						}

						int startTypeIndex = facilityRecord.indexOf("type=") + 6;
						int endTypeIndex = facilityRecord.indexOf('"', startTypeIndex);
						aFacility = facilityRecord.substring(startTypeIndex, endTypeIndex);
						cacheFacility.put(shelfSlotPort, aFacility);
					}

					StringBuilder temp = new StringBuilder(aFacility);
					for (int i = 1; i < fromAidArray.length; i++) {
						temp.append("-" + fromAidArray[i]);
					}
					fromAid = temp.toString();

					String[] toAidArray = toAid.split("-");
					shelfSlotPort = toAidArray[1] + "-" + toAidArray[2] + "-"
					    + toAidArray[3];
					aFacility = cacheFacility.get(shelfSlotPort);
					if (aFacility == null) {
						String facilityRecord = DbUtility.INSTANCE.retrieveAFacility(
						    ne, toAidArray[1], toAidArray[2], toAidArray[3]);
						if (facilityRecord == null) {
							// can't find the Facility, so skip this connection
							log.debug("Skip this connection: " + neIp + " - " + fromAid
							    + " to " + toAid);
							continue;
						}
						int startTypeIndex = facilityRecord.indexOf("type=") + 6;
						int endTypeIndex = facilityRecord.indexOf('"', startTypeIndex);
						aFacility = facilityRecord.substring(startTypeIndex, endTypeIndex);
						cacheFacility.put(shelfSlotPort, aFacility);
					}
					temp = new StringBuilder(aFacility);
					for (int i = 1; i < toAidArray.length; i++) {
						temp.append("-" + toAidArray[i]);
					}
					toAid = temp.toString();

					if (cacheConnection.contains(fromAid + ":" + toAid)) {
						log.error(ne.getNeName() + ": connection " + fromAid + ":" + toAid
						    + " already exists");
					}
					else {
						cacheConnection.add(fromAid + ":" + toAid);

						// 1+1
						String swMateAid = aResult.get("SWMATE");
						if (swMateAid != null && swMateAid.length() > 0) {

							String[] swMateAidArray = swMateAid.split("-");
							shelfSlotPort = swMateAidArray[1] + "-" + swMateAidArray[2] + "-"
							    + swMateAidArray[3];
							aFacility = cacheFacility.get(shelfSlotPort);
							if (aFacility == null) {
								String facilityRecord = DbUtility.INSTANCE
								    .retrieveAFacility(ne, swMateAidArray[1],
								        swMateAidArray[2], swMateAidArray[3]);

								if (facilityRecord == null) {
									// can't find the Facility, so skip this connection
									log.debug("Skip this connection:" + neIp + " - swMateAid="
									    + swMateAid);
									continue;
								}

								int startTypeIndex = facilityRecord.indexOf("type=") + 6;
								int endTypeIndex = facilityRecord.indexOf('"', startTypeIndex);
								aFacility = facilityRecord.substring(startTypeIndex,
								    endTypeIndex);
								cacheFacility.put(shelfSlotPort, aFacility);
							}

							temp = new StringBuilder(aFacility);
							for (int i = 1; i < swMateAidArray.length; i++) {
								temp.append("-" + swMateAidArray[i]);
							}
							swMateAid = temp.toString();

							data.add(

							"<edge" + " id=\""
							    + aResult.get("CKTID").replaceAll("&", " and ")
							    + "\" type=\"" + aResult.get("CCT") + "\" rate=\"" + rate

							    + "\" source=\"" + neId + "\" sShelf=\"" + fromAidArray[1]
							    + "\" sSlot=\"" + fromAidArray[2] + "\" sSubslot=\"" + 0
							    + "\" sPort=\"" + fromAidArray[3] + "\" sChannel=\""
							    + fromAidArray[4] + "\" sourceAid=\"" + fromAid

							    + "\" target=\"" + neId + "\" tShelf=\"" + toAidArray[1]
							    + "\" tSlot=\"" + toAidArray[2] + "\" tSubslot=\"" + 0
							    + "\" tPort=\"" + toAidArray[3] + "\" tChannel=\""
							    + toAidArray[4] + "\" targetAid=\"" + toAid

							    + "\" swmateNeid=\"" + neId + "\" swmateShelf=\""
							    + swMateAidArray[1] + "\" swmateSlot=\"" + swMateAidArray[2]
							    + "\" swmateSubslot=\"" + 0 + "\" swmatePort=\""
							    + swMateAidArray[3] + "\" swmateChannel=\""
							    + swMateAidArray[4] + "\" swmateAid=\"" + swMateAid

							    + "\" committed=\"true\" />");
						}
						else {
							data.add(

							"<edge" + " id=\""
							    + aResult.get("CKTID").replaceAll("&", " and ")
							    + "\" type=\"" + aResult.get("CCT") + "\" rate=\"" + rate

							    + "\" source=\"" + neId + "\" sShelf=\"" + fromAidArray[1]
							    + "\" sSlot=\"" + fromAidArray[2] + "\" sSubslot=\"" + 0
							    + "\" sPort=\"" + fromAidArray[3] + "\" sChannel=\""
							    + fromAidArray[4] + "\" sourceAid=\"" + fromAid

							    + "\" target=\"" + neId + "\" tShelf=\"" + toAidArray[1]
							    + "\" tSlot=\"" + toAidArray[2] + "\" tSubslot=\"" + 0
							    + "\" tPort=\"" + toAidArray[3] + "\" tChannel=\""
							    + toAidArray[4] + "\" targetAid=\"" + toAid

							    + "\" committed=\"true\" />");

						}

					}
				}
			}
			DbUtility.INSTANCE.addNewXConnect(ne, data);
			return true;
		}
		catch (Exception e) {
			log.error("Failed to get xConnections", e);
		}
		return false;

	}
}
