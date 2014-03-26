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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;

public final class PostRemoveConnections extends AbstractCommandlet {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	private Force10NetworkElement ne;
	private final List<CrossConnection> xconList;

	@SuppressWarnings("unchecked")
  public PostRemoveConnections(Map<String, Object> param) {
		super(param);
		xconList = (List<CrossConnection>) getParameters().get(
		    ClientMessageXml.CROSSCONNECTION_LIST_KEY);
	}

	@Override
	public boolean start() throws Exception {
		try {
			log.debug("Force10 PostRemoveConnections Processing "
			    + getParameters().toString());

			if (xconList == null || xconList.isEmpty()) {
				
				return true;
			}

			String neid = xconList.get(0).getSourceNeId();
			ne = (Force10NetworkElement) DiscoverNePool.INSTANCE
			    .getNeByTidOrIdOrIpandPort(neid);

			if (ne == null) {
				log.error("Force10 PostRemoveConnections failed cannot find NE " + neid);
				getCandidate().setErrorCode("ERR_IUID");
				return false;
			}

			return true;
		}
		catch (Exception e) {
			log.error("Error: ", e);
			throw e;
		}
	}
}