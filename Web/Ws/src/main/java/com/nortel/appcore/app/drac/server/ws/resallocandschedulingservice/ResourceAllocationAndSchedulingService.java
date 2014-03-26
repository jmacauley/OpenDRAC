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

package com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice;

import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.COMMON_DATA_TYPES_NS;
import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE;
import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS;

import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.StatusType;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.ws.common.Alarms;
import com.nortel.appcore.app.drac.server.ws.common.CommonMessageConstants;
import com.nortel.appcore.app.drac.server.ws.common.CommonServiceOperations;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.InvalidSessionException;
import com.nortel.appcore.app.drac.server.ws.common.NotAuthenticatedException;
import com.nortel.appcore.app.drac.server.ws.common.OperationFailedException;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache;
import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache.TokenHolder;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.common.ResourceAllocationHelper;

public class ResourceAllocationAndSchedulingService {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	protected final RequestHandler requestHandler = RequestHandler.INSTANCE;
	protected final ResourceAllocationHelper helper = new ResourceAllocationHelper();
	protected final ServiceUtilities serviceUtil = new ServiceUtilities();
	protected final UserDetailsCache userDetailsCache = new UserDetailsCache(
	    RES_ALLOC_AND_SCHEDULING_SERVICE);
	protected static final long SCHEDULE_PROGRESS_POLLING_INTERVAL = 5000;
	protected String namespace = RES_ALLOC_AND_SCHEDULING_SERVICE_NS;

	public OMElement activateReservationOccurrence(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE });
			}

			log.debug("Incoming SOAP message for activateReservationSchedule = "
			    + inputMsg.toString());

			String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg,
			    namespace);
			if (nsPrefix == null) {
				log.error("Invalid namespace");
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE });
			}
			th = authorize();
			String occurrenceId = helper
			    .parseActivateReservationOccurrenceRequestOMElement(inputMsg);
			if (occurrenceId == null) {
				log.error("Input message parsing failure");
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.REQUEST_MESSAGE_PARSING_FAILURE });
			}

			requestHandler.activateService(th.getLoginToken(), occurrenceId);
			CommonServiceOperations commonOps = new CommonServiceOperations();
			return commonOps.prepareSuccessfulCompletionResponseOMElement(
			    COMMON_DATA_TYPES_NS, nsPrefix);
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

	public OMElement addReservationOccurrence(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for addReservationOccurrence = "
			    + inputMsg.toString());

			OMElement response = null;
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
			DracService newOccurrence = helper
			    .parseAddReservationOccurrenceRequestOMElement(th.getHolder()
			        .getUserId(), inputMsg);
			if (newOccurrence == null) {
				log.error("Input message parsing failure");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.REQUEST_MESSAGE_PARSING_FAILURE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			String sharedRiskReservationOccurrenceGroup = newOccurrence.getPath()
			    .getSharedRiskServiceGroup();
			String srlgExclusions = newOccurrence.getPath().getSrlg();

			// 
			String scheduleId = newOccurrence.getScheduleId();

			Schedule parent = requestHandler.querySchedule(th.getLoginToken(), scheduleId);

			UserType userType = new UserType(th.getHolder().getUserId(), parent
			    .getUserInfo().getBillingGroup(), parent.getUserInfo()
			    .getSourceEndpointUserGroup(), parent.getUserInfo()
			    .getTargetEndpointUserGroup(), parent.getUserInfo()
			    .getSourceEndpointResourceGroup(), parent.getUserInfo()
			    .getTargetEndpointResourceGroup(), null);

			// userInfo.setUserId(th.getHolder().getUserId());
			// userInfo.setBillingGroup(parent.getUserInfo().getBillingGroup());
			// userInfo.setSourceEndpointUserGroup(parent.getUserInfo().getSourceEndpointUserGroup());
			// userInfo.setTargetEndpointUserGroup(parent.getUserInfo().getTargetEndpointUserGroup());
			// userInfo.setSourceEndpointResourceGroup(parent.getUserInfo().getSourceEndpointResourceGroup());
			// userInfo.setTargetEndpointResourceGroup(parent.getUserInfo().getTargetEndpointResourceGroup());
			newOccurrence.setUserInfo(userType);

			newOccurrence.setSrcTNA(parent.getSrcTNA());
			newOccurrence.setDestTNA(parent.getDestTNA());
			newOccurrence.setRate(parent.getRate());
			newOccurrence.setScheduleId(parent.getId());
			newOccurrence.setScheduleName(parent.getName());
			newOccurrence.setScheduleStartTime(parent.getStartTime());
			newOccurrence.setScheduleEndTime(parent.getEndTime());
			newOccurrence.setScheduleStatus(parent.getStatus());
			newOccurrence.setUserInfo(parent.getUserInfo());
			newOccurrence.setPath(parent.getPath());
			// Update sharedRiskReservationOccurrenceGroup, srlgExclusions and
			// service endtime
			newOccurrence.getPath().setSharedRiskServiceGroup(
			    sharedRiskReservationOccurrenceGroup);
			newOccurrence.getPath().setSrlg(srlgExclusions);

			// TODO: Query schedule not returning vlan ID
			newOccurrence.getPath().setSrcVlanId("4096");
			newOccurrence.getPath().setDstVlanId("4096");

			newOccurrence.setEndTime(newOccurrence.getStartTime()
			    + parent.getDurationLong());

			String occurrenceId = null;
			
			occurrenceId = requestHandler.createService(th.getLoginToken(), scheduleId,
			    newOccurrence);

			// prepare response
			response = helper.prepareAddReservationOccurrenceResponseOMElement(
			    occurrenceId, nsPrefix);
			if (response != null) {
				
			}
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

	public OMElement cancelReservationOccurrence(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for cancelReservationOccurrence = "
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
			String occurenceId = helper
			    .parseCancelReservationOccurrenceRequestOMElement(inputMsg);
			// Cancel service
			requestHandler.cancelService(th.getLoginToken(), occurenceId);
			CommonServiceOperations commonOps = new CommonServiceOperations();
			return commonOps.prepareSuccessfulCompletionResponseOMElement(
			    COMMON_DATA_TYPES_NS, nsPrefix);
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

	public OMElement cancelReservationSchedule(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for cancelReservationSchedule = "
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
			String scheduleId = helper
			    .parseCancelReservationScheduleRequestOMElement(inputMsg);
			if (scheduleId == null) {
				log.error("Input message parsing failure");
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.REQUEST_MESSAGE_PARSING_FAILURE });
			}
			requestHandler.cancelSchedule(th.getLoginToken(), scheduleId);

			CommonServiceOperations commonOps = new CommonServiceOperations();
			return commonOps.prepareSuccessfulCompletionResponseOMElement(
			    COMMON_DATA_TYPES_NS, nsPrefix);
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

	public OMElement confirmReservationSchedule(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for confirmReservationSchedule = "
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
			String scheduleId = helper
			    .parseConfirmReservationScheduleRequestOMElement(inputMsg);
			if (scheduleId == null) {
				log.error("Input message parsing failure");
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
				    new Object[] { CommonMessageConstants.REQUEST_MESSAGE_PARSING_FAILURE });
			}
			requestHandler.confirmSchedule(th.getLoginToken(), scheduleId);

			CommonServiceOperations commonOps = new CommonServiceOperations();
			return commonOps.prepareSuccessfulCompletionResponseOMElement(
			    COMMON_DATA_TYPES_NS, nsPrefix);
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

	public OMElement createReservationSchedule(final OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		// Check input parameter
		try {
			if (inputMsg == null) {
				log.error("Invalid request message");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for createReservationSchedule = "
			    + inputMsg.toString());

			// MessageContext currMsgContext =
			// MessageContext.getCurrentMessageContext();
			// 
			// 

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

			Schedule newSchedule = helper
			    .parseCreateReservationScheduleRequestOMElement(th.getHolder()
			        .getUserId(), inputMsg);
			log.debug("Creating schedule " + newSchedule.toDebugString());
			// Create schedule
			// TaskInfo task = requestHandler.createSchedule(userDetails, newSchedule);
			String scheduleId = requestHandler.createScheduleAsync(th.getLoginToken(),
			    newSchedule);
			TaskType taskType = requestHandler.getProgress(th.getLoginToken(), scheduleId);
			if (taskType != null) {
				while (taskType.getPercentage() < 100
				    && taskType.getExceptionMessage() == null) {
					Thread.sleep(SCHEDULE_PROGRESS_POLLING_INTERVAL);
					taskType = requestHandler.getProgress(th.getLoginToken(), scheduleId);
				}
			}

			OMElement response = null;
			if (taskType != null) {
				
				List<StatusType> statusInfoList = requestHandler.getStatusInfoList(
				    th.getLoginToken(), taskType.getTaskId());
				response = helper.prepareCreateReservationScheduleResponseOMElement(
				    taskType, statusInfoList, nsPrefix,
				    serviceUtil.getUserTimeZoneIdPreference(th.getLoginToken()));
				if (response != null) {
					
					return response;
				}
			}
			log.error("taskInfo is returned as null from the Nrb");
			throw new Exception("Internal error occurred on the OpenDRAC server");
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
		
    catch (ServerException e) {
      final String generalException = "RemoteException occurred in server thread; nested exception is:";
      final String nestedException = "com.nortel.appcore.app.drac.common.errorhandling.NrbException:  : ";
      final String message = e.getMessage().replace(generalException, "").replace(nestedException, "");
      // TODO: Do we have to put in an error code before returning?, meaning we
      // have to parse the error message and act accordingly?
      AxisFault axisFault = new AxisFault(message);
      throw axisFault;
      
    }

    catch (Exception e) {
      log.error("Exception = ", e);
      throw serviceUtil.createDracFault(DracErrorConstants.WS_OPERATION_FAILED, e.getMessage(), e);
    }
    finally {
      deauthorize(th);
    }
	}

	public OMElement queryPathAvailability(OMElement inputMsg) throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for queryPathAvailability = "
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

			OMElement response = null;
			th = authorize();
			DracService service = helper.parseQueryPathAvailabilityRequestOMElement(
			    th.getHolder().getUserId(), inputMsg);
			if (service == null) {
				log.error("Input message parsing failure");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.REQUEST_MESSAGE_PARSING_FAILURE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			boolean result = false;
			// The following method never returns false, instead throws exception
			// if path is not available,
			result = requestHandler.queryPath(th.getLoginToken(), service);

			if (result) {
				CommonServiceOperations commonOps = new CommonServiceOperations();
				response = commonOps.prepareSuccessfulCompletionResponseOMElement(
				    COMMON_DATA_TYPES_NS, nsPrefix);
				if (response != null) {
					
				}
			}
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

	public OMElement queryReservationOccurrence(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for queryReservationOccurrence = "
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
			OMElement response = null;
			th = authorize();
			String occurrenceId = helper
			    .parseQueryReservationOccurrenceRequestOMElement(inputMsg, nsPrefix);
			if (occurrenceId != null) {
				
				DracService service = requestHandler.getService(th.getLoginToken(), occurrenceId);
				if (service != null) {
					
				}
				response = helper.prepareQueryReservationOccurrenceResponseOMElement(
				    service, nsPrefix,
				    serviceUtil.getUserTimeZoneIdPreference(th.getLoginToken()));
				if (response != null) {
					
				}
			}
			return response;
		}
		catch (NotAuthenticatedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (InvalidSessionException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (InvalidInputException e) {
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

	public OMElement queryReservationOccurrenceAlarms(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for queryReservationOccurrenceAlarms = "
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
			OMElement response = null;
			th = authorize();
			Alarms query = new Alarms(inputMsg, namespace, nsPrefix);
			
			List<UserGroupName> userGroups = new ArrayList<UserGroupName>();

			/**
			 * @TODO fix this user group filtering, should be done on the server side
			 */
			UserGroupName userGroup = query.getUserGroup();
			if (userGroup == null || userGroup.toString().equals("")) {
				UserDetails userDetails = requestHandler.getUserDetails(th.getLoginToken());
				if (userDetails.getUserPolicyProfile() != null) {
					// leave userGroups empty if system-admin user, can see
					// everything
					if (userDetails.getUserPolicyProfile().getUserGroupType() != UserGroupType.SYSTEM_ADMIN) {
						userGroups.addAll(requestHandler.getUserGroupProfileNames(th.getLoginToken()));
					}
				}
				else {
					log.error("User policy profile is null for "
					    + th.getHolder().getUserId());
					Object[] args = new Object[1];
					args[0] = CommonMessageConstants.USER_PROFILE_NOT_FOUND;
					throw new OperationFailedException(
					    DracErrorConstants.WS_OPERATION_FAILED, args);
				}
			}
			else {
				userGroups.add(userGroup);
			}
			List<AlarmType> alarms = requestHandler.queryAllServiceAlarms(th.getLoginToken(),
			    query.getStartTime(), query.getEndTime(), userGroups);
			response = query.prepareResponseOMElement(alarms, namespace, nsPrefix,
			    serviceUtil.getUserTimeZoneIdPreference(th.getLoginToken()));
			if (response != null) {
				
			}
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

	public OMElement queryReservationOccurrences(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for queryReservationOccurrences = "
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
			OMElement response = null;
			th = authorize();
			ReservationOccurrences query = new ReservationOccurrences(inputMsg,
			    namespace);
			
			UserGroupName userGroup = query.getUserGroup();
			List<UserGroupName> userGroups = new ArrayList<UserGroupName>();
			/**
			 * @TODO fix the user group filtering, should be done server side
			 */
			if (userGroup == null) {
				UserDetails userDetails = requestHandler.getUserDetails(th.getLoginToken());
				if (userDetails.getUserPolicyProfile() != null) {
					// leave userGroups empty if system-admin user, can see
					// everything
					if (userDetails.getUserPolicyProfile().getUserGroupType() != UserGroupType.SYSTEM_ADMIN) {
						userGroups.addAll(requestHandler.getUserGroupProfileNames(th.getLoginToken()));
					}
				}
				else {
					log.error("User policy profile is null for "
					    + th.getHolder().getUserId());
					Object[] args = new Object[1];
					args[0] = CommonMessageConstants.USER_PROFILE_NOT_FOUND;
					throw new OperationFailedException(
					    DracErrorConstants.WS_OPERATION_FAILED, args);
				}
			}
			else {
				userGroups.add(userGroup);
			}
			List<DracService> services = requestHandler.getAllServices(th.getLoginToken(),
			    query.getStartTime(), query.getEndTime(), userGroups);
			response = query.prepareResponseOMElement(services, nsPrefix, namespace);
			if (response != null) {
				
			}
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

	public OMElement queryReservationSchedule(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("Input message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for queryReservationSchedule = "
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
			OMElement response = null;
			th = authorize();
			if (th != null) {
				String scheduleId = helper
				    .parseQueryReservationScheduleRequestOMElement(inputMsg, nsPrefix);
				if (scheduleId != null) {
					
					Schedule schedule = requestHandler.querySchedule(th.getLoginToken(), scheduleId);
					
					if (schedule != null) {
						log.debug("Retrieved scheduleId = " + schedule.getId() + " --> "
						    + schedule.toString());
					}
					response = helper.prepareQueryReservationScheduleResponseOMElement(
					    schedule, nsPrefix,
					    serviceUtil.getUserTimeZoneIdPreference(th.getLoginToken()));
					if (response != null) {
						
					}
				}
			}
			return response;
		}
		catch (NotAuthenticatedException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (InvalidSessionException e) {
			log.error("Exception = ", e);
			throw serviceUtil.createDracFault(e.getErrorCode(), e.getMessage(), e);
		}
		catch (InvalidInputException e) {
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

	public OMElement queryReservationSchedules(OMElement inputMsg)
	    throws AxisFault {
		TokenHolder th = null;
		try {
			if (inputMsg == null) {
				log.error("ResourceAllocationAndSchedulingServiceBindings_v3.0.wsdlnput message is null");
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.MISSING_SOAP_BODY_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}

			log.debug("Incoming SOAP message for queryReservationSchedules = "
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
			OMElement response = null;
			th = authorize();
			ReservationSchedules query = new ReservationSchedules(inputMsg, namespace);
			UserGroupName userGroup = query.getUserGroup();
			List<UserGroupName> userGroups = new ArrayList<UserGroupName>();
			/**
			 * @TODO Fix this usergroup filtering, should be done server side.
			 */
			if (userGroup == null || userGroup.toString().equals("")) {
				UserDetails userDetails = requestHandler.getUserDetails(th.getLoginToken());
				if (userDetails.getUserPolicyProfile() != null) {
					// leave userGroups empty if system-admin user, can see
					// everything
					if (userDetails.getUserPolicyProfile().getUserGroupType() != UserGroupType.SYSTEM_ADMIN) {
						userGroups.addAll(requestHandler.getUserGroupProfileNames(th.getLoginToken()));
					}
				}
				else {
					log.error("User policy profile is null for " + th.getHolder());
					Object[] args = new Object[1];
					args[0] = CommonMessageConstants.USER_PROFILE_NOT_FOUND;
					throw new OperationFailedException(
					    DracErrorConstants.WS_OPERATION_FAILED, args);
				}
			}
			else {
				userGroups.add(userGroup);
			}

			List<Schedule> schedules = requestHandler.querySchedules(th.getLoginToken(),
			    query.getStartTime(), query.getEndTime(), userGroups);
			response = query.prepareResponseOMElement(schedules, nsPrefix, namespace);
			if (response != null) {
				
			}
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

	

	public OMElement authenticate(OMElement inputMsg) throws AxisFault {
		String fakeUseslessSessionId = ""+new Date().getTime();
		return new CommonServiceOperations().authenticate(
		    RES_ALLOC_AND_SCHEDULING_SERVICE, inputMsg, serviceUtil,
		    userDetailsCache, requestHandler, MessageContext.getCurrentMessageContext(),fakeUseslessSessionId);
	}

	public OMElement disconnect(OMElement inputMsg) throws AxisFault {
		MessageContext currMsgContext = MessageContext.getCurrentMessageContext();
		CommonServiceOperations commonOps = new CommonServiceOperations();
		return commonOps.disconnect(inputMsg, serviceUtil, userDetailsCache, requestHandler,
		    currMsgContext);
	}

	protected TokenHolder authorize() throws Exception {
		return userDetailsCache.validateUser(
		    MessageContext.getCurrentMessageContext(), requestHandler);
	}

	protected void deauthorize(TokenHolder tokenHolder) {
		// does nothing in V2
		return;
	}
}
