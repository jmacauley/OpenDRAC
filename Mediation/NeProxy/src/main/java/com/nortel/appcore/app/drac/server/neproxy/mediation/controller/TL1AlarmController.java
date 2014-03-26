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

package com.nortel.appcore.app.drac.server.neproxy.mediation.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.TL1LanguageEngine;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.Report;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.ReportListener;

/**
 * TL1AlarmController Handles registration and deregistration of NetworkElement
 * events.
 * 
 * @Since 2005-11-28
 */
public final class TL1AlarmController implements ReportListener {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final NetworkElementInfo neInfo;
	private final List<NEAOListener> registerListeners = new ArrayList<NEAOListener>();
	private final TL1LanguageEngine tl1Engine;
	private TL1LineParser parser;
	// controller
	// private static final List unwantedAOs = null;
	private static final char SPACE = ' ';
	private static final char HYPEN = '-';

	public TL1AlarmController(NetworkElementInfo ne, TL1LanguageEngine engine,
	    TL1LineParser tparser) {
		neInfo = ne;
		tl1Engine = engine;
		parser = tparser;
	}

	public void addListener(NEAOListener listener) {
		registerListeners.add(listener);
	}

	public void establish() {
		tl1Engine.addAutonomousListenerForAll(neInfo.getNetworkElementName(), this);
	}

	// public TL1LineParser getParser()
	// {
	// return parser;
	// }

	@Override
	public void received(Report report) {
		if (!registerListeners.isEmpty()) {
			String code = report.getOutputCode();
			code = code.replace(SPACE, HYPEN);
			TL1AlarmEvent event = new TL1AlarmEvent(neInfo);
			event.setCommandCode(Tl1CommandCode.fromString(code));
			event.setCtag(Integer.parseInt(report.getCorrelationTag()));

			event.setTextBlocks(report.getTextBlock());

			/*
			 * move to NEEventManagerImpl to handle unwanted AOs
			 */

			// String[] temp = event.getTextBlocks();
			// for (String element : temp)
			// {
			try {
				parser.parse(event);
			}
			catch (Exception e) {
				log.error("Unable to parse event " + event.toString(), e);
			}
			// }

			if (code.indexOf("ALM") >= 0) {
				event.setIsAlarm(true);
			}
			else {
				event.setIsAlarm(false);
			}

			for (NEAOListener lis : registerListeners) {
				lis.receiveEvent(event);
			}
		}

	}

	public void reloadParser(TL1LineParser tparser) {
		parser = tparser;
	}

	// public void removeListener(NEAOListener listener)
	// {
	// registerListeners.remove(listener);
	// }

	public void terminateController() {
		registerListeners.clear();
	}

}
