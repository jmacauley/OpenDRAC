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

import com.nortel.appcore.app.drac.common.types.State.CALL;

/**
 * Created on Dec 5, 2005
 * 
 * @author nguyentd
 */
public final class CallIdType implements Serializable {
	private static final long serialVersionUID = 1;
	private final String id;

	/**
	 * The identification of the controller that owns the Call.
	 */
	private String controllerId = "unknown";
	private CALL status = CALL.EXECUTION_PENDING;

	public CallIdType(String id) {
		this.id = id;
	}

	/**
	 * @return the controllerId
	 */
	public String getControllerId() {
		return controllerId;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the status
	 */
	public CALL getStatus() {
		return status;
	}

	/**
	 * @param controllerId
	 *          the controllerId to set
	 */
	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}

	// /**
	// * @param id the id to set
	// */
	// public void setId(String id)
	// {
	// this.id = id;
	// }

	/**
	 * @param status
	 *          the status to set
	 */
	public void setStatus(CALL status) {
		this.status = status;
	}

}
