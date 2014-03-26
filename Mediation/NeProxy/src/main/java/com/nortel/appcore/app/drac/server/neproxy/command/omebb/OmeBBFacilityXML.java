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

package com.nortel.appcore.app.drac.server.neproxy.command.omebb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;

public final class OmeBBFacilityXML extends AbstractFacilityXml {
  private final Logger log = LoggerFactory.getLogger(getClass());
	// private static final String NEKEY_AID = "AID";
	private static final String NEKEY_PRISTATE = "PST";
	public static Map<String, String> commonNeToXmlMapping = null;

	public OmeBBFacilityXML(Element root) {
		super(root);
		initKeyMapping();
		initStaticAttributes();
	}

	public OmeBBFacilityXML(NetworkElement ne) {
		super(ne);
		initKeyMapping();
	}

	public OmeBBFacilityXML(String aFacility) {
		super(aFacility);
		initKeyMapping();
		initStaticAttributes();
	}

	private static void initKeyMapping() {
		if (commonNeToXmlMapping != null) {
			return;
		}
		commonNeToXmlMapping = new HashMap<String, String>();
		commonNeToXmlMapping.put(NEKEY_PRISTATE,
		    AbstractFacilityXml.PRIMARYSTATE_ATTR);

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
	public HashMap<String, String> updateFacilityAttr(String aid, Map attributes) {
		Iterator attr = attributes.keySet().iterator();
		HashMap<String, String> temp = null;
		while (attr.hasNext()) {
			String key = (String) attr.next();
			String value = (String) attributes.get(key);

			// in this case, "root" is a facility INSTANCE
			String xmlValue = getRoot().getAttributeValue(key);
			if (xmlValue == null) {
				
			}
			else {
				if (!xmlValue.equalsIgnoreCase(value)) {
					log.debug("Updating facility: old value=" + xmlValue + " new value="
					    + value);
					if (temp == null) {
						temp = new HashMap<String, String>();
					}
					temp.put(key, value);
				}
			}
		}
		return temp;

	}

	@Override
	public void updateFacilityInstance(String identification, String aid,
	    Map attributes) {
		// Iterate through all facility instances in this XML document to look
		// for the one that matches its AID with the one being updated
		String[] aidMap = aid.split("-");
		String facilityType = aidMap[0];
		Iterator ir = getRoot().getChildren().iterator();
		while (ir.hasNext()) {
			Element aFacility = (Element) ir.next();

			if (aFacility.getAttributeValue(AID_ATTR).equalsIgnoreCase(aid)) {
				Iterator attr = attributes.keySet().iterator();
				HashMap temp = null;
				HashMap newAttrList = null;
				while (attr.hasNext()) {
					String key = (String) attr.next();
					String value = (String) attributes.get(key);
					if (value == null) {
						value = "N/A";
					}

					// Try to get the existing one from the XML
					String attributeXML = aFacility.getAttributeValue(key);
					if (attributeXML != null) {
						if (!attributeXML.equalsIgnoreCase(value)) {
							log.debug("Updating facility: " + aid + " old value="
							    + aFacility.getAttributeValue(key) + " new value=" + value);
							if (temp == null) {
								temp = new HashMap();
							}
							temp.put(key, value);
						}
					}
					else {
						// Ne attribute from the NE but not in the XML DB yet. First
						// get the XML key definition, if it's not defined then ignore
						// this attribute
						if (newAttrList == null) {
							newAttrList = new HashMap();
							newAttrList.put(key, value);
						}
					}
				}
				if (temp != null) {
					getUpdateList().add(new Object[] { aid, temp });
				}
				if (newAttrList != null) {
					getNewAttributeList().add(new Object[] { aid, newAttrList });
				}
				return;
			}
		}
		

		StringBuilder buff = new StringBuilder();
		buff.append("<" + LAYER1_ELEMENT + " aid=\"" + aid + "\" ");
		buff.append("port=\"" + aidMap[4] + "\" ");
		buff.append("shelf=\"" + aidMap[1] + "\" ");
		buff.append("slot=\"" + aidMap[2] + "\" ");
		buff.append("type=\"" + facilityType + "\" ");

		Iterator attrIr = attributes.keySet().iterator();
		while (attrIr.hasNext()) {
			String key = (String) attrIr.next();
			buff.append(key + "=\"" + attributes.get(key) + "\" ");
		}
		List<Attribute> staticAttr = AbstractFacilityXml.getStaticAttributes();
		for (int i = 0; i < staticAttr.size(); i++) {
			Attribute attr = staticAttr.get(i);
			buff.append(attr.getName() + "=\"" + attr.getValue() + "\" ");
		}

		getAddList().add(buff.toString());
	}

	private void initStaticAttributes() {
		List<Attribute> staticAttr = super.getStaticAttributes();
		for (int i = 0; i < staticAttr.size(); i++) {
			Attribute anAttr = staticAttr.get(i);
			if (getRoot().getAttributeValue(anAttr.getName()) == null) {
				getRoot().setAttribute(anAttr.getName(), anAttr.getValue());
			}
		}
	}

}