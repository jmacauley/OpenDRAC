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

import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckable;

/**
 * Created on Dec 5, 2005
 * 
 * @author nguyentd
 */
public final class ControllerType implements Serializable, PolicyCheckable {
	private static final long serialVersionUID = 1;
	private String id = "unknown";
	private String ipAddress = System.getProperty("org.opendrac.controller.primary", "localhost");
	private String port = "0";
	private String version = "unknown";
	private String status = "unknown";
	private int faults;
	private int connectionHandled;
	private int connectionActive;
	private String timeZoneId;

	public int getConnectionActive() {
		return connectionActive;
	}

	public int getConnectionHandled() {
		return connectionHandled;
	}

	public int getFaults() {
		return faults;
	}

	public String getId() {
		return id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getPort() {
		return port;
	}

	public String getStatus() {
		return status;
	}

	public String getTimeZoneId() {
		return timeZoneId;
	}

	public String getVersion() {
		return version;
	}

	public void setConnectionActive(int connectionActive) {
		this.connectionActive = connectionActive;
	}

	public void setConnectionHandled(int connectionHandled) {
		this.connectionHandled = connectionHandled;
	}

	public void setFaults(int faults) {
		this.faults = faults;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "Id=" + id + " IP Address=" + ipAddress + " port=" + port
		    + " version=" + version + " status=" + status + " faults=" + faults
		    + " connectionHandled=" + connectionHandled + " connectionActive="
		    + connectionActive;
	}
}
