package org.opendrac.drac.server.ws.networkmonitoringservice;

import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;
import com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.Endpoints;
import com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.request.QueryEndpointsRequest;

public class EndpointsV3 extends Endpoints {
  private final Logger log = LoggerFactory.getLogger(getClass());
	public EndpointsV3(OMElement inputMsg, String nsPrefix, String namespace,
	    LoginToken token) throws InvalidInputException {
		this.inputMessage = inputMsg;
		OMElement selectedNode = null;

		// filterType
		selectedNode = serviceUtil.getNode(this.inputMessage, namespace, nsPrefix,
		    QueryEndpointsRequest.QUERY_ENDPOINTS_REQUEST);
		if (selectedNode != null) {
			OMElement criteria = serviceUtil.getNode(this.inputMessage, namespace,
			    nsPrefix, QueryEndpointsRequest.getTypeXPath(namespace));
			if (criteria != null) {
				String criteriaValue = criteria.getText();
				setQueryType(inputMsg, nsPrefix, namespace, token, criteriaValue);
			}
			else {
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
				    new Object[] { RequestResponseConstants.RAW_TYPE });
			}
		}
		else {
			log.error("No query endpoint request found in the request.");
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST });
		}
	}
}
