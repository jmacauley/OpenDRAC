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

package org.opendrac.drac.server.ws.handlers;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;

/**
 * Password Callback Handler is invoked by WS-Security framework (implemented
 * using Rampart) Its role is to only authenticate the user in the SOAP request
 * 
 */
public class PWCBHandler implements CallbackHandler {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final RequestHandler rh = RequestHandler.INSTANCE;

	@Override
	public void handle(Callback[] callbacks) throws IOException,
	    UnsupportedCallbackException {
		for (int i = 0; i < callbacks.length; i++) {
			WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[i];
			String userId = pwcb.getIdentifier();
			if (pwcb.getUsage() == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN) {
				try {
					rh.authenticate(ClientLoginType.WEB_SERVICE_LOGIN, userId, pwcb
					    .getPassword().toCharArray(), null, null);
					// no exception means passed
				}
				catch (Exception e) {
					log.error("Exception authenticating user", e);
					throw new UnsupportedCallbackException(callbacks[i],
					    "Authentication check failed");
				}
			}
			else {
				throw new UnsupportedCallbackException(callbacks[i],
				    "Unsupported token usage");
			}
		}
	}
}
