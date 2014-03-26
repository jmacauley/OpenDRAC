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
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * TidCellRenderer Allows a JTable cell to render an NE's IEEE address as it's
 * TID.
 * 
 * @author adlee
 * @since 2006-01-12
 */
public class TidCellRenderer extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = 5632623749826001694L;
	private final Map<String, String> elementsMap;

	public TidCellRenderer(Map<String, String> elementsMap) {
		this.elementsMap = elementsMap;
		setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	    boolean isSelected, boolean hasFocus, int row, int column) {

		Color cellForeground = null;
		Color cellBackground = null;

		if (value == null || value.equals("")) {
			return this;
		}

		// Set the foreground and background colors
		// from the table if they are not set
		cellForeground = table.getForeground();
		cellBackground = table.getBackground();

		if (isSelected) {

			cellForeground = table.getSelectionForeground();
			cellBackground = table.getSelectionBackground();

		}

		super.setForeground(cellForeground);
		super.setBackground(cellBackground);
		setForeground(cellForeground);
		setBackground(cellBackground);

		String neName = elementsMap.get(value);

		if (neName != null) {
			setText(neName);
		}
		else {
			setText((String) value);
		}

		return this;
	}

}
