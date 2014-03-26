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

package com.nortel.appcore.app.drac.common.security.policy;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.authentication.types.PasswordErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;

public final class PasswordEvaluator {
  
  private static final Logger log = LoggerFactory.getLogger(PasswordEvaluator.class);
  
	public static final String PASSWD_SPECIALCHARS = "Password special chars";

	// Password rules has the following format:
	// !'#$%()*+-./=<>@[]^{|}~_Password min length:6;Min Alpha:1;Min Digit:1;Min
	// Special:0;Min
	// Different:1;Mixed Alpha:no;
	// special chars !'#$%()*+-./=<>@[]^{|}~_

	public static final String PASSWD_MINLENGTH = "Password min length";
	public static final String PASSWD_MINALPHAVALUE = "Min Alpha";
	public static final String PASSWD_MINDIGITVALUE = "Min Digit";
	public static final String PASSWD_MINSPECIALVALUE = "Min Special";
	public static final String PASSWD_MINDIFFERENT = "Min Different";
	public static final String PASSWD_MIXEDALPHA = "Mixed Alpha";
	public static final String PASSWD_PASSWORDRULES = "Password Rule String";
	public static final int MAX_PASSWORD_LENGTH = 20;

	// private static String NULL_PARAMETER = "null";

	private PasswordEvaluator() {
		super();
	}

	/**
	 * @param minimum
	 * @param maximum
	 * @param value
	 * @return true or false
	 */
	// public static boolean checkRange(int minimum, int maximum, int value)
	// {
	// if (value >= minimum && value <= maximum)
	// {
	// return true;
	// }
	// return false;
	// }
	// public static boolean checkValue(Object item, String minString, String
	// maxString)
	// throws NumberFormatException
	// {
	//
	// int intValue;
	// int min = 0;
	// int max = 0;
	// min = Integer.parseInt(minString);
	// max = Integer.parseInt(maxString);
	// intValue = Integer.parseInt((String) item);
	//
	// return checkRange(min, max, intValue);
	// }
	/**
	 * Returns password rules.
	 * 
	 * @param encodedPasswordRules
	 *          is the encoded password rule. Essentially is a map with one
	 *          encoded rules in String format.
	 */

	public static void decodePasswordRules(
	    Map<String, String> encodedPasswordRules, Map<String, String> result) {
		try {

			if (result == null) {
				log.error("Map result should be initialized");
				return;

			}

			// Password rules has the following format:
			// !'#$%()*+-./=<>@[]^{|}~_Password min length:6;Min Alpha:1;Min
			// Digit:1;Min Special:0;Min
			// Different:1;Mixed Alpha:no;
			String rules = encodedPasswordRules.get(PASSWD_PASSWORDRULES);
			rules = new String(URLDecoder.decode(rules, "UTF-8"));

			// first extract the special characters
			int i = 0;
			while (!Character.isLetter(rules.charAt(i))) {
				i++;

				if (i > rules.length()) {
					// should we do anything, at least return here
					return;
				}
			}

			String encodedChars = new String(rules.substring(0, i));
			encodedChars = new String(URLEncoder.encode(encodedChars, "UTF-8"));

			result.put(PASSWD_SPECIALCHARS, encodedChars);

			// extract the rest of the rules
			StringTokenizer tokenizer = new StringTokenizer(rules.substring(i), ":;");

			try {
				tokenizer.nextToken(); // min len
				result.put(PASSWD_MINLENGTH, tokenizer.nextToken());

				tokenizer.nextToken(); // min alpha
				result.put(PASSWD_MINALPHAVALUE, tokenizer.nextToken());

				tokenizer.nextToken(); // min digits
				result.put(PASSWD_MINDIGITVALUE, tokenizer.nextToken());

				tokenizer.nextToken(); // min special chars
				result.put(PASSWD_MINSPECIALVALUE, tokenizer.nextToken());

				tokenizer.nextToken(); // min different chars
				result.put(PASSWD_MINDIFFERENT, tokenizer.nextToken());

				tokenizer.nextToken(); // mixed alpha chars
				result.put(PASSWD_MIXEDALPHA, tokenizer.nextToken());
			}
			catch (Exception e) {
				// e.printStackTrace();
				log.error("Unexpected error", e);
			}
		}
		catch (Exception e) {
			// e.printStackTrace();
			log.error("Unexpected error", e);
		}

	}

	/**
	 * Returns password rules.
	 * 
	 * @param securityProperties
	 *          Security properties.
	 * @return Password rules.
	 */
	public static String encodedPasswordRules(
	    Map<String, String> securityProperties) {
		StringBuilder buffer = new StringBuilder();
		String parameterName;
		String parameterValue;

		// Password rules has the following format:
		// !'#$%()*+-./=<>@[]^{|}~_Password min length:6;Min Alpha:1;Min Digit:1;Min
		// Special:0;Min
		// Different:1;Mixed Alpha:no;
		// special chars !'#$%()*+-./=<>@[]^{|}~_

		parameterValue = securityProperties.get(PASSWD_SPECIALCHARS);

		try {
			if (null != parameterValue) {
				parameterValue = new String(URLDecoder.decode(parameterValue, "UTF-8"));
				buffer.append(parameterValue);
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			log.error("encodedPasswordRules", e);
			return null;
		}

		// Password min length
		parameterName = PASSWD_MINLENGTH;
		parameterValue = securityProperties.get(PASSWD_MINLENGTH);

		if (parameterValue != null) {
			buffer.append(parameterName);
			buffer.append(":");
			buffer.append(parameterValue);
			buffer.append(";");
		}
		else {
			return null;
		}

		// Min Alpha
		parameterName = PASSWD_MINALPHAVALUE;
		parameterValue = securityProperties.get(PASSWD_MINALPHAVALUE);

		if (parameterValue != null) {
			buffer.append(parameterName);
			buffer.append(":");
			buffer.append(parameterValue);
			buffer.append(";");
		}
		else {
			return null;
		}

		// Min Digit
		parameterName = PASSWD_MINDIGITVALUE;
		parameterValue = securityProperties.get(PASSWD_MINDIGITVALUE);

		if (parameterValue != null) {
			buffer.append(parameterName);
			buffer.append(":");
			buffer.append(parameterValue);
			buffer.append(";");
		}
		else {
			return null;
		}

		// Min Special
		parameterName = PASSWD_MINSPECIALVALUE;
		parameterValue = securityProperties.get(PASSWD_MINSPECIALVALUE);

		if (parameterValue != null) {
			buffer.append(parameterName);
			buffer.append(":");
			buffer.append(parameterValue);
			buffer.append(";");
		}
		else {
			return null;
		}

		// Min Different
		parameterName = PASSWD_MINDIFFERENT;
		parameterValue = securityProperties.get(PASSWD_MINDIFFERENT);

		if (parameterValue != null) {
			buffer.append(parameterName);
			buffer.append(":");
			buffer.append(parameterValue);
			buffer.append(";");
		}
		else {
			return null;
		}

		// Mixed Alpha
		parameterName = PASSWD_MIXEDALPHA;
		parameterValue = securityProperties.get(PASSWD_MIXEDALPHA);

		if (parameterValue != null) {
			buffer.append(parameterName);
			buffer.append(":");
			buffer.append(parameterValue);
			buffer.append(";");
		}
		else {
			return null;
		}

		String result = null;
		try {
			result = new String(URLEncoder.encode(buffer.toString(), "UTF-8"));
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
		return result;
	}

	/**
	 * Validates a password against password complexity rules
	 */

	public static PasswordErrorCode validatePassword(UserDetails userDetails,
	    String password, Map<String, String> passwordRulesMap, String oldPassword) {
		int minLength = 0;
		int minAlpha = 0;
		int minDigit = 0;
		int minSpecial = 0;
		int minDifferent = 1;
		StringBuilder illegalChars = new StringBuilder();
		String allowedSpecialChars = null;
		boolean mixedAlpha = true;

		/*************************************************************/
		/* Return if password contained in the invalid password list */
		/*************************************************************/

		// if(userDetails != null){ // Just for testing, must be removed if delivery
		PasswordErrorCode invalidListError = validatePasswordAgainstInvalidList(
		    userDetails, password);
		if (!invalidListError.equals(PasswordErrorCode.NO_ERROR)) {
			return invalidListError;
		}
		// }
		/**************************************************************/
		/* Otherwise, password is validated against rules */
		/**************************************************************/
		try {
			minLength = Integer.parseInt(passwordRulesMap.get(PASSWD_MINLENGTH));

			minAlpha = Integer.parseInt(passwordRulesMap.get(PASSWD_MINALPHAVALUE));

			minDigit = Integer.parseInt(passwordRulesMap.get(PASSWD_MINDIGITVALUE));

			minSpecial = Integer.parseInt(passwordRulesMap
			    .get(PASSWD_MINSPECIALVALUE));

			allowedSpecialChars = passwordRulesMap.get(PASSWD_SPECIALCHARS);
			allowedSpecialChars = new String(URLDecoder.decode(allowedSpecialChars,
			    "UTF-8"));

			minDifferent = Integer
			    .parseInt(passwordRulesMap.get(PASSWD_MINDIFFERENT));

			String mix = passwordRulesMap.get(PASSWD_MIXEDALPHA);
			mixedAlpha = mix.equals("yes");

			if (password == null) {
				log.error("password is null");
				return PasswordErrorCode.ERROR_GENERAL;
			}

			char[] userPassword = new char[password.length()];
			userPassword = password.toCharArray();

			// String error = null;

			if (userPassword.length < minLength) {
				log.error("password length is too short");
				return PasswordErrorCode.ERROR_PASSWD_MINLENGTH;
			}

			if (userPassword.length > MAX_PASSWORD_LENGTH) {
				log.error("password length is too long");
				return PasswordErrorCode.ERROR_PASSWD_MAXLENGTH;
			}

			int letterCount = 0;
			int lowerCaseCount = 0;
			int upperCaseCount = 0;
			int digitCount = 0;
			int specialCount = 0;
			char c = ' ';

			for (int i = 0; i < userPassword.length; ++i) {
				c = userPassword[i];

				if (allowedSpecialChars.indexOf(c) != -1) {

					// this is is an allowed special char
					specialCount++;

					continue;
				}

				if (Character.isLetter(c)) // this is a letter
				{
					letterCount++;

					if (Character.isLowerCase(c)) {
						lowerCaseCount++;
					}
					else {
						upperCaseCount++;
					}

					continue;
				}

				if (Character.isDigit(c)) // this is a digit
				{
					digitCount++;

					continue;
				}

				if (c != ' ') {
					illegalChars.append(c);

				}
				else {
					// illegalChars.append(newResourceBundle.getString("SPACE"));
					illegalChars.append(" ");
				}
			}

			if (illegalChars.length() > 0) {
				return PasswordErrorCode.ERROR_PASSWD_ILLEGALCHARS;
			}

			if (letterCount < minAlpha) {
				return PasswordErrorCode.ERROR_PASSWD_MINALPHAVALUE;
			}

			if (mixedAlpha && (lowerCaseCount == 0 || upperCaseCount == 0)) {
				return PasswordErrorCode.ERROR_PASSWD_MIXEDALPHA;
			}

			if (digitCount < minDigit) {
				return PasswordErrorCode.ERROR_PASSWD_MINDIGITVALUE;
			}

			if (specialCount < minSpecial) {
				return PasswordErrorCode.ERROR_PASSWD_MINSPECIALVALUE;
			}

			if (oldPassword != null) {
				return PasswordEvaluator.checkDiffPassword(password, oldPassword,
				    minDifferent);
			}

		}
		catch (Exception e) {
			log.error("error generated in validatePassword", e);
			return PasswordErrorCode.ERROR_GENERAL;
		}

		return PasswordErrorCode.NO_ERROR;
	}

	/**
	 * Validates a user ID. Rules; -the length must be inside the limits of the
	 * global ranges -contain only alphabetic characters or number characters.
	 * 
	 * @param name
	 *          name
	 * @param min
	 *          min number of characters allowed
	 * @param max
	 *          max number of characters allowed
	 * @exception Exception
	 */
	// public static void validateUserID(String name, int min, int max)
	// throws Exception
	// {
	// validateName1(name, min, max, "USER_ID_COMPLEXITY_RULES");
	// }
	/**
	 * Checks if the new and old password differ by the minimum of characters as
	 * set in the global settings.
	 */
	private static PasswordErrorCode checkDiffPassword(String oldPassword,
	    String newPassword, int minDifferent) {
		/*
		 * Counts the number of different characters (added, removed, or replaced)
		 * between the old and new password The order of characters is not
		 * considered. The paswords are case sensitive. The algorithm is: -for each
		 * character in the old password, if this character exists also in the new
		 * password, then remove it from both passwords -after that, the rest of the
		 * passwords contain only non-common characters; -the old password contains
		 * n1 characters removed or replaced -the new password contains n2
		 * characters added or replaced -the difference is the greatest number
		 * between n1 and n2 (or the length of the longest string) Examples: the old
		 * password is Abcd12 1. new password=abcd , after removing the
		 * common(bcd):old=A12 new=a diff=3 2. new password=21bcdA , after removing
		 * the common(Abcd12):old=(null) new=(null) diff=0 3. new password=Abcd13 ,
		 * after removing the common(Abcd1):old=2 new=3 diff=1 4. new
		 * password=Abcd1234 , after removing the common(Abcd12):old=(null) new=34
		 * diff=2 5. new password=Abe , after removing the common(Ab):old=cd12 new=e
		 * diff=4
		 */

		int count = 0;
		// These two vectors store the characters in the two passwords
		Vector<Character> v1 = new Vector<Character>();
		Vector<Character> v2 = new Vector<Character>();
		int i = 0;

		try {
			for (i = 0; i < oldPassword.length(); i++) {
				v1.addElement(new Character(oldPassword.charAt(i)));
			}

			for (i = 0; i < newPassword.length(); i++) {
				v2.addElement(new Character(newPassword.charAt(i)));
			}

			Character a = null;

			while (v1.size() > 0) {
				a = v1.firstElement();
				v1.removeElementAt(0);
				if (v2.contains(a)) {
					v2.removeElement(a); // common character
				}
				else {
					count++; // different character; count it
				}
			}

			if (count < v2.size()) {
				count = v2.size();
			}

			if (count < minDifferent) {
				return PasswordErrorCode.ERROR_PASSWD_MINDIFFERENT;
			}

		}
		catch (Exception e) {
			log.error("error in checkDiffPassword", e);
			return PasswordErrorCode.ERROR_GENERAL;
		}

		return PasswordErrorCode.NO_ERROR;
	}

	/**
	 * Uses the specified key and arguments to build a localized, formatted
	 * message. If an error occurs, an exception is printed and the key itself is
	 * returned.
	 * 
	 * @param key
	 *          The key to a localized "message template" (java.text.MessageFormat
	 *          class).
	 * @param arguments
	 *          Objects that supply values for the variables in the template.
	 * @return A localized message formatted with the specified arguments.
	 * @see java.text.MessageFormat
	 */
	// private static String format(String key, Object[] arguments)
	// {
	// MessageFormat format;
	// try
	// {
	// Object object = "Error: ";// newResourceBundle.getObject(key);
	//
	// if (object instanceof MessageFormat)
	// {
	// format = (MessageFormat) object;
	// }
	// else if (object instanceof String)
	// {
	// format = new MessageFormat((String) object);
	// }
	// else
	// {
	// return key;
	// }
	//
	// return format.format(arguments);
	// }
	// catch (Exception e)
	// {
	// // handle(e);
	// }
	//
	// return key;
	// }
	/**
	 * Validates a password against password complexity rules
	 */

	// private static PasswordErrorCode validatePassword(UserDetails userDetails,
	// String password, Map
	// passwordRulesMap)
	// {
	// return validatePassword(userDetails, password, passwordRulesMap, null);
	// }
	private static PasswordErrorCode validatePasswordAgainstInvalidList(
	    UserDetails userDetails, String password) {

		try {
			AuthenticationData authenData = userDetails.getUserPolicyProfile()
			    .getUserProfile().getAuthenticationData();

			if (!authenData.getAuthenticationType().equals(
			    UserProfile.AuthenticationType.INTERNAL)) {
				log.error("Only support internal account");
				return PasswordErrorCode.ERROR_GENERAL;
			}

			GlobalPolicy policy = userDetails.getUserPolicyProfile()
			    .getGlobalPolicy();

			if (policy == null) {
				return PasswordErrorCode.NO_ERROR;
			}

			List<String> invalidList = policy.getLocalAccountPolicy()
			    .getLocalPasswordPolicy().getPwInvalids();

			if (invalidList == null) {
				return PasswordErrorCode.NO_ERROR;
			}

			if (invalidList.contains(password)) {
				return PasswordErrorCode.ERROR_PASSWD_INVALIDLIST;
			}

			return PasswordErrorCode.NO_ERROR;

		}
		catch (Exception e) {
			log.error("Failed to validate password against invalid list", e);
			// e.printStackTrace();
			return PasswordErrorCode.ERROR_GENERAL;
		}
	}

}
