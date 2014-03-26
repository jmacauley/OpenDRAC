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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.CalendarComboBoxModel;

public final class DateFieldEditor extends BasicComboBoxEditor implements
    BoundedFieldKeyEventHandlerI {
  
	private static final int MAX_DAY_SIZE = 2;
	private static final int MAX_MONTH_SIZE = 2;
	private static final int MAX_YEAR_SIZE = 4;
	private final JPanel timeField = new JPanel(new FlowLayout(FlowLayout.LEFT,
	    0, 0));
	private final BoundedTextField dayField;
	private final BoundedTextField monthField;
	private final BoundedTextField yearField;
	private final EmptyBorder border = new EmptyBorder(0, 0, 0, 0);
	private final CalendarComboBoxModel model;
	private TimeZone timeZone = TimeZone.getDefault();

	public DateFieldEditor(CalendarComboBoxModel mod) {
		model = mod;
		dayField = new BoundedTextField(MAX_DAY_SIZE, 31, this);
		monthField = new BoundedTextField(MAX_MONTH_SIZE, 12, this);
		yearField = new BoundedTextField(MAX_YEAR_SIZE, 2099, this);

		dayField.setText(Integer.toString(model.getSelection().get(
		    Calendar.DAY_OF_MONTH)));
		monthField.setText(Integer.toString(model.getSelection()
		    .get(Calendar.MONTH) + 1));
		yearField
		    .setText(Integer.toString(model.getSelection().get(Calendar.YEAR)));

		dayField.setHorizontalAlignment(SwingConstants.CENTER);
		monthField.setHorizontalAlignment(SwingConstants.CENTER);
		yearField.setHorizontalAlignment(SwingConstants.CENTER);
		dayField.setBorder(new EmptyBorder(0, 0, 0, 0));
		monthField.setBorder(new EmptyBorder(0, 0, 0, 0));
		yearField.setBorder(new EmptyBorder(0, 0, 0, 0));
		dayField.setMargin(new Insets(0, 0, 0, 0));
		monthField.setMargin(new Insets(0, 0, 0, 0));
		yearField.setMargin(new Insets(0, 0, 0, 0));

		buildPanel();

		timeField.setBackground(Color.white);
	}

	@Override
	public Component getEditorComponent() {
		return timeField;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	@Override
	public void handleBTFKeyEvent(int event) {
		updateCalendar();
	}

	public void setCalendar(Calendar cal) {
		// this.cal = cal;
		dayField.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
		monthField.setText(Integer.toString(cal.get(Calendar.MONTH) + 1));
		yearField.setText(Integer.toString(cal.get(Calendar.YEAR)));
		model.setSelection(cal);
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	private void buildPanel() {
		BoundedTextField[] fields = getLocalizedFieldsOrder();
		timeField.add(fields[0]);
		timeField.add(getSeparator());
		timeField.add(fields[1]);
		timeField.add(getSeparator());
		timeField.add(fields[2]);
	}

	private BoundedTextField getField(String pattern) {

		BoundedTextField field = null;
		if (pattern.charAt(0) == 'M') {
			field = monthField;
		}
		else if (pattern.charAt(0) == 'd') {
			field = dayField;
		}
		else if (pattern.charAt(0) == 'y') {
			field = yearField;
		}

		return field;
	}

	private BoundedTextField[] getLocalizedFieldsOrder() {
		BoundedTextField[] order = new BoundedTextField[3];
		DateFormat df = DateFormat.getInstance();
		df.setTimeZone(getTimeZone());
		SimpleDateFormat sdf = (SimpleDateFormat) df;
		String parts[] = null;

		

		if (sdf.toPattern().indexOf("/") != -1) {
			parts = sdf.toPattern().split("/");
		}
		else if (sdf.toPattern().indexOf("-") != -1) {
			parts = sdf.toPattern().split("-");
		}
		else if (sdf.toPattern().indexOf(".") != -1) {
			parts = sdf.toPattern().split(".");
		}

		if (parts != null && parts.length >= 3) {
			
			order[0] = getField(parts[0]);
			order[1] = getField(parts[1]);
			order[2] = getField(parts[2]);
		}
		else {
			
			// Use default
			order[0] = getField("M");
			order[1] = getField("d");
			order[2] = getField("y");
		}

		return order;
	}

	private JLabel getSeparator() {
		JLabel label = null;
		DateFormat df = DateFormat.getInstance();
		df.setTimeZone(getTimeZone());
		SimpleDateFormat sdf = (SimpleDateFormat) df;

		

		if (sdf.toPattern().indexOf("/") != -1) {
			label = new JLabel("/");
		}
		else if (sdf.toPattern().indexOf("-") != -1) {
			label = new JLabel("-");
		}
		else if (sdf.toPattern().indexOf(".") != -1) {
			label = new JLabel(".");
		}

		label.setBorder(border);
		return label;
	}

	private void updateCalendar() {
		Calendar cal = model.getSelection();
		if (!"".equals(dayField.getText())) {
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayField.getText()));
		}

		if (!"".equals(monthField.getText())) {
			// Calendar month starts at 0
			cal.set(Calendar.MONTH, Integer.parseInt(monthField.getText()) - 1);
		}

		if (!"".equals(yearField.getText())) {
			cal.set(Calendar.YEAR, Integer.parseInt(yearField.getText()));
		}
		model.setSelection(cal);
	}

}
