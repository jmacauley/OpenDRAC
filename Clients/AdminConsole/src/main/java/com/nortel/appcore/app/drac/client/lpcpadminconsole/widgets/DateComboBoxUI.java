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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.CalendarComboBoxModel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.CalendarPanel;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox.ComponentComboPopup;

public final class DateComboBoxUI extends BasicComboBoxUI {
	private final JPanel popupPanel;
	private final DateDropDown comboBox;

	public DateComboBoxUI(final DateDropDown dateDrop) {
		comboBox = dateDrop;
		boolean showTime = false;
		boolean showAMPM = true;
		boolean showRelative = false;
		popupPanel = new CalendarPanel(
		    (CalendarComboBoxModel) comboBox.getModel(), this, showTime, showAMPM,
		    showRelative, new Font("SanSerif", Font.PLAIN, 10));
		((CalendarPanel) popupPanel).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comboBox.repaint();
			}
		});
	}

	/**
	 * Creates the popup to be displayed when the user clicks on the combo box.
	 * 
	 * @see javax.swing.plaf.basic.BasicComboBoxUI#createPopup()
	 */
	@Override
	protected ComboPopup createPopup() {
		ComponentComboPopup myPopup = new ComponentComboPopup(comboBox,
		    popupPanel);
		myPopup.getAccessibleContext().setAccessibleParent(comboBox);
		myPopup.addObserver(comboBox);
		return myPopup;
	}
}