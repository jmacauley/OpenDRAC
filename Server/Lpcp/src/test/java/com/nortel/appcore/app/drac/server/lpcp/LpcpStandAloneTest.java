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

import static org.junit.Assert.*;

import java.rmi.Naming;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
import org.opendrac.security.InternalLoginToken;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.GraphData;
import com.nortel.appcore.app.drac.common.types.LpcpStatus;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;
import com.nortel.appcore.app.drac.database.helper.test.DbTestPopulateDb;
import com.nortel.appcore.app.drac.server.lpcp.rmi.LpcpInterface;
import com.nortel.appcore.app.drac.server.neproxy.NeProxyLauncher;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyInterface;
import com.nortel.appcore.app.drac.sshclisimulator.SshCliSimulator;
import com.nortel.appcore.app.drac.tl1simulator.Tl1Simulator;

public final class LpcpStandAloneTest {
  private static final Logger log = LoggerFactory.getLogger(LpcpStandAloneTest.class);
  private static Tl1Simulator neSim;
  private static Thread neFactoryThread;
  private static NeProxyInterface neProxy;
  private static LpcpInterface lpcpIf;
  private static InternalLoginToken neFactoryToken;
  private static InternalLoginToken token;
  private static Thread lcpThread;
  private static Lpcp lpcpInstance;
  private static SshCliSimulator ssh1;

  @BeforeClass
  public static void setUpBefore() throws Exception {
    
    System.setProperty("org.opendrac.controller.primary", "localhost");
    System.setProperty("org.opendrac.controller.secondary", "localhost");

    TestHelper.INSTANCE.initialize();

    // We need sim'ed NEs that are not producing CRS events. They can
    // sporadically wreak havoc on the
    // routing tests below. i.e. if a CRS event comes in such that two ends
    // of the same link have
    // uneven channels in use, then that link will be excluded from the
    // routing graph for a given
    // service request. Hence the periodic test failure of path-protected
    // routes. (When the one link
    // was excluded, only a working path was available.
    neSim = new Tl1Simulator(
        Tl1Simulator.OME6_NOAO_NETWORK_RANDOM_PORTS);
    neSim.startSimulator();

    
    /*
     * Populate our database with records we can use to exercise with, if using
     * the simulator skip NE data otherwise populate NE data as well.
     */
    DbTestPopulateDb.populateTestSystem(false);


    /**
     * Add NEs that point at our simulator.
     */
    List<Integer> simulatedNePortList = neSim.getActualPortNumbers();
    for (Integer nePort : simulatedNePortList) {
      // Values marked as unknown will be discovered when the NE
      // 'associates'
      NetworkElementHolder ne = new NetworkElementHolder("127.0.0.1",
          nePort.toString(), "ADMIN", NeType.UNKNOWN, "UNKNOWN",
          NeStatus.NE_UNKNOWN, null, CryptoWrapper.INSTANCE.encrypt(
              "ADMIN"), 1, NETWORK_ELEMENT_MODE.Unknown, "DEFAULT_PROXY", "",
          PROTOCOL_TYPE.NETL1_PROTOCOL, true, "UNKNOWN", null, null);
      DbNetworkElement.INSTANCE.add(ne);
    }
    if (ssh1 != null) {
      for (Integer p : ssh1.getInusePorts()) {
        DbNetworkElement.INSTANCE.add(
            new NetworkElementHolder("127.0.0.1", Integer.toString(p), "ADMIN",
                NeType.UNKNOWN, "UNKNOWN", NeStatus.NE_UNKNOWN, null,
                CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
                NETWORK_ELEMENT_MODE.Unknown, "DEFAULT_PROXY", "",
                PROTOCOL_TYPE.FORCE10_PROTOCOL, true, "UNKNOWN", null, null));
      }
    }

    /**
     * Start Mediation running
     */

    neFactoryThread = new Thread() {
      @Override
      public void run() {
        setName("NeFactory Thread");
        log.debug("Starting NeFactory running");
        new NeProxyLauncher().start();
      }
    };
    neFactoryThread.setDaemon(true);
    neFactoryThread.start();
    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

    boolean ok = false;
    int tries = 0;
    while (tries < 15) {
      try {
        neProxy = (NeProxyInterface) Naming.lookup("//localhost:"
            + RmiServerInfo.RMI_REGISTRY_PORT + "/"
            + RmiServerInfo.NEPROXY_RMI_NAME);
        neFactoryToken = InternalLoginHelper.INSTANCE.getToken(InternalLoginTokenType.NEPROXY);
        neProxy.isAlive(neFactoryToken);
        log.debug("NeFactory is running!");
        ok = true;
        break;
      }
      catch (Exception e) {
        log.debug("Could not talk to NeProxy, sleeping and retrying " + tries, e);
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
      }
      tries++;
    }
    if (!ok) {
      Exception e = new Exception(
          "Failed to get NeFactory up and running, terminating!");
      log.error("Failed to get NeFactory up and running, terminating", e);
      throw e;
    }

    /**
     * Start the LPCP_PORT running.
     */
    lcpThread = new Thread() {
      @Override
      public void run() {
        setName("LPCP_PORT thread");
        log.debug("Starting LPCP_PORT running");
        final Lpcp lpcp = getLpcp();
        lpcp.start();
      }
    };
    lcpThread.start();

    ok = false;
    tries = 0;
    while (tries < 15) {
      try {
        lpcpIf = (LpcpInterface) Naming.lookup(RmiServerInfo.LPCP_RMI_BINDING_NAME);
        token = InternalLoginHelper.INSTANCE.getToken(InternalLoginTokenType.LPCP);
        lpcpIf.isAlive(token);
        log.debug("Lpcp is running!");
        ok = true;
        break;
      }
      catch (Exception e) {
        log.debug("Could not talk to Lpcp, sleeping and retrying " + tries);
        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
      }
      tries++;
    }
    if (!ok) {
      Exception e = new Exception(
          "Failed to get lpcp up and running, terminating!");
      log.error("Failed to get lpcp up and running, terminating", e);
      throw e;
    }

    /*
     * In order to do some useful tests, bring the Neproxy up and running and
     * wait until it has had a chance to align the NEs its managing. We poll to
     * determine when its done to be sure.
     */

    int numNes = -1;
    while (true) {
      List<NetworkElementHolder> neList = neProxy.getNetworkElements(
          neFactoryToken, null);

      if (neList == null || neList.isEmpty()) {
        //
      }
      else {
        numNes = neList.size();
        boolean allAligned = true;
        for (NetworkElementHolder ne : neList) {
          if (!NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
            allAligned = false;
          }
        }
        if (allAligned) {
          log.debug("Waiting for NeProxy " + neList.size()
              + " Nes are now aligned ");
          break;
        }
        log.debug("Waiting for NeProxy not all NEs are aligned");
        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
      }
    }

    // Check for LPCP_PORT readiness
    // [1] is the server itself ready:
    while (true) {
      if (lpcpIf.getLpcpStatus(token).getState() == Lpcp.ServerState.RESTART_COMPLETE
          .asInt()) {
        break;
      }
      log.debug("Waiting for Lpcp to enter the RESTART COMPLETE state.");
      Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
    }
    // [2] having problems where by lpcp is up but the model is still empty
    // or incomplete.
    while (true) {
      GraphData gd = lpcpIf.getGraphData(token);
      if (numNes == gd.getVertices().size()) {
        log.debug("number of NEs equals LPCP_PORT graph size " + numNes + " vs "
            + gd.getVertices());
        break;
      }

      log.debug("Waiting for number of NEs to equal LPCP_PORT graph size " + numNes
          + " vs " + gd.getVertices().size());
      Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
    }

    // [3] Has Lpcp completed its NE discovery
    boolean goAhead = false;
    while (!goAhead) {
      int notDone = getLpcp().getDiscoveryMgr().getDiscoveriesNotDoneCount();
      
      goAhead = notDone == 0;
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
    
    Uninterruptibles.sleepUninterruptibly(30, TimeUnit.SECONDS);

    log.debug("=-=-=-=-=-=-=-=-= SETUP COMPLETE ");

  }

  @AfterClass
  public static void tearDownAfterTests() throws Exception {
    log.debug("=-=-=-=-=-=-=-=-= TEAR DOWN BEGINS ");

    neFactoryThread.interrupt();
    lcpThread.interrupt();
    if (ssh1 != null) {
      ssh1.stopSshServer(true);
    }
    neSim.stopSimulator();
    TestHelper.INSTANCE.destroy();
  }

  private static synchronized Lpcp getLpcp() {
    /*
     * This has to be deferred until the rmi port is selected and set via
     * org.opendrac.rmi.port
     */
    if (lpcpInstance == null) {
      lpcpInstance = new Lpcp(true);
    }
    return lpcpInstance;
  }

  @Test
  public void getPeerIPAddress() throws Exception {
    log.debug("getPeerIPAddress returns " + lpcpIf.getPeerIPAddress(token));
  }

  @Test
  public void testAuditModel() throws Exception {
    log.debug("auditModel returns " + lpcpIf.auditModel(token));
  }

  @Test
  public void testCorrectModel() throws Exception {
    lpcpIf.correctModel(token);
    log.debug("correctModel returned ");
  }

  @Test
  public void testCreateSchedule1() throws Exception {
    // Prepare input parameter map
    Map<SPF_KEYS, String> params = new HashMap<SPF_KEYS, String>();

    params.put(SPF_KEYS.SPF_SCHEDULE_NAME, "LpcpTest-1");
    params.put(SPF_KEYS.SPF_ACTIVATION_TYPE, "RESERVATION_AUTOMATIC");
    params.put(SPF_KEYS.SPF_SERVICE_STATUS, "EXECUTION_PENDING");
    params.put(SPF_KEYS.SPF_USER, "admin");
    params.put(SPF_KEYS.SPF_CONTROLLER_ID, "47.134.40.205:8001");
    params.put(SPF_KEYS.SPF_OFFSET, "30000");

    // --- Constraints ---
    params.put(SPF_KEYS.SPF_COST, "-1");
    params.put(SPF_KEYS.SPF_MBS, "50");
    params.put(SPF_KEYS.SPF_METRIC2, "-1");
    params.put(SPF_KEYS.SPF_HOP, "-1");

    // --- Routing options ---
    params.put(SPF_KEYS.SPF_PROTECTION, "UNPROTECTED");
    params.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "false");
    params.put(SPF_KEYS.SPF_RATE, "50");

    params.put(SPF_KEYS.SPF_SRCTNA, "OME0039_OC192-1-4-1");
    params.put(SPF_KEYS.SPF_SRCCHANNEL, "-1");

    params.put(SPF_KEYS.SPF_DSTTNA, "OME0237_OC192-1-4-1");
    params.put(SPF_KEYS.SPF_DSTCHANNEL, "-1");

    long startTime = System.currentTimeMillis();
    long endTime = startTime + 30 * 60 * 1000;
    params.put(SPF_KEYS.SPF_START_TIME, Long.toString(startTime));
    params.put(SPF_KEYS.SPF_END_TIME, Long.toString(endTime));
    log.debug("createSchedule queryOnly returns "
        + checkXmlMsgForException(lpcpIf.createSchedule(token, params, true)));

  }

  @Test
  public void testCreateSchedule2() throws Exception {
    // Prepare input parameter map
    Map<SPF_KEYS, String> params = new HashMap<SPF_KEYS, String>();

    params.put(SPF_KEYS.SPF_SCHEDULE_NAME, "LpcpTest-2");
    params.put(SPF_KEYS.SPF_ACTIVATION_TYPE, "RESERVATION_AUTOMATIC");
    params.put(SPF_KEYS.SPF_SERVICE_STATUS, "EXECUTION_PENDING");
    params.put(SPF_KEYS.SPF_USER, "admin");
    params.put(SPF_KEYS.SPF_CONTROLLER_ID, "47.134.40.205:8001");
    params.put(SPF_KEYS.SPF_OFFSET, "30000");

    // --- Constraints ---
    params.put(SPF_KEYS.SPF_COST, "-1");
    params.put(SPF_KEYS.SPF_MBS, "5");
    params.put(SPF_KEYS.SPF_METRIC2, "-1");
    params.put(SPF_KEYS.SPF_HOP, "-1");

    // --- Routing options ---
    //
    params.put(SPF_KEYS.SPF_PROTECTION, "PATH1PLUS1");
    params.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "false");
    params.put(SPF_KEYS.SPF_RATE, "5");

    params.put(SPF_KEYS.SPF_SRCTNA, "OME0039_OC192-1-4-1");
    params.put(SPF_KEYS.SPF_SRCCHANNEL, "-1");

    params.put(SPF_KEYS.SPF_DSTTNA, "OME0237_OC192-1-4-1");
    params.put(SPF_KEYS.SPF_DSTCHANNEL, "-1");

    long startTime = System.currentTimeMillis();
    long endTime = startTime + 30 * 60 * 1000;
    params.put(SPF_KEYS.SPF_START_TIME, Long.toString(startTime));
    params.put(SPF_KEYS.SPF_END_TIME, Long.toString(endTime));
    log.debug("createSchedule queryOnly returns "
        + checkXmlMsgForException(lpcpIf.createSchedule(token, params, true)));
  }

  @Test
  public void testCreateSchedule3() throws Exception {
    // Prepare input parameter map
    Map<SPF_KEYS, String> params = new HashMap<SPF_KEYS, String>();

    params.put(SPF_KEYS.SPF_SCHEDULE_NAME, "LpcpTest-3");
    params.put(SPF_KEYS.SPF_ACTIVATION_TYPE, "RESERVATION_AUTOMATIC");
    params.put(SPF_KEYS.SPF_SERVICE_STATUS, "EXECUTION_PENDING");
    params.put(SPF_KEYS.SPF_USER, "admin");
    params.put(SPF_KEYS.SPF_CONTROLLER_ID, "47.134.40.205:8001");
    params.put(SPF_KEYS.SPF_OFFSET, "30000");

    params.put(SPF_KEYS.SPF_SERVICEID, "testing1");
    params.put(SPF_KEYS.SPF_SCHEDULE_KEY, "randomeKey");

    // --- Constraints ---
    params.put(SPF_KEYS.SPF_COST, "-1");
    params.put(SPF_KEYS.SPF_MBS, "50");
    params.put(SPF_KEYS.SPF_METRIC2, "-1");
    params.put(SPF_KEYS.SPF_HOP, "-1");

    // --- Routing options ---
    params.put(SPF_KEYS.SPF_PROTECTION, "UNPROTECTED");
    params.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "false");
    params.put(SPF_KEYS.SPF_RATE, "50");

    params.put(SPF_KEYS.SPF_SRCTNA, "OME0039_OC192-1-4-1");
    params.put(SPF_KEYS.SPF_SRCCHANNEL, "-1");

    params.put(SPF_KEYS.SPF_DSTTNA, "OME0237_OC192-1-4-1");
    params.put(SPF_KEYS.SPF_DSTCHANNEL, "-1");

    // start in 30 minutes from now.
    long startTime = System.currentTimeMillis() + 30 * 60 * 1000;
    long endTime = startTime + (5 + 1000);
    params.put(SPF_KEYS.SPF_START_TIME, Long.toString(startTime));
    params.put(SPF_KEYS.SPF_END_TIME, Long.toString(endTime));
    log.debug("createSchedule  returns "
        + checkXmlMsgForException(lpcpIf.createSchedule(token, params, false)));
  }

  @Test
  public void testCreateSchedule4() throws Exception {
    // Prepare input parameter map
    Map<SPF_KEYS, String> params = new HashMap<SPF_KEYS, String>();

    params.put(SPF_KEYS.SPF_SCHEDULE_NAME, "LpcpTest-4");
    params.put(SPF_KEYS.SPF_ACTIVATION_TYPE, "RESERVATION_AUTOMATIC");
    params.put(SPF_KEYS.SPF_SERVICE_STATUS, "EXECUTION_PENDING");
    params.put(SPF_KEYS.SPF_USER, "admin");
    params.put(SPF_KEYS.SPF_CONTROLLER_ID, "47.134.40.205:8001");
    params.put(SPF_KEYS.SPF_OFFSET, "30000");

    params.put(SPF_KEYS.SPF_SERVICEID, "testing4");
    params.put(SPF_KEYS.SPF_SCHEDULE_KEY, "randomeKey");

    // --- Constraints ---
    params.put(SPF_KEYS.SPF_COST, "-1");
    params.put(SPF_KEYS.SPF_MBS, "50");
    params.put(SPF_KEYS.SPF_METRIC2, "-1");
    params.put(SPF_KEYS.SPF_HOP, "-1");

    // --- Routing options ---
    params.put(SPF_KEYS.SPF_PROTECTION, "PATH1PLUS1");
    params.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "false");
    params.put(SPF_KEYS.SPF_RATE, "50");

    params.put(SPF_KEYS.SPF_SRCTNA, "OME0039_OC192-1-4-1");
    params.put(SPF_KEYS.SPF_SRCCHANNEL, "-1");

    params.put(SPF_KEYS.SPF_DSTTNA, "OME0237_OC192-1-4-1");
    params.put(SPF_KEYS.SPF_DSTCHANNEL, "-1");

    // start in 40 minutes from now.
    long startTime = System.currentTimeMillis() + 40 * 60 * 1000;
    long endTime = startTime + (5 + 1000);
    params.put(SPF_KEYS.SPF_START_TIME, Long.toString(startTime));
    params.put(SPF_KEYS.SPF_END_TIME, Long.toString(endTime));
    log.debug("createSchedule  returns "
        + checkXmlMsgForException(lpcpIf.createSchedule(token, params, false)));
  }

  @Test
  public void testDumpSchedulesAndServices() throws Exception {
    List<Schedule> schedules = DbSchedule.INSTANCE.retrieve(null);
    List<ServiceXml> services = DbLightPath.INSTANCE.retrieve(null);
    
    assertNotNull(schedules);
    assertNotNull(services);
    
  }

  @Test
  public void testGetGraphData() throws Exception {
    final GraphData graphData = lpcpIf.getGraphData(token);
    log.debug("getGraphData returns " + graphData);
    assertNotNull(graphData);
  }

  @Test
  public void testGetInfo() throws Exception {
    final ServerInfoType info = lpcpIf.getInfo(token);
    log.debug("getInfo returns " + info);
    assertNotNull(info);
  }

  @Test
  public void testGetInternalBandwithUsage() throws Exception {
    long now = System.currentTimeMillis();
    final List<BandwidthRecord> internalBandwithUsage = lpcpIf.getInternalBandwithUsage(token, now, now);
    log.debug("getInternalBandwithUsage returns "
        + internalBandwithUsage);
    assertNotNull(internalBandwithUsage);
  }

  @Test
  public void testGetLpcpDiscoveryStatus() throws Exception {
    final String lpcpDiscoveryStatus = lpcpIf.getLpcpDiscoveryStatus(token);
    log.debug("getLpcpDiscoveryStatus returns "
        + lpcpDiscoveryStatus);
    assertNotNull(lpcpDiscoveryStatus);
  }

  @Test
  public void testGetLpcpStatus() throws Exception {
    final LpcpStatus lpcpStatus = lpcpIf.getLpcpStatus(token);
    log.debug("getLpcpStatus returns " + lpcpStatus);
    assertNotNull(lpcpStatus);
  }

  /**
   * Test cases starting with testLast need to run after the regular ones as
   * they are destructive
   */
  @Test
  public void testLastEditFacilityDeleteNe() throws Exception {
    List<NetworkElementHolder> neList = neProxy.getNetworkElements(
        neFactoryToken, null);
    List<Facility> facList = neProxy.getFacilities(neFactoryToken, neList
        .get(0).getId());

    // edit the first facility from the first NE in the list.
    Facility f = facList.get(0);
    lpcpIf.editFacility(token, f.getNeId(), f.getAid(), f.getTna(),
        "testLabel", "190", "", "", "1", "0",
        FacilityConstants.SIGNAL_TYPE.OSS.toString(), null, null, null);

    // delete the first Ne in the list
    lpcpIf.deleteNetworkElement(token, neList.get(0));
    final LpcpStatus lpcpStatus = lpcpIf.getLpcpStatus(token);
    log.debug("getLpcpStatus returns " + lpcpStatus);
    assertNotNull(lpcpStatus);
  }

  private String checkXmlMsgForException(ScheduleResult r) throws Exception {
    
    /**
     * <message type="report" aTag="999" ><event type="REPT"
     * command="querysched"><pathEvent><exception message
     * ="getNeIdForTNA cannot find facility for tna: OME0039_OC192-1-9-1"
     * /></pathEvent></event> </message> <message type="report" aTag="999"
     * ><event type="REPT" command="querysched
     * "><pathEvent><exception message="getNeIdForTNA cannot find facility for
     * tna: OME0039_OC192-1-9-1"/></pathEvent></event></message>"
     */

    Element event = XmlUtility.createDocumentRoot(r.getXmlPath());
    if (event == null) {
      return r.toString();
    }

    if (event.getChild("exception") != null) {
      Element exception = event.getChild("exception");
      throw new Exception("operation failed "
          + exception.getAttributeValue("message") + " xml response " + r);
    }

    if (event.getChild("event") != null
        && event.getChild("event").getChild("pathEvent") != null
        && event.getChild("event").getChild("pathEvent").getChild("exception") != null) {
      Element exception = event.getChild("event").getChild("pathEvent")
          .getChild("exception");
      throw new Exception("operation failed "
          + exception.getAttributeValue("message") + " xml response " + r);
    }
    return r.toString();
  }
}
