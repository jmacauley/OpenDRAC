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

/**
 * Created on Dec 5, 2005
 * 
 * @author nguyentd
 */
public final class OsType implements Serializable {
	private static final long serialVersionUID = 1;
	private final String osName;
	private final String osVersion;
	private final String osArchitecture;

	public OsType(String name, String version, String arch) {
		osName = name;
		osVersion = version;
		osArchitecture = arch;
	}

	public String getOsArchitecture() {
		return osArchitecture;
	}

	public String getOsName() {
		return osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	@Override
	public String toString() {
		return "OsName=" + osName + " OsVersion=" + osVersion + " osArchiture="
		    + osArchitecture;
	}
}
