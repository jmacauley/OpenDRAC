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
package org.opendrac.nsi.endpoints;

// Standard java import.
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReleaseRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFilterType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryOperationType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.security.NsaSecurityContext;
import org.opendrac.nsi.util.ExceptionCodes;
import org.opendrac.nsi.util.MessageDump;
import org.opendrac.nsi.util.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author hacksaw
 */
@WebService(serviceName = "ConnectionServiceProvider",
        portName = "ConnectionServiceProviderPort",
        endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
        targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider",
        wsdlLocation = "WEB-INF/wsdl/nsi-v1/ogf_nsi_connection_provider_v1_0.wsdl")
public class ConnectionServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionServiceProvider.class);

    /*
     * This holds the web service request context which includes all the
     * original HTTP information, including the JAAS authentication and
     * authorization information.
     */
    @Resource
    private WebServiceContext wsc;

    @PostConstruct
    public void myInit() {
        logger.info("ConnectionServiceProvider: instantiated");
    }

    @PreDestroy
    public void myDestroy() {
        logger.info("ConnectionServiceProvider: destroying");
    }

    /**
     * The reservation method processes an NSI reservation request for
     * inter-domain bandwidth.  Those parameters required for the request
     * to proceed to a processing actor will be validated, however, all other
     * parameters will be validated in the processing actor.
     *
     * @param parameters	The un-marshaled JAXB object holding the NSI reservation request.
     * @return              The GenericAcknowledgmentType object returning the correlationId sent in the reservation request.  We are acknowledging that we have received the request.
     * @throws              ServiceException if we can determine there is processing error before digging into the request.
     */
    public GenericAcknowledgmentType reserve(ReserveRequestType parameters) throws ServiceException {
        NsaSecurityContext securityContext = getNsaSecurityContext(wsc);
        StringBuilder sb = new StringBuilder("reserve [");
        sb.append(securityContext.getRemoteAddr());
        sb.append("]: ");

        if (parameters == null) {
            ServiceException se = ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "ReserveRequestType", "<null>");
            logger.error(sb.append("null ReserveRequestType").toString());
            throw se;
        }

        logger.info(sb.toString() + MessageDump.dump(ReserveRequestType.class, parameters));

        // Build an internal request for this reservation request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.reserveProvider);

        /*
         * Break out the attributes we need for handling.  correlationId is
         * needed for any acknowledgment, confirmation, or failed message.
         */
        String correlationId = getCorrelationId(parameters.getCorrelationId());
        request.setCorrelationId(correlationId);

        /*
         * We will send the confirmation, or failed message back to this
         * location.  In the future we may remove this parameter and add
         * a csRequesterEndpoint field to NSA topology.
         */
        request.setReplyTo(getReplyTo(parameters.getReplyTo()));

        /*
         * Save the calling NSA security context and pass it along for use
         * during processing of request (when implemented).
         */
        request.setNsaSecurityContext(securityContext);

        /*
         * Extract the reservation information for use by the actor processing
         * logic.
         */
        ReserveType reservation = getReservation(parameters);
        request.setPayload(reservation);

        /*
         * Extract NSA fields.
         */
        request.setRequesterNSA(getNsa(reservation.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsa(reservation.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isProviderNsa(request.getProviderNSA());

        /*
         * Get the connectionId from the reservation as we will use this to
         * serialize related requests.
         */
        request.setConnectionId(getConnectionId(reservation));

        // Route this message to the appropriate actor for processing.
        CastingDirector.getInstance().send(request);

        /*
         * We successfully sent the message for processing so acknowledge it
         * back to the requesting NSA.  We hope this returns before the
         * confirmation makes it back to the requesting NSA.
         */
        GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
        ack.setCorrelationId(correlationId);
        return ack;
    }

    public GenericAcknowledgmentType provision(ProvisionRequestType parameters) throws ServiceException {
        NsaSecurityContext securityContext = getNsaSecurityContext(wsc);
        StringBuilder sb = new StringBuilder("provision [");
        sb.append(securityContext.getRemoteAddr());
        sb.append("]: ");

        if (parameters == null) {
            ServiceException se = ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "ProvisionRequestType", "<null>");
            logger.error(sb.append("null ProvisionRequestType").toString(), se);
            throw se;
        }

        logger.info(sb.toString() + MessageDump.dump(ProvisionRequestType.class, parameters));

        // Build an internal request for this reservation request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.provisionProvider);

        /*
         * Break out the attributes we need for handling.  correlationId is
         * needed for any acknowledgment, confirmation, or failed message.
         */
        String correlationId = getCorrelationId(parameters.getCorrelationId());
        request.setCorrelationId(correlationId);

        /*
         * We will send the confirmation, or failed message back to this
         * location.
         */
        request.setReplyTo(getReplyTo(parameters.getReplyTo()));

        /*
         * Save the calling NSA security context and pass it along for use
         * during processing of request.
         */
        request.setNsaSecurityContext(securityContext);

        // Extract the reservation information.
        GenericRequestType provision = getGenericRequestType(parameters.getProvision());
        request.setPayload(provision);

        // Extract NSA fields.
        request.setRequesterNSA(getNsNetwork(provision.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(provision.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isProviderNsa(request.getProviderNSA());

        /*
         * Get the connectionId from the reservation as we will use this to
         * serialize related requests.
         */
        request.setConnectionId(getConnectionId(provision.getConnectionId()));

        // Route this message to the appropriate actor for processing.
        CastingDirector.getInstance().send(request);

        /*
         * We successfully sent the message for processing so acknowledge it
         * back to the sending.
         */
        GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
        ack.setCorrelationId(correlationId);
        return ack;
    }

    public GenericAcknowledgmentType release(ReleaseRequestType parameters) throws ServiceException {
        NsaSecurityContext securityContext = getNsaSecurityContext(wsc);
        StringBuilder sb = new StringBuilder("release [");
        sb.append(securityContext.getRemoteAddr());
        sb.append("]: ");

        if (parameters == null) {
            ServiceException se = ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "ReleaseRequestType", "<null>");
            logger.error(sb.append("null ReleaseRequestType").toString(), se);
            throw se;
        }

        logger.info(sb.toString() + MessageDump.dump(ReleaseRequestType.class, parameters));

        // Build an internal request for this reservation request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.releaseProvider);

        /*
         * Break out the attributes we need for handling.  correlationId is
         * needed for any acknowledgment, confirmation, or failed message.
         */
        String correlationId = getCorrelationId(parameters.getCorrelationId());
        request.setCorrelationId(correlationId);

        /*
         * We will send the confirmation, or failed message back to this
         * location.
         */
        request.setReplyTo(getReplyTo(parameters.getReplyTo()));

        /*
         * Save the calling NSA security context and pass it along for use
         * during processing of request.
         */
        request.setNsaSecurityContext(securityContext);

        // Extract the reservation information.
        GenericRequestType release = getGenericRequestType(parameters.getRelease());
        request.setPayload(release);

        // Extract NSA fields.
        request.setRequesterNSA(getNsNetwork(release.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(release.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isProviderNsa(request.getProviderNSA());

        /**
         * Get the connectionId from the reservation as we will use this to
         * serialize related requests.
         */
        request.setConnectionId(getConnectionId(release.getConnectionId()));

        // TODO: Implement release.
        if (true) {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        // Route this message to the appropriate actor for processing.
        CastingDirector.getInstance().send(request);

        /*
         * We successfully sent the message for processing so acknowledge it
         * back to the sending.
         */
        GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
        ack.setCorrelationId(correlationId);
        return ack;
    }

    public GenericAcknowledgmentType terminate(TerminateRequestType parameters) throws ServiceException {
        NsaSecurityContext securityContext = getNsaSecurityContext(wsc);
        StringBuilder sb = new StringBuilder("terminate [");
        sb.append(securityContext.getRemoteAddr());
        sb.append("]: ");

        if (parameters == null) {
            ServiceException se = ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "TerminateRequestType", "<null>");
            logger.error(sb.append("null TerminateRequestType").toString(), se);
            throw se;
        }

        logger.info(sb.toString() + MessageDump.dump(TerminateRequestType.class, parameters));

        // Build an internal request for this reservation request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.terminateProvider);

        /*
         * Break out the attributes we need for handling.  correlationId is
         * needed for any acknowledgment, confirmation, or failed message.
         */
        String correlationId = getCorrelationId(parameters.getCorrelationId());
        request.setCorrelationId(correlationId);

        /*
         * We will send the confirmation, or failed message back to this
         * location.
         */
        request.setReplyTo(getReplyTo(parameters.getReplyTo()));

        /*
         * Save the calling NSA security context and pass it along for use
         * during processing of request.
         */
        request.setNsaSecurityContext(securityContext);

        // Extract the reservation information.
        GenericRequestType terminate = getGenericRequestType(parameters.getTerminate());
        request.setPayload(terminate);

        // Extract NSA fields.
        request.setRequesterNSA(getNsNetwork(terminate.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(terminate.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isProviderNsa(request.getProviderNSA());

        /*
         * Get the connectionId from the reservation as we will use this to
         * serialize related requests.
         */
        request.setConnectionId(getConnectionId(terminate.getConnectionId()));

        // Route this message to the appropriate actor for processing.
        CastingDirector.getInstance().send(request);

        /*
         * We successfully sent the message for processing so acknowledge it
         * back to the sending.
         */
        GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
        ack.setCorrelationId(correlationId);
        return ack;
    }

    public GenericAcknowledgmentType query(QueryRequestType parameters) throws ServiceException {
        NsaSecurityContext securityContext = getNsaSecurityContext(wsc);
        StringBuilder sb = new StringBuilder("query [");
        sb.append(securityContext.getRemoteAddr());
        sb.append("]: ");

        if (parameters == null) {
            ServiceException se = ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "QueryRequestType", "<null>");
            logger.error(sb.append("null QueryRequestType").toString(), se);
            throw se;
        }

        logger.info(sb.toString() + MessageDump.dump(QueryRequestType.class, parameters));

        // Build an internal request for this reservation request.
        NsaMessage request = new NsaMessage();

        /*
         * Break out the attributes we need for handling.  correlationId is
         * needed for any acknowledgment, confirmation, or failed message.
         */
        String correlationId = getCorrelationId(parameters.getCorrelationId());
        request.setCorrelationId(correlationId);

        /*
         * We will send the confirmation, or failed message back to this
         * location.
         */
        request.setReplyTo(getReplyTo(parameters.getReplyTo()));

        /*
         * Save the calling NSA security context and pass it along for use
         * during processing of request.
         */
        request.setNsaSecurityContext(getNsaSecurityContext(wsc));

        // Extract the query information.
        QueryType query = getQueryType(parameters);
        QueryFilterType queryFilter = getQueryFilterType(query);

        // We want to route to operation specific provider.
        if (query.getOperation() == QueryOperationType.SUMMARY) {
            logger.info(sb.toString() + "summary operation request");
            request.setMessageType(NsaMessage.MessageType.querySummaryProvider);
        }
        else if (query.getOperation() == QueryOperationType.DETAILS) {
            logger.info(sb.toString() + "details operation request");
            request.setMessageType(NsaMessage.MessageType.queryDetailsProvider);

        }
        else {
            ServiceException se = ExceptionCodes.buildProviderException(ExceptionCodes.UNSUPPORTED_OPTION, "QueryOperationType", query.getOperation().toString());
            sb.append("invalid operation type ");
            sb.append(query.getOperation());
            logger.error(sb.toString());
            throw se;
        }

        request.setPayload(query);

        // Extract NSA fields.
        request.setRequesterNSA(getNsNetwork(query.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(query.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isProviderNsa(request.getProviderNSA());

        /*
         * TODO: We need to fill in te connectionId so the the load balancer can
         * hash to an appropriate worker.  We can change this to has on
         * correlationId since there is really no value serializing on
         * connectionId (original idea gone wrong with multiple actors).
         */
        request.setConnectionId(correlationId);

        // Route this message to the appropriate actor for processing.
        CastingDirector.getInstance().send(request);

        /*
         * We successfully sent the message for processing so acknowledge it
         * back to the sending.
         */
        GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
        ack.setCorrelationId(correlationId);
        return ack;
    }

    public void queryConfirmed(
            Holder<String> correlationId,
            QueryConfirmedType queryConfirmed) throws ServiceException {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void queryFailed(
            Holder<String> correlationId,
            QueryFailedType queryFailed) throws ServiceException {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Extract needed security attributes from the WebServiceContext.
     *
     * @param wsc the web services context from the http servlet.
     * @return the new NSA security object.
     * @throws ServiceException
     */
    public NsaSecurityContext getNsaSecurityContext(WebServiceContext wsc) throws ServiceException {
        NsaSecurityContext sc = new NsaSecurityContext();
        try {
            sc.setContext(wsc);
        }
        catch (Exception ex) {
            logger.error("getNsaSecurityContext: invalid security context from web container.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_ERROR, ex.getMessage(), "securityContext", "");
        }

        return sc;
    }

    /**
     * Extract the correltationId from the request and throw an exception if
     * not found.
     *
     * @param correlationId
     * @return correlationId
     * @throws ServiceException
     */
    public String getCorrelationId(String correlationId) throws ServiceException {
        if (correlationId == null || correlationId.isEmpty()) {
            logger.error("getCorrelationId: invalid correlationId.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "correlationId", "<null>");
        }

        return correlationId;
    }

    /**
     * Extract the replyTo from the request and throw an exception if not
     * found.
     *
     * @param replyTo
     * @return replyTo
     * @throws ServiceException
     */
    public String getReplyTo(String replyTo) throws ServiceException {
        if (replyTo == null || replyTo.isEmpty()) {
            logger.error("getReplyTo: invalid replyTo.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "replyTo", "<null>");
        }

        return replyTo;
    }

    /**
     * Extracts ReserveType from the incoming ReserveRequestType.
     *
     * @param request
     * @return reservation
     * @throws ServiceException
     */
    public ReserveType getReservation(ReserveRequestType request) throws ServiceException {
        ReserveType reservation = (ReserveType) request.getReserve();
        if (reservation == null) {
            logger.error("getReservation: invalid ReserveRequestType.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "reservation", "<null>");
        }

        return reservation;
    }

    /**
     * Extracts ReserveType from the incoming ReserveRequestType.
     *
     * @param request
     * @return reservation
     * @throws ServiceException
     */
    public GenericRequestType getGenericRequestType(GenericRequestType request) throws ServiceException {
        if (request == null) {
            logger.error("getGenericRequestType: invalid GenericRequestType.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "GenericRequestType", "<null>");
        }

        return request;
    }

    /**
     * Extracts QueryType from the incoming QueryRequestType.
     *
     * @param request
     * @return query
     * @throws ServiceException
     */
    public QueryType getQueryType(QueryRequestType query) throws ServiceException {
        if (query == null || query.getQuery() == null) {
            logger.error("getQueryType: invalid QueryType.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "QueryType", "<null>");
        }

        return query.getQuery();
    }

    /**
     * Extracts QueryFilterType from the incoming QueryType.
     *
     * @param QueryType query
     * @return QueryFilterType
     * @throws ServiceException
     */
    public QueryFilterType getQueryFilterType(QueryType query) throws ServiceException {
        if (query == null || query.getQueryFilter() == null) {
            logger.error("getQueryFilterType: invalid QueryFilterType.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "QueryFilterType", "<null>");
        }

        return query.getQueryFilter();
    }

    /**
     * Extracts the connectionId from a ReserveType.
     *
     * @param reservation
     * @return connectionId
     * @throws ServiceException
     */
    public String getConnectionId(ReserveType reservation) throws ServiceException {
        ReservationInfoType resType = reservation.getReservation();
        if (resType == null) {
            logger.error("getConnectionId: null ReservationInfoType.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "reservation", "<null>");
        }

        String connectionId = resType.getConnectionId();
        if (connectionId == null || connectionId.isEmpty()) {
            logger.error("getConnectionId: null connectionId.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "connectionId", "<null>");
        }

        return connectionId;
    }

    /**
     * Extracts the connectionId.
     *
     * @param connectionId
     * @return
     * @throws ServiceException
     */
    public String getConnectionId(String connectionId) throws ServiceException {
        if (connectionId == null || connectionId.isEmpty()) {
            logger.error("getConnectionId: null connectionId.");
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "connectionId", "<null>");
        }

        return connectionId;
    }

    /**
     * Extracts the requesterNSA or providerNSA element.
     *
     * @param nsa the NSA field to extract.
     * @param element the name of the element field for exception generation.
     * @return nsa
     * @throws ServiceException
     */
    public String getNsa(String nsa, String element) throws ServiceException {
        if (nsa == null || nsa.isEmpty()) {
            logger.error("getNsa: null nsa " + element);
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, element, "<null>");
        }

        return nsa;
    }

    /**
     * Extracts the requesterNSA or providerNSA element.
     *
     * @param nsNetwork the NsNetwork field to extract.
     * @param element the name of the element field for exception generation.
     * @return nsNetwork
     * @throws ServiceException
     */
    public String getNsNetwork(String nsNetwork, String element) throws ServiceException {
        if (nsNetwork == null || nsNetwork.isEmpty()) {
            logger.error("getNsNetwork: null nsNetwork " + element);
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, element, "<null>");
        }

        return nsNetwork;
    }

    /**
     * Determines if the provider NsNetwork identifier is managed by this NSA.
     *
     * @param request
     * @return true if the provider NSA matches this NSA.  If not an appropriate
     * exceptions is thrown.
     * @throws ServiceException
     */
    public boolean isProviderNsNetwork(String providerNsNetwork) throws ServiceException {

        // Get a reference to the DataManager.
    	DataManager dataManager = SpringApplicationContext.getBean("dataManager", DataManager.class);
        NsaConfigurationManager nsa = dataManager.getNsaConfigurationManager();

        if (nsa.isMyNsNetworkURN(providerNsNetwork)) {
            return true;
        }

        // Remember that the NSNetwork is in the providerNSA element.
        logger.error("isProviderNsNetwork: not my nsnetwork " + providerNsNetwork);
        throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "providerNSA", providerNsNetwork);
    }

    /**
     * Determines if the provider Nsa identifier is this NSA.
     *
     * @param request
     * @return true if the provider NSA matches this NSA.  If not an appropriate
     * exceptions is thrown.
     * @throws ServiceException
     */
    public boolean isProviderNsa(String providerNSA) throws ServiceException {

        if (NsaConfigurationManager.getInstance().isMyNsaURN(providerNSA)) {
            return true;
        }

        // Remember that the NSNetwork is in the providerNSA element.
        logger.error("isProviderNsa: not my NSA " + providerNSA);
        throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, "providerNSA", providerNSA);
    }
}
