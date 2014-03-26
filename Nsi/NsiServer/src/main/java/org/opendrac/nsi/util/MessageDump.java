/**
 * Copyright (c) 2011, SURFnet bv, The Netherlands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
 * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */

package org.opendrac.nsi.util;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link MessageDump} utility class to support conversion of JAXB
 * annotated java objects to an XML encoded string as per the NSI WSDL
 * specification.  Class is generic enough to be used for any JAXB annotated
 * java object not containing the {@link XmlRootElement} annotation.
 *
 * @author hacksaw
 */
public class MessageDump {

  private static final Logger logger = LoggerFactory.getLogger(MessageDump.class);

	/**
	 * Utility method to marshal a JAXB annotated java object to an XML string.
	 *
	 * @param messageClass	The class of the object to marshal.
	 * @param message		The object to marshal.
	 * @return				String containing the XML encoded object.
	 */
	public static String dump(Class<?> messageClass, Object message) {

		// Make sure we are given the correct input.
		if (messageClass == null || message == null) {
			return null;
		}

		// We will write the XML encoding into a string.
		StringWriter writer = new StringWriter();

		try {
			// We will use JAXB to marshal the java objects.
			final JAXBContext jaxbContext = JAXBContext.newInstance(messageClass);

			// We do not have @XmlRootElement annotations on the classes so
			// we need to manually create the JAXBElement.
			JAXBElement<?> element = new JAXBElement(new QName("uri", "local"), messageClass, message);

			// Marshal the object.
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbContext.createMarshaller().marshal(element, writer);
		} catch (Exception e) {
			// Something went wrong so get out of here.
			logger.error("MessageDump.dump: Error marshalling object " +
					messageClass.getName() + ": " + e.toString());
			return null;
		}

		// Return the XML string.
		return writer.toString();
	}
}
