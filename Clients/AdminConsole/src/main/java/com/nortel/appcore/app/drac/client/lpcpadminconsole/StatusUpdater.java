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

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * 
 */
public final class StatusUpdater implements Runnable {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final JButton button;
	private final Color colour;
	private int timeout = 4000;

	public StatusUpdater(JButton buttonToFlash, Color flashColour) {
		button = buttonToFlash;
		colour = flashColour;
	}

	@Override
	public void run() {
		try {
			while (timeout > 0) {
				button.setBackground(colour);
				Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
				button.setBackground(null);
				Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
				timeout -= 1000;
			}
			button.setBackground(colour);
			Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
			button.setBackground(null);
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
	}
}
