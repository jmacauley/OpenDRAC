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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChannelSelector extends JComboBox {
  private final Logger log = LoggerFactory.getLogger(getClass());

	private class ChannelPopup extends BasicComboPopup {

		/**
         * 
         */
		private static final long serialVersionUID = -93202893495359828L;
		BigInteger constraints = null;
		String rate = null;
		JComboBox box = null;
		Constraints component = null;
		Color defaultColor = (Color) UIManager.get("ToggleButton.background");
		JPopupMenu popup = new JPopupMenu();
		MouseListener mouseListener = null;

		public ChannelPopup(String rate, BigInteger constraints, final JComboBox box) {
			super(box);
			this.rate = rate;
			this.constraints = constraints;
			this.box = box;

			mouseListener = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent me) {
					JToggleButton jtb = (JToggleButton) me.getSource();
					if (jtb.isEnabled()) {
						box.removeAllItems();
						box.addItem(jtb.getToolTipText());
						jtb.setBackground(defaultColor);
						popup.setVisible(false);
					}
				}

				@Override
				public void mouseEntered(MouseEvent me) {
					JToggleButton jtb = (JToggleButton) me.getSource();
					if (jtb.isEnabled()) {
						jtb.setBackground(Color.BLUE);
					}
				}

				@Override
				public void mouseExited(MouseEvent me) {
					JToggleButton jtb = (JToggleButton) me.getSource();
					if (jtb.isEnabled()) {
						jtb.setBackground(defaultColor);
					}
				}

			};

			component = new Constraints(rate, true, null, true, false);
			popup.add(component);
			component.addMouseListenerToButtons(mouseListener);
		}

		public ChannelPopup(String rate, JComboBox box) {
			super(box);
		}

		public void setConstraints(BigInteger constraints) {
			if (this.constraints != null && this.constraints.equals(constraints)) {
				log.debug("Not setting same constraints");
				return;
			}
			int firstUnconstrainedChannel = -1;
			int currentSelectedChannel = component.getSelectedChannel();
			component.setConstraints(constraints);
			firstUnconstrainedChannel = component.getFirstUnconstrainedChannel();
			log.debug("firstUnconstrainedChannel is: " + firstUnconstrainedChannel);
			if (currentSelectedChannel == -1 && firstUnconstrainedChannel != -1) {
				box.removeAllItems();
				box.addItem("" + firstUnconstrainedChannel);
			}
			else if (currentSelectedChannel != -1) {
				box.removeAllItems();
				box.addItem("" + currentSelectedChannel);
			}
		}

		public void setRate(String rate) {
			if (this.rate.equals(rate)) {
				return;
			}
			boolean popupVisible = false;
			this.rate = rate;
			popupVisible = popup.isVisible();
			if (popupVisible) {
				popup.setVisible(false);
			}
			popup.remove(component);

			component = new Constraints(rate, true, null, true, false);
			popup.add(component);
			component.addMouseListenerToButtons(mouseListener);
			if (popupVisible) {
				show();
			}

		}

		@Override
		public void show() {
			component.select((String) box.getSelectedItem());
			popup.show(box, 0 - box.getWidth(), box.getHeight());
		}

	}

	private class ChannelSelectorUI extends BasicComboBoxUI {

		JComboBox comboBox = null;
		ChannelPopup popup = null;

		public ChannelSelectorUI(JComboBox comboBox) {
			this.comboBox = comboBox;
		}

		public void setConstraints(BigInteger constraints) {
			popup.setConstraints(constraints);
		}

		public void setRate(String rate) {
			popup.setRate(rate);
		}

		@Override
		protected ComboPopup createPopup() {
			popup = new ChannelPopup(rate, constraints, comboBox);
			return popup;
		}

	}

	/**
     * 
     */
	private static final long serialVersionUID = 6013953398348724013L;
	private BigInteger constraints = null;

	private String rate = null;

	private ChannelSelectorUI selector = null;

	public ChannelSelector(String rate, BigInteger constraints) {
		this.rate = rate;
		this.constraints = constraints;
		selector = new ChannelSelectorUI(this);
		setUI(selector);
	}

	public void addMouseListenerToButton(MouseListener mouseListener) {
		Component[] components = this.getComponents();
		for (Component component2 : components) {
			if (component2 instanceof JButton) {
				((JButton) component2).addMouseListener(mouseListener);
			}
		}
	}

	public void removeMouseListenerToButton(MouseListener mouseListener) {
		Component[] components = this.getComponents();
		for (Component component2 : components) {
			if (component2 instanceof JButton) {
				((JButton) component2).removeMouseListener(mouseListener);
			}
		}
	}

	public void setConstraints(BigInteger constraints) {
		selector.setConstraints(constraints);
	}

	public void setRate(String rate) {
		this.rate = rate;
		selector.setRate(rate);
	}
}