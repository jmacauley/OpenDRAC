package org.opendrac.automationtool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author ghost
 */
public class ConfigLoader {
	public static final String DEFAULT_CONFIG_FILE = "config/opendrac.properties".replace("/", File.separator);
	// Create variables that will store the settings configured
	// in the DRAC_config.prop property file
	public String Uname;
	public String Pass;
	public String serviceURL;
	public String scheduleName;
	public String billingGroup;
	public String startTime;
	public String endTime;
	public String serviceDuration;
	public String sourceUserGroup;
	public String sourceUserResourceGroup;
	public String sourceEndpoint;
	public String destinationEndpoint;
	public String destinationUserGroup;
	public String destinationResourceUserGroup;
	public String rate;
	public String sourceVlanID;
	public String destinationVlANID;
	public String protectionType;
	public String sourceChannel;
	public String destinationChannel;
	public String routingAlgorithm;
	//if set to true, in stead of XML-sniplets, simpler feedback will be written to console
	public boolean doSimpleFeedback = false;

	public void loadConfig() throws IOException {
		loadConfig(DEFAULT_CONFIG_FILE);
	}

	public void loadConfig(String configFile) throws IOException {
		// Attempt to load the configuration settings from the DRAC_config.pro
		// file
		// create a new property file and load config
		// from(config/DRAC_config.prop)
		// After cleaning and building this file will be deleted so it must be
		// copied
		// into the same folder as the jar file, otherwise an error will occur.
		Properties prop = new Properties();
		File file = new File(configFile);
		if (!file.exists()) {
			throw new FileNotFoundException("No configuration file found at: " + configFile);
		}
		prop.load(new BufferedInputStream(new FileInputStream(file)));
		Uname = prop.getProperty("Username");
		Pass = prop.getProperty("Pass");
		serviceURL = prop.getProperty("serviceURL");
		scheduleName = prop.getProperty("scheduleName");
		billingGroup = prop.getProperty("billingGroup");
		startTime = prop.getProperty("startTime");
		endTime = prop.getProperty("endTime");
		serviceDuration = prop.getProperty("serviceDuration");
		sourceUserGroup = prop.getProperty("sourceUserGroup");
		sourceUserResourceGroup = prop.getProperty("sourceUserResourceGroup");
		sourceEndpoint = prop.getProperty("sourceEndpoint");
		destinationEndpoint = prop.getProperty("destinationEndpoint");
		destinationUserGroup = prop.getProperty("destinationUserGroup");
		destinationResourceUserGroup = prop.getProperty("destinationResourceUserGroup");
		rate = prop.getProperty("rate");
		sourceVlanID = prop.getProperty("sourceVlanID");
		destinationVlANID = prop.getProperty("destVlanID");
		protectionType = prop.getProperty("protectionType").trim();
		sourceChannel = prop.getProperty("sourceChannel");
		destinationChannel = prop.getProperty("destinationChannel");
		routingAlgorithm = prop.getProperty("routingAlgorithm");
		if (prop.getProperty("doSimpleFeedback") != null) {
			doSimpleFeedback = Boolean.parseBoolean(prop.getProperty("doSimpleFeedback"));
		}
	}
	
	public String getHost() {
		int positionStartDomain = serviceURL.indexOf("://") > 0 ? serviceURL.indexOf("://") + 3 : 0;
		String host = serviceURL.substring(positionStartDomain).split(":")[0];
		return host;
	}
	
	public int getPort(){
		String partAfterHost = serviceURL.substring(serviceURL.indexOf(getHost())+getHost().length());
		String portPart = partAfterHost.split(":")[1].split("/")[0];
		return Integer.parseInt(portPart);
	}
}