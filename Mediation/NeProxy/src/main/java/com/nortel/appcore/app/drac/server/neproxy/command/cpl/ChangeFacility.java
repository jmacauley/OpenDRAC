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

package com.nortel.appcore.app.drac.server.neproxy.command.cpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;

public final class ChangeFacility extends AbstractCommandlet {
	private static final Set<String> PARAMETER_LIST = new HashSet<String>();
	public static final Map<String, String> XML_ATTRIBUTES = new HashMap<String, String>();

	public ChangeFacility(Map<String, Object> param) {
		super(param);
		PARAMETER_LIST.addAll(AbstractFacilityXml.COMMON_PARAMETER_LIST);

		XML_ATTRIBUTES.putAll(AbstractFacilityXml.COMMON_XML_ATTRIBUTES);
	}

	@Override
	public boolean start() {
		boolean rc = false;
		
		String targetNeId = (String) getParameters().get(ClientMessageXml.NEID_KEY);
		NetworkElement ne = (NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(targetNeId);
		Iterator<String> ir = PARAMETER_LIST.iterator();
		String aid = (String) getParameters().get(ClientMessageXml.AID_KEY);
		// HashMap modifyNeAttribute = new HashMap();
		Map<String, String> modifyDBAttribute = new HashMap<String, String>();
		while (ir.hasNext()) {
			String aParamKey = ir.next();
			String aParamValue = (String) getParameters().get(aParamKey);
			/*
			 * Determine the owner of the atrribute(s) (i.e it can be controlled by
			 * the NE or by DRAC. It it's the latter case, then just modify the value
			 * in the DB only.
			 */
			if (aParamValue != null) {
				modifyDBAttribute.put(XML_ATTRIBUTES.get(aParamKey), aParamValue);
			}
		}

		if (modifyDBAttribute != null) {
			try {
				log.debug("CPL ChangeFacility: Modify the Facility attribute in DB");
				DbUtility.INSTANCE.updateAFacility(ne, modifyDBAttribute, aid);
				ne.upDateLocalInfo();
				Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
				dbchgEvent.setReportType("facility");
				Map<String, String> data = new HashMap<String, String>();
				data.put("aid", aid);
				data.putAll(modifyDBAttribute);
				data.put("operation", "modify");
				dbchgEvent.addDataElement(data);

				NeProxy.generateEvent(dbchgEvent, ClientMessageXml.DBCHG_EVENT_VALUE);

				DbUtility.INSTANCE.generateFacilityUpdatedLog(ne, aid, data);
				rc = true;
			}
			catch (Exception e) {
				log.error("CPL ChangeFacility: Failed in modify facility DB", e);
				getCandidate().setErrorCode("ERR_SIOE");
			}
		}

		return rc;
	}

}