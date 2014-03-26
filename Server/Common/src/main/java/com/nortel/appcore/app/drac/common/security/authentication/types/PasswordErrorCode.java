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

package com.nortel.appcore.app.drac.common.security.authentication.types;

public enum PasswordErrorCode {
	/*
	 * The key strings here map to the those defined in DRACConstants. (found in
	 * the WebServer package, inaccessible here). Still, I'd really like to keep
	 * things clean on the password exception handling...
	 */
	NO_ERROR("noError"), //
	ERROR_GENERAL("drac.security.changePassword.general"), //
	ERROR_RULES_NOT_FOUND("drac.security.changePassword.noEncodedRules"), //
	ERROR_OLDPW_INVALID("drac.security.changePassword.invalidOldPassword"), //
	ERROR_PW_NOT_MATCH("drac.security.changePassword.notSame"), //
	ERROR_PW_NOT_OLD("drac.security.changePassword.cannotBeOld"), //
	ERROR_PASSWD_ILLEGALCHARS("drac.security.changePassword.illegalChars"), //
	ERROR_PASSWD_MINLENGTH("drac.security.changePassword.minLength"), //
	ERROR_PASSWD_MAXLENGTH("drac.security.changePassword.maxLength"), //
	ERROR_PASSWD_MINALPHAVALUE("drac.security.changePassword.minAlphaValue"), //
	ERROR_PASSWD_MINDIGITVALUE("drac.security.changePassword.minDigitValue"), //
	ERROR_PASSWD_MINSPECIALVALUE("drac.security.changePassword.minSpecialValue"), //
	ERROR_PASSWD_MINDIFFERENT("drac.security.changePassword.minDifferent"), //
	ERROR_PASSWD_MIXEDALPHA("drac.security.changePassword.mixedAlpha"), //
	ERROR_PASSWD_INVALIDLIST("drac.security.changePassword.invalidList");

	private final String errorKey;

	private PasswordErrorCode(String errorKey) {
		this.errorKey = errorKey;
	}

	@Override
	public String toString() {
		return errorKey;
	}
}
