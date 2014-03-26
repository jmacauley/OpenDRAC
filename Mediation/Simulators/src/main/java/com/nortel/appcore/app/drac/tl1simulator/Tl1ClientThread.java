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

package com.nortel.appcore.app.drac.tl1simulator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.StringParser;

/**
 * Simulates a single TL1 NE using local data files for canned responses.
 * Invoked by the CheeseNESimulator
 *
 * @author pitman
 */
public final class Tl1ClientThread extends Thread {
	private final Logger log = LoggerFactory.getLogger(getClass());

	/*
	 * TimerTask that when executed fires a TL1 AO event message.
	 */
	class sendAO extends TimerTask {
		private final List<String> ao;
		private int aoNumber;
		private int lastIdx = -1;

		public sendAO(List<String> aoList) {
			ao = aoList;
		}

		@Override
		public void run() {
			try {
				aoNumber++;
				lastIdx++;

				if (lastIdx >= ao.size()) {
					// wrap around
					lastIdx = 0;
				}
				StringBuilder resp = new StringBuilder();
				resp.append(getHeader());
				Formatter formatter = new Formatter(resp);
				formatter.format("A  %010d %s\r\n;\r\n", Integer.valueOf(aoNumber),
				    ao.get(lastIdx));
				String aoMessage = resp.toString();
				log.info("Generating AO message " + resp);
				client.getOutputStream().write(aoMessage.getBytes());
				client.getOutputStream().flush();
			}
			catch (Exception e) {
				if (!stop) {
					log.error("Error: ", e);
				}
			}
		}
	}

	private static final String DEFAULT_AO = "REPT ALM OC192\r\n    \"OC192-1-11-1:CL,DCC_FAIL_L,NSA,04-08,13-36-57,NEND,RCV:\\\"LINE DCC Link Failure\\\",SDH:0100000020-6080-0452,:YEAR=2010,MODE=SDH\"";
	private boolean stop;
	private final Socket client;
	private final String type;
	private final String tid;
	private final Timer aoTimer;
	private final List<String> resources = new ArrayList<String>();
	private final String name;

	public Tl1ClientThread(Socket clientSocket, String neType) throws Exception {
		client = clientSocket;
		type = neType;
		List<String> toks = StringParser.split(type, '#', false);
		tid = toks.get(toks.size() - 1).replace("!", "");

		String r = type.replace('#', '/').replace("!", "");
		resources.add(r);
		while (r.contains("/")) {
			r = r.substring(0, r.lastIndexOf('/'));
			resources.add(r);
		}

		name = client.getInetAddress().getHostAddress() + ":"
		    + client.getLocalPort() + " of type " + type + " tid " + tid;
		setName("Tl1 simulator thread for client: " + name);

		/*
		 * If the type string contains a ! then we want this simulator to emit
		 * events. The more then ! in the string the faster we'll emit them.
		 */
		List<String> aoList = new ArrayList<String>();
		if (type.indexOf('!') != -1) {

			/**
			 * Look for AO messages in files AO-1.tl1, AO-2.tl1 stop when we find no
			 * more.
			 */
			int aoCounter = 1;
			while (true) {
				String ao = lookup("AO-" + aoCounter);
				if (ao == null) {
					break;
				}
				aoCounter++;
				aoList.add(ao);
			}

			if (aoList.isEmpty()) {
				// We've asked to generate alarms but can't find any, provide a hard
				// coded one.
				aoList.add(DEFAULT_AO);
			}

			int count = 0;
			for (char c : type.toCharArray()) {
				if (c == '!') {
					count++;
				}
			}
			sendAO tt = new sendAO(aoList);
			aoTimer = new Timer("AO timer for " + name);
			long initialDelay = 1000;

			if (count == 1) {
				aoTimer.schedule(tt, initialDelay, 10 * 1000);
			}
			else if (count == 2) {
				aoTimer.schedule(tt, initialDelay, 5 * 1000);
			}
			else if (count == 3) {
				aoTimer.schedule(tt, initialDelay, 2 * 1000);
			}
			else if (count == 4) {
				aoTimer.schedule(tt, initialDelay, 1000);
			}
			else if (count == 5) {
				aoTimer.schedule(tt, initialDelay, 500);
			}
			else if (count > 5) {
				aoTimer.schedule(tt, initialDelay, 10);
			}
		}
		else {
			aoTimer = null;

		}
		log.debug("TL1Client created: Will search for resources in "
		    + resources.toString() + " found " + aoList.size()
		    + " AO events to generate");
	}

	@Override
	public void run() {
		log.info("Simulator running " + type);
		try {
			/* client.getOutputStream().write(
			    "\r\nWelcome to the TL1 Simulator\r\n\r\n< ".getBytes()); */
            client.getOutputStream().write("\r\n< ".getBytes());
			BufferedReader br = new BufferedReader(new InputStreamReader(
			    client.getInputStream()), 1024 * 4);
			StringBuilder sb = new StringBuilder();
			while (!stop) {
				int r = br.read();
				if (r == -1) {
					// end of file
					terminate();
					return;
				}

				char c = (char) r;
				sb.append(c);
				if (c == ';') {
					// got a block of tl1 stuff
					processSomething(sb);
					// start again
					sb.setLength(0);
				}
			}
		}
		catch (Exception t) {
			if (!stop) {
				log.error("Error: ", t);
			}
		}
		// just in case.
		terminate();
	}

	public synchronized void terminate() {
		log.info("Terminating client");
		try {
			stop = true;
			if (aoTimer != null) {
				aoTimer.cancel();
			}
			client.close();
			this.interrupt();
		}
		catch (Exception e) {
			log.info("ignoreing closing client socket", e);
		}
	}

	private String getHeader() {
		// Year-Month-Day Hour:min-Sec
		// date=09-01-15 17:00:00
		String date = new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(new Date());
		return "\r\n   " + tid + " " + date + "\r\n";
	}

	private String getTl1ResponseToCommand(StringBuilder b) throws Exception {

		try {
			String command = b.substring(0, b.indexOf(":")).trim();
			//
			// Atag follows the third colon;

			int pos = b.indexOf(":");
			pos = b.indexOf(":", pos + 1);
			pos = b.indexOf(":", pos + 1);
			String atag = b.substring(pos + 1);
			int end = atag.length();
			if (atag.indexOf(':') >= 0) {
				end = atag.indexOf(':');
			}
			if (atag.indexOf(';') >= 0 && atag.indexOf(';') < end) {
				end = atag.indexOf(';');
			}

			atag = atag.substring(0, end);
			//

			// String atag = "2";

			if ("RTRV-HDR".equals(command)) {
				// short circuit, this occurs often.
				return getHeader() + "M  " + atag + " COMPLD\r\n;\r\n<";
			}

			// look up command.
			String resp = lookup(command);
			if (resp != null) {
				return getHeader() + "M  " + atag + " COMPLD\r\n" + resp + "\r\n;\r\n<";
			}
			//
			return getHeader() + "M  " + atag + " COMPLD\r\n;\r\n<";

		}
		catch (Exception e) {
			// return a generic error response.
			return getHeader() + "M  0 DENY\r\n;IITA\r\n   /* error " + e.toString()
			    + " */\r\n;\r\n<";
		}
	}

	/**
	 * Look up the results for the given tl1 command code. We look for a file
	 * containing the command in our resource directory, first at the most
	 * specific level (NETYPE-RELEASE-TID), then (NETYPE-RELEASE), then (NETYPE)
	 * then ("").
	 */
	private String lookup(String command) throws Exception {
		String target;
		for (String r : resources) {
			target = r + "/" + command + ".tl1";
			InputStream i = Tl1ClientThread.class.getClassLoader()
			    .getResourceAsStream(target);
			if (i != null) {
				// found it
				StringBuilder sb = new StringBuilder();
				BufferedReader br = null;
				try {
					br = new BufferedReader(new InputStreamReader(i));
					char[] buf = new char[1024];
					int len = br.read(buf);
					while (len > 0) {
						sb.append(buf, 0, len);
						len = br.read(buf);
					}
				}
				finally {
					if (br != null) {
						br.close();
					}
				}

				if (sb.length() > 0) {
					// We need to make sure every line ends with the required
					// '\r\n' characters.
					Pattern p = Pattern.compile("[\\r\\n]");
					String[] result = p.split(sb.toString(), 0);
					StringBuilder fixed = new StringBuilder();

					for (int j = 0; j < result.length; j++) {
						if (result[j].length() > 0) {
							fixed.append(result[j]);
							fixed.append("\r\n");
						}
					}

					return fixed.toString();
				}
				log.info("Empty response for <" + target);
				return null;
			}
		}
		log.info("did not find <" + command + "> in " + resources);
		return null;
	}

	private void processSomething(StringBuilder b) throws Exception {
		log.info("Received " + b.toString().trim());
		String resp = getTl1ResponseToCommand(b);
		log.info("Responding with " + resp.trim());
		client.getOutputStream().write(resp.getBytes());
		client.getOutputStream().flush();
	}
}
