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

package org.opendrac.nsi.actors.reserve;

import akka.actor.UntypedActor;
import javax.xml.ws.Holder;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.opendrac.nsi.client.ProviderClientProxy;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link ReserveConfirmedProviderActor} class models the provider side
 * of the reserveConfirmed message, and therefore, sends the
 * reserveConfirmed message back to the requester via the NSI protocol.
 * JAX-WS bindings are used to send the SOAP message.
 *
 * @author hacksaw
 */
public class ReserveConfirmedProviderActor extends UntypedActor {
	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("reserveConfirmedProviderActor.onReceive: received message.");
        if (message == null) {
            logger.error("reserveConfirmedProviderActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("reserveConfirmedProviderActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("reserveConfirmedProviderActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    /**
     * Process the NSI reserveConfirmed message and dispatch to identified
     * NSA.
     *
     * @param reservationMessage Our generic message holding the NSI
     * reserveConfirmed message.
     */
    private void process(NsaMessage reserveConfirmed) {

		// Log the incoming message information.
        logger.info("reserveConfirmedProviderActor.process: Outgoing message [" +
                "requesterNSA=" + reserveConfirmed.getRequesterNSA() +
                ", correlationId=" + reserveConfirmed.getCorrelationId() +
                ", replyTo=" + reserveConfirmed.getReplyTo() + "]");

        // Allocate and set up an NSI proxy.
        ProviderClientProxy proxy = new ProviderClientProxy();

        // Set the SOAP endpoint for the remote NSA.
        proxy.setRequesterEndpoint(reserveConfirmed.getReplyTo(), reserveConfirmed.getRequesterNSA());

        // We need to build the ReservationFailed message and send to requestingNSA.
        ReserveConfirmedType resConfirmed = new ReserveConfirmedType();
        resConfirmed.setProviderNSA(reserveConfirmed.getProviderNSA());
        resConfirmed.setRequesterNSA(reserveConfirmed.getRequesterNSA());
        resConfirmed.setReservation((ReservationInfoType) reserveConfirmed.getPayload());

        /*
         * wsimport did something here overly smart with annotations and
         * unwrapped the reserveConfirmedRequestType into correlationId and
         * the ReserveConfirmedType.  In addition, the GeneicAcknowledgement
         * has been hidden and the correlationId result sent back is made
         * available in the holder object.  Weird but a true story.
         *
         * TODO: see if wsimport can disable this INOUT variable feature.
         */
        Holder<String> holder = new Holder<String>(reserveConfirmed.getCorrelationId());

        try {
            logger.info("reserveConfirmedProviderActor.process: Issuing reserveConfirmed to NSA" + reserveConfirmed.getRequesterNSA());
            proxy.getProxy().reserveConfirmed(holder, resConfirmed);
            logger.info("reserveConfirmedProviderActor.process: completed successfully returning correlationId=" + holder.value);
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException ex) {
            // TODO: Need error handling and retry for communication errors.
            logger.error("reserveConfirmedProviderActor.process: received exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            return;
        }

        // We are done.
        logger.info("reserveConfirmedProviderActor.process: Completed.");

    }
}
