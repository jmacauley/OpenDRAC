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

function handleClick(id)
{
  var obj = "";

  // Check browser compatibility
  if(document.getElementById)
    obj = document.getElementById(id);
  else if(document.all)
    obj = document.all[id];
  else if(document.layers)
    obj = document.layers[id];
  else
    return 1;

  if (!obj)
  {
    return 1;
  }
  else if (obj.style)
  {
    obj.style.display = ( obj.style.display != "none" ) ? "none" : "";
  }
  else
  {
    obj.visibility = "show";
  }
}

function swapImage(id)
{
  var obj = "";

  // Check browser compatibility
  if(document.getElementById)
  {
    obj = document.getElementById(id);
  }
  else if(document.all)
  {
    obj = document.all[id];
  }
  else if(document.layers)
  {
    obj = document.layers[id];
  }
  else
  {
    return 1;
  }

  if (obj.src.lastIndexOf("plus.gif") != -1)
  {
    obj.src = "/images/minus.gif";
  }
  else
  {
    obj.src = "/images/plus.gif";
  }

  return(false);
}

function swapText(id, text1, text2)
{
  var obj = "";

  // Check browser compatibility
  if(document.getElementById)
  {
    obj = document.getElementById(id);
  }
  else if(document.all)
  {
    obj = document.all[id];
  }
  else if(document.layers)
  {
    obj = document.layers[id];
  }
  else
  {
    return 1;
  }

  if (obj.innerHTML.lastIndexOf(text1) != -1)
  {
    obj.innerHTML = text2;
  }
  else
  {
    obj.innerHTML = text1;
  }

  return(false);
}
