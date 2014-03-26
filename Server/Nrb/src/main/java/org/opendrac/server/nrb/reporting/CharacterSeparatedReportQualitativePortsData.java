package org.opendrac.server.nrb.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.State;

public class CharacterSeparatedReportQualitativePortsData extends
		CharacterSeparatedReport {
	
	private static final long serialVersionUID = 676370803607109291L;
	private File report;
	private String reportString = "";
	
	public static final String KEY_PORT_ID = "port";
	public static final String KEY_NR_ASSIGNEES = "users";
	public static final String KEY_NR_CREATED_SERVICES = "nrservices";
	public static final String KEY_NR_FAILED_SERVICES = "failedservice";
	public static final String KEY_TOTAL_DURATION = "totalduration";
	public static final String KEY_FAILED_DURATION = "failedduration";
	public static final String KEY_FAILED_DURATION_PERCENTAGE = "faileddurationpercentage";
	public static final String KEY_TOTAL_BWHR = "totalbwhr";
	public static final String KEY_FAILED_BWHR = "failedbwhr";
	public static final String KEY_FAILED_BWHR_PERCENTAGE = "failedbwhrpercentage";
	
	private static final String DEFAULT_TITLE_LEADER = "Report services qualitative data by port";
	
	private static final String[] tableKeys = { KEY_PORT_ID, KEY_NR_ASSIGNEES, KEY_NR_CREATED_SERVICES,
	        KEY_NR_FAILED_SERVICES, KEY_TOTAL_DURATION, KEY_FAILED_DURATION, KEY_FAILED_DURATION_PERCENTAGE,
	        KEY_TOTAL_BWHR, KEY_FAILED_BWHR, KEY_FAILED_BWHR_PERCENTAGE };
	
	private ReportData reportData = new ReportData(tableKeys);

	private static final String HEADER;
	private String reportTitle = "";
	static {
		HEADER = initHeader();
	}

	private void intitTableHeaders() {
		reportData.setMetadata(reportTitle);
		reportData.setCollumnName(KEY_PORT_ID, "Port");
		reportData.setCollumnName(KEY_NR_ASSIGNEES, "Nr Assignees (Users)");
		reportData.setCollumnName(KEY_NR_CREATED_SERVICES, "Services created");
		reportData.setCollumnName(KEY_NR_FAILED_SERVICES, "Services failed");
		reportData.setCollumnName(KEY_TOTAL_DURATION, "Total duration");
		reportData.setCollumnName(KEY_FAILED_DURATION, "Duration failed");
		reportData.setCollumnName(KEY_FAILED_DURATION_PERCENTAGE, "Failure duration percentage");
		reportData.setCollumnName(KEY_TOTAL_BWHR, "Total bandwidth-hour (Gbit.hr)");
		reportData.setCollumnName(KEY_FAILED_BWHR, "Failed bandwidth-hour (Gbit.hr)");
		reportData.setCollumnName(KEY_FAILED_BWHR_PERCENTAGE, "Failure bandwidth-hour percentage");
	}

	private ReportItem getReportItem(String port, Map<String, Object> data){
		Map <String, Object> dataForItem = new HashMap<String, Object>();
		dataForItem.put(KEY_PORT_ID, port);
		dataForItem.put(KEY_NR_ASSIGNEES, data.get(KEY_NR_ASSIGNEES));		
		dataForItem.put(KEY_NR_CREATED_SERVICES, data.get(KEY_NR_CREATED_SERVICES));
		dataForItem.put(KEY_NR_FAILED_SERVICES, data.get(KEY_NR_FAILED_SERVICES));
		dataForItem.put(KEY_TOTAL_DURATION, ""+ convertMsecsToHrsMinSec((Long) data.get(KEY_TOTAL_DURATION)));
		dataForItem.put(KEY_FAILED_DURATION, ""+ convertMsecsToHrsMinSec((Long) data.get(KEY_FAILED_DURATION)));
		dataForItem.put(KEY_FAILED_DURATION_PERCENTAGE, ""+getSuccessDurationRatioPercentage(data));
		dataForItem.put(KEY_TOTAL_BWHR, asRoundedFloat(((Float) data.get(KEY_TOTAL_BWHR)).floatValue(), 2));
		dataForItem.put(KEY_FAILED_BWHR, asRoundedFloat(((Float) data.get(KEY_FAILED_BWHR)).floatValue(), 2));
		dataForItem.put(KEY_FAILED_BWHR_PERCENTAGE, "" + getSuccessBWHRatioPercentage(data));		
		return new ReportItem(dataForItem, false);
	}
	
	@Override
	public File getAsFile() {
		return report;
	}

	@Override
	public String getReportContents() {
		return reportString;
	}

	protected CharacterSeparatedReportQualitativePortsData(String titleLeader,
			Date startDate, Date endDate, List<DracService> serviceList, List<Schedule> schedules)
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
		fillReport(serviceList, schedules);
	}

	private static String initHeader() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Port");
		buffer.append(SEPARATOR);		
		buffer.append("Nr Assignees (Users)");
		buffer.append(SEPARATOR);
		buffer.append("Services created");
		buffer.append(SEPARATOR);
		buffer.append("Failed Services");
		buffer.append(SEPARATOR);
		buffer.append("Total duration");
		buffer.append(SEPARATOR);
		buffer.append("Duration failed");
		buffer.append(SEPARATOR);
		buffer.append("Failure duration percentage");
		buffer.append(SEPARATOR);
		buffer.append("Total bandwidth-hour (Gbit.hr)");
		buffer.append(SEPARATOR);
		buffer.append("Failed bandwidth-hour (Gbit.hr)");
		buffer.append(SEPARATOR);
		buffer.append("Failure bandwidth-hour percentage");	
		return buffer.toString();
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
		List<String> ports = new ArrayList<String>();
		Map<String, Map<String, Object>> dataByPort = new HashMap<String, Map<String, Object>>();
		
		//iterate source ports
		for (DracService service : serviceList) {	
			Schedule schedule = schedulesByServiceId.get(service.getId());
			String srcTNA = schedule.getSrcTNA();			
			Map<String, Object> portData;
			if (!ports.contains(srcTNA)) {
				ports.add(srcTNA);
				portData = new HashMap<String, Object>();
				dataByPort.put(srcTNA, portData);
			} else {
				portData = dataByPort.get(srcTNA);
			}
			doGroupedData(service, portData, schedule);
			
		}	
		//iterate dest ports
		for (DracService service : serviceList) {	
			Schedule schedule = schedulesByServiceId.get(service.getId());			
			String destTNA = schedule.getDestTNA();
			Map<String, Object> portData;							
			if (!ports.contains(destTNA)) {
				ports.add(destTNA);
				portData = new HashMap<String, Object>();
				dataByPort.put(destTNA, portData);
			} else {
				portData = dataByPort.get(destTNA);
			}			
			doGroupedData(service, portData, schedule);
		}		
		
		StringBuilder buffer = new StringBuilder();
		Date now = new Date();
		report = new File(System.getProperty("java.io.tmpdir", "/tmp")+"/QualitativeData_" + now.getTime() + ".csv".replace("/", File.separator));
		report.deleteOnExit();
		BufferedWriter writer = null;
		writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(report)));
		try {
			writer.write(this.reportTitle);
			buffer.append(this.reportTitle);
			writer.write(HEADER);
			buffer.append(HEADER);

			for (String port : ports) {
				writer.write("\n");
				buffer.append("\n");
				String line = getLine(port, dataByPort.get(port));
				reportData.addLine(getReportItem(port, dataByPort.get(port)));
				writer.write(line);
				buffer.append(line);
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		reportString = buffer.toString();
	}

	private String getLine(String port, Map<String, Object> data) {
		StringBuilder csLline = new StringBuilder();
		csLline.append(encloseInQuotes(port));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + data.get(KEY_NR_ASSIGNEES)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + data.get(KEY_NR_CREATED_SERVICES)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + data.get(KEY_NR_FAILED_SERVICES)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + convertMsecsToHrsMinSec((Long)data.get(KEY_TOTAL_DURATION))));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + convertMsecsToHrsMinSec((Long)data.get(KEY_FAILED_DURATION))));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+getSuccessDurationRatioPercentage(data)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + asRoundedFloat(((Float)data.get(KEY_TOTAL_BWHR)).floatValue(),2)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes("" + asRoundedFloat(((Float)data.get(KEY_FAILED_BWHR)).floatValue(),2)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+getSuccessBWHRatioPercentage(data)));
		return csLline.toString();
	}

	private void doGroupedData(DracService service,
			Map<String, Object> groupedData,Schedule schedule) {
		long nrPorts = 0;
		long nrServices = 0;
		long nrFailedServices = 0;
		long duration = 0;
		long failurDuration = 0;
		float bwhr = 0;
		float failedBWR = 0;

		if (groupedData.containsKey(KEY_NR_ASSIGNEES)) {
			nrPorts = ((Long) groupedData.get(KEY_NR_ASSIGNEES)).longValue();
			nrServices = ((Long) groupedData.get(KEY_NR_CREATED_SERVICES))
					.longValue();
			nrFailedServices = ((Long) groupedData.get(KEY_NR_FAILED_SERVICES))
					.longValue();
			duration = ((Long) groupedData.get(KEY_TOTAL_DURATION)).longValue();
			failurDuration = ((Long) groupedData.get(KEY_FAILED_DURATION))
					.longValue();
			bwhr = ((Float) groupedData.get(KEY_TOTAL_BWHR)).floatValue();
			failedBWR = ((Float) groupedData.get(KEY_FAILED_BWHR)).floatValue();
		}
		groupedData.put(KEY_NR_ASSIGNEES, Long.valueOf(nrPorts)
				+ getNrOfPorts(service));
		groupedData.put(KEY_NR_CREATED_SERVICES, Long.valueOf(nrServices)
				+ getNrOfServices(service));
		groupedData.put(KEY_NR_FAILED_SERVICES, Long.valueOf(nrFailedServices)
				+ getNrOfFailedServices(service));
		groupedData.put(KEY_TOTAL_DURATION, Long.valueOf(duration)
				+ getDurationServices(service));
		groupedData.put(KEY_FAILED_DURATION, Long.valueOf(failurDuration)
				+ getFailedDurationServices(service,schedule));
		groupedData.put(KEY_TOTAL_BWHR, Float.valueOf(bwhr)
				+ getBandWidhtHour(service));
		groupedData.put(KEY_FAILED_BWHR, Float.valueOf(failedBWR)
				+ getFailedBandWidhtHour(service, schedule));

	}

	private long getNrOfPorts(DracService service) {			
		long nrOfPorts = service.getCrossConnections().size();		
		return nrOfPorts;
	}

	private long getNrOfServices(DracService service) {
		return 1;
	}

	
	private boolean isFailed(DracService service) {
		State.SERVICE state = service.getStatus();
		if (state.equals(State.SERVICE.CREATE_FAILED)
				|| state.equals(State.SERVICE.DELETE_FAILED)
				|| state.equals(State.SERVICE.EXECUTION_FAILED)
				|| state.equals(State.SERVICE.EXECUTION_TIMED_OUT)
				|| state.equals(State.SERVICE.ACTIVATION_TIMED_OUT)
				|| state.equals(State.SERVICE.CONFIRMATION_TIMED_OUT)) {
			return true;
		} else {
			return false;
		}
	}

	private long getNrOfFailedServices(DracService service) {
		if(isFailed(service)){
			return 1;
		} else {
			return 0;
		}
	}

	private long getDurationServices(DracService service) {
		return service.getEndTime() - service.getStartTime();
	}

	private long getFailedDurationServices(DracService service, Schedule schedule) {
		long failedTime = 0;
		if(isFailed(service)){		
			//Long scheduledTime = schedule.getEndTime() - schedule.getStartTime();
			Long usedTime = service.getEndTime() - service.getStartTime();			
			failedTime = usedTime;
		}
		return failedTime;
	}

	private float getBandWidhtHour(DracService service) {
		float duration = getDurationServices(service);
		float bandWidth = service.getPath().getRate();		
		return (duration / (1000f * 3600f * 1000f)) * bandWidth;
	}

	private float getFailedBandWidhtHour(DracService service, Schedule schedule) {
		float duration = getFailedDurationServices(service, schedule);
		float bandWidth = service.getPath().getRate();
		return (duration / (1000f * 3600f * 1000f)) * bandWidth;
	}
	
	private int getSuccessDurationRatioPercentage(Map<String, Object> data){
		float ratio=-1;
		float totalDuration = ((Long)data.get(KEY_TOTAL_DURATION)).floatValue();
		float failureDuration = ((Long)data.get(KEY_FAILED_DURATION)).floatValue();
		if(totalDuration == 0 && failureDuration>0){
			ratio = 100f;
		}else if(totalDuration == 0 && failureDuration==0){
			ratio = 0f;
		}else{
			ratio=(failureDuration/totalDuration)*100;
		}
		return Math.round(Float.valueOf(ratio));
	}
	
	private int getSuccessBWHRatioPercentage(Map<String, Object> data){
		float ratio=-1;
		float totalBWH = ((Float)data.get(KEY_TOTAL_BWHR)).floatValue();
		float failureBWH = ((Float)data.get(KEY_FAILED_BWHR)).floatValue();
		if(totalBWH == 0 && failureBWH>0){
			ratio = 100f;
		}else if(totalBWH == 0 && failureBWH==0){
			ratio = 0f;
		}else{
			ratio=(failureBWH/totalBWH)*100;
		}
		return Math.round(Float.valueOf(ratio));
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
