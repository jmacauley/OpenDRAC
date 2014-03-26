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

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic gr831 TL1 parser which should be suitable to parse most gr831ish
 * responses from the NE. This parser is able to handle quoted strings, unquoted
 * strings, and comment blocks. These methods provide a mechanism to access
 * position and name defined blocks within strings and clean up inner strings
 * (ie replace name=\"dave""was\\here\" with name=dave"was\here).
 * <p>
 * <b> Definitions </b> <br>
 * A <b> line </b> is delimited by a crlf as per gr831. These methods accept
 * only a ReplyEvent object as input which contains a vector of Strings which
 * represent lines of data from the NE.<br>
 * A <b> block </b> is delimited by : (colons) that are not contained within an
 * inner string. Block numbers begin at 0. <br>
 * An <b> item </b> is delimited by a , (comma) that is not contained within an
 * inner string. Items are contained within blocks. Item numbers begin at 0.<br>
 * A <b> comment </b> block begins or ends with a comment delimiter (/* and * /)
 * <br>
 * A <b> quoted string </b> begins and ends with a " (double quote)<br>
 * <p>
 * <b> Quoted Strings </b> <br>
 * Quoted strings are the strings that contain machine parseable information
 * which follow the gr831 standard. For this class they are strings that begin
 * and end with a " (double quote). Quoted strings can contain zero or more
 * blocks. Each block can contain 0 or more items. Each block can either be a
 * position defined block or a name defined block. A name defined block is a
 * block that contains a = (equals sign) that is not contained inside an inner
 * string.
 * <p>
 * <b> Comment blocks </b> This class works on a line by line basis, so they do
 * not detect a comment block that spans multiple lines, however since unquoted
 * strings are not parsed, the user of these methods can loop over all lines and
 * identify the start and stop of comment lines that span multiple lines. In any
 * case these methods will strip off any comment delimiters.
 * <p>
 * <b> Unquoted Strings </b> These methods do not currently parse unquoted
 * strings at all. Unquoted strings are assumed to not contain machine parseable
 * information.
 * <p>
 * <b> Name defined blocks </b> TL1Line provides a simple mechanism to parse
 * through name defined data sequentially on a line by line basis.
 * <p>
 * Sample Usage: </br>
 * 
 * <pre>
 * // Allocate a parser for our use. myData is a ReplyEvent
 * // or AutonomousEvent which contains data from the TL1Engine.
 * //
 * TL1Parser myParser = new TL1Parser(myData);
 * 
 * // loop over all the lines of TL1 data
 * //
 * while (myParser.hasMoreElements()) {
 * 	TL1Line myLine = myParser.nextElement();
 * 
 * 	Map nameItems = myLine.getAllNameItems();
 * 
 * 	// note value will not be null if the item
 * 	// is not present. If the item is not present
 * 	// then TL1Line.emptyString is returned.
 * 	//
 * 	String value = nameItems.get(someKey);
 * 
 * 	// do something with the value
 * 	//
 * }
 * </pre>
 * <p>
 * <b> Position Defined Data </b>
 * <p>
 * Position defined blocks are blocks where the order of the comma seperated
 * items defines what the data items represent. Generally, multiple lines of
 * data will have a similar structure where the same position on consecutive
 * lines will have the same meaning. For INSTANCE the AID is usually contained
 * in block 0 in all lines of quoted data.
 * <p>
 * Sample Usage: <br>
 * 
 * <pre>
 *       // In this example we have a ReplyEvent (myData) with a single line
 *       // of data.  "OC48-01:RX,NEND,ESS,7,PRTL"
 *       //
 *       TL1Parser myParser = new TL1Parser( myData );
 * 
 *       // loop over all lines of data; most position defined
 *       // data has multiple lines with a similar structure
 *       //
 *       while(myParser.hasMoreElements() ) {
 *          TL1Line myLine = myParser.nextElement();
 * 
 *          // I know I asked for OC48-01 data and I know it is
 *          // RX NEND so I ignore these items.  The data we
 *          // want is in block 1
 *          //
 *          String parm = myLine.getItem( 1, 2 ); // ESS
 *          String count= myLine.getItem( 1, 3 ); // 7
 * 
 *          // check the data was there
 *          //
 *          if ( parm == TL1Line.emptyString ||
 *               count == TL1Line.emptyString )
 *             // log an error or retry.
 *       }
 * </pre>
 * <p>
 * <b> Limitations </b> </br>
 * <ul>
 * <li>these methods currently do not support parsing of unquoted strings in the
 * same way that quoted strings are parsed. We assume unquoted strings contain
 * no machine parseable data.</li>
 * <li>it is not expected that any line of text have more than a fixed number of
 * blocks. Consult the constant TL1Line.MAX_BLOCKS_EXPECTED for the current
 * value. It should be around 25.</li>
 * <li>these methods do not currently remove any leading or trailing whitespace
 * from any blocks or items.</li>
 * <li>the name defined parsing is not currently block specific. getNextNameItem
 * loops through all name items in all name blocks.
 * </ul>
 * <p>
 * <b> How Does it Work? </b> <br>
 * When you add or create a new parser with the ReplyEvent of data you receive,
 * it loops over all lines of data, and instantiates a helper class for each
 * line (TL1Line) which holds an array of raw data and the indeces for the block
 * and item delimiters.
 * <p>
 * These methods are stingy when it comes to String instantiation. We do not
 * create all the possible Strings that could be requested by the user. Instead
 * we only create Strings when the user requests them.
 * <p>
 * Note that it is possible to "reuse" an existing INSTANCE of TL1Parser by
 * calling setData() to replace the existing data, however note that no attempt
 * has been made to make these methods thread safe. If you have more than one
 * thread writing data to an INSTANCE of TL1Parser, you may have data
 * consistency problems.
 * <p>
 * <b> Forward Compatibility Notes </b> <br>
 * This class makes no message specific assumptions as it parses. All it cares
 * about is colon delimited and comma delimited blocks of characters. It is up
 * to the users of this class to ensure that they do not enforce a specific
 * block count or parameter count when they interpret their response messages,
 * if they want to ensure forward compatibility.
 */

public final class TL1Parser {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * A Vector which contains instances of TL1Line. The TL1Line data contains the
	 * parsed data on a per line basis.
	 */
	private final Vector<TL1Line> lineData;

	/**
	 * A count of the number of lines contained in the lineData vector. This
	 * number is updated whenever the user changes the data with the addData() or
	 * setData() methods.
	 */
	private int numberOfLines;

	/**
	 * Counter used to implement our Enumeration like behaviour for the line data.
	 */
	private int enumerationCount;

	/** used to convert collection to array */
	private static final String[] stringArray = new String[0];

	/**
	 * Create an empty INSTANCE. The user must call addData() or setData() to to
	 * supply the raw data.
	 */
	public TL1Parser() {
		// allocate a vector to hold the TL1Line elements
		// We create the vector with a small initial size
		// since many responses will have fewer than 5 lines
		// in them, however, some message will be much larger
		// so we set the size to "grow" large
		//
		lineData = new Vector<TL1Line>(5, 30);
		initialize();
	}

	/**
	 * Create a new INSTANCE with the specified data.
	 * 
	 * @param myEvent
	 *          this is a Report from the TL1 Engine.
	 */
	public TL1Parser(Report myEvent) {
		// allocate a vector to hold the TL1Line elements
		// We create the vector with a small initial size
		// since many responses will have fewer than 5 lines
		// in them, however, some message will be much larger
		// so we set the size to "grow" large
		//
		lineData = new Vector<TL1Line>(5, 30);

		// add the data
		//
		setData(myEvent);
	}

	/**
	 * Create a new INSTANCE with the specified data.
	 * 
	 * @param myResponse
	 *          this is a Response from the TL1 Engine.
	 */
	public TL1Parser(Response myResponse) {
		// allocate a vector to hold the TL1Line elements
		// We create the vector with a small initial size
		// since many responses will have fewer than 5 lines
		// in them, however, some message will be much larger
		// so we set the size to "grow" large
		//
		lineData = new Vector<TL1Line>(5, 30);

		// add the data
		//
		setData(myResponse);
	}

	// /**
	// * tests.
	// */
	// public static void main(String args[])
	// {
	// // create a vector of comment strings
	// //
	// List<String> comment = new ArrayList<String>();
	// comment.add("/* comment0 ");
	// comment.add(" comment1 ");
	// comment.add(" comment2 */");
	// comment.add("/* comment3 */");
	// comment.add("/**/");
	// comment.add("/* */");
	// comment.add("/* comment6 */");
	// comment.add("/* multiline ");
	// comment.add(" multiline ");
	// comment.add(" multiline */");
	//
	// // create a parser with the comment data
	// //
	// TL1Parser parser = new TL1Parser();
	// parser.addListData(comment);
	// // print out the data
	// //
	// 
	// 
	//
	// // Since we know that all the lines of the data
	// // are comments, we can make use of the getAllLineData
	// // method
	// //
	// 
	// 
	//
	// // Lets create some quoted position defined data
	// //
	// List<String> quoted = new ArrayList<String>();
	// quoted.add("\"OC48-01:one,two,three::block3\"");
	// quoted.add("\"OC48-01:one,two,three:block2:block3\"");
	// quoted.add("\":one,two,three:block2:block3\"");
	// quoted.add("\"\"");
	// quoted.add("\" \"");
	// quoted.add("\"one:\\\"quoted, with spaces() \"\":  \\\":more\"");
	//
	// TL1Parser parser2 = new TL1Parser();
	//
	// // Try to access an empty parser
	// //
	// 
	// 
	//
	// // OK add some data
	// //
	// 
	// parser2.addListData(quoted);
	// while (parser2.hasMoreElements())
	// {
	// TL1Line myLine = parser2.nextElement();
	// 
	// }
	//
	// // Lets create some name defined data
	// //
	// List<String> name = new ArrayList<String>();
	// name.add("\"OC48-02:name1=1,name2=2:one,two:name3=3:name4=4\"");
	// name.add("\"OC48-02:name5=5,name6=6,name7=7:name8=8\"");
	// name.add("\"OC48-02-01:name1=1,name2=2,name3=3:name4=4\"");
	// name.add("\"OC48-02-01:name5=5,name6=6,name7=7:name8=8\"");
	// name.add("\"OC48-03:date=99-01-01\"");
	// name.add("\":name1=1,name2=2,name3=3:name4=4\"");
	// name.add("\":name5=5,name6=6,name7=7:name8=8\"");
	// name.add("\"::::\"");
	// name.add("\"::,,,,::\"");
	// name.add("\":quote=\\\"quoted string():,\\\",seven=7\"");
	//
	// // name defined parser
	// //
	// TL1Parser parser3 = new TL1Parser();
	//
	// parser3.addListData(name);
	// 
	// 
	//
	// 
	//
	// } // endmain

	/**
	 * Add an array of line strings to the parser
	 * 
	 * @param dataToAdd
	 *          an array of strings ( of the format that comes out of the repsonse
	 *          messages
	 */
	public int addArrayData(String[] dataToAdd) {
		int noOfCommentedLines = dataToAdd.length;

		// loop over the input data, and convert it to something
		// we can use
		//
		for (String lineString : dataToAdd) {
			// I don't think it is possible to add null to a vector,
			// but better safe than sorry.
			//
			if (lineString != null) {
				// create a new line and add it to the lineData vector
				//
				TL1Line aLine = new TL1Line(lineString);

				// check that the parsing was OK.
				//
				if (aLine.isParsedOK()) {
					// add the new line to the vector
					//
					lineData.addElement(aLine);
					if (aLine.isCommentLine()) {
						noOfCommentedLines--;
					}
				}
				else {
					log.error("TL1Line could not parse : " + lineString);
				}
			} // lineData != null
		} // for count

		// set our local copy of the vector size
		//
		numberOfLines = lineData.size();
		return noOfCommentedLines;

	} // endclass

	/**
	 * Add the supplied data by appending to the end of the existing data. This
	 * does not replace any existing data.
	 * 
	 * @param myEvent
	 *          a Response from the engine
	 */
	public void addData(Response myEvent) {
		// The user should not pass in null
		//
		if (myEvent == null) {
			// log an error
			//
			log.error("TL1Parser: null reply event");
			return;
		}

		// add the data
		//
		addListData(myEvent.getData());
	}

	/**
	 * Parse the vector of strings and add them to our internal vector. We provide
	 * this method since users might want to tweak the data before stuffing it
	 * into the parser.
	 * 
	 * @param dataToAdd
	 *          the vector of Strings from the language engine
	 */
	/*
	 * public int addVectorData( Vector dataToAdd ) { // if the vector is null log
	 * an error // if ( dataToAdd == null ) { //log an error //
	 * log.err("TL1Parser: null vector"); return -1; } String [] data = (String[])
	 * dataToAdd.toArray( stringArray ); return addArrayData( data ); }
	 */
	public int addListData(List<String> dataToAdd) {
		// if the list is null log an error
		//
		if (dataToAdd == null) {
			// log an error
			//
			log.error("TL1Parser: null list");
			return -1;
		}

		String[] data = dataToAdd.toArray(stringArray);
		return addArrayData(data);
	}

	/**
	 * Allow the user to clear out the parser so that it can be re-used.
	 */
	public void clear() {
		initialize();
	}

	/**
	 * Return a String that is the concatenation of all the parsed line data. Each
	 * line has \n appended to the end. This is usefull if the user was parsing a
	 * multiline comment block (or unquoted string) and simply wanted the raw
	 * data. For INSTANCE if there were two lines of data:
	 * 
	 * <pre>
	 * /* line 1 comment (comment end delimiter)
	 * /* line 2 comment (comment end delimiter)
	 * </pre>
	 * 
	 * Then calling this routine would return the following String:
	 * 
	 * <pre>
	 * &quot; line 1 comment \n line 2 comment \n&quot;
	 * </pre>
	 * 
	 * @return a string representing all lines of parsed data or
	 *         TL1Line.emptyString if there is no data to return NOTE: This method
	 *         will operate on the parsedReplies eg.
	 *         parsedReplies.getAllCommentData() What you will get back is all the
	 *         comment lines. If you perform this on a linked message, you only
	 *         get the comment lines FOR THAT LINKED MESSAGE, not for the whole
	 *         message. Thus if you want all the comment lines at one shot, the do
	 *         it only when you have the COMPLD message.
	 */
	public String getAllCommentData() {
		// check the number of lines
		//
		if (numberOfLines == 0) {
			return TL1Line.emptyString;
		}

		// it is most efficient to use a StringBuilder
		// to concatinate the strings together
		//
		StringBuilder buffer = new StringBuilder();
		TL1Line line;

		// get all the line data from the vector
		//
		while (hasMoreElements()) {
			// get this line
			//
			line = nextElement();
			if (line != null && line.isCommentLine()) {
				// append the data to the StringBuilder
				//
				buffer.append(line.getLineData());

				// append a lf
				//
				buffer.append("\n");
			}
		}
		// return the string representation of the data
		//
		return buffer.toString();
	}

	/**
	 * Return a String that is the concatenation of all the parsed line data. Each
	 * line has \n appended to the end. This is usefull if the user was parsing a
	 * multiline comment block (or unquoted string) and simply wanted the raw
	 * data. For INSTANCE if there were two lines of data:
	 * 
	 * <pre>
	 * /* line 1 comment (comment end delimiter)
	 * /* line 2 comment (comment end delimiter)
	 * </pre>
	 * 
	 * Then calling this routine would return the following String:
	 * 
	 * <pre>
	 * &quot; line 1 comment \n line 2 comment \n&quot;
	 * </pre>
	 * 
	 * @return a string representing all lines of parsed data or
	 *         TL1Line.emptyString if there is no data to return
	 */
	public String getAllLineData() {
		// check the number of lines
		//
		if (numberOfLines == 0) {
			return TL1Line.emptyString;
		}

		// it is most efficient to use a StringBuilder
		// to concatinate the strings together
		//
		StringBuilder buffer = new StringBuilder();
		TL1Line line;

		// get all the line data from the vector
		//
		for (int count = 0; count < numberOfLines; count++) {
			// get this line
			//
			line = lineData.elementAt(count);
			if (line != null) {
				// append the data to the StringBuilder
				//
				buffer.append(line.getLineData());

				// append a lf
				//
				buffer.append("\n");
			}
		}
		// return the string representation of the data
		//
		return buffer.toString();
	}

	/**
	 * Return true if there are more elements (lines of data) available.
	 * 
	 * @return true if there are more lines of data available
	 */
	public boolean hasMoreElements() {
		return enumerationCount < numberOfLines;
	}

	/**
	 * Get the next line of parsed data.
	 * 
	 * @return the next line of parsed data
	 */
	public TL1Line nextElement() {
		return lineData.elementAt(enumerationCount++);
	}

	/**
	 * This method replaces any existing data with the data contained in the
	 * autonomous event.
	 * 
	 * @param myEvent
	 *          The automatic report
	 */
	public void setData(Report myEvent) {
		// clear out the parser and add the new data
		//
		initialize();

		// check the event for null
		//
		if (myEvent == null) {
			// bad event. bad.
			// log an error
			//
			log.error("TL1Parser: null autonomous event");
			return;
		}

		// other wise there is probably some data
		//
		addListData(myEvent.getData());
	}

	/**
	 * This method replaces any existing data with the new data supplied. All
	 * existing data is lost.
	 * 
	 * @param myEvent
	 *          Response data from the engine.
	 */
	public void setData(Response myEvent) {
		// clear out the old data and add the new
		// data
		//
		initialize();
		addData(myEvent);
	}

	/**
	 * Create a String representation of the parsed data.
	 * 
	 * @return a string.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		for (int line = 0; line < numberOfLines; line++) {
			TL1Line myLine = lineData.elementAt(line);
			buffer.append(myLine.toString());
		}
		return buffer.toString();
	}

	/**
	 * Check the string. This routine is only used for testing purposes.
	 * 
	 * @param data
	 *          the string to check
	 * @return info about the string
	 */
	// private String checkString(String data)
	// {
	// // check for empty string or null
	// //
	// if (data == TL1Line.emptyString)
	// {
	// return "<emptyString>";
	// }
	// if (data == null)
	// {
	// return "<null>";
	// }
	//
	// return data;
	// }
	/**
	 * Initialize our variables to their defaults. Also empty the vector and
	 * hashtable if they are not null. This allows the user to re-use this
	 * INSTANCE via calls to setData().
	 */
	private void initialize() {
		// set all the variables to their default
		// values
		//
		numberOfLines = 0;
		enumerationCount = 0;

		// if the linedata vector is non null
		// then clear out the entries
		//
		if (lineData != null) {
			lineData.removeAllElements();
		}

	}
}
