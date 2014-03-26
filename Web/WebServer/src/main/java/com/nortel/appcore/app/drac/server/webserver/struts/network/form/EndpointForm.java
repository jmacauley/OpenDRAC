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

package com.nortel.appcore.app.drac.server.webserver.struts.network.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on 26-Jul-06
 */
public final class EndpointForm extends ValidatorForm implements Comparable {
	private static final long serialVersionUID = 7009456210941076877L;
	// Query fields
	public String group = DracConstants.EMPTY_STRING;
	public List<UserGroupName> groupList = new ArrayList<UserGroupName>();
	public String layerFilter = DracConstants.LAYER2_STRING;

	// Common fields
	public String id = DracConstants.EMPTY_STRING;
	public String name = DracConstants.EMPTY_STRING;
	public String label = DracConstants.EMPTY_STRING;
	public String portType = DracConstants.EMPTY_STRING;
	public String layer = DracConstants.EMPTY_STRING;
	public String state = DracConstants.EMPTY_STRING;
	public String signalingType = DracConstants.EMPTY_STRING;
	public Map attributes = new HashMap();

	// Layer2 fields
	public String dataRate = DracConstants.EMPTY_STRING;
	public String mtu = DracConstants.EMPTY_STRING;
	public String physAddr = DracConstants.EMPTY_STRING;
	public String vcat = DracConstants.EMPTY_STRING;

	// Layer1 fields
	public String cost = DracConstants.EMPTY_STRING;
	public String metric = DracConstants.EMPTY_STRING;
	public String srlg = DracConstants.EMPTY_STRING;

	// Layer0 fields
	public String wavelength = DracConstants.EMPTY_STRING;

	// Edit MTU field
	public String newMtu = DracConstants.EMPTY_STRING;

	@Override
	public int compareTo(Object o) {
		if (o instanceof EndpointForm) {
			EndpointForm anotherForm = (EndpointForm) o;
			return this.name.compareTo(anotherForm.getName());
		}
		return 1;
	}

	/**
	 * @return the attrSet
	 */
	public Map getAttributes() {
		return attributes;
	}

	/**
	 * @return the cost
	 */
	public String getCost() {
		return cost;
	}

	/**
	 * @return the dataRate
	 */
	public String getDataRate() {
		return dataRate;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @return the groupList
	 */
	public List<UserGroupName> getGroupList() {
		return groupList;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * @return the layer
	 */
	public String getLayer() {
		return layer;
	}

	/**
	 * @return the layerFilter
	 */
	public String getLayerFilter() {
		return layerFilter;
	}

	/**
	 * @return the metric
	 */
	public String getMetric() {
		return metric;
	}

	/**
	 * @return the mtu
	 */
	public String getMtu() {
		return mtu;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the newMtu
	 */
	public String getNewMtu() {
		return newMtu;
	}

	/**
	 * @return the physAddr
	 */
	public String getPhysAddr() {
		return physAddr;
	}

	/**
	 * @return the type
	 */
	public String getPortType() {
		return portType;
	}

	/**
	 * @return the portType
	 */
	public String getSignalingType() {
		return signalingType;
	}

	/**
	 * @return the srlg
	 */
	public String getSrlg() {
		return srlg;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	public String getVcat() {
		return vcat;
	}

	public String getWavelength() {
		return wavelength;
	}

	/**
	 * @param attrSet
	 *          the attrSet to set
	 */
	public void setAttributes(Map attrSet) {
		this.attributes = attrSet;
	}

	/**
	 * @param cost
	 *          the cost to set
	 */
	public void setCost(String cost) {
		this.cost = cost;
	}

	/**
	 * @param dataRate
	 *          the dataRate to set
	 */
	public void setDataRate(String dataRate) {
		this.dataRate = dataRate;
	}

	/**
	 * @param group
	 *          the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @param groupList
	 *          the groupList to set
	 */
	public void setGroupList(List<UserGroupName> groupList) {
		this.groupList = groupList;
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param layer
	 *          the layer to set
	 */
	public void setLayer(String layer) {
		this.layer = layer;
	}

	/**
	 * @param layerFilter
	 *          the layerFilter to set
	 */
	public void setLayerFilter(String layerFilter) {
		this.layerFilter = layerFilter;
	}

	/**
	 * @param metric
	 *          the metric to set
	 */
	public void setMetric(String metric) {
		this.metric = metric;
	}

	/**
	 * @param mtu
	 *          the mtu to set
	 */
	public void setMtu(String mtu) {
		this.mtu = mtu;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param newMtu
	 *          the newMtu to set
	 */
	public void setNewMtu(String newMtu) {
		this.newMtu = newMtu;
	}

	/**
	 * @param physAddr
	 *          the physAddr to set
	 */
	public void setPhysAddr(String physAddr) {
		this.physAddr = physAddr;
	}

	/**
	 * @param type
	 *          the type to set
	 */
	public void setPortType(String type) {
		this.portType = type;
	}

	/**
	 * @param portType
	 *          the portType to set
	 */
	public void setSignalingType(String portType) {
		this.signalingType = portType;
	}

	/**
	 * @param srlg
	 *          the srlg to set
	 */
	public void setSrlg(String srlg) {
		this.srlg = srlg;
	}

	/**
	 * @param state
	 *          the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	public void setVcat(String vcat) {
		this.vcat = vcat;
	}

	public void setWavelength(String wavelength) {
		this.wavelength = wavelength;
	}
}
