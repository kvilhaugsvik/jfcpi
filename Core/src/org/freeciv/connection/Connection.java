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

import org.freeciv.packet.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A connection speaking the Freeciv protocol.
 */
public class Connection implements FreecivConnection {
    private final BackgroundReader in;
    private final OutputStream out;

    private final OverImpl overImpl;
    private final ReflexPacketKind postSend;
    private final HeaderData currentHeader;
    private final ToPacket deserializer;

    private final String loggerName;

    protected Connection(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            BasicProtocolData protoCode,
            ToPacket toPacket,
            String loggerName
    ) throws IOException {
        this.deserializer = toPacket;
        this.loggerName = loggerName;
        this.currentHeader = protoCode.getNewPacketHeaderData();
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

    /**
     * Create a new connection.
     * @param inn the connection's incoming stream.
     * @param out the connection's outgoing stream.
     * @param postReceive reflexes to run on a packet as soon as it is
     *                    received.
     * @param postSend reflexes to run on a packet as soon as it is sent.
     * @param protoCode place to find the code for the protocol.
     * @param interpreted should the new connection be interpreted?
     * @param loggerName where errors should be logged.
     * @return a new Freeciv protocol connection.
     * @throws IOException if there is a problem setting up the
     *                     connection.
     */
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

    /**
     * Create a new connection that interprets the packets it receives and
     * sends so individual fields are accessible. Use this if you need to
     * know the value of a field in a packet body or need to create a
     * packet that has fields in its body.
     * @param inn the connection's incoming stream.
     * @param out the connection's outgoing stream.
     * @param postReceive reflexes to run on a packet as soon as it is
     *                    received.
     * @param postSend reflexes to run on a packet as soon as it is sent.
     * @param protoCode place to find the code for the protocol.
     * @param loggerName where errors should be logged.
     * @return a new Freeciv protocol connection.
     * @throws IOException if there is a problem setting up the
     *                     connection.
     */
    public static ConnectionHasFullProtoData interpreted(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            ProtocolData protoCode,
            String loggerName
    ) throws IOException {
        final ProtocolVariantAutomatic protocolVariant = new ProtocolVariantAutomatic(protoCode.getNewPacketMapper());
        return new ConnectionHasFullProtoData(inn, out, postReceive, postSend, protoCode,
                new InterpretWhenPossible(protocolVariant, loggerName),
                protocolVariant, loggerName);
    }

    /**
     * Create a new connection that won't interpret the packets it receives
     * and sends. This makes it impossible to access any field of the
     * packet body without interpreting it manually. This is meant to be
     * used in cases where access to individual packet fields aren't wanted
     * enough to justify the time spent interpreting them and potential
     * bugs in packet interpretation code.
     * @param inn the connection's incoming stream.
     * @param out the connection's outgoing stream.
     * @param postReceive reflexes to run on a packet as soon as it is
     *                    received.
     * @param postSend reflexes to run on a packet as soon as it is sent.
     * @param protoCode place to find the code for the protocol.
     * @param loggerName where errors should be logged.
     * @return a new Freeciv protocol connection.
     * @throws IOException if there is a problem setting up the
     *                     connection.
     */
    public static Connection uninterpreted(
            final InputStream inn,
            final OutputStream out,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend,
            final BasicProtocolData protoCode,
            String loggerName
    ) throws IOException {
        return new Connection(inn, out, postReceive, postSend, protoCode,
                new AlwaysRaw(), loggerName);
    }

    /**
     * Connect as a Freeciv client to a Freeciv server.
     * Interpret the received packets.
     * Takes care of signing in. Responds when the server pings the client.
     * Logs problems to Logger.GLOBAL_LOGGER_NAME
     * @param address the address of the Freeciv server.
     * @param portNumber the port number of the Freeciv server.
     * @param userName the user name to sign in as
     * @return a connection to the Freeciv server.
     * @throws IOException if an I/O error occurs while setting up the
     * connection.
     * @throws InvocationTargetException if there is a problem while
     * creating the sign in packet.
     * @throws IllegalAccessException if accessing the sign in packet
     * is forbidden by Java's access control.
     * @throws BadProtocolData when the Freeciv protocol data is
     * incompatible.
     */
    public static ConnectionHasFullProtoData signInAsClient(
            final String address,
            final int portNumber,
            final String userName
    ) throws IOException, InvocationTargetException, IllegalAccessException, BadProtocolData {
        return ConnectionHelper.signInAsClient(address, portNumber, userName);
    }

    @Override public boolean packetReady() {
        return in.hasPacket();
    }

    @Override public Packet getPacket() throws NotReadyYetException {
        if (!packetReady())
            throw new NotReadyYetException("No packets waiting");

        return in.getPacket();
    }

    @Override public void send(Packet toSend) throws IOException {
        if (!isOpen()) {
            throw new IOException("Is closed. Can't send.");
        } else if (!currentHeader.sameType(toSend)) {
            throw new IllegalArgumentException("Unexpected header kind " +
                    toSend.getHeader().getClass().getCanonicalName());
        }

        this.postSend.startedReceivingOrSending();
        try {
            final byte[] asBytes = deserializer.encode(toSend, currentHeader);

            out.write(asBytes);

            this.postSend.handle(toSend.getHeader().getPacketKind());
        } catch (IOException | IllegalAccessException e) {
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
     * WARNING: Don't use this for a packet with body fields unless the
     * connection is interpreted.
     * @param number the packet number of the Freeciv packet.
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
                                      final Object... args) throws ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        return deserializer.newPacketFromValues(number, currentHeader, args);
    }

    /**
     * Create a new instance of the ping packet for the current Freeciv
     * protocol variant.
     * @return a new instance of the ping packet.
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
    public Packet newPing() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return this.newPacketFromValues(88);
    }

    /**
     * Create a new instance of the pong packet for the current Freeciv
     * protocol variant.
     * @return a new instance of the pong packet.
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
    public Packet newPong() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return this.newPacketFromValues(89);
    }
}
