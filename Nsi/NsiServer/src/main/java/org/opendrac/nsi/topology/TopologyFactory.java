package org.opendrac.nsi.topology;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse the DTOX OWL formatted topology file.  This is based off of a prototype
 * schema and will need to be updated with the new official version.
 *
 * @author hacksaw
 */
public class TopologyFactory {
    private static final Logger logger = LoggerFactory.getLogger(TopologyFactory.class);

    private OWLOntologyManager manager = null;
    private OWLDataFactory dataFactory = null;

    private OWLOntology   topologyOWL = null;

    static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";
    static final String HASPART = OWL_NAMESPACE + "hasPart";
    static final String PARTOF =  OWL_NAMESPACE + "partOf";

    // DTOX namspace.
    static final String DTOX_NAMESPACE = "http://www.glif.is/working-groups/tech/dtox#";

    // DTOX classes.
    static final String NSNETWORK = DTOX_NAMESPACE + "NSNetwork";
    static final String NSA       = DTOX_NAMESPACE + "NSA";
    static final String STP       = DTOX_NAMESPACE + "STP";

    // DTOX object properties.
    static final String CONNECTEDTO = DTOX_NAMESPACE + "connectedTo";
    static final String HASSTP =      DTOX_NAMESPACE + "hasSTP";
    static final String MANAGEDBY =   DTOX_NAMESPACE + "managedBy";
    static final String MANAGING =    DTOX_NAMESPACE + "managing";
    static final String MAPSTO =      DTOX_NAMESPACE + "mapsTo";

    // DTOX data properties.
    static final String ADMINCONTACT =       DTOX_NAMESPACE + "adminContact";
    static final String CSPROVIDERENDPOINT = DTOX_NAMESPACE + "csProviderEndpoint";
    static final String CANSWAP =            DTOX_NAMESPACE + "canSwap";
    static final String HOSTNAME =           DTOX_NAMESPACE + "hostName";

    // nsNetworkList - List of NSnetork objects with the defined topology.
    private final Map<String, NSnetwork> nsNetworkList = new ConcurrentHashMap<String, NSnetwork>();

    // nsaList - List of NSA objects within the defined topology.
    private final Map<String, Nsa> nsaList = new ConcurrentHashMap<String, Nsa>();

    // stpList - List of inter-domain Service Termination Points that are within this network.
    private final Map<String, Stp> stpList = new ConcurrentHashMap<String, Stp>();

    // nodeList - List of DTOX Nodes within this network.
    private final Map<String, Node> nodeList = new ConcurrentHashMap<String, Node>();

    public TopologyFactory(String filename) throws IOException {

        // Get hold of an ontology manager
        manager = OWLManager.createOWLOntologyManager();
        dataFactory = this.manager.getOWLDataFactory();

        try {

            // Now load the local copy from the classpath
      			topologyOWL = manager.loadOntologyFromOntologyDocument(this.getClass()
      			    .getClassLoader().getResourceAsStream(filename));
            logger.info("Loaded ontology from classpath: " + topologyOWL.toString());

            // We can always obtain the location where an ontology was loaded from
            IRI documentIRI = this.manager.getOntologyDocumentIRI(topologyOWL);
            logger.debug("Source: " + documentIRI);

        }
        catch (OWLOntologyCreationIOException e) {
            // IOExceptions during loading get wrapped in an OWLOntologyCreationIOException.
            StringBuilder exception = new StringBuilder();

            IOException ioException = e.getCause();
            if (ioException instanceof FileNotFoundException) {
                exception.append("Could not load ontology. File not found: ");
            }
            else if (ioException instanceof UnknownHostException) {
                exception.append("Could not load ontology. Unknown host: ");
            }
            else {
                exception.append("Could not load ontology: ");
                exception.append(ioException.getClass().getSimpleName());
                exception.append(" ");
            }

            exception.append(ioException.getMessage());
            throw new IOException(exception.toString(), e.getCause());
        }
        catch (UnparsableOntologyException e) {
            // If there was a problem loading an ontology because there are
            // syntax errors in the document (file) that represents the
            // ontology then an UnparsableOntologyException is thrown.
            StringBuilder exception = new StringBuilder("Could not parse the ontology: ");
            exception.append(e.getMessage());
            exception.append('\n');

            // A map of errors can be obtained from the exception
            Map<OWLParser, OWLParserException> exceptions = e.getExceptions();
            // The map describes which parsers were tried and what the errors were
            for (OWLParser parser : exceptions.keySet()) {
                exception.append("Tried to parse the ontology with the ");
                exception.append(parser.getClass().getSimpleName());
                exception.append(" parser.\nFailed because: ");
                exception.append(exceptions.get(parser).getMessage());
                exception.append('\n');
            }

            throw new IOException(exception.toString(), e.getCause());
        }
        catch (UnloadableImportException e) {
            // If our ontology contains imports and one or more of the imports
            // could not be loaded then an UnloadableImportException will be
            // thrown (depending on the missing imports handling policy).
            StringBuilder exception = new StringBuilder("Could not load import: ");
            exception.append(e.getImportsDeclaration());

            // The reason for this is specified and an OWLOntologyCreationException
            OWLOntologyCreationException cause = e.getOntologyCreationException();
            exception.append("\nReason: ");
            exception.append(cause.getMessage());
            exception.append('\n');

            throw new IOException(exception.toString(), e.getCause());
        }
        catch (OWLOntologyCreationException e) {

            throw new IOException("Could not load ontology: " + e.getMessage(), e.getCause());
        }

        // Process the document.

        // Dump the class names defined in this Ontology.
        Set<OWLClass> classes = topologyOWL.getClassesInSignature();
        for (OWLClass element : classes) {
            logger.debug("Classes: " + element.toStringID());
        }

        // Parse all NSnetworks in the topology.
        OWLClass networkClass = dataFactory.getOWLClass(IRI.create(NSNETWORK));
        Set<OWLIndividual> setOfNetwork = networkClass.getIndividuals(topologyOWL);
        NSnetwork nsNetwork = null;
        for (OWLIndividual individual : setOfNetwork) {
            logger.debug("NSnetwork: " + individual.toStringID());
            nsNetwork = parseNSnetwork(individual);
            synchronized(nsNetworkList) {
                nsNetworkList.put(individual.toStringID(), nsNetwork);
            }
        }

        // Parse all the NSA in the topology.
        OWLClass nsaClass = dataFactory.getOWLClass(IRI.create(NSA));
        Set<OWLIndividual> setOfNsa = nsaClass.getIndividuals(topologyOWL);
        Nsa nsa = null;
        for (OWLIndividual individual : setOfNsa) {
            logger.debug("NSA: " + individual.toStringID());
            nsa = parseNSA(individual);
            synchronized(nsaList) {
                nsaList.put(individual.toStringID(), nsa);
            }
        }

        // Parse all the STP in the topology.
        OWLClass stpClass = dataFactory.getOWLClass(IRI.create(STP));
        Set<OWLIndividual> setOfStp = stpClass.getIndividuals(topologyOWL);
        Stp stp = null;
        for (OWLIndividual individual : setOfStp) {
            logger.debug("STP: " + individual.toStringID());
            stp = parseSTP(individual);
            synchronized(stpList) {
                stpList.put(individual.toStringID(), stp);
            }
        }

        // We are done with the ontology so remove it.
        manager.removeOntology(topologyOWL);
    }

    private NSnetwork parseNSnetwork(OWLIndividual network) {
        // We are building an NSnetwork object so create a new one.
        NSnetwork nsNetwork = new NSnetwork();

        dumpObjectProperties(network);

        // Set the URN for this NSnetwork object.
        nsNetwork.setNetworkURN(network.toStringID());

        // label - display name for the network.
        try {
            nsNetwork.setLabel(parseLabel(network));
        }
        catch (NoSuchFieldException ex) {
            // We can ignore this and go on.
            logger.debug("parseNSnetwork: no rdf:label found for " + network.toStringID());
        }

        // canSwap - indicates VLAN Id interchange is supported in the network domain.
        nsNetwork.setCanSwap(parseCanSwapProperty(network));

        // managedBy - the Network Services Agent managing this network.
        try {
            nsNetwork.setManagedBy(parseSingleObjectProperty(network, MANAGEDBY));
        }
        catch (NoSuchFieldException ex) {
            // Can we continue without a managing NSA?
            logger.debug("parseNSnetwork: no managedBy found for " + network.toStringID());
        }

        // stpList - identifies the inter-domain Service Termination Points that are within this network.
        try {
            nsNetwork.setStpList(parseObjectProperty(network, HASSTP));
        }
        catch (NoSuchFieldException ex) {
            // Can we continue without a managing NSA?
            logger.debug("parseObjectProperty: no " + HASSTP + " found for " + network.toStringID());
        }

        // nodeList - List of DTOX Nodes within this network.
        try {
            nsNetwork.setNodeList(parseObjectProperty(network, HASPART));
        }
        catch (NoSuchFieldException ex) {
            // Can we continue without a managing NSA?
            logger.debug("parseObjectProperty: no " + HASPART + " found for " + network.toStringID());
        }

        logger.debug(nsNetwork.toString());

        return nsNetwork;
    }

    private Nsa parseNSA(OWLIndividual nsAgent) {
        // We are building an NSA object so create a new one.
        Nsa nsa = new Nsa();

        dumpObjectProperties(nsAgent);

        // Set the URN for this NSA object.
        nsa.setNsaURN(nsAgent.toStringID());

        // label - display name for the NSA.
        try {
            nsa.setLabel(parseLabel(nsAgent));
        }
        catch (NoSuchFieldException ex) {
            // We can ignore this and go on.
            logger.debug("parseNSA: no rdf:label found for " + nsAgent.toStringID());
        }

        // managing - indicates the network being managed by this NSA.
        try {
            nsa.setManaging(parseSingleObjectProperty(nsAgent, MANAGING));
        }
        catch (NoSuchFieldException ex) {
            // Can we continue without a managing NSA?
            logger.debug("parseNSA: no managing found for " + nsAgent.toStringID());
        }

        // hostName - DNS name of the NSA.
        try {
            nsa.setHostName(parseSingleLiteralDataProperty(nsAgent, HOSTNAME));
        }
        catch (NoSuchFieldException ex) {
            logger.debug("parseNSA: no hostName found for " + nsAgent.toStringID());
        }

        // csProviderEndpoint - the SOAP endpoint for this NSA's CS provider interface.
        try {
            nsa.setCsProviderEndpoint(parseSingleLiteralDataProperty(nsAgent, CSPROVIDERENDPOINT));
        }
        catch (NoSuchFieldException ex) {
            // Can we continue without a SOAP endpoint?
            logger.debug("parseNSA: no csProviderEndpoint found for " + nsAgent.toStringID());
        }

        // adminContact - administrative contact information for this NSA.
        try {
            nsa.setAdminContact(parseSingleLiteralDataProperty(nsAgent, ADMINCONTACT));
        }
        catch (NoSuchFieldException ex) {
            logger.debug("parseNSA: no adminContact found for " + nsAgent.toStringID());
        }

        // connectedTo - represents an NSA to which this NSA has a peering relationship.
        try {
            nsa.setConnectedTo(parseObjectProperty(nsAgent, CONNECTEDTO));
        }
        catch (NoSuchFieldException ex) {
            // Can we continue without a managing NSA?
            logger.debug("parseNSA: no " + CONNECTEDTO + " found for " + nsAgent.toStringID());
        }

        logger.debug(nsa.toString());

        return nsa;
    }

    private Stp parseSTP(OWLIndividual stPoint) {
        // We are building an STP object so create a new one.
        Stp stp = new Stp();

        dumpObjectProperties(stPoint);

        // Set the URN for this STP object.
        stp.setStpURN(stPoint.toStringID());

        // label - display name for the STP.
        try {
            stp.setLabel(parseLabel(stPoint));
        }
        catch (NoSuchFieldException ex) {
            // We can ignore this and go on.
            logger.debug("parseSTP: no rdf:label found for " + stPoint.toStringID());
        }

        /*
         * partOf - indicates the member network of this STP.  I have been
         * fighting with Jerry to get this included in the schema he generates
         * but for some reason he is against it.  If it is in the file parse
         * and store the value, otherwise look it up via the nsnetwork object.
         * This depends on the NSnetwork objects to be parsed first.
         */
        try {
            stp.setPartOf(parseSingleObjectProperty(stPoint, PARTOF));
        }
        catch (NoSuchFieldException ex) {
            // Can we continue without a managing NSA?
            logger.debug("parseSTP: no partOf found for " + stPoint.toStringID());

            // Not in the schema so reverse match.
            NSnetwork nsnetwork = null;
            for (Entry<String, NSnetwork> entry : nsNetworkList.entrySet()) {
                nsnetwork = entry.getValue();
                if (nsnetwork.containsStp(stPoint.toStringID())) {
                    stp.setPartOf(nsnetwork.getNetworkURN());
                    break;
                }
            }
        }

        // connectedTo - a topological link identifying the peer STP to which this STP is connected.
        try {
            stp.setConnectedTo(parseObjectProperty(stPoint, CONNECTEDTO));
        }
        catch (NoSuchFieldException ex) {
            // Can we continue without a managing NSA?
            logger.debug("parseSTP: no " + CONNECTEDTO + " found for " + stPoint.toStringID());
        }

        // mapsTo - the logical STP represented by this object instance maps through to this physical entity (port, vlan, etx.) in DTOX.
        try {
            stp.setMapsTo(parseSingleObjectProperty(stPoint, MAPSTO));
        }
        catch (NoSuchFieldException ex) {
            // Can we continue without a managing NSA?
            logger.debug("parseSTP: no " + MAPSTO + " found for " + stPoint.toStringID());
        }

        logger.debug(stp.toString());

        return stp;
    }

    private String parseLabel(OWLIndividual individual) throws NoSuchFieldException {
        OWLClass classA = dataFactory.getOWLClass(IRI.create(individual.toStringID()));
        OWLAnnotationProperty property = dataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());

        for (OWLAnnotation annotation : classA.getAnnotations(topologyOWL, property)) {
            OWLLiteral literal = (OWLLiteral) annotation.getValue();
            if (literal.isRDFPlainLiteral()) {
                return literal.getLiteral().trim();
            }
        }

        throw new NoSuchFieldException("parseLabel: rdf:label attribute not found");
    }

    private boolean parseCanSwapProperty(OWLIndividual individual) {
        try {
            Set<String> swapList = parseLiteralDataProperty(individual, CANSWAP);
            for (String canSwap : swapList) {
                if (canSwap.trim().equalsIgnoreCase("yes")) {
                    return true;
                }
            }
        }
        catch (NoSuchFieldException ex) {
            logger.debug("parseCanSwapProperty: no canSwap found for " + individual.toStringID());
        }

        return false;
    }

    private String parseSingleObjectProperty(OWLIndividual individual, String property) throws NoSuchFieldException {
        OWLObjectProperty object = dataFactory.getOWLObjectProperty(IRI.create(property));
        Set<OWLIndividual> objectPropertyValues = individual.getObjectPropertyValues((OWLObjectPropertyExpression) object, topologyOWL);

        for (OWLIndividual value: objectPropertyValues) {
           return value.toStringID();
        }

        throw new NoSuchFieldException("parseManagedBy: " + property + " not found");
    }

    private Set<String> parseObjectProperty(OWLIndividual individual, String property) throws NoSuchFieldException {
        OWLObjectProperty object = dataFactory.getOWLObjectProperty(IRI.create(property));
        Set<OWLIndividual> objectPropertyValues = individual.getObjectPropertyValues((OWLObjectPropertyExpression) object, topologyOWL);

        Set<String> values = new HashSet<String>();
        for (OWLIndividual value: objectPropertyValues) {
           values.add(value.toStringID());
        }

        if (!values.isEmpty()) {
            return values;
        }

        throw new NoSuchFieldException("parseObjectProperty: " + property + " not found");
    }

    private String parseSingleLiteralDataProperty(OWLIndividual individual, String property) throws NoSuchFieldException {
        OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(property));
        Set<OWLLiteral> value = individual.getDataPropertyValues(prop, topologyOWL);

        for (OWLLiteral literal : value) {
            String tmp = literal.getLiteral();
            if (tmp != null && !tmp.trim().isEmpty()) {
                return tmp.trim();
            }
        }

        throw new NoSuchFieldException("parseSingleLiteralDataProperty: String value not found " + property);
    }

    private Set<String> parseLiteralDataProperty(OWLIndividual individual, String property) throws NoSuchFieldException {
        OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(property));
        Set<OWLLiteral> value = individual.getDataPropertyValues(prop, topologyOWL);

        HashSet<String> result = new HashSet<String>();

        for (OWLLiteral literal : value) {
            String tmp = literal.getLiteral();
            if (tmp != null && !tmp.trim().isEmpty()) {
                result.add(tmp.trim());
            }
        }

        if (result.isEmpty()) {
            throw new NoSuchFieldException("parseLiteralDataProperty: String value not found " + property);
        }

        return result;
    }

    private void dumpObjectProperties(OWLIndividual individual) {
        Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objectPropertyValues = individual.getObjectPropertyValues(topologyOWL);

        for (OWLObjectPropertyExpression value: objectPropertyValues.keySet()) {
            logger.debug("dumpObjectProperties: " + value.toString());
        }
    }

    /**
     * @return the nsNetworkList
     */
    public Map<String, NSnetwork> getNsNetworkList() {
        return nsNetworkList;
    }

    /**
     * @return the nsaList
     */
    public Map<String, Nsa> getNsaList() {
        return nsaList;
    }

    /**
     * @return the stpList
     */
    public Map<String, Stp> getStpList() {
        return stpList;
    }

    /**
     * @return the nodeList
     */
    public Map<String, Node> getNodeList() {
        return nodeList;
    }

    /**
     * Converts the parsed topology object to a structured string.
     * @return A string representation of the topology.
     */
    @Override
    public String toString() {

        StringBuilder tmp = new StringBuilder("Topology = {\n  NSNetwork Objects = {\n");
        for (Map.Entry<String, NSnetwork> entry : nsNetworkList.entrySet()) {
            String key = entry.getKey();
            NSnetwork value = entry.getValue();
            tmp.append("    key = \"");
            tmp.append(key);
            tmp.append("\"\n    ");
            tmp.append(value.toString());
            tmp.append("\n");
        }

        tmp.append("  }\n  NSA Objects = {\n");
        for (Map.Entry<String, Nsa> entry : nsaList.entrySet()) {
            String key = entry.getKey();
            Nsa value = entry.getValue();
            tmp.append("    key = \"");
            tmp.append(key);
            tmp.append("\"\n    ");
            tmp.append(value.toString());
            tmp.append("\n");
        }

        tmp.append("  }\n  STP Objects = {\n");
        for (Map.Entry<String, Stp> entry : stpList.entrySet()) {
            String key = entry.getKey();
            Stp value = entry.getValue();
            tmp.append("    key = \"");
            tmp.append(key);
            tmp.append("\"\n    ");
            tmp.append(value.toString());
            tmp.append("\n");
        }

        tmp.append("  }\n}\n");

        return tmp.toString();
    }

    /**
     * @return the STP
     */
    public Stp getStpByURN(String urn) {
        return stpList.get(urn);
    }

    /**
     * @return the NSnetwork
     */
    public NSnetwork getNSnetworkByURN(String urn) {
        return nsNetworkList.get(urn);
    }

    /**
     * @return the NSnetwork
     */
    public NSnetwork getNSnetworkByStp(String urn) {
        Stp stp = stpList.get(urn);
        if (stp != null && stp.getPartOf() != null) {
            return getNSnetworkByURN(stp.getPartOf());
        }

        return null;
    }

    /**
     * @return the NSnetwork
     */
    public Nsa getNsaByStp(String urn) {
        Stp stp = stpList.get(urn);
        if (stp != null && stp.getPartOf() != null) {
            NSnetwork network = getNSnetworkByURN(stp.getPartOf());
            if (network != null && network.getManagedBy() != null) {
                return nsaList.get(network.getManagedBy());
            }
        }

        return null;
    }

    /**
     * @return the Nsa
     */
    public Nsa getNsaByURN(String urn) {
        return nsaList.get(urn);
    }

    public String getCsProviderEnpointByNsaURN(String nsiURN) {
        Nsa nsa = nsaList.get(nsiURN);

        if (nsa != null) {
            return nsa.getCsProviderEndpoint();
        }

        return null;
    }

}
