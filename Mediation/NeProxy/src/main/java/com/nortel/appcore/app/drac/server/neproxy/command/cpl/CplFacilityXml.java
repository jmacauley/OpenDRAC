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

import java.util.ArrayList;
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

public final class CplFacilityXml extends AbstractFacilityXml {
  private static final Logger log = LoggerFactory.getLogger(CplFacilityXml.class);
	// private static final String NEKEY_ADJAID = "ADJAID";
	// private static final String NEKEY_PRISTATE = "PST";
	// private static final String NEKEY_ADJTYPE = "ADJTYPE";
	// private static final String NEKEY_PROVFEADDR = "PROVFEADDR";
	// private static final String NEKEY_DISCFEADDR = "DISCFEADDR";
	// private static final String NEKEY_WAVELENGTH = "WAVELENGTH";

	public CplFacilityXml(AbstractNetworkElement ne) {
		super(ne);
		setRoot(new Element("node"));
		getRoot().setAttribute("ID_ATTR", ne.getNeId());
		getRoot().setAttribute("IPADDRESS_ATTR", ne.getIpAddress());
		getRoot().setAttribute("PORT_ATTR", Integer.toString(ne.getPortNumber()));
	}

	public CplFacilityXml(Element root) {
		super(root);
	}

	public static List<Attribute> getStaticAttributes() {
		List<Attribute> returnValue = new ArrayList<Attribute>(
		    AbstractFacilityXml.getStaticAttributes());
		returnValue.add(new Attribute(AbstractFacilityXml.RATE_ATTR, "STS192C"));
		return returnValue;
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
		Exception error = new Exception("Unsupported method");
		log.error("CPL CplFacilityXML: Not supported for CPL NE", error);
	}

	public void updateFacilityInstance(String layer, String aid,
	    Map<String, String> attributes, boolean insert) {
		String facilityType = aid.split("-")[0];
		// If the record exists, update those attributes that are different
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
						log.debug("CPL CplFacilityXML: Updating facility: " + aid
						    + " old value=" + aFacility.getAttributeValue(key)
						    + " new value=" + value);
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
		/*
		 * So, this is a new facility record but still possible that it is not in
		 * the database yet.
		 */
		for (int i = 0; i < getAddList().size(); i++) {
			String aRecord = getAddList().get(i);
			if (aRecord.indexOf(aid) >= 0) {
				log.debug("CPL CplFacilityXML: Appending additional attributes into new facility: "
				    + aid);
				StringBuilder tempBuff = new StringBuilder(aRecord.substring(0,
				    aRecord.length() - 2)
				    + " ");
				Iterator<String> attrIr = attributes.keySet().iterator();
				while (attrIr.hasNext()) {
					String key = attrIr.next();
					tempBuff.append(key + "=\"" + attributes.get(key) + "\" ");
				}
				tempBuff.append(" />");
				getAddList().set(i, tempBuff.toString());
				return;
			}
		}
		if (insert) {
			
			String[] aidMap = aid.split("-");
			StringBuilder buff = new StringBuilder();
			buff.append("<" + LAYER0_ELEMENT + " aid=\"" + aid + "\" " + "port=\""
			    + aidMap[3] + "\" " + "shelf=\"" + aidMap[1] + "\" " + "slot=\""
			    + aidMap[2] + "\" " + "type=\"" + facilityType + "\" ");
			Iterator<String> attrIr = attributes.keySet().iterator();
			while (attrIr.hasNext()) {
				String key = attrIr.next();
				buff.append(key + "=\"" + attributes.get(key) + "\" ");
			}

			List<Attribute> staticAttr = CplFacilityXml.getStaticAttributes();
			for (Attribute attr : staticAttr) {
				buff.append(attr.getName() + "=\"" + attr.getValue() + "\" ");
			}
			buff.append(" />");
			getAddList().add(buff.toString());
		}
	}
}
