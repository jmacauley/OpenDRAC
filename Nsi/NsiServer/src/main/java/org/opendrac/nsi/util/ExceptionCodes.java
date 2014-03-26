/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.util;

import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;

/**
 *
 * @author hacksaw
 */
public class ExceptionCodes {
    public static final ExceptionType MISSING_PARAMETER              = new ExceptionType("SVC0001", "Invalid or missing parameter");
    public static final ExceptionType UNSUPPORTED_OPTION             = new ExceptionType("SVC0002", "Parameter provided contains an unsupported value which MUST be processed");
    public static final ExceptionType ALREADY_EXISTS                 = new ExceptionType("SVC0003", "Schedule already exists for connectionId");
    public static final ExceptionType DOES_NOT_EXIST                 = new ExceptionType("SVC0004", "Schedule does not exists for connectionId");
    public static final ExceptionType MISSING_SECURITY               = new ExceptionType("SVC0005", "Invalid or missing user credentials");
    public static final ExceptionType TOPOLOGY_RESOLUTION_STP        = new ExceptionType("SVC0006", "Could not resolve STP in Topology database");
    public static final ExceptionType TOPOLOGY_RESOLUTION_STP_NSA    = new ExceptionType("SVC0007", "Could not resolve STP to managing NSA");
    public static final ExceptionType PATH_COMPUTATION_NO_PATH       = new ExceptionType("SVC0008", "Path computation failed to resolve route for reservation");
    public static final ExceptionType INVALID_STATE                  = new ExceptionType("SVC0009", "Connection state machine is in invalid state for received message");
    public static final ExceptionType INTERNAL_ERROR                 = new ExceptionType("SVC0010", "An internal error has caused a message processing failure");
    public static final ExceptionType INTERNAL_NRM_ERROR             = new ExceptionType("SVC0011", "An internal NRM error has caused a message processing failure");
    public static final ExceptionType STP_ALREADY_IN_USE             = new ExceptionType("SVC0012", "Specified STP already in use");
    public static final ExceptionType BANDWIDTH_NOT_AVAILABLE        = new ExceptionType("SVC0013", "Insufficent bandwidth available for reservation");
    public static final ExceptionType VLANID_INTERCANGE_NOT_SUPPORTED = new ExceptionType("SVC0014", "VlanId interchange not supported for requested path");

    private static final String NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";

    public static org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException buildProviderException(ExceptionType type, String parameter, String value) {
        return ExceptionCodes.buildProviderException(type, type.getText(), parameter, value);
    }

    public static org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException buildProviderException(ExceptionType type, String message, String parameter, String value) {
        ServiceExceptionType faultInfo = new ServiceExceptionType();
        faultInfo.setErrorId(type.getErrorId());
        faultInfo.setText(type.getText());

        // Set the attribute values to be the failed parameter and its value.
        // We wrap the attribute type in an attribute statement.
        if (parameter != null) {
            AttributeType attr = new AttributeType();
            attr.setName(parameter);
            attr.setNameFormat(NAME_FORMAT);
            attr.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
            attr.getAttributeValue().add(value);

            AttributeStatementType attrStatement = new AttributeStatementType();
            attrStatement.getAttributeOrEncryptedAttribute().add(attr);

            faultInfo.setVariables(attrStatement);
        }

        // Return the new exception.
        return new org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException(message, faultInfo);
    }

    public static void addVariable(org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException exception, String parameter, String value) {
        if (parameter != null) {
            AttributeType attr = new AttributeType();
            attr.setName(parameter);
            attr.setNameFormat(NAME_FORMAT);
            attr.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
            attr.getAttributeValue().add(value);
            exception.getFaultInfo().getVariables().getAttributeOrEncryptedAttribute().add(attr);
        }
    }

    public static org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException buildRequesterException(ExceptionType type, String parameter, String value) {
        return ExceptionCodes.buildRequesterException(type, type.getText(), parameter, value);
    }

    public static org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException buildRequesterException(ExceptionType type, String message, String parameter, String value) {
        ServiceExceptionType faultInfo = new ServiceExceptionType();
        faultInfo.setErrorId(type.getErrorId());
        faultInfo.setText(type.getText());

        // Set the attribute values to be the failed parameter and its value.
        // We wrap the attribute type in an attribute statement.
        if (parameter != null) {
            AttributeType attr = new AttributeType();
            attr.setName(parameter);
            attr.setNameFormat(NAME_FORMAT);
            attr.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
            attr.getAttributeValue().add(value);

            AttributeStatementType attrStatement = new AttributeStatementType();
            attrStatement.getAttributeOrEncryptedAttribute().add(attr);

            faultInfo.setVariables(attrStatement);
        }

        // Return the new exception.
        return new org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException(message, faultInfo);
    }

    public static void addVariable(org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException exception, String parameter, String value) {
        if (parameter != null) {
            AttributeType attr = new AttributeType();
            attr.setName(parameter);
            attr.setNameFormat(NAME_FORMAT);
            attr.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
            attr.getAttributeValue().add(value);
            exception.getFaultInfo().getVariables().getAttributeOrEncryptedAttribute().add(attr);

        }
    }
}
