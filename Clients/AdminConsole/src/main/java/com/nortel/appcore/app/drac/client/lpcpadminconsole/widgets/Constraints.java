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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constraints extends JPanel {

	private static final long serialVersionUID = 778116193737219142L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final JPanel selectPanel = new JPanel();
	private final JCheckBox selectAllCheck = new JCheckBox("Select All");
	private JPanel buttonsPanel;
	private JToggleButton jtb;
	private JToggleButton channelButtons[];
	private final Color defaultColour = (Color) UIManager
	    .get("ToggleButton.background");
	private final Font buttonFont = new Font("SanSerif", Font.PLAIN, 8);
	private int channels = -1;
	private BigInteger constraints;
	private boolean shiftPressed;
	private final boolean compact;
	private boolean multiselect;
	private boolean enabled;
	private int shiftStartIdx = -1;
	private int shiftEndIdx = -1;
	private int buttonWidth = 20;
	private int buttonHeight = 20;
	private final Map<JToggleButton, String> buttonsMap = new HashMap<JToggleButton, String>();
	private KeyListener keyListener;
	private MouseListener mouseListener;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	private final JButton editConstraintsButton = new JButton("Edit constraints");

	public Constraints(String rate, boolean enabled, BigInteger constraints,
	    boolean compact, boolean multiselect) {

		this.compact = compact;
		this.multiselect = multiselect;
		this.enabled = enabled;

		keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
					if (!shiftPressed) {
						
						shiftPressed = true;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent ke) {
				
				if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
					
					shiftPressed = false;
					shiftStartIdx = -1;
					shiftEndIdx = -1;
				}
			}

			@Override
			public void keyTyped(KeyEvent ke) {
				// not used
			}

		};

		mouseListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				if (me.getSource() instanceof JToggleButton) {
					
					if (shiftPressed) {
						log.debug("JToggleButton at index " + buttonsMap.get(me.getSource())
						    + " was pressed.");
						if (shiftStartIdx == -1) {
							shiftStartIdx = Integer.parseInt(buttonsMap.get(me.getSource()));
						}
						else {
							shiftEndIdx = Integer.parseInt(buttonsMap.get(me.getSource()));
						}

						if (shiftStartIdx != -1 && shiftEndIdx != -1) {
							for (int i = shiftStartIdx; i < shiftEndIdx - 1; i++) {
								// i+1 because the ith button would already have been toggled
								// Similarly for shiftEndIdx-1
								if (i + 1 < channelButtons.length) {
									channelButtons[i + 1].setSelected(!channelButtons[i + 1]
									    .isSelected());
								}
							}
						}
					}
					else {
						shiftStartIdx = Integer.parseInt(buttonsMap.get(me.getSource()));
					}
				}
			}
		};

		if (!compact) {
			selectPanel.add(selectAllCheck);
			if (constraints != null && !enabled) {

				selectPanel.add(editConstraintsButton);

				editConstraintsButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ae) {
						int option = JOptionPane
						    .showConfirmDialog(
						        null,
						        "Warning! Editing constraints may be service affecting.  Are you sure you wish to continue?");
						setEnabled(option == JOptionPane.YES_OPTION);
						((JButton) ae.getSource())
						    .setEnabled(!(option == JOptionPane.YES_OPTION));
					}
				});

			}
		}
		if (constraints != null) {
			this.constraints = new BigInteger(constraints.toString());
		}
		else {
			this.constraints = new BigInteger("0");
		}
		channels = convertRateToChannels(rate);
		

		if (channels >= 24) {
			buttonsPanel = new JPanel(new GridLayout(0, 24));
		}
		else {
			buttonsPanel = new JPanel(new GridLayout(0, channels));
		}

		setLayout(new BorderLayout());

		if (compact) {
			buttonWidth = 10;
			buttonHeight = 10;
		}

		populateButtons(rate);

		this.add(buttonsPanel, BorderLayout.NORTH);
		if (!compact) {
			this.add(selectPanel, BorderLayout.SOUTH);
		}

		selectAllCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (selectAllCheck.isSelected()) {
					selectAll();
				}
				else {
					deSelectAll();
				}
			}
		});

		setEnabled(enabled);

	}

	public void addMouseListenerToButtons(MouseListener mouseListener) {
		for (JToggleButton channelButton : channelButtons) {
			JToggleButton jtb = channelButton;
			jtb.addMouseListener(mouseListener);
		}
	}

	public void deSelectAll() {
		for (int i = 0; i < channels; i++) {
			jtb = channelButtons[i];
			jtb.setSelected(false);
		}
	}

	public BigInteger getConstraints() {
		for (int i = 0; i < channels; i++) {
			jtb = channelButtons[i];
			if (jtb.isSelected()) {				
				constraints = constraints.setBit(i + 1);						
			}
			else {				
				constraints = constraints.clearBit(i + 1);
			}
		}
		
		return constraints;
	}

	public int getFirstUnconstrainedChannel() {
		int idx = -1;
		for (int i = 0; i < channelButtons.length; i++) {
			jtb = channelButtons[i];
			if (jtb.isEnabled()) {
				idx = i;
				break;
			}
		}
		if (idx >= channelButtons.length) {
			idx = -1;
		}
		else if (idx >= 0) {
			idx = idx + 1;
		}
		return idx;
	}

	public int getSelectedChannel() {
		int selectedChannel = -1;
		for (int i = 0; i < channelButtons.length; i++) {
			jtb = channelButtons[i];
			if (jtb.isSelected()) {
				selectedChannel = i;
			}
		}

		if (selectedChannel >= 0) {
			selectedChannel++;
		}

		return selectedChannel;
	}

	public List<String> getUnconstrainedChannels() {
		List<String> list = new ArrayList<String>();

		if (channelButtons != null) {
			for (int i = 0; i < channelButtons.length; i++) {
				jtb = channelButtons[i];
				if (!jtb.isSelected()) {
					list.add("" + (i + 1));
				}
			}
		}

		return list;
	}

	public void select(String channel) {
		try {
			int channelInt = Integer.parseInt(channel);
			jtb = channelButtons[channelInt - 1];
			jtb.setSelected(true);
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
	}

	public void selectAll() {
		for (int i = 0; i < channels; i++) {
			jtb = channelButtons[i];
			jtb.setSelected(true);
		}
	}

	public void setConstraints(BigInteger constraints) {
		this.constraints = constraints;

		resetButtons();
		if (compact && constraints != null) {
			
			for (int i = 0; i < channelButtons.length; i++) {
				jtb = channelButtons[i];
				if (constraints.testBit(i + 1)) {
					jtb.setEnabled(false);
					jtb.setBackground(Color.DARK_GRAY);
				}
				else {
					jtb.setEnabled(true);
				}
			}
		}
	}

	@Override
	public void setEnabled(boolean enable) {
		for (int i = 0; i < channels; i++) {
			jtb = channelButtons[i];
			jtb.setEnabled(enable);
		}
		selectAllCheck.setEnabled(enable);
	}

	public void setRate(String rate) {
		populateButtons(rate);
	}

	private int convertRateToChannels(String rate) {
		int convertedRate = -1;
		if (rate != null) {
			if (rate.startsWith("STM64")) {
				convertedRate = 64;
			}
			else if (rate.startsWith("STM16")) {
				convertedRate = 16;
			}
			else if (rate.startsWith("STM4")) {
				convertedRate = 4;
			}
			else if (rate.startsWith("STM3")) {
				convertedRate = 3;
			}
			else if (rate.startsWith("OC192")) {
				convertedRate = 192;
			}
			else if (rate.startsWith("OC48")) {
				convertedRate = 48;
			}
			else if (rate.startsWith("OC12")) {
				convertedRate = 12;
			}
			else if (rate.startsWith("OC3")) {
				convertedRate = 3;
			}
			else if (rate.startsWith("OC1")) {
				convertedRate = 1;
			}
		}
		return convertedRate;
	}

	private void populateButtons(String rate) {

		resetPanel();
		channels = convertRateToChannels(rate);
		

		if (channels < 1) {
			return;
		}

		setLayout(new BorderLayout(1, 1));
		String label = null;

		channelButtons = new JToggleButton[channels];

		List<JToggleButton> testList = new ArrayList<JToggleButton>();

		for (int i = 0; i < channels; i++) {
			label = "" + (i + 1);
			jtb = new JToggleButton();
			if (!compact) {
				jtb.setText(label);
			}
			jtb.setRolloverEnabled(compact);
			jtb.setFont(buttonFont);
			jtb.setEnabled(enabled);
			jtb.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
			jtb.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
			jtb.setMinimumSize(new Dimension(buttonWidth, buttonHeight));
			if (!compact) {
				jtb.setSelected(this.constraints.testBit(i + 1));
			}
			else {
				jtb.setEnabled(!this.constraints.testBit(i + 1));
			}

			testList.add(jtb);

			jtb.addKeyListener(keyListener);
			jtb.addMouseListener(mouseListener);
			jtb.setToolTipText(label);

			channelButtons[i] = jtb;
			buttonsMap.put(jtb, "" + i);
			jtb.setMargin(new Insets(0, 0, 0, 0));
			if (!multiselect) {
				buttonGroup.add(jtb);
			}
			buttonsPanel.add(jtb);
		}
	}

	private void resetButtons() {
		for (JToggleButton channelButton : channelButtons) {
			jtb = channelButton;
			jtb.setEnabled(true);
			jtb.setBackground(defaultColour);
			jtb.setSelected(false);
		}
	}

	private void resetPanel() {

		Enumeration<AbstractButton> buttons = buttonGroup.getElements();

		buttonsMap.clear();
		while (buttons.hasMoreElements()) {
			buttonGroup.remove(buttons.nextElement());
		}

		buttonsPanel.removeAll();
		channelButtons = null;

	}
}