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

package com.nortel.appcore.app.drac.server.ws.common;

import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.*;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.DEFAULT_SECURITY_NSPREFIX;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.SOAP_ENVELOPE_NS;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.SOAP_ENVELOPE_NS_PREFIX;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.SOAP_HEADER_USERNAME_PATH;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.WSS_NAMESPACE;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.xmlbeans.XmlCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;

public class ServiceUtilities {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	public class DracOMElement {
		/*
		 * Note on AXIOM parsing problems: The following is a workaround to an
		 * apparent bug in axiom parsing. We should be able to simply pass the
		 * original OMElement into the AXIOMXPath to evaluate the selectSingleNode.
		 * This was not working. There are discussions of this found on the net e.g.
		 * http://www.mail-archive.com/axis-user@ws.apache.org/msg21300.html. But,
		 * by wrapping the input OMElement in an OMDocument in this way, it appears
		 * to modify the OMElement (or its original xml stream?). This broke SoapUI
		 * client calls to our web service because SoapUI places namespace prefixes
		 * in all of the soap body elements. This, in turn, resulted in the second
		 * and all subsequent calls to getServiceNamespacePrefix to return null. As
		 * such, rather than adding the original OMElement to the OMDocument, add a
		 * clone.
		 */
		OMDocument omDocument = null;

		public DracOMElement(OMElement child) {
			OMFactory fac = OMAbstractFactory.getOMFactory();
			this.omDocument = fac.createOMDocument();
			// => adding a clone of the original OMElement
			this.omDocument.addChild(child.cloneOMElement());
		}

		public OMElement selectSingleNode(AXIOMXPath xpathExp) throws Exception {
			return (OMElement) xpathExp.selectSingleNode(omDocument);
		}
	}

	private static final String DEFAULT_SERVICE_NAMESPACE_PREFIX_TOKEN = "srv";

	private static final String DEFAULT_COMMON_DATA_TYPES_NAMESPACE_PREFIX_TOKEN = "common";

	public String convertMillisToISO8601FormattedString(long timeInMillis,
	    String timeZoneId) {
		if (timeZoneId == null) {
			log.warn("timeZone Id is null. Server's local time zone will be used in responses with date time values.");
			timeZoneId = TimeZone.getDefault().getID();
		}

		// 

		TimeZone tz = TimeZone.getTimeZone(timeZoneId);
		Calendar cal = Calendar.getInstance(tz);
		cal.setTimeInMillis(timeInMillis);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		df.setTimeZone(tz);

		// The following is a workaround for a bug in Java
		// (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4919632)
		//
		// Java creates the dateTime as "2007-03-20T15:00:00-0400"
		// (no ":" in the timezone portion). On the other hand the xml
		// presentation requires datetime to be "2007-03-20T15:00:00-04:00".
		// So, we need to massage the datetime to contain ":" in the
		// timezone section

		StringBuilder output = new StringBuilder();
		output.append(df.format(cal.getTimeInMillis()));
		// Locate timezone part of the datetime
		int strlen = output.length();
		if (strlen > 2) {
			output.insert(strlen - 2, ":");
		}

		// 
		return output.toString();
	}

	public long convertToMillis(OMElement timeNode) throws InvalidInputException {
		if (timeNode == null) {
			log.error("Input parameter is null");
			Object[] args = new Object[1];
			args[0] = CommonMessageConstants.DATE_TIME_PARAMETER_RETRIEVAL_FAILURE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
		}
		return new XmlCalendar(timeNode.getText().trim()).getTimeInMillis();
		// 
		// return timeInMillis.getTimeInMillis();
	}

	public AxisFault createDracFault(int errorCode, String errorMessage,
	    Throwable throwable) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace nsCommon = fac.createOMNamespace(COMMON_DATA_TYPES_NS, "tns");
		OMElement fault = fac.createOMElement("DracFault", nsCommon);
		OMElement errorId = fac.createOMElement("errorId", nsCommon, fault);
		OMElement errorMsg = fac.createOMElement("errorMsg", nsCommon, fault);
		errorId.setText(new Integer(errorCode).toString());
		errorMsg.setText(errorMessage);
		AxisFault axisFault = new AxisFault(
		    CommonMessageConstants.REQUESTED_OPERATION_FAILED, throwable);
		axisFault.setDetail(fault);
		return axisFault;
	}
	
	public void handleInvalidInput(String logMessage, String errorMessage, int errorNr) throws InvalidInputException{
		log.error(logMessage);
		Object[] args = new Object[1];
		args[0] = errorMessage;
		throw new InvalidInputException(errorNr, args);		
	}
	public AxisFault createDracFault(int errorCode, Throwable t) {
		return createDracFault(errorCode,
		    DracErrorConstants.getErrorMessage(null, errorCode, null), t);
	}

	public String getCertificateFromSOAPHeader(SOAPHeader header)
	    throws InvalidInputException {
		String nsPrefix = null;

		Iterator<?> it = header.getChildElements();
		while (it.hasNext()) {
			OMElement element = (OMElement) it.next();
			if (element.getLocalName().compareTo(
			    RequestResponseConstants.RAW_CREDENTIALS) == 0) {
				OMNamespace ns = element.findNamespace(COMMON_DATA_TYPES_NS, null);
				if (ns != null) {
					nsPrefix = ns.getPrefix();
					if (nsPrefix.equals("")) {
						nsPrefix = "xmlns";
					}
					// 

					// Dead code
					// if (nsPrefix == null) {
					// Object[] args = new Object[1];
					// args[0] =
					// CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
					// throw new InvalidInputException(
					// DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
					// }
				}
				else {
					Object[] args = new Object[1];
					args[0] = CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
					throw new InvalidInputException(
					    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
				}
				Iterator<?> it2 = element.getChildElements();
				while (it2.hasNext()) {
					OMElement element2 = (OMElement) it2.next();
					if (element2.getLocalName().compareTo(
					    RequestResponseConstants.RAW_CERTIFICATE) == 0) {
						element2.findNamespace(COMMON_DATA_TYPES_NS, null);
						if (ns != null) {
							nsPrefix = ns.getPrefix();
							if (nsPrefix.equals("")) {
								nsPrefix = "xmlns";
							}
							// 

							// Dead code
							// if (nsPrefix == null) {
							// Object[] args = new Object[1];
							// args[0] =
							// CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
							// throw new InvalidInputException(
							// DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
							// }
						}
						else {
							Object[] args = new Object[1];
							args[0] = CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
							throw new InvalidInputException(
							    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
						}
					}
				}
			}
		}

		OMElement selectedNode = null;
		DracOMElement dracOMElement = new DracOMElement(header);

		try {
			String temp = null;
			temp = this.replaceNamespacePrefix(SOAP_ENVELOPE_CERTIFICATE,
			    DEFAULT_COMMON_DATA_TYPES_NAMESPACE_PREFIX_TOKEN, nsPrefix);

			AXIOMXPath xpathExp = new AXIOMXPath(temp);
			if (nsPrefix != null) {
				xpathExp.addNamespace(nsPrefix, COMMON_DATA_TYPES_NS);
				xpathExp.addNamespace(SOAP_ENVELOPE_NS_PREFIX, SOAP_ENVELOPE_NS);
				xpathExp.addNamespace(SOAP_ADDRESSING_NS_PREFIX, SOAP_ADDRESSING_NS);
				// 
				selectedNode = dracOMElement.selectSingleNode(xpathExp);
				if (selectedNode != null) {
					// 
					return selectedNode.getText().trim();
				}

				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_CERTIFICATE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}

			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_CERTIFICATE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);

		}
		catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw new InvalidInputException(
			    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args, e);
		}
	}

	public String getClientIpAddress(MessageContext currMsgContext) {
		return (String) currMsgContext.getProperty(MessageContext.REMOTE_ADDR);
	}

	public OMElement getNode(OMElement element, String serviceNs,
	    String serviceNsPrefix, String xpath) throws InvalidInputException {
		// 
		// 

		
		String newXpath = null;
		String nameSpacePrefix = getServiceNamespacePrefix(element, serviceNs);
		// String nameSpacePrefix = serviceNsPrefix;
		String commonNsPrefix = getServiceNamespacePrefix(element,
		    COMMON_DATA_TYPES_NS);
		if (commonNsPrefix == null && nameSpacePrefix == null) {
			log.error("original ns prefixes are null");
			Object[] args = new Object[1];
			args[0] = CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
		}

		if (nameSpacePrefix != null) {
			// 
			newXpath = this.replaceNamespacePrefix(xpath,
			    DEFAULT_SERVICE_NAMESPACE_PREFIX_TOKEN, nameSpacePrefix);
			// 
		}
		if (commonNsPrefix != null) {
			// 
			newXpath = this.replaceNamespacePrefix(newXpath,
			    DEFAULT_COMMON_DATA_TYPES_NAMESPACE_PREFIX_TOKEN, commonNsPrefix);
			// 
		}

		xpath = newXpath;

		AXIOMXPath xpathExp = null;
		OMElement selectedNode = null;

		DracOMElement dracOMElement = new DracOMElement(element);

		try {
			xpathExp = new AXIOMXPath(xpath);

			if (nameSpacePrefix.equals(commonNsPrefix)) {
				if (serviceNs.equals(COMMON_DATA_TYPES_NS)) {
					// 
					xpathExp.addNamespace(commonNsPrefix, COMMON_DATA_TYPES_NS);
				}
				else {
					// 
					xpathExp.addNamespace(nameSpacePrefix, serviceNs);
				}
			}
			else {
				xpathExp.addNamespace(nameSpacePrefix, serviceNs);
				xpathExp.addNamespace(commonNsPrefix, COMMON_DATA_TYPES_NS);
			}
			// 

			selectedNode = dracOMElement.selectSingleNode(xpathExp);

			/*
			 * if (selectedNode != null) { log.debug("selected node = " +
			 * selectedNode.toString()); }
			 */
			return selectedNode;
		}
		catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw new InvalidInputException(
			    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args, e);
		}
	}

	public String getNsPrefixFromHeader(MessageContext msgContext,
	    String serviceNs) throws Exception {
		String nsPrefix = null;
		SOAPEnvelope envelope = msgContext.getEnvelope();
		if (envelope == null) {
			log.error("SOAP message envelope is null");
			throw new Exception("SOAP envelope is invalid");
		}
		// 
		SOAPHeader header = envelope.getHeader();
		if (header == null) {
			log.error("SOAP header couldn't be found");
			throw new Exception("SOAP header couldn't be found.");
		}
		// 
		OMElement element = header.getFirstElement();
		if (element == null) {
			log.error("UserId is not in the header");
			throw new Exception("UserId is not in the header");
		}
		OMNamespace ns = element.findNamespace(serviceNs, null);
		if (ns == null) {
			log.error("Invalid namespace in SOAP header");
			throw new Exception("Invalid namespace in SOAP header");
		}
		nsPrefix = ns.getPrefix();
		if (nsPrefix == null) {
			log.error("Header is invalid, no namespace prefix");
			throw new Exception("Header is invalid, no namespace prefix");
		}
		if (nsPrefix.equals("")) {
			nsPrefix = "xmlns";
		}
		return nsPrefix;
	}

	public String getServiceNamespacePrefix(OMElement omMsg, String serviceNs) {
		String nsPrefix = null;
		OMNamespace tempNs = omMsg.findNamespace(serviceNs, null);
		if (tempNs != null) {
			nsPrefix = tempNs.getPrefix();
		}
		if (nsPrefix != null) {
			if (nsPrefix.equals("")) {
				nsPrefix = "xmlns";
			}
		}
		return nsPrefix;
	}

	public SOAPHeader getSOAPHeader(MessageContext currMsgContext)
	    throws InvalidInputException {
		currMsgContext = MessageContext.getCurrentMessageContext();
		if (currMsgContext.isSOAP11() != true) {
			log.error("Unsupported SOAP version is used. SOAP11 must be used.\n");
			Object[] args = new Object[1];
			args[0] = CommonMessageConstants.UNSUPPORTED_SOAP_VERSION_IN_REQUEST_MESSAGE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
		}

		SOAPEnvelope envelope = currMsgContext.getEnvelope();
		if (envelope != null) {
			
		}
		else {
			log.error("SOAP envelop is missing.\n");
			Object[] args = new Object[1];
			args[0] = CommonMessageConstants.MISSING_SOAP_ENVELOPE_IN_REQUEST_MESSAGE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
		}
		SOAPHeader soapHeader = envelope.getHeader();
		if (soapHeader != null) {
			
		}
		else {
			log.error("SOAP header is missing.");
			Object[] args = new Object[1];
			args[0] = CommonMessageConstants.MISSING_SOAP_HEADER_IN_REQUEST_MESSAGE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
		}
		return soapHeader;
	}

	public String getUserIdFromSOAPHeader(SOAPHeader header)
	    throws InvalidInputException {
		String nsPrefix = null;

		Iterator<?> it = header.getChildElements();
		while (it.hasNext()) {
			OMElement element = (OMElement) it.next();
			if (element.getLocalName().compareTo(
			    RequestResponseConstants.RAW_CREDENTIALS) == 0) {
				OMNamespace ns = element.findNamespace(COMMON_DATA_TYPES_NS, null);
				if (ns != null) {
					nsPrefix = ns.getPrefix();
					if (nsPrefix.equals("")) {
						nsPrefix = "xmlns";
					}
					// 

					// Dead code
					// if (nsPrefix == null) {
					// Object[] args = new Object[1];
					// args[0] =
					// CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
					// throw new InvalidInputException(
					// DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
					// }
				}
				else {
					Object[] args = new Object[1];
					args[0] = CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
					throw new InvalidInputException(
					    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
				}
				Iterator<?> it2 = element.getChildElements();
				while (it2.hasNext()) {
					OMElement element2 = (OMElement) it2.next();
					if (element2.getLocalName().compareTo(
					    RequestResponseConstants.RAW_USERID) == 0) {
						element2.findNamespace(COMMON_DATA_TYPES_NS, null);
						if (ns != null) {
							nsPrefix = ns.getPrefix();
							if (nsPrefix.equals("")) {
								nsPrefix = "xmlns";
							}
						}
						else {
							Object[] args = new Object[1];
							args[0] = CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
							throw new InvalidInputException(
							    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
						}
					}
				}
			}
		}

		OMElement selectedNode = null;
		DracOMElement dracOMElement = new DracOMElement(header);

		try {
			String temp = null;
			temp = this.replaceNamespacePrefix(SOAP_ENVELOPE_USER_ID,
			    DEFAULT_COMMON_DATA_TYPES_NAMESPACE_PREFIX_TOKEN, nsPrefix);

			AXIOMXPath xpathExp = new AXIOMXPath(temp);
			if (nsPrefix != null) {
				xpathExp.addNamespace(nsPrefix, COMMON_DATA_TYPES_NS);
				xpathExp.addNamespace(SOAP_ENVELOPE_NS_PREFIX, SOAP_ENVELOPE_NS);
				xpathExp.addNamespace(SOAP_ADDRESSING_NS_PREFIX, SOAP_ADDRESSING_NS);
				// 
				selectedNode = dracOMElement.selectSingleNode(xpathExp);
				if (selectedNode != null) {
					// 
					return selectedNode.getText().trim();
				}

				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_USERID;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}

			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_USERID;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);

		}
		catch (Exception e) {
			log.error("Exception = ", e);
			Object[] args = new Object[1];
			args[0] = e.getMessage();
			throw new InvalidInputException(
			    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args, e);
		}
	}

	public String getUserTimeZoneIdPreference(LoginToken token) {
		String userTimeZoneIdPreference = null;
		try {
			UserDetails userDetails = RequestHandler.INSTANCE.getUserDetails(
			    token);
			userTimeZoneIdPreference = new String(userDetails.getUserPolicyProfile()
			    .getUserProfile().getPreferences().getTimeZoneId());
			log.debug("User = " + userDetails.getUserID()
			    + "'s timezone preference is " + userTimeZoneIdPreference);
		}
		catch (Exception e) {
			userTimeZoneIdPreference = new String(TimeZone.getDefault().getID());
			log.warn(token
			    + "'s timezone preference couldn't be retrieved from his profile."
			    + " Server's local time zone will be used in responses with date time values.");
		}
		return userTimeZoneIdPreference;
	}

	public AxisFault inspectRequestHandlerException(RequestHandlerException e) {
		int errorCode = e.getErrorCode();
		String errorMessage = e.getMessage();
		Object[] args = new Object[1];
		args[0] = errorMessage;

		
		return createDracFault(DracErrorConstants.WS_OPERATION_FAILED,
		    DracErrorConstants.getErrorMessage(null,
		        DracErrorConstants.WS_OPERATION_FAILED, args), e);
	}

	public int[] intArrayListToIntArray(List<Integer> arrayList) {
		int[] intArray = new int[arrayList.size()];
		int i = 0;
		for (i = 0; i < arrayList.size(); i++) {
			intArray[i] = arrayList.get(i);
		}
		return intArray;
	}

	public String replaceNamespacePrefix(String xpath, String pattern,
	    String replace) {
		int s = 0;
		int e = 0;
		StringBuilder result = new StringBuilder();
		while ((e = xpath.indexOf(pattern, s)) >= 0) {
			result.append(xpath.substring(s, e));
			result.append(replace);
			s = e + pattern.length();
		}
		result.append(xpath.substring(s));
		return result.toString();
	}

	public String getUsernameFromSOAPHeader(MessageContext currMsgContext)
	    throws Exception {
		SOAPHeader header = getSOAPHeader(currMsgContext);
		String nsPrefix = null;
		Iterator<?> it = header.getChildElements();
		while (it.hasNext()) {
			OMElement element = (OMElement) it.next();
			OMNamespace ns = element.findNamespace(WSS_NAMESPACE, null);
			if (ns != null) {
				nsPrefix = ns.getPrefix();
				if (nsPrefix.equals("")) {
					nsPrefix = "xmlns";
				}
				

				// Dead code
				// if (nsPrefix == null) {
				// Object[] args = new Object[1];
				// args[0] =
				// CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
				// throw new InvalidInputException(
				// DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
				// }
			}
			else {
				Object[] args = new Object[1];
				args[0] = CommonMessageConstants.INVALID_NAMESPACE_IN_REQUEST_MESSAGE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE, args);
			}
		}

		DracOMElement dracOMElement = new DracOMElement(header);
		String xpath = this.replaceNamespacePrefix(SOAP_HEADER_USERNAME_PATH,
		    DEFAULT_SECURITY_NSPREFIX, nsPrefix);
		AXIOMXPath xpathExp = new AXIOMXPath(xpath);
		xpathExp.addNamespace(SOAP_ENVELOPE_NS_PREFIX, SOAP_ENVELOPE_NS);
		xpathExp.addNamespace(nsPrefix, WSS_NAMESPACE);
		OMElement selectedNode = dracOMElement.selectSingleNode(xpathExp);
		final String username = selectedNode.getText().trim();
		
    return username;
	}
}
