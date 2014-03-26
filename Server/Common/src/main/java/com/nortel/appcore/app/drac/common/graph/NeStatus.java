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

package com.nortel.appcore.app.drac.common.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pitman
 */
public enum NeStatus {
	/*
	 * The order of these do matter. If the ordinal is > NE_NOT_CONNECTED we
	 * assume it is connected or in the process of connecting.
	 */
	NE_DELETED("neDeleted", "Network Element has been removed"), //
	NE_UNKNOWN("unknown", "Network Element is in an unknown state"), //

	NE_NOT_PROVISION("notProvision", "Network Element is not provisioned"), //
	NE_CREATED("neCreated", "Establishing connection to Network Element"), //
	NE_NOT_CONNECT("notConnect",
	    "Connection to Network Element could not be established"), //
	NE_NOT_AUTHENTICATED("notAuthenticated", "Could not authenticate"), //
	NE_ASSOCIATED("associated",
	    "Connection to Network Element established, credentials authorized"), //
	NE_INITIALIZING("initializing", "Discovering Network Element"), //
	NE_ALIGNED("aligned", "Network Element is Ready");

	private static final Logger log = LoggerFactory.getLogger(NeStatus.class);
	private final String label;
	private final String description;

	NeStatus(String s, String desc) {
		label = s;
		description = desc;
	}

	public static NeStatus fromString(String s) throws Exception {
		for (NeStatus n : NeStatus.values()) {
			if (n.label.equalsIgnoreCase(s)) {
				return n;
			}
		}
		log.error("Unable to map NE status string <" + s
		    + "> to NeStatus enum; returning NE_UNKNOWN", new Exception(
		    "Stack Trace of caller"));
		return NE_UNKNOWN;
	}

	public String getSateDescription() {
		return description;
	}

	public String getStateString() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}
}
