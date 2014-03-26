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

package org.opendrac.drac.server.ws.resallocandschedulingservice;

import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.*;

import java.util.Date;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.opendrac.drac.server.ws.common.CommonServiceOperationsV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.ws.common.CommonMessageConstants;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.InvalidSessionException;
import com.nortel.appcore.app.drac.server.ws.common.NotAuthenticatedException;
import com.nortel.appcore.app.drac.server.ws.common.OperationFailedException;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache.TokenHolder;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.ResourceAllocationAndSchedulingService;

public class ResourceAllocationAndSchedulingService_v3_0 extends
    ResourceAllocationAndSchedulingService {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	public void init(MessageContext ctx) {
	}

	public void init(MessageContext inMessge, MessageContext outMessage) {
	}

	public ResourceAllocationAndSchedulingService_v3_0() {
		namespace = RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS;
		helper.setNamespace(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS);
	}

	@Override
	protected TokenHolder authorize() throws Exception {
		return new CommonServiceOperationsV3().authorize(
		    RES_ALLOC_AND_SCHEDULING_SERVICE, serviceUtil, userDetailsCache, requestHandler,
		    MessageContext.getCurrentMessageContext());
	}

	@Override
	protected void deauthorize(TokenHolder tokenHolder) {
		if (tokenHolder != null) {
			new CommonServiceOperationsV3().deauthorize(tokenHolder,
			    userDetailsCache, serviceUtil, requestHandler);
		}
	}
	

	
	// from here
	public OMElement queryReservationScheduleByNamePathUser(OMElement inputMsg) throws AxisFault {
		TokenHolder th = null;
		OMElement response = null;
		try {
			if (inputMsg == null) {
				serviceUtil.handleInvalidInput("Invalid request message",
				        CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}
			log.debug("Incoming SOAP message for queryReservationScheduleByNamePathUser = " + inputMsg.toString());

			String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
			if (nsPrefix == null) {
				serviceUtil.handleInvalidInput("Invalid namespace",
				        CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}

			th = authorize();
			PathType pathType = helper.parsePathTypeSchedule(th.getHolder().getUserId(), inputMsg);
			String scheduleName = helper.parseScheduleName(th.getHolder().getUserId(), inputMsg);
			
			long startTime = 0;
			long endTime = Long.MAX_VALUE;
			Schedule theSchedule = requestHandler.getScheduleByNamePathUser(th.getLoginToken(), startTime, endTime, pathType, scheduleName);
			response = helper.prepareQueryReservationScheduleResponseOMElement(theSchedule, nsPrefix,
			        serviceUtil.getUserTimeZoneIdPreference(th.getLoginToken()));
			if (response != null) {
				
			}			
		} catch (InvalidInputException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (InvalidSessionException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (NotAuthenticatedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (OperationFailedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (RequestHandlerException e) {
			log.error("Exception = ", e);
			throw serviceUtil.inspectRequestHandlerException(e);
		} catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
			        DracErrorConstants.getErrorMessage(null, DracErrorConstants.WS_OPERATION_FAILED, args), e);
		} finally {
			deauthorize(th);
		}
		return response;
	}
	
	public OMElement queryServicesByScheduleId(OMElement inputMsg) throws AxisFault {
		TokenHolder th = null;
		OMElement response = null;
		try {
			if (inputMsg == null) {
				serviceUtil.handleInvalidInput("Invalid request message",
				        CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}
			log.debug("Incoming SOAP message for queryReservationScheduleByNamePathUser = " + inputMsg.toString());

			String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
			if (nsPrefix == null) {
				serviceUtil.handleInvalidInput("Invalid namespace",
				        CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}
			th = authorize();
			String scheduleId = helper.parseScheduleId(th.getHolder().getUserId(), inputMsg);
			Schedule schedule = requestHandler.querySchedule(th.getLoginToken(), scheduleId);
			DracService[] services = schedule.getServiceIdList();

			response = helper.prepareQueryDracServicesByScheduleIdResponseOMElement(services, nsPrefix,
			        serviceUtil.getUserTimeZoneIdPreference(th.getLoginToken()));
			if (response != null) {
				
			}
		} catch (InvalidInputException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (InvalidSessionException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (NotAuthenticatedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (OperationFailedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (RequestHandlerException e) {
			log.error("Exception = ", e);
			throw serviceUtil.inspectRequestHandlerException(e);
		} catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
			        DracErrorConstants.getErrorMessage(null, DracErrorConstants.WS_OPERATION_FAILED, args), e);
		} finally {
			deauthorize(th);
		}
		return response;
	}
	
	public OMElement queryActiveServiceByScheduleId(OMElement inputMsg) throws AxisFault {
		TokenHolder th = null;
		OMElement response = null;
		Date now = new Date();
		try {
			RequestHandler requestHandler = RequestHandler.INSTANCE;
			if (inputMsg == null) {
				serviceUtil.handleInvalidInput("Invalid request message",
				        CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}
			log.debug("Incoming SOAP message for queryReservationScheduleByNamePathUser = " + inputMsg.toString());

			String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
			if (nsPrefix == null) {
				serviceUtil.handleInvalidInput("Invalid namespace",
				        CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}

			th = authorize();
			String scheduleId = helper.parseScheduleId(th.getHolder().getUserId(), inputMsg);
			Schedule schedule = requestHandler.querySchedule(th.getLoginToken(), scheduleId);
			DracService[] services = schedule.getServiceIdList();
			DracService acticeService = null;
			for(DracService service: services){
				if(service.getStartTime()<now.getTime() && now.getTime()<service.getEndTime()){
					acticeService=service;
				}
			}

			response = helper.prepareQueryActiveDracServiceResponseOMElement(acticeService, nsPrefix,
			        serviceUtil.getUserTimeZoneIdPreference(th.getLoginToken()));
			if (response != null) {
				
			}
		
		} catch (InvalidInputException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (InvalidSessionException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (NotAuthenticatedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (OperationFailedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (RequestHandlerException e) {
			log.error("Exception = ", e);
			throw serviceUtil.inspectRequestHandlerException(e);
		} catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
			        DracErrorConstants.getErrorMessage(null, DracErrorConstants.WS_OPERATION_FAILED, args), e);
		} finally {
			deauthorize(th);
		}
		return response;
	}
	
	
	public OMElement resumeServiceById(OMElement inputMsg) throws AxisFault {
		TokenHolder th = null;
		OMElement response = null;
		try {
			if (inputMsg == null) {
				serviceUtil.handleInvalidInput("Invalid request message",
				        CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}
			log.debug("Incoming SOAP message for queryReservationScheduleByNamePathUser = " + inputMsg.toString());

			String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
			if (nsPrefix == null) {
				serviceUtil.handleInvalidInput("Invalid namespace",
				        CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}

			th = authorize();
			String serviceId = helper.parseServiceId(th.getHolder().getUserId(), inputMsg);
		} catch (InvalidInputException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (InvalidSessionException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (NotAuthenticatedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (OperationFailedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (RequestHandlerException e) {
			log.error("Exception = ", e);
			throw serviceUtil.inspectRequestHandlerException(e);
		} catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
			        DracErrorConstants.getErrorMessage(null, DracErrorConstants.WS_OPERATION_FAILED, args), e);
		} finally {
			deauthorize(th);
		}
		return response;
	}	
	public OMElement terminateServiceById(OMElement inputMsg) throws AxisFault {
		TokenHolder th = null;
		OMElement response = null;
		try {
			if (inputMsg == null) {
				serviceUtil.handleInvalidInput("Invalid request message",
				        CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}
			log.debug("Incoming SOAP message for queryReservationScheduleByNamePathUser = " + inputMsg.toString());

			String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
			if (nsPrefix == null) {
				serviceUtil.handleInvalidInput("Invalid namespace",
				        CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE,
				        DracErrorConstants.WS_INVALID_REQUEST_MESSAGE);
			}

			th = authorize();
			String serviceId = helper.parseServiceId(th.getHolder().getUserId(), inputMsg);
		} catch (InvalidInputException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (InvalidSessionException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (NotAuthenticatedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (OperationFailedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		} catch (RequestHandlerException e) {
			log.error("Exception = ", e);
			throw serviceUtil.inspectRequestHandlerException(e);
		} catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
			        DracErrorConstants.getErrorMessage(null, DracErrorConstants.WS_OPERATION_FAILED, args), e);
		} finally {
			deauthorize(th);
		}
		return response;
	}	
	
	public OMElement extendCurrentServiceForSchedule(final OMElement inputMsg) throws AxisFault {
		TokenHolder th = null;
		
		try {

			if (inputMsg == null || inputMsg.toString().equals("")) {
				log.error("Invalid request message");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			final String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
			if (nsPrefix == null) {
				log.error("Invalid namespace");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			th = authorize();

			int timeExtension = helper.parseTimeExtension(th.getHolder().getUserId(), inputMsg).intValue();
			String scheduleId = helper.parseScheduleIdForExtension(th.getLoginToken().getUser(), inputMsg);
			
			DracService service =requestHandler.getCurrentlyActiveServiceByScheduleId(th.getLoginToken(), scheduleId);
			OMElement response;
			if(service == null){
				response = helper.prepareExtendCurrentServiceForScheduleResponse(nsPrefix, "No service currently running", 0);
			}else{
				Integer extendedMinutes = requestHandler.extendServiceTime(th.getLoginToken(), service, timeExtension);
				if(extendedMinutes.intValue()>0){
					response = helper.prepareExtendCurrentServiceForScheduleResponse(nsPrefix, "Service extended", extendedMinutes.intValue());
				}else{
					response = helper.prepareExtendCurrentServiceForScheduleResponse(nsPrefix, "Service could not be extended", extendedMinutes.intValue());
				}
			}
			
			return response;
		}
		catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
			        DracErrorConstants.getErrorMessage(null, DracErrorConstants.WS_OPERATION_FAILED, args), e);
		} finally {
			deauthorize(th);
		}
	}

}
