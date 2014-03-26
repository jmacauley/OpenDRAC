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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.engine;

/**
 * SyntaxException is thrown by TL1Reader's many read methods. It indicates that
 * the the data read from the network input stream is not valid for the TL1
 * information unit that was expected. </p></p> For example,
 * TL1Reader.readCorrelationTag() will throw a SyntaxException if the first
 * character it reads is not alphanumeric. @see TL1Reader
 */
public final class SyntaxException extends Exception {
	private static final long serialVersionUID = -7669990376108683312L;

	public SyntaxException(String s) {
		super(s);
	}

	public SyntaxException(String s, Throwable t) {
		super(s, t);
	}
}
