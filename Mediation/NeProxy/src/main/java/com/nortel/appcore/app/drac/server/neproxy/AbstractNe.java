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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.EquipmentXml;
import com.nortel.appcore.app.drac.common.types.Holder;
import com.nortel.appcore.app.drac.common.types.InventoryXml;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAssociationEvent;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractInitializeNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public abstract class AbstractNe extends AbstractNetworkElement {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	private NetworkElementInfo neInfo;
	private static Integer myAtag = Integer.valueOf(0);
	private static final Object LOCK = new Object();

	// Allow non-default initialized sequence
	private AbstractInitializeNe initializeNeObject;

	private static int getAtag() {
		synchronized (LOCK) {
			myAtag = Integer.valueOf(myAtag.intValue() + 1);
			return myAtag.intValue();
		}
	}

	public EquipmentXml getCard(String shelf, String slot) {
		for (EquipmentXml aCard : getCards()) {
			if (aCard.getShelf().equals(shelf) && aCard.getSlot().equals(slot)) {
				return aCard;
			}
		}
		return null;
	}

	/**
	 * @return the initializeNeObject
	 */
	public AbstractInitializeNe getInitializeNeObject() {
		return initializeNeObject;
	}

	public InventoryXml getInventory(String shelf, String slot) {
		for (InventoryXml aInventory : getInventory()) {
			if (aInventory.getShelf().equals(shelf)
			    && aInventory.getSlot().equals(slot)) {
				return aInventory;
			}
		}
		return null;
	}

	/**
	 * @return the neEventHandlingDefinition
	 */
	public Map<NeType, Map<String, Map<String, String>>> getNeEventHandlingDefinition() {
		return NePoxyDefinitionsParser.INSTANCE.getNeEventHandlingDefinition();
		// return neEventHandlingDefinition;
	}

	public NetworkElementInfo getNeInfo() {
		return neInfo;
	}

	/**
	 * @return the neInitDefinition
	 */
	public Map<NeType, List<Holder>> getNeInitDefinition() {
		return NePoxyDefinitionsParser.INSTANCE.getNeInitDefinition();
	}

	/**
	 * @return the neTypeMapping
	 */
	public Map<String, String> getNeTypeMapping() {
		return NePoxyDefinitionsParser.INSTANCE.getNeTypeMapping();
	}

	public EquipmentXml getPort(String shelf, String slot, String port) {
		for (EquipmentXml aPort : getPorts()) {
			if (aPort.getShelf().equals(shelf) && aPort.getSlot().equals(slot)
			    && aPort.getPort().equals(port)) {
				return aPort;
			}
		}
		return null;
	}

	public void initialize() {
		sendAssociationEvent(NeStatus.NE_CREATED);
	}

	public boolean isBranchSite() {
		for (int i = 0; i < this.getInventory().size(); i++) {
			InventoryXml anInventory = this.getInventory().get(i);
			if (anInventory.getComponentAid().startsWith("WSS")) {
				return true;
			}
		}
		return false;
	}

	public void sendAssociationEvent(NeStatus status) {
		log.debug("sendAssocationEvent: Processing event for " + getIpAddress()
		    + ":" + getPortNumber() + " status:" + status + " current state: "
		    + getState() + " previous state: " + getPreviousNeState());

		Date date = new Date();
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
		String occrDate = simpledateformat.format(date);
		simpledateformat = new SimpleDateFormat("HH:mm:ss");
		String occrTime = simpledateformat.format(date);

		Tl1XmlAssociationEvent evt = getDbChgEvent();
		evt.setAssociationState(status.getStateString());
		evt.setOccurrentDate(occrDate);
		evt.setOccurrentTime(occrTime);
		this.upDateLocalInfo();
		evt.updateNeInfo(status.getStateString());
		evt.updateDescription(status.getSateDescription());
		evt.setEventId(this.getTerminalId() + "_" + getAtag());
		evt.setOwnerId(this.getTerminalId());
		evt.setEventTime();

		log.debug("Broadcasting assocation change event " + getIpAddress() + ":"
		    + getPortNumber());
		NeProxy.generateEvent(evt, ClientMessageXml.ASSOCIATION_EVENT_VALUE);

		try {
			String ip = getIpAddress();
			String port = Integer.toString(getPortNumber());
			String id = getNeId();
			String tid = getNeName();
			if ("00-00-00-00-00-00".equals(tid)) {
				tid = null;
			}

			switch (status) {
			case NE_INITIALIZING:
			  log.info("Changing NE status for ip {}, port {}, id {}, tid {} to status {}",
		        new Object[]{ip,port,id,tid,NeStatus.NE_INITIALIZING.name()});
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id,
				        LogKeyEnum.KEY_NE_STATUS_INITIALIZING, new String[] { tid, ip,
				            port }));
				break;
			case NE_ALIGNED:
			  log.info("Changing NE status for ip {}, port {}, id {}, tid {} to status {}",
            new Object[]{ip,port,id,tid,NeStatus.NE_ALIGNED.name()});
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id, LogKeyEnum.KEY_NE_STATUS_ALIGNED,
				        new String[] { tid, ip, port }));
				break;
			case NE_ASSOCIATED:
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id,
				        LogKeyEnum.KEY_NE_STATUS_ASSOCIATED, new String[] { tid, ip,
				            port }));
				break;
			case NE_CREATED:
			  log.info("Changing NE status for ip {}, port {}, id {}, tid {} to status {}",
            new Object[]{ip,port,id,tid,NeStatus.NE_CREATED.name()});
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id, LogKeyEnum.KEY_NE_STATUS_CREATED,
				        new String[] { tid, ip, port }));
				break;
			case NE_DELETED:
			  log.info("Changing NE status for ip {}, port {}, id {}, tid {} to status {}",
            new Object[]{ip,port,id,tid,NeStatus.NE_DELETED.name()});
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id, LogKeyEnum.KEY_NE_STATUS_DELETED,
				        new String[] { tid, ip, port }));
				break;
			case NE_NOT_AUTHENTICATED:
			  log.info("Changing NE status for ip {}, port {}, id {}, tid {} to status {}",
            new Object[]{ip,port,id,tid,NeStatus.NE_NOT_AUTHENTICATED.name()});
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id,
				        LogKeyEnum.KEY_NE_STATUS_NOT_AUTHENTICATED, new String[] { tid,
				            ip, port }));
				break;
			case NE_NOT_CONNECT:
			  log.info("Changing NE status for ip {}, port {}, id {}, tid {} to status {}",
            new Object[]{ip,port,id,tid,NeStatus.NE_NOT_CONNECT.name()});
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id,
				        LogKeyEnum.KEY_NE_STATUS_NOT_CONNECT, new String[] { tid, ip,
				            port }));
				break;
			case NE_NOT_PROVISION:
			  log.info("Changing NE status for ip {}, port {}, id {}, tid {} to status {}",
            new Object[]{ip,port,id,tid,NeStatus.NE_NOT_PROVISION.name()});
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id,
				        LogKeyEnum.KEY_NE_STATUS_NOT_PROVISION, new String[] { tid, ip,
				            port }));
				break;
			case NE_UNKNOWN:
			  log.info("Changing NE status for ip {}, port {}, id {}, tid {} to status {}",
            new Object[]{ip,port,id,tid,NeStatus.NE_UNKNOWN.name()});
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id, LogKeyEnum.KEY_NE_STATUS_UNKNOWN,
				        new String[] { tid, ip, port }));
				break;
			default:
			  log.info("Changing NE status for ip {}, port {}, id {}, tid {} to status {}",
            new Object[]{ip,port,id,tid,"NE UNKNOW STTAUS"});
				DbUtility.INSTANCE.generateLog(
				    new LogRecord(null, ip, null, id, LogKeyEnum.KEY_NE_STATUS_UNKNOWN,
				        new String[] { tid, ip, port }));
				break;
			}
		}
		catch (Exception e) {
			log.error("sendAssociationEvent failed ", e);
		}
	}

	/**
	 * @param initializeNeObject
	 *          the initializeNeObject to set
	 */
	public void setInitializeNeObject(AbstractInitializeNe initializeNeObject) {
		this.initializeNeObject = initializeNeObject;
	}

	@Override
	public void setNeId(String id) {
		super.setNeId(id);
		neInfo.setNeID(id);
	}

	public void setNeInfo(NetworkElementInfo neInfo) {
		this.neInfo = neInfo;
	}

	@Override
	public void setNeMode(NETWORK_ELEMENT_MODE mode) {
		super.setNeMode(mode);
		neInfo.setNeMode(mode);
	}

	public boolean slotIsL2SS(String shelf, String slot) {
		EquipmentXml aCard = getCard(shelf, slot);
		if (aCard != null) {
			String componentAid = aCard.getComponentAid();

			// Consider aid types of 'L2SS' and '20GL2SS'
			if (componentAid != null && componentAid.indexOf("L2SS") >= 0) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toDebugString() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.toDebugString());
		builder.append(" AbstractNe [initializeNeObject=");
		builder.append(initializeNeObject);
		builder.append(", neInfo=");
		builder.append(neInfo);
		builder.append("]");
		return builder.toString();
	}

	public void upDateLocalInfo() {
		this.setNeId(neInfo.getNeID());
		this.setNeName(neInfo.getNeName());
		this.setNeMode(neInfo.getNeMode());
		this.setNeType(neInfo.getNeType());
	}
}
