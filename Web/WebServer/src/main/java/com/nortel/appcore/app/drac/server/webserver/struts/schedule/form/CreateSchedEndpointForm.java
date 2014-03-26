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

package com.nortel.appcore.app.drac.server.webserver.struts.schedule.form;

import org.apache.struts.validator.ValidatorForm;

/**
 * Created on 9-Nov-06
 */
public final class CreateSchedEndpointForm extends ValidatorForm implements
    Comparable {
	private static final long serialVersionUID = 223629120965464233L;
	private String layer = "";
	private String name = "";
	private String availableChannels = "";
	private String groups = "";

	@Override
	public int compareTo(Object o) {
		if (o instanceof CreateSchedEndpointForm) {
			String myName = this.getName();
			String otherName = ((CreateSchedEndpointForm) o).getName();
			return myName.compareTo(otherName);
		}
		return 1;
	}

	/**
	 * @return the availableChannels
	 */
	public String getAvailableChannels() {
		return availableChannels;
	}

	/**
	 * @return the groups
	 */
	public String getGroups() {
		return groups;
	}

	/**
	 * @return the layer
	 */
	public String getLayer() {
		return layer;
	}

	/**
	 * @return the tna
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param availableChannels
	 *          the availableChannels to set
	 */
	public void setAvailableChannels(String availableChannels) {
		this.availableChannels = availableChannels;
	}

	/**
	 * @param groups
	 *          the groups to set
	 */
	public void setGroups(String groups) {
		this.groups = groups;
	}

	/**
	 * @param layer
	 *          the layer to set
	 */
	public void setLayer(String layer) {
		this.layer = layer;
	}

	/**
	 * @param tna
	 *          the tna to set
	 */
	public void setName(String tna) {
		this.name = tna;
	}

}
