/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;

/**
 *
 * @author hacksaw
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String argv[]) {

        Configuration config = new Configuration();

        HttpTransportPipe.dump = true;

        ConnectionServiceServer server = null;
        try {
            server = new ConnectionServiceServer();
            server.server(config.getLocalWebServerAddress(), config.getLocalWebServerPort());
        }
        catch (IOException ex) {
            logger.error("Error creating server so exiting...", ex);
            server.shutdown();
            System.exit(0);
        }

        // Allocate and set up an NSI proxy.
        NsiClientProxy example = new NsiClientProxy();

        try {
            example.setProviderEndpoint(config.getProviderEndpoint(), config.getProviderUserId(), config.getProviderPassword());
        }
        catch (IOException ex) {
            logger.error("Error creating NsiClientProxy so exiting...", ex);
            server.shutdown();
            System.exit(0);
        }

        // Build a test reservation.
        Reservation reserve = new Reservation();
        reserve.setCorrelationId(Helper.getUUID());
        reserve.setReplyTo(config.getRequesterEndpoint());
        reserve.setProviderNSA(config.getProviderNsa());
        reserve.setRequesterNSA(config.getRequesterNSA());
        reserve.setGlobalUserName(config.getGlobalUserName());
        reserve.setUserRole(config.getGlobalRole());
        reserve.setGlobalReservationId(Helper.getGlobalReservationId());
        reserve.setDescription(config.getReservationDescription());
        reserve.setSourceStpId(config.getReservationSourceStp());
        reserve.setDestStpId(config.getReservationDestinationStp());
        reserve.setServiceBandwidth(config.getReservationBandwidth());
        reserve.setServiceProtection(config.getReservationProtection());

        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
        DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL);

        // Set reservation start time is now.
        GregorianCalendar startTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        startTime.add(Calendar.MINUTE, 2);
        reserve.setStartTime(startTime);

        logger.info("Reservation startTime " + df.format(startTime.getTime()) + " " + tf.format(startTime.getTime()));

        // Reservation end time is 5 minutes from now.
        GregorianCalendar endTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        endTime.add(Calendar.MINUTE, 4);
        reserve.setEndTime(endTime);

        logger.info("Reservation endTime " + df.format(endTime.getTime()) + " " + tf.format(endTime.getTime()));

        // Convert the reservation into a reservation message.
        ReserveRequestType args = reserve.getReservationRequestType();

        // We get back and ack on success that will contain the correlationId
        // we assigned to the request.
        GenericAcknowledgmentType result = null;
        try {
            logger.info("Sending reservation ---\n" + Helper.dump(ReserveRequestType.class, args));
            result = example.getProxy().reserve(args);
        }
        catch (ServiceException ex) {
            logger.error("Reservation exception - " + ex.getFaultInfo().getErrorId() + " " + ex.getFaultInfo().getText());
            logger.error(Helper.dump(ServiceExceptionType.class, ex.getFaultInfo()));
            server.shutdown();
            System.exit(0);
        }

        // The Provider NSA has accepted our request for processing.
        if (result != null) {
            logger.info("Submitted reservation request successfully correlationId=" + result.getCorrelationId());
        }
        else {
        	logger.info("Nothing happened");
        }
    }
}
