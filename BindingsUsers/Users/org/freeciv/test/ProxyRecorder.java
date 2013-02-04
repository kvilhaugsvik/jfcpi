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
import org.freeciv.utility.UI;

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

    // Stuff to do to the data
    private final List<Feature> steps;

    private boolean started = false;

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(new LinkedList<Setting<?>>(){{
            add(new Setting.IntSetting(PROXY_PORT, 5556, "listen for the Freeciv client on this port"));
            add(new Setting.IntSetting(REAL_SERVER_PORT, 55555, "connect to the Freeciv server on ths port"));
            add(new Setting.StringSetting(REAL_SERVER_ADDRESS,
                    "127.0.0.1", "connect to the Freeciv server on this address"));

            add(new Setting.StringSetting(TRACE_NAME_START, "FreecivCon", "prefix of the trace file names"));
            add(new Setting.StringSetting(TRACE_NAME_END, ".fct", "suffix of the trace file names"));
            add(new Setting.BoolSetting(TRACE_DYNAMIC, true, "should time be recorded in the trace"));

            add(UI.HELP_SETTING);

            add(new Setting.BoolSetting(VERBOSE, false, "print all packets to the terminal"));
            add(new Setting.BoolSetting(DEBUG, false, "print debug information to the terminal"));
        }}, args);

        UI.printAndExitOnHelp(settings, ProxyRecorder.class);

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
        if (settings.<Boolean>getSetting(DEBUG))
            System.out.println("In debug mode.");

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

        this.steps = new LinkedList<Feature>();

        steps.add(new PrintRaw());

        if (settings.<Boolean>getSetting(DEBUG))
            steps.add(new Debug());
    }

    @Override
    public void run() {
        if (!started)
            started = true;
        else
            throw new IllegalStateException("Already started");

        final Sink traceSink;
        try {
            traceSink = new SinkWriteToDisk();
        } catch (IOException e) {
            System.err.println(proxyNumber + ": Unable to write trace");
            e.printStackTrace();

            cleanUp();
            return;
        }
        final Sink cons = new SinkInformUser();
        final SinkForward sinkServer = new SinkForward(serverCon);
        final SinkForward sinkClient = new SinkForward(clientCon);

        while (clientCon.isOpen() && serverCon.isOpen()) {
            proxyPacket(clientCon, true, traceSink, cons, sinkServer);
            proxyPacket(serverCon, false, traceSink, cons, sinkClient);
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

    private void proxyPacket(Interpretated readFrom, boolean clientToServer, Sink traceSink, Sink cons, SinkForward sinkForward) {
        try {
            Packet fromClient = readFrom.getPacket();

            for (Feature step : steps)
                step.update(fromClient);

            if (printPacket(fromClient))
                cons.write(clientToServer, fromClient);

            for (Feature step : steps)
                step.inform(fromClient);

            traceSink.write(clientToServer, fromClient);

            sinkForward.write(clientToServer, fromClient);
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
        if (settings.<Boolean>getSetting(VERBOSE))
            return true;

        for (Feature step : steps)
            if (step.wantsAtConsole(fromClient))
                return true;

        return false;
    }

    interface Sink {
        public void write(boolean clientToServer, Packet packet) throws IOException;
    }

    class SinkWriteToDisk implements Sink {
        public SinkWriteToDisk() throws IOException {
            // the version of the trace format
            trace.writeChar(1);
            // is the time a packet arrived included in the trace
            trace.writeBoolean(settings.<Boolean>getSetting(TRACE_DYNAMIC));
        }

        public void write(boolean clientToServer, Packet packet) throws IOException {
            trace.writeBoolean(clientToServer);
            if (settings.<Boolean>getSetting(TRACE_DYNAMIC))
                trace.writeLong(System.currentTimeMillis());
            packet.encodeTo(trace);
        }
    }

    class SinkForward implements Sink {
        private final Interpretated writeTo;

        SinkForward(Interpretated writeTo) {
            this.writeTo = writeTo;
        }

        public void write(boolean clientToServer, Packet packet) throws IOException {
            writeTo.toSend(packet);
        }
    }

    class SinkInformUser implements Sink {
        public void write(boolean clientToServer, Packet packet) {
            System.out.println(proxyNumber + (clientToServer ? " c2s: " : " s2c: ") + packet);
        }
    }

    interface Feature {
        public void update(Packet packet);
        public boolean wantsAtConsole(Packet packet);
        public void inform(Packet packet);
    }

    static class PrintRaw implements Feature {
        public void update(Packet packet) {}

        public boolean wantsAtConsole(Packet packet) {
            return packet instanceof RawPacket;
        }

        public void inform(Packet packet) {}
    }

    static class Debug implements Feature {
        private final HashSet<Integer> debugHadProblem = new HashSet<Integer>();
        private final HashSet<Integer> debugDidWork = new HashSet<Integer>();

        public void update(Packet packet) {
            if (packet instanceof RawPacket)
                debugHadProblem.add(packet.getHeader().getPacketKind());
            else
                debugDidWork.add(packet.getHeader().getPacketKind());
        }

        public boolean wantsAtConsole(Packet packet) {
            return debugHadProblem.contains(packet.getHeader().getPacketKind()) &&
                    debugDidWork.contains(packet.getHeader().getPacketKind());
        }

        public void inform(Packet packet) {
            if (wantsAtConsole(packet))
                System.out.println("Debug: " + packet.getHeader().getPacketKind() + " fails but not always");
        }
    }
}
