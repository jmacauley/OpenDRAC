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
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;

public final class OmeFacilityXML extends AbstractFacilityXml {
  private final Logger log = LoggerFactory.getLogger(getClass());
	// private static final String NEKEY_AID = "AID";
	private static final String NEKEY_PRISTATE = "PST";
	private static final String NEKEY_ACTUALUNIT = "ACTUALUNITS";
	private static final String NEKEY_PROVRXUNITS = "PROVRXUNITS";
	private static final String NEKEY_MODE = "MODE";
	private static final String NEKEY_VCAT = "vcat";
	private static final String NEKEY_LCAS = "LCAS";
	private static final String NEKEY_RATE = "RATE";
	private static final String NEKEY_MAPPING = "MAPPING";

	public static final String NEKEY_AN = "AN";
	private static final String NEKEY_ANSTATUS = "ANSTATUS";
	private static final String NEKEY_ETHDPX = "ETHDPX";
	private static final String NEKEY_SPEED = "SPEED";
	private static final String NEKEY_FLOWCTRL = "FLOWCTRL";
	private static final String NEKEY_TXCON = "TXCON";
	private static final String NEKEY_PAUSETX = "PAUSETX";
	private static final String NEKEY_PAUSERX = "PAUSERX";
	private static final String NEKEY_ANETHDPX = "ANETHDPX";
	private static final String NEKEY_ANSPEED = "ANSPEED";
	private static final String NEKEY_ANPAUSETX = "ANPAUSETX";
	private static final String NEKEY_ANPAUSERX = "ANPAUSERX";
	// private static final String NEKEY_ADVETHDPX = "ADVETHDPX";
	// private static final String NEKEY_ADVSPEED = "ADVSPEED";
	// private static final String NEKEY_ADFLOWCTRL = "ADFLOWCTRL";
	public static final String NEKEY_MTU = "MTU";
	private static final String NEKEY_PASSCTRL = "PASSCTRL";
	private static final String NEKEY_PHYSADDR = "PHYSADDR";
	// private static final String NEKEY_PAUSETXOVRRIDE = "PAUSETXOVRRIDE";
	private static final String NEKEY_IFTYPE = "IFTYPE";
	private static final String NEKEY_POLICING = "POLICING";
	private static final String NEKEY_ETYPE = "ETYPE";
	private static final String NEKEY_PMODE = "PMODE";
	private static final String NEKEY_BWTHRESHLD = "BWTHRESHLD";
	private static final String NEKEY_BWREMAIN = "BWREMAIN";
	private static final String NEKEY_BWUTL = "BWUTL";
	private static final String NEKEY_LAGID = "LAGID";

	// GGL2
	public static final String L2_ACTIVATION_KEYS_LIST = "L2_activation_keys_list";
	public static final String L2_VCS_AID = "L2_vcs_aid";
	public static final String L2_FROMSIDE_VCE_AID = "L2_fromside_vce_aid";
	public static final String L2_FROMSIDE_VCEMAP_AID = "L2_fromside_vcemap_aid";
	public static final String L2_TOSIDE_VCE_AID = "L2_toside_vce_aid";
	public static final String L2_TOSIDE_VCEMAP_AID = "L2_toside_vcemap_aid";
	public static final String L2_L1_WAN_AID = "L2_L1_wan_aid";
	public static final String L2_L1_XC_FROMAID = "L2_L1_xc_fromaid";
	public static final String L2_L1_XC_TOAID = "L2_L1_xc_toaid";

	static List<String> L2ActivationEntitiesKeysList;
	static Map<String, String> commonNeToXmlMapping;

	static Map<String, String> wanNeToXmlMapping;
	static Map<String, String> ethNeToXmlMapping;
	static {
		L2ActivationEntitiesKeysList = new ArrayList<String>();
		L2ActivationEntitiesKeysList.add(L2_VCS_AID);
		L2ActivationEntitiesKeysList.add(L2_FROMSIDE_VCE_AID);
		L2ActivationEntitiesKeysList.add(L2_FROMSIDE_VCEMAP_AID);
		L2ActivationEntitiesKeysList.add(L2_TOSIDE_VCE_AID);
		L2ActivationEntitiesKeysList.add(L2_TOSIDE_VCEMAP_AID);
		L2ActivationEntitiesKeysList.add(L2_L1_WAN_AID);
		L2ActivationEntitiesKeysList.add(L2_L1_XC_FROMAID);
		L2ActivationEntitiesKeysList.add(L2_L1_XC_TOAID);
	}

	public OmeFacilityXML(Element root) {
		super(root);
		initKeyMapping();
	}

	public OmeFacilityXML(NetworkElement ne) {
		super(ne);
		initKeyMapping();
	}

	public OmeFacilityXML(String aFacility) {
		super(aFacility);
		initKeyMapping();
		initStaticAttributes();
	}

	public static List<Attribute> getStaticAttributes() {
		List<Attribute> returnValue = new ArrayList<Attribute>(
		    AbstractFacilityXml.getStaticAttributes());
		returnValue.add(new Attribute(AbstractFacilityXml.APSID_ATTR, "N/A"));
		returnValue.add(new Attribute(AbstractFacilityXml.PROTECTIONSCHEME_ATTR,
		    "N/A"));
		return returnValue;
	}

	private static void initKeyMapping() {
		if (commonNeToXmlMapping != null) {
			return;
		}
		commonNeToXmlMapping = new HashMap<String, String>();
		commonNeToXmlMapping.put(NEKEY_PRISTATE,
		    AbstractFacilityXml.PRIMARYSTATE_ATTR);

		wanNeToXmlMapping = new HashMap<String, String>(commonNeToXmlMapping);
		wanNeToXmlMapping
		    .put(NEKEY_ACTUALUNIT, AbstractFacilityXml.ACTUALUNIT_ATTR);
		wanNeToXmlMapping.put(NEKEY_PROVRXUNITS, AbstractFacilityXml.PROVUNIT_ATTR);
		wanNeToXmlMapping.put(NEKEY_MODE, AbstractFacilityXml.MODE_ATTR);
		wanNeToXmlMapping.put(NEKEY_VCAT, AbstractFacilityXml.VCAT_ATTR);
		wanNeToXmlMapping.put(NEKEY_RATE, AbstractFacilityXml.RATE_ATTR);
		wanNeToXmlMapping.put(NEKEY_MAPPING, AbstractFacilityXml.MAPPING_ATTR);
		wanNeToXmlMapping.put(NEKEY_LCAS, AbstractFacilityXml.LCAS_ATTR);

		ethNeToXmlMapping = new HashMap<String, String>(commonNeToXmlMapping);
		ethNeToXmlMapping.put(NEKEY_AN, AbstractFacilityXml.AN_ATTR);
		ethNeToXmlMapping.put(NEKEY_ANSTATUS, AbstractFacilityXml.ANSTATUS_ATTR);
		ethNeToXmlMapping.put(NEKEY_ETHDPX, AbstractFacilityXml.ETHDPX_ATTR);
		ethNeToXmlMapping.put(NEKEY_SPEED, AbstractFacilityXml.SPEED_ATTR);
		ethNeToXmlMapping.put(NEKEY_FLOWCTRL, AbstractFacilityXml.FLOWCTRL_ATTR);
		ethNeToXmlMapping.put(NEKEY_TXCON, AbstractFacilityXml.TXCOND_ATTR);
		ethNeToXmlMapping.put(NEKEY_PAUSETX, AbstractFacilityXml.PAUSETX_ATTR);
		ethNeToXmlMapping.put(NEKEY_PAUSERX, AbstractFacilityXml.PAUSERX_ATTR);
		ethNeToXmlMapping.put(NEKEY_ANETHDPX, AbstractFacilityXml.ANETHDPX_ATTR);
		ethNeToXmlMapping.put(NEKEY_ANSPEED, AbstractFacilityXml.ANSPEED_ATTR);
		ethNeToXmlMapping.put(NEKEY_ANPAUSETX, AbstractFacilityXml.PAUSETX_ATTR);
		ethNeToXmlMapping.put(NEKEY_ANPAUSERX, AbstractFacilityXml.PAUSERX_ATTR);
		ethNeToXmlMapping.put(NEKEY_MTU, AbstractFacilityXml.MTU_ATTR);
		ethNeToXmlMapping.put(NEKEY_PASSCTRL, AbstractFacilityXml.PASSCTRL_ATTR);
		ethNeToXmlMapping.put(NEKEY_PHYSADDR, AbstractFacilityXml.PHYSADDR_ATTR);
		ethNeToXmlMapping.put(NEKEY_IFTYPE, AbstractFacilityXml.IFTYPE_ATTR);
		ethNeToXmlMapping.put(NEKEY_POLICING, AbstractFacilityXml.POLICING_ATTR);
		ethNeToXmlMapping.put(NEKEY_ETYPE, AbstractFacilityXml.ETYPE_ATTR);
		ethNeToXmlMapping.put(NEKEY_PMODE, AbstractFacilityXml.PRIORITYMODE_ATTR);
		ethNeToXmlMapping.put(NEKEY_BWTHRESHLD,
		    AbstractFacilityXml.BWTHRESHOLD_ATTR);
		ethNeToXmlMapping.put(NEKEY_BWREMAIN, AbstractFacilityXml.BWREMAIN_ATTR);
		ethNeToXmlMapping.put(NEKEY_BWUTL, AbstractFacilityXml.BWUTL_ATTR);
		ethNeToXmlMapping.put(NEKEY_LAGID, AbstractFacilityXml.LADID_ATTR);
	}

	public void _OLD_updateWanFacility(String idenfication, String wanAid,
	    Map<String, String> wanAttr, String ethAid, Map<String, String> ethAttr) {
		Iterator<Element> ir = getRoot().getChildren().iterator();

		while (ir.hasNext()) {
			Element aWanFacility = ir.next();
			if (aWanFacility.getAttributeValue(AID_ATTR).equalsIgnoreCase(wanAid)) {
				Iterator<String> attr = wanAttr.keySet().iterator();
				
				// 
				Map<String, String> wanTemp = null;
				Map<String, String> ethTemp = null;
				Map<String, String> wanAttrTemp = null;
				Map<String, String> ethAttrTemp = null;
				while (attr.hasNext()) {
					String key = attr.next();
					String value = wanAttr.get(key);

					if (value == null) {
						value = "N/A";
					}
					String wanAttrXML = aWanFacility.getAttributeValue(key);
					if (wanAttrXML != null) {
						if (!wanAttrXML.equalsIgnoreCase(value)) {
							log.debug("Updating WanFacility: " + wanAid + " old value="
							    + aWanFacility.getAttributeValue(key) + " new value=" + value);
							if (wanTemp == null) {
								wanTemp = new HashMap<String, String>();
							}
							wanTemp.put(key, value);
						}
					}
					else {
						log.debug("Adding new WAN attr: " + ethAid + ":" + key + "="
						    + value);
						if (wanAttrTemp == null) {
							wanAttrTemp = new HashMap<String, String>();
						}
						wanAttrTemp.put(key, value);
					}
				}
				Iterator<String> etherIrr = ethAttr.keySet().iterator();
				Element aEthFacility = aWanFacility.getChild(LAYER2_ELEMENT);
				
				// 
				while (etherIrr.hasNext()) {
					String key = etherIrr.next();
					String value = ethAttr.get(key);

					if (value == null) {
						value = "N/A";
					}
					String ethAttrXML = aEthFacility.getAttributeValue(key);
					if (ethAttrXML != null) {
						if (!ethAttrXML.equalsIgnoreCase(value)) {
							log.debug("Updating EthFacility: " + ethAid + " old value="
							    + aEthFacility.getAttributeValue(key) + " new value=" + value);
							if (ethTemp == null) {
								ethTemp = new HashMap<String, String>();
							}
							ethTemp.put(key, value);
						}
					}
					else {
						log.debug("Adding new ETH attr: " + ethAid + ":" + key + "="
						    + value);
						if (ethAttrTemp == null) {
							ethAttrTemp = new HashMap<String, String>();
						}
						ethAttrTemp.put(key, value);
					}
				}
				if (wanTemp != null || ethTemp != null) {
					getUpdateList()
					    .add(new Object[] { wanAid, wanTemp, ethAid, ethTemp });
				}
				if (wanAttrTemp != null || ethAttrTemp != null) {
					getNewAttributeList().add(
					    new Object[] { wanAid, wanAttrTemp, ethAid, ethAttrTemp });
				}
				return;
			}
		}
		
		StringBuilder buff = new StringBuilder();
		String[] wanAidMap = wanAid.split("-");
		buff.append("<" + LAYER1_ELEMENT + " id=\"1\" aid=\"" + wanAid + "\" ");

		buff.append("port=\"" + wanAidMap[3] + "\" ");
		buff.append("shelf=\"" + wanAidMap[1] + "\" ");
		buff.append("slot=\"" + wanAidMap[2] + "\" ");
		buff.append("type=\"" + wanAidMap[0] + "\" ");
		Iterator<String> wanIr = wanAttr.keySet().iterator();
		while (wanIr.hasNext()) {
			String key = wanIr.next();
			buff.append(key + "=\"" + wanAttr.get(key) + "\" ");
		}
		List<Attribute> staticAttr = OmeFacilityXML.getStaticAttributes();
		for (int i = 0; i < staticAttr.size(); i++) {
			Attribute attr = staticAttr.get(i);
			buff.append(attr.getName() + "=\"" + attr.getValue() + "\" ");
		}
		// buff.append(
		// " manualProvision=\"false\" apsId=\"N/A\" constrain=\"0\" group=\"none\" signalingType=\"unassigned\" tna=\"N/A\" cost=\"1\" metric=\"1\" srlg=\"N/A\" >"
		// );
		buff.append(">");
		String[] ethAidMap = ethAid.split("-");
		// buff.append("<" + LAYER2_ELEMENT + " id=\"" + idenfication + ethAid +
		// "\" aid=\"" + ethAid +
		// "\" ");
		buff.append("<" + LAYER2_ELEMENT + " id=\"2\" aid=\"" + ethAid + "\" ");
		buff.append("port=\"" + ethAidMap[3] + "\" ");
		buff.append("shelf=\"" + ethAidMap[1] + "\" ");
		buff.append("slot=\"" + ethAidMap[2] + "\" ");
		buff.append("type=\"" + ethAidMap[0] + "\" ");

		Iterator<String> ethIr = ethAttr.keySet().iterator();
		while (ethIr.hasNext()) {
			String key = ethIr.next();
			buff.append(key + "=\"" + ethAttr.get(key) + "\" ");
		}

		// buff.append(
		// " manualProvision=\"false\" apsId=\"N/A\" constrain=\"0\" group=\"none\" signalingType=\"unassigned\" tna=\"N/A\" />"
		// ) ;
		for (int i = 0; i < staticAttr.size(); i++) {
			Attribute attr = staticAttr.get(i);
			buff.append(attr.getName() + "=\"" + attr.getValue() + "\" ");
		}
		buff.append("/></" + LAYER1_ELEMENT + ">");
		getAddList().add(buff.toString());
	}

	@Override
	public void populateStaticAttributes() {
		super.populateStaticAttributes();
		if (getRoot().getAttribute(AbstractFacilityXml.APSID_ATTR) == null) {
			getRoot().setAttribute(AbstractFacilityXml.APSID_ATTR, "N/A");
		}
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

	// GGL2 new
	public void updateEthFacility(String idenfication, String ethAid,
	    Map<String, String> ethAttr) {
		Iterator<Element> ir = getRoot().getChildren().iterator();

		while (ir.hasNext()) {
			Element aEthFacility = ir.next();
			if (aEthFacility.getAttributeValue(AID_ATTR).equalsIgnoreCase(ethAid)) {
				Iterator<String> attr = ethAttr.keySet().iterator();
				
				Map<String, String> ethTemp = null;
				Map<String, String> ethAttrTemp = null;
				while (attr.hasNext()) {
					String key = attr.next();
					String value = ethAttr.get(key);

					if (value == null) {
						value = "N/A";
					}
					String ethAttrXML = aEthFacility.getAttributeValue(key);
					if (ethAttrXML != null) {
						if (!ethAttrXML.equalsIgnoreCase(value)) {
							log.debug("Updating EthFacility: " + ethAid + " old value="
							    + aEthFacility.getAttributeValue(key) + " new value=" + value);
							if (ethTemp == null) {
								ethTemp = new HashMap<String, String>();
							}
							ethTemp.put(key, value);
						}
					}
					else {
						log.debug("Adding new ETH attr: " + ethAid + ":" + key + "="
						    + value);
						if (ethAttrTemp == null) {
							ethAttrTemp = new HashMap<String, String>();
						}
						ethAttrTemp.put(key, value);
					}
				}

				if (ethTemp != null) {
					getUpdateList().add(new Object[] { ethAid, ethTemp });
				}
				if (ethAttrTemp != null) {
					getNewAttributeList().add(new Object[] { ethAid, ethAttrTemp });
				}
				return;
			}
		}

		
		StringBuilder buff = new StringBuilder();
		String[] ethAidMap = ethAid.split("-");
		buff.append("<" + LAYER2_ELEMENT + " id=\"2\" aid=\"" + ethAid + "\" ");
		buff.append("port=\"" + ethAidMap[3] + "\" ");
		buff.append("shelf=\"" + ethAidMap[1] + "\" ");
		buff.append("slot=\"" + ethAidMap[2] + "\" ");
		buff.append("type=\"" + ethAidMap[0] + "\" ");

		Iterator<String> ethIr = ethAttr.keySet().iterator();
		while (ethIr.hasNext()) {
			String key = ethIr.next();
			buff.append(key + "=\"" + ethAttr.get(key) + "\" ");
		}

		List<Attribute> staticAttr = OmeFacilityXML.getStaticAttributes();
		for (int i = 0; i < staticAttr.size(); i++) {
			Attribute attr = staticAttr.get(i);
			buff.append(attr.getName() + "=\"" + attr.getValue() + "\" ");
		}

		buff.append(">");
		buff.append("/></" + LAYER2_ELEMENT + ">");
		getAddList().add(buff.toString());
	}

	@Override
	public Map<String, String> updateFacilityAttr(String aid,
	    Map<String, String> attributes) {
		Iterator<String> attr = attributes.keySet().iterator();
		HashMap<String, String> temp = null;
		while (attr.hasNext()) {
			String key = attr.next();
			String value = attributes.get(key);

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
	    Map<String, String> attributes) {
		// Iterate through all facility instances in this XML document to look
		// for the one that matches its AID with the one being updated
		Iterator<Element> ir = getRoot().getChildren().iterator();
		while (ir.hasNext()) {
			Element aFacility = ir.next();
			if (aFacility.getAttributeValue(AID_ATTR).equalsIgnoreCase(aid)) {
				Iterator<String> attr = attributes.keySet().iterator();
				HashMap<String, String> temp = null;
				HashMap<String, String> newAttrList = null;
				while (attr.hasNext()) {
					String key = attr.next();
					String value = attributes.get(key);
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
								temp = new HashMap<String, String>();
							}
							temp.put(key, value);
						}
					}
					else {
						// Ne attribute from the NE but not in the XML DB yet. First
						// get the XML key definition, if it's not defined then ignore
						// this attribute
						if (newAttrList == null) {
							newAttrList = new HashMap<String, String>();
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
		String[] aidMap = aid.split("-");
		buff.append("<" + LAYER1_ELEMENT + " id=\"1\" aid=\"" + aid + "\" ");
		buff.append("port=\"" + aidMap[3] + "\" ");
		buff.append("shelf=\"" + aidMap[1] + "\" ");
		buff.append("slot=\"" + aidMap[2] + "\" ");
		buff.append("type=\"" + aidMap[0] + "\" ");
		Iterator<String> attrIr = attributes.keySet().iterator();
		while (attrIr.hasNext()) {
			String key = attrIr.next();
			buff.append(key + "=\"" + attributes.get(key) + "\" ");
		}
		List<Attribute> staticAttr = OmeFacilityXML.getStaticAttributes();
		for (int i = 0; i < staticAttr.size(); i++) {
			Attribute attr = staticAttr.get(i);
			buff.append(attr.getName() + "=\"" + attr.getValue() + "\" ");
		}
		buff.append(" />");
		getAddList().add(buff.toString());
		/*
		 * addList.add( "<" + LAYER1_ELEMENT + " id=\"1\" aid=\"" + aid + "\" " +
		 * "port=\"" + aidMap[3] + "\" " + "primaryState=\"" +
		 * attributes.get(PRIMARYSTATE_ATTR) + "\" " + "shelf=\"" + aidMap[1] +
		 * "\" " + "slot=\"" + aidMap[2] + "\" " + "type=\"" + aidMap[0] +
		 * "\" valid=\"true\"" +
		 * " manualProvision=\"false\" apsId=\"N/A\" constrain=\"0\" group=\"none\" signalingType=\"unassigned\" tna=\"N/A\" cost=\"1\" metric=\"1\" srlg=\"N/A\""
		 * + " />");
		 */
	}

	// GGL2 Removed implicit layer1/layer2 relationship code here
	public void updateWanFacility(String idenfication, String wanAid,
	    Map<String, String> wanAttr) {
		Iterator<Element> ir = getRoot().getChildren().iterator();

		while (ir.hasNext()) {
			Element aWanFacility = ir.next();
			if (aWanFacility.getAttributeValue(AID_ATTR).equalsIgnoreCase(wanAid)) {
				Iterator<String> attr = wanAttr.keySet().iterator();
				

				Map<String, String> wanTemp = null;
				// Map<String, String> ethTemp = null;
				Map<String, String> wanAttrTemp = null;
				// Map<String, String> ethAttrTemp = null;
				while (attr.hasNext()) {
					String key = attr.next();
					String value = wanAttr.get(key);

					if (value == null) {
						value = "N/A";
					}
					String wanAttrXML = aWanFacility.getAttributeValue(key);
					if (wanAttrXML != null) {
						if (!wanAttrXML.equalsIgnoreCase(value)) {
							log.debug("Updating WanFacility: " + wanAid + " old value="
							    + aWanFacility.getAttributeValue(key) + " new value=" + value);
							if (wanTemp == null) {
								wanTemp = new HashMap<String, String>();
							}
							wanTemp.put(key, value);
						}
					}
					else {
						log.debug("Adding new WAN attr: " + wanAid + ":" + key + "="
						    + value);
						if (wanAttrTemp == null) {
							wanAttrTemp = new HashMap<String, String>();
						}
						wanAttrTemp.put(key, value);
					}
				}

				if (wanTemp != null) {
					getUpdateList().add(new Object[] { wanAid, wanTemp });
				}
				if (wanAttrTemp != null) {
					getNewAttributeList().add(new Object[] { wanAid, wanAttrTemp });
				}
				return;
			}
		}

		
		StringBuilder buff = new StringBuilder();
		String[] wanAidMap = wanAid.split("-");
		buff.append("<" + LAYER1_ELEMENT + " id=\"1\" aid=\"" + wanAid + "\" ");
		buff.append("port=\"" + wanAidMap[3] + "\" ");
		buff.append("shelf=\"" + wanAidMap[1] + "\" ");
		buff.append("slot=\"" + wanAidMap[2] + "\" ");
		buff.append("type=\"" + wanAidMap[0] + "\" ");

		Iterator<String> wanIr = wanAttr.keySet().iterator();
		while (wanIr.hasNext()) {
			String key = wanIr.next();
			buff.append(key + "=\"" + wanAttr.get(key) + "\" ");
		}
		List<Attribute> staticAttr = OmeFacilityXML.getStaticAttributes();
		for (int i = 0; i < staticAttr.size(); i++) {
			Attribute attr = staticAttr.get(i);
			buff.append(attr.getName() + "=\"" + attr.getValue() + "\" ");
		}

		buff.append(">");
		buff.append("/></" + LAYER1_ELEMENT + ">");
		getAddList().add(buff.toString());
	}

	private void initStaticAttributes() {
		List<Attribute> staticAttr = OmeFacilityXML.getStaticAttributes();
		for (int i = 0; i < staticAttr.size(); i++) {
			Attribute anAttr = staticAttr.get(i);
			if (getRoot().getAttributeValue(anAttr.getName()) == null) {
				getRoot().setAttribute(anAttr.getName(), anAttr.getValue());
			}
		}
	}
}
