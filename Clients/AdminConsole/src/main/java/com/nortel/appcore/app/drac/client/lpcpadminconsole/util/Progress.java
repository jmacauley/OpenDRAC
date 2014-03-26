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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import com.google.common.util.concurrent.Uninterruptibles;

public final class Progress {
	public interface CancelListener {
		void cancel();
	}

	private static final Dimension PROGRESS_SIZE = new Dimension(100, 25);
	private final JProgressBar ipb;
	private final CancelListener listener;
	private final JLabel progressLabel;
	private final JOptionPane optionPane;
	private final JDialog dialog;
	private final int totalSize;
	private int completed;
	private final String title;
	private final String elementType;

	public Progress(String elementType, CancelListener listener, int totalSize,
	    Frame frame, int nonCancelableSize) {
		this.elementType = elementType;
		this.listener = listener;
		this.totalSize = totalSize;
		final boolean allowCancel = true;
		JPanel panel = new JPanel();
		ipb = new JProgressBar(0, totalSize);
		ipb.setValue(0);
		ipb.setStringPainted(true);
		ipb.setPreferredSize(PROGRESS_SIZE);
		panel.setLayout(new BorderLayout());
		progressLabel = new JLabel();
		panel.add(progressLabel, BorderLayout.NORTH);
		panel.add(ipb, BorderLayout.CENTER);
		dialog = new JDialog(frame, true);
		title = "Operation Progress";
		dialog.setTitle(title);
		setStatus(false, false);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				cancel();
			}
		});

		Object options[];

		if (allowCancel) {
			options = new Object[] { cancelButton };
		}
		else {
			options = new Object[] {};
		}

		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE,
		    JOptionPane.DEFAULT_OPTION, null, options);

		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();

				if (dialog.isVisible()
				    && e.getSource() == optionPane
				    && (prop.equals(JOptionPane.VALUE_PROPERTY) || prop
				        .equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
					Object value = optionPane.getValue();

					if (value == JOptionPane.UNINITIALIZED_VALUE) {
						return;
					}

					optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
					cancel();
				}
			}
		});

		dialog.setContentPane(optionPane);

		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				if (allowCancel) {
					optionPane.setValue(Integer.valueOf(JOptionPane.CLOSED_OPTION));
				}
			}
		});

		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.pack();

		if (frame != null && frame.isVisible()) {
			dialog.setLocationRelativeTo(frame);
		}
		else {
			Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();
			dialog.setLocation((screendim.width - 200) / 2,
			    (screendim.height - 50) / 2);
		}
	}

	public void cancel() {
		if (listener != null) {
			listener.cancel();
		}

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ipb.setString("Cancelation requested. Please wait...");
			}
		});

		dialog.dispose();

		JOptionPane
		    .showMessageDialog(
		        null, // Pass in DRAC Desktop
		        "Cancelation has been requested.\nTasks which have already been dispatched\nwill run to completion.",
		        "Warning", JOptionPane.WARNING_MESSAGE);
	}

	public void done(final boolean cancelled) {
		setStatus(true, cancelled);

		if (!cancelled) {
			// Avoid flashing
		  Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
			if (dialog != null) {
				dialog.dispose();
			}
		}
	}

	public void show() {
		dialog.setVisible(true);
	}

	public void update(List<String> list) {
		completed += list.size();
		setStatus(false, false);
		ipb.setValue(completed);
	}

	private String getProgress() {
		return completed + " of " + totalSize + " "
		    + (elementType != null ? elementType : "requests") + " completed.";
	}

	private void setStatus(final boolean done, final boolean cancelled) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressLabel.setText(getProgress());
			}
		});
	}
}
