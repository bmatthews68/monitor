package com.btmatthews.utils.monitor.test.mojo;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.Monitor;
import com.btmatthews.utils.monitor.MonitorObserver;
import com.btmatthews.utils.monitor.Server;
import com.btmatthews.utils.monitor.mojo.AbstractStopMojo;
import com.btmatthews.utils.monitor.test.AbstractMonitorTest;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the Mojo that implements the stop goal.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
@ExtendWith(MockitoExtension.class)
class TestStopMojo extends AbstractMonitorTest {

    /**
     * Spy test fixture.
     */
    @Spy
    private final AbstractStopMojo mojo = new AbstractStopMojo() {
    };
    /**
     * Spy test fixture.
     */
    @Spy
    private final AbstractStopMojo wrongMojo = new AbstractStopMojo() {
    };
    /**
     * Mock for the LDAP server.
     */
    @Mock
    private Server server;
    /**
     * Mock for the logger.
     */
    @Mock
    private Logger logger;
    /**
     * Mock observer fixture.
     */
    @Mock
    private MonitorObserver observer;
    /**
     * Mock logger for used by Maven.
     */
    @Mock
    private Log log;

    /**
     * Prepare for test case execution by initialising the mocks.
     *
     * @throws Exception If there was a problem preparing the test cases.
     */
    @BeforeEach
    void setUp() throws Exception {
        ReflectionUtils.setVariableValueInObject(mojo, "monitorPort", 10000);
        ReflectionUtils.setVariableValueInObject(mojo, "monitorKey", "dummy");
        ReflectionUtils.setVariableValueInObject(wrongMojo, "monitorPort", 10000);
        ReflectionUtils.setVariableValueInObject(wrongMojo, "monitorKey", "jester");
    }

    /**
     * Verify that stop logs an error if there is no monitor running.
     */
    @Test
    void testStopWithNoServer() {
        when(mojo.getLog()).thenReturn(log);
        mojo.execute();
        verify(log).info("Sending command \"stop\" to monitor");
        verify(log).error(eq("Error sending command to monitor"), any(IOException.class));
        verifyNoMoreInteractions(server, logger, log, observer);
    }

    /**
     * Start a mock server and verify that the {@link com.btmatthews.utils.monitor.mojo.AbstractStopMojo} signals it to shutdown.
     *
     * @throws Exception If the test case failed.
     */
    @Test
    void testStopWithRunningServer() throws Exception {
        when(mojo.getLog()).thenReturn(log);
        when(server.isStarted(any(Logger.class))).thenReturn(true);
        when(server.isStopped(any(Logger.class))).thenReturn(true);
        final Monitor monitor = new Monitor("dummy", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        runWithDelay(mojo::execute);
        monitorThread.join(15000L);
        verify(server).start(logger);
        verify(server, times(2)).isStarted(logger);
        verify(logger).logInfo("Waiting for command from client");
        verify(log).info("Sending command \"stop\" to monitor");
        verify(logger).logInfo("Receiving command from client");
        verify(server).stop(logger);
        verify(server).isStopped(logger);
        verify(observer).started(server, logger);
        verify(observer).stopped(server, logger);
        verifyNoMoreInteractions(server, logger, log, observer);
    }

    /**
     * Verify that stop logs an error if the wrong client is used.
     *
     * @throws Exception If the unit test failed.
     */
    @Test
    void testStopWithWrongMojo() throws Exception {
        when(mojo.getLog()).thenReturn(log);
        when(wrongMojo.getLog()).thenReturn(log);
        when(server.isStarted(any(Logger.class))).thenReturn(true);
        when(server.isStopped(any(Logger.class))).thenReturn(true);
        final Monitor monitor = new Monitor("dummy", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        runWithDelay(() -> {
            wrongMojo.execute();
            mojo.execute();
        });
        monitorThread.join(15000L);
        verify(server).start(logger);
        verify(server, times(2)).isStarted(logger);
        verify(logger, times(2)).logInfo("Waiting for command from client");
        verify(log, times(2)).info("Sending command \"stop\" to monitor");
        verify(logger, times(2)).logInfo("Receiving command from client");
        verify(logger).logError("Invalid monitor key");
        verify(server).stop(logger);
        verify(server).isStopped(logger);
        verify(observer).started(server, logger);
        verify(observer).stopped(server, logger);
        verifyNoMoreInteractions(server, logger, log, observer);
    }
}
