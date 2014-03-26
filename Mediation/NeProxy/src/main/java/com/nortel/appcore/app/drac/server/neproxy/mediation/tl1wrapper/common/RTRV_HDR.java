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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1wrapper.common;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1wrapper.Parameter;

/**
 * @author nguyentd
 */
public class RTRV_HDR {
	public static final boolean isMOD2Required = false;
	public static final boolean isMOD2Enumerated = false;
	public static final boolean isUnprintable = false;
	public static final boolean isTIDRequired = true;
	public static final String EmbeddedType = "NONE";
	public static final String AID[] = new String[0];
	public static final String CTAG = "CTAG";
	public static final boolean isGBlockPresent = false;
	public static Parameter F_BLOCKS[][] = new Parameter[0][];
	public static final boolean F_Block_Is_Pos[] = new boolean[0];
	public static Parameter R_BLOCKS[][] = new Parameter[0][];
	public static final boolean R_Block_Is_Pos[] = new boolean[0];
}
