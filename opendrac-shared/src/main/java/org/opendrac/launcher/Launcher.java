package org.opendrac.launcher;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Launcher {

	private static final Logger log = LoggerFactory.getLogger(Launcher.class);
	private static final String basedir = System.getProperty("basedir");
	private static final String log4jConfig = basedir + "/conf/log4j.xml".replace("/", File.separator);

	private final LauncherConfigParser launcherConfigParser = new LauncherConfigParser(basedir
	    + "/conf/opendrac.xml".replace("/", File.separator));

	public Launcher() {
		setSystemProperties(launcherConfigParser.parseSystemProperties());
		DOMConfigurator.configureAndWatch(log4jConfig, 45 * 1000);
	}

	public void setSystemProperties(Properties properties) {
		for (Entry<Object, Object> property : properties.entrySet()) {
			final String propertyName = property.getKey().toString();
			final String propertyValue = property.getValue().toString();
			if (propertyValue.startsWith(".")) {
				String sysProperty = basedir + propertyValue.substring(1, propertyValue.length()).trim();
				System.setProperty(propertyName, sysProperty);
			}
			else {
				System.setProperty(propertyName, propertyValue);
			}
		}
	}

	public void startup() {
		final List<LauncherProcess> processes = launcherConfigParser.parseProcesses();
		addShutdownHook(processes);
		for (final LauncherProcess process : processes) {
			if (process.isEnabled()) {
				if (process.isForked()) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							process.init();
						}
					}) {
						{
							start();
						}
					};
				}
				else {
					process.init();
				}
			}
			else {
				log.debug("Process " + process.getClassName() + " is disabled!");
			}
		}
	}

	private void addShutdownHook(final List<LauncherProcess> processes) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.warn("Shutdown hook invoked, reversing processes list");
				Collections.reverse(processes);
				try {
					for (final LauncherProcess process : processes) {
						if (process.isRunning() && process.isEnabled() && process.getDestroyCommand() != null
						    && !process.getDestroyCommand().isEmpty()) {
							if (process.isForked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										process.destroy();
									}
								}) {
									{
										start();
									}
								};
							}
							else {
								process.destroy();
							}
						}
					}
				}
				catch (Exception e) {
					log.error("Error: ", e);
				}
			}
		});
	}

	public static void main(String[] args) {
		new Launcher().startup();
	}
}
