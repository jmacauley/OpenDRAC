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

package com.nortel.appcore.app.drac.client.lpcpadminconsole;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.NetworkGraph;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerMonitor;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;

/**
 * 
 */
public final class Reconnect implements Runnable {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final OpenDracDesktop desktop;
  private JTextArea ta;
  private JLabel errorLabel;
  private JDialog retry;
  private final Frame parent;
  private int attempt = 1;
  private int timeout = 1000;
  private boolean stop;

  public Reconnect(OpenDracDesktop dracDesktop, Frame parentFrame) {
    desktop = dracDesktop;
    parent = parentFrame;
    buildWindow();
    desktop.updateErrorStatus(true, "Server not ready");
  }

  public void buildWindow() {
    JPanel mainPanel = new JPanel(new BorderLayout(1, 1));
    JPanel borderPanel = new JPanel(new GridLayout(1, 1));
    JPanel eastButtonPanel = new JPanel();
    JPanel buttonPanel = new JPanel(new BorderLayout(1, 1));
    JLabel northErrorLabel = new JLabel();
    JButton cancelButton = new JButton("Cancel");

    ta = new JTextArea();
    errorLabel = new JLabel();
    retry = new JDialog(parent);

    retry.setUndecorated(true);

    mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        desktop.serverMonitor.stop();
        stop = true;
        JOptionPane.showMessageDialog(parent, "The Administration Console will now exit.");
        log.debug("AdminConsole exiting!");
        System.exit(0);
      }
    });

    eastButtonPanel.add(cancelButton);
    buttonPanel.add(eastButtonPanel, BorderLayout.EAST);
    ta.setBackground(Color.lightGray);
    ta.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    northErrorLabel.setText("<html>Connection to controller lost.<br>Attempting to re-establish connection.</html>");
    ta.append("Connection to server lost.\n");
    ta.append("Attempting to re-establish connection.\n");

    mainPanel.setBorder(BorderFactory.createEtchedBorder());
    mainPanel.add(northErrorLabel, BorderLayout.NORTH);
    mainPanel.add(errorLabel, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    borderPanel.add(mainPanel);
    borderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    retry.getContentPane().setLayout(new BorderLayout(1, 1));
    retry.getContentPane().add(borderPanel, BorderLayout.CENTER);
    retry.setSize(300, 100);
    retry.setLocationRelativeTo(parent);

  }

  @Override
  public void run() {
    try {
      retry.setLocationRelativeTo(parent);
      retry.setVisible(true);
      ta.append("Attempt: " + attempt + " reconnecting to server.\n");
      errorLabel.setText("Attempt: " + attempt + " reconnecting to server.");
      ta.setCaretPosition(ta.getText().length());
      desktop.updateStatus("Attempt: " + attempt + " reconnecting to server.", true);
      while (!OpenDracDesktop.getAuth().reConnect() && !stop) {
        retry.requestFocus();
        attempt++;
        timeout += 1000;
        ta.append("Next retry in: " + timeout / 1000 + " seconds.\n");
        errorLabel.setText("Next retry in: " + timeout / 1000 + " seconds.");
        ta.setCaretPosition(ta.getText().length());
        try {
          Uninterruptibles.sleepUninterruptibly(timeout, TimeUnit.MILLISECONDS);
          ta.append("Attempt: " + attempt + " reconnecting to server.\n");
          desktop.updateStatus("Attempt: " + attempt + " reconnecting to server.", true);
          errorLabel.setText("Attempt: " + attempt + " reconnecting to server.");
          ta.setCaretPosition(ta.getText().length());
        }
        catch (Exception e) {
          log.error("Exception: ", e);
        }
      }
      retry.setVisible(false);
      // this.desktop.getTopologyPanel().getView().clearGraph();
      NetworkGraph.INSTANCE.clearGraph();
      // Wait for the server status to become "Running"
      this.desktop.showProgressDialog("Waiting for server to restart");
      this.desktop.updateStatus("Waiting for server to restart.", true);
      while (!OpenDracDesktop.getAuth().isControllerRunning() && !stop) {
        if (OpenDracDesktop.getAuth().isControllerInactive()) {
          this.desktop.handleInactivateRequest();
        }
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
      }

      // Make sure the client is active
      desktop.setActive();

      desktop.serverMonitor = new ServerMonitor();
      desktop.serverMonitor.addHandler(desktop);
      desktop.serverThread = new Thread(this.desktop.serverMonitor, OpenDracDesktop.SVR_MON_THREAD_NAME + "-R");
      desktop.serverThread.setDaemon(true);
      desktop.serverThread.start();
      desktop.updateStatus("Server restart complete.", true);
      desktop.updateErrorStatus(false, "");

      checkDiscoveryStatus();
      desktop.hideProgressDialog();
      desktop.getAndDisplayServerGraph();
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  /**
   * Check the discovery status of the server...
   * <p>
   * 
   * @TODO clearly we are not checking much here, other than making sure we can
   *       talk lpcp or throw an exception trying...
   */

  private void checkDiscoveryStatus() throws Exception {

    String discStatus = new ServerOperation().getDiscoveryStatus();
    log.debug("Current discovery status: " + discStatus);
  }
}
