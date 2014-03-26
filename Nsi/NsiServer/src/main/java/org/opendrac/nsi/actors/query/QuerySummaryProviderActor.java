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
package org.opendrac.nsi.actors.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;


import java.util.Collection;
import java.util.List;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFilterType;
import org.ogf.schemas.nsi._2011._10.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.security.NsaSecurityContext;
import org.opendrac.nsi.security.SessionSecurity;

/**
 *
 * @author hacksaw
 */
public class QuerySummaryProviderActor extends UntypedActor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("QueryProviderActor.onReceive: received message.");
        if (message == null) {
            logger.error("QueryProviderActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("QueryProviderActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("QueryProviderActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    /**
     * Process the NSI query summary message.  We can satisfied the request
     * within this actor and do not require messaging to child NSA.
     *
     * @param queryMessage Our generic message holding the NSI query request.
     */
    private void process(NsaMessage queryMessage) {

        // We have already verified this field is present and not null.
        NsaSecurityContext nsaSecurityContext = queryMessage.getNsaSecurityContext();
        QueryType query = (QueryType) queryMessage.getPayload();
        String correlationId = queryMessage.getCorrelationId();
        String replyTo = queryMessage.getReplyTo();
        String requesterNSA = queryMessage.getRequesterNSA();
        String providerNSA = queryMessage.getProviderNSA();

		// Log the incoming message information.
        logger.info("QuerySummaryProviderActor.process: Incoming query summary message [" +
                nsaSecurityContext.getUserName() + " @ " +
                nsaSecurityContext.getRemoteAddr() +
                ", requesterNSA=" + requesterNSA +
                ", providerNSA=" + providerNSA +
                ", correlationId=" + correlationId + "]");

        try {
            // Parse the session security and assign to state machine.
            SessionSecurity sessionSecurity = new SessionSecurity();
            sessionSecurity.parseSessionSecurityAttr(query.getSessionSecurityAttr());

            /*
             * TODO: compare security to determine if sending NSA/User can
             * manipulate this state machine.  We will use the user's NSI role
             * to do this in the future.
             */
        }
        catch (ServiceException nsi) {
            /*
             * Send this to a web services actor for delivery of a
             * ReservationFailed message.
             */
            logger.info("QuerySummaryProviderActor.process: Sending queryFailed message " + nsi.toString());

            // Leave the state where it is for now.

            NsaMessage failedMessage = new NsaMessage();
            failedMessage.setCorrelationId(correlationId);
            failedMessage.setReplyTo(replyTo);
            failedMessage.setRequesterNSA(requesterNSA);
            failedMessage.setProviderNSA(providerNSA);
            failedMessage.setMessageType(NsaMessage.MessageType.queryFailedProvider);
            failedMessage.setPayload(nsi.getFaultInfo());

            // Route this message to the appropriate actor for processing.
            CastingDirector.getInstance().send(failedMessage);
            logger.info("QuerySummaryProviderActor.process: queryFailed message issued to actor so returning");
            return;
        }

        /*
         * We can be queried based on connectionId, globalReservationId, or
         * an empty request which means all applicable reservations (we can
         * apply access control rules if needed).
         */
        QueryFilterType queryFilter = query.getQueryFilter();

        // We stuff the results in here.
        QueryConfirmedType queryResult = new QueryConfirmedType();
        queryResult.setProviderNSA(queryMessage.getProviderNSA());
        queryResult.setRequesterNSA(queryMessage.getRequesterNSA());

        // If the query does not identify specific ids then we need to return all.
        boolean getAll = true;

        // Look up matching connectionIds.
        List<String> connectionIds = query.getQueryFilter().getConnectionId();
        if (connectionIds != null && !connectionIds.isEmpty()) {
            getByConnectionId(connectionIds, queryResult.getReservationSummary());
            getAll = false;
        }

        // Lookup matching globalReservationids.
        List<String> globalReservationIds = query.getQueryFilter().getGlobalReservationId();
        if (globalReservationIds != null && !globalReservationIds.isEmpty()) {
            getByGlobalReservationId(globalReservationIds, queryResult.getReservationSummary());
            getAll = false;
        }

        // If we did not get a query filter then return all state machines.
        if (getAll == true) {
            getAllStateMachines(queryResult.getReservationSummary());
        }

        // Now we send the confirmed back.  No pending operations, etc.
        NsaMessage queryConfirmed = new NsaMessage();
        queryConfirmed.setCorrelationId(queryMessage.getCorrelationId());
        queryConfirmed.setReplyTo(queryMessage.getReplyTo());
        queryConfirmed.setNsaSecurityContext(queryMessage.getNsaSecurityContext());
        queryConfirmed.setProviderNSA(queryMessage.getProviderNSA());
        queryConfirmed.setRequesterNSA(queryMessage.getRequesterNSA());
        queryConfirmed.setPayload(queryResult);
        queryConfirmed.setMessageType(NsaMessage.MessageType.queryConfirmedProvider);

        // Route this queryConfirmed to the associated actor.
        CastingDirector.getInstance().send(queryConfirmed);

        // We are done - all other processing is handled by other actors.
        logger.info("QuerySummaryProviderActor.process: done.");
    }

    private void getByConnectionId(List<String> connectionIds, List<QuerySummaryResultType> summaryList) {
        StateMachineManager stateMachineManager = StateMachineManager.getInstance();

        for (String connectionId : connectionIds) {
            logger.info("getReservationSummary: checking for connectionId=" + connectionId);

            StateMachine machine = stateMachineManager.getStateMachine(connectionId);
            if (machine != null) {
                logger.info("getReservationSummary: found match for connectionId=" + connectionId);
                QuerySummaryResultType result = mapStateMachine(machine);
                summaryList.add(result);
            }
        }
    }

    private void getByGlobalReservationId(List<String> globalReservationIds, List<QuerySummaryResultType> summaryList) {
        StateMachineManager stateMachineManager = StateMachineManager.getInstance();

        if (logger.isDebugEnabled()) {
            for (String globalReservationId : globalReservationIds) {
                logger.debug("getByGlobalReservationId: globalReservationId=" + globalReservationId);
            }
        }

        /*
         * Until we have database lookups for state machines we will to this
         * the hard way.  Iterate through the state machines and look for any
         * matching globalReservationId.
         */
        Collection<StateMachine> machines = stateMachineManager.getStateMachineList();
        String globalReservationId;
        for (StateMachine machine : machines) {
            globalReservationId = machine.getGlobalReservationId();

            if (globalReservationId != null && globalReservationIds.contains(globalReservationId)) {
                logger.debug("getByGlobalReservationId: found match for globalReservationId=" + globalReservationId);
                QuerySummaryResultType result = mapStateMachine(machine);
                summaryList.add(result);
            }
        }
    }

    private void getAllStateMachines(List<QuerySummaryResultType> summaryList) {
        StateMachineManager stateMachineManager = StateMachineManager.getInstance();

        Collection<StateMachine> machines = stateMachineManager.getStateMachineList();
        for (StateMachine machine : machines) {
            logger.debug("getAllStateMachines: found match for connectionId=" +  machine.getConnectionId());
            QuerySummaryResultType result = mapStateMachine(machine);
            summaryList.add(result);
        }
    }

    private QuerySummaryResultType mapStateMachine(StateMachine machine) {
        QuerySummaryResultType result = new QuerySummaryResultType();
        result.setConnectionId(machine.getConnectionId());
        result.setConnectionState(machine.getCurrentState());
        result.setDescription(machine.getDescription());
        result.setGlobalReservationId(machine.getGlobalReservationId());
        result.setPath(machine.getPath());
        result.setServiceParameters(machine.getServiceParameters());

        return result;
    }
}