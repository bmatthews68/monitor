/*
 * Copyright 2011-2021 Brian Matthews
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

package com.btmatthews.utils.monitor.mojo;

import java.util.Map;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.Monitor;
import com.btmatthews.utils.monitor.MonitorObserver;
import com.btmatthews.utils.monitor.Server;
import com.btmatthews.utils.monitor.ServerFactory;
import com.btmatthews.utils.monitor.ServerFactoryLocator;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Abstract base class for mojos that implement the run goal for plug-ins that use the Monitor framework.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public abstract class AbstractRunMojo extends AbstractServerMojo implements MonitorObserver {

    /**
     * If {@code true} the server is run as a daemon.
     */
    @Parameter(property = "monitor.daemon", defaultValue = "false")
    private boolean daemon;

    /**
     * Concrete classes should override this method to return the server type name.
     *
     * @return The server type name.
     */
    public abstract String getServerType();

    /**
     * Get the configuration parameters for the server.
     *
     * @return A {@link Map} containing the configuration paramters.
     */
    public abstract Map<String, Object> getServerConfig();

    /**
     * This callback is called after the server has started.
     *
     * @param server The server that was started.
     * @param logger Used to log information and error messages.
     * @see MonitorObserver#started(com.btmatthews.utils.monitor.Server, com.btmatthews.utils.monitor.Logger)
     */
    @Override
    public void started(final Server server, final Logger logger) {
    }

    /**
     * This callback is called after the server has exited.
     *
     * @param server The server that was stopped.
     * @param logger Used to log information and error messages.
     * @see MonitorObserver#stopped(com.btmatthews.utils.monitor.Server, com.btmatthews.utils.monitor.Logger)
     */
    @Override
    public void stopped(final Server server, final Logger logger) {
    }

    /**
     * Execute the Maven goal by creating a the server, configuring it and then running it with the monitor.
     */
    @Override
    public void execute() {

        // Create the server

        final ServerFactoryLocator locator = ServerFactoryLocator.getInstance(this);
        final ServerFactory factory = locator.getFactory(getServerType());
        final Server server = factory.createServer();

        // Configure the server

        final Map<String, Object> config = getServerConfig();
        for (final Map.Entry<String, Object> entry : config.entrySet()) {
            server.configure(entry.getKey(), entry.getValue(), this);
        }

        // Run the monitor

        final Monitor monitor = createMonitor();
        if (daemon) {
            monitor.runMonitorDaemon(server, this, this);
        } else {
            monitor.runMonitor(server, this, this);
        }
    }
}

