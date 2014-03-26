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

public class DracException extends Exception {
  private static final long serialVersionUID = 1L;
  private final ResourceKey resourceKey;
  private final Locale userLocale;
  private final Object[] argArray;

  public DracException(int errorCode, Object[] args) {
    this(null, new ResourceKey(errorCode), args, null);
  }

  public DracException(int errorCode, Object[] args, Exception throwable) {
    this(ExceptionFormatter.CURRENT_LOCALE, new ResourceKey(errorCode), args,
        throwable);
  }

  public DracException(Locale locale, ResourceKey key, Object[] args,
      Exception throwable) {
    super(throwable);
    this.userLocale = locale == null ? Locale.getDefault() : locale;
    this.resourceKey = key == null ? new ResourceKey(
        DracErrorConstants.GENERAL_ERROR_INTERNAL) : key;
    this.argArray = args;
  }

  public DracException(ResourceKey key, Object[] args, Exception t) {
    this(ExceptionFormatter.CURRENT_LOCALE, key, args, t);
  }

  public Object[] getArgs() {
    return argArray;
  }

  public int getErrorCode() {
    if (resourceKey != null) {
      return this.resourceKey.getKeyAsErrorCode();
    }

    return ResourceKey.INVALID_KEY;
  }

  public ResourceKey getKey() {
    return this.resourceKey;
  }

  public String getKeyAsString() {
    if (resourceKey == null) {
      return null;
    }
    return this.resourceKey.getKeyAsString();
  }

  public Locale getLocale() {
    return userLocale;
  }

  @Override
  public String getMessage() {
    return ExceptionFormatter.INSTANCE.formatMessage(this.userLocale,
        this.resourceKey, this.argArray);
  }

}
