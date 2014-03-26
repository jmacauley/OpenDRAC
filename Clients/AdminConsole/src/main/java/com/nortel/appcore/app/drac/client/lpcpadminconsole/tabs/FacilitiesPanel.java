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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeCache;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeIeee;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.DracTableModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.JTextFieldLimit;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.NetworkGraph;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation.EditFacilityHolder;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperationCallback;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.AidCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.Constraints;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TablePanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TidCellRenderer;
import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.FacilityConstants.SIGNAL_TYPE;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.Site;

/**
 * @author pitman
 */
public final class FacilitiesPanel implements ServerOperationCallback {
  private static final Logger log = LoggerFactory.getLogger(FacilitiesPanel.class);

  /**
   * This class just holds all the *rap associated with the edit facility dialog
   * so that its all in once place to enjoy.
   */
  static class EditFacilities {
    private final FacilitiesPanel parent;
    private final Frame parentFrame;
    private JDialog editFacilityDialog;

    public EditFacilities(Frame windowFrame, FacilitiesPanel ourParent) {
      parentFrame = windowFrame;
      parent = ourParent;
    }

    public void clearEditFacilityDialog() {
      if (editFacilityDialog != null) {
        editFacilityDialog.removeAll();
      }
    }

    public void showEditFacilityDialog(final Facility facToEdit) {

      JPanel leftGridPanel = new JPanel(new GridLayout(12, 1, 5, 5));
      JPanel rightGridPanel = new JPanel(new GridLayout(12, 1, 5, 5));
      JPanel buttonPanel = new JPanel();
      JPanel eastButtonPanel = new JPanel();
      JPanel tnaPanel = new JPanel(new BorderLayout(5, 5));
      JPanel labelPanel = new JPanel(new BorderLayout(5, 5));

      JLabel neLabel = new JLabel(OpenDracDesktop.NETWORK_ELEMENT_STR);
      JLabel aidLabel = new JLabel("Aid");
      JLabel costLabel = new JLabel("Cost");
      JLabel metric2Label = new JLabel("Metric");
      JLabel tnaLabel = new JLabel("TNA");
      JLabel labelLabel = new JLabel("User Label");
      JLabel groupLabel = new JLabel("Group");
      JLabel srlgLabel = new JLabel("SRLG");
      JLabel mtuLabel = new JLabel("MTU");
      JLabel sigTypeLabel = new JLabel("Signaling Type");
      JLabel siteLabel = new JLabel("Site");

      final JTextField neField = new JTextField();
      final JTextField aidField = new JTextField();
      final JTextField costField = new JTextField();
      final JTextField metric2Field = new JTextField();
      final JTextField tnaField = new JTextField();
      tnaField.setDocument(new JTextFieldLimit(64));
      final JTextField labelField = new JTextField();
      labelField.setDocument(new JTextFieldLimit(64));
      final JTextField groupField = new JTextField();
      final JTextField srlgField = new JTextField();
      final JTextField mtuField = new JTextField();
      final JComboBox siteBox = new JComboBox();
      final JTextField domainField = new JTextField();
      final JComboBox sigTypeBox = new JComboBox();

      JButton okButton = new JButton("Ok");
      JButton cancelButton = new JButton("Cancel");
      JButton resetButton = new JButton("Reset to unassigned");

      JPanel constraintsPanel = new JPanel(new BorderLayout());

      JButton autoTNAButton = new JButton("Generate");

      final Constraints constraints = buildConstraintsPanel(facToEdit.getNeId(), facToEdit.getAid(),
          facToEdit.getPrimaryState(), facToEdit);

      neLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      aidLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      costLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      metric2Label.setHorizontalAlignment(SwingConstants.RIGHT);
      tnaLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      labelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      groupLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      srlgLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      mtuLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      sigTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

      siteLabel.setHorizontalAlignment(SwingConstants.RIGHT);

      editFacilityDialog = new JDialog(parentFrame);

      resetButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          try {
            tnaField.setText("N/A");
            labelField.setText("N/A");
            metric2Field.setText("1");
            costField.setText("1");
            groupField.setText("none");
            srlgField.setText("N/A");
            siteBox.setSelectedIndex(0);
            sigTypeBox.setSelectedItem(FacilityConstants.SIGNAL_TYPE.unassigned.toString());
          }
          catch (Exception t) {
            log.error("reset action failed", t);
          }
        }
      });

      okButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {

          /* First, check whether the tna specified is unique */

          boolean validationOK = true;

          /*
           * If the TNA was modified, check that it is unique across all TNAs
           */
          if (!tnaField.getText().trim().equals(facToEdit.getTna())) {
            String toValidate = tnaField.getText();
            if (toValidate != null && !OpenDracDesktop.NOT_APPLICABLE.equals(toValidate)) {
              if (new ServerOperation().tnaExists(tnaField.getText())) {
                JOptionPane.showMessageDialog(editFacilityDialog, "Error: The specified TNA (" + tnaField.getText()
                    + ") is already assigned.", "Error", JOptionPane.ERROR_MESSAGE);
                validationOK = false;
              }
            }
          }

          // Old implementation did not work. Will leave this out for now.
          // overlapCount = so.getOverlapServiceCount(neid, aidField.getText(),
          // dbServer,
          // dbServerPort);
          String newSigType = (String) sigTypeBox.getSelectedItem();
          String siteId = (String) siteBox.getSelectedItem();
          String domainId = domainField.getText();

          if (!newSigType.equals(facToEdit.getSigType()) && !facToEdit.getSigType().equals("unassigned")) {
            if (newSigType.endsWith("NI") || facToEdit.getSigType().endsWith("NI")) {
              int op = JOptionPane.showConfirmDialog(editFacilityDialog,
                  "Warning! Changing NNI signaling may be service affecting.  Are you sure ?", "Warning",
                  JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
              if (op == JOptionPane.YES_OPTION) {
                validationOK = true;
              }
              else {
                validationOK = false;
              }
            }
          }

          if (validationOK) {
            sendEditFacility(facToEdit.getNeId(), aidField.getText(), tnaField.getText(), labelField.getText(),
                srlgField.getText(), costField.getText(), metric2Field.getText(), groupField.getText(),
                mtuField.getText(), (String) sigTypeBox.getSelectedItem(), constraints, domainId, siteId);
          }
        }
      });

      cancelButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
          editFacilityDialog.setVisible(false);
          editFacilityDialog.dispose();
        }
      });

      // skip in test mode
      if (parent == null) {
        neField.setText("standAloneTestClient");
      }
      else {
        neField.setText(NeCache.INSTANCE.getVertex(facToEdit.getNeId()).getLabel());
      }
      aidField.setText(facToEdit.getAid());
      costField.setText(facToEdit.getCost());
      costField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent ke) {
          if (!Character.isDigit(ke.getKeyChar())) {
            ke.consume();
          }
        }
      });
      metric2Field.setText(facToEdit.getMetric2());
      metric2Field.addKeyListener(new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent ke) {
          if (!Character.isDigit(ke.getKeyChar())) {
            ke.consume();
          }
        }
      });

      tnaField.setText(facToEdit.getTna());
      labelField.setText(facToEdit.getUserLabel());
      srlgField.setText(facToEdit.getSrlg());
      groupField.setText(facToEdit.getGroup());
      mtuField.setText(facToEdit.getMtu());
      mtuField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent ke) {
          if (!Character.isDigit(ke.getKeyChar())) {
            ke.consume();
          }
        }
      });

      FacilitiesPanel.populateSigTypeBox(sigTypeBox, facToEdit.getAid());
      sigTypeBox.removeItem(OpenDracDesktop.ALL_STR);
      /*
       * Remove the string All as this is not a valid sig type in the edit
       * facility editFacilityDialog
       */
      sigTypeBox.setSelectedItem(facToEdit.getSigType());
      neField.setEditable(false);
      aidField.setEditable(false);
      populateSiteBox(siteBox);

      siteBox.setSelectedItem(facToEdit.getSite());

      tnaField.setMinimumSize(new Dimension(300, 10));
      tnaField.setPreferredSize(new Dimension(300, 10));
      tnaField.setSize(new Dimension(300, 10));
      tnaPanel.add(tnaField, BorderLayout.CENTER);
      tnaPanel.add(autoTNAButton, BorderLayout.EAST);

      labelPanel.add(labelField, BorderLayout.CENTER);

      leftGridPanel.add(neLabel);
      rightGridPanel.add(neField);
      leftGridPanel.add(aidLabel);
      rightGridPanel.add(aidField);
      leftGridPanel.add(tnaLabel);
      rightGridPanel.add(tnaPanel); // tnaField );
      leftGridPanel.add(labelLabel);
      rightGridPanel.add(labelPanel);
      leftGridPanel.add(costLabel);
      rightGridPanel.add(costField);
      leftGridPanel.add(metric2Label);
      rightGridPanel.add(metric2Field);
      leftGridPanel.add(groupLabel);
      rightGridPanel.add(groupField);
      leftGridPanel.add(srlgLabel);
      rightGridPanel.add(srlgField);
      leftGridPanel.add(sigTypeLabel);
      rightGridPanel.add(sigTypeBox);
      leftGridPanel.add(siteLabel);
      rightGridPanel.add(siteBox);
      // Only add the MTU field if it's an ETH facility
      if (Facility.isEth(aidField.getText())) {
        leftGridPanel.add(mtuLabel);
        rightGridPanel.add(mtuField);
      }

      sigTypeBox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
          if (FacilityConstants.SIGNAL_TYPE.INNI.toString().equals(sigTypeBox.getSelectedItem())
              || FacilityConstants.SIGNAL_TYPE.ENNI.toString().equals(sigTypeBox.getSelectedItem()))
          // && !aidField.getText().startsWith("ETH"))
          {
            // ((CardLayout) cardsPanel.getLayout()).show(cardsPanel,
            // "inniConstraints");
            if (FacilityConstants.SIGNAL_TYPE.OSS.toString().equals(facToEdit.getPrimaryState())
                || "unassigned".equals(facToEdit.getSigType())) {
              if (constraints != null) {

                constraints.setEnabled(true);
              }
            }
            else {
              if (constraints != null) {

                constraints.setEnabled(true);
              }
            }
          }
          else {
            if (constraints != null) {

              constraints.setEnabled(false);
            }
          }
        }
      });

      autoTNAButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
          tnaField.setText(neField.getText() + "_" + aidField.getText());
        }
      });

      eastButtonPanel.add(resetButton);
      eastButtonPanel.add(okButton);
      eastButtonPanel.add(cancelButton);
      buttonPanel.setLayout(new BorderLayout(1, 1));
      buttonPanel.add(eastButtonPanel, BorderLayout.EAST);

      if (constraints != null) {
        constraintsPanel.add(constraints, BorderLayout.CENTER);
      }

      JPanel topPanel = new JPanel(new BorderLayout(5, 0));
      topPanel.add(leftGridPanel, BorderLayout.WEST);
      topPanel.add(rightGridPanel, BorderLayout.CENTER);

      JPanel borderPanel = new JPanel(new BorderLayout(1, 1));
      borderPanel.setBorder(BorderFactory.createTitledBorder("constraints"));
      borderPanel.add(constraintsPanel, BorderLayout.CENTER);

      JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
      mainPanel.add(topPanel, BorderLayout.NORTH);
      mainPanel.add(borderPanel, BorderLayout.CENTER);
      mainPanel.add(buttonPanel, BorderLayout.SOUTH);
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      editFacilityDialog.setTitle(EDIT_FAC_STR);
      editFacilityDialog.getContentPane().setLayout(new GridLayout(1, 1, 10, 10));
      editFacilityDialog.getContentPane().add(mainPanel);

      if ("INNI".equals(sigTypeBox.getSelectedItem()) || "ENNI".equals(sigTypeBox.getSelectedItem())) {
        if (constraints != null) {

          constraints.setEnabled(true);
        }
      }
      else {
        if (constraints != null) {

          constraints.setEnabled(false);
        }
      }

      editFacilityDialog.pack();
      editFacilityDialog.setLocationRelativeTo(parentFrame);
      editFacilityDialog.setVisible(true);
    }

    private Constraints buildConstraintsPanel(String neid, String facType, String priState, Facility f) {

      /**
       * @TOOD Wayne July 2010. Not sure if this method is required, we have
       *       just fetched the entire facility object, including constraints,
       *       now we go back to the server to fetch the constraints again...
       *       what is the purpose?
       */
      BigInteger constraints = fetchConstraints(neid, facType);

      /**
       * Sanity check to see if the above is required.
       */

      BigInteger fc = null;
      if (f.getConstraint() != null) {
        fc = new BigInteger(f.getConstraint());
      }

      if (constraints == null && fc == null || constraints != null && constraints.equals(fc)) {
        // both are null or both are equal, meaning we did not need to fetch
        // them again
      }
      else {
        log.error("Oops facility constraints did not equal fetched constraints ! fetched:" + constraints + " facility:"
            + fc + " from fac " + f.toString());
      }

      if (facType != null && facType.startsWith("OC") || facType.startsWith("STM")) {
        boolean enable = false;
        if (priState != null) {
          enable = !priState.startsWith("IS");
        }
        return buildVisualiser(facType, enable, constraints);
      }

      return null;
    }

    private Constraints buildVisualiser(String rate, boolean enabled, BigInteger constraints) {
      return new Constraints(rate, enabled, constraints, false, true);
    }

    private BigInteger fetchConstraints(String neid, String aid) {
      /**
       * @TOOD Wayne July 2010. Not sure if this method is required, we have
       *       just fetched the entire facility object, including constraints,
       *       now we go back to the server to fetch the constraints again...
       *       what is the purpose?
       */
      if (parent == null) {
        // test mode
        return BigInteger.ZERO;
      }
      return new ServerOperation().getConstraints(neid, aid);
    }

    private void populateSiteBox(JComboBox box) {
      try {
        if (box != null) {
          box.removeAllItems();
          // Add the default item
          box.addItem("N/A");

          // skip when testing stand alone
          if (parent != null) {
            List<Site> sites = parent.desktop.getNRBHandle().retrieveSiteList(parent.desktop.getLoginToken());
            for (Site site : sites) {
              box.addItem(site.getId());
            }
          }
        }
      }
      catch (Exception e) {
        log.error("populateSiteBox failed", e);
      }
    }

    private void sendEditFacility(String neid, String aid, String tna, String label, String srlg, String cost,
                                  String metric2, String group, String mtu, String sigType, Constraints constraints,
                                  String domainId, String siteId) {

      // String editFacilityCmd = null;

      if ("".equals(tna)) {
        tna = "N/A";
      }
      if ("".equals(label)) {
        label = "N/A";
      }
      if ("".equals(srlg)) {
        srlg = "N/A";
      }
      if ("".equals(cost)) {
        cost = "1";
      }
      if ("".equals(metric2)) {
        metric2 = "1";
      }
      if ("".equals(group)) {
        group = "N/A";
      }
      if ("".equals(mtu)) {
        mtu = "N/A";
      }
      if ("".equals(domainId)) {
        domainId = "N/A";
      }
      if ("".equals(siteId)) {
        siteId = "N/A";
      }
      if ("".equals(sigType)) {
        sigType = FacilityConstants.SIGNAL_TYPE.OSS.toString();
      }

      String inniConstraints = "0";
      if (constraints != null && !sigType.equals(FacilityConstants.SIGNAL_TYPE.OSS.toString())) {
        inniConstraints = constraints.getConstraints().toString();
      }

      EditFacilityHolder holder = new EditFacilityHolder();
      holder.aid = aid;
      holder.constraints = inniConstraints;
      holder.cost = cost;
      holder.domainId = domainId;
      holder.faclabel = label;
      holder.grp = group;
      holder.metric2 = metric2;
      holder.mtu = mtu;
      holder.neid = neid;
      holder.sigType = sigType;
      holder.siteId = siteId;
      holder.srlg = srlg;
      holder.tna = tna;

      log.debug("Sending editfacility command: " + holder);

      Thread edFacThread = new Thread(new ServerOperation(ServerOperation.Operation.OP_EDT_FACILITY, parent, holder),
          OpenDracDesktop.SVR_OP_THREAD_NAME);
      edFacThread.setDaemon(true);
      edFacThread.start();
    }

  }

  private static final String TYPE_STR = "Type";
  private static final String EDIT_FAC_STR = "Edit Facility";
  private static final String FACILITY_AID_STR = "Facility AID";

  private static final String PRISTATE_IS_STR = "IS";
  private static final String PRISTATE_OOSMA_STR = "OOS-MA";
  private static final String PRISTATE_OOSAU_STR = "OOS-AU";

  private static final int FACTBL_DETAILS_IDX = 22; /*
                                                     * the last and hidden
                                                     * column we store the
                                                     * facility in
                                                     */

  private final List<String> facilityColumns = Arrays.asList(new String[] { OpenDracDesktop.NE_STR, TYPE_STR,
      FACILITY_AID_STR, "Signaling Type", "TNA", "User Label", "Primary State", "Cost", "Metric2", "MTU", "VCAT",
      "APS ID", "Wavelength", "SRLG", "Group(s)", "Domain", "Site", "VALID", "Card Type", "Card PEC", "Port Type",
      "Port PEC", "-- HIDDEN --" });

  private final TablePanel tablePanel = new TablePanel(new DracTableModel<Object>(null, facilityColumns), null, null);
  private final JComboBox sigFilterBox = new JComboBox();
  private final JComboBox stateFilterBox = new JComboBox();
  private final JComboBox neFilterBox = new JComboBox();
  private final OpenDracDesktop desktop;
  private List<Facility> facilities;
  private final EditFacilities editFacilities = new EditFacilities(this.desktop, this);

  // This runs on a background thread
  private TablePanel.CustomExportFormatterI exportFormatter = new TablePanel.CustomExportFormatterI() {

    @Override
    public String getCustomExportString(String delim, String timestamp, String title, TableModel tm) {

      StringBuilder sb = new StringBuilder(1000);

      if (facilities != null) {
        sb.append("# ");
        if (title != null) {
          sb.append(title + " ");
        }
        sb.append(timestamp);
        sb.append("\n");
        sb.append("# NEname; NE Ip; Port Location; Port Type; Port Status; Port Identifier; DRAC-owned Routing Capacity");
        sb.append("\n");

        Map<String, String> allNesMap = NeCache.INSTANCE.getIeeeLabelMap();

        for (Facility fac : facilities) {

          String sigType = fac.getSigType();
          String aidType = fac.getType();
          Layer layerEnum = fac.getLayer();

          sb.append(allNesMap.get(fac.getNeId()) + delim);
          sb.append(fac.getNeIp() + ":" + fac.getNePort() + delim);

          sb.append(aidStripType(fac.getAid()) + delim);
          sb.append(sigType + delim);
          sb.append(fac.getPrimaryState() + delim);
          sb.append(aidType + delim);

          // Capacity

          if (layerEnum == Layer.LAYER0) {
            continue;
          }
          else if (layerEnum == Layer.LAYER1) {
            if ("INNI".equals(sigType) || "ENNI".equals(sigType)) {
              String cS = fac.getConstraint();
              BigInteger cBI = new BigInteger(cS);
              Constraints constraints = new Constraints(aidType, true, cBI, false, true);
              List<String> unconstrainedChannels = constraints.getUnconstrainedChannels();
              Iterator<String> chItr = unconstrainedChannels.iterator();
              while (chItr.hasNext()) {
                sb.append(chItr.next());
                if (chItr.hasNext()) {
                  sb.append(",");
                }
              }
            }
          }
          else if (layerEnum == Layer.LAYER2) {
            String facRate = Facility.getFacRateDisplay(aidType);
            if (facRate != null) {
              sb.append(facRate);
            }
          }

          sb.append("\n");
        }

      }
      return sb.toString();
    }

    private String aidStripType(String aid) {
      String port = aid;

      if (aid != null && aid.length() > 0) {
        if (Character.isLetter(aid.charAt(0))) {
          int idx = aid.indexOf("-");
          if (idx > 0) {
            port = aid.substring(idx + 1);
          }
        }
      }

      return port;
    }

  };

  public FacilitiesPanel(OpenDracDesktop d) {
    desktop = d;
  }

  private static void populateSigTypeBox(JComboBox box, String aid) {
    box.addItem(OpenDracDesktop.ALL_STR);
    for (SIGNAL_TYPE s : FacilityConstants.SIGNAL_TYPE.values()) {
      box.addItem(s.toString());
    }

    if (Facility.isL2(aid)) {
      box.removeItem(FacilityConstants.SIGNAL_TYPE.INNI.toString());
    }
  }

  public JPanel buildSlatPanel() {
    final JButton retrieveButton = new JButton("Retrieve");
    JLabel sigTypeFilterLabel = new JLabel("Signaling Type:");
    JLabel stateFilterLabel = new JLabel("Primary state:");
    JLabel neFilterLabel = new JLabel("NE:");

    retrieveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        try {
          retrieveButton.setEnabled(false);
          ((DracTableModel<Object>) tablePanel.getTable().getModel()).clearTable();
          String neIeee = OpenDracDesktop.ALL_STR;
          Object o = neFilterBox.getSelectedItem();
          if (o instanceof NeIeee) {
            neIeee = ((NeIeee) o).getIeee();
          }

          getFilteredFacilities((String) stateFilterBox.getSelectedItem(), (String) sigFilterBox.getSelectedItem(),
              neIeee);
        }
        finally {
          retrieveButton.setEnabled(true);
        }
      }
    });

    neFilterBox.setEditable(false);
    neFilterBox.setPreferredSize(new Dimension(160, 21));
    neFilterBox.addItem(OpenDracDesktop.ALL_STR);
    neFilterBox.setSelectedItem(OpenDracDesktop.ALL_STR);
    neFilterBox.addPopupMenuListener(new PopupMenuListener() {
      boolean willBecomeVisible;

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
        return;
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        return;
      }

      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        // Workaround for bug:
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4743225
        if (!willBecomeVisible) {
          JComboBox list = (JComboBox) e.getSource();

          neFilterBox.removeAllItems();
          desktop.populateNEBoxWithBackingIEEE(neFilterBox);

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

    populatePriStateBox(stateFilterBox);
    populateSigTypeBox(sigFilterBox, null);

    JPanel controlsPanel = new JPanel(new BorderLayout(1, 1));

    JPanel filtersPanel = new JPanel();
    filtersPanel.add(neFilterLabel);
    filtersPanel.add(neFilterBox);
    filtersPanel.add(stateFilterLabel);
    filtersPanel.add(stateFilterBox);
    filtersPanel.add(sigTypeFilterLabel);
    filtersPanel.add(sigFilterBox);

    // Trac #65: Export function of ports and timeslots
    JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 3, 3));
    buttonPanel.add(retrieveButton);
    buttonPanel.add(tablePanel.getExportButton(desktop, exportFormatter));

    controlsPanel.add(filtersPanel, BorderLayout.CENTER);
    controlsPanel.add(buttonPanel, BorderLayout.EAST);

    tablePanel.getTable().addMouseListener(new MouseAdapter() {
      // Some platforms (like LINUX only detect the mouse pressed)
      @Override
      public void mousePressed(MouseEvent me) {
        if (me.isPopupTrigger()) {
          showFacilityPopup((Component) me.getSource(), me.getX(), me.getY());
        }
      }

      @Override
      public void mouseReleased(MouseEvent me) {
        if (me.isPopupTrigger()) {
          showFacilityPopup((Component) me.getSource(), me.getX(), me.getY());
        }
      }
    });

    TableColumn col = tablePanel.getTable().getColumn(OpenDracDesktop.NE_STR);
    Map<String, String> allNesMap = NeCache.INSTANCE.getIeeeLabelMap();
    if (col != null) {
      col.setCellRenderer(new TidCellRenderer(allNesMap));
    }

    TableColumn facCol = tablePanel.getTable().getColumn(FACILITY_AID_STR);
    if (col != null) {
      facCol.setCellRenderer(new AidCellRenderer(allNesMap));
    }

    TableColumn typeCol = tablePanel.getTable().getColumn(TYPE_STR);
    if (col != null) {
      typeCol.setCellRenderer(new AidCellRenderer(allNesMap));
    }

    tablePanel.addButton(controlsPanel);
    return tablePanel.getWrappedPanel("Facility Management");

  }

  @Override
  public void handleServerOperationResult(ServerOperation op) {
    List<? extends RowSorter.SortKey> previousSorterKeys = tablePanel.getLastSorterKeys();

    Map<String, Object> result = op.getResult();
    if (op.getOperation() == ServerOperation.Operation.OP_GET_ALL_FACILITIES) {
      facilities = (List<Facility>) result.get(ServerOperation.MAP_RESULT_KEY);

      if (facilities != null) {

        List<List<Object>> rows = new ArrayList<List<Object>>();
        List<Object> row = null;

        try {

          for (Facility f : facilities) {
            row = new ArrayList<Object>();

            row.add(f.getNeId());
            row.add(f.getType());
            row.add(f.getAid());
            row.add(f.getSigType());
            row.add(f.getTna());
            row.add(f.getUserLabel());
            row.add(f.getPrimaryState());
            row.add(f.getCost());
            row.add(f.getMetric2());
            row.add(f.getMtu());
            row.add(f.getVcat());
            row.add(f.getApsid());
            row.add(f.getWavelength());
            row.add(f.getSrlg());
            row.add(f.getGroup());
            row.add(f.getDomain());
            row.add(f.getSite());
            row.add(f.get(DbKeys.NetworkElementFacilityCols.VALID) == null ? "" : f
                .get(DbKeys.NetworkElementFacilityCols.VALID));

            row.add(f.getCardType() == null ? "" : f.getCardType());
            row.add(f.getCardPec() == null ? "" : f.getCardPec());
            row.add(f.getPortType() == null ? "" : f.getPortType());
            row.add(f.getPortPec() == null ? "" : f.getPortPec());

            // Hide the raw facility in the last column, used for details.
            row.add(f);
            rows.add(row);
          }
        }
        catch (Exception e) {
          log.error("Unable to add facility to table offending facility on of " + facilities);
        }
        ((DracTableModel<Object>) tablePanel.getTable().getModel()).setData(rows);

        tablePanel.getTable().doLayout();
        Map<String, String> allNes = NeCache.INSTANCE.getIeeeLabelMap();
        TableColumn col = tablePanel.getTable().getColumn(OpenDracDesktop.NE_STR);
        if (col != null) {
          col.setCellRenderer(new TidCellRenderer(allNes));
        }
        TableColumn facCol = tablePanel.getTable().getColumn(FACILITY_AID_STR);
        if (col != null) {
          facCol.setCellRenderer(new AidCellRenderer(allNes));
        }

        tablePanel.getTable().removeColumn(tablePanel.getTable().getColumnModel().getColumn(FACTBL_DETAILS_IDX)); // REMOVE
        tablePanel.updateTable();
        tablePanel.sortTable(previousSorterKeys);
      }
      else {
        JOptionPane.showMessageDialog(desktop, "Error occurred retrieving facilities.", "Error",
            JOptionPane.ERROR_MESSAGE);
      }
      desktop.hideProgressDialog();
    }
    else if (op.getOperation() == ServerOperation.Operation.OP_EDT_FACILITY) {
      Exception ex = (Exception) result.get(ServerOperation.MAP_RESULT_KEY);

      if (ex != null) {
        log.error("Edit facility failed " + ex, ex);
        JOptionPane.showMessageDialog(desktop, "Failed to edit facility: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
      }
      else {
        log.debug("Edit facility completed ");
        if (editFacilities.editFacilityDialog != null) {
          String neIeee = OpenDracDesktop.ALL_STR;
          Object o = neFilterBox.getSelectedItem();
          if (o instanceof NeIeee) {
            neIeee = ((NeIeee) o).getIeee();
          }

          getFilteredFacilities((String) stateFilterBox.getSelectedItem(), (String) sigFilterBox.getSelectedItem(),
              neIeee);
          editFacilities.editFacilityDialog.setVisible(false);
        }

        JOptionPane.showMessageDialog(desktop, "Facility successfully edited.", "Result",
            JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }

  private void getFilteredFacilities(String stateFilter, String sigTypeFilter, String neFilter) {
    Map<String, String> filter = new HashMap<String, String>();

    if (!"All".equals(neFilter)) {
      filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, neFilter);
    }

    if (!"All".equals(stateFilter)) {
      // I'm allowing for substring matching e.g. OOS*
      filter.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE, stateFilter.replace('*', '%'));
    }

    if (!"All".equals(sigTypeFilter)) {
      filter.put(DbKeys.NetworkElementFacilityCols.SIGTYPE, sigTypeFilter);
    }

    Thread t = new Thread(new ServerOperation(ServerOperation.Operation.OP_GET_ALL_FACILITIES, this, filter),
        OpenDracDesktop.SVR_OP_THREAD_NAME + ": getAllFacilities()");
    desktop.showProgressDialog("Retrieving facilities...");
    t.setDaemon(true);
    t.start();
  }

  private void populateDetailsTable(TablePanel tableToPopulate, Map<String, String> details) {
    List<List<String>> rows = new ArrayList<List<String>>();

    if (details != null) {
      for (Map.Entry<String, String> e : details.entrySet()) {
        List<String> row = new ArrayList<String>();
        row.add(e.getKey());
        row.add(e.getValue());
        rows.add(row);
      }
    }
    ((DracTableModel<String>) tableToPopulate.getTable().getModel()).setData(rows);
    tableToPopulate.updateTable();
  }

  private void populatePriStateBox(JComboBox box) {
    box.addItem(OpenDracDesktop.ALL_STR);
    box.addItem(PRISTATE_IS_STR);
    box.addItem(PRISTATE_OOSMA_STR);
    box.addItem(PRISTATE_OOSAU_STR);
    box.addItem("OOS*");
  }

  private void showFacilityPopup(Component source, int x, int y) {
    JPopupMenu popup = new JPopupMenu();
    JMenuItem details = new JMenuItem("Details");
    JMenuItem findNe = new JMenuItem("Find Ne");
    JMenuItem itemModify = new JMenuItem("Edit");
    JMenuItem itemFind = new JMenuItem("Find services");
    JMenuItem deleteFacs = new JMenuItem("Delete");

    deleteFacs.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int[] row = tablePanel.getTable().getSelectedRows();

        List<Facility> toDelete = new ArrayList<Facility>();
        for (int r : row) {

          Facility f = (Facility) tablePanel.getTable().getModel()
              .getValueAt(tablePanel.getTable().convertRowIndexToModel(r), FACTBL_DETAILS_IDX);
          toDelete.add(f);
        }

        int result = JOptionPane
            .showConfirmDialog(
                null,
                "<HTML>Are you sure you want to remove "
                    + tablePanel.getTable().getSelectedRowCount()
                    + " Facilities?<BR><B>All manually provisioned data will be lost</B>The association to all impacted Network Elements will be toggled to refresh the facility list.</HTML>",
                "Remove Facilities", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
          // Delete all selected facilities.

          Set<NetworkElementHolder> nes = new HashSet<NetworkElementHolder>();
          for (Facility f : toDelete) {
            try {
              new ServerOperation().deleteFacility(f.getNeId(), f.getAid());
              nes.add(new NetworkElementHolder(f.getNeIp(), f.getNePort(), null, null, null, NeStatus.NE_UNKNOWN, null,
                  null, 1, null, null, f.getNeId(), null, false, "Unknown", null, null, null));
            }
            catch (Exception e1) {
              log.error("failed to delete facility " + f.getNeId() + "/" + f.getAid(), e1);
            }
          }

          for (NetworkElementHolder ne : nes) {
            try {
              log.debug("Toggleing assocation to NE " + ne);
              new ServerOperation().toggleNeAssociation(ne);
            }
            catch (Exception e1) {
              log.error("failed to toggle Ne association on " + ne);
            }
          }
        }
      }
    });

    details.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int row = tablePanel.getTable().getSelectedRow();
        int modelRow = tablePanel.getTable().convertRowIndexToModel(row);
        TablePanel detailEventPanel = new TablePanel(new DracTableModel<String>(null, Arrays.asList(new String[] {
            "Attribute", "Value" })), null, null);

        // @TODO Add a close button to the dialog to dismiss it?

        // Fetch the details for the event
        log.debug("asking for facility details on table row " + row + " model row " + modelRow + " with "
            + tablePanel.getTable().getColumnCount() + " columns ");

        populateDetailsTable(detailEventPanel,
            ((Facility) tablePanel.getTable().getModel().getValueAt(modelRow, FACTBL_DETAILS_IDX)).asUnmodifiableMap());

        JDialog detailsDialog = new JDialog(desktop);
        detailsDialog.getContentPane().add(detailEventPanel.getWrappedPanel("Facility Details"), BorderLayout.CENTER);
        detailsDialog.setTitle("Facility Details");
        detailsDialog.setSize(new Dimension(600, 300));
        detailsDialog.setLocationRelativeTo(desktop);
        detailsDialog.setVisible(true);

      }

    });

    findNe.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          int row = tablePanel.getTable().getSelectedRow();
          int modelRow = tablePanel.getTable().convertRowIndexToModel(row);
          log.debug("findNe on table row " + row + " model row " + modelRow + " with "
              + tablePanel.getTable().getColumnCount() + " columns ");
          Facility f = (Facility) tablePanel.getTable().getModel().getValueAt(modelRow, FACTBL_DETAILS_IDX);
          String tid = f.getNeId();
          log.debug("Find NE Looking for " + tid);
          DracVertex v = NeCache.INSTANCE.getVertex(tid);
          log.debug("Find NE found vertex " + v);
          if (v != null) {
            NetworkGraph.INSTANCE.highlightNELike(v.getLabel());
          }
        }
        catch (Exception e1) {
          log.error("Error: ", e1);
        }
      }
    });

    itemModify.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        int row = tablePanel.getTable().getSelectedRow();
        int modelRow = tablePanel.getTable().convertRowIndexToModel(row);
        // Fetch the details for the event
        log.debug("asking to edit facility on " + row + " model row " + modelRow + " with "
            + tablePanel.getTable().getColumnCount() + " columns ");
        Facility f = (Facility) tablePanel.getTable().getModel().getValueAt(modelRow, FACTBL_DETAILS_IDX);
        editFacilities.clearEditFacilityDialog();
        editFacilities.showEditFacilityDialog(f);
      }
    });

    itemFind.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        int row = tablePanel.getTable().getSelectedRow();
        int modelRow = tablePanel.getTable().convertRowIndexToModel(row);
        // Fetch the details for the event
        log.debug("asking to edit facility on " + row + " model row " + modelRow + " with "
            + tablePanel.getTable().getColumnCount() + " columns ");

        Facility f = (Facility) tablePanel.getTable().getModel().getValueAt(modelRow, FACTBL_DETAILS_IDX);

        String aid = f.getAid();

        log.debug("Looking for services located on NE " + f.getNeId() + " aid : " + aid);
        desktop.getServices(f.getNeId(), aid);
      }
    });

    popup.add(details);
    popup.add(findNe);
    popup.add(itemModify);
    popup.add(itemFind);
    popup.add(deleteFacs);
    popup.show(source, x, y);
  }
}
