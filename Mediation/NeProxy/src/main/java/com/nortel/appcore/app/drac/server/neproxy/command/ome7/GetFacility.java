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

package com.nortel.appcore.app.drac.server.neproxy.command.ome7;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;

import com.nortel.appcore.app.drac.common.types.EquipmentXml;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * @author nguyentd
 */
public final class GetFacility extends AbstractCommandlet {
	public GetFacility(Map<String, Object> param) {
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

			Iterator<String> anotherIr = getParameters().keySet().iterator();
			while (anotherIr.hasNext()) {
				String aParam = anotherIr.next();
				String aValue = (String) getParameters().get(aParam);
				paramList.put(aParam, aValue);
			}

			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				OmeFacilityXML facilityXML = null;
				String currentData = DbUtility.INSTANCE.retrieveNeFacility(ne);
				if (currentData.length() > 0) {
					ByteArrayInputStream tempData = new ByteArrayInputStream(
					    currentData.getBytes());
					SAXBuilder builder = new SAXBuilder();
					try {
						Document aDoc = builder.build(tempData);

						facilityXML = new OmeFacilityXML(aDoc.getRootElement());

						// in case the database is old, just update it with the latest
						facilityXML.populateStaticAttributes();
					}
					catch (JDOMParseException je) {
						log.error(currentData, je);
						return false;
					}
				}
				else {
					facilityXML = new OmeFacilityXML(ne);
				}
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					String aid = aResult.get("AID");
					HashMap<String, String> attributeList = new HashMap<String, String>();
					String state = aResult.get("pst");
					if (state == null) {
						state = aResult.get("PST");
					}
					attributeList.put(AbstractFacilityXml.PRIMARYSTATE_ATTR, state);
					attributeList.put(AbstractFacilityXml.VALID_ATTR, "true");

					// Add equipment info for Admin Console details
					String[] aidArr = aid.split("-");
					String shelf = aidArr[1];
					String slot = aidArr[2];
					String port = aidArr[3];
					EquipmentXml.addEqptAttributes(attributeList,
					    ne.getCard(shelf, slot), ne.getPort(shelf, slot, port));

					facilityXML.updateFacilityInstance(ne.getNeId(), aid, attributeList);
				}
				facilityXML.updateDataBase(ne);
			}
			return true;
		}
		catch (Exception e) {
			log.error("Failed to populate facility:", e);
		}
		return false;
	}
}
