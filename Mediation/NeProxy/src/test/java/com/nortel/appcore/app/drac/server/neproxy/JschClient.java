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

package com.nortel.appcore.app.drac.server.neproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.nortel.appcore.app.drac.common.utility.ExpectableInputReader;

public final class JschClient {
  private static final Logger log = LoggerFactory.getLogger(JschLogger.class);
	/**
	 * Jsch Logger registered with jsch to get more info.
	 */
	static class JschLogger implements com.jcraft.jsch.Logger {
	
		@Override
		public boolean isEnabled(int i) {
			return true;
		}

		@Override
		public void log(int i, String s) {
			// public static final int DEBUG = 0;
			// public static final int INFO = 1;
			// public static final int WARN = 2;
			// public static final int ERROR = 3;
			// public static final int FATAL = 4;

			switch (i) {
			case 0:
				
				break;
			case 1:
				log.debug("Jsch info logger Text: " + s);
				break;
			case 2:
				log.warn("Jsch warn logger Text: " + s);
				break;
			case 3:
				log.error("Jsch error logger Text: " + s);
				break;
			case 4:
				log.error("Jsch fatal logger Text: " + s, new Exception("StackTrace"));
				break;
			default:
				log.debug("Jsch log level:" + i + " logger Text: " + s);
			}
		}
	}

	/**
	 * InputStream that we can pass strings into. Its a slow stream in that the
	 * read method includes a sleep to slow it down
	 */
	static class SlowStringInputStream extends InputStream {
		private final Object lock = new Object();
		private final StringBuilder sb = new StringBuilder();
		private boolean eof;
		private final org.apache.log4j.Logger logger;

		public SlowStringInputStream(org.apache.log4j.Logger l) {
			logger = l;
		}

		public void addInput(String s) {
			
			if (logger != null) {
				logger.debug(s);
			}
			synchronized (lock) {
				sb.append(s);
				lock.notifyAll();
			}
		}

		public void eof() {
			
			eof = true;
		}

		@Override
		public int read() throws IOException {
			byte b;
			while (true) {

				synchronized (lock) {
					// reached the end.
					if (sb.length() == 0 && eof) {
						
						b = -1;
						break;
					}

					// got some thing
					if (sb.length() > 0) {

						String c = sb.substring(0, 1);
						
						sb.deleteCharAt(0);
						b = c.getBytes()[0];
						break;
					}

					/*
					 * We have not been told we have reached eof but yet we have no data
					 * to return, wait on our lock until we are notified data has arrived
					 * or an EOF has been set.
					 */

					try {
						
						lock.wait();
					}
					catch (InterruptedException e) {
						log.error("eh?", e);
					}
				}

			}

			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				log.error("eh?", e);
			}
			return b;
		}

		/**
		 * Reads up to len bytes of data into b. At most len bytes will be read.
		 */
		@Override
		public int read(byte b[], int off, int len) throws IOException {
			log.debug("SlowStringInputStream: readBlock requested off:" + off
			    + " len:" + len);
			if (b == null) {
				throw new NullPointerException();
			}
			else if (off < 0 || len < 0 || len > b.length - off) {
				throw new IndexOutOfBoundsException("off " + off + " len " + len
				    + " length-off " + (b.length - off));
			}
			else if (len == 0) {
				return 0;
			}

			/*
			 * make sure we read at least one byte, blocking if necessary, required to
			 * function as the method expects.
			 */
			int c = read();
			if (c == -1) {
				

				return -1;
			}
			b[off] = (byte) c;

			/*
			 * Stuff what ever data we have in our buffer up to the requested length,
			 * but don't block in order to get more
			 */

			int i = 1;

			synchronized (lock) {
				if (sb.length() > 0) {
					int howMany = Math.min(len - 1, sb.length());
					i += howMany;
					String str = sb.substring(0, howMany);
					log.debug("SlowStringInputStream: reading  '" + str + "' from " + sb
					    + " returning " + i);
					sb.delete(0, howMany);
					System.arraycopy(str.getBytes(), 0, b, off + 1, howMany);
				}
			}

			return i;
		}
	}

	/**
	 * Class that implements the com.jcraft.jsch.UserInfo interface to provide
	 * credentials to jsch.
	 */
	static class SshConnectInfo implements UserInfo {

		private final String pass;

		public SshConnectInfo(String password) {

			pass = password;
		}

		@Override
		public String getPassphrase() {
			return pass;
		}

		@Override
		public String getPassword() {
			return pass;
		}

		@Override
		public boolean promptPassphrase(String arg0) {
			log.debug("promptPassphrase invoked with message:" + arg0);
			return true;
		}

		@Override
		public boolean promptPassword(String arg0) {
			log.debug("promptPassword invoked with message:" + arg0);
			return true;
		}

		@Override
		public boolean promptYesNo(String arg0) {
			log.debug("promptYesNo invoked with message:" + arg0);
			return true;
		}

		@Override
		public void showMessage(String arg0) {
			log.debug("Show Message invoked with message:" + arg0);

		}
	}

	private final JSch jsch;

	public JschClient() {
		jsch = new JSch();
		JSch.setLogger(new JschLogger());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			log.debug("Starting");
			JschClient j = new JschClient();
			j.atCern("wayne", "surftest01.surfnet.nl", args[0], args[1], 22);
			// j.atSerfNet("drac", "145.145.66.230", args[0], args[1], 22);
		}
		catch (Exception t) {
			log.error("Error: ", t);
		}
		log.debug("Done");
	}

	private void atCern(String user, String ip, String password,
	    String secondPassword, int port) throws Exception {
		String prompt = "R1000-S-RFTEC-2#";
		String secondHost = "ssh drac@r1000-s-rftec-2.cern.ch\n";

		Session session = jsch.getSession(user, ip, port);
		session.setUserInfo(new SshConnectInfo(password));
		session.connect();
		Channel c = session.openChannel("shell");
		SlowStringInputStream ssis = new SlowStringInputStream(
		    Force10Logger.getLogger(ip, port));
		c.setInputStream(ssis);
		c.connect();

		ExpectableInputReader eir = new ExpectableInputReader(c.getInputStream(),
		    Force10Logger.getLogger(ip, port), ip + ":" + port);
		eir.setDaemon(true);
		eir.start();
		ssis.addInput("\n");

		// Command prompt looks like "[wayne@surftest01 ~]$ "
		eir.expect(Pattern.compile("^\\[.*\\]\\$", Pattern.MULTILINE), 20 * 1000,
		    true);
		log.debug("got prompt");

		ssis.addInput(secondHost);
		// drac@r1000-s-rftec-2.cern.ch's password:
		eir.expect(" password:", 20 * 1000, true);
		ssis.addInput(secondPassword + "\n");
		// R1000-S-RFTEC-2#

		eir.expect(prompt, 20 * 1000, true);
		Thread.sleep(1000);
		eir.clearBuffer();

		ssis.addInput("terminal length 0\r\n");
		eir.expect(prompt, 20 * 1000, true);
		Thread.sleep(1000);
		eir.clearBuffer();

		ssis.addInput("show version\r\n");
		eir.expect(prompt, 20 * 1000, true);
		Thread.sleep(1000);
		eir.clearBuffer();

		ssis.addInput("show chassis\r\n");
		eir.expect(prompt, 20 * 1000, true);
		Thread.sleep(1000);
		eir.clearBuffer();

		ssis.addInput("show vlan\r\n");
		eir.expect(prompt, 20 * 1000, true);
		Thread.sleep(1000);
		eir.clearBuffer();

		ssis.addInput("show interfaces\r\n");
		eir.expect(prompt, 10 * 60 * 1000, true);
		Thread.sleep(1000);
		eir.clearBuffer();

		ssis.addInput("exit\r\nexit\nexit\n");
		ssis.eof();
	}

	@SuppressWarnings("unused")
  private void atSerfNet(String user, String ip, String password,
	    String secondPassword, int port) throws Exception {
		String prompt = "Asd001A_F25S1T#";
		String secondHost = "ssh wayne@145.145.67.9\n";

		Session session = jsch.getSession(user, ip, port);
		session.setUserInfo(new SshConnectInfo(password));
		session.connect();
		Channel c = session.openChannel("shell");
		SlowStringInputStream ssis = new SlowStringInputStream(
		    Force10Logger.getLogger(ip, port));
		c.setInputStream(ssis);
		c.connect();

		ExpectableInputReader eir = new ExpectableInputReader(c.getInputStream(),
		    Force10Logger.getLogger(ip, port), ip + ":" + port);
		eir.setDaemon(true);
		eir.start();

		ssis.addInput("\n");

		// Command prompt looks like "[wayne@surftest01 ~]$ "
		eir.expect(Pattern.compile("^\\[.*\\]\\$", Pattern.MULTILINE), 20 * 1000,
		    true);
		log.debug("got prompt");

		ssis.addInput(secondHost);
		// drac@r1000-s-rftec-2.cern.ch's password:
		eir.expect(" password:", 20 * 1000, true);
		ssis.addInput(secondPassword + "\n");
		// R1000-S-RFTEC-2#

		eir.expect(prompt, 20 * 1000, true);
		ssis.addInput("terminal length 0\r\n");
		eir.expect(prompt, 20 * 1000, true);

		ssis.addInput("\r\nshow version\r\n\r\n");
		eir.expect(prompt, 20 * 1000, true);

		ssis.addInput("\r\nshow system\r\n\r\n");
		eir.expect(prompt, 20 * 1000, true);

		ssis.addInput("\r\nshow vlan\r\n\r\n");
		eir.expect(prompt, 20 * 1000, true);

		Thread.sleep(1000);
		eir.clearBuffer();
		ssis.addInput("show interfaces\r\n");
		String block = eir.expect(Pattern.compile("(.*)" + prompt, Pattern.DOTALL),
		    60 * 1000, true);
		// String block =
		// eir.expect(Pattern.compile("(.*)% Error: Invalid input at",
		// Pattern.DOTALL), 60 *
		// 1000, true);
		// String block =
		// eir.expect(Pattern.compile("<response MajorVersion=.* MinorVersion=.*>.*</response>",
		// Pattern.DOTALL),
		// 10 * 60 * 1000, true);
		log.debug("Got it all back in block " + block);

		ssis.addInput("exit\r\nexit\nexit\n");
		ssis.eof();

	}
}
