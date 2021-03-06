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

package com.nortel.appcore.app.drac.server.neproxy.command.ome5;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * @author nguyentd
 */
public final class GetVCEFacility extends AbstractCommandlet {
	public GetVCEFacility(Map<String, Object> param) {
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
				for (Map<String, String> aResult : result) {
					// String vceAid = aResult.get("AID");
					HashMap<String, String> vceAttrList = new HashMap<String, String>();
					vceAttrList.put(AbstractFacilityXml.VALID_ATTR, "true");

					/*
					 * for (Map.Entry<String, String> e :
					 * OmeFacilityXML.ethNeToXmlMapping.entrySet()) { String aValue =
					 * aResult.get(e.getKey()); if (aValue != null) { aValue =
					 * aValue.replaceAll("&", "/"); ethAttrList.put(e.getValue(), aValue);
					 * } }
					 */

					// Not normalized yet:
					vceAttrList.putAll(aResult);

					
				}

			}
			return true;

		}
		catch (Exception e) {
			log.error("Failed to retrieve VCE facility", e);
		}
		return false;
	}

}
