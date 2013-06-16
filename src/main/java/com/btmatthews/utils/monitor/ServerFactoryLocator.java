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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

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
     * The constructor that scans the classpath and registers all available
     * factory objects.
     *
     * @param logger      Used to report status and error messages.
     * @param classLoader The class loader used to scan the classpath for ServerFactory configurations.
     */
    public ServerFactoryLocator(final Logger logger, final ClassLoader classLoader) {
        final ServiceLoader<ServerFactory> loader = ServiceLoader.load(ServerFactory.class, classLoader);
        for (final ServerFactory serverFactory : loader) {
            serverFactoryMapping.put(serverFactory.getServerName(), serverFactory);
        }
    }

    /**
     * Get the singleton instance of the locator using the default class loader. If the singleton has not
     * already been created it will be created and initialised as a side-effect.
     *
     * @param logger Used to report status and error messages.
     * @return The singleton instance of the locator.
     */
    public static ServerFactoryLocator getInstance(final Logger logger) {
        return getInstance(logger, ServerFactoryLocator.class.getClassLoader());
    }

    /**
     * Get the singleton instance of the locator using the specified class loader. If the singleton has not
     * already been created it will be created and initialised as a side-effect.
     *
     * @param logger      Used to report status and error messages.
     * @param classLoader The class loader used to scan the classpath for ServerFactory configurations.
     * @return The singleton instance of the locator.
     */
    public static ServerFactoryLocator getInstance(final Logger logger, final ClassLoader classLoader) {
        if (instance == null) {
            instance = new ServerFactoryLocator(logger, classLoader);
        }
        return instance;
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
