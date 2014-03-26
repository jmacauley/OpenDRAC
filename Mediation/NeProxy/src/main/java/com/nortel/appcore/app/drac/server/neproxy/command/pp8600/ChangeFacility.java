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

package com.nortel.appcore.app.drac.server.neproxy.command.pp8600;

public class ChangeFacility
// extends AbstractCommandlet
{
	// private static final HashSet<String> parameterList = new HashSet<String>();
	// public static final HashMap<String, String> neAttributes = new
	// HashMap<String, String>();
	// public static final HashMap<String, String> xmlAttributes = new
	// HashMap<String, String>();
	//
	// public ChangeFacility(Map<String, Object> param)
	// {
	// super(param);
	// parameterList.add(ClientMessageXML.AUTONEGOTIATION_KEY);
	// parameterList.add(ClientMessageXML.MTU_KEY);
	// parameterList.add(ClientMessageXML.TNA_KEY);
	// parameterList.add(ClientMessageXML.COST_KEY);
	// parameterList.add(ClientMessageXML.SIGNALINGTYPE_KEY);
	// parameterList.add(ClientMessageXML.METRIC_KEY);
	// parameterList.add(ClientMessageXML.SRLG_KEY);
	// parameterList.add(ClientMessageXML.CONSTRAINT_KEY);
	// parameterList.add(ClientMessageXML.DOMAIN_KEY);
	// parameterList.add(ClientMessageXML.SITE_KEY);
	// parameterList.add(ClientMessageXML.VLANID_KEY);
	//
	// // neAttributes.put(ClientMessageXML.MTU_KEY, OmeFacilityXML.NEKEY_MTU);
	// // neAttributes.put(ClientMessageXML.AUTONEGOTIATION_KEY,
	// OmeFacilityXML.NEKEY_AN);
	//
	// xmlAttributes.put(ClientMessageXML.TNA_KEY, PP8600FacilityXML.TNA_ATTR);
	// xmlAttributes.put(ClientMessageXML.COST_KEY, PP8600FacilityXML.COST_ATTR);
	// xmlAttributes.put(ClientMessageXML.SIGNALINGTYPE_KEY,
	// PP8600FacilityXML.SIGNALTYPE_ATTR);
	// xmlAttributes.put(ClientMessageXML.METRIC_KEY,
	// PP8600FacilityXML.METRIC_ATTR);
	// xmlAttributes.put(ClientMessageXML.SRLG_KEY, PP8600FacilityXML.SRLG_ATTR);
	// xmlAttributes.put(ClientMessageXML.CONSTRAINT_KEY,
	// PP8600FacilityXML.CONSTRAIN_ATTR);
	//
	// xmlAttributes.put(ClientMessageXML.DOMAIN_KEY,
	// PP8600FacilityXML.DOMAIN_ATTR);
	// xmlAttributes.put(ClientMessageXML.SITE_KEY,
	// PP8600FacilityXML.SITEID_ATTR);
	// xmlAttributes.put(ClientMessageXML.VLANID_KEY,
	// PP8600FacilityXML.VLANID_ATTR);
	// }
	//
	// @Override
	// public boolean start()
	// {
	// boolean rc = false;
	// 
	// String targetNeId = (String)
	// getParameters().get(ClientMessageXML.NEID_KEY);
	// AbstractNe ne = DiscoverNePool.INSTANCE.getNeByTid(targetNeId);
	// Iterator ir = parameterList.iterator();
	// String aid = (String) getParameters().get(ClientMessageXML.AID_KEY);
	// HashMap modifyDBAttribute = new HashMap();
	// while (ir.hasNext())
	// {
	// String aParamKey = (String) ir.next();
	// String aParamValue = (String) getParameters().get(aParamKey);
	// // Determine the owner of the atrribute(s) (i.e it can be controlled
	// // by the NE or by DRAC. It it's the latter case, then just modify
	// // the value in the DB only.
	// if (aParamValue != null)
	// {
	// if (xmlAttributes.containsKey(aParamKey))
	// {
	// modifyDBAttribute.put(xmlAttributes.get(aParamKey), aParamValue);
	// }
	// }
	// }
	//
	// if (modifyDBAttribute != null)
	// {
	// try
	// {
	// // NOT SURE how to configure the layer string yet, so call it twice.
	// DbUtility.INSTANCE.updateAFacility(ne, modifyDBAttribute, aid,
	// "layer2");
	// ne.upDateLocalInfo();
	// Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
	// dbchgEvent.setReportType("facility");
	// HashMap data = new HashMap();
	// data.put("aid", aid);
	// data.putAll(modifyDBAttribute);
	// data.put("operation", "modify");
	// dbchgEvent.addDataElement(data);
	// NeProxy.broadcast(buildEventHeader(dbchgEvent,
	// ClientMessageXML.DBCHG_EVENT_VALUE));
	// DbUtility.INSTANCE.addNewEvent(dbchgEvent.eventNodeToString());
	// rc = true;
	// }
	// catch (Exception e)
	// {
	// getCandidate().setErrorCode("ERR_SIOE");
	// }
	// }
	//
	// return rc;
	// }
	//
	// private String buildEventHeader(Tl1XmlEvent anEvent, String eventType)
	// {
	// ClientMessageXML message = new ClientMessageXML();
	// message.addEventData(anEvent.getRootNode(), eventType);
	// return message.rootNodeToString();
	// }
}