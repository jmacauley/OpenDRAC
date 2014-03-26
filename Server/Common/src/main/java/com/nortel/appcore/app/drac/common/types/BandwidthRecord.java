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

/**
 * Similar to the UtilizationStructure, holds the result of a bandwidth query.
 * 
 * @author pitman
 */
public final class BandwidthRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String sourceNe;
	private final String sourcePort;
	private final String targetNe;
	private final String targetPort;
	private final double usage;

	public BandwidthRecord(String srcNe, String srcPort, String tgtNe,
	    String tgtPort, double util) {
		sourceNe = srcNe;
		sourcePort = srcPort;
		targetNe = tgtNe;
		targetPort = tgtPort;
		usage = util;
	}

	/**
	 * @return the sourceNe
	 */
	public String getSourceNe() {
		return sourceNe;
	}

	/**
	 * @return the sourcePort
	 */
	public String getSourcePort() {
		return sourcePort;
	}

	/**
	 * @return the targetNe
	 */
	public String getTargetNe() {
		return targetNe;
	}

	/**
	 * @return the targetPort
	 */
	public String getTargetPort() {
		return targetPort;
	}

	/**
	 * @return the usage
	 */
	public double getUsage() {
		return usage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BandwidthRecord [sourceNe=");
		builder.append(sourceNe);
		builder.append(", sourcePort=");
		builder.append(sourcePort);
		builder.append(", targetNe=");
		builder.append(targetNe);
		builder.append(", targetPort=");
		builder.append(targetPort);
		builder.append(", usage=");
		builder.append(usage);
		builder.append("]");
		return builder.toString();
	}
}
