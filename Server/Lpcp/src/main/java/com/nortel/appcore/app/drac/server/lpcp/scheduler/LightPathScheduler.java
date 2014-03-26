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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAlarmEvent;
import com.nortel.appcore.app.drac.database.helper.DbUtilityLpcpScheduler;
import com.nortel.appcore.app.drac.server.lpcp.Lpcp;

/**
 * LightPathScheduler The DRAC routing engine - based on the JUNG shortest path
 * first implementation.
 * 
 * @author nguyentd
 * @since 2005-11-08
 */
public final class LightPathScheduler extends Thread {
  private static final Logger log = LoggerFactory.getLogger(LightPathScheduler.class);

	class MyThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "LightPathScheduler worker thread");
		}
	}

	private boolean shouldTerminate;
	private static LightPathScheduler instance;
	private Lpcp lpcp;
	private long nextStartTime = System.currentTimeMillis();
	private StartScheduleThread startScheduleThread;
	private DeleteScheduleThread deleteScheduleThread;
	private Timer alarmAuditTimer;
	public static final long ALARM_AUDIT_PERIOD = 1000 * 60 * 60;

	/*
	 * Use this for sequentially process incoming events (i.e. maintaining the
	 * arrival order
	 */
	private final Executor executorSeq = Executors
	    .newSingleThreadExecutor(new MyThreadFactory());

	// Use this for thread pool of short live threads
	public static final ExecutorService executorPool = Executors
	    .newCachedThreadPool();
	private static String globalOffset;

	private LightPathScheduler() {
		super("LightPathSchedule");
		
	}

	public static synchronized LightPathScheduler getInstance(String offset) {
		if (instance == null) {
			
			globalOffset = offset;
			instance = new LightPathScheduler();
			instance.setDaemon(true);
			instance.start();
		}
		return instance;
	}

	public void cancelSchedule(String id, SERVICE state) throws Exception {
		

		ServiceXml aService = DbUtilityLpcpScheduler.INSTANCE
		    .getServiceFromCallId(id);
		log.info("Canceling service with id {}, state {} and servcie data {}", new Object[]{id, state, aService});

		if (aService == null) {
			throw new DracException(
			    DracErrorConstants.LPCP_E3045_SERVICEID_NOT_FOUND, null);
		}

		SERVICE successState = aService.getStatus();

		if (state == null) {
			if (aService.getStatus() == SERVICE.ACTIVATION_PENDING) {
				successState = SERVICE.ACTIVATION_CANCELLED;
			}
			else if (aService.getStatus() == SERVICE.CONFIRMATION_PENDING) {
				successState = SERVICE.CONFIRMATION_CANCELLED;
			}
			else if (aService.getStatus() == SERVICE.EXECUTION_PENDING) {
				successState = SERVICE.EXECUTION_CANCELLED;
			}
			else if (aService.getStatus() == SERVICE.EXECUTION_INPROGRESS) {
				successState = SERVICE.EXECUTION_PARTIALLY_CANCELLED;
			}
			else {
				successState = SERVICE.EXECUTION_CANCELLED;
			}
		}
		else {
			successState = state;
		}

		SERVICE sState = successState;
		if (aService.getStatus() == SERVICE.EXECUTION_INPROGRESS) {
			if (!deleteScheduleThread.sendCommandToNe(
			    SchedulingThread.Command.DELXCON_CMD, aService, sState,
			    SERVICE.EXECUTION_PARTIALLY_CANCELLED)) {
				log.error("failed to cancel service: " + id);
				DbUtilityLpcpScheduler.INSTANCE.updateServiceStatusByCallId(id,
				    SERVICE.EXECUTION_PARTIALLY_CANCELLED);
				return;
			}
		}

		DbUtilityLpcpScheduler.INSTANCE
		    .updateServiceStatusByCallId(id, sState);

		// Notify listeners of new state
		// This is where the LPCP_PORT service state is prop'ed to NRB_PORT via
		// event
		lpcp.updateScheduleAndLogServiceChange(aService, Lpcp.REQ_CANCEL_SERVICE,
		    sState);
		
	}

	public Lpcp getDracServerReference() {
		return this.lpcp;
	}

	public synchronized long getNextStartTime() {
		return nextStartTime;
	}

	public List<CrossConnection> getOverLappingSchedules(long startTime,
	    long endTime) {

		List<CrossConnection> listOfConnection = new ArrayList<CrossConnection>();

		try {
			List<ServiceXml> schedules = DbUtilityLpcpScheduler.INSTANCE
			    .getLiveServicesWithinTimeInterval(startTime, endTime);
			log.debug("getOverLappingSchedules got overlaping schedules " + schedules);

			// for each schedule, get the list of all nodal connections

			for (int i = 0; i < schedules.size(); i++) {
				ServiceXml tempSchedule = schedules.get(i);
				for (int j = 0; j < tempSchedule.numberOfConnectionsInPath(); j++) {
					listOfConnection.add(tempSchedule.getCrossConnectionInfo(j));
				}
			}
		}
		catch (Exception e) {
			log.error("Exception getting overlapping schedules", e);
		}

		return listOfConnection;
	}

	public synchronized void notifyThreads() {
		startScheduleThread.wakeUp();
		deleteScheduleThread.wakeUp();
	}

	public void processAlarm(ClientMessageXml event) {
		if (event.isAlarm()) {
			Tl1XmlAlarmEvent alarmEvent = new Tl1XmlAlarmEvent(event.getEventNode()
			    .getChild(ClientMessageXml.EVENTDATA_NODE));
			executorSeq.execute(new AlarmHandler(alarmEvent));
		}
	}


	@Override
	public void run() {
		
		long offsetLong = 0L;
		// Startup the alarm audit
		alarmAuditTimer = new Timer();
		// Delay a minute for the other component to be ready
		/*
		 * We have just started up, run the alarm audit in 1 minute then again every
		 * hour. We want it to run right now mostly for testing, so we see it
		 * running and know its working.
		 */

		alarmAuditTimer.schedule(new AlarmAudit(), 60 * 1000, ALARM_AUDIT_PERIOD);
		log.debug("LightPathScheduler::adding light path scheduler as listener to tl1session");

		// Parse the globaloffset
		try {
			offsetLong = Long.parseLong(globalOffset); // This number is in
			                                           // milliseconds
		}
		catch (NumberFormatException e) {
			log.error("Exception parsing global offset: " + globalOffset, e);
		}

		// Create and start the start and delete scheduling threads
		startScheduleThread = new StartScheduleThread("StartServiceThread", this);
		deleteScheduleThread = new DeleteScheduleThread("DeleteServiceThread",
		    this, offsetLong);
		startScheduleThread.setDaemon(true);
		startScheduleThread.start();
		deleteScheduleThread.setDaemon(true);
		deleteScheduleThread.start();

		while (!shouldTerminate) {
			// Ensure that the start and delete schedule threads are still alive.
			// If not, restart them
			if (!shouldTerminate) {
				if (!startScheduleThread.isAlive()) {
					log.error("Unexpected system error, StartScheduleThread is dead.");
					startScheduleThread.setDaemon(true);
					startScheduleThread.start();
				}
				if (!deleteScheduleThread.isAlive()) {
					log.error("Unexpected system error, DeleteScheduleThread is dead.");
					deleteScheduleThread.setDaemon(true);
					deleteScheduleThread.start();
				}
				Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
			}
		}
	}

	public void setLpcp(Lpcp lpcp) {
		this.lpcp = lpcp;
	}

	public synchronized void setNextStartTime(long newStartTime) {
		nextStartTime = newStartTime;
		log.debug("nextStartTime changed to: " + newStartTime);
	}
	
}
