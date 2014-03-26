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
package org.opendrac.nsi.actors.messages;

import java.io.Serializable;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendrac.nsi.security.NsaSecurityContext;

/**
 *
 * @author hacksaw
 */
public class NsaMessage implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static enum MessageType {
        reserveProvider,
        reserveConfirmedProvider,
        reserveFailedProvider,

        reserveRequesterRemote,
        reserveRequesterLocal,
        reserveConfirmedRequester,
        reserveFailedRequester,

        provisionProvider,
        provisionConfirmedProvider,
        provisionFailedProvider,

        provisionRequesterRemote,
        provisionRequesterLocal,
        provisionConfirmedRequester,
        provisionFailedRequester,

        releaseProvider,
        releaseConfirmedProvider,
        releaseFailedProvider,

        releaseRequesterRemote,
        releaseRequesterLocal,
        releaseConfirmedRequester,
        releaseFailedRequester,

        terminateProvider,
        terminateConfirmedProvider,
        terminateFailedProvider,

        terminateRequesterRemote,
        terminateRequesterLocal,
        terminateConfirmedRequester,
        terminateFailedRequester,

        querySummaryProvider,
        queryDetailsProvider,
        queryConfirmedProvider,
        queryFailedProvider,

        queryRequesterRemote,
        queryRequesterLocal,
        queryConfirmedRequester,
        queryFailedRequester,

        unknown
    }

    private MessageType messageType = MessageType.unknown;

    private NsaSecurityContext nsaSecurityContext = null;

    // Common NSI protocol fields.
    private String correlationId = null;
    private String replyTo = null;

    // Common NSI message header fields.
    private String requesterNSA = null;
    private String providerNSA = null;
    private String connectionId = null;

    /**
     * Used in provider response messages being sent back to requester for some
     * messages.
     */
    private String globalReservationId = null;
    private ConnectionStateType connectionState = ConnectionStateType.INITIAL;

    // Contents of original NSI message without header content.
    private Object payload = null;

    /**
     * @return the securityContext
     */
    public NsaSecurityContext getNsaSecurityContext() {
        return nsaSecurityContext;
    }

    /**
     * @param securityContext the securityContext to set
     */
    public void setNsaSecurityContext(NsaSecurityContext nsaSecurityContext) {
        this.nsaSecurityContext = nsaSecurityContext;
    }

    /**
     * @return the correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * @return the replyTo
     */
    public String getReplyTo() {
        return replyTo;
    }


    /**
     * @param replyTo the replyTo to set
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * @return the providerNSA
     */
    public String getProviderNSA() {
        return providerNSA;
    }

    /**
     * @param providerNSA the providerNSA to set
     */
    public void setProviderNSA(String providerNSA) {
        this.providerNSA = providerNSA;
    }

    /**
     * @return the globalReservationId
     */
    public String getGlobalReservationId() {
        return globalReservationId;
    }

    /**
     * @param globalReservationId the globalReservationId to set
     */
    public void setGlobalReservationId(String globalReservationId) {
        this.globalReservationId = globalReservationId;
    }

    /**
     * @return the connectionState
     */
    public ConnectionStateType getConnectionState() {
        return connectionState;
    }

    /**
     * @param connectionState the connectionState to set
     */
    public void setConnectionState(ConnectionStateType connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * @return the requesterNSA
     */
    public String getRequesterNSA() {
        return requesterNSA;
    }

    /**
     * @param requesterNSA the requesterNSA to set
     */
    public void setRequesterNSA(String requesterNSA) {
        this.requesterNSA = requesterNSA;
    }

    /**
     * @return the connectionId
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * @param connectionId the connectionId to set
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * @return the messageType
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @param messageType the messageType to set
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * @return the requestPayload
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * @param requestPayload the requestPayload to set
     */
    public void setPayload(Object payload) {
        this.payload = payload;
    }

}
