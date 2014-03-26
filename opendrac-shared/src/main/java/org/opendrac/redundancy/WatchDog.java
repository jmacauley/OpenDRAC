package org.opendrac.redundancy;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class WatchDog {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	static ClassPathXmlApplicationContext context;

	@Value("#{redundancyProperties.timeout}")
	private int timeout;

	@Value("#{redundancyProperties.retries}")
	protected int retries;

	@Value("#{redundancyProperties.frequency}")
	protected int frequency;

	@Value("#{redundancyProperties.start_script}")
	protected String startScript;

	@Value("#{redundancyProperties.opendrac_root}")
	protected String opendracRoot;

	protected int errorCounter;

	protected boolean isFailedOver = false;

	public abstract void heartbeat();

	protected void checkHosts(List<String> controllerHostPortMapping)
	    throws ConnectException, IOException {
		for (final String s : controllerHostPortMapping) {
			final String host = s.substring(0, s.indexOf(",")).trim();
			final String port = s.substring(s.indexOf(",") + 1, s.length()).trim();

			log.debug("Checking host: " + host + ", port:" + port);

			final Socket socket = new Socket() {
				{
					connect(
					    new InetSocketAddress(InetAddress.getByName(host),
					        Integer.parseInt(port)), timeout);
				}
			};
			socket.close();
			errorCounter = 0;
		}
	}

	protected void executeCommand(final String command,
	    final String commandRootDirectory) throws IOException {
		final ProcessBuilder processBuilder = new ProcessBuilder(command.trim()
		    .replace("/", File.separator));
		if (commandRootDirectory != null && !"".equals(command)) {
			processBuilder.directory(new File(commandRootDirectory.trim().replace(
			    "/", File.separator)));
		}

		processBuilder.start();
	}
	
	public static void main(String[] args) {
    WatchDog.context = new ClassPathXmlApplicationContext(
        "spring/opendrac-redundancy.xml");

  }

}
