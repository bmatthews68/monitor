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

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.ServerFactoryLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test the server factory locator.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
public class TestServerFactoryLocator {

    /**
     * The dummy server name.
     */
    private static final String DUMMY_SERVER_NAME = "dummy";

    /**
     * Mock object used in place of the logger.
     */
    @Mock
    private Logger logger;

    /**
     * Verify that a server factory locator is initialised properly with a dummy
     * server factory.
     */
    @Test
    void testServerFactoryLocator() {
        final ServerFactoryLocator serverFactoryLocator = ServerFactoryLocator.getInstance(logger);
        assertThat(serverFactoryLocator).isNotNull();

        assertThat(serverFactoryLocator.getFactory(DUMMY_SERVER_NAME))
                .satisfies(serverFactory -> {
                    assertThat(serverFactory.getServerName()).isEqualTo(DUMMY_SERVER_NAME);
                    assertThat(serverFactory.createServer()).isInstanceOf(DummyServer.class);
                });
    }

    /**
     * Verify that {@link ServerFactoryLocator#getInstance(com.btmatthews.utils.monitor.Logger)} returns a singleton.
     */
    @Test
    void testGetInstance() {
        final ServerFactoryLocator firstLocator = ServerFactoryLocator.getInstance(logger);
        final ServerFactoryLocator secondLocator = ServerFactoryLocator.getInstance(logger);
        assertThat(firstLocator)
                .isNotNull()
                .isSameAs(secondLocator);
    }
}
