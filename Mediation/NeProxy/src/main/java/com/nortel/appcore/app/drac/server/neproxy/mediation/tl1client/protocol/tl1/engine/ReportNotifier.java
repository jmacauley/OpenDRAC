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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.engine;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.Report;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.ReportListener;

/**
 * ReportNotifier is a wrapper class for ReportListener. It hides notification
 * and other listener-specific data and behaviour from MessageDispatcher.
 * Dispatcher has a table of report notifiers, stored by target identifier
 * (tid). When the dispatcher receives an autonomous message with a tid, it
 * looks up the notifier for that tid and sends it #runDelivery(). </p></p>
 * ReportNotifier actually handles notification for several report listeners
 * (since several listeners may register against the same tid, but different
 * output codes, and several listeners may register against the same tid and
 * code). Report notifier therefore has its own mini-registry, a table of output
 * code keys, under which are stored vectors of listeners.</p></p> When the
 * dispatcher sends #runDelivery() to a notifier, more lookup is required. The
 * notifier for a tid will determine if there are actually any listeners
 * interested in the autonomus message's output code, and will perform the
 * notification.
 * 
 * @see ResponseNotifier
 * @see MessageDispatcher
 */
final class ReportNotifier {
	/**
	 * Storage for actual ReportListeners. The table is keyed by output code
	 * (String); at each key is a vector of ReportListeners (since more than one
	 * listener could be registered against a particular tid and code).
	 */
	private final Map<String, List<ReportListener>> reportListeners = new Hashtable<String, List<ReportListener>>();

	/**
	 * this is a list of all the listeners who have requested to be notified of
	 * all the reports that are received
	 */
	private final List<ReportListener> allListeners = new ArrayList<ReportListener>(
	    5);

	/**
	 * we can optimize the all listener case by keeping a cache of the listeners
	 * in an array format. Iterating through an array is much faster than
	 * iterating though a Collection. Besides the over head of method calls to the
	 * iterator, there is the cost of casting. Every time we add or remove an all
	 * listener, we update this array. Initialize it to empty and non-null
	 */

	private ReportListener[] allArray = listenerArray;

	/** quick conversion to an array */
	private static final ReportListener[] listenerArray = new ReportListener[0];

	/**
	 * Notify the listener of the specified report (autonomous message). This
	 * method is intended for the use of ReportNotifier.Post's run() method.
	 */
	private static void deliver(ReportListener listener, Report report) {
		listener.received(report);
		// MessageDispatcher.logDebug("DELIVERING\n" + report + " " + listener);
	}

	/**
	 * Register the listener for autonomous message notification. @param code The
	 * autonomous message output code, ie. the type of autonomous message the
	 * listener is interested. @param listener The object that should be notified
	 * when any autonomous messages with the specified output code are received.
	 * Note that MessageDispatcher handles registering listeners against tids (by
	 * storing notifiers in its registry by tid). @exception NullPointerException
	 * of the code or listener are null.
	 */
	public void add(String code, ReportListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}

		List<ReportListener> listeners = reportListeners.get(code);

		if (listeners == null) {
			listeners = new ArrayList<ReportListener>();
			reportListeners.put(code, listeners);
		}

		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Add a listener who will be notified for all auto messages that are
	 * received.
	 */
	public void addListenerForAll(ReportListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}

		allListeners.add(listener);
		updateAllListenerArray();
	}

	/**
	 * Return true if the notifier contains report listeners, return false
	 * otherwise.
	 */
	public boolean isEmpty() {
		return reportListeners.isEmpty() && allListeners.isEmpty();
	}

	/**
	 * Remove the specified listener from the autonomous message notification
	 * registry.
	 * 
	 * @param code
	 *          The autonomous message output code, ie. the type of autonomous
	 *          message in which the listener was interested.
	 */
	public void remove(String code, ReportListener listener) {
		List<ReportListener> listeners = reportListeners.get(code);

		if (listeners != null) {
			listeners.remove(listener);

			if (listeners.isEmpty()) {
				reportListeners.remove(code);
			}
		}
	}

	/**
	 * remove the listener who is listening for all auto messages
	 */
	public void removeListenerForAll(ReportListener listener) {
		allListeners.remove(listener);
		updateAllListenerArray();
	}

	/**
	 * Deliver the specified report to any interested listeners. For each
	 * interested listener, post a ReportNotifier. Post on the s wing event thread
	 * for later execution. This method is called directly by the
	 * MessageDispatcher notification thread.
	 */
	public void runDelivery(Report report) {
		List<ReportListener> listeners = reportListeners
		    .get(report.getOutputCode());

		if (listeners != null) {
			ReportListener[] list = listeners.toArray(listenerArray);

			for (ReportListener element : list) {
				deliver(element, report);
			}

			// MessageDispatcher.logDebug("POSTING\n" + report + " " +
		}

		// for all the "ALL" listeners, notify them as well
		// we can avoid synchronzation on changes to the allArray
		// by grabbing it to a local var
		//
		final ReportListener all[] = allArray;

		for (ReportListener element : all) {
			deliver(element, report);
		}
	}

	/**
	 * update the cache of all listeners held in the array.
	 */
	private void updateAllListenerArray() {
		allArray = allListeners.toArray(listenerArray);
	}
}
