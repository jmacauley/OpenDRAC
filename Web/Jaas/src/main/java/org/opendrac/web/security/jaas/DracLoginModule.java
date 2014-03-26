/*
 * Copyright 2004-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package org.opendrac.web.security.jaas;

/**
 *
 * @author hacksaw
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;

/**
 * This {@link LoginModule} performs OpenDRAC-based authentication.
 *
 * <p> A supplied username and password is verified against the
 * corresponding user credentials stored in the OpenDRAC user profile service.
 * If successful then a new {@link DracUserPrincipal} is created with the
 * user's name and it is associated with the current {@link Subject}.
 * Such principals may be identified and granted management privileges in
 * the access control file for JMX remote management or in a Java security
 * policy.
 *
 * <p> This module recognizes the following <code>Configuration</code> options:
 * <dl>
 * <dt> <code>useFirstPass</code> </dt>
 * <dd> if <code>true</code>, this module retrieves the username and password
 *      from the module's shared state, using "javax.security.auth.login.name"
 *      and "javax.security.auth.login.password" as the respective keys. The
 *      retrieved values are used for authentication. If authentication fails,
 *      no attempt for a retry is made, and the failure is reported back to
 *      the calling application.</dd>
 *
 * <dt> <code>tryFirstPass</code> </dt>
 * <dd> if <code>true</code>, this module retrieves the username and password
 *      from the module's shared state, using "javax.security.auth.login.name"
 *       and "javax.security.auth.login.password" as the respective keys.  The
 *      retrieved values are used for authentication. If authentication fails,
 *      the module uses the CallbackHandler to retrieve a new username and
 *      password, and another attempt to authenticate is made. If the
 *      authentication fails, the failure is reported back to the calling
 *      application.</dd>
 *
 * <dt> <code>storePass</code> </dt>
 * <dd> if <code>true</code>, this module stores the username and password
 *      obtained from the CallbackHandler in the module's shared state, using
 *      "javax.security.auth.login.name" and
 *      "javax.security.auth.login.password" as the respective keys.  This is
 *      not performed if existing values already exist for the username and
 *      password in the shared state, or if authentication fails.</dd>
 *
 * <dt> <code>clearPass</code> </dt>
 * <dd> if <code>true</code>, this module clears the username and password
 *      stored in the module's shared state after both phases of authentication
 *      (login and commit) have completed.</dd>
 * </dl>
 */
public class DracLoginModule implements LoginModule {

	// Key to retrieve the stored username.
	private static final String USERNAME_KEY = "javax.security.auth.login.name";

	// Key to retrieve the stored password.
	private static final String PASSWORD_KEY = "javax.security.auth.login.password";

	// Get our slf4j logger reference.
	private final Logger logger = LoggerFactory.getLogger(getClass());

	// Configurable options.
	private boolean useFirstPass = false;
	private boolean tryFirstPass = false;
	private boolean storePass = false;
	private boolean clearPass = false;

	// Authentication status.
	private boolean succeeded = false;
	private boolean commitSucceeded = false;

	// Supplied username and password.
	private String username;
	private char[] password;
	private DracUserPrincipal user;
	private List<DracRolePrincipal> roles;

	// Initial state.
	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map<String, ?> sharedState;
	private Map options;

	/**
	 * Initialize this <code>LoginModule</code>.
	 *
	 * @param subject the <code>Subject</code> to be authenticated.
	 * @param callbackHandler a <code>CallbackHandler</code> to acquire the
	 *                  user's name and password.
	 * @param sharedState shared <code>LoginModule</code> state.
	 * @param options options specified in the login
	 *                  <code>Configuration</code> for this particular
	 *                  <code>LoginModule</code>.
	 */
	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState,
			Map<String, ?> options) {
		if (logger.isDebugEnabled()) {
			logger.debug("DracLoginModule.initialize: initializing...");
		}

		// Save the initalized environment.
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;

		// Initialize any configured options.
		tryFirstPass =
				"true".equalsIgnoreCase((String) options.get("tryFirstPass"));
		useFirstPass =
				"true".equalsIgnoreCase((String) options.get("useFirstPass"));
		storePass =
				"true".equalsIgnoreCase((String) options.get("storePass"));
		clearPass =
				"true".equalsIgnoreCase((String) options.get("clearPass"));
	}

	/**
	 * Begin user authentication (Authentication Phase 1).
	 *
	 * <p> Acquire the user's name and password and verify them against
	 * the corresponding credentials from OpenDRAC user profile.
	 *
	 * @return true always, since this <code>LoginModule</code>
	 *          should not be ignored.
	 * @exception FailedLoginException if the authentication fails.
	 * @exception LoginException if this <code>LoginModule</code>
	 *          is unable to perform the authentication.
	 */
	@Override
	public boolean login() throws LoginException {


		if (logger.isDebugEnabled()) {
			logger.debug("DracLoginModule.login: Entering.");
		}

		// Attempt the authentication.
		if (tryFirstPass) {

			if (logger.isDebugEnabled()) {
				logger.debug("DracLoginModule.login: (tryFirstPass) Attempting to use cache for authentication.");
			}

			try {
				// Attempt the authentication by getting the username and
				// password from shared state.
				attemptAuthentication(true);

				// Authentication succeeded.
				succeeded = true;
				if (logger.isDebugEnabled()) {
					logger.debug("DracLoginModule.login: (tryFirstPass) Authentication using cached password has succeeded");
				}
				return true;

			} catch (LoginException le) {
				// Authentication failed -- try again below by prompting.
				cleanState();
				logger.debug("DracLoginModule.login: (tryFirstPass) Authentication using cached password has failed");
			}

		} else if (useFirstPass) {

			if (logger.isDebugEnabled()) {
				logger.debug("DracLoginModule.login: (useFirstPass) Attempting to use cache for authentication.");
			}

			try {
				// Attempt the authentication by getting the username and
				// password from shared state.
				attemptAuthentication(true);

				// Authentication succeeded.
				succeeded = true;
				if (logger.isDebugEnabled()) {
					logger.debug("DracLoginModule.login: (useFirstPass) Authentication using cached password has succeeded.");
				}
				return true;

			} catch (LoginException le) {
				// Authentication failed.
				cleanState();
				logger.debug("DracLoginModule.login: (useFirstPass) Authentication using cached password has failed - giving up.");

				throw le;
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("DracLoginModule.login: Acquiring password.");
		}

		// Attempt the authentication using the supplied username and password.
		try {
			attemptAuthentication(false);

			// Authentication succeeded.
			succeeded = true;
			if (logger.isDebugEnabled()) {
				logger.debug("DracLoginModule.login: Authentication has succeeded.");
			}
			return true;

		} catch (LoginException le) {
			cleanState();
			logger.debug("DracLoginModule.login: Authentication has failed.");

			throw le;
		}
	}

	/**
	 * Complete user authentication (Authentication Phase 2).
	 *
	 * <p> This method is called if the LoginContext's
	 * overall authentication has succeeded
	 * (all the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
	 * LoginModules have succeeded).
	 *
	 * <p> If this LoginModule's own authentication attempt
	 * succeeded (checked by retrieving the private state saved by the
	 * <code>login</code> method), then this method associates a
	 * <code>DracUserPrincipal</code> with the <code>Subject</code> located
	 * in the <code>LoginModule</code>.  If this LoginModule's own
	 * authentication attempted failed, then this method removes
	 * any state that was originally saved.
	 *
	 * @exception LoginException if the commit fails
	 * @return true if this LoginModule's own login and commit
	 *          attempts succeeded, or false otherwise.
	 */
	@Override
	public boolean commit() throws LoginException {

		if (succeeded == false) {
			return false;
		} else {
			if (subject.isReadOnly()) {
				cleanState();
				throw new LoginException("Subject is read-only");
			}

			// Add Principal to the Subject.
			if (!subject.getPrincipals().contains(user)) {
				subject.getPrincipals().add(user);
			}

			// Add roles to the Subject.
			Iterator list = roles.iterator();
			while (list.hasNext()) {
				DracRolePrincipal role = (DracRolePrincipal) list.next();

				if (!subject.getPrincipals().contains(role)) {
					subject.getPrincipals().add(role);
					logger.debug("DracLoginModule.commit: adding role =\""
							+ role.getName() + "\"");
				} else {
					logger.debug("DracLoginModule.commit: role =\""
							+ role.getName()
							+ "\" already exists in principal list.");
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("DracLoginModule.commit: Authentication has completed successfully.");
			}
		}

		// In any case, clean out state.
		cleanState();
		commitSucceeded = true;
		return true;
	}

	/**
	 * Abort user authentication (Authentication Phase 2).
	 *
	 * <p> This method is called if the LoginContext's overall authentication
	 * failed (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
	 * LoginModules did not succeed).
	 *
	 * <p> If this LoginModule's own authentication attempt
	 * succeeded (checked by retrieving the private state saved by the
	 * <code>login</code> and <code>commit</code> methods),
	 * then this method cleans up any state that was originally saved.
	 *
	 * @exception LoginException if the abort fails.
	 * @return false if this LoginModule's own login and/or commit attempts
	 *          failed, and true otherwise.
	 */
	@Override
	public boolean abort() throws LoginException {

		if (logger.isDebugEnabled()) {
			logger.debug("DracLoginModule.abort: Authentication has not completed successfully");
		}

		if (succeeded == false) {
			return false;
		} else if (succeeded == true && commitSucceeded == false) {

			// Clean out state.
			succeeded = false;
			cleanState();
			user = null;
		} else {
			// Overall authentication succeeded and commit succeeded,
			// but someone else's commit failed.
			logout();
		}
		return true;
	}

	/**
	 * Logout a user.
	 *
	 * <p> This method removes the Principals that were added by the
	 * <code>commit</code> method.
	 *
	 * @exception LoginException if the logout fails.
	 * @return true in all cases since this <code>LoginModule</code>
	 *          should not be ignored.
	 */
	@Override
	public boolean logout() throws LoginException {

		if (subject.isReadOnly()) {
			cleanState();
			throw new LoginException("Subject is read-only");
		}

		// If we still have the user Principal then remove it from the subject
		// and clean up.
		if (user != null) {
			// @TODO: Can we physically log them out of OpenDRAC as well?
			logger.debug("DracLoginModule.logout: logging out user=\""
					+ user.getName() + "\"");
			subject.getPrincipals().remove(user);
			subject.getPrincipals().removeAll(roles);
			user = null;
			roles = null;
		} else {
			logger.debug("DracLoginModule.logout: user = null");
		}

		// Clean out state.
		cleanState();
		succeeded = false;
		commitSucceeded = false;

		logger.debug("DracLoginModule.logout: Subject has been logged out.");

		return true;
	}

	/**
	 * Attempt authentication
	 * This method validates the supplied user credentials against the OpenDRAC
	 * internal user account password used for the Web GUI.  We removed use of
     * the WSDL web services password.
	 *
	 * @param usePasswdFromSharedState a flag to tell this method whether
	 *          to retrieve the password from the sharedState.
	 */
	@SuppressWarnings("unchecked")  // sharedState used as Map<String,Object>
	private void attemptAuthentication(boolean usePasswdFromSharedState)
			throws LoginException {

		// Get the username and password.
		getUsernamePassword(usePasswdFromSharedState);

		// The username and password variables should now be populated so we
		// need to validate them with OpenDRAC.
		if (logger.isDebugEnabled()) {
			logger.debug("DracLoginModule.attemptAuthentication: username=\""
					+ username + "\" password=\""
					+ String.valueOf(password) + "\"");
		}


		/*
		 * Initialize the RequestHandler to OpenDRAC.  This can return
		 * successfully but we may not be connected to the NRB_PORT.
		 */
		RequestHandler rh = RequestHandler.INSTANCE;

		// Log the version of OpenDRAC to which we have connected.
		if (logger.isDebugEnabled()) {
			logger.debug("DracLoginModule.attemptAuthentication: OpenDRAC version="
					+ rh.getVersion());
		}

		// Attempt to autenticated with OpenDRAC using the supplied credentials.
		// OpenDRAC requires an IP address for each user so we can do a access
		// control check during loggin.  It also is required for authentication
		// logging requirements.  We can't get access to the remote client IP
		// address in this module so we will need to pass in a bogus address.
		LoginToken token = null;
		try {
			// Login to OpenDRAC via the RequestHandler.
			token = rh.login(ClientLoginType.INTERNAL_LOGIN,
					username, password, "localhost", null, "123");

			if (logger.isDebugEnabled()) {
				logger.debug("DracLoginModule.attemptAuthentication: login successful for user=\""
						+ token.getUser() + "\" returned token=\""
						+ token.getStaticToken() + "\"");
			}

			// TODO: We need to update last login time to avoid account expiration if
			// this account is not being used for Web GUI login.

		} catch (Exception e) {
			logger.error("DracLoginModule.attemptAuthentication: OpenDRAC login failed", e);
			throw new FailedLoginException("OpenDRAC login failed");
		}

		// Save the username and password in the shared state only if
		// authentication succeeded.
		if (storePass
				&& !sharedState.containsKey(USERNAME_KEY)
				&& !sharedState.containsKey(PASSWORD_KEY)) {
			((Map) sharedState).put(USERNAME_KEY, username);
			((Map) sharedState).put(PASSWORD_KEY, password);
		}

		// Create a new user principal.
		user = new DracUserPrincipal(token);

		// @TODO: Get the user's associated roles from OpenDRAC.  At the moment
		// we do not restrict access to web service interfaces based on access
		// control rules, but we should do this in the future.  Once
		// implemented we would populate the interfaces accessible to this
		// user as their roles.  The service code will need to check for the
		// user group names as roles that they want to have access to features.
		try {
			// Get the user's secuirty profile information.
			UserProfile userProfile = rh.getUserProfile(token, username);
			MembershipData membership = userProfile.getMembershipData();

			// Get the user group names for which this user is a member.
			Set<UserGroupName> userGroupNames =
					membership.getMemberUserGroupName();

			roles = new ArrayList<DracRolePrincipal>();

			Iterator list = userGroupNames.iterator();
			while (list.hasNext()) {
				UserGroupName name = (UserGroupName) list.next();
				DracRolePrincipal role = new DracRolePrincipal(name.toString());
				roles.add(role);

				if (logger.isDebugEnabled()) {
					logger.debug("DracLoginModule.attemptAuthentication: adding role =\""
							+ role.getName() + "\"");
				}
			}
		} catch (Exception e) {
			logger.error("DracLoginModule.attemptAuthentication: OpenDRAC getUserGroupProfileNames failed", e);
			throw new FailedLoginException("OpenDRAC login failed");
		}
	}

	/**
	 * Get the username and password.
	 * This method does not return any value.
	 * Instead, it sets global name and password variables.
	 *
	 * <p> Also note that this method will set the username and password
	 * values in the shared state in case subsequent LoginModules
	 * want to use them via use/tryFirstPass.
	 *
	 * @param usePasswdFromSharedState boolean that tells this method whether
	 *          to retrieve the password from the sharedState.
	 */
	private void getUsernamePassword(boolean usePasswdFromSharedState)
			throws LoginException {

		if (usePasswdFromSharedState) {
			// Use the password saved by the first module in the stack.
			username = (String) sharedState.get(USERNAME_KEY);
			password = (char[]) sharedState.get(PASSWORD_KEY);
			return;
		}

		// acquire username and password
		if (callbackHandler == null) {
			throw new LoginException("Error: no CallbackHandler available "
					+ "to garner authentication information from the user");
		}

		NameCallback nameCallback = new NameCallback("username");
		PasswordCallback passwordCallback = new PasswordCallback("password", false);

		try {
			callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
			username = nameCallback.getName();
			char[] tmpPassword = passwordCallback.getPassword();
			password = new char[tmpPassword.length];
			System.arraycopy(tmpPassword, 0,
					password, 0, tmpPassword.length);
			passwordCallback.clearPassword();

		} catch (IOException ioe) {
			LoginException le = new LoginException(ioe.toString());
			throw le;
		} catch (UnsupportedCallbackException uce) {
			LoginException le = new LoginException(
					"Error: " + uce.getCallback().toString()
					+ " not available to garner authentication "
					+ "information from the user");
			throw le;
		}
	}

	/**
	 * Clean out state because of a failed authentication attempt
	 */
	private void cleanState() {

		logger.debug("DracLoginModule.cleanState: " + username);

		username = null;
		if (password != null) {
			Arrays.fill(password, ' ');
			password = null;
		}

		if (clearPass) {
			sharedState.remove(USERNAME_KEY);
			sharedState.remove(PASSWORD_KEY);
		}
	}
}
