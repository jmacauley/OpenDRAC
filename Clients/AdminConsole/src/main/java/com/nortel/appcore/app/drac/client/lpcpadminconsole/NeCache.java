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

package com.nortel.appcore.app.drac.client.lpcpadminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.NetworkGraph;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

/**
 * @author pitman
 */
public enum NeCache {
	INSTANCE;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Map<String, DracVertex> neByIpPort = new TreeMap<String, DracVertex>();
	private final Map<String, DracEdge> edgeById = new HashMap<String, DracEdge>();
	private final Map<String, DracEdge> eclipsedEdgesById = new HashMap<String, DracEdge>();
	private static final Object LOCK = NetworkGraph.INSTANCE;

	public void addVertex(DracVertex v) throws Exception {
		synchronized (LOCK) {
			
			neByIpPort.put(v.getUniqueId(), v);
			NetworkGraph.INSTANCE.addVertex(v);
		}
	}

	public void cacheEdgesAndVertices(Collection<DracEdge> edges,
	    Collection<DracVertex> vertices, Collection<DracEdge> eclipsedEdges) {
		synchronized (LOCK) {
			neByIpPort.clear();
			edgeById.clear();
			this.eclipsedEdgesById.clear();

			for (DracEdge e : edges) {
				edgeById.put(e.getID(), e);
			}

			for (DracVertex v : vertices) {
				neByIpPort.put(v.getUniqueId(), v);
			}

			for (DracEdge ee : eclipsedEdges) {
				eclipsedEdgesById.put(ee.getID(), ee);
			}
		}
	}

	/**
	 * Remove it all
	 */
	public void clearCache() throws Exception {
		synchronized (LOCK) {
			
			neByIpPort.clear();
			edgeById.clear();
			this.eclipsedEdgesById.clear();
			NetworkGraph.INSTANCE.resetGraph();
		}
	}

	public List<DracEdge> getAllEclipsedEdges() {
		synchronized (LOCK) {
			return new ArrayList<DracEdge>(this.eclipsedEdgesById.values());
		}
	}

	public List<DracEdge> getAllEdge() {
		synchronized (LOCK) {
			return new ArrayList<DracEdge>(edgeById.values());
		}
	}

	public List<DracVertex> getAllVertex() {
		synchronized (LOCK) {
			return new ArrayList<DracVertex>(neByIpPort.values());
		}
	}

	public DracEdge getEdgeById(String id) {
		synchronized (LOCK) {
			return edgeById.get(id);
		}
	}

	public Map<String, String> getIeeeLabelMap() {
		HashMap<String, String> result = new HashMap<String, String>();
		synchronized (LOCK) {
			for (DracVertex v : neByIpPort.values()) {
				result.put(v.getIeee(), v.getLabel());
			}
		}
		return result;
	}

	public List<String> getIPList() {
		synchronized (LOCK) {
			return new ArrayList<String>(neByIpPort.keySet());
		}
	}

	/**
	 * Return a list of NE labels (tids) can have duplicates or unknown values
	 **/
	public String[] getNEList() {
		List<String> r = new ArrayList<String>();
		synchronized (LOCK) {
			for (DracVertex v : neByIpPort.values()) {
				r.add(v.getLabel());
			}
		}
		return r.toArray(new String[r.size()]);
	}

	public Set<NeIeee> getNEListIEEE() {
		Set<NeIeee> uniqueSet = new TreeSet<NeIeee>();
		for (DracVertex v : neByIpPort.values()) {
			uniqueSet.add(new NeIeee(v.getLabel(), v.getIeee()));
		}

		return uniqueSet;
	}

	public DracVertex getVertex(String ieee) {
		if (ieee == null) {
			return null;
		}
		synchronized (LOCK) {
			for (DracVertex v : neByIpPort.values()) {
				if (ieee.equals(v.getIeee())) {
					return v;
				}
			}
			return null;
		}
	}

	public DracVertex getVertex(String ip, String port) {
		synchronized (LOCK) {
			return neByIpPort.get(ip + "_" + port);
		}
	}

	public int getVertexCount() {
		synchronized (LOCK) {
			return neByIpPort.size();
		}
	}

	public void refreshVertex(DracVertex v) throws Exception {
		synchronized (LOCK) {
			
			NetworkGraph.INSTANCE.refreshVertex(v);
		}
	}

	public void updateNE(Element neMap, Element data) throws Exception {
		log.debug("NeCache: updateNE " + XmlUtility.rootNodeToString(neMap) + " "
		    + XmlUtility.rootNodeToString(data));
		String status = neMap.getAttributeValue("status");
		String statString = null;
		String tid = neMap.getAttributeValue("tid");
		String ip = neMap.getAttributeValue("ip");
		String type = neMap.getAttributeValue("type");
		String ieee = neMap.getAttributeValue("id");
		String port = neMap.getAttributeValue("port");

		String docStatus = null;
		String docProgress = null;

		log.debug("updateNE: status: " + status + " tid: " + tid + " ip: " + ip
		    + " type: " + type + " ieee: " + ieee + " port: " + port);

		if (data != null) {
      List<Element> dataElements = data.getChildren("element");
			for (int i = 0; i < dataElements.size(); i++) {
				Element child = dataElements.get(i);
				if ("description".equalsIgnoreCase(child.getAttributeValue("name"))) {
					statString = child.getAttributeValue("value");
				}
				if ("docPercent".equalsIgnoreCase(child.getAttributeValue("name"))) {
					docProgress = child.getAttributeValue("value");
				}
				if ("docProgressStatus".equalsIgnoreCase(child
				    .getAttributeValue("name"))) {
					docStatus = child.getAttributeValue("value");
				}
			}
		}

		synchronized (LOCK) {
			DracVertex ne = getVertex(ip, port);

			if (ne == null) {
				log.error("EYECATCHER::else HIT! update NE, NE not found " + ip + " "
				    + port);
				return;
			}

			log.debug("Properties before update: status:" + ne.getStatus() + "\n"
			    + "status description: " + statString + "\n" + "ne= " + ne.toString());

			NeStatus newStatus = NeStatus.fromString(status);

			ne.setStatus(newStatus);
			ne.setLabel(tid);
			ne.setIeee(ieee);
			ne.setType(NeType.fromString(type));

			if (docStatus != null && !"Ready".equals(docStatus)) {
				String label;
				label = " [" + docStatus;
				if (docProgress != null) {
					label = label + " : " + docProgress + "%";
				}
				if (statString != null) {
					label += " " + statString;
				}
				label += "]";

				ne.setDisplayString(label);
			}
			else {
				ne.setDisplayString(null);
			}

			refreshVertex(ne);
			log.debug("Properties after update:  NE: " + ne.toString()
			    + " DisplayString " + ne.getDisplayString());
		}
	}
}
