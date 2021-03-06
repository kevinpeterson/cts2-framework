package edu.mayo.cts2.framework.webapp.rest.osgi;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.http.proxy.DispatcherTracker;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.util.Assert;

public final class ProxyServlet extends HttpServlet {

	private static final long serialVersionUID = -8648910455822026016L;

	private transient ServiceTracker tracker;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		Assert.notNull(config.getServletContext());

		try {
			doInit(config);
		} catch (ServletException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void doInit(ServletConfig config) throws Exception {
		this.tracker = new DispatcherTracker(getBundleContext(), null,
				getServletConfig());
		this.tracker.open();
		
		config.getServletContext().setAttribute("osgi-servlet-tracker", this.tracker);	
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		HttpServlet dispatcher = (HttpServlet) this.tracker.getService();
		if (dispatcher != null) {
			dispatcher.service(req, res);
		} else {
			res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}

	@Override
	public void destroy() {
		this.tracker.close();
		super.destroy();
	}

	private BundleContext getBundleContext() throws ServletException {
		Object context = getServletContext().getAttribute(
				BundleContext.class.getName());
		if (context instanceof BundleContext) {
			return (BundleContext) context;
		}

		throw new ServletException("Bundle context attribute ["
				+ BundleContext.class.getName()
				+ "] not set in servlet context");
	}
}