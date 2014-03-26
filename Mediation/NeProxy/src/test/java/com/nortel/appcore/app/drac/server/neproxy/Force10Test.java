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

package com.nortel.appcore.app.drac.server.neproxy;

import static org.junit.Assert.*;

import java.rmi.Naming;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginToken;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.helper.test.DbTestPopulateDb;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyInterface;
import com.nortel.appcore.app.drac.sshclisimulator.SshCliSimulator;

/**
 * @DOTO: These should be moved into NeFactoryTest later. They are here to help
 *        testing during the development of the force10 box, after it becomes
 *        stable migrate into the NeFactoryTest class.
 * @author pitman
 */
public final class Force10Test {

  private static final Logger log = LoggerFactory.getLogger(Force10Test.class);

  private static Thread neFactoryThread;
  private static NeProxyInterface neProxy;
  private static InternalLoginToken token;
  private static SshCliSimulator sshd;

  @BeforeClass
  public static void setup() throws Exception {
    sshd = new SshCliSimulator(SshCliSimulator.DEFAULT_SETUP_NO_PORT);
    TestHelper.INSTANCE.initialize();
    DbTestPopulateDb.populateTestSystem(false);
    new NeProxyLauncher().start();
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

    // Values marked as unknown will be discovered when the NE 'associates'
    if (sshd != null) {
      for (Integer p : sshd.getInusePorts()) {
        DbNetworkElement.INSTANCE.add(
            new NetworkElementHolder("127.0.0.1", Integer.toString(p), "ADMIN",
                NeType.FORCE10, "UNKNOWN", NeStatus.NE_UNKNOWN, null,
                CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
                NETWORK_ELEMENT_MODE.Unknown, "DEFAULT_PROXY", "",
                PROTOCOL_TYPE.FORCE10_PROTOCOL, true, "Unknown", "UNKNOWN", null, null));
      }
    }
    else {
      DbNetworkElement.INSTANCE.add(
          new NetworkElementHolder("localhost", Integer.toString(22), "admin",
              NeType.FORCE10, "UNKNOWN", NeStatus.NE_UNKNOWN, null,
              CryptoWrapper.INSTANCE.encrypt("myDrac"), 1,
              NETWORK_ELEMENT_MODE.Unknown, "DEFAULT_PROXY", "",
              PROTOCOL_TYPE.FORCE10_PROTOCOL, true, "Unknown", "UNKNOWN", null, null));
    }

    int tries = 0;
    while (tries < 10) {
      try {
        neProxy = (NeProxyInterface) Naming.lookup("//localhost:"
            + RmiServerInfo.RMI_REGISTRY_PORT + "/"
            + RmiServerInfo.NEPROXY_RMI_NAME);
        token = InternalLoginHelper.INSTANCE.getToken(InternalLoginTokenType.NEPROXY);
        neProxy.isAlive(token);
        log.debug("NeFactory is running!");
        break;
      }
      catch (Exception e) {
        log.debug("Could not talk to NeProxy, sleeping and retrying: "+e);
        Thread.sleep(2 * 1000);
      }
      tries++;
    }
    
    assertNotNull("NeProxy is null after 10 retries!", neProxy);

    /*
     * In order to do some useful tests, bring the Neproxy up and running and
     * wait until it has had a chance to align the NEs its managing. We poll to
     * determine when its done to be sure.
     */

    while (true) {
      List<NetworkElementHolder> neList = neProxy.getNetworkElements(token,
          null);
      if (neList == null || neList.isEmpty()) {
        //
      }
      else {
        // Give it a bit time to start aligning or we get some stack tarce later on.  
        Thread.sleep(1000L);
        break;
        // FIXME: This works but is really slow (+/- 1 minutes)

        // boolean allAligned = true;
        // for (NetworkElementHolder ne : neList) {
        // if (!NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
        // allAligned = false;
        // }
        // }
        // if (allAligned) {
        // break;
        // }
        // Thread.sleep(10 * 1000);
      }
    }
  }

  @AfterClass
  public static void tearDownAfterTests() throws Exception {
    if (sshd != null) {
      sshd.stopSshServer(true);
    }
  }

  @Test
  public void testGetNe() throws Exception {
    TestHelper.INSTANCE.initialize();
    List<NetworkElementHolder> neList = neProxy.getNetworkElements(token, null);
    assertEquals(1, neList.size());
  }

}
