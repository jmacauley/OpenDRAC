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

// This function makes some advanced options visible on single click 
function toggleAdvancedOptions(chkbox,layer) {

    if (chkbox.checked) {
        document.getElementById(layer).style.visibility = "visible";
    } else {
        document.getElementById(layer).style.visibility = "hidden";
    }
}
function dateCheck(startdate, enddate) {
			
    if (Date.parse(startdate.value) > Date.parse(enddate.value)) {
        alert("End Date less than Start Date");
        return false;
    }
    }

function checkQuery(startdate, enddate,Service,formname) {
		
    if (formname.memberGroup.value == "" &&formname.startdate.value == ""
        &&formname.enddate.value == "" && document.getElementById(Service).style.visibility == "hidden" ) {
        alert("Please enter a value");
        return false;
    }
    if(document.getElementById(Service).style.visibility == "visible")
    {
    	if(formname.memberGroup.value == "" &&formname.startdate.value == ""
        &&formname.enddate.value == "" && formname.srcLayer.value == ""
        &&formname.scheduleName.value == "" &&formname.destLayer.value == ""
        &&formname.status.value == "" &&formname.srcTNA.value == ""
        && formname.userName.value == "" && formname.destTNA.value == "")
        {
        alert("Please enter a value");
        return false;
        }
    }
   
    if (dateCheck(startdate, enddate) == false) {
        return false;
    }
    return true;
}

function checkNum(w) {
		
    var weight = w.value;
    if ((weight == null) || (weight == "")) {
    	
        alert("Rate cannot be empty");
        return false;
    }
    if ((isNaN(weight)) || (weight == " ")) {
        alert("Rate can take only numbers");
        return false;
    }
}


function AdvVal(formname) {
		return true;
		}
function checkin(frm,layer) {
		
	var strval = frm.addStartTime.value;
	
	var colon_start=strval.indexOf(':');
	var hour_start= strval.substring(0,colon_start);
	var m_start=colon_start+1;
	var min_start = strval.substring(m_start,strval.indexOf(' '));
	if(strval.length==7)
	{
//		alert('seven');
		if(isNaN(strval.charAt(0)))
		{
			alert('Please enter a valid time 0');
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
				alert('please enter a valid time 1');
				//document.newform.time.value="";
				return false;
		}
		if(isNaN(strval.charAt(2))||isNaN(strval.charAt(3)))
		{
			alert('Please enter a valid time 2');
			//document.newform.time.value="";
			return false;
		}
		if (strval.charAt(4)!=" ")
		{
			alert('please enter a valid time 3');
			//document.newform.time.value="";
			return false;
		}
		if ((!strval.charAt(5).equals('A'))||(!strval.charAt(5).equals('P')))
		{
			alert('please enter a valid time 4');
			//document.newform.time.value="";
			return false;
		}
		if(strval.charAt(6)!='M')
		{
			alert('please enter a valid time 5');
			//document.newform.time.value="";
			return false;
		}
	}
//	var n1 = srtval.indexOf(":");
//    var hr1 = srtval.substring(0, n1);
//    var m1 = n1 + 1;
//    var min1 = srtval.substring(m1, srtval.indexOf(" "));
//    if ((hr1 > 12) || (hr1 < 0) || isNaN(hr1)) {
//        alert("Enter a valid Start Time");
//                //document.frm.starttime.value="";
//        return false;
//    }
//    if ((n1 < 0) || (srtval.indexOf(" ") < 0)) {
//        alert("Enter a valid Start Time");
//                //document.frm.starttime.value="";
//        return false;
//    }
//    if ((min1 > 60) || (min1 < 0) || isNaN(min1)) {
//        alert("Enter a valid Start Time");
//               // document.frm.starttime.value="";
//        return false;
//    }
	else if(strval.length==8)
	{
		//alert('eight');
		if(isNaN(strval.charAt(0))||isNaN(strval.charAt(1)))
		{
			alert('Please enter a valid time 0');
			return false;
		}
		else{
			if((hour_start > 12)||(hour_start < 0))
			{
				alert('Enter a valid Start Time hours');
				return false;
			}
			if((min_start > 59)||(min_start < 0))
			{
				alert('Enter a valid Start Time mim');
				return false;
			}	
		}
		if (strval.charAt(2)!=":")
		{
			alert('please enter a valid time 1');
				return false;
		}
		if(isNaN(strval.charAt(3))||isNaN(strval.charAt(4)))
		{
			alert('Please enter a valid time 2');
				return false;
		}
		if (strval.charAt(5)!=" ")
		{
			alert('please enter a valid time 3');
				return false;
		}
		if ((strval.charAt(6)!='A')||(strval.charAt(6)!='P'))
		{
			alert('please enter a valid time 4');
				return false;
		}
		if(strval.charAt(7)!='M')
		{
			alert('please enter a valid time 5');
				return false;
		}
	}
	else
	{
		alert('Enter a valid time');
		return false;
	}
	
//for end time
    var endval = formname.addEndTime.value;
    if ((endval == null) || (endval == "") || (endval == " ")) {
        alert("Enter a valid End Time");
        	return false;
    }
    var n2 = endval.indexOf(":");
    var hr2 = endval.substring(0, n2);
    var m2 = n2 + 1;
    var min2 = endval.substring(m2, endval.indexOf(" "));
		//alert(min2);
    if ((hr2 > 12) || (hr2 < 0) || isNaN(hr2)) {
        alert("Enter a valid End Time");
                //document.frm.endtime.value="";
        return false;
    }
    if ((n2 < 0) || (endval.indexOf(" ") < 0)) {
        alert("Enter a valid End Time");
                //document.frm.endtime.value="";
        return false;
    }
    if ((min2 > 60) || (min2 < 0) || isNaN(min2)) {
        alert("Enter a valid End Time");
               // document.frm.endtime.value="";
        return false;
    }
    var c1 = srtval.indexOf(" ");
		//alert(c1);
    d1 = c1 + 1;
		//alert(d1)
    var tod1 = srtval.charAt(d1);
		//alert(tod1);
    if ((tod1 == "P") && (hr1 != 12)) {
        hr1 = parseInt(hr1) + 12;
			   //alert(hr1);
    }
		//alert(hr1);
    var c2 = endval.indexOf(" ");
			//alert(c2);
    d2 = c2 + 1;
			//alert(d2);
    var tod2 = endval.charAt(d2);
			//alert(tod2);
    var durmin = "";
    var durhr = "";
    if ((tod2 == "P") && (hr2 != 12)) {
        hr2 = parseInt(hr2) + 12;
				   // alert(hr2);
    }
		//alert(hr2);
    if (parseInt(hr2) < parseInt(hr1)) {	
			//alert(hr2 +' '+ hr1);
        alert("End Time is less than Start Time");
        formname.dur_min.value = "";
		formname.dur_hr.value = "";
        return false;
    } else {
        if (hr2 == hr1) {
            if (min2 < min1) {
                alert("End Time is less than Start Time");
                formname.dur_min.value = "";
                formname.dur_hr.value = "";
                return false;
            } else {
                durmin = parseInt(min2) - parseInt(min1);
                durhr = "0";
            }
        } else {
            if (parseInt(hr2) > parseInt(hr1)) {
                if (parseInt(min2) >= parseInt(min1)) {	
				//alert('me');
                    durmin = parseInt(min2) - parseInt(min1);
                    durhr = parseInt(hr2) - parseInt(hr1);
                } else {
                    min2 = parseInt(min2) + 60;
                    hr2 = parseInt(hr2) - 1;
				//alert(hr2);
                    durmin = parseInt(min2) - parseInt(min1);
                    durhr = parseInt(hr2) - parseInt(hr1);
                }
            }
        }
    }
    formname.dur_min.value = durmin;
    formname.dur_hr.value = durhr;
   
    if (AdvVal(layer,formname) == false) {
        return false;
    }
}

function AdvVal(Service,frm){
	
	//alert("AdvVal");
	
	
if (document.getElementById(Service).style.visibility == "visible")
	{
	
	var cost = frm.cost.value;
     
   var metric = frm.metric.value;
     
   var hop = frm.hop.value;
      
  var srlg = frm.srlg.value;
        
      
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
	}
	
else
	{
	if (document.getElementById(Service).style.visibility == "hidden")
	
	{
            return true;
     
   }
	}

}


function checkAdd(formname,Service) {

	
	//alert(formname.startdate.value);
	//alert(formname.enddate.value);

if (formname.startdate.value == "") {

        alert("Start Date cannot be empty");

		return false;

    }	

    if (formname.enddate.value == "") {
   	

        alert("End Date cannot be empty");

        return false;

   }
	
	if (checkin(formname,Service) == false) {
    	
	    return false;

	}	
          
    //alert("checkAdd");	
    if (checkNum(formname.addRate) == false) {
        return false;
    }
   	
    if (dateCheck(formname.startdate,formname.enddate) == false) {
        return false;
    }
    if (checkin(formname,layer) == false) {
	    return false;
	}		
	if(checkTimeLimit(formname)== false)
	{
		return false;
	}
	return true;
	
}
