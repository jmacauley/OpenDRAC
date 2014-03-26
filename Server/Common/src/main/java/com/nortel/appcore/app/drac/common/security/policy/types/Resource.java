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

package com.nortel.appcore.app.drac.common.security.policy.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfileXML.ResourceType;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class Resource implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private List<String> referencingResourceGroupName;
	private String resourceID;
	private ResourceType resourceType;
	private String resourceElement = "endpoint"; // resource";
	public static final String RESOURCEID_ATTR = "resourceID";
	public static final String REFERENCINGRESOURCEGROUPNAME_ELEMENT = "referencingResourceGroupName";

	public Resource() {
		this(null, ResourceType.ENDPOINT);
	}

	public Resource(String resourceID) {
		this(resourceID, ResourceType.ENDPOINT);
	}

	protected Resource(String resourceID, ResourceType resourceType) {
		super();
		this.resourceID = resourceID;
		this.resourceType = resourceType;
		this.referencingResourceGroupName = new ArrayList<String>();
	}

	// public static String formResourceIDFromEndPoint(EndPointType endpoint)
	// {
	// return endpoint.getId();
	// }

	// public String buildReferencingResourceGroupNameList(Resource aResource)
	// {
	//
	// StringBuilder buf = new StringBuilder();
	//
	// if (this.referencingResourceGroupName != null)
	// {
	// for (String string : this.referencingResourceGroupName)
	// {
	// buf.append(XmlUtility.toXMLString(REFERENCINGRESOURCEGROUPNAME_ELEMENT,
	// XmlUtility
	// .convertStringToXMLLiterals(string)));
	// }
	// }
	// return buf.toString();
	//
	// }

	public void fromXML(Element root) throws Exception {

		if (root == null) {
			return;
		}

		String resourceID = root.getAttributeValue(RESOURCEID_ATTR);
		if (resourceID == null || resourceID.equals("")) {
			DracException ex = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { RESOURCEID_ATTR });
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		String resourceType = root.getAttributeValue(ResourceType.RESOURCE_TYPE);
		if (resourceType == null || resourceType.equals("")) {
			DracException ex = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { ResourceType.RESOURCE_TYPE });
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		this.resourceID = resourceID;
		this.resourceType = ResourceType.fromString(resourceType);

		List<Element> referenceList = root
		    .getChildren(REFERENCINGRESOURCEGROUPNAME_ELEMENT);
		if (referenceList != null) {
			Element element = null;
			for (Element element2 : referenceList) {
				element = element2;
				this.referencingResourceGroupName.add(XmlUtility
				    .convertXMLLiteralsToString(element.getText()));
			}
		}

	}

	public List<String> getReferencingResourceGroupName() {
		return referencingResourceGroupName;
	}

	public String getResourceElement() {
		return this.resourceElement;
	}

	public String getResourceID() {
		return resourceID;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setReferencingResourceGroupName(
	    List<String> referencingResourceGroupName) {
		this.referencingResourceGroupName = referencingResourceGroupName;
	}

	public void setResourceElement(String name) {
		this.resourceElement = name;
	}

	// public void setResourceID(String resourceID)
	// {
	// this.resourceID = resourceID;
	// }

	// public void setResourceType(ResourceType resourceType)
	// {
	// this.resourceType = resourceType;
	// }

	public String toXMLString() {
		StringBuilder buf = new StringBuilder(40);
		buf.append("<");
		buf.append(this.resourceElement);
		buf.append(" " + RESOURCEID_ATTR + "=\"" + this.resourceID + "\"");
		buf.append(" " + ResourceType.RESOURCE_TYPE + "=\""
		    + this.resourceType.toString() + "\"");

		// if(this.RESOURCE_ELEMENT.equals(EndpointResource.ENDPOINT_ELEMENT)){
		// if (this instanceof EndpointResource)
		// {
		// buf.append(" " + Resource.RESOURCE_ACCESSRULE_ATTR + "=\"" +
		// this.accessRule.toString() + "\"");
		// }

		buf.append(">");

		if (this.referencingResourceGroupName != null) {
			for (String string : this.referencingResourceGroupName) {
				buf.append(XmlUtility.toXMLString(REFERENCINGRESOURCEGROUPNAME_ELEMENT,
				    XmlUtility.convertStringToXMLLiterals(string)));
			}
		}

		// End UserGroupProfile
		buf.append("\n");
		buf.append("</" + this.resourceElement + ">");
		return buf.toString();
	}
}
