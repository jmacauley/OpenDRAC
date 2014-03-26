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

package com.nortel.appcore.app.drac.server.webserver.struts.common;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.struts.util.LabelValueBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;

public final class DracHelper {
  
  private static final Logger log = LoggerFactory.getLogger(DracHelper.class);
    private List<LabelValueBean> timeStringList;
    public static final String WEB_DATETIME_PARSE_FORMAT = "EEEE, MMMMM dd, yyyy H:mm";

    public static int calculateDuration(String language, String startTime,
            String endTime) {
        Locale locale = new Locale(language);
        int duration = 0;
        SimpleDateFormat dateFormatter = (SimpleDateFormat) DateFormat
        .getTimeInstance(DateFormat.SHORT, locale);
        Date start = dateFormatter.parse(startTime, new ParsePosition(0));
        Date end = dateFormatter.parse(endTime, new ParsePosition(0));
        if (start != null && end != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            int startHrs = cal.get(Calendar.HOUR_OF_DAY);
            int startMin = cal.get(Calendar.MINUTE);
            cal.setTime(end);
            int endHrs = cal.get(Calendar.HOUR_OF_DAY);
            int endMin = cal.get(Calendar.MINUTE);
            int durHrs = (endHrs - startHrs) * 60 * 60 * 1000;
            int durMns = (endMin - startMin) * 60 * 1000;
            duration = durHrs + durMns;
        }
        
        return duration;

    }

    public static String decodeFromUTF8(String value) throws Exception {
        return URLDecoder.decode(value, "UTF-8");
    }

    public static boolean doWildCardWork(String searchFor, String ugName) {
        String tempString = "";
        boolean match = false;

        if (searchFor.equals("*")) {
            match = true;
        }
        else if (searchFor.startsWith("*") && searchFor.endsWith("*")) {
            tempString = searchFor.substring(1);
            tempString = tempString.substring(0, tempString.length() - 1);
            if (ugName.contains(tempString)) {
                match = true;
            }
        }
        else if (searchFor.startsWith("*")) {
            tempString = searchFor.substring(1);
            if (ugName.endsWith(tempString)) {
                match = true;
            }
        }
        else if (searchFor.endsWith("*")) {
            tempString = searchFor.substring(0, searchFor.length() - 1);
            if (ugName.startsWith(tempString)) {
                match = true;
            }
        }
        else {
            if (ugName.equals(searchFor)) {
                match = true;
            }
        }
        return match;
    }

    public static String encodeToUTF8(String value) throws Exception {
        StringTokenizer st = new StringTokenizer(URLEncoder.encode(value, "UTF-8"),
        "+");
        StringBuilder buf = new StringBuilder();
        while (st.hasMoreTokens()) {
            buf.append(st.nextToken());
            if (st.hasMoreTokens()) {
                buf.append("%20");
            }
        }
        return buf.toString();
    }

    public static TimeZone getTimeZone(LoginToken token) throws Exception {
        return getTimeZone(RequestHandler.INSTANCE.getUserDetails(token));

    }

    public static TimeZone getTimeZone(UserDetails udt) {
        TimeZone tz = TimeZone.getDefault();
        if (udt != null) {
            if (udt.getUserPolicyProfile() != null) {
                if (udt.getUserPolicyProfile().getUserProfile() != null) {
                    if (udt.getUserPolicyProfile().getUserProfile().getPreferences() != null) {
                        String id = udt.getUserPolicyProfile().getUserProfile()
                        .getPreferences().getTimeZoneId();
                        if (id != null && !id.equals("")) {
                            tz = TimeZone.getTimeZone(id);
                        }
                    }
                }
            }
        }
        return tz;
    }

    public static long parseWebDateToMillis(Locale locale, TimeZone tz,
            String date) {
        return parseWebDateToMillis(locale, tz, date, false);
    }

    public static long parseWebDateToMillis(Locale locale, TimeZone tz,
            String date, boolean offset) {
        long timeInMillis = 0;

        SimpleDateFormat dateFormatter = new SimpleDateFormat(WEB_DATETIME_PARSE_FORMAT, locale);

        dateFormatter.setTimeZone(tz);
        dateFormatter.setLenient(true);
        String time = "0:00";
        if (offset) {
            time = "23:59";
        }
        Date d = dateFormatter.parse(date + " " + time, new ParsePosition(0));

        if (d == null) {
            log.error("Error parsing date (" + date + ")(" + time
                    + "), badly formatted");
        }
        else {
            timeInMillis = d.getTime();
        }

        return timeInMillis;
    }

    public static long parseWebDateToMillis(Locale locale, TimeZone tz, String date, String time) {
        long timeInMillis = 0;

        SimpleDateFormat dateFormatter = new SimpleDateFormat(WEB_DATETIME_PARSE_FORMAT, locale);
        if (tz != null) {
            dateFormatter.setTimeZone(tz);
        }
        Date d = dateFormatter.parse(date + " " + time, new ParsePosition(0));

        if (d == null) {
            log.error("Error parsing date (" + date + ")(" + time
                    + "), badly formatted");
        }
        else {
            timeInMillis = d.getTime();
        }

        return timeInMillis;
    }

    public static String subStringHighlight(String toHighlight, String fullOutput) {
        if (toHighlight != null) {
            String temp = toHighlight;

            if (toHighlight.equals("*")) {
                temp = "";
            }
            else if (toHighlight.startsWith("*") && toHighlight.endsWith("*")) {
                temp = toHighlight.substring(1);
                temp = temp.substring(0, temp.length() - 1);
            }
            else if (toHighlight.startsWith("*")) {
                temp = toHighlight.substring(1);
            }
            else if (toHighlight.endsWith("*")) {
                temp = toHighlight.substring(0, toHighlight.length() - 1);
            }

            if (temp.length() > 0) {
                return fullOutput.replaceFirst(temp, "<b>" + temp + "</b>");
            }
        }

        return fullOutput;
    }

    private static List<LabelValueBean> getTimeList() {

        List<LabelValueBean> timeStringList = new ArrayList<LabelValueBean>();
        timeStringList.add(new LabelValueBean("0:00", "0:00"));
        timeStringList.add(new LabelValueBean("0:30", "0:30"));
        timeStringList.add(new LabelValueBean("1:00", "1:00"));
        timeStringList.add(new LabelValueBean("1:30", "1:30"));
        timeStringList.add(new LabelValueBean("2:00", "2:00"));
        timeStringList.add(new LabelValueBean("2:30", "2:30"));
        timeStringList.add(new LabelValueBean("3:00", "3:00"));
        timeStringList.add(new LabelValueBean("3:30", "3:30"));

        timeStringList.add(new LabelValueBean("4:00", "4:00"));
        timeStringList.add(new LabelValueBean("4:30", "4:30"));
        timeStringList.add(new LabelValueBean("5:00", "5:00"));
        timeStringList.add(new LabelValueBean("5:30", "5:30"));
        timeStringList.add(new LabelValueBean("6:00", "6:00"));
        timeStringList.add(new LabelValueBean("6:30", "6:30"));

        timeStringList.add(new LabelValueBean("7:00", "7:00"));
        timeStringList.add(new LabelValueBean("7:30", "7:30"));
        timeStringList.add(new LabelValueBean("8:00", "8:00"));
        timeStringList.add(new LabelValueBean("8:30", "8:30"));
        timeStringList.add(new LabelValueBean("9:00", "9:00"));
        timeStringList.add(new LabelValueBean("9:30", "9:30"));

        timeStringList.add(new LabelValueBean("10:00", "10:00"));
        timeStringList.add(new LabelValueBean("10:30", "10:30"));
        timeStringList.add(new LabelValueBean("11:00", "11:00"));
        timeStringList.add(new LabelValueBean("11:30", "11:30"));

        timeStringList.add(new LabelValueBean("12:00", "12:00"));
        timeStringList.add(new LabelValueBean("12:30", "12:30"));
        timeStringList.add(new LabelValueBean("13:00", "13:00"));
        timeStringList.add(new LabelValueBean("13:30", "13:30"));
        timeStringList.add(new LabelValueBean("14:00", "14:00"));
        timeStringList.add(new LabelValueBean("14:30", "14:30"));
        timeStringList.add(new LabelValueBean("15:00", "15:00"));
        timeStringList.add(new LabelValueBean("15:30", "15:30"));

        timeStringList.add(new LabelValueBean("16:00", "16:00"));
        timeStringList.add(new LabelValueBean("16:30", "16:30"));
        timeStringList.add(new LabelValueBean("17:00", "17:00"));
        timeStringList.add(new LabelValueBean("17:30", "17:30"));
        timeStringList.add(new LabelValueBean("18:00", "18:00"));
        timeStringList.add(new LabelValueBean("18:30", "18:30"));

        timeStringList.add(new LabelValueBean("19:00", "19:00"));
        timeStringList.add(new LabelValueBean("19:30", "19:30"));
        timeStringList.add(new LabelValueBean("20:00", "20:00"));
        timeStringList.add(new LabelValueBean("20:30", "20:30"));
        timeStringList.add(new LabelValueBean("21:00", "21:00"));
        timeStringList.add(new LabelValueBean("21:30", "21:30"));

        timeStringList.add(new LabelValueBean("22:00", "22:00"));
        timeStringList.add(new LabelValueBean("22:30", "22:30"));
        timeStringList.add(new LabelValueBean("23:00", "23:00"));
        timeStringList.add(new LabelValueBean("23:30", "23:30"));
        return timeStringList;
    }


    public List<LabelValueBean> getTimeStringList() {
        if (timeStringList == null) {
            timeStringList = getTimeList();
        }

        return timeStringList;
    }
}
