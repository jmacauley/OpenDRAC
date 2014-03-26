package org.opendrac.server.nrb.reporting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportData implements Serializable{

  private static final long serialVersionUID = -3122156898978111172L;
	private String metadata;
	private String headerdata;
	private List<ReportItem> data = new ArrayList<ReportItem>();
	private String[] collumnKeys;
	private Map<String,String>collumNames=new HashMap<String, String>();
	
	public ReportData(String[] collumnKeys){
		this.collumnKeys=collumnKeys;
	}
	public void setCollumnName(String key, String value){
		collumNames.put(key, value);
	}
	public String getCollumnName(String key){ // NO_UCD
		return collumNames.get(key);
	}
	public Map<String,String> getCollumnNames(){
		return collumNames;
	}
	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public String getHeaderdata() {
		return headerdata;
	}

	public void setHeaderdata(String headerdata) {
		this.headerdata = headerdata;
	}

	public List<ReportItem> getData() {
		return data;
	}
	
	public ReportItem getLine(int lineNumber){
		return data.get(lineNumber);
	}
	
	public void addLine(ReportItem line){
		data.add(line);
	}
	public String[] getCollumnKeys(){
		return collumnKeys;
	}
}
