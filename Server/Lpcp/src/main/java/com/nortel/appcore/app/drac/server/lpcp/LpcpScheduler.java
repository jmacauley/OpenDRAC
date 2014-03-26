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
package com.nortel.appcore.app.drac.server.lpcp;

import static com.nortel.appcore.app.drac.common.types.PathType.PROTECTION_TYPE.*;
import static com.nortel.appcore.app.drac.common.types.SPF_KEYS.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.errorhandling.RoutingException;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.ServiceXml.XC_TYPE;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.database.helper.DbUtilityLpcpScheduler;
import com.nortel.appcore.app.drac.server.lpcp.common.LpcpConstants;
import com.nortel.appcore.app.drac.server.lpcp.common.Utility;
import com.nortel.appcore.app.drac.server.lpcp.routing.HierarchicalModel;
import com.nortel.appcore.app.drac.server.lpcp.scheduler.LightPathScheduler;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility;
import com.nortel.appcore.app.drac.server.lpcp.trackers.SonetTrackerI;

/**
 * Scheduler: Responsible for finding a path through the network from A to Z,
 * given all sorts of constraints to worry about.
 */
@SuppressWarnings("unchecked")
public final class LpcpScheduler {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final Lpcp lpcp;

  public LpcpScheduler(final Lpcp lpcp) {
    this.lpcp = lpcp;
  }

  /**
   * A front end on scheduling. All queries/creation requests come here. We put
   * this front end in place (using a Map<SFP_KEYS,object> to
   * control/document/understand what can be passed in to us.
   */
  public ScheduleResult doScheduleFrontEnd(Map<SPF_KEYS, String> params,
      boolean queryOnly) throws Exception {

    // Start: Ticket #130
    final LpcpFacility dstFac = lpcp.getModelMgr().getFacilityForTNA(
        params.get(SPF_DSTTNA));
    final LpcpFacility srcFac = lpcp.getModelMgr().getFacilityForTNA(
        params.get(SPF_SRCTNA));
    if (dstFac != null && srcFac != null) {
      if (dstFac.getNeId().equalsIgnoreCase(srcFac.getNeId())) {
        params.put(SPF_PROTECTION, UNPROTECTED.toString());
      }
    }
    // Stop: Ticket #130

    if (queryOnly) {
      log.debug("doScheduleFrontEnd: Querying schedule with values "
          + params.toString());
    }
    else {
      log.debug("doScheduleFrontEnd: Creating schedule with values "
          + params.toString());
    }

    Map<SPF_KEYS, Object> m = new HashMap<SPF_KEYS, Object>();
    for (Map.Entry<SPF_KEYS, String> e : params.entrySet()) {
      m.put(e.getKey(), e.getValue());
    }

    try {
      ScheduleResult result = doSchedule(m, queryOnly);

      return result;
    }
    catch (Exception e) {
      log.error("doScheduleFrontEnd: returning with exception ", e);
      throw e;
    }
  }

  /**
   * @TODO: I don't like that this method is not private, but we drive junit
   *        tests through here, I'd prefer we change those unit test cases to
   *        drive doScheduleFrontEnd() but that requires a running database and
   *        a populated model.
   *        <p>
   *        This method must always be called from doSchedule as that is where
   *        the lock on the modelMgr is obtained. Calling this method from
   *        anywhere else is not thread safe!
   */
  void schedule(Map<SPF_KEYS, Object> parameters) throws Exception {

    // Data for Shortest Path First algorithm. Will hold completed
    // path information.
    Map<SPF_KEYS, Object> spf = null;

    // Shared Risk Link Group exclusion values.
    String srlgs[] = null;

    // We just spent a ton of time putting the request values into the
    // map, but now we have to take them out again.

    // The source and destination ports of service.
    String srcTNA = (String) parameters.get(SPF_SRCTNA);
    String dstTNA = (String) parameters.get(SPF_DSTTNA);

    // Requested bandwidth for service.
    String rate = (String) parameters.get(SPF_RATE);

    // Maximum routing cost of the service using link "cost" values.
    String cost = (String) parameters.get(SPF_COST);

    // Maximum routing cost of the service using link "metric2" values.
    String metric2 = (String) parameters.get(SPF_METRIC2);

    // The cross connect exclusion list for overlapping schedules.
    List<CrossConnection> exclusion = (List<CrossConnection>) parameters
        .get(SPF_EXCLUDE);

    // List of Shared Risk Link Group values to exclude when visiting
    // links during path compuatation.
    String srlg = (String) parameters.get(SPF_SRLG);

    // Existing schedules to route this new service path diverse.
    String srsg = (String) parameters.get(SPF_DIVERSE_EXCLUDE);

    // Type of protection requested for this service.
    String protection = (String) parameters.get(SPF_PROTECTION);

    // Determine if the service request involves layer 1 VCAT resources.
    // If so we may need to route multiple connection paths for this
    // service. This can occur for layer 1 services, or the L2SS/EPL Ethernet
    // services.
    boolean vcatRoutingOptionRequested = "true".equals(parameters
        .get(SPF_VCATROUTING_OPTION));

    // Now we need to validate all the request data, prepare bandwidth
    // trackers, and the routing graph.
    try {

      // A value of -1 indicates costs are not considered for path
      // computation.
      int excludeCost = -1;
      int excludeMetric = -1;

      // Verify the requested bandwidth is a non-negative integer.
      checkRate(rate);

      // If we have exclusions from other schedules then populate the
      // data into appropriate bandwidth trackers.
      if (exclusion != null) {
        // Populate the trackers with the overlapping bandwidth
        // and mark ETH trackers with existing VLAN ID flows.
        lpcp.getModelMgr().markBandwidth(exclusion);
      }

      // Parse the comma separated list of services to exclude.
      if (srsg != null) {
        // Exclude service edges from graph.
        String[] serviceIds = srsg.split(",");
        log.debug("Detected " + serviceIds.length
            + " Shared Risk Service Group(s): " + srsg);
        for (String id : serviceIds) {
          excludeService(id);
        }
      }
      else {

      }

      // If SRLGs were specified, edges with the specified SRLGs need
      // to be removed from the routing graph. We need to do this in
      // the loop since each call to getPath() in the routingEngine will
      // reset the excluded edges. This can be improved
      if (srlg != null && !srlg.trim().equals("") && !"unknown".equals(srlg)) {
        log.debug("SRLGs found.  Excluding SRLG edges");

        srlgs = srlg.split(",");
        if (srlgs.length > 0) {
          // SRLGs specified - exclude these edges now.
          lpcp.getRoutingEngine().excludeSRLG(srlgs, lpcp.getTopologyMgr());
        }
      }

      // Convert the string routing cost to an integer if specified.
      if (cost != null) {
        try {
          excludeCost = Integer.parseInt(cost);
        }
        catch (Exception e) {
          log.error("Cannot parse cost: " + cost, e);
        }
      }

      if (excludeCost > 0) {
        // @TODO: Why not provide the integer instead of the string?
        // Investigate removing the use of excludeCost() since it
        // provides very little value. Cost based routing is based on
        // path computation minimizing on the sum of a cost metric on
        // all links in the path. This is excluding individual links
        // that are greater than the cost.
        lpcp.getRoutingEngine().excludeCost(cost, lpcp.getTopologyMgr());
      }

      // Convert the string routing metric to an integer if specified.
      if (metric2 != null) {
        try {
          excludeMetric = Integer.parseInt(metric2);
        }
        catch (Exception e) {
          log.error("Cannot parse metric2: " + metric2, e);
        }
      }

      if (excludeMetric > 0) {
        // @TODO: Why not provide the integer instead of the string?
        // Investigate removing the use of excludeMetric() since it
        // provides very little value. Cost based routing is based on
        // path computation minimizing on the sum of a cost metric on
        // all links in the path. This is excluding individual links
        // that are greater than the cost.
        lpcp.getRoutingEngine().excludeMetric(metric2, lpcp.getTopologyMgr());
      }

      // Map the abstract source TNA to a physical facility.
      LpcpFacility srcFac = lpcp.getModelMgr().getFacilityForTNA(srcTNA);

      // For layer 2 (Ethernet) ports we need to see if a
      // VLAN ID is also needed in the request.
      if (srcFac != null && Layer.LAYER2.equals(srcFac.getLayer())) {
        if (srcFac.isVlanIdRequired()) {
          String srcVlanId = (String) parameters.get(SPF_SRCVLAN);
          if (srcVlanId == null || srcVlanId.length() == 0 || srcVlanId.equals("4096")) {
            log.debug("vlan Id is required for provisioning to this source ETH facility: "
                + srcFac);
            throw new DracException(
                DracErrorConstants.LPCP_E3049_INVALID_SRC_VLANID, null);
          }
        }
      }

      // Map the abstract destination TNA to a physical facility.
      LpcpFacility dstFac = lpcp.getModelMgr().getFacilityForTNA(dstTNA);

      // For layer 2 (Ethernet) ports we need to see if a
      // VLAN ID is also needed in the request.
      if (dstFac != null && Layer.LAYER2.equals(dstFac.getLayer())) {
        if (dstFac.isVlanIdRequired()) {
          String dstVlanId = (String) parameters.get(SPF_DSTVLAN);
          if (dstVlanId == null || dstVlanId.length() == 0 || dstVlanId.equals("4096")) {
            log.debug("vlan Id is required for provisioning to this destination ETH facility: "
                + dstFac);
            throw new DracException(
                DracErrorConstants.LPCP_E3050_INVALID_DST_VLANID, null);
          }
        }
      }

      // Get out of here if we didn't find both the source and
      // destination facilities for this service request.
      // This can happen if a client has passed us an invalid
      // TNA string.
      if (srcFac == null || dstFac == null) {
        throw new Exception(
            "Source or destination facility could not be found '" + srcFac
                + "' '" + dstFac + "'.");
      }

      // If we are creating a service that starts and ends
      // at layer 1 then we want to make sure we do not
      // attempt to route that service across a layer 2 link.
      //
      // The general rule is that its ok to drop down a layer
      // but not to go up a layer. If the layer 1 service
      // happened to be carrying Ethernet traffic, then we
      // could go up to layer 2, but if we don't know what it
      // contains then we must stick at our layer or lower to
      // carry it.
      //
      // @TODO: The following exclusion of layer 2 links will
      // need to be revisited when we do more than single node
      // layer 2 routing combined with inter-layer routing
      // (Layer 2 -> Layer 1).
      //
      // For example, I start on a layer 2 Ethernet port off
      // of a Force10 switch then hit an EPL card on an OME
      // (multiple hops) where I want to dynamically turn up
      // a layer 1 GFP mapped service (WAN side) to a layer 1
      // port. To be totally honest, we may never want to
      // suport this.
      if (vcatRoutingOptionRequested || Layer.LAYER1.equals(srcFac.getLayer())
          || Layer.LAYER1.equals(dstFac.getLayer())) {
        // Service is vcat or carrying layer 1 traffic,
        // must not traverse layer 2 paths.
        log.debug("Requested service is vcat or starts/ends at layer 1, layer 2 paths must be removed from the graph prior to running shortest path.");
        lpcp.getRoutingEngine().excludeLayer2Edges(lpcp.getTopologyMgr());
      }

      // Path computation will store the discovered paths here.
      List<Map<SPF_KEYS, Object>> wrkPaths = new ArrayList<Map<SPF_KEYS, Object>>();
      List<Map<SPF_KEYS, Object>> prtPaths = new ArrayList<Map<SPF_KEYS, Object>>();

      // We need to handle 1+1 paths separately from
      // unprotected paths. Here we process services
      // requesting 1+1 paths.
      if (Lpcp.PRT_PATH1PLUS1.equalsIgnoreCase(protection)) {

        Map<SPF_KEYS, Object> workingPathMap = parameters;
        Map<SPF_KEYS, Object> protectionPathMap = cloneObjectMap(parameters);
        Map<SPF_KEYS, Object> map = null;
        List<Map<SPF_KEYS, Object>> maps = new ArrayList<Map<SPF_KEYS, Object>>();
        List<List<Map<SPF_KEYS, Object>>> paths = new ArrayList<List<Map<SPF_KEYS, Object>>>();
        Set<DracEdge> edges = null;

        // The working path map requires working path routing.
        workingPathMap.put(SPF_PATHTYPE, LpcpConstants.RT_PATHTYPE_WRK);

        // The protection path map requires protection path routing.
        protectionPathMap.put(SPF_PATHTYPE, LpcpConstants.RT_PATHTYPE_PRT);

        // Data to guide the path computation.
        maps.add(workingPathMap);
        maps.add(protectionPathMap);

        // Path computation results.
        paths.add(wrkPaths);
        paths.add(prtPaths);

        // We will process each of the paths (working + protection)
        // independently. However, we use path information from the
        // previous run to compute a diverse path.
        for (int i = 0; i < maps.size(); i++) {

          // Get service information for the first path.
          map = maps.get(i);

          // Get the path edges from the previous run.
          if (spf != null) {
            edges = (Set<DracEdge>) spf.get(SPF_JUNG_EDGELIST);
          }



          // Exclude all edges from previous run to get a simple diverse route.
          if (edges != null) {
            lpcp.getRoutingEngine().excludeEdges(edges);
          }

          // Determine if the service would like to route using VCAT
          // connections.
          LpcpFacility.VCAT_ROUTING_TYPE pathVcatRoutingType = lpcp
              .getRoutingEngine().getPathVcatRoutingType(srcTNA, dstTNA,
                  lpcp.getModelMgr());

          // If the service requests VCAT then we need to validate
          // the endpoints can support it. Also make sure that if
          // CCAT was requested then the endpoint is not configured
          // for VCAT only.
          if (vcatRoutingOptionRequested == true
              && pathVcatRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_CCAT) {
            throw new RoutingException(
                DracErrorConstants.LPCP_E3053_VCAT_REQUEST_CANNOT_BE_ROUTED);
          }
          else if (vcatRoutingOptionRequested == false
              && pathVcatRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_VCAT) {
            throw new RoutingException(
                DracErrorConstants.LPCP_E3054_CCAT_REQUEST_CANNOT_BE_ROUTED);
          }

          // Looks like the endpoints of the service are compatible
          // so lets get to routing.
          if (vcatRoutingOptionRequested) {
            try {
              spf = getVCATPath(srcTNA, dstTNA, lpcp.getModelMgr(), map);
            }
            catch (RoutingException re) {
              if (re.getMessage() != null
                  && re.getMessage().startsWith("No path found")) {
                if (LpcpConstants.RT_PATHTYPE_PRT.equals(map.get(SPF_PATHTYPE))) {
                  // Only the protection path could not be found, so change the
                  // message to
                  // reflect that
                  RoutingException protectionException = new RoutingException(
                      DracErrorConstants.LPCP_E3001_NO_PROTECTION_PATH_FOUND,
                      re);
                  throw protectionException;
                }
                // Re-throw the exception
                throw re;
              }
              // Re-throw the exception
              throw re;
            }

            if (LpcpConstants.RT_PATHTYPE_WRK.equals(map.get(SPF_PATHTYPE))) {
              paths.get(0).add(spf);
            }
            else {
              paths.get(1).add(spf);
            }

            // Do CCAT request
          }
          else {
            try {
              spf = getStandardPath(map);
            }
            catch (RoutingException re) {
              if (re.getMessage() != null
                  && re.getMessage().startsWith("No path found")) {
                if (LpcpConstants.RT_PATHTYPE_PRT.equals(map.get(SPF_PATHTYPE))) {
                  /*
                   * Only the protection path could not be found, so change the
                   * message to reflect that
                   */
                  RoutingException protectionException = new RoutingException(
                      DracErrorConstants.LPCP_E3001_NO_PROTECTION_PATH_FOUND,
                      re);
                  throw protectionException;
                }
                // Re-throw the exception
                throw re;
              }
              // Re-throw the exception
              throw re;
            }

            if (LpcpConstants.RT_PATHTYPE_WRK.equals(map.get(SPF_PATHTYPE))) {
              paths.get(0).add(spf);
            }
            else {
              paths.get(1).add(spf);
            }

          }

          /*
           * If SRLGs were specified, edges with the specified SRLGs need to be
           * removed from the routing graph.. We need to do this in the loop
           * since each call to getPath() in the routingEngine will reset the
           * excluded edges... this can be improved
           */
          if (srlg != null && !srlg.trim().equals("")) {
            log.debug("SRLGs found.  Excluding SRLG edges");
            srlgs = srlg.split(",");
            if (srlgs.length > 0) { // SRLGs specified - exclude these edges now
              lpcp.getRoutingEngine().excludeSRLG(srlgs, lpcp.getTopologyMgr());
            }
          }
        }

        parameters.put(SPF_WORKINGPATH, paths.get(0));
        parameters.put(SPF_PROTECTINGPATH, paths.get(1));



        String combinedPaths = lpcp.getRoutingEngine()
            .getCombinedWorkingProtectionPath((String) parameters.get(SPF_KEY),
                (String) paths.get(1).get(0).get(SPF_KEY));

        if (combinedPaths != null) {
          parameters.put(SPF_KEY, combinedPaths);
        }
      }
      // Normal, non-protected path computation
      else {
        parameters.put(SPF_PATHTYPE, LpcpConstants.RT_PATHTYPE_WRK);

        LpcpFacility.VCAT_ROUTING_TYPE pathVcatRoutingType = lpcp
            .getRoutingEngine().getPathVcatRoutingType(srcTNA, dstTNA,
                lpcp.getModelMgr());

        if (vcatRoutingOptionRequested
            && pathVcatRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_CCAT) {
          throw new RoutingException(
              DracErrorConstants.LPCP_E3053_VCAT_REQUEST_CANNOT_BE_ROUTED);
        }
        else if (!vcatRoutingOptionRequested
            && pathVcatRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_VCAT) {
          throw new RoutingException(
              DracErrorConstants.LPCP_E3054_CCAT_REQUEST_CANNOT_BE_ROUTED);
        }

        if (vcatRoutingOptionRequested) {
          spf = getVCATPath(srcTNA, dstTNA, lpcp.getModelMgr(), parameters);
        }
        else {
          spf = getStandardPath(parameters);
        }

        List<Map<SPF_KEYS, Object>> workingPaths = new ArrayList<Map<SPF_KEYS, Object>>();
        workingPaths.add(spf);
        parameters.put(SPF_WORKINGPATH, workingPaths);
      }

      /*
       * Convert the MB/s rate to an STS rate now. For VCAT, this should never
       * need to be done. Note that this rate is used for display purposes only
       * in the GUI
       */
      String convertedRate = Utility.convertMB2STS(rate);
      parameters.put(SPF_RATE, convertedRate);

    }
    catch (Exception e) {
      log.error("An exception occurred: rethrowing ", e);
      throw e;
    }
    finally {
      if (exclusion != null) {
        // Un-Populate the trackers with the overlapping bandwidth
        lpcp.getModelMgr().unmarkBandwidth(exclusion);
      }
      // Reset any excluded edges from the path calculation
      lpcp.getRoutingEngine().resetExcludedEdges();// topologyMgr.getGraph());
    }
  }

  private void checkRate(String rate) throws RoutingException {
    try {
      int rateInt = Integer.parseInt(rate);
      if (rateInt < 0) {
        log.error("Detected invalid rate: " + rateInt);
        throw new RoutingException(
            DracErrorConstants.LPCP_E3047_INVALID_RATE_SPECIFIED,
            new String[] { rate });
      }
    }
    catch (Exception e) {
      throw new RoutingException(
          DracErrorConstants.LPCP_E3047_INVALID_RATE_SPECIFIED,
          new String[] { rate }, e);
    }
  }

  private Map<SPF_KEYS, Object> cloneObjectMap(Map<SPF_KEYS, Object> map) {
    Map<SPF_KEYS, Object> clonedMap = new HashMap<SPF_KEYS, Object>();
    clonedMap.putAll(map);
    return clonedMap;
  }

  /**
   * Abandon hope all ye who enter here! This is pretty confusing code. Its made
   * worse because we take the map of parameters and add/remove/alter it as we
   * go through computing the path. In the case of 1:1 protection we do even
   * more alterations. We used to store String[]'s in the map (which we can't do
   * toString() on) for extra fun. I've converted the String[]'s into
   * List<String> in a basic fashion just so that params.toString() will be
   * useful to debug. Wayne July 3,2009. The List<String> were replaced by
   * CrossConnect objects instead, it gets better but slowly. Wayne Pitman Dec
   * 6, 2010.
   */
  private synchronized ScheduleResult doSchedule(Map<SPF_KEYS, Object> params,
      boolean queryOnly) throws Exception {
    log.debug("doSchedule " + params + " " + queryOnly);
    long timerStart = System.currentTimeMillis();
    String protectionType = (String) params.get(SPF_PROTECTION);
    String srcNeId = null;
    String dstNeId = null;

    // Resolve the source and dest ports (TNA) into their host NEs.
    srcNeId = lpcp.getModelMgr().getNeIdForTNA((String) params.get(SPF_SRCTNA));
    dstNeId = lpcp.getModelMgr().getNeIdForTNA((String) params.get(SPF_DSTTNA));

    // Add the source and dest NEs to the map of request information.
    params.put(SPF_SOURCEID, srcNeId);
    params.put(SPF_TARGETID, dstNeId);

    // Do some quick checks on the start and end time.
    validateTime(params, lpcp.getScheduler(), queryOnly);

    ScheduleResult result = null;
    try {
      /*
       * Convert the schedule start and end times from String to a Long
       * representing milliseconds since "the epoch".
       */
      long startTime = Long.parseLong((String) params.get(SPF_START_TIME));
      long endTime = Long.parseLong((String) params.get(SPF_END_TIME));

      log.debug("Start and end times of schedule are: " + startTime + "("
          + new Date(startTime) + ") and " + endTime + "(" + new Date(endTime)
          + ")");

      /*
       * We get a list of cross connects from all the schedules overlapping in
       * time with this schedule request. These are added to the exclusion list
       * for exclusion during path computation.
       */
      params.put(SPF_EXCLUDE,
          lpcp.getScheduler().getOverLappingSchedules(startTime, endTime));



      schedule(params);



      List<Map<SPF_KEYS, Object>> workingPaths = (List<Map<SPF_KEYS, Object>>) params
          .get(SPF_WORKINGPATH);
      List<Map<SPF_KEYS, Object>> protectionPaths = (List<Map<SPF_KEYS, Object>>) params
          .get(SPF_PROTECTINGPATH);

      if (!queryOnly) {
        List<CrossConnection> pathData = null;

        // This is where the callid is generated and applied to lightpath
        params
            .put(SPF_ID, lpcp.getLpcpUid() + "-" + System.currentTimeMillis());
        params.put(SPF_CALLID, params.get(SPF_ID));

        /*
         * public ServiceXml(State.SERVICE sStatus, String sId, String
         * sServiceId, String sActivationType, String sController, String
         * sScheduleId, String sScheduleName, String sVcat, long sStartTime,
         * long sEndTime, String sUser, String sBillingGroup, String sEmail,
         * String sPriority, int sMbs, String sRate, String sAend, String sZend)
         */

        State.SERVICE status = params.get(SPF_SERVICE_STATUS) == null ? State.SERVICE.CONFIRMATION_PENDING
            : State.SERVICE.valueOf((String) params.get(SPF_SERVICE_STATUS));
        ServiceXml newService = new ServiceXml(status,
            (String) params.get(SPF_ID), (String) params.get(SPF_SERVICEID),
            (String) params.get(SPF_ACTIVATION_TYPE),
            (String) params.get(SPF_CONTROLLER_ID),
            (String) params.get(SPF_SCHEDULE_KEY),
            (String) params.get(SPF_SCHEDULE_NAME),
            (String) params.get(SPF_VCATROUTING_OPTION),
            Long.parseLong((String) params.get(SPF_START_TIME)),
            Long.parseLong((String) params.get(SPF_END_TIME)),
            (String) params.get(SPF_USER), null,
            (String) params.get(SPF_EMAIL), "0",
            Integer.parseInt((String) params.get(SPF_MBS)),
            (String) params.get(SPF_RATE), (String) params.get(SPF_SOURCEID),
            (String) params.get(SPF_TARGETID));

        if (Lpcp.PRT_PATH1PLUS1.equalsIgnoreCase(protectionType)) {
          List<CrossConnection> onePlusOnePath = get1Plus1Path(srcNeId,
              dstNeId, workingPaths, protectionPaths);

          if (onePlusOnePath == null) {
            RoutingException re = new RoutingException(
                DracErrorConstants.LPCP_E3038_EXCEPTION_CALC_1PLUS1_PATH);
            throw re;
          }

          newService.addPath(XC_TYPE.MAIN, onePlusOnePath);

          // LPCP_PORT working and protecting paths created for 1+1
          pathData = (List<CrossConnection>) workingPaths.get(0).get(
              SPF_RT_PATH_DATA);
          // Add the working path
          log.debug("doSchedule LightPathScheduler::schedule::pathData is: "
              + pathData);
          newService.addPath(ServiceXml.XC_TYPE.WORKING, pathData);
          log.info("Creating path with path data: {}", pathData);
          // Add the protection path
          if (protectionPaths != null && protectionPaths.get(0) != null) {
            pathData = (List<CrossConnection>) protectionPaths.get(0).get(
                SPF_RT_PATH_DATA);
            newService.addPath(ServiceXml.XC_TYPE.PROTECTION, pathData);
            log.info("Creating portection path with path data: {}", pathData);
          }
          else {
            /* Strange place to be */
            log.error("doSchedule protectionPaths is: " + protectionPaths);
            if (protectionPaths != null) {
              log.error("doSchedule protectionPaths.get(0) is: "
                  + protectionPaths.get(0) + " size is: "
                  + protectionPaths.size());
            }
          }
        }
        else {
          pathData = (List<CrossConnection>) workingPaths.get(0).get(
              SPF_RT_PATH_DATA);
          log.debug("doSchedule LightPathScheduler::schedule::pathData is: "
              + pathData);
          newService.addPath(ServiceXml.XC_TYPE.MAIN, pathData);
        }

        log.debug("adding new ServiceXml INSTANCE to scheduler "
            + newService.toString());
        DbUtilityLpcpScheduler.INSTANCE.addNewService(newService);
        lpcp.getScheduler().notifyThreads();
      }

      if (params.get(SPF_KEY) == null) {
        log.error("doSchedule SPF_KEY is null, expected an exception by this point on the error path!");
        throw new Exception("doSchedule SPF_KEY is null");
      }

      /**
       * Regardless of if this is a query or a actual creation return the
       * ScheduleResult object. In the case of query only, the callid will be
       * empty and we the SPF_KEY will contain the proposed path. In the case of
       * creating a schedule we'll return the CALLID of the created schedule
       */
      result = new ScheduleResult((String) params.get(SPF_CALLID),
          (String) params.get(SPF_KEY));

    }
    catch (RoutingException re) {
      log.error("doSchedule Routing Exception occurred: ", re);
      throw re;
    }
    catch (Exception e) {
      log.error("doSchedule Exception creating schedule: ", e);
      throw e;
    }

    long timerEnd = System.currentTimeMillis();
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    log.debug("Total schedule time: " + sdf.format(timerEnd - timerStart)
        + " result=" + result);
    return result;
  }

  private void excludeService(String serviceId) {
    Set<DracEdge> jungEdges = new HashSet<DracEdge>();
    try {
      if (serviceId == null || "".equals(serviceId)) {
        log.debug("Cannot exclude null or empty serviceId, skipping");
        return;
      }
      ServiceXml service = lpcp.retrieveService(serviceId);

      if (service == null) {
        log.debug("Cannot find service with service id: " + serviceId);
        return;
      }


      List<CrossConnection> edges = service.getCrossConnections();

      /*
       * This ASSUMES that the edges are topologically ordered! which is true
       * unless your doing 1:1 protection in which case the protection path is
       * in the middle of the list
       */
      for (int i = 0; i < edges.size() - 1; i++) {
        CrossConnection edge = edges.get(i);
        CrossConnection nextEdge = edges.get(i + 1);

        // source or sink NE doesn't matter: nodal connection
        String thisEdgeNe = edge.getSourceNeId();
        String nextEdgeNe = nextEdge.getSourceNeId();

        lpcp.getTopologyMgr().findEdge(jungEdges, thisEdgeNe,
            edge.getSourcePortAid(), nextEdgeNe, edge.getTargetPortAid());
      }

      log.debug("Found " + jungEdges.size()
          + " edges to exclude for serviceId: " + serviceId);
      lpcp.getRoutingEngine().excludeEdges(jungEdges);
    }
    catch (Exception e) {
      log.error("Exception retrieving service:", e);
    }

  }

  private List<CrossConnection> get1Plus1Path(String srcNeid, String dstNeid,
      List<Map<SPF_KEYS, Object>> workingPaths,
      List<Map<SPF_KEYS, Object>> protectionPaths) throws RoutingException {

    // int rowIdx = 0;

    List<CrossConnection> temp = new ArrayList<CrossConnection>();
    List<CrossConnection> pathData = (List<CrossConnection>) workingPaths
        .get(0).get(SPF_RT_PATH_DATA);

    // Add working path
    for (CrossConnection row : pathData) {
      if (srcNeid.equalsIgnoreCase(row.getSourceNeId())
          || srcNeid.equalsIgnoreCase(row.getTargetNeId())
          || dstNeid.equalsIgnoreCase(row.getSourceNeId())
          || dstNeid.equalsIgnoreCase(row.getTargetNeId())) {
        if (srcNeid.equalsIgnoreCase(row.getSourceNeId())
            || srcNeid.equalsIgnoreCase(row.getTargetNeId())) {
          /*
           * Re-arrange ports at the head-end Connections need to be created
           * from the line side TO the UNI/endpoint. On the source NE,
           * connections are usually oriented from the UNI to the line so we
           * reverse them here.
           */
          final String onePoneSrcPort = row.getTargetPortAid();
          final String onePoneSrc = row.getTargetXcAid();
          final String onePoneSrcCh = row.getTargetChannel();

          final String onePoneTrgtPort = row.getSourcePortAid();
          final String onePoneTrgt = row.getSourceXcAid();
          final String onePoneTrgtCh = row.getSourceChannel();

          final String onePoneSwMate = getSwitchMate(protectionPaths, srcNeid,
              onePoneTrgt);

          /*
           * Create the bridge
           */
          Map<String, String> m = row.asMap();

          /*
           * Replace switchmate with UNI
           */
          m.put(CrossConnection.SWMATE_XC_AID, onePoneSwMate);

          /*
           * Replace source port with highspeed (INNI) working port
           */
          // wouter the swap of source and target is already done above
          // should be onePoneTrgtPort instead of onePoneSrcPort
          // RH: done, but is this still needed, wouter's comment says swap was
          // already done
          m.put(CrossConnection.TARGET_PORT_AID, onePoneTrgtPort);
          m.put(CrossConnection.TARGET_XC_AID, onePoneTrgt);
          m.put(CrossConnection.TARGET_CHANNEL, onePoneTrgtCh);

          /*
           * Replace destination port with highspeed (INNI) protection port
           * Original
           */
          // wouter the swap of source and target is already done above
          // should be onePoneSrcPort instead of onePoneDstPort
          // RH: done, but is this still needed, wouter's comment says swap was
          // already done
          m.put(CrossConnection.SOURCE_PORT_AID, onePoneSrcPort);
          m.put(CrossConnection.SOURCE_XC_AID, onePoneSrc);
          m.put(CrossConnection.SOURCE_CHANNEL, onePoneSrcCh);

          /*
           * remove the full aids so they are re-generated from the aids we just
           * switched and the channels.
           */
          m.remove(CrossConnection.TARGET_XC_AID);
          m.remove(CrossConnection.SOURCE_XC_AID);
          // fixEnds(m);
          // fixSwMate(m);
          row = new CrossConnection(m);
          // rowIdx++;
        }
        // Step 2: define tailend crossconnect
        // RH: if we use the new
        else {
          // rewind counter or we'll retrieve the tail end of the next head
          // end...
          // rowIdx--;
          Map<String, String> m = row.asMap();
          m.put(CrossConnection.SWMATE_XC_AID,
              getSwitchMate(protectionPaths, dstNeid, row.getTargetXcAid()));
          // fixEnds(m);
          // fixSwMate(m);
          row = new CrossConnection(m);
          // rowIdx++;
        }
      }
      // in any case the crossconnect (either pass thru of working path or
      // headend/tailend)
      // shall be added to collection temp
      temp.add(row);
    }

    if (srcNeid == null || dstNeid == null) {
      RoutingException re = new RoutingException(
          DracErrorConstants.LPCP_E3000_UNEXPECTED_1PLUS1_ERROR);
      throw re;
    }

    // Add protecting path
    pathData = (List<CrossConnection>) protectionPaths.get(0).get(
        SPF_RT_PATH_DATA);

    // Add pass thru connections from the protection path
    for (CrossConnection row : pathData) {
      if (srcNeid.equalsIgnoreCase(row.getSourceNeId())
          || srcNeid.equalsIgnoreCase(row.getTargetNeId())
          || dstNeid.equalsIgnoreCase(row.getSourceNeId())
          || dstNeid.equalsIgnoreCase(row.getTargetNeId())) {
        // Skip rows on the source and target NEs since we can only create the
        // connections on the endpoints once
        continue;
      }
      else {
        log.debug("protecting path: " + row);
        temp.add(row);
      }
    }

    return temp;
  }

  // New function which is generic for Headend and tailend switchmate
  private String getSwitchMate(List<Map<SPF_KEYS, Object>> protectionPaths,
      String neId, String onePoneTrgt) {
    String switchMate = null;
    CrossConnection row = null;

    List<CrossConnection> pathData = (List<CrossConnection>) protectionPaths
        .get(0).get(SPF_RT_PATH_DATA);

    // use onePoneDstPort (onePoneTrgtPort) to check together with NeId (on
    // source or destination),
    // if TRUE, the switchmate is found for the associated source or target port


    try {
      for (int i = 0; i < pathData.size(); i++) {
        if (neId.equalsIgnoreCase(pathData.get(i).getSourceNeId())
            || neId.equalsIgnoreCase(pathData.get(i).getTargetNeId())) {
          row = pathData.get(i);

          // matched target port with working --? switch mate found

          if (row.getTargetXcAid().equalsIgnoreCase(onePoneTrgt)) {
            switchMate = row.getSourceXcAid();
            if (switchMate != null) {
              break;
            }
          }
          // matched source port with working --? switch mate found

          if (row.getSourceXcAid().equalsIgnoreCase(onePoneTrgt)) {
            switchMate = row.getTargetXcAid();
            if (switchMate != null) {
              break;
            }
          }
        }
      }
    }
    catch (Exception e) {
      log.error("Exception determining switch mate:", e);
    }

    log.debug("SWITCHMATE for " + onePoneTrgt + " is " + switchMate);
    return switchMate;
  }

  private Map<SPF_KEYS, Object> getStandardPath(Map<SPF_KEYS, Object> parameters)
      throws RoutingException {
    String srcTNA = (String) parameters.get(SPF_SRCTNA);
    String dstTNA = (String) parameters.get(SPF_DSTTNA);
    String srcNeId = (String) parameters.get(SPF_SOURCEID);
    String dstNeId = (String) parameters.get(SPF_TARGETID);
    boolean fixedTimeslot = false;

    if (lpcp.getModelMgr().getFacilityForTNA(srcTNA) != null) {
      if (lpcp.getModelMgr().getFacilityForTNA(srcTNA).getType()
          .startsWith(FacilityConstants.LIM)
          || lpcp.getModelMgr().getFacilityForTNA(srcTNA).getType()
              .startsWith(FacilityConstants.CMD)) {
        fixedTimeslot = true;
      }
    }

    Map<SPF_KEYS, Object> result = lpcp.getRoutingEngine().getPath(parameters, lpcp.getTopologyMgr(), lpcp.getModelMgr(), true, fixedTimeslot);

    if (result != null) {
      if ((List<CrossConnection>) result.get(SPF_RT_PATH_DATA) != null) {
        // Search the pathlist for any excluded edges
        if (lpcp.getRoutingEngine().containsExcludedEdge(
            (List<DracEdge>) parameters.get(SPF_PATHKEY))) {
          // Recalculate the path without the excluded edge
          parameters.remove(SPF_RT_PATH_DATA);
          parameters.remove(SPF_PATHKEY);
          result = lpcp.getRoutingEngine().getPath(parameters, lpcp.getTopologyMgr(), lpcp.getModelMgr(), true, fixedTimeslot);
          if (lpcp.getRoutingEngine().containsExcludedEdge(
              (List<DracEdge>) parameters.get(SPF_PATHKEY))) {
            log.debug("Path still contains an excluded edge!");
          }
        }
      }
    }

    // Post process
    postProcess(srcTNA, dstTNA, srcNeId, dstNeId, result);
    return result;
  }

  /**
   * Compute a VCAT path through the network for the provided parameter` s.
   *
   * @param srcTNA
   *          Source port for the service request that has already been
   *          determined as VCAT compatible.
   * @param dstTNA
   *          Destination port for the service request that has already been
   *          determined as VCAT compatible.
   * @param modelMgr
   *          The network model to use during routing operations.
   * @param parameters
   *          Parameters specified for the service.
   * @return SPF path list.
   * @throws RoutingException
   *           An error has occurred during routing of the VCAT path.
   */
  private Map<SPF_KEYS, Object> getVCATPath(String srcTNA, String dstTNA,
      HierarchicalModel modelMgr, Map<SPF_KEYS, Object> parameters)
      throws RoutingException {

    Map<SPF_KEYS, Object> result = null;

    String srcNeId = (String) parameters.get(SPF_SOURCEID);
    String dstNeId = (String) parameters.get(SPF_TARGETID);
    String rate = (String) parameters.get(SPF_RATE);

    log.debug("Detected VCAT bandwidth request");
    log.debug("Requested VCAT connection base rate: " + rate);

    // Determine the current rate on the WAN facilities being used on
    // the source and destination enpoints.
    LpcpFacility srcFacility = modelMgr.getFacilityForTNA(srcTNA);
    LpcpFacility dstFacility = modelMgr.getFacilityForTNA(dstTNA);

    // @TODO: VCAT rate of STS-1-nV should be supported as well.
    // Hard coded here to STS-3-nV for now.
    int vcatWANRate = 150;

    // Make sure that if both endpoints provided are EPL/L2SS facilities (GFP
    // mapped Ethernet to SONET/SDH) that the configured STS VCAT rates
    // match.
    if (srcFacility.isEthWanEPL() && dstFacility.isEthWanEPL()) {

      // We only care if both facility rates are already set. If not
      // then we can set the rates we need.
      if (srcFacility.getRate() != null && dstFacility.getRate() != null) {
        if (!FacilityConstants.RATE_NONE.equals(srcFacility.getRate())
            && !FacilityConstants.RATE_NONE.equals(dstFacility.getRate())
            && !srcFacility.getRate().equals(dstFacility.getRate())) {
          log.error("Rate mismatch on selected ports");
          RoutingException re = new RoutingException(
              DracErrorConstants.LPCP_E3002_RATE_MISMATCH_SELECTED_PORTS,
              new String[] { srcFacility.getRate(), dstFacility.getRate() });
          throw re;
        }
      }
      else {
        log.error("Cannot determine src or dst configured rate: src rate="
            + srcFacility.getRate() + " dst rate=" + dstFacility.getRate());
      }
    }

    log.debug("Rate to be used for xcon request is: " + vcatWANRate);

    // Determine the number of path finding iterations based on the
    // number of VCAT services that need to be created to meet the requested
    // bandwidth.
    int iterations = Utility.getVCATIterations(
        Utility.convertMB2STS(Integer.toString(vcatWANRate)), rate);

    log.debug("vcat Iterations: " + iterations);

    // Error computing bandwidth so let us fail this VCAT path request.
    if (iterations == -1) {
      RoutingException re = new RoutingException(
          DracErrorConstants.LPCP_E3003_INVALID_WAN_RATE_REQUESTED,
          new String[] { Integer.toString(vcatWANRate), rate });
      throw re;
    }

    // We want to keep a list of the time each path computation iteration
    // took to complete.
    List<Long> results = new ArrayList<Long>();

    // We will store all the VCAT path iterations in here. We mark each
    // iteration as used in the model during path computation so that the
    // next iteration will not reuse previous iteration bandwidth. However,
    // we must unmark all this bandwidth at the end of all path compuation.
    List<CrossConnection> vcatExclusion = new ArrayList<CrossConnection>();

    // Convert the WAN rate into MB/s int
    try {
      // Overwrite the Ethernet Mb/s request rate in the map to match the
      // computed WAN rate which can be larger than the original rate
      // (rounded up to the nearest 150 Mb/s).
      parameters.put(SPF_RATE, Integer.toString(vcatWANRate));

      // Temp used to hold current calculated path for exclusion in next
      // iteration.
      List<CrossConnection> tempExclusion = null;
      boolean fixedTimeslot = false;

      // If the source port is a Layer 0 Channel Mux/Demux or a CPL Line
      // Interface Module then the timeslots are fixed (don't really
      // apply).
      if (srcFacility != null) {
        if (srcFacility.getType().startsWith(FacilityConstants.LIM)
            || srcFacility.getType().startsWith(FacilityConstants.CMD)) {
          fixedTimeslot = true;
        }
      }

      // When a set of VCAT member connections egress at Layer1 (i.e.
      // unassembled), ensure that the members have been provisioned in
      // contiguous blocks. This is a requirement from Surfnet to ease
      // management at hand-off point But, allow for a System Property
      // override that will permit a swiss-cheesed layer 1 vcat member
      // output.
      boolean allowNonContiguous = System.getProperty("allowNonContiguousVCAT",
          "false").equalsIgnoreCase("true");

      SonetTrackerI vcatTracker = null;
      if (!allowNonContiguous) {
        // Only one end can be an unassembled vcat.
        //
        // @TODO: What happens if this service request is for VCAT on
        // two layer 1 facilities? This seems to assume that one of
        // the facilities will be Ethernet and one a Layer 1, but never
        // both Layer 1. I believe the test should be modified for the
        // case where both ends are layer 1 (E-NNI or UNI).
        if (Layer.LAYER1.equals(srcFacility.getLayer())) {
          vcatTracker = ((SonetTrackerI) srcFacility.getTracker())
              .getBlankCopy();
        }
        else if (Layer.LAYER1.equals(dstFacility.getLayer())) {
          vcatTracker = ((SonetTrackerI) dstFacility.getTracker())
              .getBlankCopy();
        }
      }

      // Loop until we have reserved all VCAT paths need to satisfy the
      // requested bandwidth.
      for (int i = 0; i < iterations; i++) {
        // We like to track how long each path computation takes.
        long startTime = System.currentTimeMillis();
        long endTime = 0L;

        // FIXME: Overlapping paths, returns strange channels
        result = lpcp.getRoutingEngine().getPath(parameters, lpcp.getTopologyMgr(), modelMgr, false, fixedTimeslot);

        // Record the duration of the path computation.
        endTime = System.currentTimeMillis();
        results.add(Long.valueOf(endTime - startTime));

        // Now we need to exclude the path we just calculated from the
        // next iteration.
        if (result.get(SPF_CUR_PATH_LIST_KEY) != null) {
          tempExclusion = (List<CrossConnection>) result
              .get(SPF_CUR_PATH_LIST_KEY);

          // Save this path for later cleanup.
          vcatExclusion.addAll(tempExclusion);

          // Mark bandwidth associated with this path as used so that
          // it is excluded in the next iteration.
          modelMgr.markBandwidth(tempExclusion, vcatTracker);
        }

        // Tell the routing engine that the initial VCAT iteration is
        // already done. This is needed in the case that a specific
        // start channel was specified for the source/destination. It
        // is also used during EPL routing to make sure multiple
        // reservations are not allowed on a single EPL port.
        parameters.put(SPF_FIRST_VCAT_DONE, "true");
      }

      if (vcatTracker != null) {
        if (!vcatTracker.isContiguousUsage()) {
          log.debug("VCAT member connections are not contiguous on layer 1.");
          RoutingException re = new RoutingException(
              DracErrorConstants.LPCP_E3039_UNEXPECTED_ERR_VCAT_PATH_CALC,
              new String[] { "VCAT member connections are not contiguous on layer 1 endpoint." });
          throw re;
        }
      }




      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

      for (int i = 0; i < results.size(); i++) {
        log.debug("Iteration: " + i + ": "
            + sdf.format(new Date(results.get(i))));
      }



    }
    catch (RoutingException re) {
      log.error("Exception occurred during VCAT path calculation: ", re);
      throw re;
    }
    catch (Exception e) {
      RoutingException re = new RoutingException(
          DracErrorConstants.LPCP_E3039_UNEXPECTED_ERR_VCAT_PATH_CALC,
          new String[] { e.getMessage() }, e);
      log.error("Unexpected exception occurred during VCAT path calculation: ",
          e);
      throw re;
    }
    finally {
      // Unmark all bandwidth excluded during vcat path calculation

      lpcp.getRoutingEngine().resetExcludedEdges();
      modelMgr.unmarkBandwidth(vcatExclusion);
      vcatExclusion.clear();
    }

    // Post process
    postProcess(srcTNA, dstTNA, srcNeId, dstNeId, result);

    return result;
  }

  /**
   * PostProcess a computed path. A schedule can consist of many paths. A Vcat
   * and 1:1 protected schedule can generate 128 separate paths! Perform any
   * common post processing of an individual path here. In the past this method
   * did more, but its worth to keep around as a final inspection point.
   */
  private void postProcess(String srcTNA, String dstTNA, String sourceNeid,
      String targetNeid, Map<SPF_KEYS, Object> pathResult) {

    String protection = (String) pathResult.get(SPF_PROTECTION);
    String pathType = (String) pathResult.get(SPF_PATHTYPE);

    if (Lpcp.PRT_PATH1PLUS1.equalsIgnoreCase(protection)
        && LpcpConstants.RT_PATHTYPE_PRT.equalsIgnoreCase(pathType)) {
      List<CrossConnection> resultArray = (List<CrossConnection>) pathResult
          .get(SPF_RT_PATH_DATA);

      if (resultArray != null) {
        log.debug("resultArray size: " + resultArray.size());
        for (CrossConnection aRow : resultArray) {

          if (aRow.getSourceNeId().equalsIgnoreCase(sourceNeid)
              || aRow.getTargetNeId().equalsIgnoreCase(targetNeid)) {
            // Found a connection being created on the source or target
            log.error("Found a connection being created on the source or target in the 1+1 protection data! "
                + aRow);
          }
        }
      }
    }
  }

  private void validateTime(Map<SPF_KEYS, Object> parameters,
      LightPathScheduler scheduler, boolean queryOnly) throws Exception {
    long nextStartTime = scheduler.getNextStartTime();

    log.debug("currentTime: " + System.currentTimeMillis() + " nextStartTime: "
        + nextStartTime);

    long startTime = 0L;
    long endTime = 0L;

    try {
      startTime = Long.parseLong((String) parameters.get(SPF_START_TIME));
      endTime = Long.parseLong((String) parameters.get(SPF_END_TIME));
    }
    catch (Exception e) {
      throw new DracException(
          DracErrorConstants.LPCP_E3041_INVALID_SCHED_END_LESSTHAN_START, null,
          e);
    }

    // Check for start time in the past
    if (startTime < System.currentTimeMillis()
        && endTime > System.currentTimeMillis()) {
      // Change the schedule's start time to now.
      startTime = System.currentTimeMillis();
      // Update the data map with new startTime
      parameters.put(SPF_START_TIME, Long.toString(startTime));
      log.debug("Detected start time in the past.  startTime changed to: "
          + startTime);
    }
    else if (endTime < System.currentTimeMillis()) {
      // Job is completely in the past - invalid
      DracException se = new DracException(
          DracErrorConstants.LPCP_E3042_INVALID_SCHED_ENDTIME_IN_PAST, null);
      log.debug(
          "Invalid schedule detected.  startTime: " + startTime + " endTime: "
              + endTime + " currentTime: " + System.currentTimeMillis(), se);
      // Throw the exception
      throw se;
    }
    else if (endTime < startTime) {
      // Job's end time is earlier than the startTime - invalid
      DracException se = new DracException(
          DracErrorConstants.LPCP_E3041_INVALID_SCHED_END_LESSTHAN_START, null);
      log.debug("Invalid schedule.  Schedule start time is later than schedule end time.");
      throw se;
    }

    // Check for start time < nextStartTime
    if (startTime < nextStartTime && !queryOnly) {
      log.debug("Detected startTime < nextStartTime");
      log.debug("startTime: " + startTime + " nextStartTime: " + nextStartTime);
      scheduler.setNextStartTime(startTime);
    }
  }
}
