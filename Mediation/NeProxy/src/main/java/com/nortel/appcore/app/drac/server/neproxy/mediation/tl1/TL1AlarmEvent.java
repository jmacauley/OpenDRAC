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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;

public final class TL1AlarmEvent {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private NetworkElementInfo neInfo;
	/* REPT-ALM, REPT-EQPT, etc... */
	private Tl1CommandCode commandCode;
	/* atag */
	private int ctag;
	/* alarm or event - good to have */
	private boolean isAlarm = true;
	/*
	 * alarm payload could have multiple rows. Each row is a hash map of key/value
	 */
	private List<Map<String, String>> payloads;
	private List<String> textBlocks;
	private String sid;
	private String occrDate;
	private String occrTime;
	private final List<String> comment = new ArrayList<String>();
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
	    "yy-MM-dd HH:mm:ss");

	// private TL1LineParser tl1Parser;

	public TL1AlarmEvent(NetworkElementInfo ne) {
		neInfo = ne;
		Date date = new Date();
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yy-MM-dd");
		occrDate = simpledateformat.format(date);
		simpledateformat = new SimpleDateFormat("HH:mm:ss");
		occrTime = simpledateformat.format(date);
	}

	public Tl1CommandCode getCommandCode() {
		return commandCode;
	}

	/**
	 * @return the comment
	 */
	public List<String> getComment() {
		return comment;
	}

	public int getCtag() {
		return ctag;
	}

	public String getDate() {
		return occrDate;
	}

	public Date getFormattedDate() {
		if (occrDate == null || occrTime == null) {
			return new Date();
		}
		try {
			return dateFormat.parse(occrDate + " " + occrTime);
		}
		catch (ParseException exception) {
			log.error("Failed to parse date " + occrDate + " " + occrTime
			    + " defaulting to current date & time ", exception);
			return new Date();
		}
	}

	public NetworkElementInfo getNEInfo() {
		return neInfo;
	}

	public List<Map<String, String>> getPayloads() {
		return payloads;
	}

	public String getSid() {
		return sid;
	}

	public String[] getTextBlocks() {
		return textBlocks.toArray(new String[textBlocks.size()]);
	}

	public String getTime() {
		return occrTime;
	}

	public boolean isAlarm() {
		return isAlarm;
	}

	public void setCommandCode(Tl1CommandCode newCommandCode) {
		commandCode = newCommandCode;
	}

	public void setCtag(int newCtag) {
		ctag = newCtag;
	}

	public void setDate(String newDate) {
		occrDate = newDate;
	}

	public void setIsAlarm(boolean newIsAlarm) {
		isAlarm = newIsAlarm;
	}

	public void setNEInfo(NetworkElementInfo newNeInfo) {
		neInfo = newNeInfo;
	}

	public void setPayloads(List<Map<String, String>> newPayloads) {
		payloads = newPayloads;
		parsePayloads();
	}

	public void setSid(String newSid) {
		sid = newSid;
	}

	public void setTextBlocks(String[] blocks) {
		if (textBlocks == null) {
			textBlocks = new ArrayList<String>();
		}
		for (String block : blocks) {
			textBlocks.add(block);
		}
	}

	public void setTime(String newTime) {
		occrTime = newTime;
	}

	// public void setTL1Parser(TL1LineParser parser)
	// {
	// tl1Parser = parser;
	// }

	@Override
	public String toString() {
		return "TL1AlarmEvent from " + neInfo.toString() + " code:" + commandCode
		    + " ctag:" + ctag + " isAlarm:" + isAlarm + " payloads:" + payloads
		    + " textBlocks:" + textBlocks + " sid:" + sid + " date:" + occrDate
		    + " " + occrTime + " comment:" + comment;

	}

	private void parsePayloads() {
		if (payloads != null) {
			for (Map<String, String> data : payloads) {
				data.put(ObjectElement.OPERATION_KEY, commandCode.toString());
				data.put("neId", neInfo.getNeID());
			}
		}
	}
}
