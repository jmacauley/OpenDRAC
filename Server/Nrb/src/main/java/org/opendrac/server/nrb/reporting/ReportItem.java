package org.opendrac.server.nrb.reporting;

import java.io.Serializable;
import java.util.Map;

public class ReportItem implements Serializable {

	private static final long serialVersionUID = -2337888536023207479L;
	private boolean isASeparator;
	private final Map<String, Object> data;

	// data['assignee']
	public Map<String, Object> getData() {
		return data;
	}

	public ReportItem(Map<String, Object> data, boolean isASeparator) {
		this.data = data;
		this.isASeparator = isASeparator;
	}
	
	public boolean isSeparator() {
		return isASeparator;
	}

	public String getSeparatorValue() {
		String value = null;
		if (isASeparator) {
			for (String key : data.keySet()) {
				value = data.get(key).toString();
			}
		}
		return value;
	}

	public void clear() {
		data.clear();
	}
	
	public Object get(String arg) {
		return data.get(arg);
	}

}
