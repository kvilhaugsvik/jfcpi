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

import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;
import org.freeciv.packet.RawPacket;

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
    private final ProtocolVariantAutomatic variant;

    private Connection(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            BasicProtocolData protoCode,
            ToPacket toPacket,
            ProtocolVariantAutomatic protocolVariant
    ) throws IOException {
        this.currentHeader = protoCode.getNewPacketHeaderData();
        this.variant = protocolVariant;
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
        final ReflexPacketKind quickRespond = new ReflexPacketKind(postReceive, this, completeReflexesInOneStep);
        this.in = new BackgroundReader(inn, this,
                new RawFCProto(this, toPacket, currentHeader, quickRespond, protoCode));
        this.postSend = new ReflexPacketKind(postSend, this, completeReflexesInOneStep);

        this.in.start();
    }

    public static Connection full(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            ProtocolData protoCode,
            boolean interpreted
    ) throws IOException {
        if (interpreted)
            return interpreted(inn, out, postReceive, postSend, protoCode);
        else
            return uninterpreted(inn, out, postReceive, postSend, protoCode);
    }

    public static Connection interpreted(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            ProtocolData protoCode
    ) throws IOException {
        final ProtocolVariantAutomatic protocolVariant = new ProtocolVariantAutomatic(protoCode.getNewPacketMapper());
        return new Connection(inn, out, postReceive, postSend, protoCode,
                new InterpretWhenPossible(protocolVariant), protocolVariant);
    }

    public static Connection uninterpreted(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            final BasicProtocolData protoCode
    ) throws IOException {
        return new Connection(inn, out, postReceive, postSend, protoCode,
                new AlwaysRaw(), new ProtocolVariantAutomatic(null));
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
        } else if (!currentHeader.sameType(toSend)) {
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

            // need to look for capability setters in sending as well
            if (variant.needToKnowCaps())
                variant.extractVariantInfo(toSend instanceof RawPacket ?
                        new InterpretWhenPossible(variant)
                                .convert(toSend.getHeader(), packetSerialized.toByteArray()) :
                        toSend);
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
