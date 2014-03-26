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

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;
import java.util.Collection;

import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;

public class GraphData implements Serializable {
	private static final long serialVersionUID = 1L;

	private Collection<DracVertex> vertices;
	private Collection<DracEdge> edges;
	private Collection<DracEdge> eclipsedEdges;

	public Collection<DracEdge> getEclipsedEdges() {
		return eclipsedEdges;
	}

	public Collection<DracEdge> getEdges() {
		return edges;
	}

	public Collection<DracVertex> getVertices() {
		return vertices;
	}

	public void setEclipsedEdges(Collection<DracEdge> eclipsedEdges) {
		this.eclipsedEdges = eclipsedEdges;
	}

	public void setEdges(Collection<DracEdge> edges) {
		this.edges = edges;
	}

	public void setVertices(Collection<DracVertex> vertices) {
		this.vertices = vertices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GraphData [edges=");
		builder.append(edges);
		builder.append(", vertices=");
		builder.append(vertices);
		builder.append("]");
		return builder.toString();
	}
}