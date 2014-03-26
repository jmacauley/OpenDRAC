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
import java.util.HashMap;

/**
 * @author <a href="mailto:nchellia@nortel.com">Niranjan Chelliah </a>
 */

public final class StatusType implements Serializable {

	public static enum StatusInfoType {
		INFO, WARNING, ERROR
	}

	static final long serialVersionUID = 1L;

	public static final String START_TIME = "START_TIME";
	public static final String END_TIME = "END_TIME";
	public static final String MESSAGE = "MESSAGE";

	private StatusInfoType type;
	private HashMap<String, String> properties;
	private String subTaskId;

	public StatusType() {
		super();
	}

	public StatusType(StatusInfoType type) {
		super();
		this.type = type;
	}

	/**
	 * Returns the value to which the specified key is mapped.
	 * 
	 * @param key
	 *          the key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, or
	 *         <tt>null</tt> if the map contains no mapping for this key.
	 */
	public String getProperties(String key) {
		if (properties == null) {
			return null;
		}
		return properties.get(key);
	}

	public String getSubTaskId() {
		return this.subTaskId;
	}

	/**
	 * @return Returns the type.
	 */
	public StatusInfoType getType() {
		return this.type;
	}

	public void setProperties(String key, String value) {
		if (properties == null) {
			properties = new HashMap<String, String>(10);
		}
		properties.put(key, value);
	}

	public void setSubTaskId(String subTaskId) {
		this.subTaskId = subTaskId;
	}

	// public void setType(StatusInfoType type)
	// {
	// this.type = type;
	// }
}
