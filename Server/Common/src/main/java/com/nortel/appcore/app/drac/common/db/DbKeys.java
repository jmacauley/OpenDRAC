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

package com.nortel.appcore.app.drac.common.db;

/**
 * This class holds keys and other constants for the database packages. Some
 * database API's use maps that can contain the database keys. We don't want
 * clients importing the database classes directly, so the keys go here.
 * 
 * @author pitman
 */
public final class DbKeys {

	public static final class AdminConsoleUserPreferencesCols {
		public static final String USERID = "userId";
		public static final String ELEMENT = "element";

		private AdminConsoleUserPreferencesCols() {
			super();
		}

	}

	public static final class LightPathAlarmSummariesCols {
		public static final String SERVICEID = "serviceId";
		public static final String ALARMID = "id";
		public static final String SEVERITY = "severity";
		public static final String OCCURREDTIME = "occurredTime";
		public static final String SOURCE = "source";

		private LightPathAlarmSummariesCols() {
			super();
		}

	}

	public static final class LightPathCols {

		public static final String LP_SERVICEID = "serviceId"; // Used as primary
		                                                       // key
		/* LP_CALLID is hard coded in ServiceXml */
		public static final String LP_CALLID = "id";
		public static final String LP_STATUS = "status";
		public static final String LP_VCAT = "vcat";
		public static final String LP_STARTTIME = "startTime";
		public static final String LP_ENDTIME = "endTime";
		public static final String LP_USER = "user";
		public static final String LP_BILLINGGROUP = "billingGroup";
		public static final String LP_PRIORITY = "priority";
		public static final String LP_RATE = "rate";
		public static final String LP_AEND = "aEnd";
		public static final String LP_ZEND = "zEnd";
		public static final String LP_XML = "xml";
		// multi-domain for efficiency
		public static final String LP_ACTIVATIONTYPE = "activationType";
		public static final String LP_CONTROLLERID = "controllerId";
		public static final String LP_SCHEDULEID = "scheduleId";
		public static final String LP_SCHEDULENAME = "scheduleName";
		public static final String LP_MBS = "mbs";
		// filter options
		public static final String LP_FILTER_SCHEDULEID_LIST = "scheduleIdList";
		public static final String LP_STARTTIME_GREATERTHAN_EQUALTO = "lp_starttime_greaterthan_equalto";
		public static final String LP_STARTTIME_LESSTHAN_EQUALTO = "lp_starttime_lessthan_equalto";
		public static final String LP_ENDTIME_GREATERTHAN_EQUALTO = "lp_endtime_greaterthan_equalto";
		public static final String LP_ENDTIME_LESSTHAN_EQUALTO = "lp_endtime_lessthan_equalto";
		public static final String LP_NEID = "lp_neid";
		public static final String LP_AID = "lp_aid";
		public static final String LP_1PLUS1_PATH_DATA = "lp_1plus1_path_data";

		private LightPathCols() {
			super();
		}

	}

	public static final class LightPathEdgeKeys {
		public static final String EDGE_SOURCE = "source";
		public static final String EDGE_SOURCEAID = "sourceAid";
		public static final String EDGE_SOURCECHANNEL = "sourceChannel";
		public static final String EDGE_TARGET = "target";
		public static final String EDGE_TARGETAID = "targetAid";
		public static final String EDGE_TARGETCHANNEL = "targetChannel";
		// public static final String EDGE_CTKID = "ctkid";
		public static final String EDGE_RATE = "rate";
		public static final String EDGE_CCT = "CCT";
		public static final String EDGE_SWMATEAID = "SWMATE";
		public static final String EDGE_MEP = "mep";
		public static final String EDGE_VLANID = "vlanId";

		private LightPathEdgeKeys() {
			super();
		}
	}

	public static final class LogKeys {
		public static final String TIME = "time";
		public static final String ORIGINATOR = "originator";
		public static final String IP_ADDR = "address";
		public static final String BILLING_GROUP = "billingGroup";
		public static final String SEVERITY = "severity";
		public static final String CATEGORY = "category";
		public static final String LOG_TYPE = "logType";
		public static final String RESOURCE = "resource";
		public static final String RESULT = "result";
		public static final String DESC = "descr";
		public static final String XML = "xml";

		private LogKeys() {
			super();
		}
	}

	// GG ADJ
	public static final class NetworkElementAdjacencyColsV2 {
		public static final String NEID = "neid";
		public static final String PORT = "port";
		public static final String TXTAG = "txtag";
		public static final String RXTAG = "rxtag";
		public static final String TYPE = "type";
		public static final String MANUALPROVISION = "manualProvision";

		// key for use on manual links
		public static final String MANUAL_LAYR_TYPE = "manualLayrType";

		private NetworkElementAdjacencyColsV2() {
			super();
		}
	}

	public static final class NetworkElementCols {
		public static final String PK = "pk";
		public static final String NEIP = "ip";
		public static final String NEPORT = "port";
		public static final String AUTOREDISC = "autoReDiscover";
		public static final String COMMPROTOCOL = "commProtocol";
		public static final String NEID = "id";
		public static final String MANAGEDBY = "managedBy";
		public static final String MODE = "mode";
		public static final String NEINDEX = "neIndex";
		public static final String PASSWORD = "password";
		public static final String STATUS = "status";
		public static final String TID = "tid";
		public static final String TYPE = "type";
		public static final String USERID = "userId";
		public static final String SUBTYPE = "subType";
		public static final String NE_RELEASE = "neRelease";
		public static final String POSITION_X = "positionX";
		public static final String POSITION_Y = "positionY";

		private NetworkElementCols() {
			super();
		}

	}

	public static final class NetworkElementConnectionCols {
		public static final String ID = "id";
		public static final String TYPE = "type";
		public static final String RATE = "rate";
		public static final String SOURCE = "source";
		public static final String SSHELF = "sShelf";
		public static final String SSLOT = "sSlot";
		public static final String SSUBSLOT = "sSubslot";
		public static final String SPORT = "sPort";
		public static final String SCHANNEL = "sChannel";
		public static final String TSHELF = "tShelf";
		public static final String TSLOT = "tSlot";
		public static final String TSUBSLOT = "tSubslot";
		public static final String TPORT = "tPort";
		public static final String TCHANNEL = "tChannel";
		public static final String TARGET = "target";
		public static final String SOURCEAID = "sourceAid";
		public static final String TARGETAID = "targetAid";
		public static final String SWMATEAID = "swmateAid";
		public static final String SWMATE_NEID = "swmateNeid";
		public static final String SWMATE_SHELF = "swmateShelf";
		public static final String SWMATE_SLOT = "swmateSlot";
		public static final String SWMATE_SUBSLOT = "swmateSubslot";
		public static final String SWMATE_PORT = "swmatePort";
		public static final String SWMATE_CHANNEL = "swmateChannel";
		public static final String COMMITTED = "committed";
		public static final String CALLKEY = "callKey";
		/*
		 * I'm creating these keys here, instead of simply using the equivalent in
		 * DbNetworkElement, in order to avoid a collision in similarly named
		 * strings. e.g. NE 'port' and Facility 'port'
		 */
		public static final String NEID_FOR_CONN = "neidForConn";
		// Special keys for search
		public static final String ID_NOT = "idNot";

		private NetworkElementConnectionCols() {
			super();
		}

	}

	public static final class NetworkElementFacilityCols {
		public static final String PK = "pk";
		public static final String SHELF = "shelf";
		public static final String SLOT = "slot";
		public static final String PORT = "port";
		public static final String LAYER = "layer";
		public static final String AID = "aid";
		public static final String VALID = "valid";
		public static final String PRIMARYSTATE = "primaryState";
		public static final String SIGTYPE = "signalingType";
		public static final String CONSTRAIN = "constrain";
		public static final String TNA = "tna";
		public static final String SITE = "siteId";
		/*
		 * I'm creating these keys here, instead of simply using the equivalent in
		 * DbNetworkElement, in order to avoid a collision in similarly named
		 * strings. e.g. NE 'port' and Facility 'port'
		 */
		public static final String NEID_FOR_FAC = "neidForFac";
		public static final String NEIP_FOR_FAC = "neipForFac";
		public static final String NEPORT_FOR_FAC = "neportForFac";
		// Special keys for search
		public static final String TNA_SET = "tna_set";

		// Keys in the map

		public static final String COST = "cost";
		public static final String DOMAIN = "domain";
		public static final String GROUP = "group";
		public static final String FAC_NEID = "neidForFac";
		public static final String USER_LABEL = "userLabel";
		public static final String METRIC = "metric";
		public static final String SRLG = "srlg";
		public static final String TYPE = "type";
		public static final String MTU = "mtu";
		public static final String VCAT = "vcat";
		public static final String APSID = "apsId";
		public static final String WAVELENGTH = "wavelength";

		private NetworkElementFacilityCols() {
			super();
		}
	}

	public static final class SitesCols {
		public static final String ID = "id";
		public static final String LOCATION = "location";
		public static final String DESCRIPTION = "description";

		private SitesCols() {
			super();
		}

	}

	// TODO: Move / create better place for these constants in the common module.
	// Moved these two values out of database module so
	// we can break the admin consoles dependency on the database module.
	public static final String STARTTIME_LESSTHAN_EQUALTO = "starttime_lessthan_equalto";
	public static final String ENDTIME_GREATERTHAN_EQUALTO = "endtime_greaterthan_equalto";

}
