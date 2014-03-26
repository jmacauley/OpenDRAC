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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.database.helper.DbUtilityLpcpScheduler;

/**
 * DeleteScheduleThread Started by LightPathScheduler, this thread finds and
 * deletes jobs from the database that have expired. creation, refactored from
 * LightPathScheduler
 * 
 * @since 2005-11-08
 */
public final class DeleteScheduleThread extends SchedulingThread {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private boolean shouldTerminate;
	private ServiceXml aService;
	// private String command;
	private final Object schedSema = new Object();
	private final Object activeSema = new Object();
	private boolean active = true;
	private final long offset;

	public DeleteScheduleThread(String threadName, LightPathScheduler hScheduler,
	    long offset) {
		super(threadName, hScheduler);
		// this.lpcpUID = lpcpUID;
		this.offset = offset;
		
	}

	@Override
	public void run() {
		while (!shouldTerminate) {
			synchronized (schedSema) {
				try {
					if (active) {
						deleteSchedule();
					}
					else {
						synchronized (activeSema) {
							activeSema.wait();
						}
					}
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
			}
		}
	}

	public void wakeUp() {
		synchronized (schedSema) {
			schedSema.notifyAll();
		}
	}

	private void deleteSchedule() {
		try {
			log.debug("current time: " + System.currentTimeMillis() + " nextEndTime: "
			    + 0);
			aService = DbUtilityLpcpScheduler.INSTANCE.getNextServiceToDelete();

			if (aService != null) {
				log.debug("Got this schedule: " + aService + " from DB");
				long endTime;

				// delete connection
				endTime = aService.getEndTime();
				if (aService.getStatus() == SERVICE.ACTIVATION_PENDING) {
					/*
					 * A service in the activation pending state will be timed-out if no
					 * activation has been received by (endTime-2*offset)
					 */
					endTime = endTime - 2 * offset;
					log.debug("offset is: " + offset + " 2*offset: " + 2 * offset);
					log.debug("Schedule in ACTIVATION_PENDING state detected... endTime set to endTime minus 2*offset:"
					    + endTime);
				}
				else {
					// Subtract the offset from the endTime
					endTime = endTime - offset;
				}
				log.debug("... with the endTime=" + endTime);

				long timeToWait = endTime - System.currentTimeMillis();

				if (timeToWait > 0) {
					log.debug("... sleeping for " + timeToWait + " in deleteSchedule");
					schedSema.wait(timeToWait);
				}

				log.debug("woke up");

				/*
				 * If the schedule hasn't expired yet, don't send the delete to the NEs
				 * Most likely the schedule is being deleted (LightPathScheduler will
				 * send the delete in this case) OR a new schedule has been added and we
				 * got notified to check if the new schedule ends / earlier than the
				 * current one we are waiting on
				 */
				if (System.currentTimeMillis() >= endTime) {
					/*
					 * perform the task; Check if the service is still in the
					 * ACTIVATION_PENDING state. If so, timeout the activation.
					 */
					log.debug("aSchedule.getScheduleStatus(): " + aService.getStatus());
					log.debug("ACTIVATION_PENDING.ordinal(): "
					    + SERVICE.ACTIVATION_PENDING.ordinal());
					if (aService.getStatus() == SERVICE.ACTIVATION_PENDING) {
						log.debug("Activation timed-out for service: "
						    + aService.getServiceId());
						DbUtilityLpcpScheduler.INSTANCE.updateServiceByServiceId(
						    aService.getServiceId(), SERVICE.ACTIVATION_TIMED_OUT);
						hScheduler.getDracServerReference()
						    .updateScheduleAndLogServiceChange(aService,
						        SchedulingThread.Command.DELXCON_CMD.toString(),
						        SERVICE.ACTIVATION_TIMED_OUT);
					}
					else {
						/*
						 * re-read the record. there are cases now in which I've updated the
						 * db record without having necessarily notified the scheduling
						 * threads.
						 */
						ServiceXml serv = DbUtilityLpcpScheduler.INSTANCE
						    .getServiceFromCallId(aService.getCallId());

						this.sendCommandToNe(SchedulingThread.Command.DELXCON_CMD, serv,
						    SERVICE.EXECUTION_SUCCEEDED, SERVICE.DELETE_FAILED);
					}
				}
			}
			else {
				log.debug("Waiting on semaphore in deleteSchedule");
				schedSema.wait();
			}
		}
		catch (InterruptedException ie) {
			log.error("DeleteScheduleThread interrupted", ie);
		}
		catch (Exception e) {
			log.error("Exception starting schedule", e);
			Uninterruptibles.sleepUninterruptibly(4, TimeUnit.SECONDS);
		}
	}
}
