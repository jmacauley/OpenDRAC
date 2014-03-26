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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.border.BevelBorder;

public final class TimeDropDown extends JComboBox {
	private static final long serialVersionUID = 1085568053855349510L;

	private final Calendar cal;
	private TimeZone timeZone;
	private Locale locale;
	private final TimeFieldEditor editor;
	private final int increment;
	private final DateFormat df = DateFormat.getInstance();
	private final Font baseFont = new Font("SanSerif", Font.PLAIN, 10);
	private ItemListener itemListeners[];

	public TimeDropDown(int timeIncrement, TimeZone preferredTimeZone,
	    Locale locale) {
		this.timeZone = preferredTimeZone;
		this.locale = locale;
		cal = Calendar.getInstance(timeZone, locale);

		editor = new TimeFieldEditor(cal, this);

		increment = timeIncrement;
		setEditor(editor);
		setModel(new TimeComboBoxModel());
		setFont(baseFont);
		setEditable(true);
		setBorder(new BevelBorder(BevelBorder.LOWERED, Color.white,
		    Color.lightGray, Color.darkGray, Color.darkGray));
		df.setTimeZone(getTimeZone());
		init();
	}

	@Override
	public Locale getLocale() {
		if (locale == null) {
			locale = Locale.getDefault();
		}

		return locale;

	}

	public Date getTime() {
		return cal.getTime();
	}

	public void setTime(Date date) {
		cal.setTime(date);
	}

	private TimeZone getTimeZone() {
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		return timeZone;
	}

	private void init() {
		removeAllItems();
		suspendItemListeners();

		Calendar currentCalendar = new GregorianCalendar(getTimeZone());
		// In order to set the menu to the current time (next increment)
		int currentHour = currentCalendar.get(Calendar.HOUR);
		int currentMin = currentCalendar.get(Calendar.MINUTE);
		if (currentMin % increment == 0) {
			currentMin += 1;
		}
		currentMin = (int) Math.ceil(currentMin / (increment * 1.0)) * increment;
		int currentAMPM = currentCalendar.get(Calendar.AM_PM);

		// Construct a list of items
		Calendar spinner = Calendar.getInstance();
		spinner.set(Calendar.AM_PM, Calendar.AM);
		spinner.set(Calendar.HOUR, 0);
		spinner.set(Calendar.MINUTE, 0);
		spinner.set(Calendar.SECOND, 0);
		int thisDay = spinner.get(Calendar.DAY_OF_WEEK);
		while (spinner.get(Calendar.DAY_OF_WEEK) <= thisDay) {
			Date date = spinner.getTime();
			String menuItem = DateFormat.getTimeInstance(DateFormat.SHORT).format(
			    date);
			int hour = spinner.get(Calendar.HOUR);
			int min = spinner.get(Calendar.MINUTE);
			int ampm = spinner.get(Calendar.AM_PM);

			if (currentHour == hour && currentMin == min && currentAMPM == ampm) {
				resumeItemListeners();
				setSelectedItem(menuItem);
				suspendItemListeners();
			}

			addItem(menuItem);
			spinner.add(Calendar.MINUTE, increment);
		}

		resumeItemListeners();
	}

	private void resumeItemListeners() {
		if (itemListeners != null) {
			for (ItemListener itemListener : itemListeners) {
				addItemListener(itemListener);
			}
			itemListeners = null;
		}
	}

	private void suspendItemListeners() {
		itemListeners = getItemListeners();
		for (ItemListener itemListener : itemListeners) {
			removeItemListener(itemListener);
		}
	}
}