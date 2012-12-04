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
 * Abstract base class for objects that implement the {@link Server} interface. This abstract base class provides
 * default implementations for the methods defined by the interface.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public abstract class AbstractServer implements Server {
    /**
     * Invoked by the monitor to configure a server property.
     *
     * @param name   The property name.
     * @param value  The property value.
     * @param logger Used to log error messages.
     * @see com.btmatthews.utils.monitor.Server#configure(String, Object, com.btmatthews.utils.monitor.Logger)
     */
    public void configure(final String name, final Object value, final Logger logger) {
    }

    /**
     * Invoked by the monitor to launch the server.
     *
     * @param logger Used to log status and error messages.
     * @see com.btmatthews.utils.monitor.Server#start(com.btmatthews.utils.monitor.Logger)
     */
    public void start(final Logger logger) {
    }

    /**
     * Invoked by the monitor to halt the server.
     *
     * @param logger Used to log status and error messages.
     * @see com.btmatthews.utils.monitor.Server#start(com.btmatthews.utils.monitor.Logger)
     */
    public void stop(final Logger logger) {
    }
}
