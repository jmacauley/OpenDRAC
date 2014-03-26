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

package com.nortel.appcore.app.drac.server.neproxy.command.cpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;

public final class DeleteConnectionTask {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final NetworkElement ne;
	private final TL1RequestMessage message;
	private final Tl1XmlDbChangeEvent dbchgEvent;
	private final String fromAid;
	private final String toAid;
	private final String fromShelf;
	private final String toShelf;
	private final String callId;

	public DeleteConnectionTask(NetworkElement networkElement,
	    TL1RequestMessage tl1RequestMessage, Tl1XmlDbChangeEvent tl1XmlDbChangeEvent,
	    String neFromAid, String neToAid, String callId) {
		log.debug("CPL DeleteConnectionTask: Created " + networkElement + " "
		    + tl1RequestMessage + " " + tl1XmlDbChangeEvent + " " + neFromAid + " "
		    + neToAid);
		ne = networkElement;
		message = tl1RequestMessage;
		dbchgEvent = tl1XmlDbChangeEvent;
		fromAid = neFromAid;
		toAid = neToAid;
		fromShelf = neFromAid.split("-")[1];
		toShelf = neToAid.split("-")[1];

		this.callId = callId;
	}

	public String getCallId() {
		return callId;
	}

	/**
	 * @return the fromShelf
	 */
	public String getFromShelf() {
		return fromShelf;
	}

	/**
	 * @return the toShelf
	 */
	public String getToShelf() {
		return toShelf;
	}

	public boolean sendCommand() {
		log.debug("CPL DeleteConnectionTask: sendCommand() invoked");
		TL1ResponseMessage response = null;
		try {
			log.debug("CPL DeleteConnectionTask: Sending the Delete Connection ("
			    + message + ") to the NE " + ne.getNeName() + " in 60 sec for "
			    + toString());
			Thread.sleep(60000);

			response = ne.getTl1Session().sendSyncMessage(message);
			if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
				log.debug("CPL DeleteConnectionTask: Connection deleted " + toString());
				// The CPL connections are never added to the database; this was
				// commented out in ReptCrs
				// (outstanding
				// question as to 'why'?). Consequently. the attempt in the sendEvent
				// call to delete the
				// connection
				// fails, which then returns a false flag back to the TL1 command sender
				// ... thus not
				// returning
				// successfully out of the TL1 send loop. Let's break this dependance.
				// If the TL1 sends,
				// return true.

				// return sendEvent();
				sendEvent();
				return true;
			}
		}
		catch (Exception e) {
			String errorCode = e.getMessage().split(":")[0];
			log.error("CPL DeleteConnectionTask: Failed in DLT_CRS: " + errorCode
			    + " " + toString(), e);
			if (errorCode.equalsIgnoreCase("SNVS")) {
				log.debug("CPL DeleteConnectionTask: Start the retry of  DLT-CRS (5 times, 30 second sleep) for "
				    + toString());
				for (int i = 1; i <= 5; i++) {
					try {
						Thread.sleep(30 * 1000);
						log.debug("CPL DeleteConnectionTask: Retry " + i + " times for "
						    + toString());
						response = ne.getTl1Session().sendSyncMessage(message);
						if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
							log.debug("CPL DeleteConnectionTask: Connection deleted for "
							    + toString());
							return sendEvent();
						}
					}
					catch (Exception ie) {
						log.error("CPL DeleteConnectionTask: Failed in retry for "
						    + toString(), ie);
						String inErrorCode = ie.getMessage().split(":")[0];
						if (!inErrorCode.equalsIgnoreCase("SNVS")) {
							log.debug("CPL DeleteConnectionTask: returning false "
							    + toString());
							return false;
						}
					}
				}
			}
		}
		log.debug("CPL DeleteConnectionTask: returning false " + toString());
		return false;
	}

	@Override
	public String toString() {
		return " DeleteConnectionTask: neName: " + ne.getNeName() + " ne:" + ne
		    + " ToAid:" + toAid + " fromAid:" + fromAid;

	}

	private boolean sendEvent() {
		try {
			String timeDate = new java.sql.Timestamp(System.currentTimeMillis())
			    .toString();
			dbchgEvent.setOccurrentDate(timeDate.substring(2, 10));
			dbchgEvent.setOccurrentTime(timeDate.substring(11, 19));
			String existConnection = DbUtility.INSTANCE.retrieveAXConnect(ne,
			    fromAid, toAid);
			if (existConnection == null) {
				log.error("CPL DeleteConnectionTask: connection " + fromAid + ":"
				    + toAid + " doesn't exist " + toString());
				log.debug("CPL DeleteConnectionTask: returning false for " + toString());
				return false;
			}
			DbUtility.INSTANCE.deleteXConnect(ne, fromAid, toAid);
			// dbchgEvent.addDataElement(data);
			log.debug("CPL DeleteConnectionTask: Local generate DLT-CRS event: "
			    + dbchgEvent.getEventData() + " for  " + toString());

			Thread.sleep(2000);

			NeProxy.generateEvent(dbchgEvent, ClientMessageXml.DBCHG_EVENT_VALUE);
			log.debug("CPL DeleteConnectionTask: returning true for " + toString());
			return true;
		}
		catch (Exception e) {
			log.error("CPL DeleteConnectionTask: Failed to generate event "
			    + toString(), e);
		}
		log.debug("CPL DeleteConnectionTask: returning false for " + toString());
		return false;
	}
}