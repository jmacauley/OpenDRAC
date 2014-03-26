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

package com.nortel.appcore.app.drac.server.nrb;

import java.io.Serializable;

public final class LpcpEvent implements Serializable {
	public enum LpcpEventType {
		GRAPH_REFRESH_REQUIRED, SERVER_ACTIVE, SERVER_INACTIVE, DOC_EVENT, VERTEX_UPDATED;
	}

	private static final long serialVersionUID = 1L;
	private final LpcpEventType eventType;
	private final String arg;

	public LpcpEvent(LpcpEventType type, String args) {
		eventType = type;
		arg = args;
	}

	/**
	 * @return the arg
	 */
	public String getArg() {
		return arg;
	}

	/**
	 * @return the eventType
	 */
	public LpcpEventType getEventType() {
		return eventType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LpcpEvent [eventType=");
		builder.append(eventType);
		builder.append(", arg=");
		builder.append(arg);
		builder.append("]");
		return builder.toString();
	}
}
