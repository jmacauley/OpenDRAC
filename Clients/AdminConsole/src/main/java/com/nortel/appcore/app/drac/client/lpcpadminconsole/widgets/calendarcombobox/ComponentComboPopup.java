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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;

public final class ComponentComboPopup extends BasicComboPopup {
	private static final long serialVersionUID = 1L;
	private final List<ComponentComboPopupObserver> observers = new ArrayList<ComponentComboPopupObserver>();

	public ComponentComboPopup(JComboBox comboBox, Component component) {
		super(comboBox);
		removeAll();
		add(component, "Center");
	}

	public void addObserver(ComponentComboPopupObserver observer) {
		observers.add(observer);
	}

	@Override
	public void hide() {
		// ignore
	}

	@Override
	public void setVisible(boolean b) {
		if (isVisible() && !b) {
			updateObservers();
		}
		super.setVisible(b);
	}

	@Override
	public void show() {
		Point location = getPopupLocation();
		show(comboBox, location.x, location.y);
	}

	JComboBox getComboBox() {
		return comboBox;
	}

	void hideForConvMenu() {
		super.setVisible(false);
	}

	protected void updateObservers() {
		for (ComponentComboPopupObserver o : observers) {
			o.comboPopupChanged();
		}
	}

	private Point getPopupLocation() {
		Dimension popupSize = comboBox.getSize();
		Insets insets = getInsets();
		popupSize.setSize(popupSize.width - (insets.right + insets.left),
		    getPopupHeightForRowCount(comboBox.getMaximumRowCount()));
		Rectangle popupBounds = computePopupBounds(0, comboBox.getBounds().height,
		    popupSize.width, popupSize.height);
		Dimension scrollSize = popupBounds.getSize();
		Point popupLocation = popupBounds.getLocation();
		scroller.setMaximumSize(scrollSize);
		scroller.setPreferredSize(scrollSize);
		scroller.setMinimumSize(scrollSize);
		return popupLocation;
	}
}
