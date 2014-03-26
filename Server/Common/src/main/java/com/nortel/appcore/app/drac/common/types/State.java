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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.TaskType.ACTIVITY;

/**
 * @author Niranjan Chelliah
 */
public final class State {
  
  private static final Logger log = LoggerFactory.getLogger(State.class);
  
  public static enum CALL {
    CONFIRMATION_PENDING, CONFIRMATION_TIMED_OUT, CONFIRMATION_CANCELLED, ACTIVATION_PENDING, ACTIVATION_TIMED_OUT, ACTIVATION_CANCELLED, EXECUTION_PENDING, EXECUTION_INPROGRESS, EXECUTION_SUCCEEDED, EXECUTION_TIMED_OUT, EXECUTION_FAILED, EXECUTION_PARTIALLY_CANCELLED, EXECUTION_CANCELLED, CREATE_FAILED, DELETE_FAILED;
  }

  public static enum SCHEDULE {
    CONFIRMATION_PENDING(STATE_VALUE.CONFIRMATION_PENDING), CONFIRMATION_TIMED_OUT(
        STATE_VALUE.CONFIRMATION_TIMED_OUT), CONFIRMATION_CANCELLED(
        STATE_VALUE.CONFIRMATION_CANCELLED),

    EXECUTION_PENDING(STATE_VALUE.EXECUTION_PENDING), EXECUTION_INPROGRESS(
        STATE_VALUE.EXECUTION_INPROGRESS), EXECUTION_SUCCEEDED(
        STATE_VALUE.EXECUTION_SUCCEEDED), EXECUTION_PARTIALLY_SUCCEEDED(
        STATE_VALUE.EXECUTION_PARTIALLY_SUCCEEDED), EXECUTION_TIME_OUT(
        STATE_VALUE.EXECUTION_TIME_OUT), EXECUTION_FAILED(
        STATE_VALUE.EXECUTION_FAILED), EXECUTION_PARTIALLY_CANCELLED(
        STATE_VALUE.EXECUTION_PARTIALLY_CANCELLED), EXECUTION_CANCELLED(
        STATE_VALUE.EXECUTION_CANCELLED);

    private final STATE_VALUE value;

    private SCHEDULE(STATE_VALUE value) {
      this.value = value;
    }

    protected STATE_VALUE getStateValue() {
      return value;
    }
  }

  public static enum SERVICE {
    CONFIRMATION_PENDING(STATE_VALUE.CONFIRMATION_PENDING), //
    CONFIRMATION_TIMED_OUT(STATE_VALUE.CONFIRMATION_TIMED_OUT), //
    CONFIRMATION_CANCELLED(STATE_VALUE.CONFIRMATION_CANCELLED), //

    ACTIVATION_PENDING(STATE_VALUE.ACTIVATION_PENDING), //
    ACTIVATION_TIMED_OUT(STATE_VALUE.ACTIVATION_TIMED_OUT), //
    ACTIVATION_CANCELLED(STATE_VALUE.ACTIVATION_CANCELLED), //

    EXECUTION_PENDING(STATE_VALUE.EXECUTION_PENDING), //
    EXECUTION_INPROGRESS(STATE_VALUE.EXECUTION_INPROGRESS), //
    EXECUTION_SUCCEEDED(STATE_VALUE.EXECUTION_SUCCEEDED), //
    EXECUTION_TIMED_OUT(STATE_VALUE.EXECUTION_TIME_OUT), //
    EXECUTION_FAILED(STATE_VALUE.EXECUTION_FAILED), //
    EXECUTION_PARTIALLY_CANCELLED(STATE_VALUE.EXECUTION_PARTIALLY_CANCELLED), //
    EXECUTION_CANCELLED(STATE_VALUE.EXECUTION_CANCELLED), //

    // These state will be used internally
    CREATE_FAILED(STATE_VALUE.CREATE_FAILED), //
    DELETE_FAILED(STATE_VALUE.DELETE_FAILED);

    private final STATE_VALUE value;

    private SERVICE(STATE_VALUE value) {
      this.value = value;
    }

    protected STATE_VALUE getStateValue() {
      return value;
    }
  }

  private static enum STATE_VALUE {
    CONFIRMATION_PENDING, CONFIRMATION_TIMED_OUT, CONFIRMATION_CANCELLED, ACTIVATION_PENDING, ACTIVATION_TIMED_OUT, ACTIVATION_CANCELLED, EXECUTION_PENDING, EXECUTION_INPROGRESS, EXECUTION_SUCCEEDED, EXECUTION_PARTIALLY_SUCCEEDED, EXECUTION_TIME_OUT, EXECUTION_FAILED, EXECUTION_PARTIALLY_CANCELLED, EXECUTION_CANCELLED, CREATE_FAILED, DELETE_FAILED
  }

  private State() {
    super();
  }

  public static SCHEDULE evaluateState(SCHEDULE currentScheduleState,
      Set<SERVICE> serviceStateSet) {
    // Common for all activation type
    if (currentScheduleState == SCHEDULE.EXECUTION_PARTIALLY_CANCELLED
        || serviceStateSet.isEmpty()) {
      return currentScheduleState;
    }
    if (serviceStateSet.size() == 1) {
      if (serviceStateSet.contains(SERVICE.CONFIRMATION_PENDING)) {
        return SCHEDULE.CONFIRMATION_PENDING;
      }
      if (serviceStateSet.contains(SERVICE.CONFIRMATION_TIMED_OUT)) {
        return SCHEDULE.CONFIRMATION_TIMED_OUT;
      }
      if (serviceStateSet.contains(SERVICE.CONFIRMATION_CANCELLED)) {
        return SCHEDULE.CONFIRMATION_CANCELLED;
      }
      if (serviceStateSet.contains(SERVICE.ACTIVATION_TIMED_OUT)) {
        return SCHEDULE.EXECUTION_TIME_OUT;
      }
      if (serviceStateSet.contains(SERVICE.EXECUTION_PENDING)) {
        return SCHEDULE.EXECUTION_PENDING;
      }
      if (serviceStateSet.contains(SERVICE.EXECUTION_SUCCEEDED)) {
        return SCHEDULE.EXECUTION_SUCCEEDED;
      }
      if (serviceStateSet.contains(SERVICE.EXECUTION_FAILED)
          || serviceStateSet.contains(SERVICE.CREATE_FAILED)
          || serviceStateSet.contains(SERVICE.DELETE_FAILED)) {
        return SCHEDULE.EXECUTION_FAILED;
      }
      if (serviceStateSet.contains(SERVICE.EXECUTION_CANCELLED)
          || serviceStateSet.contains(SERVICE.ACTIVATION_CANCELLED)) {
        return SCHEDULE.EXECUTION_CANCELLED;
      }
      if (serviceStateSet.contains(SERVICE.EXECUTION_INPROGRESS)) {
        return SCHEDULE.EXECUTION_INPROGRESS;
      }
      if (serviceStateSet.contains(SERVICE.EXECUTION_PARTIALLY_CANCELLED)) {
        return SCHEDULE.EXECUTION_PARTIALLY_CANCELLED;
      }
      log.error(" Schedule State evaluation failed : Service state set is = "
          + serviceStateSet.toString());
      return currentScheduleState;
    }
    else if (serviceStateSet.contains(SERVICE.CONFIRMATION_PENDING)) {
      return SCHEDULE.CONFIRMATION_PENDING;
    }
    else if (serviceStateSet.contains(SERVICE.ACTIVATION_PENDING)
        || serviceStateSet.contains(SERVICE.CONFIRMATION_PENDING)
        || serviceStateSet.contains(SERVICE.EXECUTION_INPROGRESS)
        || serviceStateSet.contains(SERVICE.EXECUTION_PENDING)) {
      return SCHEDULE.EXECUTION_INPROGRESS;
    }
    else if (serviceStateSet.contains(SERVICE.EXECUTION_CANCELLED)
        && serviceStateSet.contains(SERVICE.ACTIVATION_CANCELLED)) {
      return SCHEDULE.EXECUTION_CANCELLED;
    }
    else {
      // everything else is...
      return SCHEDULE.EXECUTION_PARTIALLY_SUCCEEDED;
    }
  }

  public static SERVICE evaluateState(TaskType.ACTIVITY activityName,
      ACTIVATION_TYPE activationType, SCHEDULE currentState) {
    return SERVICE.valueOf(State.evaluateState(activityName, activationType,
        currentState.getStateValue()).name());
  }

  public static SERVICE evaluateState(TaskType.ACTIVITY activityName,
      ACTIVATION_TYPE activationType, SERVICE currentState) {
    if (activityName == ACTIVITY.SCHEDULE_CONFIRMATION) {
      if (activationType == ACTIVATION_TYPE.PRERESERVATION_AUTOMATIC) {
        return SERVICE.EXECUTION_PENDING;
      }
      else if (activationType == ACTIVATION_TYPE.PRERESERVATION_MANUAL) {
        return SERVICE.ACTIVATION_PENDING;
      }
    }
    return SERVICE.valueOf(State.evaluateState(activityName, activationType,
        currentState.getStateValue()).name());
  }

  public static SCHEDULE evaluateState(TaskType.ACTIVITY activityName,
      SCHEDULE currentState) {
    return SCHEDULE.valueOf(State.evaluateState(activityName,
        currentState.getStateValue()).name());
  }

  public static SERVICE evaluateState(TaskType.ACTIVITY activityName,
      SERVICE currentState) {
    return SERVICE.valueOf(State.evaluateState(activityName,
        currentState.getStateValue()).name());
  }

  public static boolean isActivateable(SERVICE currentState) {
    return isActivateable(STATE_VALUE.valueOf(currentState.name()));
  }

  public static boolean isCancelable(SCHEDULE currentState) {
    return State.isCancelable(currentState.getStateValue());
  }

  public static boolean isCancelable(SERVICE currentState) {
    return State.isCancelable(currentState.getStateValue());
  }

  public static boolean isConfirmable(SCHEDULE currentState) {
    return isConfirmable(STATE_VALUE.valueOf(currentState.name()));
  }

  public static boolean isConfirmable(SERVICE currentState) {
    return isConfirmable(STATE_VALUE.valueOf(currentState.name()));
  }

  public static boolean isExpandable(SCHEDULE currentState) {
    return State.isExpandable(currentState.getStateValue());
  }

  /**
   * State indicating whether a schedule can be expanded (i.e. add new services)
   */
  protected static boolean isExpandable(State.STATE_VALUE currentState) {
    if (STATE_VALUE.CONFIRMATION_PENDING == currentState
        || STATE_VALUE.EXECUTION_PENDING == currentState
        || STATE_VALUE.EXECUTION_INPROGRESS == currentState) {
      return true;
    }
    return false;
  }

  private static STATE_VALUE evaluateState(TaskType.ACTIVITY activityName,
      ACTIVATION_TYPE activationType, STATE_VALUE currentState) {
    if (TaskType.ACTIVITY.SERVICE_CREATE == activityName) {
      if (STATE_VALUE.CONFIRMATION_PENDING == currentState) {
        return STATE_VALUE.CONFIRMATION_PENDING;
      }
      else if (STATE_VALUE.EXECUTION_PENDING == currentState
          || STATE_VALUE.EXECUTION_INPROGRESS == currentState) {
        if (ACTIVATION_TYPE.RESERVATION_AUTOMATIC == activationType) {
          return STATE_VALUE.EXECUTION_PENDING;
        }
        else if (ACTIVATION_TYPE.RESERVATION_MANUAL == activationType) {
          return STATE_VALUE.ACTIVATION_PENDING;
        }
        else if (ACTIVATION_TYPE.PRERESERVATION_AUTOMATIC == activationType) {
          return STATE_VALUE.EXECUTION_PENDING;
        }
        else if (ACTIVATION_TYPE.PRERESERVATION_MANUAL == activationType) {
          return STATE_VALUE.ACTIVATION_PENDING;
        }
      }
    }
    return evaluateState(activityName, currentState);
  }

  private static STATE_VALUE evaluateState(TaskType.ACTIVITY activityName,
      STATE_VALUE currentState) {
    if ((TaskType.ACTIVITY.SERVICE_CANCEL == activityName || TaskType.ACTIVITY.SCHEDULE_CANCEL == activityName)
        && isCancelable(currentState)) {
      if (STATE_VALUE.CONFIRMATION_PENDING == currentState) {
        return STATE_VALUE.CONFIRMATION_CANCELLED;
      }
      else if (STATE_VALUE.ACTIVATION_PENDING == currentState) {
        return STATE_VALUE.ACTIVATION_CANCELLED;
      }
      else if (STATE_VALUE.EXECUTION_PENDING == currentState
          || STATE_VALUE.EXECUTION_INPROGRESS == currentState) {
        return STATE_VALUE.EXECUTION_CANCELLED;
      }
    }
    else if (TaskType.ACTIVITY.SERVICE_CONFIRMATION_CANCEL == activityName
        || TaskType.ACTIVITY.SCHEDULE_CONFIRMATION_CANCEL == activityName) {
      return STATE_VALUE.CONFIRMATION_CANCELLED;
    }
    else if (TaskType.ACTIVITY.SERVICE_ACTIVATION_CANCEL == activityName) {
      return STATE_VALUE.ACTIVATION_CANCELLED;
    }
    else if (TaskType.ACTIVITY.SERVICE_ACTIVATION == activityName
        && currentState == STATE_VALUE.ACTIVATION_PENDING) {
      return STATE_VALUE.EXECUTION_PENDING;
    }
    else if (TaskType.ACTIVITY.SCHEDULE_CONFIRMATION == activityName
        && currentState == STATE_VALUE.CONFIRMATION_PENDING) {
      return STATE_VALUE.EXECUTION_PENDING;
    }
    else if (TaskType.ACTIVITY.SERVICE_CONFIRMATION == activityName
        && currentState == STATE_VALUE.CONFIRMATION_PENDING) {
      return STATE_VALUE.ACTIVATION_PENDING;
    }
    else if (TaskType.ACTIVITY.TIMED_OUT == activityName) {
      if (STATE_VALUE.ACTIVATION_PENDING == currentState) {
        return STATE_VALUE.ACTIVATION_TIMED_OUT;
      }
      else if (STATE_VALUE.CONFIRMATION_PENDING == currentState) {
        return STATE_VALUE.CONFIRMATION_TIMED_OUT;
      }
      else if (STATE_VALUE.EXECUTION_PENDING == currentState) {
        return STATE_VALUE.EXECUTION_TIME_OUT;
      }
    }
    return currentState;
  }

  private static boolean isActivateable(STATE_VALUE currentState) {
    return currentState == STATE_VALUE.ACTIVATION_PENDING;
  }

  private static boolean isCancelable(State.STATE_VALUE currentState) {
    if (STATE_VALUE.CONFIRMATION_PENDING == currentState
        || STATE_VALUE.ACTIVATION_PENDING == currentState
        || STATE_VALUE.EXECUTION_PENDING == currentState
        || STATE_VALUE.EXECUTION_INPROGRESS == currentState) {
      return true;
    }
    return false;
  }

  private static boolean isConfirmable(STATE_VALUE currentState) {
    return currentState == STATE_VALUE.CONFIRMATION_PENDING;
  }
}
