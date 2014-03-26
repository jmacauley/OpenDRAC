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

package com.nortel.appcore.app.drac.server.neproxy.mediation.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AssociationEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.TL1LanguageEngine;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;

public final class TL1AssociationController implements PropertyChangeListener {
  private final Logger log = LoggerFactory.getLogger(getClass());
  class HeartBeatThread extends Thread {
    private final Object lock = new Object();
    private int skipCount = 1;
    private final boolean speedup;
    private final int timeout;
    private final int delay;
    private final int skip;
    private boolean terminate;
    boolean sendKeepAlive = true;

    public HeartBeatThread(boolean speedUp, int tl1Timeout, int tl1Delay,
        int tl1Skip, String name) {
      speedup = speedUp;
      timeout = tl1Timeout;
      delay = tl1Delay;
      skip = tl1Skip;
      setName(name);
      log.debug("Heart beat thread launched with " + speedup + " " + timeout
          + " " + delay + " " + skip);
    }

    public void disableKeepAlive() {
      sendKeepAlive = false;
    }

    @Override
    public void run() {

      TL1AssociationEvent event;
      while (!terminate) {
        synchronized (lock) {
          try {
            int sleep = speedup ? delay / skipCount : delay;
            lock.wait(sleep);
          }
          catch (Exception e) {
            log.error("Warning - KeepAlive: ", e);
            break;
          }
        }
        event = null;
        if (sendKeepAlive) {
          AbstractTL1SendHelper helper = new TL1SynchSendHelper(neInfo,
              keepAliveMessage, composer, parser);
          helper.setTimeout(timeout);
          try {
            tl1Engine.send(helper);
          }
          catch (Exception se) {
            log.debug("Failed to communication with NE " + neInfo.getNeName(),
                se);
            event = new TL1AssociationEvent(
                TL1AssociationEvent.ASSOCIATION_DOWN, neInfo);
            sendKeepAlive = false;
            registerListener.receiveEvent(event);
            continue;
          }
          TL1ResponseMessage response = helper.getTL1ResponseMessage();

          // If tid change, that is it. Bring down the association.
          if (!response.getTid().equals(neInfo.getNetworkElementName())) {
            log.error("Problem - Association response with TID change: "
                + response.getCommand() + " " + response.getTid() + " "
                + response.getCompletionCode() + " " + response.getDate()
                + "\n" + response.toString());
            event = new TL1AssociationEvent(TL1AssociationEvent.WRONG_TID,
                neInfo);
            sendKeepAlive = false;
            skipCount = 1;
            registerListener.receiveEvent(event);
            continue;
          }

          /*
           * if (response.getCompletionCode() == null) { if
           * (!tl1Engine.isConnected()) { try {
           * tl1Engine.connect(neInfo.getAddressInfo().getPrimaryIPAddress(),
           * neInfo.getAddressInfo().getPrimaryPort()); } catch (Exception e) {
           *  } } continue; }
           */
          // cannot bring the association down because of output is
          // ill-formatted for some
          // strange reason i.e. high rate.
          if (response.getInternalCompletionCode() != TL1ResponseMessage.COMPLETION_CODE.SUCCESS
              && response.getInternalCompletionCode() != TL1ResponseMessage.COMPLETION_CODE.TIME_OUT
              && response.getCompletionCode()
                  .equals(TL1ResponseMessage.UNKNOWN)) {
            log.error("Warning - Unknown completion code with "
                + "Internal Code = " + response.getInternalCompletionCode()
                + " for " + response.getTid() + " Ctag " + response.getCtag()
                + "\n" + response.toString());
            continue;
          }

          // TL1 output still processed, who cares if heartbeat output is not
          // arrived yet
          if (response.getInternalCompletionCode() == TL1ResponseMessage.COMPLETION_CODE.TIME_OUT
              && tl1Engine.getMessageQueueSize() > 0) {
            skipCount = 1;
            
            continue;
          }
          if (!TL1Constants.COMPLETED.equals(response.getCompletionCode())) {
            /*
             * We do not raise the association down until heartbeat fails 3
             * times for whatever the reason is.
             */
            if (skipCount <= skip) {
              
              skipCount++;
              continue;
            }
            log.debug("Exception - Association down - could not contact NE: "
                + response.getCommand() + " " + response.getTid() + " "
                + response.getCompletionCode() + " " + response.getDate()
                + "\n" + response.toString());
            event = new TL1AssociationEvent(
                TL1AssociationEvent.ASSOCIATION_DOWN, neInfo);
          }
          if (registerListener != null && event != null) {
            sendKeepAlive = false;
            registerListener.receiveEvent(event);
          }
          skipCount = 1;
        }
      }
    }

    public void terminateHeartBeat() {
      terminate = true;
      sendKeepAlive = false;
      wakeup();
    }

    public void wakeup() {
      synchronized (lock) {
        lock.notify();
      }
    }
  }

  /**
   * Global lock to stop us sending more than 1 ACT-USER at a time.
   */
  private static final Object GLOBAL_ACT_USER_LOCK = new Object();
  private final NetworkElementInfo neInfo;
  private final TL1LanguageEngine tl1Engine;
  private TL1AssociationListener registerListener;

  // private TL1RequestMessage loginMessage;
  // private PropertyChangeListener connectedListener;
  private final TL1RequestMessage keepAliveMessage;
  private TL1Composer composer;
  private TL1LineParser parser;
  private HeartBeatThread heartBeatThread;

  // //// Properties to control heartbeat
  private static final String HEARTBEAT_SKIP_COUNT_DEFAULT = "2";
  private static final String HEARTBEAT_TIMEOUT_DEFAULT = "60";
  private static final String HEARTBEAT_DELAY_DEFAULT = "20";

  public TL1AssociationController(NetworkElementInfo ne,
      TL1LanguageEngine engine, TL1Composer newComposer, TL1LineParser newParser) {
    neInfo = ne;
    tl1Engine = engine;
    composer = newComposer;
    parser = newParser;
    registerListener = null;
    heartBeatThread = null;
    Map<String, String> param2 = new HashMap<String, String>();
    param2.put(ObjectElement.OPERATION_KEY, Tl1CommandCode.RTRV_HDR.toString());
    keepAliveMessage = new TL1RequestMessage(Tl1CommandCode.RTRV_HDR, param2);
  }

  public void addListener(TL1AssociationListener listener) {
    registerListener = listener;
  }

  public void login() throws Exception {
    tl1Engine.addPropertyChangeListener(TL1LanguageEngine.CONNECTED, this);

    AbstractTL1SendHelper helper = new TL1SynchSendHelper(neInfo,
        keepAliveMessage, composer, parser);
    tl1Engine.send(helper);
    TL1ResponseMessage response = helper.getTL1ResponseMessage();
    if (!response.getTid().equals(neInfo.getNetworkElementName())) {
      neInfo.setNeName(response.getTid());
      log.debug("Updated TID to <" + response.getTid() + "> for "
          + neInfo.getAddressInfo().getPrimaryIPAddress() + " "
          + neInfo.getAddressInfo().getPrimaryPort());
    }

    /**
     * WP: June 2009. Globally only permit a single ACT-USER command to be
     * outstanding at once. Why? Thanks to OMEA stupidity, if the NE is set to
     * use OMEA for radius authentication then we hit a bug with OMEA. OMEA can
     * only process a single RADIUS login request at a time, if we have 10
     * threads all trying to login to NEs after a restart we'll have 10
     * outstanding RADIUS requests and most/all will fail and mess us up big
     * time. We used to globally restrict the number of worker threads to just
     * 1, but that slows down the rest of DRAC, especially if we have > 1 NE
     * that are unreachable, we'll spend too much time trying to reconnect to
     * the dead NEs. Instead we lock just the ACT-USER phase.
     */

    synchronized (GLOBAL_ACT_USER_LOCK) {
      helper = new TL1SynchSendHelper(neInfo, buildLoginMsg(), composer, parser);
      tl1Engine.send(helper);
      response = helper.getTL1ResponseMessage();
    }

    TL1AssociationEvent event;

    // Check for TID
    if (!response.getTid().equals(neInfo.getNetworkElementName())) {
      event = new TL1AssociationEvent(TL1AssociationEvent.WRONG_TID, neInfo);
      if (heartBeatThread != null) {
        heartBeatThread.disableKeepAlive();
      }

      if (registerListener != null) {
        registerListener.receiveEvent(event);
      }
      return;
    }
    if (!response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
      Exception ex = new Exception("AssciationException: failed to login"); // comment
      
      throw ex;
    }
    if (registerListener != null) {
      event = new TL1AssociationEvent(TL1AssociationEvent.ASSOCIATION_UP,
          neInfo);
      registerListener.receiveEvent(event);
    }
    else {
      log.debug("Association:  No listener " + neInfo.getNetworkElementName()
          + " for event ASSOCIATION_UP");
    }

    heartBeatStarts();
  }

  public void logout() throws Exception {
    Map<String, String> param1 = new HashMap<String, String>();
    param1
        .put(ObjectElement.OPERATION_KEY, Tl1CommandCode.CANC_USER.toString());
    param1.put("UID", neInfo.getUserProfile().getUserID());
    TL1RequestMessage logoutMessage = new TL1RequestMessage(
        Tl1CommandCode.CANC_USER, param1);
    AbstractTL1SendHelper helper = new TL1SynchSendHelper(neInfo,
        logoutMessage, composer, parser);
    tl1Engine.send(helper);

    // terminate = true;
    // associationThread. interrupt();

    TL1ResponseMessage response = helper.getTL1ResponseMessage();
    if (!response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
      // String payload[] = response.getTextBlocks();

      throw new Exception("Failed to log out " + response);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent e) {
    Boolean connected = (Boolean) e.getNewValue();

    
    TL1AssociationEvent event;
    if (connected.booleanValue()) {
      event = new TL1AssociationEvent(TL1AssociationEvent.ASSOCIATION_UP,
          neInfo);
    }
    else {
      event = new TL1AssociationEvent(TL1AssociationEvent.ASSOCIATION_DOWN,
          neInfo);
    }

    if (registerListener != null) {
      registerListener.receiveEvent(event);
    }

    if (heartBeatThread != null) {
      heartBeatThread.terminateHeartBeat();
      ;
    }

    /*
     * If the connection is dropped, the only way to re establish the connection
     * is to open the new socket. But we cannot rely on the IP address and port
     * number that we currently have since, NE Discovery Manager may switch to
     * the different set Anyhow, just terminate the thread and wait for next
     * instruction
     */
    // terminateAssociation();
  }

  public void reloadParserComposer(TL1Composer newComposer,
      TL1LineParser newParser) {
    composer = newComposer;
    parser = newParser;
  }

  public synchronized void terminateAssociation() {
    registerListener = null;
    if (tl1Engine != null) {
      tl1Engine.removePropertyChangeListener(TL1LanguageEngine.CONNECTED, this);
    }

    if (heartBeatThread != null) {
      heartBeatThread.terminateHeartBeat();

    }
  }

  private TL1RequestMessage buildLoginMsg() throws Exception {
    Map<String, String> param = new HashMap<String, String>();
    param.put(
        "PID",
        CryptoWrapper.INSTANCE.decrypt(
            neInfo.getUserProfile().getLoginPassword()));
    param.put(ObjectElement.OPERATION_KEY, Tl1CommandCode.ACT_USER.toString());
    param.put("UID", neInfo.getUserProfile().getUserID());
    return new TL1RequestMessage(Tl1CommandCode.ACT_USER, param);
  }

  private void heartBeatStarts() throws Exception {
    // Initializing all necessary properties.
    heartBeatThread = new HeartBeatThread(
        System.getProperty("org.opendrac.proxy.heartbeat.speedup", "false")
            .equals("true") ? true : false,
        Integer.parseInt(System.getProperty(
            "org.opendrac.proxy.heartbeat.timeout", HEARTBEAT_TIMEOUT_DEFAULT)),
        Integer.parseInt(System.getProperty(
            "org.opendrac.proxy.heartbeat.delay", HEARTBEAT_DELAY_DEFAULT)) * 1000,
        Integer.parseInt(System.getProperty(
            "org.opendrac.proxy.heartbeat.skip", HEARTBEAT_SKIP_COUNT_DEFAULT)),
        "HeartBeat thread " + neInfo.getAddressInfo().toString());
    heartBeatThread.setDaemon(true);
    heartBeatThread.start();
  }

}
