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
import java.text.MessageFormat;
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
public final class ServerFactoryLocator {

    /**
     * The singleton instance of the locator.
     */
    private static ServerFactoryLocator instance;

    /**
     * Registered factories keyed by their server name.
     */
    private final Map<String, ServerFactory> serverFactoryMapping = new HashMap<String, ServerFactory>();

    /**
     * Get the singleton instance of the locator. If the singleton has not
     * already been created it will be created and initialised as a side-effect.
     *
     * @param logger Used to report status and error messages.
     * @return The singleton instance of the locator.
     */
    public static ServerFactoryLocator getInstance(final Logger logger) {
        if (instance == null) {
            instance = new ServerFactoryLocator();
            instance.init(logger);
        }
        return instance;
    }

    /**
     * The constructor that scans the classpath and registers all available
     * factory objects.
     *
     * @param logger Used to report status and error messages.
     */
    private void init(final Logger logger) {
        try {
            final ClassLoader classLoader = ServerFactoryLocator.class.getClassLoader();
            final Enumeration<URL> resources = classLoader
                    .getResources("META-INF/service/com.btmatthews.utils.monitor.ServerFactory");
            while (resources.hasMoreElements()) {
                final URL resourceUrl = resources.nextElement();
                loadServerFactories(resourceUrl, logger);
            }
        } catch (final IOException e) {
            logger.logError("Error loading META-INF/service/com.btmatthews.utils.monitor.ServerFactory", e);
        }
    }

    /**
     * Load all the server factories from the service file accessed using the
     * URL {@code url}.
     *
     * @param url    The location of the service file.
     * @param logger Used to report status and error messages.
     * @throws IOException If there was a problem loading the service file.
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

    /**
     * Load all the server factories from the service file accessed via the
     * input stream {@code inputStream}.
     *
     * @param inputStream The input stream.
     * @param logger      Used to report status and error messages.
     * @throws IOException If there was a problem loading the input stream.
     */
    @SuppressWarnings("unchecked")
    private void loadServerFactories(final InputStream inputStream,
                                     final Logger logger) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        String serverFactoryClassName = reader.readLine();
        while (serverFactoryClassName != null) {
            if (serverFactoryClassName.length() > 0) {
                try {
                    final Class<ServerFactory> serverFactoryClass = (Class<ServerFactory>)Class.forName(serverFactoryClassName);
                    final ServerFactory serverFactory = serverFactoryClass.newInstance();
                    serverFactoryMapping.put(serverFactory.getServerName(), serverFactory);
                } catch (final ClassNotFoundException e) {
                    final String message = MessageFormat.format("Class {0} not found", serverFactoryClassName);
                    logger.logError(message, e);
                } catch (final IllegalAccessException e) {
                    final String message = MessageFormat.format(
                            "Cannot access class {0} or its constructor",
                            serverFactoryClassName);
                    logger.logError(message, e);
                } catch (final InstantiationException e) {
                    final String message = MessageFormat.format(
                            "Class {0} cannot be instantiated",
                            serverFactoryClassName);
                    logger.logError(message, e);
                }
            }
            serverFactoryClassName = reader.readLine();
        }
    }

    /**
     * Lookup the factory identified by {@code serverName}.
     *
     * @param serverName The server name.
     * @return The factory that creates server objects identified by the server
     *         name.
     */
    public ServerFactory getFactory(final String serverName) {
        return serverFactoryMapping.get(serverName);
    }
}
