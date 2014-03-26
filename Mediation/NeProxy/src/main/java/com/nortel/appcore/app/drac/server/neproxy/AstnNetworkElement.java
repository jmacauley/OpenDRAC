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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAssociationEvent;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlEvent;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractInitializeNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.controller.NEAOListener;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.IPAddressInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.UserProfile;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AssociationEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.TL1SessionMgmt;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class AstnNetworkElement extends AbstractNe implements
    NEAOListener {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	public class EventHandlingTask implements Runnable {
		private final AbstractCommandlet aCommand;
		private final Map<String, Object> aParameter;

		public EventHandlingTask(AbstractCommandlet command,
		    Map<String, Object> parameters) {
			this.aCommand = command;
			this.aParameter = parameters;
		}

		@Override
		public void run() {
			try {
				
				if (aCommand.start()) {
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
				
			}
			catch (Exception e) {
				log.error("Failed to execute " + aCommand.getClass(), e);
			}
		}
	}

	public static final String COMMON_TL1WRAPPER = "Commontl1wrapper.jar";
	public TL1SessionMgmt mainSession;

	public List<NetworkElement> internalNeList = new ArrayList<NetworkElement>();
	// Instead of creating a new queue to sequentially handle incoming
	// autonomous message, just use Executors instead.
	private final Executor executor = Executors.newSingleThreadExecutor();

	public AstnNetworkElement(String uid, CryptedString passwd, String ipAddr,
	    int portNumber, String terminalId) {
		this.setIpAddress(ipAddr);
		this.setPortNumber(portNumber);
		this.setUid(uid);
		this.setPasswd(passwd);
		UserProfile userInfo = new UserProfile(uid, passwd);
		IPAddressInfo ipAddrInfo = new IPAddressInfo(ipAddr, portNumber);
		setNeInfo(new NetworkElementInfo("00-00-00-00-00-00", "", ipAddrInfo,
		    userInfo));
		setDbChgEvent(new Tl1XmlAssociationEvent(this));
		initMap();
		this.setTerminalId(terminalId);
	}

	@Override
	public void changeNePassword(String newUid, CryptedString newPassword) {
		setPasswd(newPassword);
		setUid(newUid);
		getNeInfo().getUserProfile().setUserID(newUid);
		getNeInfo().getUserProfile().setLoginPassword(newPassword);
		if (getState() == NeStatus.NE_NOT_AUTHENTICATED) {
			setState(NeStatus.NE_NOT_CONNECT);
			setPreviousNeState(NeStatus.NE_NOT_AUTHENTICATED);
			DiscoverNePool.INSTANCE.enqueueTask(this);
		}
	}

	public boolean connect() {
		try {
			// just to make sure
			/*
			 * neInfo.setAdapterName(COMMON_TL1WRAPPER); mainSession = new
			 * TL1SessionMgmt(neInfo); mainSession.manageNE();
			 * mainSession.establishTL1Session();
			 */
			String ourNeType = "ASTN";
			String neMappedName = this.getNeTypeMapping().get(ourNeType);
			if (neMappedName == null) {
				Exception e = new Exception("ERROR in mapping NE type - " + ourNeType);
				log.error("Error: ", e);
				return false;
			}

			this.setNeType(NeType.fromString(neMappedName));
			if (this.getNeType() == NeType.UNKNOWN) {
				Exception e = new Exception("ERROR in mapping NE type - "
				    + this.getNeType());
				log.error("Error: ", e);
				return false;
			}
			getNeInfo().setNeType(this.getNeType());

			String packageName = this.getNeTypeMapping().get(NePoxyDefinitionsParser.PGKNAME_ATTR)
			    .trim();
			Class<?> actionClass = Class.forName(packageName + "."
			    + neMappedName.toLowerCase() + ".AlignNe");
			Class<?>[] args = new Class[] { AbstractNe.class };
			Constructor<?> constructor = actionClass.getConstructor(args);

			Object[] context_args = new Object[] { this };
			this.setInitializeNeObject((AbstractInitializeNe) constructor
			    .newInstance(context_args));
			return true;
		}
		catch (Exception e) {

			log.error("Failed to connect to NE", e);
			// Send back the error code here
		}
		return false;
	}

	public NetworkElement getNe(String neId) {
		for (int i = 0; i < internalNeList.size(); i++) {
			NetworkElement temp = internalNeList.get(i);
			if (temp.getNeId().equalsIgnoreCase(neId)) {
				return temp;
			}
		}
		log.error("Failed to retrieve NE from ne: " + neId);
		return null;
	}

	public int getNeIndex(String neId) {
		
		for (int i = 0; i < internalNeList.size(); i++) {
			NetworkElement temp = internalNeList.get(i);
			temp.getState();
			
			if (temp.getNeId().equalsIgnoreCase(neId)) {
				return temp.getNeIndex();
			}
		}
		log.error("Failed to retrieve NE from ne: " + neId);
		return -1;
	}

	@Override
	public void nextState() {
		switch (getState()) {
		case NE_NOT_CONNECT:
			if (getPreviousNeState() != getState()) {
				setPreviousNeState(NeStatus.NE_NOT_CONNECT);
				DbUtility.INSTANCE.upDateNeStatus(this);
				sendAssociationEvent(this.getNeStatus());
			}
			if (this.mainSession != null) {
				mainSession = null;
			}
			if (connect()) {
				setState(NeStatus.NE_ASSOCIATED);
				setPreviousNeState(NeStatus.NE_ASSOCIATED);
				DbUtility.INSTANCE.upDateNeStatus(this);
				sendAssociationEvent(this.getNeStatus());
				/*
				 * clear any pending alarm and send an event else should raise alarm if
				 * not has been raised and send an event
				 */
			}
			DiscoverNePool.INSTANCE.enqueueTask(this);
			return;
		case NE_ASSOCIATED:
			setState(NeStatus.NE_INITIALIZING);
			if (getPreviousNeState() != getState()) {
				setPreviousNeState(NeStatus.NE_INITIALIZING);
				DbUtility.INSTANCE.upDateNeStatus(this);
				sendAssociationEvent(this.getNeStatus());
			}
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
					setPreviousNeState(NeStatus.NE_ASSOCIATED);
				}
				DiscoverNePool.INSTANCE.enqueueTask(this);
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
		try {
			String eventCode = event.getCommandCode().toString();
			
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
				log.debug("...use " + className + " for " + eventCode);

				AbstractCommandlet command = AbstractCommandlet.getCommandlet(
				    className, parameters);
				executor.execute(new EventHandlingTask(command, parameters));
			}
			else {
				
			}

		}
		catch (Exception e) {
			log.error("Failed handling event", e);
		}
	}

	public void receiveEvent(TL1AssociationEvent event) {
		// Current state is Aligned and the event is from Inservice to
		// OutofService
		
		if (getState() == NeStatus.NE_ALIGNED
		    && event.getCode() > TL1AssociationEvent.ASSOCIATION_UP) {
			this.setState(NeStatus.NE_NOT_CONNECT);
			DiscoverNePool.INSTANCE.enqueueTask(this);
		}
	}

	@Override
	public void setNeName(String name) {
		super.setNeName(name);
		getNeInfo().setNeName(name);
	}

	@Override
	public void terminate() {
		for (int i = 0; i < internalNeList.size(); i++) {
			AbstractNe aNe = internalNeList.get(i);
			try {
				DiscoverNePool.INSTANCE.removeNe(aNe);
				DbUtility.INSTANCE.deleteNe(aNe);
				aNe.terminate();
			}
			catch (Exception e) {
				log.error("Failed to delete innerNe " + aNe.getNeName(), e);
			}
		}
		setState(NeStatus.NE_NOT_CONNECT);
		DbUtility.INSTANCE.upDateNeStatus(this);
		sendAssociationEvent(NeStatus.NE_NOT_PROVISION);
	}
}
