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

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * The LPCP_PORT Audit will return a list of AuditResults showing on a port by port
 * basis the results of the audit. Once a XML structure was used, but we're
 * trying to remove the XML.
 */
public final class AuditResult implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String neId;
	private final String tid;
	private final String aid;
	private final String rate;
	private final boolean mismatched;
	private final BigInteger rawTracker;
	private final BigInteger connectionTracker;

	public AuditResult(String networkId, String neTid, String neAid,
	    String neRate, boolean mismatch, BigInteger neRawTracker,
	    BigInteger neConnectionTracker) {
		neId = networkId;
		tid = neTid;
		aid = neAid;
		rate = neRate;
		mismatched = mismatch;
		rawTracker = neRawTracker;
		connectionTracker = neConnectionTracker;
	}

	public String getAid() {
		return aid;
	}

	public BigInteger getConnectionTracker() {
		return connectionTracker;
	}

	public String getNeId() {
		return neId;
	}

	public String getRate() {
		return rate;
	}

	public BigInteger getRawTracker() {
		return rawTracker;
	}

	public String getTid() {
		return tid;
	}

	public boolean isMismatched() {
		return mismatched;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuditResult [neId=");
		builder.append(neId);
		builder.append(", tid=");
		builder.append(tid);
		builder.append(", aid=");
		builder.append(aid);
		builder.append(", rate=");
		builder.append(rate);
		builder.append(", mismatched=");
		builder.append(mismatched);
		builder.append(", rawTracker=");
		builder.append(rawTracker);
		builder.append(", connectionTracker=");
		builder.append(connectionTracker);
		builder.append("]");
		return builder.toString();
	}

}
