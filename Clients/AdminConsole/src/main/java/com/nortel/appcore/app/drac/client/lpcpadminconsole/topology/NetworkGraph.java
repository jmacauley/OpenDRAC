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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.topology;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeCache;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.GraphData;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

/**
 * Network Graph: Wrapper on the JUNG graph. We maintain the non-graphical parts
 * of the graph here, mostly to keep the other parts smaller.
 * 
 * @author pitman
 */
public enum NetworkGraph {

	INSTANCE;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	private Graph<DracVertex, DracEdge> graph;
	private JungTopologyPanel viewer;

	NetworkGraph() {
		graph = new UndirectedSparseMultigraph<DracVertex, DracEdge>();
	}
	
	public synchronized void addEdge(DracEdge e) {
		graph.removeEdge(e);
		graph.addEdge(e, e.getSource(), e.getTarget());
		updateViewers();
	}

	public synchronized void addVertex(DracVertex v) {
		graph.removeVertex(v);
		graph.addVertex(v);
		updateViewers();
	}

	public synchronized void addViewer(JungTopologyPanel jp) {
		viewer = jp;
	}

	/**
	 * Build a XML representation of this graph preferences. Including layout,
	 * background image, x,y cords of each ne.
	 * 
	 * @return A big string
	 * @throws Exception
	 */
	public synchronized Element buildPreferencesXML() throws Exception {
		Element e = viewer.getGraphPreferences().asXml();
		log.debug("NetworkGraph: buildPreferencesXML returns "
		    + new XMLOutputter().outputString(e));
		return e;
	}

	/**
	 * Clear the graph.
	 */
	public synchronized void clearGraph() throws Exception {
		// Avoid concurrent list modification exceptions.
		List<DracEdge> ee = new ArrayList<DracEdge>(graph.getEdges());
		for (DracEdge e : ee) {
			graph.removeEdge(e);
		}

		List<DracVertex> vv = new ArrayList<DracVertex>(graph.getVertices());
		for (DracVertex v : vv) {
			graph.removeVertex(v);
		}
		updateViewers();
	}

	public synchronized Graph<DracVertex, DracEdge> getGraph() {
		return graph;
	}

	/*
	 * invoked on start up after the graph (edges/vertex) have been loaded and the
	 * graph needs to be refreshed
	 */
	public synchronized void graphLoaded() {
		try {
			viewer.resetGraph(true);
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
	}

	/*
	 * Removes all vertex/edges not in the given edge list(s). Resets the graph so
	 * highlighting, etc disappears as well.
	 */
	public synchronized void hideNonPath(List<Element> currentShortPath,
	    List<Element> edgesProtecting) throws Exception {
		Map<DracVertex, DracVertex> nodesInPath = new HashMap<DracVertex, DracVertex>();
		Map<DracEdge, DracEdge> linksInPath = new HashMap<DracEdge, DracEdge>();
		for (Element el : currentShortPath) {
			DracEdge e = NeCache.INSTANCE
			    .getEdgeById(el.getAttributeValue("id"));
			if (e == null) {
				log.error("Failed to find edge " + el.getAttributeValue("id"));
			}
			else {
				linksInPath.put(e, e);
				nodesInPath.put(e.getSource(), e.getSource());
				nodesInPath.put(e.getTarget(), e.getTarget());
			}
		}
		if (edgesProtecting != null) {
			for (Element el : edgesProtecting) {
				DracEdge e = NeCache.INSTANCE.getEdgeById(
				    el.getAttributeValue("id"));
				if (e == null) {
					log.error("Failed to find edge " + el.getAttributeValue("id"));
				}
				else {
					linksInPath.put(e, e);
					nodesInPath.put(e.getSource(), e.getSource());
					nodesInPath.put(e.getTarget(), e.getTarget());
				}
			}
		}

		// Now clear the graph and add the desirable edges/vertices back in.
		clearGraph();
		for (DracVertex v : nodesInPath.keySet()) {
			graph.addVertex(v);
		}

		for (DracEdge e : linksInPath.keySet()) {
			graph.addEdge(e, e.getSource(), e.getTarget());
		}

		graphLoaded();
	}

	/**
	 * Find the NE with the given label and highlight it on the graph
	 */
	public synchronized void highlightNELike(String searchString)
	    throws Exception {
		// even if we don't find a match, clear any existing high lights.
		clearHighLight();

		if (searchString == null || "".equals(searchString.trim())) {
			// Could be many NE's with an empty label
			return;
		}

		for (DracVertex v : graph.getVertices()) {
			if (v.getLabel().equalsIgnoreCase(searchString)) {
				highlightVertex(v);
				break;
			}
		}
	}

	public synchronized void highlightPath(List<Element> edgeList) {
		clearHighLight();

		log.debug("edges.size() is: " + edgeList.size() + " edges contains: "
		    + edgeList);
		for (Element edge : edgeList) {
			String edgeId = edge.getAttributeValue("id");
			DracEdge e = NeCache.INSTANCE.getEdgeById(edgeId);
			if (e == null) {
				log.debug("Fatal error, shortest path link not found!: " + edgeId
				    + " in " + edgeList);
			}
			else {
				highlightEdge(e, null);
			}
		}
		updateViewers();
	}

	public synchronized void highlightPathAndEndPoints(List<Element> edges,
	    String srcNeId, String dstNeId, String srcTNA, String dstTNA) {
		/*
		 * The original Ilog version of this method created two fake NEs named by
		 * the src and dest TNA's and drew them into the the graph to represent the
		 * ultimate start and end points of the service. We could do that here if we
		 * wanted to.
		 */
		DracVertex src = NeCache.INSTANCE.getVertex(srcNeId);
		DracVertex dest = NeCache.INSTANCE.getVertex(dstNeId);
		if (src != null) {
			highlightVertex(src);
		}
		if (dest != null) {
			highlightVertex(dest);
		}
		highlightPath(edges);
	}

	public synchronized void highlightPathDotted(List<Element> path, Color c)
	    throws Exception {
		Color highlightColour = Color.green;
		if (c != null) {
			highlightColour = c;
		}

		
		for (Element edge : path) {
			String edgeId = edge.getAttributeValue("id");
			DracEdge e = NeCache.INSTANCE.getEdgeById(edgeId);
			if (e == null) {
				log.debug("Fatal error, shortest path link not found!: " + edgeId
				    + " in " + path);
			}
			else {
				highlightEdge(e, highlightColour);
			}
		}
		updateViewers();

	}

	/*
	 * This method seams stupid with respect to what is passed in and what we
	 * do...
	 */
	public synchronized void hightLightLink(CrossConnection edge,
	    Map<String, CrossConnection> edgeMap, Color highlight) {
		
		String neId = edge.getSourceNeId();

		Collection<DracEdge> links = graph.getIncidentEdges(NeCache.INSTANCE
		    .getVertex(neId));

		String linkSrcPort = null;
		String linkDstPort = null;
		String linkSrc = null;
		String linkDst = null;
		String linkSrcID = null;
		String linkDstID = null;

		if (links != null) {
			for (DracEdge e : links) {

				linkSrcPort = e.getSourceAid();
				linkDstPort = e.getTargetAid();
				linkSrc = e.getSource().getIeee();
				linkDst = e.getTarget().getIeee();

				linkSrcID = (linkSrc + "_" + linkSrcPort).toUpperCase();
				linkDstID = (linkDst + "_" + linkDstPort).toUpperCase();

				log.debug(" linkSrcID: " + linkSrcID + " linkDstID: " + linkDstID
				    + " edgeMap: " + edgeMap);
				if (edgeMap.get(linkSrcID) != null && edgeMap.get(linkDstID) != null) {
					highlightEdge(e, highlight);
				}
			}
		}
		updateViewers();
	}

	/**
	 * Restore previously saved preferences for this graph.. such as background
	 * image, layout, x/y cordinates of nodes.
	 * 
	 * @return true if preferences were loaded.
	 */
	public synchronized boolean parseAndSetUserPrefs(Element e) throws Exception {
		log.debug("NetworkGraph: parseAndSetUserPrefs invoked with "
		    + new XMLOutputter().outputString(e));
		boolean foundPrefs = viewer.getGraphPreferences().fromXml(e);
		return foundPrefs;
	}

	public synchronized void refreshVertex(DracVertex v) throws Exception {
		// A vertex in our graph has been altered.
		log.debug("NetworkGraph: refreshVertex " + v.getIeee());
		resetGraph();
	}

	public synchronized void removeEdge(DracEdge e) throws Exception {
		graph.removeEdge(e);
		updateViewers();
	}

	public synchronized void removeVertex(DracVertex v) throws Exception {
		graph.removeVertex(v);
		updateViewers();
	}

	/**
	 * Reload our graph vertex and edges from the NeCache, reset the graph
	 */
	public synchronized void resetGraph() throws Exception {
		clearGraph();
		for (DracVertex v : NeCache.INSTANCE.getAllVertex()) {
			addVertex(v);
		}

		for (DracEdge e : NeCache.INSTANCE.getAllEdge()) {
			addEdge(e);
		}
		clearHighLight();
		graphLoaded();
	}

	public synchronized void setEnabled(boolean enabled) {
		viewer.getPanel().setEnabled(enabled);
	}

	public synchronized void setGraphData(GraphData graphData) throws Exception {
		Collection<DracEdge> edges = graphData.getEdges();
		Collection<DracVertex> vertices = graphData.getVertices();
		Collection<DracEdge> eclipsedEdges = graphData.getEclipsedEdges();

		// Update edges and vertices in NeCache:
		NeCache.INSTANCE.cacheEdgesAndVertices(edges, vertices, eclipsedEdges);

		resetGraph();

	}

	protected synchronized void clearHighLight() {
		for (DracEdge e : graph.getEdges()) {
			e.setPaintColor(null);
		}

		viewer.clearHighLight();
	}

	private void highlightEdge(DracEdge e, Paint color) {
		viewer.highlight(e, color);
	}

	private void highlightVertex(DracVertex v) {
		viewer.highlight(v);
	}

	private void updateViewers() {
		try {
			viewer.refreshGraphRequired();
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
	}

}
