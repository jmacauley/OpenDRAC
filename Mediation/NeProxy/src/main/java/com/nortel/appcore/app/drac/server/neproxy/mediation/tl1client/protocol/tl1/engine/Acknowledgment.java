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

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.Response;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;

/**
 * Acknowledgment parses an acknowledgment message. The rough format of a TL1
 * acknowledgment: </p></p> <acknowledgment> ::= <acnowledgment
 * code>^<correlation tag><cr><lf>< </p></p> Acknowledgment's nextState() method
 * reads the expected ack code, ctag and terminator, populates a Response with
 * this data, and forwards the response to its dispatcher for delivery.
 */
final class Acknowledgment extends AbstractParseState {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	/** The final 3 characters of a valid acknowledgment. */
	private static final String terminator = String
	    .valueOf(TL1Constants.ACK_TERMINATOR);

	/**
	 * Read an acknowledgment code, correlation tag and a terminator. Forward a
	 * response with this data.
	 */
	@Override
	public AbstractParseState nextState() throws IOException,
	    InterruptedException {
		Response ack = new Response();

		try {
			String code = getReader().readAcknowledgementCode();
			ack.setAcknowledgementCode(code);
			// just read one space - helps the parse machine recover from errors
			getReader().read(" ");
			/*
			 * if a server ever returns no space or more than one space in its acks,
			 * then just skip over 0+ spaces, as below
			 */
			// getReader().skip(getReader().spaces);
		}
		catch (SyntaxException exception) {
			logDebug("Error parsing acknowledgment ", exception);
		}

		try {
			String ctag = getReader().readCorrelationTag();
			ack.setCorrelationTag(ctag);
		}
		catch (SyntaxException exception) {
			/*
			 * if we are in debug mode, log a message; since this is a parsing error
			 * (probably due to express echo characters) break out by returning the
			 * initial parsing state
			 */
			return logDebug(
			    "Invalid acknowledgement ctag: " + exception.getMessage(), exception);
		}

		try {
			// More forgiving than \r\n, caused errors on DSM shelf
			getReader().skip(TL1Reader.lineEnds);
			getReader().read(terminator);
			ack.setTerminator(TL1Constants.ACK_TERMINATOR);
		}
		catch (SyntaxException exception) {
			/*
			 * if we are in debug mode, log a message; since this is a parsing error
			 * (probably due to express echo characters) break out by returning the
			 * initial parsing state
			 */
			return logDebug(
			    "Invalid acknowledgement terminator: " + exception.getMessage(),
			    exception);
		}

		// if we are in debug mode, log a message
		if (logTL1Parse) {
			logForward(ack);
		}
		return forward(ack);
	}

	/**
	 * Return a very short string that uniquely identifies the parsing state
	 * subclass, used in parsing machine debug messages.
	 */
	@Override
	protected String getDebugId() {
		return "a";
	}

	/**
	 * Forward the parsed message to the dispatcher, and remove all references to
	 * that message.
	 */
	private void logForward(Response message) {
		StringBuilder buffer = new StringBuilder(40);
		buffer.append("\nPARSER: Forwarding acknowledgment ");
		buffer.append(message.getAcknowledgmentCode());
		buffer.append(' ');
		buffer.append(message.getCorrelationTag());
		
	}
}
