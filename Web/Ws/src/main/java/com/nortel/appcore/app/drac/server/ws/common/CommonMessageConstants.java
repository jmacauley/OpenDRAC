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

package com.nortel.appcore.app.drac.server.ws.common;

public final class CommonMessageConstants {
	public static final String COMPLETED_SUCCESSFULLY = "Success";
	public static final String MISSING_SOAP_ENVELOPE_IN_REQUEST_MESSAGE = "Missing SOAP envelope";
	public static final String MISSING_SOAP_HEADER_IN_REQUEST_MESSAGE = "Missing SOAP header";
	public static final String MISSING_SOAP_BODY_IN_REQUEST_MESSAGE = "Missing SOAP body";
	public static final String INVALID_NAMESPACE_IN_REQUEST_MESSAGE = "Invalid namespace";
	public static final String REQUEST_MESSAGE_PARSING_FAILURE = "Message couldn't be parsed";
	public static final String UNSUPPORTED_SOAP_VERSION_IN_REQUEST_MESSAGE = "Unsupported SOAP version, SOAP 1.1 must be used";
	public static final String USER_PROFILE_NOT_FOUND = "User policy profile could not be found for this user";
	public static final String REQUESTED_OPERATION_FAILED = "Requested operation failed";
	public static final String SYSTEM_CONFIG_RETRIEVAL_FAILURE = "Server configuration couldn't be retrieved";
	public static final String DATE_TIME_PARAMETER_RETRIEVAL_FAILURE = "Time could not be retrieved from request message";

	private CommonMessageConstants() {
		super();
	}
}
