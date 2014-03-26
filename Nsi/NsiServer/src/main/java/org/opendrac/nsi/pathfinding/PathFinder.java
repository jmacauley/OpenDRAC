/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.domain.DataManager;
import org.opendrac.nsi.topology.Nsa;
import org.opendrac.nsi.topology.Stp;
import org.opendrac.nsi.topology.TopologyFactory;
import org.opendrac.nsi.util.ExceptionCodes;
import org.opendrac.nsi.util.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class PathFinder {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public List<PathSegment> computePath(PathType path, ServiceParametersType attributes) throws ServiceException {
        /**
         * For the demo we are just an ultimate provider NSA so we can only
         * find a path if the source and destination STP are on our NSnetwork
         * segment.
         */
        List<PathSegment> pathList = Collections.synchronizedList(new ArrayList<PathSegment>());

        // Who are we?
        List<String> myNSAList = NsaConfigurationManager.getInstance().getMyNsaIdURN();

        StringBuilder tmp = new StringBuilder("PathFinder.computePath: resolved my NSA to <");
        for (String myNsaURN: myNSAList) {
            tmp.append(myNsaURN);
            tmp.append(", ");
        }
        tmp.append(">");

        logger.info(tmp.toString());

        // Get a reference to the DataManager so we can get additional references.
        DataManager dataManager = SpringApplicationContext.getBean("dataManager", DataManager.class);

        // Verify the provided STP are in our NSnetwork.
        TopologyFactory topology = dataManager.getTopologyFactory();

        // Resolve the endpoints.
        String sourceSTP = getSTP("sourceSTP", path.getSourceSTP()).trim();
        String destSTP = getSTP("destSTP", path.getDestSTP()).trim();

        logger.info("PathFinder.computePath: Resolving path between sourceSTP=" + sourceSTP + ", destSTP=" + destSTP);

        Stp sourceStp = resolveStp(topology, "sourceSTP", sourceSTP);
        Stp destStp = resolveStp(topology, "destSTP", destSTP);

        logger.info("PathFinder.computePath: Successfully resolved sourceSTP=" + sourceStp.getStpURN() + ", destSTP=" + destStp.getStpURN());

        // Get the managing NSA for these endpoints.
        Nsa sourceNsa = resolveStptoNsa(topology, "sourceSTP", sourceSTP);
        Nsa destNsa = resolveStptoNsa(topology, "destSTP", destSTP);

        logger.info("PathFinder.computePath: Successfully resolved NSA sourceNSA=" + sourceNsa.getNsaURN() + ", destSTP=" + destNsa.getNsaURN());

        /**
         * TODO: Now we bail out of path finding by forcing source and
         * destination STP into the local domain.  We will fix this later.
         */
        String sourceURN = null;
        String destURN = null;

        for (String myNsaURN: myNSAList) {
            if (sourceNsa.getNsaURN().equalsIgnoreCase(myNsaURN)) {
                sourceURN = myNsaURN;
            }

            if (destNsa.getNsaURN().equalsIgnoreCase(myNsaURN)) {
                destURN = myNsaURN;
            }
        }

        // Did we get both endpoints on the same network?
        if (sourceURN == null) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.PATH_COMPUTATION_NO_PATH, sourceSTP, "sourceSTP");
        }
        else if (destURN == null) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.PATH_COMPUTATION_NO_PATH, destSTP, "destSTP");
        }
        else if (!sourceURN.equalsIgnoreCase(destURN)) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.PATH_COMPUTATION_NO_PATH, sourceSTP, "destSTP");
        }

        // TODO: Build the detailed path based on indiviaul segments.
        PathSegment pathSegment = new PathSegment();
        pathSegment.setNsaType(PathSegment.NsaType.LOCAL);
        pathSegment.setSourceStpURN(sourceStp.getStpURN());
        pathSegment.setDestStpURN(destStp.getStpURN());
        pathSegment.setManagingNsaURN(sourceNsa.getNsaURN());
        pathSegment.setDirectionality(path.getDirectionality());

        // We have all the common attributes in the state machine.

        // TODO: We are ignoring the explicit hop path list for now.

        // TODO: add service attribute support.
        pathSegment.setServiceAttributes(attributes.getServiceAttributes());

        // Add this path segment to the list.
        pathList.add(pathSegment);

        return pathList;
    }

    public String getSTP(String name, ServiceTerminationPointType stp) throws ServiceException {
        if (stp == null || stp.getStpId() == null || stp.getStpId().isEmpty()) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.MISSING_PARAMETER, name, "<null>");
        }

        return stp.getStpId();
    }

    public Stp resolveStp(TopologyFactory topology, String name, String stp) throws ServiceException {
        Stp result = topology.getStpByURN(stp);
        if (result == null) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.TOPOLOGY_RESOLUTION_STP, name, stp);
        }
        return result;
    }

    public Nsa resolveStptoNsa(TopologyFactory topology, String name, String stp) throws ServiceException {
        Nsa result = topology.getNsaByStp(stp);
        if (result == null) {
            throw ExceptionCodes.buildProviderException(ExceptionCodes.TOPOLOGY_RESOLUTION_STP_NSA, name, stp);
        }
        return result;
    }

}
