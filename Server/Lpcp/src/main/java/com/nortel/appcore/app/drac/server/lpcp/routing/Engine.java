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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.RoutingException;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.utility.GenericJdomParser;
import com.nortel.appcore.app.drac.server.lpcp.common.LpcpConstants;
import com.nortel.appcore.app.drac.server.lpcp.common.Utility;
import com.nortel.appcore.app.drac.server.lpcp.trackers.BasicTracker;
import com.nortel.appcore.app.drac.server.lpcp.trackers.EthTrackerI;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility;
import com.nortel.appcore.app.drac.server.lpcp.trackers.SonetTrackerI;
import com.nortel.appcore.app.drac.server.lpcp.trackers.WavelengthTracker;

/**
 * Engine The DRAC routing engine - based on the JUNG shortest path first
 * implementation.. Engine encapsulates the Jung implementation of shortest path
 * first to calculate all paths through the network.
 *
 * @since 2005-11-08
 * @author Adrian Lee
 * @version 0.2
 */

public final class Engine {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /* List of excluded edges for the current route resolution. */
	private final Map<String, DracEdge> excludedEdges = new HashMap<String, DracEdge>();

    /* Topology graph we will use for routing. */
	private final TopologyManager topologyMgr;

    /**
     * Class constructor initializes engine with a reference to the topology
     * manager.
     *
     * @param topologyManager The topology manager to associated with this
     * engine instance.
     */
	public Engine(TopologyManager topologyManager) {
		topologyMgr = topologyManager;
	}

    /**
     * Determines if the provided list of graph edges contains an excluded edge.
     *
     * @param edges list of edges.
     * @return true if there is an excluded edge in the list.
     */
	public boolean containsExcludedEdge(List<DracEdge> edges) {
		boolean rc = false;
		if (edges != null) {
			for (DracEdge e : edges) {
				if (isExcluded(e)) {
					rc = true;

					break;
				}
			}
		}
		return rc;
	}

    /**
     * Traverse the list of edges within the topology graph and exclude any
     * edges that are greater than cost.
     *
     * @param cost the value to minimize routing and is used by this method to
     * exclude edges.
     *
     * @param topology the topology graph.
     * @throws RoutingException an invalid cost was provided.
     *
     * Notes:
     *
     * TODO: This method is useless and should be removed.  Cost based
     * routing is based on path computation minimizing on the sum of a cost
     * metric on all links in the path.  This is excluding individual links
     * that are greater than the cost.  This does not provide value.  Also,
     * the schedule() method invokes this with a string after just
     * converting the string to an integer.  Why are we using a double here?
     */
	public void excludeCost(String cost, TopologyManager topology)
            throws RoutingException {

        /* Get the list of edges in our topology graph. */
		Iterator<DracEdge> it = topology.getRoutingEdges().iterator();

		/* Make sure that we can parse the input cost. */
        double excludeCost;
		try {
			excludeCost = Double.parseDouble(cost);
		}
		catch (Exception e) {
			/* The specified cost value could not be parsed. */
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3033_INVALID_COST_SPECIFIED,
			    new String[] { cost }, e);
		}

        /* Now we visit each edge and discard those greater than cost. */
		double edgeCost;
		String edgeCostStr;

		while (it.hasNext()) {
			DracEdge edge = it.next();
			edgeCostStr = edge.getCost();

			if (edgeCostStr != null && !edgeCostStr.trim().isEmpty() &&
                    !"N/A".equals(edgeCostStr.trim())) {
				edgeCost = Double.parseDouble(edgeCostStr);

				if (edgeCost > excludeCost) {
					// Remove this edge from the graph
					excludeEdge(edge);
				}
			}
		}
	}

    /**
     * Exclude the provided edge from the topology graph and add the excluded
     * edge to the excluded edges list.
     *
     * @param e edge to exclude.
     */
	public void excludeEdge(DracEdge e) {
		try {

			topologyMgr.removeEdgeFromGraph(e);
			excludedEdges.put(e.getID(), e);
		}
		catch (java.lang.IllegalArgumentException iae) {
			/*
			 * If the edge has already been excluded, we can ignore this exception The
			 * message should be that the edge is not in the graph. A better way is to
			 * globally keep track of the excluded edges and just return if the edge
			 * is already marked excluded.
			 */
			log.error("Error: ", iae);
		}
	}

    /**
     * Exclude the provided edges from the topology graph.
     *
     * @param edges the edges to exclude.
     */
	public void excludeEdges(Set<DracEdge> edges) {
		if (edges != null) {
			for (DracEdge e : edges) {
				excludeEdge(e);
			}
		}
	}

    /**
     * Exclude all layer two edges from the topology graph.
     *
     * @param topology the topology graph to traverse for layer two edges.
     *
     * @throws Exception if an invalid edge is detected in the graph.
     */
	public void excludeLayer2Edges(TopologyManager topology) throws Exception {
        /* Traverse the topology graph and discard any layer two edges. */
		for (DracEdge e : topology.getRoutingEdges()) {
            /* Lookup the source and destination facilities using the NE IEEE
             * ID and port AID.
             */
			LpcpFacility srcFac = HierarchicalModel.INSTANCE.getFacility(e.getSource().getIeee(), e.getSourceAid());
			LpcpFacility dstFac = HierarchicalModel.INSTANCE.getFacility(e.getTarget().getIeee(), e.getTargetAid());

            /* If we didn't get a match then this is bad. */
			if (srcFac == null || dstFac == null) {
				throw new Exception(
				    "Source or destination facility could not be found '"
                        + srcFac + "' '" + dstFac + "' from edge '" + e + "'");
			}

			/* If one end is layer 2 then the other end should be as well,
             * here we'll take either.
             */
			if (srcFac.isL2() || dstFac.isL2()) {
				excludeEdge(e);
			}
		}
	}

    /**
     * Traverse the list of edges within the topology graph and exclude any
     * edges that are greater than metric.
     *
     * @param metric the value to minimize routing and is used by this method to
     * exclude edges.
     *
     * @param topology the topology graph.
     *
     * @throws RoutingException an invalid metric value was provided.
     *
     * TODO: This method is useless and should be removed.  Cost based
     * routing is based on path computation minimizing on the sum of a cost
     * metric on all links in the path.  This is excluding individual links
     * that are greater than the cost.  This does not provide value.  Also,
     * the schedule() method invokes this with a string after just
     * converting the string to an integer.  Why are we using a double here?
     */
	public void excludeMetric(String metric, TopologyManager topology)
	    throws RoutingException {
		Iterator<DracEdge> it = topology.getRoutingEdges().iterator();

		double excludeMetric;

		/* First, make sure that we can parse the input metric. */
		try {
			excludeMetric = Double.parseDouble(metric);
		}
		catch (Exception e) {
			/* The metric value could not be parsed. */
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3034_INVALID_METRIC_SPECIFIED,
			    new String[] { metric }, e);
		}

		double edgeMetric;
		String edgeMetricStr;

		while (it.hasNext()) {
			DracEdge edge = it.next();
			edgeMetricStr = edge.getMetric();


			if (edgeMetricStr != null && !edgeMetricStr.trim().isEmpty() &&
                    !"N/A".equals(edgeMetricStr.trim())) {
				edgeMetric = Double.parseDouble(edgeMetricStr);

				if (edgeMetric > excludeMetric) {
					/* Remove this edge from the graph. */
					excludeEdge(edge);
				}
			}
		}
	}

    /**
     * Traverse the list of edges within the topology graph and exclude any
     * edges that include an SRLG value from the included list.
     *
     * @param srlgs a list of edge SRLG values to exclude from routing.
     *
     * @param topology the topology graph.
     *
     * @throws RoutingException an invalid SRLG value was provided.
     *
     * TODO: Why not leave these as strings and compare instead of converting
     * both to doubles?
     *
     */
	public void excludeSRLG(String srlgs[], TopologyManager topology)
            throws RoutingException {
        /* We will need to visit each edge. */
		Iterator<DracEdge> it = topology.getRoutingEdges().iterator();

		/* Convert string SRLG to exclude into a HashSet. */
        Set<Double> srlgSet = new HashSet<Double>();
		for (String srlg : srlgs) {
			try {
			  if(srlg!=null && !"unknown".equals(srlg)){
			    srlgSet.add(Double.valueOf(srlg));
			  }
			}
			catch (NumberFormatException e) {
				/* A specified SRLG value could not be parsed. */
				throw new RoutingException(
				    DracErrorConstants.LPCP_E3035_INVALID_SRLG_SPECIFIED,
				    new String[] { srlg }, e);
			}
		}

        /* Process the list of SRLG against each edge in the graph. */
		Double edgeSRLG;
		String edgeSRLGs[];
		while (it.hasNext()) {
			DracEdge edge = it.next();
			if (edge.getSrlg() != null && !edge.getSrlg().trim().isEmpty()) {
                /* SRLG string is a comma separated list. */
				edgeSRLGs = edge.getSrlg().split(",");

                /* For each SRLG we found on the edge convert and compare to
                 * exclusion list.
                 */
				for (int i = 0; i < edgeSRLGs.length; i++) {

					if (edgeSRLGs[i] != null && !"N/A".equals(edgeSRLGs[i].trim())) {
                        edgeSRLG = Double.valueOf(edgeSRLGs[i]);
                        if (srlgSet.contains(edgeSRLG)) {
                            /* Remove this edge from the graph. */
                            excludeEdge(edge);
                        }
                    }
				}
			}
		}
	}

    /**
     * Merge the working and protection path XML representation into a single
     * string for display in administration console.
     *
     * @param workingPathXML xml string representing the working path.
     * @param protectionPathXML xml string representing the protection path.
     * @return string containing xml for the merged working and protection path.
     *
     * TODO: This is a specific view of the data needed by the Admin Console so
     * should it really be in this class?  This method is called from
     * LpcpScheduler.schedule() and is stored in the schedule parameters under
     * the SPF_KEY attribute.
     *
     * <pathEvent>
     *   <pathInstance>
     *       <path>
     *           <edge source="00-17-D1-FF-A1-14" target="00-17-D1-FF-A1-14" sourceport="WAN-1-4-3"
     *               targetport="OC192-1-9-1" rate="STS3C" sourcechannel="1" targetChannel="1" />
     *           <edge source="00-17-D1-FF-A0-EF" target="00-17-D1-FF-A0-EF" sourceport="OC192-1-10-1"
     *               targetport="WAN-1-3-2" rate="STS3C" sourcechannel="1" targetChannel="1" />
     *       </path>
     *       <edgelist>
     *           <edge id="2" />
     *       </edgelist>
     *       <endpoints>
     *           <source id="00-17-D1-FF-A1-14" tna="OME0237-1-4-3_VCAT" />
     *           <target id="00-17-D1-FF-A0-EF" tna="OME0039_ETH-1-3-2_CCAT" />
     *       </endpoints>
     *       <status text="Path from: OME0237 to OME0039, cost: 1 hops: 1" />
     *   </pathInstance>
     * </pathEvent>
     *
     */
	public String getCombinedWorkingProtectionPath(String workingPathXML,
            String protectionPathXML) {

        /* Results of the combined paths. */
		String combinedPath = null;

        /* We create dom parsers for each of the paths to merge. */
		GenericJdomParser workingPathParser = new GenericJdomParser();
		GenericJdomParser protectionPathParser = new GenericJdomParser();

        /* We are going to reformat the merged path into and XML string. */
		XMLOutputter outputter = new XMLOutputter();

		Element workingRoot;
		Element protectionRoot;
		Element pathInstance;

		try {
            /* Parse the input XML strings into a DOM. */
			workingPathParser.parse(workingPathXML);
			protectionPathParser.parse(protectionPathXML);

            /* Get a reference to the root of the parsed XML document. */
			workingRoot = workingPathParser.getRoot();
			protectionRoot = protectionPathParser.getRoot();

			log.debug("workingRoot.get(pathInstance) is: "
			    + outputter.outputString(workingRoot.getChild("pathInstance")));

            /* We want the <pathInstance> element to merge. */
			pathInstance = protectionRoot.getChild("pathInstance");

            /* Detach the element from its DOM. */
			pathInstance.detach();

            /* Merge the protection path into the working path. */
			workingRoot.addContent(pathInstance);

            /* Generate the new output path string. */
			combinedPath = outputter.outputString(workingRoot);
		}
		catch (Exception e) {
			log.error("Exception getting combined path", e);
		}

		return combinedPath;
	}

    /**
     * Find a path within the topology graph meeting the specified criteria.
     *
     * @param parameters
     * @param topology
     * @param model
     * @param resetExclusions
     * @param fixedTimeslot
     * @return
     * @throws RoutingException
     */
	public synchronized Map<SPF_KEYS, Object> getPath(
            Map<SPF_KEYS, Object> parameters, TopologyManager topology,
            HierarchicalModel model, boolean resetExclusions,
            boolean fixedTimeslot) throws RoutingException {

		UserDateEdgeValue edgeWeightWorker = null;

		DracVertex src = null;
		DracVertex dst = null;

		String rSrcNode = null;
		String rDstNode = null;
		// String srlg = null;
		String srcChannel = (String) parameters.get(SPF_KEYS.SPF_SRCCHANNEL);
		String dstChannel = (String) parameters.get(SPF_KEYS.SPF_DSTCHANNEL);

		int srcTNAchannel = -1;
		int dstTNAchannel = -1;
		int hopInt = -1;
		int costInt = -1;
		int metric2Int = -1;
		RoutingException re = null;

		String rate = (String) parameters.get(SPF_KEYS.SPF_RATE);
		String srcTNA = (String) parameters.get(SPF_KEYS.SPF_SRCTNA);
		String dstTNA = (String) parameters.get(SPF_KEYS.SPF_DSTTNA);



		if (parameters.get(SPF_KEYS.SPF_HOP) != null) {
			try {
				hopInt = Integer.parseInt((String) parameters.get(SPF_KEYS.SPF_HOP));
			}
			catch (Exception ex) {
				log.error(
				    "Invalid hop metric specified: "
				        + (String) parameters.get(SPF_KEYS.SPF_HOP), ex);
			}
		}

		// srlg = (String) parameters.get(SPF_KEYS.SPF_SRLG);

		if (parameters.get(SPF_KEYS.SPF_COST) != null) {
			try {
				costInt = Integer.parseInt((String) parameters.get(SPF_KEYS.SPF_COST));
			}
			catch (Exception ex) {
				log.error(
				    "Invalid cost metric specified: "
				        + (String) parameters.get(SPF_KEYS.SPF_COST), ex);
			}
		}

		if (parameters.get(SPF_KEYS.SPF_METRIC2) != null) {
			try {
				metric2Int = Integer.parseInt((String) parameters
				    .get(SPF_KEYS.SPF_METRIC2));
			}
			catch (Exception ex) {
				log.error(
				    "Invalid metric2 metric specified: "
				        + (String) parameters.get(SPF_KEYS.SPF_METRIC2), ex);
			}
		}

		rSrcNode = resolveNeId(srcTNA, model);
		rDstNode = resolveNeId(dstTNA, model);
		log.debug("Replacing attribute for ScheduleXML.SOURCENE_ATTR with: "
		    + rSrcNode);
		log.debug("Replacing attribute for ScheduleXML.TARGETNE_ATTR with: "
		    + rDstNode);
		parameters.put(SPF_KEYS.SPF_SOURCEID, rSrcNode);
		parameters.put(SPF_KEYS.SPF_TARGETID, rDstNode);

		src = topology.getVertex(rSrcNode);
		dst = topology.getVertex(rDstNode);

		if (src == null || dst == null) {
			log.error("could not find source or dst node " + src + " " + dst + " "
			    + rSrcNode + " " + rDstNode + " " + model.getModel());
			re = new RoutingException(
			    DracErrorConstants.LPCP_E3026_CANNOT_FIND_SRC_OR_DEST, new Object[] {
			        src, dst, rSrcNode, rDstNode, });
			throw re;
		}


		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));



		Map<String, Integer> precheckedData = new HashMap<String, Integer>();

		// Reset all previously calculated channel numbers
		topology.resetChannels();
		validateTNAs(rSrcNode, rDstNode, srcTNA, dstTNA, rate, srcChannel,
		    dstChannel, model, precheckedData, parameters);

		srcTNAchannel = precheckedData.get(LpcpConstants.SRCTNACHANNEL_KEY)
		    .intValue();
		dstTNAchannel = precheckedData.get(LpcpConstants.DSTTNACHANNEL_KEY)
		    .intValue();

		log.debug("Finished checking TNAs: "
		    + sdf.format(new Date(System.currentTimeMillis())));
		log.debug("Using srcTNAchannel: " + srcTNAchannel + " dstTNAchannel: "
		    + dstTNAchannel);

		if (srcTNAchannel == -1) {
			re = new RoutingException(
			    DracErrorConstants.LPCP_E3027_SRC_CHANNEL_NOT_AVAILABLE);
			throw re;
		}

		if (dstTNAchannel == -1) {
			re = new RoutingException(
			    DracErrorConstants.LPCP_E3028_TGT_CHANNEL_NOT_AVAILABLE);
			throw re;
		}

		if (src.equals(dst)) {


		}

        /* Create a new edge worker for Jung graph evaluation. This will be
         * invoked by Jung whenever an edge is visited.
         */
		edgeWeightWorker = new UserDateEdgeValue(
		    Utility.convertStringRateToInt(rate), model.getModel(), parameters);

		if (fixedTimeslot) {

			edgeWeightWorker.setFixedTimeslot(srcTNAchannel);
		}

		// Set the routing metric to use
		if (costInt > 0) {
			edgeWeightWorker.setCostLimit(costInt * 1.0);
		}
		else if (metric2Int > 0) {
			edgeWeightWorker.setMetric2Limit(metric2Int * 1.0);
		}

		// dsp = new DijkstraShortestPath(graph, edgeWeightWorker );

		log.debug("Finding shortest path from: "
                + rSrcNode + " : " +  srcTNAchannel + " to: "
                + rDstNode + " : " +  dstTNAchannel + " hopInt: " + hopInt
                + "..." + "\nedges: " + topology.getRoutingEdges().toString()
                + "\nvertices: " + topology.getVertices().toString()
                + "\nexcluded edges: " + excludedEdges
                + "\nexcluded vertices: ");

		Map<SPF_KEYS, Object> result = null;
		try {
			result = topology.getPath(rate, dstTNAchannel, srcTNAchannel, hopInt,
			    this, edgeWeightWorker, src, dst, parameters, model);
		}
		catch (RoutingException rep) {
			// Re-throw routing exceptions
			log.error("Exception caught processing result", rep);
			throw rep;
		}
		catch (Exception e) {
			log.error("Exception caught processing result", e);
		}
		finally {
			if (resetExclusions) {
				resetExcludedEdges();
			}
		}

		log.debug("Engine::getPath::parameters: " + parameters + "\nresult="
		    + result);
		return result;
	}

    /**
     *
     * @param srcTNA
     * @param dstTNA
     * @param model
     * @return
     * @throws RoutingException
     */
	public LpcpFacility.VCAT_ROUTING_TYPE getPathVcatRoutingType(String srcTNA,
	    String dstTNA, HierarchicalModel model) throws RoutingException {
		LpcpFacility.VCAT_ROUTING_TYPE pathVcatType = LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_CCAT;

		LpcpFacility srcFac = model.getFacilityForTNA(srcTNA);
		LpcpFacility dstFac = model.getFacilityForTNA(dstTNA);

		if (srcFac == null || dstFac == null) {
			log.error("getPathVcatRoutingType(" + srcTNA + "," + dstTNA
			    + ",model yields null for src or dst facility " + srcFac + " "
			    + dstFac + " from model " + model);
			return LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_CCAT;

		}

		if (srcFac.isL2() && dstFac.isL2()) {
			LpcpFacility.VCAT_ROUTING_TYPE srcRoutingType = srcFac
			    .getVCATRoutingType();
			LpcpFacility.VCAT_ROUTING_TYPE dstRoutingType = dstFac
			    .getVCATRoutingType();

			if (srcRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_CCAT
			    || dstRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_CCAT) {
				pathVcatType = LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_CCAT;
			}

			else if (srcRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_VCAT
			    || dstRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_VCAT) {
				pathVcatType = LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_VCAT;
			}

			else {
				pathVcatType = LpcpFacility.VCAT_ROUTING_TYPE.CAN_BE_EITHER;
			}
		}
		else if (srcFac.isL2() && !dstFac.isL2()) {
			LpcpFacility.VCAT_ROUTING_TYPE srcRoutingType = srcFac
			    .getVCATRoutingType();
			pathVcatType = srcRoutingType;
		}
		else if (dstFac.isL2() && !srcFac.isL2()) {
			LpcpFacility.VCAT_ROUTING_TYPE dstRoutingType = dstFac
			    .getVCATRoutingType();
			pathVcatType = dstRoutingType;
		}

		return pathVcatType;
	}

	public boolean isExcluded(DracEdge e) {
		return excludedEdges.get(e.getID()) != null;
	}

	public void resetExcludedEdges() {

		DracEdge edge = null;
		Iterator<String> it = excludedEdges.keySet().iterator();
		ArrayList<DracEdge> resetEdges = new ArrayList<DracEdge>();

		log.debug("Engine::resetExcludedEdges - graph contains these edges before reset: "
		    + topologyMgr.getRoutingEdges());
		log.debug("Engine::resetExcludedEdges - excludedEdges contains these edges before reset: "
		    + excludedEdges);
		while (it.hasNext()) {
			edge = excludedEdges.get(it.next());
			try {
				resetEdges.add(edge);
				topologyMgr.addEdgeToGraph(edge);
			}
			catch (Exception cve) {
				log.error("Exception caught: ", cve);
			}
		}

		for (int i = 0; i < resetEdges.size(); i++) {
			edge = resetEdges.get(i);
			excludedEdges.remove(edge.getID());
		}

		log.debug("Engine::resetExcludedEdges - graph contains these edges after reset: "
		    + topologyMgr.getRoutingEdges());
		log.debug("Engine::resetExcludedEdges - excludedEdges contains these edges after reset: "
		    + excludedEdges);
	}

	/**
     *
     */
	@SuppressWarnings("unchecked")
  protected Map<SPF_KEYS, Object> processPathResult(
	    HierarchicalModel model, TopologyManager topology,
	    final Map<SPF_KEYS, Object> parameters, List<DracEdge> currentShortPath,
	    int srcTNAchannel, int dstTNAchannel, String rate,
	    UserDateEdgeValue edgeWeightWorker) throws Exception {

		Map<SPF_KEYS, Object> result = new HashMap<SPF_KEYS, Object>();
		int pathCost = 0;
		DracEdge use = null;
		DracVertex srcVtx = null;
		DracVertex dstVtx = null;
		String srcNeId = null;
		String dstNeId = null;
		String srcPort = null;
		String dstPort = null;
		String sourceChannel = null;
		String targetChannel = null;
		String srcTNA = null;
		String dstTNA = null;
		String aend = null;
		String zend = null;
		String lastNE = null;

		List<String> orderedPortList = new ArrayList<String>();
		List<String> orderedNeList = new ArrayList<String>();
		List<String> orderedChannelList = new ArrayList<String>();
		RoutingException re = null;
		Element pathRoot = null;
		Element pathInstance = null;
		XMLOutputter outputter = new XMLOutputter();
		LpcpFacility srcFac = null;
		LpcpFacility dstFac = null;
		boolean edgeFlipped = false;
		Set<DracEdge> jungEdges = null;

		String protection = (String) parameters.get(SPF_KEYS.SPF_PROTECTION);
		// String pathType = (String) parameters.get("PATHTYPE");
		// boolean isPBT = false;
		boolean isOnePlusOne = false;

		srcTNA = (String) parameters.get(SPF_KEYS.SPF_SRCTNA);
		dstTNA = (String) parameters.get(SPF_KEYS.SPF_DSTTNA);

		srcNeId = resolveNeId(srcTNA, model);
		dstNeId = resolveNeId(dstTNA, model);

		// isPBT = NeType.PP8600.equals(topology.getTypeForNeId(srcNeId)) ||
		// NeType.PP8600.equals(topology.getTypeForNeId(dstNeId));
		isOnePlusOne = LpcpConstants.PROTECTION_1PLUS1.equalsIgnoreCase(protection);

		log.debug("processPathResult: srcNeId resolved to: " + srcNeId
		    + " dstNeId resolved to: " + dstNeId);

		// If the path list size == 0, then there was no path found. Throw an
		// exception right away.
		if (currentShortPath == null || currentShortPath.size() == 0
		    && srcNeId != null && dstNeId != null && !srcNeId.equals(dstNeId)) {
			log.debug("processPathResult: currentShortPath size: "
			    + currentShortPath.size() + " : " + currentShortPath);
			re = new RoutingException(
			    DracErrorConstants.LPCP_E3020_NO_PATH_FOR_SPECIFIED_PARAMETERS);
			throw re;
		}

		// Each edge will have a source and target, sourceport, targetport... the
		// nodal XCs, however,
		// are always on a single NE so everything is offset by 1 port.
		// S1P T1P => STNAPORT S1P, T1P S2P, ....
		//
		// First, build the ordered ArrayList of ports each port pair is a src and
		// dst port on an NE
		// !!!!! The srcTNA and dstTNA are not in AID format - must map these to AID
		// format

		// if (model.getFacilityForTNA(srcTNA) != null &&
		// model.getFacilityForTNA(srcTNA).getParent() != null)
		// {
		// orderedPortList.add(Utility.convertETH2WAN(model.getAIDForTNA(srcTNA)));
		// }
		// else
		{
			orderedPortList.add(model.getAIDForTNA(srcTNA));
		}

		orderedChannelList.add(Integer.toString(srcTNAchannel));

		orderedNeList.add(srcNeId);

		// LinkedHashSet uniqueNEs = new LinkedHashSet();
		List<String> ports = new ArrayList<String>();
		// uniqueNEs.add( srcNeId );
		// Map<String, List<String>> gmplsNEMap = new HashMap<String,
		// List<String>>();

		log.debug("processPathResult: currentShortPath.size(): "
		    + currentShortPath.size());

		jungEdges = (Set<DracEdge>) parameters.get(SPF_KEYS.SPF_JUNG_EDGELIST);
		if (jungEdges == null) {
			jungEdges = new HashSet<DracEdge>();
		}

		for (int i = 0; i < currentShortPath.size(); i++) {
			use = currentShortPath.get(i);
			jungEdges.add(use);


			pathCost += (int) Double.parseDouble(edgeWeightWorker.getNumber(use)
			    .toString());
			srcVtx = use.getSource();
			dstVtx = use.getTarget();
			srcPort = use.getSourceAid();
			dstPort = use.getTargetAid();

			ports.add(srcPort);
			ports.add(dstPort);
			// if ( use.getUserDatum( "INGRESSIP" ) == null ) {
			// uniqueNEs.add( srcVtx.getUserDatum("NEID" ) );
			// uniqueNEs.add( dstVtx.getUserDatum("NEID" ) );
			// } else {
			// uniqueNEs.add( use.getUserDatum( "INGRESSIP" ) );
			// }

			// if (use.getIngressIp() != null)
			// {
			// // if ( gmplsNEMap.get( srcVtx.getUserDatum( "NEID" ) ) == null ) {
			// if (gmplsNEMap.get(srcVtx.getIeee()) == null)
			// {
			// List<String> gmplsArray = new ArrayList<String>();
			// gmplsArray.add(use.getIngressIp());
			// gmplsNEMap.put(srcVtx.getIeee(), gmplsArray);
			// }
			// else
			// {
			// List<String> gmplsArray = gmplsNEMap.get(srcVtx.getIeee());
			// gmplsArray.add(use.getIngressIp());
			// }
			// // }
			// }
			// else if (use.getEgressIp() != null)
			// {
			// if (gmplsNEMap.get(dstVtx.getIeee()) == null)
			// {
			// List<String> gmplsArray = new ArrayList<String>();
			// gmplsArray.add(use.getEgressIp());
			// gmplsNEMap.put(dstVtx.getIeee(), gmplsArray);
			// }
			// else
			// {
			// List<String> gmplsArray = gmplsNEMap.get(dstVtx.getIeee());
			// gmplsArray.add(use.getEgressIp());
			// }
			// }

			if (use.getSourceChannel() != null) {
				sourceChannel = use.getSourceChannel().toString();
			}
			else {
				log.debug("processPathResult: sourceChannel not set, sourceChannel: "
				    + sourceChannel);
			}
			if (use.getTargetChannel() != null) {
				targetChannel = use.getTargetChannel().toString();
			}
			else {
				log.debug("processPathResult:  targetChannel not set, targetChannel: "
				    + targetChannel);
			}

			log.debug("processPathResult: srcVtx: " + srcVtx + " dstVtx: " + dstVtx
			    + " srcPort: " + srcPort + " dstPort: " + dstPort
			    + " sourceChannel: " + sourceChannel + " targetChannel: "
			    + targetChannel);
			if (srcVtx != null && dstVtx != null && srcPort != null
			    && dstPort != null && sourceChannel != null && targetChannel != null) {
				// Don't put the same NE in the list more than once
				if (orderedNeList.size() > 0) {
					lastNE = orderedNeList.get(orderedNeList.size() - 1);

				}
				if (lastNE != null) {

					if (!lastNE.equalsIgnoreCase(srcVtx.getIeee())
					    && !lastNE.equalsIgnoreCase(use.getIngressIp())) {
						if (use.getIngressIp() != null) {
							orderedNeList.add(use.getIngressIp());
						}
						else {
							orderedNeList.add(srcVtx.getIeee());
						}
					}
					if (!lastNE.equalsIgnoreCase(dstVtx.getIeee())
					    && !lastNE.equalsIgnoreCase(use.getIngressIp())) {
						if (use.getIngressIp() != null) {
							orderedNeList.add(use.getIngressIp());
						}
						else {
							orderedNeList.add(dstVtx.getIeee());
						}
					}
					if (lastNE.equalsIgnoreCase(srcVtx.getIeee())) {
						orderedPortList.add(srcPort);
						orderedPortList.add(dstPort);

						orderedChannelList.add(sourceChannel);
						orderedChannelList.add(targetChannel);

						// Signal to the BLSR handler that the edge is flipped.
						edgeFlipped = false;
					}
					else if (lastNE.equalsIgnoreCase(dstVtx.getIeee())) { // Edge
						                                                    // orientation
						                                                    // is flipped.
						orderedPortList.add(dstPort);
						orderedPortList.add(srcPort);

						orderedChannelList.add(targetChannel);
						orderedChannelList.add(sourceChannel);

						// Signal to the BLSR handler that the edge is flipped.
						edgeFlipped = true;
					}
				}

				// BLSR Support
				srcFac = model.getFacility(srcVtx.getIeee(), srcPort);
				dstFac = model.getFacility(dstVtx.getIeee(), dstPort);

				if (srcFac == null || dstFac == null) {
					re = new RoutingException(
					    DracErrorConstants.LPCP_E3021_SRC_OR_DST_FACILITY_NOT_FOUND,
					    new Object[] { srcFac, dstFac });
					throw re;
				}

				LpcpFacility facilities[] = new LpcpFacility[2];

				if (edgeFlipped) {
					facilities[0] = dstFac;
					facilities[1] = srcFac;
				}
				else {
					facilities[0] = srcFac;
					facilities[1] = dstFac;
				}

				for (int j = 0; j < facilities.length; j++) {
					if (facilities[j] != null
					    && !"N/A".equalsIgnoreCase(facilities[j].getAPSId())) {
						if (facilities[j].getAPSId() != null) {
							if (aend == null) {
								aend = facilities[j].getAPSId();
								log.debug("processPathResult: BLSR ENTRY DETECTED, AEND SET TO: "
								    + aend);
							}
							else if (zend == null) {
								zend = facilities[j].getAPSId();
								log.debug("processPathResult: BLSR EXIT DETECTED, ZEND SET TO: "
								    + zend);
							}
						}
					}
				}

				// HACK
				/*
				 * if ( ( sourceChannel != null ) &&
				 * "-1".equalsIgnoreCase(sourceChannel) ) { sourceChannel = "1"; } if (
				 * ( targetChannel != null ) && "-1".equalsIgnoreCase(targetChannel) ) {
				 * targetChannel = "1"; }
				 */
				// orderedChannelList.add( sourceChannel );
				// orderedChannelList.add( targetChannel );
			}
			else {
				log.debug("processPathResult: FATAL ERROR: srcVtx: " + srcVtx
				    + " dstVtx: " + dstVtx + " srcPort: " + srcPort + " dstPort: "
				    + dstPort + " sourceChannel: " + sourceChannel + " targetChannel: "
				    + targetChannel);
				re = new RoutingException(
				    DracErrorConstants.LPCP_E3022_NULL_PARAMETERS_IN_ROUTE_CALC);
				throw re;
			}
		}
		// orderedNeList.add(dest);

		// if (model.getFacilityForTNA(dstTNA) != null &&
		// model.getFacilityForTNA(dstTNA).getParent() != null)
		// {
		// orderedPortList.add(Utility.convertETH2WAN(model.getAIDForTNA(dstTNA)));
		// }
		// else
		{
			orderedPortList.add(model.getAIDForTNA(dstTNA));
		}

		// orderedPortList.add( dstTNA );
		orderedChannelList.add(Integer.toString(dstTNAchannel));

		log.debug("processPathResult: orderedNeList: " + orderedNeList
		    + "\norderedPortList: " + orderedPortList + "\norderedChannelList: "
		    + orderedChannelList + "\nuniqueNEs: " + "\nports: " + ports);
		// "\ngmplsNEMap: "+ gmplsNEMap

		/*
		 * Iterator it = uniqueNEs.iterator(); while ( it.hasNext() ) { Dlog.log(
		 * Dlog.debug, "NE: " + (String)it.next() ); }
		 */
		Element tempElement = null;
		Element pathElement = null;

		if (parameters.get(SPF_KEYS.SPF_KEY) == null) {
			pathRoot = new Element(LpcpConstants.PATH_EVENT_NODE);
			pathInstance = new Element(LpcpConstants.PATH_INSTANCE_NODE);
			pathInstance.setAttribute(LpcpConstants.TYPE_ATTR,
			    (String) parameters.get(SPF_KEYS.SPF_PATHTYPE));
			pathElement = new Element(LpcpConstants.PATH_NODE);
			pathInstance.addContent(pathElement);
			pathRoot.addContent(pathInstance);
		}
		else {
			GenericJdomParser jparser = new GenericJdomParser();
			log.debug("processPathResult: xml to parse: "
			    + (String) parameters.get(SPF_KEYS.SPF_KEY));
			jparser.parse((String) parameters.get(SPF_KEYS.SPF_KEY));
			pathRoot = jparser.getRoot();
			if (pathRoot != null) {
				pathInstance = pathRoot.getChild(LpcpConstants.PATH_INSTANCE_NODE);
				if (pathInstance != null) {
					pathElement = pathInstance.getChild(LpcpConstants.PATH_NODE);
					if (pathElement == null) {
						re = new RoutingException(
						    DracErrorConstants.LPCP_E3023_UNEXPECTED_ERROR_VCAT_PATH_CALC_ELEMENT,
						    new Object[] { pathElement });
						throw re;
					}
				}
				else {
					re = new RoutingException(
					    DracErrorConstants.LPCP_E3024_UNEXPECTED_ERROR_VCAT_PATH_CALC_PATHINSTANCE,
					    new Object[] { pathInstance });
					throw re;
				}
			}
			else {
				re = new RoutingException(
				    DracErrorConstants.LPCP_E3025_UNEXPECTED_ERROR_VCAT_PATH_CALC_PATHROOT,
				    new Object[] { pathRoot });
				throw re;
			}
		}

		log.debug("processPathResult: pathRoot: "
		    + outputter.outputString(pathRoot) + "\npathInstance: "
		    + outputter.outputString(pathInstance) + "\npathElement: "
		    + outputter.outputString(pathElement));

		List<CrossConnection> resultArray = new ArrayList<CrossConnection>();
		String remoteMAC = null; // Remote MAC address for trunk creation
		String cct = null; // CCT for 1+1 path
		String swmate = null; // SWMATE for 1+1 path (placeholder only)

		if (currentShortPath.size() != 0) {
			for (int i = 0; i < orderedNeList.size(); i++) {
				tempElement = new Element(LpcpConstants.EDGE_NODE);
				tempElement.setAttribute(LpcpConstants.SOURCE_ATTR,
				    orderedNeList.get(i));
				tempElement.setAttribute(LpcpConstants.TARGET_ATTR,
				    orderedNeList.get(i));
				tempElement.setAttribute(LpcpConstants.SOURCEPORT_ATTR,
				    orderedPortList.get(2 * i));
				tempElement.setAttribute(LpcpConstants.TARGETPORT_ATTR,
				    orderedPortList.get(2 * i + 1));
				tempElement.setAttribute(LpcpConstants.RATE_ATTR,
				    Utility.convertMB2STS(rate));
				tempElement.setAttribute(LpcpConstants.SOURCECHANNEL_ATTR,
				    orderedChannelList.get(2 * i));
				tempElement.setAttribute(LpcpConstants.TARGETCHANNEL_ATTR,
				    orderedChannelList.get(2 * i + 1));
				pathElement.addContent(tempElement);
				CrossConnection row = null;

				// if (isPBT)
				// {
				// if (srcNeId.equalsIgnoreCase(orderedNeList.get(i)))
				// {
				// remoteMAC = dstNeId;
				// }
				// else if (dstNeId.equalsIgnoreCase(orderedNeList.get(i)))
				// {
				// remoteMAC = srcNeId;
				// }
				// else
				// {
				// remoteMAC = null;
				// }
				// }
				// else
				if (isOnePlusOne) {
					if (srcNeId.equalsIgnoreCase(orderedNeList.get(i))) {
						cct = "2WAYPR";
						// swmate = (String)orderedPortList.get((2*i)+1) + "-" +
						// (String)orderedChannelList.get((2*i)+1);
					}
					else if (dstNeId.equalsIgnoreCase(orderedNeList.get(i))) {
						cct = "2WAYPR";
						// swmate = (String)orderedPortList.get(2*i) + "-" +
						// (String)orderedChannelList.get(2*i);
					}
					else {
						cct = null;
						// swmate = null;
					}
				}

				String vid = Utility.scanForVlanIdFromNeId(parameters,
				    orderedNeList.get(i));

				row = toCrossConnect(model, orderedNeList.get(i), // Source NE
				    orderedNeList.get(i), // Target NE
				    orderedPortList.get(2 * i), // Source Port
				    orderedPortList.get(2 * i + 1), // Target Port
				    rate, // All input rates are in Mb/s
				    orderedChannelList.get(2 * i), // Source Channel
				    orderedChannelList.get(2 * i + 1), // Target Channel
				    "N/A", // Connection ID (not used)
				    aend, // BLSR only AEND NEID
				    zend, // BLSR only ZEND NEID
				    remoteMAC, // PP86K only (MEP for endpoint for trunk)
				    cct, // PP86K only TRUNK REMOTEMAC
				    swmate, // 1+1 path
				    vid // Layer 2
				);
				/*
				 * NOT NEEDED ANYMORE ACCODRING TO THINH - Nov 24, 2006 "", // PP86K
				 * only (UNI endpoint for trunk) "", // PP86K only (ENNI endpoint for
				 * trunk) "", // PP86K only (MEP for endpoint for trunk)
				 * (String)orderedNeList.get(i) }; // PP86K only
				 */
				// }
				// else
				// {
				// String vid = null;
				// vid = Utility.scanForVlanId(parameters, tna);
				// if
				// (gmplsNEMap.get(orderedNeList.get(i)).get(0).equals(parameters.get(SPF_KEYS.SPF_SOURCEID)))
				// {
				// vid = (String) parameters.get(SPF_KEYS.SPF_SRCVLAN);
				// }
				// else if
				// (gmplsNEMap.get(orderedNeList.get(i)).get(0).equals(parameters.get(SPF_KEYS.SPF_TARGETID)))
				// {
				// vid = (String) parameters.get(SPF_KEYS.SPF_DSTVLAN);
				// }
				//
				// row = toCrossConnect(model,
				// gmplsNEMap.get(orderedNeList.get(i)).get(0), // Source
				// gmplsNEMap.get(orderedNeList.get(i)).get(1), // Target
				// orderedPortList.get(2 * i), // Source Port
				// orderedPortList.get(2 * i + 1), // Target Port
				// rate, // All input rates are in Mb/s
				// orderedChannelList.get(2 * i), // Source Channel
				// orderedChannelList.get(2 * i + 1), // Target Channel
				// "N/A", // Connection ID (not used)
				// aend, // BLSR only AEND NEID
				// zend, // BLSR only ZEND NEID
				// remoteMAC, // PP86K only (MEP for endpoint for trunk)
				// cct, // PP86K only TRUNK REMOTEMAC
				// swmate, // 1+1 path
				// vid // Layer 2 PBT, EVPL
				// );
				// /*
				// * NOT NEEDED ANYMORE ACCORDING TO THINH - Nov 24, 2006 "N/A", "", "",
				// "",
				// * (String)orderedNeList.get(i) };
				// */
				// }
				resultArray.add(row);
			}
		}
		else {
			// Hairpin connections
			// Put the TNAs in
			if (srcNeId.equals(dstNeId)) {
				String vid = Utility.scanForVlanIdFromNeId(parameters, srcNeId);

				CrossConnection row = toCrossConnect(model, srcNeId, // Source NE
				    srcNeId, // Target NE
				    model.getAIDForTNA(srcTNA), // Source Port
				    model.getAIDForTNA(dstTNA), // Target Port
				    rate, // All input rates are in Mb/s
				    Integer.toString(srcTNAchannel), // Source Channel
				    Integer.toString(dstTNAchannel), // Target Channel
				    "N/A", // Connection ID (not used)
				    aend, // BLSR only AEND NEID
				    zend, // BLSR only ZEND NEID
				    srcNeId, // PP86K only (MEP for endpoint for trunk)
				    cct, // PP86K only TRUNK REMOTEMAC, in loop-back,
				         // remotemac=localmac=srcNeId
				    swmate, // 1+1 path
				    vid // Layer 2
				);
				resultArray.add(row);
			}
		}

		log.debug("processPathResult: pathRoot after processing: "
		    + outputter.outputString(pathRoot)
		    + "\npathInstance after processing: "
		    + outputter.outputString(pathInstance)
		    + "\npathElement after processing: "
		    + outputter.outputString(pathElement));

		// spfXML.append( "</path>" );
		// spfXML.append( "<edgelist>" );
		Element edgeListElement = null;
		Element edge = null;

		if (parameters.get(SPF_KEYS.SPF_KEY) == null) {
			edgeListElement = new Element(LpcpConstants.EDGELIST_NODE);
			pathInstance.addContent(edgeListElement);
		}
		else {
			edgeListElement = pathInstance.getChild(LpcpConstants.EDGELIST_NODE);
		}

		log.debug("processPathResult: pathRoot after adding edgeListElement: "
		    + outputter.outputString(pathRoot)
		    + "\npathInstance after adding edgeListElement: "
		    + outputter.outputString(pathInstance)
		    + "\npathElement after adding edgeListElement: "
		    + outputter.outputString(pathElement));

		// Only add the edges node once (during VCAT calculations, the edge is
		// always the same)
		if (parameters.get(SPF_KEYS.SPF_KEY) == null) {
			if (currentShortPath.size() != 0) {
				for (int i = 0; i < currentShortPath.size(); i++) {
					edge = new Element(LpcpConstants.EDGE_NODE);
					edge.setAttribute(LpcpConstants.ID_ATTR, currentShortPath.get(i)
					    .getID());
					edgeListElement.addContent(edge);
					// spfXML.append( " <edge id=\"" +
					// ((UndirectedSparseEdge)currentShortPath.get(i)).getUserDatum("ID")
					// + "\"/>" );
				}
			}
		}

		// spfXML.append( " </edgelist>" );
		// Add the source and destination nodes and TNAs to the result
		Element endpointsElement = new Element(LpcpConstants.ENDPOINTS_NODE);
		Element sourceElement = new Element(LpcpConstants.SOURCE_ATTR);
		Element targetElement = new Element(LpcpConstants.TARGET_ATTR);
		sourceElement.setAttribute(LpcpConstants.ID_ATTR, srcNeId);
		sourceElement.setAttribute(LpcpConstants.TNA_ATTR, srcTNA);
		targetElement.setAttribute(LpcpConstants.ID_ATTR, dstNeId);
		targetElement.setAttribute(LpcpConstants.TNA_ATTR, dstTNA);
		endpointsElement.addContent(sourceElement);
		endpointsElement.addContent(targetElement);

		// Only add the endpoints once during VCAT calculcations
		if (pathInstance.getChild(LpcpConstants.ENDPOINTS_NODE) == null) {
			pathInstance.addContent(endpointsElement);
		}

		log.debug("processPathResult: pathRoot after adding endpointsElement: "
		    + outputter.outputString(pathRoot)
		    + "\npathInstance after adding endpointsElement: "
		    + outputter.outputString(pathInstance)
		    + "\npathElement after adding endpointsElement: "
		    + outputter.outputString(pathElement));
		// spfXML.append( "<endpoints>" );
		// spfXML.append( "<source id=\"" + srcNeId + "\" tna=\"" + srcTNA +"\"/>"
		// );
		// spfXML.append( "<target id=\"" + dstNeId + "\" tna=\"" + dstTNA +"\"/>"
		// );
		// spfXML.append( "</endpoints>" );

		Element statusElement = new Element(LpcpConstants.STATUS_NODE);
		statusElement.setAttribute(
		    LpcpConstants.TEXT_ATTR,
		    "Path from: " + model.getTidForNeId(srcNeId) + " to "
		        + model.getTidForNeId(dstNeId) + ", cost: " + pathCost + " hops: "
		        + currentShortPath.size());
		// spfXML.append( "<status text=\"Path from: " +
		// model.getTidForNeId(srcNeId) + " to " +
		// model.getTidForNeId( dstNeId ) + ", cost: " + pathCost + " hops: " +
		// currentShortPath.size() +
		// "\"/>" );
		// spfXML.append( "</pathEvent>" );

		// Only add the status element once during VCAT calculations
		if (pathInstance.getChild(LpcpConstants.STATUS_NODE) == null) {
			pathInstance.addContent(statusElement);
		}
		log.debug("processPathResult: pathRoot after adding status: "
		    + outputter.outputString(pathRoot));

		parameters.put(SPF_KEYS.SPF_KEY, outputter.outputString(pathRoot)); // spfXML.toString()
		                                                                    // );

		log.debug("processPathResult: Shortest path is: " + currentShortPath
		    + " path cost: " + pathCost);

		result.put(SPF_KEYS.SPF_PATHKEY, currentShortPath);
		// result.put(LpcpConstants.COST_KEY, Integer.toString(pathCost));
		// result.put(LpcpConstants.HOPS_KEY,
		// Integer.toString(currentShortPath.size() + 1));
		// If the map already contains a resultArray PATH_LIST_KEY then append to it
		if (parameters.get(SPF_KEYS.SPF_RT_PATH_DATA) != null) {
			List<CrossConnection> currentResultArray = (List<CrossConnection>) parameters
			    .get(SPF_KEYS.SPF_RT_PATH_DATA);
			currentResultArray.addAll(resultArray);
			result.put(SPF_KEYS.SPF_RT_PATH_DATA, currentResultArray);
		}
		else {
			result.put(SPF_KEYS.SPF_RT_PATH_DATA, resultArray); // The result of the
			                                                    // cumulative path
			// calculations
		}
		result.put(SPF_KEYS.SPF_CUR_PATH_LIST_KEY, resultArray); // The result of
		                                                         // the current path
		// calculation for VCAT
		result.put(SPF_KEYS.SPF_KEY, outputter.outputString(pathRoot));

		parameters.put(SPF_KEYS.SPF_PATHKEY, currentShortPath);
		// parameters.put(LpcpConstants.COST_KEY, Integer.toString(pathCost));
		// parameters.put(LpcpConstants.HOPS_KEY,
		// Integer.toString(currentShortPath.size() + 1));
		// If the parameters map already contains a resultArray RT_PATH_DATA then
		// append to it
		if (parameters.get(SPF_KEYS.SPF_RT_PATH_DATA) == null) {
			parameters.put(SPF_KEYS.SPF_RT_PATH_DATA, resultArray);
		}

		// Put the list of JUNG edges in the result so that it can be used to
		// calculate diverse paths
		parameters.put(SPF_KEYS.SPF_JUNG_EDGELIST, jungEdges);
		result.put(SPF_KEYS.SPF_JUNG_EDGELIST, jungEdges);
		/*
		 * if ( parameters.get( PATH_LIST_KEY ) != null ) { ArrayList
		 * currentResultArray = (ArrayList)result.get( PATH_LIST_KEY ); // The
		 * result of the cumulative path calculations currentResultArray.addAll(
		 * resultArray ); parameters.put( PATH_LIST_KEY, resultArray ); } else {
		 * parameters.put( PATH_LIST_KEY, resultArray ); }
		 */
		parameters.put(SPF_KEYS.SPF_CUR_PATH_LIST_KEY, resultArray); // The result
		                                                             // of the
		                                                             // current path
		// calculation for VCAT

		return result;

	}

	private boolean checkEndpoints(LpcpFacility srcFacility,
	    LpcpFacility dstFacility) throws RoutingException {
		boolean endpointsOK = true;
		RoutingException re = null;

		// Check that the endpoints are UNI or ENNI
		if (!FacilityConstants.SIGNAL_TYPE.UNI.toString().equals(
		    srcFacility.getSigType())
		    && !FacilityConstants.SIGNAL_TYPE.ENNI.toString().equals(
		        srcFacility.getSigType())) {
			log.error("Invalid source endpoint signalingType: " + srcFacility);
			re = new RoutingException(
			    DracErrorConstants.LPCP_E3043_INVALID_SRC_ENDPOINT_SIGTYPE,
			    new String[] { srcFacility.getAid() });
			throw re;
		}

		if (!FacilityConstants.SIGNAL_TYPE.UNI.toString().equals(
		    dstFacility.getSigType())
		    && !FacilityConstants.SIGNAL_TYPE.ENNI.toString().equals(
		        dstFacility.getSigType())) {
			log.error("Invalid destination endpoint signalingType: " + dstFacility);
			re = new RoutingException(
			    DracErrorConstants.LPCP_E3044_INVALID_DST_ENDPOINT_SIGTYPE,
			    new String[] { dstFacility.getAid() });
			throw re;
		}

		// Check for non-matched endpoints
		if (Facility.isL2(srcFacility.getType())
		    && Facility.isL2(dstFacility.getType())) {
			LpcpFacility.VCAT_ROUTING_TYPE srcRoutingType = srcFacility
			    .getVCATRoutingType();
			LpcpFacility.VCAT_ROUTING_TYPE dstRoutingType = dstFacility
			    .getVCATRoutingType();

			// Both end points are ethernet ports - make sure they are either both
			// VCAT capable or both not
			boolean srcVcatCapable = srcRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_VCAT
			    || srcRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.CAN_BE_EITHER;

			boolean dstVcatCapable = dstRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.MUST_BE_VCAT
			    || dstRoutingType == LpcpFacility.VCAT_ROUTING_TYPE.CAN_BE_EITHER;

			endpointsOK = srcVcatCapable == dstVcatCapable;

			if (!endpointsOK) {
				log.error("Detected incompatible endpoints.  srcFacility VCAT is: "
				    + srcVcatCapable + " dstFacility VCAT is: " + dstVcatCapable);
				re = new RoutingException(
				    DracErrorConstants.LPCP_E3032_SRC_AND_DEST_PORT_CAPABILITIES_MISMATCH,
				    new String[] { Boolean.toString(srcVcatCapable),
				        Boolean.toString(dstVcatCapable) });
				// Throw the exception
				throw re;
			}
		}

		return endpointsOK;

	}

	/**
	 * Returns the next available channel that can satisfy the requested bandwidth
	 * if initdone is true and the input channel if initdone is false.
	 *
	 * @param String channel - channel number to check or -1 to pick first
	 * available channel
	 *
	 * @param BitBandwidthTrackerI tracker - the bandwidth tracker to check
	 *
	 * @param String rate - the rate to use
	 *
	 * @param boolean initdone - in VCAT calculations, indicates whether the first
	 * iteration was done already. On the first iteration, the specified
	 * <code>channel</code> must be used. On subsequent iterations, the next
	 * available from the <code>channel</code> is required.
	 *
	 * @return int - The input <code>channel</code> if the bandwidth is available
	 * and firstVCATMemberDone is false The next available channel from
	 * <code>channel</code> if the bandwidth is available and firstVCATMemberDone
	 * is true -1 if no channel is found.
	 */
	private int getEndChannel(String channel, BasicTracker tracker,
	    int trackerRate, Map<SPF_KEYS, Object> parameters) throws Exception {

		log.debug("getEndChannel: channel:" + channel + " trackerRate:"
		    + trackerRate + " tracker:" + tracker + " parameters:" + parameters);

		int rc;

		if (channel == null || "null".equals(channel) || "-1".equals(channel)) {
			/**
			 * Auto pick the channel if -1 or null, or not specified, return find
			 * first available channel or -1 if no bandwidth is available.
			 */

			rc = tracker.getNextChannel(trackerRate, parameters);
			log.debug("getEndChannel: returns " + rc + " channel:" + channel
			    + " trackerRate:" + trackerRate + " tracker:" + tracker
			    + " parameters:" + parameters);
			return rc;
		}

		rc = tracker.getNextChannel(channel, trackerRate, parameters);

		if (rc != -1) {
			if (tracker instanceof SonetTrackerI) {
				SonetTrackerI t = (SonetTrackerI) tracker;
				if (!t.isBoundaryChannel(Integer.parseInt(channel), trackerRate)) {
					throw new RoutingException(
					    DracErrorConstants.LPCP_E3013_DST_CHANNEL_NOT_ON_BOUNDARY);
				}
			}
		}

		log.debug("getEndChannel: returns " + rc + " channel:" + channel
		    + " trackerRate:" + trackerRate + " tracker:" + tracker
		    + " parameters:" + parameters);
		return rc;
	}

	/*
	 * resolveNeId use the unique tna to resolve the owning NE object and return
	 * it's neid.
	 */
	private String resolveNeId(String tna, HierarchicalModel model)
	    throws RoutingException {
		return model.getNeIdForTNA(tna);
	}

	private CrossConnection toCrossConnect(HierarchicalModel model,
	    String srcNe, String targetNe, String sourcePort, String targetPort,
	    String rate, String sourceChannel, String targetChannel,
	    String connectionId, String blsrA, String blsrZ, String remoteMac,
	    String cct, String swmate, String vid) throws Exception {

		Map<String, String> m = new HashMap<String, String>();
		m.put(CrossConnection.SOURCE_NEID, srcNe);
		m.put(CrossConnection.TARGET_NEID, targetNe);

		m.put(CrossConnection.SOURCE_PORT_AID, sourcePort);
		m.put(CrossConnection.TARGET_PORT_AID, targetPort);

		m.put(CrossConnection.RATE, Utility.convertMB2STS(rate)); // all rates are
		                                                          // in mbs but
		                                                          // layer 2 cross
		// connnects want STS numbers.
		m.put(CrossConnection.RATE_IN_MBS, rate);

		m.put(CrossConnection.SOURCE_CHANNEL, sourceChannel);
		m.put(CrossConnection.TARGET_CHANNEL, targetChannel);

		m.put(CrossConnection.CKTID, connectionId);

		m.put(CrossConnection.BLSR_AEND, blsrA);
		m.put(CrossConnection.BLSR_ZEND, blsrZ);

		m.put(CrossConnection.REMOTEMAC, remoteMac);
		m.put(CrossConnection.CCT_TYPE, cct);

		if (swmate != null && swmate.length() > 0) {
			m.put(CrossConnection.SWMATE_NEID, srcNe);
			m.put(CrossConnection.SWMATE_XC_AID, swmate);
		}

		/*
		 * When provisioning a layer 2 service the user may supply a vlan id, but we
		 * don't need to include that VLAN id to every XC in the path as we may be
		 * going over a layer 1 xc. Omit the vlan id unless we need it.
		 */
		LpcpFacility srcFac = model.getFacility(srcNe, sourcePort);
		LpcpFacility dstFac = model.getFacility(srcNe, targetPort);
		BasicTracker srct = srcFac.getTracker();
		BasicTracker dstt = dstFac.getTracker();

		boolean wantVlanInConnection = false;
		if (srct instanceof EthTrackerI) {
			if (((EthTrackerI) srct).supportsMultipleServiceFlows()) {
				wantVlanInConnection = true;
			}
		}

		if (dstt instanceof EthTrackerI) {
			if (((EthTrackerI) dstt).supportsMultipleServiceFlows()) {
				wantVlanInConnection = true;
			}
		}

		if (wantVlanInConnection) {
			if (vid == null) {
				log.error("ERROR: Creating a CrossConnect that needs a vlan id, but none is present!"
				    + m);
			}
			m.put(CrossConnection.VLANID, vid);
		}
		return new CrossConnection(m);
	}

	/**
	 * Performs validation on the specified TNAs. If validation is successful, the
	 * first available channel on the srcTNA and dstTNA are stored in the input
	 * resultMap. Also stored in the resultMap is the converted trackerRate
	 * (Utility.STS1, Utility.STS3C, Utility.STS24C, ... )
	 *
	 * @param String srcNeid - ne id of source NE, must not be null
	 *
	 * @param String dstNeid - ne id of destination NE, must not be null
	 *
	 * @param String srcTNA - TNA on source NE, must not be null
	 *
	 * @param String dstTNA - TNA on destination NE, must not be null
	 *
	 * @param String rate - requested rate in Mb/s
	 *
	 * @param HierarchicalModel model - The model to user
	 *
	 * @param Map resultMap - The Map that will store the calculated
	 * srcTNAchannel, dstTNAchannel Map contains: SRCTNACHANNEL_KEY = Integer
	 * source TNA channel DSTTNACHANNEL_KEY = Integer destination TNA channel
	 *
	 * @return resultMap - see above
	 */
	private void validateTNAs(String srcNeid, String dstNeid, String srcTNA,
	    String dstTNA, String rate, String srcChannel, String dstChannel,
	    HierarchicalModel model, Map<String, Integer> resultMap,
	    Map<SPF_KEYS, Object> parameters) throws RoutingException {

		boolean firstVCATMember = parameters.get(SPF_KEYS.SPF_FIRST_VCAT_DONE) == null;
		boolean vcat_user_request = "true".equalsIgnoreCase((String) parameters
		    .get(SPF_KEYS.SPF_VCATROUTING_OPTION));

		// Check for TNAs being the same. This can happen if the src and dst are the
		// same.
		if (srcTNA != null && dstTNA != null && srcTNA.equals(dstTNA)) {
			log.error("validateTNAs: Detected identical source and destination TNAs.");
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3007_PORTS_CANNOT_BE_IDENTICAL);
		}

		if (model.getModel() == null) {
			log.error("validateTNAs: model is null!");
			throw new RoutingException(DracErrorConstants.LPCP_E3019_MODEL_NOT_READY);
		}

		Map<String, LpcpFacility> srcFacMap = model.getModel().get(srcNeid);
		Map<String, LpcpFacility> dstFacMap = model.getModel().get(dstNeid);

		if (srcFacMap == null || dstFacMap == null) {
			log.error("validateTNAs: Source and/or Dest facility maps are null, cannot proceed src:"
			    + srcNeid + " dst:" + dstNeid);
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3018_SRC_OR_DST_FACILITY_MAP_NOT_FOUND,
			    new String[] { srcNeid, dstNeid });
		}

		LpcpFacility srcFacility = model.getFacilityForTNA(srcTNA);
		LpcpFacility dstFacility = model.getFacilityForTNA(dstTNA);

		if (srcFacility == null || dstFacility == null) {
			log.error("validateTNAs: Source and/or Dest facility are null, cannot proceed src:"
			    + srcFacility + " dst:" + srcFacility);
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3017_SRC_OR_DST_PORT_FACILITY_NOT_FOUND);
		}

		/**
		 * Perform validation on the selected endpoints checkEndpoints will throw a
		 * RoutingException if something is wrong
		 */
		checkEndpoints(srcFacility, dstFacility);

		BasicTracker srcTNAtracker = srcFacility.getTracker();
		BasicTracker dstTNAtracker = dstFacility.getTracker();

		if (srcTNAtracker == null || dstTNAtracker == null) {
			log.error("validateTNAs: No tracker associated with src or dst facility. \n srcFacility: "
			    + srcFacility
			    + " dstFacility: "
			    + dstFacility
			    + "\nsrcTNAtracker: "
			    + srcTNAtracker + " dstTNAtracker: " + dstTNAtracker);
			/* we reuse this exception if we can't find the port or the tracker */
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3017_SRC_OR_DST_PORT_FACILITY_NOT_FOUND);
		}

		int srcTNAchannel = -1;
		int dstTNAchannel = -1;
		try {
			if (srcTNAtracker instanceof EthTrackerI) {
				EthTrackerI ethTracker = (EthTrackerI) srcTNAtracker;

				if (vcat_user_request && firstVCATMember || !vcat_user_request) {
					String srcVlanId = (String) parameters.get(SPF_KEYS.SPF_SRCVLAN);
					if (srcVlanId != null && srcVlanId.trim().length() > 0) {
						if (!ethTracker.isVlanAvailable(srcVlanId)) {
							throw new RoutingException(
							    DracErrorConstants.LPCP_E3051_USED_SRC_VLANID);
						}
					}
				}
			}

			if (dstTNAtracker instanceof EthTrackerI) {
				EthTrackerI ethTracker = (EthTrackerI) dstTNAtracker;

				if (vcat_user_request && firstVCATMember || !vcat_user_request) {
					String dstVlanId = (String) parameters.get(SPF_KEYS.SPF_DSTVLAN);
					if (dstVlanId != null && dstVlanId.trim().length() > 0) {
						if (!ethTracker.isVlanAvailable(dstVlanId)) {
							throw new RoutingException(
							    DracErrorConstants.LPCP_E3052_USED_DST_VLANID);
						}
					}
				}
			}

			if (srcTNAtracker instanceof WavelengthTracker
			    && dstTNAtracker instanceof WavelengthTracker) {
				String srcWavelength = srcFacility.getExtendedAttributes().get(
				    FacilityConstants.WAVELENGTH_ATTR);
				String dstWavelength = dstFacility.getExtendedAttributes().get(
				    FacilityConstants.WAVELENGTH_ATTR);
				if (srcWavelength != null
				    && !srcWavelength.equalsIgnoreCase(dstWavelength)) {
					throw new RoutingException(
					    DracErrorConstants.LPCP_E3048_WAVELENGTH_MISMATCH, new String[] {
					        srcWavelength, dstWavelength });
				}
			}

			int trackerRate;
			try {
				trackerRate = Utility.convertStringRateToInt(rate);

				srcTNAchannel = getEndChannel(srcChannel, srcTNAtracker, trackerRate,
				    parameters);
				dstTNAchannel = getEndChannel(dstChannel, dstTNAtracker, trackerRate,
				    parameters);
			}
			catch (RoutingException rep) {
				log.error(
				    "validateTNAs: Exception determining start or destination channel",
				    rep);
				throw rep;
			}
			catch (Exception e) {
				log.error(
				    "validateTNAs: Exception determining start or destination channel",
				    e);
				throw new RoutingException(
				    DracErrorConstants.LPCP_E3014_ERROR_DETERMINING_SRC_OR_DST_CHANNEL,
				    e);
			}

			// If the requested channel is not available, throw routing exception
			if (vcat_user_request && firstVCATMember || !vcat_user_request) {
				if (srcChannel != null && !"-1".equals(srcChannel)
				    && !srcChannel.equals(Integer.toString(srcTNAchannel))) {
					log.error("validateTNAs: requested source channel " + srcChannel
					    + " not available");
					throw new RoutingException(
					    DracErrorConstants.LPCP_E3055_ERROR_SRC_CHANNEL_NOTAVAILABLE);
				}
				if (dstChannel != null && !"-1".equals(dstChannel)
				    && !dstChannel.equals(Integer.toString(dstTNAchannel))) {
					log.error("validateTNAs: requested dest channel " + dstChannel
					    + " not available");
					throw new RoutingException(
					    DracErrorConstants.LPCP_E3056_ERROR_DST_CHANNEL_NOTAVAILABLE);
				}
			}

			log.debug("validateTNAs: TNA channels for rate: " + rate
			    + " trackerRate: " + trackerRate + " srcTNAchannel: " + srcTNAchannel
			    + " dstTNAchannel: " + dstTNAchannel + "\nsrcTNA facility: "
			    + srcFacility + "\ndstTNA facility: " + dstFacility
			    + "\nsrcTNAtracker: " + srcTNAtracker + "\ndstTNAtracker: "
			    + dstTNAtracker);
		}
		catch (RoutingException routingException) {
			log.error("validateTNAs: Routing exception occurred:", routingException);
			throw routingException;
		}
		catch (Exception e) {
			log.error("validateTNAs: Unexpected routing exception:", e);
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3040_UNEXPECTED_ROUTING_EXCEPTION,
			    new String[] { e.getMessage() }, e);
		}

		if (srcTNAchannel == -1) {
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3016_NO_AVAILABLE_BANDWIDTH_SRC);
		}

		if (dstTNAchannel == -1) {
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3015_NO_AVAILABLE_BANDWIDTH_DST);
		}

		// Put the calculated results into the resultMap
		resultMap.put(LpcpConstants.SRCTNACHANNEL_KEY,
		    Integer.valueOf(srcTNAchannel));
		resultMap.put(LpcpConstants.DSTTNACHANNEL_KEY,
		    Integer.valueOf(dstTNAchannel));
	}
}
