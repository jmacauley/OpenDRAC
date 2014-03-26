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
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeCache;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.NetworkGraph;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.TopologyViewEventHandler;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.EmptyDataRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.types.GraphData;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;

import edu.uci.ics.jung.graph.Graph;

// This panel is meant to be in synch with the graph being displayed. As such,
// the retrieve from this panel will need to reload the graph.
public class TopologyPanel {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	private final Vector<String> adjColumns = new Vector<String>(
	    Arrays.asList(new String[] { "NE Id", "Port", "Tx Tag", "Rx Tag",
	        "Layer Type", "Manual", "NeIEEE" }));
	private final Vector<String> linksColumns = new Vector<String>(
	    Arrays.asList(new String[] { "Src", "Src Aid", "Dst", "Dst Aid",
	        "Manual", "Src Channel", "Ingress Ip", "Dst Channel", "Egress Ip",
	        "Cost", "Metric", "Weight", "SRLG", "EdgeId", "SrcIeee", "DstIeee" }));

	private static final int edgeSrcAidColumnIdx = 1;
	private static final int edgeDstAidColumnIdx = 3;
	private static final int edgeIsManualColumnIdx = 4;
	private static final int edgeIdColumnIdx = 13;
	private static final int edgeSrcIeeeColumnIdx = 14;
	private static final int edgeDstIeeeColumnIdx = 15;
	private static final int edgeStartOfHideColumnsIdx = edgeIdColumnIdx;

	private static final int adjPortColumnIdx = 1;
	private static final int adjManualColumnIdx = 5;
	private static final int adjNeIeeeColumnIdx = 6;
	private static final int adjStartOfHideColumnsIdx = adjNeIeeeColumnIdx;

	private static final int rxTagColumnIdx = 3;

	private final TablePanel adjTablePanel = new TablePanel(
	    new DefaultTableModel(new Vector<Vector<String>>(), adjColumns), null,
	    null);
	private final TablePanel eclipsedTablePanel = new TablePanel(
	    new DefaultTableModel(new Vector<Vector<String>>(), linksColumns), null,
	    null);
	private final TablePanel consolidatedLinksTablePanel = new TablePanel(
	    new DefaultTableModel(new Vector<Vector<String>>(), linksColumns), null,
	    null);
	private List<NetworkElementAdjacency> adjacenciesList;

	private JButton retrieveButton;
	private final OpenDracDesktop desktop;

	private static final String title1 = "Consolidated Links (graphed)";
	private static final String title2 = "Eclipsed Manual Links";
	private static final String title3 = "Network Adjacencies (raw)";

	public TopologyPanel(OpenDracDesktop d) {
		desktop = d;
	}

	public JPanel buildTopologyPanel() {
		JPanel buttonPanel = new JPanel(new BorderLayout(1, 1));
		JPanel seastPanel = new JPanel();
		retrieveButton = new JButton("Retrieve");

		Map<String, TableModel> map = new HashMap<String, TableModel>();
		map.put(title1, consolidatedLinksTablePanel.getTable().getModel());
		map.put(title2, eclipsedTablePanel.getTable().getModel());
		map.put(title3, adjTablePanel.getTable().getModel());
		String fileName = "Topology";

		seastPanel.add(consolidatedLinksTablePanel.getExportButton(desktop, null,
		    fileName, map));
		seastPanel.add(retrieveButton);
		buttonPanel.add(seastPanel, BorderLayout.EAST);

		retrieveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					NeCache.INSTANCE.clearCache();
				}
				catch (Exception e) {
					log.error("TopologyPanel getGraph error.", e);
					return;
				}

				retrieveButton.setEnabled(false);
				clearTables();
				desktop.showProgressDialog("Retrieving topology data ...");

				class Worker extends SwingWorker<Object, Object> {
					GraphData graphData = null;

					@Override
					public Object doInBackground() {
						try {
							graphData = new ServerOperation().getGraphData();

							Map<String, String> filter = new HashMap<String, String>();
							adjacenciesList = desktop
							    .getNRBHandle()
							    .getNetworkElementAdjacencies(desktop.getLoginToken(), filter);
						}
						catch (Exception e) {
							log.error("TopologyPanel retrieval error", e);
							return e;
						}

						return null;
					}

					@Override
					protected void done() {
						try {
							if (get() == null) {
								desktop.setCursor(OpenDracDesktop.WAIT_CURSOR);

								NetworkGraph.INSTANCE.setGraphData(graphData);
								updateTables();

								retrieveButton.setEnabled(true);
								desktop.hideProgressDialog();
								desktop.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
							}
						}
						catch (Exception e) {
							log.error("TopologyPanel retrieval error.", e);
						}
					}
				}

				new Worker().execute();
			}
		});

		consolidatedLinksTablePanel.getTable().addMouseListener(new MouseAdapter() {
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

		eclipsedTablePanel.getTable().addMouseListener(new MouseAdapter() {
			// Some platforms (like LINUX only detect the mouse pressed)
			@Override
			public void mousePressed(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showPopupEclipsedLinks((Component) me.getSource(), me.getX(),
					    me.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showPopupEclipsedLinks((Component) me.getSource(), me.getX(),
					    me.getY());
				}
			}
		});

		// Delete manual adjacency
		adjTablePanel.getTable().addMouseListener(new MouseAdapter() {
			// Some platforms (like LINUX only detect the mouse pressed)
			@Override
			public void mousePressed(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showPopupAdjList((Component) me.getSource(), me.getX(), me.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showPopupAdjList((Component) me.getSource(), me.getX(), me.getY());
				}
			}
		});

		JPanel tablePanels = new JPanel(new GridLayout(3, 1));
		tablePanels.add(consolidatedLinksTablePanel.getWrappedPanel(title1));
		tablePanels.add(eclipsedTablePanel.getWrappedPanel(title2));
		tablePanels.add(adjTablePanel.getWrappedPanel(title3));

		JPanel fullPanel = new JPanel(new BorderLayout());
		fullPanel.add(tablePanels, BorderLayout.CENTER);
		fullPanel.add(buttonPanel, BorderLayout.SOUTH);

		return fullPanel;
	}

	private void clearTables() {
		((DefaultTableModel) adjTablePanel.getTable().getModel()).setDataVector(
		    new Vector<Vector<Object>>(), adjColumns);
		adjTablePanel.updateTable();

		((DefaultTableModel) eclipsedTablePanel.getTable().getModel())
		    .setDataVector(new Vector<Vector<Object>>(), linksColumns);
		eclipsedTablePanel.updateTable();

		((DefaultTableModel) consolidatedLinksTablePanel.getTable().getModel())
		    .setDataVector(new Vector<Vector<Object>>(), linksColumns);
		consolidatedLinksTablePanel.updateTable();
	}

	private void showPopup(Component source, int x, int y) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem findLink = new JMenuItem("Find Link");

		if (consolidatedLinksTablePanel.getTable().getSelectedRowCount() == 1) {
			popup.add(findLink);

			// Column is hidden; use model to pull value out...
			DefaultTableModel tm = (DefaultTableModel) consolidatedLinksTablePanel
			    .getTable().getModel();
			Vector<?> columnData = tm.getDataVector();
			int modelRow = consolidatedLinksTablePanel.getTable()
			    .convertRowIndexToModel(
			        consolidatedLinksTablePanel.getTable().getSelectedRow());
			final Vector<?> rowData = (Vector<?>) columnData.elementAt(modelRow);
			final String edgeId = (String) rowData.elementAt(edgeIdColumnIdx);
			final DracEdge edge = NeCache.INSTANCE.getEdgeById(edgeId);

			findLink.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Element element = new Element("edge");
						element.setAttribute("id", edgeId);
						List<Element> edgeList = new ArrayList<Element>();
						edgeList.add(element);
						NetworkGraph.INSTANCE.highlightPath(edgeList);
					}
					catch (Exception e1) {
						log.error("TopologyPanel error.", e1);
					}
				}
			});

			AbstractAction removeLink = new AbstractAction("Remove link") {
				/**
                 * 
                 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					HashMap<String, String> args = new HashMap<String, String>();
					args.put("SRCNEID", (String) rowData.elementAt(edgeSrcIeeeColumnIdx));
					args.put("DSTNEID", (String) rowData.elementAt(edgeDstIeeeColumnIdx));
					args.put("SRCPORT", (String) rowData.elementAt(edgeSrcAidColumnIdx));
					args.put("DSTPORT", (String) rowData.elementAt(edgeDstAidColumnIdx));
					desktop.getJungTopologyPanel().notifyListeners(
					    TopologyViewEventHandler.EVENT_TYPE.REMOVELINK_EVENT, args);
				}
			};
			removeLink
			    .setEnabled("Y".equals(rowData.elementAt(edgeIsManualColumnIdx)));
			popup.add(removeLink);

			AbstractAction generateManual = new AbstractAction("Generate manual link") {
				/**
                 * 
                 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					HashMap<String, String> args = new HashMap<String, String>();
					args.put("SRCNEIEEE", edge.getSource().getIeee());
					args.put("DSTNEIEEE", edge.getTarget().getIeee());
					args.put("SRCPORT", edge.getSourceAid());
					args.put("DSTPORT", edge.getTargetAid());
					desktop.getJungTopologyPanel().notifyListeners(
					    TopologyViewEventHandler.EVENT_TYPE.GENERATEMANUALLINK_EVENT,
					    args);
				}
			};

			generateManual.setEnabled(!edge.isManual()
			    && !edge.hasEclipsedManualLink());
			popup.add(generateManual);

			popup.show(source, x, y);
		}

	}

	// Delete manual adjacency
	private void showPopupAdjList(Component source, int x, int y) {
		JPopupMenu popup = new JPopupMenu();

		JMenuItem deleteAdj = new JMenuItem("Delete Adjacency");

		if (adjTablePanel.getTable().getSelectedRowCount() == 1) {
			popup.add(deleteAdj);

			DefaultTableModel tm = (DefaultTableModel) adjTablePanel.getTable()
			    .getModel();
			Vector<?> columnData = tm.getDataVector();

			int modelRow = adjTablePanel.getTable().convertRowIndexToModel(
			    adjTablePanel.getTable().getSelectedRow());
			final Vector<?> rowData = (Vector<?>) columnData.elementAt(modelRow);
			final String neIEEE = (String) rowData.elementAt(adjNeIeeeColumnIdx);
			final String port = (String) rowData.elementAt(adjPortColumnIdx);

			deleteAdj.setEnabled("Y".equals(rowData.elementAt(adjManualColumnIdx)));

			deleteAdj.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if (!(JOptionPane.YES_OPTION == JOptionPane
						    .showConfirmDialog(
						        desktop,
						        "<HTML>The selected adjacency will be removed. <br><B>The associated link(s) may be removed from the routing domain.</B>.<br><br>Are you sure you wish to continue?</HTML>",
						        "Remove manual adjacency", JOptionPane.YES_NO_OPTION))) {
							return;
						}

						new ServerOperation().deleteAdjacency(neIEEE, port);

					}
					catch (Exception e1) {
						log.error("TopologyPanel error.", e1);
					}
				}
			});

			popup.show(source, x, y);
		}
	}

	private void showPopupEclipsedLinks(Component source, int x, int y) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem findNetworkLink = new JMenuItem("Find Network Link Overlay");

		if (eclipsedTablePanel.getTable().getSelectedRowCount() == 1) {
			popup.add(findNetworkLink);

			// Column is hidden; use model to pull value out...
			DefaultTableModel tm = (DefaultTableModel) eclipsedTablePanel.getTable()
			    .getModel();
			Vector<?> columnData = tm.getDataVector();
			int modelRow = eclipsedTablePanel.getTable().convertRowIndexToModel(
			    eclipsedTablePanel.getTable().getSelectedRow());
			final Vector<?> rowData = (Vector<?>) columnData.elementAt(modelRow);
			final String edgeId = (String) rowData.elementAt(edgeIdColumnIdx);

			findNetworkLink.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Element element = new Element("edge");
						element.setAttribute("id", edgeId);
						List<Element> edgeList = new ArrayList<Element>();
						edgeList.add(element);
						NetworkGraph.INSTANCE.highlightPath(edgeList);

						// auto-select the link in the other table
						Vector<?> tableData = ((DefaultTableModel) consolidatedLinksTablePanel
						    .getTable().getModel()).getDataVector();

						for (int i = 0; i < tableData.size(); i++) {
							Vector<?> rowData = (Vector<?>) tableData.elementAt(i);
							String rowEdgeId = (String) rowData.elementAt(edgeIdColumnIdx);

							if (rowEdgeId.equals(edgeId)) {
								consolidatedLinksTablePanel.getTable().clearSelection();

								int viewIdx = consolidatedLinksTablePanel.getTable()
								    .convertRowIndexToView(i);
								consolidatedLinksTablePanel.getTable().setRowSelectionInterval(
								    viewIdx, viewIdx);

								final Rectangle rect = consolidatedLinksTablePanel.getTable()
								    .getCellRect(viewIdx, 1, false);

								EventQueue.invokeLater(new Runnable() {
									@Override
									public void run() {
										consolidatedLinksTablePanel.getScrollPane()
										    .scrollRectToVisible(rect);
									}
								});

								break;
							}
						}
					}
					catch (Exception e1) {
						log.error("TopologyPanel error.", e1);
					}
				}
			});

			AbstractAction removeLink = new AbstractAction("Remove link") {
				/**
                 * 
                 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					HashMap<String, String> args = new HashMap<String, String>();
					args.put("SRCNEID", (String) rowData.elementAt(edgeSrcIeeeColumnIdx));
					args.put("DSTNEID", (String) rowData.elementAt(edgeDstIeeeColumnIdx));
					args.put("SRCPORT", (String) rowData.elementAt(edgeSrcAidColumnIdx));
					args.put("DSTPORT", (String) rowData.elementAt(edgeDstAidColumnIdx));
					desktop.getJungTopologyPanel().notifyListeners(
					    TopologyViewEventHandler.EVENT_TYPE.REMOVELINK_EVENT, args);
				}
			};
			removeLink
			    .setEnabled("Y".equals(rowData.elementAt(edgeIsManualColumnIdx)));
			popup.add(removeLink);

			popup.show(source, x, y);
		}

	}

	private void updateTables() {
		// Network Adjacencies (raw)
		Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
		if (adjacenciesList != null) {
			for (NetworkElementAdjacency adj : adjacenciesList) {
				Vector<Object> row = new Vector<Object>();

				String s = adj.getNeid();
				DracVertex v = NeCache.INSTANCE.getVertex(s.toUpperCase());
				row.add(v != null ? v.getLabel() : s);
				row.add(adj.getPort());
				row.add(adj.getTxTag());
				row.add(adj.getRxTag());
				row.add(adj.getType());
				row.add(adj.isManual() ? "Y" : "N");
				row.add(v != null ? v.getIeee() : s);

				rows.add(row);
			}

			((DefaultTableModel) adjTablePanel.getTable().getModel()).setDataVector(
			    rows, adjColumns);

			// Hide some columns
			adjTablePanel
			    .getTable()
			    .getColumnModel()
			    .removeColumn(
			        adjTablePanel.getTable().getColumnModel()
			            .getColumn(adjStartOfHideColumnsIdx));

			// Highlight missing Rx tags in raw topology data:
			TableColumn col = adjTablePanel.getTable().getColumnModel()
			    .getColumn(rxTagColumnIdx);
			if (col != null) {
				col.setCellRenderer(new EmptyDataRenderer());
			}

			adjTablePanel.updateTable();
		}

		// Consolidated Links and Eclipsed Links

		try {
			Graph<DracVertex, DracEdge> graph = NetworkGraph.INSTANCE.getGraph();

			// Consolidated Links
			List<DracEdge> edges = new ArrayList<DracEdge>(graph.getEdges());
			rows = new Vector<Vector<Object>>();
			for (DracEdge edge : edges) {
				Vector<Object> row = new Vector<Object>();

				row.add(edge.getSource().getLabel());
				row.add(edge.getSourceAid());
				row.add(edge.getTarget().getLabel());
				row.add(edge.getTargetAid());
				row.add(edge.isManual() ? "Y" : "N");
				row.add(edge.getSourceChannel());
				row.add(edge.getIngressIp());
				row.add(edge.getTargetChannel());
				row.add(edge.getEgressIp());
				row.add(edge.getCost());
				row.add(edge.getMetric());
				row.add(edge.getWeight().toString());
				row.add(edge.getSrlg());

				row.add(edge.getID());
				row.add(edge.getSource().getIeee());
				row.add(edge.getTarget().getIeee());

				rows.add(row);
			}

			((DefaultTableModel) consolidatedLinksTablePanel.getTable().getModel())
			    .setDataVector(rows, linksColumns);

			// Hide some columns
			consolidatedLinksTablePanel
			    .getTable()
			    .getColumnModel()
			    .removeColumn(
			        consolidatedLinksTablePanel.getTable().getColumnModel()
			            .getColumn(edgeStartOfHideColumnsIdx));
			consolidatedLinksTablePanel
			    .getTable()
			    .getColumnModel()
			    .removeColumn(
			        consolidatedLinksTablePanel.getTable().getColumnModel()
			            .getColumn(edgeStartOfHideColumnsIdx));
			consolidatedLinksTablePanel
			    .getTable()
			    .getColumnModel()
			    .removeColumn(
			        consolidatedLinksTablePanel.getTable().getColumnModel()
			            .getColumn(edgeStartOfHideColumnsIdx));

			consolidatedLinksTablePanel.updateTable();

			// Eclipsed Links
			List<DracEdge> eclipsedEdges = new ArrayList<DracEdge>(NeCache
			    .INSTANCE.getAllEclipsedEdges());
			rows = new Vector<Vector<Object>>();
			for (DracEdge edge : eclipsedEdges) {
				Vector<Object> row = new Vector<Object>();

				row.add(edge.getSource().getLabel());
				row.add(edge.getSourceAid());
				row.add(edge.getTarget().getLabel());
				row.add(edge.getTargetAid());
				row.add(edge.isManual() ? "Y" : "N");
				row.add(edge.getSourceChannel());
				row.add(edge.getIngressIp());
				row.add(edge.getTargetChannel());
				row.add(edge.getEgressIp());
				row.add(edge.getCost());
				row.add(edge.getMetric());
				row.add(edge.getWeight().toString());
				row.add(edge.getSrlg());

				row.add(edge.getID());
				row.add(edge.getSource().getIeee());
				row.add(edge.getTarget().getIeee());

				rows.add(row);
			}

			((DefaultTableModel) eclipsedTablePanel.getTable().getModel())
			    .setDataVector(rows, linksColumns);

			// Hide some columns
			eclipsedTablePanel
			    .getTable()
			    .getColumnModel()
			    .removeColumn(
			        eclipsedTablePanel.getTable().getColumnModel()
			            .getColumn(edgeStartOfHideColumnsIdx));
			eclipsedTablePanel
			    .getTable()
			    .getColumnModel()
			    .removeColumn(
			        eclipsedTablePanel.getTable().getColumnModel()
			            .getColumn(edgeStartOfHideColumnsIdx));
			eclipsedTablePanel
			    .getTable()
			    .getColumnModel()
			    .removeColumn(
			        eclipsedTablePanel.getTable().getColumnModel()
			            .getColumn(edgeStartOfHideColumnsIdx));

			eclipsedTablePanel.updateTable();
		}
		catch (Exception e) {
			log.error("TopologyPanel error.", e);
		}
	}
}
