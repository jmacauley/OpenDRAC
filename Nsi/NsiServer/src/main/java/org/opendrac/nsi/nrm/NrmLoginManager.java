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
package org.opendrac.nsi.nrm;

import java.util.Iterator;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.util.ExceptionCodes;

/**
 *
 * @author hacksaw
 */
public class NrmLoginManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String userName;
    private String password;
    private String billingGroup;
    private String endpointUserGroup;
    private String endpointResourceGroup;

    private LoginToken token = null;

    NsaConfigurationManager nsaManager = NsaConfigurationManager.getInstance();

    public NrmLoginManager(String userName, String password,
            String billingGroup, String endpointUserGroup,
            String endpointResourceGroup) {

        this.userName = userName;
        this.password = password;
        this.billingGroup = billingGroup;
        this.endpointUserGroup = endpointUserGroup;
        this.endpointResourceGroup = endpointResourceGroup;

    }

	private void login() throws ServiceException {

        logger.info("NrmLoginManager.login: userName=\"" + getUserName() +
                "\" password=\"" + getPassword() + "\"");

		/*
		 * Initialize the RequestHandler to OpenDRAC.  This can return
		 * successfully but we may not be connected to the NRB_PORT.
		 */
		RequestHandler rh = RequestHandler.INSTANCE;

		// Log the version of OpenDRAC to which we have connected.
        logger.info("NrmLoginManager.login: OpenDRAC version=" + rh.getVersion());

		/*
         * Attempt to autenticated with OpenDRAC using the supplied credentials.
         * OpenDRAC requires an IP address for each user so we can do a access
         * control check during login.  It also is required for authentication
         * logging requirements.  We will provide localhost since we are on
         * box.
         */
		try {
			// Login to OpenDRAC via the RequestHandler.
			token = rh.login(ClientLoginType.INTERNAL_LOGIN, getUserName(), getPassword().toCharArray(), "localhost", null, "123");

            logger.debug("NrmLoginManager.login: login successful for user=\""
                    + getToken().getUser() + "\" returned token=\""
                    + getToken().getStaticToken() + "\"");

			/*
             * TODO: We need to update last login time to avoid account
             * expiration if this account is not being used for Web GUI login.
             */

		} catch (Exception e) {
			logger.error("NrmLoginManager.login: OpenDRAC login failed", e);
			throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMException", "OpenDRAC login failed: " + e.getMessage());
		}

        // Dump out user membership data for debug.
		try {
			// Get the user's secuirty profile information.
			UserProfile userProfile = rh.getUserProfile(getToken(), getUserName());
			MembershipData membership = userProfile.getMembershipData();

			// Get the user group names for which this user is a member.
			Set<UserGroupName> userGroupNames =
					membership.getMemberUserGroupName();

			Iterator list = userGroupNames.iterator();
			while (list.hasNext()) {
				UserGroupName name = (UserGroupName) list.next();
				logger.info("NrmLoginManager.login: UserGroupName =\""
							+ name.toString() + "\"");
			}
		} catch (Exception e) {
			logger.error("NrmLoginManager.login: OpenDRAC getUserGroupProfileNames failed", e);
            throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMException", "OpenDRAC getUserGroupProfileNames failed: " + e.getMessage());
		}
	}

    public void logout() throws ServiceException {
		/*
		 * Initialize the RequestHandler to OpenDRAC.  This can return
		 * successfully but we may not be connected to the NRB_PORT.
		 */
		RequestHandler rh = RequestHandler.INSTANCE;

		try {
			// Login to OpenDRAC via the RequestHandler.
			rh.logout(getToken());

		} catch (Exception e) {
			logger.error("NrmLoginManager.logout: OpenDRAC logout failed", e);
			throw ExceptionCodes.buildProviderException(ExceptionCodes.INTERNAL_NRM_ERROR, "NRMException", "OpenDRAC logout failed: " + e.getMessage());
		}
    }

    /**
     * @return the token
     */
    public synchronized LoginToken getToken() throws ServiceException {
        RequestHandler rh = RequestHandler.INSTANCE;
        if (token != null) {
            try {
                // Login to OpenDRAC via the RequestHandler.
                rh.sessionValidate(token);
                logger.info("NrmLoginManager.getToken: login session is valid");
                return token;
            } catch (Exception e) {
                logger.info("NrmLoginManager.getToken: login token has expired so renewing");
                token = null;
            }
        }

        this.login();

        return token;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the billingGroup
     */
    public String getBillingGroup() {
        return billingGroup;
    }

    /**
     * @return the endpointUserGroup
     */
    public String getEndpointUserGroup() {
        return endpointUserGroup;
    }

    /**
     * @return the endpointResourceGroup
     */
    public String getEndpointResourceGroup() {
        return endpointResourceGroup;
    }
}
