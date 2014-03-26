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

package com.nortel.appcore.app.drac.server.neproxy.command;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;

/**
 * Created on Jan 12, 2006
 * 
 * @author nguyentd
 */
public abstract class AbstractFacilityXml {
  
  private static final Logger log = LoggerFactory.getLogger(AbstractFacilityXml.class);
  
	public static final String ROOT_ELEMENT = "node";
	public static final String LAYER1_ELEMENT = Layer.LAYER1.toString();
	public static final String LAYER2_ELEMENT = Layer.LAYER2.toString();
	public static final String LAYER0_ELEMENT = Layer.LAYER0.toString();

	/*
	 * The following key is used to indicated whether or not the facility is in //
	 * NE database
	 */
	public static final String VALID_ATTR = DbKeys.NetworkElementFacilityCols.VALID;

	// The following keys are pre-defined keys, not exist in the NE
	public static final String GROUP_ATTR = DbKeys.NetworkElementFacilityCols.GROUP;
	public static final String SIGNALTYPE_ATTR = DbKeys.NetworkElementFacilityCols.SIGTYPE;
	public static final String COST_ATTR = DbKeys.NetworkElementFacilityCols.COST;
	public static final String METRIC_ATTR = DbKeys.NetworkElementFacilityCols.METRIC;
	public static final String SRLG_ATTR = DbKeys.NetworkElementFacilityCols.SRLG;
	public static final String TNA_ATTR = DbKeys.NetworkElementFacilityCols.TNA;
	public static final String FACLABEL_ATTR = DbKeys.NetworkElementFacilityCols.USER_LABEL;
	public static final String CONSTRAIN_ATTR = DbKeys.NetworkElementFacilityCols.CONSTRAIN;
	public static final String MANUALPROV_ATTR = "manualProvision";

	public static final String ID_ATTR = "id";
	public static final String IPADDRESS_ATTR = "ip";
	public static final String FACILITY_ELEMENT = "facilityInstance";
	public static final String AID_ATTR = "aid";
	public static final String PORT_ATTR = "port";
	public static final String PRIMARYSTATE_ATTR = "primaryState";
	public static final String MODE_ATTR = "mode";
	public static final String MAPPING_ATTR = "mapping";
	public static final String LCAS_ATTR = "lcas";
	public static final String VCAT_ATTR = "vcat";
	public static final String ACTUALUNIT_ATTR = "actualUnit";
	public static final String PROVUNIT_ATTR = "provUnit";
	public static final String PROVRXUNIT_ATTR = "provRxUnit";
	public static final String RATE_ATTR = "rate";
	public static final String APSID_ATTR = "apsId";
	public static final String PROTECTIONSCHEME_ATTR = "ps";
	public static final String SITEID_ATTR = "siteId";
	public static final String DOMAIN_ATTR = "domain";

	public static final String MTU_ATTR = "mtu";
	public static final String SPEED_ATTR = "speed";
	public static final String ANSPEED_ATTR = "anspeed";
	public static final String PHYSADDR_ATTR = "physicalAddress";
	public static final String AN_ATTR = "autoNegotiation";
	public static final String ANSTATUS_ATTR = "autoNegotiationStatus";
	public static final String ANETHDPX_ATTR = "advertisedDuplex";
	public static final String ETHDPX_ATTR = "etherDuplex";
	public static final String FLOWCTRL_ATTR = "flowControl";
	public static final String TXCOND_ATTR = "txConditioning";
	public static final String PAUSETX_ATTR = "controlPauseTx";
	public static final String PAUSERX_ATTR = "controlPauseRx";
	public static final String NETHDPX_ATTR = "negotiatedDuplex";
	public static final String NSPEED_ATTR = "negotiatedSpeed";
	public static final String NPAUSETX_ATTR = "negotiatedPauseTx";
	public static final String NPAUSERX_ATTR = "negotiatedPauseRx";
	public static final String LPETHDPX_ATTR = "linkPartnerDuplex";
	public static final String LPSPEED_ATTR = "linkPartnerSpeed";
	public static final String LPFLOWCTRL_ATTR = "linkPartnerFlowControl";
	public static final String PASSCTRL_ATTR = "passControlFrame";
	public static final String IFTYPE_ATTR = "interfaceType";
	public static final String POLICING_ATTR = "policing";
	public static final String ETYPE_ATTR = "encapsulationType";
	public static final String PRIORITYMODE_ATTR = "priorityMode";
	public static final String BWTHRESHOLD_ATTR = "bandwidthThreshold";
	public static final String BWREMAIN_ATTR = "remainedBandwidth";
	public static final String BWUTL_ATTR = "bandwidthUtilization";
	public static final String LADID_ATTR = "lagId";
	public static final String VLANID_ATTR = "vlanId";

	public static final String WAVELENGTH_ATTR = "wavelength";
	public static final String ACTIVE_ATTR = "active";

	public static final String SHELF_ATTR = "shelf";
	public static final String SLOT_ATTR = "slot";
	public static final String TYPE_ATTR = "type";

	private static final List<Attribute> STATIC_ATTRIBUTES = new ArrayList<Attribute>();
	private Element root;
	private final XMLOutputter outXml = new XMLOutputter(
	    Format.getCompactFormat());
	private final List<Object[]> updateList = new ArrayList<Object[]>();
	private final List<String> addList = new ArrayList<String>();
	private final List<Object[]> newAttributeList = new ArrayList<Object[]>();

	public static final HashSet<String> COMMON_PARAMETER_LIST = new HashSet<String>();
	static {
		COMMON_PARAMETER_LIST.add(ClientMessageXml.TNA_KEY);
		COMMON_PARAMETER_LIST.add(ClientMessageXml.FACLABEL_KEY);
		COMMON_PARAMETER_LIST.add(ClientMessageXml.COST_KEY);
		COMMON_PARAMETER_LIST.add(ClientMessageXml.SIGNALINGTYPE_KEY);
		COMMON_PARAMETER_LIST.add(ClientMessageXml.METRIC_KEY);
		COMMON_PARAMETER_LIST.add(ClientMessageXml.GROUP_KEY);
		COMMON_PARAMETER_LIST.add(ClientMessageXml.SRLG_KEY);
		COMMON_PARAMETER_LIST.add(ClientMessageXml.CONSTRAINT_KEY);
		COMMON_PARAMETER_LIST.add(ClientMessageXml.DOMAIN_KEY);
		COMMON_PARAMETER_LIST.add(ClientMessageXml.SITE_KEY);
	}

	public static final HashMap<String, String> COMMON_XML_ATTRIBUTES = new HashMap<String, String>();
	static {
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.TNA_KEY,
		    AbstractFacilityXml.TNA_ATTR);
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.FACLABEL_KEY,
		    AbstractFacilityXml.FACLABEL_ATTR);
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.COST_KEY,
		    AbstractFacilityXml.COST_ATTR);
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.SIGNALINGTYPE_KEY,
		    AbstractFacilityXml.SIGNALTYPE_ATTR);
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.METRIC_KEY,
		    AbstractFacilityXml.METRIC_ATTR);
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.GROUP_KEY,
		    AbstractFacilityXml.GROUP_ATTR);
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.SRLG_KEY,
		    AbstractFacilityXml.SRLG_ATTR);
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.CONSTRAINT_KEY,
		    AbstractFacilityXml.CONSTRAIN_ATTR);
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.DOMAIN_KEY,
		    AbstractFacilityXml.DOMAIN_ATTR);
		COMMON_XML_ATTRIBUTES.put(ClientMessageXml.SITE_KEY,
		    AbstractFacilityXml.SITEID_ATTR);
	}

	public AbstractFacilityXml(AbstractNetworkElement ne) {
		root = new Element("node");
		root.setAttribute("ID_ATTR", ne.getNeId());
		root.setAttribute("IPADDRESS_ATTR", ne.getIpAddress());
		root.setAttribute("PORT_ATTR", Integer.toString(ne.getPortNumber()));
	}

	public AbstractFacilityXml(Element top) {
		root = top;
	}

	public AbstractFacilityXml(String aFacility) {
		try {
			ByteArrayInputStream data = new ByteArrayInputStream(aFacility.getBytes());
			SAXBuilder builder = new SAXBuilder();
			Document aDoc = builder.build(data);
			this.root = aDoc.getRootElement();
		}
		catch (Exception e) {
			log.error("Failed to initialize the Facility.XML " + aFacility, e);
		}
	}

	public static List<Attribute> getStaticAttributes() {
		synchronized (STATIC_ATTRIBUTES) {
			if (STATIC_ATTRIBUTES.size() == 0) {
				STATIC_ATTRIBUTES.add(new Attribute(AbstractFacilityXml.GROUP_ATTR,
				    "none"));
				STATIC_ATTRIBUTES.add(new Attribute(
				    AbstractFacilityXml.SIGNALTYPE_ATTR,
				    FacilityConstants.SIGNAL_TYPE.unassigned.toString()));
				STATIC_ATTRIBUTES.add(new Attribute(AbstractFacilityXml.TNA_ATTR,
				    FacilityConstants.DEFAULT_TNA));
				STATIC_ATTRIBUTES.add(new Attribute(AbstractFacilityXml.FACLABEL_ATTR,
				    FacilityConstants.DEFAULT_FACLABEL));
				STATIC_ATTRIBUTES
				    .add(new Attribute(AbstractFacilityXml.COST_ATTR, "1"));
				STATIC_ATTRIBUTES.add(new Attribute(AbstractFacilityXml.METRIC_ATTR,
				    "1"));
				STATIC_ATTRIBUTES.add(new Attribute(AbstractFacilityXml.SRLG_ATTR,
				    "N/A"));
				STATIC_ATTRIBUTES.add(new Attribute(AbstractFacilityXml.CONSTRAIN_ATTR,
				    "0"));
				STATIC_ATTRIBUTES.add(new Attribute(
				    AbstractFacilityXml.MANUALPROV_ATTR, "false"));
				STATIC_ATTRIBUTES.add(new Attribute(AbstractFacilityXml.SITEID_ATTR,
				    "N/A"));
				STATIC_ATTRIBUTES.add(new Attribute(AbstractFacilityXml.DOMAIN_ATTR,
				    "N/A"));
			}
		}
		return STATIC_ATTRIBUTES;
	}

	/**
	 * @return the addList
	 */
	public List<String> getAddList() {
		return addList;
	}

	/**
	 * @return the newAttributeList
	 */
	public List<Object[]> getNewAttributeList() {
		return newAttributeList;
	}

	/**
	 * @return the root
	 */
	public Element getRoot() {
		return root;
	}

	/**
	 * @return the updateList
	 */
	public List<Object[]> getUpdateList() {
		return updateList;
	}

	public void populateStaticAttributes() {
		if (root.getAttribute(AbstractFacilityXml.GROUP_ATTR) == null) {
			root.setAttribute(AbstractFacilityXml.GROUP_ATTR, "none");
		}
		if (root.getAttribute(AbstractFacilityXml.TNA_ATTR) == null) {
			root.setAttribute(AbstractFacilityXml.TNA_ATTR, "N/A");
		}
		if (root.getAttribute(AbstractFacilityXml.COST_ATTR) == null) {
			root.setAttribute(AbstractFacilityXml.COST_ATTR, "1");
		}
		if (root.getAttribute(AbstractFacilityXml.METRIC_ATTR) == null) {
			root.setAttribute(AbstractFacilityXml.METRIC_ATTR, "1");
		}
		if (root.getAttribute(AbstractFacilityXml.SRLG_ATTR) == null) {
			root.setAttribute(AbstractFacilityXml.SRLG_ATTR, "N/A");
		}
		if (root.getAttribute(AbstractFacilityXml.CONSTRAIN_ATTR) == null) {
			root.setAttribute(AbstractFacilityXml.CONSTRAIN_ATTR, "0");
		}
		if (root.getAttribute(AbstractFacilityXml.MANUALPROV_ATTR) == null) {
			root.setAttribute(AbstractFacilityXml.MANUALPROV_ATTR, "false");
		}
		if (root.getAttribute(AbstractFacilityXml.SITEID_ATTR) == null) {
			root.setAttribute(AbstractFacilityXml.SITEID_ATTR, "N/A");
		}
		if (root.getAttribute(AbstractFacilityXml.DOMAIN_ATTR) == null) {
			root.setAttribute(AbstractFacilityXml.DOMAIN_ATTR, "N/A");
		}
	}

	public String rootNodeToString() {
		return outXml.outputString(root);
	}

	/**
	 * @param root
	 *          the root to set
	 */
	public void setRoot(Element root) {
		this.root = root;
	}

	public abstract void updateDataBase(AbstractNetworkElement ne)
	    throws Exception;

	public Map<String, String> updateFacilityAttr(String aid,
	    Map<String, String> attributes) {
		Iterator<String> attr = attributes.keySet().iterator();
		HashMap<String, String> temp = null;
		while (attr.hasNext()) {
			String key = attr.next();
			String value = attributes.get(key);

			// in this case, "root" is a facility INSTANCE
			String xmlValue = root.getAttributeValue(key);
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

	public abstract void updateFacilityInstance(String idenfication, String aid,
	    Map<String, String> attributes);
}
