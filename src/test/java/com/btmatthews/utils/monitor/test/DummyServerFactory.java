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

package com.btmatthews.utils.monitor.test;

import com.btmatthews.utils.monitor.Server;
import com.btmatthews.utils.monitor.ServerFactory;

/**
 * Implements the factory that creates the dummy server.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @version 1.0.0
 */
public class DummyServerFactory implements ServerFactory {

    /**
     * Returns the name of the dummy server.
     *
     * @return Always returns {@code "dummy"}.
     * @see ServerFactory#getServerName()
     */
    @Override
    public String getServerName() {
        return "dummy";
    }

    /**
     * Creates an instance of the dummy server.
     *
     * @return An instance of {@link DummyServer}.
     * @see ServerFactory#createServer()
     */
    @Override
    public Server createServer() {
        return new DummyServer();
    }
}
