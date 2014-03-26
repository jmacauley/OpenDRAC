/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.security;

import static javax.persistence.GenerationType.*;
import static org.hibernate.annotations.LazyCollectionOption.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.hibernate.annotations.LazyCollection;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.opendrac.nsi.util.ExceptionCodes;
import org.opendrac.nsi.util.MessageDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 * @author annotated by robert
 */
@Entity
@Table(name = "NSI_SESSION_SECURITIES")
public class SessionSecurity {

	@Transient
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String GLOBAL_USERNAME = "globalUserName";
    private static final String GLOBAL_USERNAME_DEFAULT = "phineas@netherlight.net";

	private static final String USER_ROLE = "role";
    private static final String USER_ROLE_DEFAULT = "Custodian";

	private static final String BASIC_NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";

	// These we know about now which may expand in the future.
	@Column(name = "GLOBAL_USERNAME", unique = false, nullable = false)
	private String globalUserName = null;

	@ElementCollection
	@CollectionTable(name = "NSI_SESSION_SECURITY_USER_ROLES")
	@Column(name = "USER_ROLE", length = 255)
	@LazyCollection(FALSE)
	private List<String> userRole = null;

	// We keep the original statement for use in later messaging.
	@Column(name = "ATTRIBUTE_STATEMENT_TYPE", unique = false, nullable = true)
	private AttributeStatementType statement = null;

	// Id column, needed for persistence
	@SuppressWarnings("unused")
  @Id
	@GeneratedValue(strategy = TABLE, generator = "nsi_sequences")
	@TableGenerator(name = "nsi_sequences", table = "NSI_SEQUENCES", allocationSize = 1)
	@Column(name = "ID", unique = true, nullable = false)
	private int id;

    public void setDefaultSessionSecurity() {
        globalUserName = GLOBAL_USERNAME_DEFAULT;
        userRole = new ArrayList<String>();
        userRole.add(USER_ROLE_DEFAULT);
    }

	/**
	 * Extracts the requesterNSA from a ReservationType.
	 *
	 * @param reservation
	 * @return requesterNSA
	 * @throws ServiceException
	 */
	public void parseSessionSecurityAttr(AttributeStatementType statement) throws ServiceException {
		logger.debug("parseSessionSecurityAttr: Processing security attributes");

		if (statement == null) {
			logger.info("No security attributes provided");
			//throw ExceptionCodes.buildProviderException(
			    //ExceptionCodes.MISSINGPARAMETER, "sessionSecurityAttr", "<null>");
            setDefaultSessionSecurity();
            return;
		}

		// Save the statement.
		this.setStatement(statement);

		// Loop through each attribute statement provided.
		for (Object entry : statement.getAttributeOrEncryptedAttribute()) {
			logger.debug("Processing " + entry.getClass().getSimpleName());

			// At the moment we only support entries of AttributeType.
			if (entry instanceof AttributeType) {
				AttributeType attr = (AttributeType) entry;
				logger.debug("Processing AttributeType - name=" + attr.getName()
				    + ", format=" + attr.getNameFormat());

				if (attr.getName().equalsIgnoreCase(GLOBAL_USERNAME)) {
					// Check the namespace of the attribute name.
					if (!attr.getNameFormat().equalsIgnoreCase(BASIC_NAME_FORMAT)) {
						logger.info("Did not get supported namespace for attribute name"
						    + attr.getName() + " + " + attr.getNameFormat());
					}
					// We only want a single value so fail multiple.
					globalUserName = getAttributeValueAsString(GLOBAL_USERNAME,
					    attr.getAttributeValue());
				}
				else if (attr.getName().equalsIgnoreCase(USER_ROLE)) {
					// Check the namespace of the attribute name.
					if (!attr.getNameFormat().equalsIgnoreCase(BASIC_NAME_FORMAT)) {
						logger.info("Did not get supported namespace for attribute name"
						    + attr.getName() + " + " + attr.getNameFormat());
					}

					// This one could be multi-valued.
					userRole = getAttributeValuesAsString(USER_ROLE,
					    attr.getAttributeValue());
				}
			}
			else {
				throw ExceptionCodes.buildProviderException(
				    ExceptionCodes.UNSUPPORTED_OPTION, "sessionSecurityAttr", entry
				        .getClass().getSimpleName());
			}
		}

		/**
		 * We should now valid that supplied credentials to verify the user, but for
		 * now we settle for them not being null.
		 */
		if (globalUserName == null) {
			throw ExceptionCodes.buildProviderException(
			    ExceptionCodes.MISSING_SECURITY, "globalUserName", "<null>");
		}
		else if (userRole == null) {
			throw ExceptionCodes.buildProviderException(
			    ExceptionCodes.MISSING_SECURITY, "role", "<null>");
		}
	}

	public String getAttributeValueAsString(String attributeName,
	    List<Object> values) throws ServiceException {
		if (values.size() < 1) {
			throw ExceptionCodes.buildProviderException(
			    ExceptionCodes.MISSING_PARAMETER, attributeName, "<null>");
		}
		else if (values.size() > 1) {
			throw ExceptionCodes.buildProviderException(
			    ExceptionCodes.MISSING_PARAMETER, attributeName,
			    "multiple values provided");
		}

		for (Object value : values) {
			if (value instanceof String) {
				return (String) value;
			}
		}

		throw ExceptionCodes.buildProviderException(
		    ExceptionCodes.MISSING_PARAMETER, attributeName,
		    "<String> object not provided");
	}

	public List<String> getAttributeValuesAsString(String attributeName,
	    List<Object> values) throws ServiceException {
		List<String> results = new ArrayList<String>();

		for (Object value : values) {
			if (value instanceof String) {
				results.add((String) value);
			}
			else {
				logger.info("Skilling unsupported attribute value type"
				    + value.getClass());
			}
		}

		return results;
	}

	/**
	 * @return the statement
	 */
	public AttributeStatementType getStatement() {
		return statement;
	}

	/**
	 * @param statement
	 *          the statement to set
	 */
	public void setStatement(AttributeStatementType statement) {
		this.statement = statement;
	}

	public String getGlobalUserName() {
		return globalUserName;
	}

	public void setGlobalUserName(String globalUserName) {
		this.globalUserName = globalUserName;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("{ ");
		result.append(GLOBAL_USERNAME);
		result.append("=");
		result.append(globalUserName);
		result.append(", ");
		result.append(USER_ROLE);
		result.append("= { ");
		for (String role : userRole) {
			result.append(role);
			result.append(", ");
		}
		result.append("}");

		if (statement != null) {
			result.append(", statement=");
			result.append(MessageDump.dump(AttributeStatementType.class, statement));
		}
		result.append(" }");

		return result.toString();
	}
}
