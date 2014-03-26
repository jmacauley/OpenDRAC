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

package com.nortel.appcore.app.drac.common.security.policy;

import java.io.Serializable;

public final class PolicyRequest implements Serializable {
	public static enum CommandType {
		READ, WRITE, CANCEL, EDIT, DELETE, CREATE, SET_AS_PARENT
	}

	private static final long serialVersionUID = 1L;
	private final PolicyCheckable requestor;
	private final CommandType type;

	public PolicyRequest(PolicyCheckable request, CommandType aType) {
		requestor = request;
		type = aType;
	}

	public PolicyCheckable getRequestor() {
		return requestor;
	}

	public CommandType getType() {
		return type;
	}

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PolicyRequest [requestor=");
    builder.append(requestor);
    builder.append(", type=");
    builder.append(type);
    builder.append("]");
    return builder.toString();
  }


}
