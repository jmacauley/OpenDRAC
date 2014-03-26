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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author
 */
public class ResourceKey extends Object implements Serializable {
	private static final long serialVersionUID = -8576620303799011759L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Object key;
	public static final int INVALID_KEY = DracErrorConstants.GENERAL_ERROR_INTERNAL;

	public ResourceKey(Object resourceKey) {
		super();
		if (resourceKey == null) {
			log.error("Resource key should not be null");
		}

		key = resourceKey;
	}

	public int getKeyAsErrorCode() {
		try {
			return Integer.parseInt(this.getKeyAsString().trim());
		}
		catch (Exception e) {
			log.error("Key is not an integer");
			return INVALID_KEY;
		}
	}

	public String getKeyAsString() {
		return key.toString();
	}

}
