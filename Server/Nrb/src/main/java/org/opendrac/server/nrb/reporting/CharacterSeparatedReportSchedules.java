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
import com.nortel.appcore.app.drac.common.types.Schedule;

public class CharacterSeparatedReportSchedules extends CharacterSeparatedReport {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	
	private static final long serialVersionUID = 7951731149621009548L;
	public static final String DEFAULT_TITLE_LEADER = "Report scheduled services";
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
	private static final String KEY_DURATION = "duration";
	private static final String KEY_RECURRENCE = "recurrence";
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
			KEY_ENDTIME, KEY_DURATION, KEY_RECURRENCE, KEY_SRC_END_POINT,
			KEY_TRGT_END_POINT, KEY_RATE, KEY_PATHPROTECTION, KEY_STATUS,
			KEY_USERID, KEY_BILLINGGROUPE, KEY_ACTIVATION_TYPE, KEY_SRCVLAN,
			KEY_DSTVLAN};
	
	private ReportData reportData = new ReportData(tableKeys);
	
	@Override
	public String[] getTableKeys() {
		return tableKeys;
	}
	protected CharacterSeparatedReportSchedules(String titleLeader,
			Date startDate, Date endDate, List<Schedule> schedules)
			throws IOException {
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

		fillReport(schedules);
	}

	@Override
	public File getAsFile() {
		return report;
	}

	@Override
	public String getReportContents() {
		return reportString;
	}

	private void intitTableHeaders() {
		reportData.setMetadata(reportTitle);
		reportData.setCollumnName(KEY_SERVICENAME, "Service name");
		reportData.setCollumnName(KEY_STARTTIME, "Start time");
		reportData.setCollumnName(KEY_ENDTIME, "End time");
		reportData.setCollumnName(KEY_DURATION, "Duration (min.)");
		reportData.setCollumnName(KEY_RECURRENCE, "Recurrence");
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
	
	private static String initHeader() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Service name");
		buffer.append(SEPARATOR);
		buffer.append("Start time");
		buffer.append(SEPARATOR);
		buffer.append("End time");
		buffer.append(SEPARATOR);
		buffer.append("Duration (min.)");
		buffer.append(SEPARATOR);
		buffer.append("Recurrence");
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

	private void fillReport(List<Schedule> schedules) throws IOException {
		intitTableHeaders();
		StringBuilder buffer = new StringBuilder();
		Date now = new Date();
		report = new File(System.getProperty("java.io.tmpdir", "/tmp")+"/ScheduledServices_" + now.getTime() + ".csv".replace("/", File.separator));
		report.deleteOnExit();
		log.debug("Generate report: " + report.getAbsolutePath() + " for "
				+ this.getClass().getName());
		BufferedWriter writer = null;
		writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(report)));
		try {
			writer.write(this.reportTitle);
			buffer.append(this.reportTitle);
			writer.write(HEADER);
			buffer.append(HEADER);
			for (Schedule schedule : schedules) {
				writer.write("\n");
				buffer.append("\n");
				String line = getLine(schedule);
				writer.write(line);
				buffer.append(line);
				reportData.addLine(getReportItem(schedule));
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		reportString = buffer.toString();
	}

	private ReportItem getReportItem(Schedule schedule){
		Map <String, Object> dataForItem = new HashMap<String, Object>();
		
		dataForItem.put(KEY_SERVICENAME, schedule.getName());
		dataForItem.put(KEY_STARTTIME, ""+convertDate(schedule.getStartTime(), CharacterSeparatedReport.DateFormat.DATE_TIME_NRS));
		dataForItem.put(KEY_ENDTIME, ""+convertDate(schedule.getEndTime(), CharacterSeparatedReport.DateFormat.DATE_TIME_NRS));
		dataForItem.put(KEY_DURATION, "" + (schedule.getDurationLong() / (1000 * 60)));
		dataForItem.put(KEY_RECURRENCE, schedule.getRecurrence().toString());
		dataForItem.put(KEY_SRC_END_POINT, schedule.getSrcTNA());
		dataForItem.put(KEY_TRGT_END_POINT, schedule.getDestTNA());
		dataForItem.put(KEY_RATE, schedule.getRate());
		dataForItem.put(KEY_PATHPROTECTION, schedule.getPath().getProtectionType().toString());
		dataForItem.put(KEY_STATUS, schedule.getStatus().name());
		dataForItem.put(KEY_USERID, schedule.getUserInfo().getUserId());
		dataForItem.put(KEY_BILLINGGROUPE, schedule.getUserInfo().getBillingGroup().toString());
		dataForItem.put(KEY_ACTIVATION_TYPE, schedule.getActivationType().toString());
		
		String srcVlan = schedule.getPath().getSrcVlanId();
		String dstVlanTableValue = srcVlan;
		String srcVlanTableValue="";
		
		if (srcVlan.trim().equals(FacilityConstants.UNTAGGED_LOCLBL_VALUE)) {
			srcVlanTableValue = FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		} else if (srcVlan.trim().equals(
				FacilityConstants.ALLTAGGED_LOCLBL_VALUE)) {
			srcVlanTableValue = FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}
		String dstVlan = schedule.getPath().getDstVlanId();		
		if (dstVlan.trim().equals(FacilityConstants.UNTAGGED_LOCLBL_VALUE)) {
			dstVlanTableValue = FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		} else if (dstVlan.trim().equals(
				FacilityConstants.ALLTAGGED_LOCLBL_VALUE)) {
			dstVlanTableValue = FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}		
		dataForItem.put(KEY_SRCVLAN, srcVlanTableValue);
		dataForItem.put(KEY_DSTVLAN, dstVlanTableValue);	
		return new ReportItem(dataForItem, false);
	}
	
	private String getLine(Schedule schedule) {
		StringBuilder csLline = new StringBuilder();
		csLline.append(encloseInQuotes(schedule.getName()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(convertDate(schedule.getStartTime(),
				CharacterSeparatedReport.DateFormat.DATE_TIME_NRS)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(convertDate(schedule.getEndTime(),
				CharacterSeparatedReport.DateFormat.DATE_TIME_NRS)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""
				+ (schedule.getDurationLong() / (1000 * 60))));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getRecurrence().toString()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getSrcTNA()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getDestTNA()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + schedule.getRate()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getPath().getProtectionType()
				.toString()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getStatus().name()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getUserInfo().getUserId()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getUserInfo().getBillingGroup()
				.toString()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getActivationType().toString()));
		csLline.append(SEPARATOR);

		String srcVlan = schedule.getPath().getSrcVlanId();
		String srcVlanTableValue = srcVlan;
		if (srcVlan.trim().equals(FacilityConstants.UNTAGGED_LOCLBL_VALUE)) {
			srcVlanTableValue = FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		} else if (srcVlan.trim().equals(
				FacilityConstants.ALLTAGGED_LOCLBL_VALUE)) {
			srcVlanTableValue = FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}
		String dstVlan = schedule.getPath().getDstVlanId();
		String dstVlanTableValue = dstVlan;
		if (dstVlan.trim().equals(FacilityConstants.UNTAGGED_LOCLBL_VALUE)) {
			dstVlanTableValue = FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		} else if (dstVlan.trim().equals(
				FacilityConstants.ALLTAGGED_LOCLBL_VALUE)) {
			dstVlanTableValue = FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}
		csLline.append(encloseInQuotes(srcVlanTableValue));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(dstVlanTableValue));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + schedule.getPath().getCost()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + schedule.getPath().getHop()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + schedule.getPath().getMetric()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""
				+ schedule.getPath().getRoutingMetric()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getPath().getSrlg()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(schedule.getPath()
				.getSharedRiskServiceGroup()));
		return csLline.toString();
	}


	@Override
	public ReportData getReportData() {
		return reportData;
	}

}
