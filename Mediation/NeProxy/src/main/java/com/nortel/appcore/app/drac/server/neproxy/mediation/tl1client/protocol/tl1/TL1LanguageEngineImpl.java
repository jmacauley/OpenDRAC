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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.TL1LanguageEngine;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.ConnectionDropListener;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.SocketAdapter;

/**
 * This is the external interface to the TL1 engine that sends and parses
 * messages to /from the NE. This class should provide all that is required to
 * an external "customer" of the engine. Forcing the use of this interface hides
 * the underlying implementation from the user.
 * <p>
 * NOte that the connected property ( JavaBeans ) is bound, which means that it
 * will fire a property change evevnt when it changes state.
 */

public class TL1LanguageEngineImpl implements TL1LanguageEngine,
    ConnectionDropListener// , CommAdapterByteListener
{

  private final Logger log = LoggerFactory.getLogger(getClass());
	/** flag indicating if we are connected */
	private boolean connected;

	/** the proxy for property changes */
	private PropertyChangeSupport support;

	/** the actual engine */
	private TL1Engine engine;

	/** the socket adapter */
	private SocketAdapter socketAdapter;

	/**
	 * new INSTANCE
	 */
	TL1LanguageEngineImpl() {
		connected = false;
		support = new PropertyChangeSupport(this);
	}

	/**
	 * Add a listener for autonomous messages. You will only be notified of the
	 * autonoumous code, and tids that match the args you pass into this method.
	 * 
	 * @param code
	 *          the autonomous code you are interested in.
	 * @param tid
	 *          the tid of the NE that is the source of these auto messages
	 * @param listener
	 *          the listener who is notified of auto messages
	 */

	@Override
	public void addAutonomousListener(String code, String tid,
	    ReportListener listener) {
		if (engine != null) {
			engine.register(code, tid, listener);
		}
		else {
			log.error("Engine not connected");
		}
	}

	/**
	 * This listener will be notified of all autonomous events that originate from
	 * the specified TID. <b> Use this sparingly. Having many of these listeners
	 * will impact performance.
	 */

	@Override
	public void addAutonomousListenerForAll(String tid, ReportListener listener) {
		engine.registerForAll(tid, listener);
	}

	/**
	 * Add a property change listener to the engine. This is how listeners can
	 * listen for changes such as the connection state changing. For INSTANCE your
	 * code might look like:
	 * <P>
	 * connectedListener = new PropertyChangeListener() { public void
	 * propertyChange( PropertyChangeEvent e) { // we have only listened for 1
	 * property, so // we assume it is the connected property Boolean conected =
	 * (Boolean)e.getNewValue(); handleConnected( connected.booleanValue() ); } };
	 * myTL1LanguageEngine.addPropertyChangeListener (
	 * TL1LanguageEngine.CONNECTED, connectedListener );
	 * 
	 * @param property
	 *          the property the user is interested in listening to changes in.
	 * @param listener
	 *          the listener to notify of the changes
	 */

	@Override
	public void addPropertyChangeListener(String property,
	    PropertyChangeListener listener) {
		support.addPropertyChangeListener(property, listener);
	}

	@Override
	public void closeUnderlyingSocket() {
		if (socketAdapter != null) {
			socketAdapter.close();
		}
	}

	/**
	 * try to connect to the ip and port number. note that there can only ever be
	 * a single connection at a given time. If there is a problem connecting then
	 * an IOException is thrown.
	 */

	@Override
	public void connect(String ip, int port) throws IOException {
		// tidy
		cleanEngine();
		socketAdapter = new SocketAdapter(ip, port);
		socketAdapter.connect(0);

		// we're connected, create a new log. Turn it off.
		// createLog( ip, port );
		// socketAdapter.addCommAdapterByteListener(this);
		socketAdapter.setConnectionDropListener(this);
		engine = new TL1Engine(socketAdapter);
		setConnected(true);
	}

	/**
	 * implement the interface that notified us of conenctions going away.
	 */

	@Override
	public void connectionDropped() {
		// // Vu swapped these two statement to remove the bug that
		// CONNECTION_FAILED is not notified
		// /// since the listerner was removed before then.
		setConnected(false);
		cleanEngine();
	}

	/**
	 * Create a commlog for this connecttion
	 */
	/*
	 * private void createLog(String ip, int port) { if ( log != null )
	 * log.dispose(); String IP = ip.replace('.', '-'); String file = IP + "-" +
	 * port + ".log"; // log = new CommLog( file ); log = new Log(); }
	 */
	/**
	 * destroy this INSTANCE
	 */

	@Override
	public void dispose() {
		setConnected(false);
		cleanEngine();
		support = null;
	}

	/**
	 * getMessageQueueSize method comment.
	 */

	@Override
	public int getMessageQueueSize() {
		if (engine != null) {
			return engine.getMessageQueueSize();
		}

		return 0;
	}

	/**
	 * getResponseQueueSize method comment.
	 */

	@Override
	public int getResponseQueueSize() {
		return engine.getResponseQueueSize();
	}

	/**
	 * This flag returns true when the engine is connected to the gateway. It does
	 * not neccessarily imply association, since the engine knows nothing about
	 * login state or anything else.
	 */

	@Override
	public boolean isConnected() {
		return connected;
	}

	// /**
	// * listen for data from the comm adapter
	// */
	//
	// public void received(byte[] data, int available)
	// {
	// // 
	// }

	/**
	 * remove the listener for autonomous messages. users must remove listener to
	 * avoid memory leaks
	 * 
	 * @param code
	 *          the autonomous code you are interested in.
	 * @param tid
	 *          the tid of the NE that is the source of these auto messages
	 * @param listener
	 *          the listener who is notified of auto messages
	 */

	@Override
	public void removeAutonomousListener(String code, String tid,
	    ReportListener listener) {
		if (engine != null) {
			engine.deregister(code, tid, listener);
		}
	}

	/**
	 * This listener will be notified of all autonomous events that originate from
	 * the specified TID.
	 */

	@Override
	public void removeAutonomousListenerForAll(String tid, ReportListener listener) {
		engine.deregisterForAll(tid, listener);
	}

	/**
	 * Good users of this class will remove the property change listener to avoid
	 * memory leaks.
	 * 
	 * @param property
	 *          the property listening for
	 * @param listener
	 *          the listener
	 */

	@Override
	public void removePropertyChangeListener(String property,
	    PropertyChangeListener listener) {
		support.removePropertyChangeListener(property, listener);
	}

	/**
	 * Send the command to the underlying engine.
	 * 
	 * @param command
	 *          to send to the remote engine.
	 */

	@Override
	public void send(AbstractCommand command) {
		if (engine != null) {
			command.send(engine);
		}
	}

	// public void setThreadPriority(int priority)
	// {
	// if (engine != null)
	// {
	// engine.setThreadPriority(priority);
	// }
	//
	// if (socketAdapter != null)
	// {
	// socketAdapter.setReadThreadPriority(priority);
	// }
	// }

	/**
	 * Set the connected boolean and fire off a change event
	 */

	void setConnected(boolean newValue) {
		if (connected == newValue) {
			return;
		}

		boolean old = connected;
		connected = newValue;
		support.firePropertyChange(CONNECTED, old, connected);
	}

	/**
	 * clean up the engine
	 */

	private void cleanEngine() {
		if (engine != null) {
			engine.dispose();
		}

		engine = null;

		if (socketAdapter != null) {
			socketAdapter.setConnectionDropListener(null);
			// socketAdapter.removeCommAdapterByteListener(this);
			socketAdapter.dispose();
		}

		socketAdapter = null;
	}
}
