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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeCache;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.DracTableModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.NetworkGraph;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.DesktopUtil;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.AidCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.DateDropDown;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.DateTimeCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TidCellRenderer;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;

/**
 * @author pitman
 */
public final class SchedulePanel {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private JDialog cancelServiceDialog;
	private static final String SVC_ID_STR = "ID";
	private static final String SVC_CALLID_STR = "Call ID";
	private static final String SVC_SCHEDNAME_STR = "Schedule Name";
	private static final String SVC_STATUS_STR = "Status";
	private static final String SVC_USER_STR = "User";
	private static final String SVC_BANDWIDTH_STR = "Bandwidth (Mb/s)";
	private static final String SVC_STARTTIME_STR = "Start Time";
	private static final String SVC_ENDTIME_STR = "End Time";
	private static final String SVC_HEADEND_NODE_STR = "Headend Node";
	private static final String SVC_TAILEND_NODE_STR = "Tailend Node";
	private static final String SVC_ACTIVATIONTYE_STR = "ActivationType";
	private static final String SVC_VCAT_STR = "VCAT";
	private static final String SVC_HIDDEN_STR = "--- HIDDEN COLUMN --";

	private static final String SERVICEID_PANEL = "serviceIdPanel";
	private static final String DATE_PANEL = "datePanel";
	private static final String NOFILTER_PANEL = "noFilterPanel";
	private final OpenDracDesktop desktop;

	private static final int SERVICEID_IDX = 0;
	private static final int STATUS_IDX = 3;
	private static final int HIDDEN_COLUMN = 12;

	private final List<String> serviceColumns = Arrays.asList(
			new String[] {
				SVC_ID_STR,
				SVC_CALLID_STR,
				SVC_SCHEDNAME_STR,
				SVC_STATUS_STR,
				SVC_USER_STR,
				SVC_BANDWIDTH_STR,
				SVC_STARTTIME_STR,
				SVC_ENDTIME_STR,
				SVC_HEADEND_NODE_STR,
				SVC_TAILEND_NODE_STR,
				SVC_ACTIVATIONTYE_STR,
				SVC_VCAT_STR,
				SVC_HIDDEN_STR
			}
	);

	private static final String SD_SOURCEPORT_STR = "Source port";
	private static final String SD_TARGETPORT_STR = "Target port";
	private static final String SD_RATE_STR = "Rate";
	private static final String SD_SOURCECHANNEL_STR = "Source channel";
	private static final String SD_TARGETCHANNEL_STR = "Target channel";
	private static final String SD_DATADUMP_STR = "Data dump";
	private static final String SD_VLAN_ID= "Vlan Id";

	private final List<String> columnsDetails = Arrays.asList(
			new String[] {
				OpenDracDesktop.NETWORK_ELEMENT_STR,
				SD_SOURCEPORT_STR,
				SD_TARGETPORT_STR,
				SD_RATE_STR,
				SD_SOURCECHANNEL_STR,
				SD_TARGETCHANNEL_STR,
				SD_VLAN_ID,
				SD_DATADUMP_STR
			}
	);

	private final TablePanel servicesListPanel = new TablePanel(
	    new DracTableModel<Object>(new ArrayList<List<Object>>(), serviceColumns),
	    null, null);
	private final TablePanel serviceDetailsPanel = new TablePanel(
	    new DracTableModel<String>(new ArrayList<List<String>>(), columnsDetails),
	    null, null);

	public SchedulePanel(OpenDracDesktop desk) {
		desktop = desk;		
	}

	public JPanel buildSchedulePanel() {
		JPanel serviceIdFilterPanel = new JPanel();
		JPanel dateFilterPanel = new JPanel();
		final JPanel cardsPanel = new JPanel();
		JPanel noFilterPanel = new JPanel();
		JLabel serviceIdLabel = new JLabel("Service ID:");
		JLabel startDateLabel = new JLabel("From:");
		JLabel endDateLabel = new JLabel("To:");
		JLabel filterLabel = new JLabel("Filter:");
		final JComboBox filtersBox = new JComboBox();
		final JTextField serviceIdField = new JTextField(15);
		final DateDropDown startDateBox = new DateDropDown(
		    desktop.getTimeZonePreference(), desktop.getLocale());
		final DateDropDown endDateBox = new DateDropDown(
		    desktop.getTimeZonePreference(), desktop.getLocale());
		final JButton retrieveButton = new JButton("Retrieve");

		servicesListPanel.getTable().addMouseListener(new MouseAdapter() {
			// Some platforms (like LINUX only detect the mouse pressed)
			@Override
			public void mousePressed(MouseEvent me) {

				if (me.isPopupTrigger()) {
					SERVICE status = null;
					int selectedRow = servicesListPanel.getTable().getSelectedRow();
					if (selectedRow != -1) {
						status = (SERVICE) servicesListPanel.getTable().getValueAt(selectedRow,
						    STATUS_IDX);
					}
					showServiceCancelActivateModifyPopup((Component) me.getSource(),
					    me.getX(), me.getY(), status);
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {

				if (me.isPopupTrigger()) {
					SERVICE status = null;
					int selectedRow = servicesListPanel.getTable().getSelectedRow();
					if (selectedRow != -1) {
						status = (SERVICE) servicesListPanel.getTable().getValueAt(selectedRow,
						    STATUS_IDX);
					}
					showServiceCancelActivateModifyPopup((Component) me.getSource(),
					    me.getX(), me.getY(), status);
				}
			}
		});

		Map<String, String> allNesMap = NeCache.INSTANCE.getIeeeLabelMap();
		TableColumn headEndCol = servicesListPanel.getTable().getColumn(SVC_HEADEND_NODE_STR);
		if (headEndCol != null) {
			headEndCol.setCellRenderer(new TidCellRenderer(allNesMap));
		}
		TableColumn tailEndCol = servicesListPanel.getTable().getColumn(SVC_TAILEND_NODE_STR);
		if (tailEndCol != null) {
			tailEndCol.setCellRenderer(new TidCellRenderer(allNesMap));
		}
		servicesListPanel.updateTable();

		TableColumn NECol = serviceDetailsPanel.getTable().getColumn(
		    OpenDracDesktop.NETWORK_ELEMENT_STR);
		if (NECol != null) {
			NECol.setCellRenderer(new TidCellRenderer(allNesMap));
		}

		TableColumn srcCol = serviceDetailsPanel.getTable().getColumn(
		    SD_SOURCEPORT_STR);
		if (srcCol != null) {
			srcCol.setCellRenderer(new AidCellRenderer(allNesMap));
		}

		TableColumn tgtCol = serviceDetailsPanel.getTable().getColumn(SD_TARGETPORT_STR);
		if (tgtCol != null) {
			tgtCol.setCellRenderer(new AidCellRenderer(allNesMap));
		}

		servicesListPanel.getTable().removeColumn(
		    servicesListPanel.getTable().getColumnModel().getColumn(HIDDEN_COLUMN)); // REMOVE

		retrieveButton.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					retrieveButton.setEnabled(false);
					((DracTableModel<Object>) servicesListPanel.getTable().getModel())
					    .setData(new ArrayList<List<Object>>());
					servicesListPanel.updateTable();

					long stime = startDateBox.getTimeInMillis();
					long etime = endDateBox.getTimeInMillis();
					Calendar scal = Calendar.getInstance();
					scal.setTimeInMillis(stime);
					scal.set(Calendar.AM_PM, Calendar.AM);
					scal.set(Calendar.HOUR, 0);
					scal.set(Calendar.MINUTE, 0);
					scal.set(Calendar.SECOND, 0);
					scal.getTime(); // Force recalculation of time
					Calendar ecal = Calendar.getInstance();
					ecal.setTimeInMillis(etime);
					ecal.set(Calendar.AM_PM, Calendar.PM);
					ecal.set(Calendar.HOUR, 11);
					ecal.set(Calendar.MINUTE, 59);
					ecal.set(Calendar.SECOND, 59);
					ecal.getTime(); // Force recalculation of time

					if (OpenDracDesktop.FILTER_NONE.equals(filtersBox.getSelectedItem())) {
						getServices();
					}
					else if (OpenDracDesktop.FILTER_SERVICEID.equals(filtersBox
					    .getSelectedItem())) {
						getServices(serviceIdField.getText());
					}
					else if (OpenDracDesktop.FILTER_DATE.equals(filtersBox.getSelectedItem())) {
						getServices(scal.getTimeInMillis(), ecal.getTimeInMillis());
					}
				}
				finally {
					retrieveButton.setEnabled(true);
				}
			}
		});

		populateFiltersBox(filtersBox);

		startDateBox.setPreferredSize(new Dimension(110, 21));
		endDateBox.setPreferredSize(new Dimension(110, 21));

		startDateBox.setEditable(true);
		endDateBox.setEditable(true);

		cardsPanel.setLayout(new CardLayout());

		serviceIdFilterPanel.add(serviceIdLabel);
		serviceIdFilterPanel.add(serviceIdField);

		dateFilterPanel.add(startDateLabel);
		dateFilterPanel.add(startDateBox);
		dateFilterPanel.add(endDateLabel);
		dateFilterPanel.add(endDateBox);

		cardsPanel.add(noFilterPanel, NOFILTER_PANEL);
		cardsPanel.add(dateFilterPanel, DATE_PANEL);
		cardsPanel.add(serviceIdFilterPanel, SERVICEID_PANEL);

		filtersBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				if (OpenDracDesktop.FILTER_NONE.equals(filtersBox.getSelectedItem())) {
					((CardLayout) cardsPanel.getLayout())
					    .show(cardsPanel, NOFILTER_PANEL);
				}
				else if (OpenDracDesktop.FILTER_SERVICEID.equals(filtersBox
				    .getSelectedItem())) {
					((CardLayout) cardsPanel.getLayout()).show(cardsPanel,
					    SERVICEID_PANEL);
				}
				else if (OpenDracDesktop.FILTER_DATE.equals(filtersBox.getSelectedItem())) {
					((CardLayout) cardsPanel.getLayout()).show(cardsPanel, DATE_PANEL);
				}
			}
		});

		filtersBox.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());

		servicesListPanel.addButton(cardsPanel);
		servicesListPanel.addButton(filterLabel);
		servicesListPanel.addButton(filtersBox);
		servicesListPanel.addButton(servicesListPanel.getExportButton(desktop, null));
		servicesListPanel.addButton(retrieveButton);

		// Ask to be notified of selection changes.
		servicesListPanel.getTable().setSelectionMode(
		    ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = servicesListPanel.getTable().getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				try {
					// Ignore extra messages.
					if (e.getValueIsAdjusting()) {
						return;
					}

					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
					if (lsm.isSelectionEmpty()) {
						// no rows are selected
						NetworkGraph.INSTANCE.resetGraph();
						// desktop.getCurrentView().resetGraph();
					}
					else {
						// selectedRow is selected
						int selectedRow = lsm.getMinSelectionIndex();

						String selectedServiceId = (String) servicesListPanel.getTable()
						    .getValueAt(selectedRow, 0);
						log.debug("Service with id: " + selectedServiceId + " selected.");

						ServiceXml s = (ServiceXml) servicesListPanel
						    .getTable()
						    .getModel()
						    .getValueAt(
						        servicesListPanel.getTable()
						            .convertRowIndexToModel(selectedRow), HIDDEN_COLUMN);

						// We don't display the working path.
						populateServiceDetailsPanelandHighlightService(s);

						TableColumn colu = serviceDetailsPanel.getTable().getColumn(
						    OpenDracDesktop.NETWORK_ELEMENT_STR);
						Map<String, String> nesMap = NeCache.INSTANCE
						    .getIeeeLabelMap();
						if (colu != null) {
							colu.setCellRenderer(new TidCellRenderer(nesMap));
						}

						TableColumn srcCol = serviceDetailsPanel.getTable().getColumn(
						    SD_SOURCEPORT_STR);
						if (srcCol != null) {
							srcCol.setCellRenderer(new AidCellRenderer(nesMap));
						}

						TableColumn tgtCol = serviceDetailsPanel.getTable().getColumn(
						    SD_TARGETPORT_STR);
						if (tgtCol != null) {
							tgtCol.setCellRenderer(new AidCellRenderer(nesMap));
						}

						serviceDetailsPanel.updateTable();

					}
				}
				catch (Exception e1) {
					log.error("Error: ", e1);
				}
			}
		});

		// create main panel and populate with main components: tables and tabs
		JPanel mainPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		mainPanel.add(servicesListPanel.getWrappedPanel(OpenDracDesktop.SERVICES_STR));
		mainPanel.add(serviceDetailsPanel.getWrappedPanel(OpenDracDesktop.SERVICE_DETAILS_STR));
		desktop.schedTabPane.addTab("Scheduling", mainPanel);

		JPanel schedulePanel = new JPanel(new BorderLayout(1, 1));
		schedulePanel.add(desktop.schedTabPane, BorderLayout.CENTER);
		return schedulePanel;
	}

	public void cancelDialog() {
		if (cancelServiceDialog != null) {
			cancelServiceDialog.setVisible(false);
			cancelServiceDialog.dispose();
			cancelServiceDialog = null;
		}
	}

	@SuppressWarnings("unchecked")
	public void displaySchedules(List<ServiceXml> lightPathList) {
		
		desktop.tabbedPane.setSelectedIndex(OpenDracDesktop.SCHEDULING_TAB_IDX);

		try {
			servicesListPanel.updateTable();
			List<List<Object>> rows = new ArrayList<List<Object>>();

			for (ServiceXml s : lightPathList) {
				List<Object> row = new ArrayList<Object>();
				row.add(s.getServiceId());
				row.add(s.getCallId());
				row.add(s.getScheduleName());
				row.add(s.getStatus()); // (SERVICE)
				row.add(s.getUser());
				row.add(s.getBandwidth());
				row.add(Long.toString(s.getStartTime()));
				row.add(Long.toString(s.getEndTime()));

				row.add(s.getAend());
				row.add(s.getZend());
				row.add(s.getActivationType());
				row.add(s.getVcatRoutingOption());

				row.add(s);
				rows.add(row);
			}
			((DracTableModel<Object>) servicesListPanel.getTable().getModel())
			    .setData(rows);

			if (!lightPathList.isEmpty()) {
				Map<String, String> allNesMap = NeCache.INSTANCE.getIeeeLabelMap();
				TableColumn col = servicesListPanel.getTable().getColumn(
				    SVC_HEADEND_NODE_STR);
				if (col != null) {
					col.setCellRenderer(new TidCellRenderer(allNesMap));
				}
				col = servicesListPanel.getTable().getColumn(SVC_TAILEND_NODE_STR);
				if (col != null) {
					col.setCellRenderer(new TidCellRenderer(allNesMap));
				}

				col = servicesListPanel.getTable().getColumn(
				    SchedulePanel.SVC_STARTTIME_STR);
				if (col != null) {
					col.setCellRenderer(new DateTimeCellRenderer(desktop.locale, desktop
					    .getTimeZonePreference()));
				}

				col = servicesListPanel.getTable().getColumn(SchedulePanel.SVC_ENDTIME_STR);
				if (col != null) {
					col.setCellRenderer(new DateTimeCellRenderer(desktop.locale, desktop
					    .getTimeZonePreference()));
				}

				servicesListPanel.getTable().removeColumn(
				    servicesListPanel.getTable().getColumnModel().getColumn(HIDDEN_COLUMN)); // REMOVE
				servicesListPanel.updateTable();

				// Override the default column sizing
				TableColumn startCol = servicesListPanel.getTable().getColumn(
				    SVC_STARTTIME_STR);
				if (startCol != null) {
					startCol.setPreferredWidth(250);
				}
				TableColumn endCol = servicesListPanel.getTable()
				    .getColumn(SVC_ENDTIME_STR);
				if (endCol != null) {
					endCol.setPreferredWidth(250);
				}
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
		finally {
			desktop.hideProgressDialog();
			desktop.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
		}

	}

	private void activateSelectedService() {
		try {
			ServerOperation so = new ServerOperation();
			so.activateService((String) servicesListPanel.getTable().getValueAt(
			    servicesListPanel.getTable().getSelectedRow(), 0));
			JOptionPane.showMessageDialog(desktop, "Service activated successfully.");
		}
		catch (Exception e) {
			log.error("Error: ", e);
			JOptionPane.showMessageDialog(desktop,
			    "An exception occurred: " + e.getMessage());
		}
	}

	private void cancelSelectedService() {

		JLabel warningLabel = new JLabel(OpenDracDesktop.DLT_SVC_WARN_STR);
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
		JPanel buttonEastPanel = new JPanel();
		final JTable deleteTable = new JTable();
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		JScrollPane jsp = null;

		boolean trafficDelete = false;
		String columns[] = { SVC_ID_STR, SVC_CALLID_STR, SVC_SCHEDNAME_STR,
		    SVC_STATUS_STR, SVC_USER_STR, SVC_BANDWIDTH_STR, SVC_STARTTIME_STR,
		    SVC_ENDTIME_STR, SVC_HEADEND_NODE_STR, SVC_TAILEND_NODE_STR };

		cancelServiceDialog = new JDialog(desktop);

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				desktop.showProgressDialog("Canceling service(s)...");
				for (int i = 0; i < deleteTable.getModel().getRowCount(); i++) {
					sendCancelService(((String) deleteTable.getValueAt(i, SERVICEID_IDX))
					    .trim());
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				cancelServiceDialog.setVisible(false);
			}
		});

		int selectedRows[] = servicesListPanel.getTable().getSelectedRows();

		String rows[][] = new String[selectedRows.length][columns.length];

		for (int i = 0; i < selectedRows.length; i++) {
			for (int j = 0; j < columns.length; j++) {
				rows[i][j] = servicesListPanel.getTable().getValueAt(selectedRows[0], j)
				    .toString();
			}
		}

		deleteTable.setModel(new DefaultTableModel(rows, columns));
		deleteTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		DesktopUtil.sizeColumns(deleteTable);

		jsp = new JScrollPane(deleteTable);

		cancelServiceDialog.setTitle(OpenDracDesktop.CANCEL_SVC_TITLE);

		trafficDelete = isInProgressDelete(selectedRows);

		if (trafficDelete) { // Warn of in-service traffic affecting delete
			warningLabel.setText(OpenDracDesktop.DLT_ACT_SVC_WARN_STR);
		}
		else {
			warningLabel.setText(OpenDracDesktop.DLT_SVC_WARN_STR);
		}

		buttonEastPanel.add(okButton);
		buttonEastPanel.add(cancelButton);

		buttonPanel.add(buttonEastPanel, BorderLayout.EAST);

		mainPanel.add(warningLabel, BorderLayout.NORTH);
		mainPanel.add(jsp, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		cancelServiceDialog.setSize(800, 250);
		cancelServiceDialog.getContentPane()
		    .setLayout(new GridLayout(1, 1, 10, 10));
		cancelServiceDialog.add(mainPanel);
		cancelServiceDialog.setLocationRelativeTo(desktop);
		cancelServiceDialog.setVisible(true);
	}

	private void getServices() {
		Map<String, Object> parametersMap = new HashMap<String, Object>();
		Thread t = new Thread(new ServerOperation(
		    ServerOperation.Operation.OP_GET_SERVICES, desktop, parametersMap),
		    " getServices()");
		desktop.showProgressDialog("Retrieving services...");
		t.setDaemon(true);
		t.start();
	}

	private void getServices(final long startTime, final long endTime) {
		Map<String, Object> parametersMap = new HashMap<String, Object>();
		parametersMap.put(DbKeys.LightPathCols.LP_STARTTIME_GREATERTHAN_EQUALTO,
		    Long.toString(startTime));
		parametersMap.put(DbKeys.LightPathCols.LP_ENDTIME_LESSTHAN_EQUALTO,
		    Long.toString(endTime));
		Thread t = new Thread(new ServerOperation(
		    ServerOperation.Operation.OP_GET_SERVICES, desktop, parametersMap),
		    " getServices()");
		desktop.showProgressDialog("Retrieving services...");
		t.setDaemon(true);
		t.start();
	}

	private void getServices(final String serviceId) {
		Map<String, Object> parametersMap = new HashMap<String, Object>();
		parametersMap.put(DbKeys.LightPathCols.LP_SERVICEID, serviceId + "%");
		Thread t = new Thread(new ServerOperation(
		    ServerOperation.Operation.OP_GET_SERVICES, desktop, parametersMap),
		    " getServices()");
		desktop.showProgressDialog("Retrieving services...");
		t.setDaemon(true);
		t.start();
	}

	private boolean isInProgressDelete(int[] rowIdxs) {

		boolean inprogress = false;

		for (int element : rowIdxs) {
			if (OpenDracDesktop.SVC_STATE_INPROGRESS
			    .equalsIgnoreCase((String) servicesListPanel.getTable().getValueAt(
			        element, 1))) {
				inprogress = true;
			}
		}

		return inprogress;

	}

	private void populateFiltersBox(JComboBox filtersBox) {
		if (filtersBox != null) {
			filtersBox.removeAll();
			filtersBox.addItem(OpenDracDesktop.FILTER_NONE);
			filtersBox.addItem(OpenDracDesktop.FILTER_DATE);
			filtersBox.addItem(OpenDracDesktop.FILTER_SERVICEID);
		}
	}

	@SuppressWarnings("unchecked")
	private void populateServiceDetailsPanelandHighlightService(ServiceXml service)
	    throws Exception {
		// reset the graph on the left.
		NetworkGraph.INSTANCE.resetGraph();

		// Update the table with
		((DracTableModel<String>) serviceDetailsPanel.getTable().getModel())
		    .clearTable();
		serviceDetailsPanel.updateTable();

		List<List<String>> rows = new ArrayList<List<String>>();
		/*
		 * Display in the table the provisioned cross connections. The service
		 * record holds the actual provisioned cross connections as well as keeps a
		 * partial list of working and protection entries.
		 */
		for (CrossConnection edge : service.getCrossConnections()) {
			List<String> row = new ArrayList<String>();
			row.add(edge.getSourceNeId());
			row.add(edge.getSourcePortAid());
			row.add(edge.getTargetPortAid());
			row.add(edge.getRate());
			row.add(edge.getSourceChannel());
			row.add(edge.getTargetChannel());
			row.add(edge.getVlanId());
			row.add(edge.toString());
			rows.add(row);
		}
		((DracTableModel<String>) serviceDetailsPanel.getTable().getModel())
		    .setData(rows);
		serviceDetailsPanel.updateTable();

		// Highlight the path (both actual and protection)
		updateGraph(service.getCrossConnections(), new Color(0, 102, 204));
		updateGraph(service.getProtectionPath(), Color.red);

	}

	private void sendCancelService(final String schedID) {
		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					new ServerOperation().cancelService(schedID);
					desktop.hideProgressDialog();
					JOptionPane.showMessageDialog(desktop, "Service '" + schedID
					    + "' canceled successfully.", "Result",
					    JOptionPane.INFORMATION_MESSAGE);
				}
				catch (Exception t) {
					log.error("Error: ", t);
					desktop.hideProgressDialog();
					JOptionPane.showMessageDialog(desktop, "Failed to cancel service: '"
					    + schedID + "' " + t, "Error", JOptionPane.ERROR_MESSAGE);
				}
				cancelDialog();
			}
		}, "CancelService " + schedID);
		thread.setDaemon(true);
		thread.start();

	}

	private void showServiceCancelActivateModifyPopup(Component source, int x,
	    int y, SERVICE status) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem itemCancel = new JMenuItem("Cancel...");
		JMenuItem itemActivate = new JMenuItem("Activate...");

		itemCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				cancelSelectedService();
			}
		});
		/*
		 * Confirm should only be done at the schedule level
		 * itemConfirm.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent ae) { confirmSelectedService(); } });
		 */
		itemActivate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				activateSelectedService();
			}
		});


		if (status != null) {

			if (!State.SERVICE.EXECUTION_SUCCEEDED.equals(status)) {
				popup.add(itemCancel);
				// We don't support modify yet
				// popup.add(itemModify);
			}
			/*
			 * Confirm should only be done at the schedule level if (
			 * (State.SERVICE.CONFIRMATION_PENDING
			 * .toString().equalsIgnoreCase(status)) ) { popup.add(itemConfirm); }
			 */
			if (State.SERVICE.ACTIVATION_PENDING.equals(status)) {
				popup.add(itemActivate);
			}
		}
		else {
			popup.add(itemCancel);
			// We don't support modify yet
			// popup.add(itemModify);
		}
		popup.show(source, x, y);
	}

	private void updateGraph(List<CrossConnection> edgeList, Color highlight) {
		Map<String, CrossConnection> edgeMap = new HashMap<String, CrossConnection>();
		if (edgeList != null && !edgeList.isEmpty()) {
			for (CrossConnection edge : edgeList) {
				edgeMap.put((edge.getSourceNeId() + "_" + edge.getSourcePortAid())
				    .toUpperCase(), edge);
				edgeMap.put((edge.getTargetNeId() + "_" + edge.getTargetPortAid())
				    .toUpperCase(), edge);
			}
			for (CrossConnection edge : edgeList) {
				NetworkGraph.INSTANCE.hightLightLink(edge, edgeMap, highlight);
			}
		}
	}
}
