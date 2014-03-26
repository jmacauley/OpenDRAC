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

package org.opendrac.drac.server.ws.common;

import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.*;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.ws.common.CommonServiceOperations;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.InvalidSessionException;
import com.nortel.appcore.app.drac.server.ws.common.NotAuthenticatedException;
import com.nortel.appcore.app.drac.server.ws.common.OperationFailedException;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache.TokenHolder;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache.UserHolder;

public class CommonServiceOperationsV3 extends CommonServiceOperations {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	public TokenHolder authorize(String serviceName,
	    ServiceUtilities serviceUtil, UserDetailsCache userDetailsCache,
	    RequestHandler rh, MessageContext currMsgContext) throws AxisFault {
		try {
			LoginToken token = null;
			/*
			 * if (inputMsg == null) { throw new
			 * InvalidInputException(DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
			 * new Object[] {
			 * CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE }); }
			 * 
			 * log.debug("Incoming SOAP message for authorization = " +
			 * inputMsg.toString());
			 * 
			 * String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg,
			 * COMMON_DATA_TYPES_NS); if (nsPrefix == null) { throw new
			 * InvalidInputException(DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
			 * new Object[] {
			 * CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE }); }
			 */

			UserHolder h = getUserHolder(serviceName, serviceUtil,
			    RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS, currMsgContext);

			
			try {
				token = rh.authorize(ClientLoginType.WEB_SERVICE_LOGIN, h.getUserId(),
				    null, h.getIpAddress(), null);
				log.debug("User = " + h.getUserId() + " ipAddress = "
				    + h.getIpAddress() + " is authorization successfully.");
				userDetailsCache.addUser(h, token);
				return userDetailsCache.findUser(h);
			}
			catch (Exception e) {
				userDetailsCache.removeUser(h);
				log.error(
				    serviceName + " authorization failed, userId = " + h.getUserId(), e);
				throw new OperationFailedException(
				    DracErrorConstants.WS_OPERATION_FAILED, new Object[] { "login" });
			}
		}
		/*
		 * catch (InvalidInputException e) {
		 * 
		 * log.error("Exception = ", e); throw
		 * serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e); }
		 */
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

	public void deauthorize(TokenHolder th, UserDetailsCache userDetailsCache,
	    ServiceUtilities serviceUtil, RequestHandler rh) {
		try {
			log.debug("deauthorize from " + th);
			rh.logout(th.getLoginToken());
			userDetailsCache.removeUser(th.getHolder());
		}
		catch (RequestHandlerException e) {
			log.error("Exception = ", e);
		}
		catch (Exception e) {
			log.error("Exception = ", e);
		}
	}

	private UserHolder getUserHolder(String serviceName,
	    ServiceUtilities serviceUtil, String ns, MessageContext currMsgContext)
	    throws AxisFault {
		try {
			String certificate = null;

			// userId
			String username = serviceUtil.getUsernameFromSOAPHeader(currMsgContext);

			// ipAddress
			String ipAddress = serviceUtil.getClientIpAddress(currMsgContext);

			if (username == null) {
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
				    new Object[] { RequestResponseConstants.RAW_USERID });
			}

			if (ipAddress == null) {
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
				    new Object[] { "REMOTE_ADDR" });
			}

			log.debug("Authorization for " + serviceName + " : userId = " + username
			    + ", user's ipAddress = " + ipAddress);
			return new UserHolder(username, certificate, ipAddress);
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

}
