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

package com.nortel.appcore.app.drac.common.utility;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.slf4j.LoggerFactory;

/**
 * @author pitman
 */
public final class DebugFileLogger {

  private static final org.slf4j.Logger log = LoggerFactory
      .getLogger(DebugFileLogger.class);

  private DebugFileLogger() {
    super();
  }

  public static Logger getLogger(String logFileName, String pattern,
      boolean enableAfterCreation) throws Exception {
    logFileName = logFileName.replace("/", File.separator);
    Logger logger = createAndConfigureLogger(logFileName, pattern);
    if (!enableAfterCreation) {
      logger.setLevel(Level.OFF);
    }
    return logger;
  }

  private static Logger createAndConfigureLogger(String fileName, String pattern)
      throws Exception {
    File file = new File(fileName).getParentFile();
    if (file != null && !file.exists()) {
      log.warn("Logging directory " + file.toString()
          + " does not exist, attempting to create");
      // Ignore the return code, if we fail, the logger won't work.
      if (!file.mkdirs()) {
        log.error("Unable to create logging directory " + file);
      }
    }

    // "%n%d %-5p Thread: [%t]%n%m%nMDC context=%v%nFrom: %l%n"
    Logger logger = Logger.getLogger(fileName);
    logger.setAdditivity(false);
    logger.setLevel(Level.DEBUG);
    CompressedDailyRollingFileAppender appender = new CompressedDailyRollingFileAppender(
        new PatternLayout(pattern), fileName, true);
    appender.setFile(fileName, true, false, 0);
    appender.activateOptions();
    logger.addAppender(appender);
    return logger;

  }
}
