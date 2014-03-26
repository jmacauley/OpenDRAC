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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.RoutingException;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;

public final class RoutingAlgorithm {
  private final Logger log = LoggerFactory.getLogger(getClass());
	public static final int DIJKSTRA_SPF = 0;
	// private final int MAX_ALG_INDEX = DIJKSTRA_SPF; // Set this to the last
	// algorithm int
	// private final int selectedAlgorithm = -1;

	private final Graph<DracVertex, DracEdge> graph;
	private final Transformer<DracEdge, Number> worker;

	public RoutingAlgorithm(Graph<DracVertex, DracEdge> g, Transformer<DracEdge, Number> theWorker) {
		graph = g;
		worker = theWorker;
	}

	public List<DracEdge> getPath(DracVertex src, DracVertex dst, int algorithm)
	    throws RoutingException {
		List<DracEdge> path = null;

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (algorithm == DIJKSTRA_SPF) {
			DijkstraShortestPath<DracVertex, DracEdge> dsp;
			try {
                if (log.isDebugEnabled()) {
                    String a = src.getLabel() + ":" + src.getIp() + ":" + src.getPort();
                    String z = dst.getLabel() + ":" + dst.getIp() + ":" + dst.getPort();
                    log.debug("getPath: Routing DIJKSTRA_SPF " + a + " to " + z);
                }

				dsp = new DijkstraShortestPath<DracVertex, DracEdge>(graph, worker, false);

				log.debug("Beginning shortest path calculation: " + sdf.format(new Date(System.currentTimeMillis())));

				path = dsp.getPath(src, dst);

				log.debug("Finished shortest path calculation: " + sdf.format(new Date(System.currentTimeMillis())));
			}
			catch (Exception e) {
				log.error("Exception finding shortest path: ", e);

				throw new RoutingException(
				    DracErrorConstants.LPCP_E3036_EXCEPTION_CALCULATING_PATH,
				    new String[] { e.getMessage() }, e);
			}
		}
		else {
			log.error("Unknown routing algorithm: " + algorithm);
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3037_ROUTING_ALGORITHM_UNKNOWN,
			    new String[] { Integer.toString(algorithm) });
		}
		return path;
	}

}
