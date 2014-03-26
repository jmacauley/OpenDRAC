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

import java.util.Enumeration;
import java.util.Vector;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.SocketAdapter;

/**
 * ResponseTimer encapsulates response timeout notification for the TL1 engine.
 * </p></p> When the dispatcher sends a command, it creates a ResponseNotifier
 * and registers it with a timer. The timer will store the notifier in a queue,
 * sorted by the system time at which a timeout message should be sent to the
 * dispatcher (this time is managed by the response notifier). </p></p> The
 * timer runs in a loop: </p> Figure out the current system time.</p> Start
 * traversing the notifiers in the timeout queue; if their expiry time has
 * passed, send a timeout; if it has not stop (safe because they are stored in
 * sorted order).</p> Look at the timeout for the last notifier. </p> Sleep
 * until then (actually, sleep for a shorter time if necessary). </p> Repeat.
 * </p></p> When acknowledgments and output response message pieces are
 * received, the dispatcher will reset timeouts. This involves reseting the
 * expiry time for the notifier, and resorting the expiry queue. When the last
 * output response message is recieved, the dispatcher will remove response
 * notifiers. When a timeout occurs, the timer removes the notifier. @see
 * MessageDispatcher
 */
final class ResponseTimer implements Runnable {
	/**
	 * ResponseTimer notifies its dispatcher when any of its timeout periods
	 * expire.
	 */
	private MessageDispatcher dispatcher;

	/**
	 * The period (in milliseconds) that the timeout queue thread waits in between
	 * firing timeout notifications.
	 */
	private static final int WAIT_PERIOD = 5000;

	/** The thread which executes the timer's run method. */
	private Thread thread;

	/**
	 * A flag used to stop the thread cleanly.
	 */
	private volatile boolean shouldContinue = true;

	/**
	 * The collection of ResponseNotifiers whose timeouts are being monitored.
	 */
	private final Vector<ResponseNotifier> notifiers = new Vector<ResponseNotifier>();

	/**
	 * Create and return a new response timer for the specified dispatcher. The
	 * dispatcher will be notified when the timeout period of any registered
	 * response notifiers expires.
	 */
	public ResponseTimer(MessageDispatcher disp) {
		dispatcher = disp;
		SocketAdapter sa = (SocketAdapter) dispatcher.getAdapter();
		thread = new Thread(this, "TL1 Engine : ResponseTimer " + sa.getAddress()
		    + ":" + sa.getPort());
	}

	/**
	 * Add the specified notifier to the timeout queue.
	 */
	public synchronized void add(ResponseNotifier notifier) {
		// boundary check
		if (notifiers.size() < 1) {
			notifiers.addElement(notifier);
			// otherwise, insert in sorted order
		}
		else {
			insert(notifier);
		}
	}

	/**
	 * get rid of this object
	 */
	public void dispose() {
		if (shouldContinue) {
			stop();
		}

		/*
		 * anyone who is currently waiting for a message should be notified of a
		 * timeout, since the engine is going away. This is correct, since the
		 * command has been sent, but will never return.
		 */

		ResponseNotifier[] toTell = notifiers
		    .toArray(new ResponseNotifier[notifiers.size()]);

		/*
		 * cleanup. We already have our local copy of the listeners, so clear out
		 * the listeners in case the engine is also disposed dur to the timeout
		 * notification.
		 */

		notifiers.clear();

		// notify the listeners of timeouts

		for (ResponseNotifier notifier : toTell) {
			notifier.deliverTimeout(notifier.createTimeout());
		}

		dispatcher = null;
		thread = null;
	}

	/**
	 * Remove the specified notifier from the timeout queue.
	 */
	public synchronized void remove(ResponseNotifier notifier) {
		notifiers.removeElement(notifier);
	}

	/**
	 * Remove all response notifiers from the timers queue. This message is
	 * usually sent only when the dispatcher is about to suspend - we don't want
	 * timeout notifications while the TL1 engine is suspended.
	 */
	public synchronized void removeAll() {
		notifiers.removeAllElements();
	}

	/**
	 * Reset the timeout for the specified notifier. The dispatcher must have just
	 * received an acknowledgement or response with the same ctag. Tell the
	 * notifier to recalculate its expiry time, then reinsert the notifier to
	 * maintain sorted order.
	 */
	public void reset(ResponseNotifier notifier) {
		notifiers.removeElement(notifier);
		notifier.resetTimeout();
		add(notifier);
	}

	/**
	 * Execute the behavior of the response timer thread. Notify the dispatchers
	 * of any expired timeouts, wait, repeat.
	 */
	@Override
	public void run() {
		while (shouldContinue) {
			expire();

			synchronized (this) {
				try {
					wait(WAIT_PERIOD);
				}
				catch (InterruptedException exception) {
					// do nothing - this was most likely caused by a stop()
				}
			}
		}
	}

	/**
	 * Create and start the timer thread.
	 */
	public void start() {
		shouldContinue = true;
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Stop the timer thread and clean up all outstanding response notifiers.
	 */
	public void stop() {
		shouldContinue = false;
		// Enumeration enum = notifiers.elements();
		// ResponseNotifier notifier;
		// while(enum.hasMoreElements()) {
		// notifier = (ResponseNotifier) enum.nextElement();
		// // notifier.finalize();
		// }

	}

	/**
	 * Notify the dispatcher of any response notifiers whose timeout periods have
	 * passed. Remove those notifiers from the timeout queue.
	 */
	private synchronized void expire() {
		if (notifiers.isEmpty()) {
			return;
		}

		long time = System.currentTimeMillis();
		long expiry = time;

		ResponseNotifier notifier;
		Vector<ResponseNotifier> notifierClone = (Vector<ResponseNotifier>) notifiers
		    .clone();
		Enumeration<ResponseNotifier> elementList = notifierClone.elements();

		while (elementList.hasMoreElements()) {
			notifier = elementList.nextElement();
			expiry = notifier.getExpiryTime();

			// remove listeners whose timeouts have expired
			if (expiry <= time) {
				dispatcher.forward(notifier.createTimeout());
				notifiers.removeElement(notifier);
			}
			else {
				return;
			}
		}
	}

	/**
	 * Insert the specified listener in the queue, in sorted order by expiry time.
	 */
	private void insert(ResponseNotifier notifier) {
		ResponseNotifier current;
		long expiry = notifier.getExpiryTime();
		int i;

		for (i = 0; i < notifiers.size(); i++) {
			current = notifiers.elementAt(i);
			if (expiry < current.getExpiryTime()) {
				break;
			}
		}

		notifiers.insertElementAt(notifier, i);
	}
}
