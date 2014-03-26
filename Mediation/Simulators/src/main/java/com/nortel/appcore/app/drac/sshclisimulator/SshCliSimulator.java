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

package com.nortel.appcore.app.drac.sshclisimulator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.ForwardingFilter;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.StringParser;

/**
 * As the name implies, the SSH CLI simulator is a tool which runs one or more
 * SSH daemons and supports a shell interface over SSH for the purposes of
 * simulating a CLI based device (ie force 10 Ethernet switch).
 * <p>
 * This tool only has to be "good enough" for running junit and integration
 * tests and basically faking out a Ethernet switch.
 *
 * @since 2010
 * @author pitman
 */
public final class SshCliSimulator {
  private static final Logger log = LoggerFactory.getLogger(SshCliSimulator.class);
	/**
	 * Cli Shell, a separate shell is created per client instance, we redirect all
	 * the work to the parser.
	 *
	 * @author pitman
	 */
	private static class CliShell implements Command {
		private InputStream in;
		private OutputStream out;
		private OutputStream err;
		private ExitCallback exitCallback;
		private final String prompt;
		private final String directoryPath;

		public CliShell(String directory, String commandPrompt) {
			directoryPath = directory;
			prompt = commandPrompt;
			in = null;
			out = null;
			err = null;
			exitCallback = null;
		}

		@Override
		public void destroy() {
			log.info("CLI shell destroy invoked");
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					log.error("Error: ", e);
				}
				in = null;
			}

			if (out != null) {
				try {
					out.close();
				}
				catch (IOException e) {
					log.error("Error: ", e);
				}
				out = null;
			}

			if (err != null) {
				try {
					err.close();
				}
				catch (IOException e) {
					log.error("Error: ", e);
				}
				err = null;
			}

			exitCallback = null;
		}

		@Override
		public void setErrorStream(OutputStream error) {
			err = error;
		}

		@Override
		public void setExitCallback(ExitCallback callback) {
			exitCallback = callback;
		}

		@Override
		public void setInputStream(InputStream input) {
			in = input;
		}

		@Override
		public void setOutputStream(OutputStream output) {
			out = output;
		}

		@Override
		public void start(Environment env) throws IOException {
			log.info("Starting CLI shell " + directoryPath + " " + prompt);
			out.write("Welcome to the SSH CLI Simulator!\r\f".getBytes());
			out.flush();
			final Force10SwitchCliParser force10SwitchCliParser = new Force10SwitchCliParser(in, out, err, exitCallback, prompt,
			    directoryPath);
			force10SwitchCliParser.setDaemon(true);
			force10SwitchCliParser.start();
		}
	}

	/**
	 * CliShell Factory, spins up a new CLI Shell when a client connects
	 *
	 * @author pitman
	 */
	private static class CliShellFactory implements Factory<Command> {
		private final String prompt;
		private final String directoryPath;

		public CliShellFactory(String directory, String commandPrompt) {
			directoryPath = directory;
			prompt = commandPrompt;
		}

		@Override
		public Command create() {
			log.info("Creating CLI Shell " + directoryPath + " " + prompt);
			return new CliShell(directoryPath, prompt);
		}
	}

	/**
	 * KeyProvider : Provides the server's key
	 *
	 * @author pitman
	 */
	private static final class KeyProvider extends AbstractKeyPairProvider {
		@Override
		protected KeyPair[] loadKeys() {
			String res = "/SshCliSimulator.ser";
			InputStream is = SshCliSimulator.class.getResourceAsStream(res);

			if (is == null) {
				throw new RuntimeException("Unable to load resource '" + res
				    + "' for server key pair, resource not found!");
			}
			ObjectInputStream r;
			try {
				r = new ObjectInputStream(is);
				return new KeyPair[] { (KeyPair) r.readObject() };
			}
			catch (Exception e) {
				log.error("Unable to load resource '" + res + "' for server key pair",
				    e);
				throw new RuntimeException("Unable to load resource '" + res
				    + "' for server key pair", e);
			}
		}
	}

	/**
	 * Validate passwords
	 *
	 * @author pitman
	 */
	private static final class PasswordChecker implements PasswordAuthenticator {
		@Override
		public boolean authenticate(String username, String password,
		    ServerSession session) {
			/*
			 * if the user name and the password are the same we're good to go. Easy
			 * and permits us to test failures as well.
			 */
			log.info("Simulator: login with user '" + username + "' and password '"
			    + password + "'");
			return username.equalsIgnoreCase(password);
		}
	}

	/**
	 * Very permissive key checker.
	 *
	 * @author pitman
	 */
	private static final class PublicKeyChecker implements PublickeyAuthenticator {
		@Override
		public boolean authenticate(String username, PublicKey key,
		    ServerSession session) {
			// Everything is good for us.
			return true;
		}
	}

	/**
	 * Permissive forwarding filter.
	 *
	 * @author pitman
	 */
	private static final class SshForwardingFilter implements ForwardingFilter {
		@Override
		public boolean canConnect(InetSocketAddress address, ServerSession session) {
			return true;
		}

		@Override
		public boolean canForwardAgent(ServerSession session) {
			return true;
		}

		@Override
		public boolean canForwardX11(ServerSession session) {
			return true;
		}

		@Override
		public boolean canListen(InetSocketAddress address, ServerSession session) {
			return true;
		}
	}

	private final List<SshServer> servers = new ArrayList<SshServer>();
	public static final String DEFAULT_SETUP = "F10SurfNet#Asd001A_F25S1T:22"; // "F10SurfNet#Asd001A_F25S1T:22,F10Cern#R1000-S-RFTEC-2:23";
	public static final String DEFAULT_SETUP_NO_PORT = "F10SurfNet#Asd001A_F25S1T:0";// "F10SurfNet#Asd001A_F25S1T:0,F10Cern#R1000-S-RFTEC-2:0";
	public static final String TWO_NODES_NO_PORT = "F10SurfNet#Asd001A_F25S1T:10100,F10Cern#R1000-S-RFTEC-2:10101";
	public static final String THREE_NODE_SETUP = "F10SurfNet#Asd001A_F25S1T:10100,F10Cern#R1000-S-RFTEC-2:10101,F10UvA#Force10:10102";

	public SshCliSimulator() throws Exception {
		this(DEFAULT_SETUP);
	}

	public SshCliSimulator(String input) throws Exception {
		log.info("SshCliSimulator: entering");
		try {
			List<String> toks = StringParser.split(input, ',', false);

			for (String t : toks) {
				// expect a string that looks like dir#prompt:port
				List<String> parsed = StringParser.split(t.trim(), ':', false);
				List<String> p2 = StringParser.split(parsed.get(0).trim(), '#', false);

				int port = Integer.parseInt(parsed.get(1));
				String directory = p2.get(0).trim();
				String prompt = p2.get(1).trim() + "#";

				log.info("SshCliSimulator: Starting simulator port=" + port +
						" prompt=" + prompt + " directory=" + directory);
				servers.add(startSshServer(port, prompt, directory, false));

			}
		}
		catch (RuntimeException re) {
			throw new RuntimeException(
			    "Error creating simulator with configuration '" + input + "'", re);
		}

		log.info("SshCliSimulator: completed");
	}

	public static void main(String[] args) {
		SshCliSimulator sim = null;

		try {
			if (args == null || args.length == 0) {
				log.info("Starting ssh simulator with default setup '" + TWO_NODES_NO_PORT
				    + "'");
				sim = new SshCliSimulator(TWO_NODES_NO_PORT);
			}
			else if (args.length == 1 && args[0].equalsIgnoreCase("3")) {
                log.info("Starting ssh simulator for three node network '"
				    + args[0] + "'");
				sim = new SshCliSimulator(THREE_NODE_SETUP);
            }
            else {
				log.info("Starting ssh simulator with user supplied argument '"
				    + args[0] + "'");
				sim = new SshCliSimulator(args[0]);
			}

			log.info("Simulator started.");
		}
		catch (Exception t) {
			log.error("Error", t);
		}

		if (sim == null) {
			log.error("Simulator failed to start.");
		}
		else {
			ListIterator<SshServer> it = sim.servers.listIterator();
			while (it.hasNext()) {
				SshServer server = (SshServer) it.next();
				log.info("Simulator ruuning: " + server.getHost() + ":" + server.getPort());
			}
		}
		log.info("done");
	}

	/**
	 * Return the in use port numbers, used when the simulator is created on port
	 * zero, can then be called after the daemon is running to determine what port
	 * was chosen.
	 */
	public List<Integer> getInusePorts() {
		List<Integer> l = new ArrayList<Integer>();
		for (SshServer sshd : servers) {
			l.add(Integer.valueOf(sshd.getPort()));
		}
		return l;
	}

	public void stopSshServer(boolean immediately) throws Exception {
		for (SshServer sshd : servers) {
			sshd.stop(immediately);
		}
	}

	private SshServer startSshServer(int portNumber, String prompt,
	    String directoryPath, boolean generateOwnKey) throws Exception {

		log.info("Starting ssh server on port " + portNumber + " directory "
		    + directoryPath + " prompt " + prompt);
		SshServer sshd = SshServer.setUpDefaultServer();
		// {
		// /*
		// * Override the set of supported ciphers to use the CipherNone, which does
		// no encryption
		// */
		// List<NamedFactory<Cipher>> avail = new
		// LinkedList<NamedFactory<Cipher>>();
		// // avail.add(new AES128CBC.Factory());
		// // avail.add(new TripleDESCBC.Factory());
		// // avail.add(new BlowfishCBC.Factory());
		// // avail.add(new AES192CBC.Factory());
		// // avail.add(new AES256CBC.Factory());
		// avail.add(new CipherNone.Factory());
		//
		// for (Iterator<NamedFactory<Cipher>> i = avail.iterator(); i.hasNext();)
		// {
		// NamedFactory<Cipher> f = i.next();
		// try
		// {
		// Cipher c = f.create();
		// c.init(Cipher.Mode.Encrypt, new byte[c.getBlockSize()], new
		// byte[c.getIVSize()]);
		// }
		// catch (Exception e)
		// {
		// i.remove();
		// }
		// }
		// sshd.setCipherFactories(avail);
		// }
		sshd.setPort(portNumber);

		if (generateOwnKey) {
			/*
			 * The server needs its key. It will generate one if the file is missing.
			 * Put it in the target directory if possible to keep maven happy.
			 */
			String keyfile = "key.ser";
			File f = new File("./target");
			if (f.isDirectory()) {
				keyfile = "./target/key.ser";
			}
			sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyfile));
		}
		else {
			// Use a pre-generated key pair. Find the serialized java KeyPair.
			sshd.setKeyPairProvider(new KeyProvider());
		}

		sshd.setPasswordAuthenticator(new PasswordChecker());
		sshd.setPublickeyAuthenticator(new PublicKeyChecker());
		sshd.setForwardingFilter(new SshForwardingFilter());

		/* Register a shell factory to respond to clients */
		sshd.setShellFactory(new CliShellFactory(directoryPath, prompt));
		sshd.setHost("localhost");
		sshd.start();
		return sshd;
	}
}
