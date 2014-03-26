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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.util;

import java.awt.EventQueue;

/**
 * This is the 3rd version of SwingWorker (also known as SwingWorker 3), an
 * abstract class that you subclass to perform GUI-related work in a dedicated
 * thread. For instructions on using this class, see:
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html Note that
 * the API changed slightly in the 3rd version: You must now invoke start() on
 * the SwingWorker after creating it.
 */
public abstract class SwingWorker {
	/**
	 * Class to maintain reference to current worker thread under separate
	 * synchronization control.
	 */
	private static class ThreadVar {
		private Thread thread;

		ThreadVar(Thread t) {
			thread = t;
		}

		synchronized void clear() {
			thread = null;
		}

		synchronized Thread get() {
			return thread;
		}
	}

	private Object value; // see getValue(), setValue()

	// private Thread thread;

	private final ThreadVar threadVar;

	/**
	 * Start a thread that will call the <code>construct</code> method and then
	 * exit.
	 */
	public SwingWorker() {
		final Runnable doFinished = new Runnable() {
			@Override
			public void run() {
				finished();
			}
		};

		Runnable doConstruct = new Runnable() {
			@Override
			public void run() {
				try {
					setValue(construct());
				}
				finally {
					threadVar.clear();
				}

				EventQueue.invokeLater(doFinished);
			}
		};

		Thread t = new Thread(doConstruct);
		threadVar = new ThreadVar(t);
	}

	/**
	 * Compute the value to be returned by the <code>get</code> method.
	 */
	public abstract Object construct();

	/**
	 * Called on the event dispatching thread (not on the worker thread) after the
	 * <code>construct</code> method has returned.
	 */
	public void finished() {
		// Not used
	}

	/**
	 * Return the value created by the <code>construct</code> method. Returns null
	 * if either the constructing thread or the current thread was interrupted
	 * before a value was produced.
	 * 
	 * @return the value created by the <code>construct</code> method
	 */
	public Object get() {
		while (true) {
			Thread t = threadVar.get();
			if (t == null) {
				return getValue();
			}
			try {
				t.join();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // propagate
				return null;
			}
		}
	}

	

	/**
	 * Start the worker thread.
	 */
	public void start() {
		Thread t = threadVar.get();
		if (t != null) {
			t.setDaemon(true);
			t.start();
		}
	}

	/**
	 * Get the value produced by the worker thread, or null if it hasn't been
	 * constructed yet.
	 */
	protected synchronized Object getValue() {
		return value;
	}

	/**
	 * Set the value produced by worker thread
	 */
	private synchronized void setValue(Object x) {
		value = x;
	}
}
