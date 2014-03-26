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

package com.nortel.appcore.app.drac.server.neproxy;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.DebugFileLogger;

public final class Force10Logger {
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(Force10Logger.class);
	private static final String STARS = "*****";

	private static boolean defaultEnabled = Boolean.parseBoolean(System
	    .getProperty("TraceTL1", "true"));
	/*
	 * Not really a memory leak, but once opened we never forget about a logger
	 */
	private static final Map<String, Logger> OPEN_LOGGERS = new HashMap<String, Logger>();

	private Force10Logger() {
	}

	public static Logger getLogger(String address, int port) {
		try {
			synchronized (OPEN_LOGGERS) {
				String fileName = "./var/log/f10_" + address + "_" + port + ".log".replace("/", File.separator);
				Logger l = OPEN_LOGGERS.get(fileName);
				if (l != null) {
					return l;
				}

				l = DebugFileLogger.getLogger(fileName, "%m", defaultEnabled);
				l.debug(STARS + " (re)Starting logger at " + new Date().toString()
				    + STARS + "\n");
				OPEN_LOGGERS.put(fileName, l);
				return l;
			}
		}
		catch (Exception t) {
			log.error("Unable to create logger " + address + " " + port, t);
		}
		return null;
	}

	public static void log(String address, int port, String msg) {
		Logger l = getLogger(address, port);
		if (l != null && l.isDebugEnabled()) {
			l.debug(msg + "\n");
		}
	}
}
