<script language="javascript">
var queryWin;
var queryTimeWin;
function showDiv() {
    loadingElem = document.getElementById("loadingDiv").innerHTML = "<center><img src='/images/loadingAnimation.gif'><br><c:out value='${sessionScope["drac.schedule.queryPath.wait"]}'/><p><input type='button' value='<c:out value='${sessionScope["drac.schedule.queryPath.close"]}'/>' onclick='Javascript:queryWin.hide();'/></center>";
    queryWin = dhtmlwindow.open("queryWindow", "div", "loadingDiv", "<c:out value='${sessionScope["drac.schedule.queryPath"]}'/>",
        "width=240px,height=100px,resize=0,scrolling=0,center=1", "recal");
    querySched();
}

function showFindTime() {
    loadingElem = document.getElementById("loadingDiv").innerHTML = "<center><img src='/images/loadingAnimation.gif'><br><c:out value='${sessionScope["drac.schedule.queryPath.wait"]}'/><p><input type='button' value='<c:out value='${sessionScope["drac.schedule.queryPath.close"]}'/>' onclick='Javascript:queryTimeWin.hide();'/></center>";
    queryTimeWin = dhtmlwindow.open("queryWindow", "div", "loadingDiv", "<c:out value='${sessionScope["drac.schedule.queryTime.title"]}'/>",
        "width=240px,height=150px,resize=0,scrolling=0,center=1", "recal");
    querySchedForTime();
}


function processQuerySchedResponse(responseXML) {
    var item = responseXML.getElementsByTagName("result")[0];
    var result = item.firstChild.nodeValue;
    var box = document.getElementById("loadingDiv");
    if (box) {
        box.innerHTML = result + "<p><center><input type='button' value='<c:out value='${sessionScope["drac.schedule.queryPath.close"]}'/>' onclick='Javascript:queryWin.hide();'/></center>";
    }
    queryWin.load("div", "loadingDiv");
}

function processQuerySchedForTimeResponse(responseXML) {
    var item = responseXML.getElementsByTagName("result")[0];
    var result = item.firstChild.nodeValue;
    var startTime;
    item = responseXML.getElementsByTagName("startTime")[0];
    if (item) {
        startTime = item.firstChild.nodeValue;
    }
    var endTime;
    item = responseXML.getElementsByTagName("endTime")[0];
    if (item) {
        endTime = item.firstChild.nodeValue;
    }
    var box = document.getElementById("loadingDiv");
    if (box) {
        if (startTime && endTime) {
            box.innerHTML = "<c:out value='${sessionScope["drac.schedule.starttime"]}'/>:<br>" + new Date(parseInt(startTime)) + "<P><c:out value='${sessionScope["drac.schedule.endtime"]}'/>:<br>" + new Date(parseInt(endTime)) + "<p><center><c:out value='${sessionScope["drac.schedule.queryTime.msg"]}'/><br><input type='button' value='<c:out value='${sessionScope["drac.text.yes"]}'/>' onclick='Javascript:setTimeForQuery(queryTimeWin," + startTime + "," + endTime +");'/>&nbsp;<input type='button' value='<c:out value='${sessionScope["drac.text.no"]}'/>' onclick='Javascript:queryTimeWin.hide();'/></center>";
        } else {
            box.innerHTML = result + "<p><center><input type='button' value='<c:out value='${sessionScope["drac.schedule.queryPath.close"]}'/>' onclick='Javascript:queryTimeWin.hide();'/></center>";
        }
    }
    queryTimeWin.load("div", "loadingDiv");
}

function disableChannelComboBox(channelId, disabled) {
    var channelSelect = document.getElementById(channelId);
    if (channelSelect) {
        channelSelect.disabled = disabled;
        channelSelect.options.length = 1;
        channelSelect.options[0] = new Option('<c:out value='${sessionScope["drac.schedule.create.channel.auto"]}'/>', -1);
    }
}

function disablePortComboBox(portId, disabled) {
    var tnaSelect = document.getElementById(portId);
    if (tnaSelect) {
        tnaSelect.disabled = disabled;
        if (disabled) {
            tnaSelect.options.length = 0;
            tnaSelect.options[0] = new Option('<c:out value='${sessionScope["drac.schedule.create.noports"]}'/>');

            if (portId == 'srcTna')
            {
               document.getElementById('srcFacLabel').value = "";
            }
            else
            {
               document.getElementById('destFacLabel').value = "";
            }
        }
    }
}

function disableWavelengthComboBox(wavelengthId, disabled) {
    var wavelengthSelect = document.getElementById(wavelengthId);
    if (wavelengthSelect) {
        wavelengthSelect.disabled = disabled;
        if (disabled) {
            wavelengthSelect.options.length = 0;
            wavelengthSelect.options[0] = new Option('<c:out value='${sessionScope["drac.schedule.create.nowavelengths"]}'/>');
        }
    }
}

function disableResourceGroupBox(groupId, disabled) {
    var groupSelect = document.getElementById(groupId);
    if (groupSelect) {
        groupSelect.disabled = disabled;
        if (disabled) {
            groupSelect.options.length = 0;
            groupSelect.options[0] = new Option('<c:out value='${sessionScope["drac.schedule.create.error.noresgroup"]}'/>');
        }
    }
}

function disableUserGroupBox(groupId, disabled) {
    var groupSelect = document.getElementById(groupId);
    if (groupSelect) {
        groupSelect.disabled = disabled;
        if (disabled) {
            groupSelect.options.length = 0;
            groupSelect.options[0] = new Option('<c:out value='${sessionScope["drac.schedule.create.error.nousergroup"]}'/>');
        }
    }
}



function displayOccurrenceWarning(occurs) {
    return confirm("<c:out value='${sessionScope["drac.schedule.create.warning1"]}'/> " + occurs +
                    " <c:out value='${sessionScope["drac.schedule.create.warning2"]}'/>\n" +
                    "<c:out value='${sessionScope["drac.schedule.create.warning3"]}'/>");
}


function initializeDate() {
  mydate=new Date();

  <% if (!createForm.getStartdate().equals("")) { %>
        setStartDate('<c:out value="${CreateScheduleForm.getStartdate()}"/>');
  <% } else { %>
        setStartDateToday();
  <% } %>

  <% if (!createForm.getEnddate().equals("")) { %>
        setEndDate('<c:out value="${CreateScheduleForm.getEnddate()}"/>');
  <% } else { %>
        setEndDateToday();
  <% } %>

  <% if (!createForm.getStartTime().equals("")) { %>
        setStartTime('<c:out value="${CreateScheduleForm.getStartTime()}"/>');
  <% } else { %>
       setStartTimeDefault();
  <% } %>

  <% if (!createForm.getEndTime().equals("")) { %>
       setEndTime('<c:out value="${CreateScheduleForm.getEndTime()}"/>');
  <% } else { %>
       setEndTimeDefault();
  <% } %>

  <% if (!createForm.getMonthlyDay().equals("")) { %>
        document.CreateScheduleForm.monthlyDay.value="<c:out value='${CreateScheduleForm.getMonthlyDay()}'/>";
  <% } else { %>
        document.CreateScheduleForm.monthlyDay.value=mydate.getDate();
  <% } %>

  <% if (!createForm.getYearlyDay().equals("")) { %>
        document.CreateScheduleForm.yearlyDay.value="<c:out value='${CreateScheduleForm.getYearlyDay()}'/>";
  <% } else { %>
            document.CreateScheduleForm.yearlyDay.value=mydate.getDate();
  <% } %>

  <% if (!createForm.getYearlyMonth().equals("")) { %>
        document.CreateScheduleForm.yearlyMonth.value="<c:out value='${CreateScheduleForm.getYearlyMonth()}'/>";
  <% } else { %>
            document.CreateScheduleForm.yearlyMonth.value=mydate.getMonth();
  <% } %>

  <% if (!createForm.getRecEndDate().equals("")) { %>
        setEndByDate("<c:out value='${CreateScheduleForm.getRecEndDate()}'/>");
  <% } %>

  <% if (createForm.getNumOccur() != 1) { %>
        document.CreateScheduleForm.occurrences.value="<c:out value='${CreateScheduleForm.getNumOccur()}'/>";
  <% } %>

    updateDuration();

  /* var dayOfWeek = mydate.getDay();
  if (dayOfWeek == 0) document.CreateScheduleForm.weeklySun.checked = true;
  else if (dayOfWeek == 1) document.CreateScheduleForm.weeklyMon.checked = true;
  else if (dayOfWeek == 2) document.CreateScheduleForm.weeklyTue.checked = true;
  else if (dayOfWeek == 3) document.CreateScheduleForm.weeklyWed.checked = true;
  else if (dayOfWeek == 4) document.CreateScheduleForm.weeklyThu.checked = true;
  else if (dayOfWeek == 5) document.CreateScheduleForm.weeklyFri.checked = true;
  else if (dayOfWeek == 6) document.CreateScheduleForm.weeklySat.checked = true;
 */
}

function refreshRecurrence() {
    obj = (document.getElementsByName("recurrence"))[0];
    if (${CreateScheduleForm.recurrence}) {
        obj.checked = true;
    } else {
        obj.checked= false;
    }
    handleRecurrence();

    if ('${CreateScheduleForm.frequency}' == 'Weekly') {
        obj = (document.getElementsByName("frequency"))[1];
        obj.checked = true;
        handleClickWeekly();
    } else if ('${CreateScheduleForm.frequency}' == 'Monthly') {
        obj = (document.getElementsByName("frequency"))[2];
        obj.checked = true;
        handleClickMonthly();
    } else if ('${CreateScheduleForm.frequency}' == 'Yearly') {
        obj = (document.getElementsByName("frequency"))[3];
        obj.checked = true;
        handleClickYearly();
    } else {
        obj = (document.getElementsByName("frequency"))[0];
        obj.checked = true;
        handleClickDaily();
    }

}

function refreshAdvancedOptions()
{
    if ('${CreateScheduleForm.algorithm}' == 'default') {
        document.getElementById("algorithm").selectedIndex = 0;
    } else if ('${CreateScheduleForm.algorithm}' == 'spf') {
        document.getElementById("algorithm").selectedIndex = 1;
    } else if ('${CreateScheduleForm.algorithm}' == 'cspf') {
        document.getElementById("algorithm").selectedIndex = 2;
    }
    routingAlgorithmChange('algorithm');
}

function alertInvalidPattern() {
    alert("<c:out value='${sessionScope["drac.schedule.create.rec.invalid"]}'/>");
}


function warning31Days(day) {
    alert("<c:out value='${sessionScope["drac.schedule.rec.warning.31st.1"]}'/> " + day +
        " <c:out value='${sessionScope["drac.schedule.rec.warning.31st.2"]}'/>\n" +
        "<c:out value='${sessionScope["drac.schedule.rec.warning.31st.3"]}'/>");
}

function showWavelengthWarning(wavelength, forSource) {
    if (forSource) {
        alert("<c:out value='${sessionScope["drac.schedule.create.wavelength.warning1.src"]}'/>\n" +
            "<c:out value='${sessionScope["drac.schedule.create.wavelength.warning2"]}'/> " + wavelength);
    } else {
        alert("<c:out value='${sessionScope["drac.schedule.create.wavelength.warning1.dest"]}'/>\n" +
            "<c:out value='${sessionScope["drac.schedule.create.wavelength.warning2"]}'/> " + wavelength);
    }
}

function toggleSrcFilter()
{
    handleClick('srcFilters1');
    handleClick('srcFilters2');
    handleClick('srcFilters3');
    swapText("srcFilterLabel", "<c:out value='${sessionScope["drac.schedule.create.showFilter"]}'/>", "<c:out value='${sessionScope["drac.schedule.create.hideFilter"]}'/>");
}

function toggleDestFilter()
{
    handleClick('destFilters1');
    handleClick('destFilters2');
    handleClick('destFilters3');
    swapText("destFilterLabel", "<c:out value='${sessionScope["drac.schedule.create.showFilter"]}'/>", "<c:out value='${sessionScope["drac.schedule.create.hideFilter"]}'/>");
}


function resetFields()
{
    document.CreateScheduleForm.reset();

    if ('${CreateScheduleForm.srcResGroup}' != '')
        document.CreateScheduleForm.srcResGroup.options[0] = new Option('${CreateScheduleForm.srcResGroup}','${CreateScheduleForm.srcResGroup}');

    if ('${CreateScheduleForm.srcTna}' != '')
        document.CreateScheduleForm.srcTna.options[0] = new Option('${CreateScheduleForm.srcTna}','${CreateScheduleForm.srcTna}');

    if ('${CreateScheduleForm.srcWavelength}' != '')
        document.CreateScheduleForm.srcWavelength.options[0] = new Option('${CreateScheduleForm.srcWavelength}','${CreateScheduleForm.srcWavelength}');

    if ('${CreateScheduleForm.sourceChannel}' != '-1') {
        document.CreateScheduleForm.srcChannel.disabled = false;
        document.CreateScheduleForm.srcChannel.options[0] = new Option('${CreateScheduleForm.sourceChannel}','${CreateScheduleForm.sourceChannel}');
    }

    if ('${CreateScheduleForm.destResGroup}' != '')
        document.CreateScheduleForm.destResGroup.options[0] = new Option('${CreateScheduleForm.destResGroup}','${CreateScheduleForm.destResGroup}');

    if ('${CreateScheduleForm.destTna}' != '')
        document.CreateScheduleForm.destTna.options[0] = new Option('${CreateScheduleForm.destTna}','${CreateScheduleForm.destTna}');

    if ('${CreateScheduleForm.destWavelength}' != '')
        document.CreateScheduleForm.destWavelength.options[0] = new Option('${CreateScheduleForm.destWavelength}','${CreateScheduleForm.destWavelength}');

    if ('${CreateScheduleForm.destChannel}' != '-1') {
        document.CreateScheduleForm.destChannel.disabled = false;
        document.CreateScheduleForm.destChannel.options[0] = new Option('${CreateScheduleForm.destChannel}','${CreateScheduleForm.destChannel}');
    }

    initializeDate();
    refreshRecurrence();
    refreshAdvancedOptions();

    if (document.CreateScheduleForm.billingGroup.options.length == 0) {
         disableComboBoxes(true);
    }
}

dCal = new DRACCalendar("<c:out value='${myLanguage}'/>", "long", serverDigitalTime);
startDateId = "startdate";
endDateId = "enddate";
startTimeId = "starttime";
endTimeId = "endtime";
endByDateId = "endByDate";
dCal.addListener(createSchDateChange);
//initRecurrence("monthlyDay", "yearlyDay", "yearlyMonth");
// Registered html select elements that may overlap with the calendar popup.
if (navigator.appVersion.indexOf("MSIE") != -1) {
    dCal.registerSelect('enddate', 'srcGroup', null);

}

// Initialize the pulldown start/end time menues.
var startTimeMenu = new EditableList('starttime', 'startTimeLayer', 'starttimes');

var endTimeMenu = new EditableList('endtime', 'endTimeLayer', 'endtimes');

// I think this is needed for the pulldown menues to function correctly.
function timeMouseSelect(e)
{
startTimeMenu.mouseSelect(0);
endTimeMenu.mouseSelect(0);
}

document.onmousedown = timeMouseSelect;

startTimeMenu.setChangeListener(createSchTimeChange);
endTimeMenu.setChangeListener(createSchTimeChange);
resetFields();
adjustSrcResGroups(document.getElementById('srcGroup'));
adjustDestResGroups(document.getElementById('destGroup'));


var durSub = new HoldButton("", durationSub);
var durAdd = new HoldButton("", durationAdd);
var mB = new HoldButton("", monthBack);
var mF = new HoldButton("", monthForward);
var yB = new HoldButton("", yearBack);
var yF = new HoldButton("", yearForward);
var occurUp = new HoldButton("", addOccurrence);
var occurDown = new HoldButton("", subtractOccurrence);


</script>
