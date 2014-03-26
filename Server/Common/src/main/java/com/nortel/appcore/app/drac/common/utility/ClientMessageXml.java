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

package com.nortel.appcore.app.drac.common.utility;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on Jan 3, 2006
 * 
 * @author nguyentd
 */
public class ClientMessageXml {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final String ROOT = "message";
  private static final String MESSAGE_TYPE_ATTR = "type";
  private static final String REPORT_MESSAGE = "report";
  private static final String COMMANDCODE_ATTR = "command";
  public static final String ALARM_EVENT_VALUE = "alarm";
  public static final String DBCHG_EVENT_VALUE = "dbChange";
  public static final String ASSOCIATION_EVENT_VALUE = "association";
  private static final String CTAG_ATTR = "cTag";
  private static final String ATAG_ATTR = "aTag";
  public static final String INPUTDATA_NODE = "data";
  public static final String OUTPUTDATA_NODE = "output";
  private static final String ARG_NODE = "arg";
  public static final String EVENTDATA_NODE = "event";

  /*
   * status of the request
   */
  private static final String EXCEPTION_ATTR = "exception";

  public static final String ACTIVATION_TYPE_ATT = "activationType";
  public static final String PROTECTION_TYPE_ATTR = "protection";
  public Element root;
  private Element inputData;
  protected Element outputData;

  /**
   * Proxy messaging
   */
  public static final String ACTION_NODE = "action";
  public static final String ERRORCODE_NODE = "errorCode";
  public static final String NEID_KEY = "NEID";
  public static final String AID_KEY = "AID";
  public static final String AUTONEGOTIATION_KEY = "AN";
  public static final String MTU_KEY = "MTU";
  public static final String TNA_KEY = "TNA";
  public static final String FACLABEL_KEY = "FACLABEL";
  public static final String COST_KEY = "COST";
  public static final String SIGNALINGTYPE_KEY = "SIGNALINGTYPE";
  public static final String METRIC_KEY = "METRIC";
  public static final String SRLG_KEY = "SRLG";
  public static final String GROUP_KEY = "GROUP";
  public static final String CONSTRAINT_KEY = "CONSTRAINT";
  public static final String DOMAIN_KEY = "DOMAIN";
  public static final String SITE_KEY = "SITE";
  public static final String NEEVENT_NODE = "NetworkElementEvent";
  public static final String NODE_ELEMENT = "node";
  public static final String CROSSCONNECTION_KEY = "CrossConnectionKey";
  public static final String CROSSCONNECTION_LIST_KEY = "CrossConnectionListKey";

  public ClientMessageXml() {
    root = new Element(ROOT);
  }

  public ClientMessageXml(ByteArrayInputStream data) {
    try {
      // incase we want to print latter on
      data.mark(0);
      SAXBuilder builder = new SAXBuilder();
      Document aDoc = builder.build(data);
      root = aDoc.getRootElement();
      inputData = root.getChild(INPUTDATA_NODE);
      outputData = root.getChild(OUTPUTDATA_NODE);
      if (outputData == null) {
        outputData = new Element(OUTPUTDATA_NODE);
        root.addContent(outputData);
      }
    }
    catch (Exception e) {
      data.reset();
      byte[] temp = new byte[data.available()];
      data.read(temp, 0, data.available());
      log.error("Failed to initialize this:" + new String(temp), e);
    }
  }

  /**
   * Convert the event node to the corresponding string representation NOTE: it
   * ASSUMES the first child, the ONLY child, is the event node.
   */
  public String eventNodeToString() {
    String result = null;
    try {
      XMLOutputter outXml = new XMLOutputter(Format.getCompactFormat());
      result = outXml.outputString(getEventNode());
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
    return result;
  }

  public String getAtag() {
    return root.getAttributeValue(ATAG_ATTR);
  }

  public String getCommandCode() {
    return root.getAttributeValue(COMMANDCODE_ATTR);
  }

  public String getCtag() {
    return root.getAttributeValue(CTAG_ATTR);
  }

  /**
   * Convert the event node to the corressponding string representation NOTE: it
   * ASSUMES the first child, the ONLY child, is the event node.
   */
  public Element getEventNode() {
    return (Element) root.getChildren().get(0);
  }

  public String getException() {
    return root.getChild(OUTPUTDATA_NODE).getAttributeValue(EXCEPTION_ATTR);
  }

  public String[] getExceptionArgs() {
    @SuppressWarnings("unchecked")
    List<Element> argElems = root.getChild(OUTPUTDATA_NODE).getChildren(
        ARG_NODE);
    List<String> args = new ArrayList<String>();
    if (argElems != null) {
      Element node = null;
      for (int i = 0; i < argElems.size(); i++) {
        node = argElems.get(i);
        if (node != null) {
          args.add(node.getText());
        }
      }
    }
    String[] ret = args.toArray(new String[args.size()]);
    // final check
    if (ret == null) {
      return new String[0];
    }
    return ret;
  }

  public Element getInputData() {
    return inputData;
  }

  public Element getOutputData() {
    return outputData;
  }

  public boolean isAlarm() {
    String type = root.getAttributeValue(COMMANDCODE_ATTR);
    if (type == null || !type.equals(ClientMessageXml.ALARM_EVENT_VALUE)) {
      return false;
    }
    return true;
  }

  public boolean isReport() {
    return REPORT_MESSAGE.equals(root.getAttributeValue(MESSAGE_TYPE_ATTR));
  }

  public String rootNodeToString() {
    return new XMLOutputter(Format.getCompactFormat()).outputString(root);
  }

  public void setException(String exception) {
    if (outputData == null) {
      outputData = new Element(OUTPUTDATA_NODE);
      root.addContent(outputData);
    }
    outputData.setAttribute(EXCEPTION_ATTR, exception);
  }

  public void setExceptionArgs(Object[] args) {
    if (outputData == null) {
      outputData = new Element(OUTPUTDATA_NODE);
      root.addContent(outputData);
    }
    if (args != null) {
      Element argNode = null;
      for (Object arg : args) {
        argNode = new Element("arg");
        if (arg != null) {
          argNode.setText(arg.toString());
        }
        else {
          argNode.setText("null");
        }
        outputData.addContent(argNode);
      }
    }
  }

  public void setMessageType(String msgType) {
    root.setAttribute(MESSAGE_TYPE_ATTR, msgType);
  }

  public void setOutputData(String data) throws Exception {
    if (outputData == null) {
      outputData = new Element(OUTPUTDATA_NODE);
      root.addContent(outputData);
    }
    ByteArrayInputStream temp = new ByteArrayInputStream(data.getBytes());
    SAXBuilder builder = new SAXBuilder();
    Document aDoc = builder.build(temp);
    outputData.addContent(aDoc.getRootElement().detach());
  }

  @Override
  public String toString() {
    return "ClientMessageXML :" + rootNodeToString();
  }
}
