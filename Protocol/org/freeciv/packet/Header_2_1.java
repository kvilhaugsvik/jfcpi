/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

public class Header_2_1 extends PacketHeader {
    public static final int HEADER_SIZE = 3;

    public Header_2_1(DataInput in) throws IOException {
        this(getSizeThenKind(in));
    }

    public Header_2_1(int totalSize, int packetKind) {
        super(packetKind, totalSize - HEADER_SIZE, totalSize);
    }

    private Header_2_1(int[] sizeAndKind) {
        this(sizeAndKind[0], sizeAndKind[1]);
    }

    @Override public void encodeTo(DataOutput to) throws IOException {
        // header
        // length is 2 unsigned bytes
        to.writeChar(super.totalSize);
        // type
        to.writeByte(super.packetKind);
    }

    private static int[] getSizeThenKind(DataInput in) throws IOException {
        int size = in.readChar();
        int kind = in.readUnsignedByte();
        return new int[]{size, kind};
    }
}
