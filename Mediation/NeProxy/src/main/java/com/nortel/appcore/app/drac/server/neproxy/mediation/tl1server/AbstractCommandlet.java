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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on Aug 15, 2005
 * 
 * @author nguyentd
 */

public abstract class AbstractCommandlet {
  protected static final Logger log = LoggerFactory.getLogger(AbstractCommandlet.class);
  
	public static final String RESULT_KEY = "$resultKey";
	public static final String ADDITIONALEVENT_KEY = "$additionalEvent";

	private final Map<String, Object> parameters;
	private final Candidate candidate;

	public AbstractCommandlet(Map<String, Object> param) {
		parameters = param;
		Candidate c = (Candidate) param.get(NePoxyDefinitionsParser.CANDIDATE_KEY);
		if (c == null) {
			c = new Candidate();
		}
		candidate = c;
	}

	public static AbstractCommandlet getCommandlet(String className,
	    Map<String, Object> context) throws Exception {
		String cName = className.trim();
		log.debug("Invoking commandlet " + cName + " with \n"
		    + contextToString(context));
		Class<?> actionClass = Class.forName(cName);
		Constructor<?> constructor = actionClass
		    .getConstructor(new Class[] { Map.class });
		AbstractCommandlet command = (AbstractCommandlet) constructor
		    .newInstance(new Object[] { context });
		return command;
	}

	/*
	 * A better version of map.toString()
	 */
	private static String contextToString(Map<String, Object> ctx) {
		try {
			StringBuilder sb = new StringBuilder(100);

			for (Map.Entry<String, Object> m : ctx.entrySet()) {
				sb.append(m.getKey());
				sb.append('=');
				if (m.getValue() != null && m.getValue().getClass().isArray()) {
					sb.append(Arrays.asList((Object[]) m.getValue()));
				}
				else {
					sb.append(m.getValue());
				}
				sb.append('\n');
			}

			return sb.toString();
		}
		catch (Exception t) {
			log.error("Failed to build context string", t);
			return "Failed to build context string";
		}
	}

	/**
	 * @return the candidate
	 */
	public Candidate getCandidate() {
		return candidate;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	public String getResult() {
		return null;
	}

	public abstract boolean start() throws Exception;
}
