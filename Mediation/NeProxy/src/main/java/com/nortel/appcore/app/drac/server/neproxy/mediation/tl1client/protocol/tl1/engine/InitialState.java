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

/**
 * The parsing machine's initial state differentiates acknowledgments from the
 * other output messages, autonomous and response. Its basic responsibility is
 * to determine whether or not current message is an ack, and return the
 * appropriate next state. </p></p> A further responsibility of the initial
 * state is to recover from a syntax error. When any parse state detects a
 * syntax error, the state machine is out of sync with incoming data.
 * InitialState has the additional responsibility in this case of forwarding the
 * stream to the next message. @see Acknowledgment @see MessageHeader
 */
final class InitialState extends AbstractParseState {
	/**
	 * The acknowledgement state, returned when data indicates that the next
	 * message is probably an acknowledgment.
	 */
	private final Acknowledgment ack = new Acknowledgment();

	/**
	 * The output message header state, returned when data indicates that the next
	 * message is probably an autonomous or output response message.
	 */
	private final MessageHeader header = new MessageHeader();

	/**
	 * Marker characters that should indicate the end of the current message.
	 * These are used to forward over invalid data.
	 */
	// private static final char[] messageEnd =
	// { '\r', ';' };
	/** new INSTANCE */
	public InitialState() {
		setInitialState(this);
		ack.setInitialState(this);
		header.setInitialState(this);
	}

	/**
	 * Peek at the next character. Return the next state - Acknowledgment or
	 * MessageHeader. If the next character is invalid, skip spaces and return
	 * this state in an attempt to identify the start of the next message.
	 */
	@Override
	public AbstractParseState nextState() throws IOException,
	    InterruptedException {
		char character = getReader().peek();

		// if the next character is a letter,
		// it should be the first letter of an ack's code
		if (TL1Reader.isLetter(character)) {
			return ack;
		}
		else if (character == '\r') {
			return header;
		}
		else {
			// Otherwise, it's not the start of a valid message.
			getReader().read();

			switch (character) {
			case '\r':
				return logSkipping('r');
			case '\n':
				return logSkipping('n');
			default:

			}

			// An error has probably already been logged by
			// another state, so don't log another
			return logSkipping(character);
		}
	}

	/**
	 * set the Dispatcher to the new value
	 */
	@Override
	public void setDispatcher(Dispatcher dispatch) {
		super.setDispatcher(dispatch);
		ack.setDispatcher(dispatch);
		header.setDispatcher(dispatch);
	}

	/**
	 * Set the TL1 reader to the new value
	 */
	@Override
	public void setReader(TL1Reader reader) {
		super.setReader(reader);
		ack.setReader(reader);
		header.setReader(reader);
	}

	/**
	 * Return a very short string that uniquely identifies the parsing state
	 * subclass, used in parsing machine debug messages.
	 */
	@Override
	protected String getDebugId() {
		return "i";
	}
}
