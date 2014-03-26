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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DesktopUtil This class contains various miscellaneous helper methods used in
 * the DRAC Administration Desktop.
 * 
 * @author adlee
 * @since 2006-01-12
 */

public final class DesktopUtil {
  private static final Logger log = LoggerFactory.getLogger(DesktopUtil.class);
	private final Map<String, String> opticalToMBRateMap = new HashMap<String, String>();
	

	public DesktopUtil() {
		populateOpticalRateMap();
	}

	public static String formatDateTime(long time, Locale locale,
	    String dateTimePattern, TimeZone timeZonePreference) {
		Calendar dateTime = Calendar.getInstance();
		dateTime.setTimeInMillis(time);

		DateFormat dateTimeFormatter = new SimpleDateFormat(dateTimePattern, locale);
		dateTimeFormatter.setTimeZone(timeZonePreference);
		return dateTimeFormatter.format(dateTime.getTime());
	}

	public static int getChannelsForRate(String rate) {

		int result = 0;

		if ("STS1".equalsIgnoreCase(rate)) {
			result = 1;
		}
		else if ("STS3C".equalsIgnoreCase(rate)) {
			result = 3;
		}
		else if ("STS6C".equalsIgnoreCase(rate)) {
			result = 6;
		}
		else if ("STS12C".equalsIgnoreCase(rate)) {
			result = 12;
		}
		else if ("STS24C".equalsIgnoreCase(rate)) {
			result = 24;
		}
		else if ("STS48C".equalsIgnoreCase(rate)) {
			result = 48;
		}

		return result;

	}

	public static void sizeColumns(JTable table) {

		int nColumns = table.getTableHeader().getColumnModel().getColumnCount();
		int maxWidth = 75;
		int fudgeFactor = 35;
		TableColumn c = null;
		java.awt.FontMetrics metrics = table.getFontMetrics(table.getFont());

		try {
			// Set all column sizes to header value sizes
			for (int k = 0; k < nColumns; k++) {
				c = table.getTableHeader().getColumnModel().getColumn(k);
				c.setPreferredWidth(metrics.stringWidth((String) c.getHeaderValue())
				    + fudgeFactor);
			}

			for (int i = 0; i < table.getRowCount(); i++) {
				for (int j = 0; j < table.getColumnCount(); j++) {
					c = table.getTableHeader().getColumnModel().getColumn(j);

					if (table.getValueAt(i, j) != null) {
						if (table.getValueAt(i, j).toString().length() * fudgeFactor > c
						    .getPreferredWidth()) {
							maxWidth = Math.max(
							    metrics.stringWidth(table.getValueAt(i, j).toString())
							        + fudgeFactor, c.getPreferredWidth()); 
						}
						else {
							maxWidth = c.getPreferredWidth();
						}
					}
					else {
						maxWidth = c.getPreferredWidth();
					}

					if (maxWidth > c.getPreferredWidth()) {
						c.setPreferredWidth(maxWidth);
						c.setWidth(maxWidth);
					}

					maxWidth = 75;
				}
			}
		}
		catch (Exception e) {
			log.error("Exception sizing columns: ", e);

		}

	}

	public static Date toDate(Locale locale, TimeZone tz, Date date, Date time) {
		// All console parameters are rounded to the minute
		return toDate(locale, tz, date, time, Calendar.MINUTE);
	}

	public static Date toDate(Locale locale, TimeZone tz, Date date, Date time,
	    int timePrecision) {
		Calendar cal1 = Calendar.getInstance(tz, locale);
		Calendar cal2 = Calendar.getInstance(tz, locale);
		cal1.setTime(date);
		cal2.setTime(time);

		Calendar cal = new GregorianCalendar(cal1.get(Calendar.YEAR),
		    cal1.get(Calendar.MONTH), cal1.get(Calendar.DAY_OF_MONTH));
		cal.setTimeZone(tz);

		switch (timePrecision) {
		case Calendar.HOUR_OF_DAY:
			cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
			break;
		case Calendar.MINUTE:
			cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
			break;
		case Calendar.SECOND:
			cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
			cal.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
			break;
		default:
			cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
			cal.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
			cal.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
		}

		return cal.getTime();
	}

	public String getRateInMb(String sonetOrSDHRate) {
		return opticalToMBRateMap.get(sonetOrSDHRate);
	}

	public Map<String, String> getRateMap() {
		return opticalToMBRateMap;
	}

	public Image loadImageFromJar(String imageName) {
		Image img = null;

		try {
			
			URL imgURL = getClass().getResource(imageName);
			if (imgURL != null) {
				img = Toolkit.getDefaultToolkit().getImage(imgURL);
				
			}
			else {
				log.debug("getClass().getResource() for: " + imageName
				    + " returns: null");
			}

		}
		catch (Exception e) {
			log.error("Error: " + imageName, e);
		}

		return img;

	}

	/*
	 * Mapping table: SDH SONET Mb/s VC11 VT1.5 1.5 VC12 VT2 2 VC3 STS1 50 VC4
	 * STS3c 150 VC4-4c STS12c 600 VC4-8c STS24c 1200 VC4-16c STS48c 2400 VC4-64c
	 * STS192c 9600
	 */
	private void populateOpticalRateMap() {
		opticalToMBRateMap.put("VC11", "1.5");
		opticalToMBRateMap.put("VC12", "2");
		opticalToMBRateMap.put("VC3", "50");
		opticalToMBRateMap.put("VC4", "150");
		opticalToMBRateMap.put("VC4-4C", "600");
		opticalToMBRateMap.put("VC4-8C", "1200");
		opticalToMBRateMap.put("VC4-16C", "2400");
		opticalToMBRateMap.put("VC4-64C", "9600");
		opticalToMBRateMap.put("STS1", "50");
		opticalToMBRateMap.put("STS3C", "150");
		opticalToMBRateMap.put("STS12C", "600");
		opticalToMBRateMap.put("STS24C", "1000"); // HACK!
		opticalToMBRateMap.put("STS48C", "2400");
		opticalToMBRateMap.put("STS192C", "9600");
	}

}
