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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.StringTokenizer;

import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.CommandTemplate;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.TL1CommandClassMetadata;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1wrapper.Parameter;

public final class TL1Composer {
	// private static final String COMMAND_SEPERATOR = "-";
	// private static final String blockSep = ":";
	// private static final String paramSep = ",";
	// private static final String msgTerminator = ";";
	// private static final String valueSeperator = "=";
	// private static final String backSlash = "\\\"";
	private final NetworkElementInfo neInfo;

	public TL1Composer(NetworkElementInfo networkElementInfo) throws Exception {
		neInfo = networkElementInfo;
	}

	public static Class<?> getClass(NetworkElementInfo ne, String verb,
	    String mod1, String mod2) throws Exception {
		try {
			String className;
			if (mod2 != null) {
				className = ne.getAdapterName() + verb
				    + TL1CommandClassMetadata.classNameSep + mod1
				    + TL1CommandClassMetadata.classNameSep + mod2;
			}
			else {
				className = ne.getAdapterName() + verb
				    + TL1CommandClassMetadata.classNameSep + mod1;
			}

			ClassLoader cl = TL1Composer.class.getClassLoader();
			if (cl == null) {
				cl = ClassLoader.getSystemClassLoader();
			}

			return cl.loadClass(className);
		}
		catch (Exception e) {
			throw new Exception("Failed to load TL1 wrapper class for "
			    + ne.toString() + " " + verb + " " + mod1 + " " + mod2, e);
		}
	}

	/**
	 * Return the specific field.
	 **/

	static Field getField(Class<?> cl, String fieldName) throws Exception {
		Field fld;
		fld = cl.getDeclaredField(fieldName);
		fld.setAccessible(true);
		return fld;
	}

	/**
	 * Form TL1 commands
	 **/

	public void composeTL1Message(CommandTemplate commandTemplate,
	    String commandCode, Map<String, String> map) throws Exception {
		// String className = null;
		StringTokenizer blocks = new StringTokenizer(commandCode,
		    TL1CommandClassMetadata.cmdSep, false);

		// Extract Verb Mod1 and Mod2 from Command Code.
		String mod2 = null;
		String verb = blocks.nextToken();
		String mod1 = blocks.nextToken();
		commandTemplate.setCommand(commandCode);
		if (blocks.hasMoreTokens()) {
			mod2 = blocks.nextToken();
		}

		Class<?> tl1Class = getClass(neInfo, verb, mod1, mod2);
		if (mod2 == null) {

			/**
			 * If Mod2 can be formulated from the TL1 request Message HashMap update
			 * the commandCode to Accommodate the MOD2
			 **/
			commandTemplate.setCommand(commandCode + formMOD2(map, tl1Class));
		}

		// Setting this flag means that the TL1 engine will not quote or validate
		// any strings.
		// Instead the validation is done below using the validateString method.
		commandTemplate.setQuoteItemIfNecessary(false);

		/**
		 * Extract AID Values from HashMap Set The AID Values in the TL1Request
		 * Message
		 */

		setAIDComponents(commandTemplate, map, tl1Class);
		setTID(commandTemplate, map);

		// Compose the General Block and set the same in TL1Request Message
		formParamBlockSeries(commandTemplate, map, tl1Class);
		// Pu. Sept26/2002
		// add printable status into MediationLogSystem so that if this tl1 is not
		// printable,it will not
		// appear in the log.
		// setPrintableStatus(cl, commandCode);

		// LogSystem.log(Level.FINE, "TL1 composed by Mediation: " +
		// commandTemplate.toString());
		// commandTemplate.toString());
	}

	/**
	 * Form General Block. For Now, all the general blocks are empty. But we still
	 * keep this method here in case we need it in future.
	 **/

	// public void formGeneralBlock(CommandTemplate commnandTemplate, Map<String,
	// String> map, Class<?> cl)
	// throws Exception
	// {
	// if (!isGBlockPresent(cl))
	// {
	// return;
	// }
	//
	// // Extracts the data definition of the GBLOCK
	//
	// boolean isPos = isGBlockPos(cl);
	// Field fld = getField(cl, TL1CommandClassMetadata.G_BLOCK);
	// // Compose GBLOCK
	//
	// Parameter[] gblock = (Parameter[]) fld.get(null);
	// for (int offset = 0; offset < gblock.length; offset++)
	// {
	// String value = map.get(gblock[offset].getNormalizationLabel());
	// if (value != null)
	// {
	// if (!isPos)
	// {
	// commnandTemplate
	// .setParameter(TL1CommandClassMetadata.GBLOCK_NUMBER,
	// gblock[offset].getProtocolLabel(), value);
	// }
	// }
	// commnandTemplate.setParameter(TL1CommandClassMetadata.GBLOCK_NUMBER,
	// offset, value);
	// }
	// }
	/**
	 * If the MOD2 is defined as an enumerated type,then its values is present in
	 * the same HashMap as parameter blocks Returns the MOD2 value after
	 * extracting from the HashMap
	 **/

	private String formMOD2(Map<String, String> map, Class<?> cl)
	    throws Exception {
		String returnValue = "";
		Field fld;
		fld = getField(cl, TL1CommandClassMetadata.isMOD2Enumerated);
		Boolean isMOD2Enumerated = (Boolean) fld.get(null);
		if (isMOD2Enumerated.booleanValue()) {
			fld = getField(cl, TL1CommandClassMetadata.MOD2Values);
			// String[] mod2Values = (String[]) fld.get(null);
			fld = getField(cl, TL1CommandClassMetadata.MOD2Name);
			String mod2Name = (String) fld.get(null);

			// The issue is not all NEs support the default MOD2 so check first
			try {
				fld = getField(cl, TL1CommandClassMetadata.MOD2Def);
			}
			catch (Exception e) {
				return returnValue;
			}
			String mod2Def = (String) fld.get(null);
			returnValue = TL1CommandClassMetadata.cmdSep;
			if (map.containsKey(mod2Name)) {
				returnValue += map.get(mod2Name);
			}
			else {
				returnValue += mod2Def;
			}

		}
		return returnValue;
	}

	private void formParamBlockSeries(CommandTemplate commandTemplate,
	    Map<String, String> map, Class<?> cl) throws Exception {
		boolean[] fBlocksIsPos = getFBlocksIsPos(cl);

		Field fld = getField(cl, TL1CommandClassMetadata.F_BLOCKS);

		// For all the parameter blocks

		Parameter[][] fblocks = (Parameter[][]) fld.get(null);

		for (int blkNum = 0; blkNum < fblocks.length; blkNum++) {
			// For all the parameters in a block
			for (int offset = 0; offset < fblocks[blkNum].length; offset++) {

				// Extract value from HashMap
				String value = map.get(fblocks[blkNum][offset].getNormalizationLabel());
				// String key = null;
				// if (value != null)
				// {
				// key = fblocks[blkNum][offset].getNormalizationLabel();
				// }
				// 
				if (!fBlocksIsPos[blkNum]) {
					commandTemplate.setParameter(TL1CommandClassMetadata.FBLOCK_START
					    + blkNum, fblocks[blkNum][offset].getProtocolLabel(), value);
				}
				else {
					commandTemplate.setParameter(TL1CommandClassMetadata.FBLOCK_START
					    + blkNum, offset, value);
				}
			}
		}

	}

	/*
	 * Parameter[][] getFBlocks(Class cl) throws Exception { Field fld =
	 * getField(cl, TL1CommandClassMetadata.F_BLOCKS); return (Parameter[][])
	 * fld.get(null); }
	 */
	/**
	 * Compose the parameter Blocks
	 **/

	private boolean[] getFBlocksIsPos(Class<?> cl) throws Exception {
		return (boolean[]) getField(cl, TL1CommandClassMetadata.F_Block_Is_Pos)
		    .get(null);
	}

	// /*
	// * private Parameter[] getGBlock(Class cl) throws Exception { Field fld =
	// getField(cl,
	// * TL1CommandClassMetadata.G_BLOCK); return (Parameter[]) fld.get(null); }
	// */
	// private boolean isGBlockPos(Class<?> cl)
	// throws Exception
	// {
	// Field fld = getField(cl, TL1CommandClassMetadata.G_Block_Is_Pos);
	// Boolean gBlockIsPos = (Boolean) fld.get(null);
	// return gBlockIsPos.booleanValue();
	// }
	//
	// private boolean isGBlockPresent(Class<?> cl)
	// throws Exception
	// {
	// Field fld = getField(cl, TL1CommandClassMetadata.isGBlockPresent);
	// Boolean isPresent = (Boolean) fld.get(null);
	// return isPresent.booleanValue();
	// }

	private void setAIDComponents(CommandTemplate commandTemplate,
	    Map<String, String> map, Class<?> cl) throws Exception {
		Field fld = getField(cl, "AID");
		String[] aidArray = (String[]) fld.get(null);
		if (aidArray.length == 2) {
			commandTemplate.setFromAid(map.get(aidArray[0]));
			commandTemplate.setToAid(map.get(aidArray[1]));
		}

		if (aidArray.length == 1) {
			commandTemplate.setAid(map.get(aidArray[0]));
		}
	}

	/**
     */
	// private void setPrintableStatus(Class cl, String commandCode)
	// {
	//
	// try
	// {
	// Field fld = getField(cl, TL1CommandClassMetadata.isUnprintable);
	//
	// Boolean isUnprintable = (Boolean) fld.get(null);
	// /*
	// * if (isUnprintable.booleanValue()) MediationLogSystem.addUnprintableRec(
	// neInfo.getNeID(),
	// * commandCode);
	// */
	// }
	// catch (Exception e)
	// {
	// 
	// }
	//
	// }
	/**
	 * TID values are present in the same HashMap as parameter blocks These are
	 * extracted from the HashMap and TL1RequestMessage is updated with these
	 * values.
	 **/

	private void setTID(CommandTemplate commandTemplate, Map<String, String> map)
	    throws Exception {
		if (map.containsKey("TID")) {
			commandTemplate.setTid(map.get("TID"));
		}
	}
}
