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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class PrepAddConnections extends AbstractCommandlet {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	private String targetNeId = null;
	private static final String EVENT_CODE = "PREP-ADD-CONNECTIONS";

	public PrepAddConnections(Map<String, Object> param) {
		super(param);

		/*
		 * The AbstractCommandlet class carries a result class called Candidate. It
		 * will instantiate a new one if one is not provided via the input
		 * parameters map. L2SS activation relies on passing info back to the caller
		 * via the results class. In order to pass results up from the bottom, we
		 * need to ensure that only one 'Candidate' class is used by the related
		 * commandlets by putting this Candidate into the parameters map (so that a
		 * new one will not get created when the specific commandlet is created).
		 */
		param.put(NePoxyDefinitionsParser.CANDIDATE_KEY, this.getCandidate());

		@SuppressWarnings("unchecked")
    List<CrossConnection> xconList = (List<CrossConnection>) param
		    .get(ClientMessageXml.CROSSCONNECTION_LIST_KEY);
		if (xconList.size() > 0) {
			targetNeId = xconList.get(0).getSourceNeId();
		}
	}

	@Override
	public boolean start() {
		

		AbstractNe ne = DiscoverNePool.INSTANCE.getNeByTidOrIdOrIpandPort(
		    targetNeId);
		if (ne == null) {
			
			getCandidate().setErrorCode("ERR_IUID");
			return false;
		}

		if (ne.getState().ordinal() < NeStatus.NE_ALIGNED.ordinal()) {
			
			getCandidate().setErrorCode("ERR_INRY");
			return false;
		}

		Map<String, Map<String, String>> eventHandlerList = ne
		    .getNeEventHandlingDefinition().get(ne.getNeType());
		Map<String, String> aHandler = eventHandlerList.get(EVENT_CODE);
		if (aHandler == null) {
			
			getCandidate().setErrorCode("ERR_SIOE");
			return false;
		}

		try {
			String className = aHandler.get(NePoxyDefinitionsParser.CLASS_ATTR);
			log.debug("PrepAddConnections invoking commandlet " + className + " for "
			    + EVENT_CODE);
			AbstractCommandlet command = AbstractCommandlet.getCommandlet(className,
			    getParameters());
			if (command.start()) {
				
				return true;
			}
			
			getCandidate().setErrorCode("ERR_SIOE");
			return false;
		}
		catch (Exception e) {
			log.error("PrepAddConnections: Failed to invoke handler", e);
		}

		
		getCandidate().setErrorCode("ERR_SIOE");
		return false;
	}
}
