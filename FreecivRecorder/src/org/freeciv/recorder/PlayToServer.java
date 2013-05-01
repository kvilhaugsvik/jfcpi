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
import org.freeciv.packet.*;
import org.freeciv.utility.ArgumentSettings;
import org.freeciv.utility.Setting;
import org.freeciv.utility.UI;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PlayToServer {
    private static final String TRACE_FILE = "file";
    private static final String ADDRESS = "address";
    private static final String PORT = "port";
    private static final String IGNORE_TIME = "ignore-time";
    public static final LinkedList<Setting<?>> SETTINGS = new LinkedList<Setting<?>>() {{
        add(new Setting.StringSetting(TRACE_FILE,
                ProxyRecorder.DEFAULT_TRACE_PREFIX + "0" + ProxyRecorder.DEFAULT_TRACE_SUFFIX,
                "the file containing the trace to play back"));
        add(new Setting.StringSetting(ADDRESS, "127.0.0.1", "connect to the Freeciv server on this address"));
        add(new Setting.IntSetting(PORT, 5556, "connect to the Freeciv server on ths port"));
        add(new Setting.BoolSetting(IGNORE_TIME, false, "ignore time data in the trace"));
        add(UI.HELP_SETTING);
    }};

    private static boolean[] timeToExit = {false};
    private final Plumbing plumbing;

    public PlayToServer(InputStream source, Socket server, boolean ignoreDynamic) throws IOException, NoSuchMethodException {
        final PacketsMapping versionKnowledge = new PacketsMapping();

        final HashMap<Integer, ReflexReaction> reflexes = createStandardReflexes();

        final FreecivConnection conn = new Uninterpreted(server.getInputStream(), server.getOutputStream(),
                versionKnowledge.getNewPacketHeaderData(),
                ReflexPacketKind.layer(versionKnowledge.getRequiredPostReceiveRules(), reflexes),
                versionKnowledge.getRequiredPostSendRules());
        Sink toServer = new SinkForward(conn, new FilterNot(new FilterOr(
                new FilterNot(new FilterPacketFromClientToServer()),
                ProxyRecorder.CONNECTION_PACKETS)));

        final Sink reaction = new SinkProcess(new FilterPacketKind(Arrays.asList(5, 160)), versionKnowledge) {
            String challengeFileName = null;

            @Override
            public void write(boolean clientToServer, Packet packet) throws IOException {
                final Packet interpreted = interpret(clientToServer, packet);

                switch (packet.getHeader().getPacketKind()) {
                    case 5:
                        challengeFileName = System.getProperty("user.home") + "/.freeciv/" +
                                ((PACKET_SERVER_JOIN_REPLY)interpreted).getChallenge_fileValue();
                        break;
                    case 160:
                        if (null == challengeFileName)
                            throw new IllegalStateException("Don't know the file name yet");

                        String challengeString = "[challenge]\ntoken=\"" +
                                ((PACKET_SINGLE_WANT_HACK_REQ)interpreted).getTokenValue() + "\"\n";

                        FileOutputStream challengeWrite = new FileOutputStream(challengeFileName);
                        try {
                            challengeWrite.write(challengeString.getBytes());
                        } finally {
                            challengeWrite.close();
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Should have been filtered");
                }
            }
        };

        final HashMap<Source, List<Sink>> sourcesToSinks = new HashMap<Source, List<Sink>>();
        sourcesToSinks.put(new SourceTF2(source, conn, versionKnowledge, ignoreDynamic, true),
                Arrays.asList(reaction, toServer));
        sourcesToSinks.put(new SourceConn(conn, false), Arrays.asList(reaction));
        this.plumbing = new Plumbing(sourcesToSinks, timeToExit);
    }

    private static HashMap<Integer, ReflexReaction> createStandardReflexes() {
        final HashMap<Integer, ReflexReaction> reflexes = new HashMap<Integer, ReflexReaction>();
        reflexes.put(88, new ReflexReaction<PacketWrite>() {
            @Override
            public void apply(Packet incoming, PacketWrite connection) {
                try {
                    connection.toSend(new PACKET_CONN_PONG(connection.getFields2Header()));
                } catch (IOException e) {
                    System.err.println("Failed to respond");
                }
            }
        });
        reflexes.put(8, new ReflexReaction<Over>() {
            @Override
            public void apply(Packet incoming, Over connection) {
                connection.setOver();
            }
        });

        return reflexes;
    }

    public void run() throws IOException, InvocationTargetException {
        this.plumbing.run();
    }

    public static void main(String[] args) throws InvocationTargetException, IOException, InterruptedException {
        ArgumentSettings settings = new ArgumentSettings(SETTINGS, args);

        UI.printAndExitOnHelp(settings, ProxyRecorder.class);

        final InputStream traceInn;
        try {
            traceInn = new BufferedInputStream(new FileInputStream(settings.<String>getSetting(TRACE_FILE)));
        } catch (IOException e) {
            System.err.println("Failed opening trace file " + TRACE_FILE);
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
            me = new PlayToServer(traceInn, server, settings.<Boolean>getSetting(IGNORE_TIME));
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
        me.run();
    }
}
