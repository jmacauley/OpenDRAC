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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionWarningCode;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.security.policy.types.UserPolicyProfile;

/**
 * @author elmak Cleanup
 */
public final class UserDetails implements Serializable {
	private static final long serialVersionUID = 1;
	private final String uid;
	private final List<String> selectedGroup = new ArrayList<String>();
	private String sessionID;
	private final AbstractCredential credential;
	private UserPolicyProfile userPolicyProfile;
	private SessionErrorCode errorCode;
	private List<SessionWarningCode> warningCode;

	
	public UserDetails(String userSessionID, String userId,
	    AbstractCredential userCredential) {
		sessionID = userSessionID;
		uid = userId;
		credential = userCredential;
	}

	public void addWarningCode(SessionWarningCode newWarningCode) {
		if (warningCode == null) {
			warningCode = new ArrayList<SessionWarningCode>();
		}
		warningCode.add(newWarningCode);
	}

	public AbstractCredential getCredential() {
		return credential;
	}

	public SessionErrorCode getErrorCode() {
		return errorCode;
	}

	public String getLoginAddress() {
		if (credential != null) {
			return credential.getIpAddress().toString();
		}
		return null;
	}

	public IPAddress getLoginIPAddress() {
		if (credential != null) {
			return credential.getIpAddress();
		}
		return null;
	}

	public String getSessionID() {
		return sessionID;
	}

	public String getUserID() {
		return uid;
	}

	public UserPolicyProfile getUserPolicyProfile() {
		return userPolicyProfile;
	}

	public List<SessionWarningCode> getWarningCode() {
		return warningCode;
	}

	public void setErrorCode(SessionErrorCode errorVal) {
		errorCode = errorVal;
	}

	/**
	 * Not being used in any useful fashion, remove
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public void setUserPolicyProfile(UserPolicyProfile userPolicyProfile) {
		this.userPolicyProfile = userPolicyProfile;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserDetails [sessionID=");
		builder.append(sessionID);
		builder.append(", uid=");
		builder.append(uid);
		builder.append(", credential=");
		builder.append(credential);
		builder.append(", selectedGroup=");
		builder.append(selectedGroup);
		builder.append(", warningCode=");
		builder.append(warningCode);
		builder.append(", errorCode=");
		builder.append(errorCode);
		builder.append(", userPolicyProfile=");
		builder.append(userPolicyProfile);
		builder.append("]");
		return builder.toString();
	}


}
