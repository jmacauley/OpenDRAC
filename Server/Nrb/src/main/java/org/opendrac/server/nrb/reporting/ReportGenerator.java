package org.opendrac.server.nrb.reporting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;

public class ReportGenerator {

	public Report generateScheduledServicesReport(List<UserGroupName> groups)
			throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.set(0, 0, 0, 0, 0, 0);
		return generateScheduledServicesReport(calendar.getTime(), new Date(),
				groups);
	}
	

	public Report generateScheduledServicesReport(List<UserGroupName> groups,
			Date startDate, Date endDate) throws Exception {
		return generateScheduledServicesReport(startDate, endDate, groups);
	}
	
	public Report generateScheduledServicesReport(Date startDate, Date endDate,
			List<UserGroupName> groups) throws Exception {
		String title = "Scheduled services";
		DbSchedule dbSchedule = DbSchedule.INSTANCE;
		List<Schedule> schedules = dbSchedule.querySchedules(
				startDate.getTime(), endDate.getTime(), groups);

		return new CharacterSeparatedReportSchedules(title, startDate, endDate,
				schedules);
	}

	public Report getServicesReport(Date startDate, Date endDate,
			final List<UserGroupName> groups) throws Exception {

		DbSchedule dbSchedule = DbSchedule.INSTANCE;
		List<DracService> serviceList = dbSchedule.queryServices(
				startDate.getTime(), endDate.getTime(), groups);
		try{
			List<String> scheduleIds = dbSchedule.getScheduleIdsForServices(serviceList);
			List<Schedule> schedules = new ArrayList<Schedule>();
			if(scheduleIds!=null && scheduleIds.size()>0){
				schedules = dbSchedule.querySchedules(scheduleIds);
			}
		return new CharacterSeparatedReportServices(null, startDate, endDate,
				serviceList, schedules);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public Report getQualitativeReport(Date startDate, Date endDate, final List<UserGroupName> groups) throws Exception {
		DbSchedule dbSchedule = DbSchedule.INSTANCE;
		List<DracService> serviceList = dbSchedule.queryServices(
				startDate.getTime(), endDate.getTime(), null);
		try{
			List<String> scheduleIds = dbSchedule.getScheduleIdsForServices(serviceList);
			List<Schedule> schedules = new ArrayList<Schedule>();
			if(scheduleIds!=null && scheduleIds.size()>0){
				schedules = dbSchedule.querySchedules(scheduleIds);
			}
			return new CharacterSeparatedReportQualitativeServicesData(null,
				startDate, endDate, serviceList, schedules);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public Report getQualitativeReportByPort(Date startDate, Date endDate, final List<UserGroupName> groups) throws Exception {
		DbSchedule dbSchedule = DbSchedule.INSTANCE;
		List<DracService> serviceList = dbSchedule.queryServices(
				startDate.getTime(), endDate.getTime(), null);
		try{
			List<String> scheduleIds = dbSchedule.getScheduleIdsForServices(serviceList);
			List<Schedule> schedules = new ArrayList<Schedule>();
			if(scheduleIds!=null && scheduleIds.size()>0){
				schedules = dbSchedule.querySchedules(scheduleIds);
			}
		return new CharacterSeparatedReportQualitativePortsData(null,
				startDate, endDate, serviceList, schedules);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public Report getLogsReport(Date startDate, Date endDate,
			Map<String, String> filter) throws Exception {
		DbLog dbLog = DbLog.INSTANCE;
		List<LogRecord> logsList = dbLog.getLogs(startDate.getTime(),
				endDate.getTime(), filter);
		return new CharacterSeparatedReportLogs(null, startDate, endDate,
				logsList);
	}
	
	public List<Report> getReportsForGui(String titleLeader, Date startDate,
			Date endDate, Map<String, String> filter, List<UserGroupName> groups)
			throws Exception {
		
		List<Report> reports = new ArrayList<Report>();		
		reports.add(getQualitativeReport(startDate, endDate, groups));		
		reports.add(getQualitativeReportByPort(startDate, endDate, groups)) ;

		reports.add(generateScheduledServicesReport(startDate, endDate, groups));
		reports.add(getServicesReport(startDate, endDate, groups));
		//reports.add(getLogsReport(startDate, endDate, filter));		
		return reports;
	}
	
	public Report getAggregatedReport(String titleLeader, Date startDate,
			Date endDate, Map<String, String> filter, List<UserGroupName> groups)
			throws Exception {
		
		List<Report> reports = new ArrayList<Report>();		
		reports.add(getQualitativeReport(startDate, endDate, groups));		
		reports.add(getQualitativeReportByPort(startDate, endDate, groups)) ;
		reports.add(generateScheduledServicesReport(startDate, endDate, groups));
		reports.add(getServicesReport(startDate, endDate, groups));
		reports.add(getLogsReport(startDate, endDate, filter));		
		return new AggregatedReport(titleLeader, startDate, endDate, reports);
	}
	
	public Report getAggregatedReportFull(String titleLeader, Date startDate,
			Date endDate, Map<String, String> filter, List<UserGroupName> groups)
			throws Exception {
		
		List<Report> reports = new ArrayList<Report>();		
		reports.add(getQualitativeReport(startDate, endDate, groups));		
		reports.add(getQualitativeReportByPort(startDate, endDate, groups)) ;
		reports.add(generateScheduledServicesReport(startDate, endDate, groups));
		reports.add(getServicesReport(startDate, endDate, groups));
		reports.add(getLogsReport(startDate, endDate, filter));		
		return new AggregatedReport(titleLeader, startDate, endDate, reports);
	}
}
