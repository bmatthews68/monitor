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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
     * The constructor that initialises the monitor key and port.
     *
     * @param key  The monitor key that must prefix any commands.
     * @param port The port on which the monitor is listening.
     */
    public Monitor(final String key, final int port) {
        monitorKey = key;
        monitorPort = port;
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
            final ServerSocket serverSocket = new ServerSocket(monitorPort, 1, InetAddress.getLocalHost());
            try {
                serverSocket.setReuseAddress(true);
                server.start(logger);
                observer.started(server, logger);
                runMonitorInternal(server, logger, serverSocket);
                observer.stopped(server, logger);
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
        monitorThread.start();
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
     * Send a command to the monitor.
     *
     * @param command The command.
     * @param logger  Used to log error messages.
     */
    public void sendCommand(final String command, final Logger logger) {
        try {
            logger.logInfo("Sending command \"" + command + "\" to monitor");
            final Socket socket = new Socket(InetAddress.getLocalHost(), monitorPort);
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
     *         <ul>
     *         <li>{@code true} if the monitor should continue running</li>
     *         <li>{@code false} if the monitor should stop</li>
     *         </ul>
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
}
