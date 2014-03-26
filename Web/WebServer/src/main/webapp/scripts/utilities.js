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

function getMaxStringDisplay(s, maxLength)
{
  if(maxLength == null){
	  maxLength=30;
  }
  var out;
  if (s.length > maxLength)
  {
     out=s.substring(0,(maxLength - 3))+"...";
  }
  else
  {
     out=s;
  }

  return out;
}

function jsCopyDate(aDate)
{
  var newDate = new Date();
  newDate.setFullYear(aDate.getFullYear());
  newDate.setMonth(aDate.getMonth());
  newDate.setDate(aDate.getDate());
  newDate.setHours(aDate.getHours());
  newDate.setMinutes(aDate.getMinutes());
  newDate.setSeconds(aDate.getSeconds());
  return newDate;
}

function getGMTDisplay(mins)
{
    var negative = new Boolean(mins <= 0);
    <!-- here, the hrs should be just the quotient -->
    var hrs = parseInt(Math.abs(mins)/60);
    mins = Math.abs(mins)%60;
    return "GMT" + (negative==true ? "+" : "-") + (hrs < 10 ? "0" : "") + hrs + ":" + (mins < 10 ? "0" : "") + mins;
}

function sortSelectListBox(listId){
	alert(1)
   var listbox = document.getElementById(listId);
   arrTexts = new Array();
   arrValues = new Array();
   arrOldTexts = new Array();

   for(i=0; i<listbox.length; i++){
      arrTexts[i] = listbox.options[i].text;
      arrValues[i] = listbox.options[i].value;
      arrOldTexts[i] = listbox.options[i].text;
   }

   arrTexts.sort();

   for(i=0; i<listbox.length; i++){
	  listbox.options[i].text = arrTexts[i];
      for(j=0; j<listbox.length; j++){
         if (arrTexts[i] == arrOldTexts[j]){
        	listbox.options[i].value = arrValues[j];
            j = listbox.length;
         }
      }
   }
}
