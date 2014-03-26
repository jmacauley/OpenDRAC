/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.opendrac.nsi.config.xml.*;
import org.opendrac.nsi.util.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author hacksaw
 */
@Component("nsaConfigurationManager")
public class NsaConfigurationManager {
		private final Logger logger = LoggerFactory.getLogger(getClass());

    //@Resource(name="nsaIdURNList")
    //private List<String> nsaIdURNList;

    @Value("#{nsiProperties.localConfigurationFile}")
    private String localConfigurationFile;

	@Value("#{nsiProperties.nsaConfigurationFile}")
    private String nsaConfigurationFile;

    // Quick lookup to information about each NSA in the network.
    private Map<String, NsaConfigurationType> nsaConfiguration = new ConcurrentHashMap<String, NsaConfigurationType>();

    // Quick lookup to information about each local NSA instance.
    private Map<String, ConfigurationType> localConfiguration = new ConcurrentHashMap<String, ConfigurationType>();

    // The NSAId supported locally by this NSA instance.
    private List<String> nsaIdURNList = new CopyOnWriteArrayList<String>();

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() throws IOException, JAXBException{

        setLocalConfigurationFile(localConfigurationFile);
    	setNsaConfigurationFile(nsaConfigurationFile);

    }

    public static NsaConfigurationManager getInstance() {
        NsaConfigurationManager nsaConfigurationManager = SpringApplicationContext.getBean("nsaConfigurationManager", NsaConfigurationManager.class);
        return nsaConfigurationManager;
    }

    /**
     * Get the current local configuration file name.
     *
     * @return the localConfigurationFile
     */
    public String getLocalConfigurationFile() {
        return localConfigurationFile;
    }

    /**
     * Set a new local configuration file and load contents into internal map.
     *
     * @param localConfigurationFile the localConfigurationFile to set
     */
    public void setLocalConfigurationFile(String localConfigurationFile) throws IOException, JAXBException  {

        // Read the XML configuration file from classpath using JAXB.
        ConfigurationList list = null;
        logger.info("Loading local configuration file from classpath: " + localConfigurationFile);

        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationList.class);
      		list = (ConfigurationList) jaxbContext.createUnmarshaller()
      			    .unmarshal(this.getClass().getClassLoader().getResourceAsStream(localConfigurationFile));
        }
        catch (JAXBException jaxb) {
            logger.error("Error parsing file " + localConfigurationFile, jaxb);
            throw jaxb;
        }

        // Empty our existing Map for these new contents.
        localConfiguration.clear();
        nsaIdURNList.clear();

        // Now index the individual NSA entries into a Map for indexed lookup.
        for (ConfigurationType entry : list.getConfiguration()) {
            // Save this local NSA configuration.
            logger.info("Adding local entry " + entry.getNsaIdentifier());
            localConfiguration.put(entry.getNsaIdentifier(), entry);

            nsaIdURNList.add(entry.getNsaIdentifier());
        }

        this.localConfigurationFile = localConfigurationFile;
    }


    /**
     * Get my NSA identifier.
     *
     * @return the nsaIdURN
     */
    public List<String> getMyNsaIdURN() {
        return nsaIdURNList;
    }

    /**
     * @return the nsaRequesterEndpoint
     */
    public String getNsaRequesterEndpoint(String nsaIdentifier) {
        ConfigurationType nsa = localConfiguration.get(nsaIdentifier);
        if (nsa != null) {
            return nsa.getRequesterEndpoint();
        }

        return null;
    }

    /**
     * @param nsaRequesterEndpoint the nsaRequesterEndpoint to set
     */
    public void setNsaRequesterEndpoint(String nsaIdentifier, String nsaRequesterEndpoint) {
        ConfigurationType nsa = localConfiguration.get(nsaIdentifier);
        if (nsa != null) {
            nsa.setRequesterEndpoint(nsaRequesterEndpoint);
        }
    }

    /**
     * @return the nsaProviderEndpoint
     */
    public String getNsaProviderEndpoint(String nsaIdentifier) {
        ConfigurationType nsa = localConfiguration.get(nsaIdentifier);
        if (nsa != null) {
            return nsa.getProviderEndpoint();
        }

        return null;
    }

    /**
     * @param nsaProviderEndpoint the nsaProviderEndpoint to set
     */
    public void setNsaProviderEndpoint(String nsaIdentifier, String nsaProviderEndpoint) {
        ConfigurationType nsa = localConfiguration.get(nsaIdentifier);
        if (nsa != null) {
            nsa.setProviderEndpoint(nsaProviderEndpoint);
        }
    }

    /**
     * Get my NsNetwork URN.
     *
     * @return the nsNetworkURN for my NSA.
     */
    public String getMyNsNetwork(String nsaIdentifier) {
        ConfigurationType nsa = localConfiguration.get(nsaIdentifier);
        if (nsa != null) {
            return nsa.getNsNetwork();
        }

        return null;
    }

    /**
     * Get a list of NsNetwork URN.
     *
     * @return the list of nsNetworkURN for this NSA.
     */
    public List<String> getMyNsNetworkList() {

        List<String> nsNetworkList = new ArrayList<String>();
        ConfigurationType entry;

        for (String nsaIdURN : nsaIdURNList) {
            entry = localConfiguration.get(nsaIdURN);
            if (entry != null) {
                nsNetworkList.add(entry.getNsNetwork());
            }
        }

        return nsNetworkList;
    }

    /**
     * Check to see if the provided URN is managed by this NSA.
     *
     * @return the true if my managed nsnetwork.
     */
    public boolean isMyNsNetworkURN(String urn) {
        for (String nsNetworkURN : getMyNsNetworkList()) {
            if (nsNetworkURN.equalsIgnoreCase(urn)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check to see if the provided URN is one of the supported Id for this NSA.
     *
     * @return the nsNetworkURN for mu NSA.
     */
    public boolean isMyNsaURN(String urn) {
        for (String nsaURN : nsaIdURNList) {
            if (nsaURN.equalsIgnoreCase(urn)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the current configuration file name.
     *
     * @return the nsaConfigurationFile
     */
    public String getNsaConfigurationFile() {
        return nsaConfigurationFile;
    }

    /**
     * Set a new NSA configuration file and load contents into internal map.
     *
     * @param nsaConfigurationFile the nsaConfigurationFile to set
     */
    public void setNsaConfigurationFile(String nsaConfigurationFile) throws IOException, JAXBException  {

        // Read the XML NSA configuration file from classpath using JAXB.
        NsaConfigurationList nsaList = null;
        logger.info("Loading configuration file from classpath: " + nsaConfigurationFile);

        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(NsaConfigurationList.class);
      			nsaList = (NsaConfigurationList) jaxbContext.createUnmarshaller()
      			    .unmarshal(
      			    		this.getClass().getClassLoader().getResourceAsStream(nsaConfigurationFile));

        }
        catch (JAXBException jaxb) {
            logger.error("error parsing file " + nsaConfigurationFile, jaxb);
            throw jaxb;
        }

        // Empty our existing Map for these new contents.
        nsaConfiguration.clear();

        // Now index the individual NSA entries into a Map for indexed lookup.
        for (NsaConfigurationType entry : nsaList.getNsaConfiguration()) {
            logger.info("adding NSA entry " + entry.getNsaIdentifier());
            nsaConfiguration.put(entry.getNsaIdentifier(), entry);
        }

        this.nsaConfigurationFile = nsaConfigurationFile;
    }

    /**
     * Find the NSA URN identifier associated with the provided NsNetwork URN.
     *
     * @param urn NsNetwork URN to resolve into an NSA URN identifier.
     * @return matching NSA URN identifier, null otherwise.
     */
    public String getNsaIdByNsNetwork(String urn) {
        for (Entry<String, NsaConfigurationType> entry : nsaConfiguration.entrySet()) {
            for (String nsNetwork : entry.getValue().getNsNetwork()) {
                if (nsNetwork.equalsIgnoreCase(urn)) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    /**
     * Get authentication information for specified NSA.
     *
     * @param nsaIdURN NSA URN identifier to use for retrieving associated info.
     * @return the authenticationInfo for this NSA.
     */
    public AuthenticationInfoType getNsaAuthenticationInfo(String nsaIdURN) {
        NsaConfigurationType entry = nsaConfiguration.get(nsaIdURN);
        if (entry == null) {
            return null;
        }

        return entry.getAuthentication();
    }

    /**
     * Get authorization information for specified NSA.
     *
     * @param nsaIdentifier NSA URN identifier to use for retrieving associated info.
     * @return the NrmConfiguration for this NSA.
     */
    public NrmConfigurationType getNrmConfiguration(String nsaIdentifier) {
        ConfigurationType entry = localConfiguration.get(nsaIdentifier);
        if (entry == null) {
            return null;
        }

        return entry.getNrmConfiguration();
    }

    /**
     * Get NsNetwork URN information for specified NSA.
     *
     * @param nsaIdURN NSA URN identifier to use for retrieving associated info.
     * @return the NsNetwork URN associated with this NSA.
     */
    public List<String> getNsaNsNetwork(String nsaIdURN) {
        NsaConfigurationType entry = nsaConfiguration.get(nsaIdURN);
        if (entry == null) {
            return null;
        }

        return entry.getNsNetwork();
    }

    /**
     * Get the list of NSA peered through the control plane for the specified
     * NSA.
     *
     * @param nsaIdURN NSA URN identifier to use for retrieving associated info.
     * @return the NSA peering list associated with this NSA.
     */
    public List<String> getConnectedTo(String nsaIdURN) {
        NsaConfigurationType entry = nsaConfiguration.get(nsaIdURN);
        if (entry == null) {
            return null;
        }

        return entry.getConnectedTo();
    }

    /**
     * Get set of NSA identifiers for all configured NSA.
     *
     * @param nsaIdURN NSA URN identifier to use for retrieving associated info.
     * @return set of NSA identifier URN.
     */
    public Set<String> getNsaList(String nsaIdURN) {

        return nsaConfiguration.keySet();
    }
}
