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

package com.nortel.appcore.app.drac.security;

import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;

/**
 * LoginTokenCache: When a user logs in they are assigned a LoginToken, here we
 * keep a cache of valid (login) users and the ability to map from a token to a
 * userDetails object.
 * 
 * @author pitman
 */
public enum LoginTokenCache {
	INSTANCE;
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private static class ExpiredUserAuditTask extends TimerTask {
	  private final Logger log = LoggerFactory.getLogger(getClass());
		@Override
		public void run() {
			try {
				LoginTokenCache.INSTANCE.auditTokenCache();
			}
			catch (Exception t) {
				log.error("Error: ", t);
			}
		}
	}

	private final Map<LoginToken, UserDetails> map = new ConcurrentHashMap<LoginToken, UserDetails>();
	private final long TIMER_INITIAL_DELAY = Long.getLong(
	    "login.token.cache.delay", 1000 * 60 * 5).longValue();
	private final long TIMER_PERIOD = Long.getLong("login.token.cache.period",
	    1000 * 60 * 60).longValue();

	private LoginTokenCache() {
		Timer t = new Timer("LoginTokenCache timer");
		t.schedule(new ExpiredUserAuditTask(), TIMER_INITIAL_DELAY, TIMER_PERIOD);
	}

	public void addLogin(LoginToken token, UserDetails u) throws Exception {
		map.put(token, u);
	}

	public UserDetails getUser(LoginToken token) throws Exception {
		return map.get(token);
	}

	public void logoutUser(LoginToken token) throws Exception {
		map.remove(token);
	}

	public void updateUserDetails(LoginToken token, UserDetails userDetails)
	    throws Exception {
		map.put(token, userDetails);
	}

	protected void auditTokenCache() throws Exception {
		log.debug("Scanning for expired users: Scanning " + map.size()
		    + " entries.");
		for (LoginToken token : map.keySet()) {
			
			/*
			 * We are using a concurrent hash map, this permits us to alter the map as
			 * we iterate over it, if we find an expired entry we'll end up removing
			 * it
			 */
			try {
				SecurityServer.INSTANCE.sessionValidate(token);
				// No exception? The token is valid and can remain in the cache
			}
			catch (Exception e) {
				/*
				 * Exception? If the session is invalid the sessionValidate method will
				 * remove the session from the cache and throw an exception... but we
				 * might also get an exception because something failed internally... if
				 * that happens we are out of luck.
				 */

			}
		}
	}
	
	/**
	 * Change the resource profiles in the user profiles of the users currently in this cache.
	 * @param changedProfile
	 */
	protected void updateUserProfilesInTokenCacheWithResources(ResourceGroupProfile changedProfile){
		String profileName = changedProfile.getName();
		Collection<UserDetails> userDetailList =  map.values();
		for(UserDetails details: userDetailList){
			for(ResourceGroupProfile profile: details.getUserPolicyProfile().getResourceGroupList()){
				if(profile.getName().equals(profileName)){
					profile.setResourceList(changedProfile.getResourceList());
				}
			}
		}
	}
}
