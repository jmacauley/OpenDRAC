<%--
    Document   : index
    Created on : Aug 7, 2011, 11:15:23 PM
    Author     : hacksaw
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="org.opendrac.nsi.domain.DataManager"%>
<%@page import="org.opendrac.nsi.util.SpringApplicationContext"%>
<%@page import="org.opendrac.nsi.domain.StateMachineManager"%>
<%@page import="org.opendrac.nsi.domain.PendingOperationManager"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>OpenDRAC NSI version 1 - Debug</title>
  </head>
  <%
  	// TODO: If this page gets much more complex, change these scribblets to a real JSTL based JSP 
    // with accompanying model and controller
  	DataManager dataMgr = SpringApplicationContext.getBean("dataManager", DataManager.class);
  	StateMachineManager stateMgr = dataMgr.getStateMachineManager();
  	PendingOperationManager operationMgr = dataMgr.getPendingOperationManager();
  %>
  <body>
    <h1>OpenDRAC NSI version 1 - Debug</h1>
    <p>
      This page will hold debug related information for the OpenDRAC NSI implementation once 
      we actually write the code
    </p>
    
    <h2>Configured Topology</h2>
    <div>Topology file: </div>
    <pre>
<%=dataMgr.getTopologyFile()%> <br />
<%=dataMgr.getTopologyFactory()%>
    </pre>
    
    <h2>State Machines</h2>
    <pre>
<%=stateMgr%>
    </pre>
    
    <h2>Pending Operations</h2>
    <pre>
<%=operationMgr%>
    </pre>
  </body>
</html>