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

public final class NetworkElementAdjacency implements Serializable {

  private static final long serialVersionUID = 5900152473680247190L;
  
  
	private final String neid;
	private final String port;
	private final String txTag;
	private final String rxTag;
	private final String type;
	private boolean manual = false;

	/*
	 * Flag is used temporally during the topology consolidation and is not stored
	 * or preserved in the database
	 */
	private transient boolean flag = false;

	public NetworkElementAdjacency(String neId, String nePort,
	    String transmitTag, String receiveTag, String topoType, boolean isManual) {
		neid = neId;
		port = nePort;
		txTag = transmitTag;
		rxTag = receiveTag;
		type = topoType;
		manual = isManual;
	}

	public String getNeid() {
		return neid;
	}

	public String getPort() {
		return port;
	}

	public String getRxTag() {
		return rxTag;
	}

	public String getTxTag() {
		return txTag;
	}

	public String getType() {
		return type;
	}

	public boolean isFlagSet() {
		return flag;
	}

	public void setFlag(boolean flag) {
  	this.flag = flag;
  }

	public boolean isManual() {
		return manual;
	}


	@Override
	public String toString() {
		return "NetworkElementAdjacency [manual=" + manual + ", neid=" + neid
		    + ", port=" + port + ", rxTag=" + rxTag + ", txTag=" + txTag
		    + ", type=" + type + "]";
	}

}
