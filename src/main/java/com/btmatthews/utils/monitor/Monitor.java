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

package com.btmatthews.utils.monitor;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The monitor object is used to control a server.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.0.0
 */
public final class Monitor {

    /**
     * The regex for the configure command.
     */
    private static final Pattern CONFIGURE_PATTERN = Pattern.compile("configure\\s+(\\w+)=(.*)");
    /**
     * The default number of times to retry when checking for successful server start
     * or stop.
     *
     * @since 2.1.0
     */
    private static final int DEFAULT_RETRY_COUNT = 3;
    /**
     * The default interval between retries when checking for successful server start
     * or stop.
     *
     * @since 2.1.0
     */
    private static final int DEFAULT_RETRY_INTERVAL = 500;
    /**
     * The stop command.
     */
    private static final String STOP = "stop";
    /**
     * The monitor key that must prefix any commands.
     */
    private final String monitorKey;
    /**
     * The port on which the monitor is listening.
     */
    private final int monitorPort;
    /**
     * The number of times to retry when checking for successful server start
     * or stop.
     *
     * @since 2.1.0
     */
    private int retryCount;
    /**
     * The interval between retries when checking for successful server start
     * or stop.
     *
     * @since 2.1.0
     */
    private int retryInterval;

    /**
     * The constructor that initialises the monitor key and port.
     *
     * @param key  The monitor key that must prefix any commands.
     * @param port The port on which the monitor is listening.
     */
    public Monitor(final String key, final int port) {
        this(key, port, DEFAULT_RETRY_COUNT, DEFAULT_RETRY_INTERVAL);
    }

    /**
     * The constructor that initialises the monitor key and port.
     *
     * @param key      The monitor key that must prefix any commands.
     * @param port     The port on which the monitor is listening.
     * @param count    The number of retry counts.
     * @param interval The intervals between retries.
     */
    public Monitor(final String key, final int port, final int count, final int interval) {
        monitorKey = key;
        monitorPort = port;
        retryCount = count;
        retryInterval = interval;
    }

    /**
     * Static method used to send a command to a server via a monitor.
     *
     * @param key     The monitor key.
     * @param port    The monitor port.
     * @param command The command to be sent to the server.
     * @param logger  Used to log information and error messages.
     */
    public static void sendCommand(final String key, final int port, final String command, final Logger logger) {
        new Monitor(key, port).sendCommand(command, logger);
    }

    /**
     * Run the monitor listening for commands and sending them to the server.
     *
     * @param server   The server being monitored.
     * @param logger   Used to log error messages.
     * @param observer Used to handle notifications for server start and stop.
     */
    public void runMonitor(final Server server, final Logger logger, final MonitorObserver observer) {
        try {
            final ServerSocket serverSocket = bindMonitor();
            try {
                server.start(logger);
                if (waitForStart(server, logger)) {
                    observer.started(server, logger);
                    runMonitorInternal(server, logger, serverSocket);
                    if (waitForStop(server, logger)) {
                        observer.stopped(server, logger);
                    }
                }
            } finally {
                serverSocket.close();
            }
        } catch (final IOException exception) {
            logger.logError("Error starting or stopping the monitor", exception);
        }
    }

    /**
     * Spawn a thread used to run the monitor as daemon processes.
     *
     * @param server   The server.
     * @param logger   Used to log information and error messages.
     * @param observer Used to handle notifications for server start and stop.
     * @return The thread that was spawned to run the monitor.
     */
    public Thread runMonitorDaemon(final Server server, final Logger logger, final MonitorObserver observer) {
        final Thread monitorThread = new Thread(new Runnable() {
            public void run() {
                Monitor.this.runMonitor(server, logger, observer);
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
        waitForStart(server, logger);
        return monitorThread;
    }

    /**
     * Run the monitor listening for commands at {@code serverSocket} and sending them to the server.
     *
     * @param server       The server being monitored.
     * @param logger       Used to log error messages.
     * @param serverSocket The server socket on which the monitor is listening.
     */
    private void runMonitorInternal(final Server server, final Logger logger, final ServerSocket serverSocket) {
        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                try {
                    logger.logInfo("Waiting for command from client");
                    clientSocket = serverSocket.accept();
                    logger.logInfo("Receiving command from client");
                    clientSocket.setSoLinger(false, 0);
                    final String command = getCommand(clientSocket, logger);
                    if (command != null) {
                        running = executeCommand(server, command, logger);
                    }
                } finally {
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                }
            } catch (final IOException exception) {
                logger.logError("Error in the monitor", exception);
            }
        }
    }

    /**
     * Read the key and command combination from the client socket connection.
     *
     * @param clientSocket The client socket connection.
     * @param logger       Used to log error messages.
     * @return The command that was read from the client socket connection or {@code null} if the key did not match.
     * @throws IOException If there was an error reading from the client socket connection.
     */
    private String getCommand(final Socket clientSocket, final Logger logger) throws IOException {
        final InputStream inputStream = clientSocket.getInputStream();
        final Reader reader = new InputStreamReader(inputStream);
        final LineNumberReader lineReader = new LineNumberReader(reader);
        final String key = lineReader.readLine();
        if (monitorKey.equals(key)) {
            return lineReader.readLine();
        } else {
            logger.logError("Invalid monitor key");
            return null;
        }
    }

    /**
     * Send a command to the monitor.
     *
     * @param command The command.
     * @param logger  Used to log error messages.
     */
    public void sendCommand(final String command, final Logger logger) {
        try {
            logger.logInfo("Sending command \"" + command + "\" to monitor");
            final Socket socket = connectMonitor();
            try {
                socket.setSoLinger(false, 0);
                final OutputStream outputStream = socket.getOutputStream();
                final Writer writer = new OutputStreamWriter(outputStream);
                final PrintWriter printWriter = new PrintWriter(writer);
                printWriter.println(monitorKey);
                printWriter.println(command);
                printWriter.flush();
                socket.close();
            } finally {
                socket.close();
            }
        } catch (final IOException exception) {
            logger.logError("Error sending command to monitor", exception);
        }
    }

    /**
     * Execute a command that was sent to the monitor.
     * <p/>
     * The following commands are supported:
     * <ul>
     * <li>configure name=value - Set the server property {@code name} to {@code value}</li>
     * <li>stop - Stop the server</li>
     * </ul>
     *
     * @param server  The server.
     * @param command The command.
     * @param logger  Used to log error messages.
     * @return Indicates whether or not the monitor is should continue running.
     * <ul>
     * <li>{@code true} if the monitor should continue running</li>
     * <li>{@code false} if the monitor should stop</li>
     * </ul>
     */
    private boolean executeCommand(final Server server, final String command,
                                   final Logger logger) {
        final Matcher matcher = CONFIGURE_PATTERN.matcher(command);
        if (matcher.matches()) {
            server.configure(matcher.group(1), matcher.group(2), logger);
        } else if (STOP.equals(command)) {
            server.stop(logger);
            return false;
        }
        return true;
    }

    /**
     * Wait for the server to start.
     *
     * @param server The server being monitored.
     * @param logger Used to log error messages.
     * @return {@code true} if the server has started.
     * @since 2.1.0
     */
    private boolean waitForStart(final Server server, final Logger logger) {
        if (server.isStarted(logger)) {
            return true;
        }
        try {
            Thread.sleep(retryInterval);
        } catch (final InterruptedException e) {
            return false;
        }
        for (int i = 1; i < retryCount; ++i) {
            if (server.isStarted(logger)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wait for the server to stop.
     *
     * @param server The server being monitored.
     * @param logger Used to log error messages.
     * @return {@code true} if the server has stopped.
     * @since 2.1.0
     */
    private boolean waitForStop(final Server server, final Logger logger) {
        if (server.isStopped(logger)) {
            return true;
        }
        for (int i = 1; i < retryCount; ++i) {
            try {
                Thread.sleep(retryInterval);
            } catch (final InterruptedException e) {
                return false;
            }
            if (server.isStopped(logger)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bind to the monitor first attempting to use {@link java.net.InetAddress#getLocalHost()} then falling
     * back to using the loopback address if there is a security exception.
     *
     * @return The bound server socket.
     * @throws IOException If there was a problem binding to the server socket.
     * @since 2.1.2
     */
    private ServerSocket bindMonitor() throws IOException {
        final ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        try {
            serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), monitorPort), 1);
        } catch (final BindException e) {
            serverSocket.bind(new InetSocketAddress("localhost", monitorPort), 1);
        }
        return serverSocket;
    }

    /**
     * Connect to the monitor first attempting to use {@link java.net.InetAddress#getLocalHost()} then falling
     * back to using the loopback address if there is a security exception.
     *
     * @return The connected  socket.
     * @throws IOException If there was a problem connecting to the socket.
     * @since 2.1.2
     */
    private Socket connectMonitor() throws IOException {
        try {
            return new Socket(InetAddress.getLocalHost(), monitorPort);
        } catch (final ConnectException e) {
            return new Socket("localhost", monitorPort);
        }
    }
}
