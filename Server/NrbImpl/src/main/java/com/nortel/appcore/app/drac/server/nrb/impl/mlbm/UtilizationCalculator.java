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

package com.nortel.appcore.app.drac.server.nrb.impl.mlbm;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.nortel.appcore.app.drac.common.types.DracService;

public final class UtilizationCalculator {
	private static final int UTIL_NUM_BUCKETS = 1440; // 1440 1-minute intervals
	                                                  // in a day y
	private static final int UTIL_BUCKET_SIZE = 1; // size of individual range in
	                                               // minutes

	private UtilizationCalculator() {
	}

	public static double[] calculate(double maxSpeed, long rangeStart,
	    long rangeEnd, List<DracService> serviceList, int days, TimeZone tz) {
		double[] bandwidthCount = new double[UTIL_NUM_BUCKETS * days + 1];
		for (DracService aService : serviceList) {
			updateBandwidthUsage(aService.getRate(), aService.getStartTime(),
			    aService.getEndTime(), rangeStart, rangeEnd, bandwidthCount, tz);
		}
		convertToPercentArray(maxSpeed, bandwidthCount);
		return bandwidthCount;

	}

	private static void convertToPercentArray(double maxSpeed,
	    double[] bandwidthCount) {
		for (int i = 0; i < bandwidthCount.length; i++) {
			bandwidthCount[i] = bandwidthCount[i] / maxSpeed * 100;
		}
	}

	private static void updateBandwidthUsage(int rate, long startTime,
	    long endTime, long rangeStart, long rangeEnd, double[] bandwidthCount,
	    TimeZone tz) {
		long serviceStartTime = startTime;
		// if start time is before our range, push it up
		if (startTime < rangeStart) {
			serviceStartTime = rangeStart;
		}

		// if end time is after our range, push it down
		long serviceEndTime = endTime;
		if (endTime > rangeEnd) {
			serviceEndTime = rangeEnd;
		}

		// Iterate every minute over the start and end time of the service and
		// update the bandwidth
		Calendar cal = Calendar.getInstance(tz);
		cal.setTimeInMillis(serviceStartTime);
		Calendar endCal = Calendar.getInstance(tz);
		endCal.setTimeInMillis(serviceEndTime);
		cal.set(Calendar.SECOND, 0);
		endCal.set(Calendar.SECOND, 0);

		while (!cal.after(endCal)) {
			int currentMinute = (int) ((cal.getTimeInMillis() - rangeStart) / 1000 / 60);

			if (currentMinute == bandwidthCount.length) {
				break;
			}

			bandwidthCount[currentMinute] += rate;
			cal.add(Calendar.MINUTE, UTIL_BUCKET_SIZE);
		}
	}
}
