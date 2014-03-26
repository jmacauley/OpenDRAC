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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.nortel.appcore.app.drac.common.types.TaskType.ACTIVITY;

/**
 * @author Niranjan Chelliah
 */
public final class TimerService {

	private class MyTimerTask extends TimerTask {
		ACTIVITY taskName;
		String taskId;

		public MyTimerTask(ACTIVITY taskName, String taskId) {
			this.taskName = taskName;
			this.taskId = taskId;
		}

		@Override
		public void run() {
			synchronized (mutex) {
				listener.handleTimerNotification(this.taskName, this.taskId);
			}
		}
	}

	private final Timer timerEngine;
	private final ConcurrentHashMap<String, MyTimerTask> timerTaskList = new ConcurrentHashMap<String, MyTimerTask>();

	protected TimerNotificationListener listener;

	protected Object mutex = new Object();

	public TimerService(TimerNotificationListener timerListener) {
		timerEngine = new Timer("Nrb Timer Service");
		listener = timerListener;
	}

	public boolean cancel(String taskId) {
		synchronized (mutex) {
			MyTimerTask myTask = timerTaskList.get(taskId);
			if (myTask == null) {
				return false;
			}
			boolean returnVlaue = myTask.cancel();
			timerEngine.purge();
			return returnVlaue;
		}
	}

	public void schedule(ACTIVITY taskName, String taskId, long delay) {
		if (delay < 0) {
			synchronized (mutex) {
				listener.handleTimerNotification(taskName, taskId);
			}
		}
		if (taskName == null || taskId == null) {
			throw new IllegalArgumentException("Value can't be null");
		}
		synchronized (mutex) {
			MyTimerTask myTask = timerTaskList.get(taskId);
			if (myTask != null) {
				myTask.cancel();
			}
			myTask = new MyTimerTask(taskName, taskId);
			timerTaskList.put(taskId, myTask);
			timerEngine.schedule(myTask, delay);
			timerEngine.purge();
		}
	}
}
