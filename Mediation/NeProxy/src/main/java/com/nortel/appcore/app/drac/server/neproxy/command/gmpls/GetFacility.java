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

package com.nortel.appcore.app.drac.server.neproxy.command.gmpls;

public class GetFacility
// extends AbstractCommandlet
{
	// public GetFacility(Map<String, Object> param)
	// {
	// super(param);
	// }
	//
	// @Override
	// public boolean start()
	// {
	// try
	// {
	// GmplsNetworkElement gmplsNe = (GmplsNetworkElement)
	// getParameters().get(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
	// getParameters().remove(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
	// GmplsFacilityXML facilityXML = null;
	// String currentData = DbUtility.INSTANCE.retrieveNeFacility(gmplsNe);
	// if (currentData.length() > 0)
	// {
	// ByteArrayInputStream tempData = new
	// ByteArrayInputStream(currentData.getBytes());
	// SAXBuilder builder = new SAXBuilder();
	// try
	// {
	// Document aDoc = builder.build(tempData);
	//
	// facilityXML = new GmplsFacilityXML(aDoc.getRootElement());
	//
	// // in case the database is old, just update it with the latest
	// facilityXML.populateStaticAttributes();
	// }
	// catch (JDOMParseException je)
	// {
	// return false;
	// }
	// }
	// else
	// {
	// facilityXML = new GmplsFacilityXML(gmplsNe);
	// }
	// Iterator ir = getParameters().keySet().iterator();
	// while (ir.hasNext())
	// {
	// String aid = (String) ir.next();
	// HashMap aFacility = (HashMap) getParameters().get(aid);
	// HashMap attributeList = new HashMap();
	// attributeList.put(GmplsFacilityXML.PRIMARYSTATE_ATTR, "IS");
	// attributeList.put(GmplsFacilityXML.RATE_ATTR,
	// aFacility.get(GmplsFacilityXML.NEKEY_RATE));
	// attributeList.put(GmplsFacilityXML.SIGNALTYPE_ATTR,
	// aFacility.get(GmplsFacilityXML.NEKEY_SIGNALTYPE));
	// attributeList.put(GmplsFacilityXML.INGRESSIP_ATTR,
	// aFacility.get(GmplsFacilityXML.NEKEY_INGRESSIP));
	//
	// attributeList.put(GmplsFacilityXML.VALID_ATTR, "true");
	// 
	// facilityXML.updateFacilityInstance(gmplsNe.getNeId(), aid, attributeList);
	// }
	// facilityXML.updateDataBase(gmplsNe, 0);
	// }
	// catch (Exception e)
	// {
	// }
	// return true;
	// }
}
