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

package com.btmatthews.utils.monitor.test;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.Server;

/**
 * Implements a dummy server for unit test.
 * 
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @version 1.0.0
 */
public class DummyServer implements Server {

    /**
     * Invoked by the monitor to launch the server.
     * 
     * @param logger
     *            Used to log status and error messages.
     * @see com.btmatthews.utils.monitor.Server#start(com.btmatthews.utils.monitor.Logger)
     */
    public void start(final Logger logger) {
    }

    /**
     * Invoked by the monitor to halt the server.
     * 
     * @param logger
     *            Used to log status and error messages.
     * @see com.btmatthews.utils.monitor.Server#start(com.btmatthews.utils.monitor.Logger)
     */
    public void stop(final Logger logger) {
    }
}
