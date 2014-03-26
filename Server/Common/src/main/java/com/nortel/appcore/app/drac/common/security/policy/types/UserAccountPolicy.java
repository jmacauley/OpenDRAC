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

package com.nortel.appcore.app.drac.common.security.policy.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.utility.XmlUtility;

/*
 * <!-- ===== accountPolicy_T ===== --> <xsd:complexType name="accountPolicy_T"> <xsd:annotation>
 * <xsd:documentation> This structure is used to model both global and user specific account policies. All
 * elements should be initialized to defaults when used for global policies. Only those elements overriding
 * the global polices should be set for user specific settings. localPasswordPolicy - this element holds local
 * password policy rules for use with internal authentication accounts. dormantPeriod - automatically disable
 * a user's account if there has been no activity for this many days. A value of "0" indicates this check is
 * disabled. inactivityPeriod - the time in minutes a user accessing the system through a GUI can be inactive
 * before their seesion will be automatically terminated. A value of "0" indicates this check is disabled.
 * maxInvalidLoginAttempts - the maximum number of invalid login attempts permitted per user before the
 * account lockoutPeriod is invoked. A value of "0" indicates this check is disabled. lockoutPeriod - the
 * duration in seconds a user account will be disabled after multiple login failures determined by
 * maxInvalidLoginAttempts. lockedClientIPs - A set of IP addresses that will be blocked from accessing the
 * system. The ips are represented by a string seperated by a comma. </xsd:documentation> </xsd:annotation>
 * <xsd:sequence> <xsd:element name="localPasswordPolicy" type="passwordPolicy_T" minOccurs="0"/> <xsd:element
 * name="dormantPeriod" type="xsd:unsignedInt" minOccurs="0"/> <xsd:element name="inactivityPeriod"
 * type="xsd:unsignedInt" minOccurs="0"/> <xsd:element name="maxInvalidLoginAttempts" type="xsd:unsignedInt"
 * minOccurs="0"/> <xsd:element name="lockoutPeriod" type="xsd:unsignedInt" minOccurs="0"/> <xsd:element
 * name="lockedClientIPs" type="xsd:string" minOccurs="0" maxOccurs="1"/> </xsd:sequence> </xsd:complexType>
 */

/*
 * Sample <userAccountPolicy> <localPasswordPolicy> <passwordAging>100</passwordAging>
 * <passwordExpirationNotification>10</passwordExpirationNotification>
 * <passwordHistorySize>2</passwordHistorySize> <invalidPasswords></invalidPasswords>
 * <passwordRules></passwordRules> </localPasswordPolicy> <dormantPeriod>0</dormantPeriod>
 * <inactivityPeriod>0</inactivityPeriod> <maxInvalidLoginAttempts>3</maxInvalidLoginAttempts>
 * <lockoutPeriod>0</lockoutPeriod> <lockedClientIPs>10.2.2.2,10.1.1.1</lockedClientIPs> </userAccountPolicy>
 */

public final class UserAccountPolicy implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String USER_ACCOUNT_POLICY_ELEMENT = "userAccountPolicy";
	public static final String DORMANT_PERIOD_ELEMENT = "dormantPeriod";
	public static final String INACTIVITY_PERIOD_ELEMENT = "inactivityPeriod";
	public static final String MAX_INVALID_LOGIN_ELEMENT = "maxInvalidLoginAttempts";
	public static final String LOCKOUT_PERIOD_ELEMENT = "lockoutPeriod";
	public static final String LOCKED_CLIENTIPS_ELEMENT = "lockedClientIPs";

	private PasswordPolicy localPasswordPolicy;
	private Integer dormantPeriod;
	private Integer inactivityPeriod;
	private Integer maxInvalidLoginAttempts;
	private Integer lockoutPeriod;
	private List<String> lockedClientIPs;

	public UserAccountPolicy() {
		this.lockedClientIPs = new ArrayList<String>();
		this.localPasswordPolicy = new PasswordPolicy();
	}

	public void fromXML(Element root) throws Exception {
		/*****************************************************************/
		/* Construct UserAccountPolicy */
		/*****************************************************************/
		Element policy = root
		    .getChild(UserAccountPolicy.USER_ACCOUNT_POLICY_ELEMENT);

		if (policy != null) {

			Element pwPolicyElem = policy
			    .getChild(PasswordPolicy.LOCAL_PWPOLICY_ELEMENT);
			if (pwPolicyElem != null) {
				PasswordPolicy pwPolicy = new PasswordPolicy();
				pwPolicy.fromXML(pwPolicyElem);
				this.setLocalPasswordPolicy(pwPolicy);
			}

			// dormantPeriod attribute
			String dormantPeriodStr = policy
			    .getChildText(UserAccountPolicy.DORMANT_PERIOD_ELEMENT);
			if (dormantPeriodStr != null && !dormantPeriodStr.equals("")) {
				this.setDormantPeriod(Integer.decode(dormantPeriodStr));
			}

			// inActivityPeriod attribute
			String inActivityPeriodStr = policy
			    .getChildText(UserAccountPolicy.INACTIVITY_PERIOD_ELEMENT);
			if (inActivityPeriodStr != null && !inActivityPeriodStr.equals("")) {
				this.setInactivityPeriod(Integer.decode(inActivityPeriodStr));
			}

			// maxInvalidLoginAttempts attribute
			String maxInvalidLoginAttemptStr = policy
			    .getChildText(UserAccountPolicy.MAX_INVALID_LOGIN_ELEMENT);
			if (maxInvalidLoginAttemptStr != null
			    && !maxInvalidLoginAttemptStr.equals("")) {
				this.setMaxInvalidLoginAttempts(Integer
				    .decode(maxInvalidLoginAttemptStr));
			}

			// inActivityPeriod attribute
			String lockoutPeriodStr = policy
			    .getChildText(UserAccountPolicy.LOCKOUT_PERIOD_ELEMENT);
			if (lockoutPeriodStr != null && !lockoutPeriodStr.equals("")) {
				this.setLockoutPeriod(Integer.decode(lockoutPeriodStr));
			}

			// lockedClientIPs
			String lockedClientIPStr = policy
			    .getChildText(UserAccountPolicy.LOCKED_CLIENTIPS_ELEMENT);
			if (lockedClientIPStr != null && !lockedClientIPStr.equals("")) {
				this.setLockedClientIPs(XmlUtility
				    .stringToArray(lockedClientIPStr, ","));
			}
		}
	}

	public Integer getDormantPeriod() {
		return dormantPeriod;
	}

	public Integer getInactivityPeriod() {
		return inactivityPeriod;
	}

	public PasswordPolicy getLocalPasswordPolicy() {
		return localPasswordPolicy;
	}

	public List<String> getLockedClientIPs() {
		return lockedClientIPs;
	}

	public Integer getLockoutPeriod() {
		return lockoutPeriod;
	}

	public Integer getMaxInvalidLoginAttempts() {
		return maxInvalidLoginAttempts;
	}

	public void setDormantPeriod(Integer dormantPeriod) {
		this.dormantPeriod = dormantPeriod;
	}

	public void setInactivityPeriod(Integer inactivityPeriod) {
		this.inactivityPeriod = inactivityPeriod;
	}

	public void setLocalPasswordPolicy(PasswordPolicy localPasswordPolicy) {
		this.localPasswordPolicy = localPasswordPolicy;
	}

	public void setLockedClientIPs(List<String> lockedClientIPs) {
		this.lockedClientIPs = lockedClientIPs;
	}

	public void setLockoutPeriod(Integer lockoutPeriod) {
		this.lockoutPeriod = lockoutPeriod;
	}

	public void setMaxInvalidLoginAttempts(Integer maxInvalidLoginAttempts) {
		this.maxInvalidLoginAttempts = maxInvalidLoginAttempts;
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder(40);

		buf.append("<");
		buf.append(UserAccountPolicy.USER_ACCOUNT_POLICY_ELEMENT);
		buf.append(">");
		buf.append("\n");

		if (this.localPasswordPolicy != null) {
			buf.append(this.localPasswordPolicy.toXMLString());
		}

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(UserAccountPolicy.DORMANT_PERIOD_ELEMENT,
		    this.dormantPeriod));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    UserAccountPolicy.INACTIVITY_PERIOD_ELEMENT, this.inactivityPeriod));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    UserAccountPolicy.MAX_INVALID_LOGIN_ELEMENT,
		    this.maxInvalidLoginAttempts));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(UserAccountPolicy.LOCKOUT_PERIOD_ELEMENT,
		    this.lockoutPeriod));

		buf.append("\n");
		buf.append(XmlUtility.toXMLString(
		    UserAccountPolicy.LOCKED_CLIENTIPS_ELEMENT,
		    XmlUtility.arrayToString(this.lockedClientIPs, ",")));

		buf.append("\n");
		buf.append("</");
		buf.append(UserAccountPolicy.USER_ACCOUNT_POLICY_ELEMENT);
		buf.append(">");

		return buf.toString();
	}
}
