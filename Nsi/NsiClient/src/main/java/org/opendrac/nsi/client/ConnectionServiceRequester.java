/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jws.WebService;
import org.ogf.schemas.nsi._2011._10.connection._interface.ForcedEndRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
import org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
@WebService(serviceName = "ConnectionServiceRequester", portName = "ConnectionServiceRequesterPort", endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort", targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/requester", wsdlLocation = "wsdl/nsi-v1/ogf_nsi_connection_requester_v1_0.wsdl")
public class ConnectionServiceRequester {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionServiceRequester.class);

    @PostConstruct
    public void myInit() {
        logger.info("ConnectionServiceRequester: instantiated ");
    }

    @PreDestroy
    public void myDestroy() {
        logger.info("ConnectionServiceRequester: destroying");
    }

    public void reserveConfirmed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.ReserveConfirmedType reserveConfirmed) throws ServiceException {
        String connectionId = reserveConfirmed.getReservation().getConnectionId();
        String providerNSA = reserveConfirmed.getProviderNSA();
        String requesterNSA = reserveConfirmed.getRequesterNSA();

        logger.info("reserveConfirmed: Received reserveConfirmed for correlationId=" +
                correlationId.value + ", from NSA=" +
                providerNSA + ", connectionId=" +
                connectionId);
        Helper.dump(ReserveConfirmedType.class, reserveConfirmed);

        logger.info("reserveConfirmed: provisioning connectionId=" + connectionId);

        // Get the global configuration information.
        Configuration config = new Configuration();

        Provision provision = new Provision();
        provision.setConnectionId(connectionId);
        provision.setCorrelationId(Helper.getUUID());
        provision.setReplyTo(config.getRequesterEndpoint());
        provision.setGlobalUserName(config.getGlobalUserName());
        provision.setUserRole(config.getGlobalRole());
        provision.setProviderNSA(providerNSA);
        provision.setRequesterNSA(requesterNSA);

        ProvisionRequestType provisionRequest = provision.getProvisionRequestType();

        // Allocate and set up an NSI proxy.
        NsiClientProxy example = new NsiClientProxy();

        try {
            example.setProviderEndpoint(config.getProviderEndpoint(), config.getProviderUserId(), config.getProviderPassword());
        }
        catch (IOException ex) {
            logger.error("Error creating NsiClientProxy so exiting...", ex);
            return;
        }


        // We get back and ack on success that will contain the correlationId
        // we assigned to the request.
        GenericAcknowledgmentType result = null;
        try {
            logger.info("Sending provision ---\n" + Helper.dump(ProvisionRequestType.class, provisionRequest));
            result = example.getProxy().provision(provisionRequest);
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException ex) {
            logger.error("Provision exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            logger.error(Helper.dump(ServiceExceptionType.class, ex.getFaultInfo()));
            return;
        }

        // The Provider NSA has accepted our request for processing.
        if (result != null) {
            logger.info("Submitted reservation request successfully correlationId=" + result);
        }
    }

    public void reserveFailed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType reserveFailed) throws ServiceException {
        logger.info("reserveFailed: Received reserveFailed for correlationId=" +
                correlationId.value + ", from NSA=" +
                reserveFailed.getProviderNSA() + ", messageId=" +
                reserveFailed.getServiceException().getErrorId());
        Helper.dump(GenericFailedType.class, reserveFailed);
    }

    public void provisionConfirmed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType provisionConfirmed) throws ServiceException {
        String connectionId = provisionConfirmed.getConnectionId();
        String providerNSA = provisionConfirmed.getProviderNSA();
        String requesterNSA = provisionConfirmed.getRequesterNSA();

        logger.info("provisionConfirmed: Received provisionConfirmed for correlationId=" +
                correlationId.value + ", from NSA=" +
                providerNSA + ", connectionId=" +
                connectionId);
        Helper.dump(GenericConfirmedType.class, provisionConfirmed);

        logger.info("provisionConfirmed: querying connectionId=" + connectionId);

        // Get the global configuration information.
        Configuration config = new Configuration();

        Query query = new Query();

        query.setCorrelationId(Helper.getUUID());
        query.setReplyTo(config.getRequesterEndpoint());
        query.setGlobalUserName(config.getGlobalUserName());
        query.setUserRole(config.getGlobalRole());
        query.setProviderNSA(providerNSA);
        query.setRequesterNSA(requesterNSA);

        List<String> connectionIdList = new ArrayList<String>();
        connectionIdList.add(connectionId);
        QueryRequestType queryRequest = query.getQueryRequestType(connectionIdList, null);

        // Allocate and set up an NSI proxy.
        NsiClientProxy example = new NsiClientProxy();

        try {
            example.setProviderEndpoint(config.getProviderEndpoint(), config.getProviderUserId(), config.getProviderPassword());
        }
        catch (IOException ex) {
            logger.error("Error creating NsiClientProxy so exiting...", ex);
            return;
        }


        // We get back and ack on success that will contain the correlationId
        // we assigned to the request.
        GenericAcknowledgmentType result = null;
        try {
            logger.info("--- Sending query ---\n" + Helper.dump(QueryRequestType.class, queryRequest));
            result = example.getProxy().query(queryRequest);
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException ex) {
            logger.error("Query exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            logger.error(Helper.dump(ServiceExceptionType.class, ex.getFaultInfo()));
            return;
        }

        // The Provider NSA has accepted our request for processing.
        if (result != null) {
            logger.info("Submitted terminate request successfully correlationId=" + result);
        }
    }

    public void provisionFailed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType provisionFailed) throws ServiceException {
        logger.info("provisionFailed: Received provisionFailed for correlationId=" +
                correlationId.value + ", from NSA=" +
                provisionFailed.getProviderNSA() + ", messageId=" +
                provisionFailed.getServiceException().getErrorId());
        Helper.dump(GenericFailedType.class, provisionFailed);
    }

    public void releaseConfirmed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType releaseConfirmed) throws ServiceException {
        logger.info("releaseConfirmed: Received releaseConfirmed for correlationId=" +
                correlationId.value + ", from NSA=" +
                releaseConfirmed.getProviderNSA() + ", connectionId=" +
                releaseConfirmed.getConnectionId());
        Helper.dump(GenericConfirmedType.class, releaseConfirmed);
    }

    public void releaseFailed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType releaseFailed) throws ServiceException {
        logger.info("releaseFailed: Received releaseFailed for correlationId=" +
                correlationId.value + ", from NSA=" +
                releaseFailed.getProviderNSA() + ", messageId=" +
                releaseFailed.getServiceException().getErrorId());
        Helper.dump(GenericFailedType.class, releaseFailed);
    }

    public void terminateConfirmed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType terminateConfirmed) throws ServiceException {
        logger.info("terminateConfirmed: Received terminateConfirmed for correlationId=" +
                correlationId.value + ", from NSA=" +
                terminateConfirmed.getProviderNSA() + ", connectionId=" +
                terminateConfirmed.getConnectionId());
        Helper.dump(GenericConfirmedType.class, terminateConfirmed);
    }

    public void terminateFailed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType terminateFailed) throws ServiceException {
        logger.info("terminateFailed: Received terminateFailed for correlationId=" +
                correlationId.value + ", from NSA=" +
                terminateFailed.getProviderNSA() + ", messageId=" +
                terminateFailed.getServiceException().getErrorId());
        Helper.dump(GenericFailedType.class, terminateFailed);
    }

    public org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType query(org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType parameters) throws ServiceException {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void queryConfirmed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType queryConfirmed) throws ServiceException {
        String providerNSA = queryConfirmed.getProviderNSA();
        String requesterNSA = queryConfirmed.getRequesterNSA();

        logger.info("queryConfirmed: Received queryConfirmed for correlationId=" +
                correlationId.value + ", from NSA=" + providerNSA);
        Helper.dump(QueryConfirmedType.class, queryConfirmed);

        String connectionId = null;
        List<QuerySummaryResultType> resultList = queryConfirmed.getReservationSummary();

        for (QuerySummaryResultType result : resultList) {
            connectionId = result.getConnectionId();
            if (result.getConnectionState() == ConnectionStateType.PROVISIONED) {
                break;
            }
        }

        if (connectionId == null) {
            return;
        }
        
        // Get the global configuration information.
        Configuration config = new Configuration();

        Terminate terminate = new Terminate();
        terminate.setConnectionId(connectionId);
        terminate.setCorrelationId(Helper.getUUID());
        terminate.setReplyTo(config.getRequesterEndpoint());
        terminate.setGlobalUserName(config.getGlobalUserName());
        terminate.setUserRole(config.getGlobalRole());
        terminate.setProviderNSA(providerNSA);
        terminate.setRequesterNSA(requesterNSA);

        TerminateRequestType terminateRequest = terminate.getTerminateRequestType();

        // Allocate and set up an NSI proxy.
        NsiClientProxy example = new NsiClientProxy();

        try {
            example.setProviderEndpoint(config.getProviderEndpoint(), config.getProviderUserId(), config.getProviderPassword());
        }
        catch (IOException ex) {
            logger.error("Error creating NsiClientProxy so exiting...", ex);
            return;
        }


        // We get back and ack on success that will contain the correlationId
        // we assigned to the request.
        GenericAcknowledgmentType result = null;
        try {
            logger.info("Sending terminate ---\n" + Helper.dump(TerminateRequestType.class, terminateRequest));
            result = example.getProxy().terminate(terminateRequest);
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException ex) {
            logger.error("Terminate exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            logger.error(Helper.dump(ServiceExceptionType.class, ex.getFaultInfo()));
            return;
        }

        // The Provider NSA has accepted our request for processing.
        if (result != null) {
            logger.info("Submitted terminate request successfully correlationId=" + result);
        }

    }

    public void queryFailed(javax.xml.ws.Holder<java.lang.String> correlationId, org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType queryFailed) throws ServiceException {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType forcedEnd(org.ogf.schemas.nsi._2011._10.connection._interface.ForcedEndRequestType parameters) throws ServiceException {
        logger.info("forcedEnd: Received forcedEnd correlationId=" +
                parameters.getCorrelationId() + ", from NSA=" +
                parameters.getForcedEnd().getProviderNSA() + ", connectionId=" +
                parameters.getForcedEnd().getConnectionId());
        Helper.dump(ForcedEndRequestType.class, parameters);


        GenericAcknowledgmentType result = new GenericAcknowledgmentType();
        result.setCorrelationId(parameters.getCorrelationId());
        return result;
    }

}
