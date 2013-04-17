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
import org.freeciv.packet.PACKET_CONN_PONG;
import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;
import org.freeciv.recorder.traceFormat2.RecordTF2;
import org.freeciv.utility.ArgumentSettings;
import org.freeciv.utility.Setting;
import org.freeciv.utility.UI;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

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

    private final TraceFormat2Read source;
    private final SinkForward toServer;
    private final boolean ignoreDynamic;

    public PlayToServer(InputStream source, Socket server, boolean ignoreDynamic) throws IOException, NoSuchMethodException {
        final PacketsMapping versionKnowledge = new PacketsMapping();
        final Class<? extends PacketHeader> packetHeaderClass = versionKnowledge.getPacketHeaderClass();
        this.source = new TraceFormat2Read(source, new OverImpl(), packetHeaderClass);

        final HashMap<Integer, ReflexReaction> reflexes = new HashMap<Integer, ReflexReaction>();
        reflexes.put(88, new ReflexReaction() {
            @Override
            public void apply(Packet incoming, FreecivConnection connection) {
                try {
                    connection.toSend(new PACKET_CONN_PONG(connection.getFields2Header()));
                } catch (IOException e) {
                    System.err.println("Failed to respond");
                }
            }
        });
        reflexes.put(8, new ReflexReaction() {
            @Override
            public void apply(Packet incoming, FreecivConnection connection) {
                connection.setOver();
            }
        });
        final FreecivConnection conn = new Uninterpreted(server, packetHeaderClass,
                ReflexPacketKind.layer(versionKnowledge.getRequiredPostReceiveRules(), reflexes),
                versionKnowledge.getRequiredPostSendRules());
        this.toServer = new SinkForward(conn, new FilterNot(new FilterOr(
                new FilterNot(new FilterPacketFromClientToServer()),
                ProxyRecorder.CONNECTION_PACKETS)));
        this.ignoreDynamic = ignoreDynamic;
    }

    public void run() throws IOException, InvocationTargetException {
        final long beganPlaying = System.currentTimeMillis();

        RecordTF2 rec = source.readRecord();
        long sendNextAt = beganPlaying;

        while (true) {
            if (rec.ignoreMe || !rec.isClientToServer()) {
                rec = source.readRecord();
                continue;
            }

            sendNextAt = source.isDynamic() && !ignoreDynamic ? beganPlaying + rec.when : sendNextAt + 1000;
            while (System.currentTimeMillis() < sendNextAt) // TODO: Evaluate if more precision is needed
                Thread.yield();

            toServer.filteredWrite(rec.isClientToServer(), rec.packet);

            rec = source.readRecord();
        }
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
