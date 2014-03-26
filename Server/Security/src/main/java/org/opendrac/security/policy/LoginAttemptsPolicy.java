package org.opendrac.security.policy;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.nortel.appcore.app.drac.security.authentication.InvalidAttemptCache;
import com.nortel.appcore.app.drac.security.authentication.LocalAccountAuthenticatorHelper;

/**
 * Class that sets rules for locking a user, session or IP address,
 * after multiple bad logins. For locking an IP address or session
 * the system checks the amount of bad logins in a certain period of time:
 * e.g. the last 10 minutes
 */
public enum LoginAttemptsPolicy implements Serializable {
	INSTANCE;
	
	private static final long serialVersionUID = 5098945858283976418L;
	
	private static final int MAX_NR_FAILED_LOGIN_FROM_IP = Integer.getInteger("org.opendrac.max.failed.login.ip", 30);
	private static final int MAX_NR_FAILED_LOGIN_IN_SESSION = Integer.getInteger("org.opendrac.max.failed.login.session", 20);
	private static final int NR_MILLIS_FAILED_LOGIN_PERIOD_IP = Integer.getInteger("org.opendrac.failed.login.period.ip", 600000);
	private static final int NR_MILLIS_FAILED_LOGIN_PERIOD_SESSION = Integer.getInteger("org.opendrac.failed.login.period.session",600000);
	private static final int NR_MILLIS_LOCKOUT_PERIOD_IP = Integer.getInteger("org.opendrac.lockout.period.ip",100000);
	private static final int NR_MILLIS_LOCKOUT_PERIOD_SESSION = Integer.getInteger("org.opendrac.lockout.period.session",10000);

	/**
	 * Check if too many bad logins from IP address in last n milliseconds
	 * @param IP
	 * @return
	 */
	private boolean isExceedingMaxAttemptsForIP(String IP) {
		InvalidAttemptCache attemptsCache = LocalAccountAuthenticatorHelper.getInvalidAttemptCache();
		Date now = new Date();
		Date then = new Date(now.getTime()-NR_MILLIS_FAILED_LOGIN_PERIOD_IP);			
		return attemptsCache.getNrInvalidAttempstForIPSince(IP, then) >= MAX_NR_FAILED_LOGIN_FROM_IP;
	}

	/**
	 * Check if too many bad logins in session in last n milliseconds
	 * @param sessionId
	 * @return
	 */
	private boolean isExceedingMaxAttemptsForSession(String sessionId) {
		InvalidAttemptCache attemptsCache = LocalAccountAuthenticatorHelper.getInvalidAttemptCache();
		Date now = new Date();
		Date then = new Date(now.getTime()-NR_MILLIS_FAILED_LOGIN_PERIOD_SESSION);
		return attemptsCache.getNrInvalidAttempstForSessionSince(sessionId, then) >= MAX_NR_FAILED_LOGIN_IN_SESSION;
	}
	
	
	/**
	 * Check if IP address is currently locked
	 * @param IP
	 * @return boolean
	 */	
	public boolean isInLockoutPeriodIP(String IP){
		boolean locked = false;		
		if(isExceedingMaxAttemptsForIP(IP)){
			Date now = new Date();
			InvalidAttemptCache attemptsCache = LocalAccountAuthenticatorHelper.getInvalidAttemptCache();			
			Date lastLoginDate = attemptsCache.getDateLastInvalidAttemptForIP(IP);
			if(lastLoginDate != null){
				long timeDif = now.getTime()-lastLoginDate.getTime();
				locked  = timeDif<NR_MILLIS_LOCKOUT_PERIOD_IP;
			}
		}
		return locked;
	}
	
	/**
	 * Check if session is currently locked
	 * @param sessionID
	 * @return boolean
	 */
	public boolean isInLockoutPeriodSession(String sessionID){
		boolean locked = false;		
		if(isExceedingMaxAttemptsForSession(sessionID)){
			Date now = new Date();
			InvalidAttemptCache attemptsCache = LocalAccountAuthenticatorHelper.getInvalidAttemptCache();			
			Date lastLoginDate = attemptsCache.getDateLastInvalidAttemptForSessionID(sessionID);	
			if(lastLoginDate != null){
				locked  = now.getTime()-lastLoginDate.getTime()<NR_MILLIS_LOCKOUT_PERIOD_SESSION;
			}
		}
		return locked;
	}	
	
	public Set<String> getIPsInLockout(){
		InvalidAttemptCache attemptsCache  = LocalAccountAuthenticatorHelper.getInvalidAttemptCache();		
		Set<String>IPsInLockout = new HashSet<String>();
		Iterator<String> cachedIPs =  attemptsCache.getIPsInCache().iterator();
		while(cachedIPs.hasNext()){
			String IP = cachedIPs.next();
			if(isExceedingMaxAttemptsForIP(IP)){
				IPsInLockout.add(IP);
			}
		}
		return IPsInLockout;
	}
	
	public void removeIPFromCache(String IP){
		InvalidAttemptCache attemptsCache  = LocalAccountAuthenticatorHelper.getInvalidAttemptCache();	
		attemptsCache.clearInvalidAttemptForIP(IP);
	}
}
