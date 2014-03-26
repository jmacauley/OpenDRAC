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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.ServerResponseI;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.dialogs.AuthenticationDialog;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.AdminPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.AlarmPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.AuditPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.CreateScheduleTab;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.FacilitiesPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.LogPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.NeListPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.SchedulePanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.SitesPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.TopologyPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.UserPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.UtilisationPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.JungTopologyPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.NetworkGraph;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.TopologyPopupMenu;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.TopologyViewEventHandler;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.MultiTasker;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.PurgeWorker;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerMonitor;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperationCallback;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.DateDropDown;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;

public final class OpenDracDesktop extends JFrame implements
    ServerOperationCallback, TopologyViewEventHandler, ServerResponseI {
  
  private static final Logger log = LoggerFactory.getLogger(OpenDracDesktop.class);

  public static final String NETWORK_ELEMENT_STR = "Network Element";
  public static final String ALL_STR = "All";
  public static final String METRIC_KEY_COST = "Cost";
  public static final String METRIC_KEY_METRIC2 = "Metric2";
  public static final String METRIC_KEY_HOP = "Weighted Hop Count";
  public static final String SERVICES_STR = "Service";
  public static final String SERVICE_DETAILS_STR = "Service Details";
  public static final String NE_STR = "NE";
  public static final String SVC_STATE_INPROGRESS = "In Progress";
  public static final String CANCEL_SVC_TITLE = "Cancel service";
  public static final String DLT_SVC_WARN_STR = "The following services will be canceled.  Are you sure you wish to continue?";
  public static final String DLT_ACT_SVC_WARN_STR = "Traffic carrying services have been selected for cancellation.  Are you sure you wish to continue?";
  public static final String SVR_MON_THREAD_NAME = "OpenDracDesktop Server Watchdog";
  public static final String SVR_OP_THREAD_NAME = "OpenDracDesktop Server Operation Thread";
  public static final String ID_ATTR = "id";
  public static final String TNA_ATTR = "tna";
  public static final String TYPE_ATTR = "type";
  public static final String WAVELENGTH_ATTR = "wavelength";
  public static final String SITE_ATTR = "siteId";
  public static final String FILTER_NONE = "None";
  public static final String FILTER_SERVICEID = "Service id";
  public static final String FILTER_DATE = "Date";
  public static final int SCHEDULING_TAB_IDX = 4;
  public static final int SCHEDTAB_CRT_SCH_IDX = 1;
  public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
  public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
  public static final Font BASE_FONT = new Font("SanSerif", Font.PLAIN, 10);
  public static final String NOT_APPLICABLE = "N/A";
  private static final int STATE_SERVER_STANDBY = -100;
  private static final int WINDOW_WIDTH = 1154;
  private static final int WINDOW_HEIGHT = 750;
  private static AuthenticationDialog auth;
  private static final String APPLICATION_TITLE = "Open Dynamic Resource Allocation Controller Desktop - ";
  private static final String SVR_RCON_THREAD_NAME = "OpenDRAC Desktop Reconnect Thread";
  private static final String DLT_MANUAL_ADJ_STR = "The selected link will be removed.  Do you wish to continue?";
  private static final String GEN_MANUAL_LINK_STR = "A manual link, equivalent to the selected link, will be created.  Do you wish to continue?";
  private static final String USER_ELEMENT = "user";
  private static final String PREFERENCESLIST_ELEMENT = "preferencesList";
  private static final long serialVersionUID = 1L;
  private static final String PREP_SERVICES_MSG = "Preparing to purge records ...";
  private static final String PURGE_EVENTS_MSG = "Deleting event records ...";
  private static final String PURGE_ERROR_MSG = "Error(s) occurred during the purge operation.";

  public Thread serverThread;
  public final JTabbedPane schedTabPane = new JTabbedPane();
  public JTabbedPane tabbedPane;
  public final Locale locale = this.getLocale();
  public ServerMonitor serverMonitor;
  private final FacilitiesPanel facilitiesPanel = new FacilitiesPanel(this);
  private TimeZone timeZonePreference = TimeZone.getDefault();
  private final JButton statusButton = new JButton();

  private final JTextField statusField = new JTextField();
  private final JButton errorButton = new JButton();
  private JWindow progress;
  private JLabel progressLabel;
  private String timeZoneIDPreference = TimeZone.getDefault().getID();
  private JMenu fileMenu;
  private JMenu helpMenu;
  private JMenuBar mainMenuBar;
  private final TopologyPanel topologyPanel;
  private JButton servicePurgeButton;
  private JButton eventPurgeButton;
  private String maxWorkers;
  private String chunkSize;
  private long purgeConstraint;
  private LoginToken token;
  private final SchedulePanel schedulePanel = new SchedulePanel(this);
  private final AdminPanel adminPanel = new AdminPanel(this);
  private final UtilisationPanel utilisationPanel = new UtilisationPanel(this);
  private final JungTopologyPanel jungTopologyPanel;
  
  private static Properties versionProperties = new Properties();
  
  public OpenDracDesktop(String[] args) throws Exception {
    super();
    versionProperties.load(this.getClass().getResourceAsStream("/release.ext"));
    topologyPanel = new TopologyPanel(this);
    auth = new AuthenticationDialog(this, getVersion(), getBuildDate(), args);
    jungTopologyPanel = new JungTopologyPanel(this, NetworkGraph.INSTANCE);
  }

  
  public static AuthenticationDialog getAuth() {
    return auth;
  }

  /**
   * Runs the AdminConsole.
   */
  public static void main(String[] args) {

    class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
      public void uncaughtException(Thread th, Throwable t) {
        if ((th.getName().contains("AWT-EventQueue") || th.getName().contains(
            "java.awt.EventQueue"))
            && "Mac OS X".equals(System.getProperty("os.name"))) {
          return;
        }
        else {
          log.error("Error: ", t);
        }
      }
    }
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

    try {
      new OpenDracDesktop(args).initialise();
    }
    catch (Exception t) {
      log.error("Error: ", t);
    }

  }

  public static Facility[] sortFacilities(List<Facility> facilities) {
    Facility[] sortedFacilities = new Facility[facilities.size()];
    TreeMap<String, Facility> sorter = new TreeMap<String, Facility>();

    for (Facility f : facilities) {
      sorter.put(f.getAid(), f);
    }

    int idx = 0;
    Iterator<String> it = sorter.keySet().iterator();
    while (it.hasNext()) {
      sortedFacilities[idx] = sorter.get(it.next());
      idx++;
    }
    return sortedFacilities;
  }

  public String getVersion() {
      return versionProperties.getProperty("PRODUCT_VERSION", "unknown");
  }
  public String getBuildDate() {
      return versionProperties.getProperty("BUILD_DATE", "unknown");
  }
  public String getBuilder() {
      return versionProperties.getProperty("BUILDER", "unknown");
  }
  public String getProductname() {
      return versionProperties.getProperty("PRODUCT_NAME", "unknown");
  }
  
  public synchronized void getAndDisplayServerGraph() {
    // showProgressDialog("Retrieving topology...");
    new GetGraphDataWorker(this).execute();
  }


  /**
   * @return the jungTopologyPanel
   */
  public JungTopologyPanel getJungTopologyPanel() {
    return jungTopologyPanel;
  }

  public LoginToken getLoginToken() {
    if (token == null) {
      token = auth.getLoginToken();
    }
    return token;
  }

  public NrbInterface getNRBHandle() {
    return auth.getNrb();
  }


  public void getServices(final String neid, final String aid) {
    Map<String, String> parametersMap = new HashMap<String, String>();
    parametersMap.put(DbKeys.LightPathCols.LP_NEID, neid);
    parametersMap.put(DbKeys.LightPathCols.LP_AID, aid);
    Thread t = new Thread(
        new ServerOperation(ServerOperation.Operation.OP_GET_SERVICES_FROM_AID,
            this, parametersMap), SVR_OP_THREAD_NAME + ": getServices()");
    showProgressDialog("Retrieving services...");
    t.setDaemon(true);
    t.start();
  }

  public TimeZone getTimeZonePreference() {
    return timeZonePreference;
  }

  public void handleInactivateRequest() {
    try {
      String remoteIp = new ServerOperation().getRemoteServerIP();
      NetworkGraph.INSTANCE.clearGraph();
      showProgressDialog("<HTML>Server in standby<BR>Peer: " + remoteIp
          + "</HTML>");
      setStandby();
    }
    catch (Exception e) {
      log.error("Exception handling inactivate request: ", e);
    }
  }

  /**
   * ServerOperationCallback interface method(non-Javadoc)
   * 
   * @see com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperationCallback#handleServerOperationResult(com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation)
   */
  @Override
  public void handleServerOperationResult(ServerOperation op) {
    Map<String, Object> result = op.getResult();

    

    if (op.getOperation() == ServerOperation.Operation.OP_GET_DRAC_INFO) {
      adminPanel.update(result);
    }
    else if (op.getOperation() == ServerOperation.Operation.OP_GET_SERVICES
        || op.getOperation() == ServerOperation.Operation.OP_GET_SERVICES_FROM_AID) {
      @SuppressWarnings("unchecked")
      List<ServiceXml> serviceList = (List<ServiceXml>) result
          .get(ServerOperation.MAP_RESULT_KEY);

      if (serviceList != null) {
        schedulePanel.displaySchedules(serviceList);
      }
      else {
        JOptionPane.showMessageDialog(this,
            "Error occurred retrieving services.", "Error",
            JOptionPane.ERROR_MESSAGE);
      }
      hideProgressDialog();
    }
    else if (op.getOperation() == ServerOperation.Operation.OP_SAVE_PREFERENCES) {
      hideProgressDialog();

      String returnStatus = (String) result.get(ServerOperation.MAP_RESULT_KEY);
      if (ServerOperation.STATUS_SUCCESS.equals(returnStatus)) {
        JOptionPane.showMessageDialog(null,
            "User preferences saved successfully.");
      }
      else {
        JOptionPane.showMessageDialog(this, "Error saving user preferences.",
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    else if (op.getOperation() == ServerOperation.Operation.OP_LOAD_PREFERENCES) {
      hideProgressDialog();
      Element userPreferences = (Element) result
          .get(ServerOperation.MAP_RESULT_KEY);
      applyUserPreferences(userPreferences);
    }
    else {
      log.error("Unsupported command received " + op.getOperation());
    }
  }

  @Override
  public void handleServerResponse(ServerResponseI.Message operation,
      Object result) {
    try {
      switch (operation) {

      case RESPONSE_VERTEX_UPDATED:
        log.debug("Detected association event: " + result);
        Element data = ((Element) result).getChild("data");
        Element node = ((Element) result).getChild("node");
        NeCache.INSTANCE.updateNE(node, data);
        break;
      case EVENT_RETRY:
        showReEstablishWindow();
        break;
      case GRAPH_REFRESH_REQUIRED:
        getAndDisplayServerGraph();
        break;
      case RESPONSE_ACTIVE:
        hideProgressDialog();
        updateStatus("Server active.", true);
        setActive();
        break;
      case RESPONSE_INACTIVE:
        hideProgressDialog();
        updateStatus("Server switched to standby.", true);
        handleInactivateRequest();
        break;
      default:
        log.error("WARNING: Unknown operation: " + operation + " ignored.");
        break;
      }
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleTopologyViewEvents(EVENT_TYPE eventid, Object object) {
    try {
      switch (eventid) {

      case ADDLINK_EVENT: {
        Map<String, String> m = (Map<String, String>) object;
        TopologyPopupMenu.addLinkDialog(this, m.get("SRCIEEE"), m.get("SRCIP"),
            m.get("SRCLABEL"), m.get("DESTIEEE"), m.get("DESTIP"),
            m.get("DESTLABEL"));
        break;
      }
      case REMOVELINK_EVENT:
        log.debug("Remove link event detected: ");
        handleRemoveLinkEvent((Map<String, String>) object);
        break;
      case GENERATEMANUALLINK_EVENT:
        handleGenerateManualLinkEvent((Map<String, String>) object);
        break;
      case SPF_EVENT: {
        Map<String, String> m = (Map<String, String>) object;
        CreateScheduleTab.getInstance(this).showCreateScheduleDialog(
            m.get("srcIeee"), m.get("dstIeee"), m.get("srcLabel"),
            m.get("dstLabel"), m.get("srcMode"), m.get("dstMode"));
        break;
      }
      case MANAGE_EVENT:
        TopologyPopupMenu.addNEDialog(this);
        break;
      case UNMANAGE_EVENT: {
        NetworkElementHolder ne = (NetworkElementHolder) object;
        log.debug("Unmanaging NE " + ne);
        showProgressDialog("Unmanaging network element...");
        new ServerOperation().deleteNetworkElement(ne);
        hideProgressDialog();
        break;
      }
      case TOGGLE_NE_EVENT: {
        NetworkElementHolder ne = (NetworkElementHolder) object;
        log.debug("toggle Ne association NE with ip: " + ne.getIp() + " port: "
            + ne.getPort());
        showProgressDialog("Toggling network element communications...");
        new ServerOperation().toggleNeAssociation(ne);
        hideProgressDialog();

        break;
      }
      case CHANGE_NE_PASSWORD_EVENT: {
        NetworkElementHolder ne = (NetworkElementHolder) object;
        log.debug("Change NE passsword : " + ne.getIp() + ":" + ne.getPort());
        showProgressDialog("Updating NE user and password...");
        new ServerOperation().changeNePassword(ne);
        hideProgressDialog();
        break;
      }
      default:
        break;
      }
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  public void hideProgressDialog() {
    ((JFrame) this).getContentPane().setCursor(DEFAULT_CURSOR);
    if (progress != null) {
      progress.setVisible(false);
    }
  }

  public void populateNEBoxWithBackingIEEE(JComboBox box) {
    box.addItem(ALL_STR);
    for (NeIeee element : NeCache.INSTANCE.getNEListIEEE()) {
      box.addItem(element);
    }
  }

  public void setActive() {
    updateErrorStatus(false, "");
    updateStatus("Server switching to active state", true);
    tabbedPane.setEnabled(true);
    setFileMenuStandby(false);
    adminPanel.enableAddNetworkElementButton(true);
    NetworkGraph.INSTANCE.setEnabled(true);
  }

  public void showProgressDialog(String msg) {
    JPanel mainPanel = null;

    ((JFrame) this).getContentPane().setCursor(WAIT_CURSOR);

    if (progress == null) {
      JProgressBar progressBar = new JProgressBar();

      mainPanel = new JPanel();

      progressBar.setIndeterminate(true);
      progressBar.setBorderPainted(false);
      progressBar.setPreferredSize(new Dimension(100, 3));

      progress = new JWindow(this);
      progressLabel = new JLabel(msg);
      progressLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
      mainPanel.setLayout(new BorderLayout(1, 1));
      mainPanel.add(progressLabel, BorderLayout.CENTER);
      mainPanel.add(progressBar, BorderLayout.SOUTH);
      mainPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      progress.getContentPane().setLayout(new BorderLayout(1, 1));
      progress.getContentPane().add(mainPanel, BorderLayout.CENTER);
      progress.setSize(200, 50);
    }
    else {
      progressLabel.setText(msg);
    }

    if (this.isVisible()) {
      progress.setLocationRelativeTo(this);
    }
    else {
      Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();
      progress.setLocation((screendim.width - 200) / 2,
          (screendim.height - 50) / 2);
    }

    if (!progress.isVisible()) {
      progress.setVisible(true);
    }
  }

  public void updateErrorStatus(boolean status, String statusMessage) {
    errorButton.setBackground(status ? Color.red : null);
    errorButton.setToolTipText(statusMessage);
  }

  public void updateStatus(String text, boolean flash) {
    statusField.setText(text);
    if (flash && statusButton.getBackground() != Color.green) {
      flash(statusButton, Color.green);
    }
  }

  private void applyUserPreferences(Element user) {
    if (user == null) {
      updateStatus(
          "Warning: no user preferences found for user; defaults will be used for visual layout.",
          true);
      return;
    }

    String userId = user.getAttributeValue(ID_ATTR);

    try {
      boolean loaded = true;
      loaded = NetworkGraph.INSTANCE.parseAndSetUserPrefs(user);

      if (loaded) {
        updateStatus("Loaded preferences for user: " + userId + ".", true);
      }
      else {
        updateStatus(
            "User preferences could not be loaded for user: " + userId, true);
      }
    }
    catch (Exception e) {
      updateStatus("Exception loading preferences for user: " + userId + ": "
          + e.getMessage(), true);
      log.error("Error: ", e);
    }

  }

  private void buildAndShowFrame() throws Exception {
    JWindow toolbarWin = new JWindow(this);
    // Set the main Frame's menubar
    mainMenuBar = buildMainMenuBar();
    setJMenuBar(mainMenuBar);

    JPanel bpanel = new JPanel();
    JPanel mainTabbedPanel = new JPanel(new BorderLayout(1, 1));

    bpanel.setLayout(new BorderLayout());
    bpanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    mainTabbedPanel.setMinimumSize(new Dimension(0, 0));

    tabbedPane = new JTabbedPane(SwingConstants.BOTTOM);
    tabbedPane.addTab("Administration", adminPanel.buildAdminPanel());
    tabbedPane.addTab("Event Browser", new LogPanel(this).buildEventLogPanel());
    tabbedPane.addTab("Facilities", facilitiesPanel.buildSlatPanel());
    tabbedPane.addTab("Link Utilisation",
        utilisationPanel.buildUtilisationPanel());
    tabbedPane.addTab("Scheduling", schedulePanel.buildSchedulePanel());
    tabbedPane.addTab("Service Alarms", new AlarmPanel(this).buildAlarmPanel());
    tabbedPane.addTab("Audit", new AuditPanel(this).buildAuditPanel());
    tabbedPane.addTab("Sites", new SitesPanel(this).buildSitesPanel());
    tabbedPane.addTab("Network Elements",
        new NeListPanel(this).buildNePanel());
    tabbedPane.addTab("Users", new UserPanel(this).buildUserPanel());
    tabbedPane.addTab("Topology", topologyPanel.buildTopologyPanel());

    mainTabbedPanel.add(tabbedPane, BorderLayout.CENTER);

    JPanel mainViewTabbedPanel = new JPanel(new BorderLayout(5, 5));

    jungTopologyPanel.addListener(this);
    jungTopologyPanel.getPanel().setMinimumSize(new Dimension(0, 0));
    mainViewTabbedPanel.add(jungTopologyPanel.getPanel(), BorderLayout.CENTER);

    JSplitPane horizSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        mainViewTabbedPanel, mainTabbedPanel);
    horizSplitPane.setDividerLocation(0);
    horizSplitPane.setContinuousLayout(true);
    horizSplitPane.setDividerSize(2);

    bpanel.add(horizSplitPane, BorderLayout.CENTER);

    JPanel mpanel = new JPanel();
    JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
    JPanel statusButtonPanel = new JPanel(new GridLayout(1, 2));

    statusButton.setEnabled(false);
    errorButton.setEnabled(false);

    statusButton.setMargin(new Insets(0, 0, 0, 0));
    errorButton.setMargin(new Insets(0, 0, 0, 0));

    statusButtonPanel.add(errorButton);
    statusButtonPanel.add(statusButton);

    mpanel.setLayout(new BorderLayout());
    statusPanel.add(statusButtonPanel, BorderLayout.WEST);
    statusPanel.add(statusField, BorderLayout.CENTER);

    mpanel.add(bpanel, BorderLayout.CENTER);
    mpanel.add(statusPanel, BorderLayout.SOUTH);

    mpanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    getContentPane().setLayout(new BorderLayout(1, 1));
    getContentPane().add(mpanel, BorderLayout.CENTER);

    pack();

    // Size the frame
    setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

    // Center the frame
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2,
        (screenDim.height - frameDim.height) / 2);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        log.debug("Admin Console shutdown requested");
        auth.logout();
        System.exit(0);
      }
    });

    // Show the JFrame
    setVisible(true);

    // Set the divider location here to force a screen re-draw otherwise the
    // panel will
    // show up blank.
    // horizSplitPane.setOneTouchExpandable(true);
    horizSplitPane.setDividerLocation(WINDOW_WIDTH / 2 - 50);

    // Size the debug toolbar - must come after sizing and positioning the main
    // frame
    toolbarWin
        .setLocation(getLocation().x + getBounds().width, getLocation().y);

    // Do a hard reset (aka shift-reset) to display ne's
    JungTopologyPanel.resetTopology();

  }

  private JMenuBar buildMainMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenuItem saveItem = new JMenuItem("Save preferences");
    JMenuItem exitItem = new JMenuItem("Exit");
    JMenuItem aboutItem = new JMenuItem("About OpenDRAC");

    JMenuItem switchItem = new JMenuItem("Switch server...");

    fileMenu = new JMenu("File");
    helpMenu = new JMenu("Help");

    saveItem.setFont(BASE_FONT);
    exitItem.setFont(BASE_FONT);
    aboutItem.setFont(BASE_FONT);

    switchItem.setFont(BASE_FONT);

    fileMenu.add(saveItem);

    fileMenu.add(exitItem);
    helpMenu.add(aboutItem);

    switchItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            // Request authentication
            if (serverMonitor != null) {
              serverMonitor.stop();
            }
            auth.doLogin();
            auth.closeWindow();
            doPostAuth();

            // Wait for the server status to become "Running"
            showProgressDialog("Waiting for server to restart");
            updateStatus("Waiting for server to restart.", true);
            while (!auth.isControllerRunning()) {
              if (auth.isControllerInactive()) {
                handleInactivateRequest();
              }
              Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }

            setupMonitors();

            updateStatus("Server restart complete.", true);
            updateErrorStatus(false, "");

            hideProgressDialog();
            getAndDisplayServerGraph();

          }
        });
        t.setDaemon(true);
        t.start();

      }
    });

    saveItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        try {
          saveUserPreferencesJDOM();
        }
        catch (Exception ce) {
          log.error("Error: ", ce);
          JOptionPane.showMessageDialog(null,
              "An error occurred saving preferences: " + ce.getMessage()
                  + ".  Preferences have not been saved.");
        }
      }
    });

    exitItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        int option = JOptionPane.showConfirmDialog(null,
            "Are you sure you want to exit the administration client?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
          setVisible(false);
          dispose();
          auth.logout();
          System.exit(0);
        }
      }
    });

    final AboutDialog about = new AboutDialog(this);
    aboutItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        about.showAbout();
      }
    });

    menuBar.add(fileMenu);
    menuBar.add(helpMenu);
    menuBar.setAlignmentY(Component.LEFT_ALIGNMENT);
    return menuBar;
  }

  private void checkInactive() {
    ServerOperation so = new ServerOperation();
    while (auth.getServerState() == STATE_SERVER_STANDBY) {
      setStandby();
      try {
        showProgressDialog("<HTML>Server in standby<BR>Peer: "
            + so.getRemoteServerIP() + "</HTML>");
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
      }
      catch (Exception e) {
        log.error("Error: ", e);
      }
    }
    setActive();
  }

  private void doPostAuth() {
    if (auth.isAuthenticated()) {
      setTitle(APPLICATION_TITLE + auth.getServer());

      // Check if the server is in the fully started state:
      boolean serverRestarting = auth.isServerRestarting();
      String serverStateStr = auth.getServerStateString();
      if (serverRestarting) {
        showProgressDialog(serverStateStr);
        updateStatus("Waiting for server to restart...", true);
      }

      checkInactive();

      while (serverRestarting) {
        serverStateStr = auth.getServerStateString();
        showProgressDialog(serverStateStr);
        log.debug(serverStateStr);
        serverRestarting = auth.isServerRestarting();
        try {
          int timeout = 1000;
          if (auth.getServerState() == STATE_SERVER_STANDBY) {
            timeout = 15000; // Only check once every 15 seconds if server is in
                             // standby
            handleInactivateRequest();
          }
          Uninterruptibles.sleepUninterruptibly(timeout, TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
          log.error("Error: ", e);
        }
      }

      updateStatus("Server running.", true);
      hideProgressDialog();

      // Fill in controller info.
      ServerOperation so = new ServerOperation(
          ServerOperation.Operation.OP_GET_DRAC_INFO, this);
      Thread t = new Thread(so, SVR_OP_THREAD_NAME);
      t.setDaemon(true);
      t.start();

      serverMonitor = new ServerMonitor();
      serverMonitor.addHandler(this);
      serverThread = new Thread(serverMonitor, SVR_MON_THREAD_NAME);
      serverThread.setDaemon(true);
      serverThread.start();

      showProgressDialog("Loading user preferences...");
      // Retrieve the user's preferences
      loadUserPreferencesJDOM(auth.getUserName());

      hideProgressDialog();
      // Get current server view and display it
      getAndDisplayServerGraph();

      // // set user's timezone preference
      // setUserTimeZoneIDPreference();
    }
  }

  private void flash(JButton button, Color colour) {
    Thread t = new Thread(new StatusUpdater(button, colour), "StatusUpdater");
    t.setDaemon(true);
    t.start();
  }

  private JButton getPurgeButton(final String warningMessage,
      final boolean purgeEvents) {
    final JButton purgeButton = new JButton("Purge");
    purgeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        try {
          purgeButton.setEnabled(false);

          final DateDropDown dateConstraint = new DateDropDown(
              OpenDracDesktop.this.getTimeZonePreference(),
              OpenDracDesktop.this.getLocale());
          dateConstraint.setEditable(true);
          dateConstraint.setPreferredSize(new Dimension(110, 21));

          JPanel panel = new JPanel(new BorderLayout());
          panel.add(new JLabel(warningMessage, SwingConstants.LEFT),
              BorderLayout.NORTH);

          ButtonGroup bg = new ButtonGroup();
          JRadioButton ignoreButton = new JRadioButton(
              "<HTML>Ignore date</HTML>");
          ignoreButton.setActionCommand("ignoreButton");
          JRadioButton dateButton = new JRadioButton(
              "<HTML>Specify date:<sp></HTML>");
          dateButton.setActionCommand("dateButton");
          dateButton.setSelected(true);
          bg.add(ignoreButton);
          bg.add(dateButton);
          dateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
              dateConstraint.setComponentEnabled(true);
            }
          });
          ignoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
              dateConstraint.setComponentEnabled(false);
            }
          });

          JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
          innerPanel.add(ignoreButton);
          innerPanel.add(dateButton);
          innerPanel.add(dateConstraint);
          panel.add(innerPanel, BorderLayout.WEST);

          panel.add(new JLabel("<HTML><br><br>Do you wish to continue?</HTML>",
              SwingConstants.LEFT), BorderLayout.SOUTH);

          int option = JOptionPane.showConfirmDialog(OpenDracDesktop.this,
              panel, "Confirm", JOptionPane.YES_NO_OPTION);

          if (option == JOptionPane.YES_OPTION) {
            if ("dateButton".equals(bg.getSelection().getActionCommand())) {
              long etime = dateConstraint.getTimeInMillis();
              Calendar ecal = Calendar.getInstance();
              ecal.setTimeInMillis(etime);
              ecal.set(Calendar.AM_PM, Calendar.AM);
              ecal.set(Calendar.HOUR, 00);
              ecal.set(Calendar.MINUTE, 00);
              ecal.set(Calendar.SECOND, 00);
              ecal.getTime(); // Force recalculation of time
              purgeConstraint = ecal.getTimeInMillis();
            }
            else {
              purgeConstraint = 0;
            }

          }
          else {
            purgeButton.setEnabled(true);
            hideProgressDialog();
          }
        }
        catch (Exception e) {
          log.error("Error: ", e);
          purgeButton.setEnabled(true);
          hideProgressDialog();
        }
      }
    });

    return purgeButton;
  }


  private void handleGenerateManualLinkEvent(Map<String, String> eventMap) {
    String srcNeIEEE = null;
    String dstNeIEEE = null;
    String srcPort = null;
    String dstPort = null;

    if (!(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
        GEN_MANUAL_LINK_STR, "Create manual link", JOptionPane.YES_NO_OPTION))) {
      return;
    }

    log.debug("handleGenerateManualLinkEvent invoked with data " + eventMap);

    if (eventMap == null) {
      log.error("handleGenerateManualLinkEvent eventMap is null, cannot remove the link !");
      return;
    }

    srcNeIEEE = eventMap.get("SRCNEIEEE");
    dstNeIEEE = eventMap.get("DSTNEIEEE");
    srcPort = eventMap.get("SRCPORT");
    dstPort = eventMap.get("DSTPORT");

    if (srcNeIEEE == null || srcPort == null || dstNeIEEE == null
        || dstPort == null) {
      log.error("handleGenerateManualLinkEvent: one or more values is null! "
          + eventMap);
      return;
    }

    try {
      new ServerOperation().addAjacency(srcNeIEEE, srcPort, dstNeIEEE, dstPort);
    }
    catch (Exception e) {
      log.error("OpenDracDesktop::handleGenerateManualLinkEvent failed.", e);
      JOptionPane.showMessageDialog(this, "Exception generating manual link: "
          + e.getMessage());
    }
  }

  private void handleRemoveLinkEvent(Map<String, String> eventMap) {
    String srcNeID = null;
    String dstNeID = null;
    String srcPort = null;
    String dstPort = null;

    if (!(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
        DLT_MANUAL_ADJ_STR, "Remove manual adjacency",
        JOptionPane.YES_NO_OPTION))) {
      return;
    }

    log.debug("handleRemoveLinkEvent invoked with data " + eventMap);

    if (eventMap == null) {
      log.error("handleRemoveLinkEvent eventMap is null, cannot remove the link !");
      return;
    }

    srcNeID = eventMap.get("SRCNEID");
    dstNeID = eventMap.get("DSTNEID");
    srcPort = eventMap.get("SRCPORT");
    dstPort = eventMap.get("DSTPORT");

    if (srcNeID == null || srcPort == null || dstNeID == null
        || dstPort == null) {
      log.error("handleRemoveLinkEvent: one or more values is null! "
          + eventMap);
      return;
    }

    try {
      new ServerOperation().deleteAdjanceny(srcNeID, srcPort, dstNeID, dstPort);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      JOptionPane.showMessageDialog(this,
          "Exception removing link: " + e.getMessage());
    }
  }

  private void initialise() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 11));
      UIManager.put("TabbedPane.font", new Font("SansSerif", Font.PLAIN, 11));
      UIManager.put("Table.font", new Font("SansSerif", Font.PLAIN, 11));
      UIManager.put("Button.font", new Font("SansSerif", Font.PLAIN, 11));
      UIManager.put("TextField.font", new Font("SansSerif", Font.PLAIN, 11));
      setTitle(APPLICATION_TITLE);
      SwingUtilities.updateComponentTreeUI(this);

      auth.setStatus("Initialising components...");
      auth.doAuthentication();
      auth.closeWindow();

      // Do this before the tabs are built:
      // set user's timezone preference
      setUserTimeZoneIDPreference();

      buildAndShowFrame();
      doPostAuth();
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  private void loadUserPreferencesJDOM(String userId) {
    try {
      String r = getNRBHandle().loadUserPreferences(getLoginToken(), userId);
      if (r == null) {
        updateStatus("Warning: No user preferences for user: " + userId
            + " ;defaults will be used for visual layout.", true);
      }
      else {
        applyUserPreferences(XmlUtility.createDocumentRoot(r));
      }

    }
    catch (Exception e) {
      log.error("Error: ", e);
      updateStatus(
          "Warning: An error occurred retrieving user preferences for user: "
              + userId + " ;defaults will be used for visual layout.", true);
    }
  }

  /*
   * private String getLayer1Rate( String layer2Rate, String mode ) { if (
   * "sdh".equalsIgnoreCase( mode ) ) { return DesktopUtil.getRateInSDH(
   * layer2Rate ); } else { return DesktopUtil.getRateInSonet( layer2Rate ); } }
   */

  private void saveUserPreferencesJDOM() throws Exception {
    String userId = auth.getUserName();

    Element userElement = new Element(USER_ELEMENT);
    userElement.setAttribute(ID_ATTR, userId);

    Element preferencesList = new Element(PREFERENCESLIST_ELEMENT);

    preferencesList
        .addContent(NetworkGraph.INSTANCE.buildPreferencesXML());

    userElement.addContent(preferencesList); 

    Map<String, Object> map = new HashMap<String, Object>();
    map.put(DbKeys.AdminConsoleUserPreferencesCols.USERID, userId);
    map.put(DbKeys.AdminConsoleUserPreferencesCols.ELEMENT, userElement);

    Thread t = new Thread(new ServerOperation(
        ServerOperation.Operation.OP_SAVE_PREFERENCES, this, map),
        SVR_OP_THREAD_NAME + ": saveUserPreferences()");
    showProgressDialog("Saving preferences...");
    t.setDaemon(true);
    t.start();
  }

  private void setErrorStatusInactive(String statusMessage) {
    errorButton.setBackground(new Color(255, 204, 0)); // Amber/orange
    errorButton.setToolTipText(statusMessage);
  }

  private void setFileMenuStandby(boolean state) {
    if (fileMenu != null) {
      Component menuItems[] = fileMenu.getMenuComponents();
      Component item = null;
      for (int i = 0; i < menuItems.length - 2; i++) { // Disable everything
                                                       // except the switch
                                                       // servers and exit items
        item = menuItems[i];
        if (item instanceof JMenuItem) {
          item.setEnabled(!state);
        }
      }
    }
  }

  private void setStandby() {
    setErrorStatusInactive("Server in standby state");
    updateStatus("Server in standby state", true);
    tabbedPane.setEnabled(false);
    setFileMenuStandby(true);
    adminPanel.enableAddNetworkElementButton(false);
    NetworkGraph.INSTANCE.setEnabled(false);
  }

  private void setupMonitors() {
    serverMonitor = new ServerMonitor();
    serverMonitor.addHandler(this);
    serverThread = new Thread(serverMonitor, SVR_MON_THREAD_NAME + "-R");
    serverThread.setDaemon(true);
    serverThread.start();
  }

  private void setUserTimeZoneIDPreference() {
    try {
      timeZoneIDPreference = auth.getUserDetails().getUserPolicyProfile()
          .getUserProfile().getPreferences().getTimeZoneId();
    }
    catch (Exception e) {
      log.error(
          "User's timezone id preference couldn't be retrieved from the user profile. Assigning the default timezone id = "
              + timeZoneIDPreference, e);
    }
    timeZonePreference = TimeZone.getTimeZone(timeZoneIDPreference);
    log.debug("Admin Console user's timezone id preference = "
        + timeZoneIDPreference);
  }

  private void showReEstablishWindow() {
    Thread t = new Thread(new Reconnect(this, this), SVR_RCON_THREAD_NAME);
    t.setDaemon(true);
    t.start();
  }

}
