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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.InventoryXml;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class GetInventory extends AbstractCommandlet {
	public GetInventory(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		// "LIM-2-2::CTYPE=\"CPL EDFA Module 2 (MLA)\",
		// PEC=NTT830BA,REL= 12
		// ,CLEI=WMANUX7GAB,SER=NNTMHG016XY2,MDAT=2007-13,AGE=00-007-00-36,ONSC=00-006-23-34"
		try {

			String aCommand = (String) getParameters().get(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			getParameters().remove(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			NetworkElement ne = (NetworkElement) getParameters().get(
			    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			getParameters().remove(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);

			log.debug("CPL GetInventory: Starting for ne " + ne);

			Map<String, String> paramList = new HashMap<String, String>();
			paramList.put(ObjectElement.OPERATION_KEY, aCommand);

			Iterator<String> anotherIr = getParameters().keySet().iterator();
			while (anotherIr.hasNext()) {
				String aParam = anotherIr.next();
				String aValue = (String) getParameters().get(aParam);
				paramList.put(aParam, aValue);
			}

			// ArrayList<String> data = new ArrayList<String>();
			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					try {
						String[] aid = aResult.get("AID").split("-");
						String type = aResult.get("CTYPE");
						String pecCode = aResult.get("PEC");
						InventoryXml anInventory = new InventoryXml(aid[0], type, pecCode,
						    aid[1], aid[2]);
						ne.getInventory().add(anInventory);
					}
					catch (Exception e) {
						log.error("CPL GetInventory: Failed to parse this inventory: "
						    + aResult, e);
					}
				}
			}
			return true;
		}
		catch (Exception e) {
			log.error("CPL GetInventory: Fail to process the NE Inventory", e);
		}
		return false;
	}

}
