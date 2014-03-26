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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;

public final class DiscoveryManager implements Runnable {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static final int QUEUE_THROTTLE = 10;
	private final List<DiscoverThread> neDiscoveryQueue = new ArrayList<DiscoverThread>();
	private boolean stop;
	private final Object sema = new Object();
	private int started;

	public void addToDiscoveryQ(DiscoverThread discoverThread) {
		synchronized (sema) {
			neDiscoveryQueue.add(discoverThread);
			sema.notifyAll();
		}
	}

	public int getActiveJobCount() {
		return neDiscoveryQueue.size();
	}

	public int getDiscoveriesNotDoneCount() {
		int notDone = 0;
		for (DiscoverThread dt : neDiscoveryQueue) {
			if (!dt.isDone()) {
				notDone++;
			}
		}

		return notDone;
	}

	public void removeFromDiscoveryQ(String ipAddress) {
		synchronized (sema) {
			for (int i = 0; i < neDiscoveryQueue.size(); i++) {
				DiscoverThread dt = neDiscoveryQueue.get(i);
				if (dt.getIp().equals(ipAddress)) {
					neDiscoveryQueue.remove(dt);
					break;
				}
			}
			sema.notifyAll();
		}
	}

	@Override
	public void run() {
		List<DiscoverThread> tempList = new ArrayList<DiscoverThread>();

		while (!stop) {
			try {
				tempList.clear();
				synchronized (sema) {
					if (!neDiscoveryQueue.isEmpty()) {
						log.debug("DiscoveryManager:: Q size: " + neDiscoveryQueue.size()
						    + " queue: " + neDiscoveryQueue);
						for (DiscoverThread dt : neDiscoveryQueue) {

							if (dt.isDone()) {
								log.debug("Found a stale discovery thread to remove: "
								    + dt.toString());
								tempList.add(dt);
							}
							else if (!dt.isStarted()) {
								
								if (started < QUEUE_THROTTLE) {
									
									Thread discoverNEThread = new Thread(dt, "Discover ne: "
									    + dt.getTid());
									discoverNEThread.setDaemon(true);
									discoverNEThread.start();
									started++;
								}
								else {
									log.debug("LPCP_PORT discovery throttle engaged, size limit: "
									    + QUEUE_THROTTLE + " current threads started: " + started);
								}
							}
						}

						started -= tempList.size();
						removeFromDiscoveryQ(tempList);
					}
				}
				Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
			}
			catch (Exception e) {
				log.error("Exception caught in DiscoveryManager thread: ", e);
			}
		}
	}

	private void removeFromDiscoveryQ(List<DiscoverThread> completedThreads) {
		synchronized (sema) {
			neDiscoveryQueue.removeAll(completedThreads);
			sema.notifyAll();
		}
	}
}
