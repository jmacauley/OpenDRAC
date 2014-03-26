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

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckable;

/**
 * Talk about confusion. We have a database table for storing facilities.
 * Facilities are stored in an XML string format to be generic. You could read
 * the table and get a List<Facility> or a List<Map<String,String>>. However a
 * Facility object is not well used, instead the EndpointType object represents
 * a read-only view of a facility, it also carries around some extra data such
 * as a channellNumber which is used for scheduling and has nothing to do with a
 * facility per say, only how you might be using a (portion) of a facility for
 * scheduling.
 * <p>
 * Created on Dec 5, 2005
 * 
 * @author nguyentd
 */
public final class EndPointType implements Serializable, PolicyCheckable {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	String NEFacilityAttrs[] = { "tna", "cost", "metric", "srlg", "mtu",
	    "provUnit", "vcat", "speed", "physicalAddress", "autoNegotiation",
	    "autoNegotiationStatus", "advertisedDuplex", "flowControl",
	    "txConditioning", "controlPauseTx", "controlPauseRx", "negotiatedDuplex",
	    "negotiatedSpeed", "negotiatedPauseTx", "negotiatedPauseRx",
	    "linkPartnerDuplex", "linkPartnerSpeed", "linkPartnerFlowControl",
	    "passControlFrame", "interfaceType", "policing", "encapsulationType",
	    "priorityMode", "bandwidthThreshold", "remainedBandwidth",
	    "bandwidthUtilization", "lagId", "wavelength", "site", "vlanId" };

	private static final long serialVersionUID = 1;

	private String id = "unknown";
	private String state = "unknown";
	private String signalingType = "unknown";
	private String node = "unknown";
	private int channelNumber = -1;

	public static final String FACILITY_ENDPOINT_RESOURCE_ID_DELIMITER = "_";

	/**
	 * Used to determine whether or not the Endpoint has been assigned to any
	 * particular user group.
	 */
	// private boolean provisioned = true;
	/**
	 * Each element in the HashMap is a string and is referenced the key defined
	 * above.
	 */
	private Map<String, String> attributes = new HashMap<String, String>();

	// A port from GetEndPointXML.java::addEndPoint
	public EndPointType(Map<String, String> facMap) {
		// Create the id such that it aligns with facility Id in the
		// DbNetworkElementFacility
		String nodeId = facMap.get(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC);
		String aid = facMap.get(DbKeys.NetworkElementFacilityCols.AID);
		this.setId(EndPointType.encodeFacilityEndpointResourceId(nodeId, aid));

		for (String facilityAttr : NEFacilityAttrs) {
			// Loop through the attributes of the endpoint
			if (facMap.containsKey(facilityAttr)) {
				this.getAttributes().put(facilityAttr, facMap.get(facilityAttr));
			}
		}

		this.setNode(nodeId);
		this.setLayer(facMap.get(FacilityConstants.LAYER_ATTR));
		this.setState(facMap.get(FacilityConstants.PRISTATE_ATTR));
		this.setSignalingType(facMap.get(FacilityConstants.SIGTYPE_ATTR));
		// this.setProvisioned(!"none".equalsIgnoreCase(facMap.get(FacilityConstants.GROUP_ATTR)));
		this.setName(facMap.get(FacilityConstants.TNA_ATTR));
		this.setLabel(facMap.get(FacilityConstants.FACLABEL_ATTR));
		this.setSite(facMap.get(FacilityConstants.SITE_ATTR));
		this.setType(facMap.get(FacilityConstants.TYPE_ATTR));
		this.setAsL2SS(facMap.get(FacilityConstants.IS_L2SS_FACILITY));

		try {
			String constrain = facMap.get(FacilityConstants.CONSTRAINTS_ATTR);
			BigInteger bigInt = new BigInteger(constrain);
			this.setConstrain(bigInt);
		}
		catch (NumberFormatException nfe) {
			log.warn("constrain not a number", nfe);
			this.setConstrain(BigInteger.ZERO);
		}

	}

	public EndPointType() {
	  super();
  }

	// Three classes ultimately represent the same entity: EndPointType, Resource,
	// and
	// DbNetworkElementFacility.
	// These are all connectable endpoints in the network that are assigned
	// ownership. Tie together their
	// internal id representations in order to better navigate:
	public static String encodeFacilityEndpointResourceId(String neid, String aid) {
		return neid + FACILITY_ENDPOINT_RESOURCE_ID_DELIMITER + aid;
	}

	public String getAid() {
		// Extract the aid from the encoded id.
		return id.split(FACILITY_ENDPOINT_RESOURCE_ID_DELIMITER)[1];
	}

	/**
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * @return the channelNumber
	 */
	public int getChannelNumber() {
		return channelNumber;
	}

	// To Adrian: we should have a better structure for signalling values.
	// Can we just have a list of all possible value ?

	/**
	 * @return the constrain
	 */
	public BigInteger getConstrain() {
		String big = attributes.get(FacilityConstants.CONSTRAINTS_ATTR);
		if (big == null) {
			big = "0";
		}
		return new BigInteger(big);
	}

	/**
	 * @return the cost
	 */
	public int getCost() {
		String cost = attributes.get(FacilityConstants.COST_ATTR);
		if (cost == null) {
			cost = "0";
		}
		return Integer.parseInt(cost);
	}

	/**
	 * @return the dataRate
	 */
	public int getDataRate() {
		String speed = attributes.get(FacilityConstants.SPEED_ATTR);
		if (speed == null) {
			speed = "0";
		}
		return Integer.parseInt(speed);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public String getLabel() {
		return attributes.get(FacilityConstants.FACLABEL_ATTR);
	}

	// TODO: Return Layer enum here
	public String getLayer() {
		String layer = attributes.get(FacilityConstants.LAYER_ATTR);
		if (layer == null) {
			layer = "";
		}
		return layer;
	}

	/**
	 * @return the metric
	 */
	public int getMetric() {
		String metric = attributes.get(FacilityConstants.METRIC_ATTR);
		if (metric == null) {
			metric = "0";
		}
		return Integer.parseInt(metric);
	}

	/**
	 * @return the mtu
	 */
	public int getMtu() {
		String mtu = attributes.get(FacilityConstants.MTU_ATTR);
		if (mtu == null) {
			mtu = "0";
		}
		return Integer.parseInt(mtu);
	}

	public String getName() {
		return attributes.get(FacilityConstants.TNA_ATTR);
	}

	public String getNeid() {
		// Extract the neid from the encoded id.
		return id.split(FACILITY_ENDPOINT_RESOURCE_ID_DELIMITER)[0];
	}

	/**
	 * @return the node
	 */
	public String getNode() {
		return node;
	}

	/**
	 * @return the physAddr
	 */
	public String getPhysAddr() {
		String physAddr = attributes.get(FacilityConstants.PHYSICALADDRESS_ATTR);
		if (physAddr == null) {
			physAddr = "";
		}
		return physAddr;
	}

	/**
	 * @return the signalingType
	 */
	public String getSignalingType() {
		return signalingType;
	}

	public String getSite() {
		String site = attributes.get(FacilityConstants.SITE_ATTR);
		if (site == null) {
			site = "";
		}
		return site;
	}

	/**
	 * @return the srlg
	 */
	public String getSrlg() {
		String srlg = attributes.get(FacilityConstants.SRLG_ATTR);
		if (srlg == null) {
			srlg = "";
		}
		return srlg;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		String type = attributes.get(FacilityConstants.TYPE_ATTR);
		if (type == null) {
			type = "";
		}
		return type;
	}

	public String getWavelength() {
		String wavelength = attributes.get(FacilityConstants.WAVELENGTH_ATTR);
		if (wavelength == null) {
			wavelength = "";
		}
		return wavelength;
	}

	public boolean isEthWanEPL() {
		String eplFlag = attributes.get(FacilityConstants.IS_EPL);
		if (eplFlag != null && Boolean.parseBoolean(eplFlag)) {
			return true;
		}

		return false;
	}

	public boolean isLayer0() {
		return getLayer().equals(Layer.LAYER0.toString());
	}

	public boolean isLayer1() {
		return getLayer().equals(Layer.LAYER1.toString());
	}

	public boolean isLayer2() {
		return getLayer().equals(Layer.LAYER2.toString());
	}

	public boolean isUNIEndpoint() {
		return this.signalingType.equals("UNI") || this.signalingType.equals("EN");
	}

	public void setAsL2SS(String isL2SS) {
		attributes.put(FacilityConstants.IS_L2SS_FACILITY, isL2SS);
	}

	/**
     * 
     */
	public void setAttributes(Map<String, String> attr) {
		if (attr != null) {
			attributes = attr;
		}
	}

	/**
	 * @param channelNumber
	 *          the channelNumber to set
	 */
	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}

	/**
	 * @param constrain
	 *          the constrain to set
	 */
	public void setConstrain(BigInteger constrain) {
		attributes.put(FacilityConstants.CONSTRAINTS_ATTR, constrain.toString());
	}

	/**
	 * @param cost
	 *          the cost to set
	 */
	public void setCost(int cost) {
		attributes.put(FacilityConstants.COST_ATTR, Integer.toString(cost));
	}

	/**
	 * @param dataRate
	 *          the dataRate to set
	 */
	public void setDataRate(int dataRate) {
		attributes.put(FacilityConstants.SPEED_ATTR, Integer.toString(dataRate));
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	public void setLabel(String label) {
		attributes.put(FacilityConstants.FACLABEL_ATTR, label);
	}

	public void setLayer(String layer) {
		attributes.put(FacilityConstants.LAYER_ATTR, layer);
	}

	/**
	 * @param metric
	 *          the metric to set
	 */
	public void setMetric(int metric) {
		attributes.put(FacilityConstants.METRIC_ATTR, Integer.toString(metric));
	}

	/**
	 * @param mtu
	 *          the mtu to set
	 */
	public void setMtu(int mtu) {
		attributes.put(FacilityConstants.MTU_ATTR, Integer.toString(mtu));
	}

	public void setName(String name) {
		attributes.put(FacilityConstants.TNA_ATTR, name);
	}

	/**
	 * @param node
	 *          the node to set
	 */
	public void setNode(String node) {
		this.node = node;
	}

	/**
	 * @param physAddr
	 *          the physAddr to set
	 */
	public void setPhysAddr(String physAddr) {
		attributes.put(FacilityConstants.PHYSICALADDRESS_ATTR, physAddr);
	}

	/**
	 * @param signalingType
	 *          the signalingType to set
	 */
	public void setSignalingType(String signalingType) {
		this.signalingType = signalingType;
	}

	public void setSite(String site) {
		attributes.put(FacilityConstants.SITE_ATTR, site == null ? "" : site);
	}

	/**
	 * @param srlg
	 *          the srlg to set
	 */
	public void setSrlg(String srlg) {
		attributes.put(FacilityConstants.SRLG_ATTR, srlg);
	}

	/**
	 * @param state
	 *          the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @param type
	 *          the type to set
	 */
	public void setType(String type) {
		attributes.put(FacilityConstants.TYPE_ATTR, type);
	}

	public void setWavelength(String wavelength) {
		attributes.put(FacilityConstants.WAVELENGTH_ATTR, wavelength);
	}

	@Override
	public String toString() {
		return "Endpoint: " + id + " state=" + state + " signalingType="
		    + signalingType + " node=" + node + " channelNumber=" + channelNumber
		    + " attributes:" + attributes;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + Arrays.hashCode(NEFacilityAttrs);
	    result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
	    result = prime * result + channelNumber;
	    result = prime * result + ((node == null) ? 0 : node.hashCode());
	    result = prime * result + ((signalingType == null) ? 0 : signalingType.hashCode());
	    result = prime * result + ((state == null) ? 0 : state.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    EndPointType other = (EndPointType) obj;
	    if (!Arrays.equals(NEFacilityAttrs, other.NEFacilityAttrs))
		    return false;
	    if (attributes == null) {
		    if (other.attributes != null)
			    return false;
	    } else if (!attributes.equals(other.attributes))
		    return false;
	    if (channelNumber != other.channelNumber)
		    return false;
	    if (node == null) {
		    if (other.node != null)
			    return false;
	    } else if (!node.equals(other.node))
		    return false;
	    if (signalingType == null) {
		    if (other.signalingType != null)
			    return false;
	    } else if (!signalingType.equals(other.signalingType))
		    return false;
	    if (state == null) {
		    if (other.state != null)
			    return false;
	    } else if (!state.equals(other.state))
		    return false;
	    return true;
    }

    public boolean simpleEquals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    EndPointType other = (EndPointType) obj;
	    if (!Arrays.equals(NEFacilityAttrs, other.NEFacilityAttrs))
		    return false;
	   

	    if (node == null) {
		    if (other.node != null)
			    return false;
	    } else if (!node.equals(other.node))
		    return false;
	    if (signalingType == null) {
		    if (other.signalingType != null)
			    return false;
	    } else if (!signalingType.equals(other.signalingType))
		    return false;
	    if (state == null) {
		    if (other.state != null)
			    return false;
	    } else if (!state.equals(other.state))
		    return false;
	    return true;
    }

}
