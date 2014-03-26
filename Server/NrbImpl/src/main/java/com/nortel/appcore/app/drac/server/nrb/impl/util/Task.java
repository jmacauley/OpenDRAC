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

package com.nortel.appcore.app.drac.server.nrb.impl.util;

import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.TaskType.State;

/**
 * @see com.nortel.appcore.app.drac.server.nrb.impl.util.TaskExecutorService
 * @author <a href="mailto:nchellia@nortel.com">Niranjan Chelliah </a>
 */
public class Task implements Runnable {
	private final TaskType taskType;

	// /**
	// * @param taskName
	// */
	// public Task(String taskName, String taskOwner)
	// {
	// taskInfo = new TaskInfo(taskName, taskOwner);
	// taskInfo.setState(State.SUBMITTED);
	// }

	/**
	 * @param taskName
	 */
	public Task(String taskName, String taskOwner, String taskId) {
		taskType = new TaskType(taskName, taskOwner, taskId);
		taskType.setState(State.SUBMITTED);
	}

	/**
     * 
     */
	public TaskType getTaskInfo() {
		return taskType;
	}

	// ===================================================================

	@Override
	public void run() {
		// eh?
	}
}
