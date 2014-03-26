/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.server.ws.common;

import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.*;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache.TokenHolder;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache.UserHolder;

public class CommonServiceOperations {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	public OMElement authenticate(String serviceName, OMElement inputMsg,
	    ServiceUtilities serviceUtil, UserDetailsCache userDetailsCache,
	    RequestHandler rh, MessageContext currMsgContext, String sessionId) throws AxisFault {
		try {
			LoginToken token = null;
			if (inputMsg == null) {
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE });
			}

			log.debug("Incoming SOAP message for authenticate = "
			    + inputMsg.toString());

			String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg,
			    COMMON_DATA_TYPES_NS);
			if (nsPrefix == null) {
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE });
			}

			UserHolder h = parseAuthenticateRequestOMElement(serviceName, inputMsg,
			    serviceUtil, COMMON_DATA_TYPES_NS, currMsgContext);

			// see if the user in our cache
			TokenHolder t = userDetailsCache.findUser(h);
			if (t != null) {

				// user is already in our cache, validate his session
				log.debug("User authenticated previously. Just validating session for  "
				    + t);
				try {
					rh.sessionValidate(t.getLoginToken());
					
					return prepareSuccessfulCompletionResponseOMElement(
					    COMMON_DATA_TYPES_NS, nsPrefix);
				}
				catch (Exception e) {
					
					userDetailsCache.removeUser(h);
				}
			}

			
			try {
				token = rh.login(ClientLoginType.WEB_SERVICE_LOGIN, h.getUserId(), h
				    .getPassword().toCharArray(), h.getIpAddress(), null, sessionId);
				log.debug("User = " + h.getUserId() + " ipAddress = "
				    + h.getIpAddress() + " is authenticated successfully.");
				userDetailsCache.addUser(h, token);
				return prepareSuccessfulCompletionResponseOMElement(
				    COMMON_DATA_TYPES_NS, nsPrefix);
			}
			catch (Exception e) {
				userDetailsCache.removeUser(h);
				log.error(
				    serviceName + " authentication failed, userId = " + h.getUserId(),
				    e);
				throw new OperationFailedException(
				    DracErrorConstants.WS_OPERATION_FAILED, new Object[] { "login" });
			}
		}
		catch (InvalidInputException e) {

			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (OperationFailedException e) {

			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (Exception e) {

			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
			    DracErrorConstants.getErrorMessage(null,
			        DracErrorConstants.WS_OPERATION_FAILED, args), e);
		}
	}

	public OMElement disconnect(OMElement inputMsg, ServiceUtilities serviceUtil,
	    UserDetailsCache userDetailsCache, RequestHandler rh,
	    MessageContext currMsgContext) throws AxisFault {
		String nsPrefix = null;
		try {
			nsPrefix = serviceUtil.getNsPrefixFromHeader(currMsgContext,
			    COMMON_DATA_TYPES_NS);
			if (nsPrefix == null) {
				log.error("Invalid namespace");
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE });
			}

			TokenHolder th = userDetailsCache.validateUser(
			    MessageContext.getCurrentMessageContext(), rh);
			log.debug("Disconnect request from " + th);
			rh.logout(th.getLoginToken());
			userDetailsCache.removeUser(th.getHolder());
			return prepareSuccessfulCompletionResponseOMElement(COMMON_DATA_TYPES_NS,
			    nsPrefix);

		}
		catch (InvalidInputException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (InvalidSessionException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (NotAuthenticatedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (OperationFailedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (RequestHandlerException e) {
			log.error("Exception = ", e);
			throw serviceUtil.inspectRequestHandlerException(e);
		}
		catch (Exception e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
			    DracErrorConstants.getErrorMessage(null,
			        DracErrorConstants.WS_OPERATION_FAILED,
			        new Object[] { e.getMessage() }), e);
		}
	}

	public OMElement prepareSuccessfulCompletionResponseOMElement(
	    String serviceNs, String serviceNsPrefix) {
		if (serviceNsPrefix.equals("xmlns")) {
			serviceNsPrefix = "";
		}

		// Prepare response
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(serviceNs, serviceNsPrefix);
		OMElement response = fac.createOMElement(
		    RequestResponseConstants.RAW_COMPLETION_RESPONSE, ns);
		// result
		OMElement resultElement = fac.createOMElement(
		    RequestResponseConstants.RAW_RESULT, ns, response);
		resultElement.setText(CommonMessageConstants.COMPLETED_SUCCESSFULLY);
		return response;
	}

	private UserHolder parseAuthenticateRequestOMElement(String serviceName,
	    OMElement inputMsg, ServiceUtilities serviceUtil, String ns,
	    MessageContext currMsgContext) throws InvalidInputException {
		OMElement selectedNode = null;
		String userId = null;
		String certificate = null;
		String ipAddress = null;
		String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, ns);

		// userId
		selectedNode = serviceUtil.getNode(inputMsg, ns, nsPrefix,
		    AuthenticationRequest.USER_ID);
		if (selectedNode != null) {
			userId = selectedNode.getText().trim();
		}
		else {
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { RequestResponseConstants.RAW_USERID });
		}
		// certificate
		selectedNode = serviceUtil.getNode(inputMsg, ns, nsPrefix,
		    AuthenticationRequest.CERTIFICATE);
		if (selectedNode != null) {
			certificate = selectedNode.getText().trim();
		}
		else {
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { RequestResponseConstants.RAW_CERTIFICATE });
		}
		// ipAddress
		ipAddress = serviceUtil.getClientIpAddress(currMsgContext);

		if (userId == null) {
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { RequestResponseConstants.RAW_USERID });
		}

		if (certificate == null) {
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { RequestResponseConstants.RAW_CERTIFICATE });
		}

		if (ipAddress == null) {
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { "REMOTE_ADDR" });
		}

		CryptedString encryptedCertificate = null;
		try {
			encryptedCertificate = CryptoWrapper.INSTANCE.encrypt(certificate);
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
		log.debug("AuthenticationRequest for " + serviceName + " : userId = "
		    + userId + ", certificate = " + encryptedCertificate
		    + ", user's ipAddress = " + ipAddress);
		return new UserHolder(userId, certificate, ipAddress);
	}

}
