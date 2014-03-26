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

package com.nortel.appcore.app.drac.server.webserver.struts.validator;

import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.Validator;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.util.ValidatorUtils;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.validator.FieldChecks;
import org.apache.struts.validator.Resources;

import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;

/**
 * 
 */
public final class DracFieldChecks extends FieldChecks {

	/**
	 * Referenced via validator-rules.xml for struts, these methods are called via
	 * reflection!!!
	 */

	private static final long serialVersionUID = 1L;

	private DracFieldChecks() {
	}

	/**
	 * Checks if the end time is after the start time
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if not equal, false otherwise.
	 */
	public static boolean validateAfterStart(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) throws Exception {
		String endTime = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("endTimeProperty"));
		String endDate = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("endDateProperty"));
		String startTime = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("startTimeProperty"));
		String startDate = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("startDateProperty"));
		String language = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("localeProperty"));

		HttpSession session = request.getSession();
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);

		if (GenericValidator.isBlankOrNull(startTime)
		    && GenericValidator.isBlankOrNull(startDate)
		    && GenericValidator.isBlankOrNull(endDate)
		    && GenericValidator.isBlankOrNull(endTime)) {
			// if start now and end never are both clicked, all the values are blank,
			// we can let this check go
			return true;
		}
		long startTimeInMillis = 0;
		long endTimeInMillis = 0;

		// startime is blank, set it to now
		if (GenericValidator.isBlankOrNull(startTime)
		    && GenericValidator.isBlankOrNull(startDate)) {
			startTimeInMillis = System.currentTimeMillis();
		}
		else {
			startTimeInMillis = DracHelper.parseWebDateToMillis(new Locale(language),
			    tz, startDate, startTime);
		}

		// endtime is blank, set it to Dec. 31, 2999 12:00AM
		if (GenericValidator.isBlankOrNull(endDate)
		    && GenericValidator.isBlankOrNull(endTime)) {
			endTimeInMillis = DracHelper.parseWebDateToMillis(new Locale(language),
			    tz, "Tuesday, December 31, 2999", "0:00");
		}
		else {
			endTimeInMillis = DracHelper.parseWebDateToMillis(new Locale(language),
			    tz, endDate, endTime);
		}

		if (startTimeInMillis > 0 && endTimeInMillis > 0) {
			try {
				if (endTimeInMillis <= startTimeInMillis) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}
			}
			catch (Exception e) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}
		return true;
	}

	public static boolean validateAtLeastOne(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {
		String internal = ValidatorUtils
		    .getValueAsString(bean, field.getProperty());
		String aselect = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("aselectBox"));
		String radius = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("radiusBox"));

		if (internal != null && internal.equals("false") && aselect != null
		    && aselect.equals("false") && radius != null && radius.equals("false")) {
			errors.add(field.getKey(),
			    Resources.getActionMessage(validator, request, va, field));
			return false;
		}

		return true;
	}

	/**
	 * Checks if two fields are not equal
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if not equal, false otherwise.
	 */
	public static boolean validateBillingGroupCheck(Object bean,
	    ValidatorAction va, Field field, ActionMessages errors,
	    Validator validator, HttpServletRequest request) {

		String billingGroup = null;
		if (isString(bean)) {
			billingGroup = (String) bean;
		}
		else {
			billingGroup = ValidatorUtils.getValueAsString(bean, field.getProperty());
		}
		String srcGroup = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("srcGroupProp"));
		String destGroup = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("destGroupProp"));

		if (!GenericValidator.isBlankOrNull(billingGroup)
		    && !GenericValidator.isBlankOrNull(srcGroup)
		    && !GenericValidator.isBlankOrNull(destGroup)) {
			try {
				if (!billingGroup.equals(srcGroup) && !billingGroup.equals(destGroup)) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}
			}
			catch (Exception e) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the day of month is valid
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if valid, false otherwise.
	 */
	public static boolean validateDayInMonth(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {

		String day = ValidatorUtils.getValueAsString(bean, field.getProperty());
		String recurrence = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("recurrenceProp"));
		String frequency = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("frequencyProp"));

		if (recurrence != null && recurrence.equals("true")) {
			if (frequency != null && frequency.equals("Monthly")) {
				try {
					int dayNum = Integer.parseInt(day);
					if (dayNum < 0 || dayNum > 31) {
						errors.add(field.getKey(),
						    Resources.getActionMessage(validator, request, va, field));
						return false;
					}
				}
				catch (NumberFormatException e) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if the day of month is valid given a month
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if valid, false otherwise.
	 */
	public static boolean validateDayInYear(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {

		String day = ValidatorUtils.getValueAsString(bean, field.getProperty());
		String recurrence = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("recurrenceProp"));
		String frequency = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("frequencyProp"));
		String month = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("monthProp"));

		if (recurrence != null && recurrence.equals("true")) {
			if (frequency != null && frequency.equals("Yearly")) {
				try {
					int dayNum = Integer.parseInt(day);
					if (dayNum < 0) {
						errors.add(field.getKey(),
						    Resources.getActionMessage(validator, request, va, field));
						return false;
					}
					int monthNum = Integer.parseInt(month);

					// January starts at 1
					if (monthNum == 1 || monthNum == 3 || monthNum == 5 || monthNum == 7
					    || monthNum == 8 || monthNum == 10 || monthNum == 12) {
						if (dayNum > 31) {
							errors.add(field.getKey(),
							    Resources.getActionMessage(validator, request, va, field));
							return false;
						}
					}
					else if (monthNum == 4 || monthNum == 6 || monthNum == 9
					    || monthNum == 11) {
						if (dayNum > 30) {
							errors.add(field.getKey(),
							    Resources.getActionMessage(validator, request, va, field));
							return false;
						}
					}
					else if (monthNum == 2) {
						if (dayNum > 28) {
							errors.add(field.getKey(),
							    Resources.getActionMessage(validator, request, va, field));
							return false;
						}
					}
				}
				catch (NumberFormatException e) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if two fields are equal
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if not equal, false otherwise.
	 */
	public static boolean validateEqual(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {

		String value = null;
		String value2 = null;
		if (isString(bean)) {
			value = (String) bean;
		}
		else {
			value = ValidatorUtils.getValueAsString(bean, field.getProperty());
			String sProperty2 = field.getVarValue("secondProperty");
			value2 = ValidatorUtils.getValueAsString(bean, sProperty2);
		}

		if (!GenericValidator.isBlankOrNull(value)
		    && !GenericValidator.isBlankOrNull(value2)) {
			try {
				if (!value.equals(value2)) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}
			}
			catch (Exception e) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a fields value is within a range (min &amp; max specified in the
	 * vars attribute).
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if greater than 0, false otherwise.
	 */
	public static boolean validateIntGreaterThanZero(Object bean,
	    ValidatorAction va, Field field, ActionMessages errors,
	    Validator validator, HttpServletRequest request) {

		String value = null;
		if (isString(bean)) {
			value = (String) bean;
		}
		else {
			value = ValidatorUtils.getValueAsString(bean, field.getProperty());
		}

		if (!GenericValidator.isBlankOrNull(value)) {
			try {
				int intValue = Integer.parseInt(value);

				if (intValue <= 0) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));

					return false;
				}
			}
			catch (Exception e) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if the end time is in the future (cannot create schedules where
	 * endtime is already past)
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if not equal, false otherwise.
	 */
	public static boolean validateIsFutureTime(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) throws Exception {

		String endTime = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("endTimeProperty"));
		String endDate = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("endDateProperty"));
		String language = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("localeProperty"));

		HttpSession session = request.getSession();
		LoginToken token = (LoginToken) session
		    .getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);

		if (!GenericValidator.isBlankOrNull(endDate)
		    && !GenericValidator.isBlankOrNull(endTime)) {
			try {
				long endTimeInMillis = DracHelper.parseWebDateToMillis(new Locale(
				    language), tz, endDate, endTime);
				if (endTimeInMillis <= System.currentTimeMillis()) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}
			}
			catch (Exception e) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}
		return true;
	}

	public static boolean validateNameNotAll(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {

		String name = ValidatorUtils.getValueAsString(bean, field.getProperty());

		if (name != null) {
			if (name.trim().equalsIgnoreCase(DracConstants.ALL_GROUPS)) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if two fields are not equal
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if not equal, false otherwise.
	 */
	public static boolean validateNotEqual(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {

		String value = null;
		String value2 = null;
		if (isString(bean)) {
			value = (String) bean;
		}
		else {
			value = ValidatorUtils.getValueAsString(bean, field.getProperty());
			String sProperty2 = field.getVarValue("secondProperty");
			value2 = ValidatorUtils.getValueAsString(bean, sProperty2);
		}

		if (!GenericValidator.isBlankOrNull(value)
		    && !GenericValidator.isBlankOrNull(value2)) {
			try {
				if (value.equals(value2)) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}
			}
			catch (Exception e) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a fields value is within a range (min &amp; max specified in the
	 * vars attribute).
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if greater than 0, false otherwise.
	 */
	public static boolean validatePositiveInt(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {

		String value = null;
		if (isString(bean)) {
			value = (String) bean;
		}
		else {
			value = ValidatorUtils.getValueAsString(bean, field.getProperty());
		}

		if (!GenericValidator.isBlankOrNull(value)) {
			try {
				int intValue = Integer.parseInt(value);

				if (intValue < 0) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));

					return false;
				}
			}
			catch (Exception e) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}

		return true;
	}

	public static boolean validateRequiredFor(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {
		String value = ValidatorUtils.getValueAsString(bean, field.getProperty());
		String sProperty2 = field.getVarValue("requiredProperty");
		String dependentProperty = ValidatorUtils
		    .getValueAsString(bean, sProperty2);
		String dependentValue = field.getVarValue("requiredValue");

		if (dependentProperty != null && dependentProperty.equals(dependentValue)) {
			if (value == null || "".equals(value)) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}
		return true;
	}

	public static boolean validateSrlg(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {

		String srlg = ValidatorUtils.getValueAsString(bean, field.getProperty());

		if (srlg != null) {
			StringTokenizer tokenizer = new StringTokenizer(srlg, ",");
			String token = null;
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				try {
					int value = Integer.parseInt(token.trim());
					if (value < 0) {
						errors.add(field.getKey(),
						    Resources.getActionMessage(validator, request, va, field));
						return false;
					}
				}
				catch (NumberFormatException nfe) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if a fields value is a valid time
	 * 
	 * @param bean
	 *          The bean validation is being performed on.
	 * @param va
	 *          The <code>ValidatorAction</code> that is currently being
	 *          performed.
	 * @param field
	 *          The <code>Field</code> object associated with the current field
	 *          being validated.
	 * @param errors
	 *          The <code>ActionMessages</code> object to add errors to if any
	 *          validation errors occur.
	 * @param validator
	 *          The <code>Validator</code> instance, used to access other field
	 *          values.
	 * @param request
	 *          Current request object.
	 * @return True if valid, false otherwise.
	 */
	public static boolean validateTime(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {

		String value = null;
		if (isString(bean)) {
			value = (String) bean;
		}
		else {
			value = ValidatorUtils.getValueAsString(bean, field.getProperty());
		}

		if (!GenericValidator.isBlankOrNull(value)) {
			try {
				int index = value.indexOf(":");
				if (index < 0) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}

				String hourStr = value.substring(0, index);
				try {
					int hour = Integer.parseInt(hourStr);
					if (hour < 0 || hour > 23) {
						errors.add(field.getKey(),
						    Resources.getActionMessage(validator, request, va, field));
						return false;
					}
				}
				catch (NumberFormatException nfe) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}

				String minStr = value.substring(index + 1, index + 3);
				try {
					int minutes = Integer.parseInt(minStr);
					if (minutes < 0 || minutes > 60) {
						errors.add(field.getKey(),
						    Resources.getActionMessage(validator, request, va, field));
						return false;
					}
				}
				catch (NumberFormatException nfe) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}

			}
			catch (Exception e) {
				errors.add(field.getKey(),
				    Resources.getActionMessage(validator, request, va, field));
				return false;
			}
		}

		return true;
	}

	public static boolean validateWeeklyPattern(Object bean, ValidatorAction va,
	    Field field, ActionMessages errors, Validator validator,
	    HttpServletRequest request) {
		String recurrence = ValidatorUtils.getValueAsString(bean,
		    field.getProperty());
		String frequency = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("frequencyProp"));
		String sun = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("sunProp"));
		String mon = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("monProp"));
		String tue = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("tueProp"));
		String wed = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("wedProp"));
		String thu = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("thuProp"));
		String fri = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("friProp"));
		String sat = ValidatorUtils.getValueAsString(bean,
		    field.getVarValue("satProp"));

		if (recurrence != null && recurrence.equals("true")) {
			if (frequency != null && frequency.equals("Weekly")) {
				if (sun != null && sun.equals("false") && mon != null
				    && mon.equals("false") && tue != null && tue.equals("false")
				    && wed != null && wed.equals("false") && thu != null
				    && thu.equals("false") && fri != null && fri.equals("false")
				    && sat != null && sat.equals("false")) {
					errors.add(field.getKey(),
					    Resources.getActionMessage(validator, request, va, field));
					return false;
				}
			}
		}
		return true;
	}

}
