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

package com.nortel.appcore.app.drac.server.ws.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;

/*
 * The userDetails cache stores a LoginToken object for all the users currently "authenticated" to use this
 * service. Entrys are keyed with userId+certificate+ipAddress.
 */
public class UserDetailsCache {
  private final Logger log = LoggerFactory.getLogger(getClass());
	public static class TokenHolder {
		private final UserHolder holder;
		private final LoginToken token;

		public TokenHolder(UserHolder h, LoginToken t) {
			holder = h;
			token = t;
		}

		public UserHolder getHolder() {
			return holder;
		}

		public LoginToken getLoginToken() {
			return token;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TokenHolder [holder=");
			builder.append(holder);
			builder.append(", token=");
			builder.append(token);
			builder.append("]");
			return builder.toString();
		}
	}

	public static class UserHolder {
		private final String userId;
		private final String password;
		private final String ipAddress;

		public UserHolder(String user, String pass, String ip) {
			userId = user;
			password = pass;
			ipAddress = ip;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public String getKey() {
			return userId + password + ipAddress;
		}

		public String getPassword() {
			return password;
		}

		public String getUserId() {
			return userId;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("UserHolder [userId=");
			builder.append(userId);
			builder.append(", ipAddress=");
			builder.append(ipAddress);
			builder.append("]");
			return builder.toString();
		}
	}

	private final Map<String, TokenHolder> cache = new ConcurrentHashMap<String, TokenHolder>();
	private final ServiceUtilities serviceUtil = new ServiceUtilities();
	private final String webServiceName;

	public UserDetailsCache(String serviceName) {
		webServiceName = serviceName;
	}

	public void addUser(UserHolder h, LoginToken token) {
		if (h == null || token == null) {
			log.error("Null parms, cannot add to cache");
			return;
		}

		cache.put(h.getKey(), new TokenHolder(h, token));
		log.debug("Added user = " + h.getUserId() + ", ipAddress = "
		    + h.getIpAddress() + " to " + webServiceName + " users cache.");
	}

	public TokenHolder findUser(UserHolder h) {
		return cache.get(h.getKey());
	}

	public void removeUser(UserHolder h) {
		cache.remove(h.getKey());
		log.debug("Removed user = " + h.getUserId() + ", ipAddress = "
		    + h.getIpAddress() + " from " + webServiceName + " users cache.");
	}

	@Override
	public String toString() {
		StringBuilder users = new StringBuilder();
		users.append(cache);
		return users.toString();
	}

	public void updateUser(UserHolder h, LoginToken token) {
		if (h == null || token == null) {
			log.error("Null parms, cannot add to cache");
			return;
		}

		cache.put(h.getKey(), new TokenHolder(h, token));
		log.debug("Updated user = " + h.getUserId() + ", ipAddress = "
		    + h.getIpAddress() + " in " + webServiceName + " users cache.");
	}

	public TokenHolder validateUser(MessageContext currMsgContext,
	    RequestHandler rh) throws WebServiceException {
		String userId = null;
		String certificate = null;
		SOAPHeader header = null;

		// extract userId from the message header and
		header = serviceUtil.getSOAPHeader(currMsgContext);
		if (header == null) {
			log.error("Missing SOAP Header");
			throw new InvalidInputException(
			    DracErrorConstants.WS_INVALID_REQUEST_MESSAGE,
			    new Object[] { CommonMessageConstants.MISSING_SOAP_HEADER_IN_REQUEST_MESSAGE });
		}
		userId = serviceUtil.getUserIdFromSOAPHeader(header);
		if (userId == null) {
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { RequestResponseConstants.RAW_USERID });
		}

		certificate = serviceUtil.getCertificateFromSOAPHeader(header);
		if (certificate == null) {
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
			    new Object[] { RequestResponseConstants.RAW_CERTIFICATE });
		}

		/*
		 * find it in our cache. If the user is not in our cache, it means the user
		 * didn't called the authenticate before calling this operation. Respond
		 * with fault.
		 */
		String ipAddress = serviceUtil.getClientIpAddress(currMsgContext);
		UserHolder h = new UserHolder(userId, certificate, ipAddress);
		TokenHolder tokenHolder = findUser(h);
		if (tokenHolder == null) {
			throw new NotAuthenticatedException(
			    DracErrorConstants.WS_USER_MUST_BE_AUTHENTICATED_FIRST,
			    new Object[] { userId });
		}

		// Session Validation
		try {
			rh.sessionValidate(tokenHolder.getLoginToken());
			return tokenHolder;
		}
		catch (Exception e) {
			removeUser(tokenHolder.getHolder());
			log.error("Exception = ", e);
			throw new InvalidSessionException(DracErrorConstants.WS_INVALID_SESSION,
			    new Object[] { e.getMessage() }, e);
		}
	}

}
