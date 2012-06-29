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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The monitor object is used to control a server.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.0.0
 */
public final class Monitor {

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
     * @param server The server being monitored.
     * @param logger Used to log error messages.
     */
    public void runMonitor(final Server server, final Logger logger) {
        try {
            final ServerSocket serverSocket = new ServerSocket(monitorPort, 1,
                    InetAddress.getLocalHost());
            try {
                serverSocket.setReuseAddress(true);
                runMonitorInternal(server, logger, serverSocket);
            } finally {
                serverSocket.close();
            }
        } catch (final IOException exception) {
            logger.logError("Error starting or stopping the monitor", exception);
        }
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
        final InputStream inputStream = clientSocket
                .getInputStream();
        final Reader reader = new InputStreamReader(
                inputStream);
        final LineNumberReader lineReader = new LineNumberReader(
                reader);
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
            final Socket socket = new Socket(InetAddress.getLocalHost(),
                    monitorPort);
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
     * <li>pause - Pause the server</li>
     * <li>resume - Resume the paused server</li>
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
        if (command.startsWith("configure ")) {
            final int split = command.indexOf('=', 10);
            server.configure(command.substring(10, split), command.substring(split + 1), logger);
        } else if ("pause".equals(command)) {
            server.pause(logger);
        } else if ("resume".equals(command)) {
            server.resume(logger);
        } else if ("stop".equals(command)) {
            server.stop(logger);
            return false;
        }
        return true;
    }
}
