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

package com.nortel.appcore.app.drac.server.nrb.impl.mlbm;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfileXML.ResourceType;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.UserGroupType;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.Site;
import com.nortel.appcore.app.drac.common.types.SysMetricType;
import com.nortel.appcore.app.drac.common.types.UtilizationStructure;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.dracdb.DbAdminConsoleUserPreferences;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPathAlarmDetails;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementConnection;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;
import com.nortel.appcore.app.drac.database.dracdb.DbSites;
import com.nortel.appcore.app.drac.database.helper.DbUtilityCommonUtility;
import com.nortel.appcore.app.drac.security.SecurityServer;

/**
 * @author nguyentd
 */
public enum MultiLayerBWM {
  INSTANCE;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private enum CheckType {
    SCHEDULE, USER, BILLINGGROUP
  }

  /*
   * We were seeing problems when getEndpoints was being invoked in parallel and
   * consuming too many resources. Here we block or limit the number of
   * outstanding operations that can run at once. Perhaps we should have another
   * latch on the entire NRB_PORT interface?
   */
  private final int MAX_CONCURRENT_ENDPOINT_QUERY_OPERATIONS = Integer
      .getInteger("concurrent.endpoint.query.operations", 10).intValue();

  private final Semaphore endpointLatch = new Semaphore(
      MAX_CONCURRENT_ENDPOINT_QUERY_OPERATIONS, true);

  private final SysMetricType sysMetric;
  private final int scheduleOffset = Integer.parseInt(System.getProperty(
      "org.opendrac.schedule.offset", "0"));

  private MultiLayerBWM() {
    sysMetric = new SysMetricType(scheduleOffset);
  }

  public void addSite(UserDetails usrDetails, Site site) throws RemoteException {
    try {
      DbSites.INSTANCE.add(site);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage(), e });
    }
  }

  public void deleteFacility(UserDetails userDetails, String neId, String aid)
      throws Exception {
    mustBeAdminUser(userDetails, "deleteFacility");
    DbNetworkElementFacility.INSTANCE.delete(neId, aid);
  }

  public List<Schedule> getActiveSchedules(UserDetails userDetails)
      throws NrbException {
    List<Schedule> query = null;
    try {
      query = DbUtilityCommonUtility.INSTANCE.findExpandableSchedules();
    }
    catch (Exception e) {
      log.error("Unexpected Exception caught", e);
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
    List<Schedule> result = new ArrayList<Schedule>(0);
    for (Schedule schedule : query) {
      try {
        isAllowed(userDetails, new PolicyRequest(schedule,
            PolicyRequest.CommandType.READ));
        result.add(schedule);
      }
      catch (NrbException e) { // ok, don't add to list
        log.error("Error: ", e);
      }
      catch (Exception e) {
        log.error("Unexpected exception while doing policy check", e);
        throw new NrbException(
            DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
            new Object[] { e.getMessage() }, e);
      }
    }
    return result;
  }

  public List<String> getAlarms(UserDetails userDetails,
      Map<String, Object> filter) throws RemoteException {
    try {
      return XmlUtility.elementToString(DbLightPathAlarmDetails.INSTANCE
          .retrieve(filter));
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<NetworkElementHolder> getAllNetworkElements(
      UserDetails userDetails) throws Exception {
    try {
      return DbNetworkElement.INSTANCE.retrieve(null);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  /*
   * This method implements a direct call to the db to retrieve facility
   * constraint information, used only by the admin console. This replaces
   * getNetworkElementFacilities.
   */
  public Map<String, BigInteger> getFacilityConstraints(
      Map<String, String> filter) throws Exception {
    try {
      return DbNetworkElementFacility.INSTANCE.getFacilityConstraints(filter);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<String> getInprogressCalls(UserDetails userDetails)
      throws RemoteException {
    try {
      return DbLightPath.INSTANCE.getInprogressCalls();
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<LogRecord> getLogs(UserDetails userDetails, long startTime,
      long endTime, Map<String, String> filter) throws Exception {
    log.debug("wp: getLogs userDetails=" + userDetails.getUserID()
        + " startime=" + startTime + " endTime=" + endTime + " params="
        + filter);

    if (filter != null && !filter.isEmpty()) {
      // replace any * with % for SQL
      if (filter.get(DbKeys.LogKeys.ORIGINATOR) != null
          && !filter.get(DbKeys.LogKeys.ORIGINATOR).equals("")) {
        filter.put(DbKeys.LogKeys.ORIGINATOR,
            filter.get(DbKeys.LogKeys.ORIGINATOR).replace('*', '%'));
      }
      if (filter.get(DbKeys.LogKeys.IP_ADDR) != null
          && !filter.get(DbKeys.LogKeys.IP_ADDR).equals("")) {
        filter.put(DbKeys.LogKeys.IP_ADDR, filter.get(DbKeys.LogKeys.IP_ADDR)
            .replace('*', '%') + "%");
      }
      if (filter.get(DbKeys.LogKeys.BILLING_GROUP) != null
          && !filter.get(DbKeys.LogKeys.BILLING_GROUP).equals("")) {
        filter.put(DbKeys.LogKeys.BILLING_GROUP,
            filter.get(DbKeys.LogKeys.BILLING_GROUP).replace('*', '%'));
      }
      if (filter.get(DbKeys.LogKeys.RESOURCE) != null
          && !filter.get(DbKeys.LogKeys.RESOURCE).equals("")) {
        filter.put(DbKeys.LogKeys.RESOURCE, filter.get(DbKeys.LogKeys.RESOURCE)
            .replace('*', '%'));
      }
    }

    List<LogRecord> result = null;
    try {

      List<LogRecord> query = DbLog.INSTANCE
          .getLogs(startTime, endTime, filter);

      /*
       * preallocate an array list the same size as the actual logs, the user
       * might be permitted to see them all or less than all.
       */
      result = new ArrayList<LogRecord>(query.size());
      log.debug("Found: " + query.size() + " logs prior to filtering");

      // build temporary security caches
      Map<String, Boolean> allowReadGroupCache = new HashMap<String, Boolean>();
      Map<String, Boolean> allowReadUserCache = new HashMap<String, Boolean>();

      /*
       * For each log determine if they the user is permitted to view it or not.
       */
      for (LogRecord log : query) {
        if (userDetails.getUserPolicyProfile().getUserGroupType() == UserGroupType.SYSTEM_ADMIN) {
          result.add(log);
        }
        else if (log.getOriginator().equals(userDetails.getUserID())) {
          result.add(log);
        }
        else if (log.isBillingGroupSet()) {
          UserGroupName billingGroup = log.getBillingGroup();
          /*
           * check our cache if we are allowed to see this schedule so we don't
           * keep checking over and over
           */
          if (isAllowedToRead(userDetails, billingGroup.toString(),
              allowReadGroupCache, CheckType.BILLINGGROUP)) {
            result.add(log);
          }
        }
        else if (log.isOriginatorSet()) {
          if (isAllowedToRead(userDetails, log.getOriginator(),
              allowReadUserCache, CheckType.USER)) {
            result.add(log);
          }
        }
      }
    }
    catch (Exception e) {
      throw e;
    }
    log.debug("Found: " + result.size() + " logs after filtering");

    return result;
  }

  public List<NetworkElementAdjacency> getNetworkElementAdjacencies(
      UserDetails userDetails, Map<String, String> filter)
      throws RemoteException {
    try {
      return DbNetworkElementAdjacency.INSTANCE.retrieve();
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<CrossConnection> getNetworkElementConnections(
      Map<String, String> filter, UserDetails userDetails)
      throws RemoteException {
    try {
      return DbNetworkElementConnection.INSTANCE.retrieve(filter);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<Facility> getNetworkElementFacilities(UserDetails userDetails,
      Map<String, String> filter) throws RemoteException {
    try {
      return DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<NetworkElementHolder> getNetworkElements(UserDetails userDetails,
      Map<String, String> filter) throws RemoteException {
    try {
      return DbNetworkElement.INSTANCE.retrieve(filter);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public Schedule getSchedule(UserDetails userDetails, String scheduleId)
      throws NrbException {

    Schedule aSchedule = null;
    boolean isActivated;
    
    try {
      aSchedule = DbUtilityCommonUtility.INSTANCE.querySchedule(scheduleId);
      isActivated = DbLog.INSTANCE.findServiceActivatedByResource(scheduleId);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
    if (aSchedule == null) {
      throw new NrbException(DracErrorConstants.MLBW_ERROR_2010_NOT_EXIST,
          new Object[] { scheduleId });
    }

    aSchedule.setActivated(isActivated);
    // check user privilege here
    isAllowed(userDetails, new PolicyRequest(aSchedule,
        PolicyRequest.CommandType.READ));

    return aSchedule;
  }

  public List<Schedule> getSchedules(UserDetails userDetails,
      Map<String, String> filter) throws RemoteException {
    try {
      return DbSchedule.INSTANCE.retrieve(filter);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public DracService getService(UserDetails userDetails, String serviceId)
      throws NrbException {
    DracService aService = null;
    try {
      aService = DbUtilityCommonUtility.INSTANCE
          .queryServiceSummaryFromServiceId(serviceId);
    }
    catch (Exception e) {
      log.error("Error looking up schedule " + serviceId, e);
      throw new NrbException(Locale.getDefault(),
          DracErrorConstants.MLBW_ERROR_2010_NOT_EXIST,
          new Object[] { serviceId }, e);
    }
    if (aService == null) {
      throw new NrbException(DracErrorConstants.MLBW_ERROR_2010_NOT_EXIST,
          new Object[] { serviceId });
    }
    // check user privilege here
    isAllowed(userDetails, new PolicyRequest(aService,
        PolicyRequest.CommandType.READ));
    return aService;
  }

  public List<ServiceXml> getServices(UserDetails userDetails,
      Map<String, Object> filter) throws RemoteException {
    try {
      return DbLightPath.INSTANCE.retrieve(filter);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<String> getServicesEligibleForPurge(UserDetails userDetails,
      Map<String, String> filter) throws RemoteException {
    try {
      return DbLightPath.INSTANCE.getServicesEligibleForPurge(filter);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<ServiceXml> getServicesFromAID(UserDetails userDetails,
      Map<String, String> filter) throws RemoteException {
    try {
      String aid = filter.get(DbKeys.LightPathCols.LP_AID);
      String neid = filter.get(DbKeys.LightPathCols.LP_NEID);
      return DbLightPath.INSTANCE.getServicesFromAid(aid, neid);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<DracService> getServiceUsageForTNA(UserDetails userDetails,
      String tna) throws NrbException {
    try {
      // no security check, all services must be considered in calculation
      return DbSchedule.INSTANCE.getServiceUsageForTNA(tna);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<EndPointType> getUserEndpoints(UserDetails userDetails,
      List<UserGroupName> userGroupFilter, Map<String, String> facilityFilter)
      throws Exception {
    // a server-side replacement of old RequestHandler::getEndpoints
    List<EndPointType> result = new ArrayList<EndPointType>();
    List<UserGroupProfile> ugProfiles = SecurityServer.INSTANCE
        .getUserGroupProfileList(userDetails);

    for (UserGroupProfile aUserGroup : ugProfiles) {
      if (userGroupFilter.isEmpty()
          || userGroupFilter.contains(aUserGroup.getName())) {
        Set<String> resGroups = aUserGroup.getMembership()
            .getMemberResourceGroupName();
        for (String resGroupName : resGroups) {
          ResourceGroupProfile rgp = SecurityServer.INSTANCE
              .getResourceGroupProfile(userDetails, resGroupName);
          result
              .addAll(retrieveUserEndpoints(userDetails, rgp, facilityFilter));
        }
      }
    }

    return result;
  }

  public List<EndPointType> getUserEndpoints(UserDetails userDetails,
      Map<String, String> facilityFilter) throws Exception {
    return retrieveUserEndpoints(userDetails, null, facilityFilter);
  }

  public List<EndpointResourceUiType> getUserEndpointsUIInfo(
      UserDetails userDetails, ResourceGroupProfile rgp,
      Map<String, String> facilityFilter) throws Exception {
    List<EndpointResourceUiType> result = new ArrayList<EndpointResourceUiType>();
    List<EndPointType> list = retrieveUserEndpoints(userDetails, rgp,
        facilityFilter);
    for (EndPointType ep : list) {
      result.add(new EndpointResourceUiType(ep));
    }
    return result;
  }

  /* No security check on this method for interal use only */
  public SysMetricType internalGetSystemMetric() {
    return sysMetric;
  }

  public String loadUserPreferences(UserDetails userDetails, String userId)
      throws Exception {
    try {
      Element e = DbAdminConsoleUserPreferences.INSTANCE.retrieve(userId);
      if (e == null) {
        return null;
      }
      return XmlUtility.elementToString(e);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public void purgeLogs(UserDetails userDetails, Map<String, String> data)
      throws Exception {
    // @TODO SEcurity check required
    if (data.containsKey(DbKeys.LogKeys.TIME)) {
      long time = Long.parseLong(data.get(DbKeys.LogKeys.TIME));
      DbLog.INSTANCE.deleteTimeLessThan(time);
    }
    else {
      DbLog.INSTANCE.deleteTimeLessThan(0);
    }
  }

  public void purgeServices(UserDetails userDetails, List<String> serviceIds)
      throws Exception {
    try {
      DbUtilityCommonUtility.INSTANCE.purgeServices(serviceIds, userDetails);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<AlarmType> queryAllServiceAlarms(UserDetails userDetails,
      long startTime, long endTime) throws NrbException {
    List<AlarmType> result = new ArrayList<AlarmType>();
    try {
      List<AlarmType> query = DbUtilityCommonUtility.INSTANCE
          .queryAllServiceAlarms(startTime, endTime);
      for (AlarmType alarm : query) {
        try {
          // Test for access policy:
          getSchedule(userDetails, alarm.getScheduleId());
          result.add(alarm);
        }
        catch (NrbException e) {
          if (e.getErrorCode() >= DracErrorConstants.SECURITY_ERROR_MARKER_START
              && e.getErrorCode() < DracErrorConstants.MLBW_ERROR_MARKER_START) {
            continue;
          }

          // Do not throw the exception here any longer. Log the potential data
          // inconsistency and
          // continue.
          log.error("Data mismatch - alarmId " + alarm.getId()
              + " no corresponding schedule id: " + alarm.getScheduleId());
          continue;
        }
      }
    }
    catch (NrbException e) {
      throw e;
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
    return result;
  }

  @Deprecated
  public List<EndPointType> queryAllUserEndpoints(UserDetails userDetails)
      throws NrbException {
    try {
      endpointLatch.acquire();
      try {
        List<EndPointType> result = new ArrayList<EndPointType>();

        Map<String, String> facilityFilter = new HashMap<String, String>();
        facilityFilter.put(DbKeys.NetworkElementFacilityCols.LAYER,
            Layer.LAYER_ALL.toString());
        List<Map<String, String>> list = DbNetworkElementFacility.INSTANCE
            .retrieveUserEndpoints(facilityFilter);

        for (Map<String, String> facMap : list) {
          EndPointType ep = new EndPointType(facMap);
          result.add(ep);
        }

        return result;
      }
      finally {
        endpointLatch.release();
      }
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public Schedule queryScheduleByService(UserDetails userDetails,
      String serviceId) throws NrbException {
    try {
      Schedule aSchedule = DbUtilityCommonUtility.INSTANCE
          .queryScheduleFromServiceId(serviceId);

      // check user privilege here
      if (aSchedule != null) {
        isAllowed(userDetails, new PolicyRequest(aSchedule,
            PolicyRequest.CommandType.READ));
      }
      else {
        throw new NrbException(DracErrorConstants.MLBW_ERROR_2010_NOT_EXIST,
            new Object[] { serviceId });
      }

      return aSchedule;
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<Schedule> querySchedules(UserDetails userDetails, long startTime,
      long endTime, List<UserGroupName> groups, String name)
      throws NrbException {
    List<Schedule> query = null;
    try {
      query = DbUtilityCommonUtility.INSTANCE.querySchedules(startTime,
          endTime, groups, name);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
    List<Schedule> result = new ArrayList<Schedule>(0);
    for (Schedule schedule : query) {
      try {
        isAllowed(userDetails, new PolicyRequest(schedule,
            PolicyRequest.CommandType.READ));
        result.add(schedule);
      }
      catch (NrbException e) { // ok, don't add to list
        log.error("Error: ", e);
      }
      catch (Exception e) {
        throw new NrbException(
            DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
            new Object[] { e.getMessage() }, e);
      }
    }
    return result;
  }

  public AlarmType queryServiceAlarm(UserDetails userDetails, String alarmId)
      throws NrbException {
    try {
      AlarmType alarm = DbUtilityCommonUtility.INSTANCE
          .queryServiceAlarm(alarmId);

      if (alarm != null) {
        try {
          // Test for access policy:
          getSchedule(userDetails, alarm.getScheduleId());
        }
        catch (NrbException e) {
          if (!(e.getErrorCode() >= DracErrorConstants.SECURITY_ERROR_MARKER_START && e
              .getErrorCode() < DracErrorConstants.MLBW_ERROR_MARKER_START)) {
            // Do not throw the exception here any longer. Log the potential
            // data inconsistency
            // and continue.
            log.error("Data mismatch - alarmId " + alarm.getId()
                + " no corresponding schedule id: " + alarm.getScheduleId());
          }
        }
      }

      return alarm;
    }

    catch (Exception e) {
      log.error("Unexpected Exception caught", e);
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  // public List<ScheduleType> queryAllSchedule(UserDetails userDetails, long
  // startTime, long endTime)
  // throws Exception
  // {
  //
  // List<ScheduleType> temp =
  // DbUtilityCommonUtility.INSTANCE.queryAllSchedule(userDetails,
  // startTime,
  // endTime);
  //
  // if (temp == null)
  // {
  // return temp;
  // }
  //
  // List<ScheduleType> result = new ArrayList<ScheduleType>();
  // ScheduleType schedule = null;
  //
  // for (Iterator<ScheduleType> it = temp.iterator(); it.hasNext();)
  // {
  // schedule = it.next();
  // try
  // {
  // isAllowed(userDetails, new PolicyRequest(schedule,
  // PolicyRequest.CommandType.READ));
  // result.add(schedule);
  //
  // }
  // catch (Exception e)
  // {
  // continue;
  // }
  // }
  //
  // return result;
  // }

  public List<DracService> queryServices(UserDetails userDetails,
      long startTime, long endTime, List<UserGroupName> groups)
      throws NrbException {
    List<DracService> query = null;
    List<DracService> result = new ArrayList<DracService>(0);
    try {
      query = DbUtilityCommonUtility.INSTANCE.queryServices(startTime, endTime,
          groups);
    }
    catch (Exception e) {
      log.error("Unexpected Exception caught", e);
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }

    for (DracService service : query) {
      try {
        isAllowed(userDetails, new PolicyRequest(service,
            PolicyRequest.CommandType.READ));
        result.add(service);
      }
      catch (NrbException e) { // ok, don't add to list
        log.error("Error: ", e);
      }
      catch (Exception e) {
        throw new NrbException(
            DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
            new Object[] { e.getMessage() }, e);
      }

    }
    return result;
  }

  public UtilizationStructure queryUtilization(UserDetails userDetails,
      String tna, double speed, long startTime, long endTime, int days,
      TimeZone tz) throws RemoteException {
    try {
      // no security check, all services must be considered in calculation
      log.debug("Invoking getUtilization on " + tna + "  startime:" + startTime
          + " endTime:" + endTime + " days:" + days + " tz:" + tz);
      List<DracService> serviceList = DbUtilityCommonUtility.INSTANCE
          .queryUtilization(tna, startTime, endTime);
      double[] bandwidth = UtilizationCalculator.calculate(speed, startTime,
          endTime, serviceList, days, tz);
      return new UtilizationStructure(serviceList, bandwidth);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public List<Site> retrieveSiteList(UserDetails userDetails)
      throws RemoteException {
    try {
      return DbSites.INSTANCE.retrieveAll();
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public void saveUserPreferences(UserDetails userDetails, String userId,
      String xmlPreferences) throws Exception {
    try {
      Element preferences = XmlUtility.createDocumentRoot(xmlPreferences);
      DbAdminConsoleUserPreferences.INSTANCE.addUpdate(userId, preferences);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public void updateSite(UserDetails userDetails, Site site)
      throws RemoteException {
    try {
      DbSites.INSTANCE.update(site);
    }
    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
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
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  private boolean isAllowedToRead(UserDetails userDetails, String id,
      Map<String, Boolean> cache, CheckType type) {
    // check our cache if we are allowed to see this group so we don't keep
    // checking over and over
    Boolean allow = cache.get(id);
    if (allow != null && allow.booleanValue()) {
      return true;
    }
    else if (allow != null && !allow.booleanValue()) {
      return false;
    }
    else if (allow == null) { // we never checked this object yet
      try {
        if (type == CheckType.SCHEDULE) {
          Schedule sched = getSchedule(userDetails, id);
          if (sched != null) {
            cache.put(id, Boolean.TRUE);
            return true;
          }
        }
        else if (type == CheckType.USER) {
          UserProfile user = SecurityServer.INSTANCE.getUserProfile(
              userDetails, id);
          if (user != null) {
            cache.put(id, Boolean.TRUE);
            return true;
          }
        }
        else if (type == CheckType.BILLINGGROUP) {
          UserGroupProfile group = SecurityServer.INSTANCE.getUserGroupProfile(
              userDetails, new UserGroupName(id));
          if (group != null) {
            cache.put(id, Boolean.TRUE);
            return true;
          }
        }
      }
      catch (DracException rhe) {
        if (rhe.getErrorCode() >= DracErrorConstants.SECURITY_ERROR_MARKER_START
            && rhe.getErrorCode() < DracErrorConstants.MLBW_ERROR_MARKER_START) {
          cache.put(id, Boolean.FALSE);
        }
        else { // not a security error?
          log.error("Error querying type: " + type + " id: " + id, rhe);
        }
      }
      catch (Exception e) {
        log.error("Error querying type: " + type + " id: " + id, e);
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

  private List<EndPointType> retrieveUserEndpoints(UserDetails userDetails,
      ResourceGroupProfile rgp, Map<String, String> facilityFilter)
      throws Exception {
    try {
      List<EndPointType> result = new ArrayList<EndPointType>();

      // If the request wasn't scoped by a resource group, or, if the resource
      // group is 'default',
      // there is no resource list to cross reference ...
      if (rgp == null || rgp.getDefaultResourceGroup()) {
        boolean accessToAllResources = userDetails.getUserPolicyProfile()
            .hasAccessToAllResources();

        List<Map<String, String>> query = DbNetworkElementFacility.INSTANCE
            .retrieveUserEndpoints(facilityFilter);

        for (Map<String, String> facMap : query) {
          EndPointType ep = new EndPointType(facMap);
          if (accessToAllResources) {
            result.add(ep);
          }
          else {
            try {
              if (SecurityServer.INSTANCE.isAllowed(userDetails,
                  new PolicyRequest(ep, PolicyRequest.CommandType.READ))) {
                result.add(ep);
              }
            }
            catch (Exception e) {
              continue;
            }
          }
        }
      }
      else {
        List<Resource> resources = rgp.getResourceList();
        if (resources == null || resources.isEmpty()) {
          return result;
        }

        List<String> resourceEndpointIds = new ArrayList<String>();

        for (Resource resource : resources) {
          if (resource.getResourceType() == ResourceType.ENDPOINT) {
            resourceEndpointIds.add(resource.getResourceID());
          }
        }

        if (resourceEndpointIds.size() > 0) {
          List<Map<String, String>> query = DbNetworkElementFacility.INSTANCE
              .retrieveUserEndpoints(resourceEndpointIds, facilityFilter);
          for (Map<String, String> facMap : query) {
            result.add(new EndPointType(facMap));
          }
        }
      }

      return result;
    }

    catch (Exception e) {
      throw new NrbException(DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

}
