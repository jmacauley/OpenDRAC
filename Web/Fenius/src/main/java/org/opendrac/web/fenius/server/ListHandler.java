/**
 * Copyright (c) 2010, SURFnet bv, The Netherlands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
 * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */

package org.opendrac.web.fenius.server;

// Standard Java imports.
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

// Third-party imports.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// DRAC specific imports.
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;

// GLIF Fenius imports.
import is.glif.fenius.ws.connection.v1.types.ListFilter;
import is.glif.fenius.ws.connection.v1._interface.ListReq;
import is.glif.fenius.ws.connection.v1._interface.ListResp;
import is.glif.fenius.ws.connection.v1.service.InternalFault;
import is.glif.fenius.ws.connection.v1.service.ExternalFault;
import is.glif.fenius.ws.connection.v1.types.Reservation;

// Local imports.
import org.opendrac.web.fenius.utilities.DateUtils;
import org.opendrac.web.fenius.utilities.MessageDump;
import org.opendrac.web.fenius.utilities.SecurityContext;


/**
 *
 * @author hacksaw
 */
public class ListHandler extends FeniusHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 *
	 * @param listReq
	 * @return
	 * @throws InternalFault
	 * @throws ExternalFault
	 */
	public ListResp process(ListReq listReq) throws InternalFault, ExternalFault {
		// Base for our error string.
		is.glif.fenius.ws.connection.v1.faults.ExternalFault externalFault =
				new is.glif.fenius.ws.connection.v1.faults.ExternalFault();
		StringBuilder error = new StringBuilder("OpenDRAC: ListHandler.process ");
		error.append(DateUtils.now());
		error.append(" - ");

		SecurityContext securityContext = this.getSecurityContext();
		if (securityContext == null || listReq == null) {
			// Build the error string.
			error.append("operation failed due to null parameter.");
			String errMsg = error.toString();
			externalFault.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, externalFault);
		}

		// Log the incoming message.
		if (logger.isDebugEnabled()) {
			logger.debug("ListHandler.process: Incoming message ["
					+ securityContext.getUserName() + " @ "
					+ securityContext.getRemoteAddr() + "]");
			logger.debug(MessageDump.dump(ListReq.class, listReq));
		}

		// The list filter is ANDed so if reservationId is present we will
		// need to process this first due to the limited query capabilities of
		// the RequestHandler interface.
		List<Schedule> schList = null;

		// We need to set our default time criteria for filtering. If no
		// startTime provided then specify zero hour.  If no endTime is
		// provided then specify the end of Java time.
		Long startTime = 0L;

		Calendar cal = Calendar.getInstance();
		cal.set(2999, 11, 31, 0, 0);
		Long endTime = cal.getTimeInMillis();

		// If no filter provided then return all reservations for this user.
		ListFilter listFilter = listReq.getListFilter();
		if (listFilter == null) {
			schList = querySchedulesByUserGroups(startTime, endTime);
		} else {
			// We will process specific reservation ids first.
			List<String> reservationIds = listFilter.getReservationId();
			if (reservationIds != null && !reservationIds.isEmpty()) {
				schList = querySchedulesByReservationIds(reservationIds);
			} else {
				// We need to bound our query using start and end dates.
				XMLGregorianCalendar xmlCal = listFilter.getStartsAfter();
				if (xmlCal != null) {
					startTime = xmlCal.toGregorianCalendar().getTimeInMillis();
				}

				xmlCal = listFilter.getStartsBefore();
				if (xmlCal != null) {
					endTime = xmlCal.toGregorianCalendar().getTimeInMillis();
				}

				schList = querySchedulesByUserGroups(startTime, endTime);
			}
		}

		// Now apply additional filter criteria.
		ListResp resp = new ListResp();
		if (schList == null || schList.isEmpty()) {
			logger.debug("ListHandler.process: no matching schedules - return empty results");
		} else {
			// Calculate additional search criteria.
			cal.set(2999, 11, 31, 0, 0);
			Long endBeforeTime = cal.getTimeInMillis();
			Long endAfterTime = 0L;

			if (listFilter != null) {
				XMLGregorianCalendar xmlCal = listFilter.getEndsAfter();
				if (xmlCal != null) {
					endAfterTime = xmlCal.toGregorianCalendar().getTimeInMillis();
				}

				xmlCal = listFilter.getEndsBefore();
				if (xmlCal != null) {
					endBeforeTime = xmlCal.toGregorianCalendar().getTimeInMillis();
				}
			}

			// Due to a bug in DRAC we are missing some key schedule information.
			ReservationHelper helper = new ReservationHelper();
			ListIterator<Schedule> scheduleList = schList.listIterator();
			while (scheduleList.hasNext()) {
				Schedule sch = (Schedule) scheduleList.next();

				if (sch.getEndTime() > endAfterTime
						&& sch.getEndTime() < endBeforeTime) {
					Reservation result = helper.mapScheduleToReservation(sch);
					resp.getReservations().add(result);

					logger.debug("ListHandler.process: returning reservation for id = "
							+ result.getReservationId());
				}
			}
		}

		return resp;
	}

	private List<Schedule> querySchedulesByReservationIds(List<String> reservationIds) {
		List<Schedule> results = new ArrayList<Schedule>();

		RequestHandler rh = RequestHandler.INSTANCE;
		ListIterator<String> reservationIdList = reservationIds.listIterator();
		while (reservationIdList.hasNext()) {
			// We can't currently distinguish between a true NRB_PORT error and a
			// simple case of the schedule not existing that matches the
			// provided reservationId.
			String reservationId = (String) reservationIdList.next();
			Schedule sch = null;
			try {
				sch = rh.querySchedule(this.getSecurityContext().getUserToken(), reservationId);
			} catch (Exception e) {
				// Build the error string.
				StringBuilder error = new StringBuilder("ListHandler.querySchedulesByReservationIds:  querySchedule operation failed with exception - ");
				error.append(e.toString());
				logger.debug(error.toString());
			}

			if (sch != null) {
				results.add(sch);
				logger.debug("ListHandler.querySchedulesByReservationIds: found matching schedule - " + sch.toDebugString());
			}
		}
		return results;
	}

	private List<Schedule> querySchedulesByUserGroups(long startTime, long endTime)
			throws InternalFault, ExternalFault {
		// Base for our error string.
		is.glif.fenius.ws.connection.v1.faults.InternalFault internalFault =
				new is.glif.fenius.ws.connection.v1.faults.InternalFault();
		StringBuilder error = new StringBuilder("OpenDRAC: ListHandler.querySchedulesByUserGroups ");
		error.append(DateUtils.now());
		error.append(" - ");

		// Restrict schedules to only those in the fenius user groups.
		RequestHandler rh = RequestHandler.INSTANCE;

		// Take these user groups and query all applicable schedules.
		List<Schedule> results = null;
		try {
			results = rh.querySchedules(
					this.getSecurityContext().getUserToken(),
					startTime,
					endTime,
					this.getSecurityContext().getUserGroups());
		} catch (Exception e) {
			// Build the error string.
			error.append("querySchedules operation failed with exception - ");
			error.append(e.toString());
			String errMsg = error.toString();
			internalFault.setMessage(errMsg);
			logger.error(errMsg);
			throw new InternalFault(errMsg, internalFault);
		}

		return results;
	}
}