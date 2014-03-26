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

package com.nortel.appcore.app.drac.common.errorhandling;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DracErrorConstants {
  
  private static final Logger log = LoggerFactory.getLogger(DracErrorConstants.class);

  public static int ERROR_MARKER_START = 0;

  /*************************************************************/
  /* Error Code for General Purpose */
  /* Start from 0-999 */
  /*************************************************************/
  public static final int GENERAL_ERROR_MARKER_START = ERROR_MARKER_START + 0;

  public static final int GENERAL_ERROR_MARKER_INTERVAL = 1000;

  public static final int GENERAL_ERROR_INTERNAL = GENERAL_ERROR_MARKER_START + 1;

  public static final int GENERAL_ERROR_INTERNAL_REASON = GENERAL_ERROR_MARKER_START + 2;

  

  /*************************************************************/
  /* Error Code for Security Purpose */
  /* Start from 1000 - 1999 */
  /*************************************************************/
  public static final int SECURITY_ERROR_MARKER_START = GENERAL_ERROR_MARKER_START
      + GENERAL_ERROR_MARKER_INTERVAL;

  public static final int SECURITY_ERROR_MARKER_INTERVAL = 1000;

  public static final int POLICY_ERROR_EXCEED_BW_SIZE = SECURITY_ERROR_MARKER_START + 0;

  public static final int POLICY_ERROR_EXCEED_BW_DURATION = SECURITY_ERROR_MARKER_START + 1;

  public static final int POLICY_ERROR_EXCEED_BW_SERVICE_SIZE = SECURITY_ERROR_MARKER_START + 2;

  public static final int POLICY_ERROR_EXCEED_BW_AGG_SERVICE_SIZE = SECURITY_ERROR_MARKER_START + 3;

  public static final int POLICY_ERROR_GENERAL_ACCESS_CONTROL = SECURITY_ERROR_MARKER_START + 4;

  public static final int POLICY_ERROR_READ_ACCESS_CONTROL_USER_TYPE = SECURITY_ERROR_MARKER_START + 5;

  public static final int POLICY_ERROR_WRITE_ACCESS_CONTROL_USER_TYPE = SECURITY_ERROR_MARKER_START + 6;

  public static final int POLICY_ERROR_TIME_BASED = SECURITY_ERROR_MARKER_START + 7;

  public static final int AUTHENTICATION_ERROR_LOGIN_FAILED = SECURITY_ERROR_MARKER_START + 50;

  public static final int AUTHENTICATION_ERROR_LOGOUT_FAILED = SECURITY_ERROR_MARKER_START + 51;

  public static final int AUTHENTICATION_ERROR_SESSION_VALIDATE_FAILED = SECURITY_ERROR_MARKER_START + 52;

  public static final int SECURITY_ERROR_PARAMETER_INVALID = SECURITY_ERROR_MARKER_START + 80;

  public static final int SECURITY_ERROR_PARAMETER_EXISTED = SECURITY_ERROR_MARKER_START + 81;

  public static final int SECURITY_ERROR_XML_ATTR_MISSING = SECURITY_ERROR_MARKER_START + 82;

  public static final int SECURITY_ERROR_TYPE_NOT_SUPPORTED = SECURITY_ERROR_MARKER_START + 84;

  /*************************************************************/
  /* Error Code for NRB_PORT/MLBW Purpose */
  /* Start from 2000 - 2999 */
  /*************************************************************/
  public static final int MLBW_ERROR_MARKER_START = SECURITY_ERROR_MARKER_START
      + SECURITY_ERROR_MARKER_INTERVAL;

  public static final int MLBW_ERROR_MARKER_INTERVAL = 1000;

  public static final int MLBW_ERROR_2000 = MLBW_ERROR_MARKER_START + 0;

  public static final int MLBW_ERROR_2003_CONTROLLER_UNAVAILBLE = MLBW_ERROR_MARKER_START + 3;

  public static final int MLBW_ERROR_2004_CANNOT_TIMES_EQUAL = MLBW_ERROR_MARKER_START + 4;

  public static final int MLBW_ERROR_2005_INVALID_TIMES = MLBW_ERROR_MARKER_START + 5;

  public static final int MLBW_ERROR_2006_PORTS_EQUAL = MLBW_ERROR_MARKER_START + 6;

  public static final int MLBW_ERROR_2007_DB_INSERT = MLBW_ERROR_MARKER_START + 7;

  public static final int MLBW_ERROR_2009_LOOKUP_SERVICE = MLBW_ERROR_MARKER_START + 9;

  public static final int MLBW_ERROR_2010_NOT_EXIST = MLBW_ERROR_MARKER_START + 10;

  public static final int MLBW_ERROR_2012_SPECIFIED_TIME_INVALID = MLBW_ERROR_MARKER_START + 12;

  public static final int MLBW_ERROR_2013_SCHED_CREATE_DB = MLBW_ERROR_MARKER_START + 13;

  public static final int MLBW_ERROR_2014_CREATE_SERVICE_EXPIRED = MLBW_ERROR_MARKER_START + 14;

  public static final int MLBW_ERROR_2015_FAILED_CREATE_SERVICE = MLBW_ERROR_MARKER_START + 15;

  public static final int MLBW_ERROR_2016_CANCEL_SCHED_FAIL = MLBW_ERROR_MARKER_START + 16;

  public static final int MLBW_ERROR_2017_FAILED_CANCEL_SERVICE = MLBW_ERROR_MARKER_START + 17;

  public static final int MLBW_ERROR_2018_SERVER_INFO_FAILED = MLBW_ERROR_MARKER_START + 18;

  public static final int MLBW_ERROR_2020_SCHED_NOT_ACTIVE = MLBW_ERROR_MARKER_START + 20;

  public static final int MLBW_ERROR_2021_SERVICE_NOT_ACTIVE = MLBW_ERROR_MARKER_START + 21;

  public static final int MLBW_ERROR_2022_FAILED_QUERY_PATH = MLBW_ERROR_MARKER_START + 22;

  public static final int MLBW_ERROR_2023_FAILED_EDIT_OVERHEAD = MLBW_ERROR_MARKER_START + 23;

  public static final int MLBW_ERROR_2024_FAILED_EDIT_CONFIRMATION = MLBW_ERROR_MARKER_START + 24;

  public static final int MLBW_ERROR_2801_SEND_MESSAGE_FAILED_NO_USER = MLBW_ERROR_MARKER_START + 801;

  public static final int MLBW_ERROR_2803_OPERATION_FAILED_DUE_TO_THE_CURRENT_STATE = MLBW_ERROR_MARKER_START + 803;

  /*************************************************************/
  /* Error Codes for LPCP_PORT */
  /* Start from 3000 - 3999 */
  /*************************************************************/
  public static final int LPCP_ERROR_MARKER_START = MLBW_ERROR_MARKER_START
      + MLBW_ERROR_MARKER_INTERVAL;

  public static final int LPCP_ERROR_MARKER_INTERVAL = 1000;

  /* E3000 - Unexpected error calculating 1+1 path */
  public static final int LPCP_E3000_UNEXPECTED_1PLUS1_ERROR = LPCP_ERROR_MARKER_START + 0;

  /* E3001 - No protection path found */
  public static final int LPCP_E3001_NO_PROTECTION_PATH_FOUND = LPCP_ERROR_MARKER_START + 1;

  /*
   * E3002 - Rate mismatch on selected ports. Source configured rate: %1
   * destination configured rate: %2
   */
  public static final int LPCP_E3002_RATE_MISMATCH_SELECTED_PORTS = LPCP_ERROR_MARKER_START + 2;

  /* E3003 - Invalid WAN or requested rate. WAN rate: %1 requested rate: %2 */
  public static final int LPCP_E3003_INVALID_WAN_RATE_REQUESTED = LPCP_ERROR_MARKER_START + 3;

  /* E3004 - The endpoint %1 is currently not available. */
  public static final int LPCP_E3004_CANNOT_FIND_FACILITY_FOR_TNA = LPCP_ERROR_MARKER_START + 4;

  /* E3005 - Invalid source or destination node. Source %1 Destination %2 */

  /* E3007 - Source and destination ports cannot be identical */
  public static final int LPCP_E3007_PORTS_CANNOT_BE_IDENTICAL = LPCP_ERROR_MARKER_START + 7;

  /* E3013 - Specified destination channel is not on boundary for requested rate */
  public static final int LPCP_E3013_DST_CHANNEL_NOT_ON_BOUNDARY = LPCP_ERROR_MARKER_START + 13;

  /* E3014 - Error determining start or destination channel */
  public static final int LPCP_E3014_ERROR_DETERMINING_SRC_OR_DST_CHANNEL = LPCP_ERROR_MARKER_START + 14;

  /* E3015 - No available bandwidth on destination port */
  public static final int LPCP_E3015_NO_AVAILABLE_BANDWIDTH_DST = LPCP_ERROR_MARKER_START + 15;

  /* E3016 - No available bandwidth on source port */
  public static final int LPCP_E3016_NO_AVAILABLE_BANDWIDTH_SRC = LPCP_ERROR_MARKER_START + 16;

  /* E3017 - Source or destination port facility not found */
  public static final int LPCP_E3017_SRC_OR_DST_PORT_FACILITY_NOT_FOUND = LPCP_ERROR_MARKER_START + 17;

  /*
   * E3018 - Source or destination facility map not found. src neid: %1 dst
   * neid: %2
   */
  public static final int LPCP_E3018_SRC_OR_DST_FACILITY_MAP_NOT_FOUND = LPCP_ERROR_MARKER_START + 18;

  /* E3019 - Model is not ready */
  public static final int LPCP_E3019_MODEL_NOT_READY = LPCP_ERROR_MARKER_START + 19;

  /* E3020 - No path found for the specified parameters */
  public static final int LPCP_E3020_NO_PATH_FOR_SPECIFIED_PARAMETERS = LPCP_ERROR_MARKER_START + 20;

  /*
   * E3021 - Cannot find source or destination facility. Source facility %1
   * destination facility: %2
   */
  public static final int LPCP_E3021_SRC_OR_DST_FACILITY_NOT_FOUND = LPCP_ERROR_MARKER_START + 21;

  /* E3022 - FATAL ERROR: null parameters encountered in route calculation */
  public static final int LPCP_E3022_NULL_PARAMETERS_IN_ROUTE_CALC = LPCP_ERROR_MARKER_START + 22;

  /* E3023 - Unexpected error during VCAT path calculation: pathElement= %1 */
  public static final int LPCP_E3023_UNEXPECTED_ERROR_VCAT_PATH_CALC_ELEMENT = LPCP_ERROR_MARKER_START + 23;

  /* E3024 - Unexpected error during VCAT path calculation: pathInstance= %1 */
  public static final int LPCP_E3024_UNEXPECTED_ERROR_VCAT_PATH_CALC_PATHINSTANCE = LPCP_ERROR_MARKER_START + 24;

  /* E3025 - Unexpected error during VCAT path calculation: pathRoot= %1 */
  public static final int LPCP_E3025_UNEXPECTED_ERROR_VCAT_PATH_CALC_PATHROOT = LPCP_ERROR_MARKER_START + 25;

  /* E3026 - Cannot find source or destination. Source: %1 destination %2 */
  public static final int LPCP_E3026_CANNOT_FIND_SRC_OR_DEST = LPCP_ERROR_MARKER_START + 26;

  /* E3027 - Source port channel specified is not available */
  public static final int LPCP_E3027_SRC_CHANNEL_NOT_AVAILABLE = LPCP_ERROR_MARKER_START + 27;

  /* E3028 - Target port channel specified is not available */
  public static final int LPCP_E3028_TGT_CHANNEL_NOT_AVAILABLE = LPCP_ERROR_MARKER_START + 28;

  /* E3029 - No path found within specified hop count: %1 */
  public static final int LPCP_E3029_NO_PATH_FOR_HOP_COUNT = LPCP_ERROR_MARKER_START + 29;

  /*
   * E3032 - Source and destination port capabilities mismatch. Source VCAT: %1
   * destination VCAT: %2
   */
  public static final int LPCP_E3032_SRC_AND_DEST_PORT_CAPABILITIES_MISMATCH = LPCP_ERROR_MARKER_START + 32;

  /* E3033 - An invalid cost value was specified: %1 */
  public static final int LPCP_E3033_INVALID_COST_SPECIFIED = LPCP_ERROR_MARKER_START + 33;

  /* E3034 - An invalid metric value was specified %1 */
  public static final int LPCP_E3034_INVALID_METRIC_SPECIFIED = LPCP_ERROR_MARKER_START + 34;

  /* E3035 - An invalid SRLG value was specified */
  public static final int LPCP_E3035_INVALID_SRLG_SPECIFIED = LPCP_ERROR_MARKER_START + 35;

  /* E3036 - Exception calculating path: %1 */
  public static final int LPCP_E3036_EXCEPTION_CALCULATING_PATH = LPCP_ERROR_MARKER_START + 36;

  /* E3037 - Selected routing algorithm is unknown: %1 */
  public static final int LPCP_E3037_ROUTING_ALGORITHM_UNKNOWN = LPCP_ERROR_MARKER_START + 37;

  /* E3038 - Unexpected error calculating 1+1 path */
  public static final int LPCP_E3038_EXCEPTION_CALC_1PLUS1_PATH = LPCP_ERROR_MARKER_START + 38;

  /* E3039 - Unexpected error during VCAT path calculation: %1 */
  public static final int LPCP_E3039_UNEXPECTED_ERR_VCAT_PATH_CALC = LPCP_ERROR_MARKER_START + 39;

  /* E3040 - Unexpected routing exception occurred: %1 */
  public static final int LPCP_E3040_UNEXPECTED_ROUTING_EXCEPTION = LPCP_ERROR_MARKER_START + 40;

  /*
   * E3041 - Invalid schedule. Schedule start time is later than schedule end
   * time.
   */
  public static final int LPCP_E3041_INVALID_SCHED_END_LESSTHAN_START = LPCP_ERROR_MARKER_START + 41;

  /*
   * E3042 - Invalid schedule. Invalid schedule. Schedule end time is in the
   * past.
   */
  public static final int LPCP_E3042_INVALID_SCHED_ENDTIME_IN_PAST = LPCP_ERROR_MARKER_START + 42;

  /* E3043 - Invalid source endpoint signaling type. */
  public static final int LPCP_E3043_INVALID_SRC_ENDPOINT_SIGTYPE = LPCP_ERROR_MARKER_START + 43;

  /* E3044 - Invalid destination endpoint signaling type. */
  public static final int LPCP_E3044_INVALID_DST_ENDPOINT_SIGTYPE = LPCP_ERROR_MARKER_START + 44;

  /* E3045 - The specified service id was not found: %1 */
  public static final int LPCP_E3045_SERVICEID_NOT_FOUND = LPCP_ERROR_MARKER_START + 45;

  /* E3046 - Service in invalid state */
  public static final int LPCP_E3046_SERVICE_IN_INVALID_STATE = LPCP_ERROR_MARKER_START + 46;

  /* E3047 - An invalid rate was specified: %1 */
  public static final int LPCP_E3047_INVALID_RATE_SPECIFIED = LPCP_ERROR_MARKER_START + 47;

  /* E3048 - Source wavelength: %1 does not match destination wavelength: %2 */
  public static final int LPCP_E3048_WAVELENGTH_MISMATCH = LPCP_ERROR_MARKER_START + 48;

  /* E3049 - Layer2 service requires VLANID specified */
  public static final int LPCP_E3049_INVALID_SRC_VLANID = LPCP_ERROR_MARKER_START + 49;
  /* E3050 - Layer2 service requires VLANID specified */
  public static final int LPCP_E3050_INVALID_DST_VLANID = LPCP_ERROR_MARKER_START + 50;
  /* E3051 - Layer2 service requires VLANID specified */
  public static final int LPCP_E3051_USED_SRC_VLANID = LPCP_ERROR_MARKER_START + 51;
  /* E3052 - Layer2 service requires VLANID specified */
  public static final int LPCP_E3052_USED_DST_VLANID = LPCP_ERROR_MARKER_START + 52;

  /* E3053,54 - The vcat/ccat routing option requested cannot be implemented */
  public static final int LPCP_E3053_VCAT_REQUEST_CANNOT_BE_ROUTED = LPCP_ERROR_MARKER_START + 53;
  public static final int LPCP_E3054_CCAT_REQUEST_CANNOT_BE_ROUTED = LPCP_ERROR_MARKER_START + 54;

  /* E3055,56 - Requested start channel not available */
  public static final int LPCP_E3055_ERROR_SRC_CHANNEL_NOTAVAILABLE = LPCP_ERROR_MARKER_START + 55;
  public static final int LPCP_E3056_ERROR_DST_CHANNEL_NOTAVAILABLE = LPCP_ERROR_MARKER_START + 56;

  /*************************************************************/
  /* Error Code for Webservices Purpose */
  /* Start from 4000 - 4999 */
  /*************************************************************/
  public static final int WS_ERROR_MARKER_START = LPCP_ERROR_MARKER_START
      + LPCP_ERROR_MARKER_INTERVAL;

  public static final int WS_ERROR_MARKER_INTERVAL = 1000;

  public static final int WS_OPERATION_FAILED = WS_ERROR_MARKER_START + 0;

  public static final int WS_INVALID_SESSION = WS_ERROR_MARKER_START + 1;

  public static final int WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE = WS_ERROR_MARKER_START + 2;

  public static final int WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE = WS_ERROR_MARKER_START + 3;

  public static final int WS_USER_MUST_BE_AUTHENTICATED_FIRST = WS_ERROR_MARKER_START + 4;

  public static final int WS_INVALID_REQUEST_MESSAGE = WS_ERROR_MARKER_START + 5;

  /*************************************************************/
  /* Error Code for NeProxy Purpose */
  /* Start from 5000 - 5999 */
  /*************************************************************/
  public static final int NP_ERROR_MARKER_START = WS_ERROR_MARKER_START
      + WS_ERROR_MARKER_INTERVAL;

  public static final int NP_ERROR_MARKER_INTERVAL = 1000;

  /*************************************************************/
  /* Error Code for Request Handler */
  /* Start from 6000 - 6999 */
  /*************************************************************/
  public static final int RH_ERROR_MARKER_START = NP_ERROR_MARKER_START
      + NP_ERROR_MARKER_INTERVAL;

  public static final int RH_ERROR_6000_CANNOT_BIND_NRB = RH_ERROR_MARKER_START + 0;

  public static final int RH_ERROR_6002_REDUNDANCY_SWITCHOVER_IN_PROGRESS = RH_ERROR_MARKER_START + 2;

  public static final int RH_ERROR_6003_NO_ENDPOINT_FOUND_WITH_TNA = RH_ERROR_MARKER_START + 3;

  public static final int RH_ERROR_6999_INVALID_OP = RH_ERROR_MARKER_START + 999;

  // Formatter methods
  private static String propertyBase = System.getProperty(
      "org.opendrac.common.error.handling.base",
      "com.nortel.appcore.app.drac.common.resources.ErrorDescription");

  public static String formatErrorCode(Locale locale, DracException t) {
    ResourceBundle bundle = getBundle(locale);

    String msg = "";

    try {
      msg = bundle.getString(t.getKeyAsString());
      if (t.getArgs() != null) {
        msg = MessageFormat.format(msg, t.getArgs());
      }
    }
    catch (Exception e) {
      log.error("Missing Error description", e);
      log.error("Missing description from key: " + t.getKeyAsString()
          + " stack trace is from origional exception ", t);
      msg = t.getKeyAsString();
      if (t.getArgs() != null && t.getArgs().length > 0) {
        StringBuilder details = new StringBuilder(msg);
        details.append("?");
        for (int i = 0; i < t.getArgs().length; i++) {
          details.append(t.getArgs()[i].toString());
          if (i > 0) {
            details.append("&");
          }
        }
        msg += details.toString();
      }
    }
    return msg;
  }

  public static String formatErrorCode(Locale locale, DracRemoteException dracRemoteException) {
    ResourceBundle bundle = getBundle(locale);

    String msg = "";

    try {
      msg = bundle.getString(dracRemoteException.getKeyAsString());
      if (dracRemoteException.getArgs() != null) {
        msg = MessageFormat.format(msg, dracRemoteException.getArgs());
      }
    }
    catch (Exception e) {
      log.error("Missing Error description", e);
      log.error("Missing description from key: " + dracRemoteException.getKeyAsString()
          + " stack trace is from origional exception ", dracRemoteException);
      msg = dracRemoteException.getKeyAsString();
      if (dracRemoteException.getArgs() != null && dracRemoteException.getArgs().length > 0) {
        StringBuilder details = new StringBuilder(msg);
        details.append("?");
        for (int i = 0; i < dracRemoteException.getArgs().length; i++) {
          details.append(dracRemoteException.getArgs()[i].toString());
          if (i > 0) {
            details.append("&");
          }
        }
        msg += details.toString();
      }
    }
    return msg;
  }

  public static String formatErrorCode(Locale locale, String key, Object[] args) {
    ResourceBundle bundle = getBundle(locale);

    String msg = "";

    try {
      msg = bundle.getString(key);
      if (args != null) {
        msg = MessageFormat.format(msg, args);
      }
    }
    catch (Exception e) {
      log.error("Missing Error description", e);
      msg = key;
      if (args != null && args.length > 0) {
        StringBuilder details = new StringBuilder(msg);
        details.append("?");
        for (int i = 0; i < args.length; i++) {
          details.append(args[i].toString());
          if (i > 0) {
            details.append("&");
          }
        }
        msg += details.toString();
      }
    }
    return msg;
  }

  public static ResourceBundle getBundle(Locale locale) {
    ResourceBundle bundle = null;
    try {
      bundle = ResourceBundle.getBundle(propertyBase, locale);
    }
    catch (Exception e) {
      log.error("Error loading resource bundle for " + locale, e);
      bundle = ResourceBundle.getBundle(propertyBase, Locale.getDefault());
    }
    return bundle;
  }

  public static String getErrorMessage(Locale locale, int dracErrorConstants,
      Object[] args) {

    if (locale == null) {
      locale = ExceptionFormatter.INSTANCE.DEFAULT_LOCALE;
    }
    return ExceptionFormatter.INSTANCE.formatMessage(locale,
        new ResourceKey(dracErrorConstants), args);
  }
}
