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

package com.nortel.appcore.app.drac.server.neproxy.command.force10;

import java.util.Map;

import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class SetTerminal extends AbstractCommandlet {

	public SetTerminal(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() throws Exception {
		Force10NetworkElement ne = (Force10NetworkElement) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
		try {
			ne.sendCommandWaitForCommandPrompt("terminal length 0\r\n", 1000 * 20);
			return true;
		}
		catch (Exception e) {
			log.error("failed to set terminal " + ne.toDebugString(), e);
			return false;
		}
	}
}
