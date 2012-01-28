/*
 * Copyright 2011-2012 Brian Matthews
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.btmatthews.utils.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This locator object is a singleton that is used to obtain the factory that
 * will be used to create server objects that can be controlled by a monitor.
 * 
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.0.0
 */
public class ServerFactoryLocator {

    /**
     * The singleton instance of the locator.
     */
    private static ServerFactoryLocator INSTANCE;

    /**
     * Registered factories keyed by their server name.
     */
    private Map<String, ServerFactory> serverFactoryMapping = new HashMap<String, ServerFactory>();

    /**
     * Get the singleton instance of the locator. If the singleton has not
     * already been created it will be created and initialised as a side-effect.
     * 
     * @return The singleton instance of the locator.
     */
    public static ServerFactoryLocator getInstance(final Logger logger) {
	if (INSTANCE == null) {
	    INSTANCE = new ServerFactoryLocator(logger);
	}
	return INSTANCE;
    }

    /**
     * The constructor that scans the classpath and registers all available
     * factory objects.
     */
    private ServerFactoryLocator(final Logger logger) {
	try {
	    final ClassLoader classLoader = ServerFactoryLocator.class
		    .getClassLoader();
	    final Enumeration<URL> resources = classLoader
		    .getResources("META-INF/service/com.btmatthews.utils.monitor.ServerFactory");
	    while (resources.hasMoreElements()) {
		final URL resourceUrl = resources.nextElement();
		loadServerFactories(resourceUrl, logger);
	    }
	} catch (final IOException e) {
	    logger.logError("", e);
	}
    }

    /**
     * Load all the server factories from the service file accessed using the
     * URL {@code url}.
     * 
     * @param url
     *            The location of the service file.
     * @throws IOException
     *             If there was a problem loading the service file.
     */
    private void loadServerFactories(final URL url, final Logger logger)
	    throws IOException {
	final InputStream resourceStream = url.openStream();
	try {
	    loadServerFactories(resourceStream, logger);
	} finally {
	    resourceStream.close();
	}
    }

    @SuppressWarnings("unchecked")
    private void loadServerFactories(final InputStream inputStream,
	    final Logger logger) throws IOException {
	final BufferedReader reader = new BufferedReader(new InputStreamReader(
		inputStream));
	String serverFactoryClassName = reader.readLine();
	while (serverFactoryClassName != null) {
	    if (serverFactoryClassName.length() > 0) {
		try {
		    final Class<ServerFactory> serverFactoryClass = (Class<ServerFactory>) Class
			    .forName(serverFactoryClassName);
		    final ServerFactory serverFactory = serverFactoryClass
			    .newInstance();
		    serverFactoryMapping.put(serverFactory.getServerName(),
			    serverFactory);
		} catch (final ClassNotFoundException e) {
		    logger.logError("", e);
		} catch (final IllegalAccessException e) {
		    logger.logError("", e);
		} catch (final InstantiationException e) {
		    logger.logError("", e);
		}
	    }
	    serverFactoryClassName = reader.readLine();
	}
    }

    /**
     * Lookup the factory identified by {@code serverName}.
     * 
     * @param serverName
     *            The server name.
     * @return The factory that creates server objects identified by the server
     *         name.
     */
    public ServerFactory getFactory(final String serverName) {
	return serverFactoryMapping.get(serverName);
    }
}
