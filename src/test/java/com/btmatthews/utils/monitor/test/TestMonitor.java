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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Timer;
import java.util.TimerTask;

import com.btmatthews.utils.monitor.Logger;
import com.btmatthews.utils.monitor.Monitor;
import com.btmatthews.utils.monitor.MonitorObserver;
import com.btmatthews.utils.monitor.Server;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit test the monitor.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @version 1.0.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Monitor.class })
public class TestMonitor {

    /**
     * Mock the server test fixture.
     */
    @Mock
    private Server server;

    /**
     * Mock the logger test fixture.
     */
    @Mock
    private Logger logger;

    /**
     * Mock the observer test fixture.
     */
    @Mock
    private MonitorObserver observer;

    /**
     * Initialise the mock test fixtures.
     */
    @Before
    public void setUp() {
        initMocks(this);
        when(server.isStarted(any(Logger.class))).thenReturn(true);
        when(server.isStopped(any(Logger.class))).thenReturn(true);
    }

    /**
     * Verify that a monitor can be started and stopped successfully.
     *
     * @throws Exception If the test case fails.
     */
    @Test
    public void testMonitor() throws Exception {
        final Monitor monitor = new Monitor("test", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                monitor.sendCommand("stop", logger);
            }
        }, 5000L);
        monitorThread.join(10000L);
        verify(server).start(same(logger));
        verify(server, times(2)).isStarted(same(logger));
        verify(logger).logInfo(eq("Waiting for command from client"));
        verify(logger).logInfo(eq("Sending command \"stop\" to monitor"));
        verify(logger).logInfo(eq("Receiving command from client"));
        verify(server).stop(same(logger));
        verify(server).isStopped(same(logger));
        verify(observer).started(same(server), same(logger));
        verify(observer).stopped(same(server), same(logger));
        verifyNoMoreInteractions(logger, server, observer);
        validateMockitoUsage();
    }

    /**
     * Verify that a server can be configured via the monitor.
     *
     * @throws Exception If the test case fails.
     */
    @Test
    public void testMonitorConfigure() throws Exception {
        final Monitor monitor = new Monitor("test", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Monitor.sendCommand("test", 10000, "configure debug=off", logger);
                Monitor.sendCommand("test", 10000, "stop", logger);
            }
        }, 5000L);
        monitorThread.join(10000L);
        verify(server).start(same(logger));
        verify(server, times(2)).isStarted(same(logger));
        verify(logger, times(2)).logInfo(eq("Waiting for command from client"));
        verify(logger).logInfo(eq("Sending command \"configure debug=off\" to monitor"));
        verify(logger).logInfo(eq("Sending command \"stop\" to monitor"));
        verify(logger, times(2)).logInfo(eq("Receiving command from client"));
        verify(server).configure(eq("debug"), eq("off"), same(logger));
        verify(server).stop(same(logger));
        verify(server).isStopped(same(logger));
        verify(observer).started(same(server), same(logger));
        verify(observer).stopped(same(server), same(logger));
        verifyNoMoreInteractions(logger, server, observer);
        validateMockitoUsage();
    }

    /**
     * Verify that the server ignores commands with an invalid key.
     *
     * @throws Exception If the test case fails.
     */
    @Test
    public void testInvalidMonitorKey() throws Exception {
        final Monitor monitor = new Monitor("test", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Monitor.sendCommand("TEST", 10000, "stop", logger);
                Monitor.sendCommand("test", 10000, "stop", logger);
            }
        }, 5000L);
        monitorThread.join(10000L);
        verify(server).start(same(logger));
        verify(server, times(2)).isStarted(same(logger));
        verify(logger, times(2)).logInfo(eq("Waiting for command from client"));
        verify(logger, times(2)).logInfo(eq("Sending command \"stop\" to monitor"));
        verify(logger, times(2)).logInfo(eq("Receiving command from client"));
        verify(logger).logError(eq("Invalid monitor key"));
        verify(server).stop(same(logger));
        verify(server).isStopped(same(logger));
        verify(observer).started(same(server), same(logger));
        verify(observer).stopped(same(server), same(logger));
        verifyNoMoreInteractions(logger, server, observer);
        validateMockitoUsage();
    }

    /**
     * Verify that the monitor will log an error if it could nt open a TCP port.
     *
     * @throws Exception If the test case fails.
     */
    @Test
    public void testRunMonitorWithIOException() throws Exception {
        whenNew(ServerSocket.class).withNoArguments().thenThrow(new IOException());
        final Monitor monitor = new Monitor("test", 10000);
        monitor.runMonitor(server, logger, observer);
        verify(logger).logError(eq("Error starting or stopping the monitor"), any(IOException.class));
        verifyNoMoreInteractions(logger, server, observer);
        validateMockitoUsage();
    }
}
