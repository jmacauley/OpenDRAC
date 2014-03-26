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

package com.nortel.appcore.app.drac.server.nrb.impl.administration;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginToken;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.errorhandling.DracRemotePolicyException;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.errorhandling.ExceptionFormatter;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.errorhandling.ResourceKey;
import com.nortel.appcore.app.drac.common.errorhandling.RoutingException;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.common.types.AuditResult;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;
import com.nortel.appcore.app.drac.common.types.CallIdType;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.GraphData;
import com.nortel.appcore.app.drac.common.types.LpcpStatus;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.RecurrenceType.RecurrenceFreq;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.common.types.State.CALL;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.common.types.StatusType;
import com.nortel.appcore.app.drac.common.types.StatusType.StatusInfoType;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.TaskType.ACTIVITY;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;
import com.nortel.appcore.app.drac.database.helper.DbUtilityCommonUtility;
import com.nortel.appcore.app.drac.security.SecurityServer;
import com.nortel.appcore.app.drac.server.lpcp.rmi.LpcpInterface;
import com.nortel.appcore.app.drac.server.nrb.LpcpEventCallback;
import com.nortel.appcore.app.drac.server.nrb.impl.mlbm.AuditThread;
import com.nortel.appcore.app.drac.server.nrb.impl.mlbm.MultiLayerBWM;
import com.nortel.appcore.app.drac.server.nrb.impl.util.Task;
import com.nortel.appcore.app.drac.server.nrb.impl.util.TaskExecutorService;
import com.nortel.appcore.app.drac.server.nrb.impl.util.TimerNotificationListener;
import com.nortel.appcore.app.drac.server.nrb.impl.util.TimerService;

/**
 * The LpcpAdminManager is the top level wrapper on functionality that is
 * implemented in the Lpcp process. These methods talk with the lpcp and get it
 * to accomplish the given task.
 * 
 * @since Created on Dec 14, 2005
 * @author Viji Siddaiah
 */
public enum LpcpAdminManager implements TimerNotificationListener {
  
  INSTANCE;
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	// ===========================
	public static class ScheduleTask extends Task {

		private final LpcpAdminManager context;
		private long[][] numberOfService;
		private final Schedule schedule;
		private final UserDetails userDetails;

		public ScheduleTask(String taskName, String taskOwner, String taskId,
		    Schedule aSchedule, UserDetails theUserDetails, LpcpAdminManager parent) {
			super(taskName, taskOwner, taskId);
			userDetails = theUserDetails;
			schedule = aSchedule;
			context = parent;
		}

		@Override
		public void run() {
			try {
				context.createSchedule(userDetails, schedule, numberOfService, this);
			}
			catch (NrbException e) {
				e.printStackTrace();
			    getTaskInfo().setExceptionMessage(e.getMessage());
			}
		}

		/**
		 * @param numberOfService
		 *          The numberOfService to set.
		 */
		public void setNumberOfService(long[][] numberOfService) {
			this.numberOfService = numberOfService;
		}
	}

	private static final String SERVICE_CREATED_STRING = "Service Created";
	private static final long MILLISECONDS_PER_HOUR = 1000 * 3600;
	private static final long MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24;
	private static final long MILLISECONDS_PER_YEAR = MILLISECONDS_PER_DAY * 365;

	private final TaskExecutorService taskExecutor;
	private final ConcurrentHashMap<String, Task> taskList = new ConcurrentHashMap<String, Task>();
	private final TimerService nrbTimerService;
	private final AuditThread auditThread;
	private InternalLoginToken token;
	private LpcpInterface lpcpInterface;
	private String lpcpId;

	private LpcpAdminManager() {
		taskExecutor = new TaskExecutorService();
		nrbTimerService = new TimerService(this);
		auditThread = new AuditThread(this);
		auditThread.setDaemon(true);
		auditThread.start();
	}


	/*
	 * (non-Javadoc)
	 */
	public void activateService(UserDetails userDetails, String serviceId)
	    throws RemoteException {
		DracService aServiceIdType = MultiLayerBWM.INSTANCE.getService(
		    userDetails, serviceId);
		isAllowed(userDetails, new PolicyRequest(aServiceIdType,
		    PolicyRequest.CommandType.EDIT));

		if (State.isActivateable(aServiceIdType.getStatus())) {
			try {
				getLpcpInterface().activateService(token, aServiceIdType.getId());
				DbLog.INSTANCE.generateLog(
				    new LogRecord(userDetails.getUserID(), userDetails
				        .getLoginAddress(), aServiceIdType.getUserInfo()
				        .getBillingGroup(), aServiceIdType.getId(),
				        LogKeyEnum.KEY_SERVICE_ACTIVATED));
			}
			catch (Exception e) {
				DbLog.INSTANCE.generateLog(
				    new LogRecord(userDetails.getUserID(), userDetails
				        .getLoginAddress(), aServiceIdType.getUserInfo()
				        .getBillingGroup(), aServiceIdType.getId(),
				        LogKeyEnum.KEY_SERVICE_ACTIVATION_FAILED, new String[] { e
				            .getMessage() }));
				throw new NrbException(
				    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
				    new Object[] { e.getMessage() }, e);
			}
		}
		else {
			NrbException ne = new NrbException(
			    DracErrorConstants.MLBW_ERROR_2803_OPERATION_FAILED_DUE_TO_THE_CURRENT_STATE,
			    new Object[] { aServiceIdType.getId(),
			        aServiceIdType.getStatus().name() });
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        aServiceIdType.getUserInfo().getBillingGroup(), aServiceIdType
			            .getId(), LogKeyEnum.KEY_SERVICE_ACTIVATION_FAILED,
			        new String[] { ne.getMessage() }));
			throw ne;
		}
	}

	/*
	 * (non-Javadoc)
	 */
	public String asyncCreateSchedule(UserDetails usrDetails, Schedule aSchedule)
	    throws RemoteException {
		log.debug("asyncCreateSchedule invoked with schedule: "
		    + aSchedule.toDebugString());
		return createSchedule(usrDetails, aSchedule, true).getTaskId();
	}

	public List<AuditResult> auditModel(UserDetails userDetails) throws Exception {
		mustBeAdminUser(userDetails, "auditModel");
		return getLpcpInterface().auditModel(token);
	}

	public void cancelSchedule(UserDetails userDetails, String scheduleId)
	    throws NrbException {
		log.debug("Cancel schedule: " + scheduleId);
		DbUtilityCommonUtility dbUtility = DbUtilityCommonUtility.INSTANCE;
		Schedule aSchedule = null;
		try {
			aSchedule = dbUtility.querySchedule(scheduleId);
		}
		catch (Exception e) {
			log.error("Error looking up schedule " + scheduleId, e);
			throw new NrbException(DracErrorConstants.MLBW_ERROR_2010_NOT_EXIST,
			    new Object[] { scheduleId }, e);
		}

		if (aSchedule == null) {
			throw new NrbException(DracErrorConstants.MLBW_ERROR_2010_NOT_EXIST,
			    new Object[] { scheduleId });
		}

		try {
			// check user privilege here
			isAllowed(userDetails, new PolicyRequest(aSchedule,
			    PolicyRequest.CommandType.CANCEL));

			// schedule must be active
			if (!State.isCancelable(aSchedule.getStatus())) {
				log.error("Trying to cancel inactive schedule with status: {}", aSchedule.getStatus());
				throw new NrbException(
				    DracErrorConstants.MLBW_ERROR_2020_SCHED_NOT_ACTIVE,
				    new Object[] { aSchedule.getName() });
			}

			DracService[] serviceIdList = aSchedule.getServiceIdList();
			// Do not need the cancelService iteration to update the schedule status
			boolean updateScheduleStatus = false;
			for (DracService aService : serviceIdList) {
				cancelService(aService, TaskType.ACTIVITY.SCHEDULE_CANCEL,
				    updateScheduleStatus);
			}

			DbUtilityCommonUtility.INSTANCE.updateScheduleStatus(
			    aSchedule.getId());

			auditThread.interrupt();

			log.debug("calling delete schedule audit logging ");
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        aSchedule.getUserInfo().getBillingGroup(), aSchedule.getId(),
			        LogKeyEnum.KEY_SCHEDULE_CANCELED));
		}
		catch (NrbException nrbe) {
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        aSchedule.getUserInfo().getBillingGroup(), aSchedule.getId(),
			        LogKeyEnum.KEY_SCHEDULE_CANCELED_FAILED, new String[] { nrbe
			            .getMessage() }));
			throw nrbe;
		}
		catch (Exception e) {
			log.error(e.toString(), e);
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        aSchedule.getUserInfo().getBillingGroup(), aSchedule.getId(),
			        LogKeyEnum.KEY_SCHEDULE_CANCELED_FAILED, new String[] { e
			            .toString() }));
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2016_CANCEL_SCHED_FAIL,
			    new Object[] { e.toString() }, e);
		}
	}

	public void cancelService(UserDetails userDetails, String serviceId)
	    throws NrbException {
		log.debug("Canceling service: " + serviceId);
		DracService aService = null;
		Schedule aSched = null;
		try {
			aService = DbUtilityCommonUtility.INSTANCE
			    .queryServiceSummaryFromServiceId(serviceId);
			// Look up the schedule to find its user information
			aSched = DbUtilityCommonUtility.INSTANCE.queryScheduleFromServiceId(
			    serviceId);

		}
		catch (Exception e) {
			log.error("Error looking up schedule " + serviceId, e);
			throw new NrbException(DracErrorConstants.MLBW_ERROR_2010_NOT_EXIST,
			    new Object[] { serviceId }, e);
		}

		if (aService == null || aSched == null) {
			throw new NrbException(DracErrorConstants.MLBW_ERROR_2009_LOOKUP_SERVICE,
			    new Object[] { serviceId });
		}

		// Copy user info from schedule to service for policy checking
		UserGroupName billingGroup = aSched.getUserInfo().getBillingGroup();
		aService.setUserInfo(aSched.getUserInfo());

		try {
			// check user privilege here
			isAllowed(userDetails, new PolicyRequest(aService,
			    PolicyRequest.CommandType.CANCEL));
			cancelService(aService, TaskType.ACTIVITY.SERVICE_CANCEL, true);
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        billingGroup, serviceId, LogKeyEnum.KEY_SERVICE_CANCELED));
		}
		catch (NrbException nrbe) {
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        billingGroup, serviceId, LogKeyEnum.KEY_SERVICE_CANCELED_FAILED,
			        new String[] { nrbe.getMessage() }));
			throw nrbe;
		}
		catch (Exception e) {
			log.error("Unexpected error, failed to cancel the service", e);
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        billingGroup, serviceId, LogKeyEnum.KEY_SERVICE_CANCELED_FAILED,
			        new String[] { e.toString() }));
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2017_FAILED_CANCEL_SERVICE,
			    new Object[] { serviceId, e.toString() }, e);
		}
	}

	/*
	 * (non-Javadoc)
	 */
	public boolean cancelTask(UserDetails userDetails, String taskId)
	    throws RemoteException {
		boolean succeed = false;
		Task currentTask = taskList.get(taskId);
		if (currentTask != null) {
			succeed = taskExecutor.remove(currentTask);
			if (succeed) {
				taskList.remove(taskId);
			}
		}
		return succeed;
	}

	/*
	 * (non-Javadoc)
	 */
	public boolean clearTaskInfo(UserDetails userDetails, String taskId)
	    throws RemoteException {
		Task task = taskList.get(taskId);
		if (task != null) {
			TaskType.State currentState = task.getTaskInfo().getState();
			if (currentState.equals(TaskType.State.IN_PROGRESS)
			    || currentState.equals(TaskType.State.SUBMITTED)) {
				return false;
			}
			taskList.remove(taskId);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 */
	public void confirmSchedule(UserDetails userDetails, String scheduleId)
	    throws RemoteException {
		Schedule aSchedule = MultiLayerBWM.INSTANCE.getSchedule(userDetails,
		    scheduleId);
		if (!nrbTimerService.cancel(scheduleId)) {
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2803_OPERATION_FAILED_DUE_TO_THE_CURRENT_STATE,
			    new Object[] { aSchedule.getStatus().name() });
		}
		// TODO_VU to Check this
		isAllowed(userDetails, new PolicyRequest(aSchedule,
		    PolicyRequest.CommandType.EDIT));
		if (State.isConfirmable(aSchedule.getStatus())) {
			try {
				DracService[] serviceList = aSchedule.getServiceIdList();
				for (DracService serviceElement : serviceList) {
					confirmService(userDetails, serviceElement.getId());
				}
				DbUtilityCommonUtility.INSTANCE.updateScheduleStatus(
				    scheduleId,
				    State.evaluateState(ACTIVITY.SCHEDULE_CONFIRMATION,
				        aSchedule.getStatus()).name());
				DbLog.INSTANCE.generateLog(
				    new LogRecord(userDetails.getUserID(), userDetails
				        .getLoginAddress(), aSchedule.getUserInfo().getBillingGroup(),
				        aSchedule.getId(), LogKeyEnum.KEY_SCHEDULE_CONFIRMED));
			}
			catch (Exception e) {
				log.error("Error: ", e);
				DbLog.INSTANCE.generateLog(
				    new LogRecord(userDetails.getUserID(), userDetails
				        .getLoginAddress(), aSchedule.getUserInfo().getBillingGroup(),
				        aSchedule.getId(), LogKeyEnum.KEY_SCHEDULE_CONFIRMATION_FAILED,
				        new String[] { e.getMessage() }));
				throw new NrbException(
				    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
				    new Object[] { e.getMessage() }, e);
			}
		}
		else {
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2803_OPERATION_FAILED_DUE_TO_THE_CURRENT_STATE,
			    new Object[] { aSchedule.getStatus().name() });
		}
	}

	/*
	 * (non-Javadoc)
	 */
	public void confirmService(UserDetails userDetails, String serviceId)
	    throws RemoteException {
		DracService aServiceIdType = MultiLayerBWM.INSTANCE.getService(
		    userDetails, serviceId);
		isAllowed(userDetails, new PolicyRequest(aServiceIdType,
		    PolicyRequest.CommandType.EDIT));
		if (State.isConfirmable(aServiceIdType.getStatus())) {
			try {

				getLpcpInterface().confirmService(token, aServiceIdType.getId());
				@SuppressWarnings("unused")
				SERVICE serviceState = State.evaluateState(
				    ACTIVITY.SCHEDULE_CONFIRMATION, aServiceIdType.getActivationType(),
				    aServiceIdType.getStatus());
			}

			catch (Exception e) {
				log.error("confirmService failed for service " + serviceId, e);
				throw new NrbException(
				    DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON, new Object[] {
				        e.getMessage(), serviceId }, e);
			}
		}
		else {
			log.error("confirmService failed for service " + serviceId
			    + " service is int he wrong state " + aServiceIdType.getStatus());
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2803_OPERATION_FAILED_DUE_TO_THE_CURRENT_STATE,
			    new Object[] { aServiceIdType.getStatus().name() });
		}
	}

	public void correctModel(UserDetails userDetails) throws Exception {
		mustBeAdminUser(userDetails, "correctModel");
		getLpcpInterface().correctModel(token);
	}

	/** Used by the admin console for query only operations */
	public ScheduleResult createSchedule(UserDetails userDetails,
	    Map<SPF_KEYS, String> parms, boolean queryOnly) throws Exception {
		mustBeAdminUser(userDetails, "createSchedule");
		return getLpcpInterface().createSchedule(token, parms, queryOnly);
	}

	public String createService(UserDetails userDetails, String scheduleId,
	    DracService aService) throws NrbException {
		DbUtilityCommonUtility dbUtility = DbUtilityCommonUtility.INSTANCE;
		Schedule aSchedule = null;
		try {
			aSchedule = dbUtility.querySchedule(scheduleId);

		}
		catch (Exception e) {
			log.error("Error looking up schedule " + scheduleId, e);
			throw new NrbException(DracErrorConstants.MLBW_ERROR_2010_NOT_EXIST,
			    new Object[] { scheduleId }, e);
		}
		if (aSchedule == null) {
			throw new NrbException(DracErrorConstants.MLBW_ERROR_2010_NOT_EXIST,
			    new Object[] { scheduleId });
		}

		try {
			if (aSchedule.getEndTime() <= System.currentTimeMillis()) {
				throw new NrbException(
				    DracErrorConstants.MLBW_ERROR_2014_CREATE_SERVICE_EXPIRED,
				    new Object[] { scheduleId });
			}

			log.debug("Creating service with rate=" + aService.getPath().getRate()
			    + " startTime=" + aService.getStartTime() + " endTime="
			    + aService.getEndTime());

			aService.setId("SERVICE-" + System.currentTimeMillis());
			aService.setStatus(State.evaluateState(TaskType.ACTIVITY.SERVICE_CREATE,
			    aSchedule.getActivationType(), aSchedule.getStatus()));
			// If the pasedin PathType has at least one variable that is different
			// with the one in the parent schedule, we store the whole path with the
			// new service.
			@SuppressWarnings("unused")
			boolean differentPathInfo = false;
			if (aService.getPath() != null) {
				if (aService.getPath().getSourceEndPoint() == null) {
					aService.getPath().setSourceEndPoint(
					    aSchedule.getPath().getSourceEndPoint());
				}
				else {
					Map<String, String> attr = aService.getPath().getSourceEndPoint()
					    .getAttributes();
					if (attr != null) {
						if (!attr.equals(aSchedule.getPath().getSourceEndPoint()
						    .getAttributes())) {
							differentPathInfo = true;
						}
					}
				}
				if (aService.getPath().getTargetEndPoint() == null) {
					aService.getPath().setTargetEndPoint(
					    aSchedule.getPath().getTargetEndPoint());
				}
				else {
					Map<String, String> attr = aService.getPath().getTargetEndPoint()
					    .getAttributes();
					if (attr != null) {
						if (!attr.equals(aSchedule.getPath().getTargetEndPoint()
						    .getAttributes())) {
							differentPathInfo = true;
						}
					}
				}
				if (aService.getPath().getRate() == 0) {
					aService.getPath().setRate(aSchedule.getPath().getRate());
				}
				else if (aService.getPath().getRate() != aSchedule.getPath().getRate()) {
					differentPathInfo = true;
				}
				if (aService.getPath().getCost() == 0) {
					aService.getPath().setCost(aSchedule.getPath().getCost());
				}
				else if (aService.getPath().getCost() != aSchedule.getPath().getCost()) {
					differentPathInfo = true;
				}
				if (aService.getPath().getMetric() == 0) {
					aService.getPath().setMetric(aSchedule.getPath().getMetric());
				}
				else if (aService.getPath().getMetric() != aSchedule.getPath()
				    .getMetric()) {
					differentPathInfo = true;
				}
				if (aService.getPath().getSrlg().equals("unknown")) {
					aService.getPath().setSrlg(aSchedule.getPath().getSrlg());
				}
				else if (!aService.getPath().getSrlg()
				    .equals(aSchedule.getPath().getSrlg())) {
					differentPathInfo = true;
				}
				if (!aService.getPath().getSharedRiskServiceGroup().equals("")) {
					differentPathInfo = true;
				}
				if (aService.getPath().getHop() == 0) {
					aService.getPath().setHop(aSchedule.getPath().getHop());
				}
				else if (aService.getPath().getHop() != aSchedule.getPath().getHop()) {
					differentPathInfo = true;
				}
			}
			else {
				aService.setPath(aSchedule.getPath());
			}

			if (aService.getUserInfo() == null || aService.getUserInfo().isEmpty()) {
				aService.setUserInfo(aSchedule.getUserInfo());
			}

			String parentSrcTNA = aSchedule.getPath().getSourceEndPoint().getName();
			String parentDestTNA = aSchedule.getPath().getTargetEndPoint().getName();
			log.debug("Schedule TNAs: " + parentSrcTNA + " to " + parentDestTNA);
			log.debug("Service TNAs: "
			    + aService.getPath().getSourceEndPoint().getId() + "&"
			    + aService.getPath().getTargetEndPoint().getId());

			// check user priviledge here
			aService.setScheduleCreator(aSchedule.getUserInfo().getUserId());

			isAllowed(userDetails, new PolicyRequest(aService,
			    PolicyRequest.CommandType.WRITE));

			try {
				// we ignore the return code , if successful the id is copied into the
				// aService record.
				invokeCreateOrQueryCall(createServiceData(aService), aService, false);
			}
			catch (Exception e) {
				int errorCode = DracErrorConstants.MLBW_ERROR_2015_FAILED_CREATE_SERVICE;
				try {
					errorCode = Integer.parseInt(e.getMessage());
				}
				catch (NumberFormatException e1) {
					log.error("Error: ", e1);
				}
				throw new NrbException(errorCode, null, e);
			}

			// successful, update the database
			try {
				log.debug("scheduleId:" + scheduleId);
				log.debug("aService:" + aService);

				// Make sure that the status of the schedule is "active"
				// DbUtility.INSTANCE.updateScheduleStatus(scheduleId,
				// ScheduleXML.SCHEDULE_STATUS[ScheduleXML.ACTIVE]);
				// If necessary update the schedule time based on the new service time
				long scheduleStartTime = aSchedule.getStartTime();
				long scheduleEndTime = aSchedule.getEndTime();
				if (scheduleStartTime > aService.getStartTime()) {
					scheduleStartTime = aService.getStartTime();
				}
				if (scheduleEndTime < aService.getEndTime()) {
					scheduleEndTime = aService.getEndTime();
				}
				if (scheduleStartTime != aSchedule.getStartTime()
				    || scheduleEndTime != aSchedule.getEndTime()) {
					DbUtilityCommonUtility.INSTANCE.updateScheduleTime(scheduleId,
					    scheduleStartTime, scheduleEndTime);
				}
				DbLog.INSTANCE.generateLog(
				    new LogRecord(userDetails.getUserID(), userDetails
				        .getLoginAddress(), aSchedule.getUserInfo().getBillingGroup(),
				        scheduleId, LogKeyEnum.KEY_SCHEDULE_ADDSERVICE,
				        new String[] { aService.getId() }));
			}
			catch (Exception e) {
				log.error("Error: ", e);
				throw new NrbException(DracErrorConstants.MLBW_ERROR_2007_DB_INSERT,
				    new Object[] { e.toString() }, e);
			}
		}
		catch (NrbException e) {
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        aSchedule.getUserInfo().getBillingGroup(), scheduleId,
			        LogKeyEnum.KEY_SCHEDULE_ADDSERVICE_FAILED, new String[] { e
			            .getMessage() }));
			throw e;
		}
		catch (Exception e) {
			log.error("Failed to create Service, unexpected exception", e);
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        aSchedule.getUserInfo().getBillingGroup(), scheduleId,
			        LogKeyEnum.KEY_SCHEDULE_ADDSERVICE_FAILED, new String[] { e
			            .getMessage() }));

			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2015_FAILED_CREATE_SERVICE,
			    new Object[] { e.toString() }, e);
		}
		return aService.getId();
	}
	
	
	public Integer extendServiceTime(UserDetails userDetails, DracService aService,
			Integer minutesToExtendService) throws Exception {

		getLpcpInterface().extendServiceTime(token, aService, minutesToExtendService);
		return minutesToExtendService;
	}
	
	public void deleteNetworkElement(UserDetails userDetails,
	    NetworkElementHolder oldNe) throws Exception {
		mustBeAdminUser(userDetails, "deleteNetworkElement");
		getLpcpInterface().deleteNetworkElement(token, oldNe);
	}

	/**
	 * Old version of editFacility that only permits you to edit the MTU on a
	 * ethernet facility. Use editFacility for generic editing, except that
	 * editfacility is currentlly restriected to admin users only.
	 * 
	 * @param userDetails
	 * @param endPoint
	 * @throws NrbException
	 */
	public void editEndPoint(UserDetails userDetails, EndPointType endPoint)
	    throws Exception {
		// mustBeAdminUser(userDetails, "editFacility");
		getLpcpInterface().editFacility(token, endPoint.getNeid(),
		    endPoint.getAid(), null, null,
		    endPoint.getAttributes().get(FacilityConstants.MTU_ATTR), null, null,
		    null, null, null, null, null, null);
	}

	public void editFacility(UserDetails userDetails, String neid, String aid,
	    String tna, String faclabel, String mtu, String srlg, String grp,
	    String cost, String metric2, String sigType, String constraints,
	    String domainId, String siteId) throws Exception {
		mustBeAdminUser(userDetails, "editFacility");
		getLpcpInterface().editFacility(token, neid, aid, tna, faclabel, mtu, srlg,
		    grp, cost, metric2, sigType, constraints, domainId, siteId);
	}

	public GraphData getGraphData(UserDetails userDetails) throws Exception {
		mustBeAdminUser(userDetails, "getGraph");
		return getLpcpInterface().getGraphData(token);
	}

	public ServerInfoType getInfo(UserDetails userDetails) throws Exception {
		try {
			return getLpcpInterface().getInfo(token);
		}
		catch (Exception e) {
			log.error(e.toString(), e);
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2018_SERVER_INFO_FAILED,
			    new Object[] { e.toString() }, e);
		}
	}

	public List<BandwidthRecord> getInternalBandwithUsage(
	    UserDetails userDetails, long startTime, long endTime) throws Exception {
		mustBeAdminUser(userDetails, "getInternalBandwithUsage");
		return getLpcpInterface().getInternalBandwithUsage(token, startTime,
		    endTime);
	}

	public String getLpcpDiscoveryStatus(UserDetails userDetails)
	    throws Exception {
		mustBeAdminUser(userDetails, "getLpcpDiscoveryStatus");
		return getLpcpInterface().getLpcpDiscoveryStatus(token);
	}

	public LpcpStatus getLpcpStatus(UserDetails userDetails) throws Exception {
		mustBeAdminUser(userDetails, "getLpcpStatus");
		return getLpcpInterface().getLpcpStatus(token);
	}

	public String getPeerIPAddress(UserDetails userDetails) throws Exception {
		mustBeAdminUser(userDetails, "getPeerIPAddress");
		return getLpcpInterface().getPeerIPAddress(token);
	}

	/**
	 * Returns a comma delimited list of SRLG or "" in none apply.
	 */
	public String getSRLGListForService(UserDetails userDetails, String serviceId)
	    throws Exception {
		return getLpcpInterface().getSRLGListForServiceId(token, serviceId);
	}

	public List<StatusType> getStatusInfo(UserDetails userDetails, String taskId)
	    throws RemoteException {
		TaskType tf = getTaskInfo(userDetails, taskId);
		return tf == null ? null : tf.getStatusInfoList();
	}

	/*
	 * (non-Javadoc)
	 */
	public List<TaskType> getTaskInfo(UserDetails userDetails) throws Exception {
		List<TaskType> taskInfoList = null;
		if (taskList != null) {
			synchronized (taskList) {
				taskInfoList = new ArrayList<TaskType>(taskList.size());
				for (Object element2 : taskList.values()) {
					Task element = (Task) element2;
					taskInfoList.add(element.getTaskInfo());
				}
			}
		}
		return taskInfoList;
	}

	/*
	 * (non-Javadoc)
	 */
	public TaskType getTaskInfo(UserDetails userDetails, String taskId)
	    throws RemoteException {
		Task currentTask = taskList.get(taskId);
		return currentTask != null ? currentTask.getTaskInfo() : null;
	}

	public double getUtilization(UserDetails userDetails, String tnaName)
	    throws NrbException {
		try {
			return getLpcpInterface().getCurrentBandwidthUsage(token, tnaName);
		}
		catch (Exception e) {
			log.error("getCurrentBandwidthUsage failed for " + tnaName, e);
			throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
			    new Object[] { e.toString() }, e);
		}
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	public void handleTimerNotification(ACTIVITY taskName, String taskId) {
		if (ACTIVITY.TIMED_OUT == taskName) {
			try {
				Schedule aSchedule = DbUtilityCommonUtility.INSTANCE
				    .querySchedule(taskId);
				DracService[] serviceIdTypeList = aSchedule.getServiceIdList();

				DbLog.INSTANCE.generateLog(
				    new LogRecord(null, null,
				        aSchedule.getUserInfo().getBillingGroup(), aSchedule.getId(),
				        LogKeyEnum.KEY_SCHEDULE_CONFIRMATION_TIMEOUT));

				for (DracService service : serviceIdTypeList) {
					cancelService(service, ACTIVITY.TIMED_OUT, true);
				}

				DbUtilityCommonUtility.INSTANCE.updateScheduleStatus(
				    taskId,
				    State.evaluateState(ACTIVITY.TIMED_OUT, aSchedule.getStatus())
				        .name());
			}
			catch (Exception e) {
				log.error("Schedule status update failed", e);
			}
		}
	}

	/**
	 * Return void if alive toss and exception otherwise.
	 */
	public void isLpcpAlive() throws Exception {
		// if we can get a reference we are good, it will verify the interface is
		// alive...
		getLpcpInterface();
	}

	public boolean queryPath(UserDetails userDetails, DracService aService)
	    throws NrbException {
		
		try {
			// check user privilege here
			isAllowed(userDetails, new PolicyRequest(aService,
			    PolicyRequest.CommandType.WRITE));
			invokeCreateOrQueryCall(createServiceData(aService), aService, true);
			return true;
		}
		catch (NrbException e) {
			throw e;
		}
		catch (Exception e) {
			log.error("Failed to query path", e);
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2022_FAILED_QUERY_PATH,
			    new Object[] { e.getLocalizedMessage() }, e);
		}
	}

	/**
	 * Current implementation either returns 0 or number of requested minutes
	 * Calculating available time is skipped for now.
	 * @param userDetails
	 * @param serviceToExtend
	 * @param minutesToExtendService
	 * @return
	 * @throws NrbException
	 */	
	public int queryExtendServiceTime(UserDetails userDetails, DracService serviceToExtend,
	        int minutesToExtendService, Schedule schedule) throws NrbException {
		Integer timeAvailable = Integer.valueOf(0);
		DracService serviceToTestAvailability = getServiceFromService(serviceToExtend, schedule);
		serviceToTestAvailability.setStartTime(serviceToExtend.getEndTime() + 1 * 60 * 1000);
		serviceToTestAvailability.setEndTime(serviceToExtend.getEndTime() + minutesToExtendService * 60 * 1000);

		if (isAllowed(userDetails, new PolicyRequest(serviceToExtend, PolicyRequest.CommandType.WRITE))) {
			try {
			
				if (queryPath(userDetails, serviceToTestAvailability)) {
					timeAvailable = minutesToExtendService;
				}
				      
			} catch (Exception e) {				
				e.printStackTrace();// eat the exception because thrown
									// exception is part of business logic
			}
		}		
		
		return timeAvailable;
	}
	
	public void registerForLpcpEventNotifications(UserDetails userDetails,
	    LpcpEventCallback cb) throws Exception {
		mustBeAdminUser(userDetails, "registerForLpcpEventNotifications");
		lpcpInterface.registerForLpcpEventNotifications(token, cb);
	}

	/**
     */
	public void scheduleConfirmationTimeout(String scheduleId, SCHEDULE state,
	    long endTime) {
		if (state == SCHEDULE.CONFIRMATION_PENDING) {
			long creationTime = Long.valueOf(scheduleId.replaceAll("SCHEDULE-", ""))
			    .longValue();
			long confirmationTime = DbUtilityCommonUtility.INSTANCE
			    .queryConfirmationTimeout() * 1000 * 60;
			if (creationTime + confirmationTime > endTime) {
				confirmationTime = endTime - creationTime;
			}
			long currentTime = System.currentTimeMillis();
			if (currentTime > endTime) {
				// Time-out
				nrbTimerService.schedule(ACTIVITY.TIMED_OUT, scheduleId, -1L);
			}

			nrbTimerService
			    .schedule(ACTIVITY.TIMED_OUT, scheduleId, confirmationTime);
		}
	}

	/**
	 * Returns Start and End time for the service instances.
	 * 
	 * @param aSchedule
	 *          Schedule info to be created.
	 * @return Returns the Start and End time for the service instances.
	 * @throws NrbException
	 */
	protected long[][] generateServiceInfo(Schedule aSchedule)
	    throws NrbException {
		long startTime = aSchedule.getStartTime();
		long endTime = aSchedule.getEndTime();
		long duration = aSchedule.getDurationLong();
		long[][] numberOfService;
		long tempStartTime = startTime;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(startTime);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);

		if (!aSchedule.isRecurring() || aSchedule.getRecurrence() == null
		    || aSchedule.getRecurrence().getType() == null) {
			// Not a recurring schedule;
			numberOfService = new long[1][2];
			numberOfService[0][0] = startTime;
			numberOfService[0][1] = startTime + duration;
			return numberOfService;
		}

		RecurrenceFreq type = aSchedule.getRecurrence().getType();
		if (type.equals(RecurrenceFreq.FREQ_DAILY)) {

			int numberOfOccurrence = (int) ((endTime - startTime) / MILLISECONDS_PER_DAY);
			numberOfOccurrence++;
			log.debug("Creating Daily schedule: " + startTime + " endTime: "
			    + endTime + ":" + numberOfOccurrence);
			numberOfService = new long[numberOfOccurrence][2];
			for (int i = 0; i < numberOfOccurrence; i++) {
				numberOfService[i][0] = tempStartTime;
				numberOfService[i][1] = tempStartTime + duration;
				tempStartTime += MILLISECONDS_PER_DAY;
			}
		}
		else if (type.equals(RecurrenceFreq.FREQ_WEEKLY)) {
			log.debug("Creating Weekly schedule " + startTime + " endTime: "
			    + endTime);

			List<Long> tempService = new ArrayList<Long>();
			Calendar startDate = Calendar.getInstance();
			startDate.setTimeInMillis(startTime);

			Calendar endDate = Calendar.getInstance();
			endDate.setTimeInMillis(endTime);
			endDate.set(Calendar.HOUR_OF_DAY, 23);
			endDate.set(Calendar.MINUTE, 59);
			while (startDate.getTimeInMillis() <= endDate.getTimeInMillis()) {
				int tempWeekDay = startDate.get(Calendar.DAY_OF_WEEK);
				long tempTime = startDate.getTimeInMillis();

				if (isDayIncluded(aSchedule.getRecurrence().getWeekDay(), tempWeekDay)) {
					// for the last occurrence, making sure that it's still within the
					// end time.
					if (tempTime + duration <= endTime) {
						log.debug("...adding: " + new Date(tempTime).toString());
						tempService.add(new Long(tempTime));
					}
					else {
						log.debug("...skip: " + new Date(tempTime).toString());
					}
				}
				else {
					log.debug("...skip: " + new Date(tempTime).toString());
				}
				startDate.add(Calendar.DAY_OF_YEAR, 1);
			}
			if (tempService.size() == 0) {
				throw new NrbException(
				    DracErrorConstants.MLBW_ERROR_2012_SPECIFIED_TIME_INVALID, null);
			}

			int numberOfOccurrence = tempService.size();
			numberOfService = new long[numberOfOccurrence][2];
			for (int i = 0; i < numberOfOccurrence; i++) {
				long aLong = tempService.get(i).longValue();
				numberOfService[i][0] = aLong;
				numberOfService[i][1] = aLong + duration;
			}
		}
		else if (type.equals(RecurrenceFreq.FREQ_MONTHLY)) {
			List<Long> tempService = new ArrayList<Long>();
			Calendar startDate = Calendar.getInstance();
			startDate.setTimeInMillis(startTime);
			startDate.set(Calendar.DAY_OF_MONTH, aSchedule.getRecurrence().getDay());
			startDate.getTimeInMillis();

			Calendar endDate = Calendar.getInstance();
			endDate.setTimeInMillis(endTime);
			endDate.set(Calendar.HOUR_OF_DAY, 23);
			endDate.set(Calendar.MINUTE, 59);
			long currentTime = System.currentTimeMillis();
			while (startDate.getTimeInMillis() <= endDate.getTimeInMillis()) {
				long tempTime = startDate.getTimeInMillis();

				// for the first INSTANCE
				if (tempTime >= currentTime) {
					// for the last occurrence, making sure that it's still within the
					// end time.
					if (tempTime + duration <= endTime) {
						
						tempService.add(new Long(tempTime));
					}
					else {
						
					}
				}
				else {
					
				}
				startDate.add(Calendar.MONTH, 1);
			}
			if (tempService.size() == 0) {
				throw new NrbException(
				    DracErrorConstants.MLBW_ERROR_2012_SPECIFIED_TIME_INVALID, null);
			}

			int numberOfOccurrence = tempService.size();
			numberOfService = new long[numberOfOccurrence][2];
			for (int i = 0; i < numberOfOccurrence; i++) {
				long aLong = tempService.get(i).longValue();
				numberOfService[i][0] = aLong;
				numberOfService[i][1] = aLong + duration;
			}
		}
		else if (type.equals(RecurrenceFreq.FREQ_YEARLY)) {
			int numberOfOccurrence = (int) ((endTime - startTime) / MILLISECONDS_PER_YEAR);
			numberOfOccurrence++;
			numberOfService = new long[numberOfOccurrence][2];
			log.debug("Creating yearly schedule for every "
			    + aSchedule.getRecurrence().getDay() + " day of every "
			    + aSchedule.getRecurrence().getMonth() + " month");

			for (int i = 0; i < numberOfOccurrence; i++) {
				calendar.set(Calendar.MONTH, aSchedule.getRecurrence().getMonth());
				calendar.set(Calendar.DAY_OF_MONTH, aSchedule.getRecurrence().getDay());
				tempStartTime = calendar.getTimeInMillis();
				numberOfService[i][0] = tempStartTime;
				numberOfService[i][1] = tempStartTime + duration;
				log.debug("...adding "
				    + new Date(calendar.getTimeInMillis()).toString());

				calendar.add(Calendar.YEAR, 1);
			}
		}
		else {
			log.error("Unknown reoccurance type from " + aSchedule.toDebugString());
			throw new NrbException("Unknown reoccurance type from "
			    + aSchedule.toDebugString(), null);
		}

		return numberOfService;
	}

	private void cancelService(DracService aService,
	    TaskType.ACTIVITY activityName, boolean updateScheduleStatus)
	    throws Exception {

		if (State.isCancelable(aService.getStatus())) {
			State.SERVICE state = State.evaluateState(activityName,
			    aService.getStatus());

			for (int j = 0; j < aService.getCall().length; j++) {
				CallIdType aCall = aService.getCall()[j];

				try {
					getLpcpInterface().cancelService(token,
					    new String[] { aCall.getId() }, state);
				}
				catch (Exception e) {
					int code;
					if (TaskType.ACTIVITY.SERVICE_CANCEL == activityName) {
						log.error("Error cancelling service: " + aCall.getId(), e);
						code = DracErrorConstants.MLBW_ERROR_2017_FAILED_CANCEL_SERVICE;
					}
					else {
						log.error("Error cancelling schedule: " + aCall.getId(), e);
						code = DracErrorConstants.MLBW_ERROR_2016_CANCEL_SCHED_FAIL;
					}

					throw new NrbException(code, new Object[] { aCall.getId(),
					    e.toString() }, e);
				}
			}

			if (updateScheduleStatus) {
				DbUtilityCommonUtility.INSTANCE.updateScheduleStatus(
				    aService.getScheduleId());
			}
		}
		else if (TaskType.ACTIVITY.SERVICE_CANCEL == activityName) {
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2021_SERVICE_NOT_ACTIVE,
			    new Object[] { aService.getId() });
		}
	}

	private void copyCallDataIntoServiceRecord(String callId, DracService aService) {
		CallIdType aCall = new CallIdType(callId);
		aCall.setControllerId(lpcpId);
		aCall.setStatus(CALL.valueOf(aService.getStatus().name()));
		// Here's where the multiple-domain support for a service setup stops.
		// For now, it's hardcoded one-to-one.
		aService.setCall(new CallIdType[1]);
		aService.getCall()[0] = aCall;
	}

	/**
	 * Create a schedule, async or sync. Does some validation before passing it
	 * down...
	 * 
	 * @param aSchedule
	 * @throws NrbException
	 */
	private TaskType createSchedule(UserDetails userDetails, Schedule aSchedule,
	    boolean asyncFlag) throws NrbException {
		try {
			getLpcpInterface().isAlive(token);
		}
		catch (Exception e) {
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2003_CONTROLLER_UNAVAILBLE, null, e);
		}

		log.debug("createSchedule invoked with " + aSchedule.toDebugString());
		
		// TODO: SNMP Create schedule
		
		// check user privilege here
		try {
			isAllowed(userDetails, new PolicyRequest(aSchedule,
			    PolicyRequest.CommandType.WRITE));
		}
		catch (NrbException e) {
			log.debug("A schedule failed policy check: " + aSchedule.getId(), e);
			DbLog.INSTANCE.generateLog(
			    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
			        aSchedule.getUserInfo().getBillingGroup(), aSchedule.getId(),
			        LogKeyEnum.KEY_SCHEDULE_CREATE_FAILED, new String[] { e
			            .getMessage() }));
			throw e;
		}

		long startTime = aSchedule.getStartTime();
		long endTime = aSchedule.getEndTime();
		long duration = aSchedule.getDurationLong();

		log.debug("Request for new Schedule: " + aSchedule.getName() + " from"
		    + new Date(startTime).toString() + " to "
		    + new Date(endTime).toString() + " with duration=" + duration);
		log.debug("Complete schedule request is " + aSchedule.toDebugString());

		if (startTime == endTime) {
			throw new NrbException(
			    DracErrorConstants.MLBW_ERROR_2004_CANNOT_TIMES_EQUAL, null);
		}
		if (startTime > endTime) {
			throw new NrbException(DracErrorConstants.MLBW_ERROR_2005_INVALID_TIMES,
			    null);
		}

		if (aSchedule.getPath() != null) {
			String srcTNA = aSchedule.getPath().getSourceEndPoint().getAttributes()
			    .get(FacilityConstants.TNA_ATTR);
			String dstTNA = aSchedule.getPath().getTargetEndPoint().getAttributes()
			    .get(FacilityConstants.TNA_ATTR);
			if (srcTNA.equals(dstTNA)) {
				log.error("SourceTNA and dstTNA can't be the same");
				throw new NrbException(DracErrorConstants.MLBW_ERROR_2006_PORTS_EQUAL,
				    null);
			}
		}

		// Unique Id for the schedule
		aSchedule.setId("SCHEDULE-" + System.currentTimeMillis());
		ScheduleTask currentTask = new ScheduleTask("createSchedule", aSchedule
		    .getUserInfo().getUserId(), aSchedule.getId(), aSchedule, userDetails,
		    this);
		currentTask.getTaskInfo().setTaskName(aSchedule.getName());
		taskList.put(currentTask.getTaskInfo().getTaskId(), currentTask);

		// Generate start and end time for the service instances
		long[][] numberOfService = generateServiceInfo(aSchedule);

		currentTask.setNumberOfService(numberOfService);
		if (asyncFlag) {
			taskExecutor.execute(currentTask);
		}
		else {
			return createSchedule(userDetails, aSchedule, numberOfService,
			    currentTask);
		}
		return currentTask.getTaskInfo();
	}

	private DracService getServiceFromSchedule(Schedule aSchedule, long startTime, long endTime){
		String id = "SERVICE-" + System.currentTimeMillis();
		DracService aService = new DracService(aSchedule.getActivationType());
		aService.setId(id);
		aService.setStartTime(startTime);
		aService.setEndTime(endTime);
		aService.setPath(aSchedule.getPath());
		aService.setUserInfo(aSchedule.getUserInfo());

		aService.setScheduleId(aSchedule.getId());
		aService.setScheduleName(aSchedule.getName());
		aService.setRate(aSchedule.getRate());
		aService.setMbs(aSchedule.getRate());
		aService.setScheduleCreator(aSchedule.getUserInfo().getUserId());
		return aService;
	}
	
	private DracService getServiceFromService(DracService refService, Schedule schedule){
		String id = "SERVICE-" + System.currentTimeMillis();
		DracService aService = new DracService(refService.getActivationType());
		aService.setId(id);
		aService.setStartTime(refService.getStartTime());
		aService.setEndTime(refService.getEndTime());
		aService.setPath(schedule.getPath());
		
		aService.setUserInfo(refService.getUserInfo());

		aService.setScheduleId(refService.getScheduleId());
		aService.setScheduleName(refService.getScheduleName());
		aService.setRate(refService.getRate());
		aService.setMbs(refService.getMbs());
		aService.setScheduleCreator(refService.getScheduleCreator());
		
		aService.setCrossConnections(refService.getCrossConnections());
		aService.setDestTNA(schedule.getDestTNA());
		aService.setOffset(refService.getOffset());
		aService.setResultNum(refService.getResultNum());
		aService.setScheduleEndTime(refService.getScheduleEndTime());
		aService.setScheduleStartTime(refService.getScheduleStartTime());
		aService.setSrcTNA(schedule.getSrcTNA());
		aService.setUserInfo(refService.getUserInfo());
		
		return aService;
	}

	/**
	 * Called by createSchedule after performing some validation directly or
	 * indirectly if async, does the real work of creating the requested services
	 * via LPCP_PORT and writing the schedule to the database
	 */
	private TaskType createSchedule(UserDetails userDetails, Schedule aSchedule,
	    long[][] numberOfService, Task taskContext) throws NrbException {
	  
	  
	  log.info("Creating schedule called by user {} with schedule {}, no of servcices {} and task {}", new Object[]{userDetails.getUserID(), aSchedule, ArrayUtils.toString(numberOfService)});
	  
		int numberOfOccurrence = numberOfService.length;
		taskContext.getTaskInfo().setTotalNumberOfActivity(numberOfOccurrence);

		String srcTNA = aSchedule.getPath().getSourceEndPoint().getName();
		String dstTNA = aSchedule.getPath().getTargetEndPoint().getName();
		Map<String, DracService> serviceList = new HashMap<String, DracService>();
		List<CallIdType> callList = new ArrayList<CallIdType>();
		// String rate = Integer.toString(aSchedule.getPath().getRate());
		ClientMessageXml response = new ClientMessageXml();

		log.info("creating " + srcTNA + " to " + dstTNA + " for "
		    + numberOfOccurrence);

		for (int i = 0; i < numberOfOccurrence; i++) {
			DracService aService = getServiceFromSchedule(aSchedule, numberOfService[i][0], numberOfService[i][1]);
			String id = aService.getId();

			log.debug("userID in the schedule object : " + aSchedule.getUserInfo().getUserId());
			log.debug("TNAs: " + aService.getPath().getSourceEndPoint().getAttributes().get(FacilityConstants.TNA_ATTR)
			        + "&" + aService.getPath().getTargetEndPoint().getAttributes().get(FacilityConstants.TNA_ATTR));

			try {
				isAllowed(userDetails, new PolicyRequest(aService, PolicyRequest.CommandType.WRITE));
			}
			catch (NrbException e) {
				taskContext.getTaskInfo().addStatusInfo(
				    createStatusInfo(StatusInfoType.ERROR, aService.getStartTime(),
				        aService.getEndTime(), e.getMessage()));
				log.debug("userID in the schedule object : "
				    + aService.getUserInfo().getUserId());
				if (e.getCause() instanceof DracRemotePolicyException) {
				    DracRemotePolicyException dpe = (DracRemotePolicyException) e.getCause();
				    response.setException(dpe.getKeyAsString());
	                response.setExceptionArgs(dpe.getArgs());
				} else {
    				response.setException(e.getKeyAsString());
    				response.setExceptionArgs(e.getArgs());
				}

				log.debug("A service failed policy check: " + aService.getId());
				continue;
			}
			catch (Exception e) {
				taskContext.getTaskInfo().addStatusInfo(
				    createStatusInfo(StatusInfoType.ERROR, aService.getStartTime(),
				        aService.getEndTime(), "Policy checked failed"));
				log.debug("userID in the schedule object : "
				    + aService.getUserInfo().getUserId(), e);
				response.setException(e.getMessage());
				log.debug("A service failed policy check: " + aService.getId(), e);
				continue;
			}

			try {
				@SuppressWarnings("unused")
				ScheduleResult result = invokeCreateOrQueryCall(
				    createServiceData(aService), aService, false);

				serviceList.put(aService.getId(), aService);

				taskContext.getTaskInfo().addStatusInfo(
				    createStatusInfo(StatusInfoType.INFO, id, aService.getStartTime(),
				        aService.getEndTime(), SERVICE_CREATED_STRING));
			}
			catch (RoutingException re) {
				log.error("routing error creating service ", re);
				response.setException(String.valueOf(re.getErrorCode()));
				taskContext.getTaskInfo().addStatusInfo(
					    createStatusInfo(StatusInfoType.ERROR, aService.getStartTime(),
					        aService.getEndTime(), re.getMessage()));
				
			}
			catch (Exception e) {
				String exSTr = e.getMessage();
				e.printStackTrace();
				if(e instanceof DracException){
					DracException d = (DracException)e;
					exSTr = ""+ d.getKey().getKeyAsErrorCode();
				}
				log.error("error creating service ", e);
				response.setException(exSTr);
				
				ResourceKey resourceKey = new ResourceKey(response.getException());
				String[] args = response.getExceptionArgs();
				String exMsg = ExceptionFormatter.INSTANCE.formatMessage(
				    resourceKey, args);
				log.debug("... failed: " + exMsg);
				taskContext.getTaskInfo().addStatusInfo(
				    createStatusInfo(StatusInfoType.ERROR, aService.getStartTime(),
				        aService.getEndTime(), exMsg));
			}

			taskContext.getTaskInfo().markActivityCompletion();
		}

		

		List<DracService> actualServiceList = new ArrayList<DracService>();

		try {
			Map<String, Object> serviceFilter = new HashMap<String, Object>();
			serviceFilter.put(DbKeys.LightPathCols.LP_FILTER_SCHEDULEID_LIST,
			    Arrays.asList(new String[] { aSchedule.getId() }));
			List<ServiceXml> serviceResults = DbLightPath.INSTANCE.retrieve(
			    serviceFilter);

			if (serviceResults != null && serviceResults.size() > 0) {
				for (ServiceXml serviceXml : serviceResults) {
					String serviceId = serviceXml.getServiceId();
					if (serviceList.containsKey(serviceId)) {
						DracService aService = serviceList.get(serviceId);
						actualServiceList.add(aService);

						// The service list cache in the schedule record requires call data
						// too:
						copyCallDataIntoServiceRecord(serviceXml.getCallId(), aService);
						callList.add(aService.getCall()[0]);
					}
				}
			}
		}
		catch (Exception ex) {
			log.error(
			    "Error in retrieving and formatting service records prior to Schedule creation",
			    ex);
		}

		if (actualServiceList.size() != 0) {
			if (actualServiceList.size() != numberOfOccurrence) {
				log.debug("Part(s) of the shedule (got " + actualServiceList.size()
				    + ", expect " + numberOfOccurrence + ") is unsucessfull");
				taskContext.getTaskInfo().setResult(TaskType.Result.PARTIAL_SUCCESS);
			}
			else {
				taskContext.getTaskInfo().setResult(TaskType.Result.SUCCESS);
			}

			try {
				log.debug("Inserting new Schedule into database: " + serviceList.size());

				DbSchedule.INSTANCE.add(aSchedule);
				auditThread.interrupt();

				if (taskContext.getTaskInfo().getResult() == TaskType.Result.PARTIAL_SUCCESS) {
					DbLog.INSTANCE.generateLog(
					    new LogRecord(userDetails.getUserID(), userDetails
					        .getLoginAddress(),
					        aSchedule.getUserInfo().getBillingGroup(), aSchedule.getId(),
					        LogKeyEnum.KEY_SCHEDULE_CREATE_PARTIAL));
				}

				else if (taskContext.getTaskInfo().getResult() == TaskType.Result.SUCCESS) {
					DbLog.INSTANCE.generateLog(
					    new LogRecord(userDetails.getUserID(), userDetails
					        .getLoginAddress(),
					        aSchedule.getUserInfo().getBillingGroup(), aSchedule.getId(),
					        LogKeyEnum.KEY_SCHEDULE_CREATE));
				}

				scheduleConfirmationTimeout(aSchedule.getId(), aSchedule.getStatus(),
				    aSchedule.getEndTime());

				return taskContext.getTaskInfo();
			}
			catch (Exception e) {
				// Roll back if database update fail
				log.error("...failed ", e);
				rollback(taskContext, callList);
				taskContext.getTaskInfo().setResult(TaskType.Result.FAILED);
				taskContext.getTaskInfo().setExceptionMessage(e.getMessage());
				taskContext.getTaskInfo().setExceptionResourceKey(
				    "drac.nrb.error.create.schedule.db");

				DbLog.INSTANCE.generateLog(
				    new LogRecord(userDetails.getUserID(), userDetails
				        .getLoginAddress(), aSchedule.getUserInfo().getBillingGroup(),
				        aSchedule.getId(), LogKeyEnum.KEY_SCHEDULE_CREATE_FAILED,
				        new String[] { e.getMessage() }));

				throw new NrbException(
				    DracErrorConstants.MLBW_ERROR_2013_SCHED_CREATE_DB, null, e);
			}
		}

		// NO SERVICES CREATED. Full failure path...

		int errorCode = DracErrorConstants.MLBW_ERROR_2000;
		String formattedError = "UNFORMATTED ERROR";
		String[] args = {};
		try {
			errorCode = Integer.parseInt(response.getException());
			args = response.getExceptionArgs();
			formattedError = DracErrorConstants.getErrorMessage(
				    Locale.getDefault(), errorCode, args);
		}
		catch (NumberFormatException e) {
			// may be just an exception message, return unformatted...
			log.debug(
			    "Error parsing error code from response: " + response.getException(),
			    e);
			formattedError = response.getException();
		}
		
		DbLog.INSTANCE.generateLog(
		    new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
		        aSchedule.getUserInfo().getBillingGroup(), aSchedule.getId(),
		        LogKeyEnum.KEY_SCHEDULE_CREATE_FAILED,
		        new String[] { formattedError }));

		taskContext.getTaskInfo().setResult(TaskType.Result.FAILED);
		taskContext.getTaskInfo().setExceptionMessage(formattedError);
		taskContext.getTaskInfo()
		    .setExceptionResourceKey(String.valueOf(errorCode));
		throw new NrbException(errorCode, args);
	}

	/**
	 * Create service data
	 */
	private Map<SPF_KEYS, String> createServiceData(DracService aService) {
		Map<SPF_KEYS, String> data = new HashMap<SPF_KEYS, String>();
		data.put(SPF_KEYS.SPF_SERVICE_STATUS, aService.getStatus().name());
		data.put(SPF_KEYS.SPF_START_TIME, Long.toString(aService.getStartTime()));
		data.put(SPF_KEYS.SPF_END_TIME, Long.toString(aService.getEndTime()));

		data.put(SPF_KEYS.SPF_SRCTNA, aService.getPath().getSourceEndPoint()
		    .getName());
		data.put(SPF_KEYS.SPF_DSTTNA, aService.getPath().getTargetEndPoint()
		    .getName());

		data.put(SPF_KEYS.SPF_RATE, Integer.toString(aService.getRate()));
		data.put(SPF_KEYS.SPF_SRLG, aService.getPath().getSrlg());

		data.put(SPF_KEYS.SPF_VCATROUTING_OPTION, aService.getPath()
		    .getVcatRoutingOption() ? "true" : "false");
		data.put(SPF_KEYS.SPF_DIVERSE_EXCLUDE, aService.getPath()
		    .getSharedRiskServiceGroup());
		data.put(SPF_KEYS.SPF_COST, Integer.toString(aService.getPath().getCost()));
		data.put(SPF_KEYS.SPF_METRIC2,
		    Integer.toString(aService.getPath().getMetric()));
		data.put(SPF_KEYS.SPF_HOP, Integer.toString(aService.getPath().getHop()));
		data.put(SPF_KEYS.SPF_USER, aService.getUserInfo().getUserId());
		data.put(SPF_KEYS.SPF_EMAIL, aService.getUserInfo().getEmailAddress());
		data.put(SPF_KEYS.SPF_SERVICEID, aService.getId());

		data.put(
		    SPF_KEYS.SPF_OFFSET,
		    Integer.toString(MultiLayerBWM.INSTANCE.internalGetSystemMetric()
		        .getConfigureOffset()));
		data.put(
		    SPF_KEYS.SPF_SRCCHANNEL,
		    Integer.toString(aService.getPath().getSourceEndPoint()
		        .getChannelNumber()));
		data.put(
		    SPF_KEYS.SPF_DSTCHANNEL,
		    Integer.toString(aService.getPath().getTargetEndPoint()
		        .getChannelNumber()));
		data.put(SPF_KEYS.SPF_PROTECTION, aService.getPath().getProtectionType()
		    .name());
		data.put(SPF_KEYS.SPF_SRCVLAN, aService.getPath().getSrcVlanId());
		data.put(SPF_KEYS.SPF_DSTVLAN, aService.getPath().getDstVlanId());
		data.put(SPF_KEYS.SPF_ACTIVATION_TYPE, aService.getActivationType().name());
		data.put(SPF_KEYS.SPF_CONTROLLER_ID, lpcpId);
		data.put(SPF_KEYS.SPF_SCHEDULE_KEY, aService.getScheduleId());
		data.put(SPF_KEYS.SPF_SCHEDULE_NAME, aService.getScheduleName());
		data.put(SPF_KEYS.SPF_MBS, Integer.toString(aService.getMbs()));
		return data;
	}
	
	private StatusType createStatusInfo(StatusInfoType type, long startTime,
	    long endTime, String message) {
		StatusType info = new StatusType(type);
		info.setProperties(StatusType.START_TIME, Long.toString(startTime));
		info.setProperties(StatusType.END_TIME, Long.toString(endTime));
		info.setProperties(StatusType.MESSAGE, message);
		return info;
	}

	private StatusType createStatusInfo(StatusInfoType type, String taskId,
	    long startTime, long endTime, String message) {
		StatusType info = createStatusInfo(type, startTime, endTime, message);
		info.setSubTaskId(taskId);
		return info;
	}

	/**
	 * Get a RMI handle to the LPCP_PORT process. We need the local password to talk to
	 * it. Attempts to use a cached copy, if its invalid get a new one.
	 */
	private synchronized LpcpInterface getLpcpInterface() throws Exception {
		// Use our cached copy if its still valid.
		if (lpcpInterface != null) {
			try {
				if (!lpcpInterface.isAlive(token)) {
					Exception e = new Exception("Odd isAlive returned false");
					log.error("Error: ", e);
					throw e;
				}

				// still alive, go for it.
				return lpcpInterface;
			}
			catch (Exception e) {
				// no longer alive;
				lpcpInterface = null;
			}
		}

		
		// Obtain the password and lookup the interface in the registry.
		token = InternalLoginHelper.INSTANCE.getToken(InternalLoginTokenType.LPCP);
		lpcpInterface = (LpcpInterface) Naming
		    .lookup(RmiServerInfo.LPCP_RMI_BINDING_NAME);

		// verify it.
		if (!lpcpInterface.isAlive(token)) {
			Exception e = new Exception("Odd isAlive returned false");
			log.error("Error: ", e);
			throw e;
		}
		// if we got here, its fine.
		lpcpId = "localhost";
		return lpcpInterface;
	}

	private ScheduleResult invokeCreateOrQueryCall(Map<SPF_KEYS, String> data,
	    DracService aService, boolean queryOnly) throws Exception {
		ScheduleResult r = getLpcpInterface()
		    .createSchedule(token, data, queryOnly);
		log.debug("... done with callId = " + r.getCallId());
		copyCallDataIntoServiceRecord(r.getCallId(), aService);
		return r;
	}

	private boolean isAllowed(UserDetails userDetails, PolicyRequest request)
	    throws NrbException {
		try {
			return SecurityServer.INSTANCE.isAllowed(userDetails, request);
		}
		catch (DracException e) {
			log.debug("Policy check failed: ", e);
			throw new NrbException(e);
		}
		catch (Exception e) {
			log.debug("Policy check failed: ", e);
			throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
			    new Object[] { e.getMessage() }, e);
		}
	}

	private boolean isDayIncluded(int[] weekDay, int aDay) {
		for (int element : weekDay) {
			if (aDay == element) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verify that the user is an admin class user, operations from the
	 * adminConsole must be performed by an admin user. Faster than using
	 * isAllowed().
	 */
	private void mustBeAdminUser(UserDetails userDetails, String op)
	    throws Exception {
		if (!userDetails.getUserPolicyProfile().getUserGroupType()
		    .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)) {
			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL, new String[] {
			        op, "Must be admin class user" });
		}
	}

	private void rollback(Task taskContext, List<CallIdType> callList) {
		taskContext.getTaskInfo().setTotalNumberOfActivity(
		    taskContext.getTaskInfo().getTotalNumberOfActivity() + callList.size());
		for (int j = 0; j < callList.size(); j++) {
			CallIdType tempCall = callList.get(j);
			log.debug("Rollback canceling service " + tempCall.getId());
			try {
				getLpcpInterface().cancelService(token,
				    new String[] { tempCall.getId() }, null);
			}
			catch (Exception e) {
				log.error("rollback failed to cancel Service " + tempCall.getId(), e);
			}
			taskContext.getTaskInfo().markActivityCompletion();
		}
	}

}
