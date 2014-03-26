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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox;

import java.util.Date;
import java.util.TimeZone;

public final class AppCommonDateFormat {
	private TimeZone timeZone;

	public AppCommonDateFormat() {
		synchronized (this) {
			timeZone = CommonDateFormat.getTimeZone();
			CommonDateFormat.addAppCommonDateFormat(this);
		}
	}

	public String getCommonDateTimeFormat(Date date) {
		synchronized (this) {
			String result;
			TimeZone centralTZ = CommonDateFormat.getTimeZone();
			CommonDateFormat.setTimeZoneForApp(timeZone);
			result = CommonDateFormat.getCommonDateTimeFormat(date);
			CommonDateFormat.setTimeZoneForApp(centralTZ);
			return result;
		}
	}

	public String getCommonToolTipFormat(Date date) {
		synchronized (this) {
			String result;
			TimeZone centralTZ = CommonDateFormat.getTimeZone();
			CommonDateFormat.setTimeZoneForApp(timeZone);
			result = CommonDateFormat.getCommonToolTipFormat(date);
			CommonDateFormat.setTimeZoneForApp(centralTZ);
			return result;
		}
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone tz) {
		if (timeZone.equals(tz)) {
			return;
		}
		timeZone = tz;
		CommonDateFormat.repaintGUI();
	}

	void setTimeZoneFromCentral(TimeZone tz) {
		timeZone = tz;
	}
}
