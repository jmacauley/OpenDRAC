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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.DesktopUtil;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPolicyProfile;
import com.nortel.appcore.app.drac.common.types.LpcpStatus;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;

/**
 * AuthenticationDialog Defines the login window for the DRAC Administration
 * Desktop. Also performs authentication with the DRAC server.
 * 
 * @author adlee
 * @since 2006-01-12
 */

public final class AuthenticationDialog extends JDialog implements
    MouseMotionListener {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private String dummySessionId = ""+(new Date()).getTime();
  
  public final class AuthThread implements Runnable {
    public final class WidgetUpdater implements Runnable {
      private final JProgressBar bar;
      private final JTextField textField;
      private final int progressValue;
      private final String status;

      public WidgetUpdater(JProgressBar progressBar, int progress,
          JTextField testField, String statusMsg) {
        bar = progressBar;
        textField = testField;
        progressValue = progress;
        this.status = statusMsg;
      }

      @Override
      public void run() {
        bar.setValue(progressValue);
        textField.setText(status);
      }
    }

    private final boolean showErrors;
    private final AuthenticationDialog parentDialog;

    public AuthThread(AuthenticationDialog dialog, boolean showError) {
      showErrors = showError;
      parentDialog = dialog;
    }

    public boolean newAuthenticate(boolean showError) {
      int progressState = 0;

      try {
        // Grab the auth server IP address from the socket
        String authServer = ((String) serverBox.getSelectedItem()).trim(); // authServerField.getText()
        String rmiURI = "//" + authServer + ":" + RmiServerInfo.RMI_REGISTRY_PORT
            + "/" + RmiServerInfo.NRB_RMI_NAME;

        northGlassPane.setOpaque(false);
        statusField.setOpaque(false);
        statusField.setForeground(Color.WHITE);
        statusField.setHorizontalAlignment(SwingConstants.CENTER);
        parentDialog.setGlassPane(mainGlassPane);
        parentDialog.getGlassPane().setVisible(true);

        updateProgress(jpb, progressState++, statusField,
            "Performing lookup...");

        
        nrb = (NrbInterface) Naming.lookup(rmiURI);
        log.info("Connected: " + getNrb());

        String ip = null;
        try {
          InetAddress address = InetAddress.getLocalHost();
          ip = address.getHostAddress();
        }
        catch (Exception e) {
          log.error("Error: ", e);
        }

        updateProgress(jpb, progressState++, statusField, "Authenticating...");

        int failedLoginAttempts = 0;

        token = null;
        try {
          token = getNrb().login(ClientLoginType.ADMIN_CONSOLE_LOGIN,
              userField.getText(), passField.getPassword(), ip, null, dummySessionId);
        }
        catch (RemoteException re) {
          log.error("login failed", re);

          if (re.getCause() instanceof NrbException) {

            NrbException n = (NrbException) re.getCause();

            if (n.getArgs() != null && n.getArgs().length > 0) {
              if (n.getArgs()[0] instanceof SessionErrorCode) {
                SessionErrorCode ec = (SessionErrorCode) n.getArgs()[0];

                switch (ec) {
                case ERROR_ACCOUNT_DISABLED:
                  statusField.setText("Error performing authentication");
                  if (showError) {
                    JOptionPane
                        .showMessageDialog(
                            parentDialog,
                            "<HTML>An authentication error occurred.  Account is disabled.</HTML>",
                            "Authentication Error", JOptionPane.ERROR_MESSAGE);
                  }
                  return false;
                case ERROR_DORMANT:
                  statusField.setText("Error performing authentication");
                  if (showError) {
                    JOptionPane
                        .showMessageDialog(
                            parentDialog,
                            "<HTML>An authentication error occurred.  Account is locked out due to dormancy.</HTML>",
                            "Authentication Error", JOptionPane.ERROR_MESSAGE);
                  }
                  return false;
                case ERROR_INVALID_USERID_OR_PASSWORD_AND_RETRY:
                  statusField.setText("Error performing authentication");
                  if (showError) {
                    JOptionPane
                        .showMessageDialog(
                            parentDialog,
                            "<HTML>The specified user id and password combination is invalid.</HTML>",
                            "Authentication Error", JOptionPane.ERROR_MESSAGE);
                  }
                  return false;
                    
                case ERROR_INVALID_USERID_OR_PASSWORD_AND_TO_BE_LOCKEDOUT:
                    statusField.setText("Error performing authentication");
                    if (showError) {
                      JOptionPane
                          .showMessageDialog(
                              parentDialog,
                              "<HTML>The specified user id and password combination is invalid.  Account will be locked out after too many invalid login attempts.</HTML>",
                              "Authentication Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return false;                    
                case ERROR_LOCKOUT:
                  statusField.setText("Error performing authentication");
                  if (showError) {
                    JOptionPane
                        .showMessageDialog(
                            parentDialog,
                            "<HTML>An authentication error occurred.  Account is temporarily locked out.</HTML>",
                            "Authentication Error", JOptionPane.ERROR_MESSAGE);
                  }
                  return false;

                case ERROR_PASSWORD_EXPIRED:
                  statusField.setText("Error performing authentication");
                  if (showError) {
                    JOptionPane
                        .showMessageDialog(
                            parentDialog,
                            "<HTML>An authentication error occurred.  User password has expired.</HTML>",
                            "Authentication Error", JOptionPane.ERROR_MESSAGE);
                  }
                  return false;

                case ERROR_SESSION_EXPIRED:
                case NO_ERROR:
                case ERROR_NO_POLICY:
                case ERROR_LOCKED_CLIENT_IP_ADDRESSS:
                case ERROR_ASELECT_COMMUNICATION_ERROR:
                case ERROR_GENERAL:
                case ERROR_INVALID_AUTHENTICATION_TYPE:
                case ERROR_LOCKED_CLIENT_SESSION:
                default:
                  // Authentication failed
                  statusField.setText("Authentication failed");
                  if (showError) {
                    JOptionPane.showMessageDialog(parentDialog,
                        "An authentication error occurred.",
                        "Authentication Error", JOptionPane.ERROR_MESSAGE);
                  }
                  return false;
                }
              }

              if (n.getArgs()[0] instanceof String) {
                statusField.setText("Error performing authentication");
                if (showError) {
                  JOptionPane.showMessageDialog(parentDialog,
                      "<HTML>An authentication error occurred.  "
                          + n.getArgs()[0] + "</HTML>", "Authentication Error",
                      JOptionPane.ERROR_MESSAGE);
                }
                return false;
              }
              log.error(
                  "Login threw a nexted NrbException but arg 1 wasnt a SessionErrorCode or String",
                  re);
            }
            else {
              log.error("Login threw a nested NrbException with no args", re);
            }
          }
          else {
            log.error(
                "Login threw an remoteException but not a nested NrbException",
                re);
          }
          /*
           * a more generic error occurred during login that we can't give a
           * specific message to, throw it again to be caught below
           */
          throw re;
        }
        userDetails = getNrb().getUserDetails(token);
        failedLoginAttempts = getInvalidLoginAttempts(userDetails
            .getUserPolicyProfile());

        if (failedLoginAttempts > 0) {
          if (showError) {
            JOptionPane
                .showMessageDialog(
                    parentDialog,
                    "<HTML>"
                        + failedLoginAttempts
                        + " failed login attempts since last login<br>Last failed login from: "
                        + getLastInvalidLoginLocation(userDetails
                            .getUserPolicyProfile()) + "</HTML>");
          }
        }

        statusField.setText("Authentication successful.");
        updateProgress(jpb, progressState++, statusField,
            "Connecting to controller...");
        statusField.setText("Connecting to controller...");
        setAuthenticated(true);
        updateProgress(jpb, progressState++, statusField,
            "Connecting to controller...");
        statusField.setText("Connection to controller successful.");
      }
      catch (UnknownHostException ukhe) {
        if (showError) {
          JOptionPane.showMessageDialog(
              parentDialog,
              "Exception resolving controller: "
                  + (String) serverBox.getSelectedItem() + "\n"
                  + ukhe.getMessage(), "Connect Error",
              JOptionPane.ERROR_MESSAGE);
        }
      }
      catch (NotBoundException nbe) {
        log.error(
            "Exception connecting." + (String) serverBox.getSelectedItem(), nbe);
        setAuthenticated(false);
        if (showError) {
          JOptionPane
              .showMessageDialog(
                  parentDialog,
                  "NotBoundException " + nbe.getMessage(),
                  "Connect Error. Cannot connect to server, not running or invalid hostname",
                  JOptionPane.ERROR_MESSAGE);
          statusField.setText("Connection to controller failed.");
        }
      }
      catch (Exception e) {
        log.error(
            "Exception connecting." + (String) serverBox.getSelectedItem(), e);
        setAuthenticated(false);
        if (showError) {
          JOptionPane.showMessageDialog(parentDialog, e.getMessage(),
              "Connect Error", JOptionPane.ERROR_MESSAGE);
          statusField.setText("Connection to controller failed.");
        }
      }

      return isAuthenticated();
    }

    @Override
    public void run() {
      if (newAuthenticate(showErrors)) {
        showLoginPanel(false);
        saveLastUserInfo();
      }
    }

    private void updateProgress(JProgressBar jpb, int progressState,
        JTextField statusField, String statusText) {
      WidgetUpdater wu = new WidgetUpdater(jpb, progressState, statusField,
          statusText);
      EventQueue.invokeLater(wu);
    }

  }

  private boolean authenticated;
  private static final long serialVersionUID = 1L;
  private int mouseStartDragX;
  private int mouseStartDragY;
  private static final int WINDOW_WIDTH = 425;
  private static final int WINDOW_HEIGHT = 370;
  private static final int IMG_HEIGHT = 242;
  private static final int IMG_WIDTH = 423;

  private final JComboBox serverBox = new JComboBox();
  private final JTextField userField = new JTextField();
  private final JPasswordField passField = new JPasswordField();
  private final JTextField statusField = new JTextField();

  private final JFrame parent;
  private static final String SVR_STAT_RUNNING = "Server Running";
  private static final String SVR_STAT_INACTIVE = "SERVER INACTIVE";

  private final String version;
  private Image imgSplash;
  private final JPanel mainGlassPane = new JPanel();
  private final JPanel northGlassPane = new JPanel();
  private final JProgressBar jpb = new JProgressBar(0, 4);

  private UserDetails userDetails;
  private NrbInterface nrb;
  private LoginToken token;

  private JButton okButton;

  public AuthenticationDialog(JFrame parentFrame, String dracVersion, String buildDate,
      String[] argv) {
    super(parentFrame, "OpenDRAC login");
    version = dracVersion;
    parent = parentFrame;
    setUndecorated(true);

    try {
      UIManager
          .setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      SwingUtilities.updateComponentTreeUI(this);
    }
    catch (Exception e) {
      log.error("Exception setting look and feel", e);
    }

    String hostname = null;
    String username = null;
    String password = null;
    boolean autoLogin = false;
    if (argv != null) {
      if (argv.length >= 1) {
        hostname = argv[0].trim();
      }
      if (argv.length >= 2) {
        username = argv[1].trim();
      }
      if (argv.length >= 3) {
        password = argv[2].trim();
        autoLogin = true;
      }
      if (argv.length >= 4) {
        autoLogin = Boolean.parseBoolean(argv[3].trim());
      }

    }
    buildDialog(hostname, username, password);

    if (autoLogin) {
      getOkButton().doClick();
    }
  }

  /**
   * Testing only
   * 
   * @param argv
   */
  public static void main(String argv[]) {
    JFrame frame = new JFrame("Authentication Test Frame");
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });
    AuthenticationDialog ad = new AuthenticationDialog(frame, "Version", "Build Date", argv);
    frame.setVisible(true);
    ad.doAuthentication();
  }

  public void closeWindow() {
    setVisible(false);
    dispose();
  }

  public void doAuthentication() {
    setVisible(true);
    toFront();
    requestFocus();
    if ("".equals(userField.getText().trim())) {
      userField.requestFocus();
    }
    else {
      passField.requestFocus();
    }

    while (!authenticated) {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  public void doLogin() {
    showLoginPanel(true);
    setStatus("");
    authenticated = false;
    doAuthentication();
  }

  public LoginToken getLoginToken() {
    return token;
  }

  public NrbInterface getNrb() {
    return nrb;
  }

  public JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton("Ok");
    }
    return okButton;
  }

  public String getServer() {
    return (String) serverBox.getSelectedItem();
  }

  // Administration function - must only be called after a connection is
  // initially made to the server
  public int getServerState() {
    try {
      return new ServerOperation().getLpcpStatus().getState();
    }
    catch (Exception e) {
      log.error("getServerState failed, returning state as -1", e);
    }
    return -1;

  }

  public String getServerStateString() {

    try {
      return new ServerOperation().getLpcpStatus().getStateAsString();
    }
    catch (Exception e) {
      log.error("getServerStateString failed, returning 'Unknown' ", e);
    }
    return "Unknown";

  }

  public UserDetails getUserDetails() {
    return userDetails;
  }

  public String getUserName() {
    return userField.getText();
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public boolean isControllerInactive() {
    return SVR_STAT_INACTIVE.equalsIgnoreCase(getServerStateString());
  }

  public boolean isControllerRunning() {
    return SVR_STAT_RUNNING.equalsIgnoreCase(getServerStateString());
  }

  // Administration function - must only be called after a connection is
  // initially made to the server
  public boolean isServerRestarting() {
    try {
      LpcpStatus status = new ServerOperation().getLpcpStatus();
      return status.isRestart();
    }
    catch (Exception e) {
      log.error(
          "getLpcpStatus failed, ignoring and assuming the server is *NOT* restarting",
          e);
    }
    return false;
  }

  public void logout() {
    try {
      if (nrb != null && token != null) {
        
        nrb.logout(token);
        
      }
    }
    catch (Exception e) {
      log.error("Server logout failed, best effort", e);
    }
  }

  // MouseMotionListener interface methods
  @Override
  public void mouseDragged(MouseEvent me) {
    Point p = getLocation();
    setLocation(p.x + me.getX() - mouseStartDragX, p.y + me.getY()
        - mouseStartDragY);
  }

  @Override
  public void mouseMoved(MouseEvent me) {
    return;
  }

  // Re-connect to the server after a dropped connection.
  public boolean reConnect() {
    AuthThread authThread = new AuthThread(this, false);
    return authThread.newAuthenticate(false);
  }

  public void setStatus(String status) {
    statusField.setText(status);
  }

  private void buildDialog(String overrideHostname, String overrideUsername,
      String overridePassword) {

    JPanel splashPanel = new JPanel();
    JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
    JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    JPanel mainButtonPanel = new JPanel(new BorderLayout(5, 5));
    JPanel loginPanel = new JPanel(new BorderLayout(5, 5));

    JButton cancelButton = new JButton("Cancel");
    JPanel credsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
    JPanel mainServerPanel = new JPanel(new GridLayout(1, 1, 5, 5));
    JLabel splashLabel = new JLabel();
    JLabel serverLabel = new JLabel("Controller:");
    JLabel userLabel = new JLabel("User  ID:");
    JLabel passLabel = new JLabel("Password:");
    final AuthenticationDialog thisDialog = this;

    serverBox.setEditable(true);

    userField.setPreferredSize(new Dimension(160, 21));
    passField.setPreferredSize(new Dimension(160, 21));
    serverLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    serverLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
    userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    userLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
    passLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    passLabel.setHorizontalTextPosition(SwingConstants.RIGHT);

    passField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent ke) {
        // Enter key was pressed
        if (KeyEvent.VK_ENTER == ke.getKeyCode()) {
          AuthThread authThread = new AuthThread(thisDialog, true);
          Thread thread = new Thread(authThread, "AuthThread");
          thread.setDaemon(true);
          thread.start();
        }
      }
    });

    statusField.setEditable(false);
    statusField.setMargin(new Insets(0, 0, 0, 0));
    statusField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    credsPanel.add(serverLabel);
    credsPanel.add(serverBox);
    credsPanel.add(userLabel);
    credsPanel.add(userField);
    credsPanel.add(passLabel);
    credsPanel.add(passField);

    mainServerPanel.setLayout(new BorderLayout(1, 1));

    mainServerPanel.add(credsPanel, BorderLayout.WEST);

    serverBox.setPreferredSize(new Dimension(100, 21));

    loginPanel.add(mainServerPanel, BorderLayout.NORTH);
    JPanel login = new JPanel(new BorderLayout(0, 5));

    mainButtonPanel.add(buttonPanel, BorderLayout.NORTH);

    JPanel loginBorderPanel = new JPanel(new GridLayout(1, 1, 1, 5));

    login.add(loginPanel, BorderLayout.WEST);
    login.add(mainButtonPanel, BorderLayout.EAST);
    loginBorderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    loginBorderPanel.add(login);

    splashPanel.setLayout(new BorderLayout(1, 1));

    buttonPanel.add(getOkButton());
    buttonPanel.add(cancelButton);
    buttonPanel.add(new JLabel());

    imgSplash = createVersionedSplash();

    splashLabel.setIcon(new ImageIcon(imgSplash));
    splashPanel.add(splashLabel, BorderLayout.CENTER);
    Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((screendim.width - 425) / 2, (screendim.height - 300) / 2);

    loginBorderPanel.setMinimumSize(new Dimension(0, 0));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setTopComponent(splashPanel);
    splitPane.setBottomComponent(loginBorderPanel);

    splitPane.setDividerSize(0);

    mainPanel.add(splitPane, BorderLayout.CENTER);

    JPanel mainBorderPanel = new JPanel(new GridLayout(1, 1, 5, 5));
    mainBorderPanel.add(mainPanel);

    mainPanel.setBorder(BorderFactory.createRaisedBevelBorder());
    getContentPane().setLayout(new GridLayout(1, 1, 5, 5));
    getContentPane().add(mainBorderPanel); // mainPanel );
    addMouseMotionListener(this);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent me) {
        mouseStartDragX = me.getX();
        mouseStartDragY = me.getY();
      }
    });
    getOkButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        AuthThread authThread = new AuthThread(thisDialog, true);
        Thread thread = new Thread(authThread);
        thread.setDaemon(true);
        thread.start();
      }
    });

    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        closeWindow();
        parent.dispose();
        log.debug("AdminConsole exiting!");
        System.exit(0);
      }
    });

    northGlassPane.setLayout(new BorderLayout(1, 1));
    JPanel fillerPanel = new JPanel();
    fillerPanel.setPreferredSize(new Dimension(423, 220));
    fillerPanel.setOpaque(false);

    northGlassPane.add(fillerPanel, BorderLayout.NORTH);
    northGlassPane.add(statusField, BorderLayout.SOUTH);
    mainGlassPane.setOpaque(false);
    mainGlassPane.setLayout(new BorderLayout(1, 1));
    mainGlassPane.add(northGlassPane, BorderLayout.NORTH);

    pack();
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

    loadLastUserInfo();
    if ("".equals(userField.getText().trim())) {
      userField.requestFocus();
    }
    else {
      passField.requestFocus();
    }

    if (overrideHostname != null) {
      serverBox.addItem(overrideHostname);
      userField.requestFocus();
    }

    if (overrideUsername != null) {
      userField.setText(overrideUsername);
      passField.requestFocus();
    }

    if (overridePassword != null) {
      passField.setText(overridePassword);
      getOkButton().requestFocus();
    }
  }

  private Image createVersionedSplash() {
    Image dracSplash = new DesktopUtil()
        .loadImageFromJar("/client/Images/OpenDRACsplash.gif");
    dracSplash = new ImageIcon(dracSplash.getScaledInstance(175, -1,
        Image.SCALE_SMOOTH)).getImage();
    BufferedImage bi = new BufferedImage(IMG_WIDTH, IMG_HEIGHT,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D biGraphics = bi.createGraphics();
    biGraphics.setColor(new Color(141, 101, 210));
    biGraphics.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);
    biGraphics.setColor(Color.WHITE);
    biGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    biGraphics.drawString("Open Dynamic Resource Allocation Controller", 10,
        175);
    biGraphics.drawString("Version: " + version, 10, 200);
    
    biGraphics.drawImage(dracSplash,
        IMG_WIDTH - dracSplash.getWidth(this) - 25, 0, this);
    return bi;
  }

  private int getInvalidLoginAttempts(UserPolicyProfile userPolicyProfile) {
    int invalidLogins = 0;
    try {
      invalidLogins = userPolicyProfile.getUserProfile()
          .getAuthenticationData().getAuditData().getNumberOfInvalidAttempts();
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
    return invalidLogins;
  }

  private String getLastInvalidLoginLocation(UserPolicyProfile userPolicyProfile) {
    String lastInvalidLoginLocation = "";

    try {
      Object locations[] = userPolicyProfile.getUserProfile()
          .getAuthenticationData().getAuditData()
          .getLocationOfInvalidAttempts().toArray();
      if (locations.length > 0) {
        lastInvalidLoginLocation = ((IPAddress) locations[locations.length - 1])
            .getAddress();
      }
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
    return lastInvalidLoginLocation;
  }

  private void loadLastUserInfo() {

    String lastUserInfo = null;
    byte[] bytes = new byte[1024];
    StringTokenizer st = null;
    String controller = null;
    String user = null;

    try {
      File f = new File("lastuser.inf");
      if (f.exists()) {
        GZIPInputStream gis = new GZIPInputStream(new FileInputStream(f));
        gis.read(bytes, 0, 1024);
        lastUserInfo = new String(bytes);
        gis.close();
        
        st = new StringTokenizer(lastUserInfo, "\t");
        if (st.hasMoreTokens()) {
          controller = st.nextToken();
          if (st.hasMoreTokens()) {
            user = st.nextToken();
          }
        }
        if (controller != null) {
          serverBox.addItem(controller);
        }
        if (user != null) {
          userField.setText(user);
        }
      }
    }
    catch (Exception e) {
      log.error("Exception reading last user info: ", e);
    }

  }

  private void saveLastUserInfo() {

    String lastUserInfo = null;
    try {
      File f = new File("lastuser.inf");
      GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(f));
      lastUserInfo = serverBox.getSelectedItem() + "\t" + userField.getText()
          + "\t" + serverBox.getSelectedItem() + "\t";
      gos.write(lastUserInfo.getBytes(), 0, lastUserInfo.length());
      gos.flush();
      gos.close();
    }
    catch (Exception e) {
      log.error("Exception saving last user info: ", e);
    }

  }

  private void setAuthenticated(boolean authenticated) {
    this.authenticated = authenticated;
  }

  private void showLoginPanel(boolean value) {
    if (value) {
      setVisible(value);
      setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    else {
      setSize(IMG_WIDTH, IMG_HEIGHT);
    }
  }
}
