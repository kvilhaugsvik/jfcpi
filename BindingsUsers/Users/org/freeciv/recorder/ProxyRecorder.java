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

package org.freeciv.recorder;

import org.freeciv.connection.Interpreted;
import org.freeciv.connection.NotReadyYetException;
import org.freeciv.connection.ReflexReaction;
import org.freeciv.packet.Packet;
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
    private static final String TRACE_EXCLUDE_CONNECTION = "trace-exclude-connection";
    private static final String TRACE_EXCLUDE_C2S = "trace-exclude-from-client";
    private static final String TRACE_EXCLUDE_S2C = "trace-exclude-from-server";
    private static final String VERBOSE = "verbose";
    private static final String DEBUG = "debug";
    private static final String TO_TRACE = "trace-to-console";

    public static final LinkedList<Setting<?>> SETTINGS = new LinkedList<Setting<?>>() {{
        add(new Setting.IntSetting(PROXY_PORT, 5556, "listen for the Freeciv client on this port"));
        add(new Setting.IntSetting(REAL_SERVER_PORT, 55555, "connect to the Freeciv server on ths port"));
        add(new Setting.StringSetting(REAL_SERVER_ADDRESS,
                "127.0.0.1", "connect to the Freeciv server on this address"));

        add(new Setting.StringSetting(TRACE_NAME_START, "FreecivCon", "prefix of the trace file names"));
        add(new Setting.StringSetting(TRACE_NAME_END, ".fct", "suffix of the trace file names"));
        add(new Setting.BoolSetting(TRACE_DYNAMIC, true, "should time be recorded in the trace"));
        add(new Setting.BoolSetting(TRACE_EXCLUDE_CONNECTION, false,
                "don't record connection packets in the trace"));
        add(new Setting.BoolSetting(TRACE_EXCLUDE_C2S, false,
                "don't record packets sent by the client in the trace"));
        add(new Setting.BoolSetting(TRACE_EXCLUDE_S2C, false,
                "don't record packets sent by the server in the trace"));

        add(UI.HELP_SETTING);

        add(new Setting.BoolSetting(VERBOSE, false, "be verbose"));
        add(new Setting.BoolSetting(DEBUG, false, "print debug information to the terminal"));
        add(new Setting.BoolSetting(TO_TRACE, false, "print all packets going to the trace to the terminal"));
    }};

    private static final Filter CONNECTION_PACKETS = new FilterPacketKind(Arrays.asList(88, 89, 115, 116, 119));

    private final int proxyNumber;
    private final ArgumentSettings settings;
    private final Interpreted clientCon;
    private final Interpreted serverCon;
    private final DataOutputStream trace;

    // Stuff to do to the data
    private final Filter consoleFilters;
    private final Filter diskFilters;
    private final Filter forwardFilters;

    private boolean started = false;

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(SETTINGS, args);

        UI.printAndExitOnHelp(settings, ProxyRecorder.class);

        System.out.println("Listening for Freeciv clients on port " + settings.getSetting(PROXY_PORT));
        System.out.println("Will connect to Freeciv server at " + settings.getSetting(REAL_SERVER_ADDRESS) +
                ", port " + settings.getSetting(REAL_SERVER_PORT));
        System.out.println("Trace files will be named " + settings.getSetting(TRACE_NAME_START) + "#" +
                settings.getSetting(TRACE_NAME_END) + " (# is the logged connection number)");
        if (settings.<Boolean>getSetting(VERBOSE))
            for (Setting.Settable option : settings.getAll())
                System.out.println(option.name() + " = " + option.get() + "\t" + option.describe());

        try {
            ServerSocket serverProxy = new ServerSocket(settings.<Integer>getSetting(PROXY_PORT));
            ArrayList<ProxyRecorder> connections = new ArrayList<ProxyRecorder>();
            while (!serverProxy.isClosed())
                try {
                    Interpreted clientCon =
                            new Interpreted(serverProxy.accept(), Collections.<Integer, ReflexReaction>emptyMap());
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

    public ProxyRecorder(Interpreted clientCon, int proxyNumber, DataOutputStream trace, ArgumentSettings settings)
            throws IOException, InterruptedException {
        this.proxyNumber = proxyNumber;
        this.settings = settings;
        this.trace = trace;
        this.clientCon = clientCon;
        try {
            serverCon = new Interpreted(settings.<String>getSetting(REAL_SERVER_ADDRESS),
                    settings.<Integer>getSetting(REAL_SERVER_PORT),
                    Collections.<Integer, ReflexReaction>emptyMap());
        } catch (IOException e) {
            throw new IOException(proxyNumber + ": Unable to connect to server", e);
        }

        this.forwardFilters = new FilterAllAccepted(); // Forward everything

        this.diskFilters = buildTraceFilters(settings);

        this.consoleFilters = buildConsoleFilters(settings, this.diskFilters);
    }

    static private Filter buildTraceFilters(ArgumentSettings settings) {
        LinkedList<Filter> out = new LinkedList<Filter>();

        if (settings.<Boolean>getSetting(TRACE_EXCLUDE_CONNECTION))
            out.add(new FilterNot(CONNECTION_PACKETS));

        if (settings.<Boolean>getSetting(TRACE_EXCLUDE_C2S))
            out.add(new FilterNot(new FilterPacketFromClientToServer()));

        if (settings.<Boolean>getSetting(TRACE_EXCLUDE_S2C))
            out.add(new FilterPacketFromClientToServer());

        return new FilterAnd(out);
    }

    static private Filter buildConsoleFilters(ArgumentSettings settings, Filter diskFilters) {
        LinkedList<Filter> out = new LinkedList<Filter>();

        if (settings.<Boolean>getSetting(VERBOSE))
            out.add(new FilterAllAccepted());

        out.add(new FilterIsRaw());

        if (settings.<Boolean>getSetting(DEBUG))
            out.add(new FilterSometimesWorking());

        if (settings.<Boolean>getSetting(TO_TRACE))
            out.add(diskFilters);

        return new FilterOr(out);
    }

    @Override
    public void run() {
        if (!started)
            started = true;
        else
            throw new IllegalStateException("Already started");

        final Sink traceSink;
        try {
            traceSink = new SinkWriteTrace(diskFilters, trace, settings.<Boolean>getSetting(TRACE_DYNAMIC));
        } catch (IOException e) {
            System.err.println(proxyNumber + ": Unable to write trace");
            e.printStackTrace();

            cleanUp();
            return;
        }
        final Sink cons = new SinkInformUser(consoleFilters, proxyNumber);
        final SinkForward sinkServer = new SinkForward(serverCon, forwardFilters);
        final SinkForward sinkClient = new SinkForward(clientCon, forwardFilters);

        List<Sink> c2sSinks = Arrays.asList(cons, traceSink, sinkServer);
        List<Sink> s2cSinks = Arrays.asList(cons, traceSink, sinkClient);

        while (clientCon.isOpen() && serverCon.isOpen()) {
            proxyPacket(clientCon, true, c2sSinks);
            proxyPacket(serverCon, false, s2cSinks);
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

    private void proxyPacket(Interpreted readFrom, boolean clientToServer, List<Sink> sinks) {
        try {
            Packet packet = readFrom.getPacket();

            for (Sink sink : sinks)
                sink.filteredWrite(clientToServer, packet);

        } catch (NotReadyYetException e) {
            Thread.yield();
        } catch (IOException e) {
            System.err.println("Couldn't get or couldn't write packet. Finishing...");
            e.printStackTrace();
            readFrom.setOver();
            serverCon.setOver();
        }
    }
}
