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

import java.util.Date;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;

/**
 * Created on 25-Sep-06
 */
public final class TaskForm extends ValidatorForm {
	private static final long serialVersionUID = -6561655961982728386L;
	private String state = DracConstants.EMPTY_STRING;
	private String activityName = DracConstants.EMPTY_STRING;

	private String taskOwner = DracConstants.EMPTY_STRING;
	private String taskId = DracConstants.EMPTY_STRING;
	private String taskName = DracConstants.EMPTY_STRING;
	private String webSafeName = DracConstants.EMPTY_STRING;
	private Date submittedTime;

	private String exceptionMessage = DracConstants.EMPTY_STRING;
	private int totalNumberOfActivity = 0;
	private int numberOfCompletedActivity = 0;
	private int percent = 0;
	private String result = "";

	private String[] selectedItems = {};

	/**
	 * @return the activityName
	 */
	public String getActivityName() {
		return activityName;
	}

	/**
	 * @return the exceptionMessage
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}

	/**
	 * @return the numberOfCompletedActivity
	 */
	public int getNumberOfCompletedActivity() {
		return numberOfCompletedActivity;
	}

	/**
	 * @return the percent
	 */
	public int getPercent() {
		return percent;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	public String[] getSelectedItems() {
		return this.selectedItems;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @return the submittedTime
	 */
	public Date getSubmittedTime() {
		return submittedTime;
	}

	/**
	 * @return the taskId
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * @return the taskName
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * @return the taskOwner
	 */
	public String getTaskOwner() {
		return taskOwner;
	}

	/**
	 * @return the totalNumberOfActivity
	 */
	public int getTotalNumberOfActivity() {
		return totalNumberOfActivity;
	}

	public String getWebSafeName() {
		return this.webSafeName;
	}

	/**
	 * @param activityName
	 *          the activityName to set
	 */
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	/**
	 * @param exceptionMessage
	 *          the exceptionMessage to set
	 */
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	/**
	 * @param numberOfCompletedActivity
	 *          the numberOfCompletedActivity to set
	 */
	public void setNumberOfCompletedActivity(int numberOfCompletedActivity) {
		this.numberOfCompletedActivity = numberOfCompletedActivity;
	}

	/**
	 * @param percent
	 *          the percent to set
	 */
	public void setPercent(int percent) {
		this.percent = percent;
	}

	/**
	 * @param result
	 *          the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	public void setSelectedItems(String[] selectedItems) {
		this.selectedItems = selectedItems;
	}

	/**
	 * @param state
	 *          the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @param submittedTime
	 *          the submittedTime to set
	 */
	public void setSubmittedTime(Date submittedTime) {
		this.submittedTime = submittedTime;
	}

	/**
	 * @param taskId
	 *          the taskId to set
	 */
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	/**
	 * @param taskName
	 *          the taskName to set
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * @param taskOwner
	 *          the taskOwner to set
	 */
	public void setTaskOwner(String taskOwner) {
		this.taskOwner = taskOwner;
	}

	/**
	 * @param totalNumberOfActivity
	 *          the totalNumberOfActivity to set
	 */
	public void setTotalNumberOfActivity(int totalNumberOfActivity) {
		this.totalNumberOfActivity = totalNumberOfActivity;
	}

	public void setWebSafeName(String webSafeName) throws Exception {
		this.webSafeName = DracHelper.encodeToUTF8(webSafeName);
	}
}
