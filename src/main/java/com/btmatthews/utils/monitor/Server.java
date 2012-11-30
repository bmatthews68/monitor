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

/**
 * This interface is implemented by server objects that can be controlled by a
 * monitor.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.0.0
 */
public interface Server {

    /**
     * Configure the a server property.
     *
     * @param name   The property name.
     * @param value  The property value.
     * @param logger Used to log error messages.
     * @since 1.1.0
     */
    void configure(String name, Object value, Logger logger);

    /**
     * Start the server.
     *
     * @param logger Used to log error messages.
     */
    void start(Logger logger);

    /**
     * Pause the server.
     *
     * @param logger Used to log error messages.
     * @since 1.1.0
     */
    void pause(Logger logger);

    /**
     * Resume the server.
     *
     * @param logger Used to log error messages.
     * @since 1.1.0
     */
    void resume(Logger logger);

    /**
     * Stop the server.
     *
     * @param logger Used to log error messages.
     */
    void stop(Logger logger);
}
