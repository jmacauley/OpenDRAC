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

package com.nortel.appcore.app.drac.server.webserver.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracRemoteException;
import com.nortel.appcore.app.drac.common.errorhandling.InvalidLoginException;
import com.nortel.appcore.app.drac.common.errorhandling.RoutingException;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfile;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.EndpointResourceUiType;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.TaskType.State;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.common.utility.OpticalUtility;
import com.nortel.appcore.app.drac.common.utility.OpticalUtility.OpticalPortType;
import com.nortel.appcore.app.drac.common.utility.StringParser;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;

public final class DracServlet extends HttpServlet {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static final long serialVersionUID = 1L;
	private RequestHandler requestHandler;
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {
		doProcess(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {
		doProcess(request, response);
	}

	@Override
	public void init(ServletConfig config) {
		log.debug("initializing...");
		this.requestHandler = RequestHandler.INSTANCE;
	}

	private void doProcess(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		String action = request.getParameter("action");
		// start a new task
		if ("pollStatus".equals(action)) {
			processPollStatus(request, response);
		}
		else if ("querySched".equals(action)) {
			processQuerySched(request, response);
		}
		else if ("querySchedForTime".equals(action)) {
			processQuerySchedForTime(request, response);
		}
		else if ("getWavelengthForResGrp".equals(action)) {
			processGetWavelengthsForResGrp(request, response);
		}
		else if ("getTnaForResGrp".equals(action)) {
			processGetTnaForResourceGroup(request, response);
		}
		else if ("getTnaForResGrpByWL".equals(action)) {
			processGetTnaForResourceGroupByWavelength(request, response);
		}
		else if ("getTnaForResGrpByLayer".equals(action)) {
			processGetTnaForResourceGroupByLayer(request, response);
		}
		else if ("getSiteForResGrp".equals(action)) {
			processGetSiteForResourceGroup(request, response);
		}
		else if ("getResGrpForUserGrp".equals(action)) {
			processGetResourceGroupForUserGroup(request, response);
		}
		else if ("getUserGrpForUser".equals(action)) {
			processGetUserGroupForUser(request, response);
		}
		else if ("getChannelsForTna".equals(action)) {
			processGetChannelsForTna(request, response);
		}
		else if ("queryUtil".equals(action)) {
			processQueryUtil(request, response);
		}
		else if("extendService".equals(action)){
			processExtendServiceTime(request, response);
		}
	}

	private void processGetChannelsForTna(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		// This method is now authenticated
		LoginToken token = (LoginToken) request.getSession().getAttribute(
		    DracConstants.TOKEN_OBJ);

		String tna = request.getParameter("tna");
		String channelsAvailable = "";
		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String message = "";

		try {
			message = "<error>"
			    + MessageFormat.format(bundle.getString("drac.error.ajax.noPort"),
			        tna) + "</error>";
		}
		catch (MissingResourceException e) {
			message = "<error>Could not find endpoint " + tna + "</error>";
		}

		try {
			if (tna != null && !tna.equals("")) {
				Map<String, String> filter = new HashMap<String, String>();
				filter.put(DbKeys.NetworkElementFacilityCols.TNA, tna);
				filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
				    Layer.LAYER1.toString());

				// Retrieve the single server-side-policy-checked ep from the server
				EndPointType layer1EndpointByTna = requestHandler.retrieveEndpoint(token, filter);

				if (layer1EndpointByTna != null) {
					BigInteger constrain = layer1EndpointByTna.getConstrain();

					String type = layer1EndpointByTna.getType();
					OpticalPortType port = OpticalUtility.lookupOptical(type);
					if (port != null) {
						int channels = port.getChannels();
						for (int i = 1; i <= channels; i++) {
							if (!constrain.testBit(i)) {
								channelsAvailable += "<channel>" + Integer.toString(i)
								    + "</channel>\n";
							}
						}
						message = "<channelList>" + channelsAvailable + "</channelList>";
					}
					else {
						log.warn("Layer1 endpoint with type " + type
						    + " is not a valid optical port");
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Unexpected exception caught", e);
			if(e instanceof InvalidLoginException){
				response.setStatus(401);
			}			
		}
		finally {
			sendResponse(response, message);
		}
	}


	private void processGetResourceGroupForUserGroup(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		UserGroupName userGroup = new UserGroupName(request.getParameter("gid"));

		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String message = "";
		try {
			message = "<error>"
			    + MessageFormat.format(
			        bundle.getString("drac.error.ajax.noUserGroup"), userGroup)
			    + "</error>";
		}
		catch (MissingResourceException e) {
			message = "<error>Could not find user group " + userGroup + "</error>";
		}

		if (userGroup != null && !userGroup.toString().equals("")) {
			LoginToken token = (LoginToken) request.getSession().getAttribute(
			    DracConstants.TOKEN_OBJ);

			if (token != null) {
				try {
					UserGroupProfile ugp = null;
					synchronized (requestHandler) {
						ugp = this.requestHandler.getUserGroupProfile(token, userGroup);
					}

					if (ugp != null) {
						Set<String> resGroups = ugp.getMembership()
						    .getMemberResourceGroupName();
						StringBuilder buf = new StringBuilder();
						for (String resGroup : resGroups) {
							buf.append("<group>" + resGroup + "</group>\n");
						}
						message = "<groupList>\n" + buf.toString() + "</groupList>";
					}
				}
				catch (RequestHandlerException re) {
					log.error("Error looking up user group " + userGroup, re);
				}
				catch (Exception e) {
					log.error("Unexpected exception caught", e);
					if(e instanceof InvalidLoginException){
						response.setStatus(401);
					}					
				}
				finally {
					sendResponse(response, message);
				}
			}
		}

	}

	private void processGetSiteForResourceGroup(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		String resGroup = request.getParameter("gid");

		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String message = "";
		try {
			message = "<error>"
			    + MessageFormat.format(
			        bundle.getString("drac.error.ajax.noResourceGroup"), resGroup)
			    + "</error>";
		}
		catch (MissingResourceException e) {
			message = "<error>Could not find resource group " + resGroup + "</error>";
		}

		if (resGroup != null && !resGroup.equals("")) {
			LoginToken token = (LoginToken) request.getSession().getAttribute(
			    DracConstants.TOKEN_OBJ);

			if (token != null) {
				try {
					Map<String, String> filter = new HashMap<String, String>();
					filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
					    Layer.LAYER_ALL.toString());

					List<EndpointResourceUiType> list = requestHandler.getUserEndpointsUIInfo(token,
					    resGroup, filter);
					Set<String> sites = new TreeSet<String>();
					for (EndpointResourceUiType epInfo : list) {
						String site = epInfo.getSite();
						if (site != null && !site.equals("") & !site.equals("N/A")) {
							sites.add(epInfo.getSite());
						}
					}

					StringBuilder buf = new StringBuilder();
					for (String site : sites) {
						buf.append("<site>" + site + "</site>\n");
					}
					message = "<siteList>\n" + buf.toString() + "</siteList>";
				}

				catch (Exception e) {
					log.error(
					    "Exception calling getEndpointResourceUIInfo for resource group "
					        + resGroup, e);
					if(e instanceof InvalidLoginException){
						response.setStatus(401);
					}					
				}
				finally {
					sendResponse(response, message);
				}
			}
		}

	}

	private void processGetTnaForResourceGroup(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		String resGroup = request.getParameter("gid");

		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String message = "";
		try {
			message = "<error>"
			    + MessageFormat.format(
			        bundle.getString("drac.error.ajax.noResourceGroup"), resGroup)
			    + "</error>";
		}
		catch (MissingResourceException e) {
			message = "<error>Could not find resource group " + resGroup + "</error>";
		}

		if (resGroup != null && !resGroup.equals("")) {
			LoginToken token = (LoginToken) request.getSession().getAttribute(
			    DracConstants.TOKEN_OBJ);

			if (token != null) {
				try {
					Map<String, String> filter = new HashMap<String, String>();
					filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
					    Layer.LAYER_ALL.toString());

					List<EndpointResourceUiType> list = requestHandler.getUserEndpointsUIInfo(token,
					    resGroup, filter);
					Set<String> tnas = new TreeSet<String>();
					for (EndpointResourceUiType epInfo : list) {
						tnas.add(StringParser.encodeForDRACSpecialChars(epInfo.getTna()));
					}

					StringBuilder buf = new StringBuilder();
					for (String tna : tnas) {
						buf.append("<tna>" + tna + "</tna>\n");
					}
					message = "<tnaList>\n" + buf.toString() + "</tnaList>";

				}
				catch (Exception e) {
					log.error(
					    "Exception calling getEndpointResourceUIInfo for resource group "
					        + resGroup, e);
					if(e instanceof InvalidLoginException){
						response.setStatus(401);
					}					
				}
				finally {
					sendResponse(response, message);
				}
			}
		}
	}

	private void processGetTnaForResourceGroupByLayer(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		String resGroup = request.getParameter("gid");
		String layer = request.getParameter("layer");
		String site = request.getParameter("site");
		if (site != null && (site.equals("") || site.equals("N/A"))) {
			site = null;
		}

		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String message = "";
		try {
			message = "<error>"
			    + MessageFormat.format(
			        bundle.getString("drac.error.ajax.noResourceGroup"), resGroup)
			    + "</error>";
		}
		catch (MissingResourceException e) {
			message = "<error>Could not find resource group " + resGroup + "</error>";
		}

		if (resGroup != null && !resGroup.equals("")) {
			LoginToken token = (LoginToken) request.getSession().getAttribute(
			    DracConstants.TOKEN_OBJ);
			if (token != null) {
				try {
					Map<String, String> filter = new HashMap<String, String>();
					filter.put(DbKeys.NetworkElementFacilityCols.LAYER, layer);
					if (site != null) {
						filter.put(DbKeys.NetworkElementFacilityCols.SITE, site);
					}

					List<EndpointResourceUiType> list = requestHandler.getUserEndpointsUIInfo(token,
					    resGroup, filter);
					Set<String> tnas = new TreeSet<String>();
					for (EndpointResourceUiType epInfo : list) {
						String compoundTnaLabel = StringParser
						    .encodeForDRACSpecialChars(epInfo.getTna());

						if (epInfo.getLabel() != null && epInfo.getLabel().length() > 0) {
							// Create encoded compound string: tna::label, decoding on
							// receipt!!!
							compoundTnaLabel = compoundTnaLabel + "::"
							    + StringParser.encodeForDRACSpecialChars(epInfo.getLabel());
						}

						tnas.add(compoundTnaLabel);
					}

					StringBuilder buf = new StringBuilder();
					for (String tna : tnas) {
						buf.append("<tna>" + tna + "</tna>\n");
					}
					message = "<tnaList>\n" + buf.toString() + "</tnaList>";

				}
				catch (Exception e) {
					log.error(
					    "Exception calling getEndpointResourceUIInfo for resource group "
					        + resGroup, e);
					if(e instanceof InvalidLoginException){
						response.setStatus(401);
					}					
				}
				finally {
					sendResponse(response, message);
				}
			}
		}
	}

	private void processGetTnaForResourceGroupByWavelength(
	    HttpServletRequest request, HttpServletResponse response)
	    throws IOException {
		String resGroup = request.getParameter("gid");
		String wavelength = request.getParameter("wavelength");
		String site = request.getParameter("site");
		if (site != null && (site.equals("") || site.equals("N/A"))) {
			site = null;
		}

		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String message = "";
		try {
			message = "<error>"
			    + MessageFormat.format(
			        bundle.getString("drac.error.ajax.noResourceGroup"), resGroup)
			    + "</error>";
		}
		catch (MissingResourceException e) {
			message = "<error>Could not find resource group " + resGroup + "</error>";
		}

		if (resGroup != null && !resGroup.equals("")) {
			LoginToken token = (LoginToken) request.getSession().getAttribute(
			    DracConstants.TOKEN_OBJ);
			if (token != null) {
				try {
					// Note: the db call into facilities has not been indexed on
					// wavelength.
					// It will perform an indexed retrieval on layer0, but the lookup for
					// the
					// requested wavelength will continue to be done here ...
					Map<String, String> filter = new HashMap<String, String>();
					filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
					    Layer.LAYER0.toString());
					if (site != null) {
						filter.put(DbKeys.NetworkElementFacilityCols.SITE, site);
					}

					List<EndpointResourceUiType> list = requestHandler.getUserEndpointsUIInfo(token,
					    resGroup, filter);
					Set<String> tnas = new TreeSet<String>();
					for (EndpointResourceUiType epInfo : list) {
						// Wavelength check:
						if (epInfo.getWavelength().equals(wavelength)) {
							String compoundTnaLabel = StringParser
							    .encodeForDRACSpecialChars(epInfo.getTna());

							if (epInfo.getLabel() != null && epInfo.getLabel().length() > 0) {
								// Create encoded compound string: tna::label, decoding on
								// receipt!!!
								compoundTnaLabel = compoundTnaLabel + "::"
								    + StringParser.encodeForDRACSpecialChars(epInfo.getLabel());
							}

							tnas.add(compoundTnaLabel);
						}
					}

					StringBuilder buf = new StringBuilder();
					for (String tna : tnas) {
						buf.append("<tna>" + tna + "</tna>\n");
					}
					message = "<tnaList>\n" + buf.toString() + "</tnaList>";

				}
				catch (Exception e) {
					log.error(
					    "Exception calling getEndpointResourceUIInfo for resource group "
					        + resGroup, e);
					if(e instanceof InvalidLoginException){
						response.setStatus(401);
					}					
				}
				finally {
					sendResponse(response, message);
				}
			}
		}

	}

	private void processGetUserGroupForUser(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		String userId = request.getParameter("uid");

		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String message = "";
		try {
			message = "<error>"
			    + MessageFormat.format(bundle.getString("drac.error.ajax.noUser"),
			        userId) + "</error>";
		}
		catch (MissingResourceException e) {
			message = "<error>Could not find user details for " + userId + "</error>";
		}

		if (userId != null && !userId.equals("")) {
			LoginToken token = (LoginToken) request.getSession().getAttribute(
			    DracConstants.TOKEN_OBJ);
			if (token != null) {
				try {
					List<UserGroupName> userGroups = null;
					synchronized (requestHandler) {
						userGroups = this.requestHandler.getUserGroupProfileNames(token);
					}
					StringBuilder buf = new StringBuilder(30);
					for (UserGroupName userGroup : userGroups) {
						buf.append("<group>" + userGroup + "</group>\n");
					}
					message = "<groupList>\n" + buf.toString() + "</groupList>";
				}
				catch (Exception e) {
					log.error("Error looking up user group names for " + token, e);
					message = "<error>" + e.getMessage() + "</error>";
					if(e instanceof InvalidLoginException){
						response.setStatus(401);
					}					
				}

			}
		}
		sendResponse(response, message);
	}

	private void processGetWavelengthsForResGrp(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		String resGroup = request.getParameter("gid");

		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String message = "";
		try {
			message = "<error>"
			    + MessageFormat.format(
			        bundle.getString("drac.error.ajax.noResourceGroup"), resGroup)
			    + "</error>";
		}
		catch (MissingResourceException e) {
			message = "<error>Could not find resource group " + resGroup + "</error>";
		}

		if (resGroup != null && !resGroup.equals("")) {
			LoginToken token = (LoginToken) request.getSession().getAttribute(
			    DracConstants.TOKEN_OBJ);
			if (token != null) {
				try {
					Map<String, String> filter = new HashMap<String, String>();
					filter.put(DbKeys.NetworkElementFacilityCols.LAYER,
					    Layer.LAYER0.toString());

					List<EndpointResourceUiType> list = requestHandler.getUserEndpointsUIInfo(token,
					    resGroup, filter);
					Set<String> wavelengths = new TreeSet<String>();
					for (EndpointResourceUiType epInfo : list) {
						wavelengths.add(epInfo.getWavelength());
					}

					StringBuilder buf = new StringBuilder();
					for (String wavelength : wavelengths) {
						buf.append("<wavelength>" + wavelength + "</wavelength>\n");
					}
					message = "<wavelengthList>\n" + buf.toString() + "</wavelengthList>";

				}
				catch (RequestHandlerException re) {
					log.error("Error looking up resource group " + resGroup, re);
				}
				catch (Exception e) {
					log.error("Unexpected exception caught", e);
					if(e instanceof InvalidLoginException){
						response.setStatus(401);
					}					
				}
				finally {
					sendResponse(response, message);
				}
			}
		}
	}

	private void processPollStatus(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		try {
			LoginToken token = (LoginToken) request.getSession().getAttribute(
			    DracConstants.TOKEN_OBJ);
			String scheduleId = request.getParameter("sid");
			if (scheduleId != null && !scheduleId.equals("")) {
				TaskType t = null;
				synchronized (requestHandler) {
					t = this.requestHandler.getProgress(token, scheduleId);
				}
				if (t != null) {
					int percentage = t.getPercentage();
					int total = t.getTotalNumberOfActivity();
					int done = t.getNumberOfCompletedActivity();
					State state = t.getState();
					log.debug("Activity name: " + t.getActivityName());
					log.debug("Owner: " + t.getTaskOwner());
					log.debug("ID: " + t.getTaskId());
					log.debug("Percent: " + t.getPercentage());
					log.debug("Done: " + t.getNumberOfCompletedActivity());
					log.debug("Total: " + t.getTotalNumberOfActivity());
					log.debug("Exception: " + t.getExceptionMessage());
					log.debug("State: " + t.getState());
					String exceptionMessage = t.getExceptionMessage();
					if (exceptionMessage != null && !exceptionMessage.equals("")) {
						// an error occurred
						// String msg = DracErrorConstants.formatErrorCode(locale,
						// exceptionMessage, null);
						String message = "<state>" + state + "</state>\n" + "<error>"
						    + exceptionMessage + "</error>";
						sendResponse(response, message);
					}
					else {
						log.debug("Create schedule " + scheduleId + " at " + percentage
						    + "%");
						if (percentage > 100) {
							percentage = 100;
						}
						String message = "<percent>" + percentage + "</percent>\n"
						    + "<total>" + total + "</total>\n" + "<completed>" + done
						    + "</completed>\n" + "<state>" + state + "</state>";
						sendResponse(response, message);
					}
				}
				else {
					// null means completed
					String message = "<state>" + TaskType.State.DONE + "</state>\n"
					    + "<percent>100</percent>\n";
					sendResponse(response, message);
				}
			}
			else {
				// no-op
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
		}
		catch (Exception e) {
			log.error("DracServlet error:", e);
			// an error occurred
			String message = "<state>" + TaskType.State.DONE + "</state>\n"
			    + "<error>" + e.getMessage() + "</error>";
			if(e instanceof InvalidLoginException){
				response.setStatus(401);
			}			
			sendResponse(response, message);
		}
	}

	private void processQuerySched(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		String message = "";
		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		if(locale == null){
		  locale = Locale.getDefault();
		}
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String recurMsg = "";

		try {
			String pRate = request.getParameter("rate");
			String pSrcTna = request.getParameter("src");
			String pDestTna = request.getParameter("dest");
			String pSrcCh = request.getParameter("srcCh");
			String pDestCh = request.getParameter("destCh");
			String pSrcVlanId = request.getParameter("srcVlanId");
			String pDestVlanId = request.getParameter("dstVlanId");
			String pStartDate = request.getParameter("t0");
			String pStartTime = request.getParameter("t1");
			String pEndDate = request.getParameter("t2");
			String pEndTime = request.getParameter("t3");
			String billingGroup = request.getParameter("bg");
			String sourceUserGroup = request.getParameter("sug");
			String sourceResGroup = request.getParameter("srg");
			String destUserGroup = request.getParameter("dug");
			String destResGroup = request.getParameter("drg");
			// String algorithm = request.getParameter("alg");
			String protectionType = request.getParameter("prot");
			String metric = request.getParameter("met");
			String metricValue = request.getParameter("mVal");
			String srlg = request.getParameter("srlg");
			String srsg = request.getParameter("srsg");
			boolean isRecur = request.getParameter("recur") != null;
			boolean vcatRoutingOption = request.getParameter("vcatRoutingOption") != null;
			log.debug(request.getQueryString());
			boolean badInput = false;

			if (pRate == null || pRate.equals("") || pSrcTna == null
			    || pSrcTna.equals("") || pDestTna == null || pDestTna.equals("")
			    || pStartDate == null || pStartDate.equals("") || pStartTime == null
			    || pStartTime.equals("") || pEndDate == null || pEndDate.equals("")
			    || pEndTime == null || pEndTime.equals("")) {
				String result = bundle.getString(DracConstants.ERROR_QUERYPATH_NOPARMS);
				message = "<result>" + result + "</result>\n";
			}
			else if (pSrcTna.equals(pDestTna)) {
				String result = bundle.getString(DracConstants.ERROR_QUERYPATH_SAMETNA);
				message = "<result>" + result + "</result>\n";
			}
			else {
				if (isRecur) {
					recurMsg = " " + bundle.getString(DracConstants.QUERYPATH_MSG_RECUR);
				}

				LoginToken token = (LoginToken) request.getSession().getAttribute(
				    DracConstants.TOKEN_OBJ);
				TimeZone tz = DracHelper.getTimeZone(token);

				boolean ok = false;
				try {
					synchronized (requestHandler) {
						this.requestHandler.sessionValidate(token);
						ok = true;
					}
				}
				catch (Exception e) {

					String result = bundle
					    .getString(DracConstants.ERROR_SESSION_NOT_VALID);
					message = "<result>" + result + "</result>\n";
				}

				if (ok) {
					DracService service = new DracService();
					UserType userType = new UserType(token.getUser(), new UserGroupName(
					    billingGroup), sourceUserGroup, destUserGroup, sourceResGroup,
					    destResGroup, null);

					service.setUserInfo(userType);
					PathType path = new PathType();
					EndPointType sourceEndpoint = this.requestHandler.findEndpointByTna(token,
					    pSrcTna);
					if (sourceEndpoint != null) {
						path.setSourceEndPoint(sourceEndpoint);
						path.setSource(sourceEndpoint.getNode());
					}
					else {
						log.warn("Source endpoint " + pSrcTna
						    + " not found, using string values");
						sourceEndpoint = new EndPointType();
						sourceEndpoint.setName(pSrcTna);
						path.setSourceEndPoint(sourceEndpoint);
					}
					int channel = -1;
					try {
						channel = Integer.parseInt(pSrcCh);
					}
					catch (NumberFormatException nfe) {
						log.warn("source channel not a number");
					}
					path.getSourceEndPoint().setChannelNumber(channel);

					if (pSrcVlanId != null && pSrcVlanId.length() > 0) {
						path.setSrcVlanId(pSrcVlanId);
					}

					EndPointType targetEndpoint = this.requestHandler.findEndpointByTna(token,
					    pDestTna);
					if (targetEndpoint != null) {
						path.setTargetEndPoint(targetEndpoint);
						path.setTarget(targetEndpoint.getNode());
					}
					else {
						log.warn("Target endpoint " + pDestTna
						    + " not found, using string values");
						targetEndpoint = new EndPointType();
						targetEndpoint.setName(pDestTna);
						path.setTargetEndPoint(targetEndpoint);
					}

					channel = -1;
					try {
						channel = Integer.parseInt(pDestCh);
					}
					catch (NumberFormatException nfe) {
						log.warn("source channel not a number");
					}
					path.getTargetEndPoint().setChannelNumber(channel);

					if (pDestVlanId != null && pDestVlanId.length() > 0) {
						path.setDstVlanId(pDestVlanId);
					}

					try {
						int rate = Integer.parseInt(pRate);
						if (rate > 0) {
							path.setRate(rate);
						}
						else {
							throw new NumberFormatException();
						}
					}
					catch (NumberFormatException e) {
						String result = bundle
						    .getString(DracConstants.ERROR_QUERYTIME_BADRATE);
						message = "<result>" + result + "</result>\n";
						badInput = true;
					}

					service.setPath(path);
					service.setStartTime(DracHelper.parseWebDateToMillis(locale, tz,
					    pStartDate, pStartTime));
					service.setEndTime(DracHelper.parseWebDateToMillis(locale, tz,
					    pEndDate, pEndTime));

					if (protectionType != null && !protectionType.equals("")) {
						if (protectionType.equals(PathType.PROTECTION_TYPE.PATH1PLUS1
						    .name())) {
							service.getPath().setProtectionType(
							    PathType.PROTECTION_TYPE.PATH1PLUS1);
						}
					}

					if (metricValue != null && !metricValue.equals("")) {
						try {
							int value = Integer.parseInt(metricValue);
							if (value >= 0) {
								if ("cost".equals(metric)) {
									service.getPath().setCost(value);
								}
								else if ("hop".equals(metric)) {
									service.getPath().setHop(value);
								}
								else if ("metric2".equals(metric)) {
									service.getPath().setMetric(value);
								}
							}
							else {
								throw new NumberFormatException();
							}
						}
						catch (NumberFormatException e) {
							String result = bundle
							    .getString(DracConstants.ERROR_QUERYPATH_BADMETRIC);
							message = "<result>" + result + "</result>\n";
							badInput = true;
						}
					}
					if (srlg != null && !srlg.equals("")) {
						service.getPath().setSrlg(srlg.trim());
					}
					if (srsg != null && !srsg.equals("")) {
						service.getPath().setSharedRiskServiceGroup(srsg.trim());
					}

					service.getPath().setVcatRoutingOption(vcatRoutingOption);

					if (!badInput) {
						String result = "";
						try {
							synchronized (requestHandler) {
								if (this.requestHandler.queryPath(token, service)) {
									result = bundle
									    .getString("drac.schedule.queryPath.pathFound");
								}
							}
						}
						catch (MissingResourceException e) {
							// no key defined
							log.warn("No resource key defined for " + result);
							result = "Requested service is available.";
						}
						catch (RequestHandlerException e) {
							Throwable nested1 = e.getCause();
							Throwable nested2 = nested1 != null ? nested1.getCause() : null;

							if (nested1 instanceof DracRemoteException) {
								DracRemoteException ex = (DracRemoteException) nested1;
								result = DracErrorConstants.formatErrorCode(locale, ex);
							}
							else if (nested2 instanceof DracRemoteException) {
								DracRemoteException ex = (DracRemoteException) nested2;
								result = DracErrorConstants.formatErrorCode(locale, ex);
							}
							else {
								throw e;
							}
						}

						message = "<result>" + result + recurMsg + "</result>\n";
					} // else if bad input
				} // else if session is not valid
			} // else if missing required parameters
		}
		catch (RequestHandlerException e) {
			message = "<result>"
			    + bundle.getString(DracConstants.ERROR_QUERYPATH_INTERNAL)
			    + "</result>\n";
		}
		catch (Exception e) {
			if(e instanceof InvalidLoginException){
				response.setStatus(401);
			}
			// Unwrap the nested exceptions to obtain the user-friendly response.
			// This is currently the code path for policy violations. RequestHandler
			// used to throw RequestHandlerException on queryPath, which resulted
			// (above) in a generic internal error message.
			Throwable t = e;
			while (t.getCause() != null) {
				t = t.getCause();
			}

			message = "<result>" + t.getLocalizedMessage() + "</result>\n";
		}
		finally {
			sendResponse(response, message);
		}

	}

	private void processQuerySchedForTime(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		String message = "";
		Locale locale = (Locale) request.getSession().getAttribute(
		    DracConstants.MYLOCALE);
		ResourceBundle bundle = ResourceBundle.getBundle("DRAC", locale);
		String recurMsg = "";

		try {
			String pRate = request.getParameter("rate");
			String pSrcTna = request.getParameter("src");
			String pDestTna = request.getParameter("dest");
			String pSrcCh = request.getParameter("srcCh");
			String pDestCh = request.getParameter("destCh");
			String pDur = request.getParameter("dur");
			String billingGroup = request.getParameter("bg");
			String sourceUserGroup = request.getParameter("sug");
			String sourceResGroup = request.getParameter("srg");
			String destUserGroup = request.getParameter("dug");
			String destResGroup = request.getParameter("drg");
			// String algorithm = request.getParameter("alg");
			String protectionType = request.getParameter("prot");
			String metric = request.getParameter("met");
			String metricValue = request.getParameter("mVal");
			String srlg = request.getParameter("srlg");
			String srsg = request.getParameter("srsg");
			String concattype = request.getParameter("concattype");
			boolean isRecur = request.getParameter("recur") != null;
			log.debug(request.getQueryString());
			boolean badInput = false;
			boolean isVCAT = "CCAT".equals(concattype) ? false : true;			
			String pSrcVlanId = request.getParameter("srcVlanId");
			String pDestVlanId = request.getParameter("dstVlanId");
			if (pRate == null || pRate.equals("") || pSrcTna == null
			    || pSrcTna.equals("") || pDestTna == null || pDestTna.equals("")
			    || pDur == null || pDur.equals("")) {
				String result = bundle.getString(DracConstants.ERROR_QUERYTIME_NOPARMS);
				message = "<result>" + result + "</result>\n";
			}
			else if (pSrcTna.equals(pDestTna)) {
				String result = bundle.getString(DracConstants.ERROR_QUERYPATH_SAMETNA);
				message = "<result>" + result + "</result>\n";
			}
			else {
				int duration = 1;
				String result = "";
				String startTimeResult = "";
				String endTimeResult = "";

				if (isRecur) {
					recurMsg = " " + bundle.getString(DracConstants.QUERYPATH_MSG_RECUR);
				}

				try {
					duration = Integer.parseInt(pDur);
					if (duration <= 0) {
						throw new NumberFormatException();
					}
				}
				catch (NumberFormatException e) {
					result = bundle.getString(DracConstants.ERROR_QUERYTIME_BADDUR);
					message = "<result>" + result + "</result>\n";
					badInput = true;
				}

				int rate = -1;
				try {
					rate = Integer.parseInt(pRate);
					if (rate <= 0) {
						throw new NumberFormatException();
					}
				}
				catch (NumberFormatException e) {
					result = bundle.getString(DracConstants.ERROR_QUERYTIME_BADRATE);
					message = "<result>" + result + "</result>\n";
					badInput = true;
				}

				if (!badInput) {
					LoginToken token = (LoginToken) request.getSession().getAttribute(
					    DracConstants.TOKEN_OBJ);
					TimeZone tz = DracHelper.getTimeZone(token);
					// UserDetails udt = null;
					boolean ok = false;

					try {

						synchronized (requestHandler) {
							this.requestHandler.sessionValidate(token);
							ok = true;
						}
					}
					catch (Exception e) {

						result = bundle.getString(DracConstants.ERROR_SESSION_NOT_VALID);
						message = "<result>" + result + "</result>\n";
					}
					if (ok) {

						// query for a time
						List<DracService> times = null;
						synchronized (requestHandler) {
							times = this.requestHandler.getAvailableTimes(token, pSrcTna, pDestTna,
							    duration, Integer.parseInt(pRate));
						}
						if (!times.isEmpty()) {
							// loop through each timeslot and query for a path
							boolean foundPath = false;
							boolean exceptionHandled = false;
							for (DracService timeSlot : times) {

								DracService service = new DracService();

								UserType userType = new UserType(token.getUser(),
								    new UserGroupName(billingGroup), sourceUserGroup,
								    destUserGroup, sourceResGroup, destResGroup, null);
								service.setUserInfo(userType);

								PathType path = new PathType();
								EndPointType sourceEndpoint = this.requestHandler.findEndpointByTna(token,
								    pSrcTna);
								if (sourceEndpoint != null) {
									path.setSourceEndPoint(sourceEndpoint);
									path.setSource(sourceEndpoint.getNode());
								}
								else {
									log.warn("Source endpoint " + pSrcTna
									    + " not found, using string values");
									sourceEndpoint = new EndPointType();
									sourceEndpoint.setName(pSrcTna);
									path.setSourceEndPoint(sourceEndpoint);
								}
								int channel = -1;
								try {
									channel = Integer.parseInt(pSrcCh);
								}
								catch (NumberFormatException nfe) {
									log.warn("source channel not a number");
								}
								path.getSourceEndPoint().setChannelNumber(channel);

								EndPointType targetEndpoint = this.requestHandler.findEndpointByTna(token,
								    pDestTna);
								if (targetEndpoint != null) {
									path.setTargetEndPoint(targetEndpoint);
									path.setTarget(targetEndpoint.getNode());
								}
								else {
									log.warn("Target endpoint " + pDestTna
									    + " not found, using string values");
									targetEndpoint = new EndPointType();
									targetEndpoint.setName(pDestTna);
									path.setTargetEndPoint(targetEndpoint);
								}

								channel = -1;
								try {
									channel = Integer.parseInt(pDestCh);
								}
								catch (NumberFormatException nfe) {
									log.warn("source channel not a number");
								}
								path.getTargetEndPoint().setChannelNumber(channel);
								path.setRate(Integer.parseInt(pRate));
								path.setVcatRoutingOption(isVCAT);
								
								path.setSrcVlanId(pSrcVlanId);
								path.setDstVlanId(pDestVlanId);
								
								service.setPath(path);
								service.setStartTime(timeSlot.getStartTime());
								service.setEndTime(timeSlot.getStartTime() + duration * 60
								    * 1000);

								if (protectionType != null && !protectionType.equals("")) {
									if (protectionType.equals(PathType.PROTECTION_TYPE.PATH1PLUS1
									    .name())) {
										service.getPath().setProtectionType(
										    PathType.PROTECTION_TYPE.PATH1PLUS1);
									}
								}

								if (metricValue != null && !metricValue.equals("")) {
									try {
										int value = Integer.parseInt(metricValue);
										if ("cost".equals(metric)) {
											service.getPath().setCost(value);
										}
										else if ("hop".equals(metric)) {
											service.getPath().setHop(value);
										}
										else if ("metric2".equals(metric)) {
											service.getPath().setMetric(value);
										}
									}
									catch (NumberFormatException e) {
										result = bundle
										    .getString(DracConstants.ERROR_QUERYPATH_BADMETRIC);
										message = "<result>" + result + "</result>\n";
										badInput = true;
									}
								}
								if (srlg != null && !srlg.equals("")) {
									service.getPath().setSrlg(srlg.trim());
								}
								if (srsg != null && !srsg.equals("")) {
									service.getPath().setSharedRiskServiceGroup(srsg.trim());
								}

								if (!badInput) {

									try {
										synchronized (requestHandler) {
											if (this.requestHandler.queryPath(token, service)) {
												Date startDate = new Date(service.getStartTime());
												Date endDate = new Date(service.getEndTime());
												SimpleDateFormat dateFormatter = new SimpleDateFormat(
												    DracConstants.WEB_GUI_TIME2, locale);
												dateFormatter.setTimeZone(tz);
												result = dateFormatter.format(startDate) + " "
												    + dateFormatter.format(endDate);
												startTimeResult = String
												    .valueOf(service.getStartTime());
												endTimeResult = String.valueOf(service.getEndTime());
												foundPath = true;
												break;
											}
											continue;
										}
									}
									catch (Exception e1) {
										
										if (e1.getCause() instanceof DracRemoteException) {
											DracRemoteException ex = (DracRemoteException) e1
											    .getCause();
											result = DracErrorConstants.formatErrorCode(locale, ex);
											exceptionHandled = true;
										}else if(e1.getCause() instanceof RoutingException){
											RoutingException ex = (RoutingException) e1
										    .getCause();
											result = DracErrorConstants.formatErrorCode(locale, ex);	
											exceptionHandled = true;
										}else {
											throw e1;
										}

									}

								} // else if bad input
							} // end for availableTimes
							if (!foundPath && !exceptionHandled) {
								message = "<result>No path available</result>\n";
							}
							else {
								if(exceptionHandled){
									message = "<result>" + result + "</result>\n";
								}else{
								message = "<result>" + result + recurMsg + "</result>\n"
								    + "<startTime>" + startTimeResult + "</startTime>\n"
								    + "<endTime>" + endTimeResult + "</endTime>\n";
								}
							}
						}
						else { // if available times empty
							message = "<result>No path available</result>\n";
						}
					} // else bad input
				} // else if session is not valid
			} // else if missing required parameters
		}
		catch (RequestHandlerException e) {
			message = "<result>"
			    + bundle.getString(DracConstants.ERROR_QUERYPATH_INTERNAL)
			    + "</result>\n";
		}
		catch (Exception e) {
			message = "<result>" + e.getLocalizedMessage() + "</result>\n";
			if(e instanceof InvalidLoginException){
				response.setStatus(401);
			}			
		}
		finally {
			sendResponse(response, message);
		}

	}

	private void processQueryUtil(HttpServletRequest request,
	    HttpServletResponse response) throws IOException {
		String message = "";
		try {
			LoginToken token = (LoginToken) request.getSession().getAttribute(
			    DracConstants.TOKEN_OBJ);
			String tna = request.getParameter("tna");
			if (tna != null && !tna.equals("")) {
				try {
					double util = 0;
					synchronized (requestHandler) {
						util = this.requestHandler.getUtilization(token, tna);
					}
					util = Math.round(util * 10) / 10;
					message = "<util id=\"" + tna + "\">" + util + "</util>";
				}
				catch (RequestHandlerException e) {
					log.error("Nrb exception while looking up utilization on " + tna, e);
					// could not query the utilization, set it to -1
					message = "<util id=\"" + tna + "\">-1</util>";
				}
				sendResponse(response, message);
			}
			else {
				// no-op
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
		}
		catch (Exception e) {
			log.error("DracServlet error:", e);
			if(e instanceof InvalidLoginException){
				response.setStatus(401);
			}			
			// an error occurred
			message = "<error>" + e.getMessage() + "</error>";
			sendResponse(response, message);
		}
	}
	
	private void processExtendServiceTime(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Locale locale = (Locale) request.getSession().getAttribute(DracConstants.MYLOCALE);
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy HH:mm", locale);
		SimpleDateFormat gmtFormat = new SimpleDateFormat("Z", locale);
		String message = "";
		LoginToken token = (LoginToken) request.getSession().getAttribute(DracConstants.TOKEN_OBJ);
		DracService service = null;
		try {
			if (token != null) {
				String serviceId = request.getParameter("serviceId");
				service = requestHandler.getService(token, serviceId);
				int nrMinutesExtension = Integer.valueOf(request.getParameter("nrMinutesExtension")).intValue();

				service = requestHandler.getService(token, serviceId);
				int nrMinutesExtended = requestHandler.extendServiceTime(token, service, nrMinutesExtension);
				service = requestHandler.getService(token, serviceId);
				Date newEndTime = new Date(service.getEndTime());
				StringBuilder messageBuilder = new StringBuilder();
				messageBuilder.append("<extensionResponse>\n");
				messageBuilder.append("<extensionMessage>");
				messageBuilder.append("Service extended by ");
				messageBuilder.append(nrMinutesExtended);
				if (nrMinutesExtended == 1) {
					messageBuilder.append(" minute.");
				} else {
					messageBuilder.append(" minutes.");
				}
				messageBuilder.append("</extensionMessage>\n");
				messageBuilder.append("<nrMinutesExtended>");
				messageBuilder.append("" + nrMinutesExtended);
				messageBuilder.append("</nrMinutesExtended>\n");
				messageBuilder.append("<endTime>");
				messageBuilder.append(dateFormat.format(newEndTime));
				messageBuilder.append(" GMT");
				messageBuilder.append(gmtFormat.format(newEndTime));
				messageBuilder.append("</endTime>\n");
				messageBuilder.append("</extensionResponse>");
				message = createExtendServiceResponseString(nrMinutesExtended, newEndTime, dateFormat, gmtFormat, null);
			}
		} catch (Exception e) {
			if(e instanceof InvalidLoginException){
				response.setStatus(401);
			}
			e.printStackTrace();
			log.error("Unexpected exception caught", e);
			message = createExtendServiceResponseString(0, new Date(service.getEndTime()), dateFormat, gmtFormat,
			        e.getLocalizedMessage() + " " + e.getClass());
		} finally {
			sendResponse(response, message);
		}
	}

	private String createExtendServiceResponseString(int nrMinutesExtended, Date newEndTime,
	        SimpleDateFormat dateFormat, SimpleDateFormat gmtFormat, String errorMessage) {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append("<extensionResponse>\n");
		messageBuilder.append("<extensionMessage>");
		if (errorMessage != null) {
			messageBuilder.append(errorMessage);
		} else {
			messageBuilder.append("Service extended by ");
			messageBuilder.append(nrMinutesExtended);
			if (nrMinutesExtended == 1) {
				messageBuilder.append(" minute.");
			} else {
				messageBuilder.append(" minutes.");
			}
		}
		messageBuilder.append("</extensionMessage>\n");
		messageBuilder.append("<nrMinutesExtended>");
		messageBuilder.append("" + nrMinutesExtended);
		messageBuilder.append("</nrMinutesExtended>\n");
		messageBuilder.append("<endTime>");
		messageBuilder.append(dateFormat.format(newEndTime));
		messageBuilder.append(" GMT");
		messageBuilder.append(gmtFormat.format(newEndTime));
		messageBuilder.append("</endTime>\n");
		messageBuilder.append("</extensionResponse>");
		return messageBuilder.toString();
	}
	

	private void sendResponse(HttpServletResponse response, String message)
	    throws IOException {
		response.setContentType("text/xml");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter pw = response.getWriter();
		pw.write("<response>\n");
		pw.write(message);
		pw.write("\n</response>");
		pw.flush();
	}
}
