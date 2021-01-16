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
import com.btmatthews.utils.monitor.Monitor;
import com.btmatthews.utils.monitor.MonitorObserver;
import com.btmatthews.utils.monitor.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test the monitor.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
public class TestMonitor extends AbstractMonitorTest {

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
     * Verify that a monitor can be started and stopped successfully.
     *
     * @throws Exception If the test case fails.
     */
    @Test
    void testMonitor() throws Exception {
        when(server.isStarted(any(Logger.class))).thenReturn(true);
        when(server.isStopped(any(Logger.class))).thenReturn(true);
        final Monitor monitor = new Monitor("test", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        runWithDelay(() -> monitor.sendCommand("stop", logger));
        monitorThread.join(15000L);
        verify(server).start(logger);
        verify(server, times(2)).isStarted(logger);
        verify(logger).logInfo("Waiting for command from client");
        verify(logger).logInfo("Sending command \"stop\" to monitor");
        verify(logger).logInfo("Receiving command from client");
        verify(server).stop(logger);
        verify(server).isStopped(logger);
        verify(observer).started(server, logger);
        verify(observer).stopped(server, logger);
        verifyNoMoreInteractions(logger, server, observer);
    }

    /**
     * Verify that a server can be configured via the monitor.
     *
     * @throws Exception If the test case fails.
     */
    @Test
    void testMonitorConfigure() throws Exception {
        when(server.isStarted(any(Logger.class))).thenReturn(true);
        when(server.isStopped(any(Logger.class))).thenReturn(true);
        final Monitor monitor = new Monitor("test", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        runWithDelay(() -> {
            Monitor.sendCommand("test", 10000, "configure debug=off", logger);
            Monitor.sendCommand("test", 10000, "stop", logger);
        });
        monitorThread.join(15000L);
        verify(server).start(logger);
        verify(server, times(2)).isStarted(logger);
        verify(logger, times(2)).logInfo("Waiting for command from client");
        verify(logger).logInfo("Sending command \"configure debug=off\" to monitor");
        verify(logger).logInfo("Sending command \"stop\" to monitor");
        verify(logger, times(2)).logInfo("Receiving command from client");
        verify(server).configure("debug","off", logger);
        verify(server).stop(logger);
        verify(server).isStopped(logger);
        verify(observer).started(server, logger);
        verify(observer).stopped(server, logger);
        verifyNoMoreInteractions(logger, server, observer);
    }

    /**
     * Verify that the server ignores commands with an invalid key.
     *
     * @throws Exception If the test case fails.
     */
    @Test
    void testInvalidMonitorKey() throws Exception {
        when(server.isStarted(any(Logger.class))).thenReturn(true);
        when(server.isStopped(any(Logger.class))).thenReturn(true);
        final Monitor monitor = new Monitor("test", 10000);
        final Thread monitorThread = monitor.runMonitorDaemon(server, logger, observer);
        runWithDelay(() -> {
            Monitor.sendCommand("TEST", 10000, "stop", logger);
            Monitor.sendCommand("test", 10000, "stop", logger);
        });
        monitorThread.join(15000L);
        verify(server).start(logger);
        verify(server, times(2)).isStarted(logger);
        verify(logger, times(2)).logInfo("Waiting for command from client");
        verify(logger, times(2)).logInfo("Sending command \"stop\" to monitor");
        verify(logger, times(2)).logInfo("Receiving command from client");
        verify(logger).logError("Invalid monitor key");
        verify(server).stop(logger);
        verify(server).isStopped(logger);
        verify(observer).started(server, logger);
        verify(observer).stopped(server, logger);
        verifyNoMoreInteractions(logger, server, observer);
    }

    /**
     * Verify that the monitor will log an error if it could nt open a TCP port.
     *
     * @throws IOException If the test case fails.
     */
    @Test
    void testRunMonitorWithIOException() throws IOException {
        try (final ServerSocket serverSocket1 = new ServerSocket();
             final ServerSocket serverSocket2 = new ServerSocket()) {
            serverSocket1.setReuseAddress(true);
            serverSocket2.setReuseAddress(true);
            serverSocket1.bind(new InetSocketAddress(InetAddress.getLocalHost(), 10000), 1);
            serverSocket2.bind(new InetSocketAddress("localhost", 10000), 1);
            final Monitor monitor = new Monitor("test", 10000);
            monitor.runMonitor(server, logger, observer);
            verify(logger).logError(eq("Error starting or stopping the monitor"), any(IOException.class));
            verifyNoMoreInteractions(logger, server, observer);
        }
    }
}
