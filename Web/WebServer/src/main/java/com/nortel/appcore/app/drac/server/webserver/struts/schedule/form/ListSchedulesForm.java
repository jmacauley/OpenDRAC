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

package com.nortel.appcore.app.drac.server.webserver.struts.schedule.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.validator.ValidatorActionForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on 24-Jul-06
 */
public final class ListSchedulesForm extends ValidatorActionForm {
	private static final long serialVersionUID = -935073755653425095L;
	private String startdate = DracConstants.EMPTY_STRING;
	private String enddate = DracConstants.EMPTY_STRING;
	private String group = DracConstants.EMPTY_STRING;
	private List groupList = new ArrayList();

	private String selectedItem = "";
	private String[] selectedItems = {};
	private String command = "";
	private String name = DracConstants.EMPTY_STRING;
	private String id = DracConstants.EMPTY_STRING;

	public static final String CANCEL_ACTION = "cancel";
	public static final String CANCEL_SEL_ACTION = "cancelAll";
	public static final String CONFIRM_SEL_ACTION = "confirmAll";

	public static final String CANCEL_SERVICES_ACTION = "cancelServices";
	public static final String CONFIRM_ACTION = "confirm";
	public static final String ACTIVATE_ACTION = "activate";

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
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @return the groupList
	 */
	public List getGroupList() {
		return groupList;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
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

	public void reset() {
		this.selectedItems = new String[] {};
		this.command = "";
		this.id = "";
		this.name = "";
		this.startdate = "";
		this.enddate = "";
		this.group = "";
		this.selectedItem = "";
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
	 * @param group
	 *          the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @param groupList
	 *          the groupList to set
	 */
	public void setGroupList(List groupList) {
		this.groupList = groupList;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
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
