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
 * MessageHeader parses an output message (autonomous or response message)
 * header. The rough format of TL1 output message header: </p></p> <header> ::=
 * <cr><lf><lf>^^^<system id>^<YY>-<MM>-<DD>^<hh>:<mm>:<ss> MessageHeader's
 * nextState method reads the expected whitespace, sid, and date. It will then
 * peek at the identifier data. It strips the (cr, lf) and uses the next
 * character to identify the message as an autonomous or output response
 * message. MessageHeader will replace the character and return the appropriate
 * state.
 * 
 * @see ResponseIdentifier
 * @see ReportIdentifier
 */
final class MessageHeader extends AbstractParseState {
	/** The state's "model", an object which stores parsed data. */
	private OutputMessage message;

	/**
	 * The response identifier state, returned by nextState() when the peek at the
	 * message identifier indicates an output response message.
	 */
	private final ResponseIdentifier responseId = new ResponseIdentifier();

	/**
	 * The report identifier state, returned by nextState() when the peek at the
	 * message identifier indicates an autonomous message.
	 */
	private final ReportIdentifier reportId = new ReportIdentifier();

	/**
	 * Read a system identifier and date. Peek at the message identifier to
	 * determine whether the current message is really an autonomous or output
	 * response message. Strip the leading spacing (cr, lf). Use the next
	 * character to identify the message type, but leave that character on the
	 * stream. Return the appropriate state.
	 */
	@Override
	public AbstractParseState nextState() throws IOException,
	    InterruptedException {
		message = new OutputMessage();
		String sid;
		String datestamp;
		String timestamp;

		/*
		 * the reader shouldn't change, so get it since we need it alot
		 */

		TL1Reader reader = getReader();

		/*
		 * message header should start with "/r/n/n" just skip all '/r' and '/n's in
		 * case the server is not strict
		 */
		reader.skip(TL1Reader.lineEnds);

		/*
		 * message header should then continue with "   " just skip all spaces in
		 * case the server is not strict
		 */
		reader.skip(TL1Reader.spaces);

		try {
			sid = reader.readTargetIdentifier();
		}
		catch (SyntaxException exception) {
			return logDebug("Invalid SID" + exception.getMessage(), exception);
		}
		message.setSid(sid);

		try {
			reader.read(" ");
			// reader.skip(reader.spaces);
		}
		catch (SyntaxException exception) {
			return logDebug("failed to skip space " + sid + exception.getMessage(),
			    exception);
		}

		try {
			datestamp = reader.readDate();
		}
		catch (SyntaxException exception) {
			/*
			 * only log a debug message - since we relaxed the header requirements,
			 * echoed commands now look like responses (until we try to read a date)
			 */
			return logDebug(
			    "Failed to parse date " + sid + ' ' + exception.getMessage(),
			    exception);
		}

		try {
			reader.read(" ");
			// reader.skip(reader.spaces);
		}
		catch (SyntaxException exception) {
			return logDebug("Failed to read space " + sid + ' ' + datestamp
			    + exception.getMessage(), exception);
		}

		try {
			timestamp = reader.readTime();
		}
		catch (SyntaxException exception) {
			/*
			 * only log a debug message - since we relaxed the header requirements, /
			 * echoed commands now look like responses / (until we try to read a date)
			 */
			return logDebug("Failed to read time " + sid + ' ' + datestamp + ' '
			    + exception.getMessage(), exception);
		}

		StringBuilder buff = new StringBuilder();
		buff.append(datestamp);
		buff.append(" ");
		buff.append(timestamp);

		message.setDateAndTime(buff.toString());

		// get end of message header

		reader.skip(TL1Reader.lineEnds);
		reader.skip(TL1Reader.spaces);

		/*
		 * The following code was originally implemented to read the end of the
		 * header. a problem with a DSM shelf adding an extra \r to the header
		 * necessitated relaxing the requirements with the above 2 lines of code.
		 * Modified: March 1, 2001 try { reader.read("\r\n");
		 * reader.skip(reader.spaces); } catch(SyntaxException exception) { return
		 * logError("Invalid message header end spacing: " + exception.getMessage(),
		 * message); }
		 */

		char character = reader.peek();
		if (character == 'M') {
			return nextState(responseId);
		}
		else if (character == '*' || character == 'A') {
			return nextState(reportId);
		}
		else {
			return logError("Invalid message header first character: " + character
			    + "(expected M, A or *).", message);
		}
	}

	/**
	 * Set the dispatcher
	 */
	@Override
	public void setDispatcher(Dispatcher dispatch) {
		super.setDispatcher(dispatch);
		responseId.setDispatcher(dispatch);
		reportId.setDispatcher(dispatch);
	}

	/**
	 * set the inital state
	 */
	@Override
	public void setInitialState(AbstractParseState state) {
		super.setInitialState(state);
		responseId.setInitialState(state);
		reportId.setInitialState(state);
	}

	/**
	 * set the reader
	 */
	@Override
	public void setReader(TL1Reader reader) {
		super.setReader(reader);
		responseId.setReader(reader);
		reportId.setReader(reader);
	}

	/**
	 * Return a very short string that uniquely identifies the parsing state
	 * subclass, used in parsing machine debug messages.
	 */
	@Override
	protected String getDebugId() {
		return "h";
	}

	/**
	 * Give my message resource to the specified state. Remove all references to
	 * the message. As a convenience to #nextState(), return the specified state.
	 */
	private AbstractParseState nextState(AbstractMessageIdentifier nextState) {
		nextState.setMessage(message);
		message = null;
		return nextState;
	}
}
