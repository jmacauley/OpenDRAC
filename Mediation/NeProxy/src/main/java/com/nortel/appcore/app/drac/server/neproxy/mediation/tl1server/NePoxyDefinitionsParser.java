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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.Holder;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;

public enum NePoxyDefinitionsParser {

  INSTANCE;

  private final Logger log = LoggerFactory.getLogger(getClass());

  public static final String PGKNAME_ATTR = "packageName";
  public static final String CLASS_ATTR = "class";
  public static final String CANDIDATE_KEY = "$CANDIDATE";
  public static final String COMMANDNAME_KEY = "$COMMANDNAME";
  public static final String COMMANDPARAMETER_KEY = "$COMMANDPARAMETER";
  public static final String NETWORKELEMENT_KEY = "$NETWORKELEMENT";

  private static final String NEMAPPING_NODE = "neTypeMapping";
  private static final String NEINITDEF_NODE = "neInitDefinition";
  private static final String NEEVENTDEF_NODE = "neEvenHandlingDefinition";
  private static final String KEY_ATTR = "key";
  private static final String NAME_ATTR = "name";
  private static final String VALUE_ATTR = "value";

  private Element root;
  private final String fileToParse;

  private final Map<String, String> NE_TYPE_MAPPING = new HashMap<String, String>();
  private final Map<NeType, List<Holder>> NE_INIT_MAPPING = new HashMap<NeType, List<Holder>>();
  private final Map<NeType, Map<String, Map<String, String>>> NE_EVENT_HANDLING_DEFINITION = new HashMap<NeType, Map<String, Map<String, String>>>();

  protected final List<String> errors = new ArrayList<String>();

  private NePoxyDefinitionsParser() {

    fileToParse = System.getProperty("org.opendrac.neproxy.definitions",
        "neproxy-definitions.xml");
    final InputStream resourceAsStream = ClassLoader.getSystemClassLoader()
        .getResourceAsStream(fileToParse);

    try {
      Document document = new SAXBuilder().build(resourceAsStream);
      root = document.getRootElement();
      buildNeTypeMapping();
      buildInitializationList();
      buildEventHandlerList();
    }
    catch (Exception e) {
      log.error("Failed to initialize the Parser with ClassLoader Resource: '" + fileToParse
          + "'", e);
    }
    if (resourceAsStream != null) {
      try {
        resourceAsStream.close();
      }
      catch (IOException e) {
        log.error("Error: ", e);
      }
    }
  }

  public Map<String, Map<String, String>> getEvenHandlerList(NeType neType) {
    return NE_EVENT_HANDLING_DEFINITION.get(neType);
  }

  public List<Holder> getInitializationList(NeType neType) {
    return NE_INIT_MAPPING.get(neType);
  }

  public Map<NeType, Map<String, Map<String, String>>> getNeEventHandlingDefinition() {
    return NE_EVENT_HANDLING_DEFINITION;
  }

  public Map<NeType, List<Holder>> getNeInitDefinition() {
    return NE_INIT_MAPPING;
  }

  public Map<String, String> getNeTypeMapping() {
    return NE_TYPE_MAPPING;
  }

  // /**
  // * @param serverData
  // */
  // public void parse(ServerDataEvent serverData, ClientMessageXML message)
  // throws Exception
  // {
  // handleMessage(message);
  // }
  //
  // /**
  // * @param serverData
  // * @param rawMessage
  // */
  // public String parse(ServerDataEvent serverData, String rawMessage)
  // throws Exception
  // {
  // // Have to return a non-null string
  // return "";
  // }

  @SuppressWarnings("unchecked")
  private void buildEventHandlerList() {
    for (Element aNeType : (List<Element>) root.getChild(NEEVENTDEF_NODE)
        .getChildren()) {
      NeType netype = NeType.UNKNOWN;
      try {
        netype = NeType.fromString(aNeType.getName());
      }
      catch (Exception e) {
        log.error("Error mapping Netype ", e);
      }

      if (netype == NeType.UNKNOWN) {
        log.error("Bad news, Unable to map Ne type " + aNeType.getName()
            + " to a NE type enum, using UNKNOWN instead...");
      }
      Map<String, Map<String, String>> commandList = new HashMap<String, Map<String, String>>();
      NE_EVENT_HANDLING_DEFINITION.put(netype, commandList);
      for (Element aCommand : (List<Element>) aNeType.getChildren()) {
        checkKeyValid(netype, aCommand.getAttributeValue(KEY_ATTR));
        HashMap<String, String> commandParameter = new HashMap<String, String>();
        commandList.put(aCommand.getAttributeValue(KEY_ATTR), commandParameter);
        checkClassExists(aCommand.getAttributeValue(CLASS_ATTR));
        commandParameter
            .put(CLASS_ATTR, aCommand.getAttributeValue(CLASS_ATTR));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void buildInitializationList() throws Exception {
    for (Element aNeType : (List<Element>) root.getChild(NEINITDEF_NODE)
        .getChildren()) {
      NeType type = NeType.fromString(aNeType.getName());
      List<Holder> commandList = new ArrayList<Holder>();
      for (Element aCommand : (List<Element>) aNeType.getChildren()) {
        Map<String, Object> commandParameter = new HashMap<String, Object>();
        commandParameter
            .put(CLASS_ATTR, aCommand.getAttributeValue(CLASS_ATTR));
        checkClassExists(aCommand.getAttributeValue(CLASS_ATTR));
        for (Element aParameter : (List<Element>) aCommand.getChildren()) {
          commandParameter.put(aParameter.getAttributeValue(NAME_ATTR),
              aParameter.getAttributeValue(VALUE_ATTR));
        }
        checkKeyValid(type, aCommand.getAttributeValue(KEY_ATTR));
        commandList.add(new Holder(aCommand.getAttributeValue(KEY_ATTR),
            commandParameter));
      }
      NE_INIT_MAPPING.put(type, commandList);
    }
  }

  @SuppressWarnings("unchecked")
  private void buildNeTypeMapping() {
    for (Element aNeType : (List<Element>) root.getChild(NEMAPPING_NODE)
        .getChildren()) {
      NE_TYPE_MAPPING.put(aNeType.getName(), aNeType.getText());
    }
  }
  

  /**
   * To save time and sanity, when we parse the file make sure any referenced
   * classes exist, if they don't we are going to have trouble down the road,
   * this way we can scan and check them all up front.
   */
  private void checkClassExists(String className) {
    try {
      Class.forName(className);
    }
    catch (ClassNotFoundException e) {
      String s = "Error while parsing file "
          + fileToParse
          + " the referenced class "
          + className
          + " cannot be loaded and may cause problems later if this class is required";
      errors.add(s);
      log.error(s, e);
    }
  }

  /**
   * Sanitize the TL1 command codes, make sure we know about them before we
   * start running.
   */
  private void checkKeyValid(NeType neType, String key) {
    if (neType == NeType.FORCE10) {
      // The Force10 switch does not use TL1 commands!
      return;
    }

    try {
      Tl1CommandCode.fromString(key);
    }
    catch (Exception e) {
      String s = "Unable to map key " + key
          + " to command code and may cause problems later if this is required";
      errors.add(s);
      log.error(s, e);
      log.error(s, e);
    }
  }

  // private boolean handleMessage(ClientMessageXML message)
  // {
  // // Check the XML configuration for the definition of the command
  //
  // Map<String, Object> command = supportMessage.get(message.getCommandCode());
  // if (command == null)
  // {
  // message.setException("Unsupport command");
  // return false;
  // }
  // String className = (String) command.get(ClientMessageXML.ACTION_NODE);
  //
  // if (className == null)
  // {
  // message.setException("Failed to find the corresponding handler");
  // return false;
  // }
  // try
  // {
  // Map<String, Object> parameter = message.getInputDataMap();
  // Candidate aCandidate = new Candidate();
  //
  // parameter.put(CANDIDATE_KEY, aCandidate);
  // // parameter.put(NEMAPPING_KEY, NE_TYPE_MAPPING);
  // // parameter.put(NEINITDEF_KEY, NE_INIT_MAPPING);
  // // parameter.put(NEEVENTDEF_KEY, NE_EVENT_HANDLING_DEFINITION);
  // parameter.put(COMMANDPARAMETER_KEY,
  // command.get(ClientMessageXML.INPUTDATA_NODE));
  // AbstractCommandlet handler = AbstractCommandlet.getCommandlet(className,
  // parameter);
  //
  // boolean handlerRC = handler.start();
  //
  // // Pass any xml result data through...even on error; can be used on error
  // path processing.
  // if (aCandidate.getXmlResult() != null)
  // {
  // message.setOutputData(aCandidate.getXmlResult());
  // }
  //
  // if (handlerRC)
  // {
  // return true;
  // }
  //
  // Map<String, Object> errorMap = (Map<String, Object>)
  // command.get(ClientMessageXML.ERRORCODE_NODE);
  // String errorText = (String) errorMap.get(aCandidate.getErrorCode());
  // if (errorText == null)
  // {
  // errorText = "Unknown error";
  // }
  // if (aCandidate.getAdditionalErrorText() != null)
  // {
  // errorText = errorText + aCandidate.getAdditionalErrorText();
  // }
  //
  // message.setException(errorText, aCandidate.getErrorCode());
  // return false;
  // }
  // catch (Exception e)
  // {
  // message.setException(e.toString());
  // return false;
  // }
  // }

}
