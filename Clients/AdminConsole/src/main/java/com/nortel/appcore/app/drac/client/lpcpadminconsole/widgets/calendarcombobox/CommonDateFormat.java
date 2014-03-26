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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.SwingUtilities;

public final class CommonDateFormat {
	private static String toolTipPattern = "yyyy-MM-dd HH:mm:ss.SSS zzz";
	private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
	private static final TimeZone LOCAL_TIME_ZONE;
	private static TimeZone timeZone;
	private static List<WeakReference<AppCommonDateFormat>> apps = new ArrayList<WeakReference<AppCommonDateFormat>>();

	static {
		LOCAL_TIME_ZONE = TimeZone.getDefault();
		timeZone = LOCAL_TIME_ZONE;
	}

	private CommonDateFormat() {
		super();
	}

	public static synchronized String getCommonDateTimeFormat(Date date) {
		SimpleDateFormat commonDateTimeFormat = new SimpleDateFormat(
		    "yyyy-MM-dd HH:mm:ss");
		commonDateTimeFormat.setTimeZone(timeZone);
		return commonDateTimeFormat.format(date);
	}

	public static synchronized String getCommonToolTipFormat(Date date) {
		SimpleDateFormat toolTipFormat = new SimpleDateFormat(toolTipPattern);
		toolTipFormat.setTimeZone(timeZone);
		String result = toolTipFormat.format(date);
		StringBuilder sb = new StringBuilder(result);
		toolTipFormat.applyPattern("Z");
		if (UTC_TIME_ZONE.equals(timeZone)) {
			sb.append(" (Local");
			toolTipFormat.setTimeZone(LOCAL_TIME_ZONE);
			String offset = toolTipFormat.format(date);
			if (offset.charAt(0) == '+') {
				sb.append('-');
			}
			else {
				sb.append('+');
			}
			sb.append(offset.substring(1));
			sb.append(')');
		}
		else {
			sb.append(" (UTC");
			String offset = toolTipFormat.format(date);
			sb.append(offset);
			sb.append(')');
		}
		sb.insert(sb.length() - 3, ':');
		return sb.toString();
	}

	public static synchronized TimeZone getTimeZone() {
		return timeZone;
	}

	static synchronized void addAppCommonDateFormat(AppCommonDateFormat app) {
		apps.add(new WeakReference<AppCommonDateFormat>(app));
	}

	static void repaintGUI() {
		Runnable doit = new Runnable() {

			@Override
			public void run() {
				Frame frames[] = Frame.getFrames();
				if (frames == null) {
					return;
				}
				for (Frame frame : frames) {
					frame.repaint();
					Window windows[] = frame.getOwnedWindows();
					CommonDateFormat.repaintGUI(windows);
				}

			}

		};
		if (SwingUtilities.isEventDispatchThread()) {
			doit.run();
		}
		else {
			EventQueue.invokeLater(doit);
		}
	}

	static synchronized void setTimeZoneForApp(TimeZone tz) {
		timeZone = tz;
	}

	private static void repaintGUI(Window windows[]) {
		if (windows == null) {
			return;
		}
		for (Window window : windows) {
			window.repaint();
			Window w2[] = window.getOwnedWindows();
			repaintGUI(w2);
		}

	}

}