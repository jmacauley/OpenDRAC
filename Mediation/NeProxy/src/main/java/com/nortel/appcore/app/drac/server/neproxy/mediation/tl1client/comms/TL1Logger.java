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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.DebugFileLogger;

/**
 * @author pitman
 */
public final class TL1Logger {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(TL1Logger.class);
  
	private static final String STARS = "*****";

	private static boolean defaultEnabled = Boolean.parseBoolean(System
	    .getProperty("TraceTL1", "true"));
	/*
	 * Not really a memory leak, but once opened we never forget about a logger
	 */
	private static final Map<String, Logger> OPEN_LOGGERS = new HashMap<String, Logger>();

	private TL1Logger() {
	}

	public static String disableAllLogs() {
		StringBuilder sb = new StringBuilder();
		sb.append("DisableAllLogs:");
		synchronized (OPEN_LOGGERS) {
			// Turn off all logs and any future logs
			if (defaultEnabled) {
				sb.append(" future loggers disabled");
			}
			else {
				sb.append(" future loggers already disabled");
			}
			defaultEnabled = false;

			int tot = 0;
			int disabled = 0;
			for (Logger l : OPEN_LOGGERS.values()) {
				tot++;
				if (!l.getLevel().equals(Level.OFF)) {
					l.debug(STARS + " DISABLED LOGGER at " + new Date().toString() + " "
					    + STARS + "\n");
					l.setLevel(Level.OFF);
					disabled++;
				}
			}
			sb.append(" " + disabled + " loggers were disabled out of " + tot
			    + " in existance.");
		}
		return sb.toString();
	}

	public static String enableAllLogs() {
		StringBuilder sb = new StringBuilder();
		sb.append("EnableAllLogs:");
		synchronized (OPEN_LOGGERS) {
			// Turn on all logs and any future logs
			// Turn off all logs and any future logs
			if (defaultEnabled) {
				sb.append(" future loggers already enabled");
			}
			else {
				sb.append(" future loggers enabled");
			}
			defaultEnabled = true;

			int tot = 0;
			int enabled = 0;
			for (Logger l : OPEN_LOGGERS.values()) {
				tot++;
				if (!l.getLevel().equals(Level.ALL)) {
					l.setLevel(Level.ALL);
					l.debug(STARS + " ENABLED LOGGER at " + new Date().toString() + " "
					    + STARS + "\n");
					enabled++;
				}
			}
			sb.append(" " + enabled + " loggers were enabled out of " + tot
			    + " in existance.");
		}
		return sb.toString();
	}

	public static void log(String address, int port, String msg) {
		Logger l = getLogger(address, port);
		if (l != null && l.isDebugEnabled()) {
			l.debug(msg + "\n");
		}
	}

	public static void logRecvTl1(String address, int port, String msg) {
		Logger l = getLogger(address, port);
		if (l != null && l.isDebugEnabled()) {
			l.debug(msg);
		}
	}

	public static void logSentTl1(String address, int port, String msg) {
		Logger l = getLogger(address, port);
		if (l != null && l.isDebugEnabled()) {
			if (msg.startsWith("RTRV-HDR:")) {
				l.debug("<<RTRV-HDR keep alive sent>>\n");
			}
			else if (msg.startsWith("ACT-USER")) {
				// Simple way of keeping the password out of the logs.
				l.debug("<<ACT-USER sent, password hidden>>\n");
			}
			else {
				l.debug(msg);
			}
		}
	}

	private static Logger getLogger(String address, int port) {
		try {
			synchronized (OPEN_LOGGERS) {
				String fileName = "./var/log/tl1_" + address + "_" + port + ".log".replace("/", File.separator);
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
}
