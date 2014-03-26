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

package com.nortel.appcore.app.drac.server.lpcp.trackers;

import java.math.BigInteger;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;

/**
 * 
 */
public final class LpcpFacility {
  private static final Logger log = LoggerFactory.getLogger(LpcpFacility.class);
	public static enum MODE {
		SONET, SDH
		// , WAN, ETH
	}

	public enum VCAT_ROUTING_TYPE {
		MUST_BE_VCAT, MUST_BE_CCAT, CAN_BE_EITHER
	}

	public static final int STS1 = 0;
	public static final int STS3C = 1;
	public static final int STS12C = 3;
	public static final int STS24C = 4;
	public static final int STS48C = 5;
	public static final int STS192C = 6;

	public static final int STM1 = 0;
	public static final int STM2 = 1;
	public static final int STM4 = 2;
	public static final int STM8 = 3;
	public static final int STM16 = 4;
	public static final int STM64 = 5;

	public static final int VC3 = 6;
	public static final int VC4 = 7;
	public static final int VC4_2C = 8;
	public static final int VC4_4C = 9;
	public static final int VC4_8C = 10;
	public static final int VC4_16C = 11;
	public static final int VC4_64C = 12;

	private final String neid;
	private final NeType neType;
	private final String aid;
	private final String shelf;
	private final String slot;
	private final String port;
	private final String type;
	private final Layer layer;
	private final NETWORK_ELEMENT_MODE neMode;
	private String sigtype;
	private String state;
	private String tna;
	private String faclabel;
	private String srlg;
	private String group;
	private String rate;
	private String mtu; // Ethernet only
	private TrackerConstraints constraints;
	private final String apsid; // BLSR only
	private BigInteger metric = new BigInteger("-1");
	private BigInteger cost = new BigInteger("-1");
	private final BasicTracker tracker;
	private final Map<String, String> extAttr;

	public LpcpFacility(NetworkElementHolder h, String facAid, String facShelf,
	    String facSlot, String facPort, String facType, String facState,
	    String facTna, String label, String facSigtype, Layer facLayer,
	    String facRate, String facApsid, NETWORK_ELEMENT_MODE facNeMode,
	    Map<String, String> extendedAttributes,
	    TrackerConstraints trackerConstraints) {
		/*
		 * We pass in the entire network element holder, extract from it what ever
		 * we want
		 */
		neid = h.getId();

		// neMode=h.getMode();
		neType = h.getType();
		// neRelease=h.getNeRelease();

		aid = facAid;
		shelf = facShelf;
		slot = facSlot;
		port = facPort;
		type = facType;
		state = facState;
		tna = facTna;
		faclabel = label;
		sigtype = facSigtype;
		layer = facLayer;
		rate = facRate;
		apsid = facApsid;
		neMode = facNeMode;
		constraints = trackerConstraints;

		extAttr = extendedAttributes;
		try {
			srlg = extAttr.get(FacilityConstants.SRLG_ATTR);
			cost = new BigInteger(extAttr.get(FacilityConstants.COST_ATTR));
			metric = new BigInteger(extAttr.get(FacilityConstants.METRIC_ATTR));
		}
		catch (Exception e) {
			log.debug("Exception parsing SRLG, COST or METRIC2", e);
		}

		tracker = getTracker(neid, facAid, facType, facNeMode, this);
	}

	/**
	 * getTracker: Populate the tracker for this facility.
	 * <p>
	 * NOTE: This should be made into a factory class, as we support more NE types
	 * and releases, the way we create a tracker for each facility will need to
	 * improve, perhaps the mediation layer should select the tracker and insert
	 * the tracker class name into each facility record and we just create them
	 * here? That would require a uniform constructor on each tracker..humm
	 */
	private static BasicTracker getTracker(String neid, String aid, String type,
	    NETWORK_ELEMENT_MODE neMode, LpcpFacility fac) {
		BasicTracker tracker = null;

		if (type == null) {
			log.error("Facility: Unknown facility rate: type is null cannot assign a tracker!");
			return null;
		}

		try {
			LpcpFacility.MODE neModeInt = null;
			if (NETWORK_ELEMENT_MODE.SONET.equals(neMode)) {
				neModeInt = LpcpFacility.MODE.SONET;
			}
			if (NETWORK_ELEMENT_MODE.SDH.equals(neMode)) {
				neModeInt = LpcpFacility.MODE.SDH;
			}
			if (type.startsWith("OC")) { // SONET

				if (FacilityConstants.OC1.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SONET,
					    neModeInt, STS1, neid, aid, fac.getConstraints());
				}
				else if (FacilityConstants.OC3.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SONET,
					    neModeInt, STS3C, neid, aid, fac.getConstraints());
				}
				else if (FacilityConstants.OC12.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SONET,
					    neModeInt, STS12C, neid, aid, fac.getConstraints());
				}
				else if (FacilityConstants.OC48.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SONET,
					    neModeInt, STS48C, neid, aid, fac.getConstraints());
				}
				else if (FacilityConstants.OC192.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SONET,
					    neModeInt, STS192C, neid, aid, fac.getConstraints());
				}
				else {
					log.debug("Facility: Unknown facility type: " + type);
				}
			}
			else if (type.startsWith("STM")) {
				if (FacilityConstants.STM1.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SDH,
					    neModeInt, STM1, neid, aid, fac.getConstraints());
				}
				else if (FacilityConstants.STM2.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SDH,
					    neModeInt, STM2, neid, aid, fac.getConstraints());
				}
				else if (FacilityConstants.STM4.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SDH,
					    neModeInt, STM4, neid, aid, fac.getConstraints());
				}
				else if (FacilityConstants.STM8.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SDH,
					    neModeInt, STM8, neid, aid, fac.getConstraints());
				}
				else if (FacilityConstants.STM16.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SDH,
					    neModeInt, STM16, neid, aid, fac.getConstraints());
				}
				else if (FacilityConstants.STM64.equalsIgnoreCase(type)) {
					tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SDH,
					    neModeInt, STM64, neid, aid, fac.getConstraints());
				}
			}
			else if (Facility.isWan(type)) {
				tracker = new EthWanBandwidthTracker(fac);
			}
			else if (Facility.isEth(type)) {
				tracker = new EthWanBandwidthTracker(fac);
			}
			else if (type.startsWith("WAV")) {
				log.debug("type is: " + type); // FIX THIS below
				tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SONET,
				    LpcpFacility.MODE.SONET, STS192C, neid, aid, fac.getConstraints());
			}
			else if (type.startsWith("OTM2")) {
				log.debug("type is: " + type); // FIX THIS below
				tracker = new OpticalFacilityTracker(LpcpFacility.MODE.SONET,
				    LpcpFacility.MODE.SONET, STS192C, neid, aid, fac.getConstraints());
			}
			else if (type.startsWith("LIM") || type.startsWith("CMD4")
			    || type.startsWith("CMD8") || type.startsWith("CMD44")) {
				
				tracker = new WavelengthTracker(neid, aid,
				    fac.extAttr.get(FacilityConstants.WAVELENGTH_ATTR),
				    fac.getConstraints());
			}
			else if ("TenGigabitEthernet".equals(type)
			    || "GigabitEthernet".equals(type) || "FortyGigE".equals(type)) {
				tracker = new Force10EthernetTracker(fac);
			}
			else {
				log.error("LpcpFacility: Unrecognised facility type: '" + type
				    + "' unable to create a tracker for unknown facility! " + fac);
			}
		}
		catch (Exception iae) {
			log.error("Exception creating tracker: " + type + " for " + fac, iae);
		}
		return tracker;

	}

	public String getAid() {
		return aid;
	}

	public String getAPSId() {
		return apsid;
	}

	/**
	 * @return the constraints
	 */
	public TrackerConstraints getConstraints() {
		return constraints;
	}

	public BigInteger getCost() {
		return cost;
	}

	public String getDomain() {
		return extAttr.get(FacilityConstants.DOMAIN_ATTR);
	}

	public Map<String, String> getExtendedAttributes() {
		return extAttr;
	}

	public String getFacLabel() {
		return faclabel;
	}

	public String getGroup() {
		return group;
	}

	public Layer getLayer() {
		return layer;
	}

	public BigInteger getMetric2() {
		return metric;
	}

	public String getMTU() {
		return mtu;
	}

	public String getNeId() {
		return neid;
	}

	public String getPort() {
		return port;
	}

	public String getRate() {
		if (Facility.isEth(getAid())) {
			return Facility.getFacRateDisplay(getAid());
		}
		return this.rate;
	}

	public String getShelf() {
		return shelf;
	}

	public String getSigType() {
		return sigtype;
	}

	public String getSiteId() {
		return extAttr.get(FacilityConstants.SITE_ATTR);
	}

	public String getSlot() {
		return slot;
	}

	public String getSRLG() {
		return srlg;
	}

	public String getState() {
		return state;
	}

	public String getTNA() {
		return tna;
	}

	public BasicTracker getTracker() {
		return tracker;
	}

	public String getType() {
		return type;
	}

	public VCAT_ROUTING_TYPE getVCATRoutingType() {
		String vcatAttr = getVcatAttr();

		if (vcatAttr != null) {
			// As of OME R7, value can be either DISABLE, ENABLE, or ENABLE;GROUPED
			// if (FacilityConstants.VCAT_ENABLE_STR.equals(vcatAttr))
			if (vcatAttr.contains(FacilityConstants.VCAT_ENABLE_STR)) {
				return VCAT_ROUTING_TYPE.MUST_BE_VCAT;
			}
			return VCAT_ROUTING_TYPE.MUST_BE_CCAT;
		}
		return VCAT_ROUTING_TYPE.CAN_BE_EITHER;
	}

	public String getWavelength() {
		return extAttr.get(FacilityConstants.WAVELENGTH_ATTR);
	}

	public boolean isEthWanEPL() {
		String eplFlag = extAttr.get(FacilityConstants.IS_EPL);
		if (eplFlag != null && Boolean.parseBoolean(eplFlag)) {
			return true;
		}
		return false;
	}

	public boolean isL2() {
		/**
		 * @TODO why don't we just check the layer attribute of the facility?
		 * 
		 *       <pre>
		 * return layer.equals(Layer.LAYER2)
		 * </pre>
		 */

		if (Facility.isL2(getType()) || neType.equals(NeType.FORCE10)) {
			return true;
		}

		return false;
	}

	public boolean isL2SS() {
		String s = getExtendedAttributes().get(FacilityConstants.IS_L2SS_FACILITY);
		if (s != null && Boolean.parseBoolean(s)) {
			return true;
		}

		return false;
	}

	public boolean isVlanIdRequired() throws Exception {
		/**
		 * Avoid having this logic in 2 places, each Ethernet tracker will know if
		 * it supports multiple service flows, if it does a vlan is required .
		 */
		if (getTracker() instanceof EthTrackerI) {
			return ((EthTrackerI) getTracker()).supportsMultipleServiceFlows();
		}
		return false;
	}

	/**
     * 
     */
	public void setConstraints(TrackerConstraints trackerConstraints)
	    throws Exception {
		constraints = trackerConstraints;
		tracker.setConstraints(constraints);
	}

	public synchronized void setCost(String newCost) {
		try {
			cost = new BigInteger(newCost);
		}
		catch (Exception e) {
			log.error("Exception parsing new cost: " + newCost
			    + " facility not modified.", e);
		}
	}

	public synchronized void setDomain(String domain) {
		extAttr.put(FacilityConstants.DOMAIN_ATTR, domain);
	}

	public synchronized void setFacLabel(String newFaclabel) {
		faclabel = newFaclabel;
	}

	public synchronized void setGroup(String newGroup) {
		group = newGroup;
	}

	public synchronized void setMetric(String metric2) {
		try {
			this.metric = new BigInteger(metric2);
		}
		catch (Exception e) {
			log.error("Exception parsing new metric2: " + e
			    + " facility not modified.", e);
		}
	}

	public synchronized void setMTU(String newMtu) {
		mtu = newMtu;
	}

	public synchronized void setRate(String newRate) {
		rate = newRate;
	}

	public synchronized void setSigType(String sigType) {
		sigtype = sigType;
	}

	public synchronized void setSiteId(String siteId) {
		extAttr.put(FacilityConstants.SITE_ATTR, siteId);
	}

	public synchronized void setSRLG(String newSrlg) {
		srlg = newSrlg;
	}

	public synchronized void setState(String newState) {
		state = newState;
	}

	public synchronized void setTNA(String newTna) {
		tna = newTna;
	}

	public synchronized void setVCATAttr(String vcat) {
		extAttr.put(FacilityConstants.VCAT_ATTR, vcat);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LpcpFacility [neid=");
		builder.append(neid);
		builder.append(", aid=");
		builder.append(aid);
		builder.append(", neType=");
		builder.append(neType);
		builder.append(", shelf=");
		builder.append(shelf);
		builder.append(", slot=");
		builder.append(slot);
		builder.append(", port=");
		builder.append(port);
		builder.append(", type=");
		builder.append(type);
		builder.append(", layer=");
		builder.append(layer);
		builder.append(", neMode=");
		builder.append(neMode);
		builder.append(", sigtype=");
		builder.append(sigtype);
		builder.append(", state=");
		builder.append(state);
		builder.append(", tna=");
		builder.append(tna);
		builder.append(", faclabel=");
		builder.append(faclabel);
		builder.append(", srlg=");
		builder.append(srlg);
		builder.append(", group=");
		builder.append(group);
		builder.append(", rate=");
		builder.append(rate);
		builder.append(", mtu=");
		builder.append(mtu);
		builder.append(", constraints=");
		builder.append(constraints);
		builder.append(", apsid=");
		builder.append(apsid);
		builder.append(", metric=");
		builder.append(metric);
		builder.append(", cost=");
		builder.append(cost);
		builder.append(", tracker=");
		builder.append(tracker);
		builder.append(", extAttr=");
		builder.append(extAttr);
		builder.append("]");
		return builder.toString();
	}

	private String getVcatAttr() {
		return extAttr.get(FacilityConstants.VCAT_ATTR);
	}

}
