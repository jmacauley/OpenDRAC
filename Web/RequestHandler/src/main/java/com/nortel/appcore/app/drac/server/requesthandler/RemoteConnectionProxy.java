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

package com.nortel.appcore.app.drac.server.requesthandler;

import java.rmi.Naming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.info.ServerInfo;
import com.nortel.appcore.app.drac.common.info.ServerInfo.ServerState;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;

/**
 * Handles the management of connections to the remote NrbInterface server.
 * 
 */
public class RemoteConnectionProxy {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final String primaryController = System.getProperty(
      "org.opendrac.controller.primary", "localhost");
  private final String secondaryController = System.getProperty(
      "org.opendrac.controller.secondary", "localhost");

  private boolean isPrimaryControllerActive = false;
  private NrbInterface nrbInterface;

  public RemoteConnectionProxy() {
    try {
      getNrbInterface();
    }
    catch (RequestHandlerException e) {
      log.error("Error: ", e);
    }
  }

  private void bindNrb(String serverIp) throws RequestHandlerException {
    final String serverUrl = "rmi://" + serverIp + ":"
        + RmiServerInfo.RMI_REGISTRY_PORT + "/" + RmiServerInfo.NRB_RMI_NAME;
    log.debug("Looking up: " + serverUrl);
    try {
      nrbInterface = (NrbInterface) Naming.lookup(serverUrl);
    }
    catch (Exception e) {
      log.error("Exception: binding Nrb to: " + serverUrl + " has failed", e);
      throw new RemoteConnectionProxyException(
          DracErrorConstants.RH_ERROR_6000_CANNOT_BIND_NRB, null, e);
    }

    if (nrbInterface == null) {
      log.error("NrbInterface is null!");
      throw new RemoteConnectionProxyException(
          DracErrorConstants.RH_ERROR_6000_CANNOT_BIND_NRB, null);
    }
    log.debug("Retrieved: " + nrbInterface);

  }

  public void forceSwitch() throws RequestHandlerException {
    if (isSameController() && isNrbAlive()) {
      return;
    }
    String serverIp;
    if (isPrimaryControllerActive) {
      serverIp = secondaryController;
    }
    else {
      serverIp = primaryController;
    }
    log.warn("Performing switch to " + serverIp);
    try {
      bindNrb(serverIp);
      if (serverIp.equals(primaryController)) {
        isPrimaryControllerActive = true;
      }
      else if (serverIp.equals(secondaryController)) {
        isPrimaryControllerActive = false;
      }
      log.info("Successfully switched to " + serverIp);
    }
    catch (Exception e) {
      log.error("Fatal error performing switchover to: " + serverIp, e);
    }
  }

  /**
   * Returns a reference to the NrbInterface from one of the two server addresses.
   * 
   * @return NrbInterface
   * @throws RequestHandlerException
   */
  public NrbInterface getNrbInterface() throws RequestHandlerException {
    try {
      if (!isNrbAlive()) {
        log.debug("Connecting to primary controller");
        bindNrb(primaryController);
        isPrimaryControllerActive = true;
      }
      return nrbInterface;
    }
    catch (Exception e) {
      log.error("Switching over to: " + secondaryController, e);
    }
    try {
      log.debug("Connecting to secondary controller");
      bindNrb(secondaryController);
      isPrimaryControllerActive = false;
      return nrbInterface;
    }
    catch (Exception e) {
      log.error("Error trying to connect to: " + primaryController + " and :"
          + secondaryController, e);
    }
    throw new RemoteConnectionProxyException(
        DracErrorConstants.RH_ERROR_6000_CANNOT_BIND_NRB, null);
  }

  public ServerInfo getPrimaryControllerInfo() {
    ServerInfo serverInfo = new ServerInfo(primaryController);
    serverInfo.setServerConfig(ServerInfo.ServerConfigType.STANDALONE);
    serverInfo.setMode(ServerInfo.ServerRelationship.PRIMARY);
    if (isSameController() || isPrimaryControllerActive) {
      serverInfo.setState(ServerState.ACTIVE);
    }
    else {
      serverInfo.setState(ServerState.INACTIVE);
    }
    serverInfo.setIpAddress(primaryController);
    return serverInfo;
  }

  public ServerInfo getSecondaryControllerInfo() {
    ServerInfo serverInfo = new ServerInfo(secondaryController);
    serverInfo.setServerConfig(ServerInfo.ServerConfigType.STANDALONE);
    serverInfo.setMode(ServerInfo.ServerRelationship.SECONDARY);
    if (isPrimaryControllerActive) {
      serverInfo.setState(ServerState.INACTIVE);
    }
    else {
      serverInfo.setState(ServerState.ACTIVE);
    }
    serverInfo.setIpAddress(secondaryController);
    return serverInfo;
  }

  public String getServerVersion() {
    return getPrimaryControllerInfo().getSoftwareVersion();
  }
  
  public boolean isSameController() {
    return primaryController.equals(secondaryController);
  }

  private boolean isNrbAlive() {
    if (nrbInterface == null) {
      return false;
    }
    try {
      return nrbInterface.isAlive(LoginToken.getStaticToken());
    }
    catch (Exception e) {
      log.warn("Error: ", e);
      return false;
    }
  }

  public String getPrimaryController() {
    return primaryController;
  }

  public String getSecondaryController() {
    return secondaryController;
  }
}
