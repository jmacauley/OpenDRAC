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

package com.nortel.appcore.app.drac.server.webserver.struts.auditlogging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.auditlogging.form.AuditLoggingForm;
import com.nortel.appcore.app.drac.server.webserver.struts.auditlogging.form.QueryAuditLogForm;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;

/**
 * Created on 16-Aug-06
 */
public final class AuditLogHelper {
	private AuditLogHelper() {
	}

	public static List<AuditLoggingForm> getLogs(LoginToken token, Locale locale,
	    QueryAuditLogForm srcForm, TimeZone tz) throws Exception {
		List<AuditLoggingForm> results = new ArrayList<AuditLoggingForm>();

		String startTime = srcForm.getStartTime();
		String startDate = srcForm.getStartdate();
		String endTime = srcForm.getEndTime();
		String endDate = srcForm.getEnddate();
		String category = srcForm.getCategory();
		String logType = srcForm.getLogType();
		String severity = srcForm.getSeverity();
		String result = srcForm.getResult();
		String originator = srcForm.getOriginator();
		String ipAddress = srcForm.getIpAddress();
		String billingGroup = srcForm.getBillingGroup();
		String resource = srcForm.getResource();

		long start = 0;
		long end = Long.MAX_VALUE;

		if (startTime != null && endTime != null
		    && !startTime.equals(DracConstants.EMPTY_STRING)
		    && !endTime.equals(DracConstants.EMPTY_STRING)) {
			start = DracHelper.parseWebDateToMillis(locale, tz, startDate, startTime);
			end = DracHelper.parseWebDateToMillis(locale, tz, endDate, endTime);
		}

		Map<String, String> params = new HashMap<String, String>();
		if (!"".equals(category)) {
			params.put(DbKeys.LogKeys.CATEGORY, category);
		}
		if (!"".equals(logType)) {
			params.put(DbKeys.LogKeys.LOG_TYPE, logType);
		}
		if (!"".equals(severity)) {
			params.put(DbKeys.LogKeys.SEVERITY, severity);
		}
		if (!"".equals(result)) {
			params.put(DbKeys.LogKeys.RESULT, result);
		}
		if (!"".equals(originator)) {
			params.put(DbKeys.LogKeys.ORIGINATOR, originator);
		}
		if (!"".equals(ipAddress)) {
			params.put(DbKeys.LogKeys.IP_ADDR, ipAddress);
		}
		if (!"".equals(billingGroup)) {
			params.put(DbKeys.LogKeys.BILLING_GROUP, billingGroup);
		}
		if (!"".equals(resource)) {
			params.put(DbKeys.LogKeys.RESOURCE, resource);
		}

		List<LogRecord> list = RequestHandler.INSTANCE.getLogs(token, start,
		    end, params);
		for (int i = 0; i < list.size(); i++) {
			AuditLoggingForm form = new AuditLoggingForm();
			copyProperties(locale, tz, list.get(i), form);
			form.setSerialNo(i + 1);
			results.add(form);
		}

		return results;
	}

	private static void copyProperties(Locale locale, TimeZone tz, LogRecord src,
	    AuditLoggingForm dest) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
		    DracConstants.WEB_GUI_AUDITTIME, locale);
		dateFormatter.setTimeZone(tz);
		Date d = new Date(src.getTime());
		dest.setTime(dateFormatter.format(d));
		dest.setOccurTime(src.getTime());
		dest.setUserid(src.getOriginator());
		dest.setAddress(src.getIp());
		dest.setBillingGroup(src.getBillingGroup().toString());
		dest.setCategory(src.getCategory().name());
		dest.setType(src.getType().name());
		dest.setResult(src.getResult().name());
		dest.setSeverity(src.getSeverity().name());
		dest.setDescription(src.getDescription());
		dest.setResource(src.getResource());
	}
}
