/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.nrm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.config.xml.ConfigurationType;
import org.opendrac.nsi.config.xml.NrmAuthorizationInfoType;
import org.opendrac.nsi.config.xml.NrmConfigurationType;
import org.opendrac.nsi.util.SpringApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author hacksaw
 */
@Component("nrmManager")
public class NrmManager {

    // Quick lookup to information about each local NSA instance.
    private static Map<String, NrmLoginManager> loginManagers = new ConcurrentHashMap<String, NrmLoginManager>();

    public static NrmManager getInstance() {
        NrmManager nrmManager = SpringApplicationContext.getBean("nrmManager", NrmManager.class);
        return nrmManager;
    }

    public NrmLoginManager getNrmLoginManager(String nsaIdentifier) {
        // See if we already have an allocated manager for this NSA.
        NrmLoginManager manager = loginManagers.get(nsaIdentifier);

        if (manager != null) {
            return manager;
        }

        // We do not have a manager so allocate a new one.
        NsaConfigurationManager config = NsaConfigurationManager.getInstance();
        NrmConfigurationType nrmConfiguration = config.getNrmConfiguration(nsaIdentifier);
        NrmAuthorizationInfoType authorization = nrmConfiguration.getAuthorization();

        NrmLoginManager result = new NrmLoginManager(authorization.getUserName(),
                authorization.getPassword(), authorization.getBillingGroup(),
                authorization.getUserGroup(), authorization.getResourceGroup());

        loginManagers.put(nsaIdentifier, result);

        return result;
    }

    public NrmConfigurationType getNrmConfiguration(String nsaIdentifier) {
        NsaConfigurationManager config = NsaConfigurationManager.getInstance();
        NrmConfigurationType nrmConfiguration = config.getNrmConfiguration(nsaIdentifier);
        return nrmConfiguration;
    }
}
