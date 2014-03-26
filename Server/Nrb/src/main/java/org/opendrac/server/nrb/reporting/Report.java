package org.opendrac.server.nrb.reporting;

import java.io.File;
import java.io.Serializable;

public interface Report extends Serializable {
    /**
     * Get report as separate file
     * @return File with reporting data 
     */
	public File getAsFile();
	
	/**
	 * Get contents for one report
	 * @return String report contents
	 */
	public String getReportContents();
	
	public String[] getTableKeys();
	public ReportData getReportData();
}
