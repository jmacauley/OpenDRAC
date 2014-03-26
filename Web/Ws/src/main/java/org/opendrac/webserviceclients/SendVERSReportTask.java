package org.opendrac.webserviceclients;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendrac.server.nrb.reporting.CharacterSeparatedReportQualitativePortsData;
import org.opendrac.server.nrb.reporting.Report;
import org.opendrac.server.nrb.reporting.ReportData;
import org.opendrac.server.nrb.reporting.ReportItem;
import org.opendrac.www.ws.vers.SURFnetErStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import surfnet_er.ErInsertReportDocument;
import surfnet_er.ErInsertReportDocument.ErInsertReport;
import surfnet_er.ErInsertReportResponseDocument.ErInsertReportResponse;
import surfnet_er.InsertReportInput;

import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;

public class SendVERSReportTask {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Boolean isEnabled = Boolean
	    .getBoolean("org.opendrac.reporting.monthly");

	private List<String> recipients;
	private Map<Integer, Integer> dateRollers;

	private String messageTitleLeader;

	private static final int SUCCESS_RETURN_CODE = 1;
	private static final long WEBSERVICE_TIMEOUT_MILIS = 120000L;

	private String passwToWebservice;
	private String userForWebservice;

	private String password;
	private String user;

	private String ip;// empty placeholder
	private String ticket;// empty placeholder

	private static final String KEY_SERVICES_CREATED = "Services created";
	private static final String KEY_SERVICES_FAILED = "Services failed";
	private static final String KEY_TOTAL_DURATION = "Total duration";
	private static final String KEY_TOTAL_BWH = "Total bandwidth-hour";

	private SURFnetErStub wsClient;
	private String serviceURL;
	private CustomerNameMapper customerResolver = null;

	public void setCustomerResolver(CustomerNameMapper customerResolver) {
		this.customerResolver = customerResolver;
	}

	private static final long NR_SECS_IN_DAY = 24 * 60 * 60;

	private Date getStartDate() {
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(getEndDate());
		startDate.set(Calendar.MILLISECOND, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		for (Integer roller : dateRollers.keySet()) {
			startDate.roll(roller.intValue(), dateRollers.get(roller).intValue());
		}
		return startDate.getTime();
	}

	private Date getEndDate() {
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(new Date());
		endDate.set(Calendar.MILLISECOND, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.HOUR_OF_DAY, 0);
		endDate.set(Calendar.DAY_OF_MONTH, 1);
		return endDate.getTime();
	}

	private Report loadReport() throws Exception {
		RequestHandler handler = RequestHandler.INSTANCE;
		LoginToken token = handler.authorize(ClientLoginType.INTERNAL_LOGIN, user,
		    password.toCharArray(), ip, ticket);
		Date startDate = getStartDate();
		Date endDate = getEndDate();

		Report report = handler.getQualitativeReportByPort(startDate, endDate,
		    token);
		return report;
	}

	public void sendReport() throws Exception {
		sendReport(null);
	}

	public void sendReport(ReportData data) throws Exception {

		if (!isEnabled) {
			log.debug("Monthly reporting is disabled. Enable by setting org.opendrac.reporting.monthly to true");
			return;
		}

		if (data == null) {
			data = loadReport().getReportData();
		}
		int nrErrors = 0;
		int nrHandledItems = 0;
		customerResolver.reIninit();
		File sentReport = new File(System.getProperty("java.io.tmpdir", "/tmp")
		    + "/verssentreport_" + new Date().getTime()
		    + ".txt".replace("/", File.separator));
		File fullReport = new File(System.getProperty("java.io.tmpdir", "/tmp")
		    + "/versfullreport_" + new Date().getTime()
		    + ".txt".replace("/", File.separator));
		File statusReport = new File(System.getProperty("java.io.tmpdir", "/tmp")
		    + "/statusreport_" + new Date().getTime()
		    + ".txt".replace("/", File.separator));
		BufferedWriter sentReportWriter = new BufferedWriter(
		    new OutputStreamWriter(new FileOutputStream(sentReport)));
		BufferedWriter fullReportWriter = new BufferedWriter(
		    new OutputStreamWriter(new FileOutputStream(fullReport)));
		BufferedWriter statusReportWriter = new BufferedWriter(
		    new OutputStreamWriter(new FileOutputStream(statusReport)));

		statusReportWriter.write("use URL: " + serviceURL + " userName "
		    + userForWebservice + " pw: " + passwToWebservice + "\n\n");
		fullReportWriter.write("use URL: " + serviceURL + " userName "
		    + userForWebservice + " pw: " + passwToWebservice + "\n\n");
		sentReportWriter.write("use URL: " + serviceURL + " userName "
		    + userForWebservice + " pw: " + passwToWebservice + "\n\n");
		sentReportWriter.write("Start sending report\n");
		fullReportWriter.write("Start sending report\n");
		try {
			Date startDate = getStartDate();
			Date endDate = getEndDate();
			wsClient = new SURFnetErStub(serviceURL);
			wsClient._getServiceClient().getOptions()
			    .setTimeOutInMilliSeconds(WEBSERVICE_TIMEOUT_MILIS);
			Map<String, Map<String, Object>> aggregateddata = new HashMap<String, Map<String, Object>>();
			Set<String> customerNames = new HashSet<String>();
			for (ReportItem item : data.getData()) {
				String totalDuration = (String) item
				    .get(CharacterSeparatedReportQualitativePortsData.KEY_TOTAL_DURATION);

				if (totalDuration != null) {
					String port = (String) item
					    .get(CharacterSeparatedReportQualitativePortsData.KEY_PORT_ID);
					String customerName = customerResolver.getCustomerNameByPort(port);
					customerNames.add(customerName);
					Long nrServices = (Long) item
					    .get(CharacterSeparatedReportQualitativePortsData.KEY_NR_CREATED_SERVICES);
					Long nrServicesFailed = (Long) item
					    .get(CharacterSeparatedReportQualitativePortsData.KEY_NR_FAILED_SERVICES);

					Float totalBWH = (Float) item
					    .get(CharacterSeparatedReportQualitativePortsData.KEY_TOTAL_BWHR);

					ErInsertReportResponse response;
					response = sendNrSevicesCreatedReport(wsClient, port,
					    "" + nrServices, KEY_SERVICES_CREATED, customerName, startDate,
					    endDate, sentReportWriter, fullReportWriter);
					nrErrors += handleReturnMessage(response, statusReportWriter);
					response = sendNrSevicesCreatedReport(wsClient, port, ""
					    + nrServicesFailed, KEY_SERVICES_FAILED, customerName, startDate,
					    endDate, sentReportWriter, fullReportWriter);
					nrErrors += handleReturnMessage(response, statusReportWriter);

					response = sendNrSevicesCreatedReport(wsClient, port,
					    convertToNrDays("" + totalDuration), KEY_TOTAL_DURATION,
					    customerName, startDate, endDate, sentReportWriter,
					    fullReportWriter);
					nrErrors += handleReturnMessage(response, statusReportWriter);

					response = sendNrSevicesCreatedReport(wsClient, port, "" + totalBWH,
					    KEY_TOTAL_BWH, customerName, startDate, endDate,
					    sentReportWriter, fullReportWriter);
					nrErrors += handleReturnMessage(response, statusReportWriter);
					nrHandledItems += 4;
					upDateAggregateddata(aggregateddata, customerName, port, nrServices,
					    nrServicesFailed, totalBWH,
					    Float.parseFloat(convertToNrDays("" + totalDuration)));
				}
			}
			nrErrors += sendAggregateddata(aggregateddata, startDate, endDate,
			    sentReportWriter, fullReportWriter, statusReportWriter);
			if (data.getData().size() > 0) {
				nrHandledItems += (4 + customerNames.size());
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
			sentReportWriter.write("Error while sending report\n");
			fullReportWriter.write("Error while sending report\n");
			e.printStackTrace(new PrintWriter(sentReportWriter));
			e.printStackTrace(new PrintWriter(fullReportWriter));
			throw e;
		}
		finally {
			sentReportWriter.write("Sending report completed\n");
			fullReportWriter.write("Sending report completed\n");
			if (nrErrors == 0) {
				statusReportWriter
				    .write("All reporting items were sent successfully\nNr items sent: "
				        + nrHandledItems);
			}
			else {
				statusReportWriter.write("\n\nNr items handled: " + nrHandledItems
				    + "\nNr items failed:  " + nrErrors);
			}

			if (sentReportWriter != null) {
				sentReportWriter.close();
			}
			if (fullReportWriter != null) {
				fullReportWriter.close();
			}
			if (statusReportWriter != null) {
				statusReportWriter.close();
			}
		}
	}

	private int sendAggregateddata(
	    Map<String, Map<String, Object>> aggregateddata, Date then, Date now,
	    BufferedWriter sentReportWriter, BufferedWriter fullReportWriter,
	    BufferedWriter statusReportWriter) throws IOException {
		int nrErrors = 0;
		for (String customerName : aggregateddata.keySet()) {
			Map<String, Object> data = aggregateddata.get(customerName);
			Long nrServices = (Long) data
			    .get(CharacterSeparatedReportQualitativePortsData.KEY_NR_CREATED_SERVICES);
			Long nrServicesFailed = (Long) data
			    .get(CharacterSeparatedReportQualitativePortsData.KEY_NR_FAILED_SERVICES);
			Float totalBWH = (Float) data
			    .get(CharacterSeparatedReportQualitativePortsData.KEY_TOTAL_BWHR);
			Float totalDuration = (Float) data
			    .get(CharacterSeparatedReportQualitativePortsData.KEY_TOTAL_DURATION);

			ErInsertReportResponse response;
			response = sendNrSevicesCreatedReport(wsClient, "", "" + nrServices,
			    KEY_SERVICES_CREATED, customerName, then, now, sentReportWriter,
			    fullReportWriter);
			nrErrors += handleReturnMessage(response, statusReportWriter);
			response = sendNrSevicesCreatedReport(wsClient, "",
			    "" + nrServicesFailed, KEY_SERVICES_FAILED, customerName, then, now,
			    sentReportWriter, fullReportWriter);
			nrErrors += handleReturnMessage(response, statusReportWriter);

			response = sendNrSevicesCreatedReport(wsClient, "", "" + totalDuration,
			    KEY_TOTAL_DURATION, customerName, then, now, sentReportWriter,
			    fullReportWriter);
			nrErrors += handleReturnMessage(response, statusReportWriter);

			response = sendNrSevicesCreatedReport(wsClient, "", "" + totalBWH,
			    KEY_TOTAL_BWH, customerName, then, now, sentReportWriter,
			    fullReportWriter);
			nrErrors += handleReturnMessage(response, statusReportWriter);
		}
		return nrErrors;
	}

	private void upDateAggregateddata(
	    Map<String, Map<String, Object>> aggregateddata, String customer,
	    String port, Long nrServices, Long nrServicesFailed, Float totalBWH,
	    Float totalDuration) {
		Map<String, Object> data = null;
		long curNrServices = 0;
		long curNrServicesFailed = 0;
		float curTotalBWH = 0;
		float curTotalDuration = 0;
		if (aggregateddata.containsKey(customer)) {
			data = aggregateddata.get(customer);
			curNrServices = (Long) data
			    .get(CharacterSeparatedReportQualitativePortsData.KEY_NR_CREATED_SERVICES);
			curNrServicesFailed = (Long) data
			    .get(CharacterSeparatedReportQualitativePortsData.KEY_NR_FAILED_SERVICES);
			curTotalBWH = (Float) data
			    .get(CharacterSeparatedReportQualitativePortsData.KEY_TOTAL_BWHR);
			curTotalDuration = (Float) data
			    .get(CharacterSeparatedReportQualitativePortsData.KEY_TOTAL_DURATION);

		}
		else {
			data = new HashMap<String, Object>();
		}
		curNrServices += nrServices.longValue();
		curNrServicesFailed += nrServicesFailed.longValue();
		curTotalBWH += totalBWH.floatValue();
		curTotalDuration += totalDuration.floatValue();
		data.put(
		    CharacterSeparatedReportQualitativePortsData.KEY_NR_CREATED_SERVICES,
		    curNrServices);
		data.put(
		    CharacterSeparatedReportQualitativePortsData.KEY_NR_FAILED_SERVICES,
		    curNrServicesFailed);
		data.put(CharacterSeparatedReportQualitativePortsData.KEY_TOTAL_BWHR,
		    curTotalBWH);
		data.put(CharacterSeparatedReportQualitativePortsData.KEY_TOTAL_DURATION,
		    curTotalDuration);
		data.put(CharacterSeparatedReportQualitativePortsData.KEY_PORT_ID, port);
		aggregateddata.put(customer, data);
	}

	private String convertToNrDays(String duration) {
		String[] part = duration.split(",");
		String hoursStr = part[0].trim().split(" ")[0];
		String minutesStr = part[1].trim().split(" ")[0];
		String secondsStr = part[2].trim().split(" ")[0];
		long hours = Long.parseLong(hoursStr.trim());
		long minutes = Long.parseLong(minutesStr.trim());
		long seconds = Long.parseLong(secondsStr.trim());
		float totalSeconds = seconds + (60 * minutes) + (hours * 60 * 60);
		float nrDays = totalSeconds / NR_SECS_IN_DAY;
		return "" + asRoundedFloat(nrDays, 2);
	}

	private float asRoundedFloat(float f, int nrDigits) {
		BigDecimal bigDecimal = new BigDecimal(f);
		bigDecimal = bigDecimal.setScale(nrDigits, BigDecimal.ROUND_HALF_UP);
		return bigDecimal.floatValue();
	}

	private int handleReturnMessage(ErInsertReportResponse response,
	    Writer statusReportWriter) throws IOException {
		int nrErrors = 0;
		if (response.getReturnCode() != SUCCESS_RETURN_CODE) {
			nrErrors++;
			statusReportWriter.write("Reported error: " + response.getReturnCode()
			    + " - " + response.getReturnText() + "\n");
		}
		return nrErrors;
	}

	private ErInsertReportResponse sendNrSevicesCreatedReport(
	    SURFnetErStub versClient, String instance, String value, String type,
	    String organisation, Date startDate, Date endDate,
	    Writer sentReportWriter, Writer fullReportWriter) throws IOException {
		InsertReportInput reportData = getInsertReportInput(instance, value, type,
		    organisation, startDate, endDate);
		ErInsertReport messageBody = ErInsertReport.Factory.newInstance();

		messageBody.setUsername(userForWebservice);
		messageBody.setPassword(passwToWebservice);
		messageBody.setParameters(reportData);

		ErInsertReportDocument soapCallDocument = ErInsertReportDocument.Factory
		    .newInstance();
		soapCallDocument.setErInsertReport(messageBody);

		sentReportWriter.write(soapCallDocument.xmlText() + "\n\n");
		fullReportWriter.write(soapCallDocument.xmlText() + "\n");
		ErInsertReportResponse versRepsonse = versClient.er_InsertReport(
		    soapCallDocument).getErInsertReportResponse();
		fullReportWriter.write(versRepsonse.xmlText() + "\n\n");
		return versRepsonse;
	}

	private InsertReportInput getInsertReportInput(String instance, String value,
	    String type, String organisation, Date startDate, Date endDate) {
		InsertReportInput insertReportInput = InsertReportInput.Factory
		    .newInstance();
		insertReportInput.setInstance(instance);
		insertReportInput.setValue(value);
		insertReportInput.setType(type);
		insertReportInput.setIsKPI(false);
		insertReportInput.setOrganisation(organisation);
		insertReportInput.setPeriod(getPeriodDefinition(startDate));
		return insertReportInput;
	}

	private String getPeriodDefinition(Date startDate) {
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		int monthnr = startCal.get(Calendar.MONTH) + 1;
		StringBuffer definition = new StringBuffer();
		definition.append(startCal.get(Calendar.YEAR));
		definition.append("-");
		definition.append(monthnr < 10 ? ("0" + monthnr) : monthnr);
		return definition.toString();
	}

	public List<String> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
	}

	public Map<Integer, Integer> getDateRollers() {
		return dateRollers;
	}

	public void setDateRollers(Map<Integer, Integer> dateRollers) {
		this.dateRollers = dateRollers;
	}

	public String getUserForWebservice() {
		return userForWebservice;
	}

	public void setUserForWebservice(String user) {
		this.userForWebservice = user;
	}

	public String getMessageTitleLeader() {
		return messageTitleLeader;
	}

	public void setMessageTitleLeader(String messageTitleLeader) {
		this.messageTitleLeader = messageTitleLeader;
	}

	public String getPasswToWebservice() {
		return passwToWebservice;
	}

	public void setPasswToWebservice(String passwToWebservice) {
		this.passwToWebservice = passwToWebservice;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getServiceURL() {
		return serviceURL;
	}

	public void setServiceURL(String serviceURL) {
		this.serviceURL = serviceURL;
	}
}
