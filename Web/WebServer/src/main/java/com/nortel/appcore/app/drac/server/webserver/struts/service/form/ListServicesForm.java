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

package com.nortel.appcore.app.drac.server.webserver.struts.service.form;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.struts.validator.ValidatorActionForm;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

public final class ListServicesForm extends ValidatorActionForm {
	private static final long serialVersionUID = 4242571803018369058L;
	private String startdate = DracConstants.EMPTY_STRING;
	private String enddate = DracConstants.EMPTY_STRING;
	private String memberGroup = DracConstants.EMPTY_STRING;
	private List<UserGroupName> memberGroupList = new ArrayList<UserGroupName>();

	private String selectedItem = "";
	private String[] selectedItems = {};
	private String command = "";

	public static final String CANCEL_ACTION = "cancel";
	public static final String ACTIVATE_ACTION = "activate";
	public static final String CANCEL_SEL_ACTION = "cancelAll";
	public static final String ACTIVATE_SEL_ACTION = "activateAll";

	public String getCommand() {
		return command;
	}

	/**
	 * @return the enddate
	 */
	public String getEnddate() {
		return enddate;
	}

	/**
	 * @return the memberGroup
	 */
	public String getMemberGroup() {
		return memberGroup;
	}

	/**
	 * @return the memberGroupList
	 */
	public List<UserGroupName> getMemberGroupList() {
		return memberGroupList;
	}

	public String getSelectedItem() {
		return selectedItem;
	}

	public String[] getSelectedItems() {
		return this.selectedItems;
	}

	/**
	 * @return the startdate
	 */
	public String getStartdate() {
		return startdate;
	}

	public void setCommand(String action) {
		this.command = action;
	}

	/**
	 * @param enddate
	 *          the enddate to set
	 */
	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	/**
	 * @param memberGroup
	 *          the memberGroup to set
	 */
	public void setMemberGroup(String group) {
		this.memberGroup = group;
	}

	/**
	 * @param memberGroupList
	 *          the memberGroupList to set
	 */
	public void setMemberGroupList(List<UserGroupName> groupList) {
		this.memberGroupList = new ArrayList<UserGroupName>(new TreeSet<UserGroupName>(groupList));
	}

	public void setSelectedItem(String selectedItem) {
		this.selectedItem = selectedItem;
	}

	public void setSelectedItems(String[] selectedItems) {
		this.selectedItems = selectedItems;
	}

	/**
	 * @param startdate
	 *          the startdate to set
	 */
	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

}
