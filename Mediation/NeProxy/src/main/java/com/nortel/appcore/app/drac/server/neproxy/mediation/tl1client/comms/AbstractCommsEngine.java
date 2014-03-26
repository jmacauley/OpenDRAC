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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms;

import java.util.Enumeration;
import java.util.Vector;

/**
 * CommsEngine is a generalization of several comms engines. It encapsulates a
 * comm adapter and the concept of a 'running' state. It defines and manages the
 * adapter. CommsEngine's most import role is defining the start/stop/isRunning
 * API common to the TL1 engines. </p></p> CommsEngine also supports
 * registration of CommsEngineListeners. Objects can implement this interface
 * and register with comms engines for notification of suspends and resumes.
 * CommsEngines are suspended when another engine speaking a different protocol
 * needs exclusive use of a shared comm adapter. </p></p> Site Manager engines
 * must support protocol switching (ie. suspending and resuming) because of the
 * requirement to transfer files over direct cable and modem. Since some
 * messages may be dropped while file transfer is using the connection,
 * persistent application models (like alarms or pms) may become out of date.
 * These applications may register an CommsEngineListener with their language
 * engine and receive notification when the engine suspends and resumes.
 * </p></p>
 */
public abstract class AbstractCommsEngine implements ProtocolSwitch {
	/**
	 * The mechanism through which the engine exchanges text data with the
	 * network.
	 */
	private final AbstractCommAdapter adapter;

	/**
	 * A flag indicating whether or not the engine is running. Subclasses may find
	 * it convenient to reuse this flag as a stop condition for loops in run
	 * methods.
	 */
	private boolean running;

	/** A flag indicating whether or not the engine has been suspended. */
	private boolean suspended;

	/** Storage for CommsEngineListeners. */
	private final Vector<CommsEngineListener> engineListeners = new Vector<CommsEngineListener>();

	public AbstractCommsEngine(AbstractCommAdapter commsAdapter) {
		adapter = commsAdapter;
	}

	public void addListener(CommsEngineListener listener) {
		if (listener != null && !engineListeners.contains(listener)) {
			engineListeners.addElement(listener);
		}
	}

	/**
	 * Return the engine's comm adapter.
	 */
	public AbstractCommAdapter getAdapter() {
		return adapter;
	}

	/**
	 * return true if we are running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Return true if the engine is suspended. When suspended, a comms engine
	 * stops using its comm adapter and also stops any other work. The engine may
	 * be returned to a working state with resume().
	 */
	public boolean isSuspended() {
		return suspended;
	}

	/**
	 * remove the comm listener
	 */
	public void removeListener(CommsEngineListener listener) {
		engineListeners.removeElement(listener);
	}

	/**
	 * Return to a normal working (unsuspended) state. This method should only be
	 * called when it is once again safe for the engine to use its comm adapter
	 * and resume working.
	 */
	public void resume() {
		suspended = false;
		fireResumed();
	}

	/**
	 * Start using the engine's comm adapter to send and receive data. Start
	 * should only be called when there are no other conflicting comms engines
	 * using the shared comm adapter.
	 */
	public void start() {
		running = true;
	}

	/**
	 * Start the engine. This message should be sent only when it is safe for the
	 * engine to start working and using its comm adapter.</p> Part of the
	 * ProtocolSwitch interface.
	 */
	@Override
	public void startProtocol() {
		if (isRunning()) {
			resume();
		}
		else {
			start();
		}
	}

	/**
	 * Stop using the engine's comm adapter to send and receive data. Stop should
	 * be called as a polite warning that a conflicting comms engine will start
	 * using the comm adapter.
	 */
	public void stop() {
		running = false;
	}

	/**
	 * Stop the engine. Another engine needs exclusive use of the shared comm
	 * adapter; this engine should stop using the adapter, and stop all other
	 * work.</p> Part of the ProtocolSwitch interface.
	 */
	@Override
	public void stopProtocol() {
		if (isRunning()) {
			suspend();
		}
	}

	/**
	 * Suspend this engine - stop doing all work and stop using the comm adapter.
	 * The engine may be returned to a working state with resume().
	 */
	public void suspend() {
		suspended = true;
		fireSuspended();
	}

	// /**
	// * Stop communication and prepare all allocated resources for garbage
	// * collection. Note that since this method is not necessarily always
	// * called by the garbage collector, it should be called explicitly.
	// */
	// public void finalize() {
	// if(isRunning())
	// stop();
	// adapter = null;
	// }
	/**
	 * Tell all registered listeners that the engine is no longer suspended, ie it
	 * has resumed communication with the network and started working again.
	 */
	protected void fireResumed() {
		Enumeration<CommsEngineListener> listenerList = engineListeners.elements();
		CommsEngineListener listener;

		while (listenerList.hasMoreElements()) {
			listener = listenerList.nextElement();
			listener.resumed();
		}
	}

	/**
	 * Tell all registered listeners that the engine is about to suspend, ie stop
	 * temporarily cease communication and stop working.
	 */
	protected void fireSuspended() {
		Enumeration<CommsEngineListener> listenerList = engineListeners.elements();
		CommsEngineListener listener;

		while (listenerList.hasMoreElements()) {
			listener = listenerList.nextElement();
			listener.suspended();
		}
	}
}
