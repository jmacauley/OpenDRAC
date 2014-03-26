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

package com.nortel.appcore.app.drac.server.neproxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyEvent;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyEventCallback;

/**
 * Event publisher. Publish events to registered listeners over RMI. We only
 * expect LPCP_PORT to listen to our events.
 */

public enum NeProxyEventServer {
	INSTANCE;
	private static final Logger log = LoggerFactory.getLogger(NeProxyEventServer.class);
	/**
	 * A single thread that delivers events to registered clients.
	 */
	private static class EventPublisher extends Thread {
		private int maxListeners;
		private int maxQueued;

		private final LinkedBlockingQueue<NeProxyEvent> queue = new LinkedBlockingQueue<NeProxyEvent>();
		// Use a set to avoid the same listener being present multiple times.
		private final Map<NeProxyEventCallback, NeProxyEventCallback> eventListeners = new ConcurrentHashMap<NeProxyEventCallback, NeProxyEventCallback>();

		public void addListener(NeProxyEventCallback cb) throws Exception {
			eventListeners.put(cb, cb);
			maxListeners = Math.max(maxListeners, eventListeners.size());
			log.debug("Added event listener " + eventListeners.size()
			    + " listeners are registered " + maxListeners
			    + " maxListeners registered.");
		}

		public void publishEvent(NeProxyEvent e) {
			queue.add(e);
			maxQueued = Math.max(maxQueued, queue.size());
		}

		public void removeAllListeners() {
			eventListeners.clear();
		}

		@Override
		public void run() {
			while (true) {
				try {
					// Block (forever) waiting for an event to grab.
					NeProxyEvent event = queue.take();
					log.debug("Sending NeProxyEvent to " + eventListeners.size()
					    + " clients.  Maximum queue size " + maxQueued + " current size "
					    + queue.size() + " Event: " + event);
					for (NeProxyEventCallback cb : eventListeners.keySet()) {
						try {
							cb.neProxyEventReceived(event);
						}
						catch (Exception e) {
							log.error("Unable to deliver NeProxy event ", e);
						}
					}
				}
				catch (InterruptedException e) {
					log.error("Error: ", e);
				}
			}
		}
	}

	private final EventPublisher eventPublisher = new EventPublisher();

	private NeProxyEventServer() {
		eventPublisher.setDaemon(true);
		eventPublisher.start();
	}

	public void addListener(NeProxyEventCallback cb) throws Exception {
		eventPublisher.addListener(cb);
	}

	public void publishEvent(NeProxyEvent e) {
		eventPublisher.publishEvent(e);
	}

	public void removeAllListeners() throws Exception {
		log.debug("NeProxyEventServer: Removing all event listeners; going inactive or shutting down");
		eventPublisher.removeAllListeners();
	}

}
