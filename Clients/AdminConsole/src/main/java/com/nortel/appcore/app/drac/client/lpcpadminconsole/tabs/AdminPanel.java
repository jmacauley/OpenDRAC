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
import java.awt.Canvas;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.ClockJComponent;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.ClockJComponent.MultiTicker;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperationCallback;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;

/**
 * @author pitman
 */
public final class AdminPanel implements ServerOperationCallback {
  private final JButton addNetworkElementButton = new JButton("Add NE");
  private final JTextField versionField = new JTextField();
  private final JTextField stateField = new JTextField();
  private final JTextField faultField = new JTextField();
  private final JTextField connHandledField = new JTextField();
  private final JTextField connActiveField = new JTextField();
  private final JTextField clientVersionField = new JTextField();
  private final JTextField buildDateField = new JTextField();
  private final JTextField hostOSField = new JTextField();
  private final JTextField hostArchField = new JTextField();
  private final JTextField javaVersionField = new JTextField();
  private final JTextField javaVendorField = new JTextField();

  private final JTextField nrNesValue = new JTextField();
  private final JTextField nrProvSchedulesValue = new JTextField();
  private final JTextField nrActiveSchedulesValue = new JTextField();
  private final JTextField nrProvServicesValue = new JTextField();
  private final JTextField nrActiveServicesValue = new JTextField();
  private final JTextField nrProvUsersValue = new JTextField();
  
  private final Point point = new Point(5, 15);
  private ClockJComponent clockLocal = new ClockJComponent(point);
  private ClockJComponent clockServer = new ClockJComponent(point);
  private ClockJComponent clockDisplay = new ClockJComponent(point);

  private final JTextField javaNameField = new JTextField();

  private final OpenDracDesktop desktop;

  public AdminPanel(OpenDracDesktop deskTop) {
    desktop = deskTop;
  }

  public JPanel buildAdminPanel() {
    JPanel adminPanel = new JPanel(new BorderLayout(1, 1));
    adminPanel.add(buildContentsPanel(), BorderLayout.NORTH);
    adminPanel.add(buildPaddingPanel(), BorderLayout.CENTER);
    return adminPanel;
  }

  public void enableAddNetworkElementButton(boolean enabled) {
    addNetworkElementButton.setEnabled(enabled);
  }

  public void update(Map<String, Object> result) {

    ServerInfoType serverInfo = (ServerInfoType) result.get(ServerOperation.MAP_RESULT_KEY);
    if (serverInfo != null) {

      versionField.setText(serverInfo.getControllerInfo().getVersion());
      stateField.setText(serverInfo.getControllerInfo().getStatus());

      faultField.setText(Integer.toString(serverInfo.getControllerInfo().getFaults()));
      connHandledField.setText(Integer.toString(serverInfo.getControllerInfo().getConnectionHandled()));
      connActiveField.setText(Integer.toString(serverInfo.getControllerInfo().getConnectionActive()));

      javaVersionField.setText(serverInfo.getVmInfo().getVmVersion());
      javaVendorField.setText(serverInfo.getVmInfo().getVmVendor());
      javaNameField.setText(serverInfo.getVmInfo().getVmName());

      hostOSField.setText(serverInfo.getOsInfo().getOsName());
      hostArchField.setText(serverInfo.getOsInfo().getOsArchitecture());

      clockServer.setTimeZoneId(serverInfo.getControllerInfo().getTimeZoneId());
      clockDisplay.setTimeZoneId(desktop.getTimeZonePreference().getID());
      clientVersionField.setText(desktop.getProductname() + " " + desktop.getVersion());
      buildDateField.setText(desktop.getBuildDate());
    }
  }

  private JPanel buildContentsPanel() {
    JPanel panel = new JPanel(new BorderLayout(1, 1));
    panel.add(buildCtlrInfoPanel(), BorderLayout.NORTH);

    // FIXME: This will kill the controller
    // panel.add(buildMetaDataPanel(), BorderLayout.CENTER);
    return panel;
  }

  private JPanel buildPaddingPanel() {
    JPanel paddingPanel = new JPanel(new BorderLayout(5, 5));
    JPanel paddingContentsPanel = new JPanel(new GridLayout(1, 1, 5, 5));
    paddingContentsPanel.add(new Canvas());
    paddingContentsPanel.add(new Canvas());
    paddingPanel.add(paddingContentsPanel, BorderLayout.CENTER);
    return paddingPanel;
  }

  private JPanel buildCtlrInfoPanel() {
    JPanel ctlrInfoPanel = new JPanel(new BorderLayout(5, 5));
    JPanel mainInfoPanel = new JPanel(new GridLayout(16, 2, 5, 5));
    JLabel ctlrVersion = new JLabel("Controller version: ");
    JLabel ctlrState = new JLabel("Controller state: ");
    JLabel ctlrFaults = new JLabel("Controller faults: ");
    JLabel clientVersion = new JLabel("Client version: ");
    JLabel clientBuildDate = new JLabel("Client Build Date: ");
    JLabel connHandled = new JLabel("Connections handled: ");
    JLabel connActive = new JLabel("Active connections: ");
    JLabel hostOS = new JLabel("Host operating system: ");
    JLabel hostArch = new JLabel("Host architecture: ");
    JLabel javaVersion = new JLabel("Java version: ");
    JLabel javaVendor = new JLabel("Java vendor: ");
    JLabel javaName = new JLabel("Java identifier: ");

    ctlrInfoPanel.setBorder(BorderFactory.createTitledBorder("Controller"));

    versionField.setEditable(false);
    stateField.setEditable(false);
    faultField.setEditable(false);
    connHandledField.setEditable(false);
    connActiveField.setEditable(false);
    hostOSField.setEditable(false);
    hostArchField.setEditable(false);
    javaVersionField.setEditable(false);
    javaVendorField.setEditable(false);
    javaNameField.setEditable(false);
    clientVersionField.setEditable(false);
    buildDateField.setEditable(false);
    mainInfoPanel.add(ctlrVersion);
    mainInfoPanel.add(versionField);
    mainInfoPanel.add(ctlrState);
    mainInfoPanel.add(stateField);
    mainInfoPanel.add(ctlrFaults);
    mainInfoPanel.add(faultField);
    mainInfoPanel.add(clientVersion);
    mainInfoPanel.add(clientVersionField);
    mainInfoPanel.add(clientBuildDate);
    mainInfoPanel.add(buildDateField);
    mainInfoPanel.add(connHandled);
    mainInfoPanel.add(connHandledField);
    mainInfoPanel.add(connActive);
    mainInfoPanel.add(connActiveField);
    mainInfoPanel.add(hostOS);
    mainInfoPanel.add(hostOSField);
    mainInfoPanel.add(hostArch);
    mainInfoPanel.add(hostArchField);
    mainInfoPanel.add(javaVersion);
    mainInfoPanel.add(javaVersionField);
    mainInfoPanel.add(javaVendor);
    mainInfoPanel.add(javaVendorField);
    mainInfoPanel.add(javaName);
    mainInfoPanel.add(javaNameField);

    final List<ClockJComponent> clocks = new ArrayList<ClockJComponent>();
    clocks.add(clockLocal);
    clocks.add(clockServer);
    clocks.add(clockDisplay);
    MultiTicker mt = new ClockJComponent.MultiTicker(clocks);
    final Thread thread = new Thread(mt);
    thread.setDaemon(true);
    thread.start();

    mainInfoPanel.add(new JLabel("Local: "));
    mainInfoPanel.add(clockLocal);
    mainInfoPanel.add(new JLabel("Server: "));
    mainInfoPanel.add(clockServer);
    mainInfoPanel.add(new JLabel("Display: "));
    mainInfoPanel.add(clockDisplay);

    addNetworkElementButton.setFont(OpenDracDesktop.BASE_FONT);

    ctlrInfoPanel.add(mainInfoPanel, BorderLayout.CENTER);
    return ctlrInfoPanel;
  }

  /**
   * Callback method for server operation
   */
  @Override
  @SuppressWarnings("unchecked")
  public void handleServerOperationResult(ServerOperation op) {
    if (op.getOperation() == ServerOperation.Operation.OP_GET_ALL_NES) {
      List<NetworkElementHolder> nes = (List<NetworkElementHolder>) op.getResult().get(ServerOperation.MAP_RESULT_KEY);
      if (nes == null) {
        nrNesValue.setText("-");
      }
      else {
        nrNesValue.setText("" + nes.size());
      }
    }
    else if (op.getOperation() == ServerOperation.Operation.OP_GET_PROVISONED_SCHEDULES) {
      List<Schedule> schedules = (List<Schedule>) op.getResult().get(ServerOperation.MAP_RESULT_KEY);
      if (schedules == null) {
        nrProvSchedulesValue.setText("");
      }
      else {
        nrProvSchedulesValue.setText("" + schedules.size());
      }
    }
    else if (op.getOperation() == ServerOperation.Operation.OP_GET_ACTIVE_SCHEDULES) {
      List<Schedule> schedules = (List<Schedule>) op.getResult().get(ServerOperation.MAP_RESULT_KEY);
      if (schedules == null) {
        nrActiveSchedulesValue.setText("");
      }
      else {
        nrActiveSchedulesValue.setText("" + schedules.size());
      }
    }
    else if (op.getOperation() == ServerOperation.Operation.OP_GET_PROVISONED_SERVICES) {
      List<DracService> services = (List<DracService>) op.getResult().get(ServerOperation.MAP_RESULT_KEY);
      if (services == null) {
        nrProvServicesValue.setText("1");
      }
      else {
        nrProvServicesValue.setText("" + services.size());
      }
    }
    else if (op.getOperation() == ServerOperation.Operation.OP_GET_ACTIVE_SERVICES) {
      List<DracService> services = (List<DracService>) op.getResult().get(ServerOperation.MAP_RESULT_KEY);
      if (services == null) {
        nrActiveServicesValue.setText("2");
      }
      else {
        nrActiveServicesValue.setText("" + services.size());
      }
    }
    else if (op.getOperation() == ServerOperation.Operation.OP_GET_PROVISONED_USERS) {
      List<UserProfile> profiles = (List<UserProfile>) op.getResult().get(ServerOperation.MAP_RESULT_KEY);
      if (profiles == null) {
        nrProvUsersValue.setText("");
      }
      else {
        nrProvUsersValue.setText("" + profiles.size());
      }
    }
  }

}