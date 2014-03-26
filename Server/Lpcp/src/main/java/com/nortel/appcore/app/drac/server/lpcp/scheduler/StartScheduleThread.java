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

package com.nortel.appcore.app.drac.server.lpcp.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.database.helper.DbUtilityLpcpScheduler;

/**
 * StartScheduleThread Started by LightPathScheduler, this thread finds and
 * starts jobs from the database that are pending and not expired.
 * 
 * @since 2005-11-08
 */
public final class StartScheduleThread extends SchedulingThread {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private boolean shouldTerminate;
	private ServiceXml aSchedule;
	private long nextStartTime;

	private final Object startSchedSema = new Object();
	private final Object activeSema = new Object();
	private boolean active = true;

	public StartScheduleThread(String threadName, LightPathScheduler hScheduler) {
		super(threadName, hScheduler);
	}

	@Override
	public void run() {
		setNextStartTime(System.currentTimeMillis());
		while (!shouldTerminate) {
			synchronized (startSchedSema) {
				try {
					if (active) {
						startSchedule();
					}
					else {
						synchronized (activeSema) {
							activeSema.wait();
						}
					}
				}
				catch (Exception e) {
					log.error("Exception starting schedule: ", e);
				}
			}
		}
	}

	public void wakeUp() {
		synchronized (startSchedSema) {
			startSchedSema.notifyAll();
		}
	}

	private synchronized void setNextStartTime(long startTime) {
		
		nextStartTime = startTime;
	}

	private void startSchedule() {
		try {
			log.debug("current time: " + System.currentTimeMillis()
			    + " nextStartTime: " + nextStartTime);
			aSchedule = DbUtilityLpcpScheduler.INSTANCE.getNextServiceToStart(
			    nextStartTime);
			if (aSchedule != null) {
			  log.info("Startign schedule: {}", aSchedule);
				long startTime;
				// create connection
				startTime = aSchedule.getStartTime();
				log.debug("... with the startTime=" + startTime);

				if (startTime < System.currentTimeMillis()) {
					startTime = System.currentTimeMillis();
				}

				long timeToWait = startTime - System.currentTimeMillis();

				if (timeToWait > 0) {
					log.debug("... sleeping for " + timeToWait + " in startSchedule");
					startSchedSema.wait(timeToWait);
				}

				/*
				 * If the schedule hasn't expired yet, don't send the delete to the NEs
				 * Most likely the schedule is being deleted (LightPathScheduler will
				 * send the delete in this case) OR a new schedule has been added and we
				 * got notified to check if the new schedule ends earlier than the
				 * current one we are waiting on
				 */
				if (System.currentTimeMillis() >= startTime) {
					// perform the task;
					this.sendCommandToNe(SchedulingThread.Command.ADDXCON_CMD, aSchedule,
					    SERVICE.EXECUTION_INPROGRESS, SERVICE.CREATE_FAILED);
				}

				setNextStartTime(System.currentTimeMillis());
			}
			else {
				log.debug("Waiting on semaphore in startSchedule");
				startSchedSema.wait();
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
    }
  }

}
