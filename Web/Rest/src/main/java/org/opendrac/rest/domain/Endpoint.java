package org.opendrac.rest.domain;

import com.nortel.appcore.app.drac.common.types.EndPointType;

public class Endpoint {

	private String name;
	private double usage;
	private EndPointType endPointType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getUsage() {
		return usage;
	}

	public void setUsage(double usage) {
		this.usage = usage;
	}

	public EndPointType getEndPointType() {
		return endPointType;
	}

	public void setEndPointType(EndPointType endPointType) {
		this.endPointType = endPointType;
	}

}