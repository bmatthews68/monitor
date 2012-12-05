package com.btmatthews.utils.monitor.test.mojo;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.Monitor;
import com.btmatthews.utils.monitor.Server;
import com.btmatthews.utils.monitor.mojo.AbstractRunMojo;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Unit test {@link AbstractRunMojo}.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public class TestRunMojo {

    /**
     * Used for testing the logging methods.
     */
    private static final String MESSAGE = "Lorem ipsum dolor sit amet";

    /**
     * The abstract base class being tested.
     */
    @Mock
    private AbstractRunMojo mojo;

    /**
     * The mock logger fixture.
     */
    @Mock
    private Log log;

    /**
     * A mock exception.
     */
    @Mock
    private Exception exception;

    /**
     * Initialise the mock objects and test fixtures.
     *
     * @throws Exception If there was an error.
     */
    @Before
    public void setUp() throws Exception {
        initMocks(this);
        final Map<String, Object> config = new HashMap<String, Object>();
        config.put("debug", Boolean.FALSE);
        when(mojo.getLog()).thenReturn(log);
        when(mojo.getServerType()).thenReturn("dummy");
        when(mojo.getServerConfig()).thenReturn(config);
        doCallRealMethod().when(mojo).createMonitor();
        doCallRealMethod().when(mojo).logInfo(anyString());
        doCallRealMethod().when(mojo).logInfo(anyString());
        doCallRealMethod().when(mojo).logError(anyString());
        doCallRealMethod().when(mojo).logError(anyString(), any(Exception.class));
        doCallRealMethod().when(mojo).started(any(Server.class), any(Logger.class));
        doCallRealMethod().when(mojo).stopped(any(Server.class), any(Logger.class));
        doCallRealMethod().when(mojo).execute();
        ReflectionUtils.setVariableValueInObject(mojo, "monitorPort", 10000);
        ReflectionUtils.setVariableValueInObject(mojo, "monitorKey", "dummy");
    }

    /**
     * Verify that {@link Logger#logInfo(String)} has been implemented.
     */
    @Test
    public void testLogInfo() {
        mojo.logInfo(MESSAGE);
        verify(log).info(eq(MESSAGE));
    }

    /**
     * Verify that {@link Logger#logError(String)} has been implemented.
     */
    @Test
    public void testLogError() {
        mojo.logError(MESSAGE);
        verify(log).error(eq(MESSAGE));
    }

    /**
     * Verify that {@link Logger#logError(String, Throwable)} has been implemented.
     */
    @Test
    public void testLogErrorAndException() {
        mojo.logError(MESSAGE, exception);
        verify(log).error(eq(MESSAGE), same(exception));
    }

    /**
     * Verify that we can start the server as foreground process.
     *
     * @throws Exception If there was an error.
     */
    @Test
    public void testRun() throws Exception {
        ReflectionUtils.setVariableValueInObject(mojo, "daemon", Boolean.FALSE);
        final Thread mojoThread = new Thread(new Runnable() {
            public void run() {
                try {
                    mojo.execute();
                } catch (final Exception e) {
                }
            }
        });
        mojoThread.start();

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                signalStop();
            }
        }, 5000L);

        mojoThread.join(15000L);
    }

    /**
     * Verify that we can start the server as a daemon.
     *
     * @throws Exception If there was an error.
     */
    @Test
    public void testRunDaemon() throws Exception {
        ReflectionUtils.setVariableValueInObject(mojo, "daemon", Boolean.TRUE);
        mojo.execute();
        Thread.sleep(5000L);
        signalStop();
    }

    /**
     * Send a stop signal to monitor controlling the server.
     */
    private void signalStop() {
        Monitor.sendCommand("dummy", 10000, "stop", mock(Logger.class));
    }
}
