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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.OutputMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.Report;

/**
 * ParseState is the base class of the TL1 parsing states. It defines the most
 * important public method, nextState(). The parsing thread will send this
 * message to its current state, expecting that the correct parsing work will
 * occur, and that the next state in the machine will be returned. Parsing work
 * includes reading TL1 info units, building and forwarding message objects, and
 * handling errors. </p></p> Parse states use a TL1 reader to read TL1
 * information units (eg. sid, ctag). When TL1 units are successfully read, the
 * parse state will place it in a message object. This may be an OutputMessage,
 * Response or Report, depending on the state. When a state has read all the
 * units it expects (eg. MessageHeader expects a sid and date), it will
 * determine and return the next state. If a state has read a complete message,
 * it will forward the message to its dispatcher for delivery. </p></p> ERROR
 * HANDLING. Invalid TL1 syntax is detected by the TL1Reader's high-level read
 * methods. Parse states invoke the high-level read methods; when incorrect
 * syntax is encountered, the reader will throw a SyntaxException. Handling
 * these errors is the responsibility of the parse state. It should log a
 * specific error message and return the initial state, which will attempt to
 * identify the start of the next message. All parse states are intended to
 * share the same reader, initialState and dispatcher instances. When a parse
 * state is sent a set message for any of these parts, the message should be
 * propagated to any states it owns.
 * 
 * @see TL1Reader
 */
abstract class AbstractParseState {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * An object that wants to receive Response and Report instances built by a
	 * parse state should implement this interface.
	 */
	public interface Dispatcher {
		/**
		 * Receive a populated message (Response or Report) from a parse state.
		 */
		void forward(OutputMessage message);
	}

	/**
	 * Used to read high-level TL1 information units from the network input
	 * stream.
	 */
	private TL1Reader reader;

	/**
	 * The first state in the state machine. The initial state attempts to
	 * identify and parse the beginning of a TL1 output message. When a syntax
	 * error is encountered reading an information unit, this state is returned as
	 * next; the parse machine can then attempt to recover by moving on to the
	 * next message.
	 */
	private AbstractParseState initialState;

	/** An implementation of the Dispatcher */
	private Dispatcher dispatcher;

	/** State var to turn off/on logging of parse */
	protected static final boolean logTL1Parse = false;

	/** date format */
	// private static final SimpleDateFormat dateFormat = new
	// SimpleDateFormat("yy-MM-dd HH:mm:ss:SSS");
	/**
	 * Do the work appropriate for the current place in the state machine.
	 * </p></p> The work will likely involve reading TL1 data units, populating a
	 * message object, handling any syntax errors encountered, and returning the
	 * next state. Depending on the state, message forwarding may also be
	 * required.
	 */
	public abstract AbstractParseState nextState() throws IOException,
	    InterruptedException;

	/**
	 * set the dispacther
	 */
	public void setDispatcher(Dispatcher dispatch) {
		dispatcher = dispatch;
	}

	/**
	 * set the initial state
	 */
	public void setInitialState(AbstractParseState state) {
		initialState = state;
	}

	/**
	 * set the tl1 reader
	 */
	public void setReader(TL1Reader reader) {
		this.reader = reader;
	}

	/**
	 * for the message to the dispacther
	 */
	protected final AbstractParseState forward(OutputMessage message) {
		dispatcher.forward(message);

		return initialState;
	}

	/**
	 * Return a very short string that uniquely identifies the parsing state
	 * subclass. This identifier will be used in parsing machine debug messages.
	 * Parsing debug messages can be turned on by setting ParseState.DEBUG to
	 * true.
	 */
	protected abstract String getDebugId();

	/**
	 * Return the state's TL1Reader.
	 */
	protected TL1Reader getReader() {
		return reader;
	}

	/**
	 * If we are currently in debug print mode (TL1Constants.DEBUG is true), write
	 * a timestamped status message.
	 */
	protected final AbstractParseState logDebug(String message, Throwable t) {
		if (logTL1Parse) {
			return logStatus(message, t);
		}

		return initialState;
	}

	/**
	 * Write the specified syntax error to the error log. Return the initial
	 * state, which will attempt to forward the input stream to the start of the
	 * next message.
	 */
	protected final AbstractParseState logError(String error,
	    OutputMessage message) {

		StringBuilder buffer = new StringBuilder(70);

		buffer.append("\nTL1 PROBLEM: PARSER SYNTAX ERROR: ");
		buffer.append(error);
		buffer.append('\n');

		buffer.append("<MESSAGE> ");
		buffer.append(message.debugString());
		buffer.append('\n');
		buffer.append("<END MESSAGE>");

		

		return initialState;
	}

	/**
	 * A parsing error was encountered when reading the specified message, but the
	 * message contains enough routing data to deliver it to client listeners. Log
	 * a detailed description of the problem, and forward the message anyway.
	 */
	protected final AbstractParseState logErrorAndForward(String error,
	    OutputMessage message) {
		// do not forward autonomous messages
		if (message instanceof Report) {
			return logError(error, message);
		}

		logError(error, message);
		return forward(message);
	}

	/**
	 * Invalid data was read by one of the initial states. The data may be a
	 * portion of a garbled message, or may be input data echoed by the server.
	 * Log a very brief message and return the initial state. @see InitialState @see
	 * Acknowledgement @see MessageHeader
	 */
	protected final AbstractParseState logSkipping(char character) {
		if (logTL1Parse) {
			
		}
		return initialState;
	}

	/**
	 * Invalid data was read by one of the initial states. The data may be a
	 * portion of a garbled message, or may be input data echoed by the server.
	 * Log a very brief message and return the initial state. @see InitialState @see
	 * Acknowledgement @see MessageHeader
	 */
	// protected final AbstractParseState logSkipping(String message)
	// {
	// if (logTL1Parse)
	// {
	// 
	// }
	// return initialState;
	// }
	/**
	 * Write a timestamped status message. This method is intended for the use of
	 * subclasses that are about to forward a Response or Report, or are
	 * attempting to forward the data stream to valid data again.
	 */
	protected final AbstractParseState logStatus(String message, Throwable t) {
		
		return initialState;
	}

	// /**
	// * turn on or off parsing logging
	// */
	// public static void toggleTL1Parse() {
	// logTL1Parse = ! logTL1Parse;
	// }
}
