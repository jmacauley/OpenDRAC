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

import java.io.Serializable;
import java.rmi.Remote;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.opendrac.ioc.IocContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.errorhandling.OperationNotSupportedException;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.ProvisioningResultHolder;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.database.DbOperationsManager;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.NeProxyEventServer;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AddConnection;
import com.nortel.appcore.app.drac.server.neproxy.command.DeleteConnection;
import com.nortel.appcore.app.drac.server.neproxy.command.PostDeleteConnections;
import com.nortel.appcore.app.drac.server.neproxy.command.PrepAddConnections;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.Candidate;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;

public final class NeProxyInterfaceImpl implements NeProxyServerInterface,
    Remote, Serializable {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final NeProxyRemote neProxyRemote;
	private final int port = RmiServerInfo.NEPROXY_PORT;

	public NeProxyInterfaceImpl() throws Exception {
		log.debug("Binding NeProxyRemote to: " + port);
		neProxyRemote = new NeProxyRemote(port, this);
	}
	

	@Override
	public void addManualAdjacency(String sourceIEEE, String sourcePort,
	    String destIEEE, String destPort) throws Exception {
		log.debug("Processing addManualAdjacency invoked with " + sourceIEEE + " "
		    + sourcePort + " " + destIEEE + " " + destPort);
		addAdjacency(sourceIEEE, sourcePort, destIEEE, destPort);
		addAdjacency(destIEEE, destPort, sourceIEEE, sourcePort);
	}

	@Override
	public void changeNetworkElementPassword(NetworkElementHolder updatedNE)
	    throws Exception {
		String key = updatedNE.getIp() + ":" + updatedNE.getPort();
		

		AbstractNetworkElement ne = DiscoverNePool.INSTANCE.getNe(key);
		if (ne == null) {
			throw new Exception("ChangePassword: NE '" + key + "' not found!");
		}

		ne.changeNePassword(updatedNE.getUserId(), updatedNE.getPassword());
	}

	@Override
	public ProvisioningResultHolder createConnection(CrossConnection xcon)
	    throws Exception {

		// Pass my concrete object into the AbstractCommandlet world via the data
		// parameters for now.
		Map<String, Object> dm = new HashMap<String, Object>();
		dm.put(ClientMessageXml.CROSSCONNECTION_KEY, xcon);
		AddConnection ac = new AddConnection(dm);
		boolean result = ac.start();

		Candidate c = ac.getCandidate();
		ProvisioningResultHolder holder = new ProvisioningResultHolder();
		if (c.getXmlResult() != null) {
			holder.setOutputData(c.getXmlResult());
		}

		if (result) {
			return holder;
			// Success
		}

		Map<String, String> errorCodes = new HashMap<String, String>();
		errorCodes.put("PLNA", "Login Not Active");
		errorCodes.put("ERR_IUID", "data, Unknown NE ID");
		errorCodes.put("ERR_INRY", "Status, NE not ready");
		errorCodes.put("ERR_IEAE", "data, Entity Already Exists");
		errorCodes.put("ERR_IENE", "data, Entity does Not Exists");
		errorCodes.put("ERR_SIOE", "Status, Internal operation failure");

		String errorText = errorCodes.get(c.getErrorCode());
		if (errorText == null) {
			errorText = "Unknown error " + c.getErrorCode();
		}

		if (c.getAdditionalErrorText() != null) {
			errorText = errorText + c.getAdditionalErrorText();
		}

		holder.setErrorData(c.getErrorCode(), errorText, null, new Exception(
		    "StackTrace"));
		log.debug("createConnection unsuccessful with exception: " + errorText
		    + " results=" + holder.toString());
		return holder;
	}

	/**
	 * Stop accepting commands and remove all event listeners, called when going
	 * inactive or shutting down
	 */
	public void deactivate() throws Exception {
		NeProxyEventServer.INSTANCE.removeAllListeners();
	}

	@Override
	public ProvisioningResultHolder deleteConnection(CrossConnection xcon)
	    throws Exception {

		
		// Pass my concrete object into the AbstractCommandlet world via the data
		// parameters for now.
		Map<String, Object> dm = new HashMap<String, Object>();
		dm.put(ClientMessageXml.CROSSCONNECTION_KEY, xcon);
		DeleteConnection dc = new DeleteConnection(dm);

		boolean result = dc.start();

		Candidate c = dc.getCandidate();
		ProvisioningResultHolder holder = new ProvisioningResultHolder();
		if (c.getXmlResult() != null) {
			holder.setOutputData(c.getXmlResult());
		}

		if (result) {
			return holder;
			// Success
		}

		Map<String, String> errorCodes = new HashMap<String, String>();
		errorCodes.put("ERR_IUID", "data, Unknown NE ID");
		errorCodes.put("ERR_INRY", "Status, NE not ready");
		errorCodes.put("ERR_SIOE", "Status, Internal operation failure");

		String errorText = errorCodes.get(c.getErrorCode());
		if (errorText == null) {
			errorText = "Unknown error " + c.getErrorCode();
		}

		if (c.getAdditionalErrorText() != null) {
			errorText = errorText + c.getAdditionalErrorText();
		}

		holder.setErrorData(c.getErrorCode(), errorText, null, new Exception(
		    "StackTrace"));
		log.debug("deleteConnection unsuccessful with exception: " + errorText
		    + " results=" + holder.toString());
		return holder;
	}

	@Override
	public void deleteCrossConnection(CrossConnection xcon) throws Exception {
		
		String EVENT_CODE = "DLT-CRS";

		Map<String, Object> data = new HashMap<String, Object>();
		data.put(ClientMessageXml.CROSSCONNECTION_KEY, xcon);

		

		AbstractNe ne = findAlignedNe(xcon.getSourceNeId());

		Map<String, Map<String, String>> eventHandlerList = ne
		    .getNeEventHandlingDefinition().get(ne.getNeType());
		Map<String, String> aHandler = eventHandlerList.get(EVENT_CODE);

		if (aHandler == null) {
			throw new Exception("DeleteConnection no handler for " + EVENT_CODE);
		}

		String className = aHandler.get(NePoxyDefinitionsParser.CLASS_ATTR);
		log.debug("DeleteConnection invoking commandlet  " + className + " for "
		    + EVENT_CODE);

		AbstractCommandlet command = AbstractCommandlet.getCommandlet(className,
		    data);
		if (command.start()) {
			log.debug("DeleteConnection commandlet returned true");
			return;
		}
		throw new Exception("DeleteConnection failed, commandlet returned false");
	}

	@Override
	public void deleteManualAdjacency(String neIEEE, String port)
	    throws Exception {
		log.debug("Processing deleteManualAdjacency invoked with " + neIEEE + " "
		    + port);
		deleteAdjacency(neIEEE, port);
	}

	@Override
	public void deleteManualAdjacency(String sourceIEEE, String sourcePort,
	    String destIEEE, String destPort) throws Exception {
		log.debug("Processing deleteManualAdjacency invoked with " + sourceIEEE
		    + " " + sourcePort + " " + destIEEE + " " + destPort);
		deleteAdjacency(sourceIEEE, sourcePort);
		deleteAdjacency(destIEEE, destPort);
	}

	@Override
	public void deleteNetworkElement(NetworkElementHolder oldNe) throws Exception {
		String key = oldNe.getIp() + ":" + oldNe.getPort();
		

		AbstractNe ne = DiscoverNePool.INSTANCE.getNe(key);
		if (ne == null) {
			throw new Exception("Unable to delete NE '" + key + "' does not exist");
		}

		try {
			
			DiscoverNePool.INSTANCE.removeNe(ne);
			
			DbUtility.INSTANCE.deleteNe(ne);
			
			ne.terminate();
		}
		catch (Exception e) {
			throw new Exception("Failed to delete NE '" + key + "'!", e);
		}
	}

	@Override
	public void editFacility(String neid, String aid, String tna,
	    String faclabel, String mtu, String srlg, String grp, String cost,
	    String metric2, String sigType, String constraints, String domainId,
	    String siteId) throws Exception {
		log.debug("Processing editFacility invoked with " + neid + " " + aid + " "
		    + tna + " " + faclabel + " " + mtu + " " + srlg + " " + grp + " "
		    + cost + " " + metric2 + " " + " " + sigType + " " + constraints + " "
		    + domainId + " " + siteId);
		String EVENT_CODE = "ED-FAC";
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(ClientMessageXml.NEID_KEY, neid);
		data.put(ClientMessageXml.AID_KEY, aid);
		data.put(ClientMessageXml.TNA_KEY, tna);
		data.put(ClientMessageXml.FACLABEL_KEY, faclabel);
		if (mtu != null && !"N/A".equalsIgnoreCase(mtu)) { // Proxy will try to
			                                                 // update the NE even if
			                                                 // the data is invalid
			data.put(ClientMessageXml.MTU_KEY, mtu);
		}
		data.put(ClientMessageXml.SRLG_KEY, srlg);
		data.put(ClientMessageXml.GROUP_KEY, grp);
		data.put(ClientMessageXml.COST_KEY, cost);
		data.put(ClientMessageXml.METRIC_KEY, metric2);
		data.put(ClientMessageXml.SIGNALINGTYPE_KEY, sigType);
		data.put(ClientMessageXml.CONSTRAINT_KEY, constraints);
		data.put(ClientMessageXml.DOMAIN_KEY, domainId);
		data.put(ClientMessageXml.SITE_KEY, siteId);

		AbstractNe ne = findAlignedNe(neid);

		Map<String, Map<String, String>> eventHandlerList = ne
		    .getNeEventHandlingDefinition().get(ne.getNeType());
		Map<String, String> aHandler = eventHandlerList.get(EVENT_CODE);

		if (aHandler == null) {
			throw new Exception("editFacility no handler for " + EVENT_CODE);
		}

		String className = aHandler.get(NePoxyDefinitionsParser.CLASS_ATTR);
		log.debug("editFacility invoking commandlet  " + className + " for "
		    + EVENT_CODE);

		AbstractCommandlet command = AbstractCommandlet.getCommandlet(className,
		    data);
		if (command.start()) {
			log.debug("editFacility commandlet returned true");
			return;
		}
		throw new Exception("editFacility failed, commandlet returned false");
	}

	@Override
	public void enrollNetworkElement(NetworkElementHolder newNe) throws Exception {
		

		String ipAddress = newNe.getIp();
		int portNumber = Integer.parseInt(newNe.getPort());

		// FIXME: Why is this using a colon as the database PK column is using an
		// underscore as primary key?
		String internalNeId = ipAddress + ":" + portNumber;
		DiscoverNePool nePool = DiscoverNePool.INSTANCE;
		AbstractNe ne = nePool.getNe(internalNeId);
		if (ne != null) {
			throw new Exception("The NE " + ne.getNeId()
			    + " is already beeing managed " + ne.getIpAddress() + " "
			    + ne.getPortNumber());
		}

		ne = DbUtility.INSTANCE.holderToAbstractNe(newNe);
		ne.initialize();
		nePool.addNe(internalNeId, ne);
	}

	@Override
	public List<CrossConnection> getCrossConnections(String targetNeId)
	    throws Exception {
		
		AbstractNe ne = findAlignedNe(targetNeId);
		return DbUtility.INSTANCE.retrieveAllNeXconnect(ne);
	}

	@Override
	public List<Facility> getFacilities(String targetNeId) throws Exception {
		
		AbstractNe ne = findAlignedNe(targetNeId);
		return DbUtility.INSTANCE.retrieveNeFacilities(ne);
	}

	@Override
	public void updateNetworkElementPosition(String ip, String port,
	    Double positionX, Double positionY) throws Exception {
		Map<String, String> filterMap = new HashMap<String, String>();
		filterMap.put(DbKeys.NetworkElementCols.NEIP, ip);
		filterMap.put(DbKeys.NetworkElementCols.NEPORT, port);
		List<NetworkElementHolder> networkElements = DbNetworkElement.INSTANCE
		    .retrieve(filterMap);

		if (networkElements.size() > 0) {
			for (NetworkElementHolder networkElementHolder : networkElements) {
				AbstractNe networkElement = DiscoverNePool.INSTANCE
				    .getNeByTidOrIdOrIpandPort(
				        networkElementHolder.getIp() + ":"
				            + networkElementHolder.getPort());
				if(networkElement != null){
					networkElement.setPositionX(positionX);
					networkElement.setPositionY(positionY);
				}
				DbUtility.INSTANCE.upDateNe(networkElement);
			}
		}
	}

	@Override
	public List<NetworkElementHolder> getNetworkElements(String tidorId)
	    throws Exception {
		
		if (tidorId == null) {
			log.debug("Process rtrv-nelist for: ALL");
			/*
			 * Rather than simply fetch all the NEs in the database, we only return
			 * those that we know about (in the discovery pool) This is the way the
			 * old code functioned and I'm keeping it here though I wonder it we could
			 * get a way with just returning all NEs in the database
			 */
			List<NetworkElementHolder> results = new ArrayList<NetworkElementHolder>();
			for (AbstractNe ne : DiscoverNePool.INSTANCE.getNeList().values()) {

				Map<String, String> filterMap = new HashMap<String, String>();
				filterMap.put(DbKeys.NetworkElementCols.NEIP, ne.getIpAddress());
				filterMap.put(DbKeys.NetworkElementCols.NEPORT,
				    Integer.toString(ne.getPortNumber()));
				List<NetworkElementHolder> list = DbNetworkElement.INSTANCE
				    .retrieve(filterMap);
				results.addAll(list);
			}
			return results;
		}

		AbstractNetworkElement ne = DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(tidorId);
		if (ne == null) {
			throw new Exception("Unable to find NE " + tidorId + " ");
		}

		Map<String, String> filterMap = new HashMap<String, String>();
		filterMap.put(DbKeys.NetworkElementCols.NEIP, ne.getIpAddress());
		filterMap.put(DbKeys.NetworkElementCols.NEPORT,
		    Integer.toString(ne.getPortNumber()));
		return DbNetworkElement.INSTANCE.retrieve(filterMap);
	}

	public NeProxyRemote getRemote() {
		return neProxyRemote;
	}

	@Override
	public String getXmlAlarm(String neId) throws Exception {
		
		String EVENT_CODE = "RTRV-ALM";
		if (!DiscoverNePool.INSTANCE.isReady()) {
			throw new Exception(
			    "getXML alarm failed, DiscoveryNePool is not ready yet");
		}

		AbstractNe ne = findAlignedNe(neId);

		Map<String, Map<String, String>> eventHandlerList = ne
		    .getNeEventHandlingDefinition().get(ne.getNeType());
		Map<String, String> aHandler = eventHandlerList.get(EVENT_CODE);

		if (aHandler == null) {
			throw new OperationNotSupportedException("getXmlAlarm no handler for "
			    + EVENT_CODE + " from " + eventHandlerList + " for "
			    + ne.getIpAddress() + ":" + ne.getPortNumber() + " " + ne.getNeName());
		}

		String className = aHandler.get(NePoxyDefinitionsParser.CLASS_ATTR);
		log.debug("getXmlAlarm invoking commandlet " + className + " for "
		    + EVENT_CODE);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put(ClientMessageXml.NEID_KEY, neId);
		AbstractCommandlet command = AbstractCommandlet.getCommandlet(className,
		    data);
		if (command.start()) {
			return "<" + ClientMessageXml.NEEVENT_NODE + ">"
			    + command.getCandidate().getXmlResult() + "</"
			    + ClientMessageXml.NEEVENT_NODE + ">";
		}

		throw new Exception("getXmlAlarm failed, commandlet returned false");
	}

	@Override
	public boolean isAlive() throws Exception {
		// Will throw an exception if we are not currently active
		return true;
	}

	@Override
	public ProvisioningResultHolder postDeleteConnections(
	    List<CrossConnection> xconList) throws Exception {

		
		// Pass my concrete object into the AbstractCommandlet world via the data
		// parameters for now.
		Map<String, Object> dm = new HashMap<String, Object>();
		dm.put(ClientMessageXml.CROSSCONNECTION_LIST_KEY, xconList);
		PostDeleteConnections dc = new PostDeleteConnections(dm);

		boolean result = dc.start();

		Candidate c = dc.getCandidate();
		ProvisioningResultHolder holder = new ProvisioningResultHolder();
		if (c.getXmlResult() != null) {
			holder.setOutputData(c.getXmlResult());
		}

		if (result) {
			return holder;
			// Success
		}

		Map<String, String> errorCodes = new HashMap<String, String>();
		errorCodes.put("ERR_IUID", "data, Unknown NE ID");
		errorCodes.put("ERR_INRY", "Status, NE not ready");
		errorCodes.put("ERR_SIOE", "Status, Internal operation failure");

		String errorText = errorCodes.get(c.getErrorCode());
		if (errorText == null) {
			errorText = "Unknown error " + c.getErrorCode();
		}

		if (c.getAdditionalErrorText() != null) {
			errorText = errorText + c.getAdditionalErrorText();
		}

		holder.setErrorData(c.getErrorCode(), errorText, null, new Exception(
		    "StackTrace"));
		log.debug("postDeleteConnections unsuccessful with exception: " + errorText
		    + " results=" + holder.toString());
		return holder;
	}

	@Override
	public ProvisioningResultHolder prepCreateConnections(
	    List<CrossConnection> xconList) throws Exception {

		

		// Pass my concrete object into the AbstractCommandlet world via the data
		// parameters for now.
		Map<String, Object> dm = new HashMap<String, Object>();
		dm.put(ClientMessageXml.CROSSCONNECTION_LIST_KEY, xconList);
		PrepAddConnections ac = new PrepAddConnections(dm);

		boolean result = ac.start();

		Candidate c = ac.getCandidate();
		ProvisioningResultHolder holder = new ProvisioningResultHolder();
		if (c.getXmlResult() != null) {
			holder.setOutputData(c.getXmlResult());
		}

		if (result) {
			return holder;
			// Success
		}

		Map<String, String> errorCodes = new HashMap<String, String>();
		errorCodes.put("PLNA", "Login Not Active");
		errorCodes.put("ERR_IUID", "data, Unknown NE ID");
		errorCodes.put("ERR_INRY", "Status, NE not ready");
		errorCodes.put("ERR_IEAE", "data, Entity Already Exists");
		errorCodes.put("ERR_IENE", "data, Entity does Not Exists");
		errorCodes.put("ERR_SIOE", "Status, Internal operation failure");

		String errorText = errorCodes.get(c.getErrorCode());
		if (errorText == null) {
			errorText = "Unknown error " + c.getErrorCode();
		}

		if (c.getAdditionalErrorText() != null) {
			errorText = errorText + c.getAdditionalErrorText();
		}

		holder.setErrorData(c.getErrorCode(), errorText, null, new Exception(
		    "StackTrace"));
		log.debug("prepCreateConnections unsuccessful with exception: " + errorText
		    + " results=" + holder.toString());
		return holder;
	}

	@Override
	public void registerForEventNotifications(NeProxyEventCallback cb)
	    throws Exception {
		NeProxyEventServer.INSTANCE.addListener(cb);
	}

	@Override
	public void toggleNetworkElementAssociation(NetworkElementHolder existingNe)
	    throws Exception {
		String key = existingNe.getIp() + ":" + existingNe.getPort();
		

		AbstractNe ne = DiscoverNePool.INSTANCE.getNe(key);

		if (ne == null) {
			throw new Exception("Unable to locate NE '" + key + "'!");
		}

		if (ne instanceof NetworkElement) {
			NetworkElement me = (NetworkElement) ne;
			me.toggleAssocation();
			log.debug("ToggleNE: command sent to '" + key + "'");
		}
		else if (ne instanceof Force10NetworkElement) {
			Force10NetworkElement me = (Force10NetworkElement) ne;
			me.toggleAssocation();
			log.debug("ToggleNE: command sent to Force10 '" + key + "'");
		}
		else {
			throw new OperationNotSupportedException(
			    "NE '"
			        + key
			        + "' does not support the toggle command, unable to toggle communications link "
			        + ne.getClass().toString());
		}

		DbLog.INSTANCE.generateLog(
		    new LogRecord(null, null, null, null, LogKeyEnum.KEY_NE_TOGGLED,
		        new String[] { ne.getNeName(), ne.getIpAddress(),
		            Integer.toString(ne.getPortNumber()) }));
	}

	/**
	 * addAdjacency
	 */
	private void addAdjacency(String sourceIEEE, String sourcePort,
	    String destIEEE, String destPort) throws Exception {
		log.debug("Processing addAdjacency invoked with " + sourceIEEE + " "
		    + sourcePort + " " + destIEEE + " " + destPort);
		AbstractNe ne = DiscoverNePool.INSTANCE.getNeByTidOrIdOrIpandPort(
		    sourceIEEE);
		if (ne == null) {
			throw new Exception("Unable to locate targetNE '" + sourceIEEE + "'");
		}

		if (ne.getState() != NeStatus.NE_ALIGNED) {
			throw new Exception("Cannot add adjacency, NE not aligned '" + sourceIEEE
			    + "'");
		}

		List<NetworkElementAdjacency> list = new ArrayList<NetworkElementAdjacency>();
		// Construct a txTag that is unique to this manual link
		String txTag = sourceIEEE + "_" + sourcePort;
		// Construct an rxTag that is unique to this manual link
		String rxTag = destIEEE + "_" + destPort;

		NetworkElementAdjacency adj = new NetworkElementAdjacency(sourceIEEE,
		    sourcePort, txTag, rxTag,
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE, true);
		list.add(adj);

		DbNetworkElementAdjacency.INSTANCE.add(list);

		// Generate event
		Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
		dbchgEvent.setReportType("adjacency");
		Date date = new Date();

		SimpleDateFormat simpledateformat = new SimpleDateFormat(
		    "yyyy-MM-dd'-'HH-mm-ss");
		String dateTime = simpledateformat.format(date);
		dbchgEvent.setOccurrentDate(dateTime);
		dbchgEvent.setEventId(ne.getNeId() + "_" + dateTime);
		dbchgEvent.setOwnerId(ne.getTerminalId());
		dbchgEvent.updateDescription("manualAdjacencyCreation");
		Map<String, String> eventData = new HashMap<String, String>();
		eventData.put("source", sourceIEEE);
		eventData.put("sourceAid", sourcePort);
		eventData.put("target", destIEEE);
		eventData.put("targetAid", destPort);
		eventData.put("aid", sourcePort);
		eventData.put("operation", "add");
		dbchgEvent.addDataElement(eventData);
		NeProxy.generateEvent(dbchgEvent, ClientMessageXml.DBCHG_EVENT_VALUE);
		DbUtility.INSTANCE.generateLog(
		    new LogRecord(null, ne.getIpAddress(), null, null,
		        LogKeyEnum.KEY_MANUAL_ADJACENCY_CREATED, null, eventData));
	}

	private void deleteAdjacency(String sourceIEEE, String sourcePort)
	    throws Exception {
		log.debug("Processing deleteAdjacency invoked with " + sourceIEEE + " "
		    + sourcePort);

		DbNetworkElementAdjacency.INSTANCE.delete(sourceIEEE, sourcePort,
		    DbKeys.NetworkElementAdjacencyColsV2.MANUAL_LAYR_TYPE);

		AbstractNe ne = DiscoverNePool.INSTANCE.getNeByTidOrIdOrIpandPort(
		    sourceIEEE);

		if (ne != null) {
			// Generate event
			Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
			dbchgEvent.setReportType("adjacency");
			Date date = new Date();
			SimpleDateFormat simpledateformat = new SimpleDateFormat(
			    "yyyy-MM-dd'-'HH-mm-ss");
			String dateTime = simpledateformat.format(date);
			dbchgEvent.setOccurrentDate(dateTime);
			dbchgEvent.setEventId(ne.getNeId() + "_" + dateTime);
			dbchgEvent.setOwnerId(ne.getTerminalId());
			dbchgEvent.updateDescription("manualAdjacencyDeletion");
			Map<String, String> eventData = new HashMap<String, String>();
			eventData.put("aid", sourcePort);
			eventData.put("operation", "delete");
			dbchgEvent.addDataElement(eventData);
			NeProxy.generateEvent(dbchgEvent, ClientMessageXml.DBCHG_EVENT_VALUE);
			DbUtility.INSTANCE.generateLog(
			    new LogRecord(null, ne.getIpAddress(), null, null,
			        LogKeyEnum.KEY_MANUAL_ADJACENCY_DELETED, null, eventData));
		}
	}

	private AbstractNe findAlignedNe(String target) throws Exception {
		AbstractNe ne = DiscoverNePool.INSTANCE.getNeByTidOrIdOrIpandPort(
		    target);

		if (ne == null) {
			throw new Exception("Could not find NE '" + target + "'");
		}

		if (ne.getState() != NeStatus.NE_ALIGNED) {
			throw new Exception("NE '" + target
			    + "' is not aligned, cannot perform operation for  "
			    + ne.getIpAddress() + ":" + ne.getPortNumber() + " " + ne.getState());
		}
		return ne;
	}

  @Override
  public void updateAddressAndPort(final String oldAddress, final int oldPort, final String newAddress, final int newPort) throws Exception {
    final AbstractNe ne = DiscoverNePool.INSTANCE.getNe(oldAddress+":"+oldPort);
    DiscoverNePool.INSTANCE.removeNe(ne);
    DbUtility.INSTANCE.updateAddressAndPort(oldAddress, oldPort, newAddress, newPort);
    DiscoverNePool.INSTANCE.readNetworkElementsFromDatabase();
  }


}
