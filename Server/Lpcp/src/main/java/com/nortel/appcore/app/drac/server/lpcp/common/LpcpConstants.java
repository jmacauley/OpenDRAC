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

package com.nortel.appcore.app.drac.server.lpcp.common;

public final class LpcpConstants {

	// other
	public static final String DSTTNACHANNEL_KEY = "dstTNA";
	public static final String EDGE_NODE = "edge";
	public static final String EDGELIST_NODE = "edgelist";
	public static final String ENDPOINTS_NODE = "endpoints";
	public static final String ID_ATTR = "id";
	public static final String PATH_EVENT_NODE = "pathEvent";
	public static final String PATH_INSTANCE_NODE = "pathInstance";
	public static final String PATH_NODE = "path";

	public static final String PROTECTION_1PLUS1 = "PATH1PLUS1";
	public static final String RATE_ATTR = "rate";
	public static final String RT_PATHTYPE_PRT = "PROTECTING";
	public static final String RT_PATHTYPE_WRK = "WORKING";
	public static final String SOURCE_ATTR = "source";
	public static final String SOURCECHANNEL_ATTR = "sourcechannel";
	public static final String SOURCEPORT_ATTR = "sourceport";

	public static final String SRCTNACHANNEL_KEY = "srcTNA";
	public static final String STATUS_NODE = "status";
	public static final String TARGET_ATTR = "target";
	public static final String TARGETCHANNEL_ATTR = "targetChannel";
	public static final String TARGETPORT_ATTR = "targetport";
	public static final String TEXT_ATTR = "text";
	public static final String TNA_ATTR = "tna";
	// public static final String TRACKERRATE_KEY = "trackerRate";
	public static final String TYPE_ATTR = "type";

	private LpcpConstants() {
		super();
	}

}
