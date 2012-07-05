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

import java.io.*;

public class RawPacket implements Packet {
    private final boolean hasTwoBytePacketNumber;
    private final int size;
    private final int kind;
    private final byte[] content;

    public RawPacket(DataInput in, int size, int kind, boolean hasTwoBytePacketNumber) throws IOException {
        this.hasTwoBytePacketNumber = hasTwoBytePacketNumber;
        this.size = size;
        this.kind = kind;
        content = new byte[size - (hasTwoBytePacketNumber ? 4 : 3)];
        in.readFully(content);
    }

    public RawPacket(DataInput in, int size, int kind) throws IOException {
        this(in, size, kind, true);
    }

    public int getNumber() {
        return kind;
    }

    public void encodeTo(DataOutput to) throws IOException {
        // header
        // length is 2 unsigned bytes
        to.writeChar(getEncodedSize());
        // type
        if (hasTwoBytePacketNumber) to.writeChar(kind); else to.writeByte(kind);

        to.write(content);
    }

    public int getEncodedSize() {
        return size;
    }

    @Override public String toString() {
        String out = "(" + kind + ")\t";
        for (byte part: content) {
            out += ((int)part) + "\t";
        }
        return out + "\n";
    }
}
