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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.RoutingException;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;
import com.nortel.appcore.app.drac.common.types.GraphData;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.common.utility.StringParser;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.server.lpcp.Lpcp;
import com.nortel.appcore.app.drac.server.lpcp.common.Utility;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Locations in which it is known that network topology could change should
 * 'requestConsolidation' (e.g. add/del NE, rept-adj, etc). Need to maintain
 * vertex states in this graph: by providing getVertex, called from
 * Lpcp.processAssociationEvent setVertexState, setVertexMode Need to maintain
 * edge constraints in this graph: by providing 'getEdge', called from
 * Lpcp::editFacility.
 */

public enum TopologyManager {
  INSTANCE;
  private final Logger log = LoggerFactory.getLogger(getClass());
	private void consolidate() throws Exception {
		List<NetworkElementHolder> nes = DbNetworkElement.INSTANCE
		    .retrieveAll();
		List<NetworkElementAdjacency> adjList = DbNetworkElementAdjacency
		    .INSTANCE.retrieve();
		consolidate(nes, adjList);
	}

	class ConsolidationTask implements Runnable {
		private boolean freshRequest = false;

		@Override
		public void run() {
			try {
				try {
					freshRequest = false;
					consolidate();
					Uninterruptibles.sleepUninterruptibly(CONSOLIDATION_DELAY_MILLIS, TimeUnit.MILLISECONDS);
				}
				catch (Exception e) {
					log.error("TopologyManager consolidation error.", e);
				}
				if (freshRequest) {
					scheduleMyTask();
				}
			}
			catch (Exception t) {
				log.error("Major problem with the consolidation task, runnable exits!",
				    t);
			}
		}

		public void setRefresh() {
			this.freshRequest = true;
		}
	}

	class DracGraph {
		private Graph<DracVertex, DracEdge> graph = new UndirectedSparseMultigraph<DracVertex, DracEdge>();
		private int nextEdgeId;
		private int nextVertexId;

		// Maintain a list of manual links overlapped by their
		// network-discovered equivalents
		private List<DracEdge> eclipsedManualEdges = new ArrayList<DracEdge>();

		public List<DracEdge> getEclipsedManualEdges() {
			return eclipsedManualEdges;
		}

		Graph<DracVertex, DracEdge> getGraph() {
			return graph;
		}

		private void addNewEdge(String source, String dest, String srcPort,
		    String dstPort, boolean isManual, boolean hasEclipsedManualLink) {
			DracVertex src = getVertex(source);
			DracVertex dst = getVertex(dest);

			if (src == null || dst == null) {
				log.warn("DracVertex not found for source = "
				    + source + " and dest = " + dest);
				return;
			}

			if (findEdge(source, srcPort, dest, dstPort) == null) {
				// Retrieve and consolidate provisioned routing constraints
				// (cost, metrics2, srlg) from the
				// edge
				// ports, to apply them to the edge
				Map<String, String> edgeConstraints = getEdgeConstraints(source,
				    srcPort, dest, dstPort);

				if (edgeConstraints == null) {

					return;
				}

				int edgeId = nextEdgeId++;

				DracEdge edge = new DracEdge(src, srcPort, dst, dstPort,
				    new Double(1.0), edgeConstraints.get(FacilityConstants.COST_ATTR),
				    edgeConstraints.get(FacilityConstants.METRIC_ATTR),
				    edgeConstraints.get(FacilityConstants.SRLG_ATTR),
				    Integer.toString(edgeId));

				edge.setManual(isManual);
				graph.addEdge(edge, new Pair<DracVertex>(src, dst));

				// Create the eclipsed edge and appropriate linkages
				if (hasEclipsedManualLink) {
					// sanity check
					if (isManual) {
						log.error("Manual link has been created with a reference to an eclipsed manual link: source: "
						    + source
						    + " srcPort: "
						    + srcPort
						    + " dest: "
						    + dest
						    + " dstPort: " + dstPort);
					}
					else {
						// NOTE: We don't (at the moment) really have to create
						// an actual INSTANCE of Edge
						// to track an eclipsed manual link; it would be enough
						// simply to mark the network
						// link with a flag (and then derive the eclipsed link
						// at the client). However, I'll
						// create it anyways, in case we want to tweak
						// attributes on the eclipsed link at some
						// later point.

						// First, flag the network link:
						edge.setEclipsedManualLink(true);

						// Create eclipsed edge. It is assigned the same edgeId
						// for navigation

						edge = new DracEdge(src, srcPort, dst, dstPort, new Double(1.0),
						    edgeConstraints.get(FacilityConstants.COST_ATTR),
						    edgeConstraints.get(FacilityConstants.METRIC_ATTR),
						    edgeConstraints.get(FacilityConstants.SRLG_ATTR),
						    Integer.toString(edgeId));
						edge.setManual(true);
						eclipsedManualEdges.add(edge);
					}
				}
			}
			else {
				log.debug("addEdge duplicate edge being added: source: "
				    + source
				    + " srcPort: "
				    + srcPort
				    + " dest: "
				    + dest
				    + " dstPort: "
				    + dstPort);
			}
		}

		private void addNewVertex(String label, String ieee, String ip,
		    String port, String mode, NeType type, String userid,
		    CryptedString password, NeStatus status, Double positionX,
		    Double positionY) {
			DracVertex vertex = null;
			// Map<String, String> vertexMap = new HashMap<String, String>();
			try {
				vertex = new DracVertex(label, ieee, ip, port, mode, type, userid,
				    password, status, Integer.toString(nextVertexId++), positionX,
				    positionY);
				graph.addVertex(vertex);
			}

			catch (Exception e) {
				log.error("Exception addVertex: ", e);
			}
		}

		private DracEdge findEdge(String source, String srcPort, String dest,
		    String dstPort) {
			DracEdge edge = null;

			try {
				for (DracEdge e : graph.getEdges()) {
					String edgeSrcNeID = e.getSource().getIeee();
					String edgeDstNeID = e.getTarget().getIeee();
					String edgeSrcPort = e.getSourceAid();
					String edgeDstPort = e.getTargetAid();

					if (source.equalsIgnoreCase(edgeSrcNeID)
					    && srcPort.equalsIgnoreCase(edgeSrcPort)
					    && dest.equalsIgnoreCase(edgeDstNeID)
					    && dstPort.equalsIgnoreCase(edgeDstPort)) {
						edge = e;
						break;
					}

					else if (source.equalsIgnoreCase(edgeDstNeID)
					    && srcPort.equalsIgnoreCase(edgeDstPort)
					    && dest.equalsIgnoreCase(edgeSrcNeID)
					    && dstPort.equalsIgnoreCase(edgeSrcPort)) {
						edge = e;
						break;
					}
				}
			}
			catch (Exception e) {
				log.error("findEdge error.", e);
			}

			return edge;
		}

		private DracVertex getVertex(String ieee) {
			for (DracVertex v : graph.getVertices()) {
				if (v.getIeee().equals(ieee)) {
					return v;
				}
			}
			return null;
		}
	}

	static class Link {
		private final String sourceNe;
		private final String sourcePort;
		private final String destNe;
		private final String destPort;
		private final boolean isManualLink;
		private boolean hasEclipsedManualLink;

		public Link(String srcNe, String srcPort, String dstNe, String dstPort,
		    boolean isManual) {
			sourceNe = srcNe;
			sourcePort = srcPort;
			destNe = dstNe;
			destPort = dstPort;
			isManualLink = isManual;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof Link) {
				Link other = (Link) o;
				if ((sourceNe.equals(other.getSourceNe())
				    && sourcePort.equalsIgnoreCase(other.getSourcePort())
				    && destNe.equals(other.getDestNe())
				    && destPort.equalsIgnoreCase(other.getDestPort())

				|| sourceNe.equals(other.getDestNe())
				    && sourcePort.equalsIgnoreCase(other.getDestPort())
				    && destNe.equals(other.getSourceNe())
				    && destPort.equalsIgnoreCase(other.getSourcePort()))

				    && this.isManualLink() == other.isManualLink()

				    &&

				    this.hasEclipsedManualLink() == other.hasEclipsedManualLink()

				)

				{
					return true;
				}
			}

			return false;
		}

		public String getDestNe() {
			return destNe;
		}

		public String getDestPort() {
			return destPort;
		}

		public String getSourceNe() {
			return sourceNe;
		}

		public String getSourcePort() {
			return sourcePort;
		}

		public boolean hasEclipsedManualLink() {
			// return eclipsedManualLink != null;
			return hasEclipsedManualLink;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (sourceNe == null ? 0 : sourceNe.hashCode());
			result = prime * result
			    + (sourcePort == null ? 0 : sourcePort.hashCode());
			result = prime * result + (destNe == null ? 0 : destNe.hashCode());
			result = prime * result + (destPort == null ? 0 : destPort.hashCode());
			result = prime * result + Boolean.valueOf(isManualLink).hashCode();

			// Don't hash on hasEclipsedManualLink. It may be modified after
			// having been hashed.

			return result;
		}

		public boolean isManualLink() {
			return isManualLink;
		}

		public void setEclipsedManualLink(boolean b) {
			hasEclipsedManualLink = b;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Link [sourceNe=");
			builder.append(sourceNe);
			builder.append(", sourcePort=");
			builder.append(sourcePort);
			builder.append(", destNe=");
			builder.append(destNe);
			builder.append(", destPort=");
			builder.append(destPort);
			builder.append(", isManualLink=");
			builder.append(isManualLink);
			builder.append("]");
			return builder.toString();
		}
	}

	private static long CONSOLIDATION_DELAY_MILLIS = Long.getLong(
	    "drac.DRACTopologyManager.consolidationDelay", 4500L).longValue();

	private Lpcp lpcp = null;
	private final ConsolidationTask consolidationTask = new ConsolidationTask();
	private ScheduledExecutorService executor = Executors
	    .newSingleThreadScheduledExecutor(new ThreadFactory() {
		    @Override
		    public Thread newThread(Runnable runnable) {
			    return new Thread(runnable);
		    }
	    });

	private ScheduledFuture<?> runningTask = null;

	// The graph will be manipulated during routing requests
	private DracGraph dracRoutingGraph = new DracGraph();

	// Master copy of topology (separate from manipulated graph)
	private Collection<DracVertex> verticesMasterList = new ArrayList<DracVertex>();
	private Collection<DracEdge> edgesMasterList = new ArrayList<DracEdge>();

	// Used exclusively for restoring from routing constraints.
	// i.e. routing.Engine::resetExcludedEdges
	public synchronized void addEdgeToGraph(DracEdge edge) {
		dracRoutingGraph.getGraph().addEdge(edge,
		    new Pair<DracVertex>(edge.getSource(), edge.getTarget()));
	}

	// Used exclusively for Lpcp junit testcases.
	public synchronized void addVertexToGraph(DracVertex v) { // NO_UCD
		dracRoutingGraph.getGraph().addVertex(v);
	}

	public synchronized void findEdge(Set<DracEdge> listToStoreEdges,
	    String source, String srcPort, String dest, String dstPort) {
		if (listToStoreEdges != null) {
			DracEdge e = findEdgeForExclusion(source, srcPort, dest, dstPort);

			if (e != null) {
				listToStoreEdges.add(e);
			}
		}
	}

    /**
     * When a facility is edited and updated in the model, any changes to
     * routing constraints need to be applied into the graph. This method is
     * called from Lpcp::editFacility().
     *
     * @param neid
     * @param aid
     * @return
     */
	public synchronized DracEdge getEdge(String neid, String aid) {
		DracEdge edge = null;

		for (DracEdge e : dracRoutingGraph.getGraph().getEdges()) {
			String srcNEID = e.getSource().getIeee();
			String tgtNEID = e.getTarget().getIeee();

			String srcPort = e.getSourceAid();
			String tgtPort = e.getTargetAid();

			if (neid.equalsIgnoreCase(srcNEID) && srcPort.equalsIgnoreCase(aid)
			    || neid.equalsIgnoreCase(tgtNEID) && tgtPort.equalsIgnoreCase(aid)) {
				edge = e;
				break;
			}
		}

		return edge;
	}

	/**
	 * Returns the 'master' set of vertices and edges. Though thread control
	 * should ensure that this retrieval request would never execute whilst a
	 * routing query is running (with edges potentially removed).
	 */
	public synchronized GraphData getGraphData() {
		try {
			consolidate();
		}
		catch (Exception e) {
			log.error("Consolidation failed", e);
		}

		GraphData graphData = new GraphData();
		graphData.setVertices(this.verticesMasterList);
		graphData.setEdges(this.edgesMasterList);
		graphData.setEclipsedEdges(dracRoutingGraph.getEclipsedManualEdges());
		return graphData;
	}

	public synchronized NeType getNeType(String neid) {
		DracVertex v = getVertex(neid);
		if (v != null) {
			return v.getType();
		}
		log.error("getNeMode - neid not found in model: "
		    + neid);
		return NeType.UNKNOWN;
	}

    /**
     * Find a path that meets the provided criteria.  All the important work is
     * delegated to a wrapper around Jung Graph.
     *
     * @param rate
     * @param dstTNAchannel
     * @param srcTNAchannel
     * @param hopInt
     * @param engine
     * @param edgeWeightWorker Invoked by Jung whenever an edge is visited.  All
     * the smarts and data for the choosing of an edge is in this object.
     * @param src
     * @param dst
     * @param parameters
     * @param model
     * @return
     * @throws Exception
     */
	public synchronized Map<SPF_KEYS, Object> getPath(
            String rate,
            int dstTNAchannel,
            int srcTNAchannel,
            int hopInt,
            Engine engine,
            UserDateEdgeValue edgeWeightWorker,
            DracVertex src,
            DracVertex dst,
            Map<SPF_KEYS, Object> parameters,
            HierarchicalModel model) throws Exception {

        /* Create a new routing engine using the current OpenDRAC graph. */
		RoutingAlgorithm router = new RoutingAlgorithm(dracRoutingGraph.getGraph(), edgeWeightWorker);

        /* Get the shortest path. */
		List<DracEdge> currentShortPath = router.getPath(src, dst, RoutingAlgorithm.DIJKSTRA_SPF);

        /* See if we encountered any excluded edges on the shortest path. */
		Set<DracEdge> toRemove = edgeWeightWorker.getAndClearExcludedEdges();

        /* Exclude these edges from the routing engine. */
		engine.excludeEdges(toRemove);

        /* Loop until we find a path that has no excluded edges. */
		while (engine.containsExcludedEdge(currentShortPath)) {
            /* @TODO John - Why are we waiting 1 second here?? */
			this.wait(1000L);

            /* Get the shortest path. */
			currentShortPath = router.getPath(src, dst, RoutingAlgorithm.DIJKSTRA_SPF);

            /* See if we encountered any excluded edges on the shortest path. */
			toRemove = edgeWeightWorker.getAndClearExcludedEdges();

            /* Exclude these edges from the routing engine. */
			engine.excludeEdges(toRemove);
		}

        /* Check to see if we exceeded the max hop count witht he shortest path. */
		if (hopInt != -1) {
            /* Do we have a shortest path? */
			if (currentShortPath != null) {
                /* Is the shortest path longer than the max specified? */
				if (currentShortPath.size() > hopInt) {
                    /* Generate a rounting exception since we can't find a path. */
					RoutingException re = new RoutingException(
					    DracErrorConstants.LPCP_E3029_NO_PATH_FOR_HOP_COUNT,
					    new String[] { "" + hopInt });
					throw re;
				}
			}
		}

        /* This that looks good so return it. */
		Map<SPF_KEYS, Object> result = engine.processPathResult(model, this,
		    parameters, currentShortPath, srcTNAchannel, dstTNAchannel, rate,
		    edgeWeightWorker);
		return result;
	}

	public synchronized List<DracEdge> getRoutingEdges() {
		/*
		 * return a shallow copy. Typically caller will iterate this returned
		 * collection and subsequently call back to remove an edge from the
		 * underlying graph (based on routing constraints)
		 */
		return new ArrayList<DracEdge>(dracRoutingGraph.getGraph().getEdges());
	}

	// Called during edge creation.
	// Called from Lpcp::processAssociationEvent for updating status
	public synchronized DracVertex getVertex(String ieee) {
		return dracRoutingGraph.getVertex(ieee);
	}

	public synchronized Collection<DracVertex> getVertices() {
		return dracRoutingGraph.getGraph().getVertices();
	}

	public synchronized String graphToString() {
		return dracRoutingGraph.getGraph().toString();
	}

	/**
	 * Query bandwidth utilization on our internal edges.
	 */
	public synchronized List<BandwidthRecord> QueryBWUsage(
	    Map<String, Map<String, LpcpFacility>> model) throws Exception {
		List<BandwidthRecord> results = new ArrayList<BandwidthRecord>();

		for (DracEdge edge : getRoutingEdges()) {
			try {
				String srcNeId = edge.getSource().getIeee();
				String srcPort = Utility.deZeroAID(edge.getSourceAid());
				double sourceusage = model.get(srcNeId).get(srcPort).getTracker()
				    .getUtilisation();

				String dstNeId = edge.getTarget().getIeee();
				String dstPort = Utility.deZeroAID(edge.getTargetAid());
				double targetusage = model.get(dstNeId).get(dstPort).getTracker()
				    .getUtilisation();

				/**
				 * Sanity check. The utilization on either end should normally be the
				 * same, OOS connections and other issues can cause a difference to be
				 * reported. Perhaps we should return the smaller of the two rather that
				 * just complaining about it?
				 */
				// Since these are doubles consider them equal if they are
				// within 1% of each other.
				if (Math.abs(sourceusage - targetusage) > 1.0d) {
					log.error("Bandwidth calculation shows source and dest usage different! srcNe:"
					    + srcNeId
					    + " srcPort:"
					    + srcPort
					    + " dstNe:"
					    + dstNeId
					    + " dstPort:"
					    + dstPort
					    + " srcUsage:"
					    + sourceusage
					    + " dstUsage:" + targetusage);
				}

				// Use the source useage.
				double usage = sourceusage;
				BandwidthRecord r = new BandwidthRecord(srcNeId, srcPort, dstNeId,
				    dstPort, usage);
				results.add(r);
			}
			catch (Exception e) {
				log.error("Building bandwith usage, skipped offending entry " + edge, e);
			}
		}

		return results;
	}

	// Used exclusively for routing constraints.
	// i.e. routing.Engine::excludedEdges
	public synchronized void removeEdgeFromGraph(DracEdge edge) {
		dracRoutingGraph.getGraph().removeEdge(edge);
	}

	/**
	 * This method should be invoked from any location at which network topology
	 * could have changed. We don't write NE or ADJ records to the db from Lpcp,
	 * so we can't really monitor the db i/f in order to call here. Alternatively,
	 * we need to trigger from locations in Lpcp at which topology events (from
	 * NeProxy) have been received.
	 * <p>
	 * => ADD/DELETE NE: Lpcp::processAssociationEvent(Element)
	 * <p>
	 * => ADD/DELETE ADJ: Lpcp::processDBChangeEvent(Element)...for EVTNOTIF_FAC
	 * type
	 */
	public void requestConsolidation() {


		if (runningTask != null && !runningTask.isDone()) {
			consolidationTask.setRefresh();
			return;
		}

		scheduleMyTask();
	}

	public synchronized void resetChannels() {
		Iterator<DracEdge> it = getRoutingEdges().iterator();

		while (it.hasNext()) {
			DracEdge e = it.next();
			e.setSourceChannel(null);
			e.setTargetChannel(null);
			e.setIngressIp(null);
		}
	}

	public void setLpcp(Lpcp lpcp) {
		this.lpcp = lpcp;
	}

	/**
	 * This is the heart of DRAC. Here we build a network graph that is based on
	 * the current list of NEs and topological links and compare it with the
	 * previous network graph, if the new graph is different we replace the old
	 * graph and generate an event to notify the admin console that the graph has
	 * changed. Older versions used to try to update the graph live by adding and
	 * removing edges and vertices as they changed in the network, this let to a
	 * huge pile of code that was hard to test, now we have a single code path and
	 * rebuild the entire graph every time anything changes in the network...
	 * we've traded off performance for correctness. The resulting code is much
	 * easier to understand and maintain.
	 * <p>
	 * We rebuild the graph every time something relevant in the network has
	 * changed, we dampen this by only running this operation at most every
	 * CONSOLIDATION_DELAY_MILLIS or 10 seconds. This period is a balance between
	 * performance and responsiveness. During start up we'll run the consolidation
	 * once, wait 10 seconds and run it again multiple times as new NEs are
	 * aligned. After the network is aligned we'd like it to run quickly so the
	 * admin console will report changes in a timely fashion.
	 * <p>
	 * FYI: For Junit testing this method is protected and returns the new graph,
	 * though the regular caller ignores the returned graph
	 */
	protected DracGraph consolidate(List<NetworkElementHolder> nes,
	    List<NetworkElementAdjacency> adjList) throws Exception {



		// Build the new graph.
		DracGraph newGraph = new DracGraph();

		// Process the verticies.
		for (NetworkElementHolder ne : nes) {
			log.debug(String
			    .format(
			        "TopologyManager.consolidate: Adding network element vertex (%s : %s)",
			        ne.getPositionX(), ne.getPositionY()));

			newGraph.addNewVertex(ne.getTid(), ne.getId(), ne.getIp(), ne.getPort(),
			    ne.getMode().toString(), ne.getType(), ne.getUserId(),
			    ne.getPassword(), ne.getNeStatus(), ne.getPositionX(),
			    ne.getPositionY());
		}

		// Process the edges.
		createConsolidatedLinks(newGraph, adjList);

		// Compare topology to determine if there are any differences
		// between the old and new created graph.


		if (graphsDiffer(verticesMasterList, edgesMasterList,
		    dracRoutingGraph.getEclipsedManualEdges(), newGraph.getGraph()
		        .getVertices(), newGraph.getGraph().getEdges(),
		    newGraph.getEclipsedManualEdges())) {



			// We have changes so update the primary routing graph.
			synchronized (this) {
				// Tricky: the Jung graph return collections are not
				// serializable (the keyset they return do not unmarshal).
				// Need to therefore rewrap...

				this.verticesMasterList = new ArrayList<DracVertex>(newGraph.getGraph()
				    .getVertices());
				this.edgesMasterList = new ArrayList<DracEdge>(newGraph.getGraph()
				    .getEdges());

				// New routing graph
				dracRoutingGraph = newGraph;
			}

			// Notify graph clients of change...
			if (lpcp != null) {

				lpcp.notifyClientsGraphRefreshRequired();
			}
		}
		else {
			log.debug("Consolidate - no graph changes detected.");
		}


		return newGraph;

	} /* consolidate */

	/**
	 * Consolidate all the topological links and add them as edges to the network
	 * graph.
	 */
	private void createConsolidatedLinks(DracGraph newGraph,
	    List<NetworkElementAdjacency> adjancencyList) throws Exception {

		List<NetworkElementAdjacency> adjList = adjancencyList;
		/**
		 * Steps we perform:
		 * <p>
		 * 1a. Fetch all adjacencies, discard those with empty tx/rx tags as they
		 * will never consolidate and can be ignored, they are put in the database
		 * to aid in debugging.
		 * <p>
		 * 1 b. Handle the case seen in surfnet we get multiple adjacencies for the
		 * same port at different "layers" which causes duplicate errors below. We
		 * can see two entries for a given port with the same tx and rx tags being
		 * reported, one at the "PHYS" layer another at the "LINE". Create a map of
		 * NetworkElementAdjacency keyed off of source and dest and manual flags to
		 * avoid duplicates.
		 * <p>
		 * 2. Scan and check for non-unique tx/rx tags, we are in trouble if the
		 * same tag appears from more than one location. We want a tx tag to match a
		 * rx tag, but we don't want two tx or two rx tags to be the same.
		 * <p>
		 * 3. Run the consolidation matching tx/rx tags together and generate a list
		 * of consolidated links.
		 * <p>
		 * 4. Remove a manual link if a network discovered link with the same source
		 * and destination exists.
		 * <p>
		 * 5. Scan and check for duplicate NE/AID in the generated list of
		 * consolidated links. We cannot have two different links that start or end
		 * at the same NE/AID unless the both start AND end at the same AID (which
		 * the previous step should have addressed).
		 * <p>
		 * 6. Profit... Convert the final set of links into edges on the graph.
		 */

		/**
		 * 1a. Fetch all adjacencies, discard those with empty tx/rx tags as they
		 * will never consolidate and can be ignored, they are put in the database
		 * to aid in debugging.
		 * <p>
		 * 1 b. Handle the case seen in surfnet we get multiple adjacencies for the
		 * same port at different "layers" which causes duplicate errors below. We
		 * can see two entries for a given port with the same tx and rx tags being
		 * reported, one at the "PHYS" layer another at the "LINE". Create a map of
		 * NetworkElementAdjacency keyed off of source and dest and manual flags to
		 * avoid duplicates.
		 * <p>
		 * Should we get data that has a different far end at the line and physical
		 * layers our duplicate check here will not work, we'll flag that later.
		 */

		{
			log.debug("createConsolidatedLinks: Step 1a and 1b on  "
			    + adjList.size() + " adjacency records.");
			Map<String, NetworkElementAdjacency> m = new HashMap<String, NetworkElementAdjacency>();

			NetworkElementAdjacency t;
			for (NetworkElementAdjacency ad : adjList) {
				if (ad.getRxTag() == null || ad.getTxTag() == null
				    || "".equals(ad.getRxTag().trim())
				    || "".equals(ad.getTxTag().trim())) {
					log.debug("createConsolidatedLinks: ignoring adjacency with null/empty tag "
					    + ad);
				}
				else {
					// We build a key for the adjacency that includes all the
					// fields except the layer field.
					String key = ad.getNeid() + ad.getPort() + ad.getRxTag()
					    + ad.getTxTag() + ad.isManual();
					t = m.put(key, ad);
					if (t != null) {
						// 't' and 'ad' are more or less identical, log and
						// continue.
						log.debug("createConsolidatedLinks: Duplicate Adjacency entries found, ignoring one of "
						    + t + " " + ad);
					}
				}
			}
			/*
			 * Our map should now hold all the "unique" NetworkElementAdjacency we
			 * found, reset adjList to this set.
			 */
			adjList = new ArrayList<NetworkElementAdjacency>(m.values());
			log.debug("createConsolidatedLinks: done step 1a and 1b raw adjacency table is now "
			    + adjList.size() + " records long");
		}

		/**
		 * 2. Scan and check for non-unique tx/rx tags, we are in trouble if the
		 * same tag appears from more than one location. We want a tx tag to match a
		 * rx tag, but we don't want two tx or two rx tags to be the same.
		 */
		{

			Set<String> txDup = new TreeSet<String>();
			Set<String> rxDup = new TreeSet<String>();
			boolean logMore = false;
			for (NetworkElementAdjacency ad : adjList) {
				if (!txDup.add(ad.getTxTag())) {
					// Duplicate TX tags were found the consolidation will mess
					// up!
					log.error("createConsolidatedLinks: Duplicate TX tags were found, consolidation may produce inaccurate results! Tx Tag '"
					    + ad.getTxTag() + "' from '" + ad + "' is a duplicate.");
					logMore = true;
				}
				if (!rxDup.add(ad.getRxTag())) {
					// Duplicate Rx tags were found the consolidation will mess
					// up!
					log.error("createConsolidatedLinks: Duplicate RX tags were found, consolidation may produce inaccurate results! Rx Tag '"
					    + ad.getRxTag() + "' from '" + ad + "' is a duplicate.");
					logMore = true;
				}
			}

			if (logMore) {
				/*
				 * Log all the adjacencies being considered only once if errors are
				 * found, not as part of each error log...
				 */
				log.error("createConsolidatedLinks: Duplicate links were found, the full set of adjacencies being examined are "
				    + adjList);
			}
		}

		/**
		 * 3. Run the consolidation matching tx/rx tags together and generate a list
		 * of consolidated links.
		 */
		List<Link> links = new ArrayList<Link>();
		{

			NetworkElementAdjacency[] adjArr = adjList
			    .toArray(new NetworkElementAdjacency[adjList.size()]);
			for (int i = 0; i < adjArr.length; i++) {
				NetworkElementAdjacency adj1 = adjArr[i];

				if (!adj1.isFlagSet()) {
					adj1.setFlag(true);
					String txTag1 = adj1.getTxTag();
					String rxTag1 = adj1.getRxTag();

					for (int j = i + 1; j < adjArr.length; j++) {
						NetworkElementAdjacency adj2 = adjArr[j];
						if (!adj2.isFlagSet()) {
							if (txTag1.equals(adj2.getRxTag())
							    && rxTag1.equals(adj2.getTxTag())) {
								// Match ... consolidate
								adj2.setFlag(true);

								boolean isManual = adj1.isManual() && adj2.isManual();

								/*
								 * add the edge in a deterministic manner in order to run
								 * comparisons with the "smaller" NE id first.
								 */
								if (adj1.getNeid().compareTo(adj2.getNeid()) < 0) {
									links.add(new Link(adj1.getNeid(), adj1.getPort(), adj2
									    .getNeid(), adj2.getPort(), isManual));
								}
								else {
									links.add(new Link(adj2.getNeid(), adj2.getPort(), adj1
									    .getNeid(), adj1.getPort(), isManual));
								}
							}
						}
					}
				}
			}
		}

		/**
		 * 4. Remove a manual link if a network discovered link with the same source
		 * and destination exists.
		 */

		{

			Map<String, Link> dupMap = new HashMap<String, Link>();
			Link t;
			List<Link> linksToRemove = new ArrayList<Link>();
			for (Link l : links) {
				String srcKey = l.getSourceNe() + "_" + l.getSourcePort();
				String dstKey = l.getDestNe() + "_" + l.getDestPort();
				// go in both directions as a manual link could be in the
				// reverse order as a network link
				t = dupMap.put(srcKey + "+" + dstKey, l);
				if (t != null) {
					/*
					 * Link 't' and 'l' have the same source and destination, throw the
					 * manual link out. If neither is a manual link something is really
					 * wrong!
					 */
					if (l.isManualLink()) {
						linksToRemove.add(l);
						t.setEclipsedManualLink(true);
					}
					else if (t.isManualLink()) {
						linksToRemove.add(t);
						l.setEclipsedManualLink(true);
					}
					else {
						log.error("createConsolidatedLinks: Found 2 links with the same source/destination but neither are manual links! "
						    + t + " " + l);
					}
				}
				t = dupMap.put(dstKey + "+" + srcKey, l);
				if (t != null) {
					/*
					 * Link 't' and 'l' have the same source and destination, throw the
					 * manual link out. If neither is a manual link something is really
					 * wrong!
					 */
					if (l.isManualLink()) {
						linksToRemove.add(l);
						t.setEclipsedManualLink(true);
					}
					else if (t.isManualLink()) {
						linksToRemove.add(t);
						l.setEclipsedManualLink(true);
					}
					else {
						log.error("createConsolidatedLinks: Found 2 links with the same source/destination but neither are manual links! "
						    + t + " " + l);
					}
				}
			}

			if (!linksToRemove.isEmpty()) {
				log.debug("createConsolidatedLinks: Removing "
				    + linksToRemove.size()
				    + " manual topological links from the consolidated link list as network equlivant network discovered links exist "
				    + linksToRemove);
				for (Link l : linksToRemove) {
					links.remove(l);
				}
			}
		}

		/**
		 * 5. Scan and check for duplicate NE/AID in the generated list of
		 * consolidated links. We cannot have two different links that start or end
		 * at the same NE/AID unless the both start AND end at the same AID (which
		 * the previous step should have addressed).
		 */
		{

			Map<String, Link> dupMap = new HashMap<String, Link>();
			Link t;
			for (Link l : links) {
				String srcKey = l.getSourceNe() + "_" + l.getSourcePort();
				String dstKey = l.getDestNe() + "_" + l.getDestPort();

				t = dupMap.put(srcKey, l);
				if (t != null) {
					/*
					 * we already had an entry for this NE/aid pair, thats a problem
					 * unless the other link overlaps the current one (same source and
					 * dest) which we should have already addressed above.
					 */
					if (t.getSourceNe().equals(l.getSourceNe())
					    && t.getSourcePort().equals(t.getSourcePort())
					    && t.getDestNe().equals(l.getDestNe())
					    && t.getDestPort().equals(l.getDestPort())) {
						log.error("createConsolidatedLinks: Consolidated topoglical links contain duplicate links, which should not happen: "
						    + l + " " + t);
					}
					else {
						log.error("createConsolidatedLinks: Two consolidated topoglical links contain the same  NE/AID pair ("
						    + srcKey
						    + "), which should not happen, network topology is in error (fix by removing offending manual link?): "
						    + l + " " + t);
					}
				}
				t = dupMap.put(dstKey, l);
				if (t != null) {
					/*
					 * we already had an entry for this NE/aid pair, thats a problem
					 * unless the other link overlaps the current one (same source and
					 * dest) which we should have already addressed above.
					 */
					if (t.getSourceNe().equals(l.getSourceNe())
					    && t.getSourcePort().equals(t.getSourcePort())
					    && t.getDestNe().equals(l.getDestNe())
					    && t.getDestPort().equals(l.getDestPort())) {
						log.error("createConsolidatedLinks: Consolidated topoglical links contain duplicate links, which should not happen: "
						    + l + " " + t);
					}
					else {
						log.error("createConsolidatedLinks: Two consolidated topoglical links contain the same  NE/AID pair ("
						    + dstKey
						    + "), which should not happen, network topology is in error (fix by removing offending manual link?): "
						    + l + " " + t);
					}
				}
			}
		}

		/**
		 * 6. Profit... Convert the final set of links into edges on the graph.
		 */


		for (Link l : links) {
			// add the edge in a deterministic manner in order to run
			// comparisons, "smaller" ne goes first
			if (l.getSourceNe().compareTo(l.getDestNe()) < 0) {
				newGraph.addNewEdge(l.getSourceNe(), l.getDestNe(), l.getSourcePort(),
				    l.getDestPort(), l.isManualLink(), l.hasEclipsedManualLink());
			}
			else {
				newGraph.addNewEdge(l.getDestNe(), l.getSourceNe(), l.getDestPort(),
				    l.getSourcePort(), l.isManualLink(), l.hasEclipsedManualLink());
			}
		}

		log.debug("createConsolidatedLinks: Done, "
		    + links.size() + " consolidated links were found. ");
	}

	/**
	 * This method is a different than that of 'findEdge'. The original 'findEdge'
	 * looks for exactly matching pairs of Neid and port...but the original
	 * calling method was not sending in the correct information. The correct
	 * information needed from the calling method requires that it 'walk' the path
	 * from A to Z, and pass in each link segment (i.e. each edge) to form the
	 * list of jung graph edge constraints. To do that, we just need to test, for
	 * the NE that we're 'on', each graph link for a matching NE and PORT on
	 * 'this' NE, and just the other end NE.
	 */
	private synchronized DracEdge findEdgeForExclusion(String thisEdgeNe,
	    String srcPort, String nextEdgeNe, String dstPort) {
		DracEdge edge = null;
		DracVertex src = null;
		DracVertex dst = null;
		String edgeSrcPort = null;
		String edgeDstPort = null;
		String edgeSrcNeID = null;
		String edgeDstNeID = null;

		try {
			Iterator<DracEdge> it = getRoutingEdges().iterator();
			while (it.hasNext()) {
				DracEdge e = it.next();

				src = e.getSource();
				dst = e.getTarget();
				edgeSrcNeID = src.getIeee();
				edgeDstNeID = dst.getIeee();
				edgeSrcPort = e.getSourceAid();
				edgeDstPort = e.getTargetAid();

				if (thisEdgeNe.equalsIgnoreCase(edgeSrcNeID)) {
					if ((srcPort.equalsIgnoreCase(edgeSrcPort) || dstPort
					    .equalsIgnoreCase(edgeSrcPort))
					    && nextEdgeNe.equalsIgnoreCase(edgeDstNeID)) {
						edge = e;
						break;
					}
				}
				else if (thisEdgeNe.equalsIgnoreCase(edgeDstNeID)) {
					if ((srcPort.equalsIgnoreCase(edgeDstPort) || dstPort
					    .equalsIgnoreCase(edgeDstPort))
					    && nextEdgeNe.equalsIgnoreCase(edgeSrcNeID)) {
						edge = e;
						break;
					}
				}
			}
		}
		catch (Exception e) {
			log.error("findEdgeForExclusion thew exception: ", e);
		}

		return edge;
	}

	/*
	 * This method crosses over the ports model to consolidate on routing
	 * constraints between to edge points
	 */
	private synchronized Map<String, String> getEdgeConstraints(String srcNeid,
	    String srcPort, String dstNeid, String dstPort) {
		Map<String, String> constraints = new HashMap<String, String>();
		constraints.put(FacilityConstants.COST_ATTR, "1");
		constraints.put(FacilityConstants.METRIC_ATTR, "1");
		constraints.put(FacilityConstants.SRLG_ATTR, "");

		LpcpFacility srcFac = HierarchicalModel.INSTANCE.getFacility(
		    srcNeid, Utility.deZeroAID(srcPort));
		LpcpFacility dstFac = HierarchicalModel.INSTANCE.getFacility(
		    dstNeid, Utility.deZeroAID(dstPort));

		if (srcFac == null || srcFac.getExtendedAttributes() == null
		    || dstFac == null || dstFac.getExtendedAttributes() == null) {
			// Facilities not ready.
			log.debug("getEdgeConstraints - could not find source or destination facility or their ext attributes.  srcFac: "
			    + srcFac + " dstFac: " + dstFac);

			// The graph should not be populated with links for which its
			// facilities have not been loaded.
			// Otherwise,
			// its routing constraints will all be n/a, null.
			return null;
		}
		else {
			double srcCost = StringParser.parseDouble(srcFac.getExtendedAttributes()
			    .get(FacilityConstants.COST_ATTR));
			double srcMetric = StringParser.parseDouble(srcFac
			    .getExtendedAttributes().get(FacilityConstants.METRIC_ATTR));
			String srcSRLG = srcFac.getExtendedAttributes().get(
			    FacilityConstants.SRLG_ATTR);

			double dstCost = StringParser.parseDouble(dstFac.getExtendedAttributes()
			    .get(FacilityConstants.COST_ATTR));
			double dstMetric = StringParser.parseDouble(dstFac
			    .getExtendedAttributes().get(FacilityConstants.METRIC_ATTR));
			String dstSRLG = dstFac.getExtendedAttributes().get(
			    FacilityConstants.SRLG_ATTR);

			if (srcCost == -1.0) {
				srcCost = 1.0;
				log.error("getEdgeConstraints - srcCost could not be determined, defaulting cost to: "
				    + srcCost);
			}

			if (srcMetric == -1.0) {
				srcMetric = 1.0;
				log.error("getEdgeConstraints - srcMetric could not be determined, defaulting metric to: "
				    + srcMetric);
			}

			if (dstCost == -1.0) {
				dstCost = 1.0;
				log.error("getEdgeConstraints - dstCost could not be determined, defaulting cost to: "
				    + dstCost);
			}

			if (dstMetric == -1.0) {
				dstMetric = 1.0;
				log.error("getEdgeConstraints - dstMetric could not be determined, defaulting metric to: "
				    + dstMetric);
			}

			constraints.put(FacilityConstants.COST_ATTR,
			    Double.toString(Math.max(srcCost, dstCost)));
			constraints.put(FacilityConstants.METRIC_ATTR,
			    Double.toString(Math.max(srcMetric, dstMetric)));
			constraints.put(FacilityConstants.SRLG_ATTR,
			    Utility.getCombinedSRLG(srcSRLG, dstSRLG));
		}

		return constraints;
	}

	private boolean graphsDiffer(Collection<DracVertex> oldVertices,
	    Collection<DracEdge> oldEdges, Collection<DracEdge> oldEclipsedEdges,
	    Collection<DracVertex> newVertices, Collection<DracEdge> newEdges,
	    Collection<DracEdge> newEclipsedEdges) {

		// This method would consider situations where "old" collections
		// are empty to be an indication that the old graph must automatically
		// be different from the new graph. However, there may be situations
		// where the new graph is also empty as this is the result of a forced
		// audit. Make sure that we check for both old and new being empty
		// which we should consider equivalent.

		// Check vertices for empty equivalence.
		if (oldVertices == null || oldVertices.isEmpty()) {
			if (newVertices == null || newVertices.isEmpty()) {
				// Verticies are both null so we have equivalent graphs. We
				// cannot have edges without verticies so we are done.

				return false;
			}


			return true;
		}

		// Determine if members of collections differ.
		if (!CollectionUtils.disjunction(oldVertices, newVertices).isEmpty()) {

			return true;
		}

		// During a staged NE discovery, vertices can be added to the graph
		// without their IEEE retrieved and set. When the Id does come in,
		// we need to trigger a graph change.
		//
		// DracVertex equals method is implemented using Ip & port only.
		for (DracVertex oldV : oldVertices) {

			for (DracVertex newV : newVertices) {
				if (oldV.equals(newV)) {
					final boolean ieeeDiffer = !oldV.getIeee().equals(newV.getIeee());
					final boolean positionsDiffer = positionsDiffer(oldV, newV);

					if (log.isDebugEnabled()) {
						log.debug(String.format("  ieee differ? %s (%s : %s)", ieeeDiffer,
						    oldV.getIeee(), newV.getIeee()));
						log.debug(String.format(
						    "  positions differ? %s (%s : %s / %s : %s)", positionsDiffer,
						    oldV.getPositionX(), newV.getPositionX(), oldV.getPositionY(),
						    newV.getPositionY()));
					}

					if (ieeeDiffer || positionsDiffer) {
						return true;
					}
				}
			}
		}

		// Now we move on to verifying any differences in the edges. John
		// assumes if there are no edges then there are no eclipsed edges.
		if (oldEdges == null) {
			if (newEdges == null) {

				return false;
			}


			return true;
		}

		// WARNING: This will rely on the DracEdge equals method, which is
		// implemented for bidirectional equality for now!

		if (!CollectionUtils.disjunction(oldEdges, newEdges).isEmpty()) {
			log.debug("TopologyManager.graphsDiffer: returns true - edges disjunction !=0 oldEdges: "
			    + oldEdges.size() + " newEdges: " + newEdges.size());
			return true;
		}

		// Finally we check the eclipsed edges.
		if (oldEclipsedEdges == null) {
			if (newEclipsedEdges == null) {

				return false;
			}


			return true;
		}

		if (!CollectionUtils.disjunction(oldEclipsedEdges, newEclipsedEdges)
		    .isEmpty()) {
			log.debug("TopologyManager.graphsDiffer: returns true - eclipsedEdges disjunction!=0  oldEdges: "
			    + oldEclipsedEdges.size() + " newEdges: " + newEclipsedEdges.size());
			return true;
		}


		return false;
	}

	private boolean positionsDiffer(DracVertex lhs, DracVertex rhs) {
		return !equalCoordinatePosition(lhs.getPositionX(), rhs.getPositionX())
		    || !equalCoordinatePosition(lhs.getPositionY(), rhs.getPositionY());
	}

	private boolean equalCoordinatePosition(Double lhs, Double rhs) {
		return (lhs == null && rhs == null) || lhs.equals(rhs);
	}

	private void scheduleMyTask() {
		runningTask = executor.schedule(consolidationTask, 0L,
		    TimeUnit.MILLISECONDS);
	}
}
