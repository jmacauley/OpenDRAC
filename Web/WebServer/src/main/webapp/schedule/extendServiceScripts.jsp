<script language="javascript">
var queryWin;
var queryTimeWin;
var buttonVal = '<c:out value='${sessionScope["drac.service.extendService.close"]}'/>';
var cancelButtonHTML = '<input type="button" value=\"'+buttonVal+'\" onclick=\"Javascript:queryWin.hide();\"/>';
function showDiv() {
	var feedBackMsg = '<c:out value='${sessionScope["drac.service.extendService.wait"]}'/>';
	var errorMSG = validatForm();
	if(errorMSG!=""){
		feedBackMsg=errorMSG;
	}
	
    loadingElem = document.getElementById("loadingDiv").innerHTML = "<center><img src='/images/loadingAnimation.gif' id='progressIMG'><br><span id='feedbackMSG'>"+feedBackMsg+"</span><p><input type='button' value='<c:out value='${sessionScope["drac.service.extendService.close"]}'/>' onclick='Javascript:queryWin.hide();'/></center>";
    queryWin = dhtmlwindow.open("queryWindow", "div", "loadingDiv", "<c:out value='${sessionScope["drac.service.extendService.title"]}'/>",
        "width=240px,height=100px,resize=0,scrolling=0,center=1", "recal");
    if(errorMSG == ""){
    	extendServiceTime();
    }
}
 function validatForm(){
	 var extensionMinutes = document.getElementById("extensionMinutes").value; 
	 var errorMSG = 'Enter a posive integer for Nr. minutes extension';
	 if(extensionMinutes==null || extensionMinutes==""){
		 return errorMSG;
	 }else{
		
		 var mins = parseInt(extensionMinutes);
		 if(mins == 'NaN' || mins<1){
			 return errorMSG;
		 }else{
			 return '';
	 	 }
	 }
}

function extendServiceTime() {
    var serviceId = document.getElementById("serviceId");
    var extension = document.getElementById("extensionMinutes");    

    var url = "/drac?action=extendService&serviceId=" + escape(serviceId.value) + "&nrMinutesExtension=" + escape(extension.value);
    new AJAXInteraction(url, processExtendServiceResponse).doGet();
}

function processExtendServiceResponse(responseXML) {
	var message = responseXML.getElementsByTagName("extensionMessage")[0].firstChild.nodeValue;
	var extensionMinutes = responseXML.getElementsByTagName("nrMinutesExtended")[0].firstChild.nodeValue;
	var endTime = responseXML.getElementsByTagName("endTime")[0].firstChild.nodeValue;
    var box = document.getElementById("loadingDiv");
    if (box) {
    	box.innerHTML = "<p><center>"+message+"<br/><img src='/images/spacer.gif' height='19'/><br/>"+cancelButtonHTML+"</center></p>";
    	queryWin.load("div", "loadingDiv");
        if (extensionMinutes>0) {   
        	var enddate = document.getElementById("endDate");   
        	document.getElementById("extensionMinutes").value="";           
        	enddate.innerHTML= endTime.trim();        	
        } 
    }
}

</script>