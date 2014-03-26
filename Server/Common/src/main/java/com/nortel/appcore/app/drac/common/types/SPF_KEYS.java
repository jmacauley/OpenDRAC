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

package com.nortel.appcore.app.drac.common.types;

/**
 * This enumeration identifies the key values for scheduling parameters.  Used
 * as a front end to the schedule code, rather than permit users to pass in
 * a map of <String,Object> like we used to and have hard coded strings
 * everywhere we pass in a map of <SPF_KEYS,Object> just so we can keep track of
 * what might get passed in. Would have been smarter to pass in a class that
 * holds all this instead of a map. Oh, wait, we actually do that with the
 * "other" create schedule operation, except that turned out to be a mess as
 * well.
 */
public enum SPF_KEYS {
  SPF_SRCTNA, // required and validated
  SPF_DSTTNA, // required and validated

  SPF_SRCCHANNEL, //
  SPF_DSTCHANNEL, //

  SPF_SRCVLAN, //
  SPF_DSTVLAN, //

  SPF_RATE, //

  SPF_SRLG, //
  SPF_VCATROUTING_OPTION, //
  SPF_PROTECTION, //
  SPF_DIVERSE_EXCLUDE, //
  SPF_START_TIME, //
  SPF_END_TIME, //

  // SPF_METRIC("METRIC"), /* THIS IS A SPECAL CASE and gets converted into the
  // METRIC2, HOP or COST enums
  // */
  /*
   * These were provided indirectly be setting SPF_METRIC to a string like
   * "COST=5" and we'll set the cost enum to 5. Now we just set them directly.
   */
  SPF_HOP, //
  SPF_COST, //
  SPF_METRIC2, //

  SPF_SERVICEID, //
  SPF_SCHEDULE_KEY, //

  /**
   * If you search only on these enums they don't appear to be used (set but not
   * read). Some of these may be truly unused, others map be being referenced
   * via hard coded strings or other strings
   */

  SPF_MBS, // not sure what this is for?
  SPF_SCHEDULE_NAME, // not used?
  SPF_CONTROLLER_ID, // not used?
  SPF_ACTIVATION_TYPE, // not used?
  SPF_SERVICE_STATUS, // used in ServiceXml ?!

  SPF_EMAIL, // not used any more?
  SPF_USER, // used in ServiceXml - minimally.
  SPF_OFFSET, // not used?
  SPF_USERGROUP, SPF_SRCLAYER, SPF_DSTLAYER,

  /**
   * These are internal and should not be passed in
   **/

  SPF_ID, // generated
  SPF_CALLID, // generated

  SPF_SOURCEID, // NE ID of source used internally and derived from
                // the tna
  SPF_TARGETID, // NE ID of dest used internally and derived from the
                // tna
  SPF_KEY, // xml string internal
  SPF_EXCLUDE, //
  SPF_PATHTYPE, //
  SPF_WORKINGPATH, //
  SPF_PROTECTINGPATH, //
  SPF_PATHKEY, //
  SPF_RT_PATH_DATA, // List<CrossConnection>
  SPF_FIRST_VCAT_DONE, //
  SPF_JUNG_EDGELIST, //
  SPF_CUR_PATH_LIST_KEY, /* List<CrossConnection> */;

}
