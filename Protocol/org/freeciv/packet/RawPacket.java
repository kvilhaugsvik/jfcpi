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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RawPacket implements Packet {
    int size;
    int kind;
    byte[] content;

    public RawPacket(DataInput in, int size, int kind) throws IOException {
        this.size = size;
        this.kind = kind;
        content = new byte[size - 3];
        in.readFully(content);
    }

    public int getNumber() {
        return kind;
    }

    public void encodeTo(DataOutput to) throws IOException {
        // header
        // length is 2 unsigned bytes
        to.writeChar(getEncodedSize());
        // type
        to.writeByte(kind);

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
