package org.opendrac.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that parses the launcher.xml
 */
public final class LauncherConfigParser {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private static final String PROPERTY_ELEMENT = "property";
  private static final String PROPERTY_NAME_ATTRIBUTE = "name";
  private static final String PROPERTY_VALUE_ATTRIBUTE = "value";

  private static final String CLASS_ELEMENT = "class";
  private static final String CLASS_NAME_ATTRIBUTE = "name";
  private static final String CLASS_ENABLED_ATTRIBUTE = "enabled";
  private static final String CLASS_INIT_ATTRIBUTE = "init";
  private static final String CLASS_DESTROY_ATTRIBUTE = "destroy";
  private static final String CLASS_FORK_ATTRIBUTE = "fork";

  private List<LauncherProcess> processes = new ArrayList<LauncherProcess>();
  private Element rootElement;

  public LauncherConfigParser(final String fileName) {
    Document doc;
    try {
      doc = new SAXBuilder().build(fileName);
      rootElement = doc.getRootElement();
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  public Properties parseSystemProperties() {
    final Properties properties = new Properties();
    List<Element> elements = (List<Element>) rootElement
        .getChildren(PROPERTY_ELEMENT);
    for (Element element : elements) {
      properties.put(element.getAttribute(PROPERTY_NAME_ATTRIBUTE).getValue(),
          element.getAttribute(PROPERTY_VALUE_ATTRIBUTE).getValue());
    }
    return properties;
  }

  public List<LauncherProcess> parseProcesses() {
    List<Element> processElements = (List<Element>) rootElement
        .getChildren(CLASS_ELEMENT);
    for (Element processElement : processElements) {
      try {
        String id = processElement.getAttributeValue(CLASS_NAME_ATTRIBUTE);
        boolean isEnabled = processElement.getAttribute(CLASS_ENABLED_ATTRIBUTE)
            .getBooleanValue();
        boolean isForked = processElement.getAttribute(CLASS_FORK_ATTRIBUTE)
            .getBooleanValue();
        String initCommand = processElement
            .getAttributeValue(CLASS_INIT_ATTRIBUTE);
        String destroyCommand = processElement
            .getAttributeValue(CLASS_DESTROY_ATTRIBUTE);
        processes.add(new LauncherProcess(id, isEnabled, initCommand,
            destroyCommand, isForked));
      }
      catch (DataConversionException e) {
        log.error("Error: ", e);
      }
    }
    return processes;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("LauncherConfigParser [log=");
    builder.append(log);
    builder.append(", processes=");
    builder.append(processes);
    builder.append(", rootElement=");
    builder.append(rootNodeToString());
    builder.append("]");
    return builder.toString();
  }

  private String rootNodeToString() {
    return new XMLOutputter(Format.getCompactFormat())
        .outputString(rootElement);
  }

}
