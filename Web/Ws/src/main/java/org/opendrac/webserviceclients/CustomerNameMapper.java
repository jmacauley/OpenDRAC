package org.opendrac.webserviceclients;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves Customer names by full port name using an XML file with properties
 * 
 * @author andre
 * 
 */
public class CustomerNameMapper {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private Properties customerResources = null;

	private String customerResourcesFileName = null;

	public String getCustomerResourcesFileName() {
		return customerResourcesFileName;
	}

	public void setCustomerResourcesFileName(String customerResourcesFileName) {
		this.customerResourcesFileName = customerResourcesFileName;
		try {
			initProperties();
		}
		catch (IOException e) {
			log.error("Error: ", e);
		}
	}

	public String getCustomerNameByPort(String port) {
		String customer = "SURFNET";
		if (customerResources != null
		    && customerResources.getProperty(port) != null) {
			customer = customerResources.getProperty(port);
		}
		return customer;
	}

	/**
	 * Reinitialise cached data: reread the file with the customers by port
	 */
	public void reIninit() {
		try {
			initProperties();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initProperties() throws FileNotFoundException, IOException {
		if (customerResourcesFileName != null) {
			customerResources = new Properties();
			customerResources.loadFromXML(new FileInputStream(
			    customerResourcesFileName));
		}
	}
}
