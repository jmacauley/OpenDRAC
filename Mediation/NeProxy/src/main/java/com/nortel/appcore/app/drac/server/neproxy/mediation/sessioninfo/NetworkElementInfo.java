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

package com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;

public final class NetworkElementInfo {
  private final Logger log = LoggerFactory.getLogger(getClass());
	public static final String PARENT = "com.nortel.appcore.app.drac.server.neproxy.mediation.tl1wrapper.";
	public static final String COMMON_TL1WRAPPER = PARENT + "common.";

	private String neID;
	private String neName;
	private NeType neType = NeType.UNKNOWN;
	private NETWORK_ELEMENT_MODE neMode = NETWORK_ELEMENT_MODE.Unknown;
	private String adapterName;
	private final UserProfile userProfile;
	private final IPAddressInfo addressInfo;

	public static final String TCP = "TCP";
	public static final String TELNET = "TELNET";

	/**
	 * NetworkElementInfo constructor comment.
	 */
	public NetworkElementInfo(String id, String tid, IPAddressInfo ipInfo,
	    UserProfile user) {
		neID = id;
		neName = tid;
		adapterName = COMMON_TL1WRAPPER; // updated as we login
		addressInfo = ipInfo;
		userProfile = user;
		
	}

	/**
     * 
     */
	public String getAdapterName() {
		return adapterName;
	}

	/**
     * 
     */
	public IPAddressInfo getAddressInfo() {
		return addressInfo;
	}

	/**
     * 
     */
	public String getNeID() {
		return neID;
	}

	/**
     * 
     */
	public NETWORK_ELEMENT_MODE getNeMode() {
		return neMode;
	}

	/**
     * 
     */
	public String getNeName() {
		return neName.replaceAll("\"", "");
	}

	/**
     * 
     */

	public String getNetworkElementID() {
		return neID;
	}

	/**
     * 
     */

	public String getNetworkElementName() {
		return neName;
	}

	/**
     * 
     */

	public NeType getNeType() {
		return this.neType;
	}

	/**
     *
     */

	public UserProfile getUserProfile() {
		return userProfile;
	}

	/**
     * 
     */

	public void setAdapterName(String newAdapterName) {
		if (!adapterName.equals(newAdapterName)) {
			log.debug("Seting adapter name from " + adapterName + " to "
			    + newAdapterName + " on " + toString());
			adapterName = newAdapterName;
		}
	}

	/**
     * 
     */
	public void setNeID(String newNeID) {
		neID = newNeID;
	}

	/**
     * 
     */
	public void setNeMode(NETWORK_ELEMENT_MODE mode) {
		neMode = mode;
	}

	/**
     * 
     */
	public void setNeName(String newNeName) {
		neName = newNeName;
	}

	/**
     * 
     */
	public void setNeType(NeType type) {
		neType = type;
	}

	@Override
	public String toString() {
		return "NetworkElementInfo: neID:" + neID + " neName:" + neName
		    + " neType:" + neType + " neMode:" + neMode + " adapterName:"
		    + adapterName + " userProfile:" + userProfile + " addressInfo:"
		    + addressInfo;
	}

}
