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

import org.freeciv.connection.*;
import org.freeciv.packet.Packet;
import org.freeciv.utility.ArgumentSettings;
import org.freeciv.utility.Setting;
import org.freeciv.utility.UI;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

public class ProxyRecorder extends Thread {
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
    private static final String UNDERSTAND = "understand";
    private static final String DEBUG = "debug";
    private static final String TO_TRACE = "trace-to-console";

    public static final Filter CONNECTION_PACKETS = new FilterPacketKind(Arrays.asList(88, 89, 115, 116, 119));

    public static final String DEFAULT_TRACE_PREFIX = "FreecivCon";
    public static final String DEFAULT_TRACE_SUFFIX = ".fct";

    public static final LinkedList<Setting<?>> SETTINGS = new LinkedList<Setting<?>>() {{
        add(new Setting.IntSetting(PROXY_PORT, 5556, "listen for the Freeciv client on this port"));
        add(new Setting.IntSetting(REAL_SERVER_PORT, 55555, "connect to the Freeciv server on ths port"));
        add(new Setting.StringSetting(REAL_SERVER_ADDRESS,
                "127.0.0.1", "connect to the Freeciv server on this address"));

        add(new Setting.StringSetting(TRACE_NAME_START, DEFAULT_TRACE_PREFIX, "prefix of the trace file names"));
        add(new Setting.StringSetting(TRACE_NAME_END, DEFAULT_TRACE_SUFFIX, "suffix of the trace file names"));
        add(new Setting.BoolSetting(TRACE_DYNAMIC, true, "should time be recorded in the trace"));
        add(new Setting.BoolSetting(TRACE_EXCLUDE_CONNECTION, false,
                "don't record connection packets in the trace"));
        add(new Setting.BoolSetting(TRACE_EXCLUDE_C2S, false,
                "don't record packets sent by the client in the trace"));
        add(new Setting.BoolSetting(TRACE_EXCLUDE_S2C, false,
                "don't record packets sent by the server in the trace"));

        add(UI.HELP_SETTING);

        add(new Setting.BoolSetting(VERBOSE, false, "be verbose"));
        add(new Setting.BoolSetting(UNDERSTAND, false,
                "interpret the packets before processing. Warn if not understood."));
        add(new Setting.BoolSetting(DEBUG, false, "print debug information to the terminal"));
        add(new Setting.BoolSetting(TO_TRACE, false, "print all packets going to the trace to the terminal"));
    }};

    private final int proxyNumber;
    private final FreecivConnection clientCon;
    private final FreecivConnection serverCon;
    private final OutputStream trace;

    private final List<Sink> c2sSinks;
    private final List<Sink> s2cSinks;

    private static boolean timeToExit = false;

    private boolean started = false;
    private boolean finished = false;

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

        final ServerSocket serverProxy;
        try {
            serverProxy = new ServerSocket(settings.<Integer>getSetting(PROXY_PORT));
            serverProxy.setSoTimeout(2500);
        } catch (IOException e) {
            System.err.println("Error starting to listen to port " + settings.<Integer>getSetting(PROXY_PORT));
            System.exit(1);
            return;
        }

        ArrayList<ProxyRecorder> connections = new ArrayList<ProxyRecorder>();
        while (!timeToExit) {
            final Socket client;
            try {
                client = serverProxy.accept();
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                System.err.println("Incoming connection: Failed accepting new connection");
                e.printStackTrace();
                continue; // Todo: Should this exit the program?
            }

            final Socket server;
            try {
                server = new Socket(settings.<String>getSetting(REAL_SERVER_ADDRESS),
                        settings.<Integer>getSetting(REAL_SERVER_PORT));
            } catch (IOException e) {
                System.err.println("Incoming connection: Failed connecting to server");
                e.printStackTrace();
                closeIt(client);
                continue; // Todo: Should this exit the program?
            }

            final OutputStream traceOut;
            try {
                traceOut = new BufferedOutputStream(
                        new FileOutputStream(settings.<String>getSetting(TRACE_NAME_START) +
                                connections.size() + settings.<String>getSetting(TRACE_NAME_END)));
            } catch (IOException e) {
                System.err.println("Incoming connection: Failed opening trace file");
                e.printStackTrace();
                closeIt(client);
                closeIt(server);
                continue; // Todo: Should this exit the program?
            }

            try {
                final ProxyRecorder proxy = new ProxyRecorder(client, server, traceOut, connections.size(), settings);
                connections.add(proxy);
                proxy.start();
            } catch (IOException e) {
                System.err.println("Incoming connection: Failed starting");
                e.printStackTrace();
                closeIt(traceOut);
                closeIt(client);
                closeIt(server);
                continue; // Todo: Should this exit the program?
            }
        }

        closeIt(serverProxy);

        // wait for all the connections
        for (ProxyRecorder connection : connections)
            while (!connection.isFinished()) {
                Thread.yield();
            }

        System.exit(0);
    }

    private static void closeIt(Closeable toClose) {
        try {
            toClose.close();
        } catch (IOException e) {
            System.err.println("Problems while closing " + toClose);
            e.printStackTrace();
        }
    }

    public ProxyRecorder(Socket client, Socket server, OutputStream trace, int proxyNumber, ArgumentSettings settings)
            throws IOException, InterruptedException {
        this.proxyNumber = proxyNumber;
        this.trace = trace;

        if (settings.<Boolean>getSetting(UNDERSTAND)) {
            this.clientCon = new Interpreted(client, Collections.<Integer, ReflexReaction>emptyMap(), Collections.<Integer, ReflexReaction>emptyMap());
            this.serverCon = new Interpreted(server, getServerConnectionReflexes(), Collections.<Integer, ReflexReaction>emptyMap());
        } else {
            PacketsMapping versionKnowledge = new PacketsMapping(); // keep using PacketsMapping until format is settled
            this.clientCon = new Uninterpreted(client, versionKnowledge.getNewPacketHeaderData(),
                    versionKnowledge.getRequiredPostReceiveRules(), versionKnowledge.getRequiredPostSendRules());
            this.serverCon = new Uninterpreted(server, versionKnowledge.getNewPacketHeaderData(),
                    ReflexPacketKind.layer(versionKnowledge.getRequiredPostReceiveRules(), getServerConnectionReflexes()),
                    versionKnowledge.getRequiredPostSendRules());
        }

        Filter forwardFilters = new FilterAllAccepted(); // Forward everything
        Filter diskFilters = buildTraceFilters(settings);
        Filter consoleFilters = buildConsoleFilters(settings, diskFilters);

        final Sink traceSink =
                new SinkWriteTrace(diskFilters, trace, settings.<Boolean>getSetting(TRACE_DYNAMIC), proxyNumber);
        final Sink cons = new SinkInformUser(consoleFilters, proxyNumber);
        final SinkForward sinkServer = new SinkForward(serverCon, forwardFilters);
        final SinkForward sinkClient = new SinkForward(clientCon, forwardFilters);

        this.c2sSinks = Arrays.asList(cons, traceSink, sinkServer);
        this.s2cSinks = Arrays.asList(cons, traceSink, sinkClient);
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

        if (settings.<Boolean>getSetting(UNDERSTAND))
            out.add(new FilterIsRaw());

        if (settings.<Boolean>getSetting(DEBUG))
            out.add(new FilterSometimesWorking());

        if (settings.<Boolean>getSetting(TO_TRACE))
            out.add(diskFilters);

        return new FilterOr(out);
    }

    private static HashMap<Integer, ReflexReaction> getServerConnectionReflexes() {
        HashMap<Integer, ReflexReaction> reflexes = new HashMap<Integer, ReflexReaction>();
        reflexes.put(8, new ReflexReaction<ConnectionRelated>() {
            @Override
            public void apply(Packet incoming, ConnectionRelated connection) {
                timeToExit = true;
            }
        });
        return reflexes;
    }

    @Override
    public void run() {
        if (!started)
            started = true;
        else
            throw new IllegalStateException("Already started");

        while (clientCon.isOpen() && serverCon.isOpen()) {
            proxyPacket(clientCon, true, c2sSinks);
            proxyPacket(serverCon, false, s2cSinks);
            checkIfGlobalOver();
        }

        cleanUp();

        finished = true;
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

    private void proxyPacket(PacketRead readFrom, boolean clientToServer, List<Sink> sinks) {
        try {
            Packet packet;

            try {
                packet = readFrom.getPacket();
            } catch (IOException e) {
                throw new IOException("Couldn't read packet from " + (clientToServer ? "client" : "server"), e);
            }

            for (Sink sink : sinks)
                sink.filteredWrite(clientToServer, packet);

        } catch (NotReadyYetException e) {
            Thread.yield();
        } catch (IOException e) {
            System.err.println("Finishing...");
            e.printStackTrace();
            clientCon.setOver();
            serverCon.setOver();
        }
    }

    private void checkIfGlobalOver() {
        if (timeToExit) {
            clientCon.setOver();
            serverCon.setOver();
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
