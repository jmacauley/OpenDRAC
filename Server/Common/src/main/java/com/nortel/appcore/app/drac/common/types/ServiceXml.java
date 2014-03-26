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

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbKeys.LightPathEdgeKeys;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

/**
 * Was at one point a complete JDOM representation of a service. With lots of
 * work and effort we've removed the XML and made this class concrete and
 * readable. JDOM is handy at times but has a huge performance overhead and was
 * not suitable in this scenario.
 * <p>
 * Created on Sep 15, 2005
 * 
 * @author nguyentd
 */
public final class ServiceXml implements Serializable {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * A service contains the main list of cross connections (these are
	 * provisioned into the network). If the service is 1+1 protected, a working
	 * and protection path will also be stored. The working and protection paths
	 * are for viewing only and may be missing attributes that would prevent them
	 * from being provisioned in to the real network.
	 */
	public enum XC_TYPE {
		MAIN, WORKING, PROTECTION;
	}

	private static final long serialVersionUID = 1L;
	// public static final String STARTTIME_ELEMENT = "startTime";
	// public static final String ENDTIME_ELEMENT = "endTime";
	// public static final String USER_ELEMENT = "user";
	// public static final String BANDWIDTH_ELEMENT = "rate";
	// public static final String PRIORITY_ELEMENT = "priority";
	// public static final String BILLINGGROUP_ELEMENT = "billingGroup";
	// public static final String SOURCEAID_ATTR = "sourceAid";
	// public static final String TARGETAID_ATTR = "targetAid";
	// public static final String SOURCENE_ATTR = "source";
	// public static final String TARGETNE_ATTR = "target";
	// public static final String RATE_ATTR = "rate";
	// public static final String SERVICESTATUS_ATTR = "serviceStatus";
	// public static final String VCAT_ROUTING_OPTION_ATTR =
	// "VCAT_ROUTING_OPTION";
	// public static final String ACTIVATIONTYPE_ATTR = "activationType";
	// public static final String CONTROLLERID_ATTR = "controllerId";
	// public static final String SCHEDULEID_ATTR = "scheduleId";
	// public static final String SCHEDULENAME_ATTR = "scheduleName";
	// public static final String MBS_ELEMENT = "mbs";
	// private static final String EMAIL_ELEMENT = "email";

	public static final String ID_ATTR = "id";
	public static final String MEDIATION_DATA_ID = "Mediation_Data";
	public static final String MEDIATION_DATA_RECORD_ID = "Mediation_Data_Record";

	private final State.SERVICE status;
	private final String id;
	private final String serviceId;
	private final String activationType;
	private final String controllerId;
	private final String scheduleId;
	private final String scheduleName;
	private final String vcatRouting;
	private long startTime;
	private long endTime;
	private final String user;
	private final UserGroupName billingGroup;
	private final String email;
	private final String priority;
	private final int mbs;
	private final String rate;
	// begin end time in string Date().tostring() format
	private final String begin; // computed from startTime
	private final String end;// computed from endTime
	private final String aEnd;
	private final String zEnd;
	private final List<CrossConnection> primaryList = new ArrayList<CrossConnection>();
	private final List<CrossConnection> workingList = new ArrayList<CrossConnection>();
	private final List<CrossConnection> protectionList = new ArrayList<CrossConnection>();

	/**
	 * Its very, very, expensive to serialize a JDOM element, we store the string
	 * version and go to JDOM when necessary. We keep a cached copy for
	 * performance.
	 */
	private String xmlUserData = XmlUtility.rootNodeToString(new Element(
	    "UserData"));
	private transient Element cachedUserData;

	public ServiceXml(State.SERVICE sStatus, String sId, String sServiceId,
	    String sActivationType, String sController, String sScheduleId,
	    String sScheduleName, String sVcat, long sStartTime, long sEndTime,
	    String sUser, String sBillingGroup, String sEmail, String sPriority,
	    int sMbs, String sRate, String sAend, String sZend) {
		status = sStatus == null ? State.SERVICE.CONFIRMATION_PENDING : sStatus;
		id = sId;
		serviceId = sServiceId;
		activationType = sActivationType;
		controllerId = sController;
		scheduleId = sScheduleId;
		scheduleName = sScheduleName;
		vcatRouting = sVcat;
		startTime = sStartTime;
		endTime = sEndTime;
		user = sUser == null ? "N/A" : sUser;
		billingGroup = sBillingGroup == null ? UserGroupName.USER_GROUP_NOTAPPL
		    : new UserGroupName(sBillingGroup);
		email = sEmail == null || "N/A".equals(sEmail) ? null : sEmail;
		priority = sPriority == null ? "0" : (String) sPriority;
		mbs = sMbs;
		rate = sRate;
		aEnd = sAend;
		zEnd = sZend;

		Calendar aCalendar = Calendar.getInstance();
		aCalendar.setTimeInMillis(startTime);
		begin = aCalendar.getTime().toString();
		aCalendar.setTimeInMillis(endTime);
		end = aCalendar.getTime().toString();
	}

	public void addPath(XC_TYPE type, List<CrossConnection> cList) {
		for (CrossConnection c : cList) {
			if (c != null) {
				setConnectionToPath(c, type);
			}
		}
	}

	public String getActivationType() {
		return activationType;
	}

	public String getAend() {
		return aEnd;
	}

	public String getBandwidth() {
		return rate;
	}

	public String getBeginDateTime() {
		return begin;
	}

	public UserGroupName getBillingGroup() {
		return billingGroup;
	}

	public String getCallId() {
		return id;
	}

	public String getControllerId() {
		return controllerId;
	}

	public CrossConnection getCrossConnectionInfo(int pathIndex) {
		return addSpecialSauce(primaryList.get(pathIndex));
	}

	public List<CrossConnection> getCrossConnections() {
		return addSpecialSauce(primaryList);
	}

	public String getEmail() {
		return email;
	}

	public CrossConnection getEndConnection(boolean wantPathAEnd) {
		for (CrossConnection aConnection : primaryList) {
			if (wantPathAEnd) {
				String a = getAend();

				if (a.equals(aConnection.getSourceNeId())
				    || a.equals(aConnection.getTargetNeId())) {
					return addSpecialSauce(aConnection);
				}
			}
			else {
				String z = getZend();
				if (z.equals(aConnection.getSourceNeId())
				    || z.equals(aConnection.getTargetNeId())) {
					return addSpecialSauce(aConnection);
				}
			}
		}
		return null;
	}

	public String getEndDateTime() {
		return end;
	}

	public long getEndTime() {
		return endTime;
	}

	public int getMbs() {
		return mbs;
	}

	/**
	 * Return the connections grouped by NE. There will be multiple XCs per NE in
	 * the case of VCAT.
	 */
	public Map<String, List<CrossConnection>> getNeXcListMap() {
		Map<String, List<CrossConnection>> neXcListMap = new HashMap<String, List<CrossConnection>>();

		List<CrossConnection> listXcons = getCrossConnections();

		for (CrossConnection xcon : listXcons) {
			String neid = xcon.getSourceNeId();

			List<CrossConnection> neXcons = neXcListMap.get(neid);

			if (neXcons == null) {
				neXcons = new ArrayList<CrossConnection>();
				neXcListMap.put(neid, neXcons);
			}

			neXcons.add(xcon);
		}

		return neXcListMap;
	}

	public String getPriority() {
		return priority;
	}

	public List<CrossConnection> getProtectionPath() {
		return new ArrayList<CrossConnection>(protectionList);
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public String getServiceId() {
		return serviceId;
	}

	public long getStartTime() {
		return startTime;
	}

	public State.SERVICE getStatus() {
		return status;
	}

	public String getUser() {
		return user;
	}

	public String getVcatRoutingOption() {
		return vcatRouting;
	}

	public List<CrossConnection> getWorkingPath() {
		return new ArrayList<CrossConnection>(workingList);
	}

	public String getXMLUserData() {
		return this.xmlUserData;
	}

	public String getZend() {
		return zEnd;
	}

	public int numberOfConnectionsInPath() {
		return primaryList.size();
	}

	/**
	 * We store edges (crossConnections) via DbLightPathEdge. An edge is not quite
	 * a crossconnection, turn edges into crossconnections here.
	 */
	public void putEdges(List<Map<String, String>> edges) throws Exception {
		for (Map<String, String> data : edges) {
			Map<String, String> xc = new HashMap<String, String>();

			xc.put(CrossConnection.SOURCE_NEID,
			    data.get(LightPathEdgeKeys.EDGE_SOURCE));
			xc.put(CrossConnection.SOURCE_PORT_AID,
			    data.get(LightPathEdgeKeys.EDGE_SOURCEAID));
			xc.put(CrossConnection.SOURCE_CHANNEL,
			    data.get(LightPathEdgeKeys.EDGE_SOURCECHANNEL));

			xc.put(CrossConnection.TARGET_NEID,
			    data.get(LightPathEdgeKeys.EDGE_TARGET));
			xc.put(CrossConnection.TARGET_PORT_AID,
			    data.get(LightPathEdgeKeys.EDGE_TARGETAID));
			xc.put(CrossConnection.TARGET_CHANNEL,
			    data.get(LightPathEdgeKeys.EDGE_TARGETCHANNEL));

			xc.put(CrossConnection.RATE, data.get(LightPathEdgeKeys.EDGE_RATE));

			// The edge only stores sts rate
			xc.put(CrossConnection.RATE_IN_MBS, Integer.toString(getMbs()));

			// Hard coded value of DbLightPath.LP_CALLID, we can't import it here.
			xc.put(CrossConnection.CKTID,
			    "DRAC-" + data.get(DbKeys.LightPathCols.LP_CALLID));

			if (data.get(LightPathEdgeKeys.EDGE_CCT) != null) {
				xc.put(CrossConnection.CCT_TYPE, data.get(LightPathEdgeKeys.EDGE_CCT));
			}

			if (data.get(LightPathEdgeKeys.EDGE_SWMATEAID) != null) {
				xc.put(CrossConnection.SWMATE_XC_AID,
				    data.get(LightPathEdgeKeys.EDGE_SWMATEAID));
			}

			if (data.get(LightPathEdgeKeys.EDGE_VLANID) != null) {
				xc.put(CrossConnection.VLANID, data.get(LightPathEdgeKeys.EDGE_VLANID));
			}

			xc.put(CrossConnection.CALLID, getCallId());
			xc.put(CrossConnection.VCATROUTINGOPTION, getVcatRoutingOption());

			CrossConnection c = new CrossConnection(xc);
			primaryList.add(c);
		}
	}

	public synchronized void setXMLUserData(String xmlUserStoreageData) {
		if (xmlUserStoreageData != null) {

			// if we are going to set it, clear the old cached copy.
			cachedUserData = null;
			xmlUserData = xmlUserStoreageData;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceXml [id=");
		builder.append(id);
		builder.append(", scheduleId=");
		builder.append(scheduleId);
		builder.append(", serviceId=");
		builder.append(serviceId);
		builder.append(", status=");
		builder.append(status);
		builder.append(", activationType=");
		builder.append(activationType);
		builder.append(", controllerId=");
		builder.append(controllerId);
		builder.append(", scheduleName=");
		builder.append(scheduleName);
		builder.append(", vcatRouting=");
		builder.append(vcatRouting);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", user=");
		builder.append(user);
		builder.append(", billingGroup=");
		builder.append(billingGroup);
		builder.append(", email=");
		builder.append(email);
		builder.append(", priority=");
		builder.append(priority);
		builder.append(", mbs=");
		builder.append(mbs);
		builder.append(", rate=");
		builder.append(rate);
		builder.append(", begin=");
		builder.append(begin);
		builder.append(", end=");
		builder.append(end);
		builder.append(", aEnd=");
		builder.append(aEnd);
		builder.append(", zEnd=");
		builder.append(zEnd);
		builder.append(", primaryList=");
		builder.append(primaryList);
		builder.append(", workingList=");
		builder.append(workingList);
		builder.append(", protectionList=");
		builder.append(protectionList);
		builder.append(", xmlUserData=");
		builder.append(xmlUserData);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Better explain what this does! Anyone?
	 */
	@SuppressWarnings("unchecked")
  private CrossConnection addSpecialSauce(CrossConnection c) {

		/*
		 * Cross-reference the mediation data stored in the xmlUserData (on the
		 * light path / serviceXML) with the CrossConnection objects. Cross-ref is
		 * done based on Neid+CallId.
		 */

		/*
		 * One mediation data record per NE per callId is assumed. In the case of
		 * multiple XCs to the same NE for the same callId (i.e. vcat), the record
		 * will be the same (since we're creating just the one WAN). Same note in
		 * SchedulingThread::sendCommand
		 */

		if (xmlUserData != null
		    && ServiceXml.MEDIATION_DATA_ID.equals(getCachedUserData().getName())) {
			for (Element mediationDataRecord : (List<Element>) getCachedUserData()
			    .getChildren()) {
				if (ServiceXml.MEDIATION_DATA_RECORD_ID.equals(mediationDataRecord
				    .getName())) {
					String mediationDataNeId = mediationDataRecord
					    .getAttributeValue(CrossConnection.SOURCE_NEID);
					String mediationDataCallId = mediationDataRecord
					    .getAttributeValue(CrossConnection.CALLID);

					if (c.getSourceNeId().equals(mediationDataNeId)
					    && getCallId().equals(mediationDataCallId)) {
						String data = XmlUtility.rootNodeToString(mediationDataRecord);
						log.debug("ServiceXml adding the special sauce to cross connect: sauce "
						    + data + " crossConnect: " + c);
						c.setMediationData(data);
						break;
					}
				}
			}
		}
		return c;
	}

	private List<CrossConnection> addSpecialSauce(List<CrossConnection> c) {
		List<CrossConnection> result = new ArrayList<CrossConnection>(c.size());
		for (CrossConnection xc : c) {
			result.add(addSpecialSauce(xc));
		}
		return result;
	}

	private synchronized Element getCachedUserData() {
		if (cachedUserData != null) {
			return cachedUserData;
		}

		try {
			cachedUserData = XmlUtility.createDocumentRoot(xmlUserData);
		}
		catch (Exception e) {
			log.error("failed to convert to Jdom " + xmlUserData, e);
		}
		return cachedUserData;
	}

	// private CrossConnection elementToCrossConnection(Element
	// xmlCrossConnection)
	// {
	// Map<String, String> xcMap = new HashMap<String, String>();
	//
	// // for key'ing into the additional mediation data
	// String neId = xmlCrossConnection.getAttributeValue(SOURCENE_ATTR);
	// String callid = getCallId();
	//
	// // the routing code sets the connections via 'setConnectionToPath'. It's
	// version of connection
	// // SOURCEAID_ATTR is that which does not contain the channel, hence, it is
	// a SOURCE_PORT_AID.
	// xcMap.put(CrossConnection.SOURCE_PORT_AID,
	// xmlCrossConnection.getAttributeValue(SOURCEAID_ATTR));
	// xcMap.put(CrossConnection.TARGET_PORT_AID,
	// xmlCrossConnection.getAttributeValue(TARGETAID_ATTR));
	// xcMap.put(CrossConnection.SOURCE_NEID, neId);
	// xcMap.put(CrossConnection.TARGET_NEID,
	// xmlCrossConnection.getAttributeValue(TARGETNE_ATTR));
	// xcMap.put(CrossConnection.RATE,
	// xmlCrossConnection.getAttributeValue(RATE_ATTR));
	// /* The actual cross connect only stores the sts rates, copy the mbs rate
	// back in */
	// xcMap.put(CrossConnection.RATE_IN_MBS, Integer.toString(getMbs()));
	//
	// xcMap.put(CrossConnection.CKTID,
	// xmlCrossConnection.getAttributeValue(CKTID_ATTR));
	// xcMap.put(CrossConnection.SOURCE_CHANNEL,
	// xmlCrossConnection.getAttributeValue(SOURCECHANNEL_ATTR));
	// xcMap.put(CrossConnection.TARGET_CHANNEL,
	// xmlCrossConnection.getAttributeValue(TARGETCHANNEL_ATTR));
	// xcMap.put(CrossConnection.BLSR_AEND,
	// xmlCrossConnection.getAttributeValue(ClientMessageXML.AEND_KEY));
	// xcMap.put(CrossConnection.BLSR_ZEND,
	// xmlCrossConnection.getAttributeValue(ClientMessageXML.ZEND_KEY));
	// xcMap.put(CrossConnection.REMOTEMAC,
	// xmlCrossConnection.getAttributeValue(ClientMessageXML.REMOTEMAC_KEY));
	// xcMap.put(CrossConnection.CCT_TYPE,
	// xmlCrossConnection.getAttributeValue(CCT_ATTR));
	// if (xmlCrossConnection.getAttributeValue(SWMATEAID_ATTR) != null)
	// {
	// xcMap.put(CrossConnection.SWMATE_XC_AID,
	// xmlCrossConnection.getAttributeValue(SWMATEAID_ATTR));
	// }
	// xcMap.put(CrossConnection.VLANID,
	// xmlCrossConnection.getAttributeValue(VLANID_ATTR));
	// // see "GG 2009-02-03"
	// xcMap.put(CrossConnection.CALLID, callid);
	// xcMap.put(CrossConnection.VCATROUTINGOPTION, getVcatRoutingOption());
	//
	// CrossConnection xcon = new CrossConnection(xcMap);
	//
	// // Cross-reference the mediation data stored in the xmlUserData (on the
	// light path / serviceXML)
	// // with the CrossConnection objects. Cross-ref is done based on
	// Neid+CallId.
	//
	// // One mediation data record per NE per callId is assumed. In the case of
	// multiple XCs to the
	// // same NE for the same callId (i.e. vcat), the record will be the same
	// (since we're creating
	// // just the one WAN). Same note in SchedulingThread::sendCommand
	// if (xmlUserData != null &&
	// ServiceXml.MEDIATION_DATA_ID.equals(xmlUserData.getName()))
	// {
	// List<Element> mediationDataRecordList = xmlUserData.getChildren();
	// for (Element mediationDataRecord : mediationDataRecordList)
	// {
	// if
	// (ServiceXml.MEDIATION_DATA_RECORD_ID.equals(mediationDataRecord.getName()))
	// {
	// String mediationDataNeId =
	// mediationDataRecord.getAttributeValue(CrossConnection.SOURCE_NEID);
	// String mediationDataCallId =
	// mediationDataRecord.getAttributeValue(CrossConnection.CALLID);
	//
	// if (neId.equals(mediationDataNeId) && callid.equals(mediationDataCallId))
	// {
	// xcon.setMediationData(XmlUtility.rootNodeToString(mediationDataRecord));
	// break;
	// }
	// }
	// }
	// }
	//
	// return xcon;
	// }

	private void setConnectionToPath(CrossConnection xconn, XC_TYPE type) {
		switch (type) {
		case MAIN:
			primaryList.add(xconn);
			break;
		case WORKING:
			workingList.add(xconn);
			break;
		case PROTECTION:
			protectionList.add(xconn);
			break;
		default:
			log.error("unsupported type :" + type);
			break;
		}
	}

	public void setStartTime(long startTime) {
    	this.startTime = startTime;
    }

	public void setEndTime(long endTime) {
    	this.endTime = endTime;
    }
}
