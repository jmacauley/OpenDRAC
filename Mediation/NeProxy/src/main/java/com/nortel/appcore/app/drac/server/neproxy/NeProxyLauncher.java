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

import java.rmi.Naming;

import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementConnection;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyInterface;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyInterfaceImpl;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyRemote;

/**
 * @author nguyentd
 */
public final class NeProxyLauncher {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private NeProxyInterfaceImpl neProxyInterfaceImpl;

  public void start() {
    try {
      initializeDbFacilities();
      exportNeProxy();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void exportNeProxy() throws Exception {
    if (neProxyInterfaceImpl == null) {
      neProxyInterfaceImpl = new NeProxyInterfaceImpl();
    }
    NeProxyRemote neProxyRemote = neProxyInterfaceImpl.getRemote();
    String bindName = RmiServerInfo.NEPROXY_RMI_BINDING_NAME;
    log.debug("Registering NeProxyRemote interface with: " + bindName + ", "
        + neProxyRemote);
    Naming.rebind(bindName, neProxyRemote);
    // Can we lookup ourselves and invoke a method on us?
    final NeProxyInterface neProxyInterface = (NeProxyInterface) Naming
        .lookup(bindName);
    log.debug("Lookup got: " + neProxyInterface);
    log.debug("Is NeProxy alive? "
        + neProxyInterface.isAlive(InternalLoginHelper.INSTANCE
            .getToken(InternalLoginTokenType.NEPROXY)));
  }

  private void initializeDbFacilities() {
    try {
      // Clear db tables that will retrieved during alignment
      log.debug("Clearing the following db tables on server startup: adjacencies, connections");
      DbNetworkElementAdjacency.INSTANCE.deleteAllNonManual();
      DbNetworkElementConnection.INSTANCE.deleteAll();
      // Here, we need a foolproof way to set all facilities invalid; cannot
      // rely on iterating any NE lists at this point, since, as the server
      // comes up, there could
      // be references in facilities to an NE that is no longer managed, or, an
      // NE whose NEId has been changed.
      DbNetworkElementFacility.INSTANCE.invalidateAllFacilities();

    }
    catch (Exception e) {
      throw new RuntimeException("NeProxy initialization failed", e);
    }
  }

  public void stop() {
    DiscoverNePool.INSTANCE.terminateThread();
    try {
      log.debug("Waiting for shutdown");
      Thread.sleep(1500L);
      Naming.unbind(RmiServerInfo.NEPROXY_RMI_BINDING_NAME);
    }
    catch (Exception e) {
      
    }
  }
}
