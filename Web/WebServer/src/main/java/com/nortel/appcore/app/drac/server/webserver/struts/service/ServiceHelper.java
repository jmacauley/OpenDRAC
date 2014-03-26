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

package com.nortel.appcore.app.drac.server.webserver.struts.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.common.types.CallIdType;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.service.form.CallForm;
import com.nortel.appcore.app.drac.server.webserver.struts.service.form.ServiceAlarmForm;
import com.nortel.appcore.app.drac.server.webserver.struts.service.form.ServiceForm;

/**
 * Created on 31-Aug-06
 */
public final class ServiceHelper {

	private ServiceHelper() {
	}

	public static List<ServiceAlarmForm> convertListToForm(
	    List<AlarmType> alarms, Locale locale, TimeZone tz) throws Exception {
		List<ServiceAlarmForm> result = new ArrayList<ServiceAlarmForm>();
		ServiceAlarmForm form = null;
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
		    DracConstants.WEB_GUI_AUDITTIME, locale);
		dateFormatter.setTimeZone(tz);
		int cnt = 0;
		if (alarms != null) {
			for (AlarmType alarm : alarms) {
				form = new ServiceAlarmForm();
				form.setResult(++cnt);
				form.setId(alarm.getId());
				form.setServiceId(alarm.getServiceId());
				form.setScheduleName(alarm.getScheduleName());
				form.setScheduleId(alarm.getScheduleId());
				form.setDescription(alarm.getDescription());
				form.setSeverity(alarm.getSeverity());
				form.setOccurredTime(dateFormatter.format(new Date(alarm.getOccurTime())));
				form.setOccurredTimeMillis(alarm.getOccurTime());
				form.setDuration((double) alarm.getDuration() / 1000);
				if (alarm.getDuration() > 0) {
					long clearTime = alarm.getOccurTime() + alarm.getDuration();
					form.setClearedTime(dateFormatter.format(new Date(clearTime)));
					form.setClearedTimeMillis(clearTime);
				}
				result.add(form);
			}
		}
		return result;
	}

	public static void copyProperties(CallIdType[] src, CallForm[] dest) {
		for (int i = 0; i < src.length; i++) {
			CallForm call = new CallForm();
			CallIdType callBean = src[i];
			if (callBean != null) {
				call.setCallID(callBean.getId());
				call.setCallStatus(callBean.getStatus().name());
				dest[i] = call;
			}
		}
	}

	public static void copyProperties(Locale locale, TimeZone tz,
	    DracService src, ServiceForm dest) {
		if (src != null && dest != null) {
			dest.setServiceID(src.getId());
			dest.setStatus(src.getStatus().name());

			dest.setStartTimeMillis(src.getStartTime());
			dest.setEndTimeMillis(src.getEndTime());
			Date startDate = new Date(src.getStartTime());
			Date endDate = new Date(src.getEndTime());

			SimpleDateFormat dateFormatter = new SimpleDateFormat(
			    DracConstants.WEB_GUI_TIME, locale);
			dateFormatter.setTimeZone(tz);
			dest.setStartdate(dateFormatter.format(startDate));
			dest.setEnddate(dateFormatter.format(endDate));

			dateFormatter = new SimpleDateFormat(DracConstants.WEB_GUI_TIME2, locale);
			dateFormatter.setTimeZone(tz);
			dest.setStartDateForList(dateFormatter.format(startDate));
			dest.setEndDateForList(dateFormatter.format(endDate));

			if (!"".equals(src.getScheduleName())) {
				dest.setScheduleName(src.getScheduleName());
			}
			if (!"".equals(src.getScheduleId())) {
				dest.setScheduleId(src.getScheduleId());
			}
			if (!"".equals(src.getScheduleStatus())) {
				dest.setScheduleStatus(src.getScheduleStatus().name());
			}

			dest.setRate(src.getRate());

			if (src.getUserInfo() != null) {
				dest.setUserName(src.getUserInfo().getUserId());
			}

			if (src.getCall() != null) {
				CallForm[] calls = new CallForm[src.getCall().length];
				ServiceHelper.copyProperties(src.getCall(), calls);
				dest.setCalls(calls);
			}

			if (src.getPath() != null) {
				String srsg = src.getPath().getSharedRiskServiceGroup();
				StringTokenizer st = new StringTokenizer(srsg, ",");
				while (st.hasMoreTokens()) {
					dest.getSrsg().add(st.nextToken().trim());
				}
				dest.setSrlg(src.getPath().getSrlg());
			}
			dest.setCancellable(State.isCancelable(src.getStatus()));

			// remove
			dest.setActivateable(State.isActivateable(src.getStatus()));

			dest.setResultNum(src.getResultNum());
		}
	}

	public static void copyProperties(Locale locale, TimeZone tz,
	    DracService[] src, ServiceForm[] dest) {
		for (int i = 0; i < src.length; i++) {
			ServiceForm service = new ServiceForm();
			ServiceHelper.copyProperties(locale, tz, src[i], service);
			dest[i] = service;
		}
	}

	public static void copyProperties(Locale locale, TimeZone tz,
	    String serviceID, Schedule src, ServiceForm dest) {
		if (src != null && dest != null) {
			DracService[] serviceIdList = src.getServiceIdList();
			int serviceIdTypeLength = serviceIdList.length;
			DracService service = null;
			for (int j = 0; j < serviceIdTypeLength; j++) {
				service = serviceIdList[j];
				if (serviceID.equals(service.getId())) {
					ServiceHelper.copyProperties(locale, tz, service, dest);
					break;
				}
			}
			dest.setScheduleName(src.getName());
			dest.setScheduleId(src.getId());
			dest.setScheduleSrcTNA(src.getSrcTNA());
			dest.setScheduleSrcFacLabel(src.getSrcFacLabel());
			dest.setScheduleDestTNA(src.getDestTNA());
			dest.setScheduleDestFacLabel(src.getDestFacLabel());

			Date startDate = new Date(src.getStartTime());
			Date endDate = new Date(src.getEndTime());
			SimpleDateFormat dateFormatter = new SimpleDateFormat(
			    DracConstants.WEB_GUI_TIME, locale);
			dateFormatter.setTimeZone(tz);
			dest.setScheduleStartdate(dateFormatter.format(startDate));
			dest.setScheduleEnddate(dateFormatter.format(endDate));

			dest.setScheduleRate(src.getRate());
			dest.setScheduleStatus(src.getStatus().name());
			dest.setScheduleActivationType(src.getActivationType().name());
		}
	}
}
