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

/**
 * This class contains a typical TL-1 token of the form 'KEY=VALUE'.
 */
final class TL1Token {
	public final String key;
	public final String value;

	/**
	 * Create a new TL1Token which contains the key and values specified. No
	 * validation is performed on the parameters passed into the constructor.
	 * 
	 * @param newKey
	 *          the key to use for this INSTANCE.
	 * @param newvalue
	 *          the value to use for this INSTANCE
	 */
	public TL1Token(String newKey, String newValue) {
		// set up the members
		key = newKey;
		value = newValue;
	}

	/**
	 * Does not peform any error checking: an exception will be thrown if the
	 * parsed string is empty, or does not contain an equal sign.
	 * 
	 * @param token
	 *          'KEY=VALUE'
	 */
	TL1Token(String token) {
		int index = token.indexOf('=');

		key = token.substring(0, index);
		value = token.substring(index + 1);
	}
}
