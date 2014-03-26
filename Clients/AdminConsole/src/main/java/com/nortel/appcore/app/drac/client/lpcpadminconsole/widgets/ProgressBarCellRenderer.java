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

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProgressBarCellRenderer Allows a JTable cell to display a progress bar. This
 * is used in the DRAC bandwidth utilisation table.
 * 
 * @author adlee
 * @since 2006-01-12
 */
public final class ProgressBarCellRenderer extends JProgressBar implements
    TableCellRenderer {
	private static final long serialVersionUID = -7073102360596420231L;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public ProgressBarCellRenderer() {
		UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
		UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	    boolean isSelected, boolean hasFocus, int row, int column) {
		if (value.equals("")) {
			return this;
		}

		int intValue = -1;

		if (value != null) {
			try {
				intValue = (int) Double.parseDouble(value.toString());
				if (intValue == 0 && !"0".equals(value.toString())) {
					intValue = (int) Math.ceil(Double.parseDouble(value.toString()));
				}
			}
			catch (Exception e) {
				log.error("Error: ", e);
			}
		}

		setStringPainted(true);
		
		if (intValue <= 25) {
			setForeground(Color.GREEN);
			setValue(intValue);
		}

		if (intValue > 25 && intValue <= 50) {
			setForeground(Color.YELLOW);
			setValue(intValue);
		}

		if (intValue >= 75) {
			setForeground(Color.RED);
			setValue(intValue);
		}

		if (intValue > 50 && intValue < 75) {
			setForeground(Color.ORANGE);
			setValue(intValue);
		}

		return this;
	}

}
