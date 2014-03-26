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

package org.opendrac.nsi.actors.provision;

import akka.actor.UntypedActor;
import javax.xml.ws.Holder;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.opendrac.nsi.client.ProviderClientProxy;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.client.GenericConfirmedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link ProvisionConfirmedProviderActor} class models the provider side
 * of the provision confirm message. It will send a provisionConfirmed response
 * to the NSA requesting the provision operation using the NSI protocol.
 * JAX-WS bindings are used to send the SOAP message.
 *
 * @author hacksaw
 */
public class ProvisionConfirmedProviderActor extends UntypedActor {
	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("ProvisionConfirmedProviderActor received message");
        if (NsaMessage.class.isAssignableFrom(message.getClass())) {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("ProvisionConfirmedProviderActor correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    private void process(NsaMessage provisionConfirmed) {

		// Log the incoming message information.
        logger.info("ProvisionConfirmedProviderActor.process: Outgoing message [" +
                "requesterNSA=" + provisionConfirmed.getRequesterNSA() +
                ", providerNSA=" + provisionConfirmed.getProviderNSA() +
                ", correlationId=" + provisionConfirmed.getCorrelationId() +
                ", replyTo=" + provisionConfirmed.getReplyTo() + "]");

        // Allocate and set up an NSI proxy.
        ProviderClientProxy proxy = new ProviderClientProxy();

        // Set the SOAP endpoint for the remote NSA.
        proxy.setRequesterEndpoint(provisionConfirmed.getReplyTo(), provisionConfirmed.getRequesterNSA());

        /*
         * We need to build the provisionConfirmed message and send to the
         * requestingNSA.
         */
        GenericConfirmedType confirmed = GenericConfirmedRequest.generate(provisionConfirmed);

        Holder<String> holder = new Holder<String>(provisionConfirmed.getCorrelationId());

        try {
            logger.info("ProvisionConfirmedProviderActor.process: Issuing provisionConfirmed to NSA" + confirmed.getRequesterNSA());
            proxy.getProxy().provisionConfirmed(holder, confirmed);
            logger.info("ProvisionConfirmedProviderActor.process: completed successfully returning correlationId=" + holder.value);
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException ex) {
            // TODO: Need error handling and retry for communication errors.
            logger.error("ProvisionConfirmedProviderActor.process: received exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            return;
        }

        // We are done.
        logger.info("ProvisionConfirmedProviderActor.process: Completed.");

    }
}
