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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TimeFieldEditor extends BasicComboBoxEditor implements
    BoundedFieldKeyEventHandlerI {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static final int MAX_HOUR_SIZE = 12;
	private static final int MAX_MINUTE_SIZE = 59;
	private static final int MAX_SECOND_SIZE = 59;
	private static final int MAX_AMPM_SIZE = 2;
	private final JPanel timeField = new JPanel(new FlowLayout(FlowLayout.LEFT,
	    0, 0));
	private final BoundedTextField hourField;
	private final BoundedTextField minuteField;
	private final BoundedTextField secondField;
	private final BoundedTextField amPmField;
	private final EmptyBorder border = new EmptyBorder(0, 0, 0, 0);
	private TimeZone timeZone = TimeZone.getDefault();
	private Calendar cal;

	/*
	 * @param increment - The number of minutes between times
	 */
	public TimeFieldEditor(final Calendar calendar, final JComboBox box) {
		cal = calendar;
		hourField = new BoundedTextField(2, MAX_HOUR_SIZE, this);
		minuteField = new BoundedTextField(2, MAX_MINUTE_SIZE, this);
		secondField = new BoundedTextField(2, MAX_SECOND_SIZE, this);
		amPmField = new BoundedTextField(2, MAX_AMPM_SIZE, this);

		log.debug("Hour: " + cal.get(Calendar.HOUR) + " Minute: "
		    + cal.get(Calendar.MINUTE) + " AMPM: " + cal.get(Calendar.AM_PM));
		setFields();

		hourField.setHorizontalAlignment(SwingConstants.CENTER);
		minuteField.setHorizontalAlignment(SwingConstants.CENTER);
		secondField.setHorizontalAlignment(SwingConstants.CENTER);
		hourField.setBorder(new EmptyBorder(0, 0, 0, 0));
		minuteField.setBorder(new EmptyBorder(0, 0, 0, 0));
		secondField.setBorder(new EmptyBorder(0, 0, 0, 0));
		hourField.setMargin(new Insets(0, 0, 0, 0));
		minuteField.setMargin(new Insets(0, 0, 0, 0));
		secondField.setMargin(new Insets(0, 0, 0, 0));

		buildPanel();

		timeField.setBackground(Color.white);
		timeField.setBorder(new EmptyBorder(0, 0, 0, 0));

		box.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				if (ie.getStateChange() == ItemEvent.SELECTED) {
					String selectedItem = (String) box.getSelectedItem();
					String parts[] = selectedItem.split(":");
					String ampmParts[];
					String ampm = null;
					String hour = null;
					String minute = null;
					if (parts.length == 2) {
						hour = parts[0];
						ampmParts = parts[1].split(" ");
						if (ampmParts.length == 2) {
							minute = ampmParts[0];
							ampm = ampmParts[1];
						}
					}
					log.debug("Action Listener: Hour: " + hour + " minute: " + minute
					    + " ampm: " + ampm);
					int h = Integer.parseInt(hour);
					if (h == 12) {
						h = 0;
					}
					cal.set(Calendar.HOUR, h);
					cal.set(Calendar.MINUTE, Integer.parseInt(minute));
					cal.set(Calendar.AM_PM, "AM".equals(ampm) ? Calendar.AM : Calendar.PM);
					cal.getTime(); // Force recalculation of calendar fields
					
					setFields();
				}
			}
		});

	}

	@Override
	public Component getEditorComponent() {
		
		return timeField;
	}

	@Override
	public void handleBTFKeyEvent(int event) {
		updateCalendar();
	}

	private void buildPanel() {
		BoundedTextField[] fields = getLocalizedFieldsOrder();
		timeField.add(fields[0]);
		timeField.add(getSeparator());
		timeField.add(fields[1]);
		if (fields[2].equals(amPmField)) {
			timeField.add(new JLabel(" "));
		}
		else {
			timeField.add(getSeparator());
		}
		timeField.add(fields[2]);
		
	}

	private BoundedTextField getField(String pattern) {
		BoundedTextField field = null;
		if (pattern.charAt(0) == 'm') {
			
			field = minuteField;
		}
		else if (pattern.charAt(0) == 'h') {
			
			field = hourField;
		}
		else if (pattern.charAt(0) == 's') {
			
			field = secondField;
		}
		else if (pattern.charAt(0) == 'a') {
			
			field = amPmField;
		}
		else {
			
		}
		return field;
	}

	private BoundedTextField[] getLocalizedFieldsOrder() {
		BoundedTextField[] order = new BoundedTextField[] { getField("h"),
		    getField("m"), getField("s") };
		DateFormat df = DateFormat.getInstance();
		df.setTimeZone(getTimeZone());
		SimpleDateFormat sdf = (SimpleDateFormat) df;
		String parts[] = null;
		String pattern[] = sdf.toPattern().split(" ");

		

		// Pattern should be something like:
		// M/d/yy h:mm a
		if (pattern.length > 2) {
			if (pattern[1].indexOf(':') != -1) {
				parts = pattern[1].split(":");
			}
			else if (pattern[1].indexOf('-') != -1) {
				parts = pattern[1].split("-");
			}
			else if (pattern[1].indexOf('.') != -1) {
				parts = pattern[1].split(".");
			}

			if (parts != null && parts.length >= 3) {
				
				order[0] = getField(parts[0]);
				order[1] = getField(parts[1]);
				order[2] = getField(parts[2]);
			}
			else if (parts != null && parts.length == 2 && pattern.length >= 3) {
				order[0] = getField(parts[0]);
				order[1] = getField(parts[1]);
				if ("a".equalsIgnoreCase(pattern[2].trim())) {
					order[2] = getField(pattern[2].trim());
				}
				else {
					
				}
			}
			else {
				
				// Use default
				order[0] = getField("h");
				order[1] = getField("m");
				order[2] = getField("s");
			}
		}

		return order;
	}

	private JLabel getSeparator() {
		JLabel label = null;
		DateFormat df = DateFormat.getInstance();
		df.setTimeZone(getTimeZone());
		SimpleDateFormat sdf = (SimpleDateFormat) df;
		String[] pattern;

		// Date format pattern should be of the form:
		// M/d/yy h:mm a

		

		pattern = sdf.toPattern().split(" ");

		if (pattern.length == 3) { // Ensure we have 3 parts otherwise use default
			                         // We will support :, ., - and h time separators
			                         // for now
			if (pattern[1].indexOf(':') > 0) {
				label = new JLabel(":");
			}
			else if (pattern[1].indexOf('.') > 0) {
				label = new JLabel(".");
			}
			else if (pattern[1].indexOf('-') > 0) {
				label = new JLabel("-");
			}
			else if (pattern[1].indexOf('h') > 0) {
				label = new JLabel("h");
			}
			else {
				log.warn("No time separator detected... using default of colon (:)");
				label = new JLabel(":");
			}
		}
		else {
			label = new JLabel(":");
		}

		label.setBorder(border);

		
		return label;
	}

	private TimeZone getTimeZone() {
		return timeZone;
	}

	private void setFields() {
		String hour = "";
		String minute = "";
		String second = "";

		if (cal.get(Calendar.HOUR) < 10) {
			hour += "0";
		}

		if (cal.get(Calendar.MINUTE) < 10) {
			minute += "0";
		}

		if (cal.get(Calendar.SECOND) < 10) {
			second += "0";
		}

		if (cal.get(Calendar.HOUR) == 0) {
			hour = "12";
		}
		else {
			hour += cal.get(Calendar.HOUR);
		}

		minute += cal.get(Calendar.MINUTE);
		second += cal.get(Calendar.SECOND);

		if (cal.get(Calendar.AM_PM) == Calendar.AM) {
			amPmField.setText("AM");
		}
		else {
			amPmField.setText("PM");
		}

		log.debug("Set fields h:" + hour + " m:" + minute + " s:" + second + " a/p:"
		    + amPmField.getText());
		hourField.setText(hour);
		minuteField.setText(minute);
		secondField.setText(second);

	}

	private void updateCalendar() {

		log.debug("Updated calendar with " + hourField.getText() + " "
		    + minuteField.getText() + " " + secondField.getText() + " "
		    + amPmField.getText());
		if (!"".equals(hourField.getText())) {
			cal.set(Calendar.HOUR, Integer.parseInt(hourField.getText()));
		}

		if (!"".equals(minuteField.getText())) {
			cal.set(Calendar.MINUTE, Integer.parseInt(minuteField.getText()));
		}

		if (!"".equals(secondField.getText())) {
			cal.set(Calendar.SECOND, Integer.parseInt(secondField.getText()));
		}

		if (!"".equals(amPmField.getText())) {
			
			cal.set(Calendar.AM_PM, amPmField.getText().equals("AM") ? Calendar.AM
			    : Calendar.PM);
		}
		
	}
}
