package org.opendrac.webserver.struts.reporting.form;

import java.util.List;
import java.util.Locale;

import org.apache.struts.validator.ValidatorActionForm;
import org.opendrac.server.nrb.reporting.ReportItem;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

public class GenerateReportForm extends ValidatorActionForm {

	private static final long serialVersionUID = 8215721809807484463L;
	private String startdate = DracConstants.EMPTY_STRING;
	private String enddate = DracConstants.EMPTY_STRING;
	private String startTime = DracConstants.EMPTY_STRING;
	private String endTime = DracConstants.EMPTY_STRING;

	private Locale locale =  Locale.getDefault();
	private String selectedItem = "";
	private String[] selectedItems = {};
	private String command = "";

	private List<ReportItem> items;
	
	
	public List<ReportItem> getItems() {
		return items;
	}

	public void setItems(List<ReportItem> items) {
		this.items = items;
	}

	public static final String CANCEL_ACTION = "cancel";
	public static final String ACTIVATE_ACTION = "activate";
	public static final String CANCEL_SEL_ACTION = "cancelAll";
	public static final String ACTIVATE_SEL_ACTION = "activateAll";

	public String getCommand() {
		return command;
	}

	/**
	 * @return the enddate
	 */
	public String getEnddate() {
		return enddate;
	}


	public String getSelectedItem() {
		return selectedItem;
	}

	public String[] getSelectedItems() {
		return this.selectedItems;
	}

	/**
	 * @return the startdate
	 */
	public String getStartdate() {
		return startdate;
	}

	public void setCommand(String action) {
		this.command = action;
	}

	/**
	 * @param enddate
	 *          the enddate to set
	 */
	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}


	public void setSelectedItem(String selectedItem) {
		this.selectedItem = selectedItem;
	}

	public void setSelectedItems(String[] selectedItems) {
		this.selectedItems = selectedItems;
	}

	/**
	 * @param startdate
	 *          the startdate to set
	 */
	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Locale getLocale() {
    	return locale;
    }

	public void setLocale(Locale locale) {
    	this.locale = locale;
    }

}
