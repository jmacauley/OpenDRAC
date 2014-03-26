package org.opendrac.webserver.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CharsetFilter implements Filter {
	private String encoding;

	@Override
	public void init(final FilterConfig config) throws ServletException {
		encoding = config.getInitParameter("encoding");
		if (encoding == null) {
			encoding = "UTF-8";
		}
	}

	@Override
	public void doFilter(final ServletRequest request,
	    final ServletResponse response, final FilterChain next)
	    throws IOException, ServletException {
		if (request.getCharacterEncoding() == null) {
			request.setCharacterEncoding(encoding);
		}
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		next.doFilter(request, response);
	}

	@Override
	public void destroy() {
		encoding = null;
	}

}