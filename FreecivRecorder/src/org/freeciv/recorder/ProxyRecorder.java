/*
 * Copyright (c) 2012, 2013. Sveinung Kvilhaugsvik
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
import org.freeciv.utility.ArgumentSettings;
import org.freeciv.utility.Setting;
import org.freeciv.utility.UI;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

public class ProxyRecorder {
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
        add(new Setting.BoolSetting(TRACE_EXCLUDE_CONNECTION, true,
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

    private static boolean[] timeToExit = {false};
    private final Plumbing csPlumbing;
    private final Plumbing scPlumbing;

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(SETTINGS, args);

        UI.printAndExitOnHelp(settings, ProxyRecorder.class);
        printStarted(settings);

        final ProtocolData versionKnowledge = loadProtocol();

        final Filter forwardFilters = new FilterAllAccepted(); // Forward everything to the network
        final Filter diskFilters = buildTraceFilters(settings);

        final SinkInformUser.SharedData console =
                new SinkInformUser.SharedData(buildConsoleFilters(settings, diskFilters));

        final ServerSocket serverProxy = startListening(settings);
        final ArrayList<ProxyRecorder> connections = new ArrayList<ProxyRecorder>();

        fakeServer(settings, serverProxy, connections, diskFilters, forwardFilters, console, versionKnowledge);

        closeIt(serverProxy);

        letThemFinish(connections);

        System.exit(0);
    }

    private static ProtocolData loadProtocol() {
        try {
            return new ProtocolData();
        } catch (BadProtocolData e) {
            System.err.println("Problem loading protocol data.");
            System.exit(1);
            return null;
        }
    }

    private static void printStarted(ArgumentSettings settings) {
        System.out.println("Listening for Freeciv clients on port " + settings.getSetting(PROXY_PORT));
        System.out.println("Will connect to Freeciv server at " + settings.getSetting(REAL_SERVER_ADDRESS) +
                ", port " + settings.getSetting(REAL_SERVER_PORT));
        System.out.println("Trace files will be named " + settings.getSetting(TRACE_NAME_START) + "#" +
                settings.getSetting(TRACE_NAME_END) + " (# is the logged connection number)");
        if (settings.<Boolean>getSetting(VERBOSE))
            for (Setting.Settable option : settings.getAll())
                System.out.println(option.name() + " = " + option.get() + "\t" + option.describe());
    }

    private static ServerSocket startListening(ArgumentSettings settings) {
        try {
            final ServerSocket serverProxy = new ServerSocket(settings.<Integer>getSetting(PROXY_PORT));
            serverProxy.setSoTimeout(2500);
            return serverProxy;
        } catch (IOException e) {
            System.err.println("Error starting to listen to port " + settings.<Integer>getSetting(PROXY_PORT));
            System.exit(1);
            return null;
        }
    }

    private static void fakeServer(ArgumentSettings settings, ServerSocket serverProxy, ArrayList<ProxyRecorder> connections, Filter diskFilters, Filter forwardFilters, SinkInformUser.SharedData console, ProtocolData versionKnowledge) throws InterruptedException, InvocationTargetException {
        final FirstTimeRequest firstConnectionTime = new FirstTimeRequest();

        while (!timeToExit[0]) {
            final Socket client;
            try {
                client = serverProxy.accept();
            } catch (SocketTimeoutException e) {
                if (0 < connections.size() && allAreFinished(connections))
                    timeToExit[0] = true;
                continue;
            } catch (IOException e) {
                failedAcceptingConnection(e);
                continue; // Todo: Should this exit the program?
            }

            final Socket server;
            try {
                server = new Socket(settings.<String>getSetting(REAL_SERVER_ADDRESS),
                        settings.<Integer>getSetting(REAL_SERVER_PORT));
            } catch (IOException e) {
                failedConnectingToServer(client, e);
                continue; // Todo: Should this exit the program?
            }

            final OutputStream traceOut;
            try {
                traceOut = new BufferedOutputStream(
                        new FileOutputStream(settings.<String>getSetting(TRACE_NAME_START) +
                                connections.size() + settings.<String>getSetting(TRACE_NAME_END)));
            } catch (IOException e) {
                failedOpeningTraceFile(client, server, e);
                continue; // Todo: Should this exit the program?
            }

            try {
                final ProxyRecorder proxy = new ProxyRecorder(
                        connections.size(), client, server,
                        new SinkWriteTrace(diskFilters, traceOut, settings.<Boolean>getSetting(TRACE_DYNAMIC),
                                connections.size(), firstConnectionTime.getTime()),
                        console.forConnection(connections.size()),
                        forwardFilters, settings, versionKnowledge);
                connections.add(proxy);
                proxy.startThreads();
            } catch (IOException e) {
                failedStarting(client, server, traceOut, e);
                continue; // Todo: Should this exit the program?
            }
        }
    }

    private static void failedAcceptingConnection(IOException e) {
        System.err.println("Incoming connection: Failed accepting new connection");
        e.printStackTrace();
    }

    private static void failedConnectingToServer(Socket client, IOException e) {
        System.err.println("Incoming connection: Failed connecting to server");
        e.printStackTrace();
        closeIt(client);
    }

    private static void failedOpeningTraceFile(Socket client, Socket server, IOException e) {
        System.err.println("Incoming connection: Failed opening trace file");
        e.printStackTrace();
        closeIt(client);
        closeIt(server);
    }

    private static void failedStarting(Socket client, Socket server, OutputStream traceOut, IOException e) {
        System.err.println("Incoming connection: Failed starting");
        e.printStackTrace();
        closeIt(traceOut);
        closeIt(client);
        closeIt(server);
    }

    private static void closeIt(Closeable toClose) {
        try {
            toClose.close();
        } catch (IOException e) {
            System.err.println("Problems while closing " + toClose);
            e.printStackTrace();
        }
    }

    private static boolean allAreFinished(ArrayList<ProxyRecorder> connections) {
        for (ProxyRecorder connection : connections)
            if (!connection.isFinished()) {
                return false;
            }

        return true;
    }

    private static void letThemFinish(ArrayList<ProxyRecorder> connections) {
        // wait for all the connections
        for (ProxyRecorder connection : connections)
            while (!connection.isFinished()) {
                Thread.yield();
            }
    }

    public ProxyRecorder(int connectionID, Socket client, Socket server, Sink traceSink, Sink console, Filter forwardFilters, ArgumentSettings settings, ProtocolData versionKnowledge)
            throws IOException, InterruptedException {
        final FreecivConnection clientCon = Plumbing.socket2Connection(client, versionKnowledge,
                settings.<Boolean>getSetting(UNDERSTAND),
                versionKnowledge.getRequiredPostReceiveRules(),
                versionKnowledge.getRequiredPostSendRules());

        final FreecivConnection serverCon = Plumbing.socket2Connection(server, versionKnowledge,
                settings.<Boolean>getSetting(UNDERSTAND),
                ReflexPacketKind.layer(versionKnowledge.getRequiredPostReceiveRules(), getServerConnectionReflexes()),
                versionKnowledge.getRequiredPostSendRules());

        final Source serverSource = new SourceConn(serverCon, false, connectionID);
        final Sink sinkServer = new SinkForward(serverCon, forwardFilters);

        final Source clientSource = new SourceConn(clientCon, true, connectionID);
        final Sink sinkClient = new SinkForward(clientCon, forwardFilters);

        this.csPlumbing = new Plumbing(clientSource, Arrays.asList(console, traceSink, sinkServer), timeToExit);
        this.scPlumbing = new Plumbing(serverSource, Arrays.asList(console, traceSink, sinkClient), timeToExit);
    }

    static private Filter buildTraceFilters(ArgumentSettings settings) {
        LinkedList<Filter> out = new LinkedList<Filter>();

        if (settings.<Boolean>getSetting(TRACE_EXCLUDE_CONNECTION))
            out.add(new FilterNot(CONNECTION_PACKETS));

        if (settings.<Boolean>getSetting(TRACE_EXCLUDE_C2S))
            out.add(new FilterNot(new FilterPacketFromClientToServer()));

        if (settings.<Boolean>getSetting(TRACE_EXCLUDE_S2C))
            out.add(new FilterOr(new FilterPacketFromClientToServer(), new FilterPacketKind(Arrays.asList(5))));

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
            public void apply(ConnectionRelated connection) {
                timeToExit[0] = true;
            }
        });
        return reflexes;
    }

    public void startThreads() throws IOException, InvocationTargetException {
        this.csPlumbing.start();
        this.scPlumbing.start();
    }

    public boolean isFinished() {
        return csPlumbing.isFinished() && scPlumbing.isFinished();
    }
}
