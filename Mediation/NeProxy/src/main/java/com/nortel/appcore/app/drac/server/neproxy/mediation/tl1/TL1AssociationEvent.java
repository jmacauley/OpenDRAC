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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1;

import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;

public final class TL1AssociationEvent {
	public static final byte ASSOCIATION_UP = 1;
	public static final byte ASSOCIATION_DOWN = 2;
	public static final byte CONNECTION_FAILS = 3;
	public static final byte WRONG_TID = 4;
	public static final byte FAILED_LOGOUT = 5;
	// private static final String[] eventString =
	// { "inService", "outOfService", "connectionFail", "wringTid", "failedLogout"
	// };
	private final byte eventCode;

	// private final NetworkElementInfo neInfo;
	// private final String occrDate;
	// private final String occrTime;

	public TL1AssociationEvent(byte code, NetworkElementInfo ne) {
		eventCode = code;
		// neInfo = ne;
		/*
		 * Date date = new Date(); SimpleDateFormat simpledateformat = new
		 * SimpleDateFormat("yyyy-MM-dd"); occrDate = simpledateformat.format(date);
		 * simpledateformat = new SimpleDateFormat("HH:mm:ss"); occrTime =
		 * simpledateformat.format(date);
		 */
	}

	public int getCode() {
		return eventCode;
	}

}
