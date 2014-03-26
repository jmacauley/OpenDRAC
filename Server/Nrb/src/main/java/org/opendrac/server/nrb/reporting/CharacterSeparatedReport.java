package org.opendrac.server.nrb.reporting;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class CharacterSeparatedReport implements Report {

	
	private static final long serialVersionUID = 5678318664040563947L;
	private static final String quot = "\"";
	protected static final String SEPARATOR = ";";
	public enum DateFormat {
		// The DbLog schema must be updated if these are changed!
		JUST_DATE_NRS, DATE_TIME_NRS;
	}

	private static SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat(
			"MM-dd-yyyy HH:mm:ss");
	private static SimpleDateFormat justDateFormat = new SimpleDateFormat(
			"MM-dd-yyyy");

	protected String convertDate(Date date, DateFormat format) {

		if (format.equals(DateFormat.JUST_DATE_NRS)) {
			return justDateFormat.format(date);
		} else if (format.equals(DateFormat.DATE_TIME_NRS)) {
			return dateAndTimeFormat.format(date);
		} else {
			return dateAndTimeFormat.format(date);
		}
	}

	protected String convertDate(long date, DateFormat format) {
		Date theDate = new Date(date);
		return convertDate(theDate, format);
	}
	
	protected String encloseInQuotes(String input) {
		return quot + input + quot;
	}
	
	protected float asRoundedFloat(float f, int nrDigits){
		BigDecimal bigDecimal = new BigDecimal(f);
		bigDecimal = bigDecimal.setScale(nrDigits, BigDecimal.ROUND_HALF_UP);
		return bigDecimal.floatValue();
	}
	
	protected String convertMsecsToHrsMinSec(long amount){
		long totSec = amount/1000;		
		long hrs = totSec/3600;
		long min = (totSec - (hrs*3600))/60 ;
		long sec = totSec - (hrs*3600) - (min*60);
		return hrs+" hr, "+min+" min, "+sec+" sec";
	}
}
