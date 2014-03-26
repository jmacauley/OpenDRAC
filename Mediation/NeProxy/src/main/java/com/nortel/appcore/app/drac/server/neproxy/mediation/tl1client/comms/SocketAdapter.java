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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SocketAdapter class provides a concrete implementation for the abstract
 * CommAdapter class, for network/socket connections.
 */
public final class SocketAdapter extends AbstractCommAdapter {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final String address;
	private final int port;
	private Socket socket;

	/**
	 * Create and return a new socket adapter with the specified socket and port
	 * values. Do not create (open) the socket - that is done only when the
	 * connect() method is invoked.
	 * 
	 * @param deviceAddress
	 *          java.lang.String An IP addresss or host name, eg. "12.123.123.123"
	 *          or "bmery000"
	 * @param devicePort
	 *          int socket port number
	 */
	public SocketAdapter(String deviceAddress, int devicePort) {
		address = deviceAddress;
		port = devicePort;
	}

	/**
	 * Close the adapter's socket.
	 */
	@Override
	public void close() {
		try {
			
			if (socket != null) {
				socket.close();
			}
		}
		catch (Exception exception) {
			// do nothing - there is no need to recover from an exception here
			log.debug("Closing socket to " + address + ":" + port,
			    exception);
		}
	}

	/**
	 * get rid of this socket adapter
	 */
	@Override
	public void dispose() {
		super.dispose();
		close();
	}

	/**
	 * This method is used to retrieve the address we are connected to.
	 */
	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	/**
	 * Send a break sequence to the server.
	 * 
	 * @throws IOException
	 */
	// @Override
	// public void sendBreak()
	// throws IOException
	// {
	// char[] breakChars =
	// { (char) 255, (char) 243 };
	// write(new String(breakChars));
	// }
	/**
	 * Return the adapter's network host.
	 */
	@Override
	public String summaryString() {
		return address;
	}

	/**
	 * For debug purposes only; return a string describing the adapter.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(25);
		sb.append("SocketAdapter [");
		sb.append(address);
		sb.append(", ");
		sb.append(port);
		sb.append(']');
		return sb.toString();
	}

	@Override
	public synchronized void write(String message) throws IOException {
		writer.write(message);
		TL1Logger.logSentTl1(address, port, message);
	}

	/**
	 * Create/open the adapter's socket.
	 */
	@Override
	protected void open() throws CommAdapterException {
		try {
			socket = new Socket(address, port);
			is = new BufferedInputStream(socket.getInputStream(), readBufferSize);
			os = new BufferedOutputStream(socket.getOutputStream(), writeBufferSize);
			TL1Logger.log(address, port, "\n\n**** TL1 session opened at "
			    + new Date().toString() + " ****\n\n");
		}
		catch (IllegalArgumentException exception) {
			/*
			 * The specified ethernet port number is invalid. Try a number between 0
			 * and 65535.
			 */
			throw new CommAdapterException("socket.illegal port", exception);
		}
		catch (SecurityException exception) {
			// The current security manager does not allow socket connections.
			throw new CommAdapterException("socket.security", exception);
		}
		catch (UnknownHostException exception) {
			/*
			 * Thrown to indicate that the IP address of a host could not be
			 * determined.
			 */
			throw new CommAdapterException("socket.unknown host", exception);
		}
		catch (NoRouteToHostException exception) {
			/*
			 * Signals that an error occurred while attempting to connect a socket to
			 * a remote address and port. Typically, the remote host cannot be reached
			 * because of an intervening firewall, or if an intermediate router is
			 * down.
			 */
			throw new CommAdapterException("socket.no route to host", exception);
		}
		catch (BindException exception) {
			/*
			 * Signals that an error occurred while attempting to bind a socket to a
			 * local address and port. Typically, the port is in use, or the requested
			 * local address could not be assigned.
			 */
			throw new CommAdapterException("socket.port in use", exception);
		}
		catch (ConnectException exception) {
			/*
			 * Signals that an error occurred while attempting to connect a socket to
			 * a remote address and port. Typically, the connection was refused
			 * remotely (e.g., no process is listening on the remote address/port).
			 */
			throw new CommAdapterException("socket.connection refused", exception);
		}
		catch (SocketException exception) {
			/*
			 * Thrown to indicate that there is an error in the underlying protocol,
			 * such as a TCP error.
			 */
			throw new CommAdapterException("socket.protocol error", exception);
		}
		catch (Exception error) {
			/*
			 * catch all for unspecific errors, runtime exceptions, io exceptions
			 */
			throw new CommAdapterException("socket.unknown", error);
		}
	}

	/**
	 * Read data from the adapter's input stream. This is a blocking call.
	 */
	@Override
	protected int read(byte[] buf) {
		int available;
		try {
			available = is.read(buf, 0, buf.length);
			// used to log in MessageDispatcher
			if (available > 0) {
				TL1Logger.logRecvTl1(address, port, new String(buf, 0, available));
			}
			// 
		}
		catch (IOException exception) {
			fireConnectionDropped(new CommAdapterException(
			    "socket.connection dropped", exception));
			return -1;
		}

		if (available == -1) {
			fireConnectionDropped(new CommAdapterException(
			    "socket.connection dropped"));
		}

		return available;
	}
}
