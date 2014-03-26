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

public final class Task {
	private long startTime;
	private long endTime;
	private int usedBandwidth;
	private Resource resource;

	public Task(long start, long end) {
		super();
		startTime = start;
		endTime = end;
	}

	public Task(long start, long end, Resource res, int used) {
		super();
		startTime = start;
		endTime = end;
		this.resource = res;
		this.usedBandwidth = used;
	}

	public long getDuration() {
		return endTime - startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public Resource getResource() {
		return resource;
	}

	public long getStartTime() {
		return startTime;
	}

	public int getUsedBandwidth() {
		return usedBandwidth;
	}

	public void setEndTime(long end) {
		endTime = end;
	}

	public void setResource(Resource res) {
		resource = res;
	}

	public void setStartTime(long start) {
		startTime = start;
	}

	public void setUsedBandwidth(int used) {
		usedBandwidth = used;
	}

}
