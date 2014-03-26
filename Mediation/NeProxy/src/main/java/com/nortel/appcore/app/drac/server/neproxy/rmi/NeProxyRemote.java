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

import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import javax.sql.DataSource;

import org.opendrac.ioc.IocContainer;
import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginToken;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.utility.ProvisioningResultHolder;
import com.nortel.appcore.app.drac.common.utility.RmiOperationHelper;

/**
 * This is the NEProxy instance we bind into the RMI registry and permit remote
 * calls to be made to. It delegates all the real work to the
 * NeProxyServerInterface class (which also constructs it). We do this so that
 * the interface will contain RMI stubs for all the interfaces but none of the
 * implementation which we keep in the server jar.
 */

public final class NeProxyRemote extends UnicastRemoteObject implements
    NeProxyInterface {

  private static final Logger log = LoggerFactory
      .getLogger(NeProxyRemote.class);

  private static final long serialVersionUID = 1L;
  private static InternalLoginToken internalToken;
  private static RmiOperationHelper roh = new RmiOperationHelper(
      "NeProxyInterface");
  private final NeProxyServerInterface d;

  public NeProxyRemote(int port, NeProxyServerInterface delegate)
      throws Exception {
    super(port);
    d = delegate;
    internalToken = new InternalLoginToken();

    /*
     * Generate a random internal login token. Only local components will be
     * able to read this file and thus we'll only permit local communications.
     */
    InternalLoginHelper.INSTANCE.setToken(InternalLoginTokenType.NEPROXY,
        internalToken);
  }

  public static InternalLoginToken getToken() {
    return internalToken;
  }

  private static Exception error(Exception t) {
    return roh.error(t);
  }

  private static void finish() {
    roh.finish();
  }

  private static void start(InternalLoginToken token, String name,
      Object... args) throws Exception {
    String host;
    try {
      host = getClientHost();
    }
    catch (ServerNotActiveException e) {
      host = System.getProperty("org.opendrac.controller.primary", "localhost");
    }
    roh.start(name, host, args);
    // somehow equals on the token does not work
    if (token.isValid && internalToken.isValid
        && !token.token.contains(internalToken.token)) {
      log.warn("token        : " + internalToken);
      log.warn("internalToken: " + token);
      throw new Exception("Security exception, invalid internal login token");
    }
  }

  @Override
  public void addManualAdjacency(InternalLoginToken token, String sourceIEEE,
      String sourcePort, String destIEEE, String destPort) throws Exception {
    try {
      start(token, "addManualAdjacency", sourceIEEE, sourcePort, destIEEE,
          destPort);
      d.addManualAdjacency(sourceIEEE, sourcePort, destIEEE, destPort);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void changeNetworkElementPassword(InternalLoginToken token,
      NetworkElementHolder updatedNE) throws Exception {
    try {
      start(token, "changeNetworkElementPassword", updatedNE);
      d.changeNetworkElementPassword(updatedNE);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public ProvisioningResultHolder createConnection(InternalLoginToken token,
      CrossConnection xcon) throws Exception {
    try {
      start(token, "createConnection", xcon);
      ProvisioningResultHolder rc = d.createConnection(xcon);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public ProvisioningResultHolder deleteConnection(InternalLoginToken token,
      CrossConnection xcon) throws Exception {
    try {
      start(token, "deleteConnection", xcon);
      ProvisioningResultHolder rc = d.deleteConnection(xcon);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void deleteCrossConnection(InternalLoginToken token,
      CrossConnection xcon) throws Exception {
    try {
      start(token, "deleteCrossConnection", xcon);
      d.deleteCrossConnection(xcon);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void deleteManualAdjacency(InternalLoginToken token, String neIEEE,
      String port) throws Exception {
    try {
      start(token, "deleteManualAdjacency", neIEEE, port);
      d.deleteManualAdjacency(neIEEE, port);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void deleteManualAdjacency(InternalLoginToken token,
      String sourceIEEE, String sourcePort, String destIEEE, String destPort)
      throws Exception {
    try {
      start(token, "deleteManualAdjacency", sourceIEEE, sourcePort, destIEEE,
          destPort);
      d.deleteManualAdjacency(sourceIEEE, sourcePort, destIEEE, destPort);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void deleteNetworkElement(InternalLoginToken token,
      NetworkElementHolder oldNe) throws Exception {
    try {
      start(token, "deleteNetworkElement", oldNe);
      d.deleteNetworkElement(oldNe);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }

  }

  @Override
  public void editFacility(InternalLoginToken token, String neid, String aid,
      String tna, String faclabel, String mtu, String srlg, String grp,
      String cost, String metric2, String sigType, String constraints,
      String domainId, String siteId) throws Exception {
    try {
      start(token, "editFacility", neid, aid, tna, faclabel, mtu, srlg, grp,
          cost, metric2, sigType, constraints, domainId, siteId);
      d.editFacility(neid, aid, tna, faclabel, mtu, srlg, grp, cost, metric2,
          sigType, constraints, domainId, siteId);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void enrollNetworkElement(InternalLoginToken token,
      NetworkElementHolder newNe) throws Exception {
    try {
      start(token, "enrollNetworkElement", newNe);
      d.enrollNetworkElement(newNe);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public List<CrossConnection> getCrossConnections(InternalLoginToken token,
      String targetNeId) throws Exception {
    try {
      start(token, "getCrossConnections", targetNeId);
      List<CrossConnection> rc = d.getCrossConnections(targetNeId);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public List<Facility> getFacilities(InternalLoginToken token,
      String targetNeId) throws Exception {
    try {
      start(token, "getFacilities", targetNeId);
      List<Facility> rc = d.getFacilities(targetNeId);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public List<NetworkElementHolder> getNetworkElements(
      InternalLoginToken token, String tid) throws Exception {
    try {
      start(token, "getNetworkElements", tid);
      List<NetworkElementHolder> rc = d.getNetworkElements(tid);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public String getXmlAlarm(InternalLoginToken token, String neId)
      throws Exception {
    try {
      start(token, "getXmlAlarm", neId);
      String rc = d.getXmlAlarm(neId);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public boolean isAlive(InternalLoginToken token) throws Exception {
    start(token, "isAlive");
    d.isAlive();
    finish();
    return true;
  }

  @Override
  public ProvisioningResultHolder postDeleteConnections(
      InternalLoginToken token, List<CrossConnection> xconList)
      throws Exception {
    try {
      start(token, "deleteConnection", xconList);
      ProvisioningResultHolder rc = d.postDeleteConnections(xconList);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public ProvisioningResultHolder prepCreateConnections(
      InternalLoginToken token, List<CrossConnection> xconList)
      throws Exception {
    try {
      start(token, "prepCreateConnections", xconList);
      ProvisioningResultHolder rc = d.prepCreateConnections(xconList);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void registerForEventNotifications(InternalLoginToken token,
      NeProxyEventCallback cb) throws Exception {
    try {
      start(token, "registerForEventNotifications");
      d.registerForEventNotifications(cb);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void toggleNetworkElementAssociation(InternalLoginToken token,
      NetworkElementHolder existingNe) throws Exception {
    try {
      start(token, "toggleNetworkElementAssociation", existingNe);
      d.toggleNetworkElementAssociation(existingNe);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void updateNetworkElementPosition(InternalLoginToken token, String ip,
      String port, Double positionX, Double positionY) throws Exception {
    try {
      start(token, "updateNetworkElementPosition", ip, port, positionX,
          positionY);
      d.updateNetworkElementPosition(ip, port, positionX, positionY);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void updateAddressAndPort(InternalLoginToken token,
      final String oldAddress, final int oldPort, final String newAddress,
      final int newPort) throws Exception {
    try {
      start(token, "updateAddressAndPort", oldAddress, oldPort, newAddress,
          newPort);
      d.updateAddressAndPort(oldAddress, oldPort, newAddress, newPort);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }

  }

}
