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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.DracTableModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.DesktopUtil;

/**
 * A Table Panel contains a Jtext label, a JTable inside a scroll panel, a
 * status line and a button row. The idea being to keep the common layout and
 * presentation code in one place.
 * 
 * @author pitman
 */
public final class TablePanel {
  private final Logger log = LoggerFactory.getLogger(getClass());
	public interface CustomExportFormatterI {
		String getCustomExportString(String delim, String timestamp, String title,
		    TableModel tm);
	}

	static class BetterJTableTable extends JTable {
		private static final long serialVersionUID = 1L;

		public BetterJTableTable(TableModel tm, TableColumnModel cm,
		    ListSelectionModel sm) {
			super(tm, cm, sm);
		}

		/**
		 * getToolTipText Implements table cell tool tips. Shows the Tool Tip only
		 * if the full text is not visible.
		 * 
		 * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
		 * @param event
		 *          the MouseEvent
		 * @return The String for the tool tip, or null.
		 */
		@Override
		public String getToolTipText(MouseEvent event) {
			// get the row and column that the mouse is positioned over
			int row = rowAtPoint(event.getPoint());
			int column = columnAtPoint(event.getPoint());
			if (row == -1 || column == -1) {
				return null;
			}

			// get the value displayed in the cell
			Object cellObject = getValueAt(row, column);
			if (cellObject == null) {
				return null;
			}

			String cellText = cellObject.toString();

			if ("".equals(cellText)) {
				return null;
			}

			// Determine Component's preferred width is longer than the
			// visible cell width.
			// If so, return the string to be displayed as a Tool tip.
			TableCellRenderer rend = getCellRenderer(row, column);
			Component comp = rend.getTableCellRendererComponent(this, cellObject,
			    false, false, row, column);

			Rectangle visibleCellRect = getCellRect(row, column, true);
			if (comp.getPreferredSize().width > visibleCellRect.width) {
				return cellText;
			}
			return null;
		}

	}

	private static final String exportDelim = ";";
	private static final String exportFileFormat = "Semicolon separated values (txt)";
	private static final String exportFileExt = "txt";

	private final JLabel resultsLabel = new JLabel("Rows retrieved: 0");
	private final JPanel buttonPanel = new JPanel(new BorderLayout(1, 1));
	private final JTable table;
	private final JPanel eastButtonPanel = new JPanel();
	private final JPanel mainPanel;
	private final JScrollPane jsp;
	private String user;
	private JButton exportButton = null;
	private String title = null;

	private RowSorter<DracTableModel<Object>> rowSorter;
	private List<? extends RowSorter.SortKey> lastSorterKeys = null;
	@SuppressWarnings("unchecked")
	public TablePanel(TableModel model, TableColumnModel columns,
	    ListSelectionModel selection) {
		table = new BetterJTableTable(model, columns, selection);
		table.setAutoCreateRowSorter(true);

		this.rowSorter=(RowSorter<DracTableModel<Object>>)table.getRowSorter();
		lastSorterKeys =this.rowSorter.getSortKeys();
		this.rowSorter.addRowSorterListener(new RowSorterListener() {			
			@Override
			public void sorterChanged(RowSorterEvent e) {
				RowSorter sorter = e.getSource();
				lastSorterKeys = sorter.getSortKeys();				
				setCurrentSorter(sorter);				
			}
		});
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		table.getSelectionModel().addListSelectionListener(
		    new ListSelectionListener() {
			    @Override
			    public void valueChanged(ListSelectionEvent e) {
				    if (e.getValueIsAdjusting()) {
					    return;
				    }
				    updateSelection();
			    }

		    });

		jsp = new JScrollPane(table);
		buttonPanel.add(eastButtonPanel, BorderLayout.EAST);

		mainPanel = new JPanel(new BorderLayout(1, 1));
		mainPanel.add(resultsLabel, BorderLayout.NORTH);
		mainPanel.add(jsp, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		table.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("tableCellEditor".equals(evt.getPropertyName())) {
					return;
				}
				updateSelection();
			}
		});
		updateTable();
	}

	public void addButton(Component c) {
		eastButtonPanel.add(c);
	}

	public JButton getExportButton(final OpenDracDesktop desktop,
	    final CustomExportFormatterI formatter) {
		return this.getExportButton(desktop, formatter, null, null);
	}

	public JButton getExportButton(final OpenDracDesktop desktop,
	    final CustomExportFormatterI formatter, final String filename,
	    final Map<String, TableModel> tables) {
		if (exportButton == null) {
			exportButton = new JButton("Export");
			exportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					class Worker extends SwingWorker<Object, Object> {
						private final File file;

						public Worker(File inputFile) {
							file = inputFile;
						}

						@Override
						public Object doInBackground() {
							try {
								String timestamp = DesktopUtil.formatDateTime(
								    System.currentTimeMillis(), desktop.locale,
								    "yyyy-MM-dd HH:mm", desktop.getTimeZonePreference());

								if (file == null) {
									return null;
								}

								FileWriter out = null;
								try {
									out = new FileWriter(file);

									if (formatter == null) {
										defaultFormat(out, timestamp, tables);
									}
									else {
										out.write(formatter.getCustomExportString(exportDelim,
										    timestamp, title, table.getModel()));
									}

									out.flush();
								}
								finally {
									if (out != null) {
										out.close();
									}
								}
							}
							catch (Exception e) {
								log.error("Error exporting table.", e);
							}

							return null;
						}

						@Override
						protected void done() {
							exportButton.setEnabled(true);
							desktop.hideProgressDialog();
							desktop.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
						}

						private void defaultFormat(FileWriter out, String timestamp,
						    Map<String, TableModel> tableMap) throws Exception {
							// Date
							out.write("#");
							out.write(timestamp);
							out.write("\n");

							if (tableMap == null || tableMap.size() == 0) {
								tableMap = new HashMap<String, TableModel>();
								// If list wasn't provided, use this table
								tableMap.put(title, table.getModel());
							}

							for (Map.Entry<String, TableModel> entry : tableMap.entrySet()) {
								// Table title
								out.write("#");
								out.write(entry.getKey());
								out.write("\n");

								TableModel model = entry.getValue();

								// Column titles
								out.write("#");
								for (int j = 0; j < model.getColumnCount(); j++) {
									if (modelToViewColIndex(j) < 0) {
										// skip hidden/removed columns
										continue;
									}

									out.write(model.getColumnName(j) + exportDelim);
								}
								out.write("\n");

								// table data
								for (int i = 0; i < model.getRowCount(); i++) {
									for (int j = 0; j < model.getColumnCount(); j++) {
										if (modelToViewColIndex(j) < 0) {
											// skip hidden/removed columns
											continue;
										}

										String s = "";
										Object o = model.getValueAt(i, j);
										if (o != null) {
											s = o.toString();

											TableCellRenderer rend = table.getCellRenderer(i, j);
											if (rend != null) {
												Component c = rend.getTableCellRendererComponent(table,
												    o, false, false, i, j);

												if (c != null) {
													if (c instanceof JLabel) {
														s = ((JLabel) c).getText();
													}
												}
											}
										}

										out.write(s + exportDelim);
									}
									out.write("\n");
								}
							}
						}
					}

					File file = getOutputFile(desktop, filename);
					if (file != null) {
						desktop.setCursor(OpenDracDesktop.WAIT_CURSOR);
						desktop.showProgressDialog("Exporting data ...");
						new Worker(file).execute();
					}
				}
			});
		}

		return exportButton;
	}

	public JPanel getPanel() {
		return mainPanel;
	}

	public JScrollPane getScrollPane() {
		return jsp;
	}

	public JTable getTable() {
		return table;
	}

	public JPanel getWrappedPanel(String panelTitle) {
		this.title = panelTitle != null ? panelTitle : "";
		JPanel borderPanel = new JPanel(new BorderLayout(1, 1));
		borderPanel.setBorder(BorderFactory.createTitledBorder(panelTitle));
		borderPanel.add(this.getPanel(), BorderLayout.CENTER);
		JPanel outerPanel = new JPanel(new BorderLayout(1, 1));
		outerPanel.add(borderPanel, BorderLayout.CENTER);
		return outerPanel;
	}

	public void setResultsLabel(String newLabel) {
		user = newLabel;
	}

	public void updateTable() {
		DesktopUtil.sizeColumns(table);
		updateSelection();
	}

	private File getOutputFile(Component c, String filename) {
		File file = null;

		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Export facilities to file");
		fc.setFileFilter(new FileNameExtensionFilter(exportFileFormat,
		    exportFileExt));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		String defaultFile = filename != null ? filename + "."
		    : title != null ? title + "." : "DRACTableExport.";
		fc.setSelectedFile(new File(defaultFile + exportFileExt));
		fc.setAcceptAllFileFilterUsed(false);

		int returnVal = fc.showSaveDialog(c);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
		}

		return file;
	}

	// Converts a column index in the model to a visible column index.
	// Returns -1 if the index does not exist.
	private int modelToViewColIndex(int mColIndex) {
		for (int c = 0; c < table.getColumnCount(); c++) {
			TableColumn col = table.getColumnModel().getColumn(c);
			if (col.getModelIndex() == mColIndex) {
				return c;
			}
		}
		return -1;
	}

	private void updateSelection() {
		StringBuilder sb = new StringBuilder();
		sb.append("Rows retrieved: ");
		sb.append(table.getModel().getRowCount());
		sb.append(" ");
		if (user != null && !"".equals(user)) {
			sb.append(user);
			sb.append(" ");
		}

		if (table.getSelectedRowCount() > 1) {
			sb.append("(");
			sb.append(table.getSelectedRowCount());
			sb.append(" rows selected.)");

		}
		resultsLabel.setText(sb.toString());

	}

	private void setCurrentSorter(RowSorter<DracTableModel<Object>> sorter){
		this.rowSorter = sorter;
	}
	private RowSorter<DracTableModel<Object>> getCurrentSorter(){
		return this.rowSorter;
	}
	public void sortTable(List<? extends RowSorter.SortKey> previousSorterKeys){
		RowSorter<DracTableModel<Object>> theSorter =  getCurrentSorter();
		getTable().setRowSorter(theSorter);
		
		theSorter.setSortKeys(previousSorterKeys);
		theSorter.allRowsChanged();
	}

	public List<? extends RowSorter.SortKey> getLastSorterKeys() {
    	return lastSorterKeys;
    }

	public void setLastSorterKeys(List<? extends RowSorter.SortKey> lastSorterKeys) {
    	this.lastSorterKeys = lastSorterKeys;
    }
}
