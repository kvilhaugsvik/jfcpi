/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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
import org.freeciv.recorder.traceFormat2.RecordTF2;
import org.freeciv.utility.ArgumentSettings;
import org.freeciv.utility.Setting;
import org.freeciv.utility.UI;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

public class PlayToServer {
    private static final String TRACE_FILE = "file";
    private static final String ADDRESS = "address";
    private static final String PORT = "port";
    private static final String IGNORE_TIME = "ignore-time";

    public static final LinkedList<Setting<?>> SETTINGS = new LinkedList<Setting<?>>() {{
        add(new Setting.StringSetting(TRACE_FILE,
                ProxyRecorder.DEFAULT_TRACE_PREFIX + "0" + ProxyRecorder.DEFAULT_TRACE_SUFFIX,
                "the file(s) containing the trace to play back. To specify many files separate them using '" +
                        System.getProperty("path.separator", ":") + "'"));
        add(new Setting.StringSetting(ADDRESS, "127.0.0.1", "connect to the Freeciv server on this address"));
        add(new Setting.IntSetting(PORT, 5556, "connect to the Freeciv server on ths port"));
        add(new Setting.BoolSetting(IGNORE_TIME, false, "ignore time data in the trace"));
        add(UI.HELP_SETTING);
    }};

    private static boolean[] timeToExit = {false};
    private final Plumbing csPlumbing;
    private final Plumbing scPlumbing;

    public PlayToServer(final InputStream source, Socket server, boolean ignoreDynamic, FirstTimeRequest firstPlayedTime) throws IOException, NoSuchMethodException {
        final ProtocolData versionKnowledge = new ProtocolData();

        final HashMap<Integer, ReflexReaction> reflexes = createStandardReflexes(versionKnowledge);

        final FreecivConnection conn = Connection.uninterpreted(server.getInputStream(), server.getOutputStream(),
                ReflexPacketKind.layer(versionKnowledge.getRequiredPostReceiveRules(), reflexes),
                versionKnowledge.getRequiredPostSendRules(),
                versionKnowledge, Logger.GLOBAL_LOGGER_NAME);
        Sink toServer = new SinkForward(conn, new FilterNot(new FilterOr(
                new FilterNot(new FilterPacketFromClientToServer()),
                ProxyRecorder.CONNECTION_PACKETS)));

        final Sink reaction = new WantHackProtocol(versionKnowledge);

        this.csPlumbing = new Plumbing(new SourceTF2(source, new OverImpl() {
            @Override
            protected void whenDoneImpl() {
                try {
                    source.close();
                } catch (IOException e) {
                    System.err.println("Problem closing input file after it was read");
                    e.printStackTrace();
                }
            }
        }, versionKnowledge, ignoreDynamic, true, RecordTF2.NO_CONNECTION_ID, false, firstPlayedTime.getTime()),
                Arrays.asList(reaction, toServer), timeToExit);
        this.scPlumbing = new Plumbing(new SourceConn(conn, false, RecordTF2.NO_CONNECTION_ID), Arrays.asList(reaction), timeToExit);
    }

    private static HashMap<Integer, ReflexReaction> createStandardReflexes(final ProtocolData versionKnowledge) {
        final HashMap<Integer, ReflexReaction> reflexes = new HashMap<Integer, ReflexReaction>();
        reflexes.put(88, new ReflexReaction<PacketWrite>() {
            @Override
            public void apply(PacketWrite connection) {
                try {
                    connection.send(versionKnowledge.newPong(connection.getFields2Header()));
                } catch (IOException e) {
                    System.err.println("Failed to respond");
                }
            }
        });
        reflexes.put(8, new ReflexReaction<Over>() {
            @Override
            public void apply(Over connection) {
                connection.setStopReadingWhenOutOfInput();
            }
        });

        return reflexes;
    }

    public void startThreads() throws IOException, InvocationTargetException {
        this.csPlumbing.start();
        this.scPlumbing.start();
    }

    public static void main(String[] args) throws InvocationTargetException, IOException, InterruptedException {
        ArgumentSettings settings = new ArgumentSettings(SETTINGS, args);

        UI.printAndExitOnHelp(settings, ProxyRecorder.class);

        final FirstTimeRequest firstPlayedTime = new FirstTimeRequest();

        for (String fileName : settings.<String>getSetting(TRACE_FILE).split(System.getProperty("path.separator", ":")))
            startPlayBack(settings, firstPlayedTime, fileName);
    }

    private static void startPlayBack(ArgumentSettings settings, FirstTimeRequest firstPlayedTime, String fileName) throws IOException, InvocationTargetException {
        final InputStream traceInn;
        try {
            traceInn = new BufferedInputStream(new FileInputStream(fileName));
        } catch (IOException e) {
            System.err.println("Failed opening trace file " + fileName);
            System.exit(1);
            return;
        }

        final Socket server;
        try {
            server = new Socket(settings.<String>getSetting(ADDRESS),
                    settings.<Integer>getSetting(PORT));
        } catch (IOException e) {
            System.err.println("Failed connecting to server");
            e.printStackTrace();
            return;
        }

        PlayToServer me;
        try {
            me = new PlayToServer(traceInn, server, settings.<Boolean>getSetting(IGNORE_TIME), firstPlayedTime);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        } catch (NoSuchMethodException e) {
            System.err.println("Internal program error: Headers no properly ported");
            e.printStackTrace();
            System.exit(1);
            return;
        }
        me.startThreads();
    }
}
