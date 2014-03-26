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

package com.nortel.appcore.app.drac.server.webserver.struts.schedule.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts.validator.ValidatorActionForm;

public final class FindTimeForm extends ValidatorActionForm {
	private static final long serialVersionUID = 1441870697760958721L;
	private String srcTna = "";
	private String destTna = "";
	private List endPointList = new ArrayList();
	private int duration;
	private int rate;
	private Set<Map<String,String>> endPoints = new TreeSet<Map<String,String>>();
	public String getDestTna() {
		return destTna;
	}

	public int getDuration() {
		return duration;
	}

	public List getEndPointList() {
		return endPointList;
	}

	public int getRate() {
		return rate;
	}

	public String getSrcTna() {
		return srcTna;
	}

	public void setDestTna(String destTna) {
		this.destTna = destTna;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setEndPointList(List endPointList) {
		this.endPointList = endPointList;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public void setSrcTna(String srcTna) {
		this.srcTna = srcTna;
	}

	public Set<Map<String, String>> getEndPoints() {
    	return endPoints;
    }

	public void setEndPoints(Set<Map<String, String>> endPoints) {
    	this.endPoints = endPoints;
    }

}
