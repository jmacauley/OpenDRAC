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

package com.nortel.appcore.app.drac.server.nrb.impl.mlbm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule.AuditScheduleStatusHolder;
import com.nortel.appcore.app.drac.database.helper.DbUtilityCommonUtility;
import com.nortel.appcore.app.drac.server.nrb.impl.administration.LpcpAdminManager;

/**
 * This audit task is used to change the status of all schedules in the database
 * from "active" to "inactive" if the endtime passes the the current time.
 * 
 * @author
 */
public final class AuditThread extends Thread {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private boolean shouldTerminate;
	private static final long AUDIT_PERIOD = Long.getLong("auditPeriod", 60)
	    .longValue() * 1000 * 60;
	// private final SAXBuilder saxBuilder = new SAXBuilder();
	private LpcpAdminManager lpcpAdminManager;

	public AuditThread(LpcpAdminManager mbwm) {
		lpcpAdminManager = mbwm;
	}

	/**
	 * This audit task is used to change the status of all schedules in the
	 * database from "active" to "inactive" if the endtime passes the the current
	 * time.
	 */
	@Override
	public void run() {
		try {
			long waitTime = AUDIT_PERIOD;
			while (!shouldTerminate) {
				try {
					auditScheduleStatus();
					/*
					 * get the current time before calling the database just to add a few
					 * more milliseconds.
					 */
					long currTime = System.currentTimeMillis();
					long temp = DbUtilityCommonUtility.INSTANCE.getNextSchedule();
					if (temp > currTime) {
						waitTime = temp - currTime;
					}
					else {
						waitTime = AUDIT_PERIOD;
					}

					synchronized (this) {
						
						wait(waitTime);
					}
				}
				catch (InterruptedException ie) {
					log.debug(
					    "ScheduleAudit was interrupted, someone added or removed a schedule",
					    ie);
				}
				catch (Exception e) {
					log.error("ScheduleAudit fails will try in 30 sec", e);
					try {
						Thread.sleep(30000); // re-try in 30 sec
					}
					catch (Exception ee) {
						log.error("Error: ", ee);
					}
				}
			}
		}
		catch (Exception t) {
			log.error("Unexpected throwable ", t);
		}
	}

	private void auditScheduleStatus() throws Exception {
		log.debug("auditScheduleStatus audit thread running");
		List<AuditScheduleStatusHolder> myData = DbSchedule.INSTANCE
		    .getAuditScheduleStatus();
		if (myData != null) {
			for (AuditScheduleStatusHolder a : myData) {
				log.debug("auditScheduleStatus examining exprired schedule " + a);
				if (SCHEDULE.CONFIRMATION_PENDING.equals(a.getScheduleStatus())) {
					lpcpAdminManager.scheduleConfirmationTimeout(a.getScheduleId(),
					    a.getScheduleStatus(), a.getScheduleEndtime());
				}
				DbUtilityCommonUtility.INSTANCE.updateScheduleStatus(
				    a.getScheduleId());
				/**
				 * @todo We are currently not updating the service records, only the
				 *       schedule. The xml version had code to do this bit it was
				 *       already commented out at the time it was converted, not sure if
				 *       its necessary or not
				 */
			}
		}
		// Document myDoc = saxBuilder.build(new
		// ByteArrayInputStream(myData.getBytes()));
		// Element root = myDoc.getRootElement();
		// List<Element> list = root.getChildren("schedule");
		// for (Element element : list)
		// {
		// Element scheduleElement = element;
		// String scheduleId = scheduleElement.getAttributeValue("id");
		// String status = scheduleElement.getAttributeValue("status");
		// String endTime = scheduleElement.getAttributeValue("endTime");
		// if (SCHEDULE.CONFIRMATION_PENDING.name().equals(status))
		// {
		// lpcpAdminManager.scheduleConfirmationTimeout(scheduleId,
		// SCHEDULE.valueOf(status),
		// new Long(endTime).longValue());
		// }
		//
		// // We may have to come back and do this.
		// // List serviceList = scheduleElement.getChildren("service");
		// // if (serviceList!=null){
		// // for (Iterator iterator = serviceList.iterator(); iterator.hasNext();)
		// {
		// // Element serviceElement = (Element) iterator.next();
		// // String serviceId= serviceElement.getAttributeValue("id");
		// // SERVICE serviceStatus =
		// // State.evaluateState(ACTIVITY.TIMED_OUT,SERVICE.valueOf(serviceElement
		// // .getAttributeValue("status")));
		// // updateServiceStatus(scheduleId, serviceId, serviceStatus, null);
		// // }
		// // }
		// DbUtilityCommonUtility.INSTANCE.updateScheduleStatus(scheduleId);
		// }
		// }
	}
}
