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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

/**
 * TODO: Note: This should only be used until we move to J2SE 6.0. J2SE 6.0 will
 * allow close buttons to be added to JTabbedPane tabs.
 */

public final class TabCloseIcon implements Icon {
	private final Icon mIcon;
	private JTabbedPane mTabbedPane = null;
	private transient Rectangle mPosition = null;

	/**
	 * Creates a new instance of TabCloseIcon.
	 */
	public TabCloseIcon() {
		this(new ImageIcon(TabCloseIcon.class.getResource("icons/closeTab.gif")));
	}

	/**
	 * Creates a new instance of TabCloseIcon.
	 */
	public TabCloseIcon(Icon icon) {
		mIcon = icon;
	}

	/**
	 * just delegate
	 */
	@Override
	public int getIconHeight() {
		return mIcon.getIconHeight();
	}

	/**
	 * just delegate
	 */
	@Override
	public int getIconWidth() {
		return mIcon.getIconWidth();
	}

	/**
	 * when painting, remember last position painted.
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (null == mTabbedPane) {
			mTabbedPane = (JTabbedPane) c;
			mTabbedPane.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					// asking for isConsumed is *very* important, otherwise more than one
					// tab might get
					// closed!
					if (!e.isConsumed() && mPosition.contains(e.getX(), e.getY())) {
						final int index = mTabbedPane.getSelectedIndex();
						mTabbedPane.remove(index);
						e.consume();
					}
				}
			});
		}

		mPosition = new Rectangle(x, y, getIconWidth(), getIconHeight());
		mIcon.paintIcon(c, g, x, y);
	}

}
