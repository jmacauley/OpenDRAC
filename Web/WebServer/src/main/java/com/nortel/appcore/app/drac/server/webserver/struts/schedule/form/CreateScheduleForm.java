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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts.validator.ValidatorActionForm;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on 12-Jul-2006
 */
public final class CreateScheduleForm extends ValidatorActionForm {
	private static final long serialVersionUID = 169310768735673622L;
	public static final String HOP_METRIC = "hop";
	public static final String COST_METRIC = "cost";
	public static final String METRIC2_METRIC = "metric2";
	public static final String CSPF_ALG = "cspf";

	// Fields pertaining to create schedule page
	private String schName = DracConstants.EMPTY_STRING;
	private String rate = DracConstants.EMPTY_STRING;
	private String sourceChannel = DracConstants.EMPTY_STRING;
	private String destChannel = DracConstants.EMPTY_STRING;
	private String srcLayer = DracConstants.LAYER2_STRING;
	private String destLayer = DracConstants.EMPTY_STRING;
	private String srcTna = DracConstants.EMPTY_STRING;
	private String destTna = DracConstants.EMPTY_STRING;
	private String startTime = DracConstants.EMPTY_STRING;
	private String startdate = DracConstants.EMPTY_STRING;
	private String endTime = DracConstants.EMPTY_STRING;
	private String enddate = DracConstants.EMPTY_STRING;
	private String recEndDate = DracConstants.EMPTY_STRING;
	private String srcGroup = DracConstants.EMPTY_STRING;
	private String destGroup = DracConstants.EMPTY_STRING;
	private String srcResGroup = DracConstants.EMPTY_STRING;
	private String destResGroup = DracConstants.EMPTY_STRING;
	private String billingGroup = DracConstants.EMPTY_STRING;
	private long duration;
	private int numOccur = 1;
	private String protectionType = PathType.PROTECTION_TYPE.UNPROTECTED
	    .toString();

	// REMOVE
	private String scheduleType = Schedule.ACTIVATION_TYPE.RESERVATION_AUTOMATIC
	    .toString();

	private String email = DracConstants.EMPTY_STRING;
	private String algorithm = DracConstants.EMPTY_STRING;
	private String routingMetric = DracConstants.EMPTY_STRING;
	private int metricValue;
	private String srlg = DracConstants.EMPTY_STRING;
	private String srsg = DracConstants.EMPTY_STRING;
	private String systemOffsetTime = DracConstants.EMPTY_STRING;

	// layer 0
	private String srcWavelength = DracConstants.EMPTY_STRING;
	private String destWavelength = DracConstants.EMPTY_STRING;

	private String srcVlan = DracConstants.EMPTY_STRING;
	private String dstVlan = DracConstants.EMPTY_STRING;

	private boolean vcatRoutingOption;

	// Fields pertaining to recurrence
	private boolean recurrence;
	private String frequency = DracConstants.EMPTY_STRING;
	private boolean weeklySun;
	private boolean weeklyMon;
	private boolean weeklyTue;
	private boolean weeklyWed;
	private boolean weeklyThu;
	private boolean weeklyFri;
	private boolean weeklySat;
	private String monthlyDay = DracConstants.EMPTY_STRING;
	private String yearlyDay = DracConstants.EMPTY_STRING;
	private String yearlyMonth = DracConstants.EMPTY_STRING;
	private String locale = DracConstants.LANGUAGE_EN;

	// Extra helper fields
	private List<UserGroupName> groups = new ArrayList<UserGroupName>();
	private int dur_hr;
	private int dur_min;
	private boolean startNow;
	private boolean endNever;
	private List<String> tnas = new ArrayList<String>();

	// Fields returned when schedule is created for display
	private String status = DracConstants.EMPTY_STRING;
	private String[] debugOptions = {};

	/**
	 * @return the algorithm
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * @return the billingGroup
	 */
	public String getBillingGroup() {
		return billingGroup;
	}

	public String[] getDebugOptions() {
		return debugOptions;
	}

	/**
	 * @return the destChannel
	 */
	public String getDestChannel() {
		return destChannel;
	}

	/**
	 * @return the destGroup
	 */
	public String getDestGroup() {
		return destGroup;
	}

	/**
	 * @return the destLayer
	 */
	public String getDestLayer() {
		return destLayer;
	}

	public String getDestResGroup() {
		return destResGroup;
	}

	/**
	 * @return Returns the desttna.
	 */
	public String getDestTna() {
		return destTna;
	}

	public String getDestWavelength() {
		return destWavelength;
	}

	/**
	 * @return the dstVlan
	 */
	public String getDstVlan() {
		return dstVlan;
	}

	/**
	 * @return Returns the durHr.
	 */
	public int getDur_hr() {
		return dur_hr;
	}

	/**
	 * @return Returns the durMin.
	 */
	public int getDur_min() {
		return dur_min;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @return Returns the email.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return Returns the enddate.
	 */
	public String getEnddate() {
		return enddate;
	}

	/**
	 * @return Returns the endtime.
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @return Returns the frequency.
	 */
	public String getFrequency() {
		return frequency;
	}

	/**
	 * @return the groups
	 */
	public List<UserGroupName> getGroups() {
		return groups;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @return the metricValue
	 */
	public int getMetricValue() {
		return metricValue;
	}

	/**
	 * @return Returns the monthlyDay.
	 */
	public String getMonthlyDay() {
		return monthlyDay;
	}

	public int getNumOccur() {
		return numOccur;
	}

	public String getProtectionType() {
		return protectionType;
	}

	/**
	 * @return Returns the rate.
	 */
	public String getRate() {
		return rate;
	}

	/**
	 * @return the recEndDate
	 */
	public String getRecEndDate() {
		return recEndDate;
	}

	/**
	 * @return the routingMetric
	 */
	public String getRoutingMetric() {
		return routingMetric;
	}

	// REMOVE
	public String getScheduleType() {
		return scheduleType;
	}

	/**
	 * @return Returns the schName.
	 */
	public String getSchName() {
		return schName;
	}

	/**
	 * @return the sourceChannel
	 */
	public String getSourceChannel() {
		return sourceChannel;
	}

	/**
	 * @return the srcGroup
	 */
	public String getSrcGroup() {
		return srcGroup;
	}

	/**
	 * @return the srcLayer
	 */
	public String getSrcLayer() {
		return srcLayer;
	}

	public String getSrcResGroup() {
		return srcResGroup;
	}

	/**
	 * @return Returns the srctna.
	 */
	public String getSrcTna() {
		return srcTna;
	}

	/**
	 * @return the srcVlan
	 */
	public String getSrcVlan() {
		return srcVlan;
	}

	public String getSrcWavelength() {
		return srcWavelength;
	}

	/**
	 * @return Returns the srlg.
	 */
	public String getSrlg() {
		return srlg;
	}

	public String getSrsg() {
		return srsg;
	}

	/**
	 * @return Returns the startdate.
	 */
	public String getStartdate() {
		return startdate;
	}

	/**
	 * @return Returns the starttime.
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the systemOffsetTime
	 */
	public String getSystemOffsetTime() {
		return systemOffsetTime;
	}

	/**
	 * @return Returns the yearlyDay.
	 */
	public String getYearlyDay() {
		return yearlyDay;
	}

	/**
	 * @return Returns the yearlyMonth.
	 */
	public String getYearlyMonth() {
		return yearlyMonth;
	}

	/**
	 * @return the endNever
	 */
	public boolean isEndNever() {
		return endNever;
	}

	/**
	 * @return the recurrence
	 */
	public boolean isRecurrence() {
		return recurrence;
	}

	/**
	 * @return the startNow
	 */
	public boolean isStartNow() {
		return startNow;
	}

	public boolean isVcatRoutingOption() {
		return vcatRoutingOption;
	}

	/**
	 * @return Returns the weeklyFri.
	 */
	public boolean isWeeklyFri() {
		return weeklyFri;
	}

	/**
	 * @return Returns the weeklyMon.
	 */
	public boolean isWeeklyMon() {
		return weeklyMon;
	}

	/**
	 * @return Returns the weeklySat.
	 */
	public boolean isWeeklySat() {
		return weeklySat;
	}

	/**
	 * @return Returns the weeklySun.
	 */
	public boolean isWeeklySun() {
		return weeklySun;
	}

	/**
	 * @return Returns the weeklyThu.
	 */
	public boolean isWeeklyThu() {
		return weeklyThu;
	}

	/**
	 * @return Returns the weeklyTue.
	 */
	public boolean isWeeklyTue() {
		return weeklyTue;
	}

	/**
	 * @return Returns the weeklyWed.
	 */
	public boolean isWeeklyWed() {
		return weeklyWed;
	}

	/**
	 * @param algorithm
	 *          the algorithm to set
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * @param billingGroup
	 *          the billingGroup to set
	 */
	public void setBillingGroup(String billingGroup) {
		this.billingGroup = billingGroup;
	}

	public void setDebugOptions(String[] debugOptions) {
		this.debugOptions = debugOptions;
	}

	/**
	 * @param destChannel
	 *          the destChannel to set
	 */
	public void setDestChannel(String destChannel) {
		this.destChannel = destChannel;
	}

	/**
	 * @param destGroup
	 *          the destGroup to set
	 */
	public void setDestGroup(String destGroup) {
		this.destGroup = destGroup;
	}

	/**
	 * @param destLayer
	 *          the destLayer to set
	 */
	public void setDestLayer(String destLayer) {
		this.destLayer = destLayer;
	}

	public void setDestResGroup(String destResGroup) {
		this.destResGroup = destResGroup;
	}

	/**
	 * @param desttna
	 *          The desttna to set.
	 */
	public void setDestTna(String desttna) {
		this.destTna = desttna;
	}

	public void setDestWavelength(String destWavelength) {
		this.destWavelength = destWavelength;
	}

	/**
	 * @param dstVlan
	 *          the dstVlan to set
	 */
	public void setDstVlan(String dstVlan) {
		this.dstVlan = dstVlan;
	}

	/**
	 * @param durHr
	 *          The durHr to set.
	 */
	public void setDur_hr(int durHr) {
		this.dur_hr = durHr;
	}

	/**
	 * @param durMin
	 *          The durMin to set.
	 */
	public void setDur_min(int durMin) {
		this.dur_min = durMin;
	}

	/**
	 * @param duration
	 *          the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * @param email
	 *          The email to set.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @param enddate
	 *          The enddate to set.
	 */
	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	/**
	 * @param endNever
	 *          the endNever to set
	 */
	public void setEndNever(boolean endNever) {
		this.endNever = endNever;
	}

	/**
	 * @param endtime
	 *          The endtime to set.
	 */
	public void setEndTime(String endtime) {
		this.endTime = endtime;
	}

	/**
	 * @param frequency
	 *          The frequency to set.
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	/**
	 * @param groups
	 *          the groups to set
	 */
	public void setGroups(List<UserGroupName> groups) {
		this.groups = groups;
	}

	/**
	 * @param locale
	 *          the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @param metricValue
	 *          the metricValue to set
	 */
	public void setMetricValue(int metricValue) {
		this.metricValue = metricValue;
	}

	/**
	 * @param monthlyDay
	 *          The monthlyDay to set.
	 */
	public void setMonthlyDay(String monthlyDay) {
		this.monthlyDay = monthlyDay;
	}

	public void setNumOccur(int numOccur) {
		this.numOccur = numOccur;
	}

	public void setProtectionType(String protectionType) {
		this.protectionType = protectionType;
	}

	/**
	 * @param rate
	 *          The rate to set.
	 */
	public void setRate(String rate) {
		this.rate = rate;
	}

	/**
	 * @param recEndDate
	 *          the recEndDate to set
	 */
	public void setRecEndDate(String recEndDate) {
		this.recEndDate = recEndDate;
	}

	/**
	 * @param recurrence
	 *          the recurrence to set
	 */
	public void setRecurrence(boolean recurrence) {
		this.recurrence = recurrence;
	}

	/**
	 * @param routingMetric
	 *          the routingMetric to set
	 */
	public void setRoutingMetric(String routingMetric) {
		this.routingMetric = routingMetric;
	}

	// REMOVE
	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	/**
	 * @param schName
	 *          The schName to set.
	 */
	public void setSchName(String schName) {
		this.schName = schName;
	}

	/**
	 * @param sourceChannel
	 *          the sourceChannel to set
	 */
	public void setSourceChannel(String sourceChannel) {
		this.sourceChannel = sourceChannel;
	}

	/**
	 * @param srcGroup
	 *          the srcGroup to set
	 */
	public void setSrcGroup(String srcGroup) {
		this.srcGroup = srcGroup;
	}

	/**
	 * @param srcLayer
	 *          the srcLayer to set
	 */
	public void setSrcLayer(String srcLayer) {
		this.srcLayer = srcLayer;
	}

	public void setSrcResGroup(String srcResGroup) {
		this.srcResGroup = srcResGroup;
	}

	/**
	 * @param srctna
	 *          The srctna to set.
	 */
	public void setSrcTna(String srctna) {
		this.srcTna = srctna;
	}

	/**
	 * @param srcVlan
	 *          the srcVlan to set
	 */
	public void setSrcVlan(String srcVlan) {
		this.srcVlan = srcVlan;
	}

	public void setSrcWavelength(String srcWavelength) {
		this.srcWavelength = srcWavelength;
	}

	/**
	 * @param srlg
	 *          The srlg to set.
	 */
	public void setSrlg(String srlg) {
		this.srlg = srlg;
	}

	public void setSrsg(String srsg) {
		this.srsg = srsg;
	}

	/**
	 * @param startdate
	 *          The startdate to set.
	 */
	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	/**
	 * @param startNow
	 *          the startNow to set
	 */
	public void setStartNow(boolean startNow) {
		this.startNow = startNow;
	}

	/**
	 * @param starttime
	 *          The starttime to set.
	 */
	public void setStartTime(String starttime) {
		this.startTime = starttime;
	}

	/**
	 * @param status
	 *          The status to set.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @param systemOffsetTime
	 *          the systemOffsetTime to set
	 */
	public void setSystemOffsetTime(String systemOffsetTime) {
		this.systemOffsetTime = systemOffsetTime;
	}

	public void setVcatRoutingOption(boolean vcatRoutingOption) {
		this.vcatRoutingOption = vcatRoutingOption;
	}

	/**
	 * @param weeklyFri
	 *          The weeklyFri to set.
	 */
	public void setWeeklyFri(boolean weeklyFri) {
		this.weeklyFri = weeklyFri;
	}

	/**
	 * @param weeklyMon
	 *          The weeklyMon to set.
	 */
	public void setWeeklyMon(boolean weeklyMon) {
		this.weeklyMon = weeklyMon;
	}

	/**
	 * @param weeklySat
	 *          The weeklySat to set.
	 */
	public void setWeeklySat(boolean weeklySat) {
		this.weeklySat = weeklySat;
	}

	/**
	 * @param weeklySun
	 *          The weeklySun to set.
	 */
	public void setWeeklySun(boolean weeklySun) {
		this.weeklySun = weeklySun;
	}

	/**
	 * @param weeklyThu
	 *          The weeklyThu to set.
	 */
	public void setWeeklyThu(boolean weeklyThu) {
		this.weeklyThu = weeklyThu;
	}

	/**
	 * @param weeklyTue
	 *          The weeklyTue to set.
	 */
	public void setWeeklyTue(boolean weeklyTue) {
		this.weeklyTue = weeklyTue;
	}

	/**
	 * @param weeklyWed
	 *          The weeklyWed to set.
	 */
	public void setWeeklyWed(boolean weeklyWed) {
		this.weeklyWed = weeklyWed;
	}

	/**
	 * @param yearlyDay
	 *          The yearlyDay to set.
	 */
	public void setYearlyDay(String yearlyDay) {
		this.yearlyDay = yearlyDay;
	}

	/**
	 * @param yearlyMonth
	 *          The yearlyMonth to set.
	 */
	public void setYearlyMonth(String yearlyMonth) {
		this.yearlyMonth = yearlyMonth;
	}

	public List<String> getTnas() {


		return tnas;
	}

	public void setTnas(List<String> tnas) {
		Set<String> sortedTNAs = new TreeSet<String>(new Comparator<String>() {
			public int compare(String tna1, String tna2) {
				return tna1.toLowerCase().compareTo(tna2.toLowerCase());
			}
		});
		sortedTNAs.addAll(tnas);
		this.tnas = new ArrayList<String>(sortedTNAs);
	}

}
