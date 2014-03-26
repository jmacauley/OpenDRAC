package org.opendrac.server.nrb.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.Schedule;

public class CharacterSeparatedReportServices extends CharacterSeparatedReport {
  private final Logger log = LoggerFactory.getLogger(getClass());

	private static final long serialVersionUID = -3598988763591827020L;
	private static final String DEFAULT_TITLE_LEADER = "Report services";
	
	protected CharacterSeparatedReportServices (String titleLeader, Date startDate, Date endDate, List<DracService> serviceList, List<Schedule> schedules) throws IOException{
		if (titleLeader == null || titleLeader.equals("")) {
			titleLeader = DEFAULT_TITLE_LEADER;
		}

		this.reportTitle += DEFAULT_TITLE_LEADER
				+ ": "
				+ convertDate(startDate,
						CharacterSeparatedReport.DateFormat.DATE_TIME_NRS)
				+ " - "
				+ convertDate(endDate,
						CharacterSeparatedReport.DateFormat.DATE_TIME_NRS)
				+ "\n";
		fillReport(serviceList, schedules);
	}

	private static final String HEADER;
	private File report;
	private String reportString = "";
	private String reportTitle = "";
	static {
		HEADER = initHeader();
	}

	private static final String KEY_SERVICENAME = "servicename";
	private static final String KEY_STARTTIME = "starttime";
	private static final String KEY_ENDTIME = "endtime";	
	private static final String KEY_BANDWIDTH = "bandwidth";
	private static final String KEY_SRC_END_POINT = "srcendpoint";
	private static final String KEY_TRGT_END_POINT = "trgtendpoint";	
	private static final String KEY_RATE = "rate";
	private static final String KEY_PATHPROTECTION = "pathprotection";
	private static final String KEY_STATUS = "status";	
	private static final String KEY_USERID = "userid";	
	private static final String KEY_BILLINGGROUPE = "billinggroup";	
	private static final String KEY_ACTIVATION_TYPE = "activationtype";	
	private static final String KEY_SRCVLAN = "srcvlanid";	
	private static final String KEY_DSTVLAN = "dstvlanid";	
	private static final String KEY_HOP = "hop";
	private static final String KEY_METRIC = "metric";	
	private static final String KEY_ROUTING = "routing";
	private static final String KEY_RISKGROUP = "riskgroup";	
	private static final String KEY_ROUTE_BY_RISK = "routebyrisk";	
	private static final String KEY_ROUTE_BY_SERVICE = "routebyservice";

	private static final String[] tableKeys = { KEY_SERVICENAME, KEY_STARTTIME,
			KEY_ENDTIME, KEY_BANDWIDTH, KEY_SRC_END_POINT,
			KEY_TRGT_END_POINT, KEY_RATE, KEY_PATHPROTECTION, KEY_STATUS,
			KEY_USERID, KEY_BILLINGGROUPE, KEY_ACTIVATION_TYPE, KEY_SRCVLAN,
			KEY_DSTVLAN};
	
	private ReportData reportData = new ReportData(tableKeys);

	@Override
	public File getAsFile() {
		return report;
	}
	@Override
	public String getReportContents() {
	    return reportString;	    
	}
	
	private static String initHeader() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Scheduled name");
		buffer.append(SEPARATOR);
		buffer.append("Start time");
		buffer.append(SEPARATOR);
		buffer.append("End time");
		buffer.append(SEPARATOR);
		buffer.append("Mbit ps");
		buffer.append(SEPARATOR);
		buffer.append("Status");
		buffer.append(SEPARATOR);
		buffer.append("Source end point");
		buffer.append(SEPARATOR);
		buffer.append("Target end point");
		buffer.append(SEPARATOR);
		buffer.append("Rate (Mbit)");		
		buffer.append(SEPARATOR);
		buffer.append("Path protection");
		buffer.append(SEPARATOR);
		buffer.append("Status)");
		buffer.append(SEPARATOR);
		buffer.append("UserId");
		buffer.append(SEPARATOR);
		buffer.append("Billing group");
		buffer.append(SEPARATOR);		
		buffer.append("ActivationType");
		buffer.append(SEPARATOR);
		
		buffer.append("Src. VlanId");
		buffer.append(SEPARATOR);
		buffer.append("Dst. VlanId");
		
		buffer.append(SEPARATOR);
		buffer.append("Hop");
		buffer.append(SEPARATOR);
		buffer.append("Metric");
		buffer.append(SEPARATOR);
		buffer.append("Routing metric");
		buffer.append(SEPARATOR);
		buffer.append("Risk group");
		buffer.append(SEPARATOR);
		
		buffer.append("Diversely Route By Shared Risk Link Groups");
		buffer.append(SEPARATOR);
		buffer.append("Diversely Route By Existing Services (ID)");
		
		
		return buffer.toString();
	}

	private void intitTableHeaders() {
		reportData.setMetadata(reportTitle);
		reportData.setCollumnName(KEY_SERVICENAME, "Scheduled name");
		reportData.setCollumnName(KEY_STARTTIME, "Start time");
		reportData.setCollumnName(KEY_ENDTIME, "End time");
		reportData.setCollumnName(KEY_BANDWIDTH, "Bandwidth");
		reportData.setCollumnName(KEY_SRC_END_POINT, "Source end point");
		reportData.setCollumnName(KEY_TRGT_END_POINT, "Target end point");
		reportData.setCollumnName(KEY_RATE, "Rate (Mbit)");
		reportData.setCollumnName(KEY_PATHPROTECTION, "Path protection");
		reportData.setCollumnName(KEY_STATUS, "Status)");
		reportData.setCollumnName(KEY_USERID, "UserId");
		reportData.setCollumnName(KEY_BILLINGGROUPE, "Billing group");
		reportData.setCollumnName(KEY_ACTIVATION_TYPE, "ActivationType");
		reportData.setCollumnName(KEY_SRCVLAN, "Src. VlanId");
		reportData.setCollumnName(KEY_DSTVLAN, "Dst. VlanId");	
		reportData.setCollumnName(KEY_HOP, "Hop");
		reportData.setCollumnName(KEY_METRIC, "Metric");
		reportData.setCollumnName(KEY_ROUTING, "Routing metric");
		reportData.setCollumnName(KEY_RISKGROUP, "Risk group");	
		reportData.setCollumnName(KEY_ROUTE_BY_RISK, "Diversely Route By Shared Risk Link Groups");
		reportData.setCollumnName(KEY_ROUTE_BY_SERVICE, "Diversely Route By Existing Services (ID)");
	}
	private Map<String, Schedule> getSchedulesByServiceId(List<Schedule> schedules, List<DracService> services){
		Map<String, Schedule> schedulesByServiceId = new HashMap<String, Schedule>();
		for(DracService service: services){
			String scheduleIdFromService = service.getScheduleId();
			for(Schedule schedule: schedules){
				if(schedule.getId().equals(scheduleIdFromService)){
					schedulesByServiceId.put(service.getId(), schedule);
					break;
				}
			}
		}
		return schedulesByServiceId;
	}
	
	private void fillReport(List<DracService> serviceList, List<Schedule> schedules) throws IOException {
		
		intitTableHeaders();
		Map<String, Schedule> schedulesByServiceId = getSchedulesByServiceId(schedules, serviceList);
	    StringBuilder buffer = new StringBuilder();
		Date now = new Date();
		report = new File(System.getProperty("java.io.tmpdir", "/tmp")+"/ScheduledServices_" + now.getTime() + ".csv".replace("/", File.separator));
		report.deleteOnExit();
		log.debug("Generate report: " + report.getAbsolutePath()+" for "+this.getClass().getName());
		BufferedWriter writer = null;
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				report)));
		try {
			writer.write(this.reportTitle);
			buffer.append(this.reportTitle);
			writer.write(HEADER);
			buffer.append(HEADER);
			for (DracService service : serviceList) {
				writer.write("\n");
				buffer.append("\n");
				String line = getLine(service, schedulesByServiceId.get(service.getId())); 
				writer.write(line);
				buffer.append(line);
				reportData.addLine(getReportItem(service, schedulesByServiceId.get(service.getId())));
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		reportString=buffer.toString();
	}

	private String getLine(DracService service, Schedule schedule) {
		
		PathType path = schedule.getPath();
		StringBuilder csLline = new StringBuilder();
		csLline.append(encloseInQuotes(service.getScheduleName()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+convertDate(service.getStartTime(), CharacterSeparatedReport.DateFormat.DATE_TIME_NRS)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+convertDate(service.getEndTime(), CharacterSeparatedReport.DateFormat.DATE_TIME_NRS)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+service.getRate()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+service.getStatus()));
		csLline.append(SEPARATOR);
		
		
		csLline.append(encloseInQuotes(schedule.getSrcTNA()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getDestTNA()));
		csLline.append(SEPARATOR);		
		csLline.append(encloseInQuotes(""+service.getRate()));
		csLline.append(SEPARATOR);		
		csLline.append(encloseInQuotes(service.getPath().getProtectionType().toString()));
		csLline.append(SEPARATOR);		
		csLline.append(encloseInQuotes(service.getStatus().name()));
		csLline.append(SEPARATOR);		
		csLline.append(encloseInQuotes(service.getUserInfo().getUserId()));
		csLline.append(SEPARATOR);		
		csLline.append(encloseInQuotes(service.getUserInfo().getBillingGroup().toString()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(service.getActivationType().toString()));
		csLline.append(SEPARATOR);	
		
		String srcVlan = path.getSrcVlanId() ==null?"" : path.getSrcVlanId();
		String srcVlanTableValue = srcVlan;
		if(srcVlan.trim().equals(FacilityConstants.UNTAGGED_LOCLBL_VALUE)){
			srcVlanTableValue=FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		}else if(srcVlan.trim().equals(FacilityConstants.ALLTAGGED_LOCLBL_VALUE)){
			srcVlanTableValue=FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}
		String dstVlan = path.getDstVlanId() ==null ? "" : path.getDstVlanId();
		String dstVlanTableValue = dstVlan;
		if(dstVlan.trim().equals(FacilityConstants.UNTAGGED_LOCLBL_VALUE)){
			dstVlanTableValue=FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		}else if(dstVlan.trim().equals(FacilityConstants.ALLTAGGED_LOCLBL_VALUE)){
			dstVlanTableValue=FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}else{
			dstVlanTableValue = dstVlan;
		}
		csLline.append(encloseInQuotes(srcVlanTableValue));
		csLline.append(SEPARATOR);		
		csLline.append(encloseInQuotes(dstVlanTableValue));
		csLline.append(SEPARATOR);	
		csLline.append(encloseInQuotes(""+path.getCost()));
		csLline.append(SEPARATOR);	
		csLline.append(encloseInQuotes(""+path.getHop()));
		csLline.append(SEPARATOR);	
		csLline.append(encloseInQuotes(""+path.getMetric()));
		csLline.append(SEPARATOR);	
		csLline.append(encloseInQuotes(""+path.getRoutingMetric()));	
		csLline.append(SEPARATOR);
		//csLline.append(encloseInQuotes(schedule.getUserInfo().getEmailAddress()));
		csLline.append(encloseInQuotes(path.getSrlg()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(path.getSharedRiskServiceGroup()));		
		
		return csLline.toString();
	}
	
	private ReportItem getReportItem(DracService service, Schedule schedule){
		
		PathType path = schedule.getPath();
		Map <String, Object> dataForItem = new HashMap<String, Object>();
		
		dataForItem.put(KEY_SERVICENAME, service.getScheduleName());
		dataForItem.put(KEY_STARTTIME, ""+convertDate(service.getStartTime(), CharacterSeparatedReport.DateFormat.DATE_TIME_NRS));
		dataForItem.put(KEY_ENDTIME, ""+convertDate(service.getEndTime(), CharacterSeparatedReport.DateFormat.DATE_TIME_NRS));
		dataForItem.put(KEY_BANDWIDTH, "" + service.getRate());
		dataForItem.put(KEY_SRC_END_POINT, schedule.getSrcTNA());
		dataForItem.put(KEY_TRGT_END_POINT, schedule.getDestTNA());
		dataForItem.put(KEY_RATE, service.getRate());
		dataForItem.put(KEY_PATHPROTECTION, service.getPath().getProtectionType().toString());
		dataForItem.put(KEY_STATUS, service.getStatus().name());
		dataForItem.put(KEY_USERID, service.getUserInfo().getUserId());
		dataForItem.put(KEY_BILLINGGROUPE, service.getUserInfo().getBillingGroup().toString());
		dataForItem.put(KEY_ACTIVATION_TYPE, service.getActivationType().toString());
		
		String srcVlan = path.getSrcVlanId() ==null?"" : path.getSrcVlanId();
		String srcVlanTableValue = srcVlan;
		if(srcVlan.trim().equals(FacilityConstants.UNTAGGED_LOCLBL_VALUE)){
			srcVlanTableValue=FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		}else if(srcVlan.trim().equals(FacilityConstants.ALLTAGGED_LOCLBL_VALUE)){
			srcVlanTableValue=FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}
		String dstVlan = path.getDstVlanId() ==null ? "" : path.getDstVlanId();
		String dstVlanTableValue = dstVlan;
		if(dstVlan.trim().equals(FacilityConstants.UNTAGGED_LOCLBL_VALUE)){
			dstVlanTableValue=FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		}else if(dstVlan.trim().equals(FacilityConstants.ALLTAGGED_LOCLBL_VALUE)){
			dstVlanTableValue=FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}else{
			dstVlanTableValue = dstVlan;
		}
		
		dataForItem.put(KEY_SRCVLAN, srcVlanTableValue);
		dataForItem.put(KEY_DSTVLAN, dstVlanTableValue);	
		return new ReportItem(dataForItem, false);
	}
	
	@Override
	public String[] getTableKeys() {
		return tableKeys;
	}
	@Override
	public ReportData getReportData() {
		return reportData;
	}

}
