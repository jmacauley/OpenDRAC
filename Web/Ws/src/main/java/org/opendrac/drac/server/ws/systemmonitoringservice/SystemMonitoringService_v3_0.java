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

package org.opendrac.drac.server.ws.systemmonitoringservice;

import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.*;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.opendrac.drac.server.ws.common.CommonServiceOperationsV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.InvalidSessionException;
import com.nortel.appcore.app.drac.server.ws.common.NotAuthenticatedException;
import com.nortel.appcore.app.drac.server.ws.common.OperationFailedException;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache.TokenHolder;
import com.nortel.appcore.app.drac.server.ws.systemmonitoringservice.SystemMonitoringService;

public class SystemMonitoringService_v3_0 extends SystemMonitoringService {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	public SystemMonitoringService_v3_0() {
		namespace = SYSTEM_MONITORING_SERVICE_V3_0_NS;
		helper.setNamespace(SYSTEM_MONITORING_SERVICE_V3_0_NS);
	}

	/**
	 * NB: inputMsg will be NULL because there is no body defined!
	 * 
	 * @param inputMsg
	 * @return
	 * @throws AxisFault
	 */
	@Override
	public OMElement queryServers(OMElement inputMsg) throws AxisFault {
		OMElement response = null;
		String nsPrefix = "sys"; // hardcoded prefix
		TokenHolder th = null;

		log.debug("Incoming SOAP request for queryServers");
		// TODO: How do I programmatically get list of DRAC servers?
		try {
			th = authorize();
			// prepare response
			response = helper.prepareQueryServersResponseOMElement(rh, nsPrefix,
			    serviceUtil.getUserTimeZoneIdPreference(th.getLoginToken()));
			if (response != null) {
				
			}
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
		return response;
	}

	@Override
	protected TokenHolder authorize() throws Exception {
		return new CommonServiceOperationsV3().authorize(SYSTEM_MONITORING_SERVICE,
		    serviceUtil, userDetailsCache, rh,
		    MessageContext.getCurrentMessageContext());
	}

	@Override
	protected void deauthorize(TokenHolder tokenHolder) {
		if (tokenHolder != null) {
			new CommonServiceOperationsV3().deauthorize(tokenHolder,
			    userDetailsCache, serviceUtil, rh);
		}
	}
}
