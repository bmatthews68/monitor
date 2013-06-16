package com.btmatthews.utils.monitor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.ServerFactory;
import com.btmatthews.utils.monitor.ServerFactoryLocator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Unit test the server factory locator.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @version 1.0.0
 */
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
     * Prepare to execute unit tests by initialising the mock objects.
     */
    @Before
    public void setUp() {
        initMocks(this);
    }

    /**
     * Verify that a server factory locator is initialised properly with a dummy
     * server factory.
     */
    @Test
    public void testServerFactoryLocator() {
        final ServerFactoryLocator serverFactoryLocator = ServerFactoryLocator
                .getInstance(logger);
        assertNotNull(serverFactoryLocator);

        final ServerFactory serverFactory = serverFactoryLocator
                .getFactory(DUMMY_SERVER_NAME);
        assertNotNull(serverFactory);
        assertEquals(DUMMY_SERVER_NAME, serverFactory.getServerName());
        assertNotNull(serverFactory.createServer());
    }

    /**
     * Verify that {@link ServerFactoryLocator#getInstance(com.btmatthews.utils.monitor.Logger)} returns a singleton.
     *
     * @throws Exception If the test case fails.
     */
    @Test
    public void testGetInstance() {
        final ServerFactoryLocator firstLocator = ServerFactoryLocator.getInstance(logger);
        final ServerFactoryLocator secondLocator = ServerFactoryLocator.getInstance(logger);
        assertNotNull(firstLocator);
        assertNotNull(secondLocator);
        assertSame(firstLocator, secondLocator);
    }
}
