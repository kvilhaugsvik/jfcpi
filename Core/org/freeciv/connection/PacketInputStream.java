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

import org.freeciv.packet.DeltaKey;
import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;
import org.freeciv.packet.RawPacket;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

public class PacketInputStream extends FilterInputStream {
    private final Over state;
    private final HeaderData headerData;
    private final int COMPRESSION_BORDER;
    private final int JUMBO_SIZE;
    private final ReflexPacketKind quickRespond;
    private Lock completeReflexesInOneStep;
    private final DataToPackets dataToPackets;

    public PacketInputStream(InputStream in, Over state, Lock completeReflexesInOneStep, final HeaderData packetHeaderClass, ReflexPacketKind quickRespond, PacketsMapping protoCode, boolean interpreted) {
        super(in);

        this.state = state;
        this.completeReflexesInOneStep = completeReflexesInOneStep;
        this.headerData = packetHeaderClass;
        this.COMPRESSION_BORDER = protoCode.getCompressionBorder();
        this.JUMBO_SIZE = protoCode.getJumboSize();

        this.quickRespond = quickRespond;
        this.dataToPackets = interpreted ? new InterpretWhenPossible(protoCode) : new AlwaysRaw();
    }

    public Packet readPacket() throws IOException, InvocationTargetException {
        final byte[] start = readXBytesFrom(2, new byte[0], in, state);
        final int size = ((start[0] & 0xFF) << 8) | (start[1] & 0xFF);

        final PacketHeader head;
        final byte[] packet;
        completeReflexesInOneStep.lock();
        try {
            packet = readXBytesFrom(size - 2, start, in, state);

            head = headerData.newHeaderFromStream(new DataInputStream(new ByteArrayInputStream(packet)));

            quickRespond.handle(head.getPacketKind());
        } finally {
            completeReflexesInOneStep.unlock();
        }

        return dataToPackets.convert(head, packet);
    }

    public static byte[] readXBytesFrom(int wanted, byte[] start, InputStream from, Over state)
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
            } catch (SocketException e) {
                throw done(wanted, start, alreadyRead);
            }

            if (0 < bytesRead) // If some bytes were read
                alreadyRead += bytesRead; // take note of it
            else if (-1 == bytesRead || // If there stream is closed or
                    state.shouldIStopReadingWhenOutOfInput()) // nothing was read and should stop reading in that case
                throw done(wanted, start, alreadyRead); // it's done

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

    private static interface DataToPackets {
        Packet convert(PacketHeader head, byte[] remaining);
    }

    private static class AlwaysRaw implements DataToPackets {
        @Override
        public Packet convert(PacketHeader head, byte[] packet) {
            return new RawPacket(packet, head);
        }
    }

    private static class InterpretWhenPossible implements DataToPackets {
        private final PacketsMapping map;
        private final Map<DeltaKey, Packet> old;

        private InterpretWhenPossible(PacketsMapping map) {
            this.map = map;
            this.old = new HashMap<DeltaKey, Packet>();
        }

        @Override
        public Packet convert(PacketHeader head, byte[] packet) {
            try {
                DataInputStream body = new DataInputStream(new ByteArrayInputStream(packet));

                int toSkip = head.getHeaderSize();
                while (0 < toSkip)
                    toSkip = toSkip - body.skipBytes(toSkip);

                return map.interpret(head, body, old);
            } catch (IOException e) {
                return new RawPacket(packet, head);
            }
        }
    }
}
