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

public class PP8600FacilityXML
// extends AbstractFacilityXML
{
	// public final static String NEKEY_PHYSADDR = "PHYSADDR";
	// public final static String NEKEY_RATE = "RATE";
	// public final static String NEKEY_SIGNALTYPE = "SIGNALTYPE";
	// public final static String NEKEY_AID = "AID";
	// public final static String NEKEY_PRISTATE = "PST";
	// public final static String NEKEY_DIRECTION = "ETHERDUPLEX";
	// public final static String NEKEY_MTU = "MTU";
	// public final static String NEKEY_IFSPEED = "IFSPEED";
	// public final static String NEKEY_VLANID = "VLANID";
	//
	// public final static String MEP_ATTR = "mep";
	// public static HashMap<String, String> commonNeToXmlMapping = null;
	// public static HashMap<String, String> ethNeToXmlMapping = null;
	//
	// public PP8600FacilityXML(Element root)
	// {
	// super(root);
	// // this.root = root;
	// initKeyMapping();
	// initStaticAttributes();
	// }
	//
	// public PP8600FacilityXML(AbstractNetworkElement ne)
	// {
	// super(ne);
	// initKeyMapping();
	// }
	//
	// public PP8600FacilityXML(String aFacility)
	// {
	// super(aFacility);
	// initKeyMapping();
	// initStaticAttributes();
	// }
	//
	// public static List<Attribute> getStaticAttributes()
	// {
	// List<Attribute> returnValue = new
	// ArrayList<Attribute>(AbstractFacilityXML.getStaticAttributes());
	//
	// returnValue.add(new Attribute(PP8600FacilityXML.MEP_ATTR, "0"));
	// return returnValue;
	// }
	//
	// private static void initKeyMapping()
	// {
	// if (commonNeToXmlMapping != null)
	// {
	// return;
	// }
	// commonNeToXmlMapping = new HashMap();
	// commonNeToXmlMapping.put(NEKEY_PRISTATE,
	// AbstractFacilityXML.PRIMARYSTATE_ATTR);
	//
	// ethNeToXmlMapping = new HashMap(commonNeToXmlMapping);
	// ethNeToXmlMapping.put(NEKEY_RATE, AbstractFacilityXML.SPEED_ATTR);
	// ethNeToXmlMapping.put(NEKEY_SIGNALTYPE,
	// AbstractFacilityXML.SIGNALTYPE_ATTR);
	// ethNeToXmlMapping.put(NEKEY_PHYSADDR, AbstractFacilityXML.PHYSADDR_ATTR);
	// }
	//
	// @Override
	// public void updateDataBase(AbstractNetworkElement ne, int layer)
	// throws Exception
	// {
	// if (getAddList().size() > 0)
	// {
	// DbUtility.INSTANCE.addNewFacility(ne, getAddList());
	// }
	// if (getUpdateList().size() > 0)
	// {
	// DbUtility.INSTANCE.updateFacility(ne, getUpdateList(), "layer2");
	// }
	// if (getNewAttributeList().size() > 0)
	// {
	// DbUtility.INSTANCE.updateFacilityAttr(ne, getNewAttributeList(),
	// "layer2");
	// }
	// }
	//
	// @Override
	// public void updateFacilityInstance(String identification, String aid, Map
	// attributes)
	// {
	// // Iterate through all facility instances in this XML document to look
	// // for the one that matches its AID with the one being updated
	// Iterator ir = getRoot().getChildren().iterator();
	// while (ir.hasNext())
	// {
	// Element aFacility = (Element) ir.next();
	// if (aFacility.getAttributeValue(AID_ATTR).equalsIgnoreCase(aid))
	// {
	// Iterator attr = attributes.keySet().iterator();
	// HashMap temp = null;
	// HashMap newAttrList = null;
	// while (attr.hasNext())
	// {
	// String key = (String) attr.next();
	// String value = (String) attributes.get(key);
	// if (value == null)
	// {
	// value = "N/A";
	// }
	//
	// // Try to get the existing one from the XML
	// String attributeXML = aFacility.getAttributeValue(key);
	// if (attributeXML != null)
	// {
	// if (!attributeXML.equalsIgnoreCase(value))
	// {
	// if (temp == null)
	// {
	// temp = new HashMap();
	// }
	// temp.put(key, value);
	// }
	// }
	// else
	// {
	// // Ne attribute from the NE but not in the XML DB yet. First
	// // get the XML key definition, if it's not defined then ignore
	// // this attribute
	// if (newAttrList == null)
	// {
	// newAttrList.put(key, value);
	// }
	// }
	// }
	// if (temp != null)
	// {
	// getUpdateList().add(new Object[]
	// { aid, temp });
	// }
	// if (newAttrList != null)
	// {
	// getNewAttributeList().add(new Object[]
	// { aid, newAttrList });
	// }
	// return;
	// }
	// }
	// 
	// String[] aidMap = aid.split("-");
	//
	// StringBuffer buff = new StringBuffer();
	// buff.append("<" + LAYER2_ELEMENT + " id=\"2\" aid=\"" + aid + "\" ");
	// buff.append("port=\"" + aidMap[3] + "\" ");
	// buff.append("shelf=\"" + aidMap[1] + "\" ");
	// buff.append("slot=\"" + aidMap[2] + "\" ");
	// buff.append("type=\"" + aidMap[0] + "\" ");
	//
	// Iterator ethIr = attributes.keySet().iterator();
	// while (ethIr.hasNext())
	// {
	// String key = (String) ethIr.next();
	// buff.append(key + "=\"" + attributes.get(key) + "\" ");
	// }
	//
	// List<Attribute> staticAttr = getStaticAttributes();
	// for (int i = 0; i < staticAttr.size(); i++)
	// {
	// Attribute attr = staticAttr.get(i);
	// buff.append(attr.getName() + "=\"" + attr.getValue() + "\" ");
	// }
	// buff.append(" />");
	// getAddList().add(buff.toString());
	// }
	//
	// private void initStaticAttributes()
	// {
	// List<Attribute> staticAttr = PP8600FacilityXML.getStaticAttributes();
	// for (int i = 0; i < staticAttr.size(); i++)
	// {
	// Attribute anAttr = staticAttr.get(i);
	// if (getRoot().getAttributeValue(anAttr.getName()) == null)
	// {
	// getRoot().setAttribute(anAttr.getName(), anAttr.getValue());
	// }
	// }
	// }

}
