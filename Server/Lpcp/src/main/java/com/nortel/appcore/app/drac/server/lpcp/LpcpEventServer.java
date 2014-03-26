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

package com.nortel.appcore.app.drac.server.lpcp;

import java.rmi.ConnectException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.nrb.LpcpEvent;
import com.nortel.appcore.app.drac.server.nrb.LpcpEventCallback;

/**
 * Event publisher. Publish events to registered listeners over RMI.
 */
public enum LpcpEventServer {

	INSTANCE;
	private static final Logger log = LoggerFactory
	    .getLogger(LpcpEventServer.class);

	/**
	 * A single thread that delivers events to registered clients.
	 */
	private static class EventPublisher extends Thread {
		private int maxListeners;
		private int maxQueued;

		private final LinkedBlockingQueue<LpcpEvent> queue = new LinkedBlockingQueue<LpcpEvent>();
		// Use a set to avoid the same listener being present multiple times.
		private final Map<LpcpEventCallback, LpcpEventCallback> eventListeners = new ConcurrentHashMap<LpcpEventCallback, LpcpEventCallback>();

		public void addListener(LpcpEventCallback cb) throws Exception {
			eventListeners.put(cb, cb);
			maxListeners = Math.max(maxListeners, eventListeners.size());
			log.debug("Added event listener " + eventListeners.size()
			    + " listeners are registered " + maxListeners
			    + " maxListeners registered.");
		}

		public void publishEvent(LpcpEvent e) {
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
					final LpcpEvent event = queue.poll(1, TimeUnit.SECONDS);
					if (event != null) {
						log.debug("Sending LpcpEvent to " + eventListeners.size()
						    + " clients.  Maximum queue size " + maxQueued
						    + " current size " + queue.size() + " Event: "
						    + event.getEventType());

						for (LpcpEventCallback cb : eventListeners.keySet()) {
							try {
								cb.lpcpEventReceived(event);
							}
							catch (ConnectException ce) {
								log.debug(
								    "remote event client is not responding, unregistering callback!"
								        + cb, ce);
								/*
								 * EventListeners is a ConcurrentHashMap, so this will work
								 * without throwing an concurrentModification Exception!
								 */
								eventListeners.remove(cb);
							}
							catch (Exception e) {
								log.error("Unable to deliver LpcpEvent event ", e);
							}
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

	private LpcpEventServer() {
		eventPublisher.setDaemon(true);
		eventPublisher.start();
	}

	public void addListener(LpcpEventCallback cb) throws Exception {
		eventPublisher.addListener(cb);
	}

	public void publishEvent(LpcpEvent e) {
		eventPublisher.publishEvent(e);
	}

	public void removeAllListeners() throws Exception {
		log.debug("LpcpEventServer: Removing all event listeners; going inactive or shutting down");
		eventPublisher.removeAllListeners();
	}

}
