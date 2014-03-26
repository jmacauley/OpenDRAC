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

package com.nortel.appcore.app.drac.server.neproxy.mediation.controller;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Line;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Parser;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.engine.SyntaxException;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.engine.TL1Reader;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1wrapper.Parameter;

/**
 * Parse TL1Line from engine to Name-value pair. Creation date: (1/14/02
 * 10:45:30 AM)
 * 
 * @author:
 */

public final class TL1LineParser {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static final String COMMAND_SEPERATOR = "-";
	static {
		TL1Line.setNullValue("-");
	}

	// private final char[] emptySpaceChars = new char[]
	// { '\r', '\n', '\t', ' ' };
	// private final char[] spaceChars = new char[]
	// { ' ', '\t' };
	private final NetworkElementInfo neInfo;

	// private String neID = null;

	/**
     *
     */
	public TL1LineParser(NetworkElementInfo neInfo) {
		this.neInfo = neInfo;
	}

	/**
	 * Return the specific field.
	 */
	private static Field getField(Class<?> cl, String fieldName) throws Exception {
		Field fld;
		fld = cl.getDeclaredField(fieldName);
		fld.setAccessible(true);
		return fld;
	}

	/**
	 * Given the semi-parsed TL1 Acknowledgement Object ,this method parses the
	 * parameter blocks
	 * 
	 * @param tl1AckMsg
	 *          A semi-parsed TL1 Acknowledgement object. It contains the parsed
	 *          TL1 header data and the unparsed TL1 parameter blocks The parsed
	 *          TL1 header data ia available via methods getTextBlocks in
	 *          TL1AlarmEvent returns the unparsed TL1 parameter data setPayloads
	 *          in TL1AlarmEvent is used to return the parsed TL1 parameter data
	 * @exception Exception
	 */
	public void parse(TL1AlarmEvent tl1AckMsg) throws Exception {
		String command = tl1AckMsg.getCommandCode().toString();
		StringTokenizer blocks = new StringTokenizer(command,
		    TL1CommandClassMetadata.cmdSep, false);
		String mod2 = null;
		String verb = blocks.nextToken();
		String mod1 = blocks.nextToken();

		if (blocks.hasMoreTokens()) {
			mod2 = blocks.nextToken();
		}

		parse(tl1AckMsg, verb, mod1, mod2, tl1AckMsg.getTextBlocks());
	}

	public void parse(TL1ResponseMessage tl1RespMsg) throws Exception {
		String command = tl1RespMsg.getCommand().toString();
		StringTokenizer blocks = new StringTokenizer(command,
		    TL1CommandClassMetadata.cmdSep, false);
		String mod2 = null;
		String verb = blocks.nextToken();
		String mod1 = blocks.nextToken();
		if (blocks.hasMoreTokens()) {
			mod2 = blocks.nextToken();
		}

		List<String> lineList = new ArrayList<String>();
		String[] lines = tl1RespMsg.getTextBlocks();

		for (String line : lines) {
			lineList.add(line);
		}

		com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Parser parser = new com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Parser();
		parser.addListData(lineList);
		parse(parser, tl1RespMsg, verb, mod1, mod2, tl1RespMsg.getTextBlocks());
	}

	/**
     */
	private String getMOD2Name(Class<?> cl) throws Exception {
		Field fld;
		// @TODO WP: ARE we calling this for the side effect or are we really not
		// using it?
		fld = TL1Composer.getField(cl, TL1CommandClassMetadata.isMOD2Enumerated);
		fld = TL1Composer.getField(cl, TL1CommandClassMetadata.MOD2Name);
		return (String) fld.get(null);
	}

	/**
	 * @return boolean
	 */
	private boolean isBlockSep(TL1Reader tl1reader, String blkSep)
	    throws Exception {
		if (tl1reader.peek() == '\r') {
			tl1reader.read("\r");

			if (tl1reader.peek() == '\n') {
				tl1reader.read("\n");
			}
			return true;
		}

		return false;
	}

	/**
	 * Checks if the TL1 Data definition specifies the type as having Embedded
	 * Response Messages( RTRV-AO).
	 */
	private boolean isEmbedded(Class<?> cl) throws Exception {
		Boolean b = Boolean.FALSE;
		Field fld;
		try {
			fld = cl.getDeclaredField(TL1CommandClassMetadata.EmbeddedType);
			fld.setAccessible(true);
			// what?
			String type = (String) fld.get(null);
			return !type.equals("NONE");
		}
		catch (Exception t) {
			try {
				fld = cl.getDeclaredField(TL1CommandClassMetadata.isEmbedded);
				fld.setAccessible(true);
				b = (Boolean) fld.get(null);
			}
			catch (Exception tt) {
				log.error("Error: ", tt);
			}
		}
		return b.booleanValue();
	}

	/**
	 * Check if MOD2 is Enum type.
	 */
	private boolean isMOD2Enumerated(Class<?> cl) throws Exception {
		Field fld;
		fld = TL1Composer.getField(cl, TL1CommandClassMetadata.isMOD2Enumerated);
		Boolean isMOD2Enumerated = (Boolean) fld.get(null);
		return isMOD2Enumerated.booleanValue();
	}

	/**
	 * Loads the TL1 definition class from the jar file. If the TL1 Message type
	 * is embeddded ,then prasing of the embedded messages is done by
	 * parseEmbedded method and TL1 response Object is updated by setting the
	 * isEmbedded field to true by calling setEmbedded method of TL1 Response
	 * Object If the message type is not embedded then the parameter blocks are
	 * parsed.
	 */
	private void parse(
	    com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Parser parser,
	    TL1ResponseMessage tl1RespMsg, String verb, String mod1, String mod2,
	    String[] lines) throws Exception {

		Class<?> cl = TL1Composer.getClass(neInfo, verb, mod1, mod2);

		if (isEmbedded(cl)) {
			List<TL1AlarmEvent> p = parseEmbedded(lines, cl);
			tl1RespMsg.setEmbedded(true);
			tl1RespMsg.setAllMessages(p);
		}
		else {
			List<Map<String, String>> parsedLines = parseResponseBlock(cl, parser,
			    new ArrayList<String>());

			if (isMOD2Enumerated(cl)) {
				updateRespParamBlocks(parsedLines, getMOD2Name(cl), mod2);
				tl1RespMsg.setCommand(Tl1CommandCode.fromString(verb
				    + COMMAND_SEPERATOR + mod1));
			}

			tl1RespMsg.setPayload(parsedLines);

		}

	}

	/**
	 * Loads the TL1 definition class from the jar file. If the TL1 Message type
	 * is embedded ,then parsing of the embedded messages is done by parseEmbedded
	 * method and TL1 response Object is updated by setting the isEmbedded field
	 * to true by calling setEmbedded method of TL1 Response Object If the message
	 * type is not embedded then the parameter blocks are parsed.
	 */
	private void parse(TL1AlarmEvent tl1AckMsg, String verb, String mod1,
	    String mod2, String[] lines) throws Exception {

		Class<?> cl;

		try {
			cl = TL1Composer.getClass(neInfo, verb, mod1, mod2);
		}
		catch (Exception e) {
			log.warn("Unable to load class for " + verb + " " + mod1 + " " + mod2
			    + ", will now try default (" + verb + "," + mod1 + ",DEFAULT) "
			    + e.toString());
			try {
				cl = TL1Composer.getClass(neInfo, verb, mod1, "DEFAULT");
			}
			catch (Exception e1) {
				throw new Exception("Unable to load class for (" + verb + "," + mod1
				    + "," + mod2 + " or DEFAULT)", e1);
			}
		}

		TL1Parser parser = new TL1Parser();

		List<String> lineList = new ArrayList<String>();

		for (String line : lines) {
			lineList.add(line);
		}

		parser.addListData(lineList);
		if (isEmbedded(cl)) {
			// @TODO Why are we calling this? For any side effects? we are not storing
			// the reuslts.
			// parsedLines = parseEmbedded(lines, cl);
			parseEmbedded(lines, cl);
		}
		else {
			List<Map<String, String>> parsedLines = parseResponseBlock(cl, parser,
			    tl1AckMsg.getComment());

			if (isMOD2Enumerated(cl)) {
				updateRespParamBlocks(parsedLines, getMOD2Name(cl), mod2);
				tl1AckMsg.setCommandCode(Tl1CommandCode.fromString(verb
				    + COMMAND_SEPERATOR + mod1));
			}
			tl1AckMsg.setPayloads(parsedLines);
		}

		setPrintableStatus(cl, tl1AckMsg.getCommandCode().toString());
	}

	/**
	 * Parse Embedded response message. RTRV-AO.
	 */
	private List<TL1AlarmEvent> parseEmbedded(String[] lines, Class<?> cl)
	    throws Exception {
		List<TL1AlarmEvent> embeddedData = new ArrayList<TL1AlarmEvent>();

		for (String commentedline : lines) {
			// int i = 0;
			boolean commentEndReached = false;
			boolean gotItems = false;
			String q = null;
			Field fld = TL1Composer
			    .getField(cl, TL1CommandClassMetadata.commentStart);
			String commentStart = (String) fld.get(null);
			fld = TL1Composer.getField(cl, TL1CommandClassMetadata.commentEnd);
			String commentEnd = (String) fld.get(null);
			fld = TL1Composer.getField(cl, TL1CommandClassMetadata.blockSep);
			String blockSep = (String) fld.get(null);
			String line = removeCommentMark(commentedline, commentStart);

			BufferedReader reader = new BufferedReader(new StringReader(line));
			TL1Reader tl1reader = new TL1Reader(reader);
			tl1reader.skip(TL1Reader.lineEnds);
			tl1reader.skip(TL1Reader.spaces);

			while (tl1reader.peek() != commentEnd.charAt(0)) {
				gotItems = false;
				Map<String, String> map = new HashMap<String, String>();
				TL1AlarmEvent avent = new TL1AlarmEvent(neInfo);
				// Get the Auto Event header
				parseHeader(tl1reader, map, avent);
				tl1reader.skip(TL1Reader.lineEnds);
				List<String> unquotedString = new ArrayList<String>();
				boolean blockSept = isBlockSep(tl1reader, blockSep);

				while (!blockSept) {
					tl1reader.skip(TL1Reader.spaces);
					// Extract the parameter blocks for this Auto event.
					char peekCh = tl1reader.peek();

					if (peekCh != '"') {
						commentEndReached = true;
						break;
					}

					q = tl1reader.readUnquotedLine();
					unquotedString.add(q);
					gotItems = true;
					break;

				}

				if (gotItems) {
					String[] uArray = new String[unquotedString.size()];
					System.arraycopy(unquotedString.toArray(), 0, uArray, 0,
					    unquotedString.size());
					parse(avent, map.get(TL1CommandClassMetadata.verb),
					    map.get(TL1CommandClassMetadata.mod1),
					    map.get(TL1CommandClassMetadata.mod2), uArray);
					embeddedData.add(avent);
				}

				if (!commentEndReached) {
					try {
						tl1reader.skip(blockSep.toCharArray());
						if (tl1reader.peek() == commentEnd.charAt(0)) {
							break;
						}

						tl1reader.skip(TL1Reader.spaces);
					}
					catch (Exception e) {
						// Logger.writeException( "TL1 Parser parseEmbedded
						// encountered exception for RTRV-AO" , e );
						break;
					}
				}
				else {
					break;
				}
			}
		}
		return embeddedData;
	}

	/**
	 * Parses the header information and fills parsed header information into the
	 * map(HashMap)
	 */
	private void parseHeader(TL1Reader tl1reader, Map<String, String> map,
	    TL1AlarmEvent avent) throws java.lang.InterruptedException,
	    SyntaxException, java.io.IOException {
		String sid = tl1reader.readTargetIdentifier();

		map.put(TL1CommandClassMetadata.sid, sid);
		avent.setSid(sid);
		tl1reader.read(TL1CommandClassMetadata.space);
		String datestamp = tl1reader.readDate();
		map.put(TL1CommandClassMetadata.date, datestamp);
		avent.setDate(datestamp);
		tl1reader.read(TL1CommandClassMetadata.space);
		String timestamp = tl1reader.readTime();
		map.put(TL1CommandClassMetadata.time, timestamp);
		avent.setTime(timestamp);
		tl1reader.skip(TL1Reader.lineEnds);
		tl1reader.skip(TL1Reader.spaces);
		String alarmCode = tl1reader.readCode(TL1Constants.alarmCodes);
		map.put(TL1CommandClassMetadata.alarmcode, alarmCode);
		tl1reader.skip(TL1Reader.spaces);
		String atag = tl1reader.readDecimalTag();
		map.put(TL1CommandClassMetadata.atag, atag);
		avent.setCtag(Integer.parseInt(atag));
		tl1reader.skip(TL1Reader.spaces);
		String outputCode = tl1reader.readOutputCode();
		outputCode = outputCode.replace(' ', '-');
		map.put(TL1CommandClassMetadata.outputCode, outputCode);
		avent.setCommandCode(Tl1CommandCode.fromString(outputCode));
		StringTokenizer st = new StringTokenizer(outputCode,
		    TL1CommandClassMetadata.cmdSep, false);
		parseOutputCode(map, st);
	}

	/**
	 * Gets the parameter values TL1 Name-Value pairs on a given line
	 */
	private void parseNameValue(Map<String, String> map,
	    Map<String, String> labelMap, TL1Line line) {
		Map<String, String> rmap = line.getAllNameItems();
		Set<String> key = rmap.keySet();
		Iterator<String> itr = key.iterator();
		while (itr.hasNext()) {
			String keyv = itr.next();
			if (labelMap.containsKey(keyv)) {
				String medLabel = labelMap.get(keyv);
				map.put(medLabel, rmap.get(keyv));
			}
			else {
				map.put(keyv, rmap.get(keyv));
			}
		}
	}

	/**
	 * This method extracts the verb modifier1 and modifier2 if any.
	 */
	private void parseOutputCode(Map<String, String> map, StringTokenizer st) {
		try {
			map.put(TL1CommandClassMetadata.verb, st.nextToken());
			map.put(TL1CommandClassMetadata.mod1, st.nextToken());
			if (st.hasMoreTokens()) {
				map.put(TL1CommandClassMetadata.mod2, st.nextToken());
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
	}

	/**
	 * Get the Positional parameters from the parsed parameter data present in
	 * TL1Line object.
	 */
	private void parsePos(Map<String, String> map, TL1Line line,
	    Parameter[] rblocks, int blkNumber) {
		// For all the parameters in a block extract the parameter values
		for (int i = 0; i < rblocks.length; i++) {
			// Extract a parameter value by giving the block number and position
			// within the block
			String value = line.getItem(blkNumber, i);
			if (!rblocks[i].getNormalizationLabel().equals("NULL")) {
				if (value != null) {
					map.put(rblocks[i].getNormalizationLabel(), value);
				}
			}
		}
	}

	private List<Map<String, String>> parseResponseBlock(
	    Class<?> cl,
	    com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Parser parser,
	    List<String> comment) throws Exception {
		// This used to store a vector of Hash Map.
		List<Map<String, String>> parsedLines = new ArrayList<Map<String, String>>();
		// Obtain Response Block definition.
		Field fld = cl.getDeclaredField(TL1CommandClassMetadata.R_BLOCKS);
		fld.setAccessible(true);
		Field pos = cl.getDeclaredField(TL1CommandClassMetadata.R_Block_Is_Pos);
		pos.setAccessible(true);

		boolean[] rblocks_pos = (boolean[]) pos.get(null);
		Map<String, String> labelMap = new HashMap<String, String>();
		// parser.nextElement() returns a single line of parsed parameter blocks
		// So we iterate through all the lines in the response blocks
		while (parser.hasMoreElements()) {
			Map<String, String> map = new HashMap<String, String>();
			TL1Line line = parser.nextElement();
			if (line.isCommentLine()) {
				comment.add(line.getLineData());
				continue;
			}

			Parameter[][] rblocks = (Parameter[][]) fld.get(null);
			// For all the parameter blocks on a given line do parsing
			for (int i = 0; i < rblocks.length; i++) {
				// Pre intialize the parameters to null value
				// InitializeParameters(map, rblocks[i], rblocks_pos[i]);
				// Obtain mapping between mediation labels and NE labels.
				// initializeMap(labelMap, rblocks[i], rblocks_pos[i]);
				for (int j = 0; j < rblocks[i].length; j++) {
					if (!rblocks[i][j].getProtocolLabel().equals("NULL")) {
						labelMap.put(rblocks[i][j].getProtocolLabel(),
						    rblocks[i][j].getNormalizationLabel());
					}
				}
				if (rblocks_pos[i]) {
					parsePos(map, line, rblocks[i], i);
				}
			}

			parseNameValue(map, labelMap, line);
			// Add the parsed parameter name-value hashmap to the vector
			parsedLines.add(map);
		}
		return parsedLines;
	}

	/**
	 * Removes "\*" from the beginning of commented line
	 */
	private String removeCommentMark(String line, String commentStart) {
		line = skip(line, ' ');
		line = line.substring(commentStart.length(), line.length() - 2);
		return line;
	}

	/**
	 * Insert the method's description here. Creation date: (9/26/2002 4:28:47 PM)
	 * 
	 * @param cl
	 *          java.lang.Class
	 */
	private void setPrintableStatus(Class<?> cl, String commandCode) {
		try {
			Field fld = getField(cl, TL1CommandClassMetadata.isUnprintable);
			Boolean isUnprintable = (Boolean) fld.get(null);

			if (isUnprintable.booleanValue()) {
				// Logger.writeDebug(neID + " " + commandCode);
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
			// If there is no isUnprintable field in tl1 class, eat it quietly,
			// default it is printable.
		}

	}

	/**
	 * Remove specified character in ch from the begining of the String
	 */
	private String skip(String data, char ch) {
		int pos = 0;

		while (data.charAt(pos) == ch) {
			pos++;
		}

		return data.substring(pos);
	}

	/**
	 * If MOD2 exists, we should update the response command name.
	 */
	private void updateRespParamBlocks(List<Map<String, String>> parsedLines,
	    String MOD2Name, String mod2) {
		for (Map<String, String> map : parsedLines) {
			if (!map.containsKey(MOD2Name)) {
				map.put(MOD2Name, mod2);
			}
		}
	}
}