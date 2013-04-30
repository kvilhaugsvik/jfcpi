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

import org.freeciv.packet.RawPacket;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Lock;

public class PacketInputStream extends FilterInputStream {
    private final Over state;
    private final HeaderData headerData;
    private final ReflexPacketKind quickRespond;
    private Lock completeReflexesInOneStep;

    public PacketInputStream(InputStream in, Over state, Lock completeReflexesInOneStep, final HeaderData packetHeaderClass, ReflexPacketKind quickRespond) {
        super(in);

        this.state = state;
        this.completeReflexesInOneStep = completeReflexesInOneStep;
        this.headerData = packetHeaderClass;
        this.quickRespond = quickRespond;
    }

    public RawPacket readPacket() throws IOException, InvocationTargetException {
        final byte[] start = readXBytesFrom(2, new byte[0], in, state);
        final int size = ((start[0] & 0xFF) << 8) | (start[1] & 0xFF);

        completeReflexesInOneStep.lock();
        try {
            byte[] packet = readXBytesFrom(size - 2, start, in, state);

            final RawPacket rawPacket = new RawPacket(packet, headerData);

            quickRespond.handle(rawPacket);

            return rawPacket;
        } finally {
            completeReflexesInOneStep.unlock();
        }
    }

    private static byte[] readXBytesFrom(int wanted, byte[] start, InputStream from, Over state)
            throws IOException {
        assert 0 <= wanted : "Can't read a negative number of bytes";

        byte[] out = new byte[wanted + start.length];
        System.arraycopy(start, 0, out, 0, start.length);

        int alreadyRead = 0;
        while(alreadyRead < wanted) {
            final int bytesRead;
            try {
                bytesRead = from.read(out, alreadyRead + start.length, wanted - alreadyRead);
            } catch (EOFException e) {
                throw done(wanted, start, alreadyRead);
            }
            if (0 <= bytesRead)
                alreadyRead += bytesRead;
            else if (state.isOver())
                throw done(wanted, start, alreadyRead);
            Thread.yield();
        }
        return out;
    }

    private static IOException done(int wanted, byte[] start, int alreadyRead) throws DoneReading, EOFException {
        final boolean clean = 0 == start.length;
        if (clean && 0 == alreadyRead)
            return new DoneReading("Nothing to read and nothing is waiting");
        else
            return new EOFException("Nothing to read and nothing is waiting." +
                    "Read " + alreadyRead + " of " + wanted + " bytes");
    }

}
