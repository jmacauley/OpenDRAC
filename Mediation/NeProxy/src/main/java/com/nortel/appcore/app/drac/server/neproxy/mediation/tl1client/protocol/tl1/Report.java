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
 * Report encapsulates the parsed data of a TL1 autonomous message. Since
 * autonomous message and output response message syntax differ only in their
 * identifier portion, Report inherits most of its state and behaviour from the
 * base class, OutputMessage. </p></p> Refer to OutputMessage for the syntax,
 * state and behaviour related to output message header (sid, time) and text
 * block. The general syntax of the autonomous identifier is: </p></p> <auto
 * identification> ::= <cr><lf><alarm_code>^<atag>^<verb>[^modifier[^modifier]]
 * </p> <alarm_code> ::= <*C | ** | *^ | A^> </p> <atag> ::=
 * <integer>[<.><fraction>] </p></p> Note: Class name Report is used instead of
 * the more correct AutonomousMessage because the name is much shorter and most
 * (if not all) autonomous messages are reports in any case (eg. REPT ALRM COM,
 * REPT ALM EC1, etc).
 */
public final class Report extends OutputMessage {
	/**
	 * The alarm code of the autonomous message represented by this Report,
	 * ie."**", "*C", "* ", "A "
	 */
	private String alarmCode;

	/**
	 * The output code / autonomous message type / report code of the autonomous
	 * message represented by this Report, ie. REPT-ALM-ALL etc.
	 */
	private String outputCode;

	/** this contains the parsed payload data */
	private TL1Parser parsedPayload;

	public Report() {
		// default constructor currently provided only to help
		// adapt Report objects
		// into AutonomousEvent instances.

		// should be removed if no longer needed after
		// AutonomousEvent is deleted
	}

	/**
	 * Create a new Report, with header data (system identifier and date) copied
	 * from the specified OutputMessage. This method is intended for the use of
	 * the TL1 parsing state machine.
	 */
	public Report(OutputMessage message) {
		setSid(message.getSid());
		setDateAndTime(message.getDateAndTime());
	}

	/**
	 * Return a detailed string representation of this message. This method is
	 * intended for the use of the TL1 parsing machine, to provide detailed debug
	 * information on parsing errors.
	 */
	@Override
	public String debugString() {
		StringBuilder buffer = new StringBuilder(50);
		// prints the header (sid, date)
		buffer.append(super.debugString());
		buffer.append("\r\n");
		buffer.append(getAlarmCode());
		buffer.append(' ');
		buffer.append(getCorrelationTag());
		buffer.append(' ');
		buffer.append(getOutputCode());
		// prints the text block and terminator
		buffer.append(debugEndString());
		return buffer.toString();
	}

	/**
	 * return the alarm code for this report
	 */
	public String getAlarmCode() {
		return alarmCode;
	}

	/**
	 * Return a TL1 atag, or autonomously generated correlation tag, as a String.
	 * Its format is a TL1 decimal number.
	 */
	public String getAtag() {
		return getCorrelationTag();
	}

	/**
	 * Returns the TL1 autonomous message output code, or verb[ modifier1 [
	 * modifier2 ]], as a string.
	 */
	public String getOutputCode() {
		return outputCode;
	}

	/**
	 * Return the parsed payload data.
	 */
	public TL1Parser getParsedPayload() {
		if (parsedPayload == null) {
			parsedPayload = new TL1Parser(this);
		}

		return parsedPayload;
	}

	/**
	 * Sets report's alarm code, "**" | "*C" | "* " | "A ".
	 */
	public void setAlarmCode(String code) {
		alarmCode = code;
	}

	/**
	 * Set the output code:Verb value for the Autonomous Event.
	 */
	public void setOutputCode(String code) {
		outputCode = code;
	}

	/**
	 * Return a brief string representation of the response, for logging and
	 * debugging purposes.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(50);
		buffer.append("Report(Sid: ");
		buffer.append(getSid());
		buffer.append(", Alarm Code: ");
		buffer.append(getAlarmCode());
		buffer.append(", Atag: ");
		buffer.append(getCorrelationTag());
		buffer.append(", Output Code: ");
		buffer.append(getOutputCode());
		buffer.append(")");
		return buffer.toString();
	}
}
