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

package com.nortel.appcore.app.drac.server.ws.networkmonitoringservice;

import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.*;

import java.util.Date;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.ws.common.CommonMessageConstants;
import com.nortel.appcore.app.drac.server.ws.common.CommonServiceOperations;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.InvalidSessionException;
import com.nortel.appcore.app.drac.server.ws.common.NotAuthenticatedException;
import com.nortel.appcore.app.drac.server.ws.common.OperationFailedException;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache.TokenHolder;
import com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.common.NetworkMonitoringHelper;

public class NetworkMonitoringService {
  private final Logger log = LoggerFactory.getLogger(getClass());
	protected final RequestHandler rh = RequestHandler.INSTANCE;
	protected final ServiceUtilities serviceUtil = new ServiceUtilities();
	protected final UserDetailsCache userDetailsCache = new UserDetailsCache(
	    NETWORK_MONITORING_SERVICE);

	protected NetworkMonitoringHelper helper = new NetworkMonitoringHelper();
	protected String namespace = NETWORK_MONITORING_SERVICE_NS;

	public OMElement authenticate(OMElement inputMsg) throws AxisFault {
		MessageContext currMsgContext = MessageContext.getCurrentMessageContext();
		CommonServiceOperations commonOps = new CommonServiceOperations();
		String fakeUseslessSessionId = ""+new Date().getTime();
		return commonOps.authenticate(NETWORK_MONITORING_SERVICE, inputMsg,
		    serviceUtil, userDetailsCache, rh, currMsgContext,fakeUseslessSessionId);
	}

	public OMElement disconnect(OMElement inputMsg) throws AxisFault {
		MessageContext currMsgContext = MessageContext.getCurrentMessageContext();
		CommonServiceOperations commonOps = new CommonServiceOperations();
		return commonOps.disconnect(inputMsg, serviceUtil, userDetailsCache, rh,
		    currMsgContext);
	}

	public OMElement queryEndpoint(OMElement inputMsg) throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE });
			}

			log.debug("Incoming SOAP message for queryEndpoint = "
			    + inputMsg.toString());

			String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg,
			    namespace);
			if (nsPrefix == null) {
				log.error("Invalid namespace");
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE });
			}
			OMElement response = null;
			th = authorize();
			String tna = helper
			    .parseQueryEndpointRequestOMElement(inputMsg, nsPrefix);
			final EndPointType endpoint = rh.findEndpointByTna(th.getLoginToken(), tna);
			final double utilization = rh.getUtilization(th.getLoginToken(), tna);
			final Map<String, String> attributes = endpoint.getAttributes();
			attributes.put("bandwidthUtilization", Double.toString(utilization));
			endpoint.setAttributes(attributes);
			response = helper.prepareQueryEndpointResponseOMElement(endpoint,
			    nsPrefix);
			return response;

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
		finally {
			deauthorize(th);
		}
	}

	public OMElement queryEndpoints(OMElement inputMsg) throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for queryEndpoints = "
			    + inputMsg.toString());

			String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg,
			    namespace);
			if (nsPrefix == null) {
				log.error("Invalid namespace");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}
			th = authorize();
			Endpoints query = helper.endpointsFactory(inputMsg, nsPrefix,
			    th.getLoginToken());
			EndpointsQuery queryType = query.getQueryType();
			return queryType.prepareResponseOMElement();

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
		catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
			    DracErrorConstants.getErrorMessage(null,
			        DracErrorConstants.WS_OPERATION_FAILED, args), e);
		}
		finally {
			deauthorize(th);
		}
	}

	protected TokenHolder authorize() throws Exception {
		return userDetailsCache.validateUser(
		    MessageContext.getCurrentMessageContext(), rh);
	}

	protected void deauthorize(TokenHolder tokenHolder) {
		// does nothing in V2
		return;
	}
}
