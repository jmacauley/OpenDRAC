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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client;

/**
 * 
 */
public final class TL1CommandClassMetadata {
	public static final String isMOD2Required = "isMOD2Required";
	public static final String isMOD2Enumerated = "isMOD2Enumerated";
	public static final String isUnprintable = "isUnprintable";
	public static final String MOD2Name = "MOD2Name";
	public static final String MOD2Values = "mod2Values";
	public static final String MOD2Def = "MOD2Def";
	public static final String isTIDRequired = "isTIDRequired";
	public static final String AID = "AID";
	public static final String CTAG = "CTAG";
	public static final String F_BLOCKS = "F_BLOCKS";
	public static final String G_BLOCK = "G_BLOCK";
	public static final String R_BLOCKS = "R_BLOCKS";
	public static final String F_Block_Is_Pos = "F_Block_Is_Pos";
	public static final String G_Block_Is_Pos = "G_Block_Is_Pos";
	public static final String R_Block_Is_Pos = "R_Block_Is_Pos";
	public static final String isGBlockPresent = "isGBlockPresent";
	public static final String EmbeddedType = "EmbeddedType";
	public static final String isEmbedded = "isEmbedded";
	public static final String commentStart = "commentStart";
	public static final String commentEnd = "commentEnd";
	public static final String blockSep = "blockSep";
	public static final String cmdSep = "-";
	public static final String classNameSep = "_";
	public static final String space = " ";
	public static final String paramSep = ",";
	public static final String msgTerminator = ";";
	public static final String valueSeperator = "=";
	public static final String backSlash = "\\\"";
	public static final int GBLOCK_NUMBER = 4;
	public static final int FBLOCK_START = 5;
	public static final String sid = "sid";

	public static final String date = "date";
	public static final String time = "time";
	public static final String alarmcode = "alarmcode";
	public static final String atag = "atag";
	public static final String outputCode = "outputCode";
	public static final String verb = "verb";
	public static final String mod1 = "mod1";
	public static final String mod2 = "mod2";

	private TL1CommandClassMetadata() {
	}

}