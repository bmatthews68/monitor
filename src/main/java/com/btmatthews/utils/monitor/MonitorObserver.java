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

package com.btmatthews.utils.monitor;

/**
 * Implemented by objects that want to receive notifications from the monitor when a server has started or stopped.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public interface MonitorObserver {

    /**
     * This callback is called after the server has started.
     *
     * @param server The server that was started.
     * @param logger Used to log information and error messages.
     */
    void started(Server server, Logger logger);

    /**
     * This callback is called after the server has exited.
     *
     * @param server The server that was exited.
     * @param logger Used to log information and error messages.
     */
    void stopped(Server server, Logger logger);
}
