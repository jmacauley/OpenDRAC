package org.opendrac.rest.web;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.opendrac.rest.domain.ActiveSchedule;
import org.opendrac.rest.domain.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;
import com.nortel.appcore.app.drac.server.requesthandler.RemoteConnectionProxy;

@Controller("jsonController")
public class JsonController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final RemoteConnectionProxy proxy = new RemoteConnectionProxy();
	private final Map<String, LoginToken> loginTokens = new ConcurrentHashMap<String, LoginToken>();

	private NrbInterface nrbInterface;

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() throws Exception {
		nrbInterface = proxy.getNrbInterface();
		log.debug("Nrb: " + nrbInterface);
	}

	private LoginToken doAuthentication(final String username, String password)
	    throws Exception {
		password = CryptoWrapper.INSTANCE.decrypt(new CryptedString(password));

		if (loginTokens.get(username) != null) {
			try {
	      nrbInterface.logout(loginTokens.get(username));
	      loginTokens.remove(username);
      }
      catch (Exception e) {
	      log.warn("Unable to logout: "+username);
      }
		}
		final LoginToken token = nrbInterface.login(ClientLoginType.INTERNAL_LOGIN,
		    username, password.toCharArray(), InetAddress.getLocalHost()
		        .getHostAddress(), null, UUID.randomUUID().toString());
		loginTokens.put(username, token);
		return token;
	}

	@RequestMapping(value = "/username/{username}/password/{password}/endpoints", method = RequestMethod.GET)
	public @ResponseBody
	List<Endpoint> getLayer2Endpoints(@PathVariable final String username,
	    @PathVariable final String password) throws Exception {
		return getAllEndpoints(username, password, "layer2");
	}

	@RequestMapping(value = "/username/{username}/password/{password}/endpoints/layer/{layer}", method = RequestMethod.GET)
	public @ResponseBody
	List<Endpoint> getAllEndpoints(@PathVariable final String username,
	    @PathVariable final String password, @PathVariable final String layer)
	    throws Exception {
		final Map<String, String> filter = new HashMap<String, String>();
		final List<Endpoint> endpoints = new ArrayList<Endpoint>();
		filter.put(DbKeys.NetworkElementFacilityCols.LAYER, Layer.toEnum(layer)
		    .toString());
		final LoginToken token = doAuthentication(username, password);
		final List<EndPointType> userEndpoints = nrbInterface.getUserEndpoints(
		    token, filter);
		for (final EndPointType type : userEndpoints) {
			final Endpoint end = new Endpoint();
			end.setEndPointType(type);
			end.setName(type.getName());
			end.setUsage(nrbInterface.getUtilization(token, type.getName()));
			endpoints.add(end);
		}
		return endpoints;
	}

	@RequestMapping(value = "/username/{username}/password/{password}/schedules", method = RequestMethod.GET)
	public @ResponseBody
	List<ActiveSchedule> getAllSchedules(@PathVariable final String username,
	    @PathVariable final String password) throws Exception {
		final LoginToken token = doAuthentication(username, password);
		final List<Schedule> schedules = nrbInterface.getSchedules(token, null);
		final List<ActiveSchedule> activeSchedules = new ArrayList<ActiveSchedule>();
		log.debug("Schedules: " + schedules);
		for (final Schedule schedule : schedules) {
			final ActiveSchedule activeSchedule = new ActiveSchedule();
			activeSchedule.setDuration(schedule.getDuration());
			activeSchedule.setEndTime(schedule.getEndTime());
			activeSchedule.setName(schedule.getName());
			activeSchedule.setStartTime(schedule.getStartTime());
			activeSchedule.setScheduleId(schedule.getId());
			activeSchedules.add(activeSchedule);
		}
		return activeSchedules;
	}

}