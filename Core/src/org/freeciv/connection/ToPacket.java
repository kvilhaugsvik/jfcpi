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

package org.freeciv.connection;

import org.freeciv.packet.Packet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Serialize and deserialize packets to raw packet data.
 */
public interface ToPacket {
    /**
     * Deserialize a packet.
     * @param packet the packet data to interpret as a packet.
     * @param headerData packet header interpreter.
     * @return the deserialized packet.
     */
    Packet convert(byte[] packet, HeaderData headerData);

    /**
     * Serialize a packet.
     * @param packet the packet object to serialize.
     * @param headerData packet header interpreter. This is needed in case
     *                   the packet must be converted.
     * @return serialized packet data.
     * @throws IOException when there was a problem serializing the packet.
     * @throws IllegalAccessException if the encoding tries to access
     *                                something Java's access control
     *                                forbids.
     */
    byte[] encode(final Packet packet, final HeaderData headerData) throws IOException, IllegalAccessException;

    /**
     * Creates a new instance of the specified Freeciv packet for the
     * current Freeciv protocol variant. The value of each field of the
     * packet body must be specified.
     * @param number the packet number of the Freeciv packet.
     * @param headerMaker current packet header kind creation helper.
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
    Packet newPacketFromValues(final int number,
                               final HeaderData headerMaker,
                               final Object... args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException;
}
