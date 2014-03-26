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

var vis=0;
function Adv(layer)
{
 if(vis==1)
  {
    document.getElementById(layer).style.visibility='hidden';
    vis=0;
  }
  else
  {
    document.getElementById(layer).style.visibility='visible';
    vis=1;
  }
}

function checkin(layer)
{
		//for start time
        var srtval = document.frm.starttime.value;
        if((srtval==null)||(srtval=="")||(srtval==" "))
        {
                alert('Enter a valid Start Time');
               // document.frm.starttime.value="";
                return false;
        }
        var n1=srtval.indexOf(':');
        var hr1= srtval.substring(0,n1);
        var m1=n1+1;
        var min1 = srtval.substring(m1,srtval.indexOf(' '));
        if((hr1 >12)||(hr1<0)|| isNaN(hr1))
        {
                alert('Enter a valid Start Time');
                //document.frm.starttime.value="";
                return false;
        }
		if((n1<0)||(srtval.indexOf(' ')<0))
		{
			alert('Enter a valid Start Time');
                //document.frm.starttime.value="";
                return false;
		}
        if((min1>60)||(min1<0)||isNaN(min1))
         {
                alert('Enter a valid Start Time');
               // document.frm.starttime.value="";
                return false;
        }
//for end time
		var endval= document.frm.endtime.value;
        if((endval==null)||(endval=="")||(endval==" "))
        {
                alert('Enter a valid End Time');
               // document.frm.endtime.value="";
                return false;
        }
        var n2=endval.indexOf(':');
        var hr2= endval.substring(0,n2);
        var m2=n2+1;
        var min2 = endval.substring(m2,endval.indexOf(' '));
		//alert(min2);
        if((hr2 >12)||(hr2<0)|| isNaN(hr2))
        {
                alert('Enter a valid End Time');
                //document.frm.endtime.value="";
                return false;
        }
		if((n2<0)||(endval.indexOf(' ')<0))
		{
			alert('Enter a valid End Time');
                //document.frm.endtime.value="";
                return false;
		}
        if((min2>60)||(min2<0)||isNaN(min2))
         {
                alert('Enter a valid End Time');
               // document.frm.endtime.value="";
                return false;
        }
		var c1=srtval.indexOf(' ');
		//alert(c1);
		d1=c1+1;
		//alert(d1)
		var tod1=srtval.charAt(d1);
		//alert(tod1);
		if ((tod1=='P') && (hr1!=12))
		{
			hr1= parseInt(hr1)+12;
			   //alert(hr1);
		}
		//alert(hr1);
		var c2=endval.indexOf(' ');
			//alert(c2);
			d2=c2+1;
			//alert(d2);
			var tod2=endval.charAt(d2);
			//alert(tod2);
		var durmin='';
		var durhr='';
			if ((tod2=='P') && (hr2!=12))
			{
					hr2= parseInt(hr2) + 12;
				   // alert(hr2);
			}
		//alert(hr2);
		if(parseInt(hr2) < parseInt(hr1))
		{	
			//alert(hr2 +' '+ hr1);
			alert('End Time is less than Start Time');
			document.frm.dur_min.value='';
			document.frm.dur_hr.value='';
			return false;
		}
		else if(hr2==hr1)
		{
			if(min2< min1)
			{
				alert('End Time is less than Start Time');
				document.frm.dur_min.value='';
				document.frm.dur_hr.value='';
				return false;
			}
			else
			{
				durmin = parseInt(min2)-parseInt(min1);
				durhr ='0';
			}
		}
		else if(parseInt(hr2) > parseInt(hr1))
		{
			if(parseInt(min2)>=parseInt(min1))
			{	
				//alert('me');
				durmin = parseInt(min2)-parseInt(min1);
				durhr =  parseInt(hr2)-parseInt(hr1);
			}
			else 
			{
				min2=parseInt(min2)+60;
				hr2=parseInt(hr2)-1;
				//alert(hr2);
				durmin = parseInt(min2)-parseInt(min1);
				durhr =  parseInt(hr2)-parseInt(hr1);
			}
		}
		document.frm.dur_min.value= durmin;
        document.frm.dur_hr.value= durhr;
		if(AdvVal(layer)==false)
		{
			return false;
		}
}
function AdvVal(layer)
{
	if(document.getElementById(layer).style.visibility=='visible')
	{
		var cost = document.frm.cost.value;
		var metric = document.frm.metric.value;
		var hop = document.frm.hop.value;
		var emailID = document.frm.email;
		var srlg = document.frm.srlg.value;
		if ((emailID.value==null)||(emailID.value==""))
                {
                }
		else
		{
			if (echeck(emailID.value)==false)
			{
				return false;
			}
		}
		if(isNaN(cost))
		{
			alert('Cost is only numeric');
			//document.frm.cost.focus();
			return false;
		}
		if(isNaN(metric))
		{
			alert('Metric is only numeric');
			//document.frm.metric.focus();
			return false;
		}
		if(isNaN(hop))
		{
			alert('Hop is only numeric');
			//document.frm.hop.focus();
			return false;
		}
	}
	else if (document.getElementById(layer).style.visibility=='hidden')
	{
		return true;	
	}
}
function echeck(str) 
{
		var at="@";
		var dot=".";
		var lat=str.indexOf(at);
		var lstr=str.length;
		var ldot=str.indexOf(dot);
		if (str.indexOf(at)==-1)
		{
		  alert("Invalid E-mail ID");
		   return false;
		}
		if (str.indexOf(at)==-1 || str.indexOf(at)==0 || str.indexOf(at)==lstr)
		{
		   alert("Invalid E-mail ID");
		   return false;
		}
		if (str.indexOf(dot)==-1 || str.indexOf(dot)==0 || str.indexOf(dot)==lstr)
		{
		    alert("Invalid E-mail ID");
		    return false;
		}
		 if (str.indexOf(at,(lat+1))!=-1)
		 {
		    alert("Invalid E-mail ID");
		    return false;
		 }
		 if (str.substring(lat-1,lat)==dot || str.substring(lat+1,lat+2)==dot)
		 {
		    alert("Invalid E-mail ID");
		    return false;
		 }
		 if (str.indexOf(dot,(lat+2))==-1)
		 {
		    alert("Invalid E-mail ID");
		    return false;
		 }
		 if (str.indexOf(" ")!=-1)
		 {
		    alert("Invalid E-mail ID");
		    return false;
		 }
 }
function diffsrt()
{
	document.textfrm.combotext.value = document.textfrm.combosel.options[document.textfrm.combosel.selectedIndex].value;
	//alert(document.textfrm.combosel.selectedIndex);
	document.textfrm.combotext1.value = document.textfrm.combosel1.options[document.textfrm.combosel1.selectedIndex].value;
	//var ind=document.getElementById('combosel').selectedIndex;
	// var ind1=document.getElementById('combosel1').selectedIndex;
	//document.getElementById('combotext').value = document.getElementById('combosel').options[ind].value;
	//document.getElementById('combotext1').value = document.getElementById('combosel1').options[ind1].value;
	//document.getElementById('combotext1').value = document.textfrm.combosel1.options[document.textfrm.combosel1.selectedIndex].value;
	var srtval= document.textfrm.combotext.value;
	//alert(srtval);
	 if((srtval==null)||(srtval=="")||(srtval==" "))
       	 {
                alert('Enter a valid Start Time');
		document.textfrm.combotext.value="";
       	 }
	var n=srtval.indexOf(':');
        var hr1= srtval.substring(0,n);
	//alert(hr1);
        var m=n+1;
        var min1= srtval.substring(m,srtval.indexOf(' '));
	//alert(min1);
	//alert(srtval.indexOf(' '));
        if((hr1 >12)||(hr1<0)||(n<0)||(srtval.indexOf(' ')<0))
        {
                alert('Enter a valid Start Time');
		document.textfrm.combotext.value="";
        }
        if((min1>60)||(min1<0)||isNaN(min1))
        {
                alert('Enter a valid Start Time');
		document.textfrm.combotext.value="";
        }
	var endval=document.textfrm.combotext1.value;
        //alert(endval);
        if((endval==null)||(endval=="")||(endval==" "))
        {
                alert('Enter a valid End Time');
                document.textfrm.combotext1.value="";
        }
        var n1=endval.indexOf(':');
        var hr2= endval.substring(0,n1);
        //alert(hr2);
        var m1=n1+1;
        var min2= endval.substring(m1,endval.indexOf(' '));
        //alert(min2);
	//alert(endval.indexOf(' '));
        if((hr2 >12)||(hr2<0)||(n1<0)||(endval.indexOf(' ')<0))
        {
                alert('Enter a valid End Time');
                document.textfrm.combotext1.value="";
        }
        if((min2>60)||(min2<0)||isNaN(min2))
        {
                alert('Enter a valid End Time');
                document.textfrm.combotext1.value="";
        }
	var c1=srtval.indexOf(' ');
	//alert(c1);
	d1=c1+1;
	//alert(d1)
	var tod1=srtval.charAt(d1);
	//alert(tod1);
	if ((tod1=='P') && (hr1!=12))
	{
		hr1= parseInt(hr1)+12;
	       //alert(hr1);
	}
	//alert(hr1);
	var c2=endval.indexOf(' ');
        //alert(c2);
        d2=c2+1;
        //alert(d2);
        var tod2=endval.charAt(d2);
        //alert(tod2);
	var durmin='';
	var durhr='';
        if ((tod2=='P') && (hr2!=12))
        {
                hr2= parseInt(hr2) + 12;
               // alert(hr2);
        }
	//alert(hr2);
	if(parseInt(hr2) < parseInt(hr1))
	{	
		//alert(hr2 +' '+ hr1);
		alert('End Time is less than Start Time');
                document.textfrm.dur_min.value='';
		document.textfrm.dur_hr.value='';
	}
	else if(hr2==hr1)
	{
		if(min2< min1)
		{
			alert('End Time is less than Start Time');
	                document.textfrm.dur_min.value='';
			document.textfrm.dur_hr.value='';
		}
		else
		{
			durmin = parseInt(min2)-parseInt(min1);
			durhr ='0';
		}
	}
	else if(parseInt(hr2) > parseInt(hr1))
	{
		if(parseInt(min2)>=parseInt(min1))
		{	
			//alert('me');
			durmin = parseInt(min2)-parseInt(min1);
			durhr =  parseInt(hr2)-parseInt(hr1);
		}
		else 
		{
			min2=parseInt(min2)+60;
			hr2=parseInt(hr2)-1;
			//alert(hr2);
			durmin = parseInt(min2)-parseInt(min1);
			durhr =  parseInt(hr2)-parseInt(hr1);
		}
	}
	/*document.textfrm.duration.value = document.textfrm.combotext1.value - document.textfrm.combotext.value;
	if((document.textfrm.combotext1.value - document.textfrm.combotext.value)<0)
	{
		alert('End Time is less than start time');
		document.textfrm.duration.value='';
	}*/
	 document.textfrm.dur_min.value= durmin;
         document.textfrm.dur_hr.value= durhr;	
}
function ValidateRec()
{
	 if((document.textfrm.dur_min.value=="")||(document.textfrm.dur_hr.value==""))
        {
         alert("Duration cannot be empty");
         return false;
        }
	//for start time

	    var val= document.textfrm.combotext.value;
        if((val==null)||(val=="")||(val==" "))
        {
         alert('Enter a valid Start Time');
         return false;
        }

        var n=val.indexOf(':');
        var hr= val.substring(0,n);
        var m=n+1;
        var min = val.substring(m,val.indexOf(' '));
        if((hr >12)||(hr<0)||isNaN(hr))
        {
         alert('Enter a valid Start Time');
         return false;
        }
        if((n<0)||(val.indexOf(' ')<0))
        {
          alert('Enter a valid Start Time');
          return false;
        }

        if((min>60)||(min<0)||isNaN(min))
         {
          alert('Enter a valid Start Time');
          return false;
        }

//for end time

        var endval= document.textfrm.combotext1.value;
        if((endval==null)||(endval=="")||(endval==" "))
        {
          alert('Enter a valid End Time');
          return false;
        }

        var n1=endval.indexOf(':');
        var hr1= endval.substring(0,n1);
        var m1=n1+1;
        var min1 = endval.substring(m1,endval.indexOf(' '));
        if((hr1 >12)||(hr1<0)||isNaN(hr1))
        {
         alert('Enter a valid End Time');
         return false;
        }

        if((n1<0)||(endval.indexOf(' ')<0))
        {
         alert('Enter a valid End Time');
         return false;
        }

        if((min1>60)||(min1<0)||isNaN(min1))
        {
            alert('Enter a valid End Time');
            return false;
	     }

		 if(textVal()==false)
 		   return false;
		if(document.textfrm.meet[2].checked)
		{
			 var month = document.textfrm.daymonth.value;
			 if (month>28)
			 {
				var agree = confirm('Some months have fewer than '+month+' days. For these months the occurence will fall on the last day of the month');
				if (agree)
				{
					return true;
				}
				else
				{
					return false;
				}
			 }
		}
		else if (document.textfrm.meet[3].checked)
		{
			var month = document.textfrm.yearday.value;
			if (month>28)
			 {
				var agree = confirm('Some months have fewer than '+month+' days. For these months the occurence will fall on the last day of the month');
				if (agree)
				{
					return true;
				}
				else
				{
					return false;
				}
			 }
		}
		else
		{
			return true;
		}
 }
function rec(targeturl)
{
	window.open(targeturl,"",'toolbar=1,status=1,menubar=1,resize=1,location=1,replace=1,scrollbar=1');
}
function dateCheck(startdate,enddate)
{
	if (Date.parse(startdate.value) > Date.parse(enddate.value)) 
	{
		alert("End Date less than Start Date");
		return false;
	}
        /*else 
        {
                return true;
        }*/
}
function check(startdate,enddate,layer)
{
	if(document.frm.schname.value=="")
        {
                alert('Source Name cannot be empty');
                //document.frm.srclayer.focus();
                return false;
        }

	if(document.frm.srclayer.value=="")
	{
		alert('Source EndPoint Layer cannot be empty');
		//document.frm.srclayer.focus();
		return false;
	}

	if(document.frm.destlayer.value=="")
        {
                alert('Destination EndPoint Layer cannot be empty');
		//document.frm.destlayer.focus();
                return false;
        }
	if(document.frm.srctna.value=="")
        {
                alert('Source TNA cannot be empty');
                //document.frm.srctna.focus();
                return false;
        }
	if(document.frm.desttna.value=="")
        {
                alert('Destination TNA cannot be empty');
                //document.frm.desttna.focus();
                return false;
        }
	if(document.frm.startdate.value=="")
        {
                alert('Start Date cannot be empty');
                //document.frm.startdate.focus();
                return false;
        }
	if(document.frm.enddate.value=="")
        {
                alert('End Date cannot be empty');
               //document.frm.enddate.focus();
                return false;
        }
        if(checkNum(document.frm.rate) == false)
        {
		return false;
        }
	if(checkin(layer)==false)
        {
                return false;
        }
	if(dateCheck(startdate,enddate)==false)
	{
		return false;
	}
	return true;
}
function checkNum(w)
{
	var weight = w.value;
	if((weight==null)||(weight==""))
	{
		alert('Rate cannot be empty');
		return false;
	}
	if((isNaN(weight))||(weight==" "))
        {
                alert('Rate can take only numbers');
                return false;
        }
}

/* Cobobox support from http://www.vttoth.com/htmcombo.htm */

var fActiveMenu = false;
var oOverMenu = false;
function mouseSelect(e)
{
	if (fActiveMenu)
	{
		if (oOverMenu == false)
		{
			oOverMenu = false;
			document.getElementById(fActiveMenu).style.display = "none";
			fActiveMenu = false;
			return false;
		}
		return false;
	}
	return true;
}

function menuActivate(idEdit, idMenu, idSel)
{
	if (fActiveMenu) return mouseSelect(0);

	oMenu = document.getElementById(idMenu);
	oEdit = document.getElementById(idEdit);
	nTop = oEdit.offsetTop + oEdit.offsetHeight;
	nLeft = oEdit.offsetLeft;
	while (oEdit.offsetParent != document.body)
	{
		oEdit = oEdit.offsetParent;
		nTop += oEdit.offsetTop;
		nLeft += oEdit.offsetLeft;
	}
	oMenu.style.left = nLeft;
	oMenu.style.top = nTop;
	oMenu.style.display = "";
	fActiveMenu = idMenu;
	document.getElementById(idSel).focus();
	return false;
}

function textSet(idEdit, text)
{
	document.getElementById(idEdit).value = text;
	oOverMenu = false;
	mouseSelect(0);
	document.getElementById(idEdit).focus();
}

function comboKey(idEdit, idSel)
{
	if (window.event.keyCode == 13 || window.event.keyCode == 32)
		textSet(idEdit,idSel.value);
	else if (window.event.keyCode == 27)
	{
		mouseSelect(0);
		document.getElementById(idEdit).focus();
	}
}

document.onmousedown = mouseSelect;

function default1()
{	
	document.all.my.style.visibility ='hidden';
}
function set(What,Value)
{
	//alert(What + '\n' + Value);
    if (document.layers && document.layers[What] != null) document.layers[What].visibility = Value;
    else if (document.all) eval('document.all.'+What+'.style.visibility ="'+ Value+'"');
}
function clicked(Form,Radio,Layer)
{
    for (var i=0; i<Form[Radio].length; i++)
	{
        if (Form[Radio][i].checked) set(Layer,Form[Radio][i].value);
    }
}
function SwitchMenu(obj)
{
	 if(document.getElementById)
	 {
		var el = document.getElementById(obj);
        if(el.style.display == "none")
			el.style.display = "block";
		else
			el.style.display = "none";
		
	}
}
function clicked2()
{
	document.myform1.group.value="all";
	document.myform1.group.disabled=true;
	document.myform1.theuser.disabled=false;
	
}
function clicked1()
{
	document.myform1.group.disabled=false;
	document.myform1.theuser.disabled=true;
}

function textVal()
{
 if(document.getElementById('Layer8').style.visibility=='visible')
 {
   var days=document.textfrm.daymonth.value;
   if(parseInt(days) > 31)
	 {
	   alert('Please enter a valid day');
       return false;
	 }

   if(parseInt(days) < 0)
	 {
	  alert('Please enter a valid day');
	  return false;
	 }

   if(isNaN(days))
	 {
      alert('Please enter a valid day');
      return false;
	 }

   if(days=='')
	 {
	  alert('Please enter a valid day');
      return false;
	 }
  }

  if(document.getElementById('Layer9').style.visibility=='visible')
  {
    var day=document.textfrm.yearday.value;
 
    if(parseInt(day) > 31)
	  {
	   alert('Please enter a valid day');
	   return false;
	  }

    if(parseInt(day) < 0)
	  {
		alert('Please enter a valid day');
        return false;
	  }
 
    if(isNaN(day))
	  {
		 alert('Please enter a valid day');
	     return false;
	  }
 
    if(day=='')
	  {
		alert('Please enter a valid day');
        return false;
	  }
 
    var month=document.textfrm.yearmonth.value;
    if(parseInt(month) > 12)
	  {
	   alert('Please enter a valid month');
	   return false;
	  }
 
    if(parseInt(month) < 0)
	  {
	   alert('Please enter a valid month');
       return false;
	  }

    if(isNaN(month))
	  {
		alert('Please enter a valid month');
        return false;
	  }

    if(month=='')
	  {
		alert('Please enter a valid month');
	     return false;
	  }
  }
}

function empty()
{
	document.myform1.txtDate.value==null;
	document.myform1.txtDate1.value==null;
	return true;
}

