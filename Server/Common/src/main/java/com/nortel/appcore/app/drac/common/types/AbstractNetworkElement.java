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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAssociationEvent;

public abstract class AbstractNetworkElement {

	public static final String EVENTRECV_KEY = "$eventReceived";

	private String ipAddress;

	private String terminalId;

	// Cache inventory. Not all NE will populate this data
	private List<InventoryXml> inventory = new ArrayList<InventoryXml>();

	// Cache cards and ports. Not all NE types will populate this data. Added for
	// OME L2SS
	private List<EquipmentXml> ports = new ArrayList<EquipmentXml>();
	private List<EquipmentXml> cards = new ArrayList<EquipmentXml>();

	private static final Map<String, String> SONET_TO_SDH_MAP = new HashMap<String, String>();

	private static final Map<String, String> SDH_TO_SONET_MAP = new HashMap<String, String>();
	private static final String[][] SONET_TO_SDH = { { "STS3C", "VC4" },
	    { "STS6C", "VC4-2C" }, { "STS12C", "VC4-4C" }, { "STS24C", "VC4-8C" },
	    { "STS48C", "VC4-16C" }, { "STS192C", "VC4-64C" } };
	private static final String[][] SDH_TO_SONET = { { "VC4", "STS3C" },
	    { "VC4-2C", "STS6C" }, { "VC4-4C", "STS12C" }, { "VC4-8C", "STS24C" },
	    { "VC4-16C", "STS48C" }, { "VC4-64C", "STS192C" } };
	private PROTOCOL_TYPE neCommProtocol = PROTOCOL_TYPE.NETL1_PROTOCOL;

	private int portNumber;
	private String neName = "Unknown";
	private NETWORK_ELEMENT_MODE neMode = NETWORK_ELEMENT_MODE.Unknown;
	private String subType = "Unknown";
	private String neRelease = "Unknown"; // store the full ne release for viewing
	                                      // only
	private NeType neType = NeType.UNKNOWN;
	private String neId = "00-00-00-00-00-00";
	private String uid;
	private CryptedString passwd;
	// Allow to be identified as something else
	private AbstractNetworkElement idNe;

	private int neIndex = 1;
	// private boolean broadcastAssociationEvent = true;
	private NeStatus neState = NeStatus.NE_NOT_CONNECT;
	private Date lastStateChange = new Date();
	private NeStatus previousNeState = NeStatus.NE_NOT_PROVISION;
	private int totalRetry;
	private boolean autoReDiscover = true;
	private Tl1XmlAssociationEvent dbChgEvent;

	protected Double positionX;
	protected Double positionY;


	/**
	 * @return the sdhToSonetMap
	 */
	public static Map<String, String> getSdhToSonetMap() {
		return SDH_TO_SONET_MAP;
	}

	/**
	 * @return the sonetToSdhMap
	 */
	public static Map<String, String> getSonetToSdhMap() {
		return SONET_TO_SDH_MAP;
	}

	public abstract void changeNePassword(String userId, CryptedString newPassword);

	public List<EquipmentXml> getCards() {
		return cards;
	}

	/**
	 * @return the idNe
	 */
	public AbstractNetworkElement getIdNe() {
		return idNe;
	}

	/**
	 * @return the inventory
	 */
	public List<InventoryXml> getInventory() {
		return inventory;
	}

	public String getIpAddress() {
		return this.ipAddress;
	}

	/**
	 * @return the neCommProtocol
	 */
	public PROTOCOL_TYPE getNeCommProtocol() {
		return neCommProtocol;
	}

	public String getNeId() {
		return this.neId;
	}

	/**
	 * @return the neIndex
	 */
	public int getNeIndex() {
		return neIndex;
	}

	public NETWORK_ELEMENT_MODE getNeMode() {
		return neMode;
	}

	public String getNeName() {
		return neName.replaceAll("\"", "");
	}

        public String getSubType() {
                return subType;
        }

	public String getNeRelease() {
		return neRelease;
	}

	public NeStatus getNeStatus() {
		return getState();
	}

	public String getNeStatusStamp() {
		if (lastStateChange == null) {
			return null;
		}

		return "since " + lastStateChange;
	}

	public String getNeStatusString() {

		if (lastStateChange == null) {
			return getNeStatus().getStateString();
		}

		return getNeStatus().getStateString() + " " + getNeStatusStamp();
	}

	public NeType getNeType() {
		return neType;
	}

	public CryptedString getPasswd() {
		return this.passwd;
	}

	public int getPortNumber() {
		return this.portNumber;
	}

	/**
	 * @return the equipment
	 */
	public List<EquipmentXml> getPorts() {
		return ports;
	}

	/**
	 * @return the previousNeState
	 */
	public NeStatus getPreviousNeState() {
		return previousNeState;
	}

	public synchronized NeStatus getState() {
		return neState;
	}

	/**
	 * @return the terminalId
	 */
	public String getTerminalId() {
		return terminalId;
	}

	/**
	 * @return the totalRetry
	 */
	public int getTotalRetry() {
		return totalRetry;
	}

	public String getUid() {
		return this.uid;
	}

	public void incTotalRetry() {
		totalRetry++;
	}

	public void initMap() {
		for (String[] element : SONET_TO_SDH) {
			SONET_TO_SDH_MAP.put(element[0], element[1]);
		}
		for (String[] element : SDH_TO_SONET) {
			SDH_TO_SONET_MAP.put(element[0], element[1]);
		}
	}

	/**
	 * @return the autoReDiscover
	 */
	public boolean isAutoReDiscover() {
		return autoReDiscover;
	}

	// /**
	// * @return the broadcastAssociationEvent
	// */
	// public boolean isBroadcastAssociationEvent()
	// {
	// return broadcastAssociationEvent;
	// }

	public boolean isConnected() {
		if (neState.ordinal() > NeStatus.NE_NOT_CONNECT.ordinal()) {
			return true;
		}

		return false;
	}

	public abstract void nextState();

	/**
	 * @param autoReDiscover
	 *          the autoReDiscover to set
	 */
	public void setAutoReDiscover(boolean autoReDiscover) {
		this.autoReDiscover = autoReDiscover;
	}

	// /**
	// * @param broadcastAssociationEvent the broadcastAssociationEvent to set
	// */
	// public void setBroadcastAssociationEvent(boolean broadcastAssociationEvent)
	// {
	// this.broadcastAssociationEvent = broadcastAssociationEvent;
	// }

	// /**
	// * @param equipment the equipment to set
	// */
	// public void setEquipment(List<EquipmentXml> ports)
	// {
	// this.ports = ports;
	// }

	/**
	 * @param idNe
	 *          the idNe to set
	 */
	public void setIdNe(AbstractNetworkElement idNe) {
		this.idNe = idNe;
	}

	/**
	 * @param inventory
	 *          the inventory to set
	 */
	public void setInventory(List<InventoryXml> inventory) {
		this.inventory = inventory;
	}

	/**
	 * @param ipAddress
	 *          the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @param neCommProtocol
	 *          the neCommProtocol to set
	 */
	public void setNeCommProtocol(PROTOCOL_TYPE neCommProtocol) {
		this.neCommProtocol = neCommProtocol;
	}

	/**
	 * @param neId
	 *          the neId to set
	 */
	public void setNeId(String neId) {
		this.neId = neId;
	}

	/**
	 * @param neIndex
	 *          the neIndex to set
	 */
	public void setNeIndex(int neIndex) {
		this.neIndex = neIndex;
	}

	/**
     *
     */
	public void setNeMode(NETWORK_ELEMENT_MODE newNeMode) {
		neMode = newNeMode;
	}

	/**
	 * @param neName
	 *          the neName to set
	 */
	public void setNeName(String neName) {
		this.neName = neName;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public void setNeRelease(String neRelease) {
		this.neRelease = neRelease;
	}

	/**
	 * @param neType
	 *          the neType to set
	 */
	public void setNeType(NeType neType) {
		this.neType = neType;
	}

	/**
	 * @param passwd
	 *          the passwd to set
	 */
	public void setPasswd(CryptedString passwd) {
		this.passwd = passwd;
	}

	/**
	 * @param portNumber
	 *          the portNumber to set
	 */
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	/**
	 * @param previousNeState
	 *          the previousNeState to set
	 */
	public void setPreviousNeState(NeStatus previousNeState) {
		this.previousNeState = previousNeState;
	}

	public synchronized void setState(NeStatus i) {
		/*
		 * if the state is NOTPROVISION, then just return since the NE is
		 * unprovisioned
		 */
		if (neState == NeStatus.NE_NOT_PROVISION) {
			lastStateChange = null;
			return;
		}
		neState = i;
		lastStateChange = new Date();
	}

	/**
	 * @param terminalId
	 *          the terminalId to set
	 */
	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	/**
	 * @param totalRetry
	 *          the totalRetry to set
	 */
	public void setTotalRetry(int totalRetry) {
		this.totalRetry = totalRetry;
	}

	/**
	 * @param uid
	 *          the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	public abstract void terminate();

	public String toDebugString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractNetworkElement [autoReDiscover=");
		builder.append(autoReDiscover);
		// builder.append(", broadcastAssociationEvent=");
		// builder.append(broadcastAssociationEvent);
		builder.append(", dbChgEvent=");
		builder.append(dbChgEvent);
		builder.append(", cards=");
		builder.append(cards);
		builder.append(", ports=");
		builder.append(ports);
		builder.append(", idNe=");
		builder.append(idNe);
		builder.append(", inventory=");
		builder.append(inventory);
		builder.append(", ipAddress=");
		builder.append(ipAddress);
		builder.append(", neCommProtocol=");
		builder.append(neCommProtocol);
		builder.append(", neId=");
		builder.append(neId);
		builder.append(", neIndex=");
		builder.append(neIndex);
		builder.append(", neMode=");
		builder.append(neMode);
		builder.append(", neName=");
		builder.append(neName);
		builder.append(", subType=");
		builder.append(subType);
		builder.append(", neRelease=");
		builder.append(neRelease);
		builder.append(", neState=");
		builder.append(neState);
		builder.append(", lastStateChange=");
		builder.append(lastStateChange);
		builder.append(", neType=");
		builder.append(neType);
		builder.append(", passwd=");
		builder.append(passwd);
		builder.append(", portNumber=");
		builder.append(portNumber);
		builder.append(", previousNeState=");
		builder.append(previousNeState);
		builder.append(", terminalId=");
		builder.append(terminalId);
		builder.append(", totalRetry=");
		builder.append(totalRetry);
		builder.append(", uid=");
		builder.append(uid);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the dbChgEvent
	 */
	protected Tl1XmlAssociationEvent getDbChgEvent() {
		return dbChgEvent;
	}

	/**
	 * @param dbChgEvent
	 *          the dbChgEvent to set
	 */
	protected void setDbChgEvent(Tl1XmlAssociationEvent dbChgEvent) {
		this.dbChgEvent = dbChgEvent;
	}

	public Double getPositionX() {
		return positionX;
	}

	public void setPositionX(Double positionX) {
		this.positionX = positionX;
	}

	public Double getPositionY() {
		return positionY;
	}

	public void setPositionY(Double positionY) {
		this.positionY = positionY;
	}
}
