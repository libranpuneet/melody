/*
 * Copyright 2008-2012 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Java Melody is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Java Melody is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java Melody.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.bull.javamelody;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;

/**
 * Test unitaire de la classe Parameters.
 * @author Emeric Vernat
 */
public class TestParameters {
	private static void setProperty(Parameter parameter, String value) {
		Utils.setProperty(parameter, value);
	}

	/** Check. */
	@Before
	public void setUp() {
		Utils.initialize();
	}

	/** Test. */
	@Test
	public void testInitializeFilterConfig() {
		// test pas d'erreur
		Parameters.initialize((FilterConfig) null);
		assertNotNull("initialize", Parameters.class);
	}

	/** Test. */
	@Test
	public void testInitializeServletContext() {
		// test pas d'erreur
		Parameters.initialize((ServletContext) null);
		assertNotNull("initialize", Parameters.class);
	}

	/** Test. */
	@Test
	public void testGetHostName() {
		assertNotNull("getHostName", Parameters.getHostName());
	}

	/** Test. */
	@Test
	public void testGetHostAddress() {
		assertNotNull("getHostAddress", Parameters.getHostAddress());
	}

	/** Test. */
	@Test
	public void testGetResourcePath() {
		assertNotNull("getResourcePath", Parameters.getResourcePath("resource"));
	}

	/** Test.
	 * @throws MalformedURLException e */
	@Test
	public void testGetContextPath() throws MalformedURLException {
		final String path = "path";
		ServletContext context = createNiceMock(ServletContext.class);
		expect(context.getMajorVersion()).andReturn(3).anyTimes();
		expect(context.getMinorVersion()).andReturn(0).anyTimes();
		expect(context.getContextPath()).andReturn(path).anyTimes();
		replay(context);
		assertEquals("getContextPath", path, Parameters.getContextPath(context));
		verify(context);

		context = createNiceMock(ServletContext.class);
		expect(context.getMajorVersion()).andReturn(2).anyTimes();
		expect(context.getMinorVersion()).andReturn(5).anyTimes();
		expect(context.getContextPath()).andReturn(path).anyTimes();
		replay(context);
		assertEquals("getContextPath", path, Parameters.getContextPath(context));
		verify(context);

		context = createNiceMock(ServletContext.class);
		expect(context.getMajorVersion()).andReturn(2).anyTimes();
		expect(context.getMinorVersion()).andReturn(4).anyTimes();
		final URL url = getClass().getResource("/WEB-INF/web.xml");
		expect(context.getResource("/WEB-INF/web.xml")).andReturn(url).anyTimes();
		replay(context);
		assertNotNull("getContextPath", Parameters.getContextPath(context));
		verify(context);
	}

	/** Test. */
	@Test
	public void testGetResolutionSeconds() {
		if (Parameters.getResolutionSeconds() <= 0) {
			fail("getResolutionSeconds");
		}
		setProperty(Parameter.RESOLUTION_SECONDS, "60");
		if (Parameters.getResolutionSeconds() <= 0) {
			fail("getResolutionSeconds");
		}
		Exception ex = null;
		setProperty(Parameter.RESOLUTION_SECONDS, "-1");
		try {
			Parameters.getResolutionSeconds();
		} catch (final Exception e) {
			ex = e;
		}
		if (ex == null) {
			fail("getResolutionSeconds");
		}
		setProperty(Parameter.RESOLUTION_SECONDS, "60");
	}

	/** Test. */
	@Test
	public void testGetStorageDirectory() {
		final String message = "getStorageDirectory";
		final String application = "test";
		assertNotNull(message, Parameters.getStorageDirectory(application));
		setProperty(Parameter.STORAGE_DIRECTORY, "");
		assertNotNull(message, Parameters.getStorageDirectory(application));
		setProperty(Parameter.STORAGE_DIRECTORY, "/");
		assertNotNull(message, Parameters.getStorageDirectory(application));
		if (System.getProperty("os.name").toLowerCase(Locale.getDefault()).contains("windows")) {
			setProperty(Parameter.STORAGE_DIRECTORY, "c:/");
			assertNotNull(message, Parameters.getStorageDirectory(application));
		}
		setProperty(Parameter.STORAGE_DIRECTORY, "javamelody");
	}

	/** Test. */
	@Test
	public void testGetCurrentApplication() {
		Parameters.initialize((ServletContext) null);
		// null car pas de servletContext
		assertNull("getCurrentApplication", Parameters.getCurrentApplication());
	}

	/** Test. */
	@Test
	public void testGetParameter() {
		assertNull("getParameter", Parameters.getParameter(Parameter.DATASOURCES));
	}

	/** Test. */
	@Test
	public void testParameterValueOfIgnoreCase() {
		assertNotNull("Parameter.valueOfIgnoreCase",
				Parameter.valueOfIgnoreCase(Parameter.DISABLED.toString()));
	}

	/** Test. */
	@Test
	public void testPeriodValueOfIgnoreCase() {
		assertNotNull("Period.valueOfIgnoreCase", Period.valueOfIgnoreCase(Period.TOUT.toString()));
	}

	/** Test.
	 * @throws IOException e */
	@Test
	public void testGetCollectorUrlsByApplication() throws IOException {
		assertNotNull("getCollectorUrlsByApplications", Parameters.getCollectorUrlsByApplications());
	}

	/** Test.
	 * @throws IOException e */
	@Test
	public void testAddRemoveCollectorApplication() throws IOException {
		final String application = "testapp";
		Parameters.removeCollectorApplication(application);
		final int size = Parameters.getCollectorUrlsByApplications().size();
		try {
			Parameters.addCollectorApplication(application,
					Parameters.parseUrl("http://localhost:8090/test"));
			assertEquals("addCollectorApplication", size + 1, Parameters
					.getCollectorUrlsByApplications().size());
			Parameters.removeCollectorApplication(application);
			assertEquals("removeCollectorApplication", size, Parameters
					.getCollectorUrlsByApplications().size());
			// pour que le test ait une application à lire la prochaine fois
			Parameters.addCollectorApplication(application,
					Parameters.parseUrl("http://localhost:8090/test"));
		} finally {
			Parameters.removeCollectorApplication(application);
		}
	}

	/** Test.
	 * @throws MalformedURLException e */
	@Test
	public void testParseUrl() throws MalformedURLException {
		setProperty(Parameter.TRANSPORT_FORMAT, TransportFormat.XML.getCode());
		assertNotNull("parseUrl", Parameters.parseUrl("http://localhost,http://localhost"));
		assertNotNull("parseUrl", Parameters.parseUrl("http://localhost/"));
		setProperty(Parameter.TRANSPORT_FORMAT, TransportFormat.SERIALIZED.getCode());
		assertNotNull("parseUrl", Parameters.parseUrl("http://localhost,http://localhost"));
	}

	/** Test. */
	@Test
	public void testIsCounterHidden() {
		setProperty(Parameter.DISPLAYED_COUNTERS, null);
		assertFalse("isCounterHidden", Parameters.isCounterHidden("http"));
		setProperty(Parameter.DISPLAYED_COUNTERS, "http,sql");
		assertFalse("isCounterHidden", Parameters.isCounterHidden("http"));
		setProperty(Parameter.DISPLAYED_COUNTERS, "sql");
		assertTrue("isCounterHidden", Parameters.isCounterHidden("http"));
	}
}
