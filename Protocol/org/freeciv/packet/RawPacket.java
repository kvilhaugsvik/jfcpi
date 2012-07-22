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
    private final PacketHeader header;
    private final byte[] content;

    public RawPacket(DataInput in, PacketHeader header) throws IOException {
        this.header = header;
        content = new byte[header.getBodySize()];
        in.readFully(content);
    }

    public RawPacket(byte[] in, PacketHeader header) throws IOException {
        this.header = header;
        content = in;
    }

    public PacketHeader getHeader() {
        return header;
    }

    public byte[] getBodyBytes() {
        return content;
    }

    public void encodeTo(DataOutput to) throws IOException {
        header.encodeTo(to);
        to.write(content);
    }

    @Override public String toString() {
        String out = "(" + header.getPacketKind() + ")\t";
        for (byte part: content) {
            out += ((int)part) + "\t";
        }
        return out + "\n";
    }

    public int getNumber() {
        return header.getPacketKind();
    }

    public int getEncodedSize() {
        return header.getTotalSize();
    }
}
