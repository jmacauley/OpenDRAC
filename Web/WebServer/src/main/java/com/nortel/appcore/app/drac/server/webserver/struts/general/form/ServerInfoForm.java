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

package com.nortel.appcore.app.drac.server.webserver.struts.general.form;

import org.apache.struts.validator.ValidatorActionForm;

public final class ServerInfoForm extends ValidatorActionForm {
	private static final long serialVersionUID = 1L;
	public static final String SERVER1 = "server1";
	public static final String SERVER2 = "server2";

	private String redundancyMode = " ";
	private String server1Ip = " ";
	private String server1Version = " ";
	private String server1Config = " ";
	private String server1State = " ";
	private String server1Mode = " ";
	private String server2Ip = " ";
	private String server2Version = " ";
	private String server2Config = " ";
	private String server2State = " ";
	private String server2Mode = " ";
	private String forceSwitchTo = "";

	public String getForceSwitchTo() {
		return forceSwitchTo;
	}

	public String getRedundancyMode() {
		return redundancyMode;
	}

	public String getServer1Config() {
		return server1Config;
	}

	public String getServer1Ip() {
		return server1Ip;
	}

	public String getServer1Mode() {
		return server1Mode;
	}

	public String getServer1State() {
		return server1State;
	}

	public String getServer1Version() {
		return server1Version;
	}

	public String getServer2Config() {
		return server2Config;
	}

	public String getServer2Ip() {
		return server2Ip;
	}

	public String getServer2Mode() {
		return server2Mode;
	}

	public String getServer2State() {
		return server2State;
	}

	public String getServer2Version() {
		return server2Version;
	}

	public void setForceSwitchTo(String forceSwitchTo) {
		this.forceSwitchTo = forceSwitchTo;
	}

	public void setRedundancyMode(String redundancyMode) {
		this.redundancyMode = redundancyMode;
	}

	public void setServer1Config(String server1Config) {
		this.server1Config = server1Config;
	}

	public void setServer1Ip(String server1Ip) {
		this.server1Ip = server1Ip;
	}

	public void setServer1Mode(String server1Mode) {
		this.server1Mode = server1Mode;
	}

	public void setServer1State(String server1State) {
		this.server1State = server1State;
	}

	public void setServer1Version(String server1Version) {
		this.server1Version = server1Version;
	}

	public void setServer2Config(String server2Config) {
		this.server2Config = server2Config;
	}

	public void setServer2Ip(String server2Ip) {
		this.server2Ip = server2Ip;
	}

	public void setServer2Mode(String server2Mode) {
		this.server2Mode = server2Mode;
	}

	public void setServer2State(String server2State) {
		this.server2State = server2State;
	}

	public void setServer2Version(String server2Version) {
		this.server2Version = server2Version;
	}

}
