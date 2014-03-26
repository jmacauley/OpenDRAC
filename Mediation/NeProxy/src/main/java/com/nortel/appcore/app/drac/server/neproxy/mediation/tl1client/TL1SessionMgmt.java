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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.controller.AbstractTL1SendHelper;
import com.nortel.appcore.app.drac.server.neproxy.mediation.controller.TL1AlarmController;
import com.nortel.appcore.app.drac.server.neproxy.mediation.controller.TL1AssociationController;
import com.nortel.appcore.app.drac.server.neproxy.mediation.controller.TL1Composer;
import com.nortel.appcore.app.drac.server.neproxy.mediation.controller.TL1LineParser;
import com.nortel.appcore.app.drac.server.neproxy.mediation.controller.TL1SynchSendHelper;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1LanguageEngineFactoryProxy;

public final class TL1SessionMgmt {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private TL1AssociationController assController;
  private TL1AlarmController alarmController;
  private NetworkElementInfo neInfo;
  private boolean beingManaged;
  private TL1Composer composer;
  private TL1LineParser parser;
  // This is the TL1 engine which messages can be sent to.
  private TL1LanguageEngine tl1Engine;

  // With the introduction of 10GE support, a full 10G VCAT service issued from
  // DRAC
  // would otherwise result in 64 member connection requests being issued to one
  // NE
  // in 'parallel'. This was enough to set the NE into a resource busy state;
  // the
  // request would eventually timeout and fail. The number of outstanding TL1
  // requests
  // to an NE must therefore be regulated.
  private static int MAX_PER_NE_OUTSTANDING_TL1_REQUESTS = Integer
      .parseInt(System.getProperty("MaxPerNeOutstandingTL1Requests", "7"));
  private Semaphore tl1Semaphore = null;

  // private static final String RANGE = "&&";
  // private Hashtable listeners;
  // private boolean workOnDatabase = false;

  public TL1SessionMgmt(NetworkElementInfo ne) {
    this(ne, false);
  }

  public TL1SessionMgmt(NetworkElementInfo ne, boolean meteredTl1Session) {
    neInfo = ne;

    /* -------------------- Some tuning... -------------------- */
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader("MaxPerNeOutstandingTL1Requests"));
      MAX_PER_NE_OUTSTANDING_TL1_REQUESTS = br == null ? Integer.MIN_VALUE
          : Integer.parseInt(br.readLine());
    }
    catch (Exception e) {
    }
    finally {
      if (br != null) {
        try {
          br.close();
        }
        catch (Exception e) {
        }
      }
    }
    /* -------------------------------------------------------- */

    if (meteredTl1Session) {
      log.debug("NetworkElement semaphore permit size: "
          + MAX_PER_NE_OUTSTANDING_TL1_REQUESTS);
      this.tl1Semaphore = new Semaphore(MAX_PER_NE_OUTSTANDING_TL1_REQUESTS,
          true);
    }

  }

  // Acquires a permit, if one is available and returns immediately, reducing
  // the number of available
  // permits by one.
  // If no permit is available then the current thread blocks until another
  // thread releases a permit.
  public void acquireTL1Permit() throws Exception {
    tl1Semaphore.acquire();
  }

  public void deleteTL1Session(boolean logout) {
    if (!beingManaged) {
      return;
    }
    try {
      beingManaged = false;
      assController.terminateAssociation();
      alarmController.terminateController();
      tl1Engine.dispose();
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  public void establishTL1Session() throws Exception {
    final int port = neInfo.getAddressInfo().getPrimaryPort();
    final String ip = neInfo.getAddressInfo().getPrimaryIPAddress();

    tl1Engine.connect(ip, port);
    assController.login();
    alarmController.establish();
  }

  public TL1AlarmController getAlarmController() {
    return alarmController;
  }

  public TL1AssociationController getAssociationController() {
    return assController;
  }

  /**
   * Return the TL1 engine, or null if none exists.
   */
  public TL1LanguageEngine getTl1Engine() {
    return tl1Engine;
  }

  /**
   * invokeAsynch method comment.
   */
  /*
   * public int invokeAsynch(Hashtable request, TL1AsynchListener listener)
   * throws java.lang.Exception { Vector results = null; List respBlocks = null;
   * Printlog.println("Request from COM: " + request);
   * setCommandOperation(request); processCompoundFields(request);
   * TL1RequestMessage message = new TL1RequestMessage( (String)
   * request.get(ObjectElement.operationKey), toHashMap(request)); String cTag =
   * sendAsyncMessage(message, listener); try { return Integer.parseInt(cTag); }
   * catch (NumberFormatException e) { //// We know there is a problem here with
   * ctag return 0; } }
   */
  public boolean isConnected() {
    return tl1Engine.isConnected();
  }

  public void logout() throws Exception {
    if (assController != null) {
      assController.logout();
    }
  }

  public void manageNE() throws Exception {

    tl1Engine = TL1LanguageEngineFactoryProxy.createTL1LanguageEngine();
    // String platform = System.getProperty("os.name");

    composer = new TL1Composer(neInfo);
    parser = new TL1LineParser(neInfo);

    assController = new TL1AssociationController(neInfo, tl1Engine, composer,
        parser);
    alarmController = new TL1AlarmController(neInfo, tl1Engine, parser);
    beingManaged = true;
  }

  public void releaseTL1Permit() throws Exception {
    tl1Semaphore.release();
  }

  // public TL1SessionMgmt(String wrapperFile, String uniqueId, String
  // ipAddress, int port, String userId,
  // String passwd)
  // {
  // IPAddressInfo ipInfo = new IPAddressInfo(ipAddress, port);
  // UserProfile uProfile = new UserProfile(userId, passwd);
  // neInfo = new NetworkElementInfo(uniqueId, "", wrapperFile, ipInfo,
  // uProfile);
  // }

  public void reloadNeInfo(NetworkElementInfo ne) {
    try {
      this.neInfo = ne;
      // deleteTL1Session(true);
      // manageNE();
      // this.establishTL1Session();

      composer = new TL1Composer(neInfo);
      parser = new TL1LineParser(neInfo);
      this.assController.reloadParserComposer(composer, parser);
      this.alarmController.reloadParser(parser);
      
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  /**
   * Wayne Pitman: Lower level TL1 command/response. Return the unparsed
   * response message (copied from else where)
   */
  public TL1ResponseMessage sendManualToNe(Map<String, String> request)
      throws Exception {
    List<Map<String, String>> respBlocks = null;

    

    Tl1CommandCode operationKey = Tl1CommandCode.fromString(request
        .get(ObjectElement.OPERATION_KEY));
    if (operationKey == null) {
      
    }
    TL1RequestMessage message = new TL1RequestMessage(operationKey, request);
    TL1ResponseMessage response = sendSyncMessage(message);
    respBlocks = response.getPayload();
    if (respBlocks == null || respBlocks.isEmpty()) {
      
      return null;
    }

    
    return response;
  }

  /*
   * public java.util.Vector retrieveAO( int fromATAG, int toATAG,
   * java.lang.String mesgtype, int timeout) throws java.lang.Exception { Vector
   * results = null; List payloads = null; try { NetworkElement ne = Mediation
   * .getMediationFramework() .getNetworkElementManager()
   * .searchNetworkElement(neInfo); if (ne == null) throw new
   * TL1ProcessingException( ObjectElement.retrieveAO, null,
   * "Invalid NE object"); TL1ManagementGW neGW = ne.getGW(); if (neGW == null)
   * throw new TL1ProcessingException( ObjectElement.retrieveAO, null,
   * "No NE gate way object"); /// Build the AO request information Hashtable
   * request = new SimpleHashtable(); request.put(ObjectElement.operationKey,
   * ObjectElement.retrieveAO); if (fromATAG > 0 && toATAG > 0) request.put(
   * TL1FieldKeys.ATAG_SEQUENCE, String.valueOf(fromATAG) + RANGE +
   * String.valueOf(toATAG)); if (mesgtype != null)
   * request.put(TL1FieldKeys.MESSAGE_SEQUENCE, mesgtype); TL1RequestMessage
   * message = new TL1RequestMessage( ObjectElement.retrieveAO,
   * toHashMap(request)); TL1ResponseMessage response =
   * neGW.sendSyncMessage(message); List respBlocks = response.getAllMessages();
   * //// All AO of different types if (respBlocks == null) { LogSystem.log(
   * Level.FINEST, "There is no response data for this command " +
   * ObjectElement.retrieveAO); return null; } results = new Vector(); for
   * (ListIterator li = respBlocks.listIterator(); li.hasNext();) { ///// Vector
   * of TL1AlarmEvent TL1AlarmEvent tl1AE = (TL1AlarmEvent) li.next(); if
   * (mesgtype != null) { payloads = tl1AE.getPayloads();
   * results.addAll(payloads); } else { tl1AE.setNEInfo(neInfo); Hashtable msg =
   * new SimpleHashtable(); msg.put(AOAuditor.AO, tl1AE); results.add(msg); } }
   * Printlog.println( "Result of " + message.getCommandCode() + " : " +
   * results); } catch (Exception e) { // Eat exception quietly
   * Printlog.println("Exception on RTRV-AO", e); if (e instanceof TL1Exception)
   * throw e; throw new AssociationException(
   * "Cannot process this command probably due to association/connection down");
   * } return results; }
   */

  public TL1ResponseMessage sendSyncMessage(TL1RequestMessage message)
      throws Exception {
    if (!tl1Engine.isConnected()) {
      
      throw new Exception("connection to NE is not available");
    }

    AbstractTL1SendHelper helper = new TL1SynchSendHelper(neInfo, message,
        composer, parser);

    if (this.tl1Semaphore != null) {
      acquireTL1Permit();
    }

    tl1Engine.send(helper);

    /*
     * We know send blocks, since that is how we have implemented TL1SendHelper.
     * We can now query the SendHelper for the response.
     */

    TL1ResponseMessage temp = helper.getTL1ResponseMessage();
    String payload[] = temp.getTextBlocks();

    // Don't release the permit on IN_PROGRESS:
    if (TL1ResponseMessage.COMPLETION_CODE.IN_PROGRESS.equals(temp
        .getInternalCompletionCode())) {
      return null;
    }

    if (this.tl1Semaphore != null) {
      releaseTL1Permit();
    }

    switch (temp.getInternalCompletionCode()) {
    case SUCCESS:
      return temp;

    case TIME_OUT:
      throw new Exception("NA: Timeout " + Arrays.toString(payload));

    case RETRY_LATER:
      throw new Exception("NA: Retry later " + Arrays.toString(payload));

    case PROCESSING_ERROR:
      throw new Exception("NA: Internal Processing Error "
          + Arrays.toString(payload));

    case REQUEST_ERROR:
      throw new Exception(temp.getErrorCode() + ": Request Error "
          + Arrays.toString(payload));
    }

    return null;
  }

  /**
   * This method acts as the method dispatcher to deliver the corresponding TL1
   * supported method in this object. The analysis is based on the data on the
   * parameter
   **/

  public List<Map<String, String>> sendToNE(Map<String, String> request)
      throws Exception {
    Tl1CommandCode operationKey = Tl1CommandCode.fromString(request
        .get(ObjectElement.OPERATION_KEY));
    if (operationKey == null) {
      throw new Exception(
          "ObjectElement.operationKey value is null - Request terminated");
    }
    return invoke(request);
  }

  /*
   * private void setCommandOperation(SimpleHashtableIF request) { Object o =
   * request.get(ObjectElement.operationKey);
   * request.put(ObjectElement.operationKey, parser.getCommandMap().get(o)); }
   */
  private List<Map<String, String>> invoke(Map<String, String> request)
      throws java.lang.Exception {
    List<Map<String, String>> respBlocks = null;
    // setCommandOperation(request);
    // try {

    log.debug("Sending TL1 request " + request + " to "
        + neInfo.getAddressInfo());
    Tl1CommandCode operationKey = Tl1CommandCode.fromString(request
        .get(ObjectElement.OPERATION_KEY));

    if (operationKey == null) {
      log.debug("ObjectElement.operationKey value is null - Request terminated "
          + request + " to " + neInfo.getAddressInfo());
    }
    TL1RequestMessage message = new TL1RequestMessage(operationKey, request);
    TL1ResponseMessage response = sendSyncMessage(message);
    respBlocks = response.getPayload();
    if (respBlocks == null || respBlocks.isEmpty()) {
      log.debug("There is no response data for this command " + request
          + " from " + neInfo.getAddressInfo());
      return null;
    }

    log.debug("Result of operation:" + operationKey + " first response block:"
        + respBlocks.get(0) + " from " + neInfo.getAddressInfo());
    return respBlocks;
  }
}
