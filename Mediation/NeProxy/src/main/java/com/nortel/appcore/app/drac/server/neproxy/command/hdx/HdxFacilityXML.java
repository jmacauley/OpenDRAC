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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;

/**
 * @author nguyentd
 */
public final class HdxFacilityXML extends AbstractFacilityXml {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final Map<String, String> neRoleMap = new HashMap<String, String>();

	public HdxFacilityXML(AbstractNetworkElement ne) {
		super(ne);
		initNeRoleMapping();
		setRoot(new Element("node"));
		getRoot().setAttribute("ID_ATTR", ne.getNeId());
		getRoot().setAttribute("IPADDRESS_ATTR", ne.getIpAddress());
		getRoot().setAttribute("PORT_ATTR", Integer.toString(ne.getPortNumber()));
	}

	public HdxFacilityXML(Element root) {
		super(root);
		initNeRoleMapping();
	}

	@Override
	public void updateDataBase(AbstractNetworkElement ne) throws Exception {
		if (getAddList().size() > 0) {
			DbUtility.INSTANCE.addNewFacility(ne, getAddList());
		}
		if (getUpdateList().size() > 0) {
			DbUtility.INSTANCE.updateFacility(ne, getUpdateList());
		}
		if (getNewAttributeList().size() > 0) {
			DbUtility.INSTANCE.updateFacilityAttr(ne, getNewAttributeList());
		}
	}

	@Override
	public void updateFacilityInstance(String layer, String aid,
	    Map<String, String> attributes) {
		String facilityType = aid.split("-")[0];
		if (neRoleMap.containsKey(facilityType)) {
			facilityType = neRoleMap.get(facilityType);
		}
		Iterator<Element> ir = getRoot().getChildren().iterator();
		while (ir.hasNext()) {
			Element aFacility = ir.next();
			if (aFacility.getAttributeValue(AID_ATTR).equalsIgnoreCase(aid)) {
				Iterator<String> attr = attributes.keySet().iterator();
				Map<String, String> temp = null;
				while (attr.hasNext()) {
					String key = attr.next();
					String value = attributes.get(key);
					if (value == null) {
						value = "N/A";
					}

					if (!aFacility.getAttributeValue(key).equalsIgnoreCase(value)) {
						log.debug("Updating facility: " + aid + " old value="
						    + aFacility.getAttributeValue(key) + " new value=" + value);
						if (temp == null) {
							temp = new HashMap<String, String>();
						}
						temp.put(key, value);
					}
				}
				if (temp != null) {
					getUpdateList().add(new Object[] { aid, temp });
				}
				return;
			}
		}
		
		String[] aidMap = aid.split("-");
		StringBuilder buff = new StringBuilder(80);
		buff.append("<" + LAYER1_ELEMENT + " aid=\"" + aid + "\" " + "port=\""
		    + aidMap[4] + "\" " + "shelf=\"" + aidMap[1] + "\" " + "slot=\""
		    + aidMap[2] + "\" " + "type=\"" + facilityType + "\" ");
		Iterator<String> attrIr = attributes.keySet().iterator();
		while (attrIr.hasNext()) {
			String key = attrIr.next();
			buff.append(key + "=\"" + attributes.get(key) + "\" ");
		}
		List<Attribute> staticAttr = AbstractFacilityXml.getStaticAttributes();
		for (int i = 0; i < staticAttr.size(); i++) {
			Attribute attr = staticAttr.get(i);
			buff.append(attr.getName() + "=\"" + attr.getValue() + "\" ");
		}
		buff.append(" />");
		getAddList().add(buff.toString());
	}

	private void initNeRoleMapping() {
		neRoleMap.put("STM1", "OC3");
		neRoleMap.put("STM4", "OC12");
		neRoleMap.put("STM16", "OC48");
		neRoleMap.put("STM64", "OC192");
	}
}
