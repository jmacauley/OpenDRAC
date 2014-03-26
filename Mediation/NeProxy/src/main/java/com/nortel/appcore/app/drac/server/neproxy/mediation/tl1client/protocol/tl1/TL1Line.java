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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to hold a line of gr831 parsed data. A line of data in the
 * form of a string is passed into the constructor. The raw data is extracted
 * from the string, into an array, and the array is used as the source of the
 * data for subsequent operations.
 * <p>
 * Parsing involves determining the line type of data (quoted, unquoted or
 * comment), identifying the blocks - which are delimited by colons, identifying
 * the data parameters - which are delimited by commas, and identifying if a
 * block is name defined - name defined blocks contain an equals sign.
 * <p>
 * These methods also detect inner strings - which are delimited by \"
 * (backslash double quotes). The inner string markups \\ (backslash backslash)
 * and "" (double quote double quote) are replaced with their intended values of
 * \ and ".
 * <p>
 * We do not create any Strings until the user explicitly requests a data item.
 * <p>
 * In order to get the name items in the message, you should use
 * #getAllNameItems. This method returns a Map of the name defined parameters
 * and their values.
 * <p>
 * If you're interested in the position defined parameters, then
 * #getItem(int,int) is probably what you want.
 * 
 * @see com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Parser
 */

public final class TL1Line {
  private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * this is a simple Map that returns TL1Line.emptyString if the users asks for
	 * a key that is not conatined in the Map.
	 */
	static final class NameMap extends HashMap<String, String> {
		private static final long serialVersionUID = -8339030968677424889L;

		NameMap(int size) {
			super(size);
		}

		@Override
		public String get(Object key) {
			String result = super.get(key);
			if (result == null) {
				return TL1Line.nullValue;
			}

			return result;
		}

	}

	/**
	 * This variable holds the internal represenation of the data to be parsed.
	 */
	private char[] rawData;

	/**
	 * After parsing, this variable contains the index of the first non delimiting
	 * character of the line. This effectively "removes" any leading " or \*
	 * characters.
	 */
	private int rawStart;

	/**
	 * After parsing, this variable holds the index of the last non delimiting
	 * character in the line data. This effectively "removes" the any trailing "
	 * or comment delimiters.
	 */
	private int rawStop;

	/**
	 * After parsing this contains an array of indices which point to block
	 * delimiters. For INSTANCE if a line of text contained "dave:was:here" then
	 * this array would have 4 elements: <br>
	 * blocks[0] = 0 <br>
	 * blocks[1] = 5 <br>
	 * blocks[2] = 9 <br>
	 * blocks[3] = 14 <br>
	 */
	private int[] blocks;

	/**
	 * After parsing this is a <b> non rectangular two dimensional array </b>
	 * which contains an array of indices of parameter delimiters for every block
	 * in the message. The ordering of the array indices is
	 * [block][paramerterIndex]. For INSTANCE if a quoted message was
	 * "dave,was:here". This 2d array would contain the following values: <br>
	 * parmIndex[0][0] = 0 <br>
	 * parmIndex[0][1] = 5 <br>
	 * parmIndex[0][2] = 9 <br>
	 * parmIndex[1][0] = 9 <br>
	 * parmIndex[1][1] = 14 <br>
	 * Note that ParmIndex[0].length == 3, and parmIndex[1].length == 2 in the
	 * above example.
	 */
	private int[][] parmIndex;

	/**
	 * The number of blocks in the parsed message.
	 */
	private int numberOfBlocks;

	/**
	 * After parsing this is an array of booleans which indicate if a given block
	 * is a name defined block. GR831 defines a name defined block as any block
	 * which contains an = that is not contained within an inner string. If a
	 * quoted message was "dave:dave2=hey:more,more" then this array would
	 * contain: <br>
	 * isNameBlock[0] = false <br>
	 * isNameBlock[1] = true <br>
	 * isNameBlock[2] = false <br>
	 */
	private boolean[] isNameBlock;

	/**
	 * After parsing this boolean is true if this is a comment block. Note that
	 * for our purposes a comment line is any line that begins with a /* or ends
	 * with the comment delimiter. This class cannot detect a multiline comment,
	 * since this class only represents a single line of data.
	 */
	private boolean isComment;

	/**
	 * After parsing this flag is true if this line is a quoted line of data. Note
	 * that for our purposes, a quoted line is one which begins with a ".
	 */
	private boolean isQuoted;

	/**
	 * This is the maximum number of blocks we would ever expect in a single line
	 * of text.
	 */
	private static final int MAX_BLOCKS_EXPECTED = 25;

	/**
	 * This is the block number where AID type data can be found. Note that an AID
	 * is not required by these methods, however this constant is used internally
	 * by the getAID() .
	 */
	private static final int AID_BLOCK_NUMBER = 0;

	/** rawData as a String - after parsing */
	private String parsedString;

	/**
	 * Any method that can return a String can return emptyString if there is a
	 * problem, (ie the user has requested an item that is out of range) or if the
	 * item is empty. For INSTANCE if a message was : "dave,was::here" a request
	 * for block 1 would return TL1Line.emptyString. Note that a request for block
	 * 3 would also return emptyString. This is a real non-null string, so it is
	 * safe for string operations. This means it is safe to do: String result =
	 * myLine.getBlock(1); if (result.equals( TL1Line.emptyString )) // log an
	 * error But it is more efficient to do String result = myLine.getBlock(1); if
	 * ( result == TL1Line.emptyString) // log an error
	 */
	public static final String emptyString = "";

	public static String nullValue = "-";

	/**
	 * Any method that returns an array of Strings will return emptyArray if there
	 * was no data matching what the user requested. This is a real non-null array
	 * with zero elements, so it is safe to check the length of the array. You
	 * could either write code like: String result[] = myLine.getItems(2); if (
	 * result == TL1Line.emptyArray) // log an error or you could do something
	 * like: String result[] = myLine.getItems(2); // loop over all the items we
	 * got // for (int count=0; count < result.length; count ++ ) // do something
	 * with result[count] Note that in the second example, if there were no items
	 * in block 2, then emptyArray is returned and the loop is executed zero
	 * times.
	 */
	public static final String[] emptyArray = new String[0];

	/**
	 * A string used to create the runtime exception that is thrown when a user
	 * incorrectly accesses parsed TL1 data.
	 */
	private static final String quotedDescription = "Attempt to access an unquoted string as a quoted string.";

	/**
	 * A string used to create the exception when the user passes in a negative
	 * index for a block number.
	 */
	private static final String blockIndexString = "block index=";

	/**
	 * A string used to create the exception when the user passes in a negative
	 * index for an item number.
	 */
	private static final String itemIndexString = "item index=";

	/**
	 * A flag to indicate if the parsing was successful.
	 */
	private boolean parseOK;

	/**
	 * A block counter for the name defined enumeration like methods.
	 */
	private int blockIndex;

	/**
	 * An item counter for the name defined enumeration like methods.
	 */
	private int itemIndex;

	/**
	 * This variable holds the array of name defined information when
	 * hasMoreNameItems() returns true. nameInfo[0] holds the name, while
	 * nameInfo[1] holds the value.
	 */
	private String nameInfo[];

	/**
	 * This is a map of all the name defined parameters that were found in the
	 * parsing of the line of data
	 */
	private Map<String, String> nameItems;

	/**
	 * Since this is a utility class that should only be used by the TL1Parser, we
	 * declare the constructor as private so that only members of the same package
	 * can call it.
	 * 
	 * @param line
	 *          the line of data from the TL1 response
	 */
	TL1Line(String line) {
		// call the other constructor
		//
		this(line, true);
	}

	/**
	 * We need a second constructor so that we can create a new INSTANCE without
	 * parsing the data. This is useful for testing purposes.
	 * 
	 * @param line
	 *          the line of data
	 * @param parseFlag
	 *          do we parse the data now
	 */
	private TL1Line(String line, boolean parseFlag) {

		// safety first
		//
		if (line == null) {
			rawData = null;
			rawStop = 0;
		}
		else {
			rawStop = line.length();
			rawData = line.toCharArray();
		}

		rawStart = 0;
		blocks = null;
		isNameBlock = null;
		parmIndex = null;
		numberOfBlocks = 0;
		blockIndex = 0;
		itemIndex = 0;
		nameInfo = null;

		if (parseFlag) {
			// we parse the data
			//
			parseOK = parse();
			parsedString = new String(rawData);

			// if there was a problem we should log an error
			//
			if (!parseOK) {
				// log an error
				//
				log.error("TL1Line parse error: " + line);
			}
		}
	}

	
	/**
	 * Return the AID ie return block 0. If block 0 is empty then emptyString is
	 * returned.
	 * 
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception the caller is not forced to catch it.
	 */
	public static void setNullValue(String value) {
		nullValue = value;
	}

	/**
	 * Utility procedure used by the toString() method.
	 * 
	 * @param data
	 *          the string to check
	 * @return info about the string
	 */
	private static String checkString(String data) {
		// check for empty string or null
		//
		if (data == TL1Line.emptyString) {
			return "<emptyString>";
		}
		if (data == null) {
			return "<null>";
		}

		return data;
	}

	/**
	 * Return the AID ie return block 0. If block 0 is empty then emptyString is
	 * returned.
	 * 
	 * @return the AID String or emptyString
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception the caller is not forced to catch it.
	 */
	public String getAID() {
		// get the AID block
		//
		return getBlock(AID_BLOCK_NUMBER);
	}

	/**
	 * This is the correct way to get all the name defined parameters from a
	 * TL1Message. A Map is returned which contains only strings. The keys in the
	 * Map are the name defined parameters and the values in the map are the
	 * values in the TL1Message. for INSTANCE if this is my line of data:
	 * <p>
	 * "OC3-1:dave1=1,dave2=2:one,two,three:dave4=4"
	 * <p>
	 * Then the map that results from this method call will contain 3 items. They
	 * keys will be dave1 dave2 and dave4. The values associated with these keys
	 * in the Map are 1, 2, and 4 ( as strings ). The good news is that you "own"
	 * the Map that comes out of this method.
	 * <p>
	 * <b> Note that this Map does not return null if the key does not exist. It
	 * returns TL1Line.emptyString. </b>
	 * 
	 * @return a Map containing all the name defined parameters and there values.
	 *         Note that if you ask for an inexistant key TL1Line.emptyString is
	 *         returned.
	 */
	public Map<String, String> getAllNameItems() {
		if (nameItems == null) {
			nameItems = new NameMap(17);

			while (hasMoreNameItems()) {
				TL1Token token = getNextNameItem();
				nameItems.put(token.key, token.value);
			}
		}
		// we must clone the items since in the case
		// of a report there can be multiple listeners
		// and we don't want one listener mucking up the
		// map for another listener
		//
		return nameItems;
	}

	/**
	 * Return the requested block. Only use this method if you need the entire
	 * block of unparsed data.
	 * 
	 * @param blockNumber
	 *          the requested block number
	 * @return emptyString if the block number is invalid, or the String for the
	 *         block otherwise.
	 * @exception IndexOutOfBoundsException
	 *              if the user specifies a negative block number. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception the caller is not forced to catch it.
	 */
	public String getBlock(int blockNumber) {
		String result;

		// check this is quoted
		//
		validateQuoted();

		// if the block number is negative, throw an exception
		//
		validateBlock(blockNumber);

		// check that they have requested a valid block number
		//
		if (blockNumber >= numberOfBlocks) {
			return emptyString;
		}

		// get the String representation of the block
		//
		result = getString(parsedString, blocks[blockNumber] + 1,
		    blocks[blockNumber + 1]);

		return result;
	}

	/**
	 * Return the number of colon delimited blocks.
	 * 
	 * @return the count of the number of blocks on this line. The valid range for
	 *         blocks is from 0 to (getBlockCount() -1).
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception the caller is not forced to catch it.
	 */
	public int getBlockCount() {
		// make sure this is a quoted string
		//
		validateQuoted();

		// otherwise return the count
		//
		return numberOfBlocks;
	}

	/**
	 * Return an array of Strings which represent the subfields of a given item in
	 * the given block. The delimiter for sub items is the dash. For INSTANCE if
	 * an item was "OC48-01-01", this method would return an array with three
	 * entries. The entries would be OC48, 01 and 01. If the item has no compound
	 * pieces, then the item itself is returned. For INSTANCE if the item was
	 * "OC3" then an array with one item would be returned (OC3).
	 * 
	 * @param blockNumber
	 *          the requested block number
	 * @param itemNumber
	 *          the requested item number
	 * @return emptyArray if the user has requested an out of bounds item, or an
	 *         array of Strings containing the compound items.
	 * @exception IndexOutOfBoundsException
	 *              if the user specifies a negative block or item number. Since
	 *              this is a runtime exception it does not need to be explicitly
	 *              caught by the caller.
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 */
	public String[] getCompoundItems(int blockNumber, int itemNumber) {
		// make sure this is a quoted string
		//
		validateQuoted();

		// check the block number
		//
		validateBlock(blockNumber);

		// check the item
		//
		validateItem(itemNumber);

		return getCompoundItemsByChar(blockNumber, itemNumber, '-');
	}

	/**
	 * Return an array of Strings which represent the subfields of a given item in
	 * the given block. The delimiter for sub items is specified by the user. For
	 * INSTANCE if an item was "OC48&01&01", and the user specified the & as a
	 * delimiter, this method would return an array with three entries. The
	 * entries would be OC48, 01 and 01. If the item has no compound pieces, then
	 * the item itself is returned. For INSTANCE if the item was "OC3" then an
	 * array with one item would be returned (OC3).
	 * 
	 * @param blockNumber
	 *          the requested block number
	 * @param itemNumber
	 *          the requested item number
	 * @param delimiter
	 *          the delimiter character
	 * @return emptyArray if the user has requested an out of bounds item, or an
	 *         array of Strings containing the compound items.
	 * @exception IndexOutOfBoundsException
	 *              if the user specifies a negative block or item number. Since
	 *              this is a runtime exception it does not need to be explicitly
	 *              caught by the caller.
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 */
	public String[] getCompoundItemsUsingDelimiter(int blockNumber,
	    int itemNumber, char delimiter) {
		// this must be quoted string
		//
		validateQuoted();

		// check the block number
		//
		validateBlock(blockNumber);

		// check the item
		//
		validateItem(itemNumber);

		return getCompoundItemsByChar(blockNumber, itemNumber, delimiter);
	}

	/**
	 * Break the passed int String into pieces based on the specified delimiter.
	 * For INSTANCE if the string is "OC48-01-01" and the delimiter was '-' then
	 * an array with three elements would be returned. result[0] = OC48 result[1]
	 * = 01 and result[2] = 01.
	 * 
	 * @param value
	 *          the string to break apart
	 * @param delimiter
	 *          the delimiter to use when breaking up the String
	 * @return an array of Strings or emptyArray on error
	 */
	public String[] getCompoundUsingDelimiter(String value, char delimiter) {
		if (value == emptyString || value == null) {
			return emptyArray;
		}

		// char data [] = value.toCharArray();

		// other wise we have some kind of string
		//
		return getCompoundFromData(value, -1, value.length(), delimiter);

	}

	/**
	 * Return a String representing the specified item, in the specified block.
	 * 
	 * @param blockNumber
	 *          the requested block
	 * @param itemNumber
	 *          the requested item number
	 * @return emptyString if the item is empty and the String item if all was OK.
	 * @exception IndexOutOfBoundsException
	 *              if the user specifies a negative block or item number. Since
	 *              this is a runtime exception it does not need to be explicitly
	 *              caught by the caller.
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 */
	public String getItem(int blockNumber, int itemNumber) {
		// check this is quoted
		//
		validateQuoted();

		// check the block number
		//
		validateBlock(blockNumber);

		// if the item number is negative, throw an exception
		//
		validateItem(itemNumber);

		// check that they have requested a valid block number
		//
		if (blockNumber >= numberOfBlocks) {
			return null;
		}

		// if the item is not is range of our tables, return empty string
		//
		if (itemNumber >= parmIndex[blockNumber].length - 1) {
			return null;
		}

		// Everything's OK. Goodey.
		//
		return getString(parsedString, parmIndex[blockNumber][itemNumber] + 1,
		    parmIndex[blockNumber][itemNumber + 1]);
	}

	/**
	 * Return the number of items for the specified block.
	 * 
	 * @param blockNumber
	 *          the requested block number
	 * @return the number of comma delimited items in the specified block. The
	 *         valid range of items is 0 to (getItemCount() - 1).
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 * @exception IndexOutOfBoundsException
	 *              if the user specifies a negative block number. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 */
	public int getItemCount(int blockNumber) {
		// make sure this is a quoted string
		//
		validateQuoted();

		// check that the block number is sane
		//
		validateBlock(blockNumber);

		// check that they have requested a valid block number. If the
		// user has requested block number greater than what we have found,
		// it is not an error, however by definition the number of
		// items in that block is 0
		//
		if (blockNumber >= numberOfBlocks) {
			return 0;
		}

		// other wise return the correct count
		//
		return parmIndex[blockNumber].length - 1;
	}

	/**
	 * Return an array of Strings which represent the comma delimitted data items
	 * for the specified block number.
	 * 
	 * @param blockNumber
	 *          the requested block number
	 * @return emptArray if the request is invalid, or an array of Strings
	 *         representing the items. Note that an item will contain emptyString
	 *         if it is empty.
	 * @exception IndexOutOfBoundsException
	 *              if the user specifies a negative block number. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 */
	public String[] getItems(int blockNumber) {
		// make sure this is a quoted string
		//
		validateQuoted();

		// validate block number
		//
		validateBlock(blockNumber);

		// check that they have requested a valid block number
		//
		if (blockNumber >= numberOfBlocks) {
			return emptyArray;
		}

		// get the count of the number of items in the block
		//
		int itemCount = getItemCount(blockNumber);

		// check the item count
		//
		if (itemCount <= 0) {
			return emptyArray;
		}

		// allocate an array for the items
		//
		String[] result = new String[itemCount];

		// Loop, setting up each item
		//
		for (int count = 0; count < itemCount; count++) {
			result[count] = getItem(blockNumber, count);
		}

		return result;
	}

	/**
	 * Get the entire line of data with leading and trailing markers removed.
	 * Quotes are removed from quoted strings, and comment delimiters are removed
	 * from comment lines. Inner strings have been cleaned up. It is not safe to
	 * try to parse this String since the Inner String delimiters have been
	 * removed. Use this method only if you do not need to parse this line of
	 * data.
	 * 
	 * @return the String representation for this line or emptyString if there was
	 *         a problem
	 */
	public String getLineData() {
		return getString(parsedString, 0, parsedString.length());
	}

	/**
	 * Check if this is a comment line.
	 * 
	 * @return true if this is a comment line, false if this is not a comment
	 *         line.
	 */
	public boolean isCommentLine() {

		return isComment;
	}

	/**
	 * Check if the specified block is a name defined block.
	 * 
	 * @return true if this is a name defined block, false if this is not a name
	 *         defined block
	 */
	public boolean isNameDefinedBlock(int blockNumber) {

		// check that they have requested a valid block number
		//
		if (blockNumber >= numberOfBlocks || blockNumber < 0) {
			return false;
		}

		// name defined blocks must be within quoted
		// strings
		//
		if (!isQuoted) {
			return false;
		}

		// return the name block flag
		//
		return isNameBlock[blockNumber];
	}

	/**
	 * Determine if this is a position defined block. This is a position defined
	 * block if the block numbers are valid, and if this is a quoted string, and
	 * if this is not a name defined block.
	 * 
	 * @param blockNumber
	 *          the block number
	 * @return true if this is a position defined block.
	 */
	public boolean isPositionDefinedBlock(int blockNumber) {
		// check that they have requested a valid block number
		//
		if (blockNumber >= numberOfBlocks || blockNumber < 0) {
			return false;
		}

		// Position defined values must be within quoted
		// strings
		//
		if (!isQuoted) {
			return false;
		}

		// if this is not a name defined block, it
		// must be a position defined block
		//
		return !isNameBlock[blockNumber];
	}

	/**
	 * Check if this is a quoted line.
	 * 
	 * @return true if this is a quoted line, false if this is not a quoted line.
	 */
	public boolean isQuotedLine() {
		return isQuoted;
	}

	/**
	 * Check if this is an unquoted line.
	 * 
	 * @return true if this is an unquoted line, false if this is not an unquoted
	 *         line.
	 */
	public boolean isUnQuotedLine() {

		return !(isQuoted || isComment);
	}

	
	/**
	 * Utility method to get a String represention of the data
	 * 
	 * @return the string version of the parsed data.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(80);

		buffer.append("Comment=");
		buffer.append(isCommentLine());
		buffer.append(" Quoted=");
		buffer.append(isQuotedLine());
		buffer.append(" Unquoted=");
		buffer.append(isUnQuotedLine());
		buffer.append("\n");
		buffer.append("linevalue=");
		buffer.append(getLineData());
		buffer.append("\n");

		// make sure this is a quoted string before continuing
		//
		if (!isQuoted) {
			return buffer.toString();
		}

		// loop over the block count
		//
		int blockCount = getBlockCount();
		for (int block = 0; block < blockCount; block++) {
			buffer.append(" block=");
			buffer.append(block);
			buffer.append(" value=");
			buffer.append(checkString(getBlock(block)));
			buffer.append("\n");

			int itemCount = getItemCount(block);
			buffer.append(" items: ");
			for (int item = 0; item < itemCount; item++) {
				buffer.append("  ");
				buffer.append(item);
				buffer.append("=");
				buffer.append(checkString(getItem(block, item)));
			}
			buffer.append("\n");

			// try to get compound items if this is a position defined
			// block
			//
			if (isPositionDefinedBlock(block)) {
				for (int item = 0; item < itemCount; item++) {
					String[] comp = getCompoundItems(block, item);

					// if the item requested is empty, then comp is emptyArray
					//
					if (comp != emptyArray) {
						if (comp.length > 1) {
							// print out the compount items
							//
							buffer.append("   Compound:");
							for (int index = 0; index < comp.length; index++) {
								buffer.append(" ");
								buffer.append(index);
								buffer.append("=");
								buffer.append(checkString(comp[index]));
							}
							buffer.append("\n");
						}
					}
				}
			}
		}

		return buffer.toString();

	}

	/**
	 * Get the next name defined item in this line of the TL1 data. Note that it
	 * is not valid to call this method, without first calling hasMoreNameItems().
	 * <p>
	 * Note that this method is not block specific. This means that it will
	 * happily step across blocks. For INSTANCE, it we had a line of data like:
	 * 
	 * <pre>
	 * &quot;OC3-1:dave1=1,dave2=2:one,two,three:dave4=4&quot;
	 * </pre>
	 * 
	 * then subsequent requests via calls to getNextNameItem() will return the
	 * information for dave1, dave2 and dave4.
	 * 
	 * @return a TL1Token which contains the key and the value. In the example
	 *         above the first call to getNextName item would return a token like:<br>
	 *         myToken.key=dave1 <br>
	 *         myToken.value=1 <br>
	 *         This method and it's peer hasMoreNameItems used to be public, but
	 *         it resulted in some awfully performing code. Use getAllNameItems
	 *         instead.
	 */
	TL1Token getNextNameItem() {
		return new TL1Token(nameInfo[0], nameInfo[1]);
	}

	/**
	 * Check if there are any more name defined items available. Note that this
	 * method is not block specific. This means that it will happily step across
	 * blocks. For INSTANCE, it we had a line of data like:
	 * 
	 * <pre>
	 * &quot;OC3-1:dave1=1,dave2=2:one,two,three:dave4=4&quot;
	 * </pre>
	 * 
	 * then subsequent requests via calls to getNextNameItem() will return the
	 * information for dave1, dave2 and dave4.
	 * 
	 * @return true if there are more name defined items, false if there are none.
	 *         This method must be called before calling getNextNameItem().
	 *         <p>
	 *         This method and it's peer getNextNameItem used to be public, but it
	 *         resulted in some awfully performing code. Use getAllNameItems
	 *         instead.
	 */
	boolean hasMoreNameItems() {
		boolean found = false;

		while (!found && blockIndex < numberOfBlocks) {
			// if this is not a name defined block, or the
			// item index is out of range then
			// increment the block index
			//
			if (!isNameBlock[blockIndex] || itemIndex >= getItemCount(blockIndex)) {
				blockIndex++;
				itemIndex = 0;
			}
			else {
				// this means that we are within a name defined
				// block, and that the item index is within range
				//
				found = true;

				// try to get the next name defined info
				//
				nameInfo = getCompoundItemsByChar(blockIndex, itemIndex, '=');

				// if (nameInfo[1] == null )
				// nameInfo[1] = nullValue;

				// check that there was no error getting the name info
				//
				if (nameInfo == null || nameInfo.length != 2) {
					found = false;
				}
				// increment the item index
				//
				itemIndex++;
			}
		}
		return found;
	}

	/**
	 * return true if the line was parsed properly.
	 * 
	 * @return true if parsing was OK
	 */
	boolean isParsedOK() {
		return parseOK;
	}

	/**
	 * Utility function to parse an array of chars looking for the specified
	 * delimiter. Only look for the delimiter between the start and stop indices.
	 * 
	 * @param data
	 *          the array of char data in which to search
	 * @param start
	 *          the start index
	 * @param stop
	 *          the stop index
	 * @param delimiter
	 *          the character to use as a delimiter
	 * @return an array of strings or emptyArray if there was a problem.
	 */
	private String[] getCompoundFromData(String data, int start, int stop,
	    char delimiter) {

		// check the boundaries
		//
		if (start >= stop) {
			return emptyArray;
		}
		if (start == stop - 1) {
			return emptyArray;
		}

		// nothing found yet
		//
		int compoundCount = 0;
		char[] dataTemp = data.toCharArray();

		// step through searching for the delimiter
		// first we will count the delimiters
		//
		for (int index = start + 1; index < stop; index++) {
			if (dataTemp[index] == delimiter) {
				compoundCount++;
			}
		}

		// = is a special delimiter. It would never make any sense
		// for someone to send down a string like: 99=10=01,
		// however, it makes perfect sense for someone to send
		// down something like name=\"some junk =sjdflsdfj\"
		// Since the quoted string has already been cleaned
		// up, if the user passes in a = in the delimiter, we only
		// find the first one
		//
		if (compoundCount != 0 && delimiter == '=') {
			compoundCount = 1;
		}

		// We now know how many compound pieces we have
		//
		if (compoundCount == 0) {
			String[] result = new String[1];
			result[0] = getString(data, start + 1, stop);
			return result;
		}

		// Allocate the array for the compound pieces
		//
		String[] result = new String[compoundCount + 1];

		// loop again and create the Strings
		//
		int lastDelimiter = start;
		int resultCount = 0;
		int index = start + 1;

		// loop for as long as it takes
		//
		while (resultCount < compoundCount && index < stop) {
			// if this char is a delimiter
			//
			if (dataTemp[index] == delimiter) {
				// then get the compound piece
				//
				result[resultCount] = getString(data, lastDelimiter + 1, index);
				// increment the counters
				//
				lastDelimiter = index;
				resultCount++;
			}
			index++;
		}
		// we must still do the last item
		//
		result[resultCount] = getString(data, lastDelimiter + 1, stop);
		return result;
	}

	/**
	 * Return an array of Strings containing the compound items which are
	 * delimited by the specified delimiter.
	 * 
	 * @param blockNumber
	 *          the requested block number
	 * @param itemNumber
	 *          the requested item number
	 * @param delimiter
	 *          the character delimiter
	 * @return emptyArray if the user has requested an out of bounds item, or an
	 *         array of Strings containing the compound items.
	 */
	private String[] getCompoundItemsByChar(int blockNumber, int itemNumber,
	    char delimiter) {

		// check that they have requested a valid block number
		//
		if (blockNumber >= numberOfBlocks) {
			return emptyArray;
		}

		// check that they have requested a valid item number
		//
		if (itemNumber >= parmIndex[blockNumber].length - 1) {
			return emptyArray;
		}

		// setup the start and stop for the specified item
		//
		int start = parmIndex[blockNumber][itemNumber];
		int stop = parmIndex[blockNumber][itemNumber + 1];

		return getCompoundFromData(parsedString, start, stop, delimiter);
	}

	/**
	 * Create a string based on the start and stop indices from the specified
	 * data. If there is any problem, then emptyString is returned. Strings are
	 * only created when the user requests them.
	 * 
	 * @param data
	 *          the raw data
	 * @param start
	 *          the starting index
	 * @param stop
	 *          the stopping index
	 * @return the String requested or emptyString
	 */
	private String getString(String data, int start, int stop) {
		// check if the user is asking for something
		// reasonable
		//
		if (start >= stop || data == null || stop > data.length() || start < 0
		    || stop < 0) {
			// return emptyString; supporting null value
			return null;
		}

		// create a new string and return it
		//

		// // Specifially deal with inner string
		if (data.startsWith("\"", start)) {
			start += 1;
			stop -= 1;
		}

		if (start >= stop) {
			return emptyString;
		}

		return data.substring(start, stop);
	}

	/**
	 * This is the main parsing method. We detect comments, quoted string and
	 * unquoted strings here.
	 * 
	 * @return true if the parsing was successful, false if there was a problem.
	 */
	private boolean parse() {
		// sanity check
		//
		if (rawData == null) {
			return false;
		}
		if (rawStart == rawStop) {
			return false;
		}

		// If this is a really short line of text, it cannot
		// contain a comment delimiter
		//
		if (rawStop > 2) {
			// first we check for the start comment line
			//
			if (rawData[0] == '/' && rawData[1] == '*') {
				isComment = true;
				rawStart = 2;
			}
		}

		if (rawStop > 2) {
			// check for the end comment
			//
			if (rawData[rawStop - 2] == '*' && rawData[rawStop - 1] == '/') {
				// set the end point
				//
				rawStop = rawStop - 2;
				isComment = true;
			}
		}

		// if this is a comment line, then there is no further parsing
		// to do
		//
		if (isComment) {
			return true;
		}

		// check for a quoted string
		//
		if (rawData[rawStart] == '"') {
			isQuoted = true;
			rawStart++;

			// check for trailing quote
			//
			if (rawData[rawStop - 1] == '"') {
				rawStop--;
			}

			// Check if the user passed in ""
			// I guess this is OK
			//
			if (rawStart >= rawStop) {
				return true;
			}

			// do the quoted line parsing
			//
			return parseQuotedLine();
		}

		// If this is not a comment line and this is not
		// a quoted line, then this is probably an unquoted line
		// or the middle of a multiline comment
		//
		return true;
	}

	/**
	 * This is the main parsing method for quoted lines of data. We only parse
	 * quoted lines since comment lines and unquoted lines don't have any defined
	 * structure.
	 * <p>
	 * The parsing is done in two steps. First we step through the raw data to
	 * count the number of block and parameter delimiters. Then we allocate the
	 * arrays that will hold the block and parameter indices. Finally we step
	 * through the raw data again. In this round of stepping, we fill in the
	 * arrays, and cleanup any quoted strings.
	 * <p>
	 * In the second part of the parsing, we "copy" the original message to the
	 * destination. This allows us to clean up any inner strings we may encounter
	 * along the way. In actual fact, we are copying back into the original data
	 * array, but this is safe, since the parsed messages is either the same size
	 * as the original message, or smaller than the original message.
	 * 
	 * @return true if everything was OK, false if there was a problem
	 */
	private boolean parseQuotedLine() {

		// index in the raw message for the source character
		//
		int sourceIndex = rawStart;

		// number of blocks found
		//
		int blockCount = 0;

		// count of the parameter delimiters found per block
		//
		int parmCountPerBlock[] = new int[MAX_BLOCKS_EXPECTED];

		// flag to indicate if we are currently within an inner
		// string
		//
		boolean insideInnerString = false;

		// initilaize the first parmcount entry
		// other entries will be initialized as we find blocks
		//
		parmCountPerBlock[0] = 0;

		// The first step is to parse through the string, counting
		// the number of blocks and parameter delimiters we have
		//
		while (sourceIndex < rawStop) {

			// this is a sort of a state machine. We step though
			// each character in the raw data and look for "special"
			// characters
			//
			switch (rawData[sourceIndex]) {

			// search for the start or end of an
			// inner string
			//
			case '\\':
				// make sure it is safe to look at the next
				// character
				//
				if (sourceIndex < rawStop - 1) {
					if (rawData[sourceIndex + 1] == '"') {
						insideInnerString = !insideInnerString;
						sourceIndex++;
					}
					else if (rawData[sourceIndex + 1] == '\\') {
						sourceIndex++;
					}
				}
				break;

			// search for a block delimiter
			//
			case ':':
				if (!insideInnerString) {
					blockCount++;
					if (blockCount >= MAX_BLOCKS_EXPECTED) {
						// log an error
						//
						log.error("TL1Line: too many blocks." + "\nLine: " + getLineData());
						return false;
					}
					// initialize the next item
					//
					parmCountPerBlock[blockCount] = 0;
				}
				break;

			// search for a parameter delimiter
			//
			case ',':
				if (!insideInnerString) {
					parmCountPerBlock[blockCount]++;
				}
				break;

			} // endswitch

			// step to the next character
			//
			sourceIndex++;

		} // while sourceIndex < rawStop

		// if we are somehow still inside an inner string
		// we choke
		//
		if (insideInnerString) {
			// log an error
			//
			log.error("TL1Line: unbalanced inner string." + "\nLine: "
			    + getLineData());
			// return false;
		}

		// Now we have the count of the number of blocks, and
		// we also have a count of the number of parameters per
		// block.
		// create the array holding the indices of the start and stop of
		// blocks. If we detected n blocks, we need n + 2 places to
		// include the start and stop indices.
		//
		blocks = new int[blockCount + 2];

		// The number of blocks is blockCount +1
		//
		numberOfBlocks = blockCount + 1;

		// initialize the first element of the block
		//
		blocks[0] = rawStart - 1;

		// reInitialize the blockCount to zero for the second
		// wave of parsing
		//
		int blockIndex = 0;

		// we create a two dimensional non-rectangular array
		// to hold the parameter block indices
		//
		parmIndex = new int[numberOfBlocks][];
		int parmIndexCount = 0;

		// allocate our non rectangular array
		// We use parmsize + 2, since we also want to store
		// the index for the start of the first parameter
		// and the index for the stop of the last parameter
		//
		for (int counter = 0; counter < parmIndex.length; counter++) {
			int parmSize = parmCountPerBlock[counter];
			parmIndex[counter] = new int[parmSize + 2];
		}

		// allocate store for the isNameBlock array
		//
		isNameBlock = new boolean[blockCount + 1];

		// we need an index for the destination "copy"
		//
		int destIndex = rawStart;
		sourceIndex = rawStart;

		// initialize the first parm entry
		// rawStart -1 is always safe, because this procedure
		// is only called for quoted lines.
		//
		parmIndex[blockIndex][parmIndexCount] = rawStart - 1;
		parmIndexCount++;

		// make sure the flag is false.
		//
		insideInnerString = false;
		boolean copyChar;

		while (sourceIndex < rawStop) {
			// search for the start and end of an
			// inner string
			//
			copyChar = true;

			// Step through the message, looking for "special"
			// characters
			//
			switch (rawData[sourceIndex]) {

			// search for a quoted string
			//
			case '"':
				if (sourceIndex < rawStop - 1) {
					switch (rawData[sourceIndex + 1]) {
					case '"':
						// if we are inside an inner string clean up ""
						if (insideInnerString) {
							sourceIndex++;
						}
						break;
					// note that strictly speaking, if we find a "
					// that's not in an inner string this is not gr831
					// compliant, however we will let it through
					}
				}
				break;

			// search for escaped backslashes inside inner strings
			//
			case '\\':
				if (sourceIndex < rawStop - 1) {
					switch (rawData[sourceIndex + 1]) {
					case '\\':
						// if(insideInnerString)
						sourceIndex++;
						break;
					case '"':
						// update our inner string vars
						sourceIndex++;
						insideInnerString = !insideInnerString;
						copyChar = true;
						break;
					}
				}
				break;

			// search for parameter delimiters
			//
			case ',':
				if (!insideInnerString) {
					parmIndex[blockIndex][parmIndexCount] = destIndex;
					parmIndexCount++;
				}
				break;

			// search for block delimiters
			//
			case ':':
				if (!insideInnerString) {
					blocks[blockIndex + 1] = destIndex;
					parmIndex[blockIndex][parmIndex[blockIndex].length - 1] = destIndex;
					blockIndex++;
					parmIndexCount = 0;
					parmIndex[blockIndex][parmIndexCount] = destIndex;
					isNameBlock[blockIndex] = false;
					parmIndexCount++;
				}
				break;

			// search for name defined blocks
			// gr831 defines a name defined block as one which contains
			// an =
			//
			case '=':
				if (!insideInnerString) {
					isNameBlock[blockIndex] = true;
				}

			} // endswitch

			// only copy the char if it is a normal character
			//
			if (copyChar) {
				rawData[destIndex] = rawData[sourceIndex];
				destIndex++;
			}

			// next character please
			//
			sourceIndex++;
		}

		// need to change the stop point for the raw data
		// rawStop has changed if we have cleaned up any inner
		// strings
		//
		rawStop = destIndex;

		// initialize the last index of the blocks array
		//
		blocks[blocks.length - 1] = rawStop;

		// initialize the last index of the parm array for the last block
		//
		parmIndex[parmIndex.length - 1][parmIndex[parmIndex.length - 1].length - 1] = rawStop;
		return true;
	}

	/**
	 * Validate the passed in block number. Throws an exception if the block
	 * number is negative.
	 * 
	 * @param blockNumber
	 *          the requested block number.
	 * @exception IndexOutOfBoundsException
	 *              if the user specifies a negative block number. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 */
	private void validateBlock(int blockNumber) {
		// if the block number is negative, throw an exception
		//
		if (blockNumber < 0) {
			throw new IndexOutOfBoundsException(blockIndexString
			    + Integer.toString(blockNumber));
		}
	}

	/**
	 * Validate the passed in iten number. Throws an exception if the item number
	 * is negative.
	 * 
	 * @param itemNumber
	 *          the requested block number.
	 * @exception IndexOutOfBoundsException
	 *              if the user specifies a negative block number. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 */
	private void validateItem(int itemNumber) {
		// if the block number is negative, throw an exception
		//
		if (itemNumber < 0) {
			throw new IndexOutOfBoundsException(itemIndexString
			    + Integer.toString(itemNumber));
		}
	}

	/**
	 * Validate if this is a quoted string. Throws an exception if it isn't.
	 * 
	 * @exception IllegalDataAccessException
	 *              if the data is not a TL1 quoted string. Since this is a
	 *              runtime exception it does not need to be explicitly caught by
	 *              the caller.
	 */
	private void validateQuoted() {
		// make sure this is a quoted string
		//
		if (!isQuoted) {
			throw new IllegalDataAccessException(quotedDescription);
		}
	}
} // endclass
