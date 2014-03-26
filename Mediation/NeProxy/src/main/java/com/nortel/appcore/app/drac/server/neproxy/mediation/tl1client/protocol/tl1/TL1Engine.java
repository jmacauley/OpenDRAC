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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.CommandTemplate;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.AbstractCommAdapter;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.AbstractLanguageEngine;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.engine.MessageDispatcher;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.engine.ParseMachine;

/**
 * Transaction Language 1 (TL1) is a format for message exchange between
 * operating systems (OS) and network elements (NEs). A transaction is the
 * initiation, communication and execution of a specific action at the NE. A
 * transaction is started and executed with an exchange of related messages.
 * </p></p> TL1 messages contain routing and payload data. Routing data
 * identifies the transaction and NE; the format for routing data is the same
 * for all messages. Payload data is transaction specific; its format is often
 * very different for different messages. </p></p> The TL1 engine is a client
 * layer hiding routing information from higher client layers. The engine hides
 * the format and some content of routing data for OS-NE messages, as well as
 * the actual transmission of the message. The engine also hides all routing
 * data for NE-OS messages. </p></p> USAGE:</p></p> SENDING AN INPUT COMMAND
 * MESSAGE (OS-NE): use #send(CommandTemplate). An input command message will be
 * formatted (as per the data in the specified command template) and transmitted
 * to the network. </p></p> SENDING AN INPUT COMMAND MESSAGE (OS-NE) AND
 * REGISTERING FOR RESULTING OUTPUT RESPONSE MESSAGES (NE-OS): use
 * #send(CommandTemplate, ResponseListener, int). A command message will be
 * formatted and transmitted to the network. The specified listener will be
 * notified of any resulting response messages or timeouts. </p></p> REGISTERING
 * FOR AUTONOMOUS MESSAGES (NE-OS): use #register(String, String,
 * ReportListener). The specified listener will be notified of any autonomous
 * messages with the matching target identifier and output code. Use
 * #deregister(String, String, ReportListener) to stop notification and
 * particularly to remove a reference to your object (important for garbage
 * collection).
 * 
 * @see com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.ResponseListener
 * @see com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.ReportListener
 */
final class TL1Engine extends AbstractLanguageEngine {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	/** The default timeout period (in seconds) for TL1 commands. */
	// private static int DEFAULT_TIMEOUT = 120;

	/**
	 * The state machine which parses data received from the network. The parse
	 * machine constructs Response and Report objects, and places them on the
	 * dispatcher's queue for delivery. @see
	 * com.nt.transport.equinox.engine.protocol.tl1.engine.ParseMachine
	 */
	private ParseMachine parseMachine;

	/**
	 * The dispatcher delivers messages. </p></p> The dispatcher formats command
	 * messages and transmits them to the network. </p></p> The dispatcher also
	 * forwards parsed output response messages and autonomous messages to client
	 * listeners. The parse machine places Response and Report objects on the
	 * dispatcher's queue. For each such message, the dispatcher searches its
	 * registry for interested listeners, and delivers messages as required. @see
	 * com.nt.transport.equinox.engine.protocol.tl1.engine.MessageDispatcher
	 */
	private MessageDispatcher dispatcher;

	/**
	 * Return a new TL1Engine that is reading data from the specified comm
	 * adapter. @param adapter A connected, operational comm adapter.
	 * 
	 * @exception java.io.IOException
	 *              if the parse machine cannot create its data streams.
	 */
	public TL1Engine(AbstractCommAdapter adapter) throws IOException {
		super(adapter);

		dispatcher = new MessageDispatcher(getAdapter());
		parseMachine = new ParseMachine(getAdapter(), dispatcher);

		start();
	}

	/**
	 * Stop notifying the specified listener autonomous messages with the
	 * specified target identifer and autonomous message output code. To
	 * successfully remove a listener from the registry, the exact same parameters
	 * as used in #register should be provided here.
	 */
	public void deregister(String code, String tid, ReportListener listener) {

		if (dispatcher != null) {
			dispatcher.deregister(tid, code, listener);
		}
	}

	/**
	 * deregister the listener who is listening for all tl1 messages.
	 */
	public void deregisterForAll(String tid, ReportListener listener) {
		dispatcher.deregisterForAll(tid, listener);
	}

	/**
	 * destroy this object
	 */
	public void dispose() {
		if (parseMachine != null) {
			parseMachine.dispose();
		}

		if (dispatcher != null) {
			dispatcher.dispose();
		}

		dispatcher = null;
		parseMachine = null;

	}

	/**
	 * un pause
	 */
	public int getMessageQueueSize() {
		if (dispatcher != null) {
			return dispatcher.getMessageQueueSize();
		}
		return 0;

	}

	/**
	 * un pause
	 */
	public int getResponseQueueSize() {
		if (dispatcher != null) {
			return dispatcher.getResponseQueueSize();
		}
		return 0;

	}

	/**
	 * Notify the specified listener of any autonomous messages with the specified
	 * target identifer and autonomous message output code. @param tid The
	 * target/system identifier of a network element. @param code The type of
	 * autonomous message, eg. REPT-ALM-ALL. @param listener The listener that
	 * should be notified when autonomous messages from the specified tid of the
	 * specified type are received.
	 */
	public void register(String code, String tid, ReportListener listener) {
		// check for null here...
		// check for and ignore duplicates in engine.ReportNotifier
		if (tid == null) {
			throw new NullPointerException("Null TID");
		}

		if (code == null) {
			throw new NullPointerException("Null autonmous message output code");
		}

		if (listener == null) {
			throw new NullPointerException("Null listener");
		}

		dispatcher.register(tid, code, listener);
	}

	/**
	 * Add a listener who will be notified of all the auto TL1 messages that will
	 * be received.
	 */
	public void registerForAll(String tid, ReportListener listener) {

		// check for null here...
		// check for and ignore duplicates in engine.ReportNotifier
		if (tid == null) {
			throw new NullPointerException("Null TID");
		}

		if (listener == null) {
			throw new NullPointerException("Null listener");
		}

		dispatcher.registerForAll(tid, listener);
	}

	/**
	 * un pause
	 */
	@Override
	public void resume() {
		parseMachine.resume();
		dispatcher.resume();
		super.resume();
	}

	/**
	 * Format and send a formatted input command message to the network.
	 * 
	 * @param message
	 *          A command template containing all the information required to
	 *          format a command message.
	 */
	// public void send(CommandTemplate message)
	// {
	// if (isSuspended())
	// {
	// return;
	// }
	//
	// if (dispatcher != null)
	// {
	// try
	// {
	// dispatcher.send(message);
	// }
	// catch (IOException e)
	// {
	// throw new RuntimeException("Unable to send TL1 command " + message, e);
	// }
	// }
	// }
	/**
	 * Format and send a formatted input command message to the network. Use the
	 * default timeout period, DEFAULT_TIMEOUT. @param message A command template
	 * containing all the information required to format a command message. @param
	 * listener The object that should be notified of any output response messages
	 * are returned, or of a timeout if no messages are returned.
	 */
	// public void send(CommandTemplate message, ResponseListener listener)
	// {
	// if (isSuspended())
	// {
	// return;
	// }
	//
	// if (dispatcher != null)
	// {
	// try
	// {
	// dispatcher.send(message, listener, DEFAULT_TIMEOUT);
	// }
	// catch (IOException e)
	// {
	// throw new RuntimeException("Unable to send TL1 command " + message, e);
	// }
	// }
	// }
	/**
	 * Format and send a formatted input command message to the network.
	 * 
	 * @param message
	 *          A command template containing all the information required to
	 *          format a command message. @param listener The object that should
	 *          be notified of any output response messages are returned, or of a
	 *          timeout if no messages are returned. @param timeout A time period
	 *          in seconds. If no messages are received during this time, and all
	 *          response messages have not yet been received, the listener will be
	 *          notified of a timeout. The time should be long enough that it is
	 *          safe to assume that the network element has stopped communicating.
	 */
	public void send(CommandTemplate message, ResponseListener listener,
	    int timeout) {
		if (isSuspended()) {
			return;
		}

		if (dispatcher != null) {
			try {
				dispatcher.send(message, listener, timeout);
			}
			catch (IOException e) {
				log.error("Unable to send TL1 command " + message, e);
				throw new RuntimeException("Unable to send TL1 command " + message, e);
			}
		}
	}

	// public void setThreadPriority(int priority)
	// {
	// if (priority >= Thread.MIN_PRIORITY && priority <= Thread.MAX_PRIORITY)
	// {
	// if (dispatcher != null)
	// {
	// dispatcher.setThreadPriority(priority);
	// }
	// if (parseMachine != null)
	// {
	// parseMachine.setThreadPriority(priority);
	// }
	// }
	// }

	/**
	 * Start using the engine's comm adapter to send and receive data. Start
	 * should only be called when there are no other conflicting comms engines
	 * using the shared comm adapter.
	 */
	@Override
	public void start() {
		dispatcher.start();
		parseMachine.start();

		super.start();
	}

	/**
	 * Stop using the engine's comm adapter to send and receive data. Stop should
	 * be called as a polite warning that a conflicting comms engine will start
	 * using the comm adapter.
	 */
	@Override
	public void stop() {
		super.stop();

		parseMachine.stop();
		dispatcher.stop();
	}

	/**
	 * pause.
	 */
	@Override
	public void suspend() {
		super.suspend();
		parseMachine.suspend();
		dispatcher.suspend();
	}
}
