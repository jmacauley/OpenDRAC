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

package com.nortel.appcore.app.drac.server.requesthandler;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.errorhandling.DracRemoteException;
import com.nortel.appcore.app.drac.common.errorhandling.ResourceKey;

/**
 * Created on 24-Aug-06
 */
public class RequestHandlerException extends DracException {
	private static final long serialVersionUID = 1L;
	private static final ResourceKey DEFAULT_KEY = new ResourceKey(
	    DracErrorConstants.GENERAL_ERROR_INTERNAL);

	public RequestHandlerException(int errorCode, Object[] args) {
		super(errorCode, args);
	}

	public RequestHandlerException(int errorCode, Object[] args, Exception t) {
		super(errorCode, args, t);
	}

	public RequestHandlerException(Exception t) {
		super(
		    t != null ? (t.getCause() instanceof DracRemoteException ? ((DracRemoteException) t
		        .getCause()).getKey() : DEFAULT_KEY)
		        : // t !instanceOf DracRemoteException
		        null, // t == null
		    t != null ? (t.getCause() instanceof DracRemoteException ? ((DracRemoteException) t
		        .getCause()).getArgs() : null)
		        : // t !instanceOf DracRemoteException
		        null, // t == null
		    t);
	}
}
