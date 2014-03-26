<%--

    <pre>
    The owner of the original code is Ciena Corporation.

    Portions created by the original owner are Copyright (C) 2004-2010
    the original owner. All Rights Reserved.

    Portions created by other contributors are Copyright (C) the contributor.
    All Rights Reserved.

    Contributor(s):
      (Contributors insert name & email here)

    This file is part of DRAC (Dynamic Resource Allocation Controller).

    DRAC is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    DRAC is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
    Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program.  If not, see <http://www.gnu.org/licenses/>.
    </pre>

--%>

<%
/****************************************************************************
 * OpenDRAC WEB GUI version 0.2 Alpha
 *
 * File: /common/calendar.jsp
 *
 * Description:
 *   This page contains the table format for the javascript calendar.
 *
 ****************************************************************************/
%>
<script type="text/javascript" src="/scripts/utilities.js"></script>
<div id="cal" style="position:absolute; z-index:1; overflow:auto; display:none;" onmouseover="dCal.calendarOver(this)" onmouseout="dCal.calendarOut(this)">
<table cellpadding="0" cellspacing="0" class="calendar">
<tbody>
    <tr><td>
<table cellpadding="0" cellspacing="1" class="calendar" width="100%">
<tbody>
  <tr class="mtitle">
    <td>
      <div id="calarrowback" onmouseout="dCal.mout(this);mB.handleMouseOut()" onmousedown="mB.handleMouseDown()" onmouseup="mB.handleMouseUp()" onmouseover="dCal.mover(this)" class="next" onclick="dCal.changeMonth(-1)">
        <img src="/images/calendar_prev.gif" alt="" width="8" height="7" border="0"/>
      </div>
    </td>
    <td id="cal1TitleM" class="title" nowrap="nowrap" width="65"></td>
    <td>
      <div id="calarrowfwd" onmouseout="dCal.mout(this);mF.handleMouseOut()" onmousedown="mF.handleMouseDown()" onmouseup="mF.handleMouseUp()" onmouseover="dCal.mover(this)"  class="next" onclick="dCal.changeMonth(1)">
        <img src="/images/calendar_next.gif" alt="" width="8" height="7" border="0"/>
      </div>
    </td>
    <td>&nbsp;</td>
    <td>
      <div id="calarrowback" onmouseout="dCal.mout(this);yB.handleMouseOut()" onmousedown="yB.handleMouseDown()" onmouseup="yB.handleMouseUp()" onmouseover="dCal.mover(this)" class="next" onclick="dCal.changeMonth(-12)">
        <img src="/images/calendar_prev.gif" alt="" width="8" height="7" border="0"/>
      </div>
    </td>
    <td id="cal1TitleY" class="title" nowrap="nowrap"></td>
    <td>
      <div id="calarrowfwd" onmouseout="dCal.mout(this);yF.handleMouseOut()" onmousedown="yF.handleMouseDown()" onmouseup="yF.handleMouseUp()" onmouseover="dCal.mover(this)"  class="next" onclick="dCal.changeMonth(12)">
        <img src="/images/calendar_next.gif" alt="" width="8" height="7" border="0"/>
      </div>
    </td>
  </tr>
</tbody>
</table>
<table cellpadding="0" cellspacing="1" class="calendar">
  <tr>
    <td colspan="7" style="font-size: 0px" height="2"></td>
  </tr>
  <tr class="wtitle">
    <td id="cWc0" class="weekend">&nbsp;</td>
    <td id="cWc1">&nbsp;</td>
    <td id="cWc2">&nbsp;</td>
    <td id="cWc3">&nbsp;</td>
    <td id="cWc4">&nbsp;</td>
    <td id="cWc5">&nbsp;</td>
    <td id="cWc6" class="weekend">&nbsp;</td>
  </tr>

  <tr class="cells">
    <td id="c1c1"  onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
    <td id="c1c2"  onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c3"  onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c4"  onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c5"  onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c6"  onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c7"  onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
  </tr>

  <tr class="cells">
    <td id="c1c8"  onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
    <td id="c1c9"  onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c10" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c11" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c12" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c13" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c14" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
  </tr>

  <tr class="cells">
    <td id="c1c15" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
    <td id="c1c16" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c17" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c18" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c19" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c20" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c21" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
  </tr>

  <tr class="cells">
    <td id="c1c22" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
    <td id="c1c23" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c24" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c25" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c26" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c27" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c28" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
  </tr>

  <tr class="cells">
    <td id="c1c29" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
    <td id="c1c30" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c31" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c32" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c33" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c34" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c35" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
  </tr>

  <tr class="cells">
    <td id="c1c36" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
    <td id="c1c37" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c38" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c39" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c40" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c41" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)">&nbsp;</td>
    <td id="c1c42" onmouseout="dCal.cout(this)" onmouseover="dCal.cover(this)" onclick="dCal.selectDate(this, 0)" class="weekend">&nbsp;</td>
  </tr>

  <tr>
    <td colspan="2" class="link">
        <a href="javascript:dCal.setDateToday()"><font color="#ffffff"><bean:message key="drac.calendar.today"/></font></a>
    </td>
    <td colspan="5" class="link">
        <a href="javascript:dCal.hideCalendar()"><font color="#ffffff"><bean:message key="drac.text.close"/></font></a>
    </td>
  </tr>
</tbody>
</table>
</td></tr>
</tbody>
</table>
</div>


<script language="Javascript">
//
// OpenDRAC Web GUI
//
// File: /common/calendar.jsp
//
// Description:
//   Popup calendar functions supporting internationalization.
//
// TODO: Perhaps internationalization can be better configured through
//       properties in a jsp instead of hardcoding it in this js file.
//

function DRACCalendar(language, format, initialDate) {
    var cal_format = "short";
    var calInternalDate = null;
    var cal_lang = "en";


    // Maximum number of days from today which are allowed for selection.
    var cal_daysLimit = 365*10;

    // Date formats.
    var cal_dateFormat = null;
    var cal_dateLongFormat = null;
    var cal_timeFormat = null;
    var cal_shownDate = null;

    // Arrays of date cells for better performance.
    var c1cells = null;

    // Array to hold week character string.
    var cWcells = null;

    // Calendar position variables.
    var overCalendar = false;
    var inDateField = false;

    var calendarLock = new Array();
    var calendarMaxDate = null;

    var previousClass = "";

    // Flag set if month change buttons should be hidden.
    var isEndReached = false;
    var isAtBeginning = true;

    var errorDateValue = null;
    // Associative array of date and time values.
    var dateValue = null;
    var timeValue = null;
    var currentDateField = null;
    var currentTimeField = null;

    // Associative array of drop-downs select by field name.
    var selectList = null;
    var selectListCount = 0;

    // Constant containing the number of milliseconds in one day, used for date arithmetics.
    var msInDay = 24*60*60*1000;

    var m2n = null;
    var n2m = null;
    var mNames = null;
    var wNames = null;
    var wChar = null;

    var listeners = null;

    var nextElem = null;

    init(language, format, initialDate);

    function init(language, format, initialDate) {
        // Seed the initial date.
        if (initialDate != null) {
            calInternalDate = initialDate;
        } else {
            calInternalDate = new Date();
        }

        // Interface language. Initialize with appropriate language code.
        if (language != null && language != "") {
            cal_lang = language;
        }

        calendarMaxDate = new Date(3000, 0, 0);
        dateValue = new Object();
        timeValue = new Object();
        selectList = new Array();

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

        // Months to num array.
        m2n = new Array(12);
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
        n2m = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

        // Arrays of month names by language.
        mNames   = new Array(4);
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
        wNames   = new Array(4);
        wNames["en"] = new Array("Sunday", "Monday", "Tuesday", "Wednesday",
                                 "Thursday", "Friday", "Saturday");
        wNames["fr"] = new Array("dimanche", "lundi", "mardi", "mercredi", "jeudi",
                                 "vendredi", "samedi");
        wNames["de"] = new Array("Sonntag", "Montag", "Dienstag", "Mittwoch",
                                 "Donnerstag", "Freitag", "Samstag");
        wNames["it"] = new Array("domenica", "lunedì", "martedì", "mercoledì",
                                 "giovedì", "venerdì", "sabato");

        // Array of week day characters by language.
        wChar   = new Array(4);
        wChar["en"] = new Array("S", "M", "T", "W", "T", "F", "S");
        wChar["fr"] = new Array("D", "L", "M", "M", "J", "V", "S");
        wChar["de"] = new Array("S", "M", "D", "M", "D", "F", "S");
        wChar["it"] = new Array("D", "L", "M", "M", "G", "V", "S");

        errorDateValue   = new Array(4);
        errorDateValue["en"] = "Please enter a valid date value.";
        errorDateValue["fr"] = "Veuillez écrire une valeur valide de date.";
        errorDateValue["de"] = "Tragen Sie bitte einen gültigen Datumwert ein.";
        errorDateValue["it"] = "Entri prego in un valore valido della data.";

        cal_shownDate = new Date(calInternalDate.getFullYear(), calInternalDate.getMonth(), 1);

        if (format == "long") {
            cal_format = "long";
        } else {
            cal_format = "short";
        }

        // Cache calendar1 cells for better performance.
        c1cells = new Array(43);
        for (var i = 1; i <= 42; i++) {
            var elem = document.getElementById("c1c" + i);
            c1cells[i] = elem;
        }

        // Cache week character strings for better performance.
        var elem = null;
        cWcells = new Array(7);
        for (var i = 0; i <= 6; i++) {
            elem = document.getElementById("cWc" + i);
            cWcells[i] = elem;
        }

        listeners = new Array();
    }

    this.getDateObject = function(dateFieldId) {
        var dateField = document.getElementById(dateFieldId);
        if (dateField) {
            return dateValue[dateField.id];
        }
    }

    this.getTimeObject = function(timeFieldId)
    {
        var timeField = document.getElementById(timeFieldId);
        if (timeField) {
            return timeValue[timeField.id];
        }
    }


    this.isDateEmpty = function(text) {
        var result = null;

        if (cal_format == "long") {
            result = parseLongDate(text);
        }
        else {
            result = parseDate(text);
        }

        return(result == null);
    }

    //
    // Must be called when the user clicks or tabs into a date input field (onfocus event).
    //
    this.dateFocus = function(o)
    {
       dateFocus(o, null); 
    }

    this.dateFocus = function(o, focusElem)
    {
      nextElem = focusElem;

      if (calendarLock[o.id] == true)
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
    this.dateBlur = function(o)
    {
        inDateField = false;

        if (! overCalendar)
        {
            this.hideCalendar();
        }
    }

    //
    // Must be called when the content of a date input field changes (onchange event).
    //
    this.dateChange = function(o)
    {
      if (o.value.length == 0)
      {
        o.value = "";
        if (cal_format == "long")
        {
          o.value = cal_dateLongFormat[cal_lang];
        }
        else
        {
          o.value = cal_dateFormat[cal_lang];
        }
      }

      //o.className = "gen";

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

      currentDateField = o;

      doAdjustDates(theDate);
    }

    this.setDate = function(dateFieldId, theDate)
    {
        var dateField = document.getElementById(dateFieldId);
        if (dateField && theDate) {
            currentDateField = dateField;
            doAdjustDates(theDate);
        }
    }



    //
    // Must be called when the mouse goes over the calendar (onmouseover event). Will be called
    // whenever the mouse changes date cell too.
    //
    this.calendarOver = function(o) {
        overCalendar = true;
    }

    this.timeOver = function(o) {
        overTime = true;
    }

    //
    // Must be called when the mouse leaves the calendar (onmouseout event). Will be called
    // whenever the mouse changes date cell too.
    //
    this.calendarOut = function(o) {
    //    if (! inDateField)
    //        hideCalendar();
        overCalendar = false;
    }

    this.timeOut = function(o) {
        overCalendar = false;
    }

    //
    // Changes display class when mouse is over element.
    //
    this.mover = function(target) {
        previousClass = target.className;
        target.className = "over";
    }

    //
    // Restores previous display class when mouse goes off element.
    //
    this.mout = function(target) {
        target.className = previousClass;
    }

    //
    // Changes display class when mouse is over element.
    //
    this.cover = function(target) {
        if (Number(target.innerHTML) > 0 && target.className != "past" && target.className != "weekendpast") {
            previousClass = target.className;
            target.className = "over";
        }
    }

    //
    // Restores previous display class when mouse goes off element.
    //
    this.cout = function(target) {
        if (Number(target.innerHTML) > 0 && target.className != "past" && target.className != "weekendpast") {
            target.className = previousClass;
        }
    }

    //
    // Locks the calendar for the specified field
    //
    this.lock = function(id) {
        calendarLock[id] = true;
    }

    //
    // Unlocks the calendar for the specified field
    //
    this.unlock = function(id) {
        calendarLock[id] = false;
    }

    //
    // Registers a select drop-down field so it is hidden when the calendar for
    // the specified date field is displayed. Required only because of a bug with
    // Internet Explorer.
    //
    this.registerSelect = function(dateId, selectId, condition)
    {
        var tmp = new Object();
        tmp.id = selectId;
        tmp.condition = condition;

        if (selectList[dateId] == null) {
            selectList[dateId] = new Array();
        }

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
            cal_shownDate = new Date(calInternalDate.getFullYear(), calInternalDate.getMonth(), 1);
        }

        // Position (hidden) calendar underneath field.
        var c = document.getElementById("cal");
        c.style.position = "absolute";
        c.style.left = findPosX(dateField) + "px";
        c.style.top  = (findPosY(dateField) + 20) + "px";
        c.style.zIndex = 100;

        // Loop through array of select elements to hide.
        var fields = selectList[dateField.id];
        if (fields != null) {
            for (var i = 0; i < fields.length; i++)
            {
                if (document.getElementById(fields[i].id) != null)
                {
                  // Check to see if the condition is satisfied.
                  var condition = document.getElementById(fields[i].condition);
                  if (condition == null || (condition != null && condition.checked == false))
                  {
                      document.getElementById(fields[i].id).style.visibility = "hidden";
                  }
                }
            }
        }

        // Show calendar.
        document.getElementById("cal").style.display = "block";

        // Display dates in calendar.
        displayDates();
    }

    //
    // Hide the currently shown calendar.
    //
    this.hideCalendar = function()
    {
        // Hide calendar.
        document.getElementById("cal").style.display = "none";

        // Loop through array of previously hidden select elements.
        if (currentDateField)
        {
            var fields = selectList[currentDateField.id];
            if (fields != null) {
                for (var i = 0; i < fields.length; i++)
                {
                    if (document.getElementById(fields[i].id) != null)
                    {
                        document.getElementById(fields[i].id).style.visibility = "visible";
                    }
                }
            }
        }

        // Make sure "over" variable is reset.
        overCalendar = false;
    }

    this.hideTime = function()
    {
        // Make sure "over" variable is reset.
        overTime = false;
    }

    //
    // Move by the specified number of months.
    //
    this.changeMonth = function(i)
    {
        cal_shownDate.setMonth(cal_shownDate.getMonth() + i);
        displayDates();

        currentDateField.focus();
    }

    //
    // Select a date and close calendar.
    //
    this.selectDate = function(o, monthOffset) {
        if (o.className == "past" || o.className == "weekendpast" || ! Number(o.innerHTML) > 0) {
            currentDateField.focus();
            return;
        }
        var newDate = new Date(cal_shownDate.getFullYear(), cal_shownDate.getMonth() + monthOffset, o.innerHTML);
        doAdjustDates(newDate);
        //checkStartBeforeEnd(currentDateField);
        fireDateChangeEvent();
        this.hideCalendar();

        if (nextElem != null)
        {
           nextElem.focus();
        }
    }

    //
    // Sets the date to today
    // GG Nov2010 - in the context of client's timezone
    //
    this.setDateToday = function() {
        // var today = new Date();
        var today = jsCopyDate(clientDate);
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);
        doAdjustDates(today);
        fireDateChangeEvent();
        this.hideCalendar();
    }

    this.setDateString = function(dateFieldId, dateString) {
        var dateField = document.getElementById(dateFieldId);
        var date = null;
        if (cal_format == "long")
        {
            date = parseLongDate(dateString);
        }
        else
        {
            date = parseDate(dateString);
        }
        currentDateField = dateField;
        doAdjustDates(date);
        fireDateChangeEvent();
    }

    this.setTime = function(timeFieldId, newTime)
    {
        var timeField = document.getElementById(timeFieldId);
        if (timeField) {
          timeValue[timeField.id] = newTime;
          timeField.value = formatTime(newTime);
        }
    } /* setTime() */

    this.setTimeString = function(timeFieldId, newTimeString)
    {
        var timeField = document.getElementById(timeFieldId);
        if (timeField) {
            var d = parseTime(newTimeString);
            timeValue[timeField.id] = d;
            timeField.value = formatTime(d);
        }

    } /* setTime() */

    //
    // Sets the datefield element to work on
    //
    this.setDateField = function(dateField) {
        currentDateField = document.getElementById(dateField);
    }

    //
    // Returns the datefield element currently being operated on
    //
    this.getDateField = function() {
        return currentDateField;
    }

    //
    // Sets the timefield element to work on
    //
    this.setTimeField = function(timeFieldId) {
        currentTimeField = document.getElementById(timeFieldId);
    }

    //
    // Returns the timefield element currently being operated on
    //
    this.getTimeField = function() {
        return currentTimeField;
    }

    //
    // Adds a callback function to listeners
    //
    this.addListener = function(callback) {
        listeners[listeners.length] = callback;
    }

    //
    // Invokes all callbacks registered as a listener
    //
    function fireDateChangeEvent() {
        if (listeners) {
            for (var i=0; i < listeners.length; i++) {
                listeners[i]();
            }
        }
    }

    //
    // Set the date in the textfield
    //
    /*private*/ function doAdjustDates(newDate) {
      dateValue[currentDateField.id] = newDate;

      if (cal_format == "long"){
        currentDateField.value = formatLongDate(newDate);
      } else {
        currentDateField.value = formatDate(newDate, cal_dateFormat[cal_lang]);
      }
      currentDateField.className = "gen";
    }  /* doAdjustDates() */

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
        var isTodayMonth    = isSameMonth(monthDate, calInternalDate);
        var isSelectedMonth = isSameMonth(monthDate, dateValue[currentDateField.id]);
        //var isDate1Month    = isSameMonth(monthDate, dateValue[startDateField.id]);
        //var isDate2Month    = isSameMonth(monthDate, dateValue[endDateField.id]);

        for (var i = 1; i <= lastDate; i++) {
            // Display day of month.
            cell = cells[i + offset];
            cell.innerHTML = ""; // First set to empty string, required for IE 5 Mac
            cell.innerHTML = i;

            if (monthDate.getTime() <= calInternalDate.getTime()) {
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
            else if (isTodayMonth && calInternalDate.getDate() > i) {
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


    //---------------------------------------------------------------------------
    // parseLongDate() - will parse a text string representing a date to a Date
    // object.  The following long formats are supported similar to Java's
    // DateFormat class DateFormat.FULL:
    //   "en" - Tuesday, April 25, 2006
    //   "fr" - mardi 25 avril 2006
    //   "it" - martedì 25 aprile 2006
    //   "de" - Dienstag, 25. April 2006
    //---------------------------------------------------------------------------
    /*private*/ function parseLongDate(text) {
        var day = 0, month = 0, year = 0;
        var startIndex = 0;
        var parts = null;
        var origPartsLength = 0;

        if (text == null) {
            return(null);
        }

        // Parse the date into individual components.
        parts = text.split(/[\s,.\-\/]+/);
        origPartsLength = parts.length;

        // We need to get to the numeric date values, so parse away!
        if (origPartsLength > 4 || origPartsLength < 3) {
            // We did not have the proper number of date components.
            return(null);
        }

        // Check to see if week day was provided.  Note that in all supported
        // languages so far the day of the week should be first.
        if (calIndexOf(wNames[cal_lang], parts[0]) == -1) {
            if (origPartsLength == 4) {
                // We have too many elements.
                return(null);
            }
        } else {
            // Skip over the day of week.
            startIndex = 1;
        }

        // Now for the language specific parsing.
        if (cal_lang == "fr" || cal_lang == "it" || cal_lang == "de") {
            // Day - integer.
            if (parts[startIndex].length < 1 || parts[startIndex].length > 2 || !parts[startIndex].match(/[0-9]+/)) {
                return(null);
            }
            day = parts[startIndex++];

            // Month - word.
            month = calIndexOf(mNames[cal_lang], parts[startIndex]);

            if (month == -1) {
                // Check for an integer month.
                if (parts[startIndex].match(/[0-9]+/)) {
                    month = parts[startIndex] - 1;
                } else {
                    return(null);
                }
            }

            startIndex++;

            // Year - integer.
            if (parts[startIndex].length != 4 || !parts[startIndex].match(/[0-9]+/)) {
                return(null);
            }
            year = parts[startIndex];
        } else {
            // default and "en"
            // Month - word.
            month = calIndexOf(mNames[cal_lang], parts[startIndex]);

            if (month == -1) {
                // Check for an integer month.
                if (parts[startIndex].match(/[0-9]+/)) {
                    month = parts[startIndex] - 1;
                } else {
                    return(null);
                }
            }

            startIndex++;

            // Day - integer.
            if (parts[startIndex].length < 1 || parts[startIndex].length > 2 || !parts[startIndex].match(/[0-9]+/))  {
                return(null);
            }
            day = parts[startIndex++];

            // Year - integer.
            if (parts[startIndex].length != 4 || !parts[startIndex].match(/[0-9]+/)) {
                return(null);
            }
            year = parts[startIndex];
        }

        var newDate = new Date(year, month, day);

        return(newDate);
    }

    //
    // Parse a text date in dd/mm/yyyy or dd/mm/yy format and a return a date object.
    // The separator can be a dash '-' instead and the year can be left out.
    //
    /*private*/ function parseDate(text) {
        var parts = null;
        var origPartsLength = 0;

        if (text == null) {
            return(null);
        }

        parts = text.split(/[-\/]/);
        origPartsLength = parts.length;

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
        if (origPartsLength == 2 && isDateBeforeToday(newDate)) {
            var dateYearAdjusted = new Date(newDate.getTime());
            dateYearAdjusted.setFullYear(dateYearAdjusted.getFullYear() + 1);
            if (! isDateTooFar(dateYearAdjusted, 1))
                newDate = dateYearAdjusted;
        }

        return(newDate);
    }

    //---------------------------------------------------------------------------
    // parseTime() - will parse a text string representing a time to a Date
    // object.  The following formats are supported similar to Java's
    // DateFormat class DateFormat.MEDIUM:
    //   "en" - hh:mm:ss [AM|PM] using a 12 or 24 hour clock.
    //   "fr" - hh:mm:ss using the 24 hour clock.
    //   "it" - hh.mm.ss using the 24 hour clock.
    //   "de" - hh:mm:ss using the 24 hour clock.
    //---------------------------------------------------------------------------
    /*private*/ function parseTime(text)
    {
      var newDate = null;
      var hours = 0, minutes = 0, seconds = 0;
      var ampm = "";
      var startIndex = 0;
      var parts = null;
      var origPartsLength = 0;

      if (text == null)
      {
        return(null)
      }

      parts = text.toUpperCase().split(/[\s,.:\-\/]+/);
      origPartsLength = parts.length;

      // Get the hour - the only mandatory component.
      if (origPartsLength > 0)
      {
        if (parts[startIndex].match(/\d+/))
        {
          hours = parts[startIndex];
        }
        else
        {
          return(null);
        }

        startIndex++;
      }
      else
      {
        return(null);
      }

      // If the next element is an integer then it is minutes.
      if (origPartsLength > 1)
      {
        if (parts[startIndex].match(/\d+/)) // Minutes.
        {
          minutes = parts[startIndex];
        }
        else if (parts[startIndex].match(/[AP]\.?M\.?/)) // AM/PM.
        {
          ampm = parts[startIndex];
        }
        else // No minutes or AM/PM.
        {
          return(null);
        }

        startIndex++;
      }

      // If the next element is an integer then it is seconds.
      if (origPartsLength > 2)
      {
        if (ampm == "")
        {
          if (parts[startIndex].match(/\d+/)) // Seconds.
          {
            seconds = parts[startIndex];
          }
          else if (parts[startIndex].match(/[AP]\.?M\.?/)) // AM/PM.
          {
            ampm = parts[startIndex];
          }
          else // No seconds or AM/PM.
          {
            return(null);
          }
        }
        else // String too long.
        {
          return(null);
        }

        startIndex++;
      }

      // This can only be the AM/PM sting.
      if (origPartsLength > 3)
      {
        if (ampm == "")
        {
          if (parts[startIndex].match(/[AP]\.?M\.?/)) // AM/PM.
          {
            ampm = parts[startIndex];
          }
          else // No AM/PM.
          {
            return(null);
          }
        }
        else // String too long or we already have AM/PM.
        {
          return(null);
        }

        startIndex++;
      }

      // Do a quick AM/PM sanity check.
      if (ampm == "AM")
      {
        if (hours > 12)
        {
          return(null);
        }

        // Handle the special case of 12 AM.
        if (hours == 12)
        {
          hours = 0;
        }
      }
      else if (ampm == "PM")
      {
        if (hours > 12)
        {
          return(null);
        }
        hours = hours % 12 + 12;
      }

      // Make sure we have valid times.
      if ((hours > 24) ||
          (hours == 24 && (minutes > 0 || seconds > 0)) ||
          (minutes > 59) || (seconds > 59))
      {
        return(null);
      }

      // Create a new date object.
      newDate = new Date(0, 0, 0, hours, minutes, seconds);

      return(newDate);
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

    //---------------------------------------------------------------------------
    // formatTime() - will format a date object into a valid time string. The
    // following formats are supported similar to Java's DateFormat class
    // DateFormat.MEDIUM:
    //   "en" - hh:mm:ss [AM|PM] using a 12 or 24 hour clock.
    //   "fr" - hh:mm:ss using the 24 hour clock.
    //   "it" - hh.mm.ss using the 24 hour clock.
    //   "de" - hh:mm:ss using the 24 hour clock.
    //---------------------------------------------------------------------------
    /*private*/ function formatTime(theTime)
    {
      if (theTime == null)
      {
        return(cal_timeFormat[cal_lang]);
      }

      var result = new Object();
      var hours = theTime.getHours();
      var minutes = theTime.getMinutes();
      var seconds = theTime.getSeconds();
      var ampm = "";
      var separator = ":";

      if (cal_lang == "en")
      {
        /* if (hours > 12)
        {
          ampm = " PM";
          hours = hours - 12;
        }
        else if (hours == 12)
        {
          ampm = " PM";
        }
        else
        {
          ampm = " AM";
          if (hours == 0)
          {
            hours = 12;
          }
        } */
      }
      else if (cal_lang == "it")
      {
        separator = ".";
      }

      result = hours + separator + formatToTwoDigits(minutes) + ampm;
               //separator + formatToTwoDigits(seconds) + ampm;


      return(result);
    }

    /*private*/ function calIndexOf(inArray, inString) {
        var arrayValue;
        var stringValue = inString.toLowerCase();

        for (var i = 0; i < inArray.length; i++) {
            arrayValue = inArray[i].toLowerCase();
            if (stringValue == arrayValue) {
                return(i);
            }
        }

        return(-1);
    }

    //
    // Returns true if specificed date is before today's date.
    //
    /*private*/ function isDateBeforeToday(theDate)
    {
      if (theDate.getFullYear() != calInternalDate.getFullYear())
      {
        return(theDate.getFullYear() < calInternalDate.getFullYear());
      }

      if (theDate.getMonth() != calInternalDate.getMonth())
      {
        return(theDate.getMonth() < calInternalDate.getMonth());
      }

      return(theDate.getDate() < calInternalDate.getDate());
    }

    //
    // Returns true if specificed date is today's date.
    //
    /*private*/ function isDateToday(theDate)
    {
      if (theDate == null ||
          theDate.getFullYear() != calInternalDate.getFullYear() ||
          theDate.getMonth() != calInternalDate.getMonth() ||
          theDate.getDate() != calInternalDate.getDate())
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

      var days = Math.ceil((time - calInternalDate.getTime()) / msInDay) + offset - 1;
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

}

//
// Returns a Date object with only time set.
//
function getTime(theTime)
{
  return(new Date(0, 0, 0, theTime.getHours(), theTime.getMinutes(), 0, 0));
}

//
// Returns the time to the nearest half-hour mark from now
//
function getStartTimeDefault()
{
    var d = new Date(clientDate);
    var min = d.getMinutes();
    var hrs = d.getHours();
    var offset = 0;

    // how far to the next half-hour mark by milliseconds?
    if (min > 0 && min < 30) {
        offset = (30 - min) * 60 * 1000;
    }
    else if (min > 30 && min <= 59)
    {
        offset = (60 - min) * 60 * 1000;
    }

    // adjust the time
    millis = d.getTime();
    d.setTime(millis + offset);

    return d;
} /* getStartTimeDefault */

//
// Returns the time to the previous half-hour mark
// plus additional 30 minutes (e.g. 2:09 -> 1:30, 2:45 -> 2:00)
//
function getDefaultTimePast()
{
    var d = new Date(clientDate);
    var min = d.getMinutes();
    var hrs = d.getHours();
    var offset = 0;
    if (min < 30) {
        offset = (min * 60 * 1000);
    } else if (min > 30) {
        offset = ((min-30) * 60 * 1000);
    }

    // adjust the time
    millis = d.getTime();
    d.setTime(millis - offset - (30*60*1000));

    newTime = getTime(d);
    return newTime;
}

function monthForward() {
    dCal.changeMonth(1);
}

function monthBack() {
    dCal.changeMonth(-1);
}

function yearForward() {
    dCal.changeMonth(12);
}

function yearBack() {
    dCal.changeMonth(-12);
}


</script>



