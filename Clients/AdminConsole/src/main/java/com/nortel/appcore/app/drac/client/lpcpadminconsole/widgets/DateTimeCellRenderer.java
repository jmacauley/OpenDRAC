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
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.DesktopUtil;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;

public final class DateTimeCellRenderer extends JLabel implements
    TableCellRenderer {
	private static final long serialVersionUID = 3100761256028092656L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final TimeZone timeZone;
	private final Locale locale;
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm.SSS";
	private final SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_PATTERN);

	public DateTimeCellRenderer(Locale loc, TimeZone tz) {
		timeZone = tz == null ? TimeZone.getDefault() : tz;
		locale = loc == null ? Locale.getDefault() : loc;
		sdf.setTimeZone(timeZone);
		// Let the cell colour bleed through
		this.setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	    boolean isSelected, boolean hasFocus, int row, int column) {
		try {
			if (value == null) {
				setText("");
			}

			else if (value instanceof Long) {
				setText(DesktopUtil.formatDateTime(((Long) value).longValue(), locale,
				    DateFormatter.COMMON_DATETIME_FORMAT, timeZone));
			}
			else if (value instanceof String) {
				setText(DesktopUtil.formatDateTime(Long.parseLong((String) value),
				    locale, DateFormatter.COMMON_DATETIME_FORMAT, timeZone));
			}

			else {
				setText("??? " + value.getClass().toString());
			}
			/*
			 * Set the foreground and background colors from the table if they are not
			 * set
			 */
			Color cellForeground = table.getForeground();
			Color cellBackground = table.getBackground();

			if (isSelected) {
				cellForeground = table.getSelectionForeground();
				cellBackground = table.getSelectionBackground();
			}

			// TODO: What is the diff between super.setForeground and setForeground?
			super.setForeground(cellForeground);
			super.setBackground(cellBackground);
			setForeground(cellForeground);
			setBackground(cellBackground);
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
		return this;
	}
}