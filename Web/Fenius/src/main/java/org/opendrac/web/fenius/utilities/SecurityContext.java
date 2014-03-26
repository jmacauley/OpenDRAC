/**
 * Copyright (c) 2010, SURFnet bv, The Netherlands
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

package org.opendrac.web.fenius.utilities;

import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.opendrac.web.security.jaas.DracUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;


/**
 *
 * @author hacksaw
 */
public class SecurityContext {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Primary principal information.
    private String principalType;
    private String userName;
    private DracUserPrincipal userPrincipal;
    private LoginToken userToken;
    private List<UserGroupName> userGroups;

    // User session information.
    private String requestURI;
    private String remoteAddr;
    private String remoteHost;
    private String sessionId;

    public void setContext(WebServiceContext webServiceContext)
            throws IllegalArgumentException, GeneralSecurityException {

        // Validate the WebServiceContext.
        if (webServiceContext == null) {
            logger.error("SecurityContext.setContext: invalid WebServiceContext.");
            throw new IllegalArgumentException("Invalid WebServiceContext");
        }

        /*
         * The WebServiceContext holds authentication information relating to
         * the requesting user only if container-managed security is be used,
         * otherwise, the principal is empty.
         */
        Principal principal = webServiceContext.getUserPrincipal();

        // Check the principal.
        if (principal == null) {
            logger.error("SecurityContext.setContext: invalid principal.");
            throw new IllegalArgumentException("Invalid Principal");
        }

        // Get the HTTP servlet contexts for both incoming request and outgoing
        // response.
        HttpServletRequest httpRequest = (HttpServletRequest)
                webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);

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
                StringBuilder str = new StringBuilder("SecurityContext.setContext: supported Principal type=\"");
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
            logger.error("SecurityContext.setHttpContext: invalid Principal type \"" +
                    getPrincipalType() + "\"");
            throw new IllegalArgumentException("Invalid Principal type - " + getPrincipalType());

        }

        /*
         * We need the LoginToken for all DRAC operations so we might as well
         * break it out of the principal.
         */
        setUserToken(getUserPrincipal().getToken());
        setRequestURI(httpRequest.getRequestURI());
        setRemoteAddr(httpRequest.getRemoteAddr());
        setRemoteHost(httpRequest.getRemoteHost());
        setSessionId(httpRequest.getSession().getId());

        /*
         * Retrieve the user's user group information so we know which roles
         * the user is a member of.
         */

        // Get a remote reference to DRAC.
        RequestHandler rh = RequestHandler.INSTANCE;

        // Log the version of DRAC to which we have connected.
        if (logger.isDebugEnabled()) {
            logger.debug("SecurityContext.setContext: DRAC version=" +
                    rh.getVersion());
        }

        // Load user group information.
        try {
            // Get the user's secuirty profile information.
            UserProfile userProfile = rh.getUserProfile(getUserToken(), getUserName());
            MembershipData membership = userProfile.getMembershipData();


            // Get the user group names for which this user is a member.
            Set<UserGroupName> userGroupNames =
                    membership.getMemberUserGroupName();

            userGroups = new ArrayList<UserGroupName>();

            Iterator<UserGroupName> list = userGroupNames.iterator();
            while(list.hasNext()) {
                UserGroupName name = (UserGroupName) list.next();
                userGroups.add(name);

                if (logger.isDebugEnabled()) {
                    logger.debug("SecurityContext.setContext: adding user group =\""
                            + name.toString() + "\"");
                }
            }
        }
        catch (Exception e) {
            logger.error("SecurityContext.setContext: DRAC getUserGroupProfileNames failed", e);
            throw new GeneralSecurityException("DRAC login failed");
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
     * @param principalType the principalType to set
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
     * @param userName the userName to set
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
     * @param userPrincipal the userPrincipal to set
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
     * @param userToken the userToken to set
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
     * @param requestURI the requestURI to set
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
     * @param remoteAddr the remoteAddr to set
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
     * @param remoteHost the remoteHost to set
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
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isUserInRole(String role) {
        logger.debug("SecurityContext.isUserInRole: " + role);
        UserGroupName userGroup = new UserGroupName(role);
        return(userGroups.contains(userGroup));
    }

    @Override
    public String toString() {
        // Build the string.
        StringBuilder str = new StringBuilder("SecurityContext = [ userName=\"");
        str.append(getUserName());
        str.append("\", LoginToken=\"");
        str.append(getUserToken().getStaticToken());
        str.append("\", requestedURI=");
        str.append(getRequestURI());
        str.append(", remoteAddr=");
        str.append(getRemoteAddr());
        str.append(", remoteHost=");
        str.append(getRemoteHost());
        str.append(", sessionId=");
        str.append(getSessionId());
        str.append("]");
        return(str.toString());
    }
}
