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

package com.nortel.appcore.app.drac.server.neproxy.database;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementConnection;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.AstnNetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.LocalNe;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on Aug 22, 2005
 */
public enum DbUtility {
  INSTANCE;
  private final Logger log = LoggerFactory.getLogger(getClass());

  // public synchronized void addNewAdjacency(AbstractNetworkElement ne,
  // List<String> xmldata)
  // throws Exception
  // {
  // /*
  // *
  // ===================================================================================
  // Example input:
  // * [ <adjacencyInstance aid="OC48-1-5-1" source="00-1b-25-2d-5c-7a"
  // sourceAid ="OC48-1-5-1" target=""
  // * targetAid ="" manualProvision="false" />, <adjacencyInstance
  // aid="OC12-1-11-1"
  // * source="00-1b-25-2d-5c-7a" sourceAid ="OC12-1-11-1"
  // target="00-1b-25-2d-5b-e6" targetAid
  // * ="OC12-1-12-1" manualProvision="false" />, <adjacencyInstance
  // aid="OC12-1-12-1"
  // * source="00-1b-25-2d-5c-7a" sourceAid ="OC12-1-12-1"
  // target="00-1b-25-2d-5c-55" targetAid
  // * ="OC12-1-11-1" manualProvision="false" /> ]
  // *
  // ===================================================================================
  // */
  //
  // List<Map<String, String>> data = new ArrayList<Map<String, String>>();
  //
  // for (String adjacencyInstance : xmldata)
  // {
  // Element element = DbOpsHelper.xmlToElement(adjacencyInstance);
  // Map<String, String> adjMap = DbOpsHelper.elementToMap(element);
  // data.add(adjMap);
  // }
  //
  // DbNetworkElementAdjacency.INSTANCE.add(ne.getNeId(), data);
  // }

  public synchronized void addNewFacility(AbstractNetworkElement ne,
      List<String> data) throws Exception {
    List<Facility> facList = new ArrayList<Facility>(data.size());
    for (String s : data) {
      facList.add(new Facility(ne, DbOpsHelper.xmlToMap(s)));
    }
    DbNetworkElementFacility.INSTANCE.addFacility(ne.getNeId(),
        ne.getIpAddress(), ne.getPortNumber(), facList);
  }

  public void addNewFacility(AbstractNetworkElement ne, String newFacility)
      throws Exception {
    // newFacility is an xml element
    List<Facility> facList = new ArrayList<Facility>();
    facList.add(new Facility(ne, DbOpsHelper.xmlToMap(newFacility)));
    DbNetworkElementFacility.INSTANCE.addFacility(ne.getNeId(),
        ne.getIpAddress(), ne.getPortNumber(), facList);
  }

  public boolean addNewNe(AbstractNetworkElement ne, String managedBy)
      throws Exception {
    NetworkElementHolder neh = new NetworkElementHolder(ne.getIpAddress(),
        Integer.toString(ne.getPortNumber()), ne.getUid(), ne.getNeType(),
        ne.getNeName(), ne.getNeStatus(), ne.getNeStatusStamp(),
        ne.getPasswd(), ne.getNeIndex(), ne.getNeMode(), managedBy,
        ne.getNeId(), ne.getNeCommProtocol(), ne.isAutoReDiscover(),
        ne.getSubType(),
        ne.getNeRelease(), null, null);
    return DbNetworkElement.INSTANCE.add(neh);
  }

  public synchronized void addNewXConnect(AbstractNetworkElement ne,
      List<String> list) throws Exception {
    /*
     * Sample input...list of the following: [ <edge
     * id="DRAC-cd28862f-1222266429798" type="2WAY" rate="STS3C"
     * source="00-1B-25-2D-5C-7A" sShelf="1" sSlot="11" sSubslot="0" sPort="1"
     * sChannel="1" target="00-1B-25-2D-5C-7A" sourceAid="OC12-1-11-1-1"
     * tShelf="1" tSlot="1" tSubslot="0" tPort="1" tChannel="1"
     * targetAid="OC12-1-1-1-1" swmate="" committed="true" /> ]
     */
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();

    for (String s : list) {
      Element element = DbOpsHelper.xmlToElement(s);
      Map<String, String> connMap = DbOpsHelper.elementToMap(element);
      data.add(connMap);
    }

    DbNetworkElementConnection.INSTANCE.add(ne.getNeId(), data);
  }

  public void addOneXConnect(AbstractNetworkElement ne, Map<String, Object> data)
      throws Exception {
    /*
     * Sample input:
     * {$eventReceived=com.nortel.optical.mediation.tl1.TL1AlarmEvent@19269cb,
     * replaceRate=STS3C, $NETWORKELEMENT=<node id="00-1B-25-2D-5B-E6"
     * tid="OME0237" ip="47.134.3.229" port="10001" status="6" />,
     * replaceId=DRAC-cd28862f-1222265772168, replaceSourceAid=OC12-1-12-1-1,
     * class=com.nortel.optical.drac.server.neproxy.command.ome.ReptDbchg,
     * replaceTargetAddress=00-1B-25-2D-5B-E6, replaceSwmate=,
     * replaceSourceAddress=00-1B-25-2D-5B-E6, replaceCommitted=true,
     * replaceTargetAid=OC12-1-1-1-1, replaceType=2WAY}
     */

    String[] fromAidMap = ((String) data.get("replaceSourceAid")).split("-");
    String[] toAidMap = ((String) data.get("replaceTargetAid")).split("-");
    String callKey = (String) data.get("replaceCallKey");

    String sourceAid = (String) data.get("replaceSourceAid");
    String targetAid = (String) data.get("replaceTargetAid");

    Map<String, String> connMap = new HashMap<String, String>();

    connMap.put(DbKeys.NetworkElementConnectionCols.ID,
        (String) data.get("replaceId"));
    connMap.put(DbKeys.NetworkElementConnectionCols.TYPE,
        (String) data.get("replaceType"));
    connMap.put(DbKeys.NetworkElementConnectionCols.RATE,
        (String) data.get("replaceRate"));
    connMap.put(DbKeys.NetworkElementConnectionCols.SOURCE,
        (String) data.get("replaceSourceAddress"));

    connMap.put(DbKeys.NetworkElementConnectionCols.SSHELF, fromAidMap[1]);
    connMap.put(DbKeys.NetworkElementConnectionCols.SSLOT, fromAidMap[2]);
    connMap.put(DbKeys.NetworkElementConnectionCols.SSUBSLOT, "0");
    connMap.put(DbKeys.NetworkElementConnectionCols.SPORT, fromAidMap[3]);
    connMap.put(DbKeys.NetworkElementConnectionCols.SCHANNEL, fromAidMap[4]);

    connMap.put(DbKeys.NetworkElementConnectionCols.TSHELF, toAidMap[1]);
    connMap.put(DbKeys.NetworkElementConnectionCols.TSLOT, toAidMap[2]);
    connMap.put(DbKeys.NetworkElementConnectionCols.TSUBSLOT, "0");
    connMap.put(DbKeys.NetworkElementConnectionCols.TPORT, toAidMap[3]);
    connMap.put(DbKeys.NetworkElementConnectionCols.TCHANNEL, toAidMap[4]);
    connMap.put(DbKeys.NetworkElementConnectionCols.TARGET,
        (String) data.get("replaceTargetAddress"));

    connMap.put(DbKeys.NetworkElementConnectionCols.SOURCEAID, sourceAid);
    connMap.put(DbKeys.NetworkElementConnectionCols.TARGETAID, targetAid);

    String swmateAid = (String) data.get("replaceSwmate");
    if (swmateAid != null && swmateAid.length() > 0) {
      String[] swMateAidMap = swmateAid.split("-");

      connMap.put(DbKeys.NetworkElementConnectionCols.SWMATEAID, swmateAid);
      connMap.put(DbKeys.NetworkElementConnectionCols.SWMATE_SHELF,
          swMateAidMap[1]);
      connMap.put(DbKeys.NetworkElementConnectionCols.SWMATE_SLOT,
          swMateAidMap[2]);
      connMap.put(DbKeys.NetworkElementConnectionCols.SWMATE_SUBSLOT, "0");
      connMap.put(DbKeys.NetworkElementConnectionCols.SWMATE_PORT,
          swMateAidMap[3]);
      connMap.put(DbKeys.NetworkElementConnectionCols.SWMATE_CHANNEL,
          swMateAidMap[4]);
    }

    connMap.put(DbKeys.NetworkElementConnectionCols.COMMITTED,
        (String) data.get("replaceCommitted"));
    if (callKey != null) {
      connMap.put(DbKeys.NetworkElementConnectionCols.CALLKEY, callKey);
    }

    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
    list.add(connMap);

    DbNetworkElementConnection.INSTANCE.add(ne.getNeId(), list);
  }

  public void addOneXConnect(AbstractNetworkElement ne, String aConnection)
      throws Exception {

    List<Map<String, String>> data = new ArrayList<Map<String, String>>();

    Element element = DbOpsHelper.xmlToElement(aConnection);
    Map<String, String> connMap = DbOpsHelper.elementToMap(element);
    data.add(connMap);

    DbNetworkElementConnection.INSTANCE.add(ne.getNeId(), data);
  }

  // public void deleteAllAdjacency(AbstractNetworkElement ne)
  // throws Exception
  // {
  // DbNetworkElementAdjacency.INSTANCE.deleteAll(ne);
  // }

  public synchronized void deleteAllXConnection(AbstractNetworkElement ne)
      throws Exception {
    DbNetworkElementConnection.INSTANCE.deleteAll(ne);
  }

  public void deleteNe(AbstractNetworkElement ne) throws Exception {
    DbNetworkElement.INSTANCE.delete(ne);
  }

  public void deleteXConnect(AbstractNetworkElement ne, String srcAid,
      String dstAid) throws Exception {
    DbNetworkElementConnection.INSTANCE.delete(ne, srcAid, dstAid);
  }

  public void editXConnect(AbstractNetworkElement ne, String srcAid, String dstAid, Map<String, Object> data) throws Exception {
      if (log.isDebugEnabled()) {
          StringBuilder sb = new StringBuilder("editXConnect: editing crossconnect ");
          sb.append(ne.getNeName());
          sb.append(" ");
          sb.append(ne.getNeId());
          sb.append(" ");
          sb.append(srcAid);
          sb.append(" ");
          sb.append(dstAid);
          sb.append(" ");
          sb.append(data.toString());
          log.debug(sb.toString());
      }

    // JHNM - Here
    // DbNetworkElementConnection.INSTANCE.edit(ne, srcAid, dstAid, data);
  }

  public void generateFacilityUpdatedLog(AbstractNetworkElement ne, String aid,
      Map<String, String> details) throws Exception {
    String ip = ne.getIpAddress();
    String port = Integer.toString(ne.getPortNumber());
    String tid = ne.getNeName();
    generateLog(new LogRecord(null, ip, null, aid,
        LogKeyEnum.KEY_NE_FACILITY_UPDATED, new String[] { tid, ip, port },
        details));
  }

  public void generateLog(LogRecord log) throws Exception {
    DbLog.INSTANCE.addLog(log);
  }

  @Deprecated
  // FIXME: This function does not map all available attributes!
  public AbstractNe holderToAbstractNe(NetworkElementHolder newNe)
      throws Exception {
    String ipAddress = newNe.getIp();
    int portNumber = Integer.parseInt(newNe.getPort());
    String uid = newNe.getUserId();
    /*
     * If we have read the record from the database the password will be
     * encrypted, if we are adding a new NE from the UI the password is in the
     * clear. This used to be done under 2 different code paths which were
     * combined.
     */

    CryptedString passwd = newNe.getPassword();

    PROTOCOL_TYPE neProtocol = newNe.getCommProtocol();

    AbstractNe ne;

    if (neProtocol == null || neProtocol == PROTOCOL_TYPE.NETL1_PROTOCOL) {
      ne = new NetworkElement(uid, passwd, ipAddress, portNumber, NeProxy
          .INSTANCE.getTid());
      ne.setNeCommProtocol(neProtocol);
      return ne;
    }
    else if (neProtocol == PROTOCOL_TYPE.NESNMP_PROTOCOL) {
      throw new Exception("Cannot add NE, protocol type " + neProtocol
          + " is not currently supported");
    }
    else if (neProtocol == PROTOCOL_TYPE.NEGMPLS_PROTOCOL) {
      throw new Exception("Cannot add NE, protocol type " + neProtocol
          + " is not currently supported");
    }
    else if (neProtocol == PROTOCOL_TYPE.NELOCAL_PROTOCOL) {
      ne = new LocalNe(uid, passwd, ipAddress, portNumber, NeProxy
          .INSTANCE.getTid());
      ne.setNeCommProtocol(PROTOCOL_TYPE.NELOCAL_PROTOCOL);
      return ne;
    }
    else if (neProtocol == PROTOCOL_TYPE.NEASTN_PROTOCOL) {
      ne = new AstnNetworkElement(uid, passwd, ipAddress, portNumber, NeProxy
          .INSTANCE.getTid());
      ne.setNeCommProtocol(PROTOCOL_TYPE.NEASTN_PROTOCOL);
      return ne;
    }
    else if (neProtocol == PROTOCOL_TYPE.FORCE10_PROTOCOL) {
      ne = new Force10NetworkElement(uid, passwd, ipAddress, portNumber,
          NeProxy.INSTANCE.getTid());
      ne.setNeCommProtocol(PROTOCOL_TYPE.FORCE10_PROTOCOL);
      return ne;
    }
    else {
      throw new Exception("Cannot add NE, protocol type " + neProtocol
          + " is unknown/unsupported");
    }

  }

  public synchronized void invalidateFacility(AbstractNetworkElement ne)
      throws Exception {
    Map<String, String> data = new HashMap<String, String>();
    data.put(DbKeys.NetworkElementFacilityCols.VALID, "false");

    boolean addIfAbsent = false; // update only

    DbNetworkElementFacility.INSTANCE.update(ne, data, addIfAbsent);
  }

  public String retrieveAFacility(AbstractNetworkElement ne, String aid)
      throws Exception {
    /*
     * Expected return format: <layer1 actualUnit="0" aid="WAN-1-1-1"
     * apsId="N/A" constrain="0" cost="1" domain="N/A" group="none" id="1"
     * lcas="DISABLE" manualProvision="false" mapping="GFP-F" metric="1"
     * mode="SONET" port="1" primaryState="OOS-AUMA" provUnit="1" ps="N/A"
     * rate="STS3C" shelf="1" signalingType="unassigned" siteId="N/A" slot="1"
     * srlg="N/A" tna="N/A" type="WAN" valid="true" vcat="ENABLE"> <layer2
     * advertisedDuplex="UNKNOWN" aid="ETH-1-1-1" apsId="N/A"
     * autoNegotiation="ENABLE" autoNegotiationStatus="UNKNOWN" constrain="0"
     * controlPauseRx="DISABLE" controlPauseTx="UNKNOWN" cost="1" domain="N/A"
     * etherDuplex="FULL" flowControl="ASYM" group="none" id="2"
     * manualProvision="false" metric="1" mtu="9600" passControlFrame="DISABLE"
     * physicalAddress="UNKNOWN" port="1" primaryState="OOS-AUMA" ps="N/A"
     * shelf="1" signalingType="unassigned" siteId="N/A" slot="1" speed="1000"
     * srlg="N/A" tna="N/A" txConditioning="ENABLE" type="ETH" valid="true"/>
     * </layer1>
     */
    String result = null;

    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, ne.getNeId());
    filter.put(DbKeys.NetworkElementFacilityCols.AID, aid);

    List<Facility> list = DbNetworkElementFacility.INSTANCE
        .retrieveFacilities(filter);

    if (list.size() == 1) {
      result = DbOpsHelper.elementToString(list.get(0).asElement());
    }

    return result;
  }

  public List<String> retrieveAFacility(AbstractNetworkElement ne,
      String shelf, String slot) throws Exception {
    /*
     * Expected return format: [ <layer1 actualUnit="0" aid="WAN-1-1-1"
     * apsId="N/A" constrain="0" cost="1" domain="N/A" group="none" id="1"
     * lcas="DISABLE" manualProvision="false" mapping="GFP-F" metric="1"
     * mode="SONET" port="1" primaryState="OOS-AUMA" provUnit="1" ps="N/A"
     * rate="STS3C" shelf="1" signalingType="unassigned" siteId="N/A" slot="1"
     * srlg="N/A" tna="N/A" type="WAN" valid="true" vcat="ENABLE"> <layer2
     * advertisedDuplex="UNKNOWN" aid="ETH-1-1-1" apsId="N/A"
     * autoNegotiation="ENABLE" autoNegotiationStatus="UNKNOWN" constrain="0"
     * controlPauseRx="DISABLE" controlPauseTx="UNKNOWN" cost="1" domain="N/A"
     * etherDuplex="FULL" flowControl="ASYM" group="none" id="2"
     * manualProvision="false" metric="1" mtu="9600" passControlFrame="DISABLE"
     * physicalAddress="UNKNOWN" port="1" primaryState="OOS-AUMA" ps="N/A"
     * shelf="1" signalingType="unassigned" siteId="N/A" slot="1" speed="1000"
     * srlg="N/A" tna="N/A" txConditioning="ENABLE" type="ETH" valid="true"/>
     * </layer1> <layer1 actualUnit="0" aid="WAN-1-1-1" apsId="N/A"
     * constrain="0" cost="1" domain="N/A" group="none" id="1" lcas="DISABLE"
     * manualProvision="false" mapping="GFP-F" metric="1" mode="SONET" port="1"
     * primaryState="OOS-AUMA" provUnit="1" ps="N/A" rate="STS3C" shelf="1"
     * signalingType="unassigned" siteId="N/A" slot="1" srlg="N/A" tna="N/A"
     * type="WAN" valid="true" vcat="ENABLE"> <layer2 advertisedDuplex="UNKNOWN"
     * aid="ETH-1-1-1" apsId="N/A" autoNegotiation="ENABLE"
     * autoNegotiationStatus="UNKNOWN" constrain="0" controlPauseRx="DISABLE"
     * controlPauseTx="UNKNOWN" cost="1" domain="N/A" etherDuplex="FULL"
     * flowControl="ASYM" group="none" id="2" manualProvision="false" metric="1"
     * mtu="9600" passControlFrame="DISABLE" physicalAddress="UNKNOWN" port="1"
     * primaryState="OOS-AUMA" ps="N/A" shelf="1" signalingType="unassigned"
     * siteId="N/A" slot="1" speed="1000" srlg="N/A" tna="N/A"
     * txConditioning="ENABLE" type="ETH" valid="true"/> </layer1> ]
     */

    // returns list of elements in string format
    List<String> result = new ArrayList<String>();

    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, ne.getNeId());
    filter.put(DbKeys.NetworkElementFacilityCols.SHELF, shelf);
    filter.put(DbKeys.NetworkElementFacilityCols.SLOT, slot);

    List<Facility> list = DbNetworkElementFacility.INSTANCE
        .retrieveFacilities(filter);

    for (Facility fac : list) {
      result.add(DbOpsHelper.elementToString(fac.asElement()));
    }

    return result;
  }

  public String retrieveAFacility(AbstractNetworkElement ne, String shelf,
      String slot, String port) throws Exception {
    /*
     * Expected return format: <layer1 actualUnit="0" aid="WAN-1-1-1"
     * apsId="N/A" constrain="0" cost="1" domain="N/A" group="none" id="1"
     * lcas="DISABLE" manualProvision="false" mapping="GFP-F" metric="1"
     * mode="SONET" port="1" primaryState="OOS-AUMA" provUnit="1" ps="N/A"
     * rate="STS3C" shelf="1" signalingType="unassigned" siteId="N/A" slot="1"
     * srlg="N/A" tna="N/A" type="WAN" valid="true" vcat="ENABLE"> <layer2
     * advertisedDuplex="UNKNOWN" aid="ETH-1-1-1" apsId="N/A"
     * autoNegotiation="ENABLE" autoNegotiationStatus="UNKNOWN" constrain="0"
     * controlPauseRx="DISABLE" controlPauseTx="UNKNOWN" cost="1" domain="N/A"
     * etherDuplex="FULL" flowControl="ASYM" group="none" id="2"
     * manualProvision="false" metric="1" mtu="9600" passControlFrame="DISABLE"
     * physicalAddress="UNKNOWN" port="1" primaryState="OOS-AUMA" ps="N/A"
     * shelf="1" signalingType="unassigned" siteId="N/A" slot="1" speed="1000"
     * srlg="N/A" tna="N/A" txConditioning="ENABLE" type="ETH" valid="true"/>
     * </layer1>
     */

    /* Return Element in string format. */

    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, ne.getNeId());
    filter.put(DbKeys.NetworkElementFacilityCols.SHELF, shelf);
    filter.put(DbKeys.NetworkElementFacilityCols.SLOT, slot);
    filter.put(DbKeys.NetworkElementFacilityCols.PORT, port);

    List<Facility> list = DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);

    if (list.size() < 1) {
        log.debug("retrieveAFacility no facility found for ne:" + ne.getNeId()
                + " shelf:" + shelf + " slot:" + slot + " port:" + port);
    }
    else if (list.size() > 1) {
        log.error("retrieveAFacility: multiple (unexpected) facilities found for ne:"
                + ne.getNeId() + " shelf:" + shelf + " slot:" + slot
                + " port:" + port);
    }
    else {
        /* We have a single facility which is what we expected. */
        return DbOpsHelper.elementToString(list.get(0).asElement());
    }

    return null;

  }

  /**
   * Return a list of facilities matching the provided network element, shelf,
   * slot, port.  The only time multiple facilities should be returned is in the
   * cast of ETH and WAN which share the same facility location, but different
   * facility types.
   *
   * @param ne network element on which to search for the facility.
   * @param shelf shelf number of the facility.
   * @param slot slot number of the facility.
   * @param port port number of the facility.
   * @return list of matching facilities.
   * @throws Exception
   *
   * Expected return format of string: <layer1 actualUnit="0" aid="WAN-1-1-1"
   * apsId="N/A" constrain="0" cost="1" domain="N/A" group="none" id="1"
   * lcas="DISABLE" manualProvision="false" mapping="GFP-F" metric="1"
   * mode="SONET" port="1" primaryState="OOS-AUMA" provUnit="1" ps="N/A"
   * rate="STS3C" shelf="1" signalingType="unassigned" siteId="N/A" slot="1"
   * srlg="N/A" tna="N/A" type="WAN" valid="true" vcat="ENABLE"> <layer2
   * advertisedDuplex="UNKNOWN" aid="ETH-1-1-1" apsId="N/A"
   * autoNegotiation="ENABLE" autoNegotiationStatus="UNKNOWN" constrain="0"
   * controlPauseRx="DISABLE" controlPauseTx="UNKNOWN" cost="1" domain="N/A"
   * etherDuplex="FULL" flowControl="ASYM" group="none" id="2"
   * manualProvision="false" metric="1" mtu="9600" passControlFrame="DISABLE"
   * physicalAddress="UNKNOWN" port="1" primaryState="OOS-AUMA" ps="N/A"
   * shelf="1" signalingType="unassigned" siteId="N/A" slot="1" speed="1000"
   * srlg="N/A" tna="N/A" txConditioning="ENABLE" type="ETH" valid="true"/>
   * </layer1>
   *
   */
    public List<String> retrieveFacilities(AbstractNetworkElement ne,
            String shelf, String slot, String port) throws Exception {

    // Return a list of matching facilities.
    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, ne.getNeId());
    filter.put(DbKeys.NetworkElementFacilityCols.SHELF, shelf);
    filter.put(DbKeys.NetworkElementFacilityCols.SLOT, slot);
    filter.put(DbKeys.NetworkElementFacilityCols.PORT, port);

    List<Facility> list = DbNetworkElementFacility.INSTANCE
        .retrieveFacilities(filter);

    List<String> facilities = new ArrayList<String>();

    for (Facility facility : list) {
        facilities.add(DbOpsHelper.elementToString(facility.asElement()));
    }

    return facilities;
  }

  public List<CrossConnection> retrieveAllNeXconnect(AbstractNetworkElement ne)
      throws Exception {
    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementConnectionCols.NEID_FOR_CONN, ne.getNeId());
    return DbNetworkElementConnection.INSTANCE.retrieve(filter);
  }

  public CrossConnection retrieveAnXConnect(AbstractNetworkElement ne,
      String srcAid, String dstAid) throws Exception {
    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementConnectionCols.NEID_FOR_CONN, ne.getNeId());
    filter.put(DbKeys.NetworkElementConnectionCols.SOURCEAID, srcAid);
    filter.put(DbKeys.NetworkElementConnectionCols.TARGETAID, dstAid);

    List<CrossConnection> list = DbNetworkElementConnection.INSTANCE
        .retrieve(filter);
    if (list.size() == 1) {
      return list.get(0);
    }

    return null;
  }

  /**
   * @deprecated Use retrieveAnXConnect and not the xml format
   */
  @Deprecated
  public String retrieveAXConnect(AbstractNetworkElement ne, String srcAid,
      String dstAid) throws Exception {
    /*
     * Sample output: <edge committed="true" id="DRAC-loopback" rate="STS3C"
     * sChannel="1" sPort="1" sShelf="1" sSlot="1" sSubslot="0"
     * source="00-1B-25-2D-5B-E6" sourceAid="OC12-1-1-1-1" swmate=""
     * tChannel="1" tPort="1" tShelf="1" tSlot="1" tSubslot="0"
     * target="00-1B-25-2D-5B-E6" targetAid="OC12-1-1-1-1" type="1WAY"/>
     */

    CrossConnection c = retrieveAnXConnect(ne, srcAid, dstAid);
    if (c != null) {
      return DbOpsHelper.elementToString(c.asEdge());
    }
    return null;
  }

  public List<Facility> retrieveNeFacilities(AbstractNetworkElement ne)
      throws Exception {
    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, ne.getNeId());
    return DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
  }

  public String retrieveNeFacility(AbstractNetworkElement ne) throws Exception {
    /*
     * Expected output format: <node id="00-1B-25-2D-5B-E6" ip="47.134.3.229"
     * port="10001"> <layer1 aid="OC12-1-11-1" apsId="N/A" constrain="0"
     * cost="1" domain="N/A" group="none" id="1" manualProvision="false"
     * metric="1" port="1" primaryState="IS" ps="N/A" shelf="1"
     * signalingType="unassigned" siteId="N/A" slot="11" srlg="N/A" tna="N/A"
     * type="OC12" valid="false"/> <layer1 actualUnit="0" aid="WAN-1-1-1"
     * apsId="N/A" constrain="0" cost="1" domain="N/A" group="none" id="1"
     * lcas="DISABLE" manualProvision="false" mapping="GFP-F" metric="1"
     * mode="SONET" port="1" primaryState="OOS-AUMA" provUnit="1" ps="N/A"
     * rate="STS3C" shelf="1" signalingType="unassigned" siteId="N/A" slot="1"
     * srlg="N/A" tna="N/A" type="WAN" valid="false" vcat="ENABLE"> <layer2
     * advertisedDuplex="UNKNOWN" aid="ETH-1-1-1" apsId="N/A"
     * autoNegotiation="ENABLE" autoNegotiationStatus="UNKNOWN" constrain="0"
     * controlPauseRx="DISABLE" controlPauseTx="UNKNOWN" cost="1" domain="N/A"
     * etherDuplex="FULL" flowControl="ASYM" group="none" id="2"
     * manualProvision="false" metric="1" mtu="9600" passControlFrame="DISABLE"
     * physicalAddress="UNKNOWN" port="1" primaryState="OOS-AUMA" ps="N/A"
     * shelf="1" signalingType="unassigned" siteId="N/A" slot="1" speed="1000"
     * srlg="N/A" tna="N/A" txConditioning="ENABLE" type="ETH" valid="false"/>
     * </layer1> </node>
     */

    // This method needs to return what was the full jdom Element for an NE.
    // The facility retrieval i/f will assemble/nest the layers in the response
    // (via join),
    // so I need only call to select layer1 (which will pull in layer2) and
    // layer0 (as did the original
    // code).
    Element node = new Element("node");
    node.setAttribute("id", ne.getNeId());
    node.setAttribute("ip", ne.getIpAddress());
    node.setAttribute("port", Integer.toString(ne.getPortNumber()));

    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, ne.getNeId());

    // GGL2 - breaking implicit layer1/layer2 relationship

    List<Map<String, String>> list = DbNetworkElementFacility.INSTANCE
        .retrieve(filter);

    // list.addAll(DbNetworkElementFacility.INSTANCE.retrieve(filter));

    for (Map<String, String> facMap : list) {
      node.addContent(DbNetworkElementFacility.facMapToElement(facMap));
    }

    return DbOpsHelper.elementToString(node);
  }

  /**
   * Fetch the NE list as AbstractNetworkElements objects from the database,
   * used to initially populate neProxy on start up
   */
  public List<AbstractNetworkElement> retrieveNetworkElement(String proxyId)
      throws Exception {
    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementCols.MANAGEDBY, proxyId);
    List<NetworkElementHolder> hlist = DbNetworkElement.INSTANCE.retrieve(
        filter);

    List<AbstractNetworkElement> results = new ArrayList<AbstractNetworkElement>();
    for (NetworkElementHolder ne : hlist) {
      results.add(holderToAbstractNe(ne));
    }
    return results;
  }

  /**
   * @deprecated use retrieveAllNeXconnect and avoid the xml version
   */
  @Deprecated
  public String retrieveNeXconnect(AbstractNetworkElement ne) throws Exception {
    /*
     * Expected return format: <NetworkElementConnection> <node
     * id="00-1B-25-2D-5C-7A" ip="47.134.3.230" port="10001"> <edge
     * committed="true" id="DRAC-cd28862f-1222266429798" rate="STS3C"
     * sChannel="1" sPort="1" sShelf="1" sSlot="11" sSubslot="0"
     * source="00-1B-25-2D-5C-7A" sourceAid="OC12-1-11-1-1" swmate=""
     * tChannel="1" tPort="1" tShelf="1" tSlot="1" tSubslot="0"
     * target="00-1B-25-2D-5C-7A" targetAid="OC12-1-1-1-1" type="2WAY"/> </node>
     * <backslashNetworkElementConnection>
     */

    Element root = new Element("NetworkElementConnection");

    Element node = new Element("node");
    node.setAttribute("id", ne.getNeId());
    node.setAttribute("ip", ne.getIpAddress());
    node.setAttribute("port", Integer.toString(ne.getPortNumber()));

    for (CrossConnection xc : retrieveAllNeXconnect(ne)) {
      node.addContent(xc.asEdge());
    }

    root.addContent(node);

    return DbOpsHelper.elementToString(root);
  }

  // public void updateAdjacency(AbstractNetworkElement ne, Map<String, String>
  // data, String aid)
  // throws Exception
  // {
  // DbNetworkElementAdjacency.INSTANCE.update(ne, aid, data);
  // }

  public synchronized void updateAFacility(AbstractNetworkElement ne,
      Map<String, String> attributeList, String aid) throws Exception {
    this.updateAFacility(ne, attributeList, aid, false);
  }

  public synchronized void updateAFacility(AbstractNetworkElement ne,
      Map<String, String> attributeList, String aid, boolean addIfAbsent)
      throws Exception {
    DbNetworkElementFacility.INSTANCE.update(ne, aid, attributeList,
        addIfAbsent);
  }

  public synchronized void updateFacilities(AbstractNetworkElement ne,
      Map<String, String> attributeList) throws Exception {
    /*
     * Example input: this method is called from OME protection to reset these
     * two protection attributes against ALL facilities: HashMap<String, String>
     * attributeList = new HashMap<String, String>();
     * attributeList.put(AbstractFacilityXML.APSID_ATTR, "N/A");
     * attributeList.put(AbstractFacilityXML.PROTECTIONSCHEME_ATTR, "N/A");
     */

    boolean addIfAbsent = false; // update only

    DbNetworkElementFacility.INSTANCE.update(ne, attributeList,
        addIfAbsent);
  }

  public synchronized void updateFacility(AbstractNetworkElement ne,
      List<Object[]> data) throws Exception {
    boolean addIfAbsent = false; // update only

    for (Object[] dataElement : data) {
      String aid = (String) dataElement[0];
      Map<String, String> attributeList = (Map<String, String>) dataElement[1];

      DbNetworkElementFacility.INSTANCE.update(ne, aid, attributeList,
          addIfAbsent);
    }
  }

  /**
   * @param ne
   * @param data
   *          contains an array of Object[2] which has the following format
   *          Object[0] = is a String. It's the AID of the facility that
   *          contains the attribute Object[1] = is a HashMap that has only one
   *          element. The "key" is the name of the attribute and the "value" is
   *          the value of the attribute.
   */
  public synchronized void updateFacilityAttr(AbstractNetworkElement ne,
      List<Object[]> data) throws Exception {
    boolean addIfAbsent = true; // adding new attributes!!!

    for (Object[] dataElement : data) {
      String aid = (String) dataElement[0];
      Map<String, String> attributeList = (Map<String, String>) dataElement[1];

      DbNetworkElementFacility.INSTANCE.update(ne, aid, attributeList,
          addIfAbsent);
    }
  }

  public synchronized void upDateNe(AbstractNetworkElement ne) {
    try {
      Map<String, String> map = new HashMap<String, String>();
      map.put(DbKeys.NetworkElementCols.TYPE, ne.getNeType().toString());
      map.put(DbKeys.NetworkElementCols.NEID, ne.getNeId());
      map.put(DbKeys.NetworkElementCols.STATUS, ne.getNeStatusString());
      map.put(DbKeys.NetworkElementCols.MODE, ne.getNeMode().toString());
      map.put(DbKeys.NetworkElementCols.TID, ne.getNeName());

      if (ne.getPositionX() != null) {
        map.put(DbKeys.NetworkElementCols.POSITION_X,
            String.valueOf(ne.getPositionX()));
      }
      if (ne.getPositionY() != null) {
        map.put(DbKeys.NetworkElementCols.POSITION_Y,
            String.valueOf(ne.getPositionY()));
      }

      DbNetworkElement.INSTANCE.update(ne, map);
    }
    catch (Exception e) {
      log.error("Fail to update NE attributes", e);
    }
  }

  public synchronized void upDateNePassword(AbstractNetworkElement ne) {
    try {
      Map<String, String> map = new HashMap<String, String>();
      map.put(DbKeys.NetworkElementCols.PASSWORD, ne.getPasswd().toString());
      DbNetworkElement.INSTANCE.update(ne, map);
    }
    catch (Exception e) {
      log.error("Failed to update NE password", e);
    }
  }

  public synchronized void upDateNeRelease(AbstractNetworkElement ne) {
    try {
      Map<String, String> map = new HashMap<String, String>();
      map.put(DbKeys.NetworkElementCols.NE_RELEASE, ne.getNeRelease());
      DbNetworkElement.INSTANCE.update(ne, map);
    }
    catch (Exception e) {
      log.error("Fail to update NE release", e);
    }

  }

  public synchronized void upDateSubType(AbstractNetworkElement ne) {
    try {
      Map<String, String> map = new HashMap<String, String>();
      map.put(DbKeys.NetworkElementCols.SUBTYPE, ne.getSubType());
      DbNetworkElement.INSTANCE.update(ne, map);
    }
    catch (Exception e) {
      log.error("Fail to update NE subType", e);
    }

  }

  public synchronized void upDateNeStatus(AbstractNetworkElement ne) {
    try {
      Map<String, String> map = new HashMap<String, String>();
      map.put(DbKeys.NetworkElementCols.STATUS, ne.getNeStatusString());
      DbNetworkElement.INSTANCE.update(ne, map);
    }
    catch (Exception e) {
      log.error("Fail to update NE status", e);
    }
  }

  public synchronized void upDateNeUid(AbstractNetworkElement ne) {
    try {
      Map<String, String> map = new HashMap<String, String>();
      map.put(DbKeys.NetworkElementCols.USERID, ne.getUid());
      DbNetworkElement.INSTANCE.update(ne, map);
    }
    catch (Exception e) {
      log.error("Fail to update NE UID", e);
    }
  }

  public void updateOrAddFacilityAttr(AbstractNetworkElement ne, String aid,
      String name, String value) throws Exception {
    /*
     * queryExisting.append("let $a := /NetworkElementFacility/node[@ip = \"");
     * queryExisting.append(neIp + "\" and @port = \"" + nePort + "\"]/");
     * queryExisting.append(layer + "[@aid = \"" + aid + "\"]");
     * queryExisting.append(" return if (string-length(name($a/@" + name +
     * ")) > 0)"); queryExisting.append(" then (update value $a/@" + name +
     * " with \"" + value + "\")");
     * queryExisting.append(" else (update insert (attribute " + name + " {\"" +
     * value + "\"}) into $a)");
     */

    boolean addIfAbsent = true; // adding new attributes!!!

    Map<String, String> attributeList = new HashMap<String, String>();
    attributeList.put(name, value);

    DbNetworkElementFacility.INSTANCE.update(ne, aid, attributeList,
        addIfAbsent);
  }

  public void updateAddressAndPort(final String oldAddress, final int oldPort, final String newAddress, final int newPort) throws Exception {
    DbNetworkElementFacility.INSTANCE.updateAddressAndPort(oldAddress, oldPort, newAddress, newPort);

  }

}
