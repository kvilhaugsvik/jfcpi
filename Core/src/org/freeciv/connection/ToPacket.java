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
import org.freeciv.packet.PacketHeader;

import java.io.IOException;

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
}
