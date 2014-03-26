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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.ogf.schemas.nsi._2011._10.connection._interface.ForcedEndRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveConfirmedType;
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
@WebService(serviceName = "ConnectionServiceRequester",
        portName = "ConnectionServiceRequesterPort",
        endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort",
        targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/requester",
        wsdlLocation = "WEB-INF/wsdl/nsi-v1/ogf_nsi_connection_requester_v1_0.wsdl")
public class ConnectionServiceRequester {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * This holds the web service request context which includes all the
     * original HTTP information, including the JAAS authentication and
     * authorization information.
     */
    @Resource
    private WebServiceContext wsc;

    private final CastingDirector castingDirector = SpringApplicationContext.getBean("castingDirector", CastingDirector.class);

    @PostConstruct
    public void myInit() {
        logger.info("ConnectionServiceRequester: instantiated");
    }

    @PreDestroy
    public void myDestroy() {
        logger.info("ConnectionServiceRequester: destroying");
    }

    public void reserveConfirmed(Holder<String> correlationId, ReserveConfirmedType reserveConfirmed) throws ServiceException {

        // Validate we received the confirmed message.
        if (reserveConfirmed == null) {
           throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "GenericConfirmedType", "<null>");
        }

        logger.info("reserveConfirmed: " + MessageDump.dump(GenericConfirmedType.class, reserveConfirmed));

        // Build an internal request for this provisionConfirmed request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.reserveConfirmedRequester);

        /*
         * Break out the attributes we need for handling.
         */
        request.setCorrelationId(correlationId.value);
        request.setNsaSecurityContext(getNsaSecurityContext(wsc));

        /*
         * Extract NSA fields.  These are currently named incorrectly as the
         * specification states they should contain the NsNetwork name.  Try
         * not to get confused when looking up information in topology using
         * this field.
         */
        request.setRequesterNSA(getNsNetwork(reserveConfirmed.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(reserveConfirmed.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isRequesterNsNetwork(request.getRequesterNSA());

        /*
         * Get the connectionId from the request as this is used as our primary
         * index.
         */
        request.setConnectionId(getConnectionId(reserveConfirmed.getReservation()));
        request.setPayload(reserveConfirmed);

        // Route this message to the appropriate actor for processing.

        castingDirector.send(request);
    }

    public void reserveFailed(Holder<String> correlationId, GenericFailedType reserveFailed) throws ServiceException {

        // Validate we received the confirmed message.
        if (reserveFailed == null) {
           throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "GenericFailedType", "<null>");
        }

        logger.info("reserveFailed: " + MessageDump.dump(GenericFailedType.class, reserveFailed));

        // Build an internal request for this reserveFailed request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.reserveFailedRequester);

        /*
         * Break out the attributes we need for handling.
         */
        request.setCorrelationId(correlationId.value);
        request.setNsaSecurityContext(getNsaSecurityContext(wsc));

        /*
         * Extract NSA fields.  These are currently named incorrectly as the
         * specification states they should contain the NsNetwork name.  Try
         * not to get confused when looking up information in topology using
         * this field.
         */
        request.setRequesterNSA(getNsNetwork(reserveFailed.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(reserveFailed.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isRequesterNsNetwork(request.getRequesterNSA());

        /*
         * Get the connectionId from the request as this is used as our primary
         * index.
         */
        request.setConnectionId(getConnectionId(reserveFailed.getConnectionId()));
        request.setGlobalReservationId(reserveFailed.getGlobalReservationId());
        request.setPayload(reserveFailed.getServiceException());

        // Route this message to the appropriate actor for processing.

        castingDirector.send(request);
    }

    /**
     *
     * @param correlationId The correlation for the provision operation we
     * issued to the child NSA.
     * @param provisionConfirmed The provsionConfirmed NSI message indicating
     * that the corresponding child connection segment has been provisioned.
     * @return correlationId is used to populate the ACK message.
     * @throws ServiceException
     */
    public void provisionConfirmed(Holder<String> correlationId, GenericConfirmedType provisionConfirmed) throws ServiceException {

        // Validate we received the confirmed message.
        if (provisionConfirmed == null) {
           throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "GenericConfirmedType", "<null>");
        }

        logger.info("provisionConfirmed: " + MessageDump.dump(GenericConfirmedType.class, provisionConfirmed));

        // Build an internal request for this provisionConfirmed request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.provisionConfirmedRequester);

        /*
         * Break out the attributes we need for handling.
         */
        request.setCorrelationId(correlationId.value);
        request.setNsaSecurityContext(getNsaSecurityContext(wsc));

        /*
         * Extract NSA fields.  These are currently named incorrectly as the
         * specification states they should contain the NsNetwork name.  Try
         * not to get confused when looking up information in topology using
         * this field.
         */
        request.setRequesterNSA(getNsNetwork(provisionConfirmed.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(provisionConfirmed.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isRequesterNsNetwork(request.getRequesterNSA());

        /*
         * Get the connectionId from the request as this is used as our primary
         * index.
         */
        request.setConnectionId(getConnectionId(provisionConfirmed.getConnectionId()));
        request.setGlobalReservationId(provisionConfirmed.getGlobalReservationId());
        request.setPayload(provisionConfirmed);

        // Route this message to the appropriate actor for processing.

        castingDirector.send(request);

    }

    public void provisionFailed(Holder<String> correlationId, GenericFailedType provisionFailed) throws ServiceException {

        // Validate we received the confirmed message.
        if (provisionFailed == null) {
           throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "GenericFailedType", "<null>");
        }

        logger.info("provisionFailed: " + MessageDump.dump(GenericFailedType.class, provisionFailed));

        // Build an internal request for this provisionFailed request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.provisionFailedRequester);

        /*
         * Break out the attributes we need for handling.
         */
        request.setCorrelationId(correlationId.value);
        request.setNsaSecurityContext(getNsaSecurityContext(wsc));

        /*
         * Extract NSA fields.  These are currently named incorrectly as the
         * specification states they should contain the NsNetwork name.  Try
         * not to get confused when looking up information in topology using
         * this field.
         */
        request.setRequesterNSA(getNsNetwork(provisionFailed.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(provisionFailed.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isRequesterNsNetwork(request.getRequesterNSA());

        /*
         * Get the connectionId from the request as this is used as our primary
         * index.
         */
        request.setConnectionId(getConnectionId(provisionFailed.getConnectionId()));
        request.setGlobalReservationId(provisionFailed.getGlobalReservationId());
        request.setPayload(provisionFailed.getServiceException());

        // Route this message to the appropriate actor for processing.

        castingDirector.send(request);
    }

    public void releaseConfirmed(Holder<String> correlationId, GenericConfirmedType releaseConfirmed) throws ServiceException {

        // Validate we received the confirmed message.
        if (releaseConfirmed == null) {
           throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "GenericConfirmedType", "<null>");
        }

        logger.info("releaseConfirmed: " + MessageDump.dump(GenericConfirmedType.class, releaseConfirmed));

        // Build an internal request for this releaseConfirmed request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.releaseConfirmedRequester);

        /*
         * Break out the attributes we need for handling.
         */
        request.setCorrelationId(correlationId.value);
        request.setNsaSecurityContext(getNsaSecurityContext(wsc));

        /*
         * Extract NSA fields.  These are currently named incorrectly as the
         * specification states they should contain the NsNetwork name.  Try
         * not to get confused when looking up information in topology using
         * this field.
         */
        request.setRequesterNSA(getNsNetwork(releaseConfirmed.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(releaseConfirmed.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isRequesterNsNetwork(request.getRequesterNSA());

        /*
         * Get the connectionId from the request as this is used as our primary
         * index.
         */
        request.setConnectionId(getConnectionId(releaseConfirmed.getConnectionId()));
        request.setGlobalReservationId(releaseConfirmed.getGlobalReservationId());
        request.setPayload(releaseConfirmed);

        // Route this message to the appropriate actor for processing.

        castingDirector.send(request);
    }

    public void releaseFailed(Holder<String> correlationId, GenericFailedType releaseFailed) throws ServiceException {

        // Validate we received the confirmed message.
        if (releaseFailed == null) {
           throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "GenericFailedType", "<null>");
        }

        logger.info("releaseFailed: " + MessageDump.dump(GenericFailedType.class, releaseFailed));

        // Build an internal request for this releaseFailed request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.releaseFailedRequester);

        /*
         * Break out the attributes we need for handling.
         */
        request.setCorrelationId(correlationId.value);
        request.setNsaSecurityContext(getNsaSecurityContext(wsc));

        /*
         * Extract NSA fields.  These are currently named incorrectly as the
         * specification states they should contain the NsNetwork name.  Try
         * not to get confused when looking up information in topology using
         * this field.
         */
        request.setRequesterNSA(getNsNetwork(releaseFailed.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(releaseFailed.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isRequesterNsNetwork(request.getRequesterNSA());

        /*
         * Get the connectionId from the request as this is used as our primary
         * index.
         */
        request.setConnectionId(getConnectionId(releaseFailed.getConnectionId()));
        request.setGlobalReservationId(releaseFailed.getGlobalReservationId());
        request.setPayload(releaseFailed.getServiceException());

        // Route this message to the appropriate actor for processing.

        castingDirector.send(request);
    }

    public void terminateConfirmed(Holder<String> correlationId, GenericConfirmedType terminateConfirmed) throws ServiceException {

        // Validate we received the confirmed message.
        if (terminateConfirmed == null) {
           throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "GenericConfirmedType", "<null>");
        }

        logger.info("terminateConfirmed: " + MessageDump.dump(GenericConfirmedType.class, terminateConfirmed));

        // Build an internal request for this releaseConfirmed request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.terminateConfirmedRequester);

        /*
         * Break out the attributes we need for handling.
         */
        request.setCorrelationId(correlationId.value);
        request.setNsaSecurityContext(getNsaSecurityContext(wsc));

        /*
         * Extract NSA fields.  These are currently named incorrectly as the
         * specification states they should contain the NsNetwork name.  Try
         * not to get confused when looking up information in topology using
         * this field.
         */
        request.setRequesterNSA(getNsNetwork(terminateConfirmed.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(terminateConfirmed.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isRequesterNsNetwork(request.getRequesterNSA());

        /*
         * Get the connectionId from the request as this is used as our primary
         * index.
         */
        request.setConnectionId(getConnectionId(terminateConfirmed.getConnectionId()));
        request.setGlobalReservationId(terminateConfirmed.getGlobalReservationId());
        request.setPayload(terminateConfirmed);

        // Route this message to the appropriate actor for processing.

        castingDirector.send(request);
    }

    public void terminateFailed(Holder<String> correlationId, GenericFailedType terminateFailed) throws ServiceException {

        // Validate we received the confirmed message.
        if (terminateFailed == null) {
           throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "GenericFailedType", "<null>");
        }

        logger.info("terminateFailed: " + MessageDump.dump(GenericFailedType.class, terminateFailed));

        // Build an internal request for this terminateFailed request.
        NsaMessage request = new NsaMessage();
        request.setMessageType(NsaMessage.MessageType.terminateFailedRequester);

        /*
         * Break out the attributes we need for handling.
         */
        request.setCorrelationId(correlationId.value);
        request.setNsaSecurityContext(getNsaSecurityContext(wsc));

        /*
         * Extract NSA fields.  These are currently named incorrectly as the
         * specification states they should contain the NsNetwork name.  Try
         * not to get confused when looking up information in topology using
         * this field.
         */
        request.setRequesterNSA(getNsNetwork(terminateFailed.getRequesterNSA(), "requesterNSA"));
        request.setProviderNSA(getNsNetwork(terminateFailed.getProviderNSA(), "providerNSA"));

        /*
         * Verify that this message was targeting this NSA by looking at the
         * ProviderNSA field.  If invalid we will throw an exception.
         */
        isRequesterNsNetwork(request.getRequesterNSA());

        /*
         * Get the connectionId from the request as this is used as our primary
         * index.
         */
        request.setConnectionId(getConnectionId(terminateFailed.getConnectionId()));
        request.setGlobalReservationId(terminateFailed.getGlobalReservationId());
        request.setPayload(terminateFailed.getServiceException());

        // Route this message to the appropriate actor for processing.

        castingDirector.send(request);
    }

    public void query(QueryRequestType parameters) throws ServiceException {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void queryConfirmed(Holder<String> correlationId, QueryConfirmedType queryConfirmed) throws ServiceException {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void queryFailed(Holder<String> correlationId, QueryFailedType queryFailed) throws ServiceException {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void forcedEnd(ForcedEndRequestType parameters) throws ServiceException {
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
            throw ExceptionCodes.buildRequesterException(ExceptionCodes.INTERNAL_ERROR, ex.getMessage(), "securityContext", "");
        }

        return sc;
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
            throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, element, "<null>");
        }

        return nsNetwork;
    }

    /**
     * Determines if the requester NsNetwork identifier is managed by this NSA.
     *
     * @param request
     * @return true if the requester NSA matches this NSA.  If not an appropriate
     * exceptions is thrown.
     * @throws ServiceException
     */
    public boolean isRequesterNsNetwork(String requesterNsNetwork) throws ServiceException {

        // Get a reference to the DataManager.
    		DataManager dataManager = SpringApplicationContext.getBean("dataManager", DataManager.class);
        NsaConfigurationManager nsa = dataManager.getNsaConfigurationManager();

        if (nsa.isMyNsNetworkURN(requesterNsNetwork)) {
            return true;
        }

        // Remember that the NSNetwork is in the requesterNSA element.
        throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "requesterNSA", requesterNsNetwork);
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
            throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "connectionId", "<null>");
        }

        return connectionId;
    }

    /**
     * Extracts the connectionId from a ReservationInfoType.
     *
     * @param reservation
     * @return connectionId
     * @throws ServiceException
     */
    public String getConnectionId(ReservationInfoType reservation) throws ServiceException {
        if (reservation == null) {
           throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "reservation", "<null>");
        }

        String connectionId = reservation.getConnectionId();
        if (connectionId == null || connectionId.isEmpty()) {
            throw ExceptionCodes.buildRequesterException(ExceptionCodes.MISSING_PARAMETER, "connectionId", "<null>");
        }

        return connectionId;
    }
}
