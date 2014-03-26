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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeCache;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.DracTableModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.DesktopUtil;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.AidCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.DateDropDown;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.ProgressBarCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TidCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TimeDropDown;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;

/**
 * @author pitman
 */
public final class UtilisationPanel {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final OpenDracDesktop desktop;
	private long filteredStartTime = 0;
	private static final int UTILTBL_SRC_IDX = 0;
	private static final int UTILTBL_SRCFAC_IDX = 2;

	private final List<String> columns = Arrays.asList(new String[] { "Source",
	    "Target", "Source port", "Target port", "Utilisation" });
	private final TablePanel tablePanel = new TablePanel(
	    new DracTableModel<String>(null, columns), null, null);

	public UtilisationPanel(OpenDracDesktop d) {
		desktop = d;
	}

	public JPanel buildUtilisationPanel() {
		JButton filterButton = new JButton("Filter");
		tablePanel.addButton(filterButton);
		filterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				showFilterDialog();
			}
		});

		tablePanel.addButton(tablePanel.getExportButton(desktop, null));

		JButton retrieve = new JButton("Retrieve");
		tablePanel.addButton(retrieve);
		retrieve.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				loadBWUtilisation();
			}
		});

		tablePanel.getTable().addMouseListener(new MouseAdapter() {
			// Some platforms (like LINUX only detect the mouse pressed)
			@Override
			public void mousePressed(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showUtilisationPopup((Component) me.getSource(), me.getX(), me.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showUtilisationPopup((Component) me.getSource(), me.getX(), me.getY());
				}
			}
		});

		return tablePanel.getWrappedPanel("Utilisation");
	}

	private void handleQueryUtilisationEvent(java.util.List<BandwidthRecord> ur) {
		
		((DracTableModel<String>) tablePanel.getTable().getModel()).clearTable();
		tablePanel.updateTable();
		List<List<String>> rows = new ArrayList<List<String>>();
		for (BandwidthRecord br : ur) {
			String srcUsage = null;
			try {
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(2);
				srcUsage = nf.format(br.getUsage());
			}
			catch (Exception e) {
				log.error("Error: ", e);
			}

			List<String> row = new ArrayList<String>();
			row.add(br.getSourceNe());
			row.add(br.getTargetNe());
			row.add(br.getSourcePort());
			row.add(br.getTargetPort());
			row.add(srcUsage);
			rows.add(row);

		}
		((DracTableModel<String>) tablePanel.getTable().getModel()).setData(rows);

		Map<String, String> allNesMap = NeCache.INSTANCE.getIeeeLabelMap();
		TableColumn col = tablePanel.getTable().getColumn("Utilisation");
		if (col != null) {
			col.setCellRenderer(new ProgressBarCellRenderer());
		}
		col = tablePanel.getTable().getColumn("Source");
		if (col != null) {
			col.setCellRenderer(new TidCellRenderer(allNesMap));
		}
		col = tablePanel.getTable().getColumn("Target");
		if (col != null) {
			col.setCellRenderer(new TidCellRenderer(allNesMap));
		}
		col = tablePanel.getTable().getColumn("Source port");
		if (col != null) {
			col.setCellRenderer(new AidCellRenderer(allNesMap));
		}
		col = tablePanel.getTable().getColumn("Target port");
		if (col != null) {
			col.setCellRenderer(new AidCellRenderer(allNesMap));
		}
	}

	private void loadBWUtilisation() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					desktop.setCursor(OpenDracDesktop.WAIT_CURSOR);
					desktop.showProgressDialog("Retrieving bandwidth utilisation");
					

					/**
					 * By default we compute the bandwidth usage as of this instant in
					 * time. If the user has specified a different time, use that instant
					 * in time to report bandwidth on.
					 */
					long time = System.currentTimeMillis();
					if (filteredStartTime != 0) {
						time = filteredStartTime;
					}
					handleQueryUtilisationEvent(new ServerOperation().getInternalBandwithUsage(
					    time, time));
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
				finally {
					desktop.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
					tablePanel.updateTable();
					desktop.hideProgressDialog();
				}
			}
		}, "query queryUtilisation ");
		t.setDaemon(true);
		t.start();
	}

	private void showFilterDialog() {
		final JDialog filterDialog = new JDialog(desktop);
		JPanel eastButtonPanel = new JPanel();
		final TimeDropDown timeDrop = new TimeDropDown(5,
		    desktop.getTimeZonePreference(), desktop.getLocale());
		final DateDropDown dateDrop = new DateDropDown(
		    desktop.getTimeZonePreference(), desktop.getLocale());
		dateDrop.setEditable(true);

		long startTime = System.currentTimeMillis();
		timeDrop.setTime(new Date(startTime));
		dateDrop.setDate(new Date(startTime));

		JButton applyButton = new JButton("Apply");
		eastButtonPanel.add(applyButton);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				filteredStartTime = DesktopUtil.toDate(desktop.locale,
				    desktop.getTimeZonePreference(), dateDrop.getDate(),
				    timeDrop.getTime()).getTime();
				filterDialog.dispose();
				filterDialog.setVisible(false);
			}
		});

		JButton cancelButton = new JButton("Cancel");
		eastButtonPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				filterDialog.dispose();
				filterDialog.setVisible(false);
			}
		});

		JPanel buttonPanel = new JPanel(new BorderLayout(1, 1));
		buttonPanel.add(eastButtonPanel, BorderLayout.EAST);

		JPanel gridPanel = new JPanel(new GridLayout(4, 2));
		gridPanel.add(new JLabel("Date:"));
		gridPanel.add(dateDrop);
		JLabel startTimeLabel = new JLabel("Time:");
		gridPanel.add(startTimeLabel);
		gridPanel.add(timeDrop);

		JPanel mainPanel = new JPanel(new BorderLayout(1, 1));
		mainPanel.add(gridPanel, BorderLayout.NORTH);
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		filterDialog.getContentPane().setLayout(new BorderLayout(1, 1));
		filterDialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
		filterDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		filterDialog.setTitle("Filter");
		filterDialog.setLocationRelativeTo(desktop);
		filterDialog.setSize(new Dimension(300, 200));
		filterDialog.setVisible(true);
	}

	private void showUtilisationPopup(Component source, int x, int y) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem itemFind = new JMenuItem("Find services");

		itemFind.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				String neid = null;
				String facility = null;

				if (tablePanel.getTable().getSelectedRow() >= 0) {
					neid = (String) tablePanel.getTable().getValueAt(
					    tablePanel.getTable().getSelectedRow(), UTILTBL_SRC_IDX);
					facility = (String) tablePanel.getTable().getValueAt(
					    tablePanel.getTable().getSelectedRow(), UTILTBL_SRCFAC_IDX);
				}

				log.debug("facility is: " + facility);
				desktop.getServices(neid, facility);
			}
		});

		popup.add(itemFind);
		popup.show(source, x, y);
	}
}
