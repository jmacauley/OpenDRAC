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

package com.nortel.appcore.app.drac.server.ws.common;

public final class CommonConstants {
	public static final int MAX_NUM_OF_RESERVATIONS_TO_BE_RETRIEVED = 3650;
	public static final int MAX_NUM_OF_RESERVATION_OCCURRENCES_TO_BE_RETRIEVED = 3650;
	public static final int MAX_NUM_OF_RESERVATION_OCCURRENCES_TO_BE_CREATED_PER_RESERVATION = 3650;
	public static final int MAX_NUM_OF_LOGS_TO_BE_RETRIEVED = 500;
	public static final int MAX_NUM_OF_ALARMS_TO_BE_RETRIEVED = 500;
	public static final int MAX_NUM_OF_ENDPOINTS_TO_BE_RETRIEVED = 500;
	public static final int MAX_NUM_OF_DRAC_SERVERS_BE_RETRIEVED = 20;
	public static final int MAX_NUM_OF_DRAC_WEB_SERVICES = 20;
	public static final int MAX_NUM_OF_WEEKDAYS = 7;

	private CommonConstants() {
		super();
	}
}
