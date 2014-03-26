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

package com.nortel.appcore.app.drac.server.webserver.struts.common;

import java.util.Map;

import com.nortel.appcore.app.drac.common.utility.StringParser;

public class IndexedTreeNode {
	String name;
	String label;
	Map<String, String> clientDataMap;
	String subStringHighlight;
	int idx;
	int parentIdx;
	Boolean isGroup = true; // otherwise, is a user or resource group

	public IndexedTreeNode(String name, int parentIdx, int idx, Boolean isGroup) {
		this(name, parentIdx, idx, isGroup, null);
	}

	public IndexedTreeNode(String name, int parentIdx, int idx, Boolean isGroup,
	    String label) {
		this.name = name;
		this.label = label;
		this.idx = idx;
		this.parentIdx = parentIdx;
		this.isGroup = isGroup;
	}

	public Map<String, String> getClientDataMap() {
		return clientDataMap;
	}

	public String getClientSafeLabel() {
		// [1] The client uses single quote to reference bean string values
		// [2] Special chars are encoded the same way for both XML and HTML
		return StringParser.encodeForXMLSpecialChars(StringParser
		    .escapeForClient(getLabel()));
	}

	public String getClientSafeName() {
		// [1] The client uses single quote to reference bean string values
		// [2] Special chars are encoded the same way for both XML and HTML
		return DracHelper.subStringHighlight(StringParser
		    .encodeForXMLSpecialChars(subStringHighlight), StringParser
		    .encodeForXMLSpecialChars(StringParser.escapeForClient(getName())));
	}

	public int getIdx() {
		return this.idx;
	}

	public Boolean getIsGroup() {
		return this.isGroup;
	}

	public String getLabel() {
		return this.label;
	}

	public String getName() {
		return this.name;
	}

	public int getParentIdx() {
		return this.parentIdx;
	}

	// Should be called: getURLSafeName ... used for queries/actions posted via
	// URL
	public String getWebSafeName() throws Exception {
		return DracHelper.encodeToUTF8(this.name);
	}

	public void setClientDataMap(Map<String, String> map) {
		this.clientDataMap = map;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public void setIsGroup(Boolean isUser) {
		this.isGroup = isUser;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParentIdx(int idx) {
		this.parentIdx = idx;
	}

	public void setSubStringHighlight(String subStringHighlight) {
		this.subStringHighlight = subStringHighlight;
	}
}
