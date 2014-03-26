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

/**
 * @author nguyentd
 */
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.NoRouteToHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAssociationEvent;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlEvent;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractInitializeNe;
import com.nortel.appcore.app.drac.server.neproxy.command.cpl.DeleteConnectionTask;
import com.nortel.appcore.app.drac.server.neproxy.command.cpl.ReptAlarm.QueryDocTask;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.controller.NEAOListener;
import com.nortel.appcore.app.drac.server.neproxy.mediation.controller.TL1AssociationListener;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.IPAddressInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.UserProfile;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AssociationEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.TL1LanguageEngine;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.TL1SessionMgmt;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/*
 * This is a TL1 network element. Rename it on a rainy day.
 */
public final class NetworkElement extends AbstractNe implements
    TL1AssociationListener, NEAOListener {
  private static final Logger log = LoggerFactory.getLogger(NetworkElement.class);
	public class EventHandlingTask implements Runnable {
		private final AbstractCommandlet commandlet;
		private final Map<String, Object> aParameter;

		public EventHandlingTask(AbstractCommandlet command,
		    Map<String, Object> parameters) {
			commandlet = command;
			aParameter = parameters;
		}

		@Override
		public void run() {
			try {
				log.debug("Process event from NE " + getNeInfo().getNeName() + " "
				    + commandlet.getClass());

				if (commandlet.start()) {
					TL1AlarmEvent aReport = (TL1AlarmEvent) aParameter.get(EVENTRECV_KEY);
					String eventType;
					if (aReport.isAlarm()) {
						eventType = ClientMessageXml.ALARM_EVENT_VALUE;
					}
					else {
						eventType = ClientMessageXml.DBCHG_EVENT_VALUE;
					}

					NeProxy.generateEvent(
					    (Tl1XmlEvent) aParameter.get(AbstractCommandlet.RESULT_KEY),
					    eventType);


					Object additionalEvent = aParameter
					    .get(AbstractCommandlet.ADDITIONALEVENT_KEY);
					if (additionalEvent != null) {

						NeProxy.generateEvent((Tl1XmlEvent) additionalEvent,
						    ClientMessageXml.DBCHG_EVENT_VALUE);
					}
				}
				else {
					log.debug("Got a false return code while executing "
					    + commandlet.getClass() + " ne " + getNeInfo().getNeName());
				}
			}
			catch (Exception t) {
				log.error("Failed to execute " + commandlet.getClass() + " ne "
				    + getNeInfo().getNeName(), t);
			}
		}
	}

	public enum NE_TYPE_KEY {
		MODE, RELEASE, TYPE;
	}

	class MyThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r);
		}
	}

	// private static Integer myAtag = Integer.valueOf(0);

	// Store the shelves within the NE with domain ID
	private final Map<String, String> shelves = new HashMap<String, String>();

	private TL1SessionMgmt tl1Session;

	// Configuration information
	// private Integer apsId;
	private final Set<String> protectionAid = new HashSet<String>();
	private final Map<String, String> ringName = new HashMap<String, String>();

	// Not all NEs use and populate the following HashMap
	private Map<String, Map<String, String>> cacheFacility;
	private NEAOListener neAoListener;

	/*
	 * Instead of creating a new queue to sequentially handle incoming autonomous
	 * message, just use Executors instead.
	 */

	private static Set<String> unhandledEvents = new HashSet<String>();

	private final Executor executor = Executors
	    .newSingleThreadExecutor(new MyThreadFactory());

	// Use for delaying the sending of command down to the NE like CPL NE
	private final Map<String, DeleteConnectionTask> commandQueue = new HashMap<String, DeleteConnectionTask>();

	private final Map<String, QueryDocTask> timerQueue = new HashMap<String, QueryDocTask>();

	public NetworkElement(String uid, CryptedString passwd, String ipAddr,
	    int portNumber) {
		this.setIpAddress(ipAddr);
		this.setUid(uid);
		this.setPasswd(passwd);
		this.setPortNumber(portNumber);

		UserProfile userInfo = new UserProfile(uid, passwd);
		IPAddressInfo ipAddrInfo = new IPAddressInfo(ipAddr, portNumber);
		setNeInfo(new NetworkElementInfo("00-00-00-00-00-00", "", ipAddrInfo,
		    userInfo));
		setDbChgEvent(new Tl1XmlAssociationEvent(this));
		initMap();
	}

	public NetworkElement(String uid, CryptedString passwd, String ipAddr,
	    int portNumber, String terminalId) {
		this(uid, passwd, ipAddr, portNumber);
		this.setTerminalId(terminalId);
	}

	public static String getNeType(Map<NE_TYPE_KEY, String> neResults) {
		String unMappedNeType = neResults.get(NE_TYPE_KEY.TYPE);
		String release = neResults.get(NE_TYPE_KEY.RELEASE);

		String neMappedName = NePoxyDefinitionsParser.INSTANCE.getNeTypeMapping()
		    .get(unMappedNeType);
		if (neMappedName == null) {
			return null;
		}

		if (release != null) {
			/*
			 * look for a closer match, if one exists. Right now we have a mapped name
			 * such as "OME", look for lower case ome + the NE release string. Since
			 * the NE release string is very specific, we'd only expect to find a
			 * partial match so omeREL0520Z.IM would no exist, but omeREL052 might.
			 */
			String pattern = neMappedName.toLowerCase() + release;
			String bestMatch = "";
			for (String s : NePoxyDefinitionsParser.INSTANCE.getNeTypeMapping().keySet()) {
				if (pattern.startsWith(s)) {
					if (s.length() > bestMatch.length()) {
						bestMatch = s;
					}
				}
			}
			if (!"".equals(bestMatch)) {
				// something better was found
				neMappedName = NePoxyDefinitionsParser.INSTANCE.getNeTypeMapping()
				    .get(bestMatch);
			}
		}

		return neMappedName;
	}

	public static Map<NE_TYPE_KEY, String> getNeTypeAndRelease(
	    TL1SessionMgmt tl1Session, NetworkElementInfo neInfo) {
		Map<NE_TYPE_KEY, String> results = new HashMap<NE_TYPE_KEY, String>();

		/*
		 * Need to send one or two commands to figure out the type of NE so that the
		 * correct TL1wrapper can be loaded.
		 */
		/**
		 * RTRV-NETYPE:OME0359::1;IP 1 < OME0359 01-05-17 04:39:38 M 1 COMPLD
		 * "NORTEL,\"Optical Multiservice Edge 6500 OPTICAL  \",OCP,\"REL0520Z.IM\""
		 * ;
		 */
		/**
		 * RTRV-SW-VER:OME0359::1;IP 1 < OME0359 01-05-17 04:40:20 M 1 COMPLD
		 * "SHELF-1:REL0520Z.IM"
		 */
		/**
		 * <pre>
		 * RTRV-NE output:: TDM3-AMSTERDAM1 09-01-19 15:28:36 M 5 COMPLD
		 * "NEFUNCTION=\"Nortel HDXc at Netherlight Amsterdam1\",NEROLE=HDXc,NEMODE=SONET,NELOC=\"Amsterdam1_SARA_NetherLight_AC11\",NEALIAS=\"SURFnet\""
		 * </pre>
		 */

		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(ObjectElement.OPERATION_KEY,
		    Tl1CommandCode.RTRV_NETYPE.toString());
		// String neType = null;
		// String neSwVer = null;
		List<Map<String, String>> result;
		try {
			result = tl1Session.sendToNE(parameter);
			Map<String, String> aResult = result.get(0);
			results.put(NE_TYPE_KEY.TYPE, aResult.get("NETYPE").toUpperCase());
			parameter.clear();
			parameter.put(ObjectElement.OPERATION_KEY,
			    Tl1CommandCode.RTRV_SW_VER.toString());
			result = tl1Session.sendToNE(parameter);
			aResult = result.get(0);
			results.put(NE_TYPE_KEY.RELEASE, aResult.get("NE SW_VERSION")
			    .toUpperCase());

		}
		catch (Exception sendEx) {
			log.debug("RTRV-NETYPE/RTRV-SW-VER failed, trying RTRV-NE instead "
			    + neInfo.getAddressInfo(), sendEx);
			// ok so it isn't an OME, let try something else
			try {
				parameter.clear();
				parameter.put(ObjectElement.OPERATION_KEY, Tl1CommandCode.RTRV_NE.toString());
				result = tl1Session.sendToNE(parameter);
				Map<String, String> aResult = result.get(0);
				results.put(NE_TYPE_KEY.TYPE, aResult.get("NEROLE").toUpperCase());
				results.put(NE_TYPE_KEY.MODE, aResult.get("NEMODE").toUpperCase());
				results.put(NE_TYPE_KEY.RELEASE, aResult.get("NE SW_VERSION").toUpperCase());
			}
			catch (Exception wrongNeEx) {
				log.error("Error: ", wrongNeEx);
				// have to do something here
			}
		}

		return results;
	}

	@Override
	public void changeNePassword(String newUid, CryptedString newPassword) {
		setUid(newUid);
		setPasswd(newPassword);
		getNeInfo().getUserProfile().setLoginPassword(newPassword);
		getNeInfo().getUserProfile().setUserID(newUid);
		DbUtility.INSTANCE.upDateNePassword(this);
		DbUtility.INSTANCE.upDateNeUid(this);
		toggleAssocation();
	}

	public DeleteConnectionTask deQueueCommand(String key) {
		synchronized (commandQueue) {
			return commandQueue.remove(key);
		}
	}

	public QueryDocTask deQueueTimer(String key) {
		synchronized (timerQueue) {
			return timerQueue.remove(key);
		}
	}

	public void enQueueCommand(String key, DeleteConnectionTask aCommand) {
		synchronized (commandQueue) {
			commandQueue.put(key, aCommand);
		}
	}

	public void enQueueTimer(String key, QueryDocTask aTimer) {
		synchronized (timerQueue) {
			timerQueue.put(key, aTimer);
		}
	}

	public Map<String, DeleteConnectionTask> getAllCommand() {
		synchronized (commandQueue) {
			return commandQueue;
		}
	}

	public Map<String, QueryDocTask> getAllTimer() {
		synchronized (timerQueue) {
			return timerQueue;
		}
	}

	public NEAOListener getAoListener() {
		return neAoListener;
	}

	/**
	 * @return the cacheFacility
	 */
	public Map<String, Map<String, String>> getCacheFacility() {
		return cacheFacility;
	}

	/**
	 * @return the protectionAid
	 */
	public Set<String> getProtectionAid() {
		return protectionAid;
	}

	/**
	 * @return the ringName
	 */
	public Map<String, String> getRingName() {
		return ringName;
	}

	/**
	 * @return the shelves
	 */
	public Map<String, String> getShelves() {
		return shelves;
	}

	/**
	 * @return the tl1Session
	 */
	public TL1SessionMgmt getTl1Session() {
		return tl1Session;
	}

	@Override
	public void nextState() {
		if (getState() == NeStatus.NE_NOT_PROVISION) {
			return;
		}
		switch (getState()) {
		case NE_NOT_CONNECT:
			if (getPreviousNeState() != getState()) {
				setPreviousNeState(NeStatus.NE_NOT_CONNECT);
				DbUtility.INSTANCE.upDateNeStatus(this);
				sendAssociationEvent(this.getNeStatus());

				// For loss of association, mark all facilities invalid
				try {

					DbUtility.INSTANCE.invalidateFacility(this);
				}
				catch (Exception e) {
					log.error("Error setting facilities invalid. ", e);
				}
			}
			/* should I clean up (i.e. destroy database, ... */
			if (this.tl1Session != null) {
				tl1Session.deleteTL1Session(false);
				tl1Session = null;
			}

			int rc = connect();
			switch (rc) {
			case 1:
			  // TODO: SNMP Not Authenticated
				setState(NeStatus.NE_NOT_AUTHENTICATED);
				setPreviousNeState(NeStatus.NE_NOT_CONNECT);
				break;
			case 0:
	       // TODO: SNMP Authenticated and associated
				setState(NeStatus.NE_ASSOCIATED);
				setPreviousNeState(NeStatus.NE_NOT_CONNECT);
				DbUtility.INSTANCE.upDateNeStatus(this);
				sendAssociationEvent(this.getNeStatus());
				break;
			/*
			 * clear any pending alarm and send an event else should raise alarm if
			 * not has been raised and send an event
			 */
			}
			/*
			 * if ( rc == 0) { setState(ASSOCIATED); previousNeState = ASSOCIATED;
			 * DbUtility.INSTANCE.upDateNeStatus(this);
			 * sendAssociationEvent(this.getNeStatus()); }
			 */
			DiscoverNePool.INSTANCE.enqueueTask(this);
			return;
		case NE_NOT_AUTHENTICATED:
		  // TODO: SNMP Not Authenticated
			sendAssociationEvent(this.getNeStatus());
			DbUtility.INSTANCE.upDateNeStatus(this);
			tl1Session.deleteTL1Session(false);
			return;
		case NE_ASSOCIATED:
		  // TODO: SNMP Authenticated and associated
			setState(NeStatus.NE_INITIALIZING);
			if (getPreviousNeState() != getState()) {
				setPreviousNeState(NeStatus.NE_INITIALIZING);
				DbUtility.INSTANCE.upDateNeStatus(this);
				sendAssociationEvent(this.getNeStatus());
			}
			// @TODO WP: Are we missing the break here or do we want to fall through?
		case NE_INITIALIZING:
			try {
				if (getInitializeNeObject().start()) {
					TL1AssociationEvent tempEvent = new TL1AssociationEvent(
					    TL1AssociationEvent.ASSOCIATION_UP, this.getNeInfo());
					receiveEvent(tempEvent);
					setState(NeStatus.NE_ALIGNED);
					if (getPreviousNeState() != getState()) {
						setPreviousNeState(NeStatus.NE_ALIGNED);
						DbUtility.INSTANCE.upDateNe(this);
						sendAssociationEvent(this.getNeStatus());
					}
				}
				else {
					// It is possible that the connection is down
					if (!tl1Session.isConnected()) {
						setState(NeStatus.NE_NOT_CONNECT);
					}
					DiscoverNePool.INSTANCE.enqueueTask(this);
				}
			}
			catch (Exception e) {
				log.error("Exception durring initializing NE", e);
				setState(NeStatus.NE_NOT_CONNECT);
				DiscoverNePool.INSTANCE.enqueueTask(this);
			}
			return;
		default:
			return;
		}
	}

	@Override
	public void receiveEvent(TL1AlarmEvent event) {
		// OCRDATE=11-23, slotId=401, CONDTYPE=SYNCPRI, AIDDET=OSK-3-4-8-000000,
		// DIRN=NA, CONDDESCR=TIMING REFERENCE000001, YEAR=2004,
		// DGNTYPE=000001-0000-0000,
		// probableCause=0000, documentationId=0000, OBSDBHVR=TYP-SH-SL-PRT-SIG,
		// AID=MXT-1-401, shelfId=1, EXPTDBHVR=TYP-SH-SL-PRT-FREQ,
		// entityType=MXT,
		// operation=REPT-ALM, SRVEFF=NSA, NTFNCDE=MN, AIDTYPE=EQPT,
		// OCRTM=10-18-51,
		// alarmId=000001, neId=1232456, LOCN=NEND}]
		try {
			String eventCode = event.getCommandCode().toString();
			//
			Map<String, Map<String, String>> eventHandlerList = getNeEventHandlingDefinition()
			    .get(getNeInfo().getNeType());

			if (eventHandlerList == null) {
				log.debug("no event handling definition for this NE"
				    + this.getNeInfo().getNeID());
				return;
			}

			Map<String, String> aHandler = eventHandlerList.get(eventCode);

			if (aHandler != null) {
				Map<String, Object> parameters = new HashMap<String, Object>(aHandler);
				String className = (String) parameters.get(NePoxyDefinitionsParser.CLASS_ATTR);
				parameters.put(EVENTRECV_KEY, event);
				parameters.put(NePoxyDefinitionsParser.NETWORKELEMENT_KEY, this);


				AbstractCommandlet command = AbstractCommandlet.getCommandlet(
				    className, parameters);
				executor.execute(new EventHandlingTask(command, parameters));
			}
			else {
				// Reduce the noise, we'll only log this once per eventCode, per NE.
				// then shut up.
				if (!unhandledEvents
				    .contains(getNeInfo().getNeType() + "-" + eventCode)) {
					unhandledEvents.add(getNeInfo().getNeType() + "-" + eventCode);
					log.debug("Ignoring TL1 event no handler for " + eventCode + " for "
					    + getNeInfo().getNeType() + " have "
					    + eventHandlerList.toString()
					    + ". This message will be disaplayed once per event/ne INSTANCE.");
				}
			}
		}
		catch (Exception e) {
			log.error("Failed handling event", e);
		}
	}

	@Override
	public void receiveEvent(TL1AssociationEvent event) {
		// Current state is Aligned and the event is from Inservice to
		// OutofService

		if (getState() == NeStatus.NE_ALIGNED
		    && event.getCode() > TL1AssociationEvent.ASSOCIATION_UP) {
			this.setState(NeStatus.NE_NOT_CONNECT);
			DiscoverNePool.INSTANCE.enqueueTask(this);
		}
	}

	public void reloadAdapterInfo(String neMappedName) {
		getNeInfo().setAdapterName(NetworkElementInfo.PARENT + neMappedName + ".");
		tl1Session.reloadNeInfo(getNeInfo());
	}

	public void setAoListener(NEAOListener aListener) {
		neAoListener = aListener;
	}

	/**
	 * @param apsId
	 *          the apsId to set
	 */
	public void setApsId(Integer apsId) {
		// this.apsId = apsId;
	}

	/**
	 * @param cacheFacility
	 *          the cacheFacility to set
	 */
	public void setCacheFacility(Map<String, Map<String, String>> cacheFacility) {
		this.cacheFacility = cacheFacility;
	}

	@Override
	public void terminate() {
		log.debug("Terminating NE: " + this.getIpAddress() + ":"
		    + this.getPortNumber());
		/*
		 * It's important to set the state to NOTCONNECT, not NOTPROVISION, in order
		 * to have the NE to be re-discover again in redundant switching. In case of
		 * the manual deletion, the record is deleted in DB as well as in the list
		 * of NE Discovery process, so make sure that it's not there before setting
		 * the state to NOTPROVISION.
		 */

		setState(NeStatus.NE_NOT_CONNECT);
		try {
		  if(tl1Session!=null) {
		    tl1Session.deleteTL1Session(true);
		  }
		}
		catch (Exception e) {
			log.error("Failed to terminate the TL1 session", e);
		}
		try {
      DbUtility.INSTANCE.upDateNeStatus(this);
    }
    catch (Exception e) {
      log.warn("Exception updating NE status during shutdown");
    }
		if (DiscoverNePool.INSTANCE.getNe(
		    this.getIpAddress() + ":" + this.getPortNumber()) == null) {
			setState(NeStatus.NE_NOT_PROVISION);
		}
		sendAssociationEvent(NeStatus.NE_NOT_PROVISION);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toDebugString() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.toDebugString());
		builder.append(" NetworkElement [cacheFacility=");
		builder.append(cacheFacility);
		builder.append(", protectionAid=");
		builder.append(protectionAid);
		builder.append(", ringName=");
		builder.append(ringName);
		builder.append(", shelves=");
		builder.append(shelves);
		builder.append(", tl1Session=");
		builder.append(tl1Session);
		builder.append("]");
		return builder.toString();
	}

	public void toggleAssocation() {
		// Big hammer, find the socket and close it
		TL1LanguageEngine eng = tl1Session == null ? null : tl1Session.getTl1Engine();
		if (eng != null) {
			eng.closeUnderlyingSocket();
		}

		// If we were not auth state we won't retry the connection.
		if (getState() == NeStatus.NE_NOT_AUTHENTICATED) {
			setState(NeStatus.NE_NOT_CONNECT);
			setPreviousNeState(NeStatus.NE_NOT_AUTHENTICATED);
			DiscoverNePool.INSTANCE.enqueueTask(this);
		}
	}

	// Debug help
	/**
	 * Don't know if we depend on this returning XML format or not, use
	 * toDebugString instead
	 */
	@Override
	public String toString() {
		return toXMLDocument();
	}

	/**
	 * @return -1: common error 0: no error 1: failed to authenticate.
	 */
	private int connect() {
		try {
			/*
			 * WP: Before we connect/reconnect reset our adaptor name to the common
			 * one. As we connect we'll find out what type and release the NE is and
			 * select the correct adaptor, but in the mean time we'll use the common
			 * adapter for the act-user and other commands we fire at start up.
			 * Without this, the fist time we associate to the NE we use the common
			 * adapter, but on reconnection we'll use the product specific adapter
			 * which means we'll have to test the act-user scenario twice as the NE
			 * specific act-user could be different/wrong/missing.
			 */
			getNeInfo().setAdapterName(NetworkElementInfo.COMMON_TL1WRAPPER);

			boolean meteredTl1Session = true;
			tl1Session = new TL1SessionMgmt(getNeInfo(), meteredTl1Session);
			tl1Session.manageNE();
			try {
				tl1Session.establishTL1Session();
			}
			catch (NoRouteToHostException nre) {
				/*
				 * Don't log the full stack trace back, its not necessary and it makes
				 * the logs a mess if this keeps occurring.
				 */
				log.error("Failed to connect to NE, will retry: no route to host "
				    + getNeInfo().toString() + " --> " + nre);
				return -1;
			}
			catch (IOException ioe) {
				/*
				 * Don't log the full stack trace back, its not necessary and it makes
				 * the logs a mess if this keeps occurring.
				 */
				log.error("Failed to connect to NE, will retry: ioException "
				    + getNeInfo().toString() + " --> " + ioe);
				return -1;
			}
			catch (Exception e) {
				log.error(
				    "Failed to authenticate with NE or other error, will retry: Exception "
				        + getNeInfo().toString() + " --> " + e, e);
				return 1;
			}

			Map<NE_TYPE_KEY, String> neResults = getNeTypeAndRelease(tl1Session,
			    getNeInfo());
			String neMappedName = getNeType(neResults);

			if (neMappedName == null) {
				log.error("Unknown/unsupported NE type, unable to map NE to type "
				    + neResults + " " + getNeInfo().toString());
				return -1;
			}

			if (neResults.get(NE_TYPE_KEY.MODE) != null) {
				getNeInfo().setNeMode(
				    NETWORK_ELEMENT_MODE.valueOf(neResults.get(NE_TYPE_KEY.MODE)));
			}

			setNeRelease(neResults.get(NE_TYPE_KEY.RELEASE));
			log.debug("Established TL1 session with NE, setting NE release to "
			    + neResults.get(NE_TYPE_KEY.RELEASE) + " from" + neResults + " for "
			    + this.toDebugString());
			DbUtility.INSTANCE.upDateNeRelease(this);

			this.setNeType(NeType.fromString(neMappedName));
			if (this.getNeType() == NeType.UNKNOWN) {
				Exception e = new Exception("ERROR in mapping NE type - "
				    + this.getNeType() + " " + neMappedName);
				log.error("Error: ", e);
				return -1;
			}
			getNeInfo().setNeType(this.getNeType());

			reloadAdapterInfo(neMappedName);

			String packageName = this.getNeTypeMapping().get(NePoxyDefinitionsParser.PGKNAME_ATTR)
			    .trim();
			Class<?> actionClass = Class.forName(packageName + "."
			    + neMappedName.toLowerCase() + ".AlignNe");
			Class<?>[] args = new Class[] { AbstractNe.class };
			Constructor<?> constructor = actionClass.getConstructor(args);

			Object[] contextArgs = new Object[] { this };
			setInitializeNeObject((AbstractInitializeNe) constructor
			    .newInstance(contextArgs));

			if (neAoListener == null) {
				tl1Session.getAlarmController().addListener(this);
			}
			else {
				tl1Session.getAlarmController().addListener(neAoListener);
			}
			tl1Session.getAssociationController().addListener(this);
			return 0;
		}
		catch (Exception e) {

			log.error("Failed to connect to NE " + getNeInfo().toString(), e);
			// Send back the error code here
		}
		return -1;
	}

	private String toXMLDocument() {
		StringBuilder result = new StringBuilder(50);
		result.append("<node ");
		result.append("id=\"" + this.getNeInfo().getNeID() + "\" ");
		result.append("tid=\"" + this.getNeInfo().getNeName() + "\" ");
		result.append("ip=\""
		    + this.getNeInfo().getAddressInfo().getPrimaryIPAddress() + "\" ");
		result.append("port=\""
		    + this.getNeInfo().getAddressInfo().getPrimaryPort() + "\" ");
		result.append("status=\"" + this.getNeStatus() + "\" />");
		return result.toString();
	}

}
