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

package com.nortel.appcore.app.drac.common.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum NeType {
  HDX, OME, OME5, OME6, OME7, OME8, OME9, PP8600, CPL, GMPLS, ASTN, OMEBB, LOCALNE, FORCE10, UNKNOWN;

  private static final Logger log = LoggerFactory.getLogger(NeType.class);

  public static NeType fromString(String s) throws Exception {
    try {
      return NeType.valueOf(s.toUpperCase());
    }
    catch (Exception e) {
      log.error("Unable to map NE type string <" + s + "> to NeType enum; returning UNKNOWN");
      return UNKNOWN;
    }
  }
}