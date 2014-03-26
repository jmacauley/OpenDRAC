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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.topology;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeCache;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;

import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * @author pitman
 */
@SuppressWarnings("serial")
public final class TopologyPopupMenu extends JPopupMenu {
  private static final Logger log = LoggerFactory.getLogger(TopologyPopupMenu.class);
  static class LinkDialogPopulator implements Runnable {
    private final JComboBox srcPort;
    private final JComboBox destPort;
    private final JDialog dialog;
    private final DracVertex src;
    private final DracVertex dest;

    public LinkDialogPopulator(DracVertex src, DracVertex dest,
        JComboBox srcPort, JComboBox destPort, JDialog dialog) {
      log.debug("LinkDialogPopulator from " + src + " " + dest);

      this.srcPort = srcPort;
      this.destPort = destPort;
      this.dialog = dialog;
      srcPort.setEnabled(false);
      destPort.setEnabled(false);
      this.src = src;
      this.dest = dest;
    }

    @Override
    public void run() {
      try {
        try {
          String srcNeId = src.getIeee();
          String dstNeId = dest.getIeee();
          java.util.List<Facility> srcFacilities = new ServerOperation()
              .listFacalities(srcNeId);
          java.util.List<Facility> dstFacilities = new ServerOperation()
              .listFacalities(dstNeId);

          log.debug("LinkDialogPopulator src facilities from " + srcNeId
              + " are:" + srcFacilities + " " + dstNeId + " are:"
              + dstFacilities);

          if (srcFacilities != null) {
            log.debug("Found source " + srcFacilities.size() + " facilities.");
            Facility[] sortedFacs = OpenDracDesktop
                .sortFacilities(srcFacilities);
            

            for (Facility f : sortedFacs) {
              if (f.getAid() != null) // &&
              {
                if (FacilityConstants.SIGNAL_TYPE.ENNI.toString()
                    .equalsIgnoreCase(f.getSigType())
                    || FacilityConstants.SIGNAL_TYPE.INNI.toString()
                        .equalsIgnoreCase(f.getSigType())) {
                  srcPort.addItem(f.getAid());
                }
              }
            }
          }

          if (dstFacilities != null) {
            log.debug("Found dest " + dstFacilities.size() + " facilities.");
            Facility[] sortedFacs = OpenDracDesktop
                .sortFacilities(dstFacilities);
            for (Facility f : sortedFacs) {
              if (f.getAid() != null) // &&
              // !f.getAid().startsWith("WAN"))
              {
                if (FacilityConstants.SIGNAL_TYPE.ENNI.toString()
                    .equalsIgnoreCase(f.getSigType())
                    || FacilityConstants.SIGNAL_TYPE.INNI.toString()
                        .equalsIgnoreCase(f.getSigType())) {
                  destPort.addItem(f.getAid());
                }
              }
            }
          }
        }
        catch (Exception e) {
          log.error("ERROR populating link dialog ", e);
        }
        finally {
          srcPort.setEnabled(true);
          destPort.setEnabled(true);
        }
      }
      finally {
        dialog.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
      }
    }
  }

  public TopologyPopupMenu(final JungTopologyPanel jung, final Frame parent,
      final DracEdge edge, final VisualizationViewer<DracVertex, DracEdge> vv) {
    super();
    // POPUP menu for a EDGE

    add(new AbstractAction("Edge properties") {
      @Override
      public void actionPerformed(ActionEvent e) {
        showEdgeProperties(jung, parent, edge);
        vv.repaint();
      }
    });

    AbstractAction removeLink = new AbstractAction("Remove link") {
      @Override
      public void actionPerformed(ActionEvent e) {
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("SRCNEID", edge.getSource().getIeee());
        args.put("DSTNEID", edge.getTarget().getIeee());
        args.put("SRCPORT", edge.getSourceAid());
        args.put("DSTPORT", edge.getTargetAid());
        jung.notifyListeners(
            TopologyViewEventHandler.EVENT_TYPE.REMOVELINK_EVENT, args);
        vv.repaint();
      }
    };

    removeLink.setEnabled(edge.isManual());
    add(removeLink);

    AbstractAction generateManual = new AbstractAction("Generate manual link") {
      @Override
      public void actionPerformed(ActionEvent e) {
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("SRCNEIEEE", edge.getSource().getIeee());
        args.put("DSTNEIEEE", edge.getTarget().getIeee());
        args.put("SRCPORT", edge.getSourceAid());
        args.put("DSTPORT", edge.getTargetAid());
        jung.notifyListeners(
            TopologyViewEventHandler.EVENT_TYPE.GENERATEMANUALLINK_EVENT, args);
        vv.repaint();
      }
    };

    generateManual
        .setEnabled(!edge.isManual() && !edge.hasEclipsedManualLink());
    add(generateManual);
  }

  public TopologyPopupMenu(final JungTopologyPanel jung, final Frame parent,
      final DracVertex vertex,
      final VisualizationViewer<DracVertex, DracEdge> vv) {

    // POPUP menu for a Vertex
    super();
    add(new AbstractAction("Add Link") {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (vv.getPickedVertexState().getPicked().size() != 2) {
          JOptionPane
              .showMessageDialog(
                  parent,
                  "To add a manual topological link you must first select two NEs before selecting this option",
                  "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        DracVertex[] a = vv.getPickedVertexState().getPicked()
            .toArray(new DracVertex[2]);
        DracVertex src = a[0];
        DracVertex dest = a[1];

        Map<String, String> map = new HashMap<String, String>();
        map.put("SRCIEEE", src.getIeee());
        map.put("SRCIP", src.getIp());
        map.put("SRCLABEL", src.getLabel());

        map.put("DESTIEEE", dest.getIeee());
        map.put("DESTIP", dest.getIp());
        map.put("DESTLABEL", dest.getLabel());

        jung.notifyListeners(TopologyViewEventHandler.EVENT_TYPE.ADDLINK_EVENT,
            map);

        vv.repaint();
      }
    });
    add(new AbstractAction("Remove NE") {
      @Override
      public void actionPerformed(ActionEvent e) {

        int result = JOptionPane
            .showConfirmDialog(
                null,
                "<HTML>Are you sure you want to remove "
                    + vertex.getIp()
                    + " ("
                    + vertex.getLabel()
                    + ") ?<BR><B>All manually provisioned data will be lost</B></HTML>",
                "Remove Network Element", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
          NetworkElementHolder ne = new NetworkElementHolder(vertex.getIp(),
              vertex.getPort(), null, null, null, NeStatus.NE_UNKNOWN, null,
              null, 0, null, null, vertex.getIeee(), null, false, "Unknown", null, null,
              null);
          jung.notifyListeners(
              TopologyViewEventHandler.EVENT_TYPE.UNMANAGE_EVENT, ne);
        }
        vv.repaint();
        JungTopologyPanel.resetTopology();
      }
    });
    add(new AbstractAction("Create Schedule") {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (vv.getPickedVertexState().getPicked().size() != 2
            && vv.getPickedVertexState().getPicked().size() != 1) {
          JOptionPane
              .showMessageDialog(
                  parent,
                  "To create a schedule you must first select two NEs before selecting this option",
                  "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        DracVertex[] a;
        DracVertex src;
        DracVertex dest;

        if (vv.getPickedVertexState().getPicked().size() == 2) {
          a = vv.getPickedVertexState().getPicked().toArray(new DracVertex[2]);
          src = a[0];
          dest = a[1];
        }
        else {
          a = vv.getPickedVertexState().getPicked().toArray(new DracVertex[1]);
          src = a[0];
          dest = a[0];
        }

        Map<String, String> m = new HashMap<String, String>();
        m.put("srcIeee", src.getIeee());
        m.put("dstIeee", dest.getIeee());
        m.put("srcLabel", src.getLabel());
        m.put("dstLabel", dest.getLabel());
        m.put("srcMode", src.getMode());
        m.put("dstMode", dest.getMode());
        jung.notifyListeners(TopologyViewEventHandler.EVENT_TYPE.SPF_EVENT, m);
        vv.repaint();
      }
    });
    add(new AbstractAction("Toggle NE Communications Link") {
      @Override
      public void actionPerformed(ActionEvent e) {
        int result = JOptionPane
            .showConfirmDialog(
                null,
                "<HTML>Are you sure you want to toggle the communication link to "
                    + vertex.getIp()
                    + " ("
                    + vertex.getLabel()
                    + ") ?<BR><B>Communications will temporarily be lost</B></HTML>",
                "Toggle Network Element Communications",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
          NetworkElementHolder ne = new NetworkElementHolder(vertex.getIp(),
              vertex.getPort(), null, null, null, NeStatus.NE_UNKNOWN, null,
              null, 0, null, null, null, null, false, "Unknown", null, null, null);
          jung.notifyListeners(
              TopologyViewEventHandler.EVENT_TYPE.TOGGLE_NE_EVENT, ne);
        }
        vv.repaint();
      }
    });
    add(new AbstractAction("Properties") {
      @Override
      public void actionPerformed(ActionEvent e) {
        showNEProperties(jung, parent, vertex);
        vv.repaint();
      }
    });
  }

  public TopologyPopupMenu(final JungTopologyPanel jung, final Frame parent,
      final VisualizationViewer<DracVertex, DracEdge> vv) {
    // POPUP menu for the entire graph
    super();

    add(new AbstractAction("Add NE") {
      @Override
      public void actionPerformed(ActionEvent e) {
        addNEDialog(parent);
      }
    });
    add(new AbstractAction("Graph properties") {
      @Override
      public void actionPerformed(ActionEvent e) {
        showGraphProperties(jung, parent);
        vv.repaint();
      }
    });
  }

  public static void addLinkDialog(final JFrame parent, final String ne1IEEE,
      final String ne1IP, final String ne1LABEL, final String ne2IEEE,
      final String ne2IP, final String ne2LABEL) {
    final JDialog d = new JDialog(parent);
    final JTextField neighbour1 = new JTextField();
    final JTextField neighbour2 = new JTextField();
    final JTextField n1IP = new JTextField();
    final JTextField n2IP = new JTextField();
    final JComboBox sourcePort = new JComboBox();
    final JComboBox destPort = new JComboBox();
    // final JTextField weightField = new JTextField();
    final String dialogTitle = "Add Link";
    // final int DIALOG_WIDTH = 525;
    // final int DIALOG_HEIGHT = 250;

    JTextArea explain = new JTextArea(
        "Add manual topological links between unconnected ENNI or INNI designated facilities.\n"
            + "Warning: To avoid traffic problems with schedules only provision manual "
            + "topology between facilities that are directly interconnected and capable "
            + "of carrying traffic between the two facilities.\n", 4, 80);
    explain.setLineWrap(true);
    explain.setWrapStyleWord(true);
    explain.setEditable(false);
    JPanel explainP = new JPanel(new BorderLayout(1, 1));
    explainP.add(explain, BorderLayout.CENTER);
    JPanel basePanel = new JPanel(new BorderLayout(1, 1));
    JPanel anotherPanel = new JPanel(new BorderLayout(1, 1));
    JPanel mainPanel = new JPanel(new GridLayout(1, 2));
    JPanel southPanel = new JPanel(new BorderLayout(1, 1));
    JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    // JPanel weightPanel = new JPanel(new GridLayout(1, 2));
    JLabel sourceLabel = new JLabel("Network Element:");
    JLabel sourceIpLabel = new JLabel("IP:");
    JLabel destLabel = new JLabel("Network Element:");
    JLabel destIpLabel = new JLabel("IP:");
    JLabel sourcePortLabel = new JLabel("Port: ");
    JLabel destPortLabel = new JLabel("Port: ");
    // JLabel weightLabel = new JLabel("Weight: ");
    JButton linkButton = new JButton("Link");
    JButton cancelButton = new JButton("Cancel");
    JPanel srcPanel = new JPanel();
    JPanel dstPanel = new JPanel();

    d.setTitle(dialogTitle);
    d.getContentPane().setLayout(new GridLayout(1, 1));

    srcPanel.setLayout(new GridLayout(4, 2));
    dstPanel.setLayout(new GridLayout(4, 2));
    srcPanel.setBorder(BorderFactory.createTitledBorder("Source:"));
    dstPanel.setBorder(BorderFactory.createTitledBorder("Destination:"));
    srcPanel.add(sourceLabel);
    srcPanel.add(neighbour1);
    srcPanel.add(sourceIpLabel);
    srcPanel.add(n1IP);
    srcPanel.add(sourcePortLabel);
    srcPanel.add(sourcePort);

    dstPanel.add(destLabel);
    dstPanel.add(neighbour2);
    dstPanel.add(destIpLabel);
    dstPanel.add(n2IP);
    dstPanel.add(destPortLabel);
    dstPanel.add(destPort);

    buttonPanel.add(linkButton);
    buttonPanel.add(cancelButton);

    southPanel.add(buttonPanel, BorderLayout.EAST);

    linkButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        if (sourcePort.getSelectedItem() == null
            || destPort.getSelectedItem() == null) {
          JOptionPane.showMessageDialog(d, "No ports selected");
        }
        else {
          String srcPort = (String) sourcePort.getSelectedItem();
          String dstPort = (String) destPort.getSelectedItem();

          int result = JOptionPane
              .showConfirmDialog(
                  null,
                  "<HTML>Are you sure you want to create a manual topological link between '"
                      + ne1LABEL
                      + "' ("
                      + ne1IP
                      + ") "
                      + srcPort
                      + " and '"
                      + ne2LABEL
                      + "' ("
                      + ne2IP
                      + ") "
                      + dstPort
                      + " ?<BR><B>Adding invalid topological links may cause OpenDRAC to malfunction!</B></HTML>",
                  "Create manual topological link", JOptionPane.YES_NO_OPTION,
                  JOptionPane.WARNING_MESSAGE);
          if (result == JOptionPane.YES_OPTION) {
            try {
              new ServerOperation().addAjacency(ne1IEEE, sourcePort
                  .getSelectedItem().toString(), ne2IEEE, destPort
                  .getSelectedItem().toString());
            }
            catch (Exception e) {
              log.error("Error: ", e);
              JOptionPane.showMessageDialog(parent,
                  "Failed to add topological link  (" + ne1IEEE + ","
                      + sourcePort.getSelectedItem().toString() + "," + ne2IEEE
                      + "," + destPort.getSelectedItem().toString()
                      + ") \nmessage: " + e);
            }

          }
          d.setVisible(false);
        }
      }
    });

    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        d.setVisible(false);
        d.dispose();
      }
    });

    neighbour1.setEditable(false);
    neighbour2.setEditable(false);
    neighbour1.setText(ne1LABEL);
    neighbour2.setText(ne2LABEL);
    n1IP.setText(ne1IP);
    n2IP.setText(ne2IP);
    n1IP.setEditable(false);
    n2IP.setEditable(false);

    mainPanel.add(srcPanel);
    mainPanel.add(dstPanel);

    basePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    anotherPanel.add(mainPanel, BorderLayout.NORTH);
    anotherPanel.add(southPanel, BorderLayout.SOUTH);

    basePanel.add(explainP, BorderLayout.NORTH);
    basePanel.add(anotherPanel, BorderLayout.SOUTH);

    DracVertex src = NeCache.INSTANCE.getVertex(ne1IEEE);
    DracVertex dest = NeCache.INSTANCE.getVertex(ne2IEEE);

    Thread linkDialogPopulatorThread = new Thread(new LinkDialogPopulator(src,
        dest, sourcePort, destPort, d));
    linkDialogPopulatorThread.setDaemon(true);
    linkDialogPopulatorThread.start();

    d.getContentPane().add(basePanel);
    d.setCursor(OpenDracDesktop.WAIT_CURSOR);

    d.pack();
    d.setLocationRelativeTo(parent);
    d.setVisible(true);

  }

  private static double getAlmostRandom(int min, int max) {
    return min + (int) (Math.random() * ((max - min) + 1));
  }

  public static void addNEDialog(final Frame parent) {
    final JDialog d = new JDialog(parent);
    JPanel mainPanel = new JPanel(new GridLayout(5, 1, 3, 3));

    JPanel namePanel = new JPanel(new GridLayout(1, 2, 5, 5));
    JPanel ipPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    JPanel userPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    JPanel passPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    JPanel protocolPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    JPanel portPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    JLabel neName = new JLabel("Network Element Name:");
    JLabel ip = new JLabel("IP Address:");
    JLabel protocol = new JLabel("Protocol:");
    JLabel userid = new JLabel("User ID:");
    JLabel passwd = new JLabel("Password:");
    JLabel port = new JLabel("Port:");
    final JTextField userField = new JTextField();
    final JPasswordField passField = new JPasswordField();
    final JTextField neNameField = new JTextField();
    final JTextField ipField = new JTextField();

    JPanel buttonPanel = new JPanel(new BorderLayout(1, 1));
    JPanel eastButtonPanel = new JPanel();
    JButton okButton = new JButton("Ok");
    JButton cancelButton = new JButton("Cancel");

    neName.setHorizontalAlignment(SwingConstants.RIGHT);
    ip.setHorizontalAlignment(SwingConstants.RIGHT);
    protocol.setHorizontalAlignment(SwingConstants.RIGHT);
    userid.setHorizontalAlignment(SwingConstants.RIGHT);
    passwd.setHorizontalAlignment(SwingConstants.RIGHT);
    port.setHorizontalAlignment(SwingConstants.RIGHT);

    final JComboBox protocolBox = new JComboBox();
    final JComboBox portBox = new JComboBox();
    portBox.setEditable(true);
    populateProtocolBox(protocolBox, portBox);

    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        try {
          String ip = ipField.getText().trim();
          String port = ((String) portBox.getSelectedItem()).trim();
          if (NeCache.INSTANCE.getVertex(ip, port) == null) {
            CryptedString pw = CryptoWrapper.INSTANCE.encrypt(
                new String(passField.getPassword()));

            DracVertex v = new DracVertex("newly added NE", "", ip, port,
                "unknown", NeType.UNKNOWN, userField.getText(), pw,
                NeStatus.NE_UNKNOWN, "", getAlmostRandom(150, 200),
                getAlmostRandom(150, 200));
            try {
              NeCache.INSTANCE.addVertex(v);
            }
            catch (Exception e) {
              log.error("AddVertex failed ", e);
            }
            try {
              NetworkElementHolder newNe = new NetworkElementHolder(ip, port,
                  userField.getText(), null, null, NeStatus.NE_UNKNOWN, null,
                  pw, 0, NETWORK_ELEMENT_MODE.Unknown, null, null,
                  (PROTOCOL_TYPE) protocolBox.getSelectedItem(), true, "Unknown", "", 50d,
                  50d);
              new ServerOperation().enrollNetworkElement(newNe);
            }
            catch (Exception e) {
              log.error("manageNe failed", e);
            }
            d.setVisible(false);
            new ServerOperation().updateNetworkElementPosition(v.getIp(),
                v.getPort(), v.getPositionX(), v.getPositionY());
          }
          else {
            JOptionPane.showMessageDialog(null, "The NE with IP address: "
                + ipField.getText() + " is already managed by OpenDRAC");
          }

        }
        catch (Exception e1) {
          log.error("Error: ", e1);
        }
      }
    });

    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        d.setVisible(false);
        d.dispose();
      }
    });

    protocolBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        portBox.setSelectedIndex(protocolBox.getSelectedIndex());
      }
    });

    eastButtonPanel.add(okButton);
    eastButtonPanel.add(cancelButton);
    buttonPanel.add(eastButtonPanel, BorderLayout.EAST);

    d.setTitle("Manage NE");
    d.setModal(true);
    namePanel.add(neName);
    namePanel.add(neNameField);

    ipPanel.add(ip);
    ipPanel.add(ipField);

    protocolPanel.add(protocol);
    protocolPanel.add(protocolBox);

    userPanel.add(userid);
    userPanel.add(userField);

    passPanel.add(passwd);
    passPanel.add(passField);

    portPanel.add(port);
    portPanel.add(portBox);

    ipField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent fe) {
        log.debug("Detected focus lost.. ipField.getText().indexOf(.) is: "
            + ipField.getText().indexOf("."));
        if (ipField.getText().indexOf(".") < 0) {
          ipField.setText(convertHostnameToFQDN(ipField.getText()));
        }
      }
    });

    mainPanel.add(ipPanel);
    mainPanel.add(protocolPanel);
    mainPanel.add(portPanel);
    mainPanel.add(userPanel);
    mainPanel.add(passPanel);

    mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    d.getContentPane().setLayout(new BorderLayout(1, 1));
    d.getContentPane().add(mainPanel, BorderLayout.NORTH);
    d.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    d.setSize(new Dimension(300, 250));
    d.setLocationRelativeTo(parent);
    d.setVisible(true);
  }

  public static void showNEProperties(final JungTopologyPanel jung,
      Frame parent, final DracVertex ne) {
    final JDialog d = new JDialog(parent);
    d.setTitle("Network Element Properties");
    JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
    JPanel nePanel = new JPanel(new GridLayout(10, 2, 5, 5));
    JPanel buttonPanel = new JPanel();

    JLabel neNameLabel = new JLabel("Name:");
    neNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    final JTextField neNameField = new JTextField();
    neNameField.setText(ne.getLabel());
    neNameField.setEditable(false);

    JLabel neIeeeLabel = new JLabel("IEEE:");
    neIeeeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    final JTextField neIeeeField = new JTextField();
    neIeeeField.setText(ne.getIeee());
    neIeeeField.setEditable(false);

    JLabel neTypeLabel = new JLabel("NE Type: ");
    neTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    final JTextField neTypeField = new JTextField();
    neTypeField.setText(ne.getType().toString());
    neTypeField.setEditable(false);

    JLabel neStateLabel = new JLabel("State: ");
    neStateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    final JTextField neStateField = new JTextField();
    neStateField.setText(ne.getStatus().toString());
    neStateField.setEditable(false);

    JLabel modeLabel = new JLabel("Mode:");
    modeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    final JTextField modeField = new JTextField();
    modeField.setText(ne.getMode());
    modeField.setEditable(false);

    JLabel neIpLabel = new JLabel("Edit Address: ");
    neIpLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    final JTextField ipField = new JTextField();
    ipField.setText(ne.getIp());
    ipField.setEditable(true);
    ipField.setBackground(Color.LIGHT_GRAY);

    JLabel portLabel = new JLabel("Edit Port: ");
    portLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    final JTextField portField = new JTextField();
    portField.setText(ne.getPort());
    portField.setEditable(true);
    portField.setBackground(Color.LIGHT_GRAY);

    JLabel userLabel = new JLabel("Edit User Id: ");
    userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    final JTextField userField = new JTextField();
    userField.setText(ne.getUserId());
    userField.setBackground(Color.LIGHT_GRAY);
    final String oldUser = userField.getText();

    JLabel passLabel = new JLabel("Edit Password: ");
    passLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    final JPasswordField passField = new JPasswordField();
    passField.setBackground(Color.LIGHT_GRAY);
    passField.setText(ne.getPassword().toString());
    final String oldPass = new String(passField.getPassword());
    final String oldIp = ipField.getText();
    final String oldPort = portField.getText();

    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        try {
          String user = userField.getText().trim();
          String passwd = new String(passField.getPassword()).trim();
          if ((!passwd.equals(oldPass) || !user.equals(oldUser))
              && !user.equals("") && !passwd.equals("")) {
            int result = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to update the Network Element User Id and Password for "
                    + ne.getIp() + " (" + ne.getLabel()
                    + ") ? Network Element association will be toggled.",
                "Confirm password change", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
              CryptedString newPw = CryptoWrapper.INSTANCE.encrypt(passwd);
              NetworkElementHolder h = new NetworkElementHolder(ne.getIp(), ne
                  .getPort(), user, null, null, NeStatus.NE_UNKNOWN, null,
                  newPw, 0, null, null, null, null, false, "Unknown",null, null, null);
              jung.notifyListeners(
                  TopologyViewEventHandler.EVENT_TYPE.CHANGE_NE_PASSWORD_EVENT,
                  h);
            }
          }

          // Edit IP & Port
          final String newIp = ipField.getText();
          final String newPort = portField.getText();
          if (!newIp.equals(oldIp) || !newPort.equals(oldPort)) {
            int result = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to edit the Address/Port of Network Element '"
                    + neNameField.getText() + "' ?", "Confirm Ip/Port change",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
              result = JOptionPane.showConfirmDialog(null,
                  "Are You Really Sure? Topology update may take some time!",
                  "Confirm Ip/Port change", JOptionPane.YES_NO_OPTION,
                  JOptionPane.WARNING_MESSAGE);
              if (result == JOptionPane.YES_OPTION) {
                final ServerOperation serverOperation = new ServerOperation();
                serverOperation.updateAddressAndPort(oldIp,
                    Integer.parseInt(oldPort), newIp, Integer.parseInt(newPort));
                serverOperation.toggleNeAssociation(new NetworkElementHolder(
                    newIp, newPort, user, null, null, NeStatus.NE_UNKNOWN, null,
                    CryptoWrapper.INSTANCE.encrypt(passwd), 0, null, null,
                    null, null, false, "Unknown", null, null, null));
              }
            }
          }
          d.setVisible(false);
          d.dispose();
        }
        catch (Exception e) {
          log.error("Error: ", e);
        }
      }
    });

    nePanel.add(neIpLabel);
    nePanel.add(ipField);

    nePanel.add(portLabel);
    nePanel.add(portField);

    nePanel.add(userLabel);
    nePanel.add(userField);

    nePanel.add(passLabel);
    nePanel.add(passField);

    nePanel.add(neNameLabel);
    nePanel.add(neNameField);

    nePanel.add(neIeeeLabel);
    nePanel.add(neIeeeField);

    nePanel.add(modeLabel);
    nePanel.add(modeField);

    nePanel.add(neTypeLabel);
    nePanel.add(neTypeField);

    nePanel.add(neStateLabel);
    nePanel.add(neStateField);

    buttonPanel.add(okButton);
    mainPanel.add(buttonPanel, BorderLayout.EAST);
    mainPanel.add(nePanel, BorderLayout.NORTH);

    d.getContentPane().setLayout(new GridLayout(1, 1, 5, 5));
    d.getContentPane().add(mainPanel);
    d.setLocation(new Point(parent.getLocation().x + 250,
        parent.getLocation().y + 400));
    nePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    d.pack();
    d.setVisible(true);
  }

  private static String convertHostnameToFQDN(String name) {
    InetAddress addr = null;
    String ip = name;

    try {
      addr = InetAddress.getByName(name);
      ip = addr.getCanonicalHostName();
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
    return ip;
  }

  private static void populateProtocolBox(JComboBox protocolBox,
      JComboBox portBox) {
    protocolBox.removeAllItems();
    portBox.removeAllItems();

    for (PROTOCOL_TYPE p : PROTOCOL_TYPE.values()) {
      protocolBox.addItem(p);
      portBox.addItem(Integer.toString(p.getDefaultPortNumber()));
    }
  }

  private void showEdgeProperties(final JungTopologyPanel jung, Frame parent,
      final DracEdge edge) {
    final JDialog d = new JDialog(parent);
    d.setTitle("Link properties");
    JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
    JPanel linkPanel = new JPanel(new GridLayout(5, 2, 5, 5));

    final JTextField linkSrcField = new JTextField(edge.getSource().getLabel());
    linkSrcField.setEditable(false);
    final JTextField linkDstField = new JTextField(edge.getTarget().getLabel());
    linkDstField.setEditable(false);
    final JTextField linkSrcPortField = new JTextField(edge.getSourceAid());
    linkSrcPortField.setEditable(false);
    final JTextField linkDstPortField = new JTextField(edge.getTargetAid());
    linkDstPortField.setEditable(false);

    linkPanel.add(new JLabel("Source:"));
    linkPanel.add(linkSrcField);
    linkPanel.add(new JLabel("Destination:"));
    linkPanel.add(linkDstField);
    linkPanel.add(new JLabel("Source port:"));
    linkPanel.add(linkSrcPortField);
    linkPanel.add(new JLabel("Destination port: "));
    linkPanel.add(linkDstPortField);

    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        d.setVisible(false);
        d.dispose();
      }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(okButton);
    mainPanel.add(buttonPanel, BorderLayout.EAST);
    mainPanel.add(linkPanel, BorderLayout.NORTH);

    d.getContentPane().setLayout(new GridLayout(1, 1, 5, 5));
    d.getContentPane().add(mainPanel);
    d.setLocation(new Point(parent.getLocation().x + 250,
        parent.getLocation().y + 400));
    linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    d.pack();
    d.setVisible(true);
  }

  private void showGraphProperties(final JungTopologyPanel jung, Frame parent) {
    final JDialog d = new JDialog(parent);
    d.setTitle("Graph properties");

    JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
    JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    JPanel graphPanel = new JPanel(new GridLayout(0, 2, 5, 5));

    graphPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Ne Labels
    {
      graphPanel.add(new JLabel("NE labels:"));
      final JComboBox labels = new JComboBox(
          JungGraphPreferences.NeLabels.values());
      labels.setSelectedItem(jung.getGraphPreferences().getNeLabel());

      graphPanel.add(labels);
      labels.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          try {
            jung.getGraphPreferences().setNeLabel(
                (JungGraphPreferences.NeLabels) ((JComboBox) e.getSource())
                    .getSelectedItem());
            jung.updateGraphPreferences();
          }
          catch (Exception e1) {
            log.error("Error: ", e1);
          }
        }
      });
    }

    // edge line style
    {
      graphPanel.add(new JLabel("Edge line style:"));
      final JComboBox styles = new JComboBox(
          JungGraphPreferences.LineStyles.values());
      styles.setSelectedItem(jung.getGraphPreferences().getEdgeLineStyle());

      graphPanel.add(styles);
      styles.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          try {
            jung.getGraphPreferences().setEdgeLineStyle(
                (JungGraphPreferences.LineStyles) ((JComboBox) e.getSource())
                    .getSelectedItem());
            jung.updateGraphPreferences();
          }
          catch (Exception e1) {
            log.error("Error: ", e1);
          }
        }
      });
    }

    // 'network-discovered only' edge color
    {
      JButton edgeColor = new JButton("'Network-Discovered Only' Edge Colour");
      graphPanel.add(edgeColor);
      edgeColor.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
          try {
            Color colour = JColorChooser.showDialog(d, "Choose Colour", jung
                .getGraphPreferences().getEdgeColorNetworkDiscovered());
            if (colour != null) {
              jung.getGraphPreferences().setEdgeColorNetworkDiscovered(colour);
              jung.updateGraphPreferences();
            }
          }
          catch (Exception e1) {
            log.error("Error: ", e1);
          }
        }
      });
    }

    // 'manual only' edge color
    {
      JButton edgeColor = new JButton("'Manual Only' Edge Colour");
      graphPanel.add(edgeColor);
      edgeColor.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
          try {
            Color colour = JColorChooser.showDialog(d, "Choose Colour", jung
                .getGraphPreferences().getEdgeColorManual());
            if (colour != null) {
              jung.getGraphPreferences().setEdgeColorManual(colour);
              jung.updateGraphPreferences();
            }
          }
          catch (Exception e1) {
            log.error("Error: ", e1);
          }
        }
      });
    }

    // 'network-discovered and manual' edge color
    {
      JButton edgeColor = new JButton(
          "'Network-Discovered with Manual' Edge Colour");
      graphPanel.add(edgeColor);
      edgeColor.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
          try {
            Color colour = JColorChooser
                .showDialog(d, "Choose Colour", jung.getGraphPreferences()
                    .getEdgeColorNetworkDiscoveredAndManual());
            if (colour != null) {
              jung.getGraphPreferences()
                  .setEdgeColorNetworkDiscoveredAndManual(colour);
              jung.updateGraphPreferences();
            }
          }
          catch (Exception e1) {
            log.error("Error: ", e1);
          }
        }
      });
    }

    // 'selected' edge color
    {
      JButton selectedEdgeColor = new JButton("Selected Edge Colour");
      graphPanel.add(selectedEdgeColor);
      selectedEdgeColor.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
          try {
            Color colour = JColorChooser.showDialog(d, "Choose Colour", jung
                .getGraphPreferences().getEdgeColorSelected());
            if (colour != null) {
              jung.getGraphPreferences().setEdgeColorSelected(colour);
              jung.updateGraphPreferences();
            }
          }
          catch (Exception e1) {
            log.error("Error: ", e1);
          }
        }
      });
    }

    JButton resetButton = new JButton("Use defaults");
    resetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        try {
          jung.getGraphPreferences().resetToDefaults();
          jung.updateGraphPreferences();
        }
        catch (Exception e1) {
          log.error("Error: ", e1);
        }
        d.setVisible(false);
        d.dispose();
      }
    });

    buttonPanel.add(resetButton);

    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        d.setVisible(false);
        d.dispose();
      }
    });

    buttonPanel.add(okButton);
    mainPanel.add(buttonPanel, BorderLayout.EAST);
    mainPanel.add(graphPanel, BorderLayout.NORTH);

    d.getContentPane().setLayout(new GridLayout(1, 1, 5, 5));
    d.getContentPane().add(mainPanel);
    d.setLocation(new Point(parent.getLocation().x + 250,
        parent.getLocation().y + 400));

    d.pack();
    d.setVisible(true);
  }
}
