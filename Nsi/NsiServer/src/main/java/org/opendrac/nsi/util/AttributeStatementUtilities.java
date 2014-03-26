/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.util;

import java.util.ArrayList;
import java.util.List;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;


/**
 *
 * @author hacksaw
 */
public class AttributeStatementUtilities {
	private static final String BASIC_NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";

    public static String getBasicAttributeValue(
                String attributeName,
                AttributeStatementType statement
            ) throws IllegalArgumentException {

        // Handle common error by returning null.
        if (statement == null) {
            return null;
        }

		// Loop through each attribute statement provided.
		for (Object entry : statement.getAttributeOrEncryptedAttribute()) {
			// At the moment we only support entries of AttributeType.
			if (entry instanceof AttributeType) {
				AttributeType attr = (AttributeType) entry;

				if (attr.getName().equalsIgnoreCase(attributeName)) {
					// Check the namespace of the attribute name.
					if (!attr.getNameFormat().equalsIgnoreCase(BASIC_NAME_FORMAT)) {
						throw new IllegalArgumentException(
                                "Did find supported namespace for attributeName"
                                + attr.getName() + " + " + attr.getNameFormat());
					}

					// We only want a single value so fail multiple.
					return getAttributeValueAsString(attributeName,
					    attr.getAttributeValue());
				}
			}
		}

        // No matches so return null.
        return null;
    }

	public static String getAttributeValueAsString(String attributeName, List<Object> values) throws IllegalArgumentException {
		if (values.size() < 1) {
			throw new IllegalArgumentException("Missing AttributeValue for " + attributeName);
		}
		else if (values.size() > 1) {
			throw new IllegalArgumentException("Multiple AttributeValues for " + attributeName);
		}

		for (Object value : values) {
			if (value instanceof String) {
				return (String) value;
			}
		}

		throw new IllegalArgumentException("<String> object not provided for " + attributeName);
	}

	public List<String> getAttributeValuesAsString(
                String attributeName,
                List<Object> values
            ) throws IllegalArgumentException {

		List<String> results = new ArrayList<String>();

		for (Object value : values) {
			if (value instanceof String) {
				results.add((String) value);
			}
			else {
                throw new IllegalArgumentException(value.getClass() + "object found when expecting String for " + attributeName);
			}
		}

		return results;
	}
}
