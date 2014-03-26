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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1wrapper;

import java.util.Vector;

public final class Parameter {
	private String normalizationLabel;
	public String protocolLabel;
	public String value;
	public String returnType;
	public boolean positional;
	public boolean rangeable;
	public boolean listable;
	public boolean isOptional;
	public Vector<String> possibleValues = new Vector<String>();

	/**
     * 
     */
	public Parameter() {
		super();
	}

	/**
     * 
     */
	public Parameter(String tlabel) {
		protocolLabel = tlabel;
	}

	/**
     * 
     */
	public Parameter(String tlabel, boolean tisOptional, boolean tisRangable,
	    boolean tisListable, String plabel) {
		protocolLabel = tlabel;
		isOptional = tisOptional;
		rangeable = tisRangable;
		listable = tisListable;
		normalizationLabel = plabel;
	}

	/**
     * 
     */
	public Parameter(String tlabel, boolean tisOptional, boolean tisRangable,
	    boolean tisListable, String tmedLabel, String value) {
		protocolLabel = tlabel;
		isOptional = tisOptional;
		rangeable = tisRangable;
		listable = tisListable;
		normalizationLabel = tmedLabel;
		// possibleValues.addElement(value);
		// value.toString();
	}

	/**
     * 
     */
	public Parameter(String tlabel, boolean tisOptional, boolean tisRangable,
	    boolean tisListable, String tmedLabel, Vector value) {
		protocolLabel = tlabel;
		isOptional = tisOptional;
		rangeable = tisRangable;
		listable = tisListable;
		normalizationLabel = tmedLabel;
		value.toString();
	}

	/**
     * 
     */
	public Parameter(String tlabel, boolean tisOptional, boolean tisRangable,
	    boolean tisListable, Vector value) {
		protocolLabel = tlabel;
		isOptional = tisOptional;
		rangeable = tisRangable;
		listable = tisListable;
		// possibleValues.addElement(value);
		value.toString();
	}

	/**
     * 
     */
	public Parameter(String tlabel, boolean tisOptional, String tmedLabel) {
		protocolLabel = tlabel;
		isOptional = tisOptional;
		normalizationLabel = tmedLabel;
	}

	/**
     * 
     */
	public Parameter(String tlabel, String label) {
		protocolLabel = tlabel;
		normalizationLabel = label;
	}

	/**
	 * Append the latest value to the Vector
	 */
	public void addNewPossibleValue(String newValue) {
		possibleValues.addElement(newValue);
	}

	/**
     * 
     */
	public String getLabel() {
		return protocolLabel;
	}

	/**
     * 
     */
	public java.lang.String getNormalizationLabel() {
		return normalizationLabel;
	}

	/**
     * 
     */
	public java.lang.String getProtocolLabel() {
		return protocolLabel;
	}

	/**
     * 
     */
	public Vector getValues() {
		return possibleValues;
	}

	/**
     * 
     */
	public String setListable(String isListable) {
		listable = Boolean.getBoolean(isListable);
		return isListable;
	}

	/**
     * 
     */
	public void setNormalizationLabel(java.lang.String newNormalizationLabel) {
		normalizationLabel = newNormalizationLabel;
	}

	/**
     * 
     */
	public String setOptional(String isOptional) {
		this.isOptional = Boolean.getBoolean(isOptional);
		return isOptional;
	}

	/**
     * 
     */
	public String setPositional(String isPositional) {
		positional = Boolean.getBoolean(isPositional);
		return isPositional;
	}

	/**
     * 
     */
	public String setProtocolLabel(String label) {
		return protocolLabel = label;
	}

	/**
     * 
     */
	public String setRangeable(String isRangeable) {
		rangeable = Boolean.getBoolean(isRangeable);
		return isRangeable;
	}

	/**
     * 
     */
	public String setReturnType(String returnType) {
		return this.returnType = returnType;
	}

	/**
     * 
     */
	public String setValue(String value) {
		return this.value = value;
	}
}
