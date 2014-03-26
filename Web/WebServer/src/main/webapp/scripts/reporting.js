function toReportingOverviewPage(){
	var theForm = document.GenerateReportForm;
	theForm.setAttribute("action", "/reports.do");
	theForm.submit();
}