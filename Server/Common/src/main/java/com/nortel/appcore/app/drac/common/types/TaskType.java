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

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class TaskType implements Serializable {
  public static enum ACTIVITY {
    SCHEDULE_CANCEL, SERVICE_CREATE, SERVICE_CANCEL, SERVICE_CONFIRMATION, SERVICE_CONFIRMATION_CANCEL, SCHEDULE_CONFIRMATION, SCHEDULE_CONFIRMATION_CANCEL, SERVICE_ACTIVATION, SERVICE_ACTIVATION_CANCEL, TIMED_OUT
  }

  public static enum Result {
    UNKNOWN, SUCCESS, PARTIAL_SUCCESS, FAILED
  }

  public static enum State {
    SUBMITTED, IN_PROGRESS, ABORTED, DONE
  }

  static final long serialVersionUID = 1L;
  /**
   * Lifecycle state
   */
  private State state;
  private Result result = Result.UNKNOWN;
  private final String activityName;
  private final String taskOwner;
  private String taskName;
  private final String taskId;
  private Date submittedTime;
  private String exceptionMessage;
private String exceptionResourceKey;
  private int totalNumberOfActivity;
  private int numberOfCompletedActivity;
  private int percentage;
  private float localPercentage;
  private float percentageIncrementUnit = 100;
  private transient List<StatusType> statusInfoList;

  // /**
  // * @param activityName Name of the activity(Or Action)initiated by the
  // client.
  // * @param taskOwner
  // */
  // public TaskInfo(String activityName, String taskOwner)
  // {
  // this.state = State.SUBMITTED;
  // this.activityName = activityName;
  // this.taskOwner = taskOwner;
  // this.percentage = 0;
  // // this.taskId = System.currentTimeMillis();
  // }

  /**
     *
     */
  public TaskType(String activity, String owner, String taskIdentifier) {
    state = State.SUBMITTED;
    activityName = activity;
    taskOwner = owner;
    percentage = 0;
    taskId = taskIdentifier;
  }

  public void addStatusInfo(StatusType statusType) {
    if (this.statusInfoList == null) {
      this.statusInfoList = new ArrayList<StatusType>();
    }
    this.statusInfoList.add(statusType);
  }

  /**
   * Name of the activity(Or Action)initiated by the client.
   *
   * @return Returns the activity name.
   */
  public String getActivityName() {
    return this.activityName;
  }

  public String getExceptionMessage() {
    return this.exceptionMessage;
  }

  /**
   * @return Returns the exceptionResourceKey.
   */
  public String getExceptionResourceKey() {
    return exceptionResourceKey;
  }

  /**
   * @return Returns the numberOfCompletedActivity.
   */
  public int getNumberOfCompletedActivity() {
    return this.numberOfCompletedActivity;
  }

  /**
   * Returns the percentage value of task
   *
   * @return Returns the percentage.
   */
  public int getPercentage() {
    return this.percentage;
  }

  public Result getResult() {
    return result;
  }

  /**
   * @return Returns the state.
   */
  public State getState() {
    return this.state;
  }

  /**
   * Returns the status information list. Returns null if status information
   * list is empty.
   *
   * @return Returns status information list or null if status information list
   *         is empty. TODO: May be this is overweight
   */
  public List<StatusType> getStatusInfoList() {
    return this.statusInfoList;
  }

  /**
   * @return Returns the submitted time.
   */
  public Date getSubmittedTime() {
    return this.submittedTime;
  }

  /**
   * @return Returns the taskId.
   */
  public String getTaskId() {
    return this.taskId;
  }

  /**
   * Get the name of associated with the task by the client(Task identifier)
   *
   * @return Returns the task name.
   */
  public String getTaskName() {
    return this.taskName;
  }

  /**
   * @return Returns the owner.
   */
  public String getTaskOwner() {
    return taskOwner;
  }

  /**
   * @return Returns the totalNumberOfActivity.
   */
  public int getTotalNumberOfActivity() {
    return this.totalNumberOfActivity;
  }

  public synchronized void markActivityCompletion() {
    numberOfCompletedActivity++;
    localPercentage = localPercentage + percentageIncrementUnit;
    if (localPercentage > 100) {
      localPercentage = 100;
    }
    this.percentage = new Float(localPercentage).intValue();
  }

  /**
   * @param exceptionMessage
   *          The exceptionMessage to set.
   */
  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }

  /**
   * @param exceptionResourceKey
   *          The exceptionResourceKey to set.
   */
  public void setExceptionResourceKey(String exceptionResourceKey) {
    this.exceptionResourceKey = exceptionResourceKey;
  }

  /**
   * This method allow to overwrite the number of completed activity value
   * calculated by program..
   *
   * @see TaskType#markActivityCompletion()
   * @param numberOfCompletedActivity
   *          The numberOfCompletedActivity to set.
   */
  public void setNumberOfCompletedActivity(int numberOfCompletedActivity) {
    this.numberOfCompletedActivity = numberOfCompletedActivity;
  }

  /**
   * This method allow to overwrite the percentage value calculated by the
   * generic algorithm.
   *
   * @see TaskType#markActivityCompletion()
   * @param percentage
   *          The percentage to set.
   */
  public void setPercentage(int percentage) {
    this.percentage = percentage;
  }

  public void setResult(Result result) {
    this.result = result;
  }

  /**
   * @param state
   *          The state to set.
   */
  public void setState(State state) {
    if (state == State.DONE || state == State.ABORTED) {
      this.percentage = 100;
    }
    this.state = state;
  }

  /**
   * @param submittedTime
   *          The submitted time to set.
   */
  public void setSubmittedTime(long submittedTime) {
    this.submittedTime = new Date(submittedTime);
  }

  /**
   * Set the name of associated with the task by the client (Task identifier)
   *
   * @param taskName
   *          The task name to set.
   */
  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public void setTotalNumberOfActivity(int totalNumberOfActivity) {
    if (totalNumberOfActivity == 0) {
      return;
    }
    if (this.totalNumberOfActivity != totalNumberOfActivity) {
      percentageIncrementUnit = (100.0f - getPercentage())
          / ((float) totalNumberOfActivity - (float) numberOfCompletedActivity);
    }
    this.totalNumberOfActivity = totalNumberOfActivity;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TaskInfo [state=");
    builder.append(state);
    builder.append(", result=");
    builder.append(result);
    builder.append(", activityName=");
    builder.append(activityName);
    builder.append(", taskOwner=");
    builder.append(taskOwner);
    builder.append(", taskName=");
    builder.append(taskName);
    builder.append(", taskId=");
    builder.append(taskId);
    builder.append(", submittedTime=");
    builder.append(submittedTime);
    builder.append(", exceptionMessage=");
    builder.append(exceptionMessage);
    builder.append(", exceptionResourceKey=");
    builder.append(exceptionResourceKey);
    builder.append(", totalNumberOfActivity=");
    builder.append(totalNumberOfActivity);
    builder.append(", numberOfCompletedActivity=");
    builder.append(numberOfCompletedActivity);
    builder.append(", percentage=");
    builder.append(percentage);
    builder.append(", localPercentage=");
    builder.append(localPercentage);
    builder.append(", percentageIncrementUnit=");
    builder.append(percentageIncrementUnit);
    builder.append("]");
    return builder.toString();
  }

}
