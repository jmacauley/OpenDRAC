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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server;

/**
 * Created on Aug 17, 2005
 * 
 * @author nguyentd
 */
public final class Candidate {

	private String errorCode;
	private String additionalErrorText;
	private String xmlResult;

	/**
	 * @return the additionalErrorText
	 */
	public String getAdditionalErrorText() {
		return additionalErrorText;
	}

	/**
	 * @return the errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * @return the xmlResult
	 */
	public String getXmlResult() {
		return xmlResult;
	}

	/**
	 * @param additionalErrorText
	 *          the additionalErrorText to set
	 */
	public void setAdditionalErrorText(String additionalErrorText) {
		this.additionalErrorText = additionalErrorText;
	}

	/**
	 * @param errorCode
	 *          the errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @param xmlResult
	 *          the xmlResult to set
	 */
	public void setXmlResult(String xmlResult) {
		this.xmlResult = xmlResult;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);

		sb.append("Candidate : ");
		sb.append(" errorCode :");
		sb.append(errorCode);
		sb.append(" additionalErrorText :");
		sb.append(additionalErrorText);
		sb.append(" xmlResult :");
		sb.append(xmlResult);

		return sb.toString();
	}
}
