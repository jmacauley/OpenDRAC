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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.AbstractCommand;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.ReportListener;

/**
 * This is the external interface to the TL1 engine that sends and parses
 * messages to /from the NE. This class should provide all that is required to
 * an external "customer" of the engine. Forcing the use of this interface hides
 * the underlying implementation from the user.
 * <p>
 * Note that the connected property ( JavaBeans ) is bound, which means that it
 * will fire a property change event when it changes state.
 * <p>
 * Once the Engine is no longer required, it is the responsibility of whoever
 * owns the INSTANCE to dispose of it via the dispose method.
 * 
 * @see #dispose() <p>
 *      Unfortunately it is not really possible to re-connect to a engine once
 *      the connection fails. This means that generally, to reconnect the user
 *      will need to : create a new INSTANCE, connect, and then add all
 *      listeners ( ie autonomous or propertychange ) listeners.
 */
public interface TL1LanguageEngine {

	/**
	 * This is the property name that can be used with addPropertyChangeListener
	 * so that listeners can get notified of the connected property changing.
	 */
	String CONNECTED = "connected";

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
	void addAutonomousListener(String code, String tid, ReportListener listener);

	/**
	 * This listener will be notified of all autonomous events that originate from
	 * the specified TID.
	 */
	void addAutonomousListenerForAll(String tid, ReportListener listener);

	/**
	 * Add a property change listener to the engine. This is how listeners can
	 * listen for changes such as the connection state changing. For INSTANCE your
	 * code might look like:
	 * <P>
	 * 
	 * <pre>
	 * connectedListener = new PropertyChangeListener() {
	 * 	public void propertyChange(PropertyChangeEvent e) {
	 * 		// we have only listened for 1 property, so
	 * 		// we assume it is the connected property
	 * 		Boolean conected = (Boolean) e.getNewValue();
	 * 		handleConnected(connected.booleanValue());
	 * 	}
	 * };
	 * myTL1LanguageEngine.addPropertyChangeListener(TL1LanguageEngine.CONNECTED,
	 *     connectedListener);
	 * </pre>
	 * 
	 * @param property
	 *          the property the user is interested in listening to changes in.
	 * @param listener
	 *          the listener to notify of the changes
	 */
	void addPropertyChangeListener(String property,
	    PropertyChangeListener listener);

	/**
	 * Forcefully close the underlying connection to simulate a loss of assocation
	 * with the device *
	 */
	void closeUnderlyingSocket();

	/**
	 * try to connect to the ip and port number. note that there can only ever be
	 * a single connection at a given time. If there is a problem connecting then
	 * an IOException is thrown.
	 */
	void connect(String ip, int port) throws IOException;

	/**
	 * Free up all resources used by this Object. The Object is invalid after
	 * calling this method. References to this INSTANCE should be set to null.
	 */
	void dispose();

	/**
	 * Setting the priority of ParseMachine, MessageDispatcher, and CommandAdapter
	 */
	int getMessageQueueSize();

	/**
	 * Setting the priority of ParseMachine, MessageDispatcher, and CommandAdapter
	 */
	int getResponseQueueSize();

	/**
	 * This flag returns true when the engine is connected to the gateway. It does
	 * not neccessarily imply association, since the engine knows nothing about
	 * login state or anything else.
	 */
	boolean isConnected();

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
	void removeAutonomousListener(String code, String tid, ReportListener listener);

	/**
	 * remove the listener who is listening for ALL auto messages.
	 */
	void removeAutonomousListenerForAll(String tid, ReportListener listener);

	/**
	 * Good users of this class will remove the property change listener to avoid
	 * memory leaks.
	 * 
	 * @param property
	 *          the property listening for
	 * @param listener
	 *          the listener
	 */
	void removePropertyChangeListener(String property,
	    PropertyChangeListener listener);

	/**
	 * Send the command to the underlying engine.
	 * 
	 * @param command
	 *          to send to the remote engine.
	 */
	void send(AbstractCommand command);

	/**
	 * Setting the priority of ParseMachine, MessageDispatcher, and CommandAdapter
	 */
	// void setThreadPriority(int priority);
}
