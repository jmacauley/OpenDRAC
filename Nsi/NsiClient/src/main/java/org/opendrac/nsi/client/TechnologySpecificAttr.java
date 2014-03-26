/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.ogf.schemas.nsi._2011._10.connection.types.TechnologySpecificAttributesType;

/**
 *
 * @author hacksaw
 */
public class TechnologySpecificAttr {

    private static final String NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";

    public static TechnologySpecificAttributesType getTechnologySpecificAttr(String attributeName, String attributeValue) {

        AttributeStatementType ast = getBasicAttributeStatementType(attributeName, attributeValue);
        if (ast == null) {
            return null;
        }

        TechnologySpecificAttributesType tsat = new TechnologySpecificAttributesType();
        tsat.setGuaranteed(ast);
        return tsat;
    }

    public static AttributeStatementType getBasicAttributeStatementType(String attributeName, String attributeValue) {

        if (attributeName == null || attributeName.isEmpty()) {
            return null;
        }

        AttributeStatementType ass = new AttributeStatementType();
        AttributeType attr = new AttributeType();
        attr.setName(attributeName);
        attr.setNameFormat(NAME_FORMAT);

        if (attributeValue != null || !attributeValue.isEmpty()) {
            attr.getAttributeValue().add(attributeValue);
            ass.getAttributeOrEncryptedAttribute().add(attr);
        }

        return ass;
    }
}