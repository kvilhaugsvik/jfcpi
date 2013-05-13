/*
 * Copyright (c) 2012, 2013. Sveinung Kvilhaugsvik
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class BackgroundReader extends Thread {
    private final InputStream in;
    private final LinkedList<Packet> buffered;
    private final Connection parent;

    private final ToPacket toPacket;
    private final HeaderData headerData;
    private final ReflexPacketKind quickRespond;
    private final PacketsMapping protoCode;

    public BackgroundReader(InputStream in, Connection parent, ReflexPacketKind quickRespond,
                            final HeaderData currentHeader, PacketsMapping protoCode, boolean interpreted)
            throws IOException {
        this.in = in;
        this.parent = parent;
        this.buffered = new LinkedList<Packet>();
        this.toPacket = interpreted ? new InterpretWhenPossible(protoCode) : new AlwaysRaw();
        this.headerData = currentHeader;
        this.quickRespond = quickRespond;
        this.protoCode = protoCode;

        this.setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while(true) {
                SerializedPacketGroup incoming = readSerializedPacketGroup();

                synchronized (buffered) {
                    incoming.putPackets(buffered);
                }
            }
        } catch (DoneReading e) {
            // Looks good
        } catch (Exception e) {
            System.err.println("Problem in the thread that reads from the network");
            e.printStackTrace();
        } finally {
            parent.setStopReadingWhenOutOfInput();
            parent.whenDone();
        }
    }

    private SerializedPacketGroup readSerializedPacketGroup() throws IOException {
        final byte[] start = PacketInputStream.readXBytesFrom(2, new byte[0], in, parent);
        final int startSize = ((start[0] & 0xFF) << 8) | (start[1] & 0xFF);

        if (startSize < 2)
            throw new IllegalStateException("Packet size can't be this small: " + startSize);

        if (startSize < protoCode.getCompressionBorder()) {
            final byte[] packet = readNormalPacket(start, startSize);

            return new SerializedSinglePacket(packet, toPacket, headerData, quickRespond);
        } else if (startSize < protoCode.getJumboSize()) {
            final byte[] packet = readNormalPacket(start, startSize - protoCode.getCompressionBorder());

            throw new UnsupportedOperationException("Compressed packets not supported");
        } else {
            final byte[] packet = readJumboPacket(start);

            throw new UnsupportedOperationException("Compressed packets not supported");
        }
    }

    private byte[] readNormalPacket(byte[] start, int startSize) throws IOException {
        return PacketInputStream.readXBytesFrom(startSize - 2, start, in, parent);
    }

    private byte[] readJumboPacket(byte[] start) throws IOException {
        final byte[] lenBytes = PacketInputStream.readXBytesFrom(4, start, in, parent);
        final int jumboSize = ByteBuffer.wrap(lenBytes).getInt(2);

        if (jumboSize < 0)
            throw new UnsupportedOperationException("A packet larger than a signed int can measure isn't supported");

        return PacketInputStream.readXBytesFrom(jumboSize - (2 + 4), lenBytes, in, parent);
    }

    public boolean hasPacket() {
        synchronized (buffered) {
            return !buffered.isEmpty();
        }
    }

    public Packet getPacket() {
        synchronized (buffered) {
            return buffered.removeFirst();
        }
    }
}
