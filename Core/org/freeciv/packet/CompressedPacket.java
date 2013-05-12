/*
 * Copyright (c) 2013, Sveinung Kvilhaugsvik
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

import org.freeciv.connection.Over;
import org.freeciv.connection.PacketInputStream;
import org.freeciv.utility.Util;

import java.io.*;

public abstract class CompressedPacket implements Packet {
    protected final byte[] content;
    protected final PacketHeader header;
    private final boolean jumbo;

    public CompressedPacket(final int startSize, Over state, InputStream in,
                            final int COMPRESSION_BORDER, final int JUMBO_SIZE) throws IOException {
        if (startSize < COMPRESSION_BORDER) {
            throw new IllegalArgumentException("This isn't a compressed packet");
        } else if (startSize < JUMBO_SIZE) {
            this.jumbo = false;
            this.content = readCompressed(startSize, state, in);
        } else {
            this.jumbo = true;
            this.content = readCompressed(readJumboSize(in) - 4, state, in);
        }

        final int headerSize = jumbo ? 2 + 4 : 2;

        this.header = new CompressedHeader(headerSize, this.content.length, jumbo, JUMBO_SIZE);
    }

    private static byte[] readCompressed(int size, Over state, InputStream in) throws IOException {
        return PacketInputStream.readXBytesFrom(size - 2, new byte[0], in, state);
    }

    private static int readJumboSize(InputStream in) throws IOException {
        final int jumboSize = new DataInputStream(in).readInt();

        if (jumboSize < 0)
            throw new UnsupportedOperationException("A packet larger than a signed int can specify the length of isn't supported");

        return jumboSize;
    }

    @Override
    public PacketHeader getHeader() {
        return header;
    }

    @Override
    public void encodeTo(DataOutput to) throws IOException {
        header.encodeTo(to);
        to.write(content);
    }

    @Override
    public String toString() {
        return "Compressed package" +
                "\n\theader = " + header +
                Util.joinStringArray(content, ", ", "\n\tbody (raw data) = (", ")");
    }

    private static class CompressedHeader extends PacketHeader {
        private final int headerSize;

        private final boolean jumbo;
        private final int JUMBO_SIZE;

        public CompressedHeader(int headerSize, int bodySize, boolean jumbo, int JUMBO_SIZE) {
            super(0xffff, bodySize, bodySize + headerSize);

            this.JUMBO_SIZE = JUMBO_SIZE;

            this.headerSize = headerSize;
            this.jumbo = jumbo;
        }

        @Override
        public int getHeaderSize() {
            return headerSize;
        }

        @Override
        public void encodeTo(DataOutput to) throws IOException {
            if (jumbo) {
                to.writeChar(JUMBO_SIZE);
                to.writeInt(totalSize);
            } else {
                to.writeChar(totalSize);
            }
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName() +
                    "(" + (jumbo ? "jumbo" : "not jumbo") + ", " + "size = " + getTotalSize() + ")";
        }
    }
}
