package org.opendrac.server.nrb.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;

public class CharacterSeparatedReportLogs extends CharacterSeparatedReport {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	private static final long serialVersionUID = 8289420175017684038L;

	private static final String DEFAULT_TITLE_LEADER = "Report logged events";
	
	private static final String HEADER;
	private File report;
	private String reportString = "";
	private String reportTitle = "";
	static {
		HEADER = initHeader();
	}

	protected CharacterSeparatedReportLogs(String titleLeader, Date startDate,
			Date endDate, List<LogRecord> records) throws IOException {
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
		fillReport(records);
	}

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
		buffer.append("Time");
		buffer.append(SEPARATOR);
		buffer.append("Originator");
		buffer.append(SEPARATOR);
		buffer.append("Address");
		buffer.append(SEPARATOR);
		buffer.append("Billing group");
		buffer.append(SEPARATOR);
		buffer.append("Severity");
		buffer.append(SEPARATOR);
		buffer.append("Category");
		buffer.append(SEPARATOR);
		buffer.append("Type");
		buffer.append(SEPARATOR);
		buffer.append("Resource");
		buffer.append(SEPARATOR);
		buffer.append("Result");
		buffer.append(SEPARATOR);
		buffer.append("Description");
		return buffer.toString();
	}

	private void fillReport(List<LogRecord> records) throws IOException {
		StringBuilder buffer = new StringBuilder();
		Date now = new Date();
		report = new File(System.getProperty("java.io.tmpdir", "/tmp")+"/Logs_" + now.getTime() + ".csv".replace("/", File.separator));
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
			for (LogRecord record : records) {
				writer.write("\n");
				buffer.append("\n");
				String line = getLine(record);
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

	private String getLine(LogRecord record) {
		StringBuilder csLline = new StringBuilder();
		csLline.append(encloseInQuotes(convertDate(record.getTime(),
				CharacterSeparatedReport.DateFormat.DATE_TIME_NRS)));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(record.getOriginator()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(record.getIp()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+record.getBillingGroup()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+record.getSeverity()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+record.getCategory()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+record.getType()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+record.getResource()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(""+record.getResult()));
		csLline.append(SEPARATOR);
		csLline.append(encloseInQuotes(record.getDescription()));
		return csLline.toString();
	}

	@Override
	public String[] getTableKeys() {
		return null;
	}

	@Override
	public ReportData getReportData() {
		return null;
	}

}
