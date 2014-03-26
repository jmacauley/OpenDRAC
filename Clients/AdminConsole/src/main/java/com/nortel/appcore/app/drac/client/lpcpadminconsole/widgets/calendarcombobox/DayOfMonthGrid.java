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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;

public final class DayOfMonthGrid extends JComponent {
	private static final long serialVersionUID = 1L;


	private static final int USUAL_DAYS_IN_MONTH[] = { 31, 28, 31, 30, 31, 30,
	    31, 31, 30, 31, 30, 31 };

	private static String days[];
	private int baseline;
	private Rectangle boxes[];
	private int boxWidth;
	private int boxHeight;
	private final CalendarComboBoxModel model;
	private Calendar selection;
	private int highlightedBox;

	public DayOfMonthGrid(CalendarComboBoxModel theModel, Date theDate,
	    Font font) {
		baseline = -1;
		boxes = null;
		boxWidth = -1;
		boxHeight = -1;

		highlightedBox = -1;
		this.model = theModel;
		setFont(font == null ? new Font("SansSerif", 0, 12) : font);
		setDate(theDate);
		setBorder(BorderFactory.createLoweredBevelBorder());
		if (days == null) {
			findDays();
		}
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					handleClick(e);
				}
				else if (e.getClickCount() == 2) {
					handleClick(e);
					Component parent;
					for (parent = getParent(); !(parent instanceof ComponentComboPopup); parent = parent
					    .getParent()) {
						// do nothing
					}
					parent.setVisible(false);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				highlightedBox = -1;
				repaint();
			}

		});
		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent mouseevent) {
				return;
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				p.translate(4, 0);
				if (boxes == null) {
					return;
				}
				for (int i = 0; i < boxes.length; i++) {
					if (boxes[i].contains(p)) {
						highlightedBox = i;
						repaint();
						return;
					}
					highlightedBox = -1;
					repaint();
				}

			}

		});
	}

	public static int getDaysInMonth(Calendar cal) {
		boolean leapYear = false;
		if (cal instanceof GregorianCalendar) {
			GregorianCalendar gc = (GregorianCalendar) cal;
			leapYear = gc.isLeapYear(cal.get(1));
		}
		int month = cal.get(2);
		if (month == 1 && leapYear) {
			return USUAL_DAYS_IN_MONTH[month] + 1;
		}
		return USUAL_DAYS_IN_MONTH[month];
	}

	public Date getDate() {
		return selection.getTime();
	}

	public int getDay() {
		return selection.get(5);
	}

	public void handleClick(MouseEvent e) {
		Point p = e.getPoint();
		p.translate(4, 0);
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i].contains(p)) {
				Calendar newSelection = model.getSelection();
				newSelection.set(5, i + 1);
				model.setSelection(newSelection);
				setDate(model.getSelection().getTime());
				return;
			}
		}

	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(getForeground());
		paintHeader(g);
		paintDays(g);
	}

	public void setDate(Date date) {
		selection = model.getSelection();
		selection.get(1);
		selection.setTime(date != null ? date : new Date());
		boxes = null;
		if (isShowing()) {
			repaint();
		}
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		findMetrics();
		if (isShowing()) {
			repaint();
		}
	}

	private void findBoxes() {
		int xOffset = 8;
		Vector<Rectangle> boxesVector = new Vector<Rectangle>();
		int numDays = getDaysInMonth(selection);
		int row = 0;
		Date date = selection.getTime();
		date.setDate(1);
		int column = date.getDay() - 1;
		for (int i = 1; i <= numDays; i++) {
			if (++column > 6) {
				column = 0;
				row++;
			}
			boxesVector.addElement(new Rectangle(xOffset + column * boxWidth, 4
			    + boxHeight + row * boxHeight, boxWidth, boxHeight));
		}

		boxes = new Rectangle[boxesVector.size()];
		boxesVector.copyInto(boxes);
	}

	private void findDays() {
		DateFormatSymbols d = new DateFormatSymbols();
		String weekDays[] = d.getWeekdays();
		days = new String[weekDays.length - 1];
		for (int i = 1; i < weekDays.length; i++) {
			days[i - 1] = weekDays[i].substring(0, 1);
		}

	}

	private void findMetrics() {
		FontMetrics fm = getFontMetrics(getFont());
		boxWidth = fm.stringWidth("WW") + 2;
		boxHeight = fm.getHeight() + 2;
		baseline = fm.getAscent();
		setPreferredSize(new Dimension(boxWidth * 7 + 8, boxHeight * 7 + 8));
	}

	private void paintDays(Graphics g) {
		if (boxes == null) {
			findBoxes();
		}
		int selected = selection.get(5);
		for (int i = 0; i < boxes.length; i++) {
			Rectangle box = boxes[i];
			if (selected == i + 1) {
				FontMetrics fm = getFontMetrics(getFont());
				int strWidth = fm.stringWidth(Integer.toString(i + 1));
				g.setColor(UIManager.getColor("textHighlight"));
				g.fillRect(box.x - 2, box.y - 2, strWidth + 2 + 2, baseline + 2 + 2);
				g.setColor(UIManager.getColor("textHighlightText"));
				g.drawString(Integer.toString(i + 1), box.x, box.y + baseline);
				g.setColor(getForeground());
				g.drawRect(box.x - 2, box.y - 2, strWidth + 2 + 2, baseline + 2 + 2);
			}
			else if (highlightedBox == i) {
				FontMetrics fm = getFontMetrics(getFont());
				int strWidth = fm.stringWidth(Integer.toString(i + 1));
				Color highlightColor = UIManager.getColor("textHighlightText");
				Color lightHighlightColor = new Color(
				    highlightColor.getRed() + 100 > 255 ? highlightColor.getRed() - 100
				        : highlightColor.getRed() + 100,
				    highlightColor.getGreen() + 100 > 255 ? highlightColor.getGreen() - 100
				        : highlightColor.getGreen() + 100,
				    highlightColor.getBlue() + 100 > 255 ? highlightColor.getBlue() - 100
				        : highlightColor.getBlue() + 100);
				g.setColor(getForeground());
				g.drawString(Integer.toString(i + 1), box.x, box.y + baseline);
				g.setColor(lightHighlightColor);
				g.drawRect(box.x - 2, box.y - 2, strWidth + 2 + 2, baseline + 2 + 2);
				g.setColor(getForeground());
			}
			else {
				g.drawString(Integer.toString(i + 1), box.x, box.y + baseline);
			}
		}

	}

	private void paintHeader(Graphics g) {
		int x = 8;
		int y = 2 + baseline;
		for (int i = 0; i < 7; i++) {
			g.drawString(days[i], x, y);
			x += boxWidth;
		}
		g.drawLine(2, 2 + boxHeight, getPreferredSize().width - 2 - 1,
		    2 + boxHeight);
	}

}
