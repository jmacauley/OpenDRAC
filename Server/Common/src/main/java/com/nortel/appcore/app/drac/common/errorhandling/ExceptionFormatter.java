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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author
 */

public enum ExceptionFormatter {

  INSTANCE;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private String fileBase = System.getProperty(
      "org.opendrac.common.error.handling.base",
      "com.nortel.appcore.app.drac.common.resources.ErrorDescription");
  public final Locale DEFAULT_LOCALE = Locale.ENGLISH;
  public static final Locale CURRENT_LOCALE = Locale.getDefault();
  private final Map<Locale, ResourceBundle> resourceBundleList = new HashMap<Locale, ResourceBundle>();

  private ExceptionFormatter() {
    getResourceBundle(DEFAULT_LOCALE);
  }

  public String formatMessage(Locale locale, ResourceKey strKey, Object[] args) {

    if (strKey.getKeyAsString() == null) {
      log.error("Resource key should not be null");
      return null;
    }

    ResourceBundle resourceBundle = getResourceBundle(locale);
    String msg = null;

    try {
      if (resourceBundle == null) {
        log.error("Missing error description resource file: "
            + formName(locale));
      }
      msg = resourceBundle.getString(strKey.getKeyAsString());

    }
    catch (Exception e) {
      log.error("Failed to lookup message in bundle " + locale + " " + strKey,
          e);
      msg = strKey.getKeyAsString();
      if (args != null && args.length > 0) {
        StringBuilder details = new StringBuilder(msg);
        for (int i = 0; i < args.length; i++) {
          details.append("?");
          if (i > 0) {
            details.append("&");
          }
          details.append(args[i] == null ? "null" : args[i].toString());
        }
        msg = details.toString();
      }
      return msg;
    }

    if (args != null) {
      // 
      msg = MessageFormat.format(msg, args);
    }

    return msg;
  }

  public String formatMessage(ResourceKey strKey, Object[] args) {
    return formatMessage(CURRENT_LOCALE, strKey, args);
  }

  public String getFileBase() {
    return fileBase;
  }

  public ResourceBundle getResourceBundle(Locale locale) {
    ResourceBundle bundle = resourceBundleList.get(locale);

    if (bundle == null) {
      try {
        /*
         * InputStream inputStream = new FileInputStream( new
         * File(formName(locale))); bundle = new
         * PropertyResourceBundle(inputStream);
         */

        bundle = ResourceBundle.getBundle(fileBase, locale);
        resourceBundleList.put(locale, bundle);

      }
      catch (Exception e) {
        log.debug("Resource not found for locale: " + locale.toString(), e);
        try {
          // bundle = resourceBundleList.get(DEFAULT_LOCALE);
          bundle = ResourceBundle.getBundle(fileBase, DEFAULT_LOCALE);
          // substitute with the default bundle
          // so the process would not get here the second time
          resourceBundleList.put(locale, bundle);
          log.debug("Error Description of " + locale.toString()
              + "locale is replaced with the default.");
        }
        catch (Exception ex) {
          log.error(
              "Default resource not found for locale: "
                  + DEFAULT_LOCALE.toString(), ex);
        }
      }

      return bundle;
    }

    return bundle;
  }

  public void setFileBase(String fileBase) {
    this.fileBase = fileBase;
  }

  private String formName(Locale locale) {
    return fileBase + "_" + locale.toString() + ".properties";
  }
}
