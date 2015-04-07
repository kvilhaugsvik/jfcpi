/*
 * Copyright (c) 2012 - 2015. Sveinung Kvilhaugsvik
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * A Connection for packets that has the full protocol data.
 */
public class ConnectionHasFullProtoData extends Connection {
    private final ProtocolVariantAutomatic variant;

    protected ConnectionHasFullProtoData(InputStream inn, OutputStream out, Map<Integer, ReflexReaction> postReceive, Map<Integer, ReflexReaction> postSend, BasicProtocolData protoCode, ToPacket toPacket, ProtocolVariantAutomatic protocolVariant, String loggerName) throws IOException {
        super(inn, out, postReceive, postSend, protoCode, toPacket, loggerName);

        this.variant = protocolVariant;
    }

    /**
     * Creates a new instance of the specified Freeciv packet for the
     * current Freeciv protocol variant. The value of each field of the
     * packet body must be specified.
     * @param number the packet number of the Freeciv packet.
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
                                      final Map<DeltaKey, Packet> old,
                                      final Object... args) throws ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        return variant.newPacketFromValues(number, this.currentHeader, old, args);
    }

    /**
     * Create a new instance of the ping packet for the current Freeciv
     * protocol variant.
     * @param old the delta packet storage. This is where previously sent
     *            packets of the same kind can be found.
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
    public Packet newPing(final Map<DeltaKey, Packet> old) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return this.newPacketFromValues(88, old);
    }

    /**
     * Create a new instance of the pong packet for the current Freeciv
     * protocol variant.
     * @param old the delta packet storage. This is where previously sent
     *            packets of the same kind can be found.
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
    public Packet newPong(final Map<DeltaKey, Packet> old) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return this.newPacketFromValues(89, old);
    }

    /**
     * Create a new instance of the server join request packet. The fields
     * that only has one correct value for a given protocol version will
     * get the correct value assigned to them automatically.
     * @param userName The username to sign in as.
     * @param optionalCaps wanted optional capabilities. Separate each
     *                     wanted capability by a space.
     * @param old the delta packet storage. This is where previously sent
     *            packets of the same kind can be found.
     * @return a new instance of SERVER_JOIN_REQ
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
    public Packet newServerJoinRequest(final String userName,
                                       final String optionalCaps,
                                       final Map<DeltaKey, Packet> old) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return this.newPacketFromValues(4, old,
                userName,
                variant.getCapStringMandatory() + " " + optionalCaps,
                variant.getVersionLabel(),
                variant.getVersionMajor(),
                variant.getVersionMinor(),
                variant.getVersionPatch());
    }
}
