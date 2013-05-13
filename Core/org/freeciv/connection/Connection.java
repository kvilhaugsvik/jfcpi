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

package org.freeciv.connection;

import org.freeciv.packet.CompressedPacket;
import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Connection implements FreecivConnection {
    private final BackgroundReader in;
    private final OutputStream out;

    private final OverImpl overImpl;
    private final ReflexPacketKind postSend;
    private final HeaderData currentHeader;

    private Connection(
            final InputStream inn,
            final OutputStream out,
            final HeaderData headerData,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            PacketsMapping protoCode,
            boolean interpreted
    ) throws IOException {
        this.currentHeader = headerData;
        this.out = out;
        this.overImpl = new OverImpl() {
            @Override
            protected void whenDoneImpl() {
                try {
                    out.close();
                } catch (IOException e) {
                    System.err.println("Problems while closing the connection's output. Packets may not have been sent");
                    e.printStackTrace();
                }
                try {
                    inn.close();
                } catch (IOException e) {
                    System.err.println("Problems while closing the connection's input");
                    e.printStackTrace();
                }
            }
        };
        final ReentrantLock completeReflexesInOneStep = new ReentrantLock();
        this.in = new BackgroundReader(inn, this,
                new ReflexPacketKind(postReceive, this, completeReflexesInOneStep), currentHeader, protoCode, interpreted);
        this.postSend = new ReflexPacketKind(postSend, this, completeReflexesInOneStep);

        this.in.start();
    }

    public static Connection full(
            final InputStream inn,
            final OutputStream out,
            final HeaderData headerData,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            PacketsMapping protoCode,
            boolean interpreted
    ) throws IOException {
        return new Connection(inn, out, headerData, postReceive, postSend, protoCode, interpreted);
    }

    public static Connection interpreted(
            final InputStream inn,
            final OutputStream out,
            final HeaderData headerData,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            PacketsMapping protoCode
    ) throws IOException {
        return new Connection(inn, out, headerData, postReceive, postSend, protoCode, true);
    }

    public static Connection uninterpreted(
            final InputStream inn,
            final OutputStream out,
            final HeaderData headerData,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            final PacketsMapping protoCode
    ) throws IOException {
        return new Connection(inn, out, headerData, postReceive, postSend, protoCode, false);
    }

    public boolean packetReady() {
        return in.hasPacket();
    }

    public Packet getPacket() throws NotReadyYetException {
        if (!packetReady())
            throw new NotReadyYetException("No packets waiting");

        return in.getPacket();
    }

    public void toSend(Packet toSend) throws IOException {
        if (!isOpen()) {
            throw new IOException("Is closed. Can't send.");
        } else if ((!currentHeader.sameType(toSend)) && !(toSend instanceof CompressedPacket)) {
            throw new IllegalArgumentException("Unexpected header kind " +
                    toSend.getHeader().getClass().getCanonicalName());
        }

        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getHeader().getTotalSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        this.postSend.startedReceivingOrSending();
        try {
            out.write(packetSerialized.toByteArray());
            this.postSend.handle(toSend.getHeader().getPacketKind());
        } catch (IOException e) {
            setStopReadingWhenOutOfInput();
            whenDone();
            throw new IOException("Can't send", e);
        } finally {
            this.postSend.finishedRunningTheReflexes();
        }
    }

    @Override
    public void setHeaderTypeTo(Class<? extends PacketHeader> newKind) {
        currentHeader.setHeaderTypeTo(newKind);
    }

    @Override
    public Constructor<? extends PacketHeader> getStream2Header() {
        return currentHeader.getStream2Header();
    }

    @Override
    public Constructor<? extends PacketHeader> getFields2Header() {
        return currentHeader.getFields2Header();
    }

    @Override
    public void setStopReadingWhenOutOfInput() {
        overImpl.setStopReadingWhenOutOfInput();
    }

    @Override
    public void whenDone() {
        overImpl.whenDone();
    }

    @Override
    public boolean shouldIStopReadingWhenOutOfInput() {
        return overImpl.shouldIStopReadingWhenOutOfInput();
    }

    @Override
    public boolean isOpen() {
        return overImpl.isOpen();
    }
}
