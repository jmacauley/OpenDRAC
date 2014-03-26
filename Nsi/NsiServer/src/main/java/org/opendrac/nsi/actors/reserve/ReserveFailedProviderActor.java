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
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.opendrac.nsi.client.ProviderClientProxy;
import org.opendrac.nsi.client.GenericFailedRequest;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link ReserveFailedProviderActor} class models the provider side
 * of the ReserveFailed message, and therefore, sends the ReserveFailed
 * message back to the requester via the NSI protocol.  JAX-WS bindings are
 * used to send the SOAP message.
 *
 * @author hacksaw
 */
public class ReserveFailedProviderActor extends UntypedActor {
	private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@link onReceive} receives messages sent to this UntypedActor.  We
     * validate that an NsaMessage was received then delegate for processing.
     *
     * @param message Message to process.
     */
    @Override
	public void onReceive(Object message) {
        logger.info("ReserveFailedProviderActor.onReceive: received message.");
        if (message == null) {
            logger.error("ReserveFailedProviderActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("ReserveFailedProviderActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ReserveFailedProviderActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    /**
     * Process the NSI reserveFailed message and dispatch to identified
     * NSA.
     *
     * @param reservationMessage Our generic message holding the NSI reservation message.
     */
    private void process(NsaMessage reserveFailed) {

		// Log the incoming message information.
		if (logger.isDebugEnabled()) {
			logger.debug("ReserveFailedProviderActor.process: Outgoing message [" +
                    "requesterNSA=" + reserveFailed.getRequesterNSA() +
                    ", correlationId=" + reserveFailed.getCorrelationId() +
                    ", replyTo=" + reserveFailed.getReplyTo() + "]");
		}

        // Allocate and set up an NSI proxy.
        ProviderClientProxy proxy = new ProviderClientProxy();

        // Set the SOAP endpoint for the remote NSA.
        proxy.setRequesterEndpoint(reserveFailed.getReplyTo(), reserveFailed.getRequesterNSA());

        // We need to build the ReserveFailed message and send to requestingNSA.
        GenericFailedType generic = GenericFailedRequest.generate(reserveFailed);

        Holder<String> holder = new Holder<String>(reserveFailed.getCorrelationId());

        try {
            logger.info("ReserveFailedProviderActor.process: Issuing ReserveFailed to NSA " + reserveFailed.getRequesterNSA());
            proxy.getProxy().reserveFailed(holder, generic);
            logger.info("ReserveFailedProviderActor.process: completed successfully returning correlationId=" + holder.value);
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException ex) {
            // TODO: Need error handling and retry for communication errors.
            logger.error("ReserveFailedProviderActor.process: received exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            return;
        }

        // We are done.
        logger.info("ReserveFailedProviderActor.process: Completed.");

    }
}
