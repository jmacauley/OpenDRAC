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

package com.nortel.appcore.app.drac.server.webserver.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See the web.xml that goes long with this class. A simple filter to adjust the
 * cache control.
 * 
 * @author pitman
 */
public final class ResponseHeaderFilter implements Filter {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private FilterConfig config;
	private final Map<String, String> headers = new HashMap<String, String>();

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
	    FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResp = (HttpServletResponse) resp;
		for (Map.Entry<String, String> e : headers.entrySet()) {
			httpResp.addHeader(e.getKey(), e.getValue());
		}
		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		config = filterConfig;
		Enumeration e = config.getInitParameterNames();
		while (e.hasMoreElements()) {
			String headerName = (String) e.nextElement();
			String headerValue = config.getInitParameter(headerName);
			headers.put(headerName, headerValue);
		}

		log.debug("ResponseHeaderFilter set up to add the following headers: "
		    + headers);
	}

}
