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

//////////////////////////////////////////////////////////////////////////
///////  THIS FILE IS NOT LONGER USED, ALL FUNCTIONALITY MOVED TO ////////
///////  OBJECT-ORIENTED CALENDAR.JSP & CALENDAR.JS  !!!!!!!!!    ////////
//////////////////////////////////////////////////////////////////////////

//
// DRAC Web GUI version 0.1 Alpha
//
// File: /scripts/calendarNoTime.js
//
// Description:
//   Popup calendar functions supporting internationalization.  Does not
//   support time.
//
// TODO: Functionality needs to be segmented into pure calendar object and
//       separate start and end date logic.
// TODO: Perhaps internationalization can be better configured through
//       properties in a jsp instead of hardcoding it in this js file.
//


//-----------------------------------------------------------
// These variables can be set in the calling HTML page after
// calling the initCalendar function. See the configCalendar
// function for reference.
//-----------------------------------------------------------

// Interface language. Initialize with appropriate language code.
var cal_lang;

// Maximum number of days from today which are allowed for selection.
var cal_daysLimit;

// Date formats.
var cal_dateFormat;

// Long or short format for display.
var cal_format;

// Holds a reference to the initial date.
var cal_initialDate;

//-----------------------------------------------------------
// These variables should not be changed.
//-----------------------------------------------------------

var cal_shownDate;

var cal_duration;  // The time between start and end times.

// Arrays of date cells for better performance.
var c1cells = new Array(43);

// Array to hold week character string.
var cWcells = new Array(7);

// Months to num array.
var m2n = new Array(12);
m2n["Jan"] = "01";
m2n["Feb"] = "02";
m2n["Mar"] = "03";
m2n["Apr"] = "04";
m2n["May"] = "05";
m2n["Jun"] = "06";
m2n["Jul"] = "07";
m2n["Aug"] = "08";
m2n["Sep"] = "09";
m2n["Oct"] = "10";
m2n["Nov"] = "11";
m2n["Dec"] = "12";

// Array to map from an integer to corresponding month.
var n2m = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

// Arrays of month names by language.
var mNames   = new Array(4);
mNames["en"] = new Array("January", "February", "March", "April", "May",
                         "June", "July", "August", "September", "October",
                         "November", "December");
mNames["fr"] = new Array("janvier", "février", "mars", "avril", "mai",
                         "juin", "juillet", "août", "septembre", "octobre",
                         "novembre", "décembre");
mNames["de"] = new Array("Januar", "Februar", "März", "April", "Mai", "Juni",
                         "Juli", "August", "September", "Oktober", "November",
                         "Dezember");
mNames["it"] = new Array("gennaio", "febbraio", "marzo", "aprile", "maggio",
                         "giugno", "luglio", "agosto", "settembre", "ottobre",
                         "novembre", "dicembre");

// Array of week day names by language.
var wNames   = new Array(4);
wNames["en"] = new Array("Sunday", "Monday", "Tuesday", "Wednesday",
                         "Thursday", "Friday", "Saturday");
wNames["fr"] = new Array("dimanche", "lundi", "mardi", "mercredi", "jeudi",
                         "vendredi", "samedi");
wNames["de"] = new Array("Sonntag", "Montag", "Dienstag", "Mittwoch",
                         "Donnerstag", "Freitag", "Samstag");
wNames["it"] = new Array("domenica", "lunedì", "martedì", "mercoledì",
                         "giovedì", "venerdì", "sabato");

// Internationalization array of additional terms.
var minNames   = new Array(4);
minNames["en"] = "minutes";
minNames["fr"] = "minutes";
minNames["de"] = "Minuten";
minNames["it"] = "resoconto";

// Array of week day characters by language.
var wChar   = new Array(4);
wChar["en"] = new Array("S", "M", "T", "W", "T", "F", "S");
wChar["fr"] = new Array("D", "L", "M", "M", "J", "V", "S");
wChar["de"] = new Array("S", "M", "D", "M", "D", "F", "S");
wChar["it"] = new Array("D", "L", "M", "M", "G", "V", "S");

var errorDateValue   = new Array(4);
errorDateValue["en"] = "Please enter a valid date value.";
errorDateValue["fr"] = "Veuillez écrire une valeur valide de date.";
errorDateValue["de"] = "Tragen Sie bitte einen gültigen Datumwert ein.";
errorDateValue["it"] = "Entri prego in un valore valido della data.";

var errorTimeValue   = new Array(4);
errorTimeValue["en"] = "Please enter a valid time value.";
errorTimeValue["fr"] = "Veuillez écrire une valeur de temps valide.";
errorTimeValue["de"] = "Tragen Sie bitte einen Gültigkeitszeitwert ein.";
errorTimeValue["it"] = "Entri prego in un valore di tempo valido.";

// Associative array of drop-downs select by field name.
var selectList = new Object();
var selectListCount = 0;

// Associative array of date and time values.
var dateValue = new Object();
var timeValue = new Object();

// Basic calendar fields.
var startDateField = null;
var endDateField = null;
var currentDateField = null;

// Recurrence support fields.
// Monthly recurrence fields.
var recurrenceMonthlyDayField = null;

// Yearly recurrence fields.
var recurrenceYearlyMonthField = null;
var recurrenceYearlyDayField = null;

// Calendar position variables.
var overCalendar = false;
var inDateField = false;

var overTime = false;
var inTimeField = false;

var calendarStartLocked = false;
var calendarEndLocked = false;
var calendarMaxDate = new Date(3000, 0, 0);

var previousClass;

// Flag set if month change buttons should be hidden.
var isEndReached = false;
var isAtBeginning = true;


//
// Initialize calendar. Call on page load.
//   now - a GMT string representing the current date.
//
function initCalendar(startDate, endDate, language, format, initDate)
{
  configCalendar(language, initDate);

  if (format == "long")
  {
    cal_format = "long";
  }
  else
  {
    cal_format = "short";
  }

  // Cache calendar1 cells for better performance.
  for (var i = 1; i <= 42; i++)
  {
    var elem = document.getElementById("c1c" + i);
    c1cells[i] = elem;
  }

  // Cache week character strings for better performance.
  var elem = null;
  for (var i = 0; i <= 6; i++)
  {
    elem = document.getElementById("cWc" + i);
    cWcells[i] = elem;
  }

  selectList[startDate] = new Array();
  selectList[endDate] = new Array();

  startDateField = document.getElementById(startDate);
  endDateField = document.getElementById(endDate);

}




//
// Configure calendar with default values. These values can be overriden
// by the calling HTML page after it has called initCalendar().
//
/*private*/ function configCalendar(language, initDate)
{
  // Seed the initial date.
  if (initDate == null)
  {
    cal_initialDate = new Date();
  }
  else
  {
    cal_initialDate = initDate;
  }

  // Interface language. Initialize with appropriate language code.
  if (language != null && language != "")
  {
    cal_lang = language;
  }
  else /* default */
  {
    cal_lang = "en"; // default
  }

  // Maximum number of days from today which are allowed for selection.
  cal_daysLimit = 365*10;

  // Date formats.
  cal_dateFormat = new Object();
  cal_dateFormat["en"] = "DD/MM/YYYY";
  cal_dateFormat["fr"] = "JJ/MM/AAAA";
  cal_dateFormat["de"] = "TT/MM/JJJJ";
  cal_dateFormat["it"] = "GG/MM/AAAA";

  cal_dateLongFormat = new Object();
  cal_dateLongFormat["en"] = "Month DD, YYYY";
  cal_dateLongFormat["fr"] = "JJ mois AAAA";
  cal_dateLongFormat["de"] = "TT. Month JJJJ";
  cal_dateLongFormat["it"] = "GG Month AAAA";

  cal_timeFormat = new Object();
  cal_timeFormat["en"] = "h:mm:ss [AM|PM]";
  cal_timeFormat["fr"] = "h:mm:ss";
  cal_timeFormat["de"] = "h:mm:ss";
  cal_timeFormat["it"] = "h.mm.ss";

  cal_shownDate = new Date(cal_initialDate.getFullYear(), cal_initialDate.getMonth(), 1);
}

function isDateEmpty(text)
{
  var result = null;

  if (cal_format == "long")
  {
    result = parseLongDate(text);
  }
  else
  {
    result = parseDate(text);
  }

  return(result == null);
}

function isTimeEmpty(text)
{
  var result = null;

  result = parseTime(text);

  return(result == null);
}

//
// Must be called when the user clicks or tabs into a date input field (onfocus event).
//
function dateFocus(o)
{
  if (o.id == startDateField.id && calendarStartLocked == true)
  {
    return;
  }

  if (o.id == endDateField.id && calendarEndLocked == true)
  {
    return;
  }

  inDateField = true;
  currentDateField = o;

  if (! overCalendar)
  {
    showCalendar(o);
  }

  o.select();
}


//
// Must be called when the user clicks or tabs out of a date input field (onblur event).
//
function dateBlur(o)
{
    inDateField = false;

    if (! overCalendar)
    {
        hideCalendar();
    }
}

function onTimeChange(o) {
  currentTimeField = o;
  timeChange(o);
  currentTimeField = null;
}

//
// Must be called when the content of a date input field changes (onchange event).
//
function dateChange(o)
{
  if (o.value.length == 0)
  {
    o.value = "";
    if (cal_lang == "long")
    {
      o.value = cal_dateLongFormat[cal_lang];
    }
    else
    {
      o.value = cal_dateFormat[cal_lang];
    }
  }

  o.className = "gen";

  var theDate;
  if (cal_format == "long")
  {
    theDate = parseLongDate(o.value);
  }
  else
  {
    theDate = parseDate(o.value);
  }

  if (o.value == cal_dateFormat[cal_lang])
  {
    return;
  }

  if (theDate == null || isDateBeforeToday(theDate) || isDateTooFar(theDate, 1))
  {
    o.className = "invalid";
    alert(errorDateValue[cal_lang]);
    return;
  }

  doAdjustDates(theDate);
}


//
// Must be called when the mouse goes over the calendar (onmouseover event). Will be called
// whenever the mouse changes date cell too.
//
function calendarOver(o) {
    overCalendar = true;
}

function timeOver(o) {
    overTime = true;
}

//
// Must be called when the mouse leaves the calendar (onmouseout event). Will be called
// whenever the mouse changes date cell too.
//
function calendarOut(o) {
//    if (! inDateField)
//        hideCalendar();
    overCalendar = false;
}

function timeOut(o) {
    overCalendar = false;
}

//
// Changes display class when mouse is over element.
//
function mover(target) {
    previousClass = target.className;
    target.className = "over";
}

//
// Restores previous display class when mouse goes off element.
//
function mout(target) {
    target.className = previousClass;
}

//
// Changes display class when mouse is over element.
//
function cover(target) {
    if (Number(target.innerHTML) > 0 && target.className != "past" && target.className != "weekendpast") {
        previousClass = target.className;
        target.className = "over";
    }
}

//
// Restores previous display class when mouse goes off element.
//
function cout(target) {
    if (Number(target.innerHTML) > 0 && target.className != "past" && target.className != "weekendpast") {
        target.className = previousClass;
    }
}

//
// Registers a select drop-down field so it is hidden when the calendar for
// the specified date field is displayed. Required only because of a bug with
// Internet Explorer.
//
function registerSelect(dateId, selectId, condition)
{
    var tmp = new Object();
    tmp.id = selectId;
    tmp.condition = condition;

    selectList[dateId][selectList[dateId].length] = tmp;
}

//
// Display the calendar beneath the current field.
//
function showCalendar(dateField)
{
    var d = dateValue[dateField.id];

    if (d != null)
    {
        cal_shownDate = new Date(d.getFullYear(), d.getMonth(), 1);
    }
    else
    {
        cal_shownDate = new Date(cal_initialDate.getFullYear(), cal_initialDate.getMonth(), 1);
    }

    // Position (hidden) calendar underneath field.
    var c = document.getElementById("cal");
    c.style.position = "absolute";
    c.style.left = findPosX(dateField) + "px";
    c.style.top  = (findPosY(dateField) + 20) + "px";
    c.style.zIndex = 1;

    // Loop through array of select elements to hide.
    var fields = selectList[dateField.id];
    for (var i = 0; i < fields.length; i++)
    {
        if (document.getElementById(fields[i].id) != null)
        {
          // Check to see if the condition is satisfied.
          var condition = document.getElementById(fields[i].condition);
          if (condition == null || (condition != null && condition.checked == false))
          {
        document.getElementById(fields[i].id).style.display = "none";
          }
        }
    }

    // Show calendar.
    document.getElementById("cal").style.display = "block";

    // Display dates in calendar.
    displayDates();
}

function showTime(timeField) {
}

//
// Hide the currently shown calendar.
//
function hideCalendar()
{
    // Hide calendar.
    document.getElementById("cal").style.display = "none";

    // Loop through array of previously hidden select elements.
    if (currentDateField)
    {
        var fields = selectList[currentDateField.id];
        for (var i = 0; i < fields.length; i++)
        {
        if (document.getElementById(fields[i].id) != null)
            {
            document.getElementById(fields[i].id).style.display = "inline";
            }
    }
    }

    // Make sure "over" variable is reset.
    overCalendar = false;
}

function hideTime()
{
    // Make sure "over" variable is reset.
    overTime = false;
}

//
// Move by the specified number of months.
//
function changeMonth(i)
{
    cal_shownDate.setMonth(cal_shownDate.getMonth() + i);
    displayDates();

    currentDateField.focus();
}

//
// Select a date and close calendar.
//
function selectDate(o, monthOffset) {
    if (o.className == "past" || o.className == "weekendpast" || ! Number(o.innerHTML) > 0) {
        currentDateField.focus();
        return;
    }
    var newDate = new Date(cal_shownDate.getFullYear(), cal_shownDate.getMonth() + monthOffset, o.innerHTML);
    doAdjustDates(newDate);
    hideCalendar();
}

//
// Display dates in calendar.
//
/*private*/ function displayDates() {
    var nextMonthDate = new Date(cal_shownDate.getFullYear(), cal_shownDate.getMonth() + 1, 1);

    // Display calendar titles.
    document.getElementById("cal1TitleM").innerHTML = ""; // First set to empty string, required for IE 5 Mac
    document.getElementById("cal1TitleM").innerHTML = mNames[cal_lang][cal_shownDate.getMonth()];
    document.getElementById("cal1TitleY").innerHTML = ""; // First set to empty string, required for IE 5 Mac
    document.getElementById("cal1TitleY").innerHTML = cal_shownDate.getFullYear();

    isEndReached = false;
    isAtBeginning = false;

    displayWeekDays();
    displayMonth(c1cells, cal_shownDate);

    // Hide month navigation as appropriate.
    //document.getElementById("calarrowback").style.display = (isAtBeginning) ? "none" : "block";
    //document.getElementById("calarrowfwd").style.display =  (isEndReached) ? "none" : "block";
}

//
// Display the character representations for the weeks days in calendar.
//
/*private*/ function displayWeekDays()
{
  var cell = null;

  for (var i = 0; i < 7; i++)
  {
    cell = cWcells[i];
    cell.innerHTML = ""; // First set to empty string, required for IE 5 Mac
    cell.innerHTML = wChar[cal_lang][i];

    if (i % 6 == 0)
    {
      cell.className = "weekend";
    }
    else
    {
      cell.className = "";
    }
  }

} // displayWeekDays()

// Constant containing the number of milliseconds in one day, used for date arithmetics.
var msInDay = 24*60*60*1000;

//
// Display dates for specified month.
//
/*private*/ function displayMonth(cells, monthDate) {
    var lastDate = getMonthDays(monthDate);
    var offset = getCalendarOffset(monthDate);
    var cell = null;

    // Wipe first and last rows.
    for (var i = 1; i  <= offset; i++) {
        cell = cells[i];
        cell.innerHTML = ""; // First set to empty string, required for IE 5 Mac
        cell.innerHTML = "&nbsp;";
        if (i % 7 <= 1)
            cell.className = "weekend";
        else
            cell.className = "";
    }
    for (var i = offset + lastDate; i <= 42; i++) {
        cell = cells[i];
        cell.innerHTML = ""; // First set to empty string, required for IE 5 Mac
        cell.innerHTML = "&nbsp;";
        if (i % 7 <= 1)
            cell.className = "weekend";
        else
            cell.className = "";
    }

    // Display dates.
    var isTodayMonth    = isSameMonth(monthDate, cal_initialDate);
    var isSelectedMonth = isSameMonth(monthDate, dateValue[currentDateField.id]);
    var isDate1Month    = isSameMonth(monthDate, dateValue[startDateField.id]);
    var isDate2Month    = isSameMonth(monthDate, dateValue[endDateField.id]);

    for (var i = 1; i <= lastDate; i++) {
        // Display day of month.
        cell = cells[i + offset];
        cell.innerHTML = ""; // First set to empty string, required for IE 5 Mac
        cell.innerHTML = i;

        if (monthDate.getTime() <= cal_initialDate.getTime()) {
            isAtBeginning = true;
        }

        var normalClass = "";
        var pastClass = "";
        var isWeekEnd = ((i + offset) % 7) <= 1;
        if (isWeekEnd) {
            normalClass = "weekend";
            pastClass = "weekend";
        }


        // Select display class.
        if (isSelectedMonth && dateValue[currentDateField.id].getDate() == i) {
            // This is the currently selected date.
            cell.className = "current";
        }

        else if (isTodayMonth && cal_initialDate.getDate() > i) {
            cell.className = pastClass;
            //cell.className = normalClass;
            isAtBeginning = true;
        }
        else if (isDateTooFar(monthDate, i)) {
            // This date is too far in the future.
            cell.className = pastClass;
            //cell.className = normalClass;
            isEndReached = true;
        }
        else {
            cell.className = normalClass;
        }
    }
}

//
// Returns true if specificed date is before today's date.
//
/*private*/ function isDateBeforeToday(theDate)
{
  if (theDate.getFullYear() != cal_initialDate.getFullYear())
  {
    return(theDate.getFullYear() < cal_initialDate.getFullYear());
  }

  if (theDate.getMonth() != cal_initialDate.getMonth())
  {
    return(theDate.getMonth() < cal_initialDate.getMonth());
  }

  return(theDate.getDate() < cal_initialDate.getDate());
}

//
// Returns true if specificed date is today's date.
//
/*private*/ function isDateToday(theDate)
{
  if (theDate == null ||
      theDate.getFullYear() != cal_initialDate.getFullYear() ||
      theDate.getMonth() != cal_initialDate.getMonth() ||
      theDate.getDate() != cal_initialDate.getDate())
  {
    return(false);
  }

  return(true);
}

//
// Returns true if date is further than allowed days limit. The offset is
// added to the date, set to 1 if no offset is desired.
//
/*private*/ function isDateTooFar(theDate, offset)
{
  // Calculate the maximum number of days from today.
  var time = theDate.getTime();
  if (time == calendarMaxDate.getTime())
  {
    return(false);
  }

  var days = Math.ceil((time - cal_initialDate.getTime()) / msInDay) + offset - 1;
  return(days > cal_daysLimit);
}

//
// Returns the number of days in the month.
//
/*private*/ function getMonthDays(theDate) {
    // Return the last day of the current month. Using 0 as a date substract
    // 1 days from the next month, which is what we need.
    var lastDate = new Date(theDate.getFullYear(), theDate.getMonth() + 1, 0);
    return lastDate.getDate();
}


//
// Returns the weekday offset of the first day of the month.
//
/*private*/ function getCalendarOffset(theDate) {
    var firstDay = new Date(theDate.getFullYear(), theDate.getMonth(), 1);
    return firstDay.getDay();
}

//
// Returns true is the two dates have the same year and month.
//
/*private*/ function isSameMonth(firstDate, secondDate) {
    if (firstDate == null || secondDate == null)
        return false;
    return firstDate.getFullYear() == secondDate.getFullYear() &&
           firstDate.getMonth() == secondDate.getMonth();
}


//
// Formats date according to specified format.
//
/*private*/ function formatLongDate(theDate) {
/*
 * The following long formats are supported similar to Java's DateFormat
 * class DateFormat.FULL:
 *   "en" - Tuesday, April 25, 2006
 *   "fr" - mardi 25 avril 2006
 *   "it" - martedì 25 aprile 2006
 *   "de" - Dienstag, 25. April 2006
 */
  var result = new Object();
  var dayOfWeek = wNames[cal_lang][theDate.getDay()];
  var day = theDate.getDate();
  var month = mNames[cal_lang][theDate.getMonth()];
  var year = theDate.getFullYear();

  if (cal_lang == "fr" || cal_lang == "it")
  {
    result = dayOfWeek + " " + day + " " + month + " " + year;
  }
  else if (cal_lang == "de")
  {
    result = dayOfWeek + ", " + day + ". " + month + " " + year;
  }
  else /* "en" as default */
  {
    result = dayOfWeek + ", " + month + " " + day + ", " + year;
  }

  return result;
}

/*private*/ function formatDate(theDate, format) {
    var result = format.toLowerCase();
    //Ambika - seperated "DE" check from others since year format for DE is jjjj
    //and it conflicts with the "jj" date format for french
    // added "g" and "t" to the date format check
    if(cal_lang == "de")
        result = result.replace(/[j]+/, theDate.getFullYear());
    else
        result = result.replace(/[ya]+/, theDate.getFullYear());
    result = result.replace(/m+/, formatToTwoDigits(theDate.getMonth() + 1));
    result = result.replace(/[djtg]+/, formatToTwoDigits(theDate.getDate()));

    return result;
}

//
// Adds a leading 0 to the number if necessay so it is always 2 digits.
//
/*private*/ function formatToTwoDigits(n) {
    if (n >= 0 && n < 10)
        return "0" + n;
    else
        return n;
}

/*private*/ function calIndexOf(inArray, inString)
{
  var arrayValue;
  var stringValue = inString.toLowerCase();

  for (var i = 0; i < inArray.length; i++)
  {
    arrayValue = inArray[i].toLowerCase();
    if (stringValue == arrayValue)
    {
      return(i);
    }
  }

  return(-1);
}

//---------------------------------------------------------------------------
// parseLongDate() - will parse a text string representing a date to a Date
// object.  The following long formats are supported similar to Java's
// DateFormat class DateFormat.FULL:
//   "en" - Tuesday, April 25, 2006
//   "fr" - mardi 25 avril 2006
//   "it" - martedì 25 aprile 2006
//   "de" - Dienstag, 25. April 2006
//---------------------------------------------------------------------------
/*private*/ function parseLongDate(text)
{
  var day = 0, month = 0, year = 0;
  var startIndex = 0;
  var parts = null;
  var origPartsLength = 0;

  if (text == null)
  {
    return(null);
  }

  // Parse the date into individual components.
  parts = text.split(/[\s,.\-\/]+/);
  origPartsLength = parts.length;

  // We need to get to the numeric date values, so parse away!
  if (origPartsLength > 4 || origPartsLength < 3)
  {
    // We did not have the proper number of date components.
    return(null);
  }

  // Check to see if week day was provided.  Note that in all supported
  // languages so far the day of the week should be first.
  if (calIndexOf(wNames[cal_lang], parts[0]) == -1)
  {
    if (origPartsLength == 4)
    {
      // We have too many elements.
      return(null);
    }
  }
  else
  {
    // Skip over the day of week.
    startIndex = 1;
  }

  // Now for the language specific parsing.
  if (cal_lang == "fr" || cal_lang == "it" || cal_lang == "de")
  {
    // Day - integer.
    if (parts[startIndex].length < 1 || parts[startIndex].length > 2 ||
        !parts[startIndex].match(/[0-9]+/))
    {
      return(null);
    }
    day = parts[startIndex++];

    // Month - word.
    month = calIndexOf(mNames[cal_lang], parts[startIndex]);

    if (month == -1)
    {
      // Check for an integer month.
      if (parts[startIndex].match(/[0-9]+/))
      {
        month = parts[startIndex] - 1;
      }
      else
      {
        return(null);
      }
    }

    startIndex++;

    // Year - integer.
    if (parts[startIndex].length != 4 || !parts[startIndex].match(/[0-9]+/))
    {
      return(null);
    }
    year = parts[startIndex];
  }
  else // default and "en"
  {
    // Month - word.
    month = calIndexOf(mNames[cal_lang], parts[startIndex]);

    if (month == -1)
    {
      // Check for an integer month.
      if (parts[startIndex].match(/[0-9]+/))
      {
        month = parts[startIndex] - 1;
      }
      else
      {
        return(null);
      }
    }

    startIndex++;

    // Day - integer.
    if (parts[startIndex].length < 1 || parts[startIndex].length > 2 ||
        !parts[startIndex].match(/[0-9]+/))
    {
      return(null);
    }
    day = parts[startIndex++];

    // Year - integer.
    if (parts[startIndex].length != 4 || !parts[startIndex].match(/[0-9]+/))
    {
      return(null);
    }
    year = parts[startIndex];
  }

  var newDate = new Date(year, month, day);

  // Adjust year if date is past and same date next year is not too far.
  if (isDateBeforeToday(newDate))
  {
    var dateYearAdjusted = new Date(newDate.getTime());
    dateYearAdjusted.setFullYear(dateYearAdjusted.getFullYear() + 1);
    if (!isDateTooFar(dateYearAdjusted, 1))
    {
      newDate = dateYearAdjusted;
    }
  }

  return(newDate);
}

//
// Parse a text date in dd/mm/yyyy or dd/mm/yy format and a return a date object.
// The separator can be a dash '-' instead and the year can be left out.
//
/*private*/ function parseDate(text)
{
  var parts = null;
  var origPartsLenght = 0;

  if (text == null)
  {
    return(null);
  }

  parts = text.split(/[-\/]/);
  origPartsLenght = parts.length;

    // Validate.
    if (parts.length < 2 || parts.length > 3)
        return null;
    if (parts.length == 2)
        parts[2] = String((new Date()).getFullYear());
    else if (parts[2].length <= 2)
        parts[2] = String(2000 + Number(parts[2]));
    if (parts[0].length < 1 || parts[0].length > 2 || ! parts[0].match(/[0-9]+/))
        return null;
    if (parts[1].length < 1 || parts[1].length > 2 || ! parts[1].match(/[0-9]+/))
        return null;

    if (parts[2].length == 0 || parts[2].length == 3 ||
        parts[2].length > 4  || ! parts[2].match(/[0-9]+/))
        return null;
    var newDate = new Date(parts[2], Number(parts[1]) - 1, parts[0]);

    // Adjust year if date is past and same date next year is not too far.
    if (origPartsLenght == 2 && isDateBeforeToday(newDate))
    {
        var dateYearAdjusted = new Date(newDate.getTime());
        dateYearAdjusted.setFullYear(dateYearAdjusted.getFullYear() + 1);
        if (! isDateTooFar(dateYearAdjusted, 1))
            newDate = dateYearAdjusted;
    }

    return(newDate);
}

//
// Two positionning functions from site http://www.quirksmode.org/js/findpos.html.
//
/*private*/ function findPosX(obj) {
    var curleft = 0;
    if (obj.offsetParent) {
        while (obj.offsetParent) {
            curleft += obj.offsetLeft;
            obj = obj.offsetParent;
        }
    }
    else if (obj.x)
        curleft += obj.x;
    return curleft;
}

/*private*/ function findPosY(obj) {
    var curtop = 0;
    if (obj.offsetParent) {
        while (obj.offsetParent) {
            curtop += obj.offsetTop;
            obj = obj.offsetParent;
        }
    }
    else if (obj.y)
        curtop += obj.y;
    return curtop;
}

function parseMonthYear(myear)
{
    if (myear != null && myear != "" && myear.length == 7)
    {
        return m2n[myear.substring(0,3)] + "/" + myear.substring(3,7);
    }
    else
        return formatToTwoDigits(cal_initialDate.getMonth()+1);
}

function displayDate(d)
{
    return formatToTwoDigits(d.getDate()) + "/" + formatToTwoDigits(d.getMonth()+1) + "/" + d.getFullYear();
}


function isDateDefined(f)
{
  if (f.value == "")
  {
    return false;
  }

  if (f.value != "")
  {
    if (!(cal_lang))
    {
      return true;
    }
    if (f.value == cal_dateFormat[cal_lang])
    {
      return false;
    }

    var theDate;
    if (cal_format == "long")
    {
      theDate = parseLongDate(f.value);
    }
    else
    {
      theDate = parseDate(f.value);
    }

    if (theDate == null || isDateBeforeToday(theDate) || isDateTooFar(theDate, 1))
    {
      return false;
    }
  }
  return true;
}

//
// Adjust the startDate and endDate fields as needed.  Adjust the time
// as well if it becomes invalid as a result of this adjustment.
//
function doAdjustDates(newDate)
{
  dateValue[currentDateField.id] = newDate;

  if (cal_format == "long")
  {
    currentDateField.value = formatLongDate(newDate);
  }
  else
  {
    currentDateField.value = formatDate(newDate, cal_dateFormat[cal_lang]);
  }

  currentDateField.className = "gen";

  // Change one of the dates if startDate is later than endDate.
  var startDate = dateValue[startDateField.id];
  var endDate = dateValue[endDateField.id];

  if (startDate != null && endDate != null)
  {
    // If the start date is after end date we need to adjust.
    if (startDate.getTime() > endDate.getTime())
    {
      if (currentDateField.id == startDateField.id)
      {
        endDateField.value = startDateField.value;
        dateValue[endDateField.id] = dateValue[startDateField.id];
        endDate = dateValue[endDateField.id];
      }
      else if (currentDateField.id == endDateField.id)
      {
        startDateField.value = endDateField.value;
        dateValue[startDateField.id] = dateValue[endDateField.id];
        startDate = dateValue[startDateField.id];
      }
    }
  }

}  /* doAdjustDates() */

function setStartDateNow()
{
    currentDateField = startDateField;
    //doAdjustDates(new Date());
    doAdjustDates(new Date(clientDate));
    currentDateField = null;
} /* setStartDateNow */

function setEndDateNow()
{
    currentDateField = endDateField;
    //doAdjustDates(new Date());
    doAdjustDates(new Date(clientDate));
    currentDateField = null;
} /* setEndDateNow */


function lockStartDate()
{
  if (startDateField == null || startTimeField == null)
  {
    return;
  }

  // Set the startDate and startTime fields to now.
  currentDate = new Date();
  currentDateField = startDateField;
  doAdjustDates(currentDate);
  currentDateField = null;

  // Disable editing the fields.
  startDateField.disabled = true;
  calendarStartLocked = true;

} /* lockStartDate */

function unlockStartDate()
{
  if (startDateField == null)
  {
    return;
  }

  // Disable editing the fields.
  startDateField.disabled = false;
  calendarStartLocked = false;


} /* unlockStartDate */


function lockEndDate()
{
  if (endDateField == null || endTimeField == null)
  {
    return;
  }

  // Set the endDate and endTime fields to max future time.
  currentDateField = endDateField;
  doAdjustDates(calendarMaxDate);
  currentDateField = null;

  // Disable editing the fields.
  endDateField.disabled = true;
  calendarEndLocked = true;

} /* lockEndDate */

function unlockEndDate()
{
  if (endDateField == null || startDateField == null )
  {
    return;
  }

  //currentDateField = endDateField;
  //doAdjustDates(dateValue[startDateField.id]);
  //currentDateField = null;

  // Disable editing the fields.
  endDateField.disabled = false;
  calendarEndLocked = false;

} /* unlockEndDate */

function setStartDate(dateString)
{
    var date = null;
    if (cal_format == "long")
    {
        date = parseLongDate(dateString);
    }
    else
    {
        date = parseDate(dateString);
    }

    setStartDateObj(date);
} /* setStartDate */

function setStartDateObj(date)
{
    if (date != null) {
        currentDateField = startDateField;
        doAdjustDates(date);
        currentDateField = null;
    }
}

function setEndDate(dateString)
{
    var date = null;
    if (cal_format == "long")
    {
        date = parseLongDate(dateString);
    }
    else
    {
        date = parseDate(dateString);
    }

    setEndDateObj(date);
} /* setEndDate */

function setEndDateObj(date)
{
    if (date != null) {
        currentDateField = endDateField;
        doAdjustDates(date);
        currentDateField = null;
    }
}
