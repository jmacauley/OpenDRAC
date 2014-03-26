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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.ServerResponseI;
import com.nortel.appcore.app.drac.common.utility.GenericJdomParser;
import com.nortel.appcore.app.drac.server.nrb.LpcpEvent;
import com.nortel.appcore.app.drac.server.nrb.LpcpEventCallback;
import com.nortel.appcore.app.drac.server.nrb.LpcpEventListener;

/**
 * ServerMonitor This class monitors the connection between the DRAC
 * Administration Desktop and the DRAC server. Events are handled through a
 * callback mechanism to the main OpenDracDesktop class.
 * 
 * @author adlee
 */
public final class ServerMonitor implements Runnable {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private boolean stop;
	// Should use a linked list if we add/remove handlers, we don't though so no
	// performance problem
	private final List<ServerResponseI> handlers = new ArrayList<ServerResponseI>();
	private static final long KEEP_ALIVE_PERIOD = Long.getLong(
	    "authenticationDialog.keepliveTimer", 1000 * 30).longValue();

	public ServerMonitor() {
		try {
			new ServerOperation()
			    .registerForLpcpEventNotifications(new LpcpEventListener(
			        new LpcpEventCallback() {
				        @Override
				        public void lpcpEventReceived(LpcpEvent e) throws Exception {
					        log.error("LPCP_PORT event received " + e);
					        switch (e.getEventType()) {
					        case DOC_EVENT:
					        case VERTEX_UPDATED: {
						        /**
						         * <pre>
						         * <NetworkElementEvent>
						         *      <event name="association" id="TDEFAULT_PROXY_26" owner="TDEFAULT_PROXY" time="1288367738533">
						         *          <eventInfo notificationType="initializing" occurredDate="2010-10-29"
						         *              occurredTime="11:55:38" />
						         *          <data>
						         *              <element name="description" value="Discovering Network Element" />
						         *              </data>
						         *          <node type="OME7" id="00-21-E1-D6-D6-70" ip="47.134.3.230" port="10001" 
						         *              tid="OME0039" mode="SONET" status="initializing" 
						         *              />
						         *      </event>
						         * </NetworkElementEvent>
						         * </pre>
						         */
						        GenericJdomParser parser = new GenericJdomParser();
						        parser.parse(e.getArg());
						        notifyHandlers(
						            ServerResponseI.Message.RESPONSE_VERTEX_UPDATED, parser
						                .getRoot().getChild("event"));
					        }
						        break;
					        case GRAPH_REFRESH_REQUIRED:
						        notifyHandlers(
						            ServerResponseI.Message.GRAPH_REFRESH_REQUIRED, null);
						        break;
					        case SERVER_ACTIVE:
						        notifyHandlers(ServerResponseI.Message.RESPONSE_ACTIVE,
						            null);
						        break;
					        case SERVER_INACTIVE:
						        notifyHandlers(ServerResponseI.Message.RESPONSE_INACTIVE,
						            null);
						        break;
					        default:
						        log.error("Unknown event type " + e.getEventType()
						            + " recevied from LPCP_PORT, ignoring event " + e);
					        }
				        }
			        }));
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}

	}

	public void addHandler(ServerResponseI handler) {
		handlers.add(handler);
	}

	@Override
	public void run() {
		try {
			while (!stop) {

				
				if (!OpenDracDesktop.getAuth().isAuthenticated()) {
					log.error("not currently authenticated with the server, cannot validate the session");
					return;
				}

				/*
				 * Here we accomplish two things, we verify that we can still talk to
				 * the server, and we keep this users session from being considered as
				 * expired. On the web interface we expire user sessions that have not
				 * been used for some time, on the admin console we keep sessions active
				 * forever.
				 */
				new ServerOperation().sessionValidate();
				Uninterruptibles.sleepUninterruptibly(KEEP_ALIVE_PERIOD, TimeUnit.MILLISECONDS);
			}

			if (!stop) {
				log.debug("Connection to server lost!");
				notifyHandlers(ServerResponseI.Message.EVENT_RETRY, null);
			}
		}
		catch (Exception e) {
			log.error("Connection to server lost! Exception: ", e);
			notifyHandlers(ServerResponseI.Message.EVENT_RETRY, null);
		}
	}

	public void stop() {
		stop = true;
	}

	private void notifyHandlers(ServerResponseI.Message operation, Element event) {
		log.debug("notifyHandlers:" + operation + " event: "
		    + (event == null ? "" : new XMLOutputter().outputString(event)));
		for (ServerResponseI h : handlers) {
			h.handleServerResponse(operation, event);
		}
	}

}
