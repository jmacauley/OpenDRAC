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

package com.nortel.appcore.app.drac.server.neproxy.command.hdx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * Created on Jan 12, 2006
 * 
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
			ArrayList<String> data = new ArrayList<String>();
			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();

					String neId = ne.getNeId();
					String fromAid = aResult.get("FROMAID");
					String[] fromAidArray = fromAid.split("-");
					String toAid = aResult.get("TOAID");
					String[] toAidArray = toAid.split("-");
					// OC192-1-502-0-1-1-1
					String rate = aResult.get("RATE");
					if (ne.getNeMode().toString().equalsIgnoreCase("sdh")) {
						String mapRate = AbstractNetworkElement.getSdhToSonetMap()
						    .get(rate);
						log.debug("Converting rate: " + rate + " to: " + mapRate);
						rate = mapRate;
					}
					else {
						// The HDX uses this format "STS-3C" is is not the same as OME, so
						// remove the "-"
						rate = rate.replaceAll("-", "");
					}
					Object swMate = aResult.get("SWMATE");
					String newConnection = "<edge" + " id=\"" + aResult.get("CONNID")
					    + "\" type=\"" + aResult.get("CCT") + "\" rate=\"" + rate
					    + "\" source=\"" + neId +

					    "\" sShelf=\"" + fromAidArray[1] + "\" sSlot=\""
					    + fromAidArray[2] + "\" sSubslot=\"" + fromAidArray[3]
					    + "\" sPort=\"" + fromAidArray[4] + "\" sChannel=\""
					    + fromAidArray[6] + "\" target=\"" + neId + "\" sourceAid=\""
					    + fromAid +

					    "\" tShelf=\"" + toAidArray[1] + "\" tSlot=\"" + toAidArray[2]
					    + "\" tSubslot=\"" + toAidArray[3] + "\" tPort=\""
					    + toAidArray[4] + "\" tChannel=\"" + toAidArray[6]
					    + "\" targetAid=\"" + toAid + "\" swmate=\""
					    + (swMate == null ? "" : swMate) + "\" committed=\"true\" />";

					data.add(newConnection);
				}
				DbUtility.INSTANCE.addNewXConnect(ne, data);
			}
			return true;
		}
		catch (Exception e) {
			log.error("Failed to retrieve xConnection", e);
		}
		return false;
	}
}
