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

package org.opendrac.nsi.actors.terminate;

import akka.actor.UntypedActor;
import javax.xml.ws.Holder;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.opendrac.nsi.client.ProviderClientProxy;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.client.GenericFailedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class TerminateFailedProviderActor extends UntypedActor {
	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
	public void onReceive(Object message) {
        logger.info("TerminateFailedProviderActor.onReceive: received message.");
        if (message == null) {
            logger.error("TerminateFailedProviderActor.onReceive: received null message");
        }
        else if (!NsaMessage.class.isAssignableFrom(message.getClass())) {
            logger.error("TerminateFailedProviderActor.onReceive: received invalid message type " + message.getClass().getName());
        }
        else {
            NsaMessage nsaMessage = (NsaMessage) message;
            logger.info("TerminateFailedProviderActor.onReceive: correlationId=" +
                    nsaMessage.getCorrelationId() + ", messageType=" +
                    nsaMessage.getMessageType().name());
            process(nsaMessage);
        }
	}

    private void process(NsaMessage terminateFailed) {

		// Log the incoming message information.
        logger.info("TerminateFailedProviderActor.process: Outgoing message [" +
                "requesterNSA=" + terminateFailed.getRequesterNSA() +
                ", providerNSA=" + terminateFailed.getProviderNSA() +
                ", correlationId=" + terminateFailed.getCorrelationId() +
                ", replyTo=" + terminateFailed.getReplyTo() + "]");

        // Allocate and set up an NSI proxy.
        ProviderClientProxy proxy = new ProviderClientProxy();

        // Set the SOAP endpoint for the remote NSA.
        proxy.setRequesterEndpoint(terminateFailed.getReplyTo(), terminateFailed.getRequesterNSA());

        /*
         * We need to build the terminateFailed message and send to the
         * requestingNSA.
         */
        GenericFailedType generic = GenericFailedRequest.generate(terminateFailed);

        Holder<String> holder = new Holder<String>(terminateFailed.getCorrelationId());

        try {
            logger.info("TerminateFailedProviderActor.process: Issuing terminateFailed to NSA" + generic.getRequesterNSA());
            proxy.getProxy().terminateFailed(holder, generic);
            logger.info("TerminateFailedProviderActor.process: completed successfully returning correlationId=" + holder.value);
        }
        catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException ex) {
            // TODO: Need error handling and retry for communication errors.
            logger.info("TerminateFailedProviderActor.process: received exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            return;
        }

        // We are done.
        logger.info("TerminateFailedProviderActor.process: Completed.");

    }

}
