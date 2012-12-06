package com.btmatthews.utils.monitor.test.mojo;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.Monitor;
import com.btmatthews.utils.monitor.MonitorObserver;
import com.btmatthews.utils.monitor.Server;
import com.btmatthews.utils.monitor.mojo.AbstractStopMojo;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit tests for the Mojo that implements the stop goal.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbstractStopMojo.class })
public class TestStopMojo {

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
     * Spy test fixture.
     */
    @Spy
    private AbstractStopMojo mojo = new AbstractStopMojo(){};

    /**
     * Spy test fixture.
     */
    @Spy
    private AbstractStopMojo wrongMojo = new AbstractStopMojo(){};

    /**
     * Prepare for test case execution by initialising the mocks.
     */
    @Before
    public void setUp() throws Exception {
        initMocks(this);
        ReflectionUtils.setVariableValueInObject(mojo, "monitorPort", 10000);
        ReflectionUtils.setVariableValueInObject(mojo, "monitorKey", "dummy");
        ReflectionUtils.setVariableValueInObject(wrongMojo, "monitorPort", 10000);
        ReflectionUtils.setVariableValueInObject(wrongMojo, "monitorKey", "jester");
        when(mojo.getLog()).thenReturn(log);
        when(wrongMojo.getLog()).thenReturn(log);
    }


    /**
     * Verify that stop logs an error if there is no monitor running.
     *
     * @throws Exception If there was an error.
     */
    @Test
    public void testStopWithNoServer() throws Exception {
        whenNew(Socket.class)
                .withParameterTypes(InetAddress.class, int.class)
                .withArguments(any(InetAddress.class), eq(10000))
                .thenThrow(new IOException());
        mojo.execute();
        verify(log).info(eq("Sending command \"stop\" to monitor"));
        verify(log).error(eq("Error sending command to monitor"), any(IOException.class));
        verifyZeroInteractions(server, logger, log, observer);
    }


    /**
     * Start a mock server and verify that the {@link com.btmatthews.utils.monitor.mojo.AbstractStopMojo} signals it to shutdown.
     *
     * @throws Exception If the test case failed.
     */
    @Test
    public void testStopWithRunningServer() throws Exception {
        final Monitor monitor = new Monitor("dummy", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    mojo.execute();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 5000L);
        monitorThread.join(15000L);
        verify(server).start(same(logger));
        verify(logger).logInfo(eq("Waiting for command from client"));
        verify(log).info(eq("Sending command \"stop\" to monitor"));
        verify(logger).logInfo(eq("Receiving command from client"));
        verify(server).stop(same(logger));
        verify(observer).started(same(server), same(logger));
        verify(observer).stopped(same(server), same(logger));
        verifyZeroInteractions(server, logger, log, observer);
        validateMockitoUsage();
    }

    /**
     * Verify that stop logs an error if the wrong client is used.
     *
     * @throws Exception If the unit test failed.
     */
    @Test
    public void testStopWithWrongMojo() throws Exception {
        final Monitor monitor = new Monitor("dummy", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    wrongMojo.execute();
                    mojo.execute();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 5000L);
        monitorThread.join(15000L);
        verify(server).start(same(logger));
        verify(logger, times(2)).logInfo(eq("Waiting for command from client"));
        verify(log, times(2)).info(eq("Sending command \"stop\" to monitor"));
        verify(logger, times(2)).logInfo(eq("Receiving command from client"));
        verify(logger).logError(eq("Invalid monitor key"));
        verify(server).stop(same(logger));
        verify(observer).started(same(server), same(logger));
        verify(observer).stopped(same(server), same(logger));
        verifyZeroInteractions(server, logger, log, observer);
        validateMockitoUsage();
    }
}
