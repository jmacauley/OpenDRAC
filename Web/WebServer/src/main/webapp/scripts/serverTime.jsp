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
<%@ page import="java.util.Calendar" %>

<%
  // Get the server timezone offset.

  Calendar cal = Calendar.getInstance();
  String offset_string;
  int offset;
  int off_hours;
  int off_minutes;

  offset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
  off_hours = Math.abs(offset / 60);
  off_minutes = Math.abs(offset % 60);

  if (offset < 0) {
    offset_string = "GMT-";
  }
  else {
    offset_string = "GMT+";
  }

  if (off_hours < 9) {
    offset_string = offset_string + "0" + off_hours + ":";
  }
  else {
    offset_string = offset_string + off_hours + ":";
  }

  if (off_minutes < 9) {
    offset_string = offset_string + "0" + off_minutes;
  }
  else {
    offset_string = offset_string + off_minutes;
  }

%>

var standardbrowser = !document.all && !document.getElementById;

<!-- server -->
var serverDigitalTime = new Date();
serverDigitalTime.setFullYear(<%=cal.get(Calendar.YEAR)%>);
serverDigitalTime.setMonth(<%=cal.get(Calendar.MONTH)%>);
serverDigitalTime.setDate(<%=cal.get(Calendar.DAY_OF_MONTH)%>);
serverDigitalTime.setHours(<%=cal.get(Calendar.HOUR_OF_DAY)%>);
serverDigitalTime.setMinutes(<%=cal.get(Calendar.MINUTE)%>);
serverDigitalTime.setSeconds(<%=cal.get(Calendar.SECOND)%>);
var serverOffset = "<%= offset_string %>";

<!--local -->
var localDigitalTime = new Date();
var localOffset = localDigitalTime.getTimezoneOffset();

<!--client-->
var clientDigitalTime = jsCopyDate(clientDate);

if (standardbrowser) {
  document.write('<form name="tick"><input type="text" name="tock" size="22"></form>');
}

function adjustDn(hours)
{
  if (hours >= 12)
     return "pm";
  else
     return "am";
}

function adjustHours(hours)
{
  var hrs = hours;
 
 // Uncomment for 12-hour format
 /* if (hrs > 12) {
    hrs=hrs-12;
  } 
  
  if (hrs == 0) {
    hrs = 12;
  } */

  if (hrs.toString().length == 1)
  {
    hrs = "0" + hrs;
  }

  return hrs;
}

function adjustMinutes(minutes)
{
  var mins = minutes;
  if (mins <= 9 )
  {
    mins = "0" + mins;
  }

  return mins;
}

function adjustSeconds(seconds)
{
   var secs = seconds;
   if (secs <= 9 )
   {
     secs = "0" + secs;
   } 

   return secs;
}

function showClock() {

  if (!standardbrowser) {
	  var serverTimeObj = document.getElementById ? document.getElementById("server_time") : document.all.server_time;
	  var clientTimeObj = document.getElementById ? document.getElementById("client_time") : document.all.client_time;
	  var localTimeObj = document.getElementById ? document.getElementById("local_time") : document.all.local_time;
  }

  // server
  var serverMonth = serverDigitalTime.getMonth() + 1;
  var serverDay = serverDigitalTime.getDate();
  var serverYear = serverDigitalTime.getFullYear();
  var serverHours = serverDigitalTime.getHours();
  var serverMinutes = serverDigitalTime.getMinutes();
  var serverSeconds = serverDigitalTime.getSeconds();
  var serverDn = "am";

  // local
  var localHours = localDigitalTime.getHours();
  var localMinutes = localDigitalTime.getMinutes();
  var localSeconds = localDigitalTime.getSeconds();
  var localDn = "am";

  // client
  var clientHours = clientDigitalTime.getHours();
  var clientMinutes = clientDigitalTime.getMinutes();
  var clientSeconds = clientDigitalTime.getSeconds();
  var clientDn = "am";


  // Increment
  serverDigitalTime.setSeconds(serverSeconds + 1);
  localDigitalTime.setSeconds(localSeconds + 1);
  clientDigitalTime.setSeconds(clientSeconds + 1);


  // Formatting
  /* serverDn = adjustDn(serverHours);
  localDn = adjustDn(localHours);
  clientDn = adjustDn(clientHours); */

  serverHours = adjustHours(serverHours);
  localHours = adjustHours(localHours);
  clientHours = adjustHours(clientHours);

  serverMinutes = adjustMinutes(serverMinutes);
  localMinutes = adjustMinutes(localMinutes);
  clientMinutes = adjustMinutes(clientMinutes);

  serverSeconds = adjustSeconds(serverSeconds);
  localSeconds = adjustSeconds(localSeconds);
  clientSeconds = adjustSeconds(clientSeconds);

  if (standardbrowser) {
    document.tick.tock.value = serverHours + ":" + serverMinutes + ":" + serverSeconds + " " + serverOffset;
  }
  else {
    if (serverTimeObj != null)
      serverTimeObj.innerHTML = serverHours + ":" + serverMinutes + ":" + serverSeconds + " " + serverOffset;

    if (localTimeObj != null)
      localTimeObj.innerHTML = localHours + ":" + localMinutes + ":" + localSeconds +  " " + getGMTDisplay(localOffset);

    if (clientTimeObj != null)
      clientTimeObj.innerHTML = clientHours + ":" + clientMinutes + ":" + clientSeconds + " " 
            + getGMTDisplay(<%=userPreferredTimeZoneOffset%>);
  }

  setTimeout("showClock()",1000);
}

