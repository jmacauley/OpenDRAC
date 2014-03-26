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

package com.nortel.appcore.app.drac.server.requesthandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.opendrac.server.nrb.reporting.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPasswordEvaluationException;
import com.nortel.appcore.app.drac.common.info.ServerInfo;
import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.LocalAccountCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.PasswordErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.GroupPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.OrganizationData;
import com.nortel.appcore.app.drac.common.security.policy.types.PersonalData;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourcePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.types.AbstractMessageType.MessageBox;
import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.ReceivedMessageType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.SentMessageType;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;
import com.nortel.appcore.app.drac.common.types.StatusType;
import com.nortel.appcore.app.drac.common.types.SysMetricType;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.UtilizationStructure;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;
import com.nortel.appcore.app.drac.server.nrb.NrbInterfaceUtils;
import com.nortel.appcore.app.drac.server.requesthandler.scheduler.SchedulerAlgorithm;

/**
 * Purpose: This is a facade to the NrbInterface. All web client requests go through
 * this class only.
 * 
 * @author Darryl Cheung
 */

public enum RequestHandler {
  INSTANCE;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private RemoteConnectionProxy proxy;

  private RequestHandler() {
    init();
  }

  /**
   * Activates a manual reservation (i.e. schedule). It is an error to activate
   * a schedule that is not in an activation_pending state
   */
  public void activateService(LoginToken token, String serviceId)
      throws Exception {
    try {
      log.info("Activate service called for service id {} by user {}", serviceId, token.getUser());
      getNrbInterface().activateService(token, serviceId);
      
    }
    catch (Exception e) {
      log.error("activateService failed", e);
      throw e;
    }
  }

  /**
   * Activates a manual reservation (i.e. schedule). It is an error to activate
   * a schedule that is not in an activation_pending state
   */

  public Map<String, Exception> activateServices(LoginToken token,
      String[] serviceIds) throws Exception {
    try {
      
      Map<String, Exception> rc = getNrbInterface().activateServices(token,
          serviceIds);
      
      return rc;
    }
    catch (Exception e) {
      log.error("activateServices failed", e);
      throw e;
    }
  }

  public void authenticate(ClientLoginType clientType, String user,
      char[] password, String ip, String ticket) throws Exception {
    
    log.debug("Authenticating user {}", user);
    NrbInterface authenticator = getNrbInterface();
    if (authenticator == null) {
      log.error("NrbInterface object is null!  Cannot authenticate!");
      throw new RequestHandlerException(
          DracErrorConstants.GENERAL_ERROR_INTERNAL, null);
    }

    authenticator.authenticate(clientType, user, password, ip, ticket);

    
  }

  public LoginToken authorize(ClientLoginType clientType, String user,
      char[] password, String ip, String ticket) throws Exception {
    
    log.debug("Authorizing user {}", user);
    NrbInterface authenticator = getNrbInterface();
    if (authenticator == null) {
      log.error("NrbInterface object is null!  Cannot authenticate!");
      throw new RequestHandlerException(
          DracErrorConstants.GENERAL_ERROR_INTERNAL, null);
    }

    LoginToken token = authenticator.authorize(clientType, user, password, ip,
        ticket);

    
    return token;
  }

  public boolean cancelProgress(LoginToken token, String tid) throws Exception {
    try {
      log.info("Cancel progress called by user {} for tid {}", token.getUser(), tid);
      boolean rc = getNrbInterface().cancelTask(token, tid);
      
      return rc;
    }
    catch (Exception e) {
      log.error("cancelProgress failed", e);
      throw e;
    }
  }

  /**
   * Cancel an existing Schedule. The implementation of this method is done in
   * the Multi-Layer Bandwidth Manager
   */

  public void cancelSchedule(LoginToken token, String scheduleId)
      throws Exception {
    try {
      log.info("Cancel schedule called by user {} for scheduleId {}", token.getUser(), scheduleId);
      getNrbInterface().cancelSchedule(token, scheduleId);
      
    }
    catch (Exception e) {
      log.error("cancelSchedule failed", e);
      throw e;
    }
  }

  public Map<String, Exception> cancelSchedules(LoginToken token,
      String[] scheduleIds) throws Exception {
    try {
      log.info("Cancel schedule called by user {} for scheduleIds {}", token.getUser(), StringUtils.arrayToCommaDelimitedString(scheduleIds));
      Map<String, Exception> rc = getNrbInterface().cancelSchedules(token,
          scheduleIds);
      
      return rc;
    }
    catch (Exception e) {
      log.error("cancelSchedules failed", e);
      throw e;
    }
  }

  /**
   * Cancel an existing Service The implementation of this method is done in the
   * Multi-Layer Bandwidth Manager
   * 
   * @param serviceId
   *          Service id
   */
  public void cancelService(LoginToken token, String serviceId)
      throws Exception {
    try {
      log.info("Cancel service called by user {} for serviceId {}", token.getUser(), serviceId);
      getNrbInterface().cancelService(token, serviceId);
      
    }
    catch (Exception e) {
      log.error("cancelService failed", e);
      throw e;
    }
  }

  /**
   * Cancel an existing Service The implementation of this method is done in the
   * Multi-Layer Bandwidth Manager
   */

  public Map<String, Exception> cancelServices(LoginToken token,
      String[] serviceIds) throws Exception {
    try {
      log.info("Cancel services called by user {} for scheduleIds {}", token.getUser(), StringUtils.arrayToCommaDelimitedString(serviceIds));
      Map<String, Exception> rc = getNrbInterface().cancelServices(token,
          serviceIds);
      
      return rc;
    }
    catch (Exception e) {
      log.error("cancelServices failed", e);
      throw e;
    }
  }

  public void changePassword(LoginToken token, String userId, String oldpw,
      String newpw, String newpw2, ClientLoginType loginType, String clientIp, String sessionId)
      throws Exception {
    
    log.info("Changing password for userId {}", userId);
    // The most basic of checks done on client side (to save on rmi args)
    if (!newpw.equals(newpw2)) {
      throw new DracPasswordEvaluationException(
          new PasswordErrorCode[] { PasswordErrorCode.ERROR_PW_NOT_MATCH });
    }

    getNrbInterface().changePassword(token, userId, oldpw, newpw, loginType,
        clientIp,sessionId);
  }

  public boolean clearProgress(LoginToken token, String tid) throws Exception {
    try {
      
      log.info("Clear progress called by user {} for tid {}", token.getUser(), tid);
      boolean rc = getNrbInterface().clearTaskInfo(token, tid);
      
      return rc;
    }
    catch (Exception e) {
      log.error("clearProgress failed", e);
      throw e;
    }
  }

  /**
   * Confirms a pre-reservation (i.e. schedule). It is an error to confirm a
   * schedule that is not in a confirmation_pending state
   */

  public void confirmSchedule(LoginToken token, String scheduleId)
      throws Exception {
    try {
      log.info("Confirm schedule called by user {} for scheduleId {}", token.getUser(), scheduleId);
      getNrbInterface().confirmSchedule(token, scheduleId);
      
    }
    catch (Exception e) {
      log.error("confirmSchedule failed", e);
      throw e;
    }
  }

  /**
   * Confirms a pre-reservation (i.e. schedule). It is an error to confirm a
   * schedule that is not in a confirmation_pending state
   */

  public Map<String, Exception> confirmSchedules(LoginToken token,
      String[] scheduleIds) throws Exception {
    try {
      log.info("Confirm schedules called by user {} for scheduleIds {}", token.getUser(), StringUtils.arrayToCommaDelimitedString(scheduleIds));
      Map<String, Exception> rc = getNrbInterface().confirmSchedules(token,
          scheduleIds);
      
      return rc;
    }
    catch (Exception e) {
      log.error("confirmSchedules failed", e);
      throw e;
    }
  }

  public void createResourceGroupProfile(LoginToken token,
      ResourceGroupProfile aResourceGroupProfile) throws Exception {
    try {
      log.info("Creating resource profile by user {} with profile {}", token.getUser(), aResourceGroupProfile.toString());
      getNrbInterface()
          .createResourceGroupProfile(token, aResourceGroupProfile);
      
    }
    catch (Exception e) {
      log.error("createResourceGroupProfile failed", e);
      throw e;
    }
  }

  /**
   * Creating a new Schedule. The implementation of this method is done in the
   * Multi-Layer Bandwidth Manager. This method returns asynchronously. The
   * schedule creation is done in a background task on the server and can be
   * monitored by calling getProgress(String scheduleId)
   * 
   * @param aSchedule
   *          the object that contains new Schedule's information
   * @return String the scheduleID
   */
  public String createScheduleAsync(LoginToken token, Schedule aSchedule)
      throws Exception {
    log.info("Creating schedule called by user {} with schedule {}", token.getUser(), aSchedule.toString());
    checkScheduleTna(token, aSchedule);
    try {
      String retSchID = null;
      retSchID = getNrbInterface().asyncCreateSchedule(token, aSchedule);
      
      return retSchID;
    }
    catch (Exception e) {
      log.error("createScheduleAsync failed", e);
      throw e;
    }
  }

  /**
   * Creating a new Service. The implementation of this method is done in the
   * Multi-Layer Bandwidth Manager
   */
  public String createService(LoginToken token, String scheduleId,
      DracService aService) throws Exception {
    /*
     * we need to check service later on the server side since the aService does
     * not contain the tnas.
     */
    log.info("Creating service called by user {} with schedule {}", token.getUser(), aService.toString());
    checkServiceTna(token, aService);
    try {
      String rc = getNrbInterface().createService(token, scheduleId, aService);
      
      return rc;
    }
    catch (Exception e) {
      log.error("createService failed", e);
      throw e;
    }
  }

  public void createUserGroupProfile(LoginToken token,
      UserGroupProfile aUserGroupProfile) throws Exception {
    
    log.info("Creating user group profile by user {} with profile {}", token.getUser(), aUserGroupProfile.toString());
    isAllowed(token, new PolicyRequest(aUserGroupProfile,
        PolicyRequest.CommandType.CREATE));
    try {
      getNrbInterface().createUserGroupProfile(token, aUserGroupProfile);
      
    }
    catch (Exception e) {
      log.error("createUserGroupProfile failed", e);
      throw e;
    }
  }

  public void createUserProfile(LoginToken token, String userID,
      AbstractCredential credential, AccountStatus status) throws Exception {
    try {
      log.info("Creating user profile by user {} for user {}", token.getUser(), userID);
      getNrbInterface().createUserProfile(token, userID, credential, status);
      
    }
    catch (Exception e) {
      log.error("createUserProfile failed", e);
      throw e;
    }
  }

  public void deleteResourceGroup(LoginToken token, String groupName)
      throws Exception {
    try {
      log.info("Deleting resource group by user {} for group {}", token.getUser(), groupName);
      ResourceGroupProfile resGrp = this.getResourceGroupProfile(token,
          groupName);
      if (resGrp != null) {
        isAllowed(token, new PolicyRequest(resGrp,
            PolicyRequest.CommandType.DELETE));
        getNrbInterface().deleteResourceGroupProfile(token, groupName);
      }
      
    }
    catch (RequestHandlerException e) {
      log.error(
          "deleteResourceGroup failed for user: " + getUserDetails(token), e);
      throw e;
    }
    catch (Exception re) {
      log.error("deleteResourceGroup failed", re);
      throw new RequestHandlerException(re);
    }
  }

  public void deleteUser(LoginToken token, String userId) throws Exception {
    try {
      log.info("Deleting user called by user {} for user {}", token.getUser(), userId);
      UserProfile user = this.getUserProfile(token, userId);
      if (user != null) {
        isAllowed(token, new PolicyRequest(user,
            PolicyRequest.CommandType.DELETE));
        getNrbInterface().deleteUserProfile(token, userId);
      }
      
    }
    catch (RequestHandlerException e) {
      log.error("deleteUser failed for user: " + getUserDetails(token), e);
      throw e;
    }
    catch (Exception re) {
      log.error("deleteUser failed", re);
      throw new RequestHandlerException(re);
    }
  }

  public void deleteUserGroup(LoginToken token, UserGroupName groupName)
      throws Exception {
    try {
      log.info("Deleting user group called by user {} for user group {}", token.getUser(), groupName.toString());
      UserGroupProfile group = this.getUserGroupProfile(token, groupName);
      if (group != null) {
        isAllowed(token, new PolicyRequest(group,
            PolicyRequest.CommandType.DELETE));
        getNrbInterface().deleteUserGroupProfile(token, groupName);
      }
      
    }
    catch (RequestHandlerException e) {
      log.error("deleteUser failed for user: " + getUserDetails(token), e);
      throw e;
    }
    catch (Exception re) {
      log.error("deleteUserGroup failed", re);
      throw new RequestHandlerException(re);
    }
  }

  /**
   * Edit the an existing endpoint. The implementation of this method is done in
   * Administration Manager
   * 
   * @deprecated
   */
  @Deprecated
  public void editEndPoint(LoginToken token, EndPointType endPoint)
      throws Exception {
    log.info("Edit endpoint called by user {} for endpoint {}", token.getUser(), endPoint);
    isAllowed(token, new PolicyRequest(endPoint,
        PolicyRequest.CommandType.WRITE));
    try {
      getNrbInterface().editEndPoint(token, endPoint);
      
    }
    catch (Exception e) {
      log.error("editEndPoint failed", e);
      throw e;
    }
  }

  public void editGlobalPolicy(LoginToken token, GlobalPolicy policy)
      throws Exception {
    log.info("Edit global policy called by user {} with policy {}", token.getUser(), policy);
    isAllowed(token, new PolicyRequest(policy, PolicyRequest.CommandType.EDIT));
    try {
      getNrbInterface().setDefaultGlobalPolicy(token, policy);
      
    }
    catch (Exception e) {
      log.error("editGlobalPolicy failed", e);
      throw e;
    }
  }

  public void setLockedIPs(LoginToken token, GlobalPolicy policy, List<String> stillLockedIPs )throws Exception {
    log.info("Locking IP's called by user {} with IP's {}", token.getUser(), stillLockedIPs);
    isAllowed(token, new PolicyRequest(policy, PolicyRequest.CommandType.EDIT));
    try {
    	getNrbInterface().setLockedIPs(token, stillLockedIPs);
    }
    catch (Exception e) {
      log.error("editGlobalPolicy failed", e);
      throw e;
    }	  
  }
  
  public EndPointType findEndpointById(LoginToken token,
      String endpointResourceId) throws Exception {
    log.info("Finding endpoint by id called by user {} for endpoint id {}", token.getUser(), endpointResourceId);
    Map<String, String> facilityFilter = new HashMap<String, String>();
    // recently aligned format of DbNetworkElementFacility pk, EndPointType id,
    // and Resource id
    facilityFilter
        .put(DbKeys.NetworkElementFacilityCols.PK, endpointResourceId);
    return retrieveEndpoint(token, facilityFilter);
  }

  public EndPointType findEndpointByTna(LoginToken token, String tna)
      throws Exception {
    log.info("Finding endpoint by tna called by user {} for endpoint tna {}", token.getUser(), tna);
    Map<String, String> facilityFilter = new HashMap<String, String>();
    facilityFilter.put(DbKeys.NetworkElementFacilityCols.TNA, tna);
    return retrieveEndpoint(token, facilityFilter);
  }

  public void forceRedundancySwitch() throws Exception {
    proxy.forceSwitch();
  }

  /**
   * Find schedules that are still active (endTime >= current Time)
   */

  public List<Schedule> getActiveSchedules(LoginToken token) throws Exception {

    try {
      List<Schedule> rc = getNrbInterface().getActiveSchedules(token);
      log.info("Looking for all active schedules called by user {} returns {}", token.getUser(), rc);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getActiveSchedules failed", e);
      throw e;
    }
  }

  /**
   * Querying all the services within a time span. The implementation of this
   * method is done in the QueryUtility.
   */
  public List<DracService> getAllServices(LoginToken token, long startTime,
      long endTime, List<UserGroupName> groups) throws Exception {
    try {
      
      List<DracService> rc = getNrbInterface().queryServices(token, startTime,
          endTime, groups);
      log.info("Looking for all services called by user {} returns {}", token.getUser(), rc);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getAllServices failed", e);
      throw e;
    }
  }

  public List<DracService> getAvailableTimes(LoginToken token, String srcTna,
      String destTna, int minDur, int rate) throws Exception {
    try {
      log.info(
          "Looking for available times called by user {} for source tna {}, destination tna {}, minimal duration {} and rate {}",
          new Object[] { token.getUser(), srcTna, destTna, minDur, rate });
      List<DracService> services = getNrbInterface().getServiceUsageForTNA(
          token, srcTna);
      services.addAll(getNrbInterface().getServiceUsageForTNA(token, destTna));

      EndPointType srcEp = findEndpointByTna(token, srcTna);
      EndPointType destEp = findEndpointByTna(token, destTna);
      List<DracService> rc = new SchedulerAlgorithm().run(services, srcEp,
          destEp, minDur, rate);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getAvailableTimes failed", e);
      throw e;
    }
  }

  public int getConfirmationTimeout(LoginToken token) throws Exception {
    try {
      int rc = getNrbInterface().getConfirmationTimeout(token);
      log.info("Get confirmation timeout called by user {} returns {}", token.getUser(), rc);
      return rc;
    }
    catch (Exception e) {
      log.error("getConfirmationTimeout failed", e);
      throw e;
    }
  }

  public List<String> getEndpointNames(LoginToken token) throws Exception {
    return getEndpointNamesByLayer(token, Layer.LAYER_ALL);
  }

  public List<String> getEndpointNamesByLayer(LoginToken token, Layer layerEnum)
      throws Exception {
    List<String> result = new ArrayList<String>();

    // New server-side-policy-checked retrieval of user endpoints
    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementFacilityCols.LAYER, layerEnum.toString());
    List<EndpointResourceUiType> query = getNrbInterface()
        .getUserEndpointsUIInfo(token, filter);
    for (EndpointResourceUiType epInfo : query) {
      result.add(epInfo.getTna());
    }
    log.info("Get endpoint names by layer called by user {} with layer {} returns {}", new Object[] { token.getUser(),
        layerEnum.name(), result });
    return result;
  }

  public Resource getEndpointResource(LoginToken token, String resourceID)
      throws Exception {
    log.info("Get endpoint resource called by user {} with resource id {}", token.getUser(), resourceID);
    try {
      Resource anEndpointResource = getNrbInterface().getEndpointResource(
          token, resourceID);
      if (anEndpointResource != null) {
        log.debug("Found endpoint resource " + anEndpointResource);
        return anEndpointResource;
      }
      
      return null;
    }
    catch (Exception e) {
      log.error("getEndpointResource failed", e);
      throw e;
    }
  }

  /**
   * Query the all endpoints that the specified user can access, filtered by
   * user group. The callers require EndPointType as return value; these results
   * are used as beans in struts forms.
   */

  public List<EndPointType> getEndpoints(LoginToken token, String layer,
      List<UserGroupName> userGroupFilter) throws Exception {
    
    log.info("Get endpoints called by user {} with layer {} and user group filter {}", new Object[] { token.getUser(),
        layer, userGroupFilter });
    
    Map<String, String> facilityFilter = new HashMap<String, String>();
    facilityFilter.put(DbKeys.NetworkElementFacilityCols.LAYER,
        Layer.toEnum(layer).toString());
    return getNrbInterface().getUserEndpoints(token, userGroupFilter,
        facilityFilter);
  }

  public List<EndpointResourceUiType> getEndpointsForSiteId(LoginToken token,
      String siteId) throws Exception {
    
    log.info("Get endpoint for site called by user {} with site id {}", new Object[] { token.getUser(),
        siteId});
    
    Map<String, String> filter = new HashMap<String, String>();
    filter.put(DbKeys.NetworkElementFacilityCols.SITE, siteId);
    return getNrbInterface().getUserEndpointsUIInfo(token, filter);
  }

  public List<EndpointResourceUiType> getEndpointsForWavelength(
      LoginToken token, String wavelength) throws Exception {
    List<EndpointResourceUiType> result = new ArrayList<EndpointResourceUiType>();
    log.info("Get endpoint for wavelength called by user {} with wavelength {}", new Object[] { token.getUser(),
        wavelength});
    // The server side call doesn't support filtering directly on wavelength.
    // Filter instead on Layer0 and iterate the results.
    Map<String, String> filter = new HashMap<String, String>();
    filter
        .put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.LAYER0.toString());
    List<EndpointResourceUiType> eps = getNrbInterface()
        .getUserEndpointsUIInfo(token, filter);

    for (EndpointResourceUiType epInfo : eps) {
      // Wavelength check:
      if (epInfo.getWavelength().equals(wavelength)) {
        result.add(epInfo);
      }
    }
    return result;
  }

  public GlobalPolicy getGlobalPolicy(LoginToken token) throws Exception {
    log.info("Retrieving global policy by user {}", token.getUser());

    GlobalPolicy gp;
    try {
      gp = getNrbInterface().getGlobalPolicy(token);
    }
    catch (Exception e) {
      log.error("getGlobalPolicy failed", e);
      throw e;
    }
    isAllowed(token, new PolicyRequest(gp, PolicyRequest.CommandType.READ));
    
    return gp;

  }

  public List<LogRecord> getLogs(LoginToken token, long startTime,
      long endTime, Map<String, String> filter) throws Exception {
    log.info("Retrieving logs called by user {} with start time {}, endTime {} and filter {}", new Object[] { token.getUser(),
        startTime, endTime, filter});
    List<LogRecord> result = getNrbInterface().getLogs(token, startTime,
        endTime, filter);
    
    return result;
  }

  public List<Report> getAggregatedReportForGui(int reportId,
      String titleLeader, Date startDate, Date endDate,
      Map<String, String> filter, LoginToken token) throws Exception {

    return getNrbInterface().getAggregatedReportForGui(reportId, titleLeader,
        startDate, endDate, filter, token);
  }

  public Report getQualitativeReportByPort(Date startDate, Date endDate,
      LoginToken token) throws Exception {
    return getNrbInterface().getQualitativeReportByPort(startDate, endDate, token);
  }

  public Report getQualitativeReport(Date startDate, Date endDate,
      LoginToken token) throws Exception {
    return getNrbInterface().getQualitativeReport(startDate, endDate, token);
  }

  public Report getScheduledServicesReport(Date startDate, Date endDate,
      LoginToken token) throws Exception {
    return getNrbInterface().getScheduledServicesReport(startDate, endDate,
        token);
  }

  public Report getActualschedulesReport(Date startDate, Date endDate,
      LoginToken token) throws Exception {
    return getNrbInterface()
        .getActualschedulesReport(startDate, endDate, token);
  }

  public File getAggregatedReportAsFile(int reportId, String titleLeader,
      Date startDate, Date endDate, Map<String, String> filter, LoginToken token)
      throws Exception {
    Report report = getNrbInterface().getAggregatedReport(reportId,
        titleLeader, startDate, endDate, filter, token);
    return report.getAsFile();
  }

  public Report getAggregatedReport(int reportId, String titleLeader,
      Date startDate, Date endDate, Map<String, String> filter, LoginToken token)
      throws Exception {
    Report report = getNrbInterface().getAggregatedReport(reportId,
        titleLeader, startDate, endDate, filter, token);
    return report;
  }

  public Report getAggregatedReportFull(int reportId, String titleLeader,
      Date startDate, Date endDate, Map<String, String> filter, LoginToken token)
      throws Exception {
    Report report = getNrbInterface().getAggregatedReportFull(reportId,
        titleLeader, startDate, endDate, filter, token);
    return report;
  }

  /**
   * Get the progress of all past and current schedule creations
   */

  public List<TaskType> getProgress(LoginToken token) throws Exception {
    try {
      log.debug("Retrieving progress called by user {}", token.getUser());
      List<TaskType> rc = getNrbInterface().getTaskInfo(token);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getProgress failed", e);
      throw e;
    }
  }

  /**
   * Get the create schedule progress of a particular schedule
   */
  public TaskType getProgress(LoginToken token, String scheduleId)
      throws Exception {
    try {
      log.info("Retrieving progress called by user {} for schedule id {}", token.getUser(), scheduleId);
      TaskType rc = getNrbInterface().getTaskInfo(token, scheduleId);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getProgress failed", e);
      throw e;
    }
  }

  public ServerInfo[] getRedundancyServerInfo() {
    
    ServerInfo[] info = new ServerInfo[2];
    info[0] = proxy.getPrimaryControllerInfo();
    info[1] = proxy.getSecondaryControllerInfo();
    
    return info;
  }

  public List<String> getResourceGroupNameLineage(LoginToken token,
      ResourceGroupProfile rgProfile) throws Exception {
    log.info("Retrieving resource group name lineage called by user {} for resource group profile {}", token.getUser(), rgProfile);
    return getNrbInterface().getResourceGroupNameLineage(token, rgProfile);
  }

  public ResourceGroupProfile getResourceGroupProfile(LoginToken token,
      String resourceGroupName) throws Exception {
    
    log.info("Retrieving resource group profile by user {} for resource group  {}", token.getUser(), resourceGroupName);
    ResourceGroupProfile aResourceGroupProfile = null;
    try {
      aResourceGroupProfile = getNrbInterface().getResourceGroupProfile(token,
          resourceGroupName);
    }
    catch (Exception e) {
      log.error("getResourceGroupProfile failed", e);
      throw e;
    }
    // Check for resource access privileges now done server-side:
    // if (aResourceGroupProfile != null)
    // {
    // isAllowed(userDetails, new PolicyRequest(aResourceGroupProfile,
    // PolicyRequest.CommandType.READ));
    // }
    
    return aResourceGroupProfile;
  }

  public List<String> getResourceGroupProfileNames(LoginToken token)
      throws Exception {
    
    log.info("Retrieving resource group profile name by user {}", token.getUser());
    
    List<ResourceGroupProfile> groupList = this.getResourceGroups(token);
    List<String> result = new ArrayList<String>(groupList.size());
    for (ResourceGroupProfile group : groupList) {
      result.add(group.getName());
    }
    
    return result;
  }

  // isAllowed moved server-side
  public List<ResourceGroupProfile> getResourceGroups(LoginToken token)
      throws Exception {
    log.debug("Retrieving resource groups by user {}", token.getUser());
    return getNrbInterface().getResourceGroupProfileList(token);
  }

  /**
   * Given a service ID, get schedule details
   */

  public Schedule getScheduleForService(LoginToken token, String serviceId)
      throws Exception {
    log.info("Retrieving schedule for service by user {} with service id {}", token.getUser(), serviceId);
    try {
      Schedule rc = getNrbInterface().queryScheduleByService(token, serviceId);
      
      return rc;

    }
    catch (Exception e) {
      log.error("getScheduleForService failed", e);
      throw e;
    }
  }

  /**
   * Query the server's information. The implementation of this method is done
   * in the Administration Manager
   */

  public ServerInfoType getServerInfo(LoginToken token) throws Exception {
    try {
      log.info("Retrieving server info by user {}", token.getUser());
      ServerInfoType rc = getNrbInterface().getInfo(token);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getServerInfo failed", e);
      throw e;
    }
  }

  public DracService getService(LoginToken token, String serviceId)
      throws Exception {
    log.info("Retrieving service by user {} for service id {}", token.getUser(), serviceId);
    Schedule schedule = null;
    try {
      schedule = getNrbInterface().queryScheduleByService(token, serviceId);
    }
    catch (Exception e) {
      log.error("getService failed", e);
      throw e;
    }

    DracService service = null;
    if (schedule != null) {
      DracService[] serviceIdList = schedule.getServiceIdList();
      for (DracService element : serviceIdList) {
        service = element;
        if (service.getId().equals(serviceId)) {
          break;
        }
      }
      if (service != null) {
        service.setScheduleId(schedule.getId());
        service.setScheduleName(schedule.getName());
        service.setPath(schedule.getPath());
        service.setScheduleRate(schedule.getRate());
        service.setScheduleStartTime(schedule.getStartTime());
        service.setScheduleEndTime(schedule.getEndTime());
        service.setScheduleStatus(schedule.getStatus());
        service.setUserInfo(schedule.getUserInfo());

        isAllowed(token, new PolicyRequest(service,
            PolicyRequest.CommandType.READ));

        try {
          service.getPath().setSrlgInclusions(
              RequestHandler.INSTANCE.getSRLGListForService(token, serviceId));
        }
        catch (RequestHandlerException e) {
          log.warn("Error looking up SRLG for " + serviceId, e);
        }
      }
    }
    
    return service;
  }

  /**
   * Look up the SRLG's used in a service
   */

  public String getSRLGListForService(LoginToken token, String serviceId)
      throws Exception {
    log.info("Retrieving SRLG by user {} for service id {}", token.getUser(), serviceId);
    try {
      
      String rc = getNrbInterface().getSRLGListForService(token, serviceId);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getSRLGListForService failed", e);
      throw e;
    }
  }

  /**
   * Retrieve status of the services created (or failed to create) as part of a
   * schedule.
   */
  public List<StatusType> getStatusInfoList(LoginToken token, String taskId)
      throws Exception {
    log.info("Retrieving status info list by user {} for task id {}", token.getUser(), taskId);
    try {
      
      List<StatusType> rc = getNrbInterface().getStatusInfo(token, taskId);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getStatusInfoList failed", e);
      throw e;
    }
  }

  public SysMetricType getSystemMetric(LoginToken token) throws Exception {
    try {
      log.debug("Retrieving system metrics by user {}", token.getUser());
      SysMetricType rc = getNrbInterface().getSystemMetric(token);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getSystemMetric failed", e);
      throw e;
    }
  }


  public UserDetails getUserDetails(LoginToken token) throws Exception {
    try {
      log.debug("Retrieving user details by user {}", token.getUser());
      UserDetails userDetails = getNrbInterface().getUserDetails(token);
      
      return userDetails;
    }
    catch (Exception e) {
      log.error("getUserDetails failed", e);
      throw e;
    }
  }

  public List<EndpointResourceUiType> getUserEndpointsUIInfo(LoginToken token,
      Map<String, String> filter) throws Exception {
    log.debug("Retrieving user endpoint UI info by user {} with filter {}", token.getUser(), filter);
    return getNrbInterface().getUserEndpointsUIInfo(token, filter);
  }

  // Called extensively by DRACServlet (supporting WebUI Ajax calls)
  public List<EndpointResourceUiType> getUserEndpointsUIInfo(LoginToken token,
      String resGroup, Map<String, String> filter) throws Exception {
    log.info("Retrieving user endpoint UI info by user {} with filter {} and resource group {}", new Object[]{token.getUser(), filter, resGroup});
    return getNrbInterface().getUserEndpointsUIInfo(token, resGroup, filter);
  }

  public List<String> getUserGroupNameLineage(LoginToken token,
      UserGroupProfile ugProfile) throws Exception {
    log.info("Retrieving user group name lineage by user {} with profile {}", new Object[]{token.getUser(), ugProfile});
    return getNrbInterface().getUserGroupNameLineage(token, ugProfile);
  }

  public UserGroupProfile getUserGroupProfile(LoginToken token,
      UserGroupName userGroupName) throws Exception {
    
    log.debug("Retrieving user group profile by user {} with user group name {}", new Object[]{token.getUser(), userGroupName});
    UserGroupProfile aUserGroupProfile = null;
    try {
      aUserGroupProfile = getNrbInterface().getUserGroupProfile(token,
          userGroupName);
    }
    catch (Exception e) {
      log.error("getUserGroupProfile failed", e);
      throw e;
    }
    
    return aUserGroupProfile;
  }

  public List<UserGroupName> getUserGroupProfileNames(LoginToken token)
      throws Exception {
    log.debug("Retrieving user group profile names by user {} ", new Object[]{token.getUser()});
    List<UserGroupProfile> groupList = getUserGroups(token);
    Set<UserGroupName> treeSet = new TreeSet<UserGroupName>();
    for (UserGroupProfile group : groupList) {
      treeSet.add(group.getName());
    }
    List<UserGroupName> result = new ArrayList<UserGroupName>(treeSet);
    
    return result;
  }

  /**
   * Retrieve a list of usergroups to filter results by i.e. show the user only
   * those objects (s)he has access to. If the user is a member of admin, the
   * list will be empty, so that no results are filtered
   * 
   * @param token
   *          LoginToken token from request, set at login
   * @return List<UserGroupName> list of usergropups
   * @throws Exception
   */
  public List<UserGroupName> getFilterGroupsForCurrentUser(LoginToken token)
      throws Exception {
    
    log.info("Retrieving filter groups for current user by {} ", new Object[]{token.getUser()});
    
    List<UserGroupProfile> userGroups = getUserGroups(token);
    boolean isSuperuser = false;
    List<UserGroupName> filterGroups = new ArrayList<UserGroupName>();
    for (UserGroupProfile profile : userGroups) {
      filterGroups.add(profile.getName());
      if (profile.getUserGroupType().equals(UserGroupType.SYSTEM_ADMIN)) {
        isSuperuser = true;
      }
    }
    if (isSuperuser) {
      filterGroups = new ArrayList<UserGroupName>();
    }
    return filterGroups;
  }

  // isAllowed moved server-side
  public List<UserGroupProfile> getUserGroups(LoginToken token)
      throws Exception {
    log.debug("Retrieving user groups by user {} ", new Object[]{token.getUser()});
    // 
    // List<UserGroupProfile> query = null;
    // try
    // {
    // query = getRemoteNRB().getUserGroupProfileList(token);
    // }
    // catch (Exception e)
    // {
    // throw e;
    // }
    // List<UserGroupProfile> result = new ArrayList<UserGroupProfile>();
    //
    // for (UserGroupProfile group : query)
    // {
    // try
    // {
    // isAllowed(token, new PolicyRequest(group,
    // PolicyRequest.CommandType.READ));
    // result.add(group);
    // }
    // catch (RequestHandlerException e)
    // {
    // }
    // }
    // 
    // return result;

    
    return getNrbInterface().getUserGroupProfileList(token);
  }

  public List<String> getUserIDs(LoginToken token) throws Exception {
    log.info("Retrieving user id's by {} ", new Object[]{token.getUser()});
    List<UserProfile> userList = this.getUserProfiles(token);
    List<String> result = new ArrayList<String>(userList.size());
    for (UserProfile user : userList) {
      result.add(user.getUserID());
    }
    
    return result;
  }

  public List<List<String>> getUserNameLineage(LoginToken token,
      UserProfile userProfile) throws Exception {
    log.info("Retrieving user names called by {} with user profile user id {}", new Object[]{token.getUser(), userProfile.getUserID()});
    return getNrbInterface().getUserNameLineage(token, userProfile);
  }

  public UserProfile getUserProfile(LoginToken token, String userID)
      throws Exception {
    
    log.info("Retrieving user group profile called by {} with user id {}", new Object[]{token.getUser(), userID});
    UserProfile aUserProfile = null;
    try {
      aUserProfile = getNrbInterface().getUserProfile(token, userID);
    }
    catch (Exception e) {
      log.error("getUserProfile failed", e);
      throw e;
    }
    
    return aUserProfile;
  }

  /**
   * Querying utilization for an endpoint. The implementation of this method is
   * done in the Multi-Layer Bandwidth Manager
   */

  public double getUtilization(LoginToken token, String tnaName)
      throws Exception {
    try {
      log.info("Retrieving utilization by {} for tna {}", new Object[]{token.getUser(), tnaName});
      double rc = getNrbInterface().getUtilization(token, tnaName);
      
      return rc;
    }
    catch (Exception e) {
      log.error("getUtilization failed", e);
      throw e;
    }
  }

  public UtilizationStructure getUtilization(LoginToken token, String tna,
      double speed, long startTime, long endTime, int days, TimeZone tz)
      throws Exception {
    try {
      
      log.info("Getting utilization by {} for tna {}, speed {}, start {}, end {}, days {} and timezone", 
          new Object[]{token.getUser(), tna, speed, startTime, endTime, days, tz});
      UtilizationStructure u = getNrbInterface().queryUtilization(token, tna,
          speed, startTime, endTime, days, tz);
      
      
      return u;
    }
    catch (Exception e) {
      log.error("getUtilization failed", e);
      throw e;
    }
  }

  public String getVersion() {
    String rc = proxy.getServerVersion();
    return rc;
  }

  public boolean isResourceGroupEditable(LoginToken token,
      ResourceGroupProfile rgProfile) throws Exception {
    
    return getNrbInterface().isResourceGroupEditable(token, rgProfile);
  }

  public boolean isResourceGroupParentable(LoginToken token, String rgName)
      throws Exception {
    
    ResourceGroupProfile aResourceGroupProfile = null;
    try {
      aResourceGroupProfile = getNrbInterface().getResourceGroupProfile(token,
          rgName);
    }
    catch (Exception e) {
      log.error("isResourceGroupParentable failed", e);
      throw e;
    }

    if (aResourceGroupProfile != null) {
      boolean rc = isAllowed(token, new PolicyRequest(aResourceGroupProfile,
          PolicyRequest.CommandType.SET_AS_PARENT));
      
      return rc;
    }

    
    return false;
  }

  // isAllowed moved server-side
  public boolean isUserEditable(LoginToken token, UserProfile userProfile)
      throws Exception {
    
    return getNrbInterface().isUserEditable(token, userProfile);
  }

  // isAllowed moved server-side
  public boolean isUserGroupEditable(LoginToken token,
      UserGroupProfile ugProfile) throws Exception {
    
    return getNrbInterface().isUserGroupEditable(token, ugProfile);
  }

  public LoginToken login(ClientLoginType clientType, String user,
      char[] password, String ip, String ticket, String sessionId) throws Exception {
    log.debug("Logging in user {} from ip {}, ticket {} and client type {}", new Object[]{user, ip, ticket, clientType});
    NrbInterface authenticator = getNrbInterface();
    if (authenticator == null) {
      log.error("NrbInterface object is null!  Cannot login!");
      throw new RequestHandlerException(
          DracErrorConstants.GENERAL_ERROR_INTERNAL, null);
    }

    LoginToken token = authenticator.login(clientType, user, password, ip,
        ticket, sessionId);

    
    return token;
  }

  public void logout(LoginToken token) throws Exception {
    log.debug("Logging out user {}", token.getUser());
    try {
      
      getNrbInterface().logout(token);
      
    }
    catch (Exception e) {
      log.error("logout failed", e);
      throw e;
    }
  }

  
  /**
   * Retrieve a list of service affecting alarms
   */
  public List<AlarmType> queryAllServiceAlarms(LoginToken token,
      long startTime, long endTime, List<UserGroupName> userGroups)
      throws Exception {
    log.info("Query all service alarms called by user {} with start time {}, end time {} and user groups {}", new Object[]{token.getUser(), startTime, endTime, userGroups});
    try {
      
      List<AlarmType> rc = getNrbInterface().queryAllServiceAlarms(token,
          startTime, endTime);
      
      return rc;
    }
    catch (Exception e) {
      log.error("queryAllServiceAlarms failed", e);
      throw e;
    }
  }

  /**
   * query NrbInterface to determine if a service is route-able
   */
  public boolean queryPath(LoginToken token, DracService aService)
      throws Exception {
    log.info("Query path called by user {} with service {}", new Object[]{token.getUser(), aService});
    checkServiceTna(token, aService);
    try {
      boolean rc = getNrbInterface().queryPath(token, aService);
      
      return rc;
    }
    catch (Exception e) {
      log.error("queryPath failed", e);
      throw e;
    }
  }

  public Schedule querySchedule(LoginToken token, String scheduleId)
      throws Exception {
    log.info("Query schedule called by user {} with schedule id {}", new Object[]{token.getUser(), scheduleId});
    Schedule schedule = null;
    try {
      schedule = getNrbInterface().getSchedule(token, scheduleId);
    }
    catch (Exception e) {
      log.error("querySchedule failed", e);
      throw e;
    }
    return schedule;
  }

  /**
   * Extend the duration of a given service by a time in minutes This method
   * returns the nr of minutes available for extension If this is equal to the
   * amount requested, the service was extended by that amount. If it is less
   * the service is not extended.
   * 
   * @param token
   * @param serviceToExtend
   * @return available minutes
   * @throws Exception
   */
  public int extendServiceTime(LoginToken token, DracService serviceToExtend,
      int minutesToExtendService) throws Exception {
    log.info("Extending servcie time called by user {} with service {} and amount {}",
        new Object[]{token.getUser(), serviceToExtend, minutesToExtendService});
    int availableMinutes = 0;
    try {
      availableMinutes = getNrbInterface().extendServiceTime(token,
          serviceToExtend, minutesToExtendService);
    }
    catch (Exception e) {
      log.error("extendServiceTime failed", e);
      throw e;
    }
    return availableMinutes;
  }

  /**
   * Query the all Schedules summary that the specified user can access. The
   * implementation is done in QueryUtility
   */
  public List<Schedule> querySchedules(LoginToken token, long startTime,
      long endTime, List<UserGroupName> groups) throws Exception {
    
    log.info("Querying schedules called by user {} with start time {}, end time {} and groups {}",
        new Object[]{token.getUser(), startTime, endTime, groups});
    
    try {
      
      List<Schedule> rc = getNrbInterface().querySchedules(token, startTime,
          endTime, groups);
      
      return rc;
    }
    catch (Exception e) {
      log.error("querySchedules failed", e);
      throw e;
    }
  }

  /**
   * Fins a schedule by its name and path for a user. If mutiple are found the
   * currently running one is returned. If none currently running the latest is
   * returned
   * 
   * @param token
   * @param startTime
   * @param endTime
   * @param pathType
   * @param name
   * @return
   * @throws Exception
   */
  public Schedule getScheduleByNamePathUser(LoginToken token, long startTime,
      long endTime, PathType pathType, String name) throws Exception {
    
    log.info("Retrieving schedule called by user {} with start time {}, end time {}, path type {} and name {}",
        new Object[]{token.getUser(), startTime, endTime, pathType, name});
    
    Schedule theSchedule = null;
    long now = new Date().getTime();
    try {
      log.debug("querySchedules invoked look for:" + name);
      List<Schedule> schedules = getNrbInterface().querySchedules(token,
          startTime, endTime, name);
      
      List<Schedule> schedulesWithSamePathType = new ArrayList<Schedule>();
      for (Schedule schedule : schedules) {
        if (schedule.getPath().simpleEquals(pathType)) {
          schedulesWithSamePathType.add(schedule);
        }
      }
      if (schedulesWithSamePathType.size() == 0) {
        log.debug("No schedule found for: " + name);
      }
      else if (schedulesWithSamePathType.size() == 1) {
        theSchedule = schedulesWithSamePathType.get(0);
      }
      else {
        theSchedule = schedulesWithSamePathType.get(schedulesWithSamePathType
            .size() - 1);
        for (Schedule schedule : schedulesWithSamePathType) {
          String numberPartId = theSchedule.getId().substring(
              theSchedule.getId().indexOf("-") + 1);
          long theScheduleId = Long.parseLong(numberPartId);
          String numberPartIdN = schedule.getId().substring(
              schedule.getId().indexOf("-") + 1);
          long theScheduleIdN = Long.parseLong(numberPartIdN);
          if (theScheduleIdN > theScheduleId) {
            theSchedule = schedule;
          }
          long scheduleStartTime = schedule.getStartTime();
          long scheduleEndTime = schedule.getEndTime();
          if (scheduleStartTime <= now && now <= scheduleEndTime) {
            theSchedule = schedule;
            break;
          }
        }
      }
    }
    catch (Exception e) {
      log.error("querySchedules failed", e);
      throw e;
    }
    return theSchedule;
  }

  public DracService getCurrentlyActiveServiceByScheduleId(LoginToken token,
      String scheduleId) throws Exception {
    log.info("Retrieving active services for schedule called by user {} with schedule id {}", new Object[]{token.getUser(), scheduleId});
    try {
      return getNrbInterface().getCurrentlyActiveServiceByScheduleId(token,
          scheduleId);
    }
    catch (Exception e) {
      log.error("Unexpected Exception caught", e);
      throw e;
    }
  }

  /**
   * Retrieve a list of service affecting alarms
   */

  public AlarmType queryServiceAlarm(LoginToken token, String alarmId)
      throws Exception {
    log.info("Querying service alarms called  by user {} with alarm id {}", new Object[]{token.getUser(), alarmId});
    try {
      return getNrbInterface().queryServiceAlarm(token, alarmId);
    }
    catch (Exception e) {
      log.error("Unexpected Exception caught", e);
      throw e;
    }
  }

  // New server-side-policy-checked retrieval interface for a single endpoint.
  // Returns null if no ep met filter criteria and access checks.
  public EndPointType retrieveEndpoint(LoginToken token,
      Map<String, String> facilityFilter) throws Exception {
    log.info("Retrieving endpoint called by user {} with filter {}", new Object[]{token.getUser(), facilityFilter});
    EndPointType ep = null;
    List<EndPointType> list = getNrbInterface().getUserEndpoints(token,
        facilityFilter);
    if (list != null && list.size() == 1) {
      ep = list.get(0);
    }

    return ep;
  }

  // New server-side-policy-checked retrieval interface for a list of endpoints.
  public List<EndPointType> retrieveEndpoints(LoginToken token,
      Map<String, String> filter) throws Exception {
    log.info("Retrieving endpoints called by user {} with filter {}", new Object[]{token.getUser(), filter});
    return getNrbInterface().getUserEndpoints(token, filter);
  }

  public void sessionValidate(LoginToken token) throws Exception {
    log.debug("Invalidating session for user {}", token.getUser());
    try {
      
      getNrbInterface().sessionValidate(token);
      
    }
    catch (Exception e) {
      log.error("sessionValidate failed", e);
      throw e;
    }
  }

  public void setConfirmationTimeout(LoginToken token, int timeout)
      throws Exception {
    log.info("Setting confirmation timeout called by user {} with timeout {}", new Object[]{token.getUser(), timeout});
    try {
      
      getNrbInterface().setConfirmationTimeout(token, timeout);
      
    }
    catch (Exception e) {
      log.error("setConfirmationTimeout failed", e);
      throw e;
    }
  }

  public void setResourceGroupMembership(LoginToken token,
      String resourceGroupName, MembershipData membership) throws Exception {
    log.info("Setting resource group membership called by user {} with resource group name {} and policy {}", new Object[]{token.getUser(), resourceGroupName, membership});
    try {
      
      getNrbInterface().setResourceGroupMembership(token, resourceGroupName,
          membership);
      
    }
    catch (Exception e) {
      log.error("setResourceGroupMembership failed", e);
      throw e;
    }
  }

  public void setResourceGroupPolicy(LoginToken token,
      String resourceGroupName, ResourcePolicy resourcePolicy) throws Exception {
    log.info("Setting resource group policy called by user {} with resource group name {} and policy {}", new Object[]{token.getUser(), resourceGroupName, resourcePolicy});
    isAllowed(token, new PolicyRequest(resourcePolicy,
        PolicyRequest.CommandType.WRITE));
    try {
      getNrbInterface().setResourceGroupPolicy(token, resourceGroupName,
          resourcePolicy);
      
    }
    catch (Exception e) {
      log.error("setResourceGroupPolicy failed", e);
      throw e;
    }
  }

  public void setResourceGroupResourceList(LoginToken token,
      String resourceGroupName, List<Resource> resource) throws Exception {
    log.info("Setting resource group list called by user {} with resource group name {} and resources {}", new Object[]{token.getUser(), resourceGroupName, resource});
    ResourceGroupProfile rgProfile = getResourceGroupProfile(token,
        resourceGroupName);
    isAllowed(token, new PolicyRequest(rgProfile,
        PolicyRequest.CommandType.EDIT));
    try {
      getNrbInterface().setResourceGroupResourceList(token, resourceGroupName,
          resource);
      
    }
    catch (Exception e) {
      log.error("setResourceGroupResourceList failed", e);
      throw e;
    }
  }

  public void setScheduleOffset(LoginToken token, int offset) throws Exception {
    log.info("Setting schedule offset called by user {} with offset {}", new Object[]{token.getUser(), offset});
    try {
      
      getNrbInterface().setScheduleOffset(token, offset);
      
    }
    catch (Exception e) {
      log.error("setScheduleOffset failed", e);
      throw e;
    }
  }

  /**
   * Called to change password
   */

  public void setupUserAuthenticationAccount(LoginToken token, String userId,
      AbstractCredential cred) throws Exception {
    
    log.info("Setting user authentication account called by user {} for user {} with credentials {}", new Object[]{token.getUser(), userId, cred});
    
    if (token != null) {
      isAllowed(token, new PolicyRequest(getUserProfile(token, userId),
          PolicyRequest.CommandType.EDIT));
      UserDetails userDetails = getUserDetails(token);
      if (userDetails.getCredential() instanceof LocalAccountCredential) {
        try {
          getNrbInterface().setupUserAuthenticationAccount(token, userId, cred);
        }
        catch (Exception e) {
          log.error("setupUserAuthenticationAccount failed", e);
          throw e;
        }
      }
      else {
        // not local account
        String reason = DracErrorConstants.formatErrorCode(Locale.getDefault(),
            "rh.reason.notLocalAccount", null);
        log.error("setupUserAuthenticationAccount failed not a local account!");
        throw new RequestHandlerException(
            DracErrorConstants.RH_ERROR_6999_INVALID_OP,
            new Object[] { reason });
      }
    }
    
  }

  public void setUserAccountStatus(LoginToken token, String userID,
      AccountStatus status) throws Exception {
    try {
      log.info("Setting user account status called by user {} for user {} with status {}", new Object[]{token.getUser(), userID, status});
      getNrbInterface().setUserAccountStatus(token, userID, status);
      
    }
    catch (Exception e) {
      log.error("setUserAccountStatus failed", e);
      throw e;
    }
  }

  public void setUserAuthenticationData(LoginToken token, String userID,
      AuthenticationData authData) throws Exception {
    try {
      log.info("Setting authentication data called by user {} for user  {} with data {}", new Object[]{token.getUser(), userID, authData});
      getNrbInterface().setUserAuthenticationData(token, userID, authData);
      
    }
    catch (Exception e) {
      log.error("setUserAuthenticationData failed", e);
      throw e;
    }
  }

  public void setUserGroupMembership(LoginToken token,
      UserGroupName userGroupName, MembershipData membership) throws Exception {
    log.info("Setting user group membership called by user {} for user group {} with data {}", new Object[]{token.getUser(), userGroupName, membership});
    try {
      
      getNrbInterface()
          .setUserGroupMembership(token, userGroupName, membership);
      
    }
    catch (Exception e) {
      log.error("setUserGroupMembership failed", e);
      throw e;
    }
  }

  public void setUserGroupUserGroupPolicy(LoginToken token,
      UserGroupName userGroupName, GroupPolicy groupPolicy) throws Exception {
    log.info("Setting user group policy called by user {} for user group {} with policy {}", new Object[]{token.getUser(), userGroupName, groupPolicy});
    isAllowed(token, new PolicyRequest(groupPolicy,
        PolicyRequest.CommandType.EDIT));
    try {
      getNrbInterface().setUserGroupUserGroupPolicy(token, userGroupName,
          groupPolicy);
      
    }
    catch (Exception e) {
      log.error("setUserGroupUserGroupPolicy failed", e);
      throw e;
    }
  }

  public void setUserMembership(LoginToken token, String userID,
      MembershipData membership) throws Exception {
    try {
      log.info("Setting user membership called by user {} for user {} with data {}", new Object[]{token.getUser(), userID, membership});
      getNrbInterface().setUserMembership(token, userID, membership);
      
    }
    catch (Exception e) {
      log.error("setUserMembership failed", e);
      throw e;
    }
  }

  public void setUserOrganization(LoginToken token, String userID,
      OrganizationData orgData) throws Exception {
    try {
      log.info("Setting user organization called by user {} for user {} with data {}", new Object[]{token.getUser(), userID, orgData});
      getNrbInterface().setUserOrganization(token, userID, orgData);
      
    }
    catch (Exception e) {
      log.error("setUserOrganization failed", e);
      throw e;
    }
  }

  public void setUserPersonalData(LoginToken token, String userID,
      PersonalData personalData) throws Exception {
    try {
      log.info("Setting personal data called by user {} for user {} with data {}", new Object[]{token.getUser(), userID, personalData});
      getNrbInterface().setUserPersonalData(token, userID, personalData);
      
    }
    catch (Exception e) {
      log.error("setUserPersonalData failed", e);
      throw e;
    }
  }

  public void setUserTimeZoneIDPreference(LoginToken token, String userId,
      String timeZoneId) throws Exception {
    try {
      log.info("Setting time zone called by user {} for user {} with timezone {}", new Object[]{token.getUser(), userId, timeZoneId});
      getNrbInterface().setUserTimeZoneIDPreference(token, userId, timeZoneId);
      
    }
    catch (Exception e) {
      log.error("setUserTimeZoneIDPreference failed", e);
      throw e;
    }
  }

  /**
   * Verify that a schedule object has its endpoint ID set. If not, look it up
   * by TNA
   */

  private void checkScheduleTna(LoginToken token, Schedule aSchedule)
      throws Exception {
    log.info("Check schedule tna called by user {} for schedule {}", new Object[]{token.getUser(), aSchedule});
    if (aSchedule != null && aSchedule.getPath() != null) {
      setEndpointId(token, aSchedule.getPath().getSourceEndPoint());
      setEndpointId(token, aSchedule.getPath().getTargetEndPoint());
    }
  }

  /**
   * Verify that a service object has its endpoint ID set. If not, look it up by
   * TNA
   */

  private void checkServiceTna(LoginToken token, DracService aService)
      throws Exception {
    log.info("Check service tna called by user {} for service {}", new Object[]{token.getUser(), aService});
    if (aService != null && aService.getPath() != null) {
      setEndpointId(token, aService.getPath().getSourceEndPoint());
      setEndpointId(token, aService.getPath().getTargetEndPoint());
    }
  }

  /**
   * Get the remote NrbInterface object
   * 
   * @throws Exception
   */
  private NrbInterface getNrbInterface() throws Exception {
    return proxy.getNrbInterface();
  }

  private List<UserProfile> getUserProfiles(LoginToken token) throws Exception {
    log.info("Retrieving user profiles called by {}", new Object[]{token.getUser()});
    return getNrbInterface().getUserProfileList(token);

  }

  private void init() {
    proxy = new RemoteConnectionProxy();
    log.debug("Primary ip is: " + proxy.getPrimaryController());
    log.debug("Secondary ip is: " + proxy.getSecondaryController());

    if (proxy.isSameController()) {
      try {
        NrbInterfaceUtils.blockingWaitForNrbInterfaceToStart(proxy
            .getPrimaryController());
      }
      catch (Exception e) {
        log.error("Error: ", e);
      }
      
    }
    else {
      while (true) {
        if (NrbInterfaceUtils.isNrbInterfaceAlive(proxy.getPrimaryController())) {
          break;
        }
        if (NrbInterfaceUtils.isNrbInterfaceAlive(proxy
            .getSecondaryController())) {
          break;
        }
        log.debug("Waiting for server to come alive, "
            + proxy.getPrimaryController() + " "
            + proxy.getSecondaryController());
        try {
          Thread.sleep(1000 * 5);
        }
        catch (InterruptedException e) {
          log.error("Error: ", e);
          break;
        }

      }
      proxy = new RemoteConnectionProxy();
    }
  }

  // Utility methods
  @Deprecated
  private boolean isAllowed(LoginToken token, PolicyRequest request)
      throws Exception {
    try {
      /*
       * TODO: Why do we return true/false when most callers fail to check the
       * result? Are we expecting an exception to be throw in instead?
       */
      boolean result = getNrbInterface().isAllowed(token, request);
      // boolean result = getRemoteNRB().isAllowed(userDetails, request);
      Object o = request.getRequestor();
      String requestName = "Policy_NULL";
      if (o != null) {
        requestName = o.getClass().getSimpleName();
        if (o instanceof Schedule) {
          requestName = "Schedule " + ((Schedule) o).getName();
        }
        else if (o instanceof DracService) {
          requestName = "Service " + ((DracService) o).getId() + " of "
              + ((DracService) o).getScheduleName();
        }
        else if (o instanceof EndPointType) {
          requestName = "Endpoint " + ((EndPointType) o).getName();
        }
      }
      log.debug("Policy check result [" + requestName + "]: "
          + (result ? "passed" : "failed"));
      return result;
    }
    catch (Exception e) {
      /*
       * log.error( "Policy checking exception trapped: " + e.getMessage(), e);
       */
      log.debug("Policy check failed." + e.getCause().getMessage(), e);
      throw new RequestHandlerException(e);
    }
  }

  /*-******************************************
   * Support methods for DRACServlet
   *******************************************-*/

  private void setEndpointId(LoginToken token, EndPointType ep)
      throws Exception {
    if (ep.getId().equals("") || ep.getId().equals("unknown")) {
      EndPointType dbSource = this.findEndpointByTna(token, ep.getName());
      if (dbSource == null) {
        throw new RequestHandlerException(
            DracErrorConstants.RH_ERROR_6003_NO_ENDPOINT_FOUND_WITH_TNA,
            new Object[] { ep.getName() });
      }
      ep.setId(dbSource.getId());
    }
  }
}
