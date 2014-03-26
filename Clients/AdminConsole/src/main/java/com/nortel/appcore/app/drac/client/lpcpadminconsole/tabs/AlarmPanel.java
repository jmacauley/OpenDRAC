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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.DracTableModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperationCallback;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.DateTimeCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;

/**
 * @author pitman
 */
public final class AlarmPanel implements ServerOperationCallback {
	private final OpenDracDesktop desktop;
	private final List<String> alarmColumns = Arrays.asList(new String[] {
	    "Service ID", "Alarm ID", "Alarm severity", "Alarm time",
	    "Alarm description" });
	private final static int TIME_COLUMN = 3;

	private final TablePanel tablePanel = new TablePanel(
	    new DracTableModel<String>(null, alarmColumns), null, null);

	public AlarmPanel(OpenDracDesktop d) {
		desktop = d;
	}

	public JPanel buildAlarmPanel() {
		JButton retrieveButton = new JButton("Retrieve");
		retrieveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {

				getAllAlarms();
			}
		});

		tablePanel.addButton(tablePanel.getExportButton(desktop, null));
		tablePanel.addButton(retrieveButton);
		return tablePanel.getWrappedPanel("Service Alarm Management");
	}

	@Override
	public void handleServerOperationResult(ServerOperation op) {
		Map<String, Object> result = op.getResult();
		

		if (op.getOperation() == ServerOperation.Operation.OP_GET_ALL_ALARMS) {
			Element alarmList = (Element) result.get(ServerOperation.MAP_RESULT_KEY);

			if (alarmList != null) {
				displayAlarms(alarmList);
			}
			else {
				JOptionPane.showMessageDialog(desktop,
				    "Error occurred retrieving alarms.", "Error",
				    JOptionPane.ERROR_MESSAGE);
			}
			desktop.hideProgressDialog();
		}

	}

	private void displayAlarms(Element alarms) {
		List<Element> alarmList = null;
		Element alarm = null;
		List<List<String>> rows = new ArrayList<List<String>>();
		List<String> row = null;

		try {
			if (alarms != null) {
				desktop.setCursor(OpenDracDesktop.WAIT_CURSOR);
				alarmList = alarms.getChildren("svcalarm");
				for (int i = 0; i < alarmList.size(); i++) {
					alarm = alarmList.get(i);
					row = new ArrayList<String>();
					row.add(alarm.getAttributeValue("serviceId"));
					row.add(alarm.getAttributeValue("id"));
					row.add(alarm.getAttributeValue("severity"));

					row.add(alarm.getAttributeValue("time"));
					row.add(alarm.getAttributeValue("description"));
					rows.add(row);
				}

				
				((DracTableModel<String>) tablePanel.getTable().getModel())
				    .setData(rows);

				TableColumn column = tablePanel.getTable().getTableHeader()
				    .getColumnModel().getColumn(TIME_COLUMN);
				if (column != null) {
					column.setCellRenderer(new DateTimeCellRenderer(desktop.locale,
					    desktop.getTimeZonePreference()));
				}

				tablePanel.updateTable();

				// Override the default column sizing
				TableColumn timeCol = tablePanel.getTable().getTableHeader()
				    .getColumnModel().getColumn(TIME_COLUMN);
				if (timeCol != null) {
					timeCol.setPreferredWidth(250);
				}

				desktop.hideProgressDialog();
			}
		}
		finally {
			desktop.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
		}
	}

	private void getAllAlarms() {
		((DracTableModel<String>) tablePanel.getTable().getModel()).clearTable();
		Map<String, String> parametersMap = new HashMap<String, String>();
		Thread t = new Thread(new ServerOperation(
		    ServerOperation.Operation.OP_GET_ALL_ALARMS, this, parametersMap),
		    OpenDracDesktop.SVR_OP_THREAD_NAME + ": getAllAlarms()");
		desktop.showProgressDialog("Retrieving service alarms...");
		t.setDaemon(true);
		t.start();
	}
}
