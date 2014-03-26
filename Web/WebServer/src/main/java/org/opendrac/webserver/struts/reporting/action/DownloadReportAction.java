package org.opendrac.webserver.struts.reporting.action;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DownloadAction;
import org.opendrac.webserver.struts.reporting.form.GenerateReportForm;

import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;

public class DownloadReportAction extends DownloadAction {

	@Override
	protected StreamInfo getStreamInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		HttpSession session = request.getSession();
		GenerateReportForm generateReportForm = (GenerateReportForm)form;
		RequestHandler handler = RequestHandler.INSTANCE;		
		LoginToken token = (LoginToken) session.getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);
		
		String contentType = "text/csv";
		
		String startDate = generateReportForm.getStartdate();
		String startTime = generateReportForm.getStartTime();
		String endDate = generateReportForm.getEnddate();
		String endTime = generateReportForm.getEndTime();
		
		Date reportStartDate = new Date(DracHelper.parseWebDateToMillis((Locale) session.getAttribute(DracConstants.MYLOCALE), tz, startDate, startTime));	
		Date reportEndDate = new Date(DracHelper.parseWebDateToMillis((Locale) session.getAttribute(DracConstants.MYLOCALE), tz, endDate, endTime));	
		
		Map<String, String> filter = new HashMap<String, String>();		
		File report = handler.getAggregatedReportAsFile(1, "Aggregated Reports", reportStartDate, reportEndDate, filter, token);		
		response.setHeader("Content-disposition", "attachment; filename=" + report.getName());
        return new FileStreamInfo(contentType, report);
	}		
}