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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComboBoxUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CalendarPanel extends JPanel {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static final long serialVersionUID = 1L;
	private JButton popupAbsConvMenuButton;
	private JButton popupRelConvMenuButton;
	private JSpinner year;
	private JSpinner month;
	private JSpinner hour;
	private JSpinner minute;
	private JSpinner second;
	private JSpinner ampm;
	private JLabel timeZone;
	private final java.util.List<ActionListener> listeners = new ArrayList<ActionListener>();
	private DayOfMonthGrid grid;
	private static java.util.List<String> monthLabels;
	private static java.util.List<String> ampmLabels;
	private boolean showAMPM;
	private SimpleDateFormat zoneFormatter;
	private SimpleDateFormat toolTipZoneFormatter;
	private JSpinner offset;
	private JRadioButton minuteUnits;
	private JRadioButton hourUnits;
	private JRadioButton dayUnits;
	private JRadioButton monthUnits;
	private JRadioButton yearUnits;
	private JRadioButton agoDirection;
	private JRadioButton aheadDirection;
	private CalendarComboBoxModel model;

	public CalendarPanel(final CalendarComboBoxModel theModel, ComboBoxUI ui,
	    boolean showTime, boolean doShowAMPM, boolean doShowRelative, Font theFont) {
		popupAbsConvMenuButton = null;
		popupRelConvMenuButton = null;
		year = null;
		month = null;
		hour = null;
		minute = null;
		second = null;
		ampm = null;
		timeZone = null;

		grid = null;
		this.showAMPM = false;
		zoneFormatter = new SimpleDateFormat("zzzz");
		toolTipZoneFormatter = new SimpleDateFormat("z");
		offset = null;
		minuteUnits = null;
		hourUnits = null;
		dayUnits = null;
		monthUnits = null;
		yearUnits = null;
		agoDirection = null;
		aheadDirection = null;

		this.showAMPM = doShowAMPM;
		model = theModel;

		setFont(theFont == null ? getFont() : theFont);
		Date date = theModel.getSelection().getTime();
		configureDateComponents(date);
		configureTimeComponents();
		configureRelativeComponents(date);
		JPanel north = new JPanel();
		north.setLayout(new FlowLayout(0));
		north.add(popupAbsConvMenuButton);
		north.add(month);
		north.add(year);
		JPanel northRel = new JPanel(new BorderLayout());
		JPanel northRelButton = new JPanel(new FlowLayout(0));
		northRelButton.add(popupRelConvMenuButton);
		northRelButton.add(offset);
		// int rows = 5;
		JPanel westRelRadio = new JPanel(new GridLayout(5, 1));
		westRelRadio.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		westRelRadio.add(minuteUnits);
		westRelRadio.add(hourUnits);
		westRelRadio.add(dayUnits);
		westRelRadio.add(monthUnits);
		westRelRadio.add(yearUnits);
		JPanel southRelRadio = new JPanel(new FlowLayout(0));
		southRelRadio.add(agoDirection);
		southRelRadio.add(aheadDirection);
		northRel.add(northRelButton, "North");
		northRel.add(westRelRadio, "West");
		northRel.add(southRelRadio, "South");
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new GridLayout(1, 5, 0, 0));
		timePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		timePanel.add(new JLabel("Time:"));
		timePanel.add(hour);
		timePanel.add(minute);
		timePanel.add(second);
		if (doShowAMPM) {
			timePanel.add(ampm);
		}
		JPanel timeZonePanel = new JPanel(new BorderLayout());
		timeZonePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		timeZonePanel.add(timeZone, "Center");
		JPanel absolutePanel = new JPanel(new BorderLayout());
		JPanel relativePanel = new JPanel(new BorderLayout());
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Absolute", absolutePanel);
		tabbedPane.addTab("Relative", relativePanel);
		relativePanel.add(northRel, "North");
		if (theModel.getRelativeDate() != null) {
			tabbedPane.setSelectedIndex(1);
		}
		if (doShowRelative) {
			setLayout(new BorderLayout());
			add(tabbedPane, "Center");
		}
		else {
			absolutePanel = this;
		}
		absolutePanel.setLayout(new BorderLayout());
		absolutePanel.add(north, "North");
		absolutePanel.add(grid, "Center");
		if (showTime) {
			JPanel timeAndZone = new JPanel(new BorderLayout());
			timeAndZone.add(timePanel, "North");
			timeZonePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			timeAndZone.add(timeZonePanel, "South");
			absolutePanel.add(timeAndZone, "South");
		}
		else {
			absolutePanel.add(timeZonePanel, "South");
		}
		tabbedPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (tabbedPane.getSelectedIndex() == 0) {
					theModel.setSelection(theModel.getSelection());
				}
				else {
					setRelativeModelFromGUI();
				}
			}

		});
		theModel.addObserver(new CalendarComboBoxModelObserverI() {

			@Override
			public void update(Date newSelection) {
				setAbsoluteGUIFromModel();
				setRelativeGUIFromModel();
				if (theModel.getRelativeDate() == null) {
					tabbedPane.setSelectedIndex(0);
				}
				else {
					tabbedPane.setSelectedIndex(1);
				}
			}

		});
	}

	public CalendarPanel(CalendarComboBoxModel theModel, ComboBoxUI theUi,
	    Date theDate) {
		this(theModel, theUi, true, false, false, null);
	}

	private static void initializeSymbols() {
		DateFormatSymbols symbols = new DateFormatSymbols();
		String months[] = symbols.getMonths();
		monthLabels = new ArrayList<String>();
		for (String month2 : months) {
			if (month2 != null && month2.length() > 0) {
				monthLabels.add(month2);
			}
		}

		String ampm[] = symbols.getAmPmStrings();
		ampmLabels = new ArrayList<String>();
		ampmLabels.add(ampm[0]);
		ampmLabels.add(ampm[1]);
	}

	public void addActionListener(ActionListener listener) {

		listeners.add(listener);
	}

	public Calendar getSelection() {
		return model.getSelection();
	}

	private void configureDateComponents(Date date) {
		grid = new DayOfMonthGrid(model, date, getFont());
		if (monthLabels == null) {
			initializeSymbols();
		}
		month = new JSpinner(new RolloverSpinnerListModel(monthLabels));
		month.setEditor(new javax.swing.JSpinner.ListEditor(month));
		((javax.swing.JSpinner.DefaultEditor) month.getEditor()).getTextField()
		    .setFocusable(false);
		month.setValue(monthLabels.get(model.getSelection().get(2)));
		month.setPreferredSize(new Dimension(getMaxMonthWidth(), month
		    .getPreferredSize().height));
		month.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Calendar newSelection = model.getSelection();
				int day = newSelection.get(5);
				newSelection.set(5, 1);
				newSelection.set(2,
				    CalendarPanel.monthLabels.indexOf(month.getValue()));
				int daysInNewMonth = DayOfMonthGrid.getDaysInMonth(newSelection);
				if (daysInNewMonth < day) {
					newSelection.set(5, daysInNewMonth);
				}
				else {
					newSelection.set(5, day);
				}
				model.setSelection(newSelection);
				grid.setDate(newSelection.getTime());
			}

		});
		year = new JSpinner(new RolloverSpinnerNumberModel(model.getSelection()
		    .get(1), model.getMinYear(), model.getMaxYear(), 1));
		year.setEditor(new javax.swing.JSpinner.NumberEditor(year, "####"));
		((javax.swing.JSpinner.DefaultEditor) year.getEditor()).getTextField()
		    .setFocusable(false);
		year.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Calendar newSelection = model.getSelection();
				int day = newSelection.get(5);
				newSelection.set(5, 1);
				newSelection.set(1, ((Integer) year.getValue()).intValue());
				int daysInNewMonth = DayOfMonthGrid.getDaysInMonth(newSelection);
				if (daysInNewMonth < day) {
					newSelection.set(5, daysInNewMonth);
				}
				else {
					newSelection.set(5, day);
				}
				model.setSelection(newSelection);
				grid.setDate(newSelection.getTime());
			}

		});
		popupAbsConvMenuButton = createConvButton();
	}

	private void configureRelativeComponents(Date date) {
		ChangeListener relativeChangeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setRelativeModelFromGUI();
			}

		};
		ActionListener relativeActionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setRelativeModelFromGUI();
			}

		};
		offset = new JSpinner(new RolloverSpinnerNumberModel(1, 0, 1000, 1));
		offset.setEditor(new javax.swing.JSpinner.NumberEditor(offset, "0"));
		((javax.swing.JSpinner.NumberEditor) offset.getEditor()).getTextField()
		    .setFocusable(false);
		minuteUnits = new JRadioButton("Minutes");
		hourUnits = new JRadioButton("Hours");
		dayUnits = new JRadioButton("Days (1 Day = 24 Hours)");
		monthUnits = new JRadioButton("Months (1 Month = 31 Days)");
		yearUnits = new JRadioButton("Years (1 Year = 365 Days)");
		minuteUnits.setFocusable(false);
		hourUnits.setFocusable(false);
		dayUnits.setFocusable(false);
		monthUnits.setFocusable(false);
		yearUnits.setFocusable(false);
		ButtonGroup unitsGroup = new ButtonGroup();
		unitsGroup.add(minuteUnits);
		unitsGroup.add(hourUnits);
		unitsGroup.add(dayUnits);
		unitsGroup.add(monthUnits);
		unitsGroup.add(yearUnits);
		minuteUnits.setSelected(true);
		agoDirection = new JRadioButton("Ago");
		aheadDirection = new JRadioButton("Ahead");
		agoDirection.setFocusable(false);
		aheadDirection.setFocusable(false);
		ButtonGroup directionGroup = new ButtonGroup();
		directionGroup.add(agoDirection);
		directionGroup.add(aheadDirection);
		popupRelConvMenuButton = createConvButton();
		setRelativeGUIFromModel();
		offset.addChangeListener(relativeChangeListener);
		minuteUnits.addActionListener(relativeActionListener);
		hourUnits.addActionListener(relativeActionListener);
		dayUnits.addActionListener(relativeActionListener);
		monthUnits.addActionListener(relativeActionListener);
		yearUnits.addActionListener(relativeActionListener);
		agoDirection.addActionListener(relativeActionListener);
		aheadDirection.addActionListener(relativeActionListener);
	}

	private void configureTimeComponents() {
		if (showAMPM) {
			int hourNumber = model.getSelection().get(10);
			if (hourNumber == 0) {
				hourNumber = 12;
			}
			hour = new JSpinner(new RolloverSpinnerNumberModel(hourNumber, 1, 12, 1));
		}
		else {
			int hourNumber = model.getSelection().get(11);
			hour = new JSpinner(new RolloverSpinnerNumberModel(hourNumber, 0, 23, 1));
		}
		javax.swing.JSpinner.NumberEditor numberEditor = new javax.swing.JSpinner.NumberEditor(
		    hour, "00");
		numberEditor.getTextField().setFocusable(false);
		hour.setEditor(numberEditor);
		hour.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Calendar newSelection = model.getSelection();
				int hours;
				if (showAMPM) {
					hours = getHours() + getAMPMOffset();
					if (hours == 12) {
						hours = 0;
					}
					else if (hours == 24) {
						hours = 12;
					}
				}
				else {
					hours = getHours();
				}
				int oldHours = newSelection.get(11);
				int diff = hours - oldHours;
				boolean spinUp;
				if (diff == 1 || diff == -23 || diff == -11) {
					spinUp = true;
				}
				else if (diff == -1 || diff == 23 || diff == 11) {
					spinUp = false;
				}
				else {
					return;
				}
				model.rollHours(spinUp, showAMPM);
			}

		});
		minute = new JSpinner(new RolloverSpinnerNumberModel(model.getSelection()
		    .get(12), 0, 59, 1));
		numberEditor = new javax.swing.JSpinner.NumberEditor(minute, "00");
		numberEditor.getTextField().setFocusable(false);
		minute.setEditor(numberEditor);
		minute.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				model.setMinutes(((Integer) minute.getValue()).intValue());
			}

		});
		second = new JSpinner(new RolloverSpinnerNumberModel(model.getSelection()
		    .get(13), 0, 59, 1));
		numberEditor = new javax.swing.JSpinner.NumberEditor(second, "00");
		numberEditor.getTextField().setFocusable(false);
		second.setEditor(numberEditor);
		second.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				model.setSeconds(((Integer) second.getValue()).intValue());
			}

		});
		String ampmValue = ampmLabels.get(model.getSelection().get(9));
		ampm = new JSpinner(new RolloverSpinnerListModel(
		    new String[] { "AM", "PM" }));
		((javax.swing.JSpinner.DefaultEditor) ampm.getEditor()).getTextField()
		    .setFocusable(false);
		ampm.setValue(ampmValue);
		ampm.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int hours = getHours() + getAMPMOffset();
				if (hours == 12) {
					hours = 0;
				}
				else if (hours == 24) {
					hours = 12;
				}
				Calendar newSelection = model.getSelection();
				newSelection.set(11, hours);
				model.setSelection(newSelection);
			}

		});
		timeZone = new JLabel();
		Calendar newSelection = model.getSelection();
		zoneFormatter.setTimeZone(newSelection.getTimeZone());
		toolTipZoneFormatter.setTimeZone(newSelection.getTimeZone());
		timeZone.setText(zoneFormatter.format(newSelection.getTime()));
		timeZone
		    .setToolTipText(toolTipZoneFormatter.format(newSelection.getTime()));
	}

	private JButton createConvButton() {
		JButton convButton = null;

		URL u = getClass().getResource("/Arrow.gif");
		if (u == null) {
			log.error("Unable to load resource Arrow.gif, cannot decorate button!");
			convButton = new JButton("Arrow.gif");
		}
		else {
			convButton = new JButton(new ImageIcon(u));
		}

		convButton.setFocusable(false);
		convButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					/**
					 * We should really provide a way to remove the down arrrow.
					 * 
					 * @TODO We see exceptions from here when you press the down arrow
					 *       button on the calendar in DRAC. This appears to be because
					 *       DRAC uses this class directly instead of layering it inside a
					 *       ComponentComboPopupC and CalendarComboBoxC
					 */
					ComponentComboPopup popup = (ComponentComboPopup) getParent();
					popup.hideForConvMenu();

					((CalendarComboBox) popup.getComboBox()).popupConvMenu(true);

				}
				catch (Exception e) {
					log.error("CalendarPanelC down arrow button action callback ", e);
				}
			}

		});
		if (convButton.getIcon() != null) {
			convButton.setPreferredSize(new Dimension(convButton.getIcon()
			    .getIconWidth() - 1, month.getPreferredSize().height));
		}
		return convButton;
	}

	private int getAMPMOffset() {
		boolean am = ampm.getValue().equals(ampmLabels.get(0));
		return am ? 0 : 12;
	}

	private int getHours() {
		return ((Integer) hour.getValue()).intValue();

	}

	private int getMaxMonthWidth() {

		return 82;
	}

	private void setAbsoluteGUIFromModel() {
		Calendar selection = model.getSelection();
		Date newSelection = selection.getTime();
		month.setValue(monthLabels.get(selection.get(2)));
		year.setValue(Integer.valueOf(selection.get(1)));
		grid.setDate(newSelection);
		minute.setValue(Integer.valueOf(selection.get(12)));
		second.setValue(Integer.valueOf(selection.get(13)));
		int hourNumber = -1;
		if (showAMPM) {
			hourNumber = selection.get(10);
			if (hourNumber == 0) {
				hourNumber = 12;
			}
			ampm.setValue(ampmLabels.get(selection.get(9)));
		}
		else {
			hourNumber = selection.get(11);
		}
		hour.setValue(Integer.valueOf(hourNumber));
		zoneFormatter.setTimeZone(selection.getTimeZone());
		toolTipZoneFormatter.setTimeZone(selection.getTimeZone());
		timeZone.setText(zoneFormatter.format(newSelection));
		timeZone.setToolTipText(toolTipZoneFormatter.format(newSelection));
	}

	private void setRelativeGUIFromModel() {
		RelativeDate relDat = model.getRelativeDate();
		if (relDat == null) {
			return;
		}
		offset.setValue(Integer.valueOf(relDat.getOffset()));
		if (relDat.getAhead()) {
			aheadDirection.setSelected(true);
		}
		else {
			agoDirection.setSelected(true);
		}
		if (relDat.getUnits() == 0) {
			minuteUnits.setSelected(true);
		}
		else if (relDat.getUnits() == 1) {
			hourUnits.setSelected(true);
		}
		else if (relDat.getUnits() == 2) {
			dayUnits.setSelected(true);
		}
		else if (relDat.getUnits() == 3) {
			monthUnits.setSelected(true);
		}
		else {
			yearUnits.setSelected(true);
		}
	}

	private void setRelativeModelFromGUI() {
		RelativeDate relDat = new RelativeDate();
		relDat.setOffset(((Integer) offset.getValue()).intValue());
		relDat.setAhead(aheadDirection.isSelected());
		if (minuteUnits.isSelected()) {
			relDat.setUnits(0);
		}
		else if (hourUnits.isSelected()) {
			relDat.setUnits(1);
		}
		else if (dayUnits.isSelected()) {
			relDat.setUnits(2);
		}
		else if (monthUnits.isSelected()) {
			relDat.setUnits(3);
		}
		else {
			relDat.setUnits(4);
		}
		model.setRelativeDate(relDat);
	}

}
