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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * CommAdapterException is used as a "marker" - it is always guaranteed to have
 * a localized error message, so may be used directly to populate an error
 * dialog. It's toString() method returns its localized message - this would be
 * ideal to populate an error dialog...</p> CommAdapterException also provides
 * support for error logging. CommAdapterException can store a throwable (the
 * exception it wraps and for which it provides localization), which may be
 * retrieved later during logging with getSource(). Instances may also be
 * supplied with debug information object.
 */
final class CommAdapterException extends IOException {
	private static final long serialVersionUID = 5481554710721672922L;

	/** If this exception wraps another, source is the original exception. */
	private final Throwable source;

	/** a key value mapping of errors */
	private static final Map<String, String> ERR = new HashMap<String, String>();

	static {

		/*
		 * this unfortunate mapping exists to remove any dependence on swing or awt
		 * via the old ResourceBundleAdpter class in an ideal world we would remove
		 * this, but it's just not worth the root canal-like pain it would be to do
		 * so.
		 */

		ERR.put(
		    "socket.illegal port",
		    "Could not connect to the specified network port. The port should be between 0 and 65535.");
		ERR.put("socket.security",
		    "The current java security policy does not allow network connections.");
		ERR.put("socket.unknown host", "The specified host name can not be found.");
		ERR.put(
		    "socket.no route to host",
		    "The remote host can not be reached. The host can be unreachable because of an intervening firewall or an intermediate router.");
		ERR.put(
		    "socket.port in use",
		    "The specified network address or port can not be used. The port may be in use, or the address can not be assigned.");
		ERR.put("socket.connection refused",
		    "The connection is refused. Check your network settings.");

		ERR.put(
		    "socket.connection dropped",
		    "The network connection was dropped. Please check the connection and network, and reconnect.");
		ERR.put(
		    "socket.write error",
		    "An error occurred writing data to the network connection. Please check the connection.");
		ERR.put(
		    "socket.read error",
		    "An error occurred reading data from the network connection. Please check the connection.");
		ERR.put(
		    "socket.protocol error",
		    "Could not establish communication via network. Check the network connection and settings.");
		ERR.put(
		    "socket.unknown error",
		    "An unknown network connection error has occurred. Please check the connection and server.");

	}

	/**
	 * Create a new exception. The exception's message will be set to the
	 * localized message from CommsResources stored under the specified error
	 * message key. The exception will have no original throwable or extra debug
	 * information.
	 */
	public CommAdapterException(String errorMessageKey) {
		super(getErrorMapping(errorMessageKey));
		source = null;
	}

	/**
	 * Create a new exception. The exception's message will be set to the
	 * localized message from CommsResources stored under the specified error
	 * message key. The new INSTANCE will store the specified debug string, and
	 * the specified throwable as its source.
	 */
	public CommAdapterException(String errorMessageKey, Throwable error) {
		super(getErrorMapping(errorMessageKey));
		this.initCause(error);
		source = error;
	}

	/**
	 * this replaces our resource bundle adapter thing
	 */
	private static String getErrorMapping(String key) {
		String result = ERR.get(key);
		if (result == null) {
			return "unknown " + key;
		}
		return result;
	}

	/**
	 * Return the error message.
	 */
	@Override
	public String toString() {

		if (source == null) {
			return getMessage();
		}

		return getMessage() + "\n" + source;
	}
}
