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

package com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form;

import java.util.ArrayList;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfileXML.ResourceType;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on 20-Oct-06
 */
public class ResourceForm extends ValidatorForm {
	private static final long serialVersionUID = -4827377831220139483L;
	private ArrayList<String> referencingResourceGroupNames = new ArrayList<String>();
	private String resourceID = DracConstants.EMPTY_STRING;
	private ResourceType resourceType = ResourceType.ENDPOINT;
	private String name = DracConstants.EMPTY_STRING;
	private String label = DracConstants.EMPTY_STRING;

	private String error = "";

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public ArrayList<String> getReferencingResourceGroupNames() {
		return referencingResourceGroupNames;
	}

	public String getResourceID() {
		return resourceID;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	/**
	 * @param error
	 *          the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void setReferencingResourceGroupNames(
	    ArrayList<String> referencingResourceGroupNames) {
		this.referencingResourceGroupNames = referencingResourceGroupNames;
	}

	public void setResourceID(String resourceID) {
		this.resourceID = resourceID;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

}
