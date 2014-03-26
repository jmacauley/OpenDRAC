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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAssociationEvent;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractInitializeNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.IPAddressInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.UserProfile;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AssociationEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class LocalNe extends AbstractNe {
  private final Logger log = LoggerFactory.getLogger(getClass());
	public LocalNe(String newUid, CryptedString newPasswd, String ipAddr,
	    int portNumber) {
		this.setIpAddress(ipAddr);
		this.setPortNumber(portNumber);
		setUid(newUid);
		setPasswd(newPasswd);
		UserProfile userInfo = new UserProfile(getUid(), getPasswd());
		IPAddressInfo ipAddrInfo = new IPAddressInfo(ipAddr, portNumber);
		setNeInfo(new NetworkElementInfo("00-00-00-00-00-00", "", ipAddrInfo,
		    userInfo));
		setDbChgEvent(new Tl1XmlAssociationEvent(this));
	}

	public LocalNe(String uid, CryptedString passwd, String ipAddr,
	    int portNumber, String terminalId) {
		this(uid, passwd, ipAddr, portNumber);
		this.setTerminalId(terminalId);
	}

	@Override
	public void changeNePassword(String newUid, CryptedString newPassword) {
		getNeInfo().getUserProfile().setUserID(newUid);
		getNeInfo().getUserProfile().setLoginPassword(newPassword);
		if (getState() == NeStatus.NE_NOT_AUTHENTICATED) {
			setState(NeStatus.NE_NOT_CONNECT);
			setPreviousNeState(NeStatus.NE_NOT_AUTHENTICATED);
			DiscoverNePool.INSTANCE.enqueueTask(this);
		}
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
		sendAssociationEvent(NeStatus.NE_NOT_PROVISION);
		if (DiscoverNePool.INSTANCE.getNe(
		    this.getIpAddress() + ":" + this.getPortNumber()) == null) {
			setState(NeStatus.NE_NOT_PROVISION);
		}
		else {
			setState(NeStatus.NE_NOT_CONNECT);
		}
	}

	private boolean connect() {
		try {
			String ourNeType = "LOCALNE";
			// String neSwVer = "N/A";

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
			return false;
		}
	}
}
