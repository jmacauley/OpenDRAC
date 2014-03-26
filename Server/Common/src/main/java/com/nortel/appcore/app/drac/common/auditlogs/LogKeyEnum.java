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

package com.nortel.appcore.app.drac.common.auditlogs;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_CATEGORY;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_RESULT;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_SEVERITY;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_TYPE;

/**
 * Logs have a unique combination of a description key, severity, category, log
 * type, result, resource type. This set of enums links them all together into
 * single structures as a convenience when creating an log.
 * 
 * @author pitman
 */

public enum LogKeyEnum {
	KEY_A_LOG_FOR_TESTING_ONLY("test.log", LOG_SEVERITY.CRITICAL,
	    LOG_CATEGORY.SYSTEM, LOG_TYPE.EXECUTED, LOG_RESULT.SUCCESS), //

	KEY_MANUAL_ADJACENCY_CREATED("manual.adjacency.created", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.NE, LOG_TYPE.CREATED, LOG_RESULT.SUCCESS), //
	KEY_MANUAL_ADJACENCY_DELETED("manual.adjacency.deleted", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.NE, LOG_TYPE.DELETED, LOG_RESULT.SUCCESS), //
	KEY_NE_FACILITY_UPDATED("ne.facility.updated", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.NE, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_NE_CONNECTION_MISMATCH("ne.connection.mismatch", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.NE, LOG_TYPE.DELETED, LOG_RESULT.SUCCESS), //
	KEY_CREATE_USER("policy.user.create", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.CREATED, LOG_RESULT.SUCCESS), //
	KEY_EDIT_USER("policy.user.edit", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_DELETE_USER("policy.user.delete", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.DELETED, LOG_RESULT.SUCCESS), //
	KEY_EDIT_USER_PW("policy.user.edit.password", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_EDIT_USER_PW_FAILED("policy.user.edit.password.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED,
	    LOG_RESULT.FAILED), //
	KEY_CREATE_USERGROUP("policy.usergroup.create", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.CREATED, LOG_RESULT.SUCCESS), //
	KEY_EDIT_USERGROUP("policy.usergroup.edit", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_DELETE_USERGROUP("policy.usergroup.delete", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.DELETED, LOG_RESULT.SUCCESS), //
	KEY_CREATE_RESGROUP("policy.resourcegroup.create", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.CREATED, LOG_RESULT.SUCCESS), //
	KEY_EDIT_RESGROUP("policy.resourcegroup.edit", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_DELETE_RESGROUP("policy.resourcegroup.delete", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.DELETED, LOG_RESULT.SUCCESS), //
	KEY_EDIT_GLOBAL("policy.globals.modified", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_CREATE_USER_FAILED("policy.user.create.failed", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.CREATED, LOG_RESULT.FAILED), //
	KEY_EDIT_USER_FAILED("policy.user.edit.failed", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED, LOG_RESULT.FAILED), //
	KEY_DELETE_USER_FAILED("policy.user.delete.failed", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.DELETED, LOG_RESULT.FAILED), //
	KEY_CREATE_USERGROUP_FAILED("policy.usergroup.create.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.CREATED,
	    LOG_RESULT.FAILED), //
	KEY_EDIT_USERGROUP_FAILED("policy.usergroup.edit.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED,
	    LOG_RESULT.FAILED), //
	KEY_DELETE_USERGROUP_FAILED("policy.usergroup.delete.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.DELETED,
	    LOG_RESULT.FAILED), //
	KEY_CREATE_RESGROUP_FAILED("policy.resourcegroup.create.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.CREATED,
	    LOG_RESULT.FAILED), //
	KEY_EDIT_RESGROUP_FAILED("policy.resourcegroup.edit.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED,
	    LOG_RESULT.FAILED), //
	KEY_DELETE_RESGROUP_FAILED("policy.resourcegroup.delete.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.DELETED,
	    LOG_RESULT.FAILED), //
	KEY_EDIT_GLOBAL_FAILED("policy.globals.modified.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.MODIFIED,
	    LOG_RESULT.FAILED), //

	KEY_AUTH_SUCCESS("security.auth.success", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHENTICATION, LOG_TYPE.VERIFIED, LOG_RESULT.SUCCESS), //
	KEY_AUTH_FAILED("security.auth.failed", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHENTICATION, LOG_TYPE.VERIFIED, LOG_RESULT.FAILED), //
	KEY_LOGIN_SUCCESS("security.login.success", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.LOGGED_IN, LOG_RESULT.SUCCESS), //
	KEY_LOGIN_FAILED("security.login.fail", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.LOGGED_IN, LOG_RESULT.FAILED), //
	KEY_LOGOUT_SUCCESS("security.logout.success", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHENTICATION, LOG_TYPE.LOGGED_OUT, LOG_RESULT.SUCCESS), //
	KEY_LOGOUT_TIMEOUT("security.logout.sessionExpired", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.AUTHENTICATION, LOG_TYPE.LOGGED_OUT, LOG_RESULT.SUCCESS), //
	KEY_LOGOUT_FAILED("security.logout.failed", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.AUTHENTICATION, LOG_TYPE.LOGGED_OUT, LOG_RESULT.FAILED), //

	KEY_POLICY_SCHEDULE_ACCESS_FAILED("policy.schedule.access.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.ACCESS_CHECK,
	    LOG_RESULT.FAILED), //
	KEY_POLICY_SERVICE_ACCESS_FAILED("policy.service.access.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.AUTHORIZATION, LOG_TYPE.ACCESS_CHECK,
	    LOG_RESULT.FAILED), //
	KEY_SERVICE_STATE_CHANGED("service.state.changed", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.MODIFIED, LOG_RESULT.NA), //
	KEY_SERVICE_STARTED("service.started", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.CREATED, LOG_RESULT.SUCCESS), //
	KEY_SERVICE_FINISHED("service.finished", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.EXECUTED, LOG_RESULT.SUCCESS), //
	KEY_SERVICE_START_FAILED("service.start.failed", LOG_SEVERITY.CRITICAL,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.CREATED, LOG_RESULT.FAILED), //
	KEY_SERVICE_FINISHED_FAILED("service.finish.failed", LOG_SEVERITY.CRITICAL,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.EXECUTED, LOG_RESULT.FAILED), //

	KEY_NE_STATUS_DELETED("ne.status.deleted", LOG_SEVERITY.CRITICAL,
	    LOG_CATEGORY.NE, LOG_TYPE.UNMANAGED, LOG_RESULT.SUCCESS), //
	KEY_NE_STATUS_UNKNOWN("ne.status.unknown", LOG_SEVERITY.CRITICAL,
	    LOG_CATEGORY.NE, LOG_TYPE.UNKNOWN, LOG_RESULT.UNKNOWN), //
	KEY_NE_STATUS_NOT_PROVISION("ne.status.not_provision", LOG_SEVERITY.CRITICAL,
	    LOG_CATEGORY.NE, LOG_TYPE.ALIGNED, LOG_RESULT.FAILED), //
	KEY_NE_STATUS_CREATED("ne.status.created", LOG_SEVERITY.MAJOR,
	    LOG_CATEGORY.NE, LOG_TYPE.CREATED, LOG_RESULT.SUCCESS), //
	KEY_NE_STATUS_NOT_CONNECT("ne.status.not_connect", LOG_SEVERITY.MAJOR,
	    LOG_CATEGORY.NE, LOG_TYPE.LOGGED_IN, LOG_RESULT.FAILED), //
	KEY_NE_STATUS_NOT_AUTHENTICATED("ne.status.not_authenticated",
	    LOG_SEVERITY.CRITICAL, LOG_CATEGORY.NE, LOG_TYPE.LOGGED_IN,
	    LOG_RESULT.FAILED), //
	KEY_NE_STATUS_ASSOCIATED("ne.status.associated", LOG_SEVERITY.MAJOR,
	    LOG_CATEGORY.NE, LOG_TYPE.LOGGED_IN, LOG_RESULT.SUCCESS), //
	KEY_NE_STATUS_INITIALIZING("ne.status.initializing", LOG_SEVERITY.MINOR,
	    LOG_CATEGORY.NE, LOG_TYPE.LOGGED_IN, LOG_RESULT.SUCCESS), //
	KEY_NE_STATUS_ALIGNED("ne.status.aligned", LOG_SEVERITY.MAJOR,
	    LOG_CATEGORY.NE, LOG_TYPE.ALIGNED, LOG_RESULT.SUCCESS), //

	KEY_NE_TOGGLED("system.ne.toggled", LOG_SEVERITY.INFO, LOG_CATEGORY.NE,
	    LOG_TYPE.EXECUTED, LOG_RESULT.SUCCESS), //
	KEY_SCHEDULE_CREATE("schedule.create", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.CREATED, LOG_RESULT.SUCCESS), //
	KEY_SCHEDULE_CREATE_PARTIAL("schedule.create.partial", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.CREATED, LOG_RESULT.FAILED), //
	KEY_SCHEDULE_CREATE_FAILED("schedule.create.failed", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.CREATED, LOG_RESULT.FAILED), //
	KEY_SCHEDULE_CANCELED("schedule.canceled", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.CANCELED, LOG_RESULT.SUCCESS), //
	KEY_SCHEDULE_CANCELED_FAILED("schedule.canceled.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.CANCELED,
	    LOG_RESULT.FAILED), //
	KEY_SCHEDULE_ADDSERVICE("schedule.addService", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_SCHEDULE_ADDSERVICE_FAILED("schedule.addService.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.MODIFIED,
	    LOG_RESULT.FAILED), //
	KEY_SERVICE_CANCELED("service.canceled", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.CANCELED, LOG_RESULT.SUCCESS), //
	KEY_SERVICE_CANCELED_FAILED("service.cancel.failed", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.CANCELED, LOG_RESULT.FAILED), //
	KEY_MAJOR_ALARM("service.alarm.raised.mj", LOG_SEVERITY.CRITICAL,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.ALARM_RAISED, LOG_RESULT.UNKNOWN), //
	KEY_MINOR_ALARM("service.alarm.raised.mn", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.ALARM_RAISED, LOG_RESULT.UNKNOWN), //
	KEY_CRITICAL_ALARM("service.alarm.raised.cr", LOG_SEVERITY.CRITICAL,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.ALARM_RAISED, LOG_RESULT.UNKNOWN), //
	KEY_ALARM_CLEARED("service.alarm.cleared", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.ALARM_CLEARED, LOG_RESULT.UNKNOWN), //
	KEY_REDUNDANCY_SWITCH_START("redundancy.switch.start", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.SYSTEM, LOG_TYPE.REDUNDANCY, LOG_RESULT.UNKNOWN), //
	KEY_REDUNDANCY_SWITCH_END("redundancy.switch.end", LOG_SEVERITY.WARNING,
	    LOG_CATEGORY.SYSTEM, LOG_TYPE.REDUNDANCY, LOG_RESULT.UNKNOWN), //
	KEY_SETTING_OVERHEAD("system.settings.scheduleOffset", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.SYSTEM, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_SETTING_OVERHEAD_FAILED("system.settings.scheduleOffset.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.SYSTEM, LOG_TYPE.MODIFIED,
	    LOG_RESULT.FAILED), //
	KEY_SETTING_CONFIRMATION("system.settings.confirmationTimeout",
	    LOG_SEVERITY.INFO, LOG_CATEGORY.SYSTEM, LOG_TYPE.MODIFIED,
	    LOG_RESULT.SUCCESS), //
	KEY_SETTING_CONFIRMATION_FAILED("system.settings.confirmationTimeout.failed",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.SYSTEM, LOG_TYPE.MODIFIED,
	    LOG_RESULT.FAILED), //
	KEY_SCHEDULE_CONFIRMED("schedule.confirmation", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_SCHEDULE_CONFIRMATION_FAILED("schedule.confirmation.failed",
	    LOG_SEVERITY.CRITICAL, LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.MODIFIED,
	    LOG_RESULT.FAILED), //
	KEY_SCHEDULE_CONFIRMATION_TIMEOUT("schedule.confirmation.timeout",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.RESERVATIONGROUP, LOG_TYPE.MODIFIED,
	    LOG_RESULT.SUCCESS), //
	KEY_SERVICE_ACTIVATED("service.activation", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.RESERVATION, LOG_TYPE.MODIFIED, LOG_RESULT.SUCCESS), //
	KEY_SERVICE_ACTIVATION_FAILED("service.activation.failed",
	    LOG_SEVERITY.CRITICAL, LOG_CATEGORY.RESERVATION, LOG_TYPE.MODIFIED,
	    LOG_RESULT.FAILED), //
	KEY_SERVICE_ACTIVATION_TIMEOUT("service.activation.timeout",
	    LOG_SEVERITY.WARNING, LOG_CATEGORY.RESERVATION, LOG_TYPE.MODIFIED,
	    LOG_RESULT.SUCCESS), //
	KEY_SYSTEM_DB_PURGED_FULL("system.db.purged.full", LOG_SEVERITY.INFO,
	    LOG_CATEGORY.SYSTEM, LOG_TYPE.EXECUTED, LOG_RESULT.SUCCESS);

	private final String key;
	private final LOG_SEVERITY severity;
	private final LOG_CATEGORY category;
	private final LOG_TYPE type;
	private final LOG_RESULT result;

	private static final Locale CURRENT_LOCALE = Locale.getDefault();

	private static ResourceBundle BUNDLE;
	
	private static final Logger log = LoggerFactory.getLogger(LogKeyEnum.class);

	static {
		try {
			String base = "com.nortel.appcore.app.drac.common.resources.AuditLogResource";
			try {
				BUNDLE = ResourceBundle.getBundle(base, CURRENT_LOCALE);
			}
			catch (Exception e) {
				log.error("Error loading resource bundle for " + CURRENT_LOCALE, e);
				BUNDLE = ResourceBundle.getBundle(base, Locale.ENGLISH);
			}
		}
		catch (Exception t) {
			log.error("Unable to load resource bundle for logs", t);
		}
	}

	private LogKeyEnum(String resourceKey, LOG_SEVERITY auditSeverity,
	    LOG_CATEGORY auditCategory, LOG_TYPE auditType, LOG_RESULT auditResult) {
		key = resourceKey;
		severity = auditSeverity;
		category = auditCategory;
		type = auditType;
		result = auditResult;
	}

	public LOG_CATEGORY getCategory() {
		return category;
	}

	public String getFormattedLogDescription(String[] args) {
		try {
			return getFormattedLogDescription(args, false);
		}
		catch (Exception e) {
			log.error("Should not happen", e);
			return "Should never see this, internal error ";
		}
	}

	/**
	 * Return the formatted log error using the key and arguments to format the
	 * message from the resource bundle. Exceptions are swallowed or thrown based
	 * on the flag throwErrors (used for junit testing and verifying the resource
	 * bundle is complete)
	 */
	public String getFormattedLogDescription(String[] args, boolean throwErrors)
	    throws Exception {
		String msg = null;

		try {
			msg = BUNDLE.getString(key);
			if (args != null) {
				msg = MessageFormat.format(msg, (Object[]) args);
			}
		}
		catch (Exception e) {
			log.error("Missing AuditLog description " + key, e);
			if (throwErrors) {
				throw e;
			}
			msg = key;
			if (args != null && args.length > 0) {
				StringBuilder temp = new StringBuilder(msg);
				for (int i = 0; i < args.length; i++) {
					temp.append("?");
					if (i > 0) {
						temp.append("&");
					}
					temp.append(args[i]);
				}
				msg += temp.toString();
			}
		}
		return msg;
	}

	public String getKey() {
		return key;
	}

	public LOG_RESULT getResult() {
		return result;
	}

	public LOG_SEVERITY getSeverity() {
		return severity;
	}

	public LOG_TYPE getType() {
		return type;
	}

	@Override
	public String toString() {
		return key;
	}

}
