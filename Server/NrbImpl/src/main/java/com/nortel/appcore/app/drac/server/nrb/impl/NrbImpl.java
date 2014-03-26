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

package com.nortel.appcore.app.drac.server.nrb.impl;

import java.io.Serializable;
import java.math.BigInteger;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.opendrac.security.policy.LoginAttemptsPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
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
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.common.types.AuditResult;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.GraphData;
import com.nortel.appcore.app.drac.common.types.LpcpStatus;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.Site;
import com.nortel.appcore.app.drac.common.types.StatusType;
import com.nortel.appcore.app.drac.common.types.SysMetricType;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.UtilizationStructure;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.security.SecurityServer;
import com.nortel.appcore.app.drac.server.neproxy.NeProxyManager;
import com.nortel.appcore.app.drac.server.nrb.LpcpEventCallback;
import com.nortel.appcore.app.drac.server.nrb.NrbRemote;
import com.nortel.appcore.app.drac.server.nrb.NrbServerInterface;
import com.nortel.appcore.app.drac.server.nrb.impl.administration.LpcpAdminManager;
import com.nortel.appcore.app.drac.server.nrb.impl.mlbm.MultiLayerBWM;

/**
 * The NrbImpl implements the NrbServerInterface interface. Nothing very
 * interesting occurs here as we use helper classes to do the real work.
 * <p>
 * Created on Dec 14, 2005
 * 
 * @author nguyentd
 */
public final class NrbImpl implements NrbServerInterface, Remote, Serializable {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final long serialVersionUID = 1L;
  private final int port = RmiServerInfo.NRB_PORT;
  private final NrbRemote nrb;

  public NrbImpl() throws Exception {
    log.debug("Binding to port " + port);
    nrb = new NrbRemote(port, this);
  }

  @Override
  public void activateService(UserDetails userDetails, String serviceId)
      throws Exception {
    log.info("Activate service called for service id {} by user {}", serviceId, userDetails.getUserID());
    LpcpAdminManager.INSTANCE.activateService(userDetails, serviceId);
  }

  @Override
  public Map<String, Exception> activateServices(UserDetails userDetails,
      String[] serviceIds) throws Exception {
    Map<String, Exception> exceptionList = new HashMap<String, Exception>();
    if (serviceIds != null) {
      for (String serviceId : serviceIds) {
        try {
          log.debug("calling activateService service on " + serviceId);
          activateService(userDetails, serviceId);
        }
        catch (Exception nrbe) {
          log.error("Error: ", nrbe);
          exceptionList.put(serviceId, nrbe);
        }
      }
    }
    return exceptionList;
  }

  @Override
  public void addManualAdjacency(UserDetails userDetails, String sourceIEEE,
      String sourcePort, String destIEEE, String destPort) throws Exception {
    log.info("Add manual adjacency called by user {} with sourceIEEE {}, source port {}, destIEEE{}, dest port{}",
       new Object[]{userDetails.getUserID(), sourceIEEE, sourcePort, destIEEE, destPort});
    NeProxyManager.INSTANCE.addManualAdjacency(userDetails, sourceIEEE,
        sourcePort, destIEEE, destPort);
  }

  @Override
  public void updateNetworkElementPosition(UserDetails userDetails, String ip,
      String port, Double positionX, Double positionY) throws Exception {
    log.info("Update NE position called by user {} with ip {}, port {}, x {}, y {}",
        new Object[]{userDetails.getUserID(), ip, port, positionX, positionY});
    NeProxyManager.INSTANCE.updateNetworkElementPosition(userDetails, ip, port,
        positionX, positionY);
  }

  @Override
  public void addSite(UserDetails userDetails, Site site) throws Exception {
    log.info("Adding site called by user {} with site {}",
        new Object[]{userDetails.getUserID(), site});
    MultiLayerBWM.INSTANCE.addSite(userDetails, site);
  }

  @Override
  public String asyncCreateSchedule(UserDetails userDetails, Schedule aSchedule)
      throws Exception {
    log.info("Creating schedule called by user {} with schedule {}",
        new Object[]{userDetails.getUserID(), aSchedule});
    return LpcpAdminManager.INSTANCE
        .asyncCreateSchedule(userDetails, aSchedule);
  }

  @Override
  public List<AuditResult> auditModel(UserDetails userDetails) throws Exception {
    log.info("Audit model called by user {}",
        new Object[]{userDetails.getUserID()});
    return LpcpAdminManager.INSTANCE.auditModel(userDetails);
  }

  @Override
  public String auditUserProfileLinkages(UserDetails userDetails)
      throws Exception {
    log.info("Audit user profile linkages called by user {}",
        new Object[]{userDetails.getUserID()});
    return SecurityServer.INSTANCE.auditUserProfileLinkages(userDetails);
  }

  @Override
  public void authenticate(ClientLoginType loginType, String user,
      char[] password, String clientIp, String aSelectTicket) throws Exception {
//    log.info("Authenticating user {}, with ip {} and type {}",
//        new Object[]{user, clientIp, loginType});
    SecurityServer.INSTANCE.authenticate(loginType, user, password, clientIp,
        aSelectTicket);
  }

  @Override
  public LoginToken authorize(ClientLoginType loginType, String user,
      char[] password, String clientIp, String aSelectTicket) throws Exception {
    log.info("Authorizing user {}, with ip {} and type {}",
        new Object[]{user, clientIp, loginType});
    return SecurityServer.INSTANCE.authorize(loginType, user, password,
        clientIp, aSelectTicket);
  }

  @Override
  public void cancelSchedule(UserDetails userDetails, String scheduleId)
      throws Exception {
    log.info("Cancel schedule called by user {} for schedule id {}",
        new Object[]{userDetails.getUserID(), scheduleId});
    LpcpAdminManager.INSTANCE.cancelSchedule(userDetails, scheduleId);
  }

  @Override
  public Map<String, Exception> cancelSchedules(UserDetails userDetails,
      String[] scheduleIds) throws Exception {
    Map<String, Exception> exceptionList = new HashMap<String, Exception>();
    if (scheduleIds != null) {
      for (String scheduleId : scheduleIds) {
        try {
          log.debug("calling cancel schedule on " + scheduleId);
          cancelSchedule(userDetails, scheduleId);
        }
        catch (Exception nrbe) {
          exceptionList.put(scheduleId, nrbe);
        }
      }
    }
    return exceptionList;
  }

  @Override
  public void cancelService(UserDetails userDetails, String serviceId)
      throws Exception {
    log.info("Cancel service called by user {} for service id {}",
        new Object[]{userDetails.getUserID(), serviceId});
    LpcpAdminManager.INSTANCE.cancelService(userDetails, serviceId);
  }

  @Override
  public Map<String, Exception> cancelServices(UserDetails userDetails,
      String[] serviceIds) throws Exception {
    Map<String, Exception> exceptionList = new HashMap<String, Exception>();
    if (serviceIds != null) {
      for (String serviceId : serviceIds) {
        try {
          log.debug("calling cancel service on " + serviceId);
          cancelService(userDetails, serviceId);
        }
        catch (Exception nrbe) {
          exceptionList.put(serviceId, nrbe);
        }
      }
    }
    return exceptionList;
  }

  @Override
  public boolean cancelTask(UserDetails userDetails, String taskId)
      throws Exception {
    log.info("Cancel task called by user {} for task id {}",
        new Object[]{userDetails.getUserID(), taskId});
    return LpcpAdminManager.INSTANCE.cancelTask(userDetails, taskId);
  }

  @Override
  public void changeNetworkElementPassword(UserDetails userDetails,
      NetworkElementHolder updatedNE) throws Exception {
    log.info("Change NE password called by user {} for ne {}",
        new Object[]{userDetails.getUserID(), updatedNE.getTid()});
    NeProxyManager.INSTANCE
        .changeNetworkElementPassword(userDetails, updatedNE);
  }

  @Override
  public void changePassword(UserDetails userDetails, String oldpw, String newpw)
      throws Exception {
    log.info("Change user password called by user {}",
        new Object[]{userDetails.getUserID()});
    SecurityServer.INSTANCE.changePassword(userDetails, oldpw, newpw);
  }

  @Override
  public boolean clearTaskInfo(UserDetails userDetails, String taskId)
      throws Exception {
    log.info("Clear task info called by user {} for task id {}",
        new Object[]{userDetails.getUserID(), taskId});
    return LpcpAdminManager.INSTANCE.clearTaskInfo(userDetails, taskId);
  }

  @Override
  public void confirmSchedule(UserDetails userDetails, String scheduleId)
      throws Exception {
    log.info("Confirm schedule called by user {} for schedule id {}",
        new Object[]{userDetails.getUserID(), scheduleId});
    LpcpAdminManager.INSTANCE.confirmSchedule(userDetails, scheduleId);
  }

  @Override
  public Map<String, Exception> confirmSchedules(UserDetails userDetails,
      String[] scheduleIds) throws Exception {
    Map<String, Exception> exceptionList = new HashMap<String, Exception>();
    if (scheduleIds != null) {
      for (String scheduleId : scheduleIds) {
        try {
          log.debug("calling confirm schedule on " + scheduleId);
          confirmSchedule(userDetails, scheduleId);
        }
        catch (Exception nrbe) {
          exceptionList.put(scheduleId, nrbe);
        }
      }
    }
    return exceptionList;
  }

  @Override
  public void correctModel(UserDetails userDetails) throws Exception {
    log.info("Correct model called by user {}",
        new Object[]{userDetails.getUserID()});
    LpcpAdminManager.INSTANCE.correctModel(userDetails);
  }

  @Override
  public void createResourceGroupProfile(UserDetails userDetails,
      ResourceGroupProfile profile) throws Exception {
    log.info("Create resource group profile called by user {} with profile {}",
        new Object[]{userDetails.getUserID(), profile});
    SecurityServer.INSTANCE.createResourceGroupProfile(userDetails, profile);
  }

  @Override
  public ScheduleResult createSchedule(UserDetails userDetails,
      Map<SPF_KEYS, String> parms, boolean queryOnly) throws Exception {
    log.info("Create schedule called by user {} with params {} and queryOnly",
        new Object[]{userDetails.getUserID(), parms, queryOnly});
    return LpcpAdminManager.INSTANCE.createSchedule(userDetails, parms,
        queryOnly);
  }

  @Override
  public String createService(UserDetails userDetails, String scheduleId,
      DracService aService) throws Exception {
    log.info("Create service called by user {} with service {} and schedule id",
        new Object[]{userDetails.getUserID(), aService, scheduleId});
    return LpcpAdminManager.INSTANCE.createService(userDetails, scheduleId,
        aService);
  }

  @Override
  public void createUserGroupProfile(UserDetails userDetails,
      UserGroupProfile profile) throws Exception {
    log.info("Creating user group profile by user {} with profile {}", userDetails.getUserID(), profile.toString());
    SecurityServer.INSTANCE.createUserGroupProfile(userDetails, profile);
  }

  @Override
  public void createUserProfile(UserDetails userDetails, String userID,
      AbstractCredential cred, AccountStatus status) throws Exception {
    log.info("Creating user profile by user {} for user {}", userDetails.getUserID(), userID);
    SecurityServer.INSTANCE
        .createUserProfile(userDetails, userID, cred, status);
  }

  @Override
  public void deleteCrossConnection(UserDetails userDetails,
      CrossConnection xcon) throws Exception {
    log.info("Deleting XCON called by user {} with xcon {}",
        new Object[]{userDetails.getUserID(), xcon});
    NeProxyManager.INSTANCE.deleteCrossConnection(userDetails, xcon);
  }

  @Override
  public void deleteFacility(UserDetails userDetails, String neId, String aid)
      throws Exception {
    log.info("Deleting facility called by user {} with ne id {} and aid {}",
        new Object[]{userDetails.getUserID(), neId, aid});
    MultiLayerBWM.INSTANCE.deleteFacility(userDetails, neId, aid);
  }

  @Override
  public void deleteManualAdjacency(UserDetails userDetails, String neIEEE,
      String port) throws Exception {
    log.info("Deleting manual adjacency called by user {} with neIEEE {} and port",
        new Object[]{userDetails.getUserID(), neIEEE, port});
    NeProxyManager.INSTANCE.deleteManualAdjacency(userDetails, neIEEE, port);
  }

  @Override
  public void deleteManualAdjacency(UserDetails userDetails, String sourceIEEE,
      String sourcePort, String destIEEE, String destPort) throws Exception {
    log.info("Deleting manual adjacency called by user {} with source neIEEE {}, source port {}, destIEEEE{}, destPort {}",
        new Object[]{userDetails.getUserID(), sourceIEEE, sourcePort, destIEEE, destPort});
    NeProxyManager.INSTANCE.deleteManualAdjacency(userDetails, sourceIEEE,
        sourcePort, destIEEE, destPort);
  }

  @Override
  public void deleteNetworkElement(UserDetails userDetails,
      NetworkElementHolder oldNe) throws Exception {
    log.info("Deleting NE called by user {} for NE {}",
        new Object[]{userDetails.getUserID(), oldNe});
    LpcpAdminManager.INSTANCE.deleteNetworkElement(userDetails, oldNe);
  }

  @Override
  public void deleteResourceGroupProfile(UserDetails userDetails,
      String resourceGroupName) throws Exception {
    log.info("Deleting resource group profile called by user {} for resource group {}",
        new Object[]{userDetails.getUserID(), resourceGroupName});
    SecurityServer.INSTANCE.deleteResourceGroupProfile(userDetails,
        resourceGroupName);
  }

  @Override
  public void deleteUserGroupProfile(UserDetails userDetails, UserGroupName name)
      throws Exception {
    log.info("Deleting user group profile called by user {} for user group {}",
        new Object[]{userDetails.getUserID(), name});
    SecurityServer.INSTANCE.deleteUserGroupProfile(userDetails, name);
  }

  @Override
  public void deleteUserProfile(UserDetails userDetails, String userID)
      throws Exception {
    log.info("Deleting user profile called by user {} for user {}",
        new Object[]{userDetails.getUserID(), userID});
    SecurityServer.INSTANCE.deleteUserProfile(userDetails, userID);
  }

  @Override
  public void editEndPoint(UserDetails userDetails, EndPointType endPoint)
      throws Exception {
    log.info("Edit endpoint called by user {} for endpoint {}",
        new Object[]{userDetails.getUserID(), endPoint});
    LpcpAdminManager.INSTANCE.editEndPoint(userDetails, endPoint);
  }

  @Override
  public void editFacility(UserDetails userDetails, String neid, String aid,
      String tna, String faclabel, String mtu, String srlg, String grp,
      String cost, String metric2, String sigType, String constraints,
      String domainId, String siteId) throws Exception {
    
    log.info("Edit facility called by user {} with neid {}, aid {}, tna {}, faclabel {}, mtu {}, " +
    		"srlg {}, grp {}, cost {}, metric2 {}, sigType {}, constraints {}, domain id {}, site id {}",
        new Object[]{userDetails.getUserID(), neid, aid, tna, faclabel, mtu, srlg, grp, cost, metric2, sigType, constraints, domainId, siteId});
    LpcpAdminManager.INSTANCE.editFacility(userDetails, neid, aid, tna,
        faclabel, mtu, srlg, grp, cost, metric2, sigType, constraints,
        domainId, siteId);
  }

  @Override
  public void enrollNetworkElement(UserDetails userDetails,
      NetworkElementHolder newNe) throws Exception {
    log.info("Enrolling NE called by user {} for NE {}",
        new Object[]{userDetails.getUserID(), newNe});
    NeProxyManager.INSTANCE.enrollNetworkElement(userDetails, newNe);
  }

  @Override
  public List<Schedule> getActiveSchedules(UserDetails userDetails)
      throws Exception {
    log.info("Returning all active schedules called by user {}",
        new Object[]{userDetails.getUserID()});
    return MultiLayerBWM.INSTANCE.getActiveSchedules(userDetails);
  }

  @Override
  public List<String> getAlarms(UserDetails userDetails,
      Map<String, Object> filter) throws Exception {
    log.info("Returning all alarams schedules called by user {} with filter {}",
        new Object[]{userDetails.getUserID(), filter});
    return MultiLayerBWM.INSTANCE.getAlarms(userDetails, filter);
  }

  @Override
  public List<NetworkElementHolder> getAllNetworkElements(
      UserDetails userDetails) throws Exception {
    log.info("Returning all bework elements  called by user {}",
        new Object[]{userDetails.getUserID()});
    return MultiLayerBWM.INSTANCE.getAllNetworkElements(userDetails);
  }

  @Override
  public int getConfirmationTimeout(UserDetails userDetails) throws Exception {
    return SecurityServer.INSTANCE.getConfirmationTimeout(userDetails);
  }

  @Override
  public Resource getEndpointResource(UserDetails userDetails, String resourceID)
      throws Exception {
    return SecurityServer.INSTANCE.getEndpointResource(userDetails, resourceID);
  }

  @Override
  public List<Facility> getFacilities(UserDetails userDetails, String neId)
      throws Exception {
    return NeProxyManager.INSTANCE.getFacilities(userDetails, neId);
  }

  @Override
  public Map<String, BigInteger> getFacilityConstraints(
      UserDetails userDetails, Map<String, String> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getFacilityConstraints(filter);
  }

  @Override
  public GlobalPolicy getGlobalPolicy(UserDetails userDetails) throws Exception {
    return SecurityServer.INSTANCE.getGlobalPolicy(userDetails);
  }

  @Override
  public GraphData getGraphData(UserDetails userDetails) throws Exception {
    return LpcpAdminManager.INSTANCE.getGraphData(userDetails);
  }


  @Override
  public ServerInfoType getInfo(UserDetails userDetails) throws Exception {
    return LpcpAdminManager.INSTANCE.getInfo(userDetails);
  }

  @Override
  public List<String> getInprogressCalls(UserDetails userDetails)
      throws Exception {
    return MultiLayerBWM.INSTANCE.getInprogressCalls(userDetails);
  }

  @Override
  public List<BandwidthRecord> getInternalBandwithUsage(
      UserDetails userDetails, long startTime, long endTime) throws Exception {
    return LpcpAdminManager.INSTANCE.getInternalBandwithUsage(userDetails,
        startTime, endTime);
  }

  @Override
  public List<LogRecord> getLogs(UserDetails userDetails, long startTime,
      long endTime, Map<String, String> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getLogs(userDetails, startTime, endTime,
        filter);
  }

  @Override
  public String getLpcpDiscoveryStatus(UserDetails userDetails)
      throws Exception {
    return LpcpAdminManager.INSTANCE.getLpcpDiscoveryStatus(userDetails);
  }

  @Override
  public LpcpStatus getLpcpStatus(UserDetails userDetails) throws Exception {
    return LpcpAdminManager.INSTANCE.getLpcpStatus(userDetails);
  }

  @Override
  public List<NetworkElementAdjacency> getNetworkElementAdjacencies(
      UserDetails userDetails, Map<String, String> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getNetworkElementAdjacencies(userDetails,
        filter);
  }

  @Override
  public List<CrossConnection> getNetworkElementConnections(
      UserDetails userDetails, Map<String, String> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getNetworkElementConnections(filter,
        userDetails);
  }

  @Override
  public List<Facility> getNetworkElementFacilities(UserDetails userDetails,
      Map<String, String> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getNetworkElementFacilities(userDetails,
        filter);
  }

  @Override
  public List<NetworkElementHolder> getNetworkElements(UserDetails userDetails,
      Map<String, String> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getNetworkElements(userDetails, filter);
  }

  @Override
  public String getPeerIPAddress(UserDetails userDetails) throws Exception {
    return LpcpAdminManager.INSTANCE.getPeerIPAddress(userDetails);
  }

  @Override
  public List<String> getResourceGroupNameLineage(UserDetails userDetails,
      ResourceGroupProfile rgProfile) throws Exception {
    return SecurityServer.INSTANCE.getResourceGroupNameLineage(userDetails,
        rgProfile, new ArrayList<String>());
  }

  @Override
  public ResourceGroupProfile getResourceGroupProfile(UserDetails userDetails,
      String resourceGroupName) throws Exception {
    // The SecurityServer.INSTANCE will use udt to check resource access
    // privileges
    return SecurityServer.INSTANCE.getResourceGroupProfile(userDetails,
        resourceGroupName);
  }

  @Override
  public List<ResourceGroupProfile> getResourceGroupProfileList(
      UserDetails userDetails) throws Exception {
    return SecurityServer.INSTANCE.getResourceGroupProfileList(userDetails);
  }

  @Override
  public Schedule getSchedule(UserDetails userInfo, String scheduleId)
      throws Exception {
    return MultiLayerBWM.INSTANCE.getSchedule(userInfo, scheduleId);
  }

  @Override
  public int extendServiceTime(UserDetails userDetails,
      DracService serviceToExtend, int minutesToExtendService) throws Exception {
    
    log.info("Extending service time called by user {}, for service {} with minutes to extend {}",
        new Object[]{userDetails.getUserID(),serviceToExtend, minutesToExtendService});
    
    
    if (minutesToExtendService < 0) {
      throw new Exception(
          "Connot extend a service with a negative nr of minutes");
    }
    Schedule schedule = getSchedule(userDetails,
        serviceToExtend.getScheduleId());
    int nrMinutesExtended = 0;
    int availableMinutes = LpcpAdminManager.INSTANCE.queryExtendServiceTime(
        userDetails, serviceToExtend, minutesToExtendService, schedule);
    if (availableMinutes == minutesToExtendService) {
      nrMinutesExtended = LpcpAdminManager.INSTANCE.extendServiceTime(
          userDetails, serviceToExtend, minutesToExtendService);
    }
    return nrMinutesExtended;
  }

  @Override
  public List<Schedule> getSchedules(UserDetails userDetails,
      Map<String, String> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getSchedules(userDetails, filter);
  }

  @Override
  public List<ServiceXml> getServices(UserDetails userDetails,
      Map<String, Object> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getServices(userDetails, filter);
  }

  @Override
  public List<String> getServicesEligibleForPurge(UserDetails userDetails,
      Map<String, String> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getServicesEligibleForPurge(userDetails,
        filter);
  }

  @Override
  public List<ServiceXml> getServicesFromAID(UserDetails userDetails,
      Map<String, String> filter) throws Exception {
    return MultiLayerBWM.INSTANCE.getServicesFromAID(userDetails, filter);
  }

  @Override
  public List<DracService> getServiceUsageForTNA(UserDetails userDetails,
      String tna) throws Exception {
    return MultiLayerBWM.INSTANCE.getServiceUsageForTNA(userDetails, tna);
  }

  @Override
  public String getSRLGListForService(UserDetails userDetails, String serviceId)
      throws Exception {
    return LpcpAdminManager.INSTANCE.getSRLGListForService(userDetails,
        serviceId);
  }

  @Override
  public List<StatusType> getStatusInfo(UserDetails userDetails, String taskId)
      throws Exception {
    return LpcpAdminManager.INSTANCE.getStatusInfo(userDetails, taskId);
  }

  @Override
  public SysMetricType getSystemMetric(UserDetails userDetails)
      throws Exception {
    return SecurityServer.INSTANCE.getSystemMetric(userDetails);
  }

  @Override
  public List<TaskType> getTaskInfo(UserDetails userDetails) throws Exception {
    return LpcpAdminManager.INSTANCE.getTaskInfo(userDetails);
  }

  @Override
  public TaskType getTaskInfo(UserDetails userDetails, String taskId)
      throws Exception {
    return LpcpAdminManager.INSTANCE.getTaskInfo(userDetails, taskId);
  }


  @Override
  public UserDetails getUserDetails(LoginToken token) throws Exception {
    return SecurityServer.INSTANCE.getUserDetails(token);
  }

  @Override
  public List<EndPointType> getUserEndpoints(UserDetails userDetails,
      List<UserGroupName> userGroupFilter, Map<String, String> facilityFilter)
      throws Exception {
    // The login session has been validated by the remote interface.

    return MultiLayerBWM.INSTANCE.getUserEndpoints(userDetails,
        userGroupFilter, facilityFilter);
  }

  @Override
  public List<EndPointType> getUserEndpoints(UserDetails userDetails,
      Map<String, String> facilityFilter) throws Exception {
    // The login session has been validated by the remote interface.

    return MultiLayerBWM.INSTANCE.getUserEndpoints(userDetails, facilityFilter);
  }

  @Override
  public List<EndpointResourceUiType> getUserEndpointsUIInfo(
      UserDetails userDetails, Map<String, String> facilityFilter)
      throws Exception {
    return this.getUserEndpointsUIInfo(userDetails, getResourceGroupProfileList(userDetails),
        facilityFilter);
  }

  List<EndpointResourceUiType> getUserEndpointsUIInfo(UserDetails userDetails,
	        List<ResourceGroupProfile> resourceGrpProfiles, Map<String, String> facilityFilter) throws Exception {
	List<EndpointResourceUiType> endpointResourceUiTypes = new ArrayList<EndpointResourceUiType>();
	for (ResourceGroupProfile profile : resourceGrpProfiles) {
		endpointResourceUiTypes.addAll(MultiLayerBWM.INSTANCE.getUserEndpointsUIInfo(userDetails, profile,
			        facilityFilter));
	}
	return endpointResourceUiTypes;
  }
	
  @Override
  public List<EndpointResourceUiType> getUserEndpointsUIInfo(
      UserDetails userDetails, String resGroup,
      Map<String, String> facilityFilter) throws Exception {
    // The login session has been validated by the remote interface.

    ResourceGroupProfile rgp = null;
    if (resGroup != null) {
      // If the endpoint retrieval is to be scoped via a resource group,
      // the ResourceGroupProfile retrieval will be policy checked first:
      rgp = SecurityServer.INSTANCE.getResourceGroupProfile(userDetails,
          resGroup);
    }

    return MultiLayerBWM.INSTANCE.getUserEndpointsUIInfo(userDetails, rgp,
        facilityFilter);
  }

  @Override
  public List<String> getUserGroupNameLineage(UserDetails userDetails,
      UserGroupProfile ugProfile) throws Exception {
    return SecurityServer.INSTANCE.getUserGroupNameLineage(userDetails,
        ugProfile, new ArrayList<String>());
  }

  @Override
  public UserGroupProfile getUserGroupProfile(UserDetails userDetails,
      UserGroupName name) throws Exception {
    return SecurityServer.INSTANCE.getUserGroupProfile(userDetails, name);
  }

  @Override
  public List<UserGroupProfile> getUserGroupProfileList(UserDetails userDetails)
      throws Exception {
    return SecurityServer.INSTANCE.getUserGroupProfileList(userDetails);
  }

  @Override
  public List<List<String>> getUserNameLineage(UserDetails userDetails,
      UserProfile userProfile) throws Exception {
    return SecurityServer.INSTANCE.getUserNameLineage(userDetails, userProfile);
  }

  @Override
  public UserProfile getUserProfile(UserDetails userDetails, String userID)
      throws Exception {
    return SecurityServer.INSTANCE.getUserProfile(userDetails, userID);
  }

  @Override
  public List<UserProfile> getUserProfileList(UserDetails userDetails)
      throws Exception {
    return SecurityServer.INSTANCE.getUserProfileList(userDetails);
  }

  @Override
  public double getUtilization(UserDetails userDetails, String tnaName)
      throws Exception {
    return LpcpAdminManager.INSTANCE.getUtilization(userDetails, tnaName);
  }

  @Override
  public boolean isAllowed(UserDetails userDetails, PolicyRequest request)
      throws Exception {
    return SecurityServer.INSTANCE.isAllowed(userDetails, request);
  }

  /**
   * Check if NRB_PORT is alive, simply returns true
   * 
   * @TODO check userDetails
   */
  @Override
  public boolean isAlive(UserDetails userDetails) throws Exception {
    return true;
  }

  @Override
  public boolean isResourceGroupEditable(UserDetails userDetails,
      ResourceGroupProfile rgProfile) throws Exception {
    return SecurityServer.INSTANCE.isResourceGroupEditable(userDetails,
        rgProfile);
  }

  @Override
  public boolean isUserEditable(UserDetails userDetails, UserProfile userProfile)
      throws Exception {
    return SecurityServer.INSTANCE.isUserEditable(userDetails, userProfile);
  }

  @Override
  public boolean isUserGroupEditable(UserDetails userDetails,
      UserGroupProfile ugProfile) throws Exception {
    return SecurityServer.INSTANCE.isUserGroupEditable(userDetails, ugProfile);
  }

  @Override
  public String loadUserPreferences(UserDetails userDetails, String userId)
      throws Exception {
    return MultiLayerBWM.INSTANCE.loadUserPreferences(userDetails, userId);
  }

  @Override
  public LoginToken login(ClientLoginType loginType, String user,
      char[] password, String clientIp, String aSelectTicket, String sessionId) throws Exception {
    log.info("Logging in user {} from ip {}, ticket {} and client type {}", new Object[]{user, clientIp, aSelectTicket, loginType});
    return SecurityServer.INSTANCE.login(loginType, user, password, clientIp,
        aSelectTicket, sessionId);
  }

  @Override
  public void logout(LoginToken token) throws Exception {
    log.info("Logging out user {} ", new Object[]{token.getUser()});
    SecurityServer.INSTANCE.logout(token);
  }
  
  @Override
  public UserDetails prepareLogin(ClientLoginType loginType, String user,
      char[] password, String clientIp, String aSelectTicket, String sessionId) throws Exception {
    log.info("Preparing login for user {} for ip {} with ticket and session id {}",
        new Object[]{user, clientIp, aSelectTicket, sessionId});
    return SecurityServer.INSTANCE.prepareLogin(loginType, user, password,
        clientIp, aSelectTicket, sessionId);
  }

  @Override
  public void purgeLogs(UserDetails userDetails, Map<String, String> data)
      throws Exception {
    log.info("Purge logs called by user {}, with data {}",
        new Object[]{userDetails.getUserID(),data});
    MultiLayerBWM.INSTANCE.purgeLogs(userDetails, data);
  }

  @Override
  public void purgeServices(UserDetails userDetails, List<String> serviceIds)
      throws Exception {
    log.info("Purge service called by user {}, for service id's {}",
        new Object[]{userDetails.getUserID(),serviceIds});
    MultiLayerBWM.INSTANCE.purgeServices(userDetails, serviceIds);
  }

  @Override
  public List<AlarmType> queryAllServiceAlarms(UserDetails userDetails,
      long startTime, long endTime) throws Exception {
    log.info("Get all service alarams called by user {}, for start time {} and end time {}",
        new Object[]{userDetails.getUserID(),startTime, endTime});
    return MultiLayerBWM.INSTANCE.queryAllServiceAlarms(userDetails, startTime,
        endTime);
  }

  @Override
  public List<EndPointType> queryAllUserEndpoints(UserDetails userDetails)
      throws Exception {
    log.info("Get all user endpoints called by user {}",
        new Object[]{userDetails.getUserID()});
    return MultiLayerBWM.INSTANCE.queryAllUserEndpoints(userDetails);
  }

  @Override
  public boolean queryPath(UserDetails userDetails, DracService aService)
      throws Exception {
    log.info("Query path called by user {}, for service {}",
        new Object[]{userDetails.getUserID(), aService});
    return LpcpAdminManager.INSTANCE.queryPath(userDetails, aService);
  }

  @Override
  public Schedule queryScheduleByService(UserDetails userDetails,
      String serviceId) throws Exception {
    log.info("Query schedule by service called by user {}, for service id {}",
        new Object[]{userDetails.getUserID(), serviceId});
    return MultiLayerBWM.INSTANCE
        .queryScheduleByService(userDetails, serviceId);
  }

  @Override
  public List<Schedule> querySchedules(UserDetails userDetails, long startTime,
      long endTime, List<UserGroupName> groups, String name) throws Exception {
    log.info("Query schedule by called by user {}, with start time {}, end time {} for groups {}, with name {}",
        new Object[]{userDetails.getUserID(), startTime, endTime, groups, name});
    return MultiLayerBWM.INSTANCE.querySchedules(userDetails, startTime,
        endTime, groups, name);
  }

  @Override
  public AlarmType queryServiceAlarm(UserDetails userDetails, String alarmId)
      throws Exception {
    log.info("Query service alarms by called by user {} for alaram id  {}",
        new Object[]{userDetails.getUserID(), alarmId});
    return MultiLayerBWM.INSTANCE.queryServiceAlarm(userDetails, alarmId);
  }

  @Override
  public List<DracService> queryServices(UserDetails userDetails,
      long startTime, long endTime, List<UserGroupName> groups)
      throws Exception {
    log.info("Querying services by called by user {}, with start time {}, end time {} for groups {}",
        new Object[]{userDetails.getUserID(), startTime, endTime, groups});
    return MultiLayerBWM.INSTANCE.queryServices(userDetails, startTime,
        endTime, groups);
  }

  @Override
  public UtilizationStructure queryUtilization(UserDetails userDetails,
      String tna, double speed, long startTime, long endTime, int days,
      TimeZone tz) throws Exception {
    return MultiLayerBWM.INSTANCE.queryUtilization(userDetails, tna, speed,
        startTime, endTime, days, tz);
  }

  @Override
  public void registerForLpcpEventNotifications(UserDetails userDetails,
      LpcpEventCallback cb) throws Exception {
    LpcpAdminManager.INSTANCE
        .registerForLpcpEventNotifications(userDetails, cb);
  }

  @Override
  public List<Site> retrieveSiteList(UserDetails userDetails) throws Exception {
    return MultiLayerBWM.INSTANCE.retrieveSiteList(userDetails);
  }

  @Override
  public void saveUserPreferences(UserDetails userDetails, String userId,
      String xmlPreferences) throws Exception {
    MultiLayerBWM.INSTANCE.saveUserPreferences(userDetails, userId,
        xmlPreferences);
  }


  @Override
  public void sessionValidate(LoginToken token) throws Exception {
    SecurityServer.INSTANCE.sessionValidate(token);
  }

  @Override
  public void setConfirmationTimeout(UserDetails userDetails, int timeout)
      throws Exception {
    SecurityServer.INSTANCE.setConfirmationTimeout(userDetails, timeout);
  }

  @Override
  public void setDefaultGlobalPolicy(UserDetails userDetails,
      GlobalPolicy policy) throws Exception {
    SecurityServer.INSTANCE.setDefaultGlobalPolicy(userDetails, policy);
  }

  @Override
  public void setResourceGroupMembership(UserDetails userDetails,
      String resourceGroupName, MembershipData data) throws Exception {
    SecurityServer.INSTANCE.setResourceGroupMembership(userDetails,
        resourceGroupName, data);
  }

  @Override
  public void setResourceGroupPolicy(UserDetails userDetails,
      String resourceGroupName, ResourcePolicy resourcePolicy) throws Exception {
    SecurityServer.INSTANCE.setResourceGroupPolicy(userDetails,
        resourceGroupName, resourcePolicy);
  }

  @Override
  public void setResourceGroupResourceList(UserDetails userDetails,
      String resourceGroupName, List<Resource> resources) throws Exception {
    SecurityServer.INSTANCE.setResourceGroupResourceList(userDetails,
        resourceGroupName, resources);
  }

  @Override
  public void setScheduleOffset(UserDetails userDetails, int offset)
      throws Exception {
    SecurityServer.INSTANCE.setScheduleOffset(userDetails, offset);
  }

  @Override
  public void setupUserAuthenticationAccount(UserDetails userDetails,
      String userID, AbstractCredential cred) throws Exception {
    SecurityServer.INSTANCE.setupUserAuthenticationAccount(userDetails, userID,
        cred);
  }

  @Override
  public void setUserAccountStatus(UserDetails userDetails, String userID,
      AccountStatus status) throws Exception {
    SecurityServer.INSTANCE.setUserAccountStatus(userDetails, userID, status);
  }

  @Override
  public void setUserAuthenticationData(UserDetails userDetails, String userID,
      AuthenticationData authenticationData) throws Exception {
    SecurityServer.INSTANCE.setUserAuthenticationData(userDetails, userID,
        authenticationData);
  }

  @Override
  public void setUserGroupMembership(UserDetails userDetails,
      UserGroupName userGroupName, MembershipData data) throws Exception {
    SecurityServer.INSTANCE.setUserGroupMembership(userDetails, userGroupName,
        data);
  }

  @Override
  public void setUserGroupUserGroupPolicy(UserDetails userDetails,
      UserGroupName userGroupName, GroupPolicy groupPolicy) throws Exception {
    SecurityServer.INSTANCE.setUserGroupUserGroupPolicy(userDetails,
        userGroupName, groupPolicy);
  }

  @Override
  public void setUserMembership(UserDetails userDetails, String userID,
      MembershipData membership) throws Exception {
    SecurityServer.INSTANCE.setUserMembership(userDetails, userID, membership);
  }

  @Override
  public void setUserOrganization(UserDetails userDetails, String userID,
      OrganizationData orgData) throws Exception {
    SecurityServer.INSTANCE.setUserOrganization(userDetails, userID, orgData);
  }

  @Override
  public void setUserPersonalData(UserDetails userDetails, String userID,
      PersonalData personalData) throws Exception {
    SecurityServer.INSTANCE.setUserPersonalData(userDetails, userID,
        personalData);
  }

  @Override
  public void setUserTimeZoneIDPreference(UserDetails userDetails,
      String userID, String localeValue) throws Exception {
    SecurityServer.INSTANCE.setUserTimeZoneIDPreference(userDetails, userID,
        localeValue);
  }

  @Override
  public void toggleNetworkElementAssociation(UserDetails userDetails,
      NetworkElementHolder existingNe) throws Exception {
    
    log.info("Toggeling network element associated by user {} for NE {}",
        new Object[]{userDetails.getUserID(), existingNe});
    
    NeProxyManager.INSTANCE.toggleNetworkElementAssociation(userDetails,
        existingNe);
  }

  @Override
  public void updateSite(UserDetails userDetails, Site site) throws Exception {
    MultiLayerBWM.INSTANCE.updateSite(userDetails, site);
  }

  /**
   * Used to get the NRB_PORT INSTANCE which we bind into the RMI registry
   */

  protected NrbRemote getNrb() {
    return nrb;
  }

  @Override
  public void updateAddressAndPort(UserDetails userDetails,
      final String oldAddress, final int oldPort, final String newAddress,
      final int newPort) throws Exception {
    log.info("Updating address and port called  by user {} with original address {}, original port {}, new address {}, new port {}",
        new Object[]{userDetails.getUserID(), oldAddress, oldPort, newAddress, newPort});
    NeProxyManager.INSTANCE.updateAddressAndPort(userDetails, oldAddress,
        oldPort, newAddress, newPort);
  }

	@Override
	public void setLockedIPs(List<String> stillLockedIPs) throws Exception {
	  log.info("Locking ip addresses  {}",
        new Object[]{stillLockedIPs});
		Iterator<String> lockedIPs = LoginAttemptsPolicy.INSTANCE.getIPsInLockout().iterator();
		while (lockedIPs.hasNext()) {
			String ip = lockedIPs.next();
			if (!stillLockedIPs.contains(ip)) {
				LoginAttemptsPolicy.INSTANCE.removeIPFromCache(ip);
			}
		}
	}
	@Override
	public void setLockedIPs(LoginToken token, List<String> stillLockedIPs) throws Exception {
	  log.info("Locking ip addresses called by user {} with ip's {}",
        new Object[]{token.getUser(), stillLockedIPs});
		Iterator<String> lockedIPs = LoginAttemptsPolicy.INSTANCE.getIPsInLockout().iterator();
		while (lockedIPs.hasNext()) {
			String ip = lockedIPs.next();
			if (!stillLockedIPs.contains(ip)) {
				LoginAttemptsPolicy.INSTANCE.removeIPFromCache(ip);
			}
		}
	}

}
