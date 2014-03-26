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

package com.nortel.appcore.app.drac.database.dracdb;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.CallIdType;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType.RecurrenceFreq;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.common.types.State.CALL;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.DbOpWithResultsAdapter;
import com.nortel.appcore.app.drac.database.DbOperationsManager;
import com.nortel.appcore.app.drac.database.helper.DbUtilityCommonUtility;

/**
 * @author pitman
 */
public enum DbSchedule {
	INSTANCE;
	private static final Logger log = LoggerFactory.getLogger(DbSchedule.class);
	public static class AuditScheduleStatusHolder {
		private final String scheduleId;
		private final SCHEDULE scheduleStatus;
		private final long scheduleEndtime;
		private final List<List<String>> serviceRecords;

		/**
		 * Holder class that holds info about a schedule and its services that are
		 * in progress (CONFIRMATION_PENDING,EXECUTION_INPROGRESS,
		 * EXECUTION_PENDING).. ServiceRecords is a String array that holds 2 items,
		 * the service id and service status.
		 */
		public AuditScheduleStatusHolder(String scheduleIdentifier,
		    String statusOfSchedule, long endTimeFromSchedule,
		    List<List<String>> serviceRecord) {
			scheduleId = scheduleIdentifier;
			scheduleStatus = SCHEDULE.valueOf(statusOfSchedule);
			scheduleEndtime = endTimeFromSchedule;
			serviceRecords = serviceRecord;
		}

		public long getScheduleEndtime() {
			return scheduleEndtime;
		}

		public String getScheduleId() {
			return scheduleId;
		}

		public SCHEDULE getScheduleStatus() {
			return scheduleStatus;
		}

		public List<List<String>> getServiceRecords() {
			return serviceRecords;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("AuditScheduleStatusHolder [scheduleId=");
			builder.append(scheduleId);
			builder.append(", scheduleStatus=");
			builder.append(scheduleStatus);
			builder.append(", scheduleEndtime=");
			builder.append(scheduleEndtime);
			builder.append(", serviceRecords=");
			builder.append(serviceRecords);
			builder.append("]");
			return builder.toString();
		}

	}

	/**
	 * @deprecated
	 * @TODO: Moved ScheduleXML into this class to hide it, ideally we'll get rid
	 *        of this class at just moves data to/from XML formats !
	 */
	@Deprecated
	private static final class ScheduleXML {
		public static final String ID_ATTR = "id";
		public static final String ACTIVATION_TYPE_ATT = ClientMessageXml.ACTIVATION_TYPE_ATT;
		public static final String STATUS_ATTR = "status";
		public static final String USER_ELEMENT = "user";
		public static final String PATH_ELEMENT = "path";
		public static final String SOURCEENDPOINT_ELEMENT = "sourceEndPoint";
		public static final String TARGETENDPOINT_ELEMENT = "targetEndPoint";
		public static final String SOURCETNA_ATTR = "sourceTna";
		public static final String TARGETTNA_ATTR = "targetTna";

		public static final String USERID_ELEMENT = "userId";
		public static final String BILLINGGROUP_ELEMENT = "billingGroup";
		public static final String SOURCE_ENDPOINT_USERGROUP_ELEMENT = "sourceEndpointUserGroup";
		public static final String TARGET_ENDPOINT_USERGROUP_ELEMENT = "targetEndpointUserGroup";
		public static final String SOURCE_ENDPOINT_RESOURCEGROUP_ELEMENT = "sourceEndpointResourceGroup";
		public static final String TARGET_ENDPOINT_RESOURCEGROUP_ELEMENT = "targetEndpointResourceGroup";

		public static final String SOURCENE_ELEMENT = "source";
		public static final String TARGETNE_ELEMENT = "target";
		public static final String RATE_ELEMENT = "rate";
		public static final String SRLG_ELEMENT = "srlg";
		public static final String COST_ELEMENT = "cost";
		public static final String METRIC_ELEMENT = "metric";

		public static final String HOP_ELEMENT = "hop";
		public static final String SHARED_RISK_SERVICE_GROUP_ELEMENT = "sharedRiskServiceGroup";

		public static final String CHANNEL_ATTR = "channel";

		private static final String ROOT_ELEMENT = "schedule";
		private static final String NAME_ATTR = "name";
		private static final String STARTTIME_ELEMENT = "startTime";
		private static final String ENDTIME_ELEMENT = "endTime";
		private static final String DURATION_ELEMENT = "duration";
		private static final String RECURRENCE_ELEMENT = "recurrence";
		private static final String TYPE_ELEMENT = "type";
		private static final String DAY_ELEMENT = "day";
		private static final String WEEKDAY_ELEMENT = "weekDay";
		private static final String MONTH_ELEMENT = "month";

		private final Element root;

		public ScheduleXML(Schedule aSchedule) {
			root = new Element(ROOT_ELEMENT);
			try {
				root.setAttribute(ID_ATTR, aSchedule.getId());
				root.setAttribute(ACTIVATION_TYPE_ATT, aSchedule.getActivationType()
				    .name());
				root.setAttribute(STATUS_ATTR, aSchedule.getStatus().name());
				root.setAttribute(NAME_ATTR, aSchedule.getName());
				root.addContent(new Element(STARTTIME_ELEMENT).setText(Long
				    .toString(aSchedule.getStartTime())));
				root.addContent(new Element(ENDTIME_ELEMENT).setText(Long
				    .toString(aSchedule.getEndTime())));
				root.addContent(new Element(DURATION_ELEMENT).setText(Long
				    .toString(aSchedule.getDurationLong())));

				Element user = new Element(USER_ELEMENT);
				if (aSchedule.getUserInfo() == null) {
					user.addContent(new Element(USERID_ELEMENT).setText("N/A"));
				}
				else {
					user.addContent(new Element(USERID_ELEMENT).setText(aSchedule
					    .getUserInfo().getUserId()));

					if (aSchedule.getUserInfo().getSourceEndpointUserGroup() == null) {
						user.addContent(new Element(SOURCE_ENDPOINT_USERGROUP_ELEMENT));
					}
					else {
						user.addContent(new Element(SOURCE_ENDPOINT_USERGROUP_ELEMENT)
						    .setText(aSchedule.getUserInfo().getSourceEndpointUserGroup()));
					}

					if (aSchedule.getUserInfo().getTargetEndpointUserGroup() == null) {
						user.addContent(new Element(TARGET_ENDPOINT_USERGROUP_ELEMENT));
					}
					else {
						user.addContent(new Element(TARGET_ENDPOINT_USERGROUP_ELEMENT)
						    .setText(aSchedule.getUserInfo().getTargetEndpointUserGroup()));
					}

					if (aSchedule.getUserInfo().getBillingGroup() == null) {
						user.addContent(new Element(BILLINGGROUP_ELEMENT));
					}
					else {
						user.addContent(new Element(BILLINGGROUP_ELEMENT).setText(aSchedule
						    .getUserInfo().getBillingGroup().toString()));
					}

					if (aSchedule.getUserInfo().getSourceEndpointResourceGroup() == null) {
						user.addContent(new Element(SOURCE_ENDPOINT_RESOURCEGROUP_ELEMENT));
					}
					else {
						user.addContent(new Element(SOURCE_ENDPOINT_RESOURCEGROUP_ELEMENT)
						    .setText(aSchedule.getUserInfo()
						        .getSourceEndpointResourceGroup()));
					}

					if (aSchedule.getUserInfo().getTargetEndpointResourceGroup() == null) {
						user.addContent(new Element(TARGET_ENDPOINT_RESOURCEGROUP_ELEMENT));
					}
					else {
						user.addContent(new Element(TARGET_ENDPOINT_RESOURCEGROUP_ELEMENT)
						    .setText(aSchedule.getUserInfo()
						        .getTargetEndpointResourceGroup()));
					}
				}
				root.addContent(user);

				if (aSchedule.getRecurrence() != null) {
					Element recurrence = new Element(RECURRENCE_ELEMENT);
					recurrence.addContent(new Element(TYPE_ELEMENT).setText(aSchedule
					    .getRecurrence().getType().toString()));
					try {
						recurrence.addContent(new Element(DAY_ELEMENT).setText(Integer
						    .toString(aSchedule.getRecurrence().getDay())));
						recurrence.addContent(new Element(MONTH_ELEMENT).setText(Integer
						    .toString(aSchedule.getRecurrence().getMonth())));
						recurrence
						    .addContent(new Element(WEEKDAY_ELEMENT)
						        .setText(arrayToString(aSchedule.getRecurrence()
						            .getWeekDay())));
					}
					catch (Exception re) {
						log.error("Expect correct recurrence data", re);
					}
					root.addContent(recurrence);
				}
				Element path = new Element(PATH_ELEMENT);
				if (aSchedule.getPath() != null) {
					path.addContent(new Element(SOURCENE_ELEMENT).setText(aSchedule
					    .getPath().getSource()));
					path.addContent(new Element(TARGETNE_ELEMENT).setText(aSchedule
					    .getPath().getTarget()));
					path.addContent(new Element(RATE_ELEMENT).setText(Integer
					    .toString(aSchedule.getPath().getRate())));

					path.addContent(new Element(SRLG_ELEMENT).setText(aSchedule.getPath()
					    .getSrlg()));
					path.addContent(new Element(COST_ELEMENT).setText(Integer
					    .toString(aSchedule.getPath().getCost())));
					path.addContent(new Element(METRIC_ELEMENT).setText(Integer
					    .toString(aSchedule.getPath().getMetric())));
					path.addContent(new Element(HOP_ELEMENT).setText(Integer
					    .toString(aSchedule.getPath().getHop())));

					path.addContent(new Element(SHARED_RISK_SERVICE_GROUP_ELEMENT)
					    .setText(aSchedule.getPath().getSharedRiskServiceGroup()));

					path.addContent(new Element(ClientMessageXml.PROTECTION_TYPE_ATTR)
					    .setText(aSchedule.getPath().getProtectionType().name()));

					Element srcEndPoint = new Element(SOURCEENDPOINT_ELEMENT);
					srcEndPoint.setAttribute(ID_ATTR, aSchedule.getPath()
					    .getSourceEndPoint().getId());
					srcEndPoint.setAttribute(
					    SOURCETNA_ATTR,
					    aSchedule.getPath().getSourceEndPoint().getAttributes()
					        .get(FacilityConstants.TNA_ATTR));
					srcEndPoint.setAttribute(
					    CHANNEL_ATTR,
					    String.valueOf(aSchedule.getPath().getSourceEndPoint()
					        .getChannelNumber()));
					Element dstEndPoint = new Element(TARGETENDPOINT_ELEMENT);
					dstEndPoint.setAttribute(ID_ATTR, aSchedule.getPath()
					    .getTargetEndPoint().getId());
					dstEndPoint.setAttribute(
					    TARGETTNA_ATTR,
					    aSchedule.getPath().getTargetEndPoint().getAttributes()
					        .get(FacilityConstants.TNA_ATTR));
					dstEndPoint.setAttribute(
					    CHANNEL_ATTR,
					    String.valueOf(aSchedule.getPath().getTargetEndPoint()
					        .getChannelNumber()));

					path.addContent(srcEndPoint);
					path.addContent(dstEndPoint);
				}
				root.addContent(path);
			}
			catch (Exception e) {
				log.error("Failed to initialize ScheduleXML ", e);
			}
		}

		public String rootNodeToString() {
			XMLOutputter outXml = new XMLOutputter(Format.getCompactFormat());
			return outXml.outputString(root);
		}

	}

	/**
	 * @deprecated
	 * @TODO: Moved ServXML into this class to hide it, ideally we'll get rid of
	 *        this class at just moves data to/from XML formats !
	 */
	@Deprecated
	private static final class ServXML {
		private static final String MBS_ELEMENT = "mbs";
		private static final String SOURCEID_ATTR = "sourceId";
		private static final String TARGETID_ATTR = "targetId";
		private static final String SCHEDULEID_ATTR = "scheduleId";
		private static final String SCHEDULENAME_ATTR = "scheduleName";
		private static final String SCHEDULE_STATUS_ATTR = "scheduleStatus";
		private static final String START_TIME_ATTR = "startTime";
		private static final String END_TIME_ATTR = "endTime";
		private static final String STATUS_ATTR = "status";
		private static final String ID_ATTR = "id";
		private static final String PATH_ELEMENT = "path";
		private static final String NAME_ATTR = "name";
		private static final String ACTUALOFFSET_ATTR = "actualOffset";
		private static final String CONTROLLERID_ATTR = "controllerId";
		private static final String SERVICE_ELEMENT = "service";
		private static final String STARTTIME_ATTR = "startTime";
		private static final String ENDTIME_ATTR = "endTime";
		private static final String CALL_ELEMENT = "call";
		private static final String PROTECTION_ELEMENT = "protection";

		private final Element root;

		public ServXML(String xmlString) throws Exception {
			ByteArrayInputStream data = new ByteArrayInputStream(xmlString.getBytes());
			SAXBuilder builder = new SAXBuilder();
			Document aDoc = builder.build(data);
			root = aDoc.getRootElement();
		}

		public DracService rootNodeToObject() {
			DracService aService = new DracService(
			    Schedule.ACTIVATION_TYPE.valueOf(root
			        .getAttributeValue(ScheduleXML.ACTIVATION_TYPE_ATT)));
			aService.setScheduleId(root.getAttributeValue(SCHEDULEID_ATTR));
			aService.setScheduleName(root.getAttributeValue(SCHEDULENAME_ATTR));
			aService.setScheduleStatus(SCHEDULE.valueOf(root
			    .getAttributeValue(SCHEDULE_STATUS_ATTR)));

			aService.setUserInfo(parseUserInfo(root
			    .getChild(ScheduleXML.USER_ELEMENT)));

			Element serviceNode = root.getChild(SERVICE_ELEMENT);
			aService.setId(serviceNode.getAttributeValue(ScheduleXML.ID_ATTR));
			aService.setStatus(SERVICE.valueOf(serviceNode
			    .getAttributeValue(ScheduleXML.STATUS_ATTR)));
			aService.setOffset(Integer.parseInt(serviceNode
			    .getAttributeValue(ACTUALOFFSET_ATTR)));
			aService.setStartTime(Long.parseLong(serviceNode
			    .getAttributeValue(STARTTIME_ATTR)));
			aService.setEndTime(Long.parseLong(serviceNode
			    .getAttributeValue(ENDTIME_ATTR)));

			// Build the new PathType with only two value: sourceTNA and
			// targetTNA. These values
			// are taken from the parent schedule
			PathType aPath = new PathType();
			aPath.getSourceEndPoint().setId(root.getAttributeValue(SOURCEID_ATTR));
			aPath.getTargetEndPoint().setId(root.getAttributeValue(TARGETID_ATTR));
			aService.setPath(aPath);

			// with path set, NOW can set the service rate
			String rateMbs = serviceNode.getAttributeValue(MBS_ELEMENT);
			aService.setRate(Integer.parseInt(rateMbs));

			ArrayList<CallIdType> callList = new ArrayList<CallIdType>();

			Iterator<Element> ir = serviceNode.getChildren().iterator();
			while (ir.hasNext()) {
				Element aChildElement = ir.next();
				if (aChildElement.getName().equals(CALL_ELEMENT)) {
					CallIdType aCall = new CallIdType(
					    aChildElement.getAttributeValue(ScheduleXML.ID_ATTR));
					aCall.setControllerId(aChildElement
					    .getAttributeValue(CONTROLLERID_ATTR));
					String callStatus = DbUtilityCommonUtility.INSTANCE
					    .queryCallStatus(aCall.getId());

					try {
						int tempStatus = Integer.parseInt(callStatus);
						CALL callState = CALL.values()[tempStatus];
						aCall.setStatus(callState);
					}
					catch (Exception e) {
						log.warn(
						    "Error parsing call status as number, status will default to execution_pending",
						    e);
					}
					callList.add(aCall);
				}
				else if (aChildElement.getName().equals(ScheduleXML.PATH_ELEMENT)) {
					String srcTna = aChildElement.getChild(
					    ScheduleXML.SOURCEENDPOINT_ELEMENT).getAttributeValue(
					    ScheduleXML.SOURCETNA_ATTR);
					String targetTna = aChildElement.getChild(
					    ScheduleXML.TARGETENDPOINT_ELEMENT).getAttributeValue(
					    ScheduleXML.TARGETTNA_ATTR);

					aPath.getSourceEndPoint().getAttributes()
					    .put(FacilityConstants.TNA_ATTR, srcTna);
					aPath.getTargetEndPoint().getAttributes()
					    .put(FacilityConstants.TNA_ATTR, targetTna);
				}
			}
			aService.setCall(callList.toArray(new CallIdType[callList.size()]));

			return aService;
		}

		/**
		 * Parse a list of services for use in calculations concerning availability
		 * or utilization
		 */
		public List<DracService> toServiceUsageList() {
			List<Element> serviceList = root.getChildren();
			List<DracService> services = new ArrayList<DracService>();
			if (serviceList != null) {
				Iterator<Element> it = serviceList.iterator();
				while (it.hasNext()) {
					Element serviceElement = it.next();
					if (serviceElement != null) {
						DracService aService = new DracService();
						aService.setScheduleName(serviceElement
						    .getAttributeValue(NAME_ATTR));
						aService.setScheduleId(serviceElement
						    .getAttributeValue(SCHEDULEID_ATTR));
						aService.setStartTime(Long.parseLong(serviceElement
						    .getAttributeValue(START_TIME_ATTR)));
						aService.setEndTime(Long.parseLong(serviceElement
						    .getAttributeValue(END_TIME_ATTR)));
						aService.setStatus(SERVICE.valueOf(serviceElement
						    .getAttributeValue(STATUS_ATTR)));
						aService.setId(serviceElement.getAttributeValue(ID_ATTR));
						Element pathElem = serviceElement.getChild(PATH_ELEMENT);
						if (pathElem != null) {
							aService.setPath(parsePathElement(pathElem));
						}
						// if this service hasn't been cancelled, it is taking
						// up bandwidth
						// and needs to be considered in the calculation
						if (aService.getStatus() != SERVICE.ACTIVATION_CANCELLED
						    && aService.getStatus() != SERVICE.CONFIRMATION_CANCELLED
						    && aService.getStatus() != SERVICE.EXECUTION_CANCELLED) {

							services.add(aService);
						}
					}
				}
			}
			return services;
		}

		private PathType parsePathElement(Element pathElem) {
			PathType path = new PathType();
			path.setSource(pathElem.getChildText(ScheduleXML.SOURCENE_ELEMENT));
			path.setTarget(pathElem.getChildText(ScheduleXML.TARGETNE_ELEMENT));
			path.setRate(Integer.parseInt(pathElem
			    .getChildText(ScheduleXML.RATE_ELEMENT)));

			path.setSrlg(pathElem.getChildText(ScheduleXML.SRLG_ELEMENT));
			path.setCost(Integer.parseInt(pathElem
			    .getChildText(ScheduleXML.COST_ELEMENT)));
			path.setMetric(Integer.parseInt(pathElem
			    .getChildText(ScheduleXML.METRIC_ELEMENT)));
			path.setHop(Integer.parseInt(pathElem
			    .getChildText(ScheduleXML.HOP_ELEMENT)));
			try {
				path.setProtectionType(PathType.PROTECTION_TYPE.valueOf(pathElem
				    .getChildText(PROTECTION_ELEMENT)));
			}
			catch (IllegalArgumentException e) {
				log.error(
				    "Unknown protection type "
				        + pathElem.getChildText(PROTECTION_ELEMENT), e);
			}

			path.setSharedRiskServiceGroup(pathElem
			    .getChildText(ScheduleXML.SHARED_RISK_SERVICE_GROUP_ELEMENT));

			EndPointType srcEndPoint = new EndPointType();
			Element srcEndPointElem = pathElem
			    .getChild(ScheduleXML.SOURCEENDPOINT_ELEMENT);
			srcEndPoint.setId(srcEndPointElem.getAttributeValue(ScheduleXML.ID_ATTR));
			Map<String, String> srcAttr = new HashMap<String, String>();
			srcAttr.put(FacilityConstants.TNA_ATTR,
			    srcEndPointElem.getAttributeValue(ScheduleXML.SOURCETNA_ATTR));
			srcEndPoint.setAttributes(srcAttr);
			try {
				srcEndPoint.setChannelNumber(Integer.parseInt(pathElem.getChild(
				    ScheduleXML.SOURCEENDPOINT_ELEMENT).getAttributeValue(
				    ScheduleXML.CHANNEL_ATTR)));
			}
			catch (NumberFormatException e) {
				// This is an expected situation for layers 1 and 2. So, please
				// do not log it!
			}

			EndPointType dstEndPoint = new EndPointType();
			Element dstEndPointElem = pathElem
			    .getChild(ScheduleXML.TARGETENDPOINT_ELEMENT);
			dstEndPoint.setId(dstEndPointElem.getAttributeValue(ScheduleXML.ID_ATTR));
			Map<String, String> dstAttr = new HashMap<String, String>();
			dstAttr.put(FacilityConstants.TNA_ATTR,
			    dstEndPointElem.getAttributeValue(ScheduleXML.TARGETTNA_ATTR));
			dstEndPoint.setAttributes(dstAttr);
			try {
				dstEndPoint.setChannelNumber(Integer.parseInt(pathElem.getChild(
				    ScheduleXML.TARGETENDPOINT_ELEMENT).getAttributeValue(
				    ScheduleXML.CHANNEL_ATTR)));
			}
			catch (NumberFormatException e) {
				// This is an expected situation for layers 1 and 2. So, please
				// do not log it!
			}

			path.setSourceEndPoint(srcEndPoint);
			path.setTargetEndPoint(dstEndPoint);
			return path;
		}

		private UserType parseUserInfo(Element userElem) {
			/*
			 * Please DO NOT remove the folowing userinfo setup This is for schedule
			 * to undergo SECURITY. If something not working, make sure it got to be
			 * fixed at the ROOT CAUSE
			 */

			if (userElem != null) {
				return new UserType(
				    userElem.getChildText(ScheduleXML.USERID_ELEMENT),
				    new UserGroupName(userElem
				        .getChildText(ScheduleXML.BILLINGGROUP_ELEMENT)),
				    userElem
				        .getChildText(ScheduleXML.SOURCE_ENDPOINT_USERGROUP_ELEMENT),
				    userElem
				        .getChildText(ScheduleXML.TARGET_ENDPOINT_USERGROUP_ELEMENT),
				    userElem
				        .getChildText(ScheduleXML.SOURCE_ENDPOINT_RESOURCEGROUP_ELEMENT),
				    userElem
				        .getChildText(ScheduleXML.TARGET_ENDPOINT_RESOURCEGROUP_ELEMENT),
				    null);
			}
			return new UserType(null, null, null, null, null, null, null);
		}
	}

	// <schedule> attributes
	public static final String SCHD_ID = "id"; // Used as primary key
	public static final String SCHD_ACTIVATION_TYPE = "activationType";
	public static final String SCHD_NAME = "name";
	public static final String SCHD_STATUS = "status";
	public static final String SCHD_STARTTIME = "startTime";
	public static final String SCHD_ENDTIME = "endTime";
	public static final String SCHD_DURATION = "duration";

	// <user>
	public static final String SCHD_USERID = "userId";
	public static final String SCHD_SOURCEENDPOINTUSERGROUP = "sourceEndpointUserGroup";
	public static final String SCHD_TARGETENDPOINTUSERGROUP = "targetEndpointUserGroup";
	public static final String SCHD_BILLINGGROUP = "billingGroup";
	public static final String SCHD_SOURCEENDPOINTRESOURCEGROUP = "sourceEndpointResourceGroup";
	public static final String SCHD_TARGETENDPOINTRESOURCEGROUP = "targetEndpointResourceGroup";
	public static final String SCHD_EMAIL = "email";

	// <recurrence>
	public static final String SCHD_RECURRENCE_TYPE = "recurrenceType";
	public static final String SCHD_RECURRENCE_DAY = "recurrenceDay";
	public static final String SCHD_RECURRENCE_MONTH = "recurrenceMonth";
	public static final String SCHD_RECURRENCE_WEEKDAY = "recurrenceWeekday";

	// <path>
	public static final String SCHD_PATH_SOURCE = "path_source";
	public static final String SCHD_PATH_TARGET = "path_target";
	public static final String SCHD_PATH_RATE = "path_rate";
	public static final String SCHD_PATH_SRLG = "path_srlg";
	public static final String SCHD_PATH_COST = "path_cost";
	public static final String SCHD_PATH_METRIC = "path_metric";
	public static final String SCHD_PATH_HOP = "path_hop";
	public static final String SCHD_PATH_VCAT_ROUTING_OPTION = "path_vcatRoutingOption";
	public static final String SCHD_PATH_SHAREDRISKSERVICEGROUP = "path_sharedriskservicegroup";
	public static final String SCHD_PATH_PROTECTION = "path_protection";
	public static final String SCHD_PATH_NSV = "path_NameStringValue";

	public static final String SCHD_PATH_SOURCEENDPOINT_CHANNEL = "path_sourceendpoint_channel";
	public static final String SCHD_PATH_SOURCEENDPOINT_ID = "path_sourceendpoint_id";
	public static final String SCHD_PATH_SOURCEENDPOINT_TNA = "path_sourceendpoint_tna";
	public static final String SCHD_PATH_SOURCEENDPOINT_VLANID = "path_sourceendpoint_vlanid";

	public static final String SCHD_PATH_TARGETENDPOINT_CHANNEL = "path_targetendpoint_channel";
	public static final String SCHD_PATH_TARGETENDPOINT_ID = "path_targetendpoint_id";
	public static final String SCHD_PATH_TARGETENDPOINT_TNA = "path_targetendpoint_tna";
	public static final String SCHD_PATH_TARGETENDPOINT_VLANID = "path_targetendpoint_vlanid";

	// filter options
	public static final String STARTTIME_GREATERTHAN_EQUALTO = "starttime_greaterthan_equalto";
	public static final String ENDTIME_LESSTHAN_EQUALTO = "endtime_lessthan_equalto";
	/**
	 * This is a set of database column names that we permit filtering on. This
	 * can include all valid columns even if we don't expect or want anyone to
	 * actually query on a given column. Most importantly this list is used to
	 * prevent sql injection attacks, our filters are supplied as
	 * Map<String,String> and while we escape the values we don't escape the
	 * column names, instead we verify that they are in this set before permitting
	 * them to be used. As long as this set does not contain invalid SQL
	 * characters our queries are safe.
	 */
	private static final TreeSet<String> VALID_FILTER_KEYS = new TreeSet<String>(
	    Arrays.asList(new String[] { SCHD_ID, SCHD_ACTIVATION_TYPE, SCHD_NAME,
	        SCHD_STATUS, SCHD_STARTTIME, SCHD_ENDTIME, SCHD_DURATION,
	        SCHD_USERID, SCHD_SOURCEENDPOINTUSERGROUP,
	        SCHD_TARGETENDPOINTUSERGROUP, SCHD_BILLINGGROUP,
	        SCHD_SOURCEENDPOINTRESOURCEGROUP, SCHD_TARGETENDPOINTRESOURCEGROUP,
	        SCHD_EMAIL, SCHD_RECURRENCE_TYPE, SCHD_RECURRENCE_DAY,
	        SCHD_RECURRENCE_MONTH, SCHD_RECURRENCE_WEEKDAY, SCHD_PATH_SOURCE,
	        SCHD_PATH_TARGET, SCHD_PATH_RATE, SCHD_PATH_SRLG, SCHD_PATH_COST,
	        SCHD_PATH_METRIC, SCHD_PATH_HOP, SCHD_PATH_VCAT_ROUTING_OPTION,
	        SCHD_PATH_SHAREDRISKSERVICEGROUP, SCHD_PATH_PROTECTION,
	        SCHD_PATH_NSV, SCHD_PATH_SOURCEENDPOINT_CHANNEL,
	        SCHD_PATH_SOURCEENDPOINT_ID, SCHD_PATH_SOURCEENDPOINT_TNA,
	        SCHD_PATH_SOURCEENDPOINT_VLANID, SCHD_PATH_TARGETENDPOINT_CHANNEL,
	        SCHD_PATH_TARGETENDPOINT_ID, SCHD_PATH_TARGETENDPOINT_TNA,
	        SCHD_PATH_TARGETENDPOINT_VLANID, STARTTIME_GREATERTHAN_EQUALTO,
	        DbKeys.STARTTIME_LESSTHAN_EQUALTO, DbKeys.ENDTIME_GREATERTHAN_EQUALTO,
	        ENDTIME_LESSTHAN_EQUALTO }));

	/**
	 * TABLENAME is private so that anyone outside of this class must use
	 * getInstance().getTableName() to access it and thus insure the table has
	 * been created before its accessed.
	 */

	/**
	 * History of changes
	 * <p>
	 * Table Schedule had duration as an integer, table Schedule2 has duration as
	 * a BIGINT
	 * <p>
	 * Schedule includes many changes, the inclusion of the vcat, vlan and email
	 * fields, the removal of the templateIds, and support for arbitrary
	 * name/value pair fields. Fields were widened to varchar(255) if they were
	 * smaller to prevent future headache.
	 */
	private static final String TABLENAME = "Schedule";
	private static final String COMMA_SPACE = ", ";

	private static String arrayToString(int[] data) {
		if (data == null) {
			return "";
		}
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			if (i == data.length - 1) {
				temp.append(data[i]);
			}
			else {
				temp.append(data[i] + ",");
			}
		}
		return temp.toString();
	}

	private static int[] stringToIntArray(String data) {
		String[] tempString = data.split(",");

		int[] tempInt = new int[tempString.length];
		for (int i = 0; i < tempInt.length; i++) {
			try {
				tempInt[i] = Integer.parseInt(tempString[i]);
			}
			catch (Exception e) {
				// expect a numeric
				tempInt[i] = 0;
			}
		}
		return tempInt;
	}

	public boolean add(final Schedule aSchedule) throws Exception {
		StringBuilder sql = new StringBuilder();
		int i = 0;
		sql.append("insert into " + TABLENAME + " (");
		sql.append(SCHD_ID);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_ACTIVATION_TYPE);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_NAME);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_STATUS);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_STARTTIME);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_ENDTIME);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_DURATION);
		sql.append(COMMA_SPACE);
		i++;
		// <user>
		sql.append(SCHD_USERID);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_SOURCEENDPOINTUSERGROUP);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_TARGETENDPOINTUSERGROUP);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_BILLINGGROUP);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_SOURCEENDPOINTRESOURCEGROUP);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_TARGETENDPOINTRESOURCEGROUP);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_EMAIL);
		sql.append(COMMA_SPACE);
		i++;
		// <recurrence>
		sql.append(SCHD_RECURRENCE_TYPE);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_RECURRENCE_DAY);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_RECURRENCE_MONTH);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_RECURRENCE_WEEKDAY);
		sql.append(COMMA_SPACE);
		i++;
		// <path>
		sql.append(SCHD_PATH_SOURCE);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_TARGET);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_RATE);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_SRLG);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_COST);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_METRIC);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_HOP);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_VCAT_ROUTING_OPTION);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_SHAREDRISKSERVICEGROUP);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_PROTECTION);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_NSV);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_SOURCEENDPOINT_CHANNEL);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_SOURCEENDPOINT_ID);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_SOURCEENDPOINT_TNA);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_SOURCEENDPOINT_VLANID);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_TARGETENDPOINT_CHANNEL);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_TARGETENDPOINT_ID);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_TARGETENDPOINT_TNA);
		sql.append(COMMA_SPACE);
		i++;
		sql.append(SCHD_PATH_TARGETENDPOINT_VLANID);
		i++;

		sql.append(" ) values ( ");

		for (int j = 0; j < i - 1; j++) {
			sql.append("?,");
		}
		sql.append("? );");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			int updateCount = -1;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				// <schedule> attributes
				int i = 1;

				stmt.setString(i++, aSchedule.getId());
				stmt.setString(i++, aSchedule.getActivationType().name());
				stmt.setString(i++, aSchedule.getName());
				stmt.setString(i++, aSchedule.getStatus().name());
				stmt.setLong(i++, aSchedule.getStartTime());
				stmt.setLong(i++, aSchedule.getEndTime());
				stmt.setLong(i++, aSchedule.getDurationLong());

				// <user> info
				UserType userType = aSchedule.getUserInfo();
				stmt.setString(i++, userType.getUserId());
				stmt.setString(i++, userType.getSourceEndpointUserGroup());
				stmt.setString(i++, userType.getTargetEndpointUserGroup());
				stmt.setString(i++, userType.getBillingGroup().toString());
				stmt.setString(i++, userType.getSourceEndpointResourceGroup());
				stmt.setString(i++, userType.getTargetEndpointResourceGroup());
				stmt.setString(
				    i++,
				    userType.getEmailAddress() == null ? "" : userType
				        .getEmailAddress());

				// <recurrence>
				stmt.setString(i++, aSchedule.getRecurrence().getType().toString());
				stmt.setInt(i++, aSchedule.getRecurrence().getDay());
				stmt.setInt(i++, aSchedule.getRecurrence().getMonth());
				// returns int[]
				int[] ia = aSchedule.getRecurrence().getWeekDay();
				stmt.setString(i++, arrayToString(ia));

				// <path>
				PathType path = aSchedule.getPath();
				stmt.setString(i++, path.getSource());
				stmt.setString(i++, path.getTarget());
				stmt.setInt(i++, path.getRate());
				stmt.setString(i++, path.getSrlg());
				stmt.setInt(i++, path.getCost());
				stmt.setInt(i++, path.getMetric());
				stmt.setInt(i++, path.getHop());
				stmt.setString(i++, Boolean.toString(path.getVcatRoutingOption()));
				stmt.setString(i++, path.getSharedRiskServiceGroup());

				stmt.setString(i++, path.getProtectionType().name());
				stmt.setString(
				    i++,
				    XmlUtility.rootNodeToString(DbOpsHelper.mapToElement("wp",
				        path.getNsvMap())));
				stmt.setInt(i++, path.getSourceEndPoint().getChannelNumber());
				stmt.setString(i++, path.getSourceEndPoint().getId());
				stmt.setString(i++, path.getSourceEndPoint().getName());
				stmt.setString(i++,
				    path.getSrcVlanId() == null ? "" : path.getSrcVlanId());
				stmt.setInt(i++, path.getTargetEndPoint().getChannelNumber());
				stmt.setString(i++, path.getTargetEndPoint().getId());
				stmt.setString(i++, path.getTargetEndPoint().getName());
				stmt.setString(i++,
				    path.getDstVlanId() == null ? "" : path.getDstVlanId());

				return stmt;
			}

			@Override
			public Object getResult() {
				return Boolean.valueOf(updateCount > 0);
			}

			@Override
			public void setUpdateCount(int count) {
				updateCount = count;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return ((Boolean) dbOp.getResult()).booleanValue();
	}

	/*
	 * WARNING - This method will not use the service listing cached in the
	 * Schedule record. It will instead join to the LightPath table. This should
	 * be done throughout this class, foregoing the notion of a multi-span LPCP_PORT
	 * deployment in order to use the power of the relational db.
	 */

	public void delete(final String scheduleId) throws Exception {
		String sql = "delete from " + TABLENAME + " where " + SCHD_ID + " LIKE ?;";
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, scheduleId);
				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql, dbOp);
	}

	/**
	 * For testing only drops and recreates database.
	 */
	public void deleteAll() throws Exception {
		DbOperationsManager.INSTANCE.executeDbOpWithResults(
		    "delete from " + TABLENAME + ";", new DbOpWithResultsAdapter());
	}

	/**
	 * Converted to return a structure instead of xml
	 */
	public List<AuditScheduleStatusHolder> getAuditScheduleStatus()
	    throws Exception {

		/*
		 * (with aliasing) ... select from LightPath AS LP INNER JOIN Schedule AS
		 * SCHD ON LP.scheduleId = SCHD.id where ( ( SCHD.status =
		 * 'CONFIRMATION_PENDING' or SCHD.status = 'EXECUTION_INPROGRESS' or
		 * SCHD.status = 'EXECUTION_PENDING' ) AND SCHD.endTime < 9227894981873 )
		 * AND ( ( LP.status = '0' or LP.status = '3' or LP.status = '7' or
		 * LP.status = '6' ) AND LP.endTime < 9227894981873 )
		 */


		StringBuilder sql = new StringBuilder();

		sql.append("select ");

		// indexed results to avoid column name ambiguity
		sql.append("SCHD." + SCHD_ID + "," + "SCHD." + SCHD_STATUS + "," + "SCHD."
		    + SCHD_ENDTIME);
		sql.append("," + "LP." + DbKeys.LightPathCols.LP_SERVICEID + "," + "LP."
		    + DbKeys.LightPathCols.LP_STATUS);

		sql.append(" from " + DbLightPath.INSTANCE.getTableName()
		    + " AS LP INNER JOIN " + TABLENAME + " AS SCHD ON ");
		sql.append("LP." + DbKeys.LightPathCols.LP_SCHEDULEID + " = " + "SCHD."
		    + SCHD_ID + " where ");
		sql.append(" ( ");
		sql.append(" ( " + "SCHD." + SCHD_STATUS + " = 'CONFIRMATION_PENDING' or "
		    + "SCHD." + SCHD_STATUS + " = 'EXECUTION_INPROGRESS' or " + "SCHD."
		    + SCHD_STATUS + " = 'EXECUTION_PENDING'" + " ) ");
		sql.append(" AND ");
		sql.append("SCHD." + SCHD_ENDTIME + " < " + System.currentTimeMillis());
		sql.append(")");
		sql.append(" AND ");
		sql.append(" ( ");
		sql.append(" ( " + "LP." + DbKeys.LightPathCols.LP_STATUS + " = '0' or "
		    + "LP." + DbKeys.LightPathCols.LP_STATUS + " = '3' or " + "LP."
		    + DbKeys.LightPathCols.LP_STATUS + " = '7' or " + "LP."
		    + DbKeys.LightPathCols.LP_STATUS + " = '6'" + " ) ");
		sql.append(" AND ");
		sql.append("LP." + DbKeys.LightPathCols.LP_ENDTIME + " < "
		    + System.currentTimeMillis());
		sql.append(");");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			private List<AuditScheduleStatusHolder> result = new ArrayList<AuditScheduleStatusHolder>();

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				HashMap<String, List<List<String>>> map = new HashMap<String, List<List<String>>>();

				while (rs.next()) {
					String scheduleId = rs.getString(1);
					String scheduleStatus = rs.getString(2);
					long scheduleEndtime = rs.getLong(3);

					String serviceId = rs.getString(4);
					String serviceStatus = State.SERVICE.values()[Integer.parseInt(rs
					    .getString(5))].name();

					String key = scheduleId + " " + scheduleStatus + " "
					    + scheduleEndtime;
					List<String> value = Arrays.asList(new String[] { serviceId,
					    serviceStatus });

					List<List<String>> resultArr = map.get(key);

					if (resultArr == null) {
						resultArr = new ArrayList<List<String>>();
						map.put(key, resultArr);
					}

					resultArr.add(value);
				}

				/*
				 * We should have accumulated all the service records for each schedule
				 * in our map now
				 */
				for (Map.Entry<String, List<List<String>>> e : map.entrySet()) {
					StringTokenizer st = new StringTokenizer(e.getKey());
					String id = st.nextToken();
					String status = st.nextToken();
					long endTime = Long.parseLong(st.nextToken());

					AuditScheduleStatusHolder a = new AuditScheduleStatusHolder(id,
					    status, endTime, e.getValue());
					result.add(a);
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (List<AuditScheduleStatusHolder>) dbOp.getResult();

	}

	public long getNextSchedule() throws Exception {

		StringBuilder sql = new StringBuilder();
		sql.append("select MIN(" + SCHD_ENDTIME + ") from " + TABLENAME + " where ");
		sql.append(SCHD_STATUS + " = 'CONFIRMATION_PENDING' OR ");
		sql.append(SCHD_STATUS + " = 'EXECUTION_INPROGRESS' OR ");
		sql.append(SCHD_STATUS + " = 'EXECUTION_PENDING'");
		sql.append(";");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			Long result = Long.valueOf(0);

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				if (rs.next()) {
					result = Long.valueOf(rs.getLong(1));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return ((Long) dbOp.getResult()).longValue();
	}

	/**
	 * @TODO: Revisit and clean this up, this method and
	 *        getServiceUsageForTNAasXml should be combined and not do any XML
	 *        translation This is crossing tables, intentionally foregoing the
	 *        NRB_PORT/LPCP_PORT segregation!
	 */

	public List<DracService> getServiceUsageForTNA(String tna) throws Exception {
		/*
		 * NOTE: Original xml query had defect - canceled services were considered
		 * 'used' by the returning data. i.e. the caller to this routine is not
		 * checking status correctly. Will implement status check in the SQL query.
		 */
		String resultListString = getServiceUsageForTNAasXml(tna);
		if (resultListString != null) {
			return new ServXML(resultListString).toServiceUsageList();
		}

		return new ArrayList<DracService>();
	}

	/**
	 * This is crossing tables, intentionally foregoing the NRB_PORT/LPCP_PORT segregation!
	 */
	public Schedule queryScheduleFromServiceId(final String serviceId)
	    throws Exception {
		/*
		 * select from Schedule where id = ( select scheduleId from LightPath where
		 * serviceId = 'SERVICE-1228402113282' );
		 */

		final StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME + " where " + SCHD_ID + " = ");
		sql.append("( select " + DbKeys.LightPathCols.LP_SCHEDULEID + " from "
		    + DbLightPath.INSTANCE.getTableName() + " where "
		    + DbKeys.LightPathCols.LP_SERVICEID + " = ? );");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			Schedule result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, serviceId);
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				List<Schedule> list = processRetrieveResultList(rs);
				if (list == null) {
					throw new Exception(
					    "queryScheduleFromServiceId::  schedule could not be found for: '"
					        + serviceId + "' query:" + sql);
				}
				if (list.size() != 1) {
					throw new Exception(
					    "queryScheduleFromServiceId:: unique schedule could not be found for: '"
					        + serviceId + "' found " + list.size() + " matches! " + list
					        + " query " + sql);
				}

				result = list.get(0);
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (Schedule) dbOp.getResult();
	}

	public List<String> getScheduleIdsForServices(List<DracService> services){

		List<String>ids = new ArrayList<String>();
		for(DracService service : services){
			ids.add(service.getScheduleId());
		}
		return ids;
	}
	
	public List<Schedule> querySchedules(List<String> ids) throws Exception {
		StringBuilder appendString = new StringBuilder();
		for(String id: ids){
			if(appendString.length()>0){
				appendString.append(", ");
			}
			appendString.append("'");
			appendString.append(id);
			appendString.append("'");
		}
		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME + " where ");
		sql.append(" id in ( ");
		sql.append(appendString.toString());
		sql.append(" );");
		
		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<Schedule> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {				
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				result = processRetrieveResultList(rs);
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		List<Schedule> schedules = (List<Schedule>) dbOp.getResult();	
		return 	schedules;
	}
	public List<Schedule> querySchedules(long startTime, long endTime,
		    final List<UserGroupName> groups) throws Exception {
		return querySchedules(startTime, endTime, groups, null);
	}
	
	public List<Schedule> querySchedules(long startTime, long endTime,
	    final List<UserGroupName> groups, final String name) throws Exception {
		/*
		 * ($a/startTime >= " + startTime + " and $a/startTime <=
		 * " + endTime + ")" + " or ($a/endTime >= " + startTime +
		 * " and $a/endTime <= " + endTime + ") " + " or ($a/startTime <= " +
		 * startTime+ " and $a/endTime >= " + endTime + ")
		 */

		/*
		 * select from Schedule where ( ( startTime >= 1228280400000 and startTime
		 * <= 1228366740000 ) or ( endTime >= 1228280400000 and endTime <=
		 * 1228366740000 ) or ( startTime <= 1228280400000 and endTime >=
		 * 1228366740000 ) ) AND ( (STRCMP(billingGroup, 'SystemAdminGroup')=0) OR
		 * (STRCMP(billingGroup, 'SystemAdminGroup')=0) OR (STRCMP(billingGroup,
		 * 'SystemAdminGroup')=0) )
		 */

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME + " where ");
		sql.append(" ( ");

		/*
		 * Time Conditions condition 1: schedule start time occurs inside our search
		 * range condition 2: schedule end time occurs inside our search range
		 * condition condition 3: schedule start time before start range, and end
		 * time after end range (i.e. executes inside range)
		 */
		sql.append(" (( " + SCHD_STARTTIME + " >= " + startTime + " AND "
		    + SCHD_STARTTIME + " <= " + endTime + " ) ");
		sql.append("or ( " + SCHD_ENDTIME + " >= " + startTime + " AND "
		    + SCHD_ENDTIME + " <= " + endTime + " ) ");
		sql.append("or ( " + SCHD_STARTTIME + " <= " + startTime + " AND "
		    + SCHD_ENDTIME + " >= " + endTime + ")) ");
		if(name != null && !name.trim().equals("")){
			sql.append(" AND "+SCHD_NAME);
			sql.append(" = ? ");	
		}
		sql.append(" ) ");

		if (groups != null && groups.size() > 0) {
			sql.append(" AND ");
			sql.append(" ( ");

			boolean flag = false;
			for (@SuppressWarnings("unused")
			UserGroupName group : groups) {
				if (flag) {
					sql.append(" OR ");
				}
				sql.append("(" + SCHD_BILLINGGROUP + "= ?)");
				flag = true;
			}

			sql.append(" ) ");
		}
		sql.append(";");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<Schedule> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				int offset = 1;
				if(name != null && !name.trim().equals("")){
					stmt.setString(offset, name);
					offset++;
				}
				if (groups != null) {
					for (int i = 0; i < groups.size(); i++) {
						stmt.setString(i + offset, groups.get(i).toString());
					}
				}
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				result = processRetrieveResultList(rs);
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (List<Schedule>) dbOp.getResult();
	}

	/**
	 * This is crossing tables, intentionally foregoing the NRB_PORT/LPCP_PORT segregation!
	 * <p>
	 * WARNING *** These time inputs are for the LIGHTPATH record, not the
	 * aggregate time start/end of the SCHEDULE record
	 */
	public List<DracService> queryServices(final long startTime,
	    final long endTime, final List<UserGroupName> groups) throws Exception {
		List<DracService> result = new ArrayList<DracService>();
		List<ServiceXml> serviceResults = DbLightPath.INSTANCE.queryServices(
		    startTime, endTime);

		if (serviceResults == null || serviceResults.isEmpty()) {
			return result;
		}

		Set<String> scheduleIdSet = new HashSet<String>();
		StringBuilder scheduleIds = new StringBuilder();
		for (ServiceXml service : serviceResults) {
			String scheduleId = service.getScheduleId();
			if (scheduleIdSet.add(scheduleId)) {
				if (scheduleIds.length() != 0) {
					scheduleIds.append(",");
				}
				scheduleIds.append("'" + scheduleId + "'");
			}
		}

		// select * from Schedule where id IN (listIds)
		if (scheduleIds.length() > 0) {
			StringBuilder sql = new StringBuilder();
			sql.append("select * from " + TABLENAME + " where ");
			sql.append(SCHD_ID + " IN ( " + scheduleIds.toString() + " ) ");

			if (groups != null && groups.size() > 0) {
				sql.append(" AND ");
				sql.append(" ( ");

				boolean flag = false;
				for (@SuppressWarnings("unused")
				UserGroupName group : groups) {
					if (flag) {
						sql.append(" OR ");
					}

					sql.append("(" + SCHD_BILLINGGROUP + " = ?)");
					flag = true;
				}

				sql.append(" ) ");
			}
			sql.append(";");

			DbOpWithResultsAdapter schdDbOp = new DbOpWithResultsAdapter() {
				Map<String, Schedule> result;

				@Override
				public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
				    throws Exception {
					if (groups != null) {
						for (int i = 0; i < groups.size(); i++) {
							stmt.setString(i + 1, groups.get(i).toString());
						}
					}
					return stmt;
				}

				@Override
				public Object getResult() {
					return result;
				}

				@Override
				public void processResults(ResultSet rs) throws Exception {
					result = new HashMap<String, Schedule>();
					while (rs.next()) {
						result.put(rs.getString(SCHD_ID), processRetrieveResult(rs));
					}
				}
			};
			DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), schdDbOp);
			Map<String, Schedule> scheduleMap = (Map<String, Schedule>) schdDbOp
			    .getResult();

			if (scheduleMap != null && scheduleMap.size() > 0) {
				if (!serviceResults.isEmpty()) {
					for (ServiceXml serviceXml : serviceResults) {
						Schedule scheduleType = scheduleMap.get(serviceXml.getScheduleId());

						// Intersect:
						if (scheduleType != null) {
							result.add(stitchScheduleAndServiceSummary(scheduleType,
							    serviceXml));
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * This is crossing tables, intentionally foregoing the NRB_PORT/LPCP_PORT segregation!
	 */
	public DracService queryServiceSummaryFromServiceId(final String serviceId)
	    throws Exception {
		DracService serviceIdType = null;

		Map<String, Object> serviceFilter = new HashMap<String, Object>();
		serviceFilter.put(DbKeys.LightPathCols.LP_SERVICEID, serviceId);
		List<ServiceXml> lightPathList = DbLightPath.INSTANCE.retrieve(
		    serviceFilter);
		if (lightPathList != null && lightPathList.size() == 1) {
			ServiceXml service = lightPathList.get(0);
			String scheduleId = service.getScheduleId();

			Map<String, String> schedFilter = new HashMap<String, String>();
			schedFilter.put(SCHD_ID, scheduleId);
			List<Schedule> listSchedules = retrieve(schedFilter);
			if (listSchedules != null && listSchedules.size() == 1) {
				Schedule scheduleType = listSchedules.get(0);
				serviceIdType = stitchScheduleAndServiceSummary(scheduleType, service);
			}
		}

		return serviceIdType;
	}

	/**
	 * This is crossing tables, intentionally foregoing the NRB_PORT/LPCP_PORT segregation!
	 * <p>
	 * WARNING *** These time inputs are for the LIGHTPATH record, not the
	 * aggregate time start/end of the SCHEDULE record
	 */
	public List<DracService> queryUtilization(final String tna, long rangeStart,
	    long rangeEnd) throws Exception {
		List<DracService> result = new ArrayList<DracService>();
		List<ServiceXml> serviceResults = DbLightPath.INSTANCE.queryServices(
		    rangeStart, rangeEnd);

		if (serviceResults == null || serviceResults.isEmpty()) {
			return result;

		}
		// sort - is this necessary?
		Comparator<ServiceXml> comp = new Comparator<ServiceXml>() {
			@Override
			public int compare(ServiceXml s1, ServiceXml s2) {
				long t1 = s1.getStartTime();
				long t2 = s2.getStartTime();
				if (t1 > t2) {
					return -1;
				}
				else if (t1 < t2) {
					return 1;
				}
				else {
					return 0;
				}
			}
		};
		Collections.sort(serviceResults, comp);

		Set<String> scheduleIdSet = new HashSet<String>();
		StringBuilder scheduleIds = new StringBuilder();
		for (ServiceXml service : serviceResults) {
			String scheduleId = service.getScheduleId();
			if (scheduleIdSet.add(scheduleId)) {
				if (scheduleIds.length() != 0) {
					scheduleIds.append(",");
				}
				scheduleIds.append("'" + scheduleId + "'");
			}
		}

		// select * from Schedule where id IN (listIds)
		if (scheduleIds.length() > 0) {
			StringBuilder sql = new StringBuilder();
			sql.append("select * from " + TABLENAME + " where ");
			/*
			 * TODO: This is a possible SQL injection attack as we don't escape
			 * scheduleIds. I don't think its a worry as scheduleIds is built from a
			 * query to the database and is not supplied by the end user. However if
			 * its possible to write "bad" entries to the db, this could be exploited.
			 */
			sql.append(SCHD_ID + " IN ( " + scheduleIds.toString() + " ) ");
			sql.append(" AND ");

			sql.append(SCHD_PATH_SOURCEENDPOINT_TNA + " = ? OR ");
			sql.append(SCHD_PATH_TARGETENDPOINT_TNA + " = ? ");

			DbOpWithResultsAdapter schdDbOp = new DbOpWithResultsAdapter() {
				Map<String, Schedule> result;

				@Override
				public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
				    throws Exception {
					stmt.setString(1, tna);
					stmt.setString(2, tna);
					return stmt;
				}

				@Override
				public Object getResult() {
					return result;
				}

				@Override
				public void processResults(ResultSet rs) throws Exception {
					result = new HashMap<String, Schedule>();
					while (rs.next()) {
						result.put(rs.getString(SCHD_ID), processRetrieveResult(rs));
					}
				}
			};
			DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), schdDbOp);
			Map<String, Schedule> scheduleMap = (Map<String, Schedule>) schdDbOp
			    .getResult();

			if (scheduleMap != null && scheduleMap.size() > 0) {
				Element serviceList = new Element("serviceList");

				for (ServiceXml serviceXml : serviceResults) {
					Schedule scheduleType = scheduleMap.get(serviceXml.getScheduleId());

					// The service within the time range, overlapping with the
					// schedule TNA, will not be
					// null...
					if (scheduleType != null) {
						Element serviceElement = new Element("service");
						serviceElement.setAttribute("id", serviceXml.getServiceId());
						serviceElement.setAttribute("name", scheduleType.getName());
						serviceElement.setAttribute("scheduleId", scheduleType.getId());
						serviceElement
						    .setAttribute("status", State.SERVICE.values()[serviceXml
						        .getStatus().ordinal()].name());
						serviceElement.setAttribute("startTime",
						    Long.toString(serviceXml.getStartTime()));
						serviceElement.setAttribute("endTime",
						    Long.toString(serviceXml.getEndTime()));

						Element scheduleElement = DbOpsHelper.xmlToElement(new ScheduleXML(
						    scheduleType).rootNodeToString());
						serviceElement.addContent((Element) scheduleElement.getChild(
						    ScheduleXML.PATH_ELEMENT).clone());
						serviceList.addContent(serviceElement);
					}
				}

				if (serviceList.getContentSize() > 0) {
					result = new ServXML(DbOpsHelper.elementToString(serviceList))
					    .toServiceUsageList();
				}
			}
		}

		return result;
	}

	public List<Schedule> retrieve(final Map<String, String> filter)
	    throws Exception {

		boolean flag = false;
		final List<Object> attrList = new ArrayList<Object>();

		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME);

		if (filter != null) {
			for (Map.Entry<String, String> oEntry : filter.entrySet()) {
				String name = oEntry.getKey();
				String value = oEntry.getValue();

				if (!VALID_FILTER_KEYS.contains(name)) {
					throw new Exception("DbSchedule invalid filter entry " + name + "="
					    + value + "! Name is not a valid filter key from "
					    + VALID_FILTER_KEYS);
				}

				if (flag) {
					sql.append("AND ");
				}
				else {
					sql.append(" where ");
				}

				if (DbKeys.ENDTIME_GREATERTHAN_EQUALTO.equals(name)) {
					sql.append(SCHD_ENDTIME + " >= ? ");
					attrList.add(Long.valueOf(filter.get(DbKeys.ENDTIME_GREATERTHAN_EQUALTO)));
				}
				else if (ENDTIME_LESSTHAN_EQUALTO.equals(name)) {
					sql.append(SCHD_ENDTIME + " <= ? ");
					attrList.add(Long.valueOf(filter.get(ENDTIME_LESSTHAN_EQUALTO)));
				}
				else if (STARTTIME_GREATERTHAN_EQUALTO.equals(name)) {
					sql.append(SCHD_STARTTIME + " >= ? ");
					attrList.add(Long.valueOf(filter.get(STARTTIME_GREATERTHAN_EQUALTO)));
				}
				else if (DbKeys.STARTTIME_LESSTHAN_EQUALTO.equals(name)) {
					sql.append(SCHD_STARTTIME + " <= ? ");
					attrList.add(Long.valueOf(filter.get(DbKeys.STARTTIME_LESSTHAN_EQUALTO)));
				}
				else {
					/*
					 * This is not subject to a SQL injection attack, as name has been
					 * validated to be a valid column name above.
					 */
					sql.append(name + " = ? ");
					attrList.add(value);
				}

				flag = true;
			}
		}

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			List<Schedule> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				for (int i = 0; i < attrList.size(); i++) {
					Object attr = attrList.get(i);

					if (attr instanceof String) {
						stmt.setString(i + 1, (String) attr);
					}
					else if (attr instanceof Long) {
						stmt.setLong(i + 1, ((Long) attr).longValue());
					}
				}

				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				result = processRetrieveResultList(rs);
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
		return (List<Schedule>) dbOp.getResult();
	}

	public void update(final String scheduleId, final Map<String, String> data)
	    throws Exception {

	  log.info("Updating schedule with id {} and data {}", scheduleId, data);
		boolean flag = false;
		final List<Object> attrList = new ArrayList<Object>();

		StringBuilder sql = new StringBuilder();
		sql.append("update " + TABLENAME + " SET ");

		if (data.containsKey(SCHD_STATUS)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(SCHD_STATUS + " = ? ");
			attrList.add(data.get(SCHD_STATUS));
			flag = true;
		}

		if (data.containsKey(SCHD_STARTTIME)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(SCHD_STARTTIME + " = ? ");
			attrList.add(Long.valueOf(data.get(SCHD_STARTTIME)));
			flag = true;
		}

		if (data.containsKey(SCHD_ENDTIME)) {
			if (flag) {
				sql.append(", ");
			}
			sql.append(SCHD_ENDTIME + " = ? ");
			attrList.add(Long.valueOf(data.get(SCHD_ENDTIME)));
			flag = true;
		}

		if (!flag) {
			log.error(TABLENAME + " update called for unsupported data. " + data);
			return;
		}

		sql.append("where " + SCHD_ID + " LIKE ? ;");

		DbOpWithResultsAdapter dbOp = new DbOpWithResultsAdapter() {
			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				int i;
				for (i = 0; i < attrList.size(); i++) {
					Object attr = attrList.get(i);

					if (attr instanceof String) {
						stmt.setString(i + 1, (String) attr);
					}
					else if (attr instanceof Long) {
						stmt.setLong(i + 1, ((Long) attr).longValue());
					}
				}

				// where
				stmt.setString(i + 1, scheduleId);

				return stmt;
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), dbOp);
	}

	protected String getTableName() {
		return TABLENAME;
	}

	/**
	 * @TODO Revisit and clean this up, this method and getServiceUsageForTNA
	 *       should be combined and not do any XML translation
	 * @param tna
	 * @throws Exception
	 */
	private String getServiceUsageForTNAasXml(final String tna) throws Exception {
		String result = null;

		/*
		 * Note: the original XML implementation, although it returned status, did
		 * not work; the timeslots for the TNA pair were marked as blocked if
		 * occupied by a canceled service. So, check for status explicitly here
		 */
		StringBuilder sql = new StringBuilder();
		sql.append("select * from " + TABLENAME + " where ");
		sql.append(SCHD_PATH_SOURCEENDPOINT_TNA + " = ? OR "
		    + SCHD_PATH_TARGETENDPOINT_TNA + " = ?;");

		DbOpWithResultsAdapter schedDbOp = new DbOpWithResultsAdapter() {
			Map<String, Schedule> result;

			@Override
			public PreparedStatement buildPreparedStatement(PreparedStatement stmt)
			    throws Exception {
				stmt.setString(1, tna);
				stmt.setString(2, tna);
				return stmt;
			}

			@Override
			public Object getResult() {
				return result;
			}

			@Override
			public void processResults(ResultSet rs) throws Exception {
				result = new HashMap<String, Schedule>();
				while (rs.next()) {
					result.put(rs.getString(SCHD_ID), processRetrieveResult(rs));
				}
			}
		};
		DbOperationsManager.INSTANCE.executeDbOpWithResults(sql.toString(), schedDbOp);
		Map<String, Schedule> scheduleResultList = (Map<String, Schedule>) schedDbOp
		    .getResult();

		if (scheduleResultList != null && scheduleResultList.size() > 0) {
			Map<String, Object> serviceFilter = new HashMap<String, Object>();
			serviceFilter.put(DbKeys.LightPathCols.LP_FILTER_SCHEDULEID_LIST,
			    new ArrayList<String>(scheduleResultList.keySet()));
			serviceFilter.put(DbKeys.LightPathCols.LP_ENDTIME_GREATERTHAN_EQUALTO,
			    Long.toString(System.currentTimeMillis()));
			List<ServiceXml> serviceResults = DbLightPath.INSTANCE.retrieve(
			    serviceFilter);

			if (serviceResults != null && serviceResults.size() > 0) {
				Element serviceList = new Element("serviceList");

				for (ServiceXml serviceXml : serviceResults) {
					String scheduleId = serviceXml.getScheduleId();

					Element serviceElement = new Element("service");
					serviceElement.setAttribute("id", serviceXml.getServiceId());
					serviceElement.setAttribute("name", serviceXml.getScheduleName());
					serviceElement.setAttribute("scheduleId", scheduleId);
					serviceElement.setAttribute("status",
					    State.SERVICE.values()[serviceXml.getStatus().ordinal()].name());
					serviceElement.setAttribute("startTime",
					    Long.toString(serviceXml.getStartTime()));
					serviceElement.setAttribute("endTime",
					    Long.toString(serviceXml.getEndTime()));

					Schedule scheduleType = scheduleResultList.get(scheduleId);
					Element scheduleElement = DbOpsHelper.xmlToElement(new ScheduleXML(
					    scheduleType).rootNodeToString());
					serviceElement.addContent((Element) scheduleElement.getChild(
					    ScheduleXML.USER_ELEMENT).clone());
					serviceElement.addContent((Element) scheduleElement.getChild(
					    ScheduleXML.PATH_ELEMENT).clone());

					serviceList.addContent(serviceElement);
				}

				result = DbOpsHelper.elementToString(serviceList);
			}
		}

		return result;
	}

	private Schedule processRetrieveResult(ResultSet rs) throws Exception {
		// <user> info
		UserType userType = new UserType(rs.getString(SCHD_USERID),
		    new UserGroupName(rs.getString(SCHD_BILLINGGROUP)),
		    rs.getString(SCHD_SOURCEENDPOINTUSERGROUP),
		    rs.getString(SCHD_TARGETENDPOINTUSERGROUP),
		    rs.getString(SCHD_SOURCEENDPOINTRESOURCEGROUP),
		    rs.getString(SCHD_TARGETENDPOINTRESOURCEGROUP),
		    rs.getString(SCHD_EMAIL));

		// <recurrence>
		int[] weekdays = null;
		String temp = rs.getString(SCHD_RECURRENCE_WEEKDAY);
		if (temp != null) {
			weekdays = stringToIntArray(temp);
		}
		RecurrenceType recurrence = new RecurrenceType(
		    RecurrenceFreq.parseString(rs.getString(SCHD_RECURRENCE_TYPE)),
		    rs.getInt(SCHD_RECURRENCE_DAY), rs.getInt(SCHD_RECURRENCE_MONTH),
		    weekdays);

		// <path>
		PathType aPath = new PathType();
		aPath.setSource(rs.getString(SCHD_PATH_SOURCE));
		aPath.setTarget(rs.getString(SCHD_PATH_TARGET));
		aPath.setRate(rs.getInt(SCHD_PATH_RATE));
		aPath.setSrlg(rs.getString(SCHD_PATH_SRLG));
		aPath.setCost(rs.getInt(SCHD_PATH_COST));
		aPath.setRoutingMetric(rs.getInt(SCHD_PATH_METRIC));
		aPath.setHop(rs.getInt(SCHD_PATH_HOP));
		aPath.setVcatRoutingOption(Boolean.parseBoolean(rs
		    .getString(SCHD_PATH_VCAT_ROUTING_OPTION)));
		aPath.setSharedRiskServiceGroup(rs
		    .getString(SCHD_PATH_SHAREDRISKSERVICEGROUP));
		aPath.setSrcVlanId(rs.getString(SCHD_PATH_SOURCEENDPOINT_VLANID));
		aPath.setDstVlanId(rs.getString(SCHD_PATH_TARGETENDPOINT_VLANID));
		aPath.setProtectionType(PathType.PROTECTION_TYPE.valueOf(rs
		    .getString(SCHD_PATH_PROTECTION)));

		if (rs.getString(SCHD_PATH_NSV) != null) {
			Map<String, String> nsvMap = DbOpsHelper.elementToMap(XmlUtility
			    .createDocumentRoot(rs.getString(SCHD_PATH_NSV)));
			aPath.setNsvMap(nsvMap);
		}

		EndPointType srcEndPoint = new EndPointType();
		srcEndPoint.setId(rs.getString(SCHD_PATH_SOURCEENDPOINT_ID));
		HashMap<String, String> srcAttr = new HashMap<String, String>();
		String tna = rs.getString(SCHD_PATH_SOURCEENDPOINT_TNA);
		srcAttr.put(FacilityConstants.TNA_ATTR, tna);
		srcEndPoint.setAttributes(srcAttr);
		srcEndPoint.setChannelNumber(rs.getInt(SCHD_PATH_SOURCEENDPOINT_CHANNEL));
		aPath.setSourceEndPoint(srcEndPoint);

		Map<String, String> filter = new HashMap<String, String>();
		filter.put(DbKeys.NetworkElementFacilityCols.TNA, tna);
		List<Map<String, String>> list = DbNetworkElementFacility.INSTANCE
		    .retrieve(filter);
		if (list.size() == 1) {
			Map<String, String> facAttrs = list.get(0);
			String userLabel = facAttrs.get(FacilityConstants.FACLABEL_ATTR);
			if (userLabel != null) {
				srcAttr.put(FacilityConstants.FACLABEL_ATTR, userLabel);
			}
		}

		EndPointType targetEndPoint = new EndPointType();
		targetEndPoint.setId(rs.getString(SCHD_PATH_TARGETENDPOINT_ID));
		HashMap<String, String> targetAttr = new HashMap<String, String>();
		tna = rs.getString(SCHD_PATH_TARGETENDPOINT_TNA);
		targetAttr.put(FacilityConstants.TNA_ATTR, tna);
		targetEndPoint.setAttributes(targetAttr);
		targetEndPoint
		    .setChannelNumber(rs.getInt(SCHD_PATH_TARGETENDPOINT_CHANNEL));
		aPath.setTargetEndPoint(targetEndPoint);

		filter.put(DbKeys.NetworkElementFacilityCols.TNA, tna);
		list = DbNetworkElementFacility.INSTANCE.retrieve(filter);
		if (list.size() == 1) {
			Map<String, String> facAttrs = list.get(0);
			String userLabel = facAttrs.get(FacilityConstants.FACLABEL_ATTR);
			if (userLabel != null) {
				targetAttr.put(FacilityConstants.FACLABEL_ATTR, userLabel);
			}
		}

		Schedule aSchedule = new Schedule(Schedule.ACTIVATION_TYPE.valueOf(rs
		    .getString(SCHD_ACTIVATION_TYPE)), rs.getString(SCHD_ID),
		    rs.getString(SCHD_NAME), State.SCHEDULE.valueOf(rs
		        .getString(SCHD_STATUS)), rs.getLong(SCHD_STARTTIME),
		    Long.valueOf(rs.getLong(SCHD_ENDTIME)), rs.getLong(SCHD_DURATION),
		    userType, aPath,
		    !RecurrenceFreq.FREQ_ONCE.equals(recurrence.getType()), recurrence,
		    null);
		// Oddd we create the schedule (minus the service list) then set the
		// service list using the service.
		aSchedule.setServiceIdList(queryServicesForSchedule(aSchedule));
		return aSchedule;
	}

	private List<Schedule> processRetrieveResultList(ResultSet rs)
	    throws Exception {
		List<Schedule> result = new ArrayList<Schedule>();
		while (rs.next()) {
			result.add(processRetrieveResult(rs));
		}

		return result;
	}

	private List<DracService> queryServicesForSchedule(Schedule scheduleType)
	    throws Exception {
		List<DracService> list = new ArrayList<DracService>();

		try {
			Map<String, Object> serviceFilter = new HashMap<String, Object>();
			serviceFilter.put(DbKeys.LightPathCols.LP_FILTER_SCHEDULEID_LIST,
			    Arrays.asList(new String[] { scheduleType.getId() }));
			List<ServiceXml> serviceResults = DbLightPath.INSTANCE.retrieve(
			    serviceFilter);

			if (serviceResults != null && serviceResults.size() > 0) {
				for (ServiceXml serviceXml : serviceResults) {
					list.add(stitchScheduleAndServiceSummary(scheduleType, serviceXml));
				}
			}
		}
		catch (Exception ex) {
			log.error(
			    "Error in retrieving and formatting service records prior to Schedule creation",
			    ex);
		}

		return list;
	}

	private DracService stitchScheduleAndServiceSummary(Schedule scheduleType,
	    ServiceXml service) throws Exception {

		Element scheduleElement = DbOpsHelper.xmlToElement(new ScheduleXML(
		    scheduleType).rootNodeToString());

		/*
		 * build this: <serviceList scheduleName="xxx"
		 * scheduleId="SCHEDULE-1228405145446" scheduleStatus="EXECUTION_PENDING"
		 * activationType="RESERVATION_AUTOMATIC"
		 * sourceId="00-1B-25-2D-5C-7A_ETH-1-1-1"
		 * targetId="00-1B-25-2D-5B-E6_ETH-1-1-1"> <user> <userId>admin</userId>
		 * <sourceEndpointUserGroup>SystemAdminGroup</sourceEndpointUserGroup>
		 * <targetEndpointUserGroup>SystemAdminGroup</targetEndpointUserGroup>
		 * <billingGroup>SystemAdminGroup</billingGroup>
		 * <sourceEndpointResourceGroup
		 * >SystemAdminResourceGroup</sourceEndpointResourceGroup>
		 * <targetEndpointResourceGroup
		 * >SystemAdminResourceGroup</targetEndpointResourceGroup> </user> <service
		 * activationType="RESERVATION_AUTOMATIC" actualOffset="0"
		 * endTime="1228408200000" id="SERVICE-1228405145474"
		 * startTime="1228405145167" status="EXECUTION_PENDING"> <call
		 * controllerId="47.134.40.205:8001" id="cd28862f-1228405146002"
		 * status="6"/> </service> </serviceList>
		 */

		Element serviceListElement = new Element("serviceList");
		serviceListElement.setAttribute("scheduleName", scheduleType.getName());
		serviceListElement.setAttribute("scheduleId", scheduleType.getId());
		serviceListElement.setAttribute("scheduleStatus", scheduleType.getStatus()
		    .name());
		serviceListElement.setAttribute("activationType", scheduleType
		    .getActivationType().name());
		serviceListElement.setAttribute("sourceId", scheduleType.getPath()
		    .getSourceEndPoint().getId());
		serviceListElement.setAttribute("targetId", scheduleType.getPath()
		    .getTargetEndPoint().getId());

		Element userElement = (Element) scheduleElement.getChild(
		    ScheduleXML.USER_ELEMENT).clone();
		serviceListElement.addContent(userElement);

		Element serviceElement = new Element("service");
		// this is never used:
		serviceElement.setAttribute("actualOffset", "0");
		serviceElement.setAttribute("activationType", service.getActivationType());
		serviceElement.setAttribute("endTime", Long.toString(service.getEndTime()));
		serviceElement.setAttribute("id", service.getServiceId());
		serviceElement.setAttribute("startTime",
		    Long.toString(service.getStartTime()));
		serviceElement.setAttribute("status", State.SERVICE.values()[service
		    .getStatus().ordinal()].name());

		serviceElement.setAttribute("mbs", Integer.toString(service.getMbs()));

		Element callElement = new Element("call");
		callElement.setAttribute("controllerId", service.getControllerId());
		callElement.setAttribute("id", service.getCallId());
		callElement.setAttribute("status",
		    Integer.toString(service.getStatus().ordinal()));

		serviceElement.addContent(callElement);

		serviceListElement.addContent(serviceElement);

		DracService stitchedService = new ServXML(DbOpsHelper.elementToString(serviceListElement))
	    .rootNodeToObject();
		stitchedService.setCrossConnections(service.getCrossConnections());// Like this, in stead of through ServXML, because of unknown side effects to XML-checks/assumptions elsewhere in code
		return stitchedService;
	}

}
