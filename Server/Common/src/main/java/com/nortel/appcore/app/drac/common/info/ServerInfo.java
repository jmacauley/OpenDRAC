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

package com.nortel.appcore.app.drac.common.info;

public final class ServerInfo {

	private String ipAddress;
	private String redundantIpAddress;
	private ServerState serverState = ServerState.INACTIVE;
	private ServerRelationship mode = ServerRelationship.UNKNOWN;
	private ServerConfigType serverConfig = ServerConfigType.UNKNOWN;
	private final String softwareVersion = System.getProperty("org.opendrac.version");

	public enum ServerRelationship {
		UNKNOWN, PRIMARY, SECONDARY
	}

	public static enum ServerState {
		ACTIVE, INACTIVE
	}

	public enum ServerConfigType {
		UNKNOWN, STANDALONE, REDUNDANT
	}

	public ServerInfo(String address) {
		ipAddress = address;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public ServerRelationship getMode() {
		return mode;
	}

	public String getRedundantIpAddress() {
		return this.redundantIpAddress;
	}

	public ServerConfigType getServerConfig() {
		return serverConfig;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public ServerState getState() {
		return serverState;
	}

	public void setMode(ServerRelationship mode) {
		this.mode = mode;
	}

	public void setRedundantIpAddress(String redundantIpAddress) {
		this.redundantIpAddress = redundantIpAddress;
	}

	public void setServerConfig(ServerConfigType serverConfig) {
		this.serverConfig = serverConfig;
	}

	public void setState(ServerState serverState) {
		this.serverState = serverState;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServerInfo [ipAddress=");
		builder.append(ipAddress);
		builder.append(", redundantIpAddress=");
		builder.append(redundantIpAddress);
		builder.append(", serverState=");
		builder.append(serverState);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", serverConfig=");
		builder.append(serverConfig);
		builder.append(", softwareVersion=");
		builder.append(softwareVersion);
		builder.append("]");
		return builder.toString();
	}
}