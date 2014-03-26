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

package com.nortel.appcore.app.drac.server.webserver.struts.general.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

public final class LoginForm extends ValidatorForm {
	private static final long serialVersionUID = 1L;
	private String userID = DracConstants.EMPTY_STRING;
	private String password = DracConstants.EMPTY_STRING;
	private int numInvalidLogin = 0;
	private String lastLoginAddress = DracConstants.EMPTY_STRING;
	private List<String> locationOfInvalidAttempts = new ArrayList<String>();
	private String lastLoginDate = DracConstants.EMPTY_STRING;
	private String timeZone = DracConstants.EMPTY_STRING;

	/**
	 * @return the lastLoginAddress
	 */
	public String getLastLoginAddress() {
		return lastLoginAddress;
	}

	/**
	 * @return the lastLoginDate
	 */
	public String getLastLoginDate() {
		return lastLoginDate;
	}

	/**
	 * @return the locationOfInvalidAttempts
	 */
	public List<String> getLocationOfInvalidAttempts() {
		return locationOfInvalidAttempts;
	}

	/**
	 * @return the numInvalidLogin
	 */
	public int getNumInvalidLogin() {
		return numInvalidLogin;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * @param lastLoginAddress
	 *          the lastLoginAddress to set
	 */
	public void setLastLoginAddress(String lastLoginAddress) {
		this.lastLoginAddress = lastLoginAddress;
	}

	/**
	 * @param lastLoginDate
	 *          the lastLoginDate to set
	 */
	public void setLastLoginDate(String lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	/**
	 * @param locationOfInvalidAttempts
	 *          the locationOfInvalidAttempts to set
	 */
	public void setLocationOfInvalidAttempts(
	    List<String> locationOfInvalidAttempts) {
		this.locationOfInvalidAttempts = locationOfInvalidAttempts;
	}

	/**
	 * @param numInvalidLogin
	 *          the numInvalidLogin to set
	 */
	public void setNumInvalidLogin(int numInvalidLogin) {
		this.numInvalidLogin = numInvalidLogin;
	}

	/**
	 * @param password
	 *          the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * @param userID
	 *          the userID to set
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

}
