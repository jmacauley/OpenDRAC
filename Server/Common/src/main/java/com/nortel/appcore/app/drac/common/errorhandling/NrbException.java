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

package com.nortel.appcore.app.drac.common.errorhandling;

import java.util.Locale;

/**
 * Created on Dec 14, 2005
 * 
 * @author nguyentd
 */
public class NrbException extends DracRemoteException {
	private static final long serialVersionUID = 1L;

	public NrbException(DracException exception) {
		super(exception);
	}

	public NrbException(int errorCode, Object[] args) {
		super(errorCode, args);
	}

	public NrbException(int errorCode, Object[] args, Throwable t) {
		super(null, errorCode, args, t);
	}
	
	public NrbException(Locale locale, int errorCode, Object[] args,
	    Throwable throwable) {
		super(locale, errorCode, args, throwable);
	}

	public NrbException(String description, Throwable t) {
		super(description, null, null, null, t);
	}
}
