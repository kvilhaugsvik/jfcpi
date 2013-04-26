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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class RawPacket implements Packet {
    private final PacketHeader header;
    private final byte[] content;

    public RawPacket(byte[] packet, HeaderData stream2Header) throws InvocationTargetException {
        try {
            this.header = stream2Header.getStream2Header()
                    .newInstance(new DataInputStream(new ByteArrayInputStream(packet)));
        } catch (InstantiationException e) {
            throw badHeader(e);
        } catch (IllegalAccessException e) {
            throw badHeader(e);
        }
        this.content = Arrays.copyOfRange(packet, header.getHeaderSize(), packet.length);
    }

    private static IllegalStateException badHeader(Exception e) {
        return new IllegalStateException("Wrong data for reading headers", e);
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
        return header.getPacketKind() + " (not interpreted)" +
                "\n\theader = " + header +
                Util.joinStringArray(content, ", ", "\n\tbody (raw data) = (", ")");
    }
}
