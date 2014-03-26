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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.CalendarComboBoxModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.ComponentComboPopupObserver;

public final class DateDropDown extends JComboBox implements
    ComponentComboPopupObserver {
	private static final long serialVersionUID = -6931497077538240185L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private static final int MAX_YEAR = 2099;
	private final Calendar cal;
	// back only 10 years
	private final CalendarComboBoxModel model;
	private final DateFieldEditor editor;
	private TimeZone timeZone;

	public DateDropDown(TimeZone preferredTimeZone, Locale locale) {
		cal = Calendar.getInstance(preferredTimeZone, locale);
		model = new CalendarComboBoxModel(cal.getTime(), preferredTimeZone,
		    cal.get(Calendar.YEAR) - 10, MAX_YEAR);
		editor = new DateFieldEditor(model);
		this.timeZone = preferredTimeZone;
		editor.setTimeZone(timeZone);

		setEditor(editor);
		setModel(model);
		setUI(new DateComboBoxUI(this));
		setBorder(new BevelBorder(BevelBorder.LOWERED, Color.white,
		    Color.lightGray, Color.darkGray, Color.darkGray));
		setBackground(Color.white);
	}

	/**
	 * @see com.nortel.ComponentComboPopupObserver.calendarcombobox.ComponentComboPopupObserverI#comboPopupChanged()
	 */
	@Override
	public void comboPopupChanged() {
		fireActionEvent();
		log.debug("Detected comboPopupChanged event");
		editor.setCalendar(model.getSelection());
	}

	public Date getDate() {
		return model.getSelection().getTime();
	}

	public long getTimeInMillis() {
		return model.getSelection().getTimeInMillis();
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setComponentEnabled(boolean enabled) {
		this.setEnabled(enabled);
		this.setEditable(enabled);

		BasicComboBoxEditor editor1 = (BasicComboBoxEditor) this.getEditor();
		editor1.getEditorComponent().setForeground(
		    UIManager.getColor("TextField.foreground"));

		if (enabled) {
			editor1.getEditorComponent().setBackground(
			    UIManager.getColor("TextField.background"));

			if (UIManager.getLookAndFeel().getID().toUpperCase().startsWith("MOTIF")) {
				editor1.getEditorComponent().setBackground(Color.WHITE);
			}
		}
		else {
			editor1.getEditorComponent().setBackground(
			    UIManager.getColor("Panel.background"));
		}
	}

	public void setDate(Date date) {
		model.getSelection().setTime(date);
	}

}
