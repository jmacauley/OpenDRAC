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

package com.nortel.appcore.app.drac.server.neproxy.command;

import java.util.Map;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * Created on Sep 20, 2005
 * 
 * @author nguyentd
 */
public final class DeleteConnection extends AbstractCommandlet {
	private final String targetNeId;
	private static final String EVENT_CODE = "DLT-CRS";

	public DeleteConnection(Map<String, Object> param) {
		super(param);

		CrossConnection xcon = (CrossConnection) param
		    .get(ClientMessageXml.CROSSCONNECTION_KEY);
		targetNeId = xcon.getSourceNeId();
	}

	@Override
	public boolean start() {
		

		AbstractNe ne = DiscoverNePool.INSTANCE.getNeByTidOrIdOrIpandPort(
		    targetNeId);
		if (ne == null) {
			log.error("DeleteConnection failed can find NE " + targetNeId);
			getCandidate().setErrorCode("ERR_IUID");
			return false;
		}

		if (ne.getState().ordinal() < NeStatus.NE_ALIGNED.ordinal()) {
			log.error("DeleteConnection failed, Ne not aligned");
			getCandidate().setErrorCode("ERR_INRY");
			return false;
		}

		Map<String, Map<String, String>> eventHandlerList = ne
		    .getNeEventHandlingDefinition().get(ne.getNeType());
		Map<String, String> aHandler = eventHandlerList.get(EVENT_CODE);

		if (aHandler == null) {
			log.error("DeleteConnection no handler for " + EVENT_CODE);
			getCandidate().setErrorCode("ERR_SIOE");
			return false;
		}

		try {
			String className = aHandler.get(NePoxyDefinitionsParser.CLASS_ATTR);
			log.debug("DeleteConnection invoking commandlet  " + className + " for "
			    + EVENT_CODE);

			AbstractCommandlet command = AbstractCommandlet.getCommandlet(className,
			    getParameters());
			if (command.start()) {
				log.debug("DeleteConnection commandlet returned true");
				return true;
			}
			log.error("DeleteConnection failed, commandlet returned false");
			getCandidate().setErrorCode("ERR_SIOE");
			return false;
		}
		catch (Exception e) {
			log.error("DeleteConnection Failed to invoke handler", e);
		}

		log.error("DeleteConnection returning false ");
		getCandidate().setErrorCode("ERR_SIOE");
		return false;
	}
}
