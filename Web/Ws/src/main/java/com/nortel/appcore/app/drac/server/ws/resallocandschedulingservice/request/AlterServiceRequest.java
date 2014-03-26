package com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request;

import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;

public class AlterServiceRequest {

	private static final String SERVICE_TIME_EXTENSION = "srv:"
	    + RequestResponseConstants.RAW_DONT_CARE_NODE
	    + "/srv:" + RequestResponseConstants.RAW_DONT_CARE_NODE + "/srv:"
	    + RequestResponseConstants.RAW_SERVICE_TIME_EXTENSION;
	
	private static final String SCHEDULE_ID_SERVICE_EXTENSION = "srv:"
	    + RequestResponseConstants.RAW_DONT_CARE_NODE
	    + "/srv:" + RequestResponseConstants.RAW_DONT_CARE_NODE + "/srv:"
	    + RequestResponseConstants.RAW_SCHEDULE_ID_STRING;
	
	public static String getTimeServiceExtension(){
		return SERVICE_TIME_EXTENSION;
	}
	public static String getScheduleIdServiceExtension(){
		return SCHEDULE_ID_SERVICE_EXTENSION;
	}
	
}
