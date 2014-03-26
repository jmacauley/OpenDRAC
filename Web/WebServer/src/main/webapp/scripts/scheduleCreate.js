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

var endByDateId = "";
var srcFacLabelList = new Array();
var destFacLabelList = new Array();

// See StringParser.java
var ENCODED_COLON = ".colon.";
var UNENCODED_COLON = ":";

function replaceAll( str, from, to )
{
    var idx = str.indexOf( from );
    while ( idx > -1 )
    {
        str = str.replace( from, to );
        idx = str.indexOf( from );
    }
    return str;
}


//
// Check if the provided select combo box has the provided value as an item
//
function selectContains(selectElem, value) {
    if (selectElem) {
        for (var i=0; i < selectElem.options.length; i++) {
            if (selectElem.options[i].value == value) {
                return true;
            }
        }
    }
    return false;
}

//validates time and date before going to the recurrence page and calculates the value of dur_hr and dur_min
function checkTimes()
{
    var strval = document.CreateScheduleForm.startTime.value;
    var colon_start=strval.indexOf(':');
    var hour_start= strval.substring(0,colon_start);
    var m_start=colon_start+1;
    var min_start = strval.substring(m_start,strval.indexOf(' '));
    if(strval.length==7)
    {
        if(isNaN(strval.charAt(0)))
        {
            alert('Please enter a valid Time');
                return false;
        }
        else{
            if((hour_start > 12)||(hour_start < 0))
            {
                alert('Enter a valid Start Time');
                return false;
            }
            if((min_start > 59)||(min_start < 0))
            {
                alert('Enter a valid Start Time');
                return false;
            }
        }
        if (strval.charAt(1)!=":")
        {
                alert('please enter a valid Time');
                return false;
        }
        if(isNaN(strval.charAt(2))||isNaN(strval.charAt(3)))
        {
            alert('Please enter a valid Time');
            return false;
        }
        if (strval.charAt(4)!=" ")
        {
            alert('please enter a valid Time');
            return false;
        }
    }
    else if(strval.length==8)
    {
        if(isNaN(strval.charAt(0))||isNaN(strval.charAt(1)))
        {
            alert('Please enter a valid Time');
            return false;
        }
        else{
            if((hour_start > 12)||(hour_start < 0))
            {
                alert('Enter a valid Start Time Hours');
                return false;
            }
            if((min_start > 59)||(min_start < 0))
            {
                alert('Enter a valid Start Time Min');
                return false;
            }
        }
        if (strval.charAt(2)!=":")
        {
            alert('please enter a valid Time');
                return false;
        }
        if(isNaN(strval.charAt(3))||isNaN(strval.charAt(4)))
        {
            alert('Please enter a valid Time');
                return false;
        }
        if (strval.charAt(5)!=" ")
        {
            alert('please enter a valid Time');
                return false;
        }
    }
    else
    {
        alert('Enter a valid Start Time');
        return false;
    }
    //for end time
    var endval = document.CreateScheduleForm.endTime.value;
    if ((endval == null) || (endval == "") || (endval == " "))
    {
        alert("Enter a valid End Time");
            return false;
    }
    var n2 = endval.indexOf(":");
    var hr2 = endval.substring(0, n2);
    var m2 = n2 + 1;
    var min2 = endval.substring(m2, endval.indexOf(" "));
    if ((hr2 > 12) || (hr2 < 0) || isNaN(hr2))
    {
        alert("Enter a valid End Time");
        return false;
    }
    if ((n2 < 0) || (endval.indexOf(" ") < 0)) {
        alert("Enter a valid End Time");
        return false;
    }
    if ((min2 > 60) || (min2 < 0) || isNaN(min2)) {
        alert("Enter a valid End Time");
        return false;
    }


    var c1 = strval.indexOf(" ");
    d1 = c1 + 1;
    var tod1 = strval.charAt(d1);
    if ((tod1 == "P") && (hour_start != 12))
    {
        hour_start = parseInt(hour_start) + 12;
    }
    if ((tod1 == "A") && (hour_start == 12))
    {
        hour_start = parseInt(hour_start) - 12;
    }

    var c2 = endval.indexOf(" ");
    d2 = c2 + 1;
    var tod2 = endval.charAt(d2);
    var durmin = "";
    var durhr = "";
    if ((tod2 == "P") && (hr2 != 12))
    {
        hr2 = parseInt(hr2) + 12;
    }
    if ((tod2 == "A") && (hr2 == 12))
    {
        hr2 = parseInt(hr2) - 12;
    }


    if (parseInt(hr2) < parseInt(hour_start))
    {
        alert("End Time is less than Start Time");
        frm.dur_min.value = "";
        frm.dur_hr.value = "";
        return false;
    } else {
        if (hr2 == hour_start)
        {
            if (min2 < min_start)
            {
                alert("End Time is less than Start Time");
                frm.dur_min.value = "";
                frm.dur_hr.value = "";
                return false;
            } else
            {
                durmin = parseInt(min2) - parseInt(min_start);
                durhr = "0";
            }
        }
        else
        {
            if (parseInt(hr2) > parseInt(hour_start))
            {
                if (parseInt(min2) >= parseInt(min_start))
                {
                    durmin = parseInt(min2) - parseInt(min_start);
                    durhr = parseInt(hr2) - parseInt(hour_start);
                }
                else
                {
                    min2 = parseInt(min2) + 60;
                    hr2 = parseInt(hr2) - 1;
                    durmin = parseInt(min2) - parseInt(min_start);
                    durhr = parseInt(hr2) - parseInt(hour_start);
                }
            }
        }
    }
    document.CreateScheduleForm.dur_min.value = durmin;
    document.CreateScheduleForm.dur_hr.value = durhr;
    return true;
}
// Does the validation for the fields in the advanced tab, only when the fields are visible
function AdvVal()
{
    if (document.getElementById('advancedOptions').style.display == "") {
        var cost = document.CreateScheduleForm.cost.value;
        var metric = document.CreateScheduleForm.metric.value;
        var hop = document.CreateScheduleForm.hop.value;
        var emailID = document.CreateScheduleForm.email;
        //var srlg = document.CreateScheduleForm.srlg.value;
        if ((emailID.value != null) && (emailID.value != ""))
        {
            if (echeck(emailID.value) == false)
            {
                return false;
            }
        }
        if (isNaN(cost))
        {
            alert("Cost is only numeric");
                return false;
        }
        if (isNaN(metric))
        {
            alert("Metric is only numeric");
                return false;
        }
        if (isNaN(hop)) {
            alert("Hop is only numeric");
                return false;
        }
    } else if (document.getElementById('advancedOptions').style.display == "none") {
        return true;
    }
}
// validation for the email ID in advanced tab.
function echeck(str)
{
    var at = "@";
    var dot = ".";
    var lat = str.indexOf(at);
    var lstr = str.length;
    var ldot = str.indexOf(dot);
    if (str.indexOf(at) == -1)
    {
        alert("Invalid E-mail ID");
        return false;
    }
    if (str.indexOf(at) == -1 || str.indexOf(at) == 0 || str.indexOf(at) == lstr)
    {
        alert("Invalid E-mail ID");
        return false;
    }
    if (str.indexOf(dot) == -1 || str.indexOf(dot) == 0 || str.indexOf(dot) == lstr)
    {
        alert("Invalid E-mail ID");
        return false;
    }
    if (str.indexOf(at, (lat + 1)) != -1)
    {
        alert("Invalid E-mail ID");
        return false;
    }
    if (str.substring(lat - 1, lat) == dot || str.substring(lat + 1, lat + 2) == dot)
    {
        alert("Invalid E-mail ID");
        return false;
    }
    if (str.indexOf(dot, (lat + 2)) == -1)
    {
        alert("Invalid E-mail ID");
        return false;
    }
    if (str.indexOf(" ") != -1)
    {
        alert("Invalid E-mail ID");
        return false;
    }
}


function dateCheck(startdate, enddate)
{
    if (Date.parse(startdate.value) > Date.parse(enddate.value))
    {
        alert("End Date is before Start Date");
        return false;
    }
}


// Validation for the drop downs
var fActiveMenu = false;
var oOverMenu = false;
function mouseSelect(e) {
    if (fActiveMenu) {
        if (oOverMenu == false) {
            oOverMenu = false;
            document.getElementById(fActiveMenu).style.display = "none";
            fActiveMenu = false;
            return false;
        }
        return false;
    }
    return true;
}
function menuActivate(idEdit, idMenu, idSel) {

    if (fActiveMenu) {
        return mouseSelect(0);
    }
    oMenu = document.getElementById(idMenu);
    oEdit = (document.getElementsByName(idEdit))[0];
    nTop = oEdit.offsetTop + oEdit.offsetHeight;
    nLeft = oEdit.offsetLeft;
    while (oEdit.offsetParent != document.body) {
        oEdit = oEdit.offsetParent;
        nTop += oEdit.offsetTop;
        nLeft += oEdit.offsetLeft;
    }
    oMenu.style.left = nLeft;
    oMenu.style.top = nTop;
    oMenu.style.display = "";
    fActiveMenu = idMenu;

    oSel = document.getElementById(idSel);
    oSel.value = ((document.getElementsByName(idEdit))[0].value);
    oSel.focus();

    return false;
}
function textSet(idEdit, text) {
    (document.getElementsByName(idEdit))[0].value = text;
    oOverMenu = false;
    mouseSelect(0);
    (document.getElementsByName(idEdit))[0].focus();
}
function comboKey(idEdit, idSel) {
    if (window.event.keyCode == 13 || window.event.keyCode == 32) {
        textSet(idEdit, idSel.value);
    } else {
        if (window.event.keyCode == 27) {
            mouseSelect(0);
            (document.getElementsByName(idEdit))[0].focus();
        }
    }
}
document.onmousedown = mouseSelect;
function default1() {
    document.all.my.style.visibility = "hidden";
}
function set(What, Value) {
    if (document.layers && document.layers[What] != null) {
        document.layers[What].visibility = Value;
    } else {
        if (document.all) {
            eval("document.all." + What + ".style.visibility =\"" + Value + "\"");
        }
    }
}
function clicked(Form, Radio, Layer) {
    for (var i = 0; i < Form[Radio].length; i++) {
        if (Form[Radio][i].checked) {
            set(Layer, Form[Radio][i].value);
        }
    }
}
function SwitchMenu(obj) {
    if (document.getElementById) {
        var el = document.getElementById(obj);
        if (el.style.display == "none") {
            el.style.display = "block";
        } else {
            el.style.display = "none";
        }
    }
}

function handleClickDaily()
{
  obj = document.getElementById("dailyLayer");
  obj.style.display = "";
  obj = document.getElementById("weeklyLayer");
  obj.style.display = "none";
  obj = document.getElementById("monthlyLayer");
  obj.style.display = "none";
  obj = document.getElementById("yearlyLayer");
  obj.style.display = "none";
  //recurrenceChange();
  applyOccurrences();
}
function handleClickWeekly()
{
  obj = document.getElementById("dailyLayer");
  obj.style.display = "none";
  obj = document.getElementById("weeklyLayer");
  obj.style.display = "";
  obj = document.getElementById("monthlyLayer");
  obj.style.display = "none";
  obj = document.getElementById("yearlyLayer");
  obj.style.display = "none";
  //recurrenceChange();
  applyOccurrences();
}
function handleClickMonthly()
{
  obj = document.getElementById("dailyLayer");
  obj.style.display = "none";
  obj = document.getElementById("weeklyLayer");
  obj.style.display = "none";
  obj = document.getElementById("monthlyLayer");
  obj.style.display = "";
  obj = document.getElementById("yearlyLayer");
  obj.style.display = "none";
  applyOccurrences();
  var day = parseInt(document.getElementById("monthlyDay").value);
  if (day > 28)
    warning31Days(day);

}
function handleClickYearly()
{
  obj = document.getElementById("dailyLayer");
  obj.style.display = "none";
  obj = document.getElementById("weeklyLayer");
  obj.style.display = "none";
  obj = document.getElementById("monthlyLayer");
  obj.style.display = "none";
  obj = document.getElementById("yearlyLayer");
  obj.style.display = "";
  applyOccurrences();
}

function handleRecurrence() {
  obj = document.getElementById("recurrence");
  frequency = document.getElementsByName("frequency");
    for (i=0; i < frequency.length; i++)
        frequency[i].disabled = !obj.checked;
    document.getElementsByName("weeklySun")[0].disabled = !obj.checked;
    document.getElementsByName("weeklyMon")[0].disabled = !obj.checked;
    document.getElementsByName("weeklyTue")[0].disabled = !obj.checked;
    document.getElementsByName("weeklyWed")[0].disabled = !obj.checked;
    document.getElementsByName("weeklyThu")[0].disabled = !obj.checked;
    document.getElementsByName("weeklyFri")[0].disabled = !obj.checked;
    document.getElementsByName("weeklySat")[0].disabled = !obj.checked;
    document.getElementsByName("meetinmonth")[0].disabled = !obj.checked;
    document.getElementsByName("monthlyDay")[0].disabled = !obj.checked;
    document.getElementsByName("year")[0].disabled = !obj.checked;
    document.getElementsByName("yearlyMonth")[0].disabled = !obj.checked;
    document.getElementsByName("yearlyDay")[0].disabled = !obj.checked;
    document.getElementsByName("range")[0].disabled = !obj.checked;
    document.getElementsByName("range")[1].disabled = !obj.checked;

    updateRange();
    if (obj.checked) {
        //recurrenceChange();
        applyOccurrences();

    } else {
        //setStartDateNow();
        //if (!document.getElementById("endNever").checked)
            //setEndDateNow();
        recurrenceBox = document.getElementById("recurrence");
        recurrenceBox.checked = false;
        document.getElementById("occurrences").value = 1;
        setEndByDateDefault();
    }
}

function updateRange() {
   var enabled = document.getElementById("recurrence").checked;
   var byOccur = document.getElementsByName("range")[0];
   var byDate = document.getElementsByName("range")[1];
   if (!enabled) {
        document.getElementById("occurrences").disabled = true;
        lockEndByDate();
   } else {
        if (byOccur.checked) {
            document.getElementById("occurrences").disabled = false;
            lockEndByDate();
        } else {
            document.getElementById("occurrences").disabled = true;
            unlockEndByDate();
        }
   }
}

function updateConcatType() {
	obj = document.getElementsByName("concatType");
	if (obj && obj[1].checked)
	{
		document.getElementById("vcatRoutingOption").value = true;
	}
	else
	{
		document.getElementById("vcatRoutingOption").value = false;
	}
}

function enableRecurrence(recurrence)
{
    recurrenceBox = document.getElementById(recurrence);
    if(recurrenceBox == null){
    	return;
    }
    recurrenceBox.disabled = false;
    //document.getElementById("occurrences").disabled = false;
}

function disableRecurrence(recurrence)
{
    recurrenceBox = document.getElementById(recurrence);
    if(recurrenceBox == null){
    	return;
    }
    recurrenceBox.checked = false;
    //handleRecurrence();
    recurrenceBox.disabled = true;
    document.getElementById("occurrences").disabled = true;
    updateRange();
    document.getElementsByName("range")[0].disabled = true;
    document.getElementsByName("range")[1].disabled = true;
}


//
// startNowChange() - handles logic for fixing startDate to the current
//   server time (i.e. start the schedule now).
//
// External Functions:
//   unlockStartDate() - calendar.js
//   lockStartDate() - calendar.js
//
function startNowChange(checkbox, startTimeMenu)
{
  var elem = document.getElementById(checkbox);

  if (elem == null)
  {
    return;
    }

  // Check to see if the startNow element is checked.
  if (elem.checked == false)
  {
    unlockStartDate();
    startTimeMenu.unlock();
  }
  else
  {
    lockStartDate();
    startTimeMenu.lock();
  }
  updateDuration();

} /* startNowChange */

//
// endNeverChange() - handles logic for fixing endDate to infinity (i.e. a
//   schedule that never ends).
//
// External Functions:
//   unlockEndDate() - calendar.js
//   lockEndDate() - calendar.js
//
function endNeverChange(checkbox, endTimeMenu, recurrence)
{
  var elem = document.getElementById(checkbox);

  if (elem == null)
  {
    return;
  }
  // Check to see if the endNever element is checked.
  if (elem.checked == false)
  {
    lockDuration(false);
    unlockEndDate();
    endTimeMenu.unlock();
    applyOccurrences();
    enableRecurrence(recurrence);
    updateDuration();
  }
  else
  {
    lockDuration(true);
    lockEndDate();
    endTimeMenu.lock();
    disableRecurrence(recurrence);
    document.getElementById("duration").value="";
  }
  //document.getElementById("occurrences").disabled = elem.checked;

} /* endNeverChange */

function lockDuration(locked)
{
    elem = document.getElementById("duration");
    if (elem != null) {
        elem.disabled = locked;
    }
}

var oneMinute = 1000 * 60;
var oneHour = oneMinute * 60;
var oneDay = oneHour * 24;
var oneWeek = oneDay * 7;
var oneYear = oneDay * 365;

/*
 parameters: Date startDate, Date endDate
 */
function daysBetween(startDate, endDate)
{
    var DSTAdjust = 0;
    if (endDate > startDate)
        DSTAdjust = (endDate.getTimezoneOffset() - startDate.getTimezoneOffset()) * oneMinute;
    var diff = endDate.getTime() - startDate.getTime() - DSTAdjust;
    if (diff < 0)
        diff = 0;
    return Math.ceil(diff/oneDay);
} /* daysBetween */

function weeksBetween(startDate, endDate)
{
    var diff = Math.abs(endDate.getTime() - startDate.getTime());
    return Math.floor(diff/oneWeek);
} /* weeksBetween */

function weeklyOccurrences(startDate, endDate)
{
    // Count the number of whole weeks between start and end
    var wholeWeeks = weeksBetween(startDate, endDate);
    var frequency = 0;
    if (document.getElementsByName("weeklySun")[0].checked) frequency++;
    if (document.getElementsByName("weeklyMon")[0].checked) frequency++;
    if (document.getElementsByName("weeklyTue")[0].checked) frequency++;
    if (document.getElementsByName("weeklyWed")[0].checked) frequency++;
    if (document.getElementsByName("weeklyThu")[0].checked) frequency++;
    if (document.getElementsByName("weeklyFri")[0].checked) frequency++;
    if (document.getElementsByName("weeklySat")[0].checked) frequency++;
    var occurs = wholeWeeks * frequency;
    var tempDate = new Date(startDate.getTime());
    var day = tempDate.getDate();
    day = day + (wholeWeeks*7);
    tempDate.setDate(day);
    // count the leftover days
    var daysLeft = daysBetween(tempDate, endDate) + 1;
    for (var i=0; i< daysLeft; i++) {
        dayOfWeek = tempDate.getDay();
        if (dayOfWeek == 0 && document.getElementsByName("weeklySun")[0].checked) occurs++;
        else if (dayOfWeek == 1 && document.getElementsByName("weeklyMon")[0].checked) occurs++;
        else if (dayOfWeek == 2 && document.getElementsByName("weeklyTue")[0].checked) occurs++;
        else if (dayOfWeek == 3 && document.getElementsByName("weeklyWed")[0].checked) occurs++;
        else if (dayOfWeek == 4 && document.getElementsByName("weeklyThu")[0].checked) occurs++;
        else if (dayOfWeek == 5 && document.getElementsByName("weeklyFri")[0].checked) occurs++;
        else if (dayOfWeek == 6 && document.getElementsByName("weeklySat")[0].checked) occurs++;
        day = tempDate.getDate();
        day++;
        tempDate.setDate(day);
    }
    return occurs;
}

function monthlyOccurrences(startDate, endDate)
{
    var occurs = 0;
    var tempDate = new Date(startDate.getTime());
    while (tempDate.getTime() <= endDate.getTime()) {
        occurs++;
        var month = tempDate.getMonth();
        month++;
        tempDate.setMonth(month);
    }
    return occurs;
}

function yearlyOccurrences(startDate, endDate)
{
    var occurs = 0;
    var tempDate = new Date(startDate.getTime());
    while (tempDate.getTime() <= endDate.getTime()) {
        occurs++;
        var year = tempDate.getFullYear();
        year++;
        tempDate.setFullYear(year);
    }
    return occurs;
}



function countOccurrences(startDateField, endDateField)
{
    if (!(startDateField && endDateField))
        return;

    var startDate = dCal.getDateObject(startDateField.id);
    if (!startDate)
        return;
    startDate.setHours(0);
    startDate.setMinutes(0);
    startDate.setSeconds(0);
    startDate.setMilliseconds(0);
    var endDate = dCal.getDateObject(endDateField.id);
    if (!endDate)
        return;
    endDate.setHours(0);
    endDate.setMinutes(0);
    endDate.setSeconds(0);
    endDate.setMilliseconds(0);


    obj = document.getElementsByName("frequency");
    if (obj && obj[0].checked)
        return (daysBetween(startDate, endDate) + 1);
    else if (obj && obj[1].checked)
        return weeklyOccurrences(startDate, endDate);
    else if (obj && obj[2].checked)
        return monthlyOccurrences(startDate, endDate);
    else if (obj && obj[3].checked)
        return yearlyOccurrences(startDate, endDate);
    else
        return;
} /* calculateOccurrences */

function updateOccurrences(occurrencesField, startDateField, endByDateField)
{
    var num = countOccurrences(document.getElementById(startDateField), document.getElementById(endByDateField));
    if (num > 0) {
        var occurrences = document.getElementById(occurrencesField);
        if (occurrences.value != num) {
            occurrences.value = num;
        }
    }
}

// update the end date based on daily occurrences
function updateDailyOccurs(num, startDate)
{
    var tempDate = new Date(startDate.getTime());
    if (!num || !startDate)
        return;
    var days = tempDate.getDate();
    /* if ((startDate.getHours() == 23) && (startDate.getMinutes() >= 30))
        days = days + parseInt(num);
    else */
        days = days + parseInt(num) - 1;
    tempDate.setDate(days);
    dCal.setDate(endByDateId, tempDate);
}

// update the end date based on weekly occurrences
function updateWeeklyOccurs(num, startDate)
{
    if (!num || !startDate)
        return;


    var tempDate = new Date(startDate.getTime());
    var dayOfWeek;
    while (num >= 1) {
        dayOfWeek = tempDate.getDay();
        if (dayOfWeek == 0 && document.getElementsByName("weeklySun")[0].checked) num--;
        else if (dayOfWeek == 1 && document.getElementsByName("weeklyMon")[0].checked) num--;
        else if (dayOfWeek == 2 && document.getElementsByName("weeklyTue")[0].checked) num--;
        else if (dayOfWeek == 3 && document.getElementsByName("weeklyWed")[0].checked) num--;
        else if (dayOfWeek == 4 && document.getElementsByName("weeklyThu")[0].checked) num--;
        else if (dayOfWeek == 5 && document.getElementsByName("weeklyFri")[0].checked) num--;
        else if (dayOfWeek == 6 && document.getElementsByName("weeklySat")[0].checked) num--;
        if (num != 0)
        {
            day = tempDate.getDate();
            day++;
            tempDate.setDate(day);
        }
    }
    /* if ((startDate.getHours() == 23) && (startDate.getMinutes() >= 30))
        tempDate.setDate(tempDate.getDate() + 1); */
    dCal.setDate(endByDateId, tempDate);
}

// update the end date based on monthly occurrences
function updateMonthlyOccurs(num, startDate, dayOfMonth)
{
    var tempDate = new Date(startDate.getTime());
    if (!num || !startDate)
        return;
    tempDate.setDate(1);
    var month = tempDate.getMonth();
    month = month + parseInt(num) - 1;
    tempDate.setMonth(month);
    month = tempDate.getMonth();

    if (month == 3 || month == 5 || month == 8 || month == 10) {
        if (dayOfMonth > 30)
            tempDate.setDate(30);
        else
            tempDate.setDate(dayOfMonth);
    } else if (month == 1) {
        if (dayOfMonth > 28)
            tempDate.setDate(28);
        else
            tempDate.setDate(dayOfMonth);
    } else {
        tempDate.setDate(dayOfMonth);
    }
    if ((startDate.getHours() == 23) && (startDate.getMinutes() >= 30))
        tempDate.setDate(tempDate.getDate() + 1);
    dCal.setDate(endByDateId, tempDate);
}

// update the end date based on yearly occurrences
function updateYearlyOccurs(num, startDate)
{
    var tempDate = new Date(startDate.getTime());
    if (!num || !startDate)
        return;
    var year = tempDate.getFullYear();
    year = year + parseInt(num) - 1;
    tempDate.setFullYear(year);
    if ((startDate.getHours() == 23) && (startDate.getMinutes() >= 30))
        tempDate.setDate(tempDate.getDate() + 1);
    dCal.setDate(endByDateId, tempDate);
}

// update the yearlyDate fields on change
function updateYearlyDate()
{
    var date = document.getElementById("yearlyDay");
    var month = document.getElementsByName("yearlyMonth")[0];
    if (!date || !month) return;
    var newDate = date.value;
    // months start at 1 for this form! whereas Date object, month starts at 0
    if ((month.value == 1) && (parseInt(date.value) > 28)) {
        date.value = "28";
    } else if ((month.value == 3 || month.value == 5 || month.value == 8 || month.value == 10) && (parseInt(date.value) > 30)) {
        date.value = "30";
    }
    startDateField = document.getElementById("startdate");
    if (startDateField)
    {
        var startDate = dCal.getDateObject(startDateId);
        startDate.setDate(date.value);
        startDate.setMonth(parseInt(month.value));
        dCal.setDate(startDateId, startDate);
        createSchDateChange();
    }
}

//
// routingMetricChange() - opens the metric value element when needed.
//
function routingMetricChange(parent, change)
{
  var elem = document.getElementById(parent);

  if (elem == null)
  {
    return;
  }

  document.getElementById(change).disabled = (elem.value == "default");
  if (elem.value == "default") {
    document.getElementById(change).value = "";
    }
} /* routingMetricChange */

//
// routingAlgorithmChange() - opens the appropriate metric values based on a
//   routing algorithm change event.
//
function routingAlgorithmChange(parent)
{
  var algorithm = document.getElementById(parent);
  var routingMetric = document.getElementById("routingMetric");
  var routingMetricValue = document.getElementById("metricValue");
  //var srlg = document.getElementById("srlg");

  if (algorithm == null || routingMetric == null ||
      routingMetricValue == null)
  {
    return;
  }

  // If algorithm set to types without options.
  if (algorithm.value == "default" || algorithm.value == "spf")
  {
    routingMetric.disabled = true;
    routingMetric.value = "default";
    routingMetricValue.disabled = true;
    routingMetricValue.value = "";

    //srlg.disabled = true;
    //srlg.value = "";
  }
  else if (algorithm.value == "cspf")
  {
    routingMetric.disabled = false;
    if (routingMetric.value != "default")
    {
      routingMetricValue.disabled = false;
    }
    //srlg.disabled = false;
  }

} /* routingAlgorithmChange */



function modifyDuration(durationId, add){
    var elem = document.getElementById(durationId);
    if (elem != null) {
        if (elem.disabled == true) return;
        if (isNaN(elem.value))
           elem.value = 30;
        var n = new Number(elem.value);
        if (add) {
            n++;
            elem.value = n.valueOf();
            modifyEndTime(elem, add);
        } else if (n > 1) {
            n--;
            elem.value = n.valueOf();
            modifyEndTime(elem, add);
        }
    }
}

function modifyOccurrences(add)
{
    var elem = document.getElementById('occurrences');
    //alert(elem.value);
    if (elem != null) {
        if (elem.disabled == true) return;
        var n = new Number(elem.value);
        if (add) {
            n++;
        } else if (n > 1) {
            n--;
        }
        elem.value = n.valueOf();
        applyOccurrences();
    }
}

function checkOccurrences()
{
    recurrence = document.getElementById("recurrence");
    if (recurrence)
    {
        if (recurrence.checked)
        {
            var num = document.getElementById("occurrences").value;
            if (isNaN(num) || parseInt(num) < 1) {
                alertInvalidPattern();
                return false;
            }
            document.getElementsByName("range")[1].checked = true;
            updateRange();
            occurs = countOccurrences(document.getElementById("startdate"), document.getElementById("endByDate"));
            if (occurs > 1)
                return displayOccurrenceWarning(occurs);


            else
                return true;
        }
        else
            return true;
    }
    else
        return true;
}

function monthlyDayChange(elem)
{
    if (elem != null) {
        var day = elem.value;
        if (isNaN(day) || parseInt(day) < 1 || parseInt(day) > 31) {
            alertInvalidPattern();
            return;
        } else {
            applyOccurrences();
              if (parseInt(day) > 28)
                warning31Days(day);
        }
    }
}

function yearlyChange()
{
    var day = document.getElementById("yearlyDay").value;
    if (isNaN(day) || parseInt(day) < 1 || parseInt(day) > 31) {
        alertInvalidPattern();
        return;
    } else {
        applyOccurrences();
    }
}

function applyOccurrences()
{
    
	if(document.getElementById("recurrence") == null){
		return;
	}
	
	if (document.getElementById("endNever").checked || !document.getElementById("recurrence").checked)
        return;

    var num = document.getElementById("occurrences").value;
    if (isNaN(num) || parseInt(num) < 1) {
        alertInvalidPattern();
        return;
    }
    obj = document.getElementsByName("frequency");

    var startDate = dCal.getDateObject(startDateId);
    var startTime = dCal.getTimeObject(startTimeId);
    if (!startDate || !startTime)
        return;
    startDate.setHours(startTime.getHours());
    startDate.setMinutes(startTime.getMinutes());
    startDate.setSeconds(startTime.getSeconds());

    if (obj && obj[0].checked)
        updateDailyOccurs(num, startDate);
    else if (obj && obj[1].checked)
    {
        if (!checkWeeklyPattern()) {
            return;
        }
        updateWeeklyDate();
        updateWeeklyOccurs(num, startDate);
    }
    else if (obj && obj[2].checked)
    {
        var day = document.getElementById('monthlyDay').value;
        updateMonthlyDate(day);
        updateMonthlyOccurs(num, startDate, day);
    }
    else if (obj && obj[3].checked)
    {
        var day = document.getElementById("yearlyDay").value;
        updateYearlyDate();
        updateYearlyOccurs(num, startDate);
    }
    else
        return;
}

function checkWeeklyPattern()
{
    if (document.getElementById("recurrence").checked)
    {
        var obj = document.getElementsByName("frequency");
        if (obj && obj[1].checked)
        {
            if (!document.getElementsByName("weeklySun")[0].checked &&
                    !document.getElementsByName("weeklyMon")[0].checked &&
                    !document.getElementsByName("weeklyTue")[0].checked &&
                    !document.getElementsByName("weeklyWed")[0].checked &&
                    !document.getElementsByName("weeklyThu")[0].checked &&
                    !document.getElementsByName("weeklyFri")[0].checked &&
                    !document.getElementsByName("weeklySat")[0].checked)
            {
                alertInvalidPattern();
                return false;
            }
        }
    }
    return true;
}


function updateMonthlyDate(day)
{

    startDateField = document.getElementById("startdate");
    if (startDateField)
    {
        var startDate = dCal.getDateObject(startDateId);
        var month = startDate.getMonth();

        if ((month.value == 1) && (parseInt(day) > 28)) {
            startDate.setDate(28)
        } else if ((month.value == 3 || month.value == 5 || month.value == 8 || month.value == 10) && (parseInt(day) > 30)) {
            startDate.setDate(30);
        } else {
            startDate.setDate(day);
        }
        dCal.setDate(startDateId, startDate);
    }
}

// Make sure the start date matches with the weekly pattern (i.e. if start on monday, monday must be checked)
// If not, then move the starting day to determine the next day checked (i.e. if start on monday but checked
// wednesday, move start date to wednesday)
function updateWeeklyDate() {
    startDateField = document.getElementById("startdate");
    if (startDateField)
    {
        var startDate = dCal.getDateObject(startDateId);

        var day = startDate.getDay();
        //alert(day);
        if ((day == 0 && !document.getElementsByName("weeklySun")[0].checked) ||
            (day == 1 && !document.getElementsByName("weeklyMon")[0].checked) ||
            (day == 2 && !document.getElementsByName("weeklyTue")[0].checked) ||
            (day == 3 && !document.getElementsByName("weeklyWed")[0].checked) ||
            (day == 4 && !document.getElementsByName("weeklyThu")[0].checked) ||
            (day == 5 && !document.getElementsByName("weeklyFri")[0].checked) ||
            (day == 6 && !document.getElementsByName("weeklySat")[0].checked)) {
            // need to push the start date up to the next checked day
            var checkedDays = new Array(7);
            if (document.getElementsByName("weeklySun")[0].checked) checkedDays[0] = true;
            if (document.getElementsByName("weeklyMon")[0].checked) checkedDays[1] = true;
            if (document.getElementsByName("weeklyTue")[0].checked) checkedDays[2] = true;
            if (document.getElementsByName("weeklyWed")[0].checked) checkedDays[3] = true;
            if (document.getElementsByName("weeklyThu")[0].checked) checkedDays[4] = true;
            if (document.getElementsByName("weeklyFri")[0].checked) checkedDays[5] = true;
            if (document.getElementsByName("weeklySat")[0].checked) checkedDays[6] = true;

            var nextDay = -1;
            // from current day to end of the week
            for (var i=day+1; i < 7; i++) {
                if (checkedDays[i]) {
                    nextDay = i;
                    break;
                }
            }

            // week start of the week to the current day
            if (nextDay == -1) {
                for (var i=0; i < day; i++) {
                    if (checkedDays[i]) {
                        nextDay = i;
                        break;
                    }
                }
            }

            // at least one day is checked off
            if (nextDay != -1) {
                // set the new day
                if (nextDay > day) {
                    // move ahead
                    startDate.setDate(startDate.getDate() + (nextDay - day));
                } else {
                    // more complicated math to determine how far to move since next weekday is behind current weekday
                    startDate.setDate(startDate.getDate() + ((6 - day) + (nextDay+1)) );
                }
                dCal.setDate(startDateId, startDate);
            }
        }
    }
}

function querySched() {
    var startdate = document.getElementById("startdate");
    var starttime = document.getElementById("starttime");
    var enddate = document.getElementById("enddate");
    var endtime = document.getElementById("endtime");
    var srcTna = document.getElementById("srcTna");
    var destTna = document.getElementById("destTna");
    var rate = document.getElementById("rate");
    var billingGroup = document.getElementById("billingGroup");
    var sourceUserGroup = document.getElementById("srcGroup");
    var sourceResGroup = document.getElementById("srcResGroup");
    var destUserGroup = document.getElementById("destGroup");
    var destResGroup = document.getElementById("destResGroup");

    var url = "/drac?action=querySched&t0=" + escape(startdate.value) + "&t1=" + escape(starttime.value) +
        "&t2=" + escape(enddate.value) + "&t3=" + escape(endtime.value) +
        "&src=" + escape(srcTna.value) + "&dest=" + escape(destTna.value) +  "&rate=" + escape(rate.value) +
        "&bg=" + escape(billingGroup.options[billingGroup.selectedIndex].value) +
        "&sug=" + escape(sourceUserGroup.options[sourceUserGroup.selectedIndex].value) +
        "&srg=" + escape(sourceResGroup.options[sourceResGroup.selectedIndex].value) +
        "&dug=" + escape(destUserGroup.options[destUserGroup.selectedIndex].value) +
        "&drg=" + escape(destResGroup.options[destResGroup.selectedIndex].value);

    // optional parameters
    if (document.getElementById("recurrence").checked)
        url = url + "&recur=yes";

    var srcCh = document.getElementById("srcChannel");
    if (srcCh.value != '')
        url = url + "&srcCh=" + escape(srcCh.value);

    var destCh = document.getElementById("destChannel");
    if (destCh.value != '')
        url = url + "&destCh=" + escape(destCh.value);

    var srcVlanId = document.getElementById("srcVlan");
    if (srcVlanId.value != '')
        url = url + "&srcVlanId=" + escape(srcVlanId.value);
        
    var dstVlanId = document.getElementById("dstVlan");
    if (dstVlanId.value != '')
        url = url + "&dstVlanId=" + escape(dstVlanId.value);

    obj = document.getElementsByName("concatType");
    if (obj && obj[1].checked)
        url = url + "&vcatRoutingOption=yes";

    var algorithm = document.getElementById("algorithm");
    if (algorithm.options[algorithm.selectedIndex].value == 'cspf') {
        var routingMetric = document.getElementById("routingMetric");
        var metricValue = document.getElementById("metricValue");
        if (routingMetric.options[routingMetric.selectedIndex].value != 'default') {
            url = url + "&met=" + escape(routingMetric.options[routingMetric.selectedIndex].value) +
                "&mVal=" + escape(metricValue.value);
        }
    }
    var srlg = document.getElementById("srlg");
    if (srlg.value != "") {
        url = url + "&srlg=" + escape(srlg.value);
    }
    var srsg = document.getElementById("srsg");
    if (srsg.value != "") {
        url = url + "&srsg=" + escape(srsg.value);
    }

    var protection = document.getElementById("protectionType");
    if (protection.options[protection.selectedIndex].value != 'UNPROTECTED') {
        url = url + "&prot=" + escape(protection.options[protection.selectedIndex].value);
    }

    new AJAXInteraction(url, processQuerySchedResponse).doGet();
}

function adjustSrcResGroups(userGroupSelect) {
    var url = "/drac?action=getResGrpForUserGrp&gid=" + escape(userGroupSelect.value);
   
    new AJAXInteraction(url, processSrcResGroupResponse).doGet();
}


function processSrcResGroupResponse(responseXML) {
	//alert (new XMLSerializer()).serializeToString(responseXML));
    srcResGroupSelect = document.getElementById("srcResGroup");
    if(srcResGroupSelect == null || srcResGroupSelect.type =="hidden"){
    	 var currValue = document.getElementById("srcGroup").value;
    	 adjustSrcTNAList('srcResGroup', 'srcLayer', 'srcSiteBox');
	}else{
	    var currValue = srcResGroupSelect.value;
	    var currIndex = 0;
	    srcResGroupSelect.options.length = 0;
	    var groups = responseXML.getElementsByTagName("group");
	    if (groups.length > 0) {
	        for (var i=0; i < groups.length; i++) {
	            resGroup = groups[i].firstChild.nodeValue;
	            srcResGroupSelect.options[i] = new Option(resGroup,resGroup);
	            if (resGroup == currValue) currIndex = i;
	        }
	        srcResGroupSelect.selectedIndex = currIndex;
	        disablePortComboBox('srcTna', false);
	        disableResourceGroupBox('srcResGroup', false);
	        document.getElementById('srcLayer').disabled = false;
	        adjustSrcSiteList('srcResGroup');
	        adjustSrcTNAList('srcResGroup', 'srcLayer', 'srcSiteBox');
	    } else {
	        disableChannelComboBox('srcChannel', true);
	        disablePortComboBox('srcTna', true);
	        disableResourceGroupBox('srcResGroup', true);
	        document.getElementById('srcLayer').disabled = true;
	    }
    }
}

function adjustDestResGroups(userGroupSelect) {
    var url = "/drac?action=getResGrpForUserGrp&gid=" + escape(userGroupSelect.value);
    new AJAXInteraction(url, processDestResGroupResponse).doGet();
}

function processDestResGroupResponse(responseXML) {
    destResGroupSelect = document.getElementById("destResGroup");
    if(destResGroupSelect == null|| destResGroupSelect.type =="hidden"){
    	 var currValue = document.getElementById("destGroup").value;
    	 adjustDestTNAList('destResGroup', 'destLayer', 'destSiteBox');
    }else{
    var currValue = destResGroupSelect.value;
    var currIndex = 0;
    destResGroupSelect.options.length = 0;
    var groups = responseXML.getElementsByTagName("group");
    if (groups.length > 0) {

        for (var i=0; i < groups.length; i++) {
            resGroup = groups[i].firstChild.nodeValue;
            destResGroupSelect.options[i] = new Option(resGroup,resGroup);
            if (resGroup == currValue) currIndex = i;
        }
        destResGroupSelect.selectedIndex = currIndex;
        disableResourceGroupBox('destResGroup', false);
        disablePortComboBox('destTna', false);
        document.getElementById('destLayer').disabled = false;
        adjustDestSiteList('destResGroup');
        adjustDestTNAList('destResGroup', 'destLayer', 'destSiteBox');

    } else {
        disableChannelComboBox('destChannel', true);
        disablePortComboBox('destTna', true);
        disableResourceGroupBox('destResGroup', true);
        document.getElementById('destLayer').disabled = true;
    }
    }
}

function adjustSrcSiteList(resGroupId) {
    var resGroupSelect = document.getElementById(resGroupId);
    var url = "/drac?action=getSiteForResGrp&gid=" + escape(resGroupSelect.value);
    new AJAXInteraction(url, processSrcSiteResponse).doGet();
}

function processSrcSiteResponse(responseXML) {
    var siteSelect = document.getElementById("srcSiteBox");
    var allOption = siteSelect.options[0];
    siteSelect.options.length = 0;
    siteSelect.options[0] = allOption;
    var sites = responseXML.getElementsByTagName("site");
    if (sites.length > 0) {
        for (var i=0; i < sites.length; i++) {
            site = sites[i].firstChild.nodeValue;
            siteSelect.options[i+1] = new Option(site,site);
        }
    }
}

function adjustDestSiteList(resGroupId) {
    var resGroupSelect = document.getElementById(resGroupId);
    var url = "/drac?action=getSiteForResGrp&gid=" + escape(resGroupSelect.value);
    new AJAXInteraction(url, processDestSiteResponse).doGet();
}

function processDestSiteResponse(responseXML) {
    var siteSelect = document.getElementById("destSiteBox");
    var allOption = siteSelect.options[0];
    siteSelect.options.length = 0;
    siteSelect.options[0] = allOption;
    var sites = responseXML.getElementsByTagName("site");
    if (sites.length > 0) {
        for (var i=0; i < sites.length; i++) {
            site = sites[i].firstChild.nodeValue;
            siteSelect.options[i+1] = new Option(site,site);
        }
    }
}

function adjustSrcTNAList(resGroupId, layerId, siteId) {
	if(document.getElementById('srcFacLabel')!=null){
		document.getElementById('srcFacLabel').value = "";
	}
    resGroupSelect = document.getElementById(resGroupId);
    layerSelect = document.getElementById(layerId);
    siteSelect = document.getElementById(siteId);
    var layerValue = "";
    if(layerSelect ==null || layerSelect.value== ""){
    	layerValue="layer2";
    }else{
    	layerValue = layerSelect.value;
    }
    if (layerValue != "layer0") {
        var url = "/drac?action=getTnaForResGrpByLayer&gid=" + escape(resGroupSelect.value) + "&layer=" + escape(layerValue) + "&site=" + escape(siteSelect.value);
        new AJAXInteraction(url, processSrcTNAResponse).doGet();
    } else {
        var url = "/drac?action=getWavelengthForResGrp&gid=" + escape(resGroupSelect.value) + "&site=" + escape(siteSelect.value);
        new AJAXInteraction(url, processSrcWavelengthResponse).doGet();
    }
    if(layerSelect!=null){
    	onLayerChange("src", layerSelect.value);
    }
}

function processSrcTNAResponse(responseXML) {
    srcTnaSelect = document.getElementById("srcTna");
    var currValue = srcTnaSelect.value;
    var currIndex = 0;
    var theOptions = new Array();
    srcTnaSelect.options.length = 0;

    var tnas = responseXML.getElementsByTagName("tna");
    if (document.getElementById('srcLayer')!=null && document.getElementById('srcLayer').value == 'layer2') {
        disableChannelComboBox('srcChannel', true);
    }
    if (tnas.length > 0) {
    	if(document.getElementById('srcLayer')!=null && document.getElementById('srcLayer').value == 'layer2'){
    		disablePortComboBox('srcTna', false);
    	}
        for (var i=0; i < tnas.length; i++) {
      	
            compoundTnaLabel = tnas[i].firstChild.nodeValue;

            arr = compoundTnaLabel.split("::");
            tna = replaceAll(arr[0],ENCODED_COLON,UNENCODED_COLON);

            // facLabel
            var physicalName = tna;
            var logicalName = "";
            if (arr.length > 1){
            	logicalName = replaceAll(arr[1],ENCODED_COLON,UNENCODED_COLON);
            }else{
            	logicalName = "";
            }
            srcFacLabelList[i] = tna;
            
            var optionToShowUser = tna;
            if(logicalName=="" || logicalName == "N/A"){
            	optionToShowUser = tna;
        	}else{
        		optionToShowUser = logicalName;
        	}
            theOptions[i] = new Option(optionToShowUser,tna);
            if (tna == currValue) currIndex = i;            
        }        

        for(var x = 0; x < theOptions.length - 1; x++){
            for(var y =(x + 1); y < theOptions.length; y++){
               if(theOptions[x].text > theOptions[y].text){
                  var tempTx = theOptions[x].text;
                  var tempVx = theOptions[x].value;
                  var tempTy = theOptions[y].text;
                  var tempVy = theOptions[y].value;                  
                  var tempLabelx = srcFacLabelList[x];
                  var tempLabely = srcFacLabelList[y];
                  
                  theOptions[x].text =  tempTy;
                  theOptions[x].value = tempVy;
                  
                  theOptions[y].text =  tempTx;
                  theOptions[y].value = tempVx;
                  
                  srcFacLabelList[x] = tempLabely;
                  srcFacLabelList[y] = tempLabelx;        
               }
            }
         }  
       
        for(var x = 0; x < theOptions.length ; x++){
        	srcTnaSelect.options[x] = theOptions[x];
        	if(srcTnaSelect.options[x].value == currValue){
        		currIndex = i; 
        	}
        }
        var fixedValueEl = document.getElementById('fixedSrcLayer');
        if(fixedValueEl!=null){
        	var srcLayerN = fixedValueEl.value;
        }else{
        	var srcLayerN = document.getElementById('srcLayer').value;
        }
        
        srcTnaSelect.selectedIndex = currIndex;
        if (srcLayerN == 'layer1')  {
            adjustSrcChannelList('srcLayer', 'srcTna');
        }  else if (srcLayerN == 'layer0') {
            var srcWavelengthSelect = document.getElementById("srcWavelengthBox");
            var destWavelengthSelect = document.getElementById("destWavelengthBox");
            if ((destWavelengthSelect.options.length != 0) && (destWavelengthSelect.value != srcWavelengthSelect.value)) {
                if (selectContains(destWavelengthSelect, srcWavelengthSelect.value)) {
                    destWavelengthSelect.value = srcWavelengthSelect.value;
                } else {
                    showWavelengthWarning(srcWavelengthSelect.value, false);
                }
                adjustDestTNAListForWavelength('destResGroup', 'destWavelengthBox', 'destSiteBox');
            }
        }
        if(document.getElementById('srcFacLabel')!=null){
        	adjustFacLabel('src');
        }
    } else {
        disablePortComboBox('srcTna', true);
        disableChannelComboBox('srcChannel', true);
    }
}
function sortSelectListBoxx(listId) {
   var box = document.getElementById(listId);
   var temp_opts = new Array();
   var temp = new Object();
   for(var i = 0; i < box.options.length; i++){
      temp_opts[i] = box.options[i];
   }
   for(var x = 0; x < temp_opts.length - 1; x++){
      for(var y =(x + 1); y < temp_opts.length; y++){
         if(temp_opts[x].text > temp_opts[y].text){
            tempT = temp_opts[x].text;
            tempV = temp_opts[x].value;
            temp_opts[x].text = temp_opts[y].text;
            temp_opts[x].value = temp_opts[y].value;
            temp_opts[y].text = tempT;
            temp_opts[y].value = tempV;
         }
      }
   }
   for(var i = 0; i < box.options.length; i++){
      box.options[i].text = temp_opts[i].text;
      box.options[i].value = temp_opts[i].value;
   }
} 

function adjustSrcTNAListForWavelength(resGroupId, wavelengthId, siteId) {
    var resGroupSelect = document.getElementById(resGroupId);
    var wavelengthSelect = document.getElementById(wavelengthId);
    var siteSelect = document.getElementById(siteId);
    var url = "/drac?action=getTnaForResGrpByWL&gid=" + escape(resGroupSelect.value) + "&wavelength=" + escape(wavelengthSelect.value) + "&site=" + escape(siteSelect.value);
    new AJAXInteraction(url, processSrcTNAResponse).doGet();
}

function processSrcWavelengthResponse(responseXML) {
    var srcWavelengthSelect = document.getElementById("srcWavelengthBox");
    var currValue = srcWavelengthSelect.value;
    var currIndex = 0;
    srcWavelengthSelect.options.length = 0;
    var wavelengths = responseXML.getElementsByTagName("wavelength");
    if (wavelengths.length > 0) {
        for (var i=0; i < wavelengths.length; i++) {
            wavelength = wavelengths[i].firstChild.nodeValue;
            srcWavelengthSelect.options[i] = new Option(wavelength,wavelength);
            if (wavelength == currValue) currIndex = i;
        }
        srcWavelengthSelect.selectedIndex = currIndex;
        disableWavelengthComboBox('srcWavelengthBox', false);
        adjustSrcTNAListForWavelength('srcResGroup', 'srcWavelengthBox', 'srcSiteBox');
    } else {
        disablePortComboBox('srcTna', true);
        disableWavelengthComboBox('srcWavelengthBox', true);
    }
}


function adjustDestTNAList(resGroupId, layerId, siteId) 
{
	if(document.getElementById('destFacLabel') != null){
		document.getElementById('destFacLabel').value = "";
	}
    var resGroupSelect = document.getElementById(resGroupId);
    var layerValue = "";
    if(layerSelect ==null || layerSelect.value== ""){
    	layerValue="layer2";
    }else{
    	layerValue = layerSelect.value;
    }
    var siteSelect = document.getElementById(siteId);
    if (layerValue != "layer0") {
        var url = "/drac?action=getTnaForResGrpByLayer&gid=" + escape(resGroupSelect.value) + "&layer=" + escape(layerValue) + "&site=" + escape(siteSelect.value);
        new AJAXInteraction(url, processDestTNAResponse).doGet();
    } else {
        var url = "/drac?action=getWavelengthForResGrp&gid=" + escape(resGroupSelect.value) + "&site=" + escape(layerValue);
        new AJAXInteraction(url, processDestWavelengthResponse).doGet();
    }
    if(layerSelect!=null){
    	onLayerChange("dst", layerSelect.value);
    }
}

function onLayerChange(orientation, layer) 
{

    if (layer == "layer2")
    {
        // Consider an ajax call here to determine if L2 is EPL or L2SS
        if (orientation == "src")
        {
        	document.getElementById("srcVlanTd1").style.display = "";
	        document.getElementById("srcVlanTd2").style.display = "";
        }
        else
        {
	        document.getElementById("destVlanTd1").style.display = "";
	        document.getElementById("destVlanTd2").style.display = "";
        }
    }
    else
    {
        if (orientation == "src")
        {
        	document.getElementById("srcVlanTd1").style.display = "none";
	        document.getElementById("srcVlanTd2").style.display = "none";
        }
        else
        {
	        document.getElementById("destVlanTd1").style.display = "none";
	        document.getElementById("destVlanTd2").style.display = "none";
        }
    }
    
    if (layer == "layer0") {
        document.getElementById("srcWavelengthTd1").style.display = "";
        document.getElementById("srcWavelengthTd2").style.display = "";
        document.getElementById("srcChannelTd1").style.display = "none";
        document.getElementById("srcChannelTd2").style.display = "none";

        document.getElementById("destWavelengthTd1").style.display = "";
        document.getElementById("destWavelengthTd2").style.display = "";
        document.getElementById("destChannelTd1").style.display = "none";
        document.getElementById("destChannelTd2").style.display = "none";

        // set src and dest layer to both layer0
        if (document.getElementById("destLayer").value != "layer0") {
            document.getElementById("destLayer").value = "layer0";
            adjustDestTNAList('destResGroup', 'destLayer', 'destSiteBox');
        }

        if (document.getElementById("srcLayer").value != "layer0") {
            document.getElementById("srcLayer").value = "layer0";
            adjustSrcTNAList('srcResGroup', 'srcLayer', 'srcSiteBox');
        }

    } else {
        document.getElementById("srcWavelengthTd1").style.display = "none";
        document.getElementById("srcWavelengthTd2").style.display = "none";
        document.getElementById("srcChannelTd1").style.display = "";
        document.getElementById("srcChannelTd2").style.display = ""

        document.getElementById("destWavelengthTd1").style.display = "none";
        document.getElementById("destWavelengthTd2").style.display = "none";
        document.getElementById("destChannelTd1").style.display = "";
        document.getElementById("destChannelTd2").style.display = "";

        // do not allow src and dest layer to be layer0
        if (document.getElementById("destLayer").value == "layer0") {
            document.getElementById("destLayer").value = "layer2";
            adjustDestTNAList('destResGroup', 'destLayer', 'destSiteBox');
        }

        if (document.getElementById("srcLayer").value == "layer0") {
            document.getElementById("srcLayer").value = "layer2";
            adjustSrcTNAList('srcResGroup', 'srcLayer', 'srcSiteBox');
        }
    }
}

function processDestTNAResponse(responseXML) {
    destTnaSelect = document.getElementById("destTna");
    var currValue = destTnaSelect.value;
    var currIndex = 0;
    destTnaSelect.options.length = 0;
    var tnas = responseXML.getElementsByTagName("tna");
    var theOptions = new Array();
    if (document.getElementById('destLayer')!=null && document.getElementById('destLayer').value == 'layer2') {
        disableChannelComboBox('destChannel', true);
    }

    if (tnas.length > 0) {
    	if(document.getElementById('destLayer')!=null && document.getElementById('destLayer').value == 'layer2'){
    		disablePortComboBox('destTna', false);
    	}
        for (var i=0; i < tnas.length; i++) {
            compoundTnaLabel = tnas[i].firstChild.nodeValue;

            arr = compoundTnaLabel.split("::");
            tna = replaceAll(arr[0],ENCODED_COLON,UNENCODED_COLON);

            // facLabel
            var physicalName = tna;
            var logicalName = "";
            if (arr.length > 1){
            	logicalName = replaceAll(arr[1],ENCODED_COLON,UNENCODED_COLON);
            }else{
            	logicalName = "";
            }
            destFacLabelList[i] = tna;
            var optionToShowUser = tna;
            if(logicalName == "" || logicalName == "N/A"){
            	optionToShowUser = tna;
        	}else{
        		optionToShowUser = logicalName;
        	}
            theOptions[i] = new Option(optionToShowUser,tna);
        }

        for(var x = 0; x < theOptions.length - 1; x++){
            for(var y =(x + 1); y < theOptions.length; y++){
               if(theOptions[x].text > theOptions[y].text){
                  var tempTx = theOptions[x].text;
                  var tempVx = theOptions[x].value;
                  var tempTy = theOptions[y].text;
                  var tempVy = theOptions[y].value;                  
                  var tempLabelx = destFacLabelList[x];
                  var tempLabely = destFacLabelList[y];
                  
                  theOptions[x].text =  tempTy;
                  theOptions[x].value = tempVy;
                  
                  theOptions[y].text =  tempTx;
                  theOptions[y].value = tempVx;
                  
                  destFacLabelList[x] = tempLabely;
                  destFacLabelList[y] = tempLabelx;        
               }
            }
         }        
        for(var x = 0; x < theOptions.length; x++){
        	destTnaSelect.options[x] = theOptions[x];
        	if(destTnaSelect.options[x].value == currValue){
        		currIndex = i; 
        	}
        }
        destTnaSelect.selectedIndex = currIndex;

        var fixedValueEl = document.getElementById('fixedDestLayer');
        if(fixedValueEl!=null){
        	var destLayerN = fixedValueEl.value;
        }else{
        	var destLayerN = document.getElementById('destLayer').value;
        }
        if (destLayerN == 'layer1'){
            adjustDestChannelList('destLayer', 'destTna');
        } else if (destLayerN == 'layer0') {
            var srcWavelengthSelect = document.getElementById("srcWavelengthBox");
            var destWavelengthSelect = document.getElementById("destWavelengthBox");
            if ((srcWavelengthSelect.options.length != 0) && (srcWavelengthSelect.value != destWavelengthSelect.value)) {
                if (selectContains(srcWavelengthSelect, destWavelengthSelect.value)) {
                    srcWavelengthSelect.value = destWavelengthSelect.value;
                } else {
                    showWavelengthWarning(destWavelengthSelect.value, true);
                }
                adjustSrcTNAListForWavelength('srcResGroup', 'srcWavelengthBox', 'srcSiteBox');
            }
        }
        if(document.getElementById('destFacLabel')!=null){
        	adjustFacLabel('dest');
        }
    } else  {
        disablePortComboBox('destTna', true);
        disableChannelComboBox('destChannel', true);
    }
}

function processDestWavelengthResponse(responseXML) {
    var destWavelengthSelect = document.getElementById("destWavelengthBox");
    if(destWavelengthSelect == null){
    	adjustDestTNAListForWavelength('destResGroup', 'destWavelengthBox', 'destSiteBox');
    }else{
        var currValue = destWavelengthSelect.value;
        var currIndex = 0;
        destWavelengthSelect.options.length = 0;
        var wavelengths = responseXML.getElementsByTagName("wavelength");
    	
	    if (wavelengths.length > 0) {
	        for (var i=0; i < wavelengths.length; i++) {
	            wavelength = wavelengths[i].firstChild.nodeValue;
	            destWavelengthSelect.options[i] = new Option(wavelength,wavelength);
	            if (wavelength == currValue) currIndex = i;
	        }
	        destWavelengthSelect.selectedIndex = currIndex;
	        disableWavelengthComboBox('destWavelengthBox', false);
	        adjustDestTNAListForWavelength('destResGroup', 'destWavelengthBox', 'destSiteBox');
	    } else {
	        disablePortComboBox('destTna', true);
	        disableWavelengthComboBox('destWavelengthBox', true);
	    }
	}
}

function adjustDestTNAListForWavelength(resGroupId, wavelengthId, siteId) {
	
    var resGroupSelect = document.getElementById(resGroupId);
    if(document.getElementById(wavelengthId)==null){
    	var wavelengthSelect = "";
    }else{    
    	var wavelengthSelect = document.getElementById(wavelengthId);
    }
    var siteSelect = document.getElementById(siteId);
    var url = "/drac?action=getTnaForResGrpByWL&gid=" + escape(resGroupSelect.value) + "&wavelength=" + escape(wavelengthSelect.value) + "&site=" + escape(siteSelect.value);
    new AJAXInteraction(url, processDestTNAResponse).doGet();
}

function adjustFacLabel(orient)
{
   if (orient=='src')
   {
      portSelect = document.getElementById('srcTna');
      document.getElementById('srcFacLabel').value = srcFacLabelList[portSelect.selectedIndex];
   }
   else
   {
      portSelect = document.getElementById('destTna');
      document.getElementById('destFacLabel').value = destFacLabelList[portSelect.selectedIndex];
   }

}

function adjustSrcChannelList(layerId, portId) 
{
    adjustFacLabel('src');

    layerSelect = document.getElementById(layerId);
    if (layerSelect.value == 'layer1') {
        portSelect = document.getElementById(portId);

        var url = "/drac?action=getChannelsForTna&tna=" + escape(portSelect.value);
        new AJAXInteraction(url, processSrcChannelResponse).doGet();
    }
}

function processSrcChannelResponse(responseXML) {
    srcChannelSelect = document.getElementById("srcChannel");
    var currValue = srcChannelSelect.value;
    var currIndex = 0;
    var channels = responseXML.getElementsByTagName("channel");
    disableChannelComboBox('srcChannel', false);
    if (channels.length > 0) {
        for (var i=0; i < channels.length; i++) {
            channel = channels[i].firstChild.nodeValue;
            srcChannelSelect.options[i+1] = new Option(channel,channel);
            if (channel == currValue) currIndex = i+1;
        }
        srcChannelSelect.selectedIndex = currIndex;
    }
}


function adjustDestChannelList(layerId, portId) 
{
    adjustFacLabel('dest');

    layerSelect = document.getElementById(layerId);
    if (layerSelect.value == 'layer1') {
        portSelect = document.getElementById(portId);
        var url = "/drac?action=getChannelsForTna&tna=" + escape(portSelect.value);
        new AJAXInteraction(url, processDestChannelResponse).doGet();
    }
}

function processDestChannelResponse(responseXML) {
    destChannelSelect = document.getElementById("destChannel");
    var currValue = destChannelSelect.value;
    var currIndex = 0;
    destChannelSelect.options.length = 0;
    var channels = responseXML.getElementsByTagName("channel");
    disableChannelComboBox('destChannel', false);
    if (channels.length > 0) {
        for (var i=0; i < channels.length; i++) {
            channel = channels[i].firstChild.nodeValue;
            destChannelSelect.options[i+1] = new Option(channel,channel);
            if (channel == currValue) currIndex = i+1;
        }
        destChannelSelect.selectedIndex = currIndex;
    }
}

function querySchedForTime() {
    var duration = document.getElementById("duration");
    var srcTna = document.getElementById("srcTna");
    var destTna = document.getElementById("destTna");
    var rate = document.getElementById("rate");
    var billingGroup = document.getElementById("billingGroup");
    var sourceUserGroup = document.getElementById("srcGroup");
    var sourceResGroup = document.getElementById("srcResGroup");
    var destUserGroup = document.getElementById("destGroup");
    var destResGroup = document.getElementById("destResGroup");
    var concatTypeCCAT = document.getElementById("concatTypeCCAT");
    var concatTypeVCAT = document.getElementById("concatTypeVCAT");
    
    var srcVlanId = document.getElementById("srcVlan");
    var destVlanId =  document.getElementById("dstVlan");
    var concatType = "VCAT";
    if(concatTypeCCAT.checked){
    	concatType = "CCAT";
    }
    var url = "/drac?action=querySchedForTime&src=" + escape(srcTna.value) + "&dest=" + escape(destTna.value) +
        "&rate=" + escape(rate.value) + "&dur=" + escape(duration.value) +
        "&bg=" + escape(billingGroup.options[billingGroup.selectedIndex].value) +
        "&sug=" + escape(sourceUserGroup.options[sourceUserGroup.selectedIndex].value) +
        "&srg=" + escape(sourceResGroup.options[sourceResGroup.selectedIndex].value) +
        "&dug=" + escape(destUserGroup.options[destUserGroup.selectedIndex].value) +
        "&drg=" + escape(destResGroup.options[destResGroup.selectedIndex].value)+        
        "&srcVlanId=" + escape(srcVlanId.options[srcVlanId.selectedIndex].value)+
        "&dstVlanId=" + escape(destVlanId.options[destVlanId.selectedIndex].value)+        
        "&concattype=" + concatType;
    // optional parameters
    if (document.getElementById("recurrence").checked)
        url = url + "&recur=yes";

    var srcCh = document.getElementById("srcChannel");
    if (srcCh.value != '')
        url = url + "&srcCh=" + escape(srcCh.value);

    var destCh = document.getElementById("destChannel");
    if (destCh.value != '')
        url = url + "&destCh=" + escape(destCh.value);

    var algorithm = document.getElementById("algorithm");
    if (algorithm.options[algorithm.selectedIndex].value == 'cspf') {
        var routingMetric = document.getElementById("routingMetric");
        var metricValue = document.getElementById("metricValue");
        if (routingMetric.options[routingMetric.selectedIndex].value != 'default') {
            url = url + "&met=" + escape(routingMetric.options[routingMetric.selectedIndex].value) +
                "&mVal=" + escape(metricValue.value);
        }
    }
    var srlg = document.getElementById("srlg");
    if (srlg.value != "") {
        url = url + "&srlg=" + escape(srlg.value);
    }
    var srsg = document.getElementById("srsg");
    if (srsg.value != "") {
        url = url + "&srsg=" + escape(srsg.value);
    }

    var protection = document.getElementById("protectionType");
    if (protection.options[protection.selectedIndex].value != 'UNPROTECTED') {
        url = url + "&prot=" + escape(protection.options[protection.selectedIndex].value);
    }
    new AJAXInteraction(url, processQuerySchedForTimeResponse).doGet();
}

function lockEndByDate()
{
  var endByDateField = document.getElementById(endByDateId);
  setEndByDateDefault();
  dCal.lock(endByDateId);

  // Disable editing the fields.
  endByDateField.disabled = true;
} /* lockEndByDate */

function unlockEndByDate()
{
    dCal.unlock(endByDateId);
    var endByDateField = document.getElementById(endByDateId);
    //setEndByDateDefault();

    // enable editing the fields.
    endByDateField.disabled = false;
} /* unlockEndByDate */

function setEndByDateDefault() {
    var start = dCal.getDateObject(startDateId);
    dCal.setDate(endByDateId, start);
}

function modifyEndTime(durationField){
    var date = dCal.getDateObject(startDateId);
    var time = dCal.getTimeObject(startTimeId);
    date.setHours(time.getHours());
    date.setMinutes(time.getMinutes());
    date.setSeconds(0);
    var nrMinutes = durationField.value
    if (isNaN(nrMinutes)){
    	nrMinutes = 30;
    }
    var newTime = date.getTime();
    newTime += new Number(durationField.value) * 60 *1000;
    var newDate = new Date();
    newDate.setTime(newTime);
    dCal.setDate(endDateId, newDate);
    dCal.setTime(endTimeId, newDate);
}

//
// updateDuration - adjust the duration field based on current times.
//
/*private*/ function updateDuration()
{
    var startDate = dCal.getDateObject(startDateId);
    var startTime = dCal.getTimeObject(startTimeId);
    var endDate = dCal.getDateObject(endDateId);
    var endTime = dCal.getTimeObject(endTimeId);

  if (startDate == null || startTime == null ||
      endDate == null || endTime == null)
  {
    return;
  }

  var tmpStart;
  var tmpEnd;

  tmpStart = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate(), startTime.getHours(), startTime.getMinutes(), startTime.getSeconds(), 0).getTime();
  tmpEnd = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate(), endTime.getHours(), endTime.getMinutes(), endTime.getSeconds(), 0).getTime();

  // Determine minute difference.
  var minutes = Math.ceil(Math.abs(tmpEnd - tmpStart) / (1000 * 60));
  //var seconds = Math.abs(tmpEnd - tmpStart) % (1000 * 60) / 1000;

  
  if(document.getElementById("endNever").checked){
	  document.getElementById("duration").value = "";
  }
  else {
	  document.getElementById("duration").value = minutes;
  }
}

function createSchDateChange() {
    checkStartBeforeEnd(dCal.getDateField());
    updateRecurrence();
    updateOccurrences();
    if (dCal.getDateField().id == endByDateId) {
        document.getElementById('occurrences').value = countOccurrences(document.getElementById("startdate"), document.getElementById("endByDate"));

        // don't allow end by date to come before start date
        var endByDate = dCal.getDateObject(endByDateId);
        var startDate = dCal.getDateObject(startDateId);
        if (startDate.getTime() > endByDate.getTime()) {
            dCal.setDate(startDateId, endByDate);
        }
    }
    updateDuration();

}

function createSchTimeChange(elem)
{
  dCal.setTimeString(elem.id, elem.value);
  checkStartBeforeEnd(elem);
  updateDuration();
} /* timeChangeListener */

function updateRecurrence()
{
    if (!document.CreateScheduleForm.weeklySun) return;

    var currentDate = dCal.getDateObject(startDateId);
    var startTime = dCal.getTimeObject(startTimeId);
    var endDate = dCal.getDateObject(endDateId);
    var endTime = dCal.getTimeObject(endTimeId);
    var curDayOfMonth = currentDate.getDate();
    var curMonthOfYear = currentDate.getMonth();


     var dayOfWeek = currentDate.getDay();
      if (dayOfWeek == 0) {
        document.CreateScheduleForm.weeklySun.checked = true;
        document.CreateScheduleForm.weeklyMon.checked = false;
        document.CreateScheduleForm.weeklyTue.checked = false;
        document.CreateScheduleForm.weeklyWed.checked = false;
        document.CreateScheduleForm.weeklyThu.checked = false;
        document.CreateScheduleForm.weeklyFri.checked = false;
        document.CreateScheduleForm.weeklySat.checked = false;
      } else if (dayOfWeek == 1) {
        document.CreateScheduleForm.weeklySun.checked = false;
        document.CreateScheduleForm.weeklyMon.checked = true;
        document.CreateScheduleForm.weeklyTue.checked = false;
        document.CreateScheduleForm.weeklyWed.checked = false;
        document.CreateScheduleForm.weeklyThu.checked = false;
        document.CreateScheduleForm.weeklyFri.checked = false;
        document.CreateScheduleForm.weeklySat.checked = false;
      } else if (dayOfWeek == 2) {
        document.CreateScheduleForm.weeklySun.checked = false;
        document.CreateScheduleForm.weeklyMon.checked = false;
        document.CreateScheduleForm.weeklyTue.checked = true;
        document.CreateScheduleForm.weeklyWed.checked = false;
        document.CreateScheduleForm.weeklyThu.checked = false;
        document.CreateScheduleForm.weeklyFri.checked = false;
        document.CreateScheduleForm.weeklySat.checked = false;
      } else if (dayOfWeek == 3) {
        document.CreateScheduleForm.weeklySun.checked = false;
        document.CreateScheduleForm.weeklyMon.checked = false;
        document.CreateScheduleForm.weeklyTue.checked = false;
        document.CreateScheduleForm.weeklyWed.checked = true;
        document.CreateScheduleForm.weeklyThu.checked = false;
        document.CreateScheduleForm.weeklyFri.checked = false;
        document.CreateScheduleForm.weeklySat.checked = false;
      } else if (dayOfWeek == 4) {
        document.CreateScheduleForm.weeklySun.checked = false;
        document.CreateScheduleForm.weeklyMon.checked = false;
        document.CreateScheduleForm.weeklyTue.checked = false;
        document.CreateScheduleForm.weeklyWed.checked = false;
        document.CreateScheduleForm.weeklyThu.checked = true;
        document.CreateScheduleForm.weeklyFri.checked = false;
        document.CreateScheduleForm.weeklySat.checked = false;
      } else if (dayOfWeek == 5) {
        document.CreateScheduleForm.weeklySun.checked = false;
        document.CreateScheduleForm.weeklyMon.checked = false;
        document.CreateScheduleForm.weeklyTue.checked = false;
        document.CreateScheduleForm.weeklyWed.checked = false;
        document.CreateScheduleForm.weeklyThu.checked = false;
        document.CreateScheduleForm.weeklyFri.checked = true;
        document.CreateScheduleForm.weeklySat.checked = false;
      } else if (dayOfWeek == 6) {
        document.CreateScheduleForm.weeklySun.checked = false;
        document.CreateScheduleForm.weeklyMon.checked = false;
        document.CreateScheduleForm.weeklyTue.checked = false;
        document.CreateScheduleForm.weeklyWed.checked = false;
        document.CreateScheduleForm.weeklyThu.checked = false;
        document.CreateScheduleForm.weeklyFri.checked = false;
        document.CreateScheduleForm.weeklySat.checked = true;
      }

    // set monthly day value to current start date
    var monthlyDay = document.getElementById("monthlyDay");


    if (monthlyDay != null)
    {
        monthlyDay.value = curDayOfMonth;
    }

    // set the yearly day value to current start date
    var yearlyDay = document.getElementById("yearlyDay");
    if (yearlyDay != null)
    {
        yearlyDay.value = curDayOfMonth;
    }

    // set the yearly month option to the current start month
    var yearlyMonth = document.getElementById("yearlyMonth");
    if (yearlyMonth != null)
    {
        yearlyMonth.selectedIndex = curMonthOfYear;
    }
} /* updateRecurrence */

function durationAdd() {
    modifyDuration('duration',true);
}

function durationSub() {
    modifyDuration('duration', false);
}

function addOccurrence() {
    modifyOccurrences(true);
}

function subtractOccurrence() {
    modifyOccurrences(false);
}

function disableComboBoxes(disable) {
     disableUserGroupBox("billingGroup", disable);
     disableUserGroupBox("srcGroup", disable);
     disableUserGroupBox("destGroup", disable);
     disablePortComboBox('srcTna', disable);
     disableResourceGroupBox('srcResGroup', disable);
     disablePortComboBox('destTna', disable);
     disableResourceGroupBox('destResGroup', disable);
     document.getElementById("srcLayer").disabled = disable;
     document.getElementById("destLayer").disabled = disable;
     document.getElementById("srcSiteBox").disabled = disable;
     document.getElementById("destSiteBox").disabled = disable;
     disableChannelComboBox("srcChannel", disable);
     disableChannelComboBox("destChannel", disable);
}

function setTimeForQuery(queryTimeWin, startTime, endTime) {
    if ((startTime) && (endTime)) {
        var d = new Date(parseInt(startTime));

        dCal.setDate(startDateId, d);
        dCal.setTime(startTimeId, d);
        d = new Date(parseInt(endTime));
        dCal.setDate(endDateId, d);
        dCal.setTime(endTimeId, d);
    }
    if (queryTimeWin) {
        queryTimeWin.hide();
    }
}

function setEndByDate(date) {
    document.getElementById('endByDate').value = date;
}

function addDebugOption() {
    var key = document.getElementById("debugInput");
    var val = document.getElementById("debugInput2");
    if ((key && val) && (key.value != "") && (val.value != "")) {
        var sel = document.getElementById("debugOptionsBox");
        sel.options[sel.length] = new Option(key.value + "=" + val.value, key.value + "=" + val.value);
    }
}

function removeDebugOption() {
    var sel = document.getElementById("debugOptionsBox");
    for (var i = sel.length-1; i >= 0; i--) {
       if (sel.options[i].selected) {
           sel.options[i] = null;
        }
    }
}

function clearDebugOption() {
    var key = document.getElementById("debugInput");
    var val = document.getElementById("debugInput2");
    if (key && val) {
        key.value = "";
        val.value = "";
    }
}

function doSubmit() 
{
    var finalSubmissionStatus = false;

    var sel = document.getElementById("debugOptionsBox");
    if (sel) {
        for (var i = 0; i < sel.length; i++) {
            //alert(sel.options[i].value);
            sel.options[i].selected = true;
        }
    }

    finalSubmissionStatus = checkOccurrences();

    if (finalSubmissionStatus)
    {
        var create = document.getElementById("Create");
        if (create != null)
        {
            create.disabled = true; 
        }
    }

    return finalSubmissionStatus;
}
