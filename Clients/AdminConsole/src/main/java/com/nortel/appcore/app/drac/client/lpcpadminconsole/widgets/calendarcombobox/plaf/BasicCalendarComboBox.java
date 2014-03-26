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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.plaf;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.CalendarComboBox;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.CalendarComboBoxModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.CalendarPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.ComponentComboPopup;

public final class BasicCalendarComboBox extends BasicComboBoxUI {
	private final JPanel popupPanel;
	private final CalendarComboBox comboBox;
	private boolean showTime;
	private boolean showAMPM;

	public BasicCalendarComboBox(final CalendarComboBox box,
	    boolean doShowTime, boolean doShowAMPM, boolean doShowRelative, Font font) {
		comboBox = box;
		showTime = doShowTime;
		showAMPM = doShowAMPM;
		popupPanel = new CalendarPanel((CalendarComboBoxModel) box.getModel(),
		    this, doShowTime, doShowAMPM, doShowRelative, font);
		((CalendarPanel) popupPanel).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				box.updateToolTipText();
				box.repaint();
			}

		});
	}

	public boolean getShowAMPM() {
		return showAMPM;
	}

	public boolean getShowTime() {
		return showTime;
	}

	public void setShowAMPM(boolean showAMPM) {
		this.showAMPM = showAMPM;
	}

	public void setShowTime(boolean showTime) {
		this.showTime = showTime;
	}

	@Override
	protected ComboPopup createPopup() {
		ComponentComboPopup myPopup = new ComponentComboPopup(comboBox,
		    popupPanel);
		myPopup.getAccessibleContext().setAccessibleParent(comboBox);
		myPopup.addObserver(comboBox);
		return myPopup;
	}

}
