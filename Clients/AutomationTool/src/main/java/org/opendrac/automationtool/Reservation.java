package org.opendrac.automationtool;

import static org.opendrac.automationtool.ConfigLoader.DEFAULT_CONFIG_FILE;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCalendar;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.Security;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityDocument;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.UsernameToken;
import org.opendrac.www.ws.resourceallocationandschedulingservice.v3_0.ResourceAllocationAndSchedulingServiceFault;
import org.opendrac.www.ws.resourceallocationandschedulingservice.v3_0.ResourceAllocationAndSchedulingService_v30Stub;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CancelReservationScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ExtendCurrentServiceForScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ExtendCurrentServiceForScheduleResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ExtendCurrentServiceForScheduleT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.PathAvailabilityRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.PathRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.PathT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryPathAvailabilityRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryPathAvailabilityRequestDocument.QueryPathAvailabilityRequest;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleByNamePathUserRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleResponseDocument.QueryReservationScheduleResponse;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationSchedulesByDateTimeAndUserGroupT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationSchedulesRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationSchedulesResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationOccurrenceInfoT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleByNamePathUserT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.UserInfoT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidProtectionTypeT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationOccurrenceCreationResultT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleTypeT;

import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.CompletionResponseDocument;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.CredentialsDocument;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.CredentialsT;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.DracWsFaultT;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.ValidCompletionTypeT;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.ValidCompletionTypeT.Enum;

/**
 * 
 * @author Erno
 */
public class Reservation {

  private static final int timeout = 600000;

  private boolean doSimpleFeedback = false;// if set to true, in stead of
  // XML-sniplets,
  // simpler feedback will be written to console

  // Service address & User credentials variables
  private String userName;
  private String pass;
  private String serviceURL;
  private CredentialsDocument userCredsDoc;
  // Schedule Information
  private String scheduleName;
  private String billingGroup;
  private String startTime;
  private String endTime;
  private String serviceDuration;
  // Lightpath Connectivity
  private String srcUserGroup;
  private String srcUserRsrcGroup;
  private String srcEndpoint;
  private String destEndpoint;
  private String destUserGroup;
  private String destUserRsrcGroup;
  private int rate;
  private String srcVlanID;
  private String destVlanID;
  private String protectionType;
  public String routingAlgorithm;

  private Calendar fixStartTime, fixStartTime2;
  private Calendar fixEndTime, fixEndTime2;
  private TimeZone tz = TimeZone.getTimeZone("GMT+1");
  // private Calendar nowStartTime = Calendar.getInstance(tz);

  private CreateReservationScheduleRequestDocument resSchedReqDoc;
  private CreateReservationScheduleResponseDocument resSchedRespDoc;
  private ResourceAllocationAndSchedulingService_v30Stub sClient;
  private CancelReservationScheduleRequestDocument cancelResSchedReqDoc;
  private QueryReservationScheduleRequestDocument queryResSchedReqDoc; // look
  // at
  // these
  // two
  // objects
  // carefully
  private QueryReservationSchedulesRequestDocument queryResSchedSSReqdoc; // there
  // are
  // different
  // SCHEDULE(S)
  private QueryReservationSchedulesByDateTimeAndUserGroupT queryResSchedByTimeUserGroup;

  private QueryReservationScheduleByNamePathUserRequestDocument byNamePathUserRequestDocument;
  private ExtendCurrentServiceForScheduleRequestDocument extendServiceDocument;

  private ConfigLoader cl = new ConfigLoader();

  private SecurityDocument securityDocument = null;

  private Locale locale = new Locale("nl");

  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private List<String> sentencesNotDisplayed = new ArrayList<String>();

  /**
   * Setup the Reservation class.
   * 
   * Initialize user credentials, and reservation info.
   */
  public Reservation() throws IOException {
    this(DEFAULT_CONFIG_FILE);
  }

  private void initLogger() {
    Logger rootLogger = LogManager.getRootLogger();
    rootLogger.setLevel(Level.ERROR);
  }

  private void initSkippedSentences() {
    sentencesNotDisplayed.add("See server logs for details");
    sentencesNotDisplayed.add("Please see logs for more details");
  }

  /**
   * Setup the Reservation class.
   * 
   * Initialize user credentials, and reservation info.
   * 
   * @param configFile
   *          Fill in path to configuration file.
   */
  public Reservation(String configFile) throws IOException {

    final Options options = new Options();
    options.setProperty(HTTPConstants.SO_TIMEOUT, timeout);
    options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, timeout);

    initLogger();
    initSkippedSentences();
    if (configFile == null || configFile.isEmpty()) {
      configFile = System.getProperty("basedir") + "/" + DEFAULT_CONFIG_FILE.replace("/", File.separator);

    }
    cl.loadConfig(configFile);

    // Service address & User credentials
    userName = cl.Uname;
    pass = cl.Pass;
    serviceURL = cl.serviceURL;
    // Schedule Information
    scheduleName = cl.scheduleName;
    billingGroup = cl.billingGroup;
    startTime = cl.startTime;
    endTime = cl.endTime;
    serviceDuration = cl.serviceDuration;
    // Lightpath Connectivity
    srcUserGroup = cl.sourceUserGroup;
    srcUserRsrcGroup = cl.sourceUserResourceGroup;
    srcEndpoint = cl.sourceEndpoint;
    destEndpoint = cl.destinationEndpoint;
    destUserGroup = cl.destinationUserGroup;
    destUserRsrcGroup = cl.destinationResourceUserGroup;
    rate = Integer.parseInt(cl.rate);
    srcVlanID = cl.sourceVlanID;
    destVlanID = cl.destinationVlANID;
    protectionType = cl.protectionType;
    routingAlgorithm = cl.routingAlgorithm;
    doSimpleFeedback = cl.doSimpleFeedback;

    // Create an XML formatted date string with the parsed start time &
    // endtime
    fixStartTime = Calendar.getInstance(tz);
    fixStartTime.setTimeInMillis(new XmlCalendar(startTime).getTimeInMillis());
    fixEndTime = Calendar.getInstance(tz);
    fixEndTime.setTimeInMillis(new XmlCalendar(endTime).getTimeInMillis());
    // Create a client service stub & and specify URL & timeout
    sClient = new ResourceAllocationAndSchedulingService_v30Stub(serviceURL);
    sClient._getServiceClient().getOptions().setTimeOutInMilliSeconds(120000L); // 2
                                                                                // minutes
                                                                                // in
                                                                                // milliseconds
    // Create credential request doc with same user credentials
    userCredsDoc = CredentialsDocument.Factory.newInstance();
    CredentialsT credsData = CredentialsT.Factory.newInstance();
    credsData.setUserId(userName);
    credsData.setCertificate(pass); // not really a certificate just a
    // password
    userCredsDoc.setCredentials(credsData);
    initSecurityDocument();
  }

  /**
   * Create reservation schedule request document.
   * 
   * Configure the parameters required for making reservations.
   * 
   * @return Returns a CreateReservationScheduleRequestDocument
   */
  public CreateReservationScheduleRequestDocument createResSchedReqDoc() {
    // Create reservation request document
    resSchedReqDoc = CreateReservationScheduleRequestDocument.Factory.newInstance();
    CreateReservationScheduleRequestDocument.CreateReservationScheduleRequest resSchedReq = resSchedReqDoc
        .addNewCreateReservationScheduleRequest();
    ReservationScheduleRequestT reservationSched = resSchedReq.addNewReservationSchedule();
    // Add reservation request document settings
    reservationSched.setName(scheduleName);
    reservationSched.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC);
    reservationSched.setStartTime(fixStartTime);
    reservationSched.setReservationOccurrenceDuration(Integer.parseInt(serviceDuration));
    reservationSched.setIsRecurring(false);
    PathRequestT pathReq = setupPath();
    reservationSched.setPath(pathReq);
    UserInfoT userInfo = setupUser();
    reservationSched.setUserInfo(userInfo);

    return resSchedReqDoc;
  }

  public String getScheduleIdByNamePathUserRequestDocument() throws RemoteException {

    String scheduleId = null;
    try {
      Main.disableSTDOut();
      QueryReservationScheduleResponse queryReservationScheduleResponse = queryReservationScheduleByNamePathUser();
      Main.restoreSTDOut();
      if (queryReservationScheduleResponse.getIsFound()) {
        scheduleId = queryReservationScheduleResponse.getReservationSchedule().getId();
        if (doSimpleFeedback) {
          System.out.println("\nFound schedule with id: " + scheduleId);
        }
        else {
          System.out.println("\nFound schedule: \n" + queryReservationScheduleResponse + "\n\n");
        }
      }
      else {
        System.out.println("\nNo running schedule found with name: " + scheduleName);
      }
    }
    // Catch any exceptions & display the output
    catch (ResourceAllocationAndSchedulingServiceFault e) {
      DracWsFaultT dracFault = e.getFaultMessage().getDracFault();
      System.out.println(processDracError(dracFault.getErrorMsg()));
    }
    return scheduleId;
  }

  public void extendCurrentServiceForSchedule(int minutesToExtend) throws RemoteException,
      ResourceAllocationAndSchedulingServiceFault {
    QueryReservationScheduleResponse queryReservationScheduleResponse = queryReservationScheduleByNamePathUser();
    if (queryReservationScheduleResponse.getIsFound()) {
      String scheduleId = queryReservationScheduleResponse.getReservationSchedule().getId();
      extendServiceForSchedule(scheduleId, minutesToExtend);
    }
    else {
      System.out.println("No service found to extend");
    }
  }

  private QueryReservationScheduleResponse queryReservationScheduleByNamePathUser() throws RemoteException,
      ResourceAllocationAndSchedulingServiceFault {
    ReservationScheduleByNamePathUserT reservationSchedule = ReservationScheduleByNamePathUserT.Factory.newInstance();
    reservationSchedule.setName(scheduleName);
    reservationSchedule.setPath(setupPath());
    reservationSchedule.setUserInfo(setupUser());

    byNamePathUserRequestDocument = QueryReservationScheduleByNamePathUserRequestDocument.Factory.newInstance();
    QueryReservationScheduleByNamePathUserRequestDocument.QueryReservationScheduleByNamePathUserRequest queryReservationScheduleByNamePathUserRequest = byNamePathUserRequestDocument
        .addNewQueryReservationScheduleByNamePathUserRequest();
    queryReservationScheduleByNamePathUserRequest.setReservationSchedule(reservationSchedule);
    Main.disableSTDOut();
    QueryReservationScheduleResponse document = sClient.queryReservationScheduleByNamePathUser(
        byNamePathUserRequestDocument, securityDocument).getQueryReservationScheduleResponse();

    Main.restoreSTDOut();
    return document;
  }

  private ExtendCurrentServiceForScheduleResponseDocument extendCurrentServiceByScheduleId(String scheduleId,
      int minutesToExtend) throws RemoteException, ResourceAllocationAndSchedulingServiceFault {

    ExtendCurrentServiceForScheduleT definition = ExtendCurrentServiceForScheduleT.Factory.newInstance();
    definition.setScheduleId(scheduleId);
    definition.setMinutesToExtend(minutesToExtend);

    extendServiceDocument = ExtendCurrentServiceForScheduleRequestDocument.Factory.newInstance();

    ExtendCurrentServiceForScheduleRequestDocument.ExtendCurrentServiceForScheduleRequest request = extendServiceDocument
        .addNewExtendCurrentServiceForScheduleRequest();
    request.setExtensionDefinition(definition);
    Main.disableSTDOut();
    ExtendCurrentServiceForScheduleResponseDocument document = sClient.extendCurrentServiceForSchedule(
        extendServiceDocument, securityDocument);
    Main.restoreSTDOut();
    return document;
  }

  private void extendServiceForSchedule(String scheduleId, int minutesToExtend) throws RemoteException,
      ResourceAllocationAndSchedulingServiceFault {

    ExtendCurrentServiceForScheduleResponseDocument respDoc = extendCurrentServiceByScheduleId(scheduleId,
        minutesToExtend);
    if (doSimpleFeedback) {
      System.out.println("Result for extend service: "
          + respDoc.getExtendCurrentServiceForScheduleResponse().getResultString());
      String minutesSemantics = " minutes";
      if (respDoc.getExtendCurrentServiceForScheduleResponse().getMinutesExtended() == 1) {
        minutesSemantics = " minute";
      }
      System.out.println("Service is extended with : "
          + respDoc.getExtendCurrentServiceForScheduleResponse().getMinutesExtended() + minutesSemantics);
    }
    else {
      System.out.println("<------EXTENSION RESPONSE------->\n\n" + respDoc.getExtendCurrentServiceForScheduleResponse()
          + "\n\n" + "<--------------------------------->");
    }
  }

  public UserInfoT setupUser() {
    // User info data
    UserInfoT userInfo = UserInfoT.Factory.newInstance();
    userInfo.setBillingGroup(billingGroup);
    userInfo.setSourceEndpointResourceGroup(srcUserRsrcGroup);
    userInfo.setSourceEndpointUserGroup(srcUserGroup);
    userInfo.setTargetEndpointResourceGroup(destUserRsrcGroup);
    userInfo.setTargetEndpointUserGroup(destUserGroup);
    return userInfo;
  }

  public PathRequestT setupPath() {
    // Service path data
    PathRequestT pathReq = PathRequestT.Factory.newInstance();
    pathReq.setSourceTna(srcEndpoint);
    pathReq.setTargetTna(destEndpoint);
    pathReq.setRate(rate);
    pathReq.setSourceVlanId(srcVlanID);
    pathReq.setTargetVlanId(destVlanID);
    pathReq.setRoutingAlgorithm(routingAlgorithm);
    ValidProtectionTypeT.Enum pType = ValidProtectionTypeT.Enum.forString(protectionType);
    pathReq.setProtectionType(pType);
    return pathReq;
  }

  /**
   * Create reservation schedule request document.
   * 
   * Configure the parameters required for making reservations.
   * 
   * @param duration
   *          specifies how long the reservation should last for.
   * @return Returns a CreateReservationScheduleRequestDocument
   */
  public CreateReservationScheduleRequestDocument createResSchedReqDoc(String duration) {

    // Create reservation request document
    resSchedReqDoc = CreateReservationScheduleRequestDocument.Factory.newInstance();
    CreateReservationScheduleRequestDocument.CreateReservationScheduleRequest resSchedReq = resSchedReqDoc
        .addNewCreateReservationScheduleRequest();
    ReservationScheduleRequestT reservationSched = resSchedReq.addNewReservationSchedule();
    // Add reservation request document settings
    reservationSched.setName(scheduleName);
    reservationSched.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC);

    reservationSched.setStartTime(fixStartTime);
    reservationSched.setReservationOccurrenceDuration(Integer.parseInt(duration));
    reservationSched.setIsRecurring(false);

    UserInfoT userInfo = setupUser();
    PathRequestT pathReq = setupPath();
    reservationSched.setPath(pathReq);
    reservationSched.setUserInfo(userInfo);

    return resSchedReqDoc;
  }

  /**
   * Create reservation schedule request document
   * 
   * Configure the parameters required for making reservations. However the
   * startTime is automatically set to the current time.
   * 
   * @param duration
   *          specifies how long the reservation should last for.
   * @return Returns a CreateReservationScheduleRequestDocument
   */
  public CreateReservationScheduleRequestDocument createResSchedReqDocNow(String duration) {

    int durationI = Long.valueOf(duration).intValue();
    // Create reservation request document
    resSchedReqDoc = CreateReservationScheduleRequestDocument.Factory.newInstance();
    CreateReservationScheduleRequestDocument.CreateReservationScheduleRequest resSchedReq = resSchedReqDoc
        .addNewCreateReservationScheduleRequest();
    ReservationScheduleRequestT reservationSched = resSchedReq.addNewReservationSchedule();
    // Add reservation request document settings
    reservationSched.setName(scheduleName);
    reservationSched.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC);
    Calendar startTime = Calendar.getInstance(locale);
    Calendar endTime = Calendar.getInstance(locale);
    endTime.setTime(startTime.getTime());
    endTime.roll(Calendar.MINUTE, durationI);
    reservationSched.setStartTime(startTime);

    reservationSched.setReservationOccurrenceDuration(durationI);
    reservationSched.setIsRecurring(false);

    UserInfoT userInfo = setupUser();
    PathRequestT pathReq = setupPath();
    reservationSched.setUserInfo(userInfo);
    reservationSched.setPath(pathReq);
    return resSchedReqDoc;
  }

  /**
   * Create reservation schedule request document
   * 
   * Configure the parameters required for making reservations. However the
   * startTime is automatically set to the current time.
   * 
   * @return Returns a CreateReservationScheduleRequestDocument
   */
  public CreateReservationScheduleRequestDocument createResSchedReqDocNow() {
    // Create reservation request document
    resSchedReqDoc = CreateReservationScheduleRequestDocument.Factory.newInstance();
    CreateReservationScheduleRequestDocument.CreateReservationScheduleRequest resSchedReq = resSchedReqDoc
        .addNewCreateReservationScheduleRequest();
    ReservationScheduleRequestT reservationSched = resSchedReq.addNewReservationSchedule();
    // Add reservation request document settings
    reservationSched.setName(scheduleName);
    reservationSched.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC);

    reservationSched.setStartTime(Calendar.getInstance(locale));
    reservationSched.setReservationOccurrenceDuration(Integer.parseInt(serviceDuration));
    reservationSched.setIsRecurring(false);
    PathRequestT pathReq = setupPath();
    reservationSched.setPath(pathReq);
    UserInfoT userInfo = setupUser();
    reservationSched.setUserInfo(userInfo);
    return resSchedReqDoc;
  }

  /**
   * Make a reservation schedule request.
   * 
   * This calls the createResSchedReqDoc() method to retrieve a a reservation
   * request document. This document is then sent to the OpenDRAC web service.
   * 
   * Console output is the completion response sent from OpenDRAC
   */
  public void makeReservation() throws Exception {
    // Create credential request doc with same user credentials
    handleCreateReservation(createResSchedReqDoc());
  }

  /**
   * Make a reservation schedule request.
   * 
   * This calls the createResSchedReqDoc() method to retrieve a a reservation
   * request document. This document is then sent to the OpenDRAC web service.
   * However the startTime is automatically set to the current time.
   * 
   * Console output is the completion response sent from OpenDRAC
   */
  public void makeReservationNow() throws Exception {
    // Create credential request doc with same user credentials
    handleCreateReservation(createResSchedReqDocNow());
  }

  /**
   * Make a reservation schedule request.
   * 
   * This calls the createResSchedReqDoc() method to retrieve a a reservation
   * request document. This document is then sent to the OpenDRAC web service.
   * However the startTime set by specifying a value for the the duration
   * parameter.
   * 
   * Console output is the completion response sent from OpenDRAC
   * 
   * @param duration
   *          specify duration
   */
  public void makeReservationNow(String duration) throws Exception {
    // Create credential request doc with same user credentials
    handleCreateReservation(createResSchedReqDocNow(duration));
  }

  private void handleCreateReservation(CreateReservationScheduleRequestDocument createReservationScheduleRequestDocument)
      throws RemoteException {
    try {
      // Attempt to make a reservation request
      // Store the server response in a response doc & display the
      // response
      Main.disableSTDOut();
      resSchedRespDoc = sClient.createReservationSchedule(createReservationScheduleRequestDocument, securityDocument);
      Main.restoreSTDOut();
      if (doSimpleFeedback) {
        System.out.println("Reservation schedule id: "
            + resSchedRespDoc.getCreateReservationScheduleResponse().getReservationScheduleId());
        System.out.println("Result: " + resSchedRespDoc.getCreateReservationScheduleResponse().getResult());
        ReservationOccurrenceInfoT[] occurrenceInfoTs = resSchedRespDoc.getCreateReservationScheduleResponse()
            .getOccurrenceInfoArray();
        for (int infoCounter = 0; resSchedRespDoc != null && infoCounter < occurrenceInfoTs.length; infoCounter++) {
          ReservationOccurrenceInfoT infoT = occurrenceInfoTs[infoCounter];
          System.out.println("Occurrence#" + infoCounter + ": result " + infoT.getResult());
          if (!infoT.getResult().equals(ValidReservationOccurrenceCreationResultT.SUCCEEDED)) {
            System.out.println("Occurrence#" + infoCounter + ": reason " + infoT.getReason());
          }
        }
      }
      else {
        System.out.println("<------CREATE SCHEDULE RESPONSE------->\n\n"
            + resSchedRespDoc.getCreateReservationScheduleResponse() + "\n\n"
            + "<------------------------------------->");
      }
    }
    // Catch any exceptions & display the output
    catch (ResourceAllocationAndSchedulingServiceFault e) {
      DracWsFaultT dracFault = e.getFaultMessage().getDracFault();
      System.out.println(processDracError(dracFault.getErrorMsg()));
    }
  }

  private String processDracError(String rawErrorMessage) {
    String[] errorParts = rawErrorMessage.split("Exception");
    String message = errorParts[errorParts.length - 1];
    message = removeCollons(message);
    message = removeUnwantedSentences(message);
    return "Error: " + message.trim();
  }

  private String removeCollons(String rawErrorMessage) {
    String message = rawErrorMessage.replace(':', ' ');
    return message;
  }

  private String removeUnwantedSentences(String rawErrorMessage) {
    Set<String> sentences = new HashSet<String>();
    StringBuilder resultingMessage = new StringBuilder();
    String[] messageParts = rawErrorMessage.split("\\.");
    for (String part : messageParts) {
      sentences.add(part.trim());
    }
    for (String sentence : sentences) {
      if (sentence.length() > 0 && !sentencesNotDisplayed.contains(sentence)) {
        resultingMessage.append(sentence);
        resultingMessage.append(". ");
      }
    }
    return resultingMessage.toString();
  }

  /**
   * Display Schedule status for a specific Schedule.
   * 
   */
  public void scheduleStatus(String scheduleId) throws Exception {
    // Create a Query Reservation request document with the same user
    // credentials
    queryResSchedReqDoc = QueryReservationScheduleRequestDocument.Factory.newInstance();
    queryResSchedReqDoc.addNewQueryReservationScheduleRequest();
    queryResSchedReqDoc.getQueryReservationScheduleRequest().setReservationScheduleId(scheduleId);
    // Attempt to query a reservation to check its status
    try {
      Main.disableSTDOut();
      QueryReservationScheduleResponseDocument queryRespDoc = sClient.queryReservationSchedule(queryResSchedReqDoc,
          securityDocument);
      Main.restoreSTDOut();
      boolean isFound = queryRespDoc.getQueryReservationScheduleResponse().getIsFound();

      if (doSimpleFeedback) {
        System.out.println("   <-----------SCHEDULE STATUS----------->");
        if (isFound) {
          ReservationScheduleT schedule = queryRespDoc.getQueryReservationScheduleResponse().getReservationSchedule();
          PathT path = schedule.getPath();
          System.out.println("   id:              " + schedule.getId());
          System.out.println("   name:            " + schedule.getName());
          System.out.println("   can be canceled: " + schedule.getIsCanceble());
          System.out.println("   status:          " + schedule.getStatus());
          System.out.println("   type:            " + schedule.getType());
          System.out.println("   start:           " + dateAsString(schedule.getStartTime()));
          System.out.println("   duration(min.):  " + schedule.getReservationOccurrenceDuration());
          System.out.println("   recurring:       " + schedule.getIsRecurring());

          System.out.println("   source tna:       " + path.getSourceTna());
          System.out.println("   target tna:       " + path.getTargetTna());
          System.out.println("   protection type:  " + path.getProtectionType());

          System.out.println("   user id:          " + schedule.getUserId());
          System.out.println("   billing group:    " + schedule.getUserInfo().getBillingGroup());
          System.out.println("   is active:        " + schedule.getActivated());
        }
        else {
          System.out.println("   No schedule with id=" + scheduleId + " found");
        }
      }
      else {
        System.out.println("<---------SCHEDULE STATUS--------->\n\n"
            + queryRespDoc.getQueryReservationScheduleResponse() + "\n\n" + "<--------------------------------->");
      }
    }
    // Catch any exceptions & display the output
    catch (ResourceAllocationAndSchedulingServiceFault e) {
      DracWsFaultT dracFault = e.getFaultMessage().getDracFault();
      System.out.println(processDracError(dracFault.getErrorMsg()));
    }
  }

  private String dateAsString(Calendar calendar) {
    return sdf.format(calendar.getTime());
  }

  /**
   * List reservation schedules.
   * 
   * Query a list of schedules from the OpenDRAC web service. Console output is
   * a list of reservation schedules.
   */

  public void listSchedules() throws Exception {
    // Create a Query Reservation Schedule(S) document(note this is a
    // totally different doc than the one above)
    // once again with the same user credentials
    queryResSchedSSReqdoc = QueryReservationSchedulesRequestDocument.Factory.newInstance();
    QueryReservationSchedulesRequestDocument.QueryReservationSchedulesRequest queryResSchedSSReqData = queryResSchedSSReqdoc
        .addNewQueryReservationSchedulesRequest();

    queryResSchedByTimeUserGroup = QueryReservationSchedulesByDateTimeAndUserGroupT.Factory.newInstance();
    queryResSchedSSReqData.addNewCriteria();
    queryResSchedByTimeUserGroup.setStartTime(fixStartTime);
    queryResSchedByTimeUserGroup.setEndTime(fixEndTime);
    queryResSchedByTimeUserGroup.setUserGroup(srcUserGroup);
    queryResSchedSSReqData.setCriteria(queryResSchedByTimeUserGroup);
    // Attempt to query a list of schedules within the fixStartTime and
    // fixEndTime intervals Display the response
    try {
      Main.disableSTDOut();
      QueryReservationSchedulesResponseDocument queryRespSchedSSDoc = sClient.queryReservationSchedules(
          queryResSchedSSReqdoc, securityDocument);
      Main.restoreSTDOut();
      System.out.println("<---------LISTED SCHEDULES--------->\n\n"
          + queryRespSchedSSDoc.getQueryReservationSchedulesResponse() + "\n\n"
          + "<---------------------------------->");
    }
    catch (ResourceAllocationAndSchedulingServiceFault e) {
      DracWsFaultT dracFault = e.getFaultMessage().getDracFault();
      System.out.println(processDracError(dracFault.getErrorMsg()));
    }

  }

  /**
   * List reservation schedules.
   * 
   * Query a list of schedules from the OpenDRAC web service with a specified
   * interval.
   * 
   * @param startTime2
   *          start date & time in the XML format YYYY-MM-DDThh:mm:ss.
   * @param endtime
   *          end date date & time in the XML format YYYY-MM-DDThh:mm:ss.
   * 
   *          Console output is a list of reservation schedules
   */
  public void listSchedules(String startTime2, String endTime2) throws Exception {
    // Create an XML formatted date string with the parsed start time & endtime
    fixStartTime2 = Calendar.getInstance(tz);
    fixStartTime2.setTimeInMillis(new XmlCalendar(startTime2).getTimeInMillis());
    fixEndTime2 = Calendar.getInstance(tz);
    fixEndTime2.setTimeInMillis(new XmlCalendar(endTime2).getTimeInMillis());
    // Create a Query Reservation Schedule(S) document(not this is a totaly
    // different doc than the one above)
    // once again with the same user credentials
    queryResSchedSSReqdoc = QueryReservationSchedulesRequestDocument.Factory.newInstance();
    QueryReservationSchedulesRequestDocument.QueryReservationSchedulesRequest queryResSchedSSReqData = queryResSchedSSReqdoc
        .addNewQueryReservationSchedulesRequest();

    queryResSchedByTimeUserGroup = QueryReservationSchedulesByDateTimeAndUserGroupT.Factory.newInstance();
    queryResSchedSSReqData.addNewCriteria();
    queryResSchedByTimeUserGroup.setStartTime(fixStartTime2);
    queryResSchedByTimeUserGroup.setEndTime(fixEndTime2);
    queryResSchedByTimeUserGroup.setUserGroup(srcUserGroup);
    queryResSchedSSReqData.setCriteria(queryResSchedByTimeUserGroup);
    // Attempt to query a list of schedules within the fixStartTime and
    // fixEndTime intervals Display the response
    try {
      Main.disableSTDOut();
      QueryReservationSchedulesResponseDocument queryRespSchedSSDoc = sClient.queryReservationSchedules(
          queryResSchedSSReqdoc, securityDocument);
      Main.restoreSTDOut();
      System.out.println("<---------LISTED SCHEDULES--------->\n\n"
          + queryRespSchedSSDoc.getQueryReservationSchedulesResponse() + "\n\n"
          + "<---------------------------------->");
    }
    catch (ResourceAllocationAndSchedulingServiceFault e) {
      DracWsFaultT dracFault = e.getFaultMessage().getDracFault();
      System.out.println(processDracError(dracFault.getErrorMsg()));
    }
  }

  /**
   * Cancel reservation schedules
   * 
   * Query a list of schedules from the OpenDRAC web service with a specified
   * interval.
   * 
   * @param startTime2
   *          start date & time in the XML format YYYY-MM-DDThh:mm:ss.
   * @param endtime
   *          end date date & time in the XML format YYYY-MM-DDThh:mm:ss.
   *          Console output is the completion response from OpenDRAC
   */
  public void cancelSchedule(String id) throws Exception {
    // Create a Cancel Reservation Schedule document with your user
    // credentials
    cancelResSchedReqDoc = CancelReservationScheduleRequestDocument.Factory.newInstance();
    cancelResSchedReqDoc.addNewCancelReservationScheduleRequest();
    cancelResSchedReqDoc.getCancelReservationScheduleRequest().setReservationScheduleId(id);
    // Attempt to cancel a reservation schedule & display the output
    try {
      Main.disableSTDOut();
      CompletionResponseDocument cancelRespDoc = sClient.cancelReservationSchedule(cancelResSchedReqDoc,
          securityDocument);
      Main.restoreSTDOut();
      if (doSimpleFeedback) {
        String XML = cancelRespDoc.getCompletionResponse().xmlText();
        String result = XML.substring(XML.indexOf("<res:result>") + "<res:result>".length(),
            XML.indexOf("</res:result>"));
        System.out.println("Result for terminate service: " + result);
      }
      else {
        System.out.println("<------CANCELATION RESPONSE------->\n\n" + cancelRespDoc.getCompletionResponse() + "\n\n"
            + "<--------------------------------->");
      }
    }
    catch (ResourceAllocationAndSchedulingServiceFault e) {
      DracWsFaultT dracFault = e.getFaultMessage().getDracFault();
      System.out.println(processDracError(dracFault.getErrorMsg()));
    }
  }

  public void terminateCurrentSchedule() throws Exception {
    QueryReservationScheduleResponse queryReservationScheduleResponse = queryReservationScheduleByNamePathUser();
    if (queryReservationScheduleResponse.getIsFound()
        && queryReservationScheduleResponse.getReservationSchedule().getIsCanceble()) {
      String scheduleId = queryReservationScheduleResponse.getReservationSchedule().getId();
      cancelSchedule(scheduleId);
    }
    else {
      if (queryReservationScheduleResponse.getIsFound()) {
        System.out.println("Schedule already terminated");
      }
      else {
        System.out.println("No schedule found to terminate");
      }
    }
  }

  private boolean scheduleCurrentlyRunning() throws RemoteException, ResourceAllocationAndSchedulingServiceFault {
    QueryReservationScheduleResponse queryReservationScheduleResponse = queryReservationScheduleByNamePathUser();
    return scheduleRunning(queryReservationScheduleResponse);
  }

  private boolean scheduleRunning(QueryReservationScheduleResponse queryReservationScheduleResponse) {
    boolean isRunning = false;
    Date now = new Date();
    if (queryReservationScheduleResponse.getReservationSchedule() != null) {
      long startTime = queryReservationScheduleResponse.getReservationSchedule().getStartTime().getTimeInMillis();
      long duration = queryReservationScheduleResponse.getReservationSchedule().getReservationOccurrenceDuration() * 60 * 1000;
      if (now.getTime() > startTime && now.getTime() < (startTime + duration)) {
        isRunning = true;
      }
    }
    return isRunning;
  }

  public void resumeCurrentService() throws Exception {
    if (scheduleCurrentlyRunning()) {
      System.out.println("Schedule is already running.\nTerminate current schedule first.");
    }
    else {
      makeReservationNow(serviceDuration);
    }
  }

  public void showStatusInfoCurrentService() throws Exception {
    String scheduleId = getScheduleIdByNamePathUserRequestDocument();
    if (!(scheduleId == null || scheduleId.trim().equals(""))) {
      scheduleStatus(scheduleId);
    }
  }

  private void initSecurityDocument() {
    securityDocument = SecurityDocument.Factory.newInstance();
    final Security security = securityDocument.addNewSecurity();
    final UsernameToken token = security.addNewUsernameToken();
    token.setUsername(userName);
    token.setPassword(pass);
  }

  public void queryPathAvailability() throws RemoteException, ResourceAllocationAndSchedulingServiceFault {

    final QueryPathAvailabilityRequestDocument requestDocument = QueryPathAvailabilityRequestDocument.Factory
        .newInstance();

    final QueryPathAvailabilityRequest request = QueryPathAvailabilityRequest.Factory.newInstance();
    final PathAvailabilityRequestT pathAvailability = PathAvailabilityRequestT.Factory.newInstance();
    final PathRequestT path = setupPath();
    final UserInfoT userInfo = setupUser();

    pathAvailability.setEndTime(fixEndTime);
    pathAvailability.setPath(path);
    pathAvailability.setStartTime(fixStartTime);
    pathAvailability.setUserInfo(userInfo);
    request.setPathAvailability(pathAvailability);
    requestDocument.setQueryPathAvailabilityRequest(request);

    final CompletionResponseDocument queryPathAvailabilityResponse = sClient.queryPathAvailability(requestDocument,
        securityDocument);
    final Enum result = queryPathAvailabilityResponse.getCompletionResponse().getResult();

    switch (result.intValue()) {
    case ValidCompletionTypeT.INT_SUCCESS:
      System.out.println("Path is available");
      System.exit(0);
      break;

    default:
      System.out.println("Path is not available");
      System.exit(1);
      break;
    }

  }

}