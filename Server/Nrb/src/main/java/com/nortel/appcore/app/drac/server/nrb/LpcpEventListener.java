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

package com.nortel.appcore.app.drac.server.nrb;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A proxy event receiver. This class lives in the NRB_PORT package and gets its RMI
 * stubs generated and added to the NRB_PORT jar file and becomes available to the
 * rmiregistry which has the nrb jar file in its class path.
 * 
 * @author pitman
 * @since Nov 2010
 */

public final class LpcpEventListener implements LpcpEventCallback {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final LpcpEventCallback cb;

	public LpcpEventListener(LpcpEventCallback theRealCallback) {
		cb = theRealCallback;
		try {
			UnicastRemoteObject.exportObject(this);
			/*
			 * NOTE: Use public static Remote exportObject(Remote obj, int port) if
			 * you want to use a fixed port number to communicate with.
			 */
		}
		catch (RemoteException e) {
			log.error(
			    "Failed to export this object for use as a RMI callbak! ", e);
		}
	}

	@Override
	public void lpcpEventReceived(LpcpEvent e) throws Exception {
		try {
			log.debug("LpcpEventReceived: " + e);
			cb.lpcpEventReceived(e);
		}
		catch (Exception t) {
			log.error("Error: ", t);
		}
	}
}
