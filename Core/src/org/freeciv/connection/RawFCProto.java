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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class RawFCProto implements SerializedPacketGroup.SeparateSerializedPacketGroups {
    private final Over state;
    private final ToPacket toPacket;
    private final HeaderData headerData;
    private final ReflexPacketKind quickRespond;
    private final BasicProtocolData protoCode;

    public RawFCProto(Over state, ToPacket toPacket, HeaderData headerData, ReflexPacketKind quickRespond, BasicProtocolData protoCode) {
        this.state = state;
        this.toPacket = toPacket;
        this.headerData = headerData;
        this.quickRespond = quickRespond;
        this.protoCode = protoCode;
    }

    @Override
    public SerializedPacketGroup fromInputStream(InputStream in) throws IOException {
        return readSerializedPacketGroup(in, state, protoCode, toPacket, headerData, quickRespond);
    }

    private static SerializedPacketGroup readSerializedPacketGroup(InputStream from,
                                                                   Over state, BasicProtocolData protoCode,
                                                                   ToPacket toPacket, HeaderData headerData,
                                                                   ReflexPacketKind quickRespond) throws IOException {
        final byte[] start = PacketInputStream.readXBytesFrom(2, new byte[0], from, state);
        final int startSize = ((start[0] & 0xFF) << 8) | (start[1] & 0xFF);

        if (startSize < 2)
            throw new IllegalStateException("Packet size can't be this small: " + startSize);

        if (startSize < protoCode.getCompressionBorder()) {
            final byte[] packet = readNormalPacket(start, startSize, from, state);

            return new SerializedSinglePacket(packet, toPacket, headerData, quickRespond);
        } else if (startSize < protoCode.getJumboSize()) {
            final byte[] packet = readNormalPacket(start, startSize - protoCode.getCompressionBorder(), from, state);

            return new SerializedCompressedPackets(packet, false, protoCode, toPacket, headerData, quickRespond);
        } else {
            final byte[] packet = readJumboPacket(start, from, state);

            return new SerializedCompressedPackets(packet, true, protoCode, toPacket, headerData, quickRespond);
        }
    }

    private static byte[] readNormalPacket(byte[] start, int startSize, InputStream from, Over state) throws IOException {
        return PacketInputStream.readXBytesFrom(startSize - 2, start, from, state);
    }

    private static byte[] readJumboPacket(byte[] start, InputStream from, Over over) throws IOException {
        final byte[] lenBytes = PacketInputStream.readXBytesFrom(4, start, from, over);
        final int jumboSize = ByteBuffer.wrap(lenBytes).getInt(2);

        if (jumboSize < 0)
            throw new UnsupportedOperationException("A packet larger than a signed int can measure isn't supported");

        return PacketInputStream.readXBytesFrom(jumboSize - (2 + 4), lenBytes, from, over);
    }
}
