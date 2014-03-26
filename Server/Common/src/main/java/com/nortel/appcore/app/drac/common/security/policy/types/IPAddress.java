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

package com.nortel.appcore.app.drac.common.security.policy.types;

import java.io.Serializable;

public final class IPAddress implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String address;
	private final String port;

	public IPAddress(String ipAddress, String ipPort) {
		address = ipAddress;
		port = ipPort;
	}

	public static IPAddress fromString(String ipAndPort) {

		if (ipAndPort == null) {
			return null;
		}

		int indexOfAt = ipAndPort.indexOf(':');

		if (indexOfAt < 0) {
			return new IPAddress(ipAndPort, null);
		}

		return new IPAddress(ipAndPort.substring(0, indexOfAt),
		    ipAndPort.substring(indexOfAt + 1, ipAndPort.length()));
	}


	public String getAddress() {
		return address;
	}

	public String getPort() {
		return port;
	}
	

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		buf.append(address);

		if (port != null && !port.equals("")) {
			buf.append(":");
			buf.append(port);
		}

		return buf.toString();
	}

}
