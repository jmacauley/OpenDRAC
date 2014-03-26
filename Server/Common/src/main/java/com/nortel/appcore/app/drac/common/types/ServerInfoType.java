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

/**
 * Created on Dec 5, 2005
 * 
 * @author nguyentd
 */
public final class ServerInfoType implements Serializable {
	private static final long serialVersionUID = 1;
	private ControllerType controllerInfo = new ControllerType();
	private OsType osInfo = new OsType("unknown", "unknown", "unknown");
	private JvmType vmInfo = new JvmType("unknown", "unknown", "unknown");

	// A value defining minutes to add to start and end of schedule to account for
	// provisioning time
	private int systemOverhead;

	/**
	 * @return the controllerInfo
	 */
	public ControllerType getControllerInfo() {
		return controllerInfo;
	}

	/**
	 * @return the osInfo
	 */
	public OsType getOsInfo() {
		return osInfo;
	}

	/**
	 * @return the systemOverhead
	 */
	public int getSystemOverhead() {
		return systemOverhead;
	}

	/**
	 * @return the vmInfo
	 */
	public JvmType getVmInfo() {
		return vmInfo;
	}

	/**
	 * @param controllerInfo
	 *          the controllerInfo to set
	 */
	public void setControllerInfo(ControllerType controllerInfo) {
		this.controllerInfo = controllerInfo;
	}

	/**
	 * @param osInfo
	 *          the osInfo to set
	 */
	public void setOsInfo(OsType osInfo) {
		this.osInfo = osInfo;
	}

	/**
	 * @param systemOverhead
	 *          the systemOverhead to set
	 */
	public void setSystemOverhead(int systemOverhead) {
		this.systemOverhead = systemOverhead;
	}

	/**
	 * @param vmInfo
	 *          the vmInfo to set
	 */
	public void setVmInfo(JvmType vmInfo) {
		this.vmInfo = vmInfo;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(controllerInfo);
		builder.append(" systemOverhead=");
		builder.append(systemOverhead);
		builder.append(" ");
		builder.append(osInfo);
		builder.append(" ");
		builder.append(vmInfo);
		return builder.toString();
	}

	// @Override
	// public String toString()
	// {
	// return controllerInfo.toString() + "\n" + osInfo + "\n" + vmInfo;
	//
	// }

}
