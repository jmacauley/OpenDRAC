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
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.DeclareRoles;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

// 3rd Party Imports.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// GLIF Fenius WSDL imports.
import is.glif.fenius.ws.connection.v1.service.ExternalFault;
import is.glif.fenius.ws.connection.v1.service.InternalFault;

/**
 * {@link ConnectionService} is the wsimport generated endpoint class for
 * the Fenius web service.  We do as little as possible in this class just in
 * case we need to replace it in the future.  This is now generated with
 * every WSDL compile.
 *
 * @author hacksaw
 */
@WebService(serviceName = "connection", portName = "ConnectionServicePort", endpointInterface = "is.glif.fenius.ws.connection.v1.service.ConnectionService", targetNamespace = "http://fenius.glif.is/ws/connection/v1/service", wsdlLocation = "WEB-INF/wsdl/ConnectionService/ConnectionService.wsdl")
@DeclareRoles("FeniusUserGroup")
public class ConnectionService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void myInit() {
		logger.debug("ConnectionService: instantiated");
	}

	@PreDestroy
	public void myDestroy() {
		logger.debug("ConnectionService: destroying");
	}

	// This holds the web service request context which includes all the
	// original HTTP information.
	@Resource
	private WebServiceContext wsc;

	/**
	 * The reserve method processes a Fenius reserve request for bandwidth.
	 *
	 * @param arg0	The unmarshaled JAXB object hold the Fenius reserve request.
	 * @return		The ReserveResp object holding the result of the reserve request.
	 * @throws		ExternalFault or InternalFault depending on the type of error.
	 */
	public is.glif.fenius.ws.connection.v1._interface.ReserveResp reserve(is.glif.fenius.ws.connection.v1._interface.ReserveReq arg0) throws ExternalFault, InternalFault {
		ReserveHandler handler = new ReserveHandler();
		handler.setContext(wsc);
		return handler.process(arg0);
	}

	/**
	 * The query method processes a Fenius query for information relating to a
	 * previously issued reservation.
	 *
	 * @param arg0	The un-marshaled JAXB object hold the Fenius query request.
	 * @return		The QueryResp object holding the reservation information returned by the query.
	 * @throws		ExternalFault or InternalFault depending on the type of error.
	 */
	public is.glif.fenius.ws.connection.v1._interface.QueryResp query(is.glif.fenius.ws.connection.v1._interface.QueryReq arg0) throws ExternalFault, InternalFault {
		QueryHandler handler = new QueryHandler();
		handler.setContext(wsc);
		return handler.process(arg0);
	}

	/**
	 * The release method processes a Fenius release operation which terminates
	 * a reservation.
	 *
	 * @param arg0	The un-marshaled JAXB object hold the Fenius release request.
	 * @return		The ReleaseResp object holding the result of the release operation.
	 * @throws		ExternalFault or InternalFault depending on the type of error.
	 */
	public is.glif.fenius.ws.connection.v1._interface.ReleaseResp release(is.glif.fenius.ws.connection.v1._interface.ReleaseReq arg0) throws ExternalFault, InternalFault {
		ReleaseHandler handler = new ReleaseHandler();
		handler.setContext(wsc);
		return handler.process(arg0);
	}

	/**
	 * The list method processes a Fenius list operation to return a list of
	 * reservations.
	 *
	 * @param arg0	The un-marshaled JAXB object hold the Fenius list request.
	 * @return		The ListResp object holding the result of the list operation.
	 * @throws		ExternalFault or InternalFault depending on the type of error.
	 */
	public is.glif.fenius.ws.connection.v1._interface.ListResp list(is.glif.fenius.ws.connection.v1._interface.ListReq arg0) throws InternalFault, ExternalFault {
		ListHandler handler = new ListHandler();
		handler.setContext(wsc);
		return handler.process(arg0);
	}
}
