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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on Dec 5, 2005
 * 
 * @author nguyentd
 */
public class PathType implements Serializable {
  public enum PROTECTION_TYPE {
    UNPROTECTED, PATH1PLUS1
  }

  private static final long serialVersionUID = 1;
  /**
   * The the identification of the source Network Element
   */
  private String source = "00-00-00-00-00-00";
  /**
   * The the identification of the destination Network Element
   */
  private String target = "00-00-00-00-00-0";

  /**
   * The identification of the endpoint where the path starts
   */
  private EndPointType sourceEndPoint = new EndPointType();

  /**
   * The identification of the endpoint where the path ends
   */
  private EndPointType targetEndPoint = new EndPointType();

  /**
   * Specify the bandwidth, in Mbits per second, of the path
   */
  private int rate;

  /**
   * Specify the Shared Risk Link Group. The String can contain zero or more
   * SRLG values and they are commas separated. For example: srlg="2, 4, 1"
   */
  private String srlg = "";

  /**
   * For cases in which a service can optionally be routed as VCAT (otherwise,
   * CCAT). This is the case for L2SS whereby the WAN facility is created during
   * service activation
   */
  // GGL2
  private boolean vcatRoutingOption;

  /**
   * Specify the cost
   */
  private int cost = -1;
  private int metric = -1;
  private int hop = -1;

  // Specify the routing metric to use
  private int routingMetric;

  private PROTECTION_TYPE protectionType = PROTECTION_TYPE.UNPROTECTED;

  /**
   * Shared Risk Service Group is the list of service ids that user wants to
   * create a new service that is diversely routed from the services listed in
   * the Shared Risk Service Group
   */
  private String sharedRiskServiceGroup = "";

  /**
   * The srlgInclusions is a comma separated list of SRLG values of the tandem
   * nodes in the Path. example: srlg="2, 4, 1"
   */
  private String srlgInclusions = "";
  private String srcVlanId;
  private String dstVlanId;
  private final Map<String, String> nsvMap = new HashMap<String, String>();

  public PathType() {
    super();
  }

  /**
   * @return the cost
   */
  public int getCost() {
    return cost;
  }

  public String getDstVlanId() {
    return this.dstVlanId;
  }

  /**
   * @return the hop
   */
  public int getHop() {
    return hop;
  }

  /**
   * @return the metric
   */
  public int getMetric() {
    return metric;
  }

  public Map<String, String> getNsvMap() {
    return new HashMap<String, String>(nsvMap);
  }

  /**
   * @return the protectionType
   */
  public PROTECTION_TYPE getProtectionType() {
    return protectionType;
  }

  /**
   * @return the rate
   */
  public int getRate() {
    return rate;
  }

  /**
   * @return the routingMetric
   */
  public int getRoutingMetric() {
    return routingMetric;
  }

  /**
   * @return the sharedRiskServiceGroup
   */
  public String getSharedRiskServiceGroup() {
    return sharedRiskServiceGroup;
  }

  /**
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * @return the sourceEndPoint
   */
  public EndPointType getSourceEndPoint() {
    return sourceEndPoint;
  }

  public String getSrcVlanId() {
    return this.srcVlanId;
  }

  /**
   * @return the srlg
   */
  public String getSrlg() {
    return srlg;
  }

  /**
   * @return the srlgInclusions
   */
  public String getSrlgInclusions() {
    return srlgInclusions;
  }

  /**
   * @return the target
   */
  public String getTarget() {
    return target;
  }

  /**
   * @return the targetEndPoint
   */
  public EndPointType getTargetEndPoint() {
    return targetEndPoint;
  }

  public boolean getVcatRoutingOption() {
    return this.vcatRoutingOption;
  }

  /**
   * @param cost
   *          the cost to set
   */
  public void setCost(int cost) {
    this.cost = cost;
  }

  public void setDstVlanId(String vlanId) {
    this.dstVlanId = vlanId;
  }

  /**
   * @param hop
   *          the hop to set
   */
  public void setHop(int hop) {
    this.hop = hop;
  }

  /**
   * @param metric
   *          the metric to set
   */
  public void setMetric(int metric) {
    this.metric = metric;
  }

  public void setNsvMap(Map<String, String> map) {
    if (map != null) {
      nsvMap.putAll(map);
    }
  }

  /**
   * @param protectionType
   *          the protectionType to set
   */
  public void setProtectionType(PROTECTION_TYPE protectionType) {
    this.protectionType = protectionType;
  }

  /**
   * @param rate
   *          the rate to set
   */
  public void setRate(int rate) {
    this.rate = rate;
  }

  /**
   * @param routingMetric
   *          the routingMetric to set
   */
  public void setRoutingMetric(int routingMetric) {
    this.routingMetric = routingMetric;
  }

  /**
   * @param sharedRiskServiceGroup
   *          the sharedRiskServiceGroup to set
   */
  public void setSharedRiskServiceGroup(String sharedRiskServiceGroup) {
    this.sharedRiskServiceGroup = sharedRiskServiceGroup;
  }

  /*
   * NOTE: The set/get methods for vlanId are implemented here on the Path data
   * structure ... not the source and dest Endpoint structures. The reason for
   * this: when a request is received by lpcp, a lookup is done within the
   * lpcp's endpoint cache, and those cache endpoints are then referenced by the
   * path structure of the client request. Consequently, there can be no
   * per-request client data members in the endpoint structures, because these
   * would end up being copied into the cache.
   */

  /**
   * @param source
   *          the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * @param sourceEndPoint
   *          the sourceEndPoint to set
   */
  public void setSourceEndPoint(EndPointType sourceEndPoint) {
    this.sourceEndPoint = sourceEndPoint;
  }

  public void setSrcVlanId(String vlanId) {
    this.srcVlanId = vlanId;
  }

  /**
   * @param srlg
   *          the srlg to set
   */
  public void setSrlg(String srlg) {
    this.srlg = srlg;
  }

  /**
   * @param srlgInclusions
   *          the srlgInclusions to set
   */
  public void setSrlgInclusions(String srlgInclusions) {
    this.srlgInclusions = srlgInclusions;
  }

  /**
   * @param target
   *          the target to set
   */
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * @param targetEndPoint
   *          the targetEndPoint to set
   */
  public void setTargetEndPoint(EndPointType targetEndPoint) {
    this.targetEndPoint = targetEndPoint;
  }

  public void setVcatRoutingOption(boolean b) {
    this.vcatRoutingOption = b;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PathType [source=");
    builder.append(source);
    builder.append(", target=");
    builder.append(target);
    builder.append(", sourceEndPoint=");
    builder.append(sourceEndPoint);
    builder.append(", targetEndPoint=");
    builder.append(targetEndPoint);
    builder.append(", rate=");
    builder.append(rate);
    builder.append(", srlg=");
    builder.append(srlg);
    builder.append(", vcatRoutingOption=");
    builder.append(vcatRoutingOption);
    builder.append(", cost=");
    builder.append(cost);
    builder.append(", metric=");
    builder.append(metric);
    builder.append(", hop=");
    builder.append(hop);
    builder.append(", routingMetric=");
    builder.append(routingMetric);
    builder.append(", protectionType=");
    builder.append(protectionType);
    builder.append(", sharedRiskServiceGroup=");
    builder.append(sharedRiskServiceGroup);
    builder.append(", srlgInclusions=");
    builder.append(srlgInclusions);
    builder.append(", srcVlanId=");
    builder.append(srcVlanId);
    builder.append(", dstVlanId=");
    builder.append(dstVlanId);
    builder.append(", nsvMap=");
    builder.append(nsvMap);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + cost;
    result = prime * result + ((dstVlanId == null) ? 0 : dstVlanId.hashCode());
    result = prime * result + hop;
    result = prime * result + metric;
    result = prime * result + ((nsvMap == null) ? 0 : nsvMap.hashCode());
    result = prime * result
        + ((protectionType == null) ? 0 : protectionType.hashCode());
    result = prime * result + rate;
    result = prime * result + routingMetric;
    result = prime
        * result
        + ((sharedRiskServiceGroup == null) ? 0 : sharedRiskServiceGroup
            .hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    result = prime * result
        + ((sourceEndPoint == null) ? 0 : sourceEndPoint.hashCode());
    result = prime * result + ((srcVlanId == null) ? 0 : srcVlanId.hashCode());
    result = prime * result + ((srlg == null) ? 0 : srlg.hashCode());
    result = prime * result
        + ((srlgInclusions == null) ? 0 : srlgInclusions.hashCode());
    result = prime * result + ((target == null) ? 0 : target.hashCode());
    result = prime * result
        + ((targetEndPoint == null) ? 0 : targetEndPoint.hashCode());
    result = prime * result + (vcatRoutingOption ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PathType other = (PathType) obj;
    if (cost != other.cost)
      return false;
    if (dstVlanId == null) {
      if (other.dstVlanId != null)
        return false;
    }
    else if (!dstVlanId.equals(other.dstVlanId))
      return false;
    if (hop != other.hop)
      return false;
    if (metric != other.metric)
      return false;
    if (nsvMap == null) {
      if (other.nsvMap != null)
        return false;
    }
    else if (!nsvMap.equals(other.nsvMap))
      return false;
    if (protectionType != other.protectionType)
      return false;
    if (rate != other.rate)
      return false;
    if (routingMetric != other.routingMetric)
      return false;
    if (sharedRiskServiceGroup == null) {
      if (other.sharedRiskServiceGroup != null)
        return false;
    }
    else if (!sharedRiskServiceGroup.equals(other.sharedRiskServiceGroup))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    }
    else if (!source.equals(other.source))
      return false;
    if (sourceEndPoint == null) {
      if (other.sourceEndPoint != null)
        return false;
    }
    else if (!sourceEndPoint.equals(other.sourceEndPoint))
      return false;
    if (srcVlanId == null) {
      if (other.srcVlanId != null)
        return false;
    }
    else if (!srcVlanId.equals(other.srcVlanId))
      return false;
    if (srlg == null) {
      if (other.srlg != null)
        return false;
    }
    else if (!srlg.equals(other.srlg))
      return false;
    if (srlgInclusions == null) {
      if (other.srlgInclusions != null)
        return false;
    }
    else if (!srlgInclusions.equals(other.srlgInclusions))
      return false;
    if (target == null) {
      if (other.target != null)
        return false;
    }
    else if (!target.equals(other.target))
      return false;
    if (targetEndPoint == null) {
      if (other.targetEndPoint != null)
        return false;
    }
    else if (!targetEndPoint.equals(other.targetEndPoint))
      return false;
    if (vcatRoutingOption != other.vcatRoutingOption)
      return false;
    return true;
  }
	
    public boolean simpleEquals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    PathType other = (PathType) obj;
	    
	    if (dstVlanId == null) {
		    if (other.dstVlanId != null)
			    return false;
	    } 
	    if (rate != other.rate)
		    return false;

	    if (sourceEndPoint == null) {
		    if (other.sourceEndPoint != null)
			    return false;
	    } else if (!sourceEndPoint.simpleEquals(other.sourceEndPoint))
		    return false;


	    if (targetEndPoint == null) {
		    if (other.targetEndPoint != null)
			    return false;
	    } else if (!targetEndPoint.simpleEquals(other.targetEndPoint))
		    return false;

	    return true;
    }

}
