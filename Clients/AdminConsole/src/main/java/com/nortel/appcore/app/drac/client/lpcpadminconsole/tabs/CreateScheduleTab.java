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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.NeCache;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.OpenDracDesktop;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.PopulateCreateSchedWidgets;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.Tna;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.dialogs.AuthenticationDialog;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.topology.NetworkGraph;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.DesktopUtil;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperationCallback;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.AidCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.ChannelSelector;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.DateDropDown;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TabCloseIcon;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TidCellRenderer;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.TimeDropDown;
import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType.RecurrenceFreq;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.common.utility.GenericJdomParser;

/**
 * @author pitman
 */
public final class CreateScheduleTab implements ActionListener,
    ServerOperationCallback {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	private DesktopUtil desktopUtil = new DesktopUtil();
	private Date newEndDate = new Date(System.currentTimeMillis());
	private Date newStartDate = new Date(System.currentTimeMillis());
	private JDialog pathDialog;
	private JTable pathTable;
	private final TimeZone timeZonePreference;
	private final Map<Layer, List<Tna>> srcTNAMap = new HashMap<Layer, List<Tna>>();
	private final Map<Layer, List<Tna>> dstTNAMap = new HashMap<Layer, List<Tna>>();
	private JLabel srcChannelLabel = new JLabel("Channel:");
	private JLabel dstChannelLabel = new JLabel("Channel:");
	private final JLabel srcVlanLabel = new JLabel(VID);
	private final JTextField srcVlanField = new JTextField();
	private final JLabel dstVlanLabel = new JLabel(VID);
	private final JTextField dstVlanField = new JTextField();
	private JLabel srcWavelengthLabel = new JLabel("Wavelength:");
	private final JTextField srcWavelengthField = new JTextField();
	private JLabel dstWavelengthLabel = new JLabel("Wavelength:");
	private final JTextField dstWavelengthField = new JTextField();
	private final JComboBox srcSiteBox = new JComboBox();
	private final JComboBox dstSiteBox = new JComboBox();
	private TimeDropDown startTime;
	private DateDropDown startCal;
	private DateDropDown endCal;
	private TimeDropDown endTime;
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

	private static final String RESOURCE_GROUP_STR = "Resource group";
	private static CreateScheduleTab instance;
	private JDialog spfDialog;
	private final OpenDracDesktop desktop;
	private final JFrame parent;
	private static final String VID = "VlanID";
	private static final String AUTO_PRERESERVATION = "Automatic pre-reservation";
	private static final String MANUAL_PRERESERVATION = "Manual pre-reservation";
	private static final String AUTO_RESERVATION = "Automatic reservation";
	private static final String MANUAL_RESERVATION = "Manual reservation";

	private JButton hideButton;
	private JButton showButton;
	private List<Element> currentShortPath;
	private List<Element> edgesProtecting;
	private String currentSrc;
	private String currentDst;
	private String currentSrcTna;
	private String currentDstTna;

	private JLabel vcatRoutingOptionLabel = new JLabel("VCAT");
	private JCheckBox vcatRoutingOption = new JCheckBox();

	private final AuthenticationDialog auth;

	private CreateScheduleTab(OpenDracDesktop top) {
		/*
		 * desktop and parent are the same thing but stored twice so we can track
		 * what is a reference to OpenDracDesktop per say and what is a swing reference
		 * to the parent window.
		 */
		desktop = top;
		parent = desktop;
		auth = OpenDracDesktop.getAuth();

		timeZonePreference = desktop.getTimeZonePreference();
	}

	public static synchronized CreateScheduleTab getInstance(OpenDracDesktop top) {
		if (instance == null) {
			instance = new CreateScheduleTab(top);
		}

		return instance;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == hideButton) {
				if (currentShortPath != null) {
					// remove all edges/vertex from the graph that are not in this path
					NetworkGraph.INSTANCE.hideNonPath(currentShortPath,
					    edgesProtecting);
					// High light them (again)
					NetworkGraph.INSTANCE.highlightPathAndEndPoints(
					    currentShortPath, currentSrc, currentDst, currentSrcTna,
					    currentDstTna);

					if (edgesProtecting != null) {
						NetworkGraph.INSTANCE.highlightPathDotted(edgesProtecting,
						    Color.red);
					}
				}
			}
			else if (e.getSource() == showButton) {
				// getTopologyPanel().showAllNEs();
				NetworkGraph.INSTANCE.resetGraph();
				// High light them (again)
				NetworkGraph.INSTANCE.highlightPathAndEndPoints(currentShortPath,
				    currentSrc, currentDst, currentSrcTna, currentDstTna);

				if (edgesProtecting != null) {
					NetworkGraph.INSTANCE.highlightPathDotted(edgesProtecting,
					    Color.red);
				}
			}
		}
		catch (Exception e1) {
			log.error("Error: ", e1);
		}
	}

	@Override
	public void handleServerOperationResult(ServerOperation op) {
		try {
			Map<String, Object> result = op.getResult();
			if (op.getOperation() == ServerOperation.Operation.OP_CREATE_SCHEDULE) {
				desktop.hideProgressDialog();
				String xmlResult = (String) result.get(ServerOperation.MAP_RESULT_KEY);
				GenericJdomParser jparser = new GenericJdomParser();
				jparser.parse(xmlResult);
				Element root = jparser.getRoot();
				if (root != null) {
					if (ServerOperation.STATUS_SUCCESS.equals(root
					    .getAttributeValue(ServerOperation.STATUS_ATTR))) {

						JOptionPane.showMessageDialog(desktop,
						    "Schedule successfully created.", "Result",
						    JOptionPane.INFORMATION_MESSAGE);
						if (desktop.schedTabPane.getTabCount() > 1) {
							desktop.schedTabPane.removeTabAt(1);
						}
					}
					else {
						String exceptionMsg = root.getChild(ServerOperation.EXCEPTION_ATTR)
						    .getAttributeValue("message");

						JOptionPane.showMessageDialog(desktop,
						    "Failed to create schedule: " + exceptionMsg, "Error",
						    JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
	}

	public void populateBoxForLayer(Layer layer, JComboBox box,
	    Map<Layer, List<Tna>> tnaMap)

	{
		List<Tna> list = null;
		box.removeAllItems();

		list = tnaMap.get(layer);

		
		if (list != null) {
			Iterator<Tna> it = list.iterator();
			Tna tna = null;
			
			while (it.hasNext()) {
				tna = it.next();
				
				box.addItem(tna);
			}
		}
		else {
			
		}
	}

	public void populateDstSiteBox(Map<Layer, List<Tna>> dstMap) {
		populateSiteBox(dstMap, dstSiteBox);
	}

	public void populateRateBox(JComboBox box, int layer, String mode) {
		
		box.removeAllItems();
		if (layer == 0 || layer == 1) {
			if ("SONET".equals(mode) || "N/A".equals(mode)) {
				box.addItem("STS1");
				box.addItem("STS3C");
				box.addItem("STS12C");
				box.addItem("STS24C");
				box.addItem("STS48C");
				box.addItem("STS192C");
			}
			else {
				box.addItem("VC3"); // This rate is not supported on HDX in SDH mode
				box.addItem("VC4");
				box.addItem("VC4-4C");
				box.addItem("VC4-8C");
				box.addItem("VC4-16C");
				box.addItem("VC4-64C");
			}
		}
		else if (layer == 2) {
			for (int i = 1; i <= 10000; i++) {
				box.addItem("" + i);
			}
		}
		else {
			
		}
	}

	public void populateSrcSiteBox(Map<Layer, List<Tna>> srcMap) {
		populateSiteBox(srcMap, srcSiteBox);
	}

	public void setPathDialog(boolean vis) {
		if (pathDialog != null) {
			pathDialog.setVisible(vis);
		}
	}

	public void showCreateScheduleDialog(final String srcIeee,
	    final String dstIeee, final String srcLabel, final String dstLabel,
	    final String srcMode, final String dstMode) {
		final Locale locale = parent.getLocale();

		JPanel gridPanel = new JPanel(new BorderLayout(1, 1));
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		JPanel detailsSouthPanel = new JPanel(new BorderLayout(5, 5));
		JLabel srcTNALabel = new JLabel("TNA:");
		JLabel dstTNALabel = new JLabel("TNA:");
		JLabel srcLayerLabel = new JLabel("Layer:");
		JLabel dstLayerLabel = new JLabel("Layer:");
		JLabel startTimeLabel = new JLabel("Start time:");
		JLabel endTimeLabel = new JLabel("End time:");
		JLabel srcRateLabel = new JLabel("Rate:");
		JLabel dstRateLabel = new JLabel("Rate:");
		JLabel srcSiteLabel = new JLabel("Site:");
		JLabel dstSiteLabel = new JLabel("Site:");
		JLabel groupLabel = new JLabel("Group:");
		JLabel resourceGroupLabel = new JLabel(RESOURCE_GROUP_STR);
		JPanel mainButtonPanel = new JPanel(new BorderLayout(1, 1));
		JPanel buttonPanel = new JPanel();
		JButton createButton = new JButton("Schedule");
		final JButton queryButton = new JButton("Query");
		JButton cancelButton = new JButton("Cancel");
		final JComboBox srcTNABox = new JComboBox();
		final JComboBox dstTNABox = new JComboBox();
		final JComboBox srcLayerBox = new JComboBox();
		final JComboBox dstLayerBox = new JComboBox();
		final JComboBox groupBox = new JComboBox();
		final JComboBox resourceGroupBox = new JComboBox();
		final ChannelSelector srcChanBox = new ChannelSelector("OC192", null);
		final ChannelSelector dstChanBox = new ChannelSelector("OC192", null);

		JTextField srcField = new JTextField();
		JTextField dstField = new JTextField();
		final JTextField startTimeField = new JTextField();
		final JTextField endTimeField = new JTextField();
		final JTextField metricsField = new JTextField();
		final JTextField srlgField = new JTextField();
		final JComboBox srcRateBox = new JComboBox();
		final JComboBox dstRateBox = new JComboBox();
		final JComboBox metricsBox = new JComboBox();
		final JComboBox protectionBox = new JComboBox();
		final JComboBox scheduleTypeBox = new JComboBox();

		populateSchedTypeBox(scheduleTypeBox);

		srcChanBox.addMouseListenerToButton(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				Tna tna = (Tna) srcTNABox.getSelectedItem();
				BigInteger constraints = fetchConstraints(tna.getNEID(), tna.getAID());
				srcChanBox.setRate(tna.getType());
				srcChanBox.setConstraints(constraints);
			}
		});

		dstChanBox.addMouseListenerToButton(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				Tna tna = (Tna) dstTNABox.getSelectedItem();
				BigInteger constraints = fetchConstraints(tna.getNEID(), tna.getAID());
				dstChanBox.setRate(tna.getType());
				dstChanBox.setConstraints(constraints);
			}
		});

		groupBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				// Re-load the resource group box to match the user group
				populateResourceGroupBox(resourceGroupBox, new UserGroupName(
				    (String) ((JComboBox) ie.getSource()).getSelectedItem()));
			}
		});

		Tna tna = (Tna) srcTNABox.getSelectedItem();
		BigInteger constraints = null;
		if (tna != null) {
			constraints = fetchConstraints(tna.getNEID(), tna.getAID());
			srcChanBox.setRate(tna.getType());
			srcChanBox.setConstraints(constraints);
		}

		tna = (Tna) dstTNABox.getSelectedItem();
		if (tna != null) {
			constraints = fetchConstraints(tna.getNEID(), tna.getAID());
			dstChanBox.setRate(tna.getType());
			dstChanBox.setConstraints(constraints);
		}

		dstRateBox.setEnabled(false);
		srcChanBox.setEnabled(false);
		dstChanBox.setEnabled(false);

		startTime = new TimeDropDown(5, desktop.getTimeZonePreference(),
		    desktop.getLocale());
		endTime = new TimeDropDown(5, desktop.getTimeZonePreference(),
		    desktop.getLocale());

		startCal = new DateDropDown(desktop.getTimeZonePreference(),
		    desktop.getLocale());
		endCal = new DateDropDown(desktop.getTimeZonePreference(),
		    desktop.getLocale());

		startCal.setEditable(true);
		endCal.setEditable(true);

		spfDialog = new JDialog(parent);

		buttonPanel.add(queryButton);
		buttonPanel.add(createButton);

		srcRateBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					if (srcLayerBox.getSelectedItem() == dstLayerBox.getSelectedItem()) {
						dstRateBox.setSelectedItem(srcRateBox.getSelectedItem());
					}
					else {
						if (Layer.LAYER1.equals(srcLayerBox.getSelectedItem())
						    && Layer.LAYER2.equals(dstLayerBox.getSelectedItem())) {
							dstRateBox.setSelectedItem(getLayer2Rate((String) srcRateBox
							    .getSelectedItem()));
						}
					}
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
			}
		});

		srcLayerBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					Layer srcLayer = (Layer) srcLayerBox.getSelectedItem();
					Layer dstLayer = (Layer) dstLayerBox.getSelectedItem();

					populateBoxForLayer(srcLayer, srcTNABox, srcTNAMap);

					populateRateBox(srcRateBox, srcLayerBox.getSelectedIndex(), srcMode);
					dstRateBox.setEnabled(dstLayerBox.getSelectedIndex() != srcLayerBox
					    .getSelectedIndex());

					srcChannelLabel.setVisible(Layer.LAYER1.equals(srcLayer));
					srcChanBox.setEnabled(Layer.LAYER1.equals(srcLayer));
					srcChanBox.setVisible(Layer.LAYER1.equals(srcLayer));

					srcVlanLabel.setVisible(Layer.LAYER2.equals(srcLayer));
					srcVlanField.setEnabled(Layer.LAYER2.equals(srcLayer));
					srcVlanField.setVisible(Layer.LAYER2.equals(srcLayer));

					srcWavelengthLabel.setVisible(Layer.LAYER0.equals(srcLayer));
					srcWavelengthField.setEnabled(Layer.LAYER0.equals(srcLayer));
					srcWavelengthField.setVisible(Layer.LAYER0.equals(srcLayer));

					if (Layer.LAYER2.equals(srcLayer) || Layer.LAYER2.equals(dstLayer)) {
						vcatRoutingOptionLabel.setVisible(true);
						vcatRoutingOption.setVisible(true);
					}
					else {
						vcatRoutingOptionLabel.setVisible(false);
						vcatRoutingOption.setVisible(false);
						vcatRoutingOption.setSelected(false);
					}

					Map<String, String> filter = new HashMap<String, String>();
					filter.put(OpenDracDesktop.SITE_ATTR,
					    (String) srcSiteBox.getSelectedItem());
					filterTNABox(srcTNAMap, srcTNABox, filter);
					if (srcTNABox.getSelectedItem() != null) {
						String wlength = ((Tna) srcTNABox.getSelectedItem())
						    .getWavelength();
						if (wlength == null) {
							wlength = "";
						}
						srcWavelengthField.setText(wlength);
					}
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
			}
		});

		dstLayerBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					Layer srcLayer = (Layer) srcLayerBox.getSelectedItem();
					Layer dstLayer = (Layer) dstLayerBox.getSelectedItem();

					populateBoxForLayer(dstLayer, dstTNABox, dstTNAMap);
					populateRateBox(dstRateBox, dstLayerBox.getSelectedIndex(), dstMode);
					dstRateBox.setEnabled(dstLayerBox.getSelectedIndex() != srcLayerBox
					    .getSelectedIndex());

					dstChannelLabel.setVisible(Layer.LAYER1.equals(dstLayer));
					dstChanBox.setEnabled(Layer.LAYER1.equals(dstLayer));
					dstChanBox.setVisible(Layer.LAYER1.equals(dstLayer));

					dstVlanLabel.setVisible(Layer.LAYER2.equals(dstLayer));
					dstVlanField.setEnabled(Layer.LAYER2.equals(dstLayer));
					dstVlanField.setVisible(Layer.LAYER2.equals(dstLayer));

					dstWavelengthLabel.setVisible(Layer.LAYER0.equals(dstLayer));
					dstWavelengthField.setEnabled(Layer.LAYER0.equals(dstLayer));
					dstWavelengthField.setVisible(Layer.LAYER0.equals(dstLayer));
					
					vcatRoutingOption.setSelected(true);
					if (Layer.LAYER2.equals(srcLayer) || Layer.LAYER2.equals(dstLayer)) {
						vcatRoutingOptionLabel.setVisible(true);
						vcatRoutingOption.setVisible(true);
					}
					else {
						vcatRoutingOptionLabel.setVisible(false);
						vcatRoutingOption.setVisible(false);
					}

					Map<String, String> filter = new HashMap<String, String>();
					filter.put(OpenDracDesktop.SITE_ATTR,
					    (String) dstSiteBox.getSelectedItem());
					filterTNABox(dstTNAMap, dstTNABox, filter);
					if (dstTNABox.getSelectedItem() != null) {
						String wlength = ((Tna) dstTNABox.getSelectedItem())
						    .getWavelength();
						if (wlength == null) {
							wlength = "";
						}
						dstWavelengthField.setText(wlength);
					}
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
			}
		});

		queryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					String srcChannel = null;
					String dstChannel = null;
					Layer srcLayer = (Layer) srcLayerBox.getSelectedItem();
					Layer dstLayer = (Layer) dstLayerBox.getSelectedItem();

					NetworkGraph.INSTANCE.resetGraph();

					if (pathDialog != null && pathDialog.isVisible()) {
						pathDialog.setVisible(false);
						pathDialog.dispose();
					}

					queryButton.setEnabled(false);
					if (srcTNABox.getSelectedItem() != null
					    && dstTNABox.getSelectedItem() != null) {
						if (!"".equalsIgnoreCase((String) srcRateBox.getSelectedItem())) {
							log.debug("Calling findSPFInternal: srcTNABox.getSelectedItem(): "
							    + srcTNABox.getSelectedItem());
							log.debug("Calling findSPFInternal: srcTNABox.getSelectedItem().getTNA(): "
							    + ((Tna) srcTNABox.getSelectedItem()).getTNA());
							log.debug("Calling findSPFInternal: dstTNABox.getSelectedItem(): "
							    + dstTNABox.getSelectedItem());
							log.debug("Calling findSPFInternal: dstTNABox.getSelectedItem().getTNA(): "
							    + ((Tna) dstTNABox.getSelectedItem()).getTNA());
							log.debug("Calling findSPFInternal: srcRateBox.getSelectedItem(): "
							    + srcRateBox.getSelectedItem());
							
							
							log.debug("Calling findSPFInternal: metricsBox: "
							    + metricsBox.getSelectedItem() + " metricsField: "
							    + metricsField.getText());
							String metricKey = null;
							if (metricsBox.getSelectedIndex() == 0) {
								metricKey = "COST";
							}
							else if (metricsBox.getSelectedIndex() == 1) {
								metricKey = "METRIC2";
							}
							else if (metricsBox.getSelectedIndex() == 2) {
								metricKey = "HOP";
							}
							if (srcLayerBox.getSelectedIndex() == 1) {
								log.debug("Calling findSPFInternal: srcChannel: "
								    + srcChanBox.getSelectedItem());
								srcChannel = (String) srcChanBox.getSelectedItem();
							}
							if (dstLayerBox.getSelectedIndex() == 1) {
								log.debug("Calling findSPFInternal: dstChannel: "
								    + dstChanBox.getSelectedItem());
								dstChannel = (String) dstChanBox.getSelectedItem();
							}

							Date startDate = DesktopUtil.toDate(locale, timeZonePreference,
							    startCal.getDate(), startTime.getTime());
							Date endDate = DesktopUtil.toDate(locale, timeZonePreference,
							    endCal.getDate(), endTime.getTime());

							if (startDate.after(endDate)) {
								JOptionPane.showMessageDialog(null, "Start time is invalid");
								return;
							}

							findSPFInternal((Tna) srcTNABox.getSelectedItem(),
							    (Tna) dstTNABox.getSelectedItem(),
							    (String) srcRateBox.getSelectedItem(),
							    Long.toString(startDate.getTime()),
							    Long.toString(endDate.getTime()), metricKey + "="
							        + metricsField.getText(), srlgField.getText(),
							    vcatRoutingOption.isSelected(), srcChannel, dstChannel,
							    srcLayer, dstLayer, (String) groupBox.getSelectedItem(),
							    getProtection((String) protectionBox.getSelectedItem()),
							    (String) resourceGroupBox.getSelectedItem(),
							    (String) scheduleTypeBox.getSelectedItem(), true, srcIeee,
							    dstIeee, srcVlanField.getText(), dstVlanField.getText());

						}
						else {
							JOptionPane
							    .showMessageDialog(null,
							        "The source TNA, destination TNA or rate is invalid or not set");
						}
					}
					else {
						JOptionPane.showMessageDialog(null,
						    "The source or destination TNA is invalid or not set");
					}
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
				finally {
					queryButton.setEnabled(true);
				}
			}
		});

		createButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				newStartDate = DesktopUtil.toDate(locale, timeZonePreference,
				    startCal.getDate(), startTime.getTime());
				newEndDate = DesktopUtil.toDate(locale, timeZonePreference,
				    endCal.getDate(), endTime.getTime());
				String srcChannel = null;
				String dstChannel = null;
				Layer srcLayer = (Layer) srcLayerBox.getSelectedItem();
				Layer dstLayer = (Layer) dstLayerBox.getSelectedItem();

				if (pathDialog != null && pathDialog.isVisible()) {
					pathDialog.setVisible(false);
					pathDialog.dispose();
				}

				if (srcTNABox.getSelectedItem() != null
				    && dstTNABox.getSelectedItem() != null) {
					log.debug("srcTNABox.getSelectedItem(): "
					    + srcTNABox.getSelectedItem());
					log.debug("Calling findSPFInternal: srcTNABox.getSelectedItem().getTNA(): "
					    + ((Tna) srcTNABox.getSelectedItem()).getTNA());
					log.debug("dstTNABox.getSelectedItem(): "
					    + dstTNABox.getSelectedItem());
					log.debug("Calling findSPFInternal: dstTNABox.getSelectedItem().getTNA(): "
					    + ((Tna) dstTNABox.getSelectedItem()).getTNA());
					log.debug("srcRateBox.getSelectedItem(): "
					    + srcRateBox.getSelectedItem());
					log.debug("Calling findSPFInternal: metricsBox: "
					    + metricsBox.getSelectedItem() + " metricsField: "
					    + metricsField.getText());
					if (srcLayerBox.getSelectedIndex() == 1) {
						log.debug("Calling findSPFInternal: srcChannel: "
						    + srcChanBox.getSelectedItem());
						srcChannel = (String) srcChanBox.getSelectedItem();
					}
					if (dstLayerBox.getSelectedIndex() == 1) {
						log.debug("Calling findSPFInternal: dstChannel: "
						    + dstChanBox.getSelectedItem());
						dstChannel = (String) dstChanBox.getSelectedItem();
					}
					if (!"".equalsIgnoreCase(((Tna) srcTNABox.getSelectedItem())
					    .toString())
					    && !"".equalsIgnoreCase(((Tna) dstTNABox.getSelectedItem())
					        .toString())
					    && !"".equalsIgnoreCase((String) srcRateBox.getSelectedItem())) {

						Date startDate = DesktopUtil.toDate(locale, timeZonePreference,
						    startCal.getDate(), startTime.getTime());
						Date endDate = DesktopUtil.toDate(locale, timeZonePreference,
						    endCal.getDate(), endTime.getTime());

						findSPFInternal((Tna) srcTNABox.getSelectedItem(),
						    (Tna) dstTNABox.getSelectedItem(),
						    (String) srcRateBox.getSelectedItem(),
						    Long.toString(startDate.getTime()),
						    Long.toString(endDate.getTime()), metricsBox.getSelectedItem()
						        + "=" + metricsField.getText(), srlgField.getText(),
						    vcatRoutingOption.isSelected(), srcChannel, dstChannel,
						    srcLayer, dstLayer, (String) groupBox.getSelectedItem(),
						    getProtection((String) protectionBox.getSelectedItem()),
						    (String) resourceGroupBox.getSelectedItem(),
						    (String) scheduleTypeBox.getSelectedItem(), false, srcIeee,
						    dstIeee, srcVlanField.getText(), dstVlanField.getText());

					}
					else {
						JOptionPane
						    .showMessageDialog(null,
						        "The source TNA, destination TNA or rate is invalid or not set");
					}
				}
				else {
					JOptionPane.showMessageDialog(null,
					    "The source or destination TNA is invalid or not set");
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				spfDialog.setVisible(false);

			}
		});

		srcChanBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				
			}
		});

		dstChanBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				
			}
		});

		srcWavelengthField.setEditable(false);
		dstWavelengthField.setEditable(false);
		srcVlanField.setEnabled(false);
		dstVlanField.setEnabled(false);

		srcSiteBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				String selectedSite = (String) srcSiteBox.getSelectedItem();
				Map<String, String> filter = new HashMap<String, String>();
				filter.put(OpenDracDesktop.SITE_ATTR, selectedSite);
				filterTNABox(srcTNAMap, srcTNABox, filter);
				if (srcTNABox.getItemCount() == 0) {
					srcWavelengthField.setText("");
				}
				if (dstTNABox.getItemCount() == 0) {
					dstWavelengthField.setText("");
				}
			}
		});

		dstSiteBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				String selectedSite = (String) dstSiteBox.getSelectedItem();
				Map<String, String> filter = new HashMap<String, String>();
				filter.put(OpenDracDesktop.SITE_ATTR, selectedSite);
				filterTNABox(dstTNAMap, dstTNABox, filter);
				if (srcTNABox.getItemCount() == 0) {
					srcWavelengthField.setText("");
				}
				if (dstTNABox.getItemCount() == 0) {
					dstWavelengthField.setText("");
				}
			}
		});

		srcTNABox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				if (srcTNABox.getSelectedItem() != null) {
					if (((Tna) srcTNABox.getSelectedItem()).getWavelength() != null) {
						srcWavelengthField.setText(((Tna) srcTNABox.getSelectedItem())
						    .getWavelength());
					}
				}
			}
		});

		dstTNABox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				if (dstTNABox.getSelectedItem() != null) {
					if (((Tna) dstTNABox.getSelectedItem()).getWavelength() != null) {
						dstWavelengthField.setText(((Tna) dstTNABox.getSelectedItem())
						    .getWavelength());
					}
				}
			}
		});

		startTime.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				if (ie.getStateChange() == ItemEvent.SELECTED) {
					
				}
			}
		});

		startTimeField.setText(DesktopUtil.formatDateTime(newStartDate.getTime(),
		    locale, DATE_TIME_PATTERN, timeZonePreference));
		endTimeField.setText(DesktopUtil.formatDateTime(newEndDate.getTime(),
		    locale, DATE_TIME_PATTERN, timeZonePreference));

		mainButtonPanel.add(buttonPanel, BorderLayout.EAST);

		srcField.setText(srcLabel);
		dstField.setText(dstLabel);

		// Populate the src layer box
		populateLayerBox(srcLayerBox);

		// Populate the dst layer box
		populateLayerBox(dstLayerBox);

		// Populate the rate boxes
		populateRateBox(srcRateBox, srcLayerBox.getSelectedIndex(), srcMode);
		populateRateBox(dstRateBox, dstLayerBox.getSelectedIndex(), dstMode);

		JPanel srcPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel dstPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel usrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		usrPanel.setBorder(BorderFactory.createTitledBorder("User group details:"));
		srcPanel.setBorder(BorderFactory.createTitledBorder("Source details: "
		    + srcLabel));
		dstPanel.setBorder(BorderFactory.createTitledBorder("Destination details: "
		    + dstLabel));

		usrPanel.add(groupLabel);
		usrPanel.add(groupBox);

		usrPanel.add(resourceGroupLabel);
		usrPanel.add(resourceGroupBox);

		groupBox.setPreferredSize(new Dimension(165, 21));
		groupBox.setFont(OpenDracDesktop.BASE_FONT);

		resourceGroupBox.setPreferredSize(new Dimension(165, 21));
		resourceGroupBox.setFont(OpenDracDesktop.BASE_FONT);

		populateUserGroupBox(groupBox);

		populateResourceGroupBox(resourceGroupBox, new UserGroupName(
		    (String) groupBox.getSelectedItem()));

		srcLayerBox.setPreferredSize(new Dimension(75, 21));
		srcLayerBox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel srcLayerPanel = new JPanel(new BorderLayout(5, 5));
		srcLayerPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		srcLayerPanel.add(srcLayerLabel, BorderLayout.NORTH);
		srcLayerPanel.add(srcLayerBox, BorderLayout.CENTER);
		srcPanel.add(srcLayerPanel);

		srcTNABox.setPreferredSize(new Dimension(170, 21));
		srcTNABox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel srcTNAPanel = new JPanel(new BorderLayout(5, 5));
		srcTNAPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		srcTNAPanel.add(srcTNALabel, BorderLayout.NORTH); // , BorderLayout.WEST );
		srcTNAPanel.add(srcTNABox, BorderLayout.CENTER); // , BorderLayout.CENTER );
		srcPanel.add(srcTNAPanel);

		srcRateBox.setPreferredSize(new Dimension(75, 21));
		srcRateBox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel srcRatePanel = new JPanel(new BorderLayout(5, 5));
		srcRatePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		srcRatePanel.add(srcRateLabel, BorderLayout.NORTH); // WEST );
		srcRatePanel.add(srcRateBox, BorderLayout.CENTER);
		srcPanel.add(srcRatePanel);

		srcChanBox.setPreferredSize(new Dimension(50, 21));
		srcChanBox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel srcChannelPanel = new JPanel(new BorderLayout(5, 5));
		srcChannelPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		srcChannelPanel.add(srcChannelLabel, BorderLayout.NORTH); // WEST );
		srcChannelPanel.add(srcChanBox, BorderLayout.CENTER);
		srcPanel.add(srcChannelPanel);

		srcWavelengthField.setPreferredSize(new Dimension(50, 21));
		srcWavelengthField.setFont(OpenDracDesktop.BASE_FONT);
		JPanel srcWavelengthPanel = new JPanel(new BorderLayout(5, 5));
		srcWavelengthPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		srcWavelengthPanel.add(srcWavelengthLabel, BorderLayout.NORTH); // WEST );
		srcWavelengthPanel.add(srcWavelengthField, BorderLayout.CENTER);
		srcPanel.add(srcWavelengthPanel);

		srcVlanField.setPreferredSize(new Dimension(50, 21));
		srcVlanField.setFont(OpenDracDesktop.BASE_FONT);
		JPanel srcVlanPanel = new JPanel(new BorderLayout(5, 5));
		srcVlanPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		srcVlanPanel.add(srcVlanLabel, BorderLayout.NORTH); // WEST );
		srcVlanPanel.add(srcVlanField, BorderLayout.CENTER);
		srcPanel.add(srcVlanPanel);

		srcSiteBox.setPreferredSize(new Dimension(75, 21));
		srcSiteBox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel srcSitePanel = new JPanel(new BorderLayout(5, 5));
		srcSitePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		srcSitePanel.add(srcSiteLabel, BorderLayout.NORTH); // WEST );
		srcSitePanel.add(srcSiteBox, BorderLayout.CENTER);
		srcPanel.add(srcSitePanel);

		dstLayerBox.setPreferredSize(new Dimension(75, 21));
		dstLayerBox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel dstLayerPanel = new JPanel(new BorderLayout(5, 5));
		dstLayerPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		dstLayerPanel.add(dstLayerLabel, BorderLayout.NORTH); // WEST );
		dstLayerPanel.add(dstLayerBox, BorderLayout.CENTER);
		dstPanel.add(dstLayerPanel);

		dstTNABox.setPreferredSize(new Dimension(170, 21));
		dstTNABox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel dstTNAPanel = new JPanel(new BorderLayout(5, 5));
		dstTNAPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		dstTNAPanel.add(dstTNALabel, BorderLayout.NORTH);
		dstTNAPanel.add(dstTNABox, BorderLayout.CENTER);
		dstPanel.add(dstTNAPanel);

		dstRateBox.setPreferredSize(new Dimension(75, 21));
		dstRateBox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel dstRatePanel = new JPanel(new BorderLayout(5, 5));
		dstRatePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		dstRatePanel.add(dstRateLabel, BorderLayout.NORTH);
		dstRatePanel.add(dstRateBox, BorderLayout.CENTER);
		dstPanel.add(dstRatePanel);

		dstChanBox.setPreferredSize(new Dimension(50, 21));
		dstChanBox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel dstChannelPanel = new JPanel(new BorderLayout(5, 5));
		dstChannelPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		dstChannelPanel.add(dstChannelLabel, BorderLayout.NORTH);
		dstChannelPanel.add(dstChanBox, BorderLayout.CENTER);
		dstPanel.add(dstChannelPanel);

		dstWavelengthField.setPreferredSize(new Dimension(50, 21));
		dstWavelengthField.setFont(OpenDracDesktop.BASE_FONT);
		JPanel dstWavelengthPanel = new JPanel(new BorderLayout(5, 5));
		dstWavelengthPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		dstWavelengthPanel.add(dstWavelengthLabel, BorderLayout.NORTH); // WEST );
		dstWavelengthPanel.add(dstWavelengthField, BorderLayout.CENTER);
		dstPanel.add(dstWavelengthPanel);

		dstVlanField.setPreferredSize(new Dimension(50, 21));
		dstVlanField.setFont(OpenDracDesktop.BASE_FONT);
		JPanel dstVlanPanel = new JPanel(new BorderLayout(5, 5));
		dstVlanPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		dstVlanPanel.add(dstVlanLabel, BorderLayout.NORTH); // WEST );
		dstVlanPanel.add(dstVlanField, BorderLayout.CENTER);
		dstPanel.add(dstVlanPanel);

		dstSiteBox.setPreferredSize(new Dimension(75, 21));
		dstSiteBox.setFont(OpenDracDesktop.BASE_FONT);
		JPanel dstSitePanel = new JPanel(new BorderLayout(5, 5));
		dstSitePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		dstSitePanel.add(dstSiteLabel, BorderLayout.NORTH); // WEST );
		dstSitePanel.add(dstSiteBox, BorderLayout.CENTER);
		dstPanel.add(dstSitePanel);

		JPanel detailsPanel = new JPanel(new BorderLayout(5, 5));
		JPanel timePanel = new JPanel(new GridLayout(2, 4, 5, 5));
		JPanel protectionPanel = new JPanel(new BorderLayout(5, 5));
		JPanel scheduleTypePanel = new JPanel(new BorderLayout(5, 5));
		JLabel startDateLabel = new JLabel("Start date:");
		JLabel endDateLabel = new JLabel("End date:");
		JLabel protectionLabel = new JLabel("Protection:");
		JLabel scheduleTypeLabel = new JLabel("Schedule Type:");

		detailsPanel.setBorder(BorderFactory
		    .createTitledBorder("Schedule details:"));
		timePanel.add(startDateLabel);
		timePanel.add(startCal);
		timePanel.add(startTimeLabel);
		timePanel.add(startTime);
		timePanel.add(endDateLabel);
		timePanel.add(endCal);
		timePanel.add(endTimeLabel);
		timePanel.add(endTime);

		protectionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		protectionBox.setPreferredSize(new Dimension(250, 21));

		scheduleTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		scheduleTypeBox.setPreferredSize(new Dimension(250, 21));

		protectionPanel.add(protectionLabel, BorderLayout.CENTER);
		protectionPanel.add(protectionBox, BorderLayout.EAST);

		scheduleTypePanel.add(scheduleTypeLabel, BorderLayout.CENTER);
		scheduleTypePanel.add(scheduleTypeBox, BorderLayout.EAST);

		detailsSouthPanel.add(scheduleTypePanel, BorderLayout.CENTER);
		detailsSouthPanel.add(protectionPanel, BorderLayout.EAST);

		scheduleTypeBox.setPreferredSize(new Dimension(150, 21));
		scheduleTypeBox.setMaximumSize(new Dimension(150, 21));
		scheduleTypeBox.setFont(OpenDracDesktop.BASE_FONT);

		protectionBox.setPreferredSize(new Dimension(100, 21));
		protectionBox.setMaximumSize(new Dimension(100, 21));
		protectionBox.setFont(OpenDracDesktop.BASE_FONT);

		protectionBox.addItem("Unprotected");
		protectionBox.addItem("1+1 path");

		startDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		startTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		endDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		endTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		detailsPanel.add(timePanel, BorderLayout.CENTER);
		detailsPanel.add(detailsSouthPanel, BorderLayout.SOUTH);

		JPanel northGridPanel = new JPanel(new GridLayout(3, 1));
		northGridPanel.add(usrPanel);
		northGridPanel.add(srcPanel);
		northGridPanel.add(dstPanel);
		JPanel routingPanel = new JPanel(new BorderLayout(1, 1));
		routingPanel
		    .setBorder(BorderFactory.createTitledBorder("Routing metrics:"));
		populateMetrics(metricsBox);

		metricsBox.setPreferredSize(new Dimension(165, 21));
		metricsBox.setFont(OpenDracDesktop.BASE_FONT);

		metricsField.setPreferredSize(new Dimension(165, 21));

		srlgField.setPreferredSize(new Dimension(260, 21));

		JPanel northRoutingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
		JLabel metricsLabel = new JLabel("Metric:");
		JPanel srlgCenterRoutingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JPanel vcatRoutingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		vcatRoutingPanel.add(vcatRoutingOptionLabel);
		vcatRoutingPanel.add(vcatRoutingOption);

		JLabel srlgLabel = new JLabel("SRLG exclusions (must be comma delimited):");
		JLabel exceedLabel = new JLabel(" does not exceed ");
		exceedLabel.setHorizontalAlignment(SwingConstants.CENTER);
		srlgCenterRoutingPanel.add(srlgLabel);
		srlgCenterRoutingPanel.add(srlgField);

		northRoutingPanel.add(metricsLabel);
		northRoutingPanel.add(metricsBox);
		northRoutingPanel.add(exceedLabel);
		northRoutingPanel.add(metricsField);

		routingPanel.add(northRoutingPanel, BorderLayout.NORTH);
		routingPanel.add(srlgCenterRoutingPanel, BorderLayout.CENTER);
		routingPanel.add(vcatRoutingPanel, BorderLayout.SOUTH);
		gridPanel.add(northGridPanel, BorderLayout.NORTH);
		gridPanel.add(detailsPanel, BorderLayout.CENTER);
		gridPanel.add(routingPanel, BorderLayout.SOUTH);

		pathTable = new JTable(new DefaultTableModel());
		// pathTable columns
		Vector<String> pathCols = new Vector<String>();
		pathCols.add("Source");
		pathCols.add("Target");
		pathCols.add("Source port");
		pathCols.add("Target port");
		pathCols.add("Rate");
		pathCols.add("Source channel");
		pathCols.add("Target channel");

		((DefaultTableModel) pathTable.getModel()).setDataVector(
		    new Vector<Vector<String>>(), pathCols);
		pathTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
		JPanel pathNorthEastPanel = new JPanel(new BorderLayout(1, 1));
		pathPanel.add(new JScrollPane(pathTable), BorderLayout.CENTER);
		pathPanel.add(pathNorthEastPanel, BorderLayout.EAST);

		mainPanel.add(gridPanel, BorderLayout.NORTH);
		mainPanel.add(pathPanel, BorderLayout.CENTER);
		mainPanel.add(mainButtonPanel, BorderLayout.SOUTH);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Image imgClose = desktopUtil.loadImageFromJar("/client/Images/x.png");

		// A create schedule tab is already present. Remove the tab and add it back.
		if (desktop.schedTabPane.getTabCount() > 1) {
			desktop.schedTabPane.remove(OpenDracDesktop.SCHEDTAB_CRT_SCH_IDX);
		}

		desktop.schedTabPane.addTab("Create schedule", new TabCloseIcon(
		    new ImageIcon(imgClose)), mainPanel);

		desktop.tabbedPane.setSelectedIndex(OpenDracDesktop.SCHEDULING_TAB_IDX);
		desktop.schedTabPane.setSelectedIndex(OpenDracDesktop.SCHEDTAB_CRT_SCH_IDX);

		Thread populateCrtSchedThread = new Thread(new PopulateCreateSchedWidgets(
		    desktop, parent, srcTNABox, dstTNABox, srcTNAMap, dstTNAMap,
		    (Layer) srcLayerBox.getSelectedItem(), srcIeee, dstIeee));
		populateCrtSchedThread.setDaemon(true);
		populateCrtSchedThread.start();

	}

	public void showPathDialog() {

		JPanel pathDialogPanel = new JPanel(new GridLayout(2, 2, 5, 5));

		pathDialog = new JDialog(parent);
		pathDialog.setTitle("Path");
		pathDialog.setMinimumSize(new Dimension(150, 50));
		pathDialog.getContentPane().setLayout(new GridLayout(1, 1)); 
		hideButton = new JButton("Hide");
		showButton = new JButton("Show");

		hideButton.setMargin(new Insets(1, 1, 1, 1));
		showButton.setMargin(new Insets(1, 1, 1, 1));

		hideButton.addActionListener(this);
		showButton.addActionListener(this);

		hideButton.setHorizontalTextPosition(SwingConstants.CENTER);
		showButton.setHorizontalTextPosition(SwingConstants.CENTER);

		hideButton.setVerticalTextPosition(SwingConstants.BOTTOM);
		showButton.setVerticalTextPosition(SwingConstants.BOTTOM);

		hideButton.setToolTipText("Hide non-path nodes");
		showButton.setToolTipText("Show non-path nodes");

		pathDialogPanel.add(hideButton);
		pathDialogPanel.add(showButton);

		pathDialog.getContentPane().add(pathDialogPanel);

		pathDialog.pack();
		pathDialog.setLocation(parent.getLocation().x
		    + (int) (parent.getBounds().width * 0.80), parent.getLocation().y
		    + (int) (parent.getBounds().height * 0.50));
		pathDialog.setVisible(true);

		pathDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					pathDialog.setVisible(false);
					NetworkGraph.INSTANCE.resetGraph();
				}
				catch (Exception e1) {
					log.error("Error: ", e1);
				}
			}
		});
	}

	private BigInteger fetchConstraints(String neid, String aid) {
		ServerOperation so = new ServerOperation();
		return so.getConstraints(neid, aid);
	}

	private void filterTNABox(Map<Layer, List<Tna>> tnaMap, JComboBox box,
	    Map<String, String> filter) {
		boolean filterWavelength = filter.get(OpenDracDesktop.WAVELENGTH_ATTR) != null;
		boolean filterSite = filter.get(OpenDracDesktop.SITE_ATTR) != null;
		String filterStrWave = null;
		String filterStrSite = null;

		if (filterWavelength && filterSite) {
			filterStrWave = filter.get(OpenDracDesktop.WAVELENGTH_ATTR);
			filterStrSite = filter.get(OpenDracDesktop.SITE_ATTR);
		}
		else if (filterWavelength) {
			filterStrWave = filter.get(OpenDracDesktop.WAVELENGTH_ATTR);
		}
		else if (filterSite) {
			filterStrSite = filter.get(OpenDracDesktop.SITE_ATTR);
		}

		log.debug("filterStrWave: " + filterStrWave + " filterStrSite: "
		    + filterStrSite + " box.getItemCount(): " + box.getItemCount()
		    + "box.getItemAt(0: " + box.getItemAt(0) + " box.getItemAt(1) "
		    + box.getItemAt(1) + " tnaMap: " + tnaMap);

		if (filterStrWave == null && filterStrSite == null) {
			return;
		}

		if (box != null && box.getItemCount() > 0) {
			Layer layer = ((Tna) box.getItemAt(0)).getLayer();
			
			// if ( layer == 0 ) {
			box.removeAllItems();
			Tna tna = null;
			List<Tna> tnas = tnaMap.get(layer);
			
			for (int i = 0; i < tnas.size(); i++) {
				tna = tnas.get(i);
				if (filterWavelength && filterSite) {
					if (filterStrWave.equalsIgnoreCase(tna.getWavelength())
					    && filterStrSite.equalsIgnoreCase(tna.getSiteId())) {
						box.addItem(tna);
					}
				}
				else if (filterWavelength) {
					if (filterStrWave.equalsIgnoreCase(tna.getWavelength())) {
						box.addItem(tna);
					}
				}
				else if (filterSite) {
					if (filterStrSite.equalsIgnoreCase(tna.getSiteId())) {
						box.addItem(tna);
					}
				}
			}

		}
	}

	private void findSPFInternal(Tna srcTNA, Tna dstTNA, String rate,
	    String theStartTime, String theEndTime, String routingMetric,
	    String srlg, boolean vcatRoutingOption, String srcChannel,
	    String dstChannel, Layer srcLayerE, Layer dstLayerE, String group,
	    String protectionType, String resGroup, String scheduleType,
	    boolean queryOnly, String srcIeee, String dstIeee, String srcVlan,
	    String dstVlan) {
		try {
			// log what we are asking for.
			StringBuilder sb = new StringBuilder();

			// Here, need to transform back to String until final cleanups are done
			// for Layer enum
			String srcLayer = srcLayerE.toString();
			String dstLayer = dstLayerE.toString();

			if (queryOnly) {
				sb.append("Querying shortest path requested between srcTNA:");
			}
			else {
				sb.append("Provisioning shortest path requested between srcTNA:");
			}

			sb.append(srcTNA.toDebugString());

			sb.append(" dstTNA:");
			sb.append(dstTNA.toDebugString());

			sb.append(" srcChannel:");
			sb.append(srcChannel);

			sb.append(" dstChannel:");
			sb.append(dstChannel);

			sb.append(" srcLayer:");
			sb.append(srcLayer);

			sb.append(" dstLayer:");
			sb.append(dstLayer);

			sb.append(" srcIeee:");
			sb.append(srcIeee);

			sb.append(" dstIeee:");
			sb.append(dstIeee);

			sb.append(" srcVlan:");
			sb.append(srcVlan);

			sb.append(" dstVlan:");
			sb.append(dstVlan);

			sb.append(" rate:");
			sb.append(rate);

			sb.append(" theStartTime:");
			sb.append(theStartTime);

			sb.append(" theEndTime:");
			sb.append(theEndTime);

			sb.append(" routingMetric:");
			sb.append(routingMetric);

			sb.append(" srlg:");
			sb.append(srlg);

			sb.append(" group:");
			sb.append(group);

			sb.append(" vcatRoutingOption:");
			sb.append(vcatRoutingOption);

			sb.append(" protectionType:");
			sb.append(protectionType);

			sb.append(" resGroup:");
			sb.append(resGroup);

			sb.append(" scheduleType:");
			sb.append(scheduleType);

			

			int convertedRateInt = -1;

			String convertedRate = null;
			// First, convert the rate to a MB/s value
			if (desktopUtil.getRateMap().get(rate) != null) {
				convertedRate = desktopUtil.getRateMap().get(rate);
			}
			else {
				convertedRate = rate;
			}

			try {
				convertedRateInt = Integer.parseInt(convertedRate);
			}
			catch (Exception e) {
				throw new Exception("Exception converting string rate to int: "
				    + convertedRate, e);
			}

			

			if (queryOnly) {
				if ("".equals(srlg) || srlg != null && "".equals(srlg.trim())) {
					srlg = "unknown";
				}

				final HashMap<SPF_KEYS, String> params = new HashMap<SPF_KEYS, String>();
				params.put(SPF_KEYS.SPF_SOURCEID, srcIeee);
				params.put(SPF_KEYS.SPF_TARGETID, dstIeee);
				params.put(SPF_KEYS.SPF_RATE, convertedRate);
				params.put(SPF_KEYS.SPF_SRCTNA, srcTNA.getTNA());
				params.put(SPF_KEYS.SPF_DSTTNA, dstTNA.getTNA());
				params.put(SPF_KEYS.SPF_START_TIME, theStartTime);
				params.put(SPF_KEYS.SPF_END_TIME, theEndTime);
				params.put(SPF_KEYS.SPF_USER, auth.getUserName());
				params.put(SPF_KEYS.SPF_SRLG, srlg);
				params.put(SPF_KEYS.SPF_VCATROUTING_OPTION,
				    Boolean.toString(vcatRoutingOption));
				params.put(SPF_KEYS.SPF_SRCLAYER, srcLayer);
				params.put(SPF_KEYS.SPF_DSTLAYER, dstLayer);
				params.put(SPF_KEYS.SPF_SRCCHANNEL, srcChannel);
				params.put(SPF_KEYS.SPF_DSTCHANNEL, dstChannel);
				params.put(SPF_KEYS.SPF_USERGROUP, group);
				params.put(SPF_KEYS.SPF_PROTECTION, protectionType);
				params.put(SPF_KEYS.SPF_SRCVLAN, srcVlan);
				params.put(SPF_KEYS.SPF_DSTVLAN, dstVlan);

				/*
				 * SPecial case we get a key/value like "SPF_METRIC=COST=5", and we add
				 * COST=5 into our map.
				 */

				String[] tokens = routingMetric.split("=");
				
				if (tokens.length == 2) {
					log.debug("Setting metric " + tokens[0].toUpperCase() + " to: "
					    + tokens[1]);
					params.put(SPF_KEYS.valueOf("SPF_" + tokens[0].toUpperCase()),
					    tokens[1]);
				}
				else {
					log.error("Invalid or empty routing metric setting detected: " + routingMetric);
				}

				
				desktop.showProgressDialog("Path calculation in progress...");

				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							
							ScheduleResult result = new ServerOperation().querySchedule(
							    params, true);
							
							handleCreateSchedEvent(result, null);
						}
						catch (Exception e) {
							try {
								log.error("Error: ", e);
								handleCreateSchedEvent(null, e);
							}
							catch (Exception e1) {
								log.error("error processing error from querySchedule!", e1);
							}

						}
					}
				}, "query schedule ");
				t.setDaemon(true);
				t.start();
				return;
			}

			String[] metric = routingMetric.split("=");
			String metricName = null;
			String metricValue = null;

			
			
			

			if (metric.length == 2) {
				metricName = metric[0];
				metricValue = metric[1];
			}
			else if (metric.length == 1) {
				metricName = metric[0];
				metricValue = "";
			}
			else {
				JOptionPane.showMessageDialog(parent, "Invalid metric specified: "
				    + routingMetric);
				return;
			}

			EndPointType srcEndPoint = new EndPointType();
			EndPointType dstEndPoint = new EndPointType();
			RecurrenceType recurrence = new RecurrenceType(RecurrenceFreq.FREQ_ONCE,
			    0, 0, null);

			PathType pathType = new PathType();
			pathType.setSrcVlanId(srcVlan);
			pathType.setDstVlanId(dstVlan);
			pathType.setSource(srcIeee);
			pathType.setTarget(dstIeee);
			pathType.setSourceEndPoint(srcEndPoint);
			pathType.setTargetEndPoint(dstEndPoint);
			pathType.setRate(convertedRateInt);
			if ("PATH1PLUS1".equalsIgnoreCase(protectionType)) {
				pathType.setProtectionType(PathType.PROTECTION_TYPE.PATH1PLUS1);
			}
			else {
				pathType.setProtectionType(PathType.PROTECTION_TYPE.UNPROTECTED);
			}

			if (OpenDracDesktop.METRIC_KEY_HOP.equalsIgnoreCase(metricName)) {
				if (metricValue != null && !metricValue.trim().equals("")) {
					pathType.setHop(Integer.parseInt(metricValue));
				}
			}

			if (OpenDracDesktop.METRIC_KEY_COST.equalsIgnoreCase(metricName)) {
				if (metricValue != null && !metricValue.trim().equals("")) {
					pathType.setCost(Integer.parseInt(metricValue));
				}
			}

			if (OpenDracDesktop.METRIC_KEY_METRIC2.equalsIgnoreCase(metricName)) {
				if (metricValue != null && !metricValue.trim().equals("")) {
					pathType.setMetric(Integer.parseInt(metricValue));
				}
			}

			pathType.setSrlg(srlg);
			pathType.setVcatRoutingOption(vcatRoutingOption);

			ACTIVATION_TYPE type = null;
			if (scheduleType.equalsIgnoreCase(AUTO_RESERVATION)) {
				type = ACTIVATION_TYPE.RESERVATION_AUTOMATIC;
			}
			else if (scheduleType.equalsIgnoreCase(MANUAL_PRERESERVATION)) {
				type = Schedule.ACTIVATION_TYPE.PRERESERVATION_MANUAL;
			}
			else if (scheduleType.equalsIgnoreCase(AUTO_PRERESERVATION)) {
				type = Schedule.ACTIVATION_TYPE.PRERESERVATION_AUTOMATIC;
			}
			else if (scheduleType.equalsIgnoreCase(MANUAL_RESERVATION)) {
				type = Schedule.ACTIVATION_TYPE.RESERVATION_MANUAL;
			}
			else if (scheduleType.equalsIgnoreCase(AUTO_RESERVATION)) {
				type = Schedule.ACTIVATION_TYPE.RESERVATION_AUTOMATIC;
			}
			else {
				throw new Exception("unknown/unsupported schedule type '"
				    + scheduleType + "'");
			}

			// non-recurring schedules are now duration-based
			long duration = Math.abs(Long.parseLong(theEndTime)
			    - Long.parseLong(theStartTime));
			UserType ut = new UserType(auth.getUserName(), new UserGroupName(group),
			    group, group, resGroup, resGroup, null);

			Schedule schedule = new Schedule(type, "unknown", auth.getUserName()
			    + "@" + InetAddress.getLocalHost().getHostName() + "_"
			    + System.currentTimeMillis(), State.SCHEDULE.EXECUTION_PENDING,
			    Long.parseLong(theStartTime), Long.parseLong(theEndTime), duration,
			    ut, pathType, false, recurrence, null);

			if (srcChannel != null) {
				srcEndPoint.setChannelNumber(Integer.parseInt(srcChannel));
			}

			srcEndPoint.getAttributes().put(FacilityConstants.TNA_ATTR,
			    srcTNA.getTNA());
			srcEndPoint.getAttributes().put(FacilityConstants.LAYER_ATTR, srcLayer);
			srcEndPoint.setId(srcTNA.getNEID() + "_" + srcTNA.getAID());
			if (dstChannel != null) {
				dstEndPoint.setChannelNumber(Integer.parseInt(dstChannel));
			}
			dstEndPoint.setId(dstTNA.getNEID() + "_" + dstTNA.getAID());
			dstEndPoint.getAttributes().put(FacilityConstants.TNA_ATTR,
			    dstTNA.getTNA());
			dstEndPoint.getAttributes().put(FacilityConstants.LAYER_ATTR, dstLayer);

			
			desktop.showProgressDialog("Creating schedule...");
			ServerOperation so = new ServerOperation(
			    ServerOperation.Operation.OP_CREATE_SCHEDULE, this, schedule);
			Thread t = new Thread(so);
			t.setDaemon(true);
			t.start();
		}
		catch (Exception e) {
			log.error("Error: ", e);
			JOptionPane.showMessageDialog(parent,
			    "Exception occurred creating schedule: " + e.getMessage(), "Error",
			    JOptionPane.ERROR_MESSAGE);
		}
	}

	private Element getElementFromResult(ScheduleResult r) {
		GenericJdomParser parser = new GenericJdomParser();
		parser.parse(r.getXmlPath());
		Element e = parser.getRoot();

		if ("pathEvent".equals(e.getName())) {
			
			return e;
		}

		if (e.getChild("pathEvent") != null) {
			Element pe = e.getChild("pathEvent");
			
			return pe;
		}
		log.error("ServerOperation: querySchedule unable to find pathEvent in XML string "
		    + r + " returning null");
		return null;

	}

	private String getLayer2Rate(String layer1Rate) {
		return desktopUtil.getRateInMb(layer1Rate);
	}

	private String getProtection(String s) {
		String result = null;
		if ("Unprotected".equalsIgnoreCase(s)) {
			result = "UNPROTECTED";
		}
		else if ("1+1 path".equalsIgnoreCase(s)) {
			result = "PATH1PLUS1";
		}
		return result;
	}

	private void handleCreateSchedEvent(ScheduleResult r, Throwable t)
	    throws Exception { // HashMap event ) {
		
		desktop.hideProgressDialog();

		// If we were given an exception display it and leave.
		if (t != null) {
			JOptionPane.showMessageDialog(parent, "Path exception:" + t.getMessage(),
			    "Exception", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// The ScheduleResult might contain an exception inside of it (old code
		// path)

		Element event = getElementFromResult(r);
		if (event == null || event.getChild("exception") != null) {
			if (event != null) {
				Element exception = event.getChild("exception");
				if (exception != null) {
					JOptionPane.showMessageDialog(parent,
					    "Path exception: " + exception.getAttributeValue("message"),
					    "Exception", JOptionPane.ERROR_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(parent,
					    "Path exception: Unknown exception occurred.", "Exception",
					    JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				JOptionPane.showMessageDialog(parent,
				    "Path exception: Unknown exception occurred.", "Exception",
				    JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		
		if (event.getChild("pathInstance") != null
		    && event.getChild("pathInstance").getChild("edgelist") != null) {

			java.util.List<Element> paths = event.getChildren("pathInstance");
			java.util.List<Element> edgesWorking = null;
			edgesProtecting = null;

			edgesWorking = paths.get(0).getChild("edgelist").getChildren("edge");

			if (paths.size() == 2) { // One protection path
				edgesProtecting = paths.get(1).getChild("edgelist").getChildren("edge");
			}

			Element status = event.getChild("pathInstance").getChild("status");
			String statusText = status.getAttributeValue("text");
			
			// 

			currentShortPath = edgesWorking;
			if (currentShortPath != null) {

				if (event.getChild("pathInstance").getChild("endpoints") != null
				    && event.getChild("pathInstance").getChild("endpoints")
				        .getChild("source") != null
				    && event.getChild("pathInstance").getChild("endpoints")
				        .getChild("target") != null) {
					Element src = event.getChild("pathInstance").getChild("endpoints")
					    .getChild("source");
					Element dst = event.getChild("pathInstance").getChild("endpoints")
					    .getChild("target");

					currentSrc = src.getAttributeValue(OpenDracDesktop.ID_ATTR);
					currentDst = dst.getAttributeValue(OpenDracDesktop.ID_ATTR);
					currentSrcTna = src.getAttributeValue(OpenDracDesktop.TNA_ATTR);
					currentDstTna = dst.getAttributeValue(OpenDracDesktop.TNA_ATTR);

					NetworkGraph.INSTANCE.highlightPathAndEndPoints(
					    currentShortPath, currentSrc, currentDst, currentSrcTna,
					    currentDstTna);

					if (edgesProtecting != null) {
						NetworkGraph.INSTANCE.highlightPathDotted(edgesProtecting,
						    Color.red);
					}
				}
				else {
					NetworkGraph.INSTANCE.highlightPath(currentShortPath);
				}
				showPathDialog();
			}
			else {
				JOptionPane.showMessageDialog(parent,
				    "No path available at the specified time.", "Error",
				    JOptionPane.ERROR_MESSAGE);
			}
			desktop.updateStatus(statusText, true);
		}

		if (event.getChild("pathInstance").getChild("path") != null) {
			java.util.List<Element> edgeList = event.getChild("pathInstance")
			    .getChild("path").getChildren("edge");
			Element anEdge = null;
			Vector<String> row = null;

			if (edgeList != null) {
				if (((DefaultTableModel) pathTable.getModel()).getDataVector().size() != 0) {
					// Clear the data vector first
					((DefaultTableModel) pathTable.getModel()).getDataVector().clear();
				}

				String srcChanStr = null;
				String tgtChanStr = null;

				for (int i = 0; i < edgeList.size(); i++) {
					anEdge = edgeList.get(i);
					if (anEdge != null) {
						// ne = (IltNetworkElement) ilogPanel.getElements().get( (String)
						// anEdge.getAttributeValue("source") );
						// if ( ne != null ) neMode = (String)ne.getProperty( "MODE" );
						row = new Vector<String>();
						// WP: This steams silly, get the IEEE from the xml, look up the NE
						// then fetch the
						// IEEE?
						// row.add(getCurrentView().getElementProperty(anEdge.getAttributeValue("source"),
						// "IEEE"));
						// row.add(getCurrentView().getElementProperty(anEdge.getAttributeValue("target"),
						// "IEEE"));
						row.add(NeCache.INSTANCE
						    .getVertex(anEdge.getAttributeValue("source")).getIeee());
						row.add(NeCache.INSTANCE
						    .getVertex(anEdge.getAttributeValue("target")).getIeee());
						row.add(anEdge.getAttributeValue("sourceport"));
						row.add(anEdge.getAttributeValue("targetport"));
						row.add(anEdge.getAttributeValue("rate"));
						srcChanStr = anEdge.getAttributeValue("sourcechannel");
						tgtChanStr = anEdge.getAttributeValue("targetChannel");

						row.add(srcChanStr);
						row.add(tgtChanStr);
						((DefaultTableModel) pathTable.getModel()).addRow(row);
					}
				}

				Map<String, String> allNesMap = NeCache.INSTANCE.getIeeeLabelMap();
				TableColumn col = pathTable.getColumn("Source");
				if (col != null) {
					col.setCellRenderer(new TidCellRenderer(allNesMap));
				}
				col = pathTable.getColumn("Target");
				if (col != null) {
					col.setCellRenderer(new TidCellRenderer(allNesMap));
				}
				col = pathTable.getColumn("Source port");
				if (col != null) {
					col.setCellRenderer(new AidCellRenderer(allNesMap));
				}
				col = pathTable.getColumn("Target port");
				if (col != null) {
					col.setCellRenderer(new AidCellRenderer(allNesMap));
				}

				DesktopUtil.sizeColumns(pathTable);
			}
		}
	}

	private void populateLayerBox(JComboBox box) {
		box.addItem(Layer.LAYER0);
		box.addItem(Layer.LAYER1);
		box.addItem(Layer.LAYER2);
		box.setSelectedIndex(1);
	}

	private void populateMetrics(JComboBox box) {
		box.addItem(OpenDracDesktop.METRIC_KEY_COST);
		box.addItem(OpenDracDesktop.METRIC_KEY_METRIC2);
		box.addItem(OpenDracDesktop.METRIC_KEY_HOP);
		box.setSelectedIndex(2);
	}

	private void populateResourceGroupBox(JComboBox groupBox,
	    UserGroupName userGroup) {
		Set<String> resourceGroups = null;
		groupBox.removeAllItems();

		ServerOperation so = new ServerOperation();
		resourceGroups = so.getResourceGroups(auth.getUserName(), userGroup);
		if (resourceGroups != null) {
			for (String rgp : resourceGroups) {
				groupBox.addItem(rgp);
			}
		}
	}

	private void populateSchedTypeBox(JComboBox box) {
		box.removeAllItems();
		box.addItem("Automatic reservation");
		box.addItem("Manual reservation");
		box.addItem("Automatic pre-reservation");
		box.addItem("Manual pre-reservation");
	}

	private void populateSiteBox(Map<Layer, List<Tna>> tnaMap, JComboBox box) {
		Iterator<Layer> it = tnaMap.keySet().iterator();
		Set<String> unique = new HashSet<String>();

		if (box != null) {
			box.removeAllItems();
		}
		while (it.hasNext()) {
			Layer layer = it.next();
			java.util.List<Tna> layerList = tnaMap.get(layer);
			Tna tna = null;

			if (box != null) {
				if (layerList != null) {
					for (int i = 0; i < layerList.size(); i++) {
						tna = layerList.get(i);
						log.debug("populateSiteBox: For layer: " + layer + " adding: "
						    + tna.getSiteId() + " to siteId box");
						unique.add(tna.getSiteId());
					}
					Iterator<String> iter = unique.iterator();
					while (iter.hasNext()) {
						box.addItem(iter.next());
					}
				}
			}
		}
	}

	private void populateUserGroupBox(JComboBox groupBox) {
		groupBox.removeAllItems();
		java.util.List<UserGroupProfile> groups = new ServerOperation()
		    .getUserGroupProfiles();
		if (groups != null) {
			for (int i = 0; i < groups.size(); i++) {
				UserGroupProfile ugp = groups.get(i);
				groupBox.addItem(ugp.getName().toString());
			}
		}
	}
}
