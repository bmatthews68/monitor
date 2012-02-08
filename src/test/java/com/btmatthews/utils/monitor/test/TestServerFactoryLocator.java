package com.btmatthews.utils.monitor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.ServerFactory;
import com.btmatthews.utils.monitor.ServerFactoryLocator;

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
    private static final String DUMMY_SERVER_NAME = "Dummy";

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
}
