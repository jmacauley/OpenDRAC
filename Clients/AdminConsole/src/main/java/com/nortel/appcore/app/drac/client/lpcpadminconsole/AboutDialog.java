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

package com.nortel.appcore.app.drac.client.lpcpadminconsole;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AboutDialog {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	private final JFrame parent;

	public AboutDialog(JFrame parentFrame) {
		parent = parentFrame;
	}

	public static void main(String[] args) {
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		AboutDialog ab = new AboutDialog(jf);

		jf.pack();
		jf.setVisible(true);
		ab.showAbout();
	}

	public void showAbout() {
		final JDialog aboutDialog = new JDialog(parent, "About");
		aboutDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JPanel mainPanel = new JPanel(new BorderLayout(1, 1));
		JTextPane textArea = new JTextPane();
		textArea.setEditable(false);

		String res = "/META-INF/NOTICE.txt";
		try {
			
			textArea.setPage(getClass().getResource(res));
		}
		catch (IOException e) {
			log.error("Unable to load resource '" + res + "' into about pannel.", e);
		}

		JScrollPane jsp = new JScrollPane(textArea);
		mainPanel.add(jsp, BorderLayout.CENTER);

		aboutDialog.getContentPane().setLayout(new BorderLayout(1, 1));
		aboutDialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
		aboutDialog.setTitle("About OpenDRAC");
		Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();
		aboutDialog.setLocation((screendim.width - 710) / 2,
		    (screendim.height - 415) / 2);
		aboutDialog.setSize(new Dimension(710, 415));
		aboutDialog.setVisible(true);
		aboutDialog.setResizable(true);
	}
}
