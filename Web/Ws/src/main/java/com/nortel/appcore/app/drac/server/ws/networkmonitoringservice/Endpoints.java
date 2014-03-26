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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;
import com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.request.QueryEndpointsRequest;

public class Endpoints {
  private final Logger log = LoggerFactory.getLogger(getClass());
	protected final ServiceUtilities serviceUtil = new ServiceUtilities();
	protected OMElement inputMessage;
	protected EndpointsQuery queryType;

	protected Endpoints() {
		super();
	}

	public Endpoints(OMElement inputMsg, String nsPrefix, String namespace,
	    LoginToken token) throws InvalidInputException {
		this.inputMessage = inputMsg;
		OMElement selectedNode = null;

		// filterType
		selectedNode = serviceUtil.getNode(this.inputMessage, namespace, nsPrefix,
		    QueryEndpointsRequest.CRITERIA);
		if (selectedNode != null) {
			OMAttribute criteria = selectedNode.getAttribute(new QName(
			    XML_SCHEMA_INSTANCE_NS, "type", XML_SCHEMA_INSTANCE_NS_PREFIX));
			if (criteria != null) {
				String criteriaValue = criteria.getAttributeValue();
				setQueryType(inputMsg, nsPrefix, namespace, token, criteriaValue);
			}
		}
		else {
			log.error("No query criteria found in the request.");
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { RequestResponseConstants.RAW_QUERY_CRITERIA });
		}
	}

	protected void setQueryType(OMElement inputMsg, String nsPrefix,
	    String namespace, LoginToken token, String criteriaValue)
	    throws InvalidInputException {
		if (criteriaValue == null) {
			log.error("null criteria while querying endpoints.");
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { RequestResponseConstants.RAW_QUERY_CRITERIA });
		}
		if (criteriaValue.indexOf(':') != -1) {
			criteriaValue = criteriaValue.substring(criteriaValue.indexOf(':') + 1);
			
		}

		if (criteriaValue
		    .compareTo(RequestResponseConstants.RAW_QUERY_ENDPOINTS_BY_LAYER_AND_USER_GROUP) == 0) {
			queryType = new QueryEndpointsByLayerAndUserGroup(inputMsg, token,
			    nsPrefix, namespace);
		}
		else if (criteriaValue
		    .compareTo(RequestResponseConstants.RAW_QUERY_ENDPOINTS_BY_WAVELENGTH) == 0) {
			queryType = new QueryEndpointsByWavelength(inputMsg, token, nsPrefix,
			    namespace);
		}
		else if (criteriaValue
		    .compareTo(RequestResponseConstants.RAW_QUERY_ENDPOINTS_BY_SITEID) == 0) {
			queryType = new QueryEndpointsBySiteId(inputMsg, token, nsPrefix,
			    namespace);
		}
		else {
			log.error("Unsupported query criteria = " + criteriaValue);
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    new Object[] { criteriaValue, RequestResponseConstants.RAW_TYPE });
		}
	}

	public EndpointsQuery getQueryType() {
		return queryType;
	}
}