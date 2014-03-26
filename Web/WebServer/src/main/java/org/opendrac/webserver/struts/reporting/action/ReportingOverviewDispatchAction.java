package org.opendrac.webserver.struts.reporting.action;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.opendrac.server.nrb.reporting.Report;
import org.opendrac.webserver.struts.reporting.form.GenerateReportForm;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracBaseMappingDispatchAction;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;

public class ReportingOverviewDispatchAction extends
		DracBaseMappingDispatchAction {

	// These page forwards forces validation check by doing executeBefore()
	public ActionForward getReportsList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		return mapping.findForward(DracConstants.REPORTING_OVERVIEW_PAGE);
	}
	public ActionForward showList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		HttpSession session = request.getSession();
		GenerateReportForm generateReportForm = (GenerateReportForm)form;
		RequestHandler handler = RequestHandler.INSTANCE;		
		LoginToken token = (LoginToken) session.getAttribute(DracConstants.TOKEN_OBJ);
		TimeZone tz = DracHelper.getTimeZone(token);

		String startDate = generateReportForm.getStartdate();
		String startTime = generateReportForm.getStartTime();
		String endDate = generateReportForm.getEnddate();
		String endTime = generateReportForm.getEndTime();
		
		Date reportStartDate = new Date(DracHelper.parseWebDateToMillis((Locale) session.getAttribute(DracConstants.MYLOCALE), tz, startDate, startTime));	
		Date reportEndDate = new Date(DracHelper.parseWebDateToMillis((Locale) session.getAttribute(DracConstants.MYLOCALE), tz, endDate, endTime));	

		List<UserGroupName> groups = handler.getFilterGroupsForCurrentUser(token);
		List<Report> reports = handler.getAggregatedReportForGui(0, "", reportStartDate, reportEndDate, null, token);
	
		request.setAttribute("reports", reports);
		if(groups==null || groups.size()==0){
			request.setAttribute("filterGroup","all");
		}else{
			request.setAttribute("filterGroup", groups.get(0).toString());
		}
		request.setAttribute("GenerateReportForm", generateReportForm);
		request.setAttribute("startdate", generateReportForm.getStartdate());
		request.setAttribute("enddate", generateReportForm.getEnddate());
		return mapping.findForward(DracConstants.REPORTS_OVERVIEW_PAGE);
	}
	
	public ActionForward getLogList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(DracConstants.REPORTING_OVERVIEW_PAGE);
	}

}
