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

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.Monitor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Abstract base class for mojos that implement the stop and run goals for plug-ins that use the Monitor framework.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public abstract class AbstractServerMojo extends AbstractMojo implements Logger {

    /**
     * The port to listen on for the monitor commands.
     */
    @Parameter(property = "monitor.port", required = true)
    private int monitorPort;
    /**
     * Key to provide when sending commands to the mail apache.
     */
    @Parameter(property = "monitor.key", required = true)
    private String monitorKey;
    /**
     * The number of times to retry when checking if the server has started or stopped.
     *
     * @since 2.1.1
     */
    @Parameter(property = "monitor.retryCount", defaultValue = "3")
    private int monitorRetryCount;
    /**
     * The delay in milliseconds before retrying the check to see if the server has started or stopped.
     *
     * @since 2.1.1
     */
    @Parameter(property = "monitor.retryInterval", defaultValue = "500")
    private int monitorRetryInterval;

    /**
     * Create the monitor object tha is used to control a server.
     *
     * @return A {@link Monitor} object.
     */
    public Monitor createMonitor() {
        return new Monitor(monitorKey, monitorPort, monitorRetryCount, monitorRetryInterval);
    }

    /**
     * Write an informational message to the plug-in log file.
     *
     * @param message The message to be logged.
     */
    @Override
    public void logInfo(final String message) {
        getLog().info(message);
    }

    /**
     * Write an error message to the plug-in log file.
     *
     * @param message The message to be logged.
     */
    @Override
    public void logError(final String message) {
        getLog().error(message);
    }

    /**
     * Write an error message to the plug-in log file.
     *
     * @param message The message to be logged.
     * @param cause   The exception that caused the message to be logged.
     */
    @Override
    public void logError(final String message, final Throwable cause) {
        getLog().error(message, cause);
    }
}
