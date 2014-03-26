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

package com.nortel.appcore.app.drac.common.utility.event;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;

/**
 * Created on Aug 12, 2005
 * 
 * @author nguyentd
 */
public final class Tl1XmlDbChangeEvent extends Tl1XmlEvent {
	public Tl1XmlDbChangeEvent(AbstractNetworkElement ne) {
		super(ne, "dbchange");
		this.ne = ne;
	}

	// public String getReportType()
	// {
	// return getNotificationType();
	// }

	/*
	 * <event id="00-11-58-FF-87-AF_0100000066-1005-0049" name="alarm"
	 * time="1138456743310"> <data> <element name="description"
	 * value="Unequipped"/> <element name="aid" value="STS1-1-6-1-2"/> </data>
	 * <eventInfo notificationType="CL" occurredDate="2001-01-11"
	 * occurredTime="17-35-07"/> <node id="00-11-58-FF-87-AF" ip="47.134.25.118"
	 * port="10001" status="N/A" tid="OME0237" type="OME"/> </event>
	 */
	public void setReportType(String type) {
		if (type != null) {
			setNotificationType(type);
		}
	}
}
