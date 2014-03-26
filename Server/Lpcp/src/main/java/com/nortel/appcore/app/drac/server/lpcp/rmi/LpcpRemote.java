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

import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginToken;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
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
import com.nortel.appcore.app.drac.common.utility.RmiOperationHelper;
import com.nortel.appcore.app.drac.server.nrb.LpcpEventCallback;

/**
 * This is the LPCP_PORT instance we bind into the RMI registry and permit
 * remote calls to be made to. It delegates all the real work to the
 * LPCPInterfaceImp class (which also constructs it). We do this so that the
 * lpcp interface will contain RMI stubs for all the interfaces but none of the
 * implementation which we keep in the server jar.
 */

public final class LpcpRemote extends UnicastRemoteObject implements
    LpcpInterface {
  private static final Logger log = LoggerFactory.getLogger(LpcpRemote.class);
  private static final long serialVersionUID = 1L;
  private static InternalLoginToken internalToken;
  private static RmiOperationHelper rmiOperationHelper = new RmiOperationHelper(
      RmiServerInfo.LPCP_RMI_NAME);
  private final LpcpServerInterface lcpServerInterface;

  public LpcpRemote(int port, LpcpServerInterface delegate) throws Exception {
    super(port);
    lcpServerInterface = delegate;
    internalToken = new InternalLoginToken();
    /*
     * Generate a random internal login token and write it to a local file. Only
     * local components will be able to read this file and thus we'll only
     * permit local communications.
     */
    InternalLoginHelper.INSTANCE.setToken(InternalLoginTokenType.LPCP,
        internalToken);
  }

  private static Exception error(Exception t) {
    return rmiOperationHelper.error(t);
  }

  private static void finish() {
    rmiOperationHelper.finish();
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
    rmiOperationHelper.start(name, host, args);
    // somehow equals on the token does not work
    if (token.isValid && internalToken.isValid
        && !token.token.contains(internalToken.token)) {
      log.warn("token        : " + internalToken);
      log.warn("internalToken: " + token);
      throw new Exception(
          "Security exception, invalid internal login token presented :"
              + token);
    }
  }

  @Override
  public void activateService(InternalLoginToken token, String serviceId)
      throws Exception {

    try {
      start(token, "activateService", serviceId);
      lcpServerInterface.activateService(serviceId);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public List<AuditResult> auditModel(InternalLoginToken token)
      throws Exception {
    try {
      start(token, "auditModel");
      List<AuditResult> rc = lcpServerInterface.auditModel();
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void cancelService(InternalLoginToken token, String[] serviceIds,
      SERVICE state) throws Exception {
    try {
      start(token, "cancelService", Arrays.toString(serviceIds), state);
      lcpServerInterface.cancelService(serviceIds, state);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void confirmService(InternalLoginToken token, String serviceId)
      throws Exception {
    try {
      start(token, "confirmService", serviceId);
      lcpServerInterface.confirmService(serviceId);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void correctModel(InternalLoginToken token) throws Exception {
    try {
      start(token, "correctModel");
      lcpServerInterface.correctModel();
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public ScheduleResult createSchedule(InternalLoginToken token,
      Map<SPF_KEYS, String> parms, boolean queryOnly) throws Exception {
    try {
      start(token, "createSchedule", parms, queryOnly);
      ScheduleResult rc = lcpServerInterface.createSchedule(parms, queryOnly);
      finish();
      return rc;
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
      lcpServerInterface.deleteNetworkElement(oldNe);
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
      lcpServerInterface.editFacility(neid, aid, tna, faclabel, mtu, srlg, grp,
          cost, metric2, sigType, constraints, domainId, siteId);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public double getCurrentBandwidthUsage(InternalLoginToken token, String tna)
      throws Exception {
    try {
      start(token, "getCurrentBandwidthUsage", tna);
      double rc = lcpServerInterface.getCurrentBandwidthUsage(tna);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public GraphData getGraphData(InternalLoginToken token) throws Exception {
    start(token, "getGraphData");
    GraphData rc = lcpServerInterface.getGraphData();
    finish();
    return rc;
  }

  @Override
  public ServerInfoType getInfo(InternalLoginToken token) throws Exception {
    try {
      start(token, "getInfo");
      ServerInfoType rc = lcpServerInterface.getInfo();
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public List<BandwidthRecord> getInternalBandwithUsage(
      InternalLoginToken token, long startTime, long endTime) throws Exception {
    try {
      start(token, "getInternalBandwithUsage", startTime, endTime);
      List<BandwidthRecord> rc = lcpServerInterface.getInternalBandwithUsage(
          startTime, endTime);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public String getLpcpDiscoveryStatus(InternalLoginToken token)
      throws Exception {
    try {
      start(token, "getLpcpDiscoveryStatus");
      String rc = lcpServerInterface.getLpcpDiscoveryStatus();
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public LpcpStatus getLpcpStatus(InternalLoginToken token) throws Exception {
    try {
      start(token, "getLpcpStatus");
      LpcpStatus rc = lcpServerInterface.getLpcpStatus();
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public String getPeerIPAddress(InternalLoginToken token) throws Exception {
    try {
      start(token, "getPeerIPAddress");
      String rc = lcpServerInterface.getPeerIPAddress();
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public String getSRLGListForServiceId(InternalLoginToken token,
      String serviceId) throws Exception {
    try {
      start(token, "getSRLGListForServiceId");
      String rc = lcpServerInterface.getSRLGListForServiceId(serviceId);
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public boolean isAlive(InternalLoginToken token) throws Exception {
    try {
      start(token, "isAlive");
      boolean rc = lcpServerInterface.isAlive();
      finish();
      return rc;
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  @Override
  public void registerForLpcpEventNotifications(InternalLoginToken token,
      LpcpEventCallback cb) throws Exception {
    try {
      start(token, "registerForLpcpEventNotifications", cb);
      lcpServerInterface.registerForLpcpEventNotifications(cb);
      finish();
    }
    catch (Exception t) {
      throw error(t);
    }
  }

  public ScheduleResult extendServiceTime(InternalLoginToken token,
      DracService service, Integer minutesToExtendService) throws Exception {
    try {
      start(token, "alterServiceTime", new HashMap<SPF_KEYS, String>(), false);
      ScheduleResult rc = lcpServerInterface.extendServiceTime(service,
          minutesToExtendService);
      finish();
      return rc;
    }
    catch (Exception e) {
      throw error(e);
    }
  }
}
