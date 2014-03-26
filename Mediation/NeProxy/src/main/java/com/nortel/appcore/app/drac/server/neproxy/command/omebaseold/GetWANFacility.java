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

package com.nortel.appcore.app.drac.server.neproxy.command.omebaseold;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;

public final class GetWANFacility extends AbstractCommandlet {
	public GetWANFacility(Map<String, Object> param) {
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

			Map<String, String> paramList = new HashMap<String, String>();
			paramList.put(ObjectElement.OPERATION_KEY, aCommand);

			for (Map.Entry<String, Object> m : getParameters().entrySet()) {
				paramList.put(m.getKey(), m.getValue().toString());
			}

			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				// First, get the existing data from the Database
				OmeFacilityXML facilityXML = null;
				String currentData = DbUtility.INSTANCE.retrieveNeFacility(ne);
				if (currentData.length() > 0) {
					ByteArrayInputStream tempData = new ByteArrayInputStream(
					    currentData.getBytes());
					SAXBuilder builder = new SAXBuilder();
					try {
						Document aDoc = builder.build(tempData);
						facilityXML = new OmeFacilityXML(aDoc.getRootElement());
					}
					catch (JDOMParseException je) {
						log.error(currentData, je);
						return false;
					}
				}
				else {
					facilityXML = new OmeFacilityXML(ne);
				}

				/*
				 * now, go through each facility retrieved from the NE and compared it
				 * with the database copy
				 */
				for (Map<String, String> aResult : result) {
					String wanAid = aResult.get("AID");
					String[] wanAidMap = wanAid.split("-");

					HashMap<String, String> wanAttrList = new HashMap<String, String>();
					wanAttrList.put(AbstractFacilityXml.VALID_ATTR, "true");

					for (Map.Entry<String, String> e : OmeFacilityXML.wanNeToXmlMapping
					    .entrySet()) {
						String aValue = aResult.get(e.getKey());
						if (aValue != null) {
							aValue = aValue.replaceAll("&", "/");
							wanAttrList.put(e.getValue(), aValue);
						}
					}

					// GGL2 flag for L2SS-based facilities: WAN on discovery
					if (ne.slotIsL2SS(wanAidMap[1], wanAidMap[2])) {
						wanAttrList.put(FacilityConstants.IS_L2SS_FACILITY, "true");
					}

					facilityXML.updateWanFacility(ne.getNeId(), wanAid, wanAttrList);
				}

				// GGL2 Removed the implicit layer1/layer2 relationship here
				// (i.e. where it would toss any WAN that didn't have an ETH
				// in the same 'hardcoded' AID position)

				facilityXML.updateDataBase(ne);
			}
			return true;
		}
		catch (Exception e) {
			log.error("Failed to retrieve WAN facility", e);
		}
		return false;
	}

}
