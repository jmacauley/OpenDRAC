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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.server.lpcp.trackers.BasicTracker;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility;

/**
 * DRACUserDataumNumberEdgeValue is a transformer used by the jung
 * DijkstraShortest path algorithm to convert an graph edge into an appropriate
 * cost of using that edge. Normally the cost of a edge is the configured cost,
 * however the cost can be a different metric or infinity if the edge has no
 * free bandwidth on it (which causes dijkstra to look for a cheaper path).
 *
 * @TODO Wayne Aug 2010: Some of the error handling strikes me as wrong we end
 * up returning the default weight under error conditions, that might not be the
 * right thing to do...
 *
 * @author adlee
 * @since 2005-11-08
 */
public final class UserDateEdgeValue implements Transformer<DracEdge, Number> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private boolean useMetric2;
    private boolean useCost;
    private final Map<String, Map<String, LpcpFacility>> model;
    private double costLimit = -1.0;
    private double metricLimit = -1.0;
    private final int trackerRate;
    private static final double INFINITY = 999999999.9;
    private DracVertex src;
    private DracVertex dst;
    private int fixedTimeslot = -1;
    private final Set<DracEdge> excludedEdges = new HashSet<DracEdge>();
    private Map<SPF_KEYS, Object> parameters;

    public UserDateEdgeValue(int rate,
            Map<String, Map<String, LpcpFacility>> theModel,
            Map<SPF_KEYS, Object> parameters) {
        // super("WEIGHT");
        trackerRate = rate;
        model = theModel;
        this.parameters = parameters;
    }

    public Set<DracEdge> getAndClearExcludedEdges() {
        Set<DracEdge> r = new HashSet<DracEdge>(excludedEdges);
        excludedEdges.clear();
        return r;
    }

    public Number getNumber(DracEdge e) {
        double workingWeight = 0.0;
        double dblCost = 0.0;
        double dblMetric2 = 0.0;
        double dblWeight = 0.0;

        if (e == null) {
            log.error("FATAL ERROR: edge is null!", new Exception("Stacktrace"));
        }

        src = e.getSource();
        dst = e.getTarget();

        try {
            // Calculate the dynamic weight
            if (e.getWeight() != null) {
                dblWeight = e.getWeight().doubleValue();
            }
            if (e.getCost() != null) {
                dblCost = Double.parseDouble(e.getCost());
            }
            if (e.getMetric() != null) {
                dblMetric2 = Double.parseDouble(e.getMetric());
            }
            // srlg = e.getSrlg();
            workingWeight = dblWeight;

            if (useCost && dblCost > costLimit) {
                return excludeEdge(e, workingWeight, "edge metric " + e.getCost()
                        + " is greater than the cost limit of " + costLimit);
            } else if (useMetric2 && dblMetric2 > metricLimit) {
                return excludeEdge(e, workingWeight, "edge cost " + e.getMetric()
                        + " is greater than the metric limit of " + metricLimit);
            }

            // Check if bandwidth is available
            if (model == null) {
                log.error("Model is null! ");
                // not sure if we should return here
                return Double.valueOf(workingWeight);
            }

            if (trackerRate < 0) {
                log.error("trackerRate is not valid or not set: " + trackerRate);
                // not sure if we should return here
                return Double.valueOf(workingWeight);
            }

            // Check source target for available bandwidth
            if (src == null || dst == null) {
                log.error("EdgeValue: cannot find src or dst node!: src: " + src
                        + " dst: " + dst + " from edge " + e);
                // not sure if we should return here
                return Double.valueOf(workingWeight);
            }

            String srcNeId = src.getIeee();
            String dstNeId = dst.getIeee();
            String srcPort = e.getSourceAid();
            String dstPort = e.getTargetAid();
            boolean srcBWfound = false;
            boolean dstBWfound = false;

            log.debug("srcNeId: " + srcNeId + " dstNeId: " + dstNeId);

            if (srcNeId != null && dstNeId != null) {
                if (srcPort != null && dstPort != null) {

                    Map<String, LpcpFacility> srcFacMap = model.get(srcNeId);
                    Map<String, LpcpFacility> dstFacMap = model.get(dstNeId);
                    LpcpFacility srcFacility = null;
                    LpcpFacility dstFacility = null;
                    BasicTracker srcTracker = null;
                    BasicTracker dstTracker = null;
                    int channel = -1;
                    int srcChannel = -1;
                    int dstChannel = -1;

                    if (srcFacMap != null && dstFacMap != null) {
                        srcFacility = srcFacMap.get(srcPort);
                        dstFacility = dstFacMap.get(dstPort);
                        log.debug("srcFacMap: " + srcFacMap + "\nsrcPort: " + srcPort
                                + "\nfacility for srcPort: " + srcFacility);

                        if (srcFacility != null && dstFacility != null) {

                            if ((FacilityConstants.SIGNAL_TYPE.INNI.toString().equals(srcFacility.getSigType()) ||
                                    FacilityConstants.SIGNAL_TYPE.ENNI.toString().equals(srcFacility.getSigType())) &&
                                    (FacilityConstants.SIGNAL_TYPE.INNI.toString().equals(dstFacility.getSigType()) ||
                                    FacilityConstants.SIGNAL_TYPE.ENNI.toString().equals(dstFacility.getSigType()))) {

                                if (srcFacility.getType().equals(dstFacility.getType())) {
                                    // if (!"ETH".equals(srcFacility.getType()) &&
                                    // !"ETH".equals(dstFacility.getType()))
                                    if (!srcFacility.isL2() && !dstFacility.isL2()) {
                                        if (!(srcFacility.getType().startsWith(
                                                FacilityConstants.LIM) || srcFacility.getType().startsWith(FacilityConstants.CMD))) {
                                            srcTracker = srcFacility.getTracker();
                                            dstTracker = dstFacility.getTracker();
                                        } else {
                                            srcTracker = srcFacility.getTracker();
                                            dstTracker = dstFacility.getTracker();
                                        }
                                        /*
                                         * If fixedTimeslot is set, then we
                                         * don't want timeslot interchange We
                                         * need to try and get the same timeslot
                                         * on every edge
                                         */
                                        if (fixedTimeslot == -1) {
                                            srcChannel = srcTracker.getNextChannel(trackerRate,
                                                    parameters);
                                            dstChannel = dstTracker.getNextChannel(trackerRate,
                                                    parameters);
                                        } else {
                                            srcChannel = srcTracker.getNextChannel(
                                                    Integer.toString(fixedTimeslot), trackerRate,
                                                    parameters);
                                            dstChannel = dstTracker.getNextChannel(
                                                    Integer.toString(fixedTimeslot), trackerRate,
                                                    parameters);
                                        }



                                        if (fixedTimeslot == -1) {
                                            // HACK! The substring part below is necessary for
                                            // SDH differences between OME and HDX
                                            if (srcChannel != dstChannel
                                                    && srcFacility.getAid().substring(1, 2).equals(dstFacility.getAid().substring(1, 2))) {
                                                channel = -1;

                                                // @TODO
                                                log.error("This code has rusted and no longer works!");
                                                log.error("Source Port (" + srcFacility.getNeId() + ","
                                                        + srcFacility.getTNA() + "," + srcFacility.getAid()
                                                        + "," + srcChannel + ")");
                                                log.error("Dest Port (" + dstFacility.getNeId() + ","
                                                        + dstFacility.getTNA() + "," + dstFacility.getAid()
                                                        + "," + dstChannel + ")");

                                                // channel = srcTracker.getNextChannel(1, trackerRate,
                                                // srcTracker
                                                // .getInternalTrackerClone(),
                                                // dstTracker.getInternalTrackerClone(),
                                                // parameters);
                                                if (channel != -1) {
                                                    srcBWfound = true;
                                                    dstBWfound = true;
                                                }
                                            } else {
                                                log.debug("Src and Dst channels match and are set to: "
                                                        + channel);
                                                channel = srcChannel;
                                                if (channel != -1) {
                                                    srcBWfound = true;
                                                    dstBWfound = true;
                                                } else {
                                                    log.debug("No bandwidth available, Src and Dst channel is: "
                                                            + channel);
                                                }
                                            }
                                        } else { // Request requires that no Timeslot interchange
                                            // exist
                                            if (srcChannel == fixedTimeslot) {
                                                channel = srcChannel;
                                                srcBWfound = true;
                                            } else {
                                                log.debug("No bandwidth available, srcChannel: "
                                                        + srcChannel
                                                        + " does not match requested timeslot: "
                                                        + fixedTimeslot);
                                            }
                                            if (dstChannel == fixedTimeslot) {
                                                channel = dstChannel;
                                                dstBWfound = true;
                                            } else {
                                                log.debug("No bandwidth available, dstChannel: "
                                                        + dstChannel
                                                        + " does not match requested timeslot: "
                                                        + fixedTimeslot);
                                            }
                                        }

                                    } else {
                                        // Routing through ETH ports using ENNI - HACK -
                                        // bandwidth is hardcoded to available.
                                        // srcChannel = "ETH".equals(srcFacility.getType()) ?
                                        // ((EthWanBandwidthTracker) srcFacility
                                        // .getTracker()).getNextChannel(trackerRate, parameters)
                                        // : srcFacility.getTracker().getNextChannel(trackerRate,
                                        // parameters);
                                        //
                                        // dstChannel = "ETH".equals(dstFacility.getType()) ?
                                        // ((EthWanBandwidthTracker) dstFacility
                                        // .getTracker()).getNextChannel(trackerRate, parameters)
                                        // : dstFacility.getTracker().getNextChannel(trackerRate,
                                        // parameters);

                                        srcChannel = srcFacility.getTracker().getNextChannel(
                                                trackerRate, parameters);
                                        dstChannel = dstFacility.getTracker().getNextChannel(
                                                trackerRate, parameters);

                                        if (srcChannel > 0 && dstChannel > 0
                                                && srcChannel != dstChannel) {
                                            channel = Math.max(srcChannel, dstChannel);
                                            /*
                                             * Multiply the channel by 50 since
                                             * the channel is in STS channel
                                             * numbers so need to multiply by 50
                                             * to get the Mb equivalent
                                             */
                                            if (channel * 50 > Integer.parseInt(srcFacility.getExtendedAttributes().get(
                                                    FacilityConstants.SPEED_ATTR))) {
                                                srcChannel = -1;
                                                log.error("SRC BANDWIDTH EXCEEDED");
                                            }
                                            if (channel * 50 > Integer.parseInt(dstFacility.getExtendedAttributes().get(
                                                    FacilityConstants.SPEED_ATTR))) {
                                                dstChannel = -1;
                                                log.error("DST BANDWIDTH EXCEEDED");
                                            }
                                        } else if (srcChannel > 0 && dstChannel > 0
                                                && srcChannel == dstChannel) {
                                            channel = srcChannel;
                                        }

                                        log.debug("SRC CHANNEL: " + srcChannel + " DST CHANNEL: "
                                                + dstChannel + " channel: " + channel);
                                        srcBWfound = srcChannel != -1;
                                        dstBWfound = dstChannel != -1;
                                    }
                                } else {
                                    log.debug("Source and destination port types do not match - possible layer boundary crossing");

                                    srcBWfound = true;
                                    dstBWfound = true;
                                }
                            } else {
                                log.error("Cannot route over non-INNI or non-ENNI port: "
                                        + srcFacility + " " + dstFacility);
                            }
                        } else {
                            log.error("Cannot find source or destination facility - srcFacility: "
                                    + srcFacility + " dstFacility: " + dstFacility);
                        }
                        if (srcBWfound && dstBWfound) {
                            DracEdge use = e;
                            if (srcFacility.getExtendedAttributes().get(
                                    FacilityConstants.INGRESSIP_ATTR) != null) {
                                if (use.getIngressIp() == null) {
                                    use.setIngressIp(srcFacility.getExtendedAttributes().get(
                                            FacilityConstants.INGRESSIP_ATTR));
                                }
                            }

                            if (dstFacility.getExtendedAttributes().get(
                                    FacilityConstants.INGRESSIP_ATTR) != null) {
                                if (use.getEgressIp() == null) {
                                    use.setEgressIp(dstFacility.getExtendedAttributes().get(
                                            FacilityConstants.INGRESSIP_ATTR));
                                }
                            }
                            if (srcFacility.getAid().substring(1, 2).equals(dstFacility.getAid().substring(1, 2))) {
                                if (use.getSourceChannel() != null) {
                                    use.setSourceChannel(null);
                                }
                                use.setSourceChannel(Integer.valueOf(channel));
                                use.setTargetChannel(Integer.valueOf(channel));
                            } else {
                                // HACK to support SDH differences between OME and HDX
                                use.setSourceChannel(Integer.valueOf(srcChannel));
                                use.setTargetChannel(Integer.valueOf(dstChannel));
                            }
                        } else {
                            return excludeEdge(e, workingWeight,
                                    "edge has been marked as full");
                        }
                    } else {
                        log.debug("FATAL ERROR: model does not know about source or dest! srcFacMap: "
                                + srcFacMap + " dstFacMap: " + dstFacMap);
                    }
                    debug(e, srcPort, dstPort, srcBWfound, dstBWfound, srcTracker,
                            dstTracker);
                } else {
                    log.debug("FATAL ERROR: srcPort: " + srcPort + " dstPort: " + dstPort);
                }
            } else {
                log.debug("FATAL ERROR: srcNeId: " + srcNeId + " dstNeId: " + dstNeId);
            }

        } catch (Exception ex) {
            log.error("UserDateEdgeValue::FATAL EXCEPTION: ", ex);
        }

        return Double.valueOf(workingWeight);
    }

    public void setCostLimit(double costLimit) {
        this.costLimit = costLimit;
        useCost = true;
    }

    public void setFixedTimeslot(int timeslot) {
        this.fixedTimeslot = timeslot;
    }

    public void setMetric2Limit(double limit) {
        metricLimit = limit;
        useMetric2 = true;
    }

    @Override
    public Number transform(DracEdge e) { // NO_UCD
        Number n = getNumber(e);

        return n;
    }

    private void debug(DracEdge e, String srcPort, String dstPort,
            boolean srcBWfound, boolean dstBWfound, BasicTracker srcTracker,
            BasicTracker dstTracker) {
        // Debug
        double srcUtil = -1;
        double dstUtil = -1;
        try {
            srcUtil = srcTracker != null ? srcTracker.getUtilisation() : -1;
            dstUtil = dstTracker != null ? dstTracker.getUtilisation() : -1;
        } catch (Exception e1) {
            log.error("Error: ", e1);
        }

        log.debug("Edge: " + e + " Source: " + src.getIeee() + " Destination: "
                + dst.getIeee() + "\n");
        log.debug("   Source: " + src + "(" + src.getIeee() + ") Port: " + srcPort);
        log.debug(" PORT_UTIL: " + srcUtil + " SRC_CHANNEL: " + e.getSourceChannel());
        log.debug("   Destination: " + dst + "(" + dst.getIeee() + ") Port: " + dstPort);
        log.debug(" PORT_UTIL: " + dstUtil + " DST_CHANNEL: " + e.getTargetChannel());

    }

    /**
     * Wayne May 2009: We used to just remove edges directly from the graph,
     * with Jung 2.0 this throws concurrent modification exceptions, instead
     * we'll just return a very high weight and remember the edges we've
     * excluded here. Then we'll have the caller run Dijkstra again after really
     * removing the edges.
     */
    private Double excludeEdge(DracEdge e, double workingWeight, String reason) {

        log.debug("Edge: " + e + " has been excluded reason: " + reason);
        // Remove the edge from the graph
        // routingEngine.excludeEdge(e);
        excludedEdges.add(e);
        return Double.valueOf(workingWeight + INFINITY);
    }
}
