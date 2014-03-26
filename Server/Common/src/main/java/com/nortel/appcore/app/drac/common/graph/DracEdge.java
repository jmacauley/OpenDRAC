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

package com.nortel.appcore.app.drac.common.graph;

import java.awt.Paint;
import java.io.Serializable;

/**
 * DracEdge : A graph edge representing topological link between two DracVertexs
 * (NEs).
 * 
 * @author pitman
 */
public final class DracEdge implements Serializable {
  private static final long serialVersionUID = 1L;
  private final DracVertex source;
  private final DracVertex target;
  private final String sourceAid;
  private final String targetAid;
  private final Double weight;
  private final String id;
  private String cost;
  private String metric2;
  private String srlg;

  private Integer sourceChannel;
  private Integer targetChannel;
  private String ingressip;
  private String egressip;
  private Paint paintColor;

  private boolean isManual = false;

  // this network link has an underlying/eclipsed manual link equivalent
  private boolean hasEclipsedManualLink = false;

  public DracEdge(DracVertex src, String srcAid, DracVertex dest,
      String destAid, Double edgeWeight, String edgeCost, String edgeMetric2,
      String edgeSrlg, String edgeId) {
    source = src;
    sourceAid = srcAid;
    target = dest;
    targetAid = destAid;
    weight = edgeWeight;
    cost = edgeCost;
    metric2 = edgeMetric2;
    srlg = edgeSrlg;
    id = edgeId;
  }

  @Override
  // WARNING: Implemented for bidirectional equality!
  public boolean equals(Object o) {
    if (o instanceof DracEdge) {
      DracEdge other = (DracEdge) o;
      if ((source.equals(other.getSource())
          && sourceAid.equalsIgnoreCase(other.getSourceAid())
          && target.equals(other.getTarget())
          && targetAid.equalsIgnoreCase(other.getTargetAid())

      || source.equals(other.getTarget())
          && sourceAid.equalsIgnoreCase(other.getTargetAid())
          && target.equals(other.getSource())
          && targetAid.equalsIgnoreCase(other.getSourceAid()))

          && this.isManual() == other.isManual()

          && this.hasEclipsedManualLink() == other.hasEclipsedManualLink()) {
        return true;
      }
    }

    return false;
  }

  public String getCost() {
    return cost;
  }

  public String getEgressIp() {
    return egressip;
  }

  public String getID() {
    return id;
  }

  public String getIngressIp() {
    return ingressip;
  }

  public String getMetric() {
    return metric2;
  }

  /**
   * Used to remember what color to paint this edge instead of the default. Only
   * set in the DRAC Gui.
   */
  public Paint getPaintColor() {
    return paintColor;
  }

  public DracVertex getSource() {
    return source;
  }

  public String getSourceAid() {
    return sourceAid;
  }

  public Integer getSourceChannel() {
    return sourceChannel;
  }

  public String getSrlg() {
    return srlg;
  }

  public DracVertex getTarget() {
    return target;
  }

  public String getTargetAid() {
    return targetAid;
  }

  public Integer getTargetChannel() {
    return targetChannel;
  }

  public Double getWeight() {
    return weight;
  }

  public boolean hasEclipsedManualLink() {
    return this.hasEclipsedManualLink;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (source == null ? 0 : source.hashCode());
    result = prime * result + (sourceAid == null ? 0 : sourceAid.hashCode());
    result = prime * result + (target == null ? 0 : target.hashCode());
    result = prime * result + (targetAid == null ? 0 : targetAid.hashCode());
    result = prime * result + Boolean.valueOf(isManual).hashCode();

    // don't hash on hasEclipsedManualLink. The topo consolidation algorithm
    // modifies this value after having been added to the graph.
    return result;
  }

  public boolean isManual() {
    return isManual;
  }

  public void setCost(String edgeCost) {
    cost = edgeCost;
  }

  public void setEclipsedManualLink(boolean eclipsed) {
    this.hasEclipsedManualLink = eclipsed;
  }

  public void setEgressIp(String eip) {
    egressip = eip;
  }

  public void setIngressIp(String ing) {
    ingressip = ing;
  }

  public void setManual(boolean isManual) {
    this.isManual = isManual;
  }

  public void setMetric(String edgeMetric) {
    metric2 = edgeMetric;
  }

  /**
   * Used to remember what color to paint this edge instead of the default. Only
   * set in the DRAC Gui.
   */
  public void setPaintColor(Paint paintColor) {
    this.paintColor = paintColor;
  }

  public void setSourceChannel(Integer src) {
    sourceChannel = src;
  }

  public void setSrlg(String edgeSlrg) {
    srlg = edgeSlrg;
  }

  public void setTargetChannel(Integer tar) {
    targetChannel = tar;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("ID: ");
    sb.append(id);
    sb.append(" weight: ");
    sb.append(weight);
    sb.append(" cost: ");
    sb.append(cost);
    sb.append(" metric2: ");
    sb.append(metric2);
    sb.append(" SRLG: ");
    sb.append(srlg);
    sb.append(" SRC: [");
    sb.append(source.getIeee());
    sb.append(", ");
    sb.append(sourceAid);
    sb.append("] ");
    sb.append(" TGT: [");
    sb.append(target.getIeee());
    sb.append(", ");
    sb.append(targetAid);
    sb.append("]");

    return sb.toString();
  }

}
