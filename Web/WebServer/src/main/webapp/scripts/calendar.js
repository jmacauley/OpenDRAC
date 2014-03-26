/*
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

//-------------

var startDateId = "";
var endDateId = "";
var startTimeId = "";
var endTimeId = "";
var dCal = null;

//
// callback from DRAC Calendar on date change
//
function dateChange() {
    checkStartBeforeEnd(dCal.getDateField());
}

//
// timeChangeListener() - registered for time change events.
//
function timeChangeListener(elem)
{
  dCal.setTimeString(elem.id, elem.value);
  checkStartBeforeEnd(elem);
} /* timeChangeListener */


function setStartDateToday() {
    dCal.setDateField(startDateId);
    dCal.setDateToday();
}

function setEndDateToday() {
    dCal.setDateField(endDateId);
    dCal.setDateToday();
}

function setDatesToday() {
    setStartDateToday();
    setEndDateToday();
}

function setStartDate(dateString)
{
    dCal.setDateString(startDateId, dateString);
}

function setEndDate(dateString)
{
    dCal.setDateString(endDateId, dateString);
}

function setStartTime(timeString)
{
    dCal.setTimeString(startTimeId, timeString);
} /* setStartTime */

function setStartTimePast()
{
/* "past" Start Time is the past time from the last 30 minute mark */

    var newTime = getDefaultTimePast();

    dCal.setTime(startTimeId, newTime);

} /* setStartTimePast */

function setEndTime(timeString)
{
    dCal.setTimeString(endTimeId, timeString);
} /* setEndTime */


function setEndTimeFuture()
{
    defaultEnd = getStartTimeDefault();

    newTime = getTime(defaultEnd);

    dCal.setTime(endTimeId, newTime);

} /* setEndTimeFuture */

/* Start Time is the closest time to the next 30 minute mark */
function setStartTimeDefault()
{
    var defaultStart = getStartTimeDefault();
    var newTime = getTime(defaultStart);

    dCal.setDate(startDateId, defaultStart);
    dCal.setTime(startTimeId, newTime);

} /* setStartTimeDefault */

/* End Time should be 30 minutes after the start time, where
   start time has to be on a 0 or 30 minute boundary */
function setEndTimeDefault()
{
    var defaultEnd = getStartTimeDefault();
    var m1 = defaultEnd.getTime();
    defaultEnd.setTime(m1 + (1000 * 60 * 30));

    var newTime = getTime(defaultEnd);
    dCal.setDate(endDateId, defaultEnd);
    dCal.setTime(endTimeId, newTime);

} /* setEndTimeDefault */



function checkStartBeforeEnd(elem)
{
  var curStartDate = dCal.getDateObject(startDateId);
  var curStartTime = dCal.getTimeObject(startTimeId);
  var curEndDate = dCal.getDateObject(endDateId);
  var curEndTime = dCal.getTimeObject(endTimeId);

  /*alert(curStartDate);
  alert(curStartTime);
  alert(curEndDate);
  alert(curEndTime); */

    // minimum need start and end DATE
  if (curStartDate == null || curEndDate == null)
    return;

  var checkDatesOnly = false;
  if (curStartTime == null && curEndTime == null) {
    checkDatesOnly = true;
  }
  var start = new Date();

  start.setFullYear(curStartDate.getFullYear());
  start.setMonth(curStartDate.getMonth());
  start.setDate(curStartDate.getDate());
  if (curStartTime != null) {
      start.setHours(curStartTime.getHours());
      start.setMinutes(curStartTime.getMinutes());
  } else {
      start.setHours(0);
      start.setMinutes(0);
  }
  start.setSeconds(0);

  var end = new Date();
  end.setFullYear(curEndDate.getFullYear());
  end.setMonth(curEndDate.getMonth());
  end.setDate(curEndDate.getDate());
  if (curEndTime != null) {
    end.setHours(curEndTime.getHours());
    end.setMinutes(curEndTime.getMinutes());
  } else {
    end.setHours(0);
    end.setMinutes(0);
  }
  end.setSeconds(0);

  //alert("Start " + start);
  //alert("End " + end);
  if (start.getTime() >= end.getTime())
  {
    if (elem.id == startTimeId || elem.id == startDateId)
    {
      if (checkDatesOnly) {
        // push end date up
        dCal.setDate(endDateId, start);
      } else {
          // push end time forward 30 minutes
          var startMillis = start.getTime();
          startMillis += (30 * 60 * 1000);
          var newTime = new Date(startMillis);

          dCal.setDate(endDateId, newTime);
          dCal.setTime(endTimeId, newTime);
      }
    }
    else if (elem.id == endTimeId || elem.id == endDateId)
    {
      if (checkDatesOnly) {
        // push start date back
        dCal.setDate(startDateId, end);
      } else {
          // push start time back 30 minutes
          var endMillis = end.getTime();
          endMillis -= (30 * 60 * 1000);
          var newTime = new Date(endMillis);

          dCal.setDate(startDateId, newTime);
          dCal.setTime(startTimeId, newTime);
      }
    }
  }
}

function lockStartDate()
{
  // Set the startDate and startTime fields to now.
  // GG Nov2010 - in the context of client's timezone
  var startDateField = document.getElementById(startDateId);
  var startTimeField = document.getElementById(startTimeId);

  dCal.setDate(startDateId, clientDate);
  dCal.lock(startDateId);

    if (startTimeId != "") {
        dCal.setTime(startTimeId, clientDate);
        dCal.lock(startTimeId);
    }

  checkStartBeforeEnd(startDateField);

  // Disable editing the fields.
  startDateField.disabled = true;
  if (startTimeField) {
    startTimeField.disabled = true;
  }
} /* lockStartDate */

function unlockStartDate()
{
    dCal.unlock(startDateId);
    var startDateField = document.getElementById(startDateId);
    var startTimeField = document.getElementById(startTimeId);
    var endDateField = document.getElementById(endDateId);


    setStartTimeDefault();
    checkStartBeforeEnd(startDateField);

    // enable editing the fields.
    startDateField.disabled = false;
    if (startTimeField) {
    startTimeField.disabled = false;
        dCal.unlock(startTimeId);
    }
} /* unlockStartDate */


function lockEndDate()
{
  // Set the endDate and endTime fields to max.
  var currentDate = new Date(2999, 11, 31, 11, 59, 0, 0);
  var endDateField = document.getElementById(endDateId);
  var endTimeField = document.getElementById(endTimeId);
  dCal.setDate(endDateId, currentDate);
  dCal.lock(endDateId);

    if (endTimeId != "") {
        dCal.setTime(endTimeId, currentDate);
        dCal.lock(endTimeId);
    }

  checkStartBeforeEnd(endDateField);

  // Disable editing the fields.
  endDateField.disabled = true;
  if (endTimeField) {
    endTimeField.disabled = true;
  }

} /* lockEndDate */

function unlockEndDate()
{
    dCal.unlock(endDateId);
    var endDateField = document.getElementById(endDateId);
    var endTimeField = document.getElementById(endTimeId);
    var startDateField = document.getElementById(startDateId);
    var startTimeField = document.getElementById(startTimeId);

    var d = dCal.getDateObject(startDateId);
    var startTime = dCal.getTimeObject(startTimeId);

    if (startTime) {
        d.setHours(startTime.getHours());
        d.setMinutes(startTime.getMinutes());
    }

    d.setTime(d.getTime() + 30*60*1000);
    dCal.setDate(endDateId, d);

    if (endTimeField) {
        dCal.setTime(endTimeId, d);
    }


    // enable editing the fields.
    endDateField.disabled = false;
    if (endTimeField) {
        endTimeField.disabled = false;
        dCal.unlock(endTimeId);
    }

} /* unlockEndDate */
