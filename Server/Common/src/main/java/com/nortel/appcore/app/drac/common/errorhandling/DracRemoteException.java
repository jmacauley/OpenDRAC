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

import java.rmi.RemoteException;
import java.util.Locale;

public class DracRemoteException extends RemoteException {
	private static final long serialVersionUID = 1L;
	private final ResourceKey resourceKey;
	private final Locale userLocale;
	private final Object[] argArray;

	public DracRemoteException(DracException exception) {
		this(exception, exception);
	}

	public DracRemoteException(DracException exception, Throwable throwable) {
		this("", exception.getLocale(), exception.getKey(), exception.getArgs(),
		    throwable);
	}

	public DracRemoteException(int errorCode, Object[] args) {
		this(new ResourceKey(errorCode), args);
	}

	public DracRemoteException(int errorCode, Object[] args, Throwable throwable) {
		this("", null, new ResourceKey(errorCode), args, throwable);
	}

	public DracRemoteException(Locale locale, int errorCode, Object[] args,
	    Throwable throwable) {
		this("", locale, new ResourceKey(errorCode), args, throwable);
	}

	public DracRemoteException(ResourceKey key, Object[] args) {
		this("", ExceptionFormatter.CURRENT_LOCALE, key, args, null);
	}

	public DracRemoteException(String description, Locale locale,
	    ResourceKey key, Object[] args, Throwable throwable) {
		super(description == null ? "" : description, throwable);
		userLocale = locale == null ? Locale.getDefault() : locale;
		resourceKey = key == null ? new ResourceKey(
		    DracErrorConstants.GENERAL_ERROR_INTERNAL) : key;
		argArray = args;
	}

	public Object[] getArgs() {
		return argArray;
	}

	public int getErrorCode() {
		if (resourceKey != null) {
			return resourceKey.getKeyAsErrorCode();
		}

		return ResourceKey.INVALID_KEY;
	}

	public ResourceKey getKey() {
		return resourceKey;
	}

	public String getKeyAsString() {
		if (resourceKey == null) {
			return null;
		}
		return this.resourceKey.getKeyAsString();
	}

	// Dynamic getMessage()
	@Override
	public String getMessage() {
		return super.getMessage()
		    + " : "
		    + ExceptionFormatter.INSTANCE.formatMessage(userLocale,
		        resourceKey, argArray);
	}
}
