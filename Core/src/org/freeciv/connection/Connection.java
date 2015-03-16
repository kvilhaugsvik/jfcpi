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

import org.freeciv.packet.DeltaKey;
import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;
import org.freeciv.packet.RawPacket;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Connection implements FreecivConnection {
    private final BackgroundReader in;
    private final OutputStream out;

    private final OverImpl overImpl;
    private final ReflexPacketKind postSend;
    private final HeaderData currentHeader;
    private final ProtocolVariantAutomatic variant;

    private final String loggerName;

    private Connection(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            BasicProtocolData protoCode,
            ToPacket toPacket,
            ProtocolVariantAutomatic protocolVariant,
            String loggerName
    ) throws IOException {
        this.loggerName = loggerName;
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
            boolean interpreted,
            String loggerName
    ) throws IOException {
        if (interpreted)
            return interpreted(inn, out, postReceive, postSend, protoCode, loggerName);
        else
            return uninterpreted(inn, out, postReceive, postSend, protoCode, loggerName);
    }

    public static Connection interpreted(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            ProtocolData protoCode,
            String loggerName
    ) throws IOException {
        final ProtocolVariantAutomatic protocolVariant = new ProtocolVariantAutomatic(protoCode.getNewPacketMapper());
        return new Connection(inn, out, postReceive, postSend, protoCode,
                new InterpretWhenPossible(protocolVariant, loggerName),
                protocolVariant, loggerName);
    }

    public static Connection uninterpreted(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            final BasicProtocolData protoCode,
            String loggerName
    ) throws IOException {
        return new Connection(inn, out, postReceive, postSend, protoCode,
                new AlwaysRaw(), new ProtocolVariantAutomatic(null), loggerName);
    }

    public boolean packetReady() {
        return in.hasPacket();
    }

    public Packet getPacket() throws NotReadyYetException {
        if (!packetReady())
            throw new NotReadyYetException("No packets waiting");

        return in.getPacket();
    }

    public void send(Packet toSend) throws IOException {
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
                        new InterpretWhenPossible(variant, loggerName)
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

    /**
     * Creates a new instance of the specified Freeciv packet for the
     * current Freeciv protocol variant. The value of each field of the
     * packet body must be specified.
     * @param number the packet number of the Freeciv packet.
     * @param headerMaker constructor for the current packet header kind.
     * @param old the delta packet storage. This is where previously sent
     *            packets of the same kind can be found.
     * @param args the fields of the body of the packet.
     * @return a new instance of the specified packet.
     * @throws ClassNotFoundException if no packet with the given number
     * exists.
     * @throws NoSuchMethodException if the packet don't have the expected
     * method. Can be caused by wrong arguments, by the wrong number of
     * arguments or by the packet being created by an incompatible packet
     * generator.
     * @throws java.lang.reflect.InvocationTargetException if there is a
     * problem while creating the packet.
     * @throws IllegalAccessException if accessing this is forbidden by
     * Java's access control.
     */
    public Packet newPacketFromValues(final int number,
                                      final Constructor<? extends PacketHeader> headerMaker,
                                      final Map<DeltaKey, Packet> old,
                                      final Object... args) throws ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        return variant.newPacketFromValues(number, headerMaker, old, args);
    }
}
