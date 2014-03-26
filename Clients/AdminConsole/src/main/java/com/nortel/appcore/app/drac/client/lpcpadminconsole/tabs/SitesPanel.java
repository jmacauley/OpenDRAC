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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.DracTableModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.JTextFieldLimit;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;
import com.nortel.appcore.app.drac.common.types.Site;

/**
 * @author pitman
 */
public final class SitesPanel {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static final String SITES_ID = "Site ID";
	private static final String SITES_LOCATION = "Location";
	private static final String SITES_DESCRIPTION = "Description";

	private final OpenDracDesktop desktop;
	private final List<String> sitesColumns = Arrays.asList(new String[] {
	    SITES_ID, SITES_LOCATION, SITES_DESCRIPTION });
	private final TablePanel tablePanel = new TablePanel(
	    new DracTableModel<String>(null, sitesColumns), null, null);

	public SitesPanel(OpenDracDesktop d) {
		desktop = d;
	}

	public JPanel buildSitesPanel() {
		tablePanel.getTable().addMouseListener(new MouseAdapter() {
			// Some platforms only detect when mouse is pressed (Linux)
			@Override
			public void mousePressed(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showSitesPopup((Component) me.getSource(), me.getX(), me.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showSitesPopup((Component) me.getSource(), me.getX(), me.getY());
				}
			}

		});

		final JButton retrieveButton = new JButton("Retrieve");
		retrieveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					retrieveButton.setEnabled(false);

					((DracTableModel<String>) tablePanel.getTable().getModel())
					    .clearTable();
					desktop.showProgressDialog("Retrieving sites...");
					getSites();

				}
				finally {
					retrieveButton.setEnabled(true);
				}
			}
		});

		final JButton addButton = new JButton("Add Site...");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					showAddSiteDialog();
				}
				finally {
					addButton.setEnabled(true);
				}
			}
		});

		tablePanel.addButton(addButton);
		tablePanel.addButton(tablePanel.getExportButton(desktop, null));
		tablePanel.addButton(retrieveButton);

		return tablePanel.getWrappedPanel("Sites list");
	}

	private void displaySites(List<Site> sites) {
		List<List<String>> rows = new ArrayList<List<String>>();
		List<String> row = null;

		if (sites != null) {
			for (Site site : sites) {
				row = new ArrayList<String>();
				row.add(site.getId());
				row.add(site.getLocation());
				row.add(site.getDescription());
				rows.add(row);
			}
		}

		((DracTableModel<String>) tablePanel.getTable().getModel()).setData(rows);
		tablePanel.updateTable();
		desktop.hideProgressDialog();

	}

	private void getSites() {
		List<Site> sites = retrieveSites();
		displaySites(sites);
	}

	private List<Site> retrieveSites() {
		try {
			return desktop.getNRBHandle().retrieveSiteList(desktop.getLoginToken());
		}
		catch (Exception e) {
			log.error("Failed to retrieve site list.", e);
		}

		return null;
	}

	private void showAddSiteDialog() {
		final JDialog d = new JDialog(desktop);

		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		JPanel flowButtonPanel = new JPanel();
		JPanel southEastPanel = new JPanel(new BorderLayout(5, 5));
		JPanel grid = new JPanel(new GridLayout(3, 3, 5, 5));

		JButton addButton = new JButton("Add");
		JButton cancelButton = new JButton("Cancel");
		JLabel idLabel = new JLabel("ID:");
		JLabel locationLabel = new JLabel("Location: ");
		JLabel descriptionLabel = new JLabel("Description: ");
		final JTextField idField = new JTextField();
		idField.setDocument(new JTextFieldLimit(64));
		final JTextField locationField = new JTextField();
		final JTextField descriptionField = new JTextField();

		idLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		locationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		descriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		grid.add(idLabel);
		grid.add(idField);
		grid.add(locationLabel);
		grid.add(locationField);
		grid.add(descriptionLabel);
		grid.add(descriptionField);

		flowButtonPanel.add(addButton);
		flowButtonPanel.add(cancelButton);

		southEastPanel.add(flowButtonPanel, BorderLayout.EAST);

		mainPanel.add(grid, BorderLayout.NORTH);
		mainPanel.add(southEastPanel, BorderLayout.SOUTH);

		flowButtonPanel.add(addButton);
		flowButtonPanel.add(cancelButton);

		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				String siteId = idField.getText();

				if ("".equalsIgnoreCase(siteId)) {
					JOptionPane.showMessageDialog(d, "Site ID cannot be blank", "Error",
					    JOptionPane.ERROR_MESSAGE);
					return;
				}

				siteId = siteId.trim();

				List<Site> sites = retrieveSites();
				for (Site site : sites) {
					if (site.getId().equals(siteId)) {
						JOptionPane.showMessageDialog(d,
						    "Site ID must be unique (no duplicates).", "Error",
						    JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				try {
					desktop.getNRBHandle().addSite(
					    desktop.getLoginToken(),
					    new Site(siteId, locationField.getText(), descriptionField
					        .getText()));
					d.setVisible(false);
					d.dispose();
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(desktop, "Error adding site.", "Error",
					    JOptionPane.ERROR_MESSAGE);
				}
				getSites();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				d.setVisible(false);
				d.dispose();
			}
		});

		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		d.getContentPane().setLayout(new BorderLayout(5, 5));
		d.getContentPane().add(mainPanel, BorderLayout.CENTER);
		d.setTitle("Add Site");
		d.setSize(300, 200);
		d.setLocationRelativeTo(desktop);
		d.setVisible(true);
	}

	private void showEditSiteDialog() {
		final JDialog d = new JDialog(desktop);

		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		JPanel flowButtonPanel = new JPanel();
		JPanel southEastPanel = new JPanel(new BorderLayout(5, 5));
		JPanel grid = new JPanel(new GridLayout(3, 3, 5, 5));

		JButton addButton = new JButton("Edit");
		JButton cancelButton = new JButton("Cancel");
		JLabel idLabel = new JLabel("ID:");
		JLabel locationLabel = new JLabel("Location: ");
		JLabel descriptionLabel = new JLabel("Description: ");
		final JTextField idField = new JTextField();
		final JTextField locationField = new JTextField();
		final JTextField descriptionField = new JTextField();

		if (!(tablePanel.getTable().getSelectedRow() >= 0)) {
			return;
		}

		idLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		locationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		descriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		grid.add(idLabel);
		grid.add(idField);
		grid.add(locationLabel);
		grid.add(locationField);
		grid.add(descriptionLabel);
		grid.add(descriptionField);

		idField.setText((String) tablePanel.getTable().getValueAt(
		    tablePanel.getTable().getSelectedRow(), 0));
		locationField.setText((String) tablePanel.getTable().getValueAt(
		    tablePanel.getTable().getSelectedRow(), 1));
		descriptionField.setText((String) tablePanel.getTable().getValueAt(
		    tablePanel.getTable().getSelectedRow(), 2));

		idField.setEditable(false);

		flowButtonPanel.add(addButton);
		flowButtonPanel.add(cancelButton);

		southEastPanel.add(flowButtonPanel, BorderLayout.EAST);

		mainPanel.add(grid, BorderLayout.NORTH);
		mainPanel.add(southEastPanel, BorderLayout.SOUTH);

		flowButtonPanel.add(addButton);
		flowButtonPanel.add(cancelButton);

		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if ("".equalsIgnoreCase(idField.getText())) {
					JOptionPane.showMessageDialog(d, "Site ID cannot be blank", "Error",
					    JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					desktop.getNRBHandle().updateSite(
					    desktop.getLoginToken(),
					    new Site(idField.getText(), locationField.getText(),
					        descriptionField.getText()));
				}
				catch (Exception e) {
					log.error("Error: ", e);
					JOptionPane.showMessageDialog(desktop,
					    "Error modifying site: " + e.getMessage(), "Error",
					    JOptionPane.ERROR_MESSAGE);
				}

				d.setVisible(false);
				d.dispose();
				getSites();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				d.setVisible(false);
				d.dispose();
			}
		});

		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		d.getContentPane().setLayout(new BorderLayout(5, 5));
		d.getContentPane().add(mainPanel, BorderLayout.CENTER);
		d.setTitle("Edit Site");
		d.setSize(300, 200);
		d.setLocationRelativeTo(desktop);
		d.setVisible(true);

	}

	private void showSitesPopup(Component source, int x, int y) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem itemModify = new JMenuItem("Modify...");

		itemModify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				showEditSiteDialog();
			}
		});

		popup.add(itemModify);
		popup.show(source, x, y);
	}
}
