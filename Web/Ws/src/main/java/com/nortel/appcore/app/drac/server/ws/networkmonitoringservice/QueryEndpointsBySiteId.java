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

import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.ws.common.CommonConstants;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.OperationFailedException;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;
import com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.request.QueryEndpointsRequest;

public final class QueryEndpointsBySiteId implements EndpointsQuery {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final LoginToken token;
	private String nsPrefix;
	private String namespace;
	private final OMElement inputMsg;
	private final RequestHandler rh = RequestHandler.INSTANCE;

	public QueryEndpointsBySiteId(OMElement msg, LoginToken userToken,
	    String prefix, String namespace) {
		inputMsg = msg;
		token = userToken;
		nsPrefix = prefix;
		this.namespace = namespace;
	}

	@Override
	public OMElement prepareResponseOMElement() throws InvalidInputException,
	    RequestHandlerException, OperationFailedException, Exception {
		OMElement response = null;
		OMElement selectedNode = null;
		ServiceUtilities serviceUtil = new ServiceUtilities();
		String siteId = null;

		// siteId
		if ((selectedNode = serviceUtil.getNode(this.inputMsg,
		    NETWORK_MONITORING_SERVICE_NS, nsPrefix,
		    QueryEndpointsRequest.getSiteIDXPath(namespace))) != null) {
			siteId = selectedNode.getText().trim();
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_SITEID;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		List<EndpointResourceUiType> endpoints = rh.getEndpointsForSiteId(token,
		    siteId);
		

		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(NETWORK_MONITORING_SERVICE_NS,
		    nsPrefix);
		response = fac.createOMElement(
		    RequestResponseConstants.RAW_QUERY_ENDPOINTS_RESPONSE, ns);
		OMElement numOfElements = fac.createOMElement(
		    RequestResponseConstants.RAW_NUM_OF_ELEMENTS_INCLUDED, ns, response);
		OMElement totalNumOfElements = fac.createOMElement(
		    RequestResponseConstants.RAW_TOTAL_NUM_OF_MATCHING_ELEMENTS, ns,
		    response);

		// Dead code
		// if (endpoints == null) {
		// numOfElements.setText("0");
		// totalNumOfElements.setText("0");
		// return response;
		// }
		if (endpoints.isEmpty()) {
			numOfElements.setText("0");
			totalNumOfElements.setText("0");
			return response;
		}
		boolean reducedList = true;
		totalNumOfElements.setText(new Integer(endpoints.size()).toString());
		if (endpoints.size() < CommonConstants.MAX_NUM_OF_ENDPOINTS_TO_BE_RETRIEVED) {
			numOfElements.setText(new Integer(endpoints.size()).toString());
			reducedList = false;
		}
		else {
			numOfElements.setText(new Integer(
			    CommonConstants.MAX_NUM_OF_ENDPOINTS_TO_BE_RETRIEVED).toString());
		}
		int i = 0;
		for (EndpointResourceUiType endpoint : endpoints) {

			OMElement tna = fac.createOMElement(RequestResponseConstants.RAW_TNA, ns,
			    response);
			tna.setText(endpoint.getTna());

			i++;
			if (reducedList
			    && i == CommonConstants.MAX_NUM_OF_ENDPOINTS_TO_BE_RETRIEVED) {
				break;
			}
		}
		return response;
	}
}