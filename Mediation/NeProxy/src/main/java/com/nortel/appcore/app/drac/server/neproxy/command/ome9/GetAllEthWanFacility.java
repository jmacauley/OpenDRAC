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

package com.nortel.appcore.app.drac.server.neproxy.command.ome9;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.EquipmentXml;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

public final class GetAllEthWanFacility extends AbstractCommandlet {
	private NetworkElement ne;
	private List<Map<String, String>> ethResults = new ArrayList<Map<String, String>>();
	private List<Map<String, String>> eth10GResults = new ArrayList<Map<String, String>>();
	private List<Map<String, String>> wanResults = new ArrayList<Map<String, String>>();
	private Map<String, Map<String, String>> sspMap = new HashMap<String, Map<String, String>>();
	private OmeFacilityXML facilityXML = null;

	public GetAllEthWanFacility(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		try {
			ne = (NetworkElement) getParameters().get(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			getParameters().remove(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);

			// 1GE ETH
			getFac("RTRV-ETH", "ETH-1-ALL", ethResults);

			// 10G ETH
			getFac("RTRV-ETH10G", "ETH10G-1-ALL", eth10GResults);

			// FE
			getFac("RTRV-ETH100", "ETH100-1-ALL", eth10GResults);

			getFac("RTRV-WAN", "WAN-1-ALL", wanResults);

			// First, get the existing data from the Database
			String currentData = DbUtility.INSTANCE.retrieveNeFacility(ne);
			if (currentData.length() > 0) {
				ByteArrayInputStream tempData = new ByteArrayInputStream(
				    currentData.getBytes());
				SAXBuilder builder = new SAXBuilder();
				Document aDoc = builder.build(tempData);
				facilityXML = new OmeFacilityXML(aDoc.getRootElement());
			}
			else {
				facilityXML = new OmeFacilityXML(ne);
			}

			for (Map<String, String> aResult : ethResults) {
				processEthTypes(aResult);
			}

			for (Map<String, String> aResult : eth10GResults) {
				processEthTypes(aResult);
			}

			for (Map<String, String> aResult : wanResults) {
				processWan(aResult);
			}

			// Now, update or add
			for (Map<String, String> facAttrs : sspMap.values()) {
				String type = facAttrs.get("TYPE");

				if (Facility.isWan(type)) {
					facilityXML.updateWanFacility(ne.getNeId(), facAttrs.get("AID"),
					    facAttrs);
				}
				else if (Facility.isEth(type)) {
					facilityXML.updateEthFacility(ne.getNeId(), facAttrs.get("AID"),
					    facAttrs);
				}
				else {
					log.error("GetAllEthWanFacilities: unexpected facility type " + type
					    + " for aid " + facAttrs.get("AID"));
				}
			}

			facilityXML.updateDataBase(ne);

		}
		catch (Exception e) {
			log.error("Failed to retrieve facility", e);
		}

		return false;
	}

	private void getFac(String command, String rtrvAid,
	    List<Map<String, String>> facResults) throws Exception {
		Map<String, String> paramList = new HashMap<String, String>();
		paramList.put(ObjectElement.OPERATION_KEY, command);
		paramList.put("AID", rtrvAid);

		for (Map.Entry<String, Object> m : getParameters().entrySet()) {
			paramList.put(m.getKey(), m.getValue().toString());
		}

		List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
		if (result != null) {
			facResults.addAll(result);
		}
	}

	private void processEthTypes(Map<String, String> aResult) throws Exception {
		String aid = aResult.get("AID");
		String[] aidArr = aid.split("-");
		String shelf = aidArr[1];
		String slot = aidArr[2];
		String port = aidArr[3];
		String ssp = shelf + "-" + slot + "-" + port;

		HashMap<String, String> ethAttrList = new HashMap<String, String>();
		ethAttrList.put(AbstractFacilityXml.VALID_ATTR, "true");
		ethAttrList.put("AID", aid);
		ethAttrList.put("TYPE", aidArr[0]);

		for (Map.Entry<String, String> e : OmeFacilityXML.ethNeToXmlMapping
		    .entrySet()) {
			String aValue = aResult.get(e.getKey());
			if (aValue != null) {
				aValue = aValue.replaceAll("&", "/");
				ethAttrList.put(e.getValue(), aValue);
			}
		}

		// Flag for L2SS-based facilities: ETH on discovery
		if (ne.slotIsL2SS(shelf, slot)) {
			ethAttrList.put(FacilityConstants.IS_L2SS_FACILITY, "true");
		}

		// Add equipment info for Admin Console details
		EquipmentXml.addEqptAttributes(ethAttrList, ne.getCard(shelf, slot),
		    ne.getPort(shelf, slot, port));

		sspMap.put(ssp, ethAttrList);
	}

	private void processWan(Map<String, String> aResult) throws Exception {
		String aid = aResult.get("AID");
		String[] aidArr = aid.split("-");
		String shelf = aidArr[1];
		String slot = aidArr[2];
		String port = aidArr[3];
		String ssp = shelf + "-" + slot + "-" + port;

		Map<String, String> wanAttrList = new HashMap<String, String>();
		wanAttrList.put(AbstractFacilityXml.VALID_ATTR, "true");
		wanAttrList.put("AID", aid);
		wanAttrList.put("TYPE", aidArr[0]);
		for (Map.Entry<String, String> e : OmeFacilityXML.wanNeToXmlMapping
		    .entrySet()) {
			String aValue = aResult.get(e.getKey());
			if (aValue != null) {
				aValue = aValue.replaceAll("&", "/");
				wanAttrList.put(e.getValue(), aValue);
			}
		}
		// DRAC FLAG: for L2SS-based facilities: WAN on discovery
		if (ne.slotIsL2SS(shelf, slot)) {
			wanAttrList.put(FacilityConstants.IS_L2SS_FACILITY, "true");
		}

		Map<String, String> forEqptUpdate = wanAttrList;

		Map<String, String> ethAttrList = sspMap.get(ssp);
		if (ethAttrList != null) {
			// *************** Modeling Change **************************************
			// Here, we 'merge' the ETH and WAN (both belonging to the same EPL port)
			// The facility that we'll track is that of the ETH (because it is that
			// which is a selected UNI). But there are WAN attributes
			// (in the context of the ETH) that must be tracked

			// DRAC FLAG: to indicate this ETH is actually a WAN-backed ETH
			ethAttrList.put(FacilityConstants.IS_EPL, "true");

			// Now, get the attributes of interest
			Map<String, String> keyMap = OmeFacilityXML.wanToEthEPLNeToXmlMapping;
			Iterator<String> keyIr = keyMap.keySet().iterator();
			while (keyIr.hasNext()) {
				String neKey = keyIr.next();
				String xmlKey = keyMap.get(neKey);

				// The wan attrs have already been mapped from nekeys to xml keys
				String aValue = wanAttrList.get(xmlKey);
				if (aValue != null) {
					ethAttrList.put(xmlKey, aValue);
				}
			}

			forEqptUpdate = ethAttrList;
		}
		else {
			// standalone WAN
			sspMap.put(ssp, wanAttrList);
		}

		// Add equipment info for Admin Console details
		EquipmentXml.addEqptAttributes(forEqptUpdate, ne.getCard(shelf, slot),
		    ne.getPort(shelf, slot, port));
	}

}
