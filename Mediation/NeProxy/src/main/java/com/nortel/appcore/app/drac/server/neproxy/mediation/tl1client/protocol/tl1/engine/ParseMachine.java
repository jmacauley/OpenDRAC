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
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.AbstractCommAdapter;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.AbstractCommsEngine;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.CommAdapterListener;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms.SocketAdapter;

/**
 * ParseMachine encapsulates TL1Engine parsing behaviour, completely hiding raw
 * incoming network data. ParseMachine encapsulates the parsing thread and the
 * parsing state machine. It also manages the connection between the state
 * machine and the dispatcher, so that parsing states can forward parsed
 * messages to the dispatcher for delivery. </p></p> ParseMachine has the
 * parsing thread; creating, starting and stopping as required. ParseMachine
 * implements Runnable, and defines the parsing thread main loop in its run()
 * method. The parsing thread is therefore started by creating an appropriately
 * named thread with the machine (Runnable) as argument. </p></p> The parsing
 * thread loop operates by continually invoking nextState() on the current state
 * of the state machine. ParseState.nextState() polymporphically does the
 * appropriate work for the current state, blocking for input if necessary, and
 * returns the next state. In the case of completed messages or errors, this is
 * the initial state. </p></p> ParseMachine has a reference to its engine's
 * dispatcher. It provides this reference to its parse states, so that they can
 * forward parsed messages. </p></p> For the convenience of the parsing states,
 * ParseMachine uses a piped reader and writer to convert asynchronous-read
 * block-by-block comm adapter data into blocking-read stream data. ParseMachine
 * implements CommAdapterListener, and registers with the TL1 engine's comm
 * adapter. When it receives a block of data, the machine writes it to its piped
 * writer. The data becomes available in the machine's piped reader. Since this
 * is the reader used by the parse states, they are unblocked if they were
 * waiting for data. This trick greatly simplifies the parsing states, since
 * they no longer have to implement wait behaviour for data that spans two
 * blocks. @see MessageDispatcher @see TL1Engine @see ParseState @see
 * InitialState
 */
public final class ParseMachine extends AbstractCommsEngine implements
    Runnable, CommAdapterListener {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * MessageDispatcher delivers the messages built by the parsing machine.
	 * ParseMachine requires a dispatcher to create its parse states.
	 */
	private MessageDispatcher dispatcher;

	/**
	 * ParseMachine writes comm adapter to this object. The data will become
	 * available on the machine's reader, and may be read in a blocking stream
	 * style.
	 */
	private final PrintWriter writer;

	/**
	 * A read stream for the parsing states. Normally asynchronous block-by-block
	 * comm adapter data may be read in a blocking stream style here.
	 */
	private PipedReader reader;

	/** The parsing thread, which executes the machine's run method. */
	private Thread thread;

	/** The current state in the parsing state machine. */
	private AbstractParseState state;

	/**
	 * Create new INSTANCE
	 */
	public ParseMachine(AbstractCommAdapter adapter, MessageDispatcher dispatch)
	    throws IOException {
		super(adapter);
		dispatcher = dispatch;

		reader = new PipedReader();
		PipedWriter pwriter = new PipedWriter(reader);

		this.writer = new PrintWriter(pwriter);

		state = new InitialState();
		state.setDispatcher(dispatcher);
		state.setReader(new TL1Reader(reader));

		SocketAdapter a = (SocketAdapter) getAdapter();
		thread = new Thread(this, "TL1 Engine : Parse Machine " + a.getAddress()
		    + ":" + a.getPort());
	}

	/**
	 * destroy this object
	 */
	public void dispose() {
		if (isRunning()) {
			stop();
		}

		thread = null;

		if (reader != null) {
			try {
				reader.close();
			}
			catch (IOException ex) {
				log.error("Error: ", ex);
			}
		}
		reader = null;
		// writer = null;
		dispatcher = null;
		state = null;
	}

	/**
	 * The specified data was read by the machine's comm adapter. Write the data
	 * to the machine's piped writer, so that it will become available for
	 * blocking stream style reading on the machine's piped reader (used by the
	 * parsing states). </p></p> Part of the CommAdapterListener interface.
	 */
	@Override
	public void received(String data) {
		synchronized (writer) {
			writer.write(data);
			writer.flush();
			writer.notify();
		}
	}

	/**
	 * Resume parsing. Re-register with the comm adapter, so that the parsing
	 * machine will be notified of data again. Since the parsing machine does
	 * blocking reads, it will be unblocked by new data.
	 */
	@Override
	public void resume() {
		getAdapter().addCommAdapterListener(this);
		super.resume();
	}

	/**
	 * Execute the parsing thread loop. Ask for the next state in the state
	 * machine, incidentally executing the work of the current state. This may
	 * include reading TL1 information units, populating message objects,
	 * forwarding message objects to the dispatcher, handling syntax errors and
	 * forwarding the data stream to the next valid message.
	 */
	@Override
	public void run() {
		while (isRunning()) {
			try {
				// parse expected tokens and place them into messages
				state = state.nextState();

				/*
				 * a short pause so the gui thread doesn't starve when there is a flood
				 * of messages from the network
				 */
				// synchronized(this) {
				// wait(125);
				// }
			}
			catch (IOException exception) {
				/*
				 * this is always thrown when the connection is closed, normally or
				 * abnormally do nothing - whatever owns the adapter and engine must
				 * detect the drop and cleanup
				 */
				// exception.printStackTrace(System.err);
				break;
			}
			catch (InterruptedException exception) {
				// do nothing - probably dying
			}
		}

		if (getAdapter() instanceof SocketAdapter) {
			// Ok, today the adapter can only be an INSTANCE of socketAdapter.
			SocketAdapter a = (SocketAdapter) getAdapter();
			log.debug("ParseMachine no longer running on " + a.getAddress() + ":"
			    + a.getPort());
		}
		else {
			log.debug("ParseMachine no longer running ");
		}
		state = null;
	}

	public void setThreadPriority(int priority) {
		if (thread != null) {
			thread.setPriority(priority);
		}
	}

	/**
	 * Start using the engine's comm adapter to send and receive data. Start
	 * should only be called when there are no other conflicting comms engines
	 * using the shared comm adapter.
	 */
	@Override
	public void start() {
		super.start();

		getAdapter().addCommAdapterListener(this);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Stop using the engine's comm adapter to receive data. Stop should be called
	 * as a polite warning that a conflicting comms engine will start using the
	 * comm adapter.
	 */
	@Override
	public void stop() {
		super.stop();
		getAdapter().removeCommAdapterListener(this);
	}

	/**
	 * Suspend the parse machine. De-register from the comm adapter. No data will
	 * be received until the parse machine re-registers (in resume()) and so the
	 * parsing thread is effectively suspended.
	 */
	@Override
	public void suspend() {
		getAdapter().removeCommAdapterListener(this);
		super.suspend();
	}
}
