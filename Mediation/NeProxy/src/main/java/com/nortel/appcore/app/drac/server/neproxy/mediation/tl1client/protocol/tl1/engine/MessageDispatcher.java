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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.CommandTemplate;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.AbstractCommAdapter;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.AbstractCommsEngine;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.SocketAdapter;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.OutputMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.Report;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.ReportListener;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.Response;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.ResponseListener;

/**
 * MessageDispatcher encapsulates message routing. </p> MessageDispatcher
 * handles TL1 GR-831-CORE output messages (hereafter referred to as server
 * messages). These consist of autonomous events (hereafter referred to as
 * reports), acknowledgements and replies. The dispatcher maintains a registry
 * of listeners against each server message. When server message is received,
 * its header section is parsed for routing information (eg. correlation
 * tag/ctag, target identifier/tid, etc). If any listeners in the client
 * registry correspond to the routing information, the message is forwarded.
 * </p> MessageDispatcher handles sending command messages to the server.
 * Messages are sent via comm adapter to the server. If a reply listener is
 * specified for the command, the listener is added to the client registry. When
 * acknowledgements or replies for the command are later received, the listener
 * is notified. </p> MessageDispatcher also encapsulates command message timeout
 * periods. Each command can specify a timeout period. The dispatcher registers
 * the command with a timer. If a reply is not received before the timeout
 * expires, the command listener is notified.
 */
public final class MessageDispatcher extends AbstractCommsEngine implements
    Runnable, AbstractParseState.Dispatcher {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	public class StringCaseInsensitiveHashtable extends Hashtable {
		private static final long serialVersionUID = 8498406629481806701L;

		@Override
		public synchronized Object get(Object key) {
			if (key instanceof String) {
				key = ((String) key).toUpperCase();
			}

			return super.get(key);

		}

		@Override
		public synchronized Object put(Object key, Object value) {
			if (key instanceof String) {
				key = ((String) key).toUpperCase();
			}

			return super.put(key, value);
		}

	}

	/**
	 * The notification thread. All acknowledgement, response and autonomous
	 * message notifications are run in this thread.
	 */
	private Thread thread;

	// AUTONOMOUS MESSAGE FIELDS

	/**
	 * A queue of parsed messages. When the parse machine forwards a message to
	 * the dispatcher, the message is placed on this queue, and the dispatching
	 * thread is unblocked. The dispatch thread will forward the message
	 * appropriately.
	 */
	// private Vector messageQueue = new Vector();
	private final MessageQueue messageQueue = new MessageQueue();

	// REPLY FIELDS

	/**
	 * Storage for report listeners which registered by id and report code. The
	 * top level table contains id keys (String) and notifier values
	 * (ReportNotifier). Report notifiers store autonomous message output code
	 * keys (String) and vectors of listeners (ReportListener).
	 */
	private final Map<String, ReportNotifier> reportNotifiers = new StringCaseInsensitiveHashtable();

	/**
	 * Storage for output response notifiers. The table consists of correlation
	 * tag keys (String) and notifier values (ResponseNotifier).
	 */
	private Map<String, ResponseNotifier> responseNotifiers = new Hashtable<String, ResponseNotifier>();

	/** Storage for the current correlation tag. */
	private int ctag = 1;

	/**
	 * Watches timeout periods for the dispatcher. Places a timeout Response on
	 * the dispatcher's queue when a timeout occurs.
	 */
	private final ResponseTimer timer = new ResponseTimer(this);

	
	/**
	 * new INSTANCE
	 */
	public MessageDispatcher(AbstractCommAdapter comms) {
		super(comms);
	}

	
	/**
	 * Register the specified listener for reports against the specified target
	 * identifier (tid) and autonomous event code.
	 * 
	 * @exception NullPointerException
	 *              if the tid or code are null.
	 */
	public void deregister(String tid, String code, ReportListener listener) {
		ReportNotifier notifier = reportNotifiers.get(tid);

		if (notifier != null) {
			notifier.remove(code, listener);

			if (notifier.isEmpty()) {
				reportNotifiers.remove(tid);
			}
		}
	}

	/**
	 * deregiser for notification of all reports that are received from the
	 * specified TID.
	 */
	public void deregisterForAll(String tid, ReportListener listener) {
		ReportNotifier notifier = reportNotifiers.get(tid);

		if (notifier != null) {
			notifier.removeListenerForAll(listener);

			if (notifier.isEmpty()) {
				reportNotifiers.remove(tid);
			}
		}

	}

	/**
	 * destroy this object
	 */
	public void dispose() {
		if (isRunning()) {
			stop();
		}

		timer.dispose();

		responseNotifiers.clear();
		messageQueue.dispose();

		thread = null;
	}

	// /**
	// * Prepare for garbage collection.
	// */
	// public void finalize() {
	// stop();
	// messageQueue = null;
	// reportNotifiers = null;
	// responseNotifiers = null;
	// }

	/**
	 * Place the specified message object on the queue, for delivery by the
	 * notification thread.
	 */
	@Override
	public void forward(OutputMessage message) {
		// messageQueue.addElement(message);
		// 
		// SocketAdapter s = (SocketAdapter) getAdapter();
		// used to log in socket adaptor.
		// TL1Logger.logRecvTl1(s.getAddress(), s.getPort(), message.debugString());
		messageQueue.enqueue(message);
		synchronized (messageQueue) {
			messageQueue.notify();
		}
	}

	/**
	 * Return the size of messageQueue
	 */
	public int getMessageQueueSize() {
		return messageQueue.size();
	}

	/**
	 * Return the size of messageQueue
	 */
	public int getResponseQueueSize() {
		return responseNotifiers.size();
	}

	/**
	 * Register the specified listener for reports against the specified target
	 * identifier (tid) and autonomous event code.
	 * 
	 * @exception NullPointerException
	 *              if any of the specified parameters are null.
	 */
	public void register(String tid, String code, ReportListener listener) {

		// Strip quotes if necessary
		tid = stripQuotes(tid);
		ReportNotifier notifier = reportNotifiers.get(tid);

		if (notifier == null) {
			notifier = new ReportNotifier();
			reportNotifiers.put(tid, notifier);
		}

		notifier.add(code, listener);
	}

	/**
	 * Register the specified listener for reports against the specified target
	 * identifier (tid) and autonomous event code.
	 * 
	 * @exception NullPointerException
	 *              if any of the specified parameters are null.
	 */
	public void registerForAll(String tid, ReportListener listener) {

		// Strip quotes if necessary
		tid = stripQuotes(tid);
		ReportNotifier notifier = reportNotifiers.get(tid);

		if (notifier == null) {
			notifier = new ReportNotifier();
			reportNotifiers.put(tid, notifier);
		}

		notifier.addListenerForAll(listener);
	}

	/**
	 * Run the dispatching/notification thread. Wait for messages to arrive on the
	 * queue. Deliver them. Repeat.
	 */
	@Override
	public void run() {

		while (isRunning()) {
			try {
				waitForMessage();
				deliverMessages();
			}
			catch (Exception exception) {
				// an exception was thrown delivering a message - log the error
				log.error("Error: ", exception);
			}
		}
	}

	/**
	 * Format and send a formatted input command message to the network.
	 * 
	 * @throws IOException
	 */
	// public void send(CommandTemplate command)
	// throws IOException
	// {
	// if (!command.isValid())
	// {
	// throw new NullPointerException();
	// }
	//
	// command.setCtag(nextCorrelationTag());
	// // the Write command does the logging.
	// // 
	// getAdapter().write(command.toString());
	//
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
	 * @throws IOException
	 */
	public void send(CommandTemplate message, ResponseListener listener,
	    int timeout) throws IOException {
		if (!message.isValid()) {
			throw new NullPointerException("Message is not valid");
		}
		String ctag = nextCorrelationTag();
		message.setCtag(ctag);
		String command = message.getCommand();
		ResponseNotifier notifier = new ResponseNotifier(listener, command, ctag,
		    timeout);
		responseNotifiers.put(ctag, notifier);
		timer.add(notifier);
		getAdapter().write(message.toString() + "\r\n");
	}

	// public void setThreadPriority(int priority)
	// {
	// if (thread != null)
	// {
	// thread.setPriority(priority);
	// }
	// }

	/**
	 * Start using the engine's comm adapter to send and receive data. Start
	 * should only be called when there are no other conflicting comms engines
	 * using the shared comm adapter.
	 */
	@Override
	public void start() {
		super.start();
		SocketAdapter sa = (SocketAdapter) getAdapter();

		thread = new Thread(this, "TL1 Engine : MessageDispatcher Notifier " 
		    + sa.getAddress() + ":" + sa.getPort());
		thread.setDaemon(true);
		thread.start();
		timer.start();
	}

	/**
	 * Stop using the engine's comm adapter to send and receive data. Stop should
	 * be called as a polite warning that a conflicting comms engine will start
	 * using the comm adapter.
	 */
	@Override
	public void stop() {
		super.stop();
		wakeupMessage();
		timer.stop();
	}

	/**
	 * Suspend the dispatcher. Clear out the message queue so that timeouts won't
	 * be issued for outstanding commands.
	 */
	@Override
	public void suspend() {
		super.suspend();
		synchronized (messageQueue) {
			// messageQueue = new Vector();
			messageQueue.clear();
			responseNotifiers = new Hashtable<String, ResponseNotifier>();
		}
	}

	/**
	 * Check in the registry for any listeners interested in the specified
	 * response. If there are interested listeners, forward the response to them.
	 */
	private void deliver(Report report) {
		// 
		ReportNotifier notifier = reportNotifiers.get(stripQuotes(report.getSid()));

		if (notifier == null) {
			log.warn("No listener for " + report);
			return;
		}

		notifier.runDelivery(report);
	}

	/**
	 * Check in the registry for any listeners interested in the specified
	 * response. If there are interested listeners, forward the response to them.
	 */
	private void deliver(Response response) {
		String ctag = response.getCorrelationTag();
		ResponseNotifier notifier = responseNotifiers.get(ctag);
		if (notifier == null) {
			log.warn("No listener for " + response);
			return;
		}

		if (response.isComplete() || response.isTimeout()
		    || response.isParseFailure()) {
			responseNotifiers.remove(ctag);
			timer.remove(notifier);
		}
		else { // otherwise, more message pieces should follow...
			timer.reset(notifier);
		}

		notifier.runDelivery(response);
	}

	/**
	 * Deliver all the messages currently in the queue.
	 */
	private void deliverMessages() {
		Object message;

		while (!messageQueue.isEmpty()) {
			// message = messageQueue.firstElement();
			// messageQueue.removeElement(message);
			message = messageQueue.dequeue();

			if (message instanceof Response) {
				deliver((Response) message);
			}
			if (message instanceof Report) {
				deliver((Report) message);
			}
		}
	}

	/**
	 * Generate and return a unique correlation tag.
	 */
	private synchronized String nextCorrelationTag() {
		ctag += 1;

		if (ctag < 1 || ctag > 999999) {
			ctag = 1;
		}

		return String.valueOf(ctag);
	}

	/**
	 * Return string with quotes striped if necessary
	 */
	private String stripQuotes(String quotedStr) {
		if (quotedStr.length() > 2 && quotedStr.charAt(0) == '"') {
			return quotedStr.substring(1, quotedStr.length() - 1);
		}
		return quotedStr;
	}

	/**
	 * Block until a message is available for delivery.
	 */
	private void waitForMessage() throws InterruptedException {
		synchronized (messageQueue) {
			if (messageQueue.isEmpty()) {
				messageQueue.wait();
			}
		}
	}

	/**
	 * Block until a message is available for delivery.
	 */
	private void wakeupMessage() {
		synchronized (messageQueue) {
			messageQueue.notify();
		}
	}
}
