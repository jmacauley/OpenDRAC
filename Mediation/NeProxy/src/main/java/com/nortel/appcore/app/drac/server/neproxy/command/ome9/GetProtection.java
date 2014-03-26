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

import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class GetProtection extends AbstractCommandlet {
	public GetProtection(Map<String, Object> param) {
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

			log.debug("Processing " + aCommand + " for " + ne.getIpAddress());
			Map<String, String> paramList = new HashMap<String, String>();
			paramList.put(ObjectElement.OPERATION_KEY, aCommand);

			Iterator<String> anotherIr = getParameters().keySet().iterator();
			while (anotherIr.hasNext()) {
				String aParam = anotherIr.next();
				String aValue = (String) getParameters().get(aParam);
				paramList.put(aParam, aValue);
			}

			/*
			 * Since the Protection's attribute may or may not be available, we have
			 * to reset all of them in the database first.
			 */
			HashMap<String, String> attributeList = new HashMap<String, String>();
			attributeList.put(AbstractFacilityXml.APSID_ATTR, "N/A");
			attributeList.put(AbstractFacilityXml.PROTECTIONSCHEME_ATTR, "N/A");
			DbUtility.INSTANCE.updateFacilities(ne, attributeList);

			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					String protectionScheme = aResult.get("PS");
					String workingAid = aResult.get("WRKGAID");
					String protectionAid = aResult.get("PROTAID");
					ne.getProtectionAid().add(workingAid);
					ne.getProtectionAid().add(protectionAid);

					log.debug("got protection info for " + workingAid + ","
					    + protectionAid);
					// Now get the Ring information
					paramList.clear();
					paramList.put(ObjectElement.OPERATION_KEY,
					    Tl1CommandCode.RTRV_BLSRMAP.toString());
					paramList.put("WRKGAID", workingAid);
					paramList.put("PROTAID", protectionAid);

					result = ne.getTl1Session().sendToNE(paramList);
					if (result != null) {
						Iterator<Map<String, String>> ir = result.iterator();
						while (ir.hasNext()) {
							Map<String, String> blsrResult = ir.next();
							String ringName = blsrResult.get("LABEL");
							ne.getRingName().put(workingAid, ringName);
							ne.getRingName().put(protectionAid, ringName);

							// ringMap is expected to have the following format
							// "3:3/0:0/1:1/2:2/3:3"
							try {
								String ringMap = blsrResult.get("RINGMAP");
								log.debug("got ringMap info for " + ringName + " in " + ringMap);

								String apsIdStr = ringMap.split("/")[0].split(":")[0];
								ne.setApsId(Integer.valueOf(apsIdStr));

								// update the facility in the database with the apsId.
								DbUtility.INSTANCE.updateOrAddFacilityAttr(ne, workingAid,
								    AbstractFacilityXml.APSID_ATTR, apsIdStr);
								DbUtility.INSTANCE
								    .updateOrAddFacilityAttr(ne, workingAid,
								        AbstractFacilityXml.PROTECTIONSCHEME_ATTR,
								        protectionScheme);
								DbUtility.INSTANCE.updateOrAddFacilityAttr(ne,
								    protectionAid, AbstractFacilityXml.APSID_ATTR, apsIdStr);
								DbUtility.INSTANCE.updateOrAddFacilityAttr(ne,
								    protectionAid, AbstractFacilityXml.PROTECTIONSCHEME_ATTR,
								    protectionScheme);

								log.debug("apsId= " + apsIdStr);
							}
							catch (Exception ex) {
								log.error("Fail to obtain the ring information", ex);
							}
						}
					}
					return true;
				}
			}
		}
		catch (Exception e) {
			log.error("Fail to process the NE fibre protection", e);
		}
		return false;
	}
}
