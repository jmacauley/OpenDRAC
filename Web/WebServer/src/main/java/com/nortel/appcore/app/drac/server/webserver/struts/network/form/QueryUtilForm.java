/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.server.webserver.struts.network.form;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * @author nipun This file is the form bean for the Utilization related screens.
 */
public final class QueryUtilForm extends ValidatorForm {
	private static final long serialVersionUID = -1642867992973660737L;
	private String endpoint = DracConstants.EMPTY_STRING;
	private List<String> endPointList = new ArrayList<String>();
	private String startTime = DracConstants.EMPTY_STRING;
	private String endTime = DracConstants.EMPTY_STRING;
	private String startdate = DracConstants.EMPTY_STRING;
	private String enddate = DracConstants.EMPTY_STRING;
	private String range = DracConstants.EMPTY_STRING;

	public static final String RANGE_ONE_DAY = "oneDay";
	public static final String RANGE_ONE_WEEK = "oneWeek";
	public static final String RANGE_ONE_MONTH = "oneMonth";
	public static final String RANGE_THREE_MONTH = "threeMonth";

	public QueryUtilForm() {
		super();

	}

	public String getEnddate() {
		return enddate;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public List getEndPointList() {
		return endPointList;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getRange() {
		return range;
	}

	public String getStartdate() {
		return startdate;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setEnddate(String string) {
		enddate = string;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setEndPointList(List<String> endPointList) {
		this.endPointList = new ArrayList<String>(new TreeSet(endPointList));
	}

	public void setEndTime(String string) {
		endTime = string;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public void setStartdate(String string) {
		startdate = string;
	}

	public void setStartTime(String string) {
		startTime = string;
	}

}
