package com.btmatthews.utils.monitor.test.mojo;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.Monitor;
import com.btmatthews.utils.monitor.mojo.AbstractRunMojo;
import com.btmatthews.utils.monitor.test.AbstractMonitorTest;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test {@link AbstractRunMojo}.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
@ExtendWith(MockitoExtension.class)
class TestRunMojo extends AbstractMonitorTest {

    /**
     * Used for testing the logging methods.
     */
    private static final String MESSAGE = "Lorem ipsum dolor sit amet";

    /**
     * The abstract base class being tested.
     */
    @Spy
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
    @BeforeEach
    void setUp() throws Exception {
        ReflectionUtils.setVariableValueInObject(mojo, "monitorPort", 10000);
        ReflectionUtils.setVariableValueInObject(mojo, "monitorKey", "dummy");
    }

    /**
     * Verify that {@link Logger#logInfo(String)} has been implemented.
     */
    @Test
    void testLogInfo() {
        doCallRealMethod().when(mojo).logInfo(anyString());
        when(mojo.getLog()).thenReturn(log);
        mojo.logInfo(MESSAGE);
        verify(log).info(eq(MESSAGE));
        verify(mojo).logInfo(MESSAGE);
        verify(mojo).getLog();
        verifyNoMoreInteractions(log, mojo);
    }

    /**
     * Verify that {@link Logger#logError(String)} has been implemented.
     */
    @Test
    void testLogError() {
        doCallRealMethod().when(mojo).logError(anyString());
        when(mojo.getLog()).thenReturn(log);
        mojo.logError(MESSAGE);
        verify(log).error(MESSAGE);
        verify(mojo).logError(MESSAGE);
        verify(mojo).getLog();
        verifyNoMoreInteractions(log, mojo);
    }

    /**
     * Verify that {@link Logger#logError(String, Throwable)} has been implemented.
     */
    @Test
    void testLogErrorAndException() {
        doCallRealMethod().when(mojo).logError(anyString(), any());
        when(mojo.getLog()).thenReturn(log);
        mojo.logError(MESSAGE, exception);
        verify(log).error(eq(MESSAGE), same(exception));
        verify(mojo).logError(eq(MESSAGE), same(exception));
        verify(mojo).getLog();
        verifyNoMoreInteractions(log, mojo);
    }

    /**
     * Verify that we can start the server as foreground process.
     *
     * @throws Exception If there was an error.
     */
    @Test
    void testRun() throws Exception {
        final Map<String, Object> config = new HashMap<>();
        config.put("debug", Boolean.FALSE);
        when(mojo.getServerType()).thenReturn("dummy");
        when(mojo.getServerConfig()).thenReturn(config);
        ReflectionUtils.setVariableValueInObject(mojo, "daemon", Boolean.FALSE);
        final Thread mojoThread = new Thread(() -> {
            try {
                mojo.execute();
            } catch (final Exception e) {
            }
        });
        mojoThread.start();

        runWithDelay(this::signalStop);

        mojoThread.join(15000L);
    }

    /**
     * Verify that we can start the server as a daemon.
     *
     * @throws Exception If there was an error.
     */
    @Test
    void testRunDaemon() throws Exception {
        when(mojo.getServerType()).thenReturn("dummy");
        doCallRealMethod().when(mojo).execute();
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
