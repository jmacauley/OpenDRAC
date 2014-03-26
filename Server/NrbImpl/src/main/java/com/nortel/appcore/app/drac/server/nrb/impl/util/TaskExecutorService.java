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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.nortel.appcore.app.drac.common.types.TaskType.State;

/**
 * @see java.util.concurrent.ThreadPoolExecutor
 * @author <a href="mailto:nchellia@nortel.com">Niranjan Chelliah </a>
 */
public final class TaskExecutorService extends ThreadPoolExecutor {

	static class MyThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "TaskExecutorService createService");
		}
	}

	/**
	 * Timeout period for the request in seconds. Note : Timeout period = Task
	 * Submitted - Task Starting
	 */

	public TaskExecutorService() {
		super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
		    new MyThreadFactory(), new AbortPolicy());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.ThreadPoolExecutor#execute(java.lang.Runnable)
	 */
	@Override
	public void execute(Runnable command) {
		((Task) command).getTaskInfo().setSubmittedTime(System.currentTimeMillis());
		super.execute(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable,
	 * java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if (r instanceof Task) {
			((Task) r).getTaskInfo().setState(State.DONE);
		}
		super.afterExecute(r, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread,
	 * java.lang.Runnable)
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if (r instanceof Task) {
			((Task) r).getTaskInfo().setState(State.IN_PROGRESS);
		}
		super.beforeExecute(t, r);
	}

}
