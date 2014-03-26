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

package com.nortel.appcore.app.drac.security.authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InvalidAttemptCache {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private final HashMap<String, InvalidAttemptsData> attemptsPerUserCache = new HashMap<String, InvalidAttemptsData>();
  private final HashMap<String, InvalidAttemptsData> attemptsPerIPCache = new HashMap<String, InvalidAttemptsData>();
  private final HashMap<String, InvalidAttemptsData> attemptsPerSessionIDCache = new HashMap<String, InvalidAttemptsData>();
  
  private final class InvalidAttemptsData {
    private int numberOfInvalidAttempt;
    private Date lastInvalidAttempt = null;
    private SortedSet<Date> attemptsDates = new TreeSet<Date>();
    
    private InvalidAttemptsData() {
      this.numberOfInvalidAttempt = 0;
    }

    private int getNumberOfInvalidAttempt() {
      return numberOfInvalidAttempt;
    }

    private void incrementInvalidAttempt() {
      this.numberOfInvalidAttempt += 1;
      Date now = new Date();
      this.lastInvalidAttempt = now;
      addAttemptMoment(now);
    }

    private void clearNrOfInvalidAttempts() {
      this.numberOfInvalidAttempt = 0;
    }

    private void clearLastAtemptDate() {
    	this.lastInvalidAttempt = null;
    }
    
    private void clearAtemptDates() {
        this.attemptsDates = new TreeSet<Date>();
     }
    
    private Date getLastInvalidAttempt() {
    	return lastInvalidAttempt;
    }

    private SortedSet<Date> getAttemptsDates() {
    	return attemptsDates;
    }
	
	private void addAttemptMoment(Date moment){
		attemptsDates.add(moment);
	}
	
	private int getNrInvalidAttempstSince(Date since){
		int amount = 0;
		if(attemptsDates.size()>0){
			amount = attemptsDates.tailSet(since).size();
		}
		return amount;
	}
  }

  public void clearInvalidAttemptForUser(String userID) {
    InvalidAttemptsData invalid = attemptsPerUserCache.get(userID);
    if (invalid == null) {
      log.debug("New user: " + userID);
      invalid = new InvalidAttemptsData();
      attemptsPerUserCache.put(userID, invalid);
    }    
    invalid.clearNrOfInvalidAttempts();
    invalid.clearAtemptDates();
    invalid.clearLastAtemptDate();
  }

  public void clearInvalidAttemptForIP(String IP) {
    InvalidAttemptsData invalid = attemptsPerIPCache.get(IP);
    if (invalid == null) {
      log.debug("New IP: " + IP);
      invalid = new InvalidAttemptsData();
      attemptsPerIPCache.put(IP, invalid);
    }
    invalid.clearNrOfInvalidAttempts();
    invalid.clearAtemptDates();
    invalid.clearLastAtemptDate();    
  }
  
  public void clearInvalidAttemptForSession(String sessionId) {
    InvalidAttemptsData invalid = attemptsPerSessionIDCache.get(sessionId);
    if (invalid == null) {
      log.debug("New session: " + sessionId);
      invalid = new InvalidAttemptsData();
      attemptsPerSessionIDCache.put(sessionId, invalid);
    }
    invalid.clearNrOfInvalidAttempts();
    invalid.clearAtemptDates();
    invalid.clearLastAtemptDate();    
  }  
  
  public int getInvalidAttemptNumberForUser(String userID) {
    InvalidAttemptsData invalid = attemptsPerUserCache.get(userID);
    if (invalid == null) {
      return 0;
    }
    return invalid.getNumberOfInvalidAttempt();
  }
  
  public int getInvalidAttemptNumberForIP(String IP) {
    InvalidAttemptsData invalid = attemptsPerIPCache.get(IP);
    if (invalid == null) {
      return 0;
    }
    return invalid.getNumberOfInvalidAttempt();
  }
  
  public int getInvalidAttemptNumberForSession(String sessionID) {
    InvalidAttemptsData invalid = attemptsPerUserCache.get(sessionID);
    if (invalid == null) {
      return 0;
    }
    return invalid.getNumberOfInvalidAttempt();
  }  
  
  public boolean hasUserExisted(String userID) {
    return attemptsPerUserCache.get(userID) != null;
  }
  
  public boolean hasIPExisted(String userID) {
    return attemptsPerIPCache.get(userID) != null;
  }
  
  public boolean hasSessionIdExisted(String userID) {
    return attemptsPerSessionIDCache.get(userID) != null;
  }  
  
  public void incrementInvalidAttemptForUser(String userID) {
    InvalidAttemptsData invalid = attemptsPerUserCache.get(userID);
    if (invalid == null) {
      log.debug("New user: " + userID);
      invalid = new InvalidAttemptsData();
      attemptsPerUserCache.put(userID, invalid);
    }
    invalid.incrementInvalidAttempt();
    attemptsPerUserCache.put(userID, invalid);
  }
  
  public void incrementInvalidAttemptForIP(String IP) {
    InvalidAttemptsData invalid = attemptsPerIPCache.get(IP);
    if (invalid == null) {
      log.debug("New IP: " + IP);
      invalid = new InvalidAttemptsData();
      attemptsPerIPCache.put(IP, invalid);
    }
    invalid.incrementInvalidAttempt();
    attemptsPerIPCache.put(IP, invalid);
  }
  
  public void incrementInvalidAttemptForSessionId(String sessionID) {
    InvalidAttemptsData invalid = attemptsPerSessionIDCache.get(sessionID);
    if (invalid == null) {
      log.debug("New session: " + sessionID);
      invalid = new InvalidAttemptsData();
      attemptsPerSessionIDCache.put(sessionID, invalid);
    }
    invalid.incrementInvalidAttempt();
    attemptsPerSessionIDCache.put(sessionID, invalid);
  }

  public Date getDateLastInvalidAttemptForUser(String userID) {
	Date lastAttemptDate = null;
    InvalidAttemptsData attemptsData = attemptsPerUserCache.get(userID);
    if (attemptsData != null) {
    	lastAttemptDate = attemptsData.getLastInvalidAttempt(); 
    }
    return lastAttemptDate;
  }
  
  public Date getDateLastInvalidAttemptForSessionID(String sessionID) {
	Date lastAttemptDate = null;
    InvalidAttemptsData attemptsData = attemptsPerSessionIDCache.get(sessionID);
    if (attemptsData != null) {
    	lastAttemptDate = attemptsData.getLastInvalidAttempt(); 
    }
    return lastAttemptDate;
  }
  
  public Date getDateLastInvalidAttemptForIP(String IP) {
	Date lastAttemptDate = null;
    InvalidAttemptsData attemptsData = attemptsPerIPCache.get(IP);
    if (attemptsData != null) {
    	lastAttemptDate = attemptsData.getLastInvalidAttempt(); 
    }
    return lastAttemptDate;
  }
  
  public SortedSet<Date> getAttemptMomentsForUser(String userID){
	  SortedSet<Date>dates=null;
	  InvalidAttemptsData attemptsData = attemptsPerUserCache.get(userID);
	  if (attemptsData != null) {
		  dates = attemptsData.getAttemptsDates();
	  }
	  return dates;
  }
  
  public SortedSet<Date> getAttemptMomentsForSession(String sessionID){
	  SortedSet<Date>dates=null;
	  InvalidAttemptsData attemptsData = attemptsPerSessionIDCache.get(sessionID);
	  if (attemptsData != null) {
		  dates = attemptsData.getAttemptsDates();
	  }
	  return dates;
  }
  
  public SortedSet<Date> getAttemptMomentsForIP(String IP){
	  SortedSet<Date>dates=null;
	  InvalidAttemptsData attemptsData = attemptsPerIPCache.get(IP);
	  if (attemptsData != null) {
		  dates = attemptsData.getAttemptsDates();
	  }
	  return dates;
  }
  
  public int getNrInvalidAttempstForIPSince(String IP, Date refDate){
	  int amount = 0;
	  InvalidAttemptsData attemptsData = attemptsPerIPCache.get(IP);
	  if (attemptsData != null) {
		  amount = attemptsData.getNrInvalidAttempstSince(refDate);
	  }
	  return amount;
  }
  
  public int getNrInvalidAttempstForSessionSince(String sessionId, Date refDate){
	  int amount = 0;
	  InvalidAttemptsData attemptsData = attemptsPerSessionIDCache.get(sessionId);
	  if (attemptsData != null) {
		  amount = attemptsData.getNrInvalidAttempstSince(refDate);
	  }
	  return amount;	  
  }
  
  public int getNrInvalidAttempstForUserSince(String userId, Date refDate){
	  int amount = 0;
	  InvalidAttemptsData attemptsData = attemptsPerUserCache.get(userId);
	  if (attemptsData != null) {
		  amount = attemptsData.getNrInvalidAttempstSince(refDate);
	  }
	  return amount;	  
  }
  
  public Set<String> getIPsInCache(){
	  return attemptsPerIPCache.keySet();
  }
}
