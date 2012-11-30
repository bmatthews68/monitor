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

package com.btmatthews.utils.monitor.mojo;

import java.util.Map;

import com.btmatthews.utils.monitor.Monitor;
import com.btmatthews.utils.monitor.Server;
import com.btmatthews.utils.monitor.ServerFactory;
import com.btmatthews.utils.monitor.ServerFactoryLocator;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public abstract class AbstractRunMojo extends AbstractServerMojo {

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
    protected abstract String getServerType();

    /**
     * Get the configuration parameters for the server.
     *
     * @return A {@link Map} containing the configuration paramters.
     */
    protected abstract Map<String, Object> getServerConfig();

    /**
     * This callback is called after the server has started.
     */
    protected void started() {
    }

    /**
     * This callback is called after the server has exited.
     */
    protected void stopped() {
    }

    /**
     * Execute the Maven goal by creating a the server and then running it with the monitor.
     *
     * @throws MojoFailureException If there was an error executing the goal.
     */
    @Override
    public void execute() throws MojoFailureException {
        final Monitor monitor = createMonitor();
        final ServerFactoryLocator locator = ServerFactoryLocator.getInstance(this);
        final ServerFactory factory = locator.getFactory(getServerType());
        final Server server = factory.createServer();
        final Map<String, Object> config = getServerConfig();
        for (final Map.Entry<String, Object> entry : config.entrySet()) {
            server.configure(entry.getKey(), entry.getValue(), this);
        }
        server.start(this);
        started();
        if (daemon) {
            new Thread(new Runnable() {
                public void run() {
                    monitor.runMonitor(server, AbstractRunMojo.this);
                    stopped();
                }
            }).start();
        } else {
            monitor.runMonitor(server, this);
            stopped();
        }
    }
}
