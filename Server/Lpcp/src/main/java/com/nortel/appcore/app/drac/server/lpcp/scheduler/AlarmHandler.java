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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAlarmEvent;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.helper.DbUtilityLpcpScheduler;

/**
 * Created on Apr 3, 2006
 * 
 * @author nguyentd
 */
public final class AlarmHandler implements Runnable {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Tl1XmlAlarmEvent event;

  public AlarmHandler(Tl1XmlAlarmEvent event) {
    this.event = event;
  }

  /**
   * Get all Services that: a) contain the AID specified in the alarm and b) has
   * the start time that is equal or greater than the occurred time of the alarm
   * event. If the alarm affects at least one Service then: a) update the
   * Service database with alarm summary b) insert the alarm's detail into the
   * database if it's a alarm raised event else, update the duration time of the
   * existing alarm.
   */
  @Override
  public void run() {
    try {
      String alarmId = event.getEventId();
      
      // String aid = event.getAlarmAid();
      // String facility = event.getAlarmFacility();
      String channel = event.getAlarmChannel();
      String occTimeStr = event.getEventTime();
      long occTime = Long.parseLong(occTimeStr);
      String neId = event.getSourceNodeId();
      StringBuilder newAlarmBuff = new StringBuilder(50);
      newAlarmBuff.append("<alarm ");
      newAlarmBuff.append(ServiceXml.ID_ATTR + "=\"" + event.getEventId()
          + "\" ");
      newAlarmBuff.append("severity=\"" + event.getAlarmSeverity() + "\" ");
      newAlarmBuff.append("occurredTime=\"" + occTimeStr + "\" serviceId=\"");

      List<ServiceXml> services;
      String alarmServerity = event.getAlarmSeverity();

      if (alarmServerity.equals(Tl1XmlAlarmEvent.CLEAR_SEVERITY)) {
        services = DbUtilityLpcpScheduler.INSTANCE.getServicesFromAlarm(
            alarmId);
      }
      else {
        // It's possible that there are many facility entries in the alarm
        // (i.e. equipment alarm), so we have to iterate through all.
        services = new ArrayList<ServiceXml>();
        List<String> facilities = event.getAlarmFacility();
        for (int i = 0; i < facilities.size(); i++) {
          String aFac = facilities.get(i);
          services.addAll(DbUtilityLpcpScheduler.INSTANCE
              .getServicesFromAid(aFac, channel, occTime, neId));
        }
      }
      // If the size of "services" array is not zero, then the alarm affects
      // at least one service.
      for (int i = 0; i < services.size(); i++) {
        ServiceXml aService = services.get(i);
        String serviceId = aService.getServiceId();
        event.addDataElement(Tl1XmlAlarmEvent.SERVICEID_KEY, serviceId);

        // Insert into DB if it's not a clear
        if (!alarmServerity.equals(Tl1XmlAlarmEvent.CLEAR_SEVERITY)) {
          String alarmString = newAlarmBuff.toString()
              + aService.getServiceId() + "\"/>";
          DbUtilityLpcpScheduler.INSTANCE.insertAlarmSummary(neId,
              alarmString);
        }

        // Log the alarm in the audit log
        if (alarmServerity.equals(Tl1XmlAlarmEvent.CRITICAL_SEVERITY)) {
          DbLog.INSTANCE.generateLog(
              new LogRecord(null, null, aService.getBillingGroup(), serviceId,
                  LogKeyEnum.KEY_CRITICAL_ALARM, new String[] { event
                      .getEventId() }));
        }
        else if (alarmServerity.equals(Tl1XmlAlarmEvent.MAJOR_SEVERITY)) {
          DbLog.INSTANCE.generateLog(
              new LogRecord(null, null, aService.getBillingGroup(), serviceId,
                  LogKeyEnum.KEY_MAJOR_ALARM,
                  new String[] { event.getEventId() }));
        }
        else if (alarmServerity.equals(Tl1XmlAlarmEvent.MINOR_SEVERITY)) {
          DbLog.INSTANCE.generateLog(
              new LogRecord(null, null, aService.getBillingGroup(), serviceId,
                  LogKeyEnum.KEY_MINOR_ALARM,
                  new String[] { event.getEventId() }));
        }
        else if (alarmServerity.equals(Tl1XmlAlarmEvent.CLEAR_SEVERITY)) {
          DbLog.INSTANCE.generateLog(
              new LogRecord(null, null, aService.getBillingGroup(), serviceId,
                  LogKeyEnum.KEY_ALARM_CLEARED, new String[] { event
                      .getEventId() }));
        }

      }

      // Add the alarm's detail into the database if needed
      if (services.size() > 0) {
        if (alarmServerity.equals(Tl1XmlAlarmEvent.CLEAR_SEVERITY)) {
          Tl1XmlAlarmEvent raisedAlarm = DbUtilityLpcpScheduler.INSTANCE
              .getAlarm(alarmId);
          if (raisedAlarm == null) {
            log.error("Couldn't find " + alarmId + " in the database");
            return;
          }
          log.debug("Update the information of this alarm: " + alarmId);
          long raisedTime = Long.parseLong(raisedAlarm.getEventTime());
          long clearedTime = Long.parseLong(event.getEventTime());
          DbUtilityLpcpScheduler.INSTANCE.updateAlarmDuration(alarmId,
              clearedTime - raisedTime);
        }
        else {
          
          DbUtilityLpcpScheduler.INSTANCE.insertAlarmDetail(
              event.toString());
        }
      }
      else {
        log.debug("There is no service associated with this alarm: \r\n"
            + event.toString());
      }
    }
    catch (Exception e) {
      log.error("Failed to process event", e);
    }
  }
}
