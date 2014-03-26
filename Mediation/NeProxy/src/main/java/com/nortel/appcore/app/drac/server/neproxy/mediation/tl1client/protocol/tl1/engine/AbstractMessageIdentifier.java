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

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.OutputMessage;

/**
 * MessageIdentifier is a generalization of ReportIdentifier and
 * ResponseIdentifier. Since the next state for both classes is
 * MessageTerminator, the common functionality is factored out into this class.
 * MessageIdentifier is fairly lightweight, since its only responsibility is to
 * encapsulate a returning an initialized message terminator.
 */
abstract class AbstractMessageIdentifier extends AbstractParseState {
	/** The state's "model", an object which stores parsed data. */
	protected OutputMessage message;

	/**
	 * The next parse state after reading the autonomous or response identifer.
	 * MessageTerminator parses the text block and terminator.
	 */
	private final MessageTerminator terminator = new MessageTerminator();

	/**
	 * This method is intended for the use of ReportIdentifier and
	 * ResponseIdentifier's nextState() methods, when they are finished parsing
	 * the message identifier. Give my message object to my terminator state, and
	 * return that state.
	 */
	@Override
	public AbstractParseState nextState() throws IOException,
	    InterruptedException {
		terminator.setMessage(message);
		message = null;
		return terminator;
	}

	/**
	 * set the dispatcher
	 */
	@Override
	public void setDispatcher(Dispatcher dispatch) {
		super.setDispatcher(dispatch);
		terminator.setDispatcher(dispatch);
	}

	/**
	 * set the initial state
	 */
	@Override
	public void setInitialState(AbstractParseState state) {
		super.setInitialState(state);
		terminator.setInitialState(state);
	}

	/**
	 * Intended for the use of MessageHeader, so that it can pass the data it
	 * parsed on to this state. Concrete subclasses copy the data from this
	 * message into a Response or Report.
	 */
	public abstract void setMessage(OutputMessage message);

	/**
	 * Set the reader
	 */
	@Override
	public void setReader(TL1Reader reader) {
		super.setReader(reader);
		terminator.setReader(reader);
	}
}
