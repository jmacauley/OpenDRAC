package org.opendrac.ioc;

import java.security.Security;
import java.util.Arrays;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class IocContainer {

	private static final Logger log = LoggerFactory.getLogger(IocContainer.class);

	private static String[] configLocations = null;

	private static ConfigurableApplicationContext context;

	public synchronized static void close() {
		if (IocContainer.context != null && IocContainer.context.isActive()){
			IocContainer.context.close();
		}
		IocContainer.context = null;
	}

	public static <T> T getBean(Class<T> requiredType) {
		return IocContainer.context.getBean(requiredType);
	}

	public static <T> T getBean(String name, Class<T> requiredType) { // NO_UCD
		return IocContainer.context.getBean(name, requiredType);
	}

	public synchronized static void refresh() { // NO_UCD
		IocContainer.context.refresh();
	}

	public synchronized static void setConfigs(String... configLocations) {
		IocContainer.configLocations = configLocations;
		if (IocContainer.context != null
		    && !Arrays.equals(IocContainer.configLocations, configLocations)) {
			IocContainer.refresh();
		}
		log.debug("Using XML bean definitions: "
		    + Arrays.toString(IocContainer.configLocations));
	}

	public synchronized static void start() {
		if (configLocations == null) {
			throw new IllegalStateException("No spring context files found!");
		}
		if (IocContainer.context == null) {
			IocContainer.context = new ClassPathXmlApplicationContext(configLocations);
		}
	}

	public static void startWithDefaultConfig() {
		String configs[] = new String[] { "/spring/opendrac-common.xml",
		    "/spring/opendrac-database.xml", "/spring/opendrac-monitoring.xml"};
		IocContainer.setConfigs(configs);
		IocContainer.start();
	}

	static {
		Security.addProvider(new BouncyCastleProvider());
	}
}
