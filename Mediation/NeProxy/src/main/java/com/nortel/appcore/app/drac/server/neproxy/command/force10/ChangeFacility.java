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

import java.util.HashMap;
import java.util.Map;

import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;

public final class ChangeFacility extends AbstractCommandlet {
	private final Force10NetworkElement ne;

	public ChangeFacility(Map<String, Object> param) {
		super(param);
		String targetNeId = (String) getParameters().get(ClientMessageXml.NEID_KEY);
		ne = (Force10NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(targetNeId);
	}

	@Override
	public boolean start() throws Exception {
		try {
			log.debug("Force10 ChangeFacility Processing "
			    + getParameters().toString() + " for " + ne.toString());
			/**
			 * From the calling class, we can get passed the following and are
			 * expected to update and save these attributes .
			 * 
			 * <pre>
			 * Map&lt;String, Object&gt; data = new HashMap&lt;String, Object&gt;();
			 * data.put(ClientMessageXML.NEID_KEY, neid);
			 * data.put(ClientMessageXML.AID_KEY, aid);
			 * data.put(ClientMessageXML.TNA_KEY, tna);
			 * data.put(ClientMessageXML.FACLABEL_KEY, faclabel);
			 * if (mtu != null &amp;&amp; !&quot;N/A&quot;.equalsIgnoreCase(mtu)) { // Proxy will try to update
			 * 	                                                 // the NE even if the data is
			 * 	                                                 // invalid
			 * 	data.put(ClientMessageXML.MTU_KEY, mtu);
			 * }
			 * data.put(ClientMessageXML.SRLG_KEY, srlg);
			 * data.put(ClientMessageXML.GROUP_KEY, grp);
			 * data.put(ClientMessageXML.COST_KEY, cost);
			 * data.put(ClientMessageXML.METRIC_KEY, metric2);
			 * data.put(ClientMessageXML.SIGNALINGTYPE_KEY, sigType);
			 * data.put(ClientMessageXML.CONSTRAINT_KEY, constraints);
			 * data.put(ClientMessageXML.DOMAIN_KEY, domainId);
			 * data.put(ClientMessageXML.SITE_KEY, siteId);
			 */
			final String aid = (String) getParameters().get(ClientMessageXml.AID_KEY);

			Map<String, String> modifyDBAttribute = new HashMap<String, String>();

			/*
			 * Argh, the map of parameters given to this commandlet does not use the
			 * same keys we use to store facilities against, walk through the
			 * attributes we know we can set, extract them if present and map to the
			 * correct key/values.
			 */
			for (String s : AbstractFacilityXml.COMMON_PARAMETER_LIST) {
				String aParamValue = (String) getParameters().get(s);
				if (aParamValue != null) {
					modifyDBAttribute.put(
					    AbstractFacilityXml.COMMON_XML_ATTRIBUTES.get(s), aParamValue);
				}
			}

			log.debug("Force10 ChangeFacility: Modify the Facility attribute in DB "
			    + modifyDBAttribute);
			DbUtility.INSTANCE.updateAFacility(ne, modifyDBAttribute, aid);

			// why ?
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
			return true;
		}
		catch (Exception e) {
			Exception e1 = new Exception(
			    "Force10 ChangeFacility: Failed to update facility "
			        + getParameters().toString(), e);
			log.error("Error: ", e1);
			throw e1;
		}
	}
}
