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

package org.opendrac.nsi.security;

import static javax.persistence.GenerationType.*;
import static org.hibernate.annotations.LazyCollectionOption.*;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.annotations.LazyCollection;
import org.opendrac.web.security.jaas.DracUserPrincipal;

import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;

/**
 * This {@link NsaSecurityContext} class models the user security context
 * provided in every NSI request message. At the moment this is fairly static
 * based on the Rio plug-fest requirements and will need to be extended as the
 * full security profile is defined.
 * 
 * @author hacksaw
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "NSI_NSA_SECURITY_CONTEXTS")
public class NsaSecurityContext implements Serializable {
	
	@Transient
	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	// Primary principal information.
	@Column(name = "PRINCIPAL_TYPE", unique = false, nullable = true, length = 255)
	private String principalType;

	@Column(name = "USERNAME", unique = false, nullable = true, length = 255)
	private String userName;

	@Column(name = "USER_PRINCIPAL", unique = false, nullable = true, length = 4096)
	private DracUserPrincipal userPrincipal;

	@Column(name = "LOGINTOKEN", unique = false, nullable = true, length = 255)
	private LoginToken userToken;

	@ElementCollection
	@CollectionTable(name = "NSI_NSA_SECURITY_CONTEXT_USER_GROUP_NAMES")
	@Column(name = "USER_ROLE", length = 255)
	@LazyCollection(FALSE)
	private List<UserGroupName> userGroups;

	// User session information.
	@Column(name = "REQUEST_URI", unique = false, nullable = true, length = 255)
	private String requestURI;

	@Column(name = "REMOTE_ADDR", unique = false, nullable = true, length = 255)
	private String remoteAddr;

	@Column(name = "REMOTE_HOST", unique = false, nullable = true, length = 255)
	private String remoteHost;

	@Column(name = "SESSION_ID", unique = false, nullable = true, length = 255)
	private String sessionId;
	
	// Id column, needed for persistence
	@SuppressWarnings("unused")
  @Id
	@GeneratedValue(strategy = TABLE, generator = "nsi_sequences")
	@TableGenerator(name = "nsi_sequences", table = "NSI_SEQUENCES", allocationSize = 1)
	@Column(name = "ID", unique = true, nullable = true)
	private int id;

	public void setContext(WebServiceContext webServiceContext)
	    throws IllegalArgumentException, GeneralSecurityException {

		// Validate the WebServiceContext.
		if (webServiceContext == null) {
			logger.error("SecurityContext.setContext: invalid WebServiceContext.");
			throw new IllegalArgumentException("Invalid WebServiceContext");
		}

		/*
		 * The WebServiceContext holds authentication information relating to the
		 * requesting user only if container-managed security is be used, otherwise,
		 * the principal is empty.
		 */
		Principal principal = webServiceContext.getUserPrincipal();

		// Check the principal.
		if (principal == null) {
			logger.error("SecurityContext.setContext: invalid principal.");
			throw new IllegalArgumentException("Invalid Principal");
		}

		// Get the HTTP servlet contexts for both incoming request and outgoing
		// response.
		HttpServletRequest httpRequest = (HttpServletRequest) webServiceContext
		    .getMessageContext().get(MessageContext.SERVLET_REQUEST);

		// Check the http context.
		if (httpRequest == null) {
			logger.error("SecurityContext.setContext: invalid HttpServletRequest.");
			throw new IllegalArgumentException("Invalid HttpServletRequest");
		}

		// Make sure we have a principal class type we can handle.
		setPrincipalType(principal.getClass().getCanonicalName());
		if (DracUserPrincipal.class.getName().equals(getPrincipalType())) {
			setUserName(principal.getName());
			setUserPrincipal((DracUserPrincipal) principal);

			// Dump out the authentication information.
			if (logger.isDebugEnabled()) {
				// Build the debug string.
				StringBuilder str = new StringBuilder(
				    "SecurityContext.setContext: supported Principal type=\"");
				str.append(getPrincipalType());
				str.append("\", user principle=\"");
				str.append(getUserPrincipal().toString());
				str.append("\", userName=\"");
				str.append(getUserName());
				str.append("\"");
				logger.debug(str.toString());
			}
		}
		else {
			logger.error("SecurityContext.setHttpContext: invalid Principal type \""
			    + getPrincipalType() + "\"");
			throw new IllegalArgumentException("Invalid Principal type - "
			    + getPrincipalType());

		}

		/*
		 * We need the LoginToken for all DRAC operations so we might as well break
		 * it out of the principal.
		 */
		setUserToken(getUserPrincipal().getToken());
		setRequestURI(httpRequest.getRequestURI());
		setRemoteAddr(httpRequest.getRemoteAddr());
		setRemoteHost(httpRequest.getRemoteHost());
		setSessionId(httpRequest.getSession().getId());

		/*
		 * Retrieve the user's user group information so we know which roles the
		 * user is a member of.
		 */

		// Get a remote reference to DRAC.
		RequestHandler rh = RequestHandler.INSTANCE;

		// Log the version of DRAC to which we have connected.
		if (logger.isDebugEnabled()) {
			logger.debug("SecurityContext.setContext: OpenDRAC version="
			    + rh.getVersion());
		}

		// Load user group information.
		try {
			// Get the user's secuirty profile information.
			UserProfile userProfile = rh
			    .getUserProfile(getUserToken(), getUserName());
			MembershipData membership = userProfile.getMembershipData();

			// Get the user group names for which this user is a member.
			Set<UserGroupName> userGroupNames = membership.getMemberUserGroupName();

			userGroups = new ArrayList<UserGroupName>();

			Iterator<UserGroupName> list = userGroupNames.iterator();
			while (list.hasNext()) {
				UserGroupName name = (UserGroupName) list.next();
				userGroups.add(name);

				if (logger.isDebugEnabled()) {
					logger.debug("SecurityContext.setContext: adding user group =\""
					    + name.toString() + "\"");
				}
			}
		}
		catch (Exception e) {
			logger
			    .error(
			        "SecurityContext.setContext: OpenDRAC getUserGroupProfileNames failed",
			        e);
			throw new GeneralSecurityException("OpenDRAC login failed");
		}

		// Dump out the session information.
		if (logger.isDebugEnabled()) {
			logger.debug(toString());
		}
	}

	/**
	 * @return the principalType
	 */
	public String getPrincipalType() {
		return principalType;
	}

	/**
	 * @param principalType
	 *          the principalType to set
	 */
	public void setPrincipalType(String principalType) {
		this.principalType = principalType;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	public List<UserGroupName> getUserGroups() {
		return userGroups;
	}

	/**
	 * @param userName
	 *          the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the userPrincipal
	 */
	public DracUserPrincipal getUserPrincipal() {
		return userPrincipal;
	}

	/**
	 * @param userPrincipal
	 *          the userPrincipal to set
	 */
	public void setUserPrincipal(DracUserPrincipal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}

	/**
	 * @return the userToken
	 */
	public LoginToken getUserToken() {
		return userToken;
	}

	/**
	 * @param userToken
	 *          the userToken to set
	 */
	public void setUserToken(LoginToken userToken) {
		this.userToken = userToken;
	}

	/**
	 * @return the requestURI
	 */
	public String getRequestURI() {
		return requestURI;
	}

	/**
	 * @param requestURI
	 *          the requestURI to set
	 */
	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	/**
	 * @return the remoteAddr
	 */
	public String getRemoteAddr() {
		return remoteAddr;
	}

	/**
	 * @param remoteAddr
	 *          the remoteAddr to set
	 */
	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	/**
	 * @return the remoteHost
	 */
	public String getRemoteHost() {
		return remoteHost;
	}

	/**
	 * @param remoteHost
	 *          the remoteHost to set
	 */
	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId
	 *          the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isUserInRole(String role) {
		logger.debug("SecurityContext.isUserInRole: " + role);
		UserGroupName userGroup = new UserGroupName(role);
		return (userGroups.contains(userGroup));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NsaSecurityContext [principalType=").append(principalType)
		    .append(", userName=").append(userName).append(", userPrincipal=")
		    .append(userPrincipal).append(", userToken=").append(userToken)
		    .append(", userGroups=").append(userGroups).append(", requestURI=")
		    .append(requestURI).append(", remoteAddr=").append(remoteAddr)
		    .append(", remoteHost=").append(remoteHost).append(", sessionId=")
		    .append(sessionId).append("]");
		return builder.toString();
	}
}
