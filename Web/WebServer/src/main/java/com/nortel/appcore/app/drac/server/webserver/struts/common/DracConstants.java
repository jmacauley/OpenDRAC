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

package com.nortel.appcore.app.drac.server.webserver.struts.common;


public final class DracConstants {

	public static enum TimeMetric {
		SEC
	}

	public static final String PAGE_REF = "PageRef";
	public static final String PAGE_REF_LANG = "drac.language";
	public static final String PAGE_REF_LOGIN = "drac.general.login";
	public static final String PAGE_REF_LOGOUT = "drac.general.logout";
	public static final String PAGE_REF_WELCOME = "/welcome.do";
	public static final String WELCOME_JSP_PAGE = "welcome";
	public static final String LOGIN_PATH = "/login.do";
	public static final String PERFORM_LOGIN_PATH = "/performLogin.do";
	public static final String PASSWORD_PATH = "/password.do";
	public static final String LOGIN_JSP_PAGE = "LoginPage";
	public static final String PASSWORD_JSP_PAGE = "PasswordPage";
	public static final String LOGOUT_JSP_PAGE = "LogoutPage";
	public static final String LOGOUT_PATH = "/logout.do";
	public static final String PERFORM_LOGOUT_PATH = "/performLogout.do";
	public static final String SERVER_STATUS_JSP_PAGE = "ServerStatusPage";
	public static final String CREATE_SCHEDULE_JSP_PAGE = "CreateSchedulePage";
	public static final String CREATE_SCHEDULE_SIMPLE_JSP_PAGE = "CreateScheduleSimplePage";
	public static final String CREATE_SCHEDULE_RESULT_PAGE = "CreateScheduleResult";
	public static final String CREATE_SCHEDULE_PROGRESS_PAGE = "CreateScheduleProgress";
	public static final String LIST_SCHEDULES_PAGE = "listSchedules";
	public static final String LIST_SCHEDULES_RESULT_PAGE = "listSchedulesResultPage";
	public static final String SCHEDULE_DETAILS_PAGE = "scheduleDetails";
	public static final String QUERY_SCHEDULE_ACTION = "querySchedule";
	public static final String DELETE_SCHEDULE_RESULT_PAGE = "deleteScheduleResult";
	public static final String CONFIRM_SCHEDULE_RESULT_PAGE = "confirmScheduleResult";
	public static final String CANCEL_CREATE_SCHEDULE_PAGE = "cancelCreateResult";
	public static final String LIST_CREATE_PROGRESS_PAGE = "listProgressResult";
	public static final String SCH_ADVANCED_SEARCH = "schAdvancedSearch";
	public static final String SCH_ADVANCED_SEARCH_RESULTS = "srcAdvancedSearchResults";
	public static final String RH_ERROR_JSP_PAGE = "RHErrorPage";
	public static final String NO_PERMISSION_JSP_PAGE = "NoPermissionPage";
	public static final String REDUNDANCY_SWITCH_JSP_PAGE = "RedundancySwitchPage";
	public static final String CREATE_SCHEDULE_ACTION = "CreateScheduleAction";
	public static final String MYLOCALE = "myLocale";
	public static final String LANGUAGE = "language";
	public static final String CHECK_BROWSER = "checkBrowser";
	public static final String BROWSER_CONFIGURATION = "browserConfiguration";
	public static final String LANGUAGE_EN = "en";
	public static final String USER_ID = "userID";

	/**
	 * A AUTH_OBJ is a UserDetails object stored against a https session, we'd
	 * like to remove this in favor of LoginToken.
	 * 
	 * @deprecated
	 */
	@Deprecated
	public static final String AUTH_OBJ = "authObj";

	/**
	 * A TOKEN object is a LoginToken object stored against a HTTPS session.
	 */
	public static final String TOKEN_OBJ = "tokenObj";
	public static final String CALLING_URL = "callingURL";
	public static final String LOGIN = "login";
	public static final String PASSWORD = "password";
	public static final String MYURL = "myURL";
	public static final String SESSION_ERROR_CODE = "sessionErrorCode";
	public static final String EMPTY_STRING = "";
	public static final String LAYER2_STRING = "layer2";
	public static final String DASHES = "--";
	public static final String QUERY_SERVICE_JSP_PAGE = "QueryServicePage";
	public static final String LIST_SERVICE_JSP_PAGE = "ListOfServicesPageRef";
	public static final String DELETE_SERVICE_JSP_PAGE = "DeleteServicePageRef";
	public static final String ACTIVATE_SERVICE_JSP_PAGE = "ActivateServicePageRef";
	public static final String SERVICE_DETAILS_JSP_PAGE = "ServiceDetailsPageRef";
	public static final String LIST_SERVICE_ALARMS_JSP_PAGE = "listServiceAlarmsPage";
	public static final String LIST_SERVICE_ALARMS_RESULT = "listServiceAlarmsResult";
	public static final String LIST_ENDPOINTS_PAGE = "listEndpointsPage";
	public static final String LIST_ENDPOINTS_RESULT_PAGE = "listEndpointsResult";
	public static final String EDIT_ENDPOINT_PAGE = "editEndpointPage";
	public static final String EDIT_ENDPOINT_RESULT_PAGE = "editEndpointResult";
	public static final String LIST_USERS_PAGE = "listUsers";
	public static final String LIST_USERS_RESULT_PAGE = "listUsersResultPage";
	public static final String USER_DETAILS_PAGE = "userDetails";
	public static final String SUCCESS_PATH = "SuccessPath";
	public static final String CREATE_USER_JSP_PAGE = "CreateUserPage";
	public static final String EDIT_USER_JSP_PAGE = "EditUserPage";
	public static final String CREATE_USER_RESULT_PAGE = "CreateUserResult";
	public static final String CREATE_USER_ACTION = "CreateUserAction";
	public static final String HANDLE_CREATE_USER_SUCCESS_PAGE = "HandleCreateUserSuccess";
	public static final String HANDLE_CREATE_USER_FAILED_PAGE = "HandleCreateUserFailed";
	public static final String DELETE_USER_RESULT_PAGE = "deleteResult";
	public static final String CREATE_USER_GROUP_JSP_PAGE = "CreateUserGroupPage";
	public static final String CREATE_USER_GROUP_ACTION = "CreateUserGroupAction";
	public static final String HANDLE_CREATE_USER_GROUP_SUCCESS_PAGE = "HandleCreateUserGroupSuccess";
	public static final String HANDLE_CREATE_USER_GROUP_FAILED_PAGE = "HandleCreateUserGroupFailed";
	public static final String CREATE_USER_GROUP_RESULT_PAGE = "CreateUserGroupResult";
	public static final String LIST_USER_GROUPS_PAGE = "listUserGroups";
	public static final String LIST_USER_GROUPS_RESULT_PAGE = "listUserGroupsResultPage";
	public static final String USER_GROUP_DETAILS_PAGE = "userGroupDetails";
	public static final String DELETE_USERGROUP_RESULT_PAGE = "deleteResult";
	public static final String EDIT_USER_GROUP_RESULT_PAGE = "editUserGroupResultPage";
	public static final String CREATE_RESOURCE_GROUP_JSP_PAGE = "CreateResourceGroupPage";
	public static final String CREATE_RESOURCE_GROUP_ACTION = "CreateResourceGroupAction";
	public static final String HANDLE_CREATE_RESOURCE_GROUP_SUCCESS_PAGE = "HandleCreateResourceGroupSuccess";
	public static final String HANDLE_CREATE_RESOURCE_GROUP_FAILED_PAGE = "HandleCreateResourceGroupFailed";
	public static final String CREATE_RESOURCE_GROUP_RESULT_PAGE = "CreateResourceGroupResult";
	public static final String LIST_RESOURCE_GROUPS_PAGE = "listResourceGroups";
	public static final String LIST_RESOURCE_GROUPS_RESULT_PAGE = "listResourceGroupsResultPage";
	public static final String RESOURCE_GROUP_DETAILS_PAGE = "resourceGroupDetails";
	public static final String DELETE_RESGROUP_RESULT_PAGE = "deleteResult";
	public static final String FORWARD_CHANGE_PASSWORD = "passwordExpired";
	public static final String RESOURCE_DETAILS_PAGE = "resourceDetails";
	public static final String EDIT_RESOURCE_GROUP_RESULT_PAGE = "editResourceGroupResultPage";
	public static final String QUERY_UTIL_JSP_PAGE = "QueryUtilPage";
	public static final String QUERY_UTIL_LIST_JSP_PAGE = "QueryUtilListPage";
	public static final String NOT_IMPLEMENTED_ACTION = "NotImplementedPage";
	public static final String QUERY_UTIL_PAGE = "ListUtilPage";
	public static final String GRAPHICAL_VIEW_ACTION = "GraphicalViewPage";
	public static final String REPORTING_OVERVIEW_PAGE = "ReportingOverviewPage";
	public static final String REPORTS_OVERVIEW_PAGE = "ReportsOverviewPage";
	public static final String QUERY_LOG_PAGE = "QueryAuditLogPage";
	public static final String HELP_JSP_PAGE = "helpPage";
	public static final String BROWSER_JSP_PAGE = "browserPage";
	public static final String ABOUT_JSP_PAGE = "aboutPage";
	public static final String SETTINGS_JSP_PAGE = "SettingsPage";
	public static final String RELEASE_JSP_PAGE = "releasePage";
	public static final String DOWNLOAD_JSP_PAGE = "downloadPage";
	public static final String ADD_SERVICE_LIST_JSP_PAGE = "AddServiceList";
	public static final String ADD_SERVICE_JSP_PAGE = "AddService";
	public static final String ADD_SERVICE_RESULT_JSP_PAGE = "AddServiceResult";
	public static final String ADD_SERVICE_RESULT_ACTION = "AddServiceResultAction";
	public static final String GLOBAL_POLICY_JSP_PAGE = "GlobalPolicyPage";
	public static final String CHANGE_PASSWORD_JSP_PAGE = "ChangePasswordPage";
	public static final String CHANGE_PASSWORD_PATH = "/management/changePassword.do";
	public static final String CHANGE_PASSWORD_ACTION = "/management/changePasswordAction.do";
	public static final String INBOX_JSP_PAGE = "InboxPage";
	public static final String INBOX_ACTION = "doInbox";
	public static final String OUTBOX_ACTION = "doOutbox";
	public static final String OUTBOX_JSP_PAGE = "OutboxPage";
	public static final String COMPOSE_JSP_PAGE = "ComposePage";
	public static final String SHOW_MESSAGE_JSP_PAGE = "ShowMessagePage";
	public static final String SHOW_SENT_MESSAGE_JSP_PAGE = "ShowSentMessagePage";
	public static final String ALL_GROUPS = "all";
	public static final String CSRFToken = "CSRFToken";
	public static final String SessionIpAddress = "SessionIpAddress";
	// Constants for EndPointType values
	public static final String PHYS_ADDR_UNKWN = "UNKNOWN";

	public static final String AUTO_CH = "drac.schedule.detail.channel.auto";
	// Constants for routing metrics

	// Constants for days of week; Internal use only, must be internationalized in
	// JSP
	public static final String WEB_GUI_TIME = "EEE, MMM dd, yyyy H:mm 'GMT'Z";
	public static final String WEB_GUI_TIME2 = "EEE, MMM dd, yyyy H:mm";
	public static final String WEB_GUI_AUDITTIME = "yyyy-MM-dd HH:mm:ss";
	public static final String WEB_GUI_DATE = "EEE, MMM dd, yyyy";
	public static final String WEB_GUI_TIMEZONE = "' (GMT'Z')'";

	public static final String TZSTRING = "TZString";
	// Mapping of error messages to drac.properties file
	public static final String DRAC_ERROR = "dracError";
	public static final String CAUSE_EXCEPTION = "causeException";
	public static final String ERROR_CODE = "errorCode";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String SCHED_NOT_FOUND = "drac.schedule.detail.notfound";
	public static final String ADD_SERVICE_SCHED_BAD = "drac.service.add.badSchedule";
	public static final String SERVICE_NOT_FOUND = "drac.service.details.notfound";
	public static final String SCHED_CREATE_REMOVED = "drac.schedule.create.removed";
	public static final String USER_NOT_FOUND = "drac.security.userManagement.detail.notfound";
	public static final String USER_GROUP_NOT_FOUND = "drac.security.userGroupManagement.detail.notfound";
	public static final String GLOBAL_POLICY_NOT_FOUND = "drac.security.globalPolicy.detail.notfound";
	public static final String RESOURCE_GROUP_NOT_FOUND = "drac.security.resourceGroupManagement.detail.notfound";
	public static final String RESOURCE_NOT_FOUND = "drac.security.resourceGroupManagement.detail.resource.notfound";
	public static final String RESOURCE_GROUP_ACCESS_TO_ALL = "drac.security.resourceGroup.acces.to.all.message";
	public static final String DELETE_USER_PASS = "drac.security.userManagement.delete.result.pass";
	public static final String DELETE_USER_FAIL = "drac.security.userManagement.delete.result.fail";
	public static final String DELETE_USERGROUP_PASS = "drac.security.userGroupManagement.delete.result.pass";
	public static final String DELETE_USERGROUP_FAIL = "drac.security.userGroupManagement.delete.result.fail";
	public static final String DELETE_RESGROUP_PASS = "drac.security.resourceGroupManagement.delete.result.pass";
	public static final String DELETE_RESGROUP_FAIL = "drac.security.resourceGroupManagement.delete.result.fail";
	public static final String ERROR_MSG_LOGIN_SESSION_EXPIRED = "drac.general.login.error.sessionExpired";
	public static final String ERROR_MSG_LOGIN_INVALID_PASSWORD = "drac.general.login.error.invalidPassword";
	public static final String ERROR_MSG_LOGIN_LOCKED_OUT_SEC = "drac.general.login.error.lockedOut.sec";
	public static final String ERROR_MSG_LOGIN_DORMANT = "drac.general.login.error.dormant";
	public static final String ERROR_MSG_LOGIN_DISABLED = "drac.general.login.error.disabled";
	public static final String ERROR_MSG_LOGIN_LOCKED_CLIENT_IPADDRESS = "drac.general.login.error.lockedIPAddress";
	public static final String ERROR_MSG_LOGIN_LOCKED_CLIENT_SESSION = "drac.general.login.error.lockedSession";
	public static final String ERROR_MSG_LOGIN_NO_POLICY = "drac.general.login.error.noPolicy";
	public static final String ERROR_MSG_LOGIN_GENERAL_MAINTENANCE = "drac.general.login.generalMaintenance";
	public static final String ERROR_MSG_LOGIN_PROCESSING = "drac.general.login.error.processingError";
	public static final String PW_ERROR_GENERAL = "drac.security.changePassword.general";
	public static final String PW_ERROR_ILLEGALCHARS = "drac.security.changePassword.illegalChars";
	public static final String PW_ERROR_MINLENGTH = "drac.security.changePassword.minLength";
	public static final String PW_ERROR_MAXLENGTH = "drac.security.changePassword.maxLength";
	public static final String PW_ERROR_MINALPHAVALUE = "drac.security.changePassword.minAlphaValue";
	public static final String PW_ERROR_MINDIGITVALUE = "drac.security.changePassword.minDigitValue";
	public static final String PW_ERROR_MINSPECIALVALUE = "drac.security.changePassword.minSpecialValue";
	public static final String PW_ERROR_MINDIFFERENT = "drac.security.changePassword.minDifferent";
	public static final String PW_ERROR_MIXEDALPHA = "drac.security.changePassword.mixedAlpha";
	public static final String PW_ERROR_INVALIDLIST = "drac.security.changePassword.invalidList";
	public static final String ERROR_NO_ENCODED_PW_RULES = "drac.security.changePassword.noEncodedRules";
	public static final String ERROR_PW_NOT_MATCH = "drac.security.changePassword.notSame";
	public static final String ERROR_PW_EXPIRED = "drac.general.login.error.passwordExpired";
	public static final String ERROR_ASELECT_COMMS_LOGIN = "drac.general.login.error.aselect";
	public static final String ERROR_MSG_LOGIN_BROWSER_EXPIRED = "drac.general.login.error.browserTimeout";
	public static final String ERROR_REDUNDANCY_BOTHPRIMARY = "drac.general.serverStatus.error.redundancy.bothPrimary";
	public static final String ERROR_REDUNDANCY_SERVER1 = "drac.general.serverStatus.error.redundancy.server1";
	public static final String ERROR_REDUNDANCY_SERVER2 = "drac.general.serverStatus.error.redundancy.server2";
	public static final String ERROR_REDUNDANCY_SERVER2_NOT_REDUNDANT = "drac.general.serverStatus.error.redundancy.server2.notRedundant";
	public static final String ERROR_REDUNDANCY_SERVER_VERSIONS_MISMATCH = "drac.general.serverStatus.error.redundancy.version";
	public static final String ERROR_REUDNDANCY_SWITCH_VERSIONS_MISMATCH = "drac.general.serverStatus.error.redundancy.switch.version";
	public static final String ERROR_QUERYPATH_INTERNAL = "drac.error.queryPath.internal";
	public static final String ERROR_QUERYPATH_NOPARMS = "drac.error.queryPath.missingParms";
	public static final String ERROR_QUERYPATH_SAMETNA = "drac.error.queryPath.sameTna";
	public static final String ERROR_QUERYPATH_BADMETRIC = "drac.error.queryPath.badMetric";
	public static final String ERROR_SESSION_NOT_VALID = "drac.error.sessionInvalid";
	public static final String ERROR_QUERYTIME_NOPARMS = "drac.error.queryTime.missingParms";
	public static final String ERROR_QUERYTIME_BADRATE = "drac.error.queryTime.rateNAN";

	public static final String ERROR_QUERYTIME_BADDUR = "drac.error.queryTime.durationNAN";

	public static final String QUERYPATH_MSG_RECUR = "drac.schedule.queryPath.pathFound.recur";
	public static final String CANCEL_SCHED_SUCCESS_MSG = "drac.schedule.delete.success";
	public static final String CONFIRM_SCHED_SUCCESS_MSG = "drac.schedule.confirm.success";
	public static final String CANCEL_SERVICE_SUCCESS_MSG = "drac.service.delete.success";
	public static final String ACTIVATE_SERVICE_SUCCESS_MSG = "drac.service.activate.success";

	// TaskInfo state mapping to drac.properties
	public static final String[] TaskStates = { "drac.task.SUBMITTED",
	    "drac.task.IN_PROGRESS", "drac.task.ABORTED", "drac.task.DONE" };

	// Global Policy constants
	public static final String PERMISSION_GRANT_STATE = "grant";

	public static final String PERMISSION_DENY_STATE = "deny";

	public static final int SECS_PER_SEC = 1;
	public static final int SECS_PER_MINUTE = 60;
	public static final int SECS_PER_HOUR = 3600;
	public static final int SECS_PER_DAY = 86400;
	public static final int[] SECS_MULTIPLIER = { SECS_PER_SEC, SECS_PER_MINUTE,
	    SECS_PER_HOUR, SECS_PER_DAY };

	public static final String MINIMUM_PASSWORD_LENGTH = "6";

	// Channel ports

}
