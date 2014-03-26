package org.opendrac.webserver.struts.reporting.form;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class LoadReportForm extends ActionForm {
	
	private static final long serialVersionUID = 5366228210686434004L;
	private FormFile theFile;

	  /**
	   * @return Returns the theFile.
	   */
	  public FormFile getTheFile() {
	    return theFile;
	  }
	  /**
	   * @param theFile The FormFile to set.
	   */
	  public void setTheFile(FormFile theFile) {
	    this.theFile = theFile;
	  }
}
