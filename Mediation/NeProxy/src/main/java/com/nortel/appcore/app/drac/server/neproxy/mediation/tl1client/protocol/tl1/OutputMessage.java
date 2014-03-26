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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OutputMessage is a generalization of Response (representing autonomous
 * messages) and Report (representing output response messages). Autonomous
 * messages and output response messages are very similar, differing only in
 * their identification. OutputMessage therefore encapsulates the fields and
 * accessors for a message's system identifier, time, correlation / autonomous
 * tag, and text block. </p></p> OutputMessage and its subclasses are basically
 * event/data classes. They are a convenient mechanism to notify the relevant
 * client objects of the contents of autonomous messages and output response
 * messages. They are typically created and populated by the TL1 engine, and
 * sent by the TL1 dispatcher. </p></p> The general syntax for an output message
 * is as follows. Refer to the design document for more details and further
 * references. </p></p> <header><identification>[<text block>]<cr><lf><;|>>
 * <header> ::= <cr><lf>^^^<sid>^<year>-<month>-<day>^<hour>:<minute>:<second>
 * <identification> ::= <autonomous message id> | <output response id> <text
 * block> ::= (<comment> | <unquoted line> | <quoted line>)+ @see Report @see
 * Response
 */
public class OutputMessage

{
  private final Logger log = LoggerFactory.getLogger(getClass());
	/** The id of the NE that generated this message. */
	private String systemIdentifier;

	/**
	 * The NE system time from the message header, in exactly the same format as
	 * sent.
	 */
	private String timestamp;

	/** The NE system time, parsed into a date. */
	private Date date;

	/** A date parser, used to translate the timestamp string into a date. */
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
	    "yy-MM-dd HH:mm:ss");

	/**
	 * An OutputMessage's correlation tag represents both the TL1 response's
	 * correlation tag and the TL1 autonomous message's atag (autonomously
	 * generated correlation tag).
	 */
	private String correlationTag;

	/**
	 * Storage for the message's text block. This vector will have one or more
	 * text lines, which may be comments, unquoted lines, or quoted lines.
	 */
	private final Vector<String> textBlock = new Vector<String>();

	/**
	 * The last character of the output message. Valid values are:</p>
	 * TL1Constants.UNKNOWN(?): the terminator for this message wasn't parsed /
	 * hasn't been set</p> TL1Constants.INCOMPLETE(>): the message will be
	 * continued; there are further pieces following</p> TL1Constants.COMPLETE(;):
	 * the message is terminated; there are no more pieces following</p>
	 */
	protected char terminator = TL1Constants.UNKNOWN;

	/**
	 * A flag indicating whether or not a syntax error was encountered when
	 * parsing the message. If true, the text block is probably not complete and
	 * the message's isComplete() method and terminator are probably invalid.
	 */
	private final boolean isParseFailure = false;

	public OutputMessage() {
	}

	/**
	 * Add a TL1 text line to the message's text block. This method is intended
	 * for the use of the TL1 engine parsing machine only. @param line The TL1
	 * text line may be a comment, unquoted line, or quoted line.
	 */
	public void addData(String line) {
		textBlock.addElement(line);
	}

	/**
	 * Return a detailed string representation of this message. This method is
	 * intended for the use of the TL1 parsing machine, to provide detailed debug
	 * information on parsing errors.
	 */
	public String debugString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append("\r\n   ");
		buffer.append(systemIdentifier);
		buffer.append(' ');
		buffer.append(timestamp);
		// if the identifier is encountered, the parse machine will have a report
		// or a response, so stop here

		return buffer.toString();
	}

	/**
	 * Return the message's tag. This may be the correlation tag for Responses, or
	 * the autonomous tag for Reports. This method is intended for the use of the
	 * TL1 engine message dispatcher only.
	 */
	public String getCorrelationTag() {
		return correlationTag;
	}

	/**
	 * Return the message's text block. @return a Vector of strings containing TL1
	 * lines. These may be comments, quoted lines or unquoted lines. Each TL1 line
	 * is stripped of leading CR, LF and any spaces. Note that CR/LFs inside a
	 * comment are NOT stripped. Also note that the vector does NOT contain the
	 * message terminator (; or >).
	 */
	public Vector<String> getData() {
		return textBlock;
	}

	/**
	 * Return the system time read from the TL1 output message header, as a
	 * java.util.Date. We delay the parsing of the date since it is expensive and
	 * might not be used.
	 */
	public Date getDate() {
		if (date == null) {
			if (timestamp != null) {
				try {
					date = dateFormat.parse(timestamp);
				}
				catch (ParseException exception) {
					// shouldn't happen -
					// the parse machine won't pass on invalid dates
					log.error("Invalid date: " + timestamp);
				}
			}

		}
		return date;
	}

	/**
	 * Return the system time as read from the TL1 output message header, ie.
	 * "YY-MM-DD HH:MM:SS".
	 */
	public String getDateAndTime() {
		return timestamp;
	}

	/**
	 * Return the system id of the NE that sent this message.
	 */
	public String getSid() {
		return systemIdentifier;
	}

	/**
	 * Return the message's terminator character. Valid values are:</p> ' ': the
	 * terminator for this message wasn't parsed / hasn't been set</p> '>': the
	 * message will be continued; there are further pieces following</p> ';': the
	 * message is terminated; there are no more pieces following</p>
	 */
	// public char getTerminator()
	// {
	// return terminator;
	// }
	/**
	 * Return the message's text block. @return an array of strings which are TL1
	 * lines (comments, quoted lines or unquoted lines). Each TL1 line is stripped
	 * of leading CR, LF and any spaces. Note that CR/LFs inside a comment are NOT
	 * stripped. Also note that the vector does NOT contain the message terminator
	 * (; or >).
	 */
	public String[] getTextBlock() {
		String[] block = new String[textBlock.size()];
		textBlock.copyInto(block);
		return block;
	}

	/**
	 * For multi-piece autonomous messages or ouput response messages, return
	 * false if more pieces are expected; return true if no more pieces are
	 * expected.
	 */
	public boolean isComplete() {
		return terminator == ';';
	}

	/**
	 * Return a flag indicating whether or not a syntax error was encountered when
	 * parsing the message. If true, the text block is probably not complete and
	 * the message's isComplete() method and terminator are probably invalid.
	 */
	public boolean isParseFailure() {
		return isParseFailure;
	}

	/**
	 * Mark this message as a parsing failure, ie. invalid syntax was encountered
	 * and the complete message was not parsed. This method is intended for the
	 * use of the TL1 engine parsing state machine.
	 */
	// public void parsingFailed()
	// {
	// isParseFailure = true;
	// }
	/**
	 * Set the message's correlation/autonomous tag. This is used to relate output
	 * response messages to the command that triggered them, and to identify
	 * autonomous messages. This method is intended for the use of the TL1 engine
	 * parsing machine only.
	 */
	public void setCorrelationTag(String ctag) {
		correlationTag = ctag;
	}

	/**
	 * Sets date and time as one string for the output message. The expected
	 * format is "YY-MM-DD HH:MM:SS". This method is intended for the use of the
	 * TL1 engine parsing machine only.
	 */
	public void setDateAndTime(String string) {
		timestamp = string;

		// if(string != null) {
		// try {
		// date = dateFormat.parse(string);
		// } catch(ParseException exception) {
		// //shouldn't happen -
		// // the parse machine won't pass on invalid dates
		// }
		// }
	}

	/**
	 * Set the message's system identfier, the tid of the NE that generated this
	 * message. This method is intended for the use of the TL1 engine parsing
	 * machine only.
	 */
	public void setSid(String sid) {
		// systemIdentifier = TL1Util.getCleanedString( sid );
		systemIdentifier = sid;
	}

	/**
	 * Set the message's terminator character. This method is intended for the use
	 * of the TL1 parsing machine. </p></p>
	 */
	public void setTerminator(char terminator) {
		this.terminator = terminator;
	}

	/**
	 * Return a detailed string representation of this message's text block and
	 * terminator. This method is intended for the use of the TL1 parsing machine,
	 * to provide detailed debug information on parsing errors.
	 */
	protected String debugEndString() {
		StringBuilder buffer = new StringBuilder();

		Enumeration<String> elementList = getData().elements();
		while (elementList.hasMoreElements()) {
			buffer.append("\r\n   ");
			buffer.append(elementList.nextElement());
		}

		buffer.append("\r\n");
		buffer.append(terminator);

		return buffer.toString();
	}

}
