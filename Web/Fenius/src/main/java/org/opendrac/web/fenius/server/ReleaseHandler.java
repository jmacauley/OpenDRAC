/**
 * Copyright (c) 2010, SURFnet bv, The Netherlands
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
package org.opendrac.web.fenius.server;

import is.glif.fenius.ws.connection.v1._interface.ReleaseReq;
import is.glif.fenius.ws.connection.v1._interface.ReleaseResp;
import is.glif.fenius.ws.connection.v1.service.ExternalFault;
import is.glif.fenius.ws.connection.v1.service.InternalFault;
import is.glif.fenius.ws.connection.v1.types.ReservationStatus;

import org.opendrac.web.fenius.utilities.DateUtils;
import org.opendrac.web.fenius.utilities.MessageDump;
import org.opendrac.web.fenius.utilities.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;

/**
 *
 * @author hacksaw
 */
public class ReleaseHandler extends FeniusHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());

	public ReleaseResp process(ReleaseReq releaseReq) throws InternalFault, ExternalFault {
		// Base for our error string.
		is.glif.fenius.ws.connection.v1.faults.ExternalFault externalFault =
				new is.glif.fenius.ws.connection.v1.faults.ExternalFault();
		is.glif.fenius.ws.connection.v1.faults.InternalFault internalFault =
				new is.glif.fenius.ws.connection.v1.faults.InternalFault();
		StringBuilder error = new StringBuilder("OpenDRAC: ReleaseHandler.process ");
		error.append(DateUtils.now());
		error.append(" - ");

		SecurityContext securityContext = this.getSecurityContext();
		if (securityContext == null || releaseReq == null) {
			// Build the error string.
			error.append("operation failed due to null parameter.");
			String errMsg = error.toString();
			externalFault.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, externalFault);
		}

		// Log the incoming message.
		if (logger.isDebugEnabled()) {
			logger.debug("ReleaseHandler.process: Incoming message ["
					+ securityContext.getUserName() + " @ "
					+ securityContext.getRemoteAddr() + "]");
			logger.debug(MessageDump.dump(ReleaseReq.class, releaseReq));
		}

		// We drop the <credentials> and only need the <reservationId>.
		String reservationId = releaseReq.getReservationId();
		if (reservationId == null || reservationId.isEmpty()) {
			// Build the error string.
			error.append("operation failed due to null reservationId parameter.");
			String errMsg = error.toString();
			externalFault.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, externalFault);
		}

		// We are authenticated so let us get down to business.  Map the Fenius
		// query request through to a DRAC querySchedule request.
		RequestHandler rh = RequestHandler.INSTANCE;
		try {
			rh.cancelSchedule(securityContext.getUserToken(), reservationId);
		} catch (Exception e) {
			// Build the error string.
			error.append("OpenDRAC cancelSchedule operation failed with exception - ");
			error.append(e.toString());
			String errMsg = error.toString();
			internalFault.setMessage(errMsg);
			logger.error(errMsg);
			throw new InternalFault(errMsg, internalFault);
		}

		ReleaseResp resp = new ReleaseResp();
		resp.setReservationStatus(ReservationStatus.RELEASING);

		logger.debug("QueryHandler.process: returning reservation for id = "
				+ reservationId);

		return resp;
	}
}
