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

package com.nortel.appcore.app.drac.server.lpcp.rmi;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.Naming;
import java.rmi.Remote;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opendrac.launcher.RmiLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.AuditResult;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.GraphData;
import com.nortel.appcore.app.drac.common.types.LpcpStatus;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.server.nrb.LpcpEventCallback;

public class LpcpRemoteTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * This is a do nothing delegate for testing the NRB_PORT infrastructure.
   * 
   * @author pitman
   */
  static class Delegate implements LpcpServerInterface, Remote, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public void activateService(String serviceId) throws Exception {
      return;
    }

    @Override
    public List<AuditResult> auditModel() throws Exception {
      return null;
    }

    @Override
    public void cancelService(String[] serviceIds, SERVICE state)
        throws Exception {
      return;
    }

    @Override
    public void confirmService(String serviceId) throws Exception {
      return;
    }

    @Override
    public void correctModel() throws Exception {
      return;
    }

    @Override
    public void deleteNetworkElement(NetworkElementHolder oldNe)
        throws Exception {
      return;
    }

    @Override
    public void editFacility(String neid, String aid, String tna,
        String faclabel, String mtu, String srlg, String grp, String cost,
        String metric2, String sigType, String constraints, String domainId,
        String siteId) throws Exception {
      return;
    }

    @Override
    public double getCurrentBandwidthUsage(String tna) throws Exception {
      return 0;
    }

    @Override
    public GraphData getGraphData() throws Exception {
      return null;
    }

    @Override
    public ServerInfoType getInfo() throws Exception {
      return null;
    }

    @Override
    public List<BandwidthRecord> getInternalBandwithUsage(long startTime,
        long endTime) throws Exception {
      return null;
    }

    @Override
    public String getLpcpDiscoveryStatus() throws Exception {
      return null;
    }

    @Override
    public LpcpStatus getLpcpStatus() throws Exception {
      return null;
    }

    @Override
    public String getPeerIPAddress() throws Exception {
      return null;
    }

    @Override
    public String getSRLGListForServiceId(String serviceId) throws Exception {
      return null;
    }

    @Override
    public boolean isAlive() throws Exception {
      return false;
    }

    @Override
    public void registerForLpcpEventNotifications(LpcpEventCallback cb)
        throws Exception {
      return;
    }

    @Override
    public ScheduleResult createSchedule(Map<SPF_KEYS, String> parms,
        boolean queryOnly) throws Exception {
      return null;
    }

    @Override
    public ScheduleResult extendServiceTime(DracService service,
        Integer minutesToExtendService) throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

  }

  @Test
  public void testLpcpRemote() throws Exception {
    // Bind our rmi registry and LPCP_PORT to port zero so we'll work anywhere.
    final int rmiRandomPort = 1099;
    final RmiLauncher rmiLauncher = new RmiLauncher();
    rmiLauncher.start();
    log.debug("helper " + rmiLauncher);
    LpcpRemote lpcp = new LpcpRemote(rmiRandomPort, new Delegate());
    log.debug("Binding " + RmiServerInfo.LPCP_RMI_NAME);
    Naming.rebind(RmiServerInfo.LPCP_RMI_NAME, lpcp);
    LpcpInterface remoteLpcp = (LpcpInterface) Naming
        .lookup(RmiServerInfo.LPCP_RMI_NAME);
    assertNotNull(remoteLpcp);

    // bogus but good enough
    // Use reflection to call all of the methods in the interface remote class.

    int ok = rmiRandomPort;
    for (Method m : remoteLpcp.getClass().getDeclaredMethods()) {

      if (!Modifier.isPublic(m.getModifiers())
          || Modifier.isStatic(m.getModifiers())) {
        log.debug("Skipping method " + m.getName() + " " + ok);
        continue;
      }

      // Type[] argTypes = m.getGenericParameterTypes();
      Class<?>[] argTypes = m.getParameterTypes();
      Object[] args = new Object[argTypes.length];

      for (int i = 1; i < args.length; i++) {
        if (argTypes[i].isPrimitive()) {
          if (argTypes[i] == Integer.TYPE) {
            args[i] = Integer.valueOf(rmiRandomPort);
          }
          else if (argTypes[i] == Long.TYPE) {
            args[i] = Long.valueOf(rmiRandomPort);
          }
          else if (argTypes[i] == Boolean.TYPE) {
            args[i] = Boolean.FALSE;
          }
          else if (argTypes[i] == Double.TYPE) {
            args[i] = Double.valueOf(rmiRandomPort);
          }
          else {
            fail("Unsupported type, enhance test case for primiative ");
          }
        }
      }
    }
    rmiLauncher.stop();
  }

}
