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

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;

/**
 * Holds all the important data about a network element and is what we persist
 * into the database. Common across TL1, SNMP, etc NE types.
 * 
 * @author pitman
 */
public final class NetworkElementHolder implements Serializable {
	public enum NETWORK_ELEMENT_MODE {
		Unknown, SONET, SDH;
	}

	public enum PROTOCOL_TYPE {
		/*
		 * For each protocol record its name and the default port number used to
		 * talk that protocol. The default port numbers are pre-populated into the
		 * gui once the protocol has been chosen.
		 */
		NETL1_PROTOCOL("tl1", 10001), NESNMP_PROTOCOL("snmp", 161), NEGMPLS_PROTOCOL(
		    "gmpls", 8888), NELOCAL_PROTOCOL("local", 1111), NEASTN_PROTOCOL(
		    "astn", 9999), FORCE10_PROTOCOL("force10", 22);

		private final String type;
		private final int defaultPort;

		PROTOCOL_TYPE(String s, int defaultPortNumber) {
			type = s;
			defaultPort = defaultPortNumber;
		}

		public static PROTOCOL_TYPE fromString(String s) throws Exception {
			for (PROTOCOL_TYPE p : PROTOCOL_TYPE.values()) {
				if (p.name().equalsIgnoreCase(s)) {
					return p;
				}
				if (p.type.equalsIgnoreCase(s)) {
					return p;
				}
			}
			throw new Exception("Cannot convert '" + s + "' to a PROTOCOL_TYPE enum");
		}

		public String asString() {
			return type;
		}

		public int getDefaultPortNumber() {
			return defaultPort;
		}

		@Override
		public String toString() {
			return type;
		}
	}

	private static final long serialVersionUID = 1L;

	private final String ip;
	private final String port;
	private final String userId;
	private final NeType type;
	private final String tid;
	private final String status;
	private final NeStatus neStatus;
	private final CryptedString password;
	private final int neIndex;
	private final NETWORK_ELEMENT_MODE mode;
	private final String managedBy;
	private final String id;
	private final PROTOCOL_TYPE commProtocol;
	private final String subType;
	private final String neRelease;
	private final boolean autoReDiscover;
	private final Double positionX;
	private final Double positionY;

        public NetworkElementHolder(String neIp, String nePort, String neUserId,
            NeType neType, String neTid, NeStatus networkElementStatus,
            String neStatusString, CryptedString nePassword, int neNeIndex,
            NETWORK_ELEMENT_MODE neMode, String neManagedBy, String neId,
            PROTOCOL_TYPE neCommProtocol, boolean neAutoReDiscover,
            String neNeRelease, Double positionX, Double positionY) {
                ip = neIp;
                port = nePort;
                userId = neUserId;
                type = neType;
                tid = neTid;
                neStatus = networkElementStatus;
                status = neStatusString; // can be null or contain a time stamp of when the
                                         // current status was set.
                password = nePassword;
                neIndex = neNeIndex;
                mode = neMode;
                managedBy = neManagedBy;
                id = neId;
                commProtocol = neCommProtocol;
                autoReDiscover = neAutoReDiscover;
                subType = "Unknown";
                neRelease = neNeRelease;
                this.positionX = positionX;
                this.positionY = positionY;
	}

	public NetworkElementHolder(String neIp, String nePort, String neUserId,
	    NeType neType, String neTid, NeStatus networkElementStatus,
	    String neStatusString, CryptedString nePassword, int neNeIndex,
	    NETWORK_ELEMENT_MODE neMode, String neManagedBy, String neId,
	    PROTOCOL_TYPE neCommProtocol, boolean neAutoReDiscover,
            String subType,
	    String neNeRelease, Double positionX, Double positionY) {
		ip = neIp;
		port = nePort;
		userId = neUserId;
		type = neType;
		tid = neTid;
		neStatus = networkElementStatus;
		status = neStatusString; // can be null or contain a time stamp of when the
		                         // current status was set.
		password = nePassword;
		neIndex = neNeIndex;
		mode = neMode;
		managedBy = neManagedBy;
		id = neId;
		commProtocol = neCommProtocol;
		autoReDiscover = neAutoReDiscover;
		this.subType = subType;
		neRelease = neNeRelease;
		this.positionX = positionX;
		this.positionY = positionY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof NetworkElementHolder)) {
			return false;
		}
		NetworkElementHolder other = (NetworkElementHolder) obj;
		if (autoReDiscover != other.autoReDiscover) {
			return false;
		}
		if (commProtocol == null) {
			if (other.commProtocol != null) {
				return false;
			}
		}
		else if (!commProtocol.equals(other.commProtocol)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		}
		else if (!id.equals(other.id)) {
			return false;
		}
		if (ip == null) {
			if (other.ip != null) {
				return false;
			}
		}
		else if (!ip.equals(other.ip)) {
			return false;
		}
		if (managedBy == null) {
			if (other.managedBy != null) {
				return false;
			}
		}
		else if (!managedBy.equals(other.managedBy)) {
			return false;
		}
		if (mode == null) {
			if (other.mode != null) {
				return false;
			}
		}
		else if (!mode.equals(other.mode)) {
			return false;
		}
		if (neIndex != other.neIndex) {
			return false;
		}
                if (subType == null) {
                        if (other.subType != null) {
                                return false;
                        }
                }
                else if (!subType.equals(other.subType)) {
                        return false;
                }

		if (neRelease == null) {
			if (other.neRelease != null) {
				return false;
			}
		}
		else if (!neRelease.equals(other.neRelease)) {
			return false;
		}
		if (neStatus == null) {
			if (other.neStatus != null) {
				return false;
			}
		}
		else if (!neStatus.equals(other.neStatus)) {
			return false;
		}
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		}
		else if (!password.equals(other.password)) {
			return false;
		}
		if (port == null) {
			if (other.port != null) {
				return false;
			}
		}
		else if (!port.equals(other.port)) {
			return false;
		}
		if (status == null) {
			if (other.status != null) {
				return false;
			}
		}
		else if (!status.equals(other.status)) {
			return false;
		}
		if (tid == null) {
			if (other.tid != null) {
				return false;
			}
		}
		else if (!tid.equals(other.tid)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		}
		else if (!type.equals(other.type)) {
			return false;
		}
		if (userId == null) {
			if (other.userId != null) {
				return false;
			}
		}
		else if (!userId.equals(other.userId)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the commProtocol
	 */
	public PROTOCOL_TYPE getCommProtocol() {
		return commProtocol;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @return the managedBy
	 */
	public String getManagedBy() {
		return managedBy;
	}

	/**
	 * @return the mode
	 */
	public NETWORK_ELEMENT_MODE getMode() {
		return mode;
	}

	/**
	 * @return the neIndex
	 */
	public int getNeIndex() {
		return neIndex;
	}

        public String getSubType() {
                return subType;
        }

	public String getNeRelease() {
		return neRelease;
	}

	public NeStatus getNeStatus() {
		return neStatus;
	}

	/**
	 * @return the status
	 */
	public String getNeStatusString() {
		return status;
	}

	public String getNeStatusWithDate() {
		if (status == null) {
			return neStatus.toString();
		}
		return neStatus + " " + status;
	}

	/**
	 * @return the encrypted password
	 */
	public CryptedString getPassword() {
		return password;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @return the tid
	 */
	public String getTid() {
		return tid;
	}

	/**
	 * @return the type
	 */
	public NeType getType() {
		return type;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (autoReDiscover ? 1231 : 1237);
		result = prime * result
		    + (commProtocol == null ? 0 : commProtocol.hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (ip == null ? 0 : ip.hashCode());
		result = prime * result + (managedBy == null ? 0 : managedBy.hashCode());
		result = prime * result + (mode == null ? 0 : mode.hashCode());
		result = prime * result + neIndex;
		result = prime * result + (subType == null ? 0 : subType.hashCode());
		result = prime * result + (neRelease == null ? 0 : neRelease.hashCode());
		result = prime * result + (neStatus == null ? 0 : neStatus.hashCode());
		result = prime * result + (password == null ? 0 : password.hashCode());
		result = prime * result + (port == null ? 0 : port.hashCode());
		result = prime * result + (status == null ? 0 : status.hashCode());
		result = prime * result + (tid == null ? 0 : tid.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
		result = prime * result + (userId == null ? 0 : userId.hashCode());
		return result;
	}

	/**
	 * @return the autoReDiscover
	 */
	public boolean isAutoReDiscover() {
		return autoReDiscover;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NetworkElementHolder [ip=");
		builder.append(ip);
		builder.append(", port=");
		builder.append(port);
		builder.append(", tid=");
		builder.append(tid);
		builder.append(", commProtocol=");
		builder.append(commProtocol);
		builder.append(", id=");
		builder.append(id);
		builder.append(", managedBy=");
		builder.append(managedBy);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", neIndex=");
		builder.append(neIndex);
		builder.append(", subType=");
		builder.append(subType);
		builder.append(", neRelease=");
		builder.append(neRelease);
		builder.append(", neStatus=");
		builder.append(neStatus);
		builder.append(", status=");
		builder.append(status);
		builder.append(", password=");
		builder.append(password);
		builder.append(", type=");
		builder.append(type);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", autoReDiscover=");
		builder.append(autoReDiscover);
		builder.append("]");
		return builder.toString();
	}

	public Double getPositionX() {
		return positionX;
	}

	public Double getPositionY() {
		return positionY;
	}
}
