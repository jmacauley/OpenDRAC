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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;

import com.nortel.appcore.app.drac.common.types.InventoryXml;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class GetFacility extends AbstractCommandlet {
	private CplFacilityXml facilityXML;

	public GetFacility(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		/*
		 * Need to perform two queries in order to obtain all data that are needed.
		 * RTRV-ADJ:
		 * "ADJ-2-12-9::ADJTYPE=TX,PROVFEADDR=\"\",PADDRFORM=TID-SH-SL-PRT,DISCFEADDR=\""
		 * ,DADDRFORM=NULL,ADJSTAT=UNVERIFIED,CLFI=\"\":IS"
		 * "ADJ-2-12-10::ADJTYPE=RX,PROVFEADDR=\"\",PADDRFORM=TID-SH-SL-PRT,DISCFEADDR=\"\",DADDRFORM=NULL,ADJSTAT=UNVERIFIED,CLFI=\"\":IS"
		 * "ADJ-2-2-5::ADJTYPE=LINE,PROVFEADDR=\"\",PADDRFORM=NULL,DISCFEADDR=\"TOADMA-1-2-8\",DADDRFORM=TID-SH-SL-PRT,ADJSTAT=RELIABLE,CLFI=\"\":IS"
		 * RTRV-ADJ-TX:
		 * "ADJ-1-10-3::ADJTXTYPE=LH10GWTTFEC,ADJTXBIAS=0.00,CKTID=\"\",WAVELENGTH=1550.12,RATE=10.0G,ADJTXFEC=0.00,ADJTXMINPOW=-10.00,ADJTXMAXPOW=1.50,ADJTXCURPOW=-3.00,PAIREDRX=NO,DOCCARE=TRUE,ACTIVE=TRUE"
		 */
		try {
			String aCommand = (String) getParameters().get(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			getParameters().remove(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			NetworkElement ne = (NetworkElement) getParameters().get(
			    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			getParameters().remove(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);

			log.debug("CPL GetFacility: started for " + ne);

			// Get all LINE and TX first
			getAllAdj(ne);

			// Now get the extra attributes for those of TX type
			Map<String, String> paramList = new HashMap<String, String>();
			paramList.put(ObjectElement.OPERATION_KEY, aCommand);

			Iterator<String> anotherIr = getParameters().keySet().iterator();
			while (anotherIr.hasNext()) {
				String aParam = anotherIr.next();
				String aValue = (String) getParameters().get(aParam);
				paramList.put(aParam, aValue);
			}

			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				// For each record coming back from the NE, extract only the needed
				// attributes and place them in the HashMap for later updating the
				// database.
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();

					String aid = aResult.get("ADJAID");
					String wavelength = aResult.get("WAVELENGTH");
					if (wavelength != null) {
						wavelength = wavelength.replaceAll("\\.", "");
					}
					String active = aResult.get("ACTIVE");
					if (active != null) {
						active = active.toLowerCase();
					}

					HashMap<String, String> attributeList = new HashMap<String, String>();
					attributeList.put(AbstractFacilityXml.WAVELENGTH_ATTR, wavelength);
					attributeList.put(AbstractFacilityXml.ACTIVE_ATTR, active);

					String[] tempAid = aid.split("-");
					InventoryXml anInventory = ne.getInventory(tempAid[1], tempAid[2]);
					if (anInventory != null) {
						// Ask the FacilityXML to consider only the differences
						String mappedAid = anInventory.getComponentAid() + "-" + tempAid[1]
						    + "-" + tempAid[2] + "-" + tempAid[3];
						facilityXML.updateFacilityInstance(ne.getNeId(), mappedAid,
						    attributeList, false);

					}
					else {
						log.error("CPL GetFacility: Unexpected error: failed to map facility name for "
						    + aid);
					}
				}
			}
			// Now, put everything in the database
			log.debug("CPL GetFacility: Updating database with facility data for "
			    + ne.getNeName());
			facilityXML.updateDataBase(ne);
			return true;
		}
		catch (Exception e) {
			log.error("CPL GetFacility: Failed to populate facility:", e);
		}
		return false;
	}

	// This filters out those entries that are not "LINE" nor "TX";
	private void getAllAdj(NetworkElement ne) {
		try {
			Map<String, String> paramList = new HashMap<String, String>();
			paramList.put(ObjectElement.OPERATION_KEY,
			    Tl1CommandCode.RTRV_ADJ.toString());
			paramList.put("AID", "ALL");
			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				String currentData = DbUtility.INSTANCE.retrieveNeFacility(ne);
				if (currentData.length() > 0) {
					// There are data in the database
					ByteArrayInputStream tempData = new ByteArrayInputStream(
					    currentData.getBytes());
					SAXBuilder builder = new SAXBuilder();
					try {
						Document aDoc = builder.build(tempData);
						facilityXML = new CplFacilityXml(aDoc.getRootElement());

						// in case the database is old, just update it with the latest
						facilityXML.populateStaticAttributes();
					}
					catch (JDOMParseException je) {
						log.error("CPL GetFacility: " + currentData, je);
						return;
					}
				}
				else {
					// The NE is discover for the first time or the database doesn't
					// have any data associated with this NE
					facilityXML = new CplFacilityXml(ne);
				}
				if (ne.getCacheFacility() == null) {
					ne.setCacheFacility(new HashMap<String, Map<String, String>>());
				}

				// For each record coming back from the NE, extract only the needed
				// attributes.
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					
					String aid = aResult.get("ADJAID");
					ne.getCacheFacility().put(aid, aResult);
					String type = aResult.get("ADJTYPE");
					if (type.equalsIgnoreCase("LINE") || type.equalsIgnoreCase("TX")) {
						String state = aResult.get("PST");

						if (!state.equalsIgnoreCase("OOS-MA")) {
							HashMap<String, String> attributeList = new HashMap<String, String>();
							attributeList.put(AbstractFacilityXml.VALID_ATTR, "true");
							attributeList.put(AbstractFacilityXml.PRIMARYSTATE_ATTR, state);

							String[] tempAid = aid.split("-");
							InventoryXml anInventory = ne
							    .getInventory(tempAid[1], tempAid[2]);
							if (anInventory != null) {
								// Ask the FacilityXML to consider only the differences
								String mappedAid = anInventory.getComponentAid() + "-"
								    + tempAid[1] + "-" + tempAid[2] + "-" + tempAid[3];
								facilityXML.updateFacilityInstance(ne.getNeId(), mappedAid,
								    attributeList, true);
							}
							else {
								log.error("CPL GetFacility: Unexpected error: failed to map facility name for "
								    + aid);
							}
						}
						else {
							log.debug("CPL GetFacility: skipping this entry: " + aid);
						}

					}
				}
				return;
			}
		}
		catch (Exception e) {
			log.error("CPL GetFacility: Failed to populate facility:", e);
		}
	}
}
