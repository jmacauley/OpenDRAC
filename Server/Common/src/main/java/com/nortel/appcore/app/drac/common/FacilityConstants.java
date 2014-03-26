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

package com.nortel.appcore.app.drac.common;

/**
 * Facility This class represents a facility object.
 * 
 * @author adlee
 * @since 2005-11-08
 */

public final class FacilityConstants {

  public enum SIGNAL_TYPE {
    UNI, INNI, ENNI, OSS, unassigned;
  }

  public static final String OC1 = "OC1";
  public static final String OC3 = "OC3";
  public static final String OC12 = "OC12";
  public static final String OC48 = "OC48";
  public static final String OC192 = "OC192";
  public static final String STM1 = "STM1";
  public static final String STM2 = "STM2";
  public static final String STM4 = "STM4";
  public static final String STM8 = "STM8";
  public static final String STM16 = "STM16";
  public static final String STM64 = "STM64";
  public static final String LIM = "LIM";
  public static final String CMD = "CMD";
  public static final String WAN = "WAN";
  public static final String ETH = "ETH";
  public static final String LAYER_ATTR = "layer";
  public static final String AID_ATTR = "aid";
  public static final String COST_ATTR = "cost";
  // public static final String GROUP_ATTR = "group";
  public static final String METRIC_ATTR = "metric";
  public static final String PRISTATE_ATTR = "primaryState";
  public static final String SIGTYPE_ATTR = "signalingType";
  public static final String SRLG_ATTR = "srlg";
  public static final String TNA_ATTR = "tna";
  public static final String FACLABEL_ATTR = "userLabel";
  public static final String TYPE_ATTR = "type";
  public static final String ACTUALUNIT_ATTR = "actualUnit";
  public static final String LCAS_ATTR = "lcas";
  public static final String MAPPING_ATTR = "mapping";
  public static final String MODE_ATTR = "mode";
  public static final String PROVUNIT_ATTR = "provUnit";
  public static final String RATE_ATTR = "rate";
  public static final String VCAT_ATTR = "vcat";
  public static final String MTU_ATTR = "mtu";
  public static final String PHYSICALADDRESS_ATTR = "physicalAddress";
  public static final String SPEED_ATTR = "speed";
  public static final String CONSTRAINTS_ATTR = "constrain";
  public static final String INGRESSIP_ATTR = "ingressIp";
  public static final String MEP_ATTR = "mep";
  public static final String WAVELENGTH_ATTR = "wavelength";
  public static final String VCAT_DISABLE_STR = "DISABLE";
  public static final String VCAT_ENABLE_STR = "ENABLE";
  public static final String SITE_ATTR = "siteId";
  public static final String CARDTYPE_ATTR = "CardType";
  public static final String CARDPEC_ATTR = "CardPEC";
  public static final String PORTTYPE_ATTR = "PortType";
  public static final String PORTPEC_ATTR = "PortPEC";

  public static final String DOMAIN_ATTR = "domain";

  // public static final String SIGTYPE_UNI = "UNI";
  // public static final String SIGTYPE_INNI = "INNI";
  // public static final String SIGTYPE_ENNI = "ENNI";
  // public static final String SIGTYPE_OSS = "OSS";

  // a special sigtype defn for searching user endpoints (compound search of UNI
  // and ENNI)
  public static final String SIGTYPE_DRACUSERENDPOINT = "DRACUSERENDPOINT";

  public static final String STS_ATTR = "sts";
  public static final String RATE_NONE = "NONE";

  public static final String DEFAULT_TNA = "N/A";
  public static final String DEFAULT_FACLABEL = "N/A";

  // L2
  public static final String UNTAGGED_LOCLBL_FLAG = "Untagged";
  public static final String UNTAGGED_LOCLBL_VALUE = "4096";
  public static final String ALLTAGGED_LOCLBL_FLAG = "All tagged";
  public static final String ALLTAGGED_LOCLBL_VALUE = "4098";
  public static final int MAX_VLANTAGGED_LOCLBL = 4095;
  public static final int MAX_VCID = 1048575;
  public static final int MIN_WANPORT_anyL2SS = 101;
  public static final int MAX_WANPORT_20GL2SS = 228; // from 101
  public static final String IS_L2SS_FACILITY = "IS_L2SS_FACILITY";
  public static final String L2_MEDIATION_VCS_AID = "l2_vcs_aid";
  public static final String L2_MEDIATION_SOURCE_VCE_AID = "l2_source_vce_aid";
  public static final String L2_MEDIATION_SOURCE_VCEMAP_AID = "l2_source_vcemap_aid";
  public static final String L2_MEDIATION_SOURCE_WAN_AID = "l2_source_wan_aid";
  public static final String L2_MEDIATION_TARGET_VCE_AID = "l2_target_vce_aid";
  public static final String L2_MEDIATION_TARGET_VCEMAP_AID = "l2_target_vcemap_aid";
  public static final String L2_MEDIATION_TARGET_WAN_AID = "l2_target_wan_aid";

  public static final String AN_ATTR = "autoNegotiation";
  public static final String ANSTATUS_ATTR = "autoNegotiationStatus";
  public static final String ANETHDPX_ATTR = "advertisedDuplex";
  public static final String FLOWCTRL_ATTR = "flowControl";
  public static final String TXCOND_ATTR = "txConditioning";
  public static final String PAUSETX_ATTR = "controlPauseTx";
  public static final String PAUSERX_ATTR = "controlPauseRx";
  public static final String NETHDPX_ATTR = "negotiatedDuplex";
  public static final String NSPEED_ATTR = "negotiatedSpeed";
  public static final String NPAUSETX_ATTR = "negotiatedPauseTx";
  public static final String NPAUSERX_ATTR = "negotiatedPauseRx";
  public static final String LPETHDPX_ATTR = "linkPartnerDuplex";
  public static final String LPSPEED_ATTR = "linkPartnerSpeed";
  public static final String LPFLOWCTRL_ATTR = "linkPartnerFlowControl";
  public static final String PASSCTRL_ATTR = "passControlFrame";
  public static final String IFTYPE_ATTR = "interfaceType";
  public static final String POLICING_ATTR = "policing";
  public static final String ETYPE_ATTR = "encapsulationType";
  public static final String PRIORITYMODE_ATTR = "priorityMode";
  public static final String BWTHRESHOLD_ATTR = "bandwidthThreshold";
  public static final String BWREMAIN_ATTR = "remainedBandwidth";
  public static final String BWUTL_ATTR = "bandwidthUtilization";
  public static final String LADID_ATTR = "lagId";

  public static final String IS_EPL = "isEPL";
  public static final String tenGE = "10GE";
  public static final String GE = "GE ";
  public static final String FE = "FE";

  private FacilityConstants() {
    super();
  }

}
