package org.opendrac.server.nrb.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

public class AggregatedReport extends CharacterSeparatedReport {

	private static final long serialVersionUID = 3992125278026898815L;

	private static final String DEFAULT_TITLE_LEADER = "Aggregated reports";


	private String reportTitle = "";
	private File report;
	private String reportString = "";

	protected AggregatedReport(String titleLeader, Date startDate, Date endDate,
			List<Report> reports) throws IOException {
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
		aggregate(reports);
	}

	@Override
	public File getAsFile() {
		return report;
	}

	@Override
	public String getReportContents() {
		return reportString;
	}

	private void aggregate(List<Report> reports) throws IOException {
		Date now = new Date();
		StringBuilder buffer = new StringBuilder();
		report = new File(System.getProperty("java.io.tmpdir", "/tmp")+"/AggregatedReports_" + now.getTime() + ".csv".replace("/", File.separator));
		report.deleteOnExit();
		BufferedWriter writer = null;
		writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(report)));
		try {
			writer.write(reportTitle);
			buffer.append(reportTitle);
			for (Report report : reports) {
				writer.write("\n\n");
				buffer.append("\n\n");
				
				writer.write(report.getReportContents());
				buffer.append(report.getReportContents());
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		reportString = buffer.toString();
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
