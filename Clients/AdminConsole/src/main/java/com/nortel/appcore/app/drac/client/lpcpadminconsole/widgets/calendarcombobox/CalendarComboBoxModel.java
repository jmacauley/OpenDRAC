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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.DefaultComboBoxModel;

public final class CalendarComboBoxModel extends DefaultComboBoxModel {
	private static final long serialVersionUID = 1L;
	private Calendar selection;
	private RelativeDate relativeDate;
	private final int minYear;
	private final int maxYear;
	private final List<CalendarComboBoxModelObserverI> observers = new ArrayList<CalendarComboBoxModelObserverI>();
	private boolean updatingObservers;

	public CalendarComboBoxModel(Date date, TimeZone tz, int theMinYear,
	    int theMaxYear) {
		relativeDate = null;
		updatingObservers = false;
		selection = Calendar.getInstance();
		selection.setTimeZone(tz);
		selection.setTime(date);
		if (date instanceof RelativeDate) {
			relativeDate = (RelativeDate) date;
		}
		if (selection.get(14) != 999) {
			selection.set(14, 0);
		}
		this.minYear = theMinYear;
		this.maxYear = theMaxYear;

	}

	public CalendarComboBoxModel(int theMinYear, int theMaxYear) {
		this(new Date(), CommonDateFormat.getTimeZone(), theMinYear, theMaxYear);
	}

	public void addObserver(CalendarComboBoxModelObserverI observer) {
		observers.add(observer);
	}

	public int getMaxYear() {
		return maxYear;
	}

	public int getMinYear() {
		return minYear;
	}

	public RelativeDate getRelativeDate() {
		return relativeDate;
	}

	@Override
	public Object getSelectedItem() {
		if (relativeDate != null) {
			relativeDate.evaluate();
			return relativeDate;
		}
		return selection.getTime();
	}

	public Calendar getSelection() {
		return (Calendar) selection.clone();
	}

	public TimeZone getTimeZone() {
		return selection.getTimeZone();
	}

	public void rollHours(boolean rollUp, boolean showingAMPM) {
		Calendar currentSelection = getSelection();
		boolean rolled = rollHoursBounded(currentSelection, rollUp, showingAMPM);
		if (!rolled) {
			rollHoursToBoundary(!rollUp, showingAMPM);
			return;
		}
		setSelection(currentSelection);
	}

	public void rollHoursToBoundary(boolean rollUp, boolean showingAMPM) {
		Calendar currentSelection = getSelection();
		for (boolean rolled = true; rolled; rolled = rollHoursBounded(
		    currentSelection, rollUp, showingAMPM)) {
			// do nothing
		}
		setSelection(currentSelection);
	}

	public void setMillis(int millis) {
		Calendar currentSelection = getSelection();
		int oldMillis = currentSelection.get(14);
		if (millis != 999) {
			millis = 0;
		}
		if (millis == oldMillis) {
			return;
		}
		currentSelection.setTimeInMillis(currentSelection.getTimeInMillis()
		    + millis - oldMillis);
		setSelection(currentSelection);
	}

	public void setMinutes(int minutes) {
		Calendar currentSelection = getSelection();
		int oldMinutes = currentSelection.get(12);
		int offset = (minutes - oldMinutes) * 60 * 1000;
		if (offset == 0) {
			return;
		}
		currentSelection.setTimeInMillis(currentSelection.getTimeInMillis()
		    + offset);
		setSelection(currentSelection);
	}

	public void setRelativeDate(RelativeDate relativeDate) {
		if (updatingObservers) {
			return;
		}
		relativeDate.evaluate();
		this.relativeDate = relativeDate;
		selection.setTime(relativeDate);
		updateObservers();
	}

	public void setSeconds(int seconds) {
		Calendar currentSelection = getSelection();
		int oldSeconds = currentSelection.get(13);
		int offset = (seconds - oldSeconds) * 1000;
		if (offset == 0) {
			return;
		}
		currentSelection.setTimeInMillis(currentSelection.getTimeInMillis()
		    + offset);
		setSelection(currentSelection);
	}

	public void setSelection(Calendar selection) {
		if (updatingObservers) {
			return;
		}
		selection.setTimeZone(this.selection.getTimeZone());
		this.selection = selection;
		relativeDate = null;
		if (selection.get(14) == 999) {
			setMillis(0);
		}
		updateObservers();
	}

	void setTimeZone(TimeZone tz) {
		if (updatingObservers) {
			return;
		}
		if (!selection.getTimeZone().equals(tz)) {
			selection.setTimeZone(tz);
			updateObservers();
		}
	}

	private boolean rollHoursBounded(Calendar cal, boolean rollUp,
	    boolean showingAMPM) {
		int currentDay = cal.get(5);
		int currentAMPM = cal.get(9);
		int offset = 3600000;
		if (!rollUp) {
			offset = -offset;
		}
		cal.setTimeInMillis(cal.getTimeInMillis() + offset);
		int newDay = cal.get(5);
		int newAMPM = cal.get(9);
		if (showingAMPM && newAMPM != currentAMPM || !showingAMPM
		    && newDay != currentDay) {
			cal.setTimeInMillis(cal.getTimeInMillis() - offset);
			return false;
		}
		return true;
	}

	private void updateObservers() {
		Date eventDate = selection.getTime();
		if (getRelativeDate() != null) {
			eventDate = getRelativeDate();
		}
		try {
			updatingObservers = true;
			for (CalendarComboBoxModelObserverI c : observers) {
				c.update(eventDate);
			}
		}
		finally {
			updatingObservers = false;
		}
	}
}
