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

package com.nortel.appcore.app.drac.common.utility;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Something that can hold the results of an operation (success, error, error
 * codes, details, exception, etc) and be sent back to the client over RMI.
 * 
 * @author pitman
 */
public final class ProvisioningResultHolder implements Serializable {
	private String outputData;
	private String errorCode;
	private String exceptionText;
	private String additionalErrorText;
	private Throwable exception;
	private static final long serialVersionUID = 1L;

	public String getAdditionalErrorText() {
		return additionalErrorText;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public Throwable getException() {
		return exception;
	}

	public String getExceptionText() {
		return exceptionText;
	}

	public String getOutputData() {
		return outputData;
	}

	public void setErrorData(String briefErrorCode, String displayErrorText,
	    String extraInfo, Throwable ex) {
		errorCode = briefErrorCode;
		exceptionText = displayErrorText;
		additionalErrorText = extraInfo;
		exception = ex;
	}

	public void setOutputData(String od) {
		outputData = od;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ProvisioningResultHolder [errorCode=");
		sb.append(errorCode);
		sb.append(", outputData=");
		sb.append(outputData);
		sb.append(", exceptionText=");
		sb.append(exceptionText);
		sb.append(", additionalErrorText=");
		sb.append(additionalErrorText);
		sb.append(", exception=");
		if (exception == null) {
			sb.append("null");
		}
		else {
			StringWriter sw = new StringWriter();
			exception.printStackTrace(new PrintWriter(sw));
			sb.append(sw.getBuffer());
		}
		sb.append("]");
		return sb.toString();
	}

}
