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
package com.nortel.appcore.app.drac.server.lpcp.routing;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.RoutingException;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
import com.nortel.appcore.app.drac.server.lpcp.common.Utility;
import com.nortel.appcore.app.drac.server.lpcp.trackers.BasicTracker;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility;
import com.nortel.appcore.app.drac.server.lpcp.trackers.TrackerConstraints;

/**
 * DRACHierarchical model The DRAC hierarchical model - Generates and maintains
 * the model that represents all of the NEs and ports in the LightPath Control
 * Plane domain. The model is hierarchical and has this hierarchy:
 * <p>
 * { neid1, { facaid1, Facility = { AID, ServerState, ..., BitBandwidthTracker }
 * facaid2, Facility = { AID, ServerState, ..., BitBandwidthTracker } ... } neid2, {
 * facaid1, Facility = {AID, ServerState, ..., BitBandwidthTracker } ... } ... }
 * <p>
 * Operations provided by this class: Build model Get model Mark bandwidth
 * Unmark bandwidth
 * 
 * @author adlee
 * @since 2005-12-15
 */
public enum HierarchicalModel {
  
    INSTANCE;
  
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, Map<String, LpcpFacility>> model = Collections.synchronizedMap(new TreeMap<String, Map<String, LpcpFacility>>(
            String.CASE_INSENSITIVE_ORDER));
    private final Map<String, LpcpFacility> tnaMap = Collections.synchronizedMap(new HashMap<String, LpcpFacility>());

    public synchronized void editFacility(String neid, String aid, String tna,
            String faclabel, String mtu, String srlg, String group, String cost,
            String metric2, String sigType, String constraints, String domain,
            String siteId) throws Exception {
        log.debug("editfacility invoked with neid: " + neid + " aid:" + aid
                + " tna:" + tna + " facLabel:" + faclabel + " mtu:" + mtu + " srlg:"
                + srlg + " group:" + group + " cost:" + cost + " metric2:" + metric2
                + " sigType:" + sigType + " constraints:" + constraints + " domain:"
                + domain + " siteId:" + siteId);

        Map<String, LpcpFacility> facMap = model.get(neid);
        if (facMap == null) {
            throw new Exception("Edit facility failed, cannot find neId:" + neid
                    + "\nmodel: " + model);
        }

        LpcpFacility facility = facMap.get(aid);
        log.debug("editFacility before edit: " + facility);

        if (facility == null) {
            throw new Exception("Edit facility failed, cannot find faciliy:" + aid
                    + "\nmodel: " + model);
        }

        // Only change attributes that are not null
        if (constraints != null) { // Set the constraints for the facility
            BigInteger constraintBits = new BigInteger(constraints);
            TrackerConstraints tConstraints = new TrackerConstraints(constraintBits);
            facility.setConstraints(tConstraints);
        }

        if (tna != null) {
            facility.setTNA(tna);
            // Replace the entry in the facility map using tna as the key
            // facMap.put(tna, facility);
        }
        if (faclabel != null) {
            facility.setFacLabel(faclabel);
        }
        if (mtu != null) {
            facility.setMTU(mtu);
        }
        if (srlg != null) {
            facility.setSRLG(srlg);
        }
        if (group != null) {
            facility.setGroup(group);
        }
        if (cost != null) {
            facility.setCost(cost);
        }
        if (metric2 != null) {
            facility.setMetric(metric2);
        }
        if (sigType != null) {
            facility.setSigType(sigType);
        }
        if (siteId != null) {
            facility.setSiteId(siteId);
        }
        if (domain != null) {
            facility.setDomain(domain);
        }

        // rebuild the entire tna map just in case they changed the TNA.
        buildTnaMap();

        log.debug("editFacility after edit: " + facility);
    }

    public String getAIDForTNA(String tna) throws Exception {
        LpcpFacility fac = tnaMap.get(tna);
        if (fac != null) {
            return fac.getAid();
        }

        throw new Exception("Unable to lookup AID for TNA " + tna);
    }

    public LpcpFacility getFacility(String neid, String aid) {
        Map<String, LpcpFacility> facMap = model.get(neid);
        if (facMap != null) {
            return facMap.get(aid);
        }

        return null;
    }

    public LpcpFacility getFacilityForTNA(String tna) {
        return tnaMap.get(tna);
    }

    public double getFacUtilisation(String neid, String facAID) throws Exception {
        log.debug("getFacUtilisation: " + neid + " " + facAID);
        LpcpFacility facility = model.get(neid).get(facAID);
        return facility.getTracker().getUtilisation();
    }

    /**
     * The model is a Map<neid, Map<aid, LpcpFacility>> where neId is the device
     * IEEE system id, and the aid is the port name of the facility.
     */
    public Map<String, Map<String, LpcpFacility>> getModel() {
        return model;
    }

    public String getNeIdForTNA(String tna) throws RoutingException {
        if (tnaMap.get(tna) != null) {
            return tnaMap.get(tna).getNeId();
        }

        log.error("getNeIdForTNA cannot find facility for tna: " + tna
                + " known TNAs are " + tnaMap.keySet(), new Exception("Trace back"));
        RoutingException re = new RoutingException(
                DracErrorConstants.LPCP_E3004_CANNOT_FIND_FACILITY_FOR_TNA,
                new String[]{tna});
        throw re;
    }

    public String getTidForNeId(String neid) {
        String tid = neid;
        if (TopologyManager.INSTANCE.getVertex(neid) != null) {
            tid = TopologyManager.INSTANCE.getVertex(neid).getLabel();
        }
        return tid;
    }

    public synchronized void markBandwidth(List<CrossConnection> xcons) {
        markBandwidth(xcons, null);
    }

    /**
     * BitBandwidthTrackerI externalTracker: A stand-alone tracker to be evaluated
     * against the list of xcons passed in. It can be used to evaluate
     * provisioning conditions independently of the trackers set normally in the
     * network model. See additional notes below.
     */
    public synchronized void markBandwidth(List<CrossConnection> xcons,
            BasicTracker externalTracker) {
        if (xcons == null) {
            log.error("null bandwith provided to markBandwidth");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (CrossConnection con : xcons) {
            sb.append(con.toString());
            sb.append("\n");
        }
        log.debug("markBandwidth: Excluding " + sb.toString());

        for (CrossConnection conn : xcons) {
            try {
                String source = conn.getSourceNeId();
                String target = conn.getTargetNeId();
                String srcPort = conn.getSourcePortAid();
                String dstPort = conn.getTargetPortAid();

                String rate = conn.getRate();
                String srcChan = conn.getSourceChannel();
                String dstChan = conn.getTargetChannel();
                int srcChanInt = Integer.parseInt(srcChan);
                int dstChanInt = Integer.parseInt(dstChan);

                // Fix 1+1 ROUTING!
                String swMatePort = conn.getSwMatePortAid();
                String swMateChan = conn.getSwMateChannel();
                int swMateChanInt = -1;
                if (swMateChan != null) {
                    swMateChanInt = Integer.parseInt(swMateChan);
                }

                if (conn.isDracLoopback()) {
                    log.debug("Not populating marking bandwidth for connection with ID: "
                            + conn.getId());
                    continue;
                }

                StringBuilder builder = new StringBuilder();
                builder.append("markBandwidth::Populating: source: " + source
                        + " srcPort: " + srcPort + "target: " + target + " dstPort : "
                        + dstPort + " rate: " + rate + "srcChan: " + srcChanInt
                        + " dstChan: " + dstChanInt);
                builder.append(" swMatePort: " + swMatePort + " swMateChan: "
                        + swMateChanInt);
                log.debug(builder.toString());

                // rate here is a string like "STS1" "STS3C" etc.. must convert it to a
                // Mb/s rate
                rate = Utility.convertSTS2Mb(rate);

                /*
                 * NOTE on SWMATE: The conn fields provide for difference src and dst
                 * NEs, but this is for a xconn ... never on different NEs. THe
                 * switchmate argument is provided simply as a channel AID
                 * (type-shelf-slot-port-chan). It will be ASSUMED to reside on the same
                 * source NE.
                 */

                Map<String, LpcpFacility> facMapSrc = model.get(source);
                Map<String, LpcpFacility> facMapDst = model.get(target);

                if (facMapSrc != null && facMapDst != null) {
                    LpcpFacility srcFacility = facMapSrc.get(srcPort);
                    LpcpFacility dstFacility = facMapDst.get(dstPort);
                    LpcpFacility swMateFacility = null;
                    // NOTE on SWMATE
                    if (swMatePort != null) {
                        swMateFacility = facMapSrc.get(swMatePort);
                    }

                    /**
                     * Unfortunately this gets tricky and does not present a clean model
                     * yet (without significant rework to the L2 object modeling in DRAC).
                     * <p>
                     * (1) For EPL cards, we will always use the tracker based on the ETH
                     * aid.
                     * <p>
                     * (2) For L2SS, the ETH-based tracker will be used in cases in which
                     * the WANs are created on the fly.
                     * <p>
                     * (3) For services provisioned directly to the L2SS backplane WANs,
                     * we'll use the WAN-aid-based L2SS tracker.
                     */
                    // if (!srcFacility.isL2SS())
                    // {
                    // // (1) EPL
                    // srcPort = Utility.convertWAN2ETH(srcPort);
                    // srcFacility = facMapSrc.get(srcPort);
                    // }
                    //
                    // if (!dstFacility.isL2SS())
                    // {
                    // // (1) EPL
                    // dstPort = Utility.convertWAN2ETH(dstPort);
                    // dstFacility = facMapDst.get(dstPort);
                    // }
                    //
                    // if (swMateFacility != null && !swMateFacility.isL2SS())
                    // {
                    // // (1) EPL
                    // swMatePort = Utility.convertWAN2ETH(swMatePort);
                    // swMateFacility = facMapSrc.get(swMatePort);
                    // }
                    boolean gotBW = false;

                    if (srcFacility != null || dstFacility != null) {
                        BasicTracker srcTracker = srcFacility != null ? srcFacility.getTracker() : null;
                        BasicTracker dstTracker = dstFacility != null ? dstFacility.getTracker() : null;
                        BasicTracker swMateTracker = swMateFacility != null ? swMateFacility.getTracker() : null;

                        // int trackerRate = Utility.convertStringRateToInt(rate); //
                        // OpticalFacilityTracker.

                        {
                            // gotBW = srcTracker != null ? srcTracker.takeBandwidth(conID,
                            // srcChanInt,
                            // trackerRate) : false;
                            gotBW = srcTracker != null ? srcTracker.takeBandwidth(conn)
                                    : false;

                            if (!gotBW && !(srcTracker == null)) {
                                log.debug("Bandwidth overlap detected: srcFacility: "
                                        + srcFacility + " srcChan: " + srcChan + " rate: " + rate);
                            } else if (!gotBW && srcTracker == null) {
                                log.debug("Null srcTracker detected: srcFacility: "
                                        + srcFacility + " srcChan: " + srcChan + " rate: " + rate);
                            }
                        }

                        {
                            // gotBW = dstTracker != null ? dstTracker.takeBandwidth(conID,
                            // dstChanInt,
                            // trackerRate) : null;
                            gotBW = dstTracker != null ? dstTracker.takeBandwidth(conn)
                                    : false;

                            if (!gotBW && !(dstTracker == null)) {
                                log.debug("Bandwidth overlap detected: dstFacility: "
                                        + dstFacility + " dstChan: " + dstChan + " rate: " + rate);
                            } else if (!gotBW && dstTracker == null) {
                                log.debug("Null dstTracker detected: srcFacility: "
                                        + srcFacility + " srcChan: " + srcChan + " rate: " + rate);
                            }
                        }

                        // SWMATE TRACKER
                        if (swMatePort != null) {
                            gotBW = swMateTracker != null ? swMateTracker.takeBandwidth(conn)
                                    : false;

                            if (!gotBW && !(swMateTracker == null)) {
                                log.debug("Bandwidth overlap detected: swMateFacility: "
                                        + swMateFacility + " swMateChan: " + swMateChan + " rate: "
                                        + rate);
                            } else if (!gotBW && swMateTracker == null) {
                                log.debug("Null swMateFacility detected: swMateFacility: "
                                        + swMateFacility + " swMateChan: " + swMateChan + " rate: "
                                        + rate);
                            }
                        }

                        /**
                         * EXTERNAL TRACKER Initial use here is to track the provisioning of
                         * vcat member connections across a layer1 ENNI/UNI handoff ... for
                         * the purpose of evaluating whether or not the members are
                         * provisioned contiguously. The existing network model src/dst
                         * tracker for the layer1 could have been used given that (via
                         * Initial use here is to track the provisioning of vcat member
                         * connections across a layer1 ENNI/UNI handoff ... for the purpose
                         * of evaluating whether or not the members are provisioned
                         * contiguously. The existing network model src/dst tracker for the
                         * layer1 could have been used given that (via
                         * markingExclusionsForMembersOfSameService) it would hold only the
                         * vcat members (other service exclusions not included), but it was
                         * more desirable to maintain a clear separation between the network
                         * model tracker and // and one that is used for specific cases
                         * given a set of xcons.
                         */
                        if (externalTracker != null) {
                            if (source != null && source.equals(externalTracker.getNeid())
                                    && srcPort != null
                                    && srcPort.equals(externalTracker.getAid())) {
                                externalTracker.takeBandwidth(conn);
                            } else if (target != null
                                    && target.equals(externalTracker.getNeid())
                                    && dstPort != null
                                    && dstPort.equals(externalTracker.getAid())) {
                                externalTracker.takeBandwidth(conn);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("markBandwidth failed to mark " + conn);
            }
        }
    }

    public synchronized void parseAndAddToModel(List<Facility> facList)
            throws Exception {
        if (facList == null) {
            log.error("parseAndAddToModel given a null facility list");
            return;
        }

        List<NetworkElementHolder> nhList = DbNetworkElement.INSTANCE.retrieve(null);

        for (Facility fac : facList) {
            NetworkElementHolder h = null;
            String neid = fac.getNeId();

            /*
             * Strange code, if you think that all the facilities passed in belong to
             * the same NE, we could avoid looking up the NE each time ! Revisit how
             * this is called
             */
            for (NetworkElementHolder nh : nhList) {
                if (neid.equals(nh.getId())) {
                    h = nh;
                    break;
                }
            }

            addFacilityToModel(neid, makeLpcpFacilityFromFacility(h, fac));
        }

        buildTnaMap();
    }

    public synchronized void parseAndAddToModel(Map<String, String> event,
            String neid) throws Exception {
        NetworkElementHolder h = null;
        List<NetworkElementHolder> nhList = DbNetworkElement.INSTANCE.retrieve(null);

        for (NetworkElementHolder nh : nhList) {
            if (neid.equals(nh.getId())) {
                h = nh;
                break;
            }
        }

        if (h == null) {
            log.error("Failed to find the matching NE for " + neid + " from the db !");
        }

        Map<String, String> filter = new HashMap<String, String>();
        filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, neid);
        filter.put(DbKeys.NetworkElementFacilityCols.AID,
                event.get(FacilityConstants.AID_ATTR));
        List<Facility> facList = DbNetworkElementFacility.INSTANCE.retrieveFacilities(filter);

        Facility fac = null;
        if (facList != null && facList.size() == 1) {
            fac = facList.toArray(new Facility[0])[0];
        } else {
            log.error("parseAndAddToModel - received event with unknown aid.");
            return;
        }

        LpcpFacility lpcpFac = makeLpcpFacilityFromFacility(h, fac);
        addFacilityToModel(neid, lpcpFac);

    }

    /**
     * populateTrackers should only be called for xcons that are generated outside
     * of DRAC This ignores any DRAC constraints that may be set!
     */
    public synchronized boolean populateTrackers(List<CrossConnection> xcons)
            throws Exception {
        boolean done = false;

        if (xcons == null) {
            return false;
        }

        for (CrossConnection c : xcons) {
            

            // Do not populate our model with DRAC connections and DRAC loopback
            // connections
            if (c.isDracConnection()) {
                log.debug("populateTrackers: Not populating model with OpenDRAC connection: "
                        + c.getId());
                continue;
            }

            // rate = c.getRate();
            String srcNeid = c.getSourceNeId();
            String dstNeid = c.getTargetNeId();
            String swmateNeid = c.getSwMateNeId();
            String swmatePortAid = c.getSwMatePortAid();

            // String sourcePortAid = Utility.convertWAN2ETH(c.getSourcePortAid());
            // String targetPortAid = Utility.convertWAN2ETH(c.getTargetPortAid());
            // if (swmatePortAid != null)
            // {
            // swmatePortAid = Utility.convertWAN2ETH(swmatePortAid);
            // }
            String sourcePortAid = c.getSourcePortAid();
            String targetPortAid = c.getTargetPortAid();

            /*
             * HACKS >>>>> src and dst fac AIDs of WAN will all be converted to ETH
             * for the purposes of bandwidth tracker population since it is the ETH
             * port that keeps track of the BW used. OC AIDs should be untouched.
             */

            NeType neType = TopologyManager.INSTANCE.getNeType(srcNeid);
            sourcePortAid = Utility.getNormalisedAID(sourcePortAid, neType);
            targetPortAid = Utility.getNormalisedAID(targetPortAid, neType);
            if (swmatePortAid != null) {
                swmatePortAid = Utility.getNormalisedAID(swmatePortAid, neType);
            }

            

            Map<String, LpcpFacility> facMapSrc = model.get(srcNeid);
            Map<String, LpcpFacility> facMapDst = model.get(dstNeid);

            if (facMapSrc != null && facMapDst != null) {
                LpcpFacility srcFacility = facMapSrc.get(sourcePortAid);
                LpcpFacility dstFacility = facMapDst.get(targetPortAid);
                LpcpFacility swmateFacility = null;

                if (swmateNeid != null && swmatePortAid != null) {
                    swmateFacility = model.get(swmateNeid).get(swmatePortAid);
                }

                if (srcFacility != null && dstFacility != null) {
                    BasicTracker srcTracker = srcFacility.getTracker();
                    BasicTracker dstTracker = dstFacility.getTracker();
                    BasicTracker swmateTracker = null;

                    if (swmateFacility != null) {
                        swmateTracker = swmateFacility.getTracker();
                    }

                    if (srcTracker == null || dstTracker == null) {
                        log.error("populateTrackers: >>>>>FATAL ERROR, srcTracker: "
                                + srcTracker + " dstTracker: " + dstTracker
                                + "\n connection skipped: " + c);
                        continue;
                    }

                    boolean bwdone = false;
                    // takeBandwidth channels start from 1

                    // SOURCE
                    bwdone = srcTracker.takeBandwidth(c);// srcFreeChannel

                    if (!bwdone) {
                        log.error("populateTrackers: Could not allocate bandwidth on tracker: "
                                + srcTracker.toString() + " conn:" + c);
                    }

                    // TARGET
                    bwdone = dstTracker.takeBandwidth(c);

                    if (!bwdone) {
                        log.error("populateTrackers: Could not allocate bandwidth on tracker: "
                                + dstTracker.toString() + " conn:" + c);
                    }

                    // SWMATE
                    bwdone = swmateTracker != null ? swmateTracker.takeBandwidth(c)
                            : true;
                    if (!bwdone) {
                        log.error("populateTrackers: Could not allocate bandwidth on tracker: "
                                + swmateTracker.toString() + " conn:" + c);
                    }

                } else {
                    log.debug("populateTrackers:  srcFacility: " + srcFacility
                            + " dstFacility: " + dstFacility);
                }
            } else {
                log.error("populateTrackers: FATAL ERROR: facMapSrc: " + facMapSrc
                        + " facMapDst: " + facMapDst);
            }

        }

        return done;
    }

    public synchronized void removeFacilitiesForNe(String ieee) {
        Map<String, LpcpFacility> facilityMap = model.get(ieee);
        if (facilityMap == null) {
            log.debug("Cannot delete facility from model, becase model is already empty for "
                    + ieee);
            return;
        }

        log.debug("The following facilities will be removed from the model: "
                + facilityMap);
        facilityMap.clear();
        model.remove(ieee);
        // rebuild the TNA map
        buildTnaMap();
    }

    public synchronized void removeFacilityFromModel(String neid, String aid) {
        Map<String, LpcpFacility> facilitiesMap = model.get(neid);

        if (facilitiesMap == null) {
            // ne already gone
            return;
        }

        if (aid == null) {
            log.error("Cannot delete facility from model!");
        }

        if (facilitiesMap.get(aid) != null) {
            log.debug("Removing facility with neid:" + neid + " aid:" + aid);
            facilitiesMap.remove(aid);
        }
        // rebuild the TNA map
        buildTnaMap();
    }

    public synchronized boolean unmarkBandwidth(List<CrossConnection> xcons) {
        boolean done = false;

        if (xcons == null) {
            log.error("Null list provided to unmarkBandwith!");
            return done;
        }

        for (CrossConnection conn : xcons) {
            try {
                // conID = conn.get(CONN_LABEL);
                String source = conn.getSourceNeId();
                String target = conn.getTargetNeId();
                String srcPort = conn.getSourcePortAid();
                String dstPort = conn.getTargetPortAid();
                String swMatePort = conn.getSwMatePortAid();
                log.debug("unmarkBandwidth::Un-Populating: "
                        + conn);

                // srcPort = Utility.convertWAN2ETH(srcPort);
                // dstPort = Utility.convertWAN2ETH(dstPort);

                // rate here is a string like "STS1" "STS3C" etc.. must convert it to a
                // Mb/s rate
                // rate = Utility.convertSTS2Mb(rate);

                // see NOTE on SWMATE:
                Map<String, LpcpFacility> facMapSrc = model.get(source);
                Map<String, LpcpFacility> facMapDst = model.get(target);
                if (facMapSrc != null && facMapDst != null) {

                    LpcpFacility srcFacility = facMapSrc.get(srcPort);
                    LpcpFacility dstFacility = facMapDst.get(dstPort);
                    LpcpFacility swMateFacility = null;
                    // NOTE on SWMATE
                    if (swMatePort != null) {
                        swMateFacility = facMapSrc.get(swMatePort);
                    }

                    // if (!srcFacility.isL2SS())
                    // {
                    // // (1) EPL
                    // srcPort = Utility.convertWAN2ETH(srcPort);
                    // srcFacility = facMapSrc.get(srcPort);
                    // }
                    //
                    // if (!dstFacility.isL2SS())
                    // {
                    // // (1) EPL
                    // dstPort = Utility.convertWAN2ETH(dstPort);
                    // dstFacility = facMapDst.get(dstPort);
                    // }
                    //
                    // if (swMateFacility != null && !swMateFacility.isL2SS())
                    // {
                    // // (1) EPL
                    // swMatePort = Utility.convertWAN2ETH(swMatePort);
                    // swMateFacility = facMapSrc.get(swMatePort);
                    // }

                    boolean freed = false;

                    if (srcFacility != null && dstFacility != null) {
                        BasicTracker srcTracker = srcFacility.getTracker();
                        BasicTracker dstTracker = dstFacility.getTracker();
                        BasicTracker swMateTracker = swMateFacility != null ? swMateFacility.getTracker() : null;

                        // SOURCE TRACKER
                        log.debug("unmarkBandwidth srcTracker before: "
                                + srcFacility.toString());

                        freed = srcTracker.giveBandwidth(conn);

                        log.debug("unmarkBandwidth srcTracker after: "
                                + srcFacility.toString());

                        if (!freed) {
                            log.error("unmarkBandwidth:: Unable to giveBandwidth to srcFacility "
                                    + srcFacility + " conn:" + conn);
                        }

                        // DESTINATION TRACKER
                        log.debug("unmarkBandwidth dstTracker before: "
                                + dstFacility.toString());

                        freed = dstTracker.giveBandwidth(conn);
                        log.debug("unmarkBandwidth dstTracker after: "
                                + dstFacility.toString());
                        if (!freed) {
                            log.error("unmarkBandwidth:: Unable to giveBandwidth to dstFacility "
                                    + dstFacility + " conn:" + conn);
                        }

                        if (swMateFacility != null && swMateTracker != null) {
                            // SWMATE
                            log.debug("unmarkBandwidth swMateTracker before: "
                                    + swMateFacility);
                            freed = swMateTracker.giveBandwidth(conn);
                            log.debug("unmarkBandwidth swMateTracker after: " + swMateFacility);
                            if (!freed) {
                                log.error("unmarkBandwidth:: Unable to giveBandwidth to swMateTracker: "
                                        + swMateTracker + " conn:" + conn);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("unmarkBandwidth failed for conn: " + conn, e);
            }
        }

        return done;
    }

    private void addFacilityToModel(String neid, LpcpFacility facility) {
        if (facility == null) {
            log.error("Cannot add null facility to model!");
            return;
        }

        Map<String, LpcpFacility> facilitiesMap = model.get(neid);
        if (facilitiesMap == null) {
            // first facility for a new NE
            model.put(neid, new HashMap<String, LpcpFacility>());
            facilitiesMap = model.get(neid);
        }

        if (facilitiesMap.get(facility.getAid()) != null) {
            log.debug("Replacing facility with aid: " + facility.getAid());
        }
        // facilitiesMap.put(facility.getTNA(), facility);
		/*
         * Old hack used to store facility with key as both TNA and AID. Now we only
         * store AID and use the tnamap to lookup by TNA.
         */
        facilitiesMap.put(facility.getAid(), facility);

    }

    /**
     * buildTNAMap every time the model changes rebuild the tna map to keep it in
     * sync
     */
    private void buildTnaMap() {
        synchronized (tnaMap) {
            tnaMap.clear();
            for (String neid : model.keySet()) {
                for (LpcpFacility fac : model.get(neid).values()) {
                    if (fac.getTNA() != null && !"N/A".equals(fac.getTNA())) {
                        tnaMap.put(fac.getTNA(), fac);
                    }
                }
            }
        }
    }

    private LpcpFacility makeLpcpFacilityFromFacility(NetworkElementHolder h,
            Facility fac) {
        Map<String, String> extAttr = new HashMap<String, String>();
        extAttr.put(FacilityConstants.SRLG_ATTR, fac.getSrlg());
        extAttr.put(FacilityConstants.COST_ATTR, fac.getConstraint());
        extAttr.put(FacilityConstants.METRIC_ATTR, fac.getMetric2());

        if (fac.getVcat() != null) {
            extAttr.put(FacilityConstants.VCAT_ATTR, fac.getVcat());
        }

        // extAttr.put(FacilityConstants.ACTUALUNIT_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.ACTUALUNIT_ATTR));
        extAttr.put(FacilityConstants.ACTUALUNIT_ATTR,
                fac.get(FacilityConstants.ACTUALUNIT_ATTR));

        // extAttr.put(FacilityConstants.LCAS_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.LCAS_ATTR));
        extAttr.put(FacilityConstants.LCAS_ATTR,
                fac.get(FacilityConstants.LCAS_ATTR));

        // extAttr.put(FacilityConstants.MAPPING_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.MAPPING_ATTR));
        extAttr.put(FacilityConstants.MAPPING_ATTR,
                fac.get(FacilityConstants.MAPPING_ATTR));

        // extAttr.put(FacilityConstants.MODE_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.MODE_ATTR));
        extAttr.put(FacilityConstants.MODE_ATTR,
                fac.get(FacilityConstants.MODE_ATTR));

        // extAttr.put(FacilityConstants.PROVUNIT_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.PROVUNIT_ATTR));
        extAttr.put(FacilityConstants.PROVUNIT_ATTR,
                fac.get(FacilityConstants.PROVUNIT_ATTR));

        // extAttr.put(FacilityConstants.MTU_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.MTU_ATTR));
        extAttr.put(FacilityConstants.MTU_ATTR, fac.getMtu());

        // extAttr.put(FacilityConstants.PHYSICALADDRESS_ATTR, layerElement
        // .getAttributeValue(FacilityConstants.PHYSICALADDRESS_ATTR));
        extAttr.put(FacilityConstants.PHYSICALADDRESS_ATTR,
                fac.get(FacilityConstants.PHYSICALADDRESS_ATTR));

        // extAttr.put(FacilityConstants.SPEED_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.SPEED_ATTR));
        extAttr.put(FacilityConstants.SPEED_ATTR,
                fac.get(FacilityConstants.SPEED_ATTR));

        // extAttr.put(FacilityConstants.INGRESSIP_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.INGRESSIP_ATTR));
        extAttr.put(FacilityConstants.INGRESSIP_ATTR,
                fac.get(FacilityConstants.INGRESSIP_ATTR));

        // extAttr.put(FacilityConstants.MEP_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.MEP_ATTR));
        extAttr.put(FacilityConstants.MEP_ATTR, fac.get(FacilityConstants.MEP_ATTR));

        // extAttr.put(FacilityConstants.WAVELENGTH_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.WAVELENGTH_ATTR));
        extAttr.put(FacilityConstants.WAVELENGTH_ATTR, fac.getWavelength());

        // extAttr.put(FacilityConstants.SITE_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.SITE_ATTR));
        extAttr.put(FacilityConstants.SITE_ATTR,
                fac.get(FacilityConstants.SITE_ATTR));

        // extAttr.put(FacilityConstants.DOMAIN_ATTR,
        // layerElement.getAttributeValue(FacilityConstants.DOMAIN_ATTR));
        extAttr.put(FacilityConstants.DOMAIN_ATTR, fac.getDomain());

        // extAttr.put(FacilityConstants.IS_L2SS_FACILITY,
        // layerElement.getAttributeValue(FacilityConstants.IS_L2SS_FACILITY));
        extAttr.put(FacilityConstants.IS_L2SS_FACILITY,
                fac.get(FacilityConstants.IS_L2SS_FACILITY));

        // if (layerElement.getAttributeValue("InUseVlans") != null)
        // {
        // extAttr.put("InUseVlans", layerElement.getAttributeValue("InUseVlans"));
        // }
        if (fac.get("InUseVlans") != null) {
            extAttr.put("InUseVlans", fac.get("InUseVlans"));
        }

        TrackerConstraints tc = new TrackerConstraints();
        if (fac.getConstraint() != null) {
            try {
                tc = new TrackerConstraints(new BigInteger(fac.getConstraint()));
            } catch (NumberFormatException nfe) {
                log.error(
                        "Facility constraint could not be converted to a bigInteger! "
                        + fac.getConstraint(), nfe);
            }
        }

        return new LpcpFacility(h, fac.getAid(), fac.getShelf(), fac.getSlot(),
                fac.getPort(), fac.getType(), fac.getPrimaryState(), fac.getTna(),
                fac.getUserLabel(), fac.getSigType(), fac.getLayer(), fac.getType(),
                fac.getApsid(), h.getMode(), extAttr, tc);
    }
}
