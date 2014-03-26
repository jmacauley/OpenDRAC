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
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BoundedTextField extends JTextField {
	private static final long serialVersionUID = 4346979872828745292L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private static final int EVENT_KEY_TYPED = 0;
	private static final int EVENT_KEY_PRESSED = 1;
	private static final int EVENT_KEY_RELEASED = 2;
	private int maxFieldSize = 1;
	private int maxValue = 9;
	private EmptyBorder border = new EmptyBorder(0, 0, 0, 0);
	private BoundedFieldKeyEventHandlerI keyHandler = null;
	private Font baseFont = new Font("SanSerif", Font.PLAIN, 10);

	public BoundedTextField(int maxFieldSize, int maxValue,
	    BoundedFieldKeyEventHandlerI keyHandler) {
		super(maxFieldSize);
		this.keyHandler = keyHandler;
		this.maxFieldSize = maxFieldSize;
		this.maxValue = maxValue;
		init();
	}

	private void init() {
		setFont(baseFont);
		setBorder(border);
		setMargin(new Insets(0, 0, 0, 0));
		setHorizontalAlignment(SwingConstants.CENTER);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ke) {
				
				if (Character.isDigit(ke.getKeyChar())) {
					if (getSelectedText() != null) {
						log.debug("getSelectedText() length: " + getSelectedText().length()
						    + " text length: " + getText().length());
					}
					if (getSelectedText() == null
					    || !(getSelectedText().length() == getText().length())) {
						if (!"".equals(getText())
						    && Integer.parseInt(getText() + ke.getKeyChar()) > maxValue) {
							
							ke.consume();
						}
					}
					keyHandler.handleBTFKeyEvent(EVENT_KEY_PRESSED);
				}
			}

			@Override
			public void keyReleased(KeyEvent ke) {
				keyHandler.handleBTFKeyEvent(EVENT_KEY_RELEASED);
			}

			@Override
			public void keyTyped(KeyEvent ke) {
				
				log.debug("Text is: " + getText() + " length is: " + getText().length()
				    + " maxFieldSize is: " + maxFieldSize);
				if (!Character.isDigit(ke.getKeyChar())
				    && !(ke.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
					ke.consume();
				}
				else if (getSelectedText() != null
				    && !(getSelectedText().length() == getText().length())
				    && getText().length() + 1 > maxFieldSize) {
					
					ke.consume();
				}
				else if (getSelectedText() == null
				    && getText().length() + 1 > maxFieldSize) {
					
					ke.consume();
				}
				else if (getSelectedText() != null
				    && !(getSelectedText().length() == getText().length())
				    && !"".equals(getText())
				    && Integer.parseInt(getText() + ke.getKeyChar()) > maxValue) {
					
					ke.consume();
				}
				else if (getSelectedText() == null
				    && Integer.parseInt(getText() + ke.getKeyChar()) > maxValue) {
					
					ke.consume();
				}
				keyHandler.handleBTFKeyEvent(EVENT_KEY_TYPED);
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				selectAll();
			}
		});
	}
}