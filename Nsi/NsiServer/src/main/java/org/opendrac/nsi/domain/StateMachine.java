/**
 * Copyright (c) 2011, SURFnet bv, The Netherlands
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
package org.opendrac.nsi.domain;

import static javax.persistence.GenerationType.*;

import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.security.NsaSecurityContext;
import org.opendrac.nsi.security.SessionSecurity;
import org.opendrac.nsi.util.ExceptionCodes;
import org.opendrac.nsi.util.MessageDump;

/**
 *
 * @author hacksaw
 * @author annotated by robert
 */
@Entity
@Table(name = "NSI_STATE_MACHINES"/**
 * , catalog = "drac" , uniqueConstraints = {
 *
 * @UniqueConstraint(columnNames = ""),
 * @UniqueConstraint(columnNames = "") }
 */
)
public class StateMachine {

	// Original security content of the requesterNSA.
	@OneToOne(cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn
	private NsaSecurityContext nsaSecurityContext = null;

	// /////////////////////////////////////////////////////
	// Store all the parameters relating to the schedule.
	// /////////////////////////////////////////////////////

	// Requester NSA for this reservation.
	@Column(name = "REQUESTER_NSA", unique = false, nullable = false, length = 255)
	private String requesterNSA = null;

	// This will be my NSA.
	@Column(name = "PROVIDER_NSA", unique = false, nullable = false, length = 255)
	private String providerNSA = null;

	// Global Id of the reservation which may be null.
	@Column(name = "GLOBAL_RESERVATION_ID", unique = true, nullable = false, length = 255)
	private String globalReservationId = null;

	// Reservation description.
	@Column(name = "DESCRIPTION", unique = false, nullable = false, length = 255)
	private String description = null;

	/**
	 * Connection id for the reservation is unique, but there have been
	 * discussions that it is only unique in the context of the requesting NSA. We
	 * save time and assume it is unique. This will also be the primary key used
	 * in the storage structure
	 */
	@Column(name = "CONNECTION_ID", unique = true, nullable = false, length = 255)
	private String connectionId = null;

	// replyTo field for forcedEnd messages (my be moed to topology).
	@Column(name = "REPLY_TO", unique = false, nullable = false, length = 255)
	private String replyTo = null;

	// State of the reservation.
	@Enumerated(EnumType.STRING)
	@Column(name = "CURRENT_STATE", unique = false, nullable = false, length = 50)
	private ConnectionStateType currentState = ConnectionStateType.INITIAL;

	// User credentials associated with the reservation.
	@OneToOne(cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn
	private SessionSecurity sessionSecurity = null;

	// The original request service parameters.
	@Column(name = "SERVICE_PARAMETERS", unique = false, nullable = false, length = 4096)
	private ServiceParametersType serviceParameters = null;

	// Original path information.
	@Column(name = "PATH_TYPE", unique = false, nullable = false, length = 4096)
	private PathType path = null;

	// //////////////////////////////////////////////////
	// The reservation parameters to which we committed.
	// //////////////////////////////////////////////////

	// The date and time of the reservation.
	@Column(name = "START_TIME", unique = false, nullable = false, length = 255)
	private GregorianCalendar startTime = null;

	@Column(name = "END_TIME", unique = false, nullable = false, length = 255)
	private GregorianCalendar endTime = null;

	// Use only desired bandwidth for now.
	@Column(name = "DESIRED_BANDWIDTH", unique = false, nullable = false, length = 255)
	private int desiredBandwidth = -1;

	@Column(name = "MINIMUM_BANDWIDTH", unique = false, nullable = false, length = 255)
	private Integer minimumBandwidth = null;

	@Column(name = "MAXIMUM_BANDWIDTH", unique = false, nullable = false, length = 255)
	private Integer maximumBandwidth = null;

	// The resolved path list we will use for this reservation.
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@PrimaryKeyJoinColumn
	private List<PathSegment> routePathList = null;

	// Id column, needed for persistence
	@Id
	@GeneratedValue(strategy = TABLE, generator = "nsi_sequences")
	@TableGenerator(name = "nsi_sequences", table = "NSI_SEQUENCES", allocationSize = 1)
	@Column(name = "ID", unique = true, nullable = false)
	private int id;

	@Transient
	private final DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);

    @Transient
	private final DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL);


	/**
	 * @return the globalReservationId
	 */
	public String getGlobalReservationId() {
		return globalReservationId;
	}

	/**
	 * @param globalReservationId
	 *          the globalReservationId to set
	 */
	public void setGlobalReservationId(String globalReservationId) {
		this.globalReservationId = globalReservationId;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *          the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the connectionId
	 */
	public String getConnectionId() {
		return connectionId;
	}

	/**
	 * @param connectionId
	 *          the connectionId to set
	 */
	public void setConnectionId(String connectionId) throws ServiceException {
		if (connectionId == null || connectionId.isEmpty()) {
			throw ExceptionCodes.buildProviderException(
			    ExceptionCodes.MISSING_PARAMETER, "connectionId", "<null>");
		}
		this.connectionId = connectionId;
	}

	/**
	 * @return the requesterNSA
	 */
	public String getRequesterNSA() {
		return requesterNSA;
	}

	/**
	 * @param requesterNSA
	 *          the requesterNSA to set
	 */
	public void setRequesterNSA(String requesterNSA) {
		this.requesterNSA = requesterNSA;
	}

	/**
	 * @return the replyTo
	 */
	public String getReplyTo() {
		return replyTo;
	}

	/**
	 * @param replyTo
	 *          the replyTo to set
	 */
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	/**
	 * @return the currentState
	 */
	public synchronized ConnectionStateType getCurrentState() {
		return currentState;
	}

	/**
	 * @param currentState
	 *          the currentState to set
	 */
	public synchronized void setCurrentState(ConnectionStateType currentState) {
		this.currentState = currentState;
	}

    /**
     * Set the state machine state and all child path segments to newState if
     * in currentState.
     *
     * @param currentState
     *          the currentState to set
     */
    public synchronized void transitionState(ConnectionStateType currentState,
            ConnectionStateType newState) {
    if (this.currentState == currentState) {
        this.currentState = newState;
    }

    if (routePathList != null) {
                    for (PathSegment segment : routePathList) {
            if (segment.getCurrentState() == currentState) {
                segment.setCurrentState(newState);
            }
                    }
            }
    }

    /**
     * Set the state machine state and all child path segments to newState
     * independent of current state..
     *
	 * @param currentState
	 *          the currentState to set
	 */
	public synchronized void transitionState(ConnectionStateType newState) {
        this.currentState = newState;

        if (routePathList != null) {
			for (PathSegment segment : routePathList) {
                    segment.setCurrentState(newState);
			}
		}
	}

	/**
	 * @return the sessionSecurity
	 */
	public SessionSecurity getSessionSecurity() {
		return sessionSecurity;
	}

	/**
	 * @param globalUserName
	 *          the globalUserName to set
	 */
	public void setSessionSecurity(SessionSecurity sessionSecurity) {
		this.sessionSecurity = sessionSecurity;
	}

	/**
	 * @return the serviceParameters
	 */
	public ServiceParametersType getServiceParameters() {
		return serviceParameters;
	}

	/**
	 * @param serviceParameters
	 *          the serviceParameters to set
	 */
	public void setServiceParameters(ServiceParametersType serviceParameters) {
		this.serviceParameters = serviceParameters;
	}

	/**
	 * @return the path
	 */
	public PathType getPath() {
		return path;
	}

	/**
	 * @param psth
	 *          the path to set
	 */
	public void setPath(PathType path) {
		this.path = path;
	}

	/**
	 * @return the startTime
	 */
	public GregorianCalendar getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *          the startTime to set
	 */
	public void setStartTime(GregorianCalendar startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public GregorianCalendar getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *          the endTime to set
	 */
	public void setEndTime(GregorianCalendar endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the desiredBandwidth
	 */
	public int getDesiredBandwidth() {
		return desiredBandwidth;
	}

	/**
	 * @param desiredBandwidth
	 *          the desiredBandwidth to set
	 */
	public void setDesiredBandwidth(Integer desiredBandwidth) {
		this.desiredBandwidth = desiredBandwidth;
	}

	/**
	 * @return the minimumBandwidth
	 */
	public Integer getMinimumBandwidth() {
		return minimumBandwidth;
	}

	/**
	 * @param minimumBandwidth
	 *          the minimumBandwidth to set
	 */
	public void setMinimumBandwidth(Integer minimumBandwidth) {
		this.minimumBandwidth = minimumBandwidth;
	}

	/**
	 * @return the maximumBandwidth
	 */
	public Integer getMaximumBandwidth() {
		return maximumBandwidth;
	}

	/**
	 * @param maximumBandwidth
	 *          the maximumBandwidth to set
	 */
	public void setMaximumBandwidth(Integer maximumBandwidth) {
		this.maximumBandwidth = maximumBandwidth;
	}

	/**
	 * @return the pathList
	 */

	public List<PathSegment> getRoutePathList() {
		return routePathList;
	}

	/**
	 * @param pathList
	 *          the pathList to set
	 */
	public void setRoutePathList(List<PathSegment> routePathList) {
		this.routePathList = routePathList;
	}

	/**
	 * @return the nsaSecurityContext
	 */
	public NsaSecurityContext getNsaSecurityContext() {
		return nsaSecurityContext;
	}

	/**
	 * @param nsaSecurityContext
	 *          the nsaSecurityContext to set
	 */
	public void setNsaSecurityContext(NsaSecurityContext nsaSecurityContext) {
		this.nsaSecurityContext = nsaSecurityContext;
	}

	/**
	 * @return the providerNSA
	 */
	public String getProviderNSA() {
		return providerNSA;
	}

	/**
	 * @param providerNSA
	 *          the providerNSA to set
	 */
	public void setProviderNSA(String providerNSA) {
		this.providerNSA = providerNSA;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("{ id=");
        result.append(id);
        result.append(", connectionId=");
		result.append(connectionId);
		result.append(", globalReservationId=");
		result.append(globalReservationId);
		result.append(", description=");
		result.append(description);
		result.append(", replyTo=");
		result.append(replyTo);
		result.append(", requesterNSA=");
		result.append(requesterNSA);
		result.append(", providerNSA=");
		result.append(providerNSA);
		result.append(", currentState=");
        result.append(currentState.value());
		result.append(", id=");
		result.append(id);


		if (startTime != null) {
			result.append(", startTime=");
			result.append(df.format(startTime.getTime()));
            result.append(" ");
            result.append(tf.format(startTime.getTime()));
		}

		if (endTime != null) {
			result.append(", endTime=");
			result.append(df.format(endTime.getTime()));
            result.append(" ");
            result.append(tf.format(endTime.getTime()));
		}

		result.append(", desiredBandwidth=");
		result.append(desiredBandwidth);

		if (minimumBandwidth != null) {
			result.append(", minimumBandwidth=");
			result.append(minimumBandwidth.toString());
		}

		if (maximumBandwidth != null) {
			result.append(", maximumBandwidth=");
			result.append(maximumBandwidth.toString());
		}

		if (path != null) {
			result.append(", path=");
			result.append(MessageDump.dump(PathType.class, path));
		}

		if (nsaSecurityContext != null) {
			result.append(", nsaSecurityContext=");
			result.append(nsaSecurityContext.toString());
		}

		if (sessionSecurity != null) {
			result.append(", sessionSecurity=");
			result.append(sessionSecurity.toString());
		}

		if (serviceParameters != null) {
			result.append(", serviceParameters=");
			result.append(MessageDump.dump(ServiceParametersType.class,
			    serviceParameters));
		}

		if (routePathList != null) {
			result.append(", routePathList= { ");
			for (PathSegment segment : routePathList) {
				result.append(segment.toString());
				result.append(", ");
			}
			result.append(" }");
		}

		result.append(" }");
		return result.toString();
	}
}