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
 * ScheduleResult holds the result of a query or create schedule operation. It
 * is intended to clarify that the scheduler can return different data depending
 * if you are creating a schedule or querying a schedule.
 * <p>
 * If creating a schedule, expect to get the ID of the schedule that was
 * created.
 * <p>
 * If querying a schedule, expect to be returned the XML representation of the
 * path that would have been created had the schedule been created (used by the
 * admin console to highlight the potential path).
 * <p>
 * Ideally, we'll get rid of the slow and hard to parse XML string and return
 * the potential path as a list<CrossConnections> in the future and we'll save
 * time and energy both building the results and parsing them at the far end.
 * 
 * @author pitman
 * @since Nov 2010
 */
public final class ScheduleResult implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String id;
	private final String path;

	public ScheduleResult(String callId, String xmlPath) {
		id = callId;
		path = xmlPath;
	}

	/**
	 * Example output
	 * <p>
	 * 5c16802f-1288106017247
	 */
	public String getCallId() {
		return id;
	}

	/**
	 * Example path - xml string.
	 * <p>
	 * <pathEvent><pathInstance type="WORKING"><path><edge
	 * source="00-21-E1-D6-D6-70" target="00-21-E1-D6-D6-70"
	 * sourceport="OC192-1-9-1" targetport="OC12-1-11-1" rate="STS1"
	 * sourcechannel="1" targetChannel="1" /><edge source="00-21-E1-D6-D5-DC"
	 * target="00-21-E1-D6-D5-DC" sourceport="OC12-1-12-1"
	 * targetport="OC192-1-9-1" rate="STS1" sourcechannel="1" targetChannel="1"
	 * /></path><edgelist><edge id="6" /></edgelist><endpoints><source
	 * id="00-21-E1-D6-D6-70" tna="OME0039_OC192-1-9-1" /><target
	 * id="00-21-E1-D6-D5-DC" tna="OME0237_OC192-1-9-1" /></endpoints><status
	 * text="Path from: OME0039 to OME0237, cost: 1 hops: 1"
	 * /></pathInstance></pathEvent>
	 */
	public String getXmlPath() {
		return path;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScheduleResult [id=");
		builder.append(id);
		builder.append(", path=");
		builder.append(path);
		builder.append("]");
		return builder.toString();
	}
}
