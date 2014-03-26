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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.common;

import com.nortel.appcore.app.drac.common.types.Layer;

/**
 * TNA Models a TNA in the DRAC Administration Desktop
 * 
 * @author adlee
 * @since 2006-01-12
 */
public final class Tna {
	private final String neid;
	private final String tna;
	private final String aid;
	private final String type;
	private final String wavelength;
	private final String siteId;
	private final Layer layer;

	public Tna(String neId, String facAid, String tnaStr, Layer l,
	    String tnaType, String waveLength, String siteid) {
		neid = neId;
		tna = tnaStr;
		type = tnaType;
		wavelength = waveLength;
		siteId = siteid;
		aid = facAid;
		layer = l;
	}

	public String getAID() {
		return aid;
	}

	public Layer getLayer() {
		return layer;
	}

	public String getNEID() {
		return neid;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getTNA() {
		return tna;
	}

	public String getType() {
		return type;
	}

	public String getWavelength() {
		return wavelength;
	}

	public String toDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TNA");

		sb.append(" neid:");
		sb.append(neid);

		sb.append(" tna:");
		sb.append(tna);

		sb.append(" aid:");
		sb.append(aid);

		sb.append(" type:");
		sb.append(type);

		sb.append(" wavelength:");
		sb.append(wavelength);

		sb.append(" siteId:");
		sb.append(siteId);

		sb.append(" layer:");
		sb.append(layer);

		return sb.toString();
	}

	@Override
	public String toString() {
		return tna;
	}

}
