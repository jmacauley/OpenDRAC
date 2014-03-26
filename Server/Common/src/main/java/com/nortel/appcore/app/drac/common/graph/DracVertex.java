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

package com.nortel.appcore.app.drac.common.graph;

import java.io.Serializable;

import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;

/**
 * DracVertex : A graph Vertex representing a Network element.
 * 
 * @author pitman
 */
public final class DracVertex implements Serializable {
	private static final long serialVersionUID = 1L;
	private String ieee; // aka neid
	private String label; // aka tid
	private final String ip;
	private final String port;
	private String mode;
	private NeType type;
	private final String userid;
	private final CryptedString password;
	private NeStatus status;
	private final String vid;
	private String displayString;
	private Double positionX;
	private Double positionY;

	public DracVertex(String vertexLabel, String vertexIeee, String vertixIp,
	    String vertixPort, String vertixMode, NeType vertexType,
	    String vertixUserid, CryptedString vertixPassword, NeStatus vertixStatus,
	    String vertixId, Double positionX, Double positionY) {
		label = vertexLabel;
		ieee = vertexIeee;
		ip = vertixIp;
		port = vertixPort;
		mode = vertixMode;
		type = vertexType;
		userid = vertixUserid;
		password = vertixPassword;
		status = vertixStatus;
		vid = vertixId;
		displayString = null;
		this.positionX = positionX;
		this.positionY = positionY;
	}

	/**
	 * HashCode and equals are based only on IP + port number. When a NE is fist
	 * added, we don't know its IEEE address.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof DracVertex) {
			return getUniqueId().equals(((DracVertex) o).getUniqueId());
		}
		return false;
	}

	public String getDisplayString() {
		String r = getLabel();
		if (r == null || "".equals(r.trim())) {
			r = getIp() + ":" + getPort();
		}

		if (displayString != null) {
			r = r + "<br><small>" + displayString;
		}
		return r;
	}

	public String getIeee() {
		return ieee;
	}

	public String getIp() {
		return ip;
	}

	public String getLabel() {
		return label;
	}

	public String getMode() {
		return mode;
	}

	public CryptedString getPassword() {
		return password;
	}

	public String getPort() {
		return port;
	}

	public NeStatus getStatus() {
		return status;
	}

	public NeType getType() {
		return type;
	}

	public String getUniqueId() {
		return ip + "_" + port;
	}

	public String getUserId() {
		return userid;
	}

	public String getVid() {
		return vid;
	}

	/**
	 * HashCode and equals are based only on IP + port number. When a NE is fist
	 * added, we don't know its IEEE address.
	 */
	@Override
	public int hashCode() {
		return getUniqueId().hashCode();
	}

	public void setDisplayString(String display) {
		displayString = display;
	}

	public void setIeee(String id) {
		ieee = id;
	}

	public void setLabel(String tid) {
		label = tid;
	}

	public void setMode(String mod) {
		mode = mod;
	}

	public void setStatus(NeStatus st) {
		status = st;
	}

	public void setType(NeType t) {
		type = t;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("vid ");
		sb.append(vid);

		sb.append(" IEEE ");
		sb.append(ieee);

		sb.append(" label ");
		sb.append(label);

		sb.append(" ip ");
		sb.append(ip);

		sb.append(" port ");
		sb.append(port);

		sb.append(" mode ");
		sb.append(mode);

		sb.append(" type ");
		sb.append(type);

		sb.append(" userid ");
		sb.append(userid);

		sb.append(" password ");
		sb.append(password);

		sb.append(" status ");
		sb.append(status);
		return sb.toString();
	}

	public Double getPositionX() {
		return positionX;
	}

	public void setPositionX(Double x) {
		this.positionX = x;
	}

	public Double getPositionY() {
		return positionY;
	}

	public void setPositionY(Double y) {
		this.positionY = y;
	}
}
