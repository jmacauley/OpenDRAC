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
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
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
import javax.swing.SwingWorker;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeCache;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.DracTableModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.DesktopUtil;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.AidCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TidCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TrueFalseRenderer;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.AuditResult;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;

/**
 * @author pitman
 */
public final class AuditPanel {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	/*
	 * Mouse listener, we add and remove the listener from the table depending on
	 * what we are displaying.
	 */
	class MouseListenerforTable extends MouseAdapter {
		// Some platforms only detect when mouse is pressed (Linux)
		@Override
		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showAuditPopup((Component) me.getSource(), me.getX(), me.getY());
			}
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showAuditPopup((Component) me.getSource(), me.getX(), me.getY());
			}
		}
	}

	private static final String AUDIT_SRC_NEID_STR = "Source NE ID";

	private static final String AUDIT_TGT_NEID_STR = "Destination NE ID";
	private static final String AUDITMODEL_NEID_COL = "NE";

	private static final String AUDITMODEL_PORT_COL = "Port";

	private static final String AUDITMODEL_MISMATCH_COL = "Mismatched";
	private static final String ALL_CONS_STR = "All network connections";
	private static final String ORPHANED_CONS_STR = "Orphaned OpenDRAC connections";
	private static final String OSS_OVERLAP_STR = "OSS connections using OpenDRAC bandwidth";

	private static final String STRANDED_BW_STR = "OpenDRAC to network mismatch";
	private static final String DLT_CON_TITLE = "Delete connection";

	private static final String DLT_XCON_WARN_STR = "Traffic carrying connections have been selected for cancellation.  THIS IS A TRAFFIC AFFECTING OPERATION.  Are you sure you wish to continue?";
	private JDialog delConnectionsDialog;

	private final OpenDracDesktop desktop;

	private final List<String> basicAuditColumns = Arrays.asList(new String[] {
	    "Connection label", AUDIT_SRC_NEID_STR, "Source AID", AUDIT_TGT_NEID_STR,
	    "Destination AID", "Rate", "Connection Type" });

	private final List<String> advancedAuditColumns = Arrays.asList(new String[] {
	    AUDITMODEL_NEID_COL, AUDITMODEL_PORT_COL, "Rate",
	    AUDITMODEL_MISMATCH_COL, "Model view", "Network view" });

	private final TablePanel tablePanel = new TablePanel(
	    new DracTableModel<String>(null, basicAuditColumns), null, null);

	private final MouseListenerforTable mouseListener = new MouseListenerforTable();

	public AuditPanel(OpenDracDesktop d) {
		desktop = d;
	}

	public JPanel buildAuditPanel() {
		JLabel filterLabel = new JLabel("Audit:");
		final JComboBox jcb = new JComboBox();
		jcb.addItem(ALL_CONS_STR);
		jcb.addItem(ORPHANED_CONS_STR);
		jcb.addItem(OSS_OVERLAP_STR);
		jcb.addItem(STRANDED_BW_STR);

		final JButton retrieveButton = new JButton("Retrieve");
		retrieveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					retrieveButton.setEnabled(false);

					((DracTableModel<String>) tablePanel.getTable().getModel())
					    .clearTable();

					desktop.showProgressDialog("Performing audit...");
					if (((String) jcb.getSelectedItem())
					    .equalsIgnoreCase(ORPHANED_CONS_STR)) {
						tablePanel.getTable().removeMouseListener(mouseListener);
						tablePanel.getTable().addMouseListener(mouseListener);
						getOrphanedConnections();
					}
					else if (((String) jcb.getSelectedItem())
					    .equalsIgnoreCase(OSS_OVERLAP_STR)) {
						tablePanel.getTable().removeMouseListener(mouseListener);
						tablePanel.getTable().addMouseListener(mouseListener);
						getOSSConnections();
					}
					else if (((String) jcb.getSelectedItem()).equals(ALL_CONS_STR)) {
						tablePanel.getTable().removeMouseListener(mouseListener);
						getAllConnections();
					}
					else {
						getPortMismatches();
					}
				}
				finally {
					retrieveButton.setEnabled(true);
				}
			}
		});

		tablePanel.addButton(filterLabel);
		tablePanel.addButton(jcb);
		tablePanel.addButton(tablePanel.getExportButton(desktop, null));
		tablePanel.addButton(retrieveButton);
		return tablePanel.getWrappedPanel("Audit");
	}

	private String decodeBits(BigInteger bigInt) {

		StringBuilder result = new StringBuilder();

		try {

			for (int i = 0; i < bigInt.bitLength(); i++) {
				if (bigInt.testBit(i)) {
					if (!result.toString().equals("")) {
						result.append(",  ");
					}
					result.append("" + i);
				}
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}

		return result.toString();
	}

	private void displayConnections(List<CrossConnection> xcons) {
		try {
			desktop.setCursor(OpenDracDesktop.WAIT_CURSOR);

			((DracTableModel<String>) tablePanel.getTable().getModel()).clearTable();

			List<List<String>> rows = new ArrayList<List<String>>();
			List<String> row = null;

			try {
				desktop.setCursor(OpenDracDesktop.WAIT_CURSOR);

				for (CrossConnection c : xcons) {
					row = new ArrayList<String>();
					row.add(c.getId());
					row.add(c.getSourceNeId());
					row.add(c.getSourceXcAid());
					row.add(c.getTargetNeId());
					row.add(c.getTargetXcAid());
					row.add(c.getRate());
					row.add(c.getType());

					rows.add(row);
				}
			}
			catch (Exception e) {
				log.error("Error: ", e);
			}

			((DracTableModel<String>) tablePanel.getTable().getModel()).setData(rows);

			TableColumn col = tablePanel.getTable().getColumn(AUDIT_SRC_NEID_STR);
			if (col != null) {
				col.setCellRenderer(new TidCellRenderer(NeCache.INSTANCE
				    .getIeeeLabelMap()));
			}

			col = tablePanel.getTable().getColumn(AUDIT_TGT_NEID_STR);
			if (col != null) {
				col.setCellRenderer(new TidCellRenderer(NeCache.INSTANCE
				    .getIeeeLabelMap()));
			}

			tablePanel.updateTable();
			desktop.hideProgressDialog();
		}
		finally {
			desktop.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
		}
	}

	private List<CrossConnection> getActiveOSSConnections() {

		try {
			Map<String, String> filter = new HashMap<String, String>();
			/*
			 * Retrieve connections whose Ids do NOT start with "DRAC-" ... emulate
			 * the query: where not(starts-with($a/@id, \"DRAC-\")
			 */
			filter.put(DbKeys.NetworkElementConnectionCols.ID_NOT, "DRAC-%");
			return desktop.getNRBHandle().getNetworkElementConnections(
			    desktop.getLoginToken(), filter);
		}
		catch (Exception e) {
			log.error("OpenDracDesktop::getActiveOSSConnections failed.", e);
		}

		return null;
	}

	private void getAllConnections() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					desktop.showProgressDialog("<HTML>Fetching connections</HTML>");
					List<CrossConnection> xcons = desktop.getNRBHandle()
					    .getNetworkElementConnections(desktop.getLoginToken(), null);
					displayConnections(xcons);
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

	private Map<String, BigInteger> getAllConstraints() {
		Map<String, BigInteger> constraintsMap = new ServerOperation()
		    .getAllConstraints();
		
		return constraintsMap;
	}

	private Facility getFacility(String neid, String aid) {
		try {
			Map<String, String> filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, neid);
			filter.put(DbKeys.NetworkElementFacilityCols.AID, aid);
			List<Facility> facs = desktop.getNRBHandle().getNetworkElementFacilities(
			    desktop.getLoginToken(), filter);
			if (facs != null && facs.size() == 1) {
				return facs.get(0);
			}
		}
		catch (Exception e) {
			//
		}

		log.error("AuditPanel::getFacility failed to retrieve for: " + neid + " "
		    + aid);
		return null;
	}

	private Map<String, String> getInprogressCalls() throws Exception {

		Map<String, String> inprogressCallMap = new HashMap<String, String>();
		List<String> inprogressCalls = desktop.getNRBHandle().getInprogressCalls(
		    desktop.getLoginToken());

		if (inprogressCalls != null) {
			for (String id : inprogressCalls) {
				inprogressCallMap.put(id, id);
			}
		}

		return inprogressCallMap;
	}

	private void getOrphanedConnections() {

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, String> inprogressCalls = getInprogressCalls();
					Map<String, String> filter = new HashMap<String, String>();
					// STARTS WITH "DRAC-"
					filter.put(DbKeys.NetworkElementConnectionCols.ID, "DRAC-%");
					List<CrossConnection> activeXCONS = desktop.getNRBHandle()
					    .getNetworkElementConnections(desktop.getLoginToken(), filter);

					List<CrossConnection> orphanedConnections = new ArrayList<CrossConnection>();

					if (activeXCONS != null) {
						for (CrossConnection xcon : activeXCONS) {
							String xconId = xcon.getId();

							if (inprogressCalls.get(xconId) == null) {
								// Ignore loopbacks
								if (!"DRAC-loopback".equals(xconId)) {
									
									orphanedConnections.add(xcon);
								}
								else {
									
								}
							}
						}

						displayConnections(orphanedConnections);

					}
				}
				catch (Exception e) {
					log.error("OpenDracDesktop::getOrphanedConnections failed.", e);
					JOptionPane.showMessageDialog(desktop,
					    "Exception retrieving orphaned connections.");
				}

			}
		});
		t.setDaemon(true);
		t.start();

	}

	private void getOSSConnections() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, BigInteger> constraintsCache = getAllConstraints();
					List<CrossConnection> ossConnections = new ArrayList<CrossConnection>();
					long startTime = System.currentTimeMillis();
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

					sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

					List<CrossConnection> xcons = getActiveOSSConnections();

					for (int i = 0; i < xcons.size(); i++) {
						int srcChannel = -1;
						int dstChannel = -1;

						long elapsedTime = System.currentTimeMillis() - startTime;
						desktop.showProgressDialog("<HTML>Auditing connection: " + (i + 1)
						    + " of " + xcons.size() + "<BR>Elapsed time: "
						    + sdf.format(elapsedTime) + "</HTML>");
						CrossConnection xcon = xcons.get(i);

						String neid = xcon.getSourceNeId();
						String srcAid = xcon.getSourceXcAid();
						String dstAid = xcon.getTargetXcAid();

						// Strip off channel number
						srcAid = srcAid.substring(0, srcAid.lastIndexOf("-"));
						dstAid = dstAid.substring(0, dstAid.lastIndexOf("-"));

						Facility srcFac = getFacility(neid, srcAid);
						Facility dstFac = getFacility(neid, dstAid);

						if (srcFac == null || dstFac == null) {
							log.error("AuditPanel getOssConnections failed to retrieve facility info");
							continue;
						}

						String srcSigType = srcFac.getSigType();
						String dstSigType = dstFac.getSigType();

						boolean srcFacDracDoesntCare = "OSS".equalsIgnoreCase(srcSigType)
						    || "unassigned".equalsIgnoreCase(srcSigType);
						boolean dstFacDracDoesntCare = "OSS".equalsIgnoreCase(dstSigType)
						    || "unassigned".equalsIgnoreCase(dstSigType);

						String sSrcChannel = xcon.getSourceChannel();
						String sDstChannel = xcon.getTargetChannel();

						String rate = xcon.getRate();
						int channels = DesktopUtil.getChannelsForRate(rate);

						try {
							srcChannel = Integer.parseInt(sSrcChannel);
							dstChannel = Integer.parseInt(sDstChannel);
						}
						catch (Exception e) {
							log.error("Error: ", e);
							continue;
						}

						boolean overlap = false;

						// Check Src side
						if (!srcFacDracDoesntCare) {
							BigInteger srcConstraints = constraintsCache.get(neid + "_"
							    + srcAid);
							for (int j = srcChannel; j < srcChannel + channels; j++) {
								// constraint bit == false means it's assigned to DRAC
								if (!srcConstraints.testBit(j)) {
									overlap = true;
									break;
								}
							}
						}

						if (!dstFacDracDoesntCare) {
							// Check Dst side
							BigInteger dstConstraints = constraintsCache.get(neid + "_"
							    + dstAid);
							for (int j = dstChannel; j < dstChannel + channels; j++) {
								// constraint bit == false means it's assigned to DRAC
								if (!dstConstraints.testBit(j)) {
									overlap = true;
									break;
								}
							}
						}

						if (overlap) {
							ossConnections.add(xcon);
						}
					}

					displayConnections(ossConnections);
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
			}

		});
		t.setDaemon(true);
		t.start();
	}

	private void getPortMismatches() {
		final TablePanel advancedTablePanel = new TablePanel(
		    new DracTableModel<String>(null, advancedAuditColumns), null, null);

		final String warningString = "Warning: An audit operation may take some time to complete.  Continue?";
		final String warningCorrectString = "Warning: Correcting a mismatch should only be done as a last resort and will take some time to complete. Continue?";
		final String mismatchString = "Mismatch detected.  Correct model view to match network view?";

		final JDialog auditDialog = new JDialog(desktop);
		auditDialog.setTitle("Audit model");
		auditDialog.add(advancedTablePanel.getWrappedPanel("Audit"));
		auditDialog.setSize(800, 500);
		auditDialog.setLocationRelativeTo(desktop);

		desktop.hideProgressDialog();

		JButton advancedAuditButton = new JButton("Audit");
		advancedTablePanel.addButton(advancedAuditButton);
		advancedAuditButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {

				int option = -1;
				Thread auditModelThread = new Thread() {
					@Override
					public void run() {
						List<List<String>> rows = new ArrayList<List<String>>();
						List<String> row = null;
						boolean mismatchFound = false;

						try {
							desktop.showProgressDialog("Performing audit...");
							List<AuditResult> result = new ServerOperation().auditModel();
							if (result != null) {
								for (AuditResult ar : result) {

									row = new ArrayList<String>();
									row.add(ar.getNeId());
									row.add(ar.getAid());
									row.add(ar.getRate());
									row.add(Boolean.toString(ar.isMismatched()));
									row.add(decodeBits(ar.getRawTracker()));
									row.add(decodeBits(ar.getConnectionTracker()));
									if (ar.isMismatched()) {
										mismatchFound = true;
									}
									rows.add(row);

								}
								((DracTableModel<String>) advancedTablePanel.getTable()
								    .getModel()).setData(rows);

								Map<String, String> allNesMap = NeCache.INSTANCE
								    .getIeeeLabelMap();
								TableColumn col = advancedTablePanel.getTable().getColumn(
								    AUDITMODEL_NEID_COL);
								if (col != null) {
									col.setCellRenderer(new TidCellRenderer(allNesMap));
								}

								col = advancedTablePanel.getTable().getColumn(
								    AUDITMODEL_PORT_COL);
								if (col != null) {
									col.setCellRenderer(new AidCellRenderer(allNesMap));
								}

								col = advancedTablePanel.getTable().getColumn(
								    AUDITMODEL_MISMATCH_COL);
								if (col != null) {
									col.setCellRenderer(new TrueFalseRenderer());
								}

								// col = auditTable.getColumn( AUDITMODEL_MODELVIEW_COL );
								// if ( col != null ) col.setCellRenderer( new
								// PortCellRenderer());

								advancedTablePanel.getTable().setAutoResizeMode(
								    JTable.AUTO_RESIZE_OFF);
								advancedTablePanel.updateTable();
								desktop.hideProgressDialog();

								if (mismatchFound) {
									int option = JOptionPane.showConfirmDialog(auditDialog,
									    mismatchString, "Error", JOptionPane.OK_CANCEL_OPTION);
									if (option == JOptionPane.OK_OPTION) {
										option = JOptionPane.showConfirmDialog(auditDialog,
										    warningCorrectString, "Warning",
										    JOptionPane.OK_CANCEL_OPTION);
										if (option == JOptionPane.OK_OPTION) {
											desktop.showProgressDialog("Correcting mismatch...");

											try {
												new ServerOperation().correctModel();

												desktop.hideProgressDialog();

												JOptionPane.showMessageDialog(auditDialog,
												    "Mismatch corrected successfully");
												auditDialog.setVisible(false);
												auditDialog.dispose();
											}
											catch (Exception e) {
												log.error("Error: ", e);
												JOptionPane.showMessageDialog(auditDialog,
												    "Mismatch correction failed: " + e.getMessage());
											}
										}
									}
								}
								else {
									JOptionPane.showMessageDialog(auditDialog,
									    "No mismatches detected.");
								}
							}

						}
						catch (Exception e) {

							log.error("Error: ", e);
							desktop.hideProgressDialog();
						}

					}
				};

				desktop.hideProgressDialog();
				option = JOptionPane.showConfirmDialog(auditDialog, warningString,
				    "Warning", JOptionPane.OK_CANCEL_OPTION,
				    JOptionPane.WARNING_MESSAGE);

				if (option == JOptionPane.OK_OPTION) {
					auditModelThread.setDaemon(true);
					auditModelThread.start();
				}
			}
		});

		JButton advancedCloseButton = new JButton("Close");
		advancedTablePanel.addButton(advancedCloseButton);
		advancedCloseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				auditDialog.setVisible(false);
				auditDialog.dispose();
			}
		});

		auditDialog.setVisible(true);
	}

	private void sendDeleteConnection(final JTable table, final int row,
	    final int column, final CrossConnection xcon) {
		final String command = "deleteConnection for xcon" + xcon;
		log.debug("Sending: " + command);

		class Worker extends SwingWorker<Object, Object> {

			@Override
			public Object doInBackground() {
				try {
					new ServerOperation().deleteConnection(xcon);
					return null;
				}
				catch (Exception e) {
					return e;
				}
			}

			@Override
			protected void done() {
				try {
					Object rc = get();
					if (rc == null) {
						table.setValueAt("Deleted", row, column);
					}
					else {

						table.setValueAt("Failed: " + ((Exception) rc).getMessage(), row,
						    column);
					}
				}
				catch (Exception e) {
					log.error("Failed to delete connection!", e);
					table.setValueAt("Failed: " + e.getMessage(), row, column);
				}
			}
		}

		new Worker().execute();

	}

	private void showAuditPopup(Component source, int x, int y) {
		/**
		 * @TODO: don't display the delete menu if its not a connection that we want
		 *        to let users delete. In particular on the show all connections
		 *        page, don't permit users to delete drac loopback or drac-xxx
		 *        connections that are not orphaned.
		 */
		JPopupMenu popup = new JPopupMenu();
		JMenuItem itemDelete = new JMenuItem("Delete...");

		itemDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				showDeleteConnectionsDialog();
			}
		});

		popup.add(itemDelete);
		popup.show(source, x, y);
	}

	private void showDeleteConnectionsDialog() {
		JLabel warningLabel = new JLabel(DLT_XCON_WARN_STR);
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
		JPanel buttonEastPanel = new JPanel();
		final JTable deleteTable = new JTable();
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		JScrollPane jsp = null;
		// boolean trafficDelete = false;
		List<String> columns = Arrays.asList(new String[] { "Status",
		    "Connection label", AUDIT_SRC_NEID_STR, "Source AID",
		    AUDIT_TGT_NEID_STR, "Destination AID", "Rate", "Connection Type" });

		delConnectionsDialog = new JDialog(desktop);

		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {

				desktop.showProgressDialog("Deleting connection(s)...");
				for (int i = 0; i < deleteTable.getModel().getRowCount(); i++) {
					deleteTable.setValueAt("Deleting...", i, 0);

					Map<String, String> xcMap = new HashMap<String, String>();
					xcMap.put(CrossConnection.SOURCE_NEID,
					    (String) deleteTable.getValueAt(i, 2));
					xcMap.put(CrossConnection.TARGET_NEID,
					    (String) deleteTable.getValueAt(i, 4));
					xcMap.put(CrossConnection.SOURCE_XC_AID,
					    (String) deleteTable.getValueAt(i, 3));
					xcMap.put(CrossConnection.TARGET_XC_AID,
					    (String) deleteTable.getValueAt(i, 5));
					xcMap.put(CrossConnection.RATE, (String) deleteTable.getValueAt(i, 6));
					xcMap.put(CrossConnection.CCT_TYPE,
					    (String) deleteTable.getValueAt(i, 7));

					sendDeleteConnection(deleteTable, i, 0, new CrossConnection(xcMap));
					DesktopUtil.sizeColumns(deleteTable);
				}

				Thread deleteMonitor = new Thread(new Runnable() {
					@Override
					public void run() {
						boolean done = false;
						boolean allSuccess = true;
						while (!done) {
							done = true;
							allSuccess = true;
							for (int i = 0; i < deleteTable.getRowCount(); i++) {
								if ("Active".equalsIgnoreCase((String) deleteTable.getValueAt(
								    i, 0))
								    || "Deleting...".equalsIgnoreCase((String) deleteTable
								        .getValueAt(i, 0))) {
									done = false;
								}
								if (!"Deleted".equalsIgnoreCase((String) deleteTable
								    .getValueAt(i, 0))) {
									allSuccess = false;
								}
							}
							Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
						}
						desktop.hideProgressDialog();
						if (allSuccess) {
							JOptionPane.showMessageDialog(delConnectionsDialog,
							    "All connections deleted successfully");
						}
						else {
							JOptionPane.showMessageDialog(delConnectionsDialog,
							    "Some connections could not be deleted.");
						}
						if (allSuccess) {
							delConnectionsDialog.setVisible(false);
							delConnectionsDialog.dispose();
						}
					}
				});
				deleteMonitor.setDaemon(true);
				deleteMonitor.start();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				delConnectionsDialog.setVisible(false);
				delConnectionsDialog.dispose();
			}
		});

		int selectedRows[] = tablePanel.getTable().getSelectedRows();

		List<List<String>> rows = new ArrayList<List<String>>();

		for (int selectedRow : selectedRows) {
			List<String> row = new ArrayList<String>();
			for (int j = 0; j < columns.size(); j++) {
				if (j == 0) {
					row.add("Active");
				}
				else {
					row.add((String) tablePanel.getTable().getValueAt(selectedRow,
					    (j - 1)));
				}
			}
			rows.add(row);
		}

		deleteTable.setModel(new DracTableModel<String>(rows, columns));
		deleteTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		DesktopUtil.sizeColumns(deleteTable);

		jsp = new JScrollPane(deleteTable);

		delConnectionsDialog.setTitle(DLT_CON_TITLE);

		buttonEastPanel.add(okButton);
		buttonEastPanel.add(cancelButton);

		buttonPanel.add(buttonEastPanel, BorderLayout.EAST);

		mainPanel.add(warningLabel, BorderLayout.NORTH);
		mainPanel.add(jsp, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		delConnectionsDialog.setSize(800, 250);
		delConnectionsDialog.getContentPane().setLayout(
		    new GridLayout(1, 1, 10, 10));
		delConnectionsDialog.add(mainPanel);
		delConnectionsDialog.setLocationRelativeTo(desktop);

		delConnectionsDialog.setVisible(true);

	}
}
