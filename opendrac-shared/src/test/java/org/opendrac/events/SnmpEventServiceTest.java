package org.opendrac.events;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;
import org.opendrac.events.MailEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpEventServiceTest {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final SnmpEventService snmpEventService = new SnmpEventService();

  @Test
  public void testSendAlarm() {
    final SnmpEvent snmpEvent = new SnmpEvent();
    snmpEvent.setAddress("[::1]");
    snmpEvent.setDate(new Date());
    snmpEvent.setStatus("down");
    try {
      snmpEventService.sendAlarm(snmpEvent);
    }
    catch (IOException e) {
      fail();
      log.error("Error: ", e);
    }
  }

}
