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

public final class LpcpStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Old XML record format. String statusStr = "<status restart=\"" + restart +
	 * "\" state=\"" + serverState.asInt() + "\" statestring=\"" +
	 * serverState.asString() + "\"/>"; return makeEventXML(statusStr, "REPT",
	 * REQ_GET_STATUS);
	 */
	private final boolean restart;
	private final int state;
	private final String stateAsString;

	public LpcpStatus(boolean restartFlag, int serverState,
	    String serverStateAsString) {
		restart = restartFlag;
		state = serverState;
		stateAsString = serverStateAsString;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @return the stateAsString
	 */
	public String getStateAsString() {
		return stateAsString;
	}

	/**
	 * @return the restart
	 */
	public boolean isRestart() {
		return restart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LpcpStatus [restart=");
		builder.append(restart);
		builder.append(", state=");
		builder.append(state);
		builder.append(", stateAsString=");
		builder.append(stateAsString);
		builder.append("]");
		return builder.toString();
	}

}
