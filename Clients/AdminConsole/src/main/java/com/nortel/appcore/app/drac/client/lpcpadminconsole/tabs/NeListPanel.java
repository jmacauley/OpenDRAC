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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeCache;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.DracTableModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.JungTopologyPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.NetworkGraph;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.TopologyPopupMenu;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.TopologyViewEventHandler;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperationCallback;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;

/**
 * @author pitman
 */
public final class NeListPanel implements ServerOperationCallback, ActionListener {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
    private final JLabel nesManagedLabel = new JLabel("Total NEs managed: 0");
	private final JButton addNetworkElementButton = new JButton("Add NE");

	private final List<String> neColumns = Arrays.asList(new String[] { "IP",
	    "Port", "TID", "NEID", "Type", "Status", "Mode", "NeRelease", "Protocol",
	    "AutoRediscover", "NeIndex", "UserId" });
	private final OpenDracDesktop desktop;
	private final TablePanel tablePanel = new TablePanel(
	    new DracTableModel<String>(null, neColumns), null, null);

	public NeListPanel(OpenDracDesktop dracDesktop) {
		desktop = dracDesktop;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addNetworkElementButton) {
			TopologyPopupMenu.addNEDialog(desktop);
		}
	}
	public void enableAddNetworkElementButton(boolean enabled) {
		addNetworkElementButton.setEnabled(enabled);
	}
	
	public JPanel buildNePanel(){
		JPanel nePanel = new JPanel(new BorderLayout(1, 1));
		JPanel neListPanel =  buildNeListPanel();
		JPanel manageNEPanel = buildManageNEPanel();
		
		nePanel.add(neListPanel, BorderLayout.NORTH);
		nePanel.add(manageNEPanel, BorderLayout.SOUTH);
		
		return nePanel;
	}
	
	public JPanel buildManageNEPanel() {
		JPanel manageNEPanel = new JPanel(new BorderLayout(5, 5));
		JPanel addNEPanel = new JPanel(new BorderLayout(1, 1));
		JLabel addNELabel = new JLabel("Manage NE in OpenDRAC");
		JPanel findNEPanel = new JPanel(new BorderLayout(5, 5));
		JPanel findNEWestPanel = new JPanel(new BorderLayout(1, 1));
		JLabel findNELabel = new JLabel("Find NE");
		final JComboBox findNEBox = buildNEBox();
		JButton findNEButton = new JButton("Find");
		
		findNEBox.setPreferredSize(new Dimension(365, 21));
		findNEBox.setEditable(true);
		findNEBox.addPopupMenuListener(new PopupMenuListener() {
			boolean willBecomeVisible = false;

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				//
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				//
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				// Workaround for bug:
				// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4743225
				if (!willBecomeVisible) {
					JComboBox list = (JComboBox) e.getSource();

					findNEBox.removeAllItems();
					populateNEBox(findNEBox);

					willBecomeVisible = true; // prevent a loop

					try {
						list.getUI().setPopupVisible(list, true);
					}
					finally {
						willBecomeVisible = false;
					}
				}
			}
		});

		findNEBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				try {
					String neSearchString = (String) findNEBox.getSelectedItem();
					NetworkGraph.INSTANCE.highlightNELike(neSearchString);
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
			}
		});

		addNetworkElementButton.addActionListener(this);
		addNetworkElementButton.setFont(OpenDracDesktop.BASE_FONT);		
		
		addNEPanel.add(addNELabel, BorderLayout.CENTER);
		addNEPanel.add(addNetworkElementButton, BorderLayout.EAST);		
		
		findNEWestPanel.add(findNELabel, BorderLayout.WEST);
		findNEWestPanel.add(findNEBox, BorderLayout.CENTER);
		findNEWestPanel.add(findNEButton, BorderLayout.EAST);
		
		findNEButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					String neSearchString = (String) findNEBox.getSelectedItem();
					NetworkGraph.INSTANCE.highlightNELike(neSearchString);
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
			}
		});
		
		findNEPanel.add(findNEWestPanel, BorderLayout.CENTER);
		
		manageNEPanel.setBorder(BorderFactory.createTitledBorder("Network Element Management"));
		manageNEPanel.add(nesManagedLabel, BorderLayout.NORTH);
		manageNEPanel.add(addNEPanel, BorderLayout.CENTER);
		manageNEPanel.add(findNEPanel, BorderLayout.SOUTH);
		return manageNEPanel;
	}
	
	public JPanel buildNeListPanel() {
		JButton retrieveButton = new JButton("Retrieve");
		retrieveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				((DracTableModel<String>) tablePanel.getTable().getModel())
				    .clearTable();
				getAllNes();
			}
		});

		tablePanel.addButton(tablePanel.getExportButton(desktop, null));
		tablePanel.addButton(retrieveButton);
		tablePanel.getTable().addMouseListener(new MouseAdapter() {
			// Some platforms (like LINUX only detect the mouse pressed)
			@Override
			public void mousePressed(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showPopup((Component) me.getSource(), me.getX(), me.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showPopup((Component) me.getSource(), me.getX(), me.getY());
				}
			}
		});

		return tablePanel.getWrappedPanel("Network Element List");
	}

	@Override
	public void handleServerOperationResult(ServerOperation op) {
		List<NetworkElementHolder> nes = (List<NetworkElementHolder>) op
		    .getResult().get(ServerOperation.MAP_RESULT_KEY);
		if (nes == null) {
			JOptionPane.showMessageDialog(desktop,
			    "Error occurred retrieving Network elements.", "Error",
			    JOptionPane.ERROR_MESSAGE);
		}
		else {
			displayNetworkElements(nes);
		}
		desktop.hideProgressDialog();
	}

	private void displayNetworkElements(List<NetworkElementHolder> nes) {
		List<List<String>> rows = new ArrayList<List<String>>();
		List<String> row = null;

		try {
			desktop.setCursor(OpenDracDesktop.WAIT_CURSOR);
			Map<NeStatus, Integer> status = new TreeMap<NeStatus, Integer>();
			for (NetworkElementHolder ne : nes) {
				row = new ArrayList<String>();
				row.add(ne.getIp());
				row.add(ne.getPort());
				row.add(ne.getTid());
				row.add(ne.getId());
				row.add(ne.getType().toString());
				row.add(ne.getNeStatusWithDate());
				row.add(ne.getMode().toString());
				row.add(ne.getSubType());
				row.add(ne.getNeRelease());
				row.add(ne.getCommProtocol().toString());
				row.add(Boolean.toString(ne.isAutoReDiscover()));
				row.add(Integer.toString(ne.getNeIndex()));
				row.add(ne.getUserId());

				rows.add(row);

				Integer count = status.get(ne.getNeStatus());
				if (count == null) {
					count = Integer.valueOf(0);
				}
				count = Integer.valueOf(count.intValue() + 1);
				status.put(ne.getNeStatus(), count);
			}

			StringBuilder sb = new StringBuilder();

			sb.append(" (");
			for (Map.Entry<NeStatus, Integer> e : status.entrySet()) {
				sb.append(e.getValue());
				sb.append(" ");
				sb.append(e.getKey());
				sb.append(" ");
			}

			sb.append(") ");

			

			((DracTableModel<String>) tablePanel.getTable().getModel()).setData(rows);
			tablePanel.setResultsLabel(sb.toString());
			tablePanel.updateTable();
			desktop.hideProgressDialog();
		}
		finally {
			desktop.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
		}
	}

	private void getAllNes() {
		Map<String, String> parametersMap = new HashMap<String, String>();
		Thread t = new Thread(new ServerOperation(
		    ServerOperation.Operation.OP_GET_ALL_NES, this, parametersMap),
		    OpenDracDesktop.SVR_OP_THREAD_NAME + ": getAllNes()");
		desktop.showProgressDialog("Retrieving...");
		t.setDaemon(true);
		t.start();
	}
	
	private JComboBox buildNEBox() {
		return new JComboBox(NeCache.INSTANCE.getNEList());
	}

	private void populateNEBox(JComboBox box) {
		String[] nelist = NeCache.INSTANCE.getNEList();
		Arrays.sort(nelist);
		for (String s : nelist) {
			box.addItem(s);
		}
	}

	private void showPopup(Component source, int x, int y) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem findNe = new JMenuItem("Find Ne");
		JMenuItem neDelete = new JMenuItem("Remove NE");
		JMenuItem neToggle = new JMenuItem("Toggle NE Communications Link");
		JMenuItem props = new JMenuItem("Properties");

		popup.add(neDelete);
		popup.add(neToggle);

		if (tablePanel.getTable().getSelectedRowCount() > 1) {
			log.debug("Multi-Selected " + tablePanel.getTable().getSelectedRowCount()
			    + " NEs");

			neDelete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					int result = JOptionPane
					    .showConfirmDialog(
					        null,
					        "<HTML>Are you sure you want to remove "
					            + tablePanel.getTable().getSelectedRowCount()
					            + " Network Elements?<BR><B>All manually provisioned data will be lost</B></HTML>",
					        "Remove Network Element", JOptionPane.YES_NO_OPTION,
					        JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						for (int row : tablePanel.getTable().getSelectedRows()) {
							String ip = tablePanel.getTable().getValueAt(row, 0).toString();
							String port = tablePanel.getTable().getValueAt(row, 1).toString();
							String ieee = tablePanel.getTable().getValueAt(row, 3).toString();
							log.debug("Sending delete for " + ip + ":" + port + " " + ieee);
							NetworkElementHolder ne = new NetworkElementHolder(ip, port,
							    null, null, null, NeStatus.NE_UNKNOWN, null, null, 0, null,
							    null, ieee, null, false, "Unknown", null, null, null);
							desktop.handleTopologyViewEvents(
							    TopologyViewEventHandler.EVENT_TYPE.UNMANAGE_EVENT, ne);
						}
						getAllNes();
						JungTopologyPanel.resetTopology();
					}
				}
			});

			neToggle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					int result = JOptionPane
					    .showConfirmDialog(
					        null,
					        "<HTML>Are you sure you want to toggle the communication link to "
					            + tablePanel.getTable().getSelectedRowCount()
					            + " Network Elements ?<BR><B>Communications will temporarily be lost</B></HTML>",
					        "Toggle Network Element Communications",
					        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						for (int row : tablePanel.getTable().getSelectedRows()) {
							String ip = tablePanel.getTable().getValueAt(row, 0).toString();
							String port = tablePanel.getTable().getValueAt(row, 1).toString();
							log.debug("Sending toggle for " + ip + ":" + port);
							NetworkElementHolder ne = new NetworkElementHolder(ip, port,
							    null, null, null, NeStatus.NE_UNKNOWN, null, null, 0, null,
							    null, null, null, false, "Unknown", null, null, null);
							desktop.handleTopologyViewEvents(
							    TopologyViewEventHandler.EVENT_TYPE.TOGGLE_NE_EVENT, ne);
						}
						getAllNes();
					}
				}
			});
		}
		else {

			if (tablePanel.getTable().getSelectedRowCount() < 1) {
				// Didn't really select a row!
				return;
			}
			popup.add(findNe);
			popup.add(props);

			final String ip = tablePanel.getTable()
			    .getValueAt(tablePanel.getTable().getSelectedRow(), 0).toString();
			final String port = tablePanel.getTable()
			    .getValueAt(tablePanel.getTable().getSelectedRow(), 1).toString();
			final String tid = tablePanel.getTable()
			    .getValueAt(tablePanel.getTable().getSelectedRow(), 2).toString();
			final String ieee = tablePanel.getTable()
			    .getValueAt(tablePanel.getTable().getSelectedRow(), 3).toString();
			log.debug("Selected single NE " + ip + " " + port + " " + ieee);

			props.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					DracVertex vertex = NeCache.INSTANCE.getVertex(ip, port);
					if (vertex != null) {
						TopologyPopupMenu.showNEProperties(desktop.getJungTopologyPanel(),
						    desktop, vertex);
					}
				}
			});

			findNe.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						// desktop.getCurrentView().highlightNELike(tid);
						NetworkGraph.INSTANCE.highlightNELike(tid);
					}
					catch (Exception e1) {
						log.error("Error: ", e1);
					}
				}
			});

			neDelete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					int result = JOptionPane
					    .showConfirmDialog(
					        null,
					        "<HTML>Are you sure you want to remove "
					            + ip
					            + " ("
					            + tid
					            + ") ?<BR><B>All manually provisioned data will be lost</B></HTML>",
					        "Remove Network Element", JOptionPane.YES_NO_OPTION,
					        JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						NetworkElementHolder ne = new NetworkElementHolder(ip, port, null,
						    null, null, NeStatus.NE_UNKNOWN, null, null, 0, null, null,
						    ieee, null, false, "Unknown", null, null, null);
						desktop.handleTopologyViewEvents(
						    TopologyViewEventHandler.EVENT_TYPE.UNMANAGE_EVENT, ne);
						getAllNes();
					}
				}
			});

			neToggle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					int result = JOptionPane
					    .showConfirmDialog(
					        null,
					        "<HTML>Are you sure you want to toggle the communication link to "
					            + ip
					            + " ("
					            + tid
					            + ") ?<BR><B>Communications will temporarily be lost</B></HTML>",
					        "Toggle Network Element Communications",
					        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						NetworkElementHolder ne = new NetworkElementHolder(ip, port, null,
						    null, null, NeStatus.NE_UNKNOWN, null, null, 0, null, null,
						    null, null, false, "Unknown", null, null, null);
						desktop.handleTopologyViewEvents(
						    TopologyViewEventHandler.EVENT_TYPE.TOGGLE_NE_EVENT, ne);
						getAllNes();
					}
				}
			});
		}

		popup.show(source, x, y);
	}
}
