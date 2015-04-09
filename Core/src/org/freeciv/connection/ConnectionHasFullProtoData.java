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
    private final ProtocolVariant variant;

    protected ConnectionHasFullProtoData(InputStream inn, OutputStream out, Map<Integer, ReflexReaction> postReceive, Map<Integer, ReflexReaction> postSend, BasicProtocolData protoCode, ToPacket toPacket, ProtocolVariant protocolVariant, String loggerName) throws IOException {
        super(inn, out, postReceive, postSend, protoCode, toPacket, loggerName);

        this.variant = protocolVariant;
    }

    /**
     * Create a new instance of the server join request packet. The fields
     * that only has one correct value for a given protocol version will
     * get the correct value assigned to them automatically.
     * @param userName The username to sign in as.
     * @param optionalCaps wanted optional capabilities. Separate each
     *                     wanted capability by a space.
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
                                       final String optionalCaps) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return this.newPacketFromValues(4,
                userName,
                variant.getCapStringMandatory() + " " + optionalCaps,
                variant.getVersionLabel(),
                variant.getVersionMajor(),
                variant.getVersionMinor(),
                variant.getVersionPatch());
    }

    /**
     * Create a new instance of the server join request packet. The fields
     * that only has one correct value for a given protocol version will
     * get the correct value assigned to them automatically.
     * @param you_can_join true iff the client may join.
     * @param message welcome or rejection message to the client.
     * @param optionalCaps optional Freeciv protocol capabilities to enable.
     * @param challenge_file location of want hack challenge file.
     * @param conn_id connection identity number.
     * @return a new instance of SERVER_JOIN_REPLY
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
    public Packet newServerJoinReply(Boolean you_can_join,
                                     String message,
                                     String optionalCaps,
                                     String challenge_file,
                                     Integer conn_id) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        return this.newPacketFromValues(5,
                you_can_join,
                message,
                variant.getCapStringMandatory() + " " + optionalCaps,
                challenge_file,
                conn_id);
    }
}
