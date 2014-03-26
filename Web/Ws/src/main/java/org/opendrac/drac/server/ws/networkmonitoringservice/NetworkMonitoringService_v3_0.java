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

package org.opendrac.drac.server.ws.networkmonitoringservice;

import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.NETWORK_MONITORING_SERVICE_V3_0_NS;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.RES_ALLOC_AND_SCHEDULING_SERVICE;

import org.apache.axis2.context.MessageContext;
import org.opendrac.drac.server.ws.common.CommonServiceOperationsV3;

import com.nortel.appcore.app.drac.server.ws.common.UserDetailsCache.TokenHolder;
import com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.NetworkMonitoringService;

public class NetworkMonitoringService_v3_0 extends NetworkMonitoringService {
	public NetworkMonitoringService_v3_0() {
		helper.setNamespace(NETWORK_MONITORING_SERVICE_V3_0_NS);
		namespace = NETWORK_MONITORING_SERVICE_V3_0_NS;
	}

	@Override
	protected TokenHolder authorize() throws Exception {
		return new CommonServiceOperationsV3().authorize(
		    RES_ALLOC_AND_SCHEDULING_SERVICE, serviceUtil, userDetailsCache, rh,
		    MessageContext.getCurrentMessageContext());
	}

	@Override
	protected void deauthorize(TokenHolder tokenHolder) {
		if (tokenHolder != null) {
			new CommonServiceOperationsV3().deauthorize(tokenHolder,
			    userDetailsCache, serviceUtil, rh);
		}
	}
}
