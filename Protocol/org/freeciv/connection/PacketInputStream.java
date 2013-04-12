/*
 * Copyright (c) 2013 Sveinung Kvilhaugsvik.
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

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PacketInputStream extends FilterInputStream {
    private final Over state;
    private final HeaderRead headerRead;

    public PacketInputStream(InputStream in, Over state, final Class<? extends PacketHeader> packetHeaderClass) {
        this(in, state, new HeaderRead(packetHeaderClass));
    }

    public PacketInputStream(InputStream in, Over state, final HeaderRead packetHeaderClass) {
        super(in);

        this.state = state;
        this.headerRead = packetHeaderClass;
    }

    public RawPacket readPacket() throws IOException, InvocationTargetException {
        return readPacket(in, state, headerRead);
    }

    private static RawPacket readPacket(InputStream from, Over state, HeaderRead headerRead)
            throws IOException, InvocationTargetException {
        final byte[] headerStart = readXBytesFrom(headerRead.headerSize, from, state, true);
        PacketHeader head;
        try {
            head = headerRead.headerReader.newInstance(new DataInputStream(new ByteArrayInputStream(headerStart)));
        } catch (InstantiationException e) {
            throw badHeader(e);
        } catch (IllegalAccessException e) {
            throw badHeader(e);
        }

        byte[] body = readXBytesFrom(head.getBodySize(), from, state, false);
        return new RawPacket(body, head);
    }

    private static IllegalStateException badHeader(Exception e) {
        return new IllegalStateException("Wrong data for reading headers", e);
    }

    private static byte[] readXBytesFrom(int wanted, InputStream from, Over state, boolean clean)
            throws IOException {
        byte[] out = new byte[wanted];
        int alreadyRead = 0;
        while(alreadyRead < wanted) {
            final int bytesRead;
            try {
                bytesRead = from.read(out, alreadyRead, wanted - alreadyRead);
            } catch (EOFException e) {
                throw done(wanted, clean, alreadyRead);
            }
            if (0 <= bytesRead)
                alreadyRead += bytesRead;
            else if (state.isOver())
                throw done(wanted, clean, alreadyRead);
            Thread.yield();
        }
        return out;
    }

    private static IOException done(int wanted, boolean clean, int alreadyRead) throws DoneReading, EOFException {
        if (clean && 0 == alreadyRead)
            return new DoneReading("Nothing to read and nothing is waiting");
        else
            return new EOFException("Nothing to read and nothing is waiting." +
                    "Read " + alreadyRead + " of " + wanted + " bytes");
    }

    public static class HeaderRead implements PacketChangeHeader {
        public Constructor<? extends PacketHeader> headerReader;
        public int headerSize;

        public HeaderRead(final Class<? extends PacketHeader> packetHeaderClass) {
            setHeaderTypeTo(packetHeaderClass);
        }

        public void setHeaderTypeTo(Class<? extends PacketHeader> packetHeaderClass) {
            try {
                this.headerReader = packetHeaderClass.getConstructor(DataInput.class);
                this.headerSize = packetHeaderClass.getField("HEADER_SIZE").getInt(null);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Could not find constructor for header interpreter", e);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Could not find header size in header interpreter", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Could not access header size in header interpreter", e);
            }
        }

        public boolean sameType(Packet packet) {
            return headerReader.getDeclaringClass().isInstance(packet.getHeader());
        }
    }
}
