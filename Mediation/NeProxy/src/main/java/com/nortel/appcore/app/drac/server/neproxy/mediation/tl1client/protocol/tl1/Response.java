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

/**
 * Response encapsulates the parsed data of a TL1 output response message. Since
 * autonomous message and output response message syntax differ only in their
 * identifier portion, Report inherits most of its state and behaviour from the
 * base class, OutputMessage. </p></p> Refer to OutputMessage for the syntax,
 * state and behaviour related to output message header (sid, time) and text
 * block. The general structure of a TL1 output response identifier: </p></p>
 * <response identification> ::= <cr><lf>M^^<ctag>^<completion code> </p></p>
 * Response objects are also used to notify ResponseListeners of
 * acknowledgments. Although listeners are not bothered with the successful
 * acknowledgments (ie. IP, PF), they are notified when no response messages
 * will follow (ie. OK, NG, RL). In theses cases, the listener will be notified
 * with a Reponse with a valid ack code and no other state. NOTE that if no
 * acknowledgement is received for a command, a Response's acknowledgment code
 * will be null. </p></p> Note that a Response object represents only a single
 * output response message piece. Previous or subsequent output response
 * messages are represented by other instances, and a Response provides no
 * access to the data of other pieces. There are two exceptions to this: the
 * command code and acknowledgment code (if received) will always be available.
 * </p></p> Response objects are also used to notify ResponseListeners of
 * timeouts. If a command is sent with a response listener, a timeout period (in
 * seconds) must also be specified. If, before all response message pieces are
 * received, the timeout period expires without receiving any messages from the
 * NE, a timeout is sent. The listener is notified with a Response object whose
 * isTimeout() method returns true. </p></p>
 * 
 * @see ResponseListener @see TL1Constants
 */
public final class Response extends OutputMessage {
	/**
	 * The command verb that initiated this response. Note that this field must be
	 * set when the command is sent, because the command verb is not included in
	 * the response.
	 */
	private String command;

	/**
	 * If an acknowledgment is received for this response's command, this is the
	 * ack code. Possible values are OK, PF, NG, RL etc. Refer to TL1Constants for
	 * a complete listing.
	 */
	private String acknowledgementCode;

	/**
	 * The completion code from the output response message's identifier. Possible
	 * values are COMPLD, DENY, etc. Refer to TL1Constants for a complete listing.
	 */
	private String completionCode;

	/** A flag indicating whether or not this Reponse represents a timeout. */
	private boolean timedOut;

	public Response() {
		// default constructor currently provided only to help adapt
		// Response objects
		// into ReplyEvent instances.

		// should be removed if no longer needed after ReplyEvent is deleted
	}

	/**
	 * Create a new Response, with header data (system identifier and date) copied
	 * from the specified OutputMessage. This method is intended for the use of
	 * the TL1 parsing state machine.
	 */
	public Response(OutputMessage message) {
		setSid(message.getSid());
		setDateAndTime(message.getDateAndTime());
	}

	/**
	 * Create a response message that represents a timeout. This method is
	 * intended for the use of the TL1 engine parsing machine.
	 */
	public Response(String ctag, boolean timedOut) {
		setCorrelationTag(ctag);
		this.timedOut = timedOut;
	}

	/**
	 * Return a detailed string representation of this message. This method is
	 * intended for the use of the TL1 parsing machine, to provide detailed debug
	 * information on parsing errors.
	 */
	@Override
	public String debugString() {
		// if this response represents an ack, it will have an ack
		// code but no sid`
		if (acknowledgementCode != null && getSid() == null) {
			return debugAckString();
		}

		StringBuilder buffer = new StringBuilder();

		// prints the header (sid, date)
		buffer.append(super.debugString());

		buffer.append("\r\nM  ");
		buffer.append(getCorrelationTag());
		buffer.append(' ');
		buffer.append(getCompletionCode());

		// prints the text block and terminator
		buffer.append(debugEndString());

		return buffer.toString();
	}

	/**
	 * If an acknowledgement was received immediately after sending the command,
	 * this will return the parse ack code. If no acknowledgement was received,
	 * this will be null.
	 */
	public String getAcknowledgmentCode() {
		return acknowledgementCode;
	}

	/** get the command */
	public String getCommand() {
		return command;
	}

	/** gett the completion code */
	public String getCompletionCode() {

		return completionCode;
	}

	/**
	 * Return true if an acknowledgement has been received.
	 */
	public boolean isAcknowledged() {
		return acknowledgementCode != null;
	}

	/**
	 * We overload this message since ; doen't necessarily mean that there are no
	 * more messages coming.
	 * 
	 * @return true if we don't expect any other messages, false if we do expect
	 *         other messages.
	 */
	@Override
	public boolean isComplete() {
		if (TL1Constants.RETRIEVE.equals(completionCode)) {
			return false;
		}

		return super.isComplete();
	}

	/**
	 * Return true if this response represents a timeout.
	 */
	public boolean isTimeout() {
		return timedOut;
	}

	/**
	 * Intended for the use of the TL1 parsing machine.
	 */
	public void setAcknowledgementCode(String code) {
		acknowledgementCode = code;
	}

	/**
	 * Intended for the use of the TL1 parsing machine.
	 */
	public void setCommand(String s) {
		command = s;
	}

	/**
	 * Intended for the use of the TL1 parsing machine.
	 */
	public void setCompletionCode(String code) {
		completionCode = code;
	}

	/**
	 * Return a brief string representation of the response.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(60);

		// if this response represents an ack, it will have an ack
		// code but no sid`
		if (acknowledgementCode != null && getSid() == null) {
			buffer.append("Acknowledgment(Ack=");
			buffer.append(getAcknowledgmentCode());
			buffer.append(", Ctag=");
			buffer.append(getCorrelationTag());
			buffer.append(")");
			return buffer.toString();
		}

		if (isTimeout()) {
			buffer.append("Timeout(Ctag=");
			buffer.append(getCorrelationTag());
			buffer.append(")");
			return buffer.toString();
		}

		buffer.append("Response(Sid: ");
		buffer.append(getSid());

		buffer.append(", Command=");
		buffer.append(getCommand());

		buffer.append(", Ctag=");
		buffer.append(getCorrelationTag());

		buffer.append(", Completion=");
		buffer.append(getCompletionCode());
		buffer.append(')');

		return buffer.toString();
	}

	/**
	 * Return a detailed string representation of this message. This method is
	 * intended for the use of the TL1 parsing machine, to provide detailed debug
	 * information on parsing errors. </p></p> In this case, since debugString()
	 * has determined that this response represents an acknowledgement, return the
	 * appropriate string.
	 */
	private String debugAckString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getAcknowledgmentCode());
		buffer.append(' ');
		buffer.append(getCorrelationTag());
		buffer.append("\r\n");
		buffer.append(terminator);
		return buffer.toString();
	}
}
