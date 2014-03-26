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

import static com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode.ERROR_DORMANT;
import static com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode.ERROR_PASSWORD_EXPIRED;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPasswordEvaluationException;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.errorhandling.DracRemoteException;
import com.nortel.appcore.app.drac.common.errorhandling.DracRemotePolicyException;
import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.security.authentication.types.AbstractCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.LocalAccountCredential;
import com.nortel.appcore.app.drac.common.security.authentication.types.PasswordErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes;
import com.nortel.appcore.app.drac.common.security.authentication.types.SessionCodes.SessionErrorCode;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.PasswordEvaluator;
import com.nortel.appcore.app.drac.common.security.policy.PolicyRequest;
import com.nortel.appcore.app.drac.common.security.policy.types.AccountStatus;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationAuditData;
import com.nortel.appcore.app.drac.common.security.policy.types.AuthenticationData;
import com.nortel.appcore.app.drac.common.security.policy.types.GlobalPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.GroupPolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.IPAddress;
import com.nortel.appcore.app.drac.common.security.policy.types.MembershipData;
import com.nortel.appcore.app.drac.common.security.policy.types.OrganizationData;
import com.nortel.appcore.app.drac.common.security.policy.types.PersonalData;
import com.nortel.appcore.app.drac.common.security.policy.types.Resource;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.ResourcePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationState;
import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationType;
import com.nortel.appcore.app.drac.common.types.SysMetricType;
import com.nortel.appcore.app.drac.database.dracdb.DbGlobalPolicy;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.dracdb.DbResourceGroupProfile;
import com.nortel.appcore.app.drac.database.dracdb.DbUser;
import com.nortel.appcore.app.drac.database.dracdb.DbUserGroupProfile;
import com.nortel.appcore.app.drac.database.helper.DbUtilityCommonUtility;
import com.nortel.appcore.app.drac.database.helper.ProfileValidator;
import com.nortel.appcore.app.drac.security.authentication.AuthenticationLogEntry;
import com.nortel.appcore.app.drac.security.authentication.LocalAccountAuthenticator;
import com.nortel.appcore.app.drac.security.policy.HierarchicalContainmentPolicy;
import com.nortel.appcore.app.drac.security.policy.PolicyEvaluator;
import com.nortel.appcore.app.drac.security.policy.PolicyLogEntry;

public enum SecurityServer {
  INSTANCE;

  private final Logger log = LoggerFactory.getLogger(getClass());
  // private final ASelectAuthenticator aSelectAuthenticator;
  private final LocalAccountAuthenticator localAccountAuthenticator;
  private final DbUser dbUser;
  private final DbGlobalPolicy dbGlobalPolicy;
  private final DbResourceGroupProfile dbResourceGroupProfile;
  private final DbUserGroupProfile dbUserGroupProfile;

  // See notes on the method. Default 'true':
  private static boolean ALLOW_DISPLAY_OF_SECURITY_ANCESTORS = Boolean
      .parseBoolean(System.getProperty("AllowDisplayOfSecurityAncestors",
          "false"));
  static {
    try {
      ALLOW_DISPLAY_OF_SECURITY_ANCESTORS = new File(
          "AllowDisplayOfSecurityAncestors").exists();
    }
    catch (Exception e) {
      //
    }
  }

  private SecurityServer() {
    // aSelectAuthenticator = new ASelectAuthenticator();
    localAccountAuthenticator = new LocalAccountAuthenticator();
    dbUser = DbUser.INSTANCE;
    dbGlobalPolicy = DbGlobalPolicy.INSTANCE;
    dbResourceGroupProfile = DbResourceGroupProfile.INSTANCE;
    dbUserGroupProfile = DbUserGroupProfile.INSTANCE;
  }

  public String auditUserProfileLinkages(UserDetails userDetails)
      throws Exception {
    return ProfileValidator.auditUserProfileLinkages();
  }

  public void authenticate(ClientLoginType loginType, String user,
      char[] password, String clientIp, String aSelectTicket) throws Exception {
    UserDetails userDetails = prepareAuthenticate(loginType, user, password,
        clientIp, aSelectTicket);

    if (userDetails.getErrorCode() != SessionCodes.SessionErrorCode.NO_ERROR) {
      log.error("Login failed " + userDetails.getErrorCode());
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
          new Object[] { userDetails.getErrorCode() });
    }
  }

  public LoginToken authorize(ClientLoginType loginType, String user,
      char[] password, String clientIp, String aSelectTicket) throws Exception {
    UserDetails userDetails = prepareAuthorization(loginType, user, password,
        clientIp, aSelectTicket);

    UserProfile prof;
    synchronized(LOCK) {
        prof = dbUser.getUserProfile(user);
    }
    
    checkAging(user, prof, userDetails);
    
    if (userDetails.getErrorCode() != SessionCodes.SessionErrorCode.NO_ERROR) {
      log.error("Login failed " + userDetails.getErrorCode());
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
          new Object[] { userDetails.getErrorCode() });
    }

    // Looks good so far, valid login
    if (loginType == ClientLoginType.ADMIN_CONSOLE_LOGIN) {
      // Must be an admin class user, or we fail the login.
      if (!userDetails.getUserPolicyProfile().getUserGroupType()
          .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)) {
        log.error("Login failed, required a valid admin class user");
        throw new NrbException(
            DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
            new Object[] { "Admin class user required for this application" });
      }
    }

    // This is a valid login, generate a login token and return
    LoginToken tok = new LoginToken(user);
    LoginTokenCache.INSTANCE.addLogin(tok, userDetails);
    AuthenticationLogEntry.createLogEntry(userDetails,
        LogKeyEnum.KEY_LOGIN_SUCCESS, new String[] { loginType.toString() });
    return tok;
  }

  public void changePassword(UserDetails userDetails, String oldpw, String newpw)
      throws Exception {
    if (!(userDetails.getCredential() instanceof LocalAccountCredential)) {
      log.error("SecurityServer::changePassword supported only for Local account types");
    }

    UserProfile userProfile = getUserProfile(userDetails,
        userDetails.getUserID());

    isAllowed(userDetails, new PolicyRequest(userProfile,
        PolicyRequest.CommandType.EDIT));

    // The following checks, associated with a change, used to be done client
    // side.
    // The only rule remaining on the client side is ensuring that the newpw and
    // retyped newpw are equal
    String actualCurrentPassword = userProfile.getAuthenticationData()
        .getInternalAccountData().getUserPassword();
    if (!actualCurrentPassword.equals(oldpw)) {
      throw new DracPasswordEvaluationException(
          new PasswordErrorCode[] { PasswordErrorCode.ERROR_OLDPW_INVALID });
    }

    if (oldpw.equals(newpw)) {
      throw new DracPasswordEvaluationException(
          new PasswordErrorCode[] { PasswordErrorCode.ERROR_PW_NOT_OLD });
    }

    validatePassword(userDetails, oldpw, newpw);

    LocalAccountCredential newCreds = new LocalAccountCredential(
        userDetails.getUserID(), newpw, userDetails.getLoginIPAddress());

    setupUserAuthenticationAccount(userDetails, userDetails.getUserID(),
        newCreds);

  }

  public void createResourceGroupProfile(UserDetails userDetails,
      ResourceGroupProfile profile) throws Exception {
    try {
      isAllowed(userDetails, new PolicyRequest(profile,
          PolicyRequest.CommandType.WRITE));
      dbResourceGroupProfile.createResourceGroupProfile(profile);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, profile.getName(),
          LogKeyEnum.KEY_CREATE_RESGROUP);
    }
    catch (Exception e) {
      PolicyLogEntry.createLogEntry(userDetails, profile.getName(),
          LogKeyEnum.KEY_CREATE_RESGROUP_FAILED);
      throw e;
    }
  }

  public void createUserGroupProfile(UserDetails userDetails,
      UserGroupProfile profile) throws Exception {
    try {
      // @TODO missing isAllowed!
      dbUserGroupProfile.createUserGroupProfile(profile);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, profile.getName().toString(),
          LogKeyEnum.KEY_CREATE_USERGROUP);
    }
    catch (Exception e) {
      log.error("createUserGroupProfile", e);
      PolicyLogEntry.createLogEntry(userDetails, profile.getName().toString(),
          LogKeyEnum.KEY_CREATE_USERGROUP_FAILED);
      throw e;
    }

  }

  public void createUserProfile(UserDetails userDetails, String userID,
      AbstractCredential credential, AccountStatus status) throws Exception {

    try {
      // @TODO missing isAllowed
      UserProfile userProfile = new UserProfile(userID, Calendar.getInstance(),
          Calendar.getInstance());
      userProfile.setupUserAuthenticationAccount(
          dbGlobalPolicy.getGlobalPolicy(), credential);
      userProfile.setAccountStatus(status);
      dbUser.createUserProfile(userProfile);

      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_CREATE_USER);
    }
    catch (Exception e) {
      log.error("createUserProfile", e);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_CREATE_USER_FAILED);
      throw e;
    }
  }

  public void deleteResourceGroupProfile(UserDetails userDetails,
      String resourceGroupName) throws Exception {
    try {
      // @TODO call isAllowed
      dbResourceGroupProfile.deleteResourceGroupProfile(resourceGroupName);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, resourceGroupName,
          LogKeyEnum.KEY_DELETE_RESGROUP);
    }
    catch (Exception e) {
      log.error("deleteResourceGroupProfile ", e);
      PolicyLogEntry.createLogEntry(userDetails, resourceGroupName,
          LogKeyEnum.KEY_DELETE_RESGROUP_FAILED);
      throw e;
    }
  }

  public void deleteUserGroupProfile(UserDetails userDetails, UserGroupName name)
      throws Exception {
    try {
      // @TODO call isAllowed
      dbUserGroupProfile.deleteUserGroupProfile(name);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, name.toString(),
          LogKeyEnum.KEY_DELETE_USERGROUP);
    }
    catch (Exception e) {
      log.error("deleteUserGroupProfile", e);
      PolicyLogEntry.createLogEntry(userDetails, name.toString(),
          LogKeyEnum.KEY_DELETE_USERGROUP_FAILED);
      throw e;
    }
  }

  public void deleteUserProfile(UserDetails userDetails, String userID)
      throws Exception {
    try {
      // @TODO call isAllowed
      dbUser.deleteUserProfile(userID);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_DELETE_USER);
    }
    catch (Exception e) {
      log.error("deleteUserProfile", e);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_DELETE_USER_FAILED);
      throw e;
    }
  }

  public int getConfirmationTimeout(UserDetails userDetails) throws Exception {
    return DbUtilityCommonUtility.INSTANCE.queryConfirmationTimeout();
  }

  public Resource getEndpointResource(UserDetails userDetails, String resourceID)
      throws Exception {
    return dbResourceGroupProfile.getEndpointResource(resourceID);
  }

  public GlobalPolicy getGlobalPolicy(UserDetails userDetails) throws Exception {
    return dbGlobalPolicy.getGlobalPolicy();
  }

  // See comments on getUserGroupNameLineage
  public List<String> getResourceGroupNameLineage(UserDetails userDetails,
      ResourceGroupProfile rgProfile, List<String> list) throws Exception {
    list.add(rgProfile.getName().toString());

    String createdByGroup = rgProfile.getMembership().getCreatedByGroupName();

    if (createdByGroup != null && createdByGroup.length() > 0) {
      try {
        ResourceGroupProfile parentProfile = dbResourceGroupProfile
            .getResourceGroupProfile(createdByGroup);
        if (!ALLOW_DISPLAY_OF_SECURITY_ANCESTORS) {
          isAllowed(userDetails, new PolicyRequest(parentProfile,
              PolicyRequest.CommandType.READ));
        }
        getResourceGroupNameLineage(userDetails, parentProfile, list);
      }
      catch (DracRemotePolicyException e) {
        // Given the retrieval implementation, don't log here.
      }
    }
    else {
      Collections.reverse(list);
    }

    return list;
  }

  public ResourceGroupProfile getResourceGroupProfile(UserDetails userDetails,
      String resourceGroupName) throws Exception {
    ResourceGroupProfile rgp = dbResourceGroupProfile
        .getResourceGroupProfile(resourceGroupName);
    // Check resource access privileges:
    isAllowed(userDetails, new PolicyRequest(rgp,
        PolicyRequest.CommandType.READ));
    return rgp;
  }

  public List<ResourceGroupProfile> getResourceGroupProfileList(
      UserDetails userDetail) throws Exception {
    List<ResourceGroupProfile> result = new ArrayList<ResourceGroupProfile>();

    List<ResourceGroupProfile> dbquery = dbResourceGroupProfile
        .getResourceGroupProfileList();
    for (ResourceGroupProfile rgProfile : dbquery) {
      try {
        isAllowed(userDetail, new PolicyRequest(rgProfile, PolicyRequest.CommandType.READ));
        result.add(rgProfile);
      }
      catch (DracRemotePolicyException e) {
        // Given the retrieve implementation, don't log here.
      }
    }

    return result;
  }

  public SysMetricType getSystemMetric(UserDetails userDetails)
      throws Exception {
    try {
      return new SysMetricType(DbUtilityCommonUtility.INSTANCE
          .queryScheduleOffset());
    }
    catch (Exception e) {
      log.error("Failed to query system metric", e);
      return new SysMetricType(0);
    }
  }

  /**
   * Map from a loginToken to a userDetails record or throw an exception if the
   * token is not valid (user not logged in, etc). This method is called by
   * NRBRemote as part of each and every RMI call to the NRB_PORT to validate the
   * token provided to determine if the token represents a valid user or not.
   * This method must run quickly as a result.
   */
  public UserDetails getUserDetails(LoginToken token) throws Exception {
    UserDetails userDetails = LoginTokenCache.INSTANCE.getUser(token);
    if (userDetails == null) {
      /*
       * If the caller has provided the static token this lookup will fail, but
       * it indicates that they are calling a method that does not require a
       * valid token such as isAlive() and we can keep the logs smaller by not
       * complaining about it
       */
      if (!LoginToken.getStaticToken().equals(token)) {
        log.error("Unable to find logged in user for token " + token);
      }
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_SESSION_VALIDATE_FAILED,
          new Object[] { "Login Token invalid " });
    }

    return userDetails;
  }

  // This method serves the security navigation on the web ui. e.g. List Users,
  // List User Groups, etc. The
  // web ui rendering of these operations displays a tree, whereby the user or
  // group in context will be
  // shown together with its child relationships and its parent relationships
  // ... right up to System Admin.
  //
  // Note that the user/group in context can be either the authenticated user,
  // or the user/group initially
  // satisfied by the search filter. Given this, there is question perhaps of
  // how to handle data access.
  // (1) Should an authenticated user be permitted to see the names of all its
  // parents?
  // (2) or, should visibility stop at the highest available read permission?
  //
  // (2) will be implemented
  public List<String> getUserGroupNameLineage(UserDetails userDetails,
      UserGroupProfile ugProfile, List<String> list) throws Exception {
    list.add(ugProfile.getName().toString());
    String createdByGroup = ugProfile.getMembership().getCreatedByGroupName();

    if (createdByGroup != null && createdByGroup.length() > 0) {
      try {
        UserGroupProfile parentProfile = dbUserGroupProfile
            .getUserGroupProfile(new UserGroupName(createdByGroup));
        if (!ALLOW_DISPLAY_OF_SECURITY_ANCESTORS) {
          isAllowed(userDetails, new PolicyRequest(parentProfile,
              PolicyRequest.CommandType.READ));
        }
        getUserGroupNameLineage(userDetails, parentProfile, list);
      }
      catch (DracRemotePolicyException e) {
        // Given the retrieval implementation, don't log here.
      }
    }
    else {
      Collections.reverse(list);
    }

    return list;

  }

  public UserGroupProfile getUserGroupProfile(UserDetails userDetails,
      UserGroupName name) throws Exception {
    UserGroupProfile p = dbUserGroupProfile.getUserGroupProfile(name);
    isAllowed(userDetails, new PolicyRequest(p, PolicyRequest.CommandType.READ));
    return p;
  }

  public List<UserGroupProfile> getUserGroupProfileList(UserDetails userDetail)
      throws Exception {
    // return dbUserGroupProfile.getUserGroupProfileList();

    List<UserGroupProfile> result = new ArrayList<UserGroupProfile>();

    List<UserGroupProfile> dbquery = dbUserGroupProfile
        .getUserGroupProfileList();
    for (UserGroupProfile group : dbquery) {
      try {
        isAllowed(userDetail, new PolicyRequest(group, PolicyRequest.CommandType.READ));
        result.add(group);
      }
      catch (DracRemotePolicyException e) {
        // Given the retrieve implementation, don't log here.
      }
    }

    return result;
  }

  // For the given user belonging to N groups (firstLevelMemberUGNList), build a
  // lineage for each
  // group membership back to SysAdminGroup.
  // Also see comments on getUserGroupNameLineage
  public List<List<String>> getUserNameLineage(UserDetails userDetails,
      UserProfile userProfile) throws Exception {

    List<List<String>> userGroupNameTree = new ArrayList<List<String>>();
    MembershipData userProfileMembershipData = userProfile.getMembershipData();
    Set<UserGroupName> firstLevelMemberUGNList = userProfileMembershipData
        .getMemberUserGroupName();

    for (UserGroupName firstLevelUGName : firstLevelMemberUGNList) {
      List<String> branch = new ArrayList<String>();
      String parentUserGroupName = firstLevelUGName.toString();

      while (parentUserGroupName != null && parentUserGroupName.length() > 0) {
        // branch.add(parentUserGroupName);

        try {
          UserGroupProfile ugProfile = getUserGroupProfile(userDetails,
              new UserGroupName(parentUserGroupName));
          branch.add(parentUserGroupName);
          parentUserGroupName = ugProfile.getMembership()
              .getCreatedByGroupName();
        }
        catch (DracRemotePolicyException e) {
          // Given the retrieve implementation, don't log here.
          break;
        }
      }

      userGroupNameTree.add(branch);
    }

    return userGroupNameTree;
  }

  private static final Object LOCK = DbUser.INSTANCE.getLock();

  public UserProfile getUserProfile(UserDetails userDetails, String userID)
      throws Exception {

    UserProfile prof;
    synchronized (LOCK) {
        prof = dbUser.getUserProfile(userID);
        if (prof != null) {
        isAllowed(userDetails, new PolicyRequest(prof,
            PolicyRequest.CommandType.READ));
        }
    }

    return prof;
  }

  public List<UserProfile> getUserProfileList(UserDetails userDetails)
      throws Exception {
    List<UserProfile> result = new ArrayList<UserProfile>();

    List<UserProfile> dbquery = dbUser.getUserProfileList();
    for (UserProfile uProfile : dbquery) {
      try {
        isAllowed(userDetails, new PolicyRequest(uProfile,
            PolicyRequest.CommandType.READ));
        result.add(uProfile);
      }
      catch (DracRemotePolicyException e) {
        // Given the retrieve implementation, don't log here.
      }
    }

    return result;
  }

  /**
     *
     */
  public boolean isAllowed(UserDetails userDetails, PolicyRequest request)
      throws Exception {
    try {
      return PolicyEvaluator.evaluate(userDetails, request);
    }
    catch (DracPolicyException e) {
      throw new DracRemotePolicyException(e);
    }
    catch (Exception e) {
      throw new DracRemoteException(
          DracErrorConstants.GENERAL_ERROR_INTERNAL_REASON,
          new Object[] { e.getMessage() }, e);
    }
  }

  public boolean isResourceGroupEditable(UserDetails userDetails,
      ResourceGroupProfile rgProfile) throws Exception {
    try {
      isAllowed(userDetails, new PolicyRequest(rgProfile,
          PolicyRequest.CommandType.EDIT));
      return true;
    }
    catch (DracRemotePolicyException e) {
      // Given the implementation, don't log here.
    }

    return false;
  }

  public boolean isUserEditable(UserDetails userDetails, UserProfile userProfile)
      throws Exception {
    try {
      isAllowed(userDetails, new PolicyRequest(userProfile,
          PolicyRequest.CommandType.EDIT));
      return true;
    }
    catch (DracRemotePolicyException e) {
      // Given the implementation, don't log here.
    }

    return false;
  }

  public boolean isUserGroupEditable(UserDetails userDetails,
      UserGroupProfile ugProfile) throws Exception {
    try {
      isAllowed(userDetails, new PolicyRequest(ugProfile,
          PolicyRequest.CommandType.EDIT));
      return true;
    }
    catch (DracRemotePolicyException e) {
      // Given the implementation, don't log here.
    }

    return false;
  }

  public LoginToken login(ClientLoginType loginType, String user,
      char[] password, String clientIp, String aSelectTicket, String sessionId) throws Exception {
    UserDetails userDetails = prepareLogin(loginType, user, password, clientIp,
        aSelectTicket, sessionId);

    if (userDetails.getErrorCode() != SessionCodes.SessionErrorCode.NO_ERROR) {
      log.error("Login failed " + userDetails.getErrorCode());
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
          new Object[] { userDetails.getErrorCode() });
    }

    // Looks good so far, valid login
    if (loginType == ClientLoginType.ADMIN_CONSOLE_LOGIN) {
      // Must be an admin class user, or we fail the login.
      if (!userDetails.getUserPolicyProfile().getUserGroupType()
          .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)) {
        log.error("Login failed, required a valid admin class user");
        throw new NrbException(
            DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
            new Object[] { "Admin class user required for this application" });
      }
    }

    // This is a valid login, generate a login token and return
    LoginToken tok = new LoginToken(user);
    LoginTokenCache.INSTANCE.addLogin(tok, userDetails);
    AuthenticationLogEntry.createLogEntry(userDetails,
        LogKeyEnum.KEY_LOGIN_SUCCESS, new String[] { loginType.toString() });
    return tok;
  }

  public void logout(LoginToken token) throws Exception {
    UserDetails userDetails = getUserDetails(token);
    try {
      LoginTokenCache.INSTANCE.logoutUser(token);
      userDetails.setErrorCode(SessionErrorCode.NO_ERROR);

      AbstractCredential credential = userDetails.getCredential();
      if (credential == null) {
        throw new Exception("Invalid credential: null");
      }

      // if
      // (credential.getAuthenticateMethod().equals(AbstractCredential.AuthenticateMethod.A_SELECT))
      // {
      // aSelectAuthenticator.logout(userDetails);
      // }
      // else
      if (credential.getAuthenticateMethod().equals(
          AbstractCredential.AuthenticateMethod.LOCAL_ACCOUNT)
          || credential.getAuthenticateMethod().equals(
              AbstractCredential.AuthenticateMethod.WEB_SERVICES)) {
        localAccountAuthenticator.logout(userDetails);
      }
      else {
        AuthenticationLogEntry.createLogEntry(userDetails,
            LogKeyEnum.KEY_LOGOUT_FAILED, new String[] { "" });
        return;
      }

      AuthenticationLogEntry.createLogEntry(userDetails,
          LogKeyEnum.KEY_LOGOUT_SUCCESS);
    }
    catch (Exception e) {
      log.error("logout", e);
      AuthenticationLogEntry.createLogEntry(userDetails,
          LogKeyEnum.KEY_LOGOUT_FAILED, new String[] { e.getMessage() });
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGOUT_FAILED,
          new Object[] { e.getMessage() }, e);
    }
  }

  public UserDetails prepareAuthenticate(ClientLoginType loginType,
      String user, char[] password, String clientIp, String aSelectTicket)
      throws Exception {
    if (loginType == null || user == null || password == null) {
      log.error("Cannot pass null parms into login method");
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
          new Object[] { "null parms" });
    }

    UserProfile prof;
    synchronized (LOCK) {
        prof = dbUser.getUserProfile(user);
    }

    AuthenticationType authType = AuthenticationType.INTERNAL;
    if (prof != null) {
        authType = prof.getAuthenticationData().getAuthenticationType();
    }


    UserDetails userDetails = null;
    IPAddress ip = IPAddress.fromString(clientIp);
    if (ip == null) {
      ip = new IPAddress("localhost", null);
    }

    // if (authType.equals(AuthenticationType.A_SELECT))
    // {
    // userDetails = new UserDetails(loginType.toString(), user, new
    // ASelectCredential(user, new
    // String(password),
    // aSelectTicket, ip));
    // userDetails = aSelectAuthenticator.login(userDetails);
    // }
    // else
    if (authType.equals(AuthenticationType.INTERNAL)) {
      userDetails = new UserDetails(loginType.toString(), user,
          new LocalAccountCredential(user, new String(password), ip));
      userDetails = localAccountAuthenticator.authenticate(loginType,
          userDetails);
    }
    else {
      log.error("Login failed bad authType");
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
          new Object[] { authType });
    }

    return userDetails;
  }

  public UserDetails prepareAuthorization(ClientLoginType loginType,
      String user, char[] password, String clientIp, String aSelectTicket)
      throws Exception {
    if (loginType == null || user == null) {
      log.error("Cannot pass null parms into login method");
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
          new Object[] { "null parms" });
    }

    UserProfile prof;
    synchronized(LOCK) {
        prof = dbUser.getUserProfile(user);
    }

    AuthenticationType authType = AuthenticationType.INTERNAL;
    if (prof != null && !loginType.equals(ClientLoginType.INTERNAL_LOGIN)) {
      authType = prof.getAuthenticationData().getAuthenticationType();
    }

    UserDetails userDetails = null;
    IPAddress ip = IPAddress.fromString(clientIp);
    if (ip == null) {
      ip = new IPAddress("localhost", null);
    }

    // if (authType.equals(AuthenticationType.A_SELECT))
    // {
    // userDetails = new UserDetails(loginType.toString(), user, new
    // ASelectCredential(user, new
    // String(password),
    // aSelectTicket, ip));
    // userDetails = aSelectAuthenticator.login(userDetails);
    // }
    // else
    if (authType.equals(AuthenticationType.INTERNAL)) {
      userDetails = new UserDetails(loginType.toString(), user,
          new LocalAccountCredential(user, null, ip));
      userDetails = localAccountAuthenticator.authorize(loginType, userDetails);
    }
    else {
      log.error("Login failed bad authType");
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
          new Object[] { authType });
    }

    return userDetails;
  }

  public UserDetails prepareLogin(ClientLoginType loginType, String user,
      char[] password, String clientIp, String aSelectTicket, String sessionId) throws Exception {
    if (loginType == null || user == null || password == null) {
      log.error("Cannot pass null parms into login method");
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
          new Object[] { "null parms" });
    }

    UserProfile prof;
    synchronized(LOCK) {
        prof = dbUser.getUserProfile(user);
    }
    
    AuthenticationType authType = AuthenticationType.INTERNAL;
    if (prof != null) {
      authType = prof.getAuthenticationData().getAuthenticationType();
    }

    UserDetails userDetails = null;
    IPAddress ip = IPAddress.fromString(clientIp);
    if (ip == null) {
      ip = new IPAddress("localhost", null);
    }


    if (authType.equals(AuthenticationType.INTERNAL)) {
      userDetails = new UserDetails(sessionId, user,
          new LocalAccountCredential(user, new String(password), ip));
      userDetails = localAccountAuthenticator.login(loginType, userDetails);
    }
    else {
      log.error("Login failed bad authType");
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_LOGIN_FAILED,
          new Object[] { authType });
    }
    checkAging(user, prof, userDetails);
    return userDetails;
  }

  private void checkAging(String user, UserProfile prof, UserDetails userDetails) throws Exception {
    if (userDetails.getErrorCode().equals(ERROR_PASSWORD_EXPIRED) || userDetails.getErrorCode().equals(ERROR_DORMANT)) {
      final int passwordAging = prof.getAuthenticationData().getUserAccountPolicy().getLocalPasswordPolicy()
          .getPwAging();
      log.debug("Account {} has errors: '" + userDetails.getErrorCode() + "', aging is: {}", user, passwordAging);
      if (passwordAging == -1) {
        userDetails.setErrorCode(SessionErrorCode.NO_ERROR);
        dbUser.setUserAuthenticationState(user, AuthenticationState.VALID);
        log.debug("userDetails.getUserPolicyProfile().hasAccessToAllResources(): "+userDetails.getUserPolicyProfile().hasAccessToAllResources());
        
        
        final AuthenticationAuditData auditDataCopy = new AuthenticationAuditData();
        auditDataCopy.resetLocationOfInvalidAttemps();
        
        final AuthenticationData authenticationData = userDetails.getUserPolicyProfile().getUserProfile()
            .getAuthenticationData();
        
        final AuthenticationData authenticationDataCopy = new AuthenticationData();
        authenticationDataCopy.setAuditData(authenticationData.getAuditData());
        authenticationDataCopy.setAuthenticationState(authenticationData.getAuthenticationState());
        authenticationDataCopy.setAuthenticationType(authenticationData.getAuthenticationType());
        authenticationDataCopy.setInternalAccountData(authenticationData.getInternalAccountData());
        authenticationDataCopy.setLastAuthenticationStateChange(authenticationData.getLastAuthenticationStateChange());
        authenticationDataCopy.setUserAccountPolicy(authenticationData.getUserAccountPolicy());
        authenticationDataCopy.setWSDLCredential(authenticationData.getWSDLCredential());
        authenticationDataCopy.setAuditData(auditDataCopy);
        
        dbUser.setUserAuthenticationData(userDetails.getUserID(), authenticationDataCopy);
        
      }
    }
    else {
      log.debug("userDetails.getErrorCode(): {}", userDetails.getErrorCode());
    }
  }

  public void sessionValidate(LoginToken token) throws Exception {

    try {
      UserDetails userDetails = getUserDetails(token);
      userDetails.setErrorCode(SessionErrorCode.NO_ERROR);

      AbstractCredential credential = userDetails.getCredential();
      if (credential == null) {
        throw new Exception("Invalid credential: null");
      }

      if (credential.getAuthenticateMethod().equals(
          AbstractCredential.AuthenticateMethod.LOCAL_ACCOUNT)
          || credential.getAuthenticateMethod().equals(
              AbstractCredential.AuthenticateMethod.WEB_SERVICES)) {
        userDetails = localAccountAuthenticator.sessionValidate(userDetails);
      }
      else {
        userDetails
            .setErrorCode(SessionErrorCode.ERROR_INVALID_AUTHENTICATION_TYPE);
      }

      LoginTokenCache.INSTANCE.updateUserDetails(token, userDetails);
      if (userDetails.getErrorCode() != SessionErrorCode.NO_ERROR) {
        if (userDetails.getErrorCode() == SessionErrorCode.ERROR_SESSION_EXPIRED) {
          // session expired, log them out.
          LoginTokenCache.INSTANCE.logoutUser(token);
        }

        log.debug("SessionValidate: Session is invalid "
            + userDetails.getErrorCode());
        throw new NrbException(
            DracErrorConstants.AUTHENTICATION_ERROR_SESSION_VALIDATE_FAILED,
            new Object[] { userDetails.getErrorCode() });
      }
    }
    catch (NrbException nrb) {
      // Got an exception consider this login dead...
      log.error("exception in sessionValidate, session invalidated", nrb);
      LoginTokenCache.INSTANCE.logoutUser(token);
      throw nrb;
    }
    catch (Exception e) {
      // Got an exception consider this login dead...
      log.error("exception in sessionValidate, session invalidated", e);
      LoginTokenCache.INSTANCE.logoutUser(token);
      throw new NrbException(
          DracErrorConstants.AUTHENTICATION_ERROR_SESSION_VALIDATE_FAILED,
          new Object[] { e.getMessage() }, e);
    }

  }

  public void setConfirmationTimeout(UserDetails userDetails, int timeout)
      throws Exception {
    try {
      // @TODO call isallowed!
      DbUtilityCommonUtility.INSTANCE.updateConfirmationTimeout(timeout);
      DbLog.INSTANCE.generateLog(
          new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
              null, null, LogKeyEnum.KEY_SETTING_CONFIRMATION, null));
    }
    catch (Exception e) {
      log.error("Failed to update confirmation timeout", e);
      DbLog.INSTANCE.generateLog(
          new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
              null, null, LogKeyEnum.KEY_SETTING_CONFIRMATION_FAILED, null));
      throw new NrbException(
          DracErrorConstants.MLBW_ERROR_2024_FAILED_EDIT_CONFIRMATION, null, e);
    }
  }

  public void setDefaultGlobalPolicy(UserDetails userDetails,
      GlobalPolicy policy) throws Exception {
    try {
      // @TODO call isAllowed?
      dbGlobalPolicy.setDefaultGlobalPolicy(policy);
      PolicyLogEntry.createLogEntry(userDetails, null,
          LogKeyEnum.KEY_EDIT_GLOBAL);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
    }
    catch (Exception e) {
      log.error("setDefaultGlobalPolicy", e);
      PolicyLogEntry.createLogEntry(userDetails, null,
          LogKeyEnum.KEY_EDIT_GLOBAL_FAILED);
      throw e;
    }
  }

  public void setResourceGroupMembership(UserDetails userDetails,
      String resourceGroupName, MembershipData membership) throws Exception {
    // @TODO call isallowed!
    dbResourceGroupProfile.setResourceGroupMembership(resourceGroupName,
        membership);
    HierarchicalContainmentPolicy.INSTANCE.invalidate();
  }

  public void setResourceGroupPolicy(UserDetails userDetails,
      String resourceGroupName, ResourcePolicy resourcePolicy) throws Exception {
    try {
      // @TODO call isallowed!
      dbResourceGroupProfile.setResourceGroupPolicy(resourceGroupName,
          resourcePolicy);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, resourceGroupName,
          LogKeyEnum.KEY_EDIT_RESGROUP, new String[] { "policy" });
    }
    catch (Exception e) {
      log.error("setResourceGroupPolicy", e);
      PolicyLogEntry.createLogEntry(userDetails, resourceGroupName,
          LogKeyEnum.KEY_EDIT_RESGROUP_FAILED, new String[] { "policy" });
      throw e;
    }
  }

  public void setResourceGroupResourceList(UserDetails userDetails,
      String resourceGroupName, List<Resource> resourceList) throws Exception {
    try {
      // @TODO call isallowed!
      ResourceGroupProfile profile = dbResourceGroupProfile.setResourceGroupResourceList(resourceGroupName,
          resourceList);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, resourceGroupName,
          LogKeyEnum.KEY_EDIT_RESGROUP, new String[] { "resources" });
      if(profile!=null){
    	  LoginTokenCache.INSTANCE.updateUserProfilesInTokenCacheWithResources(profile);
      }
    }
    catch (Exception e) {
      log.error("setResourceGroupResourceList", e);
      PolicyLogEntry.createLogEntry(userDetails, resourceGroupName,
          LogKeyEnum.KEY_EDIT_RESGROUP_FAILED, new String[] { "resources" });
      throw e;
    }
  }



  public void setScheduleOffset(UserDetails userDetails, int offset)
      throws Exception {
    try {
      // @TODO call isallowed!
      DbUtilityCommonUtility.INSTANCE.updateScheduleOffset(offset);
      DbLog.INSTANCE.generateLog(
          new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
              null, null, LogKeyEnum.KEY_SETTING_OVERHEAD, null));
    }
    catch (Exception e) {
      log.error("Failed to update schedule offset", e);
      DbLog.INSTANCE.generateLog(
          new LogRecord(userDetails.getUserID(), userDetails.getLoginAddress(),
              null, null, LogKeyEnum.KEY_SETTING_OVERHEAD_FAILED, null));
      throw new NrbException(
          DracErrorConstants.MLBW_ERROR_2023_FAILED_EDIT_OVERHEAD, null, e);
    }
  }

  public void setupUserAuthenticationAccount(UserDetails userDetails,
      String userID, AbstractCredential credential) throws Exception {
    try {
      // @TODO call isallowed!
      dbUser.setupUserAuthenticationAccount(userID, credential);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "account" });
    }
    catch (Exception e) {
      log.error("setupUserAuthenticationAccount", e);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER_FAILED, new String[] { "account" });
      throw e;
    }
  }

  public void setUserAccountStatus(UserDetails userDetails, String userID,
      AccountStatus status) throws Exception {
    try {
      // @TODO call isallowed!
      dbUser.setUserAccountStatus(userID, status);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "account status" });
    }
    catch (Exception e) {
      log.error("setUserAccountStatus", e);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER_FAILED, new String[] { "account status" });
      throw e;
    }
  }

  public void setUserAuthenticationData(UserDetails userDetails, String userID,
      AuthenticationData authenticationData) throws Exception {
    try {
      // @TODO call isallowed!
      dbUser.setUserAuthenticationData(userID, authenticationData);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "authentication data" });
    }
    catch (Exception e) {
      log.error("setUserAuthenticationData", e);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "authentication data" });
      throw e;
    }
  }

  public void setUserGroupMembership(UserDetails userDetails,
      UserGroupName userGroupName, MembershipData membership) throws Exception {
    try {
      // @TODO call isallowed!
      dbUserGroupProfile.setUserGroupMembership(userGroupName, membership);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, userGroupName.toString(),
          LogKeyEnum.KEY_EDIT_USERGROUP, new String[] { "membership" });
    }
    catch (Exception e) {
      log.error("setUserGroupMembership", e);
      PolicyLogEntry.createLogEntry(userDetails, userGroupName.toString(),
          LogKeyEnum.KEY_EDIT_USERGROUP_FAILED, new String[] { "membership" });
      throw e;
    }
  }

  public void setUserGroupUserGroupPolicy(UserDetails userDetails,
      UserGroupName userGroupName, GroupPolicy groupPolicy) throws Exception {
    try {
      // @TODO call isallowed!
      dbUserGroupProfile
          .setUserGroupUserGroupPolicy(userGroupName, groupPolicy);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, userGroupName.toString(),
          LogKeyEnum.KEY_EDIT_USERGROUP, new String[] { "group policy" });
    }
    catch (Exception e) {
      log.error("setUserGroupUserGroupPolicy", e);
      PolicyLogEntry
          .createLogEntry(userDetails, userGroupName.toString(),
              LogKeyEnum.KEY_EDIT_USERGROUP_FAILED,
              new String[] { "group policy" });
      throw e;
    }
  }

  public void setUserMembership(UserDetails userDetails, String userID,
      MembershipData membership) throws Exception {
    try {
      // @TODO call isallowed!
      dbUser.setUserMembership(userID, membership);
      HierarchicalContainmentPolicy.INSTANCE.invalidate();
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "membership" });
    }
    catch (Exception e) {
      log.error("setUserMembership", e);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "membership" });
      throw e;
    }
  }

  public void setUserOrganization(UserDetails userDetails, String userID,
      OrganizationData orgData) throws Exception {
    try {
      // @TODO call isallowed!
      dbUser.setUserOrganization(userID, orgData);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "organization" });
    }
    catch (Exception e) {
      log.error("setUserOrganization", e);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "organization" });
      throw e;
    }
  }

  public void setUserPersonalData(UserDetails userDetails, String userID,
      PersonalData personalData) throws Exception {

    try {
      // @TODO call isallowed!
      dbUser.setUserPersonalData(userID, personalData);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "personal data" });
    }
    catch (Exception e) {
      log.error("setUserPersonalData", e);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "personal data" });
      throw e;
    }
  }

  public void setUserTimeZoneIDPreference(UserDetails userDetails,
      String userID, String localeValue) throws Exception {
    try {
      // @TODO call isallowed!
      dbUser.setUserTimeZoneIDPreference(userID, localeValue);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER, new String[] { "timezone" });
    }
    catch (Exception e) {
      log.error("setUserTimeZoneIDPreference", e);
      PolicyLogEntry.createLogEntry(userDetails, userID,
          LogKeyEnum.KEY_EDIT_USER_FAILED, new String[] { "timezone" });
      throw e;
    }
  }

  private void validatePassword(UserDetails userDetails, String oldpw,
      String newpw) throws Exception {
    String encodedPasswordRules = getGlobalPolicy(userDetails)
        .getLocalAccountPolicy().getLocalPasswordPolicy().getPwRules();

    if (encodedPasswordRules != null) {
      Map<String, String> encodedPasswordRulesMap = new HashMap<String, String>();
      encodedPasswordRulesMap.put(PasswordEvaluator.PASSWD_PASSWORDRULES,
          encodedPasswordRules);

      Map<String, String> decodedRulesMap = new HashMap<String, String>();
      PasswordEvaluator.decodePasswordRules(encodedPasswordRulesMap,
          decodedRulesMap);

      PasswordErrorCode firstErrorCode = PasswordEvaluator.validatePassword(
          userDetails, newpw, decodedRulesMap, oldpw);

      if (!firstErrorCode.equals(PasswordErrorCode.NO_ERROR)) {
        // Pass back the rule set:
        Map<String, String> ruleSet = new HashMap<String, String>();
        Integer minLength = Integer.parseInt(decodedRulesMap
            .get(PasswordEvaluator.PASSWD_MINLENGTH));
        Integer minAlpha = Integer.parseInt(decodedRulesMap
            .get(PasswordEvaluator.PASSWD_MINALPHAVALUE));
        Integer minDigit = Integer.parseInt(decodedRulesMap
            .get(PasswordEvaluator.PASSWD_MINDIGITVALUE));
        Integer minSpecial = Integer.parseInt(decodedRulesMap
            .get(PasswordEvaluator.PASSWD_MINSPECIALVALUE));
        Integer minDifferent = Integer.parseInt(decodedRulesMap
            .get(PasswordEvaluator.PASSWD_MINDIFFERENT));
        String mix = decodedRulesMap.get(PasswordEvaluator.PASSWD_MIXEDALPHA);
        boolean mixedAlpha = mix.equals("yes");

        if (minLength != 0) {
          ruleSet.put("minPwLength", minLength.toString());
        }
        if (minAlpha != 0) {
          ruleSet.put("minAlphaChars", minAlpha.toString());
        }
        if (minDigit != 0) {
          ruleSet.put("minNumericChars", minDigit.toString());
        }
        if (minSpecial != 0) {
          ruleSet.put("minSpecialChars", minSpecial.toString());
          String allowedSpecialChars = decodedRulesMap
              .get(PasswordEvaluator.PASSWD_SPECIALCHARS);
          allowedSpecialChars = new String(URLDecoder.decode(
              allowedSpecialChars, "UTF-8"));
          ruleSet.put("specialChars", allowedSpecialChars);
        }
        if (minDifferent != 0) {
          ruleSet.put("minDifferentChars", minDifferent.toString());
        }
        if (mixedAlpha) {
          ruleSet.put("mixedCase", "true");
        }

        throw new DracPasswordEvaluationException(
            new PasswordErrorCode[] { firstErrorCode }, ruleSet);
      }
    }
    else {
      log.error("SecurityServer::validatePassword - no encoded password rules found in global policy");

      throw new DracPasswordEvaluationException(
          new PasswordErrorCode[] { PasswordErrorCode.ERROR_RULES_NOT_FOUND });
    }

  }

}
