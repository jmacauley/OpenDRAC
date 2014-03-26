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
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.common.utility.ExpectableInputReader;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAssociationEvent;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractInitializeNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.IPAddressInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.UserProfile;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AssociationEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class Force10NetworkElement extends AbstractNe {
  private static final Logger log = LoggerFactory.getLogger(Force10NetworkElement.class);
	/**
	 * Once a session is established, monitor it and periodically inject data into
	 * the session to prevent it from closing and verify that the session is still
	 * running. If the session is found to be dead, close it down and trigger our
	 * machinery to reconnect.
	 * 
	 * @author pitman
	 */
	public static class F10SessonKeepAliveMonitor extends Thread {
		private final Force10NetworkElement ne;
		private static final long NAP = 1000 * 60 * 2;
		private boolean done;

		F10SessonKeepAliveMonitor(Force10NetworkElement f10) {
			ne = f10;
			setName("F10SessonKeepAliveMonitor " + ne.getIpAddress() + ":"
			    + ne.getPortNumber());
		}

		public void closeSession() {
			log.debug("KeepAlive monitor session closed");
			done = true;
			this.interrupt();
		}

		@Override
		public void run() {
			try {
				log.debug("KeepAlive monitor starting");
				while (!done) {
					// inject some data
					try {
						ne.lockCommunicationsWithNe();
						ne.getInputStream().addInput("\r\n");
						if (ne.getExpectReader().isEof()) {
							log.error("F10SessonKeepAliveMonitor: identified an EOF on session, marking session as closed");
							ne.disconnect(true);
						}
					}
					finally {
						ne.unlockCommunicationsWithNe();
					}
					Thread.sleep(NAP);
				}
			}
			catch (Exception t) {
				log.error("Error: ", t);
			}
			log.debug("KeepAlive monitor terminated.");
		}
	}

	/**
	 * InputStream that we can pass strings into. Its a slow stream in that the
	 * read method includes a sleep to slow it down to better resemble a user
	 * typing in a terminal.
	 */
	public static class SlowStringInputStream extends InputStream {
		private final Object lock = new Object();
		private final StringBuilder sb = new StringBuilder();
		private boolean eof;
		private final org.apache.log4j.Logger log;

		public SlowStringInputStream(org.apache.log4j.Logger l) {
			log = l;
		}

		public void addInput(String s) {
			log.debug("SlowStringInputStream: adding input '" + s + "'");
			if (log != null) {
				
			}
			synchronized (lock) {
				sb.append(s);
				lock.notifyAll();
			}
		}

		public void eof() {
			log.debug("SlowStringInputStream: Marking EOF");
			eof = true;
		}

		@Override
		public int read() throws IOException {
			byte b;
			while (true) {

				synchronized (lock) {
					// reached the end.
					if (sb.length() == 0 && eof) {
						log.debug("SlowStringInputStream: returning -1 (EOF)");
						b = -1;
						break;
					}

					// got some thing
					if (sb.length() > 0) {

						String c = sb.substring(0, 1);
						// Log.debug("SlowStringInputStream: reading  '" + c + "' from " +
						// sb);
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
						// Log.debug("SlowStringInputStream: waiting for input");
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
			// Log.debug("SlowStringInputStream: readBlock requested off:" + off +
			// " len:" + len);
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
				log.debug("SlowStringInputStream: readBlock hit EOF, returning -1");
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
					// Log.debug("SlowStringInputStream: reading  '" + str + "' from " +
					// sb + " returning " +
					// i);
					sb.delete(0, howMany);
					System.arraycopy(str.getBytes(), 0, b, off + 1, howMany);
				}
			}

			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				log.error("eh?", e);
			}
			return i;
		}
	}

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
				log.debug("Jsch debug log Text: " + s);
				break;
			case 1:
				log.debug("Jsch info log Text: " + s);
				break;
			case 2:
				log.warn("Jsch warn log Text: " + s);
				break;
			case 3:
				log.error("Jsch error log Text: " + s);
				break;
			case 4:
				log.error("Jsch fatal log Text: " + s, new Exception("StackTrace"));
				break;
			default:
				log.debug("Jsch Log level:" + i + " log Text: " + s);
			}
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
	private AbstractInitializeNe alignNe;
	private SlowStringInputStream ssis;
	private ExpectableInputReader eir;
	private Session session;
	private Channel c;
	private F10SessonKeepAliveMonitor keepAliveMonitor;
	private final Object disconnectLock = new Object();
	private Pattern f10Prompt = Pattern
	    .compile("Asd001A_F25S1T", Pattern.LITERAL);
	private final Map<String, Pattern> pCache = new HashMap<String, Pattern>();

	/*
	 * Only one thread can be actively talking to the device at a time otherwise
	 * we can't scrape the CLI commands and responses correctly. This lock
	 * prevents multiple access by locking at the thread level.
	 */
	private final ReentrantLock neLock = new ReentrantLock(true);

	public Force10NetworkElement(String uid, CryptedString passwd, String ipAddr,
	    int portNumber, String terminalId) {
		JSch.setLogger(new JschLogger());
		jsch = new JSch();
		setIpAddress(ipAddr);
		setPortNumber(portNumber);
		setUid(uid);
		setPasswd(passwd);
		UserProfile userInfo = new UserProfile(uid, passwd);
		IPAddressInfo ipAddrInfo = new IPAddressInfo(ipAddr, portNumber);
		setNeInfo(new NetworkElementInfo("00-00-00-00-00-00", "", ipAddrInfo,
		    userInfo));
		setDbChgEvent(new Tl1XmlAssociationEvent(this));
		setTerminalId(terminalId);
	}

	@Override
	public void changeNePassword(String userId, CryptedString newPassword) {
		log.debug("changeNE password invoked for " + getIpAddress() + ":"
		    + getPortNumber() + " " + getNeId() + " " + getNeName());
		setUid(userId);
		setPasswd(newPassword);
		getNeInfo().getUserProfile().setLoginPassword(newPassword);
		getNeInfo().getUserProfile().setUserID(userId);
		DbUtility.INSTANCE.upDateNePassword(this);
		DbUtility.INSTANCE.upDateNeUid(this);
		toggleAssocation();
	}

	/*
	 * Throw out compiled patterns
	 */
	public void clearPatternCache() {
		synchronized (pCache) {
			pCache.clear();
		}
	}

	/**
	 * When talking to the force 10 switch we need to scan the output stream for
	 * the command prompt to know if the command has finished or if we are ready
	 * to send the next command.
	 */
	public Pattern getCommandLinePrompt() {
		return f10Prompt;
	}

	/**
	 * @return the eir
	 */
	public ExpectableInputReader getExpectReader() {
		return eir;
	}

	public void lockCommunicationsWithNe() {
		while (true) {
			try {
				if (neLock.tryLock(10, TimeUnit.MINUTES)) {
					// we obtained the lock, perhaps after waiting for it.
					return;
				}
			}
			catch (InterruptedException e) {
				log.error(
				    "Force10 lockCommunicationsWithNe was interrupted, not nice, going back to waiting for the lock",
				    e);
			}
		}
	}

	@Override
	public void nextState() {
		log.debug("force10 nextState invoked, current state is " + getState()
		    + " previous state: " + getPreviousNeState());
		switch (getState()) {

		case NE_NOT_CONNECT:
			if (getPreviousNeState() != getState()) {
				setPreviousNeState(NeStatus.NE_NOT_CONNECT);
				DbUtility.INSTANCE.upDateNeStatus(this);
				sendAssociationEvent(this.getNeStatus());
			}

			int rc = connect();
			switch (rc) {
			case 0:
				setState(NeStatus.NE_ASSOCIATED);
				setPreviousNeState(NeStatus.NE_NOT_CONNECT);
				DbUtility.INSTANCE.upDateNeStatus(this);
				sendAssociationEvent(this.getNeStatus());
				break;
			case 1:
				setState(NeStatus.NE_NOT_AUTHENTICATED);
				setPreviousNeState(NeStatus.NE_NOT_CONNECT);
				break;
			default:
				// failed
			}

			DiscoverNePool.INSTANCE.enqueueTask(this);
			return;
		case NE_ASSOCIATED:
			setState(NeStatus.NE_INITIALIZING);
			if (getPreviousNeState() != getState()) {
				setPreviousNeState(NeStatus.NE_INITIALIZING);
				DbUtility.INSTANCE.upDateNeStatus(this);
				sendAssociationEvent(this.getNeStatus());
			}
			DiscoverNePool.INSTANCE.enqueueTask(this);
			break;
		case NE_INITIALIZING:
			try {
				log.debug("invoking " + alignNe.getClass());
				if (alignNe.start()) {
					TL1AssociationEvent tempEvent = new TL1AssociationEvent(
					    TL1AssociationEvent.ASSOCIATION_UP, this.getNeInfo());
					receiveEvent(tempEvent);
					setState(NeStatus.NE_ALIGNED);
					if (getPreviousNeState() != getState()) {
						setPreviousNeState(NeStatus.NE_ALIGNED);
						DbUtility.INSTANCE.upDateNe(this);
						sendAssociationEvent(this.getNeStatus());
					}
				}
				else {
					log.error("align failed, state to associated");
					setPreviousNeState(NeStatus.NE_ASSOCIATED);
				}
				DiscoverNePool.INSTANCE.enqueueTask(this);
			}
			catch (Exception e) {
				log.error("Exception durring initializing NE", e);
				setState(NeStatus.NE_NOT_CONNECT);
				DiscoverNePool.INSTANCE.enqueueTask(this);
			}
			return;
		case NE_NOT_AUTHENTICATED:
			sendAssociationEvent(this.getNeStatus());
			DbUtility.INSTANCE.upDateNeStatus(this);
			disconnect(false);
			return;
		default:
			log.debug("Force10 nextState() ignoring state " + getState());
			return;
		}
	}

	/**
	 * Compiling a regular expression is very expensive. Normally you'd just use
	 * static patterns, but our patterns include the NE name, keep a cache of
	 * already complied patterns to avoid recompiling
	 */

	public Pattern patternCache(String regex) {
		synchronized (pCache) {
			Pattern t = pCache.get(regex);
			if (t != null) {
				return t;
			}
			t = Pattern.compile(regex);
			pCache.put(regex, t);
			return t;
		}
	}

	public void receiveEvent(TL1AssociationEvent event) {
		// Current state is Aligned and the event is from Inservice to
		// OutofService
		log.debug("Handling association event: " + event.getCode());
		if (getState() == NeStatus.NE_ALIGNED
		    && event.getCode() > TL1AssociationEvent.ASSOCIATION_UP) {
			this.setState(NeStatus.NE_NOT_CONNECT);
			DiscoverNePool.INSTANCE.enqueueTask(this);
		}
	}

	/**
	 * Save the Force10's running configuration, should save the config after
	 * making provisioning changes otherwise they won't surrive a restart
	 * 
	 * @throws Exception
	 */
	public void saveRunningConfig() throws Exception {

		log.debug("Force10 saveRunningConfig " + toDebugString());
		startWithCommandPrompt();
		/**
		 * Here is what we expect to see whan saving the configuration...
		 * 
		 * <pre>
		 * Asd001A_F25S1T#copy running-config startup-config
		 * File with same name already exist.
		 * Proceed to copy the file [confirm yes/no]: yes
		 * !
		 * 3473 bytes successfully copied
		 * .
		 * Asd001A_F25S1T#
		 * </pre>
		 */
		getInputStream().addInput("copy running-config startup-config\r\n");

		/*
		 * We want to see 1 of 2 things. The confirm yes/no prompt or the command
		 * prompt that indicates that the configuration was saved. Wait for a while
		 * as the command can be slow to run.
		 */
		String block = getExpectReader().expect(
		    patternCache("(confirm yes/no|" + getCommandLinePrompt().pattern()
		        + ")"), 2 * 60 * 1000, true, 1);

		if (block.contains("confirm yes/no")) {
			// Send yes and wait for the command prompt to appear.
			getInputStream().addInput("yes\r\n");
			waitForCommandPrompt();
		}
	}

	/**
	 * Get to a command prompt, send a command and wait for the next command
	 * prompt (that indicates that the command finished) before returning. Leave
	 * the results of the command in the buffer.
	 * 
	 * @param input
	 * @throws Exception
	 */
	public void sendCommandWaitForCommandPrompt(String input, long timeOut)
	    throws Exception {
		sendCommandWaitForCommandPrompt(input, 1000, timeOut);
	}

	/**
	 * Get to a command prompt, send a command, wait for a delay period before
	 * scanning or the next command prompt (that indicates that the command
	 * finished) before returning. Leave the results of the command in the buffer.
	 * 
	 * @param input
	 * @throws Exception
	 */
	public void sendCommandWaitForCommandPrompt(String input, long initalDelay,
	    long timeOut) throws Exception {
		/*
		 * get to a sane state, make sure we are already at the command prompt and
		 * ready to send the next command
		 */
		startWithCommandPrompt();
		// send the command
		getInputStream().addInput(input);

		/*
		 * We have just sent the command line to the device, now wait a bit for the
		 * device to respond, waiting here can greatly improve performance as we are
		 * not sitting and scanning the input buffer for a regular expression that
		 * contains the command prompt. If we know the command will take 10 seconds
		 * to complete wait for 10 seconds here and we'll only have to scan the
		 * output buffer once instead of multiple times as the results of the
		 * command dribble back to us. For the force10 the command show interfaces
		 * is both slow and returns lots of data, some of which will look like a
		 * command prompt and force us to perform costly regular expression checks
		 * on the input buffer.
		 */
		Thread.sleep(initalDelay);

		/*
		 * wait for zero or some data followed by a command prompt to be returned,
		 * leave the data in the buffer for the caller to extract.
		 */

		/**
		 * Optimize our performance. The show interfaces command generates > 200k of
		 * output at Cern, 40 K at Surfnet, all of which we'll be inspecting every
		 * time a new block arrives to see if our expect pattern has been matched.
		 * Avoid repeated regular expression matches, and use the fixed string
		 * version to look for the prompt tag. Once we know the prompt is in the
		 * buffer, run the regular expression version. If the command prompt appears
		 * in the command output we'll finish the first expect call but the second
		 * call will continue to block until it sees the real command prompt...
		 * <p>
		 * Note Force10 users should use unique host names that don't resemble
		 * output seen else where. A switch called "Vlan" or "IP" would be a bad
		 * choice for us.
		 * <p>
		 * The CERN force10 contains the command prompt text in several interface
		 * labels, hence our first scan for just the command prompt will not be
		 * sucessfull and we'll end up performing the more expensive regular
		 * expression search as more data dribbles into our buffer slowing us down.
		 * Use the inital delay to avoid even looking at the buffer until we are
		 * reasonably sure the data is in the buffer, then scan for it.
		 */

		getExpectReader().expect(getCommandLinePrompt().pattern(), timeOut, false);
		getExpectReader().expect(
		    patternCache("(?ms).*^\\s*" + getCommandLinePrompt()), timeOut, false);
	}

	@Override
	public void setNeName(String name) {
		super.setNeName(name);
		getNeInfo().setNeName(name);
	}

	@Override
	public void terminate() {
		sendAssociationEvent(NeStatus.NE_NOT_PROVISION);
		setState(NeStatus.NE_NOT_CONNECT);
	}

	public void toggleAssocation() {
		log.debug("toggleAssociation invoked for " + getIpAddress() + ":"
		    + getPortNumber() + " " + getNeId() + " " + getNeName());
		disconnect(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Force10NetworkElement [neInfo=");
		builder.append(getNeInfo());
		builder.append(" currentState=");
		builder.append(getState());
		builder.append("]");
		return builder.toString();
	}

	public void unlockCommunicationsWithNe() {
		neLock.unlock();
	}

	/**
	 * Wait for the command prompt to appear in the output stream from the NE.
	 * Used after sending a command and we want to wait for the prompt before
	 * sending the next. Consumes all data in the buffer before and including the
	 * command prompt.
	 * 
	 * @throws Exception
	 */
	public void waitForCommandPrompt() throws Exception {
		getExpectReader().expect(getCommandLinePrompt(), 2 * 60 * 1000, true);
	}

	/**
	 * return code of 0 - connected ok 1 - bad password anything else - failure.
	 */
	private int connect() {
		try {
			lockCommunicationsWithNe();

			// make sure we are not already connected.
			disconnect(false);

			session = jsch.getSession(getUid(), getIpAddress(), getPortNumber());
			session.setUserInfo(new SshConnectInfo(CryptoWrapper.INSTANCE
			    .decrypt(getPasswd())));
			try {
				session.connect();
			}
			catch (JSchException jse) {
				if (jse.getMessage().contains("Auth fail")) {
					log.error("Authorization failed, wrong user/password when connecting to NE "
					    + getIpAddress()
					    + ":"
					    + getPortNumber()
					    + " "
					    + getNeId()
					    + " "
					    + getNeName());
					disconnect(false);
					return 1;
				}

				log.error("Failed to connect to NE", jse);
				disconnect(false);
				return -1;
			}

			org.apache.log4j.Logger l = Force10Logger.getLogger(getIpAddress(),
			    getPortNumber());
			ssis = new SlowStringInputStream(l);

			c = session.openChannel("shell");
			c.setInputStream(ssis);
			c.connect();

			eir = new ExpectableInputReader(c.getInputStream(), l, getIpAddress()
			    + ":" + getPortNumber());
			eir.setDaemon(true);
			eir.start();
			ssis.addInput("\n");

			/**
			 * Debug test setup, add support for double sshing to a remote box. If
			 * set, ssh first into a remote box then ssh into the final force 10
			 * switch.
			 * <p>
			 * Commission the NE with the ip/port/password of the first box then set
			 * the properties for the second box. It might be better if we did it the
			 * other way around (commission the final box and set properties for the
			 * intermediate box) as that would permit us to talk to more than one at a
			 * time... I'm doing it this way cause its easier.
			 */
			{
				String doubleSshUser = System.getProperty("Force10DoubleSshUser");
				String doubleSshHost = System.getProperty("Force10DoubleSshHost");
				String doubleSsshPassword = System
				    .getProperty("Force10DoubleSshPassword");
				if (doubleSshUser != null && doubleSshHost != null
				    && doubleSsshPassword != null) {
					log.error("Force10 Running double ssh config test mode '"
					    + doubleSshHost + "'");
					// // Command prompt looks like "[wayne@surftest01 ~]$ "
					// eir.expect(Pattern.compile("^\\[.*\\]\\$", Pattern.MULTILINE), 20 *
					// 1000, true);
					// Log.info("got prompt");

					// Wait a bit, don't try to parse a prompt
					Thread.sleep(1000);

					ssis.addInput("ssh " + doubleSshUser + "@" + doubleSshHost + "\n");
					eir.expect(" password:", 40 * 1000, true);
					ssis.addInput(doubleSsshPassword + "\n");
					ssis.addInput("\n");
				}
			}

			String ourNeType = "FORCE10";
			// String neSwVer = "N/A";

			String neMappedName = this.getNeTypeMapping().get(ourNeType);
			if (neMappedName == null) {
				Exception e = new Exception("ERROR in mapping NE type - " + ourNeType);
				log.error("Error: ", e);
				return -1;
			}

			this.setNeType(NeType.fromString(neMappedName));
			if (this.getNeType() == NeType.UNKNOWN) {
				Exception e = new Exception("ERROR in mapping NE type - "
				    + this.getNeType());
				log.error("Error: ", e);
				return -1;
			}
			getNeInfo().setNeType(this.getNeType());

			/**
			 * Figure out what the command line prompt looks like
			 */

			pickCommandLinePrompt();

			/* Align the NE */
			String packageName = this.getNeTypeMapping().get(NePoxyDefinitionsParser.PGKNAME_ATTR)
			    .trim();
			Class<?> actionClass = Class.forName(packageName + "."
			    + neMappedName.toLowerCase() + ".AlignNe");
			Class<?>[] args = new Class[] { AbstractNe.class };
			Constructor<?> constructor = actionClass.getConstructor(args);

			Object[] context_args = new Object[] { this };
			alignNe = (AbstractInitializeNe) constructor.newInstance(context_args);

			// Keep alive
			keepAliveMonitor = new F10SessonKeepAliveMonitor(this);
			keepAliveMonitor.setDaemon(true);
			keepAliveMonitor.start();

			return 0;
		}
		catch (Exception e) {
			log.error("Failed to connect/align with NE", e);
			return -1;
		}
		finally {
			unlockCommunicationsWithNe();
		}
	}

	/**
	 * Disconnect if connected. If triggerRetry is true, mark the state as
	 * NE_NOT_CONNECT and inform the state machine to attempt to reconnect.
	 */
	private void disconnect(boolean triggerRetry) {
		synchronized (disconnectLock) {
			log.debug("Disconnect invoked against Force10 NE " + getIpAddress() + ":"
			    + getPortNumber());

			if (keepAliveMonitor != null) {
				keepAliveMonitor.closeSession();
				keepAliveMonitor = null;
			}

			if (c != null) {
				c.disconnect();
				c = null;
			}

			if (session != null) {
				session.disconnect();
				session = null;
			}

			if (eir != null) {
				eir.close();
				eir = null;
			}

			if (ssis != null) {
				try {
					ssis.close();

				}
				catch (IOException e) {
					log.error("Error: ", e);
				}
				ssis = null;
			}

			clearPatternCache();
			if (triggerRetry) {
				setState(NeStatus.NE_NOT_CONNECT);
				setPreviousNeState(NeStatus.NE_NOT_AUTHENTICATED);
				DiscoverNePool.INSTANCE.enqueueTask(this);
			}
		}

	}

	/**
	 * @return the ssis
	 */
	private SlowStringInputStream getInputStream() {
		if (!neLock.isHeldByCurrentThread()) {
			log.error(
			    "The Ne communications lock is not held by this thread and its attempting to talk to the NE!!!",
			    new Exception("Stack Trace back"));
		}
		return ssis;
	}

	
  private void pickCommandLinePrompt() throws Exception {
    String prompt = "#";
    String prompt1 = null;
    try {
      prompt1 = getPrompt(prompt);
    }
    catch (Exception e) {
      
//      log.debug("Changing prompt from # to > ");
//      prompt = ">";
//      prompt1 = getPrompt(prompt);
      
      // FIXME: Or do I have to elevate privileges by issuing enable command and
      // sending the current password?

       getExpectReader().clearBuffer();
//       startWithCommandPrompt();
       // send the command
       getInputStream().addInput("enable");
       Thread.sleep(2500L);
       getInputStream().addInput("\r\n");
       getInputStream().addInput(CryptoWrapper.INSTANCE.decrypt(getPasswd()));
       Thread.sleep(2500L);
       getInputStream().addInput("\r\n");
       
    }
    prompt1 = getPrompt(prompt);
    String prompt2 = getPrompt(prompt);
    String prompt3 = getPrompt(prompt);

    log.debug("Determining prompt: '" + prompt1 + "' '" + prompt2 + "' and '" + prompt3 + "'");

    if (prompt3 != null && prompt1.equals(prompt2) && prompt2.equals(prompt3)) {
      log.debug("Determined prompt to be: '" + prompt1 + "'");
      // set the prompt pattern to be the literal string we just saw.
      f10Prompt = Pattern.compile(prompt1, Pattern.LITERAL);
      return;
    }
    throw new Exception("Failed to determine prompt:\n " + this.toDebugString());

  }

  private String getPrompt(final String prompt) throws Exception {
    getExpectReader().clearBuffer();
    getInputStream().addInput("\r\n");
    return getExpectReader().expect(Pattern.compile("^(.*)" + prompt, Pattern.MULTILINE), 30 * 1000, true, 1).trim();
  }

	/**
	 * startwithCommandPrompt: Before sending a command to the NE, make sure that
	 * we are sitting at the command prompt and that any previous commands have
	 * finished and that we are in a sane state to proceed.
	 * 
	 * @throws Exception
	 */
	private void startWithCommandPrompt() throws Exception {
		// wait a wee bit.
		Thread.sleep(100);
		// toss anything we've seen out.
		getExpectReader().clearBuffer();
		getInputStream().addInput("\r\n");

		/*
		 * Gobble as much as we can, handle the case where we have 2 command line
		 * prompts in the buffer
		 */
		/* The (?ms) is the same as Pattern.MULTILINE and Pattern.DOTALL */

		Pattern pattern = patternCache("(?ms).*^\\s*" + getCommandLinePrompt());
		getExpectReader().expect(pattern, 5 * 1000, true);

		// do it again
		getInputStream().addInput("\r\n");

		/*
		 * Gobble as much as we can, handle the case where we have 2 command line
		 * prompts in the buffer
		 */
		/* The (?ms) is the same as Pattern.MULTILINE and Pattern.DOTALL */

		getExpectReader().expect(pattern, 5 * 1000, true);
		// wait a wee bit.
		Thread.sleep(100);
		getExpectReader().clearBuffer();
	}
}

