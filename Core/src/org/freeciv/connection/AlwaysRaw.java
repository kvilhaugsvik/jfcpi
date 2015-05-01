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
import org.freeciv.packet.RawPacket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Serialize and deserialize packets to raw packet data but don't interpret
 * them. Deserialized packets will be instances of RawPacket.
 */
public class AlwaysRaw implements ToPacket {
    @Override
    public Packet convert(byte[] packet, HeaderData headerData) {
        return new RawPacket(packet, headerData);
    }

    @Override
    public byte[] encode(Packet packet, final HeaderData headerData) throws IOException, IllegalAccessException {
        return packet.toBytes();
    }

    @Override
    public Packet newPacketFromValues(int number, HeaderData headerMaker, Object... args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        /* Support ping and pong by allowing body less packets. */

        if (args.length != 0) {
            /* Only body less packets are supported. We are after all always raw. */
            throw new UnsupportedOperationException("Packets with bodies not supported");
        }

        /* Create a fitting header. */
        PacketHeader header = headerMaker.newHeader(headerMaker.getHeaderSize(), number);

        return new RawPacket(header);
    }
}
