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

package com.nortel.appcore.app.drac.server.neproxy.rmi;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.Remote;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.opendrac.launcher.RmiLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.utility.ProvisioningResultHolder;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyEventCallback;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyInterface;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyRemote;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyServerInterface;

public class NeProxyRemoteTest {

  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * This is a do nothing delegate for testing the infrastructure.
   * 
   * @author pitman
   */
  static class NeProxyServerInterfaceTestDelegate implements
      NeProxyServerInterface, Remote, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public void addManualAdjacency(String sourceIEEE, String sourcePort,
        String destIEEE, String destPort) throws Exception {
    }

    @Override
    public void changeNetworkElementPassword(NetworkElementHolder updatedNE)
        throws Exception {
    }

    @Override
    public ProvisioningResultHolder createConnection(CrossConnection xcon)
        throws Exception {
      return null;
    }

    @Override
    public ProvisioningResultHolder deleteConnection(CrossConnection xcon)
        throws Exception {
      return null;
    }

    @Override
    public void deleteCrossConnection(CrossConnection xcon) throws Exception {
    }

    @Override
    public void deleteManualAdjacency(String neIEEE, String port)
        throws Exception {
    }

    @Override
    public void deleteManualAdjacency(String sourceIEEE, String sourcePort,
        String destIEEE, String destPort) throws Exception {
    }

    @Override
    public void deleteNetworkElement(NetworkElementHolder oldNe)
        throws Exception {
    }

    @Override
    public void editFacility(String neid, String aid, String tna,
        String faclabel, String mtu, String srlg, String grp, String cost,
        String metric2, String sigType, String constraints, String domainId,
        String siteId) throws Exception {
    }

    @Override
    public void enrollNetworkElement(NetworkElementHolder newNe)
        throws Exception {
    }

    @Override
    public List<CrossConnection> getCrossConnections(String targetNeId)
        throws Exception {
      return null;
    }

    @Override
    public List<Facility> getFacilities(String targetNeId) throws Exception {
      return null;
    }

    @Override
    public List<NetworkElementHolder> getNetworkElements(String tid)
        throws Exception {
      return null;
    }

    @Override
    public String getXmlAlarm(String neId) throws Exception {
      return null;
    }

    @Override
    public boolean isAlive() throws Exception {
      return false;
    }

    // @Override
    // public String getXmlAdjacency(String neId)
    // throws Exception
    // {
    // return null;
    // }

    @Override
    public ProvisioningResultHolder postDeleteConnections(
        List<CrossConnection> xconList) throws Exception {
      return null;
    }

    @Override
    public ProvisioningResultHolder prepCreateConnections(
        List<CrossConnection> xconList) throws Exception {
      return null;
    }

    @Override
    public void registerForEventNotifications(NeProxyEventCallback cb)
        throws Exception {

    }

    @Override
    public void toggleNetworkElementAssociation(NetworkElementHolder existingNe)
        throws Exception {
    }

    @Override
    public void updateNetworkElementPosition(String ip, String port,
        Double positionX, Double positionY) {
    }

    @Override
    public void updateAddressAndPort(final String oldAddress,
        final int oldPort, final String newAddress, final int newPort)
        throws Exception {

    }

  }

  @Test
  public void testNeProxyRemote() throws Exception {
    RmiLauncher rmiLauncher = new RmiLauncher();
    rmiLauncher.start();
    NeProxyRemote neProxy = new NeProxyRemote(RmiServerInfo.RMI_REGISTRY_PORT,
        new NeProxyServerInterfaceTestDelegate());
    Naming.rebind(RmiServerInfo.NEPROXY_RMI_NAME, neProxy);
    NeProxyInterface remoteNeProxy = (NeProxyInterface) Naming
        .lookup(RmiServerInfo.NEPROXY_RMI_NAME);
    assertNotNull(remoteNeProxy);
    assertTrue(remoteNeProxy.isAlive(NeProxyRemote.getToken()));
    rmiLauncher.stop();

  }

}
