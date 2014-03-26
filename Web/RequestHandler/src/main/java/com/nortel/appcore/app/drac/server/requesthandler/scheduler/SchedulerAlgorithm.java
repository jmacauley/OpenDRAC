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

package com.nortel.appcore.app.drac.server.requesthandler.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.utility.OpticalUtility;
import com.nortel.appcore.app.drac.common.utility.OpticalUtility.OpticalPortType;

/**
 * @author
 */
public final class SchedulerAlgorithm extends AbstractSchedulerAlgorithm {

	// schedules must separate by at least 1 minute
	public static final int ONE_MIN = 60 * 1000;

	// public SchedulerAlgorithm()
	// {
	// super();
	// }

	public void addService(DracService service, EndPointType ep) {
		boolean shareable = false;
		int rate = 0;
		if (ep.getLayer().equals(Layer.LAYER1.toString())) {
			shareable = true;
			OpticalPortType opticalPort = OpticalUtility.lookupOptical(ep.getType());
			rate = opticalPort.getRate();
		}
		else {
			shareable = false;
			rate = ep.getDataRate();
		}
		Resource r = new Resource(ep.getName(), rate, shareable);
		super.addTask(new Task(service.getStartTime(), service.getEndTime(), r,
		    service.getRate()));
	}

	public List<DracService> run(List<DracService> services, EndPointType srcEp,
	    EndPointType destEp, int durationInMinutes, int rate) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 3000);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);

		// build the services list
		for (DracService aService : services) {
			if (aService.getPath().getSourceEndPoint().getId().equals(srcEp.getId())
			    || aService.getPath().getTargetEndPoint().getId()
			        .equals(srcEp.getId())) {
				addService(aService, srcEp);
			}
			if (aService.getPath().getSourceEndPoint().getId().equals(destEp.getId())
			    || aService.getPath().getTargetEndPoint().getId()
			        .equals(destEp.getId())) {
				addService(aService, destEp);
			}
		}

		// current time, 1 minute later
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.MINUTE, 1);
		cal2.set(Calendar.SECOND, 0);

		List<Task> freeTimes = super.findFreeTasks(cal2.getTimeInMillis(),
		    cal.getTimeInMillis(), durationInMinutes * 60 * 1000, rate);
		List<DracService> freeTimesList = new ArrayList<DracService>(
		    freeTimes.size());
		for (Task t : freeTimes) {
			DracService aService = new DracService();
			aService.setStartTime(t.getStartTime() + ONE_MIN);
			aService.setEndTime(t.getEndTime() - ONE_MIN);
			freeTimesList.add(aService);
		}
		return freeTimesList;
	}

}
