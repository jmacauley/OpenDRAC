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

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

//Third-party imports.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// DRAC specific imports.
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;

// GLIF Fenius imports.
import is.glif.fenius.ws.connection.v1._interface.ReserveReq;
import is.glif.fenius.ws.connection.v1._interface.ReserveResp;
import is.glif.fenius.ws.connection.v1.service.InternalFault;
import is.glif.fenius.ws.connection.v1.service.ExternalFault;
import is.glif.fenius.ws.connection.v1.types.Advance;
import is.glif.fenius.ws.connection.v1.types.Avp;
import is.glif.fenius.ws.connection.v1.types.AvpSeq;
import is.glif.fenius.ws.connection.v1.types.Credentials;
import is.glif.fenius.ws.connection.v1.types.Edge;
import is.glif.fenius.ws.connection.v1.types.Immediate;
import is.glif.fenius.ws.connection.v1.types.Reservable;
import is.glif.fenius.ws.connection.v1.types.Reservation;
import is.glif.fenius.ws.connection.v1.types.Topology;
import is.glif.fenius.ws.connection.v1.types.Vertex;
import is.glif.fenius.ws.connection.v1.types.ReservationStatus;

// Local imports.
import org.opendrac.web.fenius.utilities.DateUtils;
import org.opendrac.web.fenius.utilities.MessageDump;
import org.opendrac.web.fenius.utilities.SecurityContext;

/**
 * {@link ReserveHandler} handles processing of the Fenius reserve operation.
 *
 * <p>This class maps the Fenius reserve operation through to equivalent
 * OpenDRAC capabilities.  Many of the Fenius reserve parameters do not map
 * through to equivalent OpenDRAC parameters so they may be dropped.  In
 * addition we have an issue in that the Fenius reserve assumes a schedule
 * entry is created when processing starts, while OpenDRAC does not create one
 * internally until the schedule has been successfully routed and reserved.
 *
 * @author hacksaw
 */
public class ReserveHandler extends FeniusHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 *
	 * @param reserveReq
	 * @return
	 * @throws InternalFault
	 * @throws ExternalFault
	 */
	public ReserveResp process(ReserveReq reserveReq)
			throws InternalFault, ExternalFault {
		// Base for our error string.
		is.glif.fenius.ws.connection.v1.faults.ExternalFault externalFault =
				new is.glif.fenius.ws.connection.v1.faults.ExternalFault();

		StringBuilder error = new StringBuilder("OpenDRAC: ReserveHandler.process() ");
		error.append(DateUtils.now());
		error.append(" - ");

		SecurityContext securityContext = this.getSecurityContext();
		if (securityContext == null || reserveReq == null) {
			// Build the error string.
			error.append("operation failed due to null parameter.");
			String errMsg = error.toString();
			externalFault.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, externalFault);
		}

		// Log the incoming message.
		if (logger.isDebugEnabled()) {
			logger.error("ReserveHandler.process: Incoming message ["
					+ securityContext.getUserName() + " @ "
					+ securityContext.getRemoteAddr() + "]");
			logger.error(MessageDump.dump(ReserveReq.class, reserveReq));
		}

		// We are authenticated so let us get down to business.  Map the Fenius
		// reserve request through to a DRAC schedule request.
		Schedule sch = mapReserveReq(reserveReq);
		if (logger.isDebugEnabled()) {
			logger.debug(sch.toDebugString());
		}

		// Issue the schedule request to the NRB_PORT.
		RequestHandler rh = RequestHandler.INSTANCE;
		String schId = null;
		try {
			schId = rh.createScheduleAsync(securityContext.getUserToken(), sch);
		} catch (Exception e) {
			// Build the error string.
			error.append("OpenDRAC schedule operation failed - ");
			error.append(e.toString());
			String errMsg = error.toString();
			externalFault.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, externalFault);
		}

		logger.debug("ReserveHandler.process: schedule created with id = " + schId);

		// Map the Schedule we created to avoid asynchonous schedule
		// creation timing issues or just in case the reservation fails before
		// we can query it.
		ReservationHelper helper = new ReservationHelper();
		Reservation result = helper.mapScheduleToReservation(sch);

		// Set the returned scheduleId.
		result.setReservationId(schId);

		// Mark <reservation> element as being in the RESERVING state.
		result.setReservationStatus(ReservationStatus.RESERVING);

		ReserveResp resp = new ReserveResp();
		resp.setReservation(result);

		logger.debug("ReserveHandler.process: sleeping for 3 seconds.");

		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			logger.error("main: Thread exception " + e);
		}

		logger.debug("ReserveHandler.process: returning reservation for id = " + schId);

		return resp;
	}

	private Schedule mapReserveReq(ReserveReq req) throws ExternalFault {
		// Base for our error string.
		is.glif.fenius.ws.connection.v1.faults.ExternalFault faultInfo =
				new is.glif.fenius.ws.connection.v1.faults.ExternalFault();
		StringBuilder error = new StringBuilder("OpenDRAC: ReserveHandler.mapReserveReq() ");
		error.append(DateUtils.now());
		error.append(" - ");

		// Fenius requests are always immediate commit and automatic activation.
		Schedule sch = new Schedule();
		sch.setActivationType(Schedule.ACTIVATION_TYPE.RESERVATION_AUTOMATIC);

		// Map the user credentials through to a DRAC UserInfo.
		mapCredentials(sch, req.getCredentials());

		// Get <reservationParams>.
		Reservable res = req.getReservationParams();

		if (res == null) {
			error.append(" - operation failed due to null reservationParams parameter.");
			String errMsg = error.toString();
			faultInfo.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, faultInfo);
		}

		// Populate the Fenius <description> into the DRAC schedule name.
		mapDescription(sch, res.getDescription());

		// If the Fenius <tag> element is present we cannot map it.
		if (res.getTag() != null) {
			logger.debug("ReserveHandler.mapReserveReq(): <tag> element present but not mapped.");
		}

		// Map the Fenius <schedule> element through to a DRAC Schedule object.
		mapSchedule(sch, res.getSchedule());

		// Map the Fenius <topology> element through to DRAC endpoints.
		mapTopology(sch, res.getTopology());

		// Map the Fenius <adhocParams> element.
		// res.getAdhocParams();

		return sch;
	}

	private void mapCredentials(Schedule out, List<Credentials> in) {

		SecurityContext context = this.getSecurityContext();
		UserType userType = new UserType();
		userType.setUserId(context.getUserName());
		userType.setBillingGroup(context.getUserGroups().listIterator().next());

		// @TODO: Lookup the resource group that contains the endpoint.  This here
		// is a hack.
		userType.setSourceEndpointResourceGroup("FeniusResourceGroup");
		userType.setTargetEndpointResourceGroup("FeniusResourceGroup");
		userType.setSourceEndpointUserGroup("FeniusUserGroup");
		userType.setTargetEndpointUserGroup("FeniusUserGroup");

		// Credentials is an optional element.
		if (in != null) {
			ListIterator<Credentials> credList = in.listIterator();
			while (credList.hasNext()) {
				Credentials cred = (Credentials) credList.next();
				if (cred.getUsername() != null && !cred.getUsername().isEmpty()) {
					// OpenDRAC only supports a single billing userId.
					userType.setUserId(cred.getUsername());
				}

				if (cred.getGroup() != null && !cred.getGroup().isEmpty()) {
					// OpenDRAC only supports a single billing group.
					userType.setBillingGroup(new UserGroupName(cred.getGroup()));
				}

				logger.debug("ReserveHandler.mapCredentials(): request for username = \""
						+ cred.getUsername() + "\" group = \"" + cred.getGroup() + "\"");
			}
		}

		out.setUserInfo(userType);
	}

	private void mapDescription(Schedule out, List<String> in) {

		StringBuilder name = new StringBuilder();

		// Credentials is an optional element.
		if (in != null) {
			ListIterator<String> nameList = in.listIterator();
			while (nameList.hasNext()) {
				String nameString = (String) nameList.next();
				if (nameString != null && !nameString.isEmpty()) {
					name.append(nameString);
					if (nameList.hasNext()) {
						name.append(" : ");
					}
				}

				logger.debug("ReserveHandler.mapDescription(): "
						+ nameString);
			}
		}

		out.setName(name.toString());
	}

	private void mapSchedule(Schedule out, is.glif.fenius.ws.connection.v1.types.Schedule in)
			throws ExternalFault {
		// Base for our error string.
		is.glif.fenius.ws.connection.v1.faults.ExternalFault faultInfo =
				new is.glif.fenius.ws.connection.v1.faults.ExternalFault();
		StringBuilder error = new StringBuilder("DRAC: ReservationHelper.mapSchedule() ");
		error.append(DateUtils.now());
		error.append(" - ");

		if (in == null || out == null) {
			error.append("null input <schedule> parameter provided.");
			String errMsg = error.toString();
			faultInfo.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, faultInfo);
		}

		// The Fenius <created> element is not mapped through.
		XMLGregorianCalendar created = in.getCreated();
		if (created != null) {
			logger.debug("ReserveHandler.mapSchedule(): <created> element present but not mapped.");
			logger.debug("ReserveHandler.mapSchedule(): " + created.toString());
		}

		// Fenius schedules are single instance.
		out.setRecurring(false);

		// Choice of <immediate> or <advance> reservation.
		Immediate im = in.getImmediate();
		Advance ad = in.getAdvance();

		if (im != null) {
			// We have an <immediate> element so start time is now.
			out.setStartTime(new Date().getTime());

			// Choice of <duration> or <end>.
			if (im.getDuration() != null) {
				int duration = im.getDuration().intValue();

				// A negative value indicates an infinite schedule.
				if (duration < 0) {
					// Instead of a negative value indicating infinite the boys
					// stuck it as far in the future as supported by Java.
					Calendar cal = Calendar.getInstance();
					cal.set(2999, 11, 31, 0, 0);
					out.setEndTime(cal.getTimeInMillis());
                    out.setDuration(out.getEndTime() - out.getStartTime());
				} else {
					// Fenius duration is in seconds while DRAC is in milliseconds.
					out.setDuration(duration * 1000);
                    out.setEndTime(out.getStartTime() + out.getDurationLong());
				}
			} else if (im.getEnd() != null) {
				// End date was provided to convert to milliseconds.
				GregorianCalendar end = im.getEnd().toGregorianCalendar();
				out.setEndTime(end.getTimeInMillis());
				out.setDuration(out.getEndTime() - out.getStartTime());
			} else {
				// This should never occur so let us throw an exception.
				error.append(" - operation failed no reservation duration or end provided.");
				String errMsg = error.toString();
				faultInfo.setMessage(errMsg);
				logger.error(errMsg);
				throw new ExternalFault(errMsg, faultInfo);
			}
		} else if (ad != null) {
			logger.debug("ReserveHandler.mapSchedule(): advance schedule.");

			// We have an <advance> element.
			if (ad.getStart() != null) {
				// Start date is provided so we convert to milliseconds.
				GregorianCalendar start = ad.getStart().toGregorianCalendar();
				out.setStartTime(start.getTimeInMillis());

				logger.debug("ReserveHandler.mapSchedule(): start time " + out.getStartTime());
			}
            else {
                out.setStartTime(new Date().getTime());
            }

			// Choice of <duration> or <end>.
			if (ad.getDuration() != null) {
				int duration = ad.getDuration().intValue();

				logger.debug("ReserveHandler.mapSchedule(): duration " + duration);

				// A negative value indicates an infinite schedule.
				if (duration < 0) {
					// Instead of a negative value indicating infinite the boys
					// stuck it as far in the future as supported by Java.
					Calendar cal = Calendar.getInstance();
					cal.set(2999, 11, 31, 0, 0);
					out.setEndTime(cal.getTimeInMillis());
                    out.setDuration(out.getEndTime() - out.getStartTime());
				} else {
					// Fenius duration is in seconds while DRAC is in milliseconds.
					out.setDuration(duration * 1000);
					out.setEndTime(out.getStartTime() + out.getDurationLong());
				}
			} else if (ad.getEnd() != null) {
				// End date was provided to convert to milliseconds.
				GregorianCalendar end = ad.getEnd().toGregorianCalendar();
				out.setEndTime(end.getTimeInMillis());
                out.setDuration(out.getEndTime() - out.getStartTime());
				logger.debug("ReserveHandler.mapSchedule(): end date " + out.getEndTime());
			} else {
				// This should never occur so let us throw an exception.
				error.append(" - operation failed no reservation duration or end provided.");
				String errMsg = error.toString();
				faultInfo.setMessage(errMsg);
				logger.error(errMsg);
				throw new ExternalFault(errMsg, faultInfo);
			}
		}
	}

	private void mapTopology(Schedule out, Topology in)
			throws ExternalFault {
		// Base for our error string.
		is.glif.fenius.ws.connection.v1.faults.ExternalFault faultInfo =
				new is.glif.fenius.ws.connection.v1.faults.ExternalFault();
		StringBuilder error = new StringBuilder("DRAC: ReserveHandler.mapTopology() ");
		error.append(DateUtils.now());
		error.append(" - ");

		if (in == null || out == null) {
			error.append("null input parameter provided.");
			String errMsg = error.toString();
			faultInfo.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, faultInfo);
		}

		// Fail this operation if a directed graph request.
		if (in.isDirected()) {
			// Build the error string.
			error.append("directed graphs not supported.");
			String errMsg = error.toString();
			faultInfo.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, faultInfo);
		}

		// The Fenius <vertices> and <edges> map through to a DRAC PathType.
		PathType path = new PathType();

		// Fenius services are unprotected and ccat.
		path.setProtectionType(PathType.PROTECTION_TYPE.UNPROTECTED);
		path.setVcatRoutingOption(false);

		// At the moment we only support a single endpoint pair in a schedule.
		// We may want to support a single schedule with multiple services and
		// different endpoints.
		EndPointType sourceEndpoint = new EndPointType();
		EndPointType targetEndpoint = new EndPointType();

		// Fenius <vertices> identify the endpoint involved in the request.
		HashMap<String, Vertex> endpointMap = new HashMap<String, Vertex>();
		endpointMap = getVertexMap(in.getVertices());

		// Fenius <edges> identify the bandwidth between endpoints.
		List<Edge> edges = in.getEdges();
		if (edges != null) {
			ListIterator<Edge> edgeList = edges.listIterator();
			while (edgeList.hasNext()) {
				Edge edge = (Edge) edgeList.next();
				if (edge != null) {
					logger.debug("ReserveHandler.mapTopology():"
							+ " edgeId = " + edge.getEdgeId()
							+ " a = " + edge.getA()
							+ " z = " + edge.getZ()
							+ " bandwidth = " + edge.getBandwidth());

					// Get assoicated <bandwidth> in mb/s.
					if (edge.getBandwidth() == null) {
						// No <bandwidth> specified - Build the error string.
						error.append("no bandwidth specified for edge ");
						error.append(edge.getA());
						error.append("/");
						error.append(edge.getZ());
						String errMsg = error.toString();
						faultInfo.setMessage(errMsg);
						logger.error(errMsg);
						throw new ExternalFault(errMsg, faultInfo);
					}

					out.setRate(edge.getBandwidth().intValue());
					path.setRate(edge.getBandwidth().intValue());

					sourceEndpoint.setName(getResourceId(endpointMap, edge.getA()));
					sourceEndpoint.setChannelNumber(-1);
					path.setSrcVlanId(getTechParam(endpointMap, edge.getA(), "eth.vlanTag"));

					targetEndpoint.setName(getResourceId(endpointMap, edge.getZ()));
					targetEndpoint.setChannelNumber(-1);
					path.setDstVlanId(getTechParam(endpointMap, edge.getZ(), "eth.vlanTag"));
				}
			}
		}

		path.setSourceEndPoint(sourceEndpoint);
		path.setTargetEndPoint(targetEndpoint);
		out.setPath(path);
		logger.debug("ReserveHandler.mapTopology(): completed.");
	}

	private HashMap<String, Vertex> getVertexMap(List<Vertex> vertices) {

		HashMap<String, Vertex> endpointMap = new HashMap<String, Vertex>();

		if (vertices != null) {
			ListIterator<Vertex> vertexList = vertices.listIterator();
			while (vertexList.hasNext()) {
				Vertex vertex = (Vertex) vertexList.next();
				if (vertex != null) {
					endpointMap.put(vertex.getVertexId(), vertex);

					logger.debug("ReserveHandler.getVertexMap(): vertexId = "
							+ vertex.getVertexId() + " resourceId = "
							+ vertex.getResourceId());
				}
			}
		}

		return endpointMap;
	}

	private String getResourceId(HashMap<String, Vertex> endpointMap, String endpoint) {
		if (endpointMap != null) {
			Vertex vertex = endpointMap.get(endpoint);
			if (vertex != null) {
				return vertex.getResourceId();
			}
		}

		return null;
	}

	private String getTechParam(HashMap<String, Vertex> endpointMap, String endpoint, String name) {
		if (endpointMap != null) {
			Vertex vertex = endpointMap.get(endpoint);
			if (vertex != null) {
				return getAttrVal(vertex.getTechParams(), name);
			}
		}

		return null;
	}

	private String getAttrVal(AvpSeq avpSeq, String name) {
		if (avpSeq != null) {
			ListIterator<Avp> avpList = avpSeq.getAvp().listIterator();
			while (avpList.hasNext()) {
				Avp avp = (Avp) avpList.next();
				if (avp.getAttrName().equalsIgnoreCase(name)) {
					return avp.getAttrVal();
				}
			}
		}

		return null;
	}
}
