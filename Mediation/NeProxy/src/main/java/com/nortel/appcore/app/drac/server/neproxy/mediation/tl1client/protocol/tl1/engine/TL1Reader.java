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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;

/**
 * TL1Reader provides an API for reading high-level TL1 information units from
 * an input stream. It provides simple, blocking methods to read things as
 * simple as TL1 identifiers to units as complex as TL1 comment lines. TL1Reader
 * handles the two most complex areas related to parsing TL1 from an input
 * stream: syntax and incomplete input. </p></p> TL1Reader hides the low-level
 * (character by character) rules of TL1 information unit syntax. TL1 parsers no
 * longer need to know the correct TL1 date format, they can simply call
 * readDate(). If the data from the stream is invalid, the method will throw a
 * SyntaxException. </p></p> TL1Reader also hides the problem of incomplete
 * input by providing blocking read methods. When reading data chunks at a time,
 * there is always the possibility that a particular unit will span two chunks,
 * half of the unit in the chunk that isn't in the current buffer. TL1Reader
 * hides that problem by blocking until the rest of the unit is available.
 */
public final class TL1Reader {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * Stream containing data read from the server. A buffered reader is used
	 * because it supports the mark() operation (allowing peek/pushback
	 * functionality), while many readers do not.
	 */
	private BufferedReader reader;

	/**
	 * Constant defined for the #skip(char[]) method. Allows clients to skip over
	 * any number of spaces.
	 */
	public static final char[] spaces = { ' ' };

	/**
	 * Constant defined for the #skip(char[]) method. Allows clients to skip over
	 * any number of carriage returns and line feeds.
	 */
	public static final char[] lineEnds = { '\r', '\n' };

	/**
	 * Output response messages are required not to exceed 4069 bytes in length.
	 * While not strictly enforced by TL1Reader, this value is used as a boundary
	 * check to guard against the possible drop of critical characters. For
	 * example, if the last '/' of a comment is dropped...
	 */
	private static final int MAXIMUM_MESSAGE_LENGTH = 96000;

	/**
	 * Constant defined for SID valid character, the #isValidTIDChar(char). Allows
	 * the engine to validate the tid name northbound. The valid chars defined for
	 * HDX include any forms of character, digit, plus '.', '-', '_'.
	 */
	private static final char period = '.';
	private static final char hyphen = '-';
	private static final char underscore = '_';
	private static final char doubleQuote = '"';

	/**
	 * Create a new TL1Reader for the specified reader
	 */
	public TL1Reader(Reader reader) {
		if (reader instanceof BufferedReader) {
			this.reader = (BufferedReader) reader;
		}
		else {
			this.reader = new BufferedReader(reader);
		}
	}

	/**
	 * Return true if character is a valid tl1 letter, ie. a-z or A-Z.
	 */
	public static boolean isLetter(char letter) {
		return letter >= 'a' && letter <= 'z' || letter >= 'A' && letter <= 'Z';
	}

	/**
	 * Return a string of the ASCII values of the specified characters. This
	 * method is intended for debugging purposes.
	 */
	protected static void convertWhitespace(char[] characters) {
		for (int i = 0; i < characters.length; i++) {
			switch (characters[i]) {
			case '\r':
				characters[i] = 'r';
				break;
			case '\n':
				characters[i] = 'n';
				break;
			default:

			}
		}
	}

	/**
	 * Return true if the set includes the test string; return false otherwise.
	 */
	private static boolean includes(String[] set, String test) {
		for (String element : set) {
			if (test.equals(element)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return true if character is a valid tl1 digit, ie 0-9 .
	 */
	private static boolean isDigit(char digit) {
		return digit >= '0' && digit <= '9';
	}

	/**
	 * Return true if any of the strings in the set start with the test string;
	 * return false otherwise.
	 */
	private static boolean startsWith(String[] set, String test) {
		for (String element : set) {
			if (element.startsWith(test)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Read the next character from the server stream, then push it back on the
	 * stream so it appears as though the character was never read. Return the
	 * character.
	 */
	public char peek() throws IOException, InterruptedException {
		char character = read();
		pushback();
		return character;
	}

	/**
	 * Read the specified string from the server stream. @exception
	 * SyntaxException if the stream does contain exactly the same characters as
	 * the specified string.
	 */
	public String read(String expected) throws SyntaxException, IOException,
	    InterruptedException {

		char[] characters = new char[expected.length()];

		for (int i = 0; i < expected.length(); i++) {
			characters[i] = read();

			if (characters[i] != expected.charAt(i)) {
				convertWhitespace(characters);
				throw new SyntaxException(new String(characters, 0, i + 1));
			}
		}

		return expected;
	}

	/**
	 * Read a TL1 acknowledgement code. @see
	 * com.nt.transport.equinox.engine.protocol.tl1.TL1Constants.validCodes the
	 * set of valid TL1 acknowledgement codes. @exception SyntaxException if the
	 * string read does not exactly match one of the specified strings.
	 */
	public String readAcknowledgementCode() throws SyntaxException, IOException,
	    InterruptedException {
		String[] codes = TL1Constants.acknowledgementCodes;
		String code;

		// REMOVE first character from the stream
		char character = read();
		code = String.valueOf(character);

		// check the character - if it's not the start
		// of a valid acknowledgement code,
		// make sure it stays consumed and break out -
		// must remove at least one character
		// or we will enter an infinite loop
		if (!startsWith(codes, code)) {
			throw new SyntaxException(String.valueOf(character));
		}

		// REMOVE second character from the stream
		code = code + read();

		if (includes(codes, code)) {
			return code;
		}

		// the second character may be the start of a
		// valid message, so put it back on the stream
		pushback();
		throw new SyntaxException(String.valueOf(character));

	}

	/**
	 * Read one of the specified codes. Examples are autonomous message output
	 * codes or a response message completion codes. @param validCodes A set of
	 * expected values for the next sequence of characters. @exception
	 * SyntaxException if the string read does not exactly match one of the
	 * specified strings.
	 */
	public String readCode(String[] validCodes) throws SyntaxException,
	    IOException, InterruptedException {

		StringBuilder buffer = new StringBuilder();
		String code;

		while (true) {
			buffer.append(read());
			code = buffer.toString();

			if (includes(validCodes, code)) {
				return code;
			}
			else if (!startsWith(validCodes, code)) {
				throw new SyntaxException(code);
			}
		}
	}

	/**
	 * Read a TL1 comment from the server stream. Assume that leading whitespace
	 * (crlf^^^) and the first character (/) have been read from the stream. </p>
	 * comment ::= /'*' (any character except '*'/) '*'/ </p>
	 */
	public String readComment() throws SyntaxException, IOException,
	    InterruptedException {

		StringBuilder comment = new StringBuilder(500);
		comment.append('/');
		comment.append(read("*"));

		char character;
		while (comment.length() < MAXIMUM_MESSAGE_LENGTH) {
			character = read();
			comment.append(character);

			if (character == '*') {
				character = read();
				comment.append(character);

				if (character == '/') {
					return comment.toString();
				}
			}
		}

		// if execution reaches this point, the maximum
		// length for a message was exceeded
		throw new SyntaxException(
		    "The valid length for a response message was exceeded while reading a comment line. This may have been caused by a dropped comment terminator.");
	}

	/**
	 * Read a TL1 correlation tag from the server stream. </p> correlationtag ::=
	 * identifier | decimal number </p> This method does not restrict the ctag to
	 * 6 characters like TL1 (simpler code, more flexible for less compliant
	 * servers).
	 */
	public String readCorrelationTag() throws SyntaxException, IOException,
	    InterruptedException {
		char peek = peek();

		if (isLetter(peek)) {
			return readIdentifier();
		}
		else if (isDigit(peek)) {
			return readDecimalTag();
		}
		else {
			throw new SyntaxException(
			    peek
			        + " is an invalid (nonalphanumeric) first character for a correlation tag.");
		}
	}

	/**
	 * Read a TL1 datestamp from the server stream. </p> datestamp ::= YY-MM-DD
	 * </p>
	 */
	public String readDate() throws SyntaxException, IOException,
	    InterruptedException {

		StringBuilder datestamp = new StringBuilder();

		datestamp.append(readDigits());
		for (int i = 0; i < 2; i++) {
			read("-");
			datestamp.append('-');
			datestamp.append(readDigits());
		}

		return datestamp.toString();
	}

	/**
	 * Read a TL1 decimal correlation/autonomous tag from the input stream. </p>
	 * decimal tag ::= digit* [.] digit+ </p> digit ::= 0-9 </p> This method does
	 * not check for duplicate decimal points, like TL1 (simpler code, more
	 * flexible for less compliant servers).
	 */
	public String readDecimalTag() throws SyntaxException, IOException,
	    InterruptedException {

		StringBuilder buffer = new StringBuilder(10);

		char character;
		while (true) {
			character = read();

			if (isDigit(character) || character == '.') {
				buffer.append(character);
			}
			else {
				pushback();
				return buffer.toString();
			}
		}
	}

	/**
	 * Read a string of digits. Stop and return the string when a non-digit
	 * character is encountered.
	 */
	public String readDigits() throws IOException, InterruptedException {
		StringBuilder digits = new StringBuilder();
		char character = read();

		while (isDigit(character)) {
			digits.append(character);
			character = read();
		}

		pushback();
		return digits.toString();
	}

	/**
	 * Read a TL1 identifier from the server stream. <\p> identifier ::= letter
	 * (letter | digit | '-' | '_' | '.')*
	 */
	public String readIdentifier() throws SyntaxException, IOException,
	    InterruptedException {

		StringBuilder identifier = new StringBuilder(20);
		char character = read();

		if (isValidTIDChar(character)) {
			identifier.append(character);
		}
		else {
			throw new SyntaxException(
			    "Invalid first character for identifier. Character "
			        + (int) character + " is not a letter.");
		}
		while (true) {
			character = read();

			if (isValidTIDChar(character)) {
				identifier.append(character);
			}
			else {
				pushback();
				// 
				return identifier.toString();
			}
		}
	}

	/**
	 * Read a TL1 autonomous message output code from the server stream. output
	 * code ::= identifier | identifier^identifier | id^id^id
	 */
	public String readOutputCode() throws SyntaxException, IOException,
	    InterruptedException {

		StringBuilder outputCode = new StringBuilder();
		outputCode.append(readIdentifier());

		for (int i = 0; i < 3; i++) {
			if (read() == ' ') {
				outputCode.append(' ');
				outputCode.append(readIdentifier());
			}
			else {
				pushback();
				return outputCode.toString();
			}
		}

		return outputCode.toString();
	}

	/**
	 * Read a TL1 quoted line from the server stream. Assume that leading
	 * whitespace (crlf^^^) and the first character
	 * (") have been read from the stream. </p> quoted line ::= "any text,
	 * including inner strings" </p> inner string ::=
	 * \" ("" | \\ | any character but " or \)* \" </p> Note that for inner
	 * strings, this method will strip the leading \" from the stream, but will
	 * not append them to the quoted line. @see #readInnerString()
	 */
	public String readQuotedLine() throws SyntaxException, IOException,
	    InterruptedException {

		StringBuilder line = new StringBuilder(300);
		line.append('"');
		char character;

		while (line.length() < MAXIMUM_MESSAGE_LENGTH) {
			character = read();

			// check for an inner string
			if (character == '\\') {
				character = read();

				if (character == '"') {
					line.append(readInnerString());
				}
				else {
					line.append('\\');
					line.append(character);
				}
			}
			// the end, return the complete line
			else if (character == '"') {
				// The parser has detected the end of the TL1 quoted line. Now
				// we check to see if this is really the end of the line, or
				// perhaps a non-escaped internal quote. We should really
				// reject the entire meaage instead of being crafty.
				char peeker = read();

				log.debug(String.format(
				    "Found end quote now peeking at next character '%d'", (int) peeker));

				// TL1 expects a '\r' character at the end of a line followed by a '\n'.
				if (peeker == '\r') {
					line.append(character);
					pushback();
					return line.toString();
				}

				// we are not really done this line.
				// This is technically invalid but we
				// will cheat and make this a pseudo inner
				// string
				//
				pushback();
				line.append('\\');
				line.append(character);
				log.warn("Invalid quoted string sequence from NE '" + character
				    + "' will create pseudo inner string: " + line);

			}
			else {
				line.append(character);
			}
		}

		// if execution reaches this point, the maximum length
		// for a message was exceeded
		throw new SyntaxException(
		    "The valid length for a response message was exceeded while reading a quoted line. This may have been caused by a dropped comment terminator.");
	}

	/**
	 * Read a TL1 target identifier from the server stream. </p> target identifier
	 * ::= identifier(-identifier)* | text string
	 */
	public String readTargetIdentifier() throws SyntaxException, IOException,
	    InterruptedException {

		char character = peek();

		if (isValidTIDChar(character)) {
			StringBuilder tid = new StringBuilder(20);
			tid.append(readIdentifier());

			// / Should we remove this check
			while (peek() == '-') {
				tid.append(read());
				tid.append(readIdentifier());
			}

			return tid.toString();
		}
		// else if(character == '"')
		// return readTextString();
		// //not a valid first character for a tid

		// remove the character from the stream; if not removed,
		// another state
		// will have to deal with the invalid character again
		read();

		// change nonprinting carriage returns and line feeds
		// into a printable form
		if (character == '\r') {
			throw new SyntaxException("r");
		}
		else if (character == '\n') {
			throw new SyntaxException("n");
		}
		else {
			throw new SyntaxException(String.valueOf(character));
		}

	}

	/**
	 * Read a TL1 timestamp from the server stream. </p> timestamp ::= hh-mm-ss
	 * </p>
	 */
	public String readTime() throws SyntaxException, IOException,
	    InterruptedException {

		StringBuilder buf = new StringBuilder(15);
		try {
			buf.append(readDigits());
			read(":");
			buf.append(':');
			buf.append(readDigits());
			read(":");
			buf.append(':');
			buf.append(readDigits());
		}
		catch (SyntaxException exception) {
			throw new SyntaxException("Error reading time " + buf.toString() + " : "
			    + exception.getMessage(), exception);
		}

		return buf.toString();
	}

	/**
	 * Read a TL1 unquoted line from the server stream. </p> unquoted line ::=
	 * pretty much any valid TL1 char except cr, ; or > Note that this method
	 * expects that line header spacing (<cr><lf>^^^) is already stripped. Without
	 * stripping the header spacing first, the calling method can't know whether
	 * an unquoted line follows.
	 */
	public String readUnquotedLine() throws SyntaxException, IOException,
	    InterruptedException {
		StringBuilder line = new StringBuilder();
		char character;

		character = read();
		while (character != '\r' && line.length() < MAXIMUM_MESSAGE_LENGTH) {
			line.append(character);
			character = read();
		}
		pushback();

		// throw an exception if the maximum length for a message was exceeded
		if (line.length() >= MAXIMUM_MESSAGE_LENGTH) {
			throw new SyntaxException(
			    "The valid length for a response message was exceeded while reading an unquoted line. This may have been caused by a dropped comment terminator.");
		}

		return line.toString();

	}

	/**
	 * Remove characters from the reader's server stream until a character is read
	 * that is not one of the specified skip characters. Push the last (non-skip)
	 * character back onto the stream. Note that this method may not actually skip
	 * any characters.
	 */
	public void skip(char[] skipCharacters) throws IOException,
	    InterruptedException {
		boolean skip;
		char character;
		int i;

		do {
			skip = false;

			character = read();
			for (i = 0; i < skipCharacters.length; i++) {
				if (character == skipCharacters[i]) {
					skip = true;
				}
			}
		}
		while (skip);

		pushback();
	}

	/**
	 * Remove characters from the reader's server stream until the specified
	 * character is read. Then place that character back on the stream.
	 */
	public void skipTo(char character) throws IOException, InterruptedException {
		char test;

		do {
			test = read();
		}
		while (test != character);

		pushback();
	}

	/**
	 * Remove characters from the reader's server stream until one of the
	 * specified character is read. Then place that character back on the stream.
	 */
	public void skipTo(char[] characters) throws IOException,
	    InterruptedException {
		char test;
		int i;

		while (true) {
			test = read();

			for (i = 0; i < characters.length; i++) {
				if (test == characters[i]) {
					pushback();
					return;
				}
			}
		}
	}

	/**
	 * Validate tid character. Return boolean
	 */
	boolean isValidTIDChar(char character) {
		return isLetter(character) || isDigit(character) || character == period
		    || character == hyphen || character == underscore
		    || character == doubleQuote;
	}

	/**
	 * Read one character from the input stream. Mark the position before reading,
	 * so the character can be pushed back on the stream. If the character is not
	 * available yet, block until it is available.
	 */
	char read() throws IOException, InterruptedException {
		reader.mark(1);

		int peek = 0;
		while (peek == 0) {
			peek = reader.read();

			// This is a bit of a concern. jlint notices that
			// we have a wait without being synchronized. My
			// suspiscion here is that this scenario never happens,
			// but we should log it just in case we see it.
			//
			if (peek == -1) {
				log.error("Hmmn. TL1Reader.read() peer = -1 ");
				wait();
			}
		}

		return (char) peek;
	}

	/**
	 * Push the last character read back onto the stream. This will have no effect
	 * unless the last operation was a read().
	 */
	private void pushback() throws IOException {
		reader.reset();
	}

	/**
	 * Read a TL1 inner string from the server stream. </p> inner string ::=
	 * \" ("" | \\ | any character but " or \)* \" </p> Note that this method
	 * assumes that the leading \" has already been read from the stream. The
	 * assumption should be fairly safe, because this method should only be called
	 * from readQuotedLine(). Assuming that \" has already been stripped avoids
	 * having to place both characters back on the stream.
	 */
	private String readInnerString() throws SyntaxException, IOException,
	    InterruptedException {

		StringBuilder innerString = new StringBuilder(100);
		innerString.append("\\\"");
		char character;

		while (innerString.length() < MAXIMUM_MESSAGE_LENGTH) {
			character = read();
			innerString.append(character);

			if (character == '"') {
				character = read();
				if (character == '"') {
					innerString.append('"');
				}
				else {
					// technically this is wrong, but we fake it out by
					// pretending this is correct
					//
					innerString.append('"');
					pushback();
					log.warn("Invalid inner string sequence detected from NE : \""
					    + character + "\nCreating pseudo inner string");
				}
			}
			else if (character == '\\') {
				character = read();

				if (character == '\\') {
					innerString.append('\\');
				}
				else if (character == '"') {
					innerString.append('"');
					return innerString.toString();
				}
				else {
					throw new SyntaxException("Invalid inner string sequeunce: \\"
					    + character);
				}
			}
		}

		// if execution reaches this point, the
		// maximum length for a message was exceeded
		throw new SyntaxException(
		    "The valid length for a response message was exceeded while reading an inner string. This may have been caused by a dropped comment terminator.");
	}

	/**
	 * Read a text string from the server stream. </p> text string ::=
	 * " (\" | \\ | any char but " or \)* "
	 */
	// private String readTextString()
	// throws SyntaxException, IOException, InterruptedException
	// {
	//
	// SimpleStringBuffer textString = new SimpleStringBuffer();
	// char firstQuote = read();
	// if (firstQuote == '"')
	// {
	// textString.append('"');
	// }
	// else
	// {
	// throw new SyntaxException("Invalid first character for quoted TID: " +
	// firstQuote);
	// }
	//
	// char character = read();
	//
	// while (character != '"' && textString.length() < MAXIMUM_MESSAGE_LENGTH)
	// {
	// textString.append(character);
	// character = read();
	//
	// if (character == '\\')
	// {
	// character = read();
	// if (character == '\"' || character == '\\')
	// {
	// textString.append('\\');
	// textString.append(character);
	// character = read();
	// }
	// else
	// {
	// throw new SyntaxException("Invalid text string sequence: \\" + character);
	// }
	// }
	// }
	// textString.append('"');
	//
	// // throw an exception if the maximum length for a message was exceeded
	// if (textString.length() >= MAXIMUM_MESSAGE_LENGTH)
	// {
	// throw new SyntaxException(
	// "The valid length for a response message was exceeded while reading a TID. This may have been caused by a dropped comment terminator."
	// );
	// }
	// else
	// {
	// return textString.toString();
	// }
	// }
}
