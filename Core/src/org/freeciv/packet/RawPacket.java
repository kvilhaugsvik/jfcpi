/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

package org.freeciv.packet;

import org.freeciv.utility.Util;
import org.freeciv.connection.HeaderData;

import java.io.*;
import java.util.Arrays;

/**
 * RawPacket is a packet where only the header is interpreted.
 */
public class RawPacket implements Packet {
    private final PacketHeader header;
    private final byte[] content;

    /**
     * Create a new RawPacket from the given content.
     * @param stream2Header will read the header from the content
     * @param packet the entire packet (header and body) encoded as a byte
     *               array.
     */
    public RawPacket(byte[] packet, HeaderData stream2Header) {
        this.header = stream2Header.newHeaderFromStream(new DataInputStream(new ByteArrayInputStream(packet)));

        /* Use a copy. The original array could be changed. */
        this.content = Arrays.copyOf(packet, header.getTotalSize());
    }

    /**
     * Create a new RawPacket that only contains a header.
     * Some packet types don't have a body. Create one of those.
     * @param header the header.
     */
    public RawPacket(PacketHeader header) {
        if (header.getBodySize() != 0) {
            throw new UnsupportedOperationException("Asked to create packet with body from header alone.");
        }

        this.header = header;
        this.content = header.toBytes();
    }

    private byte[] getBodyBytes(byte[] packet, PacketHeader header) {
        return Arrays.copyOfRange(packet, header.getHeaderSize(), header.getTotalSize());
    }

    @Override public PacketHeader getHeader() {
        return header;
    }

    /**
     * Get the encoded body without the header.
     * @return the encoded body.
     */
    public byte[] getBodyBytes() {
        return getBodyBytes(content, this.header);
    }

    @Override public void encodeTo(DataOutput to) throws IOException {
        header.encodeTo(to);
        to.write(this.getBodyBytes());
    }

    @Override public byte[] toBytes() throws IOException {
        /* Return a copy so this version can't be corrupted. */
        return Arrays.copyOf(content, header.getTotalSize());
    }

    @Override public String toString() {
        return header.getPacketKind() + " (not interpreted)" +
                "\n\theader = " + header +
                Util.joinStringArray(this.getBodyBytes(), ", ", "\n\tbody (raw data) = (", ")");
    }
}
