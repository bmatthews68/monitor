package com.btmatthews.utils.monitor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.ServerFactory;
import com.btmatthews.utils.monitor.ServerFactoryLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit test the server factory locator.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @version 1.0.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServerFactoryLocator.class })
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

    @Test
    public void testGetInstance() {
        final ServerFactoryLocator firstLocator = ServerFactoryLocator.getInstance(logger);
        final ServerFactoryLocator secondLocator = ServerFactoryLocator.getInstance(logger);
        assertNotNull(firstLocator);
        assertNotNull(secondLocator);
        assertSame(firstLocator, secondLocator);
    }

    @Test
    public void testLoadFactoriesThrowsIOException() throws Exception {
        PowerMockito.spy(ClassLoader.class);
        PowerMockito.when(
                ClassLoader.class,
                "getResources",
                eq("META-INF/service/com.btmatthews.utils.monitor.ServerFactory"))
                .thenThrow(new IOException());
        ServerFactoryLocator.getInstance(logger);
        verify(logger).logError(
                eq("Error loading META-INF/service/com.btmatthews.utils.monitor.ServerFactory"),
                any(IOException.class));
    }
}
