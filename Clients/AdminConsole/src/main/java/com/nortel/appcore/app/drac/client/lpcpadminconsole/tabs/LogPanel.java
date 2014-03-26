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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.DateDropDown;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.DateTimeCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TrueFalseRenderer;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;

/**
 * @author pitman
 */
public final class LogPanel {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	private static final String FILTER_NONE = "None";
	private static final String FILTER_DATE = "Date";
	private static final int LOG_TABLE_TIME_COLUMN = 0; // the column we store the
	                                                    // time in.
	private static final int MORE_COLUMN = 10; // true/false column if details
	                                           // contains data.
	private static final int DETAILS_COL = 11; /*
																						  * the last and hidden column we
																						  * store the log details (map) in.
																						  */

	private final Vector<String> logColumns = new Vector<String>(
	    Arrays.asList(new String[] { "Time", "Originator", "IP Address",
	        "Billing Group", "Severity", "Category", "Log Type", "Result",
	        "Resource", "Description", "Details?", "-- HIDDEN --" }));

	private final TablePanel tablePanel = new TablePanel(new DefaultTableModel(
	    new Vector<Vector<String>>(), logColumns), null, null);

	private final Vector<String> logDetailsColumns = new Vector<String>(
	    Arrays.asList(new String[] { "Attribute", "Value" }));

	private final OpenDracDesktop desktop;

	public LogPanel(OpenDracDesktop d) {
		desktop = d;
	}

	public JPanel buildEventLogPanel() {
		JPanel southPanel = new JPanel(new BorderLayout(1, 1));
		JPanel seastPanel = new JPanel();
		JPanel centerPanel = new JPanel();

		final JPanel cardsPanel = new JPanel();
		cardsPanel.setLayout(new CardLayout());

		JLabel startDateLabel = new JLabel("Start date:");
		JLabel endDateLabel = new JLabel("End date:");

		final DateDropDown startDateBox = new DateDropDown(
		    desktop.getTimeZonePreference(), desktop.getLocale());
		startDateBox.setPreferredSize(new Dimension(110, 21));
		startDateBox.setEditable(true);

		final DateDropDown endDateBox = new DateDropDown(
		    desktop.getTimeZonePreference(), desktop.getLocale());
		endDateBox.setPreferredSize(new Dimension(110, 21));
		endDateBox.setEditable(true);

		final JComboBox filtersBox = new JComboBox();
		filtersBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				if (FILTER_NONE.equals(filtersBox.getSelectedItem())) {
					((CardLayout) cardsPanel.getLayout()).show(cardsPanel, FILTER_NONE);
				}
				else if (FILTER_DATE.equals(filtersBox.getSelectedItem())) {
					((CardLayout) cardsPanel.getLayout()).show(cardsPanel, FILTER_DATE);
				}
			}
		});

		filtersBox.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
		filtersBox.addItem(FILTER_NONE);
		filtersBox.addItem(FILTER_DATE);

		JPanel dateFilterPanel = new JPanel();
		dateFilterPanel.add(startDateLabel);
		dateFilterPanel.add(startDateBox);
		dateFilterPanel.add(endDateLabel);
		dateFilterPanel.add(endDateBox);

		cardsPanel.add(new JPanel(), FILTER_NONE);
		cardsPanel.add(dateFilterPanel, FILTER_DATE);

		JPanel eCenterPanel = new JPanel();
		eCenterPanel.add(new JLabel("Filter:"));
		eCenterPanel.add(filtersBox);

		TableColumn col = tablePanel.getTable().getTableHeader().getColumnModel()
		    .getColumn(LOG_TABLE_TIME_COLUMN);
		if (col != null) {
			col.setCellRenderer(new DateTimeCellRenderer(desktop.locale, desktop
			    .getTimeZonePreference()));
		}

		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(eCenterPanel, BorderLayout.EAST);

		JButton retrieveButton = new JButton("Retrieve");

		seastPanel.add(tablePanel.getExportButton(desktop, null));
		seastPanel.add(retrieveButton);
		southPanel.add(seastPanel, BorderLayout.EAST);
		southPanel.add(centerPanel, BorderLayout.CENTER);
		southPanel.add(cardsPanel, BorderLayout.WEST);

		retrieveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {

				desktop.showProgressDialog("Retrieving events...");
				if (FILTER_NONE.equals(filtersBox.getSelectedItem())) {
					getAndDisplayLogs(0, Long.MAX_VALUE);
				}
				else if (FILTER_DATE.equals(filtersBox.getSelectedItem())) {
					long stime = startDateBox.getTimeInMillis();
					long etime = endDateBox.getTimeInMillis();
					Calendar scal = Calendar.getInstance();
					scal.setTimeInMillis(stime);
					scal.set(Calendar.HOUR, 0);
					scal.set(Calendar.MINUTE, 0);
					scal.set(Calendar.SECOND, 0);
					scal.set(Calendar.AM_PM, Calendar.AM);
					Calendar ecal = Calendar.getInstance();
					ecal.setTimeInMillis(etime);
					ecal.set(Calendar.HOUR, 11);
					ecal.set(Calendar.MINUTE, 59);
					ecal.set(Calendar.SECOND, 59);
					ecal.set(Calendar.AM_PM, Calendar.PM);

					log.debug("startTime for search: " + scal.getTimeInMillis());
					log.debug("endTime for search: " + ecal.getTimeInMillis());

					getAndDisplayLogs(scal.getTimeInMillis(), ecal.getTimeInMillis());
				}

			}
		});

		tablePanel.getTable().addMouseListener(new MouseAdapter() {

			// Some platforms only detect when mouse is pressed (Linux)
			@Override
			public void mousePressed(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showLogDetailsPopup((Component) me.getSource(), me.getX(), me.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showLogDetailsPopup((Component) me.getSource(), me.getX(), me.getY());
				}
			}

		});

		tablePanel.addButton(southPanel);
		return tablePanel.getWrappedPanel("Event Logs");
	}

	private void displayLogs(List<LogRecord> logs) {
		if (logs == null) {
			return;
		}

		Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
		Vector<Object> row = null;

		try {
			desktop.setCursor(OpenDracDesktop.WAIT_CURSOR);

			for (LogRecord log : logs) {
				row = new Vector<Object>();

				// Showing time fields using configured timezone preference:
				row.add(Long.toString(log.getTime()));
				row.add(log.getOriginator());
				row.add(log.getIp());
				row.add(log.getBillingGroup().toString());
				row.add(log.getSeverity().toString());
				row.add(log.getCategory().toString());
				row.add(log.getType().toString());
				row.add(log.getResult().toString());
				row.add(log.getResource());
				row.add(log.getDescription());
				row.add(log.getDetails().isEmpty() ? "false" : "true");
				row.add(log.getDetails());
				rows.add(row);
			}

			((DefaultTableModel) tablePanel.getTable().getModel()).setDataVector(
			    rows, logColumns);

			TableColumn column = tablePanel.getTable().getTableHeader()
			    .getColumnModel().getColumn(LOG_TABLE_TIME_COLUMN);
			if (column != null) {
				column.setCellRenderer(new DateTimeCellRenderer(desktop.locale, desktop
				    .getTimeZonePreference()));
			}

			column = tablePanel.getTable().getTableHeader().getColumnModel()
			    .getColumn(MORE_COLUMN);
			if (column != null) {
				column.setCellRenderer(new TrueFalseRenderer());
			}

			tablePanel.getTable().removeColumn(
			    tablePanel.getTable().getColumnModel().getColumn(DETAILS_COL)); // REMOVE
			tablePanel.updateTable();

			// Override the default column sizing
			TableColumn timeCol = tablePanel.getTable().getColumnModel()
			    .getColumn(LOG_TABLE_TIME_COLUMN);
			if (timeCol != null) {
				timeCol.setPreferredWidth(250);
			}

			desktop.hideProgressDialog();
		}
		finally {
			desktop.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
		}
	}

	private void getAndDisplayLogs(final long startTime, final long endTime) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					List<LogRecord> logs = desktop.getNRBHandle().getLogs(
					    desktop.getLoginToken(), startTime, endTime, null);
					displayLogs(logs);
				}
				catch (Exception e) {
					log.error("OpenDracDesktop::getLogs failed.", e);
				}
			}
		}, "OpenDracDesktop getLogs()");
		t.setDaemon(true);
		t.start();
	}

	private void populateDetailsTable(TablePanel tableToPopulate,
	    Map<String, String> details) {
		Vector<Vector<String>> data = new Vector<Vector<String>>();

		if (details != null) {
			for (Map.Entry<String, String> e : details.entrySet()) {
				Vector<String> row = new Vector<String>();
				row.add(e.getKey());
				row.add(e.getValue());
				data.add(row);
			}
		}
		((DefaultTableModel) tableToPopulate.getTable().getModel()).setDataVector(
		    data, logDetailsColumns);
		tableToPopulate.updateTable();
	}

	private void showLogDetailsPopup(Component source, int x, int y) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem itemDetails = new JMenuItem("Show details...");

		itemDetails.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				int row = tablePanel.getTable().getSelectedRow();
				int modelRow = tablePanel.getTable().convertRowIndexToModel(row);
				TablePanel detailEventPanel = new TablePanel(new DefaultTableModel(
				    new Vector<Vector<String>>(), logDetailsColumns), null, null);

				// @TODO Add a close button to the dialog to dismiss it?

				// Fetch the details for the event
				log.debug("showEventDetailsDialog: asking for event details on table row "
				    + row
				    + " model row "
				    + modelRow
				    + " with "
				    + tablePanel.getTable().getColumnCount() + " columns ");

				Map<String, String> details = (Map<String, String>) tablePanel
				    .getTable().getModel().getValueAt(modelRow, DETAILS_COL);
				populateDetailsTable(detailEventPanel, details);

				JDialog eventDetailsDialog = new JDialog(desktop);
				eventDetailsDialog.getContentPane().add(
				    detailEventPanel.getWrappedPanel("Event Details"),
				    BorderLayout.CENTER);
				eventDetailsDialog.setTitle("Event Details");
				eventDetailsDialog.setSize(new Dimension(600, 300));
				eventDetailsDialog.setLocationRelativeTo(desktop);
				eventDetailsDialog.setVisible(true);
			}
		});

		popup.add(itemDetails);
		popup.show(source, x, y);
	}
}
