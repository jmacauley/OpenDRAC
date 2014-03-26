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

package com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.common;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlTokenSource;
import org.junit.Test;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.EndsAfterDateT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.PathRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.RecurrenceT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.UserInfoT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidProtectionTypeT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidRecurrenceTypeT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleTypeT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.Schedule;

/**
 * @author pitman
 */
public class ResourceAllocationHelperTest {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Test method for
	 * {@link com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.common.ResourceAllocationHelper#parseCreateReservationScheduleRequestOMElement(java.lang.String, org.apache.axiom.om.OMElement)}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParseCreateReservationScheduleRequestOMElement()
	    throws Exception {

		StAXOMBuilder sb = new StAXOMBuilder(
		    StAXUtils.createXMLStreamReader(new StringReader(
		        xmlToString(buildCreateMsg(true)))));
		log.debug("xml=" + sb.getDocumentElement());
		Schedule t = new ResourceAllocationHelper()
		    .parseCreateReservationScheduleRequestOMElement("admin",
		        sb.getDocumentElement());
		log.debug(" " + t.toDebugString());

		log.debug("----------------------");
		sb = new StAXOMBuilder(StAXUtils.createXMLStreamReader(new StringReader(
		    xmlToString(buildCreateMsg(false)))));
		log.debug("xml=" + sb.getDocumentElement());
		t = new ResourceAllocationHelper()
		    .parseCreateReservationScheduleRequestOMElement("admin",
		        sb.getDocumentElement());
		log.debug(" " + t.toDebugString());
	}

	private CreateReservationScheduleRequestDocument buildCreateMsg(
	    boolean recurring) throws Exception {
		String startTimeStr = "2010-04-01T14:30:00";
		String rate = "100";
		String duration = "30";
		String sourceTna = "OME0039_OC48-1-5-1";
		String targetTna = "OME0237_OC48-1-5-1";
		String billingGroup = "SystemAdminGroup";
		String sourceResourceGroup = "SystemAdminResourceGroup";
		String targetResourceGroup = "SystemAdminResourceGroup";
		String sourceUserGroup = "SystemAdminGroup";
		String targetUserGroup = "SystemAdminGroup";
		String scheduleName = "Junit-Testing";
		// Leave null for channel auto selection
		// String sourceChannelString = null;
		// String targetChannelString = null;

		// Optional
		String endsAfterDateStr = "2010-04-08T14:30:00-04:00";
		String protectionTypeStr = "Unprotected";
		String srlgStr = "22,23,24";
		String serviceIdExclusionStr = "SERVICE-1239300664133";
		String sourceVlanIdStr = "1111";
		String targetVlanIdStr = "2222";

		// -------------------------------------------
		// Create a manual schedule ...
		// -------------------------------------------

		CreateReservationScheduleRequestDocument createReservationScheduleRequestDoc = CreateReservationScheduleRequestDocument.Factory
		    .newInstance();
		CreateReservationScheduleRequestDocument.CreateReservationScheduleRequest createReservationScheduleRequest = createReservationScheduleRequestDoc
		    .addNewCreateReservationScheduleRequest();
		ReservationScheduleRequestT reservationSchedule = createReservationScheduleRequest
		    .addNewReservationSchedule();
		Calendar reqStartTime = Calendar.getInstance();
		reqStartTime.setTimeInMillis(new XmlCalendar(startTimeStr)
		    .getTimeInMillis());

		reservationSchedule.setStartTime(reqStartTime);
		reservationSchedule.setReservationOccurrenceDuration(Integer
		    .parseInt(duration));
		reservationSchedule.setName(scheduleName);
		// reservation schedule types:
		// - RESERVATION_SCHEDULE_AUTOMATIC
		// - RESERVATION_SCHEDULE_MANUAL
		// - PRE_RESERVATION_SCHEDULE_AUTOMATIC
		// - PRE_RESERVATION_SCHEDULE_MANUAL
		reservationSchedule
		    .setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_MANUAL);

		// resource
		// reservationSchedule.addNewResource();

		// Service path data
		PathRequestT pathReq = PathRequestT.Factory.newInstance();

		/*
		 * Set methods on the generated bean: void setSourceTna(java.lang.String
		 * sourceTna); void setTargetTna(java.lang.String targetTna); void
		 * setSourceChannel(int sourceChannel); void setNilSourceChannel(); void
		 * setTargetChannel(int targetChannel); void setNilTargetChannel(); void
		 * setVlanId(java.lang.String vlanId); void setNilVlanId(); void setRate(int
		 * rate); void setSrlgExclusions(java.lang.String srlgExclusions); void
		 * setNilSrlgExclusions(); void setCost(int cost); void setNilCost(); void
		 * setMetric(int metric); void setNilMetric(); void setHop(int hop); void
		 * setNilHop(); void setRoutingMetric(int routingMetric); void
		 * setNilRoutingMetric(); void setRoutingAlgorithm(java.lang.String
		 * routingAlgorithm); void setNilRoutingAlgorithm(); void
		 * setProtectionType(com
		 * .nortel.www.drac._2007._07._03.ws.resourceallocationandschedulingservicetypes
		 * .ValidProtectionTypeT.Enum protectionType); void setNilProtectionType();
		 * void setSharedRiskReservationOccurrenceGroup(java.lang.String
		 * sharedRiskReservationOccurrenceGroup); void
		 * setNilSharedRiskReservationOccurrenceGroup();
		 */

		pathReq.setSourceTna(sourceTna);
		pathReq.setTargetTna(targetTna);
		// if (sourceChannelString != null)
		// {
		// pathReq.setSourceChannel(Integer.parseInt(sourceChannelString));
		// }
		// if (targetChannelString != null)
		// {
		// pathReq.setTargetChannel(Integer.parseInt(targetChannelString));
		// }

		pathReq.setRate(Integer.parseInt(rate));
		pathReq.setSourceVlanId(sourceVlanIdStr);
		pathReq.setTargetVlanId(targetVlanIdStr);

		ValidProtectionTypeT.Enum type = ValidProtectionTypeT.Enum
		    .forString(protectionTypeStr);
		pathReq.setProtectionType(type);

		// Shared Risk Link Group exclusion
		pathReq.setSrlgExclusions(srlgStr);

		// serviceId exclusion
		pathReq.setSharedRiskReservationOccurrenceGroup(serviceIdExclusionStr);
		reservationSchedule.setPath(pathReq);

		// userInfo
		reservationSchedule.addNewUserInfo();
		UserInfoT userInfo = UserInfoT.Factory.newInstance();
		userInfo.setBillingGroup(billingGroup);
		userInfo.setSourceEndpointResourceGroup(sourceResourceGroup);
		userInfo.setTargetEndpointResourceGroup(targetResourceGroup);
		userInfo.setSourceEndpointUserGroup(sourceUserGroup);
		userInfo.setTargetEndpointUserGroup(targetUserGroup);
		reservationSchedule.setUserInfo(userInfo);

		// Recurrence...
		if (recurring) {
			reservationSchedule.setIsRecurring(true);
			RecurrenceT recurrence = RecurrenceT.Factory.newInstance();

			// set the desired recurrence (from examples above):
			recurrence.setType(ValidRecurrenceTypeT.DAILY_RECURRENCE_T);

			// set recurrence range
			Calendar reqEndTime = Calendar.getInstance();
			reqEndTime.setTimeInMillis(new XmlCalendar(endsAfterDateStr)
			    .getTimeInMillis());
			EndsAfterDateT endsAfterDate = EndsAfterDateT.Factory.newInstance();
			endsAfterDate.setEndDate(reqEndTime);
			recurrence.setRange(endsAfterDate);
			reservationSchedule.setRecurrence(recurrence);
		}
		else {
			reservationSchedule.setIsRecurring(false);
		}

		return createReservationScheduleRequestDoc;
	}

	private String xmlToString(XmlTokenSource o) {
		try {
			StringWriter sw = new StringWriter();
			XmlOptions op = new XmlOptions();
			op.setSavePrettyPrint();
			o.save(sw, op);
			return sw.toString();
		}
		catch (IOException e) {
			log.error("Error: ", e);
			return "?";
		}

	}
}
