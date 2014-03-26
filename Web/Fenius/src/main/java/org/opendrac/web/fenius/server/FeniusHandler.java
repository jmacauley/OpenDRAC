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

// Standard java import.
import javax.xml.ws.WebServiceContext;

// Third-party imports.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// OpenDRAC imports.
import org.opendrac.web.fenius.utilities.DateUtils;
import org.opendrac.web.fenius.utilities.SecurityContext;
import org.opendrac.web.security.jaas.DracRolePrincipal;

// GLIF Fenius WSDL imports.
import is.glif.fenius.ws.connection.v1.service.InternalFault;
import is.glif.fenius.ws.connection.v1.service.ExternalFault;

/**
 *
 * @author hacksaw
 */
public class FeniusHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());
	private SecurityContext securityContext = null;

	public void setContext(WebServiceContext wsc)
			throws InternalFault, ExternalFault {

		// Base for our error string.
		StringBuilder error = new StringBuilder("OpenDRAC: FeniusHandler() - ");
		error.append(DateUtils.now());
		error.append(" - ");

		// Build the security context.
		try {
			SecurityContext sec = new SecurityContext();
			sec.setContext(wsc);
			securityContext = sec;
		} catch (Exception e) {
			// Build the error string.
			error.append("invalid security context - ");
			error.append(e.getMessage());

			is.glif.fenius.ws.connection.v1.faults.InternalFault faultInfo =
					new is.glif.fenius.ws.connection.v1.faults.InternalFault();
			String errMsg = error.toString();
			faultInfo.setMessage(errMsg);
			logger.error(errMsg);
			throw new InternalFault(errMsg, faultInfo);
		}

		/*
		 * Now would be an appropriate time to check access control to the
		 * fenius interface.  This should be changed to method level access
		 * control in the future.  Redundant with the @DeclareRoles("fenius")
		 * annotation.
		 */
		if (!securityContext.isUserInRole(DracRolePrincipal.ROLE_FENIUS)) {
			// Build the error string.
			error.append("access control violation - user is not in fenius role.");

			is.glif.fenius.ws.connection.v1.faults.ExternalFault faultInfo =
					new is.glif.fenius.ws.connection.v1.faults.ExternalFault();
			String errMsg = error.toString();
			faultInfo.setMessage(errMsg);
			logger.error(errMsg);
			throw new ExternalFault(errMsg, faultInfo);
		}
	}

	public SecurityContext getSecurityContext() {
		return securityContext;
	}
}
