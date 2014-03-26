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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * NameDefinedBlock encapsulates the construction, structure, and display of a
 * name-defined block of a TL1 input command message. It provides a simple,
 * high-level API to specify the parameters of a name-defined block. The general
 * format of a name-defined block:</p></p> <name-defined block> ::=
 * [<name-defined parameter>[,<name-defined parameter>]*] </p> <name-defined
 * parameter> ::= <name>=<value> </p></p> Note that name-defined parameters are
 * stored as a single string. Since the value will always be concatenated with a
 * '=', it may as well done initially and simplify storage.</p></p> Note that
 * there is no special internal representation for omitted parameters. Since
 * name- defined blocks are not ordered, an empty parameter ",," is meaningless.
 */

final class NameDefinedBlock {

	/**
	 * The parameters for this name-defined block. Name-defined parameters are
	 * stored in the order in which they are specified. Name and value are
	 * concatenated and stored here as one string, for the sake of simplicity.
	 */

	private final Hashtable<String, String> parameters = new Hashtable<String, String>();

	/**
	 * Return true if the block contains any parameters, false otherwise.
	 */

	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	/**
	 * Add a name defined parameter to this block. A null parameter value deletes
	 * the name and value.
	 */

	public void setParameter(String name, String parameter) {
		if (parameter == null) {
			parameters.remove(name);
		}
		else {
			parameters.put(name, parameter);
		}
	}

	/**
	 * Return a string representation of this block. The string returned is also
	 * the correct TL1 format of the block.
	 */

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		Enumeration<String> keyList = parameters.keys();
		String name;
		String value;

		while (keyList.hasMoreElements()) {
			name = keyList.nextElement();
			value = parameters.get(name);
			buffer.append(name + "=" + value);
			if (keyList.hasMoreElements()) {
				buffer.append(',');
			}
		}
		return buffer.toString();
	}
}
