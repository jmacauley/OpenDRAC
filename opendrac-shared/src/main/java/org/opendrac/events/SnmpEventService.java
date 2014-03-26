package org.opendrac.events;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

class SnmpEventService implements EventService<SnmpEvent> {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final CommunityTarget target = new CommunityTarget();
  private TransportMapping transport;
  private Snmp snmp;

  public SnmpEventService() {
    try {
      transport = new DefaultUdpTransportMapping();
      snmp = new Snmp(transport);
      target.setCommunity(new OctetString("public"));
      target.setAddress(new UdpAddress("127.0.0.1/1620"));
      target.setVersion(SnmpConstants.version2c);
    }
    catch (IOException e) {
      log.error("Error: ", e);
    }
  }

  @Override
  public void sendAlarm(final SnmpEvent... events) throws IOException {
    for (final SnmpEvent mailEvent : events) {
      final PDU request = new PDU();
      request.setType(PDU.TRAP);
      request.add(new VariableBinding(new OID("1.3.6.1.2.1.1.3.0"), new Integer32(0)));
      request.add(new VariableBinding(new OID("1.3.6.1.6.3.1.1.4.1.0"), new OID("1.3.7")));
      request.add(new VariableBinding(new OID("1.3.6.1"), new OctetString(mailEvent.getAddress())));
      request.add(new VariableBinding(new OID("1.3.6.2"), new OctetString(mailEvent.getStatus())));
      snmp.send(request, target);
    }
  }

}
