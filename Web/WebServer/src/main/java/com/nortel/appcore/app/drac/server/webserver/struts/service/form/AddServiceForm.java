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

package com.nortel.appcore.app.drac.server.webserver.struts.service.form;

import org.apache.struts.validator.ValidatorActionForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on 11-Sep-06
 */
public final class AddServiceForm extends ValidatorActionForm {
	private static final long serialVersionUID = -2619081692825129529L;
	private String schName = DracConstants.EMPTY_STRING;
	private String schId = DracConstants.EMPTY_STRING;
	private String rate = DracConstants.EMPTY_STRING;
	private String srcLayer = DracConstants.EMPTY_STRING;
	private String destLayer = DracConstants.EMPTY_STRING;
	private String srctna = DracConstants.EMPTY_STRING;
	private String desttna = DracConstants.EMPTY_STRING;
	private String startTime = DracConstants.EMPTY_STRING;
	private String startdate = DracConstants.EMPTY_STRING;
	private String endTime = DracConstants.EMPTY_STRING;
	private String enddate = DracConstants.EMPTY_STRING;
	private String srcGroup = DracConstants.EMPTY_STRING;
	private String destGroup = DracConstants.EMPTY_STRING;
	private String srlg = DracConstants.EMPTY_STRING;
	private String srsg = DracConstants.EMPTY_STRING;
	private String locale = DracConstants.LANGUAGE_EN;

	private boolean startNow = false;
	private boolean endNever = false;

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

	/**
	 * @return the desttna
	 */
	public String getDesttna() {
		return desttna;
	}

	/**
	 * @return the enddate
	 */
	public String getEnddate() {
		return enddate;
	}

	/**
	 * @return the endTime
	 */
	public String getEndTime() {
		return endTime;
	}

	public String getLocale() {
		return locale;
	}

	/**
	 * @return the rate
	 */
	public String getRate() {
		return rate;
	}

	/**
	 * @return the schId
	 */
	public String getSchId() {
		return schId;
	}

	/**
	 * @return the schName
	 */
	public String getSchName() {
		return schName;
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

	/**
	 * @return the srctna
	 */
	public String getSrctna() {
		return srctna;
	}

	public String getSrlg() {
		return srlg;
	}

	public String getSrsg() {
		return srsg;
	}

	/**
	 * @return the startdate
	 */
	public String getStartdate() {
		return startdate;
	}

	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @return the endNever
	 */
	public boolean isEndNever() {
		return endNever;
	}

	/**
	 * @return the startNow
	 */
	public boolean isStartNow() {
		return startNow;
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

	/**
	 * @param desttna
	 *          the desttna to set
	 */
	public void setDesttna(String desttna) {
		this.desttna = desttna;
	}

	/**
	 * @param enddate
	 *          the enddate to set
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
	 * @param endTime
	 *          the endTime to set
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @param rate
	 *          the rate to set
	 */
	public void setRate(String rate) {
		this.rate = rate;
	}

	/**
	 * @param schId
	 *          the schId to set
	 */
	public void setSchId(String schId) {
		this.schId = schId;
	}

	/**
	 * @param schName
	 *          the schName to set
	 */
	public void setSchName(String schName) {
		this.schName = schName;
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

	/**
	 * @param srctna
	 *          the srctna to set
	 */
	public void setSrctna(String srctna) {
		this.srctna = srctna;
	}

	public void setSrlg(String srlg) {
		this.srlg = srlg;
	}

	public void setSrsg(String srsg) {
		this.srsg = srsg;
	}

	/**
	 * @param startdate
	 *          the startdate to set
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
	 * @param startTime
	 *          the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

}
