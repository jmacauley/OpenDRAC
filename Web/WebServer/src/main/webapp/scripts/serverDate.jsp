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

<%@ page import="java.util.Calendar,java.util.TimeZone,com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails" %>
<%@ page import="com.nortel.appcore.app.drac.common.security.policy.types.UserProfile" %>

<%
// ********* SETUP DATE and TIME CONSTANTS ***************

// Server  timezone offset for use by welcome.jsp
int serverTimeZoneOffset = (-(Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar.getInstance().get(Calendar.DST_OFFSET))/ (60 * 1000));

// User's preferred timezone
String userPreferredTimeZoneId = "";
int userPreferredTimeZoneOffset = 0;
UserDetails udt = (UserDetails) session.getAttribute("authObj");
if (udt != null && udt.getUserPolicyProfile().getUserProfile().getAuthenticationData().getAuthenticationState().equals(UserProfile.AuthenticationState.VALID))
{
      userPreferredTimeZoneId = udt.getUserPolicyProfile().getUserProfile().getPreferences().getTimeZoneId();
      // put the id into the session for later lookups, and to act as a flag
      session.setAttribute("userPreferredTimeZoneId", userPreferredTimeZoneId);

      // get the stringified offset for use by welcome.jsp
      TimeZone userPreferredTimeZone = TimeZone.getTimeZone(userPreferredTimeZoneId);
      Calendar cal = Calendar.getInstance(userPreferredTimeZone);
      userPreferredTimeZoneOffset = (-(cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET))/ (60 * 1000));
}
%>

function getDaysInMonth(aDate){
   // returns the last day of a given month
    var m = new Number(aDate.getMonth());
    var y = new Number(aDate.getYear());

    var tmpDate = new Date(y, m, 28);
    var checkMonth = tmpDate.getMonth();
    var lastDay = 27;

    while(lastDay <= 31){
        temp = tmpDate.setDate(lastDay + 1);
        if(checkMonth != tmpDate.getMonth())
            break;
        lastDay++
    }
    return lastDay;
}

function dateFormat(aDate, displayPat){
    /********************************************************
    *   Valid Masks:
    *   !mmmm = Long month (eg. January)
    *   !mmm = Short month (eg. Jan)
    *   !mm = Numeric date (eg. 07)
    *   !m = Numeric date (eg. 7)
    *   !dddd = Long day (eg. Monday)
    *   !ddd = Short day (eg. Mon)
    *   !dd = Numeric day (eg. 07)
    *   !d = Numeric day (eg. 7)
    *   !yyyy = Year (eg. 1999)
    *   !yy = Year (eg. 99)
   ********************************************************/

    intMonth = aDate.getMonth();
    intDate = aDate.getDate();
    intDay = aDate.getDay();
    intYear = aDate.getFullYear();

    var months_long =  new Array ('January','February','March','April',
       'May','June','July','August','September','October','November','December')
    var months_short = new Array('Jan','Feb','Mar','Apr','May','Jun',
       'Jul','Aug','Sep','Oct','Nov','Dec')
    var days_long = new Array('Sunday','Monday','Tuesday','Wednesday',
       'Thursday','Friday','Saturday')
    var days_short = new Array('Sun','Mon','Tue','Wed','Thu','Fri','Sat')

    var mmmm = months_long[intMonth]
    var mmm = months_short[intMonth]
    var mm = intMonth < 9?'0'+ (1 + intMonth) + '':(1+intMonth)+'';
    var m = 1+intMonth+'';
    var dddd = days_long[intDay];
    var ddd = days_short[intDay];
    var dd = intDate<10?'0'+intDate+'':intDate+'';
    var d = intDate+'';
    var yyyy = intYear;

    century = 0;
    while((intYear-century)>=100)
        century = century + 100;

    var yy = intYear - century
    if(yy<10)
        yy = '0' + yy + '';

    displayDate = new String(displayPat);

    displayDate = displayDate.replace(/!mmmm/i,mmmm);
    displayDate = displayDate.replace(/!mmm/i,mmm);
    displayDate = displayDate.replace(/!mm/i,mm);
    displayDate = displayDate.replace(/!m/i,m);
    displayDate = displayDate.replace(/!dddd/i,dddd);
    displayDate = displayDate.replace(/!ddd/i,ddd);
    displayDate = displayDate.replace(/!dd/i,dd);
    displayDate = displayDate.replace(/!d/i,d);
    displayDate = displayDate.replace(/!yyyy/i,yyyy);
    displayDate = displayDate.replace(/!yy/i,yy);

    return displayDate;
}


<% Calendar cal2 = Calendar.getInstance(); %>
var standardbrowser = !document.all && !document.getElementById;

<!-- Server -->
var serverDate = new Date();
serverDate.setFullYear(<%=cal2.get(Calendar.YEAR)%>);
serverDate.setMonth(<%=cal2.get(Calendar.MONTH)%>);
serverDate.setDate(<%=cal2.get(Calendar.DAY_OF_MONTH)%>);
serverDate.setHours(<%=cal2.get(Calendar.HOUR_OF_DAY)%>);
serverDate.setMinutes(<%=cal2.get(Calendar.MINUTE)%>);
serverDate.setSeconds(<%=cal2.get(Calendar.SECOND)%>);

<!-- Client -->
var clientDate = new Date();
<% 
   Calendar cal3 = Calendar.getInstance();
   if (session.getAttribute("userPreferredTimeZoneId") != null)
   {
    cal3.setTimeZone(TimeZone.getTimeZone((String)session.getAttribute("userPreferredTimeZoneId")));
   }
%>

clientDate.setFullYear(<%=cal3.get(Calendar.YEAR)%>);
clientDate.setMonth(<%=cal3.get(Calendar.MONTH)%>);
clientDate.setDate(<%=cal3.get(Calendar.DAY_OF_MONTH)%>);
clientDate.setHours(<%=cal3.get(Calendar.HOUR_OF_DAY)%>);
clientDate.setMinutes(<%=cal3.get(Calendar.MINUTE)%>);
clientDate.setSeconds(<%=cal3.get(Calendar.SECOND)%>);

<!-- Local --> 
var localDate = new Date();

if (standardbrowser) {
  document.write('<form name="display_date"><input type="text" name="date_field" size="20"></form>');
}

function showDate() {

  if (!standardbrowser) {
    var serverDateObj = document.getElementById ? document.getElementById("server_date") : document.all.server_date;
    var localDateObj = document.getElementById ? document.getElementById("local_date") : document.all.local_date;
    var clientDateObj = document.getElementById ? document.getElementById("client_date") : document.all.client_date;
  }

  var serverDateString = dateFormat(serverDate, "!dddd, !mmmm !dd, !yyyy");
  var clientDateString = dateFormat(clientDate, "!dddd, !mmmm !dd, !yyyy"); 
  var localDateString = dateFormat(localDate, "!dddd, !mmmm !dd, !yyyy");

  serverDate.setSeconds(serverDate.getSeconds() + 1);
  clientDate.setSeconds(clientDate.getSeconds() + 1);
  localDate.setSeconds(clientDate.getSeconds() + 1);

  if (standardbrowser) {
    document.display_date.date_field.value = serverDateString;
  }
  else {

    if (serverDateObj != null)
      serverDateObj.innerHTML = serverDateString;

    if (localDateObj != null)
      localDateObj.innerHTML = localDateString;

    if (clientDateObj != null)
      clientDateObj.innerHTML = clientDateString;
  }

  window.setTimeout("showDate()",1000);
}

