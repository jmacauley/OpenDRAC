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
 * A runtime exception that occurs when data is being accessed in an
 * inapproprate manner. This exception implies that the user of the code that
 * has thrown the exception has not checked the underlying data for validity
 * before accessing it. Note that since this is a runtime exception, the
 * compiler will not force it to be caught explicitly.
 * 
 * @see java.lang.RuntimeException
 */
final class IllegalDataAccessException extends RuntimeException {

	private static final long serialVersionUID = 3569515921133978842L;

	/**
	 * Create a new exception with the specified detailed message.
	 * 
	 * @param detailedMessage
	 *          the message describing the cause of the exception.
	 */
	public IllegalDataAccessException(String detailedMessage) {
		super(detailedMessage);
	}

}
