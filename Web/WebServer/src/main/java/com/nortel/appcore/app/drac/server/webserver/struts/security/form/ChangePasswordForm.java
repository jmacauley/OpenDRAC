/**
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

package com.nortel.appcore.app.drac.server.webserver.struts.security.form;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on 19-Jan-07
 */
public final class ChangePasswordForm extends ValidatorForm {
	private static final long serialVersionUID = -5082712089134159838L;
	private String oldPassword = DracConstants.EMPTY_STRING;
	private String newPassword1 = DracConstants.EMPTY_STRING;
	private String newPassword2 = DracConstants.EMPTY_STRING;

	/**
	 * @return the newPassword1
	 */
	public String getNewPassword1() {
		return newPassword1;
	}

	/**
	 * @return the newPassword2
	 */
	public String getNewPassword2() {
		return newPassword2;
	}

	/**
	 * @return the oldPassword
	 */
	public String getOldPassword() {
		return oldPassword;
	}

	/**
	 * @param newPassword1
	 *          the newPassword1 to set
	 */
	public void setNewPassword1(String newPassword1) {
		this.newPassword1 = newPassword1;
	}

	/**
	 * @param newPassword2
	 *          the newPassword2 to set
	 */
	public void setNewPassword2(String newPassword2) {
		this.newPassword2 = newPassword2;
	}

	/**
	 * @param oldPassword
	 *          the oldPassword to set
	 */
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

}
