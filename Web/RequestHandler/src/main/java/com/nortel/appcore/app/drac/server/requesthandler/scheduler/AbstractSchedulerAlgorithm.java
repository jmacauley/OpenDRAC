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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author
 */
public abstract class AbstractSchedulerAlgorithm {

	/*
	 * keeps track of time boundaries of all the tasks, in ascending order, such
	 * that given any t_i and t_i+1, there exists no task that starts after t_i
	 * and ends before t_i+1
	 */
	private final Set<Long> timeEdges = new TreeSet<Long>();

	// keeps track of the tasks
	private final List<Task> taskList = new ArrayList<Task>();

	// public AbstractSchedulerAlgorithm()
	// {
	// }

	/**
	 * Add a task to the task list, noting the time boundaries
	 * 
	 * @param t
	 */
	public void addTask(Task t) {
		taskList.add(t);
		timeEdges.add(t.getStartTime());
		timeEdges.add(t.getEndTime());
	}

	/**
	 * Find free timeslots of minimum specified duration between specified start
	 * and end time, available to schedule the specified bandwidth
	 * 
	 * @param startTime
	 *          Starting time range, in milliseconds
	 * @param endTime
	 *          Ending time range, in miliseconds
	 * @param duration
	 *          Minimum task duration, in milliseconds
	 * @param reqBandwidth
	 *          Bandwidth required
	 */
	public List<Task> findFreeTasks(long startTime, long endTime, int duration,
	    int reqBandwidth) {
		List<Task> freeTasks = new ArrayList<Task>();

		if (taskList.isEmpty()) {
			// simple case, nothing is scheduled
			Task t = new Task(startTime, endTime);
			freeTasks.add(t);
		}
		else {

			// add start and end time range to our time edges
			List<Long> edges = new ArrayList<Long>();
			edges.addAll(this.timeEdges);
			edges.add(startTime);
			edges.add(endTime);
			Collections.sort(edges);

			/*
			 * iterate through timeEdges and determine if we can schedule a task
			 * between t_i and t_i+1
			 */
			List<Task> tempTasks = null;
			Task aTask = null;
			for (int i = 0; i < edges.size(); i++) {
				long ti = edges.get(i);
				if (ti < startTime) {
					// this time is before start range, not interested
					continue;
				}
				else if (ti > endTime) {
					// we're looking past the end time that we care about, break
					break;
				}
				else {
					if (i != edges.size() - 1) { // haven't reached the end yet
						long ti_1 = edges.get(i + 1);
						tempTasks = getTasks(ti, ti_1);
						if (tempTasks.isEmpty()) {
							// definitely can schedule here
							freeTasks.add(new Task(ti, ti_1));
						}
						else {
							// iterate through each task in this timeslot, determine if we can
							// add
							boolean ignoreTimeSlot = false;
							Map<String, Integer> bandwidthMap = new HashMap<String, Integer>();
							for (int j = 0; j < tempTasks.size(); j++) {
								aTask = tempTasks.get(j);
								if (!aTask.getResource().isShareable()) {
									/*
									 * this task cannot have its resource shared in this timeslot
									 * break immediately, this timeslot is no good
									 */
									ignoreTimeSlot = true;
									break;
								}
								/*
								 * task can be shared, but do we have enough space? total them
								 * up use a hashmap to keep resources separate
								 */
								if (bandwidthMap.get(aTask.getResource().getId()) == null) {
									bandwidthMap.put(aTask.getResource().getId(),
									    Integer.valueOf(0));
								}
								Integer bw = bandwidthMap.get(aTask.getResource().getId())
								    + aTask.getUsedBandwidth();
								bandwidthMap.put(aTask.getResource().getId(), bw);
								if (aTask.getResource().getMaxBandwidth() - bw < reqBandwidth) {
									// there is no more bandwidth available, break immediately
									ignoreTimeSlot = true;
									break;
								}
							}

							// finished looking at all tasks in this slot, did we break out?
							if (!ignoreTimeSlot) {
								freeTasks.add(new Task(ti, ti_1));
							}
						} // end if tempTasks is not empty
					} // end if not last time edge
				} // end if ti is not before startTime
			} // end for loop
		}

		// now we have some free timeslots assigned, consolidate any that line up
		// next to eachother
		consolidate(freeTasks);

		// now check each task is long enough for our minimum duration
		List<Task> finalList = new ArrayList<Task>();
		for (int i = 0; i < freeTasks.size(); i++) {
			Task t = freeTasks.get(i);
			if (t.getDuration() >= duration) {
				finalList.add(t);
			}
		}

		return finalList;
	}

	/**
	 * Consolidate adjacent tasks in a list if their boundaries are shared
	 * 
	 * @param tasks
	 */
	private void consolidate(List<Task> tasks) {
		List<Task> tempList = new ArrayList<Task>();

		Task tempTask = new Task(-1, -1);
		for (int i = 0; i < tasks.size(); i++) {
			Task aTask = tasks.get(i);
			if (tempTask.getStartTime() == -1) {
				tempTask.setStartTime(aTask.getStartTime());
				tempTask.setEndTime(aTask.getEndTime());
			}
			if (i != tasks.size() - 1) {
				// look at the next element
				Task nextTask = tasks.get(i + 1);
				if (tempTask.getEndTime() == nextTask.getStartTime()) {
					// we can consolidate these 2 tasks
					tempTask.setEndTime(nextTask.getEndTime());
				}
				else {
					// there is a gap, add temp task to our list and reset it
					tempList
					    .add(new Task(tempTask.getStartTime(), tempTask.getEndTime()));
					tempTask = new Task(-1, -1);
				}
			}
			else {
				// reached the last element
				tempList.add(new Task(tempTask.getStartTime(), tempTask.getEndTime()));
			}
		}
		tasks.clear();
		tasks.addAll(tempList);
	}

	/**
	 * Find all tasks operating in a timebound t0-t1 Assume t0 and t1 are adjacent
	 * elements in the time edge list edge
	 * 
	 * @param t0
	 *          (equivalent to t_i)
	 * @param t1
	 *          (equivalent to t_i+1)
	 */
	private List<Task> getTasks(long t0, long t1) {
		List<Task> list = new ArrayList<Task>();
		Iterator<Task> it = taskList.iterator();
		while (it.hasNext()) {
			Task t = it.next();
			if (t.getStartTime() <= t0 && t.getEndTime() >= t1) {
				list.add(t);
			}
		}
		return list;
	}
}
