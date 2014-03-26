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

//
// DRAC Web GUI version 0.1 Alpha
//
// File: /scripts/EditableList.js
//
// Description:
// EditableList - Object supporting combo box associated with an editable
//  text field.

function EditableList(idEdit, idMenu, idSel, idParentContainer)
{
  this.idEdit = idEdit;
  this.oEdit = document.getElementById(idEdit);

  this.idMenu = idMenu;
  this.oMenu = document.getElementById(idMenu);

  this.idSel = idSel;
  this.oSel = document.getElementById(idSel);

  this.fActiveMenu = false;
  this.oOverMenu = false;
  this.changeListener = null;
  this.locked = false;
  
  if (idParentContainer != null)  
  {
  	this.parentContainer = document.getElementById(idParentContainer);
  } 
  else 
  {
  	this.parentContainer = document.body;
  }

  this.setChangeListener = function(changeListener)
  {
    this.changeListener = changeListener;
  } /* setChangeListener */

  this.overMenu = function(bool)
  {
    this.oOverMenu = bool;
  } /* overMenu */

  this.mouseSelect = function(e)
  {
    if (this.fActiveMenu)
    {
      if (this.oOverMenu == false)
      {
        this.oOverMenu = false;
        this.oMenu.style.display = "none";
        this.fActiveMenu = false;
        return false;
      }

      return false;
    }

    return true;

  } /* mouseSelect */

  this.activate = function()
  {
    if (this.fActiveMenu || this.locked == true)
    {
      return this.mouseSelect(0);
    }

    var oEdit = this.oEdit;
    nTop = oEdit.offsetTop + oEdit.offsetHeight;
    nLeft = oEdit.offsetLeft;
	
    //while (oEdit.offsetParent != document.body)
    while (oEdit.offsetParent != this.parentContainer)
    {
      oEdit = oEdit.offsetParent;
      nTop += oEdit.offsetTop;
      nLeft += oEdit.offsetLeft;
    }
    
    
  
    this.oMenu.style.left = nLeft + "px";
    this.oMenu.style.top = nTop + "px";
    this.oMenu.style.display = "";
    this.fActiveMenu = this.idMenu;
    
    for (i=0; i<this.oSel.options.length; i++) {
    	if (this.oSel.options[i].value == this.oEdit.value) {
    		this.oSel.selectedIndex = i;
    	}
    }
    
    this.oSel.focus();
  
    return false;
  
  } /* activate */

  this.textSet = function(text)
  {
    this.oEdit.value = text;
  
    this.oOverMenu = false;

    if (this.changeListener != null)
    {
      this.changeListener(this.oEdit);
    }
  
    this.mouseSelect(0);
    this.oEdit.focus();

  } /* textSet */

  this.comboKey = function()
  {
    if (window.event.keyCode == 13 || window.event.keyCode == 32)
    {
      this.textSet(oSel.value);
    }
    else if (window.event.keyCode == 27)
    {
      this.mouseSelect(0);
      this.oEdit.focus();
    }
  } /* comboKey */

  this.lock = function()
  {
    this.locked = true;

  } /* lock */

  this.unlock = function()
  {
    this.locked = false;

  } /* unlock */


} /* EditableList */