/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.freeciv.test;

import org.freeciv.connection.Interpretated;
import org.freeciv.connection.NotReadyYetException;
import org.freeciv.connection.ReflexReaction;
import org.freeciv.packet.Packet;
import org.freeciv.packet.RawPacket;
import org.freeciv.utility.ArgumentSettings;
import org.freeciv.utility.Setting;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.util.*;

public class ProxyRecorder implements Runnable {
    private static final String PROXY_PORT = "proxy-port";
    private static final String REAL_SERVER_PORT = "real-server-port";
    private static final String REAL_SERVER_ADDRESS = "real-server-address";
    private static final String TRACE_NAME_START = "trace-name-start";
    private static final String TRACE_NAME_END = "trace-name-end";
    private static final String TRACE_DYNAMIC = "record-time";
    private static final String VERBOSE = "verbose";
    private static final String DEBUG = "debug";

    private final int proxyNumber;
    private final ArgumentSettings settings;
    private final Interpretated clientCon;
    private final Interpretated serverCon;
    private final DataOutputStream trace;

    // Debug data
    private final HashSet<Integer> debugHadProblem = new HashSet<Integer>();
    private final HashSet<Integer> debugDidWork = new HashSet<Integer>();

    private boolean started = false;

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(new LinkedList<Setting<?>>(){{
            add(new Setting.IntSetting(PROXY_PORT, 5556));
            add(new Setting.IntSetting(REAL_SERVER_PORT, 55555));
            add(new Setting.StringSetting(REAL_SERVER_ADDRESS, "127.0.0.1"));

            add(new Setting.StringSetting(TRACE_NAME_START, "FreecivCon"));
            add(new Setting.StringSetting(TRACE_NAME_END, ".fct"));
            add(new Setting.BoolSetting(TRACE_DYNAMIC, true));

            add(new Setting.BoolSetting(VERBOSE, false));
            add(new Setting.BoolSetting(DEBUG, false));
        }}, args);

        System.out.println("Listening for Freeciv clients on port " + settings.getSetting(PROXY_PORT));
        System.out.println("Will connect to Freeciv server at " + settings.getSetting(REAL_SERVER_ADDRESS) +
                ", port " + settings.getSetting(REAL_SERVER_PORT));
        System.out.println("Trace files will have a name starting with " + settings.getSetting(TRACE_NAME_START) +
                " followed by the number the proxy has given to the connection and ending in " +
                settings.getSetting(TRACE_NAME_END));
        System.out.println("Time data " + (settings.<Boolean>getSetting(TRACE_DYNAMIC) ? "is" : "isn't") +
                " included in the trace.");
        System.out.println((settings.<Boolean>getSetting(VERBOSE) ? "Will" : "Won't") +
                " be verbose in output here.");

        try {
            ServerSocket serverProxy = new ServerSocket(settings.<Integer>getSetting(PROXY_PORT));
            ArrayList<ProxyRecorder> connections = new ArrayList<ProxyRecorder>();
            while (!serverProxy.isClosed())
                try {
                    Interpretated clientCon =
                            new Interpretated(serverProxy.accept(), Collections.<Integer, ReflexReaction>emptyMap());
                    ProxyRecorder proxy = new ProxyRecorder(clientCon, connections.size(),
                            new DataOutputStream(new BufferedOutputStream(
                                    new FileOutputStream(settings.<String>getSetting(TRACE_NAME_START) +
                                            connections.size() + settings.<String>getSetting(TRACE_NAME_END)))),
                            settings);
                    connections.add(proxy);
                    (new Thread(proxy)).start();
                } catch (IOException e) {
                    System.err.println("Failed accepting new connection");
                    e.printStackTrace();
                }
        } catch (IOException e) {
            System.err.println("Port not free");
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public ProxyRecorder(Interpretated clientCon, int proxyNumber, DataOutputStream trace, ArgumentSettings settings)
            throws IOException, InterruptedException {
        this.proxyNumber = proxyNumber;
        this.settings = settings;
        this.trace = trace;
        this.clientCon = clientCon;
        try {
            serverCon = new Interpretated(settings.<String>getSetting(REAL_SERVER_ADDRESS),
                    settings.<Integer>getSetting(REAL_SERVER_PORT),
                    Collections.<Integer, ReflexReaction>emptyMap());
        } catch (IOException e) {
            throw new IOException(proxyNumber + ": Unable to connect to server", e);
        }
    }

    @Override
    public void run() {
        if (!started)
            started = true;
        else
            throw new IllegalStateException("Already started");

        try {
            // the version of the trace format
            trace.writeChar(1);
            // is the time a packet arrived included in the trace
            trace.writeBoolean(settings.<Boolean>getSetting(TRACE_DYNAMIC));
        } catch (IOException e) {
            System.err.println(proxyNumber + ": Unable to write trace");
            e.printStackTrace();

            cleanUp();
            return;
        }

        while (clientCon.isOpen() && serverCon.isOpen()) {
            proxyPacket(clientCon, serverCon, true);
            proxyPacket(serverCon, clientCon, false);
        }

        cleanUp();

        System.out.println(proxyNumber + " is finished");
    }

    private void cleanUp() {
        clientCon.setOver();
        serverCon.setOver();
        try {
            trace.close();
        } catch (IOException e) {
            System.err.println("Some data may not have been written to the trace for connection " + proxyNumber);
            e.printStackTrace();
        }
    }

    private void proxyPacket(Interpretated readFrom, Interpretated writeTo, boolean clientToServer) {
        try {
            Packet fromClient = readFrom.getPacket();

            if (printPacket(fromClient))
                System.out.println(proxyNumber + (clientToServer ? " c2s: " : " s2c: ") + fromClient);

            if (settings.<Boolean>getSetting(DEBUG)) {
                debugUpdate(fromClient);
                if (debugFailsSometimes(fromClient))
                    System.out.println("Debug: " + fromClient.getHeader().getPacketKind() + " fails but not always");
            }

            trace.writeBoolean(clientToServer);
            if (settings.<Boolean>getSetting(TRACE_DYNAMIC))
                trace.writeLong(System.currentTimeMillis());
            fromClient.encodeTo(trace);

            writeTo.toSend(fromClient);
        } catch (NotReadyYetException e) {
            Thread.yield();
        } catch (IOException e) {
            System.err.println("Couldn't get or couldn't write packet. Finishing...");
            e.printStackTrace();
            readFrom.setOver();
            serverCon.setOver();
        }
    }

    private boolean printPacket(Packet fromClient) {
        return settings.<Boolean>getSetting(VERBOSE) || fromClient instanceof RawPacket || debugFailsSometimes(fromClient);
    }

    private boolean debugFailsSometimes(Packet fromClient) {
        return debugHadProblem.contains(fromClient.getHeader().getPacketKind()) &&
                debugDidWork.contains(fromClient.getHeader().getPacketKind());
    }

    private void debugUpdate(Packet fromClient) {
        if (fromClient instanceof RawPacket)
            debugHadProblem.add(fromClient.getHeader().getPacketKind());
        else
            debugDidWork.add(fromClient.getHeader().getPacketKind());
    }

}
